# RealWorld Mono Repository

*RealWorld* ist ein Projekt mit dem Ziel eine Beispielanwendung mit dem Namen *Conduit* bereitzustellen, für das man mit verschiedenen Programmiersprachen und Frameworks einen Online-Blog erstellen kann.
Details können [hier](https://main--realworld-docs.netlify.app/) gefunden werden.

## Entwicklungsumgebung

### Notwendig

- git client (e.g. https://git-scm.com/)
- [VSCode](https://code.visualstudio.com/)

#### DotNet

- [DotNet SDK 8.0](https://dotnet.microsoft.com/en-us/download/dotnet/8.0)
- VSCode Plugin [C# Dev Kit](https://marketplace.visualstudio.com/items?itemName=ms-dotnettools.csdevkit)

#### Vue

- [Node.js 24](https://nodejs.org/en/download)
- [pnpm](https://pnpm.io/installation#using-npm)
- VSCode Plugin [Vue (Official)](https://marketplace.visualstudio.com/items?itemName=Vue.volar)

#### Kotlin

- OpenJDK 21 (z.B. https://learn.microsoft.com/en-us/java/openjdk/download)
- [Android Studio](https://developer.android.com/studio?hl=de#android-studio-downloads) für Kotlin

Zusätzlich wird entweder ein physisches Android Gerät benötigt oder es muss der Android Emulator genutzt werden. Dieser ist Teil von Android Studio und kann darüber installiert werden.

### Empfohlen

Die folgenden VSCode-Plugins können zusätzlich helfen:
- [Git Graph](https://marketplace.visualstudio.com/items?itemName=mhutchie.git-graph)
  - Bessere übersicht über eurer Git-Repo und eure Commits
- [Markdown All in One](https://marketplace.visualstudio.com/items?itemName=yzhang.markdown-all-in-one)
  - Preview von Markdown-Dateien
- [Indent-Rainbow](https://marketplace.visualstudio.com/items?itemName=oderwat.indent-rainbow)
  - Hilft euch euren Code korrekt einzurücken und zeigt dieses über verschiedene Farben an
- [PlantUML](https://marketplace.visualstudio.com/items?itemName=jebbs.plantuml)
  - Zeigt auch die plantuml Diagramme direkt in VSCode an.

#### Vue

Für das Vue Projekt sind zudem auch folgende VSCode-Plugins hilfreich:
- [Vite](https://marketplace.visualstudio.com/items?itemName=antfu.vite)
- [Vitest](https://marketplace.visualstudio.com/items?itemName=vitest.explorer)
- [Playwright](https://marketplace.visualstudio.com/items?itemName=ms-playwright.playwright)

## Setup

1. Ladet euer Repo mit Hilfe von Git herunter

`git clone https://code.ovgu.de/csse-sdp/sose2026/<your-repo-name> realworld`

2. Das Repo enthält folgende unterprojekte:

```
 - apps
   - dotnet # Backend
   - kotlin # Android Frontend
   - vue # Web Frontend
```

3. Öffne die Workspace Datei `realworld-mono.code-workspace` mit VSCode und den Ordner `apps/kotlin` mit Android Studio

#### DotNet

- Navigiere in der Konsole nach `apps/dotnet`
- Führe `dotnet restore` aus, um alle Abhängigkeiten zu installieren.
- Führe `Api.csproj` aus, um das Backend zu starten
  - **Option A**
    - `Launch Api` in VSCode ausführen
  - **Option B**
    - `dotnet run --project .\src\Api\Api.csproj` ausführen
- Öffne `http://localhost:5000/api/swagger` um alle Api-Endpunkte einzusehen und zu testen
- Für weitere Konfigurationen siehe [Microsoft Dokumentation](https://learn.microsoft.com/en-us/aspnet/core/fundamentals/environments?view=aspnetcore-8.0)

#### Vue

- Navigiere in der Konsole nach `apps/vue`
- Führe `pnpm install` aus, um alle Abhängigkeiten zu installieren.
- Führe das Projekt aus, um die Webanwendung zu starten
  - **Option A**
    - `Launch WebApp` in VSCode ausführen
  - **Option B**
    - in der Konsole nach `apps/vue` navigieren
    - `pnpm run dev` ausführen
- Öffne `http://localhost:4173` um die Webanwend zu nutzen

#### Kotlin

- Android Studio installiert automatisch über Gradle alle Abhängigkeiten
  - Warte, bis dieses abgeschlossen ist
- Verbinde oder emuliere ein Android Smartphone
  - **Option A**
    - erstelle im _Device Manager_ einen Emulator
    - der Emulator öffnet sich dann automatisch in der Seitenleiste
  - **Option B**
    - verbinde ein Android-Smartphone mit deinem PC
    - die App wird dann automatisch auf deinem Smartphone ausgeführt.