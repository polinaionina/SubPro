import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.subpro.R


@Composable
fun BottomNavigationBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    NavigationBar(containerColor = Color(0xFFF4F2EF)) {
        val items = listOf(
            Triple(Screen.Main, "Главная", R.drawable.menu),
            Triple(Screen.Calendar, "Календарь", R.drawable.today),
            Triple(Screen.Add, "Добавить", R.drawable.dobavit)
        )

        items.forEach { (screen, _, iconRes) ->
            val iconSize = if (screen == Screen.Add) 65.dp else 50.dp
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(iconSize),
                        tint = Color.Unspecified
                    )
                },
                label = null,
                selected = currentScreen == screen,
                onClick = { onScreenSelected(screen) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFFACBACC)
                )
            )
        }
    }
}
