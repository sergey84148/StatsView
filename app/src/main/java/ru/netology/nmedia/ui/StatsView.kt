package ru.netology.nmedia.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
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

    private var lineWidth = AndroidUtils.dp(context, 5F).toFloat()
    private var fontSize = AndroidUtils.dp(context, 40F).toFloat()
    private var colors = emptyList<Int>()

    init {
        context.withStyledAttributes(attrs, R.styleable.StatsView) {
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth)
            fontSize = getDimension(R.styleable.StatsView_fontSize, fontSize)
            val resId = getResourceId(R.styleable.StatsView_colors, 0)
            if (resId != 0) {
                colors = resources.getIntArray(resId).toList()
            }
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = lineWidth
        strokeCap = Paint.Cap.BUTT
        strokeJoin = Paint.Join.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = fontSize
        color = android.graphics.Color.BLACK
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
        if (data.isEmpty()) {
            return
        }

        val percentages = calculatePercentages()
        if (percentages.isEmpty()) {
            paint.color = colors.getOrNull(0) ?: randomColor()
            canvas.drawArc(oval, -90F, 360F, false, paint)
            drawCenteredText(canvas, "0%")
            return
        }

        var startFrom = -90F
        for ((index, fraction) in percentages.withIndex()) {
            if (fraction == 0F) continue
            val angle = 360F * fraction
            paint.color = colors.getOrNull(index) ?: randomColor()
            canvas.drawArc(oval, startFrom, angle, false, paint)
            startFrom += angle
        }

        // Исправлено: показываем 100% вместо суммы значений
        drawCenteredText(canvas, "100.00%")
    }

    private fun drawCenteredText(canvas: Canvas, text: String) {
        val textBounds = android.graphics.Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)

        canvas.drawText(
            text,
            center.x,
            center.y + (textBounds.height() / 2F),
            textPaint
        )
    }

    private fun randomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
}