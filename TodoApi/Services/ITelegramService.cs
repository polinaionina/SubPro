using TodoApi.Models;
using UserModel = TodoApi.Models.User;

namespace TodoApi.Services
{
    public interface ITelegramService
    {
        Task<SendMessageResult> SendMessageAsync(long chatId, string message);
        Task<bool> TestConnectionAsync();
        bool ValidateTelegramLogin(TelegramAuthRequest request);
        string GenerateJwt(UserModel user);
    }
}
