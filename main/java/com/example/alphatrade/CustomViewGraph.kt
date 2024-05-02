package com.example.alphatrade

import android.content.Context
import android.graphics.*
import android.text.TextUtils.isEmpty
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat

class CustomViewGraph(context: Context, attrs: AttributeSet? = null, private var priceList: List<Float>)
    : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawColor(Color.parseColor("#00000000"), PorterDuff.Mode.CLEAR)
        drawGraph(canvas)
    }

    private fun drawGraph(canvas: Canvas?) {

        if (priceList.isEmpty()) {
            return
        }

        paint.color = ContextCompat.getColor(context, R.color.colorSecondary)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f

        val maxValue = priceList.maxOrNull() ?: 0f
        val minValue = priceList.minOrNull() ?: 0f
        val valueRange = maxValue - minValue
        val height = height.toFloat()

        path.reset()

        for ((i, value) in priceList.withIndex()) {

            val x = i * width.toFloat() / (priceList.size - 1)
            val y = (((value-minValue) / valueRange) * height)
            Log.d("Graph", "$i  $y $value")
            if (i == 0) {
                path.moveTo(x, height-y)
            } else {
                path.lineTo(x, height-y)
            }
        }

        canvas?.drawPath(path, paint)
    }
}