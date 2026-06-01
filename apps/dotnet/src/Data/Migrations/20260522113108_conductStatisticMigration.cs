using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace Realworlddotnet.Data.Migrations
{
    /// <inheritdoc />
    public partial class conductStatisticMigration : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<DateTime>(
                name: "Timestamp",
                table: "ArticleFavorites",
                type: "TEXT",
                nullable: true);

            migrationBuilder.CreateTable(
                name: "SearchCounts",
                columns: table => new
                {
                    KeywordId = table.Column<string>(type: "TEXT", nullable: false),
                    Count = table.Column<int>(type: "INTEGER", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_SearchCounts", x => x.KeywordId);
                });
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "SearchCounts");

            migrationBuilder.DropColumn(
                name: "Timestamp",
                table: "ArticleFavorites");
        }
    }
}
