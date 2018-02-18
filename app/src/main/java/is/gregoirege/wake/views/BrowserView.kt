package `is`.gregoirege.wake.views

import `is`.gregoirege.wake.R
import `is`.gregoirege.wake.activities.EditActivity
import `is`.gregoirege.wake.adapters.FileDirectoryAdapter
import `is`.gregoirege.wake.helpers.AnkoViewProvider
import `is`.gregoirege.wake.helpers.DirectoryObserver
import `is`.gregoirege.wake.helpers.dp
import `is`.gregoirege.wake.helpers.sp
import android.graphics.Color
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v7.app.AlertDialog
import android.view.Gravity
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.TextView
import org.jetbrains.anko.*
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.design.navigationView
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.io.File

class BrowserView(private val activity: EditActivity) : AnkoViewProvider<NavigationView> {
    override val view: NavigationView by lazy {
        createView(AnkoContext.create(activity, activity))
        mainView
    }

    private lateinit var adapter: FileDirectoryAdapter
    private lateinit var directoryItemsView: ExpandableListView

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

        adapter = FileDirectoryAdapter(activity)

        mainView = navigationView {
            fitsSystemWindows = true

            verticalLayout {
                frameLayout {
                    imageView(R.drawable.header) {
                        scaleType = ImageView.ScaleType.FIT_START
                        adjustViewBounds = true
                    }.lparams(height = wrapContent) {
                        bottomMargin = dimen(R.dimen.drawer_image_cut) / 2
                    }

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

                    linearLayout {
                        lparams(width = matchParent, height = matchParent) {
                            margin = dimen(R.dimen.fab_margin)
                        }

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
                    }.lparams {
                        gravity = Gravity.BOTTOM or Gravity.END

                        rightMargin = dimen(R.dimen.fab_margin)
                        topMargin = dimen(R.dimen.drawer_image_cut)
                    }

                }.lparams(width = matchParent, height = wrapContent) {
                    gravity = Gravity.TOP
                }

                verticalLayout {
                    val pad = dimen(R.dimen.drawer_padding)
                    padding = pad
                    leftPadding = pad * 2
                    rightPadding = dimen(R.dimen.drawer_padding) * 4

                    directoryItemsView = expandableListView {
                        setAdapter(this@BrowserView.adapter)
                    }
                }
            }
        }

        directoryItemsView.expandGroup(0)
        directoryItemsView.expandGroup(1)

        null
    }

    fun updateDirectoryListing(dir: File) {
        pathTextView.text = dir.absolutePath

        adapter.update(dir)

        observer = object : DirectoryObserver(dir.canonicalPath) {
            override fun onCreated(path: String) {
                val file = File(path)

                if (file.isDirectory) {
                    adapter.directories.add(file)
                } else {
                    adapter.files.add(file)
                }
            }

            override fun onDeleted(path: String) {
                delete(path, adapter.directories)
                delete(path, adapter.files)
            }

            private fun delete(path: String, adapter: MutableList<File>) {
                var i = 0

                while (i < adapter.count()) {
                    val item = adapter[i]

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