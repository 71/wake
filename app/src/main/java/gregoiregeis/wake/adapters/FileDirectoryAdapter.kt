package gregoiregeis.wake.adapters

import gregoiregeis.wake.R
import gregoiregeis.wake.activities.EditActivity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Environment
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.io.File
import java.io.FileFilter

const val FILES_STATE = 0
const val DIRECTORIES_STATE = 1
const val RECENT_STATE = 2

class FileDirectoryAdapter(private val activity: EditActivity) : BaseAdapter() {
    val files = mutableListOf<File>()
    val directories = mutableListOf<File>()
    val recentFiles = mutableListOf<File>()

    var selectedFile: View? = null
    var highestDir: File = Environment.getExternalStorageDirectory()

    private var state = FILES_STATE

    private val iconSize = activity.dimen(R.dimen.circle_size)

    private val currentFiles get() = when (state) {
        FILES_STATE       -> files
        DIRECTORIES_STATE -> directories
        RECENT_STATE      -> recentFiles
        else -> throw IllegalStateException()
    }

    fun openFiles() {
        if (state == FILES_STATE) { return }

        state = FILES_STATE
        super.notifyDataSetChanged()
    }

    fun openDirectories() {
        if (state == DIRECTORIES_STATE) { return }

        state = DIRECTORIES_STATE
        super.notifyDataSetChanged()
    }

    fun openRecentFiles() {
        if (state == RECENT_STATE) { return }

        state = RECENT_STATE
        super.notifyDataSetChanged()
    }

    override fun getItem(position: Int): Any = currentFiles[position]
    override fun getItemId(position: Int): Long = currentFiles[position].hashCode().toLong()
    override fun getCount(): Int = currentFiles.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View = activity.UI {
        val isDir = state == DIRECTORIES_STATE

        verticalLayout {
            background = StateListDrawable().apply {
                addState(intArrayOf(-android.R.attr.state_selected), ColorDrawable(Color.TRANSPARENT))
                addState(intArrayOf(android.R.attr.state_selected), ColorDrawable(Color.DKGRAY))
            }

            linearLayout {
                lparams {
                    topPadding = dimen(R.dimen.drawer_padding)
                    bottomPadding = dimen(R.dimen.drawer_padding)
                }

                isClickable = true

                imageView {
                    imageResource = if (isDir) { R.drawable.ic_folder } else { R.drawable.ic_file }
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                }.lparams(height = iconSize, width = iconSize) {
                    rightMargin = dimen(R.dimen.file_padding)
                }

                if (isDir) {
                    val dir = directories[position]

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
                        leftPadding = dimen(R.dimen.file_padding)
                        gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    }
                } else {
                    val file = files[position]
                    val modif = DateUtils.getRelativeTimeSpanString(
                            file.lastModified(),
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS)

                    onClick {
                        activity.editor.isDirty = false
                        activity.openFile(file)

                        setSelectedTheme(this@verticalLayout)
                    }

                    verticalLayout {
                        textView {
                            setTextColor(ColorStateList(
                                    arrayOf(intArrayOf(-android.R.attr.state_selected), intArrayOf(android.R.attr.state_selected)),
                                    intArrayOf(Color.LTGRAY, Color.WHITE)))

                            textSize = dimen(R.dimen.card_content_size).toFloat()
                            ellipsize = TextUtils.TruncateAt.END
                            maxLines = 1
                            text = file.name
                        }

                        textView {
                            textSize = dimen(R.dimen.card_description_size).toFloat()
                            ellipsize = TextUtils.TruncateAt.END
                            maxLines = 1
                            text = activity.getString(R.string.last_modified, modif)
                        }
                    }.lparams {
                        leftPadding = dimen(R.dimen.file_padding)
                        gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    }

                    if (file == activity.currentFile) {
                        setSelectedTheme(this@verticalLayout)
                    }
                }
            }.lparams(width = matchParent)
        }
    }.view

    private fun setSelectedTheme(view: View) {
        selectedFile?.apply {
            isSelected = false
            isClickable = true
        }

        selectedFile = view

        view.isClickable = false
        view.isSelected = true
    }

    fun update(dir: File) {
        notifyDataSetInvalidated()

        directories.clear()
        files.clear()

        if (dir != highestDir) {
            directories.add(File(".."))
        }

        directories += dir.listFiles(FileFilter { it.isDirectory })
        directories.sortBy {
            it.name.toLowerCase()
        }

        files += dir.listFiles(FileFilter { it.isFile && it.extension in arrayOf("md", "markdown", "") })
        files.sortBy {
            it.name.toLowerCase()
        }

        notifyDataSetChanged()
    }
}
