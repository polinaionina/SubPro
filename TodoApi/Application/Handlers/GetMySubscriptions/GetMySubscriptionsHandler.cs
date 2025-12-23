using Microsoft.EntityFrameworkCore;
using TodoApi.Application.Handlers;
using TodoApi.Application.Handlers.GetMySubscriptions;
using TodoApi.Data;
using TodoApi.Models;

namespace TodoApi.Application.Handlers.GetMySubscriptions;

public class GetMySubscriptionsHandler
    : IHandler<long, GetMySubscriptionsResult>
{
    private readonly AppDbContext _db;

    public GetMySubscriptionsHandler(AppDbContext db)
    {
        _db = db;
    }

    public async Task<GetMySubscriptionsResult> Handle(long telegramId)
    {
        var user = await _db.Users
            .FirstOrDefaultAsync(u => u.TelegramId == telegramId);

        if (user == null)
        {
            return new GetMySubscriptionsResult
            {
                Success = false,
                Error = "User not found"
            };
        }

        var subs = await _db.Subscriptions
            .Where(s => s.UserId == user.Id)
            .ToListAsync();

        return new GetMySubscriptionsResult
        {
            Success = true,
            Subscriptions = subs
        };
    }
}
