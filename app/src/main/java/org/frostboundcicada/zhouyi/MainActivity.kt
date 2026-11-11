package org.frostboundcicada.zhouyi

import android.os.Bundle
import android.graphics.Typeface
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.TopAppBar as MiuixTopAppBar
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

private data class Entry(val section: String, val title: String, val source: String, val body: String, val translation: String? = null)

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
    var uiScale by remember { mutableFloatStateOf(prefs.getFloat("ui_scale", 1.0f)) }
    var fontScale by remember { mutableFloatStateOf(prefs.getFloat("font_scale", 1.0f)) }
    var fontUri by remember { mutableStateOf(prefs.getString("font_uri", null)) }
    val customTypeface = remember(fontUri) {
        if (fontUri.isNullOrBlank()) null else runCatching {
            val uri = Uri.parse(fontUri)
            context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                Typeface.Builder(descriptor.fileDescriptor).build()
            }
        }.getOrNull()
    }
    val appFontFamily = remember(customTypeface) {
        customTypeface?.let { FontFamily(it) } ?: FontFamily.Default
    }
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

    val baseDensity = LocalDensity.current
    val scaledTypography = remember(appFontFamily) {
        val base = Typography()
        Typography(
            displayLarge = base.displayLarge.copy(fontFamily = appFontFamily),
            displayMedium = base.displayMedium.copy(fontFamily = appFontFamily),
            displaySmall = base.displaySmall.copy(fontFamily = appFontFamily),
            headlineLarge = base.headlineLarge.copy(fontFamily = appFontFamily),
            headlineMedium = base.headlineMedium.copy(fontFamily = appFontFamily),
            headlineSmall = base.headlineSmall.copy(fontFamily = appFontFamily),
            titleLarge = base.titleLarge.copy(fontFamily = appFontFamily),
            titleMedium = base.titleMedium.copy(fontFamily = appFontFamily),
            titleSmall = base.titleSmall.copy(fontFamily = appFontFamily),
            bodyLarge = base.bodyLarge.copy(fontFamily = appFontFamily),
            bodyMedium = base.bodyMedium.copy(fontFamily = appFontFamily),
            bodySmall = base.bodySmall.copy(fontFamily = appFontFamily),
            labelLarge = base.labelLarge.copy(fontFamily = appFontFamily),
            labelMedium = base.labelMedium.copy(fontFamily = appFontFamily),
            labelSmall = base.labelSmall.copy(fontFamily = appFontFamily)
        )
    }
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = baseDensity.density * uiScale,
            fontScale = baseDensity.fontScale * fontScale / uiScale
        )
    ) {
        MiuixTheme(controller = controller) {
            MaterialTheme(colorScheme = scheme, typography = scaledTypography) {
                MainScreen(
                    themeMode = themeMode,
                    onThemeChange = {
                        themeMode = it
                        prefs.edit().putString("theme_mode", it).apply()
                    },
                    uiScale = uiScale,
                    onUiScaleChange = {
                        uiScale = it
                        prefs.edit().putFloat("ui_scale", it).apply()
                    },
                    fontScale = fontScale,
                    onFontScaleChange = {
                        fontScale = it
                        prefs.edit().putFloat("font_scale", it).apply()
                    },
                    fontUri = fontUri,
                    onFontUriChange = {
                        fontUri = it
                        prefs.edit().putString("font_uri", it).apply()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(
    themeMode: String,
    onThemeChange: (String) -> Unit,
    uiScale: Float,
    onUiScaleChange: (Float) -> Unit,
    fontScale: Float,
    onFontScaleChange: (Float) -> Unit,
    fontUri: String?,
    onFontUriChange: (String?) -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    var category by remember { mutableStateOf("全部") }
    var query by remember { mutableStateOf("") }
    var selectedEntry by remember { mutableStateOf<Entry?>(null) }
    val libraryListState = rememberLazyListState()

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
    BackHandler(enabled = selectedEntry != null) { selectedEntry = null }

    if (selectedEntry != null) {
        ReadingPage(entry = selectedEntry!!, onBack = { selectedEntry = null })
    } else Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MiuixTopAppBar(
                title = titles[tab],
                largeTitle = if (tab == 0) "周易数字典藏" else titles[tab],
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
            Box(
                modifier = Modifier.fillMaxWidth().height(84.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                FloatingGlassNavigationBar(tab = tab) { next ->
                    tab = next
                    category = "全部"
                    query = ""
                }
            }
        }
    ) { padding ->
        if (tab == 3) {
            SettingsPage(
                themeMode = themeMode,
                onThemeChange = onThemeChange,
                uiScale = uiScale,
                onUiScaleChange = onUiScaleChange,
                fontScale = fontScale,
                onFontScaleChange = onFontScaleChange,
                fontUri = fontUri,
                onFontUriChange = onFontUriChange,
                modifier = Modifier.padding(padding)
            )
        } else if (tab == 0) {
            HomePage(
                onOpenCategory = { name ->
                    category = name
                    tab = if (name == "易经" || name == "十翼") 1 else 2
                    query = ""
                },
                modifier = Modifier.padding(padding)
            )
        } else {
            LibraryPage(
                tab, category, { category = it }, query, { query = it },
                visible, { selectedEntry = it }, libraryListState, Modifier.padding(padding)
            )
        }
    }


}

@Composable
private fun FloatingGlassNavigationBar(tab: Int, onSelect: (Int) -> Unit) {
    val icons = listOf(
        Icons.Filled.Home, Icons.Filled.List, Icons.Filled.Info, Icons.Filled.Settings
    )
    val labels = listOf("典藏", "卦序", "专题", "设置")
    val shape = RoundedCornerShape(30.dp)
    Row(
        modifier = Modifier
            .widthIn(max = 390.dp)
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 8.dp)
            .shadow(20.dp, shape)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.76f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.90f)
                    )
                )
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.34f),
                shape
            )
            .padding(horizontal = 7.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        labels.forEachIndexed { index, label ->
            val selected = tab == index
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(23.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)
                        else Color.Transparent
                    )
                    .clickable { onSelect(index) }
                    .padding(horizontal = 7.dp, vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = icons[index],
                    contentDescription = label,
                    tint = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(21.dp)
                )
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadingPage(entry: Entry, onBack: () -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(entry.title, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(entry.section, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回目录")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Text(entry.title, style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(10.dp))
                Text(entry.source, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (entry.section == "洛书") {
                item { LuoshuIllustration(entry.title) }
            }
            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                Text("正文", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                SelectionContainer {
                    Text(
                        entry.body,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Serif, lineHeight = 32.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            if (entry.translation != null) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("逐段译文", style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Text(entry.translation, modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 30.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("收录与校勘说明", style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold)
                        Text(
                            "请以条目所列底本和版本说明为准。目录占位内容不代表正文已经完整收录；缺文、残字及未经核验的译文不会根据上下文补造。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("返回目录")
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
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val categories = when (tab) {
        1 -> listOf("全部", "易经", "十翼")
        2 -> listOf("全部", "河图", "洛书", "版本档案", "相关古籍")
        else -> listOf("全部", "易经", "十翼", "河图", "洛书", "版本档案", "相关古籍")
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { name ->
                    FilterChip(
                        selected = category == name,
                        onClick = { onCategoryChange(name) },
                        label = { Text(name, maxLines = 1) },
                        shape = RoundedCornerShape(50)
                    )
                }
            }
        }
        if (!(tab == 0 && category == "全部" && query.isBlank())) {
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
private fun HomePage(
    onOpenCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        item {
            val shape = RoundedCornerShape(30.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(18.dp, shape)
                    .clip(shape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.96f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
                            )
                        )
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.30f), shape)
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        "离线古籍典藏 · 持续整理",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    "周易数字典藏",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "以《周易》经传为中心，连接历代版本、相关古籍与易学专题。",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 27.sp
                )
                Text(
                    "原文优先  ·  版本可查  ·  异文并列  ·  缺文不补",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("开始探索", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                Text("选择一个分类进入目录，不在首页直接展开正文。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item { CategoryGrid(onOpenCategory) }
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
                )
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("典藏原则", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Text(
                        "区分经文、传文与后世注释；残缺内容如实标记，不以推测补齐。译文、异文和图版均注明可核验的来源。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 21.sp
                    )
                }
            }
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
                        modifier = Modifier.weight(1f).heightIn(min = 112.dp)
                            .clickable { onCategoryChange(name) },
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                        shape = RoundedCornerShape(22.dp),
                        tonalElevation = 1.dp,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)
                        )
                    ) {
                        Column(
                            Modifier.padding(15.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.78f),
                                shape = RoundedCornerShape(13.dp)
                            ) {
                                Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                                    Text(glyph, style = MaterialTheme.typography.titleMedium,
                                        fontFamily = FontFamily.Serif,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                            Text(name, fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleSmall)
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
private fun SettingsPage(
    themeMode: String,
    onThemeChange: (String) -> Unit,
    uiScale: Float,
    onUiScaleChange: (Float) -> Unit,
    fontScale: Float,
    onFontScaleChange: (Float) -> Unit,
    fontUri: String?,
    onFontUriChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var fontMessage by remember { mutableStateOf<String?>(null) }
    val fontPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                    Typeface.Builder(descriptor.fileDescriptor).build()
                } ?: error("无法打开字体文件")
                onFontUriChange(uri.toString())
                fontMessage = "字体已载入。支持 TTF、OTF、TTC、OTC。"
            }.onFailure {
                fontMessage = "无法载入该字体：${it.message ?: "格式不受支持"}"
            }
        }
    }
    val fontName = remember(fontUri) {
        if (fontUri.isNullOrBlank()) "系统默认字体" else runCatching {
            val uri = Uri.parse(fontUri)
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                } ?: "已导入字体"
        }.getOrDefault("已导入字体")
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("显示与排版", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        item {
            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp),
                tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("界面大小", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        Text("${(uiScale * 100).toInt()}%", color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold)
                    }
                    Text("调整卡片、间距、图标和导航等界面元素的整体大小。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Slider(value = uiScale, onValueChange = onUiScaleChange,
                        valueRange = 0.85f..1.20f, steps = 6)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("紧凑", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("宽松", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("字体大小", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        Text("${(fontScale * 100).toInt()}%", color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold)
                    }
                    Text("独立调整所有页面的文字显示比例，不改变界面元素尺寸。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Slider(value = fontScale, onValueChange = onFontScaleChange,
                        valueRange = 0.85f..1.40f, steps = 10)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("小", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("大", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp),
                tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("字体", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(fontName, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text("支持导入 TTF、OTF、TTC、OTC 字体文件。字体仅保存在本机应用设置中，不会上传。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { fontPicker.launch(arrayOf("*/*")) }) {
                            Text("导入字体")
                        }
                        OutlinedButton(onClick = {
                            onFontUriChange(null)
                            fontMessage = "已恢复系统默认字体。"
                        }, enabled = fontUri != null) {
                            Text("恢复默认")
                        }
                    }
                    if (fontMessage != null) {
                        Text(fontMessage!!, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        item {
            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp),
                tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("主题模式", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("选择应用外观，也可以跟随系统自动切换。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("system" to "跟随系统", "light" to "浅色", "dark" to "深色").forEach { (value, label) ->
                            FilterChip(selected = themeMode == value,
                                onClick = { onThemeChange(value) }, label = { Text(label) })
                        }
                    }
                }
            }
        }
        item {
            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp),
                tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("典藏原则", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("缺文不补、异文并列、译文标明依据；未经原图核验的 OCR 不作为底本文字。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

private fun buildHexagramIndex(): List<Entry> = listOf(
    Entry("易经", "上经 · 第1卦 · 乾", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "乾：元亨。利貞。\n初九：潛龍勿用。\n九二：見龍在田，利見大人。\n九三：君子終日乾乾，夕惕若；厲，无咎。\n九四：或躍在淵，无咎。\n九五：飛龍在天，利見大人。\n上九：亢龍，有悔。\n用九：見羣龍无首，吉。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第2卦 · 坤", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "坤：元亨。利牝馬之貞。君子有攸往，先迷後得主。利西南得朋，東北喪朋。安貞，吉。\n初六：履霜，堅冰至。\n六二：直方大，不習无不利。\n六三：含章，可貞。或從王事，无成有終。\n六四：括囊，无咎无譽。\n六五：黃裳，元吉。\n上六：龍戰于野，其血玄黃。\n用六：利永貞。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第3卦 · 屯", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "屯：元亨，利貞。勿用有攸往，利建侯。\n初九：磐桓，利居貞，利建侯。\n六二：屯如邅如，乘馬班如，匪寇婚媾，女子貞不字，十年乃字。\n六三：即鹿无虞，惟入于林中，君子幾不如舍，往吝。\n六四：乘馬班如，求婚媾，往，吉无不利。\n九五：屯其膏；小貞吉，大貞凶。\n上六：乘馬班如，泣血漣如。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第4卦 · 蒙", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "蒙：亨。匪我求童蒙，童蒙求我。初筮告，再三瀆，瀆則不告。利貞。\n初六：發蒙，利用刑人，用說桎梏，以往吝。\n九二：包蒙吉，納婦吉，子克家。\n六三：勿用取女，見金夫，不有躬，无攸利。\n六四：困蒙，吝。\n六五：童蒙，吉。\n上九：擊蒙，不利為寇，利禦寇。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第5卦 · 需", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "需：有孚，光亨。貞吉，利涉大川。\n初九：需于郊，利用恆，无咎。\n九二：需于沙，小有言，終吉。\n九三：需于泥，致寇至。\n六四：需于血，出自穴。\n九五：需于酒食，貞吉。\n上六：入于穴，有不速之客三人來，敬之終吉。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第6卦 · 讼", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "訟：有孚，窒，惕，中吉，終凶。利見大人，不利涉大川。\n初六：不永所事，小有言，終吉。\n九二：不克訟，歸而逋，其邑人三百戶无眚。\n六三：食舊德，貞厲，終吉。或從王事，无成。\n九四：不克訟，復即命渝，安貞吉。\n九五：訟，元吉。\n上九：或錫之鞶帶，終朝三褫之。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第7卦 · 师", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "師：貞丈人吉，无咎。\n初六：師出以律，否臧，凶。\n九二：在師中吉，无咎；王三錫命。\n六三：師或輿尸，凶。\n六四：師左次，无咎。\n六五：田有禽，利執言，无咎。長子帥師，弟子輿尸，貞凶。\n上六：大君有命，開國承家，小人勿用。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第8卦 · 比", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "比：吉。原筮元永貞，无咎。不寧方來，後夫凶。\n初六：有孚，比之，无咎。有孚盈缶，終來有它，吉。\n六二：比之自內，貞吉。\n六三：比之匪人。\n六四：外比之，貞吉。\n九五：顯比。王用三驅，失前禽，邑人不誡，吉。\n上六：比之无首，凶。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第9卦 · 小畜", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "小畜：亨。密雲不雨，自我西郊。\n初九：復自道，何其咎，吉。\n九二：牽復，吉。\n九三：輿說輻，夫妻反目。\n六四：有孚，血去惕出，无咎。\n九五：有孚攣如，富以其鄰。\n上九：既雨既處，尚德載，婦貞厲，月幾望，君子征凶。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第10卦 · 履", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "履虎尾，不咥人，亨。\n初九：素履，往无咎。\n九二：履道坦坦，幽人貞吉。\n六三：眇能視，跛能履，履虎尾，咥人，凶。武人為于大君。\n九四：履虎尾，愬愬終吉。\n九五：夬履，貞厲。\n上九：視履考祥，其旋元吉。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第11卦 · 泰", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "泰：小往大來，吉亨。\n初九：拔茅茹以其彙，征吉。\n九二：包荒。用馮河，不遐遺；朋亡。得尚于中行。\n九三：无平不陂，无往不復，艱貞无咎。勿恤其孚，于食有福。\n六四：翩翩，不富以其鄰；不戒以孚。\n六五：帝乙歸妹，以祉，元吉。\n上六：城復于隍，勿用師，自邑告命，貞吝。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第12卦 · 否", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "否之匪人，不利君子貞，大往小來。\n初六，拔茅茹以其彙，貞吉。亨。\n六二，包承，小人吉，大人否。亨。\n六三，包羞。\n九四，有命，无咎，疇離祉。\n九五，休否，大人吉。其亡其亡，繫于苞桑。\n上九，傾否，先否後喜。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第13卦 · 同人", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "同人于野，亨。利涉大川，利君子貞。\n初九：同人于門，無咎。\n六二：同人于宗，吝。\n九三：伏戎于莽，升其高陵，三歲不興。\n九四：乘其墉，弗克，攻吉。\n九五：同人，先號啕而后笑。大師克相遇。\n上九：同人于郊，無悔。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第14卦 · 大有", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "大有：元亨。\n初九：无交害，匪咎，艱則无咎。\n九二：大車以載，有攸往，无咎。\n九三：公用亨于天子，小人弗克。\n九四：匪其彭，无咎。\n六五：厥孚交如，威如；吉。\n上九：自天佑之，吉无不利。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第15卦 · 谦", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "謙：亨，君子有終。\n初六：謙謙君子，用涉大川，吉。\n六二：鳴謙，貞吉。\n九三：勞謙君子，有終吉。\n六四：无不利，撝謙。\n六五：不富，以其鄰，利用侵伐，无不利。\n上六：鳴謙，利用行師，征邑國。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第16卦 · 豫", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "豫：利建侯行師。\n初六：鳴豫，凶。\n六二：介于石，不終日，貞吉。\n六三：盱豫，悔。遲有悔。\n九四：由豫，大有得。勿疑。朋盍簪。\n六五：貞疾，恆不死。\n上六：冥豫，成有渝，无咎。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第17卦 · 随", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "隨：元亨。利貞。无咎。\n初九：官有渝，貞吉。出門交有功。\n六二：系小子，失丈夫。\n六三：系丈夫，失小子。隨，有求得利，居貞。\n九四：隨有獲，貞凶。有孚在道，以明，何咎。\n九五：孚于嘉，吉。\n上六：拘系之，乃從維之。王用亨于西山。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第18卦 · 蛊", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "蠱：元亨。利涉大川。先甲三日，後甲三日。\n初六：幹父之蠱，有子考，无咎，厲終吉。\n九二：幹母之蠱，不可貞。\n九三：幹父之蠱，小有悔，无大咎。\n六四：裕父之蠱，往見吝。\n六五：幹父之蠱，用譽。\n上九：不事王侯，高尚其事。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第19卦 · 临", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "臨：元亨。利貞。至于八月有凶。\n初九：咸臨，貞吉。\n九二：咸臨，吉无不利。\n六三：甘臨，无攸利。既憂之，无咎。\n六四：至臨，无咎。\n六五：知臨，大君之宜，吉。\n上六：敦臨，吉无咎。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第20卦 · 观", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "觀：盥而不荐，有孚顒若。\n初六：童觀，小人无咎，君子吝。\n六二：窺觀，利女貞。\n六三：觀我生，進退。\n六四：觀國之光，利用賓于王。\n九五：觀我生，君子无咎。\n上九：觀其生，君子无咎。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第21卦 · 噬嗑", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "噬嗑：亨。利用獄。\n初九：屨校滅趾，无咎。\n六二：噬膚滅鼻，无咎。\n六三：噬臘肉，遇毒；小吝，无咎。\n九四：噬乾胏，得金矢，利艱貞，吉。\n六五：噬乾肉，得黃金，貞厲，无咎。\n上九：何校滅耳，凶。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第22卦 · 贲", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "賁：亨。小利有攸往。\n初九：賁其趾，舍車而徒。\n六二：賁其須。\n九三：賁如濡如，永貞吉。\n六四：賁如皤如，白馬翰如，匪寇婚媾。\n六五：賁於丘園，束帛戔戔，吝，終吉。\n上九：白賁，无咎。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第23卦 · 剥", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "剝：不利。有攸往。\n初六：剝牀以足，蔑貞凶。\n六二：剝牀以辨，蔑貞凶。\n六三：剝之，无咎。\n六四：剝牀以膚，凶。\n六五：貫魚，以宮人寵，无不利。\n上九：碩果不食，君子得輿，小人剝廬。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第24卦 · 复", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "復：亨。出入无疾，朋來无咎。反復其道，七日來復，利有攸往。\n初九：不復遠，无袛悔，元吉。\n六二：休復，吉。\n六三：頻復，厲无咎。\n六四：中行獨復。\n六五：敦復，无悔。\n上六：迷復，凶，有災眚。用行師，終有大敗，以其國君，凶；至于十年，不克征。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第25卦 · 无妄", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "无妄：元亨。利貞。其匪正有眚，不利有攸往。\n初九：无妄，往吉。\n六二：不耕穫，不菑畬，則利有攸往。\n六三：无妄之災，或系之牛，行人之得，邑人之災。\n九四：可貞，无咎。\n九五：无妄之疾，勿藥有喜。\n上九：无妄，行有眚，无攸利。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第26卦 · 大畜", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "大畜：利貞，不家食吉，利涉大川。\n初九：有厲利已。\n九二：輿說輹。\n九三：良馬逐，利艱貞。曰閑輿衛，利有攸往。\n六四：童牛之牿，元吉。\n六五：豶豕之牙，吉。\n上九：何天之衢，亨。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第27卦 · 颐", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "頤：貞吉。觀頤，自求口實。\n初九：舍爾靈龜，觀我朵頤，凶。\n六二：顛頤，拂經，于丘頤，征凶。\n六三：拂頤，貞凶，十年勿用，无攸利。\n六四：顛頤吉，虎視眈眈，其欲逐逐，无咎。\n六五：拂經，居貞吉，不可涉大川。\n上九：由頤，厲吉，利涉大川。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第28卦 · 大过", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "大過：棟橈，利有攸往，亨。\n初六：藉用白茅，无咎。\n九二：枯楊生稊，老夫得其女妻，无不利。\n九三：棟橈，凶。\n九四：棟隆，吉。有它吝。\n九五：枯楊生華，老婦得其士夫，无咎无譽。\n上六：過涉滅頂，凶，无咎。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第29卦 · 坎", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "習坎：有孚，維心亨。行有尚。\n初六：習坎，入于坎窞，凶。\n九二：坎有險，求小得。\n六三：來之坎坎，險且枕，入于坎窞，勿用。\n六四：樽酒簋貳，用缶，納約自牖，終无咎。\n九五：坎不盈，祗既平，无咎。\n上六：係用徽纆，寘于叢棘，三歲不得，凶。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "上经 · 第30卦 · 离", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "離：利貞。亨。畜牝牛，吉。\n初九：履錯然，敬之无咎。\n六二：黃離，元吉。\n九三：日昃之離，不鼓缶而歌，則大耋之嗟，凶。\n九四：突如其來如，焚如，死如，棄如。\n六五：出涕沱若，戚嗟若，吉。\n上九：王用出征，有嘉折首，獲匪其醜，无咎。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第31卦 · 咸", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "咸：亨。利貞。取女吉。\n初六：咸其拇。\n六二：咸其腓，凶，居吉。\n九三：咸其股，執其隨，往吝。\n九四：貞吉悔亡，憧憧往來，朋從爾思。\n九五：咸其脢，无悔。\n上六：咸其輔，頰，舌。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第32卦 · 恒", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "恆：亨，无咎。利貞，利有攸往。\n初六：浚恆，貞凶，无攸利。\n九二：悔亡。\n九三：不恆其德，或承之羞，貞吝。\n九四：田无禽。\n六五：恆其德，貞，婦人吉，夫子凶。\n上六：振恆，凶。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第33卦 · 遁", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "遯：亨。小利貞。\n初六：遯尾，厲，勿用有攸往。\n六二：執之用黃牛之革，莫之勝說。\n九三：系遯，有疾厲，畜臣妾吉。\n九四：好遯君子吉，小人否。\n九五：嘉遯，貞吉。\n上九：肥遯，无不利。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第34卦 · 大壮", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "大壯：利貞。\n初九：壯于趾，征凶，有孚。\n九二：貞吉。\n九三：小人用壯，君子用罔，貞厲。羝羊觸藩，羸其角。\n九四：貞吉悔亡，藩決不羸，壯于大輿之輹。\n六五：喪羊于易，无悔。\n上六：羝羊觸藩，不能退，不能遂，无攸利，艱則吉。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第35卦 · 晋", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "晉：康侯用錫馬蕃庶，晝日三接。\n初六：晉如，摧如，貞吉。罔孚，裕无咎。\n六二：晉如，愁如，貞吉。受茲介福，于其王母。\n六三：眾允，悔亡。\n九四：晉如鼫鼠，貞厲。\n六五：悔亡，失得勿恤，往吉无不利。\n上九：晉其角，維用伐邑，厲吉无咎，貞吝。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第36卦 · 明夷", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "明夷：利艱貞。\n初九：明夷于飛，垂其翼。君子于行，三日不食，有攸往，主人有言。\n六二：明夷，夷于左股，用拯馬壯，吉。\n九三：明夷于南狩，得其大首，不可疾貞。\n六四：入于左腹，獲明夷之心，于出門庭。\n六五：箕子之明夷，利貞。\n上六：不明晦，初登于天，后入于地。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第37卦 · 家人", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "家人：利女貞。\n初九：閑有家，悔亡。\n六二：无攸遂，在中饋，貞吉。\n九三：家人嗃嗃，悔厲吉；婦子嘻嘻，終吝。\n六四：富家，大吉。\n九五：王假有家，勿恤。吉。\n上九：有孚威如，終吉。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第38卦 · 睽", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "睽：小事吉。\n初九：悔亡，喪馬勿逐，自復；見惡人无咎。\n九二：遇主于巷，无咎。\n六三：見輿曳，其牛掣，其人天且劓，无初有終。\n九四：睽孤，遇元夫，交孚，厲无咎。\n六五：悔亡，厥宗噬膚，往何咎。\n上九：睽孤，見豕負涂，載鬼一車，先張之弧，后說之弧，匪寇婚媾，往遇雨則吉。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第39卦 · 蹇", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "蹇：利西南，不利東北；利見大人，貞吉。\n初六：往蹇，來譽。\n六二：王臣蹇蹇，匪躬之故。\n九三：往蹇來反。\n六四：往蹇來連。\n九五：大蹇朋來。\n上六：往蹇來碩，吉；利見大人。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第40卦 · 解", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "解：利西南，无所往，其來復吉。有攸往，夙吉。\n初六：无咎。\n九二：田獲三狐，得黃矢，貞吉。\n六三：負且乘，致寇至，貞吝。\n九四：解而拇，朋至斯孚。\n六五：君子維有解，吉；有孚于小人。\n上六：公用射隼，于高墉之上，獲之，无不利。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第41卦 · 损", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "損：有孚，元吉。无咎，可貞，利有攸往。曷之用？二簋可用享。\n初九：已事遄往，无咎，酌損之。\n九二：利貞，征凶，弗損益之。\n六三：三人行，則損一人；一人行，則得其友。\n六四：損其疾，使遄有喜，无咎。\n六五：或益之，十朋之龜弗克違，元吉。\n上九：弗損益之，无咎，貞吉，利有攸往，得臣无家。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第42卦 · 益", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "益：利有攸往。利涉大川。\n初九：利用為大作，元吉，无咎。\n六二：或益之，十朋之龜弗克違，永貞吉。王用享于帝，吉。\n六三：益之用凶事，无咎。有孚中行，告公用圭。\n六四：中行，告公從。利用為依遷國。\n九五：有孚惠心，勿問元吉。有孚惠我德。\n上九：莫益之，或擊之，立心勿恆，凶。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第43卦 · 夬", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "夬：揚于王庭，孚號，有厲，告自邑，不利即戎，利有攸往。\n初九：壯于前趾，往不勝為咎。\n九二：惕號，莫夜有戎，勿恤。\n九三：壯于頄，有凶。君子夬夬，獨行遇雨，若濡有慍，无咎。\n九四：臀无膚，其行次且。牽羊悔亡，聞言不信。\n九五：莧陸夬夬，中行无咎。\n上六：无號，終有凶。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第44卦 · 姤", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "姤：女壯，勿用取女。\n初六：系于金柅，貞吉，有攸往，見凶，羸豕孚踟躅。\n九二：包有魚，无咎，不利賓。\n九三：臀无膚，其行次且，厲，无大咎。\n九四：包无魚，起凶。\n九五：以杞包瓜，含章，有隕自天。\n上九：姤其角，吝，无咎。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第45卦 · 萃", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "萃：亨。王假有廟，利見大人，亨。利貞。用大牲吉，利有攸往。\n初六：有孚不終，乃亂乃萃，若號一握為笑，勿恤，往无咎。\n六二：引吉，无咎，孚乃利用禴。\n六三：萃如，嗟如，无攸利，往无咎，小吝。\n九四：大吉，无咎。\n九五：萃有位，无咎。匪孚，元永貞，悔亡。\n上六：齎咨涕洟，无咎。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第46卦 · 升", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "升：元亨，用見大人，勿恤，南征吉。\n初六：允升，大吉。\n九二：孚乃利用禴，无咎。\n九三：升虛邑。\n六四：王用亨于岐山，吉无咎。\n六五：貞吉，升階。\n上六：冥升，利于不息之貞。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第47卦 · 困", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "困：亨，貞大人吉，无咎，有言不信。\n初六：臀困于株木，入于幽谷，三歲不覿。\n九二：困于酒食，朱紱方來，利用亨祀，征凶，无咎。\n六三：困于石，據于蒺藜，入于其宮，不見其妻，凶。\n九四：來徐徐，困于金車，吝，有終。\n九五：劓刖，困于赤紱，乃徐有說，利用祭祀。\n上六：困于葛藟，于臲卼，曰動悔。有悔，征吉。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第48卦 · 井", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "井：改邑不改井，无喪无得，往來井井。汔至亦未繘井。羸其瓶，凶。\n初六：井泥不食，舊井无禽。\n九二：井谷射鮒，瓮敝漏。\n九三：井渫不食，為我心惻，可用汲，王明，并受其福。\n六四：井甃，无咎。\n九五：井冽，寒泉食。\n上六：井收勿幕，有孚元吉。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第49卦 · 革", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "革：巳日乃孚，元亨。利貞。悔亡。\n初九：鞏用黃牛之革。\n六二：巳日乃革之，征吉，无咎。\n九三：征凶，貞厲，革言三就，有孚。\n九四：悔亡，有孚改命，吉。\n九五：大人虎變，未占有孚。\n上六：君子豹變，小人革面，征凶，居貞吉。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第50卦 · 鼎", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "鼎：元吉，亨。\n初六：鼎顛趾，利出否，得妾以其子，无咎。\n九二：鼎有實，我仇有疾，不我能即，吉。\n九三：鼎耳革，其行塞，雉膏不食，方雨虧悔，終吉。\n九四：鼎折足，覆公餗，其形渥，凶。\n六五：鼎黃耳金鉉，利貞。\n上九：鼎玉鉉，大吉，无不利。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第51卦 · 震", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "震：亨。震來虩虩，笑言啞啞。震驚百里，不喪匕鬯。\n初九：震來虩虩，后笑言啞啞，吉。\n六二：震來厲，億喪貝，躋于九陵，勿逐，七日得。\n六三：震蘇蘇，震行无眚。\n九四：震遂泥。\n六五：震往來厲，億无喪，有事。\n上六：震索索，視矍矍，征凶。震不于其躬，于其鄰，无咎。婚媾有言。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第52卦 · 艮", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "艮：艮其背，不獲其身，行其庭，不見其人，无咎。\n初六：艮其趾，无咎，利永貞。\n六二：艮其腓，不拯其隨，其心不快。\n九三：艮其限，列其夤，厲薰心。\n六四：艮其身，无咎。\n六五：艮其輔，言有序，悔亡。\n上九：敦艮，吉。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第53卦 · 渐", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "漸：女歸吉，利貞。\n初六：鴻漸于干，小子厲，有言，无咎。\n六二：鴻漸于磐，飲食衎衎，吉。\n九三：鴻漸于陸，夫征不復，婦孕不育，凶；利禦寇。\n六四：鴻漸于木，或得其桷，无咎。\n九五：鴻漸于陵，婦三歲不孕，終莫之勝，吉。\n上九：鴻漸于陸，其羽可用為儀，吉。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第54卦 · 归妹", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "歸妹：征凶，无攸利。\n初九：歸妹以娣，跛能履，征吉。\n九二：眇能視，利幽人之貞。\n六三：歸妹以須，反歸以娣。\n九四：歸妹愆期，遲歸有時。\n六五：帝乙歸妹，其君之袂，不如其娣之袂良，月幾望，吉。\n上六：女承筐无實，士刲羊无血，无攸利。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第55卦 · 丰", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "豐：亨。王假之，勿憂，宜日中。\n初九：遇其配主，雖旬无咎，往有尚。\n六二：豐其蔀，日中見斗，往得疑疾，有孚發若，吉。\n九三：豐其沛，日中見沫，折其右肱，无咎。\n九四：豐其蔀，日中見斗，遇其夷主，吉。\n六五：來章，有慶譽，吉。\n上六：豐其屋，蔀其家，窺其戶，闃其无人，三歲不觌，凶。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第56卦 · 旅", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "旅：小亨，旅貞吉。\n初六：旅瑣瑣，斯其所取災。\n六二：旅即次，懷其資，得童僕貞。\n九三：旅焚其次，喪其童僕，貞厲。\n九四：旅于處，得其資斧，我心不快。\n六五：射雉一矢亡，終以譽命。\n上九：鳥焚其巢，旅人先笑后號咷。喪牛于易，凶。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第57卦 · 巽", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "巽：小亨。利有攸往。利見大人。\n初六：進退，利武人之貞。\n九二：巽在牀下，用史巫紛若，吉无咎。\n九三：頻巽，吝。\n六四：悔亡，田獲三品。\n九五：貞吉悔亡，无不利。无初有終，先庚三日，后庚三日，吉。\n上九：巽在牀下，喪其資斧，貞凶。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第58卦 · 兑", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "兌：亨。利貞。\n初九：和兌，吉。\n九二：孚兌，吉，悔亡。\n六三：來兌，凶。\n九四：商兌，未寧，介疾有喜。\n九五：孚于剝，有厲。\n上六：引兌。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第59卦 · 涣", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "渙：亨。王假有廟，利涉大川，利貞。\n初六：用拯馬壯，吉。\n九二：渙奔其机，悔亡。\n六三：渙其躬，无悔。\n六四：渙其群，元吉。渙有丘，匪夷所思。\n九五：渙汗其大號，渙王居，无咎。\n上九：渙其血，去逖出，无咎。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第60卦 · 节", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "節：亨。苦節不可貞。\n初九：不出戶庭，无咎。\n九二：不出門庭，凶。\n六三：不節若，則嗟若，无咎。\n六四：安節，亨。\n九五：甘節，吉；往有尚。\n上六：苦節，貞凶，悔亡。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第61卦 · 中孚", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "中孚：豚魚吉，利涉大川，利貞。\n初九：虞吉，有他不燕。\n九二：鳴鶴在陰，其子和之，我有好爵，吾與爾靡之。\n六三：得敵，或鼓或罷，或泣或歌。\n六四：月几望，馬匹亡，无咎。\n九五：有孚攣如，无咎。\n上九：翰音登于天，貞凶。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第62卦 · 小过", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "小過：亨。利貞。可小事，不可大事。飛鳥遺之音，不宜上宜下，大吉。\n初六：飛鳥以凶。\n六二：過其祖，遇其妣；不及其君，遇其臣；无咎。\n九三：弗過防之，從或戕之，凶。\n九四：无咎，弗過遇之。往厲必戒，勿用永貞。\n六五：密云不雨，自我西郊，公弋取彼在穴。\n上六：弗遇過之，飛鳥離之，凶，是謂災眚。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第63卦 · 既济", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "既濟：亨小。利貞。初吉終亂。\n初九：曳其輪，濡其尾，无咎。\n六二：婦喪其茀，勿逐，七日得。\n九三：高宗伐鬼方，三年克之，小人勿用。\n六四：繻有衣袽，終日戒。\n九五：東鄰殺牛，不如西鄰之禴祭，實受其福。\n上六：濡其首，厲。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。"),
    Entry("易经", "下经 · 第64卦 · 未济", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "未濟：亨。小狐汔濟，濡其尾，无攸利。\n初六：濡其尾，吝。\n九二：曳其輪，貞吉。\n六三：未濟，征凶，利涉大川。\n九四：貞吉，悔亡，震用伐鬼方，三年有賞于大國。\n六五：貞吉，无悔，君子之光，有孚，吉。\n上九：有孚于飲酒，无咎，濡其首，有孚失是。", "暂无可靠译文；本条已录入通行本经文，逐段译文待核定后补入。")
)

private fun buildWingsIndex(): List<Entry> =
    listOf("彖传上","彖传下","象传上","象传下","系辞上传","系辞下传","文言传","说卦传","序卦传","杂卦传").map { name ->
        Entry("十翼", "《$name》", "十翼篇目目录；全文尚待按可靠底本逐段录入。",
            "本条为篇目目录，不是全文。收录时将区分经文、传文层次，标注章次、版本异文及译文依据。")
    }

private fun buildEntries(): List<Entry> = listOf(
    Entry("洛书", "洛书九宫数图（后世通行图式）", "现代常见九宫排列示意。它是后世定型并广泛流传的图式，不应倒推为先秦《尚书·洪范》或《周易·系辞》原图。", """通行排列：

四　九　二
三　五　七
八　一　六

横列、纵列与两条对角线的和均为十五。此图常用于后世术数、历法与方位解释；其具体定型过程须与先秦“洛书”文字记载分开考察。

图像说明：上方为本应用绘制的现代矢量示意图，仅呈现通行九宫数字排列，不是古籍原图，也不代表已经证明其源自先秦。"""),
    Entry("洛书", "刘牧《洛书五行生数》第五十三图", "《易数钩隐图》卷下第五十三图；北宋刘牧图书易学体系。图式依据山东大学《周易研究》2024年第4期论文所刊图13及文中释读重绘。", """刘牧将洛书理解为五行生成数系统。其生数为：

一曰水，二曰火，三曰木，四曰金，五曰土。

这五个数是五行生数。刘牧强调此数属于“已交之数”，与其河图系统所表达的未交之象不同。此处按原文次第列出，不擅自补定图中未能由文字确认的方位。

图像说明：应用绘制的现代矢量资料图，展示五行与生数对应关系；不是古籍原图。原论文图13可用于对照核查。"""),
    Entry("洛书", "刘牧《洛书五行成数》第五十四图", "《易数钩隐图》卷下第五十四图；依据刘牧“以土数足之”的解释及山东大学《周易研究》2024年第4期论文图14重绘。", """刘牧以天五与五行生数相合，说明五行成数：

水：一与五合，成数六；
火：二与五合，成数七；
木：三与五合，成数八；
金：四与五合，成数九；
土：五与五合，成数十。

因此，五行成数为六、七、八、九、十。刘牧把生数与成数分图表达，并未在《易数钩隐图》中将二图合成一张完整的洛书总图。

图像说明：应用绘制的现代矢量资料图，以配对方式呈现生数、成数；不冒称原刻图。"""),
    Entry("洛书", "刘牧《十日生五行并相生》第五十五图", "《易数钩隐图》卷下第五十五图及其图后文字；通行数字文本据《四库全书》本与道藏本相关整理页面互校。", """天一、地六，地二、天七，天三、地八，地四、天九，天五、地十，合而生水、火、木、金、土。

十日者，刚日也。

相生者：金生水，水生木，木生火，火生土，土生金也。

相克者：金克木，木克土，土克水，水克火，火克金也。

说明：本段把天地数配合与五行生成联系起来。相生次序和相克次序照录原文；“十日”一词的具体训释不在此处擅自扩写。"""),
    Entry("洛书", "刘牧《易数钩隐图》洛书说", "《易数钩隐图》卷下“河图第四十九”图后问答；宋刘牧撰。原文可据《四库全书》本卷下及《道藏》本相关数字整理核对。", """或问曰：“洛书云：一曰水，二曰火，三曰木，四曰金，五曰土。则与龙图五行之数之位不偶者，何也？”

答曰：“此谓陈其生数也。且虽则陈其生数，乃是已交之数也。下篇分土王四季，则备其成数矣。且夫洛书、九畴，惟出于五行之数，故先陈其已交之生数，然后以土数足之，乃可见其成数也。”

整理说明：此处明确呈现刘牧对洛书的解释框架——先陈五行生数，再以中五配合说明成数。不同版本在“已／巳”“四季／四时”等字样上存在数字文本差异，正式校勘时须以具体底本影像为准。"""),
    Entry("洛书", "刘牧《龙图龟书论上》洛书相关论述", "《易数钩隐图》卷下卷末《龙图龟书论上》节录；宋刘牧撰。", """《易》曰：“河出图，洛出书，圣人则之。”

刘牧在论述中讨论了《尚书·洪范》九畴、汉儒关于禹受洛书的解释，以及河图、洛书在易学体系中的关系。他并不简单把九畴文本等同于一张固定的九宫数字图，而是试图说明圣人如何取法图书之象数。

其论述中有一段关键判断：“今河图相传于前代，其数自一至九，包四象八卦之义，而兼五行之数；洛书则惟五行生成之数也。”

校读提示：刘牧的说法属于北宋图书易学的理论建构。此处是刘牧本人的解释，不应改写成先秦经文的原始定义。关于神龟负书、伏羲画卦等叙事，须分别标记为经文、注疏或纬书传说。"""),
    Entry("洛书", "河图九数、洛书十数的名称易位", "依据刘牧《易数钩隐图》、李觏《删定易图论》相关材料及张克宾《论刘牧〈河图〉〈洛书〉说的架构与意蕴》（《周易研究》2024年第4期）。", """名称与数图关系曾发生重要变化：

刘牧系统：
• 河图：九宫九数图，四十五数；
• 洛书：五行生成数系统，天地十数合计五十五。

后世通行系统：
• 河图：天地五行生成数，五十五数；
• 洛书：九宫数图，四十五数。

因此，不能把今天常见的“河图五十五、洛书四十五”直接套用到刘牧《易数钩隐图》上，也不能把刘牧的名称配置误说成自古不变。

学术说明：北宋刘牧对图名作了与此前传统不同的配置；南宋以后围绕河九洛十、河十洛九形成持续争论，后经蔡元定、朱熹等人的论说，后世通行配置逐渐确立。具体传承环节仍应依各家原文分别核验。"""),
    Entry("洛书", "《洛书》与《洪范》九畴的关系", "《尚书·洪范》与宋代图书易学的解释关系专题；不将九畴次第直接等同于九宫数位。", """《洪范》叙述“天乃锡禹洪范九畴”，并列出九类治国纲目：

一曰五行；
二曰敬用五事；
三曰农用八政；
四曰协用五纪；
五曰建用皇极；
六曰乂用三德；
七曰明用稽疑；
八曰念用庶征；
九曰向用五福，威用六极。

文献层次说明：
《洪范》原文确有“九畴”及五行次第；《周易·系辞上传》另有“河出图，洛出书，圣人则之”；后世注家逐渐将二者关联，并发展出神龟负书、九宫数位等解释。三者之间存在解释史联系，但不是同一段原文，也不能仅凭“九畴”二字证明《洪范》原本就附有今天通行的九宫图。"""),
    Entry("洛书", "《周易·系辞上传》河图洛书原文", "通行本《系辞上传》第十一章；原文提及河图、洛书，没有直接列出九宫数字。", """是故天生神物，圣人则之；天地变化，圣人效之；天垂象，见吉凶，圣人象之；河出图，洛出书，圣人则之。

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
    Entry("相关古籍", "古籍图版总目录·朱熹《周易本义》九图", "图像来源：书格所载清康熙内府仿宋咸淳本，底本说明指向哈佛大学图书馆藏本。书格提供原色高清版与黑白版；其版权及可再利用范围须以馆藏及发布方条件为准。", """卷首原书图版目录，共九图：
一、《河图》；
二、《洛书》；
三、《伏羲八卦次序》；
四、《伏羲八卦方位》；
五、《伏羲六十四卦次序》；
六、《伏羲六十四卦方位》；
七、《文王八卦次序》；
八、《文王八卦方位》；
九、《卦变图》。

原书扫描入口：https://old.shuge.org/ebook/zhou-yi-ben-yi/
国家图书馆另藏宋咸淳元年吴革刊本，版本与康熙仿刻本须分开建档：https://www.shuge.org/view/zhou_yi_ben_yi/

图像状态：已找到整本扫描入口，九图具体页码及可打包授权仍待逐页核实；此条不把目录当作已下载图像。"""),
    Entry("相关古籍", "古籍图版总目录·元张理《易象图说》", "浙江大学图书馆数字化，Internet Archive 扫描；书目页标记原书公有领域。内篇卷上至卷下 139 页，外篇卷上至卷下 107 页；另有 1663 年摛藻堂版本的 151 页图像记录。", """已核实图版主题及原书页码：
• p.22 龙图天地未交之数；
• p.24 龙图天地交合之位；
• p.32 龙图生成数；
• p.34 洛书天地交错数；
• p.36 洛书纵横十五数；
• p.52 两仪生四象；
• p.57 八卦次序；
• p.61 先天八卦对待；
• p.65 后天八卦流行；
• p.69 先后天合德；
• p.71 伏羲六十四卦圆图；
• p.72 六十四卦循环图；
• p.86 八画卦；
• p.87 重卦图；
• p.98 六十四卦变通图；
• p.105 六十四卦应用图。

原书图像入口：https://sourcelibrary.org/book/yixiang-tushu-illustrated-explanation-of-yi-hexagram-images
插图浏览：https://sourcelibrary.org/gallery?bookId=6992cabdd4d545ae73fee906
浙江大学图书馆 CADAL 内篇扫描：https://commons.wikimedia.org/wiki/File:CADAL06065978_%E6%98%93%E8%B1%A1%E5%9C%96%E8%AA%AA%E5%85%A7%E7%AF%87%C2%B7%E5%8D%B7%E4%B8%8A~%E5%8D%B7%E4%B8%8B.djvu
外篇扫描：https://commons.wikimedia.org/wiki/File:CADAL06065979_%E6%98%93%E8%B1%A1%E5%9C%96%E8%AA%AA%E5%A4%96%E7%AF%87%C2%B7%E5%8D%B7%E4%B8%8A~%E5%8D%B7%E4%B8%8B.djvu

图像状态：已确认 22 幅插图目录；需逐图匹配正文并抽取对应原页，不能只保存封面。"""),
    Entry("相关古籍", "古籍图版总目录·明刘定之《易经图释》", "Wikimedia Commons 所藏《易经图释（一）》DjVu 扫描，来源 Internet Archive identifier 02071887.cn；页面标记 Public Domain。原书描述注明部分字迹不清、部分页面残缺。", """书首图表包括先天图、后天图等；书目提要说明其图表多与朱熹《周易本义》相同，但不列卦变图、河图、洛书。
图像扫描入口：https://commons.wikimedia.org/wiki/File:%E6%98%93%E7%B6%93%E5%9C%96%E9%87%8B(%E4%B8%80).djvu
电子文本与底本说明：https://ctext.org/wiki.pl?if=gb&remap=gb&res=472287

图像状态：第一册 138 页扫描可用；页面残缺处必须保留缺损状态。其余卷册图像仍待查找。"""),
    Entry("相关古籍", "古籍图版总目录·《正统道藏》本《周易图》三卷", "《正统道藏》第69至70册，洞真部灵图类，三卷；中国哲学书电子化计划提供馆藏影像入口及目录线索。作者未题，不能擅自归于某一位易学家。", """图题目录包括太极、周氏太极、郑氏太极贯一、河图、洛书、天地自然十五数、先天数、先天象、六十四卦阴阳倍乘、八卦纳甲、六十四卦生自两仪、六十四卦大象、序卦、杂卦、大衍数、十八变成卦、乾坤合律、四象八卦等。
馆藏影像入口：https://ctext.org/library.pl?if=gb&res=83810
图题及文本检索入口：https://ctext.org/wiki.pl?chapter=260323&if=gb&remap=gb

图像状态：已找到三卷馆藏影像入口及图题目录；须逐卷核验页图，不能把目录推测成图像已提取。"""),
    Entry("相关古籍", "古籍图版总目录·《易经》六十四卦版画", "Source Library 收录一部匿名《易经／断易天机》相关图像册，约 1368—1700 年，40 张扫描、36 幅图像；原书图像来自 Internet Archive，页面标记原件公有领域。书名及作者信息仍需谨慎处理。", """该册保存与六十四卦相关的叙事性木刻图像，部分图像可与具体卦名对应。Source Library 的审校记录指出其 OCR 有较多错误，因此这里只把它作为图像来源，不把 OCR 当作可靠正文。
原书图像与目录：https://sourcelibrary.org/zh/book/yi-jing-64-hexagram-diagrams-anonymous

图像状态：已确认 36 幅图像目录；逐幅核实卦名、页码和图像语境后，才可关联到六十四卦阅读页。"""),
    Entry("相关古籍", "古籍图版总目录·陈士元《易象钩解》", "明代陈士元易学著作，Source Library 收录卷一至卷二 151 页扫描，来源 Internet Archive，页面标记原书公有领域。", """该书以象解易，适合作为历代卦象图说与经文解释的参照。具体图版数量及各图所对应卦爻，须逐页查验原书，不能根据书名预设每页均有图。
原书扫描入口：https://sourcelibrary.org/book/vol-1-1782366422220

图像状态：已找到原书扫描入口；逐页图像编目待完成。"""),
    Entry("版本档案", "敦煌本《周易》资料", "研究线索目录；原卷影像、释文及拼接关系仍需逐项核验。", """重点档案线索：
• 敦煌《周易正义》188071：傅斯年图书馆数字资源。
• P.2530：王弼注《周易》残卷，涉及噬嗑至离卦段落。
• P.2532：王弼注《周易》残卷，涉及解至益卦段落。
• P.2616、P.2619、P.3683、P.3640等：需继续核对原卷图像及拼接关系。
• S.5735+P.2617：含《经典释文》及《易略例》相关残片。

残片拼接关系、卷次、年代判断和释文均须保留来源及置信程度。未经原图核验的 OCR 不作为底本文字。""")
)

@Composable
private fun LuoshuIllustration(title: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("图式示意", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            when {
                title.contains("九宫数图") -> {
                    val values = listOf(4, 9, 2, 3, 5, 7, 8, 1, 6)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        values.chunked(3).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                row.forEach { number ->
                                    Surface(
                                        modifier = Modifier.weight(1f).aspectRatio(1.12f),
                                        color = if (number == 5) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(14.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.30f)
                                        )
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(number.toString(),
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (number == 5) MaterialTheme.colorScheme.onPrimaryContainer
                                                    else MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Text("横、纵、斜向各线之和均为 15",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                title.contains("生数") || title.contains("成数") -> {
                    val isGenerating = title.contains("生数")
                    val values = if (isGenerating)
                        listOf("水" to "1", "火" to "2", "木" to "3", "金" to "4", "土" to "5")
                    else
                        listOf("水" to "6", "火" to "7", "木" to "8", "金" to "9", "土" to "10")
                    Text(if (isGenerating) "五行生数" else "五行成数",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        values.forEach { (element, number) ->
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 14.dp, horizontal = 3.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(number, style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(element, style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                    Text("此图仅列数与五行对应，不表示古图方位。",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("现代矢量重绘 · 非古籍原图",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
