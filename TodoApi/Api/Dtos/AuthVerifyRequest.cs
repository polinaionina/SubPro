using TodoApi.Models;
namespace TodoApi.Dtos
{
    public class AuthVerifyRequest
    {
        public string Nonce { get; set; } = null!;
        public TelegramAuthData TelegramData { get; set; } = null!;
    }
}
