package com.jadwalstudio.app.ui.screens

import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val AddSchBgCream = Color(0xFFF5F3ED)
val AddSchCardBg = Color(0xFFF8F6F0)
val AddSchCardBorder = Color(0xFFE5E0D5)
val AddSchInputBorder = Color(0xFFDFD9CE)
val AddSchTitleDark = Color(0xFF2E4841)
val AddSchLabelDark = Color(0xFF555555)
val AddSchAsterisk = Color(0xFFD9534F)
val AddSchButtonGreen = Color(0xFF4D6D5E)
val AddSchButtonGreenHover = Color(0xFF3D5A4E)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddScheduleScreen(
    onNavigateBack: () -> Unit = {},
    availableSubjects: List<String> = emptyList(),
    scheduleToEdit: ScheduleItem? = null,
    existingSchedules: List<ScheduleItem> = emptyList(),
    onSaveSchedule: (ScheduleItem) -> Unit = {}
) {
    var subject by rememberSaveable { mutableStateOf(scheduleToEdit?.subject ?: (availableSubjects.firstOrNull() ?: "")) }
    var selectedDay by rememberSaveable { mutableStateOf(scheduleToEdit?.day ?: "Senin") }
    var startTime by rememberSaveable { mutableStateOf(scheduleToEdit?.startTime ?: "07:00") }
    var endTime by rememberSaveable { mutableStateOf(scheduleToEdit?.endTime ?: "08:30") }
    var room by rememberSaveable { mutableStateOf(scheduleToEdit?.room ?: "") }
    var teacher by rememberSaveable { mutableStateOf(scheduleToEdit?.teacher ?: "") }
    var note by rememberSaveable { mutableStateOf(scheduleToEdit?.note ?: "") }
    var errorMessage by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current
    fun pickTime(value: String, onSelected: (String) -> Unit) {
        val minutes = parseTimeToMinutes(value).coerceAtLeast(0)
        android.app.TimePickerDialog(context, { _, hour, minute ->
            onSelected(String.format(java.util.Locale.ROOT, "%02d:%02d", hour, minute))
        }, minutes / 60, minutes % 60, true).show()
    }

    var expandedDropdown by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AddSchBgCream)
            .imePadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(bottom = 32.dp)
        ) {
            // Header: Tombol Back + Judul Tengah "Tambah Jadwal"
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
                        tint = AddSchTitleDark,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = if (scheduleToEdit != null) "Ubah Jadwal" else "Tambah Jadwal",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = AddSchTitleDark,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (errorMessage.isNotEmpty()) {
                Surface(
                    color = Color(0xFFFFECEB),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF5C2C0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFC93B37),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Form Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AddSchCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, AddSchCardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Field 1: MATA PELAJARAN *
                    Column {
                        Row {
                            Text(
                                text = "MATA PELAJARAN ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AddSchLabelDark,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "*",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AddSchAsterisk
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        ExposedDropdownMenuBox(
                            expanded = expandedDropdown,
                            onExpandedChange = { expandedDropdown = !expandedDropdown }
                        ) {
                            OutlinedTextField(
                                value = subject,
                                onValueChange = { subject = it },
                                placeholder = {
                                    Text(
                                        text = "Pilih mata pelajaran",
                                        color = Color(0xFF8A8882),
                                        fontSize = 13.5.sp
                                    )
                                },
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = AddSchInputBorder,
                                    focusedBorderColor = AddSchButtonGreen,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                ),
                                singleLine = true
                            )

                            ExposedDropdownMenu(
                                expanded = expandedDropdown,
                                onDismissRequest = { expandedDropdown = false }
                            ) {
                                if (availableSubjects.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("Belum ada mata pelajaran. Buat di menu Tugas terlebih dahulu.", fontSize = 12.sp, color = Color.Gray) },
                                        onClick = { expandedDropdown = false },
                                        enabled = false
                                    )
                                } else {
                                    availableSubjects.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                subject = option
                                                expandedDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Field 2: HARI *
                    Column {
                        Row {
                            Text(
                                text = "HARI ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AddSchLabelDark,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "*",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AddSchAsterisk
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Wrap day chips naturally on compact screens and larger font sizes.
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().selectableGroup()
                        ) {
                            listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat").forEach { day ->
                                val isSelected = selectedDay == day
                                Surface(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .selectable(selected = isSelected, role = Role.RadioButton, onClick = { selectedDay = day }),
                                    color = if (isSelected) AddSchButtonGreen else AddSchCardBg,
                                    shape = CircleShape,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) AddSchButtonGreen else AddSchInputBorder
                                    )
                                ) {
                                    Text(
                                        text = day,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFF333333),
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                                    )
                                }
                            }
                        }

                    }

                    // Field 3: JAM MULAI * & JAM SELESAI *
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Jam Mulai
                        Column(modifier = Modifier.weight(1f)) {
                            Row {
                                Text(
                                    text = "JAM MULAI ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AddSchLabelDark,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "*",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AddSchAsterisk
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = startTime,
                                onValueChange = { startTime = it },
                                trailingIcon = { IconButton(onClick = { pickTime(startTime) { startTime = it } }) {
                                    Icon(androidx.compose.material.icons.Icons.Default.Schedule, "Pilih jam mulai")
                                } },
                                placeholder = { Text("--:-- --") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = AddSchInputBorder,
                                    focusedBorderColor = AddSchButtonGreen
                                ),
                                singleLine = true
                            )
                        }

                        // Jam Selesai
                        Column(modifier = Modifier.weight(1f)) {
                            Row {
                                Text(
                                    text = "JAM SELESAI ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AddSchLabelDark,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "*",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AddSchAsterisk
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = endTime,
                                onValueChange = { endTime = it },
                                trailingIcon = { IconButton(onClick = { pickTime(endTime) { endTime = it } }) {
                                    Icon(androidx.compose.material.icons.Icons.Default.Schedule, "Pilih jam selesai")
                                } },
                                placeholder = { Text("--:-- --") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = AddSchInputBorder,
                                    focusedBorderColor = AddSchButtonGreen
                                ),
                                singleLine = true
                            )
                        }
                    }

                    // Field 4: RUANGAN
                    Column {
                        Text(
                            text = "RUANGAN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AddSchLabelDark,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = room,
                            onValueChange = { room = it },
                            placeholder = {
                                Text(
                                    text = "Contoh: Lab Komputer 1",
                                    color = Color(0xFF9E9B93),
                                    fontSize = 13.5.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.MeetingRoom,
                                    contentDescription = "Ruangan",
                                    tint = Color(0xFF9E9B93)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = AddSchInputBorder,
                                focusedBorderColor = AddSchButtonGreen
                            ),
                            singleLine = true
                        )
                    }

                    // Field 5: NAMA GURU
                    Column {
                        Text(
                            text = "NAMA GURU",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AddSchLabelDark,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = teacher,
                            onValueChange = { teacher = it },
                            placeholder = {
                                Text(
                                    text = "Contoh: Bpk. Budi Santoso",
                                    color = Color(0xFF9E9B93),
                                    fontSize = 13.5.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Guru",
                                    tint = Color(0xFF9E9B93)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = AddSchInputBorder,
                                focusedBorderColor = AddSchButtonGreen
                            ),
                            singleLine = true
                        )
                    }

                    // Field 6: CATATAN TAMBAHAN
                    Column {
                        Text(
                            text = "CATATAN TAMBAHAN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AddSchLabelDark,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            placeholder = {
                                Text(
                                    text = "Bawa buku cetak bab 4...",
                                    color = Color(0xFF9E9B93),
                                    fontSize = 13.5.sp
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 112.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = AddSchInputBorder,
                                focusedBorderColor = AddSchButtonGreen
                            ),
                            maxLines = 4
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Button: SIMPAN JADWAL
                    Button(
                        onClick = {
                            if (subject.isBlank()) {
                                errorMessage = "Mata pelajaran wajib dipilih!"
                                return@Button
                            }
                            
                            val startMinutes = parseTimeToMinutes(startTime)
                            val endMinutes = parseTimeToMinutes(endTime)
                            
                            // BR-19: Jam Selesai harus lebih besar dari Jam Mulai
                            if (startMinutes < 0 || endMinutes < 0) {
                                errorMessage = "Gunakan jam valid HH:mm (00:00–23:59)."
                                return@Button
                            }
                            if (endMinutes <= startMinutes) {
                                errorMessage = "Jam Selesai ($endTime) harus lebih lambat dari Jam Mulai ($startTime)!"
                                return@Button
                            }

                            // BR-20: Validasi bentrok jadwal pada hari yang sama
                            val conflict = existingSchedules.firstOrNull { other ->
                                other.day.equals(selectedDay, ignoreCase = true) &&
                                other.id != scheduleToEdit?.id &&
                                (maxOf(startMinutes, parseTimeToMinutes(other.startTime)) < minOf(endMinutes, parseTimeToMinutes(other.endTime)))
                            }

                            if (conflict != null) {
                                errorMessage = "Bentrok dengan jadwal ${conflict.subject} (${conflict.startTime} - ${conflict.endTime}) pada hari $selectedDay!"
                                return@Button
                            }

                            errorMessage = ""
                            onSaveSchedule(
                                ScheduleItem(
                                    id = scheduleToEdit?.id ?: java.util.UUID.randomUUID().toString(),
                                    subject = subject,
                                    day = selectedDay,
                                    startTime = startTime.ifBlank { "07:00" },
                                    endTime = endTime.ifBlank { "08:30" },
                                    room = room.trim(),
                                    teacher = teacher.trim(),
                                    note = note
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = AddSchButtonGreen)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Simpan",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (scheduleToEdit != null) "PERBARUI JADWAL" else "SIMPAN JADWAL",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun parseTimeToMinutes(timeStr: String): Int {
    val clean = timeStr.trim()
    if (!Regex("^(?:[01]\\d|2[0-3]):[0-5]\\d$").matches(clean)) return -1
    val parts = clean.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return h * 60 + m
}
