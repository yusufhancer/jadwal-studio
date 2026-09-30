package com.jadwalstudio.app

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import com.jadwalstudio.app.data.*
import com.jadwalstudio.app.ui.screens.*
import com.jadwalstudio.app.ui.components.HeaderUser
import com.jadwalstudio.app.ui.components.LocalHeaderUser
import com.jadwalstudio.app.ui.theme.JadwalStudioTheme
import com.jadwalstudio.app.utils.TaskDateUtils
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class Screen { SPLASH, ONBOARDING, LOGIN, REGISTER, HOME, TASKS, TASK_DETAIL, TASK_EDIT,
    ADD_TASK_CHOICE, ADD_TASK_INDIVIDUAL, ADD_TASK_GROUP, CREATE_GROUP, GROUPS, GROUP_DETAIL,
    GROUP_MEMBERS, GROUP_INVITE, GROUP_LOGS, SCHEDULE, ADD_SCHEDULE }

class StudioModel(application: Application) : AndroidViewModel(application) {
    val repository = StudioRepository(application)
    var pendingInvite by mutableStateOf("")
}

class MainActivity : ComponentActivity() {
    private lateinit var model: StudioModel
    private fun acceptInvite(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "jadwalstudio" && uri.host == "join") {
            val token = uri.lastPathSegment.orEmpty()
            if (token.matches(Regex("^[A-Za-z0-9_-]{20,100}$"))) model.pendingInvite = token
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        acceptInvite(intent)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProvider(this)[StudioModel::class.java]
        acceptInvite(intent)
        setContent { JadwalStudioTheme { AppNavigation(model.repository, model.pendingInvite) { model.pendingInvite = "" } } }
    }
}

@Composable
fun AppNavigation(repository: StudioRepository, pendingInvite: String = "", onInviteConsumed: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var screen by rememberSaveable { mutableStateOf(Screen.SPLASH) }
    var subjectName by rememberSaveable { mutableStateOf("") }
    var day by rememberSaveable { mutableStateOf("Senin") }
    var groupId by rememberSaveable { mutableStateOf("") }
    var taskId by rememberSaveable { mutableStateOf("") }
    var editScheduleId by rememberSaveable { mutableStateOf("") }
    var returnFromTask by rememberSaveable { mutableStateOf(Screen.TASK_DETAIL) }
    var returnFromGroup by rememberSaveable { mutableStateOf(Screen.TASKS) }
    var returnFromGroups by rememberSaveable { mutableStateOf(Screen.TASKS) }
    var returnFromAddTask by rememberSaveable { mutableStateOf(Screen.TASKS) }
    var inviteLink by rememberSaveable { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var showServer by remember { mutableStateOf(false) }
    var serverValue by remember { mutableStateOf(repository.baseUrl) }

    fun work(action: suspend () -> Unit) {
        if (busy) return
        busy = true
        scope.launch {
            try { action() }
            catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                if (error is ApiException && error.code == 401 && screen != Screen.LOGIN) {
                    repository.clearSession()
                    message = "Sesi tidak valid. Silakan masuk kembali."
                    screen = Screen.LOGIN
                } else message = error.message ?: "Operasi gagal. Coba lagi."
            } finally { busy = false }
        }
    }
    suspend fun refreshAfterSave() {
        try { repository.refresh() }
        catch (error: Exception) {
            if (error is kotlinx.coroutines.CancellationException) throw error
            if (error is ApiException && error.code == 401) throw error
            message = "Perubahan tersimpan, tetapi daftar belum diperbarui. Buka profil lewat foto/inisial di header, lalu pilih Muat Ulang."
        }
    }
    fun mutate(method: String, path: String, body: JSONObject? = null, after: (JSONObject) -> Unit = {}) {
        work {
            val result = repository.request(method, path, body)
            refreshAfterSave()
            after(result)
        }
    }

    val state = repository.state
    val user = state.optJSONObject("user") ?: JSONObject()
    val uid = user.optString("id")
    val groups = state.items("groups")
    val group = groups.firstOrNull { it.getString("id") == groupId }
    val rawTasks = state.items("tasks")
    val tasks = rawTasks.map { row ->
        val gid = row.optString("group_id").takeUnless { it.isEmpty() || it == "null" }
        SubjectTaskItem(id = row.getString("id"), title = row.getString("title"), isUrgent = row.optInt("priority") == 1,
            isGroup = gid != null, due = row.optString("deadline"), completed = row.getString("status") == "Done",
            subject = row.getString("subject"), status = row.getString("status"), groupId = gid,
            attachedImageName = row.items("attachments").firstOrNull()?.optString("name"),
            groupName = groups.firstOrNull { it.getString("id") == gid }?.getString("name") ?: "",
            description = row.optString("description"), notes = row.optString("notes"))
    }
    val subjects = state.items("subjects")
    val subjectNames = remember(state) { mutableStateListOf<String>().apply { addAll(subjects.map { it.getString("name") }) } }
    val schedules = state.items("schedules").map { row -> ScheduleItem(
        id = row.getString("id"), subject = row.getString("subject"), day = row.getString("day"),
        startTime = row.getString("start_time"), endTime = row.getString("end_time"),
        room = row.optString("room"), teacher = row.optString("teacher"), note = row.optString("note"))
    }
    fun subjectId(name: String): String = subjects.firstOrNull { it.getString("name") == name }?.getString("id")
        ?: throw IllegalArgumentException("Tambahkan dan pilih mata pelajaran terlebih dahulu.")
    fun back() {
        screen = backDestination(screen, returnFromTask, returnFromGroup, returnFromGroups, returnFromAddTask)
    }
    fun createTask(title: String, subject: String, deadline: String, urgent: Boolean, uri: String?, name: String?,
                   description: String, notes: String, inGroup: Boolean) {
        work {
            require(title.isNotBlank()) { "Judul tugas wajib diisi." }
            val body = json("title" to title.trim(), "subjectId" to subjectId(subject),
                "deadline" to TaskDateUtils.normalizeDeadline(deadline), "priority" to urgent,
                "description" to description, "notes" to notes, "groupId" to if (inGroup) groupId else null,
                "assigneeIds" to listOf(uid))
            if (uri != null) body.put("attachment", repository.attachment(uri, name))
            val result = repository.request("POST", "/tasks", body)
            refreshAfterSave()
            taskId = result.getString("id")
            subjectName = subject
            returnFromTask = if (inGroup) Screen.GROUP_DETAIL else Screen.TASK_DETAIL
            screen = Screen.TASK_EDIT
        }
    }
    fun logout() { work { repository.logout(); screen = Screen.LOGIN; groupId = ""; taskId = ""; inviteLink = "" } }
    fun openGroup(id: String) {
        returnFromGroup = groupReturnDestination(screen)
        groupId = id; inviteLink = ""; screen = Screen.GROUP_DETAIL
    }
    fun openGroups() { returnFromGroups = screen; screen = Screen.GROUPS }
    fun openAddTask() { returnFromAddTask = screen; screen = Screen.ADD_TASK_CHOICE }
    fun join(code: String, onJoined: () -> Unit = {}) { mutate("POST", "/groups/join", json("token" to code)) {
        onJoined(); onInviteConsumed(); openGroup(it.getString("id"))
    } }

    LaunchedEffect(pendingInvite, repository.signedIn) {
        if (pendingInvite.isNotBlank() && repository.signedIn && screen != Screen.SPLASH) screen = Screen.GROUPS
    }

    LaunchedEffect(repository.signedIn) {
        if (repository.signedIn) {
            try { repository.refresh() }
            catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                message = error.message
                if (!repository.signedIn) screen = Screen.LOGIN
            }
        }
    }
    BackHandler(enabled = screen !in listOf(Screen.HOME, Screen.SPLASH, Screen.ONBOARDING, Screen.LOGIN) && !busy) { back() }

    CompositionLocalProvider(LocalHeaderUser provides HeaderUser(user.optString("name", ""), user.optString("email", ""),
        onRefresh = { work { repository.refresh() } }, photo = user.optString("photo"),
        onPhotoSelected = { uri -> work { repository.updatePhoto(uri); refreshAfterSave() } })) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                if (subjects.isEmpty() && screen in listOf(Screen.ADD_TASK_INDIVIDUAL, Screen.ADD_TASK_GROUP, Screen.ADD_SCHEDULE)) {
                    StudioPage("Tambahkan Mata Pelajaran", { back() }) {
                        Text("Buat mata pelajaran terlebih dahulu agar tugas dan jadwal terhubung dengan pelajaran yang benar.")
                        Button(onClick = { screen = Screen.TASKS }, modifier = Modifier.fillMaxWidth()) { Text("Tambahkan Mata Pelajaran") }
                    }
                } else when (screen) {
                    Screen.SPLASH -> SplashScreen { screen = if (repository.signedIn) { if (pendingInvite.isNotBlank()) Screen.GROUPS else Screen.HOME } else Screen.ONBOARDING }
                    Screen.ONBOARDING -> OnboardingScreen { screen = Screen.LOGIN }
                    Screen.LOGIN -> Column {
                        if (BuildConfig.DEBUG) TextButton(onClick = { serverValue = repository.baseUrl; showServer = true }) { Text("Koneksi server lokal") }
                        LoginScreen(
                            onLoginSuccess = { email, password, remember -> work { require(android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { "Format email tidak valid." }; require(password.length >= 8) { "Password minimal 8 karakter." }; repository.login(email, password, remember); screen = Screen.HOME } },
                            onNavigateToRegister = { screen = Screen.REGISTER },
                            onNavigateToForgotPassword = { message = "Reset password otomatis belum tersedia pada MVP. Hubungi pengelola server untuk bantuan akun." })
                    }
                    Screen.REGISTER -> RegisterScreen(
                        onRegisterSuccess = { name, email, password, confirmation -> work {
                            require(name.isNotBlank()) { "Nama wajib diisi." }
                            require(android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { "Format email tidak valid." }
                            require(password.length >= 8) { "Password minimal 8 karakter." }
                            require(password == confirmation) { "Konfirmasi password tidak sama." }
                            repository.register(name, email, password, confirmation)
                            screen = Screen.LOGIN
                            message = "Akun berhasil dibuat. Silakan masuk."
                        } }, onNavigateToLogin = { screen = Screen.LOGIN })
                    Screen.HOME -> DashboardScreen(tasks = tasks, schedules = schedules,
                        groups = groups.map { it.getString("id") to it.getString("name") },
                        onNavigateToTasks = { screen = Screen.TASKS }, onNavigateToSchedule = { screen = Screen.SCHEDULE },
                        onAddNewTask = { openAddTask() },
                        onNavigateToGroupDetail = { id -> if (id.isNotBlank()) openGroup(id) else openGroups() },
                        onLogout = { logout() })
                    Screen.TASKS -> TaskScreen(tasks = tasks, subjectList = subjectNames,
                        groups = groups.map { it.getString("id") to it.getString("name") }, onOpenGroup = { openGroup(it) },
                        onAddSubject = { name, onSaved -> work {
                            repository.request("POST", "/subjects", json("name" to name))
                            onSaved(); refreshAfterSave()
                        } },
                        onOpenTask = { taskId = it; returnFromTask = Screen.TASKS; screen = Screen.TASK_EDIT },
                        onDeleteSubject = { name -> work { repository.request("DELETE", "/subjects/${subjectId(name)}"); refreshAfterSave() } },
                        onJoinGroupWithCode = { code, onJoined -> join(code, onJoined) }, onNavigateToDashboard = { screen = Screen.HOME },
                        onNavigateToSchedule = { screen = Screen.SCHEDULE },
                        onNavigateToSubjectDetail = { subjectName = it; screen = Screen.TASK_DETAIL },
                        onNavigateToGroupDetail = { openGroups() },
                        onNavigateToAddTaskChoice = { openAddTask() }, onLogout = { logout() })
                    Screen.TASK_DETAIL -> TaskDetailScreen(subjectName = subjectName, tasks = tasks.filter { !it.isGroup },
                        onNavigateBack = { back() }, onNavigateToAddTask = { openAddTask() },
                        onNavigateToDashboard = { screen = Screen.HOME }, onNavigateToSchedule = { screen = Screen.SCHEDULE },
                        onToggleTaskComplete = { id -> mutate("PUT", "/tasks/$id", json("status" to if (tasks.first { it.id == id }.completed) "To Do" else "Done")) },
                        onDeleteTask = { mutate("DELETE", "/tasks/$it") },
                        onOpenTask = { taskId = it; returnFromTask = Screen.TASK_DETAIL; screen = Screen.TASK_EDIT })
                    Screen.TASK_EDIT -> {
                        val task = rawTasks.firstOrNull { it.getString("id") == taskId }
                        if (task == null) StudioPage("Detail Tugas", { back() }) { Text("Tugas tidak tersedia. Muat ulang data atau kembali.") }
                        else ConnectedTaskEditor(task, groups.firstOrNull { it.getString("id") == task.optString("group_id") }, uid, { back() },
                            onSave = { body, uri, name -> work {
                                if (uri != null) body.put("attachment", repository.attachment(uri, name))
                                repository.request("PUT", "/tasks/$taskId", body)
                                refreshAfterSave()
                                back()
                            } },
                            onOpenAttachment = { id -> work {
                                val (file, mime) = repository.downloadAttachment(id)
                                val uri = FileProvider.getUriForFile(context, context.packageName + ".files", file)
                                val intent = Intent(Intent.ACTION_VIEW).setDataAndType(uri, mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                context.startActivity(Intent.createChooser(intent, "Buka lampiran"))
                            } },
                            onDelete = { mutate("DELETE", "/tasks/$taskId") { back() } })
                    }
                    Screen.ADD_TASK_CHOICE -> AddTaskChoiceScreen({ back() }, { screen = Screen.ADD_TASK_INDIVIDUAL }, { openGroups() })
                    Screen.ADD_TASK_INDIVIDUAL -> AddIndividualTaskScreen(onNavigateBack = { back() }, availableSubjects = subjectNames,
                        onSaveSuccess = { a,b,c,d,e,f,g,h -> createTask(a,b,c,d,e,f,g,h,false) })
                    Screen.ADD_TASK_GROUP -> AddGroupTaskScreen(onNavigateBack = { back() }, availableSubjects = subjectNames,
                        onNavigateToNext = { a,b,c,d,e,f,g,h -> createTask(a,b,c,d,e,f,g,h,true) })
                    Screen.GROUPS -> GroupHubScreen(groups, { back() }, { screen = Screen.CREATE_GROUP }, { openGroup(it) }, { join(it) }, pendingInvite)
                    Screen.CREATE_GROUP -> CreateGroupScreen(onNavigateBack = { back() },
                        onCreateGroupSuccess = { name, description -> mutate("POST", "/groups", json("name" to name, "description" to description)) {
                            openGroup(it.getString("id")); inviteLink = "jadwalstudio://join/" + it.getString("inviteToken")
                        } })
                    Screen.GROUP_DETAIL -> if (group != null) ConnectedGroupDetail(group, rawTasks.filter { it.optString("group_id") == groupId }, uid,
                        { back() }, { screen = Screen.ADD_TASK_GROUP },
                        { taskId = it; returnFromTask = Screen.GROUP_DETAIL; screen = Screen.TASK_EDIT },
                        { screen = Screen.GROUP_MEMBERS }, { screen = Screen.GROUP_INVITE }, { screen = Screen.GROUP_LOGS },
                        { screen = Screen.HOME }, { screen = Screen.TASKS }, { screen = Screen.SCHEDULE })
                        else StudioPage("Kelompok", { back() }) { Text("Kelompok tidak tersedia atau akses telah dicabut.") }
                    Screen.GROUP_MEMBERS -> if (group != null) ConnectedMembersScreen(group, uid, { back() }) {
                        mutate("DELETE", "/groups/$groupId/members/$it")
                    }
                    else StudioPage("Anggota Kelompok", { back() }) { Text("Kelompok tidak tersedia atau akses telah dicabut.") }
                    Screen.GROUP_INVITE -> ConnectedInviteScreen(inviteLink, { back() },
                        { mutate("POST", "/groups/$groupId/invite") { inviteLink = "jadwalstudio://join/" + it.getString("inviteToken") } },
                        { context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, inviteLink), "Bagikan undangan")) },
                        { (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Undangan JadwalStudio", inviteLink)); message = "Link disalin." })
                    Screen.GROUP_LOGS -> {
                        val logs = state.items("logs").filter { it.getString("group_id") == groupId }.mapIndexed { index, row ->
                            GroupLogItem(id = row.getString("id"), groupName = group?.optString("name") ?: "", meetingNumber = "Meeting #${index + 1}",
                                date = row.getString("date"), attendees = row.getString("attendees"), decisions = row.getString("decisions"),
                                actionItems = row.getString("action_items"), nextMeetingDate = row.getString("next_meeting_date"),
                                canDelete = row.optString("created_by") == uid || group?.optString("owner_id") == uid)
                        }
                        GroupLogScreen(groupName = group?.optString("name") ?: "", logs = logs, onAddLog = { log, onSaved -> work {
                            repository.request("POST", "/groups/$groupId/logs", json("date" to TaskDateUtils.normalizeDeadline(log.date),
                                "attendees" to log.attendees, "decisions" to log.decisions, "actionItems" to log.actionItems,
                                    "nextMeetingDate" to TaskDateUtils.normalizeDeadline(log.nextMeetingDate)))
                            onSaved()
                            refreshAfterSave()
                        } }, onDeleteLog = { mutate("DELETE", "/logs/$it") }, onNavigateBack = { back() },
                            onNavigateToDashboard = { screen = Screen.HOME }, onNavigateToTasks = { screen = Screen.TASKS }, onNavigateToSchedule = { screen = Screen.SCHEDULE })
                    }
                    Screen.SCHEDULE -> ScheduleScreen(
                        onNavigateToDashboard = { screen = Screen.HOME }, onNavigateToTasks = { screen = Screen.TASKS },
                        onNavigateToAddSchedule = { editScheduleId = ""; screen = Screen.ADD_SCHEDULE },
                        onEditSchedule = { editScheduleId = it.id; screen = Screen.ADD_SCHEDULE },
                        onDeleteSchedule = { mutate("DELETE", "/schedules/$it") }, scheduleList = schedules, selectedDay = day,
                        onDaySelected = { day = it }, onLogout = { logout() })
                    Screen.ADD_SCHEDULE -> AddScheduleScreen(availableSubjects = subjectNames,
                        scheduleToEdit = schedules.firstOrNull { it.id == editScheduleId }, existingSchedules = schedules,
                        onNavigateBack = { back() }, onSaveSchedule = { schedule -> work {
                            repository.request(if (editScheduleId.isEmpty()) "POST" else "PUT",
                                if (editScheduleId.isEmpty()) "/schedules" else "/schedules/$editScheduleId",
                                json("subjectId" to subjectId(schedule.subject), "day" to schedule.day, "startTime" to schedule.startTime,
                                    "endTime" to schedule.endTime, "room" to schedule.room, "teacher" to schedule.teacher, "note" to schedule.note))
                            refreshAfterSave(); day = schedule.day; screen = Screen.SCHEDULE
                        } })
                }
            }
        }
    }
    if (showServer) AlertDialog(onDismissRequest = { showServer = false }, title = { Text("Server lokal") },
        text = { OutlinedTextField(serverValue, { serverValue = it }, label = { Text("Alamat API") }, supportingText = { Text("USB: http://127.0.0.1:8765 · Emulator: http://10.0.2.2:8765") }) },
        confirmButton = { TextButton(onClick = {
            try { repository.setServer(serverValue); showServer = false } catch (error: Exception) { message = error.message }
        }) { Text("Simpan") } }, dismissButton = { TextButton(onClick = { showServer = false }) { Text("Batal") } })
    if (message != null) AlertDialog(onDismissRequest = { message = null }, title = { Text("JadwalStudio") },
        text = { Text(message!!) }, confirmButton = { TextButton(onClick = { message = null }) { Text("Mengerti") } })
    if (busy) Dialog(onDismissRequest = {}, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
        Surface(shape = MaterialTheme.shapes.large) {
            Row(Modifier.padding(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(28.dp)); Text("Memproses…")
            }
        }
    }
}
