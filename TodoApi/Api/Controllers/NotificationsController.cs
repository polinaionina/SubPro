using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using TodoApi.Models;
using TodoApi.Services;
using System.Reflection;
using Microsoft.Extensions.Logging;
using TodoApi.Dtos;

namespace TodoApi.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize]
    public class NotificationsController : ControllerBase
    {
        private readonly ITelegramService _telegramService;
        private readonly ILogger<NotificationsController> _logger;

        public NotificationsController(ITelegramService telegramService, ILogger<NotificationsController> logger)
        {
            _telegramService = telegramService;
            _logger = logger;
        }

        [HttpPost]
        public async Task<ActionResult<SendMessageResult>> SendNotification(TelegramMessage message)
        {
            try
            {
                if (message == null)
                {
                    return BadRequest(new { error = "Message is required" });
                }

                var telegramId = User.FindFirst("telegram_id")?.Value;
                var deviceId = User.FindFirst("device_id")?.Value;

                if (string.IsNullOrEmpty(telegramId))
                {
                    return Unauthorized(new { error = "No telegram_id in token" });
                }

                _logger.LogInformation(
                    "Sending notification for user {TelegramId} from device {DeviceId}", 
                    telegramId, deviceId);

                if (!long.TryParse(telegramId, out long userChatId))
                {
                    return BadRequest(new { error = "Invalid telegram_id format" });
                }

                var result = await _telegramService.SendMessageAsync(userChatId, message.Message);

                return result.Success ? Ok(result) : BadRequest(result);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending notification");
                return StatusCode(500, new SendMessageResult
                {
                    Success = false,
                    Error = "Internal server error"
                });
            }
        }
    }
}