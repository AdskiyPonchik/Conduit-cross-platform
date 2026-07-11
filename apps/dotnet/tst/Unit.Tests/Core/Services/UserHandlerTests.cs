using Moq;
using Xunit;
using BCrypt.Net;
using Realworlddotnet.Api.Features.Users;
using Realworlddotnet.Core.Dto;
using Realworlddotnet.Core.Repositories;
using Realworlddotnet.Core.Entities;
using Realworlddotnet.Infrastructure.Utils.Interfaces;
using System.Runtime.CompilerServices;
using System.Threading.Tasks;
using System.Threading;
using Microsoft.EntityFrameworkCore;
using Hellang.Middleware.ProblemDetails;
using Microsoft.AspNetCore.Mvc.DataAnnotations;

namespace Realworlddotnet.Unit.Tests.Features.Users;

// Working with pattern AAA(Arrange, Act, Assert).
// Blueprint of it I'll put in UpdateAsync_WithoutPassword_ShouldNotChangeOldHash().

public class UserHandlerTests
{
    private readonly Mock<IConduitRepository> _repoMock = new();
    private readonly Mock<ITokenGenerator> _tokenMock = new();
    private readonly UserHandler _handler;

    public UserHandlerTests()
    {
        _tokenMock.Setup(t => t.CreateToken(It.IsAny<string>(), It.IsAny<string>())).Returns("mock-token");
        _handler = new UserHandler(_repoMock.Object, _tokenMock.Object);
    }

    [Fact]
    public async Task CreateAsync_ShouldHashPassword()
    {
        var newUser = new NewUserDto("testuser", "test@test.com", "secret123");

        await _handler.CreateAsync(newUser, CancellationToken.None);

        _repoMock.Verify(r => r.AddUserAsync(It.Is<User>(u =>
        u.Password != "secret123" && BCrypt.Net.BCrypt.Verify("secret123", u.Password, false, HashType.SHA384))),
        Times.Once
        );
    }


    [Fact]
    public async Task LoginAsync_WithKlartext_ShouldMigrateToHash()
    {
        var login = new LoginUserDto("test@test.com", "old-password");
        var password = "old-password";
        var userInDb = new User {
            Email = "test@test.com",
            Password = password,
            Username = "olduser"
        };

        _repoMock.Setup(r => r.GetUserByEmailAsync(login.Email)).ReturnsAsync(userInDb);
        _tokenMock.Setup(t => t.CreateToken(userInDb.Username, It.IsAny<string>())).Returns("mock-token");

        await _handler.LoginAsync(login, CancellationToken.None);

        bool isVerifiable = BCrypt.Net.BCrypt.Verify(password, userInDb.Password);
        Assert.True(isVerifiable, "Password must be encrypted after migration!");
        _repoMock.Verify(r => r.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Once);
    }

    [Fact]
    public async Task LoginAsync_WithWrongPassword_ShouldThrow()
    {
        var login = new LoginUserDto("test@test.com", "wrong");
        var userInDb = new User {
            Email = "test@test.com",
            Password = BCrypt.Net.BCrypt.HashPassword("correct"),
            Username = "user"
        };

        _repoMock.Setup(r => r.GetUserByEmailAsync(login.Email)).ReturnsAsync(userInDb);

        await Assert.ThrowsAsync<ProblemDetailsException>(() =>
            _handler.LoginAsync(login, CancellationToken.None)
        );
    }

    [Fact]
    public async Task UpdateAsync_WithNewPassword_ShouldHashNewPassword()
    {
        var username = "testuser";


        var userInDb = new User
        {
            Username=username,
            Password=BCrypt.Net.BCrypt.HashPassword("old-password")
        };

        var updatedDto = new UpdatedUserDto("new_username", "new@test.com", "bio", "image", "new-secret");

        _repoMock.Setup(r => r.GetUserByUsernameAsync(username, It.IsAny<CancellationToken>()))
             .ReturnsAsync(userInDb);

        await _handler.UpdateAsync(username, updatedDto, CancellationToken.None);

        bool isNewPasswordHashed = BCrypt.Net.BCrypt.Verify("new-secret", userInDb.Password);
        Assert.True(isNewPasswordHashed, "New password must be hashed");

        _repoMock.Verify(r => r.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Once);

    }


    [Fact]
    public async Task UpdateAsync_WithoutPassword_ShouldNotChangeOldHash()
    {
        // Arrange
        var username = "testuser";
        var userInDb = new User
        {
            Username = username,
            Password = BCrypt.Net.BCrypt.HashPassword("old-password")
        };
        var updatedDto = new UpdatedUserDto("new_username", "new@test", "bio", "image", null);

        _repoMock.Setup(r => r.GetUserByUsernameAsync(username, It.IsAny<CancellationToken>())).ReturnsAsync(userInDb);
        //

        // Act
        await _handler.UpdateAsync(username, updatedDto, CancellationToken.None);
        //

        // Assert
        bool oldPasswordSaved = BCrypt.Net.BCrypt.Verify("old-password", userInDb.Password);
        Assert.True(oldPasswordSaved, "Old password musn't be changed without command");
        _repoMock.Verify(r => r.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Once);
    }
}
