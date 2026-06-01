using System;
using System.Security.Cryptography;
using Isopoh.Cryptography.Argon2;
using Realworlddotnet.Infrastructure.Utils.Interfaces;

namespace Realworlddotnet.Infrastructure.Utils;

public class Argon2PasswordHasher : IPasswordHasher
{
    public string HashPassword(string plaintext)
    {
        var salt = new byte[16];
        RandomNumberGenerator.Fill(salt);

        var config = new Argon2Config
        {
            Type = Argon2Type.HybridAddressing,
            TimeCost = 3,
            MemoryCost = 65536,
            Lanes = 1,
            Threads = 1,
            Password = System.Text.Encoding.UTF8.GetBytes(plaintext),
            Salt = salt
        };
        var argon2 = new Argon2(config);
        using var hash = argon2.Hash();
        return config.EncodeString(hash.Buffer);
    }

    public bool VerifyPassword(string plaintext, string storedHash)
        => Argon2.Verify(storedHash, plaintext);
}
