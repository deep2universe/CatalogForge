#!/usr/bin/env python3
# /// script
# requires-python = ">=3.10"
# dependencies = ["pillow", "rich"]
# ///
"""
Bildoptimierung für Claude Opus 4

Optimiert PNG-Bilder auf die empfohlene Größe für Claude Opus 4 (max 1568px).
Anthropic skaliert Bilder intern auf diese Größe - durch Vorskalierung sparen wir Tokens.

Ausführung: uv run optimize_images.py [--dry-run] [--backup] [--max-size 1568]
"""

import argparse
import shutil
from pathlib import Path
from dataclasses import dataclass

from PIL import Image
from rich.console import Console
from rich.table import Table
from rich.panel import Panel
from rich.progress import Progress, SpinnerColumn, TextColumn, BarColumn
from rich import box


@dataclass
class ImageStats:
    """Statistiken für ein einzelnes Bild."""
    path: Path
    original_size: int
    new_size: int | None = None
    original_dimensions: tuple[int, int] = (0, 0)
    new_dimensions: tuple[int, int] | None = None
    error: str | None = None

    @property
    def savings(self) -> int:
        if self.new_size is None:
            return 0
        return self.original_size - self.new_size

    @property
    def savings_percent(self) -> float:
        if self.new_size is None or self.original_size == 0:
            return 0.0
        return (self.savings / self.original_size) * 100


def format_size(size_bytes: int) -> str:
    """Formatiert Bytes in lesbare Größe."""
    if size_bytes < 1024:
        return f"{size_bytes} B"
    elif size_bytes < 1024 * 1024:
        return f"{size_bytes / 1024:.1f} KB"
    else:
        return f"{size_bytes / (1024 * 1024):.2f} MB"


def get_png_files(directory: Path) -> list[Path]:
    """Findet alle PNG-Dateien im Verzeichnis."""
    return sorted(directory.glob("*.png"))


def calculate_new_dimensions(width: int, height: int, max_size: int) -> tuple[int, int]:
    """Berechnet neue Dimensionen unter Beibehaltung des Seitenverhältnisses."""
    if width <= max_size and height <= max_size:
        return width, height
    
    if width > height:
        new_width = max_size
        new_height = int(height * (max_size / width))
    else:
        new_height = max_size
        new_width = int(width * (max_size / height))
    
    return new_width, new_height


def optimize_image(
    path: Path,
    max_size: int = 1568,
    dry_run: bool = False,
    backup: bool = False
) -> ImageStats:
    """Optimiert ein einzelnes Bild für Opus 4."""
    stats = ImageStats(
        path=path,
        original_size=path.stat().st_size
    )
    
    try:
        with Image.open(path) as img:
            stats.original_dimensions = img.size
            new_dims = calculate_new_dimensions(img.width, img.height, max_size)
            stats.new_dimensions = new_dims
            
            if dry_run:
                # Schätze neue Größe basierend auf Dimensionsreduktion
                ratio = (new_dims[0] * new_dims[1]) / (img.width * img.height)
                stats.new_size = int(stats.original_size * ratio * 0.9)  # 10% Kompressionsbonus
                return stats
            
            # Backup erstellen falls gewünscht
            if backup:
                backup_path = path.with_suffix(".png.bak")
                shutil.copy2(path, backup_path)
            
            # Bild verarbeiten
            if new_dims != img.size:
                img = img.resize(new_dims, Image.Resampling.LANCZOS)
            
            # Als optimiertes PNG speichern
            img.save(path, "PNG", optimize=True)
            
            stats.new_size = path.stat().st_size
            
    except Exception as e:
        stats.error = str(e)
    
    return stats


def create_results_table(results: list[ImageStats]) -> Table:
    """Erstellt eine formatierte Ergebnistabelle."""
    table = Table(
        title="🖼️  Bildoptimierung für Claude Opus 4",
        box=box.ROUNDED,
        show_header=True,
        header_style="bold cyan"
    )
    
    table.add_column("Datei", style="white", no_wrap=True)
    table.add_column("Vorher", justify="right", style="red")
    table.add_column("Nachher", justify="right", style="green")
    table.add_column("Ersparnis", justify="right", style="yellow")
    table.add_column("%", justify="right", style="magenta")
    table.add_column("Dimensionen", justify="center", style="dim")
    
    for stat in results:
        if stat.error:
            table.add_row(
                stat.path.name,
                format_size(stat.original_size),
                "[red]Fehler[/red]",
                "-",
                "-",
                stat.error[:30]
            )
        else:
            dim_change = ""
            if stat.original_dimensions and stat.new_dimensions:
                if stat.original_dimensions != stat.new_dimensions:
                    dim_change = f"{stat.original_dimensions[0]}×{stat.original_dimensions[1]} → {stat.new_dimensions[0]}×{stat.new_dimensions[1]}"
                else:
                    dim_change = f"{stat.original_dimensions[0]}×{stat.original_dimensions[1]} (unverändert)"
            
            table.add_row(
                stat.path.name,
                format_size(stat.original_size),
                format_size(stat.new_size) if stat.new_size else "-",
                format_size(stat.savings) if stat.savings > 0 else "-",
                f"{stat.savings_percent:.1f}%" if stat.savings_percent > 0 else "-",
                dim_change
            )
    
    return table


def create_summary_panel(results: list[ImageStats], dry_run: bool) -> Panel:
    """Erstellt ein Zusammenfassungs-Panel."""
    successful = [r for r in results if r.error is None]
    failed = [r for r in results if r.error is not None]
    
    total_original = sum(r.original_size for r in successful)
    total_new = sum(r.new_size or 0 for r in successful)
    total_savings = total_original - total_new
    savings_percent = (total_savings / total_original * 100) if total_original > 0 else 0
    
    mode = "[yellow]DRY-RUN (keine Änderungen)[/yellow]" if dry_run else "[green]Optimierung abgeschlossen[/green]"
    
    summary = f"""
{mode}

📊 [bold]Statistik:[/bold]
   Verarbeitete Dateien: {len(successful)}
   Fehlgeschlagen: {len(failed)}
   
💾 [bold]Speicherplatz:[/bold]
   Vorher gesamt: {format_size(total_original)}
   Nachher gesamt: {format_size(total_new)}
   [bold green]Ersparnis: {format_size(total_savings)} ({savings_percent:.1f}%)[/bold green]

🎯 [bold]Opus 4 Optimierung:[/bold]
   Max. Bildgröße: 1568px (empfohlen für Claude)
   Weniger Tokens = schnellere Verarbeitung
"""
    
    return Panel(summary.strip(), title="📈 Zusammenfassung", border_style="blue")


def main():
    parser = argparse.ArgumentParser(
        description="Optimiert PNG-Bilder für Claude Opus 4"
    )
    parser.add_argument(
        "--directory", "-d",
        type=Path,
        default=Path("."),
        help="Verzeichnis mit PNG-Dateien (default: aktuelles Verzeichnis)"
    )
    parser.add_argument(
        "--max-size", "-m",
        type=int,
        default=1568,
        help="Maximale Pixelgröße auf längster Seite (default: 1568 für Opus 4)"
    )
    parser.add_argument(
        "--dry-run", "-n",
        action="store_true",
        help="Zeigt nur Vorschau ohne Änderungen"
    )
    parser.add_argument(
        "--backup", "-b",
        action="store_true",
        help="Erstellt Backup der Originaldateien (.png.bak)"
    )
    
    args = parser.parse_args()
    console = Console()
    
    # Verzeichnis prüfen
    if not args.directory.exists():
        console.print(f"[red]Fehler: Verzeichnis '{args.directory}' existiert nicht[/red]")
        return 1
    
    # PNG-Dateien finden
    png_files = get_png_files(args.directory)
    
    if not png_files:
        console.print(f"[yellow]Keine PNG-Dateien in '{args.directory}' gefunden[/yellow]")
        return 0
    
    console.print(f"\n[bold]Gefunden: {len(png_files)} PNG-Dateien[/bold]\n")
    
    if args.dry_run:
        console.print("[yellow]🔍 DRY-RUN Modus - keine Dateien werden verändert[/yellow]\n")
    
    # Bilder verarbeiten mit Fortschrittsanzeige
    results: list[ImageStats] = []
    
    with Progress(
        SpinnerColumn(),
        TextColumn("[progress.description]{task.description}"),
        BarColumn(),
        TextColumn("[progress.percentage]{task.percentage:>3.0f}%"),
        console=console
    ) as progress:
        task = progress.add_task("Optimiere Bilder...", total=len(png_files))
        
        for png_file in png_files:
            progress.update(task, description=f"Verarbeite {png_file.name}...")
            stats = optimize_image(
                png_file,
                max_size=args.max_size,
                dry_run=args.dry_run,
                backup=args.backup
            )
            results.append(stats)
            progress.advance(task)
    
    # Ergebnisse anzeigen
    console.print()
    console.print(create_results_table(results))
    console.print()
    console.print(create_summary_panel(results, args.dry_run))
    
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
