package com.jadwalstudio.app.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import com.jadwalstudio.app.utils.dashboardProjects
import com.jadwalstudio.app.utils.nextSchedule
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jadwalstudio.app.R

// Warna Akurat 100% dari Desain Figma
val DashWhite = Color(0xFFFFFFFF)
val DashBgCream = Color(0xFFF5F3ED)
val DashHeaderGreen = Color(0xFF4F7366)
val DashTextDark = Color(0xFF2E4841)
val DashCard1Bg = Color(0xFFDCE0D5)
val DashCard2Bg = Color(0xFFF1ECE5)
val DashCard2Border = Color(0xFFE5E0D3)
val DashCard3Bg = Color(0xFFDCE0D5)
val DashBottomBarBg = Color(0xFFF0ECE5)
val DashBottomBarBorder = Color(0xFFDFD9CE)
val DashClockCircle = Color(0xFF4C4637)
val DashLightningRed = Color(0xFFC4654D)
val DashUrgentRed = Color(0xFFD9534F)

@Composable
fun DashboardScreen(
    tasks: List<SubjectTaskItem> = emptyList(),
    groups: List<Pair<String, String>> = emptyList(),
    schedules: List<ScheduleItem> = emptyList(),
    onNavigateToTasks: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onAddNewTask: () -> Unit = {},
    onNavigateToGroupDetail: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val groupTasks = tasks.filter { it.isGroup }
    val urgentTasks = TaskDateUtils.sortTasksByPriorityAndDueDate(
        tasks.filter { !it.completed && it.due.isNotBlank() }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DashBgCream)
    ) {
        // Konten Scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // 1. Header Lengkung
            AppHeader(onLogout = onLogout)

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Daftar Kartu Konten
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // KARTU 1: Slider Projek Aktif (Horizontal Swipe per Projek Kelompok)
                CurrentProjectSlider(
                    groupTasks = groupTasks,
                    groups = groups,
                    onClick = onNavigateToGroupDetail
                )

                Spacer(modifier = Modifier.height(12.dp))

                // KARTU 2: Jadwal Berikutnya (Data Real dari Jadwal Pelajaran)
                NextScheduleCard(
                    schedules = schedules,
                    onClick = onNavigateToSchedule
                )

                Spacer(modifier = Modifier.height(12.dp))

                // KARTU 3: Tambahkan tugas baru
                AddTaskBanner(onClick = onAddNewTask)

                Spacer(modifier = Modifier.height(16.dp))

                // Section Header: Tugas Mendesak
                UrgentTasksHeader(onViewAll = onNavigateToTasks)

                Spacer(modifier = Modifier.height(12.dp))

                if (urgentTasks.isEmpty()) {
                    // Empty state Tugas Mendesak sesuai PRD Bagian 22
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DashWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE5E0D3), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Semua Selesai",
                                tint = DashHeaderGreen,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Belum Ada Deadline",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = DashTextDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tugas selesai atau belum ada tugas dengan deadline.",
                                fontSize = 12.sp,
                                color = Color(0xFF7A8680),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    urgentTasks.take(3).forEachIndexed { index, task ->
                        if (index > 0) Spacer(modifier = Modifier.height(12.dp))
                        UrgentTaskItem(
                            tag1 = if (task.isUrgent) "PENTING" else "TUGAS",
                            tag2 = if (task.isGroup) "KELOMPOK" else "INDIVIDU",
                            title = task.title,
                            dueText = TaskDateUtils.getDueStatusLabel(task.due, task.isUrgent, task.completed),
                            isOverdueOrUrgent = !task.completed && TaskDateUtils.getDaysRemaining(task.due) < 0,
                            showLeftStrip = !task.completed && TaskDateUtils.getDaysRemaining(task.due) < 0
                        )
                    }
                }
            }
        }

        // 3. Bottom Navigation Bar Sticky di Bawah (Konsisten Persis Gambar media_1789792065543.jpg)
        AppBottomNavigationBar(
            selectedTab = BottomNavTab.DASHBOARD,
            onNavigateToDashboard = { },
            onNavigateToTasks = onNavigateToTasks,
            onNavigateToSchedule = onNavigateToSchedule,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}


// ---------------- 2. CURRENT PROJECT SLIDER ----------------
data class ProjectCardData(
    val name: String,
    val tasks: List<SubjectTaskItem>,
    val id: String = ""
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun CurrentProjectSlider(
    groupTasks: List<SubjectTaskItem> = emptyList(),
    groups: List<Pair<String, String>> = emptyList(),
    onClick: (String) -> Unit = {}
) {
    val groupedProjects = remember(groups, groupTasks) {
        dashboardProjects(groups, groupTasks).ifEmpty {
            listOf(ProjectCardData(name = "Belum Ada Project", tasks = emptyList()))
        }
    }

    val pagerState = rememberPagerState(pageCount = { groupedProjects.size })

    Column(modifier = Modifier.fillMaxWidth()) {
        if (groupedProjects.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Projek Aktif (${pagerState.currentPage + 1}/${groupedProjects.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DashTextDark,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "Geser untuk lihat lainnya →",
                    fontSize = 11.sp,
                    color = Color(0xFF7A8680)
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val project = groupedProjects[page]
            SingleProjectCard(
                project = project,
                onClick = { onClick(project.id) }
            )
        }

        if (groupedProjects.size > 1) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(groupedProjects.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(6.dp)
                            .width(if (isSelected) 18.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) DashHeaderGreen else Color(0xFFCCD6C8))
                    )
                }
            }
        }
    }
}

@Composable
private fun SingleProjectCard(
    project: ProjectCardData,
    onClick: () -> Unit = {}
) {
    val total = project.tasks.size
    val done = project.tasks.count { it.completed }
    val percent = if (total > 0) (done * 100) / total else 0
    val projectName = project.name

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DashCard1Bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut Chart Persentase
                Box(
                    modifier = Modifier.size(86.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(82.dp)) {
                        val strokePx = 9.dp.toPx()
                        val diameter = size.minDimension - strokePx
                        val topLeft = Offset(strokePx / 2, strokePx / 2)
                        val arcSize = Size(diameter, diameter)

                        // Background ring tipis kehijauan
                        drawArc(
                            color = Color(0xFFE2EBE0),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx)
                        )

                        // Lengkung persentase hijau
                        if (percent > 0) {
                            val sweepAngle = (percent / 100f) * 360f
                            drawArc(
                                color = DashHeaderGreen,
                                startAngle = 270f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokePx, cap = StrokeCap.Round)
                            )
                        }
                    }

                    Text(
                        text = "$percent%",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = DashTextDark,
                        fontFamily = FontFamily.Serif
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Info Project
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current Project",
                            fontSize = 11.5.sp,
                            color = DashHeaderGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Lihat",
                            tint = Color(0xFF7A8680),
                            modifier = Modifier.size(11.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = projectName,
                        fontSize = 19.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = DashTextDark
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (total > 0) "$done dari $total tugas selesai" else "0 dari 0 tugas selesai",
                        fontSize = 12.sp,
                        color = Color(0xFF555555)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Bar
                    val progressVal = if (total > 0) done.toFloat() / total.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = { progressVal },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = DashHeaderGreen,
                        trackColor = Color(0xFFCCD6C8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2 Kolom Info Tugas di Bawah
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(DashHeaderGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selesai",
                            tint = DashWhite,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("$done", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DashTextDark)
                        Text("Tugas Selesai", fontSize = 10.5.sp, color = Color(0xFF555555))
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(DashHeaderGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "Total",
                            tint = DashWhite,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("$total", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DashTextDark)
                        Text("Total Tugas", fontSize = 10.5.sp, color = Color(0xFF555555))
                    }
                }
            }
        }
    }
}

// ---------------- 3. NEXT SCHEDULE CARD ----------------
@Composable
private fun NextScheduleCard(
    schedules: List<ScheduleItem> = emptyList(),
    onClick: () -> Unit = {}
) {
    val clock by produceState(initialValue = System.currentTimeMillis()) {
        while (true) { value = System.currentTimeMillis(); delay(1000) }
    }
    val nextItem = remember(schedules, clock / 60000) {
        nextSchedule(schedules, java.util.Calendar.getInstance().apply {
            timeInMillis = clock
            set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
        })
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DashCard2Bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, DashCard2Border, RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Strip Vertikal Hijau
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(58.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DashHeaderGreen)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (nextItem != null) nextItem.day.uppercase() else "JADWAL PELAJARAN",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF777777),
                    letterSpacing = 0.6.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (nextItem != null) "Berikutnya : ${nextItem.subject}" else "Belum Ada Jadwal Pelajaran",
                    fontSize = 17.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = DashTextDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Waktu",
                        tint = Color(0xFF777777),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (nextItem != null) "Jam ${nextItem.startTime} - ${nextItem.endTime} (${nextItem.room.ifBlank { "Kelas" }})" else "Yuk, atur jadwal pelajaranmu sekarang!",
                        fontSize = 12.5.sp,
                        color = Color(0xFF555555)
                    )
                }
            }

            // Icon Jam Bulat Coklat Tua
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(DashClockCircle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Jam",
                    tint = DashWhite,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ---------------- 4. TAMBAHKAN TUGAS BARU ----------------
@Composable
private fun AddTaskBanner(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = DashCard3Bg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tambahkan tugas baru",
                fontSize = 17.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = DashTextDark
            )

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(DashWhite),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah",
                    tint = DashTextDark,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ---------------- 5. HEADER TUGAS MENDESAK ----------------
@Composable
private fun UrgentTasksHeader(onViewAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "⚡",
                fontSize = 18.sp,
                color = DashLightningRed
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Deadline Terdekat",
                fontSize = 20.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = DashTextDark
            )
        }

        Row(
            modifier = Modifier.clickable { onViewAll() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Lihat semua",
                fontSize = 12.5.sp,
                color = Color(0xFF444444),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(3.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Lihat",
                tint = Color(0xFF444444),
                modifier = Modifier.size(10.dp)
            )
        }
    }
}

// ---------------- 6. ITEM TUGAS MENDESAK ----------------
@Composable
private fun UrgentTaskItem(
    tag1: String,
    tag2: String,
    title: String,
    dueText: String,
    isOverdueOrUrgent: Boolean,
    showLeftStrip: Boolean
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DashWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE5E0D3), RoundedCornerShape(14.dp))
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            if (showLeftStrip) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(96.dp)
                        .background(DashUrgentRed)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TaskTag(
                        text = tag1,
                        bgColor = Color(0xFFE9E1E3),
                        textColor = Color(0xFF6F3B40)
                    )
                    TaskTag(
                        text = tag2,
                        bgColor = Color(0xFFF0F2F0),
                        textColor = Color(0xFF334A3E)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = DashTextDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isOverdueOrUrgent) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Tenggat",
                            tint = DashUrgentRed,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = dueText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = DashUrgentRed
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Jadwal",
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = dueText,
                            fontSize = 12.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }
            }
        }
    }
}

// ---------------- 7. CHIP TAG ----------------
@Composable
private fun TaskTag(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}
