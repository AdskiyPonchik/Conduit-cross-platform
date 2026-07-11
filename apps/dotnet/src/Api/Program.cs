using Microsoft.OpenApi.Models;
using Microsoft.Extensions.FileProviders;
using Realworlddotnet.Api.Features.Articles;
using Realworlddotnet.Api.Features.Images;
using Realworlddotnet.Api.Features.Profiles;
using Realworlddotnet.Api.Features.Search;
using Realworlddotnet.Api.Features.Tags;
using Realworlddotnet.Api.Features.Users;
using Realworlddotnet.Core.Repositories;
using Realworlddotnet.Infrastructure.Utils;

var builder = WebApplication.CreateBuilder(args);

// add logging
builder.Host.UseSerilog((hostBuilderContext, services, loggerConfiguration) =>
{
    loggerConfiguration.ConfigureBaseLogging("realworldDotnet");
    loggerConfiguration.AddApplicationInsightsLogging(services, hostBuilderContext.Configuration);
});

// setup database connection (used for file based SQLite).
#pragma warning disable S125
// to use in memory SQLite use: "Filename=:memory:";
#pragma warning restore S125
const string connectionString = "Filename=../../realworld.db";

builder.Services.AddCors(options =>
{
    options.AddDefaultPolicy(policy =>
    {
        policy
            .SetIsOriginAllowed(origin =>
            {
                if (Uri.TryCreate(origin, UriKind.Absolute, out var uri))
                {
                    return uri.Host == "localhost";
                }
                return false;
            })
            .AllowAnyHeader()
            .AllowAnyMethod()
            .AllowCredentials();
    });
});

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.SupportNonNullableReferenceTypes();
    c.SwaggerDoc("v1", new OpenApiInfo { Title = "realworlddotnet", Version = "v1" });

    // Add Token Authentication (RealWorld spec)
    c.AddSecurityDefinition("Token", new OpenApiSecurityScheme
    {
        Description = "JWT Authorization header using the Token scheme. Enter 'Token' [space] and then your token in the text input below.",
        Name = "Authorization",
        In = ParameterLocation.Header,
        Type = SecuritySchemeType.ApiKey,
        Scheme = "Token"
    });

    c.AddSecurityRequirement(new OpenApiSecurityRequirement
    {
        {
            new OpenApiSecurityScheme
            {
                Reference = new OpenApiReference
                {
                    Type = ReferenceType.SecurityScheme,
                    Id = "Token"
                }
            },
            Array.Empty<string>()
        }
    });
});

builder.Services.AddScoped<IConduitRepository, ConduitRepository>();
builder.Services.AddScoped<IUserHandler, UserHandler>();
builder.Services.AddScoped<IArticlesHandler, ArticlesHandler>();
builder.Services.AddScoped<ITagsHandler, TagsHandler>();
builder.Services.AddScoped<IProfilesHandler, ProfilesHandler>();
builder.Services.AddScoped<ISearchHandler, SearchHandler>();
builder.Services.AddSingleton<CertificateProvider>();

builder.Services.AddSingleton<ITokenGenerator>(container =>
{
    var logger = container.GetRequiredService<ILogger<CertificateProvider>>();
    var certificateProvider = new CertificateProvider(logger);
    var cert = certificateProvider.LoadFromFile("certificate.pfx", "password");

    return new TokenGenerator(cert);
});

builder.Services.AddAuthorization();
builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme).AddJwtBearer();

builder.Services.AddOptions<JwtBearerOptions>(JwtBearerDefaults.AuthenticationScheme)
    .Configure<ILogger<CertificateProvider>>((o, logger) =>
    {
        var certificateProvider = new CertificateProvider(logger);
        var cert = certificateProvider.LoadFromFile("certificate.pfx", "password");

        o.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateAudience = false,
            ValidateIssuer = false,
            IssuerSigningKey = new RsaSecurityKey(cert.GetRSAPublicKey())
        };
        o.Events = new JwtBearerEvents { OnMessageReceived = CustomOnMessageReceivedHandler.OnMessageReceived };
    });

// for file-based SQLite each DbContext gets its own connection via the connection string
builder.Services.AddDbContext<ConduitContext>(options => { options.UseSqlite(connectionString); });

ProblemDetailsExtensions.AddProblemDetails(builder.Services);
builder.Services.ConfigureOptions<ProblemDetailsLogging>();

var app = builder.Build();
app.UseCors();
// Configure the HTTP request pipeline.
Log.Information("Start configuring http request pipeline");

// Apply pending migrations at startup so schema and migration history stay in sync.
using (var scope = app.Services.CreateScope())
{
    using var context = scope.ServiceProvider.GetService<ConduitContext>();
    context?.Database.Migrate();
}

app.UseSerilogRequestLogging(options =>
    options.EnrichDiagnosticContext = (diagnosticContext, httpContext) =>
        diagnosticContext.Set("UserId", httpContext.User.FindFirstValue(ClaimTypes.NameIdentifier) ?? "")
);

var imagesPath = Path.Combine(builder.Environment.ContentRootPath, "images");
Directory.CreateDirectory(imagesPath);

app.UsePathBase("/api");

app.UseStaticFiles(new StaticFileOptions
{
    FileProvider = new PhysicalFileProvider(imagesPath),
    RequestPath = "/images"
});

app.UseProblemDetails();
app.UseAuthentication();
app.UseAuthorization();

app.AddTagsEndpoints();
app.AddProfilesEndpoints();
app.AddArticlesEndpoints();
app.AddUserEndpoints();
app.AddSearchEndpoints();
app.AddImageEndpoints();

app.UseSwagger();
app.UseSwaggerUI(c => c.SwaggerEndpoint("/api/swagger/v1/swagger.json", "realworlddotnet v1"));


try
{
    Log.Information("Starting web host");
    app.Run();
    return 0;
}
catch (Exception ex)
{
    Log.Fatal(ex, "Host terminated unexpectedly");
    return 1;
}
finally
{
    Log.CloseAndFlush();
    Thread.Sleep(2000);
}
