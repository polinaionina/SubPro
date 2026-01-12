cd ~/SubPro/TodoApi/Api/Controllers

cat > AuthController.cs <<'EOF'
using Microsoft.AspNetCore.Mvc;
using TodoApi.Models;
using TodoApi.Services;
using Microsoft.Extensions.Logging;
using TodoApi.Dtos;

namespace TodoApi.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : ControllerBase
    {
        private readonly UserService _userService;
        private readonly ITelegramService _telegramService;
        private readonly IConfiguration _config;
        private readonly NonceStorage _nonceStorage;
        private readonly ILogger<AuthController> _logger;

        public AuthController(
            IConfiguration config,
            NonceStorage nonceStorage,
            ILogger<AuthController> logger,
            UserService userService,
            ITelegramService telegramService)
        {
            _config = config;
            _nonceStorage = nonceStorage;
            _logger = logger;
            _userService = userService;
            _telegramService = telegramService;
        }

        [HttpPost("start")]
        public IActionResult StartAuth([FromBody] StartAuthRequest request)
        {
            if (string.IsNullOrEmpty(request?.DeviceId))
                return BadRequest("DeviceId is required");

            var nonce = Guid.NewGuid().ToString("N");
            _nonceStorage.Add(nonce, request.DeviceId);

            var publicUrl = _config["Server:PublicUrl"]!;
            var redirectUrl = $"{publicUrl}/telegram-login.html?nonce={nonce}";

            return Ok(new
            {
                nonce,
                loginUrl = redirectUrl
            });
        }

        [HttpGet("telegram-callback")]
        public async Task<IActionResult> TelegramCallback(
            [FromQuery] string id,
            [FromQuery] string first_name,
            [FromQuery] string username,
            [FromQuery] string auth_date,
            [FromQuery] string hash)
        {
            try
            {
                _logger.LogInformation("Telegram callback for user {Id}", id);

                var nonce = Request.Query["nonce"].FirstOrDefault();
                if (nonce == null || !_nonceStorage.TryGet(nonce, out var deviceId))
                    return BadRequest("Invalid nonce");

                _nonceStorage.Remove(nonce);

                var user = await _userService.GetOrCreateUserAsync(new TelegramAuthRequest
                {
                    Id = long.Parse(id),
                    FirstName = first_name,
                    LastName = Request.Query["last_name"].FirstOrDefault(),
                    Username = username,
                    PhotoUrl = Request.Query["photo_url"].FirstOrDefault(),
                    AuthDate = long.Parse(auth_date),
                    Hash = hash,
                    DeviceId = deviceId!
                });

                var jwt = _telegramService.GenerateJwt(user);

                var deepLink =
                    $"subpro://auth" +
                    $"?token={jwt}" +
                    $"&telegramId={user.TelegramId}" +
                    $"&success=true";

                if (user.ChatId.HasValue)
                {
                    await _telegramService.SendMessageAsync(
                        user.ChatId.Value,
                        "✅ Авторизация прошла успешно! Я буду присылать уведомления 🔔"
                    );
                }

                return Redirect(deepLink);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Telegram callback failed");
                return BadRequest("Telegram auth failed");
            }
        }
    }
}
EOF