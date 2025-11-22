package com.example.brigadist.ui.profile.ui.profile

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.brigadist.Orquestador
import com.example.brigadist.data.repository.FirebaseProfileRepository
import com.example.brigadist.ui.profile.ProfilePresenter
import com.example.brigadist.ui.profile.ProfileView
import com.example.brigadist.ui.profile.model.Allergies
import com.example.brigadist.ui.profile.model.EmergencyContact
import com.example.brigadist.ui.profile.model.FirebaseUserProfile
import com.example.brigadist.ui.profile.model.MedicalInfo
import com.example.brigadist.ui.profile.model.Medications
import com.example.brigadist.ui.profile.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


data class ProfileScreenState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userProfile: UserProfile? = null,
    val emergencyContact: EmergencyContact? = null,
    val medicalInfo: MedicalInfo? = null,
    val allergies: Allergies? = null,
    val medications: Medications? = null,
    val firebaseUserProfile: FirebaseUserProfile? = null
) {
    // Helper function to get display data with fallback support (always returns non-null profile data)
    fun getDisplayData(orquestador: Orquestador) = DisplayProfileData(
        isLoading = isLoading,
        errorMessage = errorMessage,
        userProfile = userProfile ?: orquestador.getUserProfile(),
        emergencyContact = emergencyContact ?: orquestador.getEmergencyContact(),
        medicalInfo = medicalInfo ?: orquestador.getMedicalInfo(),
        allergies = allergies ?: orquestador.getAllergies(),
        medications = medications ?: orquestador.getMedications(),
        firebaseUserProfile = firebaseUserProfile ?: orquestador.firebaseUserProfile
    )
}

// Non-nullable data class for display purposes (always has valid data)
data class DisplayProfileData(
    val isLoading: Boolean,
    val errorMessage: String?,
    val userProfile: UserProfile,
    val emergencyContact: EmergencyContact,
    val medicalInfo: MedicalInfo,
    val allergies: Allergies,
    val medications: Medications,
    val firebaseUserProfile: FirebaseUserProfile?
)

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    orquestador: Orquestador,
    onLogout: () -> Unit
) {
    // Single state holder for all screen state
    var state by remember { mutableStateOf(ProfileScreenState()) }
    
    // Get display data with fallback support
    val displayData = state.getDisplayData(orquestador)
    
    // Offline alert state
    val context = LocalContext.current
    var showOfflineAlert by remember { mutableStateOf(false) }
    
    // Connectivity check function
    fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    // Handle logout with connectivity check
    fun handleLogout() {
        if (!isOnline()) {
            showOfflineAlert = true
        } else {
            onLogout()
        }
    }
    
    // Create ProfilePresenter and ProfileView implementation with state updater
    val updateState: (ProfileScreenState.() -> ProfileScreenState) -> Unit = remember {
        { update -> state = state.update() }
    }
    
    val profileView = remember(updateState) {
        object : ProfileView {
            override fun showLoading() {
                updateState { copy(isLoading = true, errorMessage = null) }
            }
            
            override fun hideLoading() {
                updateState { copy(isLoading = false) }
            }
            
            override fun showProfile(profile: FirebaseUserProfile) {
                // Legacy method - intentionally empty (not used with multithreading approach)
            }
            
            override fun showProfileData(
                firebaseProfile: FirebaseUserProfile,
                userProfile: UserProfile,
                emergencyContact: EmergencyContact,
                medicalInfo: MedicalInfo,
                allergies: Allergies,
                medications: Medications
            ) {
                updateState {
                    copy(
                        isLoading = false,
                        errorMessage = null,
                        firebaseUserProfile = firebaseProfile,
                        userProfile = userProfile,
                        emergencyContact = emergencyContact,
                        medicalInfo = medicalInfo,
                        allergies = allergies,
                        medications = medications
                    )
                }
            }
            
            override fun showError(message: String) {
                updateState { copy(isLoading = false, errorMessage = message) }
            }
            
            override fun showSuccess(message: String) {
                updateState { copy(isLoading = false) }
            }
        }
    }
    
    val presenter = remember {
        ProfilePresenter(
            view = profileView,
            repository = FirebaseProfileRepository(),
            coroutineScope = CoroutineScope(Dispatchers.Default),
            context = context
        )
    }
    
    // Load profile data using multithreading when screen is first shown
    LaunchedEffect(Unit) {
        val userEmail = displayData.userProfile.email.ifEmpty { orquestador.getUserProfile().email }
        if (userEmail.isNotEmpty()) {
            presenter.loadProfile(userEmail)
        }
    }
    
    var isEditing by remember { mutableStateOf(false) }

    if (isEditing) {
        val profileToEdit = displayData.firebaseUserProfile ?: FirebaseUserProfile(
            fullName = displayData.userProfile.fullName,
            email = displayData.userProfile.email
        )

        EditProfileScreen(
            profile = profileToEdit,
            onSave = { updatedProfile ->
                presenter.saveProfile(updatedProfile)
                // Also update Orquestador for backward compatibility
                orquestador.updateUserProfile(updatedProfile)
                isEditing = false
                // Reload profile data after saving
                if (updatedProfile.email.isNotEmpty()) {
                    presenter.loadProfile(updatedProfile.email)
                }
            },
            onCancel = { isEditing = false }
        )
    } else {
        val scrollState = rememberScrollState()

        Box(modifier = modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Show loading indicator if loading
                if (displayData.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                // Show error message if any
                displayData.errorMessage?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
                
                ProfileHeader(userProfile = displayData.userProfile, onEdit = { isEditing = true })
                Spacer(modifier = Modifier.height(16.dp))

                SectionCard(
                    icon = Icons.Default.Person,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Personal Information"
                ) {
                    FieldRow(label = "Full Name", value = displayData.userProfile.fullName)
                    FieldRow(label = "Student ID", value = displayData.userProfile.studentId)
                    FieldRow(label = "Email", value = displayData.userProfile.email)
                    FieldRow(label = "Phone", value = displayData.userProfile.phone)
                }
                Spacer(modifier = Modifier.height(16.dp))

                SectionCard(
                    icon = Icons.Default.Phone,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = "Emergency Contacts"
                ) {
                    FieldRow(label = "Primary Contact", value = displayData.emergencyContact.primaryContactName)
                    FieldRow(label = "Primary Phone", value = displayData.emergencyContact.primaryContactPhone)
                    FieldRow(label = "Secondary Contact", value = displayData.emergencyContact.secondaryContactName)
                }
                Spacer(modifier = Modifier.height(16.dp))

                SectionCard(
                    icon = Icons.Default.FavoriteBorder,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Medical Information"
                ) {
                    FieldRow(label = "Blood Type", value = displayData.medicalInfo.bloodType)
                    FieldRow(label = "Primary Physician", value = displayData.medicalInfo.primaryPhysician)
                    FieldRow(label = "Physician Phone", value = displayData.medicalInfo.physicianPhone)
                    FieldRow(label = "Insurance Provider", value = displayData.medicalInfo.insuranceProvider)
                }
                Spacer(modifier = Modifier.height(16.dp))

                SectionCard(
                    icon = Icons.Default.Info,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Allergies"
                ) {
                    FieldRow(label = "Food Allergies", value = displayData.allergies.foodAllergies)
                    FieldRow(label = "Environmental Allergies", value = displayData.allergies.environmentalAllergies)
                    FieldRow(label = "Drug Allergies", value = displayData.allergies.drugAllergies)
                    FieldRow(label = "Severity Notes", value = displayData.allergies.severityNotes)
                }
                Spacer(modifier = Modifier.height(16.dp))

                SectionCard(
                    icon = Icons.Default.ShoppingCart,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = "Current Medications"
                ) {
                    FieldRow(label = "Daily Medications", value = displayData.medications.dailyMedications)
                    FieldRow(label = "Emergency Medications", value = displayData.medications.emergencyMedications)
                    FieldRow(label = "Vitamins/Supplements", value = displayData.medications.vitaminsSupplements)
                    FieldRow(label = "Special Instructions", value = displayData.medications.specialInstructions)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = ::handleLogout) {
                    Text("Log Out")
                }
            }
            }
            
            // Non-intrusive offline alert at the top
            AnimatedVisibility(
                visible = showOfflineAlert,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = "No Internet",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Please connect to the internet to successfully log out",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { showOfflineAlert = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(userProfile: UserProfile, onEdit: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = userProfile.fullName,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Student Brigade Member",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit profile",
                tint = Color(0xFFB4A4C0)
            )
        }
    }
}

@Composable
fun SectionCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderColour = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, borderColour),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun FieldRow(label: String, value: String) {
    Text(
        text = label,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.labelLarge
    )
    Spacer(modifier = Modifier.height(4.dp))
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (value.isNotBlank()) value else " ",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}