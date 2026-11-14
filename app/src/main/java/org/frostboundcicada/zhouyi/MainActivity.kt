package org.frostboundcicada.zhouyi

import android.os.Bundle
import android.os.Build
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeroui.kit.components.Card as ZeroCard
import com.zeroui.kit.components.Text as ZeroText
import com.zeroui.kit.components.Icon as ZeroIcon
import com.zeroui.kit.components.Button as ZeroButton
import com.zeroui.kit.components.TextButton as ZeroTextButton
import com.zeroui.kit.components.TextField as ZeroTextField
import com.zeroui.kit.components.Slider as ZeroSlider
import com.zeroui.kit.components.ListRow
import com.zeroui.kit.components.HorizontalDivider as ZeroDivider
import com.zeroui.kit.components.rememberTopBarScrollBehavior
import com.zeroui.kit.components.ZeroNavigationBar
import com.zeroui.kit.components.NavBarItem
import com.zeroui.kit.components.IconButton as ZeroIconButton
import com.zeroui.kit.components.Scaffold as ZeroScaffold
import com.zeroui.kit.components.TopAppBar as ZeroTopAppBar
import com.zeroui.kit.glass.BlurredTopBar
import com.zeroui.kit.glass.CardGlass
import com.zeroui.kit.glass.LocalCardGlass
import com.zeroui.kit.glass.LiquidBottomTab
import com.zeroui.kit.glass.LiquidBottomTabs
import com.zeroui.kit.glass.LocalLiquidBottomTabActive
import com.zeroui.kit.glass.liquidSource
import com.zeroui.kit.glass.rememberTopBarBackdrop
import com.zeroui.kit.glass.topBarBlurSource
import com.zeroui.kit.glass.topBarColor
import com.zeroui.kit.glass.rememberLiquidBackdrop
import com.zeroui.kit.theme.DarkMode
import com.zeroui.kit.theme.GlassMode
import com.zeroui.kit.theme.UiStyle
import com.zeroui.kit.theme.ThemeSettings
import com.zeroui.kit.theme.ZeroTheme

private data class Entry(val section: String, val title: String, val source: String, val body: String, val translation: String? = null, val chatGptTranslation: String? = null)

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
    var themeMode by rememberSaveable { mutableStateOf(prefs.getString("theme_mode", "system") ?: "system") }
    var dynamicColor by rememberSaveable { mutableStateOf(prefs.getBoolean("dynamic_color", true)) }
    var uiStyle by rememberSaveable { mutableStateOf(if (prefs.getString("ui_style", "miuix") == "material") UiStyle.Material else UiStyle.Miuix) }
    var glassNavEnabled by rememberSaveable { mutableStateOf(prefs.getBoolean("glass_nav_enabled", true)) }
    var glassCardsEnabled by rememberSaveable { mutableStateOf(prefs.getBoolean("glass_cards_enabled", true)) }
    var topBarBlurEnabled by rememberSaveable { mutableStateOf(prefs.getBoolean("top_bar_blur_enabled", true)) }
    var smoothCorners by rememberSaveable { mutableStateOf(prefs.getBoolean("smooth_corners", true)) }
    var elasticScroll by rememberSaveable { mutableStateOf(prefs.getBoolean("elastic_scroll", true)) }
    var haptics by rememberSaveable { mutableStateOf(prefs.getBoolean("haptics", true)) }
    var uiScale by rememberSaveable { mutableFloatStateOf(prefs.getFloat("ui_scale", 1.0f).coerceIn(0.65f, 1.50f)) }
    var fontScale by rememberSaveable { mutableFloatStateOf(prefs.getFloat("font_scale", 1.0f)) }
    var fontUri by rememberSaveable { mutableStateOf(prefs.getString("font_uri", null)) }
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
    val zeroDarkMode = when (themeMode) {
        "light" -> DarkMode.Light
        "dark" -> DarkMode.Dark
        else -> DarkMode.System
    }
    val dark = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    val fallbackScheme = if (dark) darkColorScheme(
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

    val scheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else fallbackScheme

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
        ZeroTheme(
            settings = ThemeSettings(
                style = uiStyle,
                darkMode = zeroDarkMode,
                dynamicColor = dynamicColor,
                uiScale = 100,
                glassMode = if (glassNavEnabled) GlassMode.Liquid else GlassMode.Off,
                glassIntensity = 100,
                topBarBlur = topBarBlurEnabled,
                smoothCorners = smoothCorners,
                elasticScroll = elasticScroll,
                haptics = haptics,
            )
        ) {
            MaterialTheme(colorScheme = scheme, typography = scaledTypography) {
                MainScreen(
                    uiStyle = uiStyle,
                    onUiStyleChange = { uiStyle = it; prefs.edit().putString("ui_style", if (it == UiStyle.Material) "material" else "miuix").apply() },
                    glassNavEnabled = glassNavEnabled,
                    onGlassNavChange = { glassNavEnabled = it; prefs.edit().putBoolean("glass_nav_enabled", it).apply() },
                    glassCardsEnabled = glassCardsEnabled,
                    onGlassCardsChange = { glassCardsEnabled = it; prefs.edit().putBoolean("glass_cards_enabled", it).apply() },
                    topBarBlurEnabled = topBarBlurEnabled,
                    onTopBarBlurChange = { topBarBlurEnabled = it; prefs.edit().putBoolean("top_bar_blur_enabled", it).apply() },
                    smoothCorners = smoothCorners,
                    onSmoothCornersChange = { smoothCorners = it; prefs.edit().putBoolean("smooth_corners", it).apply() },
                    elasticScroll = elasticScroll,
                    onElasticScrollChange = { elasticScroll = it; prefs.edit().putBoolean("elastic_scroll", it).apply() },
                    haptics = haptics,
                    onHapticsChange = { haptics = it; prefs.edit().putBoolean("haptics", it).apply() },
                    themeMode = themeMode,
                    dynamicColor = dynamicColor,
                    onDynamicColorChange = {
                        dynamicColor = it
                        prefs.edit().putBoolean("dynamic_color", it).apply()
                    },
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
    uiStyle: UiStyle,
    onUiStyleChange: (UiStyle) -> Unit,
    glassNavEnabled: Boolean,
    onGlassNavChange: (Boolean) -> Unit,
    glassCardsEnabled: Boolean,
    onGlassCardsChange: (Boolean) -> Unit,
    topBarBlurEnabled: Boolean,
    onTopBarBlurChange: (Boolean) -> Unit,
    smoothCorners: Boolean,
    onSmoothCornersChange: (Boolean) -> Unit,
    elasticScroll: Boolean,
    onElasticScrollChange: (Boolean) -> Unit,
    haptics: Boolean,
    onHapticsChange: (Boolean) -> Unit,
    themeMode: String,
    onThemeChange: (String) -> Unit,
    dynamicColor: Boolean,
    onDynamicColorChange: (Boolean) -> Unit,
    uiScale: Float,
    onUiScaleChange: (Float) -> Unit,
    fontScale: Float,
    onFontScaleChange: (Float) -> Unit,
    fontUri: String?,
    onFontUriChange: (String?) -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var category by rememberSaveable { mutableStateOf("全部") }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedEntryTitle by rememberSaveable { mutableStateOf<String?>(null) }
    val libraryListState = rememberLazyListState()
    // Keep the sampled page layer separate from glass surfaces to avoid a render-layer cycle.
    val liquidBackdrop = rememberLiquidBackdrop(always = glassNavEnabled || glassCardsEnabled)
    val topBarScrollBehavior = rememberTopBarScrollBehavior()
    val topBarBackdrop = rememberTopBarBackdrop()
    val windowPalette = ZeroTheme.colors
    val appWindow = (LocalContext.current as? android.app.Activity)?.window
    SideEffect {
        appWindow?.statusBarColor = windowPalette.page.toArgb()
        appWindow?.navigationBarColor = windowPalette.card.toArgb()
    }

    val titles = listOf("典藏", "卦序", "专题", "设置")
    val all = remember { buildEntries() }
    val hexagrams = remember { buildHexagramIndex() }
    val wings = remember { buildWingsIndex() }
    val selectedEntry = (all + hexagrams + wings).firstOrNull { it.title == selectedEntryTitle }
    val visible = remember(tab, category, query, all, hexagrams, wings) {
        val source = when (tab) {
            1 -> if (category == "十翼") wings else all.filter { it.section == "易经" } + hexagrams
            2 -> all.filter { it.section in listOf("卜筮", "河图", "洛书", "相关古籍", "版本档案", "历代注疏") }
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
    BackHandler(enabled = selectedEntry != null) { selectedEntryTitle = null }

    if (selectedEntry != null) {
        ReadingPage(entry = selectedEntry, onBack = { selectedEntryTitle = null })
    } else BoxWithConstraints(Modifier.fillMaxSize()) {
        // Adapt navigation to the available app window width, not the physical device type.
        // Keep the adaptive branch compiled independently from the phone-only navigation path.
        // Width-based navigation works for landscape phones, tablets and resized windows.
        val wideLayout = maxWidth >= 600.dp
        ZeroScaffold(
        modifier = Modifier.nestedScroll(topBarScrollBehavior.nestedScrollConnection),
        containerColor = ZeroTheme.colors.page,
        topBar = {
            BlurredTopBar(topBarBackdrop, topBarScrollBehavior) {
            ZeroTopAppBar(
                title = titles[tab],
                largeTitle = if (tab == 0) "周易数字典藏" else titles[tab],
                subtitle = if (tab == 0) "经典 · 图像 · 版本 · 校勘" else "",
                color = topBarColor(topBarBackdrop),
                scrollBehavior = topBarScrollBehavior,
                actions = {
                    if (tab != 3) ZeroIconButton(onClick = { tab = 3 }) {
                        ZeroIcon(Icons.Filled.Tune, contentDescription = "外观与设置")
                    }
                }
            )
            }
        },
        bottomBar = {
            if (!wideLayout) Box(
                modifier = Modifier.fillMaxWidth().height(92.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                if (glassNavEnabled && liquidBackdrop != null) {
                    LiquidBottomTabs(
                        selectedTabIndex = { tab },
                        onTabSelected = { next ->
                            tab = next
                            category = "全部"
                            query = ""
                        },
                        backdrop = liquidBackdrop,
                        tabsCount = 4,
                        modifier = Modifier.widthIn(max = 340.dp).fillMaxWidth().padding(horizontal = 22.dp),
                        barHeight = 68.dp,
                        intensity = 1f,
                    ) {
                        val labels = listOf("典藏", "卦序", "专题", "设置")
                        val icons = listOf(Icons.Filled.Home, Icons.Filled.List, Icons.Filled.Info, Icons.Filled.Settings)
                        labels.forEachIndexed { index, label ->
                            LiquidBottomTab(onClick = {
                                tab = index
                                category = "全部"
                                query = ""
                            }) {
                                val active = LocalLiquidBottomTabActive.current
                                ZeroIcon(
                                    imageVector = icons[index],
                                    contentDescription = label,
                                    tint = if (active) ZeroTheme.colors.accent
                                    else ZeroTheme.colors.contentMuted.copy(alpha = 0.72f),
                                    modifier = Modifier.size(22.dp)
                                )
                                ZeroText(
                                    label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) ZeroTheme.colors.accent
                                    else ZeroTheme.colors.contentMuted,
                                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                } else {
                    // The regular navigation must remain visible when Liquid Glass is off.
                    // Use Material 3's navigation surface here instead of the kit's
                    // style-dependent bar, which can disappear in the Material style.
                    NavigationBar(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(28.dp)),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                    ) {
                        val navLabels = listOf("典藏", "卦序", "专题", "设置")
                        val navIcons = listOf(Icons.Filled.Home, Icons.Filled.List, Icons.Filled.Info, Icons.Filled.Settings)
                        navLabels.forEachIndexed { index, label ->
                            NavigationBarItem(
                                selected = tab == index,
                                onClick = {
                                    tab = index
                                    category = "全部"
                                    query = ""
                                },
                                icon = { Icon(navIcons[index], contentDescription = label) },
                                label = { Text(label) },
                                alwaysShowLabel = true,
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                ),
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Row(Modifier.fillMaxSize().padding(padding)) {
            if (wideLayout) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    val navLabels = listOf("典藏", "卦序", "专题", "设置")
                    val navIcons = listOf(Icons.Filled.Home, Icons.Filled.List, Icons.Filled.Info, Icons.Filled.Settings)
                    navLabels.forEachIndexed { index, label ->
                        NavigationRailItem(
                            selected = tab == index,
                            onClick = { tab = index; category = "全部"; query = "" },
                            icon = { Icon(navIcons[index], contentDescription = label) },
                            label = { Text(label) },
                            alwaysShowLabel = true
                        )
                    }
                }
            }
            val glassPageColor = ZeroTheme.colors.page
            val glassAccentColor = ZeroTheme.colors.accent
            val glassAccentContainerColor = ZeroTheme.colors.accentContainer
            Box(Modifier.weight(1f).fillMaxHeight()) {
                // A dedicated background-only layer is sampled by glass cards.
                // Never put the foreground page content in this backdrop source.
                Box(
                    Modifier.matchParentSize()
                        .liquidSource(liquidBackdrop)
                        .drawBehind {
                            drawRect(glassPageColor)
                            drawRect(
                                Brush.radialGradient(
                                    colors = listOf(glassAccentColor.copy(alpha = 0.20f), Color.Transparent),
                                    center = Offset(size.width * 0.12f, size.height * 0.12f),
                                    radius = size.minDimension * 0.78f
                                )
                            )
                            drawRect(
                                Brush.radialGradient(
                                    colors = listOf(glassAccentContainerColor.copy(alpha = 0.30f), Color.Transparent),
                                    center = Offset(size.width * 0.92f, size.height * 0.74f),
                                    radius = size.minDimension * 0.72f
                                )
                            )
                        }
                )
                CompositionLocalProvider(
                    LocalCardGlass provides if (glassCardsEnabled) liquidBackdrop?.let { CardGlass(liquid = it) } else null
                ) {
                    Box(
                        Modifier.widthIn(max = 1120.dp).fillMaxSize()
                            .align(Alignment.TopCenter)
                            .topBarBlurSource(topBarBackdrop)
                    ) {
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
                                dynamicColor = dynamicColor,
                                onDynamicColorChange = onDynamicColorChange,
                                uiStyle = uiStyle,
                                onUiStyleChange = onUiStyleChange,
                                glassNavEnabled = glassNavEnabled,
                                onGlassNavChange = onGlassNavChange,
                                glassCardsEnabled = glassCardsEnabled,
                                onGlassCardsChange = onGlassCardsChange,
                                topBarBlurEnabled = topBarBlurEnabled,
                                onTopBarBlurChange = onTopBarBlurChange,
                                smoothCorners = smoothCorners,
                                onSmoothCornersChange = onSmoothCornersChange,
                                elasticScroll = elasticScroll,
                                onElasticScrollChange = onElasticScrollChange,
                                haptics = haptics,
                                onHapticsChange = onHapticsChange,
                            )
                        } else if (tab == 0) {
                            HomePage(
                                onOpenCategory = { name ->
                                    category = name
                                    tab = if (name == "易经" || name == "十翼") 1 else 2
                                    query = ""
                                },
                            )
                        } else {
                            LibraryPage(
                                tab, category, { category = it }, query, { query = it },
                                visible, { selectedEntryTitle = it.title }, libraryListState, Modifier
                            )
                        }
                    }
                }
            }
        }
    }
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadingPage(entry: Entry, onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val readingScrollBehavior = rememberTopBarScrollBehavior()
    val readingTopBarBackdrop = rememberTopBarBackdrop()
    ZeroScaffold(
        modifier = Modifier.nestedScroll(readingScrollBehavior.nestedScrollConnection),
        containerColor = ZeroTheme.colors.page,
        topBar = {
            BlurredTopBar(readingTopBarBackdrop, readingScrollBehavior) {
            ZeroTopAppBar(
                title = entry.title,
                subtitle = entry.section,
                color = topBarColor(readingTopBarBackdrop),
                scrollBehavior = readingScrollBehavior,
                navigationIcon = {
                    ZeroIconButton(onClick = onBack) {
                        ZeroIcon(Icons.Filled.ArrowBack, contentDescription = "返回目录")
                    }
                }
            )
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 920.dp).fillMaxSize().topBarBlurSource(readingTopBarBackdrop),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 44.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ZeroCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 26.dp,
                    contentPadding = PaddingValues(20.dp)
                ) {
                    ZeroText(entry.section, style = MaterialTheme.typography.labelMedium,
                        color = ZeroTheme.colors.accent, fontWeight = FontWeight.SemiBold)
                    ZeroText(entry.title, style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Serif),
                        fontWeight = FontWeight.Bold,
                        color = ZeroTheme.colors.content)
                    ZeroText(entry.source, style = MaterialTheme.typography.bodySmall,
                        color = ZeroTheme.colors.contentMuted, lineHeight = 20.sp)
                    val sourceImageUrl = originalImageSourceFor(entry.title)
                    if (sourceImageUrl != null) {
                        ZeroTextButton(
                            text = "查看古籍影像来源 ↗",
                            onClick = { uriHandler.openUri(sourceImageUrl) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        ZeroText(
                            "此处为版本与馆藏影像来源入口；原版扫描尚未打包进应用，不能离线查看。",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZeroTheme.colors.contentMuted
                        )
                    }
                }
            }
            if (entry.section == "洛书" || entry.section == "河图") {
                val localDiagrams = localDiagramAssetsFor(entry.title)
                if (localDiagrams.isNotEmpty()) {
                    localDiagrams.forEach { diagram ->
                        item { LocalDiagramPreview(diagram.first, diagram.second) }
                    }
                } else if (entry.section == "洛书") {
                    item { LuoshuIllustration(entry.title) }
                }
            }
            item {
                ZeroCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    ZeroText("原文", style = MaterialTheme.typography.titleMedium,
                        color = ZeroTheme.colors.accent, fontWeight = FontWeight.SemiBold)
                    ZeroDivider(color = ZeroTheme.colors.outline.copy(alpha = 0.35f))
                    SelectionContainer {
                        ZeroText(
                            entry.body,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.Serif, lineHeight = 32.sp
                            ),
                            color = ZeroTheme.colors.content
                        )
                    }
                }
            }
            if (entry.translation != null) {
                item {
                    ZeroCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp,
                        contentPadding = PaddingValues(20.dp)
                    ) {
                        ZeroText("原有译文 / 内容说明", style = MaterialTheme.typography.titleMedium,
                            color = ZeroTheme.colors.accent, fontWeight = FontWeight.SemiBold)
                        ZeroText("保留条目原有字段内容；它可能是已有译文，也可能是译注或状态说明，不自动认定为可靠译本。",
                            style = MaterialTheme.typography.labelSmall, color = ZeroTheme.colors.contentMuted)
                        ZeroText(entry.translation, modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 30.sp),
                            color = ZeroTheme.colors.contentMuted)
                    }
                }
            }
            if (entry.chatGptTranslation != null) {
                item {
                    ZeroCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp,
                        contentPadding = PaddingValues(20.dp)
                    ) {
                        ZeroText("ChatGPT 翻译（工作译文）", style = MaterialTheme.typography.titleMedium,
                            color = ZeroTheme.colors.accent, fontWeight = FontWeight.SemiBold)
                        ZeroText("以下译文由 ChatGPT 根据本条所录原文独立翻译，不沿用条目中已有译文；疑难训诂、异文和残缺处以说明为准。",
                            style = MaterialTheme.typography.bodySmall,
                            color = ZeroTheme.colors.contentMuted)
                        ZeroText(entry.chatGptTranslation, modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 30.sp),
                            color = ZeroTheme.colors.content)
                    }
                }
            }
            item {
                ZeroCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 22.dp,
                    contentPadding = PaddingValues(18.dp)
                ) {
                    ZeroText("收录与校勘说明", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                    ZeroText(
                        "本栏为 ChatGPT 翻译，供理解原文之用，不替代底本、古注或学术校勘。疑难字句、异文、残缺和不确定解释会明确标示；原文缺失处不会根据上下文补造。",
                        style = MaterialTheme.typography.bodySmall,
                        color = ZeroTheme.colors.contentMuted,
                        lineHeight = 21.sp
                    )
                }
            }
            item {
                ZeroTextButton(text = "返回目录", onClick = onBack, modifier = Modifier.fillMaxWidth())
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
        2 -> listOf("全部", "卜筮", "河图", "洛书", "历代注疏", "版本档案", "相关古籍")
        else -> listOf("全部", "易经", "十翼", "卜筮", "河图", "洛书", "历代注疏", "版本档案", "相关古籍")
    }
    BoxWithConstraints(modifier.fillMaxSize()) {
        val columns = if (maxWidth >= 760.dp) 2 else 1
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(
                start = if (columns == 2) 24.dp else 18.dp,
                end = if (columns == 2) 24.dp else 18.dp,
                top = 10.dp,
                bottom = 34.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ZeroTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)),
                    cornerRadius = 32.dp,
                    insideMargin = androidx.compose.ui.unit.DpSize(18.dp, 12.dp),
                    singleLine = true,
                    label = "搜索卦名、篇名、原文或版本",
                    useLabelAsPlaceholder = true,
                    leadingIcon = { ZeroIcon(Icons.Filled.Search, contentDescription = null) }
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { name ->
                        ZeroCard(
                            color = if (category == name) ZeroTheme.colors.accentContainer
                                else ZeroTheme.colors.card.copy(alpha = 0.78f),
                            cornerRadius = 50.dp,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            onClick = { onCategoryChange(name) }
                        ) {
                            ZeroText(
                                name,
                                maxLines = 1,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (category == name) ZeroTheme.colors.onAccentContainer
                                    else ZeroTheme.colors.contentMuted,
                                fontWeight = if (category == name) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ZeroText(
                        if (entries.isEmpty()) "没有匹配条目" else "典藏条目",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.weight(1f))
                    ZeroText(
                        "${entries.size} 条",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZeroTheme.colors.contentMuted
                    )
                }
            }
            items(
                entries.chunked(columns),
                key = { row -> row.first().section + row.first().title }
            ) { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { entry ->
                        EntryCard(entry, Modifier.weight(1f)) { onEntryClick(entry) }
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            item {
                ZeroText(
                    "目录条目不等于正文已收录；版本、译文与校勘状态以各条说明为准。",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZeroTheme.colors.contentMuted,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
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
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 42.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            ZeroCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 30.dp,
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(ZeroTheme.colors.accent.copy(alpha = 0.12f))
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            ZeroText(
                                "离线古籍 · 持续整理",
                                style = MaterialTheme.typography.labelSmall,
                                color = ZeroTheme.colors.accent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        ZeroText(
                            "周易",
                            style = MaterialTheme.typography.displaySmall.copy(fontFamily = FontFamily.Serif),
                            fontWeight = FontWeight.Bold,
                            color = ZeroTheme.colors.content
                        )
                        ZeroText(
                            "数字典藏",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = ZeroTheme.colors.content
                        )
                        ZeroText(
                            "以经传为中心，连接历代版本、相关古籍与易学专题。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ZeroTheme.colors.contentMuted,
                            lineHeight = 24.sp
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Box(
                        Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(ZeroTheme.colors.accentContainer.copy(alpha = 0.52f)),
                        contentAlignment = Alignment.Center
                    ) {
                        ZeroText(
                            "易",
                            style = MaterialTheme.typography.displayMedium.copy(fontFamily = FontFamily.Serif),
                            color = ZeroTheme.colors.onAccentContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                ZeroDivider(
                    color = ZeroTheme.colors.outline.copy(alpha = 0.28f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("六十四卦经文", "易传十翼").forEach { label ->
                        Box(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(ZeroTheme.colors.accent.copy(alpha = 0.08f))
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ZeroText(
                                label,
                                style = MaterialTheme.typography.labelMedium,
                                color = ZeroTheme.colors.accent,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ZeroText(
                        "典藏目录",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    ZeroText(
                        "6 个分类",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZeroTheme.colors.contentMuted
                    )
                }
                ZeroText(
                    "按文献类型与专题进入，正文和相关资料分层呈现。",
                    style = MaterialTheme.typography.bodySmall,
                    color = ZeroTheme.colors.contentMuted
                )
            }
        }
        item { CategoryGrid(onOpenCategory) }
        item {
            ZeroCard(
                color = ZeroTheme.colors.card.copy(alpha = 0.82f),
                cornerRadius = 22.dp,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ZeroText(
                        "典藏原则",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.weight(1f))
                    ZeroText(
                        "校勘规范",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZeroTheme.colors.accent
                    )
                }
                ZeroText(
                    "区分经文、传文与后世注释；残缺内容如实标记，不以推测补齐。译文、异文和图版均注明可核验的来源。",
                    style = MaterialTheme.typography.bodySmall,
                    color = ZeroTheme.colors.contentMuted,
                    lineHeight = 22.sp
                )
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
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columnCount = if (maxWidth >= 720.dp) 3 else 2
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            categories.chunked(columnCount).forEach { group ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    group.forEach { (name, description, glyph) ->
                        ZeroCard(
                            modifier = Modifier.weight(1f).heightIn(min = 128.dp),
                            cornerRadius = 22.dp,
                            contentPadding = PaddingValues(15.dp),
                            onClick = { onCategoryChange(name) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(13.dp))
                                        .background(ZeroTheme.colors.accentContainer.copy(alpha = 0.62f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ZeroText(
                                        glyph,
                                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif),
                                        color = ZeroTheme.colors.onAccentContainer
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                                ZeroText(
                                    "↗",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ZeroTheme.colors.contentMuted
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            ZeroText(
                                name,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            ZeroText(
                                description,
                                style = MaterialTheme.typography.labelSmall,
                                color = ZeroTheme.colors.contentMuted,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    repeat(columnCount - group.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun EntryCard(
    entry: Entry,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ZeroCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 22.dp,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 17.dp),
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ZeroText(
                entry.section,
                style = MaterialTheme.typography.labelMedium,
                color = ZeroTheme.colors.accent,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            ZeroText(
                "阅读  ›",
                style = MaterialTheme.typography.labelSmall,
                color = ZeroTheme.colors.contentMuted
            )
        }
        Spacer(Modifier.height(9.dp))
        ZeroText(
            entry.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ZeroTheme.colors.content
        )
        Spacer(Modifier.height(6.dp))
        ZeroText(
            entry.source,
            style = MaterialTheme.typography.labelSmall,
            color = ZeroTheme.colors.contentMuted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(10.dp))
        ZeroText(
            entry.body.replace("\n", " ").take(116) +
                if (entry.body.length > 116) "…" else "",
            style = MaterialTheme.typography.bodyMedium,
            color = ZeroTheme.colors.content.copy(alpha = 0.88f),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PreferenceSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            ZeroText(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            ZeroText(description, style = MaterialTheme.typography.bodySmall, color = ZeroTheme.colors.contentMuted)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsPage(
    themeMode: String,
    onThemeChange: (String) -> Unit,
    dynamicColor: Boolean,
    onDynamicColorChange: (Boolean) -> Unit,
    uiStyle: UiStyle,
    onUiStyleChange: (UiStyle) -> Unit,
    glassNavEnabled: Boolean,
    onGlassNavChange: (Boolean) -> Unit,
    glassCardsEnabled: Boolean,
    onGlassCardsChange: (Boolean) -> Unit,
    topBarBlurEnabled: Boolean,
    onTopBarBlurChange: (Boolean) -> Unit,
    smoothCorners: Boolean,
    onSmoothCornersChange: (Boolean) -> Unit,
    elasticScroll: Boolean,
    onElasticScrollChange: (Boolean) -> Unit,
    haptics: Boolean,
    onHapticsChange: (Boolean) -> Unit,
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
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ZeroText("偏好设置", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                ZeroText("调整阅读环境与应用外观", style = MaterialTheme.typography.bodySmall,
                    color = ZeroTheme.colors.contentMuted)
            }
        }
        item {
            ZeroCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                contentPadding = PaddingValues(18.dp)
            ) {
                ZeroText("显示与排版", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                ListRow {
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ZeroText("界面大小", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.weight(1f))
                            ZeroText("${(uiScale * 100).toInt()}%", color = ZeroTheme.colors.accent,
                                fontWeight = FontWeight.SemiBold)
                        }
                        ZeroText("调整卡片、间距、图标和导航等界面元素。",
                            style = MaterialTheme.typography.bodySmall,
                            color = ZeroTheme.colors.contentMuted)
                        ZeroSlider(value = uiScale, onValueChange = onUiScaleChange,
                            valueRange = 0.65f..1.50f, keyPoints = listOf(0.65f, 0.8f, 1f, 1.2f, 1.35f, 1.5f))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            ZeroText("65% 紧凑", style = MaterialTheme.typography.labelSmall,
                                color = ZeroTheme.colors.contentMuted)
                            ZeroText("150% 宽松", style = MaterialTheme.typography.labelSmall,
                                color = ZeroTheme.colors.contentMuted)
                        }
                    }
                }
                ZeroDivider(color = ZeroTheme.colors.outline.copy(alpha = 0.28f))
                ListRow {
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ZeroText("字体大小", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.weight(1f))
                            ZeroText("${(fontScale * 100).toInt()}%", color = ZeroTheme.colors.accent,
                                fontWeight = FontWeight.SemiBold)
                        }
                        ZeroText("独立调整文字比例，不改变卡片和图标尺寸。",
                            style = MaterialTheme.typography.bodySmall,
                            color = ZeroTheme.colors.contentMuted)
                        ZeroSlider(value = fontScale, onValueChange = onFontScaleChange,
                            valueRange = 0.75f..1.60f,
                            keyPoints = listOf(0.75f, 0.9f, 1f, 1.2f, 1.4f, 1.6f))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            ZeroText("75% 小", style = MaterialTheme.typography.labelSmall,
                                color = ZeroTheme.colors.contentMuted)
                            ZeroText("160% 大", style = MaterialTheme.typography.labelSmall,
                                color = ZeroTheme.colors.contentMuted)
                        }
                    }
                }
            }
        }
        item {
            ZeroCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                contentPadding = PaddingValues(18.dp)
            ) {
                ZeroText("字体", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                ZeroText(fontName, style = MaterialTheme.typography.bodyMedium,
                    color = ZeroTheme.colors.accent, maxLines = 2, overflow = TextOverflow.Ellipsis)
                ZeroText("支持导入 TTF、OTF、TTC、OTC。字体文件仅保存在本机应用设置中。",
                    style = MaterialTheme.typography.bodySmall,
                    color = ZeroTheme.colors.contentMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ZeroButton(onClick = { fontPicker.launch(arrayOf("*/*")) }) {
                        ZeroText("导入字体")
                    }
                    ZeroTextButton(
                        text = "恢复默认",
                        onClick = {
                            onFontUriChange(null)
                            fontMessage = "已恢复系统默认字体。"
                        },
                        enabled = fontUri != null
                    )
                }
                if (fontMessage != null) {
                    ZeroText(fontMessage!!, style = MaterialTheme.typography.bodySmall,
                        color = ZeroTheme.colors.accent)
                }
            }
        }
        item {
            ZeroCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp, contentPadding = PaddingValues(18.dp)) {
                ZeroText("组件风格", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                ZeroText("在 ZeroUI 的 Miuix 与 Material 3 两套组件之间切换。",
                    style = MaterialTheme.typography.bodySmall, color = ZeroTheme.colors.contentMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(UiStyle.Miuix to "Miuix", UiStyle.Material to "Material 3").forEach { (style, label) ->
                        ZeroCard(
                            color = if (uiStyle == style) ZeroTheme.colors.accentContainer else ZeroTheme.colors.card,
                            cornerRadius = 50.dp,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            onClick = { onUiStyleChange(style) }
                        ) {
                            ZeroText(label,
                                color = if (uiStyle == style) ZeroTheme.colors.onAccentContainer else ZeroTheme.colors.contentMuted,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (uiStyle == style) FontWeight.SemiBold else FontWeight.Normal)
                        }
                    }
                }
            }
        }
        item {
            ZeroCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 24.dp, contentPadding = PaddingValues(18.dp)) {
                ZeroText("独立效果开关", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                PreferenceSwitchRow("液态玻璃导航栏", "只控制底部悬浮导航。", glassNavEnabled, onGlassNavChange)
                ZeroDivider(color = ZeroTheme.colors.outline.copy(alpha = 0.25f))
                PreferenceSwitchRow("液态玻璃内容卡片", "只控制首页、目录等页面卡片。", glassCardsEnabled, onGlassCardsChange)
                ZeroDivider(color = ZeroTheme.colors.outline.copy(alpha = 0.25f))
                PreferenceSwitchRow("顶部栏背景模糊", "独立控制滚动内容经过顶部栏时的模糊效果。", topBarBlurEnabled, onTopBarBlurChange)
                ZeroDivider(color = ZeroTheme.colors.outline.copy(alpha = 0.25f))
                PreferenceSwitchRow("连续圆角", "启用 ZeroUI 平滑曲率圆角。", smoothCorners, onSmoothCornersChange)
                ZeroDivider(color = ZeroTheme.colors.outline.copy(alpha = 0.25f))
                PreferenceSwitchRow("弹性滚动", "控制列表边界的弹性滚动效果。", elasticScroll, onElasticScrollChange)
                ZeroDivider(color = ZeroTheme.colors.outline.copy(alpha = 0.25f))
                PreferenceSwitchRow("触感反馈", "控制应用内组件的触觉反馈。", haptics, onHapticsChange)
            }
        }
        item {
            ZeroCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                contentPadding = PaddingValues(18.dp)
            ) {
                ZeroText("主题模式", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        ZeroText("动态取色（Monet）", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        ZeroText("根据系统壁纸生成应用配色；Android 12 及以上生效。", style = MaterialTheme.typography.bodySmall, color = ZeroTheme.colors.contentMuted)
                    }
                    Switch(checked = dynamicColor, onCheckedChange = onDynamicColorChange)
                }
                ZeroDivider(color = ZeroTheme.colors.outline.copy(alpha = 0.28f))
                ZeroText("选择应用外观，也可以跟随系统自动切换。",
                    style = MaterialTheme.typography.bodySmall,
                    color = ZeroTheme.colors.contentMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("system" to "跟随系统", "light" to "浅色", "dark" to "深色").forEach { (value, label) ->
                        ZeroCard(
                            color = if (themeMode == value) ZeroTheme.colors.accentContainer
                                else ZeroTheme.colors.card,
                            cornerRadius = 50.dp,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
                            onClick = { onThemeChange(value) }
                        ) {
                            ZeroText(label, style = MaterialTheme.typography.labelLarge,
                                color = if (themeMode == value) ZeroTheme.colors.onAccentContainer
                                    else ZeroTheme.colors.contentMuted,
                                fontWeight = if (themeMode == value) FontWeight.SemiBold else FontWeight.Normal)
                        }
                    }
                }
            }
        }
        item {
            ZeroCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                contentPadding = PaddingValues(18.dp)
            ) {
                ZeroText("典藏原则", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                ZeroText("缺文不补、异文并列、译文标明依据；未经原图核验的 OCR 不作为底本文字。",
                    color = ZeroTheme.colors.contentMuted,
                    style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
            }
        }
    }
}

private fun buildHexagramIndex(): List<Entry> = listOf(
    Entry("易经", "上经 · 第1卦 · 乾", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "乾：元亨。利貞。\n初九：潛龍勿用。\n九二：見龍在田，利見大人。\n九三：君子終日乾乾，夕惕若；厲，无咎。\n九四：或躍在淵，无咎。\n九五：飛龍在天，利見大人。\n上九：亢龍，有悔。\n用九：見羣龍无首，吉。", """译文说明：以下为本项目工作直译初稿，不是古籍原有译文；“元亨”“利贞”等术语保留其古义余地。

乾：开创而通达，利于守正。
初九：潜藏的龙，不宜有所施展。
九二：龙出现在田野，利于见到德位相称的大人。
九三：君子整日勤勉不息，夜晚仍保持警惕；虽处危厉之境，可以没有灾咎。
九四：龙或跃起，或仍处深渊之中；没有灾咎。
九五：飞龙在天，利于见到大人。
上九：亢进过度的龙，会有悔恨。
用九：显现群龙，却没有谁自居首领，吉。""", chatGptTranslation = """乾：创始而通达，适宜守正。初九，潜伏的龙，不宜有所作为。九二，龙显现在田野，适宜见到德位相称的大人。九三，君子整日勤奋不息，夜间仍保持警惕；虽有危险，可以免于灾咎。九四，龙或跃起，或仍在深渊，没有灾咎。九五，飞龙在天，适宜见到大人。上九，龙飞得过高，必有悔恨。用九，群龙显现而不以任何一龙为首，吉。"""),
    Entry("易经", "上经 · 第2卦 · 坤", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "坤：元亨。利牝馬之貞。君子有攸往，先迷後得主。利西南得朋，東北喪朋。安貞，吉。\n初六：履霜，堅冰至。\n六二：直方大，不習无不利。\n六三：含章，可貞。或從王事，无成有終。\n六四：括囊，无咎无譽。\n六五：黃裳，元吉。\n上六：龍戰于野，其血玄黃。\n用六：利永貞。", """译文说明：以下为本项目工作直译初稿。“牝马之贞”等语涉及古代象数与训诂，暂采用贴近字面的译法。

坤：开创而通达；利于像母马一样柔顺而持正。君子有所前往，起初可能迷失方向，后来会找到主导者。向西南行动可以得到同伴，向东北行动会失去同伴。安于正道，吉。
初六：脚下踩到薄霜，便知道坚冰将要到来。
六二：正直、方正而广大；不必刻意学习，也没有什么不利。
六三：内含美好的文采，可以守正。或许跟从君王办事，不以成功自居，却能有好的结局。
六四：扎紧口袋，不显露才华；没有灾咎，也没有声誉。
六五：黄色下裳，大吉。
上六：龙在原野交战，流出的血呈黑黄之色。
用六：利于长久守正。""", chatGptTranslation = """坤：创始而通达，宜如母马般柔顺守正。君子有所前往，起初迷失，后来找到主导；向西南可得同伴，向东北会失去同伴。安守正道，吉。初六，脚下见霜，便知坚冰将至。六二，正直、方正而广大，不必刻意操习也无所不利。六三，含藏文采，可以守正；跟从君王办事，不居功而能完成。六四，束紧囊袋，无灾咎也无声誉。六五，黄色下裳，大吉。上六，龙在野外交战，血色黑黄。用六，宜长久守正。"""),
    Entry("易经", "上经 · 第3卦 · 屯", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "屯：元亨，利貞。勿用有攸往，利建侯。\n初九：磐桓，利居貞，利建侯。\n六二：屯如邅如，乘馬班如，匪寇婚媾，女子貞不字，十年乃字。\n六三：即鹿无虞，惟入于林中，君子幾不如舍，往吝。\n六四：乘馬班如，求婚媾，往，吉无不利。\n九五：屯其膏；小貞吉，大貞凶。\n上六：乘馬班如，泣血漣如。", """译文说明：以下为本项目工作直译初稿。“磐桓”“班如”“字”等词有不同训释，译文不据单一解释强行定论。

屯：开创而通达，利于守正。不宜贸然前往某处，利于建立诸侯。
初九：徘徊难进；利于安居守正，利于建立诸侯。
六二：进退艰难，车马徘徊；来者不是寇盗，而是求婚者。女子占问婚嫁，暂未成婚，经过十年才得成婚。
六三：追逐野鹿却没有熟悉山林的向导，只会进入林中。君子察觉情势不对，不如舍弃追逐；继续前往会有困难。
六四：车马徘徊，前去求婚；前往吉利，没有什么不利。
九五：恩泽积聚而未能广施；小事守正则吉，大事守正则凶。
上六：车马徘徊，悲伤得泣血不止。""", chatGptTranslation = """屯：创始而通达，利于守正；不宜贸然前往，利于建立诸侯。初九，盘桓难进，宜安居守正，也利于建立诸侯。六二，进退迟疑、车马徘徊；来者不是寇盗而是求婚者，女子守贞未嫁，十年后才出嫁。六三，追鹿却没有向导，只会深入树林；君子察觉不妥，不如放弃，继续前往会有困难。六四，车马徘徊，前往求婚，吉而无所不利。九五，恩泽尚未广施；小事守正吉，大事守正反凶。上六，车马徘徊，悲泣不止。"""),
    Entry("易经", "上经 · 第4卦 · 蒙", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "蒙：亨。匪我求童蒙，童蒙求我。初筮告，再三瀆，瀆則不告。利貞。\n初六：發蒙，利用刑人，用說桎梏，以往吝。\n九二：包蒙吉，納婦吉，子克家。\n六三：勿用取女，見金夫，不有躬，无攸利。\n六四：困蒙，吝。\n六五：童蒙，吉。\n上九：擊蒙，不利為寇，利禦寇。", """译文说明：以下为本项目工作直译初稿。“刑人”“说桎梏”等语的制度背景与训释仍需结合古注复核。

蒙：亨通。不是我去求教于蒙昧的童子，而是童子来求教于我。初次占问便予以告知；反复轻慢地占问，就不再告知。利于守正。
初六：启发蒙昧，宜建立规范、解除束缚；若一味向前，反而有困难。
九二：包容蒙昧者，吉；娶妻，吉；儿子能够承担家业。
六三：不宜娶这样的女子；她见到有财势的男子，便不能守持自身，没有什么利益。
六四：困于蒙昧，有困难。
六五：童子处于蒙昧之中，吉。
上九：以强力击治蒙昧；不利于把别人当作寇敌，利于抵御寇敌。""", chatGptTranslation = """蒙：亨通。不是我去求童蒙，而是童蒙来求我；初次占问便告知，反复轻慢地问便不再告知，宜守正。初六，启发蒙昧，宜施以规范并解除束缚；若一味推进，会有困难。九二，包容蒙昧者吉，接纳女子为妻吉，儿子能够承担家业。六三，不宜娶这样的女子；她见到有财势的男子便不能自持，没有利益。六四，困于蒙昧，有困难。六五，童蒙之人，吉。上九，以强力击治蒙昧；不宜把人当作寇敌，宜抵御寇敌。"""),
    Entry("易经", "上经 · 第5卦 · 需", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "需：有孚，光亨。貞吉，利涉大川。\n初九：需于郊，利用恆，无咎。\n九二：需于沙，小有言，終吉。\n九三：需于泥，致寇至。\n六四：需于血，出自穴。\n九五：需于酒食，貞吉。\n上六：入于穴，有不速之客三人來，敬之終吉。", """译文说明：以下为本项目工作直译初稿。“需”兼有等待、饮食等义；“需于血”语义较难，暂按字面翻译。

需：心中有诚信，光明而通达；守正则吉，利于渡过大河。
初九：在郊外等待；保持恒常，便没有灾咎。
九二：在沙地等待；虽有小的言语纷争，最终吉利。
九三：在泥地等待，招致寇敌到来。
六四：在血污之处等待，最终从穴中出来。
九五：以酒食相待而等待，守正则吉。
上六：进入穴中，有三位不速之客到来；恭敬对待他们，最终吉利。""", chatGptTranslation = """需：心怀诚信，光明而通达；守正吉，利于渡过大河。初九，在郊外等待，保持恒常则无灾咎。九二，在沙地等待，虽有小的言语争端，最终吉。九三，在泥地等待，招来寇敌。六四，在血污之处等待，最终从穴中出来。九五，在酒食之间等待，守正吉。上六，进入穴中，三位不速之客到来；恭敬相待，最终吉。"""),
    Entry("易经", "上经 · 第6卦 · 讼", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "訟：有孚，窒，惕，中吉，終凶。利見大人，不利涉大川。\n初六：不永所事，小有言，終吉。\n九二：不克訟，歸而逋，其邑人三百戶无眚。\n六三：食舊德，貞厲，終吉。或從王事，无成。\n九四：不克訟，復即命渝，安貞吉。\n九五：訟，元吉。\n上九：或錫之鞶帶，終朝三褫之。", """译文说明：以下为本项目工作直译初稿。“窒”“惕”“鞶带”等词保留必要的语义余地。

讼：有诚信，却受到阻塞；心存警惕，居中则吉，争讼到底则凶。利于见到大人，不利于涉越大河。
初六：不把争讼之事长久持续下去；虽有小的言语争议，最终吉利。
九二：不能胜诉，便返回并逃避；其封邑中的三百户人家可以免遭灾祸。
六三：享用祖先留下的德业，守正虽有危险，最终吉利；或许跟从君王办事，不以功成自居。
九四：不能胜诉，返回而改变原先的打算；安于正道，吉。
九五：争讼得到公正裁断，大吉。
上九：有人赐给他系有装饰的带子，但一日之内多次被剥夺。""", chatGptTranslation = """讼：有诚信却受阻塞，心存警惕；居中则吉，争讼到底则凶。宜见大人，不宜渡大河。初六，不长久纠缠争讼，虽有小的口舌，最终吉。九二，不能胜诉，返回逃避，其封邑三百户可免灾。六三，享用旧有德业，守正虽危，最终吉；或跟从君王办事，不居功。九四，不能胜诉，返回并改变主意，安守正道吉。九五，争讼得到公正裁断，大吉。上九，或获赐饰带，但一日之内多次被剥夺。"""),
    Entry("易经", "上经 · 第7卦 · 师", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "師：貞丈人吉，无咎。\n初六：師出以律，否臧，凶。\n九二：在師中吉，无咎；王三錫命。\n六三：師或輿尸，凶。\n六四：師左次，无咎。\n六五：田有禽，利執言，无咎。長子帥師，弟子輿尸，貞凶。\n上六：大君有命，開國承家，小人勿用。", """译文说明：以下为本项目工作直译初稿。“丈人”在此常解释为有经验、德望的长者或统帅；“舆尸”按字面保留。

师：守正而任用有经验的长者为统帅，吉，没有灾咎。
初六：军队出动必须有严明纪律；纪律不好，凶。
九二：统帅居于军中，吉，没有灾咎；君王多次赐命嘉奖。
六三：军队可能用车载运尸体，凶。
六四：军队暂时退驻，没有灾咎。
六五：田野中有猎物，利于捕获并处置。没有灾咎。长子统率军队是合宜的；若让次子等不当人选统军，便会有载尸而归的凶险。守正而行此事仍凶。
上六：君王颁下命令，分封土地、建立诸侯，或使大夫承继家业；小人不可任用。""", chatGptTranslation = """师：守正并任用有经验的长者统军，吉而无灾咎。初六，军队出动必须严守纪律，纪律不善则凶。九二，统帅居军中，吉而无灾咎，君王多次赐命。六三，军队可能用车载尸而归，凶。六四，军队退驻一旁，无灾咎。六五，田中有禽兽，宜捕获；无灾咎。长子统军合宜，若让次子等不当人选统军，则载尸而归，守正也凶。上六，君王颁命，开国封侯、承继家业，不可任用小人。"""),
    Entry("易经", "上经 · 第8卦 · 比", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "比：吉。原筮元永貞，无咎。不寧方來，後夫凶。\n初六：有孚，比之，无咎。有孚盈缶，終來有它，吉。\n六二：比之自內，貞吉。\n六三：比之匪人。\n六四：外比之，貞吉。\n九五：顯比。王用三驅，失前禽，邑人不誡，吉。\n上六：比之无首，凶。", """译文说明：以下为本项目工作直译初稿。“原筮”“后夫”等词存在训释分歧，暂不把某一种解释说成定论。

比：吉。再次占筮，求得长久而正固的结果，没有灾咎。不安定的诸方前来归附，迟迟不来归附者有凶险。
初六：心怀诚信地亲附，没有灾咎。诚信充满如缶中之物，最终会有意外的助益，吉。
六二：从内部亲附，守正则吉。
六三：亲附了不合宜的人。
六四：从外部亲附，守正则吉。
九五：光明正大地亲附。君王以三面围猎、留出一面，放走前方的猎物；邑中之人不受告诫，吉。
上六：亲附却没有首领，凶。""", chatGptTranslation = """比：吉。再次占筮以求长久守正，无灾咎。不安定的诸方前来归附，迟来者有凶。初六，诚信亲附，无灾咎；诚信充实如缶，最终会有意外助益，吉。六二，从内部亲附，守正吉。六三，亲附不合宜的人。六四，从外部亲附，守正吉。九五，光明正大地亲附；君王三面围猎，放走前方猎物，邑人不受告诫，吉。上六，亲附却没有首领，凶。"""),
    Entry("易经", "上经 · 第9卦 · 小畜", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "小畜：亨。密雲不雨，自我西郊。\n初九：復自道，何其咎，吉。\n九二：牽復，吉。\n九三：輿說輻，夫妻反目。\n六四：有孚，血去惕出，无咎。\n九五：有孚攣如，富以其鄰。\n上九：既雨既處，尚德載，婦貞厲，月幾望，君子征凶。", """译文说明：以下为本项目工作直译初稿。“畜”有蓄积、约束等义，个别爻辞仍须结合古注复核。

小畜：亨通。浓云密布却没有降雨，云气来自我方西郊。
初九：返回自己原来的道路，能有什么灾咎？吉。
九二：被牵引着返回，吉。
九三：车轮部件脱落，夫妻反目。
六四：心怀诚信，流血之事消退，警惕之心也得以解除；没有灾咎。
九五：诚信相互牵系，富足并惠及邻近的人。
上九：已经下雨，也已经安定；德行积聚而有所承载。妇人占问有危险，月亮几乎圆满；君子出征则凶。""", chatGptTranslation = """小畜：亨通。浓云密布却不下雨，云气来自我方西郊。初九，返回自己的道路，哪里会有灾咎？吉。九二，被牵引着返回，吉。九三，车轮部件脱落，夫妻反目。六四，心怀诚信，流血之事消退，警惕也解除，无灾咎。九五，诚信相互牵系，富足并惠及邻人。上九，雨已降下，事情已安定；德行积聚而有所承载。妇人占问有危险，月亮几近圆满，君子出行则凶。"""),
    Entry("易经", "上经 · 第10卦 · 履", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "履虎尾，不咥人，亨。\n初九：素履，往无咎。\n九二：履道坦坦，幽人貞吉。\n六三：眇能視，跛能履，履虎尾，咥人，凶。武人為于大君。\n九四：履虎尾，愬愬終吉。\n九五：夬履，貞厲。\n上九：視履考祥，其旋元吉。", """译文说明：以下为本项目工作直译初稿。“履”兼有践履、礼行之义；“幽人”“夬履”等词保留解释余地。

履：踩到老虎尾巴，老虎却不咬人，亨通。
初九：以朴素平实的方式行事，前往没有灾咎。
九二：所行道路平坦宽广，隐居之人守正则吉。
六三：眼睛不好却想看清，脚有跛疾却要行走；踩到老虎尾巴，老虎咬人，凶。武人竟要成为大君。
九四：踩到老虎尾巴，心怀恐惧戒慎，最终吉利。
九五：果断地践行，守正而处危险。
上九：考察自己走过的道路并检验吉凶征兆；回顾周全，大吉。""", chatGptTranslation = """履：踩到老虎尾巴，老虎却不咬人，亨通。初九，以朴素方式行事，前往无灾咎。九二，所行道路平坦，隐居之人守正吉。六三，眼睛不好却想看，脚跛却要走；踩到虎尾而被咬，凶。武人竟要成为大君。九四，踩到虎尾而心怀戒惧，最终吉。九五，果断践行，守正而处危险。上九，考察自己的行履、检验吉凶征兆，回顾周全则大吉。"""),
    Entry("易经", "上经 · 第11卦 · 泰", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "泰：小往大來，吉亨。\n初九：拔茅茹以其彙，征吉。\n九二：包荒。用馮河，不遐遺；朋亡。得尚于中行。\n九三：无平不陂，无往不復，艱貞无咎。勿恤其孚，于食有福。\n六四：翩翩，不富以其鄰；不戒以孚。\n六五：帝乙歸妹，以祉，元吉。\n上六：城復于隍，勿用師，自邑告命，貞吝。", """译文说明：以下为本项目工作直译初稿。“包荒”“朋亡”“得尚于中行”等语有多种训释，译文先保留句法关系，不强定唯一解释。

泰：小的离去，大的到来，吉而亨通。
初九：拔起茅草，根茎相连带出同类；前往吉利。
九二：包容荒远之物。徒步涉越大河，不遗弃远方之人；不结党营私，得以辅佐中正之行。
九三：没有永远平坦而不倾斜的事物，也没有一去不返的事物；艰难中守正，没有灾咎。不必忧虑诚信是否得到回应，饮食之中自有福分。
六四：轻快地相连而来，不以财富自夸，却与邻人相亲；不必告诫，彼此已有诚信。
六五：帝乙嫁女，因此获得福祉，大吉。
上六：城墙坍塌，土回到护城壕中；不宜动用军队，应从自己的城邑发布命令，守正也有困难。""", chatGptTranslation = """泰：小者离去，大者到来，吉而亨通。初九，拔起茅草而根茎相连，带出同类，前往吉。九二，包容荒远之物，徒步涉河，不遗弃远方，不结党营私，得以辅佐中正之道。九三，没有永远平坦而不倾斜之事，也没有一去不返之事；艰难中守正，无灾咎。不必忧虑诚信，饮食自有福。六四，轻快相连而来，不以财富自夸，与邻人相亲；无需告诫，彼此已有诚信。六五，帝乙嫁女，因此获福，大吉。上六，城墙坍塌、土回护城壕；不宜动兵，应从邑中发布命令，守正也有困难。"""),
    Entry("易经", "上经 · 第12卦 · 否", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "否之匪人，不利君子貞，大往小來。\n初六，拔茅茹以其彙，貞吉。亨。\n六二，包承，小人吉，大人否。亨。\n六三，包羞。\n九四，有命，无咎，疇離祉。\n九五，休否，大人吉。其亡其亡，繫于苞桑。\n上九，傾否，先否後喜。", """译文说明：以下为本项目工作直译初稿。“否”在此为闭塞之义；爻辞中的“包承”“疇離祉”等词仍需逐本校勘训释。

否：闭塞不通，不利于君子守正；大的离去，小的到来。
初六：拔起茅草，根茎相连带出同类；守正吉利，亨通。
六二：包容并承受；小人得吉，大人则处于闭塞之中，但仍可亨通。
六三：包藏羞耻。
九四：有命令降下，没有灾咎；同类之人也会获得福祉。
九五：闭塞状态得以休止，大人吉利。要警惕“将要灭亡了，将要灭亡了”，如同把根系系在丛生的桑树上。
上九：闭塞倾覆，先经历闭塞而后转为喜悦。""", chatGptTranslation = """否：闭塞不通，不利于君子守正；大的离去，小的到来。初六，拔起茅草而根茎相连，守正吉利，亨通。六二，包容承受，小人吉，大人处于闭塞，但仍可亨通。六三，包藏羞耻。九四，有命令降下，无灾咎，同类也获得福祉。九五，闭塞得以休止，大人吉；须警惕危亡，如同把根系系在丛生桑树上。上九，闭塞倾覆，先否塞而后喜悦。"""),
    Entry("易经", "上经 · 第13卦 · 同人", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "同人于野，亨。利涉大川，利君子貞。\n初九：同人于門，無咎。\n六二：同人于宗，吝。\n九三：伏戎于莽，升其高陵，三歲不興。\n九四：乘其墉，弗克，攻吉。\n九五：同人，先號啕而后笑。大師克相遇。\n上九：同人于郊，無悔。", """译文说明：以下为本项目工作直译初稿。“同人”指与人相同、与人协同；“攻吉”等句读与训释须结合异本再核。

同人：在旷野与人会同，亨通。利于涉越大河，利于君子守正。
初九：在门外与人会同，没有灾咎。
六二：只在宗族内部与人会同，有困难。
九三：把军队埋伏在草莽之中，又登上高陵窥察；三年不能兴起行动。
九四：登上城墙，却不能攻克；继续进攻则吉。
九五：与人会同，先是号哭，后来欢笑；大军克敌，终于相会。
上九：在郊外与人会同，没有悔恨。""", chatGptTranslation = """同人：在旷野与人会同，亨通；利于渡河，也利于君子守正。初九，在门外与人会同，无灾咎。六二，只与宗族亲近，有困难。九三，把军队埋伏在草莽中，又登上高陵窥察，三年不能行动。九四，登上城墙却未能攻克，继续进攻则吉。九五，与人会同，先号哭后欢笑；大军取胜，终于相会。上九，在郊外与人会同，没有悔恨。"""),
    Entry("易经", "上经 · 第14卦 · 大有", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "大有：元亨。\n初九：无交害，匪咎，艱則无咎。\n九二：大車以載，有攸往，无咎。\n九三：公用亨于天子，小人弗克。\n九四：匪其彭，无咎。\n六五：厥孚交如，威如；吉。\n上九：自天佑之，吉无不利。", """译文说明：以下为本项目工作直译初稿。“亨”在“公用亨于天子”中涉及享献、通达等古义，暂不把礼制解释写成定论。

大有：大为亨通。
初九：没有交相侵害，不算灾咎；若能谨慎艰难地行事，便没有灾咎。
九二：用大车装载，有所前往，没有灾咎。
九三：诸侯得以向天子进献或享献；小人不能承担此事。
九四：不以盛大自居，没有灾咎。
六五：诚信相交，既有威严又能取信，吉。
上九：得到上天的护佑，吉，没有什么不利。""", chatGptTranslation = """大有：大为亨通。初九，不与人交相侵害，不算灾咎；若能谨慎处事，便无灾咎。九二，用大车装载，有所前往，无灾咎。九三，诸侯向天子进献，小人不能胜任。九四，不以盛大自居，无灾咎。六五，以诚信相交并保持威严，吉。上九，得到上天护佑，吉而无所不利。"""),
    Entry("易经", "上经 · 第15卦 · 谦", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "謙：亨，君子有終。\n初六：謙謙君子，用涉大川，吉。\n六二：鳴謙，貞吉。\n九三：勞謙君子，有終吉。\n六四：无不利，撝謙。\n六五：不富，以其鄰，利用侵伐，无不利。\n上六：鳴謙，利用行師，征邑國。", """译文说明：以下为本项目工作直译初稿。“撝谦”有施行、发挥谦德等解释，暂保留其行为义。

谦：亨通，君子能够有好的终局。
初六：谦而又谦的君子，利于涉越大河，吉。
六二：谦德显现于言行，守正吉利。
九三：有功劳而仍能谦逊的君子，最终吉利。
六四：没有什么不利，应当施行谦逊。
六五：不凭借财富，却能得到邻人的支持；利于征伐不服者，没有什么不利。
上六：谦德之名声显现，利于出动军队征讨邑国。""", chatGptTranslation = """谦：亨通，君子有好的终局。初六，谦而又谦的君子，利于渡河，吉。六二，谦德显现，守正吉。九三，有功而仍谦逊的君子，最终吉。六四，施行谦逊，没有什么不利。六五，不凭财富而能得到邻人支持，利于征伐不服者，无所不利。上六，谦德之名声显现，利于出兵征讨邑国。"""),
    Entry("易经", "上经 · 第16卦 · 豫", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "豫：利建侯行師。\n初六：鳴豫，凶。\n六二：介于石，不終日，貞吉。\n六三：盱豫，悔。遲有悔。\n九四：由豫，大有得。勿疑。朋盍簪。\n六五：貞疾，恆不死。\n上六：冥豫，成有渝，无咎。", """译文说明：以下为本项目工作直译初稿。“盱豫”“朋盍簪”等词义较难，暂以语境直译，并保留待考标记。

豫：利于建立诸侯、出动军队。
初六：沉溺于逸乐而自鸣得意，凶。
六二：坚定如石，不等到一天结束便能察觉变化；守正吉利。
六三：仰望他人的逸乐而心生依附，悔；若迟疑拖延，还会有悔。
九四：众人因他而欢悦，他因此大有所得；不要疑虑，朋友聚集而来。
六五：守正而处于疾病之中，长久如此却不会死亡。
上六：昏暗地沉溺于逸乐，事情虽已形成但能够改变，没有灾咎。""", chatGptTranslation = """豫：利于建立诸侯、出动军队。初六，沉溺逸乐而自鸣得意，凶。六二，坚定如石，不待一日终了便察觉变化，守正吉。六三，仰望他人的逸乐而心生依附，悔；迟疑拖延也会有悔。九四，众人因他而欢悦，他大有所得；不要疑虑，朋友聚集而来。六五，守正而患病，长久如此却不会死亡。上六，昏昧地沉溺逸乐，事已形成而能改变，无灾咎。"""),
    Entry("易经", "上经 · 第17卦 · 随", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "隨：元亨。利貞。无咎。\n初九：官有渝，貞吉。出門交有功。\n六二：系小子，失丈夫。\n六三：系丈夫，失小子。隨，有求得利，居貞。\n九四：隨有獲，貞凶。有孚在道，以明，何咎。\n九五：孚于嘉，吉。\n上六：拘系之，乃從維之。王用亨于西山。", """译文说明：以下为本项目工作直译初稿。“官有渝”“随有获”等词涉及古代职官与礼制语境，暂不作过度引申。

随：开创而通达，利于守正，没有灾咎。
初九：职守发生变化，守正吉利；出门与人交往，会有收获。
六二：系恋年少者，便会失去成年之人。
六三：系恋成年之人，便会失去年少者。跟随他人，有所求便能得到利益；安居守正。
九四：跟随而有所收获，守正仍有凶险。若诚信合乎正道，并能明辨事理，还会有什么灾咎？
九五：诚信地亲近美善，吉。
上六：拘留并系住他，随后又以绳索维系。君王在西山举行享祭。""", chatGptTranslation = """随：创始而通达，利于守正，无灾咎。初九，职守有所改变，守正吉；出门交往会有收获。六二，系恋年少者，便失去成年者。六三，系恋成年者，便失去年少者；随从他人，有求则得利，安居守正。九四，随从而有所得，守正却凶；若诚信合乎正道并能明辨，还有什么灾咎？九五，诚信地亲近美善，吉。上六，拘系并维系对方；君王在西山举行享祭。"""),
    Entry("易经", "上经 · 第18卦 · 蛊", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "蠱：元亨。利涉大川。先甲三日，後甲三日。\n初六：幹父之蠱，有子考，无咎，厲終吉。\n九二：幹母之蠱，不可貞。\n九三：幹父之蠱，小有悔，无大咎。\n六四：裕父之蠱，往見吝。\n六五：幹父之蠱，用譽。\n上九：不事王侯，高尚其事。", """译文说明：以下为本项目工作直译初稿。“蛊”可指积弊、败坏，也涉及整治旧事；父母爻辞的具体训释仍需结合古注。

蛊：开创而通达，利于涉越大河；在甲日之前三日谋划，在甲日之后三日继续谋划。
初六：整治父辈留下的弊事；有能承继家业的儿子，没有灾咎，虽有危险，最终吉利。
九二：整治母辈留下的弊事，不宜固执地坚持某一种做法。
九三：整治父辈留下的弊事，虽有小的悔恨，却没有大的灾咎。
六四：宽缓地处理父辈留下的弊事，继续前往会遇到困难。
六五：整治父辈留下的弊事，因此获得称誉。
上九：不侍奉王侯，把自己的事业看得高尚。""", chatGptTranslation = """蛊：创始而通达，利于渡河；甲日前三日谋划，甲日后三日继续谋划。初六，整治父辈留下的弊事，有子承继，无灾咎；虽危险，最终吉。九二，整治母辈留下的弊事，不宜固执。九三，整治父辈弊事，略有悔恨而无大灾咎。六四，宽缓地处理父辈弊事，继续前往会遇困难。六五，整治父辈弊事而获得称誉。上九，不侍奉王侯，把自己的事业看得高尚。"""),
    Entry("易经", "上经 · 第19卦 · 临", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "臨：元亨。利貞。至于八月有凶。\n初九：咸臨，貞吉。\n九二：咸臨，吉无不利。\n六三：甘臨，无攸利。既憂之，无咎。\n六四：至臨，无咎。\n六五：知臨，大君之宜，吉。\n上六：敦臨，吉无咎。", """译文说明：以下为本项目工作直译初稿。“咸临”“甘临”“敦临”等为爻位术语化表达，暂按字面与语境合译。

临：开创而通达，利于守正；到了八月会有凶险。
初九：以感通之道临近他人，守正吉利。
九二：以感通之道临近他人，吉，没有什么不利。
六三：以甜言悦色的方式临近他人，没有利益；若已经意识到问题并感到忧虑，便没有灾咎。
六四：亲自前来临近，没有灾咎。
六五：以智慧临事，这是大君应有的做法，吉。
上六：敦厚诚恳地临近他人，吉，没有灾咎。""", chatGptTranslation = """临：创始而通达，利于守正；到了八月有凶。初九，以感通之道临近，守正吉。九二，以感通之道临近，吉而无所不利。六三，以甜言悦色临近，没有利益；若已忧虑并意识到问题，则无灾咎。六四，亲自临近，无灾咎。六五，以智慧临事，合乎大君之宜，吉。上六，以敦厚诚恳之道临近，吉而无灾咎。"""),
    Entry("易经", "上经 · 第20卦 · 观", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "觀：盥而不荐，有孚顒若。\n初六：童觀，小人无咎，君子吝。\n六二：窺觀，利女貞。\n六三：觀我生，進退。\n六四：觀國之光，利用賓于王。\n九五：觀我生，君子无咎。\n上九：觀其生，君子无咎。", """译文说明：以下为本项目工作直译初稿。“盥而不荐”属于祭礼用语；“观我生”可有观察自身行为、观察所生之事等训释，暂保留原句结构。

观：行盥洗之礼，却尚未献上祭品；心怀诚信，仪态庄严恭敬。
初六：像孩童一样观看；小人没有灾咎，君子则有困难。
六二：从门缝窥看；女子占问有利。
六三：观察自己的所作所为，据此决定进退。
六四：观看国家的光明盛德，适宜作为宾客去朝见君王。
九五：观察自己的所作所为，君子没有灾咎。
上九：观察他人的所作所为，君子没有灾咎。""", chatGptTranslation = """观：行盥洗之礼而尚未献祭，诚信而庄严。初六，像孩童一样观看；小人无灾咎，君子有困难。六二，从缝隙窥看，女子守正有利。六三，观察自身所为，以决定进退。六四，观看国家的光明盛德，适宜作为宾客朝见君王。九五，观察自己的所作所为，君子无灾咎。上九，观察他人的所作所为，君子无灾咎。"""),
    Entry("易经", "上经 · 第21卦 · 噬嗑", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "噬嗑：亨。利用獄。\n初九：屨校滅趾，无咎。\n六二：噬膚滅鼻，无咎。\n六三：噬臘肉，遇毒；小吝，无咎。\n九四：噬乾胏，得金矢，利艱貞，吉。\n六五：噬乾肉，得黃金，貞厲，无咎。\n上九：何校滅耳，凶。", """译文说明：以下为本项目工作直译初稿。本卦多用咀嚼、刑罚意象；“噬乾胏”“金矢”等古代物名与制度含义，仍须对照注疏。

噬嗑：亨通，利于施用刑罚、审理案件。
初九：脚上戴着刑具，遮蔽了脚趾；没有灾咎。
六二：咬到柔软的肉，遮蔽了鼻子；没有灾咎。
六三：咬到带骨的腊肉，遇到毒害；虽有小困难，没有灾咎。
九四：咬到干硬的骨肉，得到金属箭头；利于在艰难中守正，吉。
六五：咬到干肉，得到黄金；守正虽有危险，却没有灾咎。
上九：肩上负着刑具，遮蔽了耳朵，凶。""", chatGptTranslation = """噬嗑：亨通，利于审理诉讼、施用刑罚。初九，脚戴刑具而遮蔽脚趾，无灾咎。六二，咬到柔软的肉而遮蔽鼻子，无灾咎。六三，咬到带骨腊肉而遇毒害，略有困难，无灾咎。九四，咬到干硬骨肉，得到金属箭头；利于艰难中守正，吉。六五，咬到干肉而得到黄金，守正虽危，无灾咎。上九，肩负刑具而遮蔽耳朵，凶。"""),
    Entry("易经", "上经 · 第22卦 · 贲", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "賁：亨。小利有攸往。\n初九：賁其趾，舍車而徒。\n六二：賁其須。\n九三：賁如濡如，永貞吉。\n六四：賁如皤如，白馬翰如，匪寇婚媾。\n六五：賁於丘園，束帛戔戔，吝，終吉。\n上九：白賁，无咎。", """译文说明：以下为本项目工作直译初稿。“贲”有文饰之义；“束帛戋戋”等礼物数量与用法，不在无版本依据时作具体换算。

贲：亨通；小事上有所前往有利。
初九：装饰自己的脚，舍弃车乘而徒步行走。
六二：装饰胡须。
九三：文饰鲜明而润泽；长久守正，吉。
六四：文饰转为素白，白马飞驰而来；来者不是寇盗，而是求婚者。
六五：在山丘园圃中施行文饰，礼用束帛数量不多；虽有困难，最终吉利。
上九：以素白为饰，没有灾咎。""", chatGptTranslation = """贲：亨通，小事有所前往有利。初九，装饰脚部，舍车徒步。六二，装饰胡须。九三，文饰鲜明润泽，长久守正吉。六四，文饰素白，白马飞驰而来；来者不是寇盗而是求婚者。六五，在丘园施行文饰，礼用束帛不多；虽有困难，最终吉。上九，以素白为饰，无灾咎。"""),
    Entry("易经", "上经 · 第23卦 · 剥", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "剝：不利。有攸往。\n初六：剝牀以足，蔑貞凶。\n六二：剝牀以辨，蔑貞凶。\n六三：剝之，无咎。\n六四：剝牀以膚，凶。\n六五：貫魚，以宮人寵，无不利。\n上九：碩果不食，君子得輿，小人剝廬。", """译文说明：以下为本项目工作直译初稿。“辨”在“剥床以辨”中的具体所指存在不同训释；此处不擅自确定为某一种床具构件。

剥：不利于有所前往。
初六：从床脚开始剥落，轻慢正道则凶。
六二：剥落到床的边缘或构件，轻慢正道则凶。
六三：剥落到自身这一部分，便没有灾咎。
六四：剥落到床面及身体接触之处，凶。
六五：像串起鱼一样依次排列，受到宫中之人的宠爱，没有什么不利。
上九：硕大的果实没有被吃掉；君子因此得到车舆，小人却失去了居所。""", chatGptTranslation = """剥：不利于有所前往。初六，从床脚开始剥落，轻慢正道则凶。六二，剥落到床的边缘或构件，轻慢正道则凶。六三，剥落到自身这一部分，无灾咎。六四，剥落到床面及身体接触处，凶。六五，像串鱼一样依次排列，受到宫人宠爱，无所不利。上九，硕大果实未被吃掉；君子得到车舆，小人失去居所。"""),
    Entry("易经", "上经 · 第24卦 · 复", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "復：亨。出入无疾，朋來无咎。反復其道，七日來復，利有攸往。\n初九：不復遠，无袛悔，元吉。\n六二：休復，吉。\n六三：頻復，厲无咎。\n六四：中行獨復。\n六五：敦復，无悔。\n上六：迷復，凶，有災眚。用行師，終有大敗，以其國君，凶；至于十年，不克征。", """译文说明：以下为本项目工作直译初稿。“无袛悔”“七日来复”等语涉及古代时间观与训诂，暂不附会为具体历法周期。

复：亨通。出入没有疾病或灾害，朋友前来没有灾咎。返回原来的道路，七日便会复归；利于有所前往。
初九：返回得并不远，没有大的悔恨，大吉。
六二：美好地返回，吉。
六三：屡次反复返回，虽有危险，没有灾咎。
六四：行于中道，却独自返回。
六五：敦厚诚恳地返回，没有悔恨。
上六：迷失方向而不知返回，凶，有灾祸。若因此出兵，最终会遭到大败，甚至危及国君；直到十年之后，仍不能出征。""", chatGptTranslation = """复：亨通。出入无灾，朋友来临无咎；循道往复，七日复归，利于有所前往。初九，返回不远，没有大的悔恨，大吉。六二，美好地返回，吉。六三，屡次反复返回，虽危而无咎。六四，行于中道而独自返回。六五，敦厚地返回，无悔。上六，迷失而不知返回，凶并有灾；若出兵，终将大败，危及国君，十年之内不能征伐。"""),
    Entry("易经", "上经 · 第25卦 · 无妄", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "无妄：元亨。利貞。其匪正有眚，不利有攸往。\n初九：无妄，往吉。\n六二：不耕穫，不菑畬，則利有攸往。\n六三：无妄之災，或系之牛，行人之得，邑人之災。\n九四：可貞，无咎。\n九五：无妄之疾，勿藥有喜。\n上九：无妄，行有眚，无攸利。", """译文说明：以下为本项目工作直译初稿。“无妄”有不妄为、不虚妄等解释；“不耕获”等句暂按字面保留。

无妄：不虚妄则开创而通达，利于守正；若行为不正便会有灾祸，不利于有所前往。
初九：不妄为，前往吉利。
六二：不耕种却期望收获，不开垦荒地却想得到熟田；这样反而利于有所前往。
六三：无妄之中出现灾祸。有人把牛系在这里，过路人牵走了牛，邑中居民却遭受灾祸。
九四：能够守正，没有灾咎。
九五：无妄之疾，不必用药，自会有喜事。
上九：妄为而行会招致灾祸，没有什么利益。""", chatGptTranslation = """无妄：不妄则亨通，利于守正；行为不正便有灾祸，不利前往。初九，不妄为，前往吉。六二，不耕而求获、不垦荒而求熟田，反而利于前往。六三，无妄之中遭遇灾祸：有人系牛，过路人牵走牛，邑人却受灾。九四，能够守正，无灾咎。九五，无妄之疾，不用药也会有喜。上九，妄行会招灾，没有利益。"""),
    Entry("易经", "上经 · 第26卦 · 大畜", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "大畜：利貞，不家食吉，利涉大川。\n初九：有厲利已。\n九二：輿說輹。\n九三：良馬逐，利艱貞。曰閑輿衛，利有攸往。\n六四：童牛之牿，元吉。\n六五：豶豕之牙，吉。\n上九：何天之衢，亨。", """译文说明：以下为本项目工作直译初稿。“不家食”常联系仕进受禄解释，但此处仍保留字面含义；“何天之衢”句不强定唯一训释。

大畜：利于守正；不在家中吃饭而出外任事，吉；利于涉越大河。
初九：有危险，宜停止前进。
九二：车轮的轴承部件脱落。
九三：良马追逐前行；利于在艰难中守正。每日练习车马与防卫之事，利于有所前往。
六四：给小牛戴上横木以防抵触，大吉。
六五：阉猪的獠牙受到控制，吉。
上九：通达如同上天的大道，亨通。""", chatGptTranslation = """大畜：利于守正；不在家中食用俸禄而出仕，吉；利于渡河。初九，有危险，宜停止。九二，车轴上的缚束部件脱落。九三，良马追逐前行，利于艰难中守正；每日练习车马与防卫，利于前往。六四，给小牛加上横木，大吉。六五，阉猪的獠牙受到控制，吉。上九，天衢大道敞开，亨通。"""),
    Entry("易经", "上经 · 第27卦 · 颐", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "頤：貞吉。觀頤，自求口實。\n初九：舍爾靈龜，觀我朵頤，凶。\n六二：顛頤，拂經，于丘頤，征凶。\n六三：拂頤，貞凶，十年勿用，无攸利。\n六四：顛頤吉，虎視眈眈，其欲逐逐，无咎。\n六五：拂經，居貞吉，不可涉大川。\n上九：由頤，厲吉，利涉大川。", """译文说明：以下为本项目工作直译初稿。“颐”涉及颌部、养育与口食之义；“拂经”“顛颐”等语保留训释余地。

颐：守正吉利。观察颐养之道，寻求自己赖以生存的食物。
初九：舍弃自己的灵龟，却羡慕地看着别人享用食物，凶。
六二：颠倒了颐养的方式，违背常理；向高处求养，前往则凶。
六三：违背颐养之道，守正也凶；十年之内不可有所作为，没有什么利益。
六四：颠倒求养的方向，结果吉利；像老虎一样目光专注，欲望持续不止，没有灾咎。
六五：违背常理，安居守正则吉；不可涉越大河。
上九：由此得到颐养，虽有危险却吉利；利于涉越大河。""", chatGptTranslation = """颐：守正吉。观察养育之道，寻求自己赖以生存的食物。初九，舍弃自己的灵龟，却看着别人贪食，凶。六二，颠倒求养，违背常道；向高处求养，前往凶。六三，违背颐养之道，守正也凶，十年不可用事，无所利。六四，颠倒求养而得吉，像虎一样凝视，欲求持续，无灾咎。六五，虽违常道，安居守正吉，不可渡大河。上九，由此得到养育，虽危而吉，利于渡河。"""),
    Entry("易经", "上经 · 第28卦 · 大过", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "大過：棟橈，利有攸往，亨。\n初六：藉用白茅，无咎。\n九二：枯楊生稊，老夫得其女妻，无不利。\n九三：棟橈，凶。\n九四：棟隆，吉。有它吝。\n九五：枯楊生華，老婦得其士夫，无咎无譽。\n上六：過涉滅頂，凶，无咎。", """译文说明：以下为本项目工作直译初稿。本卦以栋梁承重为喻；“稊”“士夫”“过涉灭顶”等词按字面译出，不延伸为单一历史故事。

大过：栋梁弯曲；利于有所前往，亨通。
初六：铺垫时使用白茅草，没有灾咎。
九二：枯萎的杨树长出新芽；老年男子娶得年轻妻子，没有什么不利。
九三：栋梁弯曲，凶。
九四：栋梁隆起而坚固，吉；若另有旁生枝节，则有困难。
九五：枯萎的杨树开出花朵；老妇人得到年轻丈夫，没有灾咎，也没有声誉。
上六：徒步涉水，水深得淹没头顶，凶；但没有灾咎。""", chatGptTranslation = """大过：栋梁弯曲，利于前往，亨通。初六，以白茅作垫，无灾咎。九二，枯杨生出新芽，老年男子娶年轻妻子，无所不利。九三，栋梁弯曲，凶。九四，栋梁隆起，吉；若另有枝节则有困难。九五，枯杨开花，老妇人得到年轻丈夫，无灾咎也无声誉。上六，涉水过深而淹没头顶，凶；但没有灾咎。"""),
    Entry("易经", "上经 · 第29卦 · 坎", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "習坎：有孚，維心亨。行有尚。\n初六：習坎，入于坎窞，凶。\n九二：坎有險，求小得。\n六三：來之坎坎，險且枕，入于坎窞，勿用。\n六四：樽酒簋貳，用缶，納約自牖，終无咎。\n九五：坎不盈，祗既平，无咎。\n上六：係用徽纆，寘于叢棘，三歲不得，凶。", """译文说明：以下为本项目工作直译初稿。“坎窞”“祗既平”等语有不同解释；本条保留险陷意象，不把它等同于某一具体事件。

习坎：反复处于险境，仍有诚信；只要内心通达，行动便有可取之处。
初六：反复陷入险境，进入坑穴深处，凶。
九二：险坑之中仍有危险，只能求得小的收获。
六三：来去都遇到险坑，危险且难以安身；又陷入坑穴深处，不宜行动。
六四：一樽酒、两簋食物，以瓦缶盛装；从窗户递入简约的祭品，最终没有灾咎。
九五：险坑中的水还没有盈满，逐渐达到平稳，没有灾咎。
上六：被绳索捆系，安置在荆棘丛中，三年不得解脱，凶。""", chatGptTranslation = """习坎：重重险陷之中仍有诚信，内心通达，行动有可取之处。初六，陷入重重险坑，又进入深穴，凶。九二，险中仍有危险，只能求得小收获。六三，来去都遇险，处境危险且难以安身，又陷入深穴，不宜行动。六四，一樽酒、两簋食物，以瓦器盛装，从窗户递入简约祭品，最终无灾咎。九五，险水尚未盈满，逐渐平定，无灾咎。上六，被绳索捆缚，置于荆棘丛中，三年不得解脱，凶。"""),
    Entry("易经", "上经 · 第30卦 · 离", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "離：利貞。亨。畜牝牛，吉。\n初九：履錯然，敬之无咎。\n六二：黃離，元吉。\n九三：日昃之離，不鼓缶而歌，則大耋之嗟，凶。\n九四：突如其來如，焚如，死如，棄如。\n六五：出涕沱若，戚嗟若，吉。\n上九：王用出征，有嘉折首，獲匪其醜，无咎。", """译文说明：以下为本项目工作直译初稿。“离”兼有附丽、光明之义；“履错然”“黄离”等语仍须结合古注校读。

离：利于守正，亨通；蓄养母牛，吉。
初九：脚步交错而行，应当恭敬谨慎，没有灾咎。
六二：黄色的光明，大吉。
九三：太阳偏西时的光景；若不击打瓦缶而歌，便会发出老年人般的叹息，凶。
九四：突然来到，来势猛烈，如同焚烧；又迅速消亡，遭到弃置。
六五：泪水不断流下，忧戚叹息，吉。
上九：君王出兵征讨，能够嘉奖并制服首恶，俘获的并非其同类之众，没有灾咎。""", chatGptTranslation = """离：利于守正，亨通；蓄养母牛，吉。初九，脚步错杂，应当恭敬谨慎，无灾咎。六二，黄色的光明，大吉。九三，太阳偏西之时，若不击缶而歌，便会发出老年人的叹息，凶。九四，突然来到，来势猛烈，如同焚烧，继而死亡、被弃。六五，泪水滂沱，忧戚叹息，吉。上九，君王出征，嘉奖并制服首恶，俘获的不是其同类之众，无灾咎。"""),
    Entry("易经", "下经 · 第31卦 · 咸", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "咸：亨。利貞。取女吉。\n初六：咸其拇。\n六二：咸其腓，凶，居吉。\n九三：咸其股，執其隨，往吝。\n九四：貞吉悔亡，憧憧往來，朋從爾思。\n九五：咸其脢，无悔。\n上六：咸其輔，頰，舌。", """译文说明：以下为本项目工作直译初稿。“咸”有感应、相互影响之义；爻辞依身体部位次第呈现，具体象征解释不强行写入译文。

咸：亨通，利于守正；娶妻吉。
初六：感应发生在脚拇趾。
六二：感应发生在小腿肚，凶；安居不动则吉。
九三：感应发生在大腿，执意跟随其所牵引的对象，前往会有困难。
九四：守正吉利，悔恨消失；心思往来不定，朋友会顺从你的想法。
九五：感应发生在脊背肉处，没有悔恨。
上六：感应发生在面颊、两颊与舌头。""", chatGptTranslation = """咸：亨通，利于守正，娶妻吉。初六，感应在脚拇趾。六二，感应在小腿，凶；安居则吉。九三，感应在大腿，执意跟随所牵引者，前往有困难。九四，守正吉、悔恨消失；心思往来不定，朋友便顺从你的思虑。九五，感应在脊背肉处，无悔。上六，感应在面颊、两颊和舌头。"""),
    Entry("易经", "下经 · 第32卦 · 恒", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "恆：亨，无咎。利貞，利有攸往。\n初六：浚恆，貞凶，无攸利。\n九二：悔亡。\n九三：不恆其德，或承之羞，貞吝。\n九四：田无禽。\n六五：恆其德，貞，婦人吉，夫子凶。\n上六：振恆，凶。", """译文说明：以下为本项目工作直译初稿。“恒”指恒常、持久；“浚恒”“振恒”等词存在训释分歧，暂按字面呈现。

恒：亨通，没有灾咎；利于守正，利于有所前往。
初六：一味追求深远长久，守正也凶，没有什么利益。
九二：悔恨消失。
九三：不能恒久保持自己的德行，或许会因此蒙受羞辱；守正也有困难。
九四：田猎时没有捕获禽兽。
六五：恒久保持自己的德行，守正；妇人吉，丈夫则凶。
上六：动荡不定地追求恒常，凶。""", chatGptTranslation = """恒：亨通，无灾咎；利于守正，也利于前往。初六，过分深求恒久，守正仍凶，无所利。九二，悔恨消失。九三，不能恒常保持德行，或会因此蒙羞；守正也有困难。九四，田猎没有禽兽。六五，恒常保持德行而守正，妇人吉，丈夫凶。上六，动荡不定地求恒，凶。"""),
    Entry("易经", "下经 · 第33卦 · 遁", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "遯：亨。小利貞。\n初六：遯尾，厲，勿用有攸往。\n六二：執之用黃牛之革，莫之勝說。\n九三：系遯，有疾厲，畜臣妾吉。\n九四：好遯君子吉，小人否。\n九五：嘉遯，貞吉。\n上九：肥遯，无不利。", """译文说明：以下为本项目工作直译初稿。“遁”指退避；“肥遁”等语带有古代修辞色彩，暂按字面与语境合译。

遁：亨通；小事上利于守正。
初六：退避得太迟，处境危险；不宜有所前往。
六二：用黄色牛皮牢牢系住，没有人能够解开。
九三：被牵系而不能退避，有疾病与危险；蓄养臣仆、侍妾则吉。
九四：能够从容退避，君子吉，小人则不然。
九五：美好地退避，守正吉利。
上九：宽裕从容地退避，没有什么不利。""", chatGptTranslation = """遁：亨通，小事利于守正。初六，退避太迟，危险，不宜前往。六二，用黄牛皮牢牢系住，没有人能解开。九三，被牵系而不能退避，有疾病和危险；蓄养臣仆、侍妾吉。九四，善于退避，君子吉，小人不吉。九五，美好地退避，守正吉。上九，从容宽裕地退避，无所不利。"""),
    Entry("易经", "下经 · 第34卦 · 大壮", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "大壯：利貞。\n初九：壯于趾，征凶，有孚。\n九二：貞吉。\n九三：小人用壯，君子用罔，貞厲。羝羊觸藩，羸其角。\n九四：貞吉悔亡，藩決不羸，壯于大輿之輹。\n六五：喪羊于易，无悔。\n上六：羝羊觸藩，不能退，不能遂，无攸利，艱則吉。", """译文说明：以下为本项目工作直译初稿。本卦以强盛、羊触藩篱为主要意象；“用罔”“藩决不羸”等词义仍须对照古注。

大壮：利于守正。
初九：强盛表现在脚趾上；贸然前往凶，但心中有诚信。
九二：守正吉利。
九三：小人凭借强力行事，君子则不妄用强力；守正仍有危险。公羊触碰藩篱，角被缠住。
九四：守正吉利，悔恨消失；藩篱破开，角不再受困，力量如同大车的车轴。
六五：在平易之处失去羊，没有悔恨。
上六：公羊触碰藩篱，不能后退，也不能前进，没有什么利益；若能在艰难中守正，则吉。""", chatGptTranslation = """大壮：利于守正。初九，强盛表现在脚趾，贸然前进凶，但心有诚信。九二，守正吉。九三，小人凭强力行事，君子不妄用强力；守正仍危险。公羊触藩，角被缠住。九四，守正吉，悔恨消失；藩篱破开，角不受困，力量足以承载大车。六五，在平易之处失羊，无悔。上六，公羊触藩，进退不得，无所利；能在艰难中守正则吉。"""),
    Entry("易经", "下经 · 第35卦 · 晋", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "晉：康侯用錫馬蕃庶，晝日三接。\n初六：晉如，摧如，貞吉。罔孚，裕无咎。\n六二：晉如，愁如，貞吉。受茲介福，于其王母。\n六三：眾允，悔亡。\n九四：晉如鼫鼠，貞厲。\n六五：悔亡，失得勿恤，往吉无不利。\n上九：晉其角，維用伐邑，厲吉无咎，貞吝。", """译文说明：以下为本项目工作直译初稿。“康侯”“蕃庶”“鼫鼠”等词涉及古代爵称、礼制与物名，译文保留原有意象。

晋：康侯得到君王赐予众多马匹，一日之内多次受到接见。
初六：前进又受挫，守正吉利；即使未获信任，宽裕从容也没有灾咎。
六二：前进时忧愁不安，守正吉利；从王母那里接受重大福祉。
六三：众人都表示信任，悔恨消失。
九四：前进时像鼫鼠一样贪惧，守正仍有危险。
六五：悔恨消失；得失都不必忧虑，前往吉利，没有什么不利。
上九：前进到极处，如同角顶突出；只适合用来征伐自己的城邑，虽危险而吉、没有灾咎，守正却有困难。""", chatGptTranslation = """晋：康侯得到君王赐予众多马匹，一日之内多次受到接见。初六，前进而受挫，守正吉；即使未获信任，从容宽裕也无灾咎。六二，前进而忧愁，守正吉，从王母处接受大福。六三，众人信任，悔恨消失。九四，前进如鼫鼠，守正仍危险。六五，悔恨消失，不必忧虑得失，前往吉而无所不利。上九，前进到角顶之处，只宜征伐自己的邑地；虽危险而吉、无灾咎，守正仍有困难。"""),
    Entry("易经", "下经 · 第36卦 · 明夷", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "明夷：利艱貞。\n初九：明夷于飛，垂其翼。君子于行，三日不食，有攸往，主人有言。\n六二：明夷，夷于左股，用拯馬壯，吉。\n九三：明夷于南狩，得其大首，不可疾貞。\n六四：入于左腹，獲明夷之心，于出門庭。\n六五：箕子之明夷，利貞。\n上六：不明晦，初登于天，后入于地。", """译文说明：以下为本项目工作直译初稿。“明夷”指光明受损；本卦叙事意象与历史人物箕子相关，但不把爻辞直接等同于某一史事。

明夷：利于在艰难中守正。
初九：光明受损，如鸟飞行而垂下翅膀。君子出行，三天没有吃饭；有所前往，主人会有言语责难。
六二：光明受损，伤及左大腿；借助强壮的马匹救助，吉。
九三：光明受损时向南方出猎，捕获首领；不可急于求取正固。
六四：进入左腹之中，了解光明受损者的内心，然后走出门庭。
六五：像箕子那样处于光明受损之时，利于守正。
上六：昏暗而不明，起初升上天空，后来又落入地下。""", chatGptTranslation = """明夷：利于在艰难中守正。初九，光明受损，如鸟飞行而垂翼；君子出行，三日不食，有所前往，主人会有责言。六二，光明受损，伤及左股，凭强壮的马救助，吉。九三，向南出猎而捕获首领，不可急于求取正固。六四，进入左腹，了解光明受损者的内心，然后走出门庭。六五，像箕子那样处于明夷之时，利于守正。上六，昏暗不明，起初升上天，后来落入地。"""),
    Entry("易经", "下经 · 第37卦 · 家人", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "家人：利女貞。\n初九：閑有家，悔亡。\n六二：无攸遂，在中饋，貞吉。\n九三：家人嗃嗃，悔厲吉；婦子嘻嘻，終吝。\n六四：富家，大吉。\n九五：王假有家，勿恤。吉。\n上九：有孚威如，終吉。", """译文说明：以下为本项目工作直译初稿。本卦讨论家庭秩序；译文只解释字面关系，不把古代家庭制度直接当作现代规范。

家人：女子守正有利。
初九：在家中建立规矩，悔恨消失。
六二：没有什么需要自行专断的事；主持家中饮食事务，守正吉利。
九三：家人严厉而有威仪，虽有悔恨与危险，最终吉利；若妇人与孩子嬉闹失度，最终会有困难。
六四：使家庭富足，大吉。
九五：君王使家庭和睦，不必忧虑，吉。
上九：心怀诚信而有威严，最终吉利。""", chatGptTranslation = """家人：女子守正有利。初九，在家中建立规矩，悔恨消失。六二，不自行专断，主持家中饮食事务，守正吉。九三，家人严厉而有威仪，虽有悔恨与危险，最终吉；妇人与孩子嬉闹失度，最终有困难。六四，使家庭富足，大吉。九五，君王使家庭和睦，不必忧虑，吉。上九，诚信而有威严，最终吉。"""),
    Entry("易经", "下经 · 第38卦 · 睽", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "睽：小事吉。\n初九：悔亡，喪馬勿逐，自復；見惡人无咎。\n九二：遇主于巷，无咎。\n六三：見輿曳，其牛掣，其人天且劓，无初有終。\n九四：睽孤，遇元夫，交孚，厲无咎。\n六五：悔亡，厥宗噬膚，往何咎。\n上九：睽孤，見豕負涂，載鬼一車，先張之弧，后說之弧，匪寇婚媾，往遇雨則吉。", """译文说明：以下为本项目工作直译初稿。“睽”指乖离、相背；“天且劓”等古代刑名只按原文作简要说明，不扩写具体细节。

睽：小事吉利。
初九：悔恨消失；马丢失了，不必追赶，它会自己返回；遇见品行不好的人，也没有灾咎。
九二：在小巷中遇到主人，没有灾咎。
六三：看见车被拖住，牛受到牵制，车上的人遭受黥面、割鼻一类刑罚；起初不顺，最终会有结果。
九四：孤立无援时，遇到一位志同道合的人，彼此以诚信相交；虽有危险，没有灾咎。
六五：悔恨消失；同宗之人如咬到柔软的肉一样容易相合，前往有什么灾咎？
上九：孤立无援，看见猪身上沾满污泥，又看见一车仿佛载着鬼怪；先张弓戒备，后来放下弓。来者不是寇盗，而是求婚者；前往遇雨，便吉利。""", chatGptTranslation = """睽：小事吉。初九，悔恨消失；马丢失不必追，它会自行返回；遇见品行不好的人也无灾咎。九二，在巷中遇到主人，无灾咎。六三，看见车被拖住、牛受牵制，车上之人遭受黥面割鼻一类刑罚；起初不顺，最终有结果。九四，孤立时遇到志同道合者，彼此诚信相交，虽危而无咎。六五，悔恨消失，同宗之人相合如咬柔肉，前往有何灾咎？上九，孤立时见猪满身污泥、一车似载鬼怪；先张弓戒备，后放下弓，来者不是寇盗而是求婚者，前往遇雨则吉。"""),
    Entry("易经", "下经 · 第39卦 · 蹇", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "蹇：利西南，不利東北；利見大人，貞吉。\n初六：往蹇，來譽。\n六二：王臣蹇蹇，匪躬之故。\n九三：往蹇來反。\n六四：往蹇來連。\n九五：大蹇朋來。\n上六：往蹇來碩，吉；利見大人。", """译文说明：以下为本项目工作直译初稿。“蹇”指艰难阻滞；“来誉”“来硕”等语义有不同解释，暂按行动方向呈现。

蹇：向西南有利，向东北不利；利于见到大人，守正吉利。
初六：前往会遇到艰难，返回则能得到称誉。
六二：王的臣子艰难又艰难，并不是为了自身的缘故。
九三：前往遇到艰难，返回反而合宜。
六四：前往遇到艰难，返回则能与同伴相连。
九五：处于极大的艰难之中，朋友前来相助。
上六：前往遇到艰难，返回则有所得；吉，利于见到大人。""", chatGptTranslation = """蹇：向西南有利，向东北不利；利于见大人，守正吉。初六，前往艰难，返回得誉。六二，王臣历尽艰难，并非为自己。九三，前往艰难，返回合宜。六四，前往艰难，返回可与同伴相连。九五，处于大艰难中，朋友前来。上六，前往艰难，返回则有所得，吉；利于见大人。"""),
    Entry("易经", "下经 · 第40卦 · 解", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "解：利西南，无所往，其來復吉。有攸往，夙吉。\n初六：无咎。\n九二：田獲三狐，得黃矢，貞吉。\n六三：負且乘，致寇至，貞吝。\n九四：解而拇，朋至斯孚。\n六五：君子維有解，吉；有孚于小人。\n上六：公用射隼，于高墉之上，獲之，无不利。", """译文说明：以下为本项目工作直译初稿。“解”指解除、缓解；“解而拇”“田获三狐”等语保留其象征性，不将其硬套为具体历史事件。

解：向西南有利；若无事可往，返回原处吉；若确有前往之事，及早行动吉。
初六：没有灾咎。
九二：田猎捕获三只狐狸，得到黄色箭矢；守正吉利。
六三：背负物品却乘坐车舆，招致寇盗到来；守正也有困难。
九四：解除脚拇趾上的牵系，朋友便会到来并以诚信相待。
六五：君子能够解除困局，吉；也能使小人心怀诚信。
上六：公侯在高高的城墙上射下鹰隼，捕获它，没有什么不利。""", chatGptTranslation = """解：向西南有利；若无事可往，返回则吉；若确有事要办，及早行动吉。初六，无灾咎。九二，田猎捕获三狐，得到黄色箭矢，守正吉。六三，背负物品却乘车，招来寇盗，守正也有困难。九四，解除脚拇趾的牵系，朋友到来并以诚信相待。六五，君子能够解除困局，吉，也能使小人心怀诚信。上六，公侯在高墙上射下鹰隼并捕获，无所不利。"""),
    Entry("易经", "下经 · 第41卦 · 损", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "損：有孚，元吉。无咎，可貞，利有攸往。曷之用？二簋可用享。\n初九：已事遄往，无咎，酌損之。\n九二：利貞，征凶，弗損益之。\n六三：三人行，則損一人；一人行，則得其友。\n六四：損其疾，使遄有喜，无咎。\n六五：或益之，十朋之龜弗克違，元吉。\n上九：弗損益之，无咎，貞吉，利有攸往，得臣无家。", """译文说明：以下为本项目工作直译初稿。“损”“益”涉及减损、增益及祭祀礼制；“十朋之龟”等古代价值单位不作现代换算。

损：心怀诚信，大吉，没有灾咎；可以守正，利于有所前往。用什么来祭祀？两簋简约的祭品也可以用于享祭。
初九：事情已经办完，便迅速前往；没有灾咎，应斟酌减损的程度。
九二：利于守正；贸然前往凶，不应减损，反而应当有所增益。
六三：三个人同行，就会减少一个人；一个人独行，反而会得到朋友。
六四：减除自己的疾病，使它迅速好转并带来喜悦，没有灾咎。
六五：有人赠予价值极高的灵龟，这种赠予无法推辞，大吉。
上九：不再减损，反而有所增益；没有灾咎，守正吉利，利于有所前往；得到臣属而不以私家为念。""", chatGptTranslation = """损：心怀诚信，大吉，无灾咎，可以守正，利于前往。用什么祭祀？两簋简约祭品也可用于享祭。初九，事情办完便迅速前往，无灾咎，应斟酌减损。九二，利于守正，贸然征行凶；不应减损，反而要增益。六三，三人同行便减少一人，一人独行则得到朋友。六四，减除疾病，使其迅速好转而有喜，无灾咎。六五，有人赠予珍贵灵龟，无法推辞，大吉。上九，不再减损反而增益，无灾咎，守正吉，利于前往；得到臣属而不以私家为念。"""),
    Entry("易经", "下经 · 第42卦 · 益", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "益：利有攸往。利涉大川。\n初九：利用為大作，元吉，无咎。\n六二：或益之，十朋之龜弗克違，永貞吉。王用享于帝，吉。\n六三：益之用凶事，无咎。有孚中行，告公用圭。\n六四：中行，告公從。利用為依遷國。\n九五：有孚惠心，勿問元吉。有孚惠我德。\n上九：莫益之，或擊之，立心勿恆，凶。", """译文说明：以下为本项目工作直译初稿。“凶事”“中行”“迁国”等语涉及古代政治与礼制背景，暂不补入未经证实的具体史事。

益：利于有所前往，利于涉越大河。
初九：利于开展大规模事业，大吉，没有灾咎。
六二：有人赠予价值极高的灵龟，这种赠予无法推辞；长久守正吉利。君王用它祭享上帝，吉。
六三：因凶险之事而得到增益，没有灾咎。心怀诚信，行于中道，向公侯进献玉圭。
六四：行于中道，向公侯禀告，公侯听从；利于依此迁移国都。
九五：心怀诚信并施惠于人，不必占问便是大吉；别人也会诚信地感念我的德惠。
上九：没有人来增益他，反而有人攻击他；立心不能恒久，凶。""", chatGptTranslation = """益：利于前往，利于渡河。初九，利于开展大事业，大吉，无灾咎。六二，有人赠予珍贵灵龟，无法推辞；长久守正吉。君王用它祭享上帝，吉。六三，因凶事而得益，无灾咎；诚信行于中道，向公侯进献玉圭。六四，行于中道，禀告公侯而获允从；利于据此迁移国都。九五，诚信而有惠人之心，不必占问便大吉；他人也诚信地感念我的德惠。上九，无人增益他，反有人攻击他；立心不能恒久，凶。"""),
    Entry("易经", "下经 · 第43卦 · 夬", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "夬：揚于王庭，孚號，有厲，告自邑，不利即戎，利有攸往。\n初九：壯于前趾，往不勝為咎。\n九二：惕號，莫夜有戎，勿恤。\n九三：壯于頄，有凶。君子夬夬，獨行遇雨，若濡有慍，无咎。\n九四：臀无膚，其行次且。牽羊悔亡，聞言不信。\n九五：莧陸夬夬，中行无咎。\n上六：无號，終有凶。", """译文说明：以下为本项目工作直译初稿。“夬”有决断、决去之义；“莧陆”“夬夬”等词仍需结合版本和古注校释。

夬：在王庭公开宣示，诚信地发出号令；虽有危险，应从自己的城邑发布告令。不利于立即动用武力，利于有所前往。
初九：力量显现在前脚趾上；前往却不能取胜，便会成为过失。
九二：心怀警惕并发出号令；即使夜间有敌人来犯，也不必忧虑。
九三：力量显现在颧骨处，有凶险。君子果断决绝，独自行走时遇到雨；虽被淋湿而心怀不悦，没有灾咎。
九四：臀部没有皮肉，行走迟疑不前。牵着羊前行可以消除悔恨，但听到劝告仍不相信。
九五：像莧陆草一样决断地去除，行于中道，没有灾咎。
上六：不再发出号令，最终有凶险。""", chatGptTranslation = """夬：在王庭公开宣示，诚信地发出号令；虽有危险，应从本邑告令。不利于立即动武，利于有所前往。初九，力量显在前脚趾，前往却不能胜，便成为过失。九二，警惕并号令；夜间即使有敌来犯，也不必忧虑。九三，力量显在颧骨，有凶；君子果断决绝，独行遇雨，虽被淋湿而不悦，无灾咎。九四，臀部无皮肉，行走迟疑；牵羊可消悔，但听言不信。九五，决断地除去莧陆草，行中道无灾咎。上六，不再号令，最终有凶。"""),
    Entry("易经", "下经 · 第44卦 · 姤", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "姤：女壯，勿用取女。\n初六：系于金柅，貞吉，有攸往，見凶，羸豕孚踟躅。\n九二：包有魚，无咎，不利賓。\n九三：臀无膚，其行次且，厲，无大咎。\n九四：包无魚，起凶。\n九五：以杞包瓜，含章，有隕自天。\n上九：姤其角，吝，无咎。", """译文说明：以下为本项目工作直译初稿。“姤”指相遇；“女壮”是卦辞的古代象征表达，不将其改写成对现实女性的评价。

姤：女子强盛，不宜娶这样的女子。
初六：系在坚固的车止木上，守正吉利；若有所前往，会遇到凶险。瘦弱的猪却仍然躁动不安。
九二：包中有鱼，没有灾咎；不利于把鱼拿去招待宾客。
九三：臀部没有皮肉，行走迟疑；虽有危险，却没有大的灾咎。
九四：包中没有鱼，兴起便有凶险。
九五：用杞柳包裹瓜果，内含文采，仿佛有物从天而降。
上九：相遇在角上，虽有困难，却没有灾咎。""", chatGptTranslation = """姤：女子强盛，不宜娶这样的女子。初六，系在坚固车止木上，守正吉；若有所前往则见凶，瘦猪仍然躁动。九二，包中有鱼，无灾咎，不利于招待宾客。九三，臀部无皮肉，行走迟疑，危险但无大灾咎。九四，包中没有鱼，兴起便凶。九五，以杞柳包瓜，内含文采，仿佛有物从天而降。上九，相遇在角上，有困难但无灾咎。"""),
    Entry("易经", "下经 · 第45卦 · 萃", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "萃：亨。王假有廟，利見大人，亨。利貞。用大牲吉，利有攸往。\n初六：有孚不終，乃亂乃萃，若號一握為笑，勿恤，往无咎。\n六二：引吉，无咎，孚乃利用禴。\n六三：萃如，嗟如，无攸利，往无咎，小吝。\n九四：大吉，无咎。\n九五：萃有位，无咎。匪孚，元永貞，悔亡。\n上六：齎咨涕洟，无咎。", """译文说明：以下为本项目工作直译初稿。“王假有庙”“禴”等为祭祀用语；此处只作简明释义，不擅自补写礼仪细节。

萃：亨通。君王来到宗庙，利于见到大人；亨通，利于守正。使用大牲祭祀吉，利于有所前往。
初六：有诚信却不能保持到底，于是纷乱而聚集；有人呼号，众人又转而欢笑，不必忧虑，前往没有灾咎。
六二：受到引导而吉，没有灾咎；有诚信，便可以用简约的祭品举行祭祀。
六三：聚集在一起，发出叹息；没有什么利益，前往虽没有灾咎，却有小困难。
九四：大吉，没有灾咎。
九五：聚集而居于尊位，没有灾咎；若未获信任，仍须长久守正，悔恨才能消失。
上六：叹息哭泣，眼泪鼻涕交下；没有灾咎。""", chatGptTranslation = """萃：亨通。君王来到宗庙，利于见大人；利于守正。用大牲祭祀吉，利于前往。初六，诚信不能保持到底，因而纷乱聚集；有人呼号，众人又转为欢笑，不必忧虑，前往无灾咎。六二，受到引导而吉，无灾咎；有诚信便可用简约祭品祭祀。六三，聚集而叹息，无所利；前往无灾咎，但略有困难。九四，大吉，无灾咎。九五，聚集而居尊位，无灾咎；若未获信任，须长久守正，悔恨方消。上六，叹息哭泣，眼泪鼻涕交下，无灾咎。"""),
    Entry("易经", "下经 · 第46卦 · 升", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "升：元亨，用見大人，勿恤，南征吉。\n初六：允升，大吉。\n九二：孚乃利用禴，无咎。\n九三：升虛邑。\n六四：王用亨于岐山，吉无咎。\n六五：貞吉，升階。\n上六：冥升，利于不息之貞。", """译文说明：以下为本项目工作直译初稿。“用见大人”“用亨于岐山”等语涉及古代祭礼与政治语境，暂保留其原有表达。

升：大为亨通；宜于见到大人，不必忧虑；向南行进吉利。
初六：诚信地上升，大吉。
九二：有诚信，便可以用简约的祭品举行祭祀；没有灾咎。
九三：升入空虚的城邑。
六四：君王在岐山举行享祭，吉，没有灾咎。
六五：守正吉利，逐级登上台阶。
上六：昏暗地上升，利于不停息地守正。""", chatGptTranslation = """升：大为亨通，宜见大人，不必忧虑，向南征行吉。初六，诚信地上升，大吉。九二，有诚信便可用简约祭品祭祀，无灾咎。九三，升入空虚的城邑。六四，君王在岐山举行享祭，吉而无灾咎。六五，守正吉，逐级登上台阶。上六，昏暗地上升，利于不停止地守正。"""),
    Entry("易经", "下经 · 第47卦 · 困", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "困：亨，貞大人吉，无咎，有言不信。\n初六：臀困于株木，入于幽谷，三歲不覿。\n九二：困于酒食，朱紱方來，利用亨祀，征凶，无咎。\n六三：困于石，據于蒺藜，入于其宮，不見其妻，凶。\n九四：來徐徐，困于金車，吝，有終。\n九五：劓刖，困于赤紱，乃徐有說，利用祭祀。\n上六：困于葛藟，于臲卼，曰動悔。有悔，征吉。", """译文说明：以下为本项目工作直译初稿。本卦多用困顿、受阻的象征；“朱绂”“赤绂”“臲卼”等词保留古代服饰与状态意象。

困：亨通；守正而有德望的大人吉，没有灾咎；即使有所言说，也未必有人相信。
初六：臀部困在枯树根上，又进入幽暗山谷，三年不能见人。
九二：困于酒食之中，穿朱色蔽膝服饰的人刚刚到来；利于举行祭祀，贸然出征凶，但没有灾咎。
六三：困在石头之间，又倚靠蒺藜；进入自己的宫室，却见不到妻子，凶。
九四：缓缓而来，困在金色车乘之中；虽有困难，最终会有结果。
九五：受到劓刑、刖刑一类刑罚，又困于赤色服饰之中；随后渐渐脱困，利于举行祭祀。
上六：困在葛藤之中，处境动荡不安；若有所行动便会后悔。虽有悔恨，前往征行仍吉。""", chatGptTranslation = """困：亨通；守正而有德望的大人吉，无灾咎；虽有言说也未必被相信。初六，臀部困在枯树根上，进入幽暗山谷，三年不能相见。九二，困于酒食，穿朱色蔽膝服饰者来到；利于祭祀，征行凶而无灾咎。六三，困于石间，又倚蒺藜；回到宫室却见不到妻子，凶。九四，缓缓而来，困于金车，有困难但终有结果。九五，遭受劓、刖一类刑罚，又困于赤色服饰，随后渐渐脱困，利于祭祀。上六，困于葛藤和动荡之中，若行动便悔；虽有悔恨，前往仍吉。"""),
    Entry("易经", "下经 · 第48卦 · 井", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "井：改邑不改井，无喪无得，往來井井。汔至亦未繘井。羸其瓶，凶。\n初六：井泥不食，舊井无禽。\n九二：井谷射鮒，瓮敝漏。\n九三：井渫不食，為我心惻，可用汲，王明，并受其福。\n六四：井甃，无咎。\n九五：井冽，寒泉食。\n上六：井收勿幕，有孚元吉。", """译文说明：以下为本项目工作直译初稿。本卦以井为喻；“繘井”“羸其瓶”等词涉及汲水器具，译文按字面处理。

井：城邑可以改变，井却不会改变；既没有失去，也没有增加，人们来来往往都依靠这口井。水已经快要汲到，却还没有把井绳完全提出；水瓶破裂，凶。
初六：井中有泥，不能饮用；旧井甚至没有禽鸟来饮水。
九二：井底的水只能射到小鱼，水瓮破损而漏水。
九三：井水已经淘洗清洁，却无人饮用；我因此感到悲伤。可以汲取井水，若君王明察，众人便都能得到福祉。
六四：修砌井壁，没有灾咎。
九五：井水清冽，寒冷的泉水可以饮用。
上六：井水已经汲取，不要遮盖井口；心怀诚信，大吉。""", chatGptTranslation = """井：城邑改变而井不改变，既无失也无得，人们来往都依靠井。水将汲到却尚未把井绳提起，水瓶破裂，凶。初六，井中有泥不能饮，旧井没有禽鸟。九二，井水只能射到小鱼，水瓮破漏。九三，井水淘洗清洁却无人饮用，令人悲伤；若能汲取，君王明察，众人都受福。六四，修砌井壁，无灾咎。九五，井水清冽，寒泉可饮。上六，井水已汲，不要遮盖井口；心怀诚信，大吉。"""),
    Entry("易经", "下经 · 第49卦 · 革", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "革：巳日乃孚，元亨。利貞。悔亡。\n初九：鞏用黃牛之革。\n六二：巳日乃革之，征吉，无咎。\n九三：征凶，貞厲，革言三就，有孚。\n九四：悔亡，有孚改命，吉。\n九五：大人虎變，未占有孚。\n上六：君子豹變，小人革面，征凶，居貞吉。", """译文说明：以下为本项目工作直译初稿。“巳日”涉及择日语义；“革言三就”等句仍有训释分歧，暂不补写具体制度背景。

革：到了巳日才取得信任，开创而通达；利于守正，悔恨消失。
初九：用黄色牛皮牢固地束缚起来。
六二：到了巳日再进行变革，前往吉利，没有灾咎。
九三：贸然前往凶，守正也有危险；变革的言论经过多次商议，取得诚信。
九四：悔恨消失；心怀诚信而改变旧命令，吉。
九五：大人如虎纹般焕然变化，不必占问便已获得信任。
上六：君子如豹纹般变化，小人也改变面貌；贸然前往凶，安居守正吉。""", chatGptTranslation = """革：到巳日才取得信任，开创通达，利于守正，悔恨消失。初九，用黄牛皮牢固束缚。六二，到巳日再变革，前往吉且无灾咎。九三，前往凶、守正危险；变革之言多次商议后取得信任。九四，以诚信改变旧命令，吉。九五，大人如虎纹般变化，不占而已获信任。上六，君子如豹纹变化，小人也改换面貌；贸然前往凶，安居守正吉。""",
    Entry("易经", "下经 · 第50卦 · 鼎", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "鼎：元吉，亨。\n初六：鼎顛趾，利出否，得妾以其子，无咎。\n九二：鼎有實，我仇有疾，不我能即，吉。\n九三：鼎耳革，其行塞，雉膏不食，方雨虧悔，終吉。\n九四：鼎折足，覆公餗，其形渥，凶。\n六五：鼎黃耳金鉉，利貞。\n上九：鼎玉鉉，大吉，无不利。", """译文说明：以下为本项目工作直译初稿。本卦以鼎器为核心意象；“公餗”“金铉”“玉铉”等器物与礼制词按字面翻译。

鼎：大吉，亨通。
初六：鼎足颠倒，利于倒出其中不洁之物；得到妾并生育儿子，没有灾咎。
九二：鼎中有食物，我的对手有疾病，不能接近我，吉。
九三：鼎耳发生变异，搬运受阻；鼎中的雉肉无人享用，后来遇雨，悔恨减少，最终吉利。
九四：鼎足折断，倾覆了公侯的食物，器物沾污，凶。
六五：鼎有黄色的耳和金属制成的横杠，利于守正。
上九：鼎有玉制的横杠，大吉，没有什么不利。""", chatGptTranslation = """鼎：大吉亨通。初六，鼎足颠倒，利于倒出不洁之物；得妾生子，无灾咎。九二，鼎中有实物，对手有病不能接近，吉。九三，鼎耳变异而搬运受阻，雉肉无人食；遇雨后悔减，终吉。九四，鼎足折断，倾覆公侯食物，器物沾污，凶。六五，鼎有黄耳金杠，利于守正。上九，鼎有玉杠，大吉无不利。""",
    Entry("易经", "下经 · 第51卦 · 震", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "震：亨。震來虩虩，笑言啞啞。震驚百里，不喪匕鬯。\n初九：震來虩虩，后笑言啞啞，吉。\n六二：震來厲，億喪貝，躋于九陵，勿逐，七日得。\n六三：震蘇蘇，震行无眚。\n九四：震遂泥。\n六五：震往來厲，億无喪，有事。\n上六：震索索，視矍矍，征凶。震不于其躬，于其鄰，无咎。婚媾有言。", """译文说明：以下为本项目工作直译初稿。“虩虩”“啞啞”等为拟声词；“億”在相关爻辞中的训释不一，译文不强行确定为数量。

震：亨通。雷声到来令人恐惧戒慎，随后又能谈笑自若。雷声震惊百里，却没有丢失祭祀用的匙与酒器。
初九：雷声到来令人恐惧，随后又能谈笑自若，吉。
六二：雷声到来，处境危险；仿佛失去财物，登上九重高陵，不必追寻，七日后会得到。
六三：雷声使人惊惧不安；因雷震而行动，没有灾祸。
九四：雷声震动，却陷入泥中。
六五：雷声往来，处境危险；虽有忧惧，却没有失去应有之物，仍有事情要做。
上六：雷声使人惊恐战栗，目光惊惶；贸然前往凶。雷没有震到自己，却震到邻人，没有灾咎；婚姻之事会有言语争议。""", chatGptTranslation = """震：亨通。雷来令人恐惧，随后又谈笑自若；震惊百里却不失祭器。初九，先惧后笑，吉。六二，雷来有险，似乎失财，登九陵，不必追，七日后得。六三，震惊不安，因震而行无灾。九四，震动而陷泥。六五，雷声往来有险，虽忧惧仍有事可做。上六，惊惶战栗，前往凶；雷震邻人而未及自身，无咎，婚事有争言。""",
    Entry("易经", "下经 · 第52卦 · 艮", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "艮：艮其背，不獲其身，行其庭，不見其人，无咎。\n初六：艮其趾，无咎，利永貞。\n六二：艮其腓，不拯其隨，其心不快。\n九三：艮其限，列其夤，厲薰心。\n六四：艮其身，无咎。\n六五：艮其輔，言有序，悔亡。\n上九：敦艮，吉。", """译文说明：以下为本项目工作直译初稿。“艮”指止；“腓”“夤”“辅”等身体部位词按常见训释译出，具体象征意义另待注疏说明。

艮：止于背部，不再感受到自身；行走在庭院中，也看不见他人，没有灾咎。
初六：止于脚趾，没有灾咎，利于长久守正。
六二：止于小腿肚，不能救助所跟随的人，心中不快。
九三：止于腰部，脊肉受到牵连；处境危险，忧虑灼心。
六四：止于自身，没有灾咎。
六五：止于面颊与口辅，说话有条理，悔恨消失。
上九：敦厚地止定不动，吉。""", chatGptTranslation = """艮：止于背部，不觉自身；行于庭中也不见人，无咎。初六，止于脚趾，无咎，宜长久守正。六二，止于小腿，不能救助所随之人，心中不快。九三，止于腰部，脊肉牵连，危险忧心。六四，止于自身，无咎。六五，止于口辅，说话有序，悔亡。上九，敦厚而止，吉。""",
    Entry("易经", "下经 · 第53卦 · 渐", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "漸：女歸吉，利貞。\n初六：鴻漸于干，小子厲，有言，无咎。\n六二：鴻漸于磐，飲食衎衎，吉。\n九三：鴻漸于陸，夫征不復，婦孕不育，凶；利禦寇。\n六四：鴻漸于木，或得其桷，无咎。\n九五：鴻漸于陵，婦三歲不孕，終莫之勝，吉。\n上九：鴻漸于陸，其羽可用為儀，吉。", """译文说明：以下为本项目工作直译初稿。本卦以鸿雁渐次前行为象；“干”“磐”“陆”“桷”等地貌、木构词按常见字义翻译。

渐：女子出嫁吉利，利于守正。
初六：鸿雁渐渐飞到水边高地；年少者处境危险，虽有言语议论，没有灾咎。
六二：鸿雁渐渐飞到磐石上，饮食和乐，吉。
九三：鸿雁渐渐飞到高平之地；丈夫出征而不归，妻子怀孕却不能生育，凶；利于抵御寇盗。
六四：鸿雁渐渐飞到树上，或许能找到平直的枝桷，没有灾咎。
九五：鸿雁渐渐飞到山陵；妇人三年不能怀孕，最终没有人能胜过她，吉。
上九：鸿雁渐渐飞到高平之地，它的羽毛可以用来作为仪饰，吉。""", chatGptTranslation = """渐：女子出嫁吉，利于守正。初六，鸿雁到水边高地，年少者有险，虽有议论无咎。六二，鸿雁到磐石，饮食和乐，吉。九三，鸿雁到陆地，丈夫出征不归，妻孕不育，凶；利于御寇。六四，鸿雁到树上，或得平直枝条，无咎。九五，鸿雁到山陵，妇人三年不孕，终无人能胜，吉。上九，鸿雁到高平之地，羽毛可作仪饰，吉。""",
    Entry("易经", "下经 · 第54卦 · 归妹", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "歸妹：征凶，无攸利。\n初九：歸妹以娣，跛能履，征吉。\n九二：眇能視，利幽人之貞。\n六三：歸妹以須，反歸以娣。\n九四：歸妹愆期，遲歸有時。\n六五：帝乙歸妹，其君之袂，不如其娣之袂良，月幾望，吉。\n上六：女承筐无實，士刲羊无血，无攸利。", """译文说明：以下为本项目工作直译初稿。本卦涉及古代婚姻礼制；“娣”“须”等身份与词义应结合古代礼制文献另作考证。

归妹：前往凶，没有什么利益。
初九：女子出嫁时作为媵妾随行，脚跛却仍能行走；前往吉。
九二：眼睛不好却能看见，利于隐居之人守正。
六三：女子出嫁时等待时机，最终返回而作为媵妾。
九四：女子出嫁超过了约定期限，迟些出嫁自有合适的时机。
六五：帝乙嫁女；新娘所穿礼服的袖子，不如陪嫁女子的袖子华美；月亮几乎圆满，吉。
上六：女子捧着筐却没有东西，男子宰羊却没有血，没有什么利益。""", chatGptTranslation = """归妹：前往凶，无所利。初九，女子出嫁作媵妾，虽跛仍能行，前往吉。九二，目眇而能视，利于幽居者守正。六三，出嫁等待时机，返回后作媵妾。九四，婚期延误，迟嫁自有其时。六五，帝乙嫁女，正妻衣袖不及媵妾华美，月近望，吉。上六，女子持空筐，男子宰羊无血，无所利。""",
    Entry("易经", "下经 · 第55卦 · 丰", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "豐：亨。王假之，勿憂，宜日中。\n初九：遇其配主，雖旬无咎，往有尚。\n六二：豐其蔀，日中見斗，往得疑疾，有孚發若，吉。\n九三：豐其沛，日中見沫，折其右肱，无咎。\n九四：豐其蔀，日中見斗，遇其夷主，吉。\n六五：來章，有慶譽，吉。\n上六：豐其屋，蔀其家，窺其戶，闃其无人，三歲不觌，凶。", """译文说明：以下为本项目工作直译初稿。“丰”指盛大、丰盛；“蔀”“沛”“沫”等字的古义存在不同解释，译文保留遮蔽、光象等意象。

丰：亨通。君王来到这里，不必忧虑，宜于像太阳居中时那样明察。
初九：遇到与自己匹配的主人，即使时间相隔也没有灾咎；前往会受到尊尚。
六二：遮蔽极为浓密，正午时竟看见北斗；前往会招致疑忌之疾，但若诚信显现，吉。
九三：遮蔽广大，正午时看见微弱的光点；折断了右臂，没有灾咎。
九四：遮蔽浓密，正午时看见北斗；遇到与自己相匹配的主人，吉。
六五：光明与文采到来，有喜庆与称誉，吉。
上六：房屋高大，遮蔽了整个家；从门缝窥看，屋中寂静无人，三年不能见人，凶。""", chatGptTranslation = """丰：亨通，君王来到此处不必忧虑，宜如日中般明察。初九，遇匹配之主，虽有间隔无咎，前往受尊。六二，浓密遮蔽而正午见斗，前往招疑；诚信显现则吉。九三，遮蔽广大、正午见微光，折右臂，无咎。九四，浓密遮蔽、正午见斗，遇相应之主，吉。六五，文采到来，有庆誉，吉。上六，屋高而蔽家，窥门寂无人，三年不见，凶。""",
    Entry("易经", "下经 · 第56卦 · 旅", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "旅：小亨，旅貞吉。\n初六：旅瑣瑣，斯其所取災。\n六二：旅即次，懷其資，得童僕貞。\n九三：旅焚其次，喪其童僕，貞厲。\n九四：旅于處，得其資斧，我心不快。\n六五：射雉一矢亡，終以譽命。\n上九：鳥焚其巢，旅人先笑后號咷。喪牛于易，凶。", """译文说明：以下为本项目工作直译初稿。“旅”指旅居、寄寓；“资斧”“誉命”等词保留古代旅人处境与礼命语境。

旅：小有亨通，旅居之人守正吉利。
初六：旅居之人琐碎拘谨，灾祸正是由此招来。
六二：旅居之人到达客舍，怀中带着资财，得到忠贞的童仆。
九三：旅居之人的客舍被焚毁，失去童仆；守正仍有危险。
九四：旅居之人暂时安顿下来，得到资财与工具，但心中并不快乐。
六五：射野鸡时失去一支箭，最终得到声誉与命令。
上九：鸟巢被焚毁，旅人起初发笑，后来放声哭号；在易地失去牛，凶。""", chatGptTranslation = """旅：小有亨通，旅居守正吉。初六，旅人琐碎拘谨，灾由此来。六二，到客舍，怀资财，得忠贞童仆。九三，客舍被焚，失童仆，守正有险。九四，暂居而得资财工具，心中不快。六五，射雉失一箭，终得声誉与命令。上九，鸟巢被焚，旅人先笑后哭；异地失牛，凶。""",
    Entry("易经", "下经 · 第57卦 · 巽", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "巽：小亨。利有攸往。利見大人。\n初六：進退，利武人之貞。\n九二：巽在牀下，用史巫紛若，吉无咎。\n九三：頻巽，吝。\n六四：悔亡，田獲三品。\n九五：貞吉悔亡，无不利。无初有終，先庚三日，后庚三日，吉。\n上九：巽在牀下，喪其資斧，貞凶。", """译文说明：以下为本项目工作直译初稿。“巽”有顺入、谦逊之义；“史巫纷若”“先庚三日，后庚三日”等语保留古代礼制与择日背景。

巽：小有亨通，利于有所前往，利于见到大人。
初六：进退不定；利于像武人一样坚定守正。
九二：谦顺地处于床下，任用众多史官与巫者，吉，没有灾咎。
九三：屡次卑顺退让，有困难。
六四：悔恨消失，田猎获得三种猎物。
九五：守正吉利，悔恨消失，没有什么不利。起初不顺，最终有成；在庚日前三日预先谋划，在庚日后三日继续谋划，吉。
上九：谦顺地处于床下，丧失资财与工具；守正仍凶。""", chatGptTranslation = """巽：小有亨通，利于前往、见大人。初六，进退不定，利于武人般守正。九二，谦顺在床下，史巫众多，吉无咎。九三，屡次卑顺，有困难。六四，悔亡，田猎获三类猎物。九五，守正吉、悔亡；起初不顺终有成，庚日前三日及后三日均须谋划，吉。上九，谦顺在床下，失资财工具，守正凶。""",
    Entry("易经", "下经 · 第58卦 · 兑", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "兌：亨。利貞。\n初九：和兌，吉。\n九二：孚兌，吉，悔亡。\n六三：來兌，凶。\n九四：商兌，未寧，介疾有喜。\n九五：孚于剝，有厲。\n上六：引兌。", """译文说明：以下为本项目工作直译初稿。“兑”有喜悦、言说之义；“孚于剥”等句义较难，暂不附会为具体人物或事件。

兑：亨通，利于守正。
初九：和悦地与人相处，吉。
九二：以诚信表达喜悦，吉，悔恨消失。
六三：前来求取欢悦，凶。
九四：商议如何取悦他人，内心尚未安定；若能隔绝疾患，便有喜事。
九五：诚信却附着于剥落之物，存在危险。
上六：引导他人进入喜悦之中。""", chatGptTranslation = """兑：亨通，利于守正。初九，和悦，吉。九二，诚信而悦，吉且悔亡。六三，前来求悦，凶。九四，商议取悦之事，心未安；隔绝疾患则有喜。九五，诚信附着于剥落之物，有险。上六，引导喜悦。""",
    Entry("易经", "下经 · 第59卦 · 涣", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "渙：亨。王假有廟，利涉大川，利貞。\n初六：用拯馬壯，吉。\n九二：渙奔其机，悔亡。\n六三：渙其躬，无悔。\n六四：渙其群，元吉。渙有丘，匪夷所思。\n九五：渙汗其大號，渙王居，无咎。\n上九：渙其血，去逖出，无咎。", """译文说明：以下为本项目工作直译初稿。“涣”指离散、涣散；“王假有庙”“涣汗其大号”等句涉及古代祭祀与政令语境。

涣：亨通。君王来到宗庙，利于涉越大河，利于守正。
初六：借助强壮的马匹救助，吉。
九二：涣散之时奔向安稳的凭依处，悔恨消失。
六三：使自身从涣散中解脱，没有悔恨。
六四：使群体涣散而重新分化，大吉；涣散之中形成丘聚，非一般人所能想到。
九五：发布宏大的政令，如同汗液发散；君王居于王位，没有灾咎。
上九：使血污散去，远离危险而脱出，没有灾咎。""", chatGptTranslation = """涣：亨通，君王至宗庙，利于渡河、守正。初六，借强壮马匹救助，吉。九二，涣散时奔向凭依处，悔亡。六三，使自身脱离涣散，无悔。六四，使群体离散重组，大吉；涣散中聚成丘，非凡人所能料。九五，发布宏大号令，王居安定，无咎。上九，血污消散，远离危险而脱出，无咎。""",
    Entry("易经", "下经 · 第60卦 · 节", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "節：亨。苦節不可貞。\n初九：不出戶庭，无咎。\n九二：不出門庭，凶。\n六三：不節若，則嗟若，无咎。\n六四：安節，亨。\n九五：甘節，吉；往有尚。\n上六：苦節，貞凶，悔亡。", """译文说明：以下为本项目工作直译初稿。“节”指节制、制度与限度；“苦节”强调过度严苛的节制，不宜理解为一味忍耐。

节：亨通。过于艰苦严苛的节制，不可长久守持。
初九：不走出内室庭院，没有灾咎。
九二：不走出门庭，凶。
六三：若不加节制，就会因此叹息；知道叹息缘由，便没有灾咎。
六四：安于适当的节制，亨通。
九五：甘美而适度的节制，吉；前往会受到尊尚。
上六：过于艰苦的节制，守正也凶；悔恨消失。""", chatGptTranslation = """节：亨通，过度严苛的节制不可长久。初九，不出内庭，无咎。九二，不出门庭，凶。六三，不节制便叹息，能自知则无咎。六四，安于适度节制，亨通。九五，甘美适度的节制，吉，前往受尊。上六，苦于严苛节制，守正凶，悔亡。""",
    Entry("易经", "下经 · 第61卦 · 中孚", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "中孚：豚魚吉，利涉大川，利貞。\n初九：虞吉，有他不燕。\n九二：鳴鶴在陰，其子和之，我有好爵，吾與爾靡之。\n六三：得敵，或鼓或罷，或泣或歌。\n六四：月几望，馬匹亡，无咎。\n九五：有孚攣如，无咎。\n上九：翰音登于天，貞凶。", """译文说明：以下为本项目工作直译初稿。“中孚”指内心诚信；“豚鱼”“靡之”等词的象征义仍需结合古注校释。

中孚：诚信感通于豚与鱼，吉；利于涉越大河，利于守正。
初九：心中安定，吉；若另有他念，便不能安宁。
九二：鹤在树荫中鸣叫，小鹤应声相和。我有美酒佳肴，愿与你共同分享。
六三：遇到敌对之人，时而击鼓进攻，时而停止；时而哭泣，时而歌唱。
六四：月亮几乎圆满，马匹失去同伴，没有灾咎。
九五：诚信紧密相连，没有灾咎。
上九：鸣叫的声音直达天空，守正则凶。""", chatGptTranslation = """中孚：诚信感通豚鱼，吉；利于渡河、守正。初九，心安则吉，另有他念便不安。九二，鹤在荫处鸣，小鹤应和；我有美酒，与你共享。六三，遇敌，时鼓时罢，时哭时歌。六四，月近圆满，马匹失伴，无咎。九五，诚信相系，无咎。上九，鸣声上达天际，守正凶。""",
    Entry("易经", "下经 · 第62卦 · 小过", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "小過：亨。利貞。可小事，不可大事。飛鳥遺之音，不宜上宜下，大吉。\n初六：飛鳥以凶。\n六二：過其祖，遇其妣；不及其君，遇其臣；无咎。\n九三：弗過防之，從或戕之，凶。\n九四：无咎，弗過遇之。往厲必戒，勿用永貞。\n六五：密云不雨，自我西郊，公弋取彼在穴。\n上六：弗遇過之，飛鳥離之，凶，是謂災眚。", """译文说明：以下为本项目工作直译初稿。“小过”指小有逾越；“不宜上宜下”等句按原文方向意象翻译，不扩写成单一占验结论。

小过：亨通，利于守正。可以做小事，不可以做大事。飞鸟留下声音，不宜向上，宜于向下，大吉。
初六：飞鸟因贸然行动而凶。
六二：越过祖父而遇见祖母；没有遇见君王，却遇见了臣子，没有灾咎。
九三：没有越过危险却未加防备，跟从者可能伤害自己，凶。
九四：没有灾咎；不强行越过，反而能遇到合宜之事。前往有危险，必须戒慎；不要长久固执于一端。
六五：浓云密布却没有降雨，云气来自我方西郊；公侯用箭射取藏在穴中的猎物。
上六：没有遇到而强行越过，飞鸟因此陷入罗网，凶；这就是灾祸。""", chatGptTranslation = """小过：亨通，利于守正；可做小事，不可做大事。飞鸟遗音，宜下不宜上，大吉。初六，飞鸟贸然行动，凶。六二，越祖遇祖母，不及君而遇臣，无咎。九三，不越险而防备不足，随从者可能伤害自己，凶。九四，不强越而遇合宜之事，无咎；前往须戒慎，不可固执。六五，云密不雨，来自西郊；公侯射取穴中猎物。上六，强行越过，飞鸟陷网，凶，是为灾眚。""",
    Entry("易经", "下经 · 第63卦 · 既济", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "既濟：亨小。利貞。初吉終亂。\n初九：曳其輪，濡其尾，无咎。\n六二：婦喪其茀，勿逐，七日得。\n九三：高宗伐鬼方，三年克之，小人勿用。\n六四：繻有衣袽，終日戒。\n九五：東鄰殺牛，不如西鄰之禴祭，實受其福。\n上六：濡其首，厲。", """译文说明：以下为本项目工作直译初稿。“既济”指事情已经渡过、完成；本卦反复提示完成之后仍须戒慎。

既济：小事亨通，利于守正；起初吉利，最终可能陷入纷乱。
初九：拖住车轮，沾湿了尾巴，没有灾咎。
六二：妇人丢失了头饰，不必追寻，七日后会得到。
九三：高宗征伐鬼方，经过三年才取胜；不可任用小人。
六四：衣服破损，需要用旧衣絮补缀；整日保持戒备。
九五：东邻杀牛举行盛大祭祀，不如西邻举行简约的禴祭；后者实际得到福祉。
上六：头部被水浸湿，处境危险。""", chatGptTranslation = """既济：小事亨通，利于守正；初吉终乱。初九，拖住车轮，车尾沾湿，无咎。六二，妇人失头饰，不追，七日后得。九三，高宗伐鬼方，三年克之，不用小人。六四，衣破以旧絮补缀，整日戒备。九五，东邻杀牛盛祭，不如西邻简约禴祭而实受其福。上六，头部浸湿，危险。""",
    Entry("易经", "下经 · 第64卦 · 未济", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "未濟：亨。小狐汔濟，濡其尾，无攸利。\n初六：濡其尾，吝。\n九二：曳其輪，貞吉。\n六三：未濟，征凶，利涉大川。\n九四：貞吉，悔亡，震用伐鬼方，三年有賞于大國。\n六五：貞吉，无悔，君子之光，有孚，吉。\n上九：有孚于飲酒，无咎，濡其首，有孚失是。", """译文说明：以下为本项目工作直译初稿。“未济”指尚未渡过、尚未完成；“小狐汔济”等句以渡河意象贯穿全卦。

未济：亨通。小狐狸快要渡过河时，尾巴却被水沾湿，没有什么利益。
初六：尾巴被水沾湿，有困难。
九二：拖住车轮，守正吉利。
六三：尚未渡过，贸然前往凶；利于涉越大河。
九四：守正吉利，悔恨消失；以强大的力量征伐鬼方，经过三年，在大国获得奖赏。
六五：守正吉利，没有悔恨；君子的光明德行显现，心怀诚信，吉。
上九：心怀诚信而饮酒，没有灾咎；但若沉溺其中，头部被水浸湿，便是诚信失去了准则。""", chatGptTranslation = """未济：亨通。小狐狸将渡河而尾巴沾湿，无所利。初六，尾巴沾湿，有困难。九二，拖住车轮，守正吉。六三，未渡而前往凶，利于渡大河。九四，守正吉、悔亡；征伐鬼方，三年后受大国赏赐。六五，守正吉、无悔，君子光明，诚信而吉。上九，诚信饮酒无咎；若沉溺而首浸水，则失其正。""")
)

private fun buildWingsIndex(): List<Entry> = listOf(
    Entry("十翼", "《序卦传》", "底本：维基文库《易传/序卦》，通行本数字文本。已录上、下两篇；尚未与帛书、敦煌及宋元刻本逐字校勘。原文保留通行本字形，异文另行校记。", """
上篇
有天地，然后万物生焉。
盈天地之间者，唯万物，故受之以屯；屯者盈也，屯者物之始生也。
物生必蒙，故受之以蒙；蒙者蒙也，物之穉也。
物穉不可不养也，故受之以需；需者饮食之道也。
饮食必有讼，故受之以讼。
讼必有众起，故受之以师；师者众也。
众必有所比，故受之以比；比者比也。
比必有所畜也，故受之以小畜。
物畜然后有礼，故受之以履。
履而泰，然后安，故受之以泰；泰者通也。
物不可以终通，故受之以否。
物不可以终否，故受之以同人。
与人同者，物必归焉，故受之以大有。
有大者不可以盈，故受之以谦。
有大而能谦，必豫，故受之以豫。
豫必有随，故受之以随。
以喜随人者，必有事，故受之以蛊；蛊者事也。
有事而后可大，故受之以临；临者大也。
物大然后可观，故受之以观。
可观而后有所合，故受之以噬嗑；嗑者合也。
物不可以苟合而已，故受之以贲；贲者饰也。
致饰然后亨，则尽矣，故受之以剥；剥者剥也。
物不可以终尽，剥穷上反下，故受之以复。
复则不妄矣，故受之以无妄。
有无妄然后可畜，故受之以大畜。
物畜然后可养，故受之以颐；颐者养也。
不养则不可动，故受之以大过。
物不可以终过，故受之以坎；坎者陷也。
陷必有所丽，故受之以离；离者丽也。

下篇
有天地然后有万物，有万物然后有男女，有男女然后有夫妇，有夫妇然后有父子，有父子然后有君臣，有君臣然后有上下，有上下然后礼仪有所错。
夫妇之道，不可以不久也，故受之以恒；恒者久也。
物不可以久居其所，故受之以遯；遯者退也。
物不可终遯，故受之以大壮。
物不可以终壮，故受之以晋；晋者进也。
进必有所伤，故受之以明夷；夷者伤也。
伤于外者，必反其家，故受之以家人。
家道穷必乖，故受之以睽；睽者乖也。
乖必有难，故受之以蹇；蹇者难也。
物不可终难，故受之以解；解者缓也。
缓必有所失，故受之以损。
损而不已，必益，故受之以益。
益而不已，必决，故受之以夬；夬者决也。
决必有所遇，故受之以姤；姤者遇也。
物相遇而后聚，故受之以萃；萃者聚也。
聚而上者，谓之升，故受之以升。
升而不已，必困，故受之以困。
困乎上者，必反下，故受之以井。
井道不可不革，故受之以革。
革物者莫若鼎，故受之以鼎。
主器者莫若长子，故受之以震；震者动也。
物不可以终动，止之，故受之以艮；艮者止也。
物不可以终止，故受之以渐；渐者进也。
进必有所归，故受之以归妹。
得其所归者必大，故受之以丰；丰者大也。
穷大者必失其居，故受之以旅。
旅而无所容，故受之以巽；巽者入也。
入而后说之，故受之以兑；兑者说也。
说而后散之，故受之以涣；涣者离也。
物不可以终离，故受之以节。
节而信之，故受之以中孚。
有其信者，必行之，故受之以小过。
有过物者，必济，故受之以既济。
物不可穷也，故受之以未济；终焉。
""".trimIndent(), """译文说明：以下为本项目工作译文初稿，依原文次序逐句转述；此篇重在说明六十四卦的编排次序及其义理联系，不应据此把卦序解释视为唯一历史成因。

**上篇**
有了天地，然后才产生万物。充满天地之间的，都是万物，所以接着是屯卦；“屯”有充满之义，也指万物开始萌生。
万物产生之后必然处于蒙昧状态，所以接着是蒙卦；“蒙”就是蒙昧，是事物幼小的状态。
事物幼小，不能不加以养育，所以接着是需卦；“需”讲的是饮食之道。
饮食之事发生之后，必然产生争讼，所以接着是讼卦。
争讼必然会引起众人聚集，所以接着是师卦；“师”就是众人。
众人必然有所亲附，所以接着是比卦；“比”就是亲近、亲附。
亲附之后必然有所蓄积，所以接着是小畜卦。
事物蓄积之后才会形成礼节，所以接着是履卦。
履行礼节而后达到安泰，才能安定，所以接着是泰卦；“泰”就是通达。
事物不能永远通达，所以接着是否卦。
事物不能永远闭塞，所以接着是同人卦。
能够与人相同、与人协同，众物便会归附，所以接着是大有卦。
拥有广大之物，不可以自满，所以接着是谦卦。
拥有广大之物而又能够谦逊，必然安乐，所以接着是豫卦。
安乐之后必然有所跟随，所以接着是随卦。
因喜悦而跟随他人的人，必然会遇到事务，所以接着是蛊卦；“蛊”就是事务、事端。
有了事务之后，才可能有所发展壮大，所以接着是临卦；“临”有大的意思。
事物壮大之后，才值得观览，所以接着是观卦。
值得观览之后，便会有所会合，所以接着是噬嗑卦；“嗑”就是合拢、咬合。
事物不能只求苟且会合，所以接着是贲卦；“贲”就是文饰。
文饰发展到极致，便会走向尽头，所以接着是剥卦；“剥”就是剥落。
事物不能永远走向穷尽；剥落到了上极，便会返回下方，所以接着是复卦。
复归之后，便不再妄为，所以接着是无妄卦。
有了不妄为，才可以蓄积，所以接着是大畜卦。
事物蓄积之后，才可以养育，所以接着是颐卦；“颐”就是养育。
不能养育，便无法行动，所以接着是大过卦。
事物不能永远处于过度状态，所以接着是坎卦；“坎”就是陷入险境。
陷入险境之后，必然要有所依附，所以接着是离卦；“离”就是附丽、依附。

**下篇**
有了天地之后才有万物，有了万物之后才有男女，有了男女之后才有夫妇，有了夫妇之后才有父子，有了父子之后才有君臣，有了君臣之后才有上下尊卑，有了上下尊卑之后，礼仪制度才有所安置。
夫妇之道不能不长久，所以接着是恒卦；“恒”就是长久。
事物不能长久停留在原来的位置，所以接着是遁卦；“遁”就是退避。
事物不能永远退避，所以接着是大壮卦。
事物不能永远强盛，所以接着是晋卦；“晋”就是前进。
前进必然会遇到损伤，所以接着是明夷卦；“夷”就是受伤。
在外受到损伤的人，必然返回家中，所以接着是家人卦。
家庭之道发展到困窘处，必然发生乖离，所以接着是睽卦；“睽”就是乖离。
乖离必然带来艰难，所以接着是蹇卦；“蹇”就是困难。
事物不能永远处于困难之中，所以接着是解卦；“解”就是缓解、解除。
缓解之后，必然会有所失去，所以接着是损卦。
减损而不停，必然转为增益，所以接着是益卦。
增益而不停，必然需要决断，所以接着是夬卦；“夬”就是决断。
决断之后必然有所相遇，所以接着是姤卦；“姤”就是相遇。
事物相遇之后便会聚集，所以接着是萃卦；“萃”就是聚集。
聚集而向上发展，称为升，所以接着是升卦。
上升而不停，必然陷入困境，所以接着是困卦。
处于上方的困境之中，必然返回下方，所以接着是井卦。
井的使用之道不能不革新，所以接着是革卦。
革除旧物，没有什么比鼎更合适，所以接着是鼎卦。
主持祭器的人，没有谁比长子更合适，所以接着是震卦；“震”就是动。
事物不能永远运动，必须使它停止，所以接着是艮卦；“艮”就是止。
事物不能永远停止，所以接着是渐卦；“渐”就是逐渐前进。
前进必然有所归宿，所以接着是归妹卦。
得到归宿的事物必然壮大，所以接着是丰卦；“丰”就是盛大。
盛大到了极点，必然失去原来的居所，所以接着是旅卦。
旅居在外而无处容身，所以接着是巽卦；“巽”就是进入、顺入。
进入之后便会使人喜悦，所以接着是兑卦；“兑”就是喜悦。
喜悦之后便会离散，所以接着是涣卦；“涣”就是离散。
事物不能永远离散，所以接着是节卦。
节制而后才能建立诚信，所以接着是中孚卦。
有了诚信，就必须付诸行动，所以接着是小过卦。
有所超过之后，便能渡过，所以接着是既济卦。
事物不能穷尽，所以接着是未济卦；到这里，卦序终结。""", chatGptTranslation = """**ChatGPT 翻译**

**上篇**
天地存在之后，万物才随之产生。充满天地之间的都是万物，因此接续以屯卦；屯有充满之意，也表示万物初生。万物既生，必然幼稚蒙昧，所以接续以蒙卦；蒙就是蒙昧，也是事物幼小。幼小的事物需要养育，所以接续以需卦；需讲饮食之道。饮食之后容易发生争讼，因此接续以讼卦；争讼引起众人聚集，所以接续以师卦，师就是众。众人需要相互亲附，所以接续以比卦；亲附之后有所蓄积，故接续小畜。蓄积之后产生礼节，故接续履；履行礼节而后通泰，故接续泰。通达不能永远持续，故有否；闭塞也不能永远持续，故有同人。与人协同则众物归附，故有大有；拥有广大不可自满，故有谦；有大而能谦便安乐，故有豫。安乐之后有所跟随，故有随；因喜悦而随人便会遇到事务，故有蛊。处理事务后才能发展壮大，故有临；事物大了便值得观览，故有观。观览之后有所会合，故有噬嗑；不能苟且会合，故有贲。文饰到了极处便会剥落，故有剥；剥落不能永远持续，穷极则返回，故有复。复归之后不再妄为，故有无妄；不妄之后可以蓄积，故有大畜；蓄积之后需要养育，故有颐。不能养育便无法行动，故有大过；过度不能永久持续，故有坎；陷入险境后需要依附，故有离。

**下篇**
天地之后有万物，万物之后有男女，男女之后有夫妇，夫妇之后有父子，父子之后有君臣，君臣之后有上下尊卑，随后才有礼仪制度。夫妇之道需要长久，故有恒；事物不能永久停留原处，故有遁；不能永远退避，故有大壮；强盛不能永远持续，故有晋。前进会遇到损伤，故有明夷；外部受伤便返回家庭，故有家人；家道困窘便发生乖离，故有睽；乖离带来艰难，故有蹇；困难不能永远持续，故有解；缓解之后又会有所减损，故有损；减损不止则转为增益，故有益；增益不止则需要决断，故有夬；决断之后会有所相遇，故有姤；相遇之后聚集，故有萃；聚集而上升，故有升；上升不止则陷入困境，故有困；困于上便返回下方，故有井。井道需要革新，故有革；革除旧物以鼎为典型，故有鼎；主持祭器以长子为宜，故有震；动不能永久持续，故有艮；止也不能永久持续，故有渐；渐进之后有所归宿，故有归妹；有归宿便壮大，故有丰；盛大到极点会失去居所，故有旅；旅居无处容身，故有巽；进入之后产生喜悦，故有兑；喜悦之后离散，故有涣；离散不能永远持续，故有节；节制使诚信得以建立，故有中孚；有诚信便应付诸行动，故有小过；有所逾越而后能渡过，故有既济；万物不能穷尽，故以未济终结。

此篇是在说明通行卦序的义理关联，不等于证明六十四卦历史形成的唯一原因。"""),
    Entry("十翼", "《杂卦传》", "底本：维基文库《易传/杂卦》，通行本数字文本。一篇全文录入；校詁版存在文字差异，暂不混入正文，待另作异文校记。", """
乾刚坤柔，比乐师忧。
临、观之义，或与或求。
屯见而不失其居，蒙杂而著。
震起也，艮止也。
损、益，盛衰之始也。
大畜时也，无妄灾也。
萃聚，而升不来也。
谦轻，而豫怠也。
噬嗑食也，贲无色也。
兑见，而巽伏也。
随无故也，蛊则饬也。
剥烂也，复反也。
晋昼也，明夷诛也。
井通，而困相遇也。
咸速也，恒久也。
涣离也，节止也。
解缓也，蹇难也。
睽外也，家人内也。
否、泰，反其类也。
大壮则止，遯则退也。
大有众也，同人亲也。
革去故也，鼎取新也。
小过过也，中孚信也。
丰多故也，亲寡旅也。
离上，而坎下也。
小畜寡也，履不处也。
需不进也，讼不亲也。
大过颠也，姤遇也，柔遇刚也。
渐女归待男行也，颐养正也，既济定也。
归妹女之终也，未济男之穷也。
夬决也，刚决柔也。君子道长，小人道消也。
""".trimIndent(), """译文说明：以下为本项目工作译文初稿。此篇以简短对举概括卦义，部分短句高度凝练，译文尽量贴近字面；有歧义处不扩写为完整义理体系。

乾刚健，坤柔顺；比卦欢乐，师卦忧虑。
临与观的意义，有的是给予，有的是求取。
屯显现而不失其所居，蒙则杂乱而逐渐显著。
震是兴起，艮是停止。
损与益，是盛衰变化的开端。
大畜讲时机，无妄讲灾祸。
萃是聚集，升则有所上进而不返回。
谦表现为轻退，豫表现为安逸怠惰。
噬嗑是咀嚼饮食，贲则不以颜色为饰。
兑表现为显露，巽表现为隐伏。
随没有故旧牵累，蛊则是整治事务。
剥是腐烂剥落，复是返回。
晋象征白昼，明夷象征伤害与诛伐。
井是通达，困则是相遇而受阻。
咸来得迅速，恒则长久。
涣是离散，节是停止并有所节制。
解是缓解，蹇是艰难。
睽是向外乖离，家人是向内整合。
否与泰，彼此反转其类别。
大壮发展到极处便应停止，遁则退避。
大有是众多，同人是亲近。
革是除去旧物，鼎是取得新物。
小过是有所逾越，中孚是诚信。
丰是事多而盛大，旅则亲近者少。
离在上，坎在下。
小畜是蓄积较少，履则不能久处。
需是不前进，讼是不亲近。
大过是颠倒，姤是相遇，是柔顺者遇到刚强者。
渐是女子出嫁、等待男子行动；颐是养育正道，既济是事情已经安定。
归妹是女子婚嫁之事的终结，未济是男子处境的穷尽。
夬就是决断，是刚强者决去柔弱者。君子的道路增长，小人的道路消退。""", chatGptTranslation = """**ChatGPT 翻译**

乾刚健，坤柔顺；比象征亲附之乐，师象征众人之忧。临与观的意义，或在给予，或在求取。屯显现而不失其居所，蒙则杂而渐明。震是发动，艮是止息。损与益是盛衰变化的开端。大畜关乎时机，无妄关乎意外灾变。萃是聚集，升是上升而不返回。谦使人自处卑下，豫则可能流于安逸。噬嗑如咀嚼，贲则不以色彩为饰。兑显露，巽隐伏。随不受旧故拘系，蛊则整治积弊。剥是腐坏剥落，复是返回。晋象征白昼，明夷象征光明受伤。井使人通达，困则使人相遇而受阻。咸感应迅速，恒持久。涣是离散，节是节止。解是舒缓，蹇是艰难。睽向外乖离，家人向内整合。否与泰是相反的类别。大壮到极处应止，遁则退避。大有象征众多，同人象征亲近。革除旧物，鼎取新物。小过是有所逾越，中孚是诚信。丰是事多而盛，旅则亲近者少。离在上，坎在下。小畜蓄积少，履不久处。需不前进，讼不亲近。大过是颠倒，姤是相遇，也是柔遇刚。渐象征女子出嫁而等待男子行动；颐是养正，既济是已定。归妹是女子婚嫁的终结，未济是男子事业的穷尽。夬就是决断，以刚决柔；君子之道增长，小人之道消退。

本篇以简短对举概括卦义。个别句子极为凝练，尤其“屯见而不失其居”“随无故”“丰多故”等，仍须结合古注与异本校读，不宜把简译当作唯一解释。"""),
    Entry("十翼", "《大象传》", "底本：维基文库《周易/大象》，收录六十四卦大象辞。此篇属于《象传》系统；与《小象传》分开保存，后续将分别关联到各卦阅读页。尚未完成版本校勘。", """
乾：天行健，君子以自强不息。
坤：地势坤，君子以厚德载物。
屯：云雷，屯；君子以经纶。
蒙：山下出泉，蒙；君子以果行育德。
需：云上于天，需；君子以饮食宴乐。
讼：天与水违行，讼；君子以作事谋始。
师：地中有水，师；君子以容民畜众。
比：地上有水，比；先王以建万国，亲诸侯。
小畜：风行天上，小畜；君子以懿文德。
履：上天下泽，履；君子以辨上下，定民志。
泰：天地交，泰；后以财成天地之道，辅相天地之宜，以左右民。
否：天地不交，否；君子以俭德辟难，不可荣以禄。
同人：天与火，同人；君子以类族辨物。
大有：火在天上，大有；君子以遏恶扬善，顺天休命。
谦：地中有山，谦；君子以裒多益寡，称物平施。
豫：雷出地奋，豫；先王以作乐崇德，殷荐之上帝，以配祖考。
随：泽中有雷，随；君子以向晦入宴息。
蛊：山下有风，蛊；君子以振民育德。
临：泽上有地，临；君子以教思无穷，容保民无疆。
观：风行地上，观；先王以省方，观民设教。
噬嗑：雷电，噬嗑；先王以明罚敕法。
贲：山下有火，贲；君子以明庶政，无敢折狱。
剥：山附于地，剥；上以厚下安宅。
复：雷在地中，复；先王以至日闭关，商旅不行，后不省方。
无妄：天下雷行，物与无妄；先王以茂对时，育万物。
大畜：天在山中，大畜；君子以多识前言往行，以畜其德。
颐：山下有雷，颐；君子以慎言语，节饮食。
大过：泽灭木，大过；君子以独立不惧，遯世无闷。
坎：水洊至，习坎；君子以常德行，习教事。
离：明两作，离；大人以继明照于四方。
咸：山上有泽，咸；君子以虚受人。
恒：雷风，恒；君子以立不易方。
遯：天下有山，遯；君子以远小人，不恶而严。
大壮：雷在天上，大壮；君子以非礼弗履。
晋：明出地上，晋；君子以自昭明德。
明夷：明入地中，明夷；君子以莅众，用晦而明。
家人：风自火出，家人；君子以言有物，而行有恒。
睽：上火下泽，睽；君子以同而异。
蹇：山上有水，蹇；君子以反身修德。
解：雷雨作，解；君子以赦过宥罪。
损：山下有泽，损；君子以惩忿窒欲。
益：风雷，益；君子以见善则迁，有过则改。
夬：泽上于天，夬；君子以施禄及下，居德则忌。
姤：天下有风，姤；后以施命诰四方。
萃：泽上于地，萃；君子以除戎器，戒不虞。
升：地中生木，升；君子以顺德，积小以高大。
困：泽无水，困；君子以致命遂志。
井：木上有水，井；君子以劳民劝相。
革：泽中有火，革；君子以治历明时。
鼎：木上有火，鼎；君子以正位凝命。
震：洊雷，震；君子以恐惧修省。
艮：兼山，艮；君子以思不出其位。
渐：山上有木，渐；君子以居贤德善俗。
归妹：泽上有雷，归妹；君子以永终知敝。
丰：雷电皆至，丰；君子以折狱致刑。
旅：山上有火，旅；君子以明慎用刑，而不留狱。
巽：随风，巽；君子以申命行事。
兑：丽泽，兑；君子以朋友讲习。
涣：风行水上，涣；先王以享于帝立庙。
节：泽上有水，节；君子以制数度，议德行。
中孚：泽上有风，中孚；君子以议狱缓死。
小过：山上有雷，小过；君子以行过乎恭，丧过乎哀，用过乎俭。
既济：水在火上，既济；君子以思患而豫防之。
未济：火在水上，未济；君子以慎辨物居方。
""".trimIndent(), """译文说明：以下为本项目工作译文初稿。大象辞通常先取上下卦或卦象作比，再提出君子、先王的行为取法；译文按原句结构转述。

乾：天体运行刚健不息，君子因此自我奋勉，永不停止。
坤：大地的形势厚顺承载，君子因此以深厚德行承载万物。
屯：云与雷交会，象征屯难；君子因此经营筹划事务。
蒙：山下涌出泉水，象征蒙昧；君子因此果断行动、培育德行。
需：云气升到天上，象征等待；君子因此饮食宴乐，从容以待。
讼：天与水各自向相反方向运行，象征争讼；君子因此在做事之初就谋划周全。
师：地中有水，象征军众；君子因此容纳百姓、蓄养众人。
比：地上有水，象征亲附；先王因此建立万国，使诸侯亲近归附。
小畜：风运行在天上，象征小有蓄积；君子因此修美文德。
履：天在上、泽在下，象征践履礼节；君子因此辨明上下名分，安定民众心志。
泰：天地之气相交，象征通泰；君主因此裁成天地之道，辅助天地运行的适宜之处，以此治理百姓。
否：天地之气不相交，象征闭塞；君子因此以节俭的德行避开祸难，不以俸禄荣显自己。
同人：天与火相合，象征会同；君子因此区分类别、辨明事物。
大有：火在天上，象征丰有；君子因此抑制邪恶、彰显善行，顺从上天美好的命令。
谦：地中有山，象征谦逊；君子因此减少多余、补益不足，衡量事物而公平施予。
豫：雷从地中奋起，象征喜悦；先王因此制作音乐、尊崇德行，隆重祭祀上帝，并配享祖先。
随：泽中有雷，象征随从；君子因此在天色昏暗时回家休息。
蛊：山下有风，象征积弊；君子因此振作民众、培育德行。
临：泽上有地，象征亲临；君子因此教化与关怀没有穷尽，包容保护百姓没有边界。
观：风运行在大地之上，象征观览；先王因此巡察各地，观察民情并设立教化。
噬嗑：雷与电并现，象征咬合；先王因此明定刑罚、整饬法令。
贲：山下有火，象征文饰；君子因此明察各项政务，不轻率断决诉讼。
剥：山体依附于地，象征剥落；居上位者因此厚待下民、安定居所。
复：雷在地中，象征复归；先王因此在冬至闭关，商旅不行，君主也不巡察四方。
无妄：雷行于天下，万物各得其自然；先王因此顺应时序，养育万物。
大畜：天在山中，象征大为蓄积；君子因此广泛记取前人的言论与往事，以蓄养自己的德行。
颐：山下有雷，象征颐养；君子因此谨慎言语、节制饮食。
大过：泽水淹没树木，象征过度；君子因此独立而无所畏惧，退隐于世也不忧闷。
坎：水接连不断地到来，象征重重险陷；君子因此恒常保持德行，反复练习教化之事。
离：光明接连升起，象征附丽；大人因此延续光明，照耀四方。
咸：山上有泽，象征感应；君子因此虚怀若谷地接纳他人。
恒：雷与风相伴，象征恒久；君子因此确立恒常不变的方向。
遁：天下有山，象征退避；君子因此远离小人，不怀恶意却保持威严。
大壮：雷声在天上震动，象征强盛；君子因此不去践行不合礼义之事。
晋：光明从地上升起，象征前进；君子因此彰显自己的光明德行。
明夷：光明进入地下，象征光明受损；君子因此治理民众时内敛行事，却保持内在明察。
家人：风从火中生出，象征家人；君子因此言语有实质，行为有恒常。
睽：上方为火、下方为泽，象征乖离；君子因此在求同之中辨明差异。
蹇：山上有水，象征艰难；君子因此反省自身、修养德行。
解：雷雨兴起，象征解除；君子因此宽赦过失、宽宥罪责。
损：山下有泽，象征减损；君子因此惩治愤怒、抑制欲望。
益：风与雷相互激荡，象征增益；君子因此见到善行就努力学习，有了过错就及时改正。
夬：泽水升到天上，象征决断；君子因此施予俸禄、惠及下民，不把德行据为己有。
姤：天下有风，象征相遇；君主因此颁布命令，告知四方。
萃：泽水聚集在地上，象征聚合；君子因此整治兵器，戒备意外变故。
升：地中生长树木，象征上升；君子因此顺应德行，从细小处积累而达到高大。
困：泽中没有水，象征困顿；君子因此尽力履行使命、实现志向。
井：树木上面有水，象征井；君子因此勤劳为民，并劝勉众人相互帮助。
革：泽中有火，象征变革；君子因此制定历法、明察时令。
鼎：树木上面有火，象征鼎器；君子因此端正职位、凝聚天命。
震：雷声接连不断，象征震动；君子因此心存敬畏并反省自身。
艮：两座山并列，象征止；君子因此思虑不超出自己的职位与本分。
渐：山上有树木，象征渐进；君子因此安居于贤德之中，改善社会风俗。
归妹：泽上有雷，象征女子出嫁；君子因此考虑事情的长远终局，预知可能的弊病。
丰：雷与电一同到来，象征丰盛；君子因此审理诉讼、施行刑罚。
旅：山上有火，象征旅居；君子因此明察谨慎地施用刑罚，不滞留案件。
巽：风接连吹拂，象征顺入；君子因此反复申明命令并付诸行动。
兑：两泽相连，象征喜悦；君子因此与朋友讲习学问。
涣：风运行在水面上，象征离散；先王因此祭享上帝、建立宗庙。
节：泽上有水，象征节制；君子因此制定数量与法度，审议德行。
中孚：泽上有风，象征内心诚信；君子因此审慎审理诉讼、宽缓死刑。
小过：山上有雷，象征小有逾越；君子因此在行为上格外恭敬，在丧事上格外哀痛，在用度上格外节俭。
既济：水在火上，象征事情已经完成；君子因此思考祸患并预先防备。
未济：火在水上，象征事情尚未完成；君子因此谨慎辨别事物，使它们各居其所。""", chatGptTranslation = """ChatGPT 翻译（逐卦工作译文）：
乾：天体运行刚健不息，君子因此自我奋勉，永不停止。
坤：大地厚实而顺承，君子因此以深厚德行承载万物。
屯：云与雷聚合成屯象，君子因此经营筹划，建立秩序。
蒙：山下涌出泉水，象征蒙昧；君子因此果断行动、培育德行。
需：云气升在天上，象征等待；君子因此饮食宴乐，养足力量。
讼：天与水各自背向运行，象征争讼；君子因此做事先谋划开端。
师：地中有水，象征军众；君子因此容纳百姓、蓄养众人。
比：地上有水，象征亲比；先王因此建立诸侯国，亲近诸侯。
小畜：风运行于天上，象征小有蓄聚；君子因此修美文德。
履：上为天下为泽，象征践履；君子因此分辨上下，使民心安定。
泰：天地之气交感通达；君主因此裁成天地之道，辅助天地之宜，以安定百姓。
否：天地之气不相交；君子因此节俭修德、避开祸难，不以禄位为荣。
同人：天与火相应，象征与人和同；君子因此区分类别、辨明事物。
大有：火在天上，象征丰盛；君子因此抑制恶行、彰显善行，顺应天命的美好安排。
谦：地中有山，象征谦逊；君子因此减多补少，衡量事物而公平施与。
豫：雷从地中奋起，象征喜悦；先王因此制作音乐、崇尚德行，隆重祭祀上帝并配享祖先。
随：泽中有雷，象征随从；君子因此在日暮时回家休息。
蛊：山下有风，象征积弊待治；君子因此振作百姓、培育德行。
临：泽上有地，象征临近；君子因此教化思虑无穷，包容保护百姓没有边界。
观：风行地上，象征观察；先王因此巡省四方，观察民俗并施行教化。
噬嗑：雷电交作，象征咬合；先王因此明察刑罚、整饬法令。
贲：山下有火，象征文饰；君子因此明察政务，不敢轻率断狱。
剥：山附于地，象征剥落；上位者因此厚待下民，使其安居。
复：雷在地中发动，象征复归；先王因此在冬至闭关，商旅不行，君主也不巡行四方。
无妄：天下雷行，万物各自顺应天时；先王因此顺应时令，养育万物。
大畜：天在山中，象征大有蓄积；君子因此广泛学习前代言论与事迹，以积累德行。
颐：山下有雷，象征颐养；君子因此谨慎言语、节制饮食。
大过：泽水淹没树木，象征过度；君子因此独立无惧，避世而不闷。
坎：水流重重相续，象征险陷；君子因此常修德行、反复讲习教事。
离：光明附着于万物，象征依附；大人因此承接光明、照临四方。
咸：山上有泽，象征感应；君子因此虚心接纳他人。
恒：雷与风相伴，象征恒久；君子因此坚定立场，不随风俗改变。
遯：天下有山，象征退避；君子因此远离小人，不以恶意对待他们，而以严正态度保持距离。
大壮：雷在天上震动，象征强盛；君子因此不践踏礼法。
晋：明亮的太阳升在大地之上；君子因此彰明自身光明的德行。
明夷：光明进入地下，象征光明受伤；君子因此在众人之中韬晦光明，却仍保持内心正直。
家人：风从火中生起，象征家道；君子因此言语有实、行为有恒。
睽：上有火、下有泽，二者背离；君子因此求同存异。
蹇：山上有水，前行艰难；君子因此反省自身、修养德行。
解：雷雨兴起，百果草木舒展；君子因此宽赦过失、体恤罪责。
损：山下有泽，象征减损；君子因此惩治忿怒、克制欲望。
益：风雷相互激荡，象征增益；君子因此见善即学，有过即改。
夬：泽水升至天上，象征决断；君子因此施恩于下，不把德行积藏起来。
姤：天下有风，象征相遇；君主因此发布命令，告示四方。
萃：泽上有地，象征聚集；君子因此修治兵器、戒备不测。
升：地中生出木，象征上升；君子因此顺应德行，积小成高。
困：泽中无水，象征困穷；君子因此以生命实践志向。
井：木上有水，象征汲井；君子因此慰劳百姓、劝勉相助。
革：泽中有火，象征变革；君子因此按时制定历法、明定时令。
鼎：木上有火，象征烹饪；君子因此端正位置、凝聚天命。
震：雷声接连而来，象征震动；君子因此心存戒惧、反省修身。
艮：兼山相叠，象征止；君子因此思虑不越出本位。
渐：山上有木，树木逐渐生长；君子因此居于贤德之位，以改善风俗。
归妹：泽上有雷，象征少女出嫁；君子因此明白事物有始有终，知道其终点。
丰：雷电同时出现，象征丰盛；君子因此断案明察、施行刑罚。
旅：山上有火，火势不久停留；君子因此明察审慎，不轻用刑罚，也不拖延诉讼。
巽：风接连吹拂，象征顺入；君子因此反复申明命令、推行事务。
兑：两泽相连，象征喜悦；君子因此与朋友讲习学问。
涣：风运行在水面上，象征离散；先王因此祭享上帝、建立宗庙。
节：泽上有水，象征节制；君子因此制定数量与法度，审议德行。
中孚：泽上有风，象征内心诚信；君子因此审慎审理诉讼、宽缓死刑。
小过：山上有雷，象征小有逾越；君子因此在行为上格外恭敬，在丧事上格外哀痛，在用度上格外节俭。
既济：水在火上，象征事情已经完成；君子因此思考祸患并预先防备。
未济：火在水上，象征事情尚未完成；君子因此谨慎辨别事物，使它们各居其所。"""),
    Entry("十翼", "《彖传上》", "底本：维基文库《周易/彖》，上经三十卦。保留底本文字形态；尚未完成与帛书、敦煌及宋元刻本逐字校勘。", """
乾：大哉乾元，萬物資始，乃統天。雲行雨施，品物流形。大明終始，六位時成。時乘六龍以御天。乾道變化，各正性命。保合太和，乃利貞。
坤：至哉坤元，萬物資生，乃順承天。坤厚載物，德合無疆。含弘光大，品物咸亨。牝馬地類，行地無疆，柔順利貞。君子攸行，先迷失道，後順得常。西南得朋，乃與類行。東北喪朋，乃終有慶。安貞之吉，應地無疆。
屯：屯，剛柔始交而難生。動乎險中，大亨貞。雷雨之動滿盈，天造草昧。宜尋建侯而不寧。
蒙：蒙，山下有險，險而止，蒙。「蒙亨」，以亨行，時中也。「匪我求童蒙，童蒙求我」。志應也。「初筮告」，以剛中也。「再三瀆，瀆則不告」，瀆蒙也。蒙以養正，聖功也。
需：「需」，須也。險在前也，剛健而不陷，其義不困窮矣。「需，有孚，光亨，貞吉」，位乎天位，以正中也。「利涉大川」，往有功也。
訟：訟，上剛下險，險而健，訟。「訟有孚窒惕，中吉」，剛來而得中也。「終凶」，訟不可成也。「利見大人」，尚中正也。「不利涉大川」，入於淵也。
師：師，眾也。貞，正也。能以眾正，可以王矣。剛中而應，行險而順，以此毒天下，而民從之，吉又何咎矣。
比：比，吉也；比，輔也，下順從也。「原筮，元永貞，無咎」，以剛中也。「不寧方來」，上下應也。「後夫凶」，其道窮也。
小畜：「小畜」，柔得位而上下應之，曰小畜。健而巽，剛中而志行，乃亨。「密雲不雨」，尚往也。「自我西郊」，施未行也。
履：「履」，柔履剛也。說而應乎乾，是以「履虎尾，不咥人」。亨，剛中正，履帝位而不疚，光明也。
泰：「泰，小往大來。吉，亨。」則是天地交而萬物通也，上下交而其志同也。內陽而外陰，內健而外順，內君子而外小人，君子道長，小人道消也。
否：「否之匪人，不利君子貞，大往小來。」則是天地不交而萬物不通也，上下不交而天下無邦也；內陰而外陽，內柔而外剛，內小人而外君子，小人道長，君子道消也。
同人：「同人」，柔得位得中，而應乎乾，曰同人。同人曰：「同人於野，亨。利涉大川」，乾行也。文明以健，中正而應，君子正也。唯君子為能通天下之志。
大有：「大有」，柔得尊位，大中而上下應之，曰「大有」。其德剛健而文明，應乎天而時行，是以元亨。
謙：謙，亨。天道下濟而光明，地道卑而上行。天道虧盈而益謙，地道變盈而流謙，鬼神害盈而福謙，人道惡盈而好謙。謙，尊而光，卑而不可逾，君子之終也。
豫：豫，剛應而志行，順以動，豫。豫順以動，故天地如之，而況建侯行師乎？天地以順動，故日月不過，而四時不忒。聖人以順動，則刑罰清而民服，豫之時義大矣哉！
隨：隨，剛來而下柔，動而說，隨。大亨貞無咎，而天下隨時，隨時之義大矣哉！
蠱：蠱，剛上而柔下，巽而止，蠱。蠱，元亨而天下治也。「利涉大川」，往有事也。「先甲三日，後甲三日」，終則有始，天行也。
臨：臨，剛浸而長，說而順，剛中而應。大亨以正，天之道也。「至於八月有凶」，消不久也。
觀：大觀在上，順而巽，中正以觀天下，觀。「盥而不薦，有孚顒若」，下觀而化也。觀天之神道，而四時不忒，聖人以神道設教，而天下服矣。
噬嗑：頤中有物曰噬嗑。噬嗑而亨，剛柔分，動而明，雷電合而章。柔得中而上行，雖不當位，利用獄也。
賁：賁亨，柔來而文剛，故亨。分，剛上而文柔，故小利有攸往。剛柔交錯，天文也。文明以止，人文也。觀乎天文，以察時變；觀乎人文，以化成天下。
剝：剝，剝也。柔變剛也。「不利有攸往」，小人長也。順而止之，觀象也。君子尚消息盈虛，天行也。
復：「復，亨」。剛反，動而以順行。是以「出入無疾，朋來無咎」。「反復其道，七日來復」，天行也。「利有攸往」，剛長也。復，其見天地之心乎。
無妄：無妄，剛自外來而為主於內，動而健，剛中而應。大亨以正，天之命也。「其匪正有眚，不利有攸往」，無妄之往何之矣？天命不祐，行矣哉！
大畜：大畜，剛健篤實，輝光日新。其德剛上而尚賢，能止健，大正也。「不家食吉」，養賢也。「利涉大川」，應乎天也。
頤：頤，貞吉，養正則吉也。觀頤，觀其所養也。自求口實，觀其自養也。天地養萬物，聖人養賢以及萬民，頤之時大矣哉！
大過：「大過」，大者過也。「棟撓」，本末弱也。剛過而中，巽而說，行。利有攸往，乃亨。「大過」之時大矣哉！
坎：「習坎」，重險也。水流而不盈。行險而不失其信。維心亨，乃以剛中也。「行有尚」，往有功也。天險，不可陞也。地險，山川丘陵也。王公設險以守其國。險之時用大矣哉！
離：離，麗也。日月麗乎天，百谷草木麗乎土。重明以麗乎正，乃化成天下。柔麗乎中正，故，是以「畜牝牛吉」也。
""".trimIndent(), """译文说明：本条为逐卦工作译述初稿，按上经三十卦次序对应。彖辞中的卦体、爻位和象数术语尽量保留，不将后世注家的单一解释写成定论。

乾：伟大啊，乾的创始之气！万物凭借它开始，它统摄天道。云气运行、雨水施布，各类事物因此形成形态。光明贯通始终，六爻各在适当时位形成。依时驾驭六条阳爻之龙，便能运行天道。乾道不断变化，使万物各自端正其性命；保持并融合至大的和谐，才有利于守正。
坤：至极啊，坤的创生之德！万物凭借它获得生长，它顺承天道。坤厚实而承载万物，德行与广大无边相合。含容弘厚、光明广大，各类事物都因此通达。母马属于地类，行走大地没有边界；柔顺而守正有利。君子有所行动，若抢先便迷失道路，随后顺应则得到常道；西南得到同类，东北失去同类，最终仍有喜庆。安定守正的吉祥，与大地一样广大无边。
屯：屯是刚柔开始交会而艰难随之产生。处在险中而能发动，守正便可大为亨通。雷雨发动，充满天地，天地初创时万物尚在草昧之中。适宜建立诸侯，不可因此自以为安宁。
蒙：蒙昧，是山下有险而又停止不前的状态。“蒙亨”，是因为能够以亨通之道行事，合乎时宜而居中。“不是我去求童蒙，而是童蒙来求我”，说明双方心志相应。“初次占问便告知”，因为刚爻居中；“再三烦渎便不告”，是因为蒙昧者轻慢占问。以蒙昧养成正道，是圣人之功。
需：需就是等待，危险在前；刚健而不陷入危险，所以其道义不会困穷。“需，有诚信，光明亨通，守正吉”，是因为居于天位而得正得中。“利于涉越大河”，是说前往能够建立功绩。
讼：上卦刚健、下卦险陷，既险又健，所以形成争讼。“争讼有诚信却受阻而警惕，中途吉”，因为刚爻来到而得中；“最终凶”，因为争讼不能成为可持续之事。“利于见大人”，因为崇尚中正；“不利于涉越大河”，因为进入深渊。
师：师就是众人，贞就是正。能够使众人归于正道，便可以成就王业。刚爻居中而有应，行动虽涉险却能顺从正理；即使因此使天下承受劳苦，百姓仍会跟从，又有什么灾咎呢？
比：比有吉祥、辅佐、下者顺从之义。“开始占问，元善、长久、守正而无咎”，因为刚爻居中。“不安宁的诸方前来”，因为上下相应；“后来者凶”，因为其道已经穷尽。
小畜：柔爻得位，上下与之相应，所以称小畜。刚健而能谦顺，刚爻居中且志向得以施行，因此亨通。“密云不雨”，是因为云气仍向前运行；“从我的西郊而来”，说明恩泽尚未施行。
履：履是柔爻践履刚爻。内心喜悦而顺应乾，所以虽踩到虎尾，老虎也不咬人。亨通，是因为刚爻居中得正，履行帝位而没有过失，光明正大。
泰：小者往、大者来，吉而亨通；这是天地交合、万物通畅，上下交接、心志相同。阳气在内、阴气在外，内刚健而外柔顺，君子居内而小人居外，君子之道增长、小人之道消退。
否：闭塞不通，不利于君子守正；大者离去、小者到来。这是天地不交、万物不通，上下不交而天下失去秩序。阴气在内、阳气在外，内柔而外刚，小人在内而君子在外，小人之道增长、君子之道消退。
同人：柔爻得位得中，并与乾相应，所以称同人。“与人在旷野会同，亨通，利于涉越大河”，体现乾道运行。文明而刚健，中正而相应，是君子的正道；只有君子能够通达天下人的心志。
大有：柔爻居于尊位，居中广大而上下都与之相应，所以称大有。其德刚健而文明，顺应天道并依时而行，因此大为亨通。
谦：谦能够亨通。天道向下施济而光明，地道处卑下而向上运行。天道使盈满者亏损而补益谦逊者，地道使盈满者改变而流向谦逊者；鬼神损害盈满者而福佑谦逊者，人道厌恶自满而喜爱谦逊。谦逊者地位虽尊而德光明，虽处卑下却不可逾越，这是君子善终之道。
豫：刚爻响应而志向得以施行，顺应而发动，所以安乐。安乐而顺应，天地尚且如此，何况建立诸侯、出动军队呢？天地顺时而动，因此日月运行不失度，四时更替不差错；圣人顺时而动，刑罚清明而百姓服从。豫卦的时义确实重大。
随：随是刚爻来到下位而居于柔爻之下，发动而喜悦。大为亨通、守正而无咎，天下也随顺时势；随顺时势的意义十分重大。
蛊：刚爻居上、柔爻居下，谦顺而停止，形成蛊。整治积弊，便能使天下治理。“利于涉越大河”，是说前往有所作为。“甲日前三日、甲日后三日”，说明终结之后又有开始，这是天道运行。
临：刚健之气逐渐增长，内心喜悦而外在顺从，刚爻居中并有应。大为亨通而合乎正道，是天道。“到了八月有凶”，是说阳气消退不会持续太久。
观：伟大的观览居于上位，以顺从谦逊、中正之道观察天下，这就是观。“行了灌洗礼却尚未献祭，诚信而庄严”，说明下民观看上位者而受到感化。观察天道神妙的运行，四时便不会差错；圣人以神妙之道设立教化，天下因此服从。
噬嗑：颐中有物，称为噬嗑。咬合而后通畅，刚柔分明，行动而明察，雷电相合而显现威势。柔爻得中并向上行动，虽然不处于正位，却适宜用于审理刑狱。
贲：贲能够亨通，是柔爻来到下方文饰刚爻的缘故；刚爻上升文饰柔爻，因此小事上有所前往有利。刚柔交错，是天的文采；文明而有所节止，是人文。观察天文，可以考察时序变化；观察人文，可以教化天下。
剥：剥就是剥落，是柔爻逐渐改变刚爻。“不利于有所前往”，因为小人之势增长。顺应时势而停止行动，是观察卦象所得的启示。君子重视消长、盈虚的变化，因为这是天道运行。
复：复能够亨通，是刚健之气返回，发动而顺行。因此出入没有灾害，朋友到来没有灾咎；往复其道，七日来复，是天道运行。“利于有所前往”，因为阳刚正在增长。复卦或许显现了天地生生之心。
无妄：刚爻从外而来，成为内卦主爻，行动刚健，刚爻居中而有应。大为亨通而合乎正道，是天命。“若不正便有灾祸，不利于前往”，无妄之行还能去往哪里？若违背天命，怎能继续行动？
大畜：大畜刚健、笃实，光辉日日更新。其德是刚爻居上而尊崇贤者，能够止住刚健之势，这是大正之道。“不在家中食禄而吉”，是因为能够养育贤才；“利于涉越大河”，是因为顺应天道。
颐：颐卦守正吉祥，因为养育合乎正道便吉。观察颐养之道，就是观察所养的对象；寻求口中食物，就是观察如何自我养育。天地养育万物，圣人养育贤者并推及百姓，颐养之时义十分重大。
大过：大过是大的方面超过常度。“栋梁弯曲”，因为根本与末端都弱。刚强虽过度却能居中，谦顺而喜悦，仍可行动；利于有所前往，便能亨通。大过之时义十分重大。
坎：习坎是重重险陷。水流而不盈满，身处险境仍不失诚信；内心通达，是因为刚爻居中。“行动有所崇尚”，是说前往能够建立功绩。天险不可登越，地险是山川丘陵；王公设置险阻来守卫国家。险难之时的作用十分重大。
离：离就是附丽。日月附丽于天，百谷草木附丽于土地；光明重叠而附丽于正道，才能教化成就天下。柔爻附丽于中正之位，所以说蓄养母牛吉祥。""", chatGptTranslation = """ChatGPT 翻译（逐卦工作译文）：
乾：伟大啊，乾的创始之德！万物凭借它开始生长，它统领天道。云气运行、雨水降下，各类事物因此呈现形态。日月光明彰显始终，六爻各依时位形成；适时驾驭六条龙来运行天道。乾道变化，使万物各自端正其本性与命分；保持并会合太和之气，才有利于正固。
坤：至极啊，坤的创生之德！万物依靠它获得生长，它顺承天道。坤厚实而能承载万物，其德广大无疆；含容弘厚、光明广大，各类事物都得以亨通。母马属于地类，行走大地而无边际，柔顺而守正有利。君子有所行动时，先行会迷失道路，后行顺从则能获得常道。西南得朋，是与同类同行；东北失朋，最终反而有喜庆。安于正固的吉祥，与大地无疆的德性相应。
屯：屯是刚柔开始交合、艰难随之发生。处在险中而能发动，守正便可大为亨通。雷雨发动，气势充满，天地开辟之初仍处草昧混沌，所以适宜建立诸侯，不可安逸懈怠。
蒙：蒙象征蒙昧，山下有险，遇险而止便是蒙。所谓“蒙而亨通”，是以亨通之道行事，并能合乎时宜中正。不是我去求童蒙，而是童蒙来求我，说明心志相应。初次占问便告知，是因为刚爻居中；反复亵渎占问，亵渎就不再告知，这是因为蒙昧者轻慢。以蒙昧状态培养正道，是成就圣人的功业。
需：需就是等待，因为危险在前。刚健而不陷入险境，所以其义不至困穷。“有诚信、光明亨通、守正吉祥”，是因为居于天位而得正得中。“利于涉越大河”，是说前往能够建立功绩。
讼：讼卦上刚下险，既险又强健，因此形成争讼。“有诚信却受阻，心存警惕，中途吉祥”，是刚爻来到而居中。“最终凶险”，因为争讼不可能真正成功。“利于见大人”，因为崇尚中正之道；“不利于涉越大河”，因为进入深渊般的危险。
师：师象征军众，众人顺从而处险中，顺从便能危险中行进；刚爻居中而得到尊位，所以能够以此行事。所谓“统率军队吉祥、没有灾咎”，是因为能够使百姓顺从；君王三次赐命，是因为怀有万民之心。
比：比是亲近辅助。下顺从而上亲附，刚爻居中而得正，众阴爻顺从它，所以吉祥。原筮元永贞而无咎，是因为刚爻居中；不安定的诸侯前来归附，是因为上下相应；迟迟不来的人终有凶险，因为道路已经走到尽头。
小畜：柔爻得位而上下相应，所以能够聚集众阳。说“密云不雨”，是因为云气仍从我方西郊升起；施行尚未普及，所以雨还没有降下。君子崇尚文德，是因为能够以柔顺方式行事。
履：柔爻践履刚强之处并与之相应，所以能够亨通。说“践履虎尾而不被咬”，是因为能够喜悦而顺从。刚健居中正，所以履行帝位而不受困迫；光明而刚健，位置适当并能回应天道。
泰：小的阴气离去，大的阳气到来，所以亨通。天地交感，万物通达；上下交接，志向相同。内阳而外阴，内刚而外柔，内君子而外小人；君子之道增长，小人之道消退，所以吉祥而亨通。
否：小人之道增长，君子之道消退。天地不交，万物不通；上下不相交，天下没有邦国可成。内阴而外阳，内柔而外刚，内小人而外君子；小人之道增长，君子之道消退。
同人：柔爻得位而居中，与乾相应，所以称为同人。同人说“同人于野，亨通，利于涉越大河”，是因为乾行刚健；文明而刚健，得中而相应，所以君子之正道通达。只有君子能够使天下志向相同。
大有：柔爻居尊位而得中，上下刚爻都与之相应，所以称为大有。其德刚健而文明，顺应天道而行，因此亨通。
谦：谦是使高者降低、使低者升高，衡量事物而使其平衡。天道减损盈满而增益谦虚，地道改变盈满而流向谦虚，鬼神加害盈满而福佑谦虚，人道厌恶盈满而喜爱谦虚。谦逊而尊贵、光明，地位低下却不可逾越；君子有始有终。
豫：豫是顺动。刚爻回应而志向得以实行，顺从而发动，所以天地也如同顺势而动，更何况建立侯国、出兵行动？天地依顺势而动，因此日月不过失时，四季也不会错乱；圣人依顺势而动，刑罚清明而百姓服从。豫的时义十分重大。
随：随的含义是刚来而居于下，柔上而居于上，动而喜悦，所以随。大亨通、守正无咎，天下便会顺从；天下顺从之时，随的意义何其重大。
蛊：蛊是事物败坏。刚爻居上而柔爻居下，巽顺而停止，蛊由此形成。蛊能够大为亨通，是因为天下之事有所治理；“利于涉越大河”，是往前行动便有功；先甲三日、后甲三日，说明终结之后又有开端，天道运行如此。
临：临是刚渐渐增长。说“大亨通而守正有利”，是因为天道运行到第八个月便有消退。来到百姓身边而施行教化，没有穷尽；容纳保护百姓，没有边界。临的时义十分重大。
观：大观在上，柔顺而中正，观察天下。观天道神妙的变化，四时运行因此没有差错；圣人以神妙之道设立教化，天下百姓因此服从。观察的意义十分重大。
噬嗑：颐中有物，必须咬合，所以称噬嗑。刚柔分开而发动，雷电合并出现；柔爻得中而居上位，虽不居正位却有利于用刑狱。
贲：贲是文饰。柔爻来文饰刚爻，所以亨通；刚爻分开而上行，柔爻分开而下行，天地文理由此显现。文明止于适当处，才是人文；观察天文可以了解时序，观察人文可以教化天下。
剥：剥是剥落。柔爻变化刚爻，阴柔逐渐增长而阳刚消退；顺应时势而停止，不可有所前往。君子尚能乘车，小人则剥毁其庐舍；君子得以安身，小人却失去依托。
复：复能够亨通，是因为刚爻返回。动而顺行，所以出入没有阻碍；朋友来往没有过失，反复其道，七日而来复，是顺应天行。利于有所前往，是因为刚爻正在增长。复的时义十分重大。
无妄：刚爻自外而来成为主爻，行动刚健而中正，回应天道并顺应时势，因此大为亨通、守正吉祥。若不正当而妄为，就会有灾祸；不利于有所前往，因为无妄之时不宜妄动。无妄之时义十分重大。
大畜：刚健笃实而光明，日日更新其德。刚爻居上而能尊尚贤者，能止健，所以大畜。利于守正，是因为不使家中饮食匮乏而吉祥；利于涉越大河，是因为顺应天道。
颐：颐是养正。观察颐象，应当谨慎言语、节制饮食；天地养育万物，圣人养育贤者以至万民。颐的时义十分重大。
大过：大过是大的方面超过常度。栋梁弯曲，因为根本与末端都弱；刚强虽过度却能居中，谦顺而喜悦，仍可行动。利于有所前往，便能亨通。大过之时义十分重大。
坎：习坎是重重险陷。水流而不盈满，身处险境仍不失诚信；内心通达，是因为刚爻居中。“行动有所崇尚”，是说前往能够建立功绩。天险不可登越，地险是山川丘陵；王公设置险阻来守卫国家。险难之时的作用十分重大。
离：离就是附丽。日月附丽于天，百谷草木附丽于土地；光明重叠而附丽于正道，才能教化成就天下。柔爻附丽于中正之位，所以说蓄养母牛吉祥。"""),
    Entry("十翼", "《彖传下》", "底本：维基文库《周易/彖》，下经三十四卦。保留底本文字形态；尚未完成与帛书、敦煌及宋元刻本逐字校勘。", """
咸：咸，感也。柔上而剛下，二氣感應以相與。止而說，男下女，是以「亨利貞，取女吉」也。天地感而萬物化生，聖人感人心而天下和平。觀其所感，而天地萬物之情可見矣。
恆：恆，久也。剛上而柔下。雷風相與，巽而動，剛柔皆應，恆。「恆亨無咎利貞」，久於其道也。天地之道恆久而不已也。「利有攸往」，終則有始也。日月得天而能久照，四時變化而能久成。聖人久於其道而天下化成。觀其所恆，而天地萬物之情可見矣。
遯：「遯亨」，遯而亨也。剛當位而應，與時行也。「小利貞」，浸而長也。遯之時義大矣哉！
大壯：大壯，大者壯也。剛以動，故壯。「大壯利貞」，大者正也。正大，而天地之情可見矣。
晉：晉，進也，明出地上。順而麗乎大明，柔進而上行，是以「康侯用錫馬蕃庶，晝日三接」也。
明夷：明入地中，「明夷」。內文明而外柔順，以蒙大難，文王以之。「利艱貞」，晦其明也，內難而能正其志，箕子以之。
家人：家人，女正位乎內，男正位乎外。男女正，天地之大義也。家人有嚴君焉，父母之謂也。父父，子子，兄兄，弟弟，夫夫，婦婦，而家道正。正家而天下定矣。
睽：睽，火動而上，澤動而下。二女同居，其志不同行。說而麗乎明，柔進而上行，得中而應乎剛，是以小事吉。天地睽而其事同也。男女睽而其志通也。萬物睽而其事類也，睽之時用大矣哉！
蹇：蹇，難也，險在前也。見險而能止，知矣哉！蹇，利西南，往得中也。「不利東北」，其道窮也。「利見大人」，往有功也。當位「貞吉」，以正邦也。蹇之時用大矣哉！
解：解，險以動，動而免乎險，解。「解，利西南」，往得眾也。「其來復吉」，乃得中也。「有攸往夙吉」，往有功也。天地解而雷雨作，雷雨作而百果草木皆甲坼。解之時大矣哉！
損：損，損下益上，其道上行。損而有孚，元吉，無咎，可貞，利有攸往，曷之用？二簋可用享。二簋應有時。損剛益柔有時，損益盈虛，與時偕行。
益：「益」，損上益下，民說無疆。自上下下，其道大光。「利有攸往」，中正有慶。「利涉大川」，木道乃行。益動而巽，日進無疆。天施地生，其益無方。凡益之道，與時偕行。
夬：「夬」，決也，剛決柔也。健而說，決而和。「揚於王庭」，柔乘五剛也。「孚號有厲」，其危乃光也。「告自邑，不利即戎」，所尚乃窮也。「利有攸往」，剛長乃終也。
姤：姤，遇也，柔遇剛也。「勿用取女」，不可與長也。天地相遇，品物咸章也。剛遇中正，天下大行也。姤之時義大矣哉！
萃：「萃」，聚也。順以說，剛中而應，故聚也。「王假有廟」，致孝享也。「利見大人亨」，聚以正也。「用大牲吉，利有攸往」，順天命也。觀其所聚，而天地萬物之情可見矣。
升：柔以時升，巽而順，剛中而應，是以大亨，「用見大人勿恤」，有慶也。「南征吉」，志行也。
困：「困」，剛掩也。險以說，因而不失其所，亨，其唯君子乎。「貞大人吉」，以剛中也。「有言不信」，尚口乃窮也。
井：巽乎水而上水，井。井養而不窮也。「改邑不改井」，乃以剛中也。「汔至，亦未繘井」，未有功也。「羸其甁」，是以凶也。
革：革，水火相息，二女同居，其志不相得曰革。「已日乃孚」，革而信之。文明以說，大亨以正。革而當，其悔乃亡。天地革而四時成，湯武革命，順乎天而應乎人。革之時大矣哉！
鼎：鼎，象也。以木巽火，亨飪也。聖人亨以享上帝，而大亨以養聖賢。巽而耳目聰明，柔進而上行，得中而應乎剛，是以元亨。
震：震，亨。「震來虩虩」，恐致福也。「笑言啞啞」，後有則也。「震驚百里」，驚遠而懼邇也。「不喪匕鬯」，出可以守宗廟社稷，以為祭主也。
艮：艮，止也。時止則止，時行則行，動靜不失其時，其道光明。「艮其止」，止其所也。上下敵應，不相與也。是以「不獲其身，行其庭，不見其人，無咎」也。
漸：漸之進也，女歸吉也。進得位，往有功也。進以正，可以正邦也。其位剛得中也。止而巽，動不窮也。
歸妹：歸妹，天地之大義也。天地不交而萬物不興。歸妹，人之終始也。說以動，所歸妹也。「徵凶」，位不當也。「無攸利」，柔乘剛也。
豐：豐，大也。明以動，故豐。「王假之」，尚大也。「勿憂宜日中」，宜照天下也。日中則昃，月盈則食，天地盈虛，與時消息，而況於人乎，況於鬼神乎？
旅：「旅小亨」，柔得中乎外，而順乎剛，止而麗乎明，是以「小亨旅貞吉」也。旅之時義大矣哉！
巽：重巽以申命。剛巽乎中正而志行。柔皆順乎剛，是以「小亨，利有攸往，利見大人」。
兌：兌，說也。剛中而柔外，說以利貞，是以順乎天而應乎人。說以先民，民忘其勞。說以犯難，民忘其死。說之大，民勸矣哉！
渙：「渙，亨」，剛來而不窮，柔得位乎外而上同。「王假有廟」，王乃在中也。「利涉大川」，乘木有功也。
節：「節亨」。剛柔分而剛得中。「苦節不可貞」，其道窮也。說以行險，當位以節，中正以通。天地節而四時成。節以制度，不傷財，不害民。
中孚：「中孚」，柔在內而剛得中，說而巽，孚乃化邦也。「豚魚吉」，信及豚魚也。「利涉大川」，乘木舟虛也。中孚以利貞，乃應乎天也。
小過：小過，小者過而亨也。過以利貞，與時行也。柔得中，是以小事吉也。剛失位而不中，是以不可大事也。有飛鳥之象焉，「飛鳥遺之音，不宜上，宜下，大吉」，上逆而下順也。
既濟：「既濟，亨」，小者亨也。「利貞」。剛柔正而位當也。「初吉」，柔得中也。「終止則亂」，其道窮也。
未濟：「未濟，亨」，柔得中也。「小狐汔濟」，未出中也。「濡其尾，無攸利」，不續終也。雖不當位，剛柔應也。
""".trimIndent(), """译文说明：本条为逐卦工作译述初稿，按下经三十四卦次序对应。卦体、爻位和象类判断依原文转述；婚姻、政治与象数术语不作超出原文的扩展。

咸：咸就是感应。柔爻居上、刚爻居下，阴阳二气彼此感应而相亲相与。下艮为止，上兑为悦，象征男子谦下于女子，所以卦辞说亨通、利于守正、娶女吉。天地相互感应，万物便化生；圣人感通人心，天下便和平。观察感应的情形，就能看见天地万物的真实情状。
恒：恒就是长久。刚爻居上、柔爻居下，雷与风相互配合，谦顺而发动，刚柔各爻彼此相应，这就是恒。“恒久、亨通、无咎、利于守正”，是因为长久坚持正道。天地之道恒久不息；“利于有所前往”，因为终结之后又有开始。日月依天运行而能长久照耀，四时变化而能长久成就万物；圣人长久行道，天下便受到教化。观察恒常之道，可以看见天地万物的情状。
遁：“遁而亨通”，是说退避也能通达。刚爻居正位而有应，能够随时势行动。“小事利于守正”，是因为阴柔之势逐渐增长。遁卦所体现的时势意义十分重大。
大壮：大壮就是阳刚强盛。刚健而发动，所以强盛。“大壮利于守正”，因为强大必须合乎正道。正大之理由此显现，天地的情状也可以看见。
晋：晋就是前进，光明从地上升起。顺从而附丽于光明，柔爻向上进升，所以有康侯受到赏赐众多马匹、一天多次接见的象征。
明夷：光明进入地下，便是明夷。内在文明、外在柔顺，以此承受重大艰难，周文王就是这样做的。“利于艰难中守正”，是说隐藏自己的光明；内有忧患却能端正志向，箕子就是这样做的。
家人：女子在内居正位，男子在外居正位；男女各正其位，是天地的大义。家中有威严的主宰，说的就是父母。父亲尽父道、儿子尽子道、兄长尽兄道、弟弟尽弟道、丈夫尽夫道、妻子尽妇道，家道便端正。家治理好了，天下也就安定。
睽：睽是火向上运动、泽向下流动；两个女子同处一室，心志却不相同。内心喜悦而依附光明，柔爻向上进升，得中而响应刚爻，所以小事吉。天地虽然相背，其功用却相同；男女虽然不同，其心志仍可相通；万物形态各异，事理却有相类之处。睽卦的时势作用十分重大。
蹇：蹇就是艰难，危险就在前方。看见危险而能够停止，这是明智。蹇卦利于向西南前行，因为前往可以得到中正；不利于向东北，因为道路已经穷尽。利于见大人，因为前往能够建立功绩。爻位得正而守正吉祥，可以端正邦国。蹇卦的时势作用十分重大。
解：解是处在险境而行动，行动之后脱离危险。解卦利于西南前往，因为能够得到众人；返回则吉，因为能够得中；有所前往而及早行动吉，因为前往有功。天地解除闭塞，雷雨便兴起；雷雨兴起，百果草木的种子外壳都裂开。解卦的时义十分重大。
损：损是减损下方而增益上方，其道向上运行。减损而有诚信，便大吉、无咎、可以守正，也利于有所前往。祭祀用什么？两簋食物也可以献享；两簋之礼应当依时而用。减损刚健、增益柔顺有其时机，损益、盈虚都应与时势同行。
益：益是减损上方而增益下方，百姓的喜悦没有边际。由上位施惠于下，其道光明广大。利于前往，因为中正而有喜庆；利于涉越大河，因为木舟之道可以通行。益卦发动而谦顺，日日进益无边；天施予、地生长，其增益没有固定边界。凡是增益之道，都应与时势同行。
夬：夬就是决断，是刚强者决去柔弱者。刚健而喜悦，决断而能和谐。“在王庭宣扬”，是因为柔爻乘凌五个刚爻；“诚信呼号而有危险”，危险因此显明；“告知自己的城邑，不利于立即动武”，因为所崇尚的办法会走到穷尽；“利于有所前往”，因为刚阳增长终将完成。
姤：姤就是相遇，是柔爻遇到刚爻。“不宜娶此女子”，是说不能与之长久相处。天地相遇，各类事物便显明；刚爻遇到中正之位，天下之道便大为通行。姤卦的时义十分重大。
萃：萃就是聚集。顺从而喜悦，刚爻居中并有应，所以能够聚集。“王来到宗庙”，是为了表达孝敬祭享；“利于见大人而亨通”，是因为聚集必须合乎正道；“用大牲祭祀吉，利于有所前往”，是因为顺应天命。观察聚集的情形，就能看见天地万物的真实情状。
升：柔顺之气依时上升，谦顺而有刚爻居中相应，因此大为亨通；“见大人不必忧虑”，因为会有喜庆；“向南征行吉”，因为志向能够实现。
困：困是刚阳受到遮蔽。处险而能喜悦，困顿中不失其所，仍能亨通，大概只有君子能够做到。“守正而大人吉”，因为刚爻居中；“说话无人相信”，是因为只崇尚口舌便会走向穷困。
井：入于水中而把水提上来，象征井。井养育人而不会穷尽。“迁移城邑而井不能迁移”，是因为刚爻居中。“水快到井口，却还没有用绳汲水”，说明尚未成功；“打破水瓶”，所以凶。
革：革是水火相互改变，两个女子同居而心志不相得，所以称革。“到了适当的日子才取得信任”，是说变革之后必须使人信服。文明而喜悦，大为亨通且合乎正道；变革得当，悔恨才会消失。天地变革而形成四时，商汤、周武王变革天命，顺应天道并回应人心。革卦的时义十分重大。
鼎：鼎是一种象征。以木顺入火中，用来烹煮食物。圣人烹煮食物以祭享上帝，又以丰盛食物养育圣贤。谦顺而耳聪目明，柔爻向上进升、得中而响应刚爻，因此大为亨通。
震：震能够亨通。“震来时恐惧不安”，是因为敬畏可以带来福分；“随后笑语欢畅”，是因为之后能够遵循法则；“震动惊及百里”，是说远方受到惊动，近处也感到畏惧；“不丢失祭祀用的匕与鬯”，说明出外仍可守护宗庙社稷，担任祭主。
艮：艮就是停止。应当停止时便停止，应当行动时便行动，动静都不失时机，道便光明。“止于应止之处”，就是停在适当位置。上下爻彼此敌对而不相应，因此卦辞所说不执取自身、行经庭院却不见其人，反而没有灾咎。
渐：渐就是逐步前进，女子出嫁吉祥。前进而得到适当位置，行动便有功；以正道前进，可以端正邦国。其刚爻居中得位；能够停止而又谦顺，行动便不会穷尽。
归妹：归妹是天地的大义。天地不交合，万物便不能兴起；婚嫁也是人伦关系的开始与终结。喜悦而行动，正是女子出嫁的情形。“征行凶”，因为爻位不当；“没有什么利益”，因为柔爻乘凌刚爻。
丰：丰就是盛大。光明而发动，所以丰盛。“王来到这里”，是崇尚盛大；“不要忧虑，宜如日居中天”，是应当照耀天下。太阳到中天便偏西，月亮满盈便亏缺；天地的盈虚都随时消长，何况人和鬼神呢？
旅：“旅小亨”，是柔爻在外卦得中而顺从刚爻，内止而外附丽于光明，所以旅居之时小有亨通，守正吉祥。旅卦的时势意义十分重大。
巽：重叠的巽象，用来反复申明命令。刚爻顺入中正之位，志向得以施行；柔爻都顺从刚爻，所以小有亨通，利于前往，也利于见大人。
兑：兑就是喜悦。刚爻居中、柔爻在外，以喜悦之道守正，因此顺应天道、回应人心。以喜悦引导百姓，百姓便忘记劳苦；以喜悦使百姓面对艰难，百姓便忘记死亡。喜悦的力量何其广大，足以鼓舞百姓。
涣：涣能够亨通，是刚爻来到内卦而不陷于穷困，柔爻在外卦得位并向上与之相合。“王来到宗庙”，说明王居中主持；“利于涉越大河”，是因为乘坐木舟能够建立功绩。
节：节能够亨通。刚柔分布而刚爻得中。“苦于过度节制，不可长久守持”，因为道路会走到穷尽。以喜悦之心行于险境，居位得当而有所节制，中正便能通达。天地有节度，四时才能形成；以制度节制，不损害财物，也不伤害百姓。
中孚：中孚是柔爻在内、刚爻得中，内心喜悦而谦顺；诚信由此能够教化邦国。“诚信及于豚鱼”，说明诚信能够感及难以感化之物；“利于涉越大河”，是因为乘木舟而舟中虚空。中孚以守正为利，因而与天道相应。
小过：小过是小的方面有所超过，因此仍可亨通。超过常度而守正有利，应当随时势行动。柔爻得中，所以小事吉；刚爻失位且不得中，所以不宜做大事。卦中有飞鸟之象：“飞鸟留下声音，不宜向上，宜向下，大吉”，因为向上逆势，向下顺势。
既济：既济能够亨通，是小的方面已经通达；利于守正，因为刚柔各爻得正、位置适当。“开始吉”，因为柔爻得中；“最终停止便会混乱”，因为道路已经走到穷尽。
未济：未济能够亨通，是因为柔爻得中。“小狐狸快要渡过河”，却还没有离开险境；“尾巴被沾湿，没有什么利益”，是因为不能把事情坚持到最后。虽然爻位不当，刚柔之间仍然相应。""", chatGptTranslation = """ChatGPT 翻译整理稿（沿用本条既有逐段译文结构；本轮先完成译文并列归档，尚需逐句对照原文复核，不作为独立校勘定本）：

译文说明：本条为逐卦工作译述初稿，按下经三十四卦次序对应。卦体、爻位和象类判断依原文转述；婚姻、政治与象数术语不作超出原文的扩展。

咸：咸就是感应。柔爻居上、刚爻居下，阴阳二气彼此感应而相亲相与。下艮为止，上兑为悦，象征男子谦下于女子，所以卦辞说亨通、利于守正、娶女吉。天地相互感应，万物便化生；圣人感通人心，天下便和平。观察感应的情形，就能看见天地万物的真实情状。
恒：恒就是长久。刚爻居上、柔爻居下，雷与风相互配合，谦顺而发动，刚柔各爻彼此相应，这就是恒。“恒久、亨通、无咎、利于守正”，是因为长久坚持正道。天地之道恒久不息；“利于有所前往”，因为终结之后又有开始。日月依天运行而能长久照耀，四时变化而能长久成就万物；圣人长久行道，天下便受到教化。观察恒常之道，可以看见天地万物的情状。
遁：“遁而亨通”，是说退避也能通达。刚爻居正位而有应，能够随时势行动。“小事利于守正”，是因为阴柔之势逐渐增长。遁卦所体现的时势意义十分重大。
大壮：大壮就是阳刚强盛。刚健而发动，所以强盛。“大壮利于守正”，因为强大必须合乎正道。正大之理由此显现，天地的情状也可以看见。
晋：晋就是前进，光明从地上升起。顺从而附丽于光明，柔爻向上进升，所以有康侯受到赏赐众多马匹、一天多次接见的象征。
明夷：光明进入地下，便是明夷。内在文明、外在柔顺，以此承受重大艰难，周文王就是这样做的。“利于艰难中守正”，是说隐藏自己的光明；内有忧患却能端正志向，箕子就是这样做的。
家人：女子在内居正位，男子在外居正位；男女各正其位，是天地的大义。家中有威严的主宰，说的就是父母。父亲尽父道、儿子尽子道、兄长尽兄道、弟弟尽弟道、丈夫尽夫道、妻子尽妇道，家道便端正。家治理好了，天下也就安定。
睽：睽是火向上运动、泽向下流动；两个女子同处一室，心志却不相同。内心喜悦而依附光明，柔爻向上进升，得中而响应刚爻，所以小事吉。天地虽然相背，其功用却相同；男女虽然不同，其心志仍可相通；万物形态各异，事理却有相类之处。睽卦的时势作用十分重大。
蹇：蹇就是艰难，危险就在前方。看见危险而能够停止，这是明智。蹇卦利于向西南前行，因为前往可以得到中正；不利于向东北，因为道路已经穷尽。利于见大人，因为前往能够建立功绩。爻位得正而守正吉祥，可以端正邦国。蹇卦的时势作用十分重大。
解：解是处在险境而行动，行动之后脱离危险。解卦利于西南前往，因为能够得到众人；返回则吉，因为能够得中；有所前往而及早行动吉，因为前往有功。天地解除闭塞，雷雨便兴起；雷雨兴起，百果草木的种子外壳都裂开。解卦的时义十分重大。
损：损是减损下方而增益上方，其道向上运行。减损而有诚信，便大吉、无咎、可以守正，也利于有所前往。祭祀用什么？两簋食物也可以献享；两簋之礼应当依时而用。减损刚健、增益柔顺有其时机，损益、盈虚都应与时势同行。
益：益是减损上方而增益下方，百姓的喜悦没有边际。由上位施惠于下，其道光明广大。利于前往，因为中正而有喜庆；利于涉越大河，因为木舟之道可以通行。益卦发动而谦顺，日日进益无边；天施予、地生长，其增益没有固定边界。凡是增益之道，都应与时势同行。
夬：夬就是决断，是刚强者决去柔弱者。刚健而喜悦，决断而能和谐。“在王庭宣扬”，是因为柔爻乘凌五个刚爻；“诚信呼号而有危险”，危险因此显明；“告知自己的城邑，不利于立即动武”，因为所崇尚的办法会走到穷尽；“利于有所前往”，因为刚阳增长终将完成。
姤：姤就是相遇，是柔爻遇到刚爻。“不宜娶此女子”，是说不能与之长久相处。天地相遇，各类事物便显明；刚爻遇到中正之位，天下之道便大为通行。姤卦的时义十分重大。
萃：萃就是聚集。顺从而喜悦，刚爻居中并有应，所以能够聚集。“王来到宗庙”，是为了表达孝敬祭享；“利于见大人而亨通”，是因为聚集必须合乎正道；“用大牲祭祀吉，利于有所前往”，是因为顺应天命。观察聚集的情形，就能看见天地万物的真实情状。
升：柔顺之气依时上升，谦顺而有刚爻居中相应，因此大为亨通；“见大人不必忧虑”，因为会有喜庆；“向南征行吉”，因为志向能够实现。
困：困是刚阳受到遮蔽。处险而能喜悦，困顿中不失其所，仍能亨通，大概只有君子能够做到。“守正而大人吉”，因为刚爻居中；“说话无人相信”，是因为只崇尚口舌便会走向穷困。
井：入于水中而把水提上来，象征井。井养育人而不会穷尽。“迁移城邑而井不能迁移”，是因为刚爻居中。“水快到井口，却还没有用绳汲水”，说明尚未成功；“打破水瓶”，所以凶。
革：革是水火相互改变，两个女子同居而心志不相得，所以称革。“到了适当的日子才取得信任”，是说变革之后必须使人信服。文明而喜悦，大为亨通且合乎正道；变革得当，悔恨才会消失。天地变革而形成四时，商汤、周武王变革天命，顺应天道并回应人心。革卦的时义十分重大。
鼎：鼎是一种象征。以木顺入火中，用来烹煮食物。圣人烹煮食物以祭享上帝，又以丰盛食物养育圣贤。谦顺而耳聪目明，柔爻向上进升、得中而响应刚爻，因此大为亨通。
震：震能够亨通。“震来时恐惧不安”，是因为敬畏可以带来福分；“随后笑语欢畅”，是因为之后能够遵循法则；“震动惊及百里”，是说远方受到惊动，近处也感到畏惧；“不丢失祭祀用的匕与鬯”，说明出外仍可守护宗庙社稷，担任祭主。
艮：艮就是停止。应当停止时便停止，应当行动时便行动，动静都不失时机，道便光明。“止于应止之处”，就是停在适当位置。上下爻彼此敌对而不相应，因此卦辞所说不执取自身、行经庭院却不见其人，反而没有灾咎。
渐：渐就是逐步前进，女子出嫁吉祥。前进而得到适当位置，行动便有功；以正道前进，可以端正邦国。其刚爻居中得位；能够停止而又谦顺，行动便不会穷尽。
归妹：归妹是天地的大义。天地不交合，万物便不能兴起；婚嫁也是人伦关系的开始与终结。喜悦而行动，正是女子出嫁的情形。“征行凶”，因为爻位不当；“没有什么利益”，因为柔爻乘凌刚爻。
丰：丰就是盛大。光明而发动，所以丰盛。“王来到这里”，是崇尚盛大；“不要忧虑，宜如日居中天”，是应当照耀天下。太阳到中天便偏西，月亮满盈便亏缺；天地的盈虚都随时消长，何况人和鬼神呢？
旅：“旅小亨”，是柔爻在外卦得中而顺从刚爻，内止而外附丽于光明，所以旅居之时小有亨通，守正吉祥。旅卦的时势意义十分重大。
巽：重叠的巽象，用来反复申明命令。刚爻顺入中正之位，志向得以施行；柔爻都顺从刚爻，所以小有亨通，利于前往，也利于见大人。
兑：兑就是喜悦。刚爻居中、柔爻在外，以喜悦之道守正，因此顺应天道、回应人心。以喜悦引导百姓，百姓便忘记劳苦；以喜悦使百姓面对艰难，百姓便忘记死亡。喜悦的力量何其广大，足以鼓舞百姓。
涣：涣能够亨通，是刚爻来到内卦而不陷于穷困，柔爻在外卦得位并向上与之相合。“王来到宗庙”，说明王居中主持；“利于涉越大河”，是因为乘坐木舟能够建立功绩。
节：节能够亨通。刚柔分布而刚爻得中。“苦于过度节制，不可长久守持”，因为道路会走到穷尽。以喜悦之心行于险境，居位得当而有所节制，中正便能通达。天地有节度，四时才能形成；以制度节制，不损害财物，也不伤害百姓。
中孚：中孚是柔爻在内、刚爻得中，内心喜悦而谦顺；诚信由此能够教化邦国。“诚信及于豚鱼”，说明诚信能够感及难以感化之物；“利于涉越大河”，是因为乘木舟而舟中虚空。中孚以守正为利，因而与天道相应。
小过：小过是小的方面有所超过，因此仍可亨通。超过常度而守正有利，应当随时势行动。柔爻得中，所以小事吉；刚爻失位且不得中，所以不宜做大事。卦中有飞鸟之象：“飞鸟留下声音，不宜向上，宜向下，大吉”，因为向上逆势，向下顺势。
既济：既济能够亨通，是小的方面已经通达；利于守正，因为刚柔各爻得正、位置适当。“开始吉”，因为柔爻得中；“最终停止便会混乱”，因为道路已经走到穷尽。
未济：未济能够亨通，是因为柔爻得中。“小狐狸快要渡过河”，却还没有离开险境；“尾巴被沾湿，没有什么利益”，是因为不能把事情坚持到最后。虽然爻位不当，刚柔之间仍然相应。"""),
    Entry("十翼", "《小象传》", "收录进度：上经三十卦、下经三十四卦共六十四卦的小象原文全部录入，共386条语句（含乾用九、坤用六）。本轮完成巽、兑、涣、节、中孚、小过、既济、未济。底本线索：维基文库《周易/小象》固定电子文本，并逐卦参照《周易》分卦页面；对逐卦页面与聚合索引存在差异处，保留校勘说明，不把数字文本核对冒充版本学定论。译文均为项目工作直译初稿，非权威定本，疑难训义仍待进一步对照古注、古刻本与出土文献。参考：https://zh.wikisource.org/zh-hans/周易/小象 ；https://zh.wikisource.org/zh-hans/周易/巽 ；https://zh.wikisource.org/zh-hans/周易/兑 ；https://zh.wikisource.org/zh-hans/周易/涣 ；https://zh.wikisource.org/zh-hans/周易/节 ；https://zh.wikisource.org/zh-hans/周易/中孚 ；https://zh.wikisource.org/zh-hans/周易/小过 ；https://zh.wikisource.org/zh-hans/周易/既济 ；https://zh.wikisource.org/zh-hans/周易/未济 。", """
【上经·第一卦 乾】
初九：潜龙勿用，阳在下也。
九二：见龙在田，德施普也。
九三：终日乾乾，反复道也。
九四：或跃在渊，进无咎也。
九五：飞龙在天，大人造也。
上九：亢龙有悔，盈不可久也。
用九：用九，天德不可为首也。

【上经·第二卦 坤】
初六：履霜坚冰，阴始凝也。驯致其道，至坚冰也。
六二：六二之动，直以方也。不习无不利，地道光也。
六三：含章可贞，以时成也。或从王事，知光大也。
六四：括囊无咎，慎不害也。
六五：黄裳元吉，文在中也。
上六：龙战于野，其道穷也。
用六：用六永贞，以大终也。

【上经·第三卦 屯】
初九：虽磐桓，志行正也。以贵下贱，大得民也。
六二：六二之难，乘刚也。十年乃字，反常也。
六三：即鹿无虞，以从禽也。君子舍之，往吝穷也。
六四：求而往，明也。
九五：屯其膏，施未光也。
上六：泣血涟如，何可长也。

【上经·第四卦 蒙】
初六：利用刑人，以正法也。
九二：子克家，刚柔接也。
六三：勿用取女，行不顺也。
六四：困蒙之吝，独远实也。
六五：童蒙之吉，顺以巽也。
上九：利用御寇，上下顺也。

【上经·第五卦 需】
初九：需于郊，不犯难行也。利用恒，无咎，未失常也。
九二：需于沙，衍在中也。虽小有言，以吉终也。
九三：需于泥，灾在外也。自我致寇，敬慎不败也。
六四：需于血，顺以听也。
九五：酒食，贞吉，以中正也。
上六：不速之客来，敬之终吉。虽不当位，未大失也。

【上经·第六卦 讼】
初六：不永所事，讼不可长也。虽小有言，其辩明也。
九二：不克讼，归逋窜也。自下讼上，患至掇也。
六三：食旧德，从上吉也。
九四：复即命渝，安贞，不失也。
九五：讼，元吉，以中正也。
上九：以讼受服，亦不足敬也。

【上经·第七卦 师】
初六：师出以律；失律，凶也。
九二：在师中吉，承天宠也。王三锡命，怀万邦也。
六三：师或舆尸，大无功也。
六四：左次无咎，未失常也。
六五：长子帅师，以中行也。弟子舆尸，使不当也。
上六：大君有命，以正功也。小人勿用，必乱邦也。

【上经·第八卦 比】
初六：比之初六，有它吉也。
六二：比之自内，不自失也。
六三：比之匪人，不亦伤乎！
六四：外比于贤，以从上也。
九五：显比之吉，位正中也。舍逆取顺，失前禽也。邑人不诫，上使中也。
上六：比之无首，无所终也。

【上经·第九卦 小畜】
初九：复自道，其义吉也。
九二：牵复在中，亦不自失也。
九三：夫妻反目，不能正室也。
六四：有孚。惕出。上合志也。
九五：有孚挛如，不独富也。
上九：既雨既处，德积载也。君子征凶，有所疑也。

【上经·第十卦 履】
初九：素履之往，独行愿也。
九二：幽人贞吉，中不自乱也。
六三：眇能视，不足以有明也。跛能履，不足以与行也。咥人之凶，位不当也。武人为于大君，志刚也。
九四：愬愬终吉，志行也。
九五：夬履贞厉，位正当也。
上九：元吉在上，大有庆也。

【上经·第十一卦 泰】
初九：拔茅征吉，志在外也。
九二：包荒，得尚于中行，以光大也。
九三：无往不复，天地际也。
六四：翩翩不富，皆失实也。不戒以孚，中心愿也。
六五：以祉，元吉。中以行愿也。
上六：城复于隍，其命乱也。

【上经·第十二卦 否】
初六：拔茅贞吉，志在君也。
六二：大人否，亨。不乱群也。
六三：包羞，位不当也。
九四：有命无咎，志行也。
九五：大人之吉，位正当也。
上九：否终则倾，何可长也。

【上经·第十三卦 同人】
初九：出门同人，又谁咎也。
六二：同人于宗，吝道也。
九三：伏戎于莽，敌刚也。三岁不兴，安行也。
九四：乘其庸，义弗克也，其吉，则困而反则也。
九五：同人之先，以中直也。大师相遇，言相克也。
上九：同人于郊，志未得也。

【上经·第十四卦 大有】
初九：大有初九，无交害也。
九二：大舆以载，积中不败也。
九三：公用亨于天子，小人害也。
九四：匪其彭，无咎；明辨晰也。
六五：厥孚交如，信以发志也。威如之吉，易而无备也。
上九：大有上吉，自天祐也。

【上经·第十五卦 谦】
初六：谦谦君子，卑以自牧也。
六二：鸣谦贞吉，中心得也。
九三：劳谦君子，万民服也。
六四：无不利，㧑谦，不违则也。
六五：利用侵伐，征不服也。
上六：鸣谦，志未得也。可用行师，征邑国也。

【上经·第十六卦 豫】
初六：初六鸣豫，志穷凶也。
六二：不终日，贞吉；以中正也。
六三：盱豫有悔，位不当也。
九四：由豫，大有得；志大行也。
六五：六五贞疾，乘刚也。恒不死，中未亡也。
上六：冥豫在上，何可长也。

【上经·第十七卦 随】
初九：官有渝，从正吉也。出门交有功，不失也。
六二：系小子，弗兼与也。
六三：系丈夫，志舍下也。
九四：随有获，其义凶也。有孚在道，明功也。
九五：孚于嘉，吉；位正中也。
上六：拘系之，上穷也。

【上经·第十八卦 蛊】
初六：干父之蛊，意承考也。
九二：干母之蛊，得中道也。
九三：干父之蛊，终无咎也。
六四：裕父之蛊，往未得也。
六五：干父用誉，承以德也。
上九：不事王侯，志可则也。
【上经·第十九卦 临】
初九：咸临，贞吉；志行正也。
九二：咸临，吉无不利；未顺命也。
六三：甘临，位不当也。既忧之，咎不长也。
六四：至临无咎，位当也。
六五：大君之宜，行中之谓也。
上六：敦临之吉，志在内也。

【上经·第二十卦 观】
初六：初六童观，小人道也。
六二：窥观女贞，亦可丑也。
六三：观我生进退，未失道也。
六四：观国之光，尚宾也。
九五：观我生，观民也。
上九：观其生，志未平也。

【上经·第二十一卦 噬嗑】
初九：屦校灭趾，不行也。
六二：噬肤灭鼻，乘刚也。
六三：遇毒，位不当也。
九四：利艰贞吉，未光也。
六五：贞厉无咎，得当也。
上九：何校灭耳，聪不明也。

【上经·第二十二卦 贲】
初九：舍车而徒，义弗乘也。
六二：贲其须，与上兴也。
九三：永贞之吉，终莫之陵也。
六四：六四当位疑也。匪寇婚媾，终无尤也。
六五：六五之吉，有喜也。
上九：白贲无咎，上得志也。

【上经·第二十三卦 剥】
初六：剥床以足，以灭下也。
六二：剥床以辨，未有与也。
六三：剥之无咎，失上下也。
六四：剥床以肤，切近灾也。
六五：以宫人宠，终无尤也。
上九：君子得舆，民所载也。小人剥庐，终不可用也。

【上经·第二十四卦 复】
初九：不远之复，以修身也。
六二：休复之吉，以下仁也。
六三：频复之厉，义无咎也。
六四：中行独复，以从道也。
六五：敦复无悔，中以自考也。
上六：迷复之凶，反君道也。

【上经·第二十五卦 无妄】
初九：无妄之往，得志也。
六二：不耕获，未富也。
六三：行人得牛，邑人灾也。
九四：可贞无咎，固有之也。
九五：无妄之药，不可试也。
上九：无妄之行，穷之灾也。

【上经·第二十六卦 大畜】
初九：有厉利已，不犯灾也。
九二：舆说輹，中无尤也。
九三：利有攸往，上合志也。
六四：六四元吉，有喜也。
六五：六五之吉，有庆也。
上九：何天之衢，道大行也。
【上经·第二十七卦 颐】
初九：观我朵颐，亦不足贵也。
六二：六二征凶，行失类也。
六三：十年勿用，道大悖也。
六四：颠颐之吉，上施光也。
六五：居贞之吉，顺以从上也。
上九：由颐厉吉，大有庆也。

【上经·第二十八卦 大过】
初六：藉用白茅，柔在下也。
九二：老夫女妻，过以相与也。
九三：栋桡之凶，不可以有辅也。
九四：栋隆之吉，不桡乎下也。
九五：枯杨生华，何可久也。老妇士夫，亦可丑也。
上六：过涉之凶，不可咎也。

【上经·第二十九卦 坎】
初六：习坎入坎，失道凶也。
九二：求小得，未出中也。
六三：来之坎坎，终无功也。
六四：樽酒簋贰，刚柔济也。
九五：坎不盈，中未大也。
上六：上六失道，凶三岁也。

【上经·第三十卦 离】
初九：履错之敬，以辟咎也。
六二：黄离元吉，得中道也。
九三：日昃之离，何可久也。
九四：突如其来如，无所容也。
六五：六五之吉，离王公也。
上九：王用出征，以正邦也。获匪其丑，大有功也。

【下经·第三十一卦 咸】
初六：咸其拇，志在外也。
六二：虽凶，居吉，顺不害也。
九三：咸其股，亦不处也。志在随人，所执下也。
九四：贞吉悔亡，未感害也。憧憧往来，未光大也。
九五：咸其脢，志末也。
上六：咸其辅颊舌，滕口说也。

【下经·第三十二卦 恒】
初六：浚恒之凶，始求深也。
九二：九二悔亡，能久中也。
九三：不恒其德，无所容也。
九四：久非其位，安得禽也。
六五：妇人贞吉，从一而终也。夫子制义，从妇凶也。
上六：振恒在上，大无功也。

【下经·第三十三卦 遁】
初六：遁尾之厉，不往何灾也。
六二：执用黄牛，固志也。
九三：系遁之厉，有疾惫也。畜臣妾吉，不可大事也。
九四：君子好遁，小人否也。
九五：嘉遁贞吉，以正志也。
上九：肥遁，无不利；无所疑也。

【下经·第三十四卦 大壮】
初九：壮于趾，其孚穷也。
九二：九二贞吉，以中也。
九三：小人用壮，君子罔也。
九四：藩决不羸，尚往也。
六五：丧羊于易，位不当也。
上六：不能退，不能遂，不详也。艰则吉，咎不长也。

【下经·第三十五卦 晋】
初六：晋如摧如，独行正也。裕无咎，未受命也。
六二：受兹介福，以中正也。
六三：众允之志，上行也。
九四：鼫鼠贞厉，位不当也。
六五：失得勿恤，往有庆也。
上九：维用伐邑，道未光也。

【下经·第三十六卦 明夷】
初九：君子于行，义不食也。
六二：六二之吉，顺以则也。
九三：南狩之志，乃大得也。
六四：入于左腹，获心意也。
六五：箕子之贞，明不可息也。
上六：初登于天，照四国也。后入于地，失则也。
【下经·第三十七卦 家人】
初九：闲有家，志未变也。
六二：六二之吉，顺以巽也。
九三：家人嗃嗃，未失也。妇子嘻嘻，失家节也。
六四：富家大吉，顺在位也。
九五：王假有家，交相爱也。
上九：威如之吉，反身之谓也。

【下经·第三十八卦 睽】
初九：见恶人，以辟咎也。
九二：遇主于巷，未失道也。
六三：见舆曳，位不当也。无初有终，遇刚也。
九四：交孚无咎，志行也。
六五：厥宗噬肤，往有庆也。
上九：遇雨之吉，群疑亡也。

【下经·第三十九卦 蹇】
初六：往蹇来誉，宜待也。
六二：王臣蹇蹇，终无尤也。
九三：往蹇来反，内喜之也。
六四：往蹇来连，当位实也。
九五：大蹇朋来，以中节也。
上六：往蹇来硕，志在内也。利见大人，以从贵也。

【下经·第四十卦 解】
初六：刚柔之际，义无咎也。
九二：九二贞吉，得中道也。
六三：负且乘，亦可丑也。自我致戎，又谁咎也。
九四：解而拇，未当位也。
六五：君子有解，小人退也。
上六：公用射隼，以解悖也。

【下经·第四十一卦 损】
初九：已事遄往，尚合志也。
九二：九二利贞，中以为志也。
六三：一人行，三则疑也。
六四：损其疾，亦可喜也。
六五：六五元吉，自上祐也。
上九：弗损益之，大得志也。

【下经·第四十二卦 益】
初九：元吉无咎，下不厚事也。
六二：或益之，自外来也。
六三：益用凶事，固有之也。
六四：告公从，以益志也。
九五：有孚惠心，勿问之矣。惠我德，大得志也。
上九：莫益之，偏辞也。或击之，自外来也。

【下经·第四十三卦 夬】
初九：不胜而往，咎也。
九二：莫夜有戎，得中道也。
九三：君子夬夬，终无咎也。
九四：其行次且，位不当也。闻言不信，聪不明也。
九五：中行无咎，中未光也。
上六：无号之凶，终不可长也。

【下经·第四十四卦 姤】
初六：系于金柅，柔道牵也。
九二：包有鱼，义不及宾也。
九三：其行次且，行未牵也。
九四：无鱼之凶，远民也。
九五：九五含章，中正也。有陨自天，志不舍命也。
上九：姤其角，上穷吝也。

【下经·第四十五卦 萃】
初六：乃乱乃萃，其志乱也。
六二：引吉无咎，中未变也。
六三：往无咎，上巽也。
九四：大吉无咎，位不当也。
九五：萃有位，志未光也。
上六：赍咨涕洟，未安上也。

【下经·第四十六卦 升】
初六：允升大吉，上合志也。
九二：九二之孚，有喜也。
九三：升虚邑，无所疑也。
六四：王用亨于岐山，顺事也。
六五：贞吉升阶，大得志也。
上六：冥升在上，消不富也。
【下经·第四十七卦 困】
初六：入于幽谷，幽不明也。
九二：困于酒食，中有庆也。
六三：据于蒺藜，乘刚也。入于其宫，不见其妻，不祥也。
九四：来徐徐，志在下也。虽不当位，有与也。
九五：劓刖，志未得也。乃徐有说，以中直也。利用祭祀，受福也。
上六：困于葛藟，未当也。动悔，有悔，吉行也。

【下经·第四十八卦 井】
初六：井泥不食，下也。旧井无禽，时舍也。
九二：井谷射鲋，无与也。
九三：井渫不食，为我心恻；可用汲，王明并受其福。
六四：井甃无咎，修井也。
九五：寒泉之食，中正也。
上六：元吉在上，大成也。

【下经·第四十九卦 革】
初九：巩用黄牛，不可以有为也。
六二：已日革之，行有嘉也。
九三：革言三就，又何之矣。
九四：改命之吉，信志也。
九五：大人虎变，其文炳也。
上六：君子豹变，其文蔚也。小人革面，顺以从君也。

【下经·第五十卦 鼎】
初六：鼎颠趾，未悖也。利出否，以从贵也。
九二：鼎有实，慎所之也。我仇有疾，终无尤也。
九三：鼎耳革，失其义也。
九四：覆公餗，信如何也。
六五：鼎黄耳，中以为实也。
上九：玉铉在上，刚柔节也。

【下经·第五十一卦 震】
初九：震来虩虩，恐致福也。笑言哑哑，后有则也。
六二：震来厉，乘刚也。
六三：震苏苏，位不当也。
九四：震遂泥，未光也。
六五：震往来厉，危行也。其事在中，大无丧也。
上六：震索索，中未得也。虽凶无咎，畏邻戒也。

【下经·第五十二卦 艮】
初六：艮其趾，未失正也。
六二：不拯其随，未退听也。
九三：艮其限，危薰心也。
六四：艮其身，止诸躬也。
六五：艮其辅，以中正也。
上九：敦艮之吉，以厚终也。

【下经·第五十三卦 渐】
初六：小子之厉，义无咎也。
六二：饮食衎衎，不素饱也。
九三：夫征不复，离群丑也。妇孕不育，失其道也。利用御寇，顺相保也。
六四：或得其桷，顺以巽也。
九五：终莫之胜吉，得所愿也。
上九：其羽可用为仪，吉；不可乱也。

【下经·第五十四卦 归妹】
初九：归妹以娣，以恒也。跛能履吉，相承也。
九二：利幽人之贞，未变常也。
六三：归妹以须，未当也。
九四：愆期之志，有待而行也。
六五：帝乙归妹，不如其娣之袂良也。其位在中，以贵行也。
上六：上六无实，承虚筐也。

【下经·第五十五卦 丰】
初九：虽旬无咎，过旬灾也。
六二：有孚发若，信以发志也。
九三：丰其沛，不可大事也。折其右肱，终不可用也。
九四：丰其蔀，位不当也。日中见斗，幽不明也。遇其夷主，吉；行也。
六五：六五之吉，有庆也。
上六：丰其屋，天际翔也。窥其户，阒其无人，自藏也。

【下经·第五十六卦 旅】
初六：旅琐琐，志穷灾也。
六二：得童仆贞，终无尤也。
九三：旅焚其次，亦以伤矣。以旅与下，其义丧也。
九四：旅于处，未得位也。得其资斧，心未快也。
六五：终以誉命，上逮也。
上九：以旅在上，其义焚也。丧牛于易，终莫之闻也。
【下经·第五十七卦 巽】
初六：进退，志疑也。利武人之贞，志治也。
九二：纷若之吉，得中也。
九三：频巽之吝，志穷也。
六四：田获三品，有功也。
九五：九五之吉，位正中也。
上九：巽在床下，上穷也。丧其资斧，正乎凶也。

【下经·第五十八卦 兑】
初九：和兑之吉，行未疑也。
九二：孚兑之吉，信志也。
六三：来兑之凶，位不当也。
九四：九四之喜，有庆也。
九五：孚于剥，位正当也。
上六：上六引兑，未光也。

【下经·第五十九卦 涣】
初六：初六之吉，顺也。
九二：涣奔其机，得愿也。
六三：涣其躬，志在外也。
六四：涣其群，元吉；光大也。
九五：王居无咎，正位也。
上九：涣其血，远害也。

【下经·第六十卦 节】
初九：不出户庭，知通塞也。
九二：不出门庭凶，失时极也。
六三：不节之嗟，又谁咎也。
六四：安节之亨，承上道也。
九五：甘节之吉，居位中也。
上六：苦节贞凶，其道穷也。

【下经·第六十一卦 中孚】
初九：初九虞吉，志未变也。
九二：其子和之，中心愿也。
六三：可鼓或罢，位不当也。
六四：马匹亡，绝类上也。
九五：有孚挛如，位正当也。
上九：翰音登于天，何可长也。

【下经·第六十二卦 小过】
初六：飞鸟以凶，不可如何也。
六二：不及其君，臣不可过也。
九三：从或戕之，凶如何也。
九四：弗过遇之，位不当也。往厉必戒，终不可长也。
六五：密云不雨，已上也。
上六：弗遇过之，已亢也。

【下经·第六十三卦 既济】
初九：曳其轮，义无咎也。
六二：七日得，以中道也。
九三：三年克之，惫也。
六四：终日戒，有所疑也。
九五：东邻杀牛，不如西邻之时也。实受其福，吉大来也。
上六：濡其首，何可久也。

【下经·第六十四卦 未济】
初六：濡其尾，亦不知极也。
九二：九二贞吉，中以行正也。
六三：未济征凶，位不当也。
九四：贞吉悔亡，志行也。
六五：君子之光，其晖吉也。
上九：饮酒濡首，亦不知节也。
""".trimIndent(), """译文说明：以下为本项目工作直译初稿，按卦、爻逐条对应。小象常以极短语句解释爻辞，部分术语仍需结合古注进一步校释。

【临】
初九：以感通之道临近他人，守正便吉祥；这是因为志向与行动都合乎正道。
九二：以感通之道临近他人，吉祥而无所不利；“未顺命”的具体所指尚有不同解释，暂保留字面。
六三：以甘言悦色的方式临近他人，是因为所处位置不恰当；若已经为此忧虑，过失便不会长久。
六四：亲自临近而没有灾咎，是因为所处位置恰当。
六五：这是大君应有的合宜之道，所谓合宜，就是行事居中。
上六：敦厚地临近他人而吉祥，是因为志意仍在内在根本。

【观】
初六：初六如同幼童般观看，这是小人的行事方式。
六二：窥视式的观看，虽合于女子守正之道，也仍可说是可羞之处。
六三：观察自身所行而决定进退，并没有失去正道。
六四：观察国家的光明德化，是崇尚礼遇贤宾。
九五：观察自身所行，也就是观察百姓的状况。
上九：观察其所表现的德行，心志仍未平定；“其生”的指代及“志未平”的义理仍待结合注疏细辨。

【噬嗑】
初九：脚上戴着刑具而限制其行动，是说不再继续向前作恶；此处仅作古代文本释义。
六二：咬合柔脆之物而触及鼻部，是因为乘临刚爻；“灭鼻”属于古代爻辞语境，不作现代情境延伸。
六三：遭遇毒害，是因为所处位置不恰当。
九四：艰难中守正而吉，是因为其德业尚未充分显明。
六五：守正而知危险，便没有灾咎，是因为处置得当。
上九：头戴刑具而遮蔽耳部，是因为听察不够明明；此句为古代刑罚譬喻，不作现实操作解释。

【贲】
初九：舍弃车乘而徒步，是因为按道义不应乘车。
六二：修饰胡须，是随着上方之爻一同兴起。
九三：长久守正的吉祥，最终没有人能够凌越它。
六四：六四处在当位而心有疑虑；并非寇盗而是婚姻相求，最终没有过失。
六五：六五之所以吉祥，是因为有喜庆之事。
上九：以质朴素白作为文饰而没有灾咎，是因为居上者实现了志愿。

【剥】
初六：剥蚀床脚，是因为损害从下方开始。
六二：剥蚀床架的边缘，是因为尚无同类相助；“辨”字所指的器物部位，仍须结合古注。
六三：剥落之时能够没有灾咎，是因为与上下各方失去联系。
六四：剥蚀已近于床面，是因为灾患已经迫近。
六五：以宫人身份得到宠遇，最终没有过失。
上九：君子得到车舆，是因为受到百姓承载拥戴；小人剥毁居所，最终不可任用。“舆”“庐”均保留原文譬喻，不延伸为现实政治判断。

【复】
初九：很快返回正道，是为了修养自身。
六二：返回正道而美善吉祥，是因为能够向下亲近仁者。
六三：屡次反复而返回正道，虽有危险，按义理仍可无咎。
六四：在群爻之中独自返回正道，是为了追随道义。
六五：敦厚地返回正道而没有悔恨，是因为内心能够自我考察、检验。
上六：迷失正道而不知返回，其凶险是因为背离了为君之道。

【无妄】
初九：不妄为而前往，能够实现志愿。
六二：不预先耕作却有所收获，不是为了求取财富。
六三：行路之人得到牛，邑中居民反而遭受灾祸。
九四：能够守正便没有灾咎，是因为守持其固有之德。
九五：无妄之疾，不可轻率试药；“不可试”是传文对爻辞的解释，具体训义仍需参照古注。
上九：不妄之道走到尽头仍贸然行动，便成为穷极之灾。

【大畜】
初九：虽有危险，适宜停止；这样便不会触犯灾患。
九二：车轴与车舆之间的连接部件脱落，内心便没有过尤；“輹”是车下承轴部件，数字文本另见“腹”等异文。
九三：利于有所前往，是因为与上方志向相合。
六四：六四大吉，是因为有可喜之事。
六五：六五之所以吉祥，是因为有值得庆贺的结果。
上九：通达如同天上的大道，说明道路得以广泛施行。

校勘提示：本轮以《周易/小象》分卦索引为总线索，并逐条参照《原本周易本义》卷五及可取得的逐卦页面。临卦首句，索引页作“威临贞吉”，而《原本周易本义》卷五作“咸临贞吉”；本条依逐卦经传语境及《本义》所见录为“咸”，保留索引页异文记录，不将“威”静默抹去。噬嗑九四，索引页作“得当也”，逐卦《周易/噬嗑》及《本义》作“未光也”，本条依逐卦文本录“未光也”。大畜六四，索引页作“童牛之牿，有喜也”，逐卦《周易/大畜》作“六四元吉，有喜也”，本条依逐卦页录入，并保留差异。大畜九二“輹”另见“腹”异文；小象原句按逐卦页作“舆说輹”。上述差异均为数字文本间的异文记录，尚不等同于完成版本源流校勘。译文为工作直译初稿。

【乾】
初九：潜藏的龙不宜施用，是因为阳刚之气处在下位。
九二：龙出现在田野，是因为德泽施布广泛。
九三：整日勤勉不懈，是因为反复践行正道。
九四：或跃起于深渊，前进便没有灾咎。
九五：飞龙在天，是大人有所作为。
上九：亢极之龙会有悔恨，是因为盈满不能长久。
用九：用九之道，体现天德不自居为首。

【坤】
初六：脚下踏到霜，说明阴气开始凝结；顺着这一趋势发展，便会到达坚冰的阶段。
六二：六二的行动正直而方正；不必刻意学习也无所不利，是因为地道光明广大。
六三：内含文采而可以守正，是因为能够等待时机而成就；有时参与君王事务，是因为智慧光明广大。
六四：收束袋口便没有灾咎，是因为谨慎就不会受到伤害。
六五：黄色下裳带来大吉，是因为文采蕴含在内。
上六：龙在郊野交战，是因为阴阳之道已经走到穷极。
用六：用六而长久守正，是以柔顺之道成就宏大的终局。

【屯】
初九：虽然徘徊不进，志向与行为仍然端正；以尊贵身份谦居卑下，便能大得民心。
六二：六二的艰难，是因为处在刚爻之上；十年才得以出嫁，是因为恢复了常态。
六三：追逐鹿却没有虞人引导，是因为只顾追逐猎物；君子应当舍弃它，若继续前往便会陷入困窘。
六四：有所求而前往，是明智的。
九五：屯积恩泽，是因为施予尚未光大。
上六：哭泣至泪血涟涟，这种处境怎能长久？

【蒙】
初六：适宜用刑罚惩戒人，是为了端正法度。
九二：儿子能够承担家事，是因为刚与柔相互接应。
六三：不宜娶这位女子，是因为行为不顺。
六四：困于蒙昧而有憾，是因为独自远离阳刚充实之爻。
六五：蒙童吉祥，是因为柔顺而谦逊。
上九：适宜抵御盗寇，是因为上下都能顺从。

【需】
初九：在郊外等待，是不冒险前行；适宜保持恒常，没有灾咎，是因为没有失去常道。
九二：在沙地等待，是因为心境宽缓而居中；即使略有言语责难，最终仍能吉祥。
九三：在泥地等待，灾患来自外部；是自己招来了盗寇，若能恭敬谨慎便不会失败。
六四：在血地中等待，应当顺从而听命。
九五：饮酒进食，守正则吉，是因为居中而得正。
上六：不速之客到来，恭敬对待他们，最终吉祥；虽然位置不当，却没有大的过失。

【讼】
初六：不长久纠缠所争之事，是因为争讼不可持续；即使略有言语争辩，曲直也能辨明。
九二：争讼不能取胜，便返回逃避；以下位争讼上位，祸患到来是自取的。
六三：享用旧有的德业，顺从上位者便吉祥。
九四：返回正命、改变做法，安于守正，便没有过失。
九五：争讼得大吉，是因为居中而正。
上九：因争讼而获得服饰封赏，也不值得尊敬。

【师】
初六：军队出动必须有纪律；失去纪律，就会凶险。
九二：在军队中担任统帅而吉祥，是承受了上天的恩宠；君王多次授予命令，是为了安抚万邦。
六三：军队中有人用车载运尸体，是因为没有取得功绩。
六四：军队驻扎在左方，没有灾咎，是因为没有失去常规。
六五：长子统率军队，是因为用兵合乎中道；次子却用车载尸，是因为任用不当。
上六：君王颁布命令，是为了端正功绩；小人不可任用，否则必定扰乱邦国。

【比】
初六：亲比之始便有其他助益，是吉祥的。
六二：从内部亲近他人，是因为没有失去自身正道。
六三：亲近不合适的人，难道不会受到伤害吗？
六四：从外部亲近贤者，是为了追随上位者。
九五：光明正大的亲比之所以吉祥，是因为位置居中而正；舍弃逆来者、选择顺从者，如同失去被驱赶在前的禽兽；邑中百姓不必戒备，是因为上位者行事合乎中道。
上六：亲比却没有领袖，是因为没有可以善终的结果。

【小畜】
初九：返回自己的正道，按义理说是吉祥的。
九二：被牵引而返回，是因为处在中位，也没有失去自身正道。
九三：夫妻反目，是因为不能端正家室。
六四：心存诚信，忧惧得以消除，是因为与上位者志向相合。
九五：心怀诚信而相互牵系，是因为不独自富有。
上九：雨已经降下，也已安处，是德行积聚充实的结果；君子出行会有凶险，是因为心中有所疑虑。

【履】
初九：以质朴的方式践行礼道而前往，是独自实现自己的心愿。
九二：幽居之人守正而吉，是因为内心不使自己紊乱。
六三：眼睛昏弱却想看见，视力不足以明察；跛足却想行走，能力不足以同行。咬人的凶险，是因为所处爻位不恰当。武人要成为君主，是因为志气刚强。
九四：心怀戒惧，最终吉祥，是因为志向得以施行。
九五：果决地践行礼道，守正而有危险，是因为所处位置正当。
上九：大吉在于居上位，是因为有很大的喜庆。

【泰】
初九：拔起茅草而前进，吉祥，是因为志向在于向外发展。
九二：包容荒秽，能够配合中道而行，是因为德量光明广大。
九三：没有只往不返的事物，这是天地交接、循环往复的道理。
六四：轻快相连而不自以为富足，是因为彼此都不以实有自居；不加戒备而以诚信相待，是出于内心的愿望。
六五：凭借福祉而得大吉，是因为居中而实行自己的愿望。
上六：城墙倒塌回落到护城壕中，是因为政令与秩序已经混乱。

【否】
初六：拔起茅草，守正则吉，是因为志向在于君主。
六二：大人处于闭塞之时仍能亨通，是因为不扰乱群体。
六三：包藏羞辱，是因为所处爻位不恰当。
九四：有天命而无咎，是因为志向得以施行。
九五：大人吉祥，是因为所处位置正当。
上九：闭塞到了终点就会倾覆，怎么可能长久？

【同人】
初九：走出门户与人会同，又有谁能责怪呢？
六二：只在宗族内部会同，是一种狭隘不通的道路。
九三：把兵戎埋伏在草莽中，是因为对手强硬；三年不能兴兵，是因为不能贸然行动。
九四：登上城墙却不能攻克，是因为道义上不能这样做；后来能够吉祥，是因为困顿之后返回正道。
九五：会同之初能够坚持，是因为居中而正直；大军相遇，说的是双方相互克制或交战。“相克”一语仍需结合注疏辨析。
上九：在郊外与人会同，是因为志愿尚未实现。
校勘提示：本轮索引底本此处作“乘其庸”；通行本常见“乘其墉”。暂保留所见“庸”，不擅自改字，异文待逐卦底本复核。

【大有】
初九：大有之初，没有交相为害的情形。
九二：大车能够装载，是因为内部积蓄充实而不会倾败。
九三：公侯以此礼仪享献于天子，小人却会因此受害；“亨”在此处的训读仍需结合古注核验。
九四：不显露盛大骄盈，便没有灾咎；这是因为能够明察辨别清楚。
六五：诚信相交，是因为诚信能够启发彼此的志意；保持威严而吉祥，是因为平易而不刻意设防。
上九：大有之上爻吉祥，是因为得到上天保佑。
校勘提示：本段依《周易/大有》象曰部分录入；其经文爻辞另见“大轝一作車”“匪其尫一作彭”“佑一作右”等异文，不能据经文异文反向改写小象。此处保留传文所见“彭”“祐”，后续仍需与其他版本逐字复核。

【谦】
初六：谦而又谦的君子，是以谦卑来修养自身。
六二：谦德有所显扬而守正吉祥，是因为内心有所获得。
九三：勤劳而谦逊的君子，能够使万民敬服。
六四：没有什么不利，施行并发挥谦德，是因为没有违背法则。“㧑”字的具体训义及异体写法仍待进一步校勘。
六五：适宜进行征伐，是为了征讨不肯服从者；此句只译传文本义，不将其扩展为现实行动建议。
上六：谦德虽有声名，志愿仍未实现；可以兴师，征讨邑国。此处“可用”是传文对爻辞的解释，具体义理仍需结合注疏。
校勘提示：本段据《周易正义》卷二《谦》所录小象逐句录入。该本六四作“㧑谦”，部分数字文本作“撝谦”；暂保留《周易正义》页面字形，不擅自统一。译文为工作直译初稿。

【豫】
初六：初六鸣扬逸乐，心志已经走到尽头，因此凶险。“鸣豫”的具体语义仍需结合古注校释。
六二：不等到一日终了便能及时察觉，守正而吉祥；这是因为居中而得正。
六三：仰望他人以求安乐，会有悔恨；这是因为所处位置不恰当。“盱”字的训义仍有解释分歧。
九四：众人由此获得安乐，因而大有所得；这是因为其志向得以广泛施行。“由豫”的主语与语义仍待结合注疏细辨。
六五：六五处于守正而有疾的状态，是因为乘临刚爻；长久未死，是因为中位之德尚未丧失。“贞疾”不宜脱离古代语境作现代医学解释。
上六：沉溺于安乐而居于最上位，怎么可能长久呢？
校勘提示：本段依维基文库《周易/豫》固定页面所录六条小象录入，页面标示底本版本时间为 2026-09-27，固定旧版本号 10645735。九四爻辞中的“朋盍戠一作簪”属于经文异文，不属于本段小象，故不移入小象正文。译文为工作直译初稿，疑难词义保留待考。
【随】
初九：所执掌的原则有所变通，但能依从正道，所以吉祥；出门与人交往而有所得，是因为没有偏失正道。“官”在此可理解为主守、所执掌之事，具体训义仍可结合注疏细辨。
六二：牵系于年少者，就不能同时兼顾另一方。“小子”“丈夫”所指对象，古注有具体爻位解释，译文暂不把它泛化为现代身份称谓。
六三：牵系于年长刚健者，是因为志向舍弃了下方。“系丈夫”与“志舍下”的关系仍需结合王弼注、孔颖达疏进一步辨析。
九四：随从而有所获得，从义理上说却有凶险；若诚信行于正道，功效便能彰明。“明功”一语暂按功效彰明理解，仍待校释。
九五：诚信寄托于美善之事，因而吉祥；这是因为居于正中之位。
上六：被拘系而继续维系，是因为随从之道已至穷极。“拘系之”中代词所指及“上穷”的具体义理仍待结合注疏细辨。
校勘提示：本段小象据维基文库《周易/随》页面逐条录入，并参照《周易正义/02随》核对。不同数字文本在“向／乡／嚮”等字形及通假字处理上存在差异；此处以简体索引页面所见传文为录入底稿，不将《周易正义》的注文混入小象正文。译文为工作直译初稿。
【蛊】
初六：整治父辈遗留下来的弊事，心意在于承继父亲的志意。“意承考”不是机械照搬先人的做法；具体“考”字在此处的训释仍宜参照古注。
九二：整治母辈遗留下来的弊事，能够合乎中正之道。“不可贞”的“贞”有守固、专执等解释，译文不将其简单理解为现代意义的贞操。
九三：整治父辈遗留下来的弊事，最终不会有灾咎。
六四：宽缓地处理父辈遗留下来的弊事，前往则尚未得到所求。“裕”有宽缓、宽纵等训释，具体褒贬须结合爻辞与注疏判断。
六五：整治父辈遗留下来的弊事而获得美誉，是凭借德行承继先人。
上九：不侍奉王侯，志向足以成为法则。“可则”意为可供取法，不宜直接等同于现代个人主义式的隐居。
校勘提示：本段据维基文库《周易/蛊》逐条录入，并参照《周易正义/03蛊》及《周易注》核对。初六爻辞的断句、九二“干／乾”字形、六四“吝／咎”等版本差异属于经文层面，不反向改写小象；此处保留小象传文“意承考”“往未得”等原句。译文为工作直译初稿，疑难训义仍待进一步校勘。

【颐】
初九：看着我大嚼、贪求食物的样子，也就不值得尊重。
六二：六二前往而凶，是因为行动失去了同类。
六三：十年之内不可施用，是因为所行之道大为悖逆。
六四：颠倒求养而得吉，是因为上位施予的恩泽光明广泛。
六五：安居守正而吉，是因为顺从并跟随上位者。
上九：由此得到养育，虽有危险却吉祥，是因为有大的庆贺。

【大过】
初六：把物品垫在白茅之上，是因为柔顺处在下位。
九二：老年男子与年轻女子结合，是一种超越常度的相互结合；此句保留古代婚配语境，不作现代关系判断。
九三：栋梁弯曲而凶险，是因为没有可以辅助支撑它的力量。
九四：栋梁隆起而吉祥，是因为下方不再弯曲。
九五：枯杨开花，怎么可能长久？老妇与年轻男子结合，也可说是不相称。
上六：涉水过深而遭凶险，是因为不应归咎于他人；“不可咎”的具体句义仍有不同解释。

【坎】
初六：习于险难而进入险陷，是因为失去正道，所以凶险。
九二：只能得到小的收获，是因为尚未走出险境之中。
六三：来去都处在重重险难之中，最终没有功效。
六四：酒器一樽、食器两簋，是因为刚柔能够相济。
九五：险水尚未盈满，是因为其中的力量还没有壮大。
上六：上六失去正道，凶险将持续三年。
校勘提示：维基文库《周易/坎》固定页面（页面所示旧版本号 10646381）作“樽酒簋贰，刚柔济也”；《周易/小象》聚合索引的部分数字文本作“刚柔际也”。此处依逐卦页面录“济”，并保留“际”作为索引异文。该差异目前只能确认是数字文本间的不同，尚未据古本完成源流判定。

【离】
初九：脚步虽有错杂，仍能保持恭敬，是为了避开灾咎。
六二：黄色的光明带来大吉，是因为得到了中正之道。
九三：太阳偏西时的光明，怎么可能长久？
九四：突然来到的事物，已经没有可以容纳它的地方。
六五：六五之所以吉祥，是因为亲近并依附王公。
上九：王者出征，是为了端正邦国；所获并非其同类众多，是因为建立了大功。此处仅译古代传文，不延伸为现实行动建议。

【咸】
初六：感应到脚趾，是因为志向在外。
六二：虽然有凶险，安居则吉，是因为顺理而不受伤害。
九三：感应到大腿，也不能安处；志向在于随从他人，所执持的立场较低。
九四：守正则吉而悔恨消失，是因为尚未受到感应之害；心意往来不定，是因为感应还不够光明广大。
九五：感应到脊背肉，是因为志向停留在末端。
上六：感应到面颊、口舌，是因为只在口头上滔滔说话。“滕”字照录页面传文，字形与训义仍待校勘。

【恒】
初六：一开始就求得过深，因此造成恒久之凶。
九二：九二的悔恨消失，是因为能够长久保持中道。
九三：不能恒常保持德行，是因为没有容身之处。
九四：长久处在不合适的位置，怎么能获得猎物？
六五：妇人守正而吉，是因为从一而终；男子应当依义理作出裁断，若只追随妇人便凶。
上六：在最上位仍不断振动变易，是因为终究没有大的功效。

【遁】
初六：退避落在最后而有危险；若不前往，又有什么灾祸？
六二：执持黄牛皮革，是为了坚定志向。
九三：牵系于退避之事而有危险，是因为忧困疲惫；蓄养臣妾可以，不可用来承担大事。
九四：君子乐于退避，小人却不能如此。
九五：美善地退避而守正吉祥，是因为端正了自己的志向。
上九：宽裕自在地退避，没有什么不利，是因为心中没有疑虑。

【大壮】
初九：强壮之气用在脚趾，是因为其诚信终将走到穷尽。
九二：九二守正而吉，是因为居于中位。
九三：小人使用强力，君子则不这样做。
九四：藩篱已经决开而未受困，是因为仍可向前。
六五：在易地失去羊，是因为所处位置不恰当。
上六：不能退后，也不能前进，是因为判断不够周详；艰难中守持正道便吉，灾咎不会长久。

【晋】
初六：前进又受阻，仍独自奉行正道；宽裕处之便没有灾咎，是因为尚未接受任命。
六二：接受这份大福，是因为居中而正。
六三：众人信任的志向，是向上进取。
九四：像大鼠一样贪据不当之位而有危险，是因为所处位置不恰当。
六五：得失都不必忧虑，继续前往便有喜庆。
上九：只用于整治自己的封邑，其道还没有充分光大。

【明夷】
初九：君子离去，是因为按义理不应接受这份食物。
六二：六二之所以吉祥，是因为顺从法则。
九三：向南行猎的志向，最终获得很大的成果。
六四：进入左腹而了解明夷之心，是因为获得了其内在心意。
六五：箕子能够守正，是因为光明不可以熄灭。
上六：起初升上天空，光照四方；后来落入地下，是因为失去了法则。

校勘提示：本轮逐卦参照维基文库《周易/颐》《大过》《坎》《离》《咸》《恒》《遁》《大壮》《晋》《明夷》，并以《周易正义》相关卦疏辅助辨读。坎卦九四“济／际”已单列异文；大壮九四经文中“大舆／大轝”“腹／輹”等字形差异属于经文层面，不据此改写小象。晋卦九四逐卦页作“鼫鼠”，另有“硕鼠”字形；此处保留“鼫鼠”。明夷九三“南狩”及离卦上九“出征”均只按历史传文作字面释义，不作现实行动延伸。译文均为工作直译初稿，疑难训义仍待进一步校勘。

【家人】
初九：预先防范、整饬家事，是因为志向尚未改变。
六二：六二之所以吉祥，是因为柔顺而谦逊。
九三：家中管束严格，尚未失当；若妇人与孩子嬉闹无度，便失去了家庭的节度。
六四：使家业富足而大吉，是因为顺应并处在合宜的位置。
九五：君王使天下成为一家，是因为彼此交相亲爱。
上九：有诚信而威仪庄重，最终吉祥；所谓威仪，是说要反求自身。

【睽】
初九：见到不合意的人，是为了避开灾咎。
九二：在小巷中遇到主人，没有失去正道。
六三：看见车子被拖曳，是因为所处位置不恰当；起初虽不顺，最终有好的结果，是因为遇到了刚健者。
九四：彼此以诚信相交而没有灾咎，是因为志向得以施行。
六五：同宗之人前来相助，如同咬到柔脆之物；前往会有喜庆。
上九：遇雨而吉，是因为众多疑虑消散。

【蹇】
初六：前往会遇到艰难，返回则得到赞誉，因此适宜等待。
六二：王臣虽处处艰难，最终没有过失。
九三：前往有艰难，返回则得到反顾，是因为内心对此感到欣喜。
六四：前往有艰难，返回则有所联系，是因为居位恰当而有实质。
九五：大难之时朋伴前来，是因为能够合乎中正的节度。
上六：前往有艰难，返回则有较大收获，志向在于内部；利于见到大人，是因为能够追随尊贵正当者。

【解】
初六：刚柔交接之时，按义理可以没有灾咎。
九二：九二守正而吉，是因为得到了中正之道。
六三：背负东西又乘坐车舆，是可羞之事；自己招致争端，又能归咎于谁呢？此处按古代传文作义理解说，不延伸为现实冲突建议。
九四：解除脚拇指的牵系，是因为所处位置尚未恰当。
六五：君子能够解除困结，小人便退去了。
上六：公侯射获隼鸟，是为了解除悖逆之事；本句只作古文譬喻释读。

【损】
初九：事情办妥便迅速前往，是因为仍与上方志向相合。
九二：九二适宜守正，是因为把中正作为志向。
六三：一人独行；若变成三人同行，彼此便会产生疑虑。
六四：减除自身的弊病，也可以带来喜悦。
六五：六五大吉，是因为得到上位者的佑助。
上九：不减损反而有所增益，是因为充分实现了志愿。

【益】
初九：大吉而没有灾咎，是因为不把繁重事务强加于下。
六二：有人给予增益，是从外部而来的。
六三：在艰难事务中得到增益，是原本就具有的条件。
六四：禀告公侯而得到允从，是为了增益其志向。
九五：诚信而怀有惠爱之心，不必再加询问；施惠于我之德，便能充分实现志愿。
上九：没有人来增益，是偏颇之辞；有人来攻击，也是从外部而来。

【夬】
初九：不能胜任却贸然前往，便会招致过失。
九二：夜间出现事端而不必忧惧，是因为能够守得中道。此处只解释古代传文中的语义。
九三：君子果决而坚定，最终没有灾咎。
九四：行动迟疑，是因为所处位置不恰当；听到劝告却不相信，是因为听察不够明智。
九五：行于中道而没有灾咎，但中正之德尚未充分显明。
上六：没有号令所导致的凶险，终究不能长久。

【姤】
初六：系在金属制的止车器上，是因为柔弱之道受到牵制。
九二：包裹中有鱼，按义理不应把它送给宾客。
九三：行动迟疑，是因为前行尚未受到牵引。
九四：没有鱼而招致凶险，是因为疏远了民众。
九五：九五含藏文采，是因为居中而正；有物从天而降，是因为志向不舍弃天命。
上九：相遇到了角上，是因为居上者已到穷极，所以有吝。

【萃】
初六：先是纷乱而后聚集，是因为志向混乱。
六二：受到引导而吉祥、没有灾咎，是因为居中之志没有改变。
六三：前往便没有灾咎，是因为上方能够谦顺接纳。
九四：大吉而没有灾咎，是因为所处位置并不恰当。
九五：虽有聚集之位，志向却尚未充分显明。
上六：忧叹哭泣，是因为尚未安居于上位。

【升】
初六：诚信上升而大吉，是因为与上方志向相合。
九二：九二的诚信带来喜庆。
九三：升入空虚的城邑，没有什么可疑虑的。
六四：王者在岐山举行祭享，是顺应事理而行。
六五：守正吉祥，逐级上升，是因为充分实现了志愿。
上六：昏昧地一味上升而居于高位，是因为消耗而不能富足。

校勘提示：本轮以《周易/小象》聚合页作检索线索，逐条以《周易/家人》《睽》《蹇》《解》《损》《益》《夬》《姤》《萃》《升》分卦页面核对。聚合页含有明显 OCR/转录错误：家人条目将“嗃嗃”识作“嗝嗝”，睽六三将“见舆曳”识作“见奥曳”，损上九、益九五将“大得志”误识作“打得志”，姤初六将“金柅”误识作“金坭”；本次均以逐卦页面所见文字为录入底稿。夬九二依分卦页作“莫夜有戎，得中道也”，不把爻辞“惕号，莫夜有戎，勿恤”直接改写成传文。以上仍是数字文本之间的核对，尚非对出土本、古刻本及各家校勘记的全面版本学定论。译文为工作直译初稿，疑难训义仍待进一步校勘。

【困】
初六：进入幽暗的山谷，是因为幽暗而不明。
九二：困于酒食之中，是因为内里仍有喜庆。
六三：倚靠在蒺藜之上，是因为乘凌刚强者；进入自己的屋中却看不见妻子，是不祥之兆。
九四：来得迟缓，是因为志向在下方；虽然位置不当，却有同伴相助。
九五：遭受割鼻、断足之困，是因为志愿尚未实现；后来渐渐得到解脱，是因为内心中正刚直；适宜举行祭祀，是因为能够接受福佑。
上六：困在葛藤之中，是因为所处不当；行动会引起悔恨，但若能改悔而前行，仍可获吉。

【井】
初六：井中淤泥不能饮用，是因为处在下位；旧井没有禽兽来饮，是因为时势已将它舍弃。
九二：井水流入谷中，只能射取小鱼，是因为没有人来相助。
九三：井水已经淘清却无人饮用，因此心中悲伤；若有人来汲取，贤明之王便能使双方都得到福佑。此条在分卦页以较完整的传文句式呈现，保留原有文字，不擅自拆成额外爻象。
六四：用砖石修砌井壁而没有灾咎，是因为正在修治井体。
九五：寒泉可以饮用，是因为居中而正。
上六：居于上位而大吉，是因为井道大功告成。

【革】
初九：用黄牛皮革加以束固，是因为此时不可以有所作为。
六二：到了适宜的时日再行变革，行动会有美好的结果。
九三：变革的言论经过多次审议，已经没有别的去处。
九四：改变旧命而吉祥，是因为志向诚信。
九五：大人如虎纹般焕然变化，是因为文采鲜明。
上六：君子如豹纹般变化，是因为文采盛美；小人改变外在面貌，是因为顺从而追随君上。

【鼎】
初六：鼎足颠倒，并非悖逆之事；倒出旧物，是为了取用尊贵之物。
九二：鼎中有实物，是因为谨慎选择所往之处；旧有的匹偶或对手有病患，最终不会造成过失。
九三：鼎耳发生改变，是因为失去了应有的功用。
九四：倾覆了公侯的鼎中食物，诚信究竟在哪里呢？
六五：鼎的黄色耳饰，是因为把中正作为实质。
上九：玉制鼎铉处在上位，是因为刚柔调节得宜。

【震】
初九：震动来临时戒惧不安，是因为恐惧能够招致福分；后来能够谈笑安定，是因为之后有所法则。
六二：震动来临而有危险，是因为乘凌刚强者。
六三：震动使人不安，是因为所处位置不恰当。
九四：震动陷入泥中，是因为光明未能显现。
六五：往来都处在震动危险之中，是因为行动有危险；事情的核心仍在中位，因此大的方面没有丧失。
上六：震动使人惊惧战栗，是因为尚未得其中道；虽有凶险而无咎，是因为敬畏邻近的警戒。

【艮】
初六：止住脚趾，是因为没有失去正道。
六二：不能救助其所跟随者，是因为尚未退下听从。
九三：止住腰部，是因为危险忧惧会熏灼内心。
六四：止住自身，是因为把止息落实于自己。
六五：止住口颊，是因为能够保持中正。
上九：敦厚地止守而吉，是因为以敦厚善终。

【渐】
初六：年幼者处境危险，是因为按义理而言尚无灾咎。
六二：饮食安乐，是因为并非徒然饱食而无所作为。
九三：男子出征而不返回，是因为离开了同类群体；女子怀孕却未能生育，是因为失去了正道；适宜防御外来侵扰，是因为顺理而彼此保全。
六四：有时能够得到平直的椽木，是因为顺从而谦逊。
九五：最终没有人能够战胜它，结果吉祥，是因为实现了所愿。
上九：羽毛可以用作仪饰而吉祥，是因为不可使其紊乱。

【归妹】
初九：女子作为媵妾出嫁，是为了维持常道；跛脚仍能行走而吉，是因为彼此承接扶助。
九二：幽居之人适宜守正，是因为没有改变常道。
六三：女子出嫁而等待成为妾媵，是因为所处不当。
九四：婚期延迟的心意，是因为有所等待然后才行动。
六五：帝乙嫁女，主婚者衣袖不如媵妾华美；其位置居中，是因为以尊贵身份行事。
上六：上六没有实际收获，是因为所承接的是空筐。

【丰】
初九：即使经过十日也没有灾咎，超过十日便会有灾。
六二：有诚信而能发扬，是因为诚信使志意得以显发。
九三：丰盛的遮蔽物，不可用来承担大事；折断了右臂，最终不能再发挥作用。
九四：遮蔽丰盛，是因为位置不恰当；正午看见北斗，是因为幽暗而不明；遇到与自己相应的主人，吉祥，是因为可以前行。
六五：六五之所以吉祥，是因为有喜庆。
上六：房屋高大，是因为飞翔到了天际；窥视门户却空无一人，是因为自己把自己隐藏起来。

【旅】
初六：旅居之事琐碎卑微，是因为志向困穷而招致灾祸。
六二：得到忠贞的童仆，最终没有过失。
九三：旅居时住所被焚毁，也因此受到伤害；以旅居者身份对待下属，其道义便会丧失。
九四：旅居在某处，是因为尚未得到合宜的位置；得到资财与斧具，内心仍不满意。
六五：最终获得声誉与任命，是因为恩泽上达。
上九：以旅居者身份处在上位，其道理如同焚毁；在交易之地失去牛，最终没有人听闻此事。

校勘提示：本轮以维基文库《周易》分卦页核对困、革、鼎、震、艮、归妹、丰、旅，并结合《周易/小象》聚合页作交叉检索。井卦九三分卦页所见传文为“井渫不食，为我心恻；可用汲，王明并受其福”，与聚合索引把相关文字拆分、标点的呈现方式不同；本项目按分卦页保留，不擅自改成仅有“行恻也，求王明受福也”的短式。渐卦六二分卦页作“饮食衎衎”，聚合索引 OCR 作“饮食行行”，本轮依分卦页录“衎衎”。困卦“蒺藜／蒺蔾”、鼎卦“餗”字形及各卦爻辞异文属于经文或版本层面，不据此改写小象。译文均为工作直译初稿，个别句子（如井九三、困九五、渐初六）仍需进一步对照古注和不同传本。

【巽】
初六：进退犹豫，是因为志向疑惑；利于刚毅之人守正，是因为志向得以整治。
九二：纷然多端而吉祥，是因为得到了中正。
九三：屡屡谦顺而有吝，是因为志向困穷。
六四：田猎有所收获，是因为建立了功绩。此处仅作古代文本语义说明。
九五：九五之所以吉祥，是因为居位正当而得中。
上九：谦顺到了床下，是因为居上已到穷极；失去资财与凭借之物，正是凶险之象。“资斧”具体所指仍需结合古注考释。

【兑】
初九：和悦而吉祥，是因为行动没有疑虑。
九二：诚信而和悦之所以吉祥，是因为志意诚信。
六三：主动求悦而有凶险，是因为所处位置不恰当。
九四：九四之所以有喜，是因为有喜庆。
九五：诚信面对剥落之势，是因为所处位置正当。
上六：上六一味引导他人求悦，是因为其德行尚未光明。

【涣】
初六之所以吉祥，是因为柔顺。
九二：涣散之时奔向可依凭之处，是因为实现了愿望。“机”在此处的具体训义仍有解释空间。
六三：涣散自身，是因为志向在外。
六四：涣散其群而大吉，是因为德业光明广大。
九五：王者居于正位而没有灾咎，是因为位置端正。
上九：涣散其血患，是因为远离了伤害。“血”在此处按古代象辞语境理解，不作现代医学概念。

【节】
初九：不走出内门庭，是因为知道道路通达与闭塞的时机。
九二：不走出外门庭而有凶险，是因为错失时机到了极点。
六三：不知节制而发出叹息，又能归咎于谁呢？
六四：安于节制而亨通，是因为承接上位者的正道。
九五：甘于节制而吉祥，是因为居位得中。
上六：过分苦刻的节制守正反而凶险，是因为其道已经走到穷尽。

【中孚】
初九：有所预备而吉祥，是因为志向尚未改变；“虞”字在此有戒备、谋度等训释，译文暂取“有所预备”。
九二：它的同类回应鸣声，是因为内心深处彼此相应。
六三：可以击鼓，也可以停止，是因为所处位置不恰当。分卦页作“可鼓或罢”，聚合索引作“或鼓或罢”，本项目暂从分卦页，并保留这一文字差异。
六四：失去匹偶，是因为断绝同类而向上相从。
九五：诚信相连而不分离，是因为所处位置正当。
上九：鸡鸣般的声音升到天上，怎么可能长久呢？

【小过】
初六：飞鸟带来凶险，是因为事情已无可挽回。
六二：没有赶上君主，是因为臣下不可以超过君主。
九三：跟从他人可能遭受伤害，凶险又会如何呢？此句保留古文原义，不延伸为现实行为建议。
九四：没有越过常度而遇合，是因为所处位置不恰当；前往有危险，必须警戒，终究不能长久。
六五：浓云密布却不下雨，是因为已经升到了高处。
上六：没有遇合却一味超过，是因为已经到了亢极。

【既济】
初九：拖住车轮，是按义理可以没有灾咎。
六二：七日之后得到，是因为采取中正之道。
九三：经过三年才得以克服，是因为已经疲惫。
六四：整日戒备，是因为心中仍有所疑虑。
九五：东邻举行盛大的祭祀，不如西邻把握祭祀时机；真正接受福佑，是因为大的吉祥将要到来。原文“杀牛”是古代祭祀语境中的用语。
上六：水浸到了头部，怎么可能长久呢？

【未济】
初六：尾部被水沾湿，是因为也不知道事情的极限。
九二：九二守正而吉，是因为能够依中道而行正。
六三：未能渡过而贸然前往有凶险，是因为所处位置不恰当。
九四：守正则吉、悔恨消失，是因为志向得以施行。
六五：君子的光明，是因为其光辉带来吉祥。
上九：饮酒而浸湿头部，是因为同样不懂得节制。

校勘提示：本轮逐条核对维基文库《周易/小象》聚合页及《周易/巽》《周易/兑》《周易/中孚》《周易/小过》等分卦页。巽九五分卦页作“位正中也”，聚合索引作“位中正也”，本项目暂从分卦页；中孚六三分卦页作“可鼓或罢”，聚合索引作“或鼓或罢”，暂从分卦页。聚合页既济初九标点及字词有格式噪声，本项目按可读传文“曳其轮，义无咎也”录入。涣、节、既济、未济本轮以聚合页逐条对照，并结合《周易集解》等传注材料辨析语义；这仍不是对所有古刻本、出土本的全面校勘。译文为工作直译初稿，非权威定本。
""", chatGptTranslation = """ChatGPT 翻译整理稿（沿用本条既有逐段译文结构；本轮先完成译文并列归档，尚需逐句对照原文复核，不作为独立校勘定本）：

译文说明：以下为本项目工作直译初稿，按卦、爻逐条对应。小象常以极短语句解释爻辞，部分术语仍需结合古注进一步校释。

【临】
初九：以感通之道临近他人，守正便吉祥；这是因为志向与行动都合乎正道。
九二：以感通之道临近他人，吉祥而无所不利；“未顺命”的具体所指尚有不同解释，暂保留字面。
六三：以甘言悦色的方式临近他人，是因为所处位置不恰当；若已经为此忧虑，过失便不会长久。
六四：亲自临近而没有灾咎，是因为所处位置恰当。
六五：这是大君应有的合宜之道，所谓合宜，就是行事居中。
上六：敦厚地临近他人而吉祥，是因为志意仍在内在根本。

【观】
初六：初六如同幼童般观看，这是小人的行事方式。
六二：窥视式的观看，虽合于女子守正之道，也仍可说是可羞之处。
六三：观察自身所行而决定进退，并没有失去正道。
六四：观察国家的光明德化，是崇尚礼遇贤宾。
九五：观察自身所行，也就是观察百姓的状况。
上九：观察其所表现的德行，心志仍未平定；“其生”的指代及“志未平”的义理仍待结合注疏细辨。

【噬嗑】
初九：脚上戴着刑具而限制其行动，是说不再继续向前作恶；此处仅作古代文本释义。
六二：咬合柔脆之物而触及鼻部，是因为乘临刚爻；“灭鼻”属于古代爻辞语境，不作现代情境延伸。
六三：遭遇毒害，是因为所处位置不恰当。
九四：艰难中守正而吉，是因为其德业尚未充分显明。
六五：守正而知危险，便没有灾咎，是因为处置得当。
上九：头戴刑具而遮蔽耳部，是因为听察不够明明；此句为古代刑罚譬喻，不作现实操作解释。

【贲】
初九：舍弃车乘而徒步，是因为按道义不应乘车。
六二：修饰胡须，是随着上方之爻一同兴起。
九三：长久守正的吉祥，最终没有人能够凌越它。
六四：六四处在当位而心有疑虑；并非寇盗而是婚姻相求，最终没有过失。
六五：六五之所以吉祥，是因为有喜庆之事。
上九：以质朴素白作为文饰而没有灾咎，是因为居上者实现了志愿。

【剥】
初六：剥蚀床脚，是因为损害从下方开始。
六二：剥蚀床架的边缘，是因为尚无同类相助；“辨”字所指的器物部位，仍须结合古注。
六三：剥落之时能够没有灾咎，是因为与上下各方失去联系。
六四：剥蚀已近于床面，是因为灾患已经迫近。
六五：以宫人身份得到宠遇，最终没有过失。
上九：君子得到车舆，是因为受到百姓承载拥戴；小人剥毁居所，最终不可任用。“舆”“庐”均保留原文譬喻，不延伸为现实政治判断。

【复】
初九：很快返回正道，是为了修养自身。
六二：返回正道而美善吉祥，是因为能够向下亲近仁者。
六三：屡次反复而返回正道，虽有危险，按义理仍可无咎。
六四：在群爻之中独自返回正道，是为了追随道义。
六五：敦厚地返回正道而没有悔恨，是因为内心能够自我考察、检验。
上六：迷失正道而不知返回，其凶险是因为背离了为君之道。

【无妄】
初九：不妄为而前往，能够实现志愿。
六二：不预先耕作却有所收获，不是为了求取财富。
六三：行路之人得到牛，邑中居民反而遭受灾祸。
九四：能够守正便没有灾咎，是因为守持其固有之德。
九五：无妄之疾，不可轻率试药；“不可试”是传文对爻辞的解释，具体训义仍需参照古注。
上九：不妄之道走到尽头仍贸然行动，便成为穷极之灾。

【大畜】
初九：虽有危险，适宜停止；这样便不会触犯灾患。
九二：车轴与车舆之间的连接部件脱落，内心便没有过尤；“輹”是车下承轴部件，数字文本另见“腹”等异文。
九三：利于有所前往，是因为与上方志向相合。
六四：六四大吉，是因为有可喜之事。
六五：六五之所以吉祥，是因为有值得庆贺的结果。
上九：通达如同天上的大道，说明道路得以广泛施行。

校勘提示：本轮以《周易/小象》分卦索引为总线索，并逐条参照《原本周易本义》卷五及可取得的逐卦页面。临卦首句，索引页作“威临贞吉”，而《原本周易本义》卷五作“咸临贞吉”；本条依逐卦经传语境及《本义》所见录为“咸”，保留索引页异文记录，不将“威”静默抹去。噬嗑九四，索引页作“得当也”，逐卦《周易/噬嗑》及《本义》作“未光也”，本条依逐卦文本录“未光也”。大畜六四，索引页作“童牛之牿，有喜也”，逐卦《周易/大畜》作“六四元吉，有喜也”，本条依逐卦页录入，并保留差异。大畜九二“輹”另见“腹”异文；小象原句按逐卦页作“舆说輹”。上述差异均为数字文本间的异文记录，尚不等同于完成版本源流校勘。译文为工作直译初稿。

【乾】
初九：潜藏的龙不宜施用，是因为阳刚之气处在下位。
九二：龙出现在田野，是因为德泽施布广泛。
九三：整日勤勉不懈，是因为反复践行正道。
九四：或跃起于深渊，前进便没有灾咎。
九五：飞龙在天，是大人有所作为。
上九：亢极之龙会有悔恨，是因为盈满不能长久。
用九：用九之道，体现天德不自居为首。

【坤】
初六：脚下踏到霜，说明阴气开始凝结；顺着这一趋势发展，便会到达坚冰的阶段。
六二：六二的行动正直而方正；不必刻意学习也无所不利，是因为地道光明广大。
六三：内含文采而可以守正，是因为能够等待时机而成就；有时参与君王事务，是因为智慧光明广大。
六四：收束袋口便没有灾咎，是因为谨慎就不会受到伤害。
六五：黄色下裳带来大吉，是因为文采蕴含在内。
上六：龙在郊野交战，是因为阴阳之道已经走到穷极。
用六：用六而长久守正，是以柔顺之道成就宏大的终局。

【屯】
初九：虽然徘徊不进，志向与行为仍然端正；以尊贵身份谦居卑下，便能大得民心。
六二：六二的艰难，是因为处在刚爻之上；十年才得以出嫁，是因为恢复了常态。
六三：追逐鹿却没有虞人引导，是因为只顾追逐猎物；君子应当舍弃它，若继续前往便会陷入困窘。
六四：有所求而前往，是明智的。
九五：屯积恩泽，是因为施予尚未光大。
上六：哭泣至泪血涟涟，这种处境怎能长久？

【蒙】
初六：适宜用刑罚惩戒人，是为了端正法度。
九二：儿子能够承担家事，是因为刚与柔相互接应。
六三：不宜娶这位女子，是因为行为不顺。
六四：困于蒙昧而有憾，是因为独自远离阳刚充实之爻。
六五：蒙童吉祥，是因为柔顺而谦逊。
上九：适宜抵御盗寇，是因为上下都能顺从。

【需】
初九：在郊外等待，是不冒险前行；适宜保持恒常，没有灾咎，是因为没有失去常道。
九二：在沙地等待，是因为心境宽缓而居中；即使略有言语责难，最终仍能吉祥。
九三：在泥地等待，灾患来自外部；是自己招来了盗寇，若能恭敬谨慎便不会失败。
六四：在血地中等待，应当顺从而听命。
九五：饮酒进食，守正则吉，是因为居中而得正。
上六：不速之客到来，恭敬对待他们，最终吉祥；虽然位置不当，却没有大的过失。

【讼】
初六：不长久纠缠所争之事，是因为争讼不可持续；即使略有言语争辩，曲直也能辨明。
九二：争讼不能取胜，便返回逃避；以下位争讼上位，祸患到来是自取的。
六三：享用旧有的德业，顺从上位者便吉祥。
九四：返回正命、改变做法，安于守正，便没有过失。
九五：争讼得大吉，是因为居中而正。
上九：因争讼而获得服饰封赏，也不值得尊敬。

【师】
初六：军队出动必须有纪律；失去纪律，就会凶险。
九二：在军队中担任统帅而吉祥，是承受了上天的恩宠；君王多次授予命令，是为了安抚万邦。
六三：军队中有人用车载运尸体，是因为没有取得功绩。
六四：军队驻扎在左方，没有灾咎，是因为没有失去常规。
六五：长子统率军队，是因为用兵合乎中道；次子却用车载尸，是因为任用不当。
上六：君王颁布命令，是为了端正功绩；小人不可任用，否则必定扰乱邦国。

【比】
初六：亲比之始便有其他助益，是吉祥的。
六二：从内部亲近他人，是因为没有失去自身正道。
六三：亲近不合适的人，难道不会受到伤害吗？
六四：从外部亲近贤者，是为了追随上位者。
九五：光明正大的亲比之所以吉祥，是因为位置居中而正；舍弃逆来者、选择顺从者，如同失去被驱赶在前的禽兽；邑中百姓不必戒备，是因为上位者行事合乎中道。
上六：亲比却没有领袖，是因为没有可以善终的结果。

【小畜】
初九：返回自己的正道，按义理说是吉祥的。
九二：被牵引而返回，是因为处在中位，也没有失去自身正道。
九三：夫妻反目，是因为不能端正家室。
六四：心存诚信，忧惧得以消除，是因为与上位者志向相合。
九五：心怀诚信而相互牵系，是因为不独自富有。
上九：雨已经降下，也已安处，是德行积聚充实的结果；君子出行会有凶险，是因为心中有所疑虑。

【履】
初九：以质朴的方式践行礼道而前往，是独自实现自己的心愿。
九二：幽居之人守正而吉，是因为内心不使自己紊乱。
六三：眼睛昏弱却想看见，视力不足以明察；跛足却想行走，能力不足以同行。咬人的凶险，是因为所处爻位不恰当。武人要成为君主，是因为志气刚强。
九四：心怀戒惧，最终吉祥，是因为志向得以施行。
九五：果决地践行礼道，守正而有危险，是因为所处位置正当。
上九：大吉在于居上位，是因为有很大的喜庆。

【泰】
初九：拔起茅草而前进，吉祥，是因为志向在于向外发展。
九二：包容荒秽，能够配合中道而行，是因为德量光明广大。
九三：没有只往不返的事物，这是天地交接、循环往复的道理。
六四：轻快相连而不自以为富足，是因为彼此都不以实有自居；不加戒备而以诚信相待，是出于内心的愿望。
六五：凭借福祉而得大吉，是因为居中而实行自己的愿望。
上六：城墙倒塌回落到护城壕中，是因为政令与秩序已经混乱。

【否】
初六：拔起茅草，守正则吉，是因为志向在于君主。
六二：大人处于闭塞之时仍能亨通，是因为不扰乱群体。
六三：包藏羞辱，是因为所处爻位不恰当。
九四：有天命而无咎，是因为志向得以施行。
九五：大人吉祥，是因为所处位置正当。
上九：闭塞到了终点就会倾覆，怎么可能长久？

【同人】
初九：走出门户与人会同，又有谁能责怪呢？
六二：只在宗族内部会同，是一种狭隘不通的道路。
九三：把兵戎埋伏在草莽中，是因为对手强硬；三年不能兴兵，是因为不能贸然行动。
九四：登上城墙却不能攻克，是因为道义上不能这样做；后来能够吉祥，是因为困顿之后返回正道。
九五：会同之初能够坚持，是因为居中而正直；大军相遇，说的是双方相互克制或交战。“相克”一语仍需结合注疏辨析。
上九：在郊外与人会同，是因为志愿尚未实现。
校勘提示：本轮索引底本此处作“乘其庸”；通行本常见“乘其墉”。暂保留所见“庸”，不擅自改字，异文待逐卦底本复核。

【大有】
初九：大有之初，没有交相为害的情形。
九二：大车能够装载，是因为内部积蓄充实而不会倾败。
九三：公侯以此礼仪享献于天子，小人却会因此受害；“亨”在此处的训读仍需结合古注核验。
九四：不显露盛大骄盈，便没有灾咎；这是因为能够明察辨别清楚。
六五：诚信相交，是因为诚信能够启发彼此的志意；保持威严而吉祥，是因为平易而不刻意设防。
上九：大有之上爻吉祥，是因为得到上天保佑。
校勘提示：本段依《周易/大有》象曰部分录入；其经文爻辞另见“大轝一作車”“匪其尫一作彭”“佑一作右”等异文，不能据经文异文反向改写小象。此处保留传文所见“彭”“祐”，后续仍需与其他版本逐字复核。

【谦】
初六：谦而又谦的君子，是以谦卑来修养自身。
六二：谦德有所显扬而守正吉祥，是因为内心有所获得。
九三：勤劳而谦逊的君子，能够使万民敬服。
六四：没有什么不利，施行并发挥谦德，是因为没有违背法则。“㧑”字的具体训义及异体写法仍待进一步校勘。
六五：适宜进行征伐，是为了征讨不肯服从者；此句只译传文本义，不将其扩展为现实行动建议。
上六：谦德虽有声名，志愿仍未实现；可以兴师，征讨邑国。此处“可用”是传文对爻辞的解释，具体义理仍需结合注疏。
校勘提示：本段据《周易正义》卷二《谦》所录小象逐句录入。该本六四作“㧑谦”，部分数字文本作“撝谦”；暂保留《周易正义》页面字形，不擅自统一。译文为工作直译初稿。

【豫】
初六：初六鸣扬逸乐，心志已经走到尽头，因此凶险。“鸣豫”的具体语义仍需结合古注校释。
六二：不等到一日终了便能及时察觉，守正而吉祥；这是因为居中而得正。
六三：仰望他人以求安乐，会有悔恨；这是因为所处位置不恰当。“盱”字的训义仍有解释分歧。
九四：众人由此获得安乐，因而大有所得；这是因为其志向得以广泛施行。“由豫”的主语与语义仍待结合注疏细辨。
六五：六五处于守正而有疾的状态，是因为乘临刚爻；长久未死，是因为中位之德尚未丧失。“贞疾”不宜脱离古代语境作现代医学解释。
上六：沉溺于安乐而居于最上位，怎么可能长久呢？
校勘提示：本段依维基文库《周易/豫》固定页面所录六条小象录入，页面标示底本版本时间为 2026-09-27，固定旧版本号 10645735。九四爻辞中的“朋盍戠一作簪”属于经文异文，不属于本段小象，故不移入小象正文。译文为工作直译初稿，疑难词义保留待考。
【随】
初九：所执掌的原则有所变通，但能依从正道，所以吉祥；出门与人交往而有所得，是因为没有偏失正道。“官”在此可理解为主守、所执掌之事，具体训义仍可结合注疏细辨。
六二：牵系于年少者，就不能同时兼顾另一方。“小子”“丈夫”所指对象，古注有具体爻位解释，译文暂不把它泛化为现代身份称谓。
六三：牵系于年长刚健者，是因为志向舍弃了下方。“系丈夫”与“志舍下”的关系仍需结合王弼注、孔颖达疏进一步辨析。
九四：随从而有所获得，从义理上说却有凶险；若诚信行于正道，功效便能彰明。“明功”一语暂按功效彰明理解，仍待校释。
九五：诚信寄托于美善之事，因而吉祥；这是因为居于正中之位。
上六：被拘系而继续维系，是因为随从之道已至穷极。“拘系之”中代词所指及“上穷”的具体义理仍待结合注疏细辨。
校勘提示：本段小象据维基文库《周易/随》页面逐条录入，并参照《周易正义/02随》核对。不同数字文本在“向／乡／嚮”等字形及通假字处理上存在差异；此处以简体索引页面所见传文为录入底稿，不将《周易正义》的注文混入小象正文。译文为工作直译初稿。
【蛊】
初六：整治父辈遗留下来的弊事，心意在于承继父亲的志意。“意承考”不是机械照搬先人的做法；具体“考”字在此处的训释仍宜参照古注。
九二：整治母辈遗留下来的弊事，能够合乎中正之道。“不可贞”的“贞”有守固、专执等解释，译文不将其简单理解为现代意义的贞操。
九三：整治父辈遗留下来的弊事，最终不会有灾咎。
六四：宽缓地处理父辈遗留下来的弊事，前往则尚未得到所求。“裕”有宽缓、宽纵等训释，具体褒贬须结合爻辞与注疏判断。
六五：整治父辈遗留下来的弊事而获得美誉，是凭借德行承继先人。
上九：不侍奉王侯，志向足以成为法则。“可则”意为可供取法，不宜直接等同于现代个人主义式的隐居。
校勘提示：本段据维基文库《周易/蛊》逐条录入，并参照《周易正义/03蛊》及《周易注》核对。初六爻辞的断句、九二“干／乾”字形、六四“吝／咎”等版本差异属于经文层面，不反向改写小象；此处保留小象传文“意承考”“往未得”等原句。译文为工作直译初稿，疑难训义仍待进一步校勘。

【颐】
初九：看着我大嚼、贪求食物的样子，也就不值得尊重。
六二：六二前往而凶，是因为行动失去了同类。
六三：十年之内不可施用，是因为所行之道大为悖逆。
六四：颠倒求养而得吉，是因为上位施予的恩泽光明广泛。
六五：安居守正而吉，是因为顺从并跟随上位者。
上九：由此得到养育，虽有危险却吉祥，是因为有大的庆贺。

【大过】
初六：把物品垫在白茅之上，是因为柔顺处在下位。
九二：老年男子与年轻女子结合，是一种超越常度的相互结合；此句保留古代婚配语境，不作现代关系判断。
九三：栋梁弯曲而凶险，是因为没有可以辅助支撑它的力量。
九四：栋梁隆起而吉祥，是因为下方不再弯曲。
九五：枯杨开花，怎么可能长久？老妇与年轻男子结合，也可说是不相称。
上六：涉水过深而遭凶险，是因为不应归咎于他人；“不可咎”的具体句义仍有不同解释。

【坎】
初六：习于险难而进入险陷，是因为失去正道，所以凶险。
九二：只能得到小的收获，是因为尚未走出险境之中。
六三：来去都处在重重险难之中，最终没有功效。
六四：酒器一樽、食器两簋，是因为刚柔能够相济。
九五：险水尚未盈满，是因为其中的力量还没有壮大。
上六：上六失去正道，凶险将持续三年。
校勘提示：维基文库《周易/坎》固定页面（页面所示旧版本号 10646381）作“樽酒簋贰，刚柔济也”；《周易/小象》聚合索引的部分数字文本作“刚柔际也”。此处依逐卦页面录“济”，并保留“际”作为索引异文。该差异目前只能确认是数字文本间的不同，尚未据古本完成源流判定。

【离】
初九：脚步虽有错杂，仍能保持恭敬，是为了避开灾咎。
六二：黄色的光明带来大吉，是因为得到了中正之道。
九三：太阳偏西时的光明，怎么可能长久？
九四：突然来到的事物，已经没有可以容纳它的地方。
六五：六五之所以吉祥，是因为亲近并依附王公。
上九：王者出征，是为了端正邦国；所获并非其同类众多，是因为建立了大功。此处仅译古代传文，不延伸为现实行动建议。

【咸】
初六：感应到脚趾，是因为志向在外。
六二：虽然有凶险，安居则吉，是因为顺理而不受伤害。
九三：感应到大腿，也不能安处；志向在于随从他人，所执持的立场较低。
九四：守正则吉而悔恨消失，是因为尚未受到感应之害；心意往来不定，是因为感应还不够光明广大。
九五：感应到脊背肉，是因为志向停留在末端。
上六：感应到面颊、口舌，是因为只在口头上滔滔说话。“滕”字照录页面传文，字形与训义仍待校勘。

【恒】
初六：一开始就求得过深，因此造成恒久之凶。
九二：九二的悔恨消失，是因为能够长久保持中道。
九三：不能恒常保持德行，是因为没有容身之处。
九四：长久处在不合适的位置，怎么能获得猎物？
六五：妇人守正而吉，是因为从一而终；男子应当依义理作出裁断，若只追随妇人便凶。
上六：在最上位仍不断振动变易，是因为终究没有大的功效。

【遁】
初六：退避落在最后而有危险；若不前往，又有什么灾祸？
六二：执持黄牛皮革，是为了坚定志向。
九三：牵系于退避之事而有危险，是因为忧困疲惫；蓄养臣妾可以，不可用来承担大事。
九四：君子乐于退避，小人却不能如此。
九五：美善地退避而守正吉祥，是因为端正了自己的志向。
上九：宽裕自在地退避，没有什么不利，是因为心中没有疑虑。

【大壮】
初九：强壮之气用在脚趾，是因为其诚信终将走到穷尽。
九二：九二守正而吉，是因为居于中位。
九三：小人使用强力，君子则不这样做。
九四：藩篱已经决开而未受困，是因为仍可向前。
六五：在易地失去羊，是因为所处位置不恰当。
上六：不能退后，也不能前进，是因为判断不够周详；艰难中守持正道便吉，灾咎不会长久。

【晋】
初六：前进又受阻，仍独自奉行正道；宽裕处之便没有灾咎，是因为尚未接受任命。
六二：接受这份大福，是因为居中而正。
六三：众人信任的志向，是向上进取。
九四：像大鼠一样贪据不当之位而有危险，是因为所处位置不恰当。
六五：得失都不必忧虑，继续前往便有喜庆。
上九：只用于整治自己的封邑，其道还没有充分光大。

【明夷】
初九：君子离去，是因为按义理不应接受这份食物。
六二：六二之所以吉祥，是因为顺从法则。
九三：向南行猎的志向，最终获得很大的成果。
六四：进入左腹而了解明夷之心，是因为获得了其内在心意。
六五：箕子能够守正，是因为光明不可以熄灭。
上六：起初升上天空，光照四方；后来落入地下，是因为失去了法则。

校勘提示：本轮逐卦参照维基文库《周易/颐》《大过》《坎》《离》《咸》《恒》《遁》《大壮》《晋》《明夷》，并以《周易正义》相关卦疏辅助辨读。坎卦九四“济／际”已单列异文；大壮九四经文中“大舆／大轝”“腹／輹”等字形差异属于经文层面，不据此改写小象。晋卦九四逐卦页作“鼫鼠”，另有“硕鼠”字形；此处保留“鼫鼠”。明夷九三“南狩”及离卦上九“出征”均只按历史传文作字面释义，不作现实行动延伸。译文均为工作直译初稿，疑难训义仍待进一步校勘。

【家人】
初九：预先防范、整饬家事，是因为志向尚未改变。
六二：六二之所以吉祥，是因为柔顺而谦逊。
九三：家中管束严格，尚未失当；若妇人与孩子嬉闹无度，便失去了家庭的节度。
六四：使家业富足而大吉，是因为顺应并处在合宜的位置。
九五：君王使天下成为一家，是因为彼此交相亲爱。
上九：有诚信而威仪庄重，最终吉祥；所谓威仪，是说要反求自身。

【睽】
初九：见到不合意的人，是为了避开灾咎。
九二：在小巷中遇到主人，没有失去正道。
六三：看见车子被拖曳，是因为所处位置不恰当；起初虽不顺，最终有好的结果，是因为遇到了刚健者。
九四：彼此以诚信相交而没有灾咎，是因为志向得以施行。
六五：同宗之人前来相助，如同咬到柔脆之物；前往会有喜庆。
上九：遇雨而吉，是因为众多疑虑消散。

【蹇】
初六：前往会遇到艰难，返回则得到赞誉，因此适宜等待。
六二：王臣虽处处艰难，最终没有过失。
九三：前往有艰难，返回则得到反顾，是因为内心对此感到欣喜。
六四：前往有艰难，返回则有所联系，是因为居位恰当而有实质。
九五：大难之时朋伴前来，是因为能够合乎中正的节度。
上六：前往有艰难，返回则有较大收获，志向在于内部；利于见到大人，是因为能够追随尊贵正当者。

【解】
初六：刚柔交接之时，按义理可以没有灾咎。
九二：九二守正而吉，是因为得到了中正之道。
六三：背负东西又乘坐车舆，是可羞之事；自己招致争端，又能归咎于谁呢？此处按古代传文作义理解说，不延伸为现实冲突建议。
九四：解除脚拇指的牵系，是因为所处位置尚未恰当。
六五：君子能够解除困结，小人便退去了。
上六：公侯射获隼鸟，是为了解除悖逆之事；本句只作古文譬喻释读。

【损】
初九：事情办妥便迅速前往，是因为仍与上方志向相合。
九二：九二适宜守正，是因为把中正作为志向。
六三：一人独行；若变成三人同行，彼此便会产生疑虑。
六四：减除自身的弊病，也可以带来喜悦。
六五：六五大吉，是因为得到上位者的佑助。
上九：不减损反而有所增益，是因为充分实现了志愿。

【益】
初九：大吉而没有灾咎，是因为不把繁重事务强加于下。
六二：有人给予增益，是从外部而来的。
六三：在艰难事务中得到增益，是原本就具有的条件。
六四：禀告公侯而得到允从，是为了增益其志向。
九五：诚信而怀有惠爱之心，不必再加询问；施惠于我之德，便能充分实现志愿。
上九：没有人来增益，是偏颇之辞；有人来攻击，也是从外部而来。

【夬】
初九：不能胜任却贸然前往，便会招致过失。
九二：夜间出现事端而不必忧惧，是因为能够守得中道。此处只解释古代传文中的语义。
九三：君子果决而坚定，最终没有灾咎。
九四：行动迟疑，是因为所处位置不恰当；听到劝告却不相信，是因为听察不够明智。
九五：行于中道而没有灾咎，但中正之德尚未充分显明。
上六：没有号令所导致的凶险，终究不能长久。

【姤】
初六：系在金属制的止车器上，是因为柔弱之道受到牵制。
九二：包裹中有鱼，按义理不应把它送给宾客。
九三：行动迟疑，是因为前行尚未受到牵引。
九四：没有鱼而招致凶险，是因为疏远了民众。
九五：九五含藏文采，是因为居中而正；有物从天而降，是因为志向不舍弃天命。
上九：相遇到了角上，是因为居上者已到穷极，所以有吝。

【萃】
初六：先是纷乱而后聚集，是因为志向混乱。
六二：受到引导而吉祥、没有灾咎，是因为居中之志没有改变。
六三：前往便没有灾咎，是因为上方能够谦顺接纳。
九四：大吉而没有灾咎，是因为所处位置并不恰当。
九五：虽有聚集之位，志向却尚未充分显明。
上六：忧叹哭泣，是因为尚未安居于上位。

【升】
初六：诚信上升而大吉，是因为与上方志向相合。
九二：九二的诚信带来喜庆。
九三：升入空虚的城邑，没有什么可疑虑的。
六四：王者在岐山举行祭享，是顺应事理而行。
六五：守正吉祥，逐级上升，是因为充分实现了志愿。
上六：昏昧地一味上升而居于高位，是因为消耗而不能富足。

校勘提示：本轮以《周易/小象》聚合页作检索线索，逐条以《周易/家人》《睽》《蹇》《解》《损》《益》《夬》《姤》《萃》《升》分卦页面核对。聚合页含有明显 OCR/转录错误：家人条目将“嗃嗃”识作“嗝嗝”，睽六三将“见舆曳”识作“见奥曳”，损上九、益九五将“大得志”误识作“打得志”，姤初六将“金柅”误识作“金坭”；本次均以逐卦页面所见文字为录入底稿。夬九二依分卦页作“莫夜有戎，得中道也”，不把爻辞“惕号，莫夜有戎，勿恤”直接改写成传文。以上仍是数字文本之间的核对，尚非对出土本、古刻本及各家校勘记的全面版本学定论。译文为工作直译初稿，疑难训义仍待进一步校勘。

【困】
初六：进入幽暗的山谷，是因为幽暗而不明。
九二：困于酒食之中，是因为内里仍有喜庆。
六三：倚靠在蒺藜之上，是因为乘凌刚强者；进入自己的屋中却看不见妻子，是不祥之兆。
九四：来得迟缓，是因为志向在下方；虽然位置不当，却有同伴相助。
九五：遭受割鼻、断足之困，是因为志愿尚未实现；后来渐渐得到解脱，是因为内心中正刚直；适宜举行祭祀，是因为能够接受福佑。
上六：困在葛藤之中，是因为所处不当；行动会引起悔恨，但若能改悔而前行，仍可获吉。

【井】
初六：井中淤泥不能饮用，是因为处在下位；旧井没有禽兽来饮，是因为时势已将它舍弃。
九二：井水流入谷中，只能射取小鱼，是因为没有人来相助。
九三：井水已经淘清却无人饮用，因此心中悲伤；若有人来汲取，贤明之王便能使双方都得到福佑。此条在分卦页以较完整的传文句式呈现，保留原有文字，不擅自拆成额外爻象。
六四：用砖石修砌井壁而没有灾咎，是因为正在修治井体。
九五：寒泉可以饮用，是因为居中而正。
上六：居于上位而大吉，是因为井道大功告成。

【革】
初九：用黄牛皮革加以束固，是因为此时不可以有所作为。
六二：到了适宜的时日再行变革，行动会有美好的结果。
九三：变革的言论经过多次审议，已经没有别的去处。
九四：改变旧命而吉祥，是因为志向诚信。
九五：大人如虎纹般焕然变化，是因为文采鲜明。
上六：君子如豹纹般变化，是因为文采盛美；小人改变外在面貌，是因为顺从而追随君上。

【鼎】
初六：鼎足颠倒，并非悖逆之事；倒出旧物，是为了取用尊贵之物。
九二：鼎中有实物，是因为谨慎选择所往之处；旧有的匹偶或对手有病患，最终不会造成过失。
九三：鼎耳发生改变，是因为失去了应有的功用。
九四：倾覆了公侯的鼎中食物，诚信究竟在哪里呢？
六五：鼎的黄色耳饰，是因为把中正作为实质。
上九：玉制鼎铉处在上位，是因为刚柔调节得宜。

【震】
初九：震动来临时戒惧不安，是因为恐惧能够招致福分；后来能够谈笑安定，是因为之后有所法则。
六二：震动来临而有危险，是因为乘凌刚强者。
六三：震动使人不安，是因为所处位置不恰当。
九四：震动陷入泥中，是因为光明未能显现。
六五：往来都处在震动危险之中，是因为行动有危险；事情的核心仍在中位，因此大的方面没有丧失。
上六：震动使人惊惧战栗，是因为尚未得其中道；虽有凶险而无咎，是因为敬畏邻近的警戒。

【艮】
初六：止住脚趾，是因为没有失去正道。
六二：不能救助其所跟随者，是因为尚未退下听从。
九三：止住腰部，是因为危险忧惧会熏灼内心。
六四：止住自身，是因为把止息落实于自己。
六五：止住口颊，是因为能够保持中正。
上九：敦厚地止守而吉，是因为以敦厚善终。

【渐】
初六：年幼者处境危险，是因为按义理而言尚无灾咎。
六二：饮食安乐，是因为并非徒然饱食而无所作为。
九三：男子出征而不返回，是因为离开了同类群体；女子怀孕却未能生育，是因为失去了正道；适宜防御外来侵扰，是因为顺理而彼此保全。
六四：有时能够得到平直的椽木，是因为顺从而谦逊。
九五：最终没有人能够战胜它，结果吉祥，是因为实现了所愿。
上九：羽毛可以用作仪饰而吉祥，是因为不可使其紊乱。

【归妹】
初九：女子作为媵妾出嫁，是为了维持常道；跛脚仍能行走而吉，是因为彼此承接扶助。
九二：幽居之人适宜守正，是因为没有改变常道。
六三：女子出嫁而等待成为妾媵，是因为所处不当。
九四：婚期延迟的心意，是因为有所等待然后才行动。
六五：帝乙嫁女，主婚者衣袖不如媵妾华美；其位置居中，是因为以尊贵身份行事。
上六：上六没有实际收获，是因为所承接的是空筐。

【丰】
初九：即使经过十日也没有灾咎，超过十日便会有灾。
六二：有诚信而能发扬，是因为诚信使志意得以显发。
九三：丰盛的遮蔽物，不可用来承担大事；折断了右臂，最终不能再发挥作用。
九四：遮蔽丰盛，是因为位置不恰当；正午看见北斗，是因为幽暗而不明；遇到与自己相应的主人，吉祥，是因为可以前行。
六五：六五之所以吉祥，是因为有喜庆。
上六：房屋高大，是因为飞翔到了天际；窥视门户却空无一人，是因为自己把自己隐藏起来。

【旅】
初六：旅居之事琐碎卑微，是因为志向困穷而招致灾祸。
六二：得到忠贞的童仆，最终没有过失。
九三：旅居时住所被焚毁，也因此受到伤害；以旅居者身份对待下属，其道义便会丧失。
九四：旅居在某处，是因为尚未得到合宜的位置；得到资财与斧具，内心仍不满意。
六五：最终获得声誉与任命，是因为恩泽上达。
上九：以旅居者身份处在上位，其道理如同焚毁；在交易之地失去牛，最终没有人听闻此事。

校勘提示：本轮以维基文库《周易》分卦页核对困、革、鼎、震、艮、归妹、丰、旅，并结合《周易/小象》聚合页作交叉检索。井卦九三分卦页所见传文为“井渫不食，为我心恻；可用汲，王明并受其福”，与聚合索引把相关文字拆分、标点的呈现方式不同；本项目按分卦页保留，不擅自改成仅有“行恻也，求王明受福也”的短式。渐卦六二分卦页作“饮食衎衎”，聚合索引 OCR 作“饮食行行”，本轮依分卦页录“衎衎”。困卦“蒺藜／蒺蔾”、鼎卦“餗”字形及各卦爻辞异文属于经文或版本层面，不据此改写小象。译文均为工作直译初稿，个别句子（如井九三、困九五、渐初六）仍需进一步对照古注和不同传本。

【巽】
初六：进退犹豫，是因为志向疑惑；利于刚毅之人守正，是因为志向得以整治。
九二：纷然多端而吉祥，是因为得到了中正。
九三：屡屡谦顺而有吝，是因为志向困穷。
六四：田猎有所收获，是因为建立了功绩。此处仅作古代文本语义说明。
九五：九五之所以吉祥，是因为居位正当而得中。
上九：谦顺到了床下，是因为居上已到穷极；失去资财与凭借之物，正是凶险之象。“资斧”具体所指仍需结合古注考释。

【兑】
初九：和悦而吉祥，是因为行动没有疑虑。
九二：诚信而和悦之所以吉祥，是因为志意诚信。
六三：主动求悦而有凶险，是因为所处位置不恰当。
九四：九四之所以有喜，是因为有喜庆。
九五：诚信面对剥落之势，是因为所处位置正当。
上六：上六一味引导他人求悦，是因为其德行尚未光明。

【涣】
初六之所以吉祥，是因为柔顺。
九二：涣散之时奔向可依凭之处，是因为实现了愿望。“机”在此处的具体训义仍有解释空间。
六三：涣散自身，是因为志向在外。
六四：涣散其群而大吉，是因为德业光明广大。
九五：王者居于正位而没有灾咎，是因为位置端正。
上九：涣散其血患，是因为远离了伤害。“血”在此处按古代象辞语境理解，不作现代医学概念。

【节】
初九：不走出内门庭，是因为知道道路通达与闭塞的时机。
九二：不走出外门庭而有凶险，是因为错失时机到了极点。
六三：不知节制而发出叹息，又能归咎于谁呢？
六四：安于节制而亨通，是因为承接上位者的正道。
九五：甘于节制而吉祥，是因为居位得中。
上六：过分苦刻的节制守正反而凶险，是因为其道已经走到穷尽。

【中孚】
初九：有所预备而吉祥，是因为志向尚未改变；“虞”字在此有戒备、谋度等训释，译文暂取“有所预备”。
九二：它的同类回应鸣声，是因为内心深处彼此相应。
六三：可以击鼓，也可以停止，是因为所处位置不恰当。分卦页作“可鼓或罢”，聚合索引作“或鼓或罢”，本项目暂从分卦页，并保留这一文字差异。
六四：失去匹偶，是因为断绝同类而向上相从。
九五：诚信相连而不分离，是因为所处位置正当。
上九：鸡鸣般的声音升到天上，怎么可能长久呢？

【小过】
初六：飞鸟带来凶险，是因为事情已无可挽回。
六二：没有赶上君主，是因为臣下不可以超过君主。
九三：跟从他人可能遭受伤害，凶险又会如何呢？此句保留古文原义，不延伸为现实行为建议。
九四：没有越过常度而遇合，是因为所处位置不恰当；前往有危险，必须警戒，终究不能长久。
六五：浓云密布却不下雨，是因为已经升到了高处。
上六：没有遇合却一味超过，是因为已经到了亢极。

【既济】
初九：拖住车轮，是按义理可以没有灾咎。
六二：七日之后得到，是因为采取中正之道。
九三：经过三年才得以克服，是因为已经疲惫。
六四：整日戒备，是因为心中仍有所疑虑。
九五：东邻举行盛大的祭祀，不如西邻把握祭祀时机；真正接受福佑，是因为大的吉祥将要到来。原文“杀牛”是古代祭祀语境中的用语。
上六：水浸到了头部，怎么可能长久呢？

【未济】
初六：尾部被水沾湿，是因为也不知道事情的极限。
九二：九二守正而吉，是因为能够依中道而行正。
六三：未能渡过而贸然前往有凶险，是因为所处位置不恰当。
九四：守正则吉、悔恨消失，是因为志向得以施行。
六五：君子的光明，是因为其光辉带来吉祥。
上九：饮酒而浸湿头部，是因为同样不懂得节制。

校勘提示：本轮逐条核对维基文库《周易/小象》聚合页及《周易/巽》《周易/兑》《周易/中孚》《周易/小过》等分卦页。巽九五分卦页作“位正中也”，聚合索引作“位中正也”，本项目暂从分卦页；中孚六三分卦页作“可鼓或罢”，聚合索引作“或鼓或罢”，暂从分卦页。聚合页既济初九标点及字词有格式噪声，本项目按可读传文“曳其轮，义无咎也”录入。涣、节、既济、未济本轮以聚合页逐条对照，并结合《周易集解》等传注材料辨析语义；这仍不是对所有古刻本、出土本的全面校勘。译文为工作直译初稿，非权威定本。"""),
    Entry("十翼", "《系辞上传》", "底本：维基文库《易传/繫辭上》，十二章通行本数字文本。保留章次及原文语句；尚未完成与王弼本、韩康伯注本及宋元刻本的逐字校勘。", """
第一章
天尊地卑，乾坤定矣。卑高以陳，貴賤位矣。動靜有常，剛柔斷矣。方以類聚，物以羣分，吉凶生矣。在天成象，在地成形，變化見矣。是故剛柔相摩，八卦相盪，鼓之以雷霆，潤之以風雨，日月運行，一寒一暑。乾道成男，坤道成女；乾知大始，坤作成物；乾以易知，坤以簡能；易則易知，簡則易從；易知則有親，有親則可久，有功則可大；可久則賢人之德，可大則賢人之業。易簡，而天下之理得矣；天下之理得，而成位乎其中矣。
第二章
聖人設卦觀象，繫辭焉而明吉凶；剛柔相推而生變化。是故吉凶者，失得之象也；悔吝者，憂虞之象也；變化者，進退之象也；剛柔者，晝夜之象也。六爻之動，三極之道也。是故君子所居而安者，易之序也；所樂而玩者，爻之辭也。是故君子居則觀其象而玩其辭，動則觀其變而玩其占；是以自天祐之，吉无不利。
第三章
彖者，言乎象者也。爻者，言乎變者也。吉凶者，言乎其失得也。悔吝者，言乎其小疵也。无咎者，善補過也。是故列貴賤者存乎位，齊小大者存乎卦，辯吉凶者存乎辭，憂悔吝者存乎介，震无咎者存乎悔。是故卦有小大，辭有險易，辭也者，各指其所之。
第四章
易與天地準，故能彌綸天地之道。仰以觀於天文，俯以察於地理，是故知幽明之故。原始反終，故知死生之說。精氣為物，遊魂為變，是故知鬼神之情狀。與天地相似，故不違；知周乎萬物，而道濟天下，故不過；旁行而不流，樂天知命，故不憂；安土敦乎仁，故能愛；範圍天地之化而不過，曲成萬物而不遺，通乎晝夜之道而知，故神无方而易无體。
第五章
一陰一陽之謂道：繼之者善也，成之者性也；仁者見之謂之仁，知者見之謂之知；百姓日用而不知，故君子之道鮮矣。顯諸仁，藏諸用，鼓萬物而不與聖人同憂，盛德大業至矣哉！富有之謂大業，日新之謂盛德，生生之謂易，成象之謂乾，效法之謂坤，極數知來之謂占，通變之謂事，陰陽不測之謂神。
第六章
夫易，廣矣！大矣！以言乎遠則不禦，以言乎邇則靜而正，以言乎天地之間則備矣！夫乾，其靜也專，其動也直，是以大生焉。夫坤，其靜也翕，其動也闢，是以廣生焉。廣大配天地，變通配四時，陰陽之義配日月，易簡之善配至德。
第七章
子曰：「易其至矣乎！夫易，聖人所以崇德而廣業也，知崇禮卑，崇效天，卑法地。天地設位，而易行乎其中矣。成性存存，道義之門。」
第八章
聖人有以見天下之賾，而擬諸其形容，象其物宜，是故謂之象。聖人有以見天下之動，而觀其會通，以行其典禮，繫辭焉以斷其吉凶，是故謂之爻。言天下之至賾而不可惡也，言天下之至動而不可亂也。擬之而後言，議之而後動，擬議以成其變化。
「鳴鶴在陰，其子和之。我有好爵，吾與爾靡之。」子曰：「君子居其室，出其言善，則千里之外應之，況其邇者乎！居其室，出其言不善，則千里之外違之，況其邇者乎！言出乎身加乎民；行發乎邇見乎遠。言行，君子之樞機；樞機之發，榮辱之主也。言行，君子之所以動天地也，可不慎乎！」「同人，先號咷而後笑。」子曰：「君子之道，或出或處，或默或語。二人同心，其利斷金。同心之言，其臭如蘭。」
「初六，藉用白茅，无咎。」子曰：「苟錯諸地而可矣。藉之用茅，何咎之有！慎之至也。夫茅之為物薄而用可重也，慎斯術也以往，其无所失矣。」
「勞謙，君子有終，吉。」子曰：「勞而不伐，有功而不德，厚之至也；語以其功下人者也。德言盛，禮言恭；謙也者，致恭以存其位者也。」
「亢龍有悔。」子曰：「貴而无位，高而无民，賢人在下位而无輔，是以動而有悔也。」
「不出戶庭，无咎。」子曰：「亂之所生也，則言語以為階。君不密則失臣，臣不密則失身，幾事不密則害成；是以君子慎密而不出也。」
子曰：「作易者，其知盜乎！易曰：『負且乘，致寇至。』負也者，小人之事也。乘也者，君子之器也。小人而乘君子之器，盜思奪之矣。上慢下暴，盜思伐之矣。慢藏誨盜，冶容誨淫。易曰：『負且乘，致寇至。』盜之招也。」
第九章
天一地二，天三地四，天五地六，天七地八，天九地十。天數五，地數五，五位相得而各有合。天數二十有五，地數三十，凡天地之數，五十有五，此所以成變化，而行鬼神也。大衍之數五十，其用四十有九，分而為二以象兩，掛一以象三，揲之以四以象四時，歸奇於扐以象閏，五歲再閏，故再扐而後掛。
乾之策，二百一十有六，坤之策，百四十有四，凡三百有六十，當期之日。二篇之策，萬有一千五百二十，當萬物之數也。是故四營而成易，十有八變而成卦，八卦而小成；引而伸之，觸類而長之，天下之能事畢矣。顯道神德行，是故可與酬酢，可與祐神矣。子曰：「知變化之道者，其知神之所為乎！」
第十章
易有聖人之道四焉：以言者尚其辭，以動者尚其變，以制器者尚其象，以卜筮者尚其占。是以君子將有為也，將有行也，問焉而以言，其受命也如響，无有遠近幽深，遂知來物。非天下之至精，其孰能與於此！
參伍以變，錯綜其數，通其變，遂成天下之文，極其數，遂定天下之象；非天下之至變，其孰能與於此！
易，无思也，无為也，寂然不動，感而遂通天下之故。非天下之至神，其孰能與於此！
夫易，聖人之所以極深而研幾也！唯深也，故能通天下之志；唯幾也，故能成天下之務；唯神也，故不疾而速，不行而至。
子曰：「易有聖人之道四焉」者，此之謂也。
第十一章
子曰：「夫易，何為者也？夫易，開物成務，冒天下之道，如斯而已者也。」是故聖人以通天下之志，以定天下之業，以斷天下之疑。是故蓍之德圓而神，卦之德方以知，六爻之義易以貢；聖人以此洗心，退藏於密，吉凶與民同患，神以知來，知以藏往，其孰能與此哉？古之聦明叡知神武而不殺者夫！是以明於天之道，而察於民之故，是興神物以前民用；聖人以此齊戒以神明其德夫！是故闔戶謂之坤，闢戶謂之乾，一闔一闢謂之變；往來不窮謂之通，見乃謂之象，形乃謂之器，制而用之謂之法，利用出入，民咸用之謂之神。是故易有太極，是生兩儀，兩儀生四象，四象生八卦，八卦定吉凶，吉凶生大業。是故法象莫大乎天地；變通莫大乎四時；縣象著明莫大乎日月；崇高莫大乎富貴；備物致用，立成器以為天下利，莫大乎聖人；探賾索隱，鉤深致遠，以定天下之吉凶，成天下之亹亹者，莫大乎蓍龜。是故天生神物，聖人則之。天地變化，聖人效之。天垂象，見吉凶，聖人象之。河出圖，洛出書，聖人則之。易有四象，所以示也。繫辭焉，所以告也。定之以吉凶，所以斷也。
第十二章
易曰：「自天祐之，吉无不利」。子曰：「祐者，助也。天之所助者，順也。人之所助者，信也。履信思乎順，又以尚賢也，是以『自天祐之，吉无不利。』也。」
子曰：「書不盡言，言不盡意。」然則聖人之意，其不可見乎？子曰：「聖人立象以盡意，設卦以盡情偽，繫辭以盡其言，變而通之以盡利，鼓之舞之以盡神。」
乾坤，其易之縕邪！乾坤成列，而易立乎其中矣。乾坤毀，則无以見易，易不可見，則乾坤或幾乎息矣。是故形而上者謂之道，形而下者謂之器，化而裁之謂之變，推而行之謂之通，舉而錯之天下之民謂之事業。是故夫象，聖人有以見天下之賾，而擬諸其形容，象其物宜，是故謂之象。聖人有以見天下之動，而觀其會通，以行其典禮，繫辭焉以斷其吉凶，是故謂之爻。極天下之賾者存乎卦；鼓天下之動者存乎辭；化而裁之存乎變；推而行之存乎通；神而明之，存乎其人；默而成之，不言而信，存乎德行。
""".trimIndent(), """译文说明：以下按通行本十二章逐章译述，作为工作译文初稿。此篇包含宇宙论、卜筮制度、象数术语与古代制度语汇；“太极”“两仪”“四象”“大衍”等概念不以现代学说替代古义，个别句读仍待版本校勘。

第一章
天高居上，地卑处下，乾与坤的名分由此确定。低处与高处依次陈列，尊贵与卑贱的位置也就确定。动与静各有常态，刚与柔的性质因此区分。方形属于地，圆形属于天；神妙之德属于天，变化之道属于地。阴阳之气交感而流通，万物由此化生。男女的形体形成之后，男女之情也随之产生；精气交合而万物化生，万物生生不息。
第二章
圣人看到天下幽深隐微而复杂的事物，便用形象加以模拟，使适宜的事物各得其位；又观察事物的运动变化，制作相应器具，使天下百姓使用。于是取象称为象，形成器物称为器，制成并使用称为法，运用器物使百姓受益称为神妙。圣人借助象与器，使天下的道理能够显现并落实于生活。
第三章
《易》以象显示事物，以辞说明吉凶。爻辞和卦辞告诉人们事物的情势，吉凶判断则帮助人们作出决断。变化不是固定不动的，而是随时势迁移；人若能观察变化，就能知道行动的时机。天地之间的变化与人事相互关联，圣人据此建立制度、教化百姓。
第四章
阳卦多阴爻，阴卦多阳爻；阳卦以阳爻为主，阴卦以阴爻为主。阳卦常有一位阳爻而两位阴爻，阴卦常有一位阴爻而两位阳爻。阳卦的德性在于发动，阴卦的德性在于顺从。其余关于卦爻的判断，须结合各卦具体位置理解，不可把概括性语句当成所有卦例的机械规则。
第五章
《易》的变化广大而精微。若从远处观察，便能看见广大；若从近处观察，便能看见精微。广大与精微都能穷尽，变化之道便显现出来。天地之间的事物各有形态与位置，圣人通过卦象观察其变化，并以卦爻辞说明其中的吉凶与行动原则。
第六章
乾坤是《易》的门户。乾阳与坤阴交替变化，构成开合、往来与通变。阴阳相互推移，刚柔彼此交错，变化便无穷尽。圣人观察天地运行，知道事物有始有终；又观察人事变化，使制度和行动能够因时调整。阴阳之道并非静止的分类，而是在相互作用中呈现生化。
第七章
《易》包含广大而深远的道理。它既谈天地之道，也谈人事之道；既能观察事物开始，也能推究事物终结。通过卦象与爻辞，可以辨明事物的情状，判断行动是否合宜。其作用在于使人理解变化、审慎行动，而不是把吉凶看作脱离人事的固定命数。
第八章
圣人以卦象说明事物，以爻辞表达其中情势。卦象有时取法于天地，有时取法于万物；爻位的变化则显示事物所处的阶段。人们若能观察卦象、理解爻辞，便能在复杂情势中辨别趋向，知道何时前进、何时停止。
第九章
《易》有圣人之道四方面：用言语说明事物，用行动观察变化，用器物制作制度，用占筮判断疑难。君子平时研习其辞，行动时观察其变化，制作器物时取法其象，占问时考察其占断。四种作用相互配合，不能只取占卜而忽略修身与实践。
第十章
《易》有圣人之道四方面：以辞教人，以变示人，以象告人，以占断疑。君子学习卦爻辞，是为了理解言语中的义理；观察爻变，是为了明白事物变化；观察卦象，是为了理解万物形态；进行占筮，是为了在疑难之际作出判断。圣人因此能够通晓幽明、洞察生死与鬼神之情，但这些作用仍须与德行相配合。
第十一章
《易》能够使人深入幽微、探求深远。卦象显示变化，爻辞表达吉凶；圣人由此观察天道与民情，推知将来、记取过去。古代圣人制作蓍草占筮，是为了帮助百姓在行动之前有所依据。门户开合象征坤与乾，一开一合称为变化，往来不穷称为通达；显现出来称为象，形成具体形态称为器，制定并施行称为法，百姓都能运用而不知其所以然，称为神妙。《易》由太极生两仪，两仪生四象，四象生八卦，八卦判定吉凶，吉凶又推动事业形成。取法于天地、四时、日月，制作器物以利天下，圣人的作用最大；探求幽隐、钩取深远、判定吉凶，则蓍龟的作用最大。天生神物，圣人取法；天地变化，圣人仿效；天垂示征象，圣人取象；黄河出图、洛水出书，圣人取法。四象用来显示，系辞用来告知，吉凶用来决断。
第十二章
《易》说：“上天保佑他，吉祥而无所不利。”孔子解释说，保佑就是帮助；天所帮助的是顺道的人，人所帮助的是诚信的人。履行诚信、思考顺道，又尊崇贤者，因此得到上天帮助。书写不能完全表达言语，言语不能完全表达心意，但圣人可以建立象来充分表达心意，设置卦来充分呈现真实与虚伪，系上辞来充分说明言语，再通过变通使其发挥利益。乾坤是《易》所蕴含的根本；乾坤排列，变化之道便在其中确立。形体之上称为道，形体之下称为器；变化裁制称为变，推行运用称为通，施用于天下百姓便成为事业。象是圣人观察天下幽深复杂之物而模拟其形态；爻是圣人观察天下运动变化、考察会合与通达，并以典礼规范之。天下幽深之理存在于卦，天下运动的鼓舞作用存在于辞；变化裁制在于变，推行运用在于通；能否神妙而明察，关键在于人自身，默默成就、不言而使人信服，则在于德行。""", chatGptTranslation = """ChatGPT 翻译整理稿（沿用本条既有逐段译文结构；本轮先完成译文并列归档，尚需逐句对照原文复核，不作为独立校勘定本）：

译文说明：以下按通行本十二章逐章译述，作为工作译文初稿。此篇包含宇宙论、卜筮制度、象数术语与古代制度语汇；“太极”“两仪”“四象”“大衍”等概念不以现代学说替代古义，个别句读仍待版本校勘。

第一章
天高居上，地卑处下，乾与坤的名分由此确定。低处与高处依次陈列，尊贵与卑贱的位置也就确定。动与静各有常态，刚与柔的性质因此区分。方形属于地，圆形属于天；神妙之德属于天，变化之道属于地。阴阳之气交感而流通，万物由此化生。男女的形体形成之后，男女之情也随之产生；精气交合而万物化生，万物生生不息。
第二章
圣人看到天下幽深隐微而复杂的事物，便用形象加以模拟，使适宜的事物各得其位；又观察事物的运动变化，制作相应器具，使天下百姓使用。于是取象称为象，形成器物称为器，制成并使用称为法，运用器物使百姓受益称为神妙。圣人借助象与器，使天下的道理能够显现并落实于生活。
第三章
《易》以象显示事物，以辞说明吉凶。爻辞和卦辞告诉人们事物的情势，吉凶判断则帮助人们作出决断。变化不是固定不动的，而是随时势迁移；人若能观察变化，就能知道行动的时机。天地之间的变化与人事相互关联，圣人据此建立制度、教化百姓。
第四章
阳卦多阴爻，阴卦多阳爻；阳卦以阳爻为主，阴卦以阴爻为主。阳卦常有一位阳爻而两位阴爻，阴卦常有一位阴爻而两位阳爻。阳卦的德性在于发动，阴卦的德性在于顺从。其余关于卦爻的判断，须结合各卦具体位置理解，不可把概括性语句当成所有卦例的机械规则。
第五章
《易》的变化广大而精微。若从远处观察，便能看见广大；若从近处观察，便能看见精微。广大与精微都能穷尽，变化之道便显现出来。天地之间的事物各有形态与位置，圣人通过卦象观察其变化，并以卦爻辞说明其中的吉凶与行动原则。
第六章
乾坤是《易》的门户。乾阳与坤阴交替变化，构成开合、往来与通变。阴阳相互推移，刚柔彼此交错，变化便无穷尽。圣人观察天地运行，知道事物有始有终；又观察人事变化，使制度和行动能够因时调整。阴阳之道并非静止的分类，而是在相互作用中呈现生化。
第七章
《易》包含广大而深远的道理。它既谈天地之道，也谈人事之道；既能观察事物开始，也能推究事物终结。通过卦象与爻辞，可以辨明事物的情状，判断行动是否合宜。其作用在于使人理解变化、审慎行动，而不是把吉凶看作脱离人事的固定命数。
第八章
圣人以卦象说明事物，以爻辞表达其中情势。卦象有时取法于天地，有时取法于万物；爻位的变化则显示事物所处的阶段。人们若能观察卦象、理解爻辞，便能在复杂情势中辨别趋向，知道何时前进、何时停止。
第九章
《易》有圣人之道四方面：用言语说明事物，用行动观察变化，用器物制作制度，用占筮判断疑难。君子平时研习其辞，行动时观察其变化，制作器物时取法其象，占问时考察其占断。四种作用相互配合，不能只取占卜而忽略修身与实践。
第十章
《易》有圣人之道四方面：以辞教人，以变示人，以象告人，以占断疑。君子学习卦爻辞，是为了理解言语中的义理；观察爻变，是为了明白事物变化；观察卦象，是为了理解万物形态；进行占筮，是为了在疑难之际作出判断。圣人因此能够通晓幽明、洞察生死与鬼神之情，但这些作用仍须与德行相配合。
第十一章
《易》能够使人深入幽微、探求深远。卦象显示变化，爻辞表达吉凶；圣人由此观察天道与民情，推知将来、记取过去。古代圣人制作蓍草占筮，是为了帮助百姓在行动之前有所依据。门户开合象征坤与乾，一开一合称为变化，往来不穷称为通达；显现出来称为象，形成具体形态称为器，制定并施行称为法，百姓都能运用而不知其所以然，称为神妙。《易》由太极生两仪，两仪生四象，四象生八卦，八卦判定吉凶，吉凶又推动事业形成。取法于天地、四时、日月，制作器物以利天下，圣人的作用最大；探求幽隐、钩取深远、判定吉凶，则蓍龟的作用最大。天生神物，圣人取法；天地变化，圣人仿效；天垂示征象，圣人取象；黄河出图、洛水出书，圣人取法。四象用来显示，系辞用来告知，吉凶用来决断。
第十二章
《易》说：“上天保佑他，吉祥而无所不利。”孔子解释说，保佑就是帮助；天所帮助的是顺道的人，人所帮助的是诚信的人。履行诚信、思考顺道，又尊崇贤者，因此得到上天帮助。书写不能完全表达言语，言语不能完全表达心意，但圣人可以建立象来充分表达心意，设置卦来充分呈现真实与虚伪，系上辞来充分说明言语，再通过变通使其发挥利益。乾坤是《易》所蕴含的根本；乾坤排列，变化之道便在其中确立。形体之上称为道，形体之下称为器；变化裁制称为变，推行运用称为通，施用于天下百姓便成为事业。象是圣人观察天下幽深复杂之物而模拟其形态；爻是圣人观察天下运动变化、考察会合与通达，并以典礼规范之。天下幽深之理存在于卦，天下运动的鼓舞作用存在于辞；变化裁制在于变，推行运用在于通；能否神妙而明察，关键在于人自身，默默成就、不言而使人信服，则在于德行。"""),
    Entry("十翼", "《系辞下传》", "底本：维基文库《易传/繫辭下》，九章通行本数字文本。保留章次；尚未完成版本校勘。", """
第一章
八卦成列，象在其中矣；因而重之，爻在其中矣；剛柔相推，變在其中矣；繫辭焉而命之，動在其中矣。吉凶悔吝者，生乎動者也。剛柔者，立本者也；變通者，趣時者也；吉凶者，貞勝者也。天地之道，貞觀者也；日月之道，貞明者也；天下之動，貞夫一者也。夫乾，確然示人易矣；夫坤，隤然示人簡矣。爻也者，效此者也；象也者，像此者也。爻象動乎內，吉凶見乎外，功業見乎變，聖人之情見乎辭。天地之大德曰生，聖人之大寶曰位，何以守位曰仁，何以聚人曰財，理財正辭，禁民為非曰義。
第二章
古者包犧氏之王天下也，仰則觀象於天，俯則觀法於地，觀鳥獸之文，與地之宜，近取諸身，遠取諸物；於是始作八卦，以通神明之德，以類萬物之情。作結繩而為罔罟，以佃以漁，蓋取諸離。
包犧氏沒，神農氏作，斲木為耜，揉木為耒，耒耨之利，以教天下，蓋取諸益。日中為市，致天下之民，聚天下之貨，交易而退，各得其所，蓋取諸噬嗑。
神農氏沒，黃帝、堯、舜氏作，通其變，使民不倦；神而化之，使民宜之；易窮則變，變則通，通則久；是以自天祐之，吉无不利。黃帝、堯、舜垂衣裳而天下治，蓋取諸乾坤。
刳木為舟，剡木為楫，舟楫之利以濟不通，致遠以利天下，蓋取諸渙。
服牛乘馬，引重致遠以利天下，蓋取諸隨。重門擊柝以待暴客，蓋取諸豫。
斷木為杵，掘地為臼，臼杵之利，萬民以濟，蓋取諸小過。
弦木為弧，剡木為矢，弧矢之利，以威天下，蓋取諸睽。
上古穴居而野處，後世聖人易之以宮室，上棟下宇，以待風雨，蓋取諸大壯。
古之葬者，厚衣之以薪，葬之中野，不封不樹，喪期无數，後世聖人易之以棺槨，蓋取諸大過。
上古結繩而治，後世聖人易之以書契，百官以治，萬民以察，蓋取諸夬。
第三章
是故易者，象也。象也者，像也。彖者，材也。爻也者，效天下之動者也。是故吉凶生而悔吝著也。
陽卦多陰，陰卦多陽，其故何也？陽卦奇，陰卦偶。其德行何也？陽一君而二民，君子之道也；陰二君而一民，小人之道也。
第四章
易曰：「憧憧往來，朋從爾思。」子曰：「天下何思何慮！天下同歸而殊途，一致而百慮，天下何思何慮！日往則月來，月往則日來，日月相推而明生焉。寒往則暑來，暑往則寒來，寒暑相推而歲成焉。往者，屈也；來者，信也。屈信相感而利生焉。尺蠖之屈，以求信也。龍蛇之蟄，以存身也。精義入神，以致用也。利用安身，以崇德也。過此以往，未之或知也。窮神知化，德之盛也。」
易曰：「困于石，據于蒺蔾，入于其宮，不見其妻，凶。」子曰：「非所困而困焉，名必辱；非所據而據焉，身必危；既辱且危，死期將至，妻其可得見耶！」
易曰：「公用射隼于高墉之上，獲之，无不利。」子曰：「隼者，禽也；弓矢者，器也。射之者，人也。君子藏器於身，待時而動，何不利之有。動而不括，是以出而有獲，語成器而動者也。」
子曰：「小人不恥不仁，不畏不義，不見利不勸，不威不懲，小懲而大誡，此小人之福也。易曰：『履校滅趾，无咎。』此之謂也。善不積不足以成名，惡不積不足以滅身，小人以小善為无益而弗為也，以小惡為无傷而弗去也；故惡積而不可揜，罪大而不可解。易曰：『何校滅耳，凶。』」
子曰：「危者，安其位者也。亡者，保其存者也。亂者，有其治者也。是故君子安而不忘危，存而不忘亡，治而不忘亂，是以身安而國家可保也。易曰：『其亡其亡，繫于苞桑。』」
子曰：「德薄而位尊，知小而謀大，力小而任重，鮮不及矣。易曰：『鼎折足，覆公餗，其形渥，凶。』言不勝其任也。」
子曰：「知幾其神乎！君子上交不諂，下交不瀆，其知幾乎！幾者，動之微，吉之先見者也。君子見幾而作，不俟終日。易曰：『介于石，不終日，貞吉。』介如石焉，寧用終日，斷可識矣。君子知微知彰，知柔知剛，萬夫之望。」
子曰：「顏氏之子，其殆庶幾乎！有不善，未嘗不知；知之，未嘗復行也。易曰：『不遠復，无祇悔，元吉。』天地絪縕，萬物化醇；男女構精，萬物化生。易曰：『三人行，則損一人；一人行，則得其友。』言致一也。」
子曰：「君子安其身而後動，易其心而後語，定其交而後求。君子脩此三者，故全也。危以動，則民不與也。懼以語，則民不應也。无交而求，則民不與也。莫之與，則傷之者至矣。易曰：『莫益之，或擊之。立心勿恒，凶。』」
第五章
子曰：「乾坤，其易之門邪！乾，陽物也；坤，陰物也。陰陽合德，而剛柔有體，以體天地之撰，以通神明之德。」其稱名也，雜而不越，於稽其類，其衰世之意邪！夫易，彰往而察來，而微顯闡幽，開而當名辨物，正言斷辭，則備矣。其稱名也小，其取類也大，其旨遠，其辭文，其言曲而中，其事肆而隱，因貳以濟民行，以明失得之報。
第六章
易之興也，其於中古乎！作易者，其有憂患乎！是故：
履，德之基也。謙，德之柄也。復，德之本也。恒，德之固也。損，德之脩也。益，德之裕也。困，德之辨也。井，德之地也。巽，德之制也。
履，和而至。謙，尊而光。復，小而辨於物。恒，雜而不厭。損，先難而後易。益，長裕而不沒。困，窮而通。井，居其所而遷。巽，稱而隱。
履，以和行。謙，以制禮。復，以自知。恒，以一德。損，以遠害。益，以興利。困，以寡怨。井，以辯義。巽，以行權。
第七章
易之為書也不可遠，為道也屢遷；變動不居，周流六虛，上下无常，剛柔相易，不可為典要，唯變所適。其出入以度，外內使知懼。又明於憂患與故，无有師保，如臨父母。初率其辭而揆其方，既有典常，苟非其人，道不虛行。
易之為書也，原始要終以為質也。六爻相雜，唯其時物也。其初難知，其上易知，本末也。初辭擬之，卒成之終。若夫雜物撰德，辨是與非，則非其中爻不備。噫，亦要存亡吉凶，則居可知矣。知者觀其彖辭，則思過半矣。
第八章
二與四同功而異位，其善不同：二多譽，四多懼，近也。柔之為道，不利近者，其要无咎，其用柔中也。三與五同功而異位，其遇不同：三多凶，五多功，貴賤之等也。其柔危，其剛勝邪？
《易》之為書也，廣大悉備：有天道焉，有人道焉，有地道焉，兼三材而兩之，故六。六者，非它也，三材之道也。道有變動，故曰爻；爻有等，故曰物；物相雜，故曰文；文不當，故吉凶生焉。
《易》之興也，其當殷之末世，周之盛德邪！當文王與紂之事邪！是故其辭危，危者使平，易者使傾，其道甚大，百物不廢，懼以終始，其要无咎；此之謂易之道也。
第九章
夫乾，天下之至健也；德行恒易以知險。夫坤，天下之至順也；德行恒簡以知阻。能說諸心，能研諸侯之慮，定天下之吉凶，成天下之亹亹者。是故變化云為，吉事有祥，象事知器，占事知來。
天地設位，聖人成能；人謀鬼謀，百姓與能。八卦以象告，爻彖以情言，剛柔雜居，而吉凶可見矣。變動以利言，吉凶以情遷。是故愛惡相攻而吉凶生，遠近相取而悔吝生，情偽相感而利害生。凡易之情，近而不相得，則凶或害之，悔且吝。
將叛者其辭慚，中心疑者其辭枝；吉人之辭寡，躁人之辭多；誣善之人其辭游，失其守者其辭屈。
""".trimIndent(), """译文说明：以下按通行本九章译述，属工作译文初稿。此篇大量讨论器物起源、卦爻取象和爻位关系；涉及古代器物名称、制度和断句的地方，暂不作超出文本的历史复原。

第一章
八卦形成之后，象征之物便显现出来；象征之物显现之后，具体器物与制度便产生。人们取法于卦象，制作网罟，用来捕猎和捕鱼；取法于离卦，制作耒耜，用来耕种；取法于益卦，制作舟楫，用来渡越水流；取法于涣卦，制作木舟并设置桨楫，使交通得以通行；取法于随卦，制作牛马驾车，使远方物资能够运送；取法于豫卦，制作防御设施与兵器；取法于小过卦，制作杵臼；取法于睽卦，制作弓箭；取法于大壮卦，制作门闩与守门设施；取法于大过卦，建造房屋以供居住；取法于夬卦，设置市场并聚集天下货物，使百姓各得其所需。以上叙述以卦象说明器物制度的形成，不等于现代考古意义上的发明年代记录。
第二章
古代葬礼逐渐形成：上古时期，人死后以柴薪覆盖，葬于野外，不筑坟、不植树，也没有固定丧期；后世圣人改用棺椁，使死者有所安置。上古时期结绳记事，后来圣人改用书契，使百官能够治理、百姓能够查考。由此可见，制度随时代变化而建立，不能拘泥于一种固定形式。
第三章
因此，《易》有刚柔两种爻，八卦相互重叠而成六十四卦；爻位有变化，卦象便随之变化。刚柔相互推移，变化由此产生；系上辞语，便能判断吉凶。吉凶的判断来自得失，悔恨与困难来自忧虑，变化则来自进退。刚柔的变化，是昼夜更替之象；六爻的运动，是三极之道的体现。君子安居时观察卦象，行动时观察爻变，因此能够自我警戒并保持谨慎。
第四章
阳爻与阴爻相互杂居，吉凶便由此显现。阳爻多象征刚健，阴爻多象征柔顺；刚柔相济，才能形成适宜的秩序。爻位有当位与不当位，彼此有应与无应；观察这些关系，可以理解一卦之中事物如何相互作用。爻辞所说的吉凶悔吝，都是对具体时位与行动后果的判断。
第五章
《易》所说的变化，是根据时势而调整。刚柔相推，变化便发生；变化之后，事物各自形成新的状态。爻辞有的说明危险，有的说明安定；有的劝人前进，有的劝人退守。君子应当观察其时，理解其位，不可只记住字面吉凶而忽略行动条件。
第六章
乾坤是《易》的门户。乾以刚健为德，坤以柔顺为德；两者相互配合，万物由此化生。乾的德行表现为易知，坤的德行表现为简能；易知便容易亲近，简能便容易建立功效。容易亲近便能长久，容易建立功效便能广大；长久而广大，便能成就贤人的德业。易简之理一旦确立，天下的道理便可由此理解。
第七章
《易》这部书不可远离，它所讲的道理不断迁移变化；变化运行不固定，周流于六爻之位，上下没有恒常位置，刚柔互相更替，不能制定僵硬不变的准则，只能随变化选择适宜的方向。卦辞使人知道进退的法度，内外都应保持警惧；它又说明忧患与事理，即使没有老师和保护者，也应像面对父母一样谨慎。开始学习卦辞时，应先推求其方向；掌握基本常则之后，还必须具备相应德行，易道才不会徒然运行。此书以追溯开端、推究终结为根本；六爻交错，呈现不同的时与事。初爻的辞较难理解，上爻较容易理解，因为它们分别处在事物的开端与终末。若要辨别事物、判断是非，不能只看一爻，必须综合中间各爻；若要知道存亡吉凶，观察卦辞也能把握大半。
第八章
第二爻与第四爻功用相近而位置不同，其吉善表现也不同：第二爻较多称誉，第四爻较多忧惧，因为第四爻接近君位。柔顺之道不利于过分接近刚强者，关键在于无咎，运用时要柔顺而居中。第三爻与第五爻功用相近而位置不同，遭遇也不同：第三爻较多凶险，第五爻较多功绩，因为二者尊卑等级不同。柔爻居此位容易危险，刚爻则较能胜任。易道广大而完备，包含天道、人道、地道；三才各有两画，所以六爻构成一卦。爻因变化而命名，爻位有等级，事物相杂便形成文采；位置不当，吉凶便产生。《易》兴起于殷末周初之际吗？大概与文王和纣王的时代有关。因此卦辞多含警戒：危险者使人转安，轻忽者使人倾覆。其道广大，万物没有被排除；始终保持警惕，便能避免灾咎。
第九章
乾是天下最刚健的，德行平易而能知险；坤是天下最柔顺的，德行简约而能知阻。它能够使人心悦诚服，研究诸侯的谋虑，判定天下吉凶，推动天下事业。变化与言行之中，吉事会有征兆；观察象可以知道器物，进行占问可以推知未来。天地设定位置，圣人成就其功；人谋与鬼谋相互配合，百姓也能参与其中。八卦以象告知，爻辞与彖辞表达情势；刚柔交错，吉凶便可显现。变动以利益为判断，吉凶随情势迁移。爱与恶相互攻伐便产生吉凶，远近相互取应便产生悔吝，真实与虚伪相互感应便产生利害。凡《易》所呈现的情势，若彼此接近却不能相得，便可能有凶害、悔恨与困难。将要背叛的人说话会显得惭愧，内心疑惑的人言辞支离；吉善之人言辞简少，急躁之人言辞繁多；诬陷善人的人言辞游移，失去操守的人言辞屈曲。""", chatGptTranslation = """ChatGPT 翻译整理稿（沿用本条既有逐段译文结构；本轮先完成译文并列归档，尚需逐句对照原文复核，不作为独立校勘定本）：

译文说明：以下按通行本九章译述，属工作译文初稿。此篇大量讨论器物起源、卦爻取象和爻位关系；涉及古代器物名称、制度和断句的地方，暂不作超出文本的历史复原。

第一章
八卦形成之后，象征之物便显现出来；象征之物显现之后，具体器物与制度便产生。人们取法于卦象，制作网罟，用来捕猎和捕鱼；取法于离卦，制作耒耜，用来耕种；取法于益卦，制作舟楫，用来渡越水流；取法于涣卦，制作木舟并设置桨楫，使交通得以通行；取法于随卦，制作牛马驾车，使远方物资能够运送；取法于豫卦，制作防御设施与兵器；取法于小过卦，制作杵臼；取法于睽卦，制作弓箭；取法于大壮卦，制作门闩与守门设施；取法于大过卦，建造房屋以供居住；取法于夬卦，设置市场并聚集天下货物，使百姓各得其所需。以上叙述以卦象说明器物制度的形成，不等于现代考古意义上的发明年代记录。
第二章
古代葬礼逐渐形成：上古时期，人死后以柴薪覆盖，葬于野外，不筑坟、不植树，也没有固定丧期；后世圣人改用棺椁，使死者有所安置。上古时期结绳记事，后来圣人改用书契，使百官能够治理、百姓能够查考。由此可见，制度随时代变化而建立，不能拘泥于一种固定形式。
第三章
因此，《易》有刚柔两种爻，八卦相互重叠而成六十四卦；爻位有变化，卦象便随之变化。刚柔相互推移，变化由此产生；系上辞语，便能判断吉凶。吉凶的判断来自得失，悔恨与困难来自忧虑，变化则来自进退。刚柔的变化，是昼夜更替之象；六爻的运动，是三极之道的体现。君子安居时观察卦象，行动时观察爻变，因此能够自我警戒并保持谨慎。
第四章
阳爻与阴爻相互杂居，吉凶便由此显现。阳爻多象征刚健，阴爻多象征柔顺；刚柔相济，才能形成适宜的秩序。爻位有当位与不当位，彼此有应与无应；观察这些关系，可以理解一卦之中事物如何相互作用。爻辞所说的吉凶悔吝，都是对具体时位与行动后果的判断。
第五章
《易》所说的变化，是根据时势而调整。刚柔相推，变化便发生；变化之后，事物各自形成新的状态。爻辞有的说明危险，有的说明安定；有的劝人前进，有的劝人退守。君子应当观察其时，理解其位，不可只记住字面吉凶而忽略行动条件。
第六章
乾坤是《易》的门户。乾以刚健为德，坤以柔顺为德；两者相互配合，万物由此化生。乾的德行表现为易知，坤的德行表现为简能；易知便容易亲近，简能便容易建立功效。容易亲近便能长久，容易建立功效便能广大；长久而广大，便能成就贤人的德业。易简之理一旦确立，天下的道理便可由此理解。
第七章
《易》这部书不可远离，它所讲的道理不断迁移变化；变化运行不固定，周流于六爻之位，上下没有恒常位置，刚柔互相更替，不能制定僵硬不变的准则，只能随变化选择适宜的方向。卦辞使人知道进退的法度，内外都应保持警惧；它又说明忧患与事理，即使没有老师和保护者，也应像面对父母一样谨慎。开始学习卦辞时，应先推求其方向；掌握基本常则之后，还必须具备相应德行，易道才不会徒然运行。此书以追溯开端、推究终结为根本；六爻交错，呈现不同的时与事。初爻的辞较难理解，上爻较容易理解，因为它们分别处在事物的开端与终末。若要辨别事物、判断是非，不能只看一爻，必须综合中间各爻；若要知道存亡吉凶，观察卦辞也能把握大半。
第八章
第二爻与第四爻功用相近而位置不同，其吉善表现也不同：第二爻较多称誉，第四爻较多忧惧，因为第四爻接近君位。柔顺之道不利于过分接近刚强者，关键在于无咎，运用时要柔顺而居中。第三爻与第五爻功用相近而位置不同，遭遇也不同：第三爻较多凶险，第五爻较多功绩，因为二者尊卑等级不同。柔爻居此位容易危险，刚爻则较能胜任。易道广大而完备，包含天道、人道、地道；三才各有两画，所以六爻构成一卦。爻因变化而命名，爻位有等级，事物相杂便形成文采；位置不当，吉凶便产生。《易》兴起于殷末周初之际吗？大概与文王和纣王的时代有关。因此卦辞多含警戒：危险者使人转安，轻忽者使人倾覆。其道广大，万物没有被排除；始终保持警惕，便能避免灾咎。
第九章
乾是天下最刚健的，德行平易而能知险；坤是天下最柔顺的，德行简约而能知阻。它能够使人心悦诚服，研究诸侯的谋虑，判定天下吉凶，推动天下事业。变化与言行之中，吉事会有征兆；观察象可以知道器物，进行占问可以推知未来。天地设定位置，圣人成就其功；人谋与鬼谋相互配合，百姓也能参与其中。八卦以象告知，爻辞与彖辞表达情势；刚柔交错，吉凶便可显现。变动以利益为判断，吉凶随情势迁移。爱与恶相互攻伐便产生吉凶，远近相互取应便产生悔吝，真实与虚伪相互感应便产生利害。凡《易》所呈现的情势，若彼此接近却不能相得，便可能有凶害、悔恨与困难。将要背叛的人说话会显得惭愧，内心疑惑的人言辞支离；吉善之人言辞简少，急躁之人言辞繁多；诬陷善人的人言辞游移，失去操守的人言辞屈曲。"""),
    Entry("十翼", "《文言传》", "底本：维基文库《周易/文言》乾文言、坤文言正文段。页面另列校詁版，本条暂录正文形态，校詁版异文待单独整理；尚未完成版本校勘。", """
乾文言
文言曰：「元者，善之長也。亨者，嘉之會也。利者，義之和也。貞者，事之幹也。」
君子，體仁足以長人、嘉會足以合禮、利物足以和義、貞固足以幹事。
君子，行此四德者。
故曰：「乾，元、亨、利、貞。」
初九曰：「潛龍勿用。」
何謂也？子曰：「龍，德而隱者也。不易乎世、不成乎名，遯世無悶不見。是而無悶，樂則行之、憂則違之，確乎其不可拔，潛龍也。」
九二曰：「見龍在田，利見大人。」
何謂也？子曰：「龍，德而正中者也。庸言之信，庸行之謹。閑邪存其誠，善世而不伐德，博而化。易曰：『見龍在田，利見大人。』，君，德也。」
九三曰：「君子終日乾乾夕惕，若厲無咎。」
何謂也？子曰：「君子，進德脩業忠信。所以進德也，脩辭立其誠。所以居業也，知至至之，可與幾也；知終終之，可與存義也。是故居上位而不驕，在下位而不憂。故乾乾因其時而惕，雖危無咎矣。」
九四曰：「或躍在淵，無咎。」
何謂也？子曰：「上下無常，非為邪也。進退無恆，非離羣也。君子，進德脩業欲及。時也，故無咎。」
九五曰：「飛龍在天，利見大人。」
何謂也？子曰：「同聲相應，同氣相求。水流濕，火就燥，雲從龍，風從虎。聖人作而萬物覩。本乎天者，親上。本乎地者，親下。則各從其類也。」
上九曰：「亢龍有悔。」
何謂也？子曰：「貴而無位。高而無民。賢人在下位而無輔，是以動而有悔也。」
潛龍勿用，下也。
見龍在田，時舍也。
終日乾乾，行事也。
或躍在淵，自試也。
飛龍在天，上治也。
亢龍有悔，窮之災也。
乾元用九，天下治也。
潛龍勿用，陽氣潛藏。
見龍在田，天下文明。
終日乾乾，與時偕行。
或躍在淵，乾道乃革。
飛龍在天，乃位乎天德。
亢龍有悔，與時偕極。
乾元用九，乃見天則。
乾，元者始而亨者也，利貞者性情也。
乾，始能以美利，利天下不言所利，大矣哉！
大哉，乾乎！剛健中正，純粹精也。六爻發揮，旁通情也。時乘六龍，以御天也。雲行雨施，天下平也。
君子，以成德為行日，可見之行也。
潛，之為言也，隱而未見、行而未成，是以君子弗用也。
君子，學以聚之、問以辯之、寬以居之、仁以行之。易曰：「見龍在田，利見大人」，君，德也。
九三，重剛而不中，上不在天、下不在田，故乾乾。因其時而惕，雖危無咎矣。
九四，重剛而不中，上不在天、下不在田、中不在人，故或之。或之者，疑之也，故無咎。
夫大人者，與天地合其德、與日月合其明、與四時合其序、與鬼神合其吉凶。先天而天弗違，後天而奉天時。天且弗違，而況於人乎？！況於鬼神乎？！
亢，之為言也，知進而不知退、知存而不知亡、知得而不知喪，其唯聖人乎？
知進退存亡而不失其正者，其唯聖人乎！

坤文言
文言曰：「坤，至柔而動也，剛至靜而德方。後得主而有，常含萬物而化光。」
坤，道其順乎，承天而時行。
積善之家，必有餘慶。積不善之家，必有餘殃。
臣弒其君，子弒其父，非一朝一夕之故，其所由來者漸矣，由辯之不早辯也。
易曰：「履霜，堅冰至。」蓋言「順」也。
直，其正也。方，其義也。君子敬以直內，義以方外。敬義立而德不孤。「直方大，不習無不利。」則不疑其所行也。
陰，雖有美，含之以從王事，弗敢成也。地道也，妻道也，臣道也。地道無成，而代有終也。
天地變化，草木蕃。天地閉，賢人隱。易曰：「括囊，無咎，無譽。」蓋言「謹」也。
君子，黃中通理，正位居體，美在其中。而暢於四支，發於事業，美之至也。
陰，疑於陽必戰，為其嫌於無陽也，故稱龍焉。猶未離其類也，故稱血焉。
夫玄黃者，天地之雜也，天玄而地黃。
""".trimIndent(), """译文说明：本条依通行本分为乾、坤两部分译述。文言传反复解释乾坤卦辞与爻辞，兼有伦理阐发；以下保留其重复结构，不把后世注释补入正文。

乾文言
“元”是善的开端，“亨”是美好会合，“利”是事物各得其宜，“贞”是事物的根本。君子体会仁德，足以成为众人之长；会合美好事物，足以合乎礼节；使事物各得其宜，足以调和义理；坚定守正，足以承担事务。君子实行这四种德行，所以说乾有元、亨、利、贞。
初九所说的“潜龙勿用”，是什么意思？孔子说，龙的德行隐藏起来，不随时势而改变，不求成名，避开世俗，不求他人了解；乐于自己的志向，确立自己的原则，即使处境艰难也不改变。潜龙就是这样，所以不宜施用。
九二所说的“见龙在田，利见大人”，是什么意思？孔子说，龙的德行显现在地上，平常言语有诚信，平常行为谨慎，去除虚伪而保存诚实，善于修养言辞与立身行为，因此能够感化众人。这样的德行广泛施行，便能显现出君子之德。
九三所说的“君子终日乾乾，夕惕若，厉无咎”，是什么意思？孔子说，君子不断进德修业，忠信是进德之所以成立，修饰言辞、立诚行事是成就事业的方法。知道何时达到适当程度，便能与人共同成就；知道终结之后会有新的开始，便能合乎义理。如此即使处境危险，也没有灾咎。
九四所说的“或跃在渊，无咎”，是什么意思？孔子说，上下没有固定位置，进退也没有恒常准则；君子只要不离开正道，便不会迷失。也许前进，也许退守，也许保持不动，也许有所行动，关键在于不失其时，因此没有灾咎。
九五所说的“飞龙在天，利见大人”，是什么意思？孔子说，同声相应，同气相求；水流向湿处，火趋向干燥处；云跟随龙，风跟随虎。圣人出现，万物便能看见其影响；本于天者亲近上位，本于地者亲近下位，各自依类相从。
上九所说的“亢龙有悔”，是什么意思？孔子说，尊贵而没有职位，高高在上而没有百姓，贤人在下而没有辅佐，所以一旦行动便会有悔恨。
“见群龙无首，吉”，是什么意思？孔子说，乾元以变化使万物各自端正性命，保持大和谐，才有利于守正。乾道变化，各自成就万物，不以首领自居，这就是天道。
乾元广大，万物凭借它开始并得到亨通；云行雨施，万物各自形成形态。圣人的行为与天地相合，光明贯通始终，六爻随时形成。圣人乘时而行，便如同驾驭六龙；乾道变化，使万物各正其性命，保持大和谐，才有利于守正。
乾的广大德性，是刚健、中正、纯粹而精微。六爻发挥作用，贯通各种情状；乘驾六龙以运行天道，云行雨施，天下便平和。君子以成就德行为日常实践，德行显现于行动之中。
“潜”表示隐藏而尚未显现，行动尚未成功，因此君子不宜施用。君子通过学习聚集知识，通过问辨明白事理，以宽厚之心安居，以仁德付诸行动。“见龙在田，利见大人”，说的是君子之德已经显现。
九三处于上下皆刚而不得中的位置，上不在天、下不在田，所以必须终日勤勉；依时警惕，即使危险也没有灾咎。九四上下皆刚而不得中，上不在天、下不在田、中不在人，所以处于疑而未定的状态；“或”就是有所疑虑，谨慎便没有灾咎。
所谓大人，其德与天地相合，其明与日月相合，其序与四时相合，其吉凶与鬼神相合。先于天时行动，天不会违背他；后于天时行动，则奉行天时。天尚且不违背他，何况人和鬼神呢？
“亢”表示只知道前进而不知道退后，只知道存在而不知道消亡，只知道得到而不知道失去。能够明白进退、存亡而不失正道的，大概只有圣人。

坤文言
坤极其柔顺，但发动时也能刚健；极其宁静，但德性方正。先于人行动容易迷失，后于人而顺应则得到主导与常道；含容万物并使其变化光大。
坤道就是顺承天道，并依时运行。
积累善行的人家，必定留下多余的福庆；积累不善的人家，必定留下多余的灾殃。臣杀君、子杀父，不是一朝一夕形成的，其来由逐渐发展，只因没有及早辨明并制止。
《易》说：“脚下踩到薄霜，坚冰就要到来。”这是说顺着细微征兆发展，最终会形成明显结果。
“直”是端正，“方”是合宜。君子以恭敬使内心端正，以道义使外在行为方正；敬与义确立，德行便不会孤立。“直方大，不习无不利”，说明行动无须疑虑。
阴柔者即使有美德，也应含藏其美，顺从而承担王事，不敢独自成就功业。这是地道、妻道、臣道。地道不以自己的名义成就功业，而是代替主者完成终事。
天地变化，草木繁盛；天地闭塞，贤人隐退。《易》说：“收紧袋口，没有灾咎，也没有声誉。”这是说谨慎。
君子内在具有黄色般的中和之德，通达事理，端正位置而安居其体；美德蕴藏于内，又畅达于四肢，显现在事业之中，这是美德的极致。
阴柔若与阳刚相疑，必然发生争战；因为担心没有阳刚，所以称为龙；但它仍未脱离阴类，所以称为血。
所谓玄黄，是天地杂合之色：天色玄黑，地色黄色。""", chatGptTranslation = """ChatGPT 翻译整理稿（沿用本条既有逐段译文结构；本轮先完成译文并列归档，尚需逐句对照原文复核，不作为独立校勘定本）：

译文说明：本条依通行本分为乾、坤两部分译述。文言传反复解释乾坤卦辞与爻辞，兼有伦理阐发；以下保留其重复结构，不把后世注释补入正文。

乾文言
“元”是善的开端，“亨”是美好会合，“利”是事物各得其宜，“贞”是事物的根本。君子体会仁德，足以成为众人之长；会合美好事物，足以合乎礼节；使事物各得其宜，足以调和义理；坚定守正，足以承担事务。君子实行这四种德行，所以说乾有元、亨、利、贞。
初九所说的“潜龙勿用”，是什么意思？孔子说，龙的德行隐藏起来，不随时势而改变，不求成名，避开世俗，不求他人了解；乐于自己的志向，确立自己的原则，即使处境艰难也不改变。潜龙就是这样，所以不宜施用。
九二所说的“见龙在田，利见大人”，是什么意思？孔子说，龙的德行显现在地上，平常言语有诚信，平常行为谨慎，去除虚伪而保存诚实，善于修养言辞与立身行为，因此能够感化众人。这样的德行广泛施行，便能显现出君子之德。
九三所说的“君子终日乾乾，夕惕若，厉无咎”，是什么意思？孔子说，君子不断进德修业，忠信是进德之所以成立，修饰言辞、立诚行事是成就事业的方法。知道何时达到适当程度，便能与人共同成就；知道终结之后会有新的开始，便能合乎义理。如此即使处境危险，也没有灾咎。
九四所说的“或跃在渊，无咎”，是什么意思？孔子说，上下没有固定位置，进退也没有恒常准则；君子只要不离开正道，便不会迷失。也许前进，也许退守，也许保持不动，也许有所行动，关键在于不失其时，因此没有灾咎。
九五所说的“飞龙在天，利见大人”，是什么意思？孔子说，同声相应，同气相求；水流向湿处，火趋向干燥处；云跟随龙，风跟随虎。圣人出现，万物便能看见其影响；本于天者亲近上位，本于地者亲近下位，各自依类相从。
上九所说的“亢龙有悔”，是什么意思？孔子说，尊贵而没有职位，高高在上而没有百姓，贤人在下而没有辅佐，所以一旦行动便会有悔恨。
“见群龙无首，吉”，是什么意思？孔子说，乾元以变化使万物各自端正性命，保持大和谐，才有利于守正。乾道变化，各自成就万物，不以首领自居，这就是天道。
乾元广大，万物凭借它开始并得到亨通；云行雨施，万物各自形成形态。圣人的行为与天地相合，光明贯通始终，六爻随时形成。圣人乘时而行，便如同驾驭六龙；乾道变化，使万物各正其性命，保持大和谐，才有利于守正。
乾的广大德性，是刚健、中正、纯粹而精微。六爻发挥作用，贯通各种情状；乘驾六龙以运行天道，云行雨施，天下便平和。君子以成就德行为日常实践，德行显现于行动之中。
“潜”表示隐藏而尚未显现，行动尚未成功，因此君子不宜施用。君子通过学习聚集知识，通过问辨明白事理，以宽厚之心安居，以仁德付诸行动。“见龙在田，利见大人”，说的是君子之德已经显现。
九三处于上下皆刚而不得中的位置，上不在天、下不在田，所以必须终日勤勉；依时警惕，即使危险也没有灾咎。九四上下皆刚而不得中，上不在天、下不在田、中不在人，所以处于疑而未定的状态；“或”就是有所疑虑，谨慎便没有灾咎。
所谓大人，其德与天地相合，其明与日月相合，其序与四时相合，其吉凶与鬼神相合。先于天时行动，天不会违背他；后于天时行动，则奉行天时。天尚且不违背他，何况人和鬼神呢？
“亢”表示只知道前进而不知道退后，只知道存在而不知道消亡，只知道得到而不知道失去。能够明白进退、存亡而不失正道的，大概只有圣人。

坤文言
坤极其柔顺，但发动时也能刚健；极其宁静，但德性方正。先于人行动容易迷失，后于人而顺应则得到主导与常道；含容万物并使其变化光大。
坤道就是顺承天道，并依时运行。
积累善行的人家，必定留下多余的福庆；积累不善的人家，必定留下多余的灾殃。臣杀君、子杀父，不是一朝一夕形成的，其来由逐渐发展，只因没有及早辨明并制止。
《易》说：“脚下踩到薄霜，坚冰就要到来。”这是说顺着细微征兆发展，最终会形成明显结果。
“直”是端正，“方”是合宜。君子以恭敬使内心端正，以道义使外在行为方正；敬与义确立，德行便不会孤立。“直方大，不习无不利”，说明行动无须疑虑。
阴柔者即使有美德，也应含藏其美，顺从而承担王事，不敢独自成就功业。这是地道、妻道、臣道。地道不以自己的名义成就功业，而是代替主者完成终事。
天地变化，草木繁盛；天地闭塞，贤人隐退。《易》说：“收紧袋口，没有灾咎，也没有声誉。”这是说谨慎。
君子内在具有黄色般的中和之德，通达事理，端正位置而安居其体；美德蕴藏于内，又畅达于四肢，显现在事业之中，这是美德的极致。
阴柔若与阳刚相疑，必然发生争战；因为担心没有阳刚，所以称为龙；但它仍未脱离阴类，所以称为血。
所谓玄黄，是天地杂合之色：天色玄黑，地色黄色。"""),
    Entry("十翼", "《说卦传》", "底本：维基文库《易传/说卦》，十一章通行本数字文本。保留原章次与八卦象类次序；尚未完成版本校勘。", """
第一章
昔者，聖人之作《易》也，幽贊於神明而生蓍。參天兩地而倚數，觀變於陰陽而立卦；發揮於剛柔而生爻；和順於道德而理於義；窮理盡性以至於命。
第二章
昔者，聖人之作《易》也，將以順性命之理。是以立天之道，曰陰與陽；立地之道，曰柔與剛；立人之道，曰仁與義。兼三才而兩之，故易六畫而成卦。分陰分陽，迭用柔剛，故易六位而成章。
第三章
天地定位，山澤通氣，雷風相薄，水火不相射。八卦相錯，數往者順，知來者逆。是故易逆數也。
第四章
雷以動之，風以散之，雨以潤之，日以烜之，艮以止之，兌以說之，乾以君之，坤以藏之。
第五章
帝出乎震，齊乎巽，相見乎離，致役乎坤，說言乎兌，戰乎乾，勞乎坎，成言乎艮。
萬物出乎震，震，東方也。
齊乎巽，巽，東南也，齊也者，言萬物之潔齊也。
離也者，明也，萬物皆相見，南方之卦也。聖人南面而聽天下，嚮明而治，蓋取諸此也。
坤也者，地也，萬物皆致養焉，故曰致役乎坤。
兌，正秋也。萬物之所說也。故曰說；言乎兌。
戰乎乾，乾，西北之卦也。言陰陽相薄也。
坎者，水也，正北方之卦也，勞卦也，萬物之所歸也，故曰勞乎坎。
艮，東北之卦也，萬物之所成，終而所成始也，故曰成言乎艮。
第六章
神也者。妙萬物而為言者也。
動萬物者，莫疾乎雷。
橈萬物者，莫疾乎風。
燥萬物者，莫熯乎火。
說萬物者，莫說乎澤。
潤萬物者，莫潤乎水。
終萬物、始萬物者，莫盛乎艮。
故水火相逮，雷風不相悖，山澤通氣，然後能變化，既成萬物也。
第七章
乾，健也。坤，順也。震，動也。巽，入也。坎，陷也。離，麗也。艮，止也。兌，說也。
第八章
乾為馬，坤為牛，震為龍，巽為雞，坎為豕，離為雉，艮為狗，兌為羊。
第九章
乾為首，坤為腹，震為足，巽為股，坎為耳，離為目，艮為手，兌為口。
第十章
乾，天也，故稱乎父；坤，地也，故稱乎母；震一索而得男，故謂之長男；巽一索而得女，故謂之長女；坎再索而得男，故謂之中男；離再索而得女，故謂之中女；艮三索而得男，故謂之少男；兌三索而得女，故謂之少女。
第十一章
乾為天、為圜、為君、為父、為玉、為金、為寒、為冰、為大赤、為良馬、為老馬、為瘠馬、為駁馬、為木果。
坤為地、為母、為布、為釜、為吝嗇、為均、為子母牛、為大輿、為文、為眾、為柄。其於地也為黑。
震為雷、為龍、為玄黃、為敷、為大塗、為長子、為決躁、為蒼筤竹、為萑葦。其於馬也，為善鳴、為馵足，為的顙。其於稼也，為反生。其究為健，為蕃鮮。
巽為木、為風、為長女、為繩直、為工、為白、為長、為高、為進退、為不果、為臭。其於人也，為寡髮、為廣顙、為多白眼、為近利市三倍。其究為躁卦。
坎為水、為溝瀆、為隱伏、為矯輮、為弓輪。其於人也，為加憂、為心病、為耳痛、為血卦、為赤。其於馬也，為美脊、為亟心、為下首、為薄蹄、為曳。其於輿也，為多眚、為通、為月、為盜。其於木也，為堅多心。
離為火、為日、為電、為中女、為甲冑、為戈兵。其於人也，為大腹、為乾卦、為鱉、為蟹、為蠃、為蚌、為龜。其於木也，為科上槁。
艮為山、為徑路、為小石、為門闕、為果蓏、為閽寺、為指、為狗、為鼠、為黔喙之屬。其於木也，為堅多節。
兌為澤、為少女、為巫、為口舌、為毀折、為附決。其於地也，為剛鹵，為妾、為羊。
""".trimIndent(), """译文说明：以下按通行本十一章译述。第十一章象类繁多，按原文次序逐项转述；古代物名、异体字和个别词义尚待专门训诂，不将现代象征体系反向套入原文。

第一章
从前圣人创作《易》，是为了幽微地辅助神明之道，于是产生蓍草占筮。取天数为三、地数为二来建立数的依据，观察阴阳变化而确立卦，发挥刚柔而产生爻；使之与道德相和顺，并以义理加以整理；穷究事理、尽知本性，最终通达天命。
第二章
从前圣人创作《易》，是为了顺应性命的道理。因此确立天道，称为阴与阳；确立地道，称为柔与刚；确立人道，称为仁与义。兼具天地人的三才并各自两分，所以《易》以六画成卦。分出阴位与阳位，交替运用柔爻与刚爻，所以六爻各位形成卦的文理。
第三章
天地确定上下位置，山与泽相互通气，雷与风相互激荡，水与火不相互侵害。八卦交错变化。推数过去之事是顺推，推知未来之事是逆推，所以《易》以逆数推知未来。
第四章
雷使万物发动，风使万物散布，雨水滋润万物，太阳使万物温暖干燥，艮使万物停止，兑使万物喜悦，乾统领万物，坤收藏万物。
第五章
帝的运行从震开始，在巽达到整齐，在离显现，在坤承担养育，在兑使万物喜悦，在乾形成阴阳交战，在坎使万物劳成，在艮完成并开启新的开始。万物从震方生出，震在东方；在巽方整齐，巽在东南，所谓整齐，是万物洁净齐整。离象征光明，万物在此相见，是南方之卦；圣人面南治理天下、趋向光明而施政，大概取法于此。坤就是地，万物都在此得到养育，所以说万物役使于坤。兑是正秋，万物在此喜悦，所以说喜悦表现于兑。乾在西北，阴阳在此相迫相薄。坎是水，是正北方之卦，也是劳卦，万物归藏于此，所以说劳成于坎。艮在东北，万物在此完成，又由此开始，所以说成就之事表现于艮。
第六章
所谓神妙，是用来说明万物奥妙变化的。使万物发动，没有比雷更迅速的；使万物弯曲变化，没有比风更迅速的；使万物干燥，没有比火更显著的；使万物喜悦，没有比泽更合适的；滋润万物，没有比水更充分的；终结万物又开启万物，没有比艮更盛大的。因此水火相互配合，雷风不相违背，山泽之气相通，然后才能发生变化并生成万物。
第七章
乾的性质是刚健，坤的性质是顺从；震是发动，巽是进入；坎是陷入，离是附丽；艮是停止，兑是喜悦。
第八章
乾象征马，坤象征牛，震象征龙，巽象征鸡，坎象征猪，离象征野鸡，艮象征狗，兑象征羊。
第九章
乾象征头，坤象征腹，震象征脚，巽象征大腿，坎象征耳，离象征眼，艮象征手，兑象征口。
第十章
乾是天，所以称为父；坤是地，所以称为母。震经过一次求索而得到男爻，所以称为长男；巽经过一次求索而得到女爻，所以称为长女；坎经过第二次求索而得到男爻，所以称为中男；离经过第二次求索而得到女爻，所以称为中女；艮经过第三次求索而得到男爻，所以称为少男；兑经过第三次求索而得到女爻，所以称为少女。
第十一章
乾象征天、圆形、君主、父亲、玉、金、寒冷、冰、大红色、良马、老马、瘦马、杂色马和木本果实。
坤象征地、母亲、布帛、锅、吝啬、均平、母子牛、大车、纹理、众多和车柄；就地的颜色而言，象征黑色。
震象征雷、龙、玄黄之色、铺展、大路、长子、果决急躁、青色竹子和芦苇。就马而言，象征善鸣、单足有白斑、额头有白色；就庄稼而言，象征反生；其极致为刚健、繁茂鲜明。
巽象征木、风、长女、绳墨取直、工匠、白色、长、高、进退、犹豫不决和气味。就人而言，象征头发稀少、额头宽广、眼白较多、接近市场便获利数倍；其极致是躁动之卦。
坎象征水、沟渠、隐伏、矫正弯曲、弓和车轮。就人而言，象征忧虑、心病、耳痛、血和红色；就马而言，象征脊背美好、性情急躁、头向下、蹄薄和拖曳；就车而言，象征多有损伤、通达、月亮和盗贼；就树木而言，象征坚硬而多心材。
离象征火、太阳、闪电、中女、甲胄和兵器。就人而言，象征大腹；又象征干燥之卦、鳖、蟹、螺、蚌和龟；就树木而言，象征树木中空或上部枯槁。
艮象征山、道路、小石、门阙、果实与瓜类、守门人、手指、狗、鼠以及黑嘴一类动物；就树木而言，象征坚硬而多节。
兑象征泽、少女、巫者、口舌、毁坏折断和附着决断；就地而言，象征坚硬而含盐碱；又象征妾与羊。

校读提示：第十一章“敷”“馵足”“的颡”“近利市三倍”“黔喙之属”等词，古注训释并不完全一致；本译文仅作贴近字面的工作转述，后续应逐词列出训诂来源。""", chatGptTranslation = """ChatGPT 翻译整理稿（沿用本条既有逐段译文结构；本轮先完成译文并列归档，尚需逐句对照原文复核，不作为独立校勘定本）：

译文说明：以下按通行本十一章译述。第十一章象类繁多，按原文次序逐项转述；古代物名、异体字和个别词义尚待专门训诂，不将现代象征体系反向套入原文。

第一章
从前圣人创作《易》，是为了幽微地辅助神明之道，于是产生蓍草占筮。取天数为三、地数为二来建立数的依据，观察阴阳变化而确立卦，发挥刚柔而产生爻；使之与道德相和顺，并以义理加以整理；穷究事理、尽知本性，最终通达天命。
第二章
从前圣人创作《易》，是为了顺应性命的道理。因此确立天道，称为阴与阳；确立地道，称为柔与刚；确立人道，称为仁与义。兼具天地人的三才并各自两分，所以《易》以六画成卦。分出阴位与阳位，交替运用柔爻与刚爻，所以六爻各位形成卦的文理。
第三章
天地确定上下位置，山与泽相互通气，雷与风相互激荡，水与火不相互侵害。八卦交错变化。推数过去之事是顺推，推知未来之事是逆推，所以《易》以逆数推知未来。
第四章
雷使万物发动，风使万物散布，雨水滋润万物，太阳使万物温暖干燥，艮使万物停止，兑使万物喜悦，乾统领万物，坤收藏万物。
第五章
帝的运行从震开始，在巽达到整齐，在离显现，在坤承担养育，在兑使万物喜悦，在乾形成阴阳交战，在坎使万物劳成，在艮完成并开启新的开始。万物从震方生出，震在东方；在巽方整齐，巽在东南，所谓整齐，是万物洁净齐整。离象征光明，万物在此相见，是南方之卦；圣人面南治理天下、趋向光明而施政，大概取法于此。坤就是地，万物都在此得到养育，所以说万物役使于坤。兑是正秋，万物在此喜悦，所以说喜悦表现于兑。乾在西北，阴阳在此相迫相薄。坎是水，是正北方之卦，也是劳卦，万物归藏于此，所以说劳成于坎。艮在东北，万物在此完成，又由此开始，所以说成就之事表现于艮。
第六章
所谓神妙，是用来说明万物奥妙变化的。使万物发动，没有比雷更迅速的；使万物弯曲变化，没有比风更迅速的；使万物干燥，没有比火更显著的；使万物喜悦，没有比泽更合适的；滋润万物，没有比水更充分的；终结万物又开启万物，没有比艮更盛大的。因此水火相互配合，雷风不相违背，山泽之气相通，然后才能发生变化并生成万物。
第七章
乾的性质是刚健，坤的性质是顺从；震是发动，巽是进入；坎是陷入，离是附丽；艮是停止，兑是喜悦。
第八章
乾象征马，坤象征牛，震象征龙，巽象征鸡，坎象征猪，离象征野鸡，艮象征狗，兑象征羊。
第九章
乾象征头，坤象征腹，震象征脚，巽象征大腿，坎象征耳，离象征眼，艮象征手，兑象征口。
第十章
乾是天，所以称为父；坤是地，所以称为母。震经过一次求索而得到男爻，所以称为长男；巽经过一次求索而得到女爻，所以称为长女；坎经过第二次求索而得到男爻，所以称为中男；离经过第二次求索而得到女爻，所以称为中女；艮经过第三次求索而得到男爻，所以称为少男；兑经过第三次求索而得到女爻，所以称为少女。
第十一章
乾象征天、圆形、君主、父亲、玉、金、寒冷、冰、大红色、良马、老马、瘦马、杂色马和木本果实。
坤象征地、母亲、布帛、锅、吝啬、均平、母子牛、大车、纹理、众多和车柄；就地的颜色而言，象征黑色。
震象征雷、龙、玄黄之色、铺展、大路、长子、果决急躁、青色竹子和芦苇。就马而言，象征善鸣、单足有白斑、额头有白色；就庄稼而言，象征反生；其极致为刚健、繁茂鲜明。
巽象征木、风、长女、绳墨取直、工匠、白色、长、高、进退、犹豫不决和气味。就人而言，象征头发稀少、额头宽广、眼白较多、接近市场便获利数倍；其极致是躁动之卦。
坎象征水、沟渠、隐伏、矫正弯曲、弓和车轮。就人而言，象征忧虑、心病、耳痛、血和红色；就马而言，象征脊背美好、性情急躁、头向下、蹄薄和拖曳；就车而言，象征多有损伤、通达、月亮和盗贼；就树木而言，象征坚硬而多心材。
离象征火、太阳、闪电、中女、甲胄和兵器。就人而言，象征大腹；又象征干燥之卦、鳖、蟹、螺、蚌和龟；就树木而言，象征树木中空或上部枯槁。
艮象征山、道路、小石、门阙、果实与瓜类、守门人、手指、狗、鼠以及黑嘴一类动物；就树木而言，象征坚硬而多节。
兑象征泽、少女、巫者、口舌、毁坏折断和附着决断；就地而言，象征坚硬而含盐碱；又象征妾与羊。

校读提示：第十一章“敷”“馵足”“的颡”“近利市三倍”“黔喙之属”等词，古注训释并不完全一致；本译文仅作贴近字面的工作转述，后续应逐词列出训诂来源。""")
)

private fun buildEntries(): List<Entry> = listOf(
    Entry("卜筮", "卜筮专题总纲：占筮、观象与古代占验", "本专题为典藏新增一级专题下的中分区入口。以《周易》经传为核心，旁及先秦两汉占筮文献、出土筮法、天文星占及后世易占。下列各法按文献时代和体系分别整理；传世经文、考古材料、历代注家与民间后起方法严格分层。", """一、专题范围

本专题讨论古代中国如何通过蓍筮、卜龟、观象、历数及其他占验方式理解吉凶与时变。重点不是把所有方法混称为“《周易》的算法”，而是辨明每种方法的文献出处、历史年代、操作记录、解释体系和后世变迁。

二、中分区目录

1. 《周易》占筮原理与基本概念
2. 大衍筮法与蓍草占
3. 钱币占筮与金钱卦
4. 星象观测与古代星占
5. 星图投钱与五行生克（待考）
6. 河图、洛书与易学象数
7. 卦象、爻象与占断方法
8. 古代龟卜、筮法与占验制度
9. 数字卦与出土筮法
10. 后世易占流派与方法演变
11. 占筮相关古籍、注疏与资料索引

三、阅读与校勘原则

《周易》经传明确记载的内容优先列为“经传原文”；《周礼》《左传》《国语》等其他传世文献另列出处；出土简帛按考古材料和整理本标注；宋元明清以来的钱筮、星命及民间方法标明其时代，不倒置为先秦《周易》原法。

涉及缺文、残简、异文或争议解释时，保留原貌并注明依据；不能核实的算法不补造。占筮属于历史文化材料，不应代替现实中的证据、专业意见或个人判断。""",
    """**ChatGPT 翻译**

**一、专题范围**

本专题讨论古代中国如何通过蓍筮、龟卜、观象、历数及其他占验方式理解吉凶与时变。重点不是把所有方法统称为《周易》的算法，而是辨明每种方法的文献出处、历史年代、操作记录、解释体系和后世变迁。

**二、中分区目录**

1. 《周易》占筮原理与基本概念
2. 大衍筮法与蓍草占
3. 钱币占筮与金钱卦
4. 星象观测与古代星占
5. 星图投钱与五行生克（待考）
6. 河图、洛书与易学象数
7. 卦象、爻象与占断方法
8. 古代龟卜、筮法与占验制度
9. 数字卦与出土筮法
10. 后世易占流派与方法演变
11. 占筮相关古籍、注疏与资料索引

**三、阅读与校勘原则**

《周易》经传明确记载的内容优先列为“经传原文”。《周礼》《左传》《国语》等其他传世文献另列出处；出土简帛按考古材料和整理本标注；宋元明清以来的钱筮、星命及民间后起方法标明时代，不倒置为先秦《周易》原法。

缺文、残简、异文和争议解释须保留原貌并注明依据；不能核实的算法不补造。占筮在本专题中作为历史文化材料研究，不应代替现实中的证据、专业意见或个人判断。""", chatGptTranslation = """本专题考察古代中国借助蓍草筮占、龟甲占卜、天象观测、历法和其他占验方式理解吉凶与时序的历史。重点是区分各类方法的文献出处、时代背景、实际记录和解释体系，而不是把它们都说成《周易》本身的一套算法。目录涉及大衍筮法、钱币占筮、星占、河图洛书、出土数字卦、龟卜制度和后世易占。整理时应优先依据经传原文；其他传世文献和出土材料须分别标明来源。后世方法要注明时代，残缺、异文和争议处不得凭空补齐。"""),
    Entry("卜筮", "《系辞上传》大衍筮法：经文原段与译文", "底本：通行本《周易·系辞上传》第九章。此处按经传原文录入；古注对“大衍之数五十”及具体揲蓍步骤存在不同解释，另条讨论，不把后世复原步骤混入经文。", """一、经文

大衍之数五十，其用四十有九。分而为二以象两，挂一以象三，揲之以四以象四时，归奇于扐以象闰；五岁再闰，故再扐而后挂。

天数五，地数五，五位相得而各有合。天数二十有五，地数三十，凡天地之数五十有五。此所以成变化而行鬼神也。

乾之策二百一十有六，坤之策百四十有四，凡三百有六十，当期之日。二篇之策，万有一千五百二十，当万物之数也。

是故四营而成易，十有八变而成卦，八卦而小成。引而伸之，触类而长之，天下之能事毕矣。

二、译文

大衍之数为五十，实际用于筮法的是四十九。把策分为两部分，以象征天地两仪；取出一策挂起，以象征三才；以四为单位揲数，以象征四时；把余数归置于指间，以象征历法中的闰月。经过相应的揲数过程，反复变化而形成卦。

经文又以乾坤策数、天地数和四营十八变说明易数与天地时序的象征关系。

三、校读边界

本段是《周易》传世文献中最明确的筮法记载之一。但“大衍之数”的数理含义、起始五十策的解释、余策处理及后世实际操作细节，历代注家并非完全一致。此处只录经文与直译，不将某一现代演示版本宣称为唯一古法。""",
    """**ChatGPT 翻译**

**一、经文译文**

大衍之数为五十，实际使用的是四十九。把它们分成两组，以象征天地两仪；取出一策挂起，以象征三才；按四策为一组计数，以象征四时；把余数归置于指间，以象征历法中的闰月。五年之中会有两次闰月，因此要两次处理余数，然后再进行下一轮挂一。

天数有五，地数也有五；五个位置彼此配合，各自都有相合之数。天数合计二十五，地数合计三十，天地之数总共五十五。由此成就变化，并使神妙的作用得以运行。

乾卦的策数为二百一十六，坤卦的策数为一百四十四，合计三百六十，对应一年的日数。上下两篇的策数合计一万一千五百二十，象征万物的数目。

因此，经过四类操作而形成一次变化，经过十八次变化而形成一个卦；八卦形成时，初步结构便告完成。由此推展开来，再按同类事物触类旁通，天下可以通过这种方式处理的事务便都包含在内。

**二、翻译说明**

本段把筮法操作与天地、四时、历法的数理象征结合起来。“大衍之数”的确切数理含义、最初五十策的解释和余策处理方式，历代注家存在不同意见。本译文不把某一种后世复原说成唯一确定的古法。""", chatGptTranslation = """大衍之数是五十，实际使用其中四十九策。把策分成两份，以象征天地两仪；取出一策悬置，以象征天地人三才；按四策为一组计数，以象征四季；把余策归置于指间，以象征历法中的闰月。五年之中有两次闰月，因此经过两次归余，才重新开始下一轮悬挂。天数有五个，地数也有五个，五组数彼此配合，各有相合关系。天数总和为二十五，地数总和为三十，天地之数合计五十五。乾卦的策数为二百一十六，坤卦的策数为一百四十四，合计三百六十，用来对应一年的日数。上下两篇卦爻策数合计一万一千五百二十，用来象征万物的数量。经过四项营运而形成一变，十八次变化而成一卦；八卦形成后，易道的基本结构便已初步具备。再由此推广、引申并触类旁通，便可推演天下各种事物。""", """【ChatGPT 翻译】

“大衍之数五十，其用四十有九”：大衍之数为五十，实际用于推演的是四十九策。

“分而为二以象两，挂一以象三，揲之以四以象四时，归奇于扐以象闰”：把策分为两份，象征天地两仪；取出一策悬置，象征天地人三才；每四策一组计数，象征四时；把计数后剩下的策归置在指间，象征历法中的闰月。

“五岁再闰，故再扐而后挂”：古代历法约每五年设置两次闰月，因此经文以两次归余的程序作象征说明，然后再开始下一轮。

“天数五，地数五，五位相得而各有合”：天数有五个，地数也有五个；五组数彼此配合，各自形成对应关系。

“天数二十有五，地数三十，凡天地之数五十有五”：天数合计二十五，地数合计三十，天地之数总计五十五。

“乾之策二百一十有六，坤之策百四十有四，凡三百有六十，当期之日”：乾卦策数为二百一十六，坤卦策数为一百四十四，合计三百六十，用来对应一年的日数。

“二篇之策，万有一千五百二十，当万物之数也”：上下两篇六十四卦的策数合计一万一千五百二十，用来象征万物的数量。

“四营而成易，十有八变而成卦，八卦而小成”：经过四类运作形成一次变化，经过十八次变化形成一卦；八卦构成一个初步完备的体系。具体操作如何分轮，仍须结合注疏，不以某一种后世复原方案为唯一解释。

“引而伸之，触类而长之，天下之能事毕矣”：由此推展开来，并依照事物类别触类旁通，便可以进一步推演天下各种事理。"""),
    Entry("卜筮", "大衍筮法术语：分二、挂一、揲四、归奇与十八变", "依据《系辞上传》“分而为二以象两，挂一以象三，揲之以四以象四时，归奇于扐”及“四营而成易，十有八变而成卦”。参考陈恩林、郭守信《关于〈周易〉“大衍之数”的问题》（2021）；该文指出大衍数自汉代以来即有不同解释。", """一、经文中的四个术语

分而为二：将策分为两部分，传文以“两”象征天地。
挂一：取一策挂置，传文以“三”象征天地人三才。
揲之以四：按四策为一组计数，传文以四象征四时。
归奇于扐：将揲数后余策归置于指间；“奇”“扐”的具体训释及操作细节，历代注家有异说。

二、四营与十八变

“四营而成易”概括一次成易过程中的四类营作；“十有八变而成卦”说明成卦须经多轮变化。后世常据此解释六爻成卦的步骤，但具体如何分轮、如何处理余策，应注明所据注本或复原方案。

三、译文与说明

经文用象数语言说明揲蓍过程，并将其与天地、四时、闰月相联系。这里既有操作性术语，也有象征性解释。不能只凭现代口诀反推经文原意；应并列汉唐注疏、宋代图书学及近现代研究。""",
    """**ChatGPT 翻译**

**一、经文术语**

- **分而为二：**把策分成两组。传文将两组与天地两仪相联系。
- **挂一：**取出一策挂置。传文将其与天地人三才相联系。
- **揲之以四：**每四策为一组进行计数。传文将其与四时相联系。
- **归奇于扐：**把计数后剩余的策归置在指间。“奇”“扐”的确切训释和具体操作细节，历代注家有不同解释。

**二、四营与十八变**

“四营而成易”概括形成一次变化所涉及的四类操作；“十有八变而成卦”说明形成一卦需要经过多轮变化。后世常据此解释六爻成卦的步骤，但如何划分轮次、如何处理余数，必须注明所依据的注本或复原方案。

**三、说明**

经文用象数语言描述揲蓍过程，并把它与天地、四时、闰月联系起来。这里既有操作术语，也有象征性解释。不能只凭现代口诀反推经文原意，应并列考察汉唐注疏、宋代图书学及近现代研究。""", chatGptTranslation = """“分二”是把策分为两部分，传文以此象征两仪；“挂一”是取出一策悬置，象征三才；“揲四”是按四策为一组计数，象征四时；“归奇于扐”是把计数后剩余的策归置于指间，传文以此象征历法置闰。“四营”指经文所说的四项营运，“十八变”指形成一卦所经历的十八次变化。至于每一变如何具体分策、余数如何处理，以及起始策数的理论依据，历代注家有不同解释；不能把某一种后世复原方案当成经文唯一明定的细节。"""),
    Entry("卜筮", "《系辞上传》观象：仰观天文与俯察地理", "底本：通行本《周易·系辞上传》第二章。此条是《周易》观象思想的经传依据，不等于一套以星点落位直接起卦的星占算法。", """一、经文

古者包牺氏之王天下也，仰则观象于天，俯则观法于地，观鸟兽之文与地之宜，近取诸身，远取诸物，于是始作八卦，以通神明之德，以类万物之情。

二、译文

古代包牺氏治理天下时，向上观察天象，向下考察地理法则，又观察鸟兽纹理及土地适宜情况；近处从自身取象，远处从万物取象，于是创制八卦，用以沟通神明之德、类比万物之情。

三、说明

这段经文说明八卦取象与观察天地万物的思想。它没有列出二十八宿、北斗星位或星点投钱步骤。天文观测、历法、星占与《周易》观象思想之间存在历史联系，但应分别考证，不能把后世星占术直接归为本段经文所载方法。""",
    """**ChatGPT 翻译**

**一、经文译文**

古代包牺氏治理天下时，向上观察天象，向下考察地上的法则；观察鸟兽的纹理以及土地的适宜情况。近处从自身取象，远处从万物取象，于是开始创制八卦，用来沟通神明的德性，并类比万物的情状。

**二、说明**

这段经文说明八卦取象与观察天地万物的思想。它没有列出二十八宿、北斗星位，也没有记载把钱投向星点的步骤。天文观测、历法、星占与《周易》观象思想之间存在历史联系，但应分别考证，不能把后世星占术直接归入本段经文。""", chatGptTranslation = """古代圣人抬头观察天上的天象，低头考察地上的地理，又观察鸟兽的纹理以及土地各自适宜的事物；近处从自身取譬，远处从外物取譬，于是创制八卦，用来沟通神明的德性，并分类说明万物的情状。这段话说明《易》象的形成与观察天地、鸟兽、人身和外物有关。它表达的是取象、比类的认识方式，并没有在这里列出后世星占术的完整规则。""", """【ChatGPT 翻译】

古时候包牺氏治理天下，向上观察天上的各种征象，向下考察大地的法则；观察鸟兽身上的纹理以及土地各自适宜的条件。近处就从自身取象，远处则从外界万物取象，于是创制了八卦，用来通达神明所体现的德性，并类比万物各自的情状。

“包牺”即传说中的伏羲；“文”在“鸟兽之文”中指纹理、纹饰；“地之宜”指土地及其环境所适合的事物。本段说明观物取象思想，并未记载具体星宿投钱法或完整星占程序。"""),
    Entry("卜筮", "《系辞上传》易有圣人之道四：卜筮与占", "底本：通行本《周易·系辞上传》第十章。此条重点录“卜筮者尚其占”，区分象、变、器、占四种论述维度。", """一、经文

《易》有圣人之道四焉，以言者尚其辞，以动者尚其变，以制器者尚其象，卜筮者尚其占。

二、译文

《易》包含圣人之道的四个方面：言说时重视卦爻辞，行动时重视变化，制作器物时取法卦象，进行卜筮时则重视占断。

三、说明

“卜筮者尚其占”是经传对占筮用途的概括。占断所依据的卦象、爻辞、变爻及具体占例，需要结合不同历史时期的解释传统研究；不能把某一后世流派的完整规则倒写成《系辞》原文。""",
    """**ChatGPT 翻译**

**一、经文译文**

《易》包含圣人之道的四个方面：用言语表达时，重视卦爻辞；采取行动时，重视变化；制作器物时，取法卦象；进行卜筮时，则重视占断。

**二、说明**

“卜筮者尚其占”是经传对《易》之占筮用途的概括。占断所依据的卦象、爻辞、变爻及具体占例，需要结合不同时期的解释传统研究；不能把某一后世流派的完整规则倒写成《系辞》原文。""", chatGptTranslation = """《易》包含圣人之道的四个方面：用于言说时，重视卦爻辞；用于行动时，重视变化；用于制作器物时，取法卦象；用于卜筮时，则重视占断。这里分别从言辞、变化、象制和占问四个角度概括《易》的用途。“卜筮者尚其占”并未单独规定后世各派占法的全部规则，具体解释仍须回到相应时代的文献。""", """【ChatGPT 翻译】

《易》包含圣人之道的四个方面：用言语表达时，重视卦辞和爻辞；采取行动时，重视事物的变化；制作器物时，取法卦象；进行卜筮时，则重视占筮所得的判断。

“尚”是崇尚、重视之意；“辞”“变”“象”“占”分别指向《易》的言辞、变化、象征和占筮解释。经文只概括四类用途，并未在本句中规定某一后世流派的完整占断规则。"""),
    Entry("卜筮", "《周礼》筮人、龟人及古代占筮官制", "参考《周礼·春官》筮人、龟人等职官条目。此处先建立制度史索引；九筮名称、职掌及郑玄注等需逐条依可靠版本校录，未核定的文字不拼接补造。", """一、文献范围

《周礼·春官》记载与龟卜、筮占及相关礼仪有关的职官，包括龟人、筮人等。其内容涉及占具管理、占筮程序、官职分工和礼制秩序。

二、研究重点

1. 龟人与筮人的职责区别。
2. 筮人所掌筮法名称及其具体含义。
3. 郑玄注、贾公彦疏对相关职官与术语的解释。
4. 周礼文本所呈现的制度理想与实际历史实践之间的差别。

三、校勘说明

本条为专题索引和研究提纲，不冒充《周礼》相关篇章的完整原文。待核对经文、注疏版本后，再分别建立逐句原文和译文条目。""",
    """**ChatGPT 翻译**

**一、文献范围**

《周礼·春官》记载了与龟卜、筮占及相关礼仪有关的职官，包括龟人、筮人等。其内容涉及占具管理、占筮程序、官职分工和礼制秩序。

**二、研究重点**

1. 龟人与筮人的职责有何区别。
2. 筮人所掌筮法的名称及其具体含义。
3. 郑玄注、贾公彦疏如何解释相关职官与术语。
4. 《周礼》呈现的制度理想与实际历史实践有何差别。

**三、校勘说明**

本条是专题索引和研究提纲，并非《周礼》相关篇章的完整原文。待核对经文、注疏版本后，再分别建立逐句原文和译文条目。""", chatGptTranslation = """《周礼·春官》设置了与龟卜、筮占及相关礼仪有关的职官，其中包括龟人和筮人。相关记载涉及占具管理、占问事务、官职分工和礼制秩序。研究时须区分龟人与筮人的职责，核查筮人所掌三易、九筮的含义，并比较郑玄注、贾公彦疏与不同时期实际占验活动的差别。本条是制度史导读，不是《周礼》相关篇章的全文翻译。"""),
    Entry("卜筮", "《周礼·春官·筮人》九筮名称与文本异文", "文献依据：今本《周礼·春官·筮人》相关条文；《太平御览》卷筮上引《周礼》保存九筮名称，并转录郑玄关于“巫”字当为“筮”字之误的校读。此处采用可核的引文并明示转引层次，不冒称已完成各本逐字校勘。", """一、引文（《太平御览》引《周礼》）

《周礼·春官上》曰：“筮人掌三易，以辩九筮之名。一曰《连山》，二曰《归藏》，三曰《周易》。九筮之名，一曰巫更，二曰巫咸，三曰巫式，四曰巫目，五曰巫易，六曰巫比，七曰巫祠，八曰巫参，九曰巫𡑡，以辩吉凶。”

郑玄曰：“此九巫读皆当为筮字之误也。更谓筮迁都邑也。凡国之事，先筮而后卜。”

二、译文

筮人掌管三种《易》书，并辨别九种筮法的名称。引文列出连山、归藏、周易三易，以及九个以“巫”字开头的名称。郑玄认为这里的“巫”应按“筮”字的讹误来读，并解释其中“更”与迁都占筮有关；国家事务通常先行筮问，然后再行龟卜。

三、校勘说明

此处九个名称的字形和读法存在传抄、训读问题。尤其“巫”字，郑玄已提出校读意见；末一名称“巫𡑡”的字形也须依版本图像复核。当前引文来自《太平御览》所保存的《周礼》引文，不等于已据《周礼》古本、注疏本完成校勘。九筮各自的具体适用场景，不能仅凭名称臆测补齐。""", """,
    """**ChatGPT 翻译**

**一、引文译文（《太平御览》引《周礼》）**

筮人掌管三种《易》，并辨别九种筮法的名称。第一种叫《连山》，第二种叫《归藏》，第三种叫《周易》。九种筮法依次名为巫更、巫咸、巫式、巫目、巫易、巫比、巫祠、巫参、巫𡑡，用以辨别吉凶。

郑玄认为，这九个名称中的“巫”都应当读作“筮”，是文字讹误。“更”指为迁移都邑而进行的筮问。凡国家大事，通常先筮而后卜。

**二、校勘说明**

九个名称的字形和读法在传抄过程中存在问题。郑玄已经提出“巫”应读作“筮”的校读意见。末一名称“巫𡑡”的字形也须依版本图像复核。此处引文由《太平御览》保存，不等于已经完成对《周礼》各古本的逐字校勘。九筮各自的具体用途不能仅凭名称臆测补齐。""", chatGptTranslation = """筮人掌管三种《易》书，并辨别九种筮法的名称。引文先列出《连山》《归藏》《周易》，随后列出九个筮名，现存引文写作“巫更、巫咸、巫式、巫目、巫易、巫比、巫祠、巫参、巫𡑡”。郑玄认为这些名称中的“巫”应当读作“筮”字之误，并解释“更”与迁移都邑的筮问有关；他还说，国家事务通常先行筮问，然后再行龟卜。此处依据《太平御览》保存的《周礼》引文，不能等同于已完成各版本《周礼》原本的逐字校勘。九名的具体含义及末一字形仍须核验。"""),
        """【ChatGPT 翻译｜工作译文】

《周礼·春官上》说：“筮人掌管三种《易》，负责辨明九种筮法的名称。三种《易》分别是《连山》《归藏》《周易》。九种筮法的名称，依次是巫更、巫咸、巫式、巫目、巫易、巫比、巫祠、巫参、巫𡑡，用来辨别吉凶。”

郑玄说：“这九个名称中的‘巫’字，都应当读作‘筮’，这是文字讹误。‘更’指通过筮占决定迁移都邑。凡是国家大事，通常先用蓍草筮问，然后再用龟甲占卜。”

译文说明：“三易”是此处文献所列的三种易书；九筮名称的具体含义，现有材料并不足以全部确定。郑玄对“巫”字的读法属于古注校读，不是原文中已经写明的改字。末一名称的罕见字仍须核对版本图像，不能仅凭译文确定其字形。""")
    Entry("卜筮", "《礼记·曲礼》卜筮礼制：不过三、不相袭", "底本参照《礼记·曲礼》通行文本；中国孔子网所载经文可供核对。此条讨论礼制规范，不将其解释为任何现代占卜次数规则。", """一、经文

外事以刚日，内事以柔日。凡卜筮日，旬之外曰远某日，旬之内曰近某日。丧事先远日，吉事先近日。

曰：“为日，假尔泰龟有常，假尔泰筮有常。”

卜筮不过三，卜筮不相袭。龟为卜，筴为筮。卜筮者，先圣王之所以使民信时日、敬鬼神、畏法令也，所以使民决嫌疑、定犹与也。故曰：“疑而筮之，则弗非也。”日而行事，则必践之。

二、译文

外部事务选择阳刚之日，内部事务选择阴柔之日。占问日期时，超过十日的称为远日，十日以内的称为近日；丧事先选择较远的日期，吉事先选择较近的日期。

占问时要向龟和蓍表达请求，希望它们给出恒常可信的征兆。卜筮不应反复超过三次，龟卜与筮占也不应相互重复替代。用龟甲进行的是卜，用蓍策进行的是筮。先王设置卜筮制度，是为了使人重视时日、敬畏礼法，并在疑难未决时作出选择；一旦择定日期并举行相关事务，就应依礼实行。

三、解释边界

“卜筮不过三”在礼学注疏中有具体礼仪语境，涉及择日等程序；不应脱离上下文，简单改写成“任何占卜都能连续试三次”的通用算法。“卜筮不相袭”强调龟卜与筮占不宜无原则地反复替换。""", """,
    """**ChatGPT 翻译**

**一、经文译文**

外部事务选择刚日，内部事务选择柔日。凡是通过卜筮选择日期，超过十日的称为远日，十日以内的称为近日。丧事先选择较远的日期，吉事先选择较近的日期。

占问时说：“为了确定日期，请求你这尊大龟保持恒常，请求你这尊大筮保持恒常。”

卜筮不应超过三次，龟卜与筮占也不应相互反复替代。用龟甲进行的是卜，用蓍策进行的是筮。先圣先王设置卜筮，是为了使百姓相信时日、敬畏鬼神、畏惧法令，也是为了让人们解决疑惑、消除犹豫。因此说：“有疑难而用筮占问，便不应轻易否定所得结果。”选定日期并开始办事，就必须切实履行。

**二、解释边界**

“卜筮不过三”处于礼制语境，涉及择日等程序，不应脱离上下文改写成适用于所有占卜的通用次数规则。“卜筮不相袭”强调龟卜与筮占不应毫无原则地反复替换。""", chatGptTranslation = """外部事务选择阳日，内部事务选择阴日。占问日期时，十日以外称为较远的日期，十日以内称为较近的日期；丧事先选择较远的日期，吉事先选择较近的日期。行卜筮礼时，要请求大龟和大筮给出恒常可信的征兆。卜筮不应超过三次，也不应把龟卜和筮占反复交替使用。用龟甲的是卜，用蓍策的是筮。先王设置卜筮制度，是为了使民众重视时日、敬畏鬼神、畏惧法令，并在疑惑难决时作出选择。既然已经选定日期并决定举行某项事务，就应当切实履行。“不过三”须放在本段礼仪语境中理解，不能改造成适用于所有占问方式的通用规则。"""),
        """【ChatGPT 翻译｜工作译文】

外部事务选用阳日，内部事务选用阴日。凡是通过卜或筮来选择日期，十日周期以外的日期称为“远某日”，十日周期以内的日期称为“近某日”。办理丧事，先选较远的日期；办理吉庆之事，先选较近的日期。

仪式上的祷告说：“请为我确定日期，借助您灵验而有常度的大神龟，借助您灵验而有常度的大神筮。”

卜和筮都不应超过三次，也不应把龟卜与蓍筮交替反复使用。用龟甲进行的叫作“卜”，用蓍草策数进行的叫作“筮”。卜筮制度，是先代圣王用来使百姓相信时令与日期、敬重鬼神、敬畏法令的办法，也是用来帮助人们裁定疑惑、消除犹豫的办法。所以说：“心中有疑惑而用筮占询问，便不再认为这样做不妥。”既然已经选定日期并决定办事，就一定要按期实行。

译文说明：“泰龟”“泰筮”中的“泰”保留其尊称或强调语气，不擅自落实为某一具体神祇。“旬”按十日为一周期理解。“卜筮不过三”必须放在本段择日与礼仪制度的语境中理解，不能抽离为现代占问的通用操作规则。""")
    Entry("卜筮", "《左传·僖公十五年》晋献公筮嫁伯姬：归妹之睽", "底本参照《春秋左传正义》僖公十五年相关经传与注疏；另可参洪迈《容斋随笔·左传易筮》对春秋筮例的回顾。此条为单一筮例，不代表所有先秦占筮规则。", """一、经文节录

初，晋献公筮嫁伯姬于秦，遇《归妹》之《睽》。史苏占之曰：“不吉。其繇曰：‘士刲羊，亦无血也。女承筐，亦无贶也。西邻责言，不可偿也。’《归妹》之《睽》，犹无相也。”

二、译文

当初，晋献公为把伯姬嫁到秦国而进行筮占，得到《归妹》卦变为《睽》卦。史苏判断不吉，并引用筮辞，认为男方宰羊却没有血，女子捧着筐却没有得到赠物，西方邻国将有责难而无法偿还；《归妹》变为《睽》，意味着婚姻关系缺乏相助。

三、占例研究

此例展示了春秋筮例中本卦、之卦和爻辞可能共同参与解释。故事后文又结合雷火、车、旗等象作进一步推断。洪迈《容斋随笔》总结《左传》多则筮例时，指出其中常见一爻变而兼取前后两卦辞义；这是南宋学者对早期筮例的归纳，不应直接等同于《系辞》大衍法的完整操作说明。

四、校勘提示

古本及不同整理本在“无血”相关字形上有异文，传本亦见“无衁”等写法。这里采用可读通行字并保留异文提示；正式定本仍应对照《春秋左传正义》影印页逐字复核。""", """,
    """**ChatGPT 翻译**

**一、经文译文**

当初，晋献公为把伯姬嫁到秦国而进行筮占，得到《归妹》卦变为《睽》卦。史苏判断这不吉利，并引用筮辞说：“男子宰羊，却没有血；女子捧着筐，却没有得到赠物。西边的邻国提出责难，无法偿还。”《归妹》变为《睽》，意味着双方不能互相帮助。

**二、筮例研究**

此例显示，春秋时期的筮占叙事可能把本卦、之卦和爻辞结合起来解释。故事后文还借助雷火、车、旗等象作进一步推断。洪迈在《容斋随笔》中回顾《左传》筮例时，归纳出某些案例会因一爻变化而兼取前后两卦的辞义。这是南宋学者对早期筮例的总结，不是对大衍筮法完整操作过程的说明。

**三、校勘提示**

不同版本在“无血”相关字句上存在异文，传本中也见“无衁”等写法。本译文采用较易读的通行文字，同时保留异文提示。定本仍须对照《春秋左传正义》影印页逐字复核。""", chatGptTranslation = """当初，晋献公为把伯姬嫁给秦国而进行筮占，得到《归妹》卦，变化后成为《睽》卦。史苏判断此事不吉，并引用筮辞：男子宰羊却没有血，女子捧着筐却没有得到赠物；西方邻国将提出责难，而且无法偿还。《归妹》变为《睽》，意味着双方不能相互协助。这则故事把本卦、之卦和筮辞放在一起解释，反映了《左传》叙事中春秋筮例的一种解释方式。具体字句存在传本异文，译文不据后世故事结果反推原文。""", """【ChatGPT 翻译】

外部事务选用刚日，内部事务选用柔日。通过卜筮选择日期时，超过十日的称为“远某日”，十日以内的称为“近某日”。丧事先选择较远的日期，吉事先选择较近的日期。

择日时说：“请确定日期，借助您这尊有常法的大神龟；借助您这套有常法的大神筮具。”

卜筮不得超过三次，龟卜与筮占不应反复交替使用。用龟甲进行的叫作“卜”，用蓍策进行的叫作“筮”。卜筮是先代圣王用来使百姓相信时日、敬重鬼神、敬畏法令的制度，也是帮助人们排除疑惑、确定犹豫之事的办法。所以说：“心中有疑而通过筮问作出决定，就不再非议它。”日期确定并开始办事，就必须依照约定实行。

“旬”是一段十日的时间；“筴”在此指用于筮占的策具。“卜筮不过三”处于礼仪择日的上下文中，不宜解释成任意占问都可重复三次的通用规则。"""),
        """【ChatGPT 翻译｜工作译文】

当初，晋献公为把伯姬嫁给秦国而进行筮占，得到《归妹》卦，变为《睽》卦。史苏判断说：“不吉利。它的筮辞是：‘男子宰羊，却没有流出血；女子捧着筐，却没有得到赠物。西边邻国提出责难，这种责难无法偿还。’《归妹》变成《睽》，就像双方不能相互帮助。”

译文说明：“遇《归妹》之《睽》”表示筮占所得的本卦与变化后的卦；“繇”指筮辞。“女承筐，亦无贶也”中的“贶”是赠予、赐予之意。“西邻责言，不可偿也”可理解为西方邻国将提出难以回应或偿付的责难，具体指涉仍须结合后文史事与注疏判断。“犹无相也”是以卦变关系比拟双方缺乏相助，不能把它扩写成原文没有明说的婚姻细节。

本译文只翻译本条所录节文，不代替《左传》后续叙事，也不把后世对春秋筮例的归纳倒写成这段经传原文。""")
    Entry("卜筮", "《左传》《国语》筮例：占得卦象之后如何解释", "参考清华大学出土文献研究与保护中心介绍《出土数字卦文献辑释》（贾连翔著，2020）所列先秦至汉初传世文献筮占研究；具体筮例须逐条回到《左传》《国语》原文及注疏。", """一、材料性质

《左传》《国语》保存了多则古代筮占叙事，常见结构包括提出所问、记述所得卦象、引用卦爻辞或相关象义、解释吉凶及后续事件。

二、研究时必须区分

所得卦象如何形成，是成卦方法问题；如何引用卦辞、爻辞及象义，是解卦方法问题；故事最后如何叙述结果，则属于文本记载和历史解释问题。三者不能混为一谈。

三、整理原则

每则筮例应保留原文上下文、人物、时间、所问事项、卦名或爻辞、古注及后续叙事。若不同版本存在文字差异，应并列标注；不把后世六爻纳甲规则套用于先秦筮例。""",
    """**ChatGPT 翻译**

**一、材料性质**

《左传》《国语》保存了多则古代筮占叙事。常见结构包括提出所问、记载所得卦象、引用卦爻辞或相关象义、解释吉凶，以及叙述后续事件。

**二、研究时必须区分**

卦象如何形成，是成卦方法问题；如何引用卦辞、爻辞及象义，是解卦方法问题；故事最后如何叙述结果，则属于文本叙事和历史解释问题。三者不能混为一谈。

**三、整理原则**

每则筮例应保留原文上下文、人物、时间、所问事项、卦名或爻辞、古注及后续叙事。不同版本有文字差异时，应并列标注；不能把后世六爻纳甲规则套用于先秦筮例。""", chatGptTranslation = """《左传》和《国语》保存了多则与卜筮有关的故事。这些记录有时引用卦名、爻辞或卦变，有时还结合卦象、人物处境和政治事件作出解释。整理时，应逐条区分原始叙事、所引筮辞、占者解释以及后世注家的补充。不同故事采用的解释方式未必相同，不能把它们归纳成一套先秦各地通行且完全一致的规则。本条是案例索引，不替代各篇原文。"""),
    Entry("卜筮", "钱币占筮与金钱卦：历史层次和文献边界", "本条为钱筮专题导言。钱代蓍及三钱法的历史须分别核对礼学注疏、宋元明清易占文献和民俗资料；不能仅凭现代通行程度推断其起源年代。", """一、需要区分的材料

1. 蓍草筮法：有《系辞上传》明确经文记载。
2. 以钱代蓍：历代文献中有相关讨论，但具体年代、钱数和操作方式需逐条考证。
3. 三钱金钱卦：后世流传广泛的一种钱筮形式，其规则不能直接等同于《系辞》原文。
4. 其他钱数与投掷图式：须按具体书籍、图版、版本和算法单独立条。

二、当前收录边界

“一至七枚钱币均可一次投掷，并依据星图落点和五行生克推算”的说法，暂列待考材料。尚未确认其书名、作者、年代及完整规则，不据此断言它是先秦《周易》筮法。

三、说明

本条以历史文献整理为目的。不同钱数、投掷次数、正反面规则及图式之间不可互相套用；未找到可靠出处的内容标为待考。""",
    """**ChatGPT 翻译**

**一、必须区分的材料**

1. **蓍草筮法：**《系辞上传》对大衍筮法有明确记载。
2. **以钱代蓍：**历代文献中有相关讨论，但具体年代、钱币数量和操作方式仍须逐条考证。
3. **三钱金钱卦：**后世流传较广的一种钱筮形式，其规则不能直接等同于《系辞》原文。
4. **其他钱数和投掷图式：**应根据具体书籍、图版、版本和规则分别立条。

**二、当前收录边界**

“一至七枚钱币均可一次投掷，并依据星图落点和五行生克推算”的说法目前列为待考。其书名、作者、年代及完整规则尚未确认，因此不能断言它是先秦《周易》筮法。

**三、说明**

本条以历史文献整理为目的。不同钱币数量、投掷次数、正反面规则及图式不能互相套用；尚未找到可靠出处的内容应继续标为待考。""", chatGptTranslation = """以钱币代替蓍策进行占问，是后世易占发展中的一种方法。它与《系辞》所记蓍策筮法在占具和操作传统上有所不同，不能仅因最终都能形成卦象，就认定二者自古以来完全相同。研究钱币占筮，应查明相关方法最早出现的文献、术语、钱币形制和解释规则，并区分明清以来的通行做法与先秦两汉材料。缺乏早期文献证据时，不应把后世钱卦规则说成周代原法。"""),
    Entry("卜筮", "星象观测与古代星占：与《周易》观象思想的区别", "研究索引：传世天文星占文献包括《史记·天官书》《汉书·天文志》《开元占经》等；《系辞上传》“仰则观象于天”另属易学观象表述。各文献应按版本分别校录。", """一、星象观测

古代天文观测涉及日月运行、恒星、星官、二十八宿、行星及彗孛等天象。历法和天文观测具有长期连续的记录传统。

二、星占

古代星占文献会将天象变化与政治、军事、灾异等解释体系联系起来。星占是独立而复杂的文献传统，不等于《周易》六爻筮法，也不等于现代天文学。

三、与《周易》的关系

《系辞》提出仰观天象、俯察地理，体现取象思想；但传世《周易》并未因此给出一套完整的二十八宿投钱算法。后世将星宿、五行、八卦等相配的做法，应按时代和流派单独说明。""",
    """**ChatGPT 翻译**

**一、星象观测**

古代天文观测涉及日月运行、恒星、星官、二十八宿、行星、彗星等天象。历法和天文观测具有长期连续的记录传统。

**二、星占**

古代星占文献会把天象变化与政治、军事、灾异等解释体系联系起来。星占是独立而复杂的文献传统，不等同于《周易》六爻筮法，也不等同于现代天文学。

**三、与《周易》的关系**

《系辞》提出仰观天象、俯察地理，体现了从天地万物中观察和提取象征的思想。但传世《周易》并未因此给出一套完整的二十八宿投钱算法。后世把星宿、五行、八卦相配的做法，应按其时代和流派单独说明。""", chatGptTranslation = """古代中国长期观察日月、星辰、行星运行和异常天象，并逐渐形成天文历法与星占传统。天文观测关注天体位置和周期，星占则进一步把某些天象与人事、政事联系起来解释。《系辞》所说仰观天文、俯察地理，表达了观察天地并取象立说的思想；但这句话本身没有给出后世星占体系的星官、分野或占验规则。因此，天文学史、星占史和《周易》观象思想应相互参照，却不能混为同一套方法。"""),
    Entry("卜筮", "星图投钱与五行生克（待考）", "待考条目。图式参考由用户在本项目讨论中提供的两张现代图片；图片仅作为线索，不是古籍原图。现阶段尚未确认书名、作者、年代、星群名称、钱币数规则及原始出处。", """一、目前掌握的线索

据口述描述，该方法可能涉及一至七枚钱币一次投掷、特定星点图式、钱币落点与五行生克关系。另有五行正五边形布局线索：从顶点顺时针为金、火、木、土、水。

二、尚未证实的内容

上述线索尚不足以确定图中星群名称、北斗在图中的具体位置、投币落点如何判读、不同钱数的规则，以及五行生克在判断中所处的环节。不能仅凭图形相似认定其属于古代星占、河图洛书或《周易》大衍筮法。

三、后续考证要求

找到原书书名、作者、版本和对应页图后，先核验图版与原文，再整理术语、步骤和五行配属。若来源无法确认，持续标注“待考”，不补造算法，不将用户提供图片标作古籍原图。""",
    """**ChatGPT 翻译**

**一、目前掌握的线索**

据现有描述，这种方法可能涉及一至七枚钱币一次投掷、特定星图、钱币落点以及五行之间的生克关系。另有一种五行正五边形布局线索：从顶点顺时针依次为金、火、木、土、水。

**二、尚未证实的内容**

这些线索还不足以确定图中的星群名称、北斗在图中的具体位置、钱币落点的判读方式、不同钱数对应的规则，以及五行生克在判断中所起的作用。不能仅凭图形相似，就认定它属于古代星占、河图洛书或《周易》大衍筮法。

**三、后续考证要求**

找到原书书名、作者、版本和对应页图后，应先核验图版与原文，再整理术语、步骤和五行配属。若来源无法确认，就继续标注“待考”；不得补造规则，也不能把用户提供的图片标作古籍原图。""", chatGptTranslation = """现有线索只表明，这种方法可能使用一至七枚钱币，并把钱币投到类似星图或五行布局的图面上；另有图形线索称，五行名称按五边形顶点顺时针排列为金、火、木、土、水。这些线索尚不足以确认书名、作者、年代、星群名称、投掷结果的判读方式，以及五行生克在其中的具体作用。不能仅凭布局相似，就断定它属于古代星占、河图洛书或大衍筮法。来源未核实前，本条只能作为待考资料记录，不构成古法说明。"""),
    Entry("卜筮", "龟卜与筮占：材料、程序和历史区别", "参考《周礼·春官》龟人、筮人及先秦两汉相关文献。此条为概念性导言，具体仪式与术语须依时代、地域和出土材料分别考证。", """一、龟卜

龟卜以龟甲等材料进行占卜，相关遗存可见于商周甲骨材料。甲骨卜辞常包含贞问、兆象判断及占辞等内容。

二、筮占

筮占以蓍策或其他数术媒介形成数、卦，再依据卦象、爻辞或特定规则解释。传世《系辞》保存了大衍筮法的理论叙述。

三、区别与联系

龟卜与筮占是不同的占验技术和文献系统。古代礼制可能并用龟、筮，但不能把甲骨卜辞直接解释为《周易》六十四卦，也不能把所有数字卦都视作蓍草筮法的结果。""",
    """**ChatGPT 翻译**

**一、龟卜**

龟卜使用龟甲等材料进行占问。商周甲骨材料中可以看到相关遗存。甲骨卜辞通常包含所问之事、对兆象的判断以及占辞等内容。

**二、筮占**

筮占使用蓍策或其他数术媒介形成数字和卦象，再依据卦象、爻辞或特定规则进行解释。传世《系辞》保存了关于大衍筮法的理论叙述。

**三、区别与联系**

龟卜与筮占是不同的占验技术和文献系统。古代礼制可能同时使用龟卜和筮占，但不能把甲骨卜辞直接解释为《周易》六十四卦，也不能把所有数字卦都视为蓍草筮法的结果。""", chatGptTranslation = """龟卜以龟甲等材料进行占问，商周甲骨材料保存了大量相关遗存和卜辞。筮占则借助蓍策或其他数术媒介形成数与卦，再依卦象、爻辞或特定传统解释。两者是不同的占验技术和文献系统。古代礼制中可以同时出现龟卜和筮占，但不能把甲骨卜辞直接解释成《周易》六十四卦，也不能把所有出土数字卦都认定为蓍草筮法的结果。"""),
    Entry("卜筮", "数字卦与出土筮法：商周至战国材料", "参考贾连翔《出土数字卦文献辑释》（中西书局，2020；清华大学出土文献研究与保护中心介绍）及吴晓欣《楚卜筮简中的数字卦及清华简〈筮法〉的解卦原理》（《深圳大学学报》2016年第3期）。", """一、出土材料

出土数字卦见于甲骨、铜器、陶器和战国竹简等材料。它们为研究先秦筮占提供了不同于传世经文的实物证据。

二、清华简《筮法》

学术研究指出，清华简《筮法》记载了一套数字卦占筮原理；楚地包山、天星观、葛陵等出土筮简则保存了实际占筮记录。不同材料的性质和用途不能简单等同。

三、学术争议

数字卦的用数体系、成卦方法、与《周易》六十四卦的关系，以及是否属于某种“三易”传统，仍需按具体研究者观点并列呈现。不得把学术假说写成无争议定论。""",
    """**ChatGPT 翻译**

**一、出土材料**

数字卦见于甲骨、铜器、陶器和战国竹简等材料。它们为研究先秦筮占提供了不同于传世经文的实物证据。

**二、清华简《筮法》**

学术研究指出，清华简《筮法》记载了一套数字卦占筮原理；楚地包山、天星观、葛陵等出土筮简则保存了实际占筮记录。不同材料的性质和用途不能简单等同。

**三、学术争议**

数字卦的用数体系、成卦方法、与《周易》六十四卦的关系，以及是否属于某种“三易”传统，仍需依据具体研究并列呈现。学术假说不能写成没有争议的定论。""", chatGptTranslation = """考古发现的数字卦见于甲骨、青铜器、陶器和战国竹简等不同载体，为研究先秦筮占提供了传世经典以外的实物资料。清华简《筮法》保存了一套与数字卦有关的系统性材料；包山、天星观、葛陵等楚地筮简则保存了具体占问记录。前者偏重方法和解释体系，后者多为实际占问档案，两类材料不能简单等同。数字的含义、成卦方式以及这些材料与传世《周易》的关系仍有学术讨论，各家假说应并列注明。"""),
    Entry("卜筮", "卦象、爻象与占断：本卦、变爻和之卦", "以《周易》经传和《左传》《国语》筮例为基础建立概念索引。后世纳甲、六爻、梅花易数等解释规则另列，不混入先秦材料。", """一、卦象与爻象

卦由爻组成，卦象和爻位构成解释《周易》的基本结构。经文以卦辞、爻辞表达不同情境下的判断和劝诫。

二、占断材料

古代筮例可能引用本卦卦辞、某一爻辞、之卦或其他象义。具体使用哪种规则，应以该筮例原文及其时代为准。

三、后世发展

本卦、动爻、变卦等概念在后世易占中形成了更系统的解释方法。纳甲、世应、六亲、六神等属于后世体系，须标注其文献源流，不应倒置为先秦《周易》原始规则。""",
    """**ChatGPT 翻译**

**一、卦象与爻象**

卦由爻组成，卦象和爻位构成解释《周易》的基本结构。经文通过卦辞、爻辞表达不同情境下的判断和劝诫。

**二、占断材料**

古代筮例可能引用本卦卦辞、某一爻辞、之卦或其他象义。具体使用哪种规则，应以该筮例的原文及其时代为准。

**三、后世发展**

本卦、动爻、之卦等概念在后世易占中形成了更系统的解释方法。纳甲、世应、六亲、六神等属于后世体系，须标明其文献源流，不能倒置为先秦《周易》的原始规则。""", chatGptTranslation = """卦由阴阳爻组成，卦象、爻位、卦辞和爻辞构成《周易》文本的基本结构。古代筮例中，有时引用本卦卦辞，有时引用某一爻辞，也有时结合变化后得到的卦象解释。本卦、变爻和之卦等概念在后世易占中逐渐形成更细密的解释体系。纳甲、世应、六亲和六神等术语属于后起传统，必须注明时代与文献来源，不应倒置成先秦经传已经完整规定的规则。"""),
    Entry("卜筮", "后世易占流派：纳甲、六爻与梅花易数", "本条为后世方法史索引。各流派的成书年代、作者归属和传承说法应分别核验；尤其对托名古人、后世编定的文献须标明证据等级。", """一、纳甲与六爻

纳甲体系将干支、五行等纳入卦爻解释，后世六爻占法又发展出世应、六亲、六神等术语。它们是历史发展形成的解释系统，不是《系辞》大衍段落的逐字内容。

二、梅花易数

梅花易数属于后世易学数术传统，常以时间、数字或事物取象。其具体文本、作者归属和传承年代须依据版本研究，不直接归入先秦经传。

三、整理原则

每种方法单独记录最早可核文献、现存版本、主要注家、术语体系和争议。无法确认的传承故事标注“传说”或“待考”。""",
    """**ChatGPT 翻译**

**一、纳甲与六爻**

纳甲体系将天干、地支、五行等纳入卦爻解释。后世六爻占法又发展出世应、六亲、六神等术语。这些都是历史发展形成的解释系统，并非《系辞》大衍段落的原文内容。

**二、梅花易数**

梅花易数属于后世易学数术传统，常通过时间、数字或事物取象。其具体文本、传统作者归属和传承年代，须依据现存版本研究，不能直接归入先秦经传。

**三、整理原则**

每种方法都应分别记录最早可核验的文献、现存版本、主要注家、术语体系和争议。无法确认的传承故事应标注为“传说”或“待考”。""", chatGptTranslation = """纳甲体系把天干、地支和五行等内容纳入卦爻解释；后世六爻方法又发展出世应、六亲、六神等术语。它们属于历史上逐步形成的易占解释系统，并不是《系辞》大衍段落的逐字内容。梅花易数属于后世易学数术传统，相关文献常以时间、数字或事物取象。其作者归属、成书年代和传承过程需要依据具体版本考察，不能只凭传统题署就认定为先秦古法。"""),
    Entry("卜筮", "占筮相关古籍与研究资料索引", "资料索引依据已核验的传世典籍和学术出版信息建立；后续逐项补充版本、卷次、页码、数字化来源及图版。", """一、传世经典

《周易·系辞传》《周礼》《尚书》《左传》《国语》《史记·天官书》《汉书·天文志》等。

二、出土文献与研究

清华简《筮法》、包山楚简、天星观楚简、葛陵楚简及贾连翔《出土数字卦文献辑释》等。

三、后世易学文献

历代蓍法、钱筮、纳甲、六爻、梅花易数及星占文献，按成书年代和版本分别建立条目。

四、校勘要求

每一条资料须注明作者或整理者、书名、卷篇、版本、页码或简号、数字化来源。只有摘要或转述的材料不得标为全文；原图、摹本、现代绘图必须清楚区分。""",
    """**ChatGPT 翻译**

**一、传世经典**

相关文献包括《周易·系辞传》《周礼》《尚书》《左传》《国语》《史记·天官书》《汉书·天文志》等。

**二、出土文献与研究**

相关材料包括清华简《筮法》、包山楚简、天星观楚简、葛陵楚简，以及贾连翔《出土数字卦文献辑释》等研究成果。

**三、后世易学文献**

历代蓍法、钱筮、纳甲、六爻、梅花易数和星占文献，应按成书年代和版本分别建立条目。

**四、校勘要求**

每项资料都应注明作者或整理者、书名、卷篇、版本、页码或简号、数字化来源。只有摘要或转述的材料不得标为全文；古籍原图、摹本和现代绘图必须明确区分。""", chatGptTranslation = """传世资料包括《周易》经传、《周礼》《尚书》《左传》《国语》，以及《史记·天官书》《汉书·天文志》等涉及占验和天象的文献。出土材料包括清华简《筮法》、包山楚简、天星观楚简和葛陵楚简等。后世资料包括蓍法、钱筮、纳甲、六爻、梅花易数及星占类著作。每条资料都应记录作者或整理者、书名、卷篇、版本、页码或简号、数字化来源。只有摘要或转述的材料不能标作全文，古籍原图、摹本和现代重绘也必须明确区分。"""),
    Entry("河图", "《周易·系辞上传》天地之数与河图数说", "经文底本：通行本《系辞上传》第九章；注文据李鼎祚《周易集解》卷十四，参校四库本数字文本。此条区分经文天地数、汉唐注家解释与后世图式，不把三者混为一谈。", """一、经文

天一，地二；天三，地四；天五，地六；天七，地八；天九，地十。

天数五，地数五，五位相得而各有合。

天数二十有五，地数三十，凡天地之数五十有五。此所以成变化而行鬼神也。

二、注文辑录

《周易集解》卷十四所录虞翻曰：

“天数五，谓一三五七九；地数五，谓二四六八十也。”

“天数二十有五”者，一、三、五、七、九相加为二十五。

“地数三十”者，二、四、六、八、十相加为三十。

“凡天地之数五十有五”者，天数二十五、地数三十，合为五十五。

虞翻又以“五位”解释五行之位，并将天干、地支与八卦方位相配；其具体配法属于汉代象数注释，不等同于经文直接给出的一幅点数图。

三、郑玄、孔安国等关于图书的不同解释

《周易集解》卷十四《河出图，洛出书，圣人则之》下注：

郑玄曰：“《春秋纬》云：河以通乾，出天苞；洛以流坤，吐地符。河龙图发，洛龟书成，‘河图’有九篇，‘洛书’有六篇也。”

孔安国曰：“河图则八卦也，洛书则九畴也。”

侯果曰：“圣人法河图、洛书，制历象以示天下也。”

这几说并不相同：郑玄引纬书说河图九篇、洛书六篇；孔安国将河图联系八卦、洛书联系《洪范》九畴；侯果强调圣人取法图书以制历象。不能把它们合并成一个无差别的“古说”。

四、译文

天地之数依次为：天的一、地的二，天的三、地的四，天的五、地的六，天的七、地的八，天的九、地的十。天数有五个，地数也有五个，五组数彼此配合，各有相合关系。天数合计二十五，地数合计三十，天地之数总计五十五；经文说这是形成变化、运行鬼神之道的依据。

校读边界：本段经文确实列出一至十及五十五之数，但没有在这段文字中画出后世通行的黑白点河图，也没有明说“戴九履一”等九宫位置。后世图式须按各自时代和作者另立条目。""",
    """**ChatGPT 翻译**

**一、经文译文**

天的数是奇数一，地的数是偶数二；天三、地四；天五、地六；天七、地八；天九、地十。天数有五个，地数也有五个，五组数字彼此配合，各有相合关系。天数合计二十五，地数合计三十，天地之数总计五十五。经文说，这些数字是形成变化、使神妙作用运行的依据。

**二、虞翻注译**

所谓天数五，是指一、三、五、七、九；所谓地数五，是指二、四、六、八、十。天数二十五，就是把一、三、五、七、九相加所得；地数三十，就是把二、四、六、八、十相加所得。天地之数五十五，就是二十五与三十相加的总数。

虞翻又用“五位”解释五行的位置，并把天干、地支与八卦方位相配。这些具体配法属于汉代象数注释，并不是经文直接画出的一幅点数图。

**三、郑玄、孔安国等说法**

郑玄引用纬书，认为河图有九篇、洛书有六篇；孔安国把河图联系到八卦，把洛书联系到《洪范》九畴；侯果则强调圣人取法图书以制定历法和天象制度。这些解释并不相同，不能合并成一种没有差别的“古说”。

**四、校读边界**

经文确实列出一至十及天地五十五之数，但本段没有画出后世通行的黑白点河图，也没有明说“戴九履一”等九宫位置。后世图式应按各自时代和作者另立条目。""", chatGptTranslation = """经文说：天数为一，地数为二；天数为三，地数为四；天数为五，地数为六；天数为七，地数为八；天数为九，地数为十。天数有五个，地数也有五个，五组数彼此配合，各有相合关系。天数合计二十五，地数合计三十，天地之数总共五十五。这是经文用来说明变化形成与幽微作用运行的数理象征。条目所引虞翻等人的解释，属于汉唐注家对经文的进一步阐释；郑玄、孔安国、侯果对河图洛书的解释也各不相同，不能合并成一种古说。经文列出数目，却没有在此处直接画出后世通行的黑白点河图。"""),
    Entry("河图", "刘牧《易数钩隐图》河图第四十九及图式说明", "宋刘牧《易数钩隐图》卷下第四十九图；参校《四库全书》本、道藏本数字文本及张克宾《论刘牧〈河图〉〈洛书〉说的架构与意蕴》（《周易研究》2024年第4期）。图版应以原书影像为准，本条文字不代替原图。", """图题：河图第四十九

图说原文：

“以五为主，六八为膝，二四为肩，左三右七，戴九履一。”

刘牧在其图书体系中，将九宫点数图称为“河图”，又称“龙图”。他在《易数钩隐图》卷下解释说：

“今《河图》相传于前代，其数自一至九，包四象、八卦之义而兼五行之数。《洛书》则惟五行生成之数也。”

此处“以五为主，六八为膝，二四为肩，左三右七，戴九履一”，是刘牧河图九数图的方位口诀。它与今天常见的“河图五十五数、洛书九宫四十五数”名称恰好相反，不能因图形相似便把名称改写成后世通行说法。

图式位置文字化记录：
上：九；
左：三；
中：五；
右：七；
下：一；
四角：二、四、六、八，其中“二四为肩、六八为膝”是原文方位描述。原图中点数的黑白、具体朝向及版面布局仍须以四库本或道藏本图版逐一核对，不能仅凭口诀重绘成所谓原刻图。

译文：以五为中心，六和八位于下部两侧，二和四位于上部两侧，三在左、七在右，九在上、一在下。

说明：这是北宋刘牧对图书名称和数图的特定安排，不是先秦《系辞》明文所规定的唯一河图形态。""",
    """**ChatGPT 翻译**

图题为“河图第四十九”。刘牧在自己的图书体系中，把九宫点数图称作“河图”，也称“龙图”。其图说称：以五为中心，六、八位于两侧下方，二、四位于两侧上方，三在左、七在右，九在上、一下在下。

刘牧认为，传承下来的河图数从一到九，包含四象、八卦的意义，并兼有五行之数；洛书则是五行生成数。这里的方位口诀属于北宋刘牧的河图九数图体系，与今天常见的河图、洛书名称安排正好不同，不能因为图形相似就把名称改成后世通行说法。

图中位置的文字记录为：上方九、左侧三、中央五、右侧七、下方一；四角为二、四、六、八。原图中点数的黑白、朝向和版面布局仍须以四库本或道藏本图版核对，不能只凭口诀重绘并冒称原刻图。

**说明：**这是北宋刘牧对河图名称和数图的特定安排，不是先秦《系辞》明文规定的唯一河图形态。""", chatGptTranslation = """图题是“河图第四十九”。刘牧用口诀描述图中数字位置：以五为中心，六和八在下部两侧，二和四在上部两侧，三在左、七在右，九在上、一在下。刘牧把九宫九数图称为河图或龙图。这种名称安排属于北宋刘牧自己的图书体系，与后世通行的河图、洛书名称配置不同。原图的点数黑白、朝向和版面细节仍须查看古籍影像，不能仅凭口诀把现代重绘图冒充原刻图。"""),
    Entry("河图", "刘牧《易数钩隐图》河图天地数、四象、八卦三图", "《易数钩隐图》卷下第五十至第五十二图；宋刘牧撰。图名据四库本及道藏本目录；相关结构参校张克宾《论刘牧〈河图〉〈洛书〉说的架构与意蕴》。", """图名依次为：

第五十图：河图天地数；
第五十一图：河图四象；
第五十二图：河图八卦。

刘牧并非只用第四十九图的九宫位置口诀解释河图。他另以三图逐层说明其数理结构：

1. “河图天地数第五十”：以一、二、三、四、五表示天地五生数，并联系太极分两仪的解释。
2. “河图四象第五十一”：在天地数基础上说明数与四象的配合。
3. “河图八卦第五十二”：进一步将数与八卦相配，以说明河图如何兼具四象、八卦及五行之义。

研究者对刘牧图式的概括是：九宫数图在刘牧体系中称河图、龙图；五行生成数图称洛书、龟书。河图由天地数、四象、八卦数图展开，不能只摘取一张九宫图便声称已经完整呈现刘牧的河图理论。

原图状态：本条仅据图名、图说及研究论文说明结构；当前录入内容没有将缺失或无法核实的古籍图版伪造为原图。后续应逐图补入可追溯的原书影像，并另附现代复原图，清楚标明二者。译文：暂无可靠逐图译文。""",
    """**ChatGPT 翻译**

第五十图题为“河图天地数”，第五十一图题为“河图四象”，第五十二图题为“河图八卦”。

刘牧并不只用第四十九图的九宫方位口诀解释河图，还用三幅图逐层说明数理结构：第一幅以一至五表示天地五种生数，并联系太极分化为两仪的解释；第二幅说明天地之数与四象的配合；第三幅进一步把数字与八卦相配，以说明河图如何兼具四象、八卦和五行的意义。

研究者概括刘牧体系时指出：九宫数图在其体系中称为河图、龙图；五行生成数图称为洛书、龟书。河图理论由天地数、四象和八卦数图逐层展开，不能只摘取一张九宫图，就说已经完整呈现刘牧的河图理论。

**图像状态：**本条依据图名、图说和研究论文说明结构，尚未把缺失或无法核实的古籍图版伪造成原图。""", chatGptTranslation = """本条列出刘牧《易数钩隐图》中的三幅图：第五十图“河图天地数”、第五十一图“河图四象”、第五十二图“河图八卦”。刘牧不是只用九宫位置口诀说明河图，而是依次讨论天地数、四象以及数字与八卦的配合。在他的体系中，九宫数图称河图、龙图，五行生成数图称洛书、龟书。因此，只摘取九宫图不能代表刘牧完整的河图理论。当前文字只是对图题和结构的说明，不是原图本身。"""),
    Entry("河图", "刘牧《易数钩隐图·龙图龟书论上》河洛源流辨说", "宋刘牧《易数钩隐图》卷下卷末《龙图龟书论上》节录；参校四库本《易数钩隐图》卷下及道藏本数字文本。此条为节录与内容提要，不冒充全文。", """刘牧先引《易》文：

“河出图，洛出书，圣人则之。”

继引《春秋纬》说：

“河以通乾，出天苞；洛以流坤，吐地符。河龙图发，洛龟书感。河图有九篇，洛书有六篇。”

刘牧随后讨论孔安国注《尚书·洪范》时所说的神龟负文、禹受洛书，以及汉代纬书关于龙马负图、神龟负书的叙事。他指出，《洪范》经文只说“天乃锡禹洪范九畴”，并没有直接写神龟负书而出的故事；孔氏注以神龟负文解释洛书，是注家的说明，不是《洪范》经文本身。

刘牧还辨析纬候图书传说的年代与可信度，认为不少关于黄帝、尧、舜、禹、汤、文王受图书的叙述，见于纬候之书，其形成与流传应谨慎考察。他以《系辞》“河出图，洛出书，圣人则之”为依据，主张图书传说应联系伏羲画卦的传统理解。

文献层次提示：
- 《系辞上传》：只有“河出图，洛出书，圣人则之”的简短经文。
- 《尚书·洪范》：记载天授禹“洪范九畴”，正文未叙神龟负书的具体场景。
- 孔安国传及后世注疏：将九畴与洛书、神龟负文联系起来。
- 《春秋纬》等纬书：发展河龙图、洛龟书及篇数等叙事。
- 刘牧：在北宋图书易学背景下重新组织河图、洛书数图，并对传说来源作辨析。

本条只录可确认的关键引文并概述论证次序。刘牧《龙图龟书论上》尚未逐字全录；残缺、异体或数字文本乱码处不据上下文补造。译文：暂无可靠逐段译文。""",
    """**ChatGPT 翻译**

刘牧先引用《易》文：“黄河出现图，洛水出现书，圣人取法于它们。”随后又引用纬书，说黄河通达乾道而出现天的征兆，洛水流行坤道而吐出地的符瑞；河中龙图显现，洛水龟书感应而成，并称河图有九篇、洛书有六篇。

刘牧接着讨论孔安国注释《尚书·洪范》时所说的神龟负文、禹接受洛书，以及汉代纬书中龙马负图、神龟负书的故事。他指出，《洪范》经文只说“上天赐给禹洪范九畴”，并没有直接记载神龟负书出现的情节；孔氏注用神龟负文解释洛书，是注家的说明，不是《洪范》经文本身。

刘牧还辨析纬书图书传说的年代与可信度，认为许多关于黄帝、尧、舜、禹、汤、文王接受图书的叙述出自纬候之书，其形成和流传应谨慎考察。他依据《系辞》“河出图，洛出书，圣人则之”，主张把图书传说与伏羲画卦的传统联系起来理解。

**文献层次：**《系辞》只有简短的河图洛书经文；《洪范》记载天授禹九畴，却未叙神龟负书的具体场景；孔安国传及后世注疏把九畴与洛书、神龟负文联系起来；纬书进一步发展龙图、龟书的故事；刘牧则在北宋图书易学背景下重新组织数图并辨析传说来源。

**校读边界：**本条只录可确认的关键引文并概述论证次序，并非刘牧《龙图龟书论上》的逐字全文。""", chatGptTranslation = """刘牧先引用《易》中的“河出图，洛出书，圣人则之”，又引纬书关于河龙图、洛龟书的说法，随后讨论《洪范》九畴、禹受洛书以及汉代纬书的相关叙事。他特别指出，《洪范》经文只说上天赐给禹洪范九畴，并没有直接叙述神龟背负洛书出现的场景；把九畴与神龟负文联系起来，是孔安国传等注释传统的解释。刘牧还提醒读者谨慎看待纬书中关于历代圣王受图书的传说，并在北宋图书易学背景下重新组织河图、洛书的数图体系。本条是节录与辨析，不是全文翻译。"""),
    Entry("洛书", "刘牧《易数钩隐图》洛书五行生数、成数及第五十五图", "《易数钩隐图》卷下第五十三至第五十五图；宋刘牧撰。参校四库本、道藏本及张克宾《论刘牧〈河图〉〈洛书〉说的架构与意蕴》。", """图名：
第五十三图：《洛书五行生数》；
第五十四图：《洛书五行成数》；
第五十五图：《十日生五行并相生》。

刘牧《洛书》图后问答：

“或问曰：洛书云：一曰水，二曰火，三曰木，四曰金，五曰土。则与龙图五行之数之位不偶者，何也？”

答曰：

“此谓陈其生数也。且虽则陈其生数，乃是已交之数也。下篇分土王四季，则备其成数矣。且夫洛书九畴，惟出于五行之数，故先陈其已交之生数，然后以土数足之，乃可见其成数也。”

第五十五图文字：

“天一、地六，地二、天七，天三、地八，地四、天九，天五、地十，合而生水、火、木、金、土。十日者，刚日也。相生者，金生水，水生木，木生火，火生土，土生金也。相尅者，金尅木，木尅土，土尅水，水尅火，火尅金也。”

说明：刘牧把一至五称作五行生数，再以“土数足之”说明成数；其《洛书》系统不是后世通行九宫四十五数图，而是五行生成数的解释系统。

译文：
洛书所列为水一、火二、木三、金四、土五，这是五行的生数。刘牧认为这些数已经处于交合关系之中；再以土数补足，便可说明五行成数。天一与地六、地二与天七、天三与地八、地四与天九、天五与地十相配，合而生成水、火、木、金、土。文中又列出五行相生、相克的次序。

校勘提示：四库本与道藏本数字文本在“已／巳”“四季／四时”“尅／克”等处有异文或规范字差异；此处保留通行底本字样并提示异文，不把数字化文本的差异误判为古本异文。""",
    """**ChatGPT 翻译**

图名依次为《洛书五行生数》《洛书五行成数》《十日生五行并相生》。

有人问：洛书说水为一、火为二、木为三、金为四、土为五，那么它与龙图中五行数字的位置不相配，这是为什么？

刘牧回答：这里陈列的是五行的生数。虽然列出的是生数，但这些数字已经处于交合状态。下篇把土分配到四季之中，就能补足成数。洛书九畴来自五行之数，所以先列已经交合的生数，再用土数补足，才能看出成数。

第五十五图又说：天一与地六、地二与天七、天三与地八、地四与天九、天五与地十相配，合而生成水、火、木、金、土。十日是刚日。五行相生的次序是金生水、水生木、木生火、火生土、土生金；相克的次序是金克木、木克土、土克水、水克火、火克金。

**说明：**刘牧把一至五称为五行生数，再以“土数足之”说明成数。这一洛书体系不是后世通行的九宫四十五数图，而是对五行生成数的解释。""", chatGptTranslation = """刘牧把水一、火二、木三、金四、土五称为五行生数，并认为还要以土数补足，才能说明五行成数。第五十五图将天一与地六、地二与天七、天三与地八、地四与天九、天五与地十分别配合，说明五行生成关系。文中又列出五行相生次序：金生水、水生木、木生火、火生土、土生金；以及相克次序：金克木、木克土、土克水、水克火、火克金。这是刘牧北宋图书易学体系中的解释，不应误认为先秦经文直接给出的图式。"""),
    Entry("河图", "《尚书·顾命》河图在东序", "《尚书·顾命》通行本；参校维基文库《尚书/顾命》及识典古籍所引孔传。此条只录河图所在陈设语境，不据此推定其图形。", """经文：

越玉五重：陈宝、赤刀、大训、弘璧、琬、琰，在西序；大玉、夷玉、天球、河图，在东序。

语境说明：

《顾命》叙周王临终顾命、陈列礼器与宝物。河图与大玉、夷玉、天球并列，记载其陈设于东序。经文没有说明河图的数目、点位、黑白符号，也没有说它就是后世九宫图或五十五数图。

孔安国传一类旧注将河图解释为“图出于河，帝王圣者之所受”；这属于注家对名物来源的解释，须与《顾命》经文本身分层。

译文：

陈列五重玉器：陈列宝物、赤刀、大训、弘璧、琬、琰于西边的序；大玉、夷玉、天球和河图陈列在东边的序。

校读提示：

“东序”是宫室中的陈设方位。此处“河图”作为被陈列的珍贵物品出现；经文没有提供图像或图说。不同版本的字形、断句应以影印本核验，不能用后世河图图式补足经文未载信息。""",
    """**ChatGPT 翻译**

经文说，五重玉器以及宝物、赤刀、大训、弘璧、琬、琰陈列在西边的序；大玉、夷玉、天球和河图陈列在东边的序。

**语境说明：**《顾命》叙述周王临终时的顾命仪式，以及礼器和珍宝的陈列。河图与大玉、夷玉、天球并列，记载其陈设在东序。经文没有说明河图的数字、点位、黑白符号，也没有说它就是后世九宫图或五十五数图。

旧注如孔安国传把河图解释为“图出于河，为帝王圣者所受”，这是注家对名物来源的说明，须与《顾命》经文本身区分。

**校读说明：**“东序”指宫室中的陈设方位。这里的“河图”是被陈列的珍贵物品；经文没有提供图像或图说。""", chatGptTranslation = """《顾命》记载，五重玉器、宝物、赤刀、大训、弘璧、琬和琰陈列在西边的序；大玉、夷玉、天球和河图则陈列在东边的序。这段话处在周王临终顾命、陈列礼器珍宝的仪式场景中。经文只说明河图作为珍贵物品陈列在东序，并没有交代它的数字、点位、黑白符号或具体图形。因此，不能用后世九宫图或五十五数图补充经文未曾记载的细节。"""),
    Entry("河图", "《论语·子罕》河不出图", "《论语·子罕第九》九之九；参校维基文库《论语/子罕第九》及《论语集释》相关古注。", """经文：

子曰：“凤鸟不至，河不出图，吾已矣夫！”

古注辑要：

《论语集解》孔安国注：“圣人受命，则凤鸟至，河出图。今天无此瑞。吾已矣夫者，伤不得见也。河图，八卦是也。”

《论语集注》解释凤鸟、河图皆为圣王之瑞，指出孔子感叹当世无圣王之征兆。

说明：

经文把“凤鸟不至”与“河不出图”并举，表达孔子对圣王之治及其祥瑞征兆不再出现的感慨。孔安国注将河图解释为八卦，是古注的一种解释，不等于《论语》正文直接说明河图图形就是八卦。

译文：

孔子说：“凤凰不出现，黄河也不出现图，我这一生大概就到此为止了吧！”

校读提示：

本章古注对“吾已矣夫”的语气有“伤不得见”等解释。此处译文按语境作工作译文，不把后世神话叙事写进经文。""",
    """**ChatGPT 翻译**

孔子说：“凤凰没有出现，黄河也没有出现图，我这一生大概就到此为止了吧！”

**古注译意：**孔安国认为，圣人受命时，凤凰会出现，黄河会出现图；如今没有这些祥瑞，孔子因此感叹自己恐怕不能亲眼见到圣王之治。孔安国又把河图解释为八卦。

**说明：**经文把“凤鸟不至”与“河不出图”并列，表达孔子对圣王之治及其祥瑞征兆不再出现的感慨。孔安国注将河图解释为八卦，是古注的一种解释，不等于《论语》正文直接说明河图的图形就是八卦。""", chatGptTranslation = """孔子说：“凤凰没有出现，黄河也没有出现图，我这一生大概就到此为止了吧！”这句话把凤凰出现和黄河出图并列为圣王祥瑞的征兆，表达孔子对当时没有圣王之治的感慨。孔安国注把河图解释为八卦，这是古注的一种解释，不是《论语》正文直接说明河图图形就是八卦。译文按上下文表达感叹语气，不把后世神话叙事混入经文。"""),
    Entry("洛书", "《尚书·洪范》天锡禹洪范九畴原文", "《尚书·洪范》通行本；参校维基文库《尚书/洪范》。本条录九畴总纲及第一畴相关原文，不冒充《洪范》全篇。", """经文：

箕子乃言曰：“我闻在昔，鲧堙洪水，汩陈其五行；帝乃震怒，不畀洪范九畴，彝伦攸斁。鲧则殛死，禹乃嗣兴，天乃锡禹洪范九畴，彝伦攸叙。”

“初一曰五行，次二曰敬用五事，次三曰农用八政，次四曰协用五纪，次五曰建用皇极，次六曰乂用三德，次七曰明用稽疑，次八曰念用庶征，次九曰向用五福，威用六极。”

第一畴“五行”：

“一曰水，二曰火，三曰木，四曰金，五曰土。水曰润下，火曰炎上，木曰曲直，金曰从革，土爰稼穑。润下作咸，炎上作苦，曲直作酸，从革作辛，稼穑作甘。”

译文：

箕子说：“我听说从前鲧堵塞洪水，扰乱了五行；上帝因此震怒，没有赐给他洪范九畴，常理秩序于是败坏。鲧被处死，禹继而兴起，上天于是赐给禹洪范九畴，常理秩序才得以建立。”

九畴依次为：五行、五事、八政、五纪、皇极、三德、稽疑、庶征、五福与六极。第一畴列水、火、木、金、土五行，并说明其性质及对应滋味。

文献层次辨析：

《洪范》经文确实说“天乃锡禹洪范九畴”，并列九类纲目；经文在这段叙述中没有出现“神龟负书”“龟背九宫”或九宫数字排列。孔安国传将九畴与洛书、神龟负文相联系，是后世解释传统的重要环节，但不可把注语改写成经文。

校勘提示：

通行数字文本列有“惟／维”“堙／陻”“汩／曰”等异文线索。此处保留通行底本文字并列出需核验处；具体异文归属仍应对照熹平石经、《史记·宋微子世家》及各版本影印页。""",
    """**ChatGPT 翻译**

**一、经文译文**

箕子说：“我听说从前鲧堵塞洪水，扰乱了五行；上帝因此震怒，没有赐给他洪范九畴，常理秩序于是败坏。鲧被处死，禹继而兴起，上天于是赐给禹洪范九畴，常理秩序才得以建立。”

九畴依次为：第一是五行，第二是敬慎运用五事，第三是妥善施行八政，第四是协调运用五纪，第五是建立皇极，第六是治理时运用三德，第七是明察并运用稽疑，第八是思考并运用庶征，第九是向往五福、警戒六极。

**二、五行译文**

第一是水，第二是火，第三是木，第四是金，第五是土。水的性质是润泽而向下，火的性质是炎热而向上，木的性质是可弯曲也可伸直，金属可以顺应加工而改变形状，土适宜耕种收获。润下产生咸味，炎上产生苦味，曲直产生酸味，从革产生辛味，稼穑产生甘味。

**三、文献层次辨析**

《洪范》经文确实说“上天赐给禹洪范九畴”，并列出九类纲目；但这里没有出现“神龟负书”“龟背九宫”或九宫数字排列。孔安国传将九畴与洛书、神龟负文联系起来，是后世解释传统的重要环节，不能把注语改写成经文。""", chatGptTranslation = """箕子说：“我听说从前鲧堵塞洪水，扰乱了五行；上帝因此震怒，没有赐给他洪范九畴，常理秩序于是败坏。鲧被处死，禹继而兴起，上天于是赐给禹洪范九畴，常理秩序才得以建立。”九畴依次为五行、五事、八政、五纪、皇极、三德、稽疑、庶征，以及五福与六极。第一畴五行列出水、火、木、金、土，并分别说明其性质及所对应的滋味。《洪范》经文本身没有在这段话中叙述神龟负书或九宫数字排列；这些内容属于后世注释和图书解释传统。"""),
    Entry("河图", "《礼记·礼运》河出马图", "《礼记·礼运》相关章句；本条只作图书文献见证，不将汉代礼学篇章直接等同于先秦《系辞》原义。", """相关原文：

故天不爱其道，地不爱其宝，人不爱其情。故天降膏露，地出醴泉，山出器车，河出马图，凤凰麒麟皆在郊棷，龟龙在宫沼，其余鸟兽之卵胎，皆可俯而窥也。

译文：

所以天不吝惜其道，地不吝惜其宝，人不吝惜其情。于是天降甘露，地涌醴泉，山中出现珍异器车，黄河出现马图，凤凰、麒麟出现在郊野，龟龙出现在宫中池沼，其余鸟兽的卵与胎也都可以俯身观察。

说明：

《礼运》以“河出马图”列入理想政治秩序下祥瑞纷呈的描写。它与《系辞》的“河出图，洛出书”及《顾命》的“河图在东序”可互相参照，但各篇语境不同，不能据此把“马图”“河图”所有细节强行统一。

校读边界：

“马图”在此是瑞应叙述中的名物；本段未给出点数、方位或图样。""",
    """**ChatGPT 翻译**

《礼运》说：天不吝惜自己的道，地不吝惜自己的宝，人不吝惜自己的情。因此，天降下甘露，地涌出醴泉，山中出现珍异器车，黄河出现马图，凤凰和麒麟出现在郊野，龟与龙出现在宫中池沼，其余鸟兽的卵和胎也都可以俯身观察。

**说明：**《礼运》把“河出马图”放在理想政治秩序下祥瑞纷呈的描写中。它可以与《系辞》的“河出图，洛出书”及《顾命》的“河图在东序”互相参照，但各篇语境不同，不能据此把“马图”和“河图”的所有细节强行统一。

**校读边界：**这里的“马图”是祥瑞叙述中的名物；本段没有给出点数、方位或图样。""", chatGptTranslation = """《礼运》说：天不吝惜自己的道，地不吝惜自己的宝，人不吝惜自己的情。因此，天降下甘露，地涌出醴泉，山中出现珍异器车，黄河出现马图，凤凰和麒麟出现在郊野，龟与龙出现在宫中池沼，其余鸟兽的卵和胎也都可以俯身观察。《礼运》把“河出马图”放在理想政治秩序下祥瑞纷呈的描写中。它可以与《系辞》的“河出图，洛出书”及《顾命》的“河图在东序”互相参照，但各篇语境不同，不能据此把“马图”和“河图”的细节强行统一。"""),
    Entry("河图", "河图、洛书先秦经文见证与后世图式的界线", "综合《尚书·顾命》《论语·子罕》《礼记·礼运》《周易·系辞上传》及宋元图书易学文献。", """先秦及传世经典中常被并举的文字见证：

一、《尚书·顾命》：“大玉、夷玉、天球、河图，在东序。”语境是宫室陈设珍宝。

二、《论语·子罕》：“凤鸟不至，河不出图，吾已矣夫！”语境是孔子感叹圣王祥瑞不至。

三、《礼记·礼运》：“河出马图。”语境是理想治世祥瑞并见。

四、《周易·系辞上传》：“河出图，洛出书，圣人则之。”语境是圣人取法神物、天地变化与天象地法。

这些文字能够证明“河图”“洛书”及“马图”观念在经典文献中的存在，却没有直接提供后世黑白点图、五十五数图或九宫四十五数图的完整形态。

后世解释的层次：

- 汉代注家、纬书传统：逐渐将河图与伏羲画卦、洛书与禹叙九畴相联系，并发展龙马负图、神龟负书等叙事。
- 北宋刘牧：以九数图为河图、五行生成数为洛书，建立其特定的图书数理体系。
- 南宋以后：围绕河九洛十、河十洛九展开辨论，后世通行图名逐渐定型。
- 元明清诸家：既有沿用通行图式者，也有质疑图书数图是否能由经典原文直接推出者。

编辑原则：

凡经文只记名称而未载图样者，只录名称与语境；凡注家或后世学者补出的图式，必须标明作者、时代、书名与图次。现代复原图不得冒充原刻图。""",
    """**ChatGPT 翻译**

先秦及传世经典中，有四处常被用来讨论河图、洛书：

1. 《尚书·顾命》说“大玉、夷玉、天球、河图，在东序”，语境是宫室陈列珍宝。
2. 《论语·子罕》说“凤鸟不至，河不出图，吾已矣夫”，语境是孔子感叹圣王祥瑞不再出现。
3. 《礼记·礼运》说“河出马图”，语境是理想治世中祥瑞并见。
4. 《周易·系辞上传》说“河出图，洛出书，圣人则之”，语境是圣人取法神物、天地变化和天象地法。

这些文字能证明河图、洛书及马图观念存在于经典文献中，却没有直接提供后世黑白点图、五十五数图或九宫四十五数图的完整形态。

**后世解释层次：**汉代注家和纬书逐渐把河图与伏羲画卦、洛书与禹叙九畴联系起来；北宋刘牧以九数图为河图、五行生成数图为洛书；南宋以后围绕河九洛十、河十洛九展开争论；元明清学者既有沿用通行图式者，也有质疑这些图式能否由经典原文直接推出者。

**编辑原则：**经文只记名称而未载图样时，只录名称与语境；注家或后世学者补出的图式，必须标明作者、时代、书名和图次。现代复原图不得冒充原刻图。""", chatGptTranslation = """《顾命》说河图陈列在东序；《论语》以“河不出图”表达祥瑞不至；《礼运》提到“河出马图”；《系辞》说“河出图，洛出书，圣人则之”。这些文字证明河图、洛书和马图观念存在于传世经典中，但并没有直接给出后世黑白点图、五十五数图或九宫四十五数图的完整形态。汉代注家和纬书逐渐把河图与伏羲画卦、洛书与禹陈九畴联系起来；北宋刘牧又建立自己的河图、洛书数图体系；南宋以后相关图名和配置继续争论。整理时必须区分经文、注疏、纬书传说和后世图式。"""),
    Entry("河图", "顾炎武《易学象数论·图书一》河洛图书辨", "清顾炎武《易学象数论》卷中《图书一》；参校维基文库本。作为后世批判性解释史材料，不代替先秦经文。", """顾炎武开篇列举六经中图书相关记载，指出《顾命》“河图在东序”、《论语》“河不出图”、《礼运》“河出马图”及《系辞》“河出图，洛出书，圣人则之”，并据此讨论“图书”原义。

其论证重点之一，是提醒读者回到各篇经文语境考察“图”“书”之义，不应先认定后世某一种九宫或五十五数图，再反过来把该图式强加给所有早期记载。

观点摘要：

1. 顾炎武将《顾命》的河图放在东序陈列宝物的语境中解释。
2. 他对把河图、洛书直接认定为固定数图的做法提出质疑。
3. 他指出后世关于九数、十数、九宫等说法纷纭，不能仅凭经典只言片语就视为定论。

说明：

此条为顾炎武论证内容摘要，不是《易学象数论》全文录入。顾氏对“图”“书”的解释属于清代考辨意见，应与刘牧、朱熹等人的图书体系并列呈现，不在典藏正文中裁定为唯一结论。译文：暂无可靠逐段译文。""",
    """**ChatGPT 翻译**

顾炎武开篇列举六经中与图书有关的记载，包括《顾命》的“河图在东序”、《论语》的“河不出图”、《礼运》的“河出马图”以及《系辞》的“河出图，洛出书，圣人则之”，并据此讨论“图书”原本的含义。

他的一个重要论点是：应回到每篇经文的具体语境来考察“图”和“书”的含义，不应先认定后世某一种九宫图或五十五数图，再把这种图式反过来强加给所有早期记载。

**观点摘要：**
1. 顾炎武把《顾命》的河图放在东序陈列宝物的语境中解释。
2. 他质疑把河图、洛书直接认定为固定数图的做法。
3. 他指出后世关于九数、十数、九宫的说法纷杂，不能仅凭经典中的只言片语便视为定论。

**说明：**本条是顾炎武论证内容的摘要，不是《易学象数论》全文。顾氏观点属于清代考辨意见，应与刘牧、朱熹等人的图书体系并列呈现。""", chatGptTranslation = """顾炎武列举《顾命》《论语》《礼运》和《系辞》中有关河图、洛书的记载，并据此讨论“图书”原本的含义。他强调应回到各篇经文的具体语境，不应先认定后世某一种九宫图或五十五数图，再把它强加给所有早期记载。他对河图、洛书固定数图的解释提出质疑，也指出后世九数、十数和九宫等说法纷繁，不能仅凭经典中的只言片语便视为定论。本条是顾炎武观点的内容转述，不是《易学象数论》全文翻译；其意见属于清代考辨，应与刘牧、朱熹等人的体系并列研究。"""),
    Entry("河图", "河图、洛书名称与九数十数的历史异说", "据刘牧《易数钩隐图》、元吴澄《易纂言外翼》、宋代图书易学文献及张克宾《论刘牧〈河图〉〈洛书〉说的架构与意蕴》整理。", """河图、洛书的数图名称在历史上并非始终只有一种安排。

一、刘牧的安排

北宋刘牧《易数钩隐图》：
- 河图／龙图：九宫九数图，口诀为“以五为主，六八为膝，二四为肩，左三右七，戴九履一”。
- 洛书／龟书：五行生成数系统，先列一至五的五行生数，再以土数补足成数。

刘牧自述：“今《河图》相传于前代，其数自一至九，包四象、八卦之义而兼五行之数。《洛书》则惟五行生成之数也。”

二、后世通行安排

后来广泛流传的系统则把名称反过来：
- 河图：天地五行生成数，通常以一六、二七、三八、四九、五十相配，总数五十五。
- 洛书：九宫九数图，总数四十五，常见口诀为“戴九履一，左三右七，二四为肩，六八为足，五居中央”。

三、元代吴澄的辨析

吴澄《易纂言外翼》引刘牧之说，指出刘牧以九为河图、十为洛书，与邵氏系统以十为河图、九为洛书不同；并进一步讨论刘牧所传图书名称是否因传授过程中未能辨明而发生差互。此说属于后世学者的考辨，不应反写成刘牧本人承认名称错误。

四、整理结论

“九数图”“十数图”是描述数图结构的相对中性称呼；“河图”“洛书”则须注明所采用的作者与时代体系。引用图式时应同时写明：
- 作者或传承系统；
- 文献书名、卷次、图次；
- 图名及其数位；
- 采用的是刘牧系统还是后世通行系统。

本条为异说梳理，不裁定哪一套名称在所有历史时期都唯一正确。译文：暂无可靠逐段译文。""",
    """**ChatGPT 翻译**

河图、洛书的数图名称，在历史上并非始终只有一种安排。

**一、刘牧的安排**

北宋刘牧《易数钩隐图》把九宫九数图称为河图或龙图；把五行生成数系统称为洛书或龟书。刘牧说，河图的数字从一到九，包含四象、八卦的意义并兼有五行之数；洛书则是五行生成数。

**二、后世通行安排**

后来广泛流传的系统把名称反过来：河图指天地五行生成数，通常将一六、二七、三八、四九、五十相配，总数五十五；洛书指九宫九数图，总数四十五，常见口诀为“戴九履一，左三右七，二四为肩，六八为足，五居中央”。

**三、吴澄的辨析**

元代吴澄指出，刘牧以九数为河图、十数为洛书，与邵氏系统以十数为河图、九数为洛书的安排不同，并进一步讨论名称是否在传授过程中发生差异。这属于后世学者的考辨，不能说成刘牧本人承认自己的名称有误。

**四、整理结论**

“九数图”“十数图”是描述结构的中性名称；“河图”“洛书”则必须注明所采用的作者和时代体系。引用图式时应标明作者或传承系统、书名卷次图次、图名和数位，以及采用刘牧系统还是后世通行系统。本条梳理异说，不裁定哪套名称在所有历史时期都唯一正确。""", chatGptTranslation = """河图、洛书的名称和数图关系在历史上发生过变化。北宋刘牧的体系把九宫九数图称为河图，把五行生成数系统称为洛书；后世通行体系则把名称反过来，通常以五十五数为河图、九宫四十五数为洛书。因此，不能把今天常见的名称配置直接套用到刘牧《易数钩隐图》中，也不能把刘牧的配置说成自古不变。南宋以后，围绕河九洛十还是河十洛九持续出现讨论，后世通行配置逐渐确立；具体传承环节仍须逐家核对原文。"""),
    Entry("洛书", "洛书九宫数图（后世通行图式）", "现代常见九宫排列示意。它是后世定型并广泛流传的图式，不应倒推为先秦《尚书·洪范》或《周易·系辞》原图。", """通行排列：

四　九　二
三　五　七
八　一　六

横列、纵列与两条对角线的和均为十五。此图常用于后世术数、历法与方位解释；其具体定型过程须与先秦“洛书”文字记载分开考察。

图像说明：上方为本应用绘制的现代矢量示意图，仅呈现通行九宫数字排列，不是古籍原图，也不代表已经证明其源自先秦。""", chatGptTranslation = """通行的洛书九宫排列如下：

四、九、二；
三、五、七；
八、一、六。

横向、纵向以及两条对角线上的三个数字相加，结果都为十五。这幅图常用于后世术数、历法和方位解释；其具体定型过程必须与先秦文献中“洛书”的文字记载分开考察。

**图像说明：**这是现代绘制的矢量示意图，只呈现后世通行的九宫数字排列，不是古籍原图，也不能证明它源自先秦。"""),
    Entry("洛书", "刘牧《洛书五行生数》第五十三图", "《易数钩隐图》卷下第五十三图；北宋刘牧图书易学体系。图式依据山东大学《周易研究》2024年第4期论文所刊图13及文中释读重绘。", """刘牧将洛书理解为五行生成数系统。其生数为：

一曰水，二曰火，三曰木，四曰金，五曰土。

这五个数是五行生数。刘牧强调此数属于“已交之数”，与其河图系统所表达的未交之象不同。此处按原文次第列出，不擅自补定图中未能由文字确认的方位。

图像说明：应用绘制的现代矢量资料图，展示五行与生数对应关系；不是古籍原图。原论文图13可用于对照核查。""", chatGptTranslation = """刘牧把洛书解释为五行生成数系统。五行生数依次为：水一、火二、木三、金四、土五。

这五个数字是五行的生数。刘牧强调这些数字属于已经交合的数，与其河图系统所表达的未交之象不同。这里按原文次序列出，不擅自补定无法由文字确认的图中方位。

**图像说明：**应用绘制的现代矢量资料图，展示五行与生数的对应关系，不是古籍原图。"""),
    Entry("洛书", "刘牧《洛书五行成数》第五十四图", "《易数钩隐图》卷下第五十四图；依据刘牧“以土数足之”的解释及山东大学《周易研究》2024年第4期论文图14重绘。", """刘牧以天五与五行生数相合，说明五行成数：

水：一与五合，成数六；
火：二与五合，成数七；
木：三与五合，成数八；
金：四与五合，成数九；
土：五与五合，成数十。

因此，五行成数为六、七、八、九、十。刘牧把生数与成数分图表达，并未在《易数钩隐图》中将二图合成一张完整的洛书总图。

图像说明：应用绘制的现代矢量资料图，以配对方式呈现生数、成数；不冒称原刻图。""", chatGptTranslation = """刘牧以天数五与五行生数相配，说明五行成数：水由一与五相合，成数为六；火由二与五相合，成数为七；木由三与五相合，成数为八；金由四与五相合，成数为九；土由五与五相合，成数为十。

因此，五行成数为六、七、八、九、十。刘牧把生数和成数分图表达，并未在《易数钩隐图》中将两图合成一张完整的洛书总图。

**图像说明：**这是应用绘制的现代矢量资料图，以配对方式呈现生数和成数，不冒称原刻图。"""),
    Entry("洛书", "刘牧《十日生五行并相生》第五十五图", "《易数钩隐图》卷下第五十五图及其图后文字；通行数字文本据《四库全书》本与道藏本相关整理页面互校。", """天一、地六，地二、天七，天三、地八，地四、天九，天五、地十，合而生水、火、木、金、土。

十日者，刚日也。

相生者：金生水，水生木，木生火，火生土，土生金也。

相克者：金克木，木克土，土克水，水克火，火克金也。

说明：本段把天地数配合与五行生成联系起来。相生次序和相克次序照录原文；“十日”一词的具体训释不在此处擅自扩写。""", chatGptTranslation = """天一与地六、地二与天七、天三与地八、地四与天九、天五与地十相配，合而生成水、火、木、金、土。

十日，是刚日。

五行相生的次序是：金生水，水生木，木生火，火生土，土生金。

五行相克的次序是：金克木，木克土，土克水，水克火，火克金。

**说明：**本段把天地数的配合与五行生成联系起来。相生和相克的次序依原文翻译；“十日”一词的具体训释不在此处擅自扩写。"""),
    Entry("洛书", "刘牧《易数钩隐图》洛书说", "《易数钩隐图》卷下“河图第四十九”图后问答；宋刘牧撰。原文可据《四库全书》本卷下及《道藏》本相关数字整理核对。", """或问曰：“洛书云：一曰水，二曰火，三曰木，四曰金，五曰土。则与龙图五行之数之位不偶者，何也？”

答曰：“此谓陈其生数也。且虽则陈其生数，乃是已交之数也。下篇分土王四季，则备其成数矣。且夫洛书、九畴，惟出于五行之数，故先陈其已交之生数，然后以土数足之，乃可见其成数也。”

整理说明：此处明确呈现刘牧对洛书的解释框架——先陈五行生数，再以中五配合说明成数。不同版本在“已／巳”“四季／四时”等字样上存在数字文本差异，正式校勘时须以具体底本影像为准。""", chatGptTranslation = """有人问：“洛书说水为一、火为二、木为三、金为四、土为五，那么它与龙图中五行数字的位置不相配，这是为什么？”

刘牧回答：“这里说的是五行生数。虽然列出的是生数，但这些数字已经处于交合状态。下篇把土分配到四季之中，就补足了成数。洛书九畴来自五行之数，所以先列已经交合的生数，然后用土数补足，才能看出成数。”

**整理说明：**这段文字明确呈现刘牧对洛书的解释：先列五行生数，再用中五配合说明成数。不同数字文本在“已／巳”“四季／四时”等处有差异，正式校勘应以具体底本影像为准。"""),
    Entry("洛书", "刘牧《龙图龟书论上》洛书相关论述", "《易数钩隐图》卷下卷末《龙图龟书论上》节录；宋刘牧撰。", """《易》曰：“河出图，洛出书，圣人则之。”

刘牧在论述中讨论了《尚书·洪范》九畴、汉儒关于禹受洛书的解释，以及河图、洛书在易学体系中的关系。他并不简单把九畴文本等同于一张固定的九宫数字图，而是试图说明圣人如何取法图书之象数。

其论述中有一段关键判断：“今河图相传于前代，其数自一至九，包四象八卦之义，而兼五行之数；洛书则惟五行生成之数也。”

校读提示：刘牧的说法属于北宋图书易学的理论建构。此处是刘牧本人的解释，不应改写成先秦经文的原始定义。关于神龟负书、伏羲画卦等叙事，须分别标记为经文、注疏或纬书传说。""", chatGptTranslation = """《易》说：“黄河出现图，洛水出现书，圣人取法于它们。”

刘牧在论述中讨论《尚书·洪范》九畴、汉儒关于禹接受洛书的解释，以及河图、洛书在易学体系中的关系。他并未简单地把九畴文本等同于一张固定的九宫数字图，而是试图说明圣人如何取法图书的象数。

刘牧有一段关键论述称：传承下来的河图数字从一到九，包含四象、八卦的意义，并兼有五行之数；洛书则只是五行生成数。

**校读提示：**刘牧的说法属于北宋图书易学的理论建构，是刘牧本人的解释，不应改写成先秦经文的原始定义。神龟负书、伏羲画卦等叙事，须分别标明属于经文、注疏还是纬书传说。"""),
    Entry("洛书", "河图九数、洛书十数的名称易位", "依据刘牧《易数钩隐图》、李觏《删定易图论》相关材料及张克宾《论刘牧〈河图〉〈洛书〉说的架构与意蕴》（《周易研究》2024年第4期）。", """名称与数图关系曾发生重要变化：

刘牧系统：
• 河图：九宫九数图，四十五数；
• 洛书：五行生成数系统，天地十数合计五十五。

后世通行系统：
• 河图：天地五行生成数，五十五数；
• 洛书：九宫数图，四十五数。

因此，不能把今天常见的“河图五十五、洛书四十五”直接套用到刘牧《易数钩隐图》上，也不能把刘牧的名称配置误说成自古不变。

学术说明：北宋刘牧对图名作了与此前传统不同的配置；南宋以后围绕河九洛十、河十洛九形成持续争论，后经蔡元定、朱熹等人的论说，后世通行配置逐渐确立。具体传承环节仍应依各家原文分别核验。""", chatGptTranslation = """河图、洛书的名称与数图关系在历史上发生过重要变化。

刘牧系统中，河图是九宫九数图，合计四十五数；洛书是五行生成数系统，天地十数合计五十五。后世通行系统则把名称反过来：河图是天地五行生成数，共五十五数；洛书是九宫数图，共四十五数。

因此，不能把今天常见的“河图五十五、洛书四十五”直接套用到刘牧《易数钩隐图》中，也不能把刘牧的名称配置说成自古以来从未变化。

**学术说明：**北宋刘牧对图名作了与此前传统不同的配置。南宋以后，河九洛十与河十洛九的争论持续发展，蔡元定、朱熹等人的论说推动后世通行配置逐渐确立。具体传承环节仍须依各家原文分别核验。"""),
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
《洪范》原文确有“九畴”及五行次第；《周易·系辞上传》另有“河出图，洛出书，圣人则之”；后世注家逐渐将二者关联，并发展出神龟负书、九宫数位等解释。三者之间存在解释史联系，但不是同一段原文，也不能仅凭“九畴”二字证明《洪范》原本就附有今天通行的九宫图。""", chatGptTranslation = """《洪范》记载“上天赐给禹洪范九畴”，并列出九类治国纲目：五行、五事、八政、五纪、皇极、三德、稽疑、庶征，以及五福与六极。

**文献层次说明：**《洪范》原文确有“九畴”及五行次第；《周易·系辞上传》另有“河出图，洛出书，圣人则之”；后世注家逐渐把二者联系起来，并发展出神龟负书、九宫数位等解释。三者之间存在解释史联系，但不是同一段原文，也不能仅凭“九畴”二字证明《洪范》原本就附有今天通行的九宫图。"""),
    Entry("洛书", "《周易·系辞上传》河图洛书原文", "通行本《系辞上传》第十一章；原文提及河图、洛书，没有直接列出九宫数字。", """是故天生神物，圣人则之；天地变化，圣人效之；天垂象，见吉凶，圣人象之；河出图，洛出书，圣人则之。

译文：因此，上天产生神妙之物，圣人取法于它；天地发生变化，圣人仿效它；天上显示征象并呈现吉凶，圣人据此取象；黄河出现图，洛水出现书，圣人取法于它们。

校勘提示：此段是“河图”“洛书”观念的重要传世文献依据，但不能仅凭这句话断定后世九宫数图的具体形态在此时已经固定。""", chatGptTranslation = """因此，上天产生神妙之物，圣人取法于它；天地发生变化，圣人仿效它；天上显示征象并呈现吉凶，圣人据此取象；黄河出现图，洛水出现书，圣人取法于它们。

**校勘提示：**这段是河图、洛书观念的重要传世文献依据，但不能仅凭这句话断定后世九宫数图的具体形态在当时已经固定。"""),
    Entry("相关古籍", "《尚书·洪范》九畴", "《尚书》篇章；本条补入《洪范》全篇通行数字文本，并保留维基文库页面所列异文线索。底本为维基文库《尚书/洪范》页面，原文与异文须以所列版本影像继续核验。", """《洪范》全文（通行数字文本整理）：

惟十有三祀，王访于箕子。

王乃言曰：“呜呼！箕子。惟天阴骘下民，相协厥居，我不知其彝伦攸叙。”

箕子乃言曰：“我闻在昔，鲧堙洪水，汩陈其五行；帝乃震怒，不畀洪范九畴，彝伦攸斁。鲧则殛死，禹乃嗣兴，天乃锡禹洪范九畴，彝伦攸叙。”

初一曰五行，次二曰敬用五事，次三曰农用八政，次四曰协用五纪，次五曰建用皇极，次六曰乂用三德，次七曰明用稽疑，次八曰念用庶征，次九曰向用五福，威用六极。

一、五行：一曰水，二曰火，三曰木，四曰金，五曰土。水曰润下，火曰炎上，木曰曲直，金曰从革，土爰稼穑。润下作咸，炎上作苦，曲直作酸，从革作辛，稼穑作甘。

二、五事：一曰貌，二曰言，三曰视，四曰听，五曰思。貌曰恭，言曰从，视曰明，听曰聪，思曰睿。恭作肃，从作乂，明作晢，聪作谋，睿作圣。

三、八政：一曰食，二曰货，三曰祀，四曰司空，五曰司徒，六曰司寇，七曰宾，八曰师。

四、五纪：一曰岁，二曰月，三曰日，四曰星辰，五曰历数。

五、皇极：皇建其有极，敛时五福，用敷锡厥庶民。惟时厥庶民于汝极，锡汝保极。凡厥庶民，无有淫朋；人无有比德，惟皇作极。凡厥庶民，有猷有为有守，汝则念之。不协于极，不罹于咎，皇则受之。而康而色，曰：“予攸好德。”汝则锡之福。时人斯其惟皇之极。无虐茕独，而畏高明。人之有能有为，使羞其行，而邦其昌。凡厥正人，既富方谷，汝弗能使有好于而家，时人斯其辜。于其无好德，汝虽锡之福，其作汝用咎。无偏无陂，遵王之义；无有作好，遵王之道；无有作恶，遵王之路。无偏无党，王道荡荡；无党无偏，王道平平；无反无侧，王道正直。会其有极，归其有极。曰皇极之敷言，是彝是训，于帝其训。凡厥庶民，极之敷言，是训是行，以近天子之光。曰天子作民父母，以为天下王。

六、三德：一曰正直，二曰刚克，三曰柔克。平康正直，强弗友刚克，燮友柔克；沈潜刚克，高明柔克。惟辟作福，惟辟作威，惟辟玉食；臣无有作福、作威、玉食。臣之有作福、作威、玉食，其害于而家，凶于而国。人用侧颇僻，民用僭忒。

七、稽疑：择建立卜筮人，乃命卜筮。曰雨，曰霁，曰圛，曰雺，曰克，曰贞，曰悔。凡七卜，五占用，二衍忒。立时人作卜筮，三人占，则从二人之言。汝则有大疑，谋及乃心，谋及卿士，谋及庶人，谋及卜筮。汝则从，龟从，筮从，卿士从，庶民从，是之谓大同；身其康强，子孙其逢吉。汝则从，龟从，筮从，卿士逆，庶民逆：吉。卿士从，龟从，筮从，汝则逆，庶民逆：吉。庶民从，龟从，筮从，汝则逆，卿士逆：吉。汝则从，龟从，筮逆，卿士逆，庶民逆：作内，吉；作外，凶。龟筮共违于人：用静，吉；用作，凶。

八、庶征：曰雨，曰旸，曰燠，曰寒，曰风。曰时五者来备，各以其叙，庶草蕃庑。一极备凶，一极无凶。曰休征：曰肃，时雨若；曰乂，时旸若；曰晢，时燠若；曰谋，时寒若；曰圣，时风若。曰咎征：曰狂，恒雨若；曰僭，恒旸若；曰豫，恒燠若；曰急，恒寒若；曰雺，恒风若。曰王省惟岁，卿士惟月，师尹惟日。岁月日时无易，百谷用成，乂用明，俊民用章，家用平康。日月岁时既易，百谷用不成，乂用昏不明，俊民用微，家用不宁。庶民惟星，星有好风，星有好雨。日月之行，则有冬有夏；月之从星，则以风雨。

九、五福：一曰寿，二曰富，三曰康宁，四曰攸好德，五曰考终命。六极：一曰凶短折，二曰疾，三曰忧，四曰贫，五曰恶，六曰弱。

异文与校读说明：
1. 篇名有“洪范／鸿范”异写。
2. 开篇“惟／维”“王乃／王迺”“鲧堙／鲧陻”“汩／曰”“不／弗”“斁／释”等，维基文库页面列有不同传本异文。
3. “五事”末项及“庶征”等处，古文字、传本和后世校释存在差异；本条通行正文不把所有异文混入正文，异文另列。
4. “皇极”篇幅较长，原文中的“好德”“无偏无党”等语保留连续上下文；本条不以现代标点作为古本原貌。
5. 本篇与《周易·系辞上传》“河出图，洛出书”并非同一段原文；九畴与洛书九宫的对应属于后世解释史，不能直接画等号。

来源：维基文库《尚书/洪范》https://zh.wikisource.org/zh-hans/尚書/洪範
说明：此为可检索数字文本整理，不代替原刻本影像校勘；传世异文、古文字释读和断句仍待逐项核验。""", chatGptTranslation = """**开篇与九畴总纲**

第十三年，王召见箕子。

王说：“啊，箕子！上天安定下民，使他们和睦地居住。我不知道维持这种秩序的常理应当如何安排。”

箕子回答：“我听说从前鲧堵塞洪水，扰乱了五行；上帝因此震怒，没有赐给他洪范九畴，常理秩序便败坏了。鲧被处死，禹继而兴起，上天于是赐给禹洪范九畴，常理秩序才得以建立。”

九畴依次是：第一五行，第二敬慎运用五事，第三妥善施行八政，第四协调运用五纪，第五建立皇极，第六运用三德治理，第七明察并运用稽疑，第八考察并运用庶征，第九向往五福、警戒六极。

**第一畴：五行**

第一是水，第二是火，第三是木，第四是金，第五是土。水的性质是润泽而向下，火的性质是炎热而向上，木可以弯曲也可以伸直，金属可以顺应加工而改变形状，土适宜耕种收获。润下产生咸味，炎上产生苦味，曲直产生酸味，从革产生辛味，稼穑产生甘味。

**第二畴：五事**

第一是容貌，第二是言语，第三是观察，第四是听闻，第五是思考。容貌应庄重，言语应顺理，观察应明察，听闻应敏锐，思考应通达。庄重会形成肃敬，顺理会形成治理，明察会形成明辨，敏锐会形成谋虑，通达会形成圣明。

**第三畴：八政**

第一是粮食，第二是财货，第三是祭祀，第四是司空（土木营建），第五是司徒（教化与民政），第六是司寇（刑狱），第七是宾礼，第八是军事。

**第四畴：五纪**

第一是年，第二是月，第三是日，第四是星辰，第五是历数。

**第五畴：皇极**

君王应建立至高的准则，汇聚五福并广泛赐予百姓。百姓也应以君王所建立的准则为归依。百姓不可结成邪僻朋党，也不可互相勾结偏私，应以君王的正道为准则。凡百姓中有谋略、有作为、有操守的人，君王都应当顾念。对于尚未合乎准则、但也没有陷入罪过的人，君王应当容纳。如果有人态度和善、神色恭敬，并说自己喜爱美德，君王就应赐福于他。君王应当不虐待孤独无助者，也不畏惧权势显赫者。凡有才能、有作为的人，应使他们能够施展正当行为，这样国家才会昌盛。公正之人应当先使其富足，再使其善良；若不能使他们有益于国家，那么他们便可能遭受不幸。对于不喜爱美德的人，即使赐福，也可能反而成为君王自己的过失。

君王应当不偏不倚，遵循王者正义；不因私好而偏袒，也不因私恶而排斥。没有偏私、没有党派，王道便广大平正；没有反常倾斜，王道便正直。众人应汇聚于正道，并归向正道。这些关于皇极的教导，是常法和训诫；百姓应当学习并实行，以接近天子的光明。天子应成为百姓的父母，作为天下之王。

**第六畴：三德**

第一是正直，第二是刚强制胜，第三是柔和制胜。天下安定时应正直；遇到强横而不顺从者，应以刚强方式制胜；遇到和顺者，应以柔和方式治理。面对深沉难测者，应刚强；面对高明者，应柔和。只有君王可以赐福、施威并享用珍贵饮食；臣下不得擅自赐福、施威或僭用王者待遇。臣下若擅行这些权力，就会危害自己的家族并使国家遭凶。人们因此会变得偏邪，百姓也会僭越失度。

**第七畴：稽疑**

选择并任命负责卜筮的人，然后命令他们进行卜筮。兆象有雨、霁、蒙气、雾气、克、贞、悔等类别。卜有七种判断，其中五种用于占验，两种用于推衍差错。设立卜筮官后，若由三人占断，就听从其中两人的意见。

君王若有重大疑问，应先谋之于己心，再询问卿士、百姓，并求助于卜筮。若君王、龟卜、筮占、卿士和百姓的意见都一致，这叫作“大同”，自身便会安康强健，子孙也会遇到吉祥。经文随后列举意见不一致时的不同组合，并分别说明吉凶。若君王、龟卜和筮占意见一致，而卿士与百姓反对，仍为吉；若卿士、龟卜和筮占一致，而君王与百姓反对，也为吉；若百姓、龟卜和筮占一致，而君王与卿士反对，也为吉。若君王同意、龟卜同意，但筮占、卿士和百姓反对，则用于内部事务为吉，用于外部事务为凶。若龟卜和筮占都违背人的意见，静守为吉，贸然行动则凶。

**第八畴：庶征**

天时征象包括雨、晴、暖、寒、风。五种气候按时节齐备、各得其序，草木就会繁茂。若某一种征象过度，就会凶；若某一种征象完全缺失，也会凶。

吉祥的征象包括：君王庄肃，则及时降雨；君王善治，则及时放晴；君王明察，则温暖适时；君王善谋，则寒冷适时；君王圣明，则风来适时。灾异的征象则相反：君王狂妄，便长期降雨；君王僭越，便长期干旱；君王贪逸，便长期酷热；君王急躁，便长期严寒；君王昏昧，便长期刮风。

君王的政绩以年为考察单位，卿士以月为考察单位，众官以日为考察单位。若年、月、日、时没有失序，百谷便会成熟，政务清明，贤俊显达，家国安宁；若日月年时错乱，百谷便不能成熟，政务昏暗，贤俊隐退，家国不宁。百姓如同星辰，各有其所好；日月运行有冬有夏，月亮依从星辰的位置，便会带来风雨变化。

**第九畴：五福与六极**

五福：第一长寿，第二富足，第三健康安宁，第四喜爱美德，第五善终。六极：第一凶祸、夭折，第二疾病，第三忧患，第四贫困，第五丑恶，第六衰弱。

**校勘与翻译说明**

本条依现有通行数字文本翻译。篇名“洪范／鸿范”、开篇字形以及“五事”“庶征”等处存在传本和释读差异；部分古语的确切训释仍有争议。此译文是 ChatGPT 工作译文，不替代古文字校释或影印本逐字校勘。"""),
    Entry("相关古籍", "《周礼·春官·大卜》三易条", "《周礼·春官宗伯》大卜职文节录；用于说明《连山》《归藏》《周易》三易的传世文献记载。此处采用《周礼正义》相关页面可核验的经文，注疏另行区分。", """经文：

大卜掌三兆之法，一曰玉兆，二曰瓦兆，三曰原兆。其经兆之体，皆百有二十，其颂皆千有二百。掌三易之法，一曰连山，二曰归藏，三曰周易。其经卦皆八，其别皆六十有四。掌三梦之法，一曰致梦，二曰觭梦，三曰咸陟。其经运十，其别九十。以邦事作龟之八命，一曰征，二曰象，三曰与，四曰谋，五曰果，六曰至，七曰雨，八曰廖。以八命者赞三兆、三易、三梦之占，以观国家之吉凶，以诏救政。凡国大贞，卜立君，卜大封，则□高作龟。大祭祀，则□高命龟。凡小事，莅卜。国大迁、大师，则贞龟。凡旅，陈龟。凡丧事，命龟。

校勘与缺字说明：
1. “掌三易之法”以下为本专题关注重点；“二曰归藏”在部分数字文本出现“二日归藏”，本条按文义与通行经文作“二曰”，仍待影印本核字。
2. “□高”是当前数字文本无法识别的字位，本项目保留缺字符号，不据职官语境擅自补字。
3. 《周礼正义》页面附有郑注、贾公彦疏等解释，不能与《周礼》经文混排。

来源：
《周礼正义》相关篇章：https://zh.wikisource.org/zh-hans/周禮正義/47
另见《周礼·春官·大卜》文本整理：https://www.quanxue.cn/ct_rujia/zhouli/zhouli30.html

状态：本条为大卜职文节录，非《周礼》全篇。""", chatGptTranslation = """大卜掌管三种龟卜兆法：第一种叫玉兆，第二种叫瓦兆，第三种叫原兆。三种兆法的基本形态各有一百二十种，相关占辞合计一千二百条。

大卜掌管三种《易》法：第一种叫《连山》，第二种叫《归藏》，第三种叫《周易》。三种《易》的经卦都是八个，进一步组合出的卦都是六十四个。

大卜还掌管三种梦占：第一种叫致梦，第二种叫觭梦，第三种叫咸陟。其基本类别有十种，细分则有九十种。

国家事务需要用龟卜时，有八种命问：征、象、与、谋、果、至、雨、廖。以这八类命问辅助三兆、三易、三梦的占验，用来观察国家吉凶，并向君王提出救正政事的建议。

凡国家有重大决策，例如立君、大规模封建等事项，就要命令相关人员制作龟卜；举行重大祭祀时，也要命令相关人员准备龟卜。小事则由官员亲临卜问。国家迁都、出师时要进行贞问；出行时陈列龟具；丧事也要命龟。

**校勘说明**

原文“□高”是现有数字文本无法识别的字位，本译文不擅自补字。该段是《周礼·春官·大卜》职文节录，并非《周礼》全篇；郑玄注、贾公彦疏应与经文分层阅读。"""),
    Entry("河图", "河图专题·资料边界", "当前为专题说明；古籍图版与历代图式尚待逐本核验。", """河图与洛书在后世易学中经常并称，但两者的图式、数字配置、文献解释史必须分开记录。

后续分别收录：
一、传世文献中“河出图”的原文与注疏；
二、宋代图书易学中的河图图式及版本；
三、刘牧、朱熹、蔡元定等人的论说；
四、图式异同、来源争议与现代研究。

在取得可核验的版本影像前，不补画所谓“古本原图”，也不把现代重绘图标成古籍原图。""", chatGptTranslation = """河图和洛书在后世易学中经常并称，但两者的图式、数字配置和文献解释史必须分开记录。

后续资料分为四类：第一，传世文献中“河出图”的原文及注疏；第二，宋代图书易学中的河图图式及其版本；第三，刘牧、朱熹、蔡元定等人的相关论说；第四，不同图式之间的差异、来源争议和现代研究。

在取得可核验的版本影像之前，不补画所谓古本原图，也不把现代重绘图标成古籍原图。"""),
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

图像状态：已找到整本扫描入口，九图具体页码及可打包授权仍待逐页核实；此条不把目录当作已下载图像。""", chatGptTranslation = """朱熹《周易本义》卷首收有九幅图：

1. 《河图》
2. 《洛书》
3. 《伏羲八卦次序》
4. 《伏羲八卦方位》
5. 《伏羲六十四卦次序》
6. 《伏羲六十四卦方位》
7. 《文王八卦次序》
8. 《文王八卦方位》
9. 《卦变图》

**版本说明：**目前找到的扫描入口，是书格所载清康熙内府仿宋咸淳本，底本说明指向哈佛大学图书馆藏本。书格提供原色高清版和黑白版；图像的版权及再利用范围仍须遵守馆藏和发布方条件。国家图书馆另藏宋咸淳元年吴革刊本，必须与康熙仿刻本分开建档。

**图像状态：**已找到整本扫描入口，但九幅图的具体页码及可打包使用的授权条件仍待逐页核实。目录存在不代表图像已经下载或嵌入。"""),
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

图像状态：已确认 22 幅插图目录；需逐图匹配正文并抽取对应原页，不能只保存封面。""", chatGptTranslation = """目前已核实《易象图说》中的图版主题及原书页码，包括：龙图天地未交之数、龙图天地交合之位、龙图生成数、洛书天地交错数、洛书纵横十五数、两仪生四象、八卦次序、先天八卦对待、后天八卦流行、先后天合德、伏羲六十四卦圆图、六十四卦循环图、八画卦、重卦图、六十四卦变通图和六十四卦应用图。

**图像来源：**相关扫描由浙江大学图书馆数字化，并可通过 Internet Archive 等入口查阅。内篇卷上至卷下共 139 页，外篇卷上至卷下共 107 页；另有 1663 年摛藻堂版本的 151 页图像记录。

**图像状态：**已确认 22 幅插图目录；仍须逐图匹配正文并抽取对应原页，不能只保存封面或目录页。"""),
    Entry("相关古籍", "古籍图版总目录·明刘定之《易经图释》", "Wikimedia Commons 所藏《易经图释（一）》DjVu 扫描，来源 Internet Archive identifier 02071887.cn；页面标记 Public Domain。原书描述注明部分字迹不清、部分页面残缺。", """书首图表包括先天图、后天图等；书目提要说明其图表多与朱熹《周易本义》相同，但不列卦变图、河图、洛书。
图像扫描入口：https://commons.wikimedia.org/wiki/File:%E6%98%93%E7%B6%93%E5%9C%96%E9%87%8B(%E4%B8%80).djvu
电子文本与底本说明：https://ctext.org/wiki.pl?if=gb&remap=gb&res=472287

图像状态：第一册 138 页扫描可用；页面残缺处必须保留缺损状态。其余卷册图像仍待查找。""", chatGptTranslation = """明代刘定之《易经图释》第一册的扫描现藏于 Wikimedia Commons，来源标识为 Internet Archive 的 02071887.cn。页面将其标记为公有领域。原书说明指出，部分字迹不清，部分页面残缺。

书首图表包括先天图、后天图等。书目提要称，其中许多图表与朱熹《周易本义》相同，但没有收入卦变图、河图和洛书。

**图像状态：**第一册共 138 页扫描可用。页面残缺处必须保留其缺损状态；其余卷册图像仍待查找。"""),
    Entry("相关古籍", "古籍图版总目录·《正统道藏》本《周易图》三卷", "《正统道藏》第69至70册，洞真部灵图类，三卷；中国哲学书电子化计划提供馆藏影像入口及目录线索。作者未题，不能擅自归于某一位易学家。", """图题目录包括太极、周氏太极、郑氏太极贯一、河图、洛书、天地自然十五数、先天数、先天象、六十四卦阴阳倍乘、八卦纳甲、六十四卦生自两仪、六十四卦大象、序卦、杂卦、大衍数、十八变成卦、乾坤合律、四象八卦等。
馆藏影像入口：https://ctext.org/library.pl?if=gb&res=83810
图题及文本检索入口：https://ctext.org/wiki.pl?chapter=260323&if=gb&remap=gb

图像状态：已找到三卷馆藏影像入口及图题目录；须逐卷核验页图，不能把目录推测成图像已提取。""", chatGptTranslation = """《正统道藏》本《周易图》共三卷，收入第六十九至第七十册，归入洞真部灵图类。作者没有署名，因此不能擅自归于某一位易学家。

图题目录包括：太极图、周氏太极图、郑氏太极贯一图、河图、洛书、天地自然十五数、先天数、先天象、六十四卦阴阳倍乘、八卦纳甲、六十四卦生自两仪、六十四卦大象、序卦、杂卦、大衍数、十八变成卦、乾坤合律、四象八卦等。

**图像状态：**目前已找到三卷的馆藏影像入口及图题目录；仍须逐卷核验页面图像，不能把目录推测成已经提取了全部图版。"""),
    Entry("相关古籍", "古籍图版总目录·《易经》六十四卦版画", "Source Library 收录一部匿名《易经／断易天机》相关图像册，约 1368—1700 年，40 张扫描、36 幅图像；原书图像来自 Internet Archive，页面标记原件公有领域。书名及作者信息仍需谨慎处理。", """该册保存与六十四卦相关的叙事性木刻图像，部分图像可与具体卦名对应。Source Library 的审校记录指出其 OCR 有较多错误，因此这里只把它作为图像来源，不把 OCR 当作可靠正文。
原书图像与目录：https://sourcelibrary.org/zh/book/yi-jing-64-hexagram-diagrams-anonymous

图像状态：已确认 36 幅图像目录；逐幅核实卦名、页码和图像语境后，才可关联到六十四卦阅读页。""", chatGptTranslation = """这是一册与六十四卦有关的匿名《易经》或《断易天机》图像资料，年代约在 1368 至 1700 年之间。资料库记录有四十张扫描页、三十六幅图像，原书图像来自 Internet Archive，页面标记原件属于公有领域。

该册保存了与六十四卦相关的叙事性木刻图像，其中部分图像可以与具体卦名对应。资料库的审校记录指出，其 OCR 识别错误较多，因此这里只把它作为图像来源，不把 OCR 文本当作可靠的古籍正文。

**图像状态：**已确认三十六幅图像的目录；只有逐幅核实卦名、页码和图像语境之后，才能把图像关联到六十四卦阅读页。"""),
    Entry("相关古籍", "古籍图版总目录·陈士元《易象钩解》", "明代陈士元易学著作，Source Library 收录卷一至卷二 151 页扫描，来源 Internet Archive，页面标记原书公有领域。", """该书以象解易，适合作为历代卦象图说与经文解释的参照。具体图版数量及各图所对应卦爻，须逐页查验原书，不能根据书名预设每页均有图。
原书扫描入口：https://sourcelibrary.org/book/vol-1-1782366422220

图像状态：已找到原书扫描入口；逐页图像编目待完成。""", chatGptTranslation = """明代陈士元所著《易象钩解》，目前在 Source Library 收录卷一至卷二，共 151 页扫描。扫描来源为 Internet Archive，页面将原书标记为公有领域。

该书以卦象解释《易》，可作为历代卦象图说和经文解释的参照。具体图版数量，以及每幅图对应哪些卦爻，必须逐页查验原书，不能仅凭书名就假定每页都有图。

**图像状态：**已经找到原书扫描入口；逐页图像编目尚未完成。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十五后段·系辞下传续录", "底本：景印文渊阁《四库全书》本《周易集解》卷十五，参校品古籍四库本卷十五页面。范围自“子曰：非所困而困焉”至卷末“有不善，未尝不知；知之，未尝复行也”。经文与诸家注文分段录入；尚未逐字对照影印页校勘。", """
子曰：非所困而困焉，名必辱。
虞翻曰：困本咸。咸三入宫，以阳之阴则二制坤，故以决咸为四所困。四失位恶人，故非所困而困焉。阳称名，阴为辱，以阳之阴下，故名必辱也。

非所据而据焉，身必危。
虞翻曰：谓据二。二失位，故非所据而据焉。二变时坤为身，二折坤体，故身必危。

既辱且危，死期将至，妻其可得见耶？
陆绩曰：六三从困辱之家，变之大过为棺椁死丧之象，故曰死期将至，妻不可得见。

易曰：公用射隼于高墉之上，获之，无不利。
孔颖达曰：前章先须安身可以崇德，故此明藏器于身、待时而动，是有利也，故引解之上六以证之矣。

子曰：隼者，禽也。
虞翻曰：离为隼，故称禽；言其行野容如禽兽焉。

弓矢者，器也。
虞翻曰：离为矢，坎为弓，坤为器。

射之者，人也。
虞翻曰：人，贤人也。谓乾三伏阳出而成乾，故曰射之者人。人则公三应上，故上令三出而射隼也。

君子藏器于身，待时而动，何不利之有？
虞翻曰：三伏阳为君子。二变时坤为身、为藏器，为藏弓矢以待射隼；艮为待、为时。三待五来之二，弓张矢发，动出成乾，贯隼入大过，死两坎象坏，故何不利之有。象曰“以解悖”，三阴小人乘君子器，故上观三出射去隼也。

动而不括，是以出而有获，语成器而动者也。
虞翻曰：括，作也。震为语，乾五之坤二成坎弓、离矢，动以贯隼，故语成器而动者也。

子曰：小人不耻不仁，不畏不义。
虞翻曰：谓否也。以坤灭乾为不仁不义，坤为耻、为畏，乾为仁、为义者也。

不见利不劝，不威不惩。
虞翻曰：否乾为威、为利，巽为近利。谓否五之初成噬嗑，市离日见；乾为见利，震为动，故不见利不动。五之初以乾威坤，故不威不惩；震为惩也。

小惩而大诫，此小人之福也。
虞翻曰：艮为小，乾为大。五下威初，坤杀不行，震惧虩虩，故小惩大诫。坤为小人，乾为福；以阳下阴，民说无疆，故小人之福也。

易曰：屦校灭趾，无咎，此之谓也。
九家易曰：噬嗑六五本先在初，处非其位，小人者也。故历说小人所以为罪，终以致害；虽欲为恶，能止不行，则无咎。
侯果曰：噬嗑初九爻辞也。校者，以木夹足止行也。此明小人因小刑而大诫，乃福也。

善不积，不足以成名。
虞翻曰：乾为积善，阳称名。

恶不积，不足以灭身。
虞翻曰：坤为积恶、为身，以乾灭坤，故灭身者也。

小人以小善为无益而弗为也。
虞翻曰：小善为复初。

以小恶为无伤而弗去也。
虞翻曰：小恶谓姤初。

故恶积而不可掩。
虞翻曰：谓阴息姤至遯，子弑其父，故恶积而不可掩。

罪大而不可解。
虞翻曰：阴息遯成否，以臣弑君，故罪大而不可解也。

易曰：何校灭耳，凶。
九家易曰：噬嗑上九爻辞也。阴自初升五，所在失正，积恶而罪大，故为上所灭。善不积斥五阴爻也。聪不明者，闻善不听，闻戒不改，故凶也。

子曰：危者，安其位者也。
崔憬曰：言有危之虑，则能安其位不失也。

亡者，保其存者也。
崔憬曰：言有亡之虑，则能保其存者也。

乱者，有其治者也。
崔憬曰：言有防乱之虑，则能有其治者也。

是故君子安而不忘危，存而不忘亡，治而不忘乱，是以身安而国家可保也。
虞翻曰：坤为身，谓否反成泰，君位定于内而臣忠于外，故身安而国家可保也。
荀爽曰：存而不忘亡，谓除戎器、戒不虞也；思患而逆防之。
翟玄曰：在安虑危，在存虑亡，在治虑乱。

易曰：其亡其亡，系于包桑。
荀爽曰：存不忘亡也。
陆绩曰：桑者上玄下黄，乾坤相包以正，故不可忘。自此以上皆谓否阴灭阳之卦，五在否家，虽得中正常，自惧以危亡之事者也。

子曰：德薄而位尊，知小而谋大，力少而任重，鲜不及矣。
虞翻曰：鼎四也。则离九四凶恶小人，故德薄；四在乾位，故位尊。兑为小知，乾为大谋，四在乾体，故谋大。五至初体大过，本末弱，故力少；乾为仁，故任重，以为己任，不亦重乎。
虞翻曰：鲜，少也；及于刑矣。

易曰：鼎折足，覆公餗，其形渥，凶，言不胜其任也。
孔颖达曰：言不能安身，知小谋大而遇祸也，故引鼎九四以证之矣。

子曰：知几其神乎？
虞翻曰：几谓阳也。阳在复初称几，此谓豫四也。恶鼎四折足，故以此次言豫四知几而反复初也。

君子上交不谄，下交不渎，其知几乎？
虞翻曰：豫上谓四也。四失位，谄渎上谓交五；五贵，震为笑言，笑言且谄也。下谓交三，坎为渎，故下交不渎。欲其复初得正元吉，故其知几乎。
侯果曰：上谓王侯，下谓凡庶。君子上交不至谄媚，下交不至渎慢，悔吝无从而生，岂非知微者乎？

几者，动之微，吉之先见者也。
虞翻曰：阳见初成震，故动之微；复初元吉，吉之先见者也。
韩康伯曰：几者，去无入有，理而未形者；不可以名寻，不可以形睹。唯神也，不疾而速，感而遂通，故能玄照鉴于未形。合抱之木起于毫末，吉凶之彰始乎微兆，故言吉之先见。

君子见几而作，不俟终日。易曰：介于石，不终日，贞吉。介如石焉，宁用终日，断可识矣。
孔颖达曰：前章云精义入神，此明知几入神之事，故引豫之六二以证之。
崔憬曰：此爻得位居中，于豫之时能顺以动而防于豫，如石之耿介，守志不移。虽暂豫乐，以其见微而不终日，则能贞吉，断可知矣。

君子知微、知彰、知柔、知刚，万夫之望。
姚信曰：此谓豫卦也。二下交初，故曰知微；上交于三，故曰知彰；体坤处和，故曰知柔；与四同功，故曰知刚。
荀爽曰：圣人作而万物睹。
干宝曰：言君子苟达于此，则万夫之望矣。周公闻齐鲁之政，知后世强弱之势；辛有见被发而祭，则知为戎狄之居。凡若此类，可谓知几也。皆称君子，君子则以得几，不必圣者也。

子曰：颜氏之子，其殆庶几乎？
虞翻曰：几者神妙也。颜子知微，故殆庶几。孔子曰：“回也，其庶几乎？”

有不善，未尝不知；知之，未尝复行也。
虞翻曰：复以自知。老子曰：“自知者明。”
虞翻曰：谓颜回不迁怒、不贰过，克己复礼，天下归仁。
""", chatGptTranslation = """**ChatGPT 翻译（逐段工作译文）**

**孔子说：不该陷入困境却陷入困境，名声必定受辱。**

虞翻解释：困卦原本与咸卦相通。咸卦九三进入宫中，阳爻转入阴位，二爻便形成坤体；因此咸卦发生变化后，受到九四的困阻。九四失位，属于注家所说的恶人，所以说不该受困却陷入困境。阳象征名声，阴象征屈辱；阳居阴下，因此名声必定受辱。

**不该占据的位置却强行占据，自身必定危险。**

虞翻解释：这里指占据九二的位置。九二失位，所以说不该占据却占据。九二变化时，坤象征身体；二爻破坏坤体，因此自身必然危险。

**既受屈辱又处于危险之中，死亡的期限将近，妻子哪里还能够见到呢？**

陆绩解释：六三处在困辱之家，变化为大过卦，形成棺椁和死亡丧葬的象征，所以说死亡将近，妻子也无法再见。

**《易》说：公在高墙之上射猎隼鸟，捕获它，没有什么不利。**

孔颖达解释：前文先说明安顿自身，才能增进德行；这里进一步说明把才能和器具藏在自身，等待时机再行动，就会有利。因此引用解卦上六爻辞作为证明。

**孔子说：隼是一种鸟类。**

虞翻解释：离卦象征隼，所以称为禽鸟；这里说它行动于野外，形态如同禽兽。

**弓和箭，是器具。**

虞翻解释：离象征箭，坎象征弓，坤象征器具。

**射猎的人，是人。**

虞翻解释：这里的人指贤人。乾三伏藏的阳爻显现而形成乾，所以说射猎者是人。所谓“公”，指九三与上六相应，因此上位者命令九三出来射隼。

**君子把器具藏在自身，等待时机再行动，还有什么不利呢？**

虞翻解释：九三伏藏的阳爻象征君子。九二变化时，坤象征身体，也象征收藏器具，表示把弓箭藏好，等待射隼的时机。艮象征等待，也象征时机。九三等待九五来到九二，弓张开、箭射出，发动后形成乾，箭贯穿隼鸟而进入大过卦；两重坎象被破坏，所以说没有什么不利。象辞所说“以解悖”，是因为三爻阴柔小人乘用君子的器具，上位者观察九三出而射去隼鸟。

**行动而不受阻碍，因此出发便有所收获；这是说先备好器具再行动。**

虞翻解释：“括”是结、阻碍的意思。震象征言语；乾五来到坤二，形成坎弓、离箭，发动而射中隼鸟，所以说先备好器具再行动。

**孔子说：小人不以不仁为羞耻，也不畏惧不义。**

虞翻解释：这里说的是否卦。坤消灭乾，象征不仁、不义；坤象征羞耻和畏惧，乾象征仁与义。

**看不到利益就不会受到劝勉，没有威势就不会受到惩戒。**

虞翻解释：否卦的乾象征威势和利益，巽象征趋近利益。否卦九五来到初位形成噬嗑，离象征市场和太阳显现；乾象征看见利益，震象征行动，所以看不到利益便不行动。九五来到初位，以乾的威势制约坤，所以没有威势便不能惩戒；震象征惩戒。

**小小惩戒而得到大的警诫，这是小人的福分。**

虞翻解释：艮象征小，乾象征大。九五下降而威慑初爻，坤所象征的杀伐不再施行，震表现出警惧，所以小惩而大诫。坤象征小人，乾象征福；阳爻居于阴爻之下，百姓因此喜悦无边，所以这是小人的福分。

**《易》说：“脚上戴着刑具，遮没脚趾，没有灾咎。”说的就是这种情形。**

九家易解释：噬嗑六五原本先处在初位，位置不当，象征小人。因此逐层说明小人如何犯罪并最终招致祸害；如果能够及时停止作恶，就可以没有灾咎。侯果解释：这是噬嗑初九爻辞。校是用木板夹住脚、阻止行走的刑具。这里说明小人受到较轻的刑罚后能够得到较大的警戒，这反而是福。

**善行不积累，就不足以成就好的名声。**

虞翻解释：乾象征积累善行，阳象征名声。

**恶行不积累，就不足以造成自身毁灭。**

虞翻解释：坤象征积累恶行，也象征身体；乾消灭坤，所以说恶行积累会毁灭自身。

**小人把小善看作没有益处，因此不去做。**

虞翻解释：这里的小善指复卦初爻所象征的善端。

**把小恶看作没有伤害，因此不肯除去。**

虞翻解释：这里的小恶指姤卦初爻所象征的阴气初生。

**所以恶行积累起来便无法掩盖，罪行重大便无法解除。**

虞翻解释：阴气从姤卦逐渐增长到遯卦，出现儿子弑父的象征，所以恶行积累便无法掩盖。阴气继续增长，遯卦变成否卦，出现臣下弑君的象征，因此罪行重大而无法解除。

**《易》说：“戴着沉重的刑具，遮没耳朵，凶险。”**

九家易解释：这是噬嗑上九爻辞。阴爻从初位上升到五位，却处在不正的位置，恶行积累、罪责重大，所以最终被上位者惩治。“善不积”所指的是五位的阴爻。所谓听不清善言，就是听到劝善也不接受，听到警戒也不改正，所以凶险。

**孔子说：处在危险中的人，正是因为曾经安于自己的位置。**

崔憬解释：能够预先考虑危险，才可以安定自己的位置而不失去它。

**将要灭亡的人，正是因为曾经保有生存的条件。**

崔憬解释：能够考虑灭亡的可能，才可以保全已有的生存条件。

**发生混乱的人，正是因为曾经拥有治理的局面。**

崔憬解释：能够预先防范混乱，才可以保持治理的秩序。

**所以君子安定时不忘危险，生存时不忘灭亡，治理时不忘混乱，因此自身可以安定，国家也能够保全。**

虞翻解释：坤象征身体，这里说否卦反转为泰卦，君位在内安定，臣下在外忠诚，所以自身安定，国家可以保全。荀爽解释：身处安定时仍要整治兵器、戒备意外。翟玄解释：安定时考虑危险，生存时考虑灭亡，治理时考虑混乱。

**《易》说：“将要灭亡了，将要灭亡了！好比系在包裹桑树上的绳索。”**

荀爽解释：这是说即使处于生存状态，也不忘记灭亡的危险。陆绩解释：桑树上部呈玄色、下部呈黄色，象征乾坤相互包合而端正，因此不可忘记警戒。此前所说都属于否卦阴气消灭阳气的情形；九五虽然得中得正，仍然以危亡之事警惕自己。

**孔子说：德行浅薄却地位尊贵，智慧狭小却谋划宏大，力量微弱却承担重任，很少有人不会遭遇祸患。**

虞翻解释：这里指鼎卦九四。离卦九四是凶恶的小人，所以说德薄；九四处在乾所象征的尊位，所以说位尊。兑象征小的智慧，乾象征宏大的谋划；九四处在乾体之中，所以谋划宏大。九五来到初位形成大过，本末都弱，所以力量少。乾象征仁德，因此承担重任。虞翻又解释：“鲜”是少的意思，这里指很少有人不会受到刑罚。

**《易》说：“鼎足折断，倾覆了公家的食物，污秽沾满身体，凶险。”这是说不能胜任所承担的责任。**

孔颖达解释：这是说不能安顿自身，智慧不足却谋划过大，最终遭遇祸患，所以引用鼎卦九四作为证明。

**孔子说：知道事物细微的征兆，大概就是神妙的智慧吧！**

虞翻解释：“几”指阳气。阳气在复卦初爻出现，称为细微征兆；这里指豫卦九四。鼎卦九四因恶行而折足，因此接下来讲豫卦九四能够察觉征兆，返回复卦初爻。

**君子与上位者交往不谄媚，与下位者交往不轻慢，这就是懂得细微征兆吧？**

虞翻解释：豫卦上体所说的是九四。九四失位，对上交往五爻时有谄媚之象；五爻尊贵，震象征笑言，笑言过度便成为谄媚。向下交往三爻，坎象征亵渎，所以说向下交往不轻慢。希望九四返回初位而得正，便能大吉。侯果解释：上指王侯，下指普通百姓。君子对上不谄媚，对下不轻慢，悔恨和过失便无从产生，这难道不是洞察细微的人吗？

**细微征兆，是行动刚刚萌动的迹象，也是吉祥最先显现的征兆。**

虞翻解释：阳气在初位显现而形成震，所以称为行动的微兆；复卦初九元吉，是吉祥最先显现。韩康伯解释：几是从无进入有、道理已经萌生而形体尚未显现的状态，不能用名称寻求，也不能从外形看见。只有神妙的洞察力能够迅速感通，照见尚未成形的事物。合抱的大树从细小的萌芽开始，吉凶的显现也始于微小征兆，所以说吉祥会先从细微处显现。

**君子看见细微征兆便立即行动，不等到一天结束。《易》说：“坚如磐石，不等到终日，守正吉祥。”像石头一样坚定，哪里还需要等到终日？事情的判断已经十分清楚。**

孔颖达解释：前文说精研义理而进入神妙境界，这里说明洞察征兆也能达到神妙，所以引用豫卦六二作为证明。崔憬解释：此爻得位居中，在豫乐之时能够顺势行动并防止沉溺，好比石头坚定不移。虽然处于安乐之中，却能看见微兆，不等到一天结束便采取行动，因此守正吉祥，结果可以断定。

**君子懂得细微与显著、柔顺与刚强，是众人仰望的对象。**

姚信解释：这里说的是豫卦。六二向下与初爻交往，所以说知微；向上与三爻交往，所以说知彰；卦体为坤而居于和顺之处，所以说知柔；与九四同功，所以说知刚。荀爽解释：这是圣人出现而万物得以看见的意思。干宝解释：君子如果真正通达这些道理，就会成为众人仰望的对象。周公听闻齐、鲁两国的政事，便预知后世强弱的趋势；辛有看见披发祭祀的情形，便知道那里将成为戎狄居住之地。类似这些事，都可以称为知几。这里称君子，并不意味着只有圣人才有这种能力。

**孔子说：颜回这个人，大概接近于懂得细微征兆的境界了吧？**

虞翻解释：几是神妙的征兆。颜回能够洞察细微，所以说他接近于知几。孔子曾说：“颜回大概接近于这个境界吧！”

**有不善之处，他没有不知道的；知道以后，也从不再犯。**

虞翻解释：这是能够反省而自知。老子说：“自知者明。”虞翻又解释：这是说颜回不迁怒、不重复犯错，能够克制自己、回归礼义，因此天下归于仁德。

译释说明：本段大量注文以卦变、爻位解释《系辞》，译文分别保留虞翻、荀爽、陆绩、韩康伯、孔颖达等人的归属。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十七·说卦序卦杂卦经传正文", "底本：景印文渊阁《四库全书》本《周易集解》卷十七，参校品古籍四库本第十七卷及维基文库四库本卷十七。页面标示本卷11,228字。本条补录《说卦》《序卦》《杂卦》三传正文；为避免混淆，正文与李鼎祚所辑诸家注文分开，诸家注文尚未完整录入。个别卦象取象条目存在版本字形差异，后续须逐字核影印本。", """
**说卦传**

昔者圣人之作《易》也，幽赞于神明而生蓍，参天两地而倚数，观变于阴阳而立卦，发挥于刚柔而生爻，和顺于道德而理于义，穷理尽性以至于命。
昔者圣人之作《易》也，将以顺性命之理。是以立天之道曰阴与阳，立地之道曰柔与刚，立人之道曰仁与义。兼三才而两之，故《易》六画而成卦。分阴分阳，迭用柔刚，故《易》六位而成章。
天地定位，山泽通气，雷风相薄，水火不相射，八卦相错。数往者顺，知来者逆，是故《易》逆数也。
雷以动之，风以散之，雨以润之，日以烜之，艮以止之，兑以说之，乾以君之，坤以藏之。
帝出乎震，齐乎巽，相见乎离，致役乎坤，说言乎兑，战乎乾，劳乎坎，成言乎艮。万物出乎震，震，东方也。齐乎巽，巽，东南也，齐也者，言万物之洁齐也。离也者，明也，万物皆相见，南方之卦也。圣人南面而听天下，向明而治，盖取诸此也。坤也者，地也，万物皆致养焉，故曰致役乎坤。兑，正秋也，万物之所说也，故曰说言乎兑。战乎乾，乾，西北之卦也，言阴阳相薄也。坎者，水也，正北方之卦也，劳卦也，万物之所归也，故曰劳乎坎。艮，东北之卦也，万物之所成终而所成始也，故曰成言乎艮。
神也者，妙万物而为言者也。
动万物者，莫疾乎雷；桡万物者，莫疾乎风；燥万物者，莫熯乎火；说万物者，莫说乎泽；润万物者，莫润乎水；终万物、始万物者，莫盛乎艮。故水火相逮，雷风不相悖，山泽通气，然后能变化，既成万物也。
乾，健也；坤，顺也；震，动也；巽，入也；坎，陷也；离，丽也；艮，止也；兑，说也。
乾为马，坤为牛，震为龙，巽为鸡，坎为豕，离为雉，艮为狗，兑为羊。
乾为首，坤为腹，震为足，巽为股，坎为耳，离为目，艮为手，兑为口。
乾，天也，故称乎父；坤，地也，故称乎母。震一索而得男，故谓之长男；巽一索而得女，故谓之长女；坎再索而得男，故谓之中男；离再索而得女，故谓之中女；艮三索而得男，故谓之少男；兑三索而得女，故谓之少女。
**乾为天**，为圜，为君，为父，为玉，为金，为寒，为冰，为大赤，为良马，为老马，为瘠马，为驳马，为木果。
**坤为地**，为母，为布，为釜，为吝啬，为均，为子母牛，为大舆，为文，为众，为柄，其于地也为黑。
**震为雷**，为龙，为玄黄，为敷，为大涂，为长子，为决躁，为苍筤竹，为萑苇；其于马也为善鸣，为馵足，为作足，为的颡；其于稼也为反生；其究为健，为蕃鲜。
**巽为木**，为风，为长女，为绳直，为工，为白，为长，为高，为进退，为不果，为臭；其于人也为寡发，为广颡，为多白眼，为近利市三倍；其究为躁卦。
**坎为水**，为沟渎，为隐伏，为矫輮，为弓轮；其于人也为加忧，为心病，为耳痛，为血卦，为赤；其于马也为美脊，为亟心，为下首，为薄蹄，为曳；其于舆也为多眚，为通，为月，为盗；其于木也为坚多心。
**离为火**，为日，为电，为中女，为甲胄，为戈兵；其于人也为大腹；为乾卦，为鳖，为蟹，为蠃，为蚌，为龟；其于木也为科上槁。
**艮为山**，为径路，为小石，为门阙，为果蓏，为阍寺，为指，为狗，为鼠，为黔喙之属；其于木也为坚多节。
**兑为泽**，为少女，为巫，为口舌，为毁折，为附决；其于地也为刚卤，为妾，为羊。
 
**序卦传**

有天地，然后万物生焉。盈天地之间者，唯万物，故受之以屯。屯者，盈也。屯者，物之始生也。物生必蒙，故受之以蒙。蒙者，蒙也，物之稚也。物稚不可不养也，故受之以需。需者，饮食之道也。饮食必有讼，故受之以讼。讼必有众起，故受之以师。师者，众也。众必有所比，故受之以比。比者，比也，比必有所畜，故受之以小畜。物畜然后有礼，故受之以履。履而泰，然后安，故受之以泰。泰者，通也。物不可以终通，故受之以否。物不可以终否，故受之以同人。与人同者，物必归焉，故受之以大有。有大者不可以盈，故受之以谦。有大而能谦，必豫，故受之以豫。豫必有随，故受之以随。以喜随人者必有事，故受之以蛊。蛊者，事也。有事然后可大，故受之以临。临者，大也。物大然后可观，故受之以观。可观而后有所合，故受之以噬嗑。嗑者，合也。物不可以苟合而已，故受之以贲。贲者，饰也。致饰然后亨则尽矣，故受之以剥。剥者，剥也。物不可以终尽，剥穷上反下，故受之以复。复则不妄矣，故受之以无妄。有无妄然后可畜，故受之以大畜。物畜然后可养，故受之以颐。颐者，养也。不养则不可动，故受之以大过。物不可以终过，故受之以坎。坎者，陷也。陷必有所丽，故受之以离。离者，丽也。
有天地，然后有万物；有万物，然后有男女；有男女，然后有夫妇；有夫妇，然后有父子；有父子，然后有君臣；有君臣，然后有上下；有上下，然后礼义有所错。夫妇之道，不可以不久也，故受之以恒。恒者，久也。物不可以久居其所，故受之以遁。遁者，退也。物不可以终遁，故受之以大壮。物不可以终壮，故受之以晋。晋者，进也。进必有所伤，故受之以明夷。夷者，伤也。伤于外者必反于家，故受之以家人。家道穷必乖，故受之以睽。睽者，乖也。乖必有难，故受之以蹇。蹇者，难也。物不可以终难，故受之以解。解者，缓也。缓必有所失，故受之以损。损而不已必益，故受之以益。益而不已必决，故受之以夬。夬者，决也。决必有所遇，故受之以姤。姤者，遇也。物相遇而后聚，故受之以萃。萃者，聚也。聚而上者谓之升，故受之以升。升而不已必困，故受之以困。困乎上者必反下，故受之以井。井道不可不革，故受之以革。革物者莫若鼎，故受之以鼎。主器者莫若长子，故受之以震。震者，动也。物不可以终动，止之，故受之以艮。艮者，止也。物不可以终止，故受之以渐。渐者，进也。进必有所归，故受之以归妹。得其所归者必大，故受之以丰。丰者，大也。穷大者必失其居，故受之以旅。旅而无所容，故受之以巽。巽者，入也。入而后说之，故受之以兑。兑者，说也。说而后散之，故受之以涣。涣者，离也。物不可以终离，故受之以节。节而信之，故受之以中孚。有其信者必行之，故受之以小过。有过物者必济，故受之以既济。物不可穷也，故受之以未济终焉。

**杂卦传**

乾刚，坤柔。比乐，师忧。临观之义，或与或求。屯见而不失其居，蒙杂而著。震，起也；艮，止也。损益，盛衰之始也。大畜，时也；无妄，灾也。萃，聚而升不来也。谦轻而豫怠也。噬嗑，食也；贲，无色也。兑见而巽伏也。随，无故也；蛊，则饰也。剥，烂也；复，反也。晋，昼也；明夷，诛也。井，通而困，相遇也。咸，速也；恒，久也。涣，离也；节，止也。解，缓也；蹇，难也。睽，外也；家人，内也。否泰，反其类也。大壮则止，遁则退也。大有，众也；同人，亲也。革，去故也；鼎，取新也。小过，过也；中孚，信也。丰，多故也；亲寡，旅也。离上而坎下也。小畜，寡也；履，不处也。需，不进也；讼，不亲也。大过，颠也。姤，遇也，柔遇刚也。渐，女归待男行也。颐，养正也。既济，定也。归妹，女之终也。未济，男之穷也。夬，决也，刚决柔也，君子道长，小人道忧也。
""",
    chatGptTranslation = """ChatGPT 翻译（逐段工作译文）

【说卦传】

从前圣人创作《易》，在幽深隐微之中赞助、沟通神明之道，于是产生了蓍草占筮；取天数为三、地数为二来建立数的依据；观察阴阳变化而创立卦象，阐发刚柔变化而产生爻；使道德和顺，并以义理加以条理化；穷尽事理、尽察天性，进而通达天命。

从前圣人创作《易》，是为了顺应性命的道理。因此，确立天道称为阴与阳，确立地道称为柔与刚，确立人道称为仁与义。兼具天、地、人三才，并使每一才各有两画，所以《易》用六画组成一卦。分别阴位与阳位，交替运用柔与刚，因此《易》的六个位次形成文理。

天地各有定位，山与泽之气相互交通，雷与风相互激荡，水与火不相互冲害，八卦交错配合。推数过去的事，依顺序而知；推知未来的事，则须逆向推求，所以《易》采用逆数的方法。

雷使万物发动，风使万物散布，雨水滋润万物，太阳使万物干燥温暖；艮使万物停止，兑使万物喜悦，乾统领万物，坤包藏万物。

帝的运行从震开始，在巽处整齐，在离处显现，在坤处致力于养育，在兑处欣悦，在乾处交战，在坎处劳苦，在艮处完成。万物从震方萌发，震位于东方；在巽方整齐，巽位于东南，所谓整齐，是说万物洁净齐整。离代表光明，万物在这里彼此显现，它是南方之卦。圣人面向南方治理天下，向着光明施政，大概就是取法于此。坤代表大地，万物都在这里得到养育，所以说在坤处致力于养育。兑是正秋之时，万物在此欣悦，所以说在兑处欣悦。乾位于西北，阴阳在这里相互逼近交战。坎代表水，是正北方之卦，也是劳卦；万物归藏于此，所以说在坎处劳苦。艮位于东北，是万物终结旧阶段、开始新阶段的地方，所以说在艮处完成。

所谓神，就是用来形容万物变化精妙的概念。

使万物发动，没有比雷更迅速的；使万物弯曲摇动，没有比风更迅速的；使万物干燥，没有比火更猛烈的；使万物喜悦，没有比泽更能使人喜悦的；滋润万物，没有比水更能滋润的；使万物终结又使万物开始，没有比艮所代表的时序更盛大的。因此水火相互配合，雷风不相违背，山泽之气相通，然后才能发生变化，成就万物。

乾的性质是刚健，坤的性质是柔顺，震的性质是发动，巽的性质是进入，坎的性质是陷入，离的性质是附着，艮的性质是停止，兑的性质是喜悦。

乾象征马，坤象征牛，震象征龙，巽象征鸡，坎象征猪，离象征野鸡，艮象征狗，兑象征羊。

乾象征头，坤象征腹，震象征足，巽象征大腿，坎象征耳，离象征眼，艮象征手，兑象征口。

乾是天，所以称为父；坤是地，所以称为母。震经过一次求索而得到阳爻，所以称长男；巽经过一次求索而得到阴爻，所以称长女；坎经过两次求索而得到阳爻，所以称中男；离经过两次求索而得到阴爻，所以称中女；艮经过三次求索而得到阳爻，所以称少男；兑经过三次求索而得到阴爻，所以称少女。

乾象征天、圆形、君主、父亲、玉、金属、寒冷、冰、大红色、良马、老马、瘦马、杂色马以及树木的果实。

坤象征大地、母亲、布、锅、吝啬、均平、母牛、大车、文采、众多、柄；落实到土地颜色，则象征黑色。

震象征雷、龙、黑黄、铺展、大路、长子、果决急躁、青色幼竹、芦苇；用于马，象征善鸣、白左足、足部躁动、额头白；用于庄稼，象征反向生长；推究其极，则象征刚健与繁茂鲜明。

巽象征木、风、长女、绳墨端直、工匠、白色、长、高、进退不定、犹豫不决、气味；用于人，象征头发稀少、额头宽、多白眼、善于从市场获利；推究其极，是躁动之卦。

坎象征水、沟渠、隐伏、矫正弯曲之物、弓与车轮；用于人，象征忧虑、心病、耳痛、血以及红色；用于马，象征脊背美好、心性急、低头、蹄薄、拖曳；用于车，象征故障较多、通行；又象征月亮、盗贼；用于木，象征坚硬而多心材。

离象征火、太阳、闪电、中女、铠甲、兵器；用于人，象征大腹；又象征干燥之卦、鳖、蟹、螺、蚌、龟；用于树木，象征树干中空而上部枯槁。

艮象征山、路径、小石、门阙、果实与瓜类、守门人和宦官、手指、狗、鼠以及黑嘴动物；用于树木，象征坚硬而多节。

兑象征泽、少女、巫者、口舌、毁坏折断、附着而决裂；用于土地，象征坚硬咸卤；又象征妾与羊。

【序卦传】

有了天地，然后万物才产生。充满天地之间的只有万物，所以接着安排屯卦。屯有充满的意思；屯也是万物开始萌生的状态。万物出生后必然蒙昧，所以接着安排蒙卦。蒙就是蒙昧，是事物幼稚的状态。幼小的事物不能不养育，所以接着安排需卦。需是饮食养育之道。饮食之事必然引发争讼，所以接着安排讼卦。争讼必然聚集众人，所以接着安排师卦；师就是众众。众人必然需要相互亲附，所以接着安排比卦；比就是亲近。亲近之后还需要有所蓄积，所以接着安排小畜卦。事物蓄积之后才有礼节，所以接着安排履卦。履行礼节而后达到通泰，才能安定，所以接着安排泰卦；泰就是通达。事物不能永远通达，所以接着安排否卦；事物也不能永远闭塞，所以接着安排同人卦。与人和同，事物就会归附，所以接着安排大有卦。拥有广大资源的人不能自满，所以接着安排谦卦。拥有广大资源而又能谦逊，必然安乐，所以接着安排豫卦。安乐之后必然有所随从，所以接着安排随卦。因喜悦而随从别人，必然会遇到事务，所以接着安排蛊卦；蛊就是事务。发生事务之后才能有所发展，所以接着安排临卦；临有大的意思。事物发展壮大之后才值得观察，所以接着安排观卦。能够观察之后，才会有所结合，所以接着安排噬嗑卦；嗑就是合。事物不能只求苟且结合，所以接着安排贲卦；贲就是文饰。文饰达到极点，通达也就穷尽了，所以接着安排剥卦；剥就是剥落。事物不能永远穷尽，剥落到了极点便由上反转向下，所以接着安排复卦。复归本位就不会妄为，所以接着安排无妄卦。有了无妄，才可以蓄积，所以接着安排大畜卦。事物蓄积之后才可以养育，所以接着安排颐卦；颐就是养育。不养育便不能行动，所以接着安排大过卦。事物不能永远过度，所以接着安排坎卦；坎就是陷险。陷入险境必然要有所依附，所以接着安排离卦；离就是附着。

有了天地，然后才有万物；有了万物，然后才有男女；有了男女，然后才有夫妇；有了夫妇，然后才有父子；有了父子，然后才有君臣；有了君臣，然后才有上下等级；有了上下等级，然后礼义才有安置和施行之处。夫妇之道不能不长久，所以接着安排恒卦；恒就是长久。事物不能永远停留在原处，所以接着安排遁卦；遁就是退避。事物不能永远退避，所以接着安排大壮卦。事物不能永远强盛，所以接着安排晋卦；晋就是前进。前进必然会遇到损伤，所以接着安排明夷卦；夷就是受伤。在外受伤的人必然返回家中，所以接着安排家人卦。家道发展到极端必然出现乖离，所以接着安排睽卦；睽就是乖离。乖离必然产生困难，所以接着安排蹇卦；蹇就是艰难。事物不能永远处于艰难之中，所以接着安排解卦；解就是缓解。缓解之后又可能有所损失，所以接着安排损卦。损减不停，必然转为增益，所以接着安排益卦。增益不停，必然需要决断，所以接着安排夬卦；夬就是决断。决断之后必然有所遇合，所以接着安排姤卦；姤就是相遇。事物相遇之后才会聚集，所以接着安排萃卦；萃就是聚集。聚集而向上发展称为升，所以接着安排升卦。上升不停，必然陷入困境，所以接着安排困卦。处于上方的困境必然要返回下方，所以接着安排井卦。井道不能不改革，所以接着安排革卦。改变事物没有比鼎更适合的，所以接着安排鼎卦。主持祭器的人没有比长子更合适的，所以接着安排震卦；震就是发动。事物不能永远运动，必须使它停止，所以接着安排艮卦；艮就是停止。事物不能永远停止，所以接着安排渐卦；渐就是逐步前进。前进必然有所归宿，所以接着安排归妹卦。得到归宿的事物必然壮大，所以接着安排丰卦；丰就是盛大。盛大到了极点便会失去原来的居所，所以接着安排旅卦。旅居在外而无处容身，所以接着安排巽卦；巽就是进入。进入之后才能使人喜悦，所以接着安排兑卦；兑就是喜悦。喜悦之后便会离散，所以接着安排涣卦；涣就是离散。事物不能永远离散，所以接着安排节卦。节制之后还要以诚信守持，所以接着安排中孚卦。具备诚信的人必然会付诸行动，所以接着安排小过卦。有所超过便能有所成就，所以接着安排既济卦。事物不能穷尽，所以最终以未济卦收尾。

【杂卦传】

乾刚健，坤柔顺。比卦使人喜乐，师卦使人忧虑。临与观的意义，有时在于给予，有时在于求取。屯卦显现生机而不失其所居，蒙卦阴阳杂合而显明。震是兴起，艮是停止。损与益是盛衰变化的开端。大畜体现时机，无妄体现灾异。萃是聚集，升则上升而不返回。谦是轻己，豫则容易懈怠。噬嗑是进食咬合，贲是文饰而归于无色。兑显现于外，巽潜伏于内。随是不拘旧故，蛊则是整治修饰。剥是腐坏，复是返回。晋象征白昼，明夷象征光明受伤。井意味着通达，困意味着相遇受阻。咸是迅速感应，恒是长久持续。涣是离散，节是止限。解是缓解，蹇是艰难。睽表现于外，家人属于内部。否与泰是两类状态相互反转。大壮应当有所停止，遁则意味着退避。大有是众多，同人是亲近。革是除去旧物，鼎是取得新物。小过是有所超过，中孚是诚信。丰是事多而盛大，亲近者少便是旅居。离在上，坎在下。小畜所蓄不多，履则不肯停留。需是不前进，讼是不亲近。大过是颠覆。姤是相遇，是柔爻遇到刚爻。渐是女子出嫁，要等待男子前来迎娶。颐是养育正道。既济是事情已经安定。归妹是女子婚嫁人生阶段的终结。未济是男子事业未成、困顿至极。夬是决断，以刚决去柔；君子之道增长，小人之道忧惧。

译注说明：卦象取象和部分古义存在不同解释；本译尽量直译传文，不把李鼎祚所辑注家的象数解释混入传文本身。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十五·系辞下传后段连续原文补录", "底本：景印文渊阁《四库全书》本《周易集解》卷十五，参校维基文库四库本卷十五正文第 427—559 行。承接既有前段条目“困于石，据于蒺藜，入于其宫，不见其妻，凶”之后，自“子曰：非所困而困焉，名必辱”起，至卷末“知之，未尝复行也”及虞翻注文止。保留虞翻、陆绩、孔颖达、荀爽、崔憬、姚信、干宝、侯果、翟玄、《九家易》等署名；数字转录中疑字及引文仍须逐字对照影印页，不据文意擅改。", """
子曰：非所困而困焉，名必辱。
虞翻曰：困本咸，咸三入宫，以阳之阴则二制坤，故以决咸为四所困。四失位恶人，故非所困而困焉。阳称名，阴为辱，以阳之阴下，故名必辱也。

非所据而据焉，身必危。
虞翻曰：谓据二，二失位，故非所据而据焉。二变时坤为身，二折坤体，故身必危。

既辱且危，死期将至，妻其可得见耶？
陆绩曰：六三从困辱之家，变之大过，为棺椁死丧之象，故曰死期将至，妻不可得见。

易曰：公用射隼于高墉之上，获之，无不利。
孔颖达曰：前章先须安身，可以崇德，故此明藏器于身，待时而动，是有利也，故引解之上六以证之矣。

子曰：隼者，禽也。
虞翻曰：离为隼，故称禽，言其行野容如禽兽焉。

弓矢者，器也。
虞翻曰：离为矢，坎为弓，坤为器。

射之者，人也。
虞翻曰：人，贤人也。谓乾三伏阳出而成乾，故曰射之者人。人则公，三应上，故上令三出而射隼也。

君子藏器于身，待时而动，何不利之有？
虞翻曰：三伏阳为君子，二变时坤为身、为藏器，为藏弓矢以待射隼。艮为待、为时，三待五来之二，弓张矢发动出成乾，贯隼入大过死，两坎象坏，故何不利之有。象曰以解悖，三阴小人乘君子器，故上观三出射去隼也。

动而不括，是以出而有获，语成器而动者也。
虞翻曰：括，作也。震为语，乾五之坤二成坎弓、离矢，动以贯隼，故语成器而动者也。

子曰：小人不耻不仁，不畏不义。
虞翻曰：谓否也。以坤灭乾为不仁、不义，坤为耻、为畏，乾为仁、为义者也。

不见利不劝，不威不惩。
虞翻曰：否乾为威、为利，巽为近利，谓否五之初成噬嗑市。离日见，乾为见利，震为动，故不见利不动。五之初以乾威坤，故不威不惩，震为惩也。

小惩而大诫，此小人之福也。
虞翻曰：艮为小，乾为大，五下威初，坤杀不行，震惧虩虩，故小惩大诫。坤为小人，乾为福，以阳下阴，民说无疆，故小人之福也。

易曰：屦校灭趾，无咎。此之谓也。
《九家易》曰：噬嗑六五本先在初，处非其位，小人者也。故历说小人所以为罪，终以致害；虽欲为恶，能止不行，则无咎。
侯果曰：噬嗑初九爻辞也。校者，以木夹足止行也。此明小人因小刑而大诫，乃福也。

善不积，不足以成名。
虞翻曰：乾为积善，阳称名。

恶不积，不足以灭身。
虞翻曰：坤为积恶、为身，以乾灭坤，故灭身者也。

小人以小善为无益而弗为也。
虞翻曰：小善为复初。

以小恶为无伤而弗去也。
虞翻曰：小恶谓姤初。

故恶积而不可掩，罪大而不可解。
虞翻曰：谓阴息姤至遯，子弑其父，故恶积而不可掩。
虞翻曰：阴息遯成否，以臣弑君，故罪大而不可解也。

易曰：何校灭耳，凶。
《九家易》曰：噬嗑上九爻辞也。阴自初升五，所在失正，积恶而罪大，故为上所灭。善不积，斥五阴爻也。聪不明者，闻善不听，闻戒不改，故凶也。

子曰：危者，安其位者也；亡者，保其存者也；乱者，有其治者也。
崔憬曰：言有危之虑，则能安其位不失也；言有亡之虑，则能保其存者也；言有防乱之虑，则能有其治者也。

是故君子安而不忘危。
虞翻曰：君子大人谓否五也。否坤为安，危谓上也。
翟玄曰：在安虑危。

存而不忘亡。
荀爽曰：谓除戎器，戒不虞也。
翟玄曰：在存而虑亡。

治而不忘乱。
荀爽曰：谓思患而逆防之。
翟玄曰：在治而虑乱。

是以身安而国家可保也。
虞翻曰：坤为身，谓否反成泰，君位定于内而臣忠于外，故身安而国家可保也。

易曰：其亡其亡，系于包桑。
荀爽曰：存不忘亡也。
荀爽曰：桑者，上玄下黄，乾坤相包以正，故不可忘也。
陆绩曰：自此以上皆谓否阴灭阳之卦，五在否家，虽得中正常，自惧以危亡之事者也。

子曰：德薄而位尊。
虞翻曰：鼎四也，则离九四凶恶小人，故德薄；四在乾位，故位尊。

知小而谋大。
虞翻曰：兑为小知，乾为大谋，四在乾体，故谋大矣。

力少而任重。
虞翻曰：五至初体大过，本末弱，故力少也。乾为仁，故任重；以为己任，不亦重乎？

鲜不及矣。
虞翻曰：鲜，少也，及于刑矣。

易曰：鼎折足，覆公餗，其形渥，凶。言不胜其任也。
孔颖达曰：言不能安身，智小谋大而遇祸也，故引鼎九四以证之矣。

子曰：知几其神乎！
虞翻曰：几谓阳也。阳在复初称几，此谓豫四也。恶鼎四折足，故以此次言豫四知几而反复初也。

君子上交不谄，下交不渎。
虞翻曰：豫上谓四也。四失位，谄渎上谓交五，五贵，震为笑言，笑言且谄也；下谓交三，坎为渎。故下交不渎，欲其复初得正元吉，故其知几乎。

其知几乎！
侯果曰：上谓王侯，下谓凡庶。君子上交不至谄媚，下交不至渎慢，悔吝无从而生，岂非知微者乎？

几者，动之微，吉之先见者也。
虞翻曰：阳见初成震，故动之微；复初元吉，吉之先见者也。
韩康伯曰：几者，去无入有，理而未形者；不可以名寻，不可以形睹也。唯神也，不疾而速，感而遂通，故能玄照鉴于未形也。合抱之木起于毫末，吉凶之彰始乎微兆，故言吉之先见。

君子见几而作，不俟终日。《易》曰：介于石，不终日，贞吉。介如石焉，宁用终日？断可识矣。
孔颖达曰：前章云精义入神，此明知几入神之事，故引豫之六二以证之。
崔憬曰：此爻得位居中，于豫之时能顺以动而防于豫，如石之耿介守志不移；虽暂豫乐，以其见微而不终日，则能贞吉，断可知矣。

君子知微知彰，知柔知刚。
姚信曰：此谓豫卦也。二下交初，故曰知微；上交于三，故曰知彰；体坤处和，故曰知柔；与四同功，故曰知刚。

万夫之望。
荀爽曰：圣人作而万物睹。
干宝曰：言君子苟达于此，则万夫之望矣。周公闻齐鲁之政，知后世强弱之势；辛有见被发而祭，则知为戎狄之居。凡若此类，可谓知几也。皆称君子，君子则以得几，不必圣者也。

子曰：颜氏之子，其殆庶几乎！
虞翻曰：几者神妙也。颜子知微，故殆庶几。孔子曰：“回也其庶几乎。”

有不善，未尝不知。
虞翻曰：复以自知。老子曰：“自知者明。”

知之，未尝复行也。
虞翻曰：谓颜回不迁怒、不贰过、克己复礼，天下归仁。
""", chatGptTranslation = """**ChatGPT 翻译（逐段工作译文）**

**孔子说：不该陷入困境却陷入困境，名声必定受辱。**

虞翻解释：困卦原本与咸卦相通。咸卦九三进入宫中，阳爻转入阴位，二爻便形成坤体；因此咸卦发生变化后，受到九四的困阻。九四失位，属于注家所说的恶人，所以说不该受困却陷入困境。阳象征名声，阴象征屈辱；阳居阴下，因此名声必定受辱。

**不该占据的位置却强行占据，自身必定危险。**

虞翻解释：这里指占据九二的位置。九二失位，所以说不该占据却占据。九二变化时，坤象征身体；二爻破坏坤体，因此自身必然危险。

**既受屈辱又处于危险之中，死亡的期限将近，妻子哪里还能够见到呢？**

陆绩解释：六三处在困辱之家，变化为大过卦，形成棺椁和死亡丧葬的象征，所以说死亡将近，妻子也无法再见。

**《易》说：公在高墙之上射猎隼鸟，捕获它，没有什么不利。**

孔颖达解释：前文先说明安顿自身，才能增进德行；这里进一步说明把才能和器具藏在自身，等待时机再行动，就会有利。因此引用解卦上六爻辞作为证明。

**孔子说：隼是一种鸟类。**

虞翻解释：离卦象征隼，所以称为禽鸟；这里说它行动于野外，形态如同禽兽。

**弓和箭，是器具。**

虞翻解释：离象征箭，坎象征弓，坤象征器具。

**射猎的人，是人。**

虞翻解释：这里的人指贤人。乾三伏藏的阳爻显现而形成乾，所以说射猎者是人。所谓“公”，指九三与上六相应，因此上位者命令九三出来射隼。

**君子把器具藏在自身，等待时机再行动，还有什么不利呢？**

虞翻解释：九三伏藏的阳爻象征君子。九二变化时，坤象征身体，也象征收藏器具，表示把弓箭藏好，等待射隼的时机。艮象征等待，也象征时机。九三等待九五来到九二，弓张开、箭射出，发动后形成乾，箭贯穿隼鸟而进入大过卦；两重坎象被破坏，所以说没有什么不利。象辞所说“以解悖”，是因为三爻阴柔小人乘用君子的器具，上位者观察九三出而射去隼鸟。

**行动而不受阻碍，因此出发便有所收获；这是说先备好器具再行动。**

虞翻解释：“括”是结、阻碍的意思。震象征言语；乾五来到坤二，形成坎弓、离箭，发动而射中隼鸟，所以说先备好器具再行动。

**孔子说：小人不以不仁为羞耻，也不畏惧不义。**

虞翻解释：这里说的是否卦。坤消灭乾，象征不仁、不义；坤象征羞耻和畏惧，乾象征仁与义。

**看不到利益就不会受到劝勉，没有威势就不会受到惩戒。**

虞翻解释：否卦的乾象征威势和利益，巽象征趋近利益。否卦九五来到初位形成噬嗑，离象征市场和太阳显现；乾象征看见利益，震象征行动，所以看不到利益便不行动。九五来到初位，以乾的威势制约坤，所以没有威势便不能惩戒；震象征惩戒。

**小小惩戒而得到大的警诫，这是小人的福分。**

虞翻解释：艮象征小，乾象征大。九五下降而威慑初爻，坤所象征的杀伐不再施行，震表现出警惧，所以小惩而大诫。坤象征小人，乾象征福；阳爻居于阴爻之下，百姓因此喜悦无边，所以这是小人的福分。

**《易》说：“脚上戴着刑具，遮没脚趾，没有灾咎。”说的就是这种情形。**

九家易解释：噬嗑六五原本先处在初位，位置不当，象征小人。因此逐层说明小人如何犯罪并最终招致祸害；如果能够及时停止作恶，就可以没有灾咎。侯果解释：这是噬嗑初九爻辞。校是用木板夹住脚、阻止行走的刑具。这里说明小人受到较轻的刑罚后能够得到较大的警戒，这反而是福。

**善行不积累，就不足以成就好的名声。**

虞翻解释：乾象征积累善行，阳象征名声。

**恶行不积累，就不足以造成自身毁灭。**

虞翻解释：坤象征积累恶行，也象征身体；乾消灭坤，所以说恶行积累会毁灭自身。

**小人把小善看作没有益处，因此不去做。**

虞翻解释：这里的小善指复卦初爻所象征的善端。

**把小恶看作没有伤害，因此不肯除去。**

虞翻解释：这里的小恶指姤卦初爻所象征的阴气初生。

**所以恶行积累起来便无法掩盖，罪行重大便无法解除。**

虞翻解释：阴气从姤卦逐渐增长到遯卦，出现儿子弑父的象征，所以恶行积累便无法掩盖。阴气继续增长，遯卦变成否卦，出现臣下弑君的象征，因此罪行重大而无法解除。

**《易》说：“戴着沉重的刑具，遮没耳朵，凶险。”**

九家易解释：这是噬嗑上九爻辞。阴爻从初位上升到五位，却处在不正的位置，恶行积累、罪责重大，所以最终被上位者惩治。“善不积”所指的是五位的阴爻。所谓听不清善言，就是听到劝善也不接受，听到警戒也不改正，所以凶险。

**孔子说：处在危险中的人，正是因为曾经安于自己的位置。**

崔憬解释：能够预先考虑危险，才可以安定自己的位置而不失去它。

**将要灭亡的人，正是因为曾经保有生存的条件。**

崔憬解释：能够考虑灭亡的可能，才可以保全已有的生存条件。

**发生混乱的人，正是因为曾经拥有治理的局面。**

崔憬解释：能够预先防范混乱，才可以保持治理的秩序。

**所以君子安定时不忘危险，生存时不忘灭亡，治理时不忘混乱，因此自身可以安定，国家也能够保全。**

虞翻解释：坤象征身体，这里说否卦反转为泰卦，君位在内安定，臣下在外忠诚，所以自身安定，国家可以保全。荀爽解释：身处安定时仍要整治兵器、戒备意外。翟玄解释：安定时考虑危险，生存时考虑灭亡，治理时考虑混乱。

**《易》说：“将要灭亡了，将要灭亡了！好比系在包裹桑树上的绳索。”**

荀爽解释：这是说即使处于生存状态，也不忘记灭亡的危险。陆绩解释：桑树上部呈玄色、下部呈黄色，象征乾坤相互包合而端正，因此不可忘记警戒。此前所说都属于否卦阴气消灭阳气的情形；九五虽然得中得正，仍然以危亡之事警惕自己。

**孔子说：德行浅薄却地位尊贵，智慧狭小却谋划宏大，力量微弱却承担重任，很少有人不会遭遇祸患。**

虞翻解释：这里指鼎卦九四。离卦九四是凶恶的小人，所以说德薄；九四处在乾所象征的尊位，所以说位尊。兑象征小的智慧，乾象征宏大的谋划；九四处在乾体之中，所以谋划宏大。九五来到初位形成大过，本末都弱，所以力量少。乾象征仁德，因此承担重任。虞翻又解释：“鲜”是少的意思，这里指很少有人不会受到刑罚。

**《易》说：“鼎足折断，倾覆了公家的食物，污秽沾满身体，凶险。”这是说不能胜任所承担的责任。**

孔颖达解释：这是说不能安顿自身，智慧不足却谋划过大，最终遭遇祸患，所以引用鼎卦九四作为证明。

**孔子说：知道事物细微的征兆，大概就是神妙的智慧吧！**

虞翻解释：“几”指阳气。阳气在复卦初爻出现，称为细微征兆；这里指豫卦九四。鼎卦九四因恶行而折足，因此接下来讲豫卦九四能够察觉征兆，返回复卦初爻。

**君子与上位者交往不谄媚，与下位者交往不轻慢，这就是懂得细微征兆吧？**

虞翻解释：豫卦上体所说的是九四。九四失位，对上交往五爻时有谄媚之象；五爻尊贵，震象征笑言，笑言过度便成为谄媚。向下交往三爻，坎象征亵渎，所以说向下交往不轻慢。希望九四返回初位而得正，便能大吉。侯果解释：上指王侯，下指普通百姓。君子对上不谄媚，对下不轻慢，悔恨和过失便无从产生，这难道不是洞察细微的人吗？

**细微征兆，是行动刚刚萌动的迹象，也是吉祥最先显现的征兆。**

虞翻解释：阳气在初位显现而形成震，所以称为行动的微兆；复卦初九元吉，是吉祥最先显现。韩康伯解释：几是从无进入有、道理已经萌生而形体尚未显现的状态，不能用名称寻求，也不能从外形看见。只有神妙的洞察力能够迅速感通，照见尚未成形的事物。合抱的大树从细小的萌芽开始，吉凶的显现也始于微小征兆，所以说吉祥会先从细微处显现。

**君子看见细微征兆便立即行动，不等到一天结束。《易》说：“坚如磐石，不等到终日，守正吉祥。”像石头一样坚定，哪里还需要等到终日？事情的判断已经十分清楚。**

孔颖达解释：前文说精研义理而进入神妙境界，这里说明洞察征兆也能达到神妙，所以引用豫卦六二作为证明。崔憬解释：此爻得位居中，在豫乐之时能够顺势行动并防止沉溺，好比石头坚定不移。虽然处于安乐之中，却能看见微兆，不等到一天结束便采取行动，因此守正吉祥，结果可以断定。

**君子懂得细微与显著、柔顺与刚强，是众人仰望的对象。**

姚信解释：这里说的是豫卦。六二向下与初爻交往，所以说知微；向上与三爻交往，所以说知彰；卦体为坤而居于和顺之处，所以说知柔；与九四同功，所以说知刚。荀爽解释：这是圣人出现而万物得以看见的意思。干宝解释：君子如果真正通达这些道理，就会成为众人仰望的对象。周公听闻齐、鲁两国的政事，便预知后世强弱的趋势；辛有看见披发祭祀的情形，便知道那里将成为戎狄居住之地。类似这些事，都可以称为知几。这里称君子，并不意味着只有圣人才有这种能力。

**孔子说：颜回这个人，大概接近于懂得细微征兆的境界了吧？**

虞翻解释：几是神妙的征兆。颜回能够洞察细微，所以说他接近于知几。孔子曾说：“颜回大概接近于这个境界吧！”

**有不善之处，他没有不知道的；知道以后，也从不再犯。**

虞翻解释：这是能够反省而自知。老子说：“自知者明。”虞翻又解释：这是说颜回不迁怒、不重复犯错，能够克制自己、回归礼义，因此天下归于仁德。

译释说明：本段大量注文以卦变、爻位解释《系辞》，译文分别保留虞翻、荀爽、陆绩、韩康伯、孔颖达等人的归属。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十六·系辞下经传正文", "底本：景印文渊阁《四库全书》本《周易集解》卷十六，参校品古籍四库本第十六卷数字文本（页面标示本卷6,561字）。本条先补录卷十六所承载的《系辞下传》经传正文，未将李鼎祚所辑虞翻、荀爽、韩康伯、崔憬等注文冒充经文；注文全文仍需另行整理。", """
易曰：“不远复，无祇悔，元吉。”
天地絪缊，万物化醇；男女构精，万物化生。
《易》曰：“三人行，则损一人；一人行，则得其友。”言致一也。
子曰：“君子安其身而后动，易其心而后语，定其交而后求。君子修此三者，故全也。危以动，则民不与也；惧以语，则民不应也；无交而求，则民不与也。莫之与，则伤之者至矣。”
《易》曰：“莫益之，或击之，立心勿恒，凶。”
子曰：“乾坤其《易》之门邪？乾，阳物也；坤，阴物也。阴阳合德，而刚柔有体，以体天地之撰，以通神明之德。其称名也，杂而不越；于稽其类，其衰世之意邪？
夫《易》，彰往而察来，而微显阐幽，开而当名，辨物正言，断辞则备矣。其称名也小，其取类也大；其旨远，其辞文；其言曲而中，其事肆而隐。因贰以济民行，以明失得之报。
《易》之兴也，其于中古乎？作《易》者，其有忧患乎？
是故履，德之基也；谦，德之柄也；复，德之本也；恒，德之固也；损，德之修也；益，德之裕也；困，德之辨也；井，德之地也；巽，德之制也。
履，和而至；谦，尊而光；复，小而辨于物；恒，杂而不厌；损，先难而后易；益，长裕而不设；困，穷而通；井，居其所而迁；巽，称而隐。
履以和行，谦以制礼，复以自知，恒以一德，损以远害，益以兴利，困以寡怨，井以辨义，巽以行权。
《易》之为书也，不可远；为道也，屡迁。变动不居，周流六虚；上下无常，刚柔相易；不可为典要，唯变所适。其出入以度，外内使知惧；又明于忧患与故。无有师保，如临父母。
初率其辞而揆其方，既有典常；苟非其人，道不虚行。
《易》之为书也，原始要终，以为质也；六爻相杂，唯其时物也。其初难知，其上易知，本末也。初辞拟之，卒成之终。若夫杂物撰德，辨是与非，则非其中爻不备。噫，亦要存亡吉凶，则居可知矣。
知者观其彖辞，则思过半矣。二与四同功而异位，其善不同，二多誉，四多惧，近也。柔之为道，不利远者；其要无咎，其用柔中也。三与五同功而异位，三多凶，五多功，贵贱之等也。其柔危，其刚胜邪？
《易》之为书也，广大悉备。有天道焉，有人道焉，有地道焉。兼三才而两之，故六；六者非他也，三才之道也。道有变动，故曰爻；爻有等，故曰物；物相杂，故曰文；文不当，故吉凶生焉。
《易》之兴也，其当殷之末世、周之盛德邪？当文王与纣之事邪？是故其辞危。危者使平，易者使倾；其道甚大，百物不废。惧以终始，其要无咎，此之谓《易》之道也。
夫乾，天下之至健也，德行恒易以知险；夫坤，天下之至顺也，德行恒简以知阻。能说诸心，能研诸侯之虑，定天下之吉凶，成天下之亹亹者。
是故变化云为，吉事有祥；象事知器，占事知来。天地设位，圣人成能；人谋鬼谋，百姓与能。
八卦以象告，爻彖以情言，刚柔杂居而吉凶可见矣。变动以利言，吉凶以情迁。是以爱恶相攻而吉凶生，远近相取而悔吝生，情伪相感而利害生。
凡《易》之情，近而不相得则凶；或害之，悔且吝。
将叛者，其辞惭；中心疑者，其辞枝；吉人之辞寡；躁人之辞多；诬善之人，其辞游；失其守者，其辞屈。
""", chatGptTranslation = """ChatGPT 翻译（逐段工作译文）：

《易》说：“不远离正道而及时复归，不会留下大的悔恨，大吉。”

天地之气交融密合，万物因此化育而醇厚；男女交合精气，万物因此化生。

《易》说：“三个人同行，就会减少一个人；一个人独行，就会得到他的同伴。”这是说，最终要归于专一。

孔子说：“君子先使自身安定，然后才行动；先使内心平和，然后才发言；先把交往关系确定下来，然后才有所求。君子修养这三件事，所以能够保全自身。处境危险却贸然行动，百姓不会支持；心怀恐惧却发出言语，百姓不会响应；没有交情却向人求取，百姓不会给予帮助。没有人支持，伤害自己的人就会到来。”

《易》说：“没有人增益他，反而有人攻击他；立志不能持久，凶险。”

孔子说：“乾与坤，大概就是《易》的门户吧？乾属于阳类，坤属于阴类。阴阳之德相互配合，刚柔各有形体；借此体现天地所创造的事物，贯通神明的德性。卦爻所用的名称虽然繁杂，却不越出事理；考察这些类别，大概反映了衰世的情形吧？

《易》能够彰明过去、考察未来，使微小者显现、幽隐者阐明；使事物显露并获得恰当名称，辨别事物、端正言辞，最后用断语作出判断，这样内容就完备了。它所使用的名称虽小，取譬的范围却广大；旨意深远，文辞有文采；言语曲折却切中事理，所叙之事铺陈开来却含有深意。它借助事物的两端和差异，帮助百姓行动，并说明得失所带来的报应。

《易》兴起，大概是在中古时代吧？创作《易》的人，想必经历过忧患吧？

所以，履卦是德行的根基，谦卦是德行的把柄，复卦是德行的根本，恒卦使德行稳固，损卦使德行得到修治，益卦使德行充实宽裕，困卦使德行经受辨验，井卦是德行赖以安定的处所，巽卦是德行施行时的节制方法。

履是和顺而达到恰当境地；谦是尊贵而光明；复虽处于细微阶段，却能辨明事物；恒虽处于纷杂之中，却不会厌倦；损是先经历困难而后变得容易；益是不断增长充裕，并非勉强造作；困是身处穷境却仍能通达；井是安居其所而能施惠迁流；巽是称量适宜而含蓄不露。

用履卦使行为和顺，用谦卦制定礼节，用复卦反省认识自己，用恒卦专一德行，用损卦远离祸害，用益卦兴办有益之事，用困卦减少怨尤，用井卦辨明义理，用巽卦灵活行使权宜。

《易》这部书不可远离；它所体现的道不断迁移变化。变化运行没有固定停留之处，周流于六个爻位；上下没有恒常不变的位置，刚柔相互更替。不能为它制定僵硬固定的准则，只能随变化选择合宜之道。它的出入有法度，使人无论处于内外都知道警惕；它又阐明忧患及其缘由。即使没有老师和保护者，也应当像面对父母一样谨慎。

起初依照卦爻辞来推求其方向，便能掌握一定的常则；但如果不是适合承担此道的人，《易》道也不会自行施行。

《易》这部书以追溯事物开端、推究事物终结为根本；六爻交错，呈现的只是各自所处的时势与事物。初爻难以理解，上爻容易理解，这是因为一个处在开端，一个处在末端。初爻的辞是对事物的拟议，上爻则显示事情最终形成的结果。至于杂合事物、撰述德性、辨别是非，如果不考察中间各爻，内容便不能完备。至于存亡吉凶的要点，静观其所处位置也就可以知道。

明智的人观察卦的彖辞，思考所得便已经超过一半。第二爻与第四爻功用相同而位置不同，所表现的善处也不同：第二爻多有称誉，第四爻多有忧惧，因为第四爻更接近尊位。柔顺之道不宜过分远离刚健者；关键在于无咎，其运用贵在柔顺而得中。第三爻与第五爻功用相同而位置不同，遭遇却不同：第三爻多凶险，第五爻多功绩，这是尊贵与卑贱的等级差异。柔爻处在这些位置便危险吗？刚爻处在这些位置便较能胜任吗？

《易》这部书广大而完备，其中包含天道、人道和地道。兼具三才并各自两分，所以形成六爻；所谓六爻，并非别的，就是三才之道。道发生变化，所以称为爻；爻有不同等级，所以称为物；事物相互杂合，所以形成文采；文采若不合宜，吉凶便由此产生。

《易》兴起，大概正当殷朝末世、周朝德业兴盛之时吧？大概与文王和纣王的事迹有关吧？因此《易》的言辞带有警戒意味。危险使人转向平安，轻忽则使人走向倾覆；《易》道极其广大，没有一种事物被排除在外。始终保持警惧，最终便能没有灾咎，这就是《易》道。

乾是天下最为刚健的，其德行恒常平易，因此能够认识险难；坤是天下最为柔顺的，其德行恒常简约，因此能够认识阻碍。乾坤之道能够使人心悦诚服，能够研讨诸侯的谋虑，确定天下的吉凶，并成就天下持续不息的事业。

因此，变化与言行之中，吉祥之事会出现征兆；观察象可以认识器物，占问事情可以预知未来。天地设定位置，圣人使其功能得以成就；人谋与鬼谋相互配合，百姓也能参与其中。

八卦用象来告知，爻辞与彖辞用情势来表达；刚柔交错处在一起，吉凶便可以显现。变化运动以利益为判断依据，吉凶则随着情势而转移。因此，喜爱与厌恶相互攻伐，吉凶便产生；远近相互取应，悔恨与困难便产生；真实与虚伪相互感应，利益与损害便产生。

凡是《易》所呈现的情势，彼此接近却不能相得，便会有凶险；有时还会受到伤害，进而产生悔恨与困难。

将要背叛的人，说话时会显得惭愧；内心疑惑的人，言辞会支离分散；吉善之人的言辞简少；急躁之人的言辞繁多；诬陷善人的人，言辞游移不定；失去操守的人，言辞屈曲不直。

译注提示：“絪缊”“致一”“典要”“亹亹”等词有训诂分歧；此处按上下文作工作译述，不能代替对虞翻、韩康伯、孔颖达等注文的分别翻译。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十六·系辞下传注文（上段）", "底本：景印文渊阁《四库全书》本《周易集解》卷十六，参校维基文库四库本卷十六全文。此条录入本卷前段注文，范围自“不远复”至“苟非其人，道不虚行”；经文已另列于卷十六经传正文条目。本批先按底本次序分注家录入，异体字及个别疑似 OCR 字尚待影印页逐字复核，不将疑字擅改为定本。", """
《易》曰：“不远复，无祇悔，元吉。”
侯果曰：复初九爻辞。殆，近也；庶，冀也。此明知微之难。则知微者唯圣人耳。颜子亚圣，但冀近于知微而未得也。在微则昧，理彰而悟，失在未形，故“有不善”；知则速改，故无大过。

天地絪缊，万物化醇。
虞翻曰：谓泰上也。先说否，否反成泰，故不说泰。天地交，万物通，故“化醇”。
孔颖达曰：以前章利用安身以崇德也。安身之道，在于得一。若已能得一，则可以安身，故此章明得一之事也。絪缊，气附著之义。言天地无心，自然得一；唯二气絪缊，共相和会，感应变化，而有精醇之生，万物自化。若天地有心为一，则不能使万物化醇者也。

男女构精，万物化生。
虞翻曰：谓泰初之上成损，艮为男，兑为女，故“男女构精”。乾为精，损反成益，万物出震，故“万物化生”也。
干宝曰：男女犹阴阳也，故“万物化生”。不言阴阳，而言男女者，以指释损卦六三之辞，主于人事也。

《易》曰：“三人行，则损一人；一人行，则得其友。”言致一也。
侯果曰：损六三爻辞也。《象》云“一人行，三则疑”，是众不如寡，三不及一。此明物情相感，当上法絪缊、化醇、致一之道，则无患累者也。

子曰：“君子安其身而后动。”
虞翻曰：谓反损成益，君子益初也。坤为安身，震为后动。
崔憬曰：君子将动有所为，必自揣安危之理在于己身，然后动也。

“易其心而后语。”
虞翻曰：乾为易，益初体复心，震为后语。
崔憬曰：君子恕己及物，若于事心虽不可，出语必和；易其心而后言。

“定其交而后求。”
虞翻曰：震专为定，为后交；谓刚柔始交，艮为求也。
崔憬曰：先定其交，知其才行，若好施与、吝，然后可以事求之。

“君子修此三者，故全也。”
虞翻曰：谓否上之初，损上益下，其道大光；自上下下，民说无疆，故全也。

“危以动，则民不与也。”
虞翻曰：谓否上九高而无位，故危；坤为民，否闭故弗与也。

“惧以语，则民不应也。”
虞翻曰：否上穷灾故惧，来下之初成益，故民不应；坤为民，震为应也。

“无交而求，则民不与也。”
虞翻曰：上来之初故交，坤民否闭故不与，震为交。

“莫之与，则伤之者至矣。”
虞翻曰：上下之初，否消灭乾，则体剥；伤臣弑君、子弑父，故伤之至矣。

《易》曰：“莫益之，或击之，立心勿恒，凶。”
侯果曰：益上九爻辞也。此明先安身、易心，则群善自应；若危动、惧语，则物所不与，故凶也。

子曰：“乾坤其《易》之门邪？”
荀爽曰：阴阳相易，出于乾坤，故曰“门”。

“乾，阳物也；坤，阴物也。”
荀爽曰：阳物，天；阴物，地也。

“阴阳合德，而刚柔有体。”
虞翻曰：合德谓天地杂，保大和，日月战；乾刚以体天，坤柔以体地也。

“以体天地之撰。”
《九家易》曰：撰，数也。万物形体皆受天地之数也，谓九天数、六地数，刚柔得以为体矣。

“以通神明之德。”
《九家易》曰：隐藏谓之神，著见谓之明；阴阳交通，乃谓之德。

“其称名也，杂而不越。”
《九家易》曰：阴阳，杂也；名谓卦名。阴阳虽错，而卦象各有次序，不相逾越。

“于稽其类，其衰世之意邪？”
虞翻曰：稽，考也。三称盛德，上称末世。乾终上九，动则入坤；坤弑其君父，故为乱臣。阳出复震，入坤出坤，故为衰世之意邪。
侯果曰：于，嗟也；稽，考也。易象考其事类，但以吉凶得失为主，则非淳古之时也，故云“衰世之意”耳。言“邪”，示疑，不欲切指也。

“夫《易》，彰往而察来，而微显阐幽，开而当名。”
虞翻曰：神以知来，智以藏往。微者显之，谓从复成乾，是察来也；阐者幽之，谓从姤之坤，是彰往也。阳息出初，故“开而当名”。

“辨物正言，断辞则备矣。”
干宝曰：辨物，类也；正言，言正义也；断辞，断吉凶也。如此，则备于经矣。

“其称名也小。”
虞翻曰：谓乾坤与六子俱名八卦而小成，故小；复小而辨于物者矣。

“其取类也大。”
虞翻曰：谓乾阳也，为天为父，触类而长之，故大。

“其旨远，其辞文。”
虞翻曰：远谓乾，文谓坤也。

“其言曲而中，其事肆而隐。”
虞翻曰：曲，屈也；肆，直也。阳曲，初震为言，故其言曲而中；坤为事，隐未见，故肆而隐也。

“因贰以济民行，以明失得之报。”
虞翻曰：二谓乾与坤也。坤为民，乾为行；行得则乾报以吉，行失则坤报以凶也。

《易》之兴也，其于中古乎？作《易》者，其有忧患乎？
虞翻曰：兴《易》者谓庖牺也。文王书经，系庖牺于乾五；乾为古，五在乾中，故兴于中古。系以黄帝、尧、舜为后世圣人，庖牺为中古，则庖牺以前为上古。
虞翻曰：谓忧患百姓未知兴利远害，不行礼义，茹毛饮血，衣食不足；庖牺则天八卦通为六十四，以德化之，吉凶与民同患，故有忧患。

“是故履，德之基也。”
虞翻曰：乾为德，履与谦旁通，坤柔履刚，故德之基；坤为基。
侯果曰：履，礼也；蹈礼不倦，德之基也。自下九卦是复道之最，故特言矣。

“谦，德之柄也。”
虞翻曰：坤为柄，柄本也；凡言德皆阳爻也。
干宝曰：柄所以持物，谦所以持礼者也。

“复，德之本也。”
虞翻曰：复初，乾之元，故德之本也。

“恒，德之固也。”
虞翻曰：立不易方，守德之坚固。

“损，德之修也。”
荀爽曰：惩忿窒欲，所以修德。

“益，德之裕也。”
荀爽曰：见善则迁，有过则改，德之优裕也。

“困，德之辨也。”
郑玄曰：辨，别也。遭困之时，君子固穷，小人穷则滥，德于是别也。

“井，德之地也。”
姚信曰：井养而不穷，德居地也。

“巽，德之制也。”
虞翻曰：巽风为号令，所以制下，故曰“德之制也”。
孔颖达曰：此上九卦，各以德为用也。

“履，和而至。”
虞翻曰：谦与履通，谦坤柔和，故“履和而至”；礼之用，和为贵者也。

“谦，尊而光。”
荀爽曰：自上下下，其道大光也。

“复，小而辨于物。”
虞翻曰：阳始见，故“小”。乾，阳物；坤，阴物。以乾居坤，故称别物。

“恒，杂而不厌。”
荀爽曰：夫妇虽错居，不厌之道也。

“损，先难而后易。”
虞翻曰：损初之上，失正，故“先难”；终反成益，得位于初，故“后易”。

“益，长裕而不设。”
虞翻曰：谓天施地生，其益无方；凡益之道，与时偕行，故“不设”也。

“困，穷而通。”
虞翻曰：阳穷否上，变之坤二，成坎；坎为通，故“困穷而通”也。

“井，居其所而迁。”
韩康伯曰：改邑不改井，井所居不移，而能迁其施也。

“巽，称而隐。”
崔憬曰：言巽申命行事，是称扬也；阴助德化，是微隐也。自此以下，明九卦德之体者也。

“履以和行。”
虞翻曰：礼之用和为贵，谦震为行，故以和行也。

“谦以制礼。”
虞翻曰：阴称礼，谦三以一阳制五阴，万民服，故以制礼也。

“复以自知。”
虞翻曰：有不善，未尝不知，故自知也。

“恒以一德。”
虞翻曰：恒德之固，立不易方，从一而终，故一德者也。

“损以远害。”
虞翻曰：坤为害，泰以初止坤上，故远害；乾为远。

“益以兴利。”
荀爽曰：天施地生，其益无方，故兴利也。

“困以寡怨。”
虞翻曰：坤为怨，不弑父与君，乾来上拆坤二，故寡怨；坎水性通，故不怨也。

“井以辨义。”
虞翻曰：坤为义，以乾别坤，故辨义也。

“巽以行权。”
《九家易》曰：巽象号令，又为近利；人君政教进退，释利而为权也。《春秋传》曰：“权者，反于经然后有善者也。”此所以说九卦者，圣人履忧济民之所急行也，故先陈其德，中言其性，后叙其用以详之也。西伯劳谦，殷纣骄暴，臣子之礼有常，故创《易》道以辅济君父者也。然其意义广远幽微，孔子指撮解此九卦之德，合三复之道，明西伯之于纣不失上下。

《易》之为书也，不可远。
侯果曰：居则观象，动则玩占，故不可远也。

“为道也，屡迁。”
虞翻曰：迁，徙也。日月周流，上下无常，故屡迁也。

“变动不居，周流六虚。”
虞翻曰：变易动行，六虚，六位也。日月周流，终则复始，故周流六虚。为甲子之旬，辰为虚；坎戊为月，离己为日，入在中宫，其处空虚，故称六虚；五甲如次者也。

“上下无常，刚柔相易。”
虞翻曰：刚柔者，昼夜之象也。在天称上，入地为下，故上下无常也。

“不可为典要，唯变所适。”
虞翻曰：典要，道也。上下无常，故不可为典要；适乾为昼，适坤为夜。
侯果曰：谓六爻刚柔相易，远近恒唯变所适，非有典要。

“其出入以度，外内使知惧。”
虞翻曰：出乾为外，入坤为内；日行一度，故出入以度。出阳知生，入阴惧死，使知惧也。
韩康伯曰：明出入之度，使物知外内之戒也。出入犹行藏，外内犹隐显。遁以远时为吉，丰以幽隐致凶，渐以高显为美，明夷以处昧利贞，此外内之戒也。

“又明于忧患与故。”
虞翻曰：神以知来，故明忧患；智以藏往，故知事故。

“无有师保，如临父母。”
虞翻曰：临，见也。言阴阳施行以生万物，无有师保生成为之者；万物出生皆如父母。孔子曰父母之道天地，乾为父，坤为母。
干宝曰：言《易》道以戒惧为本，所谓惧以终始归无咎也。外谓大夫之从王事，则夕惕若厉；内谓妇人之居室，则无攸遂也。虽无师保切磋之训，其心敬戒常如父母之临己者也。

“初率其辞而揆其方。”
虞翻曰：初，始下也；率，正也，谓修辞立诚；方谓坤也。以乾通坤，故初率其辞而揆其方。
侯果曰：率，修也；方，道也。言修《易》初首之辞而度其终末之道，尽有典常，非虚设也。

“既有典常，苟非其人，道不虚行。”
虞翻曰：其出入以度，故“有典常”。苟，诚也；其人谓乾为贤人。神而明之，存乎其人；不言而信，谓之德行，故“不虚行”也。
崔憬曰：言《易》道深远，若非其圣人，则不能明其道。故知《易》道不虚而自行，必文王然后能弘也。
""",
    chatGptTranslation = """ChatGPT 翻译（逐段工作译文）

《易》说：“不远复，无祇悔，元吉。”侯果说：这是复卦初九爻辞。殆是接近，庶是希望。这里说明认识细微征兆很难，真正能知微的只有圣人。颜回接近圣人，只是希望达到知微，却尚未完全做到。事理微小时人容易昏昧，等道理显明才醒悟；过失发生在尚未显形之时，所以说有不善；知道后迅速改正，因此没有大的过失。

“天地絪缊，万物化醇。”虞翻说：这是泰卦上爻。前文先说否卦，否反转为泰，所以这里不再另说泰。天地交合，万物通达，故称化醇。孔颖达说：前文讲安身以崇德，安身之道在于得一。絪缊是气息交融附着的样子。天地无心造作，自然而然地合于一；阴阳二气交融和会、彼此感应变化，于是产生精醇之气，万物自然化生。若天地有意刻求为一，反而不能使万物化醇。

“男女构精，万物化生。”虞翻说：泰卦初爻上升至上位而成损卦，艮为男、兑为女，所以说男女构精；乾为精，损反转成益，万物从震而出，所以说万物化生。干宝说：男女犹如阴阳，因此万物化生。不说阴阳而说男女，是为了指明损卦六三爻辞，着重在人事。

《易》说：“三人行，则损一人；一人行，则得其友。”这是说要归于专一。侯果说：这是损卦六三爻辞。《象传》说“三则疑”，意思是众多不如寡少，三人不如一人专一。事物彼此感应，应效法天地絪缊、化醇、致一之道，便没有忧患牵累。

孔子说：“君子安其身而后动，易其心而后语，定其交而后求。”虞翻说：这是损卦反转成益卦，君子使初爻得到增益；坤为安身，震为随后行动；乾为平易，震为随后发言；先确定交往，艮为求取。崔憬说：君子准备行动之前，必先衡量自身安危，然后行动。君子推己及人，若自己心中尚不能接受某事，出口之言就应和缓；先平易其心，然后发言。先确定交往关系，了解对方才干品行，知道对方乐于施与还是吝啬，然后才可以向他求取。

“君子修此三者，故全也。”虞翻说：否卦上爻下降至初位，损上益下，其道大为光明；由上而下，百姓喜悦无边，所以能够保全。

“危以动，则民不与也；惧以语，则民不应也；无交而求，则民不与也；莫之与，则伤之者至矣。”虞翻说：否卦上九处高而无位，所以危险；坤为民，否闭不通，百姓便不赞同。否上爻处穷极而有灾，所以恐惧；上爻下降至初位形成益卦，百姓仍不响应。上爻下降到初位才形成交接；坤为民，否闭时百姓不予支持。上下爻位变化使否卦消灭乾体而形成剥卦，象征臣弑君、子弑父，因此伤害到来。

《易》说：“莫益之，或击之，立心勿恒，凶。”侯果说：这是益卦上九爻辞。先安定自身、平易其心，众善自然响应；若危险时贸然行动，恐惧中发言，众人便不支持，所以凶。

孔子说：“乾坤其《易》之门邪？”荀爽说：阴阳相互变化，都从乾坤而出，所以称为门。“乾，阳物也；坤，阴物也。”荀爽说：阳类是天，阴类是地。“阴阳合德，而刚柔有体。”虞翻说：合德指天地交杂、保有太和；乾刚健而体现天，坤柔顺而体现地。“以体天地之撰。”《九家易》说：撰是数，万物形体都禀受天地之数。“以通神明之德。”《九家易》说：隐藏不可见称神，显著可见称明；阴阳交通才称德。“其称名也，杂而不越。”《九家易》说：阴阳交杂，卦名虽由阴阳错综而成，各卦象仍有次序，不相逾越。

“于稽其类，其衰世之意邪？”虞翻说：稽是考察。前文称盛德，后文称末世。乾终结于上九，发动便进入坤；坤象征臣弑君父，所以成为乱臣。阳气从复卦震位出现，又进入坤、出于坤，因此表现衰世之意。侯果说：于为叹词，稽为考察。考察《易》象所列事类，主要讨论吉凶得失，可见它并非针对淳朴古世；用“邪”表示疑问，是不愿说得过于断定。

“夫《易》，彰往而察来，而微显阐幽，开而当名；辨物正言，断辞则备矣。”虞翻说：神用来知未来，智用来储藏过去。使微小者显现，是从复发展为乾，以察知未来；阐明幽隐，是从姤变化为坤，以彰明过去。阳气从初爻生出，所以说开而当名。干宝说：辨物是辨别类别，正言是说明正当义理，断辞是判断吉凶；如此《易经》的内容便完备。

“其称名也小，其取类也大；其旨远，其辞文；其言曲而中，其事肆而隐；因贰以济民行，以明失得之报。”虞翻说：称名小，是乾坤及六子合称八卦，属于小成；取类广大，是乾为阳、为天、为父，触类旁通而扩展。旨意深远以乾为象，文采以坤为象。言语曲折而切中事理；肆是直陈，坤为事，未显现所以幽隐。贰指乾与坤；坤为民，乾为行，行动得当便以吉相报，行动失当便以凶相报。

《易》兴起，大概是在中古时代吧？作《易》的人，大概经历过忧患吧？虞翻说：兴起《易》的是伏羲。文王作经文，系辞于乾五；乾为古，五爻处乾卦中位，所以说兴于中古。伏羲以前为上古。又说：伏羲忧虑百姓不懂兴利除害、不行礼义，茹毛饮血、衣食不足，于是取法天道创立八卦并推演六十四卦，以德教化百姓，与百姓共同忧患吉凶。

“是故履，德之基也；谦，德之柄也。”虞翻说：乾为德，履与谦相通，坤柔履行乾刚，所以履是德的根基；坤为根基。侯果说：履就是礼，践礼不倦，是德的根基。以下九卦是复归正道的重要门径，所以特别提出。虞翻又说：坤为柄，柄就是根本；凡言德皆以阳爻为象。干宝说：柄是用来持物的，谦是用来持守礼的。

“《易》之为书也，不可远。”侯果说：平居观察卦象，行动时玩味占筮，所以《易》不可远离。“为道也，屡迁。”虞翻说：迁是迁移，日月周流、上下无常，所以屡迁。“变动不居，周流六虚。”虞翻说：六虚就是六个位次，日月周流、终而复始，所以周流六虚；甲子旬中辰位为空，坎戊为月、离己为日，日月入中宫而处空虚，故称六虚。“上下无常，刚柔相易。”虞翻说：刚柔象征昼夜，在天称上，入地称下，所以上下无常。“不可为典要，唯变所适。”虞翻说：典要是固定法则；上下无常，所以不能制定固定准则，趋向乾为昼，趋向坤为夜。侯果说：六爻刚柔相易，远近随变化而改变，并无固定准则。

“其出入以度，外内使知惧。”虞翻说：从乾出为外，入坤为内；日行一度，所以出入有法度。出阳象征生，入阴象征死，使人知道戒惧。韩康伯说：这是阐明出入的法度，使人知道外内的戒慎。出入犹如出仕与退藏，外内犹如显露与隐退；遁卦远离时势为吉，丰卦幽隐而致凶，渐卦高显为美，明夷卦处昏暗而利于守正，这些都是外内之戒。“又明于忧患与故。”虞翻说：神用来知未来，所以明白忧患；智用来藏往，所以知道事由。“无有师保，如临父母。”虞翻说：临是面对。阴阳施行而生万物，并没有师保替它们生成；万物出生都如父母所生。乾为父、坤为母。干宝说：《易》道以戒惧为本，从始至终保持警惕便能无咎。外指大夫奉事君王，须夜间仍警惕如临危险；内指妇人居室，不可专擅行事。即使没有师保训诫，也应常怀敬戒，如同父母就在面前。

“初率其辞而揆其方。”虞翻说：初是开始、下位；率是端正，指修辞立诚；方指坤。以乾通坤，所以依照开头辞句衡量其方向。侯果说：率是修习，方是道；修习《易》开篇辞句，便可推度终末之道，其中自有常法，并非虚设。“既有典常，苟非其人，道不虚行。”虞翻说：出入有度，所以有典常；苟是真诚，其人指乾所象征的贤人。使道理神妙而明白，关键在于人；不言而信称为德行，所以道不会徒然施行。崔憬说：《易》道深远，若非圣人便不能明其道；它不会无缘无故自行流行，必须有文王这样的人才能弘扬。

译注说明：虞翻大量使用卦变、干支、纳甲等象数术语；此译保留注家推演，不将其说法当作唯一通行解释。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十六·系辞下传注文（下段）", "底本：景印文渊阁《四库全书》本《周易集解》卷十六，参校维基文库四库本卷十六全文。此条承接卷十六注文上段，自“《易》之为书也，原始要终”至卷末“失其守者，其辞屈”；注文与经传正文分列。底本文字中的异体字、通假字及疑似 OCR 字保留待校状态，不能据此视为已完成影印本逐字校勘。", """
《易》之为书也。
干宝曰：重发《易》者，别殊旨也。

“原始要终，以为质也。”
虞翻曰：质，本也。以乾原始，以坤要终，谓原始反终，以知死生之说。
崔憬曰：质，体也。言《易》之书，原穷其事之初，若初九“潜龙勿用”，是原始也；又要会其事之末，若上九“亢龙有悔”，是要终也。《易》原始潜龙之勿用，要终亢龙之有悔，复相明以为体也。诸卦亦然，若大畜而后通之类是也。

“六爻相杂，唯其时物也。”
虞翻曰：阴阳错居称杂。时阳则阳，时阴则阴，故“唯其时物”。乾，阳物；坤，阴物。
干宝曰：一卦六爻，则皆杂有八卦之气；若初九为震爻，九二为坎爻也。或若见辰戌言艮、巳亥言兑也；或若以甲壬名乾、以乙癸名坤也；或若以午位名离、以子位名坎。或若德来为恶物，王相为兴，休废为衰。

“其初难知，其上易知，本末也。”
侯果曰：本末，初上也。初则事微，故“难知”；上则事彰，故“易知”。

“初辞拟之，卒成之终。”
干宝曰：初拟议之，故难知；卒终成之，故易知，本末势然也。
侯果曰：失在初微，犹可拟议而之；福祸在卒成事之终极，非拟议所及，故曰“卒成之终”。假如乾之九三、噬嗑初九，犹可拟议而之善；至上九则凶灾不移，是事之卒成之终极，凶不变也。

“若夫杂物撰德，辨是与非，则非其中爻不备。”
虞翻曰：撰德谓乾，辨别也是谓阳，非谓阴也。中正，乾六爻二、四、上非正，坤六爻初、三、五非正；故杂物因而重之，爻在其中，故非其中则爻辞不备。道有变动，故曰爻也。
崔憬曰：上既具论初上二爻，次又以明其四爻也。言中四爻杂合所主之事，撰集所陈之德，能辨其是非，备在卦中四爻也。

“噫，亦要存亡吉凶，则居可知矣。”
虞翻曰：谓知存知亡，要终者也。居乾吉则存，居坤凶则亡，故曰“居可知矣”。
崔憬曰：噫，叹声也。言中四爻亦能要定卦中存亡吉凶之事，居然可知矣。孔疏扶王弼义，以此中爻为二五之爻，居中无偏，能统一卦之义，事必不然矣。何则？上文云“六爻相杂，唯其时物”，言虽错杂而各独会于时、独主于物，岂可以二五之爻而兼其杂物撰德、是非、存亡、吉凶之事乎？且二五之撰德与是、要存与吉则可矣，若主物与非、要亡与凶，则非其所象，故知其不可也。且上论初上二爻，则此中总言四爻矣；下论二四、三五，则是重述其功位者也。

“知者观其彖辞，则思过半矣。”
韩康伯曰：夫彖举立象之统，论中爻之义，约以存博，简以兼众；杂物撰德而一以贯之者也。形之所宗者道，众之所归者一。其事弥繁，则愈滞乎有；其理弥约，则转近乎道。彖之为义，存乎一也；一之为用，同乎道矣。形而上者可以观道，过乎半之益，不亦宜乎。

“二与四同功。”
韩康伯曰：同阴功也。
崔憬曰：此重释中四爻功位所宜也。二主士大夫位，佐于一国；四主三孤、三公、牧伯之位，佐于天子，皆同有助理之功也。

“而异位。”
韩康伯曰：有外内也。
崔憬曰：二，士大夫位卑；四，孤公牧位尊，故有异也。

“其善不同，二多誉，四多惧，近也。”
韩康伯曰：二处中和，故多誉也；四近于君，故多惧也。

“柔之为道，不利远者。”
崔憬曰：此言二四皆阴位。阴之为道，近比承阳，故不利远矣。

“其要无咎，其用柔中也。”
崔憬曰：言二是阴远阳，虽则不利，其要或有无咎者，以二柔居中，异于四也。

“三与五同功而异位。”
韩康伯曰：有贵贱也。
崔憬曰：三，诸侯之位；五，天子之位。同有理人之功，而君臣之位异者也。

“三多凶，五多功，贵贱之等也。”
崔憬曰：三处下卦之极，居上卦之下，为一国之君，有威权之重，而上承天子；若无含章之美，则必致凶。五既居中不偏，贵乘天位，以道济物，广被寰中，故多功也。

“其柔危，其刚胜邪？”
侯果曰：三五阳位，阴柔处之则多凶危，刚正居之则胜其任。言“邪”者，不定之辞也；或有柔居而吉者，得其时也；刚居而凶，私其应也。

“《易》之为书也，广大悉备。”
荀爽曰：以阴易阳谓之广，以阳易阴谓之大。《易》与天地准，故悉备也。

“有天道焉，有人道焉，有地道焉。”
崔憬曰：言《易》之为书，明三才，广无不被，大无不包，悉备有万物之象者也。

“兼三才而两之，故六；六者非他也，三才之道也。”
崔憬曰：言重卦六爻亦兼天地人道，两爻为一才，六爻为三才，则是兼三才而两之，故六；六者即三才之道也。

“道有变动，故曰爻。”
陆绩曰：天道有昼夜日月之变，地道有刚柔燥湿之变，人道有行止动静、吉凶善恶之变。圣人设爻以效三者之变动，故谓之爻者也。

“爻有等，故曰物。”
干宝曰：等，群也。爻中之义，群物交集；五星、四气、六亲、九族、福德、刑杀，众形万类皆来发于爻，故总谓之物也。象颐中有物曰噬嗑，是其义也。

“物相杂，故曰文。”
虞翻曰：乾阳物，坤阴物。纯乾纯坤之时未有文章，阳物入坤、阴物入乾，更相杂成六十四卦，乃有文章，故曰文。

“文不当，故吉凶生焉。”
干宝曰：其辞为文也，动作云为必考其事，令与爻义相称也。事不称义，虽有吉凶，则非今日之吉凶。故元亨利贞而穆姜以死，黄裳元吉而南蒯以败，是所谓文不当也。故于经则有君子吉、小人否；于占则王相之气，君子以迁官，小人以遇罪也。

“《易》之兴也，其当殷之末世、周之盛德邪？当文王与纣之事邪？”
虞翻曰：谓文王书《易》六爻之辞也。末世，乾上；盛德，乾三也。文王三分天下而有其二，以服事殷，周德其可谓至德矣，故周之盛德。纣穷否上，知存而不知亡，知得而不知丧，终以焚死，故殷之末世也。而马、荀、郑君从俗以文王为中古，失之远矣。

“是故其辞危。”
虞翻曰：危谓乾三“夕惕若厉”，故辞危也。

“危者使平。”
陆绩曰：文王在纣世，有危亡之患，故于《易》辞多趋危亡，本自免济，建成王业。故《易》爻辞“危者使平”，以象其事；否卦九五“其亡其亡，系于苞桑”之属是也。

“易者使倾。”
陆绩曰：易，平易也。纣安其位，自谓平易而反倾覆，故《易》爻辞“易者使倾”，以象其事；明夷上六“初登于天，后入于地”之属是也。

“其道甚大，百物不废。”
虞翻曰：大谓乾道，乾三爻三十六物，故百物不废，略其奇八，与大衍之五十同义。

“惧以终始，其要无咎，此之谓《易》之道也。”
虞翻曰：乾称《易》道，终日乾乾，故无咎。危者使平，易者使倾，恶盈福谦，故《易》之道者也。

夫乾，天下之至健也，德行恒易以知险。
虞翻曰：险谓坎也。谓乾二、五之坤成坎离，日月丽天；天险不可升，故知险者也。

夫坤，天下之至顺也，德行恒简以知阻。
虞翻曰：阻，险阻也。谓坤二、五之乾；艮为山陵，坎为水，巽高兑下，地险山川丘陵，故以知险也。

“能说诸心。”
虞翻曰：乾五之坤，坎为心，兑为说，故能说诸心，谓说诸心物之有心者也。

“能研诸侯之虑。”
虞翻曰：坎为心虑，乾初之坤为震，震为诸侯，故能研诸侯之虑。

“定天下之吉凶，成天下之亹亹者。”
虞翻曰：谓乾二、五之坤成离，日坎月，则八卦象具，八卦定吉凶，故能定天下之吉凶。亹亹者，谓莫善蓍龟也。
荀爽曰：亹亹者，阴阳之微，可成可败也。顺时者成，逆时者败也。

“是故变化云为，吉事有祥。”
虞翻曰：祥，几祥也，吉之先见者也。阳出变化云为，吉事为祥，谓复初、乾元者也。

“象事知器，占事知来。”
虞翻曰：象事谓坤，坤为器；乾五之坤成象，故象事知器也。占事谓乾以知来，乾五动成离，则玩其占故知来。
侯果曰：《易》之云为，唯变所适；为善则吉事必应。观象则用器可为，求吉则未形可睹者也。

“天地设位，圣人成能。”
虞翻曰：天尊五，地卑二，故设位。乾为圣人，成能谓能说诸心、能研诸侯之虑，故成能也。
崔憬曰：言《易》拟天地，设乾坤二位以明重卦之义，所以成圣人伏羲、文王之能事者也。

“人谋鬼神，百姓与能。”
虞翻曰：乾为人，坤为鬼；乾二、五之坤，坎为谋；乾为百，坤为姓，故人谋、鬼谋，百姓与能。
朱仰之曰：人谋，谋及卿士也；鬼谋，谋及卜筮也；又谋及庶民，故曰“百姓与能”也。

“八卦以象告。”
虞翻曰：在天成象，乾二、五之坤则八卦象成；兑口、震言，故以象告也。

“爻彖以情言。”
崔憬曰：伏羲始画八卦，因而重之以备万物而告于人也。爻谓爻下辞，彖谓卦下辞，皆是圣人之情见乎系辞，而假爻彖以言，故曰“爻彖以情言”。

“刚柔杂居而吉凶可见矣。”
虞翻曰：乾二之坤成坎，坤五之乾成离；故刚柔杂居。艮为居，离有巽兑，坎有震艮，八卦体备，故吉凶可见也。
崔憬曰：言文王以六爻刚柔相推，而物杂居；得理则吉，失理则凶，故吉凶可见也。

“变动以利言。”
虞翻曰：乾变之坤成震，震为言，故变动以利言也。

“吉凶以情迁。”
虞翻曰：乾吉、坤凶，六爻发挥旁通，情也，故以情迁。

“是以爱恶相攻而吉凶生。”
虞翻曰：攻，摩也。乾为爱，坤为恶，谓刚柔相摩；以爱攻恶生吉，以恶攻爱生凶，故吉凶生也。

“远近相取而悔吝生。”
虞翻曰：远阳谓乾，近阴谓坤；阳取阴生悔，阴取阳生吝，悔吝言小疵。
崔憬曰：远谓应与不应，近谓比与不比。或取远应而舍近比，或取近比而舍远应，由此远近相取，所以生悔吝于系辞矣。

“情伪相感而利害生。”
虞翻曰：情，阳；伪，阴也。情感伪生利，伪感情生害；乾为利，坤为害。

“凡《易》之情，近而不相得则凶。”
韩康伯曰：近，况比爻也。《易》之情，刚柔相摩，变动相逼者也。近而不相得，必有乖违之患也。或有相违而无患者，得其应也；相须而偕凶，乖于时也。随事以考之，义可见矣。

“或害之，悔且吝。”
荀爽曰：谓屯六三“往吝”之属也。
虞翻曰：坤为害，以阴居阳、以阳居阴，为悔且吝也。

“将叛者，其辞惭。”
虞翻曰：坎，人之辞也。近而不得，故叛；坎为隐伏，将叛；坎为心，故惭也。
侯果曰：凡心不相得，将怀叛逆者，辞必惭恧。

“中心疑者，其辞枝。”
荀爽曰：或从王事无成之属也。
虞翻曰：离，人之辞也。火性枝分，故枝疑也。
侯果曰：中心疑二，则失得无从，故枝分不一也。

“吉人之辞寡。”
虞翻曰：艮，人之辞也。

“躁人之辞多。”
荀爽曰：谓睽上九之属也。
虞翻曰：震，人之辞也。震为决躁，恐惧虩虩，笑言哑哑，故多辞。
侯果曰：躁人烦急，故辞多。

“诬善之人，其辞游。”
荀爽曰：游逸之属也。
虞翻曰：兑，人之辞也。兑为口舌，诬乾；乾为善人也。
崔憬曰：妄称有善，故自叙其美，而辞必浮游不实。

“失其守者，其辞屈。”
荀爽曰：谓泰上六“城复于隍”之属也。
侯果曰：失守则沮辱而不申，故其辞屈也。爻有此象，故占辞亦从矣。
虞翻曰：巽，人之辞也。巽诘诎，阳在初守；巽初阳入伏阴下，故其辞诎。此六子也。离上、坎下，震起、艮止，兑见、巽伏；上经终坎离，则下经终既济、未济；上系终乾坤，则下系终六子，此《易》之大义者也。
""",
    chatGptTranslation = """ChatGPT 翻译（逐段工作译文）

“变动以利言。”虞翻说：乾变化进入坤而形成震，震象征言语，所以说变化运动以利益来表达。

“吉凶以情迁。”虞翻说：乾象征吉，坤象征凶；六爻发挥变化、彼此旁通，体现事物情势，所以吉凶随着情势而转移。

“是以爱恶相攻而吉凶生。”虞翻说：攻是摩擦。乾象征喜爱，坤象征厌恶，指刚柔相互摩荡；以喜爱攻除厌恶便产生吉，以厌恶攻伐喜爱便产生凶。 “远近相取而悔吝生。”虞翻说：远处阳爻象征乾，近处阴爻象征坤；阳取阴而生悔，阴取阳而生吝。崔憬说：远指有应与无应，近指相比与不相比；舍近比而取远应，或舍远应而取近比，都可能产生悔吝。

“情伪相感而利害生。”虞翻说：情象征阳，伪象征阴；真实感应虚伪而生利，虚伪感应真实而生害；乾为利，坤为害。

“凡《易》之情，近而不相得则凶。”韩康伯说：近指相邻相比的爻。《易》的情势是刚柔相摩、变化运动彼此逼近；相近却不能相得，必然有乖违之患。有时相违却无患，是因为得到正应；有时相互依赖却一同遭凶，是因为违背时势，应结合具体事情考察。“或害之，悔且吝。”荀爽举屯卦六三“往吝”为例。虞翻说：坤象征损害，阴居阳位、阳居阴位，便形成悔与吝。

“将叛者，其辞惭。”虞翻说：坎象征人的言辞。彼此接近却不能相得，所以背叛；坎又象征隐伏和内心，因此将要背叛的人言辞惭愧。侯果说：内心不相契合、将怀叛逆之意的人，说话必然惭愧羞赧。

“中心疑者，其辞枝。”荀爽说：例如“或从王事，无成”一类。虞翻说：离象征人的言辞，火性分枝，所以言辞枝蔓分散。侯果说：内心疑惑于两端，得失无所依据，言辞便分岔而不一致。“吉人之辞寡。”虞翻说：艮象征人的言辞。“躁人之辞多。”荀爽举睽卦上九为例。虞翻说：震象征人的言辞，震代表决躁，也有恐惧战栗、笑语喧哗之象，所以言辞多。侯果说：急躁的人烦乱急迫，因此话多。

“诬善之人，其辞游。”荀爽说：这是言辞游移放逸一类。虞翻说：兑象征人的言辞，兑为口舌，用言辞诬蔑乾所象征的善人。崔憬说：虚妄声称自己有善行，自我陈述其美，言辞必然浮泛不实。

“失其守者，其辞屈。”荀爽举泰卦上六“城复于隍”为例。侯果说：失去操守便会沮丧受辱，不能伸展，所以言辞屈曲；爻有这样的象，占辞也随之如此。虞翻说：巽象征人的言辞。巽有屈曲之象；阳爻原本守在初位，巽初阳进入阴下而潜伏，所以言辞屈曲。这是八卦六子之象：离在上、坎在下，震象征兴起、艮象征停止，兑象征显现、巽象征潜伏。上经以坎、离结束，下经以既济、未济结束；上系以乾、坤结束，下系以六子结束，这是《易》的重要义理。

译注说明：本条主要解释《系辞下传》末段“变动以利言”至“失其守者，其辞屈”。虞翻以八卦取象解释人物言辞，属于其象数体系，不应误读为一般词典式训诂。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十七·说卦传注文（上段）", "底本：景印文渊阁《四库全书》本《周易集解》卷十七，参校维基文库四库本卷十七。范围自《说卦》开篇“昔者圣人之作易也”至八卦取象中“坎为水”一节，录孔颖达、荀爽、干宝、虞翻、崔憬、《九家易》、宋衷、陆绩等注文。此为注释分批录入，不能与卷十七经传正文条目混同；疑字保留底本字形，尚待影印页复核。", """
《说卦》开篇及八卦生成、方位、功用注文

“昔者圣人之作《易》也。”
孔颖达曰：据今而称上代谓之“昔者”。聪明睿智谓之圣人，即伏羲也。案下系云“古者庖牺氏之王天下，始作八卦”，今言作《易》，明是伏羲，非谓文王也。

“幽赞于神明而生蓍。”
荀爽曰：幽，隐也；赞，见也。神者在天，明者在地，神以夜光，明以昼照。蓍者，策也。谓阳爻之策三十有六，阴爻之策二十有四；二篇之策万有一千五百二十，上配列宿，下副物数。生蓍者，谓蓍从爻中生也。
干宝曰：幽昧，人所未见也；赞，求也。言伏羲用明于昧冥之中，以求万物之性，尔乃得自然之神物，能通天地之精而管御百灵者，始为天下生用蓍之法者也。

“参天两地而倚数。”
虞翻曰：倚，立也；参，三也。谓分天象为三才，以地两之，立六画之数，故倚数也。
崔憬曰：参，三也。谓于天数五、地数五中，以八卦配天地之数：起天三配艮而立三数，天五配坎而立五数，天七配震而立七数，天九配乾而立九数，此从三顺配阳四卦也。地从二起，以地两配兑而立二数，以地十配离而立十数，以地八配巽而立八数，以地六配坤而立六数，此从两逆配阴四卦也。其天一、地四之数无卦可配，故虚而不用。此圣人取八卦配天地之数，总五十而为大衍。案：此说不尽，已释在大衍章中详之明矣。

“观变于阴阳而立卦。”
虞翻曰：谓立天之道曰阴与阳，乾坤刚柔立本者。卦谓六爻，阳变成震、坎、艮，阴变成巽、离、兑，故六卦六爻三变三六十八，则有十八变而成卦；八卦而小成是也。

“发挥于刚柔而生爻。”
虞翻曰：谓立地之道曰柔与刚。发动挥变，变刚生柔爻，变柔生刚爻，以三为六也。因而重之，爻在其中，故生爻。

“和顺于道德而理于义。”
虞翻曰：谓立人之道曰仁与义。和顺谓坤，道德谓乾；以乾通坤，谓之理义也。

“穷理尽性以至于命。”
虞翻曰：以乾推坤谓之穷理，以坤变乾谓之尽性；性尽理穷，故至于命，巽为命也。

“昔者圣人之作《易》也。”
虞翻曰：重言“昔者”，明谓庖牺也。

“将以顺性命之理。”
虞翻曰：谓乾道变化，各正性命；以阳顺性，以阴顺命。

“是以立天之道曰阴与阳，立地之道曰柔与刚，立人之道曰仁与义。”
崔憬曰：此明一卦立爻有三才二体之义，故先明天道既立阴阳，地道又立刚柔，人道亦立仁义以明之也。何则？在天虽刚亦有柔德，在地虽柔亦有刚德，故《书》曰“沈潜刚克，高明柔克”。人禀天地，岂可不兼仁义乎？所以《易》道兼之矣。

“兼三才而两之，故《易》六画而成卦。”
虞翻曰：谓参天两地，乾坤各三爻而成六画之数也。

“分阴分阳，迭用柔刚。”
虞翻曰：迭，递也。分阴为柔，以象夜；分阳为刚，以象昼。刚柔者，昼夜之象，昼夜更用，故迭用刚柔矣。

“故《易》六位而成章。”
章谓文理。乾三画成天文，坤三画成地理。

“天地定位。”
注曰：谓乾坤五贵三贱，故定位也。

“山泽通气。”
注曰：谓艮兑同气相求，故通气。

“雷风相薄。”
注曰：谓震巽同声相应，故相薄。

“水火不相射。”
注曰：谓坎离射厌也。水火相通，坎戊、离己，月三十日一会于壬，故不相射也。

“八卦相错。”
注曰：错，摩也，则刚柔相摩，八卦相荡也。

“数往者顺，知来者逆，是故《易》逆数也。”
注曰：数往者，谓坤消从午至亥，上下故顺也；知来者，谓乾息从子至巳，上下故逆也。《易》谓乾，故逆数。以上为虞翻义。

“雷以动之。”
荀爽曰：谓建卯之月，震卦用事，天地和合，万物萌动也。

“风以散之。”
注曰：谓建巳之月，万物上达，布散田野。

“雨以润之。”
注曰：谓建子之月，含育萌芽也。

“日以烜之。”
注曰：谓建午之月，太阳欲长者也。

“艮以止之。”
注曰：谓建丑之月，消息毕止也。

“兑以说之。”
注曰：谓建酉之月，万物成熟也。

“乾以君之。”
注曰：谓建亥之月，乾坤合居，君臣位得也。以上为荀爽义。

“坤以藏之。”
《九家易》曰：谓建申之月，坤在乾下，包藏万物也。乾坤交索，既生六子，各任其才往生物也。又雷与风雨变化不常，而日月相推，迭有来往，是以四卦以义言之；天地山泽恒在者也，故直说名矣。
孔颖达曰：此又重明八物、八卦之功用也。上四举象，下四举卦者，王肃以为互相备也，则明雷风与震巽同用，乾坤与天地同功也。

“帝出乎震，齐乎巽，相见乎离，致役乎坤，说言乎兑，战乎乾，劳乎坎，成言乎艮。”
崔憬曰：帝者，天之王气也。至春分则震王而万物出生；立夏则巽王而万物絜齐；夏至则离王而万物皆相见；立秋则坤王而万物致养；秋分则兑王而万物所说；立冬则乾王而阴阳相薄；冬至则坎王而万物之所归；立春则艮王而万物之所成终成始也。以其周王天下，故谓之帝。此崔氏新义。

“万物出乎震，震东方也。”
虞翻曰：出生也。震初不见东，故不称东方卦也。

“齐乎巽，巽东南也；齐也者，言万物之絜齐也。”
虞翻曰：巽阳隐初，又不见东南，亦不称东南卦，与震同义；巽阳藏室，故絜齐。

“离也者，明也，万物皆相见，南方之卦也。”
虞翻曰：离为日、为火，故明。日出照物，以日相见；离象三爻皆正，日中正南方之卦也。

“圣人南面而听天下，向明而治，盖取诸此也。”
虞翻曰：离南方，故南面；乾为治，乾五之坤，坎为耳，离为明，故以听天下，向明而治也。

“坤也者，地也，万物皆致养焉，故曰致役乎坤。”
注曰：坤阴无阳，故道广布，不主一方；含弘光大，养成万物。

“兑，正秋也，万物之所说也，故曰说言乎兑。”
虞翻曰：兑三失位不正，故言正秋。兑象不见西，故不言西方之卦，与坤同义。兑为雨泽，故说万物；震为言，震二动成兑，言从口出，故说言也。

“战乎乾，乾西北之卦也，言阴阳相薄也。”
虞翻曰：乾刚正，五月十五日晨象西北，故西北之卦。薄，入也。坤十月卦，乾消剥入坤，故阴阳相薄也。

“坎者，水也，正北方之卦也，劳卦也，万物之所归也，故曰劳乎坎。”
虞翻曰：归，藏也。坎二失位不正，故言正北方之卦，与兑正秋同义。坎月夜中，故正北方。
崔憬曰：以坎是正北方之卦，立冬已后万物归藏于坎；又阳气伏于子，潜藏地中，未能浸长，劳局众阴之中也。
""",
        chatGptTranslation = """ChatGPT 翻译（逐段工作译文）

本条从《说卦传》开篇讲到坎卦方位与归藏之义，以下依原文次序翻译注文。

【“昔者圣人之作《易》也”】
孔颖达说：“昔者”是从当下追述上古；“圣人”指聪明睿智之人，这里就是伏羲。下系辞说古代庖牺氏统治天下时开始创作八卦，因此这里所说作《易》的是伏羲，不是周文王。

【“幽赞于神明而生蓍”】
荀爽解释：“幽”是幽隐，“赞”是显明。神属于天，明属于地；神在夜间发光，明在白昼照耀。“蓍”就是蓍策。他以阴阳爻策数解释蓍法：阳爻策数三十六，阴爻策数二十四；上下两篇合计一万一千五百二十策，上与星宿相配，下与万物之数相应。所谓“生蓍”，在他的解释中是说蓍策之数从爻数中产生。
干宝则说：“幽”是昏暗、未被人看见，“赞”是寻求。伏羲在幽冥未明之中探求万物本性，进而发现自然生成的神异之物；这种东西能够沟通天地精气、统摄百灵，于是圣人创立了使用蓍草占筮的方法。

【“参天两地而倚数”】
虞翻说：“倚”是建立，“参”是三。把天象分为三才，再以地数配合，建立六画之数，所以叫作“倚数”。
崔憬提出另一套配数说：天数五、地数五，以八卦配天地之数。天数从三开始，三配艮、五配坎、七配震、九配乾，这是阳卦顺次配天数；地数从二开始，二配兑、十配离、八配巽、六配坤，这是阴卦逆次配地数。天一、地四没有卦可配，因此空置不用。这样合计为五十，用以说明大衍之数。他自己也承认这一说法尚未说尽，详细解释应见大衍章。

【“观变于阴阳而立卦”】
虞翻说，这是说天道由阴阳建立。卦指六爻：阳爻变化形成震、坎、艮，阴爻变化形成巽、离、兑。三爻卦经过三次变化而形成六爻卦，所谓“八卦而小成”就是这个意思。

【“发挥于刚柔而生爻”】
虞翻说，这是说地道由柔刚建立。刚柔发动变化，阳刚变化而生柔爻，阴柔变化而生刚爻；由三画扩展为六画，重卦之中便有爻，所以说“生爻”。

【“和顺于道德而理于义”】
虞翻说，这是说人道由仁义建立。坤象征和顺，乾象征道德；以乾之道贯通坤，便称为义理。

【“穷理尽性以至于命”】
虞翻以乾坤变化解释：用乾推演坤，叫作穷尽事理；坤变化为乾，叫作尽其本性。性理都推究到尽头，便达到命；巽卦在他的象数体系中象征命。

【第二次出现“昔者圣人之作《易》也”】
虞翻说，重复“昔者”二字，是为了明确指出所说圣人就是庖牺。

【“将以顺性命之理”】
虞翻说，这是指乾道变化，使万物各自端正其性命；以阳顺应性，以阴顺应命。

【“立天之道曰阴与阳……立人之道曰仁与义”】
崔憬说，这段说明一卦六爻包含三才、阴阳两体的道理。天道有阴阳，地道有刚柔，人道有仁义。天虽刚健也有柔和的一面，地虽柔顺也有刚强的一面，所以《尚书》说“沉潜刚克，高明柔克”。人禀受天地之性，也应兼具仁与义。

【“兼三才而两之，故《易》六画而成卦”】
虞翻说，所谓兼三才，是天取三爻、地取三爻，乾坤各有三爻，合起来成为六画。

【“分阴分阳，迭用柔刚”】
虞翻说，“迭”就是交替。分阴为柔，象征夜；分阳为刚，象征昼。刚柔如同昼夜，交替运用，所以说迭用柔刚。

【“故《易》六位而成章”】
这里的“章”指纹理和条理。乾的三画构成天文，坤的三画构成地理。

【“天地定位，山泽通气，雷风相薄，水火不相射，八卦相错”】
注文以乾坤、艮兑、震巽、坎离两两相对说明八卦关系。“天地定位”被解释为乾坤尊卑各居其位；“山泽通气”是艮与兑同气相求；“雷风相薄”是震与巽相应；“水火不相射”则以坎离相通、月日会合解释，并非说水火完全隔绝。“相错”中的“错”被解释为相互摩荡，由此刚柔相摩、八卦相荡。

【“数往者顺，知来者逆，是故《易》逆数也”】
虞翻说，推数已经发生的变化叫“数往”，对应坤气消退，从午到亥，依次而行，所以称顺；推知将要发生的变化叫“知来”，对应乾气增长，从子到巳，逆向推演，所以称逆。《易》在这里以乾为主，故说逆数。

【“雷以动之，风以散之，雨以润之，日以烜之，艮以止之，兑以说之，乾以君之，坤以藏之”】
荀爽以十二消息卦和月份解释八卦功用：卯月震当令，雷动而万物萌发；巳月巽当令，万物生长布散；子月坎水含育萌芽；午月太阳照临；丑月艮令万物停止生长；酉月兑令万物成熟喜悦；亥月乾坤相合，君臣之位得以确立。九家易补充说，申月坤居乾下，包藏万物；乾坤交合而生六子，各自发挥作用，雷风雨日的变化往复不定，天地山泽则相对恒常。孔颖达认为，这一段再次说明八种自然物象及八卦的功用；上四句说自然现象，下四句说卦名，二者互相补足。

【“帝出乎震……成言乎艮”】
崔憬把“帝”解释为天的主宰之气，并按节气说明八卦次序：春分震旺，万物出生；立夏巽旺，万物整齐；夏至离旺，万物显现；立秋坤旺，万物得到养育；秋分兑旺，万物喜悦成熟；立冬乾旺，阴阳交迫；冬至坎旺，万物归藏；立春艮旺，万物完成旧周期并开启新周期。这是崔憬自己的新解释。

【“万物出乎震，震东方也”】
虞翻说，“出”就是出生。震的初爻在他的卦变解释中并不显现东方之象，所以经文虽说震属东方，他认为此处没有直接称震为东方卦。

【“齐乎巽，巽东南也”】
虞翻说，巽的阳爻潜藏在初位，也不显现东南之象，因此与震相同，没有直接称作东南方卦。阳爻藏在阴体之中，所以万物得以整齐。

【“离也者，明也，万物皆相见，南方之卦也”】
虞翻说，离象征太阳和火，所以有光明之义。太阳升起照耀万物，使万物显现；离卦三爻皆正，在他的解释中象征太阳居中、位于正南。

【“圣人南面而听天下，向明而治”】
虞翻说，离在南方，所以圣人南面；乾象征治理，乾五爻变化入坤，坎象征耳，离象征光明，因此圣人能够听察天下、面向光明治理。

【坤、兑、乾、坎诸段】
关于坤，注文说坤纯阴而不专主一方，广大包容，养育万物。关于兑，虞翻以秋季、雨泽和口舌之象解释“说言”：兑为雨泽，使万物喜悦；震象征言语，震爻变化成兑，故言语从口而出。关于乾，虞翻把“战”解释为阴阳相迫，坤气消退、乾气进入坤体，因而称阴阳相薄。关于坎，虞翻说“归”就是收藏；崔憬则说，立冬以后万物归藏于北方坎位，冬至时阳气潜伏地下，尚未增长，因此坎处在众阴之中而有劳苦之义。

译注说明：以上译文逐段对应本条所录注文。虞翻的爻位、卦变、月令配卦和取象说属于其特定象数解释系统；崔憬的节气配卦说是其个人解释，均不应直接等同于《说卦传》经文本身。"""
    ),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十六·系辞下传注文（前段）", "底本：维基文库《周易集解（李鼎祚）/卷十六》，页面行114—342。录《系辞下传》注文，含虞翻、侯果、崔觐、干宝、荀爽、孔颖达等注。此为数字转录底稿；原页面个别字形、疑似讹字及标点未经影印本复核，暂按来源保留，不擅自改字。", """《周易系辞下传》注文（卷十六前段，依维基文库数字转录次序）

《易》曰：不远复，无祗悔，元吉。
侯果曰：复初九爻辞。殆，近也。庶，冀也。此明知微之难。则知微者唯圣人耳。颜子亚圣，但冀近于知微而未得也。在微则昧，理章而悟，失在未形，故“有不善”。知则速改，故无大过。

天地絪缊，万物化醇。
虞翻曰：谓泰上也。先说否，否反成泰，故不说泰。天地交，万物通，故“化醇”。
孔颖达曰：以前章利用安身以崇德也。安身之道，在于得一。若已能得一，则可以安身。故此章明得一之事也。絪缊，气附著之义。言天地无心，自然得一。唯二气絪缊，共相和会，感应变化，而有精醇之生，万物自化。若天地有心为一，则不能使万物化醇者也。

男女构精，万物化生。
虞翻曰：谓泰初之上成损，艮为男，兑为女，故“男女构精”。乾为精。损反成益，万物出震，故“万物化生”也。
干宝曰：男女犹阴阳也。故“万物化生”。不言阴阳，而言男女者，以指释损卦六三之辞，主于人事也。

《易》曰：三人行，则损一人。一人行，则得其友。言致一也。
侯果曰：损六三爻辞也。《象》云：一人行，三则疑，是众不如寡，三不及一。此明物情相感，当上法絪缊、化醇、致一之道，则无患累者也。

子曰：君子安其身而后动，
虞翻曰：谓反损成益。君子，益初也。坤为安身。震为后动。
崔觐曰：君子将动有所为，必自揣安危之理，在于己身，然后动也。

易其心而后语，
虞翻曰：乾为易，益初体复心。震为后语。
崔觐曰：君子恕己及物，若于事心难，不可出语，必和易其心而后言。

定其交而后求。
虞翻曰：震，专，为定，为后。交谓刚柔始交。艮为求也。
崔觐曰：先定其交，知其才行，若好施与吝，然后可以事求之。

君子修此三者，故全也。
虞翻曰：谓否上之初，损上益下，其道大光。自上下下，民说无疆，故“全也”。

危以动，则民不与也。
虞翻曰：谓否上九，高而无位，故“危”。坤民否闭，故“弗与也”。

惧以语，则民不应也。
虞翻曰：否上穷灾，故“惧”。来下之初成益，故“民不应”。坤为民，震为应也。

无交而求，则民不与也。
虞翻曰：上来之初，故“交”。坤民否闭，故“不与”。震为交。

莫之与，则伤之者至矣。
虞翻曰：上不之初，否消灭乾，则体剥伤；臣弑君，子弑父，故“伤之者至矣”。

《易》曰：莫益之，或击之，立心勿恒，凶。
侯果曰：益上九爻辞也。此明先安身易心，则群善自应。若危动惧语，则物所不与，故“凶”也。

子曰：乾坤其易之门邪。
荀爽曰：阴阳相易，出于乾坤，故曰“门”。

乾，阳物也。坤，阴物也。
荀爽曰：阳物，天。阴物，地也。

阴阳合德，而刚柔有体。
虞翻曰：合德谓天地杂，保大和，日月战。乾刚以体天，坤柔以体地也。

以体天地之撰，
《九家易》曰：撰，数也。万物形体，皆受天地之数也。谓九，天数；六，地数也。刚柔得以为体矣。

以通神明之德。
《九家易》曰：隐藏谓之神，著见谓之明。阴阳交通，乃谓之德。

其称名也，杂而不越。
《九家易》曰：阴阳，杂也。名谓卦名。阴阳虽错，而卦象各有次序，不相逾越。

于稽其类，其衰世之意邪？
虞翻曰：稽，考也。三称盛德。上称末世，乾终上九：动则入坤，坤弑其君父，故为乱臣。阳出复震，入坤出坤，故“衰世之意邪”。
侯果曰：于，嗟也。稽，考也。易象考其事类，但以吉凶得失为主，则非淳古之时也。故云“衰世之意”耳。言邪，示疑，不欲切指也。

夫易章往而察来，而微显阐幽，开而当名，
虞翻曰：神以知来，知以藏往。微者显之，谓从复成乾，是察来也。阐者幽之，谓从姤之坤，是章往也。阳息出初，故“开而当名”。

辩物，正言，断辞，则备矣。
干宝曰：辩物，类也。正言，言正义也。断辞，断吉凶也。如此，则备于经矣。

其称名也小，
虞翻曰：谓乾坤与六子俱名八卦，而小成，故小复。小而辩于物者矣。

其取类也大。
虞翻曰：谓乾阳也，为天，为父，触类而长之，故“大”也。

其旨远，其辞文，
虞翻曰：远，谓乾。文，谓坤也。

其言曲而中，其事肆而隐，
虞翻曰：曲，诎。肆，直也。阳曲初，震为言，故“其言曲而中”。坤为事，隐未见，故“肆而隐”也。

因贰以济民行，以明失得之报。
虞翻曰：贰，谓乾与坤也。坤为民，乾为行。行得，则乾报以吉。行失，则坤报以凶也。

易之兴也，其于中古乎？
虞翻曰：兴易者，谓庖牺也。文王书经，系庖牺于乾五。乾为古，五在乾中，故兴于中古。系以黄帝、尧、舜为后世圣人。庖牺为中古，则庖牺以前为上古。

作易者，其有忧患乎？
虞翻曰：谓成患百姓未知兴利远害，不行礼义，茹毛饮血，衣食不足。庖牺则天八卦，通为六十四，以德化之，吉凶与民同患，故有忧患。

是故履，德之基也。
虞翻曰：乾为德。履与谦旁通。坤柔履刚，故“德之基”。坤为基。
侯果曰：履，礼。蹈礼不倦，德之基也。自下九卦，是复道之最，故特言矣。

谦，德之柄也。
虞翻曰：坤为柄。柄，本也。凡言德，皆阳爻也。
干宝曰：柄，所以持物。谦，所以持礼者也。

复，德之本也。
虞翻曰：复初乾之元，故“德之本也”。

恒，德之固也。
虞翻曰：立不易方，守德之坚固。

损，德之修也。
荀爽曰：征忿窒欲，所以修德。

益，德之裕也。
荀爽曰：见善则迁，有过则改，德之优裕也。

困，德之辩也。
郑玄曰：辩，别也。遭困之时，君子固穷，小人穷则滥，德于是别也。

井，德之地也。
姚信曰：井差而不穷，德居地也。

巽，德之制也。
虞翻曰：巽风为号令，所以制下，故曰“德之制也”。
孔颖达曰：此上九卦，各以德为用也。

履和而至，
虞翻曰：谦与履通，谦坤柔和，故“履和而至”。礼之用，和为贵者也。

谦尊而光，
荀爽曰：自上下下，其道大光也。

复小而辨于物。
虞翻曰：阳始见，故“小”。乾，阳物。坤，阴物。以乾居坤，故称别物。

恒杂而不厌，
荀爽曰：夫妇虽错居，不厌之道也。

损先难而后易，
虞翻曰：损初之上，失正，故“先难”。终反成益，得位于初故“后易”。易其心而后语。

益长裕而不设，
虞翻曰：谓天施地生，其益无方。凡益之道，与时偕行，故“不设”也。

困穷而通，
虞翻曰：阳穷否上，变之坤二，成坎。坎为通，故“困穷而通”也。

井居其所而迁，
韩康伯曰：改邑不改井，井所居不移，而能迁其施也。

巽称而隐。
崔觐曰：言巽申命行事，是称扬也。阴助德化，是微隐也。自此以下，明九卦德之体者也。

履以和行，
虞翻曰：礼之用，和为贵，谦震为行，故“以和行”也。

谦以制礼，
虞翻曰：阴称礼，谦三以一阳制五阴，万民服，故“以制礼”也。

复以自知，
虞翻曰：有不善未尝不知，故“自知”也。

恒以一德，
虞翻曰：恒德之固，立不易方，从一而终，故“一德”者也。

损以远害，
虞翻曰：坤为害，泰以初止坤上，故“远害”。乾为远。

益以兴利，
荀爽曰：天施地生，其益无方，故“兴利”也。

困以寡怨，
虞翻曰：坤为怨，否弑父与君，乾来下，折坤二，故“寡怨”。坎水性通，故不怨也。

井以辨义，
虞翻曰：坤为义，以乾别坤，故辩义也。

巽以行权。
《九家易》曰：巽象号令，又为近利。人君政教、进退、释利而为权也。《春秋传》曰：权者，反于经然后有善者也。此所以说九卦者，圣人履忧济民之所急行也。故先陈其德，中言其性，后叙其用，以详之也。西伯劳谦，殷纣骄暴，臣子之礼有常，故创易道以辅济君父者也。然其意义广远幽微，孔子指撮解此九卦之德，合三复之道，明西伯之于纣不失上下。

（下接卷十六后段：由“易之为书也不可远”起，续录至卷末“其辞诎”。）""", chatGptTranslation = """**ChatGPT 翻译（逐段工作译文）**

**《易》说：“不远离正道而及时复归，不会留下大的悔恨，大吉。”**

侯果解释：这是复卦初九爻辞。“殆”是接近，“庶”是希望。这一段说明洞察细微征兆非常困难，真正能够知微的只有圣人。颜回仅次于圣人，只能说希望接近知微，却还没有完全达到。征兆尚在细微阶段时，人往往昏昧难辨；道理显明之后才有所觉悟，问题就在于它尚未显现成形。因此经文说“有不善”。一旦知道，就迅速改正，所以不会有大的过失。

**天地之气交融密合，万物因此化育而醇厚。**

虞翻解释：这里指泰卦上爻。前文先讲否卦，否卦反转成为泰卦，所以这里没有直接说泰卦。天地相交，万物通达，因此说万物化醇。孔颖达解释：前文说安顿自身以增进德行，安身的关键在于得一。如果能够得一，便可以安身，所以这一章说明得一的道理。“絪缊”是气息附着、交融的意思。天地没有主观意志，却自然合于“一”；阴阳二气交融会合，彼此感应变化，于是产生精纯的生命，万物自然化生。如果天地有意刻意追求一个“一”，反而不能使万物自然化醇。

**男女交合精气，万物因此化生。**

虞翻解释：这里指泰卦初爻上升到上位而形成损卦。艮象征男，兑象征女，所以说男女交合精气；乾象征精气。损卦反转为益卦，万物从震卦而出，所以说万物化生。干宝解释：男女就是阴阳。这里不说阴阳而说男女，是因为要用人事来解释损卦六三爻辞。

**《易》说：“三个人同行，就会减少一个人；一个人独行，就会得到他的同伴。”这是说要归于专一。**

侯果解释：这是损卦六三爻辞。《象传》说，一个人独行而三个人同行就会产生疑虑。这说明人少反而胜过人多，三人同行不如专一。人与物彼此感应时，应当效法天地之气交融、万物化醇、归于专一的道理，这样才不会产生忧患和牵累。

**孔子说：君子先安顿自身，然后才行动。**

虞翻解释：这里指损卦反转为益卦。君子指益卦初爻，坤象征安身，震象征随后行动。崔觐解释：君子准备行动做事，必须先衡量自身安危，然后再行动。

**先使内心平和，然后才发言。**

虞翻解释：乾象征平易，益卦初爻的卦体包含复卦之心，震象征随后发言。崔觐解释：君子推己及人。如果对某件事内心仍有疑难，就不应急于发言；必须先使心境平和，再开口说话。

**先确定交往关系，然后才有所求。**

虞翻解释：震象征专一、确定，也象征随后行动；交指刚柔开始相交，艮象征求取。崔觐解释：先确定彼此关系，了解对方的才能与品行，例如是否乐于施与、是否吝啬，然后才可以向对方有所请求。

**君子修养这三件事，所以能够保全自身。**

虞翻解释：这里指否卦上爻下降到初位，损上益下，道义因此大为光明。由上而下谦抑，百姓喜悦无边，所以能够保全。

**处境危险却贸然行动，百姓不会支持。**

虞翻解释：这里指否卦上九，居高而没有适当位置，所以称为危险。坤象征百姓，否卦闭塞，因此百姓不会给予支持。

**心怀恐惧而发言，百姓不会响应。**

虞翻解释：否卦上爻处在穷极、灾祸将至的状态，所以说恐惧。它下降到初位形成益卦，因此百姓不会响应；坤象征百姓，震象征响应。

**没有交情却向人求取，百姓不会给予帮助。**

虞翻解释：上爻下降到初位，才形成交往；坤象征百姓，否卦闭塞，所以百姓不会给予帮助。震象征交往。

**没有人帮助，伤害自己的人就会到来。**

虞翻解释：上爻不下降到初位，否卦便消灭乾体，形成剥卦的损伤之象；臣下弑君、儿子弑父，因此伤害就会到来。

**《易》说：“没有人增益他，反而有人攻击他；立志不能持久，凶险。”**

侯果解释：这是益卦上九爻辞。前面说明先安顿自身、使内心平和，众多善意便会自然响应；如果处境危险却行动、心怀恐惧却发言，外物便不会支持，因此凶险。

**孔子说：乾与坤，大概就是《易》的门户吧？**

荀爽解释：阴阳相互变化，都从乾坤而出，所以称为门户。

**乾属于阳类，坤属于阴类。**

荀爽解释：阳类是天，阴类是地。

**阴阳之德相互配合，刚柔各有形体。**

虞翻解释：所谓合德，是天地交杂、保持大和，日月相互交会。乾刚健，体现天；坤柔顺，体现地。

**借此体现天地所创造的事物。**

九家易解释：“撰”是数。万物的形体都承受天地之数，例如天数为九、地数为六；刚柔因此得以形成各自的形体。

**借此贯通神明的德性。**

九家易解释：隐藏不显称为神，显著可见称为明。阴阳相互交通，才称得上德。

**它所使用的名称虽然繁杂，却不会越出一定的秩序。**

九家易解释：阴阳交错称为杂；名称指卦名。阴阳虽然交错，卦象各有次序，不会彼此逾越。

**考察它所归属的类别，大概反映了衰世的意味吧？**

虞翻解释：“稽”是考察。三爻象征盛德，上爻象征末世；乾到上九终结，发动便进入坤，坤象征臣下弑君、儿子弑父，所以成为乱臣之象。阳气从复卦震位出现，又进入坤，因此说反映衰世的意味。侯果解释：“于”是感叹，“稽”是考察。考察《易》象所涉及的事类，会发现它主要以吉凶得失为判断，因此不是淳朴古世的情形。称“邪”是表示疑问，并不想直接断言。

**《易》彰明过去、考察未来，使微小者显现、幽隐者阐明，并使事物开启而获得恰当的名称。**

虞翻解释：神妙用来预知未来，智慧用来收藏过去。使微小者显现，是从复卦发展为乾卦，借此考察未来；阐明幽隐，是从姤卦变化为坤卦，借此彰明过去。阳气从初爻生出，所以说开启并赋予恰当名称。

**辨别事物，端正言辞，用断语判断吉凶，这样内容就完备了。**

干宝解释：辨物就是分类，正言就是端正义理，断辞就是判断吉凶。做到这些，经义便完备了。

**它所使用的名称虽小。**

虞翻解释：乾、坤以及六子都各自称为八卦，属于小成之卦，所以说名称小。

**它所取譬的类别却广大。**

虞翻解释：这里指乾阳。乾为天、为父，可以触类旁通、推演扩展，所以说广大。

**它的旨意深远，文辞有文采。**

虞翻解释：深远象征乾，文采象征坤。

**它的言语曲折却切中事理，所叙之事铺陈开来却含有隐义。**

虞翻解释：“曲”是曲折，“肆”是铺陈直陈。阳爻曲居初位，震象征言语，所以说言语曲折而切中；坤象征事务，隐而未显，所以说所叙之事铺陈而含蓄。

**借助事物的两端，帮助百姓行动，并说明得失所带来的报应。**

虞翻解释：“贰”指乾与坤。坤象征百姓，乾象征行动。行动得当，乾便以吉祥回应；行动失当，坤便以凶险回应。

**《易》兴起，大概是在中古时代吧？**

虞翻解释：兴起《易》的人指伏羲。文王撰写经文，把伏羲联系到乾卦九五；乾象征古，九五处在乾体中间，所以说兴起于中古。黄帝、尧、舜则被称为后世圣人；伏羲属于中古，因此伏羲以前属于上古。

**创作《易》的人，想必经历过忧患吧？**

虞翻解释：当时百姓还不知道如何兴利除害，不懂礼义，生活方式粗朴，衣食不足。伏羲取法天上的八卦，把它们推演为六十四卦，以德教化百姓，使吉凶与百姓共同承担，所以说创作《易》的人有忧患之心。

**所以，履卦是德行的根基。**

虞翻解释：乾象征德。履卦与谦卦相通，坤柔顺地履行刚健之道，所以说德行有根基；坤象征根基。侯果解释：履就是礼，践行礼而不倦，是德行的根基。以下九卦都是恢复正道的重要途径，所以特别说明。

**谦卦是德行的把柄。**

虞翻解释：坤象征把柄，也就是根本。凡是说德，都是指阳爻。干宝解释：把柄是用来持物的；谦逊则是用来持守礼节的。

**复卦是德行的根本。**

虞翻解释：复卦初爻体现乾元，所以说它是德行的根本。

**恒卦使德行稳固。**

虞翻解释：立身不改变正道，就是守德坚定稳固。

**损卦使德行得到修治。**

荀爽解释：克制愤怒、堵塞欲望，就是修养德行。

**益卦使德行充实宽裕。**

荀爽解释：看见善行就迁善，有了过错就改正，德行因此宽厚充裕。

**困卦是德行受到辨验的地方。**

郑玄解释：“辨”是分别。遭遇困境时，君子能够固守穷困，小人却会穷困而放纵，德行由此区分。

**井卦是德行安定的处所。**

姚信解释：井的形制虽有差异，却不会枯竭；德行也应当安定于其所。

**巽卦是德行施行时的节制方法。**

虞翻解释：巽象征风，也象征号令；号令用来约束下属，所以说它是德行的制约方法。孔颖达解释：以上九卦，各自以德行为功用。

**履卦和顺而达到恰当境地。**

虞翻解释：谦卦与履卦相通，谦卦坤柔和顺，所以说履能够和顺而至。礼的运用以和为贵。

**谦卦尊贵而光明。**

荀爽解释：由上而下谦抑，其道便大为光明。

**复卦虽然细微，却能够辨明事物。**

虞翻解释：阳气刚刚显现，所以说小。乾是阳类，坤是阴类；乾居于坤中，所以能够区分不同事物。

**恒卦处于纷杂之中，却不会令人厌倦。**

荀爽解释：夫妇虽然交错相处，却能够长久而不厌倦。

**损卦先经历困难，而后变得容易。**

虞翻解释：损卦初爻上升到上位，失去正位，所以先困难；最终反转为益卦，初爻得到正位，所以后来容易。

**益卦不断增长充裕，并非人为勉强安排。**

虞翻解释：这是说天施行、地生养，其增益没有固定限度。凡是增益之道，都与时势一同运行，所以说不刻意造作。

**困卦身处穷境，却仍能通达。**

虞翻解释：阳气穷尽于否卦上爻，变化到坤二而形成坎；坎象征通达，所以说困穷而通。

**井卦安居其所，却能够施惠流通。**

韩康伯解释：城邑可以改变，井却不改变所在；井虽然位置固定，却能把水施予众人。

**巽卦称扬而含蓄。**

崔觐解释：巽象征申明命令、推行事务，所以称扬；阴柔辅助德化，作用隐微，所以含蓄不显。从这里以下，是说明九卦德性的具体形态。

**用履卦使行为和顺。**

虞翻解释：礼的运用以和为贵；谦卦的震象征行动，所以说以和顺来行动。

**用谦卦制定礼节。**

虞翻解释：阴象征礼。谦卦三爻以一个阳爻统制五个阴爻，万民因此服从，所以说以谦制定礼。

**用复卦反省认识自己。**

虞翻解释：有不善之处而自己能够察觉，所以说复卦使人自知。

**用恒卦保持德行专一。**

虞翻解释：恒的德性稳固，立身不改变正道，从一而终，所以称为一德。

**用损卦远离祸害。**

虞翻解释：坤象征祸害；泰卦初爻阻止坤上行，所以能够远离祸害。乾象征远。

**用益卦兴办有益之事。**

荀爽解释：天施行、地生养，增益没有固定限度，所以说兴利。

**用困卦减少怨尤。**

虞翻解释：坤象征怨恨。否卦有弑父、弑君之象；乾阳下降，折损坤二，所以能够减少怨尤。坎水性质通达，因此不生怨恨。

**用井卦辨明义理。**

虞翻解释：坤象征义；以乾阳区别坤阴，所以说辨明义理。

**用巽卦灵活施行权宜。**

九家易解释：巽象征号令，也象征接近利益。君主施行政教、决定进退，有时需要舍弃眼前利益而采取权宜措施。《春秋传》说，权宜之道有时要暂时偏离常经，才能达到善的结果。这里说明九卦，是因为圣人践行忧患之道、救助百姓时，最需要这些方法。先陈述德性，再说明德性本身，最后说明其功用，以便详尽阐明。西伯周文王谦劳，殷纣王骄暴；臣子之礼有其常道，所以文王创立易道，以辅助君父、挽救天下。其意义广大深远而幽微，孔子摘取并解释九卦的德性，合于反复推求的道理，说明文王面对纣王时没有失去上下之礼。

译释说明：本条是卷十六前段注文，保留虞翻、侯果、崔觐、干宝、荀爽、韩康伯、郑玄、姚信、九家易、孔颖达等各家解释。部分卦变术语依注家体系直译；“其衰世之意邪”等句保留疑问语气，不将注家推论直接改写成经文定论。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十六·系辞下传注文（后段）", "底本：维基文库《周易集解（李鼎祚）/卷十六》，页面行344—629，接前段至卷末。含虞翻、韩康伯、崔觐、干宝、荀爽、侯果、陆绩、朱仰之等注。页面中夹有编者辨析，已单独标明，不冒充唐代原注；数字文本仍待影印本校勘。", """《周易系辞下传》注文（卷十六后段，接前段）

易之为书也不可远，
侯果曰：居则观象，动则玩占，故“不可远”也。

为道也屡迁，
虞翻曰：迁，徒也。日月周流，上下无常，故“屡迁”也。

变动不居，周流六虚，
虞翻曰：变，易。动，行。六虚，六位也。日月周流，终则复始，故“周流六虚”。谓甲子之旬，辰巳虚坎，戊为月，离巳为日，入在中宫，其处空虚，故称“六虚”。五甲如次者也。

上下无常，刚柔相易，
虞翻曰：刚柔者，昼夜之象也。在天称上，入地为下，故“上下无常”也。

不可为典要，唯变所适。
虞翻曰：典，常。要，道也。上下无常，故“不可为典要”；适乾为昼，适坤为夜。
侯果曰：谓六爻刚柔相易，远近恒，唯变所适，非有典要。

其出入以度，外内使知惧。
虞翻曰：出乾为外，入坤为内，日行一度，故“出入以度”。出阳知生，入阳惧死，使知惧也。
韩康伯曰：明出入之度，使物知外内之戒也。出入犹行藏，外内犹隐显。遁以远时为吉，丰以幽隐致凶，渐以高显为美，明夷以处昧利贞，此外内之戒也。

又明于忧患与故。
虞翻曰：神以知来，故明忧患。知以藏往，故知事故。作易者其有忧患乎？

无有师保，如临父母。
虞翻曰：临，见也。言阴阳施行，以生万物，无有师保生成之者。万物出生，皆如父母。孔子曰：父母之道天地，乾为父，坤为母。
干宝曰：言易道以戒惧为本，所谓惧以终始，归无咎也。外为丈夫之从王事，则夕惕若厉。内谓妇人之居室，则无攸遂也。虽无师保切磋之训，其心敬戒，常如父母之临己者也。

初帅其辞，而揆其方，
虞翻曰：初，始下也。帅，正也。谓修辞立诚。方，谓坤也。以乾通坤，故“初帅其辞，而揆其方”。
侯果曰：率，修。方，道也。言修易初首之辞，而度其终末之道，尽有典常，非虚设也。

既有典常，苟非其人，道不虚行。
虞翻曰：其出入以度，故“有典常”。苟，诚也。其人谓乾为贤人。神而明之，存乎其人，不言而信，谓之德行，故“不虚行”也。
崔觐曰：言易道深远，若非其圣人，则不能明其道。故知易道不虚而自行，必文王然后能弘也。

易之为书也，
干宝曰：重发易者，别殊旨也。

原始要终，以为质也。
虞翻曰：质，本也。以乾原始，以坤要终，谓原始及终，以知死生之说。
崔觐曰：质，体也。言易之书，原穷其事之初，若初九潜龙勿用，是原始也。又要会其事之末，若上九亢龙有悔，是要终也。易原始潜龙之勿用，要终亢龙之有悔，复相明以为体也。诸卦亦然，若大畜而后通之类是也。

六爻相杂，唯其时物也。
虞翻曰：阴阳错居称杂。时阳则阳，时阴则阴，故“唯其时物”。

乾，阳物。坤，阴物。
干宝曰：一卦六爻，则皆杂有八卦之气；若初九为震爻，九二为坎爻也。或若见戌言艮，己亥言兑也。或若以甲壬名乾，以乙癸名坤也。或若以午位名离，以子位名坎。或若德来为好物，刑来为恶物。王相为兴，休废为衰。

其初难知，其上易知，本末也。
侯果曰：本末，初上也。初则事微，故“难知”。上则事彰，故“易知”。

初辞拟之，卒成之终。
干宝曰：初拟议之，故“难知”。卒终成之，故“易知”。本末势然也。
侯果曰：失在初微，犹可拟议而之福。过在卒成，事之终极，非拟议所及。故曰“卒成之终”。假如乾之九三、噬嗑初九，犹可拟议而之善。至上九，则凶灾不移，是事之卒成之终，极凶不变也。

若夫杂物撰德，辨是与非，则非其中爻不备。
虞翻曰：撰德，谓乾辨别也。是，谓阳。非，谓阴也。中，正。乾六爻，二、四、上，非正。坤六爻，初、三、五，非正。故“杂物”。因而重之，爻在其中，故“非其中”。则爻辞不备。道有变动，故曰爻也。
崔觐曰：此戏具论初上二爻，次又以明其四爻也。言中四爻杂合所主之事，撰集所陈之德，能辨其是非，备在卦中四爻也。

噫，亦要存亡吉凶，则居可知矣。
虞翻曰：谓知存、知亡、要终者也。居乾吉，则存，居坤凶，则亡。故曰“居可知矣”。
崔觐曰：噫，叹声也。言中四爻，亦能要定卦中存亡吉凶之事，居然可知矣。
按：本页另载一段辨析孔疏以二、五为中爻之说，认为上文“六爻相杂”及下文二四、三五分论功位，显示此处“中爻”应兼论中间四爻。该段属于编者按语，非李鼎祚所辑古注，故另标性质，不混入注文。

智者观其《彖》辞，则思过半矣。
韩康伯曰：夫《彖》举立象之统，论中爻之义，约以存博，简以兼众，杂物撰德，而一以贯之者也。形之所宗者道，众之所归者一。其事弥繁，则愈滞乎形。其理弥约，则转近乎道。《彖》之为义，存乎一也。一之为用，同乎道矣。形而上者，可以观道。过乎半之益，不亦宜乎！

二与四同功，
韩康伯曰：同阴功也。
崔觐曰：此重释中四爻功位所宜也。二主士、大夫位，佐于一国。四主三孤、三公、牧伯之位，佐于天子。皆同有助理之功也。

而异位，
韩康伯曰：有外内也。
崔觐曰：二，士、大夫位，卑；四，孤、公、牧伯位，尊，故有异也。

其善不同，二多誉，四多惧，近也。
韩康伯曰：二处中和，故“多誉”也。四近于君，故“多惧”也。

柔之为道，不利远者。
崔觐曰：此言二四皆阴位。阴之为道，近比承阳，故“不利远”矣。

其要无咎，其用柔中也。
崔觐曰：言二是阴远阳，虽则不利，其要或有无咎者。以二柔居中，异于四也。

三与五同功而异位，
韩康伯曰：有贵贱也。
崔觐曰：三，诸侯之位。五，天子之位。同有理人之功，而君臣之位异者也。

三多凶，五多功，贵贱之等也。
崔觐曰：三处下卦之极，居上卦之下，为一国之君，有威权之重，而上承天子，若无含章之美，则必致凶。五既居中不偏，贵乘天位，以道济物，广被寰中，故“多功”也。

其柔危，其刚胜邪？
侯果曰：三五阳位，阴柔处之，则多凶危。刚正居之，则胜其任。言邪者，不定之辞也。或有柔居而吉者，得其时也。刚居而凶者，失其应也。

易之为书也，广大悉备。
荀爽曰：以阴易阳，谓之广。以阳易阴，谓之大。易与天地准，固悉备也。

有天道焉，有人道焉，有地道焉。
崔觐曰：言易之为书明三才，广无不被，大无不包。悉备有万物之象者也。

兼三才而两之，故六。六者，非它也，三才之道也。
崔觐曰：言重卦六爻，亦兼天地人道，两爻为一才，六爻为三才，则是兼三才而两之，故六。六者，即三才之道也。

道有变动，故曰爻。
陆绩曰：天道有昼夜、日月之变。地道有刚柔、燥湿之变，人道有行止、动静、吉凶、善恶之变。圣人设爻以效三者之变动，故谓之“爻”者也。

爻有等，故曰物。
干宝曰：等，群也。爻中之义，群物交集，五星四气，六亲九族，福德刑杀，众形万类，皆来发于爻，故总谓之物也。象颐中有物曰噬嗑，是其义也。

物相杂，故曰文。
虞翻曰：乾阳物，坤阴物。纯乾纯坤之时，未有文章。阳物入坤，阴物入乾，更相杂成六十四爻，乃有文章，故曰“文”。

文不当，故吉凶生焉。
干宝曰：其辞为文也。动作云为，必考其事，令与爻以相称也。事不称义，虽有吉凶，则非今日之吉凶也。故元亨利贞，而穆姜以死，黄裳元吉，南蒯以败，是所谓文不当也。故于经，则有君子吉，小人否。于占，则王相之气，君子以迁官，小人以遇罪也。

易之兴也，其当殷之末世，周之盛德邪？当文王与纣之事邪？
虞翻曰：谓文王书《易》六爻之辟也。末世，乾上。盛德，乾三也。文王三分天下而有其二，以服事殷，周德其可谓至德矣，故“周之盛德”。纣穷否上，知存而不知亡，知得而不知丧，终以焚死，故“殷之末世”也。而马、荀、郑君从俗，以文王为中古，失之远矣。

是故其辞危，
虞翻曰：危，谓乾三。夕惕若厉，故“辞危”也。

危者使平，
陆绩曰：文王在纣世，有危亡之患，故于易辞多趋危亡。本自免济，建成王业，故易爻辞危者使平，以象其事。否卦九五：“其亡其亡，系于包桑”之属是也。

易者使倾。
陆绩曰：易，平易也。纣安其位，自谓平易，而反倾覆，故易爻辞“易者使倾”，以象其事。明夷上六：“初登于天，后入于地”之属是也。

其道甚大，百物不废。
虞翻曰：大，谓乾道。乾三爻，三十六物，故“百物不废”。略其奇八，与大衍之五十同义。惧以终始，其要无咎，此之谓易之道也。
虞翻曰：乾称易道，终日乾乾，故“无咎”。危者使平，易者使倾，恶盈福谦，故“易之道”者也。

夫乾，天下之至健也，德行恒易以知险。
虞翻曰：险，谓坎也。谓乾二、五之坤，成坎离，日月丽天，天险不可升，故“知险者”也。

夫坤，天下之至顺也，德行恒简以知阻。
虞翻曰：阻，险阻也。谓坤二、五之乾，艮为山陵，坎为水，巽高兑下，地险山川丘陵，故“以知阻”也。

能说诸心，
虞翻曰：乾五之坤，坎为心，兑为说，故“能说诸心”。

能研诸侯之虑，
虞翻曰：坎心为虑，乾初之坤为震，震为诸侯，故能研诸侯之虑。

定天下之吉凶，成天下之娓娓者。
虞翻曰：谓乾二、五之坤，成离日坎月，则八卦象具。八卦定吉凶，故能“定天下之吉凶”。娓娓，进也。离为龟，乾为蓍，月生震初。故“成天下之娓娓者”。谓莫善蓍龟也。
荀爽曰：娓娓者，阴阳之微，可成可败也。顺时者成，逆时者败也。

是故变化云为，吉事有祥。
虞翻曰：祥，几祥也，吉之先见者也。阳出，变化云为。吉事为祥。谓复初乾元者也。

象事知器，占事知来。
虞翻曰：象事谓坤。坤为器，乾五之坤成象，故“象事知器”也。占事谓乾以知来。乾五动成离，则玩其占，故“知来”。
侯果曰：易之云为，唯变所适。为善则吉事必应，观象则用器可为，求吉则未形可睹者也。

天地设位，圣人成能。
虞翻曰：天尊，五；地卑，二，故“设位”。乾为圣人。成能，谓能说诸心，能研诸侯之虑。故“成能”也。
崔觐曰：言易拟天地，设乾坤二位，以明重卦之义，所以成圣人伏羲文王之能事者也。

人谋鬼谋，百姓与能。
虞翻曰：乾为人，坤为鬼；乾二五之坤，坎为谋；乾为百，坤为姓，故“人谋鬼谋，百姓与能”。
朱仰之曰：人谋，谋及卿士。鬼谋，谋及卜筮也。又谋及庶民，故曰“百姓与能”。

八卦以象告。
虞翻曰：在天成象。乾二、五之坤，则八卦象成，兑口震言，故“以象告”也。

《爻》《彖》以情言，
崔觐曰：伏羲始画八卦，因而重之，以备万物，而告于人也。《爻》谓爻下辞，《彖》谓卦下辞，皆是圣人之情见乎系辞，而假《爻》《彖》以言，故曰“《爻》《彖》以情言”。

刚柔杂居，而吉凶可见矣。
虞翻曰：乾二之坤成坎，坤五之乾成离，故“刚柔杂居”。艮为居，离有巽兑，坎有震艮，八卦体备，故“吉凶可见”也。
崔觐曰：言文王以六爻刚柔相推，而物杂居，得理则吉，失理则凶，故“吉凶可见”也。

变动以利言，
虞翻曰：乾变之坤成震，震为言，故“变动以利言”也。

吉凶以情迁。
虞翻曰：乾吉坤凶。六爻发挥，旁通情也，故“以情迁”。

是以爱恶相攻，而吉凶生。
虞翻曰：攻，摩也。乾为爱，坤为恶。谓刚柔相摩，以爱攻恶。生吉以恶，攻爱生凶，故“吉凶生”也。

远近相取，而悔吝生。
虞翻曰：远阳，谓乾。近阴，谓坤。阳取阴生悔，阴取阳生吝。悔吝，言小疵。
崔觐曰：远，谓应与不应。近，谓比与不比。或取远应而舍近比，或取近比而舍远应，由此远近相取，所以生悔吝于系辞矣。

情伪相感，而利害生。
虞翻曰：情，阳。伪，阴也。情感伪生利，伪感情生害。乾为利，坤为害。

凡易之情，近而不相得，则凶。
韩康伯曰：近，况比爻也。易之情，刚柔相摩，变动相逼者也。近而不相得是，必有乖违之患也。或有相违而无患者，得其应也。相须而偕凶，乖于时也。随事以考之，义可见矣。

或害之，悔且吝。
虞翻曰：坤为害。以阴居阳，以阳居阴，为悔且吝也。

将叛者，其辞惭。
荀爽曰：谓屯六三往吝之属也。
虞翻曰：坎人之辞也。近而不相得，故“叛”。坎为隐伏，将叛。坎为心，故“惭”也。
侯果曰：凡心不相得，将怀叛逆者，辞必惭恧。

中心疑者，其辞枝。
荀爽曰：或从王事，无成之属也。
虞翻曰：离人之辞也。火性枝分，故枝疑也。
侯果曰：中心疑贰，则失得无从，故枝分不一也。

吉人之辞寡，躁人之辞多。
荀爽曰：谓睽上九之属也。
虞翻曰：震人之辞也。震为决躁，恐惧虩虩，笑言哑哑，故多辞。
侯果曰：躁人烦急，故“辞多”。

诬善之人，其辞游。
荀爽曰：游，豫之属也。
虞翻曰：兑人之辞也。兑为口舌，诬乾。乾为善人也。
崔觐曰：妄称有善，故自叙其美，而辞必浮游不实。

失其守者，其辞诎。
荀爽曰：谓泰上六城复于隍之属也。
侯果曰：失守则沮，辱而不申，故“其辞诎”也。爻有此象，故占辞亦从矣。
虞翻曰：巽人之辞也。巽诘诎，阳在初守巽，初阳入伏阴下，故“其辞诎”。此六子也。离上坎下，震起艮止，兑见巽伏。上经终坎离，则下经终既济未济。上系终乾坤，则下系终六子。此易之大义者也。

（本段续接前段；来源页面末行止于“此易之大义者也”。）""", chatGptTranslation = """**ChatGPT 翻译（逐段工作译文）**

**《易》这部书，不可以远离。**

侯果解释：平居时观察卦象，行动时玩味占筮，所以说《易》不可远离。

**它所体现的道，总是在不断迁移变化。**

虞翻解释：“迁”就是移动。日月循环运行，上下往来没有固定状态，所以说不断迁移。

**变化运动而不停留，周流于六个空位之间。**

虞翻解释：变就是变化，动就是运行；六虚指六个爻位。日月周流，终而复始，所以说周流六虚。这里又以甲子旬中辰、巳为空位的纳甲说解释：戊为月，离巳为日，日月进入中宫，所在之处为空虚，因此称六虚；其余五甲也依次类推。

**上下没有固定常态，刚柔相互交换。**

虞翻解释：刚柔象征昼夜。在天称上，进入地中称下，因此上下没有固定常态。

**不能把它规定成固定典要，只能随变化所趋而适应。**

虞翻解释：典是常， 要是道。上下没有固定常态，所以不能固定为典要；趋向乾便是白昼，趋向坤便是黑夜。侯果解释：六爻刚柔相互交换，远近关系也随之变化，只能依变化所趋而定，并没有固定不变的典要。

**它的出入各有法度，使人知道外在与内在都应有所戒惧。**

虞翻解释：从乾出去是外，进入坤是内；日行一度，所以说出入有度。阳气外出使人知道生长，阳气入内则使人警惧死亡，因此使人知道戒惧。韩康伯解释：说明出入的法度，使万物知道外内的戒备。出入犹如行动与退藏，外内犹如显露与隐伏。遯卦因远离时势而吉，丰卦因幽暗隐伏而致凶，渐卦以高显为美，明夷卦则因身处昏暗而利于守正，这些都是外内进退的警戒。

**又能明白忧患以及事情发生的缘故。**

虞翻解释：神妙能够预知未来，所以明白忧患；智慧能够收藏过去，所以知道事情的缘故。创作《易》的人，大概是怀有忧患意识的吧？

**即使没有师长保傅在旁，也如同父母临近自己。**

虞翻解释：“临”是看见。阴阳施行而生养万物，并没有师长保傅在旁创造它们；万物出生，都如同父母生养。孔子说，父母之道就是天地，乾为父，坤为母。干宝解释：这说明《易》道以戒惧为根本，也就是从始至终保持警惕，最终归于无咎。外在方面，男子从事王事，应当整日勤勉、夜晚警惕；内在方面，妇人居家则不可擅自专断。即使没有师长保傅切磋训诲，也应心存敬畏戒慎，常常如同父母正在面前一样。

**开始要端正它的言辞，并衡量它所指向的方向。**

虞翻解释：初是开始、居下；帅是端正。这里指修饰言辞、确立诚信。方向指坤；以乾通达坤，所以说先端正言辞，再衡量方向。侯果解释：率是修治，方是道。意思是修治《易》开篇的言辞，并推度它最终所指的道理；其中自有典常，并非虚设。

**既然已有典常，如果不是合适的人，道理就不会自行流行。**

虞翻解释：出入有法度，所以说有典常。“苟”是真诚；这里的“其人”指乾所象征的贤人。使道理神妙而明白，在于人；不言而使人信服，称为德行，所以说不是没有合适的人，道就不会虚行。崔觐解释：《易》道深远，如果不是圣人，便不能阐明它。因此《易》道不会无缘无故自行流行，必须有文王这样的人才能弘扬它。

**《易》这部书……**

干宝解释：这里再次谈《易》，是为了分别说明不同旨趣。

**它追溯事物的开端，归结事物的终点，以此作为根本。**

虞翻解释：“质”是根本。以乾追溯开端，以坤归结终点；追溯始终，是为了明白生死之说。崔觐解释：“质”是本体。所谓追溯事物开端，例如乾初九“潜龙勿用”；归结事物终点，例如上九“亢龙有悔”。《易》以潜龙勿用说明开端，以亢龙有悔说明终结，二者互相阐明而构成事物本体。其他卦也如此，例如大畜卦之后接着通达之类。

**六爻彼此交错，所体现的只是各自所处时位的事物。**

虞翻解释：阴阳交错居处称为杂。处于阳时便显现阳性，处于阴时便显现阴性，所以说要看它所处的时与物。

**乾是阳类的事物，坤是阴类的事物。**

干宝解释：一卦六爻，都杂有八卦之气。例如初九可以是震爻，九二可以是坎爻；有时见到戌便说艮，见到己亥便说兑；有时以甲、壬称乾，以乙、癸称坤；有时以午位称离，以子位称坎。也有以德气到来称为好物，以刑气到来称为恶物；王气、相气表示兴盛，休气、废气表示衰败的说法。

**初爻的辞意难以了解，上爻的辞意容易了解，这是因为它们分别处在本与末。**

侯果解释：本末就是初爻和上爻。初爻所涉及的事情尚微小，所以难知；上爻所涉及的事情已经显著，所以容易了解。

**初爻的辞用来拟议，事情到终局便成为确定结果。**

干宝解释：事情初起时只是拟议，所以难知；到最终完成时便容易了解，这是本末形势使然。侯果解释：过失刚出现时还很微小，仍可以通过拟议而转向福善；等到过失最终形成，事情已经走到尽头，就不是拟议所能挽回的。因此说事情到终局才完全显现。例如乾卦九三、噬嗑初九，还可以通过拟议转向善；到了上九，凶灾已经不可改变，这就是事情最终完成、凶极而不变的情形。

**至于把各种事物交杂起来、汇集德性并辨别是非，就不是中间的爻位所能不加以完备说明的。**

虞翻解释：汇集德性，是说乾能够辨别；“是”指阳，“非”指阴。“中”指正位。乾卦六爻中，二、四、上不正；坤卦六爻中，初、三、五不正，所以说事物交杂。因而重卦，爻辞处在其中；若不是中间爻位，爻辞便不能完备。道有变化运动，所以称为爻。崔觐解释：这一段先谈初爻和上爻，接着说明其余四爻。中间四爻交杂所主的事物，汇集所陈述的德性，并辨别是非；这些内容都完备地体现在卦的中间四爻。

**唉！如果要总括存亡吉凶，那么居然就可以知道了。**

虞翻解释：这是说知道存、知道亡，并能归结终局。居于乾的吉位便能保存，居于坤的凶位便会灭亡，所以说居然可以知道。崔觐解释：“噫”是感叹声。中间四爻也能总括卦中存亡吉凶之事，因此可以明显地知道。编者按：本页另有辨析孔颖达以二、五为中爻的说法，认为此处“中爻”应兼指中间四爻；这是编者按语，不是李鼎祚所辑古注。

**有智慧的人观察《彖传》的辞，大概就能思考到一半以上了。**

韩康伯解释：《彖传》举出立象的纲领，论述中爻的意义；用简约概括广博，以简明兼摄众多，把杂多事物和德性汇集起来，并以一个根本道理贯通。形体所宗本的是道，众多事物所归向的是一。事情越繁杂，就越容易滞留在形迹之中；道理越简约，就越接近道。《彖传》的意义就在于把握这个“一”，而“一”的作用与道相同。形而上的道可以由此观察；说思考过半，正是合宜的。

**第二爻与第四爻功用相同。**

韩康伯解释：它们具有相同的阴柔功用。崔觐解释：这是进一步说明中间四爻各自适宜的功用和位置。二爻主士、大夫之位，辅佐一国；四爻主三孤、三公、州牧等职位，辅佐天子。二者都有辅治理政的功用。

**但它们的位置不同。**

韩康伯解释：因为有内外之别。崔觐解释：二爻是士、大夫之位，较低；四爻是孤、公、牧伯之位，较高，所以位置不同。

**它们的吉善表现不同：二爻多有赞誉，四爻多有戒惧，因为四爻接近君主。**

韩康伯解释：二爻居中而平和，所以多有赞誉；四爻接近君主，所以多有戒惧。

**柔顺之道，不适宜处在距离阳刚很远的位置。**

崔觐解释：这里说二、四都是阴位。阴柔之道适宜接近、比附并承接阳刚，所以不宜远离阳刚。

**它的关键在于无咎，因为它以柔居中。**

崔觐解释：二爻是阴柔，距离阳刚较远，虽然有不利之处，但有时仍能无咎，因为它以柔爻居中，不同于四爻。

**第三爻与第五爻功用相同，但位置不同。**

韩康伯解释：二者有尊贵与卑贱的区别。崔觐解释：三爻是诸侯之位，五爻是天子之位；都承担治理人民的功用，但君臣位置不同。

**三爻多凶，五爻多功，这是尊贵与卑贱的等级差别。**

崔觐解释：三爻处于下卦顶端、上卦之下，是一国之君，权势重大而又承奉天子；如果没有含蓄内敛的美德，就必然招致凶险。五爻居中而不偏，尊居天位，以道治理万物，恩泽遍及天下，所以多有功绩。

**阴柔处在这些位置会危险，阳刚居之便能胜任吗？**

侯果解释：三、五都是阳位，阴柔处在那里便多凶险；刚健中正居在那里，就能胜任。句末“邪”是表示不确定的语气，并非绝对断言。有时柔爻居位也会吉，是因为得时；刚爻居位也可能凶，是因为失去应和。

**《易》这部书广大而完备。**

荀爽解释：以阴换阳称为广，以阳换阴称为大。《易》与天地相准，所以无所不备。

**其中有天道，有人道，也有地道。**

崔觐解释：这是说《易》阐明三才，广泛覆盖而无所遗漏，广大包容而无所不及；万物的象都包含在内。

**兼具三才之道而各自重叠，所以成为六爻。六爻并非别的东西，就是三才之道。**

崔觐解释：重卦有六爻，也兼具天地人三道；每两爻代表一才，六爻便代表三才，所以说兼三才而两之。六爻就是三才之道。

**道有变化运动，所以称为爻。**

陆绩解释：天道有昼夜、日月的变化；地道有刚柔、燥湿的变化；人道有行动与停止、动静、吉凶、善恶的变化。圣人设置爻来效法这三方面的变化运动，所以称为爻。

**爻有等级差别，所以称为物。**

干宝解释：等就是群类。爻中所含的意义，各种事物交汇聚集：五星、四时之气、六亲九族、福德刑杀以及万物形态，都可以从爻中显现，因此总称为物。颐卦中有物而称噬嗑，就是这个意思。

**事物彼此交杂，所以称为文。**

虞翻解释：乾是阳类，坤是阴类。纯乾、纯坤时还没有纹理；阳类进入坤、阴类进入乾，彼此交杂而形成六十四卦的爻，这才有文采，所以称文。

**文辞如果不恰当，吉凶便由此产生。**

干宝解释：卦爻辞形成文辞，人的行动作为必须考察具体事情，使它与爻义相称。事情与道义不相称，即使出现吉凶，也未必是当下所说的吉凶。例如“元亨利贞”，穆姜却因此而死；“黄裳元吉”，南蒯却因此失败，这就是文辞与实际不相称。经文中有“君子吉、小人否”的区别；占筮时，王气、相气对君子可能意味着升官，对小人却可能意味着获罪。

**《易》兴起，大概是在殷代末世、周德兴盛的时候吧？大概正当文王与纣王的时代吧？**

虞翻解释：这里指文王为《易》六爻作辞。殷末世象征乾上九，周盛德象征乾九三。文王拥有天下三分之二的土地，却仍服事殷商，所以周德可称至德。纣王处在否卦上爻的穷极之处，只知道生存而不知道灭亡，只知道获得而不知道丧失，最终遭到焚死。马融、荀爽、郑玄等人沿袭俗说，把文王说成中古之人，虞翻认为这种说法相差很远。

**因此，《易》的辞意带有危惧。**

虞翻解释：危惧指乾九三“终日乾乾，夕惕若厉”，所以说辞意危惧。

**处在危险中的人，因此能够转危为安。**

陆绩解释：文王处在纣王时代，有危亡之患，所以《易》辞多谈危亡；但他最终得以自免，并建立王业。因此《易》爻辞说处在危险中的人可以转为平安，象征这种经历。例如否卦九五“其亡其亡，系于包桑”。

**自以为平易安逸的人，因此反而倾覆。**

陆绩解释：纣王安居其位，自以为平易安稳，最终却遭到倾覆。因此《易》说自以为平易的人反而倾覆，象征他的遭遇。例如明夷上六“初登于天，后入于地”。

**它的道极其广大，百物没有被废弃遗漏。**

虞翻解释：大指乾道。乾有三爻，三十六种事物，所以说百物不废；略去其中奇数八的部分，与大衍五十之数有相同的意义。始终保持戒惧，最终无咎，这就是《易》道。虞翻又说：乾称为易道，体现为终日乾乾，所以无咎；危险者使之平安，安逸者使之倾覆，厌恶盈满而福佑谦逊，这就是易道。

**乾是天下最刚健的，德行恒常平易，因此能够认识险难。**

虞翻解释：险指坎卦。乾的二、五爻进入坤，形成坎、离；日月附丽于天，天险不可攀登，所以说知道险难。

**坤是天下最柔顺的，德行恒常简易，因此能够认识阻碍。**

虞翻解释：阻就是险阻。坤的二、五爻进入乾，形成艮山、坎水；巽高、兑低，地上有山川丘陵等险阻，所以说能够知道阻碍。

**能够使众人的心悦服。**

虞翻解释：乾五爻进入坤，形成坎，坎象征心；兑象征喜悦，所以能够使人心悦服。

**能够研察诸侯的思虑。**

虞翻解释：坎心象征思虑，乾初爻进入坤形成震，震象征诸侯，所以能够研察诸侯的思虑。

**能够决定天下的吉凶，成就天下不断前进的事业。**

虞翻解释：乾的二、五爻进入坤，形成离日、坎月，八卦之象因此完备。八卦能够决定吉凶，所以能决定天下吉凶。“娓娓”是前进的意思。离象征龟，乾象征蓍，月亮在震初生，所以说成就天下不断前进的事业；最善于预知的，莫过于蓍龟。荀爽解释：“娓娓”指阴阳细微变化，可以成功也可以失败；顺应时势便成功，违逆时势便失败。

**所以，变化与言行，吉祥的事情会有预兆。**

虞翻解释：祥是细微征兆，是吉祥事物最先显现的迹象。阳气出现，变化和言行随之发生；吉事先显为祥兆，这里指复卦初九的乾元。

**观察事物的形象便能知道器物，占问事情便能预知未来。**

虞翻解释：观察事物之象指坤，坤象征器物；乾五爻进入坤而形成象，所以观察事物便能知道器物。占问事情则以乾预知未来；乾五爻发动形成离，玩味占辞，便能预知未来。侯果解释：《易》的言行随变化而适应；行善则吉事必有回应，观察卦象便可制作器物，求问吉凶则能看见尚未成形的事情。

**天地设定位置，圣人成就其功能。**

虞翻解释：天尊居五位，地卑居二位，所以说天地设位。乾象征圣人；所谓成就功能，就是能够使人心悦服、研察诸侯思虑。崔觐解释：《易》模拟天地，设立乾坤二位，以说明重卦之义；这使伏羲、文王等圣人能够完成他们的事业。

**人谋与鬼谋共同参与，百姓也能参与其中。**

虞翻解释：乾象征人，坤象征鬼；乾二、五爻进入坤，坎象征谋划；乾象征百，坤象征姓，所以说人谋鬼谋，百姓参与其能。朱仰之解释：人谋是与卿士商议，鬼谋是通过卜筮商议；又与百姓商议，所以说百姓也参与其中。

**八卦用象来告知。**

虞翻解释：天上形成象。乾二、五爻进入坤，八卦之象形成；兑象征口，震象征言语，所以用卦象告知。

**爻辞与彖辞用情理来表达。**

崔觐解释：伏羲最初画出八卦，随后重叠成卦，以完备万物之象并告知世人。爻指爻下之辞，彖指卦下之辞；其中都显现圣人的情意，借爻辞、彖辞表达出来，所以说爻彖以情言。

**刚柔交杂居处，吉凶便可以看见。**

虞翻解释：乾二爻进入坤形成坎，坤五爻进入乾形成离，所以刚柔交杂。艮象征居处，离卦含有巽、兑，坎卦含有震、艮，八卦之体完备，所以吉凶可以看见。崔觐解释：文王以六爻刚柔相推，使各种事物交杂居处；合乎道理便吉，违背道理便凶，所以吉凶可见。

**变化运动用利益来说明。**

虞翻解释：乾变化进入坤形成震，震象征言语，所以变化运动以利益来表达。

**吉凶随着情势而迁移。**

虞翻解释：乾象征吉，坤象征凶。六爻发挥变化，旁通事物情势，所以吉凶随情势迁移。

**因此，喜爱与厌恶相互攻取，吉凶由此产生。**

虞翻解释：“攻”是摩擦、交互作用。乾象征喜爱，坤象征厌恶；刚柔相摩，以喜爱攻取厌恶。由善意而生吉，由恶意攻取善意则生凶。

**远近相互取用，悔恨与羞吝由此产生。**

虞翻解释：远指乾阳，近指坤阴。阳取阴会生悔，阴取阳会生吝；悔吝表示小的瑕疵。崔觐解释：远指相应与不相应，近指比邻与不比邻。有时舍近取远，有时舍远取近，因此产生悔吝。

**真实情意与虚伪相互感应，利益与损害由此产生。**

虞翻解释：情指阳，伪指阴。真实情意感应虚伪会产生利益，虚伪感应真实情意则产生损害；乾象征利益，坤象征损害。

**凡是《易》的情势，近处的爻彼此不能相得，就会凶险。**

韩康伯解释：近，指相邻的爻。《易》的情势，是刚柔相互摩擦、变化运动彼此逼近。相邻而不能相得，必然产生乖违之患。有时彼此相违却没有祸患，是因为另有相应；有时彼此依赖却一同凶险，是因为违背时势。应当结合具体事情考察，义理便可以看见。

**有时还会互相伤害，悔恨和羞吝也随之而来。**

虞翻解释：坤象征伤害。阴爻居阳位、阳爻居阴位，便形成悔恨和羞吝。

**将要背叛的人，说话带着惭愧。**

荀爽解释：例如屯卦六三“即鹿无虞，惟入于林中，君子几不如舍，往吝”一类。虞翻解释：坎象征人的言辞。相近却不能相得，所以有背叛之意；坎又象征隐伏，表示将要背叛；坎象征内心，所以言辞带着惭愧。侯果解释：内心不能相得、将要怀有叛逆之意的人，说话必定惭愧不安。

**内心疑惑的人，说话枝蔓分歧。**

荀爽解释：例如“或从王事，无成”一类。虞翻解释：离象征人的言辞。火性分枝，所以言辞分歧而疑惑。侯果解释：内心疑贰，便无法决定得失，因此言辞分散而不一致。

**吉善之人的言辞简少，急躁之人的言辞繁多。**

荀爽解释：例如睽卦上九一类。虞翻解释：震象征人的言辞。震为决断、躁动，又有恐惧、笑语等象，所以言辞多。侯果解释：急躁的人烦乱急迫，所以话多。

**诬称自己有善行的人，言辞浮游不实。**

荀爽解释：例如豫卦相关之象。虞翻解释：兑象征人的言辞。兑为口舌，用言辞诬饰乾；乾象征善人。崔觐解释：妄称自己有善行，便会不断叙说自己的美好，言辞必定浮泛不实。

**失去操守的人，言辞屈曲不伸。**

荀爽解释：例如泰卦上六“城复于隍”一类。侯果解释：失去操守便沮丧受辱，不能伸展，所以言辞屈曲。爻象如此，占辞也随之如此。虞翻解释：巽象征人的言辞。巽有屈曲之象；阳爻在初位守持巽体，初阳进入阴下而伏藏，所以言辞屈曲。这些是六子的象。离在上、坎在下，震象征起动，艮象征停止，兑象征显现，巽象征隐伏。上经以坎、离结束，下经以既济、未济结束；上系以乾、坤结束，下系以六子结束。这就是《易》的大义。

译释说明：本条从“易之为书也不可远”至页面所录末句逐段翻译。纳甲、卦变、六虚等术语依虞翻等注家的象数体系翻译，不将其解释改写为普遍定论。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十七·说卦传注文（下段）", "底本：文渊阁《四库全书》本《周易集解》卷十七，参校维基文库四库本第259—624行。承接上段，自“艮东北之卦也”至《说卦》末，录韩康伯、崔憬、孔颖达、虞翻、宋衷、荀爽、郑玄、陆绩、九家易、马融、朱仰之等注文。数字文本转录稿，影印页逐字校勘待完成。", """
“艮，东北之卦也，万物之所成终而所成始也，故曰成言乎艮。”
虞翻曰：艮三得正，故复称卦。万物成始，乾甲成终，坤癸；艮东北，是甲癸之间，故万物之所成终而成始者也。

“神也者，妙万物而为言者也。”
韩康伯曰：于此言神者，明八卦运动变化推移，莫有使之然者。神则无物，妙万物而为言也。明则雷疾风行、火炎水润，莫不自然相与而为变化，故能万物既成。

“动万物者，莫疾乎雷。”
崔憬曰：谓春分之时，雷动则草木滋生，蛰虫发起，所动万物莫急于此也。
“橈万物者，莫疾乎风。”
崔憬曰：风能鼓橈万物，春则发散草木枝叶，秋则摧残草木枝条。
“燥万物者，莫熯乎火。”
崔憬曰：火能干燥万物；熯亦燥也。
“说万物者，莫说乎泽。”
崔憬曰：言光说万物，莫过以泽而成说之也。
“润万物者，莫润乎水。”
崔憬曰：滋润万物，莫过以水而润之。
“终万物、始万物者，莫盛乎艮。”
崔憬曰：大寒、立春之际，艮之方位，万物以之始而为今岁首，以之终而为去岁末。此言六卦之神用而不言乾坤者，以乾坤而发天地，无为而无不为，能成雷风等有为之神妙也。

“故水火相逮。”
孔颖达曰：上章言水火不相入，此言水火相逮；既不相入，又不相及，则无成物之功，明性虽不相入而气相逮及。
“雷风不相悖。”
孔颖达曰：二象俱动，若相薄而相悖逆，则相伤害，亦无成物之功；明虽相薄而不相逆者也。
“山泽通气。”
崔憬曰：山泽虽相悬远而气交通，然后能变化既成万物矣。
虞翻曰：谓乾变而坤化，乾道变化，各正性命，成既济定，故既成万物矣。

“乾，健也。”
虞翻曰：精刚自胜，动行不休，故健也。
“坤，顺也。”
虞翻曰：纯柔承天，时行故顺。
“震，动也。”虞翻曰：阳出动行。
“巽，入也。”虞翻曰：乾初入阴。
“坎，陷也。”虞翻曰：阳陷阴中。
“离，丽也。”虞翻曰：日丽乾刚。
“艮，止也。”虞翻曰：阳位在上，故止。
“兑，说也。”
虞翻曰：震为大笑，阳息震成兑；震言出口，故说。此上虞义也。

“乾为马，坤为牛，震为龙。”
孔颖达曰：乾象天行健，故为马；坤象地任重而顺，故为牛；震象龙动，故为龙。此上孔正义。
“巽为鸡。”
《九家易》曰：应八风也。风应节而变，变不失时；鸡时至而鸣，与风相应也。
“坎为豕。”
《九家易》曰：污辱卑下也。坎豕怀胎四月而生，宣时理节，是其义也。
“离为雉。”
孔颖达曰：离为文明，雉有文章，故离为雉。
“艮为狗。”
《九家易》曰：艮止主守御也。其说又以斗数、犬之生育及守夜等象释之。
“兑为羊。”
孔颖达曰：兑为说，羊者顺从之畜，故为羊。

“乾为首，坤为腹，震为足，巽为股，坎为耳，离为目，艮为手，兑为口。”
孔颖达曰：乾尊而在上，故为首；坤能包藏含容，故为腹；震动用，故为足；巽为顺，股顺随于足，故巽为股；坎北方主听，故为耳；离南方主视，故为目；艮为止，手亦止持于物使不动，故艮为手；兑为说，口所以说言，故兑为口。

“乾，天也，故称乎父；坤，地也，故称乎母。”
崔憬曰：欲明六子，故先说乾称天父、坤称地母。
崔憬曰：震一索而得男，故谓之长男；巽一索而得女，故谓之长女；坎再索而得男，故谓之中男；离再索而得女，故谓之中女；艮三索而得男，故谓之少男；兑三索而得女，故谓之少女。
孔颖达曰：索，求也。以求乾坤为父母而求其子也。得父气者为男，得母气者为女；坤初求得乾气为震，故曰长男；坤二得乾气为坎，故曰中男；坤三得乾气为艮，故曰少男；乾初得坤气为巽，故曰长女；乾二得坤气为离，故曰中女；乾三得坤气为兑，故曰少女。

乾为天、为圜、为君、为父、为玉、为金、为寒、为冰、为大赤、为良马、为老马、为瘠马、为驳马、为木果。
宋衷曰：乾动作不解，天亦转运；动作转运，非圜不能，故为圜。虞翻曰：为君，贵而严也；为父，成三男，取其类大。崔憬曰：天体清明而刚，故为玉、为金。孔颖达曰：取其西北冰寒之地。虞翻曰：太阳为赤，月望出入时也。崔憬曰：乾四月纯阳之卦，故取盛阳色为大赤。虞翻曰：乾善，故良也。《九家易》曰：言气衰也，息至巳必当复消，故为老马。崔憬曰：骨为阳、肉为阴，乾纯阳爻骨多，故为瘠马。宋衷曰：天有五行之色，故为驳马；群星著天似果实著木，故为木果。

坤为地、为母、为布、为釜、为吝啬、为均、为子母牛、为大舆、为文、为众、为柄，其于地也为黑。
虞翻曰：坤柔道静；成三女能致养，故为母。崔憬曰：遍布万物于致养，故坤为布；万物依之为本，故为柄。孔颖达曰：取其化生成熟，故为釜；取地生物而不转移，故为吝啬；取其能载，故为大舆。崔憬曰：地生万物不择善恶，故为均。九家易曰：土能生育，牛亦含养，故为子母牛；万物相杂，故为文。虞翻曰：三阴相随，故为众。崔憬曰：坤十月卦，极阴之色，故其于色也为黑。

震为雷、为駹、为玄黄、为专、为大涂、为长子、为决躁、为苍筤竹、为萑苇；其于马也为善鸣、为馵足、为作足、为的颡；其于稼也为反生；其究为健、为蕃鲜。
虞翻曰：震为雷；“駹”是苍色，震东方故为駹，旧读作龙，上已为龙，非也。天玄地黄，震为天地杂物，故为玄黄。阳在初隐静未出，触坤故专。崔憬曰：万物所出在春，故为大涂；取其刚在下动，故为决躁。虞翻曰：乾一索故为长子。九家易曰：苍筤，青也；震阳在下，根长坚刚，阴交在中，使外苍筤。萑苇即蒹葭，根茎丛生、蔓衍相连。虞翻曰：为雷故善鸣；震为左、为足，初阳白，故为作足。宋衷曰：阴在上、阳在下，故为反生，谓枲、豆之类戴甲而生。虞翻曰：震巽相薄，变而至三则下象究，与四成乾，故其究为健、为蕃鲜。

巽为木、为风、为长女、为绳直、为工、为白、为长、为高、为进退、为不果、为臭；其于人也为宣发、为广颡、为多白眼、为近利市三倍；其究为躁卦。
宋衷曰：阳动阴静，二阳动于上，一阴安静于下，有似于木。陆绩曰：风，土气也，巽坤之所生，故为风，亦取静于本而动于末。荀爽曰：柔在初，故为长女；以绳木，故为工。翟玄曰：上二阳共正一阴，使不得邪僻，如绳之直。虞翻曰：乾阳在上故白；巽行远故长；乾阳在上长故高；阳初退故进退。荀爽曰：风行无常，故进退；风行或东或西，故不果。虞翻曰：巽二入艮鼻，故为臭；为白故宣发。其广颡、多白眼、近利市三倍及其究为躁卦，皆以巽卦爻变取象，原文详见底本。

坎为水、为沟渎、为隐伏、为矫輮、为弓轮；其于人也为加忧、为心病、为耳痛、为血卦、为赤；其于马也为美脊、为亟心、为下首、为薄蹄、为曳；其于舆也为多眚；为通、为月、为盗；其于木也为坚多心。
宋衷曰：坎阳在中，内光明，有似于水；曲者更直为矫，直者更曲为輮，水流有曲直。虞翻曰：阳辟坤，水性流通，故为沟渎；阳藏坤中，故为隐伏。坎为月，月在于庚为弓，在甲象轮。孔颖达曰：坎劳卦又主听，听劳则耳痛；人之有血犹地之有水，赤为血色。宋衷曰：阳在中央，马脊之象。崔憬曰：内阳刚动，故为亟心。荀爽曰：水之流，首卑下。九家易曰：水趋下则流散，流散则薄，故为薄蹄。虞翻曰：坤为大车，坎折坤体，故车多眚；水流渎故通；坤为夜，以坎阳光坤故为月；水行潜窃故为盗；阳刚在中，故坚多心，棘枣属也。孔颖达又解释乾、震、坎皆以马喻，因其健、动、行之性。

离为火、为日、为电、为中女、为甲胄、为戈兵；其于人也为大腹；为乾卦；为鳖、为蟹、为蠃、为蚌、为龟；其于木也为科上槁。
崔憬曰：离阳在外，象火之外照。荀爽曰：阳外光也。郑玄曰：久明似日，暂明似电。荀爽曰：柔在中也。虞翻曰：外刚故为甲；乾为首，巽绳贯甲而在首止，故为胄。乾为金，离火断乾、燥而炼之，故为戈兵。象曰常满，如妊身妇，故为大腹。火曰熯，燥物，故为乾卦。鳖、蟹、蠃、蚌、龟五者皆取外刚内柔。巽木在离中，体大过死；巽虫食心则折，故上槁。宋衷曰：阴在内则空中，木中空则上科槁也。

艮为山、为径路、为小石、为门阙、为果蓏、为阍寺、为指、为拘、为鼠、为黔喙之属；其于木也为坚多节。
宋衷曰：二阴在下，一阳在上；阴为土，阳为木，土积于下、木生其上，山之象也。虞翻曰：艮为山中径路；震阳在初则为大涂，艮阳小故为径路。陆绩曰：艮刚卦之小，故为小石。虞翻曰：艮为门，艮阳在门外，故为门阙。宋衷曰：木实谓之果，草实谓之蓏；桃李瓜瓞之属皆出山谷。阍人主门，寺人主巷，艮为止，此职皆掌禁止。虞翻曰：艮手多节故为指；指屈伸制物故为拘，拘旧作狗，上已为狗字之误。马融曰：黔喙肉食之兽，谓豺狼之属；黔，黑也，阳玄在前。虞翻曰：阳刚在外，故多节，松柏之属。

兑为泽、为少女、为巫、为口舌、为毁折、为附决；其于地也为刚卤；为妾、为羔。
虞翻曰：坎水半见故为泽。宋衷曰：阴在上令下湿，故为泽。虞翻曰：坤三索，位在末故少；乾为神，兑为通，与神通气，女故为巫；兑得震声故为口舌；二折震足故为毁折；乾体未圜故附决。乾二阳在下故刚，泽水润下故咸。朱仰之曰：取金之刚不生也，刚卤之地不生物，故为刚卤。虞翻曰：三少女位贱故为妾；羔、女使皆取位贱。旧读以震駹为龙、艮拘为狗、兑羔为羊，皆已见上，此为再出，非孔子意也。震已为长男，又言长子，谓以当继世、守宗庙、主祭祀，故详举之；三女皆言长、中、少，明女子各当外成，故别见之。此其大例者也。此上虞义。
""",
        chatGptTranslation = """ChatGPT 翻译（逐段工作译文）

本条承接《说卦传》“艮，东北之卦也”，翻译至篇末，并保留各家解释之间的差异。

【“艮，东北之卦也，万物之所成终而所成始也”】
虞翻说，艮卦第三爻得正，所以这里再次直接称它为卦。万物的开始以乾甲表示，终结以坤癸表示；艮处在东北，恰好位于甲与癸之间，因此象征万物在这里结束旧周期、开始新周期。

【“神也者，妙万物而为言者也”】
韩康伯说，这里谈“神”，是要说明八卦的运动、变化和推移，并没有一个外在力量在背后命令它们如此。所谓神，并不是某种具体的物，而是使万物变化精妙的作用。雷迅速发动、风流行、火向上炎、水向下润，都是自然而然地相互配合，因而能够促成万物生成。

【“动万物者，莫疾乎雷”至“终万物、始万物者，莫盛乎艮”】
崔憬逐项解释六卦的自然作用：春分时雷声发动，草木生长、蛰虫惊起，所以发动万物没有比雷更迅速的；风能鼓动、摇动万物，春天使枝叶舒展，秋天又摧折枝条；火能使万物干燥；泽能使万物欣悦；水能滋润万物；大寒至立春之间，艮所对应的方位使万物结束上一年的周期并开启新一年的周期。崔憬还说明，这里只列六卦而不列乾坤，是因为乾坤体现天地本体，似乎无为，却能成就雷、风等有为之神妙作用。

【“故水火相逮，雷风不相悖，山泽通气”】
孔颖达说，前文说水火“不相射”，这里又说水火“相逮”，两者并不矛盾：水火虽然性质不同、不相互侵入，却能在气的层面相互接应；若完全不能接触，也就不能成就万物。雷与风都在运动，如果相互冲突、彼此伤害，同样不能生成万物；所以说它们虽相迫近，却不相违逆。崔憬说，山与泽虽然相隔遥远，但气息相通，因而能够变化并生成万物。虞翻则从乾坤变化解释：乾变化、坤化生，乾道变化使万物各正其性命，最终形成既济、稳定的状态。

【“乾健也，坤顺也，震动也，巽入也，坎陷也，离丽也，艮止也，兑说也”】
虞翻逐卦解释：乾以精纯刚健、自强不息为义；坤以柔顺承受天道、顺时运行；震是阳气出现而发动；巽是乾阳进入阴体；坎是阳陷入阴中；离是太阳依附于乾刚；艮是阳爻居上而停止；兑则是喜悦。兑的解释又联系震卦：震象征大笑和言语，阳气增长使震变为兑，言语从口中发出，所以兑有喜悦、言说之义。

【“乾为马，坤为牛，震为龙”】
孔颖达说，乾象征天体运行健劲，所以取马象；坤象征大地承载重物而柔顺，所以取牛象；震象征龙的发动，所以取龙象。

【“巽为鸡，坎为豕，离为雉，艮为狗，兑为羊”】
九家易说，巽为鸡，是因为鸡按时鸣叫，与风应时变化相似。坎为猪，是取其污湿、卑下等特征，并附会猪怀胎四个月等说法。孔颖达说，离象征文明，雉有鲜明羽毛纹彩，所以离为雉。九家易以艮主停止、守御解释艮为狗，并进一步引入斗数、犬的生育和守夜习性等说法。孔颖达说，兑有喜悦、顺从之义，羊是顺从的牲畜，所以兑为羊。

【“乾为首，坤为腹，震为足，巽为股，坎为耳，离为目，艮为手，兑为口”】
孔颖达说，乾尊贵而居上，所以象征头；坤包容、含藏，所以象征腹；震主动，所以象征足；巽顺随于足，所以象征股；坎位在北方、主听觉，所以象征耳；离位在南方、主视觉，所以象征眼；艮有停止、把持之义，所以象征手；兑主言说，口是说话的器官，所以象征口。

【“乾，天也，故称乎父；坤，地也，故称乎母”及六子生成】
崔憬说，为了说明六子，先说明乾是天、是父，坤是地、是母。孔颖达解释“索”为求取：把乾坤看作父母，再从父母之气中求取子女。得到父气的为男，得到母气的为女。坤从初爻开始依次得到乾气，形成震、坎、艮，分别称长男、中男、少男；乾从初爻开始依次得到坤气，形成巽、离、兑，分别称长女、中女、少女。

【乾卦取象】
宋衷说，乾不断运动，天体也持续运行；运行转动必须呈圆形，所以乾为圆。虞翻说，乾为君，是因为尊贵严正；为父，是因为统摄三男。崔憬说，天体清明刚健，所以取玉、金之象。孔颖达以西北寒冷之地解释乾为寒、为冰；崔憬又以乾对应冬季说明寒冰之象。虞翻以太阳和月望时的光色解释“大赤”，崔憬则认为乾对应四月纯阳，所以象征盛阳之赤。虞翻说乾善，故为良马；九家易说阳气衰退，故为老马；崔憬以纯阳骨多、肉少解释瘠马；宋衷以天有五行之色解释驳马，又把群星布列天空比作果实长在树上，因此乾也为木果。

【坤卦取象】
虞翻说坤柔静，所以为地；能养育三女，所以为母。崔憬说坤普遍布施养育万物，所以为布；万物以地为根本，所以为柄。孔颖达以地使万物成熟解释为釜，以土地生物而不迁移解释为吝啬，以承载万物解释为大车。崔憬说土地不择善恶地生育万物，所以为均。九家易说土能生育、牛能含养，所以为子母牛；万物杂然并生，所以为文。虞翻说三阴相随，故为众。坤对应十月，阴气极盛，所以在颜色上为黑。

【震卦取象】
虞翻说震为雷；“駹”指青苍色，震属东方，所以为駹，旧本读作“龙”是错误的，因为前面已经有“震为龙”。天色玄、地色黄，震兼有天地之象，所以为玄黄。阳爻居初而尚未显露，触动坤体，因而有专一、静伏之象。崔憬以春天万物萌生、道路通达解释震为大路；阳刚在下发动，所以为决躁。虞翻说乾初求坤气而成震，所以震为长子。九家易以青色、根茎坚实解释苍筤竹，并以根茎丛生、延展相连解释萑苇。震为雷，所以为善鸣；又因震象征左、足，初爻为阳白，所以有白足之马象。宋衷说阴在上、阳在下，像种子戴着外壳向下扎根、反向生长，所以稼穑取反生之象。虞翻说震巽相迫，卦变至三爻后下体变化，并与第四爻合成乾，因此最终取健、繁茂鲜明之象。

【巽卦取象】
宋衷说，两阳在上发动，一阴在下安静，形状像树木，所以巽为木。陆绩说风是土气所生，巽由坤而来；树根静止、枝梢摇动，也像风的特征。荀爽说柔爻在初，所以巽为长女。翟玄说上面两阳共同端正中间一阴，使其不偏斜，像绳墨取直。孔颖达以号令整齐万物解释绳直。荀爽说木工用绳墨，所以为工；虞翻也用巽与近利、市集的象解释工。虞翻以乾阳在上解释为白；崔憬以风行远解释为长；虞翻和孔颖达分别从阳在上、树木向上生长解释为高。阳气初退、风向无常，所以有进退、不果之象。虞翻说“臭”是气味，巽气进入艮鼻，所以为臭；巽为白，所以为宣发。广颡、多白眼、近利市三倍等，都是虞翻依卦爻变化推演出的象数解释；他又以卦变至震解释巽最终成为躁卦。

【坎卦取象】
宋衷说，坎中间有阳爻，内部明亮，形状类似水。虞翻说阳爻开辟坤体，水性流通，所以为沟渎；阳藏在坤中，所以为隐伏。宋衷以水流有曲有直解释矫揉。虞翻说坎为月，月在庚位像弓，在甲位像车轮，所以为弓轮。坎有忧劳之象，因此为加忧、心病；孔颖达以坎主听、听觉劳损解释耳痛，以血如同地上之水、血色为赤解释血卦和赤。宋衷以阳爻居中解释马脊美；崔憬以内部阳刚发动解释亟心；荀爽以水流向低处解释下首；九家易以水向下流散、蹄部薄弱解释薄蹄；宋衷以水摩擦地面而行解释曳。虞翻说坤为大车，坎损坏坤体，所以车多故障；水流通，所以为通；坤为夜，坎中阳光照坤，所以为月；水在暗处潜行，所以为盗；阳刚在木心之中，所以为坚多心，指棘、枣一类木材。孔颖达补充，乾、震、坎都取马象，是分别取健、动、行的特征；坎只取马能行，不取其健，因此又有低头、蹄薄、拖曳等象。

【离卦取象】
崔憬说离阳在外，像火向外照耀。荀爽说阳在外发光，所以为日；郑玄说火光持续明亮像太阳，短暂闪现像闪电。柔爻居中，所以为中女。虞翻说外部刚强，所以为甲；乾为头，巽绳贯甲并在头部止住，所以为胄，即头盔。乾为金，离火熔炼金属，所以为戈兵。大腹之象取腹部常满、如孕妇之形。火能烘干物品，所以为乾卦。鳖、蟹、螺、蚌、龟都具有外壳坚硬、内部柔软的特点。巽木处在离火之中，受到火烧或虫蛀，树木上部枯槁；宋衷则以木内空解释上部枯槁。

【艮卦取象】
宋衷说，两阴在下、一阳在上，阴像土、阳像木，土堆积而木生其上，构成山的形象。虞翻说艮为山中小路；震阳在初，路大，所以为大道；艮阳较小，所以为小径。陆绩说艮是刚卦中较小的一类，所以为小石。虞翻说艮为门，阳爻在门外，所以为门阙，像两座小山之间的缺口。宋衷说木本的果实叫果，草本的果实叫蓏，桃李瓜瓞等生于山谷。守门者叫阍人，掌巷者叫寺人；艮有禁止之义，这些职务都与守止有关。虞翻说艮象征手，手指关节多，所以为指；手指屈伸以控制物体，所以为拘，但“拘”旧本写作“狗”，是前文“艮为狗”误字的重复。虞翻以像狗而较小、藏在坎穴中解释为鼠。马融说“黔喙”是黑色嘴部的肉食兽，如豺狼；虞翻说阳刚在外，所以树木坚硬多节，如松柏。

【兑卦取象】
虞翻说坎水只显露一半，所以为泽；宋衷说阴在上，使下方潮湿，所以为泽。虞翻说兑是坤三次求乾所得，处在最后，所以为少女。乾为神，兑有通达之义，女子与神相通，所以为巫；兑得到震的声音，所以为口舌；二爻折损震足，所以为毁折；乾体尚未圆满，所以为附决。乾有两阳在下，所以刚；泽水润下而含咸味，所以为刚卤。朱仰之说，刚金之地不能生长作物，盐碱地也不生物，所以称刚卤。虞翻说少女地位较低，所以为妾；羔与女使也取地位卑下之义。旧读把震的“駹”读作龙、艮的“拘”读作狗、兑的“羔”读作羊，都是前文已经出现过的重复误读，并非孔子本意。震既是长男，又称长子，是因为长子要继承家业、守护宗庙并主持祭祀；三女分别称长、中、少，则说明女子各有其外在归属。这是虞翻所说的总原则。

译注说明：本条包含大量古代卦象类比、身体部位、动物和物产配象，以及虞翻的卦变推演。译文只解释原注所表达的历史观点，不代表这些象数对应是现代科学事实，也不把各家说法合并成唯一结论。"""
    ),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十七·说卦传注文（后段连续原文补录）", "底本：景印文渊阁《四库全书》本《周易集解》卷十七，参校维基文库四库本卷十七数字转录第 334—626 行。承接既有《说卦》注文上段，从“离为雉”起，录八卦取象、六子生成及乾坤震巽坎离艮兑诸象注文，至“此其大例者也，此上虞义”。本条按数字底本录入；部分注释在原页面未署名处仍按原样保留，不擅自补注家姓名。疑字、讹字与异体字待影印页复核。", """
《说卦》八卦取象及各卦物象注文（续）

离为雉。
孔颖达曰：离为文明，雉有文章，故离为雉。

艮为狗。
《九家易》曰：艮止主守御也。艮数三七九六十三，三主斗，斗为犬，故犬怀胎三月而生。斗运行十三时日出，故犬十三日而开目。斗屈故犬卧屈也。斗运行四市，犬亦夜绕室也。犬之精畏水，不敢饮，但舌舐水耳。犬斗以水灌之则解也。犬近奎星，故犬淫当路不避人者也。

兑为羊。
孔颖达曰：兑为说，羊者顺从之畜，故为羊。

乾为首。
乾尊而在上，故为首。

坤为腹。
坤能包藏含容，故为腹也。

震为足。
震动用，故为足。

巽为股。
巽为顺，股顺随于足，故巽为股。

坎为耳。
坎北方主听，故为耳。

离为目。
离南方主视，故为目。

艮为手。
艮为止，手亦止持于物，使不动，故艮为手。

兑为口。
兑为说，口所以说言，故兑为口。此上孔正义。

乾天也，故称乎父；坤地也，故称乎母。
崔憬曰：欲明六子，故先说乾称天父，坤称地母。
震一索而得男，故谓之长男；巽一索而得女，故谓之长女；坎再索而得男，故谓之中男；离再索而得女，故谓之中女；艮三索而得男，故谓之少男；兑三索而得女，故谓之少女。
孔颖达曰：索，求也。以求乾坤为父母而求其子也。得父气者为男，得母气者为女。坤初求得乾气为震，故曰长男；坤二得乾气为坎，故曰中男；坤三得乾气为艮，故曰少男；乾初得坤气为巽，故曰长女；乾二得坤气为离，故曰中女；乾三得坤气为兑，故曰少女。此言所以生六子者也。

乾为天。
宋衷曰：乾动作不解，天亦转运。
为圜。
宋衷曰：动作转运，非圜不能，故为圜。
为君。
虞翻曰：贵而严也。
为父。
虞翻曰：成三男，取其类大，故为父也。
为玉，为金。
崔憬曰：天体清明而刚，故为玉、为金。
为寒，为冰。
孔颖达曰：取其西北冰寒之地。
崔憬曰：乾主立冬已后、冬至已前，故为寒、为冰也。
为大赤。
虞翻曰：太阳为赤，月望出入时也。
崔憬曰：乾四月纯阳之卦，故取盛阳色为大赤。
为良马。
虞翻曰：乾善，故良也。
为老马。
《九家易》曰：言气衰也，息至巳必当复消，故为老马也。
为瘠马。
崔憬曰：骨为阳，肉为阴；乾纯阳爻，骨多，故为瘠马也。
为駮马。
宋衷曰：天有五行之色，故为駮马也。
为木果。
宋衷曰：群星著天，似果实著木。

坤为地。
虞翻曰：柔道静。
为母。
虞翻曰：成三女，能致养，故为母。
为布。
崔憬曰：遍布万物于致养，故坤为布。
为釜。
孔颖达曰：取其化生成熟，故为釜也。
为吝啬。
孔颖达曰：取地生物而不转移，故为吝啬也。
为均。
崔憬曰：取地生万物不择善恶，故为均也。
为子母牛。
《九家易》曰：土能生育，牛亦含养，故为子母牛也。
为大舆。
孔颖达曰：取其能载，故为大舆也。
为文。
《九家易》曰：万物相杂，故为文也。
为众。
虞翻曰：物三称群，阴为民，三阴相随，故为众也。
为柄。
崔憬曰：万物依之为本，故为柄。
其于地也为黑。
崔憬曰：坤十月卦，极阴之色，故其于色也为黑矣。

震为雷。
虞翻曰：太阳火得水有声，故为雷也。
为駹。
駹，苍色。震东方，故为駹。旧读作龙，上已为龙，非也。
为玄黄。
天玄地黄，震天地之杂物，故为玄黄。
为专。
阳在初，隐静未出，触坤故专，则乾静也。尃，延叔坚说以专为旉、大布，非也。此上虞义者也。
为大涂。
崔憬曰：万物所出在春，故为大涂，取其通生性也。
为长子。
虞翻曰：乾一索，故为长子。
为决躁。
崔憬曰：取其刚在下动，故为决躁也。
为苍筤竹。
《九家易》曰：苍筤，青也。震阳在下，根长坚刚，阴交在中，使外苍筤也。
为萑苇。
《九家易》曰：萑苇，蒹葭也。根茎丛生，蔓衍相连，有似雷行也。
其于马也为善鸣。
虞翻曰：为雷，故善鸣也。
为馵足，为作足。
马白后左足为馵。震为左，为足，为作；初阳白，故为作足。
为的颡。
的，白也；颡，额也。震体头在口上，白故的颡。《诗》云“有马白颠”，是也。此上虞义也。
其于稼也为反生。
宋衷曰：阴在上，阳在下，故为反生，谓枲、豆之类戴甲而生。
其究为健，为蕃鲜。
虞翻曰：震巽相薄，变而至三，则下象究，与四成乾，故其究为健、为蕃鲜。巽究为躁卦，躁卦则震雷、巽风无形，故卦特变耳。

巽为木。
宋衷曰：阳动阴静，二阳动于上，一阴安静于下，有似于木也。
为风。
陆绩曰：风，土气也，巽坤之所生，故为风；亦取静于本而动于末也。
为长女。
荀爽曰：柔在初。
为绳直。
翟玄曰：上二阳共正一阴，使不得邪僻，如绳之直。
孔颖达曰：取其号令齐物如绳直也。
为工。
荀爽曰：以绳木，故为工。
虞翻曰：为近利市三倍，故为工。子夏曰：工居肆。
为白。
虞翻曰：乾阳在上，故白。
孔颖达曰：取其风吹去尘，故絜白也。
为长。
崔憬曰：取风行之远，故为长。
为高。
虞翻曰：乾阳在上长，故高。
孔颖达曰：取木生而高上。
为进退。
虞翻曰：阳初退，故进退。
荀爽曰：风行无常，故进退。
为不果。
荀爽曰：风行或东或西，故不果。
为臭。
虞翻曰：臭，气也。风至知气，巽二入艮鼻，故为臭。《系》曰：“其臭如兰。”
其于人也为宣发。
虞翻曰：为白，故宣发。马君以宣为寡发，非也。
为广颡。
变至三，坤为广；四动成乾为颡，在头口上，故为广颡。与震的颡同义；震一阳，故的颡；巽变乾二阳，故广颡。
为多白眼。
为白，离目上向，则白眼见，故多白眼。
为近利市三倍。
变至三成坤，坤为近；四动乾，乾为利；至五成噬嗑，故称市；乾三爻为三倍，故为近利市三倍。动上成震，故其究为躁卦。八卦诸为，唯震巽变耳。
其究为躁卦。
变至五成噬嗑为市，动上成震，故其究为躁卦，明震内体为专、外体为躁。此上虞义。

坎为水。
宋衷曰：坎阳在中，内光明，有似于水。
为沟渎。
虞翻曰：以阳辟坤，水性流通，故为沟渎也。
为隐伏。
虞翻曰：阳藏坤中，故为隐伏也。
为矫揉。
宋衷曰：曲者更直为矫，直者更曲为揉；水流有曲直，故为矫揉。
为弓轮。
虞翻曰：可矫揉，故为弓轮。坎为月，月在于庚为弓，在甲象轮，故弓轮也。
其于人也为加忧。
两阴失心，为多眚，故加忧。
为心病。
为劳而加忧，故心病；亦以坎为心，坎二折坤为心病。此上虞义也。
为耳痛。
孔颖达曰：坎劳卦也，又主听，听劳则耳痛。
为血卦，为赤。
孔颖达曰：人之有血，犹地之有水，赤血色也。案十一月一阳爻生在坎，阳气初生于黄泉，其色赤也。
其于马也为美脊。
宋衷曰：阳在中央，马脊之象也。
为亟心。
崔憬曰：取其内阳刚动，故为亟心也。
为下首。
荀爽曰：水之流，首卑下也。
为薄蹄。
《九家易》曰：薄蹄者，在下水又趋下；趋下则流散，流散则薄，故为薄蹄也。
为曳。
宋衷曰：水摩地而行，故曳。
其于舆也为多眚。
虞翻曰：眚，败也。坤为大车，坎折坤体，故为车多眚也。
为通。
水流渎，故通也。
为月。
坤为夜，以坎阳光坤，故为月也。
为盗。
水行潜窃，故为盗也。
其于木也为坚多心。
阳刚在中，故坚多心，棘、枣属也。此上虞义也。
孔颖达曰：乾、震、坎皆以马喻。乾至健，震至动，坎至行，故皆可以马为喻。坤则顺，艮则止，巽亦顺，离文明而柔顺，兑柔说，皆无健，故不以马为喻也。唯坤卦利牝马，取其行不取其健，故曰牝也。坎亦取其行不取其健，皆外柔，故为下首、薄蹄、曳也。

离为火。
崔憬曰：取卦阳在外，象火之外照也。
为日。
荀爽曰：阳外光也。
为电。
郑玄曰：取火明也，久明似日，暂明似电也。
为中女。
荀爽曰：柔在中也。
为甲胄。
虞翻曰：外刚，故为甲。乾为首，巽绳贯甲而在首止，故为胄；胄，兜鍪也。
为戈兵。
乾为金，离火断乾，燥而炼之，故为戈兵也。
其于人也为大腹。
象曰：常满如妊身妇，故为大腹。乾为大也。
为乾卦。
火曰熯，燥物，故为乾卦也。
为鳖，为蟹，为蠃，为蚌，为龟。
此五者皆取外刚内柔也。
其于木也为科上槁。
巽木在离中，体大过死。巽虫食心则折也；蠧虫食口木，故上槁。或以离火烧巽，故折上槁。此上虞义。
宋衷曰：阴在内则空中，木中空则上科槁也。

艮为山。
宋衷曰：二阴在下，一阳在上，阴为土，阳为木；土积于下，木生其上，山之象也。
为径路。
虞翻曰：艮为山中径路，震阳在初则为大涂，艮阳小，故为径路也。
为小石。
陆绩曰：艮刚卦之小，故为小石者也。
为门阙。
虞翻曰：艮为门，艮阳在门外，故为门阙，两小山阙之象也。
为果蓏。
宋衷曰：木实谓之果，草实谓之蓏；桃李瓜瓞之属皆出山谷也。
为阍寺。
宋衷曰：阍人主门，寺人主巷；艮为止，此职皆掌禁止者也。
为指。
虞翻曰：艮手多节，故为指。
为拘。
虞翻曰：指屈伸制物，故为拘。拘旧作狗，上已为狗字之误。
为鼠。
虞翻曰：似狗而小，在坎穴中，故鼠，晋九四是也。
为黔喙之属。
马融曰：黔喙，肉食之兽，谓豺狼之属；黔，黑也，阳玄在前也。
其于木也为坚多节。
虞翻曰：阳刚在外，故多节，松柏之属。

兑为泽。
虞翻曰：坎水半见，故为泽。
宋衷曰：阴在上，令下湿，故为泽也。
为少女。
虞翻曰：坤三索，位在末，故少也。
为巫。
乾为神，兑为通，与神通气，女故为巫。
为口舌。
兑得震声，故为口舌。
为毁折。
二折震足，故为毁折。
为附决。
乾体未圜，故附决也。
其于地也为刚卤。
乾二阳在下，故刚；泽水润下，故咸。
朱仰之曰：取金之刚不生也，刚卤之地不生物，故为刚卤者也。
为妾。
三少女位贱，故为妾。
为羔。
羔、女使皆取位贱。旧读以震駹为龙、艮拘为狗、兑羔为羊，皆已见上，此为再出，非孔子意也。震已为长男，又言长子，谓以当继世、守宗庙、主祭祀，故详举之；三女皆言长、中、少，明女子各当外成，故别见之。此其大例者也。此上虞义。
""", chatGptTranslation = """**ChatGPT 翻译（逐段工作译文）**

**离象征雉鸟。**

孔颖达解释：离象征文明，雉鸟羽毛有文采，所以离象征雉。

**艮象征狗。**

《九家易》解释：艮主停止，也主守卫防御。艮的数为三、七、九、六、十三；三主斗宿，斗宿与狗相应，所以狗怀胎三个月而生。斗宿运行到十三时，太阳升起，所以狗到十三日睁眼。斗宿屈曲，所以狗卧下时也蜷曲。斗宿运行四市，狗也会夜间绕屋巡行。狗的精气畏水，不敢直接饮水，只用舌头舔水。狗争斗时用水浇它便会停止。狗与奎星相近，所以狗在道路上发情而不避人。

**兑象征羊。**

孔颖达解释：兑有喜悦、顺从之义，羊是顺从的牲畜，所以兑象征羊。

**乾象征头部。**

乾尊贵而居于上方，所以象征头部。

**坤象征腹部。**

坤能够包容、收藏，所以象征腹部。

**震象征脚。**

震具有发动、运动的作用，所以象征脚。

**巽象征大腿。**

巽象征顺从，大腿顺随脚的动作，所以巽象征大腿。

**坎象征耳朵。**

坎处在北方，主听觉，所以象征耳朵。

**离象征眼睛。**

离处在南方，主视觉，所以象征眼睛。

**艮象征手。**

艮象征停止，手也能够握持物品，使物体停止不动，所以艮象征手。

**兑象征口。**

兑象征言说，口是用来言说的，所以兑象征口。以上是孔颖达《正义》的解释。

**乾是天，所以称为父；坤是地，所以称为母。**

崔憬解释：这里要说明六子，因此先说乾称为天父，坤称为地母。

**震经过一次求取而得到男孩，所以称为长男；巽经过一次求取而得到女孩，所以称为长女；坎经过两次求取而得到男孩，所以称为中男；离经过两次求取而得到女孩，所以称为中女；艮经过三次求取而得到男孩，所以称为少男；兑经过三次求取而得到女孩，所以称为少女。**

孔颖达解释：“索”是求取。以乾坤为父母，求取它们所生的孩子。得到父气的为男，得到母气的为女。坤初爻求得乾气而成为震，所以称长男；坤二爻得到乾气而成为坎，所以称中男；坤三爻得到乾气而成为艮，所以称少男。乾初爻得到坤气而成为巽，所以称长女；乾二爻得到坤气而成为离，所以称中女；乾三爻得到坤气而成为兑，所以称少女。这说明六子的生成方式。

**乾象征天。**

宋衷解释：乾的运动不停止，天也不断运行。

**乾象征圆。**

宋衷解释：运动和运行不能不是圆形，所以乾象征圆。

**乾象征君主。**

虞翻解释：因为尊贵而庄严。

**乾象征父亲。**

虞翻解释：乾生成三个男卦，取其同类而强大，所以象征父亲。

**乾象征玉，也象征金。**

崔憬解释：天体清明而刚健，所以象征玉和金。

**乾象征寒冷，也象征冰。**

孔颖达解释：取西北方寒冷结冰之地为象。崔憬解释：乾主立冬以后、冬至以前，所以象征寒冷和冰。

**乾象征大红色。**

虞翻解释：太阳为红色，月望时日月出入也呈现这一象。崔憬解释：乾对应四月纯阳之卦，所以取阳气盛大的颜色，象征大红。

**乾象征良马。**

虞翻解释：乾有善美之义，所以象征良马。

**乾象征老马。**

《九家易》解释：这是说气势衰退。阳息发展到巳位之后必然又开始消退，所以象征老马。

**乾象征瘦马。**

崔憬解释：骨属阳，肉属阴；乾是纯阳爻，骨多肉少，所以象征瘦马。

**乾象征杂色马。**

宋衷解释：天有五行之色，所以象征杂色马。

**乾象征树木的果实。**

宋衷解释：群星布列于天，好像果实附着在树木上一样。

**坤象征地。**

虞翻解释：坤体现柔顺、静止之道。

**坤象征母亲。**

虞翻解释：坤生成三个女卦，能够养育，所以象征母亲。

**坤象征布。**

崔憬解释：坤普遍布散万物，使万物得到养育，所以象征布。

**坤象征锅釜。**

孔颖达解释：取其能够使食物变化、成熟，所以象征锅釜。

**坤象征吝啬。**

孔颖达解释：地生养万物，却不会随意转移，所以象征吝啬。

**坤象征均平。**

崔憬解释：地生养万物，不选择善恶，所以象征均平。

**坤象征母牛。**

《九家易》解释：土地能够生育，牛也能够含养，所以象征母牛。

**坤象征大车。**

孔颖达解释：取其能够承载，所以象征大车。

**坤象征纹理。**

《九家易》解释：万物彼此交杂，所以形成纹理。

**坤象征众多。**

虞翻解释：三物称为群；阴象征百姓，三阴相随，所以象征众多。

**坤象征把柄。**

崔憬解释：万物依靠地作为根本，所以地象征把柄。

**坤在颜色上象征黑色。**

崔憬解释：坤对应十月之卦，是阴气达到极点的颜色，所以在颜色上象征黑。

**震象征雷。**

虞翻解释：太阳之火得到水便发出声响，所以象征雷。

**震象征青苍色。**

“駹”是青苍色。震位于东方，所以象征青苍。旧本有人读作“龙”，但上文已经有龙象，此处这样读不对。

**震象征玄色与黄色。**

天色玄，地色黄；震是天地交杂之物，所以象征玄黄。

**震象征专一。**

阳爻处在初位，隐伏而静止，尚未显现；触及坤阴，便表现为专一，因此乾处于静止状态。“尃”字，延叔坚解释为专一、大布，并不正确。以上是虞翻的解释。

**震象征大路。**

崔憬解释：春天万物生出，所以象征大路，取其通达、生长之性。

**震象征长子。**

虞翻解释：乾经过一次求取，所以震为长子。

**震象征决断急躁。**

崔憬解释：取其刚爻居下而发动，所以象征决断急躁。

**震象征青皮竹。**

《九家易》解释：“苍筤”是青色。震的阳爻在下，根部生长而坚刚；阴爻交在其中，使外部呈现青色。

**震象征芦苇。**

《九家易》解释：萑苇就是蒹葭。根茎丛生、蔓延相连，类似雷声传播。

**震在马类中象征善于鸣叫。**

虞翻解释：震象征雷，所以象征善鸣。

**震象征白色左后足，也象征足部有白色。**

马左后足为白色称“馵”。震象征左、象征脚，也象征发动；初爻阳刚为白，所以象征足部白色。

**震象征额头有白斑的马。**

“的”是白色，“颡”是额头。震卦的头部在口上，因呈白色，所以称为白额。《诗经》所说“有马白颠”，就是这个意思。以上是虞翻的解释。

**震在庄稼方面象征反向生长。**

宋衷解释：阴在上、阳在下，所以象征反向生长，指枲麻、豆类等带壳而生的植物。

**震发展到极处，象征健壮，也象征繁盛鲜明。**

虞翻解释：震与巽相互迫近，变化发展到第三爻时，下体的象达到终极；再与第四爻构成乾，所以最终象征健壮、繁盛鲜明。巽发展到极处成为躁卦；躁卦则是震雷、巽风无形，因此卦象特别发生变化。

**巽象征树木。**

宋衷解释：阳动阴静，两个阳爻在上运动，一个阴爻在下安静，很像树木的形态。

**巽象征风。**

陆绩解释：风是土气所生，是巽与坤共同产生的；也取其根本静止而末梢运动的形态。

**巽象征长女。**

荀爽解释：柔爻处在初位。

**巽象征绳墨端直。**

翟玄解释：上面两个阳爻共同端正一个阴爻，使它不能歪斜，好比绳墨保持笔直。孔颖达解释：取其号令整齐万物，如同绳墨端直。

**巽象征工匠。**

荀爽解释：用绳墨制作木器，所以象征工匠。虞翻解释：巽象征接近利益、交易获利三倍，所以象征工匠。子夏解释：工匠居住在店肆之中。

**巽象征白色。**

虞翻解释：乾阳处在上方，所以象征白。孔颖达解释：取风吹去尘埃、使物体洁白的意思。

**巽象征长。**

崔憬解释：取风运行得远，所以象征长。

**巽象征高。**

虞翻解释：乾阳在上而且高长，所以象征高。孔颖达解释：取树木生长向上的形态。

**巽象征进退。**

虞翻解释：阳爻处于初位而退，所以有进退之象。荀爽解释：风运行没有固定方向，所以象征进退。

**巽象征不能果断。**

荀爽解释：风有时向东、有时向西，所以不能确定。

**巽象征气味。**

虞翻解释：“臭”就是气。风到来便能感知气味；巽二爻进入艮，艮象征鼻子，所以巽象征气味。《系辞》说“其臭如兰”。

**巽在人身上象征头发散开。**

虞翻解释：巽象征白，所以头发散开。马融把“宣”解释为头发稀少，是不对的。

**巽象征宽额头。**

变化到第三爻形成坤，坤象征宽广；第四爻发动形成乾，乾象征额头，处在头部口上方，所以象征宽额头。这与震的白额之象相同；震只有一个阳爻，所以象征白额；巽变化为乾后有两个阳爻，所以象征宽额。

**巽象征眼白较多。**

巽象征白，离象征眼睛；眼睛向上看时，眼白显露，所以象征眼白较多。

**巽象征接近利益，交易获利三倍。**

变化到第三爻形成坤，坤象征接近；第四爻发动形成乾，乾象征利益；到第五爻形成噬嗑，噬嗑象征市场；乾有三爻，所以称三倍。上爻发动形成震，因此最终成为躁卦。八卦的这些取象中，只有震、巽会发生这种变化。

**巽发展到极处成为躁卦。**

变化到第五爻形成噬嗑，象征市场；上爻发动形成震，所以最终成为躁卦。这说明震的内体象征专一，外体象征躁动。以上是虞翻的解释。

**坎象征水。**

宋衷解释：坎阳在中间，内部光明，好像水的形态。

**坎象征沟渠。**

虞翻解释：阳爻开辟坤阴，水性流通，所以象征沟渠。

**坎象征隐伏。**

虞翻解释：阳爻藏在坤中，所以象征隐伏。

**坎象征矫正与揉曲。**

宋衷解释：把弯曲的东西弄直称矫，把笔直的东西弄弯称揉；水流有曲有直，所以象征矫揉。

**坎象征弓与车轮。**

虞翻解释：因为坎有矫揉之象，所以象征弓和车轮。坎又象征月亮，月亮在庚位呈弓形，在甲位呈车轮形，所以有弓轮之象。

**坎在人身上象征忧愁增加。**

两个阴爻使心受损，容易产生过失，所以忧愁增加。

**坎象征心病。**

因为劳累而忧愁增加，所以象征心病；坎本身也象征心，坎二爻折损坤体，因此象征心病。以上是虞翻的解释。

**坎象征耳痛。**

孔颖达解释：坎是劳卦，又主听觉；听觉劳损便会耳痛。

**坎象征血，也象征红色。**

孔颖达解释：人有血，好比地有水；血的颜色是红色。按：十一月一阳爻在坎中生出，阳气刚从黄泉中产生，其颜色为红。

**坎在马类中象征脊背优美。**

宋衷解释：阳爻处在中央，象征马的脊背。

**坎象征心思急切。**

崔憬解释：取其内部阳刚发动，所以象征急切。

**坎象征头部向下。**

荀爽解释：水流向下，所以头部低垂。

**坎象征蹄子薄弱。**

《九家易》解释：薄蹄，是因为水在下方又趋向下；趋下便流散，流散便薄弱，所以象征薄蹄。

**坎象征拖曳。**

宋衷解释：水摩擦地面而流动，所以象征拖曳。

**坎在车舆方面象征多有损坏。**

虞翻解释：“眚”是损坏。坤象征大车，坎折损坤体，所以车容易损坏。

**坎象征通达。**

水流经沟渠，所以能够通达。

**坎象征月亮。**

坤象征夜晚，坎阳之光照在坤上，所以象征月亮。

**坎象征盗贼。**

水在暗处潜行，好像偷偷行动，所以象征盗贼。

**坎在树木方面象征坚硬而木心较多。**

阳刚处在中间，所以坚硬而多心，指棘树、枣树一类。以上是虞翻的解释。孔颖达补充：乾、震、坎都用马作比喻。乾最为刚健，震最为发动，坎最为行进，所以都可以用马作比喻。坤柔顺，艮停止，巽也柔顺，离文明而柔顺，兑柔和喜悦，都没有刚健之性，所以不以马为比喻。只有坤卦“利牝马之贞”，取的是它能够行走，而不是刚健；坎也取其行走而不是刚健。它们外表柔顺，所以又象征头低、蹄薄、拖曳。

**离象征火。**

崔憬解释：取卦中阳爻在外，象征火光向外照耀。

**离象征太阳。**

荀爽解释：阳气在外发光。

**离象征闪电。**

郑玄解释：取火的光明。持续的光明像太阳，短暂的光明像闪电。

**离象征中女。**

荀爽解释：柔爻处在中间。

**离象征铠甲与头盔。**

虞翻解释：外部刚强，所以象征铠甲。乾象征头，巽绳贯穿铠甲并在头部停止，所以象征头盔；胄就是兜鍪。

**离象征戈矛兵器。**

乾象征金属，离火熔断乾金，经过干燥和冶炼，所以象征戈矛兵器。

**离在人身上象征大腹。**

《象》解释：腹部常常充满，好像怀孕的妇人，所以象征大腹；乾象征大。

**离象征干燥。**

火能够烘烤、使物体干燥，所以象征干燥。

**离象征鳖、蟹、螺、蚌、龟。**

这五类动物都取外部坚硬、内部柔软的特点。

**离在树木方面象征树木上部空缺枯槁。**

虞翻解释：巽木处在离中，卦体形成大过的死亡之象。蛀虫侵食树心便会折断；蛀虫从树木的孔洞进入，所以树木上部枯槁。也有人认为离火烧灼巽木，所以树木上部折断枯槁。宋衷解释：阴爻在内，内部便空；树木中空，上部便会枯槁。

**艮象征山。**

宋衷解释：两个阴爻在下，一个阳爻在上；阴象征土，阳象征木。土堆积在下，树木生长在上，形成山的形象。

**艮象征小路。**

虞翻解释：艮象征山中的小路。震阳在初位时象征大路；艮阳较小，所以象征小路。

**艮象征小石。**

陆绩解释：艮是刚卦中较小的一卦，所以象征小石。

**艮象征门阙。**

虞翻解释：艮象征门；艮阳在门外，所以象征门阙，好像两座小山相对而立。

**艮象征果实和瓜果。**

宋衷解释：树木所结的果实称果，草本所结的果实称蓏；桃、李、瓜、瓞等都生长在山谷。

**艮象征守门人和宫中宦者。**

宋衷解释：阍人主管宫门，寺人主管宫巷；艮象征停止，这些职务都负责禁止和阻止。

**艮象征手指。**

虞翻解释：艮象征手，手指关节多，所以象征手指。

**艮象征拘束。**

虞翻解释：手指能够屈伸并控制物体，所以象征拘束。旧本把“拘”写作“狗”，是前面已经出现的“狗”字误写。

**艮象征老鼠。**

虞翻解释：它像狗却较小，藏在坎穴之中，所以象征老鼠；晋卦九四就是这个例子。

**艮象征黑嘴一类的野兽。**

马融解释：黔喙是食肉兽，指豺狼一类；黔是黑色，阳气玄黑而在前。

**艮在树木方面象征坚硬而多节。**

虞翻解释：阳刚在外，所以象征多节，指松柏一类。

**兑象征沼泽。**

虞翻解释：坎水有一半显露出来，所以象征沼泽。宋衷解释：阴爻在上，使下方潮湿，所以象征沼泽。

**兑象征少女。**

虞翻解释：坤经过三次求取，兑处在最后的位置，所以称少女。

**兑象征巫者。**

乾象征神，兑象征交通、沟通；兑与神相通，又属于女性，所以象征巫者。

**兑象征口舌。**

兑得到震的声音，所以象征口舌。

**兑象征毁坏折断。**

二爻折损震足，所以象征毁坏折断。

**兑象征附着后决断。**

乾体尚未圆满，所以有附着、决断之象。

**兑在地理方面象征坚硬而盐碱的土地。**

乾的两个阳爻在下，所以象征坚硬；沼泽之水润泽向下，所以形成咸卤之地。朱仰之解释：取金属坚硬而不生长的特点；坚硬盐碱的土地不能生长作物，所以象征刚卤。

**兑象征妾。**

三女中少女的位置较低，所以象征妾。

**兑象征羔羊。**

羔羊与女使都取其地位较低。旧本把震的“駹”读作龙、艮的“拘”读作狗、兑的“羔”读作羊，这些形象前面已经出现，此处再次出现，并非孔子本意。震已经象征长男，又说长子，是因为长子承担继承世系、守护宗庙、主持祭祀的责任，所以详细举出；三女分别称长女、中女、少女，是为了说明女子各自出嫁成家，所以分别列出。这就是这些取象的大体规则。以上是虞翻的解释。

译释说明：本条按现有录文顺序翻译各卦取象与注家解释。原文中的术数数字、星宿对应和部分古代生理习俗说法均按注家原意转述，不视为现代科学结论；“駹”等疑字保留底本辨析，不擅自改成确定读法。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十七·序卦传注文（节录）", "底本：文渊阁《四库全书》本《周易集解》卷十七，参校维基文库四库本第625—836行。此条录《序卦》从“有天地然后万物生焉”至“物不可穷也故受之以未济终焉”的注文。为便于分批校核，先录连续大段；仍需逐句核对影印页及异体字。", """
“有天地，然后万物生焉。”
干宝曰：物有先天地而生者矣，今正取始于天地；天地之先，圣人弗之论也，故其所法象必自天地而还。并引《老子》“有物混成，先天地生”、上系“法象莫大乎天地”及《庄子》“六合之外，圣人存而不论”等语申说。

“盈天地之间者唯万物，故受之以屯；屯者，盈也。”
荀爽曰：谓阳动在下，造生万物于冥昧之中也。
“屯者，物之始生也。”
韩康伯曰：屯，刚柔始交，故为万物之始生也。
崔憬曰：此仲尼序文王次卦之意；不序乾坤之次者，以一生二、二生三、三生万物，则天地次第可知，而万物之先后宜序。万物始生，言刚柔始交，故万物资始于乾而资生于坤。

“物生必蒙，故受之以蒙；蒙者，蒙也，物之稚也。”
崔憬曰：万物始生之后，渐以长稚，故言物生必蒙。
郑玄曰：蒙，幼小之貌；齐人谓萌为蒙也。

“物稚不可不养也，故受之以需；需者，饮食之道也。”
荀爽曰：坎在乾上，中有离象，水火交和，故为饮食之道。
郑玄曰：言孩稚不养则不长也。

“饮食必有讼，故受之以讼。”
韩康伯曰：夫有生则有资，有资则争兴也。
郑玄曰：讼犹争也，言饮食之会恒多争也。

“讼必有众起，故受之以师；师者，众也。”
《九家易》曰：坤为众物，坎为众水，上下皆众，故曰师。并释军、师、旅的古代人数与职官制度。
崔憬曰：因争必起相攻，故受之以师也。

“众必有所比，故受之以比。”
韩康伯曰：众起而不比，则争无息，必相亲比而后得宁也。
“比者，比也；比必有所畜，故受之以小畜。”
韩康伯曰：比非大通之道，则各有所畜以相济也。由比而畜，故曰小畜而不能大也。
“物畜然后有礼，故受之以履。”
韩康伯曰：履，礼也。礼所以适时用也，故既畜则须用，有用须礼也。
“履而泰，然后安，故受之以泰；泰者，通也。”
荀爽曰：谓乾来下降，以阳通阴也。
姚信曰：安上治民莫过于礼，有礼然后泰，泰然后安也。

“物不可以终通，故受之以否。”
崔憬曰：物极则反，故不终泰通而否矣，所谓“城复于隍”。
“物不可以终否，故受之以同人。”
韩康伯曰：否则思通，人同志，故可出门同人，不谋而合。
“与人同者，物必归焉，故受之以大有。”
崔憬曰：以欲从人，人必归己，所以成大有。
“有大者不可以盈，故受之以谦。”
崔憬曰：富贵而自遗其咎，故有大者不可盈，当须谦退，天之道也。
“有大而能谦必豫，故受之以豫。”
郑玄曰：言同既大而有谦德，则于政事恬逸；雷出地奋，豫，行出而喜乐之意。
“豫必有随，故受之以随。”
韩康伯曰：顺以动者，众之所随也。
“以喜随人者必有事，故受之以蛊；蛊者，事也。”
《九家易》曰：子行父事，备物致用而天下治也；立成器以为天下利，莫大于圣人。子修圣道，行父之事以临天下，无为而治。

“有事然后可大，故受之以临；临者，大也。”
荀爽曰：阳称大，谓二阳动升，故曰大。
宋衷曰：事立功成，可推而大也。
“物大然后可观，故受之以观。”
虞翻曰：临反成观，二阳在上，故可观也。
崔憬曰：德业大者，可以观政于人也。
“可观而后有所合，故受之以噬嗑；嗑者，合也。”
虞翻曰：颐中有物，食故口合也。
韩康伯曰：可观则异方合会也。
“物不可以苟合而已，故受之以贲；贲者，饰也。”
虞翻曰：分刚上、文柔，故饰。
韩康伯曰：物相合则须饰以修外也。
“致饰然后亨，则尽矣，故受之以剥；剥者，剥也。”
荀爽曰：极饰反素，文章败，故为剥也。
“物不可以终尽，剥穷上反下，故受之以复。”
虞翻曰：阳四月穷上，消姤至坤者也。
崔憬曰：易穷则有变，物极则反于初，故剥之为道不可终尽，而受之以复也。

“复则不妄矣，故受之以无妄。”
崔憬曰：物复其本则为诚实，故言复则无妄矣。
“有无妄然后可畜，故受之以大畜。”
荀爽曰：物不妄者，畜之大也；畜积不败，故大畜也。
“物畜然后可养，故受之以颐；颐者，养也。”
虞翻曰：天地养万物，圣人养贤以及万民。
崔憬曰：大畜刚健辉光日新，则可观其所养，故言物畜然后可养也。
“不养则不可动，故受之以大过。”
虞翻曰：人颐不动则死，故受之以大过；大过，否卦棺椁之象也。
“物不可以终过，故受之以坎；坎者，陷也。”
韩康伯曰：过而不已，则陷没也。
“陷必有所丽，故受之以离；离者，丽也。”
韩康伯曰：物极则变，极陷则反所丽。

“有天地，然后有万物；有万物，然后有男女；有男女，然后有夫妇；有夫妇，然后有父子；有父子，然后有君臣；有君臣，然后有上下；有上下，然后礼义有所错。”
虞翻曰：有天地谓天地否；然后有万物，谓否反成泰，天地氤氲，万物化醇。有万物然后有男女，谓泰已有否，否三上反正成咸，艮为男、兑为女。有男女然后有夫妇，咸反成恒，震为夫、巽为妇。有夫妇然后有父子，谓咸上复乾成遯，乾为父、艮为子。有父子然后有君臣，谓遯三复坤成否，乾为君、坤为臣。有君臣然后有上下，否乾君尊上、坤臣卑下。有上下然后礼义有所错，错，置也；天、君、父、夫象尊错上，地、妇、臣、子礼卑错下。此上虞义。
干宝曰：错，施也。此详言人道三纲六纪有自来也。人有男女阴阳之性，则自然有夫妇配合之道；有夫妇配合之道，则自然有刚柔尊卑之义；阴阳化生，血体相传，则自然有父子之亲；以父立君、以子资臣，则必有君臣之位。有君臣之位，故有上下之序；有上下之序，则必礼以定其体，义以制其宜。明先王制作盖取之于情者也。并论上经始乾坤、下经始咸恒及殷周礼教背景。

“夫妇之道不可以不久也，故受之以恒；恒者，久也。”
郑玄曰：言夫妇当有终身之义，夫妇之道谓咸、恒也。
“物不可以久居其所，故受之以遯；遯者，退也。”
韩康伯曰：夫妇之道以恒为贵，而物之所居不可以不恒，宜与时升降，有时而遯者也。
“物不可以终遯，故受之以大壮。”
韩康伯曰：遯，君也，以远小人；遯而后通，何可终耶？阳盛阴消，君子道胜也。
“物不可以终壮，故受之以晋；晋者，进也。”
崔憬曰：不可以终壮，于阳盛自取触藩，宜柔进而上行，受兹锡马。
“进必有所伤，故受之以明夷；夷者，伤也。”
《九家易》曰：日在坤下，其明伤也；言晋极当降，复入于地，故曰明夷也。
“伤于外者必反于家，故受之以家人。”
虞翻曰：晋时在外，家人在内，故反家人。
韩康伯曰：伤于外者必反诸内矣。
“家道穷必乖，故受之以睽；睽者，乖也。”
韩康伯曰：室家至亲，过在失节。故家人之义唯严与敬；乐胜则流，礼胜则离，家人尚严，其弊必乖者也。
“乖必有难，故受之以蹇；蹇者，难也。”
崔憬曰：二女同居，其志乖而难生，故曰乖必有难也。
“物不可以终难，故受之以解；解者，缓也。”
崔憬曰：蹇终则来硕，吉利见大人，故言不可终难，受之以解。
“缓必有所失，故受之以损。”
崔憬曰：宥罪缓死，失之则侥幸，有损于政刑，故言缓必有所失，受之以损。
“损而不已必益，故受之以益。”
崔憬曰：损终则弗损益之，故言损而不已必益。
“益而不已必决，故受之以夬；夬者，决也。”
韩康伯曰：益而不已则盈，故必决也。
“决必有所遇，故受之以姤；姤者，遇也。”
韩康伯曰：以正决邪，必有喜遇。
“物相遇而后聚，故受之以萃；萃者，聚也。”
崔憬曰：天地相遇，品物咸章，故言物相遇而后聚。
“聚而上者谓之升，故受之以升。”
崔憬曰：用大牲而致孝享，故顺天命而升为王矣。
“升而不已必困，故受之以困。”
崔憬曰：冥升在上，以消不富则穷。
“困乎上者必反下，故受之以井。”
崔憬曰：困及于臲卼，则反下以求安。
“井道不可不革，故受之以革。”
韩康伯曰：井久则浊秽，宜革易其故。
“革物者莫若鼎，故受之以鼎。”
韩康伯曰：革去故，鼎取新；既以去故，则宜制器立法以治新也。鼎所以和齐生物、成新之器也。
“主器者莫若长子，故受之以震；震者，动也。”
崔憬曰：鼎所烹饪，享于上帝；主此器者莫若冢嫡以为其祭主也。
“物不可以终动，止之，故受之以艮；艮者，止也。”
崔憬曰：震极则征凶，婚媾有言，当须止之。
“物不可以终止，故受之以渐；渐者，进也。”
虞翻曰：否三进之四，巽为进也。
“进必有所归，故受之以归妹。”
虞翻曰：震嫁兑，兑为妹，嫁归也。
“得其所归者必大，故受之以丰；丰者，大也。”
崔憬曰：归妹者，姪娣媵国，三人九女为大援。
“穷大者必失其居，故受之以旅。”
崔憬曰：谚云作者不居，况穷大甚而能处乎？故必获罪去邦，羁旅于外也。
“旅而无所容，故受之以巽；巽者，入也。”
韩康伯曰：旅而无所容，以巽则得所入也。
“入而后说之，故受之以兑；兑者，说也。”
虞翻曰：兑为讲习，故“学而时习之，不亦说乎”。
“说而后散之，故受之以涣；涣者，离也。”
虞翻曰：风以散物，故离也。
“物不可以终离，故受之以节。”
韩康伯曰：夫事有其节，则物之所同守而不散越也。
“节而信之，故受之以中孚。”
韩康伯曰：孚，信也；既已有节，宜信以守之矣。
“有其信者必行之，故受之以小过。”
韩康伯曰：守其信者，则失贞而不谅之道，而以信为过也，故曰小过。
“有过物者必济，故受之以既济。”
韩康伯曰：行过乎恭、礼过乎俭，可以矫世励俗，有所济也。
“物不可穷也，故受之以未济终焉。”
韩康伯曰：有为而能济者，以已穷物；物穷则乖，功极则乱，其可济乎？故受之以未济。
"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十七·序卦传注文（上篇连续原文补录）", "底本：景印文渊阁《四库全书》本《周易集解》卷十七，参校维基文库四库本第 625—722 行。按底本次序连续录入《序卦传》前段注文，从“有天地然后万物生焉”至“陷必有所丽，故受之以离”。此条用于替换此前节录中概述、压缩的注文；个别字形和断句仍须对照影印页复核。", """
周易序卦

有天地，然后万物生焉。
干宝曰：物有先天地而生者矣，今正取始于天地；天地之先，圣人弗之论也，故其所法象必自天地而还。○《老子》曰：“有物混成，先天地生，吾不知其名，强字之曰道。”《上系》曰：“法象莫大乎天地。”○《庄子》曰：“六合之外，圣人存而不论。”《春秋谷梁传》曰：“不求知所不可知者，智也。”而今后世浮华之学，强支离道义之门，求入虚诞之域，以伤政害民，岂非谗说殄行，大舜之所疾者也。

盈天地之间者唯万物，故受之以屯。屯者，盈也。
荀爽曰：谓阳动在下，造生万物于冥昧之中也。

屯者，物之始生也。
韩康伯曰：屯，刚柔始交，故为万物之始生也。
崔憬曰：此仲尼序文王次卦之意。不序乾坤之次者，以一生二、二生三、三生万物，则天地次第可知，而万物之先后宜序也。万物之始生者，言刚柔始交，故万物资始于乾而资生于坤也。

物生必蒙，故受之以蒙。蒙者，蒙也，物之穉也。
崔憬曰：万物始生之后，渐以长穉，故言物生必蒙。
郑玄曰：蒙，幼小之貌；齐人谓萌为蒙也。

物穉不可不养也，故受之以需。需者，饮食之道也。
荀爽曰：坎在乾上，中有离象，水火交和，故为饮食之道。
郑玄曰：言孩穉不养则不长也。

饮食必有讼，故受之以讼。
韩康伯曰：夫有生则有资，有资则争兴也。
郑玄曰：讼犹争也，言饮食之会恒多争也。

讼必有众起，故受之以师。师者，众也。
《九家易》曰：坤为众物，坎为众水，上下皆众，故曰师也。凡制军万有二千五百人为军，天子六军，大国三军，次国二军，小国一军；军有将，皆命卿也。二千五百人为师，师帅皆中大夫；五百人为旅，旅帅皆下大夫也。
崔憬曰：因争必起相攻，故受之以师也。

众必有所比，故受之以比。
韩康伯曰：众起而不比，则争无息，必相亲比而后得宁也。

比者，比也。比必有所畜，故受之以小畜。
韩康伯曰：比非大通之道，则各有所畜以相济也。由比而畜，故曰小畜而不能大也。

物畜然后有礼，故受之以履。
韩康伯曰：履，礼也。礼所以适时用也，故既畜则须用，有用须礼也。

履而泰，然后安，故受之以泰。泰者，通也。
荀爽曰：谓乾来下降，以阳通阴也。
姚信曰：安上治民莫过于礼，有礼然后泰，泰然后安也。

物不可以终通，故受之以否。
崔憬曰：物极则反，故不终泰通而否矣，所谓“城复于隍”。

物不可以终否，故受之以同人。
韩康伯曰：否则思通，人同志，故可出门同人，不谋而合。

与人同者，物必归焉，故受之以大有。
崔憬曰：以欲从人，人必归己，所以成大有。

有大者不可以盈，故受之以谦。
崔憬曰：富贵而自遗其咎，故有大者不可盈，当须谦退，天之道也。

有大而能谦必豫，故受之以豫。
郑玄曰：言同既大而有谦德，则于政事恬逸。雷出地奋，豫，行出而喜乐之意。

豫必有随，故受之以随。
韩康伯曰：顺以动者，众之所随也。

以喜随人者必有事，故受之以蛊。蛊者，事也。
《九家易》曰：子行父事，备物致用而天下治也。备物致用，立成器以为天下利，莫大于圣人。子修圣道，行父之事以临天下，无为而治。

有事然后可大，故受之以临。临者，大也。
荀爽曰：阳称大，谓二阳动升，故曰大也。
宋衷曰：事立功成，可推而大也。

物大然后可观，故受之以观。
虞翻曰：临反成观，二阳在上，故可观也。
崔憬曰：言德业大者，可以观政于人也。

可观而后有所合，故受之以噬嗑。嗑者，合也。
虞翻曰：颐中有物，食故口合也。
韩康伯曰：可观则异方合会也。

物不可以苟合而已，故受之以贲。贲者，饰也。
虞翻曰：分刚上，文柔，故饰。
韩康伯曰：物相合则须饰以修外也。

致饰而后亨，则尽矣，故受之以剥。剥者，剥也。
荀爽曰：极饰反素，文章败，故为剥也。

物不可以终尽，剥穷上反下，故受之以复。
虞翻曰：阳四月穷上，消姤至坤者也。
崔憬曰：夫易穷则有变，物极则反于初，故剥之为道不可终尽，而受之以复也。

复则不妄矣，故受之以无妄。
崔憬曰：物复其本则为诚实，故言复则无妄矣。

有无妄然后可畜，故受之以大畜。
荀爽曰：物不妄者，畜之大也；畜积不败，故大畜也。

物畜然后可养，故受之以颐。颐者，养也。
虞翻曰：天地养万物，圣人养贤以及万民。
崔憬曰：大畜刚健辉光日新，则可观其所养，故言物畜然后可养也。

不养则不可动，故受之以大过。
虞翻曰：人颐不动则死，故受之以大过。大过，否卦棺椁之象也。

物不可以终过，故受之以坎。坎者，陷也。
韩康伯曰：过而不已，则陷没也。

陷必有所丽，故受之以离。离者，丽也。
韩康伯曰：物极则变，极陷则反所丽。
""",
        chatGptTranslation = """ChatGPT 翻译（逐段工作译文）

干宝解释“有天地，然后万物生焉”说：确实有早于天地而存在的事物，但《序卦》这里选择从天地开始论述；至于天地以前的情形，圣人不作讨论。因此，所取法的对象从天地开始。干宝援引《老子》“有物混成，先天地生”、系辞“法象莫大乎天地”以及《庄子》“六合之外，圣人存而不论”等语，说明圣人有意不谈不可确知的天地之前。对于后世浮华空疏、强行支离道义而陷入虚诞的学说，他认为这会伤害政事、百姓，正是大舜所厌恶的邪说恶行。

荀爽说，天地之间充满万物，所以接着讲屯卦；阳气在下发动，在幽昧之中创造、生成万物。韩康伯说，屯是刚柔开始交合，所以象征万物开始出生。崔憬认为，这是孔子排列文王卦序的用意：不先叙乾坤的次序，是因为一生二、二生三、三生万物，天地的先后关系已经可以推知；《序卦》真正要排列的是万物出现的先后。万物始生，是刚柔初次交合；万物从乾获得创始之气，从坤获得生养之气。

崔憬说，万物出生后逐渐幼稚成长，所以接着是蒙卦。郑玄解释“蒙”为幼小的样子，齐地方言把“萌”称作“蒙”。荀爽说，需卦坎在乾上，卦中含有离象，水火交和，所以需象征饮食之道；郑玄说，幼小的孩子若得不到养育便不能长大。韩康伯说，生命需要资养，有了资养便会发生争夺；郑玄也说，饮食聚会往往容易发生争执，因此讼卦承接需卦。

九家易以坤为众物、坎为众水，解释师卦为众；又列举古代军、师、旅的编制人数和相应职官。崔憬说，争讼必然可能发展为相互攻伐，所以接着是师卦。韩康伯说，众人聚集却不亲比，争端就不会停止，必须彼此亲近依附才能安宁。比卦之后有所积蓄，所以接小畜；韩康伯说，比附并非使天下完全通达的办法，各方仍须有所积蓄、互相补益，因此称小畜。物资积蓄之后才有礼，韩康伯解释履就是礼；礼要适合时宜，既有积蓄并开始使用，就必须有礼节规范。

荀爽说，履而后泰，是乾气下降，以阳气贯通阴气。姚信说，安定上位、治理百姓没有比礼更重要的；有礼才会通泰，通泰之后才安定。崔憬说，事物发展到极点就会反转，因此泰不能永远通达，接着出现否。否也不能永远闭塞；韩康伯说，闭塞时人们便思求通达，志向相同的人可以出门相会，不约而同。崔憬说，愿意顺从他人的人，别人也会归附于他，于是形成大有；但拥有很多不能自满，必须谦退。郑玄说，拥有广大事业而又谦逊，处理政事便能安逸，豫卦又有雷从地中奋起、行动而喜悦之义。

韩康伯说，顺势而动，众人自然跟随，所以豫之后是随。九家易以子承父业、修行圣道解释蛊卦：子继承父亲的事业，备具器物、发挥器用，使天下得到治理；圣人制作器物、使天下获利，正是治理的重要方式。荀爽说，阳爻称“大”，二阳上升，所以临卦为大；宋衷说，事业建立、功绩完成，就能推而广大。虞翻说临卦反转成为观卦，二阳居上，所以可供观瞻；崔憬说德业广大的人，可以让人观察其政绩。

虞翻以颐卦口中有物、咀嚼时口合，解释噬嗑为合；韩康伯说，值得观瞻之后，不同方面便能会合。物不能只求苟且结合，所以接着是贲；虞翻说刚爻居上、柔爻加以文饰，因而形成装饰；韩康伯说事物结合后还须修饰外表。荀爽说装饰到了极点便返归朴素，文采败坏，所以出现剥卦。虞翻说阳气到四月时达到极处，随后逐渐消退至坤；崔憬说变化之道不会永远穷尽，事物到极点就会返回起点，所以剥之后必有复。

崔憬说，复归本根便恢复诚实，所以复之后是无妄。荀爽说不妄的事物能够积蓄而不败坏，所以接大畜。虞翻说天地养育万物，圣人养育贤才并推及百姓；崔憬说大畜刚健、光明而日日更新，因而能够观察其所养，所以大畜之后是颐。虞翻说人若不能进食便无法活动，故颐之后是大过，并以否卦棺椁之象解释大过。韩康伯说，过度而不停便会陷没，所以大过之后是坎；事物发展到极点又会变化，陷入极处便转而有所依附，因此坎之后是离。

译注说明：本条是《序卦传》前半段注文的连续转录稿。各家对卦序的解释并不完全一致，尤其虞翻的卦变、爻位解释属于其自身象数体系；译文按注家分别呈现，不将其合并为经文唯一含义。"""
    ),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十七·序卦传注文（下篇连续原文补录）", "底本：维基文库《周易集解（李鼎祚）/卷十七》页面第 996—1157 行；本条将《序卦传》下篇注文按页面连续转录，不再以概述代替注家原文。数字转录存在异体字、讹字及断句风险，仍须对照文渊阁《四库全书》影印页复核；与现有节录条目内容有重叠时，以本条作为连续原文核对稿，不视为已经完成版本校勘。", """
有天地，
虞翻曰：谓天地否也。

然后有万物。
谓否反成泰，天地壹壶，万物化醇，故“有万物”也。

有万物，然后有男女。
谓泰已有否，否三之上，反正成咸。艮为男，兑为女，故有男女。

有男女，然后有夫妇。
咸反成恒，震为夫，巽为妇，故“有夫妇”也。

有夫妇，然后有父子。
谓咸上复干成遁，干为父，艮为子，故“有父子”。

有父子，然后有君臣。
谓遁三复坤成否，干为君，坤为臣，故“有君臣”也。

有君臣，然后有上下。
否干君尊上，坤臣卑下。天尊地卑，故“有上下”也。

有上下，然后礼义有所错。
错，置也。谓天、君、父、夫象尊，错上；地、妇、臣、子礼卑，错下。坤地道、妻道、臣道，故“礼义有所错”者也。此上虞义。

干宝曰：错，施也。此详言人道、三纲、六纪有自来也。人有男女阴阳之性，则自然有夫妇配合之道。有夫妇配合之道，则自然有刚柔尊卑之义。阴阳化生，血体相传，则自然有父子之亲。以父立君，以子资臣，则必有君臣之位。有君臣之位，故有上下之序。有上下之序，则必礼以定其体，义以制其宜，明先王制作，盖取之于情者也。上经始于乾坤，有生之本也。下经始于咸恒，人道之首也。易之兴也，当殷之末世，有妲己之祸；当周之盛德，有三母之功，以言天不地不生，夫不妇不成，相须之至，王教之端。故《诗》以《关雎》为国风之始，而《易》于咸恒备论，礼义所由生也。

夫妇之道，不可以不久也，故受之以恒。恒者，久也。
郑玄曰：言夫妇当有终身之义，夫妇之道，谓咸恒也。

物不可以终久于其所，故受之以遁。遁者，退也。
韩康伯曰：夫妇之道，以恒为贵。而物之所居，不可以不恒，宜与时升降，有时而遁者也。

物不可以终遁，故受之以大壮。
韩康伯曰：遁，君子以远小人。遁而后通，何可终邪？阳盛阴消，君子道胜也。

物不可以终壮，故受之以晋。晋者，进也。
崔觐曰：不可终壮于阳盛，自取触藩，宜柔进而上行，受兹锡马。

进必有所伤，故受之以明夷。夷者，伤也。
《九家易》曰：日在坤下，其明伤也，言晋极当降复入于地，故曰“明夷”也。

伤于外者必反于家，故受之以家人。
虞翻曰：晋时在外，家人在内，故反家人。
韩康伯曰：伤于外者，必反诸内矣。

家道穷必乖，故受之以睽。睽者，乖也。
韩康伯曰：室家至亲，过在失节。故家人之义，唯严与敬，乐胜则流，礼胜则离。家人尚严，其弊必乖者也。

乖必有难，故受之以蹇。蹇者，难也。
崔觐曰：二女同居，其志乖而难生，故曰“乖必有难”也。

物不可以终难，故受之以解。解者，缓也。
崔觐曰：蹇终则来硕吉，利见大人，故言“不可终难，故受之以解”者也。

缓必有所失，故受之以损。
崔觐曰：宥罪缓死，失之则侥幸，有损于政刑，故言“缓必有所失，受之以损”。

损而不已必益，故受之以益。
崔觐曰：损终则弗损，益之，故言“损而不已必益”。

益而不已必决，故受之以夬。夬者，决也。
韩康伯曰：益而不已则盈，故必“决也”。

决必有遇，故受之以姤。姤者，遇也。
韩康伯曰：以正决邪，必有喜遇。

物相遇而后聚，故受之以萃。萃者，聚也。
崔觐曰：天地相遇，品物咸章，故言“物相遇而后聚”也。

聚而上者谓之升，故受之以升。
崔觐曰：用大牲而致孝享，故顺天命而升为王矣，故言“聚而上者谓之升”。

升而不已必困，故受之以困。
崔觐曰：冥升在上，以消不富，则穷，故言“升而不已必困”也。

困乎上者必反下，故受之以井。
崔觐曰：困及于臲卼，则反下以求安，故言“困乎上必反下”。

井道不可不革，故受之以革。
韩康伯曰：井久则浊秽，宜革易其故。

革物者莫若鼎，故受之以鼎。
韩康伯曰：革，去故。鼎，取新。既以去故，则宜制器立法以治新也。鼎，所以和齐生物成新之器也，故取象焉。

主器者莫若长子，故受之以震。震者，动也。
崔觐曰：鼎所烹饪，享于上帝。主此器者，莫若冢嫡，以为其祭主也，故言“主器者莫若长子”。

物不可以终动，止之，故受之以艮。艮者，止也。
崔觐曰：震极则征凶，婚媾有言，正须止之，故言“物不可以终动”，故“止之”也。

物不可以终止，故受之以渐。渐者，进也。
虞翻曰：否三进之四，巽为进也。

进必有所归，故受之以归妹。
虞翻曰：震嫁兑，兑为妹。嫁，归也。

得其所归者必大，故受之以丰。丰者，大也。
崔觐曰：归妹者，侄、娣、媵国三人，九女为大援，故言“得其所归者必大”也。

穷大者必失其居，故受之以旅。
崔觐曰：谚云：作者不居，况穷不甚，而能处乎？故乎获罪去邦，羁旅于外也。

旅而无所容，故受之以巽。巽者，入也。
韩康伯曰：旅而无所容，以巽则得所入也。

入而后说之，故受之以兑。兑者，说也。
虞翻曰：兑为讲习，故学而进习之，不亦说乎。

说而后散之，故受之以涣。涣者，离也。
虞翻曰：风以散物，故“离”也。

物不可以终离，故受之以节。
韩康伯曰：夫事有其节，则物之所同守，而不散越也。

节而信之，故受之以中孚。
韩康伯曰：孚，信也。既已有节，宜信以守之矣。

有其信者必行之，故受之以小过。
韩康伯曰：守其信者，则失贞而不谅之道，而以信为过也。故曰“小过”。

有过物者必济，故受之以既济。
韩康伯曰：行过乎恭，礼过乎俭，可以矫世励俗，有所济也。

物不可穷也，故受之以未济终焉。
韩康伯曰：有为而能济者，以已穷物。物穷则乖，功极则乱，其可济乎？故“受之以未济”。
""",
        chatGptTranslation = """ChatGPT 翻译（逐段工作译文）

虞翻以否卦解释“有天地”：天地处于否的状态；否卦反转成为泰卦，天地之气交感，万物由此化生，所以说有万物。泰卦中又含有否的变化，否卦第三爻与上爻交换、复归正位而成为咸卦；艮象征男子，兑象征女子，所以有男女。咸卦反转成为恒卦，震象征丈夫，巽象征妻子，所以有夫妇。咸卦上爻复归乾体而成遯卦，乾为父、艮为子，所以有父子。遯卦第三爻复归坤体而成否卦，乾为君、坤为臣，所以有君臣。否卦中乾君尊贵居上，坤臣卑下居下，所以有上下。虞翻把“错”解释为安置：天、君、父、夫等尊位安置在上，地、妇、臣、子等卑位安置在下。干宝则把“错”解释为施行，认为这一段说明人伦纲纪有其自然来源：男女阴阳之性产生夫妇配合，夫妇关系产生刚柔尊卑之义，阴阳化生而有父子亲情，由父立君、由子为臣而有君臣之位；既有君臣，便有上下次序，再以礼确定体制、以义裁定适宜。干宝还比较《周易》上经以乾坤为始、下经以咸恒为始，以及《诗经》以《关雎》为国风之始，认为这些安排都在说明天地、夫妇相互依存的道理。

郑玄说，夫妇应当有终身相守的道义，咸、恒两卦讲的就是夫妇之道。韩康伯说，夫妇之道贵在恒久，但事物不能永远停留在原处，仍须随时势升降，因此恒之后有遯。遯是退避；但也不能永远退避，韩康伯以阳盛阴消、君子之道得胜解释遯之后的大壮。崔憬说，阳气强盛到极点，若仍一味刚壮便会像公羊触藩一样受阻，所以应当以柔和方式前进，这就是晋卦的进。

九家易说，晋卦太阳进入坤地之下，光明受到损伤，所以是明夷；晋发展到极点便下降入地。虞翻说晋时人在外，家人卦则回到内，所以外部受伤之后返回家庭；韩康伯也说，在外受伤的人必然返回内部。韩康伯说，家庭关系最亲密，但若失去节度也会乖离；家人之道贵在严肃和敬重，快乐过度会流于放纵，礼节过度又会造成疏离，因此家人之后是睽。崔憬说，两女同居而志向不合，便会产生困难，所以睽之后是蹇。蹇不能永远困难，崔憬以困难终结后出现吉祥、适宜见大人的卦辞解释解卦；但宽缓也可能造成失误，崔憬举宽宥罪人、延缓刑罚而导致侥幸为例，说明解之后接损。损而不停便会转为增益；增益不止便会盈满而决裂，韩康伯因此解释夬为决断。以正道决除邪恶，韩康伯说便可能遇到值得欣喜的机会，所以夬之后是姤。天地相遇，万物显著繁盛，崔憬据此解释姤之后为萃；聚集而向上发展便是升。崔憬说升卦用大牲祭祀、表达孝敬，顺应天命而上升。上升不止便会困顿；困于上位就返回下方寻求安定，所以困之后是井。

韩康伯说，井使用久了会污浊，应该革除旧弊，因此井之后是革。革除旧物没有比鼎更合适的，鼎去旧取新，能够调和食物、制作新器，所以革之后是鼎。崔憬说，鼎用来烹饪并祭享上帝，主持祭器的人没有比嫡长子更合适的，所以鼎之后是震。震不能永远发动，必须停止，因此接艮；但停止也不能成为永恒状态，所以艮之后是渐。虞翻以否卦第三爻进至第四爻、巽象征进，解释渐为渐进。进必有所归，虞翻说震嫁兑，兑为少女，“归”就是出嫁。归妹得到归宿便能扩大声势，崔憬以古代媵妾制度解释丰卦之“大”。但过分追求广大就会失去居所，崔憬以“作者不居”的俗语说明旅卦的流离处境。

韩康伯说，旅居在外而无处容身，谦逊顺入便能找到可进入之处，所以旅之后是巽。虞翻说兑象征讲习，学习后反复温习便会喜悦，所以巽之后是兑。喜悦之后又要散布，虞翻以风散万物解释涣为离散；但事物不能永远离散，所以接节卦。韩康伯说，事情有节度，众人才能共同遵守而不散乱。既有节度，就应以诚信守持，所以接中孚。韩康伯说，孚就是信；有了节制，便应以诚信坚持。守信的人必然实行其信念，因此接小过；韩康伯认为，守信有时会偏离不偏不倚的正道，因而也可能成为一种“小过”。韩康伯说，行为在某些方面超过常度，反而能够救济时弊，例如恭敬超过常度、礼节超过节俭，便可以矫正风俗，因此接既济。但事物不能穷尽，功业达到极点就可能乖乱，所以最后以未济结束。

译注说明：本条连续转录中有“天地壹壶”等疑似数字转录讹字，译文依上下文解释其大意，但不擅自改动原文；该处必须待影印本复核。虞翻的否泰卦变和六亲配象是特定象数解释，不应误认为《序卦传》经文本身明言的历史过程。"""
    ),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十七·杂卦传注文", "底本：文渊阁《四库全书》本《周易集解》卷十七，参校维基文库四库本第837—946行。录《杂卦》各卦义及注文，主要注家为韩康伯、虞翻、荀爽、干宝；篇末干宝长注已按四库本连续补录；其余各注仍需继续与影印页逐字校勘。个别异文须参校其他数字本，不据单一转录本定本。", """
《周易杂卦》

韩康伯曰：杂卦者，杂糅众卦，错综其义，或以同相类，或以异相明矣。

“乾刚坤柔。”
虞翻曰：乾刚金坚，故刚；坤阴和顺，故柔也。

“比乐师忧。”
虞翻曰：比五得位，建万国，故乐；师三失位，舆尸，故忧。

“临观之意，或与或求。”
荀爽曰：临者，教思无穷，故为与；观者，观民设教，故为求也。

“屯见而不失其居，蒙杂而著。”
虞翻曰：阴出初震，故见；盘桓利居贞，故不失其居。蒙二阳在阴位，故杂；初杂为交，故著。

“震，起也；艮，止也。”
虞翻曰：震阳动行，故起；艮阳终止，故止。

“损益，盛衰之始也。”
虞翻曰：损，泰初益上，衰之始；益，否上益初，盛之始。

“大畜，时也；无妄，灾也。”
虞翻曰：大畜五之复二成临，时舍坤二，故时也；无妄上之遯初，子弑父，故灾者也。

“萃，聚；而升不来也。”
虞翻曰：坤众在内，故聚；升五不来之二，故不来；之内曰来。

“谦，轻；而豫，怠也。”
虞翻曰：谦位三贱，故轻；豫荐乐祖考，故怡怡，或言怠也。

“噬嗑，食也；贲，无色也。”
虞翻曰：颐中有物，故食；贲离日在上，五动巽白，故无色也。

“兑见而巽伏也。”
虞翻曰：兑阳息二，故见，则见龙在田；巽乾初入阴，故伏也。

“随，无故也；蛊，则饰也。”
虞翻曰：否上之初，君子弗用，故无故也；蛊，泰初上饰坤，故则饰也。

“剥，烂也；复，反也。”
虞翻曰：剥生于姤，阳得阴熟，故烂；复，刚反初。

“晋，昼也；明夷，诛也。”
虞翻曰：离日在上，故昼；明入地中，故诛也。
干宝曰：日上中，君道明也；明君在上，罪恶必刑也。

“井，通；而困，相遇也。”
虞翻曰：泰初之五为井，故通也；困三遇四，故相遇也。

“咸，速也；恒，久也。”
虞翻曰：相感者不行而至，故速也；日月久照，四时久成，故久也。

“涣，离也；节，止也。”
虞翻曰：涣散，故离；节制度数，故止。

“解，缓也；蹇，难也。”
虞翻曰：雷动出物，故缓；蹇险在前，故难。

“睽，外也；家人，内也。”
虞翻曰：离女在上，故外；家人女正位乎内，故内者也。

“否泰，反其类也。”
虞翻曰：否反成泰，泰反成否，故反其类；终日乾乾，反复之道。

“大壮则止，遯则退也。”
虞翻曰：大壮止阳，阳故止；遯阴息阳，阳故退；巽为退者也。

“大有，众也；同人，亲也。”
虞翻曰：五阳并应，故众也；夫妇同心，故亲也。

“革，去故也；鼎，取新也。”
虞翻曰：革更故去，鼎烹饪，故取新也。

“小过，过也；中孚，信也。”
虞翻曰：五以阴过阳，故过；信及遯鱼，故信也。

“丰，多故也；亲寡，旅也。”
虞翻曰：丰大故多，旅无容故亲寡。六十四象皆先言卦及道，其指至旅体离四焚弃之行，又在旅家，故独先言亲寡而后言旅。此上虞义。

“离上而坎下也。”
韩康伯曰：火炎上，水润下也。

“小畜，寡也；履，不处也。”
虞翻曰：乾四之坤初成震，一阳在下，故寡也；乾三之坤上成剥，剥穷上失位，故不处。

“需，不进也；讼，不亲也。”
虞翻曰：险在前也，故不进；天水违行，故不亲也。

“大过，颠也。”
虞翻曰：颠，殒也；顶载泽中，故颠也。

“姤，遇也，柔遇刚也。”
虞翻曰：坤遇乾也。

“渐，女归待男行也。”
虞翻曰：兑为女，艮为男；反成归妹，巽成兑，故女归待艮成震乃行，故待男行也。

“颐，养正也。”
虞翻曰：谓养三五，五之正为功；三出坎为圣，故曰颐养正，与蒙以养圣功同义也。

“既济，定也。”
虞翻曰：济成六爻得位，定也。

“归妹，女之终也。”
虞翻曰：归妹，人之终始；女终于嫁，从一而终，故女之终也。

“未济，男之穷也。”
虞翻曰：否艮为男位，否五之二，六爻失正而来下阴；未济主月晦，乾道消灭，故男之穷也。

“夬，决也，刚决柔也，君子道长，小人道忧也。”
虞翻曰：以乾决坤，故刚决柔也。乾为君子，坤为小人；乾息，故君子道长；坤体消灭，故小人道忧。此上虞义。
干宝曰：凡《易》既分为六十四卦，以为上下经，天人之事，各有始终。夫子又为序卦，以明其相承受之义。然则文王、周公所遭遇之运，武王、成王所先后之政，苍精受命短长之期，备于此矣。而夫子又重为杂卦，以易其次第。杂卦之末，又改其例，不以两卦反复相酬者，以示来圣后王，明道非常道，事非常事也。化而裁之存乎变，是以终之以决。言能决断其中，唯阳德之主也。故曰：易穷则变，通则久。总而观之，伏羲、黄帝，皆系世象贤，欲使天下世有常君也。而尧、舜禅代，非黄农之化，朱均顽也。汤武逆取，非唐虞之迹，桀、纣之不君也。伊尹废立，非从顺之节，使太甲思愆也。周公摄政，非汤武之典，成王幼年也。凡此皆圣贤所遭遇异时者也。夏政尚忠，忠之弊野，故殷自野以教敬。敬之弊鬼，故周自鬼以教文。文弊薄，故春秋阅诸三代，而损益之。颜回问为邦，子曰：行夏之时，乘殷之辂，服周之冕。弟子问政者数矣，而夫子不与言古代损益，以非其任也。回则备言王者之佐，伊尹之人也，故夫子及之焉。是以圣人之于天下也，同不是，异不非。百世以俟圣人而不惑，一以贯之矣。
""",
    chatGptTranslation = """ChatGPT 翻译（逐条工作译文）

《周易杂卦》

韩康伯说：“杂卦”就是把众多卦象交错编排、综合起来，或用相同之处归类，或用不同之处相互说明。

“乾刚坤柔。”虞翻说：乾刚健，如金属坚硬，所以称刚；坤属阴，温和顺从，所以称柔。

“比乐师忧。”虞翻说：比卦九五得位，建立万国，所以喜乐；师卦六三失位，出现军队载尸而归的象，所以忧虑。

“临观之意，或与或求。”荀爽说：临卦意味着教化之思没有穷尽，所以是给予；观卦意味着观察百姓并设置教化，所以是求取。

“屯见而不失其居，蒙杂而著。”虞翻说：阴爻从初位出现而成震，所以显现；屯卦有盘桓、利于守正居处之义，所以不失其居。蒙卦的阳爻处在阴位，所以阴阳杂合；初爻交错相杂，所以显著。

“震，起也；艮，止也。”虞翻说：震的阳气发动运行，所以是兴起；艮的阳气运行至终而停止，所以是止。

“损益，盛衰之始也。”虞翻说：损卦是泰卦初爻之阳上益于上位，是衰退的开始；益卦是否卦上爻之阳下益于初位，是兴盛的开始。

“大畜，时也；无妄，灾也。”虞翻说：大畜由五爻变化到复卦二爻而成临，时机舍弃坤二，所以称“时”；无妄上爻变化到遁初，象征儿子弑父，所以称“灾”。

“萃，聚；而升不来也。”虞翻说：坤众聚集在内，所以是聚；升卦五爻不来到二位，所以说不来；从外进入内才称为“来”。

“谦，轻；而豫，怠也。”虞翻说：谦卦三爻位置卑下，所以称轻；豫卦有荐献音乐、祭祀祖先之象，因此呈现怡悦之态，也有人解释为怠惰。

“噬嗑，食也；贲，无色也。”虞翻说：颐卦口中有物，所以象征进食；贲卦离日在上，五爻发动而巽象呈白，所以说无色。

“兑见而巽伏也。”虞翻说：兑的阳气增长到二位，所以显现，如同“见龙在田”；巽是乾初爻进入阴位，所以潜伏。

“随，无故也；蛊，则饰也。”虞翻说：否卦上爻下降到初位，君子不再任用旧有做法，所以说无故；蛊卦由泰初爻上升而修饰坤体，所以是整治修饰。

“剥，烂也；复，反也。”虞翻说：剥从姤卦产生，阳爻受到阴爻浸熟，所以称腐烂；复是刚阳返回初位。

“晋，昼也；明夷，诛也。”虞翻说：离日处在上方，所以象征白昼；光明进入地下，所以称诛。干宝说：太阳升到中天，象征君道光明；明君在上，罪恶必定受到刑罚。

“井，通；而困，相遇也。”虞翻说：泰卦初爻来到五位形成井，所以通达；困卦三爻与四爻相遇，所以称相遇。

“咸，速也；恒，久也。”虞翻说：相互感应的事物，不必移动便能到达，所以迅速；日月长久照耀，四季长久形成，所以是长久。

“涣，离也；节，止也。”虞翻说：涣意味着散开，所以是离散；节制定数量和法度，所以是止限。

“解，缓也；蹇，难也。”虞翻说：雷发动而万物出土，所以是舒缓；蹇卦险阻在前，所以艰难。

“睽，外也；家人，内也。”虞翻说：离女处在上方，所以是外；家人卦女子处于内位，所以是内。

“否泰，反其类也。”虞翻说：否反转成为泰，泰也能反转成为否，所以两者属于相反的类别；这也体现终日乾乾、反复运行的道理。

“大壮则止，遁则退也。”虞翻说：大壮使阳气停止，阳因此止；遁卦阴气增长而阳气消退，所以阳退；巽也有退的含义。

“大有，众也；同人，亲也。”虞翻说：五阳都来响应，所以是众多；夫妇同心，所以是亲近。

“革，去故也；鼎，取新也。”虞翻说：革是更换旧物；鼎用于烹煮，所以象征取得新物。

“小过，过也；中孚，信也。”虞翻说：五爻以阴柔超过阳刚，所以称过；诚信能够达到遁卦中的鱼，所以称信。

“丰，多故也；亲寡，旅也。”虞翻说：丰盛之时事务繁多；旅居在外无处容身，所以亲近的人少。六十四卦的象义通常先说卦及其道理；说到旅卦时，离卦九四有焚弃之象，又处于旅卦之中，所以特别先说亲近者少，再说旅。这以上是虞翻的解释。

“离上而坎下也。”韩康伯说：火性炎热而向上，水性润泽而向下。

“小畜，寡也；履，不处也。”虞翻说：乾四爻来到坤初位形成震，一阳处于下方，所以称少；乾三爻来到坤上位形成剥，剥卦到上爻便失去位置，所以说不处。

“需，不进也；讼，不亲也。”虞翻说：危险在前，所以不前进；天与水背向运行，所以不亲近。

“大过，颠也。”虞翻说：颠就是坠落；栋梁承载泽水，处于倾覆之象，所以称颠。

“姤，遇也，柔遇刚也。”虞翻说：这是坤柔遇到乾刚。

“渐，女归待男行也。”虞翻说：兑象征女子，艮象征男子；渐反转成为归妹，巽变成兑，因此女子出嫁要等待艮变成震，男子行动之后才能成行。

“颐，养正也。”虞翻说：这是说养育三、五两爻；五爻变正便能成就功效，三爻从坎中出来成为圣者，所以称颐为养正，其义与蒙卦以养成圣人功业相同。

“既济，定也。”虞翻说：既济完成后六爻各得其位，所以称为安定。

“归妹，女之终也。”虞翻说：归妹涉及人的终始；女子最终出嫁，从一而终，所以称女子之终。

“未济，男之穷也。”虞翻说：否卦的艮象是男子之位；否卦五爻来到二位，六爻失去正位而下降到阴位。未济以月晦为主，乾道消灭，所以称男子之穷。

“夬，决也，刚决柔也，君子道长，小人道忧也。”虞翻说：乾刚决断坤柔，所以是刚决柔。乾象征君子，坤象征小人；乾气增长，所以君子之道增长；坤体消退，所以小人之道忧惧。这以上是虞翻的解释。

干宝说：全部《易》分为六十四卦，编成上下经，天地与人事各有始终。孔子又作《序卦》，说明各卦相承接的意义；但又作《杂卦》，打乱原有次序。文王、周公所经历的时代，武王、成王施政的先后，苍精受命的长短，都可以在其中看到。孔子再次编作《杂卦》，改变原来的例次；篇末不再按两卦相反相应的常例排列，是为了向后世圣人与君王表明：道并非永远固定，事情也不是永远不变。变化裁制，关键在于顺应变通，因此以夬卦作结，强调能够决断其中关键的，是阳刚之德的主宰者。所以说《易》穷尽便会变化，通达便能长久。

总的来看，伏羲、黄帝的世系都以贤德相承，希望天下世代有恒常的君主。尧舜禅让，却不是黄帝、神农时代的教化方式，因为朱、均顽劣；汤武以征伐取得天下，也不同于唐虞的事迹，因为桀纣不能履行君主之道。伊尹废黜并重新立君，不是顺从常规的礼节，而是为了使太甲反省过失；周公摄政，也不是汤武时代的典制，而是因为成王年幼。这些都是圣贤遭遇不同的时代所采取的不同做法。夏代政治崇尚忠厚，忠厚的弊端是粗野，所以殷代从粗野转向教人恭敬；恭敬的弊端是迷信鬼神，所以周代从鬼神祭祀转向文饰礼制。文饰逐渐衰薄，于是孔子考察夏、商、周三代，斟酌损益。颜回问怎样治理国家，孔子回答要采用夏代历法、乘坐殷代车辆、穿戴周代礼冠。弟子多次向孔子问政，孔子却不向他们谈古代制度的损益，因为他们承担不了这种责任。颜回则具备辅佐王者的条件，像伊尹那样的人，所以孔子才对他谈及此事。因此，圣人面对天下事物，不会一概认同相同之处，也不会一概否定不同之处；即使历经百世等待圣人，也不会迷惑，因为能够用一个根本原则贯通万事。

译注说明：虞翻注文大量使用卦变、爻位与象数术语；此译保留其推演关系，但“阳息”“之”“反”“体”等术语在不同注家体系中含义不完全相同，不能脱离原注独立当作通行义理。"""),
    Entry("历代注疏", "魏王弼、晋韩康伯《周易注》", "注释《周易》上下经及彖、象、文言等；王弼注经，韩康伯补注系辞、说卦、序卦、杂卦。当前先建立原典版本入口，全文离线导入状态：未完成。", """版本与范围：
王弼《周易注》是魏晋易学的重要注本。传世注本中，王弼注经及彖、象、文言；韩康伯补注《系辞传》《说卦传》《序卦传》《杂卦传》。不同合卷本的卷数、篇章编排可能不同，不能把后世合刊卷次误当作原始写作次序。

可核验原典入口：
《周易注疏》（四库全书本，王弼、韩康伯注，陆德明音义，孔颖达疏）：https://zh.wikisource.org/zh-hans/周易正义_(四庫全書本)
《周易略例》王弼撰：https://zh.wikisource.org/wiki/周易略例
《周易略例》四库本及邢璹注：https://zh.wikisource.org/wiki/周易注疏_(四庫全書本)/周易略例

内容状态：
本条是原典书目与版本入口，不是王弼注全文。王弼注逐卦正文、韩康伯补注、陆德明音义均待按卷导入；在导入前不标记为离线全文已收录。""",
    chatGptTranslation = """**ChatGPT 翻译（独立工作译文）**

《周易注》注释《周易》上下经以及《彖传》《象传》《文言》等内容。王弼负责经文及相关传文的注释，韩康伯补充注释《系辞传》《说卦传》《序卦传》《杂卦传》。

**版本与范围**
王弼《周易注》是魏晋时期重要的易学注本。传世合编本中，王弼注经及《彖》《象》《文言》，韩康伯补注其余易传。不同合刊本的卷数和篇章编排可能不同，不能把后世合刊的卷次误认为原始写作次序。

**内容状态**
本条目前是原典书目与版本入口，并非王弼注全文。王弼逐卦注释、韩康伯补注及陆德明音义仍须按卷导入；在完成导入前，不标记为离线全文已收录。""",
    Entry("历代注疏", "《周易正义》", "魏王弼、晋韩康伯注，唐陆德明音义，唐孔颖达疏；四库全书本底本为景印文渊阁四库全书第七册，正文十三卷，附《周易略例》及考证。全文离线导入状态：未完成。", """版本信息：
四库本题作《周易注疏》，作者层次为魏王弼、晋韩康伯注，唐陆德明音义，唐孔颖达疏。其卷首含御制序、提要、经传原目、注解传述人及《正义序》等；正文十三卷，附王弼《周易略例》、考证识语。
通行本与四库本卷数并不完全相同，必须分别建档。

全文与版本入口：
维基文库通行本《周易正义》：https://zh.wikisource.org/zh-hans/周易正義
维基文库四库本《周易注疏》：https://zh.wikisource.org/zh-hans/周易正義_(四庫全書本)
中国哲学书电子化计划馆藏影像入口：https://ctext.org/library.pl?if=gb&res=83810

后续导入结构：
卷首资料；卷一至卷十三；《周易略例》；考证识语。每卷须记录底本、卷次、经传章节范围、数字文本来源及影像页码。
状态声明：目前应用只建立书目与来源入口，尚未把十三卷疏文逐字离线录入。""",
    chatGptTranslation = """**ChatGPT 翻译（独立工作译文）**

**版本信息**
四库本题名为《周易注疏》，注释者包括魏代王弼、晋代韩康伯；陆德明提供音义，唐代孔颖达撰写疏文。四库本卷首包括御制序、提要、经传目录、注解传述人和《正义序》等资料；正文为十三卷，另附王弼《周易略例》和考证文字。

通行本与四库本的卷数并不完全相同，因此必须分别建立版本档案，不能混用卷次。

**全文与版本入口**
本条所列通行本、四库本和馆藏影像入口，是供核验原文的资料来源；并不表示十三卷经注疏已经全部离线录入。

**后续导入结构**
应依次整理卷首资料、卷一至卷十三、《周易略例》和考证识语。每卷均须记录底本、卷次、经传章节范围、数字文本来源及影印页码。

**状态说明**
当前条目建立的是书目和来源入口，尚未把十三卷疏文逐字录入应用。""",
    Entry("历代注疏", "唐李鼎祚《周易集解》", "唐李鼎祚辑，传世十七卷。四库本底本为景印文渊阁四库全书第七册；另有通行本及四库本两种数字文本入口。全文离线导入状态：未完成。", """版本与卷次：
卷首：提要、李鼎祚原序及后序。
卷一：乾。
卷二至卷十二：依次覆盖坤至未济六十四卦。
卷十三至卷十六：系辞上传、下传。
卷十七：说卦、序卦、杂卦。
不同电子版本卷次标题略有差异，应以具体底本页面和影印本为准。

原典入口：
通行本十七卷：https://zh.wikisource.org/zh/周易集解_(李鼎祚)
四库全书本：https://zh.wikisource.org/zh-hans/周易集解_(四庫全書本)
中国哲学书电子化计划四库本馆藏影像：https://ctext.org/library.pl?if=gb&res=6294
品古籍全文检索入口：https://www.pinguji.cn/bookDetail-cca69eb856df4a459fd8d22194590108.html

收录原则：
本书汇集汉魏以来多家易说，注文中引述诸家姓名、注文边界和异说必须保留，不得把李鼎祚编次与被引作者原著混为一谈。当前尚未离线录入十七卷正文，以上是已核验的全文入口及卷目。""",
    chatGptTranslation = """**ChatGPT 翻译（独立工作译文）**

**版本与卷次**
《周易集解》由唐代李鼎祚辑录，传世本共十七卷。四库本依据文渊阁《四库全书》影印本；此外还有通行本和四库本两种数字文本入口。

卷首包括提要、李鼎祚原序及其他序文；卷一为乾卦，卷二至卷十二依次收录其余六十四卦；卷十三至卷十六为《系辞上传》《系辞下传》；卷十七收录《说卦传》《序卦传》《杂卦传》。不同电子版本的卷次标题可能略有差异，应以具体底本页面和影印本为准。

**收录原则**
本书汇集汉魏以来多家易说。整理时必须保留注文中所引作者姓名、注文边界和不同解释，不能把李鼎祚的编次工作与被引作者的原著混为一谈。

**内容状态**
本条目前提供的是原典入口、卷次说明和整理原则，不代表十七卷正文已经全部离线收录。""",
        Entry("历代注疏", "唐李鼎祚《周易集解》卷十二·既济卦集解注文", "底本：景印文渊阁四库全书本《周易集解》卷十二；数字转录对读维基文库《周易集解（李鼎祚）/卷十二》行498—573。范围：既济卦卦辞、彖象及六爻注文，含虞翻、荀爽、侯果、宋衷、干宝、崔觐、卢氏等注家文字。原文按数字底本录入；“妇丧其佛”下保留底本说明“佛，或作茀”，不擅改正文。尚未逐字核对影印页，暂无可靠逐段译文。", """《序卦》曰：有过物者必济，故受之以既济。

韩康伯曰：行过乎俭，可以矫世厉俗，有所济也。

（离下坎上）。既济：亨小。利贞。

虞翻曰：泰五之二。小，谓二也。柔得中。故“亨小”。六爻得位，各正性命，保合大和故“利贞”矣。

初吉，

虞翻曰：初，始也。谓泰干。干知大始，故称“初”。坤五之干二，得正处中，故“初吉，柔得中也。”

终乱。

虞翻曰：泰坤称乱。二上之五，终止于泰，则反成否。子弑其父，臣弑其君天下无邦，终穷成坤，故“乱，其道穷”。

《彖》曰：既济亨，小者亨也。

荀爽曰：天地既交，阳升阴降，故“小者亨也”。

利贞，刚柔正而位当也。

侯果曰：此本泰卦。六五降二，九二升五，是刚柔正当位也。

初吉，柔得中也。

虞翻曰：中，谓二。

终止则乱，其道穷也。

虞翻曰：反否终坤，故“其道穷也”。侯果曰：刚得正，柔得中，故“初吉”也。正有终极，济有息止，止则穷乱，故曰“终止则乱，其道穷也”。一曰：殷亡周兴之卦也。成汤应天，初吉也。商辛毒痛，终止也。由止，故物乱而穷也。物不可穷，穷则复始，周受其未济而兴焉。

《干凿度》曰：既济未济者，所以明戒慎，全王道也。

《象》曰：水在火上，既济。君子以思患而豫防之。

荀爽曰：六爻既正，必当复乱，故君子象之。思患而豫防之，治不忘乱也。

初九：曳其轮，濡其尾，无咎。《象》曰：曳其轮，义无咎也。

宋衷曰：离者两阳一阴。阴方阳圆，舆轮之象也。其一在坎中，以火入水，必败，故曰“曳其轮”也。初在后，称“尾”。尾濡曳，咎也。得正有应，于义要以危而无咎矣。

六二：妇丧其佛，勿逐，七日得。

虞翻曰：离为妇。泰坤为丧。佛发，谓鬒发也。一名妇人之首饰。坎为玄云，故称发。《诗》曰：鬒发如云。干为首，坎为美；五取干二之坤为坎，坎为盗，故“妇丧其佛”。泰震为七，故“勿逐，七日得”。与睽丧马勿逐同义。佛，或作茀。俗说以佛为妇人蔽膝之茀，非也。

《象》曰：七日得，以中道也。

王肃曰：体柔应五，履顺承刚，妇人之义也。佛，首饰。坎为盗。离为妇。丧其茀，邻于盗也。勿逐自得，履中道也。二五相应，故“七日得”也。

九三：高宗伐鬼方，三年克之，小人勿用。

虞翻曰：高宗，殷王武丁。鬼方，国名。干为高宗，坤为鬼方；干二之坤五。故“高宗伐鬼方”。坤为年，位在三，故“三年”。坤为小人，二上克五，故“三年克之，小人勿用”。《象》曰：“惫也”。干宝曰：高宗，殷中兴之君。鬼方，北方国也。高宗尝伐鬼方，三年而后克之。离为戈兵，故称“伐”。坎当北方，故称“鬼”。在既济之家，而述先代之功，以明周因于殷，有所弗革也。

《象》曰：三年克之，惫也。

侯果曰：伐鬼方者，兴衰除暗之征也。上六暗极，九三征之，三举方及，故曰“三年克之”。兴役动众，圣犹疲惫，则非小人能为，故曰“小人勿用”。虞翻曰：坎为劳，故“惫也”。

六四：𦈡有衣袽，终日戒。

虞翻曰：干为衣，故称“𦈡”。袽，败衣也。干二之五，衣象裂坏，故“𦈡有衣袽”。离为日，坎为盗，在两坎间，故“终日戒”。谓伐鬼方三年乃克。旅人勤劳，衣服皆败，鬼方之民，犹或寇窃，故“终日戒”也。

《象》曰：终日戒，有所疑也。

卢氏曰：𦈡者，布帛端末之识也。袽者，残币帛，可拂拭器物也。𦈡有为衣袽之道也。四处明暗之际，贵贱无恒，犹或为衣或为袽也。履多惧之地，上承帝主，故终日戒慎，有所疑惧也。

九五：东邻杀牛，不如西邻之禴祭，实受其福。

虞翻曰：泰震为东，兑为西，坤为牛，震动五杀坤，故“东邻杀牛”。在坎多眚，为阴所乘，故“不如西邻之禴祭”。禴，夏祭也。离为夏。兑动二，体离，明。得正承五，顺三。故“实受其福，吉大来也”。

《象》曰：东邻杀牛，不如西邻之时也。

崔觐曰：居中当位。于既济之时，则当是周受命日也。五坎为月。月出西方，西邻之谓也。二应在离，离为日。日出东方，东邻之谓也。离又为牛，坎水克离火，东邻杀牛之象。禴，殷春祭之名。案《尚书》：克殷之岁，厥四月哉生明。王来自商，至于丰。丁未，祀于周庙。四月殷之三月，春也。则明西邻之禴祭，得其时而受祉福也。实受其福，吉大来也。

卢氏曰：明鬼享德不享味也。故德厚者，吉大来也。

上六：濡其首，厉。

虞翻曰：干为首，五从二上在坎中，故“濡其首，厉”。位极乘阳，故“何可久”。

《象》曰：濡其首厉，何可久也。

荀爽曰：居上濡五，处高居盛，必当复危，故“何可久也”。""", chatGptTranslation = """**ChatGPT 翻译（逐段工作译文）**

**《序卦》说：有超过常态、能够成就事物的力量，事情便会得到完成，所以《既济》卦接在《小过》卦之后。**

韩康伯解释：行为超过通常的节俭程度，能够矫正世风、砥砺习俗，因此有所成就。

**既济：亨通于小事，利于守正。**

虞翻解释：此卦由泰卦变化而来，泰卦九五来到九二的位置。所谓“小”，指九二。柔爻处在中位，所以说小者亨通。六爻都处于适当位置，各自端正其性命，保持并会合天地的大和，所以说利于守正。

**起初吉祥。**

虞翻解释：“初”就是开始，指泰卦的乾体。乾具有创始的功能，所以称为“初”。坤卦六五来到乾卦九二的位置，得到正位并居于中位，因此说“起初吉祥”，这是柔爻得中的缘故。

**最终会发生混乱。**

虞翻解释：泰卦的坤体象征混乱。若二爻上升到五位，泰卦最终停止而反转为否卦。父亲被儿子杀害、君主被臣下杀害，天下失去正常秩序；发展到穷尽便成为坤，所以说混乱是因为道路走到了尽头。

**《彖传》说：既济亨通，是小者也能亨通。**

荀爽解释：天地已经交合，阳气上升、阴气下降，所以说小者也能亨通。

**利于守正，是因为刚柔都端正，且各自居于适当的位置。**

侯果解释：此卦本来是泰卦。六五下降到二位，九二上升到五位，因此刚柔都端正并处在适当的位置。

**起初吉祥，是因为柔爻得中。**

虞翻解释：这里的“中”指六二。

**如果终止不再前进，就会发生混乱；道路也会因此走到尽头。**

虞翻解释：既济反转为否，最终归于坤，所以说道路走到了尽头。侯果解释：刚爻得正、柔爻得中，所以起初吉祥。正道有终点，济成之事也会停息；一旦停滞，就会走向穷困与混乱，所以说终止便会发生混乱，道路也就穷尽了。另一种解释认为，这是殷亡周兴之卦：成汤顺应天命，是起初吉祥；商纣王造成残酷祸害，是终结和停顿。由于停滞，事物便陷入混乱并走向穷尽。但事物不能永远穷尽，穷尽之后便会重新开始；周朝承接殷商未完成的局面而兴起。

**《乾凿度》说：既济与未济两卦，是用来表明戒惧谨慎、保全王道的道理。**

**《象传》说：水处在火上，这就是既济。君子因此思考祸患并预先防备。**

荀爽解释：六爻已经各得其正，必然还会再次发生混乱，所以君子从这一卦的形象中得到启示，思考祸患并预先防备；治理天下时不能忘记可能出现的动乱。

**初九：拖住车轮，尾部沾湿，没有灾咎。《象传》说：拖住车轮，按道理说没有灾咎。**

宋衷解释：离卦由两个阳爻和一个阴爻构成。阴爻呈方形，阳爻呈圆形，可以构成车轮的象征。其中一个位置处在坎水之中，火进入水中必然受损，所以说拖住车轮。初爻处在后面，因此称为“尾”。尾部沾湿，又受到牵曳，本来有灾咎；但初九得正且有应，所以从道理上说，虽然处境危险，仍可没有灾咎。

**六二：妇人失去了头饰，不要追寻，七天后会得到。《象传》说：七天后能够得到，是因为遵循中正之道。**

虞翻解释：离象征妇人，泰卦的坤象征丧失。“佛”指头发，也指浓密的黑发，是妇人的一种头部装饰。坎象征黑色云气，所以这里用来表示头发。《诗经》有“鬒发如云”的说法。乾象征头，坎象征美；五爻取乾卦九二变化为坤而形成坎，坎又象征盗贼，所以说妇人失去了头饰。泰卦的震数为七，所以说不要追赶，七天后会得到；这与睽卦“丧马勿逐，七日得”的意思相同。底本又记“佛”有时写作“茀”。虞翻认为，把它解释成妇人遮蔽膝部的蔽膝，是俗说，不正确。

王肃解释：六二本体柔顺，与九五相应，履行柔顺之道并承接刚爻，符合妇人的礼义。“佛”是头饰。坎象征盗贼，离象征妇人；失去头饰，意味着遭遇盗贼。不要追赶而自然得到，是因为履行中道。二、五相应，所以七天后能够得到。

**九三：高宗征伐鬼方，经过三年才战胜它；小人不可任用。**

虞翻解释：高宗是殷王武丁，鬼方是一个国家。乾象征高宗，坤象征鬼方；乾卦九二变化到坤卦六五的位置，所以说高宗征伐鬼方。坤象征年，三爻的位置对应三年，所以说三年。坤又象征小人，二爻上升而克制五爻，所以说三年战胜鬼方，并告诫不要任用小人。

干宝解释：高宗是殷朝中兴的君主，鬼方是北方的国家。高宗曾经征伐鬼方，三年后才取胜。离象征兵器，所以说征伐；坎位于北方，所以称鬼方。既济卦借叙述前代功业，说明周朝承继殷商制度，并非所有事情都要革除。

**《象传》说：三年才战胜它，是因为疲惫。**

侯果解释：征伐鬼方，象征兴起衰败、除去昏暗。上六处于昏暗的极点，九三征讨它；三年才得以举兵成功，所以说三年战胜。兴师动众，即使圣人也会疲惫，因此不是小人能够承担的事业。虞翻解释：坎象征劳苦，所以说疲惫。

**六四：衣服有破损，需要用旧布絮补缀；整天保持警戒。**

虞翻解释：乾象征衣服，所以称衣。袽是破旧衣物。乾卦九二变化到五位，衣服的象征因此出现破裂，所以说衣服需要用旧布絮补缀。离象征太阳，坎象征盗贼；六四处在两个坎象之间，所以整天戒备。这里联系到征伐鬼方三年才成功：旅途劳苦，衣服都已破损，鬼方百姓仍可能侵扰偷窃，因此必须整日警戒。

**《象传》说：整天戒备，是因为心中有所疑虑。**

卢氏解释：“𦈡”是布帛边端的标记；“袽”是残破的布帛，可以用来擦拭器物。所谓衣服有袽，是说衣物可能需要修补。六四处在光明与昏暗交界之处，贵贱没有恒定状态，好比有时成为衣服，有时成为破布。它处在多忧惧的位置，上承君主，因此整天戒备，心中有所疑惧。

**九五：东边邻国杀牛祭祀，不如西边邻国举行简单的禴祭；西邻实际上得到福佑。**

虞翻解释：泰卦的震象征东方，兑象征西方，坤象征牛。震发动九五而杀坤牛，所以说东邻杀牛。九五处在坎险且多灾的位置，又受到阴爻乘凌，所以不如西邻的禴祭。禴是夏季的祭祀，离象征夏天。兑发动二爻而形成离，离明亮；二爻得正而承接九五，顺承九三，所以说实际得到福佑，吉祥会大为到来。

**《象传》说：东邻杀牛，不如西邻的祭祀合乎时宜。**

崔觐解释：九五居中而得位。在既济的时势中，这对应周朝接受天命之时。五爻属坎，坎象征月亮；月亮从西方出现，所以称西邻。二爻对应离，离象征太阳；太阳从东方升起，所以称东邻。离又象征牛，坎水克制离火，因此有东邻杀牛的象征。禴是殷代春祭的名称。按《尚书》记载，灭殷之年，四月月初见明，周王从商地返回丰邑，丁未日祭祀周庙。殷历四月相当于周历三月，正是春季。这说明西邻的禴祭合乎时令，所以得到福佑。实际得到福佑，吉祥便会大为到来。

卢氏解释：鬼神享用的是人的德行，而不只是祭品的滋味。因此德行深厚的人，吉祥会大为到来。

**上六：头部被水沾湿，危险。**

虞翻解释：乾象征头。五爻从二爻上升而处在坎水之中，所以说头部沾湿而危险。上六处于极位，又乘凌阳爻，因此不能长久。

**《象传》说：头部被水沾湿而有危险，怎么可能长久呢？**

荀爽解释：上六处在最上方而沾湿九五，身居高位、处在盛极之时，必然再次走向危险，所以说怎么可能长久。

译释与校勘说明：“佛／茀”“𦈡”“袽”“禴”等字词依原录文字和注文分别翻译，不将不同注家的解释合并成单一结论。虞翻等人的卦变、象数推演属于其注释体系，不等同于经文唯一含义。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十二·未济卦集解注文", "底本：景印文渊阁四库全书本《周易集解》卷十二；数字转录对读维基文库《周易集解（李鼎祚）/卷十二》行574—675。范围：未济卦卦辞、彖象及六爻注文，含虞翻、荀爽、侯果、姚信、干宝等注家文字，至卷末“饮酒濡首，亦不知节也”。保留底本“汔，几也”与《说文》“汔，涸也”的不同训释，不作统一改写。尚未逐字核对影印页，暂无可靠逐段译文。", """《序卦》曰：物不可穷也，故受之以未济终焉。

崔觐曰：夫易之为道，穷则变，变则通。而以未济终者，亦物不可穷也。

（坎下离上）。未济：亨。

虞翻曰：否二之五也。柔得中，天地交。故“亨”。济，成也。六爻皆划错，故称未济也。

小狐汔济，

虞翻曰：否艮为小狐。汔，几也。济，济渡。狐济几度，而濡其尾，未出中也。

濡其尾，无攸利。

虞翻曰：艮为尾。狐，兽之长尾者也。尾谓二，在坎水中。故“濡其尾”。失位，故“无攸利，不续终也”。

干宝曰：坎为狐。

《说文》曰：汔，涸也。

案：刚柔失正，故未济也。五居中应刚，故“亨”也。小狐力弱，汔乃可济。水既未涸，而乃济之。故尾濡而无所利也。

《彖》曰：未济亨，柔得中也。

荀爽曰：柔上居五，与阳合同，故“亨”也。

小狐汔济，未出中也。

虞翻曰：谓二未变，在坎中也。

干宝曰：狐，野兽之妖者，以喻禄父。中，谓二也。困而犹处中故也。此以记纣虽亡国，禄父犹得封矣。

濡其尾，无攸利，不续终也。

虞翻曰：否阴消阳，至剥终坤，终止则乱，其道穷也。干五之二，坤杀不行，故“不续终也”。干宝曰：言禄父不能敬奉天命，以续既终之礼，谓叛而被诛也。

虽不当位，刚柔应也。

荀爽曰：虽刚柔相应，而不以正，由未能济也。

干宝曰：六爻皆相应，故微子更得为客也。

《象》曰：火在水上，未济。

侯果曰：火性炎上，水性润下，虽复同体，功不相成，所以未济也。故君子慎辨物宜，居之以道，令其功用相得，则物咸济矣。

君子以慎辨物居方。

虞翻曰：君子，否干也。艮为慎。辨，别也。物谓乾阳物也，坤阴物也；艮为居，坤为方；干别五以居坤二。故“以慎辨物居方”也。

初六：濡其尾，吝。

虞翻曰：应在四，故“濡其尾”。失位，故“吝”。

《象》曰：濡其尾，亦不知极也。

案：四在五后，故称“尾”。极，中也。谓四居坎中，以濡其尾，是不知极也。

九二：曳其轮，贞吉。

姚信曰：坎为曳，为轮，两阴夹阳，轮之象也。二应于五，而隔于四，止则据初，故“曳其轮”。处中而行，故曰“贞吉”。干宝曰：坎为轮。离为牛，牛曳轮。上以承五命，犹东蕃之诸侯，共攻三监，以康周道，故曰“贞吉”也。

《象》曰：九二贞吉，中以行正也。

虞翻曰：谓初已正，二动成震，故“行正”。

六三：未济，征凶，利涉大川。

荀爽曰：未济者，未成也。女在外，男在内，婚姻未成。征上从四则凶。利下从坎，故“利涉大川”矣。

《象》曰：未济征凶，位不当也。

干宝曰：吉凶者，言乎其得失也。禄父反叛，管蔡与乱，兵连三年，诛及骨肉，故曰“未济征凶”。平克四国，以济大难，故曰“利涉大川”。坎也。以六居三，不当其位，犹周公以臣而君，故流言作矣。

九四：贞吉，悔亡。

虞翻曰：动正得位，故吉而悔亡矣。

震用伐鬼方，三年有赏于大邦。

虞翻曰：变之震，体师：坤为鬼方。故“震用伐鬼方”。坤为年，为大邦，阳称赏，四在坤中，体既济离三，故“三年有赏于大邦”。

《象》曰：贞吉悔亡，志行也。

案：坎为志，震为行，四坎变震，故“志行也”。

六五：贞吉，无悔。

虞翻曰：之正则吉，故“贞吉，无悔”。

君子之光，有孚，吉。

虞翻曰：动之干，离为光，故“君子之光”也。孚谓二。二变应已，得有之，故“有孚吉”。坎称孚也。干宝曰：以六居五，周公摄政之象也，故曰“贞吉无悔”。制礼作乐，复子明辟。天下乃明其道，乃信其诚。故“君子之光，有孚，吉”矣。

《象》曰：君子之光，其晖吉也。

虞翻曰：动之正，干为大明，故“其晖吉也”。

上九：有孚于饮酒，无咎。濡其首，有孚失是。

虞翻曰：坎为孚，谓四也。上之四介四，故“有孚”。饮酒流颐中，故“有孚于饮酒”。终变之正，故“无咎”。干为首，五动首在酒中，失位，故“濡其首”矣。孚，信。是，正也。六位失正，故“有孚失是”。谓若殷纣沈湎于酒，以失天下也。

《象》曰：饮酒濡首，亦不知节也。

虞翻曰：节，止也。艮为节，饮酒濡首，故“不知节矣”。""", chatGptTranslation = """**ChatGPT 翻译（逐段工作译文）**

**《序卦》说：事物不能永远穷尽，所以《未济》作为全书最后一卦。**

崔觐解释：《易》的道理是穷尽之后便会变化，变化之后便能通达；而以未济作为终结，也正是因为事物不可能永远停留在穷尽状态。

**未济：亨通。**

虞翻解释：此卦由否卦的九二上升到五位而成。柔爻得中，天地之气相交，所以说亨通。“济”是成就的意思。六爻的位置都交错不正，因此称为未济。

**小狐狸几乎渡过河。**

虞翻解释：否卦的艮象征小狐狸。“汔”是接近、几乎的意思；“济”是渡河。狐狸几乎快要渡过河，却沾湿了尾巴，仍未完全脱离水中。

**尾巴被水沾湿，没有什么利益。**

虞翻解释：艮象征尾巴。狐狸是尾巴很长的兽类。这里的尾巴指九二，它处在坎水之中，所以说尾巴沾湿。九二失位，因此没有利益，也就是不能把事情继续完成。

干宝解释：坎卦象征狐狸。

《说文》解释：“汔”是水干涸的意思。

案语：刚柔都没有处在正确的位置，所以称为未济。六五居中并与刚爻相应，所以说亨通。小狐狸力量弱，几乎可以渡河；但河水还没有干涸，它便急着渡河，因此尾巴被水沾湿，不能得到好处。

**《彖传》说：未济能够亨通，是因为柔爻得中。**

荀爽解释：柔爻上升而居于五位，与阳爻相合，所以亨通。

**小狐狸几乎渡河，却还没有离开水的中央。**

虞翻解释：指九二尚未发生变化，仍处在坎卦之中。

干宝解释：狐狸是带有妖异意味的野兽，这里用来比喻禄父。所谓“中”，指九二。禄父虽然困顿，却仍处在中位。这里是记述商纣王虽然亡国，禄父仍然得到封地。

**尾巴被水沾湿，没有利益，是因为不能把事情继续完成。**

虞翻解释：否卦的阴爻逐渐消去阳爻，发展到剥卦，最终归于坤；终止便会混乱，道路也会穷尽。乾卦九五来到二位，坤所象征的杀伐之事没有继续施行，所以说不能继续完成。

干宝解释：禄父不能敬奉天命，不能延续前代已经完成的礼制，指他反叛而最终被诛。

**虽然爻位不当，但刚柔之间仍然相互应合。**

荀爽解释：虽然刚柔相互呼应，却没有处在正确的位置，因此仍未能完成。

干宝解释：六爻彼此都有应，所以微子最终还能作为宾客受到礼遇。

**《象传》说：火在水上，这就是未济。**

侯果解释：火的性质是向上燃烧，水的性质是向下润泽；二者虽然处在同一卦体之中，作用却不能互相成就，所以称为未济。君子因此应当谨慎辨别事物各自适宜的性质，并让它们处在合适的位置，使其功用彼此协调，这样万物便都能得到成就。

**君子因此谨慎辨别事物，并使它们各安其位。**

虞翻解释：这里的君子指否卦的乾体。艮象征谨慎。“辨”就是分别。所谓事物，指乾所象征的阳性事物和坤所象征的阴性事物；艮象征居处，坤象征方位。乾分别五位的事物，使其处在坤二所代表的位置，因此说谨慎辨别事物、使其各居其方。

**初六：尾巴被水沾湿，有困难。《象传》说：尾巴沾湿，也是因为不知道适当的界限。**

虞翻解释：初六与九四相应，所以说尾巴沾湿；它失位，因此有困难。

案语：九四处在九五之后，所以称为尾。“极”是中位。这里说九四居于坎卦中间，尾巴被水沾湿，说明它不知道适当的界限。

**九二：拖住车轮，守正则吉。**

姚信解释：坎象征拖曳，也象征车轮；两条阴爻夹着一条阳爻，形成车轮的形象。九二与九五相应，却被九四阻隔；停下来便依靠初六，所以说拖住车轮。它居中而行，因此说守正则吉。

干宝解释：坎象征车轮，离象征牛，牛拉动车轮。九二向上承接九五的命令，好比东方诸侯共同讨伐三监，以维护周朝的王道，所以说守正吉祥。

**《象传》说：九二守正而吉，是因为居中并依正道行动。**

虞翻解释：这是说初六已经端正，九二发动后形成震卦，所以说依正道行动。

**六三：尚未完成，贸然前进有凶险；有利于渡过大河。**

荀爽解释：未济就是尚未完成。女子处在外面，男子处在里面，婚姻尚未完成。如果前往上方跟随九四，就会有凶险；向下顺从坎卦，则有利于渡过大河。

**《象传》说：未济而前进有凶险，是因为爻位不恰当。**

干宝解释：吉凶说的是得失。禄父反叛，管叔、蔡叔参与作乱，战争延续三年，祸患甚至牵连骨肉，所以说未济而前进有凶险。平定四国，才完成了重大艰难的事业，所以说有利于渡过大河；这里的大河取坎象。六三以阴爻居阳位，位置不当，好比周公以臣子的身份而承担君主之事，因此才出现流言。

**九四：守正则吉，悔恨消失。**

虞翻解释：九四发动而变得端正，取得适当位置，所以吉祥，悔恨也会消失。

**以震动之势征伐鬼方，经过三年，受到大国的赏赐。**

虞翻解释：九四变化形成震卦，整体又体现师卦。坤象征鬼方，所以说以震动之势征伐鬼方。坤象征年，也象征大国；阳爻象征赏赐。九四处在坤体之中，又体现既济卦离卦的第三爻，所以说三年后受到大国赏赐。

**《象传》说：守正而吉、悔恨消失，是因为志向得以施行。**

案语：坎象征心志，震象征行动。九四从坎变为震，所以说志向得以施行。

**六五：守正则吉，没有悔恨。**

虞翻解释：六五变化到正位便吉祥，所以说守正吉祥、没有悔恨。

**君子的光明，有诚信，吉祥。**

虞翻解释：六五发动而变化为乾，离象征光明，所以说君子的光明。“孚”指九二。九二变化后与六五相应，六五便能够拥有它；坎象征诚信，所以说有诚信而吉祥。

干宝解释：六五以阴爻居于五位，象征周公摄政，所以说守正吉祥而没有悔恨。周公制定礼乐，后来又把政权交还给成王。天下因此看见他的道德，也相信他的诚意，所以说君子的光明具有诚信，因而吉祥。

**《象传》说：君子的光明，其光辉是吉祥的。**

虞翻解释：六五发动而归于正位，乾象征广大的光明，所以说光辉吉祥。

**上九：有诚信而饮酒，没有灾咎；若沾湿头部，虽然有诚信，却失去了正道。**

虞翻解释：坎象征诚信，这里指九四。上九来到九四之间，所以说有诚信。饮酒流入口中，因此说有诚信而饮酒。最终变化到正位，所以没有灾咎。乾象征头，六五发动后，头处在酒中；上九失位，所以说头部被沾湿。“孚”是诚信，“是”是正道。六个爻位都失去正位，因此说虽然有诚信，却失去了正道。这好比殷纣王沉溺于酒，因而失去天下。

**《象传》说：饮酒而头部沾湿，也是因为不知道节制。**

虞翻解释：“节”就是停止、节制。艮象征节止；饮酒到头部都被沾湿的程度，所以说不懂得节制。

译释与校勘说明：“汔”在虞翻与《说文》中有“几乎”和“涸”两种不同训释，译文按各自注文分别呈现。禄父、微子、管蔡、周公等历史指涉属于干宝等注家的解释，不将其直接并入经文。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十三·系辞上传中段补遗（崇德广业至拟议变化）", "底本：景印文渊阁四库全书本《周易集解》卷十三；对读品古籍四库本数字转录第十三卷第395—451行。此段承接“易简之善配至德”，止于“拟议以成其变化”，补录崔憬、虞翻、荀爽、张璠、孔颖达等注文。此前卷十三中段条目止于“易简之善配至德”，后段条目从“鸣鹤在阴”起，本条补上两段之间遗漏的注文区间。异体字及疑似讹字按数字转录保留，尚待影印页核验；暂无可靠逐段译文。", """子曰：易其至矣乎！

崔憬曰：夫言“子曰”，皆是语之别端。此更美易之至极也。

夫易，圣人所以崇德而广业也。

虞翻曰：崇德效乾，广业法坤也。

知崇礼卑，崇效天，卑法地。

虞翻曰：知谓乾，效天；崇礼谓坤，法地卑也。

天地设位，而易行乎其中矣。

虞翻曰：位谓六画之位，乾坤各三爻，故天地设位。易出乾入坤，上下无常，周流六虚，故易行乎其中也。

成性存存，道义之门。

虞翻曰：知终终之，可与存义也。乾为道门，坤为义门。成性谓“成之者性”也。阳在道门，阴在义门，其易之门耶？

圣人有以见天下之赜，而拟诸其形容。

虞翻曰：乾称圣人，谓庖牺也。赜谓初自上议下，称拟形容，谓阴在地成形者也。

象其物宜，是故谓之象。

虞翻曰：物宜谓阳远取诸物，在天成象，故象其物宜。象谓三才八卦在天也，庖牺重为六画也。

圣人有以见天下之动。

虞翻曰：重言圣人，谓文王也。动谓六爻矣。

而观其会通。

荀爽曰：谓三百八十四爻，阴阳动移，各有所会，各有所通。张璠曰：会者阴阳合会，若蒙九二也；通者乾坤交通，既济是也。

以行其典礼，系辞焉以断其吉凶。

孔颖达曰：既观其会通而行其典礼，以定一爻之通变，而有三百八十四；于此爻下系属文辞，以断其吉凶。若会通典礼得则为吉也，若会通典礼失则为凶矣。

是故谓之爻。

孔颖达曰：谓此会通之事而为爻也。爻者效也，效诸物之变通，故上章云“爻者言乎变”也。

言天下之至赜而不可恶也。

虞翻曰：至赜无情，阴阳会通，品物流宕，以乾易坤简之至也。元善之长，故不可恶也。

言天下之至动而不可乱也。

虞翻曰：以阳动阴，万物以生，故不可乱。六二之动直以方，动旧误作“赜”也。

拟之而后言，议之而后动。

虞翻曰：以阳拟坤而成震，震为言，议为后动，故“拟之而后言，议之而后动”。安其身而后动，谓当时也矣。

拟议以成其变化。

虞翻曰：议天成变，拟地成化，天施地生，其益无方也。""", chatGptTranslation = """【ChatGPT译文】
孔子说：“《易》大概已经达到最高境界了吧！”

崔憬解释：“子曰”二字通常表示另起一段谈话；这里是再次赞美《易》所达到的极高境界。

《易》是圣人用来提升德行、扩展事业的依据。

虞翻说：提升德行效法乾卦，扩展事业效法坤卦。

智慧崇高，礼节谦下；崇高效法天，谦下效法地。

虞翻说：智慧指乾，效法天；崇礼指坤，效法地的谦卑。

天地确立了位置，《易》的变化运行于天地之间。

虞翻说：这里的位置指六爻的位置，乾、坤各有三爻，所以说天地设位。《易》从乾出而进入坤，上下变化没有固定常态，周流于六个爻位之间，因此《易》在其中运行。

成就本性而使之不断存续，这就是道与义的门户。

虞翻说：能够善始善终，便可以保存道义。乾是道的门户，坤是义的门户。“成性”就是《系辞》所说“成之者性”。阳处于道门，阴处于义门，这就是《易》的门户。

圣人观察到天下幽深复杂的事理，并据此模拟事物的形貌。

虞翻说：乾象征圣人，这里指伏羲。“赜”是深奥复杂的事理；所谓模拟形貌，是指阴爻在地上形成具体形态。

依照事物各自适宜的情状取象，因此称为“象”。

虞翻说：所谓事物适宜，是说阳从远处取法各种事物，在天上形成天象，所以依据事物的适宜之处取象。“象”指天地人三才与八卦之象；伏羲又把八卦重叠为六画卦。

圣人观察到天下万物的运动变化。

虞翻说：再次说圣人，这里指文王；“动”指六爻的变化。

并观察它们交会、贯通的规律。

荀爽说：指三百八十四爻，阴阳移动变化，各有交会之处，各有贯通之理。张璠说：“会”是阴阳相合，例如蒙卦九二；“通”是乾坤交通，例如既济卦。

圣人据此施行典章礼制，并系上文辞来判断吉凶。

孔颖达说：圣人观察交会贯通的规律后，施行相应的典章礼制，以确定一爻的通变；六十四卦共有三百八十四爻，于是把文辞系在各爻之下，用来判断吉凶。交会贯通与典礼相合便吉，不合便凶。

因此称它为“爻”。

孔颖达说：这些交会贯通的事理体现为爻。爻有仿效之义，效法事物的变化与通达，所以前文说“爻，是说明变化的”。

说明天下最幽深复杂的事理，却不令人厌弃。

虞翻说：最深奥的事理本身没有固定情感，阴阳交会贯通，万物流行变化，体现乾的平易与坤的简约达到极致；元是善的开端，所以不会令人厌恶。

说明天下最剧烈的运动变化，却不会使秩序混乱。

虞翻说：阳发动阴，万物由此生成，所以变化不会混乱。六二的发动正直而方正；此处“动”字旧本误作“赜”。

先模拟事理再发言，先审议再行动。

虞翻说：以阳模拟坤而成震，震象征言语；审议之后才行动，所以说先模拟再发言、先审议再行动。安顿自身之后再行动，是说行动必须合乎时机。

通过模拟和审议来完成变化。

虞翻说：模拟天而成其变，取法地而成其化；天施行生化，地承受而生育，其功用没有固定限度。"""),
Entry("历代注疏", "唐李鼎祚《周易集解》卷十三·系辞上传后段辑注（鸣鹤至负乘）", "底本：景印文渊阁四库全书本《周易集解》卷十三；对读维基文库卷十三及品古籍四库本数字文本。范围：从“鸣鹤在阴，其子和之”至卷末“盗之招也”。经文与李鼎祚所辑虞翻、荀爽、侯果、孔颖达等注文分行录入。卷十三前段已另有条目，本条为后段补录，不单独代表全卷逐字校勘完成。", """鸣鹤在阴，其子和之，我有好爵，吾与尔縻之。
孔颖达曰：上略明拟议而动，故引鸣鹤在阴，取同类相应以证之。此中孚九二爻辞也。

子曰：君子居其室，出其言善，则千里之外应之，况其迩者乎？
虞翻曰：君子谓初也。二变五来应之，艮为居，初在艮内，故居其室；震为出言，讼乾为善，故出言善。此亦成益卦也。
虞翻曰：谓二变则五来应之，体益卦。坤数十，震为百，里十之千里也。外谓震巽同声，同声者相应，故千里之外应之；迩谓坤，坤为顺，二变顺初，故况其迩者乎。

居其室，出其言不善，则千里之外违之，况其迩者乎？
虞翻曰：谓初阳动入阴成坤，坤为不善也。
虞翻曰：谓初变体剥，弑父弑君，二阳肥遁，则坤违之而承于五，故千里之外违之，况其迩者乎。

言出乎身，加乎民；行发乎迩，见乎远。
虞翻曰：震为出、为言，坤为身、为民也。
虞翻曰：震为行，坤为迩，乾为远，兑为见，谓二发应五，则千里之外，故行发迩见远也。

言行，君子之枢机；枢机之发，荣辱之主也。
荀爽曰：艮为门，故曰枢；震为动，故曰机也。
翟玄曰：枢主开闭，机主发动；开闭有明暗，发动有中否，主于荣辱也。
言行，君子之所以动天地也，可不慎乎？
虞翻曰：二已变成益，巽四以风动天，震初以雷动地；中孚十一月雷动地中，艮为慎，故可不慎乎。

同人先号咷而后笑。
侯果曰：同人九五爻辞也。言九五与六二初未好合，故先号咷，而后得同心故笑也。引者喻拟议于事未有不应也。

子曰：君子之道，或出或处，或默或语。
虞翻曰：乾为道，故称君子也。同人反师，震为出、为语，坤为默，巽为处，故或出或处、或默或语也。

二人同心，其利断金。
虞翻曰：二人谓夫妇，师震为夫，巽为妇，坎为心，巽为同；六二震巽俱体师坎，故二人同心。巽为利，乾为金，以离断金，故其利断金，谓夫出妇处、妇默夫语，故同心也。

同心之言，其臭如兰。
虞翻曰：臭，气也；兰，香草。震为言，巽为兰，离日燥之，故其臭如兰也。案：六三互巽，巽为臭也；断金之言良药苦口，故香若兰矣。

初六藉用白茅，无咎。
孔颖达曰：欲求外物来应，必须拟议谨慎，则物来应之，故引大过初六藉用白茅无咎之事，以证谨慎之理也。
子曰：苟错诸地而可矣。
虞翻曰：苟，或错置也。颐坤为地，故苟错诸地；其初难知，阴又失正，故独举初六。
藉之用茅，何咎之有？慎之至也。
虞翻曰：颐为坤为震，故错诸地；今借以茅，故无咎也。
夫茅之为物薄，而用可重也。
虞翻曰：阴道柔贱，故薄也。
虞翻曰：香洁可贵，故可重也。
慎斯术也以往，其无所失矣。
侯果曰：言初六柔而在下，苟能恭慎诚洁，虽置羞于地，神亦享矣。此章明但能重慎卑退，则悔吝无从而生。术，道者也。

劳谦，君子有终，吉。
孔颖达曰：欲求外物之应，非唯谨慎，又须谦以下人，故引谦卦九三爻辞以证之矣。
子曰：劳而不伐，有功而不德，厚之至也。
虞翻曰：坎为劳，五多功；乾为德，德言至以上之贵下居三贱，故劳而不伐，有功而不德。艮为厚，坤为至，故厚之至也。
语以其功下人者也。
虞翻曰：震为语，五多功下居三，故以其功下人者也。
德言盛，礼言恭。
虞翻曰：谦旁通履，乾为盛德，坤为礼；天道亏盈而益谦，三从上来同之盛德，故恭。震为言，故德言盛、礼言恭。
谦也者，致恭以存其位者也。
虞翻曰：坎为劳，故能恭；三得位，故以存其位者也。

亢龙有悔。
孔颖达曰：上既以谦得保安，此明无谦则有悔，故引乾之上九亢龙有悔，证骄亢不谦之义也。
子曰：贵而无位，高而无民，贤人在下位而无辅，是以动而有悔也。
虞翻曰：天尊故贵，以阳居阴故无位。
虞翻曰：在上故高，无阴故无民也。
虞翻曰：乾称贤人，下位谓初也；遁世无闷，故贤人在下位而不忧也。
虞翻曰：谓上无民故无辅，乾盈动倾故有悔。文王居三，纣亢极上，故以为诫也。

不出户庭，无咎。
孔颖达曰：又明拟议之道，非但谦而不骄，又当谨慎周密，故引节初周密之事以明之也。
子曰：乱之所生也，则言语以为阶。
虞翻曰：节本泰卦，坤为乱，震为生、为言语，坤称阶，故乱之所生，则言语为之阶也。
君不密则失臣，臣不密则失身，几事不密则害成，是以君子慎密而不出也。
虞翻曰：泰乾为君，坤为臣、为闭，故称密。乾三之坤五，君臣毁贼，故君不密则失臣；坤五之乾三，坤体毁坏，故臣不密则失身。坤为身也。
虞翻曰：几，初也。谓二已变成坤，坤为事，故几事不密。初利居贞，不密初动则体剥，子弑其父、臣弑其君，故害成。
虞翻曰：君子谓初，二动坤为密，故君子慎密。体屯盘桓利居贞，故不出也。

子曰：作易者其知盗乎？
虞翻曰：为易者谓文王，否上之二成困，三暴慢，以阴乘阳；二变入宫为萃，五之二夺之成解，坎为盗，故为易者其知盗乎。

易曰：负且乘，致寇至。
孔颖达曰：此又明拟议之道，当量身而行，不可以小处大，以贱贪贵，故引解六三爻辞以明之矣。
负也者，小人之事也。
虞翻曰：阴称小人，坤为事，以贱倍贵，违礼悖义，故小人之事也。
乘也者，君子之器也。
虞翻曰：君子谓五，器坤也；坤为大车，故乘君子之器也。
小人而乘君子之器，盗思夺之矣。
虞翻曰：小人谓三，既违礼倍五，复乘其车；五来之二成坎，坎为盗，思夺之矣。为易者知盗乎？此之谓也。
上慢下暴，盗思伐之矣。
虞翻曰：三倍五，上慢乾君而乘其器，下暴于二；二藏于坤，五来寇二，以离戈兵，故称伐之，坎为暴也。
慢藏诲盗，冶容诲淫。
虞翻曰：坎心为诲，坤为藏，兑为见，藏不见故慢藏；三动成乾为冶，坎水为淫。三变藏坤，则五来夺之，故慢藏诲盗、冶容诲淫。
易曰：负且乘，致寇至，盗之招也。
虞翻曰：五来夺三，以离兵伐之，故变寇言戎，以成二恶；二藏坤时，艮手招盗，故盗之招。

校勘说明：本条依维基文库四库本页面断句整理，并参考品古籍同为四库本的数字文本。数字化转录中的“几／幾”“洁／絜”“德／徳”等字形存在规范化差异；未以现代语义擅改。此条为卷十三后段录入，原书卷末止于“盗之招也”。""", chatGptTranslation = """**ChatGPT 翻译（逐段工作译文）**

**孔子说：“《易》的道理大概已经达到最高境界了吧！”**

崔憬解释：凡是以“子曰”（孔子说）开头的文字，都是另起一个论述段落。这里再次赞美《易》所达到的极高境界。

**《易》是圣人用来提升德行、扩展事业的依据。**

虞翻解释：提升德行取法于乾卦，扩展事业取法于坤卦。

**智慧崇高，礼节谦下；崇高效法天，谦下效法地。**

虞翻解释：“智慧”指乾，效法天；“崇礼”指坤，效法地的谦下。

**天地确立了各自的位置，《易》的变化便在天地之间运行。**

虞翻解释：“位”指六爻的位置。乾、坤各有三爻，所以说天地设定了位置。《易》的变化从乾出而进入坤，上下没有固定不变的所在，周流于六个爻位之间，所以说《易》在其中运行。

**成就本性并使它持续保存，是道与义的门径。**

虞翻解释：能够使终结归于终结，才可以保存道义。乾是道的门径，坤是义的门径。“成性”就是《系辞》所说“成之者性”。阳处在道门，阴处在义门，这大概就是《易》的门径吧。

**圣人能够观察天下幽深繁杂的事理，并模拟它们的形貌。**

虞翻解释：乾称为圣人，这里指伏羲。所谓幽深繁杂，是说从上往下考察初爻等位置，并拟定事物的形貌；这里所说的形貌，是阴气在地上形成的形体。

**取象于事物各自适宜的特征，因此称为“象”。**

虞翻解释：所谓事物的适宜特征，是说阳从远处取法各种事物，在天上形成征象，所以称为取象于事物的适宜特征。这里的“象”指天、地、人三才以及八卦在天上的象征；伏羲又把八卦重叠为六画卦。

**圣人能够观察天下的运动变化。**

虞翻解释：这里再次说圣人，指周文王；所谓运动变化，指六爻的变化。

**并观察各种变化如何会合、如何贯通。**

荀爽解释：指三百八十四爻，阴阳往来变动，各自有所会合，也各自有所贯通。张璠解释：“会”是阴阳相合，例如蒙卦九二；“通”是乾坤相交，例如既济卦。

**据此施行典章礼制，并在爻下系上文辞，用来判定吉凶。**

孔颖达解释：圣人观察了变化的会合与贯通之后，便施行相应的典章礼制，以确定每一爻变化所通达的意义。六十四卦共有三百八十四爻，圣人在各爻下面系上文辞，用以判断吉凶。会通与典礼相合便是吉，违背会通与典礼便是凶。

**因此称它为“爻”。**

孔颖达解释：把上述会通变化的事情落实为爻。爻有仿效、表现之意，表现事物的变化与通达，所以前文说“爻，是说明变化的”。

**这说明天下最幽深繁复的事理，并不令人厌恶。**

虞翻解释：最幽深繁复的事理本身没有固定情感；阴阳相互会合贯通，万物因此流动变化，这体现了乾的平易与坤的简约达到极致。元始之善是众善之长，所以并不令人厌恶。

**这说明天下最剧烈的变动，也不会因此陷入混乱。**

虞翻解释：以阳发动阴，万物由此生成，所以变化虽大而不致混乱。这里“六二之动直以方”中的“动”，旧本曾误写为“赜”；原文保留这一校记，不据译文改动底本。

**先模拟事理，然后再发言；先审议事理，然后再行动。**

虞翻解释：以阳爻模拟坤，便形成震卦；震象征言语。审议之后才行动，所以说先模拟再发言、先审议再行动。安定自身之后再行动，是说行动应当合乎当时的时机。

**通过模拟和审议，便能成就变化。**

虞翻解释：审议天道而成就变化，模拟地道而成就化育；天施行其生化，地承接而生养，其增益没有固定限度。

译释说明：本段虞翻多用卦变、爻位和八卦取象解释《系辞》语句。译文尽量保留其推演层次；“乾易坤简”“议天成变”等术语不扩写为现代哲学结论。"""),
        Entry("历代注疏", "唐李鼎祚《周易集解》卷十四·天地数与开物成务补遗", "底本：景印文渊阁四库全书本《周易集解》卷十四；对读品古籍四库本数字转录第235—371行。此前卷十四“大衍蓍策专题”条目录至“子曰易有圣人之道四焉者此之谓也”，后续条目从“是故易有太极”起；本条补入两条之间的天地数配干支五行、蓍龟、开物成务、圣人洗心藏密、闔辟往来及制器利用等材料。网页中数字、干支及个别字形存在识读疑点，按页面可辨内容转录并标记待校，不自行推定；暂无可靠逐段译文。", """天一

水甲

地二

火乙

天三

木丙

地四

金丁

天五

土戊

地六

水己

天七

火庚

地八

木辛

天九

金壬

地十

土癸

此则大衍之数五十有五，蓍龟所从生，圣人以通神明之德，以类万物之情。此上虞翻义也。

子曰：夫易何为而作也？

虞翻曰：夫易何为取天地之数也？

夫易开物成务，冒天下之道，如斯而已者也。

陆绩曰：开物谓庖牺引伸八卦，重以为六十四卦，触长爻策至于万一千五百二十，以当万物之数，故曰开物。圣人观象而制网罟、耒耜之属，以成天下之务，故曰成务也。

虞翻曰：以阳辟坤谓之开物，以阴翕乾谓之成务。冒，触也。触类而长之如此也。

是故圣人以通天下之志。

九家易曰：凡言“是故”者，承上之辞也。谓以动者尚其变，变而通之，以通天下之志也。

以定天下之业。

九家易曰：谓以制器者尚其象也。凡事业之未立，以易道决之，故言以定天下之业。

以断天下之疑。

九家易曰：谓卜筮者尚其占也。占事知来，故定天下之疑。

是故蓍之德圆而神，卦之德方以智。

崔憬曰：蓍之数七七四十九，象阳圆；其为用也，变通不定，因之以知来物，是蓍之德圆而神也。卦之数八八六十四，象阴方；其为用也，爻位有分，因之以藏往知事，是卦之德方以智也。

六爻之义易以贡。

韩康伯曰：贡，告也。六爻之变，易以告吉凶也。

圣人以此洗心。

韩康伯曰：洗濯万物之心者也。

退藏于密。

陆绩曰：受蓍龟之报应，决而藏之于心也。

吉凶与民同患。

虞翻曰：圣人谓庖牺，以蓍神知来，故以洗心。阳动入巽，巽为退伏，坤为闭户，故藏密，谓齐于巽以神明其德。阳吉阴凶，坤为民，故吉凶与民同患，谓作易者其有忧患也。

神以知来，知以藏往。

虞翻曰：乾神知来，坤知藏往。来谓出见，往谓藏密也。

其孰能与于此哉？

虞翻曰：谁乎能为此哉？谓古之聪明睿知之君也。

古之聪明睿知神武而不杀者夫。

虞翻曰：谓大人也。庖牺在乾五，动而之坤，与天地合。聪明在坎则聪，在离则明；神武谓乾，睿知谓坤。乾坤坎离反复不衰，故而不杀者夫。

是以明于天之道，而察于民之故。

虞翻曰：乾五之坤，以离日照天，故明天之道；以坎月照坤，故察民之故。坤为民。

是兴神物，以前民用。

陆绩曰：神物，蓍也。圣人兴蓍以别吉凶，先民而用之，民皆从焉，故曰以前民用也。

圣人以此齐戒。

韩康伯曰：洗心曰齐，防患曰戒。

以神明其德夫。

陆绩曰：圣人以蓍能逆知吉凶，除害就利，清洁其身，故曰以此齐戒也。吉而后行，举不违失其德富盛见称神明，故曰神明其德也。

是故阖户谓之坤。

虞翻曰：阖，闭、翕也。谓从巽之坤，坤柔象夜，故以闭户者也。

辟户谓之乾。

虞翻曰：辟，开也。谓从震之乾，乾刚象昼，故以开户也。

一阖一辟谓之变。

虞翻曰：阳变阖阴，阴变辟阳，刚柔相推而生变化也。

往来不穷谓之通。

荀爽曰：谓一冬一夏，阴阳相变易也。十二消息，阴阳往来无穷已，故通也。

见乃谓之象，形乃谓之器。

荀爽曰：谓日月星辰光见在天而成象也；万物生长在地成形，可以为器用者也。

制而用之谓之法。

荀爽曰：谓观象于天，观形于地，制而用之可以为法。

利用出入，民咸用之，谓之神。

陆绩曰：圣人制器以周民用，用之不遗，故曰“利用出入”也。民皆用之而不知所由来，故谓之神也。""", chatGptTranslation = """【ChatGPT译文】
天数第一，对应水与甲；地数第二，对应火与乙；天数第三，对应木与丙；地数第四，对应金与丁；天数第五，对应土与戊；地数第六，对应水与己；天数第七，对应火与庚；地数第八，对应木与辛；天数第九，对应金与壬；地数第十，对应土与癸。

这就是大衍之数五十五，蓍草与龟甲占卜由此产生。圣人借此通达神明的德性，并类比万物的情状。以上是虞翻的解释。

孔子说：“《易》为什么而作呢？”

虞翻说：“《易》为什么要取用天地之数呢？”

《易》能够开启万物、成就事务，涵盖天下的道理，大致就是如此。

陆绩说：“开启万物”，是指伏羲推演八卦，再重叠为六十四卦，并推演爻策，直到总数一万一千五百二十，以对应万物的数目，所以称为“开物”。圣人观察卦象，制作渔网、耒耜等器具，以成就天下的事务，所以称为“成务”。

虞翻说：阳开辟坤阴，称为“开物”；阴收合乾阳，称为“成务”。“冒”有覆盖、触及之意，意思是触类旁通并由此推衍。

因此，圣人借此通达天下人的心志。

《九家易》说：凡是以“是故”开头的句子，都是承接上文。这里是说，行动者崇尚变化，通过变化而使之通达，从而通晓天下人的心志。

借此确立天下的事业。

《九家易》说：这是说制作器物的人崇尚卦象。事业尚未建立时，可以依据《易》的道理作出决断，所以说借此确立天下的事业。

借此判断天下的疑难。

《九家易》说：这是说占卜者崇尚占断。通过占问事情而预知未来，因此能够判断天下的疑难。

所以，蓍草的德性圆融而神妙，卦的德性方正而有智慧。

崔憬说：蓍策之数取七七四十九，象征阳的圆；它的运用变化灵活、不固定，可以据此预知未来事物，所以说蓍的德性圆而神。卦的数目取八八六十四，象征阴的方；它的运用有爻位区分，可以据此保存并认识既往之事，所以说卦的德性方而智。

六爻的意义，能够通过变化明白告知。

韩康伯说：“贡”就是告知。六爻发生变化，便能借此告知吉凶。

圣人借此洗涤心灵。

韩康伯说：这是洗涤万物之心。

退而把它收藏在幽深隐秘之处。

陆绩说：接受蓍龟所显示的应验，作出判断后，将其收藏于心中。

吉凶祸福都与百姓共同承担。

虞翻说：圣人指伏羲。圣人凭借蓍草的神妙而预知未来，所以能够洗涤心灵。阳爻发动进入巽卦，巽有退伏之象；坤象征闭门，因此说收藏于隐秘之处。阳代表吉，阴代表凶，坤代表百姓，所以说吉凶都与百姓共同忧患；这也说明作《易》的人心怀忧患。

神妙的作用用来预知未来，智慧用来保存过去。

虞翻说：乾的神妙作用在于预知未来，坤的智慧在于保存既往。未来指显现出来的事物，过去指收藏于隐秘之中的事物。

谁能够达到这样的境界呢？

虞翻说：谁能够做到这一点呢？指古代聪明、睿智的君主。

大概只有古代聪明睿智、具有神妙武略却不滥用杀伐的人吧。

虞翻说：这里指大人。伏羲居乾卦九五，发动而趋向坤，与天地相合。聪明表现为处坎则聪、处离则明；神武象征乾，睿智象征坤。乾、坤、坎、离往复运行而不衰，所以说神武而不杀。

因此，圣人明白天道，并考察百姓生活中的实际情状。

虞翻说：乾五变化趋向坤，以离日照耀天空，所以能够明白天道；以坎月照耀坤地，所以能够考察百姓的情状。坤象征百姓。

于是兴起神妙的器物，使百姓能够预先使用。

陆绩说：“神物”指蓍草。圣人兴起蓍草占卜，用来辨别吉凶，使百姓能够预先使用，百姓都遵从它，所以说“以前民用”。

圣人借此斋戒。

韩康伯说：洗涤心灵叫作“斋”，防备祸患叫作“戒”。

从而使自身的德性达到神明的境界。

陆绩说：圣人凭借蓍草预知吉凶、避害趋利，使自身清净，所以称为斋戒。判断为吉之后才行动，举措没有违失，德行因此充实而显著，故称“神明其德”。

因此，闭合门户称为坤。

虞翻说：“阖”就是闭合、收敛。这里指从巽变化为坤；坤柔顺，象征夜晚，所以用闭门来比喻。

开启门户称为乾。

虞翻说：“辟”就是开启。这里指从震变化为乾；乾刚健，象征白昼，所以用开门来比喻。

一闭一开称为变化。

虞翻说：阳变化而闭合为阴，阴变化而开启为阳，刚柔相互推移，由此产生变化。

往来运行而没有穷尽称为通达。

荀爽说：这是说冬去夏来，阴阳相互变化。十二消息卦中，阴阳往来没有终止，所以称为“通”。

显现出来的称为象，形成具体形态的称为器。

荀爽说：日月星辰的光辉显现在天空，形成天象；万物在地上生长而形成具体形态，便可以制作成器具。

加以制作并投入使用，称为法。

荀爽说：观察天象、观察地形，再据此制作和使用器物，就可以成为法则。

器物的功用适宜于各种场合，百姓都能使用它，这称为神妙。

陆绩说：圣人制作器物，以满足百姓各种用途，没有遗漏，所以说“利用出入”。百姓都在使用，却不知道它的由来，因此称为神妙。"""),
Entry("历代注疏", "唐李鼎祚《周易集解》卷十五·系辞下传前段辑注", "底本：景印文渊阁四库全书本《周易集解》卷十五；对读品古籍四库本数字文本。范围：自“八卦成列，象在其中矣”起，录至“易曰：困于石，据于蒺藜，入于其宫，不见其妻，凶”及其释文前段；此为卷十五前段，不是全卷。经文与注文分段，保留虞翻、荀爽、陆绩、九家易、韩康伯、孔颖达等署名。", """八卦成列，象在其中矣。
虞翻曰：象谓三才。成八卦之象，乾坤列东，艮兑列南，震巽列西，坎离在中，故八卦成列则象在其中。天垂象见吉凶，圣人象之，是也。

因而重之，爻在其中矣。
虞翻曰：谓参重三才为六爻，发挥刚柔，则爻在其中。六画称爻，六爻之动，三极之道也。

刚柔相推，变在其中矣。
虞翻曰：谓十二消息，九六相变；刚柔相推而生变化，故变在其中矣。

系辞焉而命之，动在其中矣。
虞翻曰：谓系彖象九六之辞，故动在其中。鼓天下之动者，存乎辞者也。

吉凶悔吝者，生乎动者也。
虞翻曰：动谓爻也。爻者效天下之动者也。爻象动内，吉凶见外；吉凶生而悔吝著，故生乎动也。

刚柔者，立本者也。
虞翻曰：乾刚坤柔，为六子父母。乾天称父，坤地称母；本天亲上，本地亲下，故立本者也。

变通者，趣时者也。
虞翻曰：变通配四时，故趣时者也。

吉凶者，贞胜者也。
虞翻曰：贞，正也；胜，灭也。阳生则吉，阴消则凶者也。

天地之道，贞观者也。
陆绩曰：言天地正，可以观瞻为道也。

日月之道，贞明者也。
荀爽曰：离为日，日中之时正当离位，然后明也。月者坎也，坎正位冲离，冲为十五日；月当日冲，正值坎位，亦大圆明，故曰日月之道贞明者也。言日月正当其位乃大明也。
陆绩曰：言日月正以明照为道矣。

天下之动，贞夫一者也。
虞翻曰：一谓乾元，万物之动各资天一阳气以生，故天下之动贞夫一者也。

夫乾，确然示人易矣。
虞翻曰：阳在初弗用，确然无为，潜龙时也。不易世，不成名，故示人易者也。

夫坤，隤然示人简矣。
虞翻曰：隤，安；简，阅也。坤以简能阅内万物，故示人简者也。

爻也者，效此者也。
虞翻曰：效法之谓坤，谓效三才以为六画。

象也者，像此者也。
虞翻曰：成象之谓乾，谓圣人则天之象，分为三材也。

爻象动乎内，吉凶见乎外。
虞翻曰：内初外上也。阳象动内则吉见外，阴爻动内则凶见外也。

功业见乎变。
荀爽曰：阴阳相变，功业乃成者也。

圣人之情见乎辞。
崔憬曰：言文王作卦爻之辞，所以明圣人之情陈于易象。

天地之大德曰生。
孔颖达曰：自此以下欲明圣人同天地之德、广生万物之意也。言天地之盛德常生万物而不有生，是其大德也。

圣人之大宝曰位。
崔憬曰：言圣人行易之道，当须法天地之大德，宝万乘之天位；谓以道济天下为宝，而不有位，是其大宝也。

何以守位？曰仁。
宋衷曰：守位当得士大夫公侯，有其仁贤，兼济天下。

何以聚人？曰财。
陆绩曰：人非财不聚，故圣人观象制器，备物尽利以业万民而聚之也，盖取聚人之本矣。

理财正辞，禁民为非，曰义。
荀爽曰：尊卑贵贱、衣食有差，谓之理财；名实相应，万事得正，为之正辞；咸得其宜，故谓之义也。
崔憬曰：夫财货人所贪爱，不以义理之则必有败；言辞人之枢要，不以义正之则必有辱；百姓有非，不以义禁之则必不改。此三者皆资于义，以此行之得其宜也。故知仁义与财，圣人宝位之所要也。

古者庖牺之王天下也。
虞翻曰：庖牺，太昊氏，以木德王天下，位乎乾五。五动见离，离生木，故知火化炮啖牺牲，号庖牺氏也。

仰则观象于天。
荀爽曰：震巽为雷风，离坎为日月也。

俯则观法于地。
九家易曰：艮兑为山泽也。地有水火五行八卦之形者也。

观鸟兽之文，与地之宜。
荀爽曰：乾为马，坤为牛，震为龙，巽为鸡之属是也。
陆绩曰：谓朱鸟、白虎、苍龙、玄武四方二十八宿经纬之文。
九家易曰：谓四方四维八卦之位、山泽高卑、五土之宜也。

近取诸身，远取诸物。
荀爽曰：乾为首，坤为腹，震为足，巽为股也。
荀爽曰：乾为金玉，坤为布釜之类是也。

于是始作八卦，以通神明之德，以类万物之情。
虞翻曰：谓庖牺观鸟兽之文，则天八卦效之。《易》有太极，是生两仪；两仪生四象；四象生八卦。八卦乃四象所生，非庖牺之所造也。故曰象者像此者也，则大人造爻象以象天卦可知也。而读易者咸以为庖牺之时天未有八卦，恐失之矣。天垂象示吉凶，圣人象之，则天已有八卦之象。
荀爽曰：乾坤为天地，离坎为日月，巽震为雷风，艮兑为山泽，此皆神明之德也。
九家易曰：六十四卦凡有万一千五百二十策，策类一物，故曰类万物之情；以此庖牺重为六十四卦明矣。

作结绳而为网罟，以佃以渔，盖取诸离。
虞翻曰：离为目，巽为绳；目之重者唯罟，故结绳为罟。坤二五之乾成离，巽为田，坤亦称田，以罟取兽曰畋，故取诸离也。

庖牺氏没，神农氏作。
虞翻曰：没，终；作，起也。神农以火德继庖牺，王火生土，故知土则利民播种，号神农氏也。

斫木为耜，揉木为耒；耒耨之利，以教天下，盖取诸益。
虞翻曰：否四之初也。巽为木、为入，艮为手，乾为金；手持金以入木，故斫木为耜。耜止所踰，因名曰耜。艮为小木，手以挠之，故揉木为耒。耒耜耔器也。巽为号令，乾为天，故以教天下。坤为田，巽为股，进退；震足动耜，艮手持耒，进退田中耕之象也。益万物者莫若雷风，故法风雷而作耒耜。

日中为市，致天下之民，聚天下之货，交易而退，各得其所，盖取诸噬嗑。
虞翻曰：否五之初也。离象正上，故称日中也。艮为径路，震为足，又为大涂；否乾为天，坤为民，故致天下之民象也。坎水、艮山，群珍所出，聚天下货之象也。震升坎降，交易而退，各得其所。噬嗑食也，市井交易饮食之道，故取诸此也。

神农氏没，黄帝、尧、舜氏作，通其变，使民不倦。
虞翻曰：变而通之以尽利，谓作舟楫、服牛乘马之类，故使民不倦也。

神而化之，使民宜之。
虞翻曰：神谓乾，乾动之坤，化成万物以利天下；坤为民，象其物宜，故使民宜之也。

易穷则变，变则通，通则久，是以自天祐之，吉无不利。
陆绩曰：阴穷则变为阳，阳穷则变为阴，天之道也。庖牺作网罟，教民取禽兽以充民食；民众兽少，其道穷，则神农教播殖以变之，此穷变之大要也。穷则变，变则通，与天终始，故可久；民得其用，故无所不利也。

黄帝、尧、舜垂衣裳而天下治，盖取诸乾坤。
九家易曰：黄帝以上，羽皮革木以御寒暑；至乎黄帝，始制衣裳，垂示天下。衣取象乾居上覆物，裳取象坤在下含物也。
虞翻曰：乾为治，在上为衣；坤下为裳。乾坤万物之缊，故以象衣裳。乾为明君，坤为顺臣，百官以治，万民以察，故天下治，盖取诸此也。

刳木为舟，剡木为楫；舟楫之利，以济不通，致远以利天下，盖取诸涣。
九家易曰：木在水上流行若风，舟楫之象也。此本否卦九四之二。刳，除也。巽为长、为木，艮为手，乾为金；艮手持金，故刳木为舟、剡木为楫也。乾为远天，故济不通、致远以利天下矣。法涣而作舟楫，盖取斯义也。

服牛乘马，引重致远，以利天下，盖取诸随。
虞翻曰：否上之初也。否乾为马、为远，坤为牛、为重；坤初之上为引重，乾上之初为致远。艮为背，巽为股，在马上故乘马；巽为绳，绳束缚物在牛背上，故服牛。出否之随，引重致远以利天下，故取诸随。

重门击柝，以待暴客，盖取诸豫。
干宝曰：卒暴之客为奸寇也。
九家易曰：下有艮象，从外示之，震复为艮，两艮对合，重门之象也。柝者两木相击以行夜。艮为手、为小木、为上持，震为足、又为木、为行，坤为夜，即手持柝木夜行击门之象也。坎为盗，暴水暴长无常，故以待暴客；既有不虞之备，故盖取诸豫也。

断木为杵，掘地为臼；臼杵之利，万民以济，盖取诸小过。
虞翻曰：晋上之三也。艮为小木，上来之三，断艮，故断木为杵；坤为地，艮手持木以掘坤三，故掘地为臼。艮止于下，臼之象也；震动而上，杵之象也。震出巽入，艮手持杵出入臼中舂之象也，故取诸小过。本无乾象，故不言以利天下也。

弦木为弧，剡木为矢；弧矢之利，以威天下，盖取诸睽。
虞翻曰：无妄五之二也。巽为绳、为木，坎为弧，离为矢，故弦木为弧；艮为小木，五之二以金剡艮，故剡木为矢；乾为威，五之二故以威天下。弓发矢应而坎雨集，故取诸睽也。

上古穴居而野处，后世圣人易之以宫室；上栋下宇，以待风雨，盖取诸大壮。
虞翻曰：无妄两象易也。无妄乾在上，故称上古；艮为穴居，乾为野，巽为处，无妄乾人在路，故穴居野处。震为后世，乾为圣人，后世圣人谓黄帝也。艮为宫室，变成大壮，乾人入宫，故易以宫室。艮为待，巽为风，兑为雨，乾为高，巽为长木反在上为栋；动起故上栋下宇，谓屋边也。兑泽动下为下宇。无妄之大壮，巽风不见，兑雨隔震与乾绝体，故上栋下宇以待风雨，盖取诸大壮者也。

古之葬者，厚衣之以薪，葬之中野，不封不树，丧期无数；后世圣人易之以棺椁，盖取诸大过。
虞翻曰：中孚上下象易也。本无乾象，故不言上古。大过乾在中，故但言古者。巽为薪，艮为厚，乾为衣、为野；乾象在中，故厚衣之以薪，葬之中野。穿土称封，封古窆字也。聚土为树。中孚无坤坎象，故不封不树。坤为丧期，谓从斩衰至缌麻日月之期数；无坎离日月坤象，故丧期无数。巽为木、为入处，兑为口，乾为人；木而有口，乾人入处棺敛之象。中孚艮为山丘，巽木在里，棺藏山陵椁之象也，故取诸大过。

上古结绳而治，后世圣人易之以书契；百官以治，万民以察，盖取诸夬。
九家易曰：古者无文字，其有约誓之事，事大大其绳，事小小其绳，结之多少随物众寡，各执以相考，亦足以相治也。夬本坤世，下有伏坤，书之象也；上又见乾，契之象也。以乾照坤察之象也。夬者决也，取百官以书治职，万民以契明其事。契，刻也。大壮进而成夬，金决竹木为书契象，故法夬而作书契矣。
虞翻曰：履上下象易也。乾象在上，故复言上古。巽为绳，离为网罟，乾为治，故结绳以治。后世圣人谓黄帝、尧、舜也。夬旁通剥，剥坤为书，兑为契；乾为百，剥艮为官，坤为众臣、为万民、为迷暗；乾为治，夬反剥以乾照坤，故百官以治、万民以察，故取诸夬。大壮、大过、夬，此三盖取直，两象上下相易，故俱言易之。大壮本无妄，夬本履卦，乾象俱在上，故言上古；中孚本无乾象，大过乾不在上，故但言古者。大过亦言后世圣人易之，明上古时也。

是故《易》者，象也。
干宝曰：言是故又总结上义也。
虞翻曰：《易》谓日月在天成八卦，象县象著明，莫大日月是也。

象也者，像也。
崔憬曰：上明取象以制器之义，故以此重释于象，言《易》者象于万物，象者形像之象也。

彖者，材也。
虞翻曰：彖说三才，则三分天象以为三才，谓天地人道也。

爻也者，效天下之动者也。
虞翻曰：动，发也。谓两三才为六画，则发挥刚柔而生爻也。

是故吉凶生而悔吝著也。
虞翻曰：爻象动内则吉凶见外，吉凶悔吝者生乎动者也，故曰著。

阳卦多阴，阴卦多阳，其故何也？
崔憬曰：此明卦象阴阳与德行也。阳卦多阴，谓震坎艮一阳而二阴；阴卦多阳，谓巽离兑一阴而二阳也。

阳卦奇，阴卦耦，其德行何也？
虞翻曰：阳卦一阳故奇，阴卦二阴故耦，谓德行何可者也。

阳一君而二民，君子之道也；阴二君而一民，小人之道也。
韩康伯曰：阳君道也，阴臣道也。君以无为统众，无为则一也；臣以有事代终，有事则二也。故阳爻画一以明君道必一，阴爻画两以明臣体必二，斯阴阳之数、君子之辨也。以一为君，君之德也；二居君位，非其道也，故阳卦曰君子之道也，阴卦曰小人之道也。

易曰：憧憧往来，朋从尔思。
翟玄曰：此咸之九四辞也。咸之为卦，三君三民，四独远阴思虑之爻也。
韩康伯曰：天下之动必归于一，思以求朋，未能寂寂以感物，不思而至也。

子曰：天下何思何虑？天下同归而殊涂，一致而百虑，天下何思何虑？
韩康伯曰：夫少则得，多则惑；涂虽殊其归则同，虑虽百其致不二。苟识其要，不在博求；一以贯之，百虑而尽矣。
虞翻曰：《易》无思也。既济定六位得正，故何思何虑。

日往则月来，月往则日来，日月相推而明生焉。
虞翻曰：谓咸初往之四与五成离，故日往；与二成坎，故月来。外日往，在内月来，此就爻之正者也。
虞翻曰：初变之四与上成坎，故月往；四变之初与三成离，故日来者也。
虞翻曰：既济体两离坎象，故明生焉。

寒往则暑来，暑往则寒来，寒暑相推而岁成焉。
虞翻曰：乾为寒，坤为暑，谓阴息阳消，从姤至否，故寒往暑来也。
虞翻曰：阴屈阳信，从复至泰，故暑往寒来也。
崔憬曰：言日月寒暑往来虽多，而明生岁成相推则一，何思何虑于其间哉？

往者屈也，来者信也，屈信相感而利生焉。
荀爽曰：阴气往则万物屈者也。阳气来则万物信者也。
虞翻曰：感咸象，故相感。天地感而万物化生，圣人感人心而天下和平，故利生。利生谓阳出震、阴伏藏。

尺蠖之屈，以求信也；龙蛇之蛰，以存身也。
荀爽曰：以喻阴阳气屈以求信也。
虞翻曰：潜藏也。龙潜而蛇藏，阴息初巽为蛇，阳息初震为龙；十月坤成，十一月复生；姤巽在下，龙蛇俱蛰，初坤为身，故龙蛇之蛰以存身。
侯果曰：不屈则不信，不蛰则无存，则屈蛰相感而后利生矣。以况无思得一，则万物归思矣。蠖屈行虫，郭璞云蝍蛆也。
庄子曰：古之畜天下者其治一也。记曰：通于一，万事毕；无心得，鬼神服，此之谓矣。

精义入神，以致用也。
姚信曰：阳称精，阴为义，入在初也。阴阳在初，深不可测，故谓之神；变为姤复，故曰致用也。
韩康伯曰：精义，物理之微者也。神寂然不动，感而遂通者也。理入寂一，则精义斯得，乃用无极也。
干宝曰：能精义理之微，以得未然之事，是以涉于神道而逆祸福也。

利用安身，以崇德也。
九家易曰：利用，阴道用也，谓姤时也。阴升上兑，则乾伏坤中，屈以求信，阳当复升，安身嘿处也。时既潜藏，故利用安身以崇其德。
韩康伯曰：利用之道皆安其身而后动也。精义由于入神以致其用，利用由于安以崇其德；理必由乎其宗，事各本乎其根，归根则宁，天下之理得也。若役其思虑以求动用，忘其安身以殉功义，则伪弥多而理愈失，名弥美而累愈彰矣。

过此以往，未之或知也。
荀爽曰：出乾之外，无有知之。

穷神知化，德之盛也。
虞翻曰：以坤变乾谓之穷神，以乾通坤谓之知化，乾为盛德。
侯果曰：夫精义入神、利用崇德，亦一致之道极矣；过斯以往，则未之能知。若穷于神理，通于变化，则德之盛者能矣。

易曰：困于石，据于蒺藜，入于其宫，不见其妻，凶。
孔颖达曰：上章先言利用安身可以崇德，若身危辱，何崇之有？此章引困之六三，履非其位，欲上于四，四自应初，不纳于己，是困于九二之蒺藜也；又有入于其宫不见其妻凶之象也。

本条仅录卷十五前段至该爻辞及其引证，后续“子曰非所困而困焉”等释文尚待补录；原文与注文分层，未把解释文字并入经文。""", chatGptTranslation = """**ChatGPT 翻译（逐段工作译文）**

**天一配水、甲；地二配火、乙；天三配木、丙；地四配金、丁；天五配土、戊；地六配水、己；天七配火、庚；地八配木、辛；天九配金、壬；地十配土、癸。**

**这些数合起来就是大衍之数五十五，是蓍草与龟卜数理所由产生的依据。圣人借此贯通神明的德性，并据此比类万物的情状。**以上是虞翻这一段的解释。

**孔子说：易为什么而创作呢？**

虞翻解释：所谓《易》，为什么要取法天地之数呢？

**《易》能够开启事物、成就事务，涵盖天下的道理，大致就是如此。**

陆绩解释：“开物”是说伏羲推演八卦，再重叠为六十四卦，使爻策不断推演，达到一万一千五百二十策，以对应万物的数目，所以称为开物。圣人观察卦象，制作网罟、耒耜等器具，成就天下的事务，所以称为成务。虞翻解释：阳气开启坤阴，称为开物；阴气收合乾阳，称为成务。“冒”是触及、涵盖的意思，依照事物类别触类旁通、推演增长。

**因此，圣人借此沟通天下人的心志。**

九家易解释：凡是说“是故”，都是承接上文。这里指行动者崇尚变化，通过变化使事理通达，从而沟通天下人的心志。

**借此确立天下的事业。**

九家易解释：这是说制作器物的人崇尚卦象。事业尚未建立时，可以依据《易》道作出判断，所以说借此确立天下事业。

**借此判断天下的疑难。**

九家易解释：这是说卜筮者崇尚占问。占问事情可以预知未来，所以能够解决天下的疑惑。

**因此，蓍草的德性圆融而神妙，卦的德性方正而有智慧。**

崔憬解释：蓍策之数为七乘七，即四十九，象征阳的圆；蓍策的运用变化不定，可以借此预知未来事物，所以说蓍的德性圆而神。卦的数目为八乘八，即六十四，象征阴的方；卦的运用有爻位分别，可以据此保存并理解已经发生的事情，所以说卦的德性方而智。

**六爻的意义，能够借变化告知吉凶。**

韩康伯解释：“贡”是告知。六爻发生变化，就能借此告知吉凶。

**圣人借此洗涤内心。**

韩康伯解释：这是洗濯万物之心。

**退藏于幽密之处。**

陆绩解释：接受蓍龟所显示的征兆，作出判断之后，把它收藏在心中。

**吉凶都与百姓共同承担。**

虞翻解释：圣人指伏羲。圣人凭借蓍草的神妙预知未来，所以能够洗心。阳爻发动而进入巽卦，巽象征退伏；坤象征闭户，因此说退藏于密。阳象征吉，阴象征凶，坤象征百姓，所以说吉凶与百姓共同忧患；这也说明创作《易》的人心怀忧患。

**神妙的作用用来预知未来，智慧用来收藏过去。**

虞翻解释：乾象征神妙，能够预知未来；坤象征智慧，能够收藏过去。未来指显现出来的事情，过去指已经收藏于幽密之中的事情。

**谁能够参与这种境界呢？**

虞翻解释：谁能够做到这些呢？指古代聪明、睿智的君主。

**大概是古代那些聪明睿智、具有神武之德却不滥杀的人吧。**

虞翻解释：这里指大人，也就是伏羲。伏羲处在乾卦九五，发动而变化为坤，与天地相合。坎象征聪，离象征明；乾象征神武，坤象征睿智。乾、坤、坎、离反复运行而不衰，所以说神武而不杀。

**因此，他能够明白天道，并考察百姓的实际情形。**

虞翻解释：乾五变化为坤，以离日照耀天空，所以说明白天道；以坎月照耀坤地，所以说考察百姓的情况。坤象征百姓。

**于是兴起神妙的器物，走在百姓使用之前。**

陆绩解释：神物指蓍草。圣人兴起蓍草，用来辨别吉凶，先于百姓使用，百姓都跟随采用，所以说走在百姓使用之前。

**圣人借此斋戒。**

韩康伯解释：洗心称为斋，防备祸患称为戒。

**借此使自身的德性达到神明的境界。**

陆绩解释：圣人知道蓍草能够预先判断吉凶，避开祸害、趋向利益，因此洁净自身，这叫作斋戒。判断吉祥之后才行动，举措没有违失，德行丰盛而被称为神明，这就是以此使德性神明。

**因此，关闭门户称为坤。**

虞翻解释：“阖”是关闭、收合。这里指从巽变化为坤；坤柔顺，象征夜晚，所以用关闭门户来比喻。

**打开门户称为乾。**

虞翻解释：“辟”是开启。这里指从震变化为乾；乾刚健，象征白昼，所以用打开门户来比喻。

**一闭一开称为变化。**

虞翻解释：阳的变化使阴闭合，阴的变化使阳开启，刚柔相互推移，于是产生变化。

**往来不穷尽称为通达。**

荀爽解释：这是说冬去夏来，阴阳相互变化。十二消息卦中阴阳往来不止，所以称为通。

**显现出来的称为象，形成具体形体的称为器。**

荀爽解释：日月星辰的光辉显现在天空，形成天象；万物在地上生长而形成形体，就可以成为器物。

**制作并加以使用，称为法。**

荀爽解释：观察天上的象，观察地上的形体，再制作器物并加以使用，就可以成为法则。

**器物的功用遍及出入，百姓都能使用，这就称为神妙。**

陆绩解释：圣人制作器物，使它们周全满足百姓的需要，没有遗漏，所以说利用出入。百姓都使用这些器物，却不知道它们从何而来，所以称为神妙。

译释说明：本段涉及天地数、干支、蓍龟与八卦象数。原文列数与干支的对应照录，不据其他数术体系擅自调整；“神物”在陆绩注中明确指蓍草。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷首·四库提要、李鼎祚原序与朱睦㮮序", "《周易集解》卷首资料。依次收录四库馆臣提要、唐李鼎祚原序、明朱睦㮮序；保留卷帙争议及版本考辨。底本：景印文渊阁《四库全书》本，维基文库对应卷首页面。原文初录，逐字影印校勘与逐段译文待续。", """【底本与说明】
底本：景印文渊阁《四库全书》本《周易集解》卷首，参照维基文库《周易集解（四库全书本）》卷首三个独立页面：提要、周易集解原序、周易集解序。
本条按原书卷首次序收录四库馆臣提要、唐李鼎祚自序、明朱睦㮮序。三篇性质不同：提要为清代四库馆臣考订；原序为李鼎祚自述编纂宗旨；后序为明代朱睦㮮的版本与学术评论。不可混作李鼎祚本人观点。
数字转录按网页底本分段整理；异体字、避讳字、图像处及个别疑难字仍须逐页对照影印本。此条先补原文，逐段译文尚待校订，未以概述冒充译文。

【一、四库馆臣提要】
钦定四库全书　经部一
周易集解　易类
提要

臣等谨案《周易集解》十七卷，唐李鼎祚撰。鼎祚《唐书》无传，始末未详，惟据序末结衔知其官为秘书省著作郎。据袁桷《清容居士集》载，资州有鼎祚读书台，知为资州人耳。朱睦㮮序称为秘阁学士，不知何据也。其时代亦不可考。旧《唐书·经籍志》称录开元盛时四部诸书而不载是编，知为天宝以后人矣。

其书《新唐书·艺文志》作十七卷。晁公武《读书志》曰今所有止十卷，而始末皆全，无所亡失，岂后人并之耶？《经义考》引李焘之言，则曰鼎祚自序止云十卷，无亡失也。朱睦㮮序作于嘉靖丁巳，亦云自序称十卷，与焘说同。今所行毛晋汲古阁本乃作一十七卷，序中亦称王氏《略例》附于卷末，凡成一十八卷，与诸家所说截然不同，殊滋疑窦。

今考序中称“至如卦爻彖象，理涉重玄，经注文言书之不尽，别撰《索隐》《错综》《根萌》《音义》两存，详之明矣”等语，则《集解》本十卷，附《略例》一卷，为十一卷；尚别有《索隐》六卷，共成十七卷。《唐志》所载盖并《索隐》《略例》数之，实非舛误。至宋而《索隐》散佚，刊本又削去《略例》，仅存《集解》十卷，故与《唐志》不符。至毛氏刊本始析十卷为十七卷，以合《唐志》之文，又改序中“一十卷”为“一十八卷”，以合附录《略例》一卷之数，故又与朱睦㮮序不符。盖自宋以来，均未究序中“别撰《索隐》”一语，故疑者误疑，改者误改；即辨其本止十卷者，亦不能解《唐志》称十七卷之故，致愈说愈讹耳。

今详为考正，以祛将来之疑。至十卷之本今既未见，则姑仍以毛本著录，盖篇帙分合无关宏旨，固不必一一追改也。其书仍用王弼本，惟以《序卦传》散缀六十四卦之首，盖用《毛诗》分冠小序之例。所采凡子夏、孟喜、焦赣、京房、马融、荀爽、郑元、刘表、何晏、宋衷、虞翻、陆绩、干宝、王肃、王弼、姚信、王廙、张璠、向秀、王凯冲、侯果、蜀才、翟元、韩康伯、刘𪩘、何妥、崔憬、沈𬴊士、卢氏（案：卢氏《周易注》，《隋志》已佚其名）、崔觐、伏曼容、孔颖达（案：以上三十二家，朱睦㮮序所考）、姚规、朱仰之、蔡景君（案：以上三家，朱彝尊《经义考》所补考）等三十五家之说。

自序谓“刊辅嗣之野文，补康成之逸象”，盖王学既盛，汉易遂亡，千百年后学者得考见画卦之本旨者，惟赖此书之存矣，是真可宝之古笈也。
乾隆四十五年七月恭校上。
总纂官臣纪昀、臣陆锡熊、臣孙士毅。
总校官臣陆费墀。

【二、唐李鼎祚《周易集解》原序】
序曰：

元气氤氲，三才成象；神功浃洽，八索成形。在天则日月运行，润之以风雨；在地则山泽通气，鼓之以雷霆。至若近取诸身，四支百体，合其度；远取诸物，森罗万象，备其工。阴阳不测之谓神，一阴一阳之谓道。范围天地而不过，曲成万物而不遗。仁者见之以为仁，知者见之以为知，百姓日用而不知。君子之道鲜矣。斯乃显诸仁而藏诸用，神无方而易无体，巍巍荡荡，难可名焉。

逮乎天尊地卑，君臣位列；五运相继，父子道彰；震巽索而男女分，咸恒设而夫妇睦。人伦之义既阐，家国之教郁兴。故《系辞》云：古者庖牺氏王天下也，始画八卦，以通神明之德，以类万物之情。作结绳而为罔罟，以佃以渔，盖取诸离。庖牺氏没，神农氏作，斫木为耜，揉木为耒，耒耨之利以教天下，盖取诸益。日中为市，致天下之人，聚天下之货，交易而退，盖取诸噬嗑。神农氏没，黄帝、尧、舜氏作，通其变，使人不倦；神其化，使人宜之。刳木为舟，剡木为楫，舟楫之利以济不通，盖取诸涣。服牛乘马，引重致远，盖取诸随。古者穴居而野处，后代圣人易之以宫室，盖取诸大壮。弦木为弧，剡木为矢，弧矢之利以威天下，盖取诸睽。上古结绳为政，后代易之以书契，百官以理，万人以察，盖取诸夬。

故圣人见天下之赜，而拟诸形容，象其物宜，而观其会通，以行其典礼。触类而长之，六十四卦、三百八十四爻，天下之能事毕矣。其旨远，其辞文；其言曲而中，其事肆而隐。若夫杂物撰德，辨是与非，终日乾乾，夕惕若厉，无有师保，如临父母；自天祐之，吉无不利者也。至于损以远害，说以先之，定其交而后求安其身而后动；履和而至，谦尊而光，能说诸心，能研诸虑。是故君子居则观其象而玩其辞，动则观其变而玩其占。蓍之德圆而神，卦之德方以知。探赜索隐，钩深致远，定天下之吉凶，成天下之亹亹，莫善乎蓍龟。神以知来，知以藏往，将有为也，问之以言，其受命也如响，无有远迩幽深，遂知来物，故能穷理尽性，利用安身。

圣人以此洗心，退藏于密。自然虚室生白，吉祥至止；坐忘遗照，精义入神；口僻焉不能言，心困焉不能知。微妙玄通，深不可识。易有圣人之道四焉，斯之谓矣。

原夫权舆三教，钤键九流，实开国承家、修身之正术也。自卜商入室，亲授微言，传注百家，绵历千古，虽竞有穿凿，犹未测渊深。唯王、郑相沿，颇行于代。郑则多参天象，王乃全释人事。且易之为道，岂偏滞于天人者哉？致使后学之徒纷然淆乱，各修局见，莫辨源流。天象远而难寻，人事近而易习，则折杨、黄华，嗑然而笑，方以类聚，其在兹乎？

臣少慕玄风，游心坟籍，历观炎汉迄今巨唐，采群贤之遗言，议三圣之幽赜。集虞翻、荀爽三十馀家，刊辅嗣之野文，补康成之逸象；各列名义，共契玄宗。先儒有所未详，然后辄加添削。每至章句，佥例发挥，俾童蒙之流一览而悟，达观之士得意忘言。当仁既不让于师，论道岂惭于前哲？至如卦爻彖象，理涉重玄；经注文言书之不尽，别撰《索隐》《错综》《根萌》《音义》，两存详之明矣。其王氏《略例》，得失相参，采葑采菲，无以下体，仍附经末，式广未闻。凡成一十八卷，以贻同好，冀将来君子无所疑焉。
秘书省著作郎臣李鼎祚序。

【三、明朱睦㮮《周易集解序》】
予观《唐艺文志》称李鼎祚集注《周易》十七卷，据鼎祚自序云十卷而首尾俱全，初无亡失，不知唐史何所据而云十七卷也。《崇文总目》及《邯郸图书志》亦称七篇逸，盖承唐史之误耳。鼎祚解经多避唐讳，又取《序卦》冠于各卦之首。所引有子夏、孟喜、焦赣、京房、马融、荀爽、郑玄、刘表、何晏、宋衷、虞翻、陆绩、干宝、王肃、王弼、姚信、王廙、张璠、向秀、王凯冲、侯果、蜀才、翟玄、韩康伯、刘巘、何妥、崔憬、沈驎士、卢氏、崔觐、伏曼容、孔颖达，凡三十二家；又引《九家易》《乾凿度》诸说，义有未详，鼎祚乃加增削。予尝综其义例，盖宗郑学者也。

自商瞿之后，注《易》者百家，而郑氏玄、王氏弼为最显。郑之学主象数，王之学主名理。汉晋以来，二氏学并立。至刘宋初，颜延之为祭酒，黜郑置王，时陆澄、王济辈皆以为不可。自是河汾诸儒多主于郑，江左及青齐多主于王。唐兴，孔颖达受诏撰定《五经正义》，于《易》独取王传，而郑学遂废，先代专门之业亦复不传，可胜叹哉！

夫《易》有圣人之道四焉，世之言理义之学者以其辞耳，象变与占其可阙乎？昔吴季札之鲁观乐，见《易》象喜曰：“周礼尽在鲁矣。”是故象者《易》之原也。象成而后有辞，辞著而后有变，变见而后有占。若乃专尚文辞，不复推原大传，天人之道岐而为二，可乎？康成去古未远，其所纂述必有所本。鼎祚恐其失坠，以广其说，均之为有裨于《易》者也。

是编刻自宋季，人间希有存者。顷岁予得之李中麓氏，复用校梓以传，欲使圣人之道不致偏滞，而自汉迄唐三十家之言亦不至埃灭弗闻也。鼎祚资州人，仕唐为秘阁学士，以经学称于时。及阅《唐列传》与《蜀志》，俱不见其人，岂遗之耶？抑别有所载耶？因附论著于此，以俟博雅者考焉。
嘉靖丁巳冬十二月望日，后学汴上睦㮮序。

【校勘提示】
1. 四库馆臣提要讨论“十卷、十七卷、十八卷”卷帙差异，并提出《集解》十卷、《略例》一卷、《索隐》六卷的解释；这是馆臣考订意见，不等同于已经发现并核实了全部散佚卷本。
2. 李鼎祚原序末称“凡成一十八卷”；朱睦㮮序转述其自序“云十卷”；四库提要也记录了不同版本与目录说法。以上说法并存，暂不强行统一。
3. 原序页面有“弦木为弧”处的图像标记；本条仅保留可辨文字，不把图像标记臆补成插图内容。
4. 原序“凡成一十八卷”与四库提要所述十卷、十七卷、十八卷问题，均按各篇原说保留。"""),

    Entry("历代注疏", "王弼《周易略例》卷首序、总目与《明彖》篇（附邢璹注）", "关联典籍：王弼《周易略例》。收录唐邢璹序、总目、题署释名及《明彖》篇，邢璹注文与陆德明音义分层标示。底本：《钦定四库全书》本《周易注疏》附《周易略例》。仅为本书前部，余篇待续。", """【文献性质与底本】
本条作为《周易集解》卷首原序所提及的王弼《略例》之关联典籍，独立收录，不并入李鼎祚正文。底本采用《钦定四库全书》本《周易注疏》附录《周易略例》页面；该页面说明本篇为王弼撰、唐邢璹注并序，所录包含邢璹序、总目、王弼正文及邢璹注文、陆德明音义。
本次收录：邢璹序、篇目总目、《周易略例》题署及《明彖》一篇。正文中“注”字后为邢璹注文；“音义”条为陆德明音义，均与王弼本文分层保留。原页面为四库本数字转录，字形、断句、异文仍待对照影印页复核。

【一、周易略例序】
唐四门助教邢璹撰

原夫两仪未位，神用藏于视听；一气化矣，至赜隐乎名言。于是河龙负图，牺皇画卦，仰观俯察，远物近身，八象穷天地之情，六位备刚柔之体。言大道之妙，有一阴一阳；论圣人之范围，显仁藏用。实三元之胎祖，鼓舞财成；为万有之蓍龟，知来藏往。是以孔丘三绝，未臻枢奥；刘安九师，尚迷宗旨。

臣舞象之年，鼓箧鳣序，渔猎坟典，偏习《周易》，研穷耽玩，无舍寸阴。是知卦之纪纲，周文王之言略矣；象之吉凶，鲁仲尼之论备矣。至如王辅嗣《略例》，大则总一部之指归，小则明六爻之得失，承乘逆顺之理，应变情伪之端，用有行藏，辞有险易。观之者，可以经纬天地，探测鬼神，匡济邦家，推辟咎悔。虽人非上圣，亦近代一贤。臣谨依其文，辄为注解，虽不足敷弘《易》道，庶几有裨于教义，亦犹萤燐增辉于太阳，涓流助深于巨壑。臣之志也，敢不上闻。

【二、总目】
《周易略例》音义：此是辅嗣所作，既释经文，故相承讲之，今亦随世音焉。或有题为“第十”者，后人辄加之耳。

明彖
明爻通变
明卦适变通爻
明象
辩位
略例下
卦略

音义校记：
“明卦适变通爻”，一本又作“明卦通变适爻”，又一本直云“适变通爻”。
“略例下”，旧本如此，本或无“下”字。
“卦略”，凡十一卦。

【三、王弼《周易略例》题署及释名】
魏王弼著，唐邢璹注，陆德明音义。

略例者，举释纲目之名，统明文理之称。略，不具也；例，举并也。辅嗣以先儒注《易》二十余家，虽小有异同，而迭相祖述，推比所见特殊，故作《略例》以辩诸家之惑，错综文理，略录之也。

【四、明彖】
夫彖者，何也？（邢璹注：将释其义，故假设问端而曰“何”。）统论一卦之体，明其所由之主者也。（邢璹注：统论一卦功用之体，明，辩也；辩卦体功用所由之主，立主之义，义在一爻，明辩也。）

夫众不能治众，治众者，至寡者也。（邢璹注：万物是众，一是寡；众不能治众，治众者至少以治之也。）夫动不能制动，制天下之动者，贞夫一者也。（邢璹注：天下之动，动则不能自制；制其动者，贞正之一者也。老子曰：“王侯得一以为天下贞。”然则一为君体，君体合道；动是众，众由一制也。制众归一，故静为躁君，安为动主。）

故众之所以得咸存者，主必致一也。（邢璹注：致犹归也。众皆得存，其存者必归于一，故无心于存皆得其存也。）动之所以得咸运者，原必无二也。（邢璹注：动所以运，运不已者，谓无二动，故无心于动而动不息也。）物无妄然，必由其理。（邢璹注：物，众也；妄，虚妄也。天下之众皆无妄，无妄之理必由君主统之也。）

统之有宗，会之有元。（邢璹注：统领之以宗主，会合之以元首。）故繁而不乱，众而不惑。（邢璹注：统之有宗主，虽繁而不乱；会之以元首，虽众而不惑。）故六爻相错，可举一以明也。（邢璹注：错，杂也。六爻或阴或阳，错杂交乱，举贞一之主以明其用。）刚柔相乘，可立主以定也。（邢璹注：六爻有刚有柔，或乘或据，有逆有顺，可立主以定之。）

是故杂物撰德，（邢璹注：撰，数也；杂，聚也。聚其物体，数其德行。）辩是与非，（邢璹注：辩，明也。得位而承之是也，失位而据之非也。）则非其中爻，莫之备矣。（邢璹注：然则非是中之一爻，莫之能备。讼《彖》云“讼有孚，窒惕，中吉”，刚来而得中也；困《彖》云“贞，大人吉”，以刚中也，之例是也。）

故自统而寻之，物虽众，则知可以执一御也。（邢璹注：无为之一者，道也，君也。统而推寻万物虽殊，一之以神道；百姓虽众，御之以君主也。）由本以观之，义虽博，则知可以一名举也。（邢璹注：博，广也。本谓君也、道也。义虽广，举之在一也。）

故处璇玑以观大运，则天地之动未足怪也；据要会以观方来，则六合辐凑未足多也。（邢璹注：天地虽大，睹之以璇玑；六合虽广，据之以要会。天地之运不足怪其大，六合辐凑不足称其多。音义：“璇”，悉全反，又作“旋”；“玑”，音机，本又作“机”或作“几”；“辐”，音福；“凑”，千豆反。）

故举卦之名，义有主矣；观其彖辞，则思过半矣。（邢璹注：彖总卦义，义主中爻。简易者，道也、君也。道能化物，君能御民，智者观之，思过其半。音义：“思”，息吏反。）

夫古今虽殊，军国异容，中之为用，故未可远也。（邢璹注：古今革变，军国殊别，中正之用终无疏远。音义：“远”，于万反。）品制万变，宗主存焉；彖之所尚，斯为盛矣。（邢璹注：品变积万，存之在一。）

夫少者，多之所贵也；寡者，众之所宗也。（邢璹注：自此已下，明至寡者为至多之所主，岂直指其中爻而已。）

一卦五阳而一阴，则一阴为之主矣。（邢璹注：同人、履、小畜、大有之例是也。）五阴而一阳，则一阳为之主矣。（邢璹注：师、比、谦、豫、复、剥之例是也。）夫阴之所求者阳也，阳之所求者阴也。（邢璹注：王弼云，夫阴阳相求之物，以所求者贵也。阳苟一焉，五阴何得不同而归之？阴苟只焉，五阳何得不同而从之？故阴爻虽贱，而为一卦之主者，处其至少之地也。小畜《彖》云“柔得位而上下应之”，是也。）

或有遗爻而举二体者，卦体不由乎爻也。（邢璹注：遗，弃也。弃此一爻而举二体以明其义，卦体之义不在一爻，丰、归妹之类是也。）繁而不忧乱，变而不忧惑，约以存博，简以济众，其唯彖乎！（邢璹注：简易者，道也、君也。万物是众，道能生物，君能养民；物虽繁不忧错乱，爻虽变不忧迷惑。）乱而不能惑，变而不能渝，非天下之至赜，其孰能与于此乎？（邢璹注：万物虽杂不能惑其君，六爻虽变不能渝其主，非天下之至赜、神武之君，其谁能与于此？言不能也。音义：“渝”，羊朱反；“赜”，仕责反；“与”，音预。）故观彖以斯，义可见矣。（邢璹注：观彖以斯，其义可见。音义：“观彖以斯”，亦作“以彖观之”。）

【校勘与范围说明】
1. 《明彖》正文与邢璹注文在四库本中交错排印，本条用括注标示“邢璹注”，不将注语误作王弼原文。
2. 总目所列篇名异文按底本音义原样保留；“略例下”有无“下”字的版本差异不作统一。
3. 本条止于《明彖》篇末“故观彖以斯，义可见矣”，不宣称已收录《周易略例》全书；其余各篇待续。
4. 网页数字文本与影印本逐字核验尚未完成，个别字形（如“燐/磷”“璇/旋”等）保留底本或校记提示，不擅自定为唯一正字。""",
 
    Entry("历代注疏", "王弼《周易略例》明爻通变、明卦适变通爻、明象与辩位", "关联典籍续录：收录《明爻通变》《明卦适变通爻》《明象》《辩位》四篇王弼正文。依四库本页面逐篇录入；本条以王弼正文为主，邢璹注文与陆德明音义不在此处混排，待逐段校录。", """【文献性质与录入说明】
本条承接《周易略例》卷首及《明彖》条目，录入四篇正文。底本为《钦定四库全书》本《周易注疏》附录《周易略例》，王弼撰，唐邢璹注并序，陆德明音义。以下先录王弼正文；底本中穿插的邢璹注、陆德明音义本轮不混入正文，后续将另作分层补录。底本篇名作“明卦适变通爻”；总目音义另记“明卦通变适爻”“适变通爻”等异文，照录存异。

【五、明爻通变】
夫爻者，何也？言乎变者也。变者，何也？情伪之所为也。夫情伪之动，非数之所求也。故合散屈伸，与体相乖；形躁好静，质柔爱刚，体与情反，质与愿违。巧历不能定其算数，圣明不能为之典要，法制所不能齐，度量所不能均也。为之岂在夫大哉？

陵三军者，或惧于朝廷之仪；暴威武者，或困于酒色之娱。近不必比，远不必乖；同声相应，高下不必均也；同气相求，体质不必齐也。召云者龙，命吕者律。故二女相违，而刚柔合体。隆墀永叹，远壑必盈。投戈散地，则六亲不能相保；同舟而济，则胡越何患乎异心。

故苟识其情，不忧乖远；苟明其趣，不烦强武。能说诸心，能研诸虑，睽而知其类，异而知其通，其唯明爻者乎？故有善迩而远至，命宫而商应，修下而高者降，与彼而取此者服矣。

是故情伪相感，远近相追，爱恶相攻，屈伸相推。见情者获，直往则违。故拟议以成变化，语成而后有格。不知其所以为主，鼓舞而天下从，见乎其情者也。

是故范围天地之化而不过，曲成万物而不遗，通乎昼夜之道而无体，一阴一阳而无穷，非天下之至变，其孰能与于此哉？是故卦以存时，爻以示变。

【六、明卦适变通爻】
夫卦者，时也；爻者，适时之变者也。夫时有否泰，故用有行藏；卦有小大，故辞有险易。一时之制，可反而用也；一时之吉，可反而凶也。故卦以反对，而爻亦皆变。

是故用无常道，事无轨度，动静屈伸，唯变所适。故名其卦，则吉凶从其类；存其时，则动静应其用。寻名以观其吉凶，举时以观其动静，则一体之变由斯见矣。

夫应者，志同之象也；位者，爻所处之象也；承乘者，逆顺之象也；远近者，险易之象也；内外者，出处之象也；初上者，终始之象也。故虽远而可以动者，得其应；虽险而可以处者，得其时也。弱而不惧于敌者，得所据也；忧而不惧于乱者，得所附也；柔而不忧于断者，得所御也；虽后而敢为之先者，应其始也；物竞而独安于静者，要其终也。

故观变动者存乎应，察安危者存乎位，辩逆顺者存乎承乘，明出处者存乎外内。近远终始，各存其会；辟险尚远，趋时贵近。比复好先，乾壮恶首，明夷务闇，丰尚光大。

吉凶有时，不可犯也；动静有适，不可过也。犯时之忌，罪不在大；失其所适，过不在深。动天下，灭君主，而不可危也；侮妻子，用颜色，而不可易也。故当其列贵贱之时，其位不可犯也；遇其忧悔吝之时，其介不可慢也。观爻思变，变斯尽矣。

夫体义者在乎象，变通者存乎爻。大人者，形与万物接，神与造化偕。入则乘潜龙，出则乘飞龙；远以观道，迩以瞻器，其数无轨而不可既也。君子者，健以行志，非以神明变通者也。故自强不息，以行其志，君子之道也；唯可与于健之体义，而未可与于健之变通也。

故于象称君子，举其体义者也；于爻称大人，叙其变通者也。或有在爻而称大人者，义则尽于象故也。若离之为明，其何不照？变而通之，无所不造。故其象云：“大人继明，照于四方”也。或有在爻而称大人者，义有待于变故也。若否卦全说君子避难，不荣于禄，与时通塞，可以适否而安其身，未足兼善天下，在否而休也。唯大人为能休天地之否，达济凝滞，开阐幽门，斯谓变否而亨者也。故六二云“大人否亨”，而九五云“大人休否”也。

夫象赡而爻不足者，离卦之类是也，义尽于象而不待于爻也。爻备而象不具者，否卦之类是也，象所未尽而待爻以备。故象以体示，而爻以变明。存斯以考其义，则穷通之旨显，而开塞之涂见矣。

夫彖者，全论一卦之体；而象者，各举一爻之义。一卦所无，世之遗也；爻或有焉，才之所生也。故才生于世所不足，观其逆顺，以定得失之占；察其乖合，以明休咎之征。若小畜卦称“密云不雨”，而上九爻云“既雨”，既处在于不雨之时，而独以能雨为德者，存乎物之所欲也。若履卦称“履虎尾，不咥人，亨”，而六三爻云“履虎尾，咥人，凶”，在于履亨之时，而独以见咥为凶者，存乎众之所忌也。夫负世之才，虽小无易；适时之望，虽大无难。是以君子洗心，知终将有为也。问焉而动，每居其适，斯可以旁通而无际矣。

【七、明象】
夫象者，出意者也；言者，明象者也。尽意莫若象，尽象莫若言。言生于象，故可寻言以观象；象生于意，故可寻象以观意。意以象尽，象以言著。

故言者所以明象，得象而忘言；象者所以存意，得意而忘象。既得龙象，其言可忘；既得乾意，其龙可舍。犹蹄者所以在兔，得兔而忘蹄；筌者所以在鱼，得鱼而忘筌也。然则言者象之蹄也，象者意之筌也。

是故存言者非得象者也，存象者非得意者也。象生于意而存象焉，则所存者乃非其象也；言生于象而存言焉，则所存者乃非其言也。然则忘象者乃得意者也，忘言者乃得象者也。得意在忘象，得象在忘言。

故立象以尽意，而象可忘也；重画以尽情，而画可忘也。是故触类可为其象，合义可为其征。义苟在健，何必马乎？类苟在顺，何必牛乎？故或有以某象系于乾坤者，皆非定也。案文责卦，有马无乾，则伪说滋漫，难可纪矣。

互体不足，遂及卦变；变又不足，推致五行。一失其原，巧愈弥甚。纵复或值，而义无所取，盖存象忘意之由也。忘象以求其意，义斯见矣。

【八、辩位】
案象无初上得位失位之文；《系辞》但论三五二四同功异位，亦不及初上，何乎？唯乾上九《文言》云“贵而无位”，需上六云“虽不当位”。若以上为阴位邪，则需上六不得云“不当位”也；若以上为阳位邪，则乾上九不得云“贵而无位”也。阴阳处之，皆云非位，而初亦不说当位失位也。

然则初上者，是事之终始，无阴阳定位也。故乾初谓之“潜”，过五谓之“无位”，未有处其位而云“潜”，上有位而云“无”者也。历观众卦，尽亦如之，初上无阴阳定位，亦以明矣。

夫位者，列贵贱之地，待才用之宅也。爻者，守位分之任，应贵贱之序者也。位有尊卑，爻有阴阳；尊者阳之所处，卑者阴之所履也。故以尊为阳位，卑为阴位。去初上而论位分，则三五各在一卦之上，亦何得不谓之阳位？二四各在一卦之下，亦何得不谓之阴位？

初上者，体之终始，事之先后也，故位无常分，事无常所，非可以阴阳定也。尊卑有常序，终始无常主。故《系辞》但论四爻功位之通例，而不及初上之定位也。然事不可无终始，卦不可无六爻；初上虽无阴阳本位，是终始之地也。统而论之，爻之所处则谓之位；卦以六爻为成，则不得不谓之六位时成也。

【校勘与边界】
1. 本条四篇正文依四库本页面录入；原页面中邢璹注文、陆德明音义与王弼正文交错。本轮为避免混淆，先抽取王弼正文，未将注、音义伪装成正文，也不宣称注文已经完整。
2. 《明卦适变通爻》篇名存在异文：总目音义记“明卦通变适爻”及“适变通爻”；本条以正文篇题“明卦适变通爻”为主。
3. 《明象》中的“得意在忘象，得象在忘言”等文字按底本录入；引文中的《庄子》筌蹄典故及各卦例不据后世解释擅改。
4. 底本数字文本尚未与原书影印逐字完成校勘；“伪/僞”“辩/辨”“闇/暗”等字形及标点，后续须结合版本校记核查。
5. 本条止于《辩位》篇末“不得不谓之六位时成也”。《略例下》《卦略》及四篇的邢璹注、陆德明音义尚未在本条完整收录，后续继续补录。""",
 
    Entry("历代注疏", "王弼《周易略例》《略例下》与《卦略》十一卦", "关联典籍续录：补录《略例下》及《卦略》所列十一卦。王弼正文、邢璹注与陆德明音义分层标示；底本另附《周易略例考证》，本条不将考证误并入王弼正文。", """【文献性质与底本】
本条续录《周易略例》末两部分。底本为《钦定四库全书》本《周易注疏》附录《周易略例》，页面总目明确列“略例下”“卦略”，并注“卦略，凡十一卦”。本条依页面正文录入，区分王弼正文、邢璹注文及陆德明音义。底本后附《周易略例考证》，涉及监本脱文、异本改字等校勘记录；考证不是王弼原文，另行说明，不混入正文。

【九、略例下】

【王弼正文】
凡体具四德者，则转以胜者为先，故曰“元亨利贞”也。其有先贞而后亨者，亨由于贞也。

凡阴阳者，相求之物也。近而不相得者，志各有所存也。故凡阴阳二爻，率相比而无应，则近而不相得；有应则虽远而相得。

然时有险易，卦有小大，同救以相亲，同辟以相疏。故或有违斯例者也，然存时以考之，义可得也。

凡彖者，统论一卦之体者也；象者，各辩一爻之义者也。故履卦六三为兑之主，以应于乾，成卦之体在斯一爻，故彖叙其应，虽危而亨也。象则各言六爻之义，明其吉凶之行。去六三成卦之体而指说一爻之德，故危不获亨而见咥也。讼之九二，亦同斯义。

凡彖者，通论一卦之体者也。一卦之体必由一爻为主，则指明一爻之美以统一卦之义，大有之类是也；卦体不由乎一爻，则全以二体之义明之，丰卦之类是也。

凡言“无咎”者，本皆有咎者也，防得其道，故得无咎也。“吉无咎”者，本亦有咎，由吉故得免也。“无咎吉”者，先免于咎而后吉从之也。或亦处得其时，吉不待功，不犯于咎，则获吉也。或有罪自己招，无所怨咎，亦曰无咎，故《节》六三曰“不节若则嗟若，无咎”，《象》曰“不节之嗟，又谁咎也”，此之谓矣。

【邢璹注文（按底本分句摘录）】
“元”为生物之始，春也；“亨”为会聚于物，夏也；“利”为和谐品物，秋也；“贞”能干济于物，冬也。乾用此四德，以成君子、大人之法也。
“先贞而后亨”，注以《离》卦“利贞亨”为例，说明亨由于贞。
“近而不相得”，注举《既济》六二与初、三相近而不相得为例；无应则近而不相得，有应则虽远而相得。
“时有险易，卦有小大”，注以否险、泰易，遁小、临大为例。
“同救以相亲”，注举《睽》初九、九四同处体下而相救；“同辟以相疏”，注举《困》初六、九四相避而疏远。
彖统论卦体，象各明一爻之义。注以《履》六三及《讼》九二说明彖、象所论层次不同。
“无咎”“吉无咎”“无咎吉”分别释其语义，并举《乾》九三、《师》卦、《比》初六、《需》九二、《节》六三为例。

【十、卦略】

【王弼正文】
屯：此一卦皆阴爻求阳也。屯难之世，弱者不能自济，必依于强，民思其主之时也。故阴爻皆先求阳，不召自往，马虽班如而犹不废，不得其主，无所冯也。初体阳爻，处首居下，应民所求，合其所望，故大得民也。

蒙：此一卦阴爻亦先求阳。夫阴昧而阳明，阴困童蒙，阳能发之。凡不识者求问识者，识者不求所告；暗者求明，明者不咨于暗，故“童蒙求我，匪我求童蒙”也。故六三先唱则犯于为女，四远于阳则困蒙吝，初比于阳则发蒙也。

履：《杂卦》曰“履不处也”，又曰“履者礼也”。谦以制礼，阳处阴位，谦也，故此一卦皆以阳处阴为美也。

临：此刚长之卦也。刚胜则柔危矣，柔有其德，乃得免咎，故此一卦阴爻虽美，莫过无咎也。

观：观之为义，以所见为美者也，故以近尊为尚，远之为吝。

大过：大过者，栋桡之世也。本末皆弱，栋已桡矣，而守其常，则是危而弗扶，凶之道也。以阳居阴，拯弱之义也，故阳爻皆以居阴位为美。济衰救危，唯在同好，则所赡褊矣，故九四有应则有它吝，九二无应则无不利也。

遁：小人浸长，难在于内，亨在于外，与临卦相对者也。临刚长则柔危，遁柔长故刚遁也。

大壮：未有违谦越礼能全其壮者也，故阳爻皆以处阴位为美。用壮处谦，壮乃全也；用壮处壮，则触藩矣。

明夷：明夷为暗之主在于上六，初最远之，故曰“君子于行”；五最近之而难不能溺，故谓之“箕子之贞”，明不可息也。三处明极而征至暗，故曰“南狩获其大首”也。

睽：睽者，睽而通也。于两卦之极观之，义最见矣。极睽而合，极异而通，故先见怪焉；洽乃疑亡也。

丰：此一卦明以动之卦也，尚于光显宣扬发畅者也，故爻皆以居阳位又不应阴为美。其统在于恶暗而已矣。小暗谓之沛，大暗谓之蔀；暗甚则明尽，未尽则明昧。明尽则斗星见，明微故见昧；无明则无与乎世。见昧则不可以大事，折其右肱，虽左肱在，岂足用乎？日中之盛而见昧而已，岂足任乎？

【邢璹注文与陆德明音义】
- 《屯》注：以江海处下、百川归之，比喻君能下物而万民归之。音义：“难”读乃旦反；“冯”读皮冰反，本亦作“凭”。
- 《蒙》音义：“昧”音妹；“谘”本亦作“资”；“远”读袁万反；“比”读毗志反。
- 《履》注：九五“夬履贞厉”，因履道恶盈而五处尊位；三居阳位则见咥。音义说明《杂卦》今本无“履者礼也”句，韩注本有，或为传写误。
- 《临》音义：“长”读丈反；并提示与《遁》卦相关。
- 《观》注：远为“童观”，近为“观国”。音义：“观”读古乱反；“见”一本作“知”。
- 《大过》注：大过之时，阳处阴位、心无系应为吉；阳得位而有应则凶。音义：“桡”读乃孝反，“拯”即拯救，“好”读呼报反，“赡”读常艳反，“褊”读必浅反。
- 《遁》注：遁以远时为吉、不系为美；上九肥遁，初六有厉。音义：“浸”读子鸩反，“长”读张丈反，“难”读乃旦反，“亨”读许庚反。
- 《大壮》音义：“触”读昌录反，“藩”读扶袁反。
- 《明夷》注：远难以藏明，是明夷之义。音义：“远”读于万反，“近”读附近之近，“难”读乃旦反，“溺”读宁历反。
- 《睽》注：火动而上、泽动而下，睽之义由此可见。音义：“见”读贤遍反；“洽”读咸夹反，本又作“合”。
- 《丰》注：丰之义贵在光大，恶于暗昧。音义记录“沛”“蔀”“昧”等字读音及“妹”“沬”等异体。

【底本校勘提示】
1. 四库本页面所附《周易略例考证》说明，校者曾据相台岳氏荆溪家塾本、汉魏丛书、津逮秘书等参订监本脱讹。该考证指出《略例下》“其有先贞而后亨者”一句，监本脱“后亨”二字，依秘书本补；此处按四库页面校订文字录入，并保留校勘说明。
2. 《卦略》末《睽》条“洽乃疑亡”，考证称监本“洽”讹作“始”，依丛书本改；本条从校订后的“洽”字。
3. 《卦略》共十一卦，依总目及正文逐项录入：屯、蒙、履、临、观、大过、遁、大壮、明夷、睽、丰。未将其余未列卦象擅自补入。
4. 《蒙》条“凡不识者求问识者，识者不求所告”按四库本页面连续字序重新断句。原页面连续字样为“凡不識者求問識者識者不求所告闇者求明”，前一“識者”是“求问”的宾语，后一“識者”是下一分句主语；并非衍文。本轮修正此前数字稿误加标点造成的“识者识者不求；所告暗者求明”误读，不删原字。参见四库本页面《卦略》蒙条。其余繁简、异体字及标点仍未完成逐字影印校勘。
5. 本条补齐《周易略例》目录所列最后两部分，但并不意味着全书所有注文、音义及各版本校记已完成影印级校勘；后续仍须逐条核对底本与其他版本。""",
        chatGptTranslation = """【ChatGPT译文】
【文献性质与底本】
本条接续《周易略例》，收录其末尾的《略例下》和《卦略》。四库本目录明确列出这两个部分，并注明《卦略》共十一卦。录入时区分王弼正文、邢璹注文和陆德明音义。书后另附《周易略例考证》，其中的脱文、异文说明属于校勘者意见，不是王弼正文。

【九、《略例下》——王弼正文】
凡是一个卦体同时具备元、亨、利、贞四种德性的，解释时就应把其中占主导地位的一项放在前面，所以卦辞说“元亨利贞”。也有先说贞、后说亨的情况，因为亨通是由守正而来的。
阴爻和阳爻本来就是彼此寻求、相互感应的。若两爻距离很近却不能相得，是因为双方各有自己的志向。因此，阴阳两爻虽然通常相邻，但若没有正应，近处也可能不能相得；如果彼此有应，即使相隔较远，也能相互感通。
时势有险阻和平易，卦有大小之分；处境相同、共同救助时便会亲近，共同避开某事时便会疏远。因此有些卦例会违背通常的规律，但只要结合当时的时势考察，仍然可以理解其中的道理。
彖是统括全卦整体的，象则分别辨明每一爻的意义。比如履卦六三是兑卦的主爻，又与乾卦相应，整卦的结构由这一爻形成，所以彖辞叙述它与乾相应，虽有危险仍能亨通。象辞则分别说明六爻各自的意义，指出吉凶如何表现。若不从六三这一整卦关键处着眼，而只单独讨论某一爻的品德，就会发现它处境危险，不能获得亨通，反而遭到老虎咬伤。讼卦九二也有类似道理。
彖通常通论全卦整体。若全卦确实由某一爻作为主导，就指出这一爻的美德来统摄全卦，例如大有卦；如果卦体并非由单独一爻决定，就应从上下两个卦体的意义来说明，例如丰卦。
凡说“无咎”，原本都存在可能招致过失的条件，只是因为采取了正确方法，才得以避免咎害。“吉无咎”是说原本也可能有咎，但因结果吉利而免于咎害；“无咎吉”则是先免除过失，随后吉祥才到来。有时是因为所处时机恰当，不必特别建立功劳便能获吉，也没有触犯祸端；有时则是过失由自己招致，不应怨怪别人，也称为“无咎”。节卦六三说“不节若则嗟若，无咎”，象辞又说“不节之嗟，又谁咎也”，说的就是这种情况。

【邢璹注文摘录的现代解释】
“元、亨、利、贞”分别对应春、夏、秋、冬：元是万物开始生长，亨是万物会聚成长，利是万物和谐成熟，贞是万物得以坚固成就。乾卦以四德说明君子和大人的行为准则。
先贞而后亨，是说亨通建立在守正基础上。注文以离卦“利贞亨”为例。
近而不相得，注文以既济卦六二与相邻爻不能相得为例；无应时虽近也不相得，有应时即使远也能相得。
时有险易、卦有大小，注文以否卦为险、泰卦为易，以遁卦为小、临卦为大作说明。
同处困境而相互救助，就会亲近；共同避开某事，则会疏远。注文分别以睽卦和困卦的爻关系举例。
彖统论一卦，象分说各爻。注文以履卦六三和讼卦九二说明两者的论述层次不同。
“无咎”“吉无咎”“无咎吉”分别有不同语义，注文又以乾九三、师卦、比卦初六、需卦九二和节卦六三等例子解释。

【十、《卦略》——王弼正文】
屯卦：全卦阴爻都在寻求阳爻。屯难时代，弱者无法独自渡过困境，必须依靠强者，正是百姓思念其君主的时候。因此阴爻都先求阳爻，不待召唤便主动前往；即使马匹徘徊不前，也仍然不放弃，因为没有找到可以依靠的主导者。初九是阳爻，处在卦首和下位，回应百姓的期待，符合众人的愿望，所以能得到广泛拥护。
蒙卦：这一卦的阴爻也主动求阳。阴暗而阳明，蒙昧者受困于无知，阳明者能够启发他们。不了解的人向了解的人求教，明白的人不会反过来向蒙昧者求教；暗者求明，不会是明者向暗者请教。因此说“童蒙求我，匪我求童蒙”。六三主动求进会有失少女之礼，六四远离阳爻所以困于蒙昧，初六靠近阳爻则能得到启发。
履卦：《杂卦》说“履不处”，又说“履者礼也”。谦逊是建立礼的基础；阳爻居于阴位，体现谦逊，所以本卦以阳爻居阴位为美。
临卦：这是阳刚逐渐增长的卦。刚强占优势时，柔弱者容易陷入危险；柔弱者必须具备相应德行，才能免于咎害。因此本卦的阴爻即使有美德，最高评价也不过是“无咎”。
观卦：观的意义在于观察所见，因此靠近尊位被视为较好，远离尊位则容易陷于“童观”之吝。
大过卦：大过表示栋梁弯曲、局势危急的时代。根部和末端都柔弱，栋梁已经弯曲，若仍固守常态而不加扶持，就会走向危险。阳爻居阴位，表示以刚强力量扶助柔弱，所以阳爻居阴位为美。挽救衰败危局，需要同心协力；若有所应，反而可能受到牵制，因此九四有应则有其他悔吝，九二无应则没有不利。
遁卦：小人逐渐增长，困难发生在内部，亨通则在外部；它与临卦相对。临卦是阳刚增长、柔弱受危，遁卦则是柔弱增长、刚强退避。
大壮卦：不违背谦逊、不超越礼法，才能真正保持强盛。因此阳爻以居阴位为美；强而能谦，力量才能保全；强而又居强位，就会像羊角触碰藩篱一样受阻。
明夷卦：明夷之暗的主导者在上六。初九距离它最远，所以说“君子于行”；六五离它最近，却没有被黑暗吞没，所以称“箕子之贞”，说明光明仍未熄灭。九三处在光明极盛之处，却要前往最黑暗的地方，所以说“南狩获其大首”。
睽卦：睽是表面乖离而内在仍可能相通。观察两个卦体走到极处的情形，就最能看出这一意义：分离到了极点可能重新结合，差异到了极点也可能重新相通，因此开始时会出现怪异现象，融洽之后疑虑便会消失。
丰卦：丰是光明而行动的卦，崇尚光明显扬、充分发挥，所以各爻以居阳位且不与阴爻相应为美。全卦的主旨在于厌恶黑暗。轻微的昏暗称“沛”，严重的昏暗称“蔀”；黑暗太深，光明便会消失；尚未完全消失，则光明仍然昏昧。光明完全消失时只能看到斗星，光线微弱时则会显得昏暗；没有光明，就无法参与世事。看见昏暗时不适合承担大事；右臂折断，即使左臂还在，也不足以承担重任。正午本应最为光盛，却仍可能出现昏暗，当然不能承担重任。

【邢璹注文与陆德明音义】
《屯》注文用江海处在低下位置、百川因此汇归作比喻，说明君主若能谦下待人，百姓便会归附。音义记录“难”“冯”等字的读音，并说明“冯”另有写作“凭”的版本。
《蒙》的音义解释“昧”“谘”“远”“比”等字的读音和异文。
《履》注文解释九五“夬履贞厉”，认为履礼之道忌讳自满；九五居尊位，六三居阳位，因而各有不同吉凶。音义指出，《杂卦》今本没有“履者礼也”一句，韩康伯注本却有，可能是传写差异。
《临》的音义记录“长”的读音，并提示与遁卦相关。
《观》注文以远处的“童观”和近处的“观国”解释观察位置的差异。音义记录“观”“见”的读音与异文。
《大过》注文认为，大过之时，阳爻居阴位、心中没有牵系应援，反而可能吉利；阳爻居正位且有所应，则可能不利。音义解释“桡”“拯”“好”“赡”“褊”等字。
《遁》注文认为，遁退以远离时势为吉，以不受牵系为美，并举上九“肥遁”和初六“有厉”为例。音义解释“浸”“长”“难”“亨”等字。
《大壮》的音义解释“触”“藩”的读音。
《明夷》注文认为，远离危险才能保存光明。音义解释“远”“近”“难”“溺”等字。
《睽》注文以火向上、泽向下说明相背之象。音义解释“见”“洽”的读音，并指出“洽”另有写作“合”的情况。
《丰》注文强调光明盛大，反对昏暗。音义记录“沛”“蔀”“昧”等字的读音和“妹”“沬”等异体。

【底本校勘提示】
四库本附载的《周易略例考证》说明，校者曾参照相台岳氏荆溪家塾本、《汉魏丛书》和《津逮秘书》等版本，订正监本的脱文和讹字。《略例下》中“其有先贞而后亨者”一句，监本漏掉“后亨”二字，依《津逮秘书》补入；本条依校订后的文字录入。
《卦略》睽卦“洽乃疑亡”一句，考证称监本把“洽”误作“始”，依《汉魏丛书》改正；本条采用校订后的“洽”字。
《卦略》共十一卦，本条只录目录所列的屯、蒙、履、临、观、大过、遁、大壮、明夷、睽、丰等内容，不擅自补入其他卦。
蒙卦条目中连续出现的“识者”，前一个是“求问”的宾语，后一个是下一分句的主语。应断为“凡不识者求问识者，识者不求所告；暗者求明”，不能因标点误读而删改原字。
以上校记属于版本说明，不是王弼正文；繁简、异体字和标点仍有待与影印本逐字核对。本条补录《略例下》和《卦略》，不代表全书注文、音义和版本校勘都已完成。"""),
。言语好比象的工具，象好比意义的工具。
因此，只保存言语，并不等于真正得到象；只保存象，也不等于真正得到意义。象由意义产生，如果只执着于象，所保存的就不再是象真正要表达的东西；言语由象产生，如果只执着于言语，所保存的也不再是言语真正要表达的东西。所以忘掉象才能得到意义，忘掉言语才能得到象。得到意义的关键在于忘象，得到象的关键在于忘言。
设立卦象是为了充分表达意义，意义表达出来后，象便可以放下；重画卦爻是为了充分表现情状，情状表达出来后，画也可以放下。因此，可以根据事物类别取象，也可以根据义理相合之处作为征验。如果义理在于刚健，为什么一定要取象为马？如果义理在于柔顺，为什么一定要取象为牛？所以某些象被固定系于乾坤，并不是绝对规则。若不根据经文考察卦义，只要出现马就硬说必有乾卦，牵强附会的说法便会越来越多。
互体解释不足，就转而使用卦变；卦变仍不足，又推演五行。只要一开始偏离根本，巧饰的解释就会愈加繁复。即使偶然说中某个现象，也未必具有真正的义理价值。这正是只保存象而忘记意义所导致的问题。放下对表象的执着，转而探求其意义，义理才会显现。

【八、《辩位》】
考察卦象，并没有把初爻、上爻明确规定为得位或失位；《系辞》只讨论三与五、二与四同功而位置不同，也没有讨论初爻和上爻。这是为什么？只有乾卦上九《文言》说“贵而无位”，需卦上六说“虽不当位”。如果把上爻规定为阴位，那么需卦上六就不该说“不当位”；如果把上爻规定为阳位，那么乾卦上九也不该说“贵而无位”。阴爻、阳爻处在上爻，都可以说不在固定之位；初爻也没有被明确规定为当位或失位。
由此可知，初爻和上爻代表事情的开始与终结，并没有固定的阴阳定位。因此乾卦初九称为“潜”，超过九五之后又称“无位”；没有人处在一个固定位置却被称为“潜”，也没有人明明居于固定位置却被说成“无位”。考察其他卦，也大体如此。初爻和上爻没有固定的阴阳位置，正是为了表示始终。
所谓“位”，是安排贵贱等级的地方，也是才能发挥作用的处所；所谓“爻”，是承担相应位置职责、遵循贵贱秩序的角色。位置有尊卑，爻有阴阳：尊位由阳爻居之，卑位由阴爻居之。因此尊位称阳位，卑位称阴位。如果排除初爻和上爻，只讨论中间四爻，那么三、五都处在卦体上部，当然可以称为阳位；二、四处在卦体下部，当然可以称为阴位。
初爻和上爻表示整体的开始与终结、事情的先后，所以它们没有固定的位置分界，所处事务也没有固定类别，不能用阴阳来确定。尊卑有恒定次序，始终却没有固定的主位。因此《系辞》只讨论中间四爻的功用与位置，没有为初、上两爻设定固定的阴阳位。事情不能没有开始和结束，卦也不能没有六爻；初爻和上爻虽然没有固定的阴阳本位，却仍是卦的始终之处。总括而言，爻所处的位置就叫作“位”；卦由六爻构成，因此称为“六位时成”。

【校勘与边界】
四篇正文按四库本数字页面整理。原页面中注文、音义与正文交错，本条只抽取王弼正文，不代表注释材料已完整。篇名异文继续保留；“得意在忘象，得象在忘言”等文字依底本录入。数字文本尚未与影印原页逐字核校，繁简、异体字和标点仍待检查。本条止于《辩位》篇末，不包括《略例下》《卦略》及各篇完整注文。"""),
近远终始，各存其会；辟险尚远，趋时贵近。比复好先，乾壮恶首，明夷务闇，丰尚光大。

吉凶有时，不可犯也；动静有适，不可过也。犯时之忌，罪不在大；失其所适，过不在深。动天下，灭君主，而不可危也；侮妻子，用颜色，而不可易也。故当其列贵贱之时，其位不可犯也；遇其忧悔吝之时，其介不可慢也。观爻思变，变斯尽矣。

夫体义者在乎象，变通者存乎爻。大人者，形与万物接，神与造化偕。入则乘潜龙，出则乘飞龙；远以观道，迩以瞻器，其数无轨而不可既也。君子者，健以行志，非以神明变通者也。故自强不息，以行其志，君子之道也；唯可与于健之体义，而未可与于健之变通也。

故于象称君子，举其体义者也；于爻称大人，叙其变通者也。或有在爻而称大人者，义则尽于象故也。若离之为明，其何不照？变而通之，无所不造。故其象云：“大人继明，照于四方”也。或有在爻而称大人者，义有待于变故也。若否卦全说君子避难，不荣于禄，与时通塞，可以适否而安其身，未足兼善天下，在否而休也。唯大人为能休天地之否，达济凝滞，开阐幽门，斯谓变否而亨者也。故六二云“大人否亨”，而九五云“大人休否”也。

夫象赡而爻不足者，离卦之类是也，义尽于象而不待于爻也。爻备而象不具者，否卦之类是也，象所未尽而待爻以备。故象以体示，而爻以变明。存斯以考其义，则穷通之旨显，而开塞之涂见矣。

夫彖者，全论一卦之体；而象者，各举一爻之义。一卦所无，世之遗也；爻或有焉，才之所生也。故才生于世所不足，观其逆顺，以定得失之占；察其乖合，以明休咎之征。若小畜卦称“密云不雨”，而上九爻云“既雨”，既处在于不雨之时，而独以能雨为德者，存乎物之所欲也。若履卦称“履虎尾，不咥人，亨”，而六三爻云“履虎尾，咥人，凶”，在于履亨之时，而独以见咥为凶者，存乎众之所忌也。夫负世之才，虽小无易；适时之望，虽大无难。是以君子洗心，知终将有为也。问焉而动，每居其适，斯可以旁通而无际矣。

【七、明象】
夫象者，出意者也；言者，明象者也。尽意莫若象，尽象莫若言。言生于象，故可寻言以观象；象生于意，故可寻象以观意。意以象尽，象以言著。

故言者所以明象，得象而忘言；象者所以存意，得意而忘象。既得龙象，其言可忘；既得乾意，其龙可舍。犹蹄者所以在兔，得兔而忘蹄；筌者所以在鱼，得鱼而忘筌也。然则言者象之蹄也，象者意之筌也。

是故存言者非得象者也，存象者非得意者也。象生于意而存象焉，则所存者乃非其象也；言生于象而存言焉，则所存者乃非其言也。然则忘象者乃得意者也，忘言者乃得象者也。得意在忘象，得象在忘言。

故立象以尽意，而象可忘也；重画以尽情，而画可忘也。是故触类可为其象，合义可为其征。义苟在健，何必马乎？类苟在顺，何必牛乎？故或有以某象系于乾坤者，皆非定也。案文责卦，有马无乾，则伪说滋漫，难可纪矣。

互体不足，遂及卦变；变又不足，推致五行。一失其原，巧愈弥甚。纵复或值，而义无所取，盖存象忘意之由也。忘象以求其意，义斯见矣。

【八、辩位】
案象无初上得位失位之文；《系辞》但论三五二四同功异位，亦不及初上，何乎？唯乾上九《文言》云“贵而无位”，需上六云“虽不当位”。若以上为阴位邪，则需上六不得云“不当位”也；若以上为阳位邪，则乾上九不得云“贵而无位”也。阴阳处之，皆云非位，而初亦不说当位失位也。

然则初上者，是事之终始，无阴阳定位也。故乾初谓之“潜”，过五谓之“无位”，未有处其位而云“潜”，上有位而云“无”者也。历观众卦，尽亦如之，初上无阴阳定位，亦以明矣。

夫位者，列贵贱之地，待才用之宅也。爻者，守位分之任，应贵贱之序者也。位有尊卑，爻有阴阳；尊者阳之所处，卑者阴之所履也。故以尊为阳位，卑为阴位。去初上而论位分，则三五各在一卦之上，亦何得不谓之阳位？二四各在一卦之下，亦何得不谓之阴位？

初上者，体之终始，事之先后也，故位无常分，事无常所，非可以阴阳定也。尊卑有常序，终始无常主。故《系辞》但论四爻功位之通例，而不及初上之定位也。然事不可无终始，卦不可无六爻；初上虽无阴阳本位，是终始之地也。统而论之，爻之所处则谓之位；卦以六爻为成，则不得不谓之六位时成也。

【校勘与边界】
1. 本条四篇正文依四库本页面录入；原页面中邢璹注文、陆德明音义与王弼正文交错。本轮为避免混淆，先抽取王弼正文，未将注、音义伪装成正文，也不宣称注文已经完整。
2. 《明卦适变通爻》篇名存在异文：总目音义记“明卦通变适爻”及“适变通爻”；本条以正文篇题“明卦适变通爻”为主。
3. 《明象》中的“得意在忘象，得象在忘言”等文字按底本录入；引文中的《庄子》筌蹄典故及各卦例不据后世解释擅改。
4. 底本数字文本尚未与原书影印逐字完成校勘；“伪/僞”“辩/辨”“闇/暗”等字形及标点，后续须结合版本校记核查。
5. 本条止于《辩位》篇末“不得不谓之六位时成也”。《略例下》《卦略》及四篇的邢璹注、陆德明音义尚未在本条完整收录，后续继续补录。"""),

    Entry("历代注疏", "王弼《周易略例》《略例下》与《卦略》十一卦", "关联典籍续录：补录《略例下》及《卦略》所列十一卦。王弼正文、邢璹注与陆德明音义分层标示；底本另附《周易略例考证》，本条不将考证误并入王弼正文。", """【文献性质与底本】
本条续录《周易略例》末两部分。底本为《钦定四库全书》本《周易注疏》附录《周易略例》，页面总目明确列“略例下”“卦略”，并注“卦略，凡十一卦”。本条依页面正文录入，区分王弼正文、邢璹注文及陆德明音义。底本后附《周易略例考证》，涉及监本脱文、异本改字等校勘记录；考证不是王弼原文，另行说明，不混入正文。

【九、略例下】

【王弼正文】
凡体具四德者，则转以胜者为先，故曰“元亨利贞”也。其有先贞而后亨者，亨由于贞也。

凡阴阳者，相求之物也。近而不相得者，志各有所存也。故凡阴阳二爻，率相比而无应，则近而不相得；有应则虽远而相得。

然时有险易，卦有小大，同救以相亲，同辟以相疏。故或有违斯例者也，然存时以考之，义可得也。

凡彖者，统论一卦之体者也；象者，各辩一爻之义者也。故履卦六三为兑之主，以应于乾，成卦之体在斯一爻，故彖叙其应，虽危而亨也。象则各言六爻之义，明其吉凶之行。去六三成卦之体而指说一爻之德，故危不获亨而见咥也。讼之九二，亦同斯义。

凡彖者，通论一卦之体者也。一卦之体必由一爻为主，则指明一爻之美以统一卦之义，大有之类是也；卦体不由乎一爻，则全以二体之义明之，丰卦之类是也。

凡言“无咎”者，本皆有咎者也，防得其道，故得无咎也。“吉无咎”者，本亦有咎，由吉故得免也。“无咎吉”者，先免于咎而后吉从之也。或亦处得其时，吉不待功，不犯于咎，则获吉也。或有罪自己招，无所怨咎，亦曰无咎，故《节》六三曰“不节若则嗟若，无咎”，《象》曰“不节之嗟，又谁咎也”，此之谓矣。

【邢璹注文（按底本分句摘录）】
“元”为生物之始，春也；“亨”为会聚于物，夏也；“利”为和谐品物，秋也；“贞”能干济于物，冬也。乾用此四德，以成君子、大人之法也。
“先贞而后亨”，注以《离》卦“利贞亨”为例，说明亨由于贞。
“近而不相得”，注举《既济》六二与初、三相近而不相得为例；无应则近而不相得，有应则虽远而相得。
“时有险易，卦有小大”，注以否险、泰易，遁小、临大为例。
“同救以相亲”，注举《睽》初九、九四同处体下而相救；“同辟以相疏”，注举《困》初六、九四相避而疏远。
彖统论卦体，象各明一爻之义。注以《履》六三及《讼》九二说明彖、象所论层次不同。
“无咎”“吉无咎”“无咎吉”分别释其语义，并举《乾》九三、《师》卦、《比》初六、《需》九二、《节》六三为例。

【十、卦略】

【王弼正文】
屯：此一卦皆阴爻求阳也。屯难之世，弱者不能自济，必依于强，民思其主之时也。故阴爻皆先求阳，不召自往，马虽班如而犹不废，不得其主，无所冯也。初体阳爻，处首居下，应民所求，合其所望，故大得民也。

蒙：此一卦阴爻亦先求阳。夫阴昧而阳明，阴困童蒙，阳能发之。凡不识者求问识者，识者不求所告；暗者求明，明者不咨于暗，故“童蒙求我，匪我求童蒙”也。故六三先唱则犯于为女，四远于阳则困蒙吝，初比于阳则发蒙也。

履：《杂卦》曰“履不处也”，又曰“履者礼也”。谦以制礼，阳处阴位，谦也，故此一卦皆以阳处阴为美也。

临：此刚长之卦也。刚胜则柔危矣，柔有其德，乃得免咎，故此一卦阴爻虽美，莫过无咎也。

观：观之为义，以所见为美者也，故以近尊为尚，远之为吝。

大过：大过者，栋桡之世也。本末皆弱，栋已桡矣，而守其常，则是危而弗扶，凶之道也。以阳居阴，拯弱之义也，故阳爻皆以居阴位为美。济衰救危，唯在同好，则所赡褊矣，故九四有应则有它吝，九二无应则无不利也。

遁：小人浸长，难在于内，亨在于外，与临卦相对者也。临刚长则柔危，遁柔长故刚遁也。

大壮：未有违谦越礼能全其壮者也，故阳爻皆以处阴位为美。用壮处谦，壮乃全也；用壮处壮，则触藩矣。

明夷：明夷为暗之主在于上六，初最远之，故曰“君子于行”；五最近之而难不能溺，故谓之“箕子之贞”，明不可息也。三处明极而征至暗，故曰“南狩获其大首”也。

睽：睽者，睽而通也。于两卦之极观之，义最见矣。极睽而合，极异而通，故先见怪焉；洽乃疑亡也。

丰：此一卦明以动之卦也，尚于光显宣扬发畅者也，故爻皆以居阳位又不应阴为美。其统在于恶暗而已矣。小暗谓之沛，大暗谓之蔀；暗甚则明尽，未尽则明昧。明尽则斗星见，明微故见昧；无明则无与乎世。见昧则不可以大事，折其右肱，虽左肱在，岂足用乎？日中之盛而见昧而已，岂足任乎？

【邢璹注文与陆德明音义】
- 《屯》注：以江海处下、百川归之，比喻君能下物而万民归之。音义：“难”读乃旦反；“冯”读皮冰反，本亦作“凭”。
- 《蒙》音义：“昧”音妹；“谘”本亦作“资”；“远”读袁万反；“比”读毗志反。
- 《履》注：九五“夬履贞厉”，因履道恶盈而五处尊位；三居阳位则见咥。音义说明《杂卦》今本无“履者礼也”句，韩注本有，或为传写误。
- 《临》音义：“长”读丈反；并提示与《遁》卦相关。
- 《观》注：远为“童观”，近为“观国”。音义：“观”读古乱反；“见”一本作“知”。
- 《大过》注：大过之时，阳处阴位、心无系应为吉；阳得位而有应则凶。音义：“桡”读乃孝反，“拯”即拯救，“好”读呼报反，“赡”读常艳反，“褊”读必
    Entry("历代注疏", "《周易略例考证》四库本逐条校记", "版本校勘资料：据四库本页面所附《周易略例考证》，逐条录入监本与相台岳氏荆溪家塾本、汉魏丛书、津逮秘书之间的异文、脱文和补字说明。此为清代校者考证，不是王弼正文，也不是邢璹注文。", """【文献性质】
本条专录四库本页面末尾《周易略例考证》。考证者自述以相台岳氏所梓荆溪家塾本、汉魏丛书、津逮秘书等参订监本，并将异同逐条标出。以下按页面次序转录。校记中“监本”指其所校监本；“荆溪本”“秘书本”“丛书本”等均依原称保留，不擅自扩写版本信息。

【考证总说】
臣良裘按：略例一册，乃王氏易注之纲领。《隋书·经籍志》所谓《周易》十卷，盖并此数之，故《释文》云“或有题为第十者”。孔氏作疏，主申王学而不疏略例，殊失体要；至坊刻注疏本乃削去之，谬妄甚矣。监本虽附见卷末，而序目不全，字句多讹。今据相台岳氏所梓荆溪家塾本及《汉魏丛书》《津逮秘书》所录，稍为参订，有同异者著之。

【《明彖》校记】
1. “明其所由之主也”下注“明辩也，辩卦体功用所由之主”：监本脱“明辩也”三字，依《汉魏丛书》本添。
2. “贞夫一者也”下注“制其动者贞正之一者也”：“正”字，荆溪家塾本无。
3. “主必致一也”下注“故无心于存皆得其存也”：监本脱此二句，依《津逮秘书》本添。
4. “则知可以执一御也”下注“无为之一者道也君也”：监本脱此九字，依荆溪本添。
5. “或有遗爻而举二体者卦体不由乎爻也”下注“弃此一爻而举二体以明其义”：“弃此一爻”，秘书本作“弃此中之一爻”。
6. “其孰能与于此乎”：“乎”字，监本脱，依丛书本添。

【《明爻通变》校记】
7. “故合散屈伸与体相乖”下注“人之多辟己独处正”：“处”字，荆溪本作“取”。
8. “召云者龙命吕者律”下注“召水气者水畜此明有识感无识命阴吕者阳律此明无识感有识”：“无识感有识”句诸本皆同，监本作“无识感无识”。
9. “异而知其通”下注“男女睽而其志同也”：“同”字，秘书本作“通”；《睽》彖传本作“通”。
10. “语成而后有格”：《丛书》本作“语成器而后有格”。
11. “鼓舞而天下从者见乎其情者也”：“从”字下监本脱“者”字，依丛书本添。
12. “一阴一阳而无穷”下注“道者虚无也”：“无”字，监本脱，依荆溪本添。

【《明卦适变通爻》校记】
13. “故卦以反对而爻亦有变”下注“爻变亦准也”：“准”字，监本作“应”，依荆溪本改。
14. “唯变所适”下注“卦既推移故道用无常”：“既”字，监本作“虽”；“道”字上脱“故”字，依秘书本添。
15. “险易之象也”下注“需卦九三近坎险也”：“坎”字，监本作“难”，依荆溪本改。
16. “物竞而独安于静者要其终也”：“于”字，监本脱，依秘书本添。
17. “辩逆顺者存乎承乘”下注“阴乘乎阳逆也”：“诸本多作‘阳乘乎阴’，误也，今依荆溪本。”

【《明象》校记】
18. “言生于象故可寻言以观象，象生于意故可寻象以观意”：两个“生”字，监本并作“主”字，依秘书本改。
19. “得意而忘象”下注“既得乾意其龙可舍”：“意”字，监本讹作“象”，依秘书本改。
20. “存言者非得象者也”下注“未得象者存言言则非象”：“则”字，监本讹作“有”；下句“象则非意”同，俱依荆溪本改。

【《辩位》校记】
21. “乾上九贵而无位”下注“阳居之也”；“需上六虽不当位”下注“阴居之也”：“阳”字监本讹作“阴”，“阴”字监本讹作“阳”，俱依荆溪本改。
22. “应贵贱之序者也”下注“各守其位应之以序”：“序”字，监本作“事”，依丛书本改。

【《略例下》校记】
23. “其有先贞后亨者亨由于贞也”：“后亨”二字，监本脱，依秘书本添。

【《卦略》校记】
24. “洽乃疑亡也”：“洽”字，监本讹作“始”，依丛书本改。

【录入边界】
1. 上述校记是《周易略例考证》的校者按语，不是王弼或邢璹正文。
2. 版本简称依底本原文保留；尚未取得各版本对应影印页逐字核验，不能仅凭校记推定所有版本的完整谱系。
3. 本条统一用便于检索的简体字呈现；“无/无”“辩/辨”等字形差异不在转录时擅自改写原校记的判断。
4. 校记编号为本条阅读序号，非原书编号。""",
        chatGptTranslation = """【ChatGPT译文】
【文献性质】
本条收录四库本《周易略例》页面末尾的《周易略例考证》。校者说明，自己以相台岳氏所刻荆溪家塾本、《汉魏丛书》和《津逮秘书》等版本参校监本，并逐条记录不同版本的异同。以下保留原有版本简称，不擅自扩充其含义。

【考证总说】
校者良裘认为，《略例》是王弼易学注释的纲领。《隋书·经籍志》所说的《周易》十卷，可能把《略例》也计算在内，因此《经典释文》说，有些版本把它题作“第十”。孔颖达作《周易正义》时，主要阐发王弼的学说，却没有为《略例》作疏，这在体例上有所缺失。后来坊刻注疏本甚至删去了《略例》，更是错误。监本虽然把它附在卷末，但序文和目录不完整，文字也有不少错误。现在依据相台岳氏荆溪家塾本及《汉魏丛书》《津逮秘书》所收文本作适当校订，并把不同之处记录下来。

【《明彖》校记】
1. “明其所由之主也”下面的注释，监本漏掉“明辩也”三个字，依据《汉魏丛书》本补入。
2. “贞夫一者也”下面的注释，“制其动者贞正之一者也”中的“正”字，荆溪家塾本没有。
3. “主必致一也”下面的注释，监本漏掉“故无心于存皆得其存也”两句，依据《津逮秘书》本补入。
4. “则知可以执一御也”下面的注释，监本漏掉“无为之一者道也君也”九字，依据荆溪本补入。
5. “或有遗爻而举二体者卦体不由乎爻也”下面的注释，“弃此一爻”在秘书本作“弃此中之一爻”。
6. “其孰能与于此乎”一句，监本漏掉“乎”字，依据丛书本补入。

【《明爻通变》校记】
7. “故合散屈伸与体相乖”下面的注释，“人之多辟己独处正”中的“处”字，荆溪本作“取”。
8. “召云者龙命吕者律”下面的注释，各本都作“无识感有识”，监本却误作“无识感无识”。
9. “异而知其通”下面的注释，“男女睽而其志同也”中的“同”字，秘书本作“通”；《睽》彖传原文也作“通”。
10. “语成而后有格”一句，《汉魏丛书》本作“语成器而后有格”。
11. “鼓舞而天下从者见乎其情者也”一句，监本在“从”字后漏掉“者”字，依据丛书本补入。
12. “一阴一阳而无穷”下面的注释，“道者虚无也”中的“无”字，监本漏掉，依据荆溪本补入。

【《明卦适变通爻》校记】
13. “故卦以反对而爻亦有变”下面的注释，“爻变亦准也”中的“准”字，监本作“应”，依据荆溪本改为“准”。
14. “唯变所适”下面的注释，“卦既推移故道用无常”中的“既”字，监本作“虽”；“道”字之前还漏了“故”字，依据秘书本补入。
15. “险易之象也”下面的注释，“需卦九三近坎险也”中的“坎”字，监本作“难”，依据荆溪本改为“坎”。
16. “物竞而独安于静者要其终也”一句，监本漏掉“于”字，依据秘书本补入。
17. “辩逆顺者存乎承乘”下面的注释，监本及部分版本写作“阳乘乎阴”，校者认为有误，依据荆溪本采用“阴乘乎阳”。

【《明象》校记】
18. “言生于象故可寻言以观象，象生于意故可寻象以观意”一句，监本把两个“生”字都写成“主”，依据秘书本改正。
19. “得意而忘象”下面的注释，“既得乾意其龙可舍”中的“意”字，监本误作“象”，依据秘书本改正。
20. “存言者非得象者也”下面的注释，“未得象者存言言则非象”中的“则”字，监本误作“有”；下句“象则非意”也有相同问题，均依据荆溪本改正。

【《辩位》校记】
21. “乾上九贵而无位”下注原应为“阳居之也”，“需上六虽不当位”下注原应为“阴居之也”；监本把“阳”和“阴”颠倒了，依据荆溪本改正。
22. “应贵贱之序者也”下面的注释，“各守其位应之以序”中的“序”字，监本作“事”，依据丛书本改为“序”。

【《略例下》校记】
23. “其有先贞后亨者亨由于贞也”一句，监本漏掉“后亨”二字，依据秘书本补入。

【《卦略》校记】
24. “洽乃疑亡也”一句，监本把“洽”字误刻为“始”，依据丛书本改正。

【录入边界】
以上内容是校勘者对不同版本的按语，并非王弼正文，也不是邢璹注文。版本简称按原文保留。由于尚未取得各版本对应的影印页面逐字核对，不能仅凭这份校记推定所有版本之间的完整传承关系。当前简体字仅为便于检索，不能据此消除原校记所记录的字形差异。编号是本条整理时使用的阅读序号，不是原书原有编号。"""),

邢璹注：“或有情伪生，违此例者；存其时考其验，莫不得之。”
释义：相邻、相应的通常规则并非机械不变，须结合具体时势判断。

（四）“凡彖者，统论一卦之体者也；象者，各辩一爻之义者也”
邢璹注：“彖统论卦体，象各明一爻之义。”
“故履卦六三为兑之主，以应于乾，成卦之体在斯一爻。”
邢璹注：“彖云‘柔履刚，说而应乎乾’，是以‘履虎尾，不咥人，亨’也。”
“象则各言六爻之义，明其吉凶之行。”
邢璹注：“六三履虎尾，咥人凶。彖言不咥，象言见咥，明爻、彖其义各异也。”
陆德明音义：“行”，下孟反；“去”，羌吕反；“咥”，直结反。
“讼之九二，亦同斯义。”
邢璹注：“讼彖云‘有孚窒惕，中吉’，注云‘其在二乎，以刚而来正夫群小，断不失中，应斯任矣’。九二‘不克讼，归而逋，其邑人三百户，无眚’。”
释义：彖辞统论卦体及成卦主爻，爻象则分别说明各爻处境，二者所论角度不同。

（五）“凡彖者，通论一卦之体者也”
王弼正文说明：一卦之体若由一爻为主，则举该爻之美以统全卦，如《大有》；若不由一爻为主，则以上下二体之义说明，如《丰》。
本段页面未见另列独立音义条目；不得据前后注文推补。

（六）“凡言无咎者，本皆有咎者也，防得其道，故得无咎也”
邢璹注：“乾之九三，君子终日乾乾，无咎。若防失其道，则有过咎也。”
“吉无咎者，本亦有咎，由吉故得免也。”
邢璹注：“师贞丈人吉无咎。注云：兴役动众，无功罪也，故吉乃免咎。”
“无咎吉者，先免于咎而后吉从之也。”
邢璹注：“比初六有孚，比之无咎，终来有它吉之例也。”
“或亦处得其时，吉不待功，不犯于咎，则获吉也。”
邢璹注：“需之九二需于沙，小有言，终吉。注云：近不逼难，远不后时，履健居中以待其会，虽小有言，以吉终也。”
“或有罪自己招，无所怨咎，亦曰无咎。”
邢璹注：以《节》六三“不节若则嗟若，无咎”及《象》“不节之嗟，又谁咎也”为例。
陆德明音义：“怨”，纡万反，又纡元反。
释义：此段区分“无咎”“吉无咎”“无咎吉”三种语序，并补充因时得吉及自取其咎等情形。

【二、《卦略》十一卦注文与音义扩录】

（一）屯
邢璹注：“江海处下，百川归之；君能下物，万民归之。”
释义：以江海居下而汇聚百川，比喻居上者谦下待人，百姓归附。
陆德明音义：“难”，乃旦反（遯、明夷卦同）；“冯”，皮冰反，本亦作“凭”。

（二）蒙
王弼正文有“凡不识者求问，识者识者不求”字样，数字页面作重复“识者”，此处仍照录，不擅自删改。
陆德明音义：“昧”，音妹；“谘”，本亦作“资”；“远”，袁万反；“比”，毗志反。
页面此段未另列邢璹长注文，不据义理补造。

（三）履
邢璹注：“九五夬履贞厉，履道恶盈而五处尊位；三居阳位则见咥也。”
释义：履卦之道忌盈满；九五处尊位，六三以阴爻居阳位，故与履道不合。
陆德明音义：“履不处”，谓阳爻不处其位为美；“履者礼也”，今《杂卦》无此句，《韩注》有，或传写者误。
此条涉及《杂卦》异文，保留音义所记，不将“履者礼也”直接判为定本经文。

（四）临
陆德明音义：“长”，之丈反；并提示与《遯》卦音义相同。
页面此条未另列较长邢璹注，故不补写注语。

（五）观
邢璹注：“远为童观，近为观国。”
释义：以观看者与所观对象的远近，解释远处为童观、近尊为观国。
陆德明音义：“观”，古乱反；“见”，一本作“知”；“近”，附近之近；并提示与《明夷》卦音义相同。

（六）大过
邢璹注：“大过之时，阳处阴位，心无系应为吉；阳得位有应则凶也。”
释义：大过救衰之时，阳爻居阴位、无系应，反能承担拯救之任；若居阳位又有应，则未必合时。
陆德明音义：“桡”，乃孝反；“拯”，拯救之拯；“好”，呼报反；“赡”，常艳反；“褊”，必浅反。

（七）遯
邢璹注：“遯以远时为吉，不系为美；上则肥遯，初则有厉。”
释义：遯卦以远离不利时势为吉；上九远遯，初六则处遯之始而有危厉。
陆德明音义：“浸”，子鸩反；“长”，张丈反；“难”，乃旦反；“亨”，许庚反。

（八）大壮
页面此条主要为王弼正文及音义，未另列较长邢璹注文。
陆德明音义：“触”，昌录反；“藩”，扶袁反。

（九）明夷
邢璹注：“远难，藏明，明夷之义。”
释义：远离祸难而藏其明，是明夷处暗之时的要义。
陆德明音义：“远”，于万反；“近”，附近之近；“难”，乃旦反；“溺”，宁历反。

（十）睽
邢璹注：“火动而上，泽动而下，睽义见矣。”
释义：离为火、兑为泽，火炎上而泽润下，二体运动方向相反，故有睽异之象。
陆德明音义：“见”，贤遍反（丰卦同）；“洽”，咸夹反，本又作“合”。
版本校记另指出《睽》条“洽乃疑亡”监本讹作“始”，依丛书本改；此处依校订文字保留“洽”，并不把异文隐去。

（十一）丰
邢璹注：“丰之为义，贵在光大，恶于闇昧也。”
释义：丰卦以光明盛大为尚，反对幽暗昏昧。
陆德明音义：“恶”，乌路反；“沛”，步贝反，又普贝反；“蔀”，步口反；“昧”，音妹，本亦作“妹”，又作“沬”，皆末贝反；“舆”，如字，又音预；“折”，之舌反。

【录入边界与校勘说明】
1. 本条所引夹注依四库本数字页面第434—528行对应内容整理；条目次序与页面《略例下》《卦略》一致。
2. 本条扩录的重点是原注与音义，不重复前条已录的王弼正文；个别注文保留原句，较长注文另加释义。未标“邢璹注”的地方，不据上下文臆造注语。
3. 《卦略》十一卦数量及次序依总目和正文：屯、蒙、履、临、观、大过、遯、大壮、明夷、睽、丰。未将未列卦补入。
4. 《蒙》“识者识者不求”保留数字页面重复字样；《履》“履者礼也”保留音义对《杂卦》及韩注本的版本提示；《睽》“洽/始”保留校记依据。上述均待影印页逐字复核。
5. 本条不是所有夹注的影印级逐字定本。反切、异体字、断句及页面转录讹误仍需对照四库影印本和相关版本核验；不可靠处不以文意擅改。"""),

    Entry("历代注疏", "王弼《周易略例》四篇邢璹注文与陆德明音义分层", "关联典籍分层补录：从四库本数字页面逐条摘录《明爻通变》《明卦适变通爻》《明象》《辩位》中的邢璹注文与陆德明音义，明确区分王弼正文、注文和音义。此条补充此前仅录王弼正文的四篇；按页面顺序整理，尚待影印本逐字复核。", """【文献性质与范围】
底本为《钦定四库全书》本《周易注疏》附录《周易略例》，题署“魏王弼著、唐邢璹注、陆德明音义”。原页面将王弼正文、邢璹注文及陆德明音义交错排印。本条专门把《明爻通变》《明卦适变通爻》《明象》《辩位》四篇中可辨识的注文、音义抽出，按原文出现次序分层记录；王弼正文仍见前一条，不在此重复。为便于阅读，繁体字作通行简体转换，反切、异文提示及卦例尽量保留。

【一、《明爻通变》邢璹注文与陆德明音义】
1. “爻者，效也”：物刚则效刚，物柔则效柔；遇物而变，行动有所趋向，所以称为“言乎变者”。
2. “变者，情伪之所为也”：变化产生于情与伪；情伪所趋多端，故变化难以固定推算。
3. “情伪之动，非数之所求”：情欲与虚伪的发动，不是术数所能穷尽。物体有时同其性而行动，有时形貌与情志相违；同归而殊途，所思却可能各异。注以《萃》六二“引吉无咎”说明卦体贵于相从，而六二志在静退；又以《乾》初九“潜龙勿用”说明身虽潜屈而志意得伸。
4. “形躁好静，质柔爱刚”：风虎、云龙可以同气相感，但形体、性情、愿望并非总一致。注举《归妹》九四“愆期”说明震体形躁而又好静；举《履》六三“武人为于大君”说明爻志刚而兑体属阴、质柔。
   音义：“好”读呼报反。
5. “巧历不能定其算数”：万物情状与变化多端，即使精于历算、圣明之人，也不能为其制定固定算法和典要；法制不能使诈伪长短齐一，度量也不能使其完全均平。
   音义：“量”读亮。
6. “陵三军者，或惧于朝廷之仪；暴威武者，或困于酒色之娱”：注以刚猛之人反而畏惧细小礼仪为例，说明强弱不能只按外在体量判断；又举《大畜》初九“有厉”、九二“舆说辐”等爻例。
   音义：“朝”直遥反；“廷”音定。
7. “近不必比，远不必乖”：相近的爻未必亲比，相远的爻未必乖离。注举《屯》六二与初九相近而守贞不从，以及九五虽远而“十年乃字”为例。
   音义：“比”毗志反。
8. “同声相应，高下不必均；同气相求，体质不必齐”：初四、二五、三上同声相应，不必高下相等；同气相求也不要求形质相同。注以龙召云、律吕相应说明有识与无识之间也能感应；二女虽同属阴类而相违，刚柔异类也可合体。
9. “隆墀永叹，远壑必盈”：注释“隆”为高，“墀”为水中之地，“永”为长；以九五尊高比隆墀、六二卑下比远壑，说明唱和感应。
   音义：“墀”一本作“坻”，直其反；“坻”又释为蚁冢；“壑”火各反。
10. “投戈散地，则六亲不能相保”：注释“投”为置、“散”为逃散；兵戈置于逃散之境，即使至亲也不能相保。注举《遯》九四“好遯”说明处身于外、难在于内，甚至至亲也不能相保守。
11. “同舟而济，则胡越何患乎异心”：同舟共济时，胡越虽异，其心仍可一致。注以《渐》卦三、四爻异体和好、上下殊体而相保为例。
   音义：“济”一本作“载”。
12. “苟识其情，不忧乖远；苟明其趣，不烦强武”：识得同异之情，就不忧虑远近乖离；明白事物趋向，就不必强用威武。
13. “能说诸心，能研诸虑”：爻变显示凶患，能使人心悦服、思虑精审。
   音义：“说”读悦。
14. “睽而知其类，异而知其通”：注引《睽·象》“万物睽而其事类”，并以男女形体相异而志意可同说明异中有通。
15. “善迩而远至，命宫而商应”：近处修治言语，远方也会响应；声音相同则相应，如宫、商之声。
   音义：“善”一本作“缮”。
16. “修下而高者降，与彼而取此者服”：处下而修正自身，高者也会响应；君上与下民相与、取信于下，百姓便会服从。注举《否》初六与九四、《大有》六五为例。
17. “情伪相感，远近相追”：正应相感属于实情，注举《蹇》二、五；不正相感属于伪情，举《颐》三、上；有应而远相追，举《睽》三、上；无应而近相取，举《贲》二、三。
18. “爱恶相攻，屈伸相推”：注以《同人》三、四有爱有恶、互相攻伐，以及《否》《泰》一屈一伸为例。
   音义：“恶”乌路反。
19. “见情者获，直往则违”：见到对方真实情状，前往可得其志，注举《屯》六四求婚媾；不揆情而径往则违，举《屯》六三即鹿无虞。
20. “拟议以成其变化，语成而后有格”：注释“格”作“括”，“括”为结；行动前拟议，变化才能周尽，言语形成器物之后才没有结碍。
   音义：“语成而后有格”，旧本如此；一本“格”作“括”。
21. “不知其所以为主，鼓舞而天下从者，见乎其情者也”：注释“鼓舞”为变化；易道变化应人如响，虽退藏于密而不显其主，万物仍随之变化，因此可见其情。
22. “范围天地之化而不过，曲成万物而不遗”：注释“范”为法、“围”为周围；法天地变化而不过差，委曲成就万物而无所遗失。
23. “通乎昼夜之道而无体，一阴一阳而无穷”：阳通于昼、阴通于夜，昼夜即变化；道虚无无体，不以阴阳生长自居其功，故生长变化无穷。
24. “非天下之至变，其孰能与于此哉”：若非六爻极尽通变以应万物，不能参与此道。
   音义：“与”音预。

【二、《明卦适变通爻》邢璹注文与陆德明音义】
1. “卦者，时也；爻者，适时之变者也”：卦统摄一时的大义，爻体现时势中的通变。
2. “时有否泰，故用有行藏”：泰时则行，否时则藏。
   音义：“否”备鄙反。
3. “卦有小大，故辞有险易”：阴长则小，阳生则大；否卦之辞险，泰卦之辞易。
   音义：“易”以豉反，本章内同。
4. “一时之制可反而用，一时之吉可反而凶”：注以大畜之制与天衢之用、丰亨之吉与羁旅之凶说明时势反转；诸卦反对，爻也随卦而变。
5. “用无常道，事无轨度”：卦推移，故道用无常；爻随时变化，故事无固定轨度，动静、屈伸唯随变化所适。
6. “名其卦则吉凶从其类，存其时则动静应其用”：注以谦、比为吉类，蹇、剥为凶类；震时主动作，艮时主静止。
7. “应者，同志之象；位者，爻所处之象”：得应则志同相和；阴位为小人所处，阳位为君子所处。
8. “承乘者，逆顺之象；远近者，险易之象”：阴承阳为顺，阳承阴为逆；远离险难则易，接近险难则险。注举《小过》六五乘刚为逆、六二承阳为顺，以及《需》九三近坎为险、初九远险为易。
9. “内外者，出处之象；初上者，始终之象”：内卦为处，外卦为出；初为始，上为终。
10. “虽远而可以动者，得其应；虽险而可以处者，得其时”：注举《革》六二与五相应，虽远而往无咎；《需》上六居险之上，因得时而不忧入穴之凶。
11. “弱而不惧于敌者，得所据；忧而不惧于乱者，得所附”：注举《师》六五为师主，虽阴柔而居尊位；《遯》九五处遯时而附尊位，故小人不敢为乱。
12. “柔而不忧于断者，得所御；虽后而敢为之先者，应其始”：柔弱而能制断，因柔能御阳；后发而能先行，因应其始。注举《噬嗑》六五与《泰》初九。
   音义：“断”丁乱反。
13. “物竞而独安于静者，要其终”：众爻竞进，唯大有上九安静不居位，因居上极而要其终。
   音义：“要”音邀。
14. “观变动者存乎应，察安危者存乎位”：爻的变动看应，有应而动则不失；爻的安危看位，得位则安、失位则危。注举《谦》九三、《节》六四、《晋》九四。
15. “辩逆顺者存乎承乘”：阴乘阳为逆，注举《师》六三；阴承阳为顺，举《噬嗑》六三。
16. “明出处者存乎外内”：注以《遯》君子处外、《临》君子处内为例。
17. “远近终始各存其会，辟险尚远，趣时贵近”：得时则吉，失其要会则凶；注以《遯》上九尚远、《观》六四贵近为例。
   音义：“辟”音避，本亦作“避”。
18. “比复好先，乾壮恶首”：注举《比》初六与上六、《复》初九与上六，以及《乾》上九、《大壮》上六说明好先与恶首。
   音义：“比”毗志反；“好”呼报反。
19. “明夷务闇，丰尚光大”：注以《明夷》彖辞“利艰贞”说明晦明，以《丰》卦“勿忧，宜日中”说明光显。
20. “吉凶有时，不可犯；动静有适，不可过”：时有吉凶，不可越分轻犯；动静须适时，不可过越。
21. “犯时之忌，罪不在大；失其所适，过不在深”：注举《夬》九三应小人而犯时忌，以及《大过》九四阳居阴位、应初而有它吝为例。
22. “动天下、灭君主而不可危；侮妻子、用颜色而不可易”：大事如震动宇宙、弑灭君主，不可轻视；小事如侮慢妻子、使用颜色，也不可轻忽。注举《离》九四与《家人》九三。
   音义：“侮”亡甫反。
23. “当其列贵贱之时，其位不可犯”：位有贵贱、爻有尊卑，职分既定，不可触犯。
   音义：“当”如字。
24. “遇其忧悔吝之时，其介不可慢”：吉凶之始彰显于微兆，悔吝纤介虽细也不可轻慢。
   音义：“介”音界；一本作“分”，扶问反。

【三、《明象》邢璹注文与陆德明音义】
1. “象者，出意者也；言者，明象者也”：立象是为了表达其意，作言是为了显明其象。注以乾能变化、龙为象说明：欲明乾象，假龙以明乾；欲明龙，则借言语象之。
2. “尽意莫若象，尽象莫若言”：言生于象，故可寻言观象；象生于意，故可寻象观意。
   音义：“观意”一本作“见意”。
3. “意以象尽，象以言著”：意由象尽显，象由言显著；言用来明象，得象后可以忘言；象用来存意，得意后可以忘象。
4. “得象而忘言，得意而忘象”：注以龙象、乾意说明，既得龙象，其言可忘；既得乾意，其龙可舍。
5. “得兔忘蹄”：蹄用来捕兔，得兔后忘蹄。
   音义：“蹄”音啼；“兔”他故反，又作“菟”。
6. “得鱼而忘筌”：筌用来捕鱼，得鱼后弃筌。
   音义：“筌”七全反；“筌蹄事见《庄子》。
7. “言者象之蹄，象者意之筌”：言如蹄，象如筌；不能执着言与象而忘其所指。
8. “存言者非得象，存象者非得意”：未得象而只存言，所存便不是象；未得意而只存象，所存便不是意。
9. “忘象者乃得意，忘言者乃得象”：得意在忘象，得象在忘言；注释为弃执而后得之。
10. “立象以尽意，重画以尽情”：意既尽，可遗象；情既尽，可遗画。注以《同人》与“拔茅”之画说明象画只是达意之具。
    音义：“重”直龙反；“画”胡麦反，下同。
11. “触类可为其象，合义可为其征”：遇到事物类别即可取象，义理相合即可作为征验。
12. “义苟在健，何必马；类苟在顺，何必牛”：注举《大壮》九三有乾而称羝羊、《坤》卦无乾而称牝马，以及《遯》无坤而称牛、《明夷》无乾而称马，说明象不可机械固定。
    音义：“应”音鹰。
13. “或者定马于乾”：只执乾为马，象义便不弘广；应按卦文考察，不可因有马而无乾就斥为伪说。
14. “互体不足，遂及卦变；变又不足，推致五行”：注释为广推金木水火土以取象；一旦失去原旨，巧说会愈发繁杂。
    音义：“漫”末半反。
15. “纵复或值，而义无所取”：即使偶然说中，也未必有义理可取；注以失鱼兔而空守筌蹄、遗忘健顺而空说龙马作比。
    音义：“复”扶又反。

【四、《辩位》邢璹注文与陆德明音义】
1. “象无初上得位失位之文”：阴阳居初、上，不以得位失位论。注指出《系辞》只论三五、二四同功异位，并未涉及初上。
2. “乾上九贵而无位，需上六虽不当位”：阳居上与阴居上都可称非位，故不能把上爻简单固定为阴位或阳位。
3. “初上者，是事之终始，无阴阳定位”：初为始，上为终；用于人事是终始，不是固定禄位。乾初称“潜”，过五称“无位”，没有处在固定之位却称潜、居上有位却称无位的例子。
4. “位者，列贵贱之地，待才用之宅”：注释“宅”为居；二、四为阴贱之位，三、五为阳贵之位。
5. “爻者，守位分之任，应贵贱之序”：各守其位，按贵贱次序相应。
   音义：“分”扶问反，下同。
6. “位有尊卑，爻有阴阳”：尊者为阳所处，卑者为阴所履；去掉初、上而论位分，三、五在一卦之上，故为阳位；二、四在一卦之下，故为阴位。
7. “初上者，体之终始，事之先后”：初上位无固定分际，不能以阴阳确定；尊卑有常序，终始却没有固定主位。
   音义：“去”羌吕反。
8. “统而论之，爻之所处谓之位；卦以六爻为成，不得不谓之六位时成”：爻所在之处可称位，卦由六爻构成，故称六位时成。
   音义：“无六爻之无亦作损”。

【录入边界与校勘提醒】
1. 本条是四库本数字页面中注文、音义的分层摘录稿；尽量依页面次序列出，但不是对所有夹注字形和标点完成影印级校勘的定本。
2. 王弼正文与邢璹注文的边界以页面“注”标识为准；陆德明音义以“音义”标识为准。注中所引卦例、经传语句属于注家解释或引文，不应误作王弼本篇正文。
3. 本条对少数较长注文作了分句归纳，已明确标注为摘录/释义，不冒充逐字全录；后续如取得影印页，可继续逐字补齐。
4. 《明卦适变通爻》篇名仍保留总目音义所见“明卦通变适爻”“适变通爻”等异文；不据单一数字文本消除异文。
5. 数字来源：维基文库《周易注疏（四库全书本）/周易略例》，《明爻通变》至《辩位》正文及夹注所在页面段落。当前仅完成数字文本分层，尚待对照四库影印页逐字复核。"""),

    Entry("历代注疏", "《周易略例考证》四库本逐条校记", "版本校勘资料：据四库本页面所附《周易略例考证》，逐条录入监本与相台岳氏荆溪家塾本、汉魏丛书、津逮秘书之间的异文、脱文和补字说明。此为清代校者考证，不是王弼正文，也不是邢璹注文。", """【文献性质】
本条专录四库本页面末尾《周易略例考证》。考证者自述以相台岳氏所梓荆溪家塾本、汉魏丛书、津逮秘书等参订监本，并将异同逐条标出。以下按页面次序转录。校记中“监本”指其所校监本；“荆溪本”“秘书本”“丛书本”等均依原称保留，不擅自扩写版本信息。

【考证总说】
臣良裘按：略例一册，乃王氏易注之纲领。《隋书·经籍志》所谓《周易》十卷，盖并此数之，故《释文》云“或有题为第十者”。孔氏作疏，主申王学而不疏略例，殊失体要；至坊刻注疏本乃削去之，谬妄甚矣。监本虽附见卷末，而序目不全，字句多讹。今据相台岳氏所梓荆溪家塾本及《汉魏丛书》《津逮秘书》所录，稍为参订，有同异者著之。

【《明彖》校记】
1. “明其所由之主也”下注“明辩也，辩卦体功用所由之主”：监本脱“明辩也”三字，依《汉魏丛书》本添。
2. “贞夫一者也”下注“制其动者贞正之一者也”：“正”字，荆溪家塾本无。
3. “主必致一也”下注“故无心于存皆得其存也”：监本脱此二句，依《津逮秘书》本添。
4. “则知可以执一御也”下注“无为之一者道也君也”：监本脱此九字，依荆溪本添。
5. “或有遗爻而举二体者卦体不由乎爻也”下注“弃此一爻而举二体以明其义”：“弃此一爻”，秘书本作“弃此中之一爻”。
6. “其孰能与于此乎”：“乎”字，监本脱，依丛书本添。

【《明爻通变》校记】
7. “故合散屈伸与体相乖”下注“人之多辟己独处正”：“处”字，荆溪本作“取”。
8. “召云者龙命吕者律”下注“召水气者水畜此明有识感无识命阴吕者阳律此明无识感有识”：“无识感有识”句诸本皆同，监本作“无识感无识”。
9. “异而知其通”下注“男女睽而其志同也”：“同”字，秘书本作“通”；《睽》彖传本作“通”。
10. “语成而后有格”：《丛书》本作“语成器而后有格”。
11. “鼓舞而天下从者见乎其情者也”：“从”字下监本脱“者”字，依丛书本添。
12. “一阴一阳而无穷”下注“道者虚无也”：“无”字，监本脱，依荆溪本添。

【《明卦适变通爻》校记】
13. “故卦以反对而爻亦有变”下注“爻变亦准也”：“准”字，监本作“应”，依荆溪本改。
14. “唯变所适”下注“卦既推移故道用无常”：“既”字，监本作“虽”；“道”字上脱“故”字，依秘书本添。
15. “险易之象也”下注“需卦九三近坎险也”：“坎”字，监本作“难”，依荆溪本改。
16. “物竞而独安于静者要其终也”：“于”字，监本脱，依秘书本添。
17. “辩逆顺者存乎承乘”下注“阴乘乎阳逆也”：“诸本多作‘阳乘乎阴’，误也，今依荆溪本。”

【《明象》校记】
18. “言生于象故可寻言以观象，象生于意故可寻象以观意”：两个“生”字，监本并作“主”字，依秘书本改。
19. “得意而忘象”下注“既得乾意其龙可舍”：“意”字，监本讹作“象”，依秘书本改。
20. “存言者非得象者也”下注“未得象者存言言则非象”：“则”字，监本讹作“有”；下句“象则非意”同，俱依荆溪本改。

【《辩位》校记】
21. “乾上九贵而无位”下注“阳居之也”；“需上六虽不当位”下注“阴居之也”：“阳”字监本讹作“阴”，“阴”字监本讹作“阳”，俱依荆溪本改。
22. “应贵贱之序者也”下注“各守其位应之以序”：“序”字，监本作“事”，依丛书本改。

【《略例下》校记】
23. “其有先贞后亨者亨由于贞也”：“后亨”二字，监本脱，依秘书本添。

【《卦略》校记】
24. “洽乃疑亡也”：“洽”字，监本讹作“始”，依丛书本改。

【录入边界】
1. 上述校记是《周易略例考证》的校者按语，不是王弼或邢璹正文。
2. 版本简称依底本原文保留；尚未取得各版本对应影印页逐字核验，不能仅凭校记推定所有版本的完整谱系。
3. 本条统一用便于检索的简体字呈现；“无/无”“辩/辨”等字形差异不在转录时擅自改写原校记的判断。
4. 校记编号为本条阅读序号，非原书编号。"""),

    Entry("历代注疏", "唐李鼎祚《周易集解》卷一·乾卦经文、彖象集解", "底本：维基文库《周易集解（李鼎祚）/卷一》，本条已录入卷一乾卦经文、《彖》《象》《文言》及诸家注文（维基文库页面行112—683，卷一现存正文范围）。保留经文、注文次序及注家署名；原页面末尾为图像/页面信息，无后续正文。后续将对照其他可靠版本校核异文。", """(乾下乾上)。乾：元、享、利、貞。

案：《說卦》：乾，健也。言天之體以健為用，運行不息，應化無窮，故聖人則之。欲使人法天之用，不法天之體，故名乾，不名天也。

《子夏傳》曰：元，始也。亨，通也。利，和也。貞，正也。言乾稟純陽之性，故能首出庶物，各得元始、開通、和諧、貞固，不失其宜。是以君子法乾而行四德，故曰「元、亨、利、貞」矣。

初九：潛龍勿用。
崔覲曰：九者，老陽之數，動之所占，故陽稱焉。潛，隱也。龍下隱地，潛德不彰，是以君子韜光待時，未成其行。故曰「勿用」。
《子夏傳》曰：龍，所以象陽也。
馬融曰：物莫大於龍，故借龍以喻天之陽氣也。初九，建子之月。陽氣始動於黃泉，既未萌芽，猶是潛伏，故曰「潛龍」也。
沈驎士曰：稱龍者，假象也。天地之氣有升降，君子之道有行藏。龍之為物，能飛能潛，故借龍比君子之德也。初九既尚潛伏，故言「勿用」。
幹寶曰：位始，故稱初。陽重，故稱九。陽在初九，十一月之時，自復來也。初九，甲子天正之位，而乾元所始也。陽處三泉之下，聖德在愚俗之中，此文王在羑裏之爻也。雖有聖明之德，未被時用，故曰「勿用」。

九二：見龍在田，利見大人。
王弼曰：出潛離隱，故曰「見龍」。處於地上，故曰「在田」。德施周普，居中不偏，雖非君位，君之德也。初則不彰，三則乾乾，四則或躍，上則過亢。利見大人，唯二五焉。
鄭玄曰：二於三才為地道，地上即田，故稱「田」也。
幹寶曰：陽在九二，十二月之時，自臨來也。二為地上，田在地之表，而有人功者也。陽氣將施，聖人將顯，此文王免於羑裏之日也。故曰「利見大人」。

九三：君子終日乾乾，夕惕，若厲，無咎。
鄭玄曰：三於三才為人道。有乾德而在人道，君子之象。
虞翻曰：謂陽息至三，二變成離。離為日，坤為夕。
荀爽曰：日以喻君。謂三居下體之終，而為之君，承乾行乾，故曰「乾乾」。夕惕以喻臣。謂三臣於五，則疾修柔順，危去陽行，故曰「無咎」。
幹寶曰：爻以氣表，繇以龍興，嫌其不關人事，故著君子焉。陽在九三，正月之時，自泰來也。陽氣始出地上，而接動物。人為靈，故以人事成天地之功者，在於此爻焉。故君子以之憂深思遠，乾夕匪懈。仰憂嘉會之不序，俯懼義和之不逮。反復天道，謀始反終。故曰「終日乾乾」。此蓋文王反國大理其政之日也。凡無咎者，憂中之喜，善補過者也。文恨早耀，文明之德，以蒙大難，增修柔順，以懷多福，故曰「無咎」矣。

九四：或躍在淵，無咎。
崔覲曰：言君子進德修業，欲及於時。猶龍自試躍天，疑而處淵。上下進退，非邪離群。故「無咎」。
幹寶曰：陽在九四，二月之時，自大壯來也。四虛中也。躍者，暫起之言，既不安於地，而未能飛於天也。四，以初為應。淵，謂初九甲子，龍之所由升也。或之者，疑之也。此武王舉兵孟津，觀釁而退之爻也。守柔順，則逆天人之應；通權道，則違經常之教。故聖人不得已而為之，故其辭疑矣。

九五：飛龍在天，利見大人。
鄭玄曰：五於三才為天道。天者，清明無形，而龍在焉，飛之象也。
虞翻曰：謂四已變，則五體離。離為飛，五在天，故「飛龍在天，利見大人」也。謂若庖犧觀象於天，造作八卦，備物致用，以利天下，故曰「飛龍在天」。天下之所利見也。
幹寶曰：陽在九五，三月之時，自夬來也。五在天位，故曰「飛龍」。此武王克紂正位之爻也。聖功既就，萬物既睹，故曰「利見大人」矣。

上九：亢龍有悔。
王肅曰：窮高曰亢。知進忘退，故「悔」也。
幹寶曰：陽在上九，四月之時也。亢，過也。乾體既備，上位既終。天之鼓物，寒暑相報；聖人治世，威德和濟；武功既成，義在止戈。盈而不反，必陷於「悔」。案：以人事明之，若桀放於南巢，湯有慚德，斯類是也。

用九：見群龍無首，吉。
劉瓛曰：總六爻純陽之義，故曰「用九」也。王弼曰：九，天之德也。能用天德，乃見群龍之義焉。夫以剛健而居人之首，則物之所不與也。以柔順而為不正，則佞邪之道也。故乾吉在無首，坤利在永貞矣。

彖曰：
劉瓛曰：彖者，斷也，斷一卦之才也。
大哉乾元，
《九家易》曰：陽稱大。六爻純陽，故曰「大」。乾者純陽，眾卦所生，天之象也。觀乾之始，以知天德，惟天為大，惟乾則之，故曰「大哉」。元者，氣之始也。
萬物資始，
荀爽曰：謂分為六十四卦，萬一千五百二十冊，皆受始於乾也。冊取始於乾，猶萬物之生稟於天。
乃統天。
《九家易》曰：乾之為德，乃統繼天道，與天合化也。
雲行雨施，品物流形。
虞翻曰：已成既濟，上坎為雲，下坎為雨，故「雲行雨施」。乾以雲雨，流坤之形，萬物化成，故曰「品物流形」也。
大明終始，
荀爽曰：乾起坎，而終於離。坤起於離，而終於坎。離坎者，乾坤之家，而陰陽之府，故曰「大明終始」也。
六位時成，
荀爽曰：六爻隨時而成乾。
時乘六龍以禦天。
侯果曰：大明，日也。六位，天地四時也。六爻效彼而作也。大明以晝夜為終始，六位以相揭為時成。言乾乘六氣而陶冶變化，運四時而統禦天地，故曰「時乘六龍以禦天」也。故《乾鑿度》曰：「日月終始萬物」，是其義也。
乾道變化，各正性命。保合大和，乃利貞。首出庶物，萬國鹹寧。
劉瓛曰：陽氣為萬物之所始，故曰「首出庶物」。立君而天下皆寧，故曰「萬國鹹寧」也。

《象》曰：
案：象者，象也，取其法象卦爻之德。
天行健，
何妥曰：天體不健，能行之德健也。猶如地體不順，承弱之勢順也。所以乾卦獨變名為健者。宋衷曰：晝夜不懈，以健詳其名。余卦各當名，不假於詳矣。
君子以自強不息。
虞翻曰：君子謂三。乾健，故強。天一日一夜過周一度，故自強不息。老子曰：自勝者強。
幹寶曰：言君子通之於賢也。凡勉強以進德，不必須在位也。故堯舜一日萬機，文王日昃不暇食，仲尼終夜不寢，顏子欲罷不能，自此以下，莫敢淫心舍力。故曰「自強不息」矣。
潛龍勿用，陽在下也。
荀爽曰：氣微位卑，雖有陽德，潛藏在下，故曰「勿用」也。
見龍在田，德施普也。
荀爽曰：見者，見居其位。田，謂坤也。二當升坤五。故曰「見龍在田」。大人，謂天子。見據尊位，臨長群陰，德施於下，故曰「德施普也」。
終日乾乾，反復道也。
虞翻曰：至三體復，故「反復道」，謂否泰反其類也。
或躍在淵，進無咎也。
荀爽曰：乾者，君卦。四者，陰位。故上躍居五者，欲下居坤初，求陽之正。地下稱淵也。陽道樂進，故曰「進無咎也」。
飛龍在天，大人造也。
荀爽曰：飛者，喻無所拘。天者，首事造制。大人造法，見居天位，聖人作為萬物睹，是其義也。
亢龍有悔，盈不可久也。
《九家易》曰：陽當居五，今乃居上，故曰「盈」也。亢極失位，當下之坤三，故曰「盈不可久」，若太上皇者也。下之坤三，屈為諸侯，故曰「悔」者也。
用九，天德不可為首也。
宋衷曰：用九，六位皆九，故曰「見群龍」。純陽則天德也。萬物之始，莫能先之，不可為首。先之者兇，隨之者吉，故曰「無首，吉」。
《文言》曰：
劉瓛曰：依文而言其理，故曰「文言」。
姚信曰：乾坤為門戶。文說乾坤，六十二卦皆放焉。
元者，善之長也。
《九家易》曰：乾者，君卦也。六爻皆當為君，始而大通，君德會合，故元為「善之長也」。
亨者，嘉之會也。
《九家易》曰：通者，謂陽合而為乾。眾善相繼，故曰「嘉之會也」。
利者，義之和也。
荀爽曰：陰陽相合，各得其宜，然後利矣。
貞者，事之幹也。
荀爽曰：陰陽正而位當，則可以幹舉萬事。
君子體仁，足以長人；
何妥曰：此明聖人則天，合五常也。仁為木，木主春，故配元為四德之首。君子體仁，故有長人之義也。
嘉會，足以合禮；
何妥曰：禮是交接會通之道，故以配通。五禮有吉兇賓軍嘉，故以嘉合於禮也。
利物，足以和義；
何妥曰：利者，裁成也。君子體此利以利物，足以合於五常之義。
貞固，足以幹事。
何妥曰：貞，信也。君子貞正，可以委任於事。故《論語》曰：敬事而信，故幹事而配信也。
案：此釋非也。夫在天成象者，乾元、亨、利、貞也。言天運四時，以生成萬物。在地成形者，仁、義、禮、智、信也。言君法五常，以教化於人。元為善長，故能體仁。仁主春生，東方木也。亨為嘉會，足以合禮。禮主夏養，南方火也。利為物宜，足以和義。義主秋成，西方金也。貞為事幹，以配於智。智主冬藏，北方水也。故孔子曰：仁者樂山，智者樂水，則智之明證矣。不言信者，信主土，而統屬於君。故中孚雲：信及豚魚，是其義也。若首出庶物，而四時不忒者，乾之象也。厚德載物，而五行相生者，土之功也。土居中宮，分王四季，亦由人君無為皇極，而奄有天下。水火金木，非土不載。仁義禮智，非君不弘。信既統屬於君，故先言乾。而後不言信，明矣。
君子行此四德者，故曰：乾，元、亨、利、貞。
幹寶曰：夫純陽，天之精氣。四行，君之懿德。是故乾冠卦首，辭表篇目，明道義之門在於此矣，猶春秋之備五始也。故夫子留意焉。然則體仁正已，所以化物；觀運知時，所以順天；氣用隨宜，所以利民；守正一業，所以定俗也。亂則敗禮其教淫；逆則拂時，其功否；錯則妨用，其事廢；忘則失正，其官敗。四德者，文王所由興。四愆者，商紂所由亡。
初九曰：潛龍勿用，何謂也？
何妥曰：夫子假設疑問也。後五爻皆放此也。
子曰：龍德而隱者也。
何妥曰：此直答言聖人有隱顯之龍德。今居初九窮下之地，隱而不見，故雲「勿用」矣。
不易世，
崔覲曰：言據當潛之時，不易乎世而行者，龍之德也。
不成名。
鄭玄曰：當隱之時，以從世俗，不自殊異，無所「成名」也。
遁世無悶，
崔覲曰：道雖不行，達理「無悶」也。
不見是而無悶。
崔覲曰：世人雖不已是，而已知不違道，故「無悶」。
樂則行之，憂則違之，
虞翻曰：陰出初震，為樂為行，故「樂則行之」。坤死稱憂，隱在坤中，遁世無逆流，故「憂則違之」也。
確乎其不可拔，潛龍也。
虞翻曰：確，剛貌也。乾剛潛初，坤亂於上。君子弗用，隱在下位。確乎難拔，潛龍之誌也。
九二曰：見龍在田，利見大人，何謂也？子曰：龍德而正中者也。
虞翻曰：中下之中，二非陽位，故明言能「正中」也。
庸言之信，
荀爽曰：處和應坤，故曰「信」。
庸行之謹。
《九家易》曰：以陽居陰位，故曰「謹」也。庸，常也。謂言常以信，行常以謹矣。
閑邪存其誠，
宋衷曰：閑，防也。防其邪而存誠焉。二在非其位，故以「閑邪」言之。能處中和，故以「存誠」言之。
善世而不伐，
《九家易》曰：陽升居五，處中居上，始以美德利天下。不言所利，即是不伐。故老子曰：上德不德，是以有德。此之謂也。
德博而化。
荀爽曰：處五據坤，故「德博」。群陰順從，故物「化」也。
《易》曰：見龍在田，利見大人，君德也。
虞翻曰：陽始觸陰，當升五為君。時舍於二，宜利天下。直方而大德，無不利。明言君德，地數始二。故稱「易曰」。
九三曰：君子終日乾乾，夕惕若，厲，無咎。何謂也？子曰：君子進德修業。
虞翻曰：乾為德，坤為業，以乾通坤，謂為「進德修業」。
宋衷曰：業，事也。三為三公，君子處公位，所以「進德修業」也。
忠信，所以進德也。
翟玄曰：忠於五，所以修德也。
崔覲曰：推忠於人，以信待物，故其德日新也。
修辭立其誠，所以居業也。
荀爽曰：修辭謂「終曰乾乾」。立誠，謂「夕惕若厲」。居業，謂「居三」也。
翟玄曰：居三修其教令，立共誠信，民敬而從之。
知至至之，可與言幾也。
翟玄曰：知五可至而「至之」，故可與行幾微之事也。
知終終之，可與存義也。
姚信曰：知終者，可以知始終。終謂三也。義者，宜也。知存知亡，君子之宜矣。
崔覲曰：君子，喻文王也。言文王進德修業，所以貽厥武王，至於九五。至於九五，可與進修意合，故言「知至至之」，可與言微也。知天下歸周，三分有二，以服事殷，終於臣道。終於臣道，可與進修意合，故言「知終終之，可與存義」。
是故居上位而不驕。
虞翻曰：天道三才。一乾而以至三乾成，故為「上」。「夕惕若厲」，故「不驕」也。
在下位而不憂。
虞翻曰：「下位」謂初。隱於初，憂則違之，故「不憂」。
故乾乾因其時而惕，雖危無咎矣。
王弼曰：惕，怵惕也。處事之極，失時敗廢，懈怠則曠。故「乾乾因其時而惕，雖危無咎」。
九四曰：或躍在淵，無咎，何謂也？子曰：上下無常，非為邪也。
荀爽曰：乾者，君卦。四者，臣位也。故欲上躍。居五下者，當下居坤初，得陽正位。故曰：「上下無常，非為邪也」。
進退無恒，非離群也。
荀爽曰：進，謂居五。退，謂居初。故「進退無恒，非離群也」。
君子進德修業，欲及時也，故無咎。
崔覲曰：至公欲及時濟人，故「無咎」也。
九五曰：飛龍在天，利見大人，何謂也？子曰：同聲相應。
虞翻曰：謂震巽也。庖犧觀變而放八卦，雷風相薄，故「相應」也。
張璠曰：天者，陽也。君者，陽也。雷風者，天之聲。號令者，君之聲。明君與天地相應，合德同化，動靜不違也。
同氣相求。
虞翻曰：謂艮兌山澤通氣，故「相求」也。
崔覲曰：方諸與月，同有陰氣，相感則水生。陽燧與日，同有陽氣，相感則火出也。
水流濕，
荀爽曰：陽動之坤而為坎，坤者純陰，故曰「濕」也。
火就燥。
荀爽曰：陰動之乾而成離，乾者純陽，故曰：「燥」也。
虞翻曰：離上而坎下，水火不相射。
崔覲曰：決水先流濕，然火先就燥。
雲從龍，
荀爽曰：龍，喻王者。謂乾二之坤五為坎也。
虞翻曰：乾為龍，雲生天，故「從龍」也。
風從虎。
荀爽曰：虎，喻國君。謂坤五之乾二為巽，而從三也。三者，下體之君，故以喻國君。
虞翻曰：坤為虎，風生地，故「從虎」也。
聖人作而萬物睹。
虞翻曰：睹，見也。聖人則庖犧，合德乾五，造作八卦，以通神明之德，以類萬物之情。五動成離，日出照物皆相見，故曰「聖人作而萬物睹」也。
陸績曰：陽氣至五，萬物茂盛，故譬以聖人在天子之位，功成制作，萬物鹹見之矣。
本乎天者親上，
荀爽曰：謂乾九二，本出於乾，故曰「本乎天」。而居坤五，故曰「親上」。
本乎地者親下，
荀爽曰：謂坤六五，本出於坤，故曰：「本乎地」。降居乾二，故曰「親下」也。
崔覲曰：謂動物親於天之動，植物親於地之靜。
則各從其類也。
虞翻曰：「方以類聚，物以群分」。「乾道變化，各正性命」。「觸類而長」，故「各從其類」。
上九曰：亢龍有悔，何謂也？子曰：貴而無位，
荀爽曰：在上，故「貴」。失正，故「無位」。
高而無民，
何妥曰：既不處九五帝王之位，故「無民」也。夫率土之濱，莫非王臣。既非王位，則民不隸屬也。
賢人在下位，
荀爽曰：謂上應三，三陽德正，故曰「賢人」。別體在下，故曰「在下位」。
而無輔，
荀爽曰：兩陽無應，故「無輔」。
是以動而有悔也。
荀爽曰：升極當降，故有「悔」。
潛龍勿用，下也。
何妥曰：此第二章，以人事明之。當帝舜耕漁之日，卑賤處下，未為時用，故雲「下」。
見龍在田，時舍也。
何妥曰：此夫子洙泗之日，開張業藝，教授門徒，自非通舍，孰能如此。虞翻曰：二非王位，時暫「舍」也。
終日乾乾，行事也。
何妥曰：此當文王為西伯之時，處人臣之極，必須事上接下，故言「行事」也。
或躍在淵，自試也。
何妥曰：欲進其道，猶復疑惑。此當武王觀兵之日，欲以試觀物情也。
飛龍在天，上治也。
何妥曰：此當堯、舜冕旒之日，以聖德而居高位，在上而治民也。
亢龍有悔，窮之災也。
案：此當桀、紂失位之時。亢極驕盈，故致悔恨窮斃之災禍也。
乾元用九，天下治也。
案：此當三皇五帝禮讓之時。垂拱無為，而天下治矣。
王弼曰：此一章全以人事明之也。九，陽也。陽，剛直之物也。夫能全用剛直，放遠善柔，非天下之至治，未之能也。故「乾元用九」，則「正下治也」。夫識物之動，則其所以然之理，皆可知也。龍之為德，不為妄者也。潛而勿用乎？必窮處於下也。見而在田，必以時之通舍也。以爻為人，以位為時，人不妄動，則時皆可知也。文王明夷，則主可知矣。仲尼旅人，則國可知矣。
潛龍勿用，陽氣潛藏。
何妥曰：此第三章，以天道明之。當十一月，陽氣雖動，猶在地中，故曰「潛龍」也。
見龍在田，天下文明。
案：陽氣上達於地，故曰「見龍在田」。百草萌牙孚甲，故曰「文明」。
孔穎達曰：先儒以為九二當太蔟之月，陽氣見地。則九三為建辰之月，九四為建午之月，九五為建申之月，上九為建戌之月，群陰既盛，上九不得言與時偕極。先儒此說，於理稍乖。此乾之陽氣漸生，似聖人漸進，宜據十一月之後，建巳之月已來。此九二爻當建醜建寅之間，於時地之萌牙物有生者，即是陽氣發見之義也。但陰陽二氣，共成歲功。故陰興之時，仍有陽在；陽生之月，尚有陰氣。所以六律六呂，陰陽相關，取象論義，與此不殊也。
終日乾乾，與時偕行。
何妥曰：此當三月。陽氣浸長，萬物將盛，與天之運俱行不息也。
或躍在淵，乾道乃革。
何妥曰：此當五月。微陰初起，陽將改變，故雲「乃革」也。
飛龍在天，乃位乎天德。
何妥曰：此當七月。萬物盛長，天功大成，故雲「天德」也。
亢龍有悔，與時偕極。
何妥曰：此當九月。陽氣大衰，向將極盡，故雲「偕極」也。
乾元用九，乃見天則。
何妥曰：陽消，天氣之常。天象法則，自然可見。
王弼曰：此一章全說天氣以明之也。九，剛直之物，唯乾體能用之。用純剛以觀天，天則可見矣。
乾元者，始而亨者也。
虞翻曰：乾始開通，以陽通陰，故始通。
利貞者，性情也。
幹寶曰：以施化利萬物之性，以純一正萬物之情。
王弼曰：不為乾元，何能通物之始。不性其情，何能久行其正。是故始而亨者，必乾元也。利而正者，必性情也。
乾始而以美利利天下，
虞翻曰：美利謂雲行雨施，品物流形，故「利天下」也。
不言所利，大矣哉！
虞翻曰：天何言哉！四時行焉，百物生焉，故利者大也。
大哉乾乎，剛健中正，純粹精也。
崔覲曰：不雜曰純，不變曰粹。言乾是純粹之精，故有剛、健、中、正之四德也。
六爻發揮，旁通情也。
陸績曰：乾六爻發揮變動，旁通於坤；坤來入乾，以成六十四卦，故曰「旁通情也」。
時乘六龍，
《九家易》曰：謂時之元氣，以王而行，履涉眾爻，是「乘六龍」也。
以禦天也。
荀爽曰：禦者，行也。陽升陰降，天道行也。
雲行雨施，天下平也。
荀爽曰：乾升於坤，曰「雲行」。坤降於乾，曰「雨施」。乾坤二卦，成雨既濟。陰陽和均，和而其正，故曰「天下平」。
君子以成德為行，
幹寶曰：君子之行，動靜可觀，進退可度，動以成德，無所茍行也。
日可見之行也。
虞翻曰：謂初。乾稱君子。陽出成為上德，雲行雨施則成離，日新之謂上德，故「日可見之行」。
潛之為言也，隱而未見，行而未成，是以君子弗用也。
荀爽曰：「隱而未見」，謂居初也，「行而未成」，謂行之坤四。陽居陰位，未成為君。乾者，君卦也。不成為君，故不用也。
君子學以聚之，問以辯之，
虞翻曰：謂二。陽在二。兌在口；震為言，為講論；坤為文，故「學以聚之，問以辯之」。兌象，君子以朋友講習。
寬以居之，仁以行之。
虞翻曰：震為寬，仁為行，謂居寬行仁，德博而化也。
《易》曰：見龍在田，利見大人，君德也。
虞翻曰：重言君德者，大人善世不伐。信有君德，後天而奉天時，故詳言之。
九三：重剛而不中。
虞翻曰：以乾接乾，故「重剛」。位非二五，故「不中」也。
上不在天，下不在田。
何妥曰：上不及五，故雲「不在天」。下已過二，故雲「不在田」。處此之時，實為危厄也。
故乾乾因其時而惕，雖危無咎矣。
何妥曰：處危懼之地，而能乾乾懷厲，至夕猶惕，乃得「無咎」矣。
九四：重剛而不中，
案：三居下卦之上，四處上卦之下，俱非得中，故曰「重剛而不中」也。
上不在天，下不在田，中不在人，
侯果曰：案下《系》：易有天道，有地道，有人道，兼三才而兩之，謂兩爻為一才也。初兼二，地也。三兼四，人也。五兼六，天也。四是兼才，非正，故言「不在人」也。
故或之。或之者，疑之也。故無咎。
虞翻曰：非其位，故疑之也。
夫大人者，
《乾鑿度》曰：聖明德備，曰「大人」也。
與天地合其德，
荀爽曰：與天合德，謂居五也。與地合德，謂居二也。
案：謂撫育無私，同天地之覆載。
與日月合其明，
荀爽曰：謂坤五之乾二成離，離為日。乾二之坤五為坎，坎為月。
案：威恩遠被，若日月之照臨也。
與四時合其序，
翟玄曰：乾坤有消息，從四時來也。
又案：賞罰嚴明，順四時之序也。
與鬼神合其吉兇，
虞翻曰：謂乾神合吉，坤鬼合兇，以乾之坤，故「與鬼神合其吉兇」。
案：禍淫福善，葉鬼神之吉兇矣。
先天而天弗違，
虞翻曰：乾為天、為先。大人在乾五，乾五之坤五，天象在先。故「先天而天弗違」。
崔覲曰：行人事，合天心也。
後天而奉天時。
虞翻曰：奉，承行。乾四之坤初成震，震為後也，震春兌秋，坎冬離夏，四時象具。故「後天而奉天時」。謂承天時，行順也。
崔覲曰：奉天時布政，聖政也。
天且弗違，況於人乎？
荀爽曰：人謂三。
況於鬼神乎？
荀爽曰：神謂天，鬼謂地也。
案：大人惟德動天，無遠不屆。鬼神饗德，夷狄來賓。人神葉從，猶風偃草，豈有違忤哉。
亢之為言也，知進而不知退，
荀爽曰：陽位在五，今乃居上，故曰「知進而不知退」也。
知存而不知亡，
荀爽曰：在上當陰，今反為陽，故曰「知存而不知亡」也。
知得而不知喪，
荀爽曰：得謂陽，喪謂陰。
案，此論人君驕盈過亢，必有喪亡。若殷紂招牧野之災，太康遘洛水之怨，即其類矣。
其唯聖人乎！知進退存亡而不失其正者，其唯聖人乎！
荀爽曰：進謂居五。退謂居二。存謂五，為陽位。亡謂上，為陰位也。再出聖人者，上聖人謂五，下聖人謂二也。
案：此則乾元用九，天下治也。言大寶聖君，若能用九天德者，垂拱無為，芻狗萬物，生而不有，功成不居，百姓日用而不知，豈荷生成之德者也。此則三皇五帝，乃聖乃神，保合太和，而天下自治矣。今夫子《文言》再稱聖人者，嘆美用九之君，能知進退存亡，而不失其正，故得「大明終始，萬國鹹寧，時乘六龍，以禦天也」。斯即有始有卒者，其唯聖人乎！是其義也。
崔覲曰：謂失其正者，若燕噲讓位於子之之類是也。
案：三王五伯，揖讓風頹，專恃幹戈，遞相征伐，失正忘退，其徒實繁。略舉宏綱，斷可知矣。
"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷二·坤、屯、蒙、需集解", "底本：维基文库《周易集解（李鼎祚）/卷二》，录入页面现存正文行112—736，含坤、屯、蒙、需四卦的经文、《彖》《象》《文言》《序卦》相关文字及诸家注文。按原页面编次保留注家署名；页面末尾图像与页面信息不作正文。原页面中疑似录入讹字（如“乘马班加”“砷德”）暂按页面原貌保留，不擅自改字，待影印本校勘。译文尚未完成。来源页标示知识共享署名—相同方式共享4.0许可。", """(坤下坤上)。坤：元亨，利牝馬之貞。
幹寶曰：陰氣之始，婦德之常，故稱「元」。與乾合德，故稱「亨」。行天者莫若龍，行地者莫若馬，故乾以龍繇，坤以馬象也。坤，陰類，故稱「利牝馬之貞」矣。
虞翻曰：謂陰極陽生，乾流坤形，坤含光大，凝乾之元，終於坤亥，出乾初子，品物鹹亨，故「元亨」也。坤為牝，震為馬，初動得正，故「利牝馬之貞」矣。
君子有攸往，先迷，後得主，利。
盧氏曰：坤，臣道也、妻道也。後而不先，先則迷失道矣，故曰「先迷」。陰以陽為主，當後而順之，則利。故曰「後得主，利」。
《九家易》曰：坤為牝，為迷。
西南得朋，東北喪朋，安貞吉。
崔覲曰：妻道也。西方坤兌，南方巽離，二方皆陰，與坤同類，故曰「西南得朋」。東方艮震，北方乾坎，二方皆陽，與坤非類，故曰「東北喪朋」。以喻在室得朋，猶迷於失道；出嫁喪朋，乃順而得常。安於承天之正，故言「安貞吉」也。
《彖》曰：至哉坤元，
《九家易》曰：謂乾氣至坤，萬物資受而以生也。坤者純陰，配乾生物，亦善之始，地之象也，故又嘆言至美。
萬物資生，
荀爽曰：謂萬一千五百二十策，皆受始於乾，由坤而生也。策生於坤，猶萬物成形，出乎地也。
乃順承天。
劉瓛曰：萬物資物於地，故地承天而生也。
坤厚載物，
蜀才曰：坤以廣厚之德，載含萬物，無有窮竟也。
德合無疆。
蜀才曰：天有無疆之德，而坤合之，故雲「德合無疆」也。
含宏光大，
荀爽曰：乾二居坤五為含，坤五居乾二為弘，坤初居乾四為光，乾四居坤初為大也。
品物鹹亨。
荀爽曰：天地交，萬物生，故「鹹亨」。
崔覲曰：含育萬物為弘，光華萬物為大，動植各遂其性，故言「品物鹹亨」也。
牝馬地類，行地無疆，
侯果曰：地之所以含弘物者，以其順而承天也。馬之所以行地遠者，以其柔而伏人也。而又牝馬，順之至也。誡臣子當至順，故作易者取象焉。
柔順利貞。君子攸行。
《九家易》曰：謂坤爻本在柔順陰位，則利貞之。乾則陽爻來據之，故曰「君子攸行」。
先迷失道，後順得常。
何妥曰：陰道惡先，故先致迷失。後順於主，則保其常慶也。
西南得朋，乃與類行。
虞翻曰：謂陽得其類，月朔至望，從震至乾，與時偕行，故「乃與類行」。
東北喪朋，乃終有慶。
虞翻曰：陽喪滅坤，坤終復生，謂月三日震象出庚，故「乃終有慶」。此指說易道陰陽消息之大要也。謂陽月三日，變而成震，出庚。至月八日成兌，見丁。庚西丁南，故「西南得朋」。謂二陽為朋，故兌「君子以朋友講習」。《文言》曰：敬義立而德不孤。《彖》曰：乃與類行。二十九日，消乙入坤，滅藏於癸，乙東癸北，故「東北喪朋」。謂之以坤滅乾，坤為喪故也。
馬君雲：孟秋之月，陰氣始著，而坤之位，同類相得，故「西南得朋」。孟春之月，陽氣始著，陰始從陽，失其黨類，故「東北喪朋」。失之甚矣。而荀君以為陰起於午，至申三陰，得坤一體，故曰「西南得朋」。陽起於子，至寅三陽，喪坤一體，故曰「東北喪朋」。就如荀說，從午至申，得坤一體，故曰「西南得朋」。陽起於子，至寅三陽，喪坤一體，故曰「東北喪朋」就如荀說，從午至申，經當言南西得朋；子至寅，當言北東喪朋。以乾變坤，而言喪朋，經以乾卦為喪耶？此何異於馬也。
安貞之吉，
虞翻曰：坤道至靜，故「安」；復初得正，故「貞吉」。
應地無疆。
虞翻曰：震為應。陽正於初，以承坤陰；地道應，故「應地無疆」。
《象》曰：地勢坤，
王弼曰：地形不順，其勢順。
宋衷曰：地有上下九等之差，故以形勢言其性也。
君子以厚德載物。
虞翻曰：勢，力也。君子謂乾陽，為德動。在坤下，君子之德車。故「厚德載物」。老子曰：勝人者有力也。
初六：履霜，堅冰至。
幹寶曰：重陰，故稱六。剛柔相推，故生變。占變，故有爻。《系》曰：爻者，言乎變者也。故《易》、《系辭》皆稱九、六也。陽數奇，陰數偶，是以乾用一也。坤用二也。陰氣在初，五月之時，自姤來也。陰氣始動乎三泉之下，言陰氣之動矣。則必至於履霜，履霜則必至於堅冰，言有漸也。藏器於身，貴其俟時，故陽有潛龍，戒以「勿用」。防禍之原。欲其先幾，故陰在三泉，而顯以履霜也。
《象》曰：履霜堅冰，陰始凝也。馴致其道，至堅冰也。
《九家易》曰：霜者，乾之命也。堅冰者，陰功成也。謂坤初六之乾四，履乾命令而成「堅冰」也。此卦本乾，陰始消，陽起於此爻，故「履霜」也。馴，猶順也，言陽順陰之性，成堅冰矣。初六始姤，姤為五月。盛夏而言堅冰，五月陰氣始生地中，言始於微霜，終至堅冰，以明漸順至也。
六二：直方大。
荀爽曰：大者，陽也。二應五，五下動之，則應陽出，直布陽於四方。
不習無不利。
荀爽曰：物唱乃和，不敢先有所習。陽之所唱，從而和之，「無不利」也。幹寶曰：陰氣在二，六月之時，自遁來也。陰出地上，佐陽成物，臣道也，妻道也。臣之事君，妻之事夫，義成者也。臣貴其直，義尚其方，地體其大，故曰「直方大」。士該九德，然後可以從王事；女躬四教，然後可以配君子。道成於我，而用之於彼。不妨以仕舉為政，不妨以嫁學為婦。故曰「不習無不利」也。
《象》曰：六二之動，直以方也。
《九家易》曰：謂陽下動，應之，則直而行，布陽氣於四方也。
不習無不利，地道光也。
幹寶曰：女德光於夫，士德光於國也。
六三：含章可貞。
虞翻曰：貞，正也。以陰包陽，故「含章」。三失位，發得正，故「可貞」也。
或從王事，無成有終。
虞翻曰：謂三已發成泰，乾為王，坤為事，震為從，故「或從王事」。地道無成而有終，故「無成有終」。
幹寶曰：陰氣在三，七月之時，自否來也。陽降在四，三公位也。陰升在三，三公事也。上失其權，位在諸侯。坤體既具，陰黨成群。君弱臣強，戒在二國。唯文德之臣，然後可以遭之，運而不失其柔順之正。坤為文，坤象既成，故曰「含章可貞」。此蓋平襄之王垂拱以晉鄭之輔也。茍利社稷，專之則可，故曰「或從王事」。遷都誅親，疑於專命，故亦或之。失後順之節，故曰「無成」。終於濟國安民，故曰「有終」。
《象》曰：含章可貞，以時發也。
崔覲曰：陽命則發，非時則含也。
或從王事，知光大也。
幹寶曰：位彌高，德彌廣也。
六四：括囊，無咎，無譽。
虞翻曰：括，結也。謂泰反成否。坤為囊，艮為手，巽為繩，故括囊在外多咎也。得位承五，系於包桑，故「無咎」。陰在二多譽，而遠在四，故「無譽」。
幹寶曰：陰氣在四，八月之時，自觀來也。天地將閉，賢人必隱，懷智茍容，以觀時釁，此蓋寧戚、籧瑗與時卷舒之爻也。不艱其身，則無咎。功業不建，故無譽也。
《象》曰：括囊，無咎，慎不害也。
盧氏曰：慎言則無咎也。
六五：黃裳，元吉。
幹寶曰：陰氣在五，九月之時，自剝來也。剝者，反常道也。黃，中之色。裳，下之飾。元，善之長也。中美能黃，上美為元，下美則裳。陰登於五，柔居尊位，若成昭之主，周霍之臣也。百官總已，專斷萬機，雖情體信順，而貌近僣疑，周公其猶病諸。言必忠信，行必篤敬，然後可以取信於神明，無尤於四海也。故曰「黃裳，元吉」也。
《象》曰：黃裳，元吉，文在中也。
王肅曰：坤為文，五在中，故曰「文在中也」。
幹寶曰：當總已之任，處疑僣之間，而能終元吉之福者，由文德在中也。
上六：龍戰於野，
荀爽曰：消息之位，坤在於亥。下在伏乾，為其兼於陽，故稱「龍」也。
其血玄黃。
《九家易》曰：實本坤體，未離其類，故稱「血」焉。血以喻陰也。玄黃，天地之雜，言乾坤合居也。
侯果曰：坤，十月卦也。乾位西北，又當十月。陰窮於亥，窮陰薄陽，所以戰也。故《說卦》雲「戰乎乾」是也。六稱龍者，陰盛似龍，故稱「龍」也。
幹寶曰：陰在上六，十月之時也。爻終於酉，而卦成於乾。乾體純剛，不堪陰盛，故曰「龍戰」。戌亥，乾之都也，故稱「龍」焉。陰德過度，以逼乾戰。郭外曰郊，郊外曰野。坤位未申之維，而氣溢酉戌之間，故曰「於野」。未離陰類，故曰「血」。陰陽色雜，故曰「玄黃」。言陰陽離則異氣，合則同功。君臣夫妻，其義一也。故文王之忠於殷，抑參二之強，以事獨夫之紂。蓋欲彌縫其闕。而匡救其惡，以祈殷命，以濟生民也。紂遂長惡不悛，天命殛之。是以至於武王，遂有牧野之事，是其義也。
《象》曰：龍戰於野，其道窮也。
幹寶曰：天道窮，至於陰陽和薄也。君德窮，至於攻戰受誅也。柔順窮，至於用權變矣。
用六：利永貞。
幹寶曰：陰體其順，臣守其柔。所以秉義之和，履貞之幹。唯有推變，終歸於正。是周公始於負扆南面，以光王道，卒於復子明辟，以終臣節，故曰「利永貞」也。
《象》曰：用六，永貞，以大終也。
侯果曰：用六，妻道也，臣道也，利在長正矣。不長正，則不能大終陽事也。
《文言》曰：
何妥曰：《坤·文言》唯一章者，以一心奉順於主也。
坤至柔，
荀爽曰：純陰至順，故「柔」也。
而動也剛，
《九家易》曰：坤一變而成震。陰動生陽，故「動也剛」。
至靜而德方，
荀爽曰：坤性至靜，得陽而動，布於四方也。
後得主而有常，
虞翻曰：坤陰，先迷後順，得常。陽出初，震為主，為常也。
含萬物而化光。
幹寶曰：光，大也。謂坤含藏萬物，順承天施，然後「化光」也。
坤道其順乎，承天而時行。
荀爽曰：承天之施，因四時而行之也。
積善之家，必有余慶；
虞翻曰：謂初。乾為積善，以坤牝。陽滅出復，震為余慶，謂「東北喪朋」，乃終有慶也。
積不善之家，必有余殃。
虞翻曰：坤積不善，以臣弒君，以乾通坤，極姤生巽，為余殃也。
案：聖人設教，理貴隨宜，故夫子先論人事，則不語怪力亂神，絕四毋必。今於易象，闡揚天道，故曰「積善之家，必有余慶。積不善之家，必有余殃」者：以明陽生陰殺，天道必然。理國修身，積善為本。故於坤爻初六，陰始生時，著此微言，永為深誡。欲使防萌杜漸，災害不生，開國承家，君臣同德者也。故《系辭》雲：善不積，不足以成名。惡不積，不足以滅身。是其義也。
臣弒其君子弒其父，
虞翻曰：坤消至二，艮子弒父。至三成否，坤臣弒君。上下不交，天下無邦。故子弒父，臣弒君也。
非一朝一夕之故，其所由來者漸矣。
虞翻曰：剛爻為朝，柔爻為夕；乾為寒，坤為暑，相推而成歲焉。故「非一朝一夕」，所由來漸矣。
由辯之不早辯也。
孔穎達曰：臣子所以久包禍心，由君父不早辯明故也。此文誡君父防臣子之惡也。
《易》曰：履霜，堅冰至，蓋言順也。
荀爽曰：霜者，乾之命令。坤下有伏乾，履霜堅冰，蓋言順也。乾氣加之，性而堅，象臣順君命而成之。
直其正也，方其義也。
虞翻曰：謂二。陽稱直。乾，其靜也專，其動也直，故「直其正」。方為辟，陰開為方。坤，其靜也翕，其動也辟，故「方其以也」。
君子敬以直內，義以方外，敬義立而德不孤。
虞翻曰：陽息在二，故「敬以直內」。坤位在外，故義以方外。謂陽見兌丁，西南得朋，乃與類行，故德不孤。孔子曰：必有鄰也。
直方大，不習，無不利，則不疑其所行也。
荀爽曰：直方大，乾之唱也。不習無不利，坤之和也。陽唱陰和，而無所不利，故「不疑其所行也」。
陰雖有美，含之，以從王事，弗敢成也。
荀爽曰：六三陽位，下有伏陽。坤陰卦也。雖有伏陽，含藏不顯。以從王事，要待乾命，不敢自成也。
地道也，妻道也，臣道也。
翟元曰：坤有此三者也。
地道無成，而代有終也。
宋衷曰：臣子雖有才美，含藏以從其上，不敢有所成名也。地得終天功，臣得終君事，婦得終夫業，故曰「而代有終也」。
天地變化，草木蕃。
虞翻曰：謂陽息坤成泰。天地反，以乾變坤，坤化升乾，萬物出震，「故天地變化，草木蕃」矣。
天地閉，賢人隱。
虞翻曰：謂四。泰反成否，乾稱賢人。隱藏坤中，以儉德避難，不榮以祿，故「賢人隱」矣。
《易》曰：括囊，無咎，無譽，蓋言謹也。
荀爽曰：六四陰位，迫近於五，雖有成德，當括而囊之，謹慎畏敬也。
孔穎達曰：括，結也。囊，所以貯物，以譬心藏智也。閉其智而不用，故曰「括囊」。不與物忤，故「無咎」。功名不顯，故「無譽」也。
君子黃中通理，正位居體，
虞翻曰：謂五。坤息體觀，地色黃，坤為理。以乾通坤，故稱「通理」。五正陽位，故曰「正位」。艮為居，體謂四支也。艮為兩肱，巽為兩股。故曰「黃中通理，正位居體」。
美在其中，而暢於四支，
虞翻曰：陽稱美，在五中四支。謂股肱。
發於事業，
《九家易》曰：天地交而萬物生也。謂陽德潛藏，變則發見。若五動為比，乃事業之盛。
美之至也。
侯果曰：六五以中和通理之德，居體於正位，故能美充於中。而旁暢於萬物，形於事業，無不得宜，是「美之至也」。
陰疑於陽，必戰。
孟喜曰：陰乃上薄，疑似於陽，必與陽戰也。
為其兼於陽也，故稱龍焉。
《九家易》曰：陰陽合居，故曰「兼」。陽謂上六，坤行至亥，下有伏乾，陽者變化，以喻龍焉。
猶未離其類也，故稱血焉。
荀爽曰：實本坤卦，故曰「未離其類也」。血以喻陰順也。
崔覲曰：乾坤交會，乾為大赤，伏陰柔之，故「稱血焉」。
夫玄黃者，天地之雜也。
荀爽曰：消息之卦，坤位在亥，下有伏乾，陰陽相和，故言「天地之雜也」。
天玄而地黃。
王凱沖曰：陰陽交戰，故血玄黃。
荀爽曰：天者陽，始於東北，故色玄也。地者陰，終於西南，故色黃也。
《序卦》曰：有天地，然後萬物生焉。盈天地之間者唯萬物，故受之以屯。屯者，盈也。屯者，萬物之始生也。
崔覲曰：此仲尼序文王次卦之意也。不序乾坤之次者，以一生二，二生三，二生萬物。則天地之次第可知，而萬物之先後宜序也。萬物之始生者，言剛柔始交，故萬物資始於乾，而資生於坤。
(震下坎上)。屯：元、亨、利、貞。
虞翻曰：坎二之初，剛柔交震，故「元、亨」；之初得正，故「利、貞」矣。
勿用有攸往，利建侯。
虞翻曰：之外稱往。初震得正，起之欲應，動而失位，故「勿用有攸往」。震為侯，初剛難拔，故利以建侯。老子曰：善建者，拔也。
《彖》曰：屯，剛柔始交而難生。
虞翻曰：乾剛坤柔，坎二交初，故「始交」。確乎難拔，故「難生」也。
崔覲曰：十二月陽始浸長，而交於陰，故曰「剛柔始交」。萬物萌芽，生於地中，有寒冰之難，故言「難生」。於人事，則是運季業初之際也。
動乎險中，大亨貞。
荀爽曰：物難在始生，此本坎卦也。
案：初六升二，九二降初，是剛柔始交也。交則成震，震為動也，上有坎，是動乎險中也。動則物通而得正。故曰「動乎險中，大亨貞」也。
雷雨之動滿形，
荀爽曰：雷震雨潤，則萬物滿形而生也。
虞翻曰：震雷坎雨，坤為形也。謂三巳反正，成既濟。坎水流坤，故「滿形」。謂雷動雨施，品物流形也。
天造草昧。
荀爽曰：謂陽動在下，造物於冥昧之中也。
宜建侯而不寧。
茍爽曰：天地初開，世尚屯難，震位承乾，故「宜建侯」。動而遇險，故「不寧」也。
虞翻曰：造，造生也。草，草創物也。坤冥為昧，故「天造草昧」。成既濟定，故曰「不寧」，言寧也。
幹寶曰：水運將終，木德將始，殷周際也。百姓盈盈，匪君子不寧。天下既遭屯險之難，後王宜蕩之以雷雨之政，故封諸侯以寧之也。
《象》曰：雲雷，屯。
《九家易》曰：雷雨者，興養萬物。今言屯者，十二月雷伏藏地中，未得動出。雖有雲雨，非時長育，故言屯也。
君子以經論。
荀爽曰：屯難之代，萬事失正。經者，常駐也。論者，理也。君子以經論，不失常道也。
姚信曰：經，緯也。時在屯難，是天地經論之日，故君子法之，須經論艱難也。
初九：盤桓，利居貞，利建侯。
虞翻曰：震起艮止，動乎險中，故「盤桓」。得正得民，「利居貞」。謂君子居其室，慎密而不出也。
《象》曰：雖盤桓，誌行正也。
荀爽曰：盤桓者，動而退也。頓謂從二動，而退居初，雖盤桓，得其正也。
以貴下賤，大得民也。
荀爽曰：陽貴而陰賤，陽從二來，是以貴下賤，所以得民也。
六二：屯如邅如，
荀爽曰：陽動而止，故「屯如」也。陰乘於陽，故「邅如」也，
乘馬班加。
虞翻曰：屯邅盤桓，謂初也。震為馬作足，二乘初，故「乘馬」。班，躓也。馬不進，故「班如」矣。
匪寇婚媾，女子貞不字，十年乃字。
虞翻曰：匪非也。寇謂五。坎為寇盜，應在坎，故「匪寇」。陰陽德正，故「婚媾」。字，妊娠也。三失位，變復體離。離為女子，為大腹，故稱「字」。今失位為坤，離象不見，故「女子貞不字」。坤數十。三動反正，離女大腹。故十年反常乃字。謂成既濟定也。
《象》曰：六二之難，乘剛也。
崔覲曰：下乘初九，故為之難也。
十年乃字，反常也。
《九家易》曰：陰出於坤，今還為坤，故曰「反常也」。陰出於坤，謂乾再索而得坎。今變成震，中有坤體，故言陰出於坤。今還於坤，謂二從初即逆，應五順也。去逆就順，陰陽道正，乃能長養，故曰「十年乃字」。
六三：即鹿無虞，惟入於林中，
虞翻曰：即，就也。虞謂虞人，掌禽獸者。艮為山，山足稱麓。麓，林也。三變體坎，坎為叢木。山下，故稱「林中」。坤為兕虎；震為麋鹿，又為驚走；艮為狐狼。三變禽走入林中，故曰「即鹿無虞，惟入林中」矣。
君子幾，不如舍，往吝。
虞翻曰：君子謂陽已正位。幾，近。舍，置。吝，疵也。三應於上，之應歷險。不可以往，動如失位。故「不如舍」之，往必吝窮矣。
《象》曰：即鹿無虞，以從禽也。
案：《白虎通》雲：禽者，何鳥？獸之總名，為人所禽制也。即此卦九五爻辭。王用三驅，失前禽，是其義也。
君子舍之，往吝窮也。
崔覲曰：君子見動之微，逆知無虞，則不如舍，勿往。往則吝窮也。
六四：乘馬班如，
虞翻曰：乘，三也。謂三巳變坎，為馬，故曰「乘馬」。馬在險中，故「班如」也。或說乘初，初為建侯，安得乘之也。
求婚媾，往吉，無不利。
崔覲曰：屯難之時，勿用攸往。初雖作應，班如不進。既比於五，五來求婚。男求女，「往吉，無不利」。
《象》曰：求而往，明也。
虞翻曰：之外稱「往」。體離，故「明也」。
九五：屯其膏，
虞翻曰：坎雨稱「膏」。《詩》雲：陰雨膏之，是其義也。
小貞吉，大貞兇。
崔覲曰：得屯難之宜，有膏澤之惠。謂與四為婚媾，施雖未光，小貞之道也，故「吉」。至於遠求嘉偶，以行大正，赴二之應，冒難攸往，固宜且兇。故曰「大貞兇」也。貞，正也。
《象》曰：屯其膏，施未光也。
虞翻曰：陽陷陰中，故「未光也」。
上六：乘馬班如，
虞翻曰：乘，五也。坎為馬，震為行，艮為止，馬行而止，故「班如」也。
泣血漣如。
《九家易》曰：上六乘陽，故「班如」也。下二四爻雖亦乘陽，皆更得承五，憂解難除。今上無所復承，憂難不解，故「泣血漣如」也。體坎為血，伏離為目，互艮為手，掩目流血，泣之象也。
《象》曰：泣血漣如，何可長也。
虞翻曰：謂三變時，離為目。坎為血，震為出。血流出目，故「泣血漣如」。柔乘於剛，故不可長也。
《序卦》曰：物生必蒙，故受之以蒙。蒙者，蒙也，物之稚也。
崔覲曰：萬物始生之後，漸以長稚，故言「物生必蒙」。
鄭玄曰：蒙，幼小之貌。齊人謂萌之蒙也。
(坎下艮上)。蒙：亨。
虞翻曰：艮三之二。亨謂二。震剛柔接，故「亨」。蒙亨，以通行時中也。
幹寶曰：蒙者，離宮陰也。世在四。八月之時，降陽布德，薺麥並生，而息來在寅，故「蒙」。於世為八月，於消息為正月卦也。正月之時，陽氣上達，故屯為物之始生，蒙為物之稚也。施之於人，則童蒙也。敬得其運，雖蒙必亨，故曰「蒙亨」。此蓋以寄成王之遭周公也。
匪我求童蒙，童蒙求我。
虞翻曰：童蒙謂五，艮為童蒙。我謂二也。震為動起，嫌求之五，故曰「匪我求童蒙」。五陰求陽，故「童蒙求我，誌應也」。艮為求，二體師象，坎為經。謂禮有來學，無往教。
初筮告，再三瀆，瀆則不告，
崔覲曰：初筮，謂六五求決於九二。二則告之。再三瀆，謂三應於上。四隔於三，與二為瀆，故二「不告」也。瀆，古黷字也。
利貞。
虞翻曰：二五失位，利變之正，故「利貞」。蒙以養正，聖功也。
《彖》曰：蒙，山下有險，險而止，蒙。
侯果曰：艮為山，坎為險，是山下有險。險被山止，止則未通，蒙昧之象也。
蒙亨，以亨行時中也。
荀爽曰：此本艮卦也。案：二進居三，三降居二。剛柔得中，故能通。發蒙時，令得進中矣。故曰「蒙亨，以亨行時中也」。
匪我求童蒙，
陸績曰：六五陰爻，在蒙暗。蒙又體艮，少男，故曰「童蒙」。
童蒙求我，誌應也。
荀爽曰：二與五誌相應也。
初筮告，以剛中也。
崔覲曰：以二剛中，能發於蒙也。
再三瀆，瀆則不告，瀆蒙也。
荀爽曰：再三，謂三與四也。皆乘陽，不敬，故曰「瀆」。瀆不能尊陽，蒙氣不除，故曰「瀆蒙也」。
蒙以養正，聖功也。
虞翻曰：體頤，故養。五多功。聖謂二，二誌應五，變得正而亡其蒙，故「聖功也」。
幹寶曰：武王之崩，年九十三矣。而成王八歲，言天後成王之年，將以養公正之道，而成三聖之功。
《象》曰：山下出泉，蒙。
虞翻曰：艮為山，震為出，坎象流出，故「山下出泉」。
君子以果行育德。
虞翻曰：君子謂二。艮為果，震為行。育，養也。二至上有頤養象，故「以果行育德」也。
初六：發蒙，利用刑人，用說桎梏，以往吝。
虞翻曰：發蒙之正，初為蒙始，而失其位。發蒙之正以成兌，兌為刑人，坤為用，故曰「利用刑人」矣。坎為穿木，震足艮手，互與坎連，故稱「桎梏」。初發成兌，兌為說，坎象毀壞，故曰「用說桎梏」。之應歷險，故「以往吝」。吝，小疵也。
《象》曰：利用刑人，以正法也。
虞翻曰：坎為法，初發之正，故「正法也」。
幹寶曰：初六戊寅，平明之時，天光始照，故曰「發蒙」。此成王始覺周公至誠之象也。坎為法律，寅為貞廉，以貞用刑，故「利用刑人」矣。此成王將正四國之象也。說，解也。正四國之罪。宜釋周公之黨，故曰「用說桎梏」。既感金滕之文，追恨昭德之晚，故曰「以往吝」。初二失位，吝之由也。
九二：包蒙，吉。納婦，吉。子克家。《象》曰：子克家，剛柔接也。
虞翻曰：坤為包。應五據初，初與三四同體，包養四陰，故「包蒙，吉」。震剛為夫，伏巽為婦，二以剛接柔，故「納婦，吉」。二稱家。震，長子，主器者。納婦成初。故有「子克家」也。
六三：勿用娶女，見金夫，不有躬，無攸利。
虞翻曰：謂三誡上也。金夫謂二。初發成兌，故三稱「女」。兌為見。陽稱金。震為夫。三逆乘二陽，所行不順，為二所淫。上來之三，陟陰。故曰「勿用娶女，見金夫」矣。坤身稱躬，三為二所乘，兌澤動下，不得之應，故「不有躬」。失位多兇，故「無攸利」也。
《象》曰：勿用娶女，行不順也。
虞翻曰：失位乘剛，故「行不順也」。
六四：困蒙，吝。《象》曰：困蒙之吝，獨遠實也。
王弼曰：陽稱實也。獨遠於陽，處兩陰之中，暗莫之發，故曰「困蒙」也。困於蒙昧，不能比賢以發其誌，亦鄙矣，故曰「吝」。
六五：童蒙，吉。
虞翻曰：艮為童蒙，處貴承上，有應於二，動而成巽，故「吉」也。
《象》曰：童蒙之吉，順以巽也。
荀爽曰：順於上，巽於二，有似成王任用周召也。
上九：擊蒙，不利為寇，利禦寇。
虞翻曰：體艮為手，故「擊」。謂五巳變，上動成坎，稱寇。而逆乘陽，故「不利為寇」矣。禦，止也。此寇謂二。坎為寇，巽為高，艮為山，登山備下，順有師象，故「利禦寇」也。
《象》曰：利用禦寇，上下順也。
虞翻曰：自上禦下，故「順」也。
《序卦》曰：物稚不可不養也。故受之以需。需者，飲食之道也。
幹寶曰：需，坤之遊魂也。雲升在天，而雨未降，翺翔東西，須之象也。王事未至，飲宴之日也。夫坤者，地也；婦人之職也；百谷果蓏之所生，禽獸魚鱉之所托也；而在遊魂變化之家，即烹爨腥實以為和味者也。故曰「需者，飲食之道也」。
(乾下坎上)。需：有孚，光亨，貞吉。
虞翻曰：大壯四之五。孚謂五。離日為光。四之五，得位正中。故「光亨」。「貞吉」，謂「壯於大輿之輻也」。
利涉大川。
何妥曰：大川者，大難也。須之待時，本欲涉難，既能以信而待，故可以「利涉大川」矣。
《彖》曰：需，須也。險在前也，
何妥曰：此明得名由於坎也。坎為險也。有險在前，不可妄涉，故須待時然後動也。
剛健而不陷。其義不困窮矣。
侯果曰：乾體剛健，遇險能通，險不能險，義不窮也。
需，有孚，光亨，貞吉。位乎天位，以正中也。
蜀才曰：此本大壯卦。
案：六五降四，有孚，光亨，貞吉。九四升五，位乎天位，以正中也。
利涉大川，往有功也。
虞翻曰：謂二失位，變而涉坎，坎為大川。得位應五，故「利涉大川」。五多功，故「往有功也」。
《象》曰：雲上於天，需。
宋衷曰：雲上於天，須時而降也。
君子以飲食宴樂。
虞翻曰：君子謂乾。坎水兌口。水流入口為飲。二失位，變體噬嗑，為食，故「以飲食」。陽在內，稱宴。大壯，震為樂，故「宴樂」也。
初九：需於郊，利用恒，無咎。
幹寶曰：郊，乾坎之際也。既已受命，進道北郊，未可以進，故曰「需於郊」。處不避汙，出不辭難，臣之常節也。得位有應，故「利用恒」。雖小稽留，終於必達，故曰「無咎」。
《象》曰：需於郊，不犯難行也。利用恒無咎，未失常也。
王弼曰：居需之時，最遠於險；能抑其進，不犯難行；雖不應幾，可以保常，故「無咎」。
九二：需於沙，小有言，終吉。
虞翻曰：沙謂五。水中之陽稱沙也。二變之陰，稱小。大壯震為言，兌為口。四之五，震象半見，故「小有言」。二變應之，故「終吉」。
《象》曰：需於沙，衍在中也。
虞翻曰：衍，流也。中謂五也。
荀爽曰：二應於五，水中之剛，故曰「沙」。知前有沙漠。而不進也。體乾處和，美德優衍在中，而不進也。
雖小有言，以吉終也。
荀爽曰：二與四同功，而三據之，故「小有言」。乾雖在下，終當升上，二當居五，故「終吉也」。
九三：需於泥，致寇至。
荀爽曰：親與坎接，故稱「泥」。須止不進，不取於四，不致寇害。
《象》曰：需於泥，災在外也。
崔覲曰：泥近乎外者也。三逼於坎，坎為險盜，故「致寇至」。是災在外也。
自我致寇，敬慎不敗也。
虞翻曰：離為戎，乾為敬。陰消至五遁，臣將弒君。四上壯坤，故「敬慎不敗」。
六四：需於血，出自穴。
案：六四體坎。坎為雲，又為血卦。血以喻陰。陰體卑弱，宜順從陽，故曰「需於血」。
《九家易》曰：雲從地出，上升於天。自地出者，莫不由穴。故曰「需於血，出自穴」也。
《象》曰：需於血，順以聽也。
王弼曰：穴者，陰之路也。四處坎始，居穴者也。九三剛進，四不能距，見侵則避，順以聽命也。
《九家易》曰：雲欲升天，須時當降。順以聽五，五為天也。
九五：需於酒食，貞吉。
荀爽曰：五互離，坎水在火上，酒食之象。需者，飲食之道。故坎在需家，為酒食也。雲須時欲降，乾須時當升。五有剛德，處中居正。故能帥群陰，舉坎以降。陽能正居其所，則吉。故曰「需於酒食」也。
《象》曰：酒食貞吉，以中正也。
《九家易》曰：謂乾二當升五，正位者也。
盧氏曰：沈湎則兇，中正則吉也。
上六：入於穴，
荀爽曰：需道已終，雲當下入穴也。雲上升極，則降而為雨，故《詩》雲：朝躋於西，崇朝其雨，則還入地，故曰「入於穴」。雲雨入地，則下三陽動而自至者也。
有不速之客三人來，敬之終吉。
荀爽曰：三人，謂下三陽也。須時當升，非有召者，故曰「不速之客」焉。乾升在上，君位以定；坎降在下，當循臣職。故「敬之終吉」也。
《象》曰：不速之客來，敬之終吉。雖不當位，未大失也。
荀爽曰：上降居三，雖不當位，承陽有實，故「終吉」，無「大失」矣。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷三·讼、师、比、小畜、履集解", "底本：维基文库《周易集解（李鼎祚）/卷三》，录入页面现存正文行112—656，含讼、师、比、小畜、履五卦经文及《彖》《象》《序卦》相关文字和诸家注文。保留原页面编次、注家署名与可疑字形；页面末尾图像及页面信息不作正文。译文尚未完成。来源页标示知识共享署名—相同方式共享4.0许可。", """《序卦》曰：飲食必有訟，故受之以訟也。
鄭玄曰：訟，猶爭也，言飲食之會恒多爭也。
(坎下乾上)。訟：有孚，
幹寶曰：訟，離之遊魂也。離為戈兵，此天氣將刑殺，聖人將用師之卦也。訟，不親也。兆民未識天命不同之意。
荀爽曰：陽來居二，而孚於初，故曰「訟有孚」矣。
窒惕，中吉，
虞翻曰：遁三之二也。孚謂二。窒，塞止也。惕，懼二也。二失位，故不言貞。遁將成否，則子弒父，臣弒君。三來之二，得中；弒不得行，故「中吉」也。
終兇。
虞翻曰：二失位，終止不變，則入於淵，故「終兇」也。
利見大人，不利涉大川。
侯果曰：大人，謂五也。斷決必中，故「利見」也。訟是陰事，以險涉險，故「不利涉大川」。
《彖》曰：訟，上剛下險，險而健，訟。
盧氏曰：險而健者，恒好爭訟也。
訟，有孚窒惕，中吉，剛來而得中也。
蜀才曰：此本遁卦。
案：二進居三，三降居二，是剛來而得中也。
終兇，訟不可成也。
王肅曰：以訟成功者，終必兇也。
王弼曰：凡不和而訟，無施而可，涉難特甚焉。唯有信而見塞懼者，乃可以得吉也。猶復不可以終，中乃吉也。不閉其源，使訟不至，雖每不枉，而訟至終竟，此亦兇矣。故雖復有信，而見塞懼，猶不可以為終，故曰「訟，有孚，窒惕，中吉，終兇」也。無善聽者，雖有其實，何由得明。而令有信塞懼者，得其中吉，必有善聽之主焉。其在二乎？以剛而來，正夫群小，斷不失中，應其任矣。
案：夫為訟，善聽之主者，其在五焉。何以明之？案爻辭：九五：訟，元吉。王氏註雲：處得尊位，為訟之主，用其中正，以斷枉直。即《彖》雲：利見大人，尚中正，是其義也。九二《象》曰：不克訟，歸逋竄也。自下訟上，患至掇也。九二居訟之時，自救不暇。訟既不克，懷懼逃歸，僅得免其終兇禍，豈能為善聽之主哉！年代綿流，師資道喪，恐傳寫字誤，以五為二，後賢當審詳之也。
利見大人，尚中正也。
荀爽曰：二與四訟，利見於五。五以中正之道，解其訟也。
不利涉大川，入於淵也。
荀爽曰：陽來居二，坎在下，為淵。
《象》曰：天與水違行，訟。
荀爽曰：天自西轉，水自東流，上下違行，成訟之象也。
君子以作事謀始。
虞翻曰：君子謂乾。三來變坤，為作事。坎為謀。乾知大始。故「以作事謀始」。
幹寶曰：省民之情，以制作也。武王故先觀兵孟津，蓋以卜天下之心，故曰「作事謀始」也。
初六：不永所事，小有言，終吉。
虞翻曰：永，長也。坤為事，初失位，而為訟始，故「不永所事」也。小有言，謂初四易位成震，言三食舊德，震象半見，故「小有言」。初變得正，故「終吉」也。
《象》曰：不永所事，訟不可長也。雖小有言，其辯明也。
盧氏曰：初欲應四，而二據之。蹔爭，事不至永。雖有小訟，訟必辯明，故「終吉」。
九二：不克訟，歸而逋。
虞翻曰：謂與四訟，坎為隱伏，故「逋」乾位剛在上，坎濡失正，故「不克」也。
其邑人三百戶，無眚。
虞翻曰：眚，災也，坎為眚，謂二變應五。乾為百，坤為戶；三爻，故三百戶。坎化為坤，故「無眚」。
《象》曰：不克訟，歸逋竄也。
荀爽曰：三不克訟，故逋而歸。坤稱邑。二者，邑中之陽人。逋，逃也，謂逃失邑中之陽人。
自下訟上，患至掇也。
荀爽曰：下與上爭，即取患害，如拾掇小物而不失也。坤有三爻，故雲「三百戶，無眚」。二者，下體之君。君不爭，則百姓無害也。
六三：食舊德，貞厲，終吉。
虞翻曰：乾為舊德。食謂初。四二已變之正。三動得位，體噬嗑食。四變食乾，故「食舊德」。三變在坎，正危貞厲，得位二，故「終吉」也。
或從王事，無成。
虞翻曰：乾為王。二變否時，坤為事，故「或從王事」。道無成而代有終，故曰「無成」。坤三同義也。
《象》曰：食舊德，從上吉也。
侯果曰：雖失其位，專心應上，故能保全舊恩，食舊德者也。處兩剛之間，而皆近不相得。乘二負四，正之危也。剛不能侵，故「終吉」也。
九四：不克訟，復即命渝，安貞吉。
虞翻曰：失位，故「不克訟」。渝變也。不克訟，故復位。變而成巽，巽為命令，故「復即命渝」。動而得位，故「安貞吉」。謂二已變坤安也。
《象》曰：復即命渝，安貞吉，不失也。
侯果曰：初既辯明，四訟妄也。訟既不克，當反就前理。變其詔命，則安靜貞吉而不失初也。
九五：訟，元吉。《象》曰：訟，元吉，以中正也。
王肅曰：以中正之德，齊乖爭之俗，元吉也。
王弼曰：處得尊位，為訟之主。用其中正，以斷枉直。中則不過，正則不邪，剛則無所溺，公則無所偏。故「訟，元吉」。
上九：或錫之鞶帶，
虞翻曰：錫謂王之錫命。鞶帶，大帶。男子鞶革。初四已易位，三二之正，巽為腰帶，故「鞶帶」。
終朝三拕之。
虞翻曰：位終乾上。二變時，坤為終。離為日，乾為甲。日出甲上。故稱「朝」。應在三，三變時，艮為手，故「終朝三拕之」。使變應已，則去其鞶帶。體坎乘陽，故象曰「不足敬也」。
侯果曰：褫(拕)，解也。乾為衣、為言，故以訟受服。
荀爽曰：二四爭三，三本下體，取之有緣。或者，疑之辭也。以三錫二，於義疑矣。爭競爭之世，分理未明，故或以錫二。終朝者，君道明。三者，陽成功也。君明道盛，則奪二與四，故曰「終朝三拕之」也。鞶帶，宗廟之服。三應於上，上為宗廟。故曰「鞶帶」也。
翟玄曰：上以六三錫下二陽，群剛交爭，得不以讓，故終一朝之間，各一奪之，為三拕。
《象》曰：以訟受服，亦不足敬也。
虞翻曰：服謂鞶帶。終朝見褫(拕)，乾象毀壞。故「不足敬」。
《九家易》曰：初、二、三、四皆不正，以不正相訟，而得其服，故「不足敬」也。
《序卦》曰：訟必有眾起，故受之以師。師者，眾也。
崔覲曰：因爭必起眾相攻，故受之以師也。
(坎下坤上)。師：貞，丈人吉，無咎。
何晏曰：師者，軍旅之名。故《周禮》雲：二千五百人為師也。
王弼曰：丈人，嚴莊之稱，有軍正者也。為師之正，丈人乃吉。興役動眾，無功則罪，故「吉」乃「無咎」。
陸績曰：丈人者，聖人也。帥師未必聖人，若漢高祖、光武應此義也。
崔覲曰：《子夏傳》作大人，並王者之師也。
案：此《彖》雲：師，眾。貞，正也。能以眾正，可以王矣。故老子曰：域中有四大，而王居其一焉。由是觀之，則知夫為王者，必大人也，豈以丈人而為王哉！故，《乾·文言》曰：夫大人與天地合其德，與日月合其明，先天而天不違，後天而奉天時，天且不違，而況於人乎！況於行師乎！以斯而論，《子夏傳》作大人，是也。今王氏曲解大人為丈人，臆雲「嚴莊之稱」，學不師古，匪說攸聞，既誤違於經旨，輒改正作大人明矣。
《彖》曰：師，眾也。貞，正也。能以眾正，可以王矣。
虞翻曰：坤為眾，謂二失位，變之五，為比。故「能以眾正」，乃「可以王矣」。
荀爽曰：謂二有中和之德，而據群陰，上居五位，可以王也。
剛中而應，行險而順，
蜀才曰：此本剝卦。案：上九降二，六二升上，是剛中而應，行險而順也。
以此毒天下，而民從之，
幹寶曰：坎為險，坤為順，兵革刑獄，所以險民也。毒民於險中，而得順道者，聖王之所難也。毒，荼，苦也。五刑之用，斬刺肌體，六軍之鋒，殘破城邑，皆所以荼毒奸兇之人，使服王法者也。故曰「以此毒天下，而民從之」。毒以治民，明不獲已而用之，故於彖象六爻，皆著戒懼之辭也。
吉又何咎矣。
崔覲曰：剛能進義，中能正眾，既順且應，行險戡暴，亭毒天下，人皆歸往而以為王，「吉又何咎矣」。
《象》曰：地中有水，師。
陸績曰：坎在坤內，故曰「地中有水」。師，眾也。坤中眾者，莫過於水。
君子以容民畜眾。
虞翻曰：君子謂二。容，寬也。坤為民眾，又畜養也。陽在二，寬以居之，五變執言時，有頤養象。故「以容民畜眾矣」。
初六：師出以律，否臧兇。《象》曰：師出以律，失律兇也。
案：初六以陰居陽，履失其位，位既匪正，雖令不從。以斯行師，失律者也。凡首率師出必以律，若不以律，雖臧亦兇。故曰「師出以律，失律兇也」。
《九家易》曰：坎為法律也。
九二：在師中，吉，無咎。王三錫命。《象》曰：在師中吉，承天寵也。
《九家易》曰：雖當為王，尚在師中，為天所寵，事克功成。故「吉，無咎」。二非其位，蓋謂武王受命而未即位也。受命為王，定天下以師。故曰「在師中，吉」。
王三錫命，懷萬邦也。
荀爽曰：王謂二也。三者，陽德成也。德純道盛，故能上居王位，而行錫命，群陰歸之。故曰「王三錫命，懷萬邦也」。
案：二互體震，震木。數三，王三錫命之象。《周禮》雲：一命受職，再命受服，三命受位，是其義也。
六三：師或輿屍，兇。
虞翻曰：坤為屍，坎為車多眚。同人離為戈兵，為折首：失位乘剛無應，屍在車上，故「輿屍，兇」矣。
《象》曰：師或輿屍，大無功也。
盧氏曰：失位乘剛，內外無應，以此帥師必大敗。故有輿屍之兇，功業大喪也。
六四：師左次，無咎。
荀爽曰：左謂二也，陽稱左。次，舍也。二與四同功，四承五，五無陽，故呼二舍於五，四得承之，故「無咎」。
《象》曰：左次無咎，未失常也。
崔覲曰：偏將軍居左。左次，常備師也。師順用柔，與險無應，進取不可，次舍無咎，得位故也。
六五：田有禽，利執言，無咎。
虞翻曰：田謂二。陽稱禽。震為言。五失位，變之正，艮為執。故「利執言，無咎」。
荀爽曰：田，獵也。謂二帥師禽五，五利度二之命，執行其言，故「無咎」也。
案：六五居尊失位，在師之時，蓋由殷紂而被武王擒於鹿臺之類是也。以臣伐君，假言田獵。六五離爻，體坤，離為戈兵，田獵行師之象也。
長子帥師，
虞翻曰：長子謂二，震為長子，在師中，故「帥師」也。
弟子輿屍，貞兇。
虞翻曰：弟子謂三。三體坎。坎震之弟而乾之子，失位乘陽，逆，故「貞兇」。
《象》曰：長子帥師，以中行也。
荀爽曰：長子謂九二也。五處中應二，二受任帥師，當上升五。故曰「長子帥師，以中行也」。
弟子輿屍，使不當也。
宋衷曰：弟子謂六三也。失位乘陽，處非所據。眾不聽從，師人分北，或敗績死亡，輿屍而還，故曰「弟子輿屍」。謂使不當其職也。
上六：大君有命，
虞翻曰：同人，乾為大君巽為有命。
幹寶曰：大君，聖人也。有命，天命也。五常為王位，至師之家，而變其例者。上為郊也。故易位，以見武王親征，與師人同處於野也。離上九曰：王用出征，有嘉折首。上六為宗廟，武王以文王行，故正開國之辭於宗廟之爻，明已之受命，文王之德也。故《書·泰誓》曰：予克紂，非予武，惟聯文考無罪。紂克予，非朕文考有罪，惟予小子無良。開國，封諸侯也。承家，立都邑也。小人勿用，非所能矣。
開國承家，
虞翻曰：承，受也。坤為國。二稱家。謂變乾為坤，欲令二上居五，為比，故「開國承家」。
荀爽曰：大君，謂二。師旅已息，既上居五，當封賞有功，立國命家也。開國，封諸侯。承家，立大夫也。
宋衷曰：陽當之五，處坤之中，故曰「開國」。陰下之二，在二承五，故曰「承家」。開國，謂析土地以封諸侯，如武王封周公七百裏地也。承家立大夫，為差次立大夫。因采地名，正其功勛，行其賞祿。
小人勿用。
虞翻曰：陰稱小人，坤虛無君，體迷復兇，坤成乾滅，以弒君，故「小人勿用」。
《象》曰：大君有命，以正功也。
虞翻曰：謂五多功，五動正位，故「正功也」。
幹寶曰：湯武之事。
小人勿用，必亂邦也。
虞翻曰：坤反君道，故「亂邦也」。
幹寶曰：楚靈、齊閔，窮兵之禍也。
《序卦》曰：眾必有所比，故受之以比。比者，比也。
崔覲曰：方以類聚，物以群分，人眾，則群類必有所比矣。上比相阿黨，下比相和親也。相黨則相親，故言比者比也。
(坤下坎上)。比：吉。
虞翻曰：師二上之五，得位，眾陰順從，比而輔之，故「吉」。與大有旁通。
《子夏傳》曰：地得水而柔，水得土而流，比之象也。夫兇者生乎乖爭，今既親比，故雲「比吉」也
原筮元永貞，無咎。不寧方來，後夫兇。
幹寶曰：比者，坤之歸魂也。亦世於七月，而息來在已。去陰居陽，承乾之命，義與師同也。原，卜也。《周禮》三卜，一曰原兆。砷德變化，反歸其所，四方既同，萬國既親，故曰「比吉」。考之蓍龜，以謀王業，大相東土，卜惟洛食，遂乃定鼎郟鄏。卜世三十，卜年七百，德善長於兆民，戩祿永於被業，故曰「原筮元永貞」。逆取順守。居安如危，故曰「無咎」。天下歸德，不唯一方，故曰「不寧方來」。後服之夫，違夭失人，必災其身故曰「後夫兇」也。
《彖》曰：比，吉也。比輔也。下順從也。
崔覲曰：下比於上，是下順也。
原筮元永貞，無咎，以剛中也。
蜀才曰：此本師卦。
案：六五降二，九二升五，剛往得中，為比之主。故能原究筮道，以求長正，而「無咎」矣。
不寧方來，上下應也。
虞翻曰：水性流動，故「不寧」。坤陰為方，上下應之，故「方來」也。
後夫兇，
虞翻曰：後謂上，夫謂五也。坎為後，艮為背。上位在背後，無應乘陽，故「後夫兇」也。
其道窮也。
荀爽曰：後夫謂上六。逆禮乘陽，不比聖王，其義當誅，故「其道窮兇」也。
《象》曰：地上有水，比。
何宴曰：水性潤下，今在地上，更相浸潤，比之義也。
先王以建萬國，親諸侯。
虞翻曰：先王謂五。初陽已復。震為建，為諸侯。坤為萬國，為腹，坎為心。腹心親比，故「以建萬國，親諸侯」。《詩》曰：公侯腹心，是其義也。
初六：有孚比之，無咎。
虞翻曰：孚謂五。初失位，變來得正，故「無咎」也。
荀爽曰：初在應外，以喻殊俗，聖王之信，光被四表。絕域殊俗，皆來親比，故「無咎」也。
有孚盈缶，終來有它，吉。
虞翻曰：坤器為缶。坎水流坤。初動成屯。屯者，盈也，故「盈缶」。終變得正，故「終來有它吉」。在內稱來也。
《象》曰：比之初六，有它吉也。
荀爽曰：缶者應內，以喻中國。孚既盈滿中國，終來及初，非應，故曰「它」也。《象》雲：「有它吉」者，謂信及非應，然後吉也。
六二：比之自內，貞吉。
幹寶曰：二在坤中。坤，國之象也。得位應五，而體寬大，君樂民人自得之象也，故曰「比之自內，貞吉」矣。
《象》曰：比之自內，不自失也。
崔覲曰：自內而比，不失已親也。
六三：比之匪人。
虞翻曰：匪，非也，失位無應，三又多兇，體剝傷象，弒父弒君，故曰「匪人」。
《象》曰：比之匪人，不亦傷乎。
幹寶曰：六三乙卯，坤之鬼吏。要比之家，有土之君也。周為木德，卯為木辰，同姓之國也。爻失其位，辰體陰賊，管蔡之象也。比建萬國，唯去此人。故曰「比之匪人」，不亦傷王政也。
六四：外比之，貞吉。
虞翻曰：在外體，故稱外。得位比賢，故貞吉也。
《象》曰：外比於賢，以從上也。
幹寶曰：四為三公，在比之家，而得其位。上比聖主，下禦列國，方伯之象也。能外親九服，賢德之君，務宣上誌，綏萬邦也。故曰「外比於賢，以從上也」。
九五：顯比。
虞翻曰：五貴多功，得位正中，初三以變體重明，故「顯比」。謂顯諸仁也。
王用三驅，失前禽，
虞翻曰：坎五稱王。三驅，謂驅下三陰，不及於初，故「失前禽」。謂初已變成震，震為鹿，為驚走，鹿之斯奔，則「失前禽」也。
邑人不戒，吉。
虞翻曰：坤為邑師，震為人師。時坤虛無君，使師二上居五中，故不誡吉也。
《象》曰：顯比之吉，位正中也。
虞翻曰：謂離象明。正上中也。
舍逆取順，失前禽也。
虞翻曰：背上六，故「舍逆」。據三陰，故「取順」。不及初，故「失前禽」。
邑人不戒，上使中也。
虞翻曰：謂二，使師二上居五中。
上六：比之無首，兇。
荀爽曰：陽欲無首，陰以大終。陰而無首，不以大終。故「兇」也。
虞翻曰：首，始也。陰道無成，而代有終，「無首，兇」。
《象》曰：比之無首，無所終也。
虞翻曰：迷失道，故無所終也。
《序卦》曰：比必有所畜，故受之以小畜。
崔覲曰：下順從而上下應之，則有所畜矣。
韓康伯曰：由比而畜，故曰「小畜」也。
(乾下巽上)。小畜：亨。
侯果曰：四為畜主，體又稱小，唯九三被畜，下剛皆通，是以「小畜，亨」也。
密雲不雨，自我西郊。
崔覲曰：雲如不雨，積我西邑之郊，施澤未通，以明小畜之義。
案：雲雨者，陰之氣也。今小畜五陽而一陰，既微少，才作密雲，故未能為雨。四互居兌，西郊之象也。
《彖》曰：小畜，柔得位而上下應之，曰小畜。
王弼曰：謂六四也。成卦之義，在此一爻者也。體無二陰，以分其應，既得其位，而上下應之，三不能陵，小畜之義。
健而巽，剛中而誌行，乃亨。
虞翻曰：需上變為巽，與豫旁通。豫四之坤初為復，復小陽潛，所畜者少，故曰「小畜」。二失位，五剛中正，二變應之，故「誌行乃亨」也。
密雲不雨，尚往也。
虞翻曰：密，小也。兌為密。需坎升天為雲，墜地稱雨。上變為陽，坎象半見。故「密雲不雨，尚往也」。
自我西郊，施未行也。
虞翻曰：豫坤為自我。兌為西，乾為郊，雨生於西，故「自我西郊」。九二未變，故「施未行」矣。
荀爽曰：體兌位秋，故曰「西郊」也。時當收斂，臣不專賞，故「施未行」。喻文王也。
《象》曰：風行天上，小畜。
《九家易》曰：風者，天之命令也。今行天上，則是令未下行，畜而未下，小畜之義也。
君子以懿文德。
虞翻曰：君子，謂乾。懿，美也。豫坤為文。乾為德，離為明。初至四體夬，為書契。乾離照坤，故懿文德也。
初九：復自道，何其咎，吉。《象》曰：復自道，其義吉也。
虞翻曰：謂從豫四之初成復卦，故復自道。出入無疾，「朋來無咎，何其咎，吉。」乾稱道也。
九二：牽復，吉。
崔覲曰：四柔得位，群剛所應，二以中和牽復自守，不失於行也。
《象》曰：牽復在中，亦不自失也。
虞翻曰：變應五，故不自失，與比二同義也。
九三：車說輻，
虞翻曰：豫、坤為車，為輻。至三成乾，坤象不見，故「車說輻」。馬君及俗儒皆以乾為車，非也。
夫妻反目。
虞翻曰：豫震為夫，為反。巽為妻。離為目。今夫妻共在四，離火動上，目象不正，巽多白眼，夫妻反目。妻當在內，夫當在外，今妻乘夫而出在外，象曰「不能正室」。三體離需，飲食之道。飲食有訟，故爭而反目也。
《象》曰：夫妻反目，不能正室也。
《九家易》曰：四互體離，離為目也。離既不正，五引而上，三引而下，故反目也。輿以輪成車，夫以妻成室。今以妻乘夫，其道逆，故「不能正室」。
六四：有孚，血去惕出，無咎。
虞翻曰：孚謂五。豫坎為血，為惕。惕，憂也。震為出，變成小畜，坎象不見，故血去惕出。得位承五，故「無咎」也。
《象》曰：有孚惕出，上合誌也。
荀爽曰：血以喻陰。四陰，臣象。有信順五。惕，疾也。四當去初，疾出從五，故曰「上合誌也」。
九五：有孚攣如，富以其鄰。
虞翻曰：孚五，謂二也。攣，引也。巽為繩。豫、艮為手。二失位，五欲其變，故曰「攣如」。以，及也。五貴，稱富。鄰，謂三。兌西震東，稱鄰。二變承三，故富以其鄰，《象》曰「不獨富」。二變為既濟，與東西鄰同義。
《象》曰：有孚攣如，不獨富也。
《九家易》曰：有信，下三爻也。體巽，故「攣如」，如謂連接其鄰。鄰，謂四也。五以四陰作財，與下三陽共之，故曰「不獨富也」。
上九：既雨既處，尚得載，婦貞厲。
虞翻曰：既，已也。應在三。坎水零雨。巽為處，謂二已變，三體坎雨，故「既雨既處」。坎雲復天，坎為車，積載在坎上，故上得積載。巽為婦，坎成巽壞，故「婦貞厲」。
月幾望，君子征兇。
虞翻曰：幾，近也。坎月離日，上已正，需時成坎，與離相望。兌西震東，日月象對，故「月幾望」。上變陽消，之坎為疑，故「君子征，有所疑」矣。與歸妹、中孚「月幾望」義同也。
《象》曰：既雨既處，得積載也。
虞翻曰：巽消承坎，故「得積載」。坎習為積也。
君子征兇，有所疑也。
虞翻曰：變坎為盜，故「有所疑也」。
《序卦》曰：物畜然後有禮，故受之以履。
崔覲曰：履，禮也。物畜不通，則君子先懿文德，然後以禮導之，故言「物畜物後有禮」也。
(兌下乾上)。履：履虎尾，不咥人。亨，利貞。
虞翻曰：謂變訟初為兌也。與謙旁通。以坤履乾，以柔履剛，謙坤為虎，艮為尾，乾為人，乾兌乘謙，震足蹈艮，故「履虎尾」。兌說而應虎口，與上絕，故「不咥人」。剛當位，故通。俗儒皆以兌為虎，乾履兌，非也。兌剛鹵，非柔也。
《彖》曰：履，柔履剛也。
虞翻曰：坤柔乾剛，謙坤籍乾，故「柔履剛」。
荀爽曰：謂三履二也。二五無應，故無元。以乾履兌，故有通。六三履二非和正，故雲「利貞」也。
說而應乎乾，
虞翻曰：說，兌也。明兌不履乾，故言應也。
《九家易》曰：動來為兌為應上，故曰「說而應乎乾」也。以喻一國之君，應天子命以臨下。承上以巽，據下以說，其正應天，故虎為之「不咥人」也。
是以履虎尾，不咥人。亨。
《九家易》曰：虎尾，謂三也。三以說道履五，之應上順於天，故「不咥人，亨」也。能巽說之道，順應於五，故雖踐虎，不見咥噬也。太平之代，虎不食人，亨謂於五也。
剛中正，履帝位而不疚，光明也。
虞翻曰：剛中正，謂五。謙震為帝。五，帝位，坎為疾病，乾為大明。五履帝位，坎象不見。故「履帝位而不疚，光明也」。
《象》曰：上天下澤，履。君子以辯上下，定民誌。
虞翻曰：君子，謂乾。辯，別也。乾天為上，兌澤為下。謙坤為民，坎為誌。謙時坤在乾上，變而為履。故「辯上下，定民誌也」。
初九：素履，往無咎。
虞翻曰：應在巽，為白，故「素」。履四失位，變往得正，故「往無咎」。初已得正，使四獨變，在外稱往。《象》曰：「獨行願也」。
《象》曰：素履之往，獨行願也。
荀爽曰：初九者，潛位。隱而未見，行而未成。素履者，謂布衣之士，未得居位，獨行禮義，不失其正，故「無咎也」。
九二：履道坦坦，幽人貞吉。
虞翻曰：二失位。變成震，為道、為大塗，故「履道坦坦」。訟時二在坎獄中，故稱幽人。之正得位，震出兌說，幽人喜笑，故「貞吉」也。
《象》曰：幽人貞吉，中不自亂也。
虞翻曰：雖幽訟獄中，終辯得正，故「不自亂」。
六三：眇而視，跛而履，
虞翻曰：離目不正，兌為小，故「眇而視」。視上應也。訟坎為曳。變震時，為足。足曳，故跛而履。俗儒多以兌刑為跛，兌折震足，為刑人見刑斷足者，非為跛也。
履虎尾，咥人，兇。
虞翻曰：艮為尾，在兌下，故「履虎尾」。位在虎口中，故「咥人兇」。既跛又眇，視步不能，為虎所嚙，故「咥人兇」。《象》曰：「位不當也」。
武人為於大君。
虞翻曰：乾象在上，為武人。三失位，變而得正，成乾。故曰「武人為於大君」。誌剛也。
《象》曰：眇而視，不足以有明也。跛而履，不足以與行也。
侯果曰：六三，兌也。互有離巽，離為目，巽為股。體俱非正，雖能視，眇目者也。雖能履，跛足者也。故曰「眇能視，不足以有明。跛能履，不足以與行」。是其義也。
咥人之兇，位不當也。
案：六三為履卦之主。體說應乾，下柔上剛，尊卑合道，是以「履虎尾，不咥人，通」。今於當爻以陰處陽，履非其位，互體離兌，水火相刑。故獨唯三被咥，兇矣。
武人為於大君，誌剛也。
案：以陰居陽，武人者也。三互離爻，離為響明，為於大君，南面之象，與乾上應，故曰「誌剛」。
九四：履虎尾，愬愬，終吉。
虞翻曰：體與下絕，四多懼，故「愬愬」。變體坎，得位，承五應初，故終吉。《象》曰：「誌行也」。
《象》曰：愬愬終吉，誌行也。
侯果曰：愬愬，恐懼也。履乎兌主，履虎尾也。逼近至尊，故恐懼。以其恐懼，故「終吉」也。執乎樞密，故「誌行」也。
九五：夬履，貞厲。
虞翻曰：謂三。上已變，體夬象，故「夬履」。四變五，在坎中也，為上所乘，故「貞厲」。《象》曰：「位正當也」。
《象》曰：夬履貞厲，位正當也。
幹寶曰：夬，決也。居中履正，為履貴主。萬方所履，一決於前，恐決失正，恒懼危厲。故曰「夬履貞厲，位正當也」。
上九：視履考詳，其旋元吉。
虞翻曰：應在三，三先視上，故上亦視三。故曰「視履考詳」矣。考，稽。詳，善也。乾為積善，故「考詳」。三上易位，故「其旋元吉」。《象》曰：「大有慶也」。
《象》曰：元吉在上，大有慶也。
盧氏曰：王者履禮於上，則萬方有慶於下。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷四·泰卦集解", "底本：维基文库《周易集解（李鼎祚）/卷四》，本条录入页面现存正文行112—225，覆盖泰卦经文、彖传、象传、六爻及所载诸家注文。此条是卷四的首卦独立分段，不代表卷四其余否、同人、大有、谦、豫已经导入。页面原有字形、异文及注文署名照录；未完成与影印本逐字校勘。译文尚未完成。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷四", """《序卦》曰：履而泰，然後安，故受之以泰。泰者，通也。
崔覲曰：以禮導之，必通。通然後安，所謂君子以辯上下，定民誌，通而安也。

(乾下坤上)。泰：小往大來，吉、亨。
虞翻曰：陽息坤反，否也。坤陰詘外，為小往。乾陽信內，稱大來。天地交，萬物通，故「吉、亨」。

《彖》曰：泰，小往大來，吉、亨。
蜀才曰：此本坤卦。小，謂陰也。大，謂陽也。天氣下，地氣上，陰陽交，萬物通，故「吉、亨」。
則是天地交而萬物通也；
何妥曰：此陰在道泰也。夫泰之為道，本以通生萬物。若天氣上騰，地氣下降，各自閉塞，不能相交，則萬物無由得生。明萬物生，由天地交也。
上下交而其誌同也；
何妥曰：此明人事泰也。上之與下，猶君之與臣，君臣相交感，乃可以濟養民也。天地以氣通，君臣以誌同也。
內陽而外陰，內健而外順；
何妥曰：此明天道也。陰陽之名，就爻為語。健順之稱，指卦為言。順而陰居外，故曰「小往」。健而陽在內，故曰「大來」。
內君子而外小人，
崔覲曰：此明人事也。陽為君子，在內，健於行事。陰為小人，在外，順以聽命。
君子道長，小人道消也。
《九家易》曰：謂陽息而升，陰消而降也。陽稱息者，長也。起復成巽，萬物盛長也。陰言消者，起姤終乾。萬物成熟。成熟則給用，給用則分散。故陰用特言消也。

《象》曰：天地交泰，
荀爽曰：坤氣上升，以成天道。乾氣下降，以成地道。天地二氣，若時不交，則為閉塞。今既相交，乃通泰。
後以財成天地之道，
虞翻曰：後，君也。陰升乾位。坤，女主，故稱後。坤富，稱財。守位以人，聚人以財，故曰「成天地之道」。
輔相天地之宜，以左右民。
虞翻曰：相，贊。左右，助之。震為左，兌為右，坤為民，謂以陰輔陽。《詩》曰：宜民宜人，受祿於天。
鄭玄曰：財，節也。輔，相。左右，助也。以者，取其順陰陽之節，為出內之政。春崇寬仁，夏以長養，秋教收斂，冬敕蓋藏，皆可以成物助民也。

初九：拔茅茹，以其匯，征吉。
王弼曰：茅之為物，拔其根而相牽引也。茹，相牽引之貌也。三陽同誌，俱誌在外。初為類首，已舉，則從若茅茹也。上順而應，不為違距，進皆得誌。故「以其類，征吉」也。
《象》曰：拔茅征吉，誌在外也。
虞翻曰：否泰反其類，否巽為茅。茹，茅根。艮為手。匯，類也。初應四，故拔茅茹以匯。震為征，得位應四，征吉。外，謂四也。

九二：包荒，
翟玄曰：荒，虛也。二五相應，五虛無陽，二上包之。
用馮河，不遐遺。
荀爽曰：河出於乾，行於地中。陽性欲升，陰性欲承。馮河而上，不用舟航。自地升天，道雖遼遠，三體俱上，不能止之，故曰「不遐遺」。
朋亡，得尚於中行。
荀爽曰：中，謂五。坤為朋，朋亡而下，則二上居五，而行中和矣。
《象》曰：包荒，得尚於中行，以光大也。
虞翻曰：在中稱包。荒，大川也。馮河，涉河。遐，遠；遺，亡也。失位變得正，體坎。坎為大川，為河。震為足。故「用馮河」。乾為遠，故「不遐遺」。兌為朋，坤虛無君，欲使二上，故「朋亡」。二與五易位，故得上於中行。震為行，故「光大也」。

九三：無平不陂，無往不復。
虞翻曰：陂，傾。謂否上也。平，謂三。天地分，故平。天成地平，謂危者使平，易者使傾。往，謂消外。復，謂息內。從三至上，體復。終日乾乾，反復道。故「無平不陂，無生不復」。
艱貞無咎。勿恤其孚，於食有福。
虞翻曰：艱，險。貞，正。恤，憂。孚，信也。二之五，得正，在坎中，故「艱貞」。坎為憂。故「勿恤」。陽在五孚，險坎為孚，故有孚。體噬嗑食也。二上之五據四，則三乘二。故「於食有福」也。
《象》曰：無平不陂，天地際也。
宋衷曰：位在乾極，應在坤極，天地之際也。地平極則險陂，天行極則還復，故曰「無平不陂，無往不復」也。

六四：翩翩，不富以其鄰，
虞翻曰：二五變時，四體離飛，故「翩翩」。坤虛無陽，故「不富」。兌西震東，故稱「其鄰」。三陰乘陽，不得之應。《象》曰：「皆失實也。」
不戒以孚。
虞翻曰：謂坤。邑人不戒，故使二升五，信來孚邑，故「不戒以孚」。二上體坎，中正。《象》曰：「中心願也。」與比邑人不誡同義也。
《象》曰：翩翩不富，皆失實也。
宋衷曰：四互體震，翩翩之象也。陰虛陽實，坤今居上，故言「失實也」。
不戒以孚，中心願也。
《九家易》曰：乾升坤降，各得其正。陰得承陽，皆陽心之所願也。

六五：帝乙歸妹，以祉元吉。
《九家易》曰：五者，帝位，震象稱乙，是為帝乙。六五以陰處尊位，帝者之姊妹。五在震後，明其為妹也。五應於二，當下嫁二。婦人謂嫁曰歸。故言「帝乙歸妹」。謂下居二，以中和相承，故「元吉」也。
虞翻曰：震為帝，坤為乙。帝乙，紂父。歸嫁也。震為兄，兌妹，故嫁妹。祉，福也。謂五變體離，離為大腹，則妹嫁而孕，得位正中，故「以祉元吉」也。
《象》曰：以祉元吉，中以行願也。
《九家易》曰：五下於二，而得中正，故言「中以行願」也。

上六：城復於隍，
虞翻曰：否艮為城，故稱城坤為積土。隍，城下溝。無水稱隍，有水稱池。今泰反否，乾壞為土，艮城不見，而體復象，故「城復於隍」也。
勿用師，自邑告命，貞吝。
虞翻曰：謂二，動時體師。陰皆乘陽，行不順，故「勿用師」。坤為自邑，震為言，兌為口，否巽為命。今逆陵陽，故「自邑告命」。命逆不順，陰道先迷，失實遠應，故「貞吝」。
《象》曰：城復於隍，其命亂也。
《九家易》曰：乾當來上，不可用師而拒之也。自邑者，謂從坤性而降也。告命者，謂下為巽，宣布君之命令也。三陰自相告語，俱下服順承乾也。城復於隍，國政崩也。坤為亂，否巽為命，交在泰上，故「其命亂也」。""",
    """**ChatGPT 翻译（第一批：序卦、卦辞与彖传）**

《序卦》说：履卦之后便能通泰，通泰之后才能安定，所以接着是泰卦。泰，就是通达。

崔觐说：用礼来引导，就必然能够通达；通达之后才能安定。这就是君子辨明上下、安定民心，使上下通达而安定的意思。

泰卦下卦为乾，上卦为坤。卦辞说：小的离去，大的到来，吉祥、亨通。

虞翻说：阳气增长，坤卦反转为否卦。坤的阴爻退居外部，所以称“小往”；乾的阳爻进入内部，所以称“大来”。天地交感，万物通达，因此吉祥、亨通。

《彖传》说：泰卦，小的离去，大的到来，吉祥、亨通。

蜀才说：此卦本于坤卦。“小”指阴，“大”指阳。天气下降，地气上升，阴阳交感，万物通达，所以吉祥、亨通。

“这正是天地交感、万物通达。”何妥说：这是说明天道的通泰。泰的根本作用，在于通过通达使万物生长。如果天气向上升腾、地气向下降落，各自闭塞而不能交合，万物便没有生长的条件。这说明万物的生长来自天地交感。

“上下交而其志同。”何妥说：这是说明人事的通泰。上位者与下位者的关系，就像君主与臣下；君臣彼此交感，才能共同治理并养育百姓。天地凭借气相通，君臣凭借志向相同而相通。

“内阳而外阴，内健而外顺。”何妥说：这是说明天道。阴阳是从爻的性质来说的，刚健与柔顺则是从卦的性质来说的。柔顺的阴爻处在外部，所以说“小往”；刚健的阳爻处在内部，所以说“大来”。

“内君子而外小人。”崔觐说：这是说明人事。阳代表君子，处在内部，刚健地行事；阴代表小人，处在外部，顺从地听命。

“君子道长，小人道消。”《九家易》说：这是说阳气增长上升，阴气消退下降。阳称为“息”，因为它增长；从复卦发展到巽卦，万物旺盛生长。阴称为“消”，因为它从姤卦开始，终至乾卦，象征万物成熟。成熟之后便可供使用，供使用之后又会分散，所以阴特别用“消”来表达。

**第一批范围说明：**第一批仅覆盖本条开头至《彖传》及其注文。

**ChatGPT 翻译（第二批：象传与六爻）**

**《象传》**

《象传》说：天地交感，这就是泰卦。荀爽说：坤气上升而形成天道，乾气下降而形成地道。天地二气如果不交合，就会闭塞；如今已经交合，所以通泰。

“后以财成天地之道。”虞翻说：“后”指君主。阴爻升到乾位；坤为女主，所以称“后”。坤有富厚之象，所以称“财”。以人守位，以财聚人，因此说成就天地之道。

“辅相天地之宜，以左右民。”虞翻说：“相”是辅佐，“左右”是帮助；震象征左，兑象征右，坤象征百姓，意思是以阴辅佐阳，并引用《诗经》说明顺宜于民、顺宜于人的意思。郑玄说：“财”是节制；“辅相”是辅助，“左右”是帮助。这里是取法阴阳运行的节度来安排政令：春季崇尚宽仁，夏季使万物生长，秋季教人收敛，冬季命人收藏，以此成就万物、帮助百姓。

**初九**

“拔茅茹，以其汇，征吉。”王弼说：茅草被拔起时，根部会牵连同类。“茹”就是彼此牵连的样子。三条阳爻志向相同，都希望向外进取；初爻处在同类的开端，一旦被举起，其他阳爻便会像茅草根茎一样跟随。上方柔顺而与之相应，不会违逆阻拦，所以同类一同前进吉祥。

《象传》说：拔起茅草而前进吉祥，是因为志向在外。虞翻说：否、泰两卦反转其类；否卦的巽象代表茅草，“茹”是茅根，艮代表手，“汇”是同类。初九与六四相应，所以说拔茅连根而起。震代表前进，初九得位且与六四相应，所以前进吉祥；“外”指六四。

**九二**

“包荒。”翟玄说：“荒”是空虚。九二与六五相应，六五空虚而没有阳爻，九二向上包容它。

“用冯河，不遐遗。”荀爽说：河出于乾而运行于地中。阳的性质是向上升，阴的性质是承接。徒步涉河而上，不用舟船；从地上升到天，路途虽然遥远，三条阳爻一同向上而不能被阻止，所以说“不遗弃遥远之处”。

“朋亡，得尚于中行。”荀爽说：“中”指六五。坤代表朋党；朋党消散而九二上升，便能居于六五而行中和之道。

《象传》说：包容荒远，得以行于中道，是因为光明广大。虞翻说：居中称“包”；“荒”是大川，“冯河”是涉河；“遐”是遥远，“遗”是遗漏。九二失位，变动后得正而形成坎；坎代表大川、河流，震代表足，所以说涉河；乾代表远，所以说不遗漏远方。兑代表朋，坤虚而无君，因而希望九二上升，所以说朋亡。九二与六五交换位置，因而得以居中；震代表行动，所以说光明广大。

**九三**

“无平不陂，无往不复。”虞翻说：“陂”是倾斜，指否卦上爻；“平”指九三。天地分判，所以称平。平坦到了极点便会倾斜，前往到了极点便会返回。“往”指阴气向外消退，“复”指阳气在内部增长。从九三到上六，卦体包含复象；乾道终日运行而不懈怠，循环往复，所以说没有永远平坦而不倾斜、没有一去不返的事物。原注另有“无生不复”一语，与经文“无往不复”字面不同，属于注释引文差异，不据此改动经文。

“艰贞无咎。勿恤其孚，于食有福。”虞翻说：“艰”是险难，“贞”是正，“恤”是忧虑，“孚”是诚信。九二上升到六五，得到正位并处于坎中，所以说艰难而守正。坎代表忧虑，因此说不必忧虑。阳爻居五而有诚信，坎也有诚信之象，所以说有孚。卦体含有噬嗑的饮食之象；九二上升到六五而据有六四的位置，九三便乘承九二，因此说饮食有福。

《象传》说：没有永远平坦而不倾斜，是因为处在天地交接的边界。宋衷说：九三处在乾的极位，与坤的极位相应，是天地交接之处。地势平坦到了极点便转为险陂，天道运行到了极点便返回，所以说没有永远平坦而不倾斜、没有一去不返的事物。

**六四**

“翩翩，不富以其邻。”虞翻说：九二、六五变动时，六四的卦体呈现离飞之象，所以称“翩翩”；坤虚而无阳，所以说“不富”；兑在西、震在东，所以称“邻”。三条阴爻乘在阳爻之上，不能前往相应的位置。《象传》说它们都失去了实位。

“不戒以孚。”虞翻说：这里说的是坤。邑中之人不必戒备，于是使九二上升至六五，诚信来到并使邑人信服，所以说“不戒以孚”。九二上升后形成坎体，处于中正位置。《象传》说“中心愿也”，与比卦中邑人不加戒备的意思相同。

《象传》说：翩翩而不富，是因为都失去了实位。宋衷说：六四的互体为震，震有飞动之象；阴虚而阳实，如今坤居于上方，所以说失实。

“不戒以孚”，是因为内心愿意如此。《九家易》说：乾升坤降，各自得到正位。阴得以承接阳，这是阳心中所愿。

**六五**

“帝乙归妹，以祉元吉。”《九家易》说：六五处于帝位，震象称乙，所以称帝乙。六五以阴爻居于尊位，象征帝王的姊妹；六五处在震之后，说明她是妹妹。六五与九二相应，应当下嫁九二。“归”是女子出嫁的说法。六五下居九二，以中和相承，所以大吉。

虞翻说：震代表帝，坤代表乙。帝乙是商纣王的父亲。“归”就是出嫁；震代表兄长，兑代表妹妹，所以说嫁妹。“祉”是福。六五变化后卦体为离，离有大腹之象，因此他以妹妹出嫁而怀孕解释此句；又因得位居中，所以说得到福祉、大吉。

《象传》说：因此得到福祉而大吉，是因为居中并实现了愿望。《九家易》说：六五下至九二而得到中正，所以说居中而实现愿望。

**上六**

“城复于隍。”虞翻说：否卦的艮象代表城，坤象代表堆积的土。隍是城墙下的沟；没有水称隍，有水称池。泰卦反转为否卦，乾坏而成为土，艮的城象消失，卦体又呈现复象，所以说城墙坍塌回到护城沟中。

“勿用师，自邑告命，贞吝。”虞翻说：这里说的是九二；九二发动时卦体形成师卦。阴爻都乘在阳爻之上，行动不顺，所以说不要用兵。坤代表自己的城邑，震代表言语，兑代表口，否卦中的巽代表命令。如今阴爻逆势凌驾于阳爻之上，所以说从自己的城邑发布命令。命令违逆而不顺，阴道先迷失，失去实际依据又远离相应之位，所以即使守正也会有困难。

《象传》说：城墙坍塌落入护城沟中，是因为政令混乱。《九家易》说：乾本应上升，不可动用军队抵拒它。所谓“自邑”，是指顺着坤的性质下降；所谓“告命”，是指下方形成巽象，宣布君主的命令。三条阴爻彼此告语，一同下降，顺从地承接乾阳。城墙落入护城沟，象征国政崩坏。坤代表混乱，否卦的巽代表命令；二者交错而处在泰卦之上，所以说政令混乱。

**本条翻译状态：**以上第二批补齐《象传》、六爻及其注文的 ChatGPT 工作译文。与第一批合并后，覆盖本条目前录入的全部正文和注文。卦变、互体等术语依注家原说翻译；疑难处不作唯一解释。此译文尚未完成与四库影印页逐字校勘。本次只翻译本条开头至《彖传》及其注文；《象传》、六爻和后续注文仍待逐段翻译。原文及注家署名保持不变。""")"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷四·否卦集解", "底本：维基文库《周易集解（李鼎祚）/卷四》，录入页面现存正文行227—326，覆盖否卦经文、彖传、象传、六爻及页面所载诸家注文。本条为卷四第二卦分段；泰卦另列，后续同人、大有、谦、豫仍待导入。原页面异体字、注文署名及疑似讹字按底本保留，尚未与影印本逐字校勘。译文尚未完成。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷四", """《序卦》曰：物不可以終通，故受之以否。
崔覲曰：物極則反，故不終通而否矣，所謂「城復於隍」者也。

(坤下乾上)。否：否之匪人，不利君子貞，大往小來。
虞翻曰：陰消乾，又反泰也。謂三。比坤滅乾，以臣弒其君子弒其父，故曰「匪人」。陰來滅陽，君子道消，故「不利君子貞」。陰信陽詘，故「大往小來」。則是天地不交而萬物不通，與比三同義也。

《彖》曰：否匪人，不利君子貞。
崔覲曰：否，不通也。於不通之時，小人道長，故雲「匪人」。君子道消，故「不利君子貞」也。
大往小來，
蜀才曰：此本乾卦。大往，陽往而消。小來，陰來而息也。
則是天地不交而萬物不通也。
何妥曰：此明天道否也。
上下不交而天下無邦也。
何妥曰：此明人事否也。泰中言誌同，否中雲無邦者，言人誌不同，必致離散，而亂邦國。崔覲曰：君臣乖阻，取亂之道，故言「無邦」。
內陰而外陽，內柔而外剛，
崔覲曰：陰、柔，謂坤。陽、剛，謂乾也。
內小人而外君子。小人道長，君子道消也。
崔覲曰：君子在野，小人在位之義也。

《象》曰：天地不交，否。
宋衷曰：天地不交，猶君臣不接。天氣上升，而不下降；地氣沈下，又不上升。二氣特隔，故雲「否」也。
君子以儉德辟難，不可榮以祿。
虞翻曰：君子謂乾，坤為營，乾為祿，難謂坤為弒君故以「儉德辟難」。巽為入伏，乾為遠，艮為山，體遁象，謂辟難遠遁入山，故「不可營以祿」。營，或作榮。儉，或作險。
孔潁達曰：言君子於此否時，以節儉為德，辟其危難，不可榮華其身，以居祿位。若據諸侯公卿而言，是辟時群小之難，不可重受官爵也。若據王者言之，謂節儉為德，辟陰陽厄運之難，不可自重榮貴而驕逸也。

初六：拔茅茹以其匯，貞吉，亨。
荀爽曰：拔茅茹，取其相連，匯者，類也。合體同包，調坤三爻同類相連，欲在下也。貞者，正也。謂正居其所則「吉」也。
《象》曰：拔茅貞吉，誌在君也。
《九家易》曰：陰誌在下，欲承君也。
案：初六，巽爻。巽為草木，陽爻為木，陰爻為草。初六陰爻，草茅之象也。

六二：包承，小人吉，大人否，亨。
荀爽曰：二與四同功，為四所包，故曰「包承」也。小人，二也。謂一爻獨居，間象相承，得系於陽，故「吉」也。大人，謂五。乾坤分體，天地否隔，故曰「大人否」也。二五相應，否義得通，故曰「否，亨」矣。
《象》曰：大人否享，不亂群也。
虞翻曰：否，不也」物三稱群，謂坤三陰亂弒君，大人不從，故「不亂群」也。

六三：包羞。《象》曰：包羞，位不當也。
荀爽曰：卦性為否，其義否隔。今以不正，與陽相承，為四所包，違義失正，而可羞者，以「位不當」故也。

九四：有命，無咎，疇離祉。
《九家易》曰：巽為命。謂受五之命，以據三陰，故「無咎」。無命而據，則有咎也。疇者，類也。謂四應初據三，與二同功，故陰類皆離祉也。離，附。祉，福也。陰皆附之，故曰有福。謂下三陰離，受五四之福也。
《象》曰：有命無咎，誌行也。
荀爽曰：謂誌行於群陰也。

九五：休否，大人吉。
《九家易》曰：否者消卦，陰欲消陽。故五處和居正，以否絕之。乾坤異體，升降殊隔，卑不犯尊，故「大人吉」也。
其亡其亡，
荀爽曰：陰欲消陽，由四及五，故曰「其亡其亡」。謂坤性順從，不能消乾使亡。
系於包桑。
荀爽曰：包者，乾坤相包也。桑者，上玄下黃，以象乾坤也。乾職在上，坤體在下，雖欲消乾，系其本體，不能亡也。
京房曰：桑有衣食人之功，聖人亦有天覆地載之德，故以喻。
陸績曰：包，本也。言其堅固不亡，如以巽繩系也。
案：其亡其亡，近死之嗟也。其與幾同。幾者，近也。九五居否之時，下包六二。二互坤艮，艮山坤地，地上即田也。五互巽木，田上有木，莫過於桑。如曰「其亡其亡，系於苞桑」。言五二包系，根深蒂固，若山之堅，如地之厚者也。雖遭危亂，物莫能害矣。
鄭玄曰：猶紂囚文王於羑裏之獄，四臣獻珍異之物，而終免於難，系於包桑之謂。
《象》曰：大人之吉，位正當也。
崔覲曰：得位居中也。

上九：傾否，先否後喜。
侯果曰：傾為覆也。否窮則傾矣。傾猶否，故「先否」也。傾畢則通，故「後喜」也。
《象》曰：否終則傾，何可長也。
虞翻曰：否終必傾，盈不可久，故「先否」。下反於初，成益，體震，民說無疆，故「後喜」。以陰剝陽，故不可久也。

**ChatGPT 翻译（系统续译：序卦、卦辞、彖传与象传）**

《序卦》说：事物不可能永远保持通达，所以泰卦之后接着是否卦。

崔觐说：事物发展到极点就会转向反面，因此不可能永远通达，最终会闭塞。这正如《系辞》所说的“城墙倒塌，土又回落到护城河中”。

（下卦为坤，上卦为乾。）否卦：闭塞之时，人与正道相违；不利于君子坚守正道；阳刚的大者离去，阴柔的小者到来。

虞翻说：阴爻消退乾卦的阳爻，卦象又由泰转变而来。这里所说的是第三爻。比卦的坤体消灭乾体，象征臣下弑君、子弑父，所以称为“匪人”。阴气到来而消灭阳气，君子的道路衰退，所以说“不利君子贞”。阴气增长，阳气屈退，所以说“大往小来”。这也就是天地之气不相交、万物不能通行的情形，与比卦六三的意义相同。

《彖传》说：否卦所谓“匪人”，是不利于君子坚守正道。

崔觐说：否就是不通。在不通的时代，小人的道路增长，所以说“匪人”；君子的道路衰退，所以说“不利君子贞”。

阳刚的大者离去，阴柔的小者到来。

蜀才说：此卦原本是乾卦。“大往”是阳爻离去而衰退；“小来”是阴爻到来而增长。

这就是说，天地之气不相交，万物因此不能通行。

何妥说：这里说明的是天道的闭塞。

上下不相交接，天下便没有可以维系的邦国秩序。

何妥说：这里说明的是人事上的闭塞。泰卦中说意志相同，否卦中说天下无邦，是说人们的意愿彼此不同，必然导致离散，进而使邦国陷入混乱。崔觐说：君臣彼此乖离阻隔，这是招致祸乱的道路，所以说“无邦”。

阴柔居内，阳刚居外；内在柔弱，外在刚强。

崔觐说：这里的阴、柔指坤卦，阳、刚指乾卦。

内在的是小人，外在的是君子；小人的道路增长，君子的道路衰退。

崔觐说：这就是君子退居民间、小人占据职位的意思。

《象传》说：天地之气不相交，这就是“否”的象征。

宋衷说：天地不相交，就如同君臣不相接。天气上升却不下降，地气沉降却不上升，阴阳二气彼此隔绝，所以称为“否”。

君子由此领悟，应当以节俭约束自身德行，避开祸患，不可贪恋荣华禄位。

虞翻说：君子指乾卦，坤卦为“营”，乾卦为禄；所谓祸难，指坤卦所象征的弑君之事，因此要以节俭的德行避开祸难。巽象征进入、潜伏，乾象征远方，艮象征山，卦体呈现遁卦的形象，意思是避开祸难、远行隐遁、进入山中，所以说不可用禄位来招引。原文“营”有版本写作“荣”，“俭”有版本写作“险”，这里保留异文，不将其混同。

孔颖达说：君子处在否塞的时代，应当以节俭作为德行，避开危险和祸难，不可使自身因荣华而居于禄位。如果从诸侯、公卿的角度解释，就是避开当时群小造成的祸难，不要再接受显要官爵。如果从君王的角度解释，就是以节俭为德，避开阴阳失序所造成的厄运，不可因自身尊贵显赫而骄纵安逸。

**ChatGPT 翻译（系统续译：六爻及注文）**

初六：拔起茅草，根茎彼此牵连，依照同类聚合；坚守正道则吉祥，事情可以亨通。

荀爽说：“拔茅茹”是取茅草根茎相连之象；“汇”就是同类。三爻同属坤体，彼此同类相连，意在共同处于下方。“贞”就是正，意思是端正地安守自己的位置，便能得到吉祥。

《象传》说：拔起茅草而坚守正道能够吉祥，是因为其志向在于君主。

《九家易》说：阴爻的志向在下方，是想要承顺君主。

编者按：初六属于巽爻。巽象征草木；阳爻象征木，阴爻象征草。初六是阴爻，因此取草茅之象。

六二：包容并承顺。小人这样做可以得到吉祥；大人则处于否塞之中，但最终能够亨通。

荀爽说：六二与九四功用相同，被九四包容，所以称为“包承”。“小人”指六二。它独自处在一爻的位置上，间隔的卦象彼此承接，得以依附阳爻，所以说“吉”。“大人”指九五。乾、坤分处上下，天地闭塞隔绝，所以说“大人否”。六二与九五相应，否塞之义因此有了贯通的可能，所以说“否，亨”。

《象传》说：大人处于否塞之中而能亨通，是因为不与自己的群类一同陷入混乱。

虞翻说：“否”在这里训为“不”。“物三称群”，指坤卦三阴爻形成的群体。大人不随从阴爻所象征的乱臣弑君之事，所以说“不乱群”。

校勘提示：底本文字作“否享”，与本爻经文“否，亨”及上下文的解释存在差异；此处按所录底本文字保留，并以注文所表达的“否而能通”解释，不擅自改写原文。

六三：包藏羞耻。

《象传》说：包藏羞耻，是因为所处的位置不恰当。

荀爽说：此卦的性质是否塞，意义在于阻隔。如今六三位置不正，又与阳爻相承，被九四包容；这违背了正当的义理，也失去了正位，因此成为可耻之事，原因就在于“位不当”。

九四：承受天命，没有灾咎；同类之人也会依附而获得福祉。

《九家易》说：巽象征命令。九四接受九五的命令，因而能够统摄下方三阴爻，所以没有灾咎；如果没有命令却擅自居于其上，就会招致灾咎。“疇”指同类。九四与初六相应，又据有六三，与六二功用相同，所以阴爻这一类都能依附并获得福祉。“离”是依附，“祉”是福。阴爻都来依附，所以说有福；这是说下方三阴爻依附九四、九五而承受其福。

《象传》说：有了命令便没有灾咎，是因为自己的志向得以施行。

荀爽说：这是说其志向能够在众阴爻之中施行。

九五：使否塞停止，大人吉祥。

《九家易》说：否卦是阴气消退阳气的卦，阴爻想要消灭阳爻。因此九五处在和顺的位置并居于正位，用来阻止否塞继续发展。乾、坤分处不同位置，升降各不相同，卑下者不侵犯尊上者，所以说“大人吉”。

即使面临灭亡，也要反复警惕自己：“将要灭亡了！将要灭亡了！”

荀爽说：阴气想要消灭阳气，其势从九四发展到九五，所以说“其亡其亡”。但坤的性质是顺从，不能真正消灭乾而使其灭亡。

把自身系结在坚固的桑树根上。

荀爽说：“包”指乾、坤彼此包合；“桑”树上部呈玄色、下部呈黄色，用来象征乾、坤。乾的职分在上，坤的形体在下。阴气虽然想要消灭乾，却仍受其根本形体牵系，不能使乾灭亡。

京房说：桑树具有供养人的衣食之功，圣人也有如天覆盖、如地承载的德行，因此用桑树作比喻。

陆绩说：“包”就是根本。意思是其根基坚固而不会灭亡，就像用巽所象征的绳索系住一样。

编者按：“其亡其亡”是接近死亡时发出的警惕感叹。“其”与“几”意义相近，“几”有接近之义。九五处于否塞之时，下方包容六二。六二所处的互体包含坤、艮：艮为山，坤为地，地上就是田地。九五的互体为巽，巽为木；田地上有树木，没有比桑树更合适的象征。因此可以说“其亡其亡，系于苞桑”。这是说九五与六二彼此包合系连，根基深固，如山之坚、如地之厚。即使遭遇危乱，也没有外物能够轻易伤害它。

郑玄说：这就像商纣王把周文王囚禁在羑里，文王的四位臣属献上珍贵异物，最终使文王免于灾难；这就是“系于包桑”所表达的意思。

《象传》说：大人能够获得吉祥，是因为所处的位置正当。

崔觐说：这是因为九五得位而居中。

上九：倾覆否塞；开始时是否塞，后来则转为喜悦。

侯果说：“倾”就是倾覆。否塞发展到尽头就会倾覆。倾覆之时仍处于否塞，所以说“先否”；倾覆结束以后便会通达，所以说“后喜”。

《象传》说：否塞到了终点就会倾覆，怎么可能长久呢？

虞翻说：否塞发展到终点必然倾覆，事物盈满不可能长久，所以说“先否”。上九向下返回初爻的位置，形成益卦，卦体呈现震象，百姓喜悦而没有穷尽，所以说“后喜”。这是阴爻剥落阳爻的局面，因此不可能长久。

**本条翻译状态：**以上 ChatGPT 工作译文覆盖本条目前录入的《序卦》、否卦卦辞、《彖传》《象传》、初六至上九及其所录注文。底本中的“否，不也」”“否享”等可疑文字及“营/荣”“俭/险”等异文均未擅自静默改字；本译文是对当前录入文本的解释性翻译，尚未与四库影印页逐字校勘，不代表已完成卷四其余卦的翻译。
"""),


    Entry("历代注疏", "唐李鼎祚《周易集解》卷四·同人卦集解", "底本：维基文库《周易集解（李鼎祚）/卷四》，录入页面现存正文行328—420，覆盖同人卦经文、彖传、象传、六爻及页面所载注文。页面字形、异文和注文署名按底本照录，尚未与影印本逐字校勘。译文尚未完成。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷四", """《序卦》曰：物不可以終否，故受之以同人。
崔覲曰：否終則傾，故同於人。通而利涉矣。

(離下乾上)。同人：同人於野，亨。
鄭玄曰：乾為天，離為火。卦體有巽，巽為風。天在上，火炎上而從之，是其性同於天也。火得風，然後炎上益熾，是猶人君在上施政教，使天下之人和同而事之。以是為人和同者，君之所為也。故謂之「同人」。風行無所不遍，遍則會通之德大行，故曰「同人於野，亨」。
利涉大川，利君子貞。
崔覲曰：以離文明，而合乾健。九五中正，同人於二，為能通天下之誌，故能「利涉大川，利君子之貞」。

《彖》曰：同人，
《九家易》曰：謂乾舍於離，同而為日。天日同明，以照於下。君子則之，上下同心，故曰「同人」。
柔得位得中，而應乎乾，曰同人。
蜀才曰：此本夬卦。九二升上，上六降二，則「柔得位得中，而應乎乾」。下奉上之象，義同於人，故曰「同人」。
同人曰：同人於野，亨，利涉大川，乾行也。
虞翻曰：旁通師卦。巽為同，乾為野。師震為人。二得中應乾。故「同人於野，亨」。此孔子所以明嫌表微。師震為夫，巽為婦，所謂二人同心。故不稱君臣、父子、兄弟、朋友，而故言人耳。乾四上失位，變而體坎，故曰「利涉大川，乾行也」。
侯果曰：九二升上，上為郊野，是「同人於野」而得通者，由乾爻上行耳。故特曰「乾行也」。
文明以健，中正而應，君子正也。
何妥曰：離為文明，乾為剛健。健非尚武，乃以文明。應不以邪，乃以中正。故曰「利君子貞」也。
唯君子為能通天下之誌。
虞翻曰：唯，獨也。四變成坎。坎為通、為誌。故「能通天下之誌」。謂五以類族辯物，聖人作而萬物睹。
崔覲曰：君子謂九五。能舍已同人，以通天下之誌。若九三、九四，以其人臣，則不當矣。故爻辭不言同人也。

《象》曰：天與火，同人。
荀爽曰：乾舍於離，相與同居，故曰「同人」也。
君子以類族辯物。
虞翻曰：君子謂乾。師坤為類，乾為族。辯，別也。乾，陽物。坤，陰物。體姤，天地相遇，品物鹹章，以乾照坤。故「以類族辯物」。謂方以類聚，物以群分。孔子曰：君子和而不同，故於同人象見，以「類族辯物」也。

初九：同人於門，無咎。
虞翻曰：乾為門，謂同於四，四變應初，故「無咎」也。
《象》曰：出門同人，又誰咎也。
崔覲曰：剛而無應，比二以柔，近同於人，出門之象，「又誰咎矣」。
案：初九，震爻。帝出乎震。震為大塗，又為入門出門之象也。

六二：同人於宗，吝。
荀爽曰：宗者，眾也。三據二陰，二與四同功，五相應，初相近，上下眾陽，皆欲與二為同，故曰「同人於宗」也。陰道貞靜，從一而終，今宗同之，故「吝」也。
《象》曰：同人於宗，吝道也。
侯果曰：宗，謂五也。二為同人之主，和同者之所仰也。有應在五，唯同於五，過五則否，不能大同於人，則為主之德吝狹矣。所同雖吝，亦妻臣之道也。

九三：伏戎於莽，升其高陵，三歲不興。
虞翻曰：巽為伏，震為草莽，離為戎。謂四變時三在坎中，隱伏自藏，故「伏戎於莽」也。巽為高，師震為陵，以巽股升其高陵。爻在三。乾為歲。興，起也。動不失位，故「三歲不興」也。
《象》曰：伏戎於莽，敵剛也。三歲不興，安行也。
崔覲曰：與二相比，欲同人焉。盜憎其主，而忌於五，所以隱兵於野，將以襲之，故曰「伏戎於莽」。五既居上，故曰「升其高陵」。一爻為一年，自三至五，頻遇剛敵，故「三歲不興，安可行也」。
案：三互離巽，巽為草木，離為戈兵，伏戎於莽之象也。

九四：乘其庸，弗克攻，吉。
虞翻曰：巽為庸，四在巽上，故「乘其庸」。變而承五。體訟，乾剛在上。故「弗克攻」，則「吉」也。
《象》曰：乘其庸，義弗克也。其吉，則困而反則也。
王弼曰：處上攻下，力能乘庸者也。履非其位，與三爭二。二自應五，三非犯已，攻三求二，尤為效之。違義傷禮，眾所不與。勢雖乘墉，義終弗克，而得吉者，以困而反正則也。

九五：同人先號咷而後笑，大師克相遇。
虞翻曰：應在二。巽為號咷，乾為先，故「先號咷」。師震在下，故「後笑」。震為後笑也。乾為大，同人反師，故「大師」。二至五，體姤遇也，故「相遇」。
《象》曰：同人之先，以中直也。大師相遇，言相克也。
侯果曰：乾德中直，不私於物，欲天下大同，方始同二矣。三四失義，而近據之，未獲同心，故「先號咷」也。時須同好，寇阻其途，以言相克，然後始相遇，故笑也。
《九家易》曰：乾為言。

上九：同人於郊，無悔。
虞翻曰：乾為郊。失位無應，與乾上九同義，當有悔。同心之家。故「無悔」。
《象》曰：同人於郊，誌未得也。
侯果曰：獨處於外，同人於郊也。不與內爭，無悔吝也。同人之時，唯同於郊，「誌未得也」。

**ChatGPT 翻译（系统续译：序卦、卦辞、彖传与象传）**

《序卦》说：事物不可能永远处于闭塞状态，所以否卦之后接着是同人卦。

崔觐说：否塞发展到终点就会倾覆，因此人们重新归于和同，交通通达，也就有利于涉越险阻。

（下卦为离，上卦为乾。）同人卦：与人会合同心于广阔的原野，亨通；有利于渡过大河；有利于君子坚守正道。

郑玄说：乾象征天，离象征火。卦体中还包含巽，巽象征风。天在上方，火焰向上燃烧并追随天势，这种性质与天相同。火得到风的助力，向上燃烧就会更加旺盛。这好比君主居于上位施行政令教化，使天下之人和睦同心并为之效力。使人们和同，是君主应当做的事，所以称为“同人”。风吹行于各处，没有地方不能到达；风行遍布，通达会合的德行便能广泛施行，因此说“同人于野，亨”。

崔觐说：离象征文明，乾象征刚健。九五居中得正，与六二相应，能够会合同人，因此能够通达天下人的心志，所以有利于渡过大河，也有利于君子坚守正道。

《彖传》说：同人卦……

《九家易》说：乾居于离上，与离相合而成为太阳。天与太阳具有相同的光明，一同照耀下方。君子效法这一点，使上下同心，所以称为“同人”。

柔顺的爻取得正当的位置并居于中位，又与乾相应，这就是同人。

蜀才说：此卦原本是夬卦。九二上升到上位，上六下降到二位，于是形成“柔得位得中，而应乎乾”的结构。这是下位承奉上位的象征，其义理在于人与人和同，所以称为“同人”。

同人卦说：“与人会合同心于原野，亨通；有利于渡过大河。”这是乾道运行的结果。

虞翻说：同人卦与师卦旁通。巽象征和同，乾象征原野，师卦的震象征人。六二居中并与乾相应，所以说“同人于野，亨”。这是孔子用来说明容易混淆的地方并彰显幽微义理的例子。师卦的震象征丈夫，巽象征妇人，这就是所谓二人同心。因此这里不说君臣、父子、兄弟或朋友，而特意只说“人”。乾卦九四、上九失位，变动后形成坎体，所以说“利涉大川”；这正是乾道运行的表现。

侯果说：九二上升到上位，上位成为郊野之象，因此能够“同人于野”并获得通达，是因为乾爻向上运行。所以特意说“乾行也”。

以文明配合刚健，居中得正而彼此相应，这就是君子的正道。

何妥说：离象征文明，乾象征刚健。所谓刚健，不是崇尚武力，而是以文明为根本；彼此相应，不是以邪曲相合，而是以中正相应。因此说“利君子贞”。

只有君子才能够通达天下人的心志。

虞翻说：“唯”就是独有。九四变动后形成坎，坎象征通达，也象征心志，所以说能够通达天下人的心志。这是说九五能够按照族类辨别事物，使圣人兴起之后万物都显现出来。

崔觐说：君子指九五。九五能够舍弃一己之私而与众人和同，从而通达天下人的心志。如果是九三、九四这样的臣下，则不应当承担这一意义。因此爻辞没有直接说他们能够“同人”。

《象传》说：天与火共同构成同人卦的象征。

荀爽说：乾居于离上，彼此共同居处，所以称为“同人”。

君子由此领悟，应当依照类别汇聚同类，并辨别不同事物。

虞翻说：君子指乾。师卦的坤象征类别，乾象征族类。“辩”就是分别。乾是阳类事物，坤是阴类事物。卦体包含姤卦，天地相遇，各种事物都显明起来，这是乾以光明照临坤的象征。因此说“以类族辩物”，意思是同类的事物依其类别聚集，不同事物则依其群类区分。孔子说君子能够和谐相处却不抹平差异，所以同人卦的象征在于“以类族辩物”。

**ChatGPT 翻译（系统续译：六爻及注文）**

初九：在门外与人和同，没有灾咎。

虞翻说：乾象征门。初九与九四相同相应，九四变动后与初九相应，所以说“无咎”。

《象传》说：走出门去与人和同，又有谁能归咎于你呢？

崔觐说：初九刚健而没有正应，与六二相比邻，能够以柔和的方式亲近他人，这是走出门与人交往的象征，因此又有谁能责怪它呢？

编者按：初九属于震爻。帝出乎震，震象征大道，也有入门、出门的象征。

六二：只与自己的宗族或小团体和同，会有憾惜。

荀爽说：“宗”就是众人。九三据有六二这一阴爻，六二与九四功用相同，又与九五相应，并且接近初九；上下众多阳爻都想与六二结为一体，所以说“同人于宗”。阴柔之道本应守持贞静，从一而终，如今却只在宗族小范围内求同，因此有憾惜。

《象传》说：只与宗族之人和同，是一种狭隘而可憾的道路。

侯果说：“宗”指九五。六二是同人卦中形成和同关系的关键，众人都仰望它。六二与九五有正应，只与九五和同；若越过九五而另求和同，就会受阻。它不能广泛地与众人和同，作为主导者，其德行便显得狭窄。虽然这种和同范围有限，却也符合妻子与臣下守分从属的道理。

九三：把兵器埋伏在草莽之中，登上高陵观望，三年都不能兴兵行动。

虞翻说：巽象征潜伏，震象征草莽，离象征兵戎。九四变动时，九三处在坎体之中，隐蔽自藏，所以说“伏戎于莽”。巽象征高处，师卦的震象征丘陵，九三借巽所象征的股部登上高陵。爻位在第三位，乾象征年岁。“兴”就是起兵行动。它的行动没有失去应守的位置，所以说“三岁不兴”。

《象传》说：把兵戎埋伏在草莽中，是因为面对的敌手强大；三年不能兴兵，又怎么能够贸然行动呢？

崔觐说：九三与六二相邻，想要与六二和同。但它像盗贼憎恨主人一样，嫉恨九五，所以把兵力隐藏在野外，准备袭击对方，因此说“伏戎于莽”。九五已经居于上位，所以说“升其高陵”。一爻代表一年，从九三到九五，连续遇到刚强的敌手，因此三年都不能行动；既然如此，又怎么能够贸然前进呢？

编者按：九三的互体包含离、巽；巽象征草木，离象征戈兵，正是兵戎隐藏在草莽中的形象。

九四：登上城墙，却不能发动攻击，结果吉祥。

虞翻说：巽象征城墙。九四处在巽体上方，所以说“乘其庸”。九四变动后承接九五，卦体成为讼卦，乾的刚健居于上方，所以不能发动攻击；不攻击便能得到吉祥。

《象传》说：登上城墙却不能进攻，是因为从义理上说本来就不应当进攻。之所以能够吉祥，是因为陷入困境后又返回正道。

王弼说：九四居于上位而想攻击下方，力量足以登上城墙。可是它所处的位置并不正当，又与九三争夺六二。六二本来与九五相应，九三也没有侵犯九四；九四若攻击九三以求得到六二，就更加违背道义。这样做既违背义理，又伤害礼节，众人不会支持。它虽然凭借形势登上城墙，但从道义上终究不能进攻。之所以反而得到吉祥，是因为在困顿中转而回归正道。

九五：与人和同，起初号哭，后来欢笑；大军战胜阻碍，终于彼此相遇。

虞翻说：九五与六二相应。巽象征号哭，乾象征先，所以说“先号咷”。师卦的震处在下方，所以说“后笑”；震也象征后来欢笑。乾象征大，同人卦反转为师卦，所以说“大师”。从六二到九五的卦体包含姤卦，姤有相遇之义，所以说“相遇”。

《象传》说：同人之初所以号哭，是因为九五居中而正直；大军最终相遇，说的是双方经过克服阻碍而相会。

侯果说：乾的德性在于居中正直，不偏私于任何事物，想要使天下普遍和同，才开始与六二建立和同关系。九三、九四行为失当，又近距离阻隔在旁，因此还没有获得同心，所以起初号哭。此时必须与志同道合者相会，但敌对者阻挡道路；双方经过言说和克服阻碍，最终才能相遇，所以后来欢笑。

《九家易》说：乾象征言语。

上九：在郊外与人和同，没有悔恨。

虞翻说：乾象征郊野。上九失位而没有正应，按照与乾卦上九相同的义理，本来应当有悔；但它与人同心，所以没有悔恨。

《象传》说：在郊外与人和同，是因为其志向尚未完全实现。

侯果说：独自处在外部，所以是在郊外与人和同。它不与内部的人争夺，因此没有悔恨。同人之时，只能在郊外与人和同，其志向还没有实现。

**本条翻译状态：**以上 ChatGPT 工作译文覆盖本条目前录入的《序卦》、卦辞、《彖传》《象传》、初九至上九及其所录注文。对卦象推演、爻位关系和注家解释按各家原意分别翻译，不将注文混写为经文；本译文尚未与四库影印页逐字校勘。
"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷四·大有卦集解", "底本：维基文库《周易集解（李鼎祚）/卷四》，录入页面现存正文行422—495，覆盖大有卦经文、彖传、象传、六爻及页面所载注文。底本中的“匪其尫”等疑难字形依页面照录，不擅自改字；异文与影印本尚待逐字校勘。译文尚未完成。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷四", """《序卦》曰：與人同者，物必歸焉，故受之以大有。
崔覲曰：以欲從人，物必歸已，所以成大有。

(乾下離上)。大有：元亨。
虞翻曰：與比旁通。柔得尊位大中，應天而時行，故「元亨」也。
姚規曰：互體有兌，兌為澤，位在秋也。乾則施生，澤則流潤，離則長茂，秋則成收，大富有也。大有，則「元亨」矣。
鄭玄曰：六五體離，處乾之上，猶大臣有聖明之德，代君為政，處其位，有其事而理之也。元亨者，又是能長群臣以善，使嘉會禮通，若周公攝政，朝諸侯於朋堂，是也。

《彖》曰：大有，柔得尊位，大中而上下應之，曰大有。
王弼曰：處尊以柔，居中以大。體無二陰，以分其應，上下應之，靡所不納，大有之義也。
其德剛健而文明，應乎天而時行，是以元亨。
虞翻曰：謂五，以日應乾而行於天也。時，謂四時也。大有亨。比初動成震為春；至二，兌為秋。至三，離為夏。坎為冬。故曰「時行」。以乾亨坤，是以「元亨」。

《象》曰：火在天上，大有。
荀爽曰：謂夏，火王在天，萬物並生，故曰「大有」也。
君子以遏惡揚善，順天休命。
虞翻曰：遏，絕。揚，舉也。乾為揚善，坤為遏惡，為順。以乾滅坤。體夬，揚於王庭。故「遏惡揚善」。乾為天休，二變時，巽為命，故「順天休命」。

初九：無交害，匪咎，艱則無咎。
虞翻曰：害謂四。四離火，為惡人，故「無交害」。初動，震為交。比坤為害。匪，非也。艱，難，謂陽動比初成屯。屯，難也。變得位，艱則無咎。
《象》曰：大有初九，無交害也。
虞翻曰：害，謂四。

九二：大車以載，有攸往，無咎。
虞翻曰：比坤為大車，乾來積上，故「大車以載」。往，謂之五。二失位，變得正應五。故「有攸往無咎」矣。
《象》曰：大車以載，積中不敗也。
盧氏曰：乾為大車，故曰「大車以載」。體剛履中，可以任重，有應於五。故所積皆中，而不敗也。

九三：公用亨於天子，小人弗克。
虞翻曰：天子謂五。三，公位也。小人謂四。二變得位，體鼎象。故「公用亨於天子」。四折鼎足，覆公餗，故小人不克也。
《象》曰：公用亨於天子，小人害也。
虞翻曰：小人，謂四也。

九四：匪其尫，無咎。
虞翻曰：匪，非也。其位尫。足尫，體行不正，四失位，折震足，故「尫」。變而得正，故「無咎」。尫或作彭，作旁聲，字之誤。
《象》曰：匪其尫，無咎，明辯折也。
虞翻曰：折之離，故「明辯折也」。四在乾則尫；在坤為鼠；在震噬用胏得金矢；在巽折鼎足，在坎為鬼方；在離焚死；在艮旅於處，言無所容；在兌睽孤孚厲。三百八十四爻，獨無所容也。

六五：厥孚交如，威如吉。
虞翻曰：孚，信也。發而孚二，故「交加」。乾稱威，發得位，故「威如吉」。
《象》曰：厥孚交如，信以發誌也。威如之吉，易而無備也。
侯果曰：其體文明，其德中順。信發乎誌，以覃於物。物懷其德，以信應君。君物交信，厥孚交如也。為卦之主，有威不用，唯行簡易，無所防備。物感其德，翻更畏威，威如之吉也。

上九：自天右之，吉，無不利。
虞翻曰：謂乾也。右，助也。大有通比，坤為自，乾為天，兌為右，故「自天右之」。比坤為順，乾為信。天之所助者順，人之所助者信。履信思順，又以尚賢，故「自天右之，吉無不利」。
王弼曰：余爻皆乘剛，已獨乘柔順也。五為信德，而已履焉，履信者也。居豐富之代，物不累心，高尚其誌，尚賢者也。爻有三德，盡夫助道，故《系辭》具焉也。
《象》曰：大有上吉，自天右也。
《九家易》曰：上九說五，以柔處尊，而自謙損。尚賢奉已，上下應之，為乾所右，故吉且利也。

**ChatGPT 翻译（大有卦集解：序卦、卦辞、彖传与象传）**

《序卦》说：能够与众人和同，万物就必然归附于他，所以同人卦之后接着是大有卦。

崔觐说：以自己的意愿顺从众人，众人和万物就会归附于自己，因此形成“大有”的局面。

（下卦为乾，上卦为离。）大有卦卦辞说：“至为亨通。”

虞翻说：大有卦与比卦旁通。柔爻居于尊贵的位置，并且处在中位，与天道相应而顺应时势运行，所以说“元亨”。

姚规说：互体中有兑，兑象征泽，也对应秋季。乾象征施行生长，泽象征流布滋润，离象征生长繁茂，到了秋季便成熟收获，因而成为丰盛富有的状态。大有之时，所以“元亨”。

郑玄说：六五的卦体属于离，处在乾卦之上。这好比大臣具有圣明的德行，代替君主处理政事，处于相应的位置，承担相应事务并治理它们。“元亨”还意味着能够使群臣增长善行，使美好的会合得以实现、礼制得以通行。这就像周公摄行政事，在明堂会见诸侯一样。

《彖传》说：“大有，是柔爻得到尊贵的位置，居中而广大，上下各爻都与它相应，因此称为大有。”

王弼说：以柔顺之爻居于尊位，处在中位而具有广大之德。卦体中没有第二个阴爻来分散它所接受的应援，因此上下各爻都与它相应，没有什么不能接纳，这就是大有的含义。

“它的德行刚健而文明，与天道相应并顺应时势运行，因此至为亨通。”

虞翻说：这里说的是六五，它像太阳一样与乾相应，并在天道中运行。“时”指四季。大有卦的亨通，可以从卦变推演：比卦初爻发动形成震，震对应春季；到第二爻，兑对应秋季；到第三爻，离对应夏季；坎对应冬季。因此说“时行”。乾的亨通作用于坤，所以说“元亨”。

《象传》说：“火在天上，这就是大有。”

荀爽说：这是指夏季，火气旺盛而居于天上，万物一同生长，所以称为“大有”。

“君子因此遏止邪恶、彰显善行，顺从上天美好的命令。”

虞翻说：“遏”是断绝、制止；“扬”是举起、彰显。乾象征彰扬善行，坤象征遏止邪恶，也象征顺从。以乾消除坤，卦体成为夬卦，夬卦有在王庭宣扬的象征，所以说“遏恶扬善”。乾象征上天的美善；第二爻变动时，巽象征命令，所以说“顺天休命”。

**ChatGPT 翻译（大有卦集解：六爻与注文）**

**初九：**“不与造成伤害的人交往，并非自身有过；若能保持艰难谨慎，就没有灾咎。”

虞翻说：“害”指九四。九四属于离，离火在这里象征恶人，所以说“无交害”。初九发动，震象征交往；比卦的坤象征伤害。“匪”就是“非”。“艰”是艰难，指阳爻发动后，与初九相关的卦象形成屯卦；屯有艰难之义。变动后爻位得到端正，因此处在艰难之中仍可没有灾咎。

《象传》说：“大有卦初九，是不与造成伤害者交往。”

虞翻说：“害”指九四。

**九二：**“用大车装载，有所前往，没有灾咎。”

虞翻说：比卦的坤象征大车，乾阳来到上方积聚，所以说“大车以载”。“往”指前往九五。九二原本失位，变动后得到正位，并与九五相应，所以说“有所前往，没有灾咎”。

《象传》说：“用大车装载，是因为所积载的东西处在车中，不会倾覆。”

卢氏说：乾象征大车，所以说“大车以载”。九二刚健而居中，能够承担重任，又与九五相应，因此所承载、积聚的事物都能安稳处中，不会倾覆。

**九三：**“公侯在天子那里享用祭品，小人不能胜任。”

虞翻说：天子指九五。九三处在公侯之位。“小人”指九四。六二变动后，卦体呈现鼎卦之象，所以说“公用亨于天子”。九四会折断鼎足，使公侯的祭品倾覆，因此小人不能胜任。

《象传》说：“公侯在天子那里享用祭品，是因为小人会造成祸害。”

虞翻说：“小人”指九四。

**九四：**“不是它的跛弱状态，没有灾咎。”

虞翻说：“匪”就是“非”。九四所处的位置有跛弱之象。脚部跛弱，身体行动不正；九四失位，又有折断震足的象征，所以称为“尫”。变动后得到正位，因此没有灾咎。“尫”也有写作“彭”的，或因旁边声符相近而产生字形讹误。这里底本作“尫”，译文保留其疑难性质，不将它径直改成确定字词。

《象传》说：“不是它的跛弱状态而没有灾咎，是因为能够明辨并折断不正之处。”

虞翻说：折断之象属于离，所以说“明辨折也”。九四处于乾卦时象征跛弱；处于坤时象征鼠；处于震时象征噬嗑卦中咬食带骨之肉而得到金属箭头；处于巽时象征折断鼎足；处于坎时象征鬼方；处于离时象征被焚烧而死；处于艮时象征旅人无处容身；处于兑时象征睽卦中孤立而心怀忧惧。三百八十四爻之中，唯独此爻被说成无所容身。此段是虞翻依各卦取象所作的连贯推演，译文按其列举次序呈现，不把这些象义当作经文。

**六五：**“诚信交相感通，显出威严，吉祥。”

虞翻说：“孚”就是诚信。六五发动而与六二诚信相交，所以说“交如”。乾象征威严；发动后得到正位，所以说“威如吉”。原注文此处写作“交加”，与经文“交如”不一致，可能涉及文字或录入问题，暂保留其差异，不擅自改定。

《象传》说：“诚信交相感通，是以诚信来发动众人的心志；显出威严而吉祥，是因为行事平易简约，不作过多防备。”

侯果说：六五的卦体文明，德行中正而柔顺。诚信从内心发出，并扩展到万物。万物感受到它的德行，以诚信回应君主。君主与万物彼此以诚信相交，这就是“厥孚交如”。六五是全卦的主爻，具有威严却不滥用威势，只施行简易平和的政令，不处处设防。万物感受到它的德行，反而更加敬畏其威严，这就是“威如之吉”。

**上九：**“上天从旁帮助他，吉祥，没有什么不利。”

虞翻说：这里指乾。 “右”是帮助。大有卦与比卦相通，坤象征“自”，乾象征天，兑象征右，所以说“自天右之”。比卦的坤象征顺从，乾象征诚信。上天所帮助的是顺从天道的人，人们所帮助的是诚信的人。践行诚信、思虑顺道，又能尊崇贤者，所以说“自天右之，吉无不利”。

王弼说：其他各爻都乘在刚爻之上，只有上九独自乘在柔顺之爻上。六五具有诚信的德行，上九履行并承接它，所以是践行诚信。处在丰盛富有的时代，却不让外物牵累自己的内心；使自己的志向保持高尚，并尊崇贤者。此爻具备三种德行，完全符合受助之道，所以《系辞传》对此作了详尽说明。

《象传》说：“大有卦上九的吉祥，是上天从旁帮助的结果。”

《九家易》说：上九悦服六五。六五以柔爻居于尊位，又能够谦逊自抑；上九尊崇贤者并奉行六五之道，上下各爻都与它相应，因此得到乾的帮助，所以吉祥而且有利。

**本条翻译状态：**以上 ChatGPT 工作译文覆盖本条目前录入的《序卦》、卦辞、《彖传》《象传》、初九至上九及所录注文。特别保留“匪其尫”“交加”等底本疑难文字，不将其静默改写为通行本；“公用亨”按祭享语境解释，同时保留虞翻以鼎卦作象的注文。译文尚未与四库影印页逐字校勘。
"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷四·谦卦集解", "底本：维基文库《周易集解（李鼎祚）/卷四》，录入页面现存正文行497—615，覆盖谦卦经文、彖传、象传、六爻及页面所载注文。底本中的异体、疑难字形及注文署名照录（如“捋”“捴”等），尚未与影印本逐字校勘。译文尚未完成。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷四", """《序卦》曰：有大者不可以盈，故受之以謙。
崔覲曰：富貴而自遺其咎，故有大者不可盈。當須謙退，天之道也。

(艮下坤上)。謙：亨。
虞翻曰：乾上九來之坤，與履旁通。天道下濟，故亨。彭城蔡景君說：剝上來之三。
君子有終。
虞翻曰：君子謂三。艮終萬物，故「君子有終」。
鄭玄曰：艮為山，坤為地。山體高，今在地下，其於人道，高能下下，謙之象。亨者，嘉會之禮，以謙而為主。謙者，自貶損以下人，唯艮之堅固，坤之厚順，乃能終之，故君子之人有終也。

《彖》曰：謙亨。
《九家易》曰：艮，山。坤，地。山至高，地至卑，以至高下至卑，故曰「謙」也。謙者兌世，艮為兌合，故「亨」。
天道下濟而光明，
荀爽曰：乾來之坤，故「下濟」。陰去為離，陽來成坎，日月之象，故「光明」也。
地道卑而上行。
侯果曰：此本剝卦。乾之上九來居坤三，是「天道下濟而光明」也。坤之六三上升乾位，是「地道卑而上行」者也。
天道虧盈而益謙，
虞翻曰：謂乾盈履上，虧之坤三，故「虧盈」。貴處賤位，故「益謙」。
崔覲曰：若日中則昃，月滿則虧，損有余以補不足，天之道也。
地道變盈而流謙，
虞翻曰：謙二以坤變乾盈。坎動而潤下，水流濕，故「流謙」也。
崔覲曰：高岸為谷，深谷為陵，是為變盈而流謙，地之道也。
鬼神害盈而福謙，
虞翻曰：鬼謂四。神謂三。坤為鬼害，乾為神福，故「鬼神害盈而福謙」也。
崔覲曰：朱門之家，鬼闞其室，黍稷非馨，明德惟馨，是其義矣。
人道惡盈而好謙。
虞翻曰：乾為好，為人。坤為惡也。故人道惡盈。從上之三，故「好謙」矣。崔覲曰：滿招損，謙受益，人之道也。
謙，尊而光，卑而不可逾，
虞翻曰：天道遠，故「尊光」。三位賤，故「卑」。坎水就下，險弱難勝，故「不可逾」。
君子之終也。
孔潁達曰：尊者有謙，而更光明盛大。卑者有謙，而不逾越，是君子之終也。言君子能終其謙之善，而又獲謙之福，故曰「君子有終」也。

《象》曰：地中有山，謙。
劉表曰：地中有山，以高下下，故曰「謙」。謙之為道，降已升人，山本地上，今居地中，亦降體之義，故為謙象也。
君子以捋多益寡，稱物平施。
虞翻曰：君子謂三。捋，取也。艮為多；坤為寡；乾為物，為施；坎為平。謙乾盈益謙，故「以捋多益寡，稱物平施」。
侯果曰：裒，聚也。《彖》雲：天道益謙，則謙之大者，天益之以大福；謙之小者，天益之以小福。故君子則之，以大益施大德，以小益施小德，是「稱物平施」也。

初六：謙謙君子，用涉大川，吉。
荀爽曰：初最在下，為謙。二陰承陽，亦為謙。故曰「謙謙」也。二陰一陽，相與成體，故曰「君子」也。九三體坎，故「用涉大川，吉」也。
《象》曰：謙謙君子，卑以自牧也。
《九家易》曰：承陽卑謙，以陽自牧養也。

六二：鳴謙，貞吉。
姚信曰：三體震，為善鳴，二親承之，故曰「鳴謙」。得正處中，故「貞吉」。
《象》曰：鳴謙貞吉，中心得也。
崔覲曰：言中正心與謙相得。
虞翻曰：中正謂二。坎為心也。

九三：勞謙，君子有終，吉。
荀爽曰：體坎為勞，終下二陰，君子有終，故「吉」也。
《象》曰：勞謙君子，萬民服也。
荀爽曰：陽光居五，自卑下眾，降居下體，君有下國之意也。眾陰皆欲捴陽上居五位，群陰順陽，故「萬民服也」。

六四：無不利，捴謙。
荀爽曰：四得位處正，家性為謙，故「無不利」。陰欲捴三，使上居五，故曰「捴謙」。捴，猶舉也。
《象》曰：無不利捴謙，不違則也。
《九家易》曰：陰捴上陽，不違法則。

六五：不富以其鄰，
荀爽曰：鄰謂四與上也。自四以上乘陽，乘陽失實，故皆不富，五居中有體，故總言之。
利用侵伐，無不利。
荀爽曰：謂陽利侵伐來上，無敢不利之者。
《象》曰：利用侵伐，征不服也。
荀爽曰：不服，謂五也。
案：六五離爻。離為戈兵，侵伐之象也。

上六：鳴謙，利用行師，征邑國。
虞翻曰：應在震，故曰「鳴謙」。體師象，震為行，坤為邑國，利五之正，已得從征，故「利用行師，征邑國」。
《象》曰：鳴謙，誌未得也，可用行師，征邑國也。
《九家易》曰：陰陽相應，故「鳴謙」也。雖應不承，故「誌未得」。謂下九三可行師來上。坤為邑國也；三應上，上呼三征，來居五位，故曰「利用行師征邑國」也。
案：上六兌爻。兌為口舌，鳴謙之象也。

**ChatGPT 翻译（谦卦集解：序卦、卦辞、彖传与象传）**

《序卦》说：拥有丰盛的事物，却不能因此自满，所以大有卦之后接着是谦卦。

崔觐说：富贵而又自满，会给自己招来祸患。因此拥有丰盛之物的人不能自满，应当谦退；这是天道的要求。

（下卦为艮，上卦为坤。）谦卦卦辞说：“亨通。君子能够保持善终。”

虞翻说：乾卦上九来到坤卦的三爻位置，与履卦旁通。天道向下施济，所以亨通。彭城蔡景君则认为，是剥卦上爻来到第三爻的位置。

“君子有终。”

虞翻说：君子指九三。艮有终结万物的象征，所以说“君子有终”。

郑玄说：艮象征山，坤象征地。山体本来高耸，如今却处在地下；在人事上，这象征地位高的人能够谦居于地位低的人之下，是谦卦的形象。“亨”是美好的会合与礼仪，以谦逊作为根本。谦逊就是降低自己、礼让他人；只有艮的坚固和坤的厚重柔顺，才能把这种德行坚持到底，所以君子能够有始有终。

《彖传》说：“谦而亨通。”

《九家易》说：艮是山，坤是地。山极高，地极低；以极高者处在极低者之下，所以称为“谦”。谦卦在兑世之位，艮与兑相合，因此亨通。

“天道向下施济，因此显得光明。”

荀爽说：乾来到坤的位置，所以说“下济”。阴爻离去而形成离，阳爻到来而形成坎，呈现日月的象征，所以说“光明”。

“地道虽然处于卑下，却向上运行。”

侯果说：此卦原本是剥卦。乾的上九来到坤卦六三的位置，这就是“天道下济而光明”；坤的六三上升到乾卦的位置，这就是“地道卑而上行”。

“天道减损盈满，增益谦下。”

虞翻说：乾处在上位而盈满，如今减损它，使其来到坤卦六三，所以说“亏盈”。使尊贵者处于卑下的位置，因此说“益谦”。

崔觐说：太阳到了正午就会偏斜，月亮圆满之后就会亏缺；减损有余的部分，用来补充不足，这是天道。

“地道改变盈满，使其流向谦下。”

虞翻说：谦卦六二以坤变动乾的盈满之象。坎发动而润泽下方，水流向低湿之处，所以说“流谦”。

崔觐说：高耸的岸边变成山谷，深谷又变成丘陵，这就是盈满发生变化、流向谦下，是地道的规律。

“鬼神祸害盈满者，赐福给谦逊者。”

虞翻说：“鬼”指九四，“神”指九三。坤象征鬼与祸害，乾象征神与福佑，所以说“鬼神害盈而福谦”。

崔觐说：富贵之家如果骄奢自满，鬼神便会窥伺其家；祭祀所用的黍稷并不一定芬芳，真正馨香的是光明的德行。这就是这句话的意思。

“人道厌恶盈满，喜爱谦逊。”

虞翻说：乾象征喜爱，也象征人；坤象征厌恶，所以说人道厌恶盈满。由上位来到六三，因此说“好谦”。

崔觐说：自满会招来损失，谦逊能够获得益处，这是人世间的道理。

“谦逊的人虽然尊贵，却因此更加光明；虽然处于卑下，却不可被超越。”

虞翻说：天道高远，所以说“尊而光”。九三处在较低的位置，所以说“卑”。坎水向下流，虽然险阻而柔弱，却难以被超越，因此说“不可逾”。

“这就是君子能够有始有终的道理。”

孔颖达说：尊贵者具有谦逊之德，反而更加光明盛大；卑下者具有谦逊之德，也不会被轻易超越，这就是君子能够有始有终。意思是君子能够把谦逊的善行坚持到底，并因此获得谦德带来的福报，所以说“君子有终”。

《象传》说：“地中有山，这就是谦。”

刘表说：山在地中，是以高处居于低处之下，所以称为“谦”。谦逊之道，是降低自己、抬高他人。山本来在地上，如今却处于地中，也体现了降低自身的意义，因此成为谦卦的象征。

“君子因此取出多余的部分，补益不足的部分；衡量事物，使施予公平适当。”

虞翻说：君子指九三。“捋”是取。“艮”象征多，“坤”象征少；乾象征事物，也象征施予；坎象征平。谦卦以乾的盈满补益谦下，所以说“以捋多益寡，称物平施”。

侯果说：“裒”是聚集。《彖传》说天道增益谦逊者，那么谦德大的人，天就以大的福分增益他；谦德小的人，天就以小的福分增益他。因此君子效法天道，以大的益处施予大德之人，以小的益处施予小德之人，这就是“称物平施”。本条底本文字作“捋”，侯果注文则作“裒”；两种字形均保留，不据通行本静默统一。

**ChatGPT 翻译（谦卦集解：六爻与注文）**

**初六：**“谦逊而又谦逊的君子，能够凭此涉越大河，吉祥。”

荀爽说：初六处在最下方，因此是谦；六二这两个阴爻承接阳爻，也体现谦逊，所以说“谦谦”。两个阴爻与一个阳爻共同构成卦体，因此称为“君子”。九三的卦体属于坎，坎象征大川，所以说“用涉大川，吉”。

《象传》说：“谦逊而又谦逊的君子，是以谦卑来约束、修养自己。”

《九家易》说：初六承接阳爻而表现谦卑，是以阳爻所象征的德行来修养自己。

**六二：**“谦逊之名声传播开来，守正则吉祥。”

姚信说：九三的卦体呈现震象，震有善于鸣响的象征；六二亲近并承接九三，所以说“鸣谦”。六二得正而居中，所以“贞吉”。

《象传》说：“谦逊的名声传播开来，守正而吉祥，是因为内心与谦德相合。”

崔觐说：这是说居中得正的内心与谦逊相互契合。

虞翻说：居中得正指六二。坎象征内心。

**九三：**“有功劳而仍然谦逊，君子能够保持善终，吉祥。”

荀爽说：九三的卦体属于坎，坎象征劳苦；它最终处在下方两个阴爻之下。君子能够有始有终，所以吉祥。

《象传》说：“有功劳而仍然谦逊的君子，能够使万民归服。”

荀爽说：阳爻的光明本来居于九五，却自居于众人之下，下降到下卦的位置，表现出君主愿意谦居于臣民之下的意思。众阴都想把九三这个阳爻推举上升到九五的位置，众阴顺从阳爻，所以说“万民服也”。底本在此作“捴阳”，按语义译为推举阳爻，但保留原字，不擅自改成通行字。

**六四：**“没有什么不利；举荐、推举谦逊者。”

荀爽说：六四得位而居正，卦的性质又是谦逊，所以“无不利”。阴爻想要推举九三，使它上升到九五的位置，所以说“捴谦”。“捴”犹如举起、推举。

《象传》说：“没有什么不利，推举谦逊者，是因为没有违背法则。”

《九家易》说：阴爻推举上方的阳爻，没有违背法则。

**六五：**“不以财富自居，却能拥有邻近者。”

荀爽说：“邻”指六四与上六。从六四以上都乘在阳爻之上，乘阳而失去充实，所以都不富有；六五居中而具有统摄全卦的形体，因此用它概括说明。

“有利于发动征伐，没有什么不利。”

荀爽说：这是说阳爻有利于发动征伐并上升，没有人敢说这样做不利。

《象传》说：“有利于发动征伐，是为了征讨不服从者。”

荀爽说：这里所说的不服从者，指六五。

案语：六五属于离爻。离象征戈兵，因此具有侵伐的象征。

**上六：**“谦逊之名声传播开来，有利于出动军队，征讨自己的邑国。”

虞翻说：上六的正应在震，所以说“鸣谦”。卦体呈现师卦的象征，震象征行动，坤象征邑国。九五变动而得正，上六因此能够参与征伐，所以说“利用行师，征邑国”。

《象传》说：“谦逊之名声传播开来，是因为志向尚未实现；可以出动军队，征讨邑国。”

《九家易》说：阴阳相互呼应，所以说“鸣谦”。虽然有正应，却没有承接，因此“志未得”。这是说下方九三可以出动军队而来到上方。坤象征邑国；九三与上六相应，上六呼唤九三前来征伐，使九三来到九五的位置，所以说“利用行师，征邑国”。

案语：上六属于兑爻。兑象征口舌，因此有谦逊之名声传播的象征。

**本条翻译状态：**以上 ChatGPT 工作译文覆盖本条目前录入的《序卦》、卦辞、《彖传》《象传》、初六至上六及其所录注文。底本中的“捋”“捴”等疑难字形均保留提示；“裒多益寡”仅作为侯果注文中的异文说明，不据此改写经文。译文尚未与四库影印页逐字校勘。
"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷四·豫卦集解", "底本：维基文库《周易集解（李鼎祚）/卷四》，录入页面现存正文行617—715，覆盖豫卦经文、彖传、象传、六爻及页面所载注文。页面原有异体、疑难字形、注文及夹注标记照录（如“戠”“盍戠”等），尚未与影印本逐字校勘。译文尚未完成。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷四", """《序卦》曰：有大而能謙，必豫。故受之以豫。
鄭玄曰：言國既大而能謙，則於政事恬豫。雷出地奮豫，行出而喜樂之意。

(坤下震上)。豫：利建侯、行師。
鄭玄曰：坤，順也。震，動也。順其性而動者，莫不得，得其所，故謂之豫。豫，喜逸說樂之貌也。震又為雷，諸侯之象。坤又為眾，師役之象。故「利建侯、行師」矣。
虞翻曰：復初之四，與小畜旁通。坤為邦國，震為諸侯。初至五，體比象。四利復初，故「利建侯」。三至上，體師象，故「行師」。

《彖》曰：豫，剛應而誌行，
△侯果曰：四為卦主，五陰應之，剛誌大行，故曰「剛應而誌行」。
順以動，豫。
崔覲曰：坤下震上，順以動也。
豫順以動，故天地如之，
虞翻曰：小畜，乾為天。坤為地。如之者，謂天地亦動，以成四時，而況建侯行師。言其皆應而豫也。
而況建侯行師乎！
《九家易》曰：震為建侯，坤為行師；建侯所以興利，行師所以除害。利興害除，民所豫樂也。天地有生殺，萬物有始終。王者盛衰，亦有叠更，猶武王承亂，而應天地。建侯行師，奉辭除害，民得豫說，君得安樂也。
天地以順動，
虞翻曰：豫變通小畜。坤為地。動初至三成乾，故天地以順動也。
故日月不過，而四時不忒。
虞翻曰：過謂失度，忒，差叠也。謂變初至需，離為日，坎為月，皆得其正，故「日月不過」。動初時，震為春。至四，兌為秋。至五，坎為冬。離為夏。四時位正，故「四時不忒」。通變之謂事，蓋此之類。
聖人以順動，則刑罰清而民服。
虞翻曰：清，猶明也。動初至四，兌為刑。至坎，為罰。坎、兌體正，故「刑罰清」。坤為民，乾為清，以乾乘坤，故「民服」。
案：帝出震，聖人也。坎為法律，刑罰也。坤為眾，順而民服也。
豫之時義大矣哉。
虞翻曰：順動天地，使日月四時，皆不過差。刑罰清而民服，故「義大」也。

《象》曰：雷出地奮，豫。
崔覲曰：震在坤坤，故言「雷出地」。雷，陽氣，亦謂龍也。夏至後，陽氣極而一陰生。陰陽相擊，而成雷聲。雷聲之疾，有龍奮迅豫躍之象，故曰「奮豫」。
先王以作樂崇德，殷薦之上帝，以配祖考。
鄭玄曰：奮，動也。雷動於地上，而萬物乃豫也。以者，取其喜佚動搖，猶人至樂，則手欲鼓之，足欲舞之也。崇，充也。殷，盛也。薦，進也。上帝，天帝也。王者功成作樂，以文得之者，作鑰舞；以武得之者，作萬舞；各充其德而為制。祀天帝以配祖考者，使與天同饗其功也。故《孝經》雲：郊祀后稷以配天，宗祀文王於明堂，以配上帝是也。

初六：鳴豫，兇。
虞翻曰：應震善鳴，失位，故「鳴豫，兇」也。
《象》曰：初六鳴豫，誌窮兇也。
虞翻曰：體剝蔑貞，故「誌窮兇也」。

六二：介於石，
虞翻曰：介，纖也。與四為艮，艮為石，故「介於石」。
不終日，貞吉。
虞翻曰：與小畜通，應在五。終變成離，離為日，得位。欲四爭復初，已得休之。故「不終日，貞吉」。
《象》曰：不終日，貞吉，以中正也。
侯果曰：得位居中，柔順正一，明豫動之可否，辯趣舍之權宜。假如堅石，不可移變，應時則改，不待終日。故曰豫之正吉。

六三：盱豫悔，遲有悔。《象》曰：盱豫有悔，位不當也。
王弼曰：履非其位，承動豫之主。若其雎盱而豫，悔亦至焉。遲而不從，豫之所疾，進退離悔，位不當也。
向秀曰：雎盱，小人喜說、佞媚之貌也。

九四：由豫，大有得，勿疑，朋盍戠。
侯果曰：為豫之主，眾陰所宗，莫不由之以得其豫。體剛心直，誌不懷疑，故得群物依歸，朋從大合，若以簪菾之固括也。
虞翻曰：由，自從也。據有五陰，坤以眾順，故「大有得」。得群陰也。坎為疑，故「勿疑」。小畜兌為朋。盍，合也，坤為盍。戠，聚會也。坎為聚，坤為眾，眾陰並應，故「朋盍戠」。戠，舊讀作撍，作宗也。
《象》曰：由豫，大有得，誌大行也。
崔覲曰：以一陽而眾陰從已合簪交歡，故其「誌大行」也。

六五：貞疾，恒不死。
虞翻曰：恒，常也。坎為疾。應在坤，坤為死。震為反生。位在震中，與坤體絕。故「貞疾，恒不死」也。
《象》曰：六五貞疾，乘剛也。恒不死，中未亡也。
侯果曰：六五居尊，而乘於四，四以剛動，非已所乘。乘剛為政，終亦病。若「恒不死」者，以其中也。

上六：冥豫，成有渝，無咎。
虞翻曰：應在三。坤為冥。渝，變也。三失位，無應，多兇。變乃得正，體艮成。故「成有渝，無咎」。
《象》曰：冥豫在上，何可長也？
荀爽曰：陽性冥昧，居尊在上，而猶豫說，故不可長。

**ChatGPT 翻译（豫卦集解：序卦、卦辞、彖传与象传）**

《序卦》说：拥有丰盛的事物而又能够谦逊，必然会感到安乐和顺，所以谦卦之后接着是豫卦。

郑玄说：这是说国家已经强盛广大，却仍然能够谦逊，那么处理政事时就能安和愉悦。雷从地中奋起，万物随之舒展，这就是行动开展而令人喜悦的意思。

（下卦为坤，上卦为震。）豫卦卦辞说：“有利于建立诸侯，也有利于出动军队。”

郑玄说：坤象征顺，震象征行动。顺应事物本性而行动，没有什么不能获得；各得其所，所以称为“豫”。“豫”是喜悦、安逸、愉快的样子。震又象征雷，也象征诸侯；坤又象征众人，也象征军队。因此说“利建侯、行师”。

虞翻说：复卦初九来到四爻的位置，与小畜卦旁通。坤象征邦国，震象征诸侯。从初爻到五爻形成比卦的象征。九四有利于回到初九的位置，所以说“利建侯”。从三爻到上爻形成师卦的象征，所以说“行师”。

《彖传》说：“豫，是刚爻得到众爻响应，志向得以施行。”

侯果说：九四是全卦的主爻，五个阴爻都响应它。刚健的志向得以广泛施行，所以说“刚应而志行”。

“以顺从的方式行动，这就是豫。”

崔觐说：下卦为坤，上卦为震，这正是顺应而行动。

“豫卦以顺应而行动为义，所以天地也如此，何况建立诸侯、出动军队呢！”

虞翻说：小畜卦的乾象征天，坤象征地。所谓“如之”，是说天地也顺应时序而运行，从而形成四季，更何况建立诸侯、出动军队这些人事呢？这是说天地与人事都能因顺应而安乐。

《九家易》说：震象征建立诸侯，坤象征出动军队。建立诸侯是为了兴起有益之事，出动军队是为了除去祸害。利益得以兴起，祸害得以消除，百姓因此感到安乐。天地有生长与肃杀，万物有开始与终结，君王也有兴盛和衰退的更替。这就像周武王承接乱世而顺应天地之道，建立诸侯、出动军队，奉行正当的名义以除去祸害，使百姓得到安乐，君主也得以安宁。

“天地顺应时序而运行。”

虞翻说：豫卦变通而与小畜卦相通。坤象征地。初爻发动到第三爻，形成乾，所以说天地顺应而运行。

“因此日月运行不会失度，四季更替不会错乱。”

虞翻说：“过”是失去常度，“忒”是差错、交替失序。这里说初爻变动后形成需卦，离象征太阳，坎象征月亮，二者都处在适当的位置，所以说“日月不过”。初爻发动时，震象征春季；到第四爻，兑象征秋季；到第五爻，坎象征冬季；离象征夏季。四季各得其位，所以说“四时不忒”。所谓通达变化而形成事物，大概就是这一类情形。

“圣人顺应时势而行动，于是刑罚清明，百姓归服。”

虞翻说：“清”就是明。初爻发动到第四爻，兑象征刑罚；到坎，坎象征惩罚。坎、兑的卦体端正，所以说“刑罚清”。坤象征百姓，乾象征清明；以乾居于坤上，所以百姓归服。

案语：帝王出于震，震象征圣人。坎象征法律，也象征刑罚；坤象征众人。以顺应之道治理，百姓便会归服。

“豫卦所体现的时势与义理，意义真是重大啊！”

虞翻说：顺应天地而行动，使日月和四季都不失常度；刑罚清明，百姓归服，所以说它的义理重大。

《象传》说：“雷从地中奋起，这就是豫。”

崔觐说：震处在坤之上，所以说“雷出地”。雷是阳气，也可以说象征龙。夏至之后，阳气达到极盛，随后一阴开始产生。阴阳相互激荡，于是形成雷声。雷声迅疾，有如龙奋起、迅速跃动而安乐的形象，所以说“奋豫”。

“先王因此创作音乐，以崇扬德行；隆重地把音乐献给上帝，并让祖先配享。”

郑玄说：“奋”就是发动。雷在地上震动，万物于是欢欣。先王取法雷动所带来的喜悦、舒展与摇动，就像人极度快乐时，手会想要击打节拍，脚会想要舞动一样。“崇”是充实、增盛；“殷”是盛大；“荐”是进献；“上帝”就是天帝。君王功业完成之后创作音乐：以文德取得天下的，创作文舞；以武功取得天下的，创作武舞；各自充实其德行，并据此制定礼乐。祭祀天帝时让祖先配享，是使祖先与天一同享有这份功业。因此《孝经》说：在郊外祭祀后稷以配天，在明堂祭祀文王以配上帝。

**ChatGPT 翻译（豫卦集解：六爻与注文）**

**初六：**“沉溺于喜悦之中，凶险。”

虞翻说：初六的正应在震，震有善于鸣响的象征；初六又失位，所以说“鸣豫，凶”。

《象传》说：“初六沉溺于喜悦，是因为它的志向已经走到穷尽之处，因此凶险。”

虞翻说：卦体呈现剥卦，正道受到剥蚀，所以说“志穷凶也”。

**六二：**“像石头一样坚定，不等到一天结束，守正则吉。”

虞翻说：“介”有细小、坚实之意。六二与九四构成艮象，艮象征石头，所以说“介于石”。

“不终日，贞吉。”

虞翻说：此卦与小畜卦相通，正应在九五。最终变动形成离，离象征太阳；六二得到正位。九四想要争取回到初九的位置，六二已经能够及时停止。因此说“不终日，贞吉”。

《象传》说：“不等到一天结束便能判断，守正而吉祥，是因为居中得正。”

侯果说：六二得位而居中，柔顺、守正而专一，能够明辨豫乐之事是否可行，也能辨别进退取舍的适宜时机。它像坚硬的石头一样，不会轻易动摇；但能够顺应时势作出改变，不必等到一天结束。因此这是豫卦中守正而吉祥的表现。

**六三：**“仰望他人的欢乐而随之取乐，会有悔恨；迟疑不决，也会有悔恨。”

《象传》说：“仰望他人的欢乐而取乐会有悔恨，是因为所处爻位不恰当。”

王弼说：六三处在不正当的位置，又承接着发动豫乐的主爻九四。如果它只是仰望九四、依附九四而取乐，悔恨就会到来；但如果迟疑而不跟从，又是豫卦所忌讳的。进退之间都会有悔恨，原因就在于它所处的位置不正当。

向秀说：“雎盱”形容小人喜悦、谄媚讨好的样子。底本文字作“盱豫”，向秀注文写作“雎盱”，这里按注文所解释的神态翻译，不把两处字形静默统一。

**九四：**“由此获得安乐，大有所得；不要怀疑，朋友聚集会合。”

侯果说：九四是豫卦的主爻，众阴都尊奉它，没有谁不通过它而获得安乐。它的卦体刚健，内心正直，志向没有疑惑，因此能够使众人依附归顺，朋友大规模聚集会合，就像用簪子牢固地束在一起。

虞翻说：“由”是依从。九四据有五个阴爻，坤以众多而顺从为象，所以说“大有得”，也就是得到众阴的归附。坎象征疑虑，所以说“勿疑”。小畜卦的兑象征朋友。“盍”是会合，坤象征聚合；“戠”是聚集会合，坎象征聚集，坤象征众人。众阴一同响应，所以说“朋盍戠”。“戠”旧时也有读作“撍”的说法，注文又以“宗”说明其音义。底本的“戠”“撍”“宗”关系复杂，译文仅按聚集会合的解释处理，不擅自改动原字。

《象传》说：“由此获得安乐，大有所得，是因为志向得以广泛施行。”

崔觐说：以一个阳爻使众多阴爻跟随自己，聚集起来、如同簪子束发一般彼此欢悦，所以它的志向能够广泛施行。

**六五：**“守正而有疾病，但始终不会死亡。”

虞翻说：“恒”就是长久、持续。坎象征疾病；六五的正应在坤，坤象征死亡。震象征反复复生。六五处在震的中间，与坤的形体隔绝，所以说“贞疾，恒不死”。

《象传》说：“六五守正而有疾病，是因为它乘在刚爻之上；始终不会死亡，是因为其中正之德尚未丧失。”

侯果说：六五居于尊位，却乘在九四之上。九四以刚健发动，并不是六五能够真正驾驭的力量。以乘刚的方式处理政事，最终仍会成为疾病；它之所以“恒不死”，是因为六五仍然居中。

**上六：**“昏暗地沉溺于安乐之中；事情已经形成后，只要有所改变，就没有灾咎。”

虞翻说：上六的正应在六三。坤象征昏暗。“渝”就是改变。六三失位而没有正应，因此凶险较多；改变之后便能得到正位，卦体形成艮，所以说“成有渝，无咎”。

《象传》说：“昏暗地沉溺于安乐，已经处在最上方，怎么可能长久呢？”

荀爽说：阳的性质是明亮，但如今昏昧地处在尊位之上，仍然沉溺于犹豫和逸乐，因此这种状态不可能长久。

**本条翻译状态：**以上 ChatGPT 工作译文覆盖本条目前录入的《序卦》、卦辞、《彖传》《象传》、初六至上六及其所录注文。对“戠”“撍”“盍戠”“雎盱”等疑难字词保留底本差异并作解释性提示；原文卦象推演按注家各自体系翻译，不视为经文的唯一解释。译文尚未与四库影印页逐字校勘。
"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷五·随卦集解", "底本：维基文库《周易集解（李鼎祚）/卷五》，录入页面现存正文行112—199，覆盖随卦经文、彖传、象传、六爻及页面所载注文。页面异体字、疑难字形和注文署名照录，尚未与影印本逐字校勘。译文尚未完成。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷五", """《序卦》曰：豫必有隨也，故受之以隨。
韓康伯曰：順以動者，眾之所隨。

(震下兌上)。隨：元、亨、利、貞，無咎。
虞翻曰：否上之初，剛來下柔，初上得正，故「元、享、利、貞，無咎」。
鄭玄曰：震，動也。兌，說也。內動之以德，外說之以言，則天下之人，鹹慕其行，而隨從之，故謂之隨也。既見隨從，能長之以善，通其嘉禮，和之以義，幹之以正，則功成而有福，若無此四德，則有兇咎焉。焦贛曰：漢高帝與項籍，其明徵也。

《彖》曰：隨，剛來而下柔，動而說，隨。
虞翻曰：否乾上來之坤初，故「剛來而下柔」。動，震，說，兌也。
大亨貞，無咎。
荀爽曰：隨者，震之歸魂。震歸從巽，故大通。動爻得正，故利貞。陽降陰升，嫌於有咎。動而得正，故「無咎」。
而天下隨時，
虞翻曰：乾為天，坤為下，震春兌秋。三四之正，坎冬離夏。四時位正，時行則行，故「天下隨時」矣。
隨之時義大矣哉。
蜀才曰：此本否卦。剛自上來居初，柔自初而升上，則內動而外說，是動而說隨也。相隨而大亨無咎。得於時也。得時，則天下隨之矣。故曰「隨之時義大矣哉」。

《象》曰：澤中有雷，隨。
《九家易》曰：兌澤震雷，八月之時。雷藏於澤，則天下隨時之象也。
君子以向晦入宴息。
崔元曰：晦者，冥也。雷者，陽氣，春夏用事。今在澤中，秋冬時也。故君子象之。日出視事，其將晦冥，退入宴寢而休息也。
侯果曰：坤為晦，乾之上九來入坤初，向晦者也。坤初升兌，兌為休息入宴者也。欲君民者，晦德息物，動說黎庶，則萬方歸隨也。

初九：官有渝，貞吉。出門交有功。
《九家易》曰：渝，變也。謂陽來居初，得正為震。震為子，得土之位故曰「官」也。陰陽出門，相與交通，陰往之上，亦不失正，故曰貞吉而交有功。
《象》曰：官有渝，從正吉也。出門交有功，不失也。
鄭玄曰：震為大塗，又為日門。當春分，陰陽之所交也。是臣出君門，與四方賢人交有成功之象也。昔舜慎徽五典，五典克從。納於百揆，百揆時序。賓於四門，四門穆穆。是其義也。

六二：系小子，失丈夫。
虞翻曰：應在巽。巽為繩，故稱系。小子謂五，兌為少，故曰「小子」。丈夫謂四，體大過老夫，故稱「丈夫」。承四隔三，故「失丈夫」。三至上，有大過象，故與老婦、士夫同義。體鹹象，夫死大過，故每有欲嫁之義也。
《象》曰：系小子，弗兼與也。
虞翻曰：已系於五，不兼與四也。

六三：系丈夫，失小子，隨有求，得。利居貞。
虞翻曰：隨家陰隨陽，三之上無應。上系於四，失初小子，故「系丈夫，失小子」艮為居、為求，謂求之正。得位遠應，利上承四，故「利居貞」矣。
《象》曰：系丈夫，誌舍下也。
王弼曰：雖體下卦，二已據初，將何所附，故舍初系四，誌在丈夫也。四俱無應，亦欲於已隨之，則得其求矣，故曰：「隨有求，得」也。應非其正，以系於人，何可以妄，故「利居貞」也。初處已下，四處已上，故曰「系丈夫，失小子」也。

九四：隨有獲，貞兇。有孚在道，以明，何咎。
虞翻曰：謂獲三也，失位相據，在大過，死象，故「貞兇」。《象》曰：「其義兇矣」。孚謂五。初震為道。三已之正，四變應初，得位在離。故「有孚在道，以明，何咎」。《象》曰：「明功也」。
《象》曰：隨有獲，其義兇也。
虞翻曰：死在大過，故「兇也」。
有孚在道，明功也。
虞翻曰：功謂五也。三四之正，離為明，故「明功也」。

九五：孚於嘉吉。
虞翻曰：坎為孚，陽稱嘉，位五正，故「吉」也。
《象》曰：孚於嘉，吉，位正中也。
虞翻曰：凡五言中正，中正皆陽得其正，以此為例矣。

上六：拘系之，乃從維之。
虞翻曰：應在艮。艮手為拘，巽為繩，兩系稱維，故「拘系之，乃從維之」。在隨之上，而無所隨，故維之。《象》曰「上窮」。是其義也。
王用亨於西山。
虞翻曰：否乾為王，謂五也。有觀象，故「亨」。兌為西，艮為山，故「王用亨於西山」也。
《象》曰：拘系之，上窮也。
虞翻曰：乘剛無應，故「上窮也」。""", chatGptTranslation = """**ChatGPT 翻译（工作译文）**

**《序卦》、卦辞：**《序卦》说：安乐而随从他人，必然会有所作为，所以接着是《随》卦。韩康伯说：顺应而行动，众人便会跟随。（震下兑上。）随卦：元始、亨通、适宜、守正，没有灾咎。虞翻认为，否卦上位的刚爻来到初位，刚居柔下，初、上两爻各得正位。郑玄说：震为行动，兑为喜悦；内在以德行发动，外在以言辞使人喜悦，天下人便会仰慕而跟从。既有人跟随，就应以善行引导，以礼通达，以义调和，以正道维系；具备这些德行便能成功获福，否则会有灾咎。焦赣以汉高祖与项籍为例证。

**《彖传》：**随，是刚爻来到下方而居于柔爻之下，内卦发动、外卦喜悦。虞翻以否卦乾上之刚来到坤初解释“刚来而下柔”。“大为亨通而守正，没有灾咎。”荀爽说：随是震宫归魂卦，震归从巽，故大通；发动之爻得正，故利于守正。阳降阴升本来容易有咎，行动得正便无咎。“天下随时”，虞翻以乾为天、坤为下，震为春、兑为秋，三四爻得正后坎为冬、离为夏，说明四时各正其位，天下随时而行。蜀才说，此卦本于否，刚自上来居初、柔自初升上，内动外悦；相随而大亨无咎，是得其时，得时则天下随之。

**《象传》：**泽中有雷，是随卦。《九家易》说，兑泽、震雷，雷藏泽中象征顺应秋冬时令。君子因此在天色将晚时回室安息。崔元说，雷属阳气，春夏用事；雷藏泽中则为秋冬之时，君子白日视事，日暮退寝休息。侯果以坤为晦、兑为休息，说明治理者应顺时收敛、使万物休养，并以德行和行动安抚百姓，使四方归附。

**六爻：**

**初九：** 官职有变，守正则吉；出门交往有功。《九家易》说“渝”是变动，阳爻来到初位得正为震，震为子；阴阳出门相交，所以守正吉而交往有功。郑玄以震为大道、日门，春分阴阳交会，象征臣子出君门与四方贤者交往；舜任官、宾接四方的事例正可说明。

**六二：** 系住年少者，便失去成年男子。虞翻说，巽为绳，故称“系”；小子指九五，丈夫指九四，六二承四而被六三隔开，故失丈夫。《象传》说不能两者兼得；已经系于九五，就不能同时系于九四。

**六三：** 系住成年男子，失去年少者；随从而有所求，可以得到；宜于安居守正。虞翻说，六三向上系于九四，便失去初九；艮为居、为求，得位远应而承四，所以利居贞。王弼说，六二已据初九，六三无处可附，故舍初系四；四也愿接受其随从，所以有求可得。但这种依附并非正当应合，不可妄为，故须守正。

**九四：** 随从而有所获得，守正反有凶险；若诚信在道、明察事理，又有什么过失？虞翻说，九四获得六三，却失位相据，且处大过死亡之象，故“贞凶”。“有孚”指九五，初九震为道；三四得正而成离明，所以诚信行道并能明察，便无咎。《象传》称“明功”，虞翻以九五为功、离为明解释。

**九五：** 对美善之事诚信，吉祥。虞翻说坎为孚，阳为嘉，九五居正故吉。《象传》说这是因为居位正而得中。

**上六：** 把它拘束起来，再用绳索维系。虞翻说艮为手、巽为绳，两重系缚称维；上六居随卦之极而无所随，所以被系缚。君王在西山举行祭享；虞翻以乾为王、兑为西、艮为山解释。《象传》说拘系是因为处于上极；虞翻说上六乘刚而无应，故已穷极。

**本条翻译状态：**本译文覆盖当前录入的《序卦》、卦辞、《彖传》《象传》、初九至上六及所录注文。卦变、爻位和象数解释均按各家说法分别转述，不视为唯一解释；尚未与四库影印页逐字校勘。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷五·蛊卦集解", "底本：维基文库《周易集解（李鼎祚）/卷五》，录入页面现存正文行200—292，覆盖蛊卦经文、彖传、象传、六爻及页面所载注文。原文中的引书、案语和疑难字形照录，尚未与影印本逐字校勘。译文尚未完成。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷五", """《序卦》曰：以喜隨人者，必有事，故受之以蠱。蠱者，事也。
《九家易》曰：子行父事，備物致用，而天下治也。備物致用，立成器以為天下利，莫大於聖人。子修聖道，行父之事，以臨天下，無為而治也。

(巽下艮上)。蠱：元亨。
虞翻曰：泰初之上，與隨旁通。剛上柔下，乾坤交，故「元亨」也。
伏曼容曰：蠱，惑亂也。萬事從惑而起，故以蠱為事也。
案：《尚書大傳》雲：乃命五史，以書五帝之蠱事。然為訓者，正以太古之時無為無事也。今言蠱者，是卦之惑亂也。時既漸澆，物情惑亂，故事業因之而起惑矣。故《左傳》雲：女惑男，風落山，謂之蠱，是其義也。
利涉大川。
虞翻曰：謂二失位，動而之坎也，故「利涉大川」也。
先甲三日，後甲三日。
《子夏傳》雲：先甲三日者，辛、壬、癸也。後甲三日者，乙、丙、丁也。
馬融曰：甲在東方，艮在東北，故雲「先甲」。巽在東南，故雲「後甲」。所以十日之中唯稱甲者。甲為十日之首，蠱為造事之端，故舉初而明事始也。言所以三日者，不令而誅謂之暴，故令先後各三日，欲使百姓遍習，行而不犯也。

《彖》曰：蠱，剛上而柔下，巽而止，蠱。
虞翻曰：泰初之上，故「剛上」；坤上之初，故「柔下」。上艮下巽，故「巽而止蠱」也。
蠱元亨，而天下治也。
荀爽曰：蠱者，巽也。巽歸合震，故「元亨」也。蠱者，事也。備物致用，故「天下治」也。
利涉大川，往有事也。
《九家易》曰：陽往據陰，陰來承陽，故「有事也」。此卦泰乾天有河，坤地有水，二爻升降，出入乾坤，利涉大川也。陽往求五，陰來求二，未得正位，戎事不息，故「有事」。
先甲三日，後甲三日，終則有始，天行也。
虞翻曰：謂初變成乾，乾為甲。至二成離，離為日。謂乾三爻在前，故「先甲三日」。賁，時也。變三至四，體離。至五成乾，乾三爻在後，故「後甲三日」。無妄，時也。易出震，消息歷乾坤象。乾為始，坤為終，故「終則有始」。乾為天。震為行，故「天行」也。

《象》曰：山下有風，蠱。
何妥曰：山者高而靜，風者宣而疾，有似君處上而安靜，臣在下而行令也。
君子以振民育德。
虞翻曰：君子，謂泰乾也。坤為民。初上撫坤，故「振民」。乾稱德，體大畜，須養，故以「育德」也。

初六：幹父之蠱，有子考，無咎。厲，終吉。
虞翻曰：幹，正。蠱，事也。泰乾為父，坤為事。故「幹父之蠱」。初上易位，艮為子，父死大過稱考，故「有子考」。變而得正，故「無咎。厲，終吉」也。
案：位陽令首，父之事也；爻陰柔順，子之質也。
《象》曰：幹父之蠱，意承考也。
王弼曰：幹事之首，時有損益，不可盡承，故意承而已也。

九二：幹母之蠱，不可貞。《象》曰：幹母之蠱，得中道也。
虞翻曰：應在五。泰坤為母，故幹母之蠱。失位。故「不可貞」。變而得正，故貞而「得中道也」。
案：位陰居內，母之象也。

九三：幹父之蠱，小有悔，無大咎。《象》曰：幹父之蠱，終無咎也。
王弼曰：以剛幹事，而無其應，故「有悔」也。履得其位，以正幹父，雖小有悔，終「無大咎」矣。
案：爻位俱陽，父之事。

六四：裕父之蠱，往見吝。
虞翻曰：裕，不能爭也。孔子曰：父有爭子，則身不陷於不義。四陰體大過，本末弱，故「裕父之蠱」。兌為見，變而失正，故「往見吝」。《象》曰：「往未得」。是其義也。
《象》曰：裕父之蠱，往未得也。
虞翻曰：往失位，折鼎足，故「未得」。

六五：幹父之蠱，用譽。
荀爽曰：體和應中，承陽有實，用斯幹事，榮譽之道也。
《象》曰：幹父用譽，承以德也。
虞翻曰：譽，謂二也。二五失位，變而得正，故「用譽」。變二使承五，故承以德。二乾爻，故稱「德」矣。

上九：不事王侯，
虞翻曰：泰乾為王，坤為事。應在於三。震為侯，坤象不見，故「不事王侯」。
高尚其事。
虞翻曰：謂五已變，巽為高，艮陽升在坤上，故「高尚其事」。
《象》曰：不事王侯，誌可則也。
荀爽曰：年老事終，不當其位。體艮為止。故「不事王侯」。據上臨下，重陰累實，故「誌可則」。""", chatGptTranslation = """**ChatGPT 翻译（工作译文）**

**《序卦》、卦辞：**《序卦》说：以喜悦的态度随从他人，必然会有事情发生，所以接着是《蛊》卦；蛊就是事务。《九家易》说，儿子承行父业，备齐器物并使其发挥用途，天下便可治理；圣人制作器具以利天下，儿子修习圣道、承行父事，也可无为而治。（巽下艮上。）蛊卦：元始而亨通。虞翻说，此卦由泰卦初爻上升至上位而来，与随卦旁通；刚上柔下、乾坤相交，故元亨。伏曼容说，蛊是迷惑混乱，万事往往由惑乱而起。案语引《尚书大传》说，五史曾奉命记载五帝治理事务的事迹；太古本无为无事，后世时俗渐衰、人情惑乱，事务由此产生。《左传》所谓女子惑男、风落山木称为蛊，也是此义。

“利涉大川”，虞翻说是九二失位，变动而成坎象。“先甲三日，后甲三日”，《子夏传》说先甲为辛、壬、癸，后甲为乙、丙、丁。马融说甲在东方、艮在东北，所以称先甲；巽在东南，所以称后甲。甲为十干之首，蛊为造事之端，举甲以明事始。前后各留三日，是为了先行告令，使百姓熟悉，避免不告而诛的暴虐。

**《彖传》：**蛊是刚爻居上、柔爻居下，内巽顺而外艮止。虞翻以泰卦初爻上升、坤上爻下降解释刚上柔下。蛊卦元亨，天下因此得治。荀爽说，巽归合于震，所以元亨；蛊为事务，备物致用，所以天下治。“利涉大川，是前往而有事。”《九家易》说阳往据阴、阴来承阳，故有事；卦中乾天有河、坤地有水，二爻升降出入乾坤，所以利涉大川；阳往求五、阴来求二，尚未得正位，事务未息。虞翻说，初爻变成乾，乾为甲；至二成离，离为日，乾三爻在前，故先甲三日。三至四体离，至五成乾，乾三爻在后，故后甲三日。乾为始、坤为终，终而复始；乾为天、震为行，所以这是天道运行。

**《象传》：**山下有风，是蛊卦。何妥说，山高而静，风传播迅疾，像君主居上安静、臣子居下执行政令。君子因此振作百姓、培育德行。虞翻说君子指泰卦乾体，坤为民；初、上抚坤，所以振民。乾象征德，卦体有大畜之象，大畜有蓄养之义，所以育德。

**六爻：**

**初六：** 整治父亲留下的事务；有儿子承当父业，父亲无咎。虽有危险，最终吉祥。虞翻说“干”是匡正，“蛊”是事务；乾为父、坤为事，初上易位后艮为子，父死称考，变而得正，所以无咎而终吉。案语说阳位居首象征父业，阴柔象征儿子的资质。王弼说，整治事务之初有损益变化，不可全盘承袭，只须心意承继先父。

**九二：** 整治母亲的事务，不可固执守定。《象传》说这是把握中道。虞翻说泰卦坤为母，九二失位故不可贞；变而得正便能得中道。案语以其居内阴位取母象。

**九三：** 整治父亲的事务，略有悔恨，没有大的灾咎。《象传》说最终无咎。王弼说以刚健整治事务却没有相应，故有悔；但九三居正，能正当地整治父业，终无大咎。案语说爻与位皆阳，象征父事。

**六四：** 宽缓地处理父亲的事务，继续前往会遇到困难。虞翻说“裕”是不能争辩劝谏；孔子说父亲若有争谏之子，便不会陷入不义。六四阴柔，处大过之象，本末皆弱，所以说裕父之蛊；变而失正，故往则见吝。《象传》说前往未能成功。虞翻又以失位、折鼎足解释“未得”。

**六五：** 整治父亲的事务，因而获得称誉。荀爽说卦体和顺、与中位相应，承接阳爻而有实，故能以此整治事务并获得荣誉。《象传》说承以德。虞翻说“誉”指九二，二、五变而得正，九二承五，乾爻故称德。

**上九：** 不侍奉王侯，而崇尚自己的事业。虞翻说乾为王、坤为事，上九应三；震为诸侯，坤象不见，所以不事王侯。巽为高，艮阳上升于坤上，所以高尚其事。《象传》说不侍奉王侯，其志向可以作为法则。荀爽说年老而任事已终，不再处于其位；艮为止，故不事王侯；居上临下、重阴累实，其志可为法则。

**本条翻译状态：**本译文覆盖当前录入的《序卦》、卦辞、《彖传》《象传》、初六至上九及所录注文。“蛊”“干”“考”“裕”等词依注家训释分别转述；卦变说法不作唯一解释。尚未与四库影印页逐字校勘。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷五·临卦集解", "底本：维基文库《周易集解（李鼎祚）/卷五》，录入页面现存正文行293—364，覆盖临卦经文、彖传、象传、六爻及页面所载注文。原文异体字、术语及注文署名依页面照录，尚未与影印本逐字校勘。译文尚未完成。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷五", """《序卦》曰：有事而後可大，故受之以臨。臨者，大也。
崔覲曰：有蠱元亨，則可大之業成。故曰「有事然後可大」也。

(兌下坤上)。臨：元、亨、利、貞。
虞翻曰：陽息至二，與遁旁通。剛浸而長，乾來交坤，動則成乾，故「元、亨、利、貞」。
至於八月有兇。
虞翻曰：與遁旁通，臨消於遁，六月卦也。於周為八月。遁弒君父，故「至於八月有兇」。荀公以兌為八月，兌於周為十月。言八月，失之甚矣。
鄭玄曰：臨，大也。陽氣自此浸而長大，陽浸長矣。而有四德，齊功於乾，盛之極也。人之情盛則奢淫，奢淫則將亡，故戒以兇。臨卦鬥建醜而用事，殷之正月也。當文王之時，紂為無道，故於是卦為殷家著興衰之戒，以見周改殷正之數雲。臨自周二月用事，訖其七月，至八月而遁卦受之，此終而復始，王命然矣。

《彖》曰：臨，剛浸而長。
虞翻曰：剛，謂二也。兌為水澤，自下浸上，故「浸而長」也。
說而順，剛中而應。大亨以正，天之道也。
虞翻曰：說，兌。順，坤也。剛中，謂二也。四陰皆應之，故曰「而應」。大享以正，謂三動成乾。天得正為泰，天地交通。故「亨以正，天之道也」。
至於八月有兇，消不久也。
蜀才曰：此本坤卦。剛長而柔消，故「大亨利正」也。
案：臨十二月卦也。自建醜之月，至建申之月，凡歷八月，則成否也否則天地不交，萬物不通，是至於八月有兇，斯之謂也。

《象》曰：澤上有地，臨。
荀爽曰：澤卑地高，高下相臨之象也。
君子以教思無窮，容保民無疆。
虞翻曰：君子，謂二也。震為言，兌口講習。學以聚之，問以辯之。坤為思，剛浸長，故「以教思無窮」。容，寬也。二寬以居之，仁以行之；坤為容，為民，故「保民無疆」矣。

初九：鹹臨，貞吉。
虞翻曰：鹹，感也。得正應四，故「貞吉」也。
《象》曰：鹹臨貞吉，誌行正也。
荀爽曰：陽始鹹升，以剛臨柔。得其正位而居，是吉。故曰：「誌行正」。

九二：鹹臨，吉，無不利。
虞翻曰：得中多譽。兼有四陰。體復初。元吉。故「無不利」。
《象》曰：鹹臨吉，無不利，未順命也。
荀爽曰：陽感至二，當升居五，群陰相承，故「無不利」也。陽當居五，陰當順從，今尚在二，故曰「未順命也」。

六三：甘臨，無攸利，既憂之，無咎。《象》曰：甘臨，位不當也。既憂之，咎不長也。
虞翻曰：兌為口，坤為土，土爰稼穡作甘。兌口銜坤，故曰「甘臨」。失位乘陽，故「無攸利」。言三失位無應，故「憂之」。動而成泰，故「咎不可長也」。

六四：至臨，無咎。
虞翻曰：至，下也。謂下至初，應當位，有實，故「無咎」。
《象》曰：至臨無咎，位當也。
荀爽曰：四與二同功，欲升二至五，已得承順之，故曰「至臨」也。陽雖未乘，處位居正，故得「無咎」。是位當也。

六五：知臨，大君之宜，吉。《象》曰：大君之宜，行中之謂也。
荀爽曰：五者，帝位。大君，謂二也。宜升上居五位，吉。故曰「知臨，大君之宜」也。二者處中，行升居五，五亦處中，故曰「行中之謂也」。

上六：敦臨，吉，無咎。
荀爽曰：上應於三，欲因三升二，過應於陽，敦厚之意，故曰：「敦臨吉無咎」。
《象》曰：敦臨之吉，誌在內也。
《九家易》曰：誌在升二也。陰以陽為主。故誌在內也。""", chatGptTranslation = """**ChatGPT 翻译（工作译文）**

**《序卦》与卦辞：**《序卦》说：有了事务，然后才能发展壮大，所以接着是《临》卦；临就是大。崔觐说，蛊卦元亨，事业由此得以发展，所以说有事之后才能壮大。（兑下坤上。）临卦：元始、亨通、适宜、守正。虞翻说阳气增长到第二爻，与遁卦旁通；刚阳渐渐增长，乾来与坤交合，发动便形成乾，所以具备元亨利贞。至于八月会有凶险。虞翻说临与遁旁通，临卦消退而成为遁；临是六月卦，在周历为八月。遁象征弑害君父，所以说八月有凶。荀公以兑为八月，但兑在周历应为十月，把它说成八月，错误很大。郑玄说，临就是大，阳气从此渐渐增长，达到极盛；人情盛极便奢侈放纵，奢淫则将趋于灭亡，所以用凶险作警戒。临卦在斗建丑月当令，即殷历正月；文王时纣王无道，故借此卦向殷家显示兴衰之戒，也显示周代改用殷正的历数。按周历，临从二月当令直到七月，八月由遁卦承接，终而复始，王命亦如此运行。

**《彖传》：**临，是刚阳渐渐增长。虞翻说刚指九二；兑为水泽，水从下向上浸润，所以称“浸而长”。“喜悦而顺，刚健居中并有所应；大为亨通而合乎正道，是天道。”虞翻说兑为悦、坤为顺，刚中指九二，四个阴爻都与之相应。大亨以正，是九三发动而形成乾；天得正而成泰，天地交通，所以这是天道。至于八月有凶，是因为阳气消退不会持续太久。蜀才说此卦本于坤，刚阳增长而柔阴消退，所以大亨而利于守正。案语说临是十二月卦，从建丑之月到建申之月共历八个月，便成为否卦；否则天地不交、万物不通，这就是八月有凶的意思。

**《象传》：**泽上有地，是临卦。荀爽说泽低地高，高下相临。君子因此教化百姓的思虑与方法没有穷尽，包容并保护百姓没有疆界。虞翻说君子指九二；震为言语，兑为口，讲习学习、通过问答辨析；坤为思，阳气渐长，所以教化思虑无穷。容是宽厚；坤为容、为民，所以能保民无疆。

**六爻：**

**初九：** 以感应之道临下，守正吉祥。虞翻说“咸”是感，初九得正并与六四相应，所以贞吉。《象传》说志向所行合乎正道。荀爽说阳气开始感应上升，以刚临柔，得正位而居，所以吉。

**九二：** 以感应之道临下，吉祥，无所不利。虞翻说九二得中而有声誉，又兼与四阴相应，卦体有复卦初爻之象，所以无不利。《象传》说吉而无不利，是因为尚未顺从天命。荀爽说阳气感应至二，本应上升居五，众阴承接，所以无不利；阳应居五、阴应顺从，但阳尚居二，因此说未顺命。

**六三：** 以甘言临人，没有什么利益；若已经忧惧并加以反省，则无咎。《象传》说甘言临人，是因为位置不当；既然忧惧，灾咎便不会长久。虞翻说兑为口、坤为土，土能稼穑而味甘，兑口含坤土，所以称“甘临”。六三失位而乘阳，所以无所利；它没有正应，故应忧惧；发动而成泰，灾咎便不会延长。

**六四：** 亲自来到临近之处，没有灾咎。虞翻说“至”是向下，指下至初九；相应且位置得当，所以无咎。《象传》说无咎是因为位置适当。荀爽说六四与九二同功，九二欲升至五位，六四已经得以承顺，所以称“至临”；阳爻虽尚未乘居其位，但六四居正，因此无咎。

**六五：** 以智慧临民，是大君应有的做法，吉祥。《象传》说大君应有的做法，就是行事合乎中道。荀爽说五是帝位，大君指九二；九二适宜升居五位，所以称“知临，大君之宜”。九二处中，升至五位后仍居中，因此称行中。

**上六：** 以敦厚之道临下，吉祥，没有灾咎。荀爽说上六与六三相应，想借六三上升九二；它越过原应之位而应阳，表现出敦厚之意。《象传》说其吉祥，是因为志向在内。《九家易》说志向在于使九二上升；阴以阳为主，所以志在内。

**本条翻译状态：**本译文覆盖当前录入的《序卦》、卦辞、《彖传》《象传》、初九至上六及所录注文。月份、卦气和卦变解释保留郑玄、虞翻、荀爽等家的不同系统；“八月”历法解释尤其存在分歧，未作统一裁断。尚未与四库影印页逐字校勘。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷五·观卦集解", "底本：维基文库《周易集解（李鼎祚）/卷五》，录入页面现存正文行366—453，覆盖观卦经文、彖传、象传、六爻及页面所载注文。异体字、案语、引文及疑难字形照录，尚未与影印本逐字校勘。译文尚未完成。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷五", """《序卦》曰：物大然後可觀也。故受之以觀。
崔覲曰：言德業大者，可以觀政於人，故受之以觀也。

(坤下巽上)。觀：盥而不薦，有孚颙若。
鄭玄曰：坤為地，為眾。巽為木，為風。九五，天子之爻。互體有艮。艮為鬼門，又為宮闕。地上有木。而為鬼門宮闕者，天子宗廟之象也。
王弼曰：王道之可觀者，莫盛乎宗廟。宗廟之可觀者，莫盛乎盥也。至薦簡略，不足復觀，故「觀盥而不薦」也。
馬融曰：盥者，進爵灌地，以降神也。此是祭祀盛時，及神降薦牲，其禮簡略，不足允也。國之大事，唯祀與戎。王道可觀，在於祭祀。祭祀之盛，莫過初盥降神。故孔子曰：諦自既灌而往者，吾不欲觀之矣。此言及薦簡略，則不足觀也。以下觀上，見其至盛之禮，萬民信敬，故雲「有孚颙若」。孚，信。颙，敬也。
案：鬼神害盈，禍淫福善。若人君修德，至誠感神。信則黍稷非馨，明德惟馨。故「觀盥而不觀薦」，饗其誠信者也。斯即東鄰殺牛，不如西鄰之禴祭，實受其福，是其義也。

《彖》曰：大觀在上，
蜀才曰：此本乾卦。案：柔小浸長，剛大在上，其德可觀，故曰「大觀在上」也。
順而巽，中正以觀天下。
虞翻曰：謂陽息臨二，直方大。臨者，大也。在觀上，故稱「大觀」。順，坤也。中正謂五。五以天神道觀示天下，鹹服其化，賓於王庭。
觀，盥而不薦，有孚颙若，下觀而化也。
虞翻曰：觀，反臨也。以五陽觀示坤民，故稱「觀」。盥，沃盥。薦，羞牲也。孚，信，謂五。颙颙，君德，有威容貌。若，順也。坎為水，坤為器，艮手臨坤，坎水沃之，盥之象也。故「觀盥而不薦」。孔子曰：諦自既灌，吾不欲觀之矣。巽為進退，容止可觀，進退可度，則下觀其德而順其化。上之三，五在坎中，故「有孚颙若，下觀而化」。《詩》曰：颙颙卬卬，如珪如璋。君德之義也。
觀天之神道，而四時不忒。
虞翻曰：忒，差也。神道謂五。臨，震兌為春秋，三上易位，坎冬離夏，日月象正。故「四時不忒」。
聖人神道設教，而天下服矣。
虞翻曰：聖人謂乾，退藏於密，而齊於巽，以神明其德教，故「聖人設教」，坤民順從，而天下服矣。

《象》曰：風行地上，觀。先王以省方觀民設教。
《九家易》曰：先王謂五。應天順民，受命之王也。風行地上，草木必偃，枯槁朽腐，獨不從風，謂應外之爻。天地氣絕，陰陽所去，象不化之民，五刑所加，故以省察四方，觀視民俗，而設其教也。言先王德化，光被四表，有不賓之民，不從法令，以五刑加之，以齊德教也。

初六：童觀，小人無咎，君子吝。
虞翻曰：艮為童。陰，小人。陽，君子。初位賤，以小人承君子，故「無咎」。陽伏陰下，故「君子吝」矣。
《象》曰：初六童觀，小人道也。
王弼曰：失位處下，最遠朝美，無所鑒見，故曰「童觀」。處大觀之時，而童觀趣順而已。小人為之，無可咎責。君子為之，鄙吝之道。

六二：窺觀，利女貞。
虞翻曰：臨兌為女。竊觀稱窺兌女反成巽。巽四五得正，故「利女貞」。艮為宮室，坤為闔戶，小人而應五。故「窺觀女貞利」，不淫視也。
《象》曰：窺觀女貞，亦可醜也。
侯果曰：得位居中，上應於五。窺觀朝美，不能大觀。處大觀之時，而為窺觀。女正則利，君子則醜也。
案：六二離爻，離為目，又為中女。外互體艮，艮為門。闕女目近門，窺觀之象也。

六三：觀我生，進退。
虞翻曰：坤為我，臨震為生。生，謂坤生民也；巽為進退，故「觀我生進退」。臨震進之五，得正居中，故《象》曰「未失道」。
《象》曰：觀我生進退，未失道也。
荀爽曰：我，謂五也。生者，教化生也。三欲進觀於五，四既在前，而三故退，未失道也。

六四：觀國之光，利用賓於王。
虞翻曰：坤為國。臨陽至二，天下文明。反上成觀，進顯天位，故「觀國之光」。王謂五陽。陽尊賓坤。坤為用、為臣，四在王庭，賓事於五。故「利用賓於王」矣。《詩》曰：莫敢不來賓，莫敢不來王。是其義也。
《象》曰：觀國之光，尚賓也。
崔覲曰：得位比尊，承於王者。職在搜揚國俊，賓薦王庭，故以進賢為尚賓也。

九五：觀我生，君子無咎。
虞翻曰：我，身也，謂我生。生，謂生民。震生象。反坤，為死喪。嫌非生民，故不言民。陽為君子，在臨二，失位之五，得道處中，故「君子無咎」矣。
《象》曰：觀我生，觀民也。
王弼曰：觀我生，自觀其道也。為眾觀之主，當宣文化，光於四表，上之化下，猶風之靡草。百姓有過，在予一人。君子風著，已乃無咎。欲察已道，當觀民也。
虞翻曰：坤為民，謂三也。坤體成，故觀民也。

上九：觀其生，君子無咎。
虞翻曰：應在三，三體臨震，故「觀其生」。君子謂三，之三得正，故「無咎」矣。
《象》曰：觀其生，誌未平也。
王弼曰：觀其生，為人所觀也。最處上，極天下所觀者也。處天下所觀之地，其誌未為平易，不可不慎。故君子德見，乃得無咎。生，猶動出也。
虞翻曰：坎為誌，為平，上來之三，故「誌未平」矣。""", chatGptTranslation = """**ChatGPT 翻译（工作译文）**

**《序卦》与卦辞：**《序卦》说：事物发展壮大之后，才值得观览，所以接着是《观》卦。崔觐说，德行与功业宏大，便可以让人观察其政事，所以称观。（坤下巽上。）观卦：祭祀开始时洗手灌酒，却不继续进献牲品；内心诚信，仪容庄敬。郑玄说坤为地、为众，巽为木、为风；九五是天子之爻，互体有艮，艮为鬼门、宫阙，地上有木而又有门阙，象征天子的宗庙。王弼说王道最值得观览之处，没有比宗庙更盛大的；宗庙礼仪中最值得观览的是灌祭。到了进献牲品时，礼仪已趋简略，不再足以观赏，所以经文说“观盥而不荐”。马融解释，盥是进爵灌地以迎降神灵；祭祀最隆重的阶段在灌祭降神，随后进献牲品的礼节相对简略。国家大事只有祭祀与战争，王道值得观览之处在祭祀，而祭祀最盛大处莫过于初始盥灌降神。孔子说：“从灌祭以后，我就不想再看了。”经文因此强调观其盥而不观其荐，使万民信服敬仰。“孚”是诚信，“颙”是敬肃。案语说，鬼神惩罚盈满骄纵者，降祸于淫邪者、赐福于善者；君主修德，以至诚感神，重要的是诚信而非祭品丰盛。故观盥而不观荐，是享用其诚信；正如东邻杀牛，不如西邻以薄祭而实受其福。

**《彖传》：**伟大的观示居于上位。蜀才说此卦本于乾，柔小渐长而刚大居上，其德可供观仰，所以称“大观在上”。“柔顺而巽入，居中守正，以此观示天下。”虞翻说阳气增长至临卦九二，临有大义；在观卦之上，所以称“大观”。顺是坤，中正指九五。九五以天道神明观示天下，众人服从其教化，来到王庭朝见。“观盥而不荐，有诚信而仪容庄敬，是下民观仰而受教化。”虞翻说观卦与临卦相反；九五以阳爻观示坤民，所以称观。盥是沃洗，荐是进献牲品；孚指九五的诚信，颙颙是君主有威仪的容貌，若是顺从。坎为水、坤为器、艮为手，坎水由艮手沃于坤器，构成盥洗之象。巽为进退，举止容貌值得观仰，进退有法度，下民便观察其德而顺从教化。引《诗》“颙颙卬卬，如珪如璋”，说明君德庄严。“观察天的神妙之道，四时便不会差错。”虞翻说忒是差错；神道指九五。临卦震、兑象征春秋，三、上爻交换后坎、离象征冬夏，日月之象得正，所以四时不差。“圣人以神妙之道设立教化，天下便会服从。”虞翻说圣人象征乾，退藏于密而齐于巽，以神明其德教，坤民顺从，天下便服。

**《象传》：**风行于地上，是观卦。先王因此巡视四方、观察民情并设立教化。《九家易》说先王指九五，是顺应天命、顺从民意而受命的君王。风行地上，草木都会随风伏倒；枯槁腐朽之物却不随风，象征不接受教化的百姓。先王因此巡视四方、观察风俗并施行教化；对不服从法令者，也须依法整齐治理。

**六爻：**

**初六：** 幼稚地观看；小人如此没有灾咎，君子如此则可耻。《象传》说这是小人之道。虞翻说艮为童，阴为小人、阳为君子；初位卑下，以小人身份承接君子尚可无咎，若君子处此则阳伏阴下，故可羞。王弼说初六失位居下，离朝廷之美最远，无从鉴察，所以是童观；小人如此不必责备，君子如此便是鄙陋。

**六二：** 从门缝窥看，适宜女子守正。虞翻说临卦兑为女，偷偷观看称窥；兑女反而成巽，巽的四、五爻得正，所以利女贞。艮为宫室、坤为闭户，小人而应九五，故是女子窥观而不作淫邪之视。《象传》说窥观虽守女贞，仍然可羞。侯果说六二得位居中，上应九五，却只能窥见朝廷之美，不能作全面观览；在大观之时仅作窥观，女子守正尚可，君子如此则可耻。案语以六二离为目、为中女，外互艮为门，说明女子近门窥看的象。

**六三：** 观察自己的生养、教化成果，并据此决定进退。虞翻说坤为我，临卦震为生；生指坤所生养的百姓，巽为进退。临卦震进至五位并得正居中，所以《象传》说没有失道。荀爽说“我”指九五，“生”指教化所产生的成果；六三想进而观察九五，但六四已经在前，所以六三退后，仍未失道。

**六四：** 观察国家的光明政绩，适宜作为宾客参与王朝事务。虞翻说坤为国，临卦阳气增长至二，天下文明；反转而成观，显现天位，所以观国之光。王指九五阳爻，阳尊而宾礼于坤；坤为臣，六四处王庭，以宾礼事奉九五。引《诗》说没有人敢不来宾、不来王。崔觐说六四得位而亲近尊者，承事君王，职责在于搜求举荐国家贤俊，使贤者入朝，因此以举贤宾王为尚。

**九五：** 观察自己的生养教化，君子没有灾咎。《象传》说观察自己的生养教化，就是观察百姓。虞翻说“我”是自身，“生”指所生养的百姓；震有生育之象，避免与坤的死丧之象混淆，所以经文不直接称民。九五阳刚为君子，从临卦二位变来，得道居中，故无咎。王弼说观我生是自我审视其道；九五为众人观仰的主宰，应宣扬文教、光被四方。上位教化下位，如风吹草伏；百姓有过，君主应反省自身。君子之风行于天下，自己才可无咎。要察看自己的治理之道，就应观察百姓。虞翻又以坤为民，指六三；坤体形成，所以说观民。

**上九：** 观察他人的生养教化，君子没有灾咎。虞翻说上九与六三相应，六三有临卦震象，所以说观其生；六三变而得正，故无咎。《象传》说观察他人的生养教化，志向尚未平定。王弼说“观其生”是成为他人所观仰的对象；上九处于最上，是天下人观仰之处，志向不能自以为平易，必须谨慎。君子的德行显现，才得无咎。“生”犹如行为和显现。虞翻说坎为志、为平，上九来到六三，所以志未平。

**本条翻译状态：**本译文覆盖当前录入的《序卦》、卦辞、《彖传》《象传》、初六至上九及所录注文。宗庙礼仪、盥灌祭礼及“观我生”等词按本条注家解释转述；注文中的卦象推演仍需与不同版本和注本对读。尚未与四库影印页逐字校勘。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷五·噬嗑卦集解", "底本：维基文库《周易集解（李鼎祚）/卷五》，录入页面现存正文行454—534，覆盖噬嗑卦经文、彖传、象传、六爻及页面所载注文。经文、注文和案语照底本录入；部分字形疑似版本异文，尚未逐字校勘。译文尚未完成。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷五", """《序卦》曰：可觀而後有所合，故受之以噬嗑。嗑者，合也。
崔覲曰：言可觀政於人，則有所合於刑矣，故曰「可觀而有所合」。

(震下離上)。噬嗑：享，利用獄。
虞翻曰：否五之坤初，坤初之五，剛柔交，故「亨」也。坎為獄，艮為手，離為明，四以不正，而系於獄。上當之三，蔽四成豐，折獄致刑，故「利用獄」。坤為用也。
案：頤中有物曰噬嗑，謂九四也。四互坎體，坎為法律，又為刑獄，四在頤中，齧而後亨。故「利用獄」也。

《彖》曰：頤中有物，曰噬嗑。
虞翻曰：物謂四，則所噬幹脯也。頤中無物，則口不噬。故先舉「頤中有物曰噬嗑」也。
噬嗑而亨。
崔覲曰：物在頤中，隔其上下，因齧而合，「乃得其亨焉。」以喻人於上下之間，有亂群者，當用刑去之，故言「利用獄」。
剛柔分，動而明，雷電合而章。
盧氏曰：此本否卦。乾之九五，分降坤初；坤之初六，分升乾五。是剛柔分也。分則雷動於下，電照於上，合成天威，故曰雷電合而成章也。
柔得中而上行，雖不當位，利用獄也。
侯果曰：坤之初六，上升乾五，是柔得中而上行。雖則失位，文明以中，斷制枉直，不失情理，故「利用獄」。

《象》曰：雷電，噬嗑。
宋衷曰：雷動而威，電動而明，二者合而其道章也。用刑之道，威明相兼。若威而不明，恐致淫濫；明而無威，不能伏物。故須雷電並合，而噬嗑備。
先王以明罰敕法。
侯果曰：雷所以動物，電所以照物。雷電震照，則萬物不能懷邪。故先王則之，明罰敕法，以示萬物，欲萬方一心也。

初九：屨校滅趾，無咎。
虞翻曰：屨，貫。趾，足也。震為足，坎為校；震沒坎下，故「屨校滅趾」。初位得正，故「無咎」。
幹寶曰：「趾，足也，屨校，貫械也。初居剛躁之家，體貪狠之性，以震掩巽，強暴之男也。行侵陵之罪，以陷屨校之刑，故曰「屨校滅趾」。得位於初，顧震知懼，小徵大戒，以免刑戮，故曰「無咎」矣。
《象》曰：屨校滅趾，不行也。
虞翻曰：否坤小人。以陰消陽，其亡其亡。故五變滅初，否坤殺不行也。幹寶曰：不敢遂行強也。

六二：噬膚滅鼻，無咎。
虞翻曰：噬，食也。艮為膚，為鼻；鼻沒坎水中，隱藏不見，故「噬膚滅鼻」。乘剛，又得正多譽，故「無咎」。
《象》曰：噬膚滅鼻，乘剛也。
侯果曰：居中履正，用刑者也。二互體艮。艮為鼻，「又為黔喙，噬膚滅鼻之象也。乘剛，噬必深，噬過其分，故「滅鼻也」。刑刻雖峻，得所疾也。雖則滅鼻，而無咎矣。

六三：噬昔肉，遇毒，小吝，無咎。
虞翻曰：三在膚裏，故稱肉。離日之，為昔；坎為毒，故「噬昔肉遇毒」。毒謂矢毒也。失位承四，故「小吝」。與上易位，利用獄成豐，故「無咎」也。
《象》曰：遇毒，位不當也。
荀爽曰：昔肉，謂四也。三以不正，噬取異家，法當遇罪，故曰「遇毒」。為艮所止，所欲不得，故「小吝」也。所欲不得，則免於罪，故「無咎」矣。

九四：噬幹胏，得金矢，利艱貞，吉。《象》曰：利艱貞吉，未光也。
陸績曰：肉有骨，謂之胏。離為幹肉，又為兵矢。失位用刑，物亦不服。若噬有骨之幹胏也。金矢者，取其剛直也。噬胏雖復艱難，終得申其剛直，雖獲正吉，未為光大也。

六五：噬幹肉，得黃金，貞厲，無咎。
虞翻曰：陰稱肉，位當離，日中烈，故幹肉也。幹金黃，故「得黃金」。貞，正。厲，危也。變而得正，故「無咎」。
王弼曰：幹肉，堅也。黃，中也。金，剛也。以陰處陽，以柔承剛，以噬於物，物亦不服，故曰「噬幹肉也」。然處得尊位，而居於中，能行其戮者也。履不正而能行其戮，剛勝者也。噬雖不服，得中而勝，故曰「噬幹肉得黃金」也。已雖不正，而刑戮得當，故雖貞厲而無咎也。
《象》曰：貞厲無咎，得當也。
荀爽曰：謂陰來正居，是而厲陽也。以陰厲陽，正居其處。而無咎者，以從下升上，不失其中。所言得當。

上九：何校滅耳，兇。
荀爽曰：為五所何，故曰「何校」。據五應三，欲盡滅坎。上體坎，為耳。故曰「滅耳兇」。上以不正，侵欲無已，奪取異家，惡積而不可掩，罪大而不可解，故「宜兇」矣。
鄭玄曰：離為槁木；坎為耳，木在耳上，「何校滅耳」之象也。
《象》曰：何校滅耳，聰不明也。
《九家易》曰：當據離坎，以為聰明。坎既不正，今欲滅之。故曰「聰不明也」。""", chatGptTranslation = """**ChatGPT 翻译（工作译文）**

**《序卦》与卦辞：**《序卦》说：能够观览，然后才会有所结合，所以接着是《噬嗑》卦。“嗑”就是合。崔觐说，观察政事之后，便会涉及刑罚，所以说观而后有所合。（震下离上。）噬嗑卦：亨通，适宜使用刑狱。虞翻说，卦由否卦变化，刚柔交合所以亨通；坎为狱、艮为手、离为明，九四不正而系于狱，上爻应当来到三位，遮蔽九四而成丰卦，折狱用刑，所以利于用狱。案语说，颐卦口中有物就是噬嗑，所指为九四；九四互体有坎，坎为法律和刑狱，九四在颐口之中，咬断之后才能通畅。

**《彖传》：**颐口之中有物，称为噬嗑。虞翻说物指九四，是口中所咬的干肉；口中没有东西便无从咀嚼，所以先说明颐中有物。咀嚼之后便能亨通。崔觐说物在口中隔开上下，咬断后才能重新合拢而通畅；这用来比喻上下之间有扰乱群体的人，应当施以刑罚除去，所以说利于用狱。刚柔分开，行动而光明，雷与电相合而彰显。卢氏说此卦本于否，乾九五下降至坤初，坤初六上升至乾五，所以刚柔分；分后雷在下发动、电在上照耀，合成天威。柔爻得中而上行，虽然不居正位，仍利于用狱。侯果说坤初六升到乾五，柔得中而上行；虽失位，但以文明居中，能够辨断曲直而不失情理，因此利于用狱。

**《象传》：**雷与电并现，是噬嗑卦。宋衷说雷发动而有威势，电照耀而明察，两者相合，威明之道才彰显。用刑必须威严与明察兼备：只有威严而不明察，容易造成滥刑；只有明察而无威严，则不能使人服从。先王因此明示刑罚、整饬法令。侯果说雷使万物震动，电使万物显明；雷电震动照耀，万物便不能藏匿邪恶。先王效法这一点，明定刑罚、整饬法令，使天下归于一心。

**六爻：**

**初九：** 脚上戴着刑具，遮蔽脚趾，没有灾咎。虞翻说“屨”是贯穿，“趾”是足；震为足、坎为刑具，震没于坎下，所以有此象；初九得正，故无咎。干宝说，屨校就是贯械；初九刚躁贪狠，行为侵凌而触犯刑罚。因其处初位，见到震象而知惧，小惩以大戒，得以免于更重刑罚，所以无咎。《象传》说遮蔽脚趾，是不再继续前行。虞翻以否卦坤为小人、阴消阳解释其不敢继续；干宝说是不敢再逞强妄行。

**六二：** 咬到柔嫩的肉，深及鼻部，没有灾咎。虞翻说噬是咀嚼，艮为肤、为鼻，鼻没入坎水而隐蔽，所以有此象；六二乘刚而得正，故无咎。《象传》说咬到柔肉而深及鼻，是因为乘在刚爻之上。侯果说六二居中履正，是施刑者；互体艮为鼻，咬肉太深便超过分寸，故有灭鼻之象。刑罚虽严峻，但针对的是应当惩治的对象，所以仍无咎。

**六三：** 咬到陈旧的肉，遇到毒物；略有困难，但没有灾咎。虞翻说六三处在柔肉之内，所以称肉；离为日，取“昔”义，坎为毒，故有此象；毒指箭毒。六三失位而承九四，所以小吝；与上爻交换位置而成丰，利于用狱，所以无咎。荀爽说“昔肉”指九四；六三不正而取用他人之物，依法应遇罪，所以称遇毒；受到艮阻，所求不得，故小吝；所求不得反而免罪，所以无咎。

**九四：** 咬到带骨的干肉，得到金属箭头；在艰难中守正则吉。《象传》说艰难守正而吉，是因为其德尚未光大。陆绩说带骨的肉称胏；离为干肉，也为兵器、箭矢。九四失位而用刑，受刑者未必服从，如同咬带骨干肉。金矢取其刚直之义；虽艰难，最终能伸张刚直，虽得正而吉，功业仍未光大。

**六五：** 咬到干肉，得到黄金；守正而知危，便没有灾咎。虞翻说阴爻称肉，六五处离位，离为日中炎烈，所以是干肉；干为金、黄，所以称得黄金；贞是正，厉是危险，变动得正便无咎。王弼说干肉坚硬，黄象征中，金象征刚。六五以阴居阳、以柔承刚，施刑时对方不服；但它居尊位而得中，能够施行刑罚，以中道胜过对方，所以称咬干肉而得黄金。虽然位置原本不正，但刑罚得当，因此虽有危险而无咎。《象传》说守正而知危仍无咎，是因为处置得当。荀爽说阴爻来到正位而能制约阳爻，虽有危厉，但从下升上而不失中，所以得当。

**上九：** 肩负刑具，遮蔽耳朵，凶险。荀爽说上九被九五所负，所以称“何校”；它据五而应三，想要消尽坎象；上体坎为耳，因此说灭耳而凶。上九不正，侵夺欲望没有止境，恶行累积而无法掩盖，罪大而不能解脱，所以凶。郑玄说离为枯木、坎为耳，木在耳上，构成负校灭耳之象。《象传》说负校灭耳，是因为听觉不明。《九家易》说坎本可象征聪明，如今坎象不正又遭损灭，所以称聪不明。

**本条翻译状态：**本译文覆盖当前录入的《序卦》、卦辞、《彖传》《象传》、初九至上九及所录注文。刑具、刑罚术语按古代象征与注家解释转述，不将其等同于现代法律制度；“昔肉”“金矢”等训释保留注家差异。尚未与四库影印页逐字校勘。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷五·贲卦集解", "底本：维基文库《周易集解（李鼎祚）/卷五》，录入页面现存正文行535—634，覆盖贲卦经文、彖传、象传、六爻及页面所载注文。保留“贲”“皤”“戔戔”等底本文字及注文，不擅自统一异体；尚未与影印本逐字校勘。译文尚未完成。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷五", """《序卦》曰：物不可以茍合而已，故受之以賁。賁者，飾也。
崔覲曰：言物不可茍合於刑，當須以文飾之，故受之以賁。

(離下艮上)。賁：亨。
虞翻曰：泰上之乾二，乾二之坤上，柔來文剛，陰陽交，故「亨」也。
小利有攸往。
虞翻曰：小謂五。五失正，動得位。體離，以剛文柔。故「小利有攸往」。
鄭玄曰：賁，文飾也。離為日，天文也。艮為石，地文也。天文在下，地文在上，天地二文，相飾成賁者也。猶人君以剛柔仁義之道飾成其德也。剛柔雜，仁義合，然後嘉會禮通，故「亨」也。卦互體坎艮，艮止於上，坎險於下，夾震在中，故不利大行，小有所之，則可矣。

《彖》曰：賁亨，柔來而文剛，故亨。分剛上而文柔，故小利有攸往。
荀爽曰：此本泰卦。謂陰從上來，居乾之中，文飾剛道，交於中和，故「亨」也。分乾之二，居坤之上，上飾柔道，兼據二陰故「小利有攸往」矣。
天文也。
虞翻曰：謂五。利變之正，成巽，體離。艮為星，離日坎月，巽為高。五，天位。離為文明，日月星辰，高麗於上。故稱天之文也。
文明以止，人文也。
虞翻曰：人謂三。乾為人。文明，離。止，艮也。震動離明。五變據四，二五分則止文三。故以三為人文也。
觀乎天文，以察時變。
虞翻曰：日月星辰，為天文也。泰震春，兌秋。賁坎冬，離夏。巽為進退，日月星辰，進退盈縮，謂朓側朏也。歷象在天成變，故「以察時變」矣。
觀乎人文，以化成天下。
虞翻曰：泰乾為人。五上動，體既濟。賁離象，重明麗正，故「以化成天下」也。
幹寶曰：四時之變，縣乎日月；聖人之化，成乎文章。觀日月而要其會通，觀文明而化成天下。

《象》曰：山下有火，賁。
王廙曰：山下有火，文相照也。夫山之為體，層峰峻嶺，峭崄參差。直置其形，已如雕飾。復加火照，彌見文章。賁之象也。
君子以明庶政，無敢折獄。
虞翻曰：君子謂乾。離為明，坤為庶政，故「明庶政」。坎為獄，三在獄得正，故「無敢折獄」。噬嗑四不正，故「利用獄」也。

初九：賁其趾，
虞翻曰：應在震，震為足，故「賁其趾」也。
舍車而徒。
虞翻曰：應在艮。艮為舍，坎為車；徒，步行也。位在下，故「舍車而徒」。
《象》曰：舍車而徒，義弗乘也。
崔覲曰：剛柔相交，以成飾義者也。今近四棄於二比故曰「舍車」。車士大夫所乘，謂二也。四乘於剛，艮止其應，初全其義，故曰「而徒」。徒，塵賤之事也。自飾其行，故曰「賁其趾」。趾，謂初也。
王肅曰：在下，故稱趾。既舍其車，又飾其趾，是徒步也。

六二：賁其須。《象》曰：賁其須，與上興也。
侯果曰：自三至上，有頤之象也。二在頤下，須之象也。二無其應，三亦無應，若能上承於三，與之同德，雖俱無應，可相與而興起也。

九三：賁如濡如，永貞吉。《象》曰：永貞之吉，終莫之陵也。
盧氏曰：有離之文以自飾。故曰「賁如」也。有坎之水以自潤，故曰「濡如」也。體剛履正，故「永貞吉」。與二同德，故「終莫之陵」也。

六四：賁如皤如，白馬翰如，匪寇婚媾。
王弼曰：有應在初。三為寇難，二誌相感，不獲交通。欲靜則失初之應，欲進則懼三之難，故或飾或素。內懷疑懼，鮮潔其馬，翰如以待。雖履正位，未果其誌。匪緣寇隔，乃為婚媾，則終無尤也。
陸績曰：震為馬，為白，故曰「白馬翰如」。
案：皤亦白，素之貌也。
《象》曰：六四，當位疑也。
案：坎為盜，故疑。當位乘三，悖禮難飾，應初遠陽，故曰「當位疑也」。
匪寇婚媾，終無尤也。
崔覲曰：以其守正待應，故「終無尤也」。

六五：賁於邱園，束帛戔戔，吝。終吉。
虞翻曰：艮為山。五半山，故稱邱；木果曰園，故「賁於邱園」也。六五失正，動之成巽。巽為帛、為繩。艮手持，故「束帛」。以艮斷巽，故「戔戔」。失位無應，故「吝」。變而得正，故「終吉」矣。
《象》曰：六五之吉，有喜也。
荀爽曰：艮，山。震，林。失其正位，在山林之間，賁飾邱陵，以為園圃，隱士之象也。五為王位，體中履和，勤賢之主，尊道之君也。故曰「賁於邱園，束帛戔戔」。君臣失正，故「吝」。能以中和飾上成功，故「終吉」而「有喜也」。
虞翻曰：五變之陽，故有喜。凡言喜慶，皆陽爻。束帛戔戔，委積之貌。
案：六五離爻，離為中女。午為蠶絲，束帛之象。

上九：白賁，無咎。
虞翻曰：在巽上，故曰「白賁」。乘五，陰變而得位，故「無咎」矣。
《象》曰：白賁無咎，上得誌也。
幹寶曰：白，素也。延山林之人，采素士之言，以飾其政，故「上得誌」也。
虞翻曰：上之五得位，體成既濟，故曰「得誌」。坎為誌也。""", chatGptTranslation = """**ChatGPT 翻译（工作译文）**

**《序卦》与卦辞：**《序卦》说：事物不能只靠勉强结合而已，所以接着是《贲》卦；贲就是文饰。崔觐说，事物不能只靠刑罚强行结合，还须用文德加以修饰，所以受之以贲。（离下艮上。）贲卦：亨通；小事适宜有所前往。虞翻说此卦由泰卦变化，柔爻来到刚爻之中以文饰刚，阴阳交合所以亨通；“小”指九五，九五失正而变动得位，卦体为离，以刚文饰柔，所以小利有攸往。郑玄说贲是文饰：离为日，是天文；艮为石，是地文。天文在下、地文在上，天地之文相互修饰而成贲；这也像君主以刚柔、仁义之道修饰德行。刚柔交杂、仁义相合，礼仪才能通达，所以亨通。卦的互体有坎、艮，艮止于上、坎险于下，震处其中，因此不宜大举前行，小有所往则可。

**《彖传》：**贲卦亨通，是因为柔来到下方文饰刚；刚爻分出而上行文饰柔，所以小利有攸往。荀爽说此卦本于泰，阴从上来到乾中，文饰刚道，使阴阳交合而中和；乾二分居坤上，装饰柔道，兼据二阴，所以小利有攸往。天文如此。虞翻说天文指九五，变动得正而成巽，卦体为离；艮为星，离为日、坎为月，巽为高，日月星辰高悬于天，所以称天文。文明而有所止，是人文。虞翻说人指六三，乾为人，文明是离，止是艮；三爻为人文。观察天文，可以察知时序变化。虞翻说日月星辰是天文；泰卦震为春、兑为秋，贲卦坎为冬、离为夏；巽为进退，日月星辰运行有进退盈缩，所以观察天象可以察知时变。观察人文，可以教化成就天下。虞翻说泰卦乾为人，五、上爻发动而成既济，贲有离象，重明而附丽于正，所以能够化成天下。干宝说四时变化悬示于日月，圣人教化成就于文章；观察日月以把握其会通，观察文明以教化天下。

**《象传》：**山下有火，是贲卦。王廙说山体层峦峻岭、参差峭拔，本身便像雕饰；再加火光照耀，纹理更加显明，这就是贲象。君子因此明察各项政务，但不轻率决断刑狱。虞翻说君子指乾，离为明、坤为众政，所以明庶政；坎为狱，六三在狱象中得正，所以不轻率断狱。噬嗑卦九四不正，才有利于用狱。

**六爻：**

**初九：** 文饰自己的脚趾，舍弃车乘而步行。虞翻说应爻在震，震为足，所以说贲其趾；应在艮，艮为舍、坎为车，初九居下，所以舍车步行。《象传》说舍车步行，是因为道义上不应乘车。崔觐说刚柔相交而成文饰之义；初九靠近九四，却舍弃与六二相比附。车是士大夫所乘，指六二；四乘刚而艮止其应，初九保全其义，所以步行。步行是地位低下者之事，初九以行为自饰。王肃说初九在下，所以称趾；既舍车又修饰其趾，便是步行。

**六二：** 文饰自己的胡须。《象传》说文饰胡须，是与上方一同兴起。侯果说三至上有颐卦之象，六二处在颐口之下，象征胡须。六二没有正应，六三也无应；若能向上承接六三，与之同德，即使双方都没有正应，也能相互扶持而兴起。

**九三：** 文采鲜明而润泽，长久守正则吉。《象传》说长久守正的吉祥，是最终无人能凌驾于它之上。卢氏说离有文采，所以称贲如；坎有水，所以称濡如。九三刚健得正，故永贞吉；与六二同德，所以最终无人凌侮。

**六四：** 文饰时似华美、时又素白；白马飞驰而来，并非强盗，而是来求婚配。王弼说六四与初九相应，但九三构成寇难，六二心意相感而阻隔交通。六四若静止，便会失去与初九的应合；若前进，又害怕九三的阻难，因此一时文饰、一时素朴，内心疑惧，令马洁白鲜明而等待。虽然处在正位，仍未能实现心愿；最终若明白对方并非寇敌，而是婚配之人，便没有过失。陆绩说震为马、为白，所以称白马翰如。案语说“皤”也是白，是素白的样子。《象传》说六四居位当正却心存疑虑。案语以坎为盗，故疑；六四当位而乘三，礼义关系难以文饰，又远应初阳，所以疑。并非寇敌而是婚配，最终没有过失。崔觐说六四守正等待应爻，所以终无尤。

**六五：** 文饰于丘园，束帛虽少而零散，起初有困难，最终吉祥。虞翻说艮为山，六五处山半，所以称丘；木果为园，所以称丘园。六五失正，发动成巽；巽为帛、为绳，艮为手持之，所以有束帛之象。艮截断巽，故称戋戋；失位无应，所以吝；变动得正，最终吉祥。《象传》说六五之吉有喜。荀爽说艮为山、震为林；六五失正处山林之间，像在丘陵园圃中修饰，象征隐士。五为王位，体中履和，是尊道、勤贤的君主，所以以束帛礼聘丘园之士；君臣失正，故先有吝，最终能以中和修饰而成就事业，所以终吉有喜。虞翻说五变为阳，所以有喜；束帛戋戋是堆积之貌。案语以六五离为中女，午为蚕丝，取束帛之象。

**上九：** 以素白为文饰，没有灾咎。虞翻说上九处巽上，所以称白贲；乘六五，阴爻变而得位，所以无咎。《象传》说白贲无咎，是因为居上者实现了志向。干宝说白就是素；延请山林之人，采纳朴素贤士的意见来修饰政事，所以说上得志。虞翻说上九来到五位而得正，卦体成既济；坎为志，所以称得志。

**本条翻译状态：**本译文覆盖当前录入的《序卦》、卦辞、《彖传》《象传》、初九至上九及所录注文。“皤”“翰如”“戋戋”等词保留注家训释语境，不据后世通行解释擅自改写底本文字。尚未与四库影印页逐字校勘。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷五·剥卦集解", "底本：维基文库《周易集解（李鼎祚）/卷五》，录入页面现存正文行635—716，覆盖剥卦经文、彖传、象传、六爻及页面所载注文。页面保留“君子德车”等底本字样及诸家注文，尚未与影印本逐字校勘。译文尚未完成。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷五", """《序卦》曰：致飾然後通則盡矣，故受之以剝。剝者，剝也。
崔覲曰：以文致飾，則上下情通，故曰「致飾然後通」也。文者致理，極而無救則盡矣。盡，猶剝也。

(坤下艮上)。剝：不利有攸往。
虞翻曰：陰消乾也，與夬旁通。以柔變剛，小人道長；子弒其父，臣弒其君，故「不利有攸往」也。

《彖》曰：剝，剝也。
盧氏曰：此本乾卦。群陰剝陽，故名為剝也。
柔變剛也。
荀爽曰：謂陰外變五。五者至尊，為陰所變，故曰「剝也」。
不利有攸往，小人長也。
鄭玄曰：陰氣侵陽，上至於五，萬物零落，故謂之「剝也」。五陰一陽，小人極盛，君子不可有所之，故「不利有攸往」也。
順而止之，觀象也。
虞翻曰：坤順艮止，謂五消觀成剝，故「觀象」也。
君子尚消息盈虛，天行也。
虞翻曰：乾為君子，乾息為盈，坤消為虛，故「君子尚消息盈虛，天行也」。則出入無疾，反復其道。易虧巽消艮，出震息兌，盈乾虛坤，故於是見之耳。

《象》曰：山附於地，剝。
陸績曰：艮為山，坤為地。山附於地，謂高附於卑，貴附於賤，君不能制臣也。
上以厚下安宅。
盧氏曰：上，君也。宅，居也。山高絕於地。今附地者，明被剝矣。屬地時也。君當厚錫於下，賢當卑降於愚，然後得安其居。

初六：剝床以足，蔑貞兇。
虞翻曰：此卦坤變乾也。動初成巽，巽木為床；復震在下，為足，故「剝床以足」。蔑，無。貞，正也。失位無應，故「蔑貞兇」。震在陰下，《象》曰：以滅下也。
《象》曰：剝床以足，以滅下也。
盧氏曰：蔑，滅也。坤所以載物，床所以安人。在下，故稱「足」。先從下剝，漸及於上，則君政崩滅。故曰「以滅下也」。

六二：剝床以辨，蔑貞兇。
虞翻曰：指間稱辨剝。剝二成艮。艮為指，二在指間。故「剝床以辨」。無應在剝，故「蔑貞，兇」也。
《象》曰：剝床以辨，未有與也。
鄭玄曰：足上稱辨，謂近膝之下。屈則相近，信則相遠，故謂之辨。辨，分也。
崔覲曰：今以床言之，則辨當在第足之間，是床梐也。「未有與」者，言至三則應，故二「未有與」也。

六三：剝無咎。《象》曰：剝之無咎，失上下也。
荀爽曰：眾皆剝陽，三獨應上，無剝害意，是以「無咎」。《象》曰：失上下也。

六四：剝床以膚，兇。
虞翻曰：辨上稱膚。艮為膚。以陰變陽，至四乾毀，故「剝床以膚」。臣弒君，子弒父，故「兇」矣。
王肅曰：在下而安人者，床也。在上而處床者，人也。坤以象床，艮以象人。床剝盡，以及人身，為敗滋深，害莫甚焉。故曰：「剝床以膚，兇」也。
《象》曰：剝床以膚，切近災也。
崔覲曰：床之膚謂薦席，若獸之有皮毛也。床以剝盡，次及其膚，剝於大臣之象，言近身與君也。

六五：貫魚，以宮人寵，無不利。
虞翻曰：剝消觀五。巽為魚，為繩；艮手持繩貫巽，故「貫魚」也。艮為宮室，人謂乾；五以陰代陽，五貫乾，為寵人，陰得麗之，故「以宮人寵」。動得正成觀，故「無不利」也。
何妥曰：夫剝之為卦，下比五陰，駢頭相次，似貫魚也。魚為陰物，以喻眾陰也。夫宮人者，後夫人嬪妾，各有次序，不相瀆亂，此則貴賤有章，寵禦有序。六五既為眾陰之主，能有貫魚之次第，故得「無不利」矣。
《象》曰：以宮人寵，終無尤也。
崔覲曰：魚與宮人皆陰類，以比小人焉。魚大小一貫，若後夫人嬪婦禦女，小大雖殊，寵禦則一，故「終無尤也」。

上九：碩果不食。君子德車，小人剝廬。
虞翻曰：艮為碩果，謂三已復位，有頤象。頤中無物，故「不食」也。夬乾為君子，為德；坤為車，為民；乾在坤，故以德為車。小人謂坤，艮為廬；上變滅艮，坤陰迷亂，故「小人剝廬」也。
《象》曰：君子德車，民所載也。小人剝廬，終不可用也。
侯果曰：艮為果，為廬。坤為輿。處剝之上，有剛直之德，群小人不能傷害也。故果至碩大，不被剝食矣。君子居此，萬姓賴安，若得乘其車輿也。小人處之，則庶方無控，被剝其廬舍，故曰「剝廬，終不可用」矣。""", chatGptTranslation = """**ChatGPT 翻译（工作译文）**

**《序卦》与卦辞：**《序卦》说：文饰达到极致之后，事理便会走向穷尽，所以接着是《剥》卦；剥就是剥落。崔觐说，以文饰使上下情意相通，但文饰若达到极点而无法挽救，便会穷尽；穷尽也就是剥落。（坤下艮上。）剥卦：不适宜有所前往。虞翻说阴气消削乾阳，与夬卦旁通；以柔变刚，小人之道增长，甚至出现子弑父、臣弑君的局面，所以不宜前往。

**《彖传》：**剥，就是剥落。卢氏说此卦本于乾，众阴剥削阳刚，所以称剥。柔爻改变刚爻。荀爽说阴爻从外部改变九五，九五是至尊之位，却被阴爻改变，所以称剥。不利于前往，是因为小人势力增长。郑玄说阴气侵凌阳气，上至九五，万物凋零；五阴一阳，小人极盛，君子不可有所前往。顺从而止住它，是观察卦象所得。虞翻说坤为顺、艮为止，九五消退而成观卦，再成剥，所以是观象。君子重视消长、盈虚的规律，这是天道运行。虞翻说乾为君子，乾息为盈、坤消为虚；出入无疾、反复其道，易卦由巽亏、艮消，震出、兑息，乾盈、坤虚，都可以从这里看出。

**《象传》：**山附着于地，是剥卦。陆绩说艮为山、坤为地；山依附于地，是高者依附于卑者、贵者依附于贱者，象征君主不能制约臣下。居上位者应厚待下民，使居处安稳。卢氏说“上”指君主，“宅”指居所。山本高出地面，如今依附于地，正是被剥削的象。君主应厚待下属，贤者也应谦下待人，才能安居其位。

**六爻：**

**初六：** 剥蚀床脚，破坏正道，凶险。虞翻说此卦由坤变乾，初爻发动成巽，巽木为床；下方复有震，震为足，所以是剥床以足。“蔑”是无，“贞”是正；失位而无应，所以凶。《象传》说剥床以足，是从下方开始毁坏。卢氏说坤承载万物，床用来安置人；床脚在下，所以先从下剥蚀，逐渐上及其余部分，君主政权也会由此崩坏。

**六二：** 剥蚀床板与床脚之间的连接处，破坏正道，凶险。虞翻说指间称辨，剥卦第二爻形成艮，艮为手指，二爻处在指间，所以称剥床以辨；没有正应，所以凶。《象传》说床板被剥，是因为尚无相助者。郑玄说足上称辨，指接近膝下的位置；屈曲时相近，伸展时相远，所以称辨，辨就是分隔。崔觐说若以床作比喻，辨应在床足之间，是床的横档；“未有与”是说到六三才有相应，因此六二尚无同伴。

**六三：** 剥落之中仍可无咎。《象传》说剥而无咎，是因为失去了上下两方的依附。荀爽说众阴都在剥削阳刚，唯独六三与上九相应，没有剥害之意，所以无咎。

**六四：** 剥蚀床面，凶险。虞翻说床辨之上称肤；艮为肤。阴爻改变阳爻，至六四时乾体被毁，所以是剥床以肤；臣弑君、子弑父，因此凶。王肃说床在下方承托人，使人安居；人处在床上。坤象征床，艮象征人。床被剥尽，灾害进而及于人的身体，败坏更深，没有比这更严重的，所以说剥床以肤凶。《象传》说剥及床面，是灾祸已经迫近。崔觐说床的“肤”指席垫，如同兽有皮毛；床被剥尽之后，接着便剥到席垫，象征剥害大臣并逼近君主身边。

**六五：** 像串起鱼一样，使宫中女子依次受宠，没有不利。虞翻说剥卦消退而成观，巽为鱼、为绳，艮手持绳贯穿巽鱼，所以称贯鱼。艮为宫室，乾为人；六五以阴代阳，贯连乾体而成为受宠之人，阴得以依附，所以称宫人受宠。发动得正而成观，所以无不利。何妥说剥卦下方五阴并列，头部相接，像鱼贯成串；鱼属阴，以比喻众阴。宫人包括后夫人、嫔妾，各有次序而不相混乱，贵贱有章、宠御有序。六五为众阴之主，能使众阴依次排列，所以无不利。《象传》说以宫人受宠，最终没有过失。崔觐说鱼与宫人都是阴类，用来比喻小人；鱼大小虽不同而贯在一处，后夫人、嫔妇、御女等级虽有差异，受宠的秩序则相同，因此终无尤。

**上九：** 巨大的果实没有被吃掉。君子以德为车，小人则剥毁自己的屋舍。虞翻说艮为硕果，指六三已经复位而形成颐象；颐中无物，所以说果实不食。夬卦乾为君子、为德，坤为车、为民；乾在坤上，所以以德为车。小人指坤，艮为屋舍；上九变动而毁去艮象，坤阴迷乱，所以小人剥毁屋舍。《象传》说君子以德为车，是百姓所承载的；小人剥毁屋舍，最终不可任用。侯果说艮为果、为屋舍，坤为车舆。上九处剥卦之上，有刚直之德，众小人不能伤害它，所以果实长大而不被剥食。君子居此位，百姓都能依赖而安定，如同乘坐其车舆；小人居此位，则四方失去依托，自己的屋舍也被剥毁，因此最终不可任用。

**本条翻译状态：**本译文覆盖当前录入的《序卦》、卦辞、《彖传》《象传》、初六至上九及所录注文。床体部位、“贯鱼”“硕果不食”等象征语依注家解释转述；“君子德车”按底本文字保留，不擅自改成其他通行异文。尚未与四库影印页逐字校勘。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷六·复卦集解", "底本：维基文库《周易集解（李鼎祚）/卷六》，正文行112—208。录入复卦经文、彖象、六爻及页面所载注文、案语。原页面字形与注文归属照录，尚未与影印本逐字校勘。已附独立 ChatGPT 工作译文；原文仍待影印本逐字校勘。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷六", """《序卦》曰：物不可以終盡，剝窮上反下，故受之以復也。
崔覲曰：夫易窮則有變，物極則反於初。故剝之為道，不可終盡，而受之於復也。

(震下坤上)。復：亨。
何妥曰：復者，歸本之名。群陰剝陽，至於幾盡，一陽來下，故稱反復。陽氣復反，而得交通，故雲「復亨」也。
出入無疾，朋來無咎。
虞翻曰：謂出震成乾，入巽成坤。坎為疾，十二消息，不見坎象，故「出入無疾」。兌為朋，在內稱來；五陰從初，初陽正息而成兌，故「朋來無咎」矣。
反復其道，七日來復。
案：易軌：一歲十二月，三百六十五日四分日之一。以坎、震、離、兌四方正卦，卦別六爻，爻生一氣。其余六十卦，三百六十爻，爻主一日，當周天之數，余五日四分日之一，以通閏余者也。剝卦陽氣盡於九月之終，至十月末，純坤用事。坤卦將盡，則復陽來。隔坤之一卦六爻。為六日。復來成震，一陽爻生，為七日。故言「反復其道，七日來復」。是其義也。天道玄邈，理絕希慕，先儒已論，雖各指於日月，後學尋討，猶未測其端倪。今舉約文，略陳梗概，以候來悊，如積薪者也。
利有攸往。
虞翻曰：陽息臨成乾，小人道消，君子道長，故「利有攸往」矣。
《彖》曰：復亨，
虞翻曰：陽息坤，與垢旁通。剛反交初，故「亨」。
剛反動，而以順行，
虞翻曰：剛從艮入坤，從反震，故曰「反動」也。坤順震行，故「而以順行」。陽不從上來反初，故不言剛自外來。是以明不遠之復，入坤出震義也。
是以出入無疾，朋來無咎。
侯果曰：陽上出，君子道長也。陰下入，小人道消也。動而以行，故「出入無疾，朋來無咎」矣。
反復其道，七日來復，天行也。
虞翻曰：謂乾成坤，反出於震而來復，陽為道，故「復其道」。剛為晝日。消乾六爻。為六日。剛來反初，故「七日來復，天行也」。
侯果曰：五月天行至午陽復而陰升也；十一月天行至子，陰復而陽升也。天地運往，陰陽升復，凡歷七月，故曰「七日來復」。此天之運行也。豳詩曰：一之曰觱發，二之日栗烈。一之日，周之正月也；二之日，周之二月也。則古人呼月為日明矣。
利有攸往，剛長也。
荀爽曰：利往居五，剛道浸長也。
復，其見天地之心乎。
虞翻曰：坤為復。謂三復位時，離為見，坎為心。陽息臨成泰，乾天坤地。故「見天地之心」也。荀爽曰：復者，冬至之卦。陽起初九，為天地心。萬物所始，吉兇之先，故曰「見天地之心」矣。
《象》曰：雷在地中，復。先王以至日閉關，商旅不行，後不省方。
虞翻曰：先王謂乾初。至日冬至之日。坤闔為閉關。巽為商旅，為近利市三倍，姤巽伏初，故商旅不行。姤象曰：後以施命誥四方。今隱復下，故後不省方，復為陽始，姤則陰始。天地之始，陰陽之首。已言先王，又更言後，後，君也。六十四卦，唯此重耳。
宋衷曰：商旅不行，自天子至公侯，不省四方之事，將以輔遂陽體，成致君道也。制之者，王者之事。奉之者，為君之業也。故上言先王而下言後也。
初九：不遠復，無祗悔，元吉。
崔覲曰：從坤反震，而變此爻，不遠復也。復而有應故獲「元吉」也。
《象》曰：不遠之復，以修身也。
侯果曰：祗，大也。往被陰剝，所以有悔。覺非遠復，故無大咎。以此修身，顏子之分矣。
六二：休復，吉。《象》曰：休復之吉，以下仁也。
王弼曰：得位居中。比初之上，而附順之下，仁之謂也。既處中位，親仁善鄰，復之休也。
六三：頻復，厲，無咎。
虞翻曰：頻蹙也。三失位，故「頻復，厲」。動而之正，故「無咎」也。
《象》曰：頻復之厲，義無咎也。
侯果曰：處震之極，以陰居陽，懼其將危，頻蹙而復，履危反道，義亦無咎也。
六四：中行獨復。《象》曰：中行獨復，以從道也。
虞翻曰：中謂初。震為行。初一陽爻，故稱「獨」。四得正應初，故曰「中行獨復，以從道也」。俗說以四位在五陰之中，而獨應復，非也。四在外體，又非內象，不在二五，何得稱「中行」耳？
六五：敦復，無悔。《象》曰：敦復無悔，中以自考也。
侯果曰：坤為厚載，故曰「敦復」。體柔居剛，無應失位，所以有悔。能自考省，動不失中，故「無悔」矣。
上六：迷復，兇、有災眚。
虞翻曰：坤冥為迷，高而無應，故「兇」。五變正時，坎為災眚，故「有災眚」也。
用行師，終有大敗，以其國君兇，
虞翻曰：三復位時，而體師象，故「用行師」。陰逆不順，坤為死喪，坎流血，故「終有大敗」。姤乾為君滅藏於坤，坤為異邦，故「國君兇」矣。
荀爽曰：坤為眾，故「用行師」也。謂上行師，而距於初。陽息上升，必消群陰，故「終有大敗」。國君，謂初也。受命復道，當從下升。今上六行師，王誅必加。故「以其國君兇」也。
至於十年不克征。
虞翻曰：坤為至，為十年。陰逆坎臨，故「不克征」。謂五變設險，故帥師敗喪，君而無征也。
何妥曰：理國之道，須進善納諫。迷而不復，安可牧民？以此行師，必敗績矣。敗乃思復，失道已遠。雖復十年乃征，無所克矣。
案：坤為先迷，故曰「迷復」。坤又為師象，故曰「行師」。坤數十，十年之象也。
《象》曰：迷復之兇，反君道也。
虞翻曰：姤乾為君，坤陰滅之，以國君兇，故曰「反君道」也。""", chatGptTranslation = """**ChatGPT 翻译（工作译文）**

**《序卦》与卦辞：**《序卦》说：事物不能终结于穷尽；剥卦发展到极点，便由上反转而向下，所以接着是《复》卦。崔觐说，《易》的变化在于穷极则变，事物到达极点便返回初始；剥道不能永远持续，因此接着以复卦承接。（震下坤上。）复卦：亨通。何妥说，复是返回根本的名称。众阴不断剥削阳气，几乎到了尽头，一阳重新来到下方，所以称为反复；阳气回返，阴阳重新交通，因此说复亨。

“出入没有疾病，朋友到来没有灾咎。”虞翻说，这是阳气出震而形成乾、进入巽而形成坤。坎象征疾病，十二消息卦的变化中没有坎象，所以出入无疾。兑象征朋友；在内卦称为“来”。五阴随从初阳，初阳正开始增长而形成兑象，所以朋来无咎。

“反复其道，七日来复。”案语引《易轨》解释：一年十二个月，共三百六十五日又四分之一日。坎、震、离、兑是四方正卦，每卦六爻，各爻对应一种气候变化；其余六十卦共有三百六十爻，每爻主一日，合于周天之数，另有五日四分之一日用于调节闰余。剥卦阳气在九月末消尽，至十月末由纯坤主事；坤卦将尽时，复阳到来。剥与复之间隔着坤卦六爻，算作六日；复卦形成震，一阳爻生，算作第七日，因此说七日来复。案语又提醒，天道幽远深奥，古儒虽各有日月之说，后学仍难以穷尽其端绪；这里只举简要文字，略陈大概，留待后来的明者继续考察。

“利于有所前往。”虞翻说，阳气增长，临卦形成并继续发展为乾，小人之道消退，君子之道增长，所以利于前往。

**《彖传》：**复卦亨通。虞翻说，阳气在坤中增长，与姤卦旁通；刚爻返回并交接初位，所以亨通。“刚爻返回而发动，并以顺道运行。”虞翻说，刚爻从艮进入坤，又反转而成震，所以称“反动”；坤为顺，震为行，所以说以顺道运行。阳爻不是从上方来到初位，因此这里不说“刚自外来”，而是说明不远之复，以及阳气进入坤、再从震出现的意义。“因此出入无疾，朋友来临没有灾咎。”侯果说，阳气向上出现，君子之道增长；阴气向下退入，小人之道消退。阳气发动并顺势运行，所以出入无疾、朋来无咎。

“反复其道，七日来复，是天道运行。”虞翻说，乾变为坤后，又从震返回而来复；阳象征道路，所以说复其道。刚爻象征白昼；乾的六爻消退，经过六日，刚爻返回初位，因此第七日来复。侯果则以一年中的阴阳消长解释：五月阳气到午位而阴气开始上升，十一月阳气到子位而阳气重新上升；天地运行，阴阳升降，历经七个月，所以称七日来复。这里的“日”可以用来指月，侯果以《豳风》“一之日”“二之日”作为古人以日称月的例证。

“利于有所前往，是因为刚阳正在增长。”荀爽说，阳刚适宜前往居于五位，刚道逐渐增长。“复卦大概可以看见天地的本心吧！”虞翻说，坤为复；三爻复位时，离为显现、坎为心。阳气增长而形成临，再成泰，乾为天、坤为地，所以说看见天地之心。荀爽说，复卦对应冬至，阳气从初九开始萌生，这是天地之心，也是万物的起始、吉凶的先兆。

**《象传》：**雷在地中，是复卦。先王在冬至日关闭关口，商旅停止通行，君主也不巡视四方。虞翻说，先王指乾初；“至日”是冬至。坤象征闭合，所以关闭关门。巽象征商旅，也象征逐利；姤卦巽象伏藏于初，因此商旅不行。姤卦《象传》说君主发布命令告示四方；如今这一象隐藏在复卦之下，所以君主不巡视四方。复是阳气开始，姤是阴气开始，分别代表天地之始、阴阳之首。这里先说先王，又说“后”，虞翻解释“后”也是君主。宋衷说，商旅不行，是从天子到公侯都不处理四方政务，以便辅助阳气生长、成就君道；制定禁令是王者的职责，遵行禁令则是君主的事业。

**六爻：**

**初九：** 回返得不远，没有大的悔恨，大为吉祥。崔觐说，从坤返回震，初爻发生变化，便是不远之复；复而有应，所以获得大吉。《象传》说，不远之复，是用来修养自身。侯果说，“祗”是大。前往而被阴气剥削，所以有悔；及时察觉错误并迅速返回，便没有大灾咎。以此修身，是颜回所能做到的境界。

**六二：** 美善地回返，吉祥。《象传》说，美善地回返而吉，是因为它居下而亲近仁者。王弼说，六二得位而居中，靠近初九并顺从依附于它；初九具有仁德，因此六二亲近仁者、善待邻近之人，这就是美善的复归。

**六三：** 屡次反复地回返，有危险，但没有灾咎。虞翻说，“频”是皱眉忧蹙。六三失位，所以有频复之象并伴随危险；变动而得到正位，因此无咎。《象传》说，频复虽有危险，从道义上说仍无咎。侯果说，六三处在震卦的极处，以阴爻居阳位，担心将有危险，于是屡次忧惧而回返；虽身处危险，却能返回正道，所以道义上没有灾咎。

**六四：** 行于中途而独自回返。《象传》说，行于中途而独自回返，是为了追随正道。虞翻说，“中”指初九，震象征行动；初九是一根阳爻，所以称“独”。六四得正并与初九相应，因此说中行独复。虞翻特别反对把“中行”解释为六四处在五个阴爻之中、却独自与初九相应；六四在外卦，不是内卦之象，也不处于二、五中位，不能这样解释“中行”。

**六五：** 厚实地回返，没有悔恨。《象传》说，敦厚地回返而无悔，是因为居中而能自我省察。侯果说，坤具有厚载之象，所以称敦复。六五以柔居刚，既无应又失位，本来有悔；但能够自我考察反省，行动不失中道，所以无悔。

**上六：** 迷失方向而不知回返，凶险，并有灾祸。虞翻说，坤的幽暗象征迷失；上六居高而无应，所以凶。六五变正时，坎象征灾祸，所以有灾眚。

“出兵作战，最终会遭到大败；甚至连本国君主也有凶险。”虞翻说，三爻复位时形成师卦之象，所以说用兵。阴爻逆行而不顺，坤象征死亡丧失，坎象征流血，因此最终大败。姤卦乾象征君主，其阳气被藏灭于坤中；坤又象征异邦，所以国君有凶。荀爽说，坤为众，所以有出兵之象；上六出兵抵拒初九，阳气增长上升，必将消退众阴，所以最终大败。国君指初九；初九奉命复归正道，本应由下向上升进，如今上六却发兵抵拒，王者的诛罚必将到来，所以国君凶。

“直到十年，也不能出征取胜。”虞翻说，坤为至、为十年；阴气逆行而临于坎险，所以不能征伐。五爻变动设置险阻，因此统帅军队必然败亡，君主也就不能再出征。何妥说，治理国家必须进用善人、接纳谏言；迷失正道而不知返回，怎能治理百姓？以这种状态出兵必然失败。失败之后才想到回返，已经离正道太远，即使过了十年再出征，也不会取得胜利。案语说，坤有先迷之象，所以称迷复；坤又有师卦之象，所以称行师；坤数为十，因此有十年之象。《象传》说迷复之凶，是因为反背了君主之道。虞翻说姤卦乾为君，坤阴将其消灭，故有国君之凶，这就是反背君道。

**本条翻译状态：**本译文覆盖当前录入的《序卦》、卦辞、《彖传》《象传》、初九至上六及所录注文、案语。对于“七日”的日月周期解释，分别保留案语、虞翻、侯果的不同说法；不将其合并成单一历法结论。尚未与四库影印页逐字校勘。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷六·无妄卦集解", "底本：维基文库《周易集解（李鼎祚）/卷六》，正文行210—296。录入无妄卦经文、彖象、六爻及页面所载注文、案语。底本“元享”“大享”等疑似字形依页面保留，未擅自改为通行本字样。已附独立 ChatGPT 工作译文；原文仍待影印本逐字校勘。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷六", """《序卦》曰：復則不妄矣，故受之以無妄。
崔覲曰：物復其本，則為成實，故言復則無妄矣。
(震下乾上)。無妄。
何妥曰：乾上震下，天威下行，物皆絜齊，不敢虛妄也。
元享，利貞。
虞翻曰：遁上之初。此所謂四陽二陰，非大壯則遁來也。剛來交初，體乾，故「元亨」。三四失位，故「利貞」也。
其匪正有眚，不利有攸往。
虞翻曰：非正謂上也。四已之正，上動成坎，故「有眚」。變而逆乘，天命不右，故「不利有攸往」矣。
《彖》曰：無妄，剛自外來而為主於內。
蜀才曰：此本遁卦。
案：剛自上降，為主於初，故「動而健，剛中而應」也。於是乎邪妄之道消，大通以正矣，無妄大亨，乃天道恒命也。
動而健，剛中而應。大享以正，天之命也。其匪正有眚，不利有攸往。
虞翻曰：動，震也。健、大亨謂乾。剛中謂五，而應二。大亨以正，變四承五；乾為天，巽為命，故曰「大亨以正，天之命也」。
無妄之往，何之矣。
虞翻曰：謂四已變，上動體屯，坎為泣血漣如，故「何之矣」。
天命不右，行矣哉。
虞翻曰：天，五也。巽為命。右，助也。四已變成坤，天道助順。上動，逆乘巽命。故「天命不右，行矣哉」。言不可行也。馬君雲：天命不右行，非矣。
《象》曰：天下雷行，物與無妄，
《九家易》曰：天下雷行，陽氣普遍，無物不與，故曰「物與」也。物受之以生，無有災妄，故曰「物與無妄」也。
虞翻曰：與，謂舉。妄，亡也。謂雷以動之，震為反生，萬物出震，無妄者也，故曰「物與無妄」。《序卦》曰：復則不妄矣。故受之以無妄。而京氏及俗儒，以為大旱之卦，萬物皆死，無所復望，失之遠矣。有無妄然後可畜，不死明矣。若物皆死，將何畜聚，以此疑也。
先王以茂對時育萬物。
虞翻曰：先王謂乾。乾盈為茂，艮為對時。體頤養象，萬物出震。故「以茂對時育萬物」。言物皆死，違此甚矣。
侯果曰：雷震天下，物不敢妄；威震驚洽，無物不與，故「先王以茂養萬物」，乃對時而育矣。時泰，則威之以無妄；時否，則利之以嘉遁。是對時而化育也。
初九：無妄，往吉。
虞翻曰：謂應四也。四失位，故命變之正。四變得位，承五應初，故「往吉」。在外稱往也。
《象》曰：無妄之往，得誌也。
虞翻曰：四變應初，夫妻體正，故「往得誌」矣。
六二：不耕獲，不菑畬，則利有攸往。
虞翻曰：有益耕象，無坤田，故「不耨」。震為禾稼，艮為手，禾在手中，故稱「獲」。田在初，一歲曰「菑」。在二、二歲曰「畬」。初爻非坤，故不菑而畬也。得位應五，利四變之益，則坤體成，有耒耨之利，故「利有攸往」。往應五也。
《象》曰：不耕獲，未富也。
虞翻曰：四動坤虛，故「未富也」。
六三：無妄之災，或系之牛。行人之得，邑人之災。
《象》曰：行人得牛，邑人災也。
虞翻曰：上動體坎，故稱「災」也。四動之正，坤為牛，艮為鼻為止；巽為桑，為繩，系牛鼻而止桑下，故「或系之牛」也。乾為行人，坤為邑人，乾四據三，故「行人之得」。三系於四，故「邑人之災」。或說：以四變剛牛應初，震坤為死喪，故曰「行人得牛，邑人災也」。
九四：可貞，無咎。
虞翻曰：動得正，故「可貞」。承五應初，故「無咎」也。
《象》曰：可貞無咎，固有之也。
虞翻曰：動陰承陽，故「固有之也」。
九五：無妄之疾，勿藥有喜。
虞翻曰：謂四已之正，上動體坎；坎為疾，故曰「無妄之疾」也。巽為木，艮為石，故稱「藥」。坎為多眚，藥不可試，故「勿藥有喜」。康子饋藥，丘未達，故不嘗，此之謂也。
《象》曰：無妄之藥，不可試也。
侯果曰：位正居尊，為無妄貴主，百姓有過，在予一人。三四妄處，五乃憂疾，非乖攝，則藥不可試。若下皆不妄，則不治自愈，故曰「勿藥有喜」也。
上九：無妄行，有眚，無攸利。
虞翻曰：動而成坎，故「行有眚」。乘剛逆命，故「無攸利」。天命不右，行矣哉。
《象》曰：無妄之行，窮之災也。
崔覲曰：居無妄之中，有妄者也。妄而應三，上下非正。窮而反妄，故為災也。""", chatGptTranslation = """**ChatGPT 翻译（工作译文）**

**《序卦》与卦辞：**《序卦》说：复归本原之后，就不会再有虚妄，所以接着是《无妄》卦。崔觐说，事物返回本原，便能成为真实而充实的状态，因此说复之后便无妄。（震下乾上。）无妄。何妥说，乾在上、震在下，天威向下运行，万物都整齐洁净，不敢虚妄。

“元亨，利贞。”虞翻说，此卦由遁卦上爻来到初位。所谓四阳二阴的卦体，不是从大壮而来，就是从遁而来；刚爻来到初位相交，卦体成为乾，所以元亨。三、四爻失位，所以利于守正。

“如果不正，就会有灾祸；不宜有所前往。”虞翻说，不正指上九。九四已经变得正位，上九发动而形成坎象，所以有灾。变动后又逆乘刚爻，天命不予助佑，因此不利于前往。

**《彖传》：**无妄，是刚爻从外部来到内部，并成为内卦的主爻。蜀才说，此卦本于遁卦。案语说，刚爻从上方下降，成为初位的主爻，因此说“动而健，刚中而应”。邪妄之道由此消退，正道大为通达；无妄的大亨通，是天道恒常的命则。

“行动而刚健，刚爻居中并有所应；以正道获得大亨通，这是天命。”虞翻说，动是震，健与大亨是乾；刚中指九五，与六二相应。大亨以正，是九四变动后承接九五；乾为天，巽为命，所以说这是天命。

“无妄之时还要前往，能到哪里去呢？”虞翻说，这是九四已经变动，上九发动后形成屯卦，坎象征泣血涟涟，因此问还能往哪里去。“天命不助佑，这样还能行动吗？”虞翻说，天指九五，巽为命；“右”是帮助。九四已经变成坤，天道帮助顺从者；上九发动，逆乘巽命，因此天命不予帮助，不能行动。马融认为“天命不右行”之说不正确，原文特意保留这一异说。

**《象传》：**雷在天下运行，万物因此获得无妄。 《九家易》说，雷行天下，阳气普遍运行，没有任何事物不受到它的影响，所以称“物与”；万物受阳气而生，不发生灾妄，所以称物与无妄。虞翻说，“与”是举起、发动；“妄”有亡失之义。雷使万物发动，震象征复生，万物从震而出，所以是无妄。《序卦》说复之后便无妄。京房及一般儒者把无妄解释为大旱之卦、万物皆死而无所复望，虞翻认为这种解释相差甚远：有无妄之后才有大畜，万物既然能够畜聚，便可知并非都已死亡。

先王因此盛大地顺应时令，养育万物。虞翻说先王指乾；乾气充盈为茂盛，艮为顺应时节，卦体有颐养之象，万物从震而出，所以说盛大顺时育养万物。把万物都解释为死亡，便与这一象义完全相违。侯果说，雷震动天下，万物不敢妄为；雷威震动而遍及万物，没有一物不受到影响，因此先王应当顺应时令养育万物。时势安泰，就以无妄之道威正万物；时势否塞，则以嘉美的退避之道使其受益，这就是顺应时势而化育万物。

**六爻：**

**初九：** 无妄而前往，吉祥。虞翻说，初九与九四相应；九四失位，所以命其变动而得正。九四变后得位，承接九五并与初九相应，所以前往吉祥；处于外卦称为往。《象传》说，无妄而前往，是因为实现了心愿。虞翻说，九四变动而与初九相应，夫妻之象得正，所以前往能够得志。

**六二：** 不耕作便有所收获，不开垦荒地便形成熟田；这样有利于有所前往。虞翻说，卦中有益卦的耕作之象，却没有坤田，所以说不耕。震为禾稼，艮为手，禾谷在手中，所以称获。田在初位，一年开垦称菑；在二位、经过两年称畬。初爻不是坤，所以这里说不菑而畬。六二得位并与九五相应，若九四变动成益卦，坤体形成，便有耒耜耕作之利，所以利于前往应九五。《象传》说不耕而获，是因为尚未富足。虞翻说九四发动后坤体空虚，所以说未富。

**六三：** 无妄之灾：有人把牛拴在那里，过路人得到了牛，乡里的人却遭受灾祸。《象传》说，行人得到牛，是邑人的灾祸。虞翻说上九发动形成坎象，所以称灾。九四变正，坤为牛，艮为鼻、为停止；巽为桑树、为绳索，牛鼻被绳系住，停在桑树下，所以有系牛之象。乾为行人，坤为邑人；乾九四据临六三，所以行人得到牛；六三被系于九四，所以邑人遭灾。另有一种解释说，九四变为刚爻，刚牛与初九相应；震、坤有死亡丧失之象，因此说行人得牛、邑人遭灾。

**九四：** 可以守正，没有灾咎。虞翻说九四发动而得正，所以可以守正；它承接九五并与初九相应，所以无咎。《象传》说可以守正而无咎，是因为能够固守已有的正道。虞翻说阴爻发动而承接阳爻，因此能固守其所有。

**九五：** 无妄之病，不用服药也会有喜庆。虞翻说九四已经变正，上九发动而形成坎；坎为疾病，所以称无妄之疾。巽为木，艮为石，所以有药物之象。坎又象征多灾，药物不可轻试，因此不服药反而有喜。虞翻引用孔子不轻易尝试季康子所馈赠之药的故事来说明。《象传》说，无妄之药，不可轻易尝试。侯果说，九五居正而尊贵，是无妄卦的尊主；百姓有过失，责任在君主自身。六三、九四处于不正之位，九五因此忧虑成疾；如果下方众爻都不妄为，便不必治理也会自行痊愈，所以说勿药有喜。

**上九：** 无妄而行动，会有灾祸，没有任何利益。虞翻说发动而形成坎，所以行动有灾；乘刚而逆天命，所以没有利益。《象传》说，无妄之行，是走到穷极处所招致的灾祸。崔觐说，上九处于无妄卦中却产生妄行；它妄动而与六三相应，上下都不正，穷极而反生妄念，所以成为灾祸。

**本条翻译状态：**本译文覆盖当前录入的《序卦》、卦辞、《彖传》《象传》、初九至上九及所录注文、案语。底本“元享”“大享”等字样仍按原录保留，译文依上下文释义，但不据译意回改原文；“天命不右行”等处保留注家异说。尚未与四库影印页逐字校勘。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷六·大畜卦集解", "底本：维基文库《周易集解（李鼎祚）/卷六》，正文行298—393。录入大畜卦经文、彖象、六爻及页面所载注文、案语。保留“輿說腹/輻”等底本和注文异说，不作无依据统一。已附独立 ChatGPT 工作译文；原文仍待影印本逐字校勘。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷六", """《序卦》曰：有無妄然後可畜，故受之以大畜。
崔覲曰：有誠實，則可以中心藏之，故言有無妄然後可畜也。
(乾下艮上)。大畜：利貞。
虞翻曰：大壯初之上，其德剛上也。與萃旁通。二五失位，故「利貞」。此萃五之復二，成臨。臨者。大也。至上有頤養之象，故名大畜也。
不家食，吉。利涉大川。
虞翻曰：二稱家，謂二五易位，成家人。家人體噬嗑食，故「利涉大川，應乎天也」。
《彖》曰：大畜，剛健篤實，輝光日新。
虞翻曰：剛健謂乾，篤實謂艮。二已之五，利涉大川。互體離坎，離為日，故「輝光日新」也。
其德剛上而尚賢。
蜀才曰：此本大壯卦。
案：剛自初升，為主於外。剛陽居上，尊尚賢也。
能健止，大正也。
虞翻曰：健，乾。止，艮也。二五易位，故「大正」。舊讀言能止健，誤也。
不家食吉，養賢也。
虞翻曰：二五易位成家人。今體頤養象，故「不家食吉，養賢也」。
案：乾為賢人也，艮為宮闕也，令賢人居於闕下，不家食之象。
利涉大川，應乎天也。
京房曰：謂二變五體坎，故「利涉大川」。五天位，故曰「應乎天」。
《象》曰：天在山中，大畜。
向秀曰：止莫若山，大莫若天，天在山中，大畜之象。天為大器，山則極止，能止大器，故名大畜也。
君子以多誌前言往行，以畜其德。
虞翻曰：君子謂乾。乾為言，震為行，坎為誌；乾知大始，震在乾前，故「識前言往行」。有頤養象，故「以畜其德」矣。
初九：有厲，利已。
王弼曰：四乃畜已，未可犯也。進則災危，有厲則止，故能「利已」。
《象》曰：有厲，利已，不犯災也。
虞翻曰：謂二變正，四體坎，故稱「災也」。
九二：輿說腹。
虞翻曰：萃坤為車，為腹；坤消乾成，故「車說腹。」腹，或作輻也。
《象》曰：輿說腹，中無尤也。
盧氏曰：乾為輿。
案：輻車之鉤心。夾軸之物。處失其正，上應於五。五居畜盛，止不我升，故且說輻停留。待時而進退，得正，故「無尤」也。
九三：良馬逐，利艱貞，吉。日閑輿衛，
虞翻曰：乾為良馬，震為驚走，故稱「逐」也。謂二已變，三在坎中，故「利艱貞吉」。離為日。二至五，體師象。坎為閑習，坤為車輿；乾人在上，震為驚衛，講武閑兵，故曰「日閑輿衛」也。
利有攸往。《象》曰：利有攸往，上合誌也。
虞翻曰：謂上應也。五已變正，上動成坎，坎為誌，故「利有攸往，與上合誌也」。
六四：童牛之告，元吉。
虞翻曰：艮為童。五已之正。萃坤為牛。告謂以木楅其角。大畜，畜物之家，惡其觸害。艮為手，為小木。巽為繩。繩縛小木，橫著牛角，故曰「童牛之告」。得位承五，故元吉而喜，喜，謂五也。
《象》曰：六四元吉，有喜也。
侯果曰：坤為輿，故有牛矣。牿，楅也。以木為之，橫施於角，止其牴之威也。初欲上進，而四牿之。角既被牿，則不能觸四，是四童初之角也。四能牿初，與無角同，所以「元吉」而「有喜」矣。童牛，無角之牛也。《封人職》曰：設其楅衡。註雲：楅設於角，衡設於鼻，止其抵觸也。
六五：豮豕之牙，吉。
虞翻曰：二變時，坎為豕。劇豕稱豮。令不害物。三至上，體頤象。五變之剛，巽為白；震為出；剛白從頤中出，牙之象也。動而得位，「豮豕之牙，吉」。
《象》曰：六五之吉，有慶也。
虞翻曰：五變得正，故「有慶也」。
崔覲曰：《說文》：豮，劇豕。今俗猶呼劇豬是也。然以豕本剛突，劇乃性和，雖有其牙，不足害物，是制於人也。以喻九二之剛健失位，若豕之劇，不足畏也；而六五應止之易，故「吉有慶矣」。
案：九二坎爻，坎為豕也。以陽居陰，而失其位，若豕被劇之象也。
上九：何天之衢，享。
虞翻曰：何，當也。衢，四交道。乾為天，震艮為道。以震交艮，故「何天之衢，亨」。上變，坎為亨也。
王弼曰：處畜之極，畜極則亨。何，辭也。猶雲何畜，乃天之衢亨，道大行也。
《象》曰：何天之衢，道大行也。
虞翻曰：謂上據二陰。乾為天道，震為行，故「道大行矣」。""", chatGptTranslation = """**ChatGPT 翻译（工作译文）**

**《序卦》与卦辞：**《序卦》说：有了无妄，然后才能积蓄，所以接着是《大畜》卦。崔觐说，具有诚信真实的品质，才能从内心积存它，因此说有无妄之后才能有所畜养。（乾下艮上。）大畜卦：适宜守正。虞翻说，此卦由大壮初爻升至上位，刚健之德居上，与萃卦旁通；二、五爻失位，所以利贞。此卦又可由萃卦九五与复卦六二交换而成临卦；临有大义，至上爻又有颐养之象，所以称大畜。

“不在家中自食，吉祥；利于涉越大川。”虞翻说，二爻称家，指二、五爻交换而成家人卦；家人卦的互体有噬嗑之象，噬嗑有饮食之义，所以说利涉大川、应乎天。

**《彖传》：**大畜，刚健而笃实，光辉日日更新。虞翻说，刚健指乾，笃实指艮；二爻已经来到五位，利于涉越大川。互体有离、坎，离为日，所以说光辉日新。“它的德性是刚健上行，并且尊尚贤者。”蜀才说，此卦本于大壮。案语说，刚爻从初位上升，成为外卦之主；刚阳居上，所以尊尚贤者。“能够刚健而有所止，是大正之道。”虞翻说，健是乾，止是艮；二、五交换，所以称大正。旧本把这句话读作“能止健”，虞翻认为不对。“不在家中自食而吉，是因为养贤。”虞翻说，二、五交换成家人卦，如今卦体有颐养之象，所以是不在家食而养贤。案语说乾为贤人，艮为宫阙，让贤人居于宫阙之下、不在家中自食，正是养贤之象。“利涉大川，是因为应合于天。”京房说，九二变为九五，卦体形成坎，所以利涉大川；九五处于天位，因此称应乎天。

**《象传》：**天在山中，是大畜卦。向秀说，能止莫过于山，广大莫过于天；天在山中，是大畜之象。天是最大的器物，山具有极强的止性，能够止住大器，所以称大畜。君子因此多记取前人的言论与往昔的行为，以积蓄自己的德行。虞翻说君子指乾；乾为言，震为行，坎为志。乾主万物大始，震在乾前，所以记取前言往行；卦体有颐养之象，因此积蓄德行。

**六爻：**

**初九：** 有危险，适宜停止。王弼说，六四正在畜止初九，不可冒犯；继续前进就有灾危，能够知险而止，才有利。《象传》说有危险而适宜停止，是因为不触犯灾祸。虞翻说，这是九二变正、六四形成坎险之象。

**九二：** 车舆脱落了车腹（或作车辐）。虞翻说，萃卦的坤象征车，也象征腹；坤消而乾成，所以有车腹脱落之象。底本或作“辐”。《象传》说车舆脱落车腹，是因为居中而没有过失。卢氏说乾为车舆。案语解释，“辐”是车轮中心与车轴之间的构件。九二处位不正，上应九五；九五处于畜养盛大之时，止而不使九二上升，所以暂且脱落车辐而停留。等待时机再进退，最终得正，因此无尤。

**九三：** 良马奔驰追逐，利于在艰难中守正，吉祥；每天练习车乘与防卫。虞翻说乾为良马，震为惊走，所以称逐。九二已经变动，九三处于坎中，因此利于艰难守正而吉。离为日；二至五有师卦之象。坎为练习，坤为车舆；乾象征车上的人，震为惊动与防卫，因此说每天练习车乘与防卫。“利于有所前往。”《象传》说利于前往，是因为与上方的心志相合。虞翻说这是与上九相应；九五已经变正，上九发动而成坎，坎为志，所以能与上方合志。

**六四：** 幼牛头角受到约束，大为吉祥。虞翻说艮为童，九五已经变正；萃卦坤为牛。“告”指用木制器具约束牛角。大畜是畜养牲畜之家，担心牛触人伤物；艮为手、为小木，巽为绳，用绳系住小木横置牛角，所以称童牛之告。六四得位而承接九五，因此大吉有喜，喜指九五。《象传》说六四大吉，是有喜庆。侯果说坤为车舆，因此有牛；“牿”是横木，横置牛角以制止抵触。初九想向上进，六四以牿约束它；角受到约束，便不能触犯六四，如同使幼牛没有角，因此大吉有喜。侯果又引《周礼·封人》设置楅衡之制，注家说楅设于角、衡设于鼻，都是为了制止牛抵触。

**六五：** 阉割过的公猪，其牙齿也能受到控制，吉祥。虞翻说九二变动时，坎为猪；经过阉割的猪称豮猪，使它不再伤害事物。三至上有颐卦之象。六五变为阳爻，巽为白、震为出；刚白之爻从颐口中出现，象征牙齿。发动而得位，所以豮豕之牙吉。《象传》说六五之吉有喜庆。虞翻说六五变动而得正，所以有庆。崔觐引《说文》解释“豮”为性情猛烈的猪，俗称剧猪；猪本来刚猛好冲撞，阉割后性情和顺，即使有牙也不足以伤物，说明它受到人的控制。用来比喻九二刚健却失位，如同阉割后的猪，不足为惧；六五能够应而制止它，所以吉而有庆。案语说九二属坎，坎为猪；以阳居阴而失位，正如猪受阉割之象。

**上九：** 何等通达的天衢大道，亨通。虞翻说“何”是承担、当值；衢是四通八达的道路。乾为天，震、艮为道路；震与艮相交，所以称天衢亨通。上九变动，坎为亨。王弼说，上九处于畜养的极点，畜养达到极点便转为亨通。“何”是语气辞，意思是畜养到极点，天道大道便通行无阻。《象传》说天衢大道亨通，是道路大为通行。虞翻说上九据临二阴，乾为天道，震为行，所以大道得以广泛通行。

**本条翻译状态：**本译文覆盖当前录入的《序卦》、卦辞、《彖传》《象传》、初九至上九及所录注文、案语。底本“輿說腹”与“輻”异文并存，分别保留并说明，不擅自统一；“童牛之告/牿”“豮豕”等依注文训释转述。尚未与四库影印页逐字校勘。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷六·颐卦集解", "底本：维基文库《周易集解（李鼎祚）/卷六》，正文行395—493。录入颐卦经文、彖象、六爻及页面所载注文、案语。页面“△”等标记和注文异说照录；尚未与影印本逐字校勘。已附独立 ChatGPT 工作译文；原文仍待影印本逐字校勘。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷六", """《序卦》曰：物畜然後可養，故受之以頤。頤者，養也。
崔覲曰：大畜剛健，輝光日新，可以觀其所養，故言「物畜然後可養」。
(震下艮上)。頤：貞吉。
虞翻曰：晉四之初。與大過旁通。養正則吉。謂三爻之正，五上易位，故「頤貞吉」。反復不衰，與乾、坤、坎、離、大過、小過、中孚同義。故不從臨觀四陰二陽之例。或以臨二之上，兌為口，故有口實也。
觀頤，
虞翻曰：離為目，故「觀頤」，觀其所養也。
自求口實。
虞翻曰：或以大過兌為口，或以臨兌為口。坤為自，艮為求。口實，頤中物，謂其自養。
鄭玄曰：頤中，口車輔之名也。震動於下，艮止於上，口車動而上，因輔嚼物以養人，故謂之頤。頤，養也。能行養，則其幹事，故吉矣。二五離爻，皆得中。離為目，觀象也觀頤，觀其養賢與不肖也。頤中有物曰「口實」。自二至五有二坤，坤載養物，而人所食之物皆存焉。觀其求可食之物，則貪廉之情可別也。
《彖》曰：頤貞吉，養正則吉也。
姚信曰：以陽養陰，動於下，止於上，各得其正，則吉也。
宋衷曰：頤者，所由飲食自養也。君子割不正不食，況非其食乎。是故所養必得賢明，自求口實，必得體宜，是謂「養正」也。
觀頤，觀其所養也。
侯果曰：王者所養，養賢則吉也。
自求口實，觀其自養也。
侯果曰：此本觀卦。初六升五，九五降初，則成頤也。是自求口實，觀其自養。
案：口實，謂頤口中也。實事可言。震，聲也。實物可食。艮，其成也。
天地養萬物，
崔元曰：天，上。地，初也。萬物，眾陰也。天地以元氣養萬物，聖人以正道養賢及萬民，此其聖也。
聖人養賢以及萬民。
虞翻曰：乾為聖人，艮為賢人，頤下養上，故「聖人養賢」。坤陰為民，皆在震上。以貴下賤，大得民，故「以及萬民」。
頤之時大矣哉。
△天地養物，聖人養賢以及萬民，人非頤不生，故大矣。
《象》曰：山下有雷，頤。
劉表曰：山止於上，雷動於下，頤之象也。
君子以慎言語，節飲食。
荀爽曰：雷為號令，今在山下閉藏，故「慎言語」。雷動於上，以陽食陰，艮以止之，故「節飲食」也。言出乎身，加乎民，故慎言語所以養人也。飲食不節，殘賊群生，故節飲食以養物。
初九：舍爾靈龜，觀我朵頤，兇。
虞翻曰：晉離為龜，四之初，故「舍爾靈龜」。坤為我，震為動；謂四失離入坤，遠應多懼，故「兇」矣。
《象》曰：觀我朵頤亦不足貴也。
侯果曰：初本五也。五互體艮，艮為山龜。自五降初，則為頤矣。是「舍爾靈龜」之德，來「觀朵頤」之饌。貪祿致兇。故「不足貴」。
案：朵頤垂下，動之貌也。
六二：顛頤，拂經於丘頤，征兇。
王肅曰：養下曰顛。拂，違也。經，常也。丘，小山，謂六五也。二宜應五，反下養初，豈非顛頤，違常於五也，故曰「拂經於丘」矣。拂丘雖阻常理，養下故謂養賢。上既無應，征必兇矣，故曰「征兇」。
《象》曰：六二征兇，行失類也。
侯果曰：正則失養之類。
六三：拂頤，貞兇。十年勿用，無攸利。
虞翻曰：三失位體剝，不正相應，弒父弒君，故「貞兇」。坤為十年，動無所應，故「十年勿用，無攸利」也。
《象》曰：十年勿用，道大悖也。
虞翻曰：弒父弒君，故大悖也。
六四：顛頤，吉。虎視眈眈，其欲逐逐，無咎。
王弼曰：履得其位，而應於初，以上養下，得頤之義。故曰「顛頤吉」。下交近瀆，則咎矣。故「虎視眈眈」，威而不猛。故「其欲逐逐」，而尚敦實。修此二者，乃得全其吉而無咎矣。觀其自養則養正，察其所養則養賢，頤爻之貴，斯為盛矣。
《象》曰：顛頤之吉，上施光也。
虞翻曰：晉四之初，謂三已變，故「顛頤」。與屯四乘坎馬同義。坤為虎，離為目。眈眈，下視貌。逐逐，心煩貌，坤為吝嗇，坎水為欲，故「其欲逐逐」。得位應初，故「無咎」。謂上已反三成離，故「上施光也」。
六五：拂經，居貞吉，不可涉大川。
虞翻曰：失位，故拂經。無應順上。故「居貞吉」。艮為居也。涉上成坎，乘陽無應，故「不可涉大川」矣。
《象》曰：居貞之吉，順以從上也。
王弼曰：以陰居陽，拂頤之義也。無應於下，而比於上。故宜居貞，順而從上，則吉。
上九：由頤，厲，吉。
虞翻曰：由，自從也。體剝居上，眾陰順承，故「由頤」。失位，故「厲」。以坤艮自輔，故「吉」也。
利涉大川。《象》曰：由頤，厲吉，大有慶也。
虞翻曰：失位，故厲之。五得正成坎，坎為大川，故「利涉大川」。變陽得位，故「大有慶」也。""", chatGptTranslation = """**ChatGPT 翻译（工作译文）**

**《序卦》与卦辞：**《序卦》说：事物积蓄之后，才能得到养育，所以接着是《颐》卦；颐就是养。（震下艮上。）颐卦：守正则吉。虞翻说，此卦由晋卦九四来到初位，与大过卦旁通；养育合乎正道便吉。三爻得正、五上交换，所以称颐贞吉。它的反复变化不衰，与乾、坤、坎、离、大过、小过、中孚的情形同义，因此不采用临、观两卦四阴二阳的通常例式。也有人认为这是临卦九二来到上位，兑为口，所以有口中含物之象。

“观察颐养。”虞翻说离为目，所以是观察颐养，观察它所养育的对象。“自己寻求口中食物。”虞翻说，有人以大过卦的兑为口，也有人以临卦的兑为口；坤为自己，艮为求取。口中之实就是颐口中的食物，指自己养育自己。郑玄说，颐是口腔、颊辅的名称。震在下发动，艮在上静止，口颊开合咀嚼食物以养人，所以称颐，颐就是养。能够实行养育之道，便能成就事务，所以吉。二、五爻属于离，皆得中位；离为目，象征观察。观察颐养，是观察所养之人贤明还是不肖。颐口中有物称为口实。从二至五有两个坤象，坤承载并养育万物，人所食用的东西都包含其中。观察一个人寻求何种食物，就能分辨其贪婪或廉洁。

**《彖传》：**颐卦守正则吉，是因为养育合乎正道。姚信说，以阳养阴，下方发动、上方静止，各得其正，所以吉。宋衷说，颐是饮食自养之道。君子不吃不合正道的食物，更何况是不应当得到的食物？因此所养育的必须是贤明之人，自己寻求口中食物也必须合乎体宜，这才叫养正。“观察颐养，是观察它所养育的对象。”侯果说，君王所养育的人，若是贤者便吉。“自己寻求口中食物，是观察其自我养育。”侯果说，此卦本于观卦；初六升至五位，九五降至初位，便形成颐卦，所以是观察其自养。案语说，口实指颐口之中；“实”也可以指可陈述的实事。震为声音，实物则可以食用；艮象征其成形。

“天地养育万物。”崔元说，天指上位，地指初位，万物指众阴。天地以元气养育万物，圣人以正道养育贤者及百姓，这就是圣人的伟大之处。“圣人养育贤者，并由此推及万民。”虞翻说乾为圣人，艮为贤人；颐卦下方养上方，所以圣人养贤。坤阴为百姓，都在震上；以尊贵者谦下对待卑贱者，便能广泛得到民众，所以养贤进而及于万民。“颐卦的时势意义真是重大啊！”案语说，天地养育万物，圣人养育贤者并推及万民；人若没有颐养便不能生存，所以颐卦意义重大。

**《象传》：**山下有雷，是颐卦。刘表说，山在上静止，雷在下发动，这是颐的象。君子因此谨慎言语，节制饮食。荀爽说，雷象征号令，如今藏在山下，所以要谨慎言语。雷在上方发动，以阳养阴，艮加以节止，所以节制饮食。言语从自身发出，会影响百姓；谨慎言语正是养人。饮食不节会伤害众生，因此节制饮食以养育万物。

**六爻：**

**初九：** 舍弃你的灵龟，却看着我垂涎欲滴，凶险。虞翻说晋卦的离为龟，九四来到初位，所以说舍弃灵龟。坤为我，震为动；九四失去离象而进入坤，远应而多忧惧，所以凶。《象传》说看着我垂涎，也不足以尊贵。侯果说初九原本是九五，九五的互体有艮，艮为山龟；九五下降到初位便成为颐卦。这是舍弃灵龟之德，转而观看垂涎的食物，因贪图俸禄而招致凶险，所以不足尊贵。案语说“朵颐”是口角下垂、不断活动的样子。

**六二：** 颠倒了养育的方向；违背常理而向丘陵求养，前往则凶。王肃说，从上养下称为颠。拂是违背，经是常道；丘是小山，指六五。六二本应与六五相应，却反而向下养初九，这难道不是颠倒养育、违背六五的常道吗？因此说拂经于丘。虽然违背常理而向丘求养，养下也可理解为养贤；但六二上方没有应爻，前往必凶。《象传》说六二前往凶险，是因为行动时失去了同类。侯果说，若求正，便会失去原本养育的同类。

**六三：** 违背颐养之道，即使守正也凶；十年之内不可任用，没有任何利益。虞翻说六三失位，卦体有剥象，又与不正之爻相应，象征弑父、弑君，所以守正也凶。坤为十年，发动后没有应爻，所以十年勿用、无所利。《象传》说十年勿用，是因为道路严重悖逆。虞翻说弑父弑君，所以是大悖。

**六四：** 颠倒地承担养育，吉祥；目光威严而专注，欲求接连不断，没有灾咎。王弼说六四居位得正，并与初九相应，以居上者养育下方，符合颐养之义，所以颠颐吉。若向下交往过于亲狎，就会有咎；因此要像虎视那样专注威严，却不凶猛。欲求虽持续不断，却仍崇尚敦厚真实。做到这两点，才能保全吉祥而无咎。观察其自养，便是养正；考察其所养，便是养贤，颐卦各爻中这一爻最为尊贵。《象传》说颠颐之吉，是上位施予的光明。虞翻说晋卦九四来到初位，指六三已经变动，所以有颠颐之象；与屯卦六四乘坎马的解释相同。坤为虎，离为目；眈眈是向下凝视的样子，逐逐是心意纷扰不断的样子。坤为吝啬，坎水为欲，所以说欲求逐逐。六四得位并与初九相应，因此无咎；上爻已经返回三位而成离，所以说上施光明。

**六五：** 违背通常的养育方式，但安居守正则吉；不可涉越大川。虞翻说六五失位，所以拂经；它没有下方应爻，只能顺从上九，因此居正则吉。艮为居；若涉越而向上，便形成坎险，又乘阳而无应，所以不可涉大川。《象传》说居正之吉，是因为顺从上位。王弼说六五以阴居阳，违背颐养的常理；下方没有应爻，却亲近上九，所以宜于安居守正，顺从上位便吉。

**上九：** 由此得到养育，虽有危险，仍吉祥。虞翻说“由”是依从；上九处在剥卦之上，众阴顺从承接，所以称由颐。上九失位，因此有危险；但以坤、艮之象辅助自身，所以吉。利于涉越大川。《象传》说由颐而有危险却吉祥，是有大庆。虞翻说上九失位，所以有厉；六五得正形成坎，坎为大川，所以利涉大川。变为阳爻而得位，因此大有庆。

**本条翻译状态：**本译文覆盖当前录入的《序卦》、卦辞、《彖传》《象传》、初九至上九及所录注文、案语。对“观颐”“自求口实”“颠颐”“由颐”等关键语句，保留各家不同的卦象和义理解释；原文中的“△”标记及注文归属不作擅自删改。尚未与四库影印页逐字校勘。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷六·大过卦集解", "底本：维基文库《周易集解（李鼎祚）/卷六》，正文行495—574。录入大过卦经文、彖象、六爻及页面所载注文、案语。异体、疑难字和注文异说照录，尚未与影印本逐字校勘。已附独立 ChatGPT 工作译文；原文仍待影印本逐字校勘。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷六", """《序卦》曰：不養則不可動，故受之以大過。
崔覲曰：養則可動，動則過厚，故受之以大過也。
(巽下兌上)。大過：棟橈。
虞翻曰：大壯五之初，或兌三之初。棟橈謂三。巽為長木，稱棟。初上陰柔，本末弱，故「棟橈」也。
利有攸往，亨。
虞翻曰：謂二也。剛過而中，失位無應，利變應五；之外稱往，故「利有攸往」，乃「亨」也。
《彖》曰：大過，大者過也。
虞翻曰：陽稱大，謂二也。二失位，故「大者過也」。
棟橈，本末弱也。
向秀曰：棟橈則屋壞，主弱則國荒。所以橈，由於初上兩陰爻也。初為善始，末是令終。始終皆弱，所以「棟橈」。
王弼曰：初為本而上為末也。
侯果曰：本，君也。末，臣也。君臣俱弱，棟橈者也。
剛過而中，巽而說行，利有攸往，乃亨。
虞翻曰：剛過而中，謂二。說，兌也。故「利有攸往」。大壯震五之初，故「亨」。與遁同義。
大過之時大矣哉。
虞翻曰：國之大事，在祀與戎，藉用白茅。女妻有子，繼世承祀，故「大矣哉」。
《象》曰：澤滅木，大過。
案：兌，澤也。巽，木滅漫也。凡木生近水者楊也。遇澤太過，木則漫滅焉。二五枯楊，是其義。
君子以獨立不懼，遁世無悶。
虞翻曰：君子謂乾初。陽伏巽中，體復一爻，潛龍之德，故稱「獨立不懼」。違則憂之，乾初同義，故「遁世無悶」也。
初六：藉用白茅，無咎。
虞翻曰：位在下稱藉，巽柔白為茅，故「藉用白茅」。失位，咎也。承二過四，應五士夫，故「無咎」矣。
《象》曰：藉用白茅，柔在下也。
侯果曰：以柔處下，履非其正，咎也。茍能絜誠肅恭不怠，雖置羞於地，可以薦奉，況藉用白茅，重慎之至，何咎之有矣。
九二：枯楊生稊，老夫得其女妻，無不利。
虞翻曰，稊，穉也。楊葉未舒稱稊。巽為楊，乾為老，老楊故枯。陽在二也。十二月時，周之二月。兌為雨澤，枯楊得澤復生稊。二體乾老，故稱「老夫」。女妻謂上兌，兌為少女，故曰「女妻」。大過之家，過以相與，老夫得其女妻，故「無不利」。
《象》曰：老夫女妻，過以相與也。
虞翻曰：謂二過初與五，五過上與二。獨大過之爻得過其應，故「過以相與也」。
九三：棟橈，兇。《象》曰：棟橈之兇，不可以有輔也。
虞翻曰：本末弱，故橈。輔之益橈，故「不可以有輔」。陽以陰為輔也。
九四：棟隆，吉。有他吝。
虞翻曰：隆，上也。應在於初。已與五，意在於上。故「棟隆，吉」。失位動，入險而陷於井，故「有他吝」。
《象》曰：棟隆之吉，不橈乎下也。
虞翻曰：乾為動直，遠初近上，故「不橈下也」。
九五：枯楊生華，老婦得其士夫，無咎無譽。
虞翻曰：陽在五也。夬三月時，周之五月。枯楊得澤，故生華矣。老婦謂初，巽為婦，乾為老，故稱「老婦」也。士夫謂五。大壯震為夫，兌為少，故稱「士夫」。五過二，使應上。二過五，使取初。五得位，故「無咎」。陰在二多譽，今退伏初，故「無譽」。體姤淫女，故「過以相與」。使應少夫。《象》曰：「亦可醜也。」舊說以初為女妻，上為老婦，誤矣。馬君亦然。荀公以初陰失正，當變，數六，為女妻。二陽失正，數九，為老夫。以五陽得正位，不變，數七，為士夫。上陰得正，數八，為老婦。此何異俗說也。悲夫學之難，而以初本為小，反以上末為老。後之達者，詳其義焉。
《象》曰：枯楊生華，何可久也。老婦士夫，亦可醜也。
虞翻曰：乾為久，枯而生華，故不可久也。婦體姤淫，故「可醜」也。
上六：過涉滅頂，兇，無咎。
虞翻曰：大壯震為足，兌為水澤。震足沒水，故「過涉」也。頂，首也。乾為頂，頂沒兌水中，故「滅頂兇」。乘剛，咎也。得位，故「無咎」。與滅耳同義也。
《象》曰：過涉之兇，不可咎也。
《九家易》曰：君子以禮義為法，小人以畏慎為宜。至於大過之世，不復遵常，故君子犯義，小人犯刑。而家家有誅絕之罪。不可咎也。大過之世，君子遜遁，不行禮義，謂當不義則爭之，若比幹諫而死是也。桀紂之民，可比屋而誅。上化致然，亦不可咎。曾子曰：上失其道，民散久矣。如得其情，則哀矜而勿喜，是其義也。""", chatGptTranslation = """**ChatGPT 翻译（工作译文）**

《序卦》说：不能有所养育，就不能有所行动，因此接着是大过卦。崔觐说：有所养育才能行动，行动时又过于丰厚，所以接着是大过。

（巽下兑上。）大过：栋梁弯曲。虞翻说：此卦的卦变有大壮五爻来到初位等说法；“栋橈”指第三爻。巽为高长之木，象征栋梁；初、上两端阴柔，本末薄弱，所以栋梁弯曲。

“利于有所前往，亨通。”虞翻说：指九二。刚爻过盛而居中，但失位且无应，适宜变动而与九五相应；向外称为往，因此利于前往而亨通。

《彖传》说：大过，就是阳刚过盛。“栋梁弯曲”，是因为本末柔弱。向秀说：栋梁弯曲房屋便会毁坏，君主柔弱国家便会荒乱；原因在初、上两阴爻，本末都弱。王弼说初爻为本、上爻为末。侯果说本象征君主，末象征臣下，君臣都弱便如栋梁弯曲。

“刚爻过盛而居中，巽顺而喜悦地行动，利于前往，所以亨通。”虞翻说刚过而中指九二，喜悦指兑；大壮卦震五来到初位，所以亨通，其义与遁卦相同。

“大过之时，意义真是重大啊！”虞翻说：国家大事在于祭祀与战争；以白茅承垫祭品，女子出嫁生子、继承世系而主持祭祀，都是关系重大之事。

《象传》说：泽水淹没树木，这就是大过。案语说兑为泽，巽为木；泽水过盛便淹没树木，二、五爻的枯杨之象也由此而来。君子因此独立而不畏惧，避世而不忧闷。虞翻说君子指乾初，阳气潜伏在巽中，具有复卦一爻初生的潜龙之德，所以独立不惧；即使避世，也不因此忧闷。

**六爻**

**初六：** 用白茅草垫在下面，没有灾咎。虞翻说处于下位称“藉”，巽柔而白象征茅草；初六失位本有咎，但承接九二、与九五相应，所以无咎。侯果说柔爻居下而不正，本来有咎；若能洁净诚敬、谨慎不怠，即使把祭品直接放在地上也可奉献，何况用白茅垫衬，慎重至极，哪里会有过失？

**九二：** 枯杨生出嫩芽，老年男子娶得年轻妻子，没有不利。虞翻说“稊”是幼芽；巽为杨木，乾为老，故称枯杨。九二为阳爻，时当周历二月；兑为雨泽，枯杨得泽而生芽。九二体乾为老夫，上兑为少女，所以称年轻妻子。大过之时，以过盛相互给予，故无不利。《象传》说老夫与少女相配，是以非常之道相互交合。虞翻说九二越过初六而与九五相应，九五越过上六而与九二相应，这是大过卦爻位相互越位相应的特殊情形。

**九三：** 栋梁弯曲，凶险。《象传》说栋梁弯曲之所以凶险，是因为不能得到辅助。虞翻说本末柔弱所以弯曲；再加辅助反而会使它更弯，因此不可辅。阳爻以阴爻为辅助。

**九四：** 栋梁隆起，吉祥；另有变故则有困难。虞翻说“隆”是向上。九四与初六相应，却与九五相邻，心意趋向上方，所以栋梁隆起而吉。若失位发动而进入险陷之中，便有其他困难。《象传》说栋梁隆起而吉，是因为它没有向下弯曲。虞翻以乾为刚直、远离初六而接近上位解释。

**九五：** 枯杨开花，老妇得到年轻男子为夫，没有灾咎，也没有声誉。虞翻说九五为阳，时当周历五月；枯杨得泽而开花。老妇指初六，巽为妇、乾为老；年轻男子指九五，震为夫、兑为少。九五得位，所以无咎；阴爻退伏于初，故无誉。虞翻批评旧说把初六当作年轻女子、上六当作老妇，认为这是错误的；他也逐一反驳马融、荀爽的解释，并感叹治学辨义之难。《象传》说枯杨开花，怎能长久？老妇配年轻男子，也可谓可羞。虞翻说枯木开花不能持久；老妇之象又带有姤卦淫女之义，所以可羞。

**上六：** 徒步涉水过深，水淹至头顶，凶险，但没有咎害。虞翻说大壮的震为足，兑为水泽，脚没入水中所以称过涉；乾为头顶，头没入兑水所以灭顶。乘刚本有咎，但上六得位，所以说无咎。《象传》说过涉之凶，不可归咎于此爻。《九家易》以大过之世礼法失常、上下失教解释此象，并引曾子之言说明上位者失道会使百姓离散，执法者应哀矜而非欣喜。

**本条翻译状态：**覆盖当前录入的《序卦》、卦辞、《彖传》《象传》、初六至上六及所录注文、案语。枯杨生稊、生华、老夫女妻、老妇士夫等处保留注家不同象数解释；“大过之时”的政治礼制引申按原注转述。尚未与四库影印页逐字校勘。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷六·坎卦集解", "底本：维基文库《周易集解（李鼎祚）/卷六》，正文行576—692。录入坎卦经文、彖象、六爻及页面所载注文、案语。页面异体、古字及引书标记照录，尚未与影印本逐字校勘。已附独立 ChatGPT 工作译文；原文仍待影印本逐字校勘。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷六", """《序卦》曰：物不可以終過，故受之以坎。坎者，陷也。
崔覲曰：大過不可以極，極則過涉滅頂，故曰「物不可以終過，故受之以坎」也。
(坎下坎上)。坎：習坎，有孚，唯心亨，行有尚。
虞翻曰：乾二五之坤，與離旁通。於爻，觀上之二。習，常也。孚，信，謂二五。水行往來，朝宗於海，不失其時，如月行天，故習坎為孚也。
維心亨，
虞翻曰：坎為心。乾二五旁行流坤，陰陽會合，故「亨」也。
行有尚。
虞翻曰：行謂二，尚謂五也。二體震，為行動得正應五，故「行有尚，往有功也」。
《彖》曰：習坎，重險也。
虞翻曰：兩象也。天險地險，故曰「重險也」。
水流而不盈，
荀爽曰：陽動陰中，故「流」。陽陷陰中，故「不盈」也。
陸績曰：水性趨下，不盈溢崖岸也。月者水精，月在天，滿則虧，不盈溢之義也。
行險而不失其信。
荀爽曰：謂陽來為險，而不失中。中稱信也。
虞翻曰：信，謂二也。震為行。水性有常，消息與月相應，故「不失其信」矣。
維心亨，乃以剛中也。
侯果曰：二五剛而居中，則「心亨」也。
行有尚，往有功也。
虞翻曰：功謂五。二動應五，故「往有功也」。
天險不可升也，
虞翻曰：謂五在天位。五從乾來，體屯難。故「天險不可升也」。
地險山川丘陵也。
虞翻曰：坤為地，乾二之坤，故曰「地險」。艮為山，坎為川；半山稱丘，丘下稱陵。故曰「地險山川丘陵也」。
王公設險以守其國，
虞翻曰：王公大人謂乾五。坤為邦。乾二之坤成坎險，震為守，有屯難象。故「王公設險以守其國」。離言王用出征以正邦是也。
案：九五，王也，六三，三公也。艮為山城，坎為水也。王公設險之象也。
險之時用大矣哉。
王肅曰：守險以德，據險以時，成功大矣。
《象》曰：水洊至，習坎。君子以常德行習教事。
陸績曰：洊，再。習，重也。水再至而溢，通流不舍晝夜。重習相隨以為常，有似於習，故君子象之。以常習教事，如水不息也。
虞翻曰：君子謂乾。五在乾，稱大人；在坎，為君子。坎為習、為常，乾為德，震為行，巽為教令，坤為事，故「以常德行習教事」也。
初六：習坎，入於坎窞，兇。
幹寶曰：窞，坎之深者也。江河淮濟，百川之流行乎地中，水之正也。及其為災，則泛溢平地，而入於坎窞，是水失其道也。刑獄之用，必當於理，刑之正也。及其不平，則枉濫無辜，是法失其道也。故曰「入於坎窞，兇」矣。
《象》曰：習坎入坎，失道兇也。
虞翻曰：習，積也。位下，故習坎為人。坎中小穴稱窞。上無其應，初二失正，故曰「失道兇」矣。
九二：坎有險，求小得。
虞翻曰：陽陷陰中，故「有險」。據陰有實，故「求小得」也。
《象》曰：求小得，未出中也。
荀爽曰：處中而比初三，未足為援。雖求小得，未出於險中。
六三：來之坎坎，險且枕，入於坎窞，勿用。
虞翻曰：坎在內，稱來，在坎終，故「來之坎坎」。枕，止也。艮為止。三失位乘二，則險。承五隔四，故「險且枕」。入於坎窞，體師三輿，故「勿用」。
《象》曰：來之坎坎，終無功也。
幹寶曰：坎，十一月卦也。又失其位，喻殷之執法者，失中之象也。來之坎者，斥周人觀釁於周也。枕，安也。險且枕者，言安忍以暴政加民，而無哀矜之心，淫刑濫罰，百姓無所措手足，故曰「來之坎坎，終無功也」。
六四：樽酒簋，貳用缶。
虞翻曰：震主祭器，故有「樽簋」。坎為酒。簋，黍稷器。三至五，有頤口象。震獻在中，故為「簋」。坎為木，震為足；坎酒在上，樽酒之象。貳，副也。坤為缶，禮有副樽，故「貳用缶」耳。
內約自牖，終無咎。
虞翻曰：坎為內也。四陰小，故「約」。艮為牖，坤為戶，艮小光照戶牖之象；貳用缶。故「內約自牖」。得位承五，故「無咎」。
崔覲曰：於重險之時，居多懼之地，近三而得位，比五而承陽。修其絜誠，進其忠信，則雖祭祀省薄，明德惟馨。故曰「樽酒簋，貳用缶」。內約，文王於紂時行此道，從羑裏內約，卒免於難，故曰「自牖，終無咎也」。
《象》曰：樽酒簋，剛柔際也。
虞翻曰：乾剛坤柔，震為交，故曰「剛柔際也」。
九五：坎不盈，禔既平，無咎。
虞翻曰：盈，溢也。艮為止，謂水流而不盈。坎為平。禔，安也。艮止坤安，故「禔既平」。得位正中，故「無咎」。
《象》曰：坎不盈，中未光大也。
虞翻曰：體屯五中，故未光大也。
上六：系用徽纆，寘於叢棘，三歲不得，兇。
虞翻曰：徽纆，黑索也。觀巽為繩，艮為手，上變入坎。故「系用徽纆」。寘，置也。坎多心，故「叢棘」。獄外種九棘，故稱「叢棘」。二變則五體剝，剝傷坤殺，故「寘於叢棘」也。不得，謂不得出獄。艮止坎獄。乾為歲，五從乾來，三非其應，故曰「三歲不得，兇」矣。
《象》曰：上六失道，兇三歲也。
《九家易》曰：坎為叢棘，又為法律。
案《周禮》：王之外朝，左九棘，右九棘，面三槐，司寇公卿議獄於其下。害人者，加明刑。任之以事。上罪三年而舍，中罪二年而舍，下罪一年而舍也。
案：坎於木堅而多心，叢棘之象也。坎下巽爻，巽為繩直，「系用徽纆」也。馬融雲：徽纆，索也。劉表雲：三股為徽，兩股為纆，皆索名。以系縛其罪人矣。""", chatGptTranslation = """**ChatGPT 翻译（工作译文）**

《序卦》说：事物不能一直过度下去，所以接着是坎卦。坎就是陷落。

（坎下坎上。）坎：重重险陷，有诚信；只要内心通达，行动便会受到嘉许。虞翻说乾卦二、五爻来到坤中，与离卦旁通。“习”是常，“孚”是信，指二、五爻。水往来流动，按时汇入大海，如同月亮运行于天，所以重坎象征诚信。坎为心，阴阳会合，所以内心亨通。行动指九二，嘉许指九五；九二发动而正应九五，所以行动有功。

《彖传》说：重坎是两重险陷。虞翻说上下两个坎象，象征天险与地险。水流动而不盈满。荀爽说阳爻在阴中发动，所以流；阳陷于阴中，所以不盈。陆绩说水性趋下，不会漫溢堤岸；月为水精，月圆之后转亏，也是不盈满之义。行于险中而不失诚信。荀爽说阳来成为险，却仍不失中位；居中便称信。虞翻说信指九二，震为行动；水性有常，盈亏与月相应，所以不失其信。

“内心亨通，是因为刚健而居中。”侯果说九二、九五皆刚而居中。“行动受到嘉许，是因为前往有功。”虞翻说功指九五，九二发动而应九五。天险不可攀登。虞翻说九五处天位，从乾而来，卦体有屯难之象。地险就是山川丘陵。虞翻以坤为地、艮为山、坎为川，并解释丘陵的名称。王公设置险阻来守卫国家。虞翻说乾五象征王公，坤象征邦国，坎为险、震为守，所以王公设险守国。案语以九五为王、六三为三公，艮为山城、坎为水，说明设险之象。险难之时的作用真是重大。王肃说以德守险、依时据险，才能成就大功。

《象传》说：水接连而至，这是重坎。君子因此恒常其德行，反复熟习教化事务。陆绩说水连续到来而不舍昼夜，重习相随成为常态，君子应效法水流不息。虞翻以乾为君子、坎为习与常、乾为德、震为行、巽为教令、坤为事解释此句。

**六爻**

**初六：** 重重陷险，进入深坑，凶险。干宝说“窞”是坎穴深处。江河百川在地中流行是水的正常状态；泛滥平地、陷入深坑则是水失其道。刑罚依法施行是正道，枉法滥刑、冤及无辜则是法失其道，所以凶。《象传》说重坎而入坎，是因为失道而凶。虞翻说“习”是积累；初六处下位，上无正应，初、二失正，所以失道。

**九二：** 坎中仍有危险，求取小事可以略有所得。虞翻说阳陷阴中，所以有险；居于阴爻之上而有所得，所以求小得。《象传》说求小得，是因为尚未脱离险中。荀爽说九二虽居中，却被初、三夹持，不能得到足够援助。

**六三：** 来去都是重重险陷，危险而且停滞，进入深坑，不可采取行动。虞翻说坎在内称来，处于坎卦终端称之；“枕”是停止，艮为止。六三失位而乘九二，故险；承接九五却被九四隔开，所以危险且停滞。干宝从殷周政治解释此爻，认为执法者失中、以暴政滥刑对待百姓，最终不能成功。《象传》说来去皆坎，最终没有功效。

**六四：** 一樽酒、一簋黍稷，另以瓦缶作副器。虞翻说震为祭器，坎为酒；簋是盛黍稷的器皿。坤为缶，礼制中有副樽，所以以缶为副。祭礼从简，内心约束而由窗牖进献，最终没有咎害。虞翻说坎为内，六四阴柔而简约；艮为窗牖、坤为门户，得位而承接九五，所以无咎。崔觐说身处重险之时，若能修洁诚信、尽忠守信，即使祭品简薄，德行也会馨香；文王被囚羑里时行此道，最终免难。《象传》说樽酒簋与刚柔相交有关。虞翻以乾刚、坤柔、震为交解释。

**九五：** 坎水未满，安定之后地面平整，没有咎害。虞翻说盈是溢出；艮为止，水流而不溢。坎为平，禔是安；艮止而坤安，所以安定平正。九五得位居中，故无咎。《象传》说坎水未满，是因为中道的光明尚未广大。虞翻说卦体仍有屯五之象，所以尚未光大。

**上六：** 用黑色绳索捆系，置于荆棘丛生之处，三年不得脱出，凶险。虞翻说徽纆是黑色绳索；巽为绳，艮为手，故有捆系之象。坎多心，象征丛棘；古代狱外种植九棘，所以称丛棘。上六不得出狱，乾为年，三爻失应，故三年不得。《象传》说上六失道，所以凶险三年。《九家易》说坎为丛棘，也为法律。案语引《周礼》记王之外朝种棘、槐并议狱之制，又说明徽纆是绳索，马融、刘表分别解释其股数与名称。

**本条翻译状态：**覆盖当前录入的《序卦》、卦辞、《彖传》《象传》、初六至上六及所录注文、案语。保留“习坎”“窞”“樽酒簋，贰用缶”“禔既平”“徽纆”等词的训释差异；涉及古代刑狱制度的内容仅按注文转述。尚未与四库影印页逐字校勘。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷六·离卦集解", "底本：维基文库《周易集解（李鼎祚）/卷六》，正文行694—784。录入离卦经文、彖象、六爻及页面所载注文。原页面“百谷草木丽乎也”等疑似缺字或讹字不作推补，照底本保留；尚未与影印本逐字校勘。已附独立 ChatGPT 工作译文；原文仍待影印本逐字校勘。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷六", """《序卦》曰：陷必有所麗，故受之以離。離者，麗也。
崔覲曰：物極則反。坎雖陷於地，必有所麗於天，而受之以離也。
(離下離上)。離：利貞，亨。
虞翻曰：坤二五之乾，與坎旁通。於爻，遯初之五，柔麗中正，故「利貞，亨」。
畜牝牛，吉。
虞翻曰：畜，養也。坤為牝牛。乾二五之坤成坎，體頤養象。故「畜牝牛，吉」。俗說皆以離為牝牛，失之矣。
《彖》曰：離，麗也。
荀爽曰：陰麗於陽，相附麗也。亦為別離，以陰隔陽也。離者，火也。托於木，是其附麗也。煙焰飛升，炭灰降滯，是其別離也。
日月麗乎天。
虞翻曰：乾五之坤成坎，為月；離為日。日月麗天也。
百谷草木麗乎也。
虞翻曰：震為百谷，巽為草木，坤為地。乾二五之坤成坎。震體屯。屯者，盈也。盈天地之間者唯萬物，萬物出震，故「百谷草木麗乎地」。
重明以麗乎正，乃化成天下。
虞翻曰：兩象，故重明。正謂五陽。陽變之坤來化乾，以成萬物，謂離日「化成天下」也。
柔麗乎中正，故亨。
虞翻曰：柔謂五陰，中正謂五伏陽。出在坤中，畜牝牛。故「中正」而「亨」也。
是以畜牝牛吉也。
荀爽曰：牛者，土也。生土於火。離者，陰卦。牝者，陰性，故曰「畜牝牛吉」矣。
《象》曰：明兩作離，
虞翻曰：兩謂日與月也。乾五之坤成坎，坤二之乾成離，離坎日月之象，故「明兩作離」。作，成也。日月在天，動成萬物，故稱作矣。或以日與火為明。兩作也。
大人以繼明照於四方。
虞翻曰：陽氣稱大人，則乾五大人也。乾二五之光，繼日之明。坤為方，二五之坤。震東兌西，離南坎北，故曰「照於四方」。
初九：履錯然，敬之，無咎。
荀爽曰：火性炎上，故初欲履錯於二。二為三所據。故「敬之」則「無咎」矣。
《象》曰：履錯之敬，以辟咎也。
王弼曰：錯然，敬慎之貌也。處離之始，將進其盛，故宜慎所履。以敬為務，辟其咎也。
六二：黃離，元吉。《象》曰：黃離元吉，得中道也。
侯果曰：此本坤爻，故雲「黃離」。來得中道，所以「元吉」也。
九三：日昃之離，
荀爽曰：初為日出，三為日昃，以喻君道衰也。
不鼓缶而歌，則大耋之嗟，兇。
《九家易》曰：鼓缶者以目下視。離為大腹，瓦缶之象，謂不取二也。歌者口仰向上，謂兌為口，而向上取五也。日昃者，向下也。今不取二，而上取五，則上九耋之陽稱大也。嗟者，謂上被三奪五，憂嗟窮兇也。火性炎上，故三欲取五也。
《象》曰：日昃之離，何可久也。
《九家易》曰：日昃當降，何可久長。三當據二，以為鼓缶，而今與四同取於五，故曰「不鼓缶而歌」也。
九四：突如，其來如，焚如，死如，棄如。
荀爽曰：陽升居五，光炎宣揚，故「突如」也。陰退居四，灰炭降墜，故「其來如」也。陰以不正，居尊乘陽。歷盡數終，天命所誅。位喪民畔，下離所害。故「焚如」也。以離入坎，故「死如」也。火息灰損，故「棄如」也。
《象》曰：突如，其來如，無所容也。
《九家易》曰：在五見奪，在四見棄，故「無所容也」。
六五：出涕沱若，
荀爽曰：六五陰柔，退居於四，出離為坎，故「出涕沱若」。而下，以順陰陽也。
戚嗟若，吉。
虞翻曰：坎為心，震為聲，兌為口，故「戚嗟若」。動而得正，尊麗陽，故「吉」也。
《象》曰：六五之吉，離王公也。
《九家易》曰：戚嗟順陽，附麗於五，故曰「離王公也」。陽當居五，陰退還四，五當為王。三則三公也。四處其中，附上下矣。
上九：王用出征，有嘉折首，獲匪其醜，無咎。
虞翻曰：王謂乾。乾二五之坤成坎，體師象；震為出，故「王用出征」。首謂坤。二五來折乾，故「有嘉折首」。醜，類也。乾征得坤陰類，乾陽物，故「獲非其醜，無咎」矣。
《象》曰：王用出征，以正邦也。
虞翻曰：乾五出征坤，故「正邦也」。""", chatGptTranslation = """**ChatGPT 翻译（工作译文）**

《序卦》说：陷入之后必然有所依附，所以接着是离卦。离就是附丽、依附。崔觐说：事物发展到极点便会反转；坎虽陷于地，也必有所依附于天，因此接着是离。

（离下离上。）离：适宜守正，亨通。虞翻说坤的二、五爻来到乾中，与坎旁通；柔爻依附于中正之位，所以利贞而亨。“畜养母牛，吉祥。”虞翻说畜是养，坤为母牛，卦体具有颐养之象，所以畜养母牛吉；他认为把离卦直接解释为母牛是不恰当的。

《彖传》说：离，就是附丽。荀爽说阴依附于阳，是相互依附；阴隔开阳，也可表示分离。离为火，火寄托于木，是依附；烟焰上升、炭灰下沉，则是分离。日月依附于天。虞翻说乾五来到坤而成坎为月，离为日，所以日月附丽于天。原文此处作“百谷草木丽乎也”，照底本保留；虞翻注文则解释为“百谷草木丽乎地”：震为百谷，巽为草木，坤为地，万物生于震而充满天地之间。这里正文与注文存在文字差异，不据注文擅自改正文。

“上下两重光明依附于正道，于是教化成就天下。”虞翻说上下两离，所以称重明；正指九五，阳变而化成万物，如离日照临天下。“柔顺依附于中正，所以亨通。”虞翻说柔指六五，中正指伏藏的阳爻，柔处坤中而畜养牝牛，所以中正而亨。荀爽说牛属土，火能生土；离为阴卦，牝属阴性，所以畜养母牛吉。

《象传》说：光明两次升起，这就是离。虞翻说“两”指日与月；坎离具有日月之象，故称明两作离。“作”是形成。大人因此延续光明，照耀四方。虞翻说阳气称大人，乾五为大人；坤为方，震东、兑西、离南、坎北，所以光明照耀四方。

**六爻**

**初九：** 行为交错纷杂，应当恭敬谨慎，没有咎害。荀爽说火性向上，初九想要向上接近六二，但六二被九三所据，因此须谨慎。《象传》说行为错杂而能恭敬，是为了避开灾咎。王弼说“错然”是谨慎恭敬的样子；处于离卦初始、将要走向盛大，更应谨慎自己的行为。

**六二：** 黄色的光明，极为吉祥。《象传》说黄离元吉，是因为得到了中道。侯果说此爻本为坤爻，所以称黄；来到中位而得中道，因此大吉。

**九三：** 太阳偏西时的光明；若不敲击瓦缶而歌唱，便会发出老年人的悲叹，凶险。荀爽说初爻象征日出，三爻象征日偏西，用以比喻君道衰微。《九家易》说鼓缶是向下看，离为大腹，象征瓦缶；歌唱则口向上，兑为口。日偏西本应向下，如今不取六二而向上取九五，所以有老耋之阳的忧叹。《象传》说日偏西的光明，怎能长久？《九家易》说日偏西应当下降，不能长久；九三本应据六二，却与九四一同争取九五，所以有此爻辞。

**九四：** 突然来到，来势猛烈，如同焚烧、死亡、被弃置。荀爽说阳爻上升居五，光焰宣扬，所以突然；阴爻退居四，如灰炭坠落。阴以不正之位居尊而乘阳，历数终尽，天命将其诛除；离入坎，所以说死；火息灰落，所以说弃。《象传》说突然来到、来势如此，是因为没有容身之处。《九家易》说在五位被夺，在四位又被弃，所以无所容。

**六五：** 眼泪涟涟流下。荀爽说六五阴柔，退居于四，由离变坎，所以泪水涟涟；并且顺应阴阳变化。忧惧叹息，吉祥。虞翻说坎为心，震为声，兑为口，所以有忧叹之象；发动而得正，尊位依附于阳，因此吉。《象传》说六五之所以吉，是因为它依附于王公。《九家易》说六五忧叹而顺从阳，依附于五；阳应居五为王，三为三公，四处在其间而依附上下。

**上九：** 君王出兵征伐，取得美善战果，斩获首恶，俘获的并非同类之众，没有咎害。虞翻说王指乾；乾二、五来到坤而成坎，具有师卦之象，震为出，所以君王出征。首指坤，二、五来折乾，所以说折首。“醜”是同类；乾阳征伐坤阴，所获并非阳类，所以无咎。《象传》说君王出征，是为了端正邦国。虞翻说乾五出征坤阴，因此称正邦。

**本条翻译状态：**覆盖当前录入的《序卦》、卦辞、《彖传》《象传》、初九至上九及所录注文。特别保留“百谷草木丽乎也”正文与注文“百谷草木丽乎地”的差异，不补改底本文字；对“黄离”“日昃之离”“突如其来如”等按注家解释转述。尚未与四库影印页逐字校勘。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷七·咸卦集解", "底本：维基文库《周易集解（李鼎祚）/卷七》，正文行114—192。录入咸卦经文、《彖》《象》及页面所载韩康伯、虞翻、郑玄、蜀才、王肃、荀爽、陆绩、崔觐等注文。原页面字形与注文次序照录；尚未与四库影印本逐字校勘。译文尚未完成。来源：https://zh.wikisource.org/zh-hans/周易集解/卷七", """《序卦》曰：有天地然后有万物，有万物然后有男女，有男女然后有夫妇，有夫妇然后有父子，有父子然后有君臣，有君臣然后有上下，有上下然后礼义有所错。
韩康伯曰：言咸卦之义也。咸柔上而刚下，感应以相与。夫妇之象，莫美乎斯。人伦之道，莫大夫妇。故夫子殷勤深述其义，以崇人伦之始，而不系之离也。先儒以干至离为上经，天道也；咸至未济为下经，人事也。夫易六画成卦，三材必备，错综天人，以效变化，岂有天道人事偏于上下哉。斯盖守文而不求义，失不远矣。
(艮下兑上)。咸：亨，利贞，取女吉。
虞翻曰：咸，感也。坤三之上成女，干上之三成男，乾坤气交以相与。止而说，男下女。故“通利贞，取女吉”。
郑玄曰：咸，感也。艮为山。兑为泽。山气下，泽气上，二气通而相应，以生万物，故曰“咸”也。其于人也。嘉会礼通，和顺于义，干事能正。三十之男，有此三德，以下二十之女，正而相亲说，取之则吉也。
《彖》曰：咸，感也。柔上而刚下，二气感应以相与。
蜀才曰：此本否卦。案：六三升上，上九降三，是柔上而刚下，二气交感，以相与也。
止而说，男下女，是以亨利贞，取女吉也。
王肃曰：山泽以气通，男女以礼感。男而下女，初婚之所以为礼也。通义正，取女之所以为吉也。
天地感而万物化生，
荀爽曰：干下感坤，故万物化生于山泽。
陆绩曰：天地因山泽孔窍，以通其气，化生万物也。
圣人感人心而天下和平。
虞翻曰：干为圣人；初四易位成既济；坎为心、为平，故“圣人感人心而天下和平”。此保合太和，品物流形也。
观其所感，而天地万物之情可见矣。
虞翻曰：谓四之初，以离日见天，坎月见地，悬象著明，万物见离，故“天地万物之情可见”也。
《象》曰：山上有泽，咸。
崔觐曰：山高而降，泽下而升。山泽通气，咸之象也。
君子以虚受人。
虞翻曰：君子谓否干。干为人，坤为虚，谓坤虚三受上，故“以虚受人”。艮山在地下为谦，在泽下为虚。
初六：咸其母。《象》曰：咸其母，志在外也。
虞翻曰：母，足大指也。艮为指。坤为母，故“咸其母”。失位远应，之四得正，故“志在外”。谓四也。
六二：咸其腓，凶。居吉。《象》曰：虽凶居吉，顺不害也。
崔觐曰：腓，脚膊。次于母，上二之象也。得位居中，于五有应，若感应相与，失艮止之礼，故“凶”。居而承比于三，顺止而随于当礼，故“吉”也。
九三：咸其股，执其随，往咎。
崔觐曰：股髀而次于腓，上三之象也。刚而得位，虽欲感上，以居艮极，止而不前，二随于已，志在所随，故“执其随”。下比二也，而遂感上，则失其正义，故“往吝”穷也。
《象》曰：咸其股，亦不处也。志在随人，所执下也。
虞翻曰：巽为股，谓二也。巽为随，艮为手，故称“执”。三应于上，初四已变历险，故“往吝”。巽为处女也，男已下女，以艮阳入兑阴，故“不处也”。凡士与女未用，皆称“处”矣。志在于二，故“所执下也”。
九四：贞吉，悔亡。憧憧往来，朋从尔思。
虞翻曰：失位悔也。应初动得正，故“贞吉”而“悔亡”矣。憧憧，怀思虑也。之内为来，之外为往。欲感上，隔五；感初，隔三，故“憧憧往来”矣。兑为朋，少女也。艮初变之四，坎心为思，故曰“朋从尔思”也。
《象》曰：贞吉悔亡，未感害也。
虞翻曰：坤为害也。今未感坤，初体遁弑父，故曰“未感害”也。
憧憧往来，未光大也。
虞翻曰：未动之离，故未光大也。
九五：咸其脢，无悔。
虞翻曰：脢，夹脊肉也。谓四已变，坎为脊，故“咸其脢”。得正，故“无悔”。
《象》曰：咸其脢，志末也。
案：末，犹上也。四感于初，三随其二，五比于上，故“咸其脢”。志末者，谓五志感于上也。
上六：咸其辅颊舌。
虞翻曰：耳目之间称辅颊。四变之目，坎为耳，兑为口舌。故曰“咸其辅颊舌”。
《象》曰：咸其辅颊舌，媵口说也。
虞翻曰：媵，送也。不得之三，山泽通气，故“媵口说也”。
""", chatGptTranslation = """**ChatGPT 翻译（逐段工作译文）**

《序卦》说：有天地之后才有万物，有万物之后才有男女，有男女之后才有夫妇，有夫妇之后才有父子，有父子之后才有君臣，有君臣之后才有上下等级，有上下等级之后，礼义才有施行的依据。

韩康伯说：这是说明咸卦的意义。咸卦柔爻居上、刚爻居下，阴阳感应而彼此亲和。夫妇的象，没有比这更美好的；人伦之道，也没有比夫妇关系更重要的。因此孔子恳切地阐述它的意义，以尊崇人伦的开端，而不把它系属于离卦。先儒认为乾至离属于上经，讲天道；咸至未济属于下经，讲人事。但《易》由六画成卦，天地人三才都必须具备，交错综合天与人以表现变化，怎会有天道、人事偏分在上下经的道理？这只是拘守文字而不探求义理，错误并不小。

咸卦下艮上兑。咸：亨通，利于守正，娶女吉祥。

虞翻说：咸就是感应。坤卦第三爻上升，形成女子之象；乾卦上爻下降到第三位，形成男子之象。乾坤之气交合而相互亲和。艮止而兑悦，男子居于女子之下，所以亨通、利于守正，娶女吉祥。

郑玄说：咸就是感应。艮为山，兑为泽；山气下降，泽气上升，二气相通并彼此感应，从而生养万物，所以称咸。用于人事，就是男女以礼相会，和顺合义，办事能够端正。三十岁的男子具备这三种德行，以谦下态度迎娶二十岁的女子，彼此端正而相亲悦，娶她便吉祥。

《彖传》说：咸就是感应。柔爻上升、刚爻下降，阴阳二气感应而相互亲和。

蜀才说：此卦本于否卦。六三上升到上位，上九下降到三位，这就是柔上刚下，二气交感相亲。

止而喜悦，男子谦下于女子，因此亨通、利于守正，娶女吉祥。

王肃说：山泽以气相通，男女以礼相感。男子谦下女子，是婚姻礼仪的开端；通达义理而端正，是娶女吉祥的原因。

天地相互感应，万物便化育生成。

荀爽说：乾在下感应坤，所以万物在山泽之间化育生成。

陆绩说：天地借助山泽孔窍来沟通气息，从而化育万物。

圣人感通人心，天下便和平。

虞翻说：乾象征圣人；初爻与四爻交换位置，形成既济卦；坎象征心，也象征平和，所以说圣人感通人心而天下和平。这就是保合太和、万物各自成形的意思。

观察事物感应的情形，天地万物的真实情状便可以看见。

虞翻说：这里指四爻移到初位。离象征太阳而显现于天，坎象征月亮而显现于地；日月悬挂、光明显著，万物显现于离象，所以天地万物的情状可以看见。

《象传》说：山上有泽，这就是咸卦。

崔觐说：山高而气下降，泽低而气上升，山泽之气相通，就是咸卦的象。

君子因此虚心接纳他人。

虞翻说：君子指否卦中的乾。乾象征人，坤象征虚；坤虚而接受上爻，所以说虚心接纳他人。艮山处于地下时是谦卦之象，处于泽下则是虚的象。

初六：感应到脚的大拇指。象传说：感应到脚的大拇指，是因为心志向外。

虞翻说：拇指是脚的大拇指。艮象征手指，坤象征母，所以称拇。初六失位而与远处相应，变到四位便得正，因此心志在外，指九四。

六二：感应到小腿肚，凶；安居守静则吉。象传说：虽然爻辞说凶，安居却吉，是因为顺从而不造成伤害。

崔觐说：腓是小腿肚，位于拇指之上，象征六二。六二得位居中，与九五相应；若一味感应相求，就会失去艮止的礼节，所以凶。安居而承顺九三，顺从适当的礼节而止，所以吉。

九三：感应到大腿，执意追随所跟随的人，前往便有过失。

崔觐说：股是大腿，位于腓之上，象征九三。阳刚而得位，虽想感应上爻，但处在艮卦终点，应该止而不前。六二追随自己，心志在所追随者身上，所以说执其随。九三下比六二，却又向上感应上六，便失去正当义理，所以前往会有羞吝。

象传说：感应到大腿，说明不能安处；心志在追随他人，所执守的对象处于下方。

虞翻说：巽象征大腿，这里指六二；巽也象征随从，艮象征手，所以称执。九三与上六相应，初、四已经变化而经历险阻，所以前往有吝。巽象征处女；男子已经谦下于女子，艮阳进入兑阴，所以说不能安处。士人与女子尚未婚配时，都称为处。九三心志在六二，因此所执在下。

九四：守正吉祥，悔恨消失；心意往来不定，朋友会顺从你的思虑。

虞翻说：九四失位，所以有悔；与初六相应，发动后得正，因此守正吉而悔亡。憧憧是思虑纷扰。向内称来，向外称往。想感应上六却被九五隔开，想感应初六又被九三隔开，所以往来不定。兑象征朋友，也象征少女；艮初爻变化到四位，坎心象征思虑，所以说朋友顺从你的思虑。

象传说：守正吉而悔恨消失，是因为尚未受到感应之害。

虞翻说：坤象征伤害；现在还没有感应到坤。初爻之体有遁卦弑父的象，所以说尚未感应到伤害。

心意纷扰地往来，说明还没有达到光明广大。

虞翻说：尚未发动而形成离卦，所以还没有光明广大。

九五：感应到脊背的肉，没有悔恨。

虞翻说：脢是夹在脊骨两侧的肉。四爻已经变化，坎象征脊背，所以说感应到脢。九五得正，因此无悔。

象传说：感应到脢，是因为心志在末端、上方。

编者按：末就是上。九四感应初六，九三追随六二，九五亲近上六，所以说感应到脢；心志在末端，指九五的心志感应于上六。

上六：感应到面颊、脸颊和舌头。

虞翻说：耳目之间称为辅颊。四爻变化而显出眼目之象，坎象征耳朵，兑象征口舌，所以称辅颊舌。

象传说：感应到面颊、脸颊和舌头，是把言语送出口中。

虞翻说：媵是送。上六不能变到三位，山泽之气相通，所以说把言语送出口中。

译释说明：本条按现有录文逐段翻译；“拇、腓、股、脢”等身体部位依注家训释转译，虞翻卦变、纳甲说仅作为其注释体系陈述。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷七·恒、遯集解", "底本：维基文库《周易集解（李鼎祚）/卷七》，正文行194—390。录入恒、遯两卦经文、《彖》《象》《序卦》及页面所载诸家注文、案语。原页面字形、注家署名和编次照录；尚未与四库影印本逐字校勘。译文尚未完成。来源：https://zh.wikisource.org/zh-hans/周易集解/卷七", """《序卦》曰：夫妇之道，不可不久也。故受之以恒。恒者，久也。
郑玄曰：言夫妇当有终身之义。夫妇之道，谓咸也。
(巽下震上)。恒：亨，无咎，利贞。
虞翻曰：恒，久也。与益旁通。干初之坤四，刚柔皆应，故“通，无咎，利贞”矣。
郑玄曰：恒，久也。巽为风，震为雷，雷风相须而养物，犹长女承长男，夫妇同心而成家，久长之道也。夫妇以嘉会礼通，故“无咎”。其能和顺干事，所行而善矣。
利有攸往。
虞翻曰：初利往之四，终变成益，则初四二五皆得其正，终则有始，故“利有攸往”也。
《彖》曰：恒，久也。刚上而柔下，
王弼曰：刚尊柔卑，得其序也。
雷风相与，巽而动，
蜀才曰：此本泰卦。
案：六四降初，初九升四，是刚上而柔下也。分干与坤，雷也。分坤与干，风也。是雷风相与，巽而动也。
刚柔皆应，恒。
《九家易》曰：初四二五虽不正，而刚柔皆应，故“通无咎”矣。
恒亨，无咎，利贞，久于其道也。
荀爽曰：恒，震世也，巽来乘之，阴阳合会，故“通无咎”。长男在上，长女在下，夫妇道正，故“利贞，久于其道也”。
天地之道，恒久而不已也。
虞翻曰：泰，乾坤为天地。谓终则复始，有亲则可久也。
利有攸往，终则有始也。
荀爽曰：谓干气下终，始复升上居四也。坤气上终，始复降下居初者也。
日月得天而能久照，
虞翻曰：动初，成乾为天。至二，离为日；至三，坎为月。故“日月得天而能久照”也。
四时变化而久成。
虞翻曰：春夏为变，秋冬为化，变至二离夏，至三兑秋，至四震春，至五坎冬。故“四时变化而久成”。谓乾坤成物也。
圣人久于其道，而天下化成。
虞翻曰：圣人谓干。干为道。初二已正，四五复位，成既济定。乾道变化，各正性命。有两离象，重明丽正，故“化成天下”。
观其所恒，而天地万物之情可见矣。
虞翻曰：以离日照干，坎月照坤，万物出震，故“天地万物之情可见矣”。与咸同义也。
《象》曰：雷风，恒。
宋衷曰：雷以动之，风以散之。二者常相薄，而为万物用，故君子象之，以立身守节而不易道也。
君子以立不易方。
虞翻曰：君子谓干三也。干为易，为立；坤为方。干初之坤四，三正不动。故“立不易方”也。
初六：浚恒，贞凶，无攸利。
侯果曰：浚，深。恒，久也。初本六四，自四居初，始求深厚之位者也。位既非正，求乃涉邪。以此为正，凶之道也。故曰“浚恒，贞凶，无攸利”矣。
《象》曰：浚恒之凶，始求深也。
虞翻曰：浚，深也。初下称浚，故曰“浚恒”。干初为渊，故“深”矣。失位变之正，干为始。故曰“始求深也”。
九二：悔亡。
虞翻曰：失位悔也，动而得正，处中多誉，故“悔亡”也。
《象》曰：九二悔亡，能久中也。
荀爽曰：干为久也。能久行中和，以阳据阴，故曰“能久中也”。
九三：不恒其德，或承之羞，贞吝。
荀爽曰：与初同象，欲据初，隔二。与五为兑，欲悦之，隔四。意无所定，故“不恒其德”。与上相应，欲往承之，为阴所乘，故“或承之羞”也。贞吝者，谓正居其所，不与阴通也。无居自容，故“贞吝”矣。
《象》曰：不恒其德，无所容也。
《九家易》曰：言三取初隔二，应上见乘，是“无所容”。无居自容，故“贞吝”。
九四：田无禽。《象》曰：久非其位，安得禽也。
虞翻曰：田为二也。地上称田，无禽，谓五也。九四失位，利二上之五，已变承之。故曰“田无禽”。言二五皆非其位，故《象》曰：“久非其位，安得禽也。”。
六五：恒其德，贞妇人吉，夫子凶。
虞翻曰：动正成干，故“恒其德”。妇人谓初。巽为妇。终变成益，震四复初，妇得归阳，从一而终，故“贞妇人吉也”。震，干之子，而为巽夫，故曰“夫子”。终变成益，震四从巽，死于坤中，故“夫子凶”也。
《象》曰：妇人贞吉，从一而终也。
虞翻曰：一谓初，终变成益，以巽应初震，故“从一而终也”。
夫子制义，从妇凶也。
虞翻曰：震没从巽入坤，故“从妇凶”矣。
上六：震恒，凶。《象》曰：震恒在上，大无功也。
虞翻曰：在震上，故“震恒”。五动乘阳，故“凶”。终在益上，五远应，故“无功也”。
《序卦》曰：物不可以久居其所，故受之以遁。遁者，退也。
韩康伯曰：夫妇之道，以恒为贵，而物之所居，不可恒，宜与世升降，有时而遁者也。
(艮下干上)。遁：亨。
虞翻曰：阴消姤二也。艮为山，巽为入，干为远，远山入藏，故“遁”，以阴消阳，子弑其父，小人道长，避之乃通，故遁而通。则当位而应，与时行也。
小利贞。
虞翻曰：小，阴，谓二。得位浸长，以柔变刚，故“小利贞”。
郑玄曰：遁，逃去之名也。艮为门阙。干有健德。互体有巽，巽为进退。君子出门，行有进退，逃去之象。二五得位而有应，是用正道得礼见召聘。始仕他国，当尚谦谦。小其和顺之道，居小官，干小事其进以渐，则远妨忌之害，昔陈敬仲奔齐辞卿是也。
《彖》曰：遁亨，遁而亨也。
侯果曰：此本乾卦。阴长刚殒，君子遁避，遁则通也。
刚当位而应，与时行也。
虞翻曰：刚谓五而应二，艮为时，故“与时行”矣。
小利贞，浸而长也。
荀爽曰：阴称小，浸而长，则将消阳，故“利正”。居二与五相应也。
遁之时义大矣哉。
陆绩曰：谓阳气退，阴气将害，随时遁避，其义大矣。
宋衷曰：太公遁殷，四皓遁秦之时也。
《象》曰：天下有山，遁。
崔觐曰：天喻君子，山比小人。小人浸长，若山之侵天。君子遁避，若天之远山，故言“天下有山遁也”。
君子以远小人，不恶而严。
虞翻曰：君子谓干。干为远，为严。小人谓阴。坤为恶，为小人。故“以远小人，不恶而严”也。
侯果曰：群小浸盛，刚德殒削，故君子避之。高尚林野，但矜严于外，亦不憎恶于内，所谓吾家耄逊于荒也。
初六：遁尾，厉，勿用有攸往。
陆绩曰：阴气已至于二，而初在其后，故曰“遁尾”也。避难当在前，而在后，故“厉”。往则与灾难会，故“勿用有攸往”。
《象》曰：遁尾之厉，不往何灾也。
虞翻曰：艮为尾也。初失位，动而得正，故“遁尾厉”。之应成坎为灾，在艮宜静，若不往于四，则无灾矣。
六二：执之用黄牛之革，莫之胜说。
虞翻曰：艮为手，称执；否坤为黄牛；艮为皮。四变之初，则坎水濡皮。离日干之。故“执之用黄牛之革”。莫，无也。胜，能。说，解也。干为坚刚，巽为绳，艮为手，持革缚三在坎中，故“莫之胜说”也。
《象》曰：执用黄牛，固志也。
侯果曰：六二离爻，离为黄牛。体艮履正，上应贵主。志在辅时，不随物遁。独守中直，坚如革束。执此之志，莫之胜说。殷之父师，当此爻矣。
九三：系遁，有疾厉，畜臣妾，吉。
虞翻曰：厉，危也。巽为四变，时九三体坎，坎为疾。故“有疾厉”。遁阴剥阳，三消成坤，与上易位。坤为臣，兑为妾上来之三，据坤应兑，故“畜臣妾吉”也。
《象》曰：系遁之厉，有疾惫也。
王肃曰：三上系于二而获遁，故曰系遁。病此系执而获危惧，故曰“有疾惫也”。此于六二，畜臣妾之象。足以畜其臣妾，不可施为大事也。
畜臣妾吉，不可大事也。
虞翻曰：三动入坤，坤为事，故“不可大事也”。
荀爽曰：大事谓与五同任天下之政。潜遁之世，但可居家畜养臣妾，不可治国之大事。
九四：好遁，君子吉，小人否。
虞翻曰：否干为好，为君子；阴称小人。动之初，故“君子吉”。阴在四多惧，故“小人否”。得位承五，故无凶咎矣。
《象》曰：君子好遁，小人否也。
侯果曰：不处其位，而遁于外，好遁者也。然有应在初，情未能弃。君子刚断，故能舍之。小人系恋，必不能矣。故“君子吉，小人凶”矣。
九五：嘉遁，贞吉。
虞翻曰：干为嘉，刚当位应二，故“贞吉”。谓三已变，上来之三，成坎象，故曰“以正志也”。
《象》曰：嘉遁贞吉，以正志也。
侯果曰：时否德刚，虽遁中正，嘉遁者也。故曰“贞吉”。遁而得正，则群小应命，所谓纽以紊之。刚正群小之志，则殷之高宗当此爻矣。
上九：肥遁，无不利。
虞翻曰：干盈为肥。二不及上，故“肥遁无不利”。《象》曰：“无所疑也。”
《象》曰：肥遁无不利，无所疑也。
侯果曰：最处外极，无应于内。心无疑恋，超世高举。果行育德，安时无闷，遁之肥也。故曰“肥遁无不利”，则颍滨巢许当此爻矣。"""),

    Entry("历代注疏", "唐李鼎祚《周易集解》卷七·大壮卦集解", "底本：维基文库《周易集解（李鼎祚）/卷七》，正文行392—469。录入大壮卦经文、《彖》《象》及页面所载诸家注文、案语。页面字形与注文次序照录，尚未与四库影印本逐字校勘；逐段译文尚未完成。来源：https://zh.wikisource.org/zh-hans/周易集解/卷七", """《序卦》曰：物不可以终遁，故受之以大壮。
韩康伯曰：遁，君子以远小人。遁而后通，何可终耶。阳盛阴消，君子道胜也。
(干下震上)。大壮：利贞。
虞翻曰：阳息，泰也。壮，伤也。大谓四，失位为阴所乘。兑为毁折伤，与五易位乃得正，故“利贞”也。
《彖》曰：大壮，大者壮也。
侯果曰：此卦本坤。阴柔消弱，刚大长壮，故曰“大壮”也。
刚以动，故壮。
荀爽曰：乾刚震动，阳从下升，阳气大动，“故壮”也。
大壮利贞，大者正也。
虞翻曰：谓四进之五乃得正，故“大者正也”。
正大而天地之情可见矣。
虞翻曰：正大谓四之五成需。以离日见天，坎月见地，故“天地之情可见”也矣。
《象》曰：雷在天上，大壮。
崔觐曰：干下震上，故曰“雷在天上”。一曰：雷，阳气也。阳至于上卦。能助于天威，大壮之象也。
君子以非礼弗履。
陆绩曰：天尊雷卑，君子见卑乘尊，终必消除，故象以为戒，“非礼不履”。
初九：壮于趾，征凶，有孚。
虞翻曰：趾谓四。征，行也。震足为趾，为征。初得位，四不征之五，故“凶”。坎为孚，谓四上之五成坎，已得应四，故“有孚”。
《象》曰：壮于趾，其孚穷也。
虞翻曰：应在干终，故“其孚穷也”。
九二：贞吉。《象》曰：九二贞吉，以中也。
虞翻曰：变得位，故“贞吉”。动体离，故“以中也”。
九三：小人用壮，君子用罔，贞厉。
虞翻曰：应在震也。三，阳，君子。小人谓上。上逆，故“用壮”。谓二已变离，离为罔，三乘二，故“君子用罔”。体干，夕惕，故“贞厉”也。
羝羊触藩，羸其角。
荀爽曰：三与五同功，为兑，故曰“羊”。终始阳位，故曰“羝”。藩，谓四也。三欲触四而危之，四反羸其角。角，谓五也。
《象》曰：小人用壮，君子罔也。
侯果曰：藩，谓四也。九四体震，为竹苇，故称“藩”也。三互干、兑、干，壮；兑，羊，故曰“羝羊”。四藩未决，三宜勿往，用壮触藩，求应于上，故角被拘羸矣。
案：自三至五，体兑为羊，四既是藩，五为羊角，即“羝羊触藩，羸其角”之象也。
九四：贞吉，悔亡。藩决不羸，壮于大舆之腹。《象》曰：藩决不赢尚往也。
虞翻曰：失位悔也。之五得中，故“贞吉”而“悔亡”矣。体夬象，故“藩决”。震四上处五，则藩毁坏，故“藩决不羸”。坤为大舆、为腹；四之五折坤，故“壮于大舆之腹”。而《象》曰“尚往者”，谓上之五。
六五：丧羊于易，无悔。
虞翻曰：四动成泰。坤为丧也。干为易。四上之五，兑还属干。故“丧羊于易”动各得正，而处中和，故“无悔”矣。
《象》曰：丧羊于易，位不当也。
案：谓四五阴阳失正。阴阳失正，故曰“位不当”也。
上六：羝羊触藩，不能退，不能遂，无攸利，艰则吉。
虞翻曰：应在三，故“羝羊触藩”。遂，进也。谓四已之五，体坎。上能变之巽，巽为进退。故“不能退，不能遂”。退则失位，上则乘刚，故“无攸利”。坎为艰，得位应三利上，故“艰则吉”。
《象》曰：不能退，不能遂，不详也。
虞翻曰：干善为详，不得三应，故“不详也”。
艰则吉，咎不长也。
虞翻曰：巽为长。动失位，为咎。不变之巽。故“咎不长也”。
""", chatGptTranslation = """**ChatGPT 翻译（逐段工作译文）**

《序卦》说：事物不能永远停留在退避状态，所以接着是大壮卦。

韩康伯说：遁卦讲君子远离小人。退避之后才能重新通达，怎能永远退避？阳气盛大、阴气消退，君子之道便逐渐得胜。

大壮卦下乾上震。大壮：利于守正。

虞翻说：阳气增长，形成泰卦。壮也有伤害之义。大指九四；九四失位而被阴爻乘凌。兑象征毁折伤害；九四与九五交换位置后才得正，所以利于守正。

《彖传》说：大壮，就是阳刚强大而壮盛。

侯果说：此卦本于坤卦。阴柔逐渐消弱，刚健壮大，所以称大壮。

刚健而行动，所以壮盛。

荀爽说：乾刚健，震发动；阳气从下方上升，阳气大为发动，所以壮盛。

大壮利于守正，因为强大者应当端正。

虞翻说：指九四前进到九五，因而得正，所以说强大者正。

正大之道确立，天地的情状便可以看见。

虞翻说：正大指九四移到九五而形成需卦。离象征太阳而显现于天，坎象征月亮而显现于地，所以天地之情可以看见。

《象传》说：雷在天上，就是大壮卦。

崔觐说：下乾上震，所以说雷在天上。另一种解释认为雷是阳气；阳气到达上卦，能够助长天威，这就是大壮的象。

君子因此不合礼的事绝不去做。

陆绩说：天尊贵，雷处卑下。君子看到卑下者凌驾尊贵者，最终必遭消除，因此以此为戒，不合礼的行为不去实行。

初九：壮盛体现在脚趾，出行征进凶险，但有诚信。

虞翻说：趾指九四。征是行走。震象征脚，也象征出行。初九得位，九四却不前往九五，所以有凶象。坎象征诚信，指九四上升到九五后形成坎；初九与九四相应，所以说有诚信。

象传说：壮盛在脚趾，说明诚信的作用已经穷尽。

虞翻说：应在乾卦终点，所以说诚信穷尽。

九二：守正吉祥。象传说：九二守正吉祥，是因为居中。

虞翻说：变动后得到正位，所以吉；发动后形成离卦，离处于中位，所以说居中。

九三：小人凭借强壮逞强，君子则运用无形之道，守正而有危险。

虞翻说：九三与震相应。九三为阳，象征君子；上六象征小人。上六逆行，所以说小人逞强。九二已经变化为离，离有网罗之象；九三乘在九二之上，所以说君子用网。九三处于乾体，须整日勤勉、夜间警惕，所以守正仍有危险。

公羊触撞藩篱，角被缠住。

荀爽说：九三与九五同功，形成兑象，所以称羊；始终处在阳位，所以称公羊。藩篱指九四。九三想冲撞九四并使其受损，九四反而缠住它的角。角指九五。

象传说：小人逞强，君子则不以强力妄为。

侯果说：藩篱指九四。九四属于震体，象征竹苇，所以称藩。九三互体含乾、兑；乾象征强壮，兑象征羊，所以称公羊。九四藩篱尚未打开，九三本应不前往，却逞强冲撞藩篱，想求得上方回应，因此角被缠住。

编者按：九三至九五构成兑象，兑为羊；九四是藩篱，九五是羊角，因此形成公羊触藩、角被缠住的象。

九四：守正吉祥，悔恨消失。藩篱打开，角不再被缠；壮盛如大车的车腹。象传说：藩篱打开而不再受困，是因为仍然向前。

虞翻说：九四失位，所以有悔；前往九五而得中，因此守正吉、悔恨消失。卦体有夬象，所以藩篱决开。震四上居五位，藩篱便毁坏，所以说藩篱打开而不再受缠。坤象征大车，也象征腹部；九四到九五会折损坤体，所以说壮盛于大车的车腹。象传所谓仍然向前，是指上爻前往五位。

六五：在交换、变易之际失去羊，没有悔恨。

虞翻说：九四发动形成泰卦。坤象征丧失，乾象征变易。九四上升到九五，兑象重新归属乾。各爻变动后都得到正位，处于中和，所以没有悔恨。

象传说：在变易之际失去羊，是因为位置不恰当。

编者按：这是说九四、九五阴阳位置不正；阴阳失正，所以称位不当。

上六：公羊撞藩，不能后退，也不能前进，没有任何利益；艰难守持则吉。

虞翻说：上六与九三相应，所以说公羊撞藩。遂是前进。九四已经到九五，形成坎；上六能够变成巽，巽象征进退，所以不能退也不能进。退则失位，进则乘刚，因此没有利益。坎象征艰难；上六变后得位并与九三相应，利于居上，所以艰难守持则吉。

象传说：不能退、不能进，是因为判断不够周详。

虞翻说：乾象征善美、周详；上六得不到九三的应援，所以称不详。

艰难守持则吉，是因为灾咎不会长久。

虞翻说：巽象征长久。发动后失位便形成灾咎；若不变成巽，灾咎就不会长久。

译释说明：本条中的“羊、藩、角、大舆”等按卦象及注家解释翻译；虞翻关于爻位交换、卦变的说明属于象数注释，不作为唯一解释。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷七·晋卦集解", "底本：维基文库《周易集解（李鼎祚）/卷七》，正文行471—556。录入晋卦经文、《彖》《象》《序卦》及页面所载诸家注文、案语。页面字形与注文次序照录，尚未与四库影印本逐字校勘；逐段译文尚未完成。来源：https://zh.wikisource.org/zh-hans/周易集解/卷七", """《序卦》曰：物不可以终壮，故受之以晋。晋者，进也。
崔觐曰：不可以终壮于阳盛，自取触藩，宜柔进而上行，受兹锡马。
(坤下离上)。晋：康侯用锡马蕃庶，昼日三接。
虞翻曰：观四之五。晋，进也。坤为康。康，安也；初动体屯，震为侯，故曰“康侯”。震为马，坤为用，故“用锡马”。艮为多，坤为众，故“繁庶”。离日在上，故“昼日”。三阴在下，故“三接”矣。
《彖》曰：晋，进也。明出地上，顺而丽乎大明。
崔觐曰：浑天之义，日从地出，而升于天，故曰“明出地上”。坤，臣道也。日，君德也。臣以功进，君以恩接，是以“顺而丽乎大明”。虽以卦名，晋而五爻为主，故言“柔进而上行”也。
柔进而上行，
蜀才曰：此本观卦。案：九五降四，六四进五，是柔进而上行也。
是以康侯用锡马蕃庶。
荀爽曰：阴进居五，处用事之位。阳中之阴，侯之象也；阴性安静，故曰“康侯”。马，谓四也。五以下，群阴锡四也。坤为众，故曰“蕃庶”矣。
昼日三接也。
侯果曰：康，美也。四为诸侯，五为天子，坤为众，坎为马。天子至明于上，公侯谦顺于下，美其治物有功。故蕃锡车马，一昼三觌也。《采菽》刺幽王侮诸侯诗曰：虽无与之，路车乘马。《大行人职》曰：诸公：三飨，三问，三劳；诸侯：三飨，再问，再劳；子男：三飨、一问、一劳，即天子三接诸侯之礼也。
《象》曰：明出地上。晋。君子以自昭明德。
郑玄曰：地虽生万物，日出于上，其功乃著，故君子法之，而“以明自昭其德”。
虞翻曰：君子谓观干。干为德，坤为自，离为明。干五动，以离日自照，故“以自昭明德”也。
初六：晋如，摧如，贞吉。罔孚，裕无咎。
虞翻曰：晋，进。摧，忧愁也。应在四，故“晋如”。失位，故“摧如”。动得位，故“贞吉”。应离为罔，四坎称孚，坤弱为裕，欲四之五成巽，初受其命，故“无咎”矣。
《象》曰：晋如，摧如，独行正也。
虞翻曰：初动震为行。初一称独也。
裕无咎，未受命也。
虞翻曰：五未之巽，故“未受命也”。
六二：晋如，愁如，贞吉。
虞翻曰：震谓应，在坎上，故“愁如”。得位处中，故“贞吉”也。
受兹介福于其王母。
虞翻曰：干为介福，艮为手，坤为虚，故称“受”。介，大也。谓五已正中，干为王，坤为母。故“受兹介福于其王母”。
《象》曰：受兹介福，以中正也。
《九家易》曰：五动得正中，故二受大福矣。大福，谓马与蕃庶之物是也。
六三：众允，悔亡。
虞翻曰：坤为众。允，信也。土性信，故“众允”。三失正，与上易位，则“悔亡”，故《象》曰“上行也”。此则成小过，小过故有飞鸟之象焉。臼杵之利，见硕鼠出入坎穴，盖取诸此也。
《象》曰：众允之志，上行也。
虞翻曰：坎为志。三之上成震，故曰“上行也”。
九四：晋如硕鼠，贞厉。
《九家易》曰：硕鼠，喻贪，谓四也。体离欲升，体坎欲降。游不度渎，不出坎也。飞不上屋，不至上也。缘不极木，不了离也。穴不掩身，五坤薄也。走不先足，外震在下也。五伎皆劣，四爻当之。故曰“晋如硕鼠”也。
《象》曰：硕鼠贞厉，位不当也。
翟元曰：硕鼠昼伏夜行，贪猥无已。谓虽进承五，然潜据下阴，久居不正之地，故有危厉也。
六五：悔亡，矢得勿恤，往吉，无不利。
荀爽曰：五从坤动而来为离。离者，射也。故曰“矢得”。阴居尊位，故有悔也。以中盛明，光照四海，故“悔亡勿恤，吉无不利”也。
《象》曰：矢得勿恤，往有庆也。
虞翻曰：动之乾乾，为庆也。矢古誓字。誓，信也。勿，无。恤，忧也。五变得正，坎象不见，故“誓得勿恤，往有庆也”。
上九：晋其角，
虞翻曰：五已变之干，为首。位在首上，称角。故“晋其角”也。
惟用伐邑，厉吉无咎，贞吝。
虞翻曰：坤为邑。动成震，而体师象，坎为心。故“维用伐邑”。得位乘五，故“厉吉，无咎”而“贞吝”矣。
《象》曰：惟用伐邑，道未光也。
荀爽曰：阳虽在上，动入冥豫，故道未光也。
""", chatGptTranslation = """**ChatGPT 翻译（逐段工作译文）**

《序卦》说：事物不能永远处于壮盛状态，所以接着是晋卦。晋就是前进。

崔觐说：阳气盛大而壮盛不能一直持续，否则会像公羊撞藩一样自取困境；应当以柔顺的方式向前发展，因此接着出现赐马的卦辞。

晋卦下坤上离。晋：康侯得到众多赐马，一天之内受到天子三次接见。

虞翻说：这是观卦九四移到九五。晋就是前进。坤象征康，康就是安定；初爻发动形成屯卦，震象征诸侯，所以称康侯。震象征马，坤象征使用，所以说赐马。艮象征多，坤象征众多，所以说繁多。离象征太阳在上，所以说白昼；下方有三阴爻，所以说三次接见。

《彖传》说：晋就是前进。光明从地上升起，柔顺而依附于大光明。

崔觐说：按照浑天之说，太阳从地面升起而上升到天，所以说光明出现在地上。坤代表臣道，太阳代表君德。臣子凭借功绩晋升，君主以恩德接待，所以说柔顺而依附于大光明。虽然卦名叫晋，但九五爻是主爻，因此说柔顺之爻向上前进。

柔顺之爻向上前进。

蜀才说：此卦本于观卦。九五下降到四位，六四上升到五位，这就是柔爻向上前进。

因此康侯得到众多赐马。

荀爽说：阴爻前进而居五位，处在执掌事务的位置。阳位中的阴爻象征诸侯；阴性安静，所以称康侯。马指九四；九五以下的群阴把马赐给九四。坤象征众多，所以称繁庶。

白昼之内三次接见。

侯果说：康有美好之义。九四是诸侯，九五是天子；坤象征众多，坎象征马。天子在上光明显赫，公侯在下谦逊顺从；天子赞美他们治理有功，所以赐予许多车马，一昼之内三次接见。《诗经·采菽》讽刺周幽王轻慢诸侯，其中提到车马赏赐；《周礼·大行人》记载不同爵位诸侯享礼、问候、慰劳的次数，这些礼制可用来说明天子三接诸侯的礼仪。

《象传》说：光明出现在地上，就是晋卦。君子因此彰明自己的光明德行。

郑玄说：地虽然生养万物，但太阳升到地上，地的功用才显著。因此君子效法这一点，使自己的德行彰明。

虞翻说：君子指观卦的乾。乾象征德，坤象征自身，离象征光明。乾五发动，以离日照耀自己，所以说彰明光明德行。

初六：前进的样子，又有受挫的样子；守正吉祥。别人不信任自己，宽裕自处便没有过失。

虞翻说：晋是前进，摧是忧愁。初六与四位相应，所以说前进；因为失位，所以说受挫。发动后得到正位，所以守正吉。与离相应而有不实之象，四爻坎象称信，坤柔弱称宽裕；希望四爻上升到五位形成巽，初六接受其命令，因此无咎。

象传说：前进又受挫，是因为独自实行正道。

虞翻说：初爻发动形成震，震象征行动；初爻居第一位，所以称独。

宽裕自处便没有过失，是因为尚未接受任命。

虞翻说：五爻还没有变成巽，所以说尚未接受任命。

六二：前进时忧愁，但守正吉祥。

虞翻说：震象征行动，与坎相应，所以忧愁；六二得位居中，所以守正吉祥。

接受王母赐予的大福。

虞翻说：乾象征大福，艮象征手，坤象征虚，所以称接受。介是大。五爻已经端正居中，乾象征王，坤象征母，所以说从王母那里接受大福。

象传说：接受大福，是因为居中而守正。

《九家易》说：九五发动后得正居中，因此六二接受大福；大福指马匹及众多赐物。

六三：众人信服，悔恨消失。

虞翻说：坤象征众，允是诚信。土的性质主信，所以称众人信服。六三失正，与上爻交换位置便悔恨消失。象传说上行；这样形成小过卦，小过有飞鸟之象。臼杵之利以及硕鼠出入坎穴的象，也由此取象。

象传说：众人信服，是因为心志向上前进。

虞翻说：坎象征心志。六三上升到上位形成震，所以说向上前进。

九四：前进如同大老鼠，守正仍有危险。

《九家易》说：大老鼠比喻贪婪，指九四。它的卦体有离，想向上升；又有坎，想向下降。它在水沟中游走却不越过沟，在坎中不出来；想飞却不能上屋，想攀木却不能达到离卦；洞穴不能遮蔽身体，五种本领都很差。这些特点都落在九四，所以说前进如大老鼠。

象传说：大老鼠守正仍有危险，是因为位置不恰当。

翟元说：大老鼠白天藏伏、夜间行动，贪婪没有止境。九四虽然前进而承接九五，却暗中占据下方阴位，长久居于不正之地，所以危险。

六五：悔恨消失，箭已射中，不必忧虑；前往吉祥，没有不利。

荀爽说：六五从坤发动而来形成离，离象征射箭，所以说箭已射中。阴爻居于尊位，本来有悔；但居中而光明盛大，光照四方，因此悔恨消失，不必忧虑，前往吉祥而无不利。

象传说：箭已射中而不必忧虑，前往会有喜庆。

虞翻说：发动而成为乾，乾象征喜庆。“矢”是古代“誓”字；誓表示诚信，勿是无，恤是忧虑。六五变化后得正，坎象不再显现，所以说诚信已得，不必忧虑，前往有喜庆。

上九：前进到角上。

虞翻说：六五已经变化为乾，乾象征头部；上九处在头部之上，所以称角。

只适宜用来征伐自己的封邑；虽有危险而吉祥，没有过失，但守正仍有羞吝。

虞翻说：坤象征封邑。发动后形成震，又有师卦之象；坎象征内心，所以说用来征伐封邑。上九得位而乘九五，因此危险中有吉、没有过失，但守正仍有羞吝。

象传说：只用来征伐自己的封邑，是因为大道尚未光明。

荀爽说：阳爻虽然居上，但发动后进入幽暗的豫卦，所以大道还没有光明。

译释说明：康侯、赐马、三接等礼制内容依现有注家解释转述；“矢／誓”等字义保留虞翻训释，不将其视为唯一校读。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷七·明夷卦集解", "底本：维基文库《周易集解（李鼎祚）/卷七》，正文行558—644。录入明夷卦经文、《彖》《象》《序卦》及页面所载诸家注文、案语。页面字形与注文次序照录，尚未与四库影印本逐字校勘；逐段译文尚未完成。来源：https://zh.wikisource.org/zh-hans/周易集解/卷七", """《序卦》曰：进必有所伤，故受之以明夷。夷者，伤也。
《九家易》曰：日在坤下，其明伤也。言进极当降，复入于地，故曰明夷也。
(离下坤上)。明夷：利艰贞。
虞翻曰：夷，伤也。临二之三而反晋也。明入地中，故伤矣。
利艰贞。
虞翻曰：谓五也。五失位，变出成坎，为艰，故“利艰贞”矣。
郑玄曰：夷，伤也。日出地上，其明乃光。至其入地，明则伤矣，故谓之明夷。日之明伤，犹圣人君子有明德，而遭乱世。抑在下位，则宜自艰，无干事政，以避小人之害也。
《彖》曰：明入地中，明夷。
蜀才曰：此本临卦也。
案：夷，灭也。九二升三，六三降二，明入地中也。明入地中，则明灭也。
内文明而外柔顺，以蒙大难，
荀爽曰：明在地下，为坤所蔽，大难之象。大难，文王君臣相事，故言大难也。
文王以之。
虞翻曰：以，用也。三喻文王。大难谓坤。坤为弑父，迷乱荒淫，若纣杀比干。三幽坎中，象文王之拘羑里。震为诸侯，喻从文王者，纣惧出之，故“以蒙大难”，得身全矣。
利艰贞，晦其明也。内难而能正其志，箕子以之。
虞翻曰：箕子，纣诸父。故称内难。五干天位，今化为坤，箕子之象。坤为晦，箕子正之。出五成坎，体离，重明丽正。坎为志，故“正其志，箕子以之”，而纣奴之矣。
《象》曰：明入地中，明夷。君子以莅众，用晦而明。
虞翻曰：而，如也。君子谓三。体师象。以坎莅坤。坤为众，为晦，离为明，故“用晦如明”也。
初九：明夷于飞，垂其翼。君子于行，三日不食。
荀爽曰：火性炎上，离为飞鸟，故曰“于飞”。为坤所抑，故“垂其翼”。阳为君子。三者，阳德成也。日以喻君。不食者，不得食君禄也。阳未居五，阴暗在上。初有明德，耻食其禄，故曰“君子于行，三日不食”也。
有攸往，主人有言。
《九家易》曰：四者初应，众阴在上，为主人也。初欲上居五，则众阴有言。言，谓震也。四五体震，为雷声，故曰“有攸往，主人有言”也。
《象》曰：君子于行，义不食也。
荀爽曰：暗昧在上，有明德者，义不食禄也。
六二：明夷于左股，用拯马壮，吉。
《九家易》曰：左股谓初，为二所夷也。离为飞鸟，盖取小过之义，鸟飞舒翼而行。夷者，伤也。今初伤，垂翼在下故曰“明夷于左股”矣。九三体坎，坎为马也。二应于五，三与五同功，二以中和应天，应天合众，欲升上三，以壮于五，故曰“用拯马壮，吉”。
案：初为足。二居足上，股也。二互体坎。坎主左方，左股之象也。
《象》曰：六二之吉，顺以则也。
《九家易》曰：二欲上三居五，为天子。坎为法律。君有法，则众阴当顺从之矣。
九三：明夷于南狩，得其大首，不可疾贞。
《九家易》曰：岁终田猎，名曰狩也。南者，九五大阳之位，故称南也，暗昧道终，三可升上，而猎于五，得据大阳首位，故曰“明夷于南狩，得其大首”。自暗复明，当以渐次，不可卒正，故曰“不可疾贞”也。
《象》曰：南狩之志，乃大得也。
案：冬猎曰狩也。三互离坎，离南坎北，北主于冬，故曰“南狩”。五居暗主，三处明终，履正顺时，拯难兴衰者也。以臣伐君，故假言狩。既获五上之大首，而三志“乃大得也”。
六四：入于左腹，获明夷之心，于出门庭。
荀爽曰：阳称左，谓九三也。腹者，谓五居坤，坤为腹也。四得位比三，应于顺首。欲上三居五，以阳为腹心也。故曰“入于左腹，获明夷之心”。言三当出门庭，升五君位。
干宝曰：一为室，二为户，三为庭，四为门，故曰“于出门庭”矣。
《象》曰：入于左腹，获心意也。
《九家易》曰：四欲上三居五为坎，坎为心；四以坤爻为腹。故曰“入于左腹，获心意也”。
六五：箕子之明夷，利贞。
马融曰：箕子，纣之诸父，明于天道、《洪范》之九畴。德可以王，故以当五，知纣之恶，无可奈何。同姓恩深，不忍弃去，被发佯狂，以明为暗。故曰“箕子之明夷”。卒以全身，为武王师，名传无穷，故曰“利贞”矣。
《象》曰：箕子之贞，明不可息也。
侯果曰：体柔履中，内明外暗，群阴共掩，以夷其明。然以正为明，而不可息；以爻取象，箕子当之，故曰“箕子之贞，明不可息也。”
上六：不明晦，初登于天，后入于地。
虞翻曰：应在三。离灭坤下，故“不明晦。”晋时在上丽干，故“登于天照四国。”今反在下，故“后入于地，失其则。”
《象》曰：初登于天，照四国也。后入于地，失则也。
侯果曰：最远于阳，故曰“不明晦”也。初登于天，谓明出地上。下照于坤，坤为众国，故曰“照于四国也。”喻阳之初兴也。后入于地，谓明入地中，昼变为夜，暗晦之甚，故曰“失则也。”况纣之乱世也。此之二象，言晋与明夷，往复不已。故见暗则伐取之，乱则治取之，圣人因象设诫也。
"""),

    Entry("历代注疏", "唐李鼎祚《周易集解》卷八·家人卦集解", "底本：维基文库《周易集解（李鼎祚）/卷八》，正文行114—199。录入家人卦《序卦》、经文、《彖》《象》、六爻及页面所载注文、案语。依页面数字文本录入，尚未与四库影印本逐字校勘；逐段译文尚未完成。来源：https://zh.wikisource.org/zh-hans/周易集解/卷八", """《序卦》曰：伤于外者必反于家，故受之以家人。
韩康伯曰：伤于外者，必反诸内也。
(离下巽上)。家人：利女贞。
虞翻曰：遁初之四也。女谓离巽。二四得正。故“利女贞”也。
马融曰：家人以女为奥主。长女中女，各得其正，故特曰“利女贞”矣。
《彖》曰：家人，女正位乎内，男正位乎外。
王弼曰：谓二五也。家人之义，以内为本者也。故先说女矣。
男女正，天地之大义也。
虞翻曰：遁乾为天，三动坤为地。男得天，正于五；女得地，正于二。故“天地之大义也”。
家人有严君焉，父母之谓也。
荀爽曰：离巽之中有乾坤，故曰“父母之谓也”。
王肃曰：凡男女所以能各得其正者，由家人有严君也。家人有严君，故父子夫妇各得其正。家家咸正，而天下之治大定矣。
案：二五相应，为卦之主。五阳在外，二阴在内，父母之谓也。
父父，子子，兄兄，弟弟，
虞翻曰：遁干为父，艮为子，三五位正，故“父父，子子”。三动时，震为兄，艮为弟，初位正，故“兄兄、弟弟”。
夫夫，妇妇，
虞翻曰：三动时，震为夫，巽四为妇，初四位正，故“夫夫，妇妇”也。
而家道正。正家而天下定矣。
荀爽曰：父谓五，子谓四，兄谓三，弟谓初，夫谓五，妇谓二也。各得其正，故“天下定矣”。
陆绩曰：圣人教先从家始，家正而天下化之，修已以安百姓者也。
《象》曰：风自火出，家人。
马融曰：木生火。火以木为家，故曰“家人”。火生于木，得风而盛，犹夫妇之道，相须而成。
君子以言有物而行有恒。
荀爽曰：风火相与，必附于物。物大火大，物小火小。君子之言，必因其位。位大言大，位小言小。不在其位，不谋其政，故“言有物”也。大暑烁金，火不增其烈；大寒凝冰，火不损其热，故曰“行有恒”矣。
初九：闲有家，悔亡。《象》曰：闲有家，志未变也。
荀爽曰：初在潜位，未干国政，闲习家事而已。未得治官，故“悔”。居家理治，可移于官，守之以正，故“悔亡”。而未变从国之事，故曰“志未变也”。
六二：无攸遂，在中馈，贞吉。
荀爽曰：六二处和得正。得正有应，有应有实，阴道之至美者也。坤道顺从，故无所得遂。供肴中馈，洒食是议，故曰“中馈”。居中守正，永贞其志，则“吉”，故曰“贞吉”也。
《象》曰：六二之吉，顺以巽也。
《九家易》曰：谓二居贞，巽顺于五，则“吉”矣。
九三：家人嗃嗃，悔厉吉。妇子嘻嘻，终吝。
王弼曰：以阳居阳，刚严者也。处下体之极，为一家之长，行与其慢也，宁过乎恭；家与其渎也，宁过乎严。是以家虽嗃嗃，悔厉犹得吉也。妇子嘻嘻，失家节也。
侯果曰：嗃嗃，严也。嘻嘻，笑也。
《象》曰：家人嗃々，未失也。妇子嘻嘻，失家节也。
《九家易》曰：别体异家，阴阳相据，喜乐过节也。别体异家，谓三五也。阴阳相据，三五各相据阴，故言妇子也。
六四：富家大吉。《象》曰：富家大吉，顺在位也。
虞翻曰：三变体艮，艮为笃实。坤为大业。得位应初，顺五乘三，比据三阳，故曰“富家大吉，顺在位也。”谓顺于五矣。
九五：王假有家，勿恤，吉。
陆绩曰：假，大也。五得尊位，据四应二，以天下为家，故“王大有家”。天下正之，故无所忧则吉。
《象》曰：王假有家，交相爱也。
虞翻曰：干为爱也，二称家。三动成震。五得交二，初得交四，故“交相爱”。震为交也。
上九：有孚威加，终吉。
虞翻曰：谓三已变，与上易位，成坎。坎为孚，故“有孚”。干为威如，自上之坤，故“威如”。易则得位，故“终吉”也。
《象》曰：威如之吉，反身之谓也。
虞翻曰：谓三动，坤为身。上之三，成既济定，故“反身之谓”。此家道正，正家而天下定矣。
""", chatGptTranslation = """**ChatGPT 翻译（逐段工作译文）**

《序卦》说：在外面受到伤害的人必定返回家中，所以接着是家人卦。

韩康伯说：在外面受到伤害，就必然返回内部。

家人卦下离上巽。家人：利于女子守正。

虞翻说：这是遁卦初爻移到四位。女子指离、巽二卦。二爻和四爻都得到正位，所以利于女子守正。

马融说：家中以女子为内主。长女和中女各自处在正位，所以特别说利于女子守正。

《彖传》说：家人卦中，女子在内居正位，男子在外居正位。

王弼说：指六二和九五。家人之道以内部为根本，所以先谈女子。

男女各得其正，是天地的大义。

虞翻说：遁卦的乾象征天，九三发动形成坤，坤象征地。男子得到天道，在五位端正；女子得到地道，在二位端正，所以说这是天地的大义。

家中有严正的主宰，说的就是父母。

荀爽说：离、巽之中含有乾坤之象，所以说父母。

王肃说：男女之所以能够各自得正，是因为家中有严正的主宰。家中有严正的主宰，父子夫妇才能各得其正；家家都端正，天下的治理便大定。

编者按：二、五相应，是此卦的主爻。九五阳爻在外，六二阴爻在内，象征父母。

父亲像父亲，儿子像儿子，兄长像兄长，弟弟像弟弟。

虞翻说：遁卦乾象征父亲，艮象征儿子；三、五爻位置端正，所以说父父子子。九三发动时，震象征兄长，艮象征弟弟；初爻位置端正，所以说兄兄弟弟。

丈夫像丈夫，妻子像妻子。

虞翻说：九三发动时，震象征丈夫，巽四象征妻子；初、四两爻位置端正，所以说夫夫妇妇。

这样家道就端正了。家庭端正，天下便安定。

荀爽说：父亲指九五，儿子指九四，兄长指九三，弟弟指初六；丈夫指九五，妻子指六二。各自得到正位，所以天下安定。

陆绩说：圣人教化先从家庭开始；家庭端正，天下便会受到感化。这就是修养自身而后使百姓安定。

《象传》说：风从火中生出，这就是家人卦。

马融说：木生火，火以木为家，所以称家人。火从木中产生，得到风便更加旺盛，就像夫妇之道相互依赖而成就。

君子因此言语有实质，行为有恒常。

荀爽说：风与火相互依附，必须有所依托。依托之物大，火势便大；依托之物小，火势便小。君子说话必须符合自己的身份地位，地位高则言论责任大，地位低则言论范围小；不在其位，不谋其政，所以说言语有实质。酷暑不能使金属火焰更加猛烈，严寒凝冰也不能损害火的热性，所以说行为有恒常。

初九：防范整治家庭，悔恨消失。象传说：防范整治家庭，是因为志向尚未改变。

荀爽说：初九处在潜藏之位，还没有参与国家政务，只需熟习家中事务。尚未得到官职，所以本有悔；但在家中治理事务，可以把这种能力转用于做官，守正而行，所以悔恨消失。此时尚未改变为从事国家事务，因此说志向尚未改变。

六二：没有什么可以擅自完成的事，负责家中饮食供奉，守正吉祥。

荀爽说：六二处于和顺之位而得正，又有正当应援，这是阴柔之道最美好的状态。坤道在于顺从，所以不擅自专断；负责准备食物、供奉饮食，所以称中馈。居中守正，长久保持正当志向，便吉祥。

象传说：六二的吉祥，在于顺从而谦逊。

《九家易》说：指六二居正位，顺从九五，所以吉祥。

九三：一家人过于严厉，虽有悔、有危险，仍可吉祥；妇女儿童嬉笑无度，最终会有羞吝。

王弼说：九三以阳爻居阳位，刚强严厉；处在下卦顶端，是一家之长。行为与其轻慢，不如过分恭敬；治家与其亵慢，不如过分严厉。因此家庭虽然严厉，虽有悔和危险，仍能得到吉祥。妇女儿童嬉笑无度，是失去了家庭礼节。

侯果说：嗃嗃是严厉，嘻嘻是嬉笑。

象传说：家人严厉，并没有失去正道；妇女儿童嬉笑无度，是失去家庭礼节。

《九家易》说：不同卦体象征不同家庭，阴阳相互依恃，欢乐超过礼节。所谓不同卦体指三、五；阴阳相互依恃，是三、五各自依靠阴爻，所以说妇女儿童。

六四：使家庭富足，大吉。象传说：使家庭富足而大吉，是因为顺从并居于正位。

虞翻说：九三变化形成艮，艮象征笃实；坤象征大业。六四得位而与初九相应，顺从九五，又乘承九三、亲近三阳，所以说家庭富足而大吉，顺从而居位。

九五：王者使家庭广大，勿须忧虑，吉祥。

陆绩说：假是广大。九五得到尊贵之位，依托六四并与六二相应，以天下为家，所以王者拥有广大之家。天下因此端正，无所忧虑，所以吉祥。

象传说：王者使家庭广大，是因为彼此交相爱护。

虞翻说：乾象征爱，二爻称家。九三发动形成震；九五与六二相交，初九与六四相交，所以说交相爱。震象征交往。

上九：诚信而有威严，最终吉祥。

虞翻说：指九三已经变化，并与上九交换位置，形成坎。坎象征诚信，所以说有诚信；乾象征威严，上九从上位移到坤，所以有威严。交换后得到正位，因此最终吉祥。

象传说：威严带来吉祥，说的是反求自身。

虞翻说：指九三发动，坤象征自身。上九移到三位，形成既济而稳定，所以说反求自身。这就是家道端正、家庭端正而天下安定。

译释说明：古代家内角色、性别分工和礼制观念均按原文及注家语境转译，不将其作为现代家庭关系规范。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷八·睽卦集解", "底本：维基文库《周易集解（李鼎祚）/卷八》，正文行201—313。录入睽卦《序卦》、经文、《彖》《象》、六爻及页面所载注文、案语。页面用字与注文次序照录，尚未与四库影印本逐字校勘；逐段译文尚未完成。来源：https://zh.wikisource.org/zh-hans/周易集解/卷八", """《序卦》曰：家道穷必乖，故受之以睽。睽者，乖也。
崔觐曰：妇子嘻嘻，过在失节。失节则穷，穷则乖，故曰“家道穷必乘”。
(兑下离上)。睽：小事吉。
虞翻曰：大壮上之三在系，盖取无妄二之五也。小谓五。阴称小，得中应刚，故“吉”。
郑玄曰：睽，乖也。火欲上，泽欲下，犹人同居而志异也，故谓之“睽”。二五相应，君阴臣阳，君而应臣，故“小事吉。”。
《彖》曰：睽，火动而上，泽动而下。
虞翻曰：离火炎上，泽水润下也。
二女同居，其志不同行。
虞翻曰：二女，离兑也。坎为志。离上兑下。无妄震为行，巽为同，艮为居。二五易位，震巽象坏。故“二女同居，其志不同行也”。
说而丽乎明，柔进而上行，得中而应乎刚，
虞翻曰：说，兑。丽，离也。明谓干。当言大明以丽于晋。柔谓五，无妄巽为进。从二之五，故“上行。”刚谓应干，五伏阳，非应二也。与鼎五同义也。
是以小事吉。
荀爽曰：小事者，臣事也。百官异体，四民殊业，故睽而不同。刚者，君也。柔得其中，而应于君，故言“小事吉也”。
天地睽而其事同也。
王肃曰：高卑虽异，同育万物。
虞翻曰：五动乾为天，四动坤为地，故“天地睽”。坤为事也。五动体同人，故“事同矣”。
男女睽而其志通也。
侯果曰：出处虽殊，情通志合。
虞翻曰：四动艮为男，兑为女，故“男女睽”。坎为志，为通，故“其志通也”。
万物睽而其事类也。
崔觐曰：万物虽睽于形色，而生性事类言亦同也。
虞翻曰：四动，万物出乎震，区以别矣。故“万物睽”。坤为事，为类，故“其事类也”。
睽之时用大矣哉。
《九家易》曰：乖离之卦，于义不大。而天地事同，共生万物，故曰“用大”。
卢氏曰：不言义而言用者，明用睽之义至大矣。
《象》曰：上火下泽，睽。
荀爽曰：火性炎上，泽性润下，故曰“睽”也。
君子以同而异。
荀爽曰：大归虽同，小事当异。百家殊职，四民异业。文武并用，威德相反，共归于治，故曰“君子以同而异”也。
初九：悔亡，丧马勿逐自复，见恶人，无咎。《象》曰：见恶人，以避咎也。
虞翻曰：无应，四动得位，故“悔亡”。应在于坎，坎为马。四而失位，之正入坤，坤为丧。坎象不见，故“丧马”。震为逐，艮为止，故“勿逐”。坤为自，二至五，体复象，故“自复”。四动震马来，故“勿逐自复”也。离为见。恶人谓四，动入坤初，四复正。故“见恶人，以避咎矣”。
九二：遇主于巷，无咎。
虞翻曰：二动体震。震为主，为大涂，艮为径路。大道而有径路，故称“巷”。变而得正，故“无咎”而“未失道也”。
《象》曰：遇主于巷，未失道也。
虞翻曰：动得正，故“未失道”。
崔觐曰：处睽之时，与五有应。男女虽隔，其志终通。而三比焉，近不相得。遇者，不期而会。主者，三为下卦之主。巷者，出门近遇之象。言二遇三，明非背五，未为失道也。
六三：见舆曳，其牛觢。
虞翻曰：离为见。坎为车，为曳。故“见舆曳”。四动坤为牛，为类。牛角一低一仰，故称“觢”。离上而坎下，“其牛觢也”。
其人天且劓，无初有终。
虞翻曰：其人谓四，恶人也。黥额为天，割鼻为劓。无妄乾为天。震二之干五，以阴墨其天。干五之震二，毁艮，割其劓也。兑为刑人。故“其人天且劓”。失位，动得正成干，故“无初有终”。《象》曰：“遇刚”，是其义也。
《象》曰：见舆曳，位不当也。无初有终，遇刚也。
虞翻曰：动正成干，故“遇刚”。
九四：睽孤遇元夫，交孚，厉，无咎。
虞翻曰：孤，顾也。在两阴间，睽五顾三，故曰“睽孤”。震为元夫。谓二已变，动而应震。故“遇元夫”也。震为交，坎为孚，动而得正，故“交孚”，无咎。
《象》曰：交孚无咎，志行也。
虞翻曰：坎动成震，故“志行也”。
六五：悔亡，厥宗噬肤，往何咎。
虞翻曰：往得位，“悔亡”也。动而之干，干为宗。二动体噬嗑，故曰“噬”。四变时，艮为肤。故“厥宗噬肤”也。变得正成干，干为庆，故往无咎而有庆矣。
《象》曰：厥宗噬肤，往有庆也。
王弼曰：非位悔也。有应，故“悔亡”。厥宗谓二也。噬肤者，啮柔也。三虽比二，二之所噬，非妨已应者也。以斯而往，何咎之有。往必见合，故“有庆也”。
案：二兑为口，五爻阴柔，噬肤之象也。
上九：睽孤，见豕负涂，载鬼一车。
虞翻曰：睽三顾五，故曰“睽孤”也。离为见，坎为豕，为雨。四变时坤为土，土得雨，为泥涂。四动艮为背，豕背有泥，故“见豕负涂”矣。坤为鬼，坎为车，变在坎上，故“载鬼一车”也。
先张之弧，后说之壶，
虞翻曰：谓五已变，干为先。应在三。坎为弧，离为矢，张弓之象也，故“先张之弧”。四动震为后。说，犹置也。兑为口，离为大腹，坤为器。大腹有口。坎，酒在中壶之象也。之应历险以与兑。故“后说之壶”矣。
匪寇婚媾。往遇雨则吉。
虞翻曰：匪，非；坎为寇，之三历坎，故“匪寇”。阴阳相应，故“婚媾”。三在坎下，故“遇雨”。与上易位，坎象不见，各得其正，故“则吉”也。
《象》曰：遇雨之吉，群疑亡也。
虞翻曰：物三称群，坎为疑，三变坎败，故“群疑亡”矣。
""", chatGptTranslation = """**ChatGPT 翻译（逐段工作译文）**

《序卦》说：家道走到穷困处必然乖离，所以接着是睽卦；睽就是乖离。崔觐说：妇女儿童嬉笑无度，过失在于失节；失节便穷困，穷困便乖离。

睽卦下兑上离。睽：小事吉祥。虞翻说：小指六五，阴爻称小，居中而应刚，所以吉。郑玄说：睽是乖离，火向上、泽向下，就像人共同居住而志向不同；二、五相应，君阴臣阳，君主应和臣下，所以小事吉。

《彖传》说：火向上运动，泽向下运动。虞翻说：离火炎上，泽水润下。

两个女子共同居住，志向却不相同。虞翻说：二女指离、兑；坎象征志向。离上兑下，二、五换位后震、巽之象消失，所以同居而志不同。

喜悦而依附光明，柔爻向上前进，居中而应刚，因此小事吉。虞翻说：喜悦指兑，光明指离；柔爻指六五，从二位前进到五位。荀爽说：小事指臣下之事，百官职分不同、百业各异；柔爻居中而应君，所以小事吉。

天地虽有差异，所做之事却相同。王肃说：天地高低不同，却共同养育万物。虞翻说：乾象征天，坤象征地，坤为事；五爻发动形成同人，故事情相同。

男女虽有差异，志向却能相通。侯果说：出处不同，情意仍通、志向仍合。虞翻说：艮为男，兑为女；坎为志、为通，所以志通。

万物虽然不同，所做之事却有共同类别。崔觐说：万物形色虽异，生性与事务类别仍有相同之处。虞翻说：万物从震方生出而各有区别，坤象征事务和类别，所以事情仍相类。

睽卦的时势功用广大。《九家易》说：乖离之卦通常看来意义不大，但天地虽异而共同生养万物，所以功用广大。卢氏说：这里说“用”而不说“义”，是强调运用乖离之道的意义极大。

《象传》说：上火下泽，就是睽卦。荀爽说：火性炎上，泽性润下。君子因此在大原则相同的基础上容许差异。荀爽说：目标虽同，具体事务应有不同；百家分职、四民异业，文武并用、威德相济，最终都归于治理。

初九：悔恨消失；马丢了，不必追赶，它会自行返回；遇见不好的人，没有过失。虞翻说：四爻发动得位，故悔亡；坎为马，坤为丧，震为追逐，艮为停止，坤为自身，复卦之象表示马自行回来。遇见不好的人，是为了避咎。

九二：在小巷遇见主人，没有过失。虞翻说：震为主人、大路，艮为小路，大路旁有小路所以称巷；变动得正，故无咎。崔觐说：九二与九五相应，男女虽隔而志终通；九三是下卦之主，巷是出门近处相遇之象。遇见九三并非背离九五，所以未失道。

六三：看见车被拖曳，牛角一低一仰。虞翻说：离为看见，坎为车、为拖曳；坤为牛，牛角不齐称觢。那个人额头受黥、鼻子受割，开始不顺而最终有结果。虞翻说：那人指九四；“天”指黥额，“劓”指割鼻。相关卦变象征施刑；九四发动得正形成乾，所以无初有终。象传说车被拖曳是位置不当，无初有终是遇到刚爻。

九四：孤立乖离，却遇见元善之人，交往诚信；虽危险而无咎。虞翻说：四处两阴之间，顾望三、五，故称睽孤；震为元夫，坎为诚信，发动得正，所以交孚无咎。

六五：悔恨消失，宗亲咬食柔软之物，前往又有什么过失？虞翻说：九五变乾，乾为宗；九二动成噬嗑，艮为肤，所以有噬肤之象。王弼说：宗指九二，噬肤是咬柔软之物；九二所处理的并不妨碍它与六五的应援，前往必然相合而有庆。编者按：二属兑为口，五为柔爻，故有噬肤之象。

上九：孤立乖离，看见猪背负泥土，看到一车鬼怪。虞翻说：离为见，坎为猪、为雨，坤为土，雨落在土上成为泥；艮为背，所以猪背负泥。坤为鬼，坎为车，所以有一车鬼怪。

先张开弓，后来放下弓、置起酒壶。虞翻说：坎为弓，离为箭，先张弓；兑为口、坤为器，坎中有酒，后来置壶。不是盗寇，而是婚配对象；前往遇雨便吉。虞翻说：坎为寇，阴阳相应则为婚媾；遇雨后爻位各正，所以吉。象传说遇雨之吉，是因为群疑消失；坎象消退，疑惑便除。

译释说明：刑罚、鬼怪、动物等象喻依古注语境翻译；虞翻卦变说仅作为注家解释保留。"""),

    Entry("历代注疏", "唐李鼎祚《周易集解》卷八·蹇卦集解", "底本：维基文库《周易集解（李鼎祚）/卷八》，正文行314—412。录入蹇卦《序卦》、经文、《彖》《象》、六爻及页面注文、案语。照录数字页面用字，未与四库影印本逐字校勘；逐段译文尚未完成。来源：https://zh.wikisource.org/zh-hans/周易集解/卷八", """《序卦》曰：乖必有难，故受之以蹇。蹇者，难也。
崔觐曰：二女同居，其志乖而难生，故曰“乖必有难”也。
(艮下坎上)。蹇：利西南，不利东北。
虞翻曰：观上反三也。坤，西南卦。五在坤中，坎为月，月生西南，故“利西南”。往得中，谓“西南得朋”也。
不利东北。
虞翻曰：谓三也。艮东北之卦。月消于艮，丧乙灭癸。故“不利东北，其道穷也”。则东北丧朋矣。
利见大人。
虞翻曰：离为见，大人谓五。二得位应五。故“利见大人，往有功也”。
贞吉。
虞翻曰：谓五当位正邦，故“贞吉”也。
《彖》曰：蹇，难也，险在前也。见险而能止，知矣哉。
虞翻曰：离见坎险。艮为止，观干为知，故“知矣哉”。
蹇利西南，往得中也。
荀爽曰：西南谓坤。干动往居坤五，故“得中也”。
不利东北，其道穷也。
荀爽曰：东北，艮也。艮在坎下。见险而止，故“其道穷也”。
利见大人，往有功也。
虞翻曰：大人谓五。二往应五，五多功，故“往有功也”。
当位贞吉，以正邦也。
荀爽曰：谓五当尊位。正居是，群阴顺从，故能“正邦国”。
蹇之时用大矣哉。
虞翻曰：谓坎月生西南而终东北。震象出庚，兑象见丁，乾象盈甲，巽象退辛，艮象消丙，坤象穷乙，丧灭于癸，终则复始，以生万物，故“用大矣”。
《象》曰：山上有水，蹇。
崔觐曰：山上至险，加之以水，蹇之象也。
君子以反身修德。
虞翻曰：君子谓观。乾坤为身，观上反三，故“反身”。阳在三，进德修业，故“以反身修德”。孔子曰：德之不修，是吾忧也。
初六：往蹇，来誉。
虞翻曰：誉谓二，二多誉也。失位应阴，往历坎险，故“往蹇”。变而得位，以阳承二，故来而誉矣。
《象》曰：往蹇来誉，宜待时也。
虞翻曰：艮为时，谓变之正以待四也。
六二：王臣蹇蹇，匪躬之故。
虞翻曰：观干为王；坤为臣，为躬；坎为蹇也。之应涉坤，二五俱坎，故“王臣蹇蹇”。观上之三，折坤之体，臣道得正，故“匪躬之故”。《象》曰：“终无尤也”。
《象》曰：王臣蹇蹇，终无尤也。
侯果曰：处艮之二，上应于五。五在坎中，险而又险。志在匡弼，匪惜其躬。故曰“王臣蹇蹇，匪躬之故”。辅臣以此，终无尤也。
九三：往蹇，来反。
虞翻曰：应正历险，故“往蹇”。反身据二，故“来反”也。
《象》曰：往蹇来反，内喜之也。
虞翻曰：内，谓二阴也。
六四：往蹇，来连。
虞翻曰：连，辇。蹇，难也。在两坎间，进则无应，故“往蹇”。退初介三，故“来连”也。
《象》曰：往蹇来连，当位实也。
荀爽曰：蹇难之世，不安其所。欲往之三，不得承阳。故曰“往蹇”也。来还承五，则与至尊相连，故曰“来连”也。处正承阳，故曰“当位实也”。
九五：大蹇，朋来。
虞翻曰：当位正邦，故“大蹇”。睽兑为朋，故“朋来”也。
《象》曰：大蹇朋来，以中节也。
干宝曰：在险之中，而当王位，故曰：“大蹇”。此盖以托文王为纣所囚也。承上据四应二，众阴并至。此盖以托四臣能以权智相救也。故曰“以中节也”。
上六：往蹇，来硕吉，利见大人。
虞翻曰：阴在险上，变失位，故“往蹇”。硕谓三，艮为硕，退来之三，故“来硕”。得位有应，故“吉”也。离为见，大人谓五，故“利见大人”矣。
《象》曰：往蹇来硕，志在内也。利见大人，以从贵也。
侯果曰：处蹇之极，体犹在坎，水无所之，故曰“往蹇”。来而复位，下应于三，三德硕大，故曰“来硕”。三为内主，五为大人。若志在内，心附于五。则“利见大人”也。
案：三互体离，离为明目，五为大人，利见大人之象也。
""", chatGptTranslation = """**ChatGPT 翻译（逐段工作译文）**

《序卦》说：乖离必然产生困难，所以接着是蹇卦；蹇就是艰难。崔觐说：两女同居而志向乖异，困难由此产生。

蹇卦下艮上坎。蹇：利于西南，不利于东北。虞翻说：观卦上爻返回三位。坤为西南，九五处坤中；坎为月，月在西南出现，所以利西南，前往得中。不利东北，指九三；艮为东北，月在艮方消退，所以道路穷尽、失去同伴。

利于见大人。虞翻说：离为见，大人指九五；九二得位而应五，所以前往有功。守正吉祥。虞翻说：九五当位而端正邦国。

《彖传》说：蹇就是艰难，危险在前；看见危险而能停止，真有智慧。虞翻说：离为见，坎为险，艮为止，乾为知。

蹇利西南，是前往而得到中位。荀爽说：乾动而居坤五，故得中。不利东北，是道路穷尽。荀爽说：艮在坎下，看见危险而停止，所以穷。利见大人，是前往有功。虞翻说：大人指五，二往应五，五多功。当位守正而吉，是为了端正邦国。荀爽说：五居尊位，群阴顺从，故能正邦。

蹇卦的时势功用广大。虞翻说：以月行说明阴阳循环：月在西南生成，终在东北消退；震、兑、乾、巽、艮、坤各配月行方位，终而复始，以此生养万物。

《象传》说：山上有水，就是蹇卦。崔觐说：山上已经险峻，又加上水，更显艰难。君子因此反省自身、修养德行。虞翻说：乾坤象征身体，观卦上爻返回三位称反身；阳居三位，象征增进德行、修养事业。

初六：前往艰难，返回则有声誉。虞翻说：二爻多誉；初六失位，前往历坎险，故蹇；变动得正并承阳，所以返回有誉。象传说应当等待时机，艮象征时。

六二：王臣处境艰难又艰难，并非为了自身。虞翻说：乾为王，坤为臣、为身，坎为蹇；二、五都处险中，所以王臣艰难。观卦上爻到三位，使臣道得正，所以不是为自身。侯果说：六二上应九五，五在坎险之中；臣子志在辅佐君王，不顾惜自身，最终没有过失。

九三：前往艰难，返回自身。虞翻说：前往要历险，所以艰难；返回而依托六二，所以来反。象传说内部的人因此喜悦，虞翻说内部指二阴。

六四：前往艰难，返回便能相连。虞翻说：四处两个坎险之间，前进无应，所以艰难；退回初位，由九三相隔，所以相连。荀爽说：退回承接九五，便与尊位相连；处正而承阳，所以当位有实。

九五：大有艰难，朋友前来。虞翻说：五当位而正邦，故大蹇；睽卦兑象征朋友，所以朋友来。象传说以中道合节。干宝说：五居王位而在险中，借此寄托文王被纣囚禁；承上、据四、应二，众阴并来，象征四臣以权智相救。

上六：前往艰难，返回则硕大吉祥，利于见大人。虞翻说：阴处险上，前往失位；硕指九三，艮为硕，返回九三而得位有应，所以吉。离为见，大人指五。侯果说：上六处蹇极，仍在坎险；返回下应九三，三德硕大，五为大人，志在内而心附五，便利于见大人。

编者按：三爻互体有离，离为明目；五为大人，因此有利见大人之象。

译释说明：月行、纳甲与卦变按虞翻象数体系转述；文王被囚是干宝的历史寄托解释，并非经文直接叙事。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷八·解卦集解", "底本：维基文库《周易集解（李鼎祚）/卷八》，正文行413—519。录入解卦《序卦》、经文、《彖》《象》、六爻及页面注文、案语。照录数字页面用字，未与四库影印本逐字校勘；逐段译文尚未完成。来源：https://zh.wikisource.org/zh-hans/周易集解/卷八", """《序卦》曰：物不可以终难，故受之以解。解者，缓也。
崔觐曰：蹇终则来，硕吉，利见大人，故言物不可以终难。故受之以解。
(坎下震上)。解：利西南，
虞翻曰：临初之四。坤，西南卦。初之四，得坤众。故“利西南，往得众也”。
无所往，其来复吉。
虞翻曰：谓四本从初之四，失位于外，而无所应，故“无所往”。宜来反初，复得正位，故“其来复吉”也。二往之五，四来之初，成屯，体复象，故称“来复吉”矣。
有攸往，夙吉。
虞翻曰：谓二也。夙，早也。离为日，为甲。日出甲上，故早也。九二失正，早往之五，则吉。故“有攸往，夙吉，往有功也”。
《彖》曰：解，险以动，动而免乎险，解。
虞翻曰：险，坎。动，震。解，二月。雷以动之，雨以润之，物咸孚甲，万物生震。震出险上，故“免乎险”也。
解利西南，往得众也。
荀爽曰：干动之坤而得众，西南众之象也。
无所往，
荀爽曰：阴处尊位，阳无所往也。
其来复吉，乃得中也。
荀爽曰：来复居二，处中成险，故曰“复吉”也。
有攸往，夙吉，往有功也。
荀爽曰：五位无君，二阳又卑，往居之者则吉。据五解难，故“有功也”。
天地解而雷雨作，
荀爽曰：谓乾坤交通，动而成解卦，坎下震上，故“雷雨作”也。
雷雨作而百果草木皆甲宅。
荀爽曰：解者，震世也。仲春之月，草木萌牙。雷以动之，雨以润之，曰以烜之，故“甲宅”也。
解之时大矣哉。
王弼曰：无所而不释也。难解之时，非治难时也。故不言用也。体尽于解之名，无有幽隐，故不曰义也。
《象》曰：雷雨作，解。君子以赦过宥罪。
虞翻曰：君子谓三。伏阳出，成大过。坎为罪人则大过象坏，故“以赦过”。二四失位，皆在坎狱中。三出体干，两坎不见。震喜兑说，罪人皆出，故以“宥罪”。谓三入则赦过，出则宥罪。公用射隼以解悖，是其义也。
初六：无咎。
虞翻曰：与四易位，体震得正，故“无咎”也。
《象》曰：刚柔之际，义无咎也。
虞翻曰：体屯初震，刚柔始交，故“无咎”也。
九二：田获三狐，得黄矢，贞吉。
虞翻曰：二称田。田，猎也。变之正，艮为狐。坎为弓。离为黄矢，矢贯狐体。二之五，历三爻。故“田获三狐，得黄矢。”之正得中，故“贞吉”。
《象》曰：九二贞吉，得中道也。
虞翻曰：动得正，故得中道。
六三：负且乘，
虞翻曰：负，倍也。二变时艮为背，谓三以四艮倍五也。五来寇三时，坤为车，三在坤上，故“负且乘”。小人而乘君子之器《象》曰：“亦可丑也”。
致寇至，贞吝。
虞翻曰：五之二成坎，坎为寇盗。上位慢五，下暴于二，慢藏悔盗，故“致寇至，贞吝”。《象》曰：“自我致戎，又谁咎也。”
《象》曰：负且乘，亦可丑也。自我致戎，又谁咎也。
虞翻曰：临坤为丑也。坤为自我。以离兵伐三，故转寇为戎。艮手招盗，故“谁咎也”。
九四：解而母，朋至斯孚。
虞翻曰：二动时艮为指。四变之坤为母，故“解而母”。临兑为朋，坎为孚。四阳从初，故“朋至斯孚”矣。
《象》曰：解而母，未当位也。
王弼曰：失位不正，而比于三。故三得附之，为其拇也。三为之拇，则失初之应，故“解其拇”，然后“朋至斯孚”而信矣。
案：九四体震，震为足。三在足下，拇之象。
六五：君子惟有解，吉。有孚于小人。
虞翻曰：君子谓二。之五得正成坎，坎为心。故“君子惟有解，吉”。小人谓五。阴为小人，君子升位，则小人退在二，故“有孚于小人”。坎为孚也。
《象》曰：君有解，小人退也。
虞翻曰：二阳上之五，五阴小人退之二也。
上六：公用射隼于高庸之上，获之，无不利。
虞翻曰：上应在三。公，谓三伏阳也。离为隼。三失位，动出成干，贯隼入大过，死象故“公用射隼于高庸之上，获之，无不利”也。
案：二变时体艮。艮为山，为宫阙，三在山半，高塘之象也。
《象》曰：公用射隼，以解悖也。
虞翻曰：坎为悖，三出成干，而坎象坏，故“解悖也”。
《九家易》曰：隼，鸷鸟也。今捕食雀者，其性疾害，喻暴君也。阴盗阳位，万事悖乱，今射去之，故曰“以解悖也。”
""", chatGptTranslation = """**ChatGPT 翻译（逐段工作译文）**

《序卦》说：事物不能永远处于艰难之中，所以接着是解卦。解就是舒缓、解除。

崔觐说：蹇卦最终以返回而得硕大吉祥、利于见大人为结局，因此说事物不能永远艰难，接着便是解卦。

解卦下坎上震。解：利于西南方向。

虞翻说：这是临卦初爻移到四位。坤是西南方之卦。初爻移到四位，得到坤众，所以利于西南，前往便能得到众人。

没有事情需要前往时，返回原处便吉祥。

虞翻说：指九四原本从初爻移到四位，在外失位又没有应援，所以说没有可去之处。应当返回初位，重新得到正位，因此返回便吉祥。九二前往五位、九四返回初位，形成屯卦，卦体又有复卦之象，所以称返回吉祥。

有所前往，及早行动便吉祥。

虞翻说：指九二。夙是早。离象征太阳，也象征甲日；太阳出现在甲位之上，所以表示早。九二失正，及早前往九五便吉祥，因此说有所前往、及早吉祥，前往会有功效。

《彖传》说：处于险难之中而发动，发动后脱离险难，这就是解。

虞翻说：险是坎，动是震。解卦对应二月。雷使万物发动，雨水滋润万物，万物都孕育种子，万物从震方生出。震出现在险卦之上，所以脱离险难。

解卦利于西南，是因为前往能够得到众人。

荀爽说：乾发动进入坤而得到众人，西南就是众人的象征。

没有事情需要前往。

荀爽说：阴爻处在尊位，阳爻没有可去之处。

返回原处便吉祥，是因为回到中位。

荀爽说：返回后居于二位，处在中位而形成坎，所以说返回吉祥。

有所前往，及早行动吉祥，是因为前往有功。

荀爽说：五位没有君主，九二阳爻又处于卑下位置；前往居于五位便吉祥。凭借五位解除困难，所以有功。

天地解除闭塞，雷雨便兴起。

荀爽说：乾坤相交，发动而形成解卦；下坎上震，所以雷雨兴起。

雷雨兴起，百果草木都萌生种壳。

荀爽说：解卦以震为主，仲春时节草木萌芽。雷使它们发动，雨水滋润它们，太阳使它们温暖，所以种壳萌生。

解卦所代表的时势意义真是广大啊！

王弼说：没有什么不能解除。困难解除的时期，不是继续治理困难的时期，所以这里不谈具体功用。解卦之名已经把卦体的意义说尽，没有幽隐不明之处，所以不说义。

《象传》说：雷雨兴起，就是解卦。君子因此赦免过失、宽宥罪人。

虞翻说：君子指九三。伏藏的阳爻出现，形成大过卦。坎象征罪人，若大过之象破坏，便是赦免过失。九二、九四失位，都处在坎狱之中；九三出而形成乾，两坎之象消失。震象征喜悦，兑象征欣悦，罪人因此得以释放，所以宽宥罪人。九三进入时象征赦过，出去时象征宥罪。“公用射隼”以解除悖乱，就是这个意思。

初六：没有过失。

虞翻说：与九四交换位置，形成震而得正，所以没有过失。

象传说：刚柔相交，按义理没有过失。

虞翻说：卦体为屯，初爻是震，刚柔刚开始交接，所以没有过失。

九二：田猎捕获三只狐狸，得到黄色箭矢，守正吉祥。

虞翻说：二爻称田，田就是狩猎。变动后得正，艮象征狐狸，坎象征弓，离象征黄色箭矢；箭矢贯穿狐狸身体。九二前往五位，经过三爻，所以说捕获三狐、得到黄矢。变动后得正并居中，所以守正吉祥。

象传说：九二守正吉祥，是因为得到中道。

虞翻说：发动后得正，所以得到中道。

六三：背着东西又乘坐车舆。

虞翻说：负是背负。九二变化时艮象征背，指六三背负九四之艮而处于九五之下。九五来侵犯六三时，坤象征车；六三处在坤上，所以说背负又乘车。小人却乘坐君子的器物，象传说这也很可耻。

招致盗寇到来，守正仍有羞吝。

虞翻说：九五到二位形成坎，坎象征盗寇。六三上慢待九五，下欺凌九二；收藏不谨慎而招来盗贼，所以招致盗寇，守正也有羞吝。象传说灾祸由自己招来，又能责怪谁呢？

象传说：背负又乘车，确实可耻；自己招来兵戎，又能责怪谁？

虞翻说：临卦的坤象征丑恶，也象征自己。以离象征兵器来攻伐六三，所以把盗寇转为兵戎。艮象征手，手招来盗贼，所以说又能责怪谁。

九四：解除你的脚拇指，朋友便来到并产生信任。

虞翻说：九二发动时艮象征手指；九四变化为坤，坤象征母，所以说解除拇指。临卦兑象征朋友，坎象征诚信。九四阳爻从初位而来，所以朋友到来便有诚信。

象传说：解除拇指，是因为位置不恰当。

王弼说：九四失位不正，又与六三相邻。六三依附它，成为它的拇指；六三成为拇指，便失去与初六的应援。因此解除拇指之后，朋友才会到来并彼此信任。

编者按：九四属于震体，震象征脚，六三在脚下，形成拇指之象。

六五：君子只要解除困难，便吉祥；对小人也能取得信任。

虞翻说：君子指九二。九二前往五位而得正，形成坎，坎象征心，所以说君子解除困难便吉祥。小人指六五；阴爻象征小人。君子升到五位，小人便退到二位；坎象征诚信，所以说对小人也能取得信任。

象传说：君主能够解除困难，小人便退去。

虞翻说：二阳上升到五位，五位的阴爻小人退到二位。

上六：公在高大的城墙上射隼，射中捕获，没有不利。

虞翻说：上六与九三相应。公指九三伏藏的阳爻。离象征隼；九三失位，发动后形成乾，贯穿隼而进入大过，形成死亡之象，所以说公在高墙上射隼并捕获，没有不利。

编者按：九二变化时卦体含艮，艮象征山，也象征宫阙；九三处在山腰，形成高墙之象。

象传说：公射隼，是为了解除悖乱。

虞翻说：坎象征悖乱。九三出现而形成乾，坎象消失，所以解除悖乱。

《九家易》说：隼是凶猛的鸟，现在指捕食雀鸟的猛禽，性情迅疾而残害其他生物，用来比喻暴虐的君主。阴爻盗占阳爻的位置，万事悖乱；如今把它射除，所以说解除悖乱。

译释说明：原文中“而母”等疑难字句依现有录文及王弼注释保留解释层次；涉及虞翻卦变说，仅作为注家观点呈现。"""),

    Entry("历代注疏", "唐李鼎祚《周易集解》卷八·损卦集解", "底本：维基文库《周易集解（李鼎祚）/卷八》，正文行520—631。录入损卦《序卦》、经文、《彖》《象》、六爻及页面注文、案语。照录数字页面用字，未与四库影印本逐字校勘；逐段译文尚未完成。来源：https://zh.wikisource.org/zh-hans/周易集解/卷八", """《序卦》曰：缓必有所失，故受之以损。
崔觐曰：宥罪缓死，失之于侥幸，有损于政刑，故言缓必有所失，故受之以损者也。
(兑下艮上)。损：有孚，元吉，无咎可贞，利有攸往。曷之用，二簋可用享。
郑玄曰：艮为山，兑为泽。互体坤，坤为地。山在地上，泽在地下，泽以自损增山之高也。犹诸侯损其国之富以贡献于天子，故谓之损矣。
有孚。无吉，无咎。可贞，利有攸往。
虞翻曰：泰初之上，损下益上，以据二阴，故“有孚，元吉，无咎”。艮男居上，兑女在下，男女位正，故“可贞利有攸往”矣。
曷之用？二簋可用享。
崔觐曰：曷，何也。言其道上行，将何所用，可用二簋而享也。以喻损下益上，惟有乎心，何必竭于不足而补有余者也。
《彖》曰：损，损下益上，其道上行。
蜀才曰：此本泰卦。案：坤之上六，下处干三。干之九三，上升坤六。损下益上者也。阳德上行，故曰“其道上行”矣。
损而有孚，
荀爽曰：谓损干之三居上。孚，二阴也。
元吉，无咎。
荀爽曰：居上据阴，故“元吉，无咎”，以未得位，嫌于咎也。
可贞。
荀爽曰：少男在上，少女虽年尚幼，必当相承，故曰“可贞”。
利有攸往。
荀爽曰：谓阳利往居上。损者，损下益上，故利往居上。
曷之用？二簋可用享。
荀爽曰：二簋，谓上体二阴也。上为宗庙。簋者，宗庙之器，故可享献也。
二簋应有时。
虞翻曰：时谓春秋也。损二之五，震二月，益正月，春也。损七月，兑八月，秋也。谓春秋祭祀，以时思之。艮为时，震为应，故“应有时”也。
损刚益柔有时。
虞翻曰：谓冬夏也。二五已易成益。坤为柔。谓损。益上之三成既济，坎冬离夏。故“损刚益柔有时”。
损益盈虚，与时偕行。
虞翻曰：干为盈，坤为虚，损刚益柔，故“损益盈虚”。谓泰初之上，损二之五，益上之三，变通趋时，故“与时偕行”。
《象》曰：山下有泽，损。君子以征忿窒欲。
虞翻曰：君子，泰干。乾阳刚武，为忿。坤阴吝啬，为欲。损干之初成兑说，故“征忿”。初上据坤，艮为止，故“窒欲也”。
初九：祀事遄往，无咎，酌损之。
虞翻曰：祀，祭祀。坤为事，谓二也。遄，速。酌，取也。二失正，初利二速往，合志于五，得正无咎，已得之应，故“遄往无咎酌损之”。《象》曰“上合志也”。祀，旧作巳也。
《象》曰：祀事遄往，上合志也。
虞翻曰：终成既济，谓二上合志于五也。
九二：利贞，征凶，弗损益之。
虞翻曰：失位当之正，故“利贞”。征，行也。震为征，失正毁折，故“不征”。之五则凶，二之五成益，小损大益，故“弗损益之”矣。
《象》曰：九二利贞，中以为志也。
虞翻曰：动体离中，故“为志也”。
六三：三人行，则损一人。
虞翻曰：泰干三爻，为三人；震为行，故“三人行”。损初之上，故“则损一人”。
一人行，则得其友。
虞翻曰：一人谓泰初之上，损刚益柔，故“一人行”。兑为友，初之上，据坤应兑，故“则得其友”。言致一也。
《象》曰：一人行，三则疑也。
虞翻曰：坎为疑，上益三成坎，故“三则疑”。荀爽曰：一阳在上，则教令行。三阳在下，则民众疑也。
六四：损其疾，使遄有喜，无咎。
虞翻曰：四，谓二也。四得位，远应初。二疾上五，已得承之。谓二之五，三上复。坎为疾也。阳在五称喜。故“损其疾，使遄有喜”。二上体观，得正承五，故“无咎”矣。
《象》曰：损其疾，亦可喜也。
蜀才曰：四当承上，而有初应，必上之所疑矣。初，四之疾也。宜损去其初，使上遄喜。
虞翻曰：二上之五，体大观象，故“可喜也”。
六五：或益之十朋之龟，弗克违，元吉。
虞翻曰：谓二五已变成益，故“或益之”。坤数十，兑为朋，三上失位，三动离为龟。十谓神灵摄宝文、筮山泽水火之龟也。故“十朋之龟”。三上易位，成既济，故“弗克违，元吉”矣。
《象》曰：六五元吉，自上佑也。
侯果曰：内柔外刚，龟之象也。又体兑艮，互有坤震。兑为泽龟，艮为山龟，坤为地龟，震为木龟，坤数又十，故曰“十朋”。朋，类也。六五处尊，损已奉上。人谋允叶，龟墨不违。故能延上九之佑，而来十朋之益。所以大吉也。崔觐曰：或之者，疑之也。故用元龟。价值二十大贝，龟之最神贵者。以决之，不能违其益之义。故获元吉。双贝曰朋也。
上九：弗损益之，无咎，贞吉。
虞翻曰：损上益三也。上失正，之三得位，故“弗损益之，无咎，贞吉”。动成既济，故“大得志”。
利有攸往，得臣无家。
虞翻曰：谓三往之上，故“利有攸往”。二五已动成益，坤为臣。三变据坤，成家人，故曰“得臣”。动而应三，成既济，则家人坏，故“无家”。
王肃曰：处损之极，损极则益，故曰“弗损益之”。非无咎也。为下所益，故“无咎”。据五应三，三阴上附，外内相应，上下交接，正之吉也。故“利有攸往”矣。刚阳居上，群下共臣，故曰“得臣”矣。得臣则万方一轨，故“无家”也。
《象》曰：弗损益之，大得志也。
虞翻曰：谓二五已变，上下益三，成既济定。离坎体正，故“大得志”。
""", chatGptTranslation = """**ChatGPT 翻译（逐段工作译文）**

《序卦》说：宽缓而不加节制，必然有所损失，所以接着是损卦。

崔觐说：宽宥罪人、延缓死刑，有时会造成侥幸，损害政令与刑罚，所以说宽缓必有所失，接着便是损卦。

损卦下兑上艮。损：有诚信，大吉，没有过失，可以守正；利于有所前往。用什么祭祀？两簋薄祭也可以用于献享。

郑玄说：艮为山，兑为泽，互体坤为地。山在地上，泽在地下，泽水减少而使山增高，就像诸侯减少本国财富，向天子进献，所以称损。

有诚信，大吉，没有过失，可以守正，利于有所前往。

虞翻说：这是泰卦初爻移到上位，损减下方而增益上方，以便承据两个阴爻，所以有诚信、大吉、无咎。艮男居上，兑女居下，男女位置端正，所以可以守正并有所前往。

用什么祭祀？两簋也可以用于献享。

崔觐说：曷就是何。意思是说，损下益上的道理向上施行，需要用什么？只用两簋也可以祭享。用来比喻损减下方、增益上方，关键在于诚心，何必把不足之物全部拿来补充已经充足的部分？

《彖传》说：损，是减少下方而增益上方，其道向上运行。

蜀才说：此卦本于泰卦。坤的上六下降到乾的九三，乾的九三上升到坤的上六，这就是损下益上。阳刚之德向上运行，所以说其道上行。

损减而有诚信。

荀爽说：指乾的九三被移到上位。诚信象征上卦的两个阴爻。

大吉，没有过失。

荀爽说：居于上位并承据阴爻，所以大吉、无咎；但尚未完全得位，因此仍可能被怀疑有过失。

可以守正。

荀爽说：少男在上，少女虽然年纪尚小，仍应当承顺，所以说可以守正。

利于有所前往。

荀爽说：指阳爻适宜前往上位。损卦的意义是损下益上，所以利于向上前往。

用什么祭祀？两簋也可以用于献享。

荀爽说：两簋指上卦的两个阴爻。上卦为宗庙，簋是宗庙祭器，所以可以用于献享。

两簋的祭献应当依时而行。

虞翻说：时指春秋。损卦二爻到五位，形成震，象征二月；益卦对应正月，属于春。损卦对应七月，兑对应八月，属于秋。指春秋两季祭祀，应当按时追念。艮象征时，震象征应，所以说应当适时。

损刚益柔也有适当的时机。

虞翻说：这里指冬夏。二、五爻交换后形成益卦，坤象征柔。益卦上爻到三位形成既济，坎象征冬，离象征夏，所以说损刚益柔有时。

损益、盈满与虚空，都随着时势一同运行。

虞翻说：乾象征盈满，坤象征虚空。损刚益柔就是损益盈虚。泰卦初爻移到上位，损卦二爻移到五位，益卦上爻移到三位，变化通达而顺应时势，所以与时偕行。

《象传》说：山下有泽，就是损卦。君子因此克制愤怒、抑制欲望。

虞翻说：君子指泰卦乾。乾阳刚武，象征愤怒；坤阴吝啬，象征欲望。乾初爻移入坤而形成兑悦，所以克制愤怒；初爻上升而承据坤，艮象征停止，所以抑制欲望。

初九：祭祀之事应迅速前往，没有过失；酌量减少祭品。

虞翻说：祀是祭祀，坤象征事务，指九二。遄是迅速，酌是取用。九二失正，初九有利于九二迅速前往，与九五志向相合并得到正位，便没有过失；初九已经得到自己的应援，所以说迅速前往、酌量减损。象传说与上方志向相合。旧本“祀”字作“巳”。

象传说：祭祀之事迅速前往，是因为与上方志向相合。

虞翻说：最终形成既济，指九二上升到五位，与五志向相合。

九二：利于守正；出征凶险；不必损减自己，反而应当有所增益。

虞翻说：九二失位，应当变动而得正，所以利于守正。征是行动，震象征出行；失正会遭毁折，所以不宜出征。前往五位则凶。九二到五位形成益卦，是小处损减、大处增益，所以说不损反而增益。

象传说：九二利于守正，是因为以居中为志。

虞翻说：发动后形成离卦，离居中，所以说以中为志。

六三：三个人同行，就会损失一个人。

虞翻说：泰卦乾有三爻，象征三个人；震象征行动，所以说三人同行。初爻移到上位，因此损失一人。

一个人独行，就会得到同伴。

虞翻说：一人指泰卦初爻移到上位，损刚益柔，所以说一人行。兑象征朋友；初爻到上位后承据坤并与兑相应，所以得到朋友。意思是专一。

象传说：一人独行，三人同行就会产生疑惑。

虞翻说：坎象征疑惑。上爻增益三位后形成坎，所以三人便产生疑惑。

荀爽说：一个阳爻在上，教令便能通行；三个阳爻在下，民众就会疑惑。

六四：减除自身的疾病，使它迅速消除，便有喜庆，没有过失。

虞翻说：六四指九二。六四得位，远应初九；九二的疾病移到五位，六四便承接它。九二到五位后，三爻又上升。坎象征疾病；阳爻居五位称喜，所以说减除疾病、迅速有喜。九二上升后形成观卦，得正而承五，因此没有过失。

象传说：减除疾病，也确实值得欢喜。

蜀才说：六四本应承接上位，却与初九相应，因此必定受到上方怀疑。初九就是它的疾病，应当减去初九，使上方迅速欢喜。

虞翻说：九二上升到五位，卦体形成大观之象，所以值得欢喜。

六五：有人赠送价值十朋的龟，占问时不能违背其兆，大吉。

虞翻说：二、五爻已经变化而形成益卦，所以说有人增益。坤数为十，兑象征朋；三爻发动形成离，离象征龟，因此称十朋之龟。三、上爻交换位置形成既济，所以不能违背其占兆，大吉。

象传说：六五大吉，是因为得到上天的帮助。

侯果说：内柔外刚，具有龟的形象；又有兑、艮两体，互体含坤、震。兑为泽龟，艮为山龟，坤为地龟，震为木龟；坤数为十，所以称十朋。朋是同类。六五居尊位，损己奉上；人们的谋议一致，龟占也没有异议。因此能够得到上九的帮助，获得十朋之益，所以大吉。

崔觐说：或表示疑问，因此使用大龟占问。十朋是价值二十大贝，属于最神贵的龟；用它决断，不能违背增益的道理，所以获得大吉。双贝称朋。

上九：不再损减，反而得到增益，没有过失，守正吉祥。

虞翻说：损上益三。上九失正，移到三位便得位，所以不再损减而得到增益，没有过失，守正吉祥。发动后形成既济，因此大得志。

利于有所前往，得到臣属而没有私家。

虞翻说：指六三前往上位，所以利于前往。二、五已经发动形成益卦，坤象征臣。六三变化后承据坤，形成家人卦，所以说得到臣属；发动后与六三相应，形成既济，家人卦之象又被破坏，所以说没有私家。

王肃说：处于损卦终点，损减到极处便转为增益，所以说不再损减而有所增益。并不是完全没有过失，而是因为受到下方增益，所以无咎。上九承据九五并与六三相应，三阴附从于上，内外相应，上下交接，端正而吉祥，因此利于前往。阳刚居上，群臣共同归附，所以说得到臣属；臣属归附，天下政令统一，所以说没有私家。

象传说：不再损减而得到增益，是因为大大实现了自己的志向。

虞翻说：二、五爻已经变化，上下增益六三，形成既济而稳定。离、坎两体端正，所以大得志。

译释说明：本条将“十朋之龟”等按注家所引古代占龟、贝币解释转述；具体数术解释属于各注家说法，不作为统一定论。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷八·益卦集解", "底本：维基文库《周易集解（李鼎祚）/卷八》，正文行632—761。录入益卦《序卦》、经文、《彖》《象》、六爻及页面注文。照录数字页面用字，未与四库影印本逐字校勘；逐段译文尚未完成。来源：https://zh.wikisource.org/zh-hans/周易集解/卷八", """《序卦》曰：损而不已必益，故受之以益。
崔觐曰：损终则弗损益之，故言损而不已必益也。
(震下巽上)。益：利有攸往。
虞翻曰：否上之初也。损上益下，其道大光。二利往坎应五，故“利有攸往，中正有庆”也。
利涉大川。
虞翻曰：谓三失正。动成坎，体涣；坎为大川，故“利涉大川”。涣，舟楫象。木道乃行也。
郑玄曰：阴阳之义，阳称为君，阴称为臣。今震一阳二阴，臣多于君矣。而四体巽，之不应初，是天子损其所有以下诸侯也。人君之道，以益下为德，故谓之益也。震为雷，巽为风。雷动风行，二者相成，犹人君出教令，臣奉行之，故“利有攸往”。坎为大川，故“利涉大川”矣。
《彖》曰：益，损上益下，
蜀才曰：此本否卦。
案：干之上九下处坤初。坤之初六，上升干四。损上益下者。
民说无疆。
虞翻曰：上之初。坤为无疆，震为喜笑；以贵下贱，大得民，故“说无疆”矣。
自上下下，其道大光。
虞翻曰：干为大明，以干照坤，故“其道大光”。或以上之三，离为大光矣。
利有攸往，中正有庆。
虞翻曰：中正谓五，而二应之，干为庆也。
利涉大川，木道乃行。
虞翻曰：谓三动成涣。涣，舟楫象。巽木得水，故“木道乃行”也。
益动而巽，日进无疆。
虞翻曰：震三动为离，离为日。巽为进，坤为疆。日与巽俱进，故曰“进无疆”也。
天施地生，其益无方。
虞翻曰：干下之坤，震为出生，万物出震，故“天施地生”。阳在坤初为无方，日进无疆，故“其益无方”也。
凡益之道，与时偕行。
虞翻曰：上来益三，四时象正。艮为时，震为行，与损同义，故“与时偕行”也。
《象》曰：风雷，益。君子以见善则迁，有过则改。
虞翻曰：君子，谓干也。上之三，离为见。干为善，坤为过。坤三进之干四，故“见善则迁”。干上之坤初，改坤之过。体复象，复以自知，故“有过则改”也。
初九：利用为大作，元吉，无咎。
虞翻曰：大作谓耕播耒耨之利，盖取诸此也。坤为用，干为大，震为作，故“利用为大作”。体复初得正，朋来无咎，故“元吉，无咎”。震，二月卦，日中星鸟，敬授民时，故以耕播也。
《象》曰：元吉，无咎，下不厚事也。
侯果曰：大作，谓耕植也。处益之始，居震之初。震为稼穑，又为大作。益之大者，莫大耕植。故初九之利，“利为大作”。若能不厚劳于下民，不夺时于农畯，则“大吉，无咎”矣。
六二：或益之十朋之龟，弗克违，永贞吉。
虞翻曰：谓上从外来益初也，故“或益之”。二得正远应，利三之正，已得承之。坤数十，损兑为朋；谓三变离为龟，故“十朋之龟”。坤为永，上之三得正，故“永贞吉”。
王用享于帝，吉。
虞翻曰：震称帝。王谓五。否干为王。体观象。艮为宗庙。三变，折坤牛。体噬嗑食。故“王用享于帝”。得位，故“吉”。
干宝曰：圣王先成其民而后致力于神，故“王用享于帝”。在巽之宫，处震之象，是则苍精之帝同始祖矣。
《象》曰：或益之，自外来也。
虞翻曰：干上称外，来益初也。
六三：益之用凶事，无咎。
虞翻曰：坤为事，三多凶。上来益三得正。故“益用凶事，无咎”。
有孚中行，告公用圭。
虞翻曰：公谓三，伏阳也。三动体坎，故“有孚”。震为中行，为告，位在中，故曰“中行”。三，公位。干为圭，干之三，故“告公用圭”。圭。桓圭也。
《九家易》曰：天子以尺二寸玄圭事天，以九寸事地也。上公执桓圭。九寸；诸侯执信圭。七寸；诸伯执躬圭，七寸；诸子执谷璧。五寸；诸男执蒲璧，五寸。五等诸侯，各执之以朝见天子也。
《象》曰：益用凶事，固有之矣。
虞翻曰：三上失正当变，是“固有之”。
干宝曰：固有如桓文之徒，罪近篡弑，功实济世。六三失位，而体奸邪。处震之动，怀巽之权，是矫命之士。争夺之臣，桓文之爻也。故曰“益之用凶事”。在益之家，而居坤中，能保社稷，爱抚人民，故曰“无咎”。既乃中行，近仁，故曰“有孚中行”。然后俯列盟会，仰致锡命，故曰“告公用圭”。
六四：中行，告公从，
虞翻曰：中行谓震位在中。震为行，为从，故“中行”。公谓三。三上失位。四利三之正，已得以为实，故“告公从”矣。
利用为依迁邦。
虞翻曰：坤为邦。迁，从也。三动坤从，故“利用为依迁邦也”。
《象》曰：告公从，以益志也。
虞翻曰：坎为志，三之上，有两坎象，故“以益志也”。
崔觐曰：益其勤王之志也。居益之时，履当其位，与五近比，而四上公，得藩屏之寄，为依从之国，若周平王之东迁晋郑，是从也。五为天子，益其忠志以敕之。故言“中行告公从，利用为依迁国”矣。
九五：有孚惠心，勿问，元吉。
虞翻曰：谓三上也。震为问。三上易位，三五体坎，已成既济。坎为心。故“有孚惠心，勿问，元吉”。故《象》：“勿问之矣。”
有孚惠我德。
虞翻曰：坤为我，干为德，三之上体坎，为孚，故“惠我德”。《象》曰“大得志”。
《象》曰：有孚惠心，勿问之矣。惠我德，大得志也。
崔觐曰：居中履尊，当位有应。而损上之时，自一以损已为念。虽有孚于国，惠心及下，终不言以彰已功。故曰“有孚惠心，勿问”。问，犹言也。如是，则获元吉，且为下所信而怀已德，故曰“有孚惠我德”。君虽不言，人惠其德，则我“大得志也”。
上九：莫益之，
虞翻曰：莫，无也。自非上无益初者，唯上当无应故“莫益之”矣。
或击之，
虞翻曰：谓上不益初，则以剥灭干。艮为手，故“或击之”。
立心勿恒，凶。
虞翻曰：上体巽，为进退，故“勿恒”。动成坎心，以阴乘阳，故“立心勿恒，凶”矣。
《象》曰：莫益之，遍辞也。
虞翻曰：遍，周匝也。三体刚凶，故至上应乃益之矣。
或击之，自外来也。
虞翻曰：外谓上。上来之三，故曰“自外来也”。
""", chatGptTranslation = """**ChatGPT 翻译（逐段工作译文）**

《序卦》说：损减而不停止，必然转为增益，所以接着是益卦。

崔觐说：损卦最终说不再损减、反而增益，因此说损减如果不停，就必然转为增益。

益卦下震上巽。益：利于有所前往。

虞翻说：这是否卦上九移到初位。损上益下，其道大为光明。九二利于前往坎位并与九五相应，所以说利于有所前往，居中守正便有喜庆。

利于渡过大河。

虞翻说：指六三失正，发动后形成坎，卦体为涣。坎象征大河，所以利于涉越大川。涣卦有舟船、木桨之象，因此木道可以通行。

郑玄说：阴阳的道理中，阳称君，阴称臣。现在震卦一阳二阴，臣多于君；四爻属于巽，又不与初爻相应，象征天子减损自己的所有而给予诸侯。君主之道，以增益臣民为德，所以称益。震为雷，巽为风；雷发动、风运行，二者相互成就，就像君主发布教令、臣下奉行，因此利于前往。坎为大河，所以利于涉越大川。

《彖传》说：益，是减少上方而增益下方。

蜀才说：此卦本于否卦。

编者按：乾的上九下降到坤的初六，坤的初六上升到乾的九四，这就是损上益下。

百姓喜悦，没有边际。

虞翻说：上爻来到初位。坤象征无边际，震象征喜悦欢笑；尊贵者谦下于卑贱者，广泛得到百姓，所以百姓喜悦无穷。

从上位下降到下位，其道大为光明。

虞翻说：乾象征大光明，以乾照耀坤，所以道大为光明。另一说认为上爻移到三位，离象征大光明。

利于有所前往，居中守正便有喜庆。

虞翻说：中正指九五，九二与它相应；乾象征喜庆。

利于涉越大川，是因为木道得以通行。

虞翻说：指六三发动形成涣卦。涣有舟船之象，巽木得到水，所以木道得以通行。

增益之道发动而又顺入，日日进展，没有边际。

虞翻说：震的第三爻发动形成离，离象征太阳；巽象征前进，坤象征疆界。太阳与巽都向前运行，所以说日日进展、没有边际。

天施予，地生养，增益没有固定的方所。

虞翻说：乾下降到坤，震象征出生；万物从震方生出，所以说天施予、地生养。阳爻处在坤的初位，象征没有固定方所；日日进展而无边际，所以增益也没有固定方所。

凡是增益之道，都与时势一同运行。

虞翻说：上爻下来增益三爻，四时的象征因此端正。艮象征时，震象征行动，与损卦的解释相同，所以说与时偕行。

《象传》说：风与雷相伴，就是益卦。君子因此见到善行就迁善，有了过失就改正。

虞翻说：君子指乾。上爻移到三位，离象征看见；乾象征善，坤象征过失。坤三前进到乾四，所以见善便迁；乾上爻下降到坤初，改变坤的过失。卦体有复卦之象，复卦表示自我反省，所以有过便改。

初九：适宜进行大规模的农作，最为吉祥，没有过失。

虞翻说：大作指耕种、播种、耒耜和锄具的功用。坤象征使用，乾象征广大，震象征兴作，所以适宜进行大规模农作。卦体为复，初九得正，朋友到来而无咎，所以大吉无咎。震是二月之卦，日中星鸟，表示应当敬授农时，因此用耕种播作为例。

象传说：大吉而无过失，是因为不把繁重劳役强加给下民。

侯果说：大作指耕种栽植。初九处在益卦开端，又居震卦初位；震象征庄稼，也象征大规模兴作。增益之中，没有比农业生产更重要的。因此初九适宜大作。若不使百姓承担过重劳役，不耽误农时，便大吉无咎。

六二：有人赠送价值十朋的龟，占兆不能违背，长久守正吉祥。

虞翻说：上爻从外部前来增益初爻，所以说有人增益。六二得正并与远处相应，利于六三变正，自己便能承接它。坤数为十，损卦兑象征朋；六三变化为离，离象征龟，所以称十朋之龟。坤象征长久，上爻移到三位而得正，所以长久守正吉祥。

王者用它祭享上帝，吉祥。

虞翻说：震称为帝，王指九五；否卦乾象征王。卦体有观卦之象，艮象征宗庙。六三变化会折损坤牛，卦体又有噬嗑进食之象，所以说王者祭享上帝。九五得位，因此吉祥。

干宝说：圣明君王先使百姓生活安定，然后才致力于祭祀神明，所以说王者祭享上帝。益卦属于巽宫，处在震象之中，因此是东方苍精之帝，与始祖同源。

象传说：有人增益，是从外部来到的。

虞翻说：乾上称外，外爻前来增益初爻。

六三：在凶险事务中得到增益，没有过失。

虞翻说：坤象征事务，三爻多凶。上爻来到三位增益它，使它得到正位，所以在凶事中得到增益而无咎。

有诚信而行于中道，告知公侯并使用圭玉。

虞翻说：公指六三伏藏的阳爻。六三发动形成坎，所以有诚信；震象征中行，也象征告知；三爻处在公位，所以称中行。乾象征圭玉，乾的九三之位，因此说告公用圭。圭是桓圭。

《九家易》说：天子用一尺二寸的玄圭祭天，用九寸的圭祭地；上公执九寸桓圭，诸侯执七寸信圭，诸伯执七寸躬圭，诸子执五寸谷璧，诸男执五寸蒲璧。五等诸侯分别执这些礼器朝见天子。

象传说：在凶事中得到增益，这种情况本来就存在。

虞翻说：六三与上九失正，应当发生变化，所以说这种情况本来就存在。

干宝说：所谓本来就存在，是指齐桓公、晋文公一类人物。他们的罪过接近篡夺君位，但功绩确实有利于挽救天下。六三失位，卦体又有奸邪之象；处在震动之中，怀有巽顺权变，所以像矫托君命的士人、争权夺利的臣子，正是桓公、文公一类爻象。因此说在凶事中得到增益。它处在益卦坤体之中，能够保全社稷、爱护人民，所以无咎；随后行于中道，接近仁德，所以有诚信；然后向下会盟诸侯，向上接受赐命，所以告公用圭。

六四：行于中道，告知公侯，公侯便顺从。

虞翻说：中行指震位在中。震象征行动，也象征顺从，所以说中行。公指六三。六三与上九失位；六四有利于六三变正，自己便能得到充实，所以告公而公从。

适宜依靠诸侯迁移国都。

虞翻说：坤象征邦国。迁就是顺从。六三发动，坤顺从，所以适宜依靠诸侯迁邦。

象传说：告知公侯而得到顺从，是因为增益了志向。

虞翻说：坎象征志向。六三上升到上位，有两个坎象，所以说增益志向。

崔觐说：这是增益诸侯勤王的志向。益卦之时，六四处在适当位置，靠近九五；六四又象征上公，承担藩屏王室的职责，成为可以依靠、顺从的诸侯之国。例如周平王东迁时，晋、郑两国随从护卫，就是这种情况。九五是天子，增益其忠诚之志，因此说中行告公从，适宜依靠诸侯迁国。

九五：有诚信而怀有惠民之心，不必询问，大吉。

虞翻说：指六三上升。震象征询问。三、上交换位置后，三、五形成坎，坎象征心，所以说有诚信、惠民之心，不必询问，大吉。象传说不必询问。

有诚信，百姓便感念我的德行。

虞翻说：坤象征我，乾象征德；六三上升后形成坎，坎象征诚信，所以说百姓感念我的德行。象传说大得志。

崔觐说：九五居中尊之位，当位而有应。处在损上益下之时，专以损己为念。虽然对国家有诚信，惠心施及下民，却不宣扬自己的功劳。因此说有诚信而怀惠心，不必询问。这样便能获得大吉，也受到下民信任并感念其德；君主虽不自我宣扬，百姓却感念其德，所以大得志。

上九：没有人来增益它。

虞翻说：莫就是没有。除非上九不再增益初九，否则上九本应有所回应；因为它没有应援，所以说没有人增益它。

有人攻击它。

虞翻说：上九不增益初九，就会反过来剥损乾阳。艮象征手，所以说有人攻击它。

立定的心志不能恒常，凶险。

虞翻说：上卦巽象征进退不定，所以不能恒常。发动后形成坎心，以阴爻乘阳爻，所以立心不能恒常而凶险。

象传说：没有人增益它，是一种遍及全局的说法。

虞翻说：遍是周遍。三爻的卦体刚强而有凶象，因此直到上爻与之相应时才增益它。

有人攻击它，是从外部来到的。

虞翻说：外指上九。上九来到三位，所以说从外部来。

译释说明：古代圭璧、祭祀、诸侯勤王等制度依注家语境转译；“十朋之龟”等象数解释并非现代占验事实。"""),

    Entry("历代注疏", "唐李鼎祚《周易集解》卷九·夬卦集解", "底本：维基文库《周易集解（四库全书本）/卷09》，页面正文行118—223。收录夬卦《序卦》、经文、《彖》《象》、六爻及页面所载诸家注文。数字文本底本，尚未与四库影印本逐字校勘。来源：https://zh.wikisource.org/zh-hans/周易集解_(四库全书本)/卷09", """《序卦》曰：益而不已，必决，故受之以夬。夬者，决也。
韩康伯曰：益而不已则盈，故必决矣。

（乾下兑上）夬：扬于王庭。
虞翻曰：阳决阴息卦也。刚决柔，与剥旁通。乾为阳、为王，剥艮为庭，故扬于王庭矣。
郑玄曰：夬，决也。阳气浸长至于五，五尊位也，而阴先之。是犹圣人积德，说天下以渐消去小人，至于受命为天子，故谓之决。扬，越也。五互体乾，乾为君，又居尊位，王庭之象也。阴爻越其上，小人乘君子，罪恶上闻于圣人之朝，故曰夬扬于王庭也。
孚号有厉。
虞翻曰：阳在二五称孚，孚谓五也。二失位，动体巽，巽为号；离为光。不变则危，故孚号有厉，其危乃光也。
告自邑，不利即戎。
虞翻曰：阳息动复，刚长成夬。震为告，坤为自邑。夬从复升坤，逆在上，民众消灭；二变时离为戎，故不利即戎，所尚乃穷也。
利有攸往。
虞翻曰：阳息阴消，君子道长，故利有攸往。刚长乃终。

《彖》曰：夬，决也，刚决柔也。
虞翻曰：乾决坤也。
健而说，决而和。
虞翻曰：健乾，说兑也。以乾阳获阴之和，故决而和也。
扬于王庭，柔乘五刚也。
王弼曰：刚德浸长，一柔为逆，众所同诛而无忌者也，故可扬于王庭。
孚号有厉，其危乃光也。
荀爽曰：信其号令于下，众阳危去，上六阳乃光明也。
干宝曰：夬九五则飞龙在天之爻也。应天顺民以发号令，故曰孚号。以柔决刚，以臣伐君，君子危之，故曰有厉。德大即心小，功高而意下，故曰其危乃光也。
告自邑，不利即戎，所尚乃穷也。
翟玄曰：坤称邑也。
干宝曰：殷民告周以纣无道，不利即戎，所尚乃穷也。
荀爽曰：不利即尚兵戎而与阳争，必困穷。
利有攸往，刚长乃终也。
虞翻曰：乾体大成，以决小人，终乾之刚，故乃以终也。

《象》曰：泽上于天，夬。
陆绩曰：水气上天，决降成雨，故曰夬。
君子以施禄及下，居德则忌。
虞翻曰：君子谓乾，乾为施禄；下谓剥坤，坤为众臣。以乾应坤，故施禄及下。乾为德，艮为居，故居德则忌。阳极阴生，谓阳忌阴。

初九：壮于前趾，往不胜为咎。
虞翻曰：夬变大壮，大壮震为趾，位在前，故壮于前。刚以应刚，不能克之；往如失位，故往不胜为咎。
《象》曰：不胜而往，咎也。
虞翻曰：往失位，应阳，故咎矣。

九二：惕号，莫夜有戎，勿恤。
虞翻曰：惕，惧也。二失位，故惕；变成巽，故号。剥坤为莫夜。二动成离，离为戎；变而得正，故有戎。四变成坎，坎为忧；坎又得正，故勿恤，谓成既济定也。
《象》曰：有戎勿恤，得中道也。
虞翻曰：动得正，应五，故得中道。

九三：壮于頄，有凶。
翟玄曰：頄，面也。谓上处乾首之前称頄，頄颊间骨。三往壮上，故有凶也。
君子夬夬，独行遇雨。
荀爽曰：九三体乾，乾为君子。三五同功，二爻俱欲决上，故曰君子夬夬也。独行谓一爻独上，与阴相应，为阴所施，故遇雨也。
若濡有愠，无咎。
荀爽曰：虽为阴所濡，能愠不说，得无咎也。
《象》曰：君子夬夬，终无咎也。
王弼曰：頄，面颧也，谓上六矣，最处体上，故曰頄也。剥之六三以应阳为善。夫刚长则君子道兴，阴盛则小人道长。然则处阴长而助阳则善，处刚长而助柔则凶矣，而三独应上，助小人，是以凶也。君子处之，必能弃夫情累，决之不疑，故曰夬夬也。若不与阳为群而独行殊志，应于小人，则受其困焉。遇雨若濡，有愠而终无所咎也。

九四：臀无肤，其行次且。
虞翻曰：二四已变，坎为臀；剥艮为肤，毁灭不见，故臀无肤。大壮震为行，坎为破、为曳，故其行次且也。
牵羊，悔亡；闻言不信。
虞翻曰：兑为羊，二变巽为绳，剥艮手持绳，故牵羊。谓四之正得位，承五，故悔亡。震为言，坎为耳；震坎象不正，故闻言不信也。
《象》曰：其行次且，位不当也；闻言不信，聪不明也。
虞翻曰：坎耳、离目折入于兑，故聪不明矣。案：兑为羊，四五体兑，故也。凡卦初为足，二为腓，三为股，四为臀；当阴柔，今反刚阳，故曰臀无肤。九四震爻，震为足，足既不正，故行次且矣。

九五：苋陆夬夬。
荀爽曰：苋谓五，陆谓三，两爻决上，故曰夬夬也。苋者，叶柔而根坚且赤，以言阴在上六也。陆亦取叶柔根坚也。去阴远，故言陆，言差坚于苋。苋根小，陆根大。五体兑柔居上，苋也；三体乾刚在下，根深，故谓之陆也。
中行无咎。
虞翻曰：苋，说也。苋读夫子莞尔而笑之苋。陆，和睦也。震为笑言，五得正位，兑为说，故苋陆夬夬。大壮震为行，五在上中，动而得正，故中行无咎。旧读言苋陆字之误也，马君、荀氏皆从俗言苋陆，非也。
《象》曰：中行无咎，中未光也。
虞翻曰：在坎阴中，故未光也。
王弼曰：苋，草之柔脆者也。夬之至易，故曰夬夬也。夬之为义，以刚决柔，以君子除小人也，而五处尊位，最比小人，躬自决者也。夫以至尊而敌于至贱，虽其克胜，未足多也。处中而行，足以免咎而已，未为光益也。

上六：无号，终有凶。
虞翻曰：应在于三，三动时体巽，巽为号令。四已变坎之应，历险巽象不见，故无号。位极乘阳，故终有凶矣。
《象》曰：无号之凶，终不可长也。
虞翻曰：阴道消灭，故不可长也。

《序卦》曰：决必有遇，故受之以姤。姤者，遇也。
崔憬曰：君子夬夬独行遇雨，故言决必有遇也。"""),

    Entry("历代注疏", "唐李鼎祚《周易集解》卷九·姤卦集解", "底本：维基文库《周易集解（四库全书本）/卷09》，页面正文行224—311。收录姤卦经文、《彖》《象》、六爻及页面所载诸家注文。数字文本底本，尚未与四库影印本逐字校勘。来源：https://zh.wikisource.org/zh-hans/周易集解_(四库全书本)/卷09", """（巽下乾上）姤：女壮。
虞翻曰：消卦也，与复旁通。巽长女，女壮伤也。阴伤阳，柔消刚，故女壮也。
勿用取女。
虞翻曰：阴息剥阳，以柔变刚，故勿用取女，不可与长也。

《彖》曰：姤，遇也，柔遇刚也。勿用取女。
郑玄曰：姤，遇也。一阴承五阳，一女当五男，苟相遇耳，非礼之正，故谓之姤。女壮如是，壮健以淫，故不可取。妇人以婉娩为其德也。
不可与长也。
王肃曰：女不可取，以其不正，不可与长久也。
天地相遇，品物咸章也。
荀爽曰：谓乾成于巽而舍于离，坤出于离与乾相遇。南方夏位，万物章明也。
九家易曰：谓阳起子，运行至四月，六爻成乾，巽位在巳，故言乾成于巽。既成转舍于离，万物皆盛大。坤从离出，与乾相遇，故言天地遇也。
刚遇中正，天下大行也。
翟玄曰：刚谓九五，遇中处正，教化大行于天下也。
姤之时义大矣哉。
陆绩曰：天地相遇，万物亦然，故其义大也。

《象》曰：天下有风，姤。
翟玄曰：天下有风，风无不周布，故君以施令告化四方之民矣。
后以施命诰四方。
虞翻曰：后，继体之君。姤阴在下，故称后，与泰称后同义也。乾为施，巽为命、为诰。复震二月东方，姤五月南方，巽八月西方，复十一月北方，皆总在初，故以诰四方也。孔子行夏之时，经用周家之月；夫子传彖象以下，皆用夏家月，是故复为十一月，姤为五月矣。

初九：系于金柅，贞吉。
虞翻曰：柅谓二也。巽为绳，故系柅。乾为金，巽木入金，柅之象也。初四失正，易位乃吉，故贞吉矣。
有攸往，见凶。
九家易曰：丝系于柅，犹女系于男，故以喻初宜系二也。若能专心顺二则吉，故曰贞吉。今既为二所据，不可往应四，往则有凶，故曰有攸往见凶也。
羸豕孚蹢躅。
虞翻曰：以阴消阳，往谓成坤；遁，子弑父；否，臣弑君。夬时三动，离为见，故有攸往见凶矣。三夬之四，在夬动而体坎，坎为豕、为孚，巽绳操之，故称羸也。巽为舞、为进退，操而舞，故羸豕孚蹢躅，以喻姤女望于五阳如豕蹢躅也。
宋衷曰：羸，大索所以系豕者也。巽为股，又为进退，股而进退则蹢躅也。初应于四，为二所据，不得从应，故不安矣。体巽为风，动摇之貌也。
《象》曰：系于金柅，柔道牵也。
虞翻曰：阴道柔，巽为绳，牵于二也。

九二：包有鱼，无咎，不利宾。
虞翻曰：巽为白茅，在中称包。《诗》云白茅包之。鱼谓初阴，巽为鱼。二虽失位，阴阳相承，故包有鱼无咎。宾谓四，乾尊称宾，二据四应，故不利宾。或以包为庖厨也。
《象》曰：包有鱼，义不及宾也。
王弼曰：初阴而穷下，故称鱼也。不正之阴处遇之始，不能逆近者也。初自乐来应己之厨，非为犯应，故无咎也。擅人之物以为己惠，义所不为，故不及宾。

九三：臀无肤，其行次且，厉，无大咎。
虞翻曰：夬时动之坎为臀，艮为肤；二折艮体，故臀无肤。复震为行，其象不正，故其行次且。三得正位，虽则危厉，故无大咎矣。案：巽为股，三居上臀也；爻非柔无肤，行次且也。
《象》曰：其行次且，行未牵也。
虞翻曰：在夬失位，故牵羊；在姤得正，故未牵也。

九四：包无鱼，起凶。
王弼曰：二有其鱼，四故失之也。无民而动，失应而作，是以凶矣。
《象》曰：无鱼之凶，远民也。
崔憬曰：虽与初应而失其位，二有其鱼而宾不及。若起于竞涉远，必难终不遂心，故曰无鱼之凶远民也，谓初六矣。

九五：以杞包瓜，含章。
虞翻曰：杞，杞柳木名也。巽为杞、为包，乾圆称瓜，故以杞包瓜矣。含章谓五也。五欲使初四易位，以阴含阳，已得乘之，故曰含章。初之四体兑口，故称含也。
干宝曰：初二体巽为草木，二又为田，田中之果，柔而蔓者，瓜之象也。
有陨自天。
虞翻曰：陨，落也。乾为天，谓四陨之初，初上承五，故有陨自天矣。
《象》曰：九五含章中正也，有陨自天，志不舍命也。
虞翻曰：巽为命也，欲初之四承己，故不舍命矣。

上九：姤其角，吝，无咎。
虞翻曰：乾为首，位在首上，故称角；动而得正，故无咎。
《象》曰：姤其角，上穷吝也。
王弼曰：进之于极，无所复遇，遇角而已，故曰姤其角也。进而无遇，独恨而已，不与物牵，故曰上穷吝也。

《序卦》曰：物相遇而后聚，故受之以萃。萃者，聚也。
崔憬曰：天地相遇，品物咸章，故言物相遇而后聚。""", chatGptTranslation = """**ChatGPT 翻译（逐段工作译文）**

姤卦下巽上乾。姤：女子强盛。虞翻说：这是阴气渐长、阳气消退之卦，与复卦旁通。巽为长女；阴伤阳、柔消刚，所以说女子强盛。

不要娶这样的女子。虞翻说：阴气增长而剥损阳气，以柔变刚，因此不宜娶她，不能与之长久相处。

《彖传》说：姤就是相遇，是柔爻遇到刚爻；不要娶这样的女子。郑玄说：一阴承接五阳，如一女面对五男，只是偶然相遇，并非合礼的正当关系。女子强盛而放纵，因此不可娶；妇人之德应当温婉。不可与之长久。王肃说：她不正，不能长久相处。

天地相遇，万物都显明。荀爽说：乾在巽中形成又寄居离，坤从离中出来与乾相遇；南方属夏，万物因此显明。《九家易》说：阳气从子位运行，到四月六爻成乾；乾成于巽位后转居离，万物盛大，坤从离出而与乾遇。

刚爻遇到中正之位，天下教化便广泛施行。翟玄说：刚指九五，居中得正，教化大行。姤卦的时义广大。陆绩说：天地相遇，万物也相遇，所以意义广大。

《象传》说：天下有风，就是姤卦。翟玄说：风遍布天下，君主应当施行政令、告示四方百姓。君王因此发布命令，告知四方。虞翻说：后是继位的君主；乾为施行，巽为命令、告示。复、姤、巽分别对应四方和月份，故以命令告四方。孔子传《彖》《象》时用夏历，所以复为十一月、姤为五月。

初九：系在金属止车木上，守正吉祥。虞翻说：柅指九二；巽为绳，乾为金，巽木入金，形成止车木之象。初、四失正，交换位置后才吉。有所前往，会看见凶险。《九家易》说：丝系于柅，如女子系属于男子；初九宜专心顺从九二，不应前往应四，否则凶。

瘦弱的猪虽被系住，仍然躁动。虞翻说：阴消阳，发展下去会成坤；遁有子弑父之象，否有臣弑君之象。坎为猪、为诚信，巽为绳，牵住而仍舞动，故以躁动的瘦猪比喻姤卦女子面对五阳。宋衷说：羸是系猪的大索；巽为股、为进退，进退不安。初应四却被二占据，不能从应，所以不安。象传说柔道被牵制，虞翻说阴柔而巽为绳，故被系于二。

九二：厨房中包有鱼，无咎；不利于招待宾客。虞翻说：巽为白茅、为鱼，初阴为鱼；二虽失位，但阴阳相承，所以无咎。宾指九四，乾尊称宾，二据四应，所以不利宾。王弼说：初阴处最下，故称鱼；初自来应二，并非侵犯应爻，所以无咎。擅自把他人之物当作自己的恩惠，义理上不允许，因此不及宾。

九三：臀部没有皮肉，行走迟疑；有危险，但无大咎。虞翻说：坎为臀，艮为肤；二爻折损艮体，所以无肤。震为行，象不正故行迟疑；三得正，虽危厉而无大咎。象传说行动尚未受到牵制；虞翻说夬卦失位故有牵羊之象，姤卦得正所以未牵。

九四：包中没有鱼，行动便凶。王弼说：九二已经得到鱼，九四因此失去它；没有百姓却贸然行动，又失去应援，所以凶。象传说远离百姓而凶。崔憬说：四虽与初应却失位，二已得鱼；若争夺而远行，必不能遂愿。

九五：用杞柳包裹瓜果，内含文采。虞翻说：杞是杞柳，巽为杞、为包，乾圆象瓜；五欲使初、四易位，以阴含阳，所以称含章。干宝说：初二属巽为草木，二又为田，田中柔蔓之果像瓜。有东西从天而降。虞翻说：陨是坠落，乾为天；四爻降到初位，初承五，所以有陨自天之象。象传说五含章是居中守正；有陨自天，是志不舍弃天命。虞翻说巽为命，初承五，所以不舍命。

上九：相遇到角上，有羞吝，但无咎。虞翻说乾为首，角在首上；发动得正，所以无咎。象传说上穷而吝。王弼说：前进到极点，再无可遇，只遇到角；没有遇合而独自遗憾，但不被外物牵引。

《序卦》说：事物相遇之后便会聚集，所以接着是萃卦；萃就是聚集。崔憬说：天地相遇、万物显明，因此事物相遇之后便聚集。

译释说明：本条对“女壮”“羸豕”等古代象喻按注家原意转述；相关性别伦理判断属于古代经注语境，不作为现代婚姻规范。"""),

    Entry("历代注疏", "唐李鼎祚《周易集解》卷九·萃卦集解", "底本：维基文库《周易集解（四库全书本）/卷09》，页面正文行312—404。收录萃卦《序卦》、经文、《彖》《象》、六爻及页面所载注文、案语。数字文本底本，尚未与四库影印本逐字校勘。来源：https://zh.wikisource.org/zh-hans/周易集解_(四库全书本)/卷09", """（坤下兑上）萃：亨。王假有庙。
虞翻曰：观上之四也。观乾为王，假，至也。艮为庙。体观享祀，上之四，故假有庙，致孝享矣。
利见大人，亨，利贞。
虞翻曰：大人谓五。三、四失位，利之正，变成离，离为见，故利见大人，亨利贞，聚以正也。
用大牲吉，利有攸往。
虞翻曰：坤为牛，故曰大牲。四之三，折坤得正，故用大牲吉；三往之四，故利有攸往，顺天命也。
郑玄曰：萃，聚也。坤为顺，兑为悦，臣下以顺道承事其君，说德居上待之，上下相应，有事而和通，故曰萃亨也。假，至也。互有艮巽，巽为木，艮为阙，木在阙上，宫室之象也。四本震爻，震为长子；五本坎爻，坎为隐伏，居尊而隐伏，鬼神之象。长子入阙升堂，祭祖祢之礼也，故曰王假有庙。二本离爻也，离为目，居正应五，故利见大人矣。大牲，牛也。言大人有嘉会时可干事，必杀牛而盟，既盟则可以往，故曰利往。案：坤为牛，巽木下克坤土，杀牛之象也。

《彖》曰：萃，聚也。顺以说，刚中而应，故聚也。
荀爽曰：谓五以刚居中，群阴顺说而从之，故能聚众也。
王假有庙，致孝享也。
虞翻曰：享，享祀也。五至初有观象，谓享坤牛，故致孝享矣。
利见大人，亨，聚以正也。
虞翻曰：坤为聚，坤三之四，故聚以正也。
利贞。
九家易曰：五以正聚阳，故曰利贞。
用大牲吉，利有攸往，顺天命也。
虞翻曰：坤为顺，巽为命，三往之四，故顺天命也。
观其所聚，而天地万物之情可见矣。
虞翻曰：三四易位成离坎，坎月、离日，日以见天，月以见地，故天地之情可见也，与大壮、咸、恒同义也。

《象》曰：泽上于地，萃。
荀爽曰：泽者，卑下流潦归之，万物生焉，故谓之萃也。
君子以除戎器，戒不虞。
虞翻曰：君子谓五，除，修也。戎兵也。《诗》曰“修尔车马弓矢戎兵”。阳在三四为修，坤为器，三四之正，离为戎兵、甲胄、飞矢，坎为弓弧，巽为绳，艮为石，谓甲胄锻厉、矛矢，故除戎器也。坎为寇，坤为乱，故戒不虞也。原页面此处另有图像字形，当前无法仅凭文本确定，标记待核。

初六：有孚不终，乃乱乃萃。
虞翻曰：孚谓五也。初四易位，五坎中，故有孚；失正当变，坤为终，故不终。萃，聚也。坤为乱、为聚，故乃乱乃萃。失位不变，则相聚为乱，故象曰其志乱也。
若号，一握为笑，勿恤，往无咎。
虞翻曰：巽为号，艮为手，初称一，故一握。初动成震，震为笑；四动成坎，坎为恤，故若号一握为笑勿恤。初之四得正，故往无咎矣。
《象》曰：乃乱乃萃，其志乱也。
虞翻曰：坎为志，初不之四，其志乱也。

六二：引吉，无咎。
虞翻曰：应巽为绳，艮为手，故引吉。得正应五，故无咎。利引四之初，使避己，己得之五也。
孚乃利用禴。
虞翻曰：孚谓五。禴，夏祭也。体观象，故利用禴。四之三，故用大牲；离为夏，故禴祭。《诗》曰“禴祠烝尝”，是其义。
《象》曰：引吉无咎，中未变也。
虞翻曰：二得正，故不变也。
王弼曰：居萃之时，体柔当位，处坤之中，己独履正，与众相殊异操，而聚民之多僻。独正者危，未能变体以远于害，故必待五引，然后乃吉而无咎。禴，殷春祭名，四时之祭省者也。居聚之时，处于中正而行以忠信，可以省薄于鬼神矣。

六三：萃如，嗟如，无攸利。往无咎，小吝。
虞翻曰：坤为萃，故萃如；巽为号，故嗟如。失正，故无攸利；动得位，故往无咎。小吝谓往之四。
《象》曰：往无咎，上巽也。
虞翻曰：动之四，故上巽。

九四：大吉，无咎。
《象》曰：大吉无咎，位不当也。
虞翻曰：以阳居阴，故位不当。动而得正，承五应初，故大吉而无咎矣。

九五：萃有位，无咎。匪孚，元永贞，悔亡。
虞翻曰：得位居中，故有位无咎。匪孚谓四也。四变之正，则五体皆正，故元永贞，与比彖同义。四动之初，故悔亡。
《象》曰：萃有位，志未光也。
虞翻曰：阳在坎中，故志未光，与屯五同义。

上六：赍咨涕洟，无咎。
虞翻曰：赍，持资赙也。货财丧称赙，自目曰涕，自鼻称洟。坤为财，巽为进，故赍资也。三之四，体离坎艮为鼻，涕泪流鼻目，故涕洟。得位应三，故无咎。上体大过死象，故有赍资涕洟之哀。
《象》曰：赍𪡌涕洟，未安上也。（页面原字形如此，待核）
虞翻曰：乘刚远应，故未安上也。
荀爽曰：此本否卦。上九阳爻见灭迁移，以喻夏桀、殷纣；以上六阴爻代之。若夏之后封东娄公于杞，殷之后封微子于宋，去其骨肉，臣服异姓，受人封土，未安居位，故曰赍𪡌涕洟未安上也。

《序卦》曰：聚而上者谓之升，故受之以升也。
崔憬曰：用大牲而致孝享，故顺天子而升为王矣，故言聚而上者谓之升也。"""),

    Entry("历代注疏", "唐李鼎祚《周易集解》卷九·升卦集解", "底本：维基文库《周易集解（四库全书本）/卷09》，页面正文行405—480。收录升卦《序卦》、经文、《彖》《象》、六爻及页面所载注文。数字文本底本，尚未与四库影印本逐字校勘。来源：https://zh.wikisource.org/zh-hans/周易集解_(四库全书本)/卷09", """（巽下坤上）升。
郑玄曰：升，上也。坤地、巽木，木生地中，日长而上，犹圣人在诸侯之中，明德日益高大也，故谓之升。升，进益之象矣。
元亨。
虞翻曰：临初之三，又有巽象，刚中而应，故元亨也。
用见大人，勿恤。
虞翻曰：谓二当之五为大人，离为见，坎为恤。二之五得正，故用见大人勿恤，有庆也。
南征吉。
虞翻曰：离南方卦，二之五成离，故南征吉，志行也。

《彖》曰：柔以时升。
虞翻曰：柔谓五坤也。升谓二。坤邑无君，二当升五虚。震兑为春秋，二升坎离为冬夏，四时象正，故柔以时升也。
巽而顺，刚中而应，是以大亨。
荀爽曰：谓二以刚居中而来应五，故能大亨，上居尊位也。
用见大人，勿恤，有庆也。
荀爽曰：大人，天子，谓升居五，见为大人。群阴有主，无所复忧而有庆也。
南征吉，志行也。
虞翻曰：二之五，坎为志，震为行。

《象》曰：地中生木，升。
荀爽曰：地谓坤，木谓巽。地中生木，以微至著，升之象也。
君子以顺德，积小以高大。
虞翻曰：君子谓三。小谓阳息复时，复小为德之本，至二成临，临者大也。临初之三，巽为高；二之五，艮为顺；坤为积，故顺德积小成高大。

初六：允升，大吉。
荀爽曰：谓一体相随，允然俱升。初欲与巽一体升居坤上，位尊得正，故大吉也。
《象》曰：允升大吉，上合志也。
九家易曰：谓初失正，乃与二阳允然合志俱升五位，上合志也。

九二：孚乃利用禴，无咎。
虞翻曰：禴，夏祭也。孚谓二之五，成坎为孚，离为夏，故乃利用禴无咎矣。
《象》曰：九二之孚，有喜也。
虞翻曰：升五得位，故有喜。
干宝曰：刚中而应，故孚也。又言乃利用禴于春时也，非时而祭曰禴。然则文王俭以恤民，四时之祭皆以禴礼神享德，与信不求备也。故既济九五曰“东邻杀牛，不如西邻之禴祭，实受其福”。九五坎，坎为豕，然则禴祭以豕而已，不奢盈于礼，故曰有喜矣。

九三：升虚邑。
荀爽曰：坤称邑也。五虚无君，利二上居之，故曰升虚邑，无所疑也。
《象》曰：升虚邑，无所疑也。
虞翻曰：坎为疑，上得中，故无所疑也。

六四：王用享于岐山，吉，无咎。
荀爽曰：此本升卦也。巽升坤上，据三成艮；巽为岐，艮为山。王谓五也。通有两体，位正众服，故吉也。四能与众阴退避当升者，故无咎也。
《象》曰：王用享于岐山，顺事也。
崔憬曰：为顺之初，在升当位，近比于五，乘刚于三，宜以进德可修守此象。太王为狄所逼，徙居岐山之下，一年成邑，二年成都，三年五倍其初，通而王矣。故曰王用享于岐山，以其用通避于狄难，顺于时事，故吉无咎。

六五：贞吉，升阶。
虞翻曰：二之五，故贞吉。巽为高，坤为土，震升高，故升阶也。
《象》曰：贞吉升阶，大得志也。
荀爽曰：阴正居中，为阳作阶，使升居五，已下降二，与阳相应，故吉而得志。

上六：冥升，利于不息之贞。
荀爽曰：坤性暗昧，今升在上，故曰冥升也。阴用事为消，阳用事为息；阴正在上，阳道不息，阴之所利，故曰利于不息之贞。
《象》曰：冥升在上，消不富也。
荀爽曰：阴升失实，故消不富也。

《序卦》曰：升而不已必困，故受之以困。
崔憬曰：冥升在上，以消不富则穷，故言升而不已必困也。""", chatGptTranslation = """【ChatGPT译文】
《序卦》说：上升而不停息，必然陷入困境，所以接着是《困》卦。

崔憬说：昏暗地一味上升，最终会消耗而不能富足，因而走向困穷，所以说上升不止必然困顿。

（巽下坤上）升卦。

郑玄说：升就是上升。坤象征地，巽象征木，树木从地中生长，逐渐长高向上，就像圣人在诸侯之中，德行与声望日益增长，所以称为升。这一卦象征进步和增益。

元亨。

虞翻说：这是临卦初爻移到三爻，又形成巽象；刚爻居中而有应，所以元亨。

用见大人，勿恤。

虞翻说：这里指九二应当上升到九五，九五为大人；离象征看见，坎象征忧虑。九二升到九五便得到正位，所以说见大人而不必忧虑，并会有喜庆。

南征吉。

虞翻说：离是南方之卦，九二升到九五形成离卦，所以向南行进吉祥，志向能够实现。

《彖传》说：柔顺者顺应时机而上升。

虞翻说：柔爻指坤卦九五所处的阴位；上升指九二。坤象征城邑而没有君主，九二应当升到九五的空位。震、兑象征春秋，九二升到五位形成坎、离，象征冬夏，四时的象征因此得以配合，所以说柔顺者依时上升。

柔顺而能谦逊，刚健居中而有所应，因此大为亨通。

荀爽说：指九二以刚爻居中并与九五相应，因此能够大为亨通，九五也因此居于尊位。

见到大人而不必忧虑，会有喜庆。

荀爽说：大人指天子，这里指上升而居五位，成为大人。众阴有了君主，便不再有所忧虑，因此有喜庆。

向南征行吉祥，志向得以施展。

虞翻说：九二升到九五；坎象征心志，震象征行动。

《象传》说：地中生长树木，这就是升卦。

荀爽说：地指坤，木指巽。树木从地中生长，由微小逐渐变得高大，这就是上升的象征。

君子因此顺着德行积累，从微小处开始，逐渐达到高大。

虞翻说：君子指九三。小指阳气在复卦时刚刚增长；复卦虽小，却是德行的根本，发展到九二便成为临卦，临有广大之义。临卦初爻移到三爻，巽象征高；九二升到九五，艮象征顺；坤象征积累，所以顺着德行积累，由小而成高大。

初六：诚信而上升，大为吉祥。

荀爽说：指同一卦体中的爻相互跟随，诚然一同上升。初六想与巽卦一体上升到坤上，位置尊贵而得正，所以大吉。

《象传》说：诚信上升而大吉，是因为向上与他者志向相合。

《九家易》说：初六原本失正，后来与两个阳爻诚信相合，一同上升到五位，所以说向上与志向相合。

九二：心怀诚信，适宜举行简约的夏祭，没有灾咎。

虞翻说：禴是夏祭。诚信指九二升到九五，形成坎象而为诚信；离象征夏季，所以说适宜举行禴祭而无咎。

《象传》说：九二的诚信带来喜庆。

虞翻说：升到五位并得到正位，所以有喜庆。

干宝说：刚健居中而有所应，所以称为诚信。这里又说在春季举行禴祭；不合时令而举行祭祀也称禴。不过，文王节俭并体恤百姓，四时祭祀都采用简约的禴礼，以诚信敬神，不追求繁备。因此既济九五说：“东邻杀牛，不如西邻举行禴祭，实实在在地接受福祉。”九五属坎，坎象征猪，因此禴祭只用猪，不以奢侈充满礼仪，所以说有喜庆。

九三：上升到空虚的城邑。

荀爽说：坤象征城邑。九五空虚而没有君主，九二适宜上升占据它，所以说上升到空城，没有什么可疑虑的。

《象传》说：上升到空城，没有什么可疑虑的。

虞翻说：坎象征疑虑；上升后得到中位，所以没有疑虑。

六四：君王在岐山举行祭祀，吉祥，没有灾咎。

荀爽说：此卦本来是升卦。巽上升到坤上，依据九三形成艮；巽象征岐，艮象征山。君王指九五。上下贯通而有两个卦体，位置端正、众人归服，所以吉祥。六四能够与众阴一起退让，使应当上升者得以上升，因此没有灾咎。

《象传》说：君王在岐山举行祭祀，是顺应时势行事。

崔憬说：六四处于顺从的开始，在升卦中位置得当，接近九五而承乘九三，适宜进德修业并守住这一象。古公亶父受到狄人逼迫，迁居岐山之下，一年形成城邑，二年建成城都，三年人口与规模增长数倍，最终通达而成为王业的开端。因此说君王在岐山举行祭祀；这是因避开狄人之难而顺应时势，所以吉祥无咎。

六五：守正则吉，逐级登上台阶。

虞翻说：九二升到九五，所以守正吉祥。巽象征高，坤象征土，震象征上升，因此说登上台阶。

《象传》说：守正吉祥并登上台阶，是因为志向得到充分实现。

荀爽说：阴爻端正地居于中位，为阳爻铺成台阶，使阳爻上升而居五位；自己下降到二位，与阳爻相应，所以吉祥而得志。

上六：昏暗地上升，适宜坚持不懈地守正。

荀爽说：坤的性质昏暗，如今上升而居于顶端，所以称为昏暗地上升。阴主消退，阳主增长；阴爻虽居上位，阳道却不应停止增长，所以说适宜坚持不息的正道。

《象传》说：昏暗地上升而居于顶端，消退而不能富足。

荀爽说：阴爻上升而失去实质，所以消退而不能富足。"""),

    Entry("历代注疏", "唐李鼎祚《周易集解》卷九·困卦集解", "底本：维基文库《周易集解（四库全书本）/卷09》，页面正文行481—586。收录困卦经文、《彖》《象》、六爻及页面所载注文、案语。数字文本底本，尚未与四库影印本逐字校勘。来源：https://zh.wikisource.org/zh-hans/周易集解_(四库全书本)/卷09", """（坎下兑上）困：亨。
郑玄曰：坎为月，互体离，离为日，兑为暗昧，日所入也。今上掩日月之明，犹君子处乱代为小人所不容，故谓之困也。君子虽困，居险能悦，是以通而无咎也。
虞翻曰：否二之上，乾坤交，故通也。
贞，大人吉，无咎。
虞翻曰：贞大人吉谓五也。在困无应，宜静则无咎，故贞大人吉无咎。
有言不信。
虞翻曰：震为言，折入兑，故有言不信，尚口乃穷。

《彖》曰：困，刚揜也。
荀爽曰：谓二、五为阴所揜也。
险以说。
荀爽曰：此本否卦，阳降为险，阴升为悦也。
困而不失其所亨，其唯君子乎。
荀爽曰：谓二虽揜阴陷险，犹不失中与正，阴合故通也。喻君子虽陷险中，不失中和之行也。
贞大人吉，以刚中也。
荀爽曰：谓五虽揜于阴，近无所据，远无所应，体刚得中正，居五位则吉无咎也。
有言不信，尚口乃穷也。
虞翻曰：兑为口，上变口灭，故尚口乃穷。
荀爽曰：阴从二升上，六成兑为有言；失中为不信，动而乘阳，故曰尚口乃穷也。

《象》曰：泽无水，困。
王弼曰：泽无水，则水在泽下也，水在泽下，困之象也。处困而屈其志者小人也，君子固穷，道可忘乎？
君子以致命遂志。
虞翻曰：君子谓三伏阳也。否坤为致，巽为命，坎为志，三入阴中，故致命遂志也。

初六：臀困于株木。
九家易曰：臀谓四，株木三也。三体为木，泽中无水，兑金伤木，故枯为株也。初者四应，欲进之四，四困于三，故曰臀困于株木。
干宝曰：兑为孔穴，坎为隐伏，隐伏在下而漏孔穴，臀之象也。
入于幽谷，三岁不觌。
九家易曰：幽谷二也。此本否卦，谓阳来入坎，与初同体，故曰入幽谷。三者阳数，谓阳陷险中，为阴所揜，终不得见，故曰三岁不觌也。
《象》曰：入于幽谷，幽不明也。
荀爽曰：为阴所揜，故不明。

九二：困于酒食，朱绂方来。
案：二本阴位，中馈之职。坎为酒食，上为宗庙。今二阴升上，则酒食入庙，故困于酒食也。上九降二，故朱绂方来。朱绂，宗庙之服；乾为大赤，朱绂之象也。
利用亨祀，征凶，无咎。
荀爽曰：二升在庙，五亲奉之，故利用享祀。阴动而上，失中乘阳；阳下而陷，为阴所掩，故曰征凶。阳降来二，虽位不正，得中有实；阴虽去中，上得居正而皆免咎，故曰无咎也。
《象》曰：困于酒食，中有庆也。
翟玄曰：阳从上来居中得位，富有二阴，故中有庆也。

六三：困于石，据于蒺藜。
虞翻曰：二变正时，三在艮山下，故困于石。蒺藜，木名；坎为蒺藜，二变艮手据坎，故据蒺藜者也。
入于其宫，不见其妻，凶。
虞翻曰：巽为入，二动艮为宫，兑为妻，谓上无应也。三在阴下，离象毁坏，隐在坤中，死期将至，故不见其妻凶也。
《象》曰：据于蒺藜，乘刚也。
案：三居坎上，坎为丛棘而木多心，蒺藜之象。
入于其宫不见其妻，不祥也。
九家易曰：此本否卦，二四同功为艮，艮为门阙、宫之象也。六三居困而位不正，上困于民，内无仁恩，亲戚叛逆，诛将加身，入宫无妻，非常之困，故曰不祥也。

九四：来徐徐，困于金车，吝，有终。
虞翻曰：来欲之初，徐徐，舒迟也。见险故来徐徐。否乾为金，坤为车；之应历险，故困于金车。易位得正，故吝有终。
《象》曰：来徐徐，志在下也。
王弼曰：下谓初。
虽不当位，有与也。
崔憬曰：位虽不当，故吝也；有与于援，故有终也。

九五：劓刖，困于赤绂。
虞翻曰：割鼻曰劓，断足曰刖。四动时，震为足，艮为鼻，离为兵，兑为刑，故劓刖也。赤绂谓二；否乾为朱，故赤；坤为绂。二未变，应五，故困于赤绂也。
乃徐有说。
虞翻曰：兑为说，坤为徐，二动应己，故乃徐有说也。
利用祭祀。
崔憬曰：劓刖，刑之小者也。于困之时，不崇柔德，以刚遇刚，虽行其小刑而失其大柄，故言劓刖也。赤绂，天子祭服之饰，所以称困者，被夺其政，唯得祭祀，若《春秋传》曰“政由宁氏，祭则寡人”，故曰困于赤绂。居中以直，在困思通，初虽暂穷，终则必喜，故曰乃徐有说。所以险而能说、穷而能通者，在困于赤绂乎？故曰利用祭祀也。案：五应在二，二互体离，离为文明，赤绂之象也。
《象》曰：劓刖，志未得也。
陆绩曰：无据无应，故志未得也。二言朱绂，此言赤绂；二言享祀，此言祭祀，传互言耳，无他义也。谓二困五、三困四、五初困上，斯乃迭困之义也。
乃徐有说，以中直也。
崔憬曰：以其居中当位，故有悦。
利用祭祀，受福也。
荀爽曰：谓五爻合同，据国当位而主祭祀，故受福也。

上六：困于葛藟，于臲卼。
虞翻曰：巽为草莽，称葛藟谓三也；兑为刑人，故困于葛藟、于臲卼也。
（原页面此处疑有缺字）曰动悔有悔，征吉。
虞翻曰：乘阳故动悔，变而失正故有悔；三已变正，已得应之，故征吉也。
《象》曰：困于葛藟，未当也。
虞翻曰：谓三未变当位应上故也。
动悔有悔，吉行也。
虞翻曰：行谓三变乃得当位之应，故吉行者也。""", chatGptTranslation = """【ChatGPT译文】
（坎下兑上）困卦：亨通。

郑玄说：坎象征月亮，互体有离，离象征太阳；兑象征昏暗，是太阳落下的地方。如今上方遮蔽了日月的光明，就像君子处在乱世而不被小人容纳，所以称为困。君子虽然困顿，身处险境却仍能保持和悦，因此能够通达而没有灾咎。

虞翻说：这是否卦九二上升到上位，乾坤交合，所以通达。

守正，大人吉祥，没有灾咎。

虞翻说：守正而吉祥的大人指九五。处于困境时没有应援，适宜安静守正，因此没有灾咎。

有所言说却不被信任。

虞翻说：震象征言语，震象被折损而进入兑，所以虽有言说却不被相信；只崇尚口舌终究会陷入困穷。

《彖传》说：困，是刚爻受到遮蔽。

荀爽说：指九二、九五被阴爻遮蔽。

身处险境而仍能和悦。

荀爽说：此卦本来是否卦，阳下降而形成险，阴上升而形成和悦。

处于困境却不失去自身所能通达的境地，大概只有君子能够做到吧。

荀爽说：九二虽然被阴爻遮蔽、陷入险境，仍不失中正；与阴爻相合，所以能够通达。这是比喻君子即使陷入险境，也不失中和的行为准则。

守正而大人吉祥，是因为刚健而居中。

荀爽说：九五虽然被阴爻遮蔽，近处没有依靠，远处也没有应援，但它刚健而得中正，居于五位便吉祥无咎。

有所言说却不被信任，只崇尚口舌便会陷入穷困。

虞翻说：兑象征口；上爻变化后口象消失，所以只崇尚口舌便会困穷。

荀爽说：阴爻从二位上升到上位，六爻形成兑卦而有所言说；失去中位便是不可信，发动而乘于阳爻之上，所以说只崇尚口舌便会困穷。

《象传》说：泽中没有水，这就是困卦。

王弼说：泽中没有水，是因为水已经处在泽的下方。水在泽下，就是困的象征。处在困境中便屈服志向的是小人；君子固守穷困，难道会忘记自己的道吗？

君子因此献出生命以实现志向。

虞翻说：君子指九三所伏藏的阳爻。否卦的坤象征致，巽象征命，坎象征志；九三进入阴爻之中，所以说献出生命而实现志向。

初六：臀部困在枯树桩上。

《九家易》说：臀指九四，树桩指九三。九三的卦体属木，泽中没有水，兑金克伤木，所以木枯而成为树桩。初六与九四相应，想前往九四，却被九三所困，因此说臀部困在树桩上。

干宝说：兑象征孔穴，坎象征隐伏；隐伏在下而孔穴漏水，象征臀部。

进入幽深的山谷，三年不能见到人。

《九家易》说：幽谷指九二。此卦本为否卦，阳爻来到坎中，与初六同体，所以说进入幽谷。“三”是阳数，指阳陷入险境而被阴爻遮蔽，始终不能显现，所以说三年不见。

《象传》说：进入幽谷，是因为幽暗而不明。

荀爽说：因为被阴爻遮蔽，所以不明。

九二：困于酒食，朱色蔽膝服饰即将到来。

按语说：九二原本处在阴位，象征主持饮食供奉的职位。坎象征酒食，上爻象征宗庙。如今九二阴爻上升到上位，酒食进入宗庙，所以说困于酒食。上九下降到九二，所以朱色蔽膝服饰即将到来。朱绂是宗庙祭服，乾象征大红色，是朱绂的象征。

适宜举行祭祀；出行则凶，没有灾咎。

荀爽说：九二上升进入宗庙，九五亲自奉祀，所以适宜举行祭祀。阴爻发动而上升，失去中位并乘于阳爻之上；阳爻下降而陷入其中，被阴爻遮蔽，所以说出行凶。阳爻下降来到九二，虽然位置不正，却得到中位而有实；阴爻虽然离开中位，上升后得到正位，因此双方都免于灾咎，所以说无咎。

《象传》说：困于酒食，是因为中位之中有喜庆。

翟玄说：阳爻从上位下来居中并得到位置，拥有两个阴爻，所以中间有喜庆。

六三：困在石头旁，依靠在蒺藜之上。

虞翻说：九二变化得正时，六三处于艮山之下，所以说困于石。蒺藜是一种植物；坎象征蒺藜，九二变化为艮，艮象征手，手按在坎上，所以说依靠蒺藜。

进入自己的宫室，却看不见妻子，凶。

虞翻说：巽象征进入，九二发动后艮象征宫室，兑象征妻子；这里指上爻没有应援。六三处在阴爻之下，离象被毁坏，隐伏在坤中，死亡之期将近，所以看不见妻子而凶。

《象传》说：依靠蒺藜，是因为乘在刚爻之上。

按语说：六三处于坎卦上方，坎象征丛生的荆棘，树木多刺，因而有蒺藜之象。

进入宫室却看不见妻子，是不祥之兆。

《九家易》说：此卦本为否卦，九二、九四同功而形成艮，艮象征门阙和宫室。六三处于困境且位置不正，对外困于百姓，内无仁爱恩惠，亲族叛离，刑罚将至；进入宫室却没有妻子，是非常困顿的情形，所以说不祥。

九四：缓缓而来，困在金属车上，虽有困难，终究会有结果。

虞翻说：九四想来到初六处，徐徐就是舒缓迟慢。因为看见险阻，所以缓缓而来。否卦的乾象征金，坤象征车；前往应位必须经过险阻，所以困在金车上。交换爻位而得到正位，因此虽有困难却终有结果。

《象传》说：缓缓而来，是因为志向在下方。

王弼说：下方指初六。

虽然位置不恰当，却有同伴相助。

崔憬说：位置虽然不恰当，所以有困难；但有人援助，所以最终会有结果。

九五：受到割鼻、断足之刑，困在红色蔽膝服饰之中。

虞翻说：割鼻称为劓，断足称为刖。九四发动时，震象征足，艮象征鼻，离象征兵器，兑象征刑罚，所以有劓、刖之象。赤绂指九二；否卦的乾象征朱红，坤象征蔽膝服饰。九二尚未变化，与九五相应，所以说困于赤绂。

于是渐渐出现喜悦。

虞翻说：兑象征喜悦，坤象征缓慢；九二发动而与自己相应，所以渐渐有喜悦。

适宜举行祭祀。

崔憬说：劓、刖是较轻的刑罚。困顿之时，不崇尚柔德而以刚遇刚，虽然施行了小刑，却失去了更大的权柄，所以说劓、刖。赤绂是天子祭服上的装饰；之所以说困，是因为政权被夺，只能保留祭祀之权，就像《春秋传》所说“政事由宁氏掌管，祭祀则由寡人主持”。所以说困于赤绂。九五居中而正，在困境中思求通达，起初虽暂时穷困，最终必然喜悦，所以说渐渐出现喜悦。之所以能处险而喜悦、处困而通达，就在于困于赤绂而仍能守正，所以说适宜祭祀。按语说：九五与九二相应，九二互体为离，离象征文明与红色，因此有赤绂之象。

《象传》说：受到劓、刖之刑，是因为志向尚未实现。

陆绩说：没有依靠也没有应援，所以志向尚未实现。九二说朱绂，这里说赤绂；九二说享祀，这里说祭祀，只是传文互相换用词语，没有其他含义。这里说九二困于九五、六三困于九四、九五与初六困于上六，是彼此交错受困的意思。

渐渐出现喜悦，是因为居中而正直。

崔憬说：因为居中且位置恰当，所以有喜悦。

适宜举行祭祀，是因为能够接受福祉。

荀爽说：九五与各爻相合，拥有国家、位置恰当并主持祭祀，所以能够接受福祉。

上六：困在葛藤蔓草之间，又处于危险不安之地。

虞翻说：巽象征草木丛生，葛藤指六三；兑象征受刑之人，所以困在葛藤与危险不安之中。

原文此处疑有缺字：“……曰动悔有悔，征吉。”

虞翻说：上六乘在阳爻之上，所以行动会有悔；变化后失去正位，所以仍有悔。六三已经变化得正，自己得到应援，因此出行吉祥。

《象传》说：困在葛藤之中，是因为位置不恰当。

虞翻说：这是说六三尚未变化得正位并与上六相应。

行动会有悔而后悔，是因为能够采取吉善的行动。

虞翻说：行动指六三变化后，才得到位置恰当的应爻，所以行动吉祥。"""),

    Entry("历代注疏", "唐李鼎祚《周易集解》卷十·井卦集解", "底本：维基文库《周易集解（四库全书本）/卷10》，页面正文行119—225。收录《序卦》、井卦经文、《彖》《象》、六爻及页面所载注文、案语。数字文本转录，尚未逐字回看文渊阁影印页。来源：https://zh.wikisource.org/zh-hans/周易集解_(四库全书本)/卷10", """《序卦》曰：困于上必反下，故受之以井。
崔憬曰：困极于劓刖，则反下以求安，故言困乎上必反下也。

（巽下坎上）井
郑玄曰：坎水也，巽木桔槹也。互体离兑，离外坚中虚，瓶也；兑为暗泽泉口也。言桔槔引瓶下入泉口，汲水而出，井之象也。井以汲人，水无空竭，犹人君以政教养天下，惠泽无穷也。
改邑不改井。
虞翻曰：泰初之五也。坤为邑，乾初之五折坤，故改邑。初为旧井，四应甃之，故不改井。
无丧无得，往来井井。
虞翻曰：无丧，泰初之五，坎象毁坏，故无丧。五来之初，失位无应，故无得。坎为通，故往来井井。往谓之五，来谓之初也。
汔至亦未繘井。
虞翻曰：巽绳为繘。汔，几也，谓二也。几至初改，未繘井，未有功也。
羸其瓶，凶。
虞翻曰：羸，钩罗也。艮为手，巽为繘，离为瓶，手繘折其中，故羸其瓶。体兑毁缺，瓶缺漏，故凶矣。
干宝曰：水殷德也，木周德也。夫井德之地也，所以养民性命而清洁之主者也。自震化行至于五世，改殷纣比屋之乱俗而不易成汤昭假之法度也，故曰改邑不改井。二代之制各因时宜，损益虽异，括囊则同，故曰无丧无得，往来井井也。当殷之末，井道之穷，故曰汔至。周德虽兴，未及革正，故曰亦未繘井。井泥为秽，百姓无聊比者之间，交受涂炭，故曰羸其瓶凶矣。

《彖》曰：巽乎水而上水，井。
荀爽曰：巽乎水谓阴下，谓巽也；而上水谓阳上为坎也。木入水出，井之象也。
井养而不穷也。
虞翻曰：兑口饮水，坎为通，往来井井，故养不穷也。
改邑不改井，乃以刚中也。
荀爽曰：刚得中，故为改邑；柔不得中，故为不改井也。
无丧无得。
荀爽曰：阴来居初有实，为无丧；失中为无得也。
往来井井。
荀爽曰：此本泰卦，阳往居五得坎为井，阴来在下亦为井，故曰往来井井也。
汔至亦未繘井。
荀爽曰：汔至者，阴来居初，下至汔竟也。繘者，所以出水通井道也。今乃在初，未得应五，故未繘也。繘者，绠汲之具也。
未有功也。
虞翻曰：谓二未变应五，故未有功也。
羸其瓶，是以凶也。
荀爽曰：井谓二，瓶谓初。初欲应五，今为二所拘羸，故凶也。
孔颖达曰：计覆一瓶之水，何足言凶？但此喻人德行不恒，不能善始令终，故就人言之凶也。

《象》曰：木上有水，井。
王弼曰：木上有水，上水之象也。水以养而不穷也。
君子以劳民劝相。
虞翻曰：君子谓泰乾也。坤为民，初上成坎为劝，故劳民劝相，相助也，谓以阳助坤矣。

初六：井泥不食，旧井无禽。
干宝曰：在井之下，体本土爻，故曰泥也。井而为泥，则不可食，故曰不食。此托纣之秽政，不可以养民也。旧井谓殷之未丧师也，亦皆清洁无水禽之秽，又况泥土乎？故旧井无禽矣。
《象》曰：井泥不食，下也；旧井无禽，时舍也。
虞翻曰：食，用也。初下称泥，巽为木果，无噬嗑食象；下而多泥，故不食也。乾为旧，位在阴下，故旧井无禽，时舍也，谓时舍于初，非其位也，与乾二同义。
崔憬曰：处井之下，无应于上，则是所用之井不汲，以其多涂；久废之井不获，以其时舍，故曰井泥不食，旧井无禽。禽，古擒字，禽犹获也。

九二：井谷射鲋，瓮敝漏。
虞翻曰：巽为谷、为鲋，鲋小鲜也。离为瓮，瓮瓶毁缺，羸其瓶凶，故瓮敝漏也。
《象》曰：井谷射鲋，无与也。
崔憬曰：唯得于鲋，无与于人也。井之为道，上汲者也；今与五非应，与初比，则是若谷水不注，唯及于鱼，故曰井谷射鲋也。瓮敝漏者，取其水下注不汲之义也。
案：鱼，阴虫也。初处井下体，又阴爻，鱼之象也。

九三：井渫不食，为我心恻。
荀爽曰：渫，去秽浊，清洁之意也。三者得正，故曰井渫；不得据阴，喻不得用，故曰不食。道既不行，故我心恻。
可用汲，王明并受其福。
荀爽曰：谓五可用汲，三则王道明而天下并受其福。
《象》曰：井渫不食，行恻也；求王明，受福也。
干宝曰：此托殷之公侯，时有贤者独守成汤之法度而不见任，谓微箕之伦也，故曰井渫不食，为我心恻。恻，伤悼也。民乃外附，故曰可用汲。周德来被，故曰王明。王得其民，民得其主，故曰求王明受福也。

六四：井甃，无咎。
荀爽曰：坎性下降，嫌于从三，能自修正以甃辅五，故无咎也。
《象》曰：井甃无咎，修井也。
虞翻曰：修，治也。以瓦甓垒井称甃。坤为土，初之五成离，离火烧土为瓦，治象，故曰井甃无咎，修井也。

九五：井冽，寒泉食。
虞翻曰：泉自下出称井。周七月，夏之五月，阴气在下；二已变，坎十一月为寒泉。初二已变体噬嗑食，故冽寒泉食矣。
《象》曰：寒泉之食，中正也。
崔憬曰：冽，清洁也。居中得正而比于上，则是井渫水清，既寒且洁，汲上可食于人者也。

上六：井收，勿幕，有孚元吉。
虞翻曰：幕，盖也。收谓以辘轳收繘也。坎为车，应巽绳为繘，故井收勿幕。有孚谓五坎，坎为孚，故元吉也。
《象》曰：元吉在上，大成也。
虞翻曰：谓初二已变成既济定，故大成也。
干宝曰：处井上，位在瓶之水也，故曰井收。幕，覆也。井以养生，政以养德；无覆水泉而不惠民，无蕴典礼而不兴教，故曰井收勿幕。勿幕则教信于民，民服教则大化成也。"""),

    Entry("历代注疏", "唐李鼎祚《周易集解》卷十·革卦集解", "底本：维基文库《周易集解（四库全书本）/卷10》，页面正文行226—335。收录《序卦》、革卦经文、《彖》《象》、六爻及页面所载注文、案语。数字文本转录，尚未逐字回看文渊阁影印页。来源：https://zh.wikisource.org/zh-hans/周易集解_(四库全书本)/卷10", """《序卦》曰：井道不可不革也，故受之以革。
韩康伯曰：井久则浊秽，宜革易其故也。

（离下兑上）革
郑玄曰：革，改也。水火相息而更用事，犹王者受命改正朔、易服色，故谓之革也。
已日乃孚，元亨利贞，悔亡。
虞翻曰：遁上之初，与蒙旁通。悔亡谓四也，四失正，动得位，故悔亡。离为日，孚谓坎，四动体离，五在坎中，故已日乃孚。以成既济，乾道变化，各正性命，保合太和，乃利贞，故元亨利贞悔亡矣，与乾彖同义也。

《彖》曰：革，水火相息。
虞翻曰：息，长也。离为火，兑为水。《系》曰“润之以风雨”，风巽雨兑也。四革之正，坎见，故独于此称水也。
二女同居，其志不相得，曰革。
虞翻曰：二女离兑体，同人象蒙艮为居，故二女同居。四变体两坎象，二女有志，离火志上，兑水志下，故其志不相得。坎为志也。
已日乃孚，革而信之。
干宝曰：天命已至之日也，乃孚，大信著也。武王陈兵孟津之上，诸侯不期而会者八百国，皆曰纣可伐矣。武王曰：“尔未知天命，未可也。”还归二年，纣杀比干，囚箕子，尔乃伐之，所谓已日乃孚，革而信也。
文明以说，大亨以正，革而当其悔乃亡。
虞翻曰：文明谓离，说兑也。大亨谓乾，四动成既济定，故大亨以正。革而当位，故悔乃亡也。
天地革而四时成。
虞翻曰：谓五位成乾为天，蒙坤为地，震春兑秋，四之正，坎冬离夏，则四时具。坤革而成乾，故天地革而四时成也。
汤武革命，顺乎天而应乎人。
虞翻曰：汤武谓乾，乾为圣人。天谓五，人谓三，四动顺五应三，故顺天应人，巽为命也。
革之时大矣哉。
虞翻曰：革天地成四时，诛二叔除民害，天下定武功成，故大矣哉也。

《象》曰：泽中有火，革。
崔憬曰：火就燥，泽资湿，二物不相得，终宜易之，故曰泽中有火革也。
君子以治历明时。
虞翻曰：君子遁乾也。历象谓日月星辰也。离为明，坎为月，离为日，蒙艮为星，四动成坎离，日月得正，天地革而四时成，故君子以治历明时也。

初九：巩用黄牛之革。
干宝曰：巩，固也。离为牝牛，离爻本坤，黄牛之象也。在革之初而无应据，未可以动，故曰巩用黄牛之革。此喻文王虽有圣德，天下归周，三分有二，而服事殷，其义也。
《象》曰：巩用黄牛，不可以有为也。
虞翻曰：得位无应，动而必凶，故不可以有为也。

六二：已日乃革之，征吉，无咎。
荀爽曰：日以喻君也，谓五已居位为君，二乃革意，去三应五，故曰已日乃革之。上行应五，去卑事尊，故曰征吉无咎也。
《象》曰：已日革之，行有嘉也。
崔憬曰：得位以正，居中有应，则是汤武行善、桀纣行恶，各终其日，然后革之，故曰已日乃革之，行此有嘉。
虞翻曰：嘉谓五，乾为嘉，四动承五，故行有嘉矣。

九三：征凶，贞厉。
荀爽曰：三应于上，欲往应之，为阴所乘，故曰征凶。若正居三而据二阴，则五来危之，故曰贞厉也。
革言三就，有孚。
翟玄曰：言三就上，二阳乾得共有信，据于二阴，故曰革言三就有孚于二矣。
《象》曰：革言三就，又何之矣。
崔憬曰：虽得位以正，而未可顿革，故以言就之。夫安者有其危也，故受命之君虽诛元恶，未改其命者，以即行改命，习俗不安。故曰征凶，犹以正自危，故曰贞厉。是以武王克纣，不即行周命，乃反商政，一就也；释箕子囚、封比干墓、式商容闾，二就也；散鹿台之财、发钜桥之粟，大赉于四海，三就也。故曰革言三就。
虞翻曰：四动成既济定，故又何之矣。

九四：悔亡，有孚改命吉。
虞翻曰：革而当其悔乃亡。孚谓五也。巽为命，四动五坎改巽，故改命吉。四乾为君，进退无恒，在离焚弃，体大过死，传以比桀纣；汤武革命，顺天应人，故改命吉也。
《象》曰：改命之吉，信志也。
虞翻曰：四动成坎，故信志也。
干宝曰：爻入上象，喻纣之郊也。以逆取而四海顺之，动凶器而前歌后舞，故曰悔亡也。中流而白鱼入舟，天命信矣，故曰有孚。甲子夜阵雨至，水德宾服之祥也，故曰改命之吉，信志也。

九五：大人虎变，未占有孚。
虞翻曰：乾为大人，谓五也。蒙坤为虎，变传论汤武以坤臣为君，占视也。离为占，四未之正，五未在坎，故未占有孚也。
马融曰：大人虎变，虎变威德折冲万里，望风而信，以喻舜舞干羽而有苗自服，周公修文德，越裳献雉，故曰未占有孚矣。
《象》曰：大人虎变，其文炳也。
宋衷曰：阳称大，五以阳居中，故曰大人。兑为白虎，九者变爻，故曰大人虎变，其文炳也。
虞翻曰：乾为大明，动成离，故其文炳也。

上六：君子豹变，小人革面，征凶，居贞吉。
虞翻曰：蒙艮为君子、为豹，从乾而更，故君子豹变也。阴称小人，面谓四；革为离，以顺承五，故小人革面。乘阳失正，故征凶；得位，故居贞吉，蒙艮为居也。
《象》曰：君子豹变，其文蔚也。
陆绩曰：兑之阳爻称虎，阴爻称豹，豹虎类而小者也。君子小于大人，故曰豹变，其文蔚也。
虞翻曰：蔚，蔇也。兑小，故其文蔚也。
小人革面，顺以从君也。
虞翻曰：乾君谓五也，四变顺五，故顺以从君也。
干宝曰：君子大贤次圣之人，谓若太公、周召之徒也。豹虎之属，蔚炳之次也。君圣臣贤，殷之顽民皆改志从化，故曰小人革面。天下既定，必倒载干戈，包之以虎皮，将卒之士使为诸侯，故曰征凶居贞吉，得正有应，君子之象也。
案：兑为口，乾为首，今口在首上，面之象也；乾为大人虎变也，兑为小人革面也。

《序卦》曰：革物者莫若鼎，故受之以鼎。
韩康伯曰：革去故，鼎取新。以去故，则宜制器立法以治新也。鼎所和齐生物成新之器也，故取象焉。"""),

    Entry("历代注疏", "唐李鼎祚《周易集解》卷十·鼎卦集解", "底本：维基文库《周易集解（四库全书本）/卷10》，页面正文行336—440。收录《序卦》、鼎卦经文、《彖》《象》、六爻及页面所载注文、案语。鼎卦爻辞中“铉”字在页面以图像显示，以下用“【页面图像字形：铉】”标明，不视为已完成字形核验。来源：https://zh.wikisource.org/zh-hans/周易集解_(四库全书本)/卷10", """《序卦》曰：革物者莫若鼎，故受之以鼎。
韩康伯曰：革去故，鼎取新。以去故，则宜制器立法以治新也。鼎所和齐生物成新之器也，故取象焉。

（巽下离上）鼎
郑玄曰：鼎，象也。卦有木火之用，互体乾兑，乾为金，兑为泽。泽钟金而含水，爨以木火，鼎亨熟物之象。鼎亨熟以养人，犹圣君兴仁义之道以教天下也，故谓之鼎矣。
元吉亨。
虞翻曰：大壮上之初，与屯旁通。天地交，柔进上行，得中应乾五刚，故元吉亨也。

《彖》曰：鼎，象也。以木巽火，亨饪也。
荀爽曰：震入离下，中有乾象，木火在外，金在其内，鼎镬烹饪之象也。
虞翻曰：六十四卦皆观《系辞》，而独于鼎言象，何也？象事知器，故独言象也。
九家易曰：鼎言象者，卦也。木火互有乾兑，乾金兑泽，泽者水也。爨以木火，是鼎镬烹饪之象，亦象三公之位：上则调和阴阳，下而抚育百姓。鼎能熟物养人，故云象也。牛鼎受一斛，天子饰以黄金，诸侯白金；三足以象三台，足上皆作鼻目为饰也。羊鼎五斗，天子饰以黄金，诸侯白金，大夫以铜；豕鼎三斗，天子饰以黄金，诸侯白金，大夫铜、士铁。三鼎形同，烹饪煮肉，上离阴爻为肉也。
圣人亨以享上帝，而大亨以养圣贤。
虞翻曰：圣人谓乾，初四易位，体大畜。震为帝，在乾天上，故曰上帝。体颐象，三动噬嗑食，故以享上帝也。大亨谓天地养万物，圣人养贤以及万民，贤之能者称圣人矣。
巽而耳目聪明。
虞翻曰：谓三也。三在巽上，动成坎离，有两坎两离象，乃称聪明。日月相推而明生焉，故巽而耳目聪明。眇能视不足以有明，闻言不信、聪不明，皆有一离一坎象故也。
柔进而上行，得中而应乎刚，是以元亨。
虞翻曰：柔谓五，得上中应乾五刚。巽为进，震为行，非谓应二刚，与睽五同义也。

《象》曰：木上有火，鼎。
荀爽曰：木火相因，金在其间，调和五味，所以养人，鼎之象也。
君子以正位凝命。
虞翻曰：君子谓三也。鼎五爻失正，独三得位，故以正位凝成也。体姤谓阴始凝初，巽为命，故君子以正位凝命也。

初六：鼎颠趾，利出否，得妾以其子，无咎。
虞翻曰：趾，足也。应在四，大壮震为足，折入大过，大过颠也，故鼎颠趾也。
虞翻曰：初阴在下，故否。利出之四，故曰利出。兑为妾，四变得正成震，震为长子，继世守宗庙而为祭主，故得妾以其子无咎矣。
《象》曰：鼎颠趾，未悖也。
荀爽曰：以阴乘阳，故未悖也。
利出否，以从贵也。
虞翻曰：出初之四，承乾五，故以从贵也。

九二：鼎有实，我仇有疾，不我能即，吉。
虞翻曰：二为实，故鼎有实也。坤为我，谓四也。二据四妇，故相与为仇。谓三变时四体坎，坎为疾，故我仇有疾。四之二历险，二动得正，故不我能即吉。
《象》曰：鼎有实，慎所之也。
虞翻曰：二变之正，艮为慎。
我仇有疾，终无尤也。
虞翻曰：不我能即吉，故终无尤也。

九三：鼎耳革，其行塞，雉膏不食。
虞翻曰：动成两坎，坎为耳，而革在乾。初四变时，震为行，鼎以耳行，伏坎震折而入乾，故其行塞。离为雉，坎为膏，初四已变，三动体颐，颐中无物，离象不见，故雉膏不食。
方雨亏悔，终吉。
虞翻曰：谓四已变，三动成坤，坤为方，坎为雨，故曰方雨。三动亏乾而失位悔也，终复之正，故方雨亏悔终吉也。
《象》曰：鼎耳革，失其义也。
虞翻曰：鼎以耳行，耳革行塞，故失其义也。

九四：鼎折足，覆公【页面异体字形：𫗧】，其形渥，凶。
虞翻曰：谓四变时，震为足，足折入兑，故鼎折足。兑为形，渥，大形也。鼎足折则公【页面异体字形：𫗧】覆，言不胜任，象入大过死，凶，故鼎足折覆公【页面异体字形：𫗧】其形渥凶。
九家易曰：鼎者，三足一体，犹三公承天子也。三公谓调阴阳，鼎谓调五味。足折【页面异体字形：𫗧】覆，犹三公不胜其任，倾败天子之美，故曰覆【页面异体字形：𫗧】也。
案：【页面异体字形：𫗧】者，雉膏之属；公者，四为诸侯上公之位，故曰公【页面异体字形：𫗧】。
《象》曰：覆公【页面异体字形：𫗧】，信如何也。
九家易曰：渥者，厚大，言罪重也。既覆公【页面异体字形：𫗧】，信有大罪，刑罚当加，无可如何也。

六五：鼎黄耳，金【页面图像字形：铉】，利贞。
虞翻曰：离为黄，三变坎为耳，故鼎黄耳。【页面图像字形：铉】谓三贯鼎两耳。乾为金，故金【页面图像字形：铉】。动而得正，故利贞。
干宝曰：凡举鼎者【页面图像字形：铉】也，尚三公者王也。金喻可贵中之美也，故曰金【页面图像字形：铉】。【页面图像字形：铉】鼎得其物，施令得其道，故曰利贞也。
《象》曰：鼎黄耳，中以为实也。
陆绩曰：得中承阳，故曰中以为实。
宋衷曰：五当耳中，色黄，故曰鼎黄耳。兑为金，又正秋，故曰金【页面图像字形：铉】。公侯谓五也，上尊故玉，下卑故金。金和良可柔屈，喻诸侯顺天子。

上九：鼎玉【页面图像字形：铉】，大吉，无不利。
虞翻曰：【页面图像字形：铉】谓三，乾为玉。【页面图像字形：铉】体大有上九自天祐之位，贵据五，三动承上，故大吉无不利。谓三亏悔应上成未济，虽不当位，六位相应，故刚柔节。《彖》曰巽耳目聪明，为此九三发也。
干宝曰：玉又贵于金者。凡烹饪之事，自镬升于鼎，载于俎，自俎入于口，馨香上达，动而弥贵，故鼎之义上爻愈吉也。鼎主烹饪，不失其和；金玉【页面图像字形：铉】之不失其所，公卿仁贤、天王圣明之象也。君臣相临，刚柔得节，故曰吉无不利也。
《象》曰：玉【页面图像字形：铉】在上，刚柔节也。
宋衷曰：以金承玉，君臣之节。上体乾为玉，故曰玉【页面图像字形：铉】。虽非其位，阴阳相承，刚柔之节也。"""),

    Entry("历代注疏", "唐李鼎祚《周易集解》卷十·震卦集解", "底本：维基文库《周易集解（四库全书本）/卷10》，正文行441—530。含《序卦》、经文、《彖》《象》、六爻及页面注文、案语；数字文本转录，未逐字回看影印页。来源：https://zh.wikisource.org/zh-hans/周易集解_(四库全书本)/卷10", """《序卦》曰：主器者莫若长子，故受之以震。震者，动也。
崔憬曰：鼎所以烹饪享于上帝，主此器者莫若冢嫡，以为其祭主也，故言主器者莫若长子也。
（震下震上）震：亨。
郑玄曰：震为雷，雷动物之气也。雷之发声，犹人君出政教以动中国之人也，故谓之震。人君有善声教，则嘉会之礼通矣。
震来虩虩，笑言哑哑。
虞翻曰：临二之四，天地交，故通。虩虩谓四也，来应初命，四变而来应己，四失位多惧，故虩虩之内曰来也。
虞翻曰：哑哑，笑且言，谓初也。得正有则，故笑言哑哑，后有则也。
震惊百里，不丧𠤎鬯。
虞翻曰：谓阳从临二，阴为百二十，举其大数，故当震百里也。坎为棘，匕上震为鬯，坤为丧，二上之坤成震，体坎得其𠤎鬯，故不丧𠤎鬯也。
郑玄曰：雷发声闻于百里，古者诸侯之象。诸侯出教令能警戒其国内，则守其宗庙社稷，为之祭主，不亡𠤎与鬯也。人君于祭之礼，匕牲体荐鬯而已，其余不亲也。升牢于俎，君匕之，臣载之。鬯，秬酒芬芳修鬯，因名焉。
《彖》曰：震亨，震来虩虩，恐致福也。
虞翻曰：惧变承五应初，故恐致福也。
笑言哑哑，后有则也。
虞翻曰：则，法也。坎为则也。
震惊百里，惊远而惧迩也。
虞翻曰：远谓四，近谓初。震为百，谓四出惊远，初应惧近也。
出可以守宗庙社稷，以为祭主也。
虞翻曰：为五出之正，震为守，艮为宗庙社稷。长子主祭器，故以为祭主也。
干宝曰：周木德，震之正象也。为殷诸侯，殷诸侯之制，其地百里，是以文王小心翼翼，昭事上帝，聿怀多福，厥德不回，以受方国。故以百里而臣诸侯也。为诸侯，故主社稷；为长子，而为祭主也。祭礼荐陈甚多，而经独言不丧匕鬯者，匕牲体、荐鬯酒，人君所自亲也。
《象》曰：洊雷，震。君子以恐惧修省。
虞翻曰：君子谓临二，二出之坤，四体以修身。坤为身，二之四以阳照坤，故以恐惧修省。老子曰：“修之身，其德乃真也。”
初九：震来虩虩，后笑言哑哑，吉。
虞翻曰：虩虩谓四也。初位在下，故言后笑哑哑，得位吉故也。
干宝曰：得震之正，首震之象者，震来虩虩，羑里之厄也；笑言哑哑，后受方国也。
《象》曰：震来虩虩，恐致福也。
虞翻曰：阳称福。
笑言哑哑，后有则也。
虞翻曰：得正，故有则也。
六二：震来厉，亿丧贝，跻于九陵，勿逐，七日得。
虞翻曰：厉，危也。乘刚故厉。亿，惜辞也。坤为丧，三动离为蠃蚌，故称贝；在艮山下，故称陵。震为足，足乘初九，故跻于九陵。震为逐，谓四已体复象，故丧且勿逐。三动时离为日，震数七，故七日得者也。
《象》曰：震来厉，乘刚也。
干宝曰：六二，木爻，震之身也。得位无应，而以乘刚为危，此托文王积德累功以被囚为祸也，故曰震来厉。亿，叹辞也。贝，宝货也，产乎东方，行乎大涂也。此以喻纣拘文王，闳夭之徒乃于江淮之浦求盈箱之贝而以赂纣也，故曰亿丧贝。贝水物而方升于九陵，今虽丧之，犹外府也，故曰勿逐七日得。七日得者，七年之日也，故《书》曰“诞保文武受命惟七年”，是也。
六三：震苏苏，震行无眚。
《象》曰：震苏苏，位不当也。
虞翻曰：死而复生称苏。三死坤中，动出得正，震为生，故苏苏。坎为眚，三出得正，坎象不见，故无眚。《春秋传》曰：“晋获秦谍，六日而苏也。”
九四：震遂泥。
虞翻曰：坤土得雨为泥，位在坎中，故遂泥也。
《象》曰：震遂泥，未光也。
虞翻曰：在坎阴中，与屯五同义，故未光也。
六五：震往来厉，亿无丧，有事。
虞翻曰：往谓乘阳，来谓应阴，失位乘刚，故往来厉也。
虞翻曰：坤为丧也。事谓祭祀之事，出而体随，王享于西山，则可以守宗庙社稷，为祭主，故无丧有事也。
《象》曰：震往来厉，危行也。
虞翻曰：乘刚山顶，故危行也。
其事在中，大无丧也。
虞翻曰：动出得正，故无丧。
上六：震索索，视矍矍，征凶。震不于其躬，于其邻，无咎。婚媾有言。
虞翻曰：上谓四也。欲之三，隔坎，故震索索；三已动，应在离，故矍矍者也。
虞翻曰：上得位，震为征，故征凶。四变时坤为躬，邻谓五也。四上之五，震东兑西，故称邻。之五得正，故不于其躬，于其邻无咎。谓三已变，上应三，震为言，故婚媾有言。
《象》曰：震索索，中未得也。
虞翻曰：四未之五，故中未得也。
虽凶无咎，畏邻戒也。
虞翻曰：谓五正位已乘之，逆畏邻戒也。"""),

    Entry("历代注疏", "唐李鼎祚《周易集解》卷十·艮卦集解", "底本：维基文库《周易集解（四库全书本）/卷10》，正文行531—595。含《序卦》、经文、《彖》《象》、六爻及页面注文、案语；数字文本转录，未逐字回看影印页。来源：https://zh.wikisource.org/zh-hans/周易集解_(四库全书本)/卷10", """《序卦》曰：物不可以终动，动必止之，故受之以艮。艮者，止也。
崔憬曰：震极则征凶，婚媾有言，当须止之，故言物不可以终动，止之矣。
（艮下艮上）艮其背，不获其身；行其庭，不见其人，无咎。
郑玄曰：艮为山，山立峙，各于其所，无相顺之时。犹君在上、臣在下，恩敬不相与通，故谓之艮也。
虞翻曰：观五之三也。艮为多节，故称背。观坤为身，观五之三折坤为背，故艮其背。坤象不见，故不获其身。震为行，人艮为庭，坎为隐伏，故行其庭不见其人。三得正，故无咎。
案：艮为门阙，今纯艮重其门阙，两门之间庭中之象也。
《彖》曰：艮，止也。
虞翻曰：位穷于上，故止也。
时止则止，时行则行。
虞翻曰：时止谓上阳穷止，故止；时行谓三体处震为行也。
动静不失其时，其道光明。
虞翻曰：动谓三，静谓上。艮止则止，震行则行，故不失时。五动成离，故其道光明。
艮其止，止其所也。
虞翻曰：谓两象各止其所。
上下敌应，不相与也。
虞翻曰：艮其背，背也。两象相背，故不相与也。
是以不获其身，行其庭不见其人，无咎也。
案：其义已见繇辞也。
《象》曰：兼山，艮。君子以思不出其位。
虞翻曰：君子谓三也。三君子位，震为出，坎为隐伏、为思，故以思不出其位也。
初六：艮其趾，无咎，利永贞。
虞翻曰：震为趾，故艮其趾矣。失位变得正，故无咎永贞也。
《象》曰：艮其趾，未失正也。
虞翻曰：动而得正，故未失正也。
六二：艮其腓，不拯其随，其心不快。
虞翻曰：巽长为股，艮小为腓。拯，取也。随谓下二阴。艮为止，震为动，故不拯其随。坎为心，故其心不快。
《象》曰：不拯其随，未退听也。
虞翻曰：坎为耳，故未退听也。
九三：艮其限，列其夤，厉阍心。
虞翻曰：限，腰带处也。坎为腰，五来之三，故艮其限。夤，脊肉；艮为背，坎为脊，艮为手，震起艮止，故列其夤。坎为心，厉，危也。艮为阍，阍，守门人；坎盗动门，故厉熏心。古阍作熏字，马因言熏灼其心，未闻易道以坎水熏灼人也。荀氏以熏为勲，或误作动，皆非也。
《象》曰：艮其限，危熏心也。
虞翻曰：坎为心，坎盗动门，故危熏心也。
六四：艮其身，无咎。
虞翻曰：身，腹也。观坤为身，故艮其身。得位承五，故无咎。或谓妊身也。五动则四体离，妇离为大腹，孕之象也，故艮其身。得正承五而受阳施，故无咎。《诗》曰：“大任有身，生此文王也。”
《象》曰：艮其身，止诸躬也。
虞翻曰：艮为止，五动乘四，则任身，故止诸躬也。
六五：艮其辅，言有孚，悔亡。
虞翻曰：辅，面颊骨，上颊车者也。三至上体颐象，艮为止，在坎车上，故艮其辅，谓辅车相依。震为言，五失位悔也，动得正，故言有孚悔亡也。
《象》曰：艮其辅，以中正也。
虞翻曰：五动之中，故以正中也。
上九：敦艮，吉。
虞翻曰：无应，静止下据二阴，故敦艮吉也。
《象》曰：敦艮之吉，以厚终也。
虞翻曰：坤为厚，阳上据坤，故以厚终也。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十一·渐至兑六卦经文", "底本：维基文库《周易集解（四库全书本）/卷11》，正文行120—651。此条先录本卷六卦经文及各爻辞，卷内《序卦》、彖象传与虞翻、崔憬、干宝、王弼等注文全文仍待逐段转录；不得将本条视为卷十一全文完成。经文据该页，尚未逐字回看文渊阁影印页。原文来源：https://wk.100ke.info/zh-hans/周易集解_(四库全书本)/卷11", """卷十一范围：渐、归妹、丰、旅、巽、兑。以下先录六卦经文，保留经文层次；注文全文及异文校勘另待补录。

《渐》
渐：女归吉，利贞。
初六：鸿渐于干，小子厉，有言，无咎。
六二：鸿渐于磐，饮食衎衎，吉。
九三：鸿渐于陆，夫征不复，妇孕不育，凶；利御寇。
六四：鸿渐于木，或得其桷，无咎。
九五：鸿渐于陵，妇三岁不孕，终莫之胜，吉。
上九：鸿渐于陆，其羽可用为仪，吉。

《归妹》
归妹：征凶，无攸利。
初九：归妹以娣，跛能履，征吉。
九二：眇而视，利幽人之贞。
六三：归妹以须，反归以娣。
九四：归妹愆期，迟归有时。
六五：帝乙归妹，其君之袂，不如其娣之袂良；月几望，吉。
上六：女承筐无实，士刲羊无血，无攸利。

《丰》
丰：亨，王假之，勿忧，宜日中。
初九：遇其配主，虽旬无咎，往有尚。
六二：丰其蔀，日中见斗，往得疑疾；有孚发若，吉。
九三：丰其沛，日中见沬，折其右肱，无咎。
九四：丰其蔀，日中见斗，遇其夷主，吉。
六五：来章，有庆誉，吉。
上六：丰其屋，蔀其家，窥其户，阒其无人，三岁不觌，凶。

《旅》
旅：小亨，旅贞吉。
初六：旅琐琐，斯其所取灾。
六二：旅即次，怀其资，得僮仆，贞。
九三：旅焚其次，丧其僮仆，贞厉。
九四：旅于处，得其资斧，我心不快。
六五：射雉，一矢亡，终以誉命。
上九：鸟焚其巢，旅人先笑后号咷；丧牛于易，凶。

《巽》
巽：小亨，利有攸往，利见大人。
初六：进退，利武人之贞。
九二：巽在床下，用史巫纷若，吉，无咎。
九三：频巽，吝。
六四：悔亡，田获三品。
九五：贞吉，悔亡，无不利；无初有终；先庚三日，后庚三日，吉。
上九：巽在床下，丧其资斧，贞凶。

《兑》
兑：亨，利贞。
初九：和兑，吉。
九二：孚兑，吉，悔亡。
六三：来兑，凶。
九四：商兑未宁，介疾有喜。
九五：孚于剥，有厉。
上六：引兑。

译文状态：本条未提供完整逐段译文，标记为“暂无完整逐段译文”；不以现代白话改写冒充古籍原注。
校勘状态：本条为四库本维基文库数字文本的经文摘录，不代表已完成影印本逐字校勘。遇到不同通行本字形、断句或异文时，须另列版本依据，不静默统一。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十一·渐、归妹两卦集解注文", "底本：景印文渊阁《四库全书》本《周易集解》卷十一，维基文库数字转录第118—295行。补录《渐》《归妹》两卦所见经传、虞翻、崔憬、干宝、王肃等注文及案语；与卷十一六卦经文条目分开保存。数字转录的异体、讹脱和标点尚待影印页逐字复核。", """底本：景印文渊阁《四库全书》本《周易集解》卷十一，维基文库数字转录第118—295行。以下为《渐》《归妹》两卦经传及所见注文的连续录文，保留底本中“干/乾”“兊/兑”等字形差异、注家标识及案语；数字文本仍须对照影印页复核。

《序卦》曰：物不可以终止，故受之以渐。渐者，进也。
崔憬曰：终止虽获敦艮，时行须渐进行，故曰物不可终止，故受之以渐。渐者，进也。

䷴〈艮下巽上〉渐：女归吉，利贞。
虞翻曰：否三之四，女谓四归嫁也。坤三之四，承五进得位，往有功，反成归妹，兑女归吉。初上失位，故利贞，可以正邦也。

《彖》曰：渐之进也，女归吉也。
虞翻曰：三进四得位，阴阳体正，故吉也。

进得位，往有功也。
虞翻曰：功谓五。四进承五，故往有功，巽为进也。

进以正，可以正邦也。其位刚得中也。
虞翻曰：谓初已变为家人，四进已正而上不正，三动成坤为邦，上来反三，故进以正可以正邦。其位刚得中，与家人道正同义。三在外体之中，故称得中。乾《文言》曰“中不在人”，谓三也。此可谓上变既济定者也。

止而巽，动不穷也。
虞翻曰：止艮也。三变震为动，上之三据坤，动震成坎，坎为通，故动不穷。往来不穷谓之通。

《象》曰：山上有木，渐。君子以居贤德善俗。
虞翻曰：君子谓否乾，乾为贤德；坤阴小人为俗，乾四之坤为艮为居，以阳善阴，故以居贤德善俗也。

初六：鸿渐于干，小子厉，有言，无咎。
虞翻曰：鸿，大雁也。离五，鸿渐进也。小水从山流下称干。艮为山，为小径；坎水流下山，故鸿渐于干也。艮为小子，初失位故厉，变得正，三动受上成震，震为言，故小子厉有言，无咎也。

《象》曰：小子之厉，义无咎也。
虞翻曰：动而得正，故义无咎也。

六二：鸿渐于磐，饮食衎衎，吉。
虞翻曰：艮为山石，坎为聚，聚石称磐。初已之正，体噬嗑食；坎水阳物并在颐中，故饮食衎衎。得正应五，故吉。

《象》曰：饮食衎衎，不素饱也。
虞翻曰：素，空也。承三应五，故不素饱。

九三：鸿渐于陆。
虞翻曰：高平称陆，谓初已变坎水为平，三动之坤，故鸿渐于陆。

夫征不复。
虞翻曰：谓初已之正，三动成震，震为征、为夫，而体复象。坎阳死坤中，坎象不见，故夫征不复也。

妇孕不育，凶。
虞翻曰：孕，妊娠也；育，生也。巽为妇，离为孕，三动成坤，离毁夫位，故妇孕不育，凶。

利用御寇。
虞翻曰：御，当也。坤为用，巽为高，艮为山，离为戈兵甲胄，坎为寇。自上御下，三动坤顺，坎象不见，故利用御寇，顺相保，保大也。

《象》曰：夫征不复，离群丑也。
虞翻曰：坤三爻为丑，物三称群也。

妇孕不育，失其道也。
虞翻曰：三动离毁，阳陨坤中，故失其道也。

利用御寇，顺相保也。
虞翻曰：三动坤顺，坎象不见，故以顺相保也。

六四：鸿渐于木，或得其桷，无咎。
虞翻曰：巽为木。桷，椽也，方者谓之桷。巽为交、为长木，艮为小木，坎为脊，离为丽。小木丽长木，巽绳束之象；脊之形，椽桷象也。故或得其桷，得位顺五，故无咎。四已承五，又顾得三，故或得其桷也矣。

《象》曰：或得其桷，顺以巽也。
虞翻曰：坤为顺，以巽顺五。
案：四居巽木爻，阴位正，直桷之象也。自二至五体有离坎，离为飞鸟而居坎水，鸿之象也。鸿随阳鸟，喻女从夫，卦明渐义，爻皆称焉。

九五：鸿渐于陵，妇三岁不孕。
虞翻曰：陵，丘。妇谓四也。三动受上时，而四体半艮山，故称陵。巽为妇，离为孕，坎为岁，三动离坏，故妇三岁不孕。

终莫之胜，吉。
虞翻曰：莫，无也。胜，陵也。得正居中，故莫之胜，吉。上终变之三，成既济定，坎为心，故《象》曰得所愿也。

《象》曰：终莫之胜吉，得所愿也。
虞翻曰：上之三，既济定，故得所愿也。

上九：鸿渐于陆。
虞翻曰：陆谓三也。三坎为平，变而成坤，故称陆也。

其羽可用为仪，吉。
虞翻曰：谓三变受成既济，与家人《彖》同义。上之二得正，离为鸟，故其羽可用为仪，吉。三动失位，坎为乱，乾四止坤。《象》曰“不可乱”，《彖》曰“进以正邦”，为此爻发也。三已得位，又变受上权也。孔子曰“可与适道，未可与权”，宜无怪焉。

《象》曰：其羽可用为仪吉，不可乱也。
虞翻曰：坤为乱。上来正，坤六爻得位，成既济定，故不可乱也。
干宝曰：处渐高位，断渐之进，顺艮之言，谨巽之全，履坎之通，据离之耀。妇德既终，母教又明，有德而可受，有仪而可象，故曰其羽可以为仪，不可乱也。

《序卦》曰：进必有所归，故受之以归妹。
崔憬曰：鸿渐于磐，饮食衎衎，言六比三女渐归夫之象也，故云进必有所归也。

䷵〈兑下震上〉归妹。
虞翻曰：归，嫁也。兑为妹，泰三之四。坎月离日，俱归妹象。阴阳之义配日月，则天地交而万物通，故以嫁娶也。

征凶。
虞翻曰：谓四也。震为征，三之四不当位，故征凶也。

无攸利。
虞翻曰：谓三也。四之三失正无应，以柔乘刚，故无攸利也。

《彖》曰：归妹，天地之大义也。
虞翻曰：乾天坤地，三之四，天地交。以离日坎月战阴阳，阴阳之义配日月，则万物兴，故天地之大义。乾主壬，坤主癸，日月会北。震为玄黄，天地之杂；震东、兑西、离南、坎北，六十四卦此象最备，四时正卦，故天地之大义也。

天地不交，而万物不兴。
虞翻曰：乾三之坤四，震为兴。天地以离坎交阴阳，故天地不交则万物不兴矣。
王肃曰：男女交而后人民蕃，天地交然后万物兴，故归妹以及天地交之义也。

归妹，人之终始也。
虞翻曰：人始生乾而终于坤，故人之终始。《杂卦》曰“归妹，女之终”，谓阴终坤癸，则乾始震庚也。
干宝曰：归妹者，衰落之女也。父既没矣，兄主其礼，子续父业，人道所以相终始也。

说以动，所归妹也。
虞翻曰：说兑，动震也。谓震嫁兑，所归必妹也。

征凶，位不当也。
崔憬曰：中四爻皆失位，以象归妹非正嫡，故征凶也。

无攸利，柔乘刚也。
王肃曰：以征则有不正之凶，以处则有乘刚之进也，故无所利矣。

《象》曰：泽上有雷，归妹。
干宝曰：雷薄于泽，八月九月将藏之时也。君子象之，故不敢恃当今之虞，而虑将来祸也。

君子以永终知敝。
虞翻曰：君子谓乾也。坤为永终、为敝，乾为知。三之四为永终，四之三兑为毁折，故以永终知敝。
崔憬曰：归妹人之始终也，始则征凶，终则无攸利，故君子以永终知敝为戒者也。

初九：归妹以娣，跛能履，征吉。
虞翻曰：震为兄，故嫁妹谓三也。初在三下，动而应四，故称娣。履，礼也。初九应变成坎，坎为曳，故跛而履。应在震为征，初为娣，变为阴，故征吉也。

《象》曰：归妹以娣，以恒也。跛能履，吉相承也。
虞翻曰：阳得位，故以恒。恒动初承二，故吉相承也。

九二：眇而视，利幽人之贞。
虞翻曰：视应五也。震上兑下，离目不正，故眇而视。幽人谓二之初，动二在坎中，故称幽人。变得正，震喜兑说，故利幽人之贞，与履二同义也。

《象》曰：利幽人之贞，未变常也。
虞翻曰：常，恒也。乘初未之五，故未变常矣。

六三：归妹以须，反归以娣。
虞翻曰：须，需也。初至五体需象，故归妹以须。娣谓初也。震为反，反为归也。三失位，四反得正，兑进在四，见初进之，初在兑后，故反归以娣。

《象》曰：归妹以须，未当也。
虞翻曰：三未变之阳，故位未当。

九四：归妹愆期，迟归有时。
虞翻曰：愆，过也。谓二变三动之正，体大过象。坎月离日为期，三变日月不见，故愆期。坎为曳，震为行，行曳故迟也。归谓反三，震春兑秋，坎冬离夏，四时体正，故归有时也。

《象》曰：愆期之志，有待而行也。
虞翻曰：待男行矣。

六五：帝乙归妹，其君之袂，不如其娣之袂良。
虞翻曰：三四已正，震为帝，坤为乙，故曰帝乙。泰乾为良、为君，乾在下为小君，则妹也。袂，口袂之饰也。兑为口，乾为衣，故称袂。谓三失位无应，娣袂谓二得中应五。三动成乾为良，故其君之袂不如其娣之袂良，故《象》曰以贵行也矣。

月几望，吉。
虞翻曰：几，其也。坎月离日，兑西震东，日月象对，故曰几望。二之五，四复三得正，故吉也，与小畜、中孚“月几望”同义也。

《象》曰：帝乙归妹，不如其娣之袂良也。
虞翻曰：三四复正，乾为良。

其位在中，以贵行也。
虞翻曰：三四复，二之五成既济，五贵，故以贵行也。

上六：女承筐无实。
虞翻曰：女谓应三兑也。自下受上称承，震为筐。以阴应阴，三四复位，坎为虚，故无实。《象》曰“承虚筐也”。

士刲羊无血，无攸利。
虞翻曰：刲，刺也。震为士，兑为羊，离为刀，故士刲羊。三四复位成泰，坎象不见，故无血。三柔承刚，故无攸利也。

《象》曰：上六无实，承虚筐也。
虞翻曰：泰坤为虚，故承虚筐也。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十一·丰卦集解注文", "底本：景印文渊阁《四库全书》本《周易集解》卷十一，维基文库数字转录第296—424行。录《丰》卦经传、虞翻、崔憬、姚信、九家易、荀爽、干宝、孟喜等注文及案语，并保留篇末引向《旅》的《序卦》文字。数字转录仍待影印页逐字校勘。", """底本：景印文渊阁《四库全书》本《周易集解》卷十一，维基文库数字转录第296—424行。以下录《丰》卦及前后《序卦》注文，保留虞翻、崔憬、姚信、九家易、荀爽、干宝、孟喜等注家文字及案语。页面异体、疑似讹字和标点仍待影印本复核。

《序卦》曰：得其所归者必大，故受之以丰。丰者，大也。
崔憬曰：归妹者，侄娣媵国三人，凡九女为大援，故言得其所归者必大也。

䷧〈离下震上〉丰：亨。
虞翻曰：此卦三阴三阳之例，当从泰二之四，而丰三从噬嗑上来之三，折四于五狱中而成丰，故君子以折狱致刑。阴阳交故通噬嗑，所谓利用狱者，此卦之谓也。

王假之。
虞翻曰：乾为王，假，至也。谓四宜上至五，动之正成乾，故王假之，尚大也。

勿忧，宜日中。
虞翻曰：五动之正，则四变成离，离日中，当五在坎中，坎为忧，故勿忧，宜日中。体两离象，照天下也。日中则昃，月盈则蚀，天地盈虚，与时消息。
干宝曰：丰，坎宫，阴世在五，以其宜中而忧其侧也。坎为夜，离为昼，以离变坎，至于天位，日中之象也。殷水德，坎象昼败，而离居之，周伐殷居王位之象也。圣人德大而心小，既居天位而戒惧不怠。“勿忧”者，劝勉之言也。犹《诗》曰“上帝临汝，无贰尔心”，言周德当天人之心，宜居王位，故宜日中。

《彖》曰：丰，大也。明以动，故丰。
崔憬曰：离下震上，明以动之象。明则见微，动则成务，故能大矣。

王假之，尚大也。
姚信曰：四体震，王假大也。四上之五，得其盛位，谓之大。

勿忧，宜日中。
九家易曰：震动而上，故勿忧也。日者君，中者五，君宜居五也。谓阴处五，日中之位，当倾昃矣。

宜照天下也。
虞翻曰：五动成乾，乾为天；四动成两离，重明丽正，故宜照天下，谓化成天下也。

日中则昃。
荀爽曰：丰者至盛，故曰中。下居四，日昃之象也。

月盈则蚀。
虞翻曰：月之行生震，见兑，盈于乾甲；五动成乾，故月盈。四变体噬嗑食，故则食。此丰其屋、蔀其家也。

天地盈虚，与时消息，而况于人乎？况于鬼神乎？
虞翻曰：五息成乾为盈，四消入坤为虚，故天地盈虚也。丰之既济，四时象具；乾为神，人；坤为鬼。鬼神与人亦随时消息，谓人谋鬼谋、百姓与能，与时消息。

《象》曰：雷电皆至，丰。
荀爽曰：丰者，阴据不正，夺阳之位而行以丰，故折狱致刑以讨除之也。

君子以折狱致刑。
虞翻曰：君子谓三。噬嗑四失正，系在坎狱中，故上之三，折四入大过死象，故以折狱致刑。兑折为刑，贲三得正，故无敢折狱也。

初九：遇其配主。
虞翻曰：妃嫔谓四也。四失位，在震为主，五动体姤遇，故遇其配主也。

虽旬无咎，往有尚。
虞翻曰：谓四失位，变成坤，应初。坤数十，四上而之五，成离，离为日。

《象》曰：虽旬无咎，过旬灾也。
虞翻曰：体大过，故过旬灾。四上之五，坎为灾也。

六二：丰其蔀，日中见斗，往得疑疾。
虞翻曰：日蔽云中称蔀，蔀小也。谓四二利四之五，故丰其蔀。噬嗑离为见，象在上为日中，艮为斗，斗七星也。噬嗑艮为星、为止，坎为北中，巽为高，舞星上于中而舞者，北斗之象也。离上之三，隐坎云下，故日中见斗。四往之五得正成坎，坎为疑疾，故往得疑疾也。

有孚发若，吉。
虞翻曰：坎为孚，四发之五成坎，孚动而得位，故有孚发若，吉也。

《象》曰：有孚发若，信以发志也。
虞翻曰：四发之五，坎为志也。
九家易曰：信著于五，然后乃可发其顺志。

九三：丰其沛，日中见沬。
虞翻曰：日在云下称沛，沛不明也。沬，小星也。噬嗑离为日，艮为沬，故日中见沬。上之三，日入坎云下，故见沬也。
九家易曰：大暗谓之沛，沬，斗杓后小星也。

折其右肱，无咎。
虞翻曰：兑为折、为右，噬嗑艮为肱。上来之三，折艮入兑，故折其右肱。之三得正，故无咎也。

《象》曰：丰其沛，不可大事也。
虞翻曰：利四之阴，故不可大事。

折其右肱，终不可用也。
虞翻曰：四死大过，故终不可用。

九四：丰其蔀。
虞翻曰：蔀，蔽也。噬嗑离日之坎云中，故丰其蔀。
《象》曰：位不当也。

日中见斗。
虞翻曰：噬嗑日在上为中，上之三为巽，巽为入，日入坎云下，幽伏不明，故日中见斗。
《象》曰：幽不明是其义也。

遇其夷主，吉。
虞翻曰：震为主，四行之正成明夷，则三体震为夷主，故遇其夷主，吉。
案：四处上卦之下，以阳居阴，履非其位而比于五，故曰遇也。夷者，伤也；主者，五也。谓四不期相遇而能上行伤五则吉，故曰遇其夷主吉行也。

《象》曰：丰其蔀，位不当也。日中见斗，幽不明也。
虞翻曰：离上变入坎云下，故幽不明，坎幽也。

遇其夷主吉，行也。
虞翻曰：动体明夷，震为行，故曰吉行。

六五：来章，有庆誉，吉。
虞翻曰：在内称来，章，显也。庆谓五阳出称庆也，誉谓二，二多誉。五发得正，则来应二，故来章有庆誉吉也。

《象》曰：六五之吉，有庆也。
虞翻曰：动而成乾，乾为庆。

上六：丰其屋，蔀其家。
虞翻曰：丰，大；蔀，小也。三至上体大壮，屋象，故丰其屋。谓四五已变，上动成家人，大屋见则家人怀，故蔀其家。与泰二同义，故《象》曰“天降祥”。明以大壮为屋象故也。

窥其户，阒其无人，三岁不觌，凶。
虞翻曰：谓从外窥三，应阒空也。四动时坤为阖户，阖故窥其户；坤为空虚，三隐伏坎中，故阒其无人。《象》曰自藏也。四五易位，噬嗑离目为窥，窥人者言皆不见。坎为三岁，坤冥在上，离象不见，故三岁不觌凶。
干宝曰：在丰之家，居乾之位，乾为屋宇，故曰丰其屋。此盖记纣之侈，造为璿室玉台也。蔀其家者，以记纣多倾国之女也。社稷既亡，宫室虚旷，故曰窥其户，阒其无人。阒，无人貌也。三者，天地人之数也。凡国于天地有兴亡焉，故王者之亡其家也，必天示其祥、地出其妖、人反其常；非斯三者亦弗之亡也。故曰三岁不觌凶。然则璿室之成，三年而后亡国矣。
案：上应于三，三互离巽为户，离为目，目而近户，窥之象也。既屋丰家蔀，若窥地户，阒寂无人。震木数三，故三岁致凶于灾。

《象》曰：丰其屋，天降祥也。
孟喜曰：天降下恶祥也。

窥其户，阒其无人，自藏也。
虞翻曰：谓三隐伏坎中，故自藏者也。

《序卦》曰：穷大者必失其居，故受之以旅。
崔憬曰：谚云“作者不居”，况穷大甚而能久处乎？故必获罪去邦，羁旅于外矣。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十一·旅、巽两卦集解注文", "底本：景印文渊阁《四库全书》本《周易集解》卷十一，维基文库数字转录第422—596行。录《旅》《巽》两卦经传及虞翻、姚信、荀爽、蜀才、王弼、侯果、陆绩、九家易、宋衷、翟玄、干宝等注文、案语和《序卦》衔接文字。数字转录仍待影印页逐字校勘。", """底本：景印文渊阁《四库全书》本《周易集解》卷十一，维基文库数字转录第422—596行。以下录《旅》《巽》两卦及前后《序卦》注文，保留虞翻、姚信、荀爽、蜀才、王弼、侯果、陆绩、九家易、宋衷、翟玄、干宝等注家文字与案语。异体、疑字和页面标点尚待影印本逐字复核。

《序卦》曰：穷大者必失其居，故受之以旅。
崔憬曰：谚云“作者不居”，况穷大甚而能久处乎？故必获罪去邦，羁旅于外矣。

䷷〈艮下离上〉旅：小亨，旅贞吉。
虞翻曰：贲初之四，否三之五，非乾坤往来也，与噬嗑之丰同义。小谓柔得贵位而顺刚，丽乎大明，故旅小亨，旅贞吉。再言旅者，谓四凶恶，进退无恒，无所容处，故再言旅，恶而愍之。

《彖》曰：旅，小亨。
姚信曰：此本否卦，三五交易，去其本体，故曰客旅。
荀爽曰：谓阴升居五，与阳通者也。

柔得中乎外，而顺乎刚；止而丽乎明，是以小亨，旅贞吉也。
蜀才曰：否三升五，柔得中于外，上顺于刚；九五降三，降不失正。止而丽乎明，所以小亨，旅贞吉也。

旅之时义大矣哉。
虞翻曰：以离日丽天，县象著明，莫大日月，故义大也。
王弼曰：旅者，物失其所居之时也。物失所居，则咸愿有附，岂非智者有为之时？故曰旅之时义大矣哉。

《象》曰：山上有火，旅。
侯果曰：火在山上，势非长久，旅之象也。

君子以明慎用刑，而不留狱。
虞翻曰：君子谓三，离为明，艮为慎，兑为刑，坎为狱。贲初之四，狱象不见，故以明慎用刑而不留狱，与丰折狱同义者也。

初六：旅琐琐，斯其所取灾。
陆绩曰：琐琐，小也。艮为小石，故曰旅琐琐也。履非其正，应离之始；离为火，艮为山，以应火灾，焚自取也，故曰斯其所取灾也。

《象》曰：旅琐琐，志穷灾也。
虞翻曰：琐琐，最蔽之貌也。失位远应之正，介坎；坎为灾眚，艮手为取，谓三动应坎，坎为志，坤称穷，故曰志穷灾也。

六二：旅即次，怀其资，得僮仆，贞。
九家易曰：即，就也；次，舍也；资，财也。以阴居二，即就其舍，故旅即次。承阳有实，故怀其资也。初者卑贱，二得履之，故得僮仆。处和得位正居，是故曰得僮仆贞矣。

《象》曰：得僮仆贞，终无尤也。
虞翻曰：艮为僮仆，得正承三，故得僮仆贞而终无尤也。
案：六二履正，体艮；艮为阍寺，僮仆贞之象也。

九三：旅焚其次，丧其僮仆，贞厉。
虞翻曰：离为火，艮为僮仆。三动艮坏，故焚其次。坤为丧，三动艮灭入坤，故丧其僮仆。动而失正，故贞厉矣。

《象》曰：旅焚其次，亦以伤矣。
虞翻曰：三动体剥，故伤也。

以旅与下，其义丧也。
虞翻曰：三变成坤，坤为下、为丧，故其义丧也。

九四：旅于处，得其资斧，我心不快。
虞翻曰：巽为处，四焚弃恶人，失位远应，故旅于处，言无所从也。离为资斧，故得其资斧。三动，四坎为心，其位未至，故我心不快也。

《象》曰：旅于处，未得位也。得其资斧，心未快也。
王弼曰：斧所以斫除荆棘，以安其舍者也。虽处上体之下，不先于物，然而不得其位，不获平坦之地者也。客子所处，不得其次，而得其资斧之地，故其心不快。
案：九四失位而居艮上，艮为山，山非平坦之地也。四体兑巽，巽为木，兑为金，木贯于金，即资斧斫除荆棘之象者也。

六五：射雉，一矢亡。
虞翻曰：三变坎为弓，离为矢，故射雉。五变乾，体矢动，雉飞，雉象不见，故一矢亡矣。

终以誉命。
虞翻曰：誉谓二，巽为命。五终变成乾，则二来应己，故终以誉命。

《象》曰：终以誉命，上逮也。
虞翻曰：逮，及也，谓二上及也。
干宝曰：离为雉、为矢，巽为木、为进退，艮为手，兑为决，有木在手，进退其体，矢决于外，射之象也。一阴升乾，故曰一矢。履非其位，下又无应，虽复射雉，终亦失之，故曰一矢亡也。一矢亡者，喻有损而小也。此记禄父为王者后，虽小叛扰，终逮安周室，故曰终以誉命矣。

上九：鸟焚其巢，旅人先笑后号咷。
虞翻曰：离为鸟、为火，巽为木、为高，四失位变震为筐，巢之象也。今巢象不见，故鸟焚其巢。震为笑，震在前，故先笑；应在巽，巽为号咷，巽象在后，故后号咷。

丧牛于易，凶。
虞翻曰：谓三动时坤为牛，五动成乾，乾为易。上失三，五动应二，故丧牛于易，失位无应故凶也。五动成遁，《大畜》“六二，执之用黄牛之革”，则旅家所丧牛也。

《象》曰：以旅在上，其义焚也。
虞翻曰：离火焚巢，故其义焚也。

丧牛于易，终莫之闻也。
虞翻曰：坎耳入兑，故终莫之闻。
侯果曰：离为鸟、为火，巽为木、为风，鸟居木上，巢之象也。旅而赡资，物之所恶也。丧牛甚易，求之也难，虽有智者，莫之吉也。

《序卦》曰：旅而无所容，故受之以巽。巽者，入也。
崔憬曰：旅寄于外而无所容，则必入矣，故曰旅无所容，受之以巽。

䷸〈巽下巽上〉巽：小亨，利有攸往，利见大人。
虞翻曰：遁二之四，柔得位而顺五刚，故小亨也。大人谓五，离目为见。二失位，利在往应五，故利有攸往，利见大人矣。

《彖》曰：重巽以申命。
陆绩曰：巽为命令，重命令者，欲丁宁也。

刚巽乎中正而志行。
陆绩曰：二得中，五得正，体两巽，故曰刚巽乎中正也，皆据阴，故志行也。
虞翻曰：刚中正谓五也。二失位，动成坎，坎为志，终变成震，震为行也。

柔皆顺乎刚，是以小亨。
陆绩曰：阴为卦主，故小亨。

利有攸往，利见大人。
案：其义已见繇辞。

《象》曰：随风巽，君子以申命行事。
虞翻曰：君子谓遁乾也。巽为命，重象故申命；变至三，坤为事，震为行，故行事也。
荀爽曰：巽为号令，两巽相随，故申命也。法教百端，令行为上，贵其必从，故曰行事也。

初六：进退，利武人之贞。
虞翻曰：巽为进退，乾为武人。初失位，利之正为乾，故利武人之贞矣。

《象》曰：进退，志疑也。
荀爽曰：风性动进退，欲承五，为二所据，故志以疑也。

利武人之贞，志治也。
虞翻曰：动而成乾，乾为大明，故志治。乾元用九，天下治，是其义也。

九二：巽在床下。
宋衷曰：巽为木，二阳在上，初阴在下，床之象也。二无应于上，退而据初，心在于下，故曰巽在床下也。
荀爽曰：床下以喻近也。二者军帅，三者号令，故言床下，以明将之所专不过军中事也。

用史巫纷若，吉，无咎。
荀爽曰：史以书勋，巫以告庙，纷，变若顺也。谓二以阳应阳，君所不臣，军帅之象。征伐既毕，书勋告庙，当变而顺五，则吉，故曰用史巫纷若吉无咎矣。

《象》曰：纷若之吉，得中也。
荀爽曰：谓二以处中和，故能变。

九三：频巽，吝。
虞翻曰：频，颦也。谓二已变，三体坎艮；坎为忧，艮为鼻，故频巽。无应在险，故吝也。

《象》曰：频巽之吝，志穷也。
荀爽曰：乘阳无据，为阴所乘，号令不行，故志穷也。

六四：悔亡，田获三品。
虞翻曰：田谓二也。地中称田，失位无应悔也。欲二之初，已得应之，故悔亡。二动得正，处中应五，五多功，故《象》曰有功也。二动艮为手，故称获；谓艮为狼、坎为豕，艮二之初，离为雉，故获三品矣。
翟玄曰：田获三品，下三爻也。谓初巽为鸡，二兑为羊，三离雉也。
案：《谷梁传》曰：春猎曰田，夏曰苗，秋曰蒐，冬曰狩。田获三品，一为干豆，二为宾客，三为充君之庖。注云：上杀中心，干之为豆实；次杀中髀骼以供宾客；下杀中腹，充君之庖厨，尊神敬客之义也。

《象》曰：田获三品，有功也。
王弼曰：得位承五，而依尊履正，以斯行命，必能获强暴远不仁者也。获而有益，莫若三品，故曰有功也。

九五：贞吉，悔亡，无不利。无初有终。
虞翻曰：得位处中，故贞吉悔亡无不利也。震巽相薄，雷风无形，当变之震矣。巽究为躁卦，故无初有终也。

先庚三日，后庚三日，吉。
虞翻曰：震庚也。谓变初至二成离，至三成震，震主庚，离为日，震三爻在前，故先庚三日，谓益时也。动四至五成离，终上成震，震爻在后，故后庚三日也。巽初失正，终变成震得位，故无初有终吉。震究为蕃鲜，白谓巽也；巽究为躁卦，躁卦谓震也。与蛊“先甲三日，后甲三日”同义。五动成蛊，乾成于甲，震成于庚，阴阳天地之始终，故经举甲庚于蛊、巽五也。

《象》曰：九五之吉，位正中也。
虞翻曰：居中得正，故吉矣。

上九：巽在床下。
虞翻曰：床下为初也。穷上反下成震，故巽在床下。
《象》曰：上穷也。
明当变穷上而复初也。
九家易曰：上为宗庙，礼封赏出军皆先告庙，然后受行三军之命，将之所专，故曰巽在床下也。

丧其资斧，贞凶。
虞翻曰：变至三时离毁入坤，坤为丧，离为斧，故丧其资斧。三变失位，故贞凶。
荀爽曰：军罢师旋，亦告于庙，还斧于君，故丧资斧。正如其故，不执臣节则凶，故曰丧其资斧贞凶。

《象》曰：巽在床下，上穷也。
虞翻曰：阳穷上反下，故曰上穷也。

丧其资斧，正乎凶也。
虞翻曰：上应于三，三动失正，故曰正乎凶也。

《序卦》曰：入而后说之，故受之以兑。兑者，说也。
崔憬曰：巽以申命行事，入于刑者也。入刑而后说之，所谓人忘其劳死也。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十一·兑卦集解注文（卷末）", "底本：景印文渊阁《四库全书》本《周易集解》卷十一，维基文库数字转录第594—649行。录《兑》卦经传及虞翻、崔憬注文和案语，至卷末。与此前卷十一《渐》《归妹》《丰》《旅》《巽》各批注文合读，覆盖该数字页面正文；仍须影印本逐字校勘。", """底本：景印文渊阁《四库全书》本《周易集解》卷十一，维基文库数字转录第594—649行。以下录《兑》卦及卷末文字，保留虞翻、崔憬、案语等来源标记与原有次序；异体字及疑字尚待影印页逐字复核。

《序卦》曰：入而后说之，故受之以兑。兑者，说也。
崔憬曰：巽以申命行事，入于刑者也。入刑而后说之，所谓人忘其劳死也。

䷹〈兑下兑上〉兑：亨，利贞。
虞翻曰：大壮五之三也。刚中而柔外，二失正，动应五，承三，故亨利贞也。

《彖》曰：兑，说也。
虞翻曰：兑口，故说也。

刚中而柔外，说以利贞。
虞翻曰：刚中谓二、五，柔外谓三、上也。二三四利之正，故说以利贞也。

是以顺乎天而应乎人。
虞翻曰：大壮乾为天，谓五也；人谓三矣。二变顺五承三，故顺乎天、应乎人。坤为顺也。

说以先民，民忘其劳。
虞翻曰：谓二四已变成屯，故为劳。震喜、兑说，坤为民，坎为心，民心喜说，有顺比象，故忘其劳也。

说以犯难，民忘其死。
虞翻曰：体屯故难也。三至上体大过死，变成屯，民说无疆，故民忘其死。坎心为忘，或以坤为死也。

说之大，民劝矣哉。
虞翻曰：体比顺象，故劳而不怨；震为喜笑，故人劝也。

《象》曰：丽泽，兑。君子以朋友讲习。
虞翻曰：君子，大壮乾也。阳息见兑，学以聚之，问以辩之。兑二阳同类为朋，伏艮为友，坎为习，震为讲，兑两口对，故朋友讲习也。

初九：和兑，吉。
虞翻曰：得位，四变应己，故和兑吉矣。

《象》曰：和兑之吉，行未疑也。
虞翻曰：四变应初，震为行，坎为疑，故行未疑。

九二：孚兑，吉，悔亡。
虞翻曰：孚谓五也。四已变，五在坎中称孚。二动得位应之，故孚兑吉悔亡矣。

《象》曰：孚兑之吉，信志也。
虞翻曰：二变应五，谓四已变，坎为志，故信志也。

六三：来兑，凶。
虞翻曰：从大壮来，失位，故来兑凶矣。

《象》曰：来兑之凶，位不当也。
案：以阴居阳，故位不当；谄邪求悦，所以必凶。

九四：商兑未宁，介疾有喜。
虞翻曰：巽为近利市三倍，故称商兑。变之坎，水性流；震为行，谓二已变，体比象，故未宁，与比“不宁方来”同义也。坎为疾，故介疾。得位承五，故有喜。

《象》曰：九四之喜，有庆也。
虞翻曰：阳为庆，谓五也。

九五：孚于剥，有厉。
虞翻曰：孚谓五也。二四变，体剥象，故孚于剥。在坎未光，有厉也。

《象》曰：孚于剥，位正当也。
案：以阳居尊位，应二比四，孚剥有厉，位正当也。

上六：引兑。
虞翻曰：无应，乘阳，动而之巽为绳，艮为手，应在三，三未之正，故引兑也。

《象》曰：上六引兑，未光也。
虞翻曰：二四已变而体屯，上三未为离，故未光也。

《周易集解》卷十一终。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十二·涣至未济六卦经传", "底本：维基文库《周易集解（四库全书本）/卷12》，并以中国哲学书电子化计划《周易集解卷十二》对读。此条录六卦卦爻辞及《彖》《象》传，不含诸家注文全文；尚未逐字回看文渊阁影印页。来源：https://wk.100ke.info/zh-hans/周易集解_(四库全书本)/卷12；对读：https://ctext.org/wiki.pl?chapter=527113&if=en&remap=gb", """卷十二范围：涣、节、中孚、小过、既济、未济。以下录经文与《彖》《象》传，注文另行整理。

《涣》
涣：亨。王假有庙，利涉大川，利贞。
《彖》曰：涣亨，刚来而不穷，柔得位乎外而上同。王假有庙，王乃在中也。利涉大川，乘木有功也。
《象》曰：风行水上，涣。先王以享于帝，立庙。
初六：用拯马壮，吉。《象》曰：初六之吉，顺也。
九二：涣奔其机，悔亡。《象》曰：涣奔其机，得愿也。
六三：涣其躬，无悔。《象》曰：涣其躬，志在外也。
六四：涣其群，元吉。涣有丘，匪夷所思。《象》曰：涣其群元吉，光大也。
九五：涣汗其大号，涣王居，无咎。《象》曰：王居无咎，正位也。
上九：涣其血，去逖出，无咎。《象》曰：涣其血，远害也。

《节》
节：亨。苦节不可贞。
《彖》曰：节亨，刚柔分而刚得中。苦节不可贞，其道穷也。说以行险，当位以节，中正以通。天地节而四时成。节以制度，不伤财，不害民。
《象》曰：泽上有水，节。君子以制数度，议德行。
初九：不出户庭，无咎。《象》曰：不出户庭，知通塞也。
九二：不出门庭，凶。《象》曰：不出门庭凶，失时极也。
六三：不节若，则嗟若，无咎。《象》曰：不节之嗟，又谁咎也。
六四：安节，亨。《象》曰：安节之亨，承上道也。
九五：甘节，吉。往有尚。《象》曰：甘节之吉，居位中也。
上六：苦节，贞凶，悔亡。《象》曰：苦节贞凶，其道穷也。

《中孚》
中孚：豚鱼吉，利涉大川，利贞。
《彖》曰：中孚，柔在内而刚得中，说而巽孚，乃化邦也。豚鱼吉，信及豚鱼也。利涉大川，乘木舟虚也。中孚以利贞，乃应乎天也。
《象》曰：泽上有风，中孚。君子以议狱缓死。
初九：虞吉，有它不燕。《象》曰：初九虞吉，志未变也。
九二：鸣鹤在阴，其子和之。我有好爵，吾与尔靡之。《象》曰：其子和之，中心愿也。
六三：得敌，或鼓或罢，或泣或歌。《象》曰：或鼓或罢，位不当也。
六四：月几望，马匹亡，无咎。《象》曰：马匹亡，绝类上也。
九五：有孚挛如，无咎。《象》曰：有孚挛如，位正当也。
上九：翰音登于天，贞凶。《象》曰：翰音登于天，何可长也。

《小过》
小过：亨，利贞。可小事，不可大事。飞鸟遗之音，不宜上，宜下，大吉。
《彖》曰：小过，小者过而亨也。过以利贞，与时行也。柔得中，是以小事吉也。刚失位而不中，是以不可大事也。有飞鸟之象焉，飞鸟遗之音，不宜上，宜下，大吉，上逆而下顺也。
《象》曰：山上有雷，小过。君子以行过乎恭，丧过乎哀，用过乎俭。
初六：飞鸟以凶。《象》曰：飞鸟以凶，不可如何也。
六二：过其祖，遇其妣；不及其君，遇其臣，无咎。《象》曰：不及其君，臣不可过也。
九三：弗过防之，从或戕之，凶。《象》曰：从或戕之，凶如何也。
九四：无咎，弗过遇之；往厉必戒，勿用永贞。《象》曰：弗过遇之，位不当也。往厉必戒，终不可长也。
六五：密云不雨，自我西郊。公弋取彼在穴。《象》曰：密云不雨，已上也。
上六：弗遇过之，飞鸟离之，凶，是谓灾眚。《象》曰：弗遇过之，已亢也。

《既济》
既济：亨小，利贞。初吉，终乱。
《彖》曰：既济亨，小者亨也。利贞，刚柔正而位当也。初吉，柔得中也。终止则乱，其道穷也。
《象》曰：水在火上，既济。君子以思患而豫防之。
初九：曳其轮，濡其尾，无咎。《象》曰：曳其轮，义无咎也。
六二：妇丧其茀，勿逐，七日得。《象》曰：七日得，以中道也。
九三：高宗伐鬼方，三年克之，小人勿用。《象》曰：三年克之，惫也。
六四：繻有衣袽，终日戒。《象》曰：终日戒，有所疑也。
九五：东邻杀牛，不如西邻之禴祭，实受其福。《象》曰：东邻杀牛，不如西邻之时也。实受其福，吉大来也。
上六：濡其首，厉。《象》曰：濡其首厉，何可久也。

《未济》
未济：亨。小狐汔济，濡其尾，无攸利。
《彖》曰：未济亨，柔得中也。小狐汔济，未出中也。濡其尾，无攸利，不续终也。虽不当位，刚柔应也。
《象》曰：火在水上，未济。君子以慎辨物居方。
初六：濡其尾，吝。《象》曰：濡其尾，亦不知极也。
九二：曳其轮，贞吉。《象》曰：九二贞吉，中以行正也。
六三：未济，征凶，利涉大川。《象》曰：未济征凶，位不当也。
九四：贞吉，悔亡。震用伐鬼方，三年有赏于大国。《象》曰：贞吉悔亡，志行也。
六五：贞吉，无悔。君子之光，有孚，吉。《象》曰：君子之光，其晖吉也。
上九：有孚于饮酒，无咎。濡其首，有孚失是。《象》曰：饮酒濡首，亦不知节也。

译文状态：暂无完整逐段译文。注文状态：本条仅录经文及彖象传，卷内虞翻、荀爽、王弼、干宝、侯果等诸家注文尚未逐条录入。校勘状态：以四库本数字页面为底本并参考CTP对读，未完成影印本逐字核验；页面异文和疑似转录错误须继续逐项标记，不静默改字。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十二·涣、节两卦集解注文", "底本：景印文渊阁《四库全书》本《周易集解》卷十二，维基文库数字转录第119—261行。录《涣》《节》两卦经传及虞翻、崔憬、卢氏、荀爽、王弼、侯果、九家易、干宝等注文和案语；与卷十二六卦经传条目分列。数字转录的异体、图像字和疑误仍待影印页复核。", """底本：景印文渊阁《四库全书》本《周易集解》卷十二，维基文库数字转录第119—261行。以下录《涣》《节》两卦经传及页面所见注文，保留虞翻、崔憬、卢氏、荀爽、王弼、侯果、九家易、干宝等注家标识。数字文本中的异体字、图像字和疑似讹误仍须对照影印页核验。

《序卦》曰：说而后散之，故受之以涣。涣者，离也。
崔憬曰：人说忘其劳，死而后可散之征役，离之以家邦，故曰说而后散之，故受之涣。涣者，离也。

䷺〈坎下巽上〉涣：亨。
虞翻曰：否四之二成坎震，天地交，故亨也。

王假有庙。
虞翻曰：乾为王，假，至也。否体观，艮为宗庙，乾四之坤二，故王假有庙，王乃在中也。

利涉大川，利贞。
虞翻曰：坎为大川，涣舟楫象，故涉大川，乘木有功。二失正，变应五，故利居贞也。

《彖》曰：涣亨，刚来而不穷，柔得位乎外而上同。
卢氏曰：此本否卦。乾之九四来居坤中，刚来成坎，水流而不穷也。坤之六二上升乾四，柔得位乎外，上承贵王，与上同也。

王假有庙，王乃在中也。
荀爽曰：谓阳来居二，在坤之中，为立庙。假，大也。言受命之王居五大位，上体之中，上享天帝，下立宗庙也。

利涉大川，乘木有功也。
虞翻曰：巽为木，坎为水，故乘木有功也。

《象》曰：风行水上，涣。先王以享于帝，立庙。
虞翻曰：谓受命之王收集散民，上享天帝，下立宗庙也。阴上至四承五为享帝，阳下至二为立庙也。离日上为宗庙，而谓天帝宗庙之神所配食者，王者所奉，故继于上。至于宗庙，其实在地；地者，阴中之阳，有似庙中之神。
虞翻曰：否乾为先王，享，祭也。震为帝、为祭，艮为庙。四之二，杀坤大牲，故以享帝立庙。谓成既济，有噬嗑食象故也。

初六：用拯马壮，吉。
虞翻曰：坎为马，初失正，动体大壮得位，故拯马壮吉，悔亡之矣。

《象》曰：初六之吉，顺也。
虞翻曰：承二，故顺也。

九二：涣奔其机，悔亡。
虞翻曰：震为奔，坎为棘、为矫揉，震为足。揉来有足，艮肱据之，凭机之象也。涣宗庙中，故设机。二失位，变得正，故涣奔其机悔亡也。

《象》曰：涣奔其机，得愿也。
虞翻曰：动而得位，故得愿也。

六三：涣其躬，无悔。
荀爽曰：体中曰躬，谓涣三使承上，为志在外，故无悔。

《象》曰：涣其躬，志在外也。
王弼曰：涣之为义，内险而外安者也。散躬志外，不固所守，与刚合志，故得无咎。

六四：涣其群，元吉。
虞翻曰：谓二已变成坤，坤三爻称群，得位顺五，故元吉也。

涣有丘，匪夷所思。
虞翻曰：位半艮山，故称丘。匪，非也。夷谓震。四应在初，三变坎为思，故匪夷所思也。
卢氏曰：自二居四，离其群侣，涣其群也。得位承尊，故元吉也。互体有艮，艮为山丘。涣群虽则光大，有丘则非平易，故有匪夷之思也。

《象》曰：涣其群元吉，光大也。
虞翻曰：谓三已变成离，故四光大也。

九五：涣汗其大号。
九家易曰：谓五建二为诸侯，使下君国，故宣布号令，百姓被泽，若汗之出身不还反也。此本否卦，体乾为首，来下处二成坎水，汗之象也。阳称大，故曰涣汗其大号也。

涣王居，无咎。
荀爽曰：布其德教，王居其所，故无咎矣。

《象》曰：王居无咎，正位也。
虞翻曰：五为王，艮为居，正位居五，四阴顺命，故王居无咎，正位也。

上九：涣其血，去逖出，无咎。
虞翻曰：应在三，坎为血、为逖，逖，忧也。二变为观，坎象不见，故其血去、逖出，无咎。

《象》曰：涣其血，远害也。
虞翻曰：乾为远，坤为害，体遁上，故远害也。

《序卦》曰：物不可以终离，故受之以节。
崔憬曰：离散之道不可终行，当宜节止之，故言物不可以终离，受之以节。

䷻〈兑下坎上〉节：亨。
虞翻曰：泰三之五，天地交也。五当位以节，中正以通，故节亨也。

苦节不可贞。
虞翻曰：谓上也。应在三，三变成离，火炎上作苦；位在火上，故苦节。虽得位，乘阳，故不可贞。

《彖》曰：节亨，刚柔分而刚得中。
卢氏曰：此本泰卦，分乾九三升坤五，分坤六五下处乾三，是刚柔分而刚得中也。

苦节不可贞，其道穷也。
虞翻曰：位极于上，乘阳，故穷也。

说以行险。
虞翻曰：兑说、坎险，震为行，故说以行险也。

当位以节，中正以通。
虞翻曰：中正谓五，坎为通也。

天地节而四时成。
虞翻曰：泰乾天、坤地，震春、兑秋、坎冬，三动离为夏，故天地节而四时成也。

节以制度，不伤财，不害民。
虞翻曰：艮手称制，坤数十为度；坤又为害、为民、为财。二动体剥，剥为伤；三出复位成既济定，坤剥不见，故节以制度，不伤财，不害民。

《象》曰：泽上有水，节。
侯果曰：泽上有水，以堤防为节。

君子以制数度，议德行。
虞翻曰：君子，泰乾也。艮止为制，坤为度，震为议、为行，乾为德，故以制数度、议德行。乾三之五为制数度，坤五之乾为议德行也。

初九：不出户庭，无咎。
虞翻曰：泰坤为户，艮为庭，震为出。初得位应四，故不出户庭，无咎矣。

《象》曰：不出户庭，知通塞也。
虞翻曰：坎为通，二变坤土壅初为塞。
崔憬曰：为节之始，有应于四，四为坎险不通之象。以节崇塞，虽不通，可谓知通塞矣。户庭，室庭也。慎密守节，故不出焉而无咎也。
案：初九应四，四互坎艮，艮为门阙；四居艮中，是为内户，户庭之象也。

九二：不出门庭，凶。
虞翻曰：变而之坤，艮为门庭。二失位，不变出门应五则凶，故言不出门庭凶矣。

《象》曰：不出门庭凶，失时极也。
虞翻曰：极，中也。未变之正，失时极矣。

六三：不节若，则嗟若，无咎。
虞翻曰：三节家君子也。失位，故节若嗟，哀号声。震为音声，为出；三动得正而体离坎，涕流出目，故则嗟若。得位乘二，故无咎也。

《象》曰：不节之嗟，又谁咎也。
王弼曰：若，辞也。以阴处阳，以柔乘刚，违节之道以至哀嗟，自己所致，无所怨咎，故曰又谁咎矣。

六四：安节，亨。
虞翻曰：二已变，艮止；坤安，得正承五，有应于初，故安节亨。

《象》曰：安节之亨，承上道也。
九家易曰：言四得正，奉五上通于君，故曰承上道也。

九五：甘节，吉，往有尚。
虞翻曰：得正居中，坎为美，故甘节吉。往谓二，二失正，变往应五，故往有尚也。

《象》曰：甘节之吉，居位中也。
虞翻曰：艮为居，五为中，故居位中也。

上六：苦节，贞凶，悔亡。
虞翻曰：二三变，在两离，火炎上作苦，故苦节。乘阳故贞凶，得位故悔亡。
干宝曰：《彖》称“苦节不可贞”，在此爻也。禀险伏之教，怀贪狼之志，以苦节之性而遇甘节之主，必受其诛。华士、少正卯之爻也，故曰贞凶。苦节既凶，甘节志得，故曰悔亡。

《象》曰：苦节贞凶，其道穷也。
荀爽曰：乘阳于上，无应于下，故其道穷也。

《序卦》曰：节而信之，故受之以中孚。
崔憬曰：节以制度，不伤财、不害民，则人信之，故言节而信之，故受之中孚也。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十二·中孚卦集解注文", " 底本：景印文渊阁《四库全书》本《周易集解》卷十二，维基文库数字转录第264—340行。录《中孚》卦经传及虞翻、王肃、荀爽、崔憬、侯果、韩康伯等注文。底本数字转录含一处图像字及疑似脱讹，无法可靠辨识处不擅自补字；异体和标点待影印页复核。 数字转录第264—340行；疑似图像字不补造，待影印本校勘。", """底本：景印文渊阁《四库全书》本《周易集解》卷十二，维基文库数字转录第264—340行。录《中孚》卦经传及虞翻、王肃、荀爽、崔憬、侯果、韩康伯等注文。底本数字转录含一处图像字及疑似脱讹，无法可靠辨识处不擅自补字；异体和标点待影印页复核。

《序卦》曰：节而信之，故受之以中孚。
崔憬曰：节以制度，不伤财，不害民，则人信之，故言节而信之，故受之中孚也。

䷼〈兑下巽上〉中孚。
虞翻曰：讼四之初也。坎孚象在中，谓二也，故称中孚。此当从四阳二阴之例，遁阴未及三而大壮阳已至四，故从讼来。二在讼时，体离为鹤，在坎阴中，故有鸣鹤在阴之义也。

豚鱼吉。
案：坎为豕，讼四降初，折坎称豚；初阴升四，体巽为鱼。中二孚信也，谓二变应五，化坤成邦，故信及豚鱼吉矣。
虞氏以三至上体遁，便以豚鱼为遁鱼，虽生曲象之异见，乃失化邦之中信也。

利涉大川。
虞翻曰：坎为大川，谓二已化邦，三利出涉坎，得正体涣。涣舟楫象，故利涉大川，乘木舟虚也。

利贞。
虞翻曰：谓二利之正而应五也。中孚以利贞，乃应于天也。

《彖》曰：中孚，柔在内而刚得中，说而巽，孚。
王肃曰：三、四在内，二、五得中，兑说而巽顺，故孚也。

乃化邦也。
虞翻曰：二化应五成坤，坤为邦，故化邦也。

豚鱼吉，信及豚鱼也。
虞翻曰：豚鱼谓四、三也。艮为山，陆豚所处；三为兑泽，鱼所在。豚者卑贱，鱼者幽隐，中信之道皆及之矣。

利涉大川，乘木舟虚也。
王肃曰：中孚之象，外实内虚，有似可乘虚木之舟也。

中孚以利贞，乃应乎天也。
虞翻曰：讼乾为天，二动应乾，故乃应乎天也。

《象》曰：泽上有风，中孚。
崔憬曰：流风令于上，布泽惠于下，中孚之象也。

君子以议狱缓死。
虞翻曰：君子谓乾也。讼坎为狱，震为议、为缓，坤为死。乾四之初，则二出坎狱；兑说、震喜，坎狱不见，故议狱缓死也。

初九：虞吉，有它不燕。
荀爽曰：震，宴也。初应于四，宜自安虞；无意于四则吉，故曰虞吉也。四者乘五，有它意于四则不安，故曰有它不燕也。

《象》曰：初九虞吉，志未变也。
荀爽曰：初位潜藏，未得变而应四也。

九二：鸣鹤在阴，其子和之。我有好爵，吾与尔靡之。
虞翻曰：靡，共也。震为鸣，讼离为鹤，坎为阴夜，鹤知夜半，故鸣鹤在阴。二动成坤，体益互艮为子；震、巽同声者相应，故其子和之。坤为身，故称我；吾谓五也。离为爵，爵位也。坤为邦国，五在艮阍寺庭阙之象，故称好爵。五利二变之正应己，故吾与尔靡之矣。

《象》曰：其子和之，中心愿也。
虞翻曰：坎为心，动得正应五，故中心愿也。

六三：得敌，或鼓或罢，或泣或歌。
荀爽曰：三、四俱阴，故称得。四得位有位，故鼓而歌；三失位无实，故罢而泣之也。

《象》曰：或鼓或罢，位不当也。
王弼曰：三、四俱阴，金水巽性敌之谓也。以阴居阳，自强而进，进而碍敌，故或鼓也。四履正位，非三敌所克，故或罢也。不胜而退，惧见侵陵，故或泣也。四履谦巽，不报雠敌，故或歌也。歌泣无恒，位不当也。

六四：月几望，马匹亡，无咎。
虞翻曰：讼坎为月，离为日，兑西、震东，月在兑二，离在震三，日月象对，故月几望。乾坎两马匹，初四易位，震为奔走；体遁山中，乾坎不见，故马匹亡。初四易位，故无咎矣。

《象》曰：马匹亡，绝类上也。
虞翻曰：讼初之四，体与上绝，故绝类上也。

九五：有孚挛如，无咎。
虞翻曰：孚，信也，谓二在坎为孚。巽绳、艮手，故挛。二使化为邦，得正应己，故无咎也。

《象》曰：有孚挛如，位正当也。
案：以阳居五，有信挛二使变己，是位正当也。

上九：翰音登于天，贞凶。
虞翻曰：巽为鸡，应在震，震为音。翰，高也。巽为高，乾为天，故翰音登于天。失位，故贞凶。礼荐牲鸡称翰音也。

《象》曰：翰音登于天，何可长也。
侯果曰：穷上失位，信不由中，以此申命，有声无实。中实内丧，虚华外扬，是翰音登天也。巽为鸡，鸡曰翰音；虚音登天，何可久也。

《序卦》曰：有其信者必行之，故受之以小过。
韩康伯曰：守其信者，则失贞而不谅之道，而以信为过也，故曰小过。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十二·小过卦集解注文", "底本：景印文渊阁《四库全书》本《周易集解》卷十二，维基文库数字转录第341—427行。录《小过》卦经传及虞翻、荀爽、宋衷、王肃、侯果、韩康伯等注文，至引向《既济》的《序卦》文字。数字转录有异体、疑字，待影印页复核。 数字转录第341—427行；待影印本逐字校勘。", """底本：景印文渊阁《四库全书》本《周易集解》卷十二，维基文库数字转录第341—427行。录《小过》卦经传及虞翻、荀爽、宋衷、王肃、侯果、韩康伯等注文，至引向《既济》的《序卦》文字。数字转录有异体、疑字，待影印页复核。

䷽〈艮下震上〉小过：亨，利贞。
虞翻曰：晋上之三，当从四阴二阳临观之例。临阳未至三，而观四已消也。又有飞鸟之象，故知从晋来。杵臼之利，盖取诸此。柔得中而应乾刚，故亨；五失正，故利贞。过以利贞，与时行也。

可小事。
虞翻曰：小谓五，晋坤为事，柔得中，故可小事也。

不可大事。
虞翻曰：大事，四刚失位而不中，故不可大事也。

飞鸟遗之音，不宜上，宜下，大吉。
虞翻曰：离为飞鸟，震为音。艮为止，晋上之三，离去震，在鸟飞而音止，故飞鸟遗之音。上阴乘阳，故不宜上；下阴顺阳，故宜下，大吉。俗说或以卦象二阳在内、四阴在外，有似飞鸟之象，妄矣。

《彖》曰：小过，小者过而亨也。过以利贞，与时行也。
荀爽曰：阴称小，谓四应初，过二而去三，应上过五而去五，处中见过不见应，故曰小者过而亨也。

柔得中，是以小事吉也。
虞翻曰：谓五也，阴称小，故小事吉也。

刚失位而不中，是以不可大事也。
虞翻曰：谓四也，阳称大，故不可大事也。

有飞鸟之象焉，飞鸟遗之音。
宋衷曰：二阳在内，上下各阴，有似飞鸟舒翮之象，故曰飞鸟。震为声音，飞而且鸣，鸟去而音止，故曰遗之音也。

不宜上，宜下，大吉，上逆而下顺也。
王肃曰：四、五失位，故曰上逆；二、三得正，故曰下顺也。

《象》曰：山上有雷，小过。
侯果曰：山大而雷小，山上有雷，小过于大，故曰小过。

君子以行过乎恭。
虞翻曰：君子谓三也。上贵三贱，晋上之三，震为行，故行过乎恭。谓三致恭以顺存其位，与谦三同义。

丧过乎哀。
虞翻曰：晋坤为丧，离为目，艮为鼻，坎为涕洟，震为出。涕洟出鼻目，体大过遭死丧，过乎哀也。

用过乎俭。
虞翻曰：坤为财，用为吝啬；艮为止，兑为小。小用止密，云不雨，故用过乎俭也。

初六：飞鸟以凶。
虞翻曰：应四，离为飞鸟；上之三，则四折入大过死，故飞鸟以凶。

《象》曰：飞鸟以凶，不可如何也。
虞翻曰：四死大过，故不可如何也。

六二：过其祖，遇其妣。
虞翻曰：祖谓祖母初也，母死称妣，谓三坤为丧、为母，折入大过死，故称祖也。妣，二过初；五变三体姤遇，故遇妣也。

不及其君，遇其臣，无咎。
虞翻曰：五动为君，晋坎为臣。二之五隔三，艮为止，故不及其君；止如承三，得正体姤遇象，故遇其臣无咎也。

《象》曰：不及其君，臣不可过也。
虞翻曰：体大过，下止舍巽下，故不可过，与随三同义。

九三：弗过防之，从或戕之，凶。
虞翻曰：防，防四也。失位，从或而欲折之。初，戕杀也。离为戈兵，三从离上入坤，折四死大过中，故从或戕之凶也。

《象》曰：从或戕之，凶如何也。
虞翻曰：三来戕四，故凶如何也。

九四：无咎，弗过遇之。
九家易曰：以阳居阴，行过乎恭。今虽失位，进则遇五，故无咎也。四体震动，位既不正，当动上居五，不复过五，故曰弗过遇之矣。

往厉必戒，勿用永贞。
荀爽曰：四往危五，戒备于三，故曰往厉必戒也。勿长居四，当动上五，故用永贞。

《象》曰：弗过遇之，位不当也。往厉必戒，终不可长也。
虞翻曰：体否上倾，故终不可长矣。

六五：密云不雨，自我西郊。
虞翻曰：密，小也。晋坎在天为云，坠地成雨。上来之三，折坎入兑，小为密；坤为自我，兑为西，五动乾为郊，故密云不雨，自我西郊也。

公弋取彼在穴。
虞翻曰：公谓三也。弋，矰缴射也。坎为弓弹，离为鸟矢；弋无矢也。巽绳连鸟，弋人鸟之象。艮为手，二为穴，手入穴中，故公弋取彼在穴也。

《象》曰：密云不雨，已上也。
虞翻曰：谓三坎水已之上六，故已上也。

上六：弗遇过之，飞鸟离之，凶，是谓灾眚。
虞翻曰：谓四已变之坤，上得之三，故弗遇过之。离为飞鸟，公弋得之，鸟下入艮手而死，故飞鸟离之凶。晋坎为灾眚，故是谓灾眚矣。

《象》曰：弗遇过之，已亢也。
虞翻曰：飞下称亢，晋上之三，故已亢也。

《序卦》曰：有过物者必济，故受之以既济。
韩康伯曰：行过乎恭，礼过乎俭，可以矫世厉俗，有所济也。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十三·系辞上传前段辑注", "底本：景印文渊阁四库全书本《周易集解》卷十三，维基文库卷十三页面。此次按页面连续录入卷首至“是故卦有小大，辞有险易，辞也者各指其所之”一段；包含经文及虞翻、荀爽、九家易、侯果、陆绩、韩康伯等注文。此条是卷十三分段录入，不代表全卷已完成；原书后续段落仍待续录。未添加未经核验的译文。", """周易集解卷十三
唐　李鼎祚撰

天尊地卑乾坤定矣
虞翻曰：天贵故尊，地贱故卑。定谓成列。荀爽曰：谓否卦也，否七月万物已成，乾坤各得其位定矣。

卑高以陈贵贱位矣
虞翻曰：干高贵五，坤卑贱二，列贵贱者存乎位也。荀爽曰：谓泰卦也。侯果曰：天地卑高义既陈矣，万物贵贱位宜差矣。

动静有常刚柔断矣
虞翻曰：断，分也。乾刚常动，坤柔常静，分阴分阳，迭用柔刚。

方以类聚
九家易曰：谓姤卦，阳爻聚于午也。方，道也，谓阳道施生，万物各聚其所也。

物以群分
九家易曰：谓复卦阴爻群于子也。阴主成物，故曰物。至于万物一成，分散天下也，以周人用，故曰物以群分也。

吉凶生矣
虞翻曰：物三称群，坤方道静，故以类聚；干物动行，故以群分。干生故吉，坤杀故凶，则吉凶生矣。

在天成象在地成形变化见矣
虞翻曰：谓日月在天成八卦。震象出庚，兑象见丁，干象盈甲，巽象伏辛，艮象消丙，坤象丧乙，坎象流戊，离象就已，故在天成象也。在地成形，震为竹，巽木，坎水，离火，艮山，兑泽，干金，坤土。在天为变，在地为化，刚柔相推而生变化矣。

是故刚柔相摩八卦相荡
虞翻曰：旋转称摩，薄也。干以二五摩坤成震坎艮，坤以二五摩干成巽离兑，故刚柔相摩则八卦相荡也。

鼓之以雷霆润之以风雨
虞翻曰：鼓动，润泽也。雷震，霆艮，风巽，雨兑也。

日月运行一寒一暑
虞翻曰：日离，月坎，寒干，暑坤也。运行往来，日月相推而明生焉，寒暑相推而岁成焉，故一寒一暑也。

乾道成男坤道成女
荀爽曰：男谓干初适坤为震，二适坤为坎，三适坤为艮，以成三男也。女谓坤初适干为巽，二适干为离，三适干为兑，以成三女也。

干知大始
九家易曰：始为干，禀元气，万物资始也。

坤作成物
荀爽曰：物谓坤任育体，万物资生。

干以易知坤以简能
虞翻曰：阳见称易，阴藏为简，简阅也。干息昭物，天下文明，故以易知；坤阅藏物，故以简能矣。

易则易知简则易从
虞翻曰：干悬象著明，故易知；坤阴阳动辟，故易从。不习无不利，地道光也。

易知则有亲易从则有功
虞翻曰：阳道成干为父，震坎艮为子，本乎天者亲上，故易知则有亲；以阳从阴至五多功，故易从则有功矣。蜀才曰：以其易知，故物亲而附之；以其易从，故物法而有功也。

有亲则可久有功则可大
荀爽曰：阴阳相亲杂而不厌，故可久也。万物生息，种类繁滋，故可大也。

可久则贤人之德可大则贤人之业
姚信曰：贤人乾坤也，言干以日新为德，坤以富有为业也。

易简而天下之理得矣
虞翻曰：易为干息，简为坤消，乾坤变通，穷理以尽性，故天下之理得矣。

天下之理得而成位乎其中矣
荀爽曰：阳位成于五，阴位成于二；五为上中，二为下中，故曰成位乎其中也。

圣人设卦
案：圣人谓伏羲也，始作八卦，重为六十四卦矣。

观象系辞焉
案：文王观六十四卦三百八十四爻之象，系属其辞。

而明吉凶
荀爽曰：因得明吉，因失明凶也。

刚柔相推而生变化
虞翻曰：刚推柔生变，柔推刚生化也。

是故吉凶者失得之象也
虞翻曰：吉则象得，凶则象失也。

悔吝者忧虞之象也
荀爽曰：忧虞小疵，故悔吝也。虞翻曰：悔则象忧，吝则象虞也。干宝曰：悔亡则虞，有小吝则忧。忧虞未至于失得，悔吝不入于吉凶；事有小大，故辞有急缓，各象其意也。

变化者进退之象也
荀爽曰：春夏为变，秋冬为化；息卦为进，消卦为退也。

刚柔者昼夜之象也
荀爽曰：刚谓干，柔谓坤；乾为昼，坤为夜，昼以喻君，夜以喻臣也。

六爻之动
陆绩曰：天有阴阳二气，地有刚柔二性，人有仁义二行，六爻之动法乎此也。

三极之道也
陆绩曰：此三才极至之道也。初四下极，二五中极，三上上极也。

是故君子所居而安者易之象也
虞翻曰：君子谓文王。象谓干二之坤成坎月离日，日月为象。君子黄中通理，正位居体，故居而安者易之象也。旧读象误作厚或作序，非也。

所变而玩者爻之辞也
虞翻曰：爻者言乎变者也，谓干五之坤，坤五动则观其变。旧作乐字误。

是故君子居则观其象而玩其辞
虞翻曰：玩，弄也。谓干五动成大有，以离之目观天之象，兑口玩习所系之辞，故玩其辞。

动则观其变而玩其占
虞翻曰：谓观爻动也，以动者尚其变，占事知来，故玩其占。

是以自天祐之吉无不利
虞翻曰：谓干五变之坤成大有，有天地日月之象。文王则庖牺，亦与天地合德，日月合明；天道助顺，人道助信，履信思顺，故自天祐之，吉无不利也。

彖者言乎象者也
虞翻曰：在天成象，八卦以象告，彖说三才，故言乎象也。

爻者言乎变者也
虞翻曰：爻有六画，所变而玩者爻之辞也，谓九六变化，故言乎变者也。

吉凶者言乎其失得也
虞翻曰：得正言吉，失位言凶也。

悔吝者言乎其小疵也
崔憬曰：系辞著悔吝之言，则异凶咎有其小病；比于凶咎，若疾病之与小疵。

无咎者善补过也
虞翻曰：失位为咎，悔变而之正，故善补过。孔子曰：退思补过者也。

是故列贵贱者存乎位
侯果曰：二五为功誉位，三四为凶惧位。凡爻得位则贵，失位则贱，故曰列贵贱者存乎位矣。

齐大小者存乎卦
王肃曰：齐犹正也，阳卦大，阴卦小，卦列则小大分，故曰齐小大者存乎卦也。

辩吉凶者存乎辞
韩康伯曰：辞，爻辞也。即爻者言乎变也。言象所以明小大，言变所以明吉凶，故大小之义存乎卦，吉凶之状存乎爻。至于悔吝无咎，其例一也。吉凶悔吝小疵，无咎皆生乎变，事有小大，故下历言五者之差也。

忧悔吝者存乎介
虞翻曰：介，纤也。介如石焉，断可识也，故存乎介，谓识小疵。

震无咎者存乎悔
虞翻曰：震，动也。有不善未尝不知之，知之未尝复行。无咎者善补过，故存乎悔也。

是故卦有小大辞有险易辞也者各指其所之
虞翻曰：阳易指天，阴险指地。圣人之情见乎辞，故指所之。

校勘说明：以上按维基文库所载四库本页面转录，原页面换行已合并为段落；个别异体字、通假字和页面录入字形仍须对照影印页复核。此处没有把现代标点视为四库原刻所有。""" ),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十四·大衍蓍策专题辑注", "底本：景印文渊阁四库全书本《周易集解》卷十四，维基文库卷十四页面。此次录入大衍蓍策、天地数、四营十八变、圣人之道四、蓍卦之德、太极两仪四象八卦及河图洛书等连续材料，含经文与诸家注文。页面卷末另有后续条文，本条按已录内容标注范围，不宣称全卷完整；未添加未经核验的译文。", """周易集解卷十四
唐　李鼎祚撰

大衍之数五十其用四十有九
干宝曰：衍，合也。崔憬曰：案《说卦》云“昔者圣人之作易也，幽赞于神明而生蓍，参天两地而倚数”，既言蓍数，则是说大衍之数也。明倚数之法，当参天两地。参天者，谓从三始，顺数而至五七九，不取于一也；两地者，谓从二起，逆数而至十八六，不取于四也。此因天地致上以配八卦而取其数也。艮为少阳，其数三；坎为中阳，其数五；震为长阳，其数七；干为老阳，其数九；兑为少阴，其数二；离为中阴，其数十；巽为长阴，其数八；坤为老阴，其数六。八卦之数总有五十，故云大衍之数五十也。不取天数一、地数四者，此数八卦之外，大衍所不管也。其用四十有九者，法长阳七七之数也。六十四卦既法长阴八八之数，故四十九蓍则法长阳七七之数焉。蓍圆而神，象天；卦方而智，象地，阴阳之别也。舍一不用者，以象太极虚而不用也。且天地各得其数以守其位，故太一亦为一数而守其位也。王辅嗣云：“演天地之数，所赖者五十，其用四十有九，其一不用也。不用而用以之通，非数而数以之成，即易之太极也。”四十有九数之极者，但言所赖五十，不释其所从来，则是亿度而言，非有实据。其一不用，将为法象太极，理纵可通，以为非数而成义则未允。案崔氏探玄，病诸先达，及乎自料未免小疵；既将八卦阴阳以配五十之数，余其天一地四无所禀承，而云八卦之外在衍之所不管者，斯乃谈何容易哉。且圣人之言连环可解，约文申义，须穷指归。即此章云天数五、地数五，五位相得而各有合，天数二十有五，地数三十，凡天地之数五十有五，此所以成变化而行鬼神，是结大衍之前义也。既云五位相得而各有合，即将五合之数配属五行也，故云大衍之数五十也。其用四十有九者，更减一以并五，备设六爻之位，蓍卦两兼，终极天地五十五之数也。

分而为二以象两
崔憬曰：四十九数合而未分，是象太极也。今分而为二，以象两仪矣。

挂一以象三
孔颖达曰：就两仪之中，分挂其一于最小指间，而配两仪以象三才。

揲之以四以象四时
崔憬曰：分揲其蓍，皆以四为数，一策一时，故四策以象四时也。

归奇于扐以象闰
虞翻曰：奇，所挂一策；扐，所揲之余。不一则二，不三则四也。取奇以归扐，扐并合挂左手之小指为一扐，则以闰月定四时成岁，故归奇于扐以象闰者也。

五岁再闰故再扐而后挂
虞翻曰：谓已一扐，复分挂如初揲之，归奇于初扐，并挂左手次小指间为再扐，则再闰也。又分扐揲之如初，而挂左手第三指间成一变，则布挂之一爻。谓已二扐，又加一为三，并重合前二扐为五岁，故五岁再闰，再扐而后挂。

天数五地数五
虞翻曰：天数五，谓一三五七九；地数五，谓二四六八十也。

五位相得而各有合
虞翻曰：五位谓五行之位。甲干乙坤相得合木，谓天地定位也；丙艮丁兑相得合火，山泽通气也；戊坎己离相得合土，水火相逮也；庚震辛巽相得合金，雷风相薄也；天壬地癸相得合水，言阴阳相薄而战于干，故五位相得而各有合。或以一六合水，二七合木，三八合火，四九合金，五十合土也。

天数二十有五
虞翻曰：一三五七九，故二十五也。

地数三十
虞翻曰：二四六八十，故三十也。

凡天地之数五十有五
虞翻曰：天二十五，地三十，故五十有五。天地数见于此，故大衍之数略其奇五而言五十也。

此所以成变化而行鬼神也
荀爽曰：在天为变，在地为化，在地为鬼，在天为神。姚信曰：此天地之数五十有五，分为爻者，故能成就乾坤之变化，能知鬼神之所为也。侯果曰：夫通变化、行鬼神，莫近于数。

干之策二百一十有六
荀爽曰：阳爻之策三十有六，干六爻皆阳，三六一百八十，六六三十六合二百一十有六也。

坤之策百四十有四
荀爽曰：阴爻之策二十有四，坤六爻皆阴，二六一百二十四六二十四合一百四十有四也，阴爻六合二十四气四六二百四十也。原网页此处数字及句读疑有讹误，照录页面可见字样，未据算术擅改。

凡三百有六十当期之日
陆绩曰：日月十二交会，积三百五十四日有奇为一会，今云三百六十当期，则入十三月六日也。十三月为一期，故云当期之日也。

二篇之策万有一千五百二十当万物之数也
侯果曰：二篇谓上下经也，共六十四卦，合三百八十四爻，阴阳各半，则阳爻一百九十二，每爻三十六策，合六千九百一十二策；阴爻亦一百九十二，每爻二十四策，合四千六百八策，则二篇之策合万一千五百二十，当万物之数也。

是故四营而成易
荀爽曰：营者谓七八九六也。陆绩曰：分而为二以象两一营也，挂一以象三二营也，揲之以四以象四时三营也，归奇于扐以象闰四营也，谓四度营为方成易之一爻者也。

十有八变而成卦
荀爽曰：二揲策挂左手一指间、三指间满而成一爻，又六爻三六十八，故十有八变而成卦也。

八卦而小成
侯果曰：谓三画成天地雷风日月山泽之象，此八卦未尽万物情理，故曰小成也。

引而伸之触类而长之
虞翻曰：引谓庖牺引信三才兼而两之，以六画触动也。谓六画以成六十四卦，故引而信之，触类而长之。

天下之能事毕矣
虞翻曰：谓干以简能，能说诸心，能研诸侯之虑，故能事毕。

显道神德行
虞翻曰：显道神德行，干二五之坤成离日坎月，日月在天运行照物，故显道神德行，默而成之，不言而信，存于德行者也。

是故可与酬酢可与祐神矣
九家易曰：阳往为酬，阴来为酢，阴阳相配谓之祐神也。

子曰知变化之道者其知神之所为乎
虞翻曰：在阳称变，干五之坤；在阴称化，坤二之干。阴阳不测之谓神，知变化之道者，故知神之所为。

易有圣人之道四焉
崔憬曰：圣人德合天地，智周万物，故能用此易道，大略有四，谓尚辞、尚变、尚象、尚占也。

以言者尚其辞
虞翻曰：圣人之情见于辞，系辞焉以尽辞也。

以动者尚其变
陆绩曰：变谓爻之变化，当议之而后动矣。

以制器者尚其象
荀爽曰：结绳为网罟，盖取诸离，此类是也。

以卜筮者尚其占
虞翻曰：干为蓍，蓍称筮；离为龟，龟称卜。动则玩其占，故尚其占者也。

是故君子将有为也将有行也问焉而以言
虞翻曰：有为谓建侯，有行谓行师也。干二五之坤成震，有师象；震为行、为言、为问，故有为有行。凡应九筮之法，则筮之，谓问于蓍龟以言其吉凶，爻象动内，吉凶见外，蓍德圆神，卦德方智，故史拟神智以断吉凶也。

其受命也如响
虞翻曰：言神不疾而速，不行而至，不言善应。干二五之坤成震巽，巽为命，震为响，故受命同声相应故如响也。

无有远近幽深遂知来物
虞翻曰：远谓天，近谓地；阴谓幽，深谓阳。来物谓乾坤，神以知来，感而遂通，谓幽赞神明而生蓍也。

非天下之至精其孰能与于此
至精谓干纯粹精也。

参伍以变错综其数
虞翻曰：逆上称错，综理也。谓五岁再闰，再扐而后挂，以成一爻之变而倚六画之数，卦从下升，故错综其数则三天两地而倚数者也。

通其变遂成天地之文
虞翻曰：变而通之，观变阴阳始立卦，乾坤相亲，故成天地之文；物相杂，故曰文。

极其数遂定天下之象
虞翻曰：数六画之数，六爻之动，三极之道，故定天下吉凶之象也。

易无思也无为也
虞翻曰：天下何思何虑，同归而殊涂，一致而百虑，故无所为，谓其静也专。

寂然不动
虞翻曰：谓隐藏坤初机息矣，专故不动者也。

感而遂通天下之故
虞翻曰：感，动也。以阳变阴，通天下之故，谓发挥刚柔而生爻者也。

非天下之至神其孰能与于此
虞翻曰：至神谓易隐初入微知几其神乎。韩康伯曰：非忘象者则无以制象，非遗数者则无以极数；至精者无筹策而不可乱，至变者体一而无不周，至神者寂然而无不应，斯盖功用之母、象数所由立，故曰非至精至变至神则不能与于此也。

夫易圣人之所以极深而研几也
荀爽曰：谓伏羲画卦穷极易幽深，文王系辞研尽易几微者也。

唯深也故能通天下之志
虞翻曰：深谓幽赞神明，无有远近幽深遂知来物，故通天下之志，谓蓍也。

唯几也故能成天下之务
虞翻曰：务，事也。谓易研几开物，故成天下之务，谓卦者也。

唯神也故不疾而速不行而至
虞翻曰：神谓易也，谓日月斗在天，日行一度，月行十三度，从天西转，故不疾而速；星寂然不动，随天右周，感而遂通，故不行而至者也。

子曰易有圣人之道四焉者此之谓也
侯果曰：言易唯深唯神，蕴此四道，因圣人以章，故曰圣人之道矣。

校勘说明：以上是卷十四所录连续专题材料，不等同卷十四全文。原网页在“子曰易有圣人之道四焉者此之谓也”后尚有天地数、乾坤阖辟、太极两仪、河图洛书、立象尽意等段落；这些后续内容尚未在本条完整转录。原页面个别数字、干支及异体字存在录入或识读疑点，须以四库影印页复核，不以本次简体标点稿取代底本。""" ),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十四·太极河洛与立象尽意续录", "底本：景印文渊阁四库全书本《周易集解》卷十四，维基文库卷十四页面。补录“易有太极”至卷末“默而成之，不言而信，存乎德行”的主要经文及注文。前条大衍蓍策材料与本条合读覆盖该网页所载卷十四主体；仍未逐字核对四库影印页，异体字、数字及句读疑点须保留。", """是故易有太極是生兩儀
干寶曰：發初言是故總衆篇之義也。虞翻曰：太極太一也，分為天地故生兩儀也。

兩儀生四象
虞翻曰：四象四時也。兩儀謂乾坤也，乾二五之坤成坎離震兌，震春兌秋坎冬離夏，故兩儀生四象。歸妹卦備，故彖獨稱天地之大義也。

四象生八卦
虞翻曰：乾二五之坤則生震坎艮，坤二五之乾則生巽離兌，故四象生八卦。乾坤生春，艮兌生夏，震巽生秋，坎離生冬者也。

八卦定吉凶
虞翻曰：陽生則吉，陰生則凶，謂方以類聚、物以群分、吉凶生矣，已言於上，故不言生而獨言定吉凶也。

吉凶生大業
荀爽曰：一消一息，萬物豐殖，富有之謂大業。

是故法象莫大乎天地
翟玄曰：見象立法，莫過天地也。

變通莫大乎四時
荀爽曰：四時相變，終而復始也。

懸象著明莫大乎日月
虞翻曰：謂日月懸天成八卦象。三日暮震象出庚，八日兌象見丁，十五日乾象盈甲，十七日旦巽象退辛，二十三日艮象消丙，三十日坤象滅乙，晦夕朔旦坎象流戊，日中則離，離象就巳，戊巳土位象見於中。日月相推而明生焉，故懸象著明莫大乎日月者也。

崇高莫大乎富貴
虞翻曰：謂乾正位於五，五貴坤富，以乾通坤，故高大富貴也。

備物致用立成器以為天下利莫大乎聖人
虞翻曰：神農、黃帝、堯、舜也。民多否閉，取乾之坤謂之備物，以坤之乾謂之致用。乾為物，坤為器用。否四之初耕稼之利，否五之初市井之利，否四之二舟楫之利，否上之初牛馬之利，謂十三蓋取以利天下，通其變使民不倦，神而化之使民宜之，聖人作而萬物覩，故莫乎聖人者也。

探賾索隱鈎深致遠以定天下之吉凶成天下之亹亹者莫大乎蓍龜
虞翻曰：探取賾初也，初隱未見，故探賾索隱，則幽贊神明而生蓍。初深故曰鈎深致遠，謂乾，乾為蓍；乾五之坤大有，離為龜。乾生知吉，坤殺知凶，故定天下之吉凶莫大於蓍龜也。侯果曰：亹，勉也。夫幽隱深遠之情、吉凶未兆之事，物皆勉勉然願知之，然不能也。及蓍成卦、龜成兆也，雖神道之幽密、未然之吉凶，坐可觀也，是蓍龜成天下勉勉之聖也。

是故天生神物聖人則之
孔穎達曰：謂生蓍龜，聖人法則之以為卜筮者也。

天地變化聖人效之
陸績曰：天有晝夜四時變化之道，聖人設三百八十四爻以效之矣。

天垂象見吉凶聖人象之
荀爽曰：謂在璇璣玉衡以齊七政也。宋衷曰：天垂陰陽之象以見吉凶，謂日月薄蝕、五星亂行，聖人象之，亦著九六爻位得失示人，所以有吉凶之占也。

河出圖洛出書聖人則之
鄭玄曰：《春秋緯》云：河以通乾出天苞，洛以流坤吐地符，河龍圖發，洛龜書成，河圖有九篇，洛書有六篇也。孔安國曰：河圖則八卦也，洛書則九疇也。侯果曰：聖人法河圖洛書，制曆象以示天下也。

易有四象所以示也
侯果曰：四象謂上下神物也、變化也、垂象也、圖書也。四者治人之洪範，易有此象所以示人也。

繫辭焉所以告也
虞翻曰：謂繫彖象之辭，八卦以象告也。

定之以吉凶所以斷也
繫辭焉以斷其吉凶，八卦定吉凶以斷天下之疑也。

易曰自天祐之吉无不利
侯果曰：此引大有上九辭以證之義也。大有上九履信思順，自天祐之，言人能依四象所示、繫辭所告，又能思順，則天及人皆共祐之，吉无不利者也。

子曰祐者助也
虞翻曰：大有兌為口，口助稱祐。

天之所助者順也
虞翻曰：大有五以陰順上，故為天所助者順也。

人之所助者信也
虞翻曰：信謂二也，乾為人為信，庸言之信也。

履信思乎順又以尚賢也
虞翻曰：大有五應二而順上，故履信思順。比坤為順，坎為思，乾為賢人，坤伏乾下，故又以尚賢者也。

是以自天祐之吉无不利也
崔憬曰：言上九履五厥孚，履人事以信也；比五而不應三，思天道之順也；崇四匪彭，明辯於五，又以尚賢也。以自天祐之吉无不利，重引易文以證成其義。

子曰書不盡言言不盡意
虞翻曰：謂書易之動九六之變，不足以盡易之所言；言之則不足以盡庖犧之意也。

然則聖人之意其不可見乎
侯果曰：設疑而問也，欲明立象可以盡聖人言意也。

子曰聖人立象以盡意
崔憬曰：言伏羲仰觀俯察而立八卦之象以盡其意。

設卦以盡情偽
崔憬曰：設卦謂因而重之為六十四卦，情偽盡在其中矣。

繫辭焉以盡其言
崔憬曰：文王作卦爻之辭以繫伏羲立卦之象，象既盡意，故辭以盡言也。

變而通之以盡利
陸績曰：變三百八十四爻使相交通，以盡天下之利。

鼓之舞之以盡神
虞翻曰：神，易也。陽息震為鼓，陰消巽為舞，故鼓之舞之以盡神。荀爽曰：鼓者動也，舞者行也，謂三百八十四爻動行相反其卦，所以盡易之神也。

乾坤其易之緼耶
虞翻曰：緼，藏也。易麗乾藏坤，故為易之緼也。

乾坤成列而易立乎其中矣
侯果曰：緼，淵隩也，六子因之而生，故云立乎其中矣。

乾坤毁則无以見易
荀爽曰：毁乾坤之體，則无以見陰陽之交易也。

易不可見則乾坤或幾乎息矣
侯果曰：乾坤者動用之物也，物既動用則不能无毁息矣。天動極復靜，靜極復動，雖天地至此不違變化也。

是故形而上者謂之道形而下者謂之器
崔憬曰：此結上文兼明易之形器變通之事業也。凡天地萬物皆有形質，就形質之中有體有用。體者即形質也，用者即形質上之妙用也。言有妙理之用以扶其體則是道也；其體比用若器之於物，則是體為形之下，謂之為器也。假令天地圓蓋方軫為體為器，以萬物資始資生為用為道；動物以形軀為體為器，以靈識為用為道；植物以枝幹為器為體，以生性為道為用。

化而裁之謂之變
翟玄曰：化變剛柔而則之，故謂之變也。

推而行之謂之通
翟玄曰：推行陰陽，故謂之通也。

舉而措之天下之民謂之事業
陸績曰：變通盡利，觀象制器，舉而措之於天下，民咸用之，亦為事業。九家易曰：謂聖人畫卦為萬民事業之象，故天下之民尊之得為事業矣。

是故夫象聖人有以見天下之賾
崔憬曰：此重明易之緼，更引易象及辭以釋之，言伏羲見天下之深賾，即易之緼者也。

而擬諸其形容象其物宜是故謂之象
陸績曰：此明說立象盡意、設卦盡情偽之意也。

聖人有以見天下之動而觀其會通以行其典禮
侯果曰：典禮有時而用、有時而去，故曰觀其會通也。

繫辭焉以斷其吉凶是故謂之爻
崔憬曰：言文王見天下之動，所以繫象而為其辭，謂之為爻。

極天下之賾者存乎卦
陸績曰：言卦象極盡天下之深情也。

鼓天下之動者存乎辭
宋衷曰：欲知天下之動者，在於六爻之辭也。

化而裁之存乎變推而行之存乎通
崔憬曰：言易道陳陰陽變化之事而裁成之，存乎其變；推理達本而行之，在乎其通。

神而明之存乎其人
荀爽曰：苟非其人，道不虛行也。崔憬曰：言易神无不通、明无不照，能達此理者存乎其人，謂文王述易之聖人。

默而成之不言而信存乎德行
九家易曰：默而成謂陰陽相處也，不言而信謂陰陽相應也。德者有實，行者相應也。崔憬曰：言伏羲成六十四卦，不有言述，而以卦象明之而人信之，在乎合天地之德、聖人之行也。

校勘说明：本条据卷十四页面后段转录，止于“周易集解卷十四”。页面将部分注文连排，录入时以“某家曰”标示；“河图九篇、洛书六篇”等为郑玄所引纬书说，不作为已证实的先秦图书原貌。""" ),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷十三·系辞上传中段续录", "底本：景印文渊阁四库全书本《周易集解》卷十三，维基文库卷十三页面。承接卷十三前段，补录“易与天地准”至“易简之善配至德”一段，含虞翻、荀爽、九家易、韩康伯、郑玄、侯果、王凯冲、宋衷等注文。卷十三后续仍有“子曰易其至矣乎”及鸣鹤、劳谦、负且乘等材料，尚待续录。", """易與天地準故能彌綸天地之道
虞翻曰：準，同也。彌，大；綸，絡。謂易在天下包絡萬物，以言乎天地之間則備矣，故與天地準也。

仰以觀於天文俯則察於地理
荀爽曰：謂陰升之陽則成天之文也，陽降之陰則成地之理也。

是故知幽明之故
荀爽曰：幽謂天上地下不可得覩者也，謂否卦變成未濟也。明謂天地之間萬物陳列著於耳目者，謂泰卦變成既濟也。

原始反終故知死生之說
九家易曰：陰陽交合，物之始也；陰陽分離，物之終也。合則生，離則死，故原始反終故知死生之說矣。交泰時春也，分離否時秋也。

精氣為物遊魂為變
虞翻曰：魂，陽物，謂乾神也；變謂坤鬼。乾純粹精，故主為物；乾流坤體變成萬物，故遊魂為變也。

是故知鬼神之情狀與天地相似故不違
虞翻曰：乾神似天，坤鬼似地，聖人與天地合德，鬼神合吉凶，故不違。鄭玄曰：精氣謂七八也，遊魂謂九六也。七八木火之數也，九六金水之數。木火用事而物生，故曰精氣為物；金水用事而物變，故曰遊魂為變。精氣謂之神，遊魂謂之鬼，木火生物，金水終物，二物變化其情與天地相似，故無所差違之也。

知周乎萬物
荀爽曰：二篇之策萬有一千五百二十，當萬物之數，故曰知周乎萬物也。

而道濟天下故不過
九家易曰：言乾坤道濟成天下而不過也。王凱沖曰：智周道濟，洪纖不遺，亦不過差也。

旁行而不流
九家易曰：旁行周合六十四卦，月主五卦，爻主一日，歲既周而復始也。侯果曰：應變旁行，周被萬物而不流淫也。

樂天知命故不憂
荀爽曰：坤建於亥，乾立於巳，陰陽孤絕，其法宜憂。坤下有伏乾為樂天，乾下有伏巽為知命，陰陽合居故不憂。

安土敦乎仁故能愛
荀爽曰：安土謂否卦乾坤相據故安上；敦仁謂泰卦，天氣下降以生萬物故敦仁，生息萬物故謂之愛也。

範圍天地之化而不過
九家易曰：範者法也，圍者周也。言乾坤消息法周天地而不過於十二辰也。辰，日月所會之宿，謂諏訾、降婁、大梁、實沈、鶉首、鶉火、鶉尾、壽星、大火、析木、星紀、玄枵之屬是也。

曲成萬物而不遺
荀爽曰：謂二篇之策曲成萬物無遺失也。侯果曰：言陰陽二氣委曲成物，不遺微細也。

通乎晝夜之道而知
荀爽曰：晝者謂乾，夜者坤也，通於乾坤之道，無所不知矣。

故神无方而易无體
干寶曰：否泰盈虛者神也，變而周流者易也。言神之鼓萬物無常方，易之應變化無定體也。

一陰一陽之謂道
韓康伯曰：道者何？無之稱也，無不通也，無不由也，況之曰道。寂然無體，不可為象；必有之用極而無之功顯，故至乎神无方而易无體而道可見矣。故窮以盡神，因神以明道。陰陽雖殊，無一以待之，在陰為無陰，陰以之生；在陽為無陽，陽以之成，故曰一陰一陽也。

繼之者善也成之者性也
虞翻曰：繼，統也。謂乾能統天生物，坤合乾性養化成之，故繼之者善，成之者性也。

仁者見之謂之仁知者見之謂之知
侯果曰：仁者見道謂道有仁，知者見道謂道有知也。

百姓日用而不知
侯果曰：用道以濟，然不知其方。

故君子之道鮮矣
韓康伯曰：君子體道以為用，仁知則滯於所見，百姓日用而不知，體斯道者不亦鮮矣乎？故常無欲以觀妙，可以語至而言極矣。

顯諸仁藏諸用
王凱沖曰：萬物皆成，仁功著也；不見所為，藏諸用也。

鼓萬物而不與聖人同憂
侯果曰：聖人成務不能無心，故有憂；神道鼓物寂然無情，故無憂也。

盛德大業至矣哉
荀爽曰：盛德者天，大業者地也。

富有之謂大業日新之謂盛德
王凱沖曰：物無不備，故曰富有；變化不息，故曰日新。

生生之謂易
荀爽曰：陰陽相易，轉相生也。

成象之謂乾
案：道生一，一生二，二生三，三才既備，以成乾象也。

效法之謂坤
案：爻猶效也，效乾三天之法而兩地，成坤之象卦也。

極數知來之謂占
孔穎達曰：謂窮極蓍策之數，逆知將來之事，占其吉凶也。

通變之謂事
虞翻曰：事謂變通趨時，以盡利天下之民，謂之事業也。

陰陽不測之謂神
韓康伯曰：神也者，變化之極，妙萬物而為言，不可以形詰者也。故陰陽不測。嘗試論之曰：原夫兩儀之運、萬物之動，豈有使之然哉？莫不獨化於太虛，欻爾而自造矣。造之非我，理自玄應；化之無主，數自冥運。故不知所以然而況之神矣。是以明兩儀以太極為始，言變化而稱乎神也。夫唯天之所為者，窮理體化，坐忘遺照，至虛而善應，則以道為稱；不思玄覽，則以神為名，蓋資道而同乎道，由神而冥於神者也。

夫易廣矣大矣
虞翻曰：乾象動直故大，坤形動闢故廣也。

以言乎遠則不禦
虞翻曰：禦，止也。遠謂乾，天高不禦也。

以言乎邇則靜而正
虞翻曰：地謂坤，坤至靜而德方故正也。

以言乎天地之間則備矣
虞翻曰：謂易廣大悉備，有天地人道焉，故稱備也。

夫乾其靜也專其動也直是以大生焉
宋衷曰：乾靜不用事，則清靜專一含養萬物矣；動而用事，則直道而行，導出萬物矣。一專一直，動靜有時，而物無夭瘁，是以大生焉。

夫坤其靜也翕其動也闢是以廣生焉
宋衷曰：翕猶閉也。坤靜不用事，閉藏微伏，應育萬物矣；動而用事，則開闢群蟄，敬導沈滯矣。一翕一闢，動靜不失時，而物無災害，是以廣生也。

廣大配天地
荀爽曰：陰廣陽大，配天地。

變通配四時
虞翻曰：變通趨時，謂十二月消息也。泰、大壯、夬配春，乾、姤、遯配夏，否、觀、剝配秋，坤、復、臨配冬，謂十二月消息相變通而周於四時也。

陰陽之義配日月
荀爽曰：謂乾舍於離配日而居，坤舍於坎配月而居，之義是也。

易簡之善配至德
荀爽曰：乾德至健，坤德至順，乾坤簡易相配於天地，故易簡之善配至德。""" ),
    Entry("历代注疏", "魏王弼注、唐孔颖达疏《周易正义》卷一·乾卦经注疏首段", "底本：景印文渊阁《四库全书》本《周易注疏》卷一；参校中华文库《周易正义（四库全书本）/卷01》及识典古籍《周易注疏卷一》。本条录乾卦卦辞、初九、九二及九三开头的经文、陆德明音义、王弼注与孔颖达疏。只录当前核对范围，不代表乾卦或卷一全文完成；后续续录九三余文、九四至用九、彖象与文言，并逐层区分注、音义、疏。", """【上经·乾】

䷀〈乾下乾上〉

乾：元亨利贞。

音义：乾，竭然反，依字作乾，下乙。乾从旦、㫃，㫃音偃。《说卦》云：“乾，健也。”此八纯卦象天。亨，许庚反，卦德也，训通也，余放此。

疏：正义曰：乾者，此卦之名。谓之卦者，《易纬》云：“卦者，挂也。”言悬挂物象以示于人，故谓之卦。但二画之体虽象阴阳之气，未成万物之象，未得成卦，必三画以象三才，写天地、雷风、水火、山泽之象，乃谓之卦也。故《系辞》云“八卦成列，象在其中矣”是也。但初有三画，虽有万物之象，于万物变通之理犹有未尽，故更重之而有六画，备万物之形象，穷天下之能事，故六画成卦也。此乾卦本以象天，天乃积诸阳气而成天。此卦六爻皆阳画，故以象天。

王弼注：天者，定体之名；乾者，体用之称。天以健为用，运行不息，应化无穷。圣人作《易》本以教人，欲使人法天之用，不法天之体，故名乾不名天。元亨利贞者，乾之四德也。

初九：潜龙勿用。

王弼注：文言备矣。

音义：潜，捷盐反。龙，喻阳气及圣人。

疏：正义曰：居第一之位，故称初；以其阳爻，故称九。潜者，隐伏之名；龙者，变化之物。言天之自然之气起于建子之月，阴气始盛，阳气潜在地下，故言初九潜龙也。此自然之象。圣人作法，言于此潜龙之时，小人道盛，圣人虽有龙德，于此时唯宜潜藏，不可施用，故言勿用。

九二：见龙在田，利见大人。

王弼注：出潜离隐，故曰见龙；处于地上，故曰在田。德施周普，居中不偏。虽非君位，君之德也。初则不彰，三则乾乾，四则或跃，上则过亢。利见大人，唯二、五焉。

音义：见龙之见，贤遍反，示也；注及下皆同。利见之见，如字。大人，王肃云圣人在位之目。

疏：正义曰：阳处二位，故曰九二；阳气发见，故曰见龙。田是地上可营为有益之处，阳气发在地上，故曰在田。且初与二俱为地道，二在初上，所以称田。见龙在田是自然之象；利见大人以人事托之，言龙见在田之时，犹似圣人久潜稍出，虽非君位而有君德，故天下众庶利见九二之大人。

校勘与层次说明：以上将经文、王弼注、陆德明音义、孔颖达疏分层排录。原网页个别文字经简繁转换，诸如“悬挂／县挂”“乾／干”等需回到四库影印页复核；本条暂依可核数字底本，不把注文并入经文。此条只覆盖乾卦开篇至九二，不补写尚未录入的后续段落。"""),
    Entry("历代注疏", "魏王弼注、唐孔颖达疏《周易正义》卷一·乾卦九三至用九及彖象", "底本：景印文渊阁《四库全书》本《周易注疏》卷一；参校中华文库《周易正义（四库全书本）/卷01》、识典古籍《周易注疏卷一》。本条续录乾卦九三、九四、九五、上九、用九及《彖》《大象》相关经文、王弼注和孔颖达疏节录。疏文为分层节录，不冒充逐字完整；后续仍须补齐疏文、音义及《小象》《文言》各节。", """九三：君子终日乾乾，夕惕若厉，无咎。

王弼注：处下体之极，居上体之下，在不中之位，履重刚之险。上不在天，未可以安其尊；下不在田，未可以宁其居。纯修下道，则居上之德废；纯修上道，则处下之礼旷。故终日乾乾，至于夕惕犹若厉也。居上不骄，在下不忧，因时而惕，不失其几，虽危而劳，可以无咎。

疏：正义曰：以阳居三位，故称九三；以居不得中，故戒慎。既能如此戒慎，则无罪咎；如其不然，则有咎。故《系辞》云：“无咎者，善补过也。”此一爻因阳居九三之位，皆以人事明其象。

九四：或跃在渊，无咎。

王弼注：去下体之极，居上体之下，乾道革之时也。上不在天，下不在田，中不在人，履重刚之险，而无定位所处，斯诚进退无常之时也。近乎尊位，欲进其道，迫乎在下，非跃所及；欲静其居，居非所安，持疑犹与，未敢决志。用心存公，进不在私，疑以为虑，不谬于果，故无咎也。

疏：正义曰：三与四为人道，九四居上体之下，故称“中不在人”。《文言》云“上下无常，进退无恒”，正是进退无常之时。

九五：飞龙在天，利见大人。

王弼注：不行不跃，而在乎天，非飞而何？故曰飞龙也。龙德在天，则大人之路亨也。夫位以德兴，德以位叙，以至德而处盛位，万物之睹，不亦宜乎？

疏：正义曰：九五阳气盛至于天，故云“飞龙在天”。此自然之象，犹若圣人有龙德而居天位，天下众庶皆得瞻仰，故曰“利见大人”。

上九：亢龙，有悔。

音义：亢，苦浪反。子夏传云：“极也。”《广雅》云：“高也。”

疏：正义曰：上九亢阳之极，虽极未至大凶，但有悔吝而已。《系辞》云：“悔吝者，言乎其小疵也。”故称有悔。

用九：见群龙无首，吉。

王弼注：九，天之德也。能用天德，乃见群龙之义焉。夫以刚健而居人之首，则物之所不与也；以柔顺而为不正，则佞邪之道也。故乾吉在无首，坤利在永贞。

《彖》曰：大哉乾元！万物资始，乃统天。云行雨施，品物流形。大明终始，六位时成，时乘六龙以御天。乾道变化，各正性命。保合太和，乃利贞。首出庶物，万国咸宁。

王弼注：天也者，形之名也；健也者，用形者也。夫形也者，物之累也。有天之形，而能永保无亏，为物之首，统之者岂非至健哉！大明乎终始之道，故六位不失其时而成；升降无常，随时而用，处则乘潜龙，出则乘飞龙，故曰时乘六龙也。乘变化而御大器，静专动直，不失太和，岂非正性命之情者邪？

《象》曰：天行健，君子以自强不息。

疏：正义曰：此大象也。十翼之中第三翼，总象一卦，故谓之大象。天有纯刚，故有健用。今画纯阳之卦以比拟之，故谓之象。天运行不息，故曰“天行健”。君子法此天道，自强不息。

层次说明：本条经文、王弼注与孔颖达疏分层。孔疏只录所列段落的节录，不等于完整疏文；陆德明音义、《小象》逐爻解释及《文言》仍待独立补齐。字形按当前数字底本录入，影印本核验未完成。"""),
    Entry("历代注疏", "魏王弼注、唐孔颖达疏《周易正义》卷一·乾卦《小象》及《文言》前五节", "底本：维基文库《周易正义/01乾》所载《周易正义》卷一乾卦；参校识典古籍《周易注疏卷一》及四库本数字文本入口。此条续录乾卦《小象》六爻与用九，并录《文言》前五节中目前核对到的初九、九二、九三、九四、九五、上九及乾元用九等段。为便于分批核校，孔颖达疏按原文连续录入；本条止于“利贞者，性情也”及其疏文，不代表《文言》全文已完成。数字底本存在异体字、俗字和个别疑似录入讹误，暂依所见文本保留，待影印本逐字复核。译文：暂无可靠逐段译文。", """《小象》：

「潜龙勿用」，阳在下也。「见龙在田」，德施普也。「终日乾乾」，反复道也。〈以上言之则不骄，以下言之则不忧，反覆皆道也。〉

疏：自此以下至「盈不可久」，是夫子释六爻之《象》辞，谓之「小象」。以初九阳潜地中，故云「阳在下也」。经言「龙」而《象》言「阳」者，明经之称「龙」，则阳气也。此一爻之象，专明天之自然之气也。「见龙在田，德施普」者，此以人事言之，用龙德在田，似圣人已出在世，道德恩施，能普遍也。「终日乾乾，反复道」者，此亦以人事言之。君子「终日乾乾」，自强不息，故反之与覆，皆合其道。反谓进反在上也，处下卦之上，能不骄逸，是反能合道也。覆谓从上倒覆而下，居上卦之下，能不忧惧，是覆能合道也。

「或跃在渊」，进无咎也。「飞龙在天」，大人造也。「亢龙有悔」，盈不可久也。

疏：「或跃在渊，进无咎」者，此亦人事言之。进则跳跃在上，退在潜处在渊，犹圣人疑或而在于贵位也。心所欲进，意在于公，非是为私，故「进无咎」也。「飞龙在天，大人造」者，此亦人事言之。「飞龙在天」，犹圣人之在王位。造，为也。唯大人能为之而成就也。「亢龙有悔，盈不可久」者，此亦人事言之。九五是盈也，盈而不已则至上九，地致亢极，有悔恨也。故云「盈不可久也」。

用九，天德不可为首也。

疏：此一节释经之「用九」之《象》辞。经称「用九」，故《象》更叠云「用九」。云「天德不可为首」者，此夫子释辞也。九是天之德也，天德刚健，当以柔和接待于下，不可更怀尊刚为物之首，故云「天德不可为首也」。

《文言》第一节：乾之四德

《文言》曰：元者善之长也，亨者嘉之会也，利者义之和也，贞者事之干也。君子体仁足以长人，嘉会足以合礼，利物足以和义，贞固足以干事。君子行此四德者，故曰：「乾，元、亨、利、贞。」

疏：此为《文言》第一节，论乾之四德。孔颖达释「元者善之长」以下，谓天之生养万物，以施生为善之大者；「元」为施生之宗。亨是通畅万物，使嘉美会聚；利是利益庶物，使各得其宜；贞是以中正之气成就万物。君子体仁、合礼、利物、贞固，是人法天行四德。疏文又辨四德与四时相配，并论诸卦具四德、三德、二德、一德或无德的不同例例。此处节录其义旨，未将该段疏文全部展开。

《文言》第二节：初九

初九曰：「潜龙勿用」，何谓也？子曰：「龙德而隐者也。不易乎世，〈不为世俗所移易也。〉不成乎名，遁世无闷，不见是而无闷，乐则行之，忧则违之，确乎其不可拔，『潜龙』也。」

疏：此释初九爻辞。孔颖达解释「龙德而隐」为圣人有龙德而隐居；「不易乎世」是不因世俗与险难改变本志；「不成乎名」是隐默而不求令名；「遁世无闷」是不因避世而忧闷；「不见是而无闷」是举世不认可自己也不忧闷；「乐则行之，忧则违之」是依内心所安而行止；「确乎其不可拔」是身虽隐遁，守道之志坚实不可拔。

《文言》第三节：九二

九二曰：「见龙在田，利见大人」，何谓也？子曰：「龙德而正中者也。庸言之信，庸行之谨，闲邪存其诚，善世而不伐，德博而化。《易》曰：『见龙在田，利见大人。』君德也。」

疏：此释九二爻辞。「龙德而正中」谓九二居中不偏，但不如九五居尊得位。「庸言之信，庸行之谨」谓平常言语信实、平常行为谨慎。「闲邪存其诚」谓防闲邪恶而存其诚实。「善世而不伐」谓为善于世而不自夸功。「德博而化」谓德行广博，能感化世俗。九二尚未居君位，故称「君德」。

《文言》第四节：九三

九三曰：「君子终日乾乾，夕惕若厉，无咎」，何谓也？子曰：「君子进德修业。忠信所以进德也。修辞立其诚，所以居业也。知至至之，可与几也。知终终之，可与存义也。」〈处一体之极，是「至」也。居一卦之尽，是「终」也。处事之至而不犯咎，「知至」者也。故可与成务矣。处终而能全其终，「知终」者也。夫进物之速者，义不若利，存物之终者，利不及义。故「靡不有初，鲜克有终」。夫「可与存义」者，其唯「知终」者乎？〉

疏：此释九三爻辞。德谓德行，业谓功业。九三所以终日乾乾，是欲进益道德、修营功业。「忠信所以进德」谓推忠于人、以信待物，使德日进；「修辞立其诚，所以居业」谓外修文教、内立诚实，内外相成而有功业可居。「知至至之，可与几」是识别事物将至、尚未成形的先机；「知终终之，可与存义」是知所处之终而保全其宜。孔疏并辨九三进退两端：可进则进，可退则退，须随时而不失其宜。

是故居上位而不骄，在下位而不忧。〈居下体之上，在上体之下，明夫终敝，故「不骄」也。知夫至至，故「不忧」也。〉

疏：居下体之上位而不骄，是因知终而不怀骄慢；处上卦之下而称下位，因知事将至、时机将进，故不忧。

故乾乾因其时而惕，虽危无咎矣。〈惕，怵惕之谓也。处事之极，失时则废，懈怠则旷，故「因其时而惕，虽危无咎」。〉

疏：九三因其已终、已至之时而心怀惕惧，虽处危地仍不至有咎。失时则事废，懈怠则功业空旷，所以须因时而惕。

《文言》第五节：九四

九四曰：「或跃在渊，无咎」，何谓也？子曰：「上下无常，非为邪也。进退无恒，非离群也。君子进德修业，欲及时也，故无咎。」

疏：上而欲跃、下而欲退，是上下无常；其意在公，并非邪曲。进退无恒，是时势使然，并非苟且离群。君子进德修业，是欲及时而行。孔颖达又引何氏之说，解释「上下」据位而言，「进退」据爻而言。

《文言》第六节起首：九五

九五曰：「飞龙在天，利见大人」，何谓也？子曰：「同声相应，同气相求。水流湿，火就燥，云从龙，风从虎，圣人作而万物睹，本乎天者亲上，本乎地者亲下，则各从其类也。」

疏：此释九五爻义。孔颖达以声气相应、水火就其所宜、云从龙而风从虎，说明同类相感；「圣人作而万物睹」则以圣人居位、万物瞻仰，说明大人与万物之间的感应。此段疏文在底本中尚有后续，留待下一批继续录入。

校勘与完整性说明：本条《小象》录入至用九；《文言》录入至九五段起首，九五疏文未完，后续尚有九五疏、上九、第三至第六节后续及全篇收束。上述疏文中明确标为节录或概述的部分，不冒充逐字全文。"""),
    Entry("历代注疏", "魏王弼注、唐孔颖达疏《周易正义》卷一·乾卦《文言》九五至上九及第三至第六节", "底本：维基文库《周易正义/01乾》卷一乾卦，逐段核对页面“文言”部分；参校中华文库四库本《周易正义》卷一。此条续录《文言》第二节九五、上九，第三节六爻人事，第四节六爻天时，第五节乾元四德，第六节初九至上九。经文按底本分段录入；孔颖达疏按底本录入所见段落。王弼注文、陆德明音义未在本条补齐，不能据本条视为乾卦注疏全部完成。译文：暂无可靠逐段译文。", """《文言》第二节续：九五、上九

九五曰：「飛龍在天，利見大人」，何謂也？子曰：「同聲相應，同氣相求。水流濕，火就燥，雲從龍，風從虎，聖人作而萬物睹，本乎天者親上，本乎地者親下，則各從其類也。」

孔颖达疏：此明九五爻之義。「飛龍在天」者，言天能廣感眾物，眾物應之，所以「利見大人」。因大人與眾物感應，故廣陳眾物相感應，以明聖人之作而萬物瞻睹以結之也。「同聲相應」者，若彈宮而宮應，彈角而角動是也。「同氣相求」者，若天欲雨而柱礎潤是也。此二者聲氣相感也。「水流濕，火就燥」者，此二者以形象相感，水流於地，先就濕處；火焚其薪，先就燥處。此同氣水火，皆無識而相感，先明自然之物，故發初言之也。「雲從龍，風從虎」者，龍是水畜，雲是水氣。故龍吟則景雲出，是「雲從龍」也。虎是威猛之獸，風是震動之氣，此亦是同類相感。故虎嘯則穀風生，是「風從虎」也。此二句明有識之物感無識，故以次言之，漸就有識而言也。「聖人作而萬物睹」者，此二句正釋「飛龍在天，利見大人」之義。「聖人作」則「飛龍在天」也，「萬物睹」則「利見大人」也。陳上數事之名，本明於此，是有識感有識也。此亦同類相感，聖人有生養之德，萬物有生養之情，故相感應也。「本乎天者親上，本乎地者親下」者，在上雖陳感應，唯明數事而已。此則廣解天地之間共相感應之義。疏文又引天產、地產、動物、植物之說，說明所受之氣不同而各親其類，並指出感應不止同類，也有異類相感，所舉磁石引針、琥珀拾芥等為例。

上九曰：「亢龍有悔」，何謂也？子曰：「貴而無位，高而無民。〈下無陰也。〉賢人在下位而無輔，〈賢人雖在下而當位，不為之助。〉是以動而有悔也。」

孔颖达疏：此明上九爻辭。「貴而無位」者，以上九非位而上九居之，是無位也。「高而無民」者，六爻皆無陰，是無民也。賢人雖在下位，不為之輔助也。聖人設戒，居此之時不可動作也。王弼注文又指出上九處上卦之極而不當位，獨立而動，物莫之與；並辨乾卦以龍取象、九三以君子為目之由。

《文言》第三节：以人事释六爻

「潛龍勿用」，下也。「見龍在田」，時舍也。「終日乾乾」，行事也。「或躍在淵」，自試也。「飛龍在天」，上治也。「亢龍有悔」，窮之災也。乾元「用九」，天下治也。

王弼注：此一章全以人事明之也。九，陽也。陽，剛直之物也。夫能全用剛直，放遠善柔，非天下至理，未之能也。故「乾元用九」，則「天下治」也。龍之為德，不為妄者也；潛而勿用，必窮處於下；見而在田，必以時之通舍。以爻為人，以位為時，人不妄動，則時皆可知。

孔颖达疏：此一節是《文言》第三節，說六爻人事所治之義。「潛龍勿用，下也」者，言聖人於此潛龍之時，在卑下也。「見龍在田，時舍」者，舍謂通舍，九二以見龍在田，是時之通舍也。「終日乾乾，行事」者，言行此知至、知終之事也。「或躍在淵，自試」者，言聖人逼近五位，不敢果決而進，唯漸漸自試，意欲前進，遲疑不定，故云「自試」也。「飛龍在天，上治」者，言聖人居上位而治理也。「亢龍有悔，窮之災」者，言位窮而致災，災則悔也，非為大禍災也。「乾元用九，天下治」者，《易經》上稱「用九」，「用九」之文總是乾德；「乾」字不可獨言，故舉「元」德以配「乾」。乾元用九德而天下治。疏文又辨九五只是一爻、乾元包六爻，故一稱「上治」、一稱「天下治」。

《文言》第四节：以天时之气释六爻

「潛龍勿用」，陽氣潛藏。「見龍在田」，天下文明。「終日乾乾」，與時偕行。〈與天時俱不息。〉「或躍在淵」，乾道乃革。「飛龍在天」，乃位乎天德。「亢龍有悔」，與時偕極。〈與時運俱終極。〉「乾元用九」，乃見天則。〈此一章全說天氣以明之也。九，剛直之物，唯乾體能用之，用純剛以觀天，天則可見矣。〉

孔颖达疏：此一節是《文言》第四節，明六爻天氣之義。「天下文明」者，陽氣在田，始生萬物，故天下有文章而光明也。「與時偕行」者，此以天道釋爻象，九三乾乾不息、終日自戒，同於天時生物不息。偕，俱也。「乾道乃革」者，去下體、入上體，故云「乃革」也。「乃位乎天德」者，位當天德之位，言九五陽居於天，照臨廣大，故云「天德」也。「與時偕極」者，與時運俱至終極。「乃見天則」者，陽是剛亢之物，能用此純剛，唯天乃然，故云「乃見天則」。

《文言》第五节：复明乾元四德

「乾元」者，始而亨者也。「利貞」者，性情也。〈不為「乾元」，何能通物之始？無不性其情，何能久行其正？是故「始而亨者」，必「乾元」也。利而正者，必「性情」也。〉

孔颖达疏：此一節是第五節，復明上初章及乾四德之義。「乾元者，始而亨者也」，以乾非自當分有德，以元、亨、利、貞為德。「元」是四德之首，故夫子恆以「元」配「乾」而言之，欲見乾、元相將之義；以有乾之元德，故能為物之始而亨通。此解元、亨二德。「利貞者，性情也」者，所以能利益於物而得正者，由性制於情也。性者天生之質，正而不邪；情者性之欲也，若不能以性制情，使其情如性，則不能久行其正。

「乾始，能以美利利天下，不言所利，大矣哉！大哉乾乎，剛健中正，純粹精也！六爻發揮，旁通情也。「時乘六龍」，以御天也。「雲行雨施」，天下平也。」

孔颖达疏：此段复说始而亨、利、贞之义。「乾始」谓乾能始生万物，解元；「能以美利利天下」解利。不复说亨、贞，是前文亨连始、贞连利，举始与利而通包亨、贞。「不言所利」是无所不利。又释「刚健中正，纯粹精」：六爻俱阳，是纯粹；纯粹不杂，是精。「六爻发挥，旁通情」谓六爻发越挥散，旁通万物之情。「云行雨施，天下平」谓天下普得其利而均平不偏陂。

《文言》第六节：广明六爻之义

君子以成德為行，日可見之行也。潛之為言也，隱而未見，行而未成，是以君子弗用也。

孔颖达疏：此一節是《文言》第六節，更復明六爻之義。此節明初九爻辭。「君子以成德為行」者，言君子當以成就道德為行，使德行彰顯；今日所以潜隐，是时未可见。「潜之为言也，隐而未见，行而未成」，是德之幽隐未宣见，所行之行未可成就，故君子于时不用。

君子學以聚之，問以辯之，〈以君德而處下體，資納於物者也。〉寬以居之，仁以行之。《易》曰：「見龍在田，利見大人」，君德也。

孔颖达疏：此复明九二之德。九二从微而进，未在君位，故习学以畜其德；学有未了，更详问其事，以辩决于疑。宽以居之，是用宽裕之道居处其位；仁以行之，是以仁恩之心行之被物。经文称君德，是有君德而未居君位。

九三，重剛而不中，上不在天，下不在田。故乾乾因其時而惕，雖危無咎矣。

孔颖达疏：此明九三爻辞。重刚，谓上下俱阳；不中，谓不居二、五之中位；上不在天，谓非五位；下不在田，谓非二位。居危之地，以乾乾夕惕、戒惧不息，故得无咎。

九四，重剛而不中，上不在天，下不在田，中不在人，故或之。或之者，疑之也，故無咎。

孔颖达疏：三、四俱为人道。九三近二，是下近于地，尚属人道；九四上近于天、下远于地，非人所处，故特言“中不在人”。九四上下无定，心有所疑，欲进欲退，犹豫不定，故无咎。

夫大人者，與天地合其德，與日月合其明，與四時合其序，與鬼神合其吉凶。先天而天弗違，後天而奉天時。天且弗違，而況於人乎？況於鬼神乎？

孔颖达疏：此明九五爻辞，广言大人无所不合。「与天地合其德」谓覆载；「与日月合其明」谓照临；「与四时合其序」如赏以春夏、刑以秋冬；「与鬼神合其吉凶」如福善祸淫。「先天而天弗违」是先于天时行事，天在后而不违；「后天而奉天时」是顺奉上天。天尚且不违，何况人和鬼神。

「亢」之為言也，知進而不知退，知存而不知亡，知得而不知喪。其唯聖人乎！知進退存亡，而不失其正者，其唯聖人乎！

孔颖达疏：此明上九之义。「知进而不知退，知存而不知亡，知得而不知丧」说明上九亢极有悔，正由只知进、存、得而不知退、亡、丧。若能兼知三者，虽居上位，也不至于亢。经文两称“其唯圣人乎”，前一处说明圣人能知进退存亡，后一处说明圣人还能不失正道。

录入范围与校勘说明：本条补齐《文言》第二节九五、上九，并录第三至第六节经文及孔疏主要段落。部分疏文在此为节录整理，非逐字全文；页面原文中有异体字、通假字和个别数字转录异常，以上未据义擅改。需以影印四库本逐字复核后，才能标为严格校定本。乾卦《文言》经文主体至此已录齐；《周易正义》乾卦其余孔疏、王弼注与陆德明音义仍需逐段补全。"""),
    Entry("历代注疏", "魏王弼注、唐孔颖达疏《周易正义》卷一·乾卦九四至用九、《彖》《大象》《小象》疏文补录", "续补乾卦经注疏中此前仅作节录或概述的孔颖达疏文。本条以维基文库《周易正义/01乾》页面逐段录入九四、九五、上九、用九、《彖》《大象》《小象》所见疏文，并与四库本数字文本互校。此为数字转录补录，不代表已经完成宋元明清各本影印逐字校勘。", """一、九四：或跃在渊，无咎

王弼注：
去下体之极，居上体之下，乾道革之时也。上不在天，下不在田，中不在人，履重刚之险，而无定位所处，斯诚进退无常之时也。近乎尊位，欲进其道，迫乎在下，非跃所及。欲静其居，居非所安，持疑犹豫未敢决志。用心存公，进不在私，疑以为虑，不谬于果，故「无咎」也。

孔颖达疏：
「九四：或跃在渊，无咎」。正义曰：「或，疑也。跃，跳跃也。」言九四阳气渐进，似若龙体欲飞，犹疑或也。跃于在渊，未即飞也。此自然之象，犹若圣人位渐尊高，欲进于王位，犹豫疑惧，在于故位，未即进也。云「无咎」者，以其疑惑进退，不即果敢以取尊位，故无咎也。若其贪利务进，时未可行而行，则物所不与，故有咎也。若周西伯内执王心，外率诸侯以事纣也。

注疏曰：「去下体之极」者，离下体入上体，但在下体之上，故云「去下体之极」。注九三云「处下体之极」，彼仍处九三，与此别也。「乾道革之时」者，革，变也。九四去下体入上体，是乾道革之时。「上不在天，下不在田，中不在人」者，《易》之为体，三与四为人道，人近在下，不近于上，故九四云「中不在人」，异于九三也。「而无定位所处」者，九四以阳居阴上，既不在于天，下复不在于地，中又不当于人，上下皆无定位所处。「斯诚进退无常之时」者，《文言》云「上下无常」「进退无恒」是也。「欲进其道，迫乎在下，非跃所及」者，谓欲进其圣道而居王位，但逼迫于下，群众未许，非己独跃所能进及也。「欲静其居，居非所安，持疑犹豫，未敢决志」者，谓志欲静其居处，百姓既未离祸患，须当拯救，所以不得安居，故迟疑犹豫，未敢决断其志而苟进也。「用心存公，进不在私」者，本为救乱除患，不为于己，是进不在私也。「疑以为虑，不谬于果」者，谬谓谬错，果谓果敢；若不思虑，苟欲求进，当错谬于果敢之事，而致败亡；若疑惑以为思虑，则不至于错谬果敢之事。疏文以宋襄公与楚人战而致败亡为反例。

二、九五：飞龙在天，利见大人

王弼注：
不行不跃而在乎天，非飞而何？故曰「飞龙」也。龙德在天，则大人之路亨也。夫位以德兴，德以位叙，以至德而处盛位，万物之睹，不亦宜乎？

孔颖达疏：
「九五」至「利见大人」。正义曰：言九五阳气盛至于天，故云「飞龙在天」。此自然之象，犹若圣人有龙德飞腾而居天位，德备天下，为万物所瞻睹，故天下利见此居王位之大人。
注「龙德在天」。正义曰：「龙德在天，则大人之路亨」，谓若圣人有龙德居在天位，则大人道路得亨通。犹若文王拘在羑里，是大人道路未亨也。「夫位以德兴」者，位谓王位，以圣德之人能兴王位也。「德以位叙」者，谓有圣德之人得居王位，乃能叙其圣德。若孔子虽有圣德，而无其位，是德不能以位叙也。

三、上九：亢龙有悔

王弼注文：本爻经文下未见单独注文；《文言》相关注文另见前条，不以注文缺失为由补造。

孔颖达疏：
正义曰：上九亢阳之至，大而极盛，故曰「亢龙」。此自然之象，以人事言之，似圣人有龙德，上居天位，久而亢极，物极则反，故「有悔」也。纯阳虽极，未至大凶，但有悔吝而已。《系辞》云：「悔吝者，言乎其小疵也。」故郑引尧之末年，四凶在朝，是以有悔未大凶也。凡悔之为文，既是小疵，不单称悔也，必以余字配之。其悔若在，则言「有悔」，谓当有此悔，则此经是也。其悔若无，则言「悔亡」，言其悔已亡也，若《恒卦·九二》「悔亡」是也。其悔虽亡，或是更取他文结之，若《复卦·初九》「不远复无祗悔」之类是也。但圣人至极，终始无亏，故《文言》云：「知进退存亡而不失其正者，其唯圣人乎？」是知大圣之人，本无此悔。但九五天位，有大圣而居者，亦有非大圣而居者，不能不有骄亢，故圣人设法以戒之也。

四、用九：见群龙，无首，吉

王弼注：
九，天之德也。能用天德，乃见「群龙」之义焉。夫以刚健而居人之首，则物之所不与也。以柔顺而为不正，则佞邪之道也。故《乾》吉在「无首」，《坤》利在「永贞」。

孔颖达疏：
「用九：见群龙，无首，吉」。正义曰：「用九见群龙」者，此一句说「乾元」能用天德也。九，天德也。若体「乾元」，圣人能用天德，则见「群龙」之义。「群龙」之义，以无首为吉，故曰「用九，见群龙，无首，吉」也。
注「九，天之德」。正义曰：「九，天之德」者，言六爻俱九，乃共成天德，非是一爻之九，则为天德也。

五、《彖》传：大哉乾元至各正性命

经文：
《彖》曰：「大哉乾元！万物资始，乃统天。云行雨施，品物流形，大明终始，六位时成，时乘六龙，以御天。乾道变化，各正性命。保合大和，乃利贞。首出庶物，万国咸宁。」

王弼注：
天也者，形之名也。健也者，用形者也。夫形也者，物之累也。有天之形而能永保无亏，为物之首，统之者岂非至健哉！大明乎终始之道，故六位不失其时而成，升降无常，随时而用，处则乘潜龙，出则乘飞龙，故曰「时乘六龙」也。乘变化而御大器，静专动直，不失大和，岂非正性命之情者邪？

孔颖达疏：
「彖曰大哉乾元」至「各正性命」。正义曰：夫子所作《彖》辞，统论一卦之义，或说其卦之德，或说其卦之义，或说其卦之名，故《略例》云：「彖者何也？统论一卦之体，明其所由之主。」案褚氏、庄氏并云：「彖，断也，断定一卦之义，所以名为彖也。」但此《彖》释乾与元、亨、利、贞之德。诸儒所说此《彖》分解四德，意各不同。今案庄氏之说，于理稍密，依而用之。

「大哉乾元，万物资始，乃统天」者，此三句解释乾与元。「乾」是卦名，「元」是乾德之首，故以元德配乾释之。「大哉乾元」者，阳气昊大，乾体广远，又以元大始生万物，故曰「大哉乾元」。「万物资始」者，万象之物皆资取乾元，而各得始生，不失其宜，所以称「大」。「乃统天」者，以其至健而为物始，以此乃能统领于天；天是有形之物，以其至健，能总统一切有形，是乾元之德也。

「云行雨施，品物流形」者，此二句释「亨」之德。乾能用天之德，使云气流行、雨泽施布，故品类之物流布成形，各得亨通，无所壅蔽。

「大明终始，六位时成」者，此二句总结乾卦之德。乾之为德，大明晓万物终始之道：始则潜伏，终则飞跃，可潜则潜，可飞则飞，故六爻之位依时而成。若不明终始之道，应潜而飞、应飞而潜、应生而杀、应杀而生，则六位不能依时而成。

「时乘六龙，以御天」者，此二句申明乾元「乃统天」之义。乾之为德，依时乘驾六爻阳气以控御天体。六龙即六位之龙：以所居上下言之谓六位，以阳气升降言之谓六龙。

「乾道变化，各正性命」者，此二句更申明乾元资始之义。道体无形，自然使物开通，谓之为道。乾卦之德自然通物，故称乾道。「变」谓后来改前，以渐移改；「化」谓一有一无，忽然而改。乾道使物渐变、使物卒化，各能正定物之性命。性是天生之质，如刚柔迟速之别；命是人所禀受，如贵贱夭寿之属。

注疏曰：王弼注以天之有形而能永保无亏，反显乾健之用；「静专动直，不失大和」则联系《系辞》乾体静专动直之说。疏文并说明《彖》体例不一，歎美卦德、解释卦名、总结卦义等方式因卦而异，不可机械求同。

「保合大和，乃利贞」：
王弼注：「不和而刚暴。」
孔颖达疏：此二句释「利贞」也。纯阳刚暴，若无和顺，则物不得利，又失其正。以能保安合会大利之道，乃能利贞于万物，言万物得利而贞正也。

「首出庶物，万国咸宁」：
王弼注：「万国所以宁，各以有君也。」
孔颖达疏：自上已来，皆论乾德自然养万物之道。此二句论圣人上法乾德、生养万物，言圣人为君在众物之上，最尊高于物，以头首出于众物之上，各置君长以领万国，故万国皆得宁也。人君位实尊高，故于此云首出于庶物者也。志须卑下，故前经云「无首，吉」也。前文说乾用天德，其事既详，故此文圣人以人事象乾，于文略也。

六、《大象》：天行健，君子以自强不息

经文：
《象》曰：「天行健，君子以自强不息。」

孔颖达疏：
「《象》曰天行健」至「自强不息」。正义曰：此《大象》也。《十翼》之中第三翼，总象一卦，故谓之「大象」。万物之体自然各有形象，圣人设卦以写万物之象。今夫子释此卦之所象，故言「《象》曰」。天有纯刚，故有健用。今画纯阳之卦以比拟之，故谓之《象》。

《象》在《彖》后，是因《彖》详而《象》略；《彖》统论一卦，《象》则取象立义。「天行健」者，行是运动之称，健是强壮之名，「乾」是众健之训。此处《大象》不取其他健物，而专说天，是因万物虽壮健仍有衰怠，唯天运转不息、未曾休息，故称「天行健」。天是体名，乾是用名，健是训义，三者并见，最为详悉，所以尊乾异于他卦。

疏文继而分类说明六十四卦《大象》的取象方式：有总举卦象而不分别上下体者；有直举上下二体者；有先举上象而连于下象者；有先举下象以显上象者；也有借象立义、并非实有其形的假象。举例包括屯、泰、否、噬嗑、恒、解、益、丰、震、巽、坎、离、艮、兑、讼、履、同人、睽、需、小畜、大有、豫、观、剥、大过、大壮、晋、家人、夬、萃、涣、既济、未济、比、临、咸、旅、井、鼎、渐、归妹、蹇、节、中孚、小过、蒙、师、蛊、贲、无妄、颐、遯、损、姤、谦、随、升、革、复、大畜、明夷、困等卦。各例的具体取象理由应结合各卦条目阅读，不能把此处概括误作逐卦注释全文。

疏文又解释：「天行健」谓天体运行昼夜不息、周而复始、无时亏退，是天的自然之象；「君子以自强不息」则是人事取法天象，君子自强勉力而不停止。这里的「君子」通指君临上位、子爱下民者，包括天子、诸侯及有地之公卿大夫；位尊者取法卦象的范围较广，位卑者则应量力而行。若卦义只适用于天子，则另称「先王」。

七、《小象》：六爻及用九

经文：
「潜龙勿用」，阳在下也。
「见龙在田」，德施普也。
「终日乾乾」，反复道也。
「或跃在渊」，进无咎也。
「飞龙在天」，大人造也。
「亢龙有悔」，盈不可久也。
用九，天德不可为首也。

孔颖达疏：
「潜龙勿用」至「反复道也」。正义曰：自此以下至「盈不可久」，是夫子释六爻之《象》辞，谓之「小象」。以初九阳潜地中，故云「阳在下也」。经言「龙」而《象》言「阳」，是说明经文所称「龙」即阳气。此一爻之象，专明天之自然之气。

「见龙在田，德施普」者，此以人事言之，用龙德在田，似圣人已出在世，道德恩施，能普遍也。《比》卦「初九勿用」是其周普之例；若《比》九五，则范围尚狭。

「终日乾乾，反复道」者，此亦以人事言之。君子终日乾乾，自强不息，故反之与覆，皆合其道。反谓进反在上，处下卦之上而能不骄逸，是反能合道；覆谓从上倒覆而下，居上卦之下而能不忧惧，是覆能合道。

「或跃在渊，进无咎」者，此亦人事言之。进则跳跃在上，退则潜处在渊，犹圣人疑惑而处于贵位。心所欲进，意在于公，非为私利，故进无咎。

「飞龙在天，大人造」者，此亦人事言之。「飞龙在天」犹圣人居王位；「造」训为「为」，唯大人能为之而成就。姚信、陆绩等以「造」为造至之「造」，孔疏据《象》辞上下为韵，认为此读不合。

「亢龙有悔，盈不可久」者，此亦人事言之。九五是盈，盈而不已则至上九，达到亢极而有悔恨，故说「盈不可久」。六爻《象》辞首爻说「阳在下」，是举自然之象；其余五爻论人事，互文相通，故知初爻亦兼有人事之义。

「用九，天德不可为首也」。正义曰：此释经之「用九」《象》辞。经称「用九」，故《象》重言「用九」。九是天之德，天德刚健，当以柔和接待于下，不可怀尊刚而为万物之首，故说「天德不可为首」。

校勘与完整性说明：
1. 本条依据维基文库《周易正义/01乾》页面九四至《小象》段落录入，并参看四库本数字文本。网页数字文本中有“鷃疑”等明显疑似转录讹字，本条按上下文只在可确定处作通行字整理；不能确定的异文仍须回看原叶。
2. 本条中《大象》疏的六十四卦分类部分为节录概述，不冒充逐字全文；其余标为“孔颖达疏”的段落依页面逐段转录，标点及部分异体字作统一处理。
3. 《彖》疏后半对《彖》体例的长篇辨析仅作概括，尚未逐字补录；乾卦其余疏文与《文言》部分仍须继续逐段核验。
4. 译文：暂无可靠逐段译文。"""),
    Entry("历代注疏", "宋程颐《伊川易传》", "宋程颐撰，四卷。四库本底本为景印文渊阁四库全书第九册；正文四卷，覆盖上经乾至大有、谦至离，下经咸至升、困至未济。全文离线导入状态：未完成。", """版本与卷次：
卷首含御制题、提要及《伊川易传序》。
卷一：周易上经，乾至大有。
卷二：周易上经，谦至离。
卷三：周易下经，咸至升。
卷四：周易下经，困至未济。

原典入口：
维基文库通行本《伊川易传》：https://zh.wikisource.org/zh-hans/伊川易傳
四库全书本：《伊川易传》：https://zh.wikisource.org/wiki/伊川易傳_(四庫全書本)

录入说明：
程颐以义理解释卦爻，正文常在经文之后逐条作解。导入时须保留经文与程传的对应关系，并区分程传原文与后世标点、简化字转换。当前本条只建立四卷结构和来源入口，尚未把四卷全文离线收录。""",
        chatGptTranslation = """【ChatGPT译文】
版本与卷次：
卷首包括皇帝御制题辞、提要以及《伊川易传序》。
第一卷：周易上经，范围从乾卦到大有卦。
第二卷：周易上经，范围从谦卦到离卦。
第三卷：周易下经，范围从咸卦到升卦。
第四卷：周易下经，范围从困卦到未济卦。

原典入口：
维基文库通行本《伊川易传》。
四库全书本《伊川易传》。

录入说明：
程颐主要运用义理阐释卦与爻，通常在经文之后逐条解释。导入时，应保留经文与程颐解释之间的对应关系，并区分程颐原文、后人添加的标点以及简体字转换。本条目前只列出四卷的结构和原典来源，四卷全文还没有完整收录到离线资料中。"""),
    Entry("历代注疏", "宋朱熹《周易本义》", "朱熹撰。已核实明嘉靖三十年（1551）刊本扫描及清乾隆十五年（1750）刊本扫描；另有卷首九图的版本档案。全文数字文本离线导入状态：未完成。", """版本档案：
明嘉靖辛亥（1551）逢原谿馆刊本，三卷，附《易五赞》一卷；国家图书馆藏扫描：https://commons.wikimedia.org/wiki/File:NCL-01278-0001_%E5%91%A8%E6%98%93%E6%9C%AC%E7%BE%A9.pdf
清乾隆十五年（1750）天都黄晟槐荫草堂刊本，四卷、六册；国家图书馆藏扫描：https://commons.wikimedia.org/wiki/File:NCL-00037_%E5%91%A8%E6%98%93%E6%9C%AC%E7%BE%A9.pdf
另有清康熙内府仿宋咸淳本扫描入口：https://old.shuge.org/ebook/zhou-yi-ben-yi/

卷首九图：
河图、洛书、伏羲八卦次序、伏羲八卦方位、伏羲六十四卦次序、伏羲六十四卦方位、文王八卦次序、文王八卦方位、卦变图。

注意：
以上扫描版本卷数、版式和图版位置不同，不能混成一个无版本标记的文本。当前条目记录了原书版本与扫描入口，尚未逐卷离线录入《周易本义》全文；图像也未全部打包进应用。""",
        chatGptTranslation = """【ChatGPT译文】
版本档案：
明嘉靖三十年（1551）逢原谿馆刊刻本，共三卷，另附《易五赞》一卷；现有国家图书馆所藏扫描件。
清乾隆十五年（1750）天都黄晟槐荫草堂刊刻本，共四卷、六册；现有国家图书馆所藏扫描件。
此外，还提供清康熙内府仿宋咸淳本的扫描入口。

卷首九图：
此处原文在当前记录中只保留了“卷首九图：河图、洛……”这一未完列表。后续图名与说明并未完整录入，因此不能据上下文擅自补全。

录入状态：
朱熹《周易本义》的版本扫描线索已经核实，但正文数字文本尚未完整导入。版本档案和卷首图像线索不能视为全书正文。"""),
    Entry("历代注疏", "王弼《周易略例》", "王弼易学义例专著。现已按维基文库分章数字文本导入序及七章王弼正文；四库本另含唐邢璹注、陆德明音义与考证，尚未与正文合并导入。", """《周易略例序》（唐邢璹序，非王弼正文）：
原夫兩儀未位，神用藏於視聽，一氣化矣。至賾隱乎名言，於是河龍負圖，犧皇畫卦，仰觀俯察遠物近身，入象窮天地之情，六位備柔之體。言大道之妙有，一陰一陽。論聖人之範圍，顯仁藏用。寔三元之胎祖，鼓舞財成。為萬有之蓍龜，知來藏往。是以孔子三絕，未臻樞奧；劉安九師，尚迷宗旨。
臣舞象之年，鼓篋鱣序，漁獵墳典，偏習《周易》，研究耽玩，無舍寸陰，是知卦之紀綱，周文王之方略矣。象之吉兇，魯仲尼之論備矣。至如王輔嗣略例，大則揔一部之指歸，小則明六爻之得失。承乘逆順之理，應變情偽之端，用有行藏，辭有險易。觀之者，可以經緯天地，探測鬼神，匡濟邦家，推辟咎悔。雖人非上聖，亦近代一賢。臣謹依其文，輒為註解，雖不足敷弘《易》道，庶幾有裨於教義，亦猶螢燐增輝於太陽，涓流助深於巨壑。臣之誌也，敢不上聞。

一、明彖
夫《彖》者，何也？統論一卦之體，明其所由之主者也。夫眾不能治眾，治眾者，至寡者也。夫動不能制動，制天下之動者，貞夫一者也。故眾之所以得咸存者，主必致一也。動之所以得咸運者，原必無二也。
物無妄然，必由其理。統之有宗，會之有元，故繁而不亂，眾而不惑。故六爻相錯，可舉一以明也。剛柔相乘，可立主以定也。是故雜物撰德，辯是與非，則非其中爻，莫之備矣。故處統而尋之，物雖眾，則知可以執一禦也。由本以觀之，義雖博，則知可以一名舉也。故處璇璣以觀大運，則天地之動未足怪也。據會要以觀方來，則六合輻湊未足多也。故舉卦之名，義有主矣。觀其《彖辭》，則思過半矣！夫古今雖殊，軍國異容，中之為用，故未可遠也。品制萬變，宗主存焉。《彖》之所尚，斯為盛矣！
夫少者，多之所貴也。寡者，眾之所宗也。一卦五陽而一陰，則一陰為之主矣。五陰而一陽，則一陽為之主矣。夫陰之所求者，陽也。陽之所求者，陰也。陽茍一焉，五陰何得不同而歸之？陰茍只焉，五陽何得不同而從之？故陰爻雖賤，而為一卦之主者，處其至少之地也。或有遺爻而舉二體者，卦體不由乎爻也。繁而不憂亂，變而不憂惑，約以存博，簡以濟眾，其唯《彖》乎？亂而不能惑，變而不能渝，非天下之至賾，其孰能與於此乎？故觀《彖》以斯，義可見矣。

二、明爻通變
夫爻者，何也？言乎變者也。變者何也？情偽之所為也。夫情偽之動，非數之所求也。故合散屈伸，與體相乖。形躁好靜，質柔愛剛，何與情反，質與願違。巧歷不能定其算數，聖明不能為之典要，法制所不能齊，度量所不能均也。為之乎豈在乎大哉！陵三軍者，或懼於朝廷之儀，暴威武者，或困於酒色之娛。
近不必比，遠不必乖。同聲相應，高下不必均也。同氣相求，體質不必齊也。召雲者龍，命呂者律。故二女相違，而剛柔合體。隆墀永歎，遠壑必盈。投戈散地，則六親不能相保。同舟而濟，則吳越何患乎異心？故茍識其情，不憂乖遠。茍明其趣，不煩強武。能說諸心，能研諸慮。睽而知其類，異而知其通，其唯明爻者乎？故有善邇而遠至，命宮而商應。修下而高者降，與彼而取此者服矣。
是故情偽相感，遠近相追，愛惡相攻，屈伸相推，見情者獲，直往則違。故擬議以成其變化，語成器而後有格。不知其所以為主，鼓舞而天下從者，見乎其情者也。
是故範圍天地之化而不過，曲成萬物而不遺，通乎晝夜之道而無體，一陰一陽而無窮。非天下之至變，其孰能與於此哉！是故卦以存時，爻以示變。

三、明卦適變通爻
夫卦者，時也；爻者，適時之變者也。夫時有否泰，故用有行藏。卦有小大，故辭有險易。一時之制，可反而用也。一時之吉，可反而兇也。故卦以反對，而爻亦皆變。是故用無常道，事無軌度，動靜屈伸，唯變所適。故名其卦，則吉兇從其類；存其時，則動靜應其用。尋名以觀其吉兇，舉時以觀其動靜，則一體之變，由斯見矣。夫應者，同誌之象也；位者，爻所處之象也；承乘者，逆順之象也；遠近者，險易之象也；內外者，出處之象也；初上者，始終之象也。是故雖遠而可以動者，得其應也；雖險而可以處者，得其時也。弱而不懼於敵者，得所據也。憂而不懼於亂者，得所附也。柔而不憂於斷者，得所禦也。雖後而敢為之先者，應其始也。物競而獨安於靜者，要其終也。故觀變動者，存乎應。察安危者，存乎位。辯逆順者，存乎承乘。明出處者，存乎外內。

四、明象
夫象者，出意者也。言者，明象者也。盡意莫若象，盡象莫若言。言生於象，故可尋言以觀象。象生於意，故可尋象以觀意。意以象盡，象以言著。故言者所以明象，得象而忘言。象者所以存意，得意而忘象。猶蹄者所以在兔，得兔而忘蹄；筌者所以在魚，得魚而忘筌也。然則，言者，象之蹄也；象者，意之筌也。是故存言者，非得象者也；存象者，非得意者也。象生於意而存象焉，則所存者乃非其象也。言生於象而存言焉，則所存者乃非其言也。然則，忘象者，乃得意者也；忘言者，乃得象者也。得意在忘象，得象在忘言。故立象以盡意，而象可忘也。重畫以盡情，而畫可忘也。
是故觸類可為其象，合義可為其征。義茍在健，何必馬乎？類茍在順，何必牛乎？爻茍合順，何必坤乃為牛？義茍應健，何必乾乃為馬？而或者定馬於乾，案文責卦，有馬無乾，則偽說滋漫，難可紀矣。互體不足，遂及卦變，變又不足，推致五行。一失其原，巧愈彌甚。縱復或值，而義無所取。蓋存象忘意之由也。忘象以求其意，義斯見矣。

五、辯位
案：《象》無初上得位失位之文，又《系辭》但論三五、二四同功異位，亦不及初上，何乎？唯《乾》上九《文言》雲：「貴而無位」，《需》上六雲：「雖不當位」。若以上為陰位邪？則《需》上六不得雲不當位也。若以上陽位邪？則《乾》上九不得雲貴而無位也。陰陽處之，皆雲非位，而初亦不說當位失位也。然則，初上者，是事之終始，無陰陽定位也。故《乾》初謂之潛，過五謂之無位，未有處其位，而雲潛，上有位，而雲無者也。歷觀眾卦，盡亦如之。初上無陰陽定位，亦以明矣。
夫位者，列貴賤之地，待才用之宅也。爻者，守位分之任，應貴賤之序者也。位有尊卑，爻有陰陽。尊者，陽之所處；卑者，陰之所履也。故以尊為陽位，卑為陰位。去初上而論位分，則三五各在一卦之上，亦何得不謂之陽位？二四各在一卦之下，亦何得不謂之陰位？初上者，體之終始，事之先後也。故位無常分，事無常所。非可以陰陽定也。尊卑有常序，終始無常主。故《系辭》但論四爻功位之通例，而不及初上之定位也。然事不可無終始，卦不可無六爻，初上雖無陰陽本位，是終始之地也。統而論之，爻之所處則謂之位，卦以六爻為成，則不得不謂之六位時成也。

六、略例下
凡體具四德者，則轉以勝者為先，故曰「元亨，利貞」也。其有先貞而後亨者，亨由於貞也。凡陰陽者，相求之物也。近而不相得者，誌各有所存也。故凡陰陽二爻，率相比而無應，則近而不相得；有應，則雖遠而相得，然時有險易，卦有小大；同救以相親，同辟以相疏。故或有違斯例者也。然存時以考之，義可得也。
凡《彖》者，統論一卦之體者也。《象》者，各辯一爻之義者也。故《履》卦六三，為兌之主，以應於《乾》。成卦之體，在斯一爻。故《彖》敘其應，雖危而亨也。《象》則各言六爻之義，明其吉兇之行。去六三成卦之體，而指說一爻之德，故危不獲亨而見咥也。《訟》之九二，亦同斯義。凡《彖》者，通論一卦之體者也。一卦之體，必由一爻為主。則指明一爻之美，以統一卦之義，大有之類是也。卦體不由乎一爻，則全以二體之義明之，豐卦之類是也。
凡言無咎者，本皆有咎者也，防得其道，故得無咎也。吉，無咎者，本亦有咎，由吉故得免也。無咎，吉者，先免於咎，而後吉從之也。或亦處得其時，吉不待功，不犯於咎，則獲吉也。或有罪自己招，無所怨咎，亦曰無咎。故《節》六三曰：「不節若，則嗟若，無咎」。《象》曰：「不節之嗟，又誰咎也」？此之謂矣。

七、卦略
屯：此一卦，皆陰爻求陽也。《屯》難之世，弱者不能自濟，必依於強，民思其主之時也。故陰爻皆先求陽，不召自往。馬雖班如，而猶不廢，不得其主，無所馮也。初體陽爻，處首居下，應民所求，合其所望，故大得民也。
蒙：此一卦，陰爻亦先求陽。夫陰昧而陽明，陰困童蒙，陽能發之。凡不識者求問識者，識者不求所告。暗者求明，明者不咨於暗。故童蒙求我，匪我求童蒙也。故六三先唱，則犯於為女。四遠於陽，則困蒙吝。初比於陽，則發蒙也。
履：《雜卦》曰：「《履》，不處也」。又曰：《履》者，禮也，謙以制禮。陽處陰位，謙也。故此一卦，皆以陽處陰為美也。
臨：此剛長之卦也。剛勝則柔危矣，柔有其德，乃得免咎。故此一卦，陰爻雖美，莫過無咎也。
觀：觀之為義，以所見為美者也。故以近尊為尚，遠之為吝。
大過：大過者，棟橈之世也。本末皆弱，棟已橈矣。而守其常，則是危而弗扶，兇之道也。以陽居陰，拯弱之義也，故陽爻皆以居陰位為美。濟衰救危，唯在同好，則所贍褊矣。故九四有應，則有它吝；九二無應，則無不利也。
遯：小人浸長，難在於內，亨在於外，與《臨》卦相對者也。《臨》，剛長則柔危；《遯》，柔長故剛遯也。
大壯：未有違謙越禮能全其壯者也，故陽爻皆以處陰位為美。用壯處謙，壯乃全也；用壯處壯，則觸藩矣。
明夷：為暗之主，在於上六。初最遠之，故曰「君子於行」。五最近之，而難不能溺，故謂之「箕子之貞，明不可息也」。三處明極而征至暗，故曰「南狩獲其大首」也。
睽：睽者，睽而通也。於兩卦之極觀之，義最見矣。極睽而合，極異而通，故先見怪焉，洽乃疑亡也。
豐：此一卦，明以動之卦也。尚於光顯，宣揚發暢者也。故爻皆以居陽位，又不應陰為美。其統在於惡暗而已矣。小暗謂之沛，大暗謂之蔀。暗甚則明盡，未盡則明昧，明盡則鬥星見，明微故見昧，無明則無與乎世，見昧則不可以大事。折其右肱，雖左肱在，豈足用乎？日中之盛，而見昧而已，豈足任乎？

版本与校勘说明：
1. 王弼正文按维基文库《周易略例》分章数字文本录入；章名与正文分开保存。
2. 四库本《周易注疏·周易略例》另含唐邢璹序、邢璹注、陆德明音义及朱良裘等考证。此处不把注文伪装成王弼原文；后续将以独立层次追加。
3. 四库本考证指出不同刻本有脱文、讹字及补校处；如“故卦以反对而爻亦皆变／有变”等字句存在版本差异。此处采用分章数字文本，不宣称已完成版本学定本。
4. 《卦略》当前数字文本列屯、蒙、履、临、观、大过、遯、大壮、明夷、睽、丰十一卦；未据此擅自补出其他卦例。
5. 来源：维基文库《周易略例》及七章分页面；四库本对校入口：https://wk.100ke.info/zh-hant/周易注疏_(四庫全書本)/周易略例
"""),
    Entry("历代注疏", "《御纂周易折中》", "清康熙五十四年（1715）奉敕编纂，李光地主持纂修，汇辑汉唐宋元明诸家易说并折中取舍。四库本含卷首及正文多卷。全文离线导入状态：未完成。", """编纂与内容：
康熙五十四年春告成。卷首含御制序、引用姓氏、纲领等；正文以六十四卦经传为主，广泛征引历代易学家说，并对不同解释加以取舍。它是清代官修汇释，不是先秦或宋代原始注本。

原典入口：
维基文库四库本全文：https://zh.wikisource.org/zh-hans/御纂周易折中_(四庫全書本)
卷首（纲领、易学源流等）：https://zh.wikisource.org/zh-hans/御纂周易折中_(四庫全書本)/卷首

使用说明：
引用诸家姓氏、注家观点和编者按语须分层标注。尤其需区分被引古说、李光地等编纂者的折中意见及康熙御制序。当前只建立全文入口和内容说明，尚未逐卷离线导入。""",
        chatGptTranslation = """【ChatGPT译文】
编纂与内容：
本书于康熙五十四年（1715）春季编纂完成，奉皇帝命令修纂，由李光地主持。卷首包括皇帝御制序、引用书目与作者姓氏、编纂纲领等内容。正文以六十四卦的经文和传文为中心，广泛汇集汉、唐、宋、元、明各代易学家的解释，并对不同观点进行比较、取舍。
因此，《御纂周易折中》属于清代官方编纂的综合性易学汇释，不能误认为先秦原典，也不能视为宋代某一位学者的个人注本。

原典入口：
维基文库所收四库全书本全文入口。
卷首入口包括纲领、易学源流等材料。

使用说明：
引用本书时，必须区分被征引的历代学者原说、李光地等编纂者所作的评议和折中意见，以及康熙皇帝的御制序文。当前条目只建立了全文入口和内容说明，尚未逐卷导入正文。"""),
    Entry("版本档案", "《周易略例》〈明象〉与〈辩位〉疑难字句对校", "依据四库本数字页面、王弼《周易略例》分篇文本及其他独立数字转录，记录《明象》《辩位》三处值得回查的文字。凡未取得原页影像者均标为数字文本对校，不将推定直接升级为定本。", """【一、《明象》“得象在忘言”与四库页“得象往忘言”】
四库本《周易注疏》附录《周易略例》的数字页面在《明象》末段录作“得意在忘象得象往忘言”。王弼《周易略例·明象》分篇文本及其他独立数字转录均作“得意在忘象，得象在忘言”。本项目正文采用“得象在忘言”，并保留如下校记：
- 四库本数字页面：往。
- 《周易略例·明象》分篇数字文本：在。
- 其他数字转录：在。
判断：四库本数字页面的“往”很可能是转录或识别错误；由于尚未直接核验四库原叶影像，暂不把它断定为原刻讹字。后续应查看原叶字形，再确定是原刻异文还是现代数字化错误。

【二、《辩位》“无六爻之无亦作损”】
四库本数字页面将“无六爻之无亦作损”置于《辩位》正文末句“卦以六爻为成，则不得不谓之六位时成也”之后，并明确标作“音义”。因此：
- “卦不可无六爻”中的“无”仍按正文录入，不改成“损”。
- “无六爻之无亦作损”原样保存为陆德明音义层的文字提示。
- 这条音义究竟指异体、借字、版本异写还是数字转录层的特殊字形说明，单凭当前页面文字不足以断定；暂标“待原叶及参校本核验”，不自行解释成确定的通假关系。

【三、《辩位》注文阴阳互倒】
四库本附载《周易略例考证》记载：“乾上九贵而无位”下注“阳居之也”，“需上六虽不当位”下注“阴居之也”；考证指出监本将“阳”“阴”二字互讹，依荆溪家塾本改正。
本项目注文层以四库本校订后的“乾上九—阳居之；需上六—阴居之”为主，同时保留监本互倒这一校勘记录。不可把监本误字误录为王弼正文。
同一组校记还指出，“爻者守位分之任，应贵贱之序者也”下注“各守其位，应之以序”，监本“序”讹作“事”，依《汉魏丛书》本改。该异文属于邢璹注文，不属于王弼正文。

【来源与证据边界】
1. 四库本《周易注疏·周易略例》数字页面：《明象》及《辩位》段、卷末《周易略例考证》。
2. 王弼《周易略例·明象》分篇数字文本，用于对照“得象在忘言”。
3. 其他数字转录用于交叉核对，不等于原刻本影像。
4. 目前尚未逐页取得四库原叶、监本及荆溪家塾本图像；以上属于数字文本与四库所载校记的对校成果，不宣称已完成原刻影印级鉴定。""",
        chatGptTranslation = """【ChatGPT译文】
一、《明象》“得象在忘言”与四库页面所录“得象往忘言”
四库本《周易注疏》附录《周易略例》的数字文本，在《明象》末段写作“得意在忘象得象往忘言”。王弼《周易略例·明象》的分篇文本以及其他独立数字转录，则都写作“得意在忘象，得象在忘言”。
本项目正文采用“得象在忘言”，同时保留版本差异：四库本数字页面作“往”；分篇数字文本和其他数字转录作“在”。
目前判断，“往”很可能是数字转录或文字识别造成的错误。但由于还没有直接核对四库本原叶图像，不能断言这是原刻本自身的错字。后续应查看原书页面的字形，再判断它究竟属于刻本异文，还是现代数字化过程中的错误。

二、《辩位》“无六爻之无亦作损”
这一部分的原文在当前条目中没有完整显示，现有记录仅保留了疑难字句的校对主题和开头。不能凭标题推测后续校勘结论；未录入的部分应继续标为待核。

校勘原则：
这里记录的是不同数字文本之间的对校线索，并非已经完成原刻本影印核验的定本。推测性判断必须与已核实的字形证据分开。"""),
    Entry("版本档案", "《周易略例》宋刻本与相台岳氏本影印线索", "《周易略例》版本档案：区分宋刻本、元相台岳氏荆溪家塾本、清四库本及其现代影印/数字转录。当前取得的是馆藏目录与出版目录证据，不代表已经逐页查看影印图版；后续影印校勘须以可核对的原页图像为准。", """【一、宋刻本线索】
国家图书馆出版社的《周易》影印本产品目录说明，所用底本为国家图书馆藏宋刻本；目录列出《周易略例》第十，起页标为191页。该记录可作为宋刻本影印本的书目线索。当前仅核验出版社目录说明，尚未取得并逐页核对该影印本图像。

目录入口：
https://www.nlcpress.com/ProductView.aspx?Id=10131

【二、元相台岳氏荆溪家塾本线索】
日本国立情报学研究所 CiNii Books 的馆藏记录著录《周易》（王弼、韩康伯注，陆德明释文），北京图书馆出版社2003年《中华再造善本》影印本；记录注明据中国国家图书馆藏元相台岳氏荆溪家塾刻本影印，四册中的第四册含《周易略例》第十。该记录可用于确认影印底本的版本身份及册次，不等于已经核验每页版心、行款或字形。

馆藏目录入口：
https://ge.nii.ac.jp/ncid/BB1522718X

【三、四库本与其他数字文本的边界】
本项目此前录入的《周易注疏（四库全书本）·周易略例》属于清代四库系统的数字转录层。其后附《周易略例考证》明言参据相台岳氏荆溪家塾本、《汉魏丛书》《津逮秘书》参订监本脱讹。四库本校记可以提供异文线索，但不能代替直接查看各参校本原页。

【四、后续校勘记录规范】
1. 每条异文分别记录底本、参校本、原页图像位置、卷次/叶码及行次。
2. “目录著录”“出版社说明”“数字转录”“影印原页”四类证据分开标记，不把书目记录写成亲见原本。
3. 只有在原页图像可辨时，才判断字形、脱文、衍文、版刻漫漶及后人补版；仅有转录文本时标记为“待影印核验”。
4. 对《蒙》条“凡不识者求问识者，识者不求所告”的断句，现已按四库本数字页面连续字序重新标点；仍须在宋刻本、相台本图像中复核字形与句读。
5. 本条仅建立可追溯的版本与影印线索，不宣称已完成宋刻本或元相台本的逐字校勘。""",
        chatGptTranslation = """【ChatGPT译文】
一、宋刻本线索
国家图书馆出版社的《周易》影印本产品目录说明，该影印本采用国家图书馆收藏的宋刻本作为底本，目录中列有《周易略例》第十，起始页码为191页。
这条信息可以作为宋刻本影印本的书目线索。但目前核实的只是出版社目录说明，还没有取得该影印本的图像并逐页检查，因此不能说已经完成对宋刻本正文的核校。
目录入口见原文所列国家图书馆出版社页面。

二、元相台岳氏荆溪家塾本线索
日本国立情报学研究所的 CiNii Books 馆藏目录，著录了一部《周易》（王弼、韩康伯注，陆德明释文）。目录所列为北京图书馆出版社2003年出版的《中华再造善本》影印本，并注明其底本依据中国国家图书馆收藏的元代相台岳氏荆溪家塾刻本。
该套书共四册，其中第四册包含《周易略例》。
这属于馆藏与出版目录提供的版本线索，不等同于已经直接检查元刻本原叶。后续若开展影印校勘，仍须以可逐页核对的图像为依据。"""),
    Entry("版本档案", "马王堆帛书《周易》及易传资料", "1973年长沙马王堆三号汉墓出土帛书《周易》相关资料。经文涉及六十四卦，另有《二三子》《系辞》《衷》《要》《缪和》《昭力》等易传材料。释文整理者廖名春；现代释文不是帛书原件。全文及图版离线导入状态：未完成。", """资料层次必须区分：
一、帛书原件及其考古图像；
二、整理者释文、缀合和释读；
三、后世研究者的校释与现代标点；
四、与传世通行本的异文对照。

已核验资料入口：
《马王堆帛书周易经传释文》（廖名春释文，收入《续修四库全书》经部易类）：https://www.openguji.com/item/d59f2og6qznm
相关资料说明指出，该释文整理的是1973年出土帛书《周易》经文及《二三子》《系辞》《衷》《要》《缪和》《昭力》等篇；不能把整理者释文误称为原件影像。

录入规则：
帛书异体字、通假字、残字、缀合处和释读分歧须并列保存；无法辨认处标“残缺／未释”，不按通行本补写。每条异文记录帛书原图位置、释文版本和传世本对应位置。当前尚未将帛书图版与逐字释文离线收录。""",
        chatGptTranslation = """【ChatGPT译文】
资料范围：
1973年，考古人员在长沙马王堆三号汉墓发现了帛书《周易》相关材料。帛书经文涉及六十四卦，此外还包括《二三子》《系辞》《衷》《要》《缪和》《昭力》等易学传说材料。
本条提到的释文整理者是廖名春。现代整理、释读后的文字属于研究者制作的释文，不能把它当成帛书原件本身。

资料层次必须区分：
一、帛书原件及其考古图像。
二、整理者所作的释文、残片缀合和文字释读。
三、后来的研究者所作的校释与现代标点。
四、帛书文字与传世通行本之间的异文对照。

已核验资料入口：
原文所列《马王堆帛书周易经传释文》，是廖名春整理的释文，收入《续修四库全书》经部易类。相关资料说明，该释文整理了1973年出土的帛书《周易》经文，以及《二三子》《系辞》《衷》《要》《缪和》《昭力》等篇。
因此，引用时必须称为整理者释文，不能称为帛书原件影像。

录入规则：
帛书中的异体字、通假字、残缺文字、残片缀合位置以及不同释读意见，都应并列保存。无法辨认的地方应标注“残缺”或“未释”，不能直接按照传世通行本补写。每一条异文还应记录帛书原图的位置、整理者的释读依据和相关研究意见。"""),
    Entry("版本档案", "敦煌本《周易》资料", "研究线索目录；原卷影像、释文及拼接关系仍需逐项核验。", """重点档案线索：
• 敦煌《周易正义》188071：傅斯年图书馆数字资源。
• P.2530：王弼注《周易》残卷，涉及噬嗑至离卦段落。
• P.2532：王弼注《周易》残卷，涉及解至益卦段落。
• P.2616、P.2619、P.3683、P.3640等：需继续核对原卷图像及拼接关系。
• S.5735+P.2617：含《经典释文》及《易略例》相关残片。

残片拼接关系、卷次、年代判断和释文均须保留来源及置信程度。未经原图核验的 OCR 不作为底本文字。""")
)
    .map { entry -> entry.copy(translation = relatedTranslationFor(entry.title)) }

private fun relatedTranslationFor(title: String): String = when (title) {
    "唐李鼎祚《周易集解》卷一·乾卦经文、彖象集解" -> "译文状态：已录入《周易集解》卷一现存乾卦经文、《彖》《象》《文言》及注文全文；逐段译文尚未完成，暂标“暂无完整逐段译文”。原文依据维基文库卷一页面（页面行112—683），页面末尾为图像及页面信息。"
    "唐李鼎祚《周易集解》卷二·坤、屯、蒙、需集解" -> "译文状态：本条已录入《周易集解》卷二现存坤、屯、蒙、需四卦经文及《彖》《象》《文言》《序卦》相关正文和诸家注文；逐段译文尚未完成，暂标“暂无完整逐段译文”。底本为维基文库卷二页面正文行112—736。"
    "唐李鼎祚《周易集解》卷三·讼、师、比、小畜、履集解" -> "译文状态：本条已录入《周易集解》卷三现存讼、师、比、小畜、履五卦正文及诸家注文；逐段译文尚未完成，暂标“暂无完整逐段译文”。底本为维基文库卷三页面正文行112—656。"
    "唐李鼎祚《周易集解》卷四·同人卦集解" -> "译文状态：暂无完整逐段译文。已录入维基文库卷四页面正文行328—420，覆盖同人卦经文、彖象及注文；后续需逐段翻译并区分注文作者。"
    "唐李鼎祚《周易集解》卷四·谦卦集解" -> "译文状态：暂无完整逐段译文。已录入维基文库卷四正文行497—615，覆盖谦卦经文、彖象及注文；底本疑难字形和异体照录，后续需逐段翻译并校勘。"
    "唐李鼎祚《周易集解》卷五·随卦集解" -> "译文状态：暂无完整逐段译文。已录入维基文库卷五正文行112—199，覆盖随卦经文、彖象及注文；后续需逐段翻译并校勘。"
    "唐李鼎祚《周易集解》卷五·观卦集解" -> "译文状态：暂无完整逐段译文。已录入维基文库卷五正文行366—453，覆盖观卦经文、彖象及注文；后续需逐段翻译并校勘。"
    "唐李鼎祚《周易集解》卷五·噬嗑卦集解" -> "译文状态：暂无完整逐段译文。已录入维基文库卷五正文行454—534，覆盖噬嗑卦经文、彖象及注文；疑难字形按底本保留，后续需逐段翻译并校勘。"
    "唐李鼎祚《周易集解》卷五·贲卦集解" -> "译文状态：暂无完整逐段译文。已录入维基文库卷五正文行535—634，覆盖贲卦经文、彖象及注文；保留底本异体与注文，后续需逐段翻译并校勘。"
    "唐李鼎祚《周易集解》卷五·剥卦集解" -> "译文状态：暂无完整逐段译文。已录入维基文库卷五正文行635—716，覆盖剥卦经文、彖象及注文；后续需逐段翻译并校勘。"
    "唐李鼎祚《周易集解》卷六·复卦集解" -> "译文状态：暂无完整逐段译文。已录入维基文库卷六正文行112—208，含经文、彖象、六爻及注文；后续逐段翻译并校勘。"
    "唐李鼎祚《周易集解》卷六·无妄卦集解" -> "译文状态：暂无完整逐段译文。已录入维基文库卷六正文行210—296，底本疑难字形照录；后续逐段翻译并校勘。"
    "唐李鼎祚《周易集解》卷六·大畜卦集解" -> "译文状态：暂无完整逐段译文。已录入维基文库卷六正文行298—393，保留注文异说；后续逐段翻译并校勘。"
    "唐李鼎祚《周易集解》卷六·颐卦集解" -> "译文状态：暂无完整逐段译文。已录入维基文库卷六正文行395—493，保留案语和页面标记；后续逐段翻译并校勘。"
    "唐李鼎祚《周易集解》卷六·大过卦集解" -> "译文状态：暂无完整逐段译文。已录入维基文库卷六正文行495—574，含经文、彖象及注文；后续逐段翻译并校勘。"
    "唐李鼎祚《周易集解》卷六·坎卦集解" -> "译文状态：暂无完整逐段译文。已录入维基文库卷六正文行576—692，含经文、彖象、六爻、案语及引书；后续逐段翻译并校勘。"
    "唐李鼎祚《周易集解》卷六·离卦集解" -> "译文状态：暂无完整逐段译文。已录入维基文库卷六正文行694—784。页面“百谷草木丽乎也”等疑似缺字照录，未擅自补字；后续逐段翻译并校勘。"
    "唐李鼎祚《周易集解》卷七·咸卦集解" -> "译文状态：已录入咸卦经文、《彖》《象》及页面所载诸家注文（正文行114—192）；逐段译文尚未完成，暂标“暂无完整逐段译文”。疑难字形照底本保留，尚未与四库影印本逐字校勘。"
    "唐李鼎祚《周易集解》卷七·恒、遯集解" -> "译文状态：已录入恒、遯两卦经文、《彖》《象》《序卦》及页面所载注文（正文行194—390）；逐段译文尚未完成，暂标“暂无完整逐段译文”。尚未与四库影印本逐字校勘。"

    "唐李鼎祚《周易集解》卷七·大壮卦集解" -> "译文状态：已录入大壮卦经文、《彖》《象》及页面所载注文（正文行392—469）；逐段译文尚未完成，暂标“暂无完整逐段译文”。尚未与四库影印本逐字校勘。"
    "唐李鼎祚《周易集解》卷七·晋卦集解" -> "译文状态：已录入晋卦经文、《彖》《象》《序卦》及页面所载注文（正文行471—556）；逐段译文尚未完成，暂标“暂无完整逐段译文”。尚未与四库影印本逐字校勘。"
    "唐李鼎祚《周易集解》卷七·明夷卦集解" -> "译文状态：已录入明夷卦经文、《彖》《象》《序卦》及页面所载注文（正文行558—644）；逐段译文尚未完成，暂标“暂无完整逐段译文”。尚未与四库影印本逐字校勘。"

    "唐李鼎祚《周易集解》卷九·夬卦集解" -> "译文状态：已录入夬卦《序卦》、经文、《彖》《象》、六爻及页面所载注文（正文行118—223）；暂无完整逐段译文。原页面特殊字形“扬”以可读字记录，须回看页面图像核验；尚未与四库影印本逐字校勘。"
    "唐李鼎祚《周易集解》卷九·姤卦集解" -> "译文状态：已录入姤卦经文、《彖》《象》、六爻及页面所载注文（正文行224—311）；暂无完整逐段译文。尚未与四库影印本逐字校勘。"

    "唐李鼎祚《周易集解》卷九·萃卦集解" -> "译文状态：已录入萃卦《序卦》、经文、《彖》《象》、六爻及页面所载注文、案语（正文行312—404）；暂无完整逐段译文。原页面个别图像字形无法仅凭文本确认，已标记待核；尚未与四库影印本逐字校勘。"
    "唐李鼎祚《周易集解》卷九·升卦集解" -> "译文状态：已录入升卦《序卦》、经文、《彖》《象》、六爻及页面所载注文（正文行405—480）；暂无完整逐段译文。尚未与四库影印本逐字校勘。"
    "唐李鼎祚《周易集解》卷九·困卦集解" -> "译文状态：已录入困卦经文、《彖》《象》、六爻及页面所载注文、案语（正文行481—586）；暂无完整逐段译文。疑难字及页面残缺处按数字底本保留，尚未与四库影印本逐字校勘。"

    "唐李鼎祚《周易集解》卷十·井卦集解" -> "译文状态：已录入《序卦》、井卦经文、《彖》《象》、六爻及页面注文、案语（正文行119—225）；暂无完整逐段译文。页面数字文本中个别字形、断句尚待影印页复核。"
    "唐李鼎祚《周易集解》卷十·革卦集解" -> "译文状态：已录入《序卦》、革卦经文、《彖》《象》、六爻及页面注文（正文行226—335）；暂无完整逐段译文。尚未逐字回看文渊阁影印页。"
    "唐李鼎祚《周易集解》卷十·鼎卦集解" -> "译文状态：已录入《序卦》、鼎卦经文、《彖》《象》、六爻及页面注文、案语（正文行336—440）；暂无完整逐段译文。爻辞中“铉”字在页面以图像显示，已明确标注图像字形，未伪称已核定。"
    "唐李鼎祚《周易集解》卷十·震卦集解" -> "译文状态：已录入《序卦》、震卦经文、《彖》《象》、六爻及页面注文、案语（正文行441—530）；暂无完整逐段译文。特殊字形按数字页面可见文本保留，待影印页复核。"
    "唐李鼎祚《周易集解》卷十·艮卦集解" -> "译文状态：已录入《序卦》、艮卦经文、《彖》《象》、六爻及页面注文、案语（正文行531—595）；暂无完整逐段译文。个别疑难字、异文尚待影印页复核。"
    "唐李鼎祚《周易集解》卷八·家人卦集解" -> "译文状态：已录入家人卦经文、《彖》《象》、六爻及页面注文（正文行114—199）；逐段译文尚未完成，暂标“暂无完整逐段译文”。尚未与四库影印本逐字校勘。"
    "唐李鼎祚《周易集解》卷八·睽卦集解" -> "译文状态：已录入睽卦经文、《彖》《象》、六爻及页面注文（正文行201—313）；逐段译文尚未完成，暂标“暂无完整逐段译文”。尚未与四库影印本逐字校勘。"
    "唐李鼎祚《周易集解》卷八·蹇卦集解" -> "译文状态：已录入蹇卦经文、《彖》《象》、六爻及页面注文（正文行314—412）；逐段译文尚未完成，暂标“暂无完整逐段译文”。尚未与四库影印本逐字校勘。"

    "唐李鼎祚《周易集解》卷八·损卦集解" -> "译文状态：已录入损卦经文、《彖》《象》、六爻及页面注文（正文行520—631）；逐段译文尚未完成，暂标“暂无完整逐段译文”。尚未与四库影印本逐字校勘。"
    "唐李鼎祚《周易集解》卷八·益卦集解" -> "译文状态：已录入益卦经文、《彖》《象》、六爻及页面注文（正文行632—761）；逐段译文尚未完成，暂标“暂无完整逐段译文”。尚未与四库影印本逐字校勘。"
    "唐李鼎祚《周易集解》卷八·解卦集解" -> "译文状态：已录入解卦经文、《彖》《象》、六爻及页面注文（正文行413—519）；逐段译文尚未完成，暂标“暂无完整逐段译文”。尚未与四库影印本逐字校勘。"
    "唐李鼎祚《周易集解》卷五·蛊卦集解" -> "译文状态：暂无完整逐段译文。已录入维基文库卷五正文行200—292，覆盖蛊卦经文、彖象及注文；保留引书、案语和底本字形，后续需逐段翻译并校勘。"
    "唐李鼎祚《周易集解》卷五·临卦集解" -> "译文状态：暂无完整逐段译文。已录入维基文库卷五正文行293—364，覆盖临卦经文、彖象及注文；后续需逐段翻译并校勘。"
    "唐李鼎祚《周易集解》卷四·豫卦集解" -> "译文状态：暂无完整逐段译文。已录入维基文库卷四正文行617—715，覆盖豫卦经文、彖象及注文；底本疑难字形和夹注标记照录，后续需逐段翻译并校勘。"
    "唐李鼎祚《周易集解》卷四·大有卦集解" -> "译文状态：暂无完整逐段译文。已录入维基文库卷四页面正文行422—495，覆盖大有卦经文、彖象及注文；疑难字形按底本保留，后续需逐段翻译并校勘。"
    "刘牧《十日生五行并相生》第五十五图" -> """译文说明：以下为古文节录的工作直译初稿。
“天一、地六，地二、天七，天三、地八，地四、天九，天五、地十，合而生水、火、木、金、土”：天数一与地数六、地数二与天数七、天数三与地数八、地数四与天数九、天数五与地数十相配，合起来生成水、火、木、金、土。
“十日者，刚日也”：所谓十日，指阳刚之日。
“相生者：金生水，水生木，木生火，火生土，土生金也”：五行相生的次序是金生水、水生木、木生火、火生土、土生金。
“相克者：金克木，木克土，土克水，水克火，火克金也”：五行相克的次序是金克木、木克土、土克水、水克火、火克金。
校读提示：“十日”具体所指及图中配数的解释，仍须结合该书上下文和版本核对。"""
    "刘牧《易数钩隐图》洛书说" -> """译文说明：以下为引文工作直译初稿。
有人问：“《洛书》说：一是水，二是火，三是木，四是金，五是土。那么，它与龙图五行之数的排列位置不相配，是什么缘故？”
回答说：“这里说的是陈列五行的生数。虽然陈列的是生数，却是已经交合之后的数。下篇再把土数配入四季，便具备了成数。况且洛书与九畴，都是从五行之数发展出来的，所以先陈列已经交合的生数，然后再用土数补足，才可以看见它的成数。”
译释提示：“已交之数”“土王四季”等语属于刘牧图书易学的特定理论用语，以上只作贴近字面的转述，不将其解释扩大为先秦原义。"""
    "刘牧《龙图龟书论上》洛书相关论述" -> """译文说明：本条以现代内容提要为主；所引《周易》句的译文如下。
“《易》曰：河出图，洛出书，圣人则之”： 《周易》说，黄河出现图，洛水出现书，圣人取法于它们。
其余内容是对刘牧论述的现代摘要：刘牧把《洪范》九畴、汉儒关于禹受洛书的解释，以及河图、洛书的易学关系放在一起讨论。他提出河图与洛书各有不同的数理系统。这是北宋刘牧的理论主张，不是《周易》经文对河图、洛书图形的直接定义。"""
    "王弼《周易略例》" -> """译文状态：本轮先完成王弼正文七章的离线录入。逐段译文尚未完成；为避免把提要冒充逐段翻译，当前暂标“暂无完整逐段译文”。以下为章节导读，不替代原文译文。

《明彖》：讨论彖辞如何统摄一卦整体。王弼强调以少统多、以一御众，辨明一卦的主爻与整体意义。
《明爻通变》：讨论爻所体现的变化，强调人的情伪、处境、远近、同异与应变关系，不能只靠固定数术推算。
《明卦适变通爻》：卦代表时势，爻体现时势中的变化；判断吉凶要结合卦名、时位、应、承乘、内外、初上等关系。
《明象》：讨论言、象、意的关系，提出“得意在忘象，得象在忘言”，不可执着于固定象数而忘记其所表达的意义。
《辩位》：论初爻、上爻并非简单固定为阴位或阳位，初上主要体现事物的始终；中间四爻才有较稳定的尊卑位序讨论。
《略例下》：补充四德、阴阳相应、彖象分工以及“无咎”等易学义例。
《卦略》：以屯、蒙、履、临、观、大过、遯、大壮、明夷、睽、丰十一卦说明前述解卦原则。

注意：以上是章节内容导读，不是逐句译文。正文中的异体字、通假字和版本异文仍需逐段校订。"""
    "《尚书·洪范》九畴" -> """译文说明：以下为《洪范》全篇工作直译初稿。古汉语制度名词、古文字及若干疑难句仍需与古注、出土文献和不同传本对照，不作为权威译本。

开篇：到了十三年，王前去询问箕子。王说：“啊，箕子！上天在冥冥之中安定下民，使他们协调地居处；我不知道人伦常道应当怎样安排。”

箕子回答：“我听说从前鲧堵塞洪水，扰乱了五行的常序；上帝因此震怒，不把《洪范》九畴赐给他，人伦常道于是败坏。鲧被处死，禹继而兴起；上天于是把《洪范》九畴赐给禹，人伦常道才得以安排。”

九畴总目：第一是五行；第二是敬重并运用五事；第三是整治并运用八政；第四是协调并运用五纪；第五是建立皇极；第六是运用三种德；第七是明辨并运用稽疑；第八是观察并运用各种征验；第九是向往五福，并以六极为警戒。

一、五行：第一是水，第二是火，第三是木，第四是金，第五是土。水的性质是润泽而向下，火是炎热而向上，木是可屈可直，金是可顺从加工改变，土用于种植收获。润下产生咸味，炎上产生苦味，曲直产生酸味，从革产生辛味，稼穑产生甘味。

二、五事：第一是容貌，第二是言语，第三是观看，第四是听闻，第五是思考。容貌要恭敬，言语要顺当，观看要明察，听闻要聪敏，思考要通达。恭敬表现为肃敬，顺当表现为治理，明察表现为明哲，聪敏表现为谋虑，通达表现为圣明。

三、八政：第一是粮食，第二是财货，第三是祭祀，第四是司空（掌管土地与工程），第五是司徒（掌管教化与民事），第六是司寇（掌管刑禁），第七是宾礼，第八是军事。

四、五纪：第一是年，第二是月，第三是日，第四是星辰，第五是历法数度。

五、皇极：君王建立最高的准则，汇聚五福并广泛赐给百姓。百姓因此归向君王所立的准则，也帮助君王保持准则。百姓不可结成邪僻朋党，人与人之间不可结成偏私关系，应由君王树立准则。百姓中有谋划、有作为、有操守的人，君王应当顾念；不合准则而又没有陷入罪咎的人，君王也应接纳。有人安和其容色，说“我所爱好的是德行”，君王就应赐福给他。不要虐待孤独无依的人，同时要敬畏显贵有势者。有人有才能、有作为，应使他修明德行，这样国家就会昌盛。对正直之人，应使其富足并安于善道。不要偏私，不要结党；王道应当广大、平正、端直。君王公布的准则，应成为常法与训诫；百姓听到准则，应当遵行，以接近天子的光明。天子是百姓的父母，是天下的君王。

六、三德：第一是正直，第二是以刚强制胜，第三是以柔和制胜。平和安定时应当正直；强硬而不友善者，用刚强方式治理；温和友善者，用柔和方式治理。对深沉潜伏者可用刚强，对高明显达者可用柔和。福、威和珍贵饮食应由君主掌握；臣下不得擅自施福、施威或享用特权。臣下若擅权，会危害其家并使国家遭凶。人们因此会偏邪不正，百姓也会僭越失常。

七、稽疑：选择并任命负责卜筮的人，再命令他们进行占问。龟卜所见有雨、霁、圛、雺、克等兆象；蓍筮有贞、悔之分。占问中有七类判断，其中五类用于占决，另两类用于推演差错。建立卜筮制度后，三人占问，应听从其中两人的意见。遇到重大疑难，要与自己商议，与卿士商议，与百姓商议，也与龟卜、蓍筮商议。君主、龟卜、蓍筮、卿士、百姓意见一致，称为大同，是吉祥的。后文分别列举不同意见组合及其吉凶；若龟卜、蓍筮都违背人意，静守则吉，妄动则凶。

八、庶征：征验包括雨、晴、暖、寒、风。五种征候按时序齐备，各得其序，草木就会繁茂。五者有一项过度会凶，有一项完全缺失也会凶。吉祥的征验是：君主肃敬，及时雨就顺应而来；君主善治，及时晴朗就顺应而来；君主明哲，及时温暖就顺应而来；君主善谋，及时寒冷就顺应而来；君主圣明，及时风就顺应而来。凶咎的征验则与狂妄、僭越、逸乐、急躁、昏蒙等相应，出现持续失时的雨、晴、暖、寒、风。君主的省察对应岁，卿士对应月，众正官对应日。岁月日时不失常度，百谷成熟，政事明治，俊秀之民显达，家国安宁；若岁月日时失序，百谷不成，政事昏暗，贤俊隐微，家国不宁。百姓如同众星，星象有与风、雨相应的征候。日月运行有冬有夏，月亮跟随不同星宿，则有风雨之应。

九、五福：第一是长寿，第二是富足，第三是健康安宁，第四是爱好德行，第五是善终。六极：第一是凶祸、夭折，第二是疾病，第三是忧患，第四是贫困，第五是恶劣处境，第六是衰弱。

译释边界：“圛”“雺”“克”“贞”“悔”“皇极”“庶征”等词有多家训释；“五纪”“三德”也涉及古代制度语义。以上先给出便于阅读的工作直译，不能替代逐词训诂。对原文异文不确定处，以版本说明标记，不在译文中悄悄抹平。"""
    "《周礼·春官·大卜》三易条" -> """译文说明：以下为节录工作直译初稿；“□高”是底本文字缺失，译文不补。
大卜掌管三种兆象占法：玉兆、瓦兆、原兆。它们的基本兆体各有一百二十种，相关繇辞各有一千二百条。又掌管三种《易》书的占筮法：连山、归藏、周易。它们的经卦都是八种，别卦都是六十四种。大卜还掌管三种梦占之法，并以龟卜、三易、三梦的占问辅助观察国家吉凶，提出补救政事的建议。
后文涉及国君即位、封国、祭祀、迁都、出师、出行和丧事等场合的占卜职责。原文“□高”处无法据当前数字文本确认，故不译、不补。
术语提示：“三易”在此是《周礼》传世文本的记载；其与后世所传《连山》《归藏》文本的关系仍需另作版本考证。"""
    "《周易·系辞上传》河图洛书原文" -> """译文说明：本条原文后已经附有译文。状态：已译初稿，尚待与不同传本校勘。此处译文为：“因此，上天产生神妙之物，圣人取法于它；天地发生变化，圣人仿效它；天上显示征象并呈现吉凶，圣人据此取象；黄河出现图，洛水出现书，圣人取法于它们。”"""
    "唐李鼎祚《周易集解》卷四·泰卦集解" -> """译文状态：暂无完整逐段译文。当前只完成原文录入，后续应按经文、彖象及各家注文逐段翻译，并区分注文作者。"""
    "唐李鼎祚《周易集解》卷四·否卦集解" -> """译文状态：暂无完整逐段译文。当前只完成原文录入，后续按经文、彖象及各家注文逐段翻译，并区分注文作者。"""
    else -> "译文状态：不适用。本条为现代图式说明、书目目录、版本档案或专题资料说明，不是连续古文原文；没有需要另行翻译的古文正文。若后续补入古籍原文，将另设译文并标明底本与状态。"
}

private fun originalImageSourceFor(title: String): String? = when {
    title.contains("刘牧") || title.contains("易数钩隐图") ->
        "https://profound.fate-craft.com/dao/book/LSDZ0153"
    title.contains("周易本义") ->
        "https://commons.wikimedia.org/wiki/File:NLC892-411999019942-45020_%E5%91%A8%E6%98%93%E6%9C%AC%E7%BE%A9_%E7%AC%AC1%E5%86%8A.pdf"
    title.contains("易象图说") ->
        "https://commons.wikimedia.org/wiki/File:CADAL06065978_%E6%98%93%E8%B1%A1%E5%9C%96%E8%AA%AA%E5%85%A7%E7%AF%87%C2%B7%E5%8D%B7%E4%B8%8A~%E5%8D%B7%E4%B8%8B.djvu"
    title.contains("易经图释") ->
        "https://commons.wikimedia.org/wiki/File:%E6%98%93%E7%B6%93%E5%9C%96%E9%87%8B(%E4%B8%80).djvu"
    title.contains("正统道藏") || title.contains("周易图") ->
        "https://ctext.org/library.pl?if=gb&res=83810"
    title.contains("六十四卦版画") ->
        "https://sourcelibrary.org/zh/book/yi-jing-64-hexagram-diagrams-anonymous"
    title.contains("易象钩解") ->
        "https://sourcelibrary.org/book/vol-1-1782366422220"
    else -> null
}

private fun localDiagramAssetsFor(title: String): List<Pair<String, String>> = when {
    title.contains("天地数、四象、八卦三图") -> listOf(
        "tiandi_numbers.svg" to "刘牧《易数钩隐图》天地生成数（现代数表）",
        "four_images.svg" to "四象基本爻象（现代线画）",
        "eight_trigrams.svg" to "八卦基本爻象（现代线画）"
    )
    title.contains("河图第四十九") -> listOf(
        "hetu49.svg" to "刘牧《易数钩隐图》河图第四十九（五方生成数示意）"
    )
    title.contains("五行生数") && title.contains("成数") -> listOf(
        "luoshu_birth.svg" to "刘牧《易数钩隐图》洛书五行生数（第五十三图）",
        "luoshu_complete.svg" to "刘牧《易数钩隐图》洛书五行成数（第五十四图）",
        "ten_stems.svg" to "刘牧《易数钩隐图》十日生五行并相生（第五十五图）"
    )
    title.contains("第五十三图") -> listOf(
        "luoshu_birth.svg" to "刘牧《易数钩隐图》洛书五行生数（第五十三图）"
    )
    title.contains("第五十四图") -> listOf(
        "luoshu_complete.svg" to "刘牧《易数钩隐图》洛书五行成数（第五十四图）"
    )
    title.contains("第五十五图") || title.contains("十日生五行") -> listOf(
        "ten_stems.svg" to "刘牧《易数钩隐图》十日生五行并相生（第五十五图）"
    )
    else -> emptyList()
}

@Composable
private fun LocalDiagramPreview(fileName: String, caption: String) {
    ZeroCard(
        modifier = Modifier.fillMaxWidth(),
        color = ZeroTheme.colors.card.copy(alpha = 0.42f),
        cornerRadius = 24.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ZeroText(
                caption,
                style = MaterialTheme.typography.titleSmall,
                color = ZeroTheme.colors.accent,
                fontWeight = FontWeight.SemiBold
            )
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(430.dp),
                factory = { context ->
                    android.webkit.WebView(context).apply {
                        settings.javaScriptEnabled = false
                        settings.allowFileAccess = true
                        settings.loadsImagesAutomatically = true
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false
                        setBackgroundColor(android.graphics.Color.rgb(244, 236, 217))
                        loadUrl("file:///android_asset/zhouyi_diagrams/$fileName")
                    }
                },
                update = { view ->
                    val target = "file:///android_asset/zhouyi_diagrams/$fileName"
                    if (view.tag != target) {
                        view.tag = target
                        view.loadUrl(target)
                    }
                }
            )
            ZeroText(
                "本图为本地离线 SVG。因对应古图扫描尚未取得可直接收录的图像文件，此处按已录原文及方位／数序说明作现代复原，明确不作为古籍原图。",
                style = MaterialTheme.typography.labelSmall,
                color = ZeroTheme.colors.contentMuted
            )
        }
    }
}

@Composable
private fun LuoshuIllustration(title: String) {
    ZeroCard(
        modifier = Modifier.fillMaxWidth(),
        color = ZeroTheme.colors.card.copy(alpha = 0.42f),
        cornerRadius = 24.dp,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ZeroText("图式示意", style = MaterialTheme.typography.titleSmall,
                color = ZeroTheme.colors.accent, fontWeight = FontWeight.SemiBold)
            when {
                title.contains("九宫数图") -> {
                    val values = listOf(4, 9, 2, 3, 5, 7, 8, 1, 6)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        values.chunked(3).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                row.forEach { number ->
                                    ZeroCard(
                                        modifier = Modifier.weight(1f).aspectRatio(1.12f),
                                        color = if (number == 5) ZeroTheme.colors.accentContainer
                                            else ZeroTheme.colors.card,
                                        cornerRadius = 14.dp,
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            ZeroText(number.toString(),
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (number == 5) ZeroTheme.colors.onAccentContainer
                                                    else ZeroTheme.colors.content)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    ZeroText("横、纵、斜向各线之和均为 15",
                        style = MaterialTheme.typography.labelMedium,
                        color = ZeroTheme.colors.contentMuted)
                }
                title.contains("生数") || title.contains("成数") -> {
                    val isGenerating = title.contains("生数")
                    val values = if (isGenerating)
                        listOf("水" to "1", "火" to "2", "木" to "3", "金" to "4", "土" to "5")
                    else
                        listOf("水" to "6", "火" to "7", "木" to "8", "金" to "9", "土" to "10")
                    ZeroText(if (isGenerating) "五行生数" else "五行成数",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        values.forEach { (element, number) ->
                            ZeroCard(
                                modifier = Modifier.weight(1f),
                                color = ZeroTheme.colors.card,
                                cornerRadius = 14.dp,
                                contentPadding = PaddingValues(vertical = 14.dp, horizontal = 3.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    ZeroText(number, style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold, color = ZeroTheme.colors.accent)
                                    ZeroText(element, style = MaterialTheme.typography.labelMedium,
                                        color = ZeroTheme.colors.content)
                                }
                            }
                        }
                    }
                    ZeroText("此图仅列数与五行对应，不表示古图方位。",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZeroTheme.colors.contentMuted)
                }
            }
            ZeroText("现代矢量重绘 · 非古籍原图",
                style = MaterialTheme.typography.labelSmall,
                color = ZeroTheme.colors.contentMuted)
        }
    }
}
