package com.example.brigadist.ui.news.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.brigadist.ui.videos.components.CategoryChip

@Composable
fun CategoryChipsRow(
    categories: List<String>,
    selected: Set<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.horizontalScroll(rememberScrollState())) {
        categories.forEachIndexed { i, cat ->
            CategoryChip(
                text = cat,
                selected = selected.contains(cat),
                onClick = { onSelected(cat) }
            )
            if (i != categories.lastIndex) Spacer(Modifier.width(12.dp))
        }
    }
}
