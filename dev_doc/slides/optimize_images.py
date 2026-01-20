#!/usr/bin/env python3
# /// script
# requires-python = ">=3.10"
# dependencies = ["pillow", "rich"]
# ///
"""
Bildoptimierung für Claude Opus 4

Optimiert PNG-Bilder auf die empfohlene Größe für Claude Opus 4 (max 1568px).
Anthropic skaliert Bilder intern auf diese Größe - durch Vorskalierung sparen wir Tokens.

Ausführung: uv run optimize_images.py [--dry-run] [--backup] [--workers 8] [--force]
"""

import argparse
import os
import shutil
import time
from concurrent.futures import ProcessPoolExecutor, as_completed
from dataclasses import dataclass
from pathlib import Path

from PIL import Image
from rich import box
from rich.console import Console
from rich.panel import Panel
from rich.progress import BarColumn, Progress, SpinnerColumn, TextColumn, TimeElapsedColumn
from rich.table import Table


@dataclass
class ImageStats:
    """Statistiken für ein einzelnes Bild."""
    path: Path
    original_size: int
    new_size: int | None = None
    original_dimensions: tuple[int, int] = (0, 0)
    new_dimensions: tuple[int, int] | None = None
    error: str | None = None
    skipped: bool = False

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


def check_image_needs_optimization(path: Path, max_size: int) -> tuple[bool, int, int, int]:
    """
    Prüft schnell ob ein Bild Optimierung braucht (nur Header-Read).
    
    Returns:
        (needs_optimization, width, height, file_size)
    """
    try:
        file_size = path.stat().st_size
        with Image.open(path) as img:
            width, height = img.size
            needs_opt = width > max_size or height > max_size
            return needs_opt, width, height, file_size
    except Exception:
        # Bei Fehler: Zur Verarbeitung markieren (Fehler wird dort behandelt)
        return True, 0, 0, 0


def filter_images(
    png_files: list[Path], 
    max_size: int, 
    console: Console
) -> tuple[list[tuple[Path, int, int, int]], list[ImageStats]]:
    """
    Filtert Bilder schnell in zwei Kategorien.
    
    Returns:
        (needs_work_list, already_optimal_stats)
        - needs_work_list: [(path, width, height, file_size), ...]
        - already_optimal_stats: [ImageStats, ...] für übersprungene Bilder
    """
    needs_work: list[tuple[Path, int, int, int]] = []
    already_optimal: list[ImageStats] = []
    
    with Progress(
        SpinnerColumn(),
        TextColumn("[progress.description]{task.description}"),
        BarColumn(),
        TextColumn("[progress.percentage]{task.percentage:>3.0f}%"),
        console=console,
        transient=True
    ) as progress:
        task = progress.add_task("Scanne Bilder...", total=len(png_files))
        
        for path in png_files:
            needs_opt, width, height, file_size = check_image_needs_optimization(path, max_size)
            
            if needs_opt:
                needs_work.append((path, width, height, file_size))
            else:
                # Bereits optimal - als übersprungen markieren
                already_optimal.append(ImageStats(
                    path=path,
                    original_size=file_size,
                    new_size=file_size,
                    original_dimensions=(width, height),
                    new_dimensions=(width, height),
                    skipped=True
                ))
            
            progress.advance(task)
    
    return needs_work, already_optimal


def optimize_image(args: tuple) -> ImageStats:
    """
    Optimiert ein einzelnes Bild für Opus 4.
    
    Args als Tuple für Multiprocessing-Kompatibilität:
    (path, max_size, dry_run, backup, original_width, original_height, original_size)
    """
    path, max_size, dry_run, backup, orig_width, orig_height, orig_size = args
    
    stats = ImageStats(
        path=path,
        original_size=orig_size,
        original_dimensions=(orig_width, orig_height)
    )
    
    try:
        new_dims = calculate_new_dimensions(orig_width, orig_height, max_size)
        stats.new_dimensions = new_dims
        
        if dry_run:
            # Schätze neue Größe basierend auf Dimensionsreduktion
            ratio = (new_dims[0] * new_dims[1]) / (orig_width * orig_height)
            stats.new_size = int(orig_size * ratio * 0.9)
            return stats
        
        with Image.open(path) as img:
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


def process_images_parallel(
    images_to_process: list[tuple[Path, int, int, int]],
    max_size: int,
    dry_run: bool,
    backup: bool,
    workers: int,
    console: Console
) -> tuple[list[ImageStats], float]:
    """
    Verarbeitet Bilder parallel mit ProcessPoolExecutor.
    
    Returns:
        Tuple aus (Ergebnisliste, Dauer in Sekunden)
    """
    if not images_to_process:
        return [], 0.0
    
    # Args mit vorberechneten Dimensionen
    args_list = [
        (path, max_size, dry_run, backup, width, height, file_size)
        for path, width, height, file_size in images_to_process
    ]
    results: list[ImageStats] = []
    
    start_time = time.perf_counter()
    
    with Progress(
        SpinnerColumn(),
        TextColumn("[progress.description]{task.description}"),
        BarColumn(),
        TextColumn("[progress.percentage]{task.percentage:>3.0f}%"),
        TextColumn("•"),
        TimeElapsedColumn(),
        console=console
    ) as progress:
        task = progress.add_task(
            f"Optimiere mit {workers} Worker(n)...", 
            total=len(images_to_process)
        )
        
        with ProcessPoolExecutor(max_workers=workers) as executor:
            futures = {
                executor.submit(optimize_image, args): args[0] 
                for args in args_list
            }
            
            for future in as_completed(futures):
                try:
                    result = future.result()
                    results.append(result)
                except Exception as e:
                    path = futures[future]
                    results.append(ImageStats(
                        path=path,
                        original_size=path.stat().st_size if path.exists() else 0,
                        error=str(e)
                    ))
                
                progress.advance(task)
    
    duration = time.perf_counter() - start_time
    
    return results, duration


def create_results_table(results: list[ImageStats], show_skipped: bool = True) -> Table:
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
    table.add_column("Status", justify="center", style="dim")
    
    # Sortiere nach Dateiname
    sorted_results = sorted(results, key=lambda x: x.path.name)
    
    for stat in sorted_results:
        if stat.error:
            table.add_row(
                stat.path.name,
                format_size(stat.original_size),
                "[red]Fehler[/red]",
                "-",
                "-",
                stat.error[:20]
            )
        elif stat.skipped:
            if show_skipped:
                table.add_row(
                    stat.path.name,
                    format_size(stat.original_size),
                    format_size(stat.original_size),
                    "-",
                    "-",
                    f"[cyan]⏭ übersprungen[/cyan] ({stat.original_dimensions[0]}×{stat.original_dimensions[1]})"
                )
        else:
            dim_info = ""
            if stat.original_dimensions and stat.new_dimensions:
                if stat.original_dimensions != stat.new_dimensions:
                    dim_info = f"{stat.original_dimensions[0]}×{stat.original_dimensions[1]} → {stat.new_dimensions[0]}×{stat.new_dimensions[1]}"
                else:
                    dim_info = f"{stat.original_dimensions[0]}×{stat.original_dimensions[1]}"
            
            table.add_row(
                stat.path.name,
                format_size(stat.original_size),
                format_size(stat.new_size) if stat.new_size else "-",
                format_size(stat.savings) if stat.savings > 0 else "-",
                f"{stat.savings_percent:.1f}%" if stat.savings_percent > 0 else "-",
                f"[green]✓[/green] {dim_info}"
            )
    
    return table


def create_summary_panel(
    results: list[ImageStats], 
    dry_run: bool, 
    duration: float,
    workers: int,
    scan_duration: float
) -> Panel:
    """Erstellt ein Zusammenfassungs-Panel."""
    processed = [r for r in results if not r.skipped and r.error is None]
    skipped = [r for r in results if r.skipped]
    failed = [r for r in results if r.error is not None]
    
    total_original = sum(r.original_size for r in processed)
    total_new = sum(r.new_size or 0 for r in processed)
    total_savings = total_original - total_new
    savings_percent = (total_savings / total_original * 100) if total_original > 0 else 0
    
    skipped_size = sum(r.original_size for r in skipped)
    
    mode = "[yellow]DRY-RUN (keine Änderungen)[/yellow]" if dry_run else "[green]Optimierung abgeschlossen[/green]"
    
    images_per_sec = len(processed) / duration if duration > 0 else 0
    
    summary = f"""
{mode}

📊 [bold]Statistik:[/bold]
   Optimiert: {len(processed)}
   Übersprungen: {len(skipped)} (bereits ≤1568px, {format_size(skipped_size)})
   Fehlgeschlagen: {len(failed)}
   
💾 [bold]Speicherplatz (optimierte Bilder):[/bold]
   Vorher: {format_size(total_original)}
   Nachher: {format_size(total_new)}
   [bold green]Ersparnis: {format_size(total_savings)} ({savings_percent:.1f}%)[/bold green]

⚡ [bold]Performance:[/bold]
   Scan-Phase: {scan_duration:.2f}s
   Optimierung: {duration:.2f}s ({images_per_sec:.1f} Bilder/s)
   Worker: {workers}

🎯 [bold]Opus 4 Optimierung:[/bold]
   Max. Bildgröße: 1568px (empfohlen für Claude)
"""
    
    return Panel(summary.strip(), title="📈 Zusammenfassung", border_style="blue")


def main():
    parser = argparse.ArgumentParser(
        description="Optimiert PNG-Bilder für Claude Opus 4 (parallel)"
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
    parser.add_argument(
        "--workers", "-w",
        type=int,
        default=min(os.cpu_count() or 4, 8),
        help=f"Anzahl paralleler Worker (default: {min(os.cpu_count() or 4, 8)})"
    )
    parser.add_argument(
        "--force", "-f",
        action="store_true",
        help="Verarbeite alle Bilder, auch bereits optimierte"
    )
    parser.add_argument(
        "--hide-skipped",
        action="store_true",
        help="Zeige übersprungene Bilder nicht in der Tabelle"
    )
    
    args = parser.parse_args()
    console = Console()
    
    if not args.directory.exists():
        console.print(f"[red]Fehler: Verzeichnis '{args.directory}' existiert nicht[/red]")
        return 1
    
    png_files = get_png_files(args.directory)
    
    if not png_files:
        console.print(f"[yellow]Keine PNG-Dateien in '{args.directory}' gefunden[/yellow]")
        return 0
    
    console.print(f"\n[bold]Gefunden: {len(png_files)} PNG-Dateien[/bold]\n")
    
    if args.dry_run:
        console.print("[yellow]🔍 DRY-RUN Modus - keine Dateien werden verändert[/yellow]\n")
    
    # Phase 1: Schneller Scan
    scan_start = time.perf_counter()
    
    if args.force:
        # Force-Modus: Alle Bilder verarbeiten
        needs_work = []
        for path in png_files:
            _, width, height, file_size = check_image_needs_optimization(path, args.max_size)
            needs_work.append((path, width, height, file_size))
        already_optimal: list[ImageStats] = []
        console.print(f"[dim]Force-Modus: Alle {len(needs_work)} Bilder werden verarbeitet[/dim]\n")
    else:
        needs_work, already_optimal = filter_images(png_files, args.max_size, console)
        console.print(f"[dim]Scan abgeschlossen: {len(needs_work)} zu optimieren, {len(already_optimal)} bereits optimal[/dim]\n")
    
    scan_duration = time.perf_counter() - scan_start
    
    # Phase 2: Parallele Optimierung (nur für Bilder die es brauchen)
    if needs_work:
        console.print(f"[bold]Optimiere {len(needs_work)} Bilder mit {args.workers} Worker(n)...[/bold]\n")
        processed_results, process_duration = process_images_parallel(
            needs_work,
            max_size=args.max_size,
            dry_run=args.dry_run,
            backup=args.backup,
            workers=args.workers,
            console=console
        )
    else:
        processed_results = []
        process_duration = 0.0
        console.print("[green]✓ Alle Bilder sind bereits optimal - nichts zu tun![/green]\n")
    
    # Kombiniere Ergebnisse
    all_results = processed_results + already_optimal
    
    # Ergebnisse anzeigen
    console.print()
    console.print(create_results_table(all_results, show_skipped=not args.hide_skipped))
    console.print()
    console.print(create_summary_panel(
        all_results, 
        args.dry_run, 
        process_duration, 
        args.workers,
        scan_duration
    ))
    
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
