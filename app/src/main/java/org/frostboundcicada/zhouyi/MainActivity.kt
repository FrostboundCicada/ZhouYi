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
    private var category = "总览"

    private val hexagramNames = listOf(
        "乾","坤","屯","蒙","需","讼","师","比","小畜","履","泰","否","同人","大有","谦","豫","随","蛊","临","观","噬嗑","贲","剥","复","无妄","大畜","颐","大过","坎","离",
        "咸","恒","遁","大壮","晋","明夷","家人","睽","蹇","解","损","益","夬","姤","萃","升","困","井","革","鼎","震","艮","渐","归妹","丰","旅","巽","兑","涣","节","中孚","小过","既济","未济"
    )
    private val hexagramIndex = hexagramNames.mapIndexed { i, name ->
        val section = if (i < 30) "上经" else "下经"
        Entry("易经", "${section} · 第${i + 1}卦", "${name}卦目录条目；经文与传文尚待据可靠底本逐项录入。", "本条目前仅建立卦名与通行本次序，不代表正文已收录。后续将分别录入卦辞、爻辞、彖传、大象、小象，并标注底本、参校本、位置与异文。缺文、残字和不可辨字一律保留缺失状态，不按上下文补写。")
    }.drop(2)
    private val wingsIndex = listOf("彖传上","彖传下","象传上","象传下","系辞上传","系辞下传","文言传","说卦传","序卦传","杂卦传").map {
        Entry("十翼", "易传十翼", "《${it}》", "十翼篇目目录；全文尚待按可靠底本逐段录入。", "本条为篇目目录，不是全文。收录时将区分经文、传文层次，标注章次、版本异文及译文依据。")
    }

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
        Entry("相关古籍", "《尚书·洪范》九畴", "《洪范》原文节录；与洛书九宫的对应关系属于后世解释史议题。", """
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
     + hexagramIndex + wingsIndex

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
            setPadding(dp(18), dp(14), dp(18), dp(8))
            setBackgroundColor(paper)
        }
        root.addView(TextView(this).apply {
            text = "易 · 藏"
            textSize = 12f
            setTextColor(Color.rgb(157, 70, 48))
            typeface = Typeface.DEFAULT_BOLD
        })
        root.addView(TextView(this).apply {
            text = "周易数字典藏"
            textSize = 29f
            setTextColor(ink)
            typeface = Typeface.create("serif", Typeface.BOLD)
            setPadding(0, dp(3), 0, dp(2))
        })
        root.addView(TextView(this).apply {
            text = "经典原文 · 易传十翼 · 河图洛书 · 版本文献"
            textSize = 12f
            setTextColor(muted)
            setPadding(0, 0, 0, dp(12))
        })
        query = EditText(this).apply {
            hint = "⌕  搜索卦名、篇名、原文或版本"
            textSize = 14f
            setSingleLine(true)
            inputType = InputType.TYPE_CLASS_TEXT
            setTextColor(ink)
            setHintTextColor(muted)
            setPadding(dp(14), 0, dp(14), 0)
            background = panel(Color.WHITE, 14)
            addTextChangedListener(SimpleTextWatcher { render() })
        }
        root.addView(query, LinearLayout.LayoutParams(-1, dp(48)))
        val navScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            setPadding(0, dp(12), 0, dp(10))
        }
        val nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("总览", "易经", "十翼", "河图", "洛书", "版本档案", "相关古籍").forEach { name ->
            nav.addView(TextView(this).apply {
                text = name
                textSize = 13f
                gravity = android.view.Gravity.CENTER
                setTextColor(if (category == name) Color.WHITE else muted)
                setPadding(dp(15), 0, dp(15), 0)
                background = GradientDrawable().apply {
                    setColor(if (category == name) ink else Color.WHITE)
                    cornerRadius = dp(20).toFloat()
                    if (category != name) setStroke(dp(1), Color.rgb(229, 221, 207))
                }
                setOnClickListener { category = name; makeUiRefreshNav(); render() }
            }, LinearLayout.LayoutParams(-2, dp(36)).apply { marginEnd = dp(8) })
        }
        navScroll.addView(nav)
        root.addView(navScroll, LinearLayout.LayoutParams(-1, dp(54)))
        val scroll = ScrollView(this)
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun makeUiRefreshNav() {
        makeUi()
    }

    private fun render() {
        if (!::list.isInitialized) return
        list.removeAllViews()
        val term = if (::query.isInitialized) query.text.toString().trim() else ""
        val matches = entries + hexagramIndex + wingsIndex
        val filtered = matches.filter { e ->
            (category == "总览" || e.section == category) &&
            (term.isEmpty() || e.title.contains(term, true) || e.body.contains(term, true) ||
                e.source.contains(term, true) || e.section.contains(term, true))
        }
        if (category == "总览" && term.isEmpty()) {
            val hero = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(18), dp(17), dp(18), dp(17))
                background = GradientDrawable().apply {
                    setColor(ink)
                    cornerRadius = dp(18).toFloat()
                }
            }
            hero.addView(TextView(this).apply {
                text = "典籍总览"
                textSize = 12f
                setTextColor(Color.rgb(224, 192, 132))
            })
            hero.addView(TextView(this).apply {
                text = "从经文出发，逐本核对版本"
                textSize = 20f
                setTextColor(Color.WHITE)
                typeface = Typeface.create("serif", Typeface.BOLD)
                setPadding(0, dp(5), 0, dp(5))
            })
            hero.addView(TextView(this).apply {
                text = "目录先行，正文持续校录；未核验内容不以推测补齐。"
                textSize = 12f
                setTextColor(Color.rgb(225, 218, 204))
            })
            list.addView(hero, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) })
            list.addView(TextView(this).apply {
                text = "典藏分类"
                textSize = 20f
                setTextColor(ink)
                typeface = Typeface.create("serif", Typeface.BOLD)
                setPadding(dp(2), dp(4), 0, dp(9))
            })
            listOf(
                "易经" to "六十四卦 · 上经与下经",
                "十翼" to "彖传、象传、系辞等十篇",
                "河图" to "文献源流、历代图式与争议",
                "洛书" to "九宫图式、文献依据与解释史",
                "版本档案" to "敦煌写本、宋本、日本古活字本",
                "相关古籍" to "《尚书·洪范》等互见文献"
            ).forEach { (name, desc) ->
                list.addView(TextView(this).apply {
                    text = "${name}    ›\n${desc}"
                    textSize = 15f
                    setTextColor(ink)
                    setPadding(dp(15), dp(13), dp(12), dp(13))
                    background = panel(Color.WHITE, 14)
                    setOnClickListener { category = name; makeUi(); render() }
                }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) })
            }
        } else {
            list.addView(TextView(this).apply {
                text = if (category == "总览") "检索结果" else category
                textSize = 21f
                setTextColor(ink)
                typeface = Typeface.create("serif", Typeface.BOLD)
                setPadding(dp(2), dp(6), 0, dp(10))
            })
        }
        if (filtered.isEmpty()) {
            list.addView(TextView(this).apply {
                text = "没有找到匹配内容。"
                setTextColor(muted)
                setPadding(dp(12), dp(20), dp(12), dp(20))
            })
        } else if (!(category == "总览" && term.isEmpty())) {
            filtered.forEach { e ->
                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(15), dp(13), dp(15), dp(14))
                    background = panel(Color.WHITE, 15)
                    setOnClickListener { showEntry(e) }
                }
                card.addView(TextView(this).apply {
                    text = e.section + " · " + e.source
                    textSize = 10f
                    setTextColor(muted)
                })
                card.addView(TextView(this).apply {
                    text = e.title
                    textSize = 18f
                    setTextColor(ink)
                    typeface = Typeface.create("serif", Typeface.BOLD)
                    setPadding(0, dp(5), 0, dp(6))
                })
                card.addView(TextView(this).apply {
                    text = e.body.replace("\n", " ").take(100) + if (e.body.length > 100) "…" else ""
                    textSize = 12f
                    setTextColor(ink)
                })
                list.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(9) })
            }
        }
        list.addView(TextView(this).apply {
            text = "目录条目不等于正文已收录；版本与译文状态以各条说明为准。"
            textSize = 11f
            setTextColor(muted)
            gravity = android.view.Gravity.CENTER
            setPadding(dp(8), dp(15), dp(8), dp(22))
        })
    }

    private fun showEntry(e: Entry) {
        val body = TextView(this).apply {
            text = e.source + "\n\n" + e.body
            textSize = 16f
            setTextColor(ink)
            typeface = Typeface.create("serif", Typeface.NORMAL)
            setLineSpacing(dp(5).toFloat(), 1.08f)
            setPadding(dp(20), dp(12), dp(20), dp(16))
            setTextIsSelectable(true)
        }
        val scroll = ScrollView(this).apply { addView(body) }
        android.app.AlertDialog.Builder(this)
            .setTitle(e.title)
            .setView(scroll)
            .setPositiveButton("关闭", null)
            .show()
    }

