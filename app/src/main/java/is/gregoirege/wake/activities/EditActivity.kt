package `is`.gregoirege.wake.activities

import `is`.gregoirege.wake.*
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import org.jetbrains.anko.*
import org.jetbrains.anko.cardview.v7.cardView
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.design.navigationView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.textChangedListener
import org.jetbrains.anko.support.v4.drawerLayout
import java.io.File
import java.io.FileFilter

const val RW_REQUEST_CODE = 0x12

class EditActivity : AppCompatActivity() {
    private lateinit var editor : EditText
    private lateinit var fab : FloatingActionButton
    private lateinit var directoryAdapter: ArrayAdapter<File>
    private lateinit var fileAdapter: ArrayAdapter<File>
    private lateinit var currentDir: File
    private lateinit var currentFile: File
    private lateinit var pathTextView: TextView
    private lateinit var fileTextView: TextView
    private lateinit var drawer: DrawerLayout
    private lateinit var filesTitle: TextView

    private val wakeDir = Environment.getExternalStorageDirectory().resolve("Wake")
    private val welcomeFile = wakeDir.resolve("welcome.md")

    private var observer : DirectoryObserver? = null
        set(value) {
            field?.close()
            field = value
        }

    private var isDirty = false
        set(value) {
            field = value

            if (value) {
                fab.show()
            } else {
                fab.hide()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askReadWritePermission()

        performSetup()

        val canRead = canReadWrite

        currentDir = wakeDir
        currentFile = welcomeFile

        directoryAdapter = object : ArrayAdapter<File>(this, -1) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?) = UI {
                val dir = getItem(position)
                val marg = dimen(R.dimen.drawer_padding) * 2
                val size = (parent as GridView).columnWidth - marg

                cardView {
                    lparams(width = size, height = wrapContent) {
                        margin = dimen(R.dimen.card_padding)
                    }

                    radius = dimen(R.dimen.card_radius).toFloat()

                    onClick {
                        changeDirectory(dir)
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

        fileAdapter = object : ArrayAdapter<File>(this, -1) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?) = UI {
                val file = getItem(position)
                val modif = DateUtils.getRelativeTimeSpanString(
                        file.lastModified(),
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS)

                cardView {
                    lparams(width = matchParent, height = wrapContent)

                    radius = dimen(R.dimen.card_radius).toFloat()

                    onClick {
                        this@EditActivity.isDirty = false
                        openFile(file)
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
                            text = getString(R.string.lastModified, modif)
                        }
                    }.lparams {
                        margin = dimen(R.dimen.card_padding)
                    }
                }
            }.view
        }

        val theme = Theme("default", "fonts/firacode_retina.ttf", "fonts/firacode_regular.ttf",
                Color.WHITE, Color.BLACK,
                Color.WHITE, Color.DKGRAY,
                Color.WHITE, Color.GRAY)

        drawer = drawerLayout {
            backgroundColor = theme.background

            frameLayout {
                fitsSystemWindows = true

                scrollView {
                    lparams(width = matchParent, height = matchParent)

                    isFocusableInTouchMode = true
                    descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS

                    editor = themedEditText(R.style.Editor) {
                        val renderer = Renderer(this@EditActivity, theme) { text }

                        textSize = 14f
                        backgroundColorResource = android.R.color.transparent
                        typeface = renderer.normalTypeface

                        textChangedListener {
                            onTextChanged { s, start, before, count ->
                                this@EditActivity.isDirty = true
                                renderer.render(s!!.toString(), start, before, count)
                            }
                        }

                        if (!canRead) {
                            setText(R.string.askPermission)
                        }
                    }.lparams(width = matchParent, height = wrapContent, gravity = Gravity.TOP) {
                        val margin = 16.dp

                        setMargins(margin, margin, margin, margin)
                    }
                }

                fab = floatingActionButton {
                    imageResource = R.drawable.ic_save
                    backgroundTintList = getColorStateList(R.color.save)
                    visibility = View.INVISIBLE

                    onClick {
                        currentFile.writeText(editor.text.toString())
                        this@EditActivity.isDirty = false
                    }
                }.lparams(gravity = Gravity.BOTTOM or Gravity.END) {
                    margin = dimen(R.dimen.fab_margin)
                }
            }.lparams(width = matchParent, height = matchParent)

            navigationView {
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
                        backgroundTintList = getColorStateList(R.color.create)

                        size = FloatingActionButton.SIZE_MINI

                        onClick {
                            var text : EditText? = null

                            val view = UI {
                                frameLayout {
                                    text = editText {
                                        hintResource = R.string.file_name
                                    }.lparams(width = matchParent) {
                                        margin = 5.dp
                                    }
                                }
                            }

                            val dialog = AlertDialog.Builder(this@EditActivity)
                                    .setTitle(R.string.create_file)
                                    .setCancelable(true)
                                    .setView(view.view)
                                    .setPositiveButton(R.string.create, { _, _ ->
                                        val newFileName = text?.text?.toString()

                                        if (newFileName != null && !newFileName.isBlank()) {
                                            val newFile = currentDir.resolve(newFileName)

                                            newFile.createNewFile()
                                            openFile(newFile)
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
                        backgroundTintList = getColorStateList(R.color.create)

                        size = FloatingActionButton.SIZE_MINI

                        onClick {
                            var text : EditText? = null

                            val view = UI {
                                frameLayout {
                                    text = editText {
                                        hintResource = R.string.dir_name
                                    }.lparams(width = matchParent) {
                                        margin = 5.dp
                                    }
                                }
                            }

                            val dialog = AlertDialog.Builder(this@EditActivity)
                                    .setTitle(R.string.create_dir)
                                    .setCancelable(true)
                                    .setView(view.view)
                                    .setPositiveButton(R.string.create, { _, _ ->
                                        val newDirName = text?.text?.toString()

                                        if (newDirName != null && !newDirName.isBlank()) {
                                            val newDir = currentDir.resolve(newDirName)

                                            newDir.mkdir()
                                            changeDirectory(newDir)
                                        }
                                    })
                                    .create()

                            dialog.show()
                        }
                    }
                }
            }.lparams(height = matchParent, gravity = Gravity.START)
        }

        KeyboardVisibilityEvent.setEventListener(this, { isOpen ->
            if (isOpen) {
                fab.visibility = View.INVISIBLE
            } else if (isDirty) {
                fab.show()
            }
        })

        if (canRead) {
            changeDirectory(currentDir)
            openFile(currentFile)
        }
    }

    private fun changeDirectory(directory: File) {
        val dir = when (directory.path) {
            ".." -> currentDir.parentFile
            else -> directory
        }

        currentDir = dir
        pathTextView.text = dir.absolutePath

        directoryAdapter.clear()
        fileAdapter.clear()

        if (currentDir != Environment.getExternalStorageDirectory()) {
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

        observer = object : DirectoryObserver(currentDir.canonicalPath) {
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

    private fun openFile(file: File) {
        currentFile = file
        fileTextView.text = currentFile.name

        drawer.closeDrawer(Gravity.START)
        editor.setText(file.readText())
    }

    private val canReadWrite
        get() = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
             && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun askReadWritePermission() {
        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), RW_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = when {
        requestCode == RW_REQUEST_CODE && resultCode == Activity.RESULT_OK -> {
            performSetup()
            changeDirectory(wakeDir)
            openFile(welcomeFile)
        }
        else -> {}
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(Gravity.START)) {
            drawer.closeDrawer(Gravity.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun performSetup() {
        if (!wakeDir.exists()) {
            wakeDir.mkdir()
        }

        val preferences = wakeDir.resolve("preferences.md")

        if (!preferences.exists()) {
            preferences.createNewFile()
            preferences.writeText(getString(R.string.defaultPreferences))
        }

        val themes = wakeDir.resolve("themes")

        if (!themes.exists()) {
            themes.mkdirs()
        }

        val current = wakeDir.resolve("current.md")

        if (!current.exists()) {
            current.createNewFile()
            current.writeText(getString(R.string.defaultTheme))
        }

        val about = wakeDir.resolve("about.md")

        if (!about.exists()) {
            about.createNewFile()
            about.writeText(getString(R.string.about))
        }

        if (!welcomeFile.exists()) {
            welcomeFile.createNewFile()
            welcomeFile.writeText(getString(R.string.defaultFile))
        }
    }
}
