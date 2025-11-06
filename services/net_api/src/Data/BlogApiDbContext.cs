using Microsoft.EntityFrameworkCore;

public sealed class BlogApiDbContext : DbContext
{
    private IConfiguration _configuration;

    public DbSet<User> Users { get; set; }
    public DbSet<Post> Posts { get; set; }
    public DbSet<Tag> Tags { get; set; }
    public DbSet<Comment> Comments { get; set; }

    public BlogApiDbContext(DbContextOptions<BlogApiDbContext> options, IConfiguration configuration)
    : base(options)
    {
        _configuration = configuration;
    }

    protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
    {
        if (optionsBuilder.IsConfigured)
        {
            // Skip if already configured
            return;
        }

        string? connectionString = _configuration.GetConnectionString("Default");
        if (string.IsNullOrEmpty(connectionString))
        {
            throw new InvalidOperationException("Connection string \"Default\" not found !");
        }
        optionsBuilder.UseNpgsql(connectionString).UseSeeding((context, _) =>
        {
            IConfigurationSection seedingSection = _configuration.GetRequiredSection("Seeding");

            User? admin = context.Set<User>().FirstOrDefault(u => u.Email == "admin@blog.com");
            if (admin == null)
            {
                string? adminEmail = seedingSection["AdminUser:Email"];
                string? adminName = seedingSection["AdminUser:Name"];
                string? adminPassword = seedingSection["AdminUser:Password"];
                if (adminEmail == null || adminName == null || adminPassword == null)
                {
                    throw new InvalidOperationException("AdminUser seeding data is missing");
                }

                context.Set<User>().Add(new User
                {
                    Email = adminEmail,
                    Name = adminName,
                    Password = adminPassword
                });
                context.SaveChanges();
            }
        });
    }
}