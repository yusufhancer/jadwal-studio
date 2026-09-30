package com.jadwalstudio.app.ui.screens

import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.jadwalstudio.app.data.items
import com.jadwalstudio.app.data.strings
import com.jadwalstudio.app.data.json
import com.jadwalstudio.app.ui.theme.BgCream
import org.json.JSONObject

@Composable
fun StudioPage(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(BgCream).statusBarsPadding().navigationBarsPadding().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") }
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
fun GroupHubScreen(groups: List<JSONObject>, onBack: () -> Unit, onCreate: () -> Unit,
                   onOpen: (String) -> Unit, onJoin: (String) -> Unit, initialInvite: String = "") {
    var code by remember(initialInvite) { mutableStateOf(initialInvite) }
    StudioPage("Tugas Kelompok", onBack) {
        Button(onClick = onCreate, modifier = Modifier.fillMaxWidth()) { Text("Buat Kelompok") }
        OutlinedTextField(code, { code = it }, label = { Text("Link atau kode undangan") }, modifier = Modifier.fillMaxWidth())
        OutlinedButton(onClick = { onJoin(code.trim()) }, enabled = code.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Gabung Kelompok") }
        if (groups.isEmpty()) Text("Belum ada kelompok. Buat kelompok atau bergabung lewat undangan.")
        groups.forEach { group ->
            Card(onClick = { onOpen(group.getString("id")) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(group.getString("name"), style = MaterialTheme.typography.titleMedium)
                    Text("${group.items("members").size} anggota", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun ConnectedGroupDetail(group: JSONObject, tasks: List<JSONObject>, userId: String, onBack: () -> Unit,
                         onAddTask: () -> Unit, onTask: (String) -> Unit, onMembers: () -> Unit,
                         onInvite: () -> Unit, onLogs: () -> Unit,
                         onDashboard: () -> Unit = {}, onTasks: () -> Unit = {}, onSchedule: () -> Unit = {}) {
    val owner = group.items("members").firstOrNull { it.getString("id") == group.getString("owner_id") }
    GroupDetailScreen(
        groupName = group.getString("name"),
        tasks = tasks.map { GroupTaskItem(it.getString("id"), it.getString("title"), it.optString("deadline"), it.getString("status")) },
        onNavigateBack = onBack, onNavigateToMembers = onMembers, onNavigateToInviteLink = onInvite,
        onNavigateToLogs = onLogs, onNavigateToDashboard = onDashboard, onNavigateToTasks = onTasks,
        onNavigateToSchedule = onSchedule, onAddTask = onAddTask, onOpenTask = onTask,
        ownerName = owner?.optString("name") ?: "", memberCount = group.items("members").size,
        createdDate = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale("id", "ID")).format(java.util.Date(group.getLong("created_at") * 1000)),
        isOwner = group.getString("owner_id") == userId)
}

@Composable
fun ConnectedMembersScreen(group: JSONObject, userId: String, onBack: () -> Unit, onRemove: (String) -> Unit) {
    var remove by remember { mutableStateOf<JSONObject?>(null) }
    StudioPage("Anggota Kelompok", onBack) {
        group.items("members").forEach { member ->
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(member.getString("name"), style = MaterialTheme.typography.titleMedium)
                    Text("${member.getString("role")} · ${member.getString("email")}", style = MaterialTheme.typography.bodySmall)
                    Text("${member.getInt("finished")} / ${member.getInt("assigned")} tugas selesai · ${member.getInt("contribution")}%")
                    if (group.getString("owner_id") == userId && member.getString("id") != userId) {
                        TextButton(onClick = { remove = member }) { Text("Hapus anggota", color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }
    remove?.let { member ->
        AlertDialog(onDismissRequest = { remove = null }, title = { Text("Hapus anggota?") },
            text = { Text("${member.getString("name")} akan kehilangan akses kelompok. Tugas yang tidak memiliki penerima lagi akan dialihkan ke owner.") },
            confirmButton = { TextButton(onClick = { onRemove(member.getString("id")); remove = null }) { Text("Hapus") } },
            dismissButton = { TextButton(onClick = { remove = null }) { Text("Batal") } })
    }
}

@Composable
fun ConnectedInviteScreen(link: String, onBack: () -> Unit, onGenerate: () -> Unit, onShare: () -> Unit, onCopy: () -> Unit) {
    var confirm by remember { mutableStateOf(false) }
    StudioPage("Link Undangan", onBack) {
        Text("Link berlaku hingga owner membuat link baru. Hanya anggota yang bergabung dengan undangan valid dapat membuka kelompok.")
        if (link.isNotBlank()) {
            OutlinedTextField(link, {}, readOnly = true, modifier = Modifier.fillMaxWidth(), label = { Text("Link aktif") })
            Button(onClick = onShare, modifier = Modifier.fillMaxWidth()) { Text("Bagikan Link") }
            OutlinedButton(onClick = onCopy, modifier = Modifier.fillMaxWidth()) { Text("Salin Link") }
        } else Text("Buat link undangan baru untuk dibagikan. Link lama tetap berlaku sampai kamu menggantinya.")
        OutlinedButton(onClick = { confirm = true }, modifier = Modifier.fillMaxWidth()) { Text("Buat Link Baru") }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("Ganti link undangan?") },
        text = { Text("Link sebelumnya akan langsung tidak berlaku.") },
        confirmButton = { TextButton(onClick = { confirm = false; onGenerate() }) { Text("Buat Link") } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("Batal") } })
}

@Composable
fun ConnectedTaskEditor(task: JSONObject, group: JSONObject?, userId: String, onBack: () -> Unit,
                        onSave: (JSONObject, String?, String?) -> Unit, onOpenAttachment: (String) -> Unit,
                        onDelete: () -> Unit) {
    var status by rememberSaveable(task.getString("id")) { mutableStateOf(task.getString("status")) }
    var description by rememberSaveable(task.getString("id")) { mutableStateOf(task.optString("description")) }
    var notes by rememberSaveable(task.getString("id")) { mutableStateOf(task.optString("notes")) }
    var assignees by rememberSaveable(task.getString("id")) { mutableStateOf(task.strings("assigneeIds").toList()) }
    var attachment by rememberSaveable { mutableStateOf<String?>(null) }
    var attachmentName by rememberSaveable { mutableStateOf<String?>(null) }
    var checkingAttachment by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val canEdit = task.getString("created_by") == userId || group?.optString("owner_id") == userId || userId in task.strings("assigneeIds")
    StudioPage("Detail Tugas", onBack) {
        Text(task.getString("title"), style = MaterialTheme.typography.headlineSmall)
        Text(task.getString("subject"), style = MaterialTheme.typography.bodyMedium)
        Text(com.jadwalstudio.app.utils.TaskDateUtils.getDueStatusLabel(task.optString("deadline"), completed = task.getString("status") == "Done"))
        Text("Status", style = MaterialTheme.typography.titleMedium)
        listOf("To Do", "In Progress", "Review", "Done").forEach { option ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = status == option, onClick = { status = option }, enabled = canEdit)
                Text(option)
            }
        }
        OutlinedTextField(description, { description = it }, label = { Text("Deskripsi tugas") }, readOnly = !canEdit, modifier = Modifier.fillMaxWidth(), minLines = 3)
        OutlinedTextField(notes, { notes = it }, label = { Text("Catatan pengerjaan") }, readOnly = !canEdit, modifier = Modifier.fillMaxWidth(), minLines = 3)
        if (group != null) {
            Text("Ditugaskan kepada", style = MaterialTheme.typography.titleMedium)
            group.items("members").forEach { member ->
                val id = member.getString("id")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(id in assignees, { checked -> assignees = if (checked) assignees + id else assignees - id }, enabled = canEdit)
                    Text(member.getString("name"), modifier = Modifier.weight(1f))
                }
            }
        }
        task.items("attachments").forEach { file ->
            OutlinedButton(onClick = { onOpenAttachment(file.getString("id")) }, modifier = Modifier.fillMaxWidth()) { Text(file.getString("name")) }
        }
        if (canEdit) {
            TaskAttachmentPicker(attachment, attachmentName, { uri, name -> attachment = uri; attachmentName = name }, onChecking = { checkingAttachment = it })
            Button(onClick = { onSave(json("status" to status, "description" to description, "notes" to notes, "assigneeIds" to assignees.toList()), attachment, attachmentName) },
                enabled = assignees.isNotEmpty() && !checkingAttachment, modifier = Modifier.fillMaxWidth()) { Text("SIMPAN PERUBAHAN") }
            TextButton(onClick = { confirmDelete = true }) { Text("Hapus Tugas", color = MaterialTheme.colorScheme.error) }
        } else Text("Kamu dapat melihat tugas ini. Perubahan tersedia untuk owner, pembuat, atau anggota yang ditugaskan.")
    }
    if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text("Hapus tugas?") },
        text = { Text("Tugas dan seluruh lampirannya akan dihapus.") },
        confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Hapus") } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Batal") } })
}
