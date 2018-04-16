package `is`.gregoirege.wake.views

import `is`.gregoirege.wake.R
import `is`.gregoirege.wake.Renderer
import `is`.gregoirege.wake.activities.EditActivity
import `is`.gregoirege.wake.helpers.AnkoViewProvider
import `is`.gregoirege.wake.helpers.dp
import android.content.res.ColorStateList
import android.support.design.widget.FloatingActionButton
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import org.jetbrains.anko.*
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.textChangedListener

class EditorView(private val activity: EditActivity) : AnkoViewProvider<FrameLayout> {
    override val view: FrameLayout by lazy {
        createView(AnkoContext.create(activity, activity))
        mainFrame
    }

    lateinit var editor : EditText private set
    lateinit var fab : FloatingActionButton private set
    lateinit var mainFrame: FrameLayout private set

    var lastHash = 0
    var isDirty = false
        set(value) {
            field = value

            if (value) {
                fab.show()
            } else {
                fab.hide()
            }
        }

    private fun createView(ui: AnkoContext<EditActivity>) = with(ui) {
        val theme = owner.userTheme

        mainFrame = frameLayout {
            lparams(width = matchParent, height = matchParent)

            fitsSystemWindows = true

            scrollView {
                lparams(width = matchParent, height = matchParent)

                isFocusableInTouchMode = true
                descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS

                editor = themedEditText(R.style.Editor) {
                    val renderer = Renderer(ui.owner, theme) { text }

                    textSize = 14f
                    backgroundColorResource = android.R.color.transparent
                    typeface = renderer.normalTypeface

                    textChangedListener {
                        onTextChanged { s, start, before, count ->
                            val str = s!!.toString()

                            this@EditorView.isDirty = str.hashCode() != lastHash
                            renderer.render(str, start, before, count)
                        }
                    }

                    if (!ui.owner.canReadWrite) {
                        setText(R.string.askPermission)
                    }
                }.lparams(width = matchParent, height = wrapContent, gravity = Gravity.TOP) {
                    val margin = 16.dp

                    setMargins(margin, margin, margin, margin)
                }
            }

            fab = floatingActionButton {
                imageResource = R.drawable.ic_save
                backgroundTintList = ColorStateList.valueOf(theme.accentColor)
                visibility = View.INVISIBLE

                onClick {
                    val str = editor.text.toString()
                    ui.owner.currentFile.writeText(str)
                    this@EditorView.isDirty = false
                    lastHash = str.hashCode()
                }
            }.lparams(gravity = Gravity.BOTTOM or Gravity.END) {
                margin = dimen(R.dimen.fab_margin)
            }
        }
    }

    fun initialize() {
        KeyboardVisibilityEvent.setEventListener(activity, { isOpen ->
            if (isOpen) {
                fab.visibility = View.INVISIBLE
            } else if (isDirty) {
                fab.show()
            }
        })
    }
}
