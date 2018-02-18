package `is`.gregoirege.wake.components

import `is`.gregoirege.wake.R
import `is`.gregoirege.wake.Renderer
import `is`.gregoirege.wake.activities.EditActivity
import `is`.gregoirege.wake.helpers.AnkoViewProvider
import `is`.gregoirege.wake.helpers.Theme
import `is`.gregoirege.wake.helpers.dp
import android.graphics.Color
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
        val theme = Theme("default", "fonts/firacode_retina.ttf", "fonts/firacode_regular.ttf",
                Color.WHITE, Color.BLACK,
                Color.WHITE, Color.DKGRAY,
                Color.WHITE, Color.GRAY)

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
                            this@EditorView.isDirty = true
                            renderer.render(s!!.toString(), start, before, count)
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
                backgroundTintList = ui.ctx.getColorStateList(R.color.save)
                visibility = View.INVISIBLE

                onClick {
                    ui.owner.currentFile.writeText(editor.text.toString())
                    this@EditorView.isDirty = false
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
