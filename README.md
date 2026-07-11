# Conduit - Cross-Platform RealWorld Implementation

![Build Status](https://github.com/USERNAME/conduit-cross-platform/actions/workflows/ci.yml/badge.svg)

> A full-stack, cross-platform implementation of the [RealWorld (Conduit)](https://github.com/gothinkster/realworld) specification. 

This repository is a monorepo that brings together three distinct phases of development into a single, cohesive ecosystem. It showcases a modern, scalable architecture featuring a robust backend API, a reactive web application, and a native mobile client.

## 🚀 The Ecosystem

This project was built incrementally across three major stages, now unified into one repository:

1. **Backend API (.NET 8):** A robust, RESTful API built with C# and ASP.NET Core, utilizing Entity Framework Core for data persistence. It handles authentication, article management, tagging, and user profiles.
2. **Web Client (Vue.js):** A modern, responsive Single Page Application (SPA) built with Vue 3, Vite, and Pinia. It provides a seamless reading and writing experience for web users.
3. **Mobile Client (Kotlin / Android):** A native Android application written in Kotlin, featuring MVVM architecture, View Binding, and Coroutines/Flow for smooth asynchronous operations.

## ✨ Features

- **User Authentication:** Secure JWT-based registration and login.
- **Article Management:** Create, read, update, and delete (CRUD) articles with markdown support.
- **Social Features:** Favorite articles, follow/unfollow other authors, and comment on posts.
- **Global & Feed Views:** Read a personalized feed of followed authors or explore the global tag-based feed.
- **Cross-Platform Synchronization:** Any action (e.g., favoriting an article) on the Android app is instantly reflected on the Web client via the unified .NET backend.
- **Automated CI Pipeline:** Fully automated GitHub Actions workflow that builds, lints, and tests all three codebases simultaneously.

## 🏗️ Architecture & Tech Stack

### Monorepo Structure
```text
.
├── apps/
│   ├── dotnet/       # Backend API
│   ├── vue/          # Web SPA
│   └── kotlin/       # Native Android App
└── .github/          # CI/CD Pipelines
```

### Technologies Used

| Tier | Technologies |
|------|--------------|
| **Backend** | C#, .NET 8, ASP.NET Core, Entity Framework Core, SQLite |
| **Frontend Web** | Vue.js 3, Vite, Pinia, TypeScript |
| **Mobile (Android)** | Kotlin, Android SDK, MVVM, Retrofit, Navigation Component |
| **DevOps** | GitHub Actions (CI), pnpm, Gradle |

## 🛠️ Getting Started

To run the entire ecosystem locally, you will need .NET 8, Node.js (with pnpm), and Android Studio.

### 1. Run the Backend API
```bash
cd apps/dotnet
dotnet restore
dotnet ef database update --project src/Data --startup-project src/Api
dotnet run --project src/Api
```
*The API will be available at `http://localhost:5000`.*

### 2. Run the Web Application
Open a new terminal window:
```bash
cd apps/vue
pnpm install
pnpm dev
```
*The web app will be available at `http://localhost:4173`.*

### 3. Run the Android App
- Open the `apps/kotlin` directory in **Android Studio**.
- Sync the Gradle project.
- Ensure the API base URL in the app is pointing to your local machine (or an emulator-friendly address like `10.0.2.2`).
- Build and run on an emulator or physical device.

## 🧪 Testing

This project maintains a strong emphasis on reliability. Automated tests are configured for all three environments:
- **.NET:** `dotnet test` (xUnit/NUnit)
- **Vue:** `pnpm test` (Vitest / Vue Test Utils)
- **Kotlin:** `./gradlew test` (JUnit)

---

*This project was developed as a comprehensive exercise in full-stack and mobile development, demonstrating the ability to integrate diverse technology stacks into a unified product.*
