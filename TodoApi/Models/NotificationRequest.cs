
public class NotificationRequest
{
    public string Message { get; set; }
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
    public string MessageId { get; set; }
    public string Error { get; set; }
    public DateTime SentAt { get; set; } = DateTime.UtcNow;
}