package com.jadwalstudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jadwalstudio.app.ui.theme.BgCream
import com.jadwalstudio.app.ui.theme.SageGreen
import com.jadwalstudio.app.ui.theme.TextDark
import com.jadwalstudio.app.ui.theme.White

// ---------------------- OTP SCREEN (KIRIM KODE) ----------------------
@Composable
fun OtpScreen(
    onSubmitOtp: (String) -> Unit
) {
    var otpCode by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthHeader()

            Spacer(modifier = Modifier.height(130.dp))

            // 4 Underline Garis Input OTP (Persis Desain Figma)
            BasicTextField(
                value = otpCode,
                onValueChange = {
                    if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                        otpCode = it
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.focusRequester(focusRequester),
                decorationBox = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 4) {
                            val char = otpCode.getOrNull(i)?.toString() ?: ""
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(55.dp)
                            ) {
                                Text(
                                    text = char,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.height(38.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.5.dp)
                                        .background(if (i == otpCode.length) SageGreen else Color(0xFF333333))
                                )
                            }
                        }
                    }
                }
            )
        }

        // Tombol MASUKAN KODE di Bawah
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp, vertical = 36.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { onSubmitOtp(otpCode) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SageGreen,
                    contentColor = White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "MASUKAN KODE",
                    color = White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// ---------------------- RESET PASSWORD SCREEN (KATA SANDI BARU) ----------------------
@Composable
fun ResetPasswordScreen(
    onResetSuccess: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            AuthHeader()

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
            ) {
                Text(
                    text = "Kata Sandi Baru",
                    color = SageGreen,
                    fontSize = 34.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Buat kata sandi baru yang kuat untuk melindungi akun Anda.",
                    color = Color(0xFF666666),
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "KATA SANDI BARU",
                    color = Color(0xFF444444),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    placeholder = { Text("••••••••", color = Color.Gray) },
                    singleLine = true,
                    visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                            Icon(
                                imageVector = if (isNewPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password",
                                tint = Color.Gray
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SageGreen,
                        unfocusedBorderColor = SageGreen.copy(alpha = 0.5f),
                        focusedContainerColor = White.copy(alpha = 0.35f),
                        unfocusedContainerColor = White.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Info Minimal 8 Karakter
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .border(1.dp, Color(0xFF777777), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("i", fontSize = 9.sp, color = Color(0xFF777777))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Minimal 8 karakter",
                        color = Color(0xFF777777),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "KONFIRMASI KATA SANDI BARU",
                    color = Color(0xFF444444),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = { Text("••••••••", color = Color.Gray) },
                    singleLine = true,
                    visualTransformation = if (isConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { isConfirmVisible = !isConfirmVisible }) {
                            Icon(
                                imageVector = if (isConfirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle confirm password",
                                tint = Color.Gray
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SageGreen,
                        unfocusedBorderColor = SageGreen.copy(alpha = 0.5f),
                        focusedContainerColor = White.copy(alpha = 0.35f),
                        unfocusedContainerColor = White.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Tombol Konfirmasi Sandi di Bawah
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp, vertical = 36.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onResetSuccess,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SageGreen,
                    contentColor = White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Konfirmasi sandi",
                    color = White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
