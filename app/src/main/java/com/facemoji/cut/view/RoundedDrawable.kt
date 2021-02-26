package com.facemoji.cut.view

import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.Shader.TileMode
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.NinePatchDrawable
import android.util.Log
import android.widget.ImageView.ScaleType
import androidx.annotation.ColorInt
import java.util.*
import kotlin.math.min

class RoundedDrawable(private val sourceBitmap: Bitmap) : Drawable() {
    private val mBounds = RectF()
    private val mDrawableRect = RectF()
    private val mBitmapRect = RectF()
    private val mBitmapPaint: Paint
    private val mBitmapWidth: Int = 0
    private val mBitmapHeight: Int = 0
    private val mBorderRect = RectF()
    private val mBorderPaint: Paint
    private val mShaderMatrix = Matrix()
    private val mSquareCornersRect = RectF()
    var tileModeX = TileMode.CLAMP
        private set
    var tileModeY = TileMode.CLAMP
        private set
    private var mRebuildShader = true

    /**
     * @return 角度半径
     */
    var cornerRadius = 0f
        private set

    // [ topLeft, topRight, bottomLeft, bottomRight ]
    private val mCornersRounded = booleanArrayOf(true, true, true, true)
    var isOval = false
        private set
    var borderWidth = 0f
        private set
    var borderColors =
        ColorStateList.valueOf(DEFAULT_BORDER_COLOR)
        private set
    var scaleType = ScaleType.FIT_CENTER
        private set

    override fun isStateful(): Boolean = borderColors.isStateful

    override fun onStateChange(state: IntArray): Boolean {
        val newColor = borderColors.getColorForState(state, 0)
        return if (mBorderPaint.color != newColor) {
            mBorderPaint.color = newColor
            true
        } else super.onStateChange(state)
    }

    private fun updateShaderMatrix() {
        val scale: Float
        var dx: Float
        var dy: Float
        when (scaleType) {
            ScaleType.CENTER -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset(borderWidth / 2, borderWidth / 2)
                mShaderMatrix.reset()
                mShaderMatrix.setTranslate(
                    ((mBorderRect.width() - mBitmapWidth) * 0.5f + 0.5f),
                    ((mBorderRect.height() - mBitmapHeight) * 0.5f + 0.5f)
                )
            }
            ScaleType.CENTER_CROP -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset(borderWidth / 2, borderWidth / 2)
                mShaderMatrix.reset()
                dx = 0f
                dy = 0f
                if (mBitmapWidth * mBorderRect.height() > mBorderRect.width() * mBitmapHeight) {
                    scale = mBorderRect.height() / mBitmapHeight.toFloat()
                    dx = (mBorderRect.width() - mBitmapWidth * scale) * 0.5f
                } else {
                    scale = mBorderRect.width() / mBitmapWidth.toFloat()
                    dy = (mBorderRect.height() - mBitmapHeight * scale) * 0.5f
                }
                mShaderMatrix.setScale(scale, scale)
                mShaderMatrix.postTranslate(
                    (dx + 0.5f).toInt() + borderWidth / 2,
                    (dy + 0.5f).toInt() + borderWidth / 2
                )
            }
            ScaleType.CENTER_INSIDE -> {
                mShaderMatrix.reset()
                scale =
                    if (mBitmapWidth <= mBounds.width() && mBitmapHeight <= mBounds.height()) 1.0f
                    else {
                        min(
                            mBounds.width() / mBitmapWidth.toFloat(),
                            mBounds.height() / mBitmapHeight.toFloat()
                        )
                    }
                dx = ((mBounds.width() - mBitmapWidth * scale) * 0.5f + 0.5f)
                dy = ((mBounds.height() - mBitmapHeight * scale) * 0.5f + 0.5f)
                mShaderMatrix.setScale(scale, scale)
                mShaderMatrix.postTranslate(dx, dy)
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(borderWidth / 2, borderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
            ScaleType.FIT_CENTER -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.CENTER)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(borderWidth / 2, borderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
            ScaleType.FIT_END -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.END)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(borderWidth / 2, borderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
            ScaleType.FIT_START -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.START)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(borderWidth / 2, borderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
            ScaleType.FIT_XY -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset(borderWidth / 2, borderWidth / 2)
                mShaderMatrix.reset()
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
            else -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.CENTER)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(borderWidth / 2, borderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
        }
        mDrawableRect.set(mBorderRect)
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        mBounds.set(bounds)
        updateShaderMatrix()
    }

    override fun draw(canvas: Canvas) {
        if (mRebuildShader) {
            val bitmapShader = BitmapShader(sourceBitmap, tileModeX, tileModeY)
            if (tileModeX == TileMode.CLAMP && tileModeY == TileMode.CLAMP) {
                bitmapShader.setLocalMatrix(mShaderMatrix)
            }
            mBitmapPaint.shader = bitmapShader
            mRebuildShader = false
        }
        if (isOval) {
            if (borderWidth > 0) {
                canvas.drawOval(mDrawableRect, mBitmapPaint)
                canvas.drawOval(mBorderRect, mBorderPaint)
            } else {
                canvas.drawOval(mDrawableRect, mBitmapPaint)
            }
        } else {
            if (any(mCornersRounded)) {
                val radius = cornerRadius
                if (borderWidth > 0) {
                    canvas.drawRoundRect(mDrawableRect, radius, radius, mBitmapPaint)
                    canvas.drawRoundRect(mBorderRect, radius, radius, mBorderPaint)
                    redrawBitmapForSquareCorners(canvas)
                    redrawBorderForSquareCorners(canvas)
                } else {
                    canvas.drawRoundRect(mDrawableRect, radius, radius, mBitmapPaint)
                    redrawBitmapForSquareCorners(canvas)
                }
            } else {
                canvas.drawRect(mDrawableRect, mBitmapPaint)
                if (borderWidth > 0) {
                    canvas.drawRect(mBorderRect, mBorderPaint)
                }
            }
        }
    }

    private fun redrawBitmapForSquareCorners(canvas: Canvas) {
        if (all(mCornersRounded)) return
        if (cornerRadius == 0f) return

        val left = mDrawableRect.left
        val top = mDrawableRect.top
        val right = left + mDrawableRect.width()
        val bottom = top + mDrawableRect.height()
        val radius = cornerRadius
        if (!mCornersRounded[Corner.TOP_LEFT]) {
            mSquareCornersRect[left, top, left + radius] = top + radius
            canvas.drawRect(mSquareCornersRect, mBitmapPaint)
        }
        if (!mCornersRounded[Corner.TOP_RIGHT]) {
            mSquareCornersRect[right - radius, top, right] = radius
            canvas.drawRect(mSquareCornersRect, mBitmapPaint)
        }
        if (!mCornersRounded[Corner.BOTTOM_RIGHT]) {
            mSquareCornersRect[right - radius, bottom - radius, right] = bottom
            canvas.drawRect(mSquareCornersRect, mBitmapPaint)
        }
        if (!mCornersRounded[Corner.BOTTOM_LEFT]) {
            mSquareCornersRect[left, bottom - radius, left + radius] = bottom
            canvas.drawRect(mSquareCornersRect, mBitmapPaint)
        }
    }

    private fun redrawBorderForSquareCorners(canvas: Canvas) {
        // 没有方角
        if (all(mCornersRounded)) return
        // 没有圆角
        if (cornerRadius == 0f) return

        val left = mDrawableRect.left
        val top = mDrawableRect.top
        val right = left + mDrawableRect.width()
        val bottom = top + mDrawableRect.height()
        val radius = cornerRadius
        val offset = borderWidth / 2
        if (!mCornersRounded[Corner.TOP_LEFT]) {
            canvas.drawLine(left - offset, top, left + radius, top, mBorderPaint)
            canvas.drawLine(left, top - offset, left, top + radius, mBorderPaint)
        }
        if (!mCornersRounded[Corner.TOP_RIGHT]) {
            canvas.drawLine(right - radius - offset, top, right, top, mBorderPaint)
            canvas.drawLine(right, top - offset, right, top + radius, mBorderPaint)
        }
        if (!mCornersRounded[Corner.BOTTOM_RIGHT]) {
            canvas.drawLine(right - radius - offset, bottom, right + offset, bottom, mBorderPaint)
            canvas.drawLine(right, bottom - radius, right, bottom, mBorderPaint)
        }
        if (!mCornersRounded[Corner.BOTTOM_LEFT]) {
            canvas.drawLine(left - offset, bottom, left + radius, bottom, mBorderPaint)
            canvas.drawLine(left, bottom - radius, left, bottom, mBorderPaint)
        }
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun getAlpha(): Int = mBitmapPaint.alpha

    override fun setAlpha(alpha: Int) {
        mBitmapPaint.alpha = alpha
        invalidateSelf()
    }

    override fun getColorFilter(): ColorFilter? {
        return mBitmapPaint.colorFilter
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mBitmapPaint.colorFilter = cf
        invalidateSelf()
    }

    override fun setDither(dither: Boolean) {
        mBitmapPaint.isDither = dither
        invalidateSelf()
    }

    override fun setFilterBitmap(filter: Boolean) {
        mBitmapPaint.isFilterBitmap = filter
        invalidateSelf()
    }

    override fun getIntrinsicWidth(): Int = mBitmapWidth

    override fun getIntrinsicHeight(): Int = mBitmapHeight

    /**
     * @param corner 需要获取圆角半径的角标 ，参考Corner参数定义
     * @return 指定角标的圆角半径
     */
    fun getCornerRadius(corner: Int): Float = if (mCornersRounded[corner]) cornerRadius else 0f

    /**
     * 设置角标的圆角
     * @param radius 圆角角度
     * @param corner 设置指定角时指定值，为空时默认为设置全部角
     * @return the [RoundedDrawable]
     */
    fun setCornerRadius(radius: Float, @Corner corner: Int?): RoundedDrawable {
        if (corner == null) { //不是设置指定角
            setCornerRadius(radius, radius, radius, radius)
        } else {
            require(!(radius != 0f && cornerRadius != 0f && cornerRadius != radius)) {
                "Multiple nonzero corner radii not yet supported."
            }
            if (radius == 0f) {
                if (only(corner, mCornersRounded)) cornerRadius = 0f
                mCornersRounded[corner] = false
            } else {
                if (cornerRadius == 0f) cornerRadius = radius
                mCornersRounded[corner] = true
            }
        }
        return this
    }

    /**
     * 设置多个不同角的圆角
     * @param topLeft top left corner radius.
     * @param topRight top right corner radius
     * @param bottomRight bottom right corner radius.
     * @param bottomLeft bottom left corner radius.
     * @return the [RoundedDrawable]
     */
    fun setCornerRadius(
        topLeft: Float,
        topRight: Float,
        bottomRight: Float,
        bottomLeft: Float
    ): RoundedDrawable {
        val radiusSet: MutableSet<Float> = HashSet(4)
        radiusSet.add(topLeft)
        radiusSet.add(topRight)
        radiusSet.add(bottomRight)
        radiusSet.add(bottomLeft)
        radiusSet.remove(0f)
        require(radiusSet.size <= 1) { "Multiple nonzero corner radii not yet supported." }
        cornerRadius = (if (radiusSet.isNotEmpty()) {
            val radius = radiusSet.iterator().next()
            require(!(java.lang.Float.isInfinite(radius) || java.lang.Float.isNaN(radius) || radius < 0)) { "Invalid radius value: $radius" }
            radius
        } else 0f)
        mCornersRounded[Corner.TOP_LEFT] = topLeft > 0
        mCornersRounded[Corner.TOP_RIGHT] = topRight > 0
        mCornersRounded[Corner.BOTTOM_RIGHT] = bottomRight > 0
        mCornersRounded[Corner.BOTTOM_LEFT] = bottomLeft > 0
        return this
    }

    /**
     * 设置边框宽度
     * @param width 宽度
     */
    fun setBorderWidth(width: Float): RoundedDrawable {
        borderWidth = width
        mBorderPaint.strokeWidth = borderWidth
        return this
    }

    /**
     * 获取边框色值
     */
    val borderColor: Int
        get() = borderColors.defaultColor

    /**
     * 设置边框色值
     * @param color 色值
     */
    fun setBorderColor(@ColorInt color: Int): RoundedDrawable =
        setBorderColor(ColorStateList.valueOf(color))

    fun setBorderColor(colors: ColorStateList?): RoundedDrawable {
        borderColors = colors ?: ColorStateList.valueOf(0)
        mBorderPaint.color = borderColors.getColorForState(state, DEFAULT_BORDER_COLOR)
        return this
    }

    /**
     * 是否设置成椭圆
     */
    fun setOval(oval: Boolean): RoundedDrawable {
        isOval = oval
        return this
    }

    /**
     * 设置比列类型
     */
    fun setScaleType(scaleType: ScaleType?): RoundedDrawable {
        var scaleType = scaleType
        if (scaleType == null) scaleType = ScaleType.FIT_CENTER
        if (this.scaleType != scaleType) {
            this.scaleType = scaleType
            updateShaderMatrix()
        }
        return this
    }

    /**
     * 设置X 图块模式
     */
    fun setTileModeX(tileModeX: TileMode): RoundedDrawable {
        if (this.tileModeX != tileModeX) {
            this.tileModeX = tileModeX
            mRebuildShader = true
            invalidateSelf()
        }
        return this
    }

    /**
     * 设置Y 图块模式
     */
    fun setTileModeY(tileModeY: TileMode): RoundedDrawable {
        if (this.tileModeY != tileModeY) {
            this.tileModeY = tileModeY
            mRebuildShader = true
            invalidateSelf()
        }
        return this
    }

    /**
     * 获取bitmap
     */
    fun toBitmap(): Bitmap? = drawableToBitmap(this)

    companion object {
        private const val TAG = "RoundedDrawable"
        const val DEFAULT_BORDER_COLOR = Color.BLACK

        @JvmStatic
        fun fromBitmap(bitmap: Bitmap?): RoundedDrawable? = bitmap?.let { RoundedDrawable(it) }

        @JvmStatic
        fun fromDrawable(drawable: Drawable?): Drawable? {
            if (drawable != null) {
                //已经是RoundedDrawable就返回
                when (drawable) {
                    is RoundedDrawable -> return drawable
                    is LayerDrawable -> {
                        val num = drawable.numberOfLayers

                        //遍历图层并在可能的情况下更改为RoundedDrawables
                        for (i in 0 until num) {
                            val d = drawable.getDrawable(i)
                            drawable.setDrawableByLayerId(drawable.getId(i), fromDrawable(d))
                        }
                        return drawable
                    }
                    is NinePatchDrawable -> return drawable
                    else -> {
                        val bm = drawableToBitmap(drawable)
                        if (bm != null) return RoundedDrawable(bm)
                    }
                }
            }
            return drawable
        }

        fun drawableToBitmap(drawable: Drawable): Bitmap? {
            if (drawable is BitmapDrawable) return drawable.bitmap
            var bitmap: Bitmap?
            val width = Math.max(drawable.intrinsicWidth, 2)
            val height = Math.max(drawable.intrinsicHeight, 2)
            try {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "Failed to create bitmap from drawable!")
                bitmap = null
            }
            return bitmap
        }

        private fun only(index: Int, booleans: BooleanArray): Boolean {
            var i = 0
            val len = booleans.size
            while (i < len) {
                if (booleans[i] != (i == index)) return false
                i++
            }
            return true
        }

        private fun any(booleans: BooleanArray): Boolean {
            for (b in booleans) if (b) return true
            return false
        }

        private fun all(booleans: BooleanArray): Boolean {
            for (b in booleans) if (b) return false
            return true
        }
    }

    init {
        mBitmapRect[0f, 0f, mBitmapWidth.toFloat()] = mBitmapHeight.toFloat()
        mBitmapPaint = Paint()
        mBitmapPaint.style = Paint.Style.FILL
        mBitmapPaint.isAntiAlias = true
        mBorderPaint = Paint()
        mBorderPaint.style = Paint.Style.STROKE
        mBorderPaint.isAntiAlias = true
        mBorderPaint.color = borderColors.getColorForState(state, DEFAULT_BORDER_COLOR)
        mBorderPaint.strokeWidth = borderWidth
    }
}