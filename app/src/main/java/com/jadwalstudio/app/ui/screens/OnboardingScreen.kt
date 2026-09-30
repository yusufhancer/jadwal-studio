package com.jadwalstudio.app.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jadwalstudio.app.R
import com.jadwalstudio.app.ui.theme.BgCream
import com.jadwalstudio.app.ui.theme.DotInactive
import com.jadwalstudio.app.ui.theme.JadwalStudioTheme
import com.jadwalstudio.app.ui.theme.SageGreen
import com.jadwalstudio.app.ui.theme.TextDescription
import com.jadwalstudio.app.ui.theme.TextMuted
import com.jadwalstudio.app.ui.theme.White
import kotlinx.coroutines.launch

data class OnboardingItem(
    @DrawableRes val imageRes: Int,
    val description: String
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val items = listOf(
        OnboardingItem(
            imageRes = R.drawable.ilustrasi_onboarding1,
            description = "Susun jadwal sekolah mingguanmu dengan rapi. Bebas bentrok dan selalu siap menghadapi pelajaran setiap hari."
        ),
        OnboardingItem(
            imageRes = R.drawable.ilustrasi_onboarding2,
            description = "Sistem otomatis mengurutkan tugas berdasarkan sisa waktu terdekat. Prioritas pengerjaan jadi lebih jelas tanpa rasa panik."
        ),
        OnboardingItem(
            imageRes = R.drawable.ilustrasi_onboarding3,
            description = "Notifikasi pengingat bertahap H-3, H-1, dan 3 jam sebelum tenggat siap menjaga konsistensi belajar dan waktu istirahatmu."
        )
    )

    val pagerState = rememberPagerState(pageCount = { items.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tombol SKIP di atas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "SKIP",
                color = TextMuted,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onFinish()
                    }
            )
        }

        // Konten Slider Tengah
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            val item = items[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Gambar orang / ilustrasi diperbesar proporsional
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.62f),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = item.imageRes),
                        contentDescription = "Onboarding Illustration ${page + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Teks deskripsi lebih besar dan nyaman dibaca
                Text(
                    text = item.description,
                    color = TextDescription,
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Indikator Dots
        Row(
            modifier = Modifier.padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(items.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 10.dp else 7.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) SageGreen else DotInactive)
                )
            }
        }

        // Tombol Aksi NEXT / CONTINUE
        Button(
            onClick = {
                if (pagerState.currentPage < items.size - 1) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    onFinish()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = SageGreen,
                contentColor = White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            AnimatedContent(
                targetState = pagerState.currentPage == items.size - 1,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "ButtonTextTransition"
            ) { isLastPage ->
                Text(
                    text = if (isLastPage) "CONTINUE" else "NEXT",
                    color = White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardingScreenPreview() {
    JadwalStudioTheme {
        OnboardingScreen(onFinish = {})
    }
}
