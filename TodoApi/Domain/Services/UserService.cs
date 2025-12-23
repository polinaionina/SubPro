using TodoApi.Data;
using TodoApi.Models;
using Microsoft.EntityFrameworkCore;

namespace TodoApi.Services
{
    public class UserService
    {
        private readonly AppDbContext _context;

        public UserService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<User> GetOrCreateUserAsync(TelegramAuthRequest request)
        {
            var user = await _context.Users.FirstOrDefaultAsync(u => u.TelegramId == request.Id);

            if (user != null) return user;

            user = new User
            {
                TelegramId = request.Id,
                Username = request.Username ?? "",
                FirstName = request.FirstName,
                LastName = request.LastName,
                DeviceId = request.DeviceId,
                CreatedAt = DateTime.UtcNow
            };

            _context.Users.Add(user);
            await _context.SaveChangesAsync();

            return user;
        }
    }
}
