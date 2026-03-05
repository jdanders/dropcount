package io.github.jdanders.dropcount.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jdanders.dropcount.config.ThemeConfig
import io.github.jdanders.dropcount.model.AnimationSpeed
import io.github.jdanders.dropcount.model.VisualTheme
import io.github.jdanders.dropcount.ui.theme.*

import androidx.compose.ui.res.stringResource
import io.github.jdanders.dropcount.R

/**
 * Dialog for game settings including animation speed.
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
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
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Animation Speed Section
                SectionHeader(text = stringResource(R.string.settings_animation_speed), renderer = renderer)

                // Speed preset buttons
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

                Spacer(modifier = Modifier.height(4.dp))

                // Visual Theme Section
                SectionHeader(text = stringResource(R.string.settings_visual_theme), renderer = renderer)

                // Theme selection buttons
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
            }
        },
        confirmButton = {
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
        },
        containerColor = renderer.getOverlayBackgroundColor(),
        shape = dialogShape
    )
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
            Text(
                text = stringResource(id = speed.displayNameRes).uppercase(),
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = stringResource(R.string.speed_multiplier_format, speed.multiplier),
                fontSize = 12.sp,
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
            Text(
                text = stringResource(id = theme.displayNameRes).uppercase(),
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = stringResource(id = theme.descriptionRes),
                fontSize = 11.sp,
                color = contentColor.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}
