package io.github.jdanders.dropcount.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.jdanders.dropcount.config.ThemeConfig
import io.github.jdanders.dropcount.model.AnimationSpeed
import io.github.jdanders.dropcount.model.VisualTheme
import io.github.jdanders.dropcount.ui.components.AutoShrinkText
import io.github.jdanders.dropcount.ui.theme.*

import androidx.compose.ui.res.stringResource
import io.github.jdanders.dropcount.R

/**
 * Dialog for game settings including animation speed and visual theme.
 */
@Composable
fun SettingsDialog(
    currentSpeed: AnimationSpeed,
    onSpeedChange: (AnimationSpeed) -> Unit,
    currentTheme: VisualTheme,
    onThemeChange: (VisualTheme) -> Unit,
    onDismiss: () -> Unit
) {
    val renderer = remember(currentTheme) { currentTheme.createRenderer() }

    val isFoundry = currentTheme == VisualTheme.FOUNDRY
    val dialogShape = if (isFoundry) RoundedCornerShape(0.dp) else RoundedCornerShape(24.dp)
    val buttonShape = if (isFoundry) RoundedCornerShape(0.dp) else RoundedCornerShape(12.dp)
    val selectionColor = if (isFoundry) IndustrialOrange else ButtonPrimary

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f)
                .wrapContentHeight(),
            shape = dialogShape,
            color = renderer.getOverlayBackgroundColor(),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Title
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = if (isFoundry) Alignment.Start else Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.settings_title).let { if (isFoundry) it.uppercase() else it },
                        fontWeight = FontWeight.Black,
                        fontSize = if (isFoundry) 28.sp else 32.sp,
                        letterSpacing = if (isFoundry) 2.sp else 1.sp,
                        color = renderer.getLabelTextColor()
                    )
                    if (isFoundry) {
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = IndustrialOrange, thickness = 2.dp)
                    }
                }

                // Scrollable content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Animation Speed Section
                    SectionHeader(text = stringResource(R.string.settings_animation_speed), renderer = renderer)

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimationSpeed.entries.forEach { speed ->
                            SpeedButton(
                                speed = speed,
                                isSelected = currentSpeed == speed,
                                onClick = { onSpeedChange(speed) },
                                renderer = renderer,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Visual Theme Section
                    SectionHeader(text = stringResource(R.string.settings_visual_theme), renderer = renderer)

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VisualTheme.entries.forEach { theme ->
                            ThemeButton(
                                theme = theme,
                                isSelected = currentTheme == theme,
                                onClick = { onThemeChange(theme) },
                                renderer = renderer,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Close button - always visible outside scroll area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    contentAlignment = if (isFoundry) Alignment.CenterStart else Alignment.CenterEnd
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = selectionColor
                        ),
                        shape = buttonShape,
                        border = if (isFoundry) BorderStroke(2.dp, IndustrialOrange) else null
                    ) {
                        Text(
                            text = stringResource(R.string.action_close).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = if (isFoundry) Color.Black else Color.Unspecified
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String, renderer: ThemeRenderer) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = renderer.getLabelTextColor()
    )
}

@Composable
private fun SpeedButton(
    speed: AnimationSpeed,
    isSelected: Boolean,
    onClick: () -> Unit,
    renderer: ThemeRenderer,
    modifier: Modifier = Modifier
) {
    val isFoundry = renderer is FoundryThemeRenderer
    val selectionColor = if (isFoundry) IndustrialOrange else ButtonPrimary
    val containerColor = if (isSelected) selectionColor else renderer.getCardBackgroundColor()
    val contentColor = if (isSelected) Color.Black else renderer.getLabelTextColor()
    val borderColor = if (isSelected) selectionColor else renderer.getCardBorderColor()
    val shape = if (isFoundry) RoundedCornerShape(0.dp) else RoundedCornerShape(12.dp)

    Button(
        onClick = onClick,
        modifier = modifier.height(ThemeConfig.SPEED_SELECTION_BUTTON_HEIGHT.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(if (isFoundry) 2.dp else 1.dp, borderColor),
        shape = shape
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AutoShrinkText(
                text = stringResource(id = speed.displayNameRes).uppercase(),
                style = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp),
                color = contentColor
            )
            AutoShrinkText(
                text = stringResource(R.string.speed_multiplier_format, speed.multiplier),
                style = LocalTextStyle.current.copy(fontSize = 12.sp),
                color = contentColor.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ThemeButton(
    theme: VisualTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    renderer: ThemeRenderer,
    modifier: Modifier = Modifier
) {
    val isFoundry = renderer is FoundryThemeRenderer
    val selectionColor = if (isFoundry) IndustrialOrange else ButtonPrimary
    val containerColor = if (isSelected) selectionColor else renderer.getCardBackgroundColor()
    val contentColor = if (isSelected) Color.Black else renderer.getLabelTextColor()
    val borderColor = if (isSelected) selectionColor else renderer.getCardBorderColor()
    val shape = if (isFoundry) RoundedCornerShape(0.dp) else RoundedCornerShape(12.dp)

    Button(
        onClick = onClick,
        modifier = modifier.height(ThemeConfig.THEME_SELECTION_BUTTON_HEIGHT.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(if (isFoundry) 2.dp else 1.dp, borderColor),
        shape = shape
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AutoShrinkText(
                text = stringResource(id = theme.displayNameRes).uppercase(),
                style = LocalTextStyle.current.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp),
                color = contentColor
            )
            AutoShrinkText(
                text = stringResource(id = theme.descriptionRes),
                style = LocalTextStyle.current.copy(fontSize = 11.sp),
                color = contentColor.copy(alpha = 0.7f)
            )
        }
    }
}
