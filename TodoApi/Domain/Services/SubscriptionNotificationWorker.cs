using Microsoft.EntityFrameworkCore;
using TodoApi.Data;
using TodoApi.Services;   
using TodoApi.Models;      

public class SubscriptionNotificationWorker : BackgroundService
{
    private readonly IServiceScopeFactory _scopeFactory;
    private readonly ILogger<SubscriptionNotificationWorker> _logger;

    public SubscriptionNotificationWorker(
        IServiceScopeFactory scopeFactory,
        ILogger<SubscriptionNotificationWorker> logger)
    {
        _scopeFactory = scopeFactory;
        _logger = logger;
    }

    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        while (!stoppingToken.IsCancellationRequested)
        {
            await CheckSubscriptions();
            await Task.Delay(TimeSpan.FromMinutes(1), stoppingToken);


        }
    }

    private async Task CheckSubscriptions()
    {
        using var scope = _scopeFactory.CreateScope();
        var db = scope.ServiceProvider.GetRequiredService<AppDbContext>();
        var telegram = scope.ServiceProvider.GetRequiredService<ITelegramService>();

        var now = DateTime.UtcNow;

        var subscriptions = await db.Subscriptions
            .Include(s => s.User)
            .Where(s =>
                s.IsActive &&
                s.User.ChatId != null &&
                now >= s.NextPaymentDate.AddDays(-s.NotificationDaysBefore) &&
                now < s.NextPaymentDate
            )
            .ToListAsync();


        foreach (var sub in subscriptions)
        {
            await telegram.SendMessageAsync(
                sub.User.ChatId.Value,
                $"⏰ Подписка <b>{sub.Name}</b> заканчивается {sub.NextPaymentDate:dd.MM.yyyy}"
            );
        }
    }
}
