using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using TodoApi.Data;
using TodoApi.Models;
using TodoApi.Application.Handlers;
using TodoApi.Application.Handlers.CreateSubscription;
using TodoApi.Application.Handlers.GetMySubscriptions;
using TodoApi.Dtos;



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

        [Authorize]
        [HttpPost]
        public async Task<IActionResult> Create(
            [FromBody] CreateSubscriptionDto dto,
            [FromServices]
            IHandler<(long, CreateSubscriptionDto), CreateSubscriptionResult> handler)
        {
            var telegramId = long.Parse(
                User.FindFirst("telegram_id")!.Value);

            var result = await handler.Handle((telegramId, dto));

            if (!result.Success)
                return BadRequest(result.Error);

            return Ok();
        }


        [Authorize]
        [HttpGet]
        public async Task<IActionResult> GetMy(
            [FromServices]
            IHandler<long, GetMySubscriptionsResult> handler)
        {
            var telegramIdStr = User.FindFirst("telegram_id")?.Value;
            if (!long.TryParse(telegramIdStr, out var telegramId))
                return Unauthorized();

            var result = await handler.Handle(telegramId);

            if (!result.Success)
                return NotFound(result.Error);

            return Ok(result.Subscriptions);
        }

    }
}