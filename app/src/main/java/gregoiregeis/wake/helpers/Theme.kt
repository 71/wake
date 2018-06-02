package gregoiregeis.wake.helpers

import android.content.res.ColorStateList
import org.jetbrains.anko.db.classParser

class Theme(
        val font: String,
        val codeFont: String,
        val accentColor: Int,
        val foreground: Int,
        val background: Int,
        val codeForeground: Int,
        val codeBackground: Int,
        val quoteForeground: Int,
        val quoteBackground: Int) {

    fun getStateList(normalStateColor: Int, state: Int = android.R.attr.state_checked): ColorStateList {
        return ColorStateList(
                arrayOf(
                        intArrayOf(-state),
                        intArrayOf( state)),
                intArrayOf(normalStateColor, accentColor)
        )
    }

    companion object {
        fun fromYaml(data: Map<String, Any>): Theme {
            var font: String? = null
            var codeFont: String? = null
            var accentColor: Int? = null
            var foreground: Int? = null
            var background: Int? = null
            var codeForeground: Int? = null
            var codeBackground: Int? = null
            var quoteForeground: Int? = null
            var quoteBackground: Int? = null

            fun parseColor(input: String): Int {
                val asHex = Regex("(0x|#)([0-9a-fA-F]){2,8}").matchEntire(input)

                if (asHex != null) {
                    return Integer.parseInt(asHex.groupValues[1], 16)
                }

                throw IllegalArgumentException()
            }

            for (datum in data) {
                val k = datum.key
                val v = datum.value as String

                when (k.toLowerCase()) {
                    "font"     -> font     = v
                    "codefont" -> codeFont = v

                    "accentcolor"     -> accentColor     = parseColor(v)
                    "foreground"      -> foreground      = parseColor(v)
                    "background"      -> background      = parseColor(v)
                    "codeforeground"  -> codeForeground  = parseColor(v)
                    "codebackground"  -> codeBackground  = parseColor(v)
                    "quoteforeground" -> quoteForeground = parseColor(v)
                    "quotebackground" -> quoteBackground = parseColor(v)
                }
            }

            return Theme(font!!, codeFont!!, accentColor!!, foreground!!, background!!, codeForeground!!, codeBackground!!, quoteForeground!!, quoteBackground!!)
        }
    }
}

val themeParser = classParser<Theme>()
