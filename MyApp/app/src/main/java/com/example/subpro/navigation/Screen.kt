sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Calendar : Screen("calendar")
    object Add : Screen("add_choice")
    object Form : Screen("add_form")
    data class Edit(val id: Int) : Screen("edit")
    object TelegramAuth : Screen("telegram_auth")
}