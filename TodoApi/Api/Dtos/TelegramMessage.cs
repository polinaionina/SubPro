namespace TodoApi.Dtos
{
    public class TelegramMessage
    {
        public long ChatId { get; set; }
        public string Message { get; set; } = null!;
        public string? PhotoUrl { get; set; }
    }

}