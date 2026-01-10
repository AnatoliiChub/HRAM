import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Suppress("ComposableNaming")
@Composable
fun appStateChanged(onChanged: (state: AppState) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> onChanged(AppState.BACKGROUND)
                Lifecycle.Event.ON_RESUME -> onChanged(AppState.FOREGROUND)
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

enum class AppState {
    FOREGROUND,
    BACKGROUND;

    fun isBackground() = this == BACKGROUND
}
