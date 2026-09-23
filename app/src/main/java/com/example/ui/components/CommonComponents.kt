package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color Palette matching screenshots
val MetricBlueStart = Color(0xFF2563EB)
val MetricBlueEnd = Color(0xFF3B82F6)

val MetricPurpleStart = Color(0xFF9333EA)
val MetricPurpleEnd = Color(0xFFA855F7)

val MetricGreenStart = Color(0xFF059669)
val MetricGreenEnd = Color(0xFF10B981)

val MetricOrangeStart = Color(0xFFEA580C)
val MetricOrangeEnd = Color(0xFFF97316)

val MetricVioletStart = Color(0xFF7C3AED)
val MetricVioletEnd = Color(0xFF8B5CF6)

val SoftBgColor = Color(0xFFF8FAFC)
val CardBorderColor = Color(0xFFE2E8F0)

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "MetricScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(115.dp)
            .scale(scale)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick?.invoke()
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(gradientColors))
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CircularPaidStamp(
    dateRange: String,
    modifier: Modifier = Modifier
) {
    var hasAppeared by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        hasAppeared = true
        kotlinx.coroutines.delay(120)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val stampScale by animateFloatAsState(
        targetValue = if (hasAppeared) 1f else 1.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "StampSlamScale"
    )

    val stampRotation by animateFloatAsState(
        targetValue = if (hasAppeared) -12f else 10f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "StampSlamRotation"
    )

    Box(
        modifier = modifier
            .size(72.dp)
            .scale(stampScale)
            .rotate(stampRotation)
            .border(2.5.dp, Color(0xFF16A34A), CircleShape)
            .background(Color(0xFFDCFCE7).copy(alpha = 0.85f), CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "PAID",
                color = Color(0xFF15803D),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            if (dateRange.isNotEmpty()) {
                Text(
                    text = dateRange.take(10),
                    color = Color(0xFF166534),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun TopFilterPanel(
    fromDate: String,
    toDate: String,
    technicianFilter: String,
    technicianList: List<String>,
    onFromDateChange: (String) -> Unit,
    onToDateChange: (String) -> Unit,
    onTechnicianChange: (String) -> Unit,
    showTechnicianFilter: Boolean = true,
    modifier: Modifier = Modifier
) {
    var techDropdownExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Filter:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )

                // From Date Field
                OutlinedTextField(
                    value = fromDate,
                    onValueChange = onFromDateChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("From (YYYY-MM-DD)", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "From",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF64748B)
                        )
                    },
                    trailingIcon = {
                        if (fromDate.isNotEmpty()) {
                            IconButton(onClick = { onFromDateChange("") }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MetricBlueStart,
                        unfocusedBorderColor = CardBorderColor
                    )
                )

                // To Date Field
                OutlinedTextField(
                    value = toDate,
                    onValueChange = onToDateChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("To (YYYY-MM-DD)", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "To",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF64748B)
                        )
                    },
                    trailingIcon = {
                        if (toDate.isNotEmpty()) {
                            IconButton(onClick = { onToDateChange("") }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MetricBlueStart,
                        unfocusedBorderColor = CardBorderColor
                    )
                )
            }

            if (showTechnicianFilter) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { techDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFF8FAFC)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFF475569)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = technicianFilter,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1E293B)
                                )
                            }
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFF64748B)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = techDropdownExpanded,
                        onDismissRequest = { techDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All technicians") },
                            onClick = {
                                onTechnicianChange("All technicians")
                                techDropdownExpanded = false
                            }
                        )
                        technicianList.forEach { techName ->
                            DropdownMenuItem(
                                text = { Text(techName) },
                                onClick = {
                                    onTechnicianChange(techName)
                                    techDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleBarChart(
    title: String,
    dataPoints: List<Pair<String, Double>>,
    highlightLabel: String,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val maxValue = (dataPoints.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(1.0)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1E293B)
                )
                if (highlightLabel.isNotEmpty()) {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = highlightLabel,
                            color = Color(0xFF15803D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (dataPoints.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No sales data recorded for this period",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    dataPoints.forEach { (label, value) ->
                        val ratio = (value / maxValue).toFloat().coerceIn(0.05f, 1f)
                        val animatedRatio by animateFloatAsState(
                            targetValue = ratio,
                            animationSpec = tween(durationMillis = 600),
                            label = "barHeight"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "₹${value.toInt()}",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(26.dp)
                                    .fillMaxHeight(animatedRatio)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(barColor, barColor.copy(alpha = 0.7f))
                                        )
                                    )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF475569),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
