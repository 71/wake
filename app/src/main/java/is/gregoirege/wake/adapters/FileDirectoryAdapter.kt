package `is`.gregoirege.wake.adapters

import `is`.gregoirege.wake.R
import `is`.gregoirege.wake.activities.EditActivity
import `is`.gregoirege.wake.helpers.dp
import android.graphics.Color
import android.os.Environment
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.io.File
import java.io.FileFilter

class FileDirectoryAdapter(private val activity: EditActivity) : BaseExpandableListAdapter() {
    val files = mutableListOf<File>()
    val directories = mutableListOf<File>()

    private val iconSize = activity.dimen(R.dimen.circle_size)

    override fun hasStableIds() = true
    override fun isChildSelectable(groupPosition: Int, childPosition: Int) = false

    override fun getGroupCount() = if (files.isNotEmpty()) { 2 } else { 1 }

    override fun getGroup(groupPosition: Int) = when(groupPosition) {
        0 -> directories
        1 -> files
        else -> null
    }

    override fun getGroupId(groupPosition: Int) = when (groupPosition) {
        0 -> 0L
        1 -> 1L
        else -> -1L
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?) = activity.UI {
        linearLayout {
            verticalPadding = dimen(R.dimen.drawer_padding)

            imageView {
                imageResource = if (isExpanded) {
                    R.drawable.ic_expand_less
                } else {
                    R.drawable.ic_expand_more
                }

                scaleType = ImageView.ScaleType.CENTER_INSIDE
            }.lparams(height = iconSize, width = iconSize)

            textView(if (groupPosition == 0) { R.string.directories } else { R.string.files }) {
                textAppearance = R.style.MenuTitle
            }.lparams {
                margin = dimen(R.dimen.card_padding)
            }
        }
    }.view


    override fun getChildrenCount(groupPosition: Int) = when (groupPosition) {
        0 -> directories.count()
        1 -> files.count()
        else -> 0
    }

    override fun getChild(groupPosition: Int, childPosition: Int) = when (groupPosition) {
        0 -> directories.getOrNull(childPosition)
        1 -> files.getOrNull(childPosition)
        else -> null
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?) = activity.UI {
        val isDir = groupPosition == 0

        verticalLayout {
            linearLayout {
                lparams {
                    topMargin = dimen(R.dimen.drawer_padding)
                }

                isClickable = true

                imageView {
                    backgroundResource = R.drawable.background_circle
                    imageResource = if (isDir) { R.drawable.ic_folder } else { R.drawable.ic_file }

                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                }.lparams(height = iconSize, width = iconSize)

                if (isDir) {
                    val dir = directories[childPosition]

                    onClick {
                        activity.changeDirectory(dir)
                    }

                    textView {
                        textColor = Color.WHITE
                        textSize = dimen(R.dimen.card_content_size).toFloat()
                        ellipsize = TextUtils.TruncateAt.END
                        maxLines = 1
                        text = dir.name
                    }.lparams {
                        leftMargin = dimen(R.dimen.file_padding)
                        gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    }
                } else {
                    val file = files[childPosition]
                    val modif = DateUtils.getRelativeTimeSpanString(
                            file.lastModified(),
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS)

                    onClick {
                        activity.editor.isDirty = false
                        activity.openFile(file)
                    }

                    verticalLayout {
                        textView {
                            textColor = Color.WHITE
                            textSize = dimen(R.dimen.card_content_size).toFloat()
                            ellipsize = TextUtils.TruncateAt.END
                            maxLines = 1
                            text = file.name
                        }

                        textView {
                            textSize = dimen(R.dimen.card_description_size).toFloat()
                            ellipsize = TextUtils.TruncateAt.END
                            maxLines = 1
                            text = activity.getString(R.string.lastModified, modif.replaceRange(0, 1, modif[0].toString().toLowerCase()))
                        }
                    }.lparams {
                        leftMargin = dimen(R.dimen.file_padding)
                        gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    }
                }
            }

            view {
                backgroundColor = if (isLastChild) { Color.TRANSPARENT } else { Color.DKGRAY }
            }.lparams(width = matchParent, height = 1) {
                topMargin = dimen(R.dimen.drawer_padding)
            }
        }
    }.view

    override fun getChildId(groupPosition: Int, childPosition: Int) = when(groupPosition) {
        0 -> directories.getOrNull(childPosition)?.hashCode()?.toLong() ?: 0
        1 -> files.getOrNull(childPosition)?.hashCode()?.toLong() ?: 0
        else -> 0
    }

    fun update(dir: File) {
        this.notifyDataSetInvalidated()

        directories.clear()
        files.clear()

        if (dir != Environment.getExternalStorageDirectory()) {
            directories.add(File(".."))
        }

        for (file in dir.listFiles(FileFilter { it.isDirectory })) {
            directories.add(file)
        }

        for (file in dir.listFiles(FileFilter { it.isFile && it.extension in arrayOf("md", "markdown", "") })) {
            files.add(file)
        }

        this.notifyDataSetChanged()
    }
}
