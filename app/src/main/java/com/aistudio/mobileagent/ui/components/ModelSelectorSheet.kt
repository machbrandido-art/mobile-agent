package com.aistudio.mobileagent.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.mobileagent.data.model.AiModelInfo
import com.aistudio.mobileagent.data.repository.AppRepository
import com.aistudio.mobileagent.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectorSheet(
    selectedModelId: String,
    onSelectModel: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PureBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Select AI Model",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Text(
                text = "Choose between local on-device neural weights or cloud endpoints",
                fontSize = 13.sp,
                color = TextLightGray,
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                items(AppRepository.availableModels) { model ->
                    val isSelected = model.id == selectedModelId

                    Surface(
                        color = if (isSelected) DarkElevatedCard else DarkCardBlack,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) ElegantBlue else DarkBorderBlack,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onSelectModel(model.id)
                                onDismiss()
                            }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (model.isLocal) Icons.Default.Memory else Icons.Default.Cloud,
                                        contentDescription = null,
                                        tint = if (model.isLocal) EmeraldSuccess else ElegantBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = model.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextWhite
                                    )
                                }

                                Surface(
                                    color = if (model.isLocal) EmeraldSuccess.copy(alpha = 0.2f) else ElegantBlue.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (model.isLocal) "ON-DEVICE" else "CLOUD",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (model.isLocal) EmeraldSuccess else ElegantBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = model.description,
                                fontSize = 12.sp,
                                color = TextLightGray,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BadgeChip(label = "Params: ${model.parameterCount}")
                                BadgeChip(label = "Ctx: ${model.contextWindow / 1000}k")
                                if (model.supportsReasoning) {
                                    BadgeChip(label = "Reasoning", highlight = true)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeChip(label: String, highlight: Boolean = false) {
    Surface(
        color = if (highlight) ElegantPurple.copy(alpha = 0.2f) else DarkElevatedCard,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.border(1.dp, if (highlight) ElegantPurple.copy(alpha = 0.5f) else DarkBorderBlack, RoundedCornerShape(4.dp))
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (highlight) ElegantPurple else TextLightGray,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
