package com.facemoji.cut.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.FrameLayout
import com.facemoji.cut.R

/**
 * 圆角FrameLayout
 *
 * @author jzhou
 */
class RoundFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    private var topLeftRadius = 0f
    private var topRightRadius = 0f
    private var bottomLeftRadius = 0f
    private var bottomRightRadius = 0f
    private val roundPaint: Paint
    private val imagePaint: Paint

    //实现4
    override fun dispatchDraw(canvas: Canvas) {
        canvas.saveLayer(
            RectF(0F, 0F, canvas.width.toFloat(), canvas.height.toFloat()),
            imagePaint,
            Canvas.ALL_SAVE_FLAG
        )
        super.dispatchDraw(canvas)
        drawTopLeft(canvas)
        drawTopRight(canvas)
        drawBottomLeft(canvas)
        drawBottomRight(canvas)
        canvas.restore()
    }

    private fun drawTopLeft(canvas: Canvas) {
        if (topLeftRadius > 0) {
            val path = Path()
            path.moveTo(0f, topLeftRadius)
            path.lineTo(0f, 0f)
            path.lineTo(topLeftRadius, 0f)
            path.arcTo(RectF(0F, 0F, topLeftRadius * 2, topLeftRadius * 2), -90f, -90f)
            path.close()
            canvas.drawPath(path, roundPaint)
        }
    }

    private fun drawTopRight(canvas: Canvas) {
        if (topRightRadius > 0) {
            val width = width
            val path = Path()
            path.moveTo(width - topRightRadius, 0f)
            path.lineTo(width.toFloat(), 0f)
            path.lineTo(width.toFloat(), topRightRadius)
            path.arcTo(
                RectF(width - 2 * topRightRadius, 0f, width.toFloat(), topRightRadius * 2),
                0f,
                -90f
            )
            path.close()
            canvas.drawPath(path, roundPaint)
        }
    }

    private fun drawBottomLeft(canvas: Canvas) {
        if (bottomLeftRadius > 0) {
            val height = height
            val path = Path()
            path.moveTo(0f, height - bottomLeftRadius)
            path.lineTo(0f, height.toFloat())
            path.lineTo(bottomLeftRadius, height.toFloat())
            path.arcTo(
                RectF(
                    0f, height - 2 * bottomLeftRadius,
                    bottomLeftRadius * 2, height.toFloat()
                ), 90f, 90f
            )
            path.close()
            canvas.drawPath(path, roundPaint)
        }
    }

    private fun drawBottomRight(canvas: Canvas) {
        if (bottomRightRadius > 0) {
            val height = height
            val width = width
            val path = Path()
            path.moveTo(width - bottomRightRadius, height.toFloat())
            path.lineTo(width.toFloat(), height.toFloat())
            path.lineTo(width.toFloat(), height - bottomRightRadius)
            path.arcTo(
                RectF(
                    width - 2 * bottomRightRadius, height - 2
                            * bottomRightRadius, width.toFloat(), height.toFloat()
                ), 0f, 90f
            )
            path.close()
            canvas.drawPath(path, roundPaint)
        }
    }

    init {
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.RoundFrameLayout)
            val radius = ta.getDimension(R.styleable.RoundFrameLayout_radius, 0f)
            topLeftRadius = ta.getDimension(R.styleable.RoundFrameLayout_topLeftRadius, radius)
            topRightRadius = ta.getDimension(R.styleable.RoundFrameLayout_topRightRadius, radius)
            bottomLeftRadius =
                ta.getDimension(R.styleable.RoundFrameLayout_bottomLeftRadius, radius)
            bottomRightRadius =
                ta.getDimension(R.styleable.RoundFrameLayout_bottomRightRadius, radius)
            ta.recycle()
        }
        roundPaint = Paint()
        roundPaint.color = Color.WHITE
        roundPaint.isAntiAlias = true
        roundPaint.style = Paint.Style.FILL
        roundPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        imagePaint = Paint()
        imagePaint.xfermode = null
    }
}