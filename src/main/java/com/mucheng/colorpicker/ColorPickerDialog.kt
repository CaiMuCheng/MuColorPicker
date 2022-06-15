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
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import java.util.*

open class ColorPickerDialog(context: Context, style: Int = 0) : AlertDialog(context, style),
    View.OnClickListener {

    private var title = "颜色选择器"

    private var titleTextColor = Color.WHITE

    override fun setTitle(title: CharSequence?) {
        if (title != null) {
            this.title = title.toString()
        } else {
            this.title = ""
        }
    }

    fun setTitleTextColor(color: Int): ColorPickerDialog {
        this.titleTextColor = color
        return this
    }

    fun setBackgroundColor(color: Int): ColorPickerDialog {
        window?.setBackgroundDrawable(ColorDrawable(color))
        return this
    }

    private lateinit var argb: MaterialTextView

    private lateinit var hsv: MaterialTextView

    private lateinit var colorTextView: MaterialTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_colorpicker)

        val title = findViewById<MaterialTextView>(R.id.title)!!
        title.setTextColor(titleTextColor)
        title.text = this.title

        val container = findViewById<LinearLayoutCompat>(R.id.container)!!

        val displayMetrics = context.resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.5).toInt()
        val height = (displayMetrics.widthPixels * 0.5).toInt()

        argb = findViewById(R.id.argb)!!
        hsv = findViewById(R.id.hsv)!!
        colorTextView = findViewById(R.id.color)!!

        val colorPickerView = ColorPickerView(context, width, height)
        colorPickerView.setOnColorChangedListener(object : OnColorChangedListener {

            @SuppressLint("SetTextI18n")
            override fun onColorChanged(color: Int) {
                val hexString = Integer.toHexString(color).uppercase(Locale.getDefault())
                colorTextView.text = "#$hexString"

                val array = toARGB(color)
                argb.text = array.joinToString()

                hsv.text = toHsvColors(color).joinToString()
            }

        })
        container.addView(colorPickerView)

        val button = findViewById<MaterialButton>(R.id.ok)!!
        button.setOnClickListener(this)
        argb.setOnClickListener(this)
        hsv.setOnClickListener(this)
        colorTextView.setOnClickListener(this)
    }

    open fun toARGB(color: Int): IntArray {
        val alpha = Color.alpha(color)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return intArrayOf(alpha, red, green, blue)
    }

    open fun toHsvColors(color: Int): FloatArray {
        val hsvColors = FloatArray(3)
        Color.colorToHSV(color, hsvColors)
        return hsvColors
    }

    override fun onClick(v: View) {
        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        when (v.id) {
            R.id.argb -> {
                val clipData = ClipData.newPlainText(null, argb.text)
                clipboard.setPrimaryClip(clipData)

                Toast.makeText(context,
                    "ARGB 颜色复制成功", Toast.LENGTH_SHORT)
                    .show()
                dismiss()
            }

            R.id.hsv -> {
                val clipData = ClipData.newPlainText(null, hsv.text)
                clipboard.setPrimaryClip(clipData)

                Toast.makeText(context,
                    "HSV 颜色复制成功", Toast.LENGTH_SHORT)
                    .show()
                dismiss()
            }

            R.id.color -> {
                val clipData = ClipData.newPlainText(null, colorTextView.text)
                clipboard.setPrimaryClip(clipData)

                Toast.makeText(context,
                    "16 进制颜色复制成功", Toast.LENGTH_SHORT)
                    .show()
                dismiss()
            }

            R.id.ok -> dismiss()
        }
    }

}