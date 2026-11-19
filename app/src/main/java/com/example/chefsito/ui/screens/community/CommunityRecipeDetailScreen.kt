package com.example.chefsito.ui.screens.community

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.chefsito.data.remote.CommunityRecipe
import com.example.chefsito.ui.components.ChefsitoBackButton
import com.example.chefsito.ui.theme.*

/**
 * Detalle de una receta obtenida de la API externa.
 * Muestra imagen, título, categoría e instrucciones devueltas por la red.
 */
@Composable
fun CommunityRecipeDetailScreen(
    recipe: CommunityRecipe,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoCrema)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ChefsitoBackButton(text = "Regresar", onBackClick = onBack)
        Spacer(modifier = Modifier.height(12.dp))

        if (recipe.imageUrl.isNotBlank()) {
            Image(
                painter = rememberAsyncImagePainter(model = recipe.imageUrl),
                contentDescription = recipe.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(2.dp, TintaComic, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Public,
                contentDescription = null,
                tint = NaranjaCoral,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "RECETA DE LA RED",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NaranjaCoral
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = recipe.name,
            fontSize = 25.sp,
            fontWeight = FontWeight.Black,
            color = TintaComic
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoPill(recipe.cuisine)
            InfoPill(recipe.difficulty)
            InfoPill("${recipe.prepTimeMinutes} min")
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "INGREDIENTES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TintaComic.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (recipe.ingredients.isEmpty()) {
            Text(
                text = "La API no devolvió ingredientes para esta receta.",
                fontSize = 12.sp,
                color = TintaComic.copy(alpha = 0.7f)
            )
        } else {
            recipe.ingredients.forEach { ingredient ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = VerdeFresco,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ingredient,
                        fontSize = 13.sp,
                        color = TintaComic
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "INSTRUCCIONES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TintaComic.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (recipe.instructions.isEmpty()) {
            Text(
                text = "La API no devolvió instrucciones para esta receta.",
                fontSize = 12.sp,
                color = TintaComic.copy(alpha = 0.7f)
            )
        } else {
            recipe.instructions.forEachIndexed { index, instruction ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.5.dp, TintaComic, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BlancoPuro)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .border(1.5.dp, TintaComic, RoundedCornerShape(8.dp))
                                .background(AmarilloMostaza, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = TintaComic
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = instruction,
                            fontSize = 13.sp,
                            color = TintaComic,
                            lineHeight = 19.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Receta obtenida de dummyjson.com. Los datos son propiedad de la comunidad.",
            fontSize = 10.sp,
            color = TintaComic.copy(alpha = 0.55f)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun InfoPill(text: String) {
    Box(
        modifier = Modifier
            .border(1.5.dp, TintaComic, RoundedCornerShape(20.dp))
            .background(BlancoPuro)
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TintaComic
        )
    }
}
