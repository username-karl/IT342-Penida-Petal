package com.petal.ui.messaging

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.petal.data.messaging.ConversationResponse
import com.petal.data.messaging.MessageResponse
import com.petal.ui.components.PetalHeader
import com.petal.ui.components.PetalPrimaryButton
import com.petal.ui.components.PetalSecondaryButton
import com.petal.ui.components.PetalTextField
import com.petal.ui.theme.*

@Composable
fun InboxScreen(
    viewModel: MessagingViewModel,
    onBack: () -> Unit,
    onConversation: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadConversations()
    }
    
    Column(Modifier.fillMaxSize().background(Paper)) {
        PetalHeader(
            title = "Messages", 
            subtitle = "Conversations with your florists", 
            action = { PetalSecondaryButton("Back", onBack) }
        )
        
        if (state.isLoading && state.conversations.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading conversations...", color = Stone500)
            }
        } else if (state.error != null && state.conversations.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error ?: "Error loading conversations", color = Stone500)
            }
        } else if (state.conversations.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active conversations.", color = Stone500)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.conversations) { conversation ->
                    ConversationCard(conversation = conversation, onClick = { onConversation(conversation.id) })
                }
            }
        }
    }
}

@Composable
fun ConversationCard(conversation: ConversationResponse, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(SurfaceWarm, RoundedCornerShape(8.dp))
            .border(1.dp, Stone200, RoundedCornerShape(8.dp))
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = conversation.floristName ?: "Florist",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Stone950
            )
            if ((conversation.unreadCount ?: 0) > 0) {
                Text(
                    text = "${conversation.unreadCount} new",
                    color = Stone950,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .background(BlushSoft, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Order #${conversation.orderId}",
            style = MaterialTheme.typography.labelMedium,
            color = Stone700
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = conversation.lastMessagePreview ?: "No messages yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = Stone500
        )
    }
}

@Composable
fun ChatScreen(
    conversationId: Long,
    viewModel: MessagingViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var messageText by remember { mutableStateOf("") }
    
    LaunchedEffect(conversationId) {
        viewModel.startPollingMessages(conversationId)
    }
    
    DisposableEffect(conversationId) {
        onDispose {
            viewModel.stopPollingMessages()
        }
    }
    
    Column(Modifier.fillMaxSize().background(Paper)) {
        PetalHeader(
            title = "Conversation", 
            subtitle = "Order notes & updates", 
            action = { PetalSecondaryButton("Back", onBack) }
        )
        
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(state.messages) { message ->
                MessageNoteCard(message)
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWarm)
                .border(1.dp, Stone200)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    PetalTextField(
                        label = "Type your message...",
                        value = messageText,
                        onValueChange = { messageText = it },
                        singleLine = false
                    )
                }
                PetalPrimaryButton(
                    text = "Send",
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(conversationId, messageText)
                            messageText = ""
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MessageNoteCard(message: MessageResponse) {
    val isBuyer = message.senderType == "buyer"
    val bgColor = if (isBuyer) SageSoft else BlushSoft
    val borderColor = Stone200
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(4.dp))
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isBuyer) "You" else "Florist",
                style = MaterialTheme.typography.labelSmall,
                color = Stone700,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message.createdAt?.take(10) ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = Stone500
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = message.content,
            style = MaterialTheme.typography.bodyMedium,
            color = Stone950
        )
    }
}
