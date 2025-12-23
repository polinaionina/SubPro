using NUnit.Framework;
using Moq;
using FluentAssertions;
using TodoApi.Models;
using System.IdentityModel.Tokens.Jwt;

namespace TodoApi.Services;

public class NonceStorageTest
{
    [Test]
    public void AddTryGetShouldReturnDeviceId()
    {
        var storage = new NonceStorage();

        storage.Add("nonce1", "device1");

        var result = storage.TryGet("nonce1", out var deviceId);

        result.Should().BeTrue();
        deviceId.Should().Be("device1");
    }
    
    [Test]
    public void ReturnFalseForUnknownNonce()
    {
        var storage = new NonceStorage();

        var result = storage.TryGet("unknown", out var deviceId);

        result.Should().BeFalse();
        deviceId.Should().BeNull();
    }
    
    [TestCase(null)]
    [TestCase("")]
    public void ReturnFalseForEmptyNonce(string nonce)
    {
        var storage = new NonceStorage();

        var result = storage.TryGet(nonce, out var deviceId);

        result.Should().BeFalse();
        deviceId.Should().BeNull();
    }
    
    [Test]
    public void RemoveShouldDeleteNonce()
    {
        var storage = new NonceStorage();
        storage.Add("nonce1", "device1");

        storage.Remove("nonce1");

        storage.TryGet("nonce1", out _).Should().BeFalse();
    }
}