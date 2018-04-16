package `is`.gregoirege.wake.helpers

import android.content.res.ColorStateList
import org.jetbrains.anko.db.classParser

class Theme(
        val name: String,
        val font: String,
        val codeFont: String,
        val accentColor: Int,
        val foreground: Int,
        val background: Int,
        val codeForeground: Int,
        val codeBackground: Int,
        val quoteForeground: Int,
        val quoteBackground: Int) {

    fun getStateList(normalStateColor: Int): ColorStateList {
        return ColorStateList(
                arrayOf(
                        intArrayOf(-android.R.attr.state_checked),
                        intArrayOf( android.R.attr.state_checked)),
                intArrayOf(normalStateColor, accentColor)
        )
    }

    companion object {
        fun fromYaml(yaml: String): Theme {
            var name: String? = null
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
                val inQuotes = Regex("\"#([0-9a-fA-F])\"").matchEntire(input)

                if (inQuotes != null) {
                    return Integer.parseInt(inQuotes.groupValues[1], 16)
                }

                val asHex = Regex("0x([0-9a-fA-F])").matchEntire(input)

                if (asHex != null) {
                    return Integer.parseInt(asHex.groupValues[1], 16)
                }

                throw IllegalArgumentException()
            }

            for (line in Regex("$([a-zA-Z-]+): *(.+)^", RegexOption.MULTILINE).findAll(yaml)) {
                val k = line.groupValues[1]
                val v = line.groupValues[2]

                when (k.toLowerCase()) {
                    "name"     -> name     = v
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

            return Theme(name!!, font!!, codeFont!!, accentColor!!, foreground!!, background!!, codeForeground!!, codeBackground!!, quoteForeground!!, quoteBackground!!)
        }
    }
}

val themeParser = classParser<Theme>()
