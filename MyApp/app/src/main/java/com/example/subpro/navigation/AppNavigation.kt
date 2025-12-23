import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.subpro.MainActivity
import com.example.subpro.mutil.NotificationHelper
import com.example.subpro.ui.global.screen.AddSubscriptionScreen
import com.example.subpro.ui.screens.CalendarScreen
import com.example.subpro.ui.screens.MainScreen

@Composable
fun AppNavigation(
    onSendNotification: () -> Unit,
    isTelegramAuthSuccess: Boolean,
    notificationHelper: NotificationHelper
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
    val bottomBarScreens = listOf(Screen.Main.route, Screen.Calendar.route)
    val showBottomBar = currentScreen.route in bottomBarScreens

    val context = LocalContext.current
    val activity = remember(context) { context as? MainActivity }

    LaunchedEffect(isTelegramAuthSuccess) {
        if (isTelegramAuthSuccess) {
            currentScreen = Screen.Main
            activity?.telegramAuthSuccess = false
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it }
                )
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = Color(0xFFF4F2EF)
        ) {
            when (val screen = currentScreen) {
                is Screen.Main -> MainScreen(
                    onSendNotification = onSendNotification,
                    onGoToTelegramAuth = { currentScreen = Screen.TelegramAuth },
                    onEditSubscription = { id -> currentScreen = Screen.Edit(id) },
                    notificationHelper = notificationHelper,
                )
                is Screen.Calendar -> CalendarScreen()
                is Screen.Add -> SubscriptionChoiceScreen(
                    onAddCustom = { currentScreen = Screen.Form },
                    onSuccess = { currentScreen = Screen.Main }
                )
                is Screen.Form -> AddSubscriptionScreen(
                    context = LocalContext.current,
                    onBack = { currentScreen = Screen.Main },
                    subscriptionId = null
                )
                is Screen.Edit -> AddSubscriptionScreen(
                    context = LocalContext.current,
                    onBack = { currentScreen = Screen.Main },
                    subscriptionId = screen.id
                )
                is Screen.TelegramAuth -> TelegramAuthScreen(
                    serverBaseUrl = "https://droopingly-troughlike-dedra.ngrok-free.dev",
                    onBack = { currentScreen = Screen.Main },
                    onAuthSuccess = { currentScreen = Screen.Main }
                )
            }
        }
    }
}
