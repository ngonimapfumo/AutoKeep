package com.ngonim.autokeep.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.ngonim.autokeep.data.VehiclePhotoStore

@Composable
fun VehicleHero(
    modifier: Modifier = Modifier,
    photoUri: String? = null,
    contentDescription: String? = null,
    showAddHint: Boolean = false,
) {
    val bitmap = remember(photoUri) { VehiclePhotoStore.decode(photoUri) }
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(primary, primary.copy(alpha = 0.55f)))),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        } else if (showAddHint) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.AddAPhoto,
                    contentDescription = contentDescription,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(36.dp),
                )
                Text("Add a photo", color = Color.White.copy(alpha = 0.9f))
            }
        } else {
            Icon(
                imageVector = Icons.Filled.DirectionsCar,
                contentDescription = contentDescription,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(56.dp),
            )
        }
    }
}
