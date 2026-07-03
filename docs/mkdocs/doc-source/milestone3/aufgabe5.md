# Dokumentation der Code-Änderungen (WYSIWYG & Bild-Erweiterung)

Diese Dokumentation beschreibt alle Anpassungen im Vue-Frontend, die zur Implementierung des WYSIWYG-Editors und der artikelbezogenen Bildverwaltung vorgenommen wurden. Alle Änderungen wurden rein clientseitig gelöst, sodass keine Anpassungen am .NET-Backend erforderlich waren.

---

## 1. Komponente: Artikel-Metadaten (Anzeige Edit-Button)
* **Datei-Pfad:** `src/components/ArticleDetailMeta.vue`
* **Art der Änderung:** Visuelle Stil-Anpassung (CSS-Klassen)

### Beschreibung der Änderungen:
* **Farbänderung des Bearbeiten-Buttons:** Der Button für „Edit Article“ besaß im ursprünglichen Code die Bootstrap-Klasse `btn-outline-secondary`. Dies führte dazu, dass er grau, unscheinbar und optisch wie ausgegraut/deaktiviert wirkte.
* **Umstellung auf Primärfarbe:** Die Klasse wurde auf `btn-primary` geändert. Dadurch wird der Button nun in einem klaren, gut sichtbaren Blau (bzw. der primären Akzentfarbe des Themes) dargestellt. Diese Änderung wurde an beiden Stellen im Template durchgeführt (sowohl im oberen Banner als auch im unteren Aktionsbereich).

---

## 2. Seite: Artikel erstellen und bearbeiten (Editor)
* **Datei-Pfad:** `src/pages/EditArticle.vue`
* **Art der Änderung:** Erweiterung der Benutzeroberfläche (UI), Integration der Live-Vorschau und Implementierung des Bild-Uploads

### Beschreibung der Änderungen:

* **Einführung des WYSIWYG-Live-Previews:**
  * Das Eingabefeld für den Artikel-Inhalt (`textarea`) wurde in ein zweispaltiges Grid-Layout (`row` mit zwei `col-md-6`-Spalten) eingebettet.
  * Auf der linken Seite befindet sich das gewohnte Markdown-Eingabefeld.
  * Auf der rechten Seite wurde eine Live-Vorschau-Box hinzugefügt, die mittels `v-html` den formatierten Text direkt darstellt.
  * Zur Konvertierung wird die in deiner Codebase bereits vorhandene Funktion `renderMarkdown` aus `src/plugins/marked.ts` reaktiv über eine `computed`-Property aufgerufen, wodurch eine exakte und sichere (XSS-geschützte) Darstellung gewährleistet ist.

* **Artikelbezogener Bilder-Upload (Nativ):**
  * Es wurde eine Upload-Sektion integriert, die über ein `v-if="slug"` gesteuert wird. Bilder können hochgeladen werden, sobald der Artikel einen gültigen Slug besitzt (entweder beim Bearbeiten eines bestehenden Artikels oder nach dem ersten Speichern).
  * Die Funktion `onImageUpload` verarbeitet das ausgewählte Bild als `FormData` mit dem exakt vom Backend erwarteten Key `'file'`.
  * Der Upload erfolgt über einen nativen `fetch`-Request an den Backend-Endpunkt `${CONFIG.API_HOST}/api/images/articles/${slug.value}`.
  * Für die Autorisierung wird das korrekte Authentifizierungs-Token ohne Umwege aus der reaktiven Variable deines Pinia-Stores ausgelesen und als `Token <token>` im `Authorization`-Header mitgeschickt (`userStore.user?.token`).

* **Persistentes Laden der Bild-Liste aus dem Backend:**
  * Beim Laden eines bestehenden Artikels (`fetchArticle`) greift die Komponente nun direkt auf das vom Backend-Mapper (`ArticlesMapper.cs`) standardmäßig bereitgestellte Array `images` zu.
  * Dadurch werden alle jemals für diesen Artikel hochgeladenen Bilder in der Liste `uploadedImages` angezeigt, selbst wenn sie (noch) nicht im Markdown-Inhalt verbaut wurden. Eine unzuverlässige Speicherung im lokalen Browser-Laufwerk (`localStorage`) ist somit hinfällig.

* **Sicherstellung der Datenkonsistenz & Bereinigung:**
  * **Kopier-Funktion:** Jedes hochgeladene Bild wird unterhalb des Uploaders gelistet. Ein Klick auf „Kopieren“ legt die fertige Markdown-Syntax `![alt text](URL "Title")` direkt in die Zwischenablage des Nutzers, damit der Link bequem im Text eingefügt werden kann.
  * **Entfernung der Lösch-Funktion:** Da das Backend keinen Endpunkt zum Löschen von Bildern anbietet und Modifikationen dort ausgeschlossen sind, wurde die `removeImageLink`-Funktion sowie der dazugehörige „Entfernen“-Button restlos aus dem Code entfernt, um Verwirrungen und Dateninkonsistenzen (Bild in UI weg, aber in der DB vorhanden) zu vermeiden.

  ## Backend Changes
* **Endpoint:** `POST /api/images/articles/{slug}`.
* **Access:** Requires authorization. The system verifies that the authenticated user is the author of the specified article; a mismatch results in a `403 Forbidden` response.
* **Validation:** Accepts only `.jpg`, `.jpeg`, and `.png` extensions.
* **Storage Logic:** 
  * Each uploaded file receives a unique GUID-based filename (`{Guid.NewGuid()}{ext}`) to support multiple images per article without overwriting.
* **Database Architecture:**
  * A new `ArticleImages` table (`DbSet<ArticleImage>`) was introduced.
  * The `ArticleImage` entity structure includes an `Id` (Guid), a `Url` (string with a maximum length of 2000 characters), and an `ArticleId` (Guid) acting as a foreign key to the `Articles` table.
  * A one-to-many relationship was established, allowing the `Article` entity to hold a collection of `ArticleImage` objects.
* **Data Retrieval:**
  * The `ConduitRepository` was updated to eagerly load associated images using `.Include(x => x.Images)` during database queries.
  * The `ArticleResponse` model and `ArticlesMapper` were extended to expose a `List<string> Images`, mapping the related image URLs into the API JSON response.