using NUnit.Framework;
using Moq;
using FluentAssertions;
using TodoApi.Models;
using System.IdentityModel.Tokens.Jwt;


namespace TodoApi.Services;

[TestFixture]
public class TelegramServiceTests
{
    private TelegramService CreateServiceWithFakeConfig()
    {
        var settings = new Dictionary<string, string>
        {
            ["TelegramBot:Token"] = "fake-token",
            ["Jwt:Key"] = "bomba_this_is_a_very_long_and_secure_test_key_67_chars",
            ["Jwt:Issuer"] = "test-issuer",
            ["Jwt:Audience"] = "test-audience",
            ["Jwt:LifetimeMinutes"] = "67"
        };

        var configuration = new ConfigurationBuilder()
            .AddInMemoryCollection(settings!)
            .Build();

        var logger = Mock.Of<ILogger<TelegramService>>();

        return new TelegramService(configuration, logger);
    }
    
    [Test]
    public void ConstructorShouldThrowWhenNoHaveToken()
    {
        var config = new ConfigurationBuilder().Build();
        var logger = Mock.Of<ILogger<TelegramService>>();

        Assert.Throws<ArgumentException>(() =>
            new TelegramService(config, logger)
        );
    }
    
    [Test]
    public async Task SendMessageAsyncReturnErrorWhenMessageIsEmpty()
    {
        var service = CreateServiceWithFakeConfig();

        var result = await service.SendMessageAsync(123, "");

        result.Success.Should().BeFalse();
        result.Error.Should().Be("Message cannot be empty");
    }
    
    [Test]
    public void GenerateJwtCreateTokenWithClaims()
    {
        var service = CreateServiceWithFakeConfig();

        var user = new User
        {
            Id = 1,
            TelegramId = 123456,
            Username = "test"
        };

        var token = service.GenerateJwt(user);

        token.Should().NotBeNullOrEmpty();

        var jwt = new JwtSecurityTokenHandler().ReadJwtToken(token);

        jwt.Claims.Should().Contain(c => c.Type == "telegram_id" && c.Value == "123456");
        jwt.Claims.Should().Contain(c => c.Type == "user_id" && c.Value == "1");
    }
}