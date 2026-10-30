package com.example.chefsito.ui.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chefsito.ui.theme.*

data class SavedRecipe(
    val id: String,
    val title: String,
    val price: String,
    val time: String,
    val isFavorite: Boolean = true,
)

@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier,
    onRecipeClick: (String) -> Unit = {},
) {
    var savedRecipes by remember { mutableStateOf(emptyList<SavedRecipe>()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FondoCrema)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            item {
                Text(
                    text = "Recetas Guardadas",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TintaComic
                )
                Text(
                    text = "${savedRecipes.size} recetas guardadas",
                    fontSize = 12.sp,
                    color = TintaComic.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (savedRecipes.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .border(2.dp, TintaComic, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BlancoPuro)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                tint = TintaComic.copy(alpha = 0.4f),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Aún no tienes recetas guardadas",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TintaComic
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tus recetas favoritas aparecerán aquí.",
                                fontSize = 12.sp,
                                color = TintaComic.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else {
                items(savedRecipes, key = { it.id }) { recipe ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .border(2.dp, TintaComic, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BlancoPuro)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .border(2.dp, TintaComic, CircleShape)
                                    .clip(CircleShape)
                                    .background(AmarilloMostaza),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = recipe.title,
                                    tint = TintaComic,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = recipe.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TintaComic
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .border(1.5.dp, TintaComic, RoundedCornerShape(20.dp))
                                            .background(VerdeFresco)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = recipe.price,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BlancoPuro
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .border(1.5.dp, TintaComic, RoundedCornerShape(20.dp))
                                            .background(BlancoPuro)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = null,
                                                tint = TintaComic,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = recipe.time,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TintaComic
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                TextButton(
                                    onClick = { onRecipeClick(recipe.id) },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Ver receta",
                                            color = NaranjaCoral,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = NaranjaCoral,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = {
                                    savedRecipes = savedRecipes.filter { it.id != recipe.id }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .border(2.dp, TintaComic, CircleShape)
                                    .clip(CircleShape)
                                    .background(NaranjaCoral)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Quitar de favoritos",
                                    tint = BlancoPuro,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
