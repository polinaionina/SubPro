using TodoApi.Models;


public class CreateSubscriptionDto
{
    public string Name { get; set; } = null!;
    public decimal Price { get; set; }
    public BillingPeriod Period { get; set; }

    public DateTime NextPaymentDate { get; set; }
    public int NotificationDaysBefore { get; set; } = 3;
}