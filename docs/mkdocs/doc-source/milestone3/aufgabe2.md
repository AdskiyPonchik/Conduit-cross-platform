# 2. Passwort-Bug-Fix & Playwright E2E-Testing

Zusammenfassung der Lokalisierung und Behebung des Passwort-Überschreibungsfehlers im Web-Frontend sowie der Implementierung eines robusten Ende-zu-Ende-Tests zur Absicherung der Einstellungs-Workflows.

---

## Feature 1: Bereinigung leerer Formularwerte in den Einstellungen

* **Problembeschreibung:** Wenn ein Benutzer seine Profileinstellungen aktualisierte und das Eingabefeld für das neue Passwort leer ließ, übermittelte das reaktive Formular-Objekt standardmäßig einen leeren String (`"password": ""`) im JSON-Body des HTTP-`PUT`-Requests. Das Backend interpretierte diese explizite Übergabe als Aktualisierungswunsch und überschrieb das bestehende Passwort in der Datenbank, wodurch der Benutzeraccount funktional unbrauchbar wurde.
* **Datei:** `src/pages/Settings.vue`

### Durchgeführte Änderungen und Erklärungen:
1. **Payload-Filterung via Reduce:** Innerhalb der asynchronen Methode `onSubmit()` wurde eine Transformation vorgeschaltet, die das reaktive Objekt mittels `Object.entries(form).reduce(...)` bereinigt.
2. **Gezielter Ausschluss leerer Passwörter:** Die Filter-Logik fängt den Schlüssel `'password'` ab. Ist dessen zugewiesener Wert ein leerer String (`''`), wird dieser Eintrag übersprungen und nicht in das neue, bereinigte Transfer-Objekt übernommen.
3. **Erhalt valider Daten:** Alle anderen ausgefüllten Felder (Username, Bio, Email, Image) werden über `Object.assign` unverändert weitergegeben, sodass das Passwort-Attribut im ausgehenden Netzwerk-Payload komplett abwesend ist und die serverseitige Datenbank das bestehende Passwort unberührt lässt.

---

## Feature 2: Playwright E2E-Test mit LocalStorage-Injektion

* **Problembeschreibung:** Beim automatisierten Testen geschützter Routen trat eine asynchrone Wettlaufbedingung (*Race Condition*) auf. Wenn der Test-Runner die Seite via `page.goto()` aufrief und den Authentifizierungs-Token erst danach in den `localStorage` schreiben wollte, war das Frontend bereits vollständig initialisiert. Der Vue-Lifecycle (`onMounted`) erkannte den leeren Speicher, stufte den Test-Runner als unautorisiert ein und leitete ihn sofort zur Login-Seite um, was den Test fehlschlagen ließ.
* **Datei:** `e2e/settings.spec.ts`

### Durchgeführte Änderungen und Erklärungen:
1. **Verwendung von addInitScript:** Im `test.beforeEach`-Block wurde der Ablauf auf Playwright-Ebene deterministisch synchronisiert. Mittels `page.addInitScript()` wird das Benutzer-Mock-Objekt inklusive JWT-Token garantiert injiziert, *bevor* die Vue-Anwendung oder deren Lifecycle-Hooks im Browser-Kontext starten.
2. **Netzwerk-Interzeption (HTTP-Routing):** Über `page.route()` wird der ausgehende `PUT`-Request auf die API-Route `**/api/user` abgefangen. Dem Frontend wird ein erfolgreicher Server-Response (HTTP 200 OK) simuliert, während im Hintergrund der post-Formular-Payload isoliert ausgelesen wird.
3. **Regressionsschutz über Assertions:** Der Test verifiziert im abgefangenen JSON-Payload, dass die vorgenommene Namensänderung übertragen wurde, das Attribut `password` jedoch vollständig nicht existent (`undefined`) bleibt.