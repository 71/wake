package gregoiregeis.wake.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.PaintDrawable
import android.text.Layout
import android.text.style.LeadingMarginSpan
import android.text.style.LineBackgroundSpan
import android.text.style.LineHeightSpan

class FullBackgroundColorSpan(color: Int) : LineBackgroundSpan, LeadingMarginSpan, LineHeightSpan{
    private val bgPaint = PaintDrawable(color).paint

    override fun drawBackground(c: Canvas?, p: Paint?, left: Int, right: Int, top: Int, baseline: Int, bottom: Int, text: CharSequence?, start: Int, end: Int, lnum: Int) {
        c!!.drawRect(0f, top.toFloat(), c.width.toFloat(), bottom.toFloat(), bgPaint)
    }

    override fun getLeadingMargin(first: Boolean) = 20

    override fun drawLeadingMargin(c: Canvas?, p: Paint?, x: Int, dir: Int, top: Int, baseline: Int, bottom: Int, text: CharSequence?, start: Int, end: Int, first: Boolean, layout: Layout?) {}

    override fun chooseHeight(text: CharSequence?, start: Int, end: Int, spanstartv: Int, v: Int, fm: Paint.FontMetricsInt?) {
        fm!!.apply {
            ascent -= 10
            descent += 10
        }
    }
}
