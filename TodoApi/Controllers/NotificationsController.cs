using Microsoft.AspNetCore.Mvc;
using TodoApi.Models;
using TodoApi.Services;
using System.Reflection;

namespace TodoApi.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
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
                var messageType = message.GetType();
                foreach (var prop in messageType.GetProperties())
                {
                    _logger.LogInformation("Property {Name} = {Value}",
                        prop.Name, prop.GetValue(message));
                }

                var controllerName = GetType().Name;
                _logger.LogInformation("Controller {Controller} is processing message", controllerName);

                MethodInfo? method = typeof(ITelegramService)
                    .GetMethod("SendMessageAsync");

                if (method == null)
                {
                    return StatusCode(500, new SendMessageResult
                    {
                        Success = false,
                        Error = "Reflection error: method SendMessageAsync not found"
                    });
                }

                var taskObject = method.Invoke(
                    _telegramService,
                    new object[] { message.ChatId, message.Message }
                );

                var task = (Task<SendMessageResult>)taskObject!;
                var result = await task;

                return result.Success ? Ok(result) : BadRequest(result);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending notification to chat {ChatId}", message.ChatId);
                return StatusCode(500, new SendMessageResult
                {
                    Success = false,
                    Error = "Internal server error"
                });
            }
        }

        [HttpGet("test")]
        public async Task<ActionResult> TestConnection()
        {
            var isConnected = await _telegramService.TestConnectionAsync();
            
            if (isConnected)
            {
                return Ok(new { message = "Bot connection successful" });
            }
            else
            {
                return BadRequest(new { message = "Bot connection failed" });
            }
        }

        [HttpPost("test-message")]
        public async Task<ActionResult<SendMessageResult>> SendTestMessage([FromQuery] long chatId)
        {
            var testMessage = $"✅ Test notification from TodoAPI!\n" +
                            $"🕒 Time: {DateTime.UtcNow:yyyy-MM-dd HH:mm:ss} UTC\n" +
                            $"🔧 API: TodoApi";

            var result = await _telegramService.SendMessageAsync(chatId, testMessage);
            
            if (result.Success)
            {
                return Ok(result);
            }
            else
            {
                return BadRequest(result);
            }
        }
    }
}