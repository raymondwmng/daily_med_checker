package com.raymondwmng.dailymedchecker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raymondwmng.dailymedchecker.R
import com.raymondwmng.dailymedchecker.model.CapsuleSlot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Renders a realistic large metallic blister pack of capsules.
 * Untaken slots show a 3D two-tone capsule inside a clear blister bubble;
 * taken slots show an empty popped foil cavity.
 */
@Composable
fun BlisterPackView(
    packNumber: Int,
    slots: List<CapsuleSlot>,
    onSlotClick: (CapsuleSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    val columns = when {
        slots.size <= 4 -> 2
        slots.size <= 12 -> 3
        slots.size <= 24 -> 4
        else -> 5
    }
    val rows = slots.chunked(columns)

    val foilBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFE2E8F0),
            Color(0xFFF8FAFC),
            Color(0xFFCBD5E1),
            Color(0xFFE2E8F0),
            Color(0xFF94A3B8)
        ),
        start = Offset(0f, 0f),
        end = Offset(900f, 1200f)
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color(0xFF334155),
                spotColor = Color(0xFF1E293B)
            ),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = foilBrush, shape = RoundedCornerShape(28.dp))
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White, Color(0xFF64748B))
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top embossed foil header strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.blister_header_pack, packNumber, slots.size),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = stringResource(R.string.blister_header_hint),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B),
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Grid of capsule cavities with perforated foil dividers
                rows.forEachIndexed { rowIndex, rowSlots ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowSlots.forEach { slot ->
                            CapsuleCavityItem(
                                slot = slot,
                                compact = columns >= 4,
                                onClick = { onSlotClick(slot) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Fill remaining empty grid cells in the final row to preserve alignment
                        val emptyCells = columns - rowSlots.size
                        repeat(emptyCells) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                    if (rowIndex < rows.lastIndex) {
                        PerforatedDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun PerforatedDivider() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .height(2.dp)
    ) {
        drawLine(
            color = Color(0xFF94A3B8).copy(alpha = 0.55f),
            start = Offset(8f, size.height / 2f),
            end = Offset(size.width - 8f, size.height / 2f),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
        )
    }
}

@Composable
private fun CapsuleCavityItem(
    slot: CapsuleSlot,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val capsuleAlpha by animateFloatAsState(
        targetValue = if (slot.isTaken) 0f else 1f,
        animationSpec = tween(durationMillis = 250),
        label = "capsuleAlpha"
    )

    val capsuleWidth = if (compact) 34.dp else 42.dp
    val capsuleHeight = if (compact) 68.dp else 82.dp

    val statusDesc = if (slot.isTaken) {
        stringResource(R.string.cd_slot_taken, slot.slotNumber)
    } else {
        stringResource(R.string.cd_slot_available, slot.slotNumber)
    }

    val cavityBgBrush = if (slot.isTaken) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF94A3B8).copy(alpha = 0.45f),
                Color(0xFFCBD5E1).copy(alpha = 0.6f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.85f),
                Color(0xFFE2E8F0).copy(alpha = 0.9f)
            )
        )
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(brush = cavityBgBrush, shape = RoundedCornerShape(20.dp))
            .border(
                width = 1.5.dp,
                color = if (slot.isTaken) Color(0xFF94A3B8) else Color.White,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                role = Role.Button,
                onClick = onClick
            )
            .semantics { contentDescription = statusDesc }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Slot number badge
        Text(
            text = "#${slot.slotNumber}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (slot.isTaken) Color(0xFF64748B) else Color(0xFF1E293B)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Blister cavity + Capsule drawing
        Box(
            modifier = Modifier.size(width = capsuleWidth + 16.dp, height = capsuleHeight + 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.size(width = capsuleWidth, height = capsuleHeight)
            ) {
                val w = size.width
                val h = size.height
                val cornerRadius = CornerRadius(w / 2f, w / 2f)

                if (slot.isTaken) {
                    // Recessed / popped empty foil cavity
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF475569).copy(alpha = 0.35f),
                                Color(0xFF64748B).copy(alpha = 0.20f),
                                Color(0xFF94A3B8).copy(alpha = 0.35f)
                            )
                        ),
                        size = Size(w, h),
                        cornerRadius = cornerRadius
                    )
                    // Inner recessed shadow rim
                    drawRoundRect(
                        color = Color(0xFF475569).copy(alpha = 0.55f),
                        size = Size(w, h),
                        cornerRadius = cornerRadius,
                        style = Stroke(
                            width = 2.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f), 0f)
                        )
                    )
                    // Torn foil crease lines inside empty cavity
                    drawLine(
                        color = Color(0xFF475569).copy(alpha = 0.35f),
                        start = Offset(w * 0.28f, h * 0.25f),
                        end = Offset(w * 0.72f, h * 0.75f),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = Color(0xFF475569).copy(alpha = 0.35f),
                        start = Offset(w * 0.72f, h * 0.30f),
                        end = Offset(w * 0.28f, h * 0.70f),
                        strokeWidth = 2f
                    )
                }

                if (capsuleAlpha > 0.01f) {
                    // Drop shadow under untaken capsule
                    drawRoundRect(
                        color = Color(0xFF0F172A).copy(alpha = 0.22f * capsuleAlpha),
                        topLeft = Offset(3f, 5f),
                        size = Size(w, h),
                        cornerRadius = cornerRadius
                    )

                    val pillPath = Path().apply {
                        addRoundRect(
                            RoundRect(
                                rect = Rect(0f, 0f, w, h),
                                cornerRadius = cornerRadius
                            )
                        )
                    }

                    clipPath(pillPath) {
                        // Top half of capsule (Vibrant Coral / Red cap)
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFDC2626).copy(alpha = capsuleAlpha),
                                    Color(0xFFEF4444).copy(alpha = capsuleAlpha),
                                    Color(0xFFB91C1C).copy(alpha = capsuleAlpha)
                                )
                            ),
                            topLeft = Offset(0f, 0f),
                            size = Size(w, h * 0.52f)
                        )

                        // Bottom half of capsule (Warm Cream / White body)
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFF8FAFC).copy(alpha = capsuleAlpha),
                                    Color(0xFFFFFFFF).copy(alpha = capsuleAlpha),
                                    Color(0xFFCBD5E1).copy(alpha = capsuleAlpha)
                                )
                            ),
                            topLeft = Offset(0f, h * 0.50f),
                            size = Size(w, h * 0.50f)
                        )

                        // Center seam band where cap joins body
                        drawRect(
                            color = Color(0xFF991B1B).copy(alpha = 0.45f * capsuleAlpha),
                            topLeft = Offset(0f, h * 0.49f),
                            size = Size(w, h * 0.035f)
                        )

                        // Glossy 3D vertical specular highlight on left side of capsule
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.55f * capsuleAlpha),
                            topLeft = Offset(w * 0.20f, h * 0.10f),
                            size = Size(w * 0.16f, h * 0.80f),
                            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
                        )
                    }

                    // Plastic blister dome outline around capsule
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.8f * capsuleAlpha),
                        topLeft = Offset(-3f, -3f),
                        size = Size(w + 6f, h + 6f),
                        cornerRadius = CornerRadius((w + 6f) / 2f, (w + 6f) / 2f),
                        style = Stroke(width = 2f)
                    )
                }
            }

            if (slot.isTaken) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = Color(0xFF0F766E).copy(alpha = 0.85f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Bottom caption: short taken time/date or "Untaken"
        val caption = if (slot.isTaken && slot.takenAtMillis != null) {
            formatShortSlotStamp(slot.takenAtMillis)
        } else {
            stringResource(R.string.slot_status_full)
        }
        Text(
            text = caption,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = if (slot.isTaken) FontWeight.SemiBold else FontWeight.Medium,
            color = if (slot.isTaken) Color(0xFF0F766E) else Color(0xFF64748B),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatShortSlotStamp(millis: Long): String {
    val sdf = SimpleDateFormat("M月d日 HH:mm", Locale("zh", "HK"))
    return sdf.format(Date(millis))
}
