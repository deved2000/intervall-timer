# Intervall Timer

Ein schlichter Intervall-Timer für Android: Sets, Training, Pause, Vorbereiten und Abkühlen. Kurz vor dem Ende jeder Trainings- und Pausenphase ertönt ein **tiefer Gong**.

- **Schnellstart:** Sets, Training und Pause einstellen, dann START drücken
- **Voreinstellungen:** speichern, bearbeiten, löschen
- **Gong-Countdown (Final Count):** Bei `N` ertönt der Gong bei N, N−1, …, 1 Sekunde(n) vor Ende und noch einmal beim Ende. Beispiel `2`: Gong bei 2 s, 1 s und 0 s
- Farbige Vollbild-Anzeige je Phase (Farben einstellbar)
- Antippen blendet die Steuerung ein (Pause, vor/zurück, Lautstärke). **Gedrückt halten beendet den Timer.**
- Option „Letzte Pause überspringen“
- Alles wird nur lokal auf dem Gerät gespeichert. Kein Konto, keine Cloud, keine Internet-Berechtigung.

Technik: Kotlin, Jetpack Compose, minSdk 26 (Android 8.0).

---

# Schritt-für-Schritt: Veröffentlichen auf GitHub

So entsteht am Ende eine **signierte APK**, die GitHub automatisch baut. Den Signier-Schlüssel erzeugst du **lokal auf deinem Rechner**. Er landet nie im Code, sondern nur verschlüsselt in den GitHub-Secrets.

## Schritt 0 – Was du brauchst

| Was | Wofür | Woher |
|---|---|---|
| GitHub-Konto | Code hosten, App bauen lassen | github.com |
| Git | Code hochladen | git-scm.com (Windows: „Git for Windows“) |
| Java JDK 17 oder neuer | enthält `keytool` für den Schlüssel | adoptium.net (Temurin 17) **oder** Android Studio (bringt Java mit) |

Prüfen, ob alles da ist (Terminal bzw. PowerShell):

```bash
git --version
keytool -help
```

> **Windows & Android Studio:** Wenn `keytool` nicht gefunden wird, liegt es meist hier:
> `"C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe"`. Setze diesen Pfad dann statt `keytool` in den Befehlen ein.

## Schritt 1 – Repository auf GitHub anlegen

1. Auf github.com oben rechts auf **+** → **New repository** klicken.
2. Name: z. B. `intervall-timer`, Sichtbarkeit: **Public**.
3. **Kein** README, keine .gitignore und keine Lizenz anhaken (das Projekt bringt sie schon mit).
4. **Create repository** klicken und die angezeigte URL kopieren, z. B. `https://github.com/DEINNAME/intervall-timer.git`.

## Schritt 2 – Code hochladen

Die ZIP-Datei entpacken, ein Terminal **im entpackten Ordner** öffnen (dort liegen `README.md`, `app/` und `.github/`) und Folgendes eingeben:

```bash
git init
git add .
git commit -m "Erste Version"
git branch -M main
git remote add origin https://github.com/DEINNAME/intervall-timer.git
git push -u origin main
```

Beim ersten `git push` fragt Git nach deiner Anmeldung. Meist öffnet sich ein Browserfenster. Falls nach einem Passwort gefragt wird: GitHub akzeptiert dort ein **Personal Access Token** statt deines Passworts (GitHub → Settings → Developer settings → Personal access tokens).

> Der Ordner `.github` ist auf macOS und Linux versteckt. Er muss mit hochgeladen werden, weil darin die automatische Build-Anleitung liegt. Mit `git add .` passiert das automatisch.

## Schritt 3 – Signier-Schlüssel lokal erzeugen

Android installiert nur signierte Apps. Jedes Update muss mit **demselben Schlüssel** signiert sein.

Wechsle in einen Ordner **außerhalb** des Projekts, z. B. in deinen Benutzerordner, und führe aus:

```bash
keytool -genkeypair -v -keystore intervalltimer-release.jks -keyalg RSA -keysize 4096 -validity 10000 -alias intervalltimer
```

- Du wirst nach einem **Passwort** gefragt. Wähle ein starkes Passwort und notiere es.
- Danach folgen Fragen zu Name, Organisation usw. Die Antworten erscheinen im Zertifikat. Du kannst überall etwas Einfaches eintragen oder einfach Enter drücken. Am Ende mit `ja` (oder `yes`) bestätigen.
- Fragt keytool zusätzlich nach einem Schlüssel-Passwort, nimm **dasselbe Passwort**.

Ergebnis: die Datei `intervalltimer-release.jks`.

> ⚠️ **Sicher aufbewahren!** Speichere die `.jks`-Datei und das Passwort an einem sicheren Ort, z. B. im Passwort-Manager mit Backup. Geht der Schlüssel verloren, kannst du keine Updates mehr ausliefern: Nutzer müssten die App deinstallieren und neu installieren.
> **Niemals** die `.jks`-Datei ins Repository legen. Die `.gitignore` blockiert sie zur Sicherheit.

## Schritt 4 – Schlüssel als GitHub-Secret hinterlegen

GitHub braucht den Schlüssel als Text (Base64). Damit kopierst du ihn direkt in die Zwischenablage:

**macOS:**
```bash
base64 -i intervalltimer-release.jks | pbcopy
```

**Linux:**
```bash
base64 -w0 intervalltimer-release.jks
```
(Ausgabe markieren und kopieren.)

**Windows (PowerShell, im Ordner mit der Datei):**
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("$PWD\intervalltimer-release.jks")) | Set-Clipboard
```

Dann im Browser:

1. Dein Repository öffnen → **Settings** → links **Secrets and variables** → **Actions**.
2. Für jede Zeile der Tabelle auf **New repository secret** klicken:

| Name (genau so schreiben) | Wert |
|---|---|
| `SIGNING_KEYSTORE_BASE64` | der kopierte Base64-Text (Strg/Cmd + V) |
| `SIGNING_STORE_PASSWORD` | dein Passwort aus Schritt 3 |
| `SIGNING_KEY_PASSWORD` | dasselbe Passwort |
| `SIGNING_KEY_ALIAS` | `intervalltimer` |

GitHub speichert Secrets verschlüsselt. Nicht einmal du kannst sie später wieder ansehen, nur ersetzen.

## Schritt 5 – Erste Version veröffentlichen

Im Projektordner im Terminal:

```bash
git tag v1.0.0
git push origin v1.0.0
```

Was dann passiert:

1. Im Repository-Tab **Actions** startet „Build & Release APK“ (gelber Punkt = läuft).
2. Nach etwa 3–6 Minuten erscheint ein grüner Haken.
3. Unter **Releases** (rechte Seitenleiste auf der Repository-Startseite) liegt jetzt `IntervallTimer-1.0.0.apk`.

> Nur testen, ohne Release: **Actions** → „Build & Release APK“ → **Run workflow**. Die APK liegt dann unten auf der Seite des Laufs unter „Artifacts“ (als ZIP).

**Roter Haken?** Den Lauf anklicken und den rot markierten Schritt öffnen. Die häufigsten Ursachen:
- „Fehlende Secrets“ → Ein Secret-Name ist falsch geschrieben (Schritt 4).
- „Keystore was tampered with, or password was incorrect“ → falsches Passwort im Secret.
- Fehler beim Base64-Dekodieren → Den Base64-Text nochmal vollständig kopieren und das Secret ersetzen.

## Schritt 6 – App aufs Handy

1. Auf dem Handy die Releases-Seite öffnen und die `.apk` herunterladen.
2. Antippen. Android fragt beim ersten Mal, ob der Browser „unbekannte Apps installieren“ darf → erlauben.
3. Installieren, fertig.

## Schritt 7 – Updates

Code ändern, dann:

```bash
git add .
git commit -m "Beschreibung der Änderung"
git push
git tag v1.0.1
git push origin v1.0.1
```

Die Versionsnummer steigt bei jedem Build automatisch. So installiert sich das Update einfach über die alte Version, solange es mit demselben Schlüssel signiert ist.

## Optional: Lizenz hinzufügen

Wenn andere deinen Code nutzen dürfen sollen: im Repository **Add file → Create new file**, als Dateiname `LICENSE` eingeben. GitHub bietet dann rechts **Choose a license template** an (z. B. MIT).

---

## Projektstruktur

```
.github/workflows/release.yml   Automatischer Build + Signierung + Release
app/build.gradle.kts            App-Konfiguration (Paketname, Versionen, Signierung)
app/src/main/
  java/app/intervalltimer/
    MainActivity.kt             Einstieg, Bildschirmwechsel
    AppViewModel.kt             Zustand, Speichern, Timer-Steuerung
    TimerEngine.kt              Ablauf-Logik inkl. Gong-Zeitpunkte
    audio/GongPlayer.kt         Gong abspielen
    data/                       Modelle + lokale Speicherung
    ui/                         Bildschirme (Start, Editor, Timer, Einstellungen)
  res/raw/gong.wav              Der tiefe Gong (synthetisch erzeugt, frei nutzbar)
```

## Anpassen

- **App-Name:** `app/src/main/res/values/strings.xml`
- **Paketname** (`app.intervalltimer`): in `app/build.gradle.kts` (`applicationId`). Nur ändern, *bevor* du die App verteilst, denn ein neuer Paketname gilt als neue App.
- **Gong-Klang:** `res/raw/gong.wav` durch eine eigene WAV- oder OGG-Datei mit demselben Namen ersetzen
- **Gong auch in Vorbereiten/Abkühlen:** in `TimerEngine.kt`, Funktion `thresholds()`, die Phasen ergänzen
- **Standardfarben und -werte:** `data/Models.kt`

## Lokal in Android Studio öffnen (optional)

**File → Open** und den Projektordner wählen. Android Studio lädt alles Nötige selbst herunter. Mit ▶ startest du die App auf einem Handy oder Emulator (Debug-Version, ohne deinen Release-Schlüssel).
