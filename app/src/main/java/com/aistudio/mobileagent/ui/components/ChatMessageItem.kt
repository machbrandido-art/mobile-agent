package com.aistudio.mobileagent.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.mobileagent.data.model.Message
import com.aistudio.mobileagent.data.model.MessageRole
import com.aistudio.mobileagent.data.model.MessageStatus
import com.aistudio.mobileagent.data.model.ToolCall
import com.aistudio.mobileagent.ui.theme.*

@Composable
fun ChatMessageItem(
    message: Message,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == MessageRole.USER

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .testTag(if (isUser) "user_message_${message.id}" else "assistant_message_${message.id}"),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isUser) {
            // User Message Bubble (Material Black)
            Surface(
                color = DarkCardBlack,
                shape = RoundedCornerShape(18.dp, 4.dp, 18.dp, 18.dp),
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .border(1.dp, DarkBorderBlack, RoundedCornerShape(18.dp, 4.dp, 18.dp, 18.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = message.content,
                        color = TextWhite,
                        fontSize = 15.sp,
                        lineHeight = 21.sp
                    )
                    if (message.attachments.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            message.attachments.forEach { att ->
                                Surface(
                                    color = DarkElevatedCard,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.border(1.dp, DarkBorderBlack, RoundedCornerShape(8.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Attachment,
                                            contentDescription = "Attachment",
                                            modifier = Modifier.size(14.dp),
                                            tint = ElegantBlue
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = att, fontSize = 11.sp, color = TextLightGray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Assistant Message Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp)
            ) {
                // Header (Icon & Model / Status)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(ElegantBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Mobile Agent",
                            tint = PureBlack,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mobile Agent (OmniRoute)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextWhite
                    )
                    if (message.status == MessageStatus.STREAMING) {
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 2.dp,
                            color = ElegantBlue
                        )
                    }
                }

                // Thinking / Reasoning Block
                if (!message.thought.isNullOrBlank()) {
                    var expandedThought by remember { mutableStateOf(false) }
                    Surface(
                        color = DarkCardBlack,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .border(1.dp, DarkBorderBlack, RoundedCornerShape(10.dp))
                            .clickable { expandedThought = !expandedThought }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = "Thinking process",
                                        tint = ElegantPurple,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "OmniRoute & Agent Reasoning",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ElegantPurple
                                    )
                                }
                                Icon(
                                    imageVector = if (expandedThought) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Toggle thought",
                                    tint = TextLightGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            AnimatedVisibility(visible = expandedThought) {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    Text(
                                        text = message.thought,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextLightGray
                                    )
                                }
                            }
                        }
                    }
                }

                // Tool Calls
                if (message.toolCalls.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        message.toolCalls.forEach { tool ->
                            ToolCallCard(tool = tool)
                        }
                    }
                }

                // Main Message Content
                if (message.content.isNotBlank()) {
                    Surface(
                        color = DarkCardBlack,
                        shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkBorderBlack, RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp))
                    ) {
                        Text(
                            text = message.content,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = TextWhite,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                // Error Message if any
                if (!message.error.isNullOrBlank()) {
                    Surface(
                        color = RoseError.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .border(1.dp, RoseError.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = RoseError, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = message.error, fontSize = 12.sp, color = RoseError)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToolCallCard(tool: ToolCall) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        color = PureBlack,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorderBlack, RoundedCornerShape(10.dp))
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusIcon = when (tool.status) {
                        "success" -> Icons.Default.CheckCircle
                        "running" -> Icons.Default.Sync
                        else -> Icons.Default.Build
                    }
                    val statusColor = when (tool.status) {
                        "success" -> EmeraldSuccess
                        "running" -> AmberWarning
                        else -> ElegantBlue
                    }
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = "Tool status",
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tool.toolName,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = TextWhite
                    )
                }

                Surface(
                    color = when (tool.status) {
                        "success" -> EmeraldSuccess.copy(alpha = 0.2f)
                        "running" -> AmberWarning.copy(alpha = 0.2f)
                        else -> DarkElevatedCard
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = tool.status.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (tool.status) {
                            "success" -> EmeraldSuccess
                            "running" -> AmberWarning
                            else -> TextLightGray
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "Arguments:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextLightGray
                    )
                    Text(
                        text = tool.arguments,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextLightGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkCardBlack, RoundedCornerShape(4.dp))
                            .border(1.dp, DarkBorderBlack, RoundedCornerShape(4.dp))
                            .padding(6.dp)
                    )

                    if (!tool.result.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Result:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextLightGray
                        )
                        Text(
                            text = tool.result,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = EmeraldSuccess,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkCardBlack, RoundedCornerShape(4.dp))
                                .border(1.dp, DarkBorderBlack, RoundedCornerShape(4.dp))
                                .padding(6.dp)
                        )
                    }
                }
            }
        }
    }
}
