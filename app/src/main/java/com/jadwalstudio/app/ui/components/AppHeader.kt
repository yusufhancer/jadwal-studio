package com.jadwalstudio.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.TextButton
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jadwalstudio.app.R

data class HeaderUser(val name: String = "", val email: String = "", val onRefresh: () -> Unit = {},
    val photo: String = "", val onPhotoSelected: (android.net.Uri) -> Unit = {})
val LocalHeaderUser = androidx.compose.runtime.staticCompositionLocalOf { HeaderUser() }

val AppHeaderGreen = Color(0xFF4F7366)

@Composable
fun AppHeader(
    modifier: Modifier = Modifier,
    avatarText: String = LocalHeaderUser.current.name.take(1).uppercase(),
    userName: String = LocalHeaderUser.current.name,
    userEmail: String = LocalHeaderUser.current.email,
    onLogout: () -> Unit = {},
    onAvatarClick: (() -> Unit)? = null
) {
    var showProfileDialog by rememberSaveable { mutableStateOf(false) }
    val headerUser = LocalHeaderUser.current
    val photo = remember(headerUser.photo) {
        runCatching {
            val bytes = android.util.Base64.decode(headerUser.photo, android.util.Base64.DEFAULT)
            android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }.getOrNull()
    }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            showProfileDialog = false
            headerUser.onPhotoSelected(uri)
        }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(AppHeaderGreen)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo & Brand JadwalStudio
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo_didalam_apk),
                    contentDescription = "Logo JadwalStudio",
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "JadwalStudio",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.sp,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Personal planner",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))
            // Avatar A dengan status dot hijau di LUAR circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable {
                        if (onAvatarClick != null) {
                            onAvatarClick()
                        } else {
                            showProfileDialog = true
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(Color(0xFFD9CFC4)),
                    contentAlignment = Alignment.Center
                ) {
                    if (photo != null) Image(photo, "Foto profil", Modifier.size(36.dp), contentScale = ContentScale.Crop)
                    else Text(
                        text = avatarText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                }
            }
        }
    }

    if (showProfileDialog) {
        Dialog(
            onDismissRequest = { showProfileDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(24.dp)),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD9CFC4)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photo != null) Image(photo, "Foto profil", Modifier.size(68.dp), contentScale = ContentScale.Crop)
                        else Text(
                            text = avatarText,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = userName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFF2E4841)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = userEmail,
                        fontSize = 13.sp,
                        color = Color(0xFF7A8680)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = { photoPicker.launch("image/*") }) {
                        Text("Ganti foto profil", color = AppHeaderGreen)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val refreshData = LocalHeaderUser.current.onRefresh
                    Button(
                        onClick = {
                            showProfileDialog = false
                            refreshData()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppHeaderGreen)
                    ) {
                        Text("Muat Ulang", color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            showProfileDialog = false
                            onLogout()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9534F))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Keluar Akun (Logout)",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { showProfileDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0ECE5))
                    ) {
                        Text(
                            text = "Tutup",
                            color = Color(0xFF555555),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
