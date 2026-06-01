# Meilenstein 3

Sie sind Entwickelnde für ein Softwareunternehmen, das eine Webseitenapplikation für das Verfassen, Ändern und Verwalten von Blogartikeln umsetzen möchte. Hierfür soll der Bestandscode von Conduit verwendet werden. Conduit ist eine Beispielapplikation des [RealWorld](https://main--realworld-docs.netlify.app/)-Projektes, um verschiedene Frameworks und Programmiersprachen für eine vorher spezifizierte Applikation zu verwenden.

Für die angestrebte Webseitenapplikation Conduit wurden drei Softwareteile der Client-Server-Anwendung in Form eines Mono-Repository zusammengefasst. Diese enthalten:

- ein Backend auf Basis von ASP.Net, geschrieben in C#
- ein Frontend für Webbrowser auf Basis von Vue.js, geschrieben in Typescript
- ein Frontend für eine native Android Applikation, geschrieben Kotlin

Die drei Softwareteile sind im Unterordner `apps` zu finden. Für das Mono-Repository gibt es eine [Projekt Readme](./readme_project.md), wie das Projekt einzurichten ist.

## Zusammenfassung

Als Entwickelnde besteht nun die Aufgabe das Conduit kundenspezifisch weiterzuentwickeln. So ermöglicht Conduit zwar grundsätzlich die Formatierung der Artikel mit Markdown, aber nicht alle Frontends unterstützen dessen Darstellung. Entsprechend sollen die beide Frontends (natives Frontend in Kotlin bzw. das Web-Frontend in Vue.js/Typescript) angepasst werden, dass sie die Formatierung von Artikeln bestmöglich unterstützen. Darüber hinaus bedarf es einer Überarbeitung der Nutzerprofile. Frontend Tests sollen dabei die korrekte Funktionsweise sicherstellen.
Die Softwareentwicklung soll neben den Codeanpassungen aktiv die Versionierung als auch automatische Builds verwenden. Ein Review Prozess und die Nutzung von statischer Codeanalyse sollen dabei die Qualität des Codes erhöhen.

Die Aufgaben sind zu erledigen und im Repository hochzuladen. Dies erfolgt durch Commits und Push via Git an das jeweilige Projekt-Repository. Die Abgabefrist ist der `03.07.2026 23:59 Uhr (MESZ)`. Der finale Commit ist mit
```[bash]
git tag abgabe-meilenstein3
```
zu taggen und im Repository mit
```[bash]
git push origin tag abgabe-meilenstein3
```
hochzuladen. Prüfen Sie im Team vor der Abgabedeadline, dass die korrekte Version getagged und hochgeladen wurde.

## Meilensteinaufgabe

1. Code Reviews
	- Auch in diesem Meilenstein, sollen Sie das Issue-Tracking sowie eine Branching-Strategie wie im Meilenstein 2 verwenden (u.a., Issues mit dem Template verwenden und jedes Issue mit min. einem separaten Branch).
	- Die Merge-Requests von Gitlab sind aktiv *für alle Merges in den main-Branch* zu nutzen.
	- Nutzen Sie vor dem Merge das "Review"-Feature und kontrollieren Sie Ihre Änderungen.
    	+ Kommentare im Merge-Request selbst
    	+ Kommentare zu konkreten Änderungen in den `Changes`
    	+ Nutzen Sie `Review approval`, um die Änderungen abschließend zu bewerten
	- Ein Review muss immer von einer Person erfolgen, die keine Änderungen zu dem Merge beigetragen hat.
	- Jede Person muss dabei mindestens einmal die Rolle eines Entwicklers und einmal des Reviewers eingenommen haben.

2. Frontend Tests in Vue
	- Aktuell gibt es noch einen Bug im Frontend, der auf schlechtes UI-Design zurückzuführen ist.
	- Wird das Profil eines Nutzers aktualisiert, wird immer auch das Passwort geändert, auch wenn das Passwortfeld leer gelassen wird.
    	+ [ ] Replizieren Sie den Fehler und dokumentieren Sie ihre Erkenntnisse in einem Issue.
    	+ [ ] Schreiben Sie mit `playwright` Frontend-Tests, die diesen Anwendungsfall testen.
    	+ [ ] Konzeptionieren Sie eine mögliche Lösung für diesen Bug und implementieren Sie diese.
    	+ [ ] Das korrekte Beheben des Fehlers ist mit dem Frontend Test sicherzustellen.

3. Frontend Tests in Kotlin
	- Schreiben Sie Frontend Tests für die nachstehenden Anwendungsfälle
        + [ ] Suche nach `swift`
          + Anmelden
          + Suchwort eingeben
          + Verifizieren, dass die erste Seite der Suchergebnisse angezeigt wird
        + [ ] Suche nach `swift wissen`
          + Anmelden
          + Suchwort eingeben
          + Verifizieren, dass genau ein Suchergebnis angezeigt wird
        + [ ] Suche nach `foo`
          + Anmelden
          + Suchwort eingeben
          + Verifizieren, dass der "Keine Ergebnisse" Hinweis angezeigt wird
    - Gehen Sie bei den Frontend-Tests wie folgt vor
    	+ [ ] Konzeptionieren Sie den Ablauf des Tests und dokumentieren Sie diesen in einem Issue
    	+ [ ] Schreiben Sie mit `Expresso` Frontend Tests, um die Anwendungsfälle zu testen.
    	+ [ ] Konzeptionieren Sie eine mögliche Lösung für diesen Bug und implementieren Sie diese.
    	+ [ ] Das korrekte Beheben des Fehlers ist mit dem Frontend-Test sicherzustellen.

4. Profilbilder
	- Vue-Frontend
		+ [ ] Es soll für authentifizierte Nutzer unter Settings möglich sein, beim Feld für das Profilbild über einen File-Dialog ein lokales Bild auszuwählen.
		+ [ ] Nach der Auswahl eines Bildes soll der lokale Dateipfad des Bildes im vue-Frontend angezeigt werden.
		+ [ ] Erst mit Klick auf "Update Settings" soll das Bild über einen eigenständigen dedizierten Endpunkt für Bilder hochgeladen werden.
		+ [ ] Zum Anzeigen des Bildes kann vereinfacht eine URL seitens des Backends zum Bild verwendet werden (bspw. `localhost:8081/api/images/123877631.jpg`).
	- Kotlin-Frontend
		+ [ ] Es soll für authentifizierte Nutzer unter Profile möglich sein, beim Feld für das Profilbild über einen File-Dialog ein Bild auszuwählen.
		+ [ ] Es soll ebenso möglich sein, ein neues Bild über die Kamera innerhalb der vue-Applikation aufzunehmen.
		+ [ ] Nach der Auswahl eines Bildes soll der lokale Dateipfad des Bildes im Kotlin-Frontend angezeigt werden.
		+ [ ] Erst mit Klick auf "Save" soll das Bild über einen eigenständigen dedizierten Endpunkt für Bilder hochgeladen werden.
		+ [ ] Zum Anzeigen des Bildes kann vereinfacht eine URL seitens des Backends zum Bild verwendet werden (bspw. `localhost:8081/api/images/123877631.jpg`).
	- .Net-Backend
		+ [ ] Um das Backend mit einem Endpunkt zum Hochladen von Bildern zu erweitern, lesen Sie sich vorab dazu den [Blogbeitrag von ASP.Net](https://learn.microsoft.com/de-de/aspnet/core/mvc/models/file-uploads?view=aspnetcore-7.0) durch, in dem beschrieben wird, wie Dateien in ASP.Net hochgeladen, gespeichert und abgerufen werden können.
		+ [ ] Erstellen Sie einen Endpunkt, der nur für authentifzierte Nutzer verfügbar ist und über den Bilddateien (ausschließlich `*.jpg`/`*.jpeg`/`*.png`) via POST als `multipart/form-data` Encoding hochgeladen werden können .
		+ [ ] Speichern Sie die Datei im lokalen File-System des Backends, so dass Dateien eindeutig benannt und gefunden werden können (d.h., eventuelle gleichnamige Dateien überschreiben sich nicht) und das diese über das Backend aufgerufen werden können (bspw. `localhost:8081/api/images/123877631.jpg`).
		+ [ ] Beim Hochladen eines neuen Profilbildes eines Nutzers soll sein ursprüngliches Profilbild auf dem Server durch sein neues ersetzt werden, ohne das der Pfad angepasst wird.
		+ [ ] Speichern Sie in der Datenbank zu den Nutzern jeweils den Link (bspw. `localhost:8081/api/images/123877631.jpg`) zu der hochgeladenen Bilddatei auf dem Server für die Spalte `Image`.

5. Formatierung von Blogartikeln
	- vue-Frontend
		+ [ ] Blogartikel werden standardmäßig bereits als `HTML`gerendert, wenn diese mit Markdown formatiert wurden. Implementieren Sie einen WYSIWYG-Editor (What you see is what you get), sodass Nutzer beim Erstellen oder Bearbeiten von Blogartikeln direkt die Formatierung sehen und einstellen können. 
		+ [ ] Es soll möglich sein Bilder im Text zu integrieren. Der Einfachheit halber soll es möglich sein, Bilder beim Editieren hochzuladen und artikelbezogen die zugehörigen Links zu allen Bildern auf dem Server (bspw. `localhost:8081/api/images/123877631.jpg`) aufzulisten, damit diese dann mit folgender Markdown-Syntax `![alt text](localhost:8081/api/images/123877631.jpg "Title")` im Artikel integriert werden können. Die Bilder müssen somit separat hochgeladen werden, ohne dass vorherige Bilder überschrieben werden. \[Optional\] können Sie noch das Löschen von Bildern implementieren.
	- Kotlin-Frontend
 		+ [ ] Blogartikel sollen im Kotlin-Frontend als `HTML` gerendert werden. Hierzu muss die Markdown-Syntax in HTML umgewandelt werden. Nutzen Sie hierfür eine vorhandene Bibliothek.
		+ [ ] Beim Editieren soll es möglich sein, eine Preview des gerenderten Ergebnisses anzuzeigen. Dabei sollen bei der Preview sowohl der Text des Blogartikels im Markdown als auch das gerenderte Ergebnis angezeigt werden können.
		+ [ ] Es soll möglich sein Bilder im Text zu integrieren. Der Einfachheit halber soll es möglich sein, Bilder beim Editieren hochzuladen und artikelbezogen die zugehörigen Links zu allen Bildern auf dem Server (bspw. `localhost:8081/api/images/123877631.jpg`) aufzulisten, damit diese dann mit folgender Markdown-Syntax `![alt text](localhost:8081/api/images/123877631.jpg "Title")` im Artikel integriert werden können. **Hinweis:** Die Bilder müssen somit separat hochgeladen werden, ohne dass vorherige Bilder überschrieben werden. **Hinweis:** Das Löschen von Bildern muss nicht implementiert werden.
	- .Net-Backend
		+ [ ] Erweitern Sie die Datenbank so, dass zu Artikeln noch eine Menge an Bildern (d.h. Links zu Bildern bspw. `localhost:8081/api/images/123877631.jpg`) gespeichert werden, die im Artikel verwendet werden.
		+ [ ] Der Endpunkt zur GET-Operation `articles` soll zusätzlich noch eine Liste an Bildern zurückgeben.
		+ [ ] Das Backend soll Bilder, die in Blogartikeln anzeigt werden, über einen Endpunkt annehmen. Verwenden Sie hierzu den Endpunkt und die Funktionalität zum Verwalten von Bildern aus der Aufgabe 'Profilbilder'. Beachten Sie, dass für Artikel mehrere Bilder zulässig sind, während bei Profilbildern nur ein Bild zulässig ist und dieses bei Neu-Upload überschrieben werden sollen. Bilden Sie dies im Endpunkt entsprechend ab.

6. Code Styles
    - Um die Qualität von Programmcode sicherzustellen, soll statische Code Analyse genutzt werden.
    - Ein Teil der Code Analyse sind Code Styles, also Richtlinien bezüglich Namensgebung, Formatierung und ähnliche. Diese sind i.d.R. programmiersprachenspezifisch.
    - [ ] Überlegen Sie sich Richtlinien, die jeweils für die drei Projekte gelten sollen.
        + Dokumentieren Sie in einem Issue, wie Sie zu ihren Richtlinien gekommen sind.
        + Begründen Sie dabei Ihre Entscheidung.
    - [ ] Binden Sie in vue `eslint` ein und konfigurieren Sie es entsprechend.
    - [ ] Binden Sie in Kotlin `ktlint` ein und konfigurieren Sie es entsprechend.
    - [ ] Konfigurieren Sie in dotnet den integrierten Style-Checker (`dotnet format`) entsprechend.
    - [ ] Binden sie alle drei Style-Checker in die CI-Pipeline ein. Ergänzen Sie dafür in den CI-Skripten im Repository die entsprechenden Befehle. **Hinweis:** Es geht nur um die Validierung der konfigurierten Styles, nicht um das automatische Formatieren!

1. \[Optional\] Rollenmodell
    - Aktuell sind alle Nutzer in Conduit gleichberechtigt und dürfen nur ihre eigenen Profile und Artikel bearbeiten. Typischerweise gibt es aber auch Administratoren und Moderatoren auf solchen Plattformen.
    - Implementieren Sie ein Rollenmodell im Backend:
        + [ ] Alle bestehenden Nutzer sind standardmäßig `User`.
        + [ ] `User` dürfen
            + Ihre eigenen Profile bearbeiten
            + Ihre eigenen Artikel bearbeiten und löschen
            + Ihre eigenen Kommentare bearbeiten und löschen.
        + [ ] `Moderatoren` dürfen
            + alles was `User` dürfen
            + fremde Kommentare löschen
        + [ ] `Administratoren` dürfen
            + alles was `Moderatoren` dürfen
            + fremde Artikel löschen
            + Profilbilder von Nutzern löschen
            + Die Rolle von *allen* Nutzern ändern
        + [ ] Erstellen oder Ergänzen Sie entsprechende Endpunkte, um die Funktionalitäten umzusetzen
            + Prüfen Sie dabei, ob der authentifizierte Nutzer die richtige Rolle besitzt.
    - Das Rollenmodell soll in mindestens einem Frontend umgesetzt werden
        + [ ] Ergänzen Sie das Frontend, um entsprechende Funktionalitäten.
        + [ ] Schaffen Sie eine Admin-Seite, auf der die Nutzerrollen geändert werden können.
        + [ ] Die entsprechenden Funktionalitäten sollen basierend auf der Rolle ein- und ausgeblendet werden.