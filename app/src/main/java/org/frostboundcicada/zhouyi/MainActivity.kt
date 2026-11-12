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
                        ZeroText("逐段译文", style = MaterialTheme.typography.titleMedium,
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
        2 -> listOf("全部", "河图", "洛书", "版本档案", "相关古籍")
        else -> listOf("全部", "易经", "十翼", "河图", "洛书", "版本档案", "相关古籍")
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
""".trimIndent(), "暂无可靠译文；本次仅录原文，不以现代白话转述冒充校定译文。"),
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
""".trimIndent(), "暂无可靠译文；原文中的“亲寡旅也”等字句保留通行页面正文形态，校詁版异文另列，不在此处擅自统一。"),
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
""".trimIndent(), "暂无可靠译文；本条为六十四卦大象辞原文。"),
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
""".trimIndent(), "暂无可靠译文；原文已按上经三十卦逐条录入，异文校勘待后续完成。"),
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
""".trimIndent(), "暂无可靠译文；原文已按下经三十四卦逐条录入，异文校勘待后续完成。"),
    Entry("十翼", "《小象传》", "待录入：维基文库《周易/小象》六十四卦爻象解释；篇幅较长，将分卦关联并保留爻位。", "本条尚未收录全文。"),
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
""".trimIndent(), "暂无可靠译文；十二章原文已录入，章节文字和传本异文仍待逐条校勘。"),
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
""".trimIndent(), "暂无可靠译文；九章原文已录入，保留章次，异文和断句仍待校勘。"),
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
""".trimIndent(), "暂无可靠译文；保留乾、坤分篇结构，暂不将校詁版混入正文。"),
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
""".trimIndent(), "暂无可靠译文；此篇含大量象类术语，待逐章建立术语表后再补译。")
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
