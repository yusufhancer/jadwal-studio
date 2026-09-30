package com.jadwalstudio.app.ui.screens

import androidx.compose.runtime.saveable.rememberSaveable

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jadwalstudio.app.R

// ==========================================
// 1. SCREEN PILIH JENIS TUGAS (screen-tambah-tugas)
// ==========================================
@Composable
fun AddTaskChoiceScreen(
    onNavigateBack: () -> Unit = {},
    onChooseIndividual: () -> Unit = {},
    onChooseGroup: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3ED))
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
            // Header: Back Button + "Jenis Tugas"
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
                        tint = Color(0xFF2E4841),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Jenis Tugas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF2E4841),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "Tambahkan Tugas",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF2E4841)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Pilih jenis tugas yang ingin Anda buat untuk mengatur jadwal dengan lebih baik.",
                    fontSize = 12.5.sp,
                    color = Color(0xFF7A8680),
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card 1: Tugas Individu
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E0D3)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ilustrasi_tugas_individu),
                        contentDescription = "Tugas Individu",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tugas Individu",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF2E4841)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Buat tugas untuk diri sendiri. Atur tenggat waktu dan lacak progres belajar Anda secara mandiri.",
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onChooseIndividual,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F7366))
                    ) {
                        Text(
                            text = "Pilih Tugas Individu",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 2: Tugas Kelompok
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E0D3)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ilustrasi_tugas_kelompok),
                        contentDescription = "Tugas Kelompok",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tugas Kelompok",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF2E4841)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Buat tugas kelompok dan kolaborasi dengan teman sekelas. Bagikan tugas dan pantau progres bersama.",
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onChooseGroup,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F7366))
                    ) {
                        Text(
                            text = "Pilih Tugas Kelompok",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// Daftar default mata pelajaran di aplikasi (mengikuti data mata pelajaran di Halaman Tugas)
val defaultSubjectsList = listOf(
    "Bahasa Indonesia",
    "Matematika",
    "Projek PPLG"
)

// Komponen Dropdown Pilihan Mata Pelajaran (Hanya memilih dari data yang ada di tugas)
@Composable
fun SubjectSelectField(
    selectedSubject: String,
    onSubjectSelected: (String) -> Unit,
    availableSubjects: List<String> = defaultSubjectsList,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = "Mata Pelajaran",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color(0xFF2D312E)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { expanded = true },
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD6CEBF))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedSubject.ifBlank { "Pilih Mata Pelajaran" },
                    fontSize = 15.sp,
                    color = if (selectedSubject.isNotBlank()) Color(0xFF1D1B20) else Color(0xFF79747E),
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Pilih",
                    tint = Color(0xFF4F7366),
                    modifier = Modifier.size(24.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(Color.White)
            ) {
                availableSubjects.forEach { subject ->
                    val isChosen = subject.equals(selectedSubject, ignoreCase = true)
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = subject,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isChosen) Color(0xFF4F7366) else Color(0xFF2D312E),
                                    fontSize = 14.sp
                                )
                                if (isChosen) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Terpilih",
                                        tint = Color(0xFF4F7366),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onSubjectSelected(subject)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// Komponen Upload & Preview Lampiran File / Gambar (2 Pilihan: Gambar di Galeri atau File)
@Composable
fun TaskAttachmentPicker(
    attachedImageUri: String?,
    attachedImageName: String?,
    onImageSelected: (uri: String?, name: String?) -> Unit,
    modifier: Modifier = Modifier,
    onChecking: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var showOptionsDialog by remember { mutableStateOf(false) }

    val pickerScope = rememberCoroutineScope()
    var pickerError by remember { mutableStateOf<String?>(null) }
    var checkingFile by remember { mutableStateOf(false) }
    fun selectFile(uri: Uri?) {
        if (uri == null) return
        checkingFile = true
        onChecking(true)
        pickerScope.launch {
            try {
                val name = com.jadwalstudio.app.data.validateAttachmentInput(context, uri)
                onImageSelected(uri.toString(), name)
                pickerError = null
            } catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                pickerError = error.message ?: "Lampiran tidak dapat dibaca."
            } finally { checkingFile = false; onChecking(false) }
        }
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { selectFile(it) }
    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { selectFile(it) }

    val previewBitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(initialValue = null, attachedImageUri) {
        value = null
        value = withContext(Dispatchers.IO) {
            if (attachedImageUri.isNullOrBlank()) null else runCatching {
                val uri = Uri.parse(attachedImageUri)
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
                if (options.outWidth <= 0 || options.outHeight <= 0) null else {
                    options.inJustDecodeBounds = false
                    options.inSampleSize = 1
                    while (options.outWidth / options.inSampleSize > 1024 || options.outHeight / options.inSampleSize > 1024) options.inSampleSize *= 2
                    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options)?.asImageBitmap() }
                }
            }.getOrNull()
        }
    }

    val bitmap = previewBitmap
    Column(modifier = modifier) {
        if (checkingFile) Text("Memeriksa lampiran…", color = Color(0xFF4F7366))
        if (pickerError != null) Text(pickerError!!, color = Color(0xFFD9534F))

        Text(
            text = "Lampiran File / Gambar",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color(0xFF2D312E)
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (attachedImageUri.isNullOrBlank()) {
            // Upload Dropzone
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.5.dp, Color(0xFFD6CEBF), RoundedCornerShape(16.dp))
                    .clickable { showOptionsDialog = true },
                color = Color.White.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Upload",
                        tint = Color(0xFF4F7366),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Klik untuk upload file / gambar",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4F7366)
                    )
                    Text(
                        text = "Pilih Gambar di Galeri atau Dokumen File",
                        fontSize = 11.sp,
                        color = Color(0xFF888888)
                    )
                }
            }
        } else {
            // Card File Terlampir
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFC7DFD3), RoundedCornerShape(16.dp)),
                color = Color(0xFFF1F8F4),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Thumbnail
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE2EDE7)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Thumbnail",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFF4F7366),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = attachedImageName ?: "lampiran_tugas",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E4841),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "✓ Berhasil dilampirkan",
                            fontSize = 11.5.sp,
                            color = Color(0xFF3F6B43),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Tombol Hapus Lampiran
                    IconButton(
                        onClick = { onImageSelected(null, null) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hapus",
                            tint = Color(0xFFB04A4A),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    if (showOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showOptionsDialog = false },
            title = {
                Text(
                    text = "Pilih Lampiran",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF2E4841)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Pilih sumber lampiran tugas:",
                        fontSize = 13.sp,
                        color = Color(0xFF666666)
                    )

                    // Pilihan 1: Pilih Gambar di Galeri
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showOptionsDialog = false
                                imagePickerLauncher.launch(arrayOf("image/jpeg", "image/png"))
                            },
                        color = Color(0xFFF3EFE6),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Collections,
                                contentDescription = null,
                                tint = Color(0xFF4F7366),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Pilih Gambar di Galeri",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2D312E)
                                )
                                Text(
                                    text = "Format foto tugas (JPG, PNG, JPEG)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF777777)
                                )
                            }
                        }
                    }

                    // Pilihan 2: Pilih File / Dokumen
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showOptionsDialog = false
                                filePickerLauncher.launch(arrayOf("application/pdf", "image/jpeg", "image/png", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                            },
                        color = Color(0xFFE4EDE7),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = null,
                                tint = Color(0xFF2E5343),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Pilih File / Dokumen",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E5343)
                                )
                                Text(
                                    text = "Semua dokumen file (PDF, DOCX, dll.)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF557766)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showOptionsDialog = false }) {
                    Text("Batal", color = Color(0xFF666666))
                }
            }
        )
    }
}

// Komponen Input Deadline Cerdas dengan Pemilih Kalender dan Pilihan Cepat
@Composable
fun TaskDeadlinePickerField(
    deadline: String,
    onDeadlineChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val quickOptions = listOf("Hari Ini", "Besok", "Lusa", "3 Hari", "1 Minggu")

    Column(modifier = modifier) {
        Text(
            text = "Deadline Pengumpulan",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color(0xFF2D312E)
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = deadline,
            onValueChange = onDeadlineChange,
            placeholder = { Text("Pilih atau ketik tanggal") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            trailingIcon = {
                IconButton(onClick = {
                    val cal = java.util.Calendar.getInstance()
                    android.app.DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val selectedCal = java.util.Calendar.getInstance().apply {
                                set(year, month, dayOfMonth)
                            }
                            val sdf = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale("id", "ID"))
                            onDeadlineChange(sdf.format(selectedCal.time))
                        },
                        cal.get(java.util.Calendar.YEAR),
                        cal.get(java.util.Calendar.MONTH),
                        cal.get(java.util.Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Pilih Kalender",
                        tint = Color(0xFF4F7366),
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Choice Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickOptions.forEach { opt ->
                val isSelected = deadline.equals(opt, ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Color(0xFF4F7366) else Color(0xFFEFECE5),
                    modifier = Modifier.clickable { onDeadlineChange(com.jadwalstudio.app.utils.TaskDateUtils.normalizeDeadline(opt)) }
                ) {
                    Text(
                        text = opt,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF4C4637),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. SCREEN ISI TUGAS INDIVIDU (screen-tambah-tugas-individu)
// ==========================================
@Composable
fun AddIndividualTaskScreen(
    onNavigateBack: () -> Unit = {},
    availableSubjects: List<String> = defaultSubjectsList,
    onSaveSuccess: (title: String, subject: String, deadline: String, isUrgent: Boolean, imageUri: String?, imageName: String?, description: String, notes: String) -> Unit = { _, _, _, _, _, _, _, _ -> }
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var subject by rememberSaveable { mutableStateOf(availableSubjects.firstOrNull() ?: "") }
    var deadline by rememberSaveable { mutableStateOf("Besok") }
    var isUrgent by rememberSaveable { mutableStateOf(true) }
    var attachedImageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var attachedImageName by rememberSaveable { mutableStateOf<String?>(null) }
    var checkingAttachment by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3ED))
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
            // Header
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
                        tint = Color(0xFF2E4841),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Tambah Tugas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF2E4841),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Judul Tugas
                Column {
                    Text(text = "Judul Tugas", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2D312E))
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Masukkan Judul Tugas") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        ),
                        singleLine = true
                    )
                }

                // Mata Pelajaran (Memilih dari data mata pelajaran)
                SubjectSelectField(
                    selectedSubject = subject,
                    onSubjectSelected = { subject = it },
                    availableSubjects = availableSubjects
                )

                // Deadline Pengumpulan dengan Kalender & Quick Chips
                TaskDeadlinePickerField(
                    deadline = deadline,
                    onDeadlineChange = { deadline = it }
                )

                // Tingkat Prioritas (Prioritas vs Biasa)
                Column {
                    Text(text = "Tingkat Prioritas", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2D312E))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { isUrgent = true },
                            color = if (isUrgent) Color(0xFF2D463E) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isUrgent) Color(0xFF2D463E) else Color(0xFFE5E0D3))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Prioritas",
                                    color = if (isUrgent) Color.White else Color(0xFF333333),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { isUrgent = false },
                            color = if (!isUrgent) Color(0xFF2D463E) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (!isUrgent) Color(0xFF2D463E) else Color(0xFFE5E0D3))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Biasa",
                                    color = if (!isUrgent) Color.White else Color(0xFF333333),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = { Text("Deskripsi tugas") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                OutlinedTextField(value = notes, onValueChange = { notes = it },
                    label = { Text("Catatan pengerjaan") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                // Upload Lampiran File / Foto Soal
                TaskAttachmentPicker(
                    attachedImageUri = attachedImageUri,
                    attachedImageName = attachedImageName,
                    onChecking = { checkingAttachment = it },
                    onImageSelected = { uri, name ->
                        attachedImageUri = uri
                        attachedImageName = name
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Button: Simpan Tugas
                Button(
                    onClick = {
                        onSaveSuccess(title, subject, deadline, isUrgent, attachedImageUri, attachedImageName, description, notes)
                    },
                    enabled = !checkingAttachment,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F7366))
                ) {
                    Text(
                        text = "Simpan Tugas",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. SCREEN ISI TUGAS KELOMPOK (screen-tambah-tugas-kelompok)
// ==========================================
@Composable
fun AddGroupTaskScreen(
    onNavigateBack: () -> Unit = {},
    availableSubjects: List<String> = defaultSubjectsList,
    onNavigateToNext: (title: String, subject: String, deadline: String, isUrgent: Boolean, imageUri: String?, imageName: String?, description: String, notes: String) -> Unit = { _, _, _, _, _, _, _, _ -> }
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var subject by rememberSaveable { mutableStateOf(availableSubjects.firstOrNull() ?: "Projek PPLG") }
    var deadline by rememberSaveable { mutableStateOf("3 hari") }
    var isUrgent by rememberSaveable { mutableStateOf(true) }
    var attachedImageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var attachedImageName by rememberSaveable { mutableStateOf<String?>(null) }
    var checkingAttachment by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3ED))
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
            // Header
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
                        tint = Color(0xFF2E4841),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Tambah Tugas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF2E4841),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Judul Tugas
                Column {
                    Text(text = "Judul Tugas", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2D312E))
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Masukkan Judul Tugas Kelompok") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        ),
                        singleLine = true
                    )
                }

                // Mata Pelajaran (Memilih dari data mata pelajaran)
                SubjectSelectField(
                    selectedSubject = subject,
                    onSubjectSelected = { subject = it },
                    availableSubjects = availableSubjects
                )

                // Deadline Pengumpulan dengan Kalender & Quick Chips
                TaskDeadlinePickerField(
                    deadline = deadline,
                    onDeadlineChange = { deadline = it }
                )

                // Tingkat Prioritas
                Column {
                    Text(text = "Tingkat Prioritas", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2D312E))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { isUrgent = true },
                            color = if (isUrgent) Color(0xFF2D463E) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isUrgent) Color(0xFF2D463E) else Color(0xFFE5E0D3))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Prioritas",
                                    color = if (isUrgent) Color.White else Color(0xFF333333),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { isUrgent = false },
                            color = if (!isUrgent) Color(0xFF2D463E) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (!isUrgent) Color(0xFF2D463E) else Color(0xFFE5E0D3))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Biasa",
                                    color = if (!isUrgent) Color.White else Color(0xFF333333),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = { Text("Deskripsi tugas") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                OutlinedTextField(value = notes, onValueChange = { notes = it },
                    label = { Text("Catatan pengerjaan") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                // Upload Area
                TaskAttachmentPicker(
                    attachedImageUri = attachedImageUri,
                    attachedImageName = attachedImageName,
                    onChecking = { checkingAttachment = it },
                    onImageSelected = { uri, name ->
                        attachedImageUri = uri
                        attachedImageName = name
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Button: Selanjutnya (mengarah ke Buat Kelompok)
                Button(
                    onClick = {
                        onNavigateToNext(title, subject, deadline, isUrgent, attachedImageUri, attachedImageName, description, notes)
                    },
                    enabled = !checkingAttachment,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F7366))
                ) {
                    Text(
                        text = "Selanjutnya",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ==========================================
// 4. SCREEN BUAT KLOMPOK (screen-buat-kelompok)
// ==========================================
@Composable
fun CreateGroupScreen(
    onNavigateBack: () -> Unit = {},
    onCreateGroupSuccess: (String, String) -> Unit = { _, _ -> }
) {
    var groupName by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3ED))
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
            // Header
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
                        tint = Color(0xFF2E4841),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Buat Klompok",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF2E4841),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ilustrasi_buat_kelompok),
                    contentDescription = "Ilustrasi Buat Kelompok",
                    modifier = Modifier.size(240.dp, 200.dp)
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Nama Kelompok
                Column {
                    Text(text = "Nama Kelompok", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2D312E))
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        placeholder = { Text("Contoh: Tim Alpha PPLG") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        ),
                        singleLine = true
                    )
                }

                // Deskripsi Kelompok
                Column {
                    Text(text = "Deskripsi", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2D312E))
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Tuliskan deskripsi singkat tugas kelompok...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        ),
                        maxLines = 4
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Button: Buat Klompok
                Button(
                    onClick = {
                        if (groupName.isNotBlank()) {
                            onCreateGroupSuccess(groupName, description)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F7366))
                ) {
                    Text(
                        text = "Buat Klompok",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
