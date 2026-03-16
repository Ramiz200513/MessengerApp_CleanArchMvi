package com.example.messengerapp.presentation.auth.chat.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.domain.domain.model.ChatWithPartner

@Composable
fun ChatItem(
    item: ChatWithPartner,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onAvatarClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val partner = item.partner
    val isFavorite = item.chat.isFavorite
    val isOnline = partner?.online == true

    val hasUnreadMessages = item.hasUnreadMessages
    Column(modifier = modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- 1. АВАТАРКА С ИНДИКАТОРОМ ОНЛАЙН ---
            Box(modifier = Modifier.size(56.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { partner?.id?.let { onAvatarClick(it) } },
                    contentAlignment = Alignment.Center
                ) {
                    if (partner?.photoUrl.isNullOrBlank()) {
                        Text(
                            text = (partner?.username ?: "U").take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge
                        )
                    } else {
                        AsyncImage(
                            model = partner?.photoUrl,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // ЗЕЛЕНЫЙ КРУЖОК ОНЛАЙНА
                if (isOnline) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(14.dp)
                            .offset(x = (-2).dp, y = (-2).dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50)) // Зеленый цвет
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // --- 2. ТЕКСТ (Имя и Сообщение) ---
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = partner?.username ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (hasUnreadMessages) FontWeight.Bold else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hasUnreadMessages) MaterialTheme.colorScheme.onSurface else Color.Gray,
                    fontWeight = if (hasUnreadMessages) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // --- 3. ПРАВАЯ ЧАСТЬ (Избранное + Синий кружок) ---
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                // ОБНОВЛЕННЫЙ ДИЗАЙН ИЗБРАННОГО
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(32.dp) // Сделали кнопку компактнее
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFFFB300) else Color.LightGray.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // СИНИЙ КРУЖОК (Непрочитанное сообщение)
                if (hasUnreadMessages) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary) // Синий цвет темы
                    )
                } else {
                    Spacer(modifier = Modifier.size(10.dp)) // Чтобы текст не прыгал
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(start = 88.dp),
            thickness = 0.5.dp,
            color = Color.LightGray.copy(alpha = 0.3f)
        )
    }
}