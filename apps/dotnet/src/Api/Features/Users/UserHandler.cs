using Realworlddotnet.Core.Dto;
using Realworlddotnet.Core.Entities;
using Realworlddotnet.Core.Repositories;
using Realworlddotnet.Infrastructure.Utils.Interfaces;

namespace Realworlddotnet.Api.Features.Users;

public class UserHandler(
    IConduitRepository repository,
    ITokenGenerator tokenGenerator,
    IPasswordHasher passwordHasher)
    : IUserHandler
{
    public async Task<UserDto> CreateAsync(NewUserDto newUser, CancellationToken cancellationToken)
    {
        var user = new User(newUser)
        {
            Password = passwordHasher.HashPassword(newUser.Password)
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
        var hashedPassword = updatedUser.Password != null
            ? passwordHasher.HashPassword(updatedUser.Password)
            : null;
        user.UpdateUser(updatedUser with { Password = hashedPassword });
        await repository.SaveChangesAsync(cancellationToken);
        var token = tokenGenerator.CreateToken(user.Username, user.Role.ToString());
        return new UserDto(user.Username, user.Email, token, user.Bio, user.Image, user.Role.ToString());
    }

    public async Task<UserDto> LoginAsync(LoginUserDto login, CancellationToken cancellationToken)
    {
        var user = await repository.GetUserByEmailAsync(login.Email);

        if (user == null)
        {
            throw new ProblemDetailsException(422, "Incorrect Credentials");
        }

        var passwordValid = passwordHasher.VerifyPassword(login.Password, user.Password);

        if (!passwordValid)
        {
            throw new ProblemDetailsException(422, "Incorrect Credentials");
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
}
