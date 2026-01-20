# Bildoptimierung für Claude Opus 4

Optimiert PNG-Bilder auf max 1568px für optimale Token-Effizienz bei Claude.

## Schnellstart

```bash
# Vom Projekt-Root ausführen:
uv run dev_doc/slides/optimize_images.py -d dev_doc/slides

# Dry-Run (Vorschau ohne Änderungen)
uv run dev_doc/slides/optimize_images.py -d dev_doc/slides --dry-run

# Mit Backup
uv run dev_doc/slides/optimize_images.py -d dev_doc/slides --backup
```

## Parameter

| Parameter | Kurz | Default | Beschreibung |
|-----------|------|---------|--------------|
| `--directory` | `-d` | `.` | Verzeichnis mit PNG-Dateien |
| `--max-size` | `-m` | `1568` | Max. Pixelgröße (längste Seite) |
| `--dry-run` | `-n` | - | Nur Vorschau, keine Änderungen |
| `--backup` | `-b` | - | Erstellt `.png.bak` Backups |
| `--workers` | `-w` | CPU-Kerne (max 8) | Parallele Worker |
| `--force` | `-f` | - | Alle Bilder verarbeiten (auch bereits optimierte) |
| `--hide-skipped` | - | - | Übersprungene nicht in Tabelle zeigen |

## Beispiele

```bash
# Mit Backup
uv run optimize_images.py --backup

# 4 Worker, max 1200px
uv run optimize_images.py -w 4 -m 1200

# Force: Alle neu verarbeiten
uv run optimize_images.py --force

# Kompakte Ausgabe
uv run optimize_images.py --hide-skipped
```

## Warum 1568px?

Claude Opus 4 skaliert Bilder intern auf max 1568px. Durch Vorskalierung:
- Weniger Tokens = schnellere Verarbeitung
- Geringere Kosten
- Keine Qualitätsverluste (Claude würde ohnehin skalieren)
