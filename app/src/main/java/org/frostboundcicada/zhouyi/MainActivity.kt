package org.frostboundcicada.zhouyi

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import android.widget.*
import android.text.InputType

private data class Entry(val section: String, val title: String, val source: String, val body: String)

class MainActivity : Activity() {
    private val ink = Color.rgb(43, 38, 32)
    private val muted = Color.rgb(115, 104, 91)
    private val paper = Color.rgb(248, 245, 237)
    private lateinit var list: LinearLayout
    private lateinit var query: EditText
    private var category = "全部"

    private val entries = listOf(
        Entry("易经", "乾卦·经文", "《周易》通行本·上经；多版本逐字校勘尚待完成。", """
乾：元亨，利贞。

初九：潜龙勿用。
九二：见龙在田，利见大人。
九三：君子终日乾乾，夕惕若，厉，无咎。
九四：或跃在渊，无咎。
九五：飞龙在天，利见大人。
上九：亢龙有悔。
用九：见群龙无首，吉。

彖曰：大哉乾元，万物资始，乃统天。云行雨施，品物流形。大明终始，六位时成，时乘六龙以御天。乾道变化，各正性命，保合太和，乃利贞。首出庶物，万国咸宁。

象曰：天行健，君子以自强不息。
""".trimIndent()),
        Entry("易经", "坤卦·经文", "《周易》通行本·上经；异文校记待逐本补录。", """
坤：元亨，利牝马之贞。君子有攸往，先迷，后得主，利。西南得朋，东北丧朋。安贞吉。

初六：履霜，坚冰至。
六二：直方大，不习无不利。
六三：含章可贞，或从王事，无成有终。
六四：括囊，无咎无誉。
六五：黄裳，元吉。
上六：龙战于野，其血玄黄。
用六：利永贞。

彖曰：至哉坤元，万物资生，乃顺承天。坤厚载物，德合无疆。含弘光大，品物咸亨。

象曰：地势坤，君子以厚德载物。
""".trimIndent()),
        Entry("洛书", "洛书九宫数图（通行图式）", "后世通行九宫排列示意；不是已核验的先秦原图。", """
四　九　二
三　五　七
八　一　六

横、纵、两条对角线各数之和均为十五：
4+9+2=15；3+5+7=15；8+1+6=15。
4+3+8=15；9+5+1=15；2+7+6=15。
4+5+6=15；2+5+8=15。

以上是通行九宫图式的算术关系。其历史形成及与早期“洛书”记载的对应关系，仍须依据具体文献版本和出土材料分别论证。
""".trimIndent()),
        Entry("洛书", "《周易·系辞上传》相关原文", "通行本《系辞上传》第十一章；原文提及河图、洛书，但未直接列出九宫数字。", """
是故天生神物，圣人则之；天地变化，圣人效之；天垂象，见吉凶，圣人象之；河出图，洛出书，圣人则之。

译文：因此，上天产生神妙之物，圣人取法于它；天地发生变化，圣人仿效它；天上显示征象并呈现吉凶，圣人据此取象；黄河出现图，洛水出现书，圣人取法于它们。

校勘提示：此段是“河图”“洛书”观念的重要传世文献依据，但不能仅凭这句话断定后世九宫数图的具体形态在此时已经固定。
""".trimIndent()),
        Entry("洛书", "《尚书·洪范》九畴", "《洪范》原文节录；与洛书九宫的对应关系属于后世解释史议题。", """
惟十有三祀，王访于箕子。王乃言曰：“呜呼！箕子，惟天阴骘下民，相协厥居，我不知其彝伦攸叙。”

箕子乃言曰：“我闻在昔，鲧陻洪水，汩陈其五行；帝乃震怒，不畀洪范九畴，彝伦攸斁。鲧则殛死，禹乃嗣兴。天乃锡禹洪范九畴，彝伦攸叙。”

九畴次第：
一曰五行；二曰敬用五事；三曰农用八政；四曰协用五纪；五曰建用皇极；六曰乂用三德；七曰明用稽疑；八曰念用庶征；九曰向用五福，威用六极。

说明：此处展示《洪范》九畴文本结构。不能未经论证就将其直接称为洛书九宫图原文。
""".trimIndent()),
        Entry("河图", "河图专题·资料边界", "当前为专题说明；古籍图版与历代图式尚待逐本核验。", """
河图与洛书在后世易学中经常并称，但两者的图式、数字配置、文献解释史必须分开记录。

后续分别收录：
一、传世文献中“河出图”的原文与注疏；
二、宋代图书易学中的河图图式及版本；
三、刘牧、朱熹、蔡元定等人的论说；
四、图式异同、来源争议与现代研究。

在取得可核验的版本影像前，不补画所谓“古本原图”，也不把现代重绘图标成古籍原图。
""".trimIndent()),
        Entry("版本档案", "敦煌本《周易》资料", "研究线索目录；原卷影像、释文及拼接关系仍需逐项核验。", """
重点档案线索：
• 敦煌《周易正义》188071：傅斯年图书馆数字资源。
• P.2530：王弼注《周易》残卷，涉及噬嗑至离卦段落。
• P.2532：王弼注《周易》残卷，涉及解至益卦段落。
• P.2616、P.2619、P.3683、P.3640等：需继续核对原卷图像及拼接关系。
• S.5735+P.2617：含《经典释文》及《易略例》相关残片。

残片拼接关系、卷次、年代判断和释文均须保留来源及置信程度。未经原图核验的 OCR 不作为底本文字。
""".trimIndent())
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = paper
        window.navigationBarColor = paper
        window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        makeUi()
        render()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun panel(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun makeUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(18), dp(16), dp(8))
            setBackgroundColor(paper)
        }
        root.addView(TextView(this).apply {
            text = "周易数字典藏"
            textSize = 27f
            setTextColor(ink)
            typeface = Typeface.create("serif", Typeface.BOLD)
        })
        root.addView(TextView(this).apply {
            text = "离线古籍阅读 · 原文优先 · 版本可追溯"
            textSize = 12f
            setTextColor(muted)
            setPadding(0, dp(4), 0, dp(12))
        })
        query = EditText(this).apply {
            hint = "搜索卦名、篇名或正文"
            textSize = 14f
            setSingleLine(true)
            inputType = InputType.TYPE_CLASS_TEXT
            setPadding(dp(12), 0, dp(12), 0)
            background = panel(Color.WHITE, 12)
            addTextChangedListener(SimpleTextWatcher { render() })
        }
        root.addView(query, LinearLayout.LayoutParams(-1, dp(46)))
        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("全部", "易经", "洛书", "河图", "版本档案").forEach { name ->
            nav.addView(Button(this).apply {
                text = name
                textSize = 10f
                isAllCaps = false
                setOnClickListener { category = name; render() }
            }, LinearLayout.LayoutParams(0, dp(44), 1f).apply { marginEnd = dp(2) })
        }
        root.addView(nav)
        val scroll = ScrollView(this)
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun render() {
        if (!::list.isInitialized) return
        list.removeAllViews()
        val term = if (::query.isInitialized) query.text.toString().trim() else ""
        val matches = entries.filter { e ->
            (category == "全部" || e.section == category) &&
            (term.isEmpty() || e.title.contains(term, true) ||
             e.body.contains(term, true) || e.source.contains(term, true))
        }
        if (matches.isEmpty()) list.addView(TextView(this).apply {
            text = "没有找到匹配内容。"
            setTextColor(muted)
            setPadding(dp(12), dp(20), dp(12), dp(20))
        })
        matches.forEach { e ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(14), dp(16), dp(16))
                background = panel(Color.WHITE, 14)
            }
            card.addView(TextView(this).apply {
                text = e.section
                textSize = 11f
                setTextColor(Color.rgb(151, 91, 49))
            })
            card.addView(TextView(this).apply {
                text = e.title
                textSize = 19f
                setTextColor(ink)
                typeface = Typeface.create("serif", Typeface.BOLD)
                setPadding(0, dp(4), 0, dp(5))
            })
            card.addView(TextView(this).apply {
                text = e.source
                textSize = 11f
                setTextColor(muted)
                setPadding(0, 0, 0, dp(12))
            })
            card.addView(TextView(this).apply {
                text = e.body
                textSize = 16f
                setTextColor(ink)
                typeface = Typeface.create("serif", Typeface.NORMAL)
                setLineSpacing(dp(5).toFloat(), 1.08f)
                setTextIsSelectable(true)
            })
            list.addView(card, LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(12)
            })
        }
        list.addView(TextView(this).apply {
            text = "开发预览版：未录入内容不会以推测文字补齐；校勘、译文及图像出处将逐条补充。"
            textSize = 11f
            setTextColor(muted)
            gravity = android.view.Gravity.CENTER
            setPadding(dp(8), dp(12), dp(8), dp(20))
        })
    }
}
