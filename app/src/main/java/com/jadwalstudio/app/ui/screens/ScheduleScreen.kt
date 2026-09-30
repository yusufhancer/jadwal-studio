package com.jadwalstudio.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.semantics.Role
import com.jadwalstudio.app.ui.components.AppBottomNavigationBar
import com.jadwalstudio.app.ui.components.AppHeader
import com.jadwalstudio.app.ui.components.BottomNavTab
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jadwalstudio.app.R

// Model Data Jadwal Pelajaran
data class ScheduleItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val subject: String,
    val day: String,
    val startTime: String,
    val endTime: String,
    val room: String,
    val teacher: String,
    val note: String = ""
)

// Warna Sesuai Desain Figma
val SchBgCream = Color(0xFFF5F3ED)
val SchHeaderGreen = Color(0xFF4F7366)
val SchActiveDayGreen = Color(0xFF486958)
val SchPillBorder = Color(0xFF2E4841)
val SchCardBorder = Color(0xFFEBE6DC)
val SchClockBadgeBg = Color(0xFFDFE7E1)
val SchClockTextGreen = Color(0xFF355347)
val SchFabGreen = Color(0xFF78988A)
val SchBottomBarBg = Color(0xFFF0ECE5)
val SchBottomBarBorder = Color(0xFFDFD9CE)

@Composable
fun ScheduleScreen(
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToAddSchedule: () -> Unit = {},
    onEditSchedule: (ScheduleItem) -> Unit = {},
    onDeleteSchedule: (String) -> Unit = {},
    scheduleList: List<ScheduleItem> = emptyList(),
    selectedDay: String = "Senin",
    onDaySelected: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val days = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat")
    val currentDaySchedules = scheduleList.filter { it.day.equals(selectedDay, ignoreCase = true) }.sortedBy { it.startTime }
    var scheduleToDelete by remember { mutableStateOf<ScheduleItem?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SchBgCream)
    ) {
        // Konten Scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = 100.dp)
        ) {
            // 1. Header JadwalStudio (Konsisten dengan Dashboard & Tugas)
            AppHeader(onLogout = onLogout)

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Day Selector Pills (Senin, Selasa, Rabu, Kamis, Jumat)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .selectableGroup()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                days.forEach { day ->
                    val isActive = day.equals(selectedDay, ignoreCase = true)
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .selectable(selected = isActive, role = Role.Tab, onClick = { onDaySelected(day) }),
                        color = if (isActive) SchActiveDayGreen else SchBgCream,
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isActive) SchActiveDayGreen else SchPillBorder
                        )
                    ) {
                        Text(
                            text = day,
                            color = if (isActive) Color.White else Color(0xFF333333),
                            fontSize = 13.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Konten Jadwal: Empty State vs Filled State
            if (currentDaySchedules.isEmpty()) {
                // Tampilan Kosong (Gambar 1)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 24.dp, end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ilustrasi_jadwal_empty),
                        contentDescription = "Belum Ada Jadwal",
                        modifier = Modifier.size(240.dp, 160.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Belum Ada Jadwal",
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E2621),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Kamu belum menambahkan jadwal pelajaran.\nYuk, buat jadwal pertamamu sekarang!",
                        fontSize = 14.sp,
                        color = Color(0xFF646A63),
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Tampilan Terisi (Gambar 3)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    currentDaySchedules.forEach { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SchCardBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Kolom Kiri: Badge Jam + Waktu Mulai & Selesai
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(68.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(38.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = SchClockBadgeBg
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = "Clock",
                                                tint = SchClockTextGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = item.startTime,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SchClockTextGreen
                                    )
                                    Text(
                                        text = "-",
                                        fontSize = 11.sp,
                                        color = SchClockTextGreen
                                    )
                                    Text(
                                        text = item.endTime,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SchClockTextGreen
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Garis Pemisah Vertikal
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(72.dp)
                                        .background(Color(0xFFECE6DA))
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                // Kolom Kanan: Detail Mapel, Ruang, dan Guru
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.subject,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E2621),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { onEditSchedule(item) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit Jadwal",
                                                    tint = SchActiveDayGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { scheduleToDelete = item },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Hapus Jadwal",
                                                    tint = Color(0xFFD9534F),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = "Ruangan",
                                            tint = SchActiveDayGreen,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = item.room.ifEmpty { "Ruang Kelas" },
                                            fontSize = 13.sp,
                                            color = Color(0xFF444444)
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Guru",
                                            tint = SchActiveDayGreen,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = item.teacher.ifEmpty { "Guru Pengajar" },
                                            fontSize = 13.sp,
                                            color = Color(0xFF444444)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Floating Action Button Bulat Sage Green (+)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 92.dp)
                .size(56.dp)
                .clip(CircleShape)
                .clickable { onNavigateToAddSchedule() },
            color = SchFabGreen,
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Jadwal",
                    tint = Color(0xFF243B33),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // 5. Bottom Navigation Bar (Tab Jadwal Aktif Sesuai media_1789792065543.jpg)
        AppBottomNavigationBar(
            selectedTab = BottomNavTab.JADWAL,
            onNavigateToDashboard = onNavigateToDashboard,
            onNavigateToTasks = onNavigateToTasks,
            onNavigateToSchedule = { },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (scheduleToDelete != null) {
            AlertDialog(
                onDismissRequest = { scheduleToDelete = null },
                title = {
                    Text(
                        text = "Hapus Jadwal",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                },
                text = {
                    Text("Apakah kamu yakin ingin menghapus jadwal ${scheduleToDelete?.subject} (${scheduleToDelete?.startTime} - ${scheduleToDelete?.endTime}) pada hari ${scheduleToDelete?.day}?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteSchedule(scheduleToDelete!!.id)
                            scheduleToDelete = null
                        }
                    ) {
                        Text("Hapus", color = Color(0xFFD9534F), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { scheduleToDelete = null }) {
                        Text("Batal", color = Color(0xFF555555))
                    }
                }
            )
        }
    }
}
