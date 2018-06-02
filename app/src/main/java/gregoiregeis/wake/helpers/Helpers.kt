package gregoiregeis.wake.helpers

import android.content.res.ColorStateList
import android.content.res.Resources
import android.net.Uri
import android.os.Environment
import android.support.design.widget.FloatingActionButton
import android.view.View
import org.yaml.snakeyaml.Yaml
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

val File.isAvailable: Boolean
    get() = this == Environment.getExternalStorageDirectory() || this.parentFile?.isAvailable == true

var FloatingActionButton.backgroundTint
    inline get() = this.backgroundTintList!!.defaultColor
    inline set(value) { this.backgroundTintList = ColorStateList.valueOf(value) }


interface AnkoViewProvider<out V: View> {
    val view: V
}

fun getYamlBlock(file: File): Map<String, Any> {
    val text = StringBuffer()
    var began = false

    for (line in file.readLines()) {
        if (began) {
            if (line.startsWith("```")) {
                break
            }

            text.append(line)
        } else if (line.startsWith("```yaml")) {
            began = true
        }
    }

    return Yaml().load(text.toString())
}
