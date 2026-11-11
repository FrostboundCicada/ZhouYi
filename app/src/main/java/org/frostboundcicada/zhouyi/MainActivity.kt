package org.frostboundcicada.zhouyi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.NavigationBar as MiuixNavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem as MiuixNavigationBarItem
import top.yukonga.miuix.kmp.basic.TopAppBar as MiuixTopAppBar
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

private data class Entry(val section: String, val title: String, val source: String, val body: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ZhouYiApp() }
    }
}

@Composable
private fun ZhouYiApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("zhouyi_settings", 0) }
    var themeMode by remember { mutableStateOf(prefs.getString("theme_mode", "system") ?: "system") }
    val miuixMode = when (themeMode) {
        "light" -> ColorSchemeMode.Light
        "dark" -> ColorSchemeMode.Dark
        else -> ColorSchemeMode.System
    }
    val controller = remember(themeMode) {
        ThemeController(miuixMode, keyColor = Color(0xFF86533D))
    }
    val dark = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    val scheme = if (dark) darkColorScheme(
        primary = Color(0xFFE0B89E), onPrimary = Color(0xFF392316),
        primaryContainer = Color(0xFF654632), onPrimaryContainer = Color(0xFFFFE0CB),
        secondary = Color(0xFFD1BDAA), background = Color(0xFF151411),
        surface = Color(0xFF1E1C18), surfaceVariant = Color(0xFF2B2823),
        onSurface = Color(0xFFEAE3D9), onSurfaceVariant = Color(0xFFBDB3A7),
        outline = Color(0xFF514A41)
    ) else lightColorScheme(
        primary = Color(0xFF80513A), onPrimary = Color.White,
        primaryContainer = Color(0xFFF1DED0), onPrimaryContainer = Color(0xFF32190D),
        secondary = Color(0xFF6D5B4D), background = Color(0xFFF6F3ED),
        surface = Color(0xFFFFFCF7), surfaceVariant = Color(0xFFECE6DC),
        onSurface = Color(0xFF28231E), onSurfaceVariant = Color(0xFF6F665D),
        outline = Color(0xFFD8CEC1)
    )

    SideEffect {
        val window = (context as? android.app.Activity)?.window
        window?.statusBarColor = scheme.background.toArgb()
        window?.navigationBarColor = scheme.surface.toArgb()
        @Suppress("DEPRECATION")
        run {
            window?.decorView?.systemUiVisibility = if (dark) 0 else
                android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
    }

    MiuixTheme(controller = controller) {
        MaterialTheme(colorScheme = scheme) {
            MainScreen(themeMode, onThemeChange = {
                themeMode = it
                prefs.edit().putString("theme_mode", it).apply()
            })
        }
    }
}

@Composable
private fun MainScreen(themeMode: String, onThemeChange: (String) -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    var category by remember { mutableStateOf("全部") }
    var query by remember { mutableStateOf("") }
    var selectedEntry by remember { mutableStateOf<Entry?>(null) }

    val titles = listOf("典藏", "卦序", "专题", "设置")
    val all = remember { buildEntries() }
    val hexagrams = remember { buildHexagramIndex() }
    val wings = remember { buildWingsIndex() }
    val visible = remember(tab, category, query, all, hexagrams, wings) {
        val source = when (tab) {
            1 -> all.filter { it.section == "易经" } + hexagrams
            2 -> all.filter { it.section in listOf("河图", "洛书", "相关古籍", "版本档案") }
            else -> all + wings
        }
        source.filter { e ->
            val categoryMatch = category == "全部" || e.section == category ||
                (category == "十翼" && e.section == "十翼")
            val q = query.trim()
            categoryMatch && (q.isEmpty() || e.title.contains(q, true) ||
                e.body.contains(q, true) || e.source.contains(q, true) || e.section.contains(q, true))
        }
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MiuixTopAppBar(
                title = titles[tab],
                largeTitle = if (tab == 0) "周易数字典藏" else titles[tab],
                subtitle = if (tab == 0) "经典原文 · 易传十翼 · 河图洛书 · 版本文献" else null,
                color = MaterialTheme.colorScheme.background,
                titleColor = MaterialTheme.colorScheme.onSurface,
                largeTitleColor = MaterialTheme.colorScheme.onSurface,
                actions = {
                    if (tab != 3) IconButton(onClick = { tab = 3 }) {
                        Icon(Icons.Filled.Tune, contentDescription = "外观与设置")
                    }
                }
            )
        },
        bottomBar = {
            MiuixNavigationBar(color = MaterialTheme.colorScheme.surface) {
                val icons = listOf(Icons.Filled.Home, Icons.Filled.List, Icons.Filled.Info, Icons.Filled.Settings)
                val labels = listOf("典藏", "卦序", "专题", "设置")
                labels.forEachIndexed { index, label ->
                    MiuixNavigationBarItem(
                        selected = tab == index,
                        onClick = { tab = index; category = "全部"; query = "" },
                        icon = icons[index],
                        label = label
                    )
                }
            }
        }
    ) { padding ->
        if (tab == 3) {
            SettingsPage(themeMode, onThemeChange, Modifier.padding(padding))
        } else {
            LibraryPage(
                tab, category, { category = it }, query, { query = it },
                visible, { selectedEntry = it }, Modifier.padding(padding)
            )
        }
    }

    selectedEntry?.let { entry ->
        ModalBottomSheet(
            onDismissRequest = { selectedEntry = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 680.dp)
                    .padding(horizontal = 24.dp).padding(bottom = 28.dp)
            ) {
                Text(entry.section, color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(entry.title, style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                Text(entry.source, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                HorizontalDivider(Modifier.padding(vertical = 16.dp))
                SelectionContainer {
                    LazyColumn {
                        item {
                            Text(entry.body,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = FontFamily.Serif, lineHeight = 30.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryPage(
    tab: Int,
    category: String,
    onCategoryChange: (String) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    entries: List<Entry>,
    onEntryClick: (Entry) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = when (tab) {
        1 -> listOf("全部", "易经", "十翼")
        2 -> listOf("全部", "河图", "洛书", "版本档案", "相关古籍")
        else -> listOf("全部", "易经", "十翼", "河图", "洛书", "版本档案", "相关古籍")
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (tab == 0 && category == "全部" && query.isBlank()) {
            item { ArchiveHero() }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard("64", "卦序目录", Modifier.weight(1f))
                    MetricCard("10", "十翼篇目", Modifier.weight(1f))
                    MetricCard("2", "专题方向", Modifier.weight(1f))
                }
            }
        }
        item {
            OutlinedTextField(
                value = query, onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(22.dp),
                placeholder = { Text("搜索卦名、篇名、原文或版本") },
                leadingIcon = { Text("⌕", fontSize = 24.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { name ->
                    FilterChip(
                        selected = category == name,
                        onClick = { onCategoryChange(name) },
                        label = { Text(name, maxLines = 1) },
                        shape = RoundedCornerShape(50)
                    )
                }
            }
        }
        if (tab == 0 && category == "全部" && query.isBlank()) {
            item {
                Text("按专题浏览", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            item { CategoryGrid(onCategoryChange) }
            item {
                Text("典藏条目", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        } else {
            item {
                Text(if (entries.isEmpty()) "没有匹配条目" else "共 ${entries.size} 条",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(entries, key = { it.section + it.title }) { entry ->
            EntryCard(entry) { onEntryClick(entry) }
        }
        item {
            Text("目录条目不等于正文已收录；版本、译文与校勘状态以各条说明为准。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        }
    }
}

@Composable
private fun ArchiveHero() {
    Surface(color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(22.dp)) {
            Text("CLASSICAL TEXT ARCHIVE", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(12.dp))
            Text("在经文与版本之间，\n建立可追溯的典籍档案。",
                style = MaterialTheme.typography.headlineSmall, fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(12.dp))
            Text("原文优先 · 逐段译注 · 版本可查 · 缺文不补",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f))
        }
    }
}

@Composable
private fun MetricCard(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(modifier, color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp), tonalElevation = 1.dp) {
        Column(Modifier.padding(vertical = 15.dp, horizontal = 12.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CategoryGrid(onCategoryChange: (String) -> Unit) {
    val categories = listOf(
        Triple("易经", "六十四卦 · 经文与爻辞", "经"),
        Triple("十翼", "彖、象、系辞等十篇", "传"),
        Triple("河图", "文献源流与历代图式", "河"),
        Triple("洛书", "九宫图式与解释史", "洛"),
        Triple("版本档案", "敦煌、宋本与日本古本", "本"),
        Triple("相关古籍", "互见文献与引书", "典")
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        categories.chunked(2).forEach { group ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                group.forEach { (name, description, glyph) ->
                    Surface(
                        modifier = Modifier.weight(1f).clickable { onCategoryChange(name) },
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(20.dp), tonalElevation = 1.dp
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(14.dp)) {
                                Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                                    Text(glyph, style = MaterialTheme.typography.titleLarge,
                                        fontFamily = FontFamily.Serif,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(name, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                Text(description, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryCard(entry: Entry, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(22.dp), tonalElevation = 1.dp) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(entry.section, style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text("阅读  ›", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(7.dp))
            Text(entry.title, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(5.dp))
            Text(entry.source, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(9.dp))
            Text(entry.body.replace("\n", " ").take(116) +
                if (entry.body.length > 116) "…" else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SettingsPage(themeMode: String, onThemeChange: (String) -> Unit,
                         modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("外观", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp),
            tonalElevation = 1.dp) {
            Column(Modifier.padding(18.dp)) {
                Text("主题模式", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text("选择应用外观，也可以跟随系统自动切换。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("system" to "跟随系统", "light" to "浅色", "dark" to "深色").forEach { (value, label) ->
                        FilterChip(selected = themeMode == value,
                            onClick = { onThemeChange(value) }, label = { Text(label) })
                    }
                }
            }
        }
        Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp),
            tonalElevation = 1.dp) {
            Column(Modifier.padding(18.dp)) {
                Text("设计系统", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text("Miuix · HyperOS 风格导航与顶部栏",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Material 3 · 色彩体系、卡片、输入与底部阅读面板",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp),
            tonalElevation = 1.dp) {
            Column(Modifier.padding(18.dp)) {
                Text("典藏原则", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text("缺文不补、异文并列、译文标明依据；未经原图核验的 OCR 不作为底本文字。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private fun buildHexagramIndex(): List<Entry> {
    val names = listOf(
        "乾","坤","屯","蒙","需","讼","师","比","小畜","履","泰","否","同人","大有","谦","豫","随","蛊","临","观","噬嗑","贲","剥","复","无妄","大畜","颐","大过","坎","离",
        "咸","恒","遁","大壮","晋","明夷","家人","睽","蹇","解","损","益","夬","姤","萃","升","困","井","革","鼎","震","艮","渐","归妹","丰","旅","巽","兑","涣","节","中孚","小过","既济","未济"
    )
    return names.mapIndexed { index, name ->
        val section = if (index < 30) "上经" else "下经"
        Entry("易经", "$section · 第${index + 1}卦 · $name",
            "$name卦目录条目；经文与传文尚待据可靠底本逐项录入。",
            "本条目前仅建立卦名与通行本次序，不代表正文已收录。后续将分别录入卦辞、爻辞、彖传、大象、小象，并标注底本、参校本、位置与异文。缺文、残字和不可辨字一律保留缺失状态，不按上下文补写。")
    }.drop(2)
}

private fun buildWingsIndex(): List<Entry> =
    listOf("彖传上","彖传下","象传上","象传下","系辞上传","系辞下传","文言传","说卦传","序卦传","杂卦传").map { name ->
        Entry("十翼", "《$name》", "十翼篇目目录；全文尚待按可靠底本逐段录入。",
            "本条为篇目目录，不是全文。收录时将区分经文、传文层次，标注章次、版本异文及译文依据。")
    }

private fun buildEntries(): List<Entry> = listOf(
    Entry("易经", "乾卦·经文", "《周易》通行本·上经；多版本逐字校勘尚待完成。", """乾：元亨，利贞。

初九：潜龙勿用。
九二：见龙在田，利见大人。
九三：君子终日乾乾，夕惕若，厉，无咎。
九四：或跃在渊，无咎。
九五：飞龙在天，利见大人。
上九：亢龙有悔。
用九：见群龙无首，吉。

彖曰：大哉乾元，万物资始，乃统天。云行雨施，品物流形。大明终始，六位时成，时乘六龙以御天。乾道变化，各正性命，保合太和，乃利贞。首出庶物，万国咸宁。

象曰：天行健，君子以自强不息。"""),
    Entry("易经", "坤卦·经文", "《周易》通行本·上经；异文校记待逐本补录。", """坤：元亨，利牝马之贞。君子有攸往，先迷，后得主，利。西南得朋，东北丧朋。安贞吉。

初六：履霜，坚冰至。
六二：直方大，不习无不利。
六三：含章可贞，或从王事，无成有终。
六四：括囊，无咎无誉。
六五：黄裳，元吉。
上六：龙战于野，其血玄黄。
用六：利永贞。

彖曰：至哉坤元，万物资生，乃顺承天。坤厚载物，德合无疆。含弘光大，品物咸亨。

象曰：地势坤，君子以厚德载物。"""),
    Entry("洛书", "洛书九宫数图（通行图式）", "后世通行九宫排列示意；不是已核验的先秦原图。", """四　九　二
三　五　七
八　一　六

横、纵、两条对角线各数之和均为十五：
4+9+2=15；3+5+7=15；8+1+6=15。
4+3+8=15；9+5+1=15；2+7+6=15。
4+5+6=15；2+5+8=15。

以上是通行九宫图式的算术关系。其历史形成及与早期“洛书”记载的对应关系，仍须依据具体文献版本和出土材料分别论证。"""),
    Entry("洛书", "《周易·系辞上传》相关原文", "通行本《系辞上传》第十一章；原文提及河图、洛书，但未直接列出九宫数字。", """是故天生神物，圣人则之；天地变化，圣人效之；天垂象，见吉凶，圣人象之；河出图，洛出书，圣人则之。

译文：因此，上天产生神妙之物，圣人取法于它；天地发生变化，圣人仿效它；天上显示征象并呈现吉凶，圣人据此取象；黄河出现图，洛水出现书，圣人取法于它们。

校勘提示：此段是“河图”“洛书”观念的重要传世文献依据，但不能仅凭这句话断定后世九宫数图的具体形态在此时已经固定。"""),
    Entry("相关古籍", "《尚书·洪范》九畴", "《洪范》原文节录；与洛书九宫的对应关系属于后世解释史议题。", """惟十有三祀，王访于箕子。王乃言曰：“呜呼！箕子，惟天阴骘下民，相协厥居，我不知其彝伦攸叙。”

箕子乃言曰：“我闻在昔，鲧陻洪水，汩陈其五行；帝乃震怒，不畀洪范九畴，彝伦攸斁。鲧则殛死，禹乃嗣兴。天乃锡禹洪范九畴，彝伦攸叙。”

九畴次第：
一曰五行；二曰敬用五事；三曰农用八政；四曰协用五纪；五曰建用皇极；六曰乂用三德；七曰明用稽疑；八曰念用庶征；九曰向用五福，威用六极。

说明：此处展示《洪范》九畴文本结构。不能未经论证就将其直接称为洛书九宫图原文。"""),
    Entry("河图", "河图专题·资料边界", "当前为专题说明；古籍图版与历代图式尚待逐本核验。", """河图与洛书在后世易学中经常并称，但两者的图式、数字配置、文献解释史必须分开记录。

后续分别收录：
一、传世文献中“河出图”的原文与注疏；
二、宋代图书易学中的河图图式及版本；
三、刘牧、朱熹、蔡元定等人的论说；
四、图式异同、来源争议与现代研究。

在取得可核验的版本影像前，不补画所谓“古本原图”，也不把现代重绘图标成古籍原图。"""),
    Entry("版本档案", "敦煌本《周易》资料", "研究线索目录；原卷影像、释文及拼接关系仍需逐项核验。", """重点档案线索：
• 敦煌《周易正义》188071：傅斯年图书馆数字资源。
• P.2530：王弼注《周易》残卷，涉及噬嗑至离卦段落。
• P.2532：王弼注《周易》残卷，涉及解至益卦段落。
• P.2616、P.2619、P.3683、P.3640等：需继续核对原卷图像及拼接关系。
• S.5735+P.2617：含《经典释文》及《易略例》相关残片。

残片拼接关系、卷次、年代判断和释文均须保留来源及置信程度。未经原图核验的 OCR 不作为底本文字。""")
)
