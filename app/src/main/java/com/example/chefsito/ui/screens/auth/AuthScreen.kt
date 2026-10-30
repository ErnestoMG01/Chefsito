package com.example.chefsito.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chefsito.ui.theme.*
import com.example.chefsito.ui.viewmodel.AuthViewModel

@Composable
fun ChefHatIcon(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Hat Puff Path
        val path = Path().apply {
            moveTo(w * 0.25f, h * 0.60f)
            cubicTo(w * 0.05f, h * 0.45f, w * 0.15f, h * 0.15f, w * 0.35f, h * 0.20f)
            cubicTo(w * 0.40f, h * 0.02f, w * 0.60f, h * 0.02f, w * 0.65f, h * 0.20f)
            cubicTo(w * 0.85f, h * 0.15f, w * 0.95f, h * 0.45f, w * 0.75f, h * 0.60f)
            close()
        }

        // Fill puff yellow
        drawPath(path = path, color = AmarilloMostaza)
        // Outline puff
        drawPath(path = path, color = TintaComic, style = Stroke(width = w * 0.06f))

        // Hat Base/Brim
        val brimWidth = w * 0.54f
        val brimHeight = h * 0.22f
        val brimLeft = w * 0.23f
        val brimTop = h * 0.58f

        drawRoundRect(
            color = AmarilloMostaza,
            topLeft = Offset(brimLeft, brimTop),
            size = Size(brimWidth, brimHeight),
            cornerRadius = CornerRadius(w * 0.06f)
        )
        drawRoundRect(
            color = TintaComic,
            topLeft = Offset(brimLeft, brimTop),
            size = Size(brimWidth, brimHeight),
            cornerRadius = CornerRadius(w * 0.06f),
            style = Stroke(width = w * 0.06f)
        )

        // Inner vertical fold lines
        drawLine(
            color = TintaComic,
            start = Offset(w * 0.42f, brimTop + h * 0.04f),
            end = Offset(w * 0.42f, brimTop + brimHeight - h * 0.04f),
            strokeWidth = w * 0.04f
        )
        drawLine(
            color = TintaComic,
            start = Offset(w * 0.58f, brimTop + h * 0.04f),
            end = Offset(w * 0.58f, brimTop + brimHeight - h * 0.04f),
            strokeWidth = w * 0.04f
        )
    }
}

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(),
    onLoginSuccess: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoCrema)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header Banner Estilo Cómic
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.5.dp, TintaComic, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(AmarilloMostaza)
                .padding(20.dp)
        ) {
            // Círculos decorativos en el fondo
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = Color(0x33000000),
                    radius = size.width * 0.18f,
                    center = Offset(size.width * 0.90f, size.height * 0.15f)
                )
                drawCircle(
                    color = Color(0x22000000),
                    radius = size.width * 0.12f,
                    center = Offset(size.width * 0.78f, size.height * 0.82f)
                )
            }

            Column {
                // Tarjeta del Logo de Chefsito
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .border(2.5.dp, TintaComic, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(BlancoPuro),
                    contentAlignment = Alignment.Center
                ) {
                    ChefHatIcon(size = 40.dp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "MiMenú",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = TintaComic
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Come bien, gasta poco",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TintaComic.copy(alpha = 0.85f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Selector de Pestañas: Iniciar sesión / Registrarme
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .border(2.5.dp, TintaComic, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(BlancoPuro)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (uiState.isLoginMode) TintaComic else Color.Transparent)
                    .clickable { viewModel.setLoginMode(isLogin = true) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Iniciar sesión",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (uiState.isLoginMode) BlancoPuro else TintaComic
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (!uiState.isLoginMode) TintaComic else Color.Transparent)
                    .clickable { viewModel.setLoginMode(isLogin = false) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Registrarme",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (!uiState.isLoginMode) BlancoPuro else TintaComic
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Formulario Dinámico
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Campo Nombre (solo en modo Registro)
            AnimatedVisibility(
                visible = !uiState.isLoginMode,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ChefsitoCustomField(
                    label = "TU NOMBRE",
                    value = uiState.name,
                    onValueChange = { viewModel.onNameChange(it) },
                    placeholder = "Valentina García",
                    icon = Icons.Default.Person,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )
            }

            // Campo Correo Electrónico
            ChefsitoCustomField(
                label = "CORREO ELECTRÓNICO",
                value = uiState.email,
                onValueChange = { viewModel.onEmailChange(it) },
                placeholder = "tu@correo.com",
                icon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            // Campo Contraseña
            ChefsitoCustomField(
                label = "CONTRASEÑA",
                value = uiState.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                placeholder = "••••••••",
                icon = Icons.Default.Lock,
                isPassword = true,
                isPasswordVisible = uiState.isPasswordVisible,
                onTogglePasswordVisibility = { viewModel.togglePasswordVisibility() },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )

            // Link "¿Olvidaste tu contraseña?" (solo en Iniciar sesión)
            if (uiState.isLoginMode) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaranjaCoral,
                        modifier = Modifier.clickable { viewModel.onForgotPasswordClick() }
                    )
                }
            }

            // Banner de Mensaje de Error
            uiState.errorMessage?.let { error ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, TintaComic, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(NaranjaCoral)
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(BlancoPuro),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PriorityHigh,
                                contentDescription = null,
                                tint = NaranjaCoral,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BlancoPuro
                        )
                    }
                }
            }

            // Banner de Mensaje de Éxito
            uiState.successMessage?.let { success ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, TintaComic, RoundedCornerShape(14.dp))
                        .clip(RoundedCornerShape(14.dp))
                        .background(VerdeFresco)
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BlancoPuro),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = VerdeFresco,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "ÉXITO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = BlancoPuro.copy(alpha = 0.9f)
                            )
                            Text(
                                text = success,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlancoPuro
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón Principal CTA
            Button(
                onClick = { viewModel.submitForm(onLoginSuccess) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(2.5.dp, TintaComic, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NaranjaCoral)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (uiState.isLoginMode) "Entrar  →" else "Crear cuenta  →",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = BlancoPuro
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ChefsitoCustomField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = TintaComic
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .border(2.dp, TintaComic, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(BlancoPuro),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Caja Amarilla del Icono a la Izquierda
            Box(
                modifier = Modifier
                    .width(52.dp)
                    .fillMaxHeight()
                    .background(AmarilloMostaza)
                    .border(
                        width = 2.dp,
                        color = TintaComic,
                        shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TintaComic,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Campo de Texto Principal
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 14.sp,
                        color = TintaComic.copy(alpha = 0.45f)
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TintaComic
                    ),
                    visualTransformation = if (isPassword && !isPasswordVisible) {
                        PasswordVisualTransformation()
                    } else {
                        VisualTransformation.None
                    },
                    keyboardOptions = keyboardOptions,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Icono del Ojo para Contraseña a la Derecha
            if (isPassword && onTogglePasswordVisibility != null) {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Mostrar u ocultar contraseña",
                        tint = TintaComic.copy(alpha = 0.6f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    ChefsitoTheme {
        AuthScreen()
    }
}
