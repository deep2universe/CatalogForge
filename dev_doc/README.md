# Dev Documentation

Entwickler-Tools und Dokumentation für CatalogForge.

## API Test Script

Das `test-api.sh` Script testet alle REST-Endpunkte des Backends.

### Voraussetzungen

- `curl` (vorinstalliert auf macOS/Linux)
- `jq` (optional, für schöne JSON-Formatierung)
- Laufender Backend-Server

### Verwendung

```bash
# Server starten (in anderem Terminal)
cd catForge-backend
./gradlew bootRun

# Tests ausführen
./test-api.sh
```

### Optionen

```bash
# Anderer Server/Port
BASE_URL=http://localhost:9090 ./test-api.sh
```

### Getestete Endpunkte

| Bereich | Endpunkte |
|---------|-----------|
| Products | Liste, Pagination, Filter, Suche, Kategorien, Serien |
| Skills | Liste, nach Kategorie, Beispiel-Prompts |
| Layouts | Get (404 für nicht-existente) |
| Images | Get (404 für nicht-existente) |
| PDF | Download (404 für nicht-existente) |
| Errors | 404, 405 Handling |

### Hinweise

- Layout-Generierung (`POST /layouts/generate/*`) benötigt `GEMINI_API_KEY`
- Das Script testet nur Read-Operationen und Error-Handling
- Exit-Code 0 = alle Tests bestanden, 1 = Fehler

### Beispiel-Output

```
═══════════════════════════════════════════════════════════════
  Products API
═══════════════════════════════════════════════════════════════

▶ List all products
  GET /products
  ✓ Status: 200
  Response (truncated):
    [
      {
        "id": 1,
        "name": "Actros L",
        ...
      }
    ]
```
