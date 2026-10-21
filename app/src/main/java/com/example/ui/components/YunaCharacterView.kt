package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPinkGlow
import com.example.voice.VoiceState
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun YunaCharacterView(
    voiceState: VoiceState,
    audioAmplitude: Float,
    personality: String,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    // Idle breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "idle_breathing")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (reducedMotion) 6000 else 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    // Holographic scanner line vertical progress
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (reducedMotion) 8000 else 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanner"
    )

    // Cyber particle oscillation
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particles"
    )

    // Eye blinking trigger
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(reducedMotion) {
        if (!reducedMotion) {
            while (true) {
                delay(Random.nextLong(2500, 5000))
                isBlinking = true
                delay(150)
                isBlinking = false
            }
        }
    }

    // Lip sync amplitude smoothing
    val smoothLipSync = remember { Animatable(0f) }
    LaunchedEffect(audioAmplitude, voiceState) {
        val target = if (voiceState == VoiceState.SPEAKING) audioAmplitude else 0f
        smoothLipSync.animateTo(target, tween(60))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF131728),
                        DarkBackground
                    ),
                    radius = 500f
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        NeonPinkGlow,
                        ElectricBlueGlow,
                        Color.Transparent
                    )
                ),
                RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Ambient cyberpunk particles and grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Subtle cyber rain / particle lines
            val particleCount = if (reducedMotion) 12 else 24
            for (i in 0 until particleCount) {
                val seed = i * 47.3f
                val px = ((seed * 19) % width)
                val speed = 0.5f + (i % 3) * 0.4f
                val py = ((seed + particlePhase * 60f * speed) % height)
                val color = if (i % 2 == 0) ElectricBlue.copy(alpha = 0.25f) else NeonPink.copy(alpha = 0.25f)

                drawLine(
                    color = color,
                    start = Offset(px, py),
                    end = Offset(px, py + 8.dp.toPx()),
                    strokeWidth = 1.5.dp.toPx()
                )
            }

            // Circular holographic aura pulse when speaking/listening
            val pulseRadius = when (voiceState) {
                VoiceState.SPEAKING -> 120.dp.toPx() + (audioAmplitude * 35.dp.toPx())
                VoiceState.LISTENING -> 115.dp.toPx() + (sin(particlePhase * 2) * 10.dp.toPx())
                else -> 105.dp.toPx()
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (voiceState == VoiceState.SPEAKING) NeonPink.copy(alpha = 0.25f)
                        else if (voiceState == VoiceState.LISTENING) ElectricBlue.copy(alpha = 0.35f)
                        else Color.Transparent,
                        Color.Transparent
                    ),
                    radius = pulseRadius + 40.dp.toPx(),
                    center = center
                ),
                radius = pulseRadius + 40.dp.toPx(),
                center = center
            )

            // Outer reactive neon ring
            if (voiceState == VoiceState.LISTENING || voiceState == VoiceState.SPEAKING) {
                drawCircle(
                    color = if (voiceState == VoiceState.SPEAKING) NeonPink else ElectricBlue,
                    radius = pulseRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Avatar Core Frame with Breathing scale
        Box(
            modifier = Modifier
                .size(190.dp)
                .scale(breathingScale)
                .shadow(24.dp, CircleShape, spotColor = NeonPink, ambientColor = ElectricBlue)
                .clip(CircleShape)
                .border(2.5.dp, Brush.sweepGradient(listOf(NeonPink, ElectricBlue, NeonPink)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.yuna_character_1792551155920),
                contentDescription = "YUNA AI Korean Cyber Assistant",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dynamic Eye Blinking & Lip Sync Canvas Overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f

                // Eye blinking eyelid cover
                if (isBlinking) {
                    val eyeY = cy * 0.76f
                    val leftEyeX = cx * 0.88f
                    val rightEyeX = cx * 1.12f
                    // Eyelid slit
                    drawLine(
                        color = Color(0xFF1E1A29),
                        start = Offset(leftEyeX - 10.dp.toPx(), eyeY),
                        end = Offset(leftEyeX + 10.dp.toPx(), eyeY),
                        strokeWidth = 3.dp.toPx()
                    )
                    drawLine(
                        color = Color(0xFF1E1A29),
                        start = Offset(rightEyeX - 10.dp.toPx(), eyeY),
                        end = Offset(rightEyeX + 10.dp.toPx(), eyeY),
                        strokeWidth = 3.dp.toPx()
                    )
                }

                // Lip Sync Mouth movement when speaking
                if (voiceState == VoiceState.SPEAKING && smoothLipSync.value > 0.05f) {
                    val mouthY = cy * 1.25f
                    val mouthHeight = 4.dp.toPx() + (smoothLipSync.value * 9.dp.toPx())
                    val mouthWidth = 12.dp.toPx()

                    drawOval(
                        color = Color(0xFF38101E),
                        topLeft = Offset(cx - (mouthWidth / 2f), mouthY - (mouthHeight / 2f)),
                        size = androidx.compose.ui.geometry.Size(mouthWidth, mouthHeight)
                    )
                    // Lip glow outline
                    drawOval(
                        color = NeonPink.copy(alpha = 0.7f),
                        topLeft = Offset(cx - (mouthWidth / 2f), mouthY - (mouthHeight / 2f)),
                        size = androidx.compose.ui.geometry.Size(mouthWidth, mouthHeight),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Holographic HUD scanline
                val scanY = size.height * scanLineY
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            ElectricBlue.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, scanY),
                    end = Offset(size.width, scanY),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
        }

        // Status Badge at top right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xDD0C0F1A))
                .border(1.dp, if (voiceState == VoiceState.LISTENING) ElectricBlue else NeonPink, RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            val statusLabel = when (voiceState) {
                VoiceState.LISTENING -> "● LISTENING..."
                VoiceState.PROCESSING -> "● THINKING..."
                VoiceState.SPEAKING -> "● SPEAKING..."
                VoiceState.IDLE -> "● ONLINE ($personality)"
            }
            val statusColor = when (voiceState) {
                VoiceState.LISTENING -> ElectricBlue
                VoiceState.PROCESSING -> Color(0xFFFBBF24)
                VoiceState.SPEAKING -> NeonPink
                VoiceState.IDLE -> Color(0xFF10B981)
            }
            Text(
                text = statusLabel,
                color = statusColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Live Voice Waveform Bar at bottom
        if (voiceState == VoiceState.LISTENING || voiceState == VoiceState.SPEAKING) {
            VoiceWaveformBar(
                audioAmplitude = audioAmplitude,
                voiceState = voiceState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
fun VoiceWaveformBar(
    audioAmplitude: Float,
    voiceState: VoiceState,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .size(width = 160.dp, height = 24.dp)
    ) {
        val barCount = 13
        val spacing = size.width / (barCount - 1)
        val mid = barCount / 2

        for (i in 0 until barCount) {
            val dist = kotlin.math.abs(i - mid).toFloat()
            val factor = (1f - (dist / mid)).coerceIn(0.2f, 1f)
            val barHeight = (4.dp.toPx() + (audioAmplitude * 18.dp.toPx() * factor)).coerceIn(3.dp.toPx(), 22.dp.toPx())
            val x = i * spacing
            val top = (size.height - barHeight) / 2f
            val color = if (voiceState == VoiceState.SPEAKING) NeonPink else ElectricBlue

            drawLine(
                color = color,
                start = Offset(x, top),
                end = Offset(x, top + barHeight),
                strokeWidth = 3.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}
