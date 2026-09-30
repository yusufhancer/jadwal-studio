package com.jadwalstudio.app.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.jadwalstudio.app.ui.components.AppBottomNavigationBar
import com.jadwalstudio.app.ui.components.BottomNavTab
import com.jadwalstudio.app.utils.TaskDateUtils
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

val IsiBgCream = Color(0xFFF5F3ED)
val IsiGreen = Color(0xFF4F7366)
val IsiTitleDark = Color(0xFF2E4841)

data class SubjectTaskItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val isUrgent: Boolean,
    val isGroup: Boolean,
    val due: String,
    val completed: Boolean = false,
    val subject: String = "Bahasa Indonesia",
    val attachedImageUri: String? = null,
    val attachedImageName: String? = null,
    val status: String = "To Do",
    val groupId: String? = null,
    val groupName: String = "",
    val description: String = "",
    val notes: String = ""
)

@Composable
fun TaskDetailScreen(
    subjectName: String = "Bahasa Indonesia",
    tasks: List<SubjectTaskItem> = emptyList(),
    onNavigateBack: () -> Unit = {},
    onNavigateToAddTask: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onToggleTaskComplete: (String) -> Unit = {},
    onDeleteTask: (String) -> Unit = {},
    onOpenTask: (String) -> Unit = {}
) {
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    var selectedTaskForImage by remember { mutableStateOf<SubjectTaskItem?>(null) }
    var taskToDelete by remember { mutableStateOf<SubjectTaskItem?>(null) }
    val filters = listOf("Semua", "Mendesak", "Selesai")

    // Filter daftar tugas sesuai mata pelajaran yang aktif, otomatis terurut sisa waktu terdekat
    val subjectTasks = remember(tasks, subjectName) {
        val matching = tasks.filter { it.subject.equals(subjectName, ignoreCase = true) }
        val baseList = matching
        TaskDateUtils.sortTasksByPriorityAndDueDate(baseList)
    }

    val filteredList = when (selectedFilterIndex) {
        1 -> subjectTasks.filter { !it.completed && (it.isUrgent || TaskDateUtils.getDaysRemaining(it.due, it.isUrgent) <= 2) }
        2 -> subjectTasks.filter { it.completed }
        else -> subjectTasks
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IsiBgCream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(bottom = 90.dp)
        ) {
            // Header: Back Button + Subject Name
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = IsiTitleDark,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = subjectName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = IsiTitleDark,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Pills (Semua, Mendesak, Selesai)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEachIndexed { index, label ->
                    val isActive = selectedFilterIndex == index
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .clickable { selectedFilterIndex = index },
                        color = if (isActive) Color(0xFF2D463E) else Color(0xFFEDE8DF),
                        shape = CircleShape
                    ) {
                        Text(
                            text = label,
                            color = if (isActive) Color.White else Color(0xFF444444),
                            fontSize = 12.sp,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Task List
            if (filteredList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE8EFEA),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Tugas Kosong",
                                tint = Color(0xFF4F7366),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Belum Ada Tugas",
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = IsiTitleDark,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Belum ada tugas untuk mata pelajaran ini.\nYuk, tambahkan tugas barumu sekarang!",
                        fontSize = 13.sp,
                        color = Color(0xFF7A8680),
                        lineHeight = 18.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToAddTask,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F7366)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah Tugas",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tambah Tugas",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    filteredList.forEach { task ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                onOpenTask(task.id)
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEBE6DC))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                // Badges
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    if (task.isUrgent) {
                                        Surface(
                                            color = Color(0xFFFBECEC),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "PENTING",
                                                color = Color(0xFFC4654D),
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Surface(
                                        color = if (task.isGroup) Color(0xFFF0F2F0) else Color(0xFFE8EEF5),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (task.isGroup) "KLOMPOK" else "INDIVIDU",
                                            color = if (task.isGroup) Color(0xFF334A3E) else Color(0xFF3D5B7B),
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = task.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    color = IsiTitleDark
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Deadline",
                                        tint = if (task.isUrgent) Color(0xFFD9534F) else Color(0xFF777777),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val dueLabel = TaskDateUtils.getDueStatusLabel(task.due, task.isUrgent, task.completed)
                                    val displayText = if (task.due.equals(dueLabel, ignoreCase = true) || dueLabel == "Selesai") {
                                        task.due
                                    } else {
                                        "${task.due} • $dueLabel"
                                    }
                                    Text(
                                        text = displayText,
                                        fontSize = 12.sp,
                                        color = if (!task.completed && TaskDateUtils.getDaysRemaining(task.due) < 0) Color(0xFFD9534F) else Color(0xFF666666),
                                        fontWeight = if (!task.completed && TaskDateUtils.getDaysRemaining(task.due) < 0) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }

                                // Tombol / Chip khusus untuk melihat lampiran gambar
                                if (task.attachedImageUri != null || task.attachedImageName != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFE8F2EC),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC4DBCF)),
                                        modifier = Modifier.clickable { onOpenTask(task.id) }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Image,
                                                contentDescription = "Lampiran",
                                                tint = Color(0xFF386150),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = "Lihat Lampiran",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF386150)
                                            )
                                        }
                                    }
                                }
                            }

                            // Aksi Kanan: Tombol Lihat Gambar & Tombol Centang
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (task.attachedImageUri != null || task.attachedImageName != null) {
                                    IconButton(
                                        onClick = { onOpenTask(task.id) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = "Buka Lampiran",
                                            tint = Color(0xFF4F7366),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }

                                // Checkbox Button
                                Surface(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .clickable { onToggleTaskComplete(task.id) },
                                    color = if (task.completed) IsiGreen else Color.Transparent,
                                    shape = CircleShape,
                                    border = androidx.compose.foundation.BorderStroke(
                                        2.dp,
                                        if (task.completed) IsiGreen else Color(0xFFBDC5BD)
                                    )
                                ) {
                                    if (task.completed) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selesai",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Tombol Hapus Tugas
                                IconButton(
                                    onClick = { taskToDelete = task },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus Tugas",
                                        tint = Color(0xFFD9534F),
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        // Floating Action Button Bulat Sage Green (+) di Kanan Bawah
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 96.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable { onNavigateToAddTask() },
                color = Color(0xFF78988A),
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Tugas",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }

        // Bottom Navigation Bar Sticky (Tab Tugas Aktif Sesuai media_1789792065543.jpg)
        AppBottomNavigationBar(
            selectedTab = BottomNavTab.TUGAS,
            onNavigateToDashboard = onNavigateToDashboard,
            onNavigateToTasks = onNavigateBack,
            onNavigateToSchedule = onNavigateToSchedule,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Modal Preview Lampiran Gambar Tugas
        if (selectedTaskForImage != null) {
            TaskImagePreviewDialog(
                task = selectedTaskForImage!!,
                onDismiss = { selectedTaskForImage = null },
                onToggleComplete = {
                    onToggleTaskComplete(it)
                    selectedTaskForImage = selectedTaskForImage?.copy(completed = !selectedTaskForImage!!.completed)
                }
            )
        }

        if (taskToDelete != null) {
            AlertDialog(
                onDismissRequest = { taskToDelete = null },
                title = {
                    Text(
                        text = "Hapus Tugas",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                },
                text = {
                    Text("Apakah kamu yakin ingin menghapus tugas '${taskToDelete?.title}'?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteTask(taskToDelete!!.id)
                            taskToDelete = null
                        }
                    ) {
                        Text("Hapus", color = Color(0xFFD9534F), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { taskToDelete = null }) {
                        Text("Batal", color = Color(0xFF555555))
                    }
                }
            )
        }
    }
}

// ==========================================
// DIALOG PREVIEW GAMBAR LAMPIRAN TUGAS
// ==========================================
@Composable
fun TaskImagePreviewDialog(
    task: SubjectTaskItem,
    onDismiss: () -> Unit,
    onToggleComplete: (String) -> Unit
) {
    val context = LocalContext.current
    val bitmap = remember(task.attachedImageUri) {
        if (!task.attachedImageUri.isNullOrBlank() && task.attachedImageUri != "sample_mtk") {
            try {
                val uri = Uri.parse(task.attachedImageUri)
                val stream = context.contentResolver.openInputStream(uri)
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 620.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFFAF8F5),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE4EDE7)
                        ) {
                            Text(
                                text = task.subject,
                                color = Color(0xFF2E5343),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = IsiTitleDark
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color(0xFF666666)
                        )
                    }
                }

                // Info Lampiran
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = null,
                        tint = Color(0xFF4F7366),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = task.attachedImageName ?: "lampiran_soal_tugas.jpg",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF555555)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Area Gambar Lampiran
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(min = 220.dp, max = 340.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFDCD6CA), RoundedCornerShape(16.dp))
                        .background(Color.White)
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Foto Soal Tugas",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        // Render Gambar Visual Lembar Soal Matematika
                        MathTaskExerciseVisual()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer Tombol
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onToggleComplete(task.id)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (task.completed) Color(0xFF888888) else Color(0xFF4F7366)
                        )
                    ) {
                        Text(
                            text = if (task.completed) "Batal Selesai" else "Tandai Selesai",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE5E0D3)
                        )
                    ) {
                        Text(
                            text = "Tutup",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    }
                }
            }
        }
    }
}

// Visual Lembar Tugas Soal Matematika (Grid Kertas Berpetak & Rumus)
@Composable
fun MathTaskExerciseVisual() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFDFC))
    ) {
        // Grid pattern
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 20.dp.toPx()
            for (x in 0..(size.width / step).toInt()) {
                drawLine(
                    color = Color(0xFFE6EFEA),
                    start = Offset(x * step, 0f),
                    end = Offset(x * step, size.height),
                    strokeWidth = 1f
                )
            }
            for (y in 0..(size.height / step).toInt()) {
                drawLine(
                    color = Color(0xFFE6EFEA),
                    start = Offset(0f, y * step),
                    end = Offset(size.width, y * step),
                    strokeWidth = 1f
                )
            }

            // Sketsa Kurva Parabola Grafik Fungsi Kuadrat
            val graphOrigin = Offset(size.width - 60.dp.toPx(), 90.dp.toPx())
            // Sumbu X & Y
            drawLine(
                color = Color(0xFF8FA89B),
                start = Offset(graphOrigin.x - 45.dp.toPx(), graphOrigin.y),
                end = Offset(graphOrigin.x + 45.dp.toPx(), graphOrigin.y),
                strokeWidth = 1.5f
            )
            drawLine(
                color = Color(0xFF8FA89B),
                start = Offset(graphOrigin.x, graphOrigin.y - 45.dp.toPx()),
                end = Offset(graphOrigin.x, graphOrigin.y + 45.dp.toPx()),
                strokeWidth = 1.5f
            )

            // Kurva Parabola
            val path = Path().apply {
                val startX = graphOrigin.x - 35.dp.toPx()
                val peakY = graphOrigin.y - 30.dp.toPx()
                moveTo(startX, graphOrigin.y + 35.dp.toPx())
                quadraticBezierTo(graphOrigin.x, peakY - 10.dp.toPx(), graphOrigin.x + 35.dp.toPx(), graphOrigin.y + 35.dp.toPx())
            }
            drawPath(path = path, color = Color(0xFF2C6B53), style = Stroke(width = 3f))
        }

        // Teks Soal Lembar Latihan Matematika
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF2E4841)
            ) {
                Text(
                    text = " LEMBAR SOAL TUGAS MTK ",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Bab 2: Persamaan & Fungsi Kuadrat",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E2D27)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "1. Tentukan akar-akar persamaan dari:\n    x² - 5x + 6 = 0",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2E3A34),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "2. Diketahui kurva f(x) = x² - 4x + 3:\n    a. Tentukan titik potong sumbu X & Y\n    b. Tentukan koordinat titik puncak (Xp, Yp)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2E3A34),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "3. Hitung diskriminan (D = b² - 4ac) dari:\n    2x² + 3x - 5 = 0",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2E3A34),
                lineHeight = 16.sp
            )
        }
    }
}
