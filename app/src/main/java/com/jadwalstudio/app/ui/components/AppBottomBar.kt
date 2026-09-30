package com.jadwalstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class BottomNavTab {
    DASHBOARD,
    TUGAS,
    JADWAL
}

private val NavBg = Color(0xFFF0ECE5)
private val NavBorder = Color(0xFFDFD9CE)
private val NavActiveBg = Color(0xFF4B6B5D)
private val NavInactiveContent = Color(0xFF4C4637)
private val NavActiveContent = Color.White

@Composable
fun AppBottomNavigationBar(
    selectedTab: BottomNavTab,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = NavBg,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = NavBorder)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppBottomNavItem(
                    icon = Icons.Default.GridView,
                    label = "Dashboard",
                    isSelected = selectedTab == BottomNavTab.DASHBOARD,
                    onClick = onNavigateToDashboard,
                    modifier = Modifier.weight(1f)
                )
                AppBottomNavItem(
                    icon = Icons.Default.Timer,
                    label = "Tugas",
                    isSelected = selectedTab == BottomNavTab.TUGAS,
                    onClick = onNavigateToTasks,
                    modifier = Modifier.weight(1f)
                )
                AppBottomNavItem(
                    icon = Icons.Default.DateRange,
                    label = "Jadwal",
                    isSelected = selectedTab == BottomNavTab.JADWAL,
                    onClick = onNavigateToSchedule,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AppBottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isSelected) {
        // Squircle Pill Active Sesuai Gambar media_1789792065543.jpg & navigasi_button.png
        Surface(
            modifier = modifier
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(20.dp))
                .selectable(selected = true, role = Role.Tab, onClick = onClick),
            color = NavActiveBg,
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NavActiveContent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = label,
                    color = NavActiveContent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        // Inactive State Sesuai Gambar
        Column(
            modifier = modifier
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(14.dp))
                .selectable(selected = false, role = Role.Tab, onClick = onClick)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NavInactiveContent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                color = NavInactiveContent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
