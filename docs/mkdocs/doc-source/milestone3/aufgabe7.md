# Dokumentation: Implementierung des Rollenmodells (RBAC)

Diese Dokumentation beschreibt die Konzeption, die Funktionsweise und die konkreten Datei-Änderungen für die Einführung des rollenbasierten Berechtigungsmodells (Role-Based Access Control) in Conduit. Das System unterscheidet nun dynamisch zwischen den Rollen **User**, **Moderator** und **Admin**[cite: 2].

---

## 1. Übersicht & Funktionsweise

Das Berechtigungsmodell baut auf einer hierarchischen Rechtestruktur auf, bei der übergeordnete Rollen alle Privilegien der untergeordneten Rollen erben[cite: 2]:

| Rolle | Erlaubte Aktionen (Eigene Entitäten) | Erlaubte Aktionen (Fremde Entitäten) |
| :--- | :--- | :--- |
| **User** | Eigene Artikel, Kommentare und Profile bearbeiten/löschen[cite: 2]. | Artikel lesen, Kommentare verfassen, Feeds nutzen[cite: 1, 2]. |
| **Moderator** | Alles, was ein `User` darf[cite: 2]. | **Fremde Kommentare löschen**[cite: 2]. |
| **Admin** | Alles, was ein `Moderator` darf[cite: 2]. | **Fremde Artikel löschen**, **Profilbilder von Nutzern löschen**, **Rollen aller Nutzer ändern**[cite: 2]. |

---

## 2. Backend-Architektur (.NET 8)

Im C#-Backend wurden die Datenmodelle, Handlers und Endpunkte angepasst, um die Rollen im System zu verankern und abzusichern[cite: 2, 5]:

* **`src/Core/Entities/User.cs`**: Einführung des Enums `UserRole` (`User = 0`, `Moderator = 1`, `Admin = 2`)[cite: 2]. Die Entität `User` besitzt nun das Property `Role` mit dem Standardwert `UserRole.User`[cite: 2].
* **`src/Core/Dto/UserDto.cs`**: Erweiterung des Datentransferobjekts `UserDto` um das Feld `Role` (`string`), um die Rolle an das Frontend auszugeben[cite: 2]. Hinzufügen des Typs `RoleUpdateDto` für Rollenänderungen[cite: 2].
* **`src/Api/Features/Users/UserEndpoints.cs`**: Implementierung des Endpunkts `PUT /api/user/{targetUsername}/role`[cite: 5]. Der Endpunkt prüft über die Rolle des Administrators[cite: 5]. Falls diese nicht vorhanden ist, wird ein `403 Forbidden` zurückgegeben[cite: 5].
* **`src/Api/Features/Images/ImageEndpoints.cs`**: Implementierung des Endpunkts `DELETE /api/images/profiles/{username}`[cite: 5]. Dieser prüft ebenfalls die Admin-Rolle ab, löscht bei Autorisierung die physische Datei des Avatars vom Server und leert den Bildpfad in der Datenbank[cite: 5].
* **`src/Api/Features/Articles/ArticlesHandler.cs`**: Die Autorisierungsprüfungen in `UpdateArticleAsync`, `DeleteArticleAsync` und `RemoveCommentAsync` wurden angepasst[cite: 5]. Lösch- und Update-Aktionen erlauben nun den Zugriff, wenn der Anforderer der Eigentümer ist ODER die erforderliche administrative Rolle (`Admin` für Artikel, `Moderator`/`Admin` für Kommentare) besitzt[cite: 5].

---

## 3. Frontend-Implementierung (Vue 3 / TypeScript)

Im Vue-Frontend wurden die Ansichten und Komponenten so modifiziert, dass administrative Steuerelemente basierend auf der Benutzerrolle ein- oder ausgeblendet werden und eine zentrale Admin-Oberfläche zur Verfügung steht[cite: 2].

### Übersicht der geänderten Dateien

* **`src/services/api.ts`**: Das automatisch generierte Interface `User` wurde um das Feld `role: string;` erweitert, um Typisierungsfehler im gesamten Projekt zu verhindern[cite: 1].
* **`src/router.ts`**: Registrierung der neuen Route `/admin` für das Dashboard[cite: 1]. Absicherung über einen `beforeEnter`-Guard, der die Rolle direkt aus dem lokalen Speicher (`userStorage.get()`) validiert[cite: 1].
* **`src/components/AppNavigation.vue`**: Erweiterung der Navigationsleiste[cite: 1]. Wenn ein Nutzer mit der Rolle `'Admin'` angemeldet ist, wird der Navigationslink zum Admin-Dashboard dynamisch in der Menüleiste eingeblendet[cite: 1].
* **`src/components/ArticleDetailMeta.vue`**: Entkopplung von Bearbeitungs- und Löschrechten[cite: 1]. Der "Delete Article"-Button nutzt das neue Computed-Property `displayDeleteButton`, welches für den Autoren *und* für Admins `true` ergibt[cite: 1].
* **`src/components/ArticleDetailComment.vue`**: Die Sichtbarkeit des Mülleimer-Symbols (`showRemove`) wurde erweitert[cite: 1]. Es wird eingeblendet, wenn der Nutzer der Verfasser ist oder die Rolle `'Moderator'` bzw. `'Admin'` besitzt[cite: 1].
* **`src/pages/Profile.vue`**: Integration eines "Delete Profile Image"-Buttons im Profilbanner[cite: 1]. Der Button ist exklusiv für Admins sichtbar und setzt einen `DELETE`-Request an das Backend ab[cite: 1].
* **`src/pages/Admin.vue`**: Zentrale Verwaltungsseite für Administratoren[cite: 1]. Bietet ein Formular zur Eingabe eines Benutzernamens und ein Dropdown zur Zuweisung einer neuen Rolle via API[cite: 1].
* **`src/pages/Admin.spec.ts`**: Komplette Test-Suite mit Vitest und Testing-Library, die das fehlerfreie Rendern und die korrekte Payload-Struktur der Admin-Aktionen prüft.

---

## 4. Datenfluss & API-Payloads

Bei der Durchführung administrativer Aktionen kommuniziert das Frontend über standardisierte JSON-Strukturen mit der API[cite: 5].

Beispiel für das Absenden einer Rollenänderung in `Admin.vue` an den Endpunkt `PUT /api/user/{username}/role`[cite: 5]:

```json
{
  "user": {
    "role": "Moderator"
  }
}