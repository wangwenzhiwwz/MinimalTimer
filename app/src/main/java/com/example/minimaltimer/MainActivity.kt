package com.example.minimaltimer

import android.content.Context
import android.content.res.Configuration
import android.os.*
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.minimaltimer.ui.theme.MinimalTimerTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 物理常亮：作为摆件时钟的核心
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        enableEdgeToEdge()
        setContent {
            MinimalTimerTheme {
                HideSystemBars() // 沉浸式隐藏小白条
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFF050506)
                ) { innerPadding ->
                    FliqloScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun HideSystemBars() {
    val view = LocalView.current
    val window = (view.context as ComponentActivity).window
    SideEffect {
        val controller = WindowCompat.getInsetsController(window, view)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}

/**
 * 强震动执行器：双重保障，确保在各版本 Android 上都有震感
 */
fun performStrongVibration(context: Context, type: String) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (vibrator.hasVibrator()) {
            val effect = when (type) {
                // 提高点击震感：使用最大振幅（255）
                "CLICK" -> VibrationEffect.createOneShot(60, 255)
                "LONG_PRESS" -> VibrationEffect.createOneShot(200, 255)
                else -> null
            }
            effect?.let { vibrator.vibrate(it) }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun FliqloScreen(modifier: Modifier = Modifier) {
    var isRunning by rememberSaveable { mutableStateOf(false) }
    var timeInSeconds by rememberSaveable { mutableStateOf(0L) }
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current // 获取系统的触感反馈接口
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000)
            timeInSeconds++
        }
    }

    val hours = String.format("%02d", (timeInSeconds / 3600).toInt())
    val minutes = String.format("%02d", ((timeInSeconds % 3600) / 60).toInt())
    val seconds = String.format("%02d", (timeInSeconds % 60).toInt())

    Box(
        modifier = modifier
            .fillMaxSize()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    // 调用强震
                    performStrongVibration(context, "CLICK")
                    // 同时尝试系统级触感反馈作为补充
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    isRunning = !isRunning
                },
                onLongClick = {
                    performStrongVibration(context, "LONG_PRESS")
                    isRunning = false
                    timeInSeconds = 0L
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLandscape) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val lWidth = 240.dp
                val lHeight = 220.dp
                val lFontSize = 180.sp
                FliqloCard(hours, lWidth, lHeight, lFontSize)
                Spacer(modifier = Modifier.width(12.dp))
                FliqloCard(minutes, lWidth, lHeight, lFontSize)
                Spacer(modifier = Modifier.width(12.dp))
                FliqloCard(seconds, lWidth, lHeight, lFontSize)
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val pWidth = 260.dp
                val pHeight = 200.dp
                val pFontSize = 160.sp
                FliqloCard(hours, pWidth, pHeight, pFontSize)
                Spacer(modifier = Modifier.height(20.dp))
                FliqloCard(minutes, pWidth, pHeight, pFontSize)
                Spacer(modifier = Modifier.height(20.dp))
                FliqloCard(seconds, pWidth, pHeight, pFontSize)
            }
        }
    }
}

@Composable
fun FliqloCard(text: String, width: Dp, height: Dp, fontSize: TextUnit) {
    var currentText by remember { mutableStateOf(text) }
    var nextText by remember { mutableStateOf(text) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(text) {
        if (text != currentText) {
            nextText = text
            rotation.animateTo(180f, tween(500, easing = FastOutSlowInEasing))
            currentText = nextText
            rotation.snapTo(0f)
        }
    }

    Box(modifier = Modifier.size(width, height)) {
        DigitHalfCanvas(nextText, true, fontSize)
        DigitHalfCanvas(currentText, false, fontSize)

        if (rotation.value <= 90f) {
            DigitHalfCanvas(currentText, true, fontSize, -rotation.value, rotation.value / 90f)
        } else {
            DigitHalfCanvas(nextText, false, fontSize, 180f - rotation.value, (180f - rotation.value) / 90f)
        }

        Box(modifier = Modifier.align(Alignment.Center).fillMaxWidth().height(1.8.dp).background(Color(0xFF050505)))
    }
}

@Composable
fun DigitHalfCanvas(
    text: String,
    isTop: Boolean,
    fontSize: TextUnit,
    rotationX: Float = 0f,
    shadowAlpha: Float = 0f
) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF232326), Color(0xFF1B1B1D))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                this.rotationX = rotationX
                cameraDistance = 16f * density
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            }
            .drawWithContent {
                clipRect(
                    top = if (isTop) 0f else size.height / 2f,
                    bottom = if (isTop) size.height / 2f else size.height
                ) {
                    this@drawWithContent.drawContent()
                }
            }
            .background(gradientBrush, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(0xFFF0F0F5),
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = (-5).sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false
        )
        if (shadowAlpha > 0f) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = shadowAlpha * 0.5f)))
        }
    }
}