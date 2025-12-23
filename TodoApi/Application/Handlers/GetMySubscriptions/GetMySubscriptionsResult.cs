using TodoApi.Models;

namespace TodoApi.Application.Handlers.GetMySubscriptions;

public class GetMySubscriptionsResult
{
    public bool Success { get; init; }
    public string? Error { get; init; }
    public List<Subscription>? Subscriptions { get; init; }
}
