using System;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Security.Claims;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Routing;
using Microsoft.Extensions.Hosting;
using Realworlddotnet.Api.Features.Users;
using Realworlddotnet.Core.Dto;
using Realworlddotnet.Infrastructure.Extensions.Authentication;

namespace Realworlddotnet.Api.Features.Images;


public static class ImageEndpoints
{
    public static void AddImageEndpoints(this IEndpointRouteBuilder app)
    {
        var imageGroup = app.MapGroup("images").RequireAuthorization().WithTags("Images");
        imageGroup.MapPost("/", UploadImage).DisableAntiforgery();
    }

    private static async Task<IResult> UploadImage(
        IFormFile file,
        [FromServices] IHostEnvironment env,
        [FromServices] IUserHandler userHandler,
        ClaimsPrincipal claimsPrincipal,
        HttpRequest request,
        CancellationToken cancellationToken)
    {
        if (file == null || file.Length == 0)
        {
            return Results.BadRequest("No file uploaded.");
        }

        var ext = Path.GetExtension(file.FileName).ToLowerInvariant();
        var allowedExtensions = new[] { ".jpg", ".jpeg", ".png" };
        
        if (!allowedExtensions.Contains(ext))
        {
            return Results.BadRequest("Only .jpg, .jpeg, and .png files are allowed.");
        }

        var imagesDir = Path.Combine(env.ContentRootPath, "images");
        Directory.CreateDirectory(imagesDir);

        var username = claimsPrincipal.GetUsername();
        var user = await userHandler.GetAsync(username, cancellationToken);
        string currentImage = user.Image;

        string filename;


        if (!string.IsNullOrEmpty(currentImage) && currentImage.Contains("/api/images/"))
        {
            // Try to overwrite the existing file so the URL remains exactly the same
            try
            {
                var uri = new Uri(currentImage);
                filename = Path.GetFileName(uri.LocalPath);
            }
            catch
            {
                filename = $"profile_{username}{ext}";
            }
        }
        else
        {
            filename = $"profile_{username}{ext}";
        }

        var filePath = Path.Combine(imagesDir, filename);

        using (var stream = new FileStream(filePath, FileMode.Create))
        {
            await file.CopyToAsync(stream, cancellationToken);
        }

        var baseUrl = $"{request.Scheme}://{request.Host}{request.PathBase}";
        var imageUrl = $"{baseUrl}/images/{filename}";

        await userHandler.UpdateAsync(username, new UpdatedUserDto(null, null, null, imageUrl, null), cancellationToken);

        return Results.Ok(new { image = imageUrl });
    }
}