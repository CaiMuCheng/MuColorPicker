/*
 *
 *  * Copyright (c) 2022 SuMuCheng
 *  *
 *  * CN:
 *  * 作者：SuMuCheng
 *  * Github 主页：https://github.com/CaiMuCheng
 *  *
 *  * 你可以免费使用、商用以下代码，也可以基于以下代码做出修改，但是必须在你的项目中标注出处
 *  * 例如：在你 APP 的设置中添加 “关于编辑器” 一栏，其中标注作者以及此编辑器的 Github 主页
 *  *
 *  * 此代码使用 MPL 2.0 开源许可证，你必须标注作者信息
 *  * 若你要修改文件，请勿删除此注释
 *  * 若你违反以上条例我们有权向您提起诉讼!
 *  *
 *  * EN:
 *  * Author: SuMuCheng
 *  * Github Homepage: https://github.com/CaiMuCheng
 *  *
 *  * You can use the following code for free, commercial use, or make modifications based on the following code, but you must mark the source in your project.
 *  * For example: add an "About Editor" column in your app's settings, which identifies the author and the Github home page of this editor.
 *  *
 *  * This code uses the MPL 2.0 open source license, you must mark the author information
 *  * Do not delete this comment if you want to modify the file.
 *  *
 *  * If you violate the above regulations we have the right to sue you!
 *
 */

package com.mucheng.colorpicker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt

@SuppressLint("ViewConstructor")
open class ColorPickerView(
    context: Context?,
    pickerWidth: Int,
    pickerHeight: Int,
) : View(context) {

    companion object {
        private const val STROKE_WIDTH = 140f
    }

    private val colors =
        intArrayOf(-65536, -65281, -16776961, -16711681, -16711936, -256, -65536)

    private val mWidth = pickerWidth * 2
    private val mHeight = (pickerHeight + STROKE_WIDTH).toInt() + 120
    private val centerX = pickerWidth / 2f
    private val centerY = pickerHeight / 2f

    private val outCircleShader =
        SweepGradient(centerX * 2, centerY + STROKE_WIDTH / 2, colors, null)

    private val outCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = STROKE_WIDTH
        shader = outCircleShader
    }

    private val innerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = colors[0]
    }

    private val rectColors = intArrayOf(-16777216, innerCirclePaint.color, -1)

    private var lightnessChangeShader =
        LinearGradient(centerX, 0f, centerX * 3, 0f, rectColors, null, Shader.TileMode.MIRROR)

    private val lightnessChangeBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = lightnessChangeShader
        style = Paint.Style.FILL
    }

    private val lightnessChangeBarRect = Rect()

    private var onColorChangedListener: OnColorChangedListener? = null

    fun setOnColorChangedListener(listener: OnColorChangedListener) {
        this.onColorChangedListener = listener
    }

    override fun onDraw(canvas: Canvas) {
        // 绘制外圆
        canvas.drawCircle(
            centerX * 2, centerY + STROKE_WIDTH / 2, centerY / 1.2f, outCirclePaint
        )

        // 绘制内圆
        canvas.drawCircle(
            centerX * 2, centerY + STROKE_WIDTH / 2, centerY / 5, innerCirclePaint
        )

        // 绘制明度改变条
        drawLightnessChangeBar(canvas)
    }

    private fun drawLightnessChangeBar(canvas: Canvas) {
        val rect = lightnessChangeBarRect
        rect.left = centerX.toInt()
        rect.top = (centerY + STROKE_WIDTH / 2).toInt() * 2 + 20
        rect.right = (centerX * 3).toInt()
        rect.bottom = (centerY + STROKE_WIDTH / 2).toInt() * 2 + 100

        if (downInOutCircle) {
            rectColors[1] = innerCirclePaint.color
            lightnessChangeShader =
                LinearGradient(rect.left.toFloat(),
                    0f,
                    rect.right.toFloat(),
                    0f,
                    rectColors,
                    null,
                    Shader.TileMode.MIRROR)
            lightnessChangeBarPaint.shader = lightnessChangeShader
        }

        canvas.drawRect(lightnessChangeBarRect, lightnessChangeBarPaint)
    }

    private var downInOutCircle = false
    private var downInRect = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x - mWidth / 2
        val y = event.y - mHeight / 2

        val inOutCircle = checkInOutCircle(x, y)
        val inRect = checkInRect(event.x, event.y)

        Log.e("IN Rects", """
            InOut: $inOutCircle
            InRect: $inRect
        """.trimIndent())

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downInOutCircle = inOutCircle
                downInRect = inRect

                if (downInRect) {
                    val color = interpRectColor(rectColors, x)
                    innerCirclePaint.color = color
                    onColorChangedListener?.onColorChanged(color)
                    downInOutCircle = false
                } else if (downInOutCircle) {
                    val angle = atan2(y.toDouble(), x.toDouble()).toFloat()
                    var unit = (angle / 6.283185307179586).toFloat()

                    if (unit < 0f) {
                        unit += 1f
                    }

                    val color = interpCircleColor(colors, unit)
                    innerCirclePaint.color = color
                    onColorChangedListener?.onColorChanged(color)
                } else {
                    val angle = atan2(y.toDouble(), x.toDouble()).toFloat()
                    var unit = (angle / 6.283185307179586).toFloat()

                    if (unit < 0f) {
                        unit += 1f
                    }

                    val color = interpCircleColor(colors, unit)
                    innerCirclePaint.color = color
                    onColorChangedListener?.onColorChanged(color)
                }
                invalidate()
            }

            MotionEvent.ACTION_UP -> {

            }

            MotionEvent.ACTION_MOVE -> {
                if (inRect) {
                    val color = interpRectColor(rectColors, x)
                    innerCirclePaint.color = color
                    onColorChangedListener?.onColorChanged(color)
                    downInOutCircle = false
                } else {
                    val angle = atan2(y.toDouble(), x.toDouble()).toFloat()
                    var unit = (angle / 6.283185307179586).toFloat()

                    if (unit < 0f) {
                        unit += 1f
                    }

                    val color = interpCircleColor(colors, unit)
                    innerCirclePaint.color = color
                    onColorChangedListener?.onColorChanged(color)
                    downInOutCircle = true
                }
                invalidate()
            }
        }

        return true
    }

    private fun checkInOutCircle(x: Float, y: Float): Boolean {
        val outRadius = centerY / 1.2f + STROKE_WIDTH
        val outCircle = PI * outRadius * outRadius

        val inRadius = centerY / 1.2f
        val inCircle = PI * inRadius * inRadius

        val fingerCircle = PI * (x * x + y * y)

        return fingerCircle < outCircle && fingerCircle > inCircle
    }

    private fun checkInInnerCircle(): Boolean {
        val innerRadius = centerY / 5
        val innerCircle = PI * innerRadius * innerRadius
        val fingerCircle = PI * (x * x + y * y)

        return fingerCircle < innerCircle
    }

    private fun checkInRect(x: Float, y: Float): Boolean {
        val rect = lightnessChangeBarRect
        return rect.left <= x && x <= rect.right && y >= rect.top && y <= rect.bottom
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(mWidth, mHeight)
    }

    private fun interpCircleColor(colors: IntArray, unit: Float): Int {
        if (unit <= 0.0f) {
            return colors[0]
        }
        if (unit >= 1.0f) {
            return colors[colors.size - 1]
        }
        val p = unit * (colors.size - 1)
        val i = p.toInt()
        val p2 = p - i
        val c0 = colors[i]
        val c1 = colors[i + 1]
        val a = ave(Color.alpha(c0), Color.alpha(c1), p2)
        val r = ave(Color.red(c0), Color.red(c1), p2)
        val g = ave(Color.green(c0), Color.green(c1), p2)
        val b = ave(Color.blue(c0), Color.blue(c1), p2)
        return Color.argb(a, r, g, b)
    }

    private fun interpRectColor(colors: IntArray, x: Float): Int {
        val rectRight = lightnessChangeBarRect.right - centerX * 2
        val c0: Int
        val c1: Int
        val p: Float
        if (x < 0.0f) {
            c0 = colors[0]
            c1 = colors[1]
            p = (rectRight + x) / rectRight
        } else {
            c0 = colors[1]
            c1 = colors[2]
            p = x / rectRight
        }
        val a = ave(Color.alpha(c0), Color.alpha(c1), p)
        val r = ave(Color.red(c0), Color.red(c1), p)
        val g = ave(Color.green(c0), Color.green(c1), p)
        val b = ave(Color.blue(c0), Color.blue(c1), p)
        return Color.argb(a, r, g, b)
    }

    private fun ave(s: Int, d: Int, p: Float): Int {
        return ((d - s) * p).roundToInt() + s
    }


}