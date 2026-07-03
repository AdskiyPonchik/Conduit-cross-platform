# Dokumentationsübersicht (FDMP)

Willkommen zur technischen Dokumentation der Systemerweiterungen. In diesem Abschnitt werden die funktionalen Erweiterungen, Bugfixes sowie die Qualitätssicherungs-Maßnahmen für das Web-Frontend zusammenfassend beschrieben.

## Bearbeitete Aufgabenstellungen

*   **[Aufgabe 2: Passwort-Bug & E2E-Tests](aufgabe2.md)**
    *   Behebung des Fehlers beim unbeabsichtigten Überschreiben/Leeren von Nutzerpasswörtern.
    *   Wasserfeste Playwright-E2E-Absicherung inklusive Auflösung von asynchronen Race Conditions.
*   **[Aufgabe 4: Profilbild-Upload & Namens-Validierung](aufgabe4.md)**
    *   Implementierung eines nativen `multipart/form-data` lokalen Bilduploads an das bestehende Backend.
    *   Native HTML5-Echtzeitvalidierung zur Sperrung von Sonderzeichen in Benutzernamen bei der Registrierung.
*   **[Aufgabe 5: Echtzeitredern von Artikel WYSIWYG](aufgabe5.md)**
    *   WYSISYG Editor für die Frontends und darstellen von mehreren Bildern in einem Artikel
*   **[Aufgabe 7: Rollenmodelle für die Administration](aufgabe7.md)**
    *   Implementierung von Rollenmodellen im Vue Frontend und Dotnet Backend

---
> **Hinweis zur Architektur:** Alle serverseitigen Schnittstellen (.NET-Backend) verbleiben exakt im bereitgestellten Originalzustand. Die funktionale Implementierung und das Abfangen von Validierungsfehlern erfolgen vollständig autark innerhalb der Vue.js-Frontendkomponenten.