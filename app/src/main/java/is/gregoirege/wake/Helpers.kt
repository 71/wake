package `is`.gregoirege.wake

import android.content.res.ColorStateList
import android.content.res.Resources
import android.net.Uri
import android.support.design.widget.FloatingActionButton
import java.io.File

val Int.dp
    inline get() = (this * Resources.getSystem().displayMetrics.density).toInt()
val Int.px
    inline get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Float.sp
    inline get() = (this * Resources.getSystem().displayMetrics.scaledDensity)
val Float.px
    inline get() = (this / Resources.getSystem().displayMetrics.scaledDensity)

val File.uri: Uri
    inline get() = Uri.fromFile(this)

var FloatingActionButton.backgroundTint
    inline get() = this.backgroundTintList!!.defaultColor
    inline set(value) { this.backgroundTintList = ColorStateList.valueOf(value) }
