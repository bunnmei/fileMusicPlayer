package space.webkombinat.musicplayer.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import space.webkombinat.musicplayer.R
import space.webkombinat.musicplayer.data.AppSettings
import space.webkombinat.musicplayer.data.EqualizerSettings
import space.webkombinat.musicplayer.data.LibraryState
import space.webkombinat.musicplayer.data.ThemeMode
import space.webkombinat.musicplayer.playback.EqualizerCapabilities
import space.webkombinat.musicplayer.ui.LocalBottomContentPadding
import space.webkombinat.musicplayer.ui.theme.supportsDynamicColor
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onFoldersClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val capabilities by viewModel.capabilities.collectAsStateWithLifecycle()
    val capabilitiesLoaded by viewModel.capabilitiesLoaded.collectAsStateWithLifecycle()
    val library by viewModel.library.collectAsStateWithLifecycle()
    val rescan by viewModel.rescan.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        val current = settings ?: return@Scaffold
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = LocalBottomContentPadding.current + 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SectionHeader(stringResource(R.string.section_appearance))
            SettingsCard {
                AppearanceSettings(current, viewModel)
            }

            SectionHeader(stringResource(R.string.section_sound))
            SettingsCard {
                VolumeSetting(current.volume, viewModel::setVolume)
            }
            SettingsCard {
                EqualizerSection(
                    settings = current.equalizer,
                    capabilities = capabilities,
                    loaded = capabilitiesLoaded,
                    viewModel = viewModel,
                )
            }

            SectionHeader(stringResource(R.string.section_library))
            SettingsCard {
                LibrarySection(
                    library = library,
                    excludedCount = current.excludedFolders.size,
                    rescan = rescan,
                    onFoldersClick = onFoldersClick,
                    onRescan = viewModel::rescanLibrary,
                )
            }

            SectionHeader(stringResource(R.string.section_about))
            SettingsCard {
                val context = LocalContext.current
                val version = remember {
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
                }
                SettingRow(title = stringResource(R.string.version), description = version)
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(vertical = 8.dp), content = content)
    }
}

@Composable
private fun SettingRow(
    title: String,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (description != null) {
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(16.dp))
            trailing()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppearanceSettings(settings: AppSettings, viewModel: SettingsViewModel) {
    Text(
        stringResource(R.string.theme),
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp),
    )
    val options: List<Triple<ThemeMode, Int, ImageVector>> = listOf(
        Triple(ThemeMode.SYSTEM, R.string.theme_system, Icons.Rounded.BrightnessAuto),
        Triple(ThemeMode.LIGHT, R.string.theme_light, Icons.Rounded.LightMode),
        Triple(ThemeMode.DARK, R.string.theme_dark, Icons.Rounded.DarkMode),
    )
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        options.forEachIndexed { index, (mode, label, icon) ->
            SegmentedButton(
                selected = settings.themeMode == mode,
                onClick = { viewModel.setThemeMode(mode) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                label = { Text(stringResource(label)) },
            )
        }
    }
    Spacer(Modifier.height(8.dp))
    if (supportsDynamicColor) {
        SettingRow(
            title = stringResource(R.string.dynamic_color),
            description = stringResource(R.string.dynamic_color_desc),
            onClick = { viewModel.setDynamicColor(!settings.dynamicColor) },
        ) {
            Switch(checked = settings.dynamicColor, onCheckedChange = viewModel::setDynamicColor)
        }
    }
}

@Composable
private fun VolumeSetting(volume: Float, onChange: (Float) -> Unit) {
    // スライダーの動きを滑らかにするため、表示用の値は手元に持つ
    var value by remember(volume) { mutableFloatStateOf(volume) }
    SettingRow(
        title = stringResource(R.string.volume),
        description = stringResource(R.string.volume_desc),
    ) {
        Text("${(value * 100).roundToInt()}%", style = MaterialTheme.typography.labelLarge)
    }
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.AutoMirrored.Rounded.VolumeDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Slider(
            value = value,
            onValueChange = {
                value = it
                onChange(it)
            },
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
        )
        Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EqualizerSection(
    settings: EqualizerSettings,
    capabilities: EqualizerCapabilities?,
    loaded: Boolean,
    viewModel: SettingsViewModel,
) {
    if (loaded && capabilities == null) {
        SettingRow(title = stringResource(R.string.equalizer), description = stringResource(R.string.equalizer_unsupported))
        return
    }
    SettingRow(
        title = stringResource(R.string.equalizer),
        description = stringResource(R.string.equalizer_desc),
        onClick = { viewModel.setEqualizerEnabled(!settings.enabled) },
    ) {
        Switch(checked = settings.enabled, onCheckedChange = viewModel::setEqualizerEnabled)
    }
    val caps = capabilities ?: return
    AnimatedVisibility(visible = settings.enabled) {
        Column {
            PresetChips(settings, caps, viewModel::selectPreset)
            BandSliders(settings, caps, viewModel::setBandLevels)
            if (caps.bassBoostSupported || caps.virtualizerSupported) {
                AdvancedEffects(settings, caps, viewModel)
            }
            TextButton(
                onClick = viewModel::resetSound,
                modifier = Modifier.align(Alignment.End).padding(end = 12.dp),
            ) {
                Text(stringResource(R.string.reset))
            }
        }
    }
}

@Composable
private fun PresetChips(
    settings: EqualizerSettings,
    caps: EqualizerCapabilities,
    onSelect: (EqualizerCapabilities.Preset) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 8.dp),
    ) {
        item {
            FilterChip(
                selected = settings.preset == EqualizerSettings.CUSTOM_PRESET,
                onClick = {},
                label = { Text(stringResource(R.string.preset_custom)) },
            )
        }
        items(caps.presets, key = { it.index }) { preset ->
            FilterChip(
                selected = settings.preset == preset.index,
                onClick = { onSelect(preset) },
                label = { Text(preset.name) },
            )
        }
    }
}

@Composable
private fun BandSliders(
    settings: EqualizerSettings,
    caps: EqualizerCapabilities,
    onChange: (List<Int>) -> Unit,
) {
    val bandCount = caps.bandFrequencies.size
    val saved = List(bandCount) { settings.bandLevels.getOrNull(it) ?: 0 }
    var levels by remember(saved) { mutableStateOf(saved) }
    caps.bandFrequencies.forEachIndexed { band, hz ->
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = formatFrequency(hz),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(56.dp),
            )
            Slider(
                value = levels[band].toFloat(),
                onValueChange = { new ->
                    levels = levels.toMutableList().also { it[band] = new.roundToInt() }
                    onChange(levels)
                },
                valueRange = caps.minLevel.toFloat()..caps.maxLevel.toFloat(),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = String.format(Locale.ROOT, "%+.1f dB", levels[band] / 100f),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.End,
                modifier = Modifier.width(64.dp),
            )
        }
    }
}

/** 低音ブースト・バーチャライザー。普段は畳んでおき、画面をすっきりさせる */
@Composable
private fun AdvancedEffects(settings: EqualizerSettings, caps: EqualizerCapabilities, viewModel: SettingsViewModel) {
    var expanded by rememberSaveable { mutableStateOf(settings.bassBoost > 0 || settings.virtualizer > 0) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "expand")
    SettingRow(title = stringResource(R.string.advanced_effects), onClick = { expanded = !expanded }) {
        Icon(Icons.Rounded.ExpandMore, contentDescription = null, modifier = Modifier.rotate(rotation))
    }
    AnimatedVisibility(visible = expanded) {
        Column {
            if (caps.bassBoostSupported) {
                StrengthSlider(stringResource(R.string.bass_boost), settings.bassBoost, viewModel::setBassBoost)
            }
            if (caps.virtualizerSupported) {
                StrengthSlider(stringResource(R.string.virtualizer), settings.virtualizer, viewModel::setVirtualizer)
            }
        }
    }
}

@Composable
private fun StrengthSlider(label: String, strength: Int, onChange: (Int) -> Unit) {
    var value by remember(strength) { mutableFloatStateOf(strength.toFloat()) }
    Column(Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text("${(value / 10).roundToInt()}%", style = MaterialTheme.typography.labelMedium)
        }
        Slider(
            value = value,
            onValueChange = {
                value = it
                onChange(it.roundToInt())
            },
            valueRange = 0f..1000f,
        )
    }
}

@Composable
private fun LibrarySection(
    library: LibraryState,
    excludedCount: Int,
    rescan: RescanState,
    onFoldersClick: () -> Unit,
    onRescan: () -> Unit,
) {
    if (library is LibraryState.Ready) {
        val lib = library.library
        SettingRow(
            title = stringResource(R.string.library),
            description = stringResource(R.string.library_stats, lib.songs.size, lib.albums.size, lib.artists.size),
        )
    }
    SettingRow(
        title = stringResource(R.string.folders),
        description = if (excludedCount == 0) {
            stringResource(R.string.folders_all_shown)
        } else {
            stringResource(R.string.folders_excluded_count, excludedCount)
        },
        onClick = onFoldersClick,
    ) {
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null)
    }
    SettingRow(
        title = stringResource(R.string.rescan),
        description = when (rescan) {
            RescanState.Idle -> stringResource(R.string.rescan_desc)
            RescanState.Running -> stringResource(R.string.rescanning)
            is RescanState.Done -> stringResource(R.string.rescan_done, rescan.fileCount)
        },
    ) {
        OutlinedButton(onClick = onRescan, enabled = rescan != RescanState.Running) {
            Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.rescan), modifier = Modifier.size(18.dp))
        }
    }
}

private fun formatFrequency(hz: Int): String =
    if (hz >= 1000) {
        val khz = hz / 1000f
        if (khz % 1f == 0f) "${khz.toInt()} kHz" else String.format(Locale.ROOT, "%.1f kHz", khz)
    } else {
        "$hz Hz"
    }
