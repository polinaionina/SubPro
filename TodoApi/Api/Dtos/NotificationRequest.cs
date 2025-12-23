namespace TodoApi.Dtos
{
public class NotificationRequest
{
    public string Message { get; set; } = null!;
    public string? PhotoUrl { get; set; }
    public NotificationType Type { get; set; } = NotificationType.Text;
}

public enum NotificationType
{
    Text,
    Photo,
    Document
}

public class SendMessageResult
{
    public bool Success { get; set; }
    public string MessageId { get; set; } = null!;
    public string Error { get; set; } = null!;
    public DateTime SentAt { get; set; } = DateTime.UtcNow;
}
}