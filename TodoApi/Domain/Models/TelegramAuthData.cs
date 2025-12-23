using System.Text.Json.Serialization;

namespace TodoApi.Models
{
    public class TelegramAuthData
    {
        [JsonPropertyName("id")]
        public long Id { get; set; }

        [JsonPropertyName("first_name")]
        public string? FirstName { get; set; }

        [JsonPropertyName("username")]
        public string? Username { get; set; }

        [JsonPropertyName("auth_date")]
        public long AuthDate { get; set; }

        [JsonPropertyName("hash")]
        public string Hash { get; set; } = null!;
    }
}
