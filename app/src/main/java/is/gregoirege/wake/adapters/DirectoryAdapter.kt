package `is`.gregoirege.wake.adapters

import `is`.gregoirege.wake.R
import `is`.gregoirege.wake.activities.EditActivity
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.GridView
import org.jetbrains.anko.*
import org.jetbrains.anko.cardview.v7.cardView
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.io.File

class DirectoryAdapter(private val activity: EditActivity) : ArrayAdapter<File>(activity, -1) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?) = activity.UI {
        val dir = getItem(position)
        val marg = dimen(R.dimen.drawer_padding) * 2
        val size = (parent as GridView).columnWidth - marg

        cardView {
            lparams(width = size, height = wrapContent) {
                margin = dimen(R.dimen.card_padding)
            }

            radius = dimen(R.dimen.card_radius).toFloat()

            onClick {
                activity.changeDirectory(dir)
            }

            textView {
                textSize = dimen(R.dimen.card_content_size).toFloat()
                ellipsize = TextUtils.TruncateAt.END
                maxLines = 1
                text = dir.name
            }.lparams {
                margin = dimen(R.dimen.card_padding)
            }
        }
    }.view
}
