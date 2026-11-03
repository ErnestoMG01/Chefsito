package com.example.chefsito.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.chefsito.ui.components.ChefsitoMessageBanner
import com.example.chefsito.ui.components.MessageType
import com.example.chefsito.ui.theme.*
import com.example.chefsito.ui.viewmodel.ChefViewModel
import java.io.File

@Composable
fun ProfileScreen(
    viewModel: ChefViewModel,
    onNavigateHome: () -> Unit = {},
    onNavigatePlan: () -> Unit = {},
    onNavigateFavorites: () -> Unit = {},
    onNavigateProfile: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val imagePath by viewModel.profileImagePath.collectAsState()

    // Datos guardados: se cargan como valor inicial de los campos
    val savedName by viewModel.profileName.collectAsState()
    val savedEmail by viewModel.profileEmail.collectAsState()
    val savedPhone by viewModel.profilePhone.collectAsState()

    var name by remember { mutableStateOf(savedName) }
    var email by remember { mutableStateOf(savedEmail) }
    var phone by remember { mutableStateOf(savedPhone) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.updateProfileImage(context, it)
            statusMessage = "Tu foto de perfil se actualizó correctamente."
            isError = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoCrema)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            statusMessage?.let { message ->
                item {
                    ChefsitoMessageBanner(
                        title = if (isError) "ERROR" else "ÉXITO",
                        message = message,
                        type = if (isError) MessageType.WARNING else MessageType.SUCCESS,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }

            item {
                Text(
                    text = "Mi Perfil",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TintaComic
                )
                Text(
                    text = "Administra tus datos y seguridad",
                    fontSize = 12.sp,
                    color = TintaComic.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ---- Foto de perfil ----
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, TintaComic, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BlancoPuro)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .border(2.dp, TintaComic, CircleShape)
                                .clip(CircleShape)
                                .background(FondoCrema),
                            contentAlignment = Alignment.Center
                        ) {
                            val path = imagePath
                            if (path != null && File(path).exists()) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = File(path)),
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(text = "👤", fontSize = 28.sp)
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Foto de perfil",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TintaComic
                            )
                            Text(
                                text = "JPG, PNG o WEBP.",
                                fontSize = 11.sp,
                                color = TintaComic.copy(alpha = 0.6f)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { imageLauncher.launch("image/*") },
                                modifier = Modifier
                                    .height(36.dp)
                                    .border(1.5.dp, TintaComic, RoundedCornerShape(8.dp)),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BlancoPuro),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "⬆ Subir foto",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TintaComic
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Datos personales",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TintaComic
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ---- Datos personales ----
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("NOMBRE") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = TintaComic)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("CORREO ELECTRÓNICO") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = TintaComic)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("TELÉFONO OPCIONAL") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = TintaComic)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            if (email.isNotBlank() && !email.contains("@")) {
                                statusMessage = "Ingresa un correo electrónico válido."
                                isError = true
                            } else {
                                viewModel.updateProfileData(name, email, phone)
                                statusMessage = "Tus datos se actualizaron correctamente."
                                isError = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(2.dp, TintaComic, RoundedCornerShape(10.dp)),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NaranjaCoral)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                tint = BlancoPuro,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar mis datos", color = BlancoPuro, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Cambiar contraseña",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TintaComic
                )
                Text(
                    text = "Usa al menos 8 caracteres.",
                    fontSize = 11.sp,
                    color = TintaComic.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ---- Contraseña ----
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("CONTRASEÑA ACTUAL") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = TintaComic)
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("NUEVA CONTRASEÑA") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = TintaComic)
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("CONFIRMAR NUEVA CONTRASEÑA") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = TintaComic)
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            if (newPassword.length < 8) {
                                statusMessage = "La nueva contraseña debe tener al menos 8 caracteres."
                                isError = true
                            } else if (newPassword != confirmPassword) {
                                statusMessage = "Las contraseñas nuevas no coinciden."
                                isError = true
                            } else {
                                statusMessage = "Tu contraseña se cambió correctamente."
                                isError = false
                                currentPassword = ""
                                newPassword = ""
                                confirmPassword = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(2.dp, TintaComic, RoundedCornerShape(10.dp)),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VerdeFresco)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = BlancoPuro,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Actualizar contraseña", color = BlancoPuro, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(2.dp, TintaComic, RoundedCornerShape(10.dp)),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TintaComic)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null,
                                tint = TintaComic,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cerrar sesión", color = TintaComic, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
