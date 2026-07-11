using Moq;
using Xunit;
using BCrypt.Net;
using Realworlddotnet.Api.Features.Users;
using Realworlddotnet.Core.Dto;
using Realworlddotnet.Core.Repositories;
using Realworlddotnet.Core.Entities;
using Realworlddotnet.Infrastructure.Utils.Interfaces;
using System.Threading;
using System.Threading.Tasks;
using Hellang.Middleware.ProblemDetails;



namespace Realworlddotnet.Unit.Tests.Features.Users;

public class PasswordHashingTest
{
    private readonly Mock<IConduitRepository> _repoMock = new();
    private readonly Mock<ITokenGenerator> _tokenMock = new();
    private readonly UserHandler _handler;

    public PasswordHashingTest()
    {
        _tokenMock.Setup(t => t.CreateToken(It.IsAny<string>(), It.IsAny<string>())).Returns("mock-token");
        _handler = new UserHandler(_repoMock.Object, _tokenMock.Object);
    }

    [Fact]
    public void Test_1_Hashing()
    {
        string rawPassword = "mysecuredpass123";

        string hashedPassword = BCrypt.Net.BCrypt.HashPassword(rawPassword);

        Assert.NotEqual(rawPassword, hashedPassword);
    }

    [Fact]
    public void Test_2_Salting()
    {
        string identicalPassword = "somepassword";
        string hash1 = BCrypt.Net.BCrypt.HashPassword(identicalPassword);
        string hash2 = BCrypt.Net.BCrypt.HashPassword(identicalPassword);

        // even with same password because of Salt hash must be different
        Assert.NotEqual(hash1,hash2);
    }

    [Fact]
    public void Test_3_Hashing_and_Salting_in_Combination()
    {
        string password = "somepassword";
        string hashedPassword = BCrypt.Net.BCrypt.HashPassword(password);


        // defines the salt from hashed password and checks if password pass.
        bool isValid = BCrypt.Net.BCrypt.Verify(password, hashedPassword);

        Assert.True(isValid);
    }

    [Fact]
    public async Task Test_4_Speichern_In_Der_Datenbank()
    {
        var newUser = new NewUserDto("db_test_user", "db@test.com", "secretPass");

        await _handler.CreateAsync(newUser, CancellationToken.None);

        _repoMock.Verify(r => r.AddUserAsync(It.Is<User>(u =>
            u.Username == "db_test_user" &&
            u.Email == "db@test.com" &&
            u.Password != "secretPass" &&
            BCrypt.Net.BCrypt.Verify("secretPass", u.Password, false, HashType.SHA384))),
            Times.Once);

            _repoMock.Verify(r => r.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Once);
    }

    // verifies that a plaintext password which naturally matches the BCrypt
    // regex pattern triggers the fallback logic and successfully completes
    // migration without locking the user out.
    [Fact]
    public async Task Test_5_Migration_With_Regex_Collision_Plaintext()
    {
        string dangerousPlaintext = "$2a$11$QZAdkC1H0hyEt5CvcvBeIOW4nO48etvNKwE5dFsRGfczvthHMXdla";
        
        var existingUser = new User
        {
            Username = "collision_user",
            Email = "collision@test.com",
            Password = dangerousPlaintext
        };

        _repoMock.Setup(r => r.GetUserByEmailAsync(existingUser.Email))
                 .ReturnsAsync(existingUser);

        var loginDto = new LoginUserDto(existingUser.Email, dangerousPlaintext);

        var result = await _handler.LoginAsync(loginDto, CancellationToken.None);

        Assert.NotNull(result);
        _repoMock.Verify(r => r.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Once);
        Assert.NotEqual(dangerousPlaintext, existingUser.Password);
        Assert.True(BCrypt.Net.BCrypt.Verify(dangerousPlaintext, existingUser.Password));
    }

    // Demonstrates BCrypt's inherent algorithmic limitation
    // where inputs are truncated at 72 bytes, proving that additional
    // characters are ignored during evaluation.
    [Fact]
    public void Test_6_BCrypt_72Byte_Truncation_EdgeCase()
    {
        string basePassword = new string('A', 72);
        string longPassword1 = basePassword + "Secret123";
        string longPassword2 = basePassword + "Malicious999";

        string hash = BCrypt.Net.BCrypt.HashPassword(longPassword1);
        bool isMatch = BCrypt.Net.BCrypt.Verify(longPassword2, hash);
        
        Assert.True(isMatch);
    }

    // Ensures that the system handles C-style null terminator strings
    // securely and does not suffer from Null-Byte Injection vulnerability.
    [Fact]
    public void Test_7_NullByte_Injection_EdgeCase()
    {
        string cleanPassword = "myNormalPassword";
        string injectedPassword = "myNormalPassword\0withNullByteAttack";

        string hash = BCrypt.Net.BCrypt.HashPassword(cleanPassword);
        bool isMatch = BCrypt.Net.BCrypt.Verify(injectedPassword, hash);
        
        Assert.False(isMatch);
    }

    // Verifies that attempting to log in with a null password does not crash the application with a 500 error but is handled securely or throws a validation exception.
    [Fact]
    public async Task Test_8_Login_With_Null_Password_Throws_Exception()
    {
        var existingUser = new User
        {
            Username = "null_pass_user",
            Email = "nullpass@test.com",
            Password = "$2a$11$QZAdkC1H0hyEt5CvcvBeIOW4nO48etvNKwE5dFsRGfczvthHMXdla"
        };

        _repoMock.Setup(r => r.GetUserByEmailAsync(existingUser.Email))
                 .ReturnsAsync(existingUser);

        var loginDto = new LoginUserDto(existingUser.Email, null!);

        await Assert.ThrowsAsync<ProblemDetailsException>(() =>
            _handler.LoginAsync(loginDto, CancellationToken.None));
    }

    // Ensures that creating a user with a null password field is prevented and handled gracefully by the validation layer before hashing occurs.
    [Fact]
    public async Task Test_9_Create_User_With_Null_Password_Throws_Exception()
    {
        var newUser = new NewUserDto("null_user", "null@test.com", null!);

        await Assert.ThrowsAsync<System.ArgumentNullException>(() =>
            _handler.CreateAsync(newUser, CancellationToken.None));
    }

    // Checks how the hashing mechanism behaves when an empty string is provided as a password, ensuring it doesn't cause unexpected sub-system behavior.
    [Fact]
    public async Task Test_10_Login_With_Empty_Password_String_Is_Rejected()
    {
        var existingUser = new User
        {
            Username = "empty_user",
            Email = "empty@test.com",
            Password = "$2a$11$QZAdkC1H0hyEt5CvcvBeIOW4nO48etvNKwE5dFsRGfczvthHMXdla"
        };

        _repoMock.Setup(r => r.GetUserByEmailAsync(existingUser.Email))
                 .ReturnsAsync(existingUser);

        var loginDto = new LoginUserDto(existingUser.Email, "");

        await Assert.ThrowsAsync<ProblemDetailsException>(() =>
            _handler.LoginAsync(loginDto, CancellationToken.None));
    }
}
