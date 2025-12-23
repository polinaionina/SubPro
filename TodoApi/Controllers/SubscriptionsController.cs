using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using TodoApi.Data;
using TodoApi.Models;

namespace TodoApi.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class SubscriptionsController : ControllerBase
    {
        private readonly AppDbContext _context;

        public SubscriptionsController(AppDbContext context)
        {
            _context = context;
        }

        [HttpPost]
        public async Task<IActionResult> Create([FromBody] CreateSubscriptionDto dto)
        {
            var telegramIdStr = User.FindFirst("telegram_id")?.Value;
            if (telegramIdStr == null)
                return Unauthorized("telegram_id not found in token");

            if (!long.TryParse(telegramIdStr, out var telegramId))
                return Unauthorized("Invalid telegram_id");

            var user = await _context.Users
                .FirstOrDefaultAsync(u => u.TelegramId == telegramId);

            if (user == null)
                return NotFound("User not found");

            var subscription = new Subscription
            {
                UserId = user.Id,
                Name = dto.Name,
                Price = dto.Price,
                Period = dto.Period,

                NextPaymentDate = dto.NextPaymentDate,

                NotificationDaysBefore = dto.NotificationDaysBefore,
                IsActive = true,
                CreatedAt = DateTime.UtcNow
            };


            _context.Subscriptions.Add(subscription);
            await _context.SaveChangesAsync();

            return Ok(subscription);
        }

        [HttpGet]
        public async Task<IActionResult> GetMy()
        {
            var telegramIdStr = User.FindFirst("telegram_id")?.Value;
            if (!long.TryParse(telegramIdStr, out var telegramId))
                return Unauthorized();

            var user = await _context.Users
                .FirstOrDefaultAsync(u => u.TelegramId == telegramId);

            if (user == null)
                return NotFound();

            var subs = await _context.Subscriptions
                .Where(s => s.UserId == user.Id)
                .ToListAsync();

            return Ok(subs);
        }
    }
}