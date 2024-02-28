import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.vangel.modeling.App
import java.awt.Dimension

fun main() = application {
    Window(
        title = "Modeling",
        state = rememberWindowState(
            placement = WindowPlacement.Maximized,
            width = 1920.dp,
            height = 1080.dp,
            position = WindowPosition(0.dp, 0.dp),
        ),
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = Dimension(350, 600)
        App()
    }
}