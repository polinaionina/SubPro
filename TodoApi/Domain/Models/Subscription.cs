namespace TodoApi.Models
{
    public class Subscription
    {
        public long Id { get; set; }
        public long UserId { get; set; }

        public string Name { get; set; } = null!;
        public decimal Price { get; set; }
        public BillingPeriod Period { get; set; }

        public DateTime NextPaymentDate { get; set; }
        public int NotificationDaysBefore { get; set; } = 3;
        public bool IsActive { get; set; } = true;
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        public User? User { get; set; }
    }
}