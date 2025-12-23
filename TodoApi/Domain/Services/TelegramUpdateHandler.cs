using Telegram.Bot;
using Telegram.Bot.Types;
using Telegram.Bot.Types.Enums;
using Microsoft.EntityFrameworkCore;
using TodoApi.Data;
using TodoApi.Services;

public class TelegramUpdateHandler
{
    private readonly ITelegramBotClient _bot;
    private readonly IServiceScopeFactory _scopeFactory;

    public TelegramUpdateHandler(
        ITelegramBotClient bot,
        IServiceScopeFactory scopeFactory)
    {
        _bot = bot;
        _scopeFactory = scopeFactory;
    }

    public async Task HandleAsync(Update update)
    {
        if (update.Type != UpdateType.Message)
            return;

        var message = update.Message;
        if (message?.Text == null)
            return;

        if (message.Text.StartsWith("/start"))
        {
            await HandleStart(message);
        }
    }

    private async Task HandleStart(Message message)
    {
        var telegramId = message.From!.Id;
        var chatId = message.Chat.Id;

        using var scope = _scopeFactory.CreateScope();
        var db = scope.ServiceProvider.GetRequiredService<AppDbContext>();

        var user = await db.Users
            .FirstOrDefaultAsync(u => u.TelegramId == telegramId);

        if (user == null)
        {
            await _bot.SendTextMessageAsync(
                chatId,
                "❌ Сначала авторизуйся в приложении"
            );
            return;
        }

        user.ChatId = chatId;
        await db.SaveChangesAsync();

        await _bot.SendTextMessageAsync(
            chatId,
            "✅ Бот подключён! Теперь я буду присылать уведомления о подписках 🔔"
        );
    }
}
