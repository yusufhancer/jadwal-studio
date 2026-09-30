package com.jadwalstudio.app.ui.screens

import androidx.compose.runtime.saveable.rememberSaveable

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jadwalstudio.app.ui.components.AppBottomNavigationBar
import com.jadwalstudio.app.ui.components.BottomNavTab

// Model Log Kelompok Sesuai PRD Bagian 13 & 18
data class GroupLogItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val groupName: String,
    val meetingNumber: String = "Meeting #01",
    val date: String,
    val attendees: String,
    val decisions: String,
    val actionItems: String,
    val nextMeetingDate: String = "",
    val canDelete: Boolean = false
)

@Composable
fun GroupLogScreen(
    groupName: String = "Projek PPLG",
    logs: List<GroupLogItem> = emptyList(),
    onAddLog: (GroupLogItem, () -> Unit) -> Unit = { _, _ -> },
    onDeleteLog: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {}
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var deleteId by rememberSaveable { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3ED))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // Header Sub-Page: Back Button + Judul "Catatan Meeting"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color(0xFF2E4841),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Catatan Meeting",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF2E4841)
                    )
                    Text(
                        text = groupName,
                        fontSize = 12.sp,
                        color = Color(0xFF7A8680)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Konten: Empty State vs Daftar Log
            if (logs.isEmpty()) {
                // Empty state sesuai PRD Bagian 22 ("Belum ada catatan meeting." + Tombol "Tambah Log")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E0D3))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            color = Color(0xFFE2EAD9)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.EventNote,
                                    contentDescription = "Catatan Meeting",
                                    tint = Color(0xFF4F7366),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Belum Ada Catatan Meeting",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFF1F2925)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Catat hasil diskusi kelompok, pembagian tugas, dan tanggal pertemuan berikutnya.",
                            fontSize = 12.5.sp,
                            color = Color(0xFF7A8680),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F7366)),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Tambah Log",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tambah Log",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                // Daftar Riwayat Log Rapat
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    logs.forEachIndexed { index, log ->
                        MeetingLogCard(
                            log = log,
                            onDelete = { deleteId = log.id }
                        )
                    }
                }
            }
        }

        // Floating Action Button Tambah Log (jika logs tidak kosong)
        if (logs.isNotEmpty()) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF4F7366),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 110.dp, end = 20.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Catatan Meeting")
            }
        }

        // Bottom Navigation Bar
        AppBottomNavigationBar(
            selectedTab = BottomNavTab.TUGAS,
            onNavigateToDashboard = onNavigateToDashboard,
            onNavigateToTasks = onNavigateToTasks,
            onNavigateToSchedule = onNavigateToSchedule,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (deleteId != null) {
        AlertDialog(onDismissRequest = { deleteId = null },
            title = { Text("Hapus catatan rapat?") },
            text = { Text("Catatan ini akan dihapus permanen.") },
            confirmButton = { TextButton(onClick = {
                val id = deleteId
                deleteId = null
                if (id != null && logs.any { it.id == id && it.canDelete }) onDeleteLog(id)
            }) { Text("Hapus") } },
            dismissButton = { TextButton(onClick = { deleteId = null }) { Text("Batal") } })
    }
    if (showAddDialog) {
        AddGroupLogDialog(
            defaultMeetingNumber = "Meeting #${String.format(java.util.Locale.ROOT, "%02d", logs.size + 1)}",
            onDismiss = { showAddDialog = false },
            onSave = { newLog ->
                onAddLog(newLog.copy(groupName = groupName)) { showAddDialog = false }
            }
        )
    }
}

// ---------------- KARTU DETAIL LOG RAPAT ----------------
@Composable
private fun MeetingLogCard(
    log: GroupLogItem,
    onDelete: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E0D3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Kartu: Judul Meeting & Tanggal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFE2EAD9),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = log.meetingNumber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F7366),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Tanggal",
                            tint = Color(0xFF7A8680),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = log.date,
                            fontSize = 12.sp,
                            color = Color(0xFF7A8680)
                        )
                    }
                }

                if (log.canDelete) IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Text(
                        text = "✕",
                        fontSize = 14.sp,
                        color = Color(0xFF9E9E9E),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Peserta / Attendees
            if (log.attendees.isNotBlank()) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Attendees",
                        tint = Color(0xFF4F7366),
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Peserta Hadir:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F2925)
                        )
                        Text(
                            text = log.attendees,
                            fontSize = 12.5.sp,
                            color = Color(0xFF555555)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Keputusan / What was decided
            if (log.decisions.isNotBlank()) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Decisions",
                        tint = Color(0xFFC78C06),
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Keputusan Rapat:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F2925)
                        )
                        Text(
                            text = log.decisions,
                            fontSize = 12.5.sp,
                            color = Color(0xFF333333)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Action Items
            if (log.actionItems.isNotBlank()) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Action Items",
                        tint = Color(0xFF4F7366),
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Tindak Lanjut / Action Items:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F2925)
                        )
                        Text(
                            text = log.actionItems,
                            fontSize = 12.5.sp,
                            color = Color(0xFF333333)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Next Meeting Date
            if (log.nextMeetingDate.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8F6F0),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E0D3)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Next Meeting",
                            tint = Color(0xFF4F7366),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pertemuan Berikutnya: ${log.nextMeetingDate}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4F7366)
                        )
                    }
                }
            }
        }
    }
}

// ---------------- DIALOG FORM TAMBAH LOG MEETING ----------------
@Composable
fun AddGroupLogDialog(
    defaultMeetingNumber: String = "Meeting #01",
    onDismiss: () -> Unit = {},
    onSave: (GroupLogItem) -> Unit = {}
) {
    var meetingNumber by rememberSaveable { mutableStateOf(defaultMeetingNumber) }
    var date by rememberSaveable { mutableStateOf(java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ROOT).format(java.util.Date())) }
    var attendees by rememberSaveable { mutableStateOf("") }
    var decisions by rememberSaveable { mutableStateOf("") }
    var actionItems by rememberSaveable { mutableStateOf("") }
    var nextMeetingDate by rememberSaveable { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                // Header Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tambah Catatan Meeting",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF2E4841)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Text("✕", fontSize = 16.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFD9534F),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // 1. Judul / Nomor Meeting
                Text("NOMOR / JUDUL MEETING *", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = meetingNumber,
                    onValueChange = { meetingNumber = it },
                    placeholder = { Text("Contoh: Meeting #01", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4F7366),
                        unfocusedBorderColor = Color(0xFFDCD6CA)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Tanggal Rapat
                Text("TANGGAL RAPAT *", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    placeholder = { Text("Contoh: 28 Agustus 2026", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4F7366),
                        unfocusedBorderColor = Color(0xFFDCD6CA)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Peserta (Attendees)
                Text("PESERTA HADIR (ATTENDEES)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = attendees,
                    onValueChange = { attendees = it },
                    placeholder = { Text("Contoh: Yusuf, Raka, Dimas", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4F7366),
                        unfocusedBorderColor = Color(0xFFDCD6CA)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Keputusan yang Diambil (Decisions)
                Text("KEPUTUSAN RAPAT (WHAT WAS DECIDED) *", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = decisions,
                    onValueChange = { decisions = it },
                    placeholder = { Text("Contoh: Membagi project menjadi UI, database, dan testing.", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4F7366),
                        unfocusedBorderColor = Color(0xFFDCD6CA)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 5. Poin Aksi (Action Items)
                Text("TINDAK LANJUT (ACTION ITEMS)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = actionItems,
                    onValueChange = { actionItems = it },
                    placeholder = { Text("Contoh: Yusuf -> UI, Raka -> Database", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4F7366),
                        unfocusedBorderColor = Color(0xFFDCD6CA)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 6. Tanggal Meeting Berikutnya
                Text("TANGGAL MEETING BERIKUTNYA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = nextMeetingDate,
                    onValueChange = { nextMeetingDate = it },
                    placeholder = { Text("Contoh: 30 Agustus 2026", fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4F7366),
                        unfocusedBorderColor = Color(0xFFDCD6CA)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Tombol Simpan Log
                Button(
                    onClick = {
                        if (meetingNumber.isBlank() || decisions.isBlank() || attendees.isBlank() || actionItems.isBlank() || date.isBlank()) {
                            errorMessage = "Isi nomor meeting, tanggal, peserta, keputusan, dan tindak lanjut."
                            return@Button
                        }
                        try {
                            com.jadwalstudio.app.utils.TaskDateUtils.normalizeDeadline(date)
                            com.jadwalstudio.app.utils.TaskDateUtils.normalizeDeadline(nextMeetingDate)
                        } catch (error: IllegalArgumentException) {
                            errorMessage = error.message ?: "Tanggal tidak valid."
                            return@Button
                        }
                        onSave(
                            GroupLogItem(
                                groupName = "",
                                meetingNumber = meetingNumber.trim(),
                                date = date.trim(),
                                attendees = attendees.trim(),
                                decisions = decisions.trim(),
                                actionItems = actionItems.trim(),
                                nextMeetingDate = nextMeetingDate.trim()
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F7366))
                ) {
                    Text(
                        text = "SIMPAN CATATAN MEETING",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
