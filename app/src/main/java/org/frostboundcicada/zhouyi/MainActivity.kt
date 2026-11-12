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
            2 -> all.filter { it.section in listOf("河图", "洛书", "相关古籍", "版本档案", "历代注疏") }
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
                            text = "打开古籍原书影像 ↗",
                            onClick = { uriHandler.openUri(sourceImageUrl) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        ZeroText(
                            "馆藏／数字化原书入口，需要联网；目前尚未缓存为应用内离线图片。",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZeroTheme.colors.contentMuted
                        )
                    }
                }
            }
            if (entry.section == "洛书") {
                item { LuoshuIllustration(entry.title) }
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
                        ZeroText("译文与内容状态", style = MaterialTheme.typography.titleMedium,
                            color = ZeroTheme.colors.accent, fontWeight = FontWeight.SemiBold)
                        ZeroText(entry.translation, modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 30.sp),
                            color = ZeroTheme.colors.contentMuted)
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
                        "请以条目所列底本和版本说明为准。目录占位内容不代表正文已经完整收录；缺文、残字及未经核验的译文不会根据上下文补造。",
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
        2 -> listOf("全部", "河图", "洛书", "历代注疏", "版本档案", "相关古籍")
        else -> listOf("全部", "易经", "十翼", "河图", "洛书", "历代注疏", "版本档案", "相关古籍")
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
用九：显现群龙，却没有谁自居首领，吉。"""),
    Entry("易经", "上经 · 第2卦 · 坤", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "坤：元亨。利牝馬之貞。君子有攸往，先迷後得主。利西南得朋，東北喪朋。安貞，吉。\n初六：履霜，堅冰至。\n六二：直方大，不習无不利。\n六三：含章，可貞。或從王事，无成有終。\n六四：括囊，无咎无譽。\n六五：黃裳，元吉。\n上六：龍戰于野，其血玄黃。\n用六：利永貞。", """译文说明：以下为本项目工作直译初稿。“牝马之贞”等语涉及古代象数与训诂，暂采用贴近字面的译法。

坤：开创而通达；利于像母马一样柔顺而持正。君子有所前往，起初可能迷失方向，后来会找到主导者。向西南行动可以得到同伴，向东北行动会失去同伴。安于正道，吉。
初六：脚下踩到薄霜，便知道坚冰将要到来。
六二：正直、方正而广大；不必刻意学习，也没有什么不利。
六三：内含美好的文采，可以守正。或许跟从君王办事，不以成功自居，却能有好的结局。
六四：扎紧口袋，不显露才华；没有灾咎，也没有声誉。
六五：黄色下裳，大吉。
上六：龙在原野交战，流出的血呈黑黄之色。
用六：利于长久守正。"""),
    Entry("易经", "上经 · 第3卦 · 屯", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "屯：元亨，利貞。勿用有攸往，利建侯。\n初九：磐桓，利居貞，利建侯。\n六二：屯如邅如，乘馬班如，匪寇婚媾，女子貞不字，十年乃字。\n六三：即鹿无虞，惟入于林中，君子幾不如舍，往吝。\n六四：乘馬班如，求婚媾，往，吉无不利。\n九五：屯其膏；小貞吉，大貞凶。\n上六：乘馬班如，泣血漣如。", """译文说明：以下为本项目工作直译初稿。“磐桓”“班如”“字”等词有不同训释，译文不据单一解释强行定论。

屯：开创而通达，利于守正。不宜贸然前往某处，利于建立诸侯。
初九：徘徊难进；利于安居守正，利于建立诸侯。
六二：进退艰难，车马徘徊；来者不是寇盗，而是求婚者。女子占问婚嫁，暂未成婚，经过十年才得成婚。
六三：追逐野鹿却没有熟悉山林的向导，只会进入林中。君子察觉情势不对，不如舍弃追逐；继续前往会有困难。
六四：车马徘徊，前去求婚；前往吉利，没有什么不利。
九五：恩泽积聚而未能广施；小事守正则吉，大事守正则凶。
上六：车马徘徊，悲伤得泣血不止。"""),
    Entry("易经", "上经 · 第4卦 · 蒙", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "蒙：亨。匪我求童蒙，童蒙求我。初筮告，再三瀆，瀆則不告。利貞。\n初六：發蒙，利用刑人，用說桎梏，以往吝。\n九二：包蒙吉，納婦吉，子克家。\n六三：勿用取女，見金夫，不有躬，无攸利。\n六四：困蒙，吝。\n六五：童蒙，吉。\n上九：擊蒙，不利為寇，利禦寇。", """译文说明：以下为本项目工作直译初稿。“刑人”“说桎梏”等语的制度背景与训释仍需结合古注复核。

蒙：亨通。不是我去求教于蒙昧的童子，而是童子来求教于我。初次占问便予以告知；反复轻慢地占问，就不再告知。利于守正。
初六：启发蒙昧，宜建立规范、解除束缚；若一味向前，反而有困难。
九二：包容蒙昧者，吉；娶妻，吉；儿子能够承担家业。
六三：不宜娶这样的女子；她见到有财势的男子，便不能守持自身，没有什么利益。
六四：困于蒙昧，有困难。
六五：童子处于蒙昧之中，吉。
上九：以强力击治蒙昧；不利于把别人当作寇敌，利于抵御寇敌。"""),
    Entry("易经", "上经 · 第5卦 · 需", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "需：有孚，光亨。貞吉，利涉大川。\n初九：需于郊，利用恆，无咎。\n九二：需于沙，小有言，終吉。\n九三：需于泥，致寇至。\n六四：需于血，出自穴。\n九五：需于酒食，貞吉。\n上六：入于穴，有不速之客三人來，敬之終吉。", """译文说明：以下为本项目工作直译初稿。“需”兼有等待、饮食等义；“需于血”语义较难，暂按字面翻译。

需：心中有诚信，光明而通达；守正则吉，利于渡过大河。
初九：在郊外等待；保持恒常，便没有灾咎。
九二：在沙地等待；虽有小的言语纷争，最终吉利。
九三：在泥地等待，招致寇敌到来。
六四：在血污之处等待，最终从穴中出来。
九五：以酒食相待而等待，守正则吉。
上六：进入穴中，有三位不速之客到来；恭敬对待他们，最终吉利。"""),
    Entry("易经", "上经 · 第6卦 · 讼", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "訟：有孚，窒，惕，中吉，終凶。利見大人，不利涉大川。\n初六：不永所事，小有言，終吉。\n九二：不克訟，歸而逋，其邑人三百戶无眚。\n六三：食舊德，貞厲，終吉。或從王事，无成。\n九四：不克訟，復即命渝，安貞吉。\n九五：訟，元吉。\n上九：或錫之鞶帶，終朝三褫之。", """译文说明：以下为本项目工作直译初稿。“窒”“惕”“鞶带”等词保留必要的语义余地。

讼：有诚信，却受到阻塞；心存警惕，居中则吉，争讼到底则凶。利于见到大人，不利于涉越大河。
初六：不把争讼之事长久持续下去；虽有小的言语争议，最终吉利。
九二：不能胜诉，便返回并逃避；其封邑中的三百户人家可以免遭灾祸。
六三：享用祖先留下的德业，守正虽有危险，最终吉利；或许跟从君王办事，不以功成自居。
九四：不能胜诉，返回而改变原先的打算；安于正道，吉。
九五：争讼得到公正裁断，大吉。
上九：有人赐给他系有装饰的带子，但一日之内多次被剥夺。"""),
    Entry("易经", "上经 · 第7卦 · 师", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "師：貞丈人吉，无咎。\n初六：師出以律，否臧，凶。\n九二：在師中吉，无咎；王三錫命。\n六三：師或輿尸，凶。\n六四：師左次，无咎。\n六五：田有禽，利執言，无咎。長子帥師，弟子輿尸，貞凶。\n上六：大君有命，開國承家，小人勿用。", """译文说明：以下为本项目工作直译初稿。“丈人”在此常解释为有经验、德望的长者或统帅；“舆尸”按字面保留。

师：守正而任用有经验的长者为统帅，吉，没有灾咎。
初六：军队出动必须有严明纪律；纪律不好，凶。
九二：统帅居于军中，吉，没有灾咎；君王多次赐命嘉奖。
六三：军队可能用车载运尸体，凶。
六四：军队暂时退驻，没有灾咎。
六五：田野中有猎物，利于捕获并处置。没有灾咎。长子统率军队是合宜的；若让次子等不当人选统军，便会有载尸而归的凶险。守正而行此事仍凶。
上六：君王颁下命令，分封土地、建立诸侯，或使大夫承继家业；小人不可任用。"""),
    Entry("易经", "上经 · 第8卦 · 比", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "比：吉。原筮元永貞，无咎。不寧方來，後夫凶。\n初六：有孚，比之，无咎。有孚盈缶，終來有它，吉。\n六二：比之自內，貞吉。\n六三：比之匪人。\n六四：外比之，貞吉。\n九五：顯比。王用三驅，失前禽，邑人不誡，吉。\n上六：比之无首，凶。", """译文说明：以下为本项目工作直译初稿。“原筮”“后夫”等词存在训释分歧，暂不把某一种解释说成定论。

比：吉。再次占筮，求得长久而正固的结果，没有灾咎。不安定的诸方前来归附，迟迟不来归附者有凶险。
初六：心怀诚信地亲附，没有灾咎。诚信充满如缶中之物，最终会有意外的助益，吉。
六二：从内部亲附，守正则吉。
六三：亲附了不合宜的人。
六四：从外部亲附，守正则吉。
九五：光明正大地亲附。君王以三面围猎、留出一面，放走前方的猎物；邑中之人不受告诫，吉。
上六：亲附却没有首领，凶。"""),
    Entry("易经", "上经 · 第9卦 · 小畜", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "小畜：亨。密雲不雨，自我西郊。\n初九：復自道，何其咎，吉。\n九二：牽復，吉。\n九三：輿說輻，夫妻反目。\n六四：有孚，血去惕出，无咎。\n九五：有孚攣如，富以其鄰。\n上九：既雨既處，尚德載，婦貞厲，月幾望，君子征凶。", """译文说明：以下为本项目工作直译初稿。“畜”有蓄积、约束等义，个别爻辞仍须结合古注复核。

小畜：亨通。浓云密布却没有降雨，云气来自我方西郊。
初九：返回自己原来的道路，能有什么灾咎？吉。
九二：被牵引着返回，吉。
九三：车轮部件脱落，夫妻反目。
六四：心怀诚信，流血之事消退，警惕之心也得以解除；没有灾咎。
九五：诚信相互牵系，富足并惠及邻近的人。
上九：已经下雨，也已经安定；德行积聚而有所承载。妇人占问有危险，月亮几乎圆满；君子出征则凶。"""),
    Entry("易经", "上经 · 第10卦 · 履", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "履虎尾，不咥人，亨。\n初九：素履，往无咎。\n九二：履道坦坦，幽人貞吉。\n六三：眇能視，跛能履，履虎尾，咥人，凶。武人為于大君。\n九四：履虎尾，愬愬終吉。\n九五：夬履，貞厲。\n上九：視履考祥，其旋元吉。", """译文说明：以下为本项目工作直译初稿。“履”兼有践履、礼行之义；“幽人”“夬履”等词保留解释余地。

履：踩到老虎尾巴，老虎却不咬人，亨通。
初九：以朴素平实的方式行事，前往没有灾咎。
九二：所行道路平坦宽广，隐居之人守正则吉。
六三：眼睛不好却想看清，脚有跛疾却要行走；踩到老虎尾巴，老虎咬人，凶。武人竟要成为大君。
九四：踩到老虎尾巴，心怀恐惧戒慎，最终吉利。
九五：果断地践行，守正而处危险。
上九：考察自己走过的道路并检验吉凶征兆；回顾周全，大吉。"""),
    Entry("易经", "上经 · 第11卦 · 泰", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "泰：小往大來，吉亨。\n初九：拔茅茹以其彙，征吉。\n九二：包荒。用馮河，不遐遺；朋亡。得尚于中行。\n九三：无平不陂，无往不復，艱貞无咎。勿恤其孚，于食有福。\n六四：翩翩，不富以其鄰；不戒以孚。\n六五：帝乙歸妹，以祉，元吉。\n上六：城復于隍，勿用師，自邑告命，貞吝。", """译文说明：以下为本项目工作直译初稿。“包荒”“朋亡”“得尚于中行”等语有多种训释，译文先保留句法关系，不强定唯一解释。

泰：小的离去，大的到来，吉而亨通。
初九：拔起茅草，根茎相连带出同类；前往吉利。
九二：包容荒远之物。徒步涉越大河，不遗弃远方之人；不结党营私，得以辅佐中正之行。
九三：没有永远平坦而不倾斜的事物，也没有一去不返的事物；艰难中守正，没有灾咎。不必忧虑诚信是否得到回应，饮食之中自有福分。
六四：轻快地相连而来，不以财富自夸，却与邻人相亲；不必告诫，彼此已有诚信。
六五：帝乙嫁女，因此获得福祉，大吉。
上六：城墙坍塌，土回到护城壕中；不宜动用军队，应从自己的城邑发布命令，守正也有困难。"""),
    Entry("易经", "上经 · 第12卦 · 否", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "否之匪人，不利君子貞，大往小來。\n初六，拔茅茹以其彙，貞吉。亨。\n六二，包承，小人吉，大人否。亨。\n六三，包羞。\n九四，有命，无咎，疇離祉。\n九五，休否，大人吉。其亡其亡，繫于苞桑。\n上九，傾否，先否後喜。", """译文说明：以下为本项目工作直译初稿。“否”在此为闭塞之义；爻辞中的“包承”“疇離祉”等词仍需逐本校勘训释。

否：闭塞不通，不利于君子守正；大的离去，小的到来。
初六：拔起茅草，根茎相连带出同类；守正吉利，亨通。
六二：包容并承受；小人得吉，大人则处于闭塞之中，但仍可亨通。
六三：包藏羞耻。
九四：有命令降下，没有灾咎；同类之人也会获得福祉。
九五：闭塞状态得以休止，大人吉利。要警惕“将要灭亡了，将要灭亡了”，如同把根系系在丛生的桑树上。
上九：闭塞倾覆，先经历闭塞而后转为喜悦。"""),
    Entry("易经", "上经 · 第13卦 · 同人", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "同人于野，亨。利涉大川，利君子貞。\n初九：同人于門，無咎。\n六二：同人于宗，吝。\n九三：伏戎于莽，升其高陵，三歲不興。\n九四：乘其墉，弗克，攻吉。\n九五：同人，先號啕而后笑。大師克相遇。\n上九：同人于郊，無悔。", """译文说明：以下为本项目工作直译初稿。“同人”指与人相同、与人协同；“攻吉”等句读与训释须结合异本再核。

同人：在旷野与人会同，亨通。利于涉越大河，利于君子守正。
初九：在门外与人会同，没有灾咎。
六二：只在宗族内部与人会同，有困难。
九三：把军队埋伏在草莽之中，又登上高陵窥察；三年不能兴起行动。
九四：登上城墙，却不能攻克；继续进攻则吉。
九五：与人会同，先是号哭，后来欢笑；大军克敌，终于相会。
上九：在郊外与人会同，没有悔恨。"""),
    Entry("易经", "上经 · 第14卦 · 大有", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "大有：元亨。\n初九：无交害，匪咎，艱則无咎。\n九二：大車以載，有攸往，无咎。\n九三：公用亨于天子，小人弗克。\n九四：匪其彭，无咎。\n六五：厥孚交如，威如；吉。\n上九：自天佑之，吉无不利。", """译文说明：以下为本项目工作直译初稿。“亨”在“公用亨于天子”中涉及享献、通达等古义，暂不把礼制解释写成定论。

大有：大为亨通。
初九：没有交相侵害，不算灾咎；若能谨慎艰难地行事，便没有灾咎。
九二：用大车装载，有所前往，没有灾咎。
九三：诸侯得以向天子进献或享献；小人不能承担此事。
九四：不以盛大自居，没有灾咎。
六五：诚信相交，既有威严又能取信，吉。
上九：得到上天的护佑，吉，没有什么不利。"""),
    Entry("易经", "上经 · 第15卦 · 谦", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "謙：亨，君子有終。\n初六：謙謙君子，用涉大川，吉。\n六二：鳴謙，貞吉。\n九三：勞謙君子，有終吉。\n六四：无不利，撝謙。\n六五：不富，以其鄰，利用侵伐，无不利。\n上六：鳴謙，利用行師，征邑國。", """译文说明：以下为本项目工作直译初稿。“撝谦”有施行、发挥谦德等解释，暂保留其行为义。

谦：亨通，君子能够有好的终局。
初六：谦而又谦的君子，利于涉越大河，吉。
六二：谦德显现于言行，守正吉利。
九三：有功劳而仍能谦逊的君子，最终吉利。
六四：没有什么不利，应当施行谦逊。
六五：不凭借财富，却能得到邻人的支持；利于征伐不服者，没有什么不利。
上六：谦德之名声显现，利于出动军队征讨邑国。"""),
    Entry("易经", "上经 · 第16卦 · 豫", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "豫：利建侯行師。\n初六：鳴豫，凶。\n六二：介于石，不終日，貞吉。\n六三：盱豫，悔。遲有悔。\n九四：由豫，大有得。勿疑。朋盍簪。\n六五：貞疾，恆不死。\n上六：冥豫，成有渝，无咎。", """译文说明：以下为本项目工作直译初稿。“盱豫”“朋盍簪”等词义较难，暂以语境直译，并保留待考标记。

豫：利于建立诸侯、出动军队。
初六：沉溺于逸乐而自鸣得意，凶。
六二：坚定如石，不等到一天结束便能察觉变化；守正吉利。
六三：仰望他人的逸乐而心生依附，悔；若迟疑拖延，还会有悔。
九四：众人因他而欢悦，他因此大有所得；不要疑虑，朋友聚集而来。
六五：守正而处于疾病之中，长久如此却不会死亡。
上六：昏暗地沉溺于逸乐，事情虽已形成但能够改变，没有灾咎。"""),
    Entry("易经", "上经 · 第17卦 · 随", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "隨：元亨。利貞。无咎。\n初九：官有渝，貞吉。出門交有功。\n六二：系小子，失丈夫。\n六三：系丈夫，失小子。隨，有求得利，居貞。\n九四：隨有獲，貞凶。有孚在道，以明，何咎。\n九五：孚于嘉，吉。\n上六：拘系之，乃從維之。王用亨于西山。", """译文说明：以下为本项目工作直译初稿。“官有渝”“随有获”等词涉及古代职官与礼制语境，暂不作过度引申。

随：开创而通达，利于守正，没有灾咎。
初九：职守发生变化，守正吉利；出门与人交往，会有收获。
六二：系恋年少者，便会失去成年之人。
六三：系恋成年之人，便会失去年少者。跟随他人，有所求便能得到利益；安居守正。
九四：跟随而有所收获，守正仍有凶险。若诚信合乎正道，并能明辨事理，还会有什么灾咎？
九五：诚信地亲近美善，吉。
上六：拘留并系住他，随后又以绳索维系。君王在西山举行享祭。"""),
    Entry("易经", "上经 · 第18卦 · 蛊", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "蠱：元亨。利涉大川。先甲三日，後甲三日。\n初六：幹父之蠱，有子考，无咎，厲終吉。\n九二：幹母之蠱，不可貞。\n九三：幹父之蠱，小有悔，无大咎。\n六四：裕父之蠱，往見吝。\n六五：幹父之蠱，用譽。\n上九：不事王侯，高尚其事。", """译文说明：以下为本项目工作直译初稿。“蛊”可指积弊、败坏，也涉及整治旧事；父母爻辞的具体训释仍需结合古注。

蛊：开创而通达，利于涉越大河；在甲日之前三日谋划，在甲日之后三日继续谋划。
初六：整治父辈留下的弊事；有能承继家业的儿子，没有灾咎，虽有危险，最终吉利。
九二：整治母辈留下的弊事，不宜固执地坚持某一种做法。
九三：整治父辈留下的弊事，虽有小的悔恨，却没有大的灾咎。
六四：宽缓地处理父辈留下的弊事，继续前往会遇到困难。
六五：整治父辈留下的弊事，因此获得称誉。
上九：不侍奉王侯，把自己的事业看得高尚。"""),
    Entry("易经", "上经 · 第19卦 · 临", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "臨：元亨。利貞。至于八月有凶。\n初九：咸臨，貞吉。\n九二：咸臨，吉无不利。\n六三：甘臨，无攸利。既憂之，无咎。\n六四：至臨，无咎。\n六五：知臨，大君之宜，吉。\n上六：敦臨，吉无咎。", """译文说明：以下为本项目工作直译初稿。“咸临”“甘临”“敦临”等为爻位术语化表达，暂按字面与语境合译。

临：开创而通达，利于守正；到了八月会有凶险。
初九：以感通之道临近他人，守正吉利。
九二：以感通之道临近他人，吉，没有什么不利。
六三：以甜言悦色的方式临近他人，没有利益；若已经意识到问题并感到忧虑，便没有灾咎。
六四：亲自前来临近，没有灾咎。
六五：以智慧临事，这是大君应有的做法，吉。
上六：敦厚诚恳地临近他人，吉，没有灾咎。"""),
    Entry("易经", "上经 · 第20卦 · 观", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "觀：盥而不荐，有孚顒若。\n初六：童觀，小人无咎，君子吝。\n六二：窺觀，利女貞。\n六三：觀我生，進退。\n六四：觀國之光，利用賓于王。\n九五：觀我生，君子无咎。\n上九：觀其生，君子无咎。", """译文说明：以下为本项目工作直译初稿。“盥而不荐”属于祭礼用语；“观我生”可有观察自身行为、观察所生之事等训释，暂保留原句结构。

观：行盥洗之礼，却尚未献上祭品；心怀诚信，仪态庄严恭敬。
初六：像孩童一样观看；小人没有灾咎，君子则有困难。
六二：从门缝窥看；女子占问有利。
六三：观察自己的所作所为，据此决定进退。
六四：观看国家的光明盛德，适宜作为宾客去朝见君王。
九五：观察自己的所作所为，君子没有灾咎。
上九：观察他人的所作所为，君子没有灾咎。"""),
    Entry("易经", "上经 · 第21卦 · 噬嗑", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "噬嗑：亨。利用獄。\n初九：屨校滅趾，无咎。\n六二：噬膚滅鼻，无咎。\n六三：噬臘肉，遇毒；小吝，无咎。\n九四：噬乾胏，得金矢，利艱貞，吉。\n六五：噬乾肉，得黃金，貞厲，无咎。\n上九：何校滅耳，凶。", """译文说明：以下为本项目工作直译初稿。本卦多用咀嚼、刑罚意象；“噬乾胏”“金矢”等古代物名与制度含义，仍须对照注疏。

噬嗑：亨通，利于施用刑罚、审理案件。
初九：脚上戴着刑具，遮蔽了脚趾；没有灾咎。
六二：咬到柔软的肉，遮蔽了鼻子；没有灾咎。
六三：咬到带骨的腊肉，遇到毒害；虽有小困难，没有灾咎。
九四：咬到干硬的骨肉，得到金属箭头；利于在艰难中守正，吉。
六五：咬到干肉，得到黄金；守正虽有危险，却没有灾咎。
上九：肩上负着刑具，遮蔽了耳朵，凶。"""),
    Entry("易经", "上经 · 第22卦 · 贲", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "賁：亨。小利有攸往。\n初九：賁其趾，舍車而徒。\n六二：賁其須。\n九三：賁如濡如，永貞吉。\n六四：賁如皤如，白馬翰如，匪寇婚媾。\n六五：賁於丘園，束帛戔戔，吝，終吉。\n上九：白賁，无咎。", """译文说明：以下为本项目工作直译初稿。“贲”有文饰之义；“束帛戋戋”等礼物数量与用法，不在无版本依据时作具体换算。

贲：亨通；小事上有所前往有利。
初九：装饰自己的脚，舍弃车乘而徒步行走。
六二：装饰胡须。
九三：文饰鲜明而润泽；长久守正，吉。
六四：文饰转为素白，白马飞驰而来；来者不是寇盗，而是求婚者。
六五：在山丘园圃中施行文饰，礼用束帛数量不多；虽有困难，最终吉利。
上九：以素白为饰，没有灾咎。"""),
    Entry("易经", "上经 · 第23卦 · 剥", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "剝：不利。有攸往。\n初六：剝牀以足，蔑貞凶。\n六二：剝牀以辨，蔑貞凶。\n六三：剝之，无咎。\n六四：剝牀以膚，凶。\n六五：貫魚，以宮人寵，无不利。\n上九：碩果不食，君子得輿，小人剝廬。", """译文说明：以下为本项目工作直译初稿。“辨”在“剥床以辨”中的具体所指存在不同训释；此处不擅自确定为某一种床具构件。

剥：不利于有所前往。
初六：从床脚开始剥落，轻慢正道则凶。
六二：剥落到床的边缘或构件，轻慢正道则凶。
六三：剥落到自身这一部分，便没有灾咎。
六四：剥落到床面及身体接触之处，凶。
六五：像串起鱼一样依次排列，受到宫中之人的宠爱，没有什么不利。
上九：硕大的果实没有被吃掉；君子因此得到车舆，小人却失去了居所。"""),
    Entry("易经", "上经 · 第24卦 · 复", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "復：亨。出入无疾，朋來无咎。反復其道，七日來復，利有攸往。\n初九：不復遠，无袛悔，元吉。\n六二：休復，吉。\n六三：頻復，厲无咎。\n六四：中行獨復。\n六五：敦復，无悔。\n上六：迷復，凶，有災眚。用行師，終有大敗，以其國君，凶；至于十年，不克征。", """译文说明：以下为本项目工作直译初稿。“无袛悔”“七日来复”等语涉及古代时间观与训诂，暂不附会为具体历法周期。

复：亨通。出入没有疾病或灾害，朋友前来没有灾咎。返回原来的道路，七日便会复归；利于有所前往。
初九：返回得并不远，没有大的悔恨，大吉。
六二：美好地返回，吉。
六三：屡次反复返回，虽有危险，没有灾咎。
六四：行于中道，却独自返回。
六五：敦厚诚恳地返回，没有悔恨。
上六：迷失方向而不知返回，凶，有灾祸。若因此出兵，最终会遭到大败，甚至危及国君；直到十年之后，仍不能出征。"""),
    Entry("易经", "上经 · 第25卦 · 无妄", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "无妄：元亨。利貞。其匪正有眚，不利有攸往。\n初九：无妄，往吉。\n六二：不耕穫，不菑畬，則利有攸往。\n六三：无妄之災，或系之牛，行人之得，邑人之災。\n九四：可貞，无咎。\n九五：无妄之疾，勿藥有喜。\n上九：无妄，行有眚，无攸利。", """译文说明：以下为本项目工作直译初稿。“无妄”有不妄为、不虚妄等解释；“不耕获”等句暂按字面保留。

无妄：不虚妄则开创而通达，利于守正；若行为不正便会有灾祸，不利于有所前往。
初九：不妄为，前往吉利。
六二：不耕种却期望收获，不开垦荒地却想得到熟田；这样反而利于有所前往。
六三：无妄之中出现灾祸。有人把牛系在这里，过路人牵走了牛，邑中居民却遭受灾祸。
九四：能够守正，没有灾咎。
九五：无妄之疾，不必用药，自会有喜事。
上九：妄为而行会招致灾祸，没有什么利益。"""),
    Entry("易经", "上经 · 第26卦 · 大畜", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "大畜：利貞，不家食吉，利涉大川。\n初九：有厲利已。\n九二：輿說輹。\n九三：良馬逐，利艱貞。曰閑輿衛，利有攸往。\n六四：童牛之牿，元吉。\n六五：豶豕之牙，吉。\n上九：何天之衢，亨。", """译文说明：以下为本项目工作直译初稿。“不家食”常联系仕进受禄解释，但此处仍保留字面含义；“何天之衢”句不强定唯一训释。

大畜：利于守正；不在家中吃饭而出外任事，吉；利于涉越大河。
初九：有危险，宜停止前进。
九二：车轮的轴承部件脱落。
九三：良马追逐前行；利于在艰难中守正。每日练习车马与防卫之事，利于有所前往。
六四：给小牛戴上横木以防抵触，大吉。
六五：阉猪的獠牙受到控制，吉。
上九：通达如同上天的大道，亨通。"""),
    Entry("易经", "上经 · 第27卦 · 颐", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "頤：貞吉。觀頤，自求口實。\n初九：舍爾靈龜，觀我朵頤，凶。\n六二：顛頤，拂經，于丘頤，征凶。\n六三：拂頤，貞凶，十年勿用，无攸利。\n六四：顛頤吉，虎視眈眈，其欲逐逐，无咎。\n六五：拂經，居貞吉，不可涉大川。\n上九：由頤，厲吉，利涉大川。", """译文说明：以下为本项目工作直译初稿。“颐”涉及颌部、养育与口食之义；“拂经”“顛颐”等语保留训释余地。

颐：守正吉利。观察颐养之道，寻求自己赖以生存的食物。
初九：舍弃自己的灵龟，却羡慕地看着别人享用食物，凶。
六二：颠倒了颐养的方式，违背常理；向高处求养，前往则凶。
六三：违背颐养之道，守正也凶；十年之内不可有所作为，没有什么利益。
六四：颠倒求养的方向，结果吉利；像老虎一样目光专注，欲望持续不止，没有灾咎。
六五：违背常理，安居守正则吉；不可涉越大河。
上九：由此得到颐养，虽有危险却吉利；利于涉越大河。"""),
    Entry("易经", "上经 · 第28卦 · 大过", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "大過：棟橈，利有攸往，亨。\n初六：藉用白茅，无咎。\n九二：枯楊生稊，老夫得其女妻，无不利。\n九三：棟橈，凶。\n九四：棟隆，吉。有它吝。\n九五：枯楊生華，老婦得其士夫，无咎无譽。\n上六：過涉滅頂，凶，无咎。", """译文说明：以下为本项目工作直译初稿。本卦以栋梁承重为喻；“稊”“士夫”“过涉灭顶”等词按字面译出，不延伸为单一历史故事。

大过：栋梁弯曲；利于有所前往，亨通。
初六：铺垫时使用白茅草，没有灾咎。
九二：枯萎的杨树长出新芽；老年男子娶得年轻妻子，没有什么不利。
九三：栋梁弯曲，凶。
九四：栋梁隆起而坚固，吉；若另有旁生枝节，则有困难。
九五：枯萎的杨树开出花朵；老妇人得到年轻丈夫，没有灾咎，也没有声誉。
上六：徒步涉水，水深得淹没头顶，凶；但没有灾咎。"""),
    Entry("易经", "上经 · 第29卦 · 坎", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "習坎：有孚，維心亨。行有尚。\n初六：習坎，入于坎窞，凶。\n九二：坎有險，求小得。\n六三：來之坎坎，險且枕，入于坎窞，勿用。\n六四：樽酒簋貳，用缶，納約自牖，終无咎。\n九五：坎不盈，祗既平，无咎。\n上六：係用徽纆，寘于叢棘，三歲不得，凶。", """译文说明：以下为本项目工作直译初稿。“坎窞”“祗既平”等语有不同解释；本条保留险陷意象，不把它等同于某一具体事件。

习坎：反复处于险境，仍有诚信；只要内心通达，行动便有可取之处。
初六：反复陷入险境，进入坑穴深处，凶。
九二：险坑之中仍有危险，只能求得小的收获。
六三：来去都遇到险坑，危险且难以安身；又陷入坑穴深处，不宜行动。
六四：一樽酒、两簋食物，以瓦缶盛装；从窗户递入简约的祭品，最终没有灾咎。
九五：险坑中的水还没有盈满，逐渐达到平稳，没有灾咎。
上六：被绳索捆系，安置在荆棘丛中，三年不得解脱，凶。"""),
    Entry("易经", "上经 · 第30卦 · 离", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "離：利貞。亨。畜牝牛，吉。\n初九：履錯然，敬之无咎。\n六二：黃離，元吉。\n九三：日昃之離，不鼓缶而歌，則大耋之嗟，凶。\n九四：突如其來如，焚如，死如，棄如。\n六五：出涕沱若，戚嗟若，吉。\n上九：王用出征，有嘉折首，獲匪其醜，无咎。", """译文说明：以下为本项目工作直译初稿。“离”兼有附丽、光明之义；“履错然”“黄离”等语仍须结合古注校读。

离：利于守正，亨通；蓄养母牛，吉。
初九：脚步交错而行，应当恭敬谨慎，没有灾咎。
六二：黄色的光明，大吉。
九三：太阳偏西时的光景；若不击打瓦缶而歌，便会发出老年人般的叹息，凶。
九四：突然来到，来势猛烈，如同焚烧；又迅速消亡，遭到弃置。
六五：泪水不断流下，忧戚叹息，吉。
上九：君王出兵征讨，能够嘉奖并制服首恶，俘获的并非其同类之众，没有灾咎。"""),
    Entry("易经", "下经 · 第31卦 · 咸", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "咸：亨。利貞。取女吉。\n初六：咸其拇。\n六二：咸其腓，凶，居吉。\n九三：咸其股，執其隨，往吝。\n九四：貞吉悔亡，憧憧往來，朋從爾思。\n九五：咸其脢，无悔。\n上六：咸其輔，頰，舌。", """译文说明：以下为本项目工作直译初稿。“咸”有感应、相互影响之义；爻辞依身体部位次第呈现，具体象征解释不强行写入译文。

咸：亨通，利于守正；娶妻吉。
初六：感应发生在脚拇趾。
六二：感应发生在小腿肚，凶；安居不动则吉。
九三：感应发生在大腿，执意跟随其所牵引的对象，前往会有困难。
九四：守正吉利，悔恨消失；心思往来不定，朋友会顺从你的想法。
九五：感应发生在脊背肉处，没有悔恨。
上六：感应发生在面颊、两颊与舌头。"""),
    Entry("易经", "下经 · 第32卦 · 恒", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "恆：亨，无咎。利貞，利有攸往。\n初六：浚恆，貞凶，无攸利。\n九二：悔亡。\n九三：不恆其德，或承之羞，貞吝。\n九四：田无禽。\n六五：恆其德，貞，婦人吉，夫子凶。\n上六：振恆，凶。", """译文说明：以下为本项目工作直译初稿。“恒”指恒常、持久；“浚恒”“振恒”等词存在训释分歧，暂按字面呈现。

恒：亨通，没有灾咎；利于守正，利于有所前往。
初六：一味追求深远长久，守正也凶，没有什么利益。
九二：悔恨消失。
九三：不能恒久保持自己的德行，或许会因此蒙受羞辱；守正也有困难。
九四：田猎时没有捕获禽兽。
六五：恒久保持自己的德行，守正；妇人吉，丈夫则凶。
上六：动荡不定地追求恒常，凶。"""),
    Entry("易经", "下经 · 第33卦 · 遁", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "遯：亨。小利貞。\n初六：遯尾，厲，勿用有攸往。\n六二：執之用黃牛之革，莫之勝說。\n九三：系遯，有疾厲，畜臣妾吉。\n九四：好遯君子吉，小人否。\n九五：嘉遯，貞吉。\n上九：肥遯，无不利。", """译文说明：以下为本项目工作直译初稿。“遁”指退避；“肥遁”等语带有古代修辞色彩，暂按字面与语境合译。

遁：亨通；小事上利于守正。
初六：退避得太迟，处境危险；不宜有所前往。
六二：用黄色牛皮牢牢系住，没有人能够解开。
九三：被牵系而不能退避，有疾病与危险；蓄养臣仆、侍妾则吉。
九四：能够从容退避，君子吉，小人则不然。
九五：美好地退避，守正吉利。
上九：宽裕从容地退避，没有什么不利。"""),
    Entry("易经", "下经 · 第34卦 · 大壮", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "大壯：利貞。\n初九：壯于趾，征凶，有孚。\n九二：貞吉。\n九三：小人用壯，君子用罔，貞厲。羝羊觸藩，羸其角。\n九四：貞吉悔亡，藩決不羸，壯于大輿之輹。\n六五：喪羊于易，无悔。\n上六：羝羊觸藩，不能退，不能遂，无攸利，艱則吉。", """译文说明：以下为本项目工作直译初稿。本卦以强盛、羊触藩篱为主要意象；“用罔”“藩决不羸”等词义仍须对照古注。

大壮：利于守正。
初九：强盛表现在脚趾上；贸然前往凶，但心中有诚信。
九二：守正吉利。
九三：小人凭借强力行事，君子则不妄用强力；守正仍有危险。公羊触碰藩篱，角被缠住。
九四：守正吉利，悔恨消失；藩篱破开，角不再受困，力量如同大车的车轴。
六五：在平易之处失去羊，没有悔恨。
上六：公羊触碰藩篱，不能后退，也不能前进，没有什么利益；若能在艰难中守正，则吉。"""),
    Entry("易经", "下经 · 第35卦 · 晋", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "晉：康侯用錫馬蕃庶，晝日三接。\n初六：晉如，摧如，貞吉。罔孚，裕无咎。\n六二：晉如，愁如，貞吉。受茲介福，于其王母。\n六三：眾允，悔亡。\n九四：晉如鼫鼠，貞厲。\n六五：悔亡，失得勿恤，往吉无不利。\n上九：晉其角，維用伐邑，厲吉无咎，貞吝。", """译文说明：以下为本项目工作直译初稿。“康侯”“蕃庶”“鼫鼠”等词涉及古代爵称、礼制与物名，译文保留原有意象。

晋：康侯得到君王赐予众多马匹，一日之内多次受到接见。
初六：前进又受挫，守正吉利；即使未获信任，宽裕从容也没有灾咎。
六二：前进时忧愁不安，守正吉利；从王母那里接受重大福祉。
六三：众人都表示信任，悔恨消失。
九四：前进时像鼫鼠一样贪惧，守正仍有危险。
六五：悔恨消失；得失都不必忧虑，前往吉利，没有什么不利。
上九：前进到极处，如同角顶突出；只适合用来征伐自己的城邑，虽危险而吉、没有灾咎，守正却有困难。"""),
    Entry("易经", "下经 · 第36卦 · 明夷", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "明夷：利艱貞。\n初九：明夷于飛，垂其翼。君子于行，三日不食，有攸往，主人有言。\n六二：明夷，夷于左股，用拯馬壯，吉。\n九三：明夷于南狩，得其大首，不可疾貞。\n六四：入于左腹，獲明夷之心，于出門庭。\n六五：箕子之明夷，利貞。\n上六：不明晦，初登于天，后入于地。", """译文说明：以下为本项目工作直译初稿。“明夷”指光明受损；本卦叙事意象与历史人物箕子相关，但不把爻辞直接等同于某一史事。

明夷：利于在艰难中守正。
初九：光明受损，如鸟飞行而垂下翅膀。君子出行，三天没有吃饭；有所前往，主人会有言语责难。
六二：光明受损，伤及左大腿；借助强壮的马匹救助，吉。
九三：光明受损时向南方出猎，捕获首领；不可急于求取正固。
六四：进入左腹之中，了解光明受损者的内心，然后走出门庭。
六五：像箕子那样处于光明受损之时，利于守正。
上六：昏暗而不明，起初升上天空，后来又落入地下。"""),
    Entry("易经", "下经 · 第37卦 · 家人", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "家人：利女貞。\n初九：閑有家，悔亡。\n六二：无攸遂，在中饋，貞吉。\n九三：家人嗃嗃，悔厲吉；婦子嘻嘻，終吝。\n六四：富家，大吉。\n九五：王假有家，勿恤。吉。\n上九：有孚威如，終吉。", """译文说明：以下为本项目工作直译初稿。本卦讨论家庭秩序；译文只解释字面关系，不把古代家庭制度直接当作现代规范。

家人：女子守正有利。
初九：在家中建立规矩，悔恨消失。
六二：没有什么需要自行专断的事；主持家中饮食事务，守正吉利。
九三：家人严厉而有威仪，虽有悔恨与危险，最终吉利；若妇人与孩子嬉闹失度，最终会有困难。
六四：使家庭富足，大吉。
九五：君王使家庭和睦，不必忧虑，吉。
上九：心怀诚信而有威严，最终吉利。"""),
    Entry("易经", "下经 · 第38卦 · 睽", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "睽：小事吉。\n初九：悔亡，喪馬勿逐，自復；見惡人无咎。\n九二：遇主于巷，无咎。\n六三：見輿曳，其牛掣，其人天且劓，无初有終。\n九四：睽孤，遇元夫，交孚，厲无咎。\n六五：悔亡，厥宗噬膚，往何咎。\n上九：睽孤，見豕負涂，載鬼一車，先張之弧，后說之弧，匪寇婚媾，往遇雨則吉。", """译文说明：以下为本项目工作直译初稿。“睽”指乖离、相背；“天且劓”等古代刑名只按原文作简要说明，不扩写具体细节。

睽：小事吉利。
初九：悔恨消失；马丢失了，不必追赶，它会自己返回；遇见品行不好的人，也没有灾咎。
九二：在小巷中遇到主人，没有灾咎。
六三：看见车被拖住，牛受到牵制，车上的人遭受黥面、割鼻一类刑罚；起初不顺，最终会有结果。
九四：孤立无援时，遇到一位志同道合的人，彼此以诚信相交；虽有危险，没有灾咎。
六五：悔恨消失；同宗之人如咬到柔软的肉一样容易相合，前往有什么灾咎？
上九：孤立无援，看见猪身上沾满污泥，又看见一车仿佛载着鬼怪；先张弓戒备，后来放下弓。来者不是寇盗，而是求婚者；前往遇雨，便吉利。"""),
    Entry("易经", "下经 · 第39卦 · 蹇", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "蹇：利西南，不利東北；利見大人，貞吉。\n初六：往蹇，來譽。\n六二：王臣蹇蹇，匪躬之故。\n九三：往蹇來反。\n六四：往蹇來連。\n九五：大蹇朋來。\n上六：往蹇來碩，吉；利見大人。", """译文说明：以下为本项目工作直译初稿。“蹇”指艰难阻滞；“来誉”“来硕”等语义有不同解释，暂按行动方向呈现。

蹇：向西南有利，向东北不利；利于见到大人，守正吉利。
初六：前往会遇到艰难，返回则能得到称誉。
六二：王的臣子艰难又艰难，并不是为了自身的缘故。
九三：前往遇到艰难，返回反而合宜。
六四：前往遇到艰难，返回则能与同伴相连。
九五：处于极大的艰难之中，朋友前来相助。
上六：前往遇到艰难，返回则有所得；吉，利于见到大人。"""),
    Entry("易经", "下经 · 第40卦 · 解", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "解：利西南，无所往，其來復吉。有攸往，夙吉。\n初六：无咎。\n九二：田獲三狐，得黃矢，貞吉。\n六三：負且乘，致寇至，貞吝。\n九四：解而拇，朋至斯孚。\n六五：君子維有解，吉；有孚于小人。\n上六：公用射隼，于高墉之上，獲之，无不利。", """译文说明：以下为本项目工作直译初稿。“解”指解除、缓解；“解而拇”“田获三狐”等语保留其象征性，不将其硬套为具体历史事件。

解：向西南有利；若无事可往，返回原处吉；若确有前往之事，及早行动吉。
初六：没有灾咎。
九二：田猎捕获三只狐狸，得到黄色箭矢；守正吉利。
六三：背负物品却乘坐车舆，招致寇盗到来；守正也有困难。
九四：解除脚拇趾上的牵系，朋友便会到来并以诚信相待。
六五：君子能够解除困局，吉；也能使小人心怀诚信。
上六：公侯在高高的城墙上射下鹰隼，捕获它，没有什么不利。"""),
    Entry("易经", "下经 · 第41卦 · 损", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "損：有孚，元吉。无咎，可貞，利有攸往。曷之用？二簋可用享。\n初九：已事遄往，无咎，酌損之。\n九二：利貞，征凶，弗損益之。\n六三：三人行，則損一人；一人行，則得其友。\n六四：損其疾，使遄有喜，无咎。\n六五：或益之，十朋之龜弗克違，元吉。\n上九：弗損益之，无咎，貞吉，利有攸往，得臣无家。", """译文说明：以下为本项目工作直译初稿。“损”“益”涉及减损、增益及祭祀礼制；“十朋之龟”等古代价值单位不作现代换算。

损：心怀诚信，大吉，没有灾咎；可以守正，利于有所前往。用什么来祭祀？两簋简约的祭品也可以用于享祭。
初九：事情已经办完，便迅速前往；没有灾咎，应斟酌减损的程度。
九二：利于守正；贸然前往凶，不应减损，反而应当有所增益。
六三：三个人同行，就会减少一个人；一个人独行，反而会得到朋友。
六四：减除自己的疾病，使它迅速好转并带来喜悦，没有灾咎。
六五：有人赠予价值极高的灵龟，这种赠予无法推辞，大吉。
上九：不再减损，反而有所增益；没有灾咎，守正吉利，利于有所前往；得到臣属而不以私家为念。"""),
    Entry("易经", "下经 · 第42卦 · 益", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "益：利有攸往。利涉大川。\n初九：利用為大作，元吉，无咎。\n六二：或益之，十朋之龜弗克違，永貞吉。王用享于帝，吉。\n六三：益之用凶事，无咎。有孚中行，告公用圭。\n六四：中行，告公從。利用為依遷國。\n九五：有孚惠心，勿問元吉。有孚惠我德。\n上九：莫益之，或擊之，立心勿恆，凶。", """译文说明：以下为本项目工作直译初稿。“凶事”“中行”“迁国”等语涉及古代政治与礼制背景，暂不补入未经证实的具体史事。

益：利于有所前往，利于涉越大河。
初九：利于开展大规模事业，大吉，没有灾咎。
六二：有人赠予价值极高的灵龟，这种赠予无法推辞；长久守正吉利。君王用它祭享上帝，吉。
六三：因凶险之事而得到增益，没有灾咎。心怀诚信，行于中道，向公侯进献玉圭。
六四：行于中道，向公侯禀告，公侯听从；利于依此迁移国都。
九五：心怀诚信并施惠于人，不必占问便是大吉；别人也会诚信地感念我的德惠。
上九：没有人来增益他，反而有人攻击他；立心不能恒久，凶。"""),
    Entry("易经", "下经 · 第43卦 · 夬", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "夬：揚于王庭，孚號，有厲，告自邑，不利即戎，利有攸往。\n初九：壯于前趾，往不勝為咎。\n九二：惕號，莫夜有戎，勿恤。\n九三：壯于頄，有凶。君子夬夬，獨行遇雨，若濡有慍，无咎。\n九四：臀无膚，其行次且。牽羊悔亡，聞言不信。\n九五：莧陸夬夬，中行无咎。\n上六：无號，終有凶。", """译文说明：以下为本项目工作直译初稿。“夬”有决断、决去之义；“莧陆”“夬夬”等词仍需结合版本和古注校释。

夬：在王庭公开宣示，诚信地发出号令；虽有危险，应从自己的城邑发布告令。不利于立即动用武力，利于有所前往。
初九：力量显现在前脚趾上；前往却不能取胜，便会成为过失。
九二：心怀警惕并发出号令；即使夜间有敌人来犯，也不必忧虑。
九三：力量显现在颧骨处，有凶险。君子果断决绝，独自行走时遇到雨；虽被淋湿而心怀不悦，没有灾咎。
九四：臀部没有皮肉，行走迟疑不前。牵着羊前行可以消除悔恨，但听到劝告仍不相信。
九五：像莧陆草一样决断地去除，行于中道，没有灾咎。
上六：不再发出号令，最终有凶险。"""),
    Entry("易经", "下经 · 第44卦 · 姤", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "姤：女壯，勿用取女。\n初六：系于金柅，貞吉，有攸往，見凶，羸豕孚踟躅。\n九二：包有魚，无咎，不利賓。\n九三：臀无膚，其行次且，厲，无大咎。\n九四：包无魚，起凶。\n九五：以杞包瓜，含章，有隕自天。\n上九：姤其角，吝，无咎。", """译文说明：以下为本项目工作直译初稿。“姤”指相遇；“女壮”是卦辞的古代象征表达，不将其改写成对现实女性的评价。

姤：女子强盛，不宜娶这样的女子。
初六：系在坚固的车止木上，守正吉利；若有所前往，会遇到凶险。瘦弱的猪却仍然躁动不安。
九二：包中有鱼，没有灾咎；不利于把鱼拿去招待宾客。
九三：臀部没有皮肉，行走迟疑；虽有危险，却没有大的灾咎。
九四：包中没有鱼，兴起便有凶险。
九五：用杞柳包裹瓜果，内含文采，仿佛有物从天而降。
上九：相遇在角上，虽有困难，却没有灾咎。"""),
    Entry("易经", "下经 · 第45卦 · 萃", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "萃：亨。王假有廟，利見大人，亨。利貞。用大牲吉，利有攸往。\n初六：有孚不終，乃亂乃萃，若號一握為笑，勿恤，往无咎。\n六二：引吉，无咎，孚乃利用禴。\n六三：萃如，嗟如，无攸利，往无咎，小吝。\n九四：大吉，无咎。\n九五：萃有位，无咎。匪孚，元永貞，悔亡。\n上六：齎咨涕洟，无咎。", """译文说明：以下为本项目工作直译初稿。“王假有庙”“禴”等为祭祀用语；此处只作简明释义，不擅自补写礼仪细节。

萃：亨通。君王来到宗庙，利于见到大人；亨通，利于守正。使用大牲祭祀吉，利于有所前往。
初六：有诚信却不能保持到底，于是纷乱而聚集；有人呼号，众人又转而欢笑，不必忧虑，前往没有灾咎。
六二：受到引导而吉，没有灾咎；有诚信，便可以用简约的祭品举行祭祀。
六三：聚集在一起，发出叹息；没有什么利益，前往虽没有灾咎，却有小困难。
九四：大吉，没有灾咎。
九五：聚集而居于尊位，没有灾咎；若未获信任，仍须长久守正，悔恨才能消失。
上六：叹息哭泣，眼泪鼻涕交下；没有灾咎。"""),
    Entry("易经", "下经 · 第46卦 · 升", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "升：元亨，用見大人，勿恤，南征吉。\n初六：允升，大吉。\n九二：孚乃利用禴，无咎。\n九三：升虛邑。\n六四：王用亨于岐山，吉无咎。\n六五：貞吉，升階。\n上六：冥升，利于不息之貞。", """译文说明：以下为本项目工作直译初稿。“用见大人”“用亨于岐山”等语涉及古代祭礼与政治语境，暂保留其原有表达。

升：大为亨通；宜于见到大人，不必忧虑；向南行进吉利。
初六：诚信地上升，大吉。
九二：有诚信，便可以用简约的祭品举行祭祀；没有灾咎。
九三：升入空虚的城邑。
六四：君王在岐山举行享祭，吉，没有灾咎。
六五：守正吉利，逐级登上台阶。
上六：昏暗地上升，利于不停息地守正。"""),
    Entry("易经", "下经 · 第47卦 · 困", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "困：亨，貞大人吉，无咎，有言不信。\n初六：臀困于株木，入于幽谷，三歲不覿。\n九二：困于酒食，朱紱方來，利用亨祀，征凶，无咎。\n六三：困于石，據于蒺藜，入于其宮，不見其妻，凶。\n九四：來徐徐，困于金車，吝，有終。\n九五：劓刖，困于赤紱，乃徐有說，利用祭祀。\n上六：困于葛藟，于臲卼，曰動悔。有悔，征吉。", """译文说明：以下为本项目工作直译初稿。本卦多用困顿、受阻的象征；“朱绂”“赤绂”“臲卼”等词保留古代服饰与状态意象。

困：亨通；守正而有德望的大人吉，没有灾咎；即使有所言说，也未必有人相信。
初六：臀部困在枯树根上，又进入幽暗山谷，三年不能见人。
九二：困于酒食之中，穿朱色蔽膝服饰的人刚刚到来；利于举行祭祀，贸然出征凶，但没有灾咎。
六三：困在石头之间，又倚靠蒺藜；进入自己的宫室，却见不到妻子，凶。
九四：缓缓而来，困在金色车乘之中；虽有困难，最终会有结果。
九五：受到劓刑、刖刑一类刑罚，又困于赤色服饰之中；随后渐渐脱困，利于举行祭祀。
上六：困在葛藤之中，处境动荡不安；若有所行动便会后悔。虽有悔恨，前往征行仍吉。"""),
    Entry("易经", "下经 · 第48卦 · 井", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "井：改邑不改井，无喪无得，往來井井。汔至亦未繘井。羸其瓶，凶。\n初六：井泥不食，舊井无禽。\n九二：井谷射鮒，瓮敝漏。\n九三：井渫不食，為我心惻，可用汲，王明，并受其福。\n六四：井甃，无咎。\n九五：井冽，寒泉食。\n上六：井收勿幕，有孚元吉。", """译文说明：以下为本项目工作直译初稿。本卦以井为喻；“繘井”“羸其瓶”等词涉及汲水器具，译文按字面处理。

井：城邑可以改变，井却不会改变；既没有失去，也没有增加，人们来来往往都依靠这口井。水已经快要汲到，却还没有把井绳完全提出；水瓶破裂，凶。
初六：井中有泥，不能饮用；旧井甚至没有禽鸟来饮水。
九二：井底的水只能射到小鱼，水瓮破损而漏水。
九三：井水已经淘洗清洁，却无人饮用；我因此感到悲伤。可以汲取井水，若君王明察，众人便都能得到福祉。
六四：修砌井壁，没有灾咎。
九五：井水清冽，寒冷的泉水可以饮用。
上六：井水已经汲取，不要遮盖井口；心怀诚信，大吉。"""),
    Entry("易经", "下经 · 第49卦 · 革", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "革：巳日乃孚，元亨。利貞。悔亡。\n初九：鞏用黃牛之革。\n六二：巳日乃革之，征吉，无咎。\n九三：征凶，貞厲，革言三就，有孚。\n九四：悔亡，有孚改命，吉。\n九五：大人虎變，未占有孚。\n上六：君子豹變，小人革面，征凶，居貞吉。", """译文说明：以下为本项目工作直译初稿。“巳日”涉及择日语义；“革言三就”等句仍有训释分歧，暂不补写具体制度背景。

革：到了巳日才取得信任，开创而通达；利于守正，悔恨消失。
初九：用黄色牛皮牢固地束缚起来。
六二：到了巳日再进行变革，前往吉利，没有灾咎。
九三：贸然前往凶，守正也有危险；变革的言论经过多次商议，取得诚信。
九四：悔恨消失；心怀诚信而改变旧命令，吉。
九五：大人如虎纹般焕然变化，不必占问便已获得信任。
上六：君子如豹纹般变化，小人也改变面貌；贸然前往凶，安居守正吉。"""),
    Entry("易经", "下经 · 第50卦 · 鼎", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "鼎：元吉，亨。\n初六：鼎顛趾，利出否，得妾以其子，无咎。\n九二：鼎有實，我仇有疾，不我能即，吉。\n九三：鼎耳革，其行塞，雉膏不食，方雨虧悔，終吉。\n九四：鼎折足，覆公餗，其形渥，凶。\n六五：鼎黃耳金鉉，利貞。\n上九：鼎玉鉉，大吉，无不利。", """译文说明：以下为本项目工作直译初稿。本卦以鼎器为核心意象；“公餗”“金铉”“玉铉”等器物与礼制词按字面翻译。

鼎：大吉，亨通。
初六：鼎足颠倒，利于倒出其中不洁之物；得到妾并生育儿子，没有灾咎。
九二：鼎中有食物，我的对手有疾病，不能接近我，吉。
九三：鼎耳发生变异，搬运受阻；鼎中的雉肉无人享用，后来遇雨，悔恨减少，最终吉利。
九四：鼎足折断，倾覆了公侯的食物，器物沾污，凶。
六五：鼎有黄色的耳和金属制成的横杠，利于守正。
上九：鼎有玉制的横杠，大吉，没有什么不利。"""),
    Entry("易经", "下经 · 第51卦 · 震", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "震：亨。震來虩虩，笑言啞啞。震驚百里，不喪匕鬯。\n初九：震來虩虩，后笑言啞啞，吉。\n六二：震來厲，億喪貝，躋于九陵，勿逐，七日得。\n六三：震蘇蘇，震行无眚。\n九四：震遂泥。\n六五：震往來厲，億无喪，有事。\n上六：震索索，視矍矍，征凶。震不于其躬，于其鄰，无咎。婚媾有言。", """译文说明：以下为本项目工作直译初稿。“虩虩”“啞啞”等为拟声词；“億”在相关爻辞中的训释不一，译文不强行确定为数量。

震：亨通。雷声到来令人恐惧戒慎，随后又能谈笑自若。雷声震惊百里，却没有丢失祭祀用的匙与酒器。
初九：雷声到来令人恐惧，随后又能谈笑自若，吉。
六二：雷声到来，处境危险；仿佛失去财物，登上九重高陵，不必追寻，七日后会得到。
六三：雷声使人惊惧不安；因雷震而行动，没有灾祸。
九四：雷声震动，却陷入泥中。
六五：雷声往来，处境危险；虽有忧惧，却没有失去应有之物，仍有事情要做。
上六：雷声使人惊恐战栗，目光惊惶；贸然前往凶。雷没有震到自己，却震到邻人，没有灾咎；婚姻之事会有言语争议。"""),
    Entry("易经", "下经 · 第52卦 · 艮", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "艮：艮其背，不獲其身，行其庭，不見其人，无咎。\n初六：艮其趾，无咎，利永貞。\n六二：艮其腓，不拯其隨，其心不快。\n九三：艮其限，列其夤，厲薰心。\n六四：艮其身，无咎。\n六五：艮其輔，言有序，悔亡。\n上九：敦艮，吉。", """译文说明：以下为本项目工作直译初稿。“艮”指止；“腓”“夤”“辅”等身体部位词按常见训释译出，具体象征意义另待注疏说明。

艮：止于背部，不再感受到自身；行走在庭院中，也看不见他人，没有灾咎。
初六：止于脚趾，没有灾咎，利于长久守正。
六二：止于小腿肚，不能救助所跟随的人，心中不快。
九三：止于腰部，脊肉受到牵连；处境危险，忧虑灼心。
六四：止于自身，没有灾咎。
六五：止于面颊与口辅，说话有条理，悔恨消失。
上九：敦厚地止定不动，吉。"""),
    Entry("易经", "下经 · 第53卦 · 渐", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "漸：女歸吉，利貞。\n初六：鴻漸于干，小子厲，有言，无咎。\n六二：鴻漸于磐，飲食衎衎，吉。\n九三：鴻漸于陸，夫征不復，婦孕不育，凶；利禦寇。\n六四：鴻漸于木，或得其桷，无咎。\n九五：鴻漸于陵，婦三歲不孕，終莫之勝，吉。\n上九：鴻漸于陸，其羽可用為儀，吉。", """译文说明：以下为本项目工作直译初稿。本卦以鸿雁渐次前行为象；“干”“磐”“陆”“桷”等地貌、木构词按常见字义翻译。

渐：女子出嫁吉利，利于守正。
初六：鸿雁渐渐飞到水边高地；年少者处境危险，虽有言语议论，没有灾咎。
六二：鸿雁渐渐飞到磐石上，饮食和乐，吉。
九三：鸿雁渐渐飞到高平之地；丈夫出征而不归，妻子怀孕却不能生育，凶；利于抵御寇盗。
六四：鸿雁渐渐飞到树上，或许能找到平直的枝桷，没有灾咎。
九五：鸿雁渐渐飞到山陵；妇人三年不能怀孕，最终没有人能胜过她，吉。
上九：鸿雁渐渐飞到高平之地，它的羽毛可以用来作为仪饰，吉。"""),
    Entry("易经", "下经 · 第54卦 · 归妹", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "歸妹：征凶，无攸利。\n初九：歸妹以娣，跛能履，征吉。\n九二：眇能視，利幽人之貞。\n六三：歸妹以須，反歸以娣。\n九四：歸妹愆期，遲歸有時。\n六五：帝乙歸妹，其君之袂，不如其娣之袂良，月幾望，吉。\n上六：女承筐无實，士刲羊无血，无攸利。", """译文说明：以下为本项目工作直译初稿。本卦涉及古代婚姻礼制；“娣”“须”等身份与词义应结合古代礼制文献另作考证。

归妹：前往凶，没有什么利益。
初九：女子出嫁时作为媵妾随行，脚跛却仍能行走；前往吉。
九二：眼睛不好却能看见，利于隐居之人守正。
六三：女子出嫁时等待时机，最终返回而作为媵妾。
九四：女子出嫁超过了约定期限，迟些出嫁自有合适的时机。
六五：帝乙嫁女；新娘所穿礼服的袖子，不如陪嫁女子的袖子华美；月亮几乎圆满，吉。
上六：女子捧着筐却没有东西，男子宰羊却没有血，没有什么利益。"""),
    Entry("易经", "下经 · 第55卦 · 丰", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "豐：亨。王假之，勿憂，宜日中。\n初九：遇其配主，雖旬无咎，往有尚。\n六二：豐其蔀，日中見斗，往得疑疾，有孚發若，吉。\n九三：豐其沛，日中見沫，折其右肱，无咎。\n九四：豐其蔀，日中見斗，遇其夷主，吉。\n六五：來章，有慶譽，吉。\n上六：豐其屋，蔀其家，窺其戶，闃其无人，三歲不觌，凶。", """译文说明：以下为本项目工作直译初稿。“丰”指盛大、丰盛；“蔀”“沛”“沫”等字的古义存在不同解释，译文保留遮蔽、光象等意象。

丰：亨通。君王来到这里，不必忧虑，宜于像太阳居中时那样明察。
初九：遇到与自己匹配的主人，即使时间相隔也没有灾咎；前往会受到尊尚。
六二：遮蔽极为浓密，正午时竟看见北斗；前往会招致疑忌之疾，但若诚信显现，吉。
九三：遮蔽广大，正午时看见微弱的光点；折断了右臂，没有灾咎。
九四：遮蔽浓密，正午时看见北斗；遇到与自己相匹配的主人，吉。
六五：光明与文采到来，有喜庆与称誉，吉。
上六：房屋高大，遮蔽了整个家；从门缝窥看，屋中寂静无人，三年不能见人，凶。"""),
    Entry("易经", "下经 · 第56卦 · 旅", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "旅：小亨，旅貞吉。\n初六：旅瑣瑣，斯其所取災。\n六二：旅即次，懷其資，得童僕貞。\n九三：旅焚其次，喪其童僕，貞厲。\n九四：旅于處，得其資斧，我心不快。\n六五：射雉一矢亡，終以譽命。\n上九：鳥焚其巢，旅人先笑后號咷。喪牛于易，凶。", """译文说明：以下为本项目工作直译初稿。“旅”指旅居、寄寓；“资斧”“誉命”等词保留古代旅人处境与礼命语境。

旅：小有亨通，旅居之人守正吉利。
初六：旅居之人琐碎拘谨，灾祸正是由此招来。
六二：旅居之人到达客舍，怀中带着资财，得到忠贞的童仆。
九三：旅居之人的客舍被焚毁，失去童仆；守正仍有危险。
九四：旅居之人暂时安顿下来，得到资财与工具，但心中并不快乐。
六五：射野鸡时失去一支箭，最终得到声誉与命令。
上九：鸟巢被焚毁，旅人起初发笑，后来放声哭号；在易地失去牛，凶。"""),
    Entry("易经", "下经 · 第57卦 · 巽", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "巽：小亨。利有攸往。利見大人。\n初六：進退，利武人之貞。\n九二：巽在牀下，用史巫紛若，吉无咎。\n九三：頻巽，吝。\n六四：悔亡，田獲三品。\n九五：貞吉悔亡，无不利。无初有終，先庚三日，后庚三日，吉。\n上九：巽在牀下，喪其資斧，貞凶。", """译文说明：以下为本项目工作直译初稿。“巽”有顺入、谦逊之义；“史巫纷若”“先庚三日，后庚三日”等语保留古代礼制与择日背景。

巽：小有亨通，利于有所前往，利于见到大人。
初六：进退不定；利于像武人一样坚定守正。
九二：谦顺地处于床下，任用众多史官与巫者，吉，没有灾咎。
九三：屡次卑顺退让，有困难。
六四：悔恨消失，田猎获得三种猎物。
九五：守正吉利，悔恨消失，没有什么不利。起初不顺，最终有成；在庚日前三日预先谋划，在庚日后三日继续谋划，吉。
上九：谦顺地处于床下，丧失资财与工具；守正仍凶。"""),
    Entry("易经", "下经 · 第58卦 · 兑", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "兌：亨。利貞。\n初九：和兌，吉。\n九二：孚兌，吉，悔亡。\n六三：來兌，凶。\n九四：商兌，未寧，介疾有喜。\n九五：孚于剝，有厲。\n上六：引兌。", """译文说明：以下为本项目工作直译初稿。“兑”有喜悦、言说之义；“孚于剥”等句义较难，暂不附会为具体人物或事件。

兑：亨通，利于守正。
初九：和悦地与人相处，吉。
九二：以诚信表达喜悦，吉，悔恨消失。
六三：前来求取欢悦，凶。
九四：商议如何取悦他人，内心尚未安定；若能隔绝疾患，便有喜事。
九五：诚信却附着于剥落之物，存在危险。
上六：引导他人进入喜悦之中。"""),
    Entry("易经", "下经 · 第59卦 · 涣", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "渙：亨。王假有廟，利涉大川，利貞。\n初六：用拯馬壯，吉。\n九二：渙奔其机，悔亡。\n六三：渙其躬，无悔。\n六四：渙其群，元吉。渙有丘，匪夷所思。\n九五：渙汗其大號，渙王居，无咎。\n上九：渙其血，去逖出，无咎。", """译文说明：以下为本项目工作直译初稿。“涣”指离散、涣散；“王假有庙”“涣汗其大号”等句涉及古代祭祀与政令语境。

涣：亨通。君王来到宗庙，利于涉越大河，利于守正。
初六：借助强壮的马匹救助，吉。
九二：涣散之时奔向安稳的凭依处，悔恨消失。
六三：使自身从涣散中解脱，没有悔恨。
六四：使群体涣散而重新分化，大吉；涣散之中形成丘聚，非一般人所能想到。
九五：发布宏大的政令，如同汗液发散；君王居于王位，没有灾咎。
上九：使血污散去，远离危险而脱出，没有灾咎。"""),
    Entry("易经", "下经 · 第60卦 · 节", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "節：亨。苦節不可貞。\n初九：不出戶庭，无咎。\n九二：不出門庭，凶。\n六三：不節若，則嗟若，无咎。\n六四：安節，亨。\n九五：甘節，吉；往有尚。\n上六：苦節，貞凶，悔亡。", """译文说明：以下为本项目工作直译初稿。“节”指节制、制度与限度；“苦节”强调过度严苛的节制，不宜理解为一味忍耐。

节：亨通。过于艰苦严苛的节制，不可长久守持。
初九：不走出内室庭院，没有灾咎。
九二：不走出门庭，凶。
六三：若不加节制，就会因此叹息；知道叹息缘由，便没有灾咎。
六四：安于适当的节制，亨通。
九五：甘美而适度的节制，吉；前往会受到尊尚。
上六：过于艰苦的节制，守正也凶；悔恨消失。"""),
    Entry("易经", "下经 · 第61卦 · 中孚", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "中孚：豚魚吉，利涉大川，利貞。\n初九：虞吉，有他不燕。\n九二：鳴鶴在陰，其子和之，我有好爵，吾與爾靡之。\n六三：得敵，或鼓或罷，或泣或歌。\n六四：月几望，馬匹亡，无咎。\n九五：有孚攣如，无咎。\n上九：翰音登于天，貞凶。", """译文说明：以下为本项目工作直译初稿。“中孚”指内心诚信；“豚鱼”“靡之”等词的象征义仍需结合古注校释。

中孚：诚信感通于豚与鱼，吉；利于涉越大河，利于守正。
初九：心中安定，吉；若另有他念，便不能安宁。
九二：鹤在树荫中鸣叫，小鹤应声相和。我有美酒佳肴，愿与你共同分享。
六三：遇到敌对之人，时而击鼓进攻，时而停止；时而哭泣，时而歌唱。
六四：月亮几乎圆满，马匹失去同伴，没有灾咎。
九五：诚信紧密相连，没有灾咎。
上九：鸣叫的声音直达天空，守正则凶。"""),
    Entry("易经", "下经 · 第62卦 · 小过", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "小過：亨。利貞。可小事，不可大事。飛鳥遺之音，不宜上宜下，大吉。\n初六：飛鳥以凶。\n六二：過其祖，遇其妣；不及其君，遇其臣；无咎。\n九三：弗過防之，從或戕之，凶。\n九四：无咎，弗過遇之。往厲必戒，勿用永貞。\n六五：密云不雨，自我西郊，公弋取彼在穴。\n上六：弗遇過之，飛鳥離之，凶，是謂災眚。", """译文说明：以下为本项目工作直译初稿。“小过”指小有逾越；“不宜上宜下”等句按原文方向意象翻译，不扩写成单一占验结论。

小过：亨通，利于守正。可以做小事，不可以做大事。飞鸟留下声音，不宜向上，宜于向下，大吉。
初六：飞鸟因贸然行动而凶。
六二：越过祖父而遇见祖母；没有遇见君王，却遇见了臣子，没有灾咎。
九三：没有越过危险却未加防备，跟从者可能伤害自己，凶。
九四：没有灾咎；不强行越过，反而能遇到合宜之事。前往有危险，必须戒慎；不要长久固执于一端。
六五：浓云密布却没有降雨，云气来自我方西郊；公侯用箭射取藏在穴中的猎物。
上六：没有遇到而强行越过，飞鸟因此陷入罗网，凶；这就是灾祸。"""),
    Entry("易经", "下经 · 第63卦 · 既济", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "既濟：亨小。利貞。初吉終亂。\n初九：曳其輪，濡其尾，无咎。\n六二：婦喪其茀，勿逐，七日得。\n九三：高宗伐鬼方，三年克之，小人勿用。\n六四：繻有衣袽，終日戒。\n九五：東鄰殺牛，不如西鄰之禴祭，實受其福。\n上六：濡其首，厲。", """译文说明：以下为本项目工作直译初稿。“既济”指事情已经渡过、完成；本卦反复提示完成之后仍须戒慎。

既济：小事亨通，利于守正；起初吉利，最终可能陷入纷乱。
初九：拖住车轮，沾湿了尾巴，没有灾咎。
六二：妇人丢失了头饰，不必追寻，七日后会得到。
九三：高宗征伐鬼方，经过三年才取胜；不可任用小人。
六四：衣服破损，需要用旧衣絮补缀；整日保持戒备。
九五：东邻杀牛举行盛大祭祀，不如西邻举行简约的禴祭；后者实际得到福祉。
上六：头部被水浸湿，处境危险。"""),
    Entry("易经", "下经 · 第64卦 · 未济", "《周易》通行本经文；据维基文库《周易》各卦页面“易經”段整理。当前仅收卦辞、六爻辞及乾用九／坤用六；未混入彖传、象传或后世注释。尚未完成与帛书、敦煌本及各刻本的逐字校勘。", "未濟：亨。小狐汔濟，濡其尾，无攸利。\n初六：濡其尾，吝。\n九二：曳其輪，貞吉。\n六三：未濟，征凶，利涉大川。\n九四：貞吉，悔亡，震用伐鬼方，三年有賞于大國。\n六五：貞吉，无悔，君子之光，有孚，吉。\n上九：有孚于飲酒，无咎，濡其首，有孚失是。", """译文说明：以下为本项目工作直译初稿。“未济”指尚未渡过、尚未完成；“小狐汔济”等句以渡河意象贯穿全卦。

未济：亨通。小狐狸快要渡过河时，尾巴却被水沾湿，没有什么利益。
初六：尾巴被水沾湿，有困难。
九二：拖住车轮，守正吉利。
六三：尚未渡过，贸然前往凶；利于涉越大河。
九四：守正吉利，悔恨消失；以强大的力量征伐鬼方，经过三年，在大国获得奖赏。
六五：守正吉利，没有悔恨；君子的光明德行显现，心怀诚信，吉。
上九：心怀诚信而饮酒，没有灾咎；但若沉溺其中，头部被水浸湿，便是诚信失去了准则。""")
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
事物不能穷尽，所以接着是未济卦；到这里，卦序终结。"""),
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
夬就是决断，是刚强者决去柔弱者。君子的道路增长，小人的道路消退。"""),
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
"""),
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

校读提示：第十一章“敷”“馵足”“的颡”“近利市三倍”“黔喙之属”等词，古注训释并不完全一致；本译文仅作贴近字面的工作转述，后续应逐词列出训诂来源。""")
)

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
说明：此为可检索数字文本整理，不代替原刻本影像校勘；传世异文、古文字释读和断句仍待逐项核验。"""),
    Entry("相关古籍", "《周礼·春官·大卜》三易条", "《周礼·春官宗伯》大卜职文节录；用于说明《连山》《归藏》《周易》三易的传世文献记载。此处采用《周礼正义》相关页面可核验的经文，注疏另行区分。", """经文：

大卜掌三兆之法，一曰玉兆，二曰瓦兆，三曰原兆。其经兆之体，皆百有二十，其颂皆千有二百。掌三易之法，一曰连山，二曰归藏，三曰周易。其经卦皆八，其别皆六十有四。掌三梦之法，一曰致梦，二曰觭梦，三曰咸陟。其经运十，其别九十。以邦事作龟之八命，一曰征，二曰象，三曰与，四曰谋，五曰果，六曰至，七曰雨，八曰廖。以八命者赞三兆、三易、三梦之占，以观国家之吉凶，以诏救政。凡国大贞，卜立君，卜大封，则□高作龟。大祭祀，则□高命龟。凡小事，莅卜。国大迁、大师，则贞龟。凡旅，陈龟。凡丧事，命龟。

校勘与缺字说明：
1. “掌三易之法”以下为本专题关注重点；“二曰归藏”在部分数字文本出现“二日归藏”，本条按文义与通行经文作“二曰”，仍待影印本核字。
2. “□高”是当前数字文本无法识别的字位，本项目保留缺字符号，不据职官语境擅自补字。
3. 《周礼正义》页面附有郑注、贾公彦疏等解释，不能与《周礼》经文混排。

来源：
《周礼正义》相关篇章：https://zh.wikisource.org/zh-hans/周禮正義/47
另见《周礼·春官·大卜》文本整理：https://www.quanxue.cn/ct_rujia/zhouli/zhouli30.html

状态：本条为大卜职文节录，非《周礼》全篇。"""),
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
    Entry("历代注疏", "魏王弼、晋韩康伯《周易注》", "注释《周易》上下经及彖、象、文言等；王弼注经，韩康伯补注系辞、说卦、序卦、杂卦。当前先建立原典版本入口，全文离线导入状态：未完成。", """版本与范围：
王弼《周易注》是魏晋易学的重要注本。传世注本中，王弼注经及彖、象、文言；韩康伯补注《系辞传》《说卦传》《序卦传》《杂卦传》。不同合卷本的卷数、篇章编排可能不同，不能把后世合刊卷次误当作原始写作次序。

可核验原典入口：
《周易注疏》（四库全书本，王弼、韩康伯注，陆德明音义，孔颖达疏）：https://zh.wikisource.org/zh-hans/周易正义_(四庫全書本)
《周易略例》王弼撰：https://zh.wikisource.org/wiki/周易略例
《周易略例》四库本及邢璹注：https://zh.wikisource.org/wiki/周易注疏_(四庫全書本)/周易略例

内容状态：
本条是原典书目与版本入口，不是王弼注全文。王弼注逐卦正文、韩康伯补注、陆德明音义均待按卷导入；在导入前不标记为离线全文已收录。"""),
    Entry("历代注疏", "《周易正义》", "魏王弼、晋韩康伯注，唐陆德明音义，唐孔颖达疏；四库全书本底本为景印文渊阁四库全书第七册，正文十三卷，附《周易略例》及考证。全文离线导入状态：未完成。", """版本信息：
四库本题作《周易注疏》，作者层次为魏王弼、晋韩康伯注，唐陆德明音义，唐孔颖达疏。其卷首含御制序、提要、经传原目、注解传述人及《正义序》等；正文十三卷，附王弼《周易略例》、考证识语。
通行本与四库本卷数并不完全相同，必须分别建档。

全文与版本入口：
维基文库通行本《周易正义》：https://zh.wikisource.org/zh-hans/周易正義
维基文库四库本《周易注疏》：https://zh.wikisource.org/zh-hans/周易正義_(四庫全書本)
中国哲学书电子化计划馆藏影像入口：https://ctext.org/library.pl?if=gb&res=83810

后续导入结构：
卷首资料；卷一至卷十三；《周易略例》；考证识语。每卷须记录底本、卷次、经传章节范围、数字文本来源及影像页码。
状态声明：目前应用只建立书目与来源入口，尚未把十三卷疏文逐字离线录入。"""),
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
本书汇集汉魏以来多家易说，注文中引述诸家姓名、注文边界和异说必须保留，不得把李鼎祚编次与被引作者原著混为一谈。当前尚未离线录入十七卷正文，以上是已核验的全文入口及卷目。"""),
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
《九家易》曰：乾當來上，不可用師而拒之也。自邑者，謂從坤性而降也。告命者，謂下為巽，宣布君之命令也。三陰自相告語，俱下服順承乾也。城復於隍，國政崩也。坤為亂，否巽為命，交在泰上，故「其命亂也」。"""),
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
虞翻曰：否終必傾，盈不可久，故「先否」。下反於初，成益，體震，民說無疆，故「後喜」。以陰剝陽，故不可久也。"""),


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
侯果曰：獨處於外，同人於郊也。不與內爭，無悔吝也。同人之時，唯同於郊，「誌未得也」。"""),
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
《九家易》曰：上九說五，以柔處尊，而自謙損。尚賢奉已，上下應之，為乾所右，故吉且利也。"""),
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
案：上六兌爻。兌為口舌，鳴謙之象也。"""),
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
荀爽曰：陽性冥昧，居尊在上，而猶豫說，故不可長。"""),
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
虞翻曰：乘剛無應，故「上窮也」。"""),
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
荀爽曰：年老事終，不當其位。體艮為止。故「不事王侯」。據上臨下，重陰累實，故「誌可則」。"""),
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
《九家易》曰：誌在升二也。陰以陽為主。故誌在內也。"""),
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
虞翻曰：坎為誌，為平，上來之三，故「誌未平」矣。"""),
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
《九家易》曰：當據離坎，以為聰明。坎既不正，今欲滅之。故曰「聰不明也」。"""),
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
虞翻曰：上之五得位，體成既濟，故曰「得誌」。坎為誌也。"""),
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
侯果曰：艮為果，為廬。坤為輿。處剝之上，有剛直之德，群小人不能傷害也。故果至碩大，不被剝食矣。君子居此，萬姓賴安，若得乘其車輿也。小人處之，則庶方無控，被剝其廬舍，故曰「剝廬，終不可用」矣。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷六·复卦集解", "底本：维基文库《周易集解（李鼎祚）/卷六》，正文行112—208。录入复卦经文、彖象、六爻及页面所载注文、案语。原页面字形与注文归属照录，尚未与影印本逐字校勘。译文尚未完成。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷六", """《序卦》曰：物不可以終盡，剝窮上反下，故受之以復也。
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
虞翻曰：姤乾為君，坤陰滅之，以國君兇，故曰「反君道」也。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷六·无妄卦集解", "底本：维基文库《周易集解（李鼎祚）/卷六》，正文行210—296。录入无妄卦经文、彖象、六爻及页面所载注文、案语。底本“元享”“大享”等疑似字形依页面保留，未擅自改为通行本字样。译文尚未完成。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷六", """《序卦》曰：復則不妄矣，故受之以無妄。
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
崔覲曰：居無妄之中，有妄者也。妄而應三，上下非正。窮而反妄，故為災也。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷六·大畜卦集解", "底本：维基文库《周易集解（李鼎祚）/卷六》，正文行298—393。录入大畜卦经文、彖象、六爻及页面所载注文、案语。保留“輿說腹/輻”等底本和注文异说，不作无依据统一。译文尚未完成。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷六", """《序卦》曰：有無妄然後可畜，故受之以大畜。
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
虞翻曰：謂上據二陰。乾為天道，震為行，故「道大行矣」。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷六·颐卦集解", "底本：维基文库《周易集解（李鼎祚）/卷六》，正文行395—493。录入颐卦经文、彖象、六爻及页面所载注文、案语。页面“△”等标记和注文异说照录；尚未与影印本逐字校勘。译文尚未完成。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷六", """《序卦》曰：物畜然後可養，故受之以頤。頤者，養也。
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
虞翻曰：失位，故厲之。五得正成坎，坎為大川，故「利涉大川」。變陽得位，故「大有慶」也。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷六·大过卦集解", "底本：维基文库《周易集解（李鼎祚）/卷六》，正文行495—574。录入大过卦经文、彖象、六爻及页面所载注文、案语。异体、疑难字和注文异说照录，尚未与影印本逐字校勘。译文尚未完成。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷六", """《序卦》曰：不養則不可動，故受之以大過。
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
《九家易》曰：君子以禮義為法，小人以畏慎為宜。至於大過之世，不復遵常，故君子犯義，小人犯刑。而家家有誅絕之罪。不可咎也。大過之世，君子遜遁，不行禮義，謂當不義則爭之，若比幹諫而死是也。桀紂之民，可比屋而誅。上化致然，亦不可咎。曾子曰：上失其道，民散久矣。如得其情，則哀矜而勿喜，是其義也。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷六·坎卦集解", "底本：维基文库《周易集解（李鼎祚）/卷六》，正文行576—692。录入坎卦经文、彖象、六爻及页面所载注文、案语。页面异体、古字及引书标记照录，尚未与影印本逐字校勘。译文尚未完成。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷六", """《序卦》曰：物不可以終過，故受之以坎。坎者，陷也。
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
案：坎於木堅而多心，叢棘之象也。坎下巽爻，巽為繩直，「系用徽纆」也。馬融雲：徽纆，索也。劉表雲：三股為徽，兩股為纆，皆索名。以系縛其罪人矣。"""),
    Entry("历代注疏", "唐李鼎祚《周易集解》卷六·离卦集解", "底本：维基文库《周易集解（李鼎祚）/卷六》，正文行694—784。录入离卦经文、彖象、六爻及页面所载注文。原页面“百谷草木丽乎也”等疑似缺字或讹字不作推补，照底本保留；尚未与影印本逐字校勘。译文尚未完成。来源：https://zh.wikisource.org/wiki/周易集解_(李鼎祚)/卷六", """《序卦》曰：陷必有所麗，故受之以離。離者，麗也。
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
虞翻曰：乾五出征坤，故「正邦也」。"""),
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
"""),
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
"""),
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
"""),
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
"""),
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
"""),

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
"""),
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
"""),

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
"""),
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
"""),

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
崔憬曰：天地相遇，品物咸章，故言物相遇而后聚。"""),

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
《象》曰：赍咨涕洟，未安上也。
虞翻曰：乘刚远应，故未安上也。
荀爽曰：此本否卦。上九阳爻见灭迁移，以喻夏桀、殷纣；以上六阴爻代之。若夏之后封东娄公于杞，殷之后封微子于宋，去其骨肉，臣服异姓，受人封土，未安居位，故曰赍咨涕洟未安上也。

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
崔憬曰：冥升在上，以消不富则穷，故言升而不已必困也。"""),

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
曰动悔有悔，征吉。
虞翻曰：乘阳故动悔，变而失正故有悔；三已变正，已得应之，故征吉也。
《象》曰：困于葛藟，未当也。
虞翻曰：谓三未变当位应上故也。
动悔有悔，吉行也。
虞翻曰：行谓三变乃得当位之应，故吉行者也。"""),
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
程颐以义理解释卦爻，正文常在经文之后逐条作解。导入时须保留经文与程传的对应关系，并区分程传原文与后世标点、简化字转换。当前本条只建立四卷结构和来源入口，尚未把四卷全文离线收录。"""),
    Entry("历代注疏", "宋朱熹《周易本义》", "朱熹撰。已核实明嘉靖三十年（1551）刊本扫描及清乾隆十五年（1750）刊本扫描；另有卷首九图的版本档案。全文数字文本离线导入状态：未完成。", """版本档案：
明嘉靖辛亥（1551）逢原谿馆刊本，三卷，附《易五赞》一卷；国家图书馆藏扫描：https://commons.wikimedia.org/wiki/File:NCL-01278-0001_%E5%91%A8%E6%98%93%E6%9C%AC%E7%BE%A9.pdf
清乾隆十五年（1750）天都黄晟槐荫草堂刊本，四卷、六册；国家图书馆藏扫描：https://commons.wikimedia.org/wiki/File:NCL-00037_%E5%91%A8%E6%98%93%E6%9C%AC%E7%BE%A9.pdf
另有清康熙内府仿宋咸淳本扫描入口：https://old.shuge.org/ebook/zhou-yi-ben-yi/

卷首九图：
河图、洛书、伏羲八卦次序、伏羲八卦方位、伏羲六十四卦次序、伏羲六十四卦方位、文王八卦次序、文王八卦方位、卦变图。

注意：
以上扫描版本卷数、版式和图版位置不同，不能混成一个无版本标记的文本。当前条目记录了原书版本与扫描入口，尚未逐卷离线录入《周易本义》全文；图像也未全部打包进应用。"""),
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
引用诸家姓氏、注家观点和编者按语须分层标注。尤其需区分被引古说、李光地等编纂者的折中意见及康熙御制序。当前只建立全文入口和内容说明，尚未逐卷离线导入。"""),
    Entry("版本档案", "马王堆帛书《周易》及易传资料", "1973年长沙马王堆三号汉墓出土帛书《周易》相关资料。经文涉及六十四卦，另有《二三子》《系辞》《衷》《要》《缪和》《昭力》等易传材料。释文整理者廖名春；现代释文不是帛书原件。全文及图版离线导入状态：未完成。", """资料层次必须区分：
一、帛书原件及其考古图像；
二、整理者释文、缀合和释读；
三、后世研究者的校释与现代标点；
四、与传世通行本的异文对照。

已核验资料入口：
《马王堆帛书周易经传释文》（廖名春释文，收入《续修四库全书》经部易类）：https://www.openguji.com/item/d59f2og6qznm
相关资料说明指出，该释文整理的是1973年出土帛书《周易》经文及《二三子》《系辞》《衷》《要》《缪和》《昭力》等篇；不能把整理者释文误称为原件影像。

录入规则：
帛书异体字、通假字、残字、缀合处和释读分歧须并列保存；无法辨认处标“残缺／未释”，不按通行本补写。每条异文记录帛书原图位置、释文版本和传世本对应位置。当前尚未将帛书图版与逐字释文离线收录。"""),
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
        "https://www.shidianguji.com/book/DZ0159/chapter/DZ0159_58"
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
