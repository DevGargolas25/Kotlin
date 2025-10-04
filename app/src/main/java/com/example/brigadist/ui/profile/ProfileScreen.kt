package com.example.brigadist.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brigadist.ui.profile.model.*

// Import necesario para íconos
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = viewModel()
) {
    // TODO: Reemplazar por el email real del usuario autenticado
    val currentUserEmail = remember { mutableStateOf("juan.perez@uniandes.edu.co") }

    val loading by profileViewModel.loading
    val error by profileViewModel.errorMessage
    val userData by profileViewModel.userData

    var fullName by remember { mutableStateOf(userData["fullName"]?.toString() ?: "") }
    var email by remember { mutableStateOf(userData["email"]?.toString() ?: "") }
    var phone by remember { mutableStateOf(userData["phone"]?.toString() ?: "") }
    var bloodType by remember { mutableStateOf(userData["bloodType"]?.toString() ?: "") }
    var doctorName by remember { mutableStateOf(userData["doctorName"]?.toString() ?: "") }

    LaunchedEffect(userData) {
        fullName = userData["fullName"]?.toString() ?: ""
        email = userData["email"]?.toString() ?: ""
        phone = userData["phone"]?.toString() ?: ""
        bloodType = userData["bloodType"]?.toString() ?: ""
        doctorName = userData["doctorName"]?.toString() ?: ""
    }

    LaunchedEffect(currentUserEmail.value) {
        val e = currentUserEmail.value
        if (e.isNotBlank()) {
            profileViewModel.loadProfile(e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "avatar",
                    modifier = Modifier.align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = fullName.ifBlank { "Nombre no establecido" },
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = email.ifBlank { "Email no establecido" },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Nombre completo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Teléfono") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = bloodType,
            onValueChange = { bloodType = it },
            label = { Text("Tipo de sangre") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = doctorName,
            onValueChange = { doctorName = it },
            label = { Text("Médico") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val updateMap = mapOf<String, Any?>(
                    "fullName" to fullName,
                    "email" to email,
                    "phone" to phone,
                    "bloodType" to bloodType,
                    "doctorName" to doctorName
                )
                profileViewModel.updateProfileFields(updateMap) { _, _ -> }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar cambios")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        error?.let { msg ->
            Text(text = "Error: $msg", color = MaterialTheme.colorScheme.error)
        }
    }
}
