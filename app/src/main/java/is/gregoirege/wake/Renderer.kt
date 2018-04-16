package `is`.gregoirege.wake

import `is`.gregoirege.wake.helpers.Theme
import `is`.gregoirege.wake.spans.CustomTypefaceSpan
import `is`.gregoirege.wake.spans.FullBackgroundColorSpan
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
import android.text.Spanned
import android.text.style.*
import android.view.View
import com.vladsch.flexmark.ast.*
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough
import com.vladsch.flexmark.parser.Parser
import android.content.Intent
import android.net.Uri
import com.vladsch.flexmark.ext.autolink.AutolinkExtension
import com.vladsch.flexmark.ext.footnotes.Footnote
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem
import com.vladsch.flexmark.superscript.Superscript
import com.vladsch.flexmark.superscript.SuperscriptExtension
import com.vladsch.flexmark.util.options.MutableDataSet
import kotlin.math.max
import kotlin.math.min


private class Change(val style: Any, val start: Int, val end: Int)

class Renderer(private val context: Context, private val theme: Theme, private val getEditable: () -> Editable) {
    val codeTypeface = try {
        Typeface.createFromAsset(context.assets, theme.codeFont)!!
    } catch (_: Exception) {
        Typeface.MONOSPACE
    }!!

    val normalTypeface = try {
        Typeface.createFromAsset(context.assets, theme.font)!!
    } catch (_: Exception) {
        Typeface.DEFAULT
    }!!

    private val spans = mutableListOf<Change>()

    private var currentStart = 0
    private var currentEnd = 0

    private val visitor = NodeVisitor(
            VisitHandler(Text::class.java, ::visit),
            VisitHandler(Heading::class.java, ::visit),
            VisitHandler(StrongEmphasis::class.java, ::visit),
            VisitHandler(Emphasis::class.java, ::visit),
            VisitHandler(Strikethrough::class.java, ::visit),

            VisitHandler(Link::class.java, ::visit),
            VisitHandler(AutoLink::class.java, ::visit),
            VisitHandler(RefNode::class.java, ::visit),
            VisitHandler(MailLink::class.java, ::visit),
            VisitHandler(InlineLinkNode::class.java, ::visit),

            VisitHandler(Image::class.java, ::visit),
            VisitHandler(ImageRef::class.java, ::visit),

            VisitHandler(BulletListItem::class.java, ::visit),

            VisitHandler(CodeBlock::class.java, ::visit),
            VisitHandler(FencedCodeBlock::class.java, ::visit),
            VisitHandler(IndentedCodeBlock::class.java, ::visit),
            VisitHandler(Code::class.java, ::visit),

            VisitHandler(BlockQuote::class.java, ::visit),

            VisitHandler(Footnote::class.java, ::visit),
            VisitHandler(Superscript::class.java, ::visit),
            VisitHandler(TaskListItem::class.java, ::visit)
    )

    private val extensions = listOf(
            StrikethroughExtension.create(),
            AutolinkExtension.create(),
            FootnoteExtension.create(),
            SuperscriptExtension.create(),
            TaskListExtension.create())

    private val parserOptions = MutableDataSet()
            .set(Parser.EXTENSIONS, extensions)

    private val parser = Parser.builder(parserOptions).build()


    private fun visit(heading: Heading) {
        val size = when (heading.level) {
            1 -> 2.0f
            2 -> 1.8f
            3 -> 1.6f
            4 -> 1.4f
            5 -> 1.2f
            else -> 1.0f
        }

        setSpan(RelativeSizeSpan(size), heading)
    }

    private fun visit(text: Text) {
        setSpan(ForegroundColorSpan(theme.foreground), text)
    }

    private fun visit(bold: StrongEmphasis) {
        setSpan(StyleSpan(Typeface.BOLD), bold)
    }

    private fun visit(em: Emphasis) {
        setSpan(StyleSpan(Typeface.ITALIC), em)
    }

    private fun visit(strikethrough: Strikethrough) {
        setSpan(StrikethroughSpan(), strikethrough)
    }


    private fun visit(footnote: Footnote) {
        setSpan(ForegroundColorSpan(Color.GRAY), footnote)
    }

    private fun visit(superscript: Superscript) {
        setSpan(SuperscriptSpan(), superscript)
    }

    private fun visit(taskListItem: TaskListItem) {
        setSpan(BulletSpan(), taskListItem)
    }


    private fun visit(link: Link) {
        visitLink(link, link.url.toString())
    }

    private fun visit(link: AutoLink) {
        visitLink(link, link.url.toString())
    }

    private fun visit(link: RefNode) {
        visitLink(link, link.url.toString())
    }

    private fun visit(link: MailLink) {
        visitLink(link, link.url.toString())
    }

    private fun visit(link: InlineLinkNode) {
        visitLink(link, link.url.toString())
    }

    private fun visitLink(link: Node, url: String) {
        setSpan(object : URLSpan(url) {
            override fun onClick(widget: View?) {
                val i = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                }
                widget?.context?.startActivity(i)
            }
        }, link)

        setSpan(ForegroundColorSpan(theme.accentColor), link)
    }


    private fun visit(image: Image) {
        val uri = try {
            Uri.parse(image.url.toString())
        } catch (e: Exception) {
            return
        }

        // setSpan(ImageSpan(context, uri), image)
    }
    private fun visit(image: ImageRef) {
        val uri = try {
            Uri.parse(image.url.toString())
        } catch (e: Exception) {
            return
        }

        // setSpan(ImageSpan(context, uri), image)
    }


    private fun visit(bulletListItem: BulletListItem) {
        setSpan(BulletSpan(20, 0x00000000), bulletListItem)
    }


    private fun visit(code: CodeBlock)         = visitCode(code, true)
    private fun visit(code: Code)              = visitCode(code, false)
    private fun visit(code: FencedCodeBlock)   = visitCode(code, true)
    private fun visit(code: IndentedCodeBlock) = visitCode(code, true)

    private fun visitCode(node: Node, block: Boolean) {
        if (block) {
            setSpan(FullBackgroundColorSpan(theme.codeBackground), node, false)
        } else {
            setSpan(BackgroundColorSpan(theme.codeBackground), node, false)
        }

        setSpan(CustomTypefaceSpan(codeTypeface), node, false)
        setSpan(ForegroundColorSpan(theme.codeForeground), node)
    }


    private fun visit(quote: BlockQuote) {
        setSpan(FullBackgroundColorSpan(theme.quoteBackground), quote, false)
        setSpan(ForegroundColorSpan(theme.quoteForeground), quote, false)
        setSpan(QuoteSpan(Color.TRANSPARENT), quote)
    }


    private fun setSpan(style: Any, node: Node, visitChildren: Boolean = true) {
        if (node.textLength != 0) {
            val start = node.startOffset
            val end = node.endOffset

            if (max(start, currentStart) - min(end, currentEnd) <= 0) {
                getEditable().setSpan(style, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spans.add(Change(style, start, end))
            }
        }

        if (visitChildren) {
            visitor.visitChildren(node)
        }
    }

    fun render(s: String, start: Int, before: Int, count: Int) {
        currentStart = start
        currentEnd = start + max(count, before)

        val editable = getEditable()
        val prevEnd = start + before

        var i = 0

        while (i < spans.count()) {
            val span = spans[i]

            if (max(start, span.start) - min(prevEnd, span.end) <= 0) {
                editable.removeSpan(span.style)
                spans.removeAt(i)
            } else {
                i++
            }
        }

        if (s.isNotEmpty()) {
            visitor.visit(parser.parse(s))
        }
    }
}
