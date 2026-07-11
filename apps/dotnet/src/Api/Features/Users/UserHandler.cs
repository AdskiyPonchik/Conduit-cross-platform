using BCrypt.Net;
using Realworlddotnet.Core.Dto;
using Realworlddotnet.Core.Repositories;
using Realworlddotnet.Core.Entities;
using System.Text.RegularExpressions;

namespace Realworlddotnet.Api.Features.Users;

public class UserHandler(IConduitRepository repository, ITokenGenerator tokenGenerator)
    : IUserHandler
{
    public async Task<UserDto> CreateAsync(NewUserDto newUser, CancellationToken cancellationToken)
    {
        // Hashing the password of new user BEFORE the entity is created
        var hashedPassword = BCrypt.Net.BCrypt.HashPassword(newUser.Password);

        var user = new User
        {
            Username = newUser.Username,
            Email = newUser.Email,
            Password = hashedPassword
        };
        await repository.AddUserAsync(user);
        await repository.SaveChangesAsync(cancellationToken);
        var token = tokenGenerator.CreateToken(user.Username, user.Role.ToString());
        return new UserDto(user.Username, user.Email, token, user.Bio, user.Image, user.Role.ToString());
    }

    public async Task<UserDto> UpdateAsync(
        string username, UpdatedUserDto updatedUser, CancellationToken cancellationToken)
    {
        var user = await repository.GetUserByUsernameAsync(username, cancellationToken);
        if (!string.IsNullOrEmpty(updatedUser.Password))
        {
            // creating new object but with changed password
            updatedUser = updatedUser with
            {
                Password = BCrypt.Net.BCrypt.HashPassword(updatedUser.Password)
            };
        }

        user.UpdateUser(updatedUser);

        await repository.SaveChangesAsync(cancellationToken);
        var token = tokenGenerator.CreateToken(user.Username, user.Role.ToString());
        return new UserDto(user.Username, user.Email, token, user.Bio, user.Image, user.Role.ToString());
    }

    public async Task<UserDto> LoginAsync(LoginUserDto login, CancellationToken cancellationToken)
    {
        var user = await repository.GetUserByEmailAsync(login.Email);
        if (user == null) throw new ProblemDetailsException(422, "Incorrect Credentials");

        bool isValid = false;
        bool needsMigration = false;

        // 1. If it looks like BCrypt, first check it as a hash
        if (IsBCryptHash(user.Password))
        {
            try
            {
                isValid = BCrypt.Net.BCrypt.Verify(login.Password, user.Password);
            }
            catch
            {
                isValid = false; // in case of an invalid format within BCrypt
            }
        }

        // 2. fallback: if there is no match (or if this is a regex collision and the database contained plaintext)
        if (!isValid)
        {
            isValid = user.Password == login.Password;
            if (isValid)
            {
                needsMigration = true; // Password matched as plain text -> time to hash.
            }
        }

        if (!isValid) throw new ProblemDetailsException(422, "Incorrect Credentials");

        // 3. We save the updated hash (EF will now write it).
        if (needsMigration)
        {
            user.Password = BCrypt.Net.BCrypt.HashPassword(login.Password);
            await repository.SaveChangesAsync(cancellationToken);
        }

        var token = tokenGenerator.CreateToken(user.Username, user.Role.ToString());
        return new UserDto(user.Username, user.Email, token, user.Bio, user.Image, user.Role.ToString());
    }

    public async Task<UserDto> GetAsync(string username, CancellationToken cancellationToken)
    {
        var user = await repository.GetUserByUsernameAsync(username, cancellationToken);
        var token = tokenGenerator.CreateToken(user.Username, user.Role.ToString());
        return new UserDto(user.Username, user.Email, token, user.Bio, user.Image, user.Role.ToString());
    }

    public async Task<UserDto> UpdateRoleAsync(string targetUsername, string roleName, CancellationToken cancellationToken)
    {
        if (!Enum.TryParse<UserRole>(roleName, true, out var role))
        {
            throw new ProblemDetailsException(422, $"Invalid role: {roleName}");
        }

        var user = await repository.GetUserByUsernameAsync(targetUsername, cancellationToken);
        user.Role = role;
        await repository.SaveChangesAsync(cancellationToken);
        var token = tokenGenerator.CreateToken(user.Username, user.Role.ToString());
        return new UserDto(user.Username, user.Email, token, user.Bio, user.Image, user.Role.ToString());
    }

    private static bool IsBCryptHash(string password)
    {
        return Regex.IsMatch(password, @"^\$2[ayb]\$[0-9]{2}\$[./A-Za-z0-9]{53}$");
    }
}
