package com.example.brigadist.ui.map.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.MapType

@Composable
fun MapTypeSelector(
    selectedMapType: MapType,
    onMapTypeSelected: (MapType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MapType.values().forEach { mapType ->
                val isSelected = selectedMapType == mapType
                FilterChip(
                    onClick = { onMapTypeSelected(mapType) },
                    label = {
                        Text(
                            text = when (mapType) {
                                MapType.NORMAL -> "Normal"
                                MapType.SATELLITE -> "Satellite"
                                MapType.TERRAIN -> "Terrain"
                                MapType.HYBRID -> "Hybrid"
                                MapType.NONE -> "None"
                            },
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selected = isSelected,
                    modifier = Modifier.height(32.dp)
                )
            }
        }
    }
}
