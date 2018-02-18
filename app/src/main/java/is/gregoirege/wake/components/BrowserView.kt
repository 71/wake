package `is`.gregoirege.wake.components

import `is`.gregoirege.wake.R
import `is`.gregoirege.wake.activities.EditActivity
import `is`.gregoirege.wake.adapters.DirectoryAdapter
import `is`.gregoirege.wake.adapters.FileAdapter
import `is`.gregoirege.wake.helpers.AnkoViewProvider
import `is`.gregoirege.wake.helpers.DirectoryObserver
import `is`.gregoirege.wake.helpers.dp
import `is`.gregoirege.wake.helpers.sp
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Environment
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v7.app.AlertDialog
import android.view.Gravity
import android.view.View
import android.widget.*
import org.jetbrains.anko.*
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.design.navigationView
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.io.File
import java.io.FileFilter

class BrowserView(private val activity: EditActivity) : AnkoViewProvider<NavigationView> {
    override val view: NavigationView by lazy {
        createView(AnkoContext.create(activity, activity))
        mainView
    }

    private lateinit var directoryAdapter: ArrayAdapter<File>
    private lateinit var fileAdapter: ArrayAdapter<File>

    private lateinit var filesTitle: TextView

    private lateinit var pathTextView: TextView
    private lateinit var mainView: NavigationView

    lateinit var fileTextView: TextView private set

    private var observer : DirectoryObserver? = null
        set(value) {
            field?.close()
            field = value
        }

    private fun createView(ui: AnkoContext<EditActivity>) = with(ui) {
        val activity = ui.owner

        directoryAdapter = DirectoryAdapter(activity)
        fileAdapter = FileAdapter(activity)

        mainView = navigationView {
            fitsSystemWindows = true

            verticalLayout {
                frameLayout {
                    imageView(R.drawable.header) {
                        scaleType = ImageView.ScaleType.FIT_START
                        adjustViewBounds = true
                    }.lparams(height = wrapContent)

                    verticalLayout {
                        pathTextView = textView {
                            textColor = Color.WHITE

                            setShadowLayer(1f.sp, 1f.sp, 1f.sp, Color.argb(100, 0, 0, 0))
                        }
                        fileTextView = textView {
                            textSize = 8f.sp
                            textColor = Color.WHITE

                            setShadowLayer(1f.sp, 1.5f.sp, 1.5f.sp, Color.argb(140, 0, 0, 0))
                        }
                    }.lparams(height = 200) {
                        margin = dimen(R.dimen.drawer_padding)
                    }
                }.lparams(width = matchParent, height = wrapContent) {
                    gravity = Gravity.TOP
                }

                verticalLayout {
                    padding = dimen(R.dimen.drawer_padding)

                    textView(R.string.directories) {
                        textAppearance = R.style.MenuTitle
                    }.lparams {
                        topMargin = dimen(R.dimen.drawer_title_top_margin)
                        bottomMargin = dimen(R.dimen.drawer_title_top_margin)
                        leftMargin = 4.dp
                    }

                    gridView {
                        numColumns = 2
                        stretchMode = GridView.STRETCH_COLUMN_WIDTH
                        gravity = Gravity.FILL_HORIZONTAL or Gravity.CENTER_VERTICAL
                        adapter = directoryAdapter
                        verticalSpacing = dimen(R.dimen.drawer_divider_height)
                    }.lparams(weight = 0.3f)

                    filesTitle = textView(R.string.files) {
                        textAppearance = R.style.MenuTitle
                    }.lparams {
                        topMargin = dimen(R.dimen.drawer_title_top_margin)
                        bottomMargin = dimen(R.dimen.drawer_title_top_margin)
                        leftMargin = 4.dp
                    }

                    listView {
                        adapter = fileAdapter

                        divider = ColorDrawable(Color.TRANSPARENT)
                        dividerHeight = dimen(R.dimen.drawer_divider_height)
                    }.lparams(weight = 0.7f)
                }
            }

            linearLayout {
                lparams(width = matchParent, height = matchParent) {
                    margin = dimen(R.dimen.fab_margin)
                }

                gravity = Gravity.BOTTOM or Gravity.END

                floatingActionButton {
                    imageResource = R.drawable.ic_create
                    backgroundTintList = ui.ctx.getColorStateList(R.color.create)

                    size = FloatingActionButton.SIZE_MINI

                    onClick {
                        var text : EditText? = null

                        val view = ui.ctx.UI {
                            frameLayout {
                                text = editText {
                                    hintResource = R.string.file_name
                                }.lparams(width = matchParent) {
                                    margin = 5.dp
                                }
                            }
                        }

                        val dialog = AlertDialog.Builder(ui.owner)
                                .setTitle(R.string.create_file)
                                .setCancelable(true)
                                .setView(view.view)
                                .setPositiveButton(R.string.create, { _, _ ->
                                    val newFileName = text?.text?.toString()

                                    if (newFileName != null && !newFileName.isBlank()) {
                                        val newFile = ui.owner.currentDir.resolve(newFileName)

                                        newFile.createNewFile()
                                        ui.owner.openFile(newFile)
                                    }
                                })
                                .create()

                        dialog.show()
                    }
                }.lparams {
                    rightMargin = dimen(R.dimen.fab_margin)
                }

                floatingActionButton {
                    imageResource = R.drawable.ic_create_new_folder
                    backgroundTintList = ui.ctx.getColorStateList(R.color.create)

                    size = FloatingActionButton.SIZE_MINI

                    onClick {
                        var text : EditText? = null

                        val view = ui.ctx.UI {
                            frameLayout {
                                text = editText {
                                    hintResource = R.string.dir_name
                                }.lparams(width = matchParent) {
                                    margin = 5.dp
                                }
                            }
                        }

                        val dialog = AlertDialog.Builder(ui.owner)
                                .setTitle(R.string.create_dir)
                                .setCancelable(true)
                                .setView(view.view)
                                .setPositiveButton(R.string.create, { _, _ ->
                                    val newDirName = text?.text?.toString()

                                    if (newDirName != null && !newDirName.isBlank()) {
                                        val newDir = ui.owner.currentDir.resolve(newDirName)

                                        newDir.mkdir()
                                        ui.owner.changeDirectory(newDir)
                                    }
                                })
                                .create()

                        dialog.show()
                    }
                }
            }
        }
    }

    fun updateDirectoryListing(dir: File) {
        pathTextView.text = dir.absolutePath

        directoryAdapter.clear()
        fileAdapter.clear()

        if (dir != Environment.getExternalStorageDirectory()) {
            directoryAdapter.add(File(".."))
        }

        for (file in dir.listFiles(FileFilter { it.isDirectory })) {
            directoryAdapter.add(file)
        }

        for (file in dir.listFiles(FileFilter { it.isFile && it.extension in arrayOf("md", "markdown", "") })) {
            fileAdapter.add(file)
        }

        filesTitle.visibility = if (fileAdapter.isEmpty) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }

        observer = object : DirectoryObserver(dir.canonicalPath) {
            override fun onCreated(path: String) {
                val file = File(path)

                if (file.isDirectory) {
                    directoryAdapter.add(file)
                } else {
                    fileAdapter.add(file)
                }

                filesTitle.visibility = View.VISIBLE
            }

            override fun onDeleted(path: String) {
                delete(path, fileAdapter)
                delete(path, directoryAdapter)

                if (fileAdapter.isEmpty) {
                    filesTitle.visibility = View.INVISIBLE
                }
            }

            private fun delete(path: String, adapter: ArrayAdapter<File>) {
                var i = 0

                while (i < adapter.count) {
                    val item = adapter.getItem(i)

                    if (item.path == path) {
                        adapter.remove(item)
                    } else {
                        i++
                    }
                }
            }
        }
    }
}