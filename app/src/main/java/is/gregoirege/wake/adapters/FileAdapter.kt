package `is`.gregoirege.wake.adapters

import `is`.gregoirege.wake.R
import `is`.gregoirege.wake.activities.EditActivity
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import org.jetbrains.anko.*
import org.jetbrains.anko.cardview.v7.cardView
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.io.File

class FileAdapter(private val activity: EditActivity) : ArrayAdapter<File>(activity, -1) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?) = activity.UI {
        val file = getItem(position)
        val modif = DateUtils.getRelativeTimeSpanString(
                file.lastModified(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS)

        relativeLayout {
            lparams(width = matchParent, height = wrapContent)

            cardView {
                lparams(width = matchParent, height = wrapContent, gravity = Gravity.FILL_HORIZONTAL)

                radius = dimen(R.dimen.card_radius).toFloat()

                onClick {
                    activity.editor.isDirty = false
                    activity.openFile(file)
                }

                verticalLayout {
                    textView {
                        textSize = dimen(R.dimen.card_content_size).toFloat()
                        ellipsize = TextUtils.TruncateAt.END
                        maxLines = 1
                        text = file.name
                    }

                    textView {
                        textSize = dimen(R.dimen.card_description_size).toFloat()
                        ellipsize = TextUtils.TruncateAt.END
                        maxLines = 1
                        text = activity.getString(R.string.lastModified, modif)
                    }
                }.lparams {
                    margin = dimen(R.dimen.card_padding)
                }
            }
        }
    }.view
}