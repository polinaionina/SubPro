namespace TodoApi.Models
{
    public class User
    {
        public long Id { get; set; }
        public string Username { get; set; } = null!;
        public string FirstName { get; set; } = null!;
        public string? LastName { get; set; }
        public string DeviceId { get; set; } = null!;
        public DateTime CreatedAt { get; set; }

        public long? TelegramId { get; set; }
        public long? ChatId { get; set; }
    }
}
