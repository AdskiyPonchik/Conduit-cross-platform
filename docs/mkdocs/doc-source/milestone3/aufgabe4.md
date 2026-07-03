# 4. Lokaler Bild-Upload & Namens-Validierung

Zusammenfassung der Konzeptionierung und Integration eines lokalen Datei-Uploads über Binärdaten-Transfer sowie der clientseitigen Echtzeitvalidierung des Registrierungsformulars über die HTML5 Constraint Validation API.

---

## Feature 1: Lokaler Profilbild-Upload über Form-Data

* **Problembeschreibung:** Die Anwendung erlaubte im Einstellungsformular bisher ausschließlich das Einfügen externer Bild-Links als Textkette. Ein nativer Bildupload für lokale Dateien fehlte. Zudem führte die Nutzung relativer API-Pfade (z. B. `fetch('/api/images')`) zu Fehlern, da Anfragen fälschlicherweise an den Frontend-Port (4173) statt an den separaten Port des .NET-Backends geschickt wurden.
* **Datei:** `src/pages/Settings.vue`

### Durchgeführte Änderungen und Erklärungen:
1. **Visuelle Maskierung des Inputs:** Im HTML-Template wurde ein styled `<label>` integriert, welches als Klick-Schaltfläche dient und einen über CSS ausgeblendeten (`display: none`) HTML5-Datei-Input steuert, der auf `.jpg, .jpeg, .png` beschränkt ist. Ein `v-if="localFilePath"` spiegelt das erfolgreiche Feedback durch die reaktive Dateinamen-Anzeige im DOM wider.
2. **FormData und absolute Adressierung:** In der Methode `onSubmit()` werden die Binärdaten bei Vorhandensein in ein `FormData`-Objekt verpackt. Der Parameter-Schlüssel wird exakt als `'file'` deklariert, um mit der Schnittstelle des Backends zu harmonieren. Der asynchrone Request wird via `fetch` an die absolute, über `CONFIG.API_HOST` aufgelöste Backend-URL geschickt.
3. **Zuweisung und Button-Aktivierung:** Nach erfolgreichem Upload wird die vom Server generierte Bild-URL extrahiert und `form.image` zugewiesen. Die Computed Property `isButtonDisabled` wurde so erweitert, dass der Update-Button sofort aktiv und anklickbar wird, sobald das reaktive Objekt `selectedFile` nicht mehr `null` ist.

---

## Feature 2: Alphanumerische Zeichen-Sperre für Benutzernamen

* **Problembeschreibung:** Wenn Benutzer Sonderzeichen oder Leerzeichen im Namen verwendeten, führte dies zu nachgelagerten System- und Routingfehlern. Das Erzwingen von rein alphanumerischen Werten über das HTML5-Attribut `pattern` löste zwar das Problem, führte aber zur unschönen Browser-Standardmeldung *"Angefordertes Format wählen"*. Das einfache Überschreiben via `setCustomValidity` sperrte das Feld jedoch permanent, selbst wenn der Benutzer den Text danach korrigierte.
* **Datei:** `src/pages/Register.vue`

### Durchgeführte Änderungen und Erklärungen:
1. **Regulärer Ausdruck im DOM:** Dem Texteingabefeld des Benutzernamens wurde das native Attribut `pattern="^[a-zA-Z0-9]+$"` hinzugefügt, wodurch Sonderzeichen direkt auf Formularebene blockiert werden.
2. **Individuelles Fehler-Feedback:** Über die Event-Verknüpfung `@invalid="onInvalidUsername"` wird eine Funktion aufgerufen, die im Moment des Absendeversuchs bei Fehlerschlag mittels `event.target.setCustomValidity()` einen maßgeschneiderten, verständlichen deutschen Hinweistext in die Browser-Sprechblase injiziert.
3. **Lösen der Validierungsblockade:** Über den Event-Handler `@input="onInputUsername"` wird bei *jedem einzelnen Tastenanschlag* im Eingabefeld der Fehlerzustand über `setCustomValidity('')` sofort wieder geleert. Dies erlaubt es dem Browser, die Gültigkeit bei jedem erneuten Klick auf den Registrierungs-Button unvoreingenommen neu zu prüfen und verhindert eine dauerhafte Formular-Sperre.