package com.jadwalstudio.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import com.jadwalstudio.app.ui.components.AppBottomNavigationBar
import com.jadwalstudio.app.ui.components.AppHeader
import com.jadwalstudio.app.ui.components.BottomNavTab
import com.jadwalstudio.app.utils.TaskDateUtils
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jadwalstudio.app.R

// Palet Warna Sesuai Desain Figma Halaman Tugas
val TaskBgCream = Color(0xFFF5F3ED)
val TaskHeaderGreen = Color(0xFF4F7366)
val TaskTitleDark = Color(0xFF2E4841)
val TaskChipActiveBg = Color(0xFF2D463E)
val TaskChipInactiveBg = Color(0xFFEDE8DF)
val TaskChipInactiveText = Color(0xFF444444)
val TaskFabGreen = Color(0xFF739387)
val TaskBottomBarBg = Color(0xFFF0ECE5)
val TaskBottomBarBorder = Color(0xFFDFD9CE)

data class TaskModel(
    val id: String,
    val title: String,
    val subject: String,
    val isGroup: Boolean,
    val dueText: String,
    val isUrgent: Boolean = false
)

@Composable
fun TaskScreen(
    tasks: List<SubjectTaskItem> = emptyList(),
    groups: List<Pair<String, String>> = emptyList(),
    onOpenGroup: (String) -> Unit = {},
    subjectList: androidx.compose.runtime.snapshots.SnapshotStateList<String> = remember { mutableStateListOf() },
    onDeleteSubject: (String) -> Unit = {},
    onAddSubject: (String, () -> Unit) -> Unit = { _, _ -> },
    onOpenTask: (String) -> Unit = {},
    onJoinGroupWithCode: (String, () -> Unit) -> Unit = { _, _ -> },
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onNavigateToSubjectDetail: (String) -> Unit = {},
    onNavigateToGroupDetail: () -> Unit = {},
    onNavigateToAddTaskChoice: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    // 0 = Semua, 1 = Tugas Individu, 2 = Tugas Klompok
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    var showAddSubjectDialog by rememberSaveable { mutableStateOf(false) }
    var showJoinGroupDialog by rememberSaveable { mutableStateOf(false) }
    var subjectToDelete by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TaskBgCream)
    ) {
        // Konten Utama (Jika dialog aktif, terapkan efek blur halus)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (showAddSubjectDialog) Modifier.blur(10.dp) else Modifier)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 164.dp)
        ) {
            // 1. Header Lengkung Hijau Sage (Logo Asli 64dp)
            AppHeader(onLogout = onLogout)

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Judul "Tugas" (Font Lora / Serif)
            Text(
                text = "Tugas",
                color = TaskTitleDark,
                fontSize = 22.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Filter Chips Pill: Semua, Tugas Individu, Tugas Klompok (Berfungsi Penuh)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TaskFilterChip(
                    text = "Semua",
                    isSelected = selectedFilterIndex == 0,
                    onClick = { selectedFilterIndex = 0 }
                )
                TaskFilterChip(
                    text = "Tugas Individu",
                    isSelected = selectedFilterIndex == 1,
                    onClick = { selectedFilterIndex = 1 }
                )
                TaskFilterChip(
                    text = "Tugas Kelompok",
                    isSelected = selectedFilterIndex == 2,
                    onClick = { selectedFilterIndex = 2 }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Filter Tugas berdasarkan tab terpilih, otomatis terurut sisa waktu terdekat
            val filteredTasks = remember(tasks, selectedFilterIndex) {
                val base = when (selectedFilterIndex) {
                    1 -> tasks.filter { !it.isGroup }
                    2 -> tasks.filter { it.isGroup }
                    else -> tasks
                }
                TaskDateUtils.sortTasksByPriorityAndDueDate(base)
            }

            // Area Konten: Jika belum ada mata pelajaran, tampilkan empty state ilustrasi dan tombol sesuai PRD
            if (subjectList.isEmpty() && selectedFilterIndex == 0 && tasks.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ilustrasi_tugas),
                        contentDescription = "Ilustrasi Belajar di Meja",
                        modifier = Modifier.size(240.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Belum Ada Mata Pelajaran",
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = TaskTitleDark,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Kamu belum menambahkan mata pelajaran.\nYuk, buat mata pelajaran pertamamu!",
                        fontSize = 13.5.sp,
                        color = Color(0xFF646A63),
                        lineHeight = 20.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    androidx.compose.material3.Button(
                        onClick = { showAddSubjectDialog = true },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = TaskFabGreen),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah Mapel",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tambahkan Mata Pelajaran",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Tampilkan Kategori Mata Pelajaran & Daftar Tugas
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header Mata Pelajaran
                    if (subjectList.isNotEmpty() && selectedFilterIndex != 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mata Pelajaran (${subjectList.size})",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TaskTitleDark,
                                fontFamily = FontFamily.Serif
                            )
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showAddSubjectDialog = true },
                                color = Color(0xFFE2EAD9)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Tambah mata pelajaran",
                                        tint = TaskHeaderGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Tambah",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TaskHeaderGreen
                                    )
                                }
                            }
                        }
                        subjectList.forEach { subject ->
                            SubjectCardItem(
                                subjectName = subject,
                                taskCount = tasks.count { !it.isGroup && it.subject.equals(subject, ignoreCase = true) },
                                onClick = {
                                    onNavigateToSubjectDetail(subject)
                                },
                                onDelete = { subjectToDelete = subject }
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Daftar Tugas Terkait Filter
                    Text(
                        text = "Daftar Tugas",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TaskTitleDark,
                        fontFamily = FontFamily.Serif
                    )

                    if (filteredTasks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Belum ada tugas pada kategori ini.",
                                color = Color(0xFF777777),
                                fontSize = 13.5.sp
                            )
                        }
                    } else {
                        filteredTasks.forEach { task ->
                            TaskRowCard(task = task, onClick = { onOpenTask(task.id) })
                        }
                    }
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedFilterIndex != 1) {
                    Text(
                        "Kelompok Saya (${groups.size})", fontSize = 19.sp,
                        fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = TaskTitleDark
                    )
                    if (groups.isEmpty()) {
                        Text("Belum bergabung dengan kelompok.", color = TaskChipInactiveText)
                    }
                    groups.forEach { (id, name) ->
                        Card(onClick = { onOpenGroup(id) }, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Group, contentDescription = null, tint = TaskHeaderGreen)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(name, fontWeight = FontWeight.SemiBold, color = TaskTitleDark)
                                    Text("${tasks.count { it.groupId == id }} tugas", fontSize = 12.sp, color = TaskChipInactiveText)
                                }
                                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null,
                                    modifier = Modifier.size(16.dp), tint = TaskHeaderGreen)
                            }
                        }
                    }
                }
                Button(onClick = onNavigateToGroupDetail, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TaskHeaderGreen)) {
                    Text("Kelola Kelompok")
                }
                TextButton(onClick = { showJoinGroupDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Gabung dengan kode undangan", color = TaskHeaderGreen)
                }
            }
        }

        // 5. Floating Action Button Bulat Sage Green (+) di Kanan Bawah
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 96.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable { showAddSubjectDialog = true },
                color = TaskFabGreen,
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Mata Pelajaran",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }

        // 6. Bottom Navigation Bar Sticky (Tab Tugas Aktif Sesuai media_1789792065543.jpg)
        AppBottomNavigationBar(
            selectedTab = BottomNavTab.TUGAS,
            onNavigateToDashboard = onNavigateToDashboard,
            onNavigateToTasks = { },
            onNavigateToSchedule = onNavigateToSchedule,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // 7. Pop-up Tambah Mata Pelajaran dengan Backdrop Dim & Blur Sesuai media_1789271708996.png
        if (showAddSubjectDialog) {
            AddSubjectDialog(
                onDismiss = { showAddSubjectDialog = false },
                onSave = { subjectName ->
                    onAddSubject(subjectName.trim()) { showAddSubjectDialog = false }
                }
            )
        }

        // Pop-up Gabung Kelompok dengan Kode Undangan
        if (showJoinGroupDialog) {
            JoinGroupDialog(
                onDismiss = { showJoinGroupDialog = false },
                onJoin = { code ->
                    onJoinGroupWithCode(code) { showJoinGroupDialog = false }
                }
            )
        }

        // Konfirmasi Hapus Mata Pelajaran
        if (subjectToDelete != null) {
            AlertDialog(
                onDismissRequest = { subjectToDelete = null },
                title = {
                    Text(
                        text = "Hapus Mata Pelajaran",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                },
                text = {
                    Text("Apakah kamu yakin ingin menghapus mata pelajaran '$subjectToDelete'? Tugas individu dan jadwal terkait juga akan terhapus. Mata pelajaran yang dipakai tugas kelompok tidak dapat dihapus.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteSubject(subjectToDelete!!)
                            subjectToDelete = null
                        }
                    ) {
                        Text("Hapus", color = Color(0xFFD9534F), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { subjectToDelete = null }) {
                        Text("Batal", color = Color(0xFF555555))
                    }
                }
            )
        }
    }
}


// ---------------- FILTER CHIP PILL ----------------
@Composable
private fun TaskFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = if (isSelected) TaskChipActiveBg else TaskChipInactiveBg
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else TaskChipInactiveText,
            fontSize = 12.5.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

// ---------------- SUBJECT CARD ITEM ----------------
@Composable
private fun SubjectCardItem(
    subjectName: String,
    taskCount: Int = 0,
    onClick: () -> Unit,
    onDelete: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E0D3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(TaskHeaderGreen)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = subjectName,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = TaskTitleDark
                    )
                    Text(
                        text = "$taskCount tugas aktif",
                        fontSize = 11.5.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Mata Pelajaran",
                        tint = Color(0xFFD9534F),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Buka Tugas",
                    tint = Color(0xFF7A8680),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// ---------------- TASK ROW CARD ----------------
@Composable
private fun TaskRowCard(task: SubjectTaskItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E0D3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!task.completed && TaskDateUtils.getDaysRemaining(task.due) < 0) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(84.dp)
                        .background(Color(0xFFD9534F))
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = Color(0xFFE9E1E3),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (task.isUrgent) "PENTING" else "TUGAS",
                            color = Color(0xFF6F3B40),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        color = Color(0xFFF0F2F0),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (task.isGroup) "KELOMPOK" else "INDIVIDU",
                            color = Color(0xFF334A3E),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.title,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = TaskTitleDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = task.subject,
                        fontSize = 11.5.sp,
                        color = Color(0xFF666666)
                    )
                    val dueLabel = TaskDateUtils.getDueStatusLabel(task.due, task.isUrgent, task.completed)
                    val displayText = if (task.due.equals(dueLabel, ignoreCase = true) || dueLabel == "Selesai") {
                        task.due
                    } else {
                        "${task.due} • $dueLabel"
                    }
                    Text(
                        text = displayText,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!task.completed && TaskDateUtils.getDaysRemaining(task.due) < 0) Color(0xFFD9534F) else Color(0xFF666666)
                    )
                }
            }
        }
    }
}

// ---------------- POP UP TAMBAH MATA PELAJARAN (media_1789271708996.png) ----------------
@Composable
fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var subjectName by rememberSaveable { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Consume clicks to prevent dismiss, but children still receive touch
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Kartu Form Putih Berbingkai Halus
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E0D3)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp)
                    ) {
                        // Label: Nama Mata Pelajaran
                        Text(
                            text = "Nama Mata Pelajaran",
                            color = Color(0xFF555555),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Input Field
                        OutlinedTextField(
                            value = subjectName,
                            onValueChange = { subjectName = it },
                            placeholder = {
                                Text(
                                    text = "Contoh: Fisika",
                                    color = Color(0xFF888899),
                                    fontSize = 14.5.sp
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TaskHeaderGreen,
                                unfocusedBorderColor = Color(0xFFE5E0D3),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            textStyle = TextStyle(fontSize = 14.5.sp, color = Color(0xFF333333))
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Deskripsi Bantuan Sesuai Desain
                        Text(
                            text = "Pastikan nama mata pelajaran unik untuk menghindari kebingungan dalam jadwal Anda.",
                            color = Color(0xFF777777),
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tombol Hijau Sage "+ Simpan Mata Pelajaran"
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .clickable {
                            if (subjectName.isNotBlank()) {
                                onSave(subjectName)
                            }
                        },
                    color = Color(0xFF466A5D),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color(0xFF466A5D),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Simpan Mata Pelajaran",
                            color = Color.White,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ---------------- DIALOG GABUNG KELOMPOK DENGAN KODE ----------------
@Composable
fun JoinGroupDialog(
    onDismiss: () -> Unit = {},
    onJoin: (String) -> Unit = {}
) {
    var inviteCode by rememberSaveable { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gabung Kelompok",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = TaskTitleDark
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Text("✕", fontSize = 16.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Masukkan kode undangan atau token yang dibagikan oleh ketua kelompokmu.",
                    fontSize = 12.5.sp,
                    color = Color(0xFF666666)
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (errorMsg.isNotEmpty()) {
                    Text(
                        text = errorMsg,
                        color = Color(0xFFD9534F),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Text(
                    text = "KODE UNDANGAN / LINK *",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF555555)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it; errorMsg = "" },
                    placeholder = { Text("Contoh: JS-PPLG-892", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TaskHeaderGreen,
                        unfocusedBorderColor = Color(0xFFDCD6CA)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (inviteCode.isBlank()) {
                            errorMsg = "Kode undangan tidak boleh kosong!"
                            return@Button
                        }
                        onJoin(inviteCode.trim())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TaskHeaderGreen)
                ) {
                    Text(
                        text = "GABUNG SEKARANG",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
