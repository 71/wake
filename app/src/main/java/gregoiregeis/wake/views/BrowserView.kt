package gregoiregeis.wake.views

import gregoiregeis.wake.R
import gregoiregeis.wake.activities.EditActivity
import gregoiregeis.wake.adapters.FileDirectoryAdapter
import gregoiregeis.wake.helpers.AnkoViewProvider
import gregoiregeis.wake.helpers.DirectoryObserver
import gregoiregeis.wake.helpers.isAvailable
import android.graphics.Color
import android.view.View
import android.widget.RelativeLayout
import com.lapism.searchview.Search
import com.lapism.searchview.widget.SearchView
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil
import org.jetbrains.anko.*
import org.jetbrains.anko.design.bottomNavigationView
import java.io.File

class BrowserView(private val activity: EditActivity) : AnkoViewProvider<RelativeLayout> {
    override val view: RelativeLayout by lazy {
        createView(AnkoContext.create(activity, activity))
        mainView
    }

    private lateinit var mainView: RelativeLayout

    lateinit var adapter: FileDirectoryAdapter
    lateinit var search: SearchView

    var searching = false

    private var observer : DirectoryObserver? = null
        set(value) {
            field?.close()
            field = value
        }

    private fun createView(ui: AnkoContext<EditActivity>) = with(ui) {
        val activity = ui.owner

        adapter = FileDirectoryAdapter(activity)

        mainView = relativeLayout {
            lparams(width = matchParent, height = matchParent)

            fitsSystemWindows = true
            id = View.generateViewId()

            val nav = bottomNavigationView {
                id = View.generateViewId()
                backgroundColorResource = R.color.bottomnav_background

                itemTextColor = activity.userTheme.getStateList(Color.LTGRAY)
                itemIconTintList = itemTextColor

                inflateMenu(R.menu.browser_menu)

                setOnNavigationItemSelectedListener {
                    when (it.itemId) {
                        R.id.action_files       -> adapter.openFiles()
                        R.id.action_directories -> adapter.openDirectories()
                        R.id.action_recent      -> adapter.openRecentFiles()
                    }

                    true
                }
            }.lparams(width = matchParent, height = wrapContent) {
                alignParentBottom()
            }

            val header = frameLayout {
                id = View.generateViewId()
                backgroundColorResource = R.color.header_background

                search = SearchView(context).apply {
                    setLogoIcon(R.drawable.ic_home)

                    setOnLogoClickListener {
                        setQuery(activity.currentFile.parentFile.absolutePath, false)
                    }

                    setOnQueryTextListener(object: Search.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: CharSequence): Boolean {
                            val file = File(query.toString())

                            if (file.exists() && file.isFile ||
                                    (file.parentFile != null && (file.parentFile.exists() && file.createNewFile()) || (file.parentFile.mkdirs() && file.createNewFile()))) {
                                activity.openFile(file)
                            } else if (file.isDirectory) {
                                UIUtil.hideKeyboard(activity)
                            }

                            return true
                        }

                        override fun onQueryTextChange(newText: CharSequence) {
                            if (searching) {
                                return
                            }

                            val file = File(newText.toString())

                            searching = true

                            if (file.isDirectory && file.isAvailable) {
                                activity.changeDirectory(file)
                            } else if (file.parentFile != null && file.parentFile.isAvailable && file.parentFile.isDirectory) {
                                activity.changeDirectory(file.parentFile)
                            }

                            searching = false
                        }
                    })
                }

                addView(search)
            }.lparams(width = matchParent, height = wrapContent)

            listView {
                this@BrowserView.adapter = FileDirectoryAdapter(activity)
                this@listView.adapter = this@BrowserView.adapter

                dividerHeight = 0
            }.lparams(width = matchParent, height = wrapContent) {
                below(header)
                above(nav)
            }
        }

        null
    }

    fun updateDirectoryListing(dir: File) {
        if (!searching) {
            search.setQuery(dir.absolutePath, false)
        }

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
