using Microsoft.EntityFrameworkCore;
using TodoApi.Application.Handlers;
using TodoApi.Data;
using TodoApi.Models;

namespace TodoApi.Application.Handlers.CreateSubscription;

public class CreateSubscriptionHandler
    : IHandler<(long TelegramId, CreateSubscriptionDto Dto), CreateSubscriptionResult>
{
    private readonly AppDbContext _db;

    public CreateSubscriptionHandler(AppDbContext db)
    {
        _db = db;
    }

    public async Task<CreateSubscriptionResult> Handle(
        (long TelegramId, CreateSubscriptionDto Dto) input)
    {
        var (telegramId, dto) = input;

        var user = await _db.Users
            .FirstOrDefaultAsync(u => u.TelegramId == telegramId);

        if (user == null)
        {
            return new CreateSubscriptionResult
            {
                Success = false,
                Error = "User not found"
            };
        }

        var subscription = new Subscription
        {
            UserId = user.Id,
            Name = dto.Name,
            Price = dto.Price,
            Period = dto.Period,
            NextPaymentDate = dto.NextPaymentDate,
            NotificationDaysBefore = dto.NotificationDaysBefore,
            CreatedAt = DateTime.UtcNow
        };

        _db.Subscriptions.Add(subscription);
        await _db.SaveChangesAsync();

        return new CreateSubscriptionResult
        {
            Success = true
        };
    }
}
