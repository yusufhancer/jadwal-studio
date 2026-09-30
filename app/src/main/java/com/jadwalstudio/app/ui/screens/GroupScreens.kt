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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.jadwalstudio.app.utils.TaskDateUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jadwalstudio.app.R

// Model Tugas Kelompok
data class GroupTaskItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val dueText: String,
    val status: String = "Progress"
)

val defaultGroupTasks = emptyList<GroupTaskItem>()

// ==========================================
// 1. SCREEN DETAIL KELOMPOK (screen-detail-kelompok)
// ==========================================
@Composable
fun GroupDetailScreen(
    groupName: String = "Tim UI/UX JadwalStudio",
    tasks: List<GroupTaskItem> = defaultGroupTasks,
    onNavigateBack: () -> Unit = {},
    onNavigateToAllGroupTasks: () -> Unit = {},
    onNavigateToMembers: () -> Unit = {},
    onNavigateToInviteLink: () -> Unit = {},
    onNavigateToLogs: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    ownerName: String = "",
    createdDate: String = "",
    memberCount: Int = 0,
    isOwner: Boolean = false,
    onAddTask: () -> Unit = {},
    onOpenTask: (String) -> Unit = {}
) {
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
            // Header Sub-Page: Tombol Back + Text "Detail Kelompok" di Sebelahnya Sesuai Permintaan
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
                Text(
                    text = "Detail Kelompok",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF2E4841)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Card 1: Profil Kelompok (Avatar + Nama + Tag + Owner + Dibuat) Sesuai preview.html
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E0D3))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = Color(0xFF466A5D)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = "Kelompok",
                                    tint = Color.White,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = groupName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = Color(0xFF1F2925)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = Color(0xFFE2EAD9),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (isOwner) "Owner" else "Member",
                                    color = Color(0xFF55694A),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Owner: $ownerName",
                                fontSize = 11.5.sp,
                                color = Color(0xFF7A8680)
                            )
                            Text(
                                text = "Dibuat: $createdDate",
                                fontSize = 11.5.sp,
                                color = Color(0xFF7A8680)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats: 3 Kolom (Members, Tugas, Progress) Sesuai preview.html
                    val totalGroupTasks = tasks.size
                    val doneGroupTasks = tasks.count { it.status == "Done" }
                    val progressPercent = if (totalGroupTasks > 0) (doneGroupTasks * 100) / totalGroupTasks else 0

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF8F6F0),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E0D3))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = memberCount.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2925))
                                Text(text = "Members", fontSize = 11.sp, color = Color(0xFF7A8680), fontWeight = FontWeight.Medium)
                            }
                            Box(modifier = Modifier.width(1.dp).height(28.dp).background(Color(0xFFE5E0D3)))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "$totalGroupTasks", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2925))
                                Text(text = "Tugas", fontSize = 11.sp, color = Color(0xFF7A8680), fontWeight = FontWeight.Medium)
                            }
                            Box(modifier = Modifier.width(1.dp).height(28.dp).background(Color(0xFFE5E0D3)))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "$progressPercent%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F7366))
                                Text(text = "Progress", fontSize = 11.sp, color = Color(0xFF7A8680), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Tugas Kelompok Header & Daftar Tugas Sesuai preview.html
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tugas Kelompok",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2925)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(onClick = onAddTask, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F7366))) { Text("Tambah Tugas Kelompok") }
                Spacer(modifier = Modifier.height(12.dp))
                // Card 3: Daftar Tugas Kelompok List Sesuai preview.html
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E0D3))
                ) {
                    if (tasks.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Belum Ada Tugas Kelompok",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2925),
                                fontFamily = FontFamily.Serif
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Belum ada tugas kelompok yang dibuat.",
                                fontSize = 12.sp,
                                color = Color(0xFF7A8680)
                            )
                        }
                    } else {
                        Column {
                            tasks.forEachIndexed { index, task ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenTask(task.id) }
                                        .padding(horizontal = 16.dp, vertical = 13.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = task.title,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2D312E)
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "Deadline: ${task.dueText}",
                                            fontSize = 11.sp,
                                            color = if (task.status != "Done" && com.jadwalstudio.app.utils.TaskDateUtils.getDaysRemaining(task.dueText) < 0) Color(0xFFD9534F) else Color(0xFF888888)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (task.status == "Done") Color(0xFFCEE7D0) else Color(0xFFF4E3BA)
                                    ) {
                                        Text(
                                            text = task.status,
                                            color = if (task.status == "Done") Color(0xFF3F6B43) else Color(0xFF8C6B1C),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                if (index < tasks.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0xFFE5E0D3))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 4: Menu Kelompok Sesuai preview.html
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Menu Kelompok",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2925)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3 Tombol: Anggota, Invite Link & Catatan Meeting (PRD Bagian 13)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tombol Anggota
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(82.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onNavigateToMembers() },
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E0D3))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = "Anggota",
                                tint = Color(0xFF1F2925),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Anggota",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F2925)
                            )
                        }
                    }

                    // Tombol Invite Link
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(82.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(enabled = isOwner) { onNavigateToInviteLink() },
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E0D3))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Invite Link",
                                tint = Color(0xFF1F2925),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Invite Link",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F2925)
                            )
                        }
                    }

                    // Tombol Catatan Meeting (PRD Bagian 13)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(82.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onNavigateToLogs() },
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E0D3))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.EventNote,
                                contentDescription = "Catatan Meeting",
                                tint = Color(0xFF4F7366),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Catatan Rapat",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4F7366)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Navigation Bar (Tab Tugas Aktif Sesuai media_1789792065543.jpg)
        AppBottomNavigationBar(
            selectedTab = BottomNavTab.TUGAS,
            onNavigateToDashboard = onNavigateToDashboard,
            onNavigateToTasks = onNavigateToTasks,
            onNavigateToSchedule = onNavigateToSchedule,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ==========================================
// 2. SCREEN INVIT LINK (screen-invite-link)
// ==========================================
@Composable
fun GroupInviteLinkScreen(
    onNavigateBack: () -> Unit = {}
) {
    var copied by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3ED))
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
                    text = "Invit Link",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF2E4841),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card: Invite Link Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E0D3))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Invite Link Kelompok",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2925)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Bagikan link di bawah untuk mengundang teman bergabung ke kelompok belajar Anda.",
                        fontSize = 12.5.sp,
                        color = Color(0xFF7A8680),
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Copy Box
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF7F5F0),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E0D3))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "jadwalstudio.app/join/PPLG-4819",
                                fontSize = 12.5.sp,
                                color = Color(0xFF333333),
                                fontWeight = FontWeight.Medium
                            )

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { copied = true },
                                color = if (copied) Color(0xFF3F6B43) else Color(0xFF4F7366)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Salin",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (copied) "Tersalin!" else "Salin",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 2: Note Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E0D3))
            ) {
                Text(
                    text = "Bagikan link ini hanya kepada anggota yang ingin kamu undang ke kelompok.",
                    fontSize = 12.5.sp,
                    color = Color(0xFF555555),
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(18.dp)
                )
            }
        }
    }
}

// ==========================================
// 3. SCREEN ANGGOTA KELOMPOK (screen-anggota-kelompok)
// ==========================================
@Composable
fun GroupMembersScreen(
    members: List<Pair<String, String>> = listOf(Pair("Ahmad (Kamu)", "Owner")),
    onRemoveMember: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateToInvite: () -> Unit = {}
) {
    var memberToDelete by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3ED))
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
                    text = "Anggota",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF2E4841),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row: Count + "+ Undang"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${members.size} Anggota",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2925)
                )

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onNavigateToInvite() },
                    color = Color(0xFF4E6E5D)
                ) {
                    Text(
                        text = "+ Undang",
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Members List Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E0D3))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    members.forEach { (name, role) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(38.dp),
                                    shape = CircleShape,
                                    color = Color(0xFFEDE8DF)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = name.first().toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF4F7366)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2D312E)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (role == "Owner") Color(0xFFE0ECE5) else Color(0xFFEDECE1),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = role,
                                        color = if (role == "Owner") Color(0xFF28563A) else Color(0xFF736F5D),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }

                                if (role != "Owner") {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { memberToDelete = name },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Hapus Anggota",
                                            tint = Color(0xFFD9534F),
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (memberToDelete != null) {
                        AlertDialog(
                            onDismissRequest = { memberToDelete = null },
                            title = {
                                Text(
                                    text = "Hapus Anggota",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                )
                            },
                            text = {
                                Text("Apakah kamu yakin ingin mengeluarkan ${memberToDelete} dari kelompok ini?")
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        onRemoveMember(memberToDelete!!)
                                        memberToDelete = null
                                    }
                                ) {
                                    Text("Hapus", color = Color(0xFFD9534F), fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { memberToDelete = null }) {
                                    Text("Batal", color = Color(0xFF555555))
                                }
                            }
                        )
                    }

                    if (members.size <= 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE5E0D3)))
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Belum Ada Anggota Lain",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D312E)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Kamu adalah owner kelompok ini. Bagikan link kelompok untuk mengundang teman!",
                                fontSize = 11.5.sp,
                                color = Color(0xFF7A8680),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onNavigateToInvite,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4E6E5D)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = "Link",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Bagikan Link Kelompok",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Hak Akses Card
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "Hak Akses",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2925)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E0D3))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column {
                            Text(text = "Owner", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color(0xFF4F7366))
                            Text(text = "Kelola anggota, undang anggota, dan kelola tugas kelompok.", fontSize = 12.sp, color = Color(0xFF666666))
                        }
                        Column {
                            Text(text = "Member", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color(0xFF2D312E))
                            Text(text = "Melihat anggota dan mengerjakan tugas yang diberikan.", fontSize = 12.sp, color = Color(0xFF666666))
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. SCREEN LIHAT SEMUA TUGAS KELOMPOK (media_1789970006397.png)
// ==========================================
data class GroupTaskDetailItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val subject: String,
    val date: String,
    val status: String, // "On Progress", "Selesai", "Belum Dimulai"
    val members: List<String> = listOf("A", "Y", "D"),
    val extraMembersCount: Int = 2
)

val defaultAllGroupTasks = emptyList<GroupTaskDetailItem>()

@Composable
fun AllGroupTasksScreen(
    tasks: List<GroupTaskDetailItem> = defaultAllGroupTasks,
    onNavigateBack: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTab by remember { mutableStateOf("Semua") }
    var isSortDescending by remember { mutableStateOf(true) }

    val filterTabs = listOf("Semua", "On Progress", "Selesai")

    val filteredTasks = remember(searchQuery, selectedFilterTab, tasks, isSortDescending) {
        val filtered = tasks.filter { item ->
            val matchesQuery = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.subject.contains(searchQuery, ignoreCase = true)

            val matchesTab = when (selectedFilterTab) {
                "On Progress" -> item.status.equals("On Progress", ignoreCase = true)
                "Selesai" -> item.status.equals("Selesai", ignoreCase = true)
                else -> true
            }

            matchesQuery && matchesTab
        }
        val sorted = filtered.sortedWith { a, b ->
            val aDone = a.status.equals("Selesai", ignoreCase = true)
            val bDone = b.status.equals("Selesai", ignoreCase = true)
            if (aDone != bDone) return@sortedWith if (!aDone) -1 else 1

            val daysA = TaskDateUtils.getDaysRemaining(a.date)
            val daysB = TaskDateUtils.getDaysRemaining(b.date)
            if (daysA != daysB) return@sortedWith daysA.compareTo(daysB)
            a.title.compareTo(b.title, ignoreCase = true)
        }
        if (isSortDescending) sorted else sorted.reversed()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3ED))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(bottom = 36.dp)
        ) {
            // 1. Top Header: Back Button + Title "Tugas Kelompok"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color(0xFF2E4841),
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tugas Kelompok",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF2E4841)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 2. Search Box + Filter Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Cari tugas kelompok...",
                            fontSize = 13.5.sp,
                            color = Color(0xFF888888)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF4F7366),
                        unfocusedBorderColor = Color(0xFFE5E0D3)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            selectedFilterTab = when (selectedFilterTab) {
                                "Semua" -> "On Progress"
                                "On Progress" -> "Selesai"
                                else -> "Semua"
                            }
                        },
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E0D3))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filter",
                            tint = Color(0xFF2E4841),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Filter Tabs (Semua, On Progress, Selesai)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                filterTabs.forEach { tab ->
                    val isSelected = selectedFilterTab == tab
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedFilterTab = tab },
                        color = if (isSelected) Color(0xFF436557) else Color.White,
                        shape = RoundedCornerShape(14.dp),
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E0D3))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = tab,
                                color = if (isSelected) Color.White else Color(0xFF333333),
                                fontSize = 13.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Counter & Sort Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total ${filteredTasks.size} tugas kelompok",
                    fontSize = 12.5.sp,
                    color = Color(0xFF7A8680),
                    fontWeight = FontWeight.Medium
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isSortDescending = !isSortDescending }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isSortDescending) "Terbaru" else "Terlama",
                        fontSize = 12.5.sp,
                        color = Color(0xFF444444),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Sort",
                        tint = Color(0xFF555555),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. List Kartu Tugas Kelompok
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada tugas kelompok yang cocok.",
                        fontSize = 13.5.sp,
                        color = Color(0xFF888888)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    filteredTasks.forEach { item ->
                        GroupTaskCard(task = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupTaskCard(task: GroupTaskDetailItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEBE6DC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Info Kiri: Judul, Mata Pelajaran, Tanggal
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2925)
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = Color(0xFF7A8680),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = task.subject,
                        fontSize = 11.5.sp,
                        color = Color(0xFF7A8680)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color(0xFF7A8680),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val dueLabel = TaskDateUtils.getDueStatusLabel(task.date)
                    val displayText = if (task.date.equals(dueLabel, ignoreCase = true) || dueLabel == "Selesai") {
                        task.date
                    } else {
                        "${task.date} • $dueLabel"
                    }
                    Text(
                        text = displayText,
                        fontSize = 11.5.sp,
                        color = if (TaskDateUtils.getDaysRemaining(task.date) <= 1) Color(0xFFD9534F) else Color(0xFF7A8680),
                        fontWeight = if (TaskDateUtils.getDaysRemaining(task.date) <= 1) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Kanan: Status Badge + Avatars & Chevron Right
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Status Badge
                val (badgeBg, badgeTextColor) = when (task.status) {
                    "Selesai" -> Pair(Color(0xFFCEE7D0), Color(0xFF3F6B43))
                    "On Progress" -> Pair(Color(0xFFFCEFD2), Color(0xFF9E7019))
                    else -> Pair(Color(0xFFE8E8E8), Color(0xFF666666))
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = task.status,
                        color = badgeTextColor,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Avatars Row + Chevron Arrow
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy((-4).dp)) {
                        task.members.forEach { initial ->
                            Surface(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape),
                                color = Color(0xFFEDE8DF),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = initial,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF444444)
                                    )
                                }
                            }
                        }
                        if (task.extraMembersCount > 0) {
                            Surface(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape),
                                color = Color(0xFFE4EDE7),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "+${task.extraMembersCount}",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF355849)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Detail",
                        tint = Color(0xFF888888),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
