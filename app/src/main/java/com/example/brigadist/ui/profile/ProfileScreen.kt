package com.example.brigadist.ui.profile


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

/**
 * Profile screen displaying user information such as personal details, emergency contacts,
 * medical info, allergies and current medications. It closely follows the layout of
 * the provided images, using cards with rounded corners and subtle borders to group
 * related fields. A bottom navigation bar allows movement between the main app screens.
 */
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onNavigateHome: () -> Unit = {},
    onNavigateChat: () -> Unit = {},
    onNavigateMap: () -> Unit = {},
    onNavigateVideos: () -> Unit = {},
    onSOS: () -> Unit = {},
    onEditProfile: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    Scaffold(

        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header: avatar, name, role and edit button
            ProfileHeader(onEdit = onEditProfile)
            Spacer(modifier = Modifier.height(16.dp))

            // Personal Information Section
            SectionCard(
                icon = Icons.Default.Person,
                iconTint = Color(0xFF75C1C7),
                title = "Personal Information"
            ) {
                FieldRow(label = "Full Name", value = "John Smith")
                FieldRow(label = "Student ID", value = "SB2024001")
                FieldRow(label = "Email", value = "john.smith@university.edu")
                FieldRow(label = "Phone", value = "+1 (555) 123-4567")
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Emergency Contacts Section
            SectionCard(
                icon = Icons.Default.Phone,
                iconTint = Color(0xFF60B896),
                title = "Emergency Contacts"
            ) {
                FieldRow(label = "Primary Contact", value = "Jane Smith (Mother)")
                FieldRow(label = "Primary Phone", value = "+1 (555) 987-6543")
                FieldRow(label = "Secondary Contact", value = "")
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Medical Information Section
            SectionCard(
                icon = Icons.Default.FavoriteBorder,
                iconTint = Color(0xFF75C1C7),
                title = "Medical Information"
            ) {
                FieldRow(label = "Blood Type", value = "O+")
                FieldRow(label = "Primary Physician", value = "Dr. Sarah Johnson")
                FieldRow(label = "Physician Phone", value = "+1 (555) 234-5678")
                FieldRow(label = "Insurance Provider", value = "University Health Plan")
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Allergies Section
            SectionCard(
                icon = Icons.Default.Info,
                iconTint = Color(0xFF75C1C7),
                title = "Allergies"
            ) {
                FieldRow(label = "Food Allergies", value = "Peanuts, Shellfish")
                FieldRow(label = "Environmental Allergies", value = "Pollen, Dust mites")
                FieldRow(label = "Drug Allergies", value = "")
                FieldRow(label = "Severity Notes", value = "Carry EpiPen for severe reactions")
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Current Medications Section
            SectionCard(
                icon = Icons.Default.ShoppingCart,
                iconTint = Color(0xFF60B896),
                title = "Current Medications"
            ) {
                FieldRow(label = "Daily Medications", value = "Inhaler (Albuterol) - As needed for asthma")
                FieldRow(label = "Emergency Medications", value = "EpiPen - For severe allergic reactions")
                FieldRow(label = "Vitamins/Supplements", value = "Vitamin D3 - 1000 IU daily")
                FieldRow(label = "Special Instructions", value = "Keep inhaler and EpiPen accessible at all times")
            }
        }
    }
}

/**
 * Header at the top of the profile screen containing an avatar placeholder, the user's
 * name and role, and an edit icon. Colours and typography mirror the provided
 * design.
 */
@Composable
fun ProfileHeader(onEdit: () -> Unit = {}) {
    val avatarBackground = Color(0xFFEAF5F6)
    val avatarTint = Color(0xFF75C1C7)
    val nameColour = Color(0xFF4A2951)
    val roleColour = Color(0x884A2951)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(avatarBackground, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = avatarTint,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "John Smith",
                color = nameColour,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Student Brigade Member",
                color = roleColour,
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

/**
 * A card component grouping related profile fields. Displays an icon and a title at the
 * top followed by arbitrary content. The card uses a white background, subtle border
 * and rounded corners similar to the design in the uploaded images.
 */
@Composable
fun SectionCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderColour = Color(0x3399D2D2)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
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
                    color = Color(0xFF4A2951),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

/**
 * Displays a single labelled field within a profile section. The label appears above
 * a rounded surface containing the value. Empty values are represented by an
 * unobtrusive placeholder.
 */
@Composable
fun FieldRow(label: String, value: String) {
    val labelColour = Color(0xFF4A2951)
    val valueColour = Color(0xFF4A2951)
    val fieldBackground = Color(0xFFF7FBFC)
    Text(
        text = label,
        color = labelColour,
        style = MaterialTheme.typography.labelLarge
    )
    Spacer(modifier = Modifier.height(4.dp))
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = fieldBackground,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (value.isNotBlank()) value else " ",
            color = valueColour,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 12.dp)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}