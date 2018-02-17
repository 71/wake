package `is`.gregoirege.wake

import org.jetbrains.anko.db.classParser

class Theme(
        val name: String,
        val font: String,
        val codeFont: String,
        val foreground: Int,
        val background: Int,
        val codeForeground: Int,
        val codeBackground: Int,
        val quoteForeground: Int,
        val quoteBackground: Int)

val themeParser = classParser<Theme>()

