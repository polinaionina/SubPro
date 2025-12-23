using Microsoft.AspNetCore.Mvc;
using Telegram.Bot.Types;

[ApiController]
[Route("api/telegram/webhook")]
public class TelegramWebhookController : ControllerBase
{
    private readonly TelegramUpdateHandler _handler;

    public TelegramWebhookController(TelegramUpdateHandler handler)
    {
        _handler = handler;
    }

    [HttpPost]
    public async Task<IActionResult> Post([FromBody] Update update)
    {
        await _handler.HandleAsync(update);
        return Ok();
    }
}
