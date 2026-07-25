import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp
import com.gym.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Gym Management & Member Application (KMP MVC)",
        state = rememberWindowState(width = 1100.dp, height = 800.dp)
    ) {
        App()
    }
}
