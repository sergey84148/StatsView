package ru.netology.nmedia.ui

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import ru.netology.nmedia.R
import ru.netology.nmedia.util.AndroidUtils
import kotlin.math.min
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private var radius = 0F
    private var center = PointF(0F, 0F)
    private var oval = RectF(0F, 0F, 0F, 0F)

    private var lineWidth = AndroidUtils.dp(context, 20F).toFloat()
    private var fontSize = AndroidUtils.dp(context, 40F).toFloat()
    private var colors = emptyList<Int>()

    // Угол вращения (от 0 до 360)
    private var rotationAngle = 0F
        set(value) {
            field = value
            invalidate()
        }

    // Прогресс заполнения (от 0 до 1)
    private var fillProgress = 0F
        set(value) {
            field = value
            invalidate()
        }

    private val handler = Handler(Looper.getMainLooper())
    private var rotationAnim: ObjectAnimator? = null
    private var fillAnim: ObjectAnimator? = null

    init {
        context.withStyledAttributes(attrs, R.styleable.StatsView) {
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            fontSize = getDimension(R.styleable.StatsView_fontSize, fontSize)
            val resId = getResourceId(R.styleable.StatsView_colors, 0)
            if (resId != 0) {
                colors = resources.getIntArray(resId).toList()
            }
        }

        startAnimations()
    }

    private fun startAnimations() {
        // Анимация вращения: полный оборот за 2 секунды
        rotationAnim = ObjectAnimator.ofFloat(this, "rotationAngle", 0F, 360F).apply {
            duration = 2000
            interpolator = LinearInterpolator()
        }

        // Анимация заполнения: от 0 до 1 за 2 секунды
        fillAnim = ObjectAnimator.ofFloat(this, "fillProgress", 0F, 1F).apply {
            duration = 2000
        }

        // Запускаем анимации с паузой между повторами
        runAnimationWithDelay()
    }

    private fun runAnimationWithDelay() {
        // Запускаем анимации
        rotationAnim?.start()
        fillAnim?.start()

        // Через 5 секунд (2 секунды анимация + 3 секунды пауза) сбрасываем и запускаем снова
        handler.postDelayed({
            // Сбрасываем значения
            rotationAngle = 0F
            fillProgress = 0F

            // Перезапускаем анимации
            rotationAnim?.cancel()
            fillAnim?.cancel()

            rotationAnim?.start()
            fillAnim?.start()

            // Рекурсивно запускаем следующий цикл
            runAnimationWithDelay()
        }, 2500) // 5000 мс = 2 секунды анимация + 3 секунды пауза
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = fontSize
        color = Color.BLACK
    }

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    private fun calculatePercentages(): List<Float> {
        if (data.isEmpty()) return emptyList()
        val total = data.sum()
        if (total == 0F) return emptyList()
        return data.map { it / total }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth / 2
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius, center.y - radius,
            center.x + radius, center.y + radius,
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) return

        val percentages = calculatePercentages()
        if (percentages.isEmpty()) {
            paint.color = colors.getOrNull(0) ?: randomColor()
            canvas.drawArc(oval, -90F + rotationAngle, 360F * fillProgress, false, paint)
            drawCenteredText(canvas, "${(fillProgress * 100).toInt()}%")
            return
        }

        // Стартовый угол с учетом вращения
        var startFrom = -90F + rotationAngle

        for ((index, fraction) in percentages.withIndex()) {
            if (fraction == 0F) continue
            val fullSectorAngle = 360F * fraction
            // Каждый сектор заполняется пропорционально fillProgress
            val sectorToDraw = fullSectorAngle * fillProgress

            paint.color = colors.getOrNull(index) ?: randomColor()
            canvas.drawArc(oval, startFrom, sectorToDraw, false, paint)

            startFrom += fullSectorAngle
        }

        drawCenteredText(canvas, "100.00%")
    }

    private fun drawCenteredText(canvas: Canvas, text: String) {
        val textBounds = android.graphics.Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.WHITE
        }

        val padding = 30F
        canvas.drawCircle(
            center.x,
            center.y,
            textBounds.width() / 2F + padding,
            bgPaint
        )

        canvas.drawText(
            text,
            center.x,
            center.y,
            textPaint
        )
    }

    private fun randomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}