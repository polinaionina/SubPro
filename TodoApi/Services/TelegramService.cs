using Telegram.Bot;
using Telegram.Bot.Types;
using Telegram.Bot.Types.Enums;
using TodoApi.Models;
using System.Text;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using Microsoft.IdentityModel.Tokens;
using UserModel = TodoApi.Models.User;

namespace TodoApi.Services
{
    public class TelegramService : ITelegramService
    {
        private readonly ITelegramBotClient _botClient;
        private readonly ILogger<TelegramService> _logger;
        private readonly IConfiguration _configuration;

        public TelegramService(IConfiguration configuration, ILogger<TelegramService> logger)
        {
            _configuration = configuration;
            _logger = logger;

            var botToken = _configuration["TelegramBot:Token"];
            if (string.IsNullOrEmpty(botToken))
                throw new ArgumentException("Telegram bot token is not configured");

            _botClient = new TelegramBotClient(botToken);
        }

        public async Task<SendMessageResult> SendMessageAsync(long chatId, string message)
        {
            try
            {
                if (string.IsNullOrWhiteSpace(message))
                    return new SendMessageResult { Success = false, Error = "Message cannot be empty" };

                var sentMessage = await _botClient.SendTextMessageAsync(
                    chatId, message, parseMode: ParseMode.Html
                );

                _logger.LogInformation(
                    "Message sent to chat {ChatId}, message ID: {MessageId}", 
                    chatId, sentMessage.MessageId
                );

                return new SendMessageResult { Success = true, MessageId = sentMessage.MessageId.ToString() };
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error sending message to chat {ChatId}", chatId);
                return new SendMessageResult { Success = false, Error = ex.Message };
            }
        }

        public async Task<bool> TestConnectionAsync()
        {
            try
            {
                var me = await _botClient.GetMeAsync();
                _logger.LogInformation("Bot connection test successful. Bot: @{BotUsername}", me.Username);
                return true;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Bot connection test failed");
                return false;
            }
        }

        public bool ValidateTelegramLogin(TelegramAuthRequest request)
        {
            // TODO: добавить проверку подписи Telegram
            return true;
        }

        public string GenerateJwt(UserModel user)
        {
            var jwtSettings = _configuration.GetSection("Jwt");
            var key = Encoding.UTF8.GetBytes(jwtSettings["Key"]!);

            var claims = new List<Claim>
            {
                new Claim("telegram_id", user.TelegramId!.Value.ToString()),
                new Claim("user_id", user.Id.ToString()),
                new Claim(ClaimTypes.Name, user.Username ?? "")
            };

            var token = new JwtSecurityToken(
                issuer: jwtSettings["Issuer"],
                audience: jwtSettings["Audience"],
                claims: claims,
                expires: DateTime.UtcNow.AddMinutes(
                    double.Parse(jwtSettings["LifetimeMinutes"] ?? "60")
                ),
                signingCredentials: new SigningCredentials(
                    new SymmetricSecurityKey(key),
                    SecurityAlgorithms.HmacSha256
                )
            );

            return new JwtSecurityTokenHandler().WriteToken(token);
        }
    }
}
