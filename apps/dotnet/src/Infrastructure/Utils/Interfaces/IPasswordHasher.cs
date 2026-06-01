namespace Realworlddotnet.Infrastructure.Utils.Interfaces;

public interface IPasswordHasher
{
    string HashPassword(string plaintext);
    bool VerifyPassword(string plaintext, string storedHash);
}
