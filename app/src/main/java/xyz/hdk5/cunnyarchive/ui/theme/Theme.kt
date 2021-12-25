package xyz.hdk5.cunnyarchive.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Blue200,
    primaryVariant = Blue700,
    onPrimary = Color.Black,

    secondary = Pink200,
    secondaryVariant = Pink200,
    onSecondary = Color.Black,
)

private val LightColorPalette = lightColors(
    primary = Blue500,
    primaryVariant = Blue700,
    onPrimary = Color.Black,

    secondary = Pink200,
    secondaryVariant = Pink700,
    onSecondary = Color.Black,
)

@Composable
fun CunnyArchiveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}
