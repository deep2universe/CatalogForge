# Daimler Truck Katalog - JSON Schema Dokumentation

## Übersicht

Dieses Dokument beschreibt alle Key-Elemente im Daimler Truck Produktkatalog JSON.

---

## Hauptstruktur

```
{
  "katalog": { ... },      // Hauptcontainer mit Fahrzeugdaten
  "metadaten": { ... }     // Metadaten und Zusatzinformationen
}
```

---

## 1. Katalog-Objekt (`katalog`)

| Key | Typ | Pflicht | Beschreibung | Beispiel |
|-----|-----|---------|--------------|----------|
| `titel` | string | ✓ | Titel des Katalogs | "Daimler Truck Produktkatalog 2025" |
| `unternehmen` | string | ✓ | Unternehmensname | "Daimler Truck AG" |
| `version` | string | ✓ | Versionsnummer | "1.0" |
| `erstellungsdatum` | string | ○ | Datum (YYYY-MM-DD) | "2025-01-15" |
| `sprache` | string | ○ | BCP 47 Sprachcode | "de-DE" |
| `trucks` | array | ✓ | Array aller Fahrzeuge | [...] |

---

## 2. Metadaten-Objekt (`metadaten`)

| Key | Typ | Beschreibung | Beispiel |
|-----|-----|--------------|----------|
| `anzahl_fahrzeuge` | integer | Gesamtanzahl Fahrzeuge | 30 |
| `kategorien` | array[string] | Liste aller Kategorien | ["Fernverkehr", "Baustelle"] |
| `baureihen` | array[string] | Liste aller Baureihen | ["Actros L", "Arocs"] |
| `hinweis` | string | Allgemeiner Hinweis | "Alle Angaben sind..." |
| `bildhinweis` | string | Hinweis zu Bildern | "Die Bild-URLs sind..." |
| `copyright` | string | Copyright-Vermerk | "Daimler Truck AG" |

---

## 3. Truck-Objekt (`katalog.trucks[]`)

### 3.1 Grunddaten

| Key | Typ | Pflicht | Beschreibung | Beispiel |
|-----|-----|---------|--------------|----------|
| `id` | integer | ✓ | Eindeutige ID | 1 |
| `modell` | string | ✓ | Vollständige Modellbezeichnung | "Mercedes-Benz Actros L 1853 LS ProCabin" |
| `baureihe` | string | ✓ | Baureihe | "Actros L" |
| `kategorie` | string | ✓ | Einsatzkategorie | "Fernverkehr" |
| `bild_url` | string (URI) | ✓ | URL zum Produktbild | "https://..." |
| `kurzbeschreibung` | string | ✓ | Kurze Beschreibung (1-2 Sätze) | "Das Flaggschiff..." |
| `preis_ab_eur` | integer | ✓ | Einstiegspreis in EUR | 189500 |

### 3.2 Textinhalte

| Key | Typ | Pflicht | Beschreibung | Länge |
|-----|-----|---------|--------------|-------|
| `marketing_text_lang` | string | ✓ | Langer Marketingtext | 300+ Wörter |
| `marketing_text_mittel` | string | ✓ | Mittlerer Marketingtext | ~150 Wörter |
| `ausstattung_highlights` | array[string] | ✓ | Ausstattungsmerkmale | ~8 Einträge |

### 3.3 Erlaubte Werte für `baureihe`

```
"Actros L" | "Actros F" | "eActros" | "Arocs" | "eArocs" | 
"Atego" | "Econic" | "eEconic" | "Unimog" | "Zetros" | 
"Actros SLT" | "Arocs SLT"
```

---

## 4. Technische Daten (`katalog.trucks[].technische_daten`)

> **Hinweis:** Nicht alle Felder sind für jeden Fahrzeugtyp relevant. 
> Diesel-Fahrzeuge haben andere Felder als Elektro-Fahrzeuge.

### 4.1 Motor & Antrieb (Diesel/Gas)

| Key | Typ | Einheit | Beschreibung | Beispiel |
|-----|-----|---------|--------------|----------|
| `motor` | string | - | Motorbezeichnung mit Norm | "OM 471 Euro VI-E" |
| `hubraum_l` | float | Liter | Hubraum | 12.8 |
| `leistung_ps` | integer | PS | Motorleistung | 530 |
| `leistung_kw` | integer | kW | Motorleistung | 390 |
| `drehmoment_nm` | integer | Nm | Max. Drehmoment | 2600 |
| `abgasnorm` | string | - | Abgasnorm | "Euro VI-E" |

### 4.2 Elektro-Antrieb

| Key | Typ | Einheit | Beschreibung | Beispiel |
|-----|-----|---------|--------------|----------|
| `antrieb` | string | - | Art des E-Antriebs | "Elektrisch (2 Elektromotoren)" |
| `dauerleistung_kw` | integer | kW | Dauerleistung | 400 |
| `spitzenleistung_kw` | integer | kW | Spitzenleistung | 600 |
| `spitzenleistung_ps` | integer | PS | Spitzenleistung | 816 |
| `batteriekapazitaet_kwh` | integer | kWh | Batteriekapazität | 621 |
| `batterietyp` | string | - | Batterietechnologie | "LFP" |
| `reichweite_km` | integer | km | Elektrische Reichweite | 500 |
| `ladeleistung_max_kw` | integer | kW | Max. Ladeleistung | 400 |
| `bordspannung_v` | integer | V | Bordspannung | 800 |

### 4.3 Alternative Kraftstoffe (CNG/LNG)

| Key | Typ | Einheit | Beschreibung | Beispiel |
|-----|-----|---------|--------------|----------|
| `kraftstoff` | string | - | Kraftstoffart | "LNG (Liquefied Natural Gas)" |
| `tankvolumen_kg` | integer | kg | Tankinhalt (Gas) | 80 |

### 4.4 Getriebe & Fahrwerk

| Key | Typ | Einheit | Beschreibung | Beispiel |
|-----|-----|---------|--------------|----------|
| `getriebe` | string | - | Getriebetyp | "Mercedes PowerShift 3 (12-Gang)" |
| `antriebsformel` | string | - | Achskonfiguration | "4x2", "6x4", "8x8 permanent" |

### 4.5 Masse & Gewichte

| Key | Typ | Einheit | Beschreibung | Beispiel |
|-----|-----|---------|--------------|----------|
| `gesamtgewicht_t` | integer | t | Zul. Gesamtgewicht | 40 |
| `zuggesamtgewicht_t` | integer | t | Zul. Zuggesamtgewicht | 250 |
| `nutzlast_t` | integer | t | Nutzlast | 22 |
| `hinterachse_t` | integer | t | Hinterachslast | 10 |
| `leergewicht_reduzierung_kg` | integer | kg | Gewichtsersparnis (Leichtbau) | 350 |

### 4.6 Abmessungen

| Key | Typ | Einheit | Beschreibung | Beispiel |
|-----|-----|---------|--------------|----------|
| `radstand_mm` | integer | mm | Radstand | 3700 |
| `bodenfreiheit_mm` | integer | mm | Bodenfreiheit | 400 |
| `wattiefe_mm` | integer | mm | Wattiefe | 800 |
| `einstiegshoehe_mm` | integer | mm | Einstiegshöhe | 380 |
| `sattelhoehe_mm` | integer | mm | Sattelhöhe | 950 |
| `lichte_hoehe_m` | float | m | Lichte Ladehöhe | 3.0 |
| `ladeflaeche_m` | float | m | Ladeflächenlänge | 7.2 |
| `kipper_volumen_m3` | integer | m³ | Kippervolumen | 5 |

### 4.7 Fahrerhaus

| Key | Typ | Beschreibung | Erlaubte Werte |
|-----|-----|--------------|----------------|
| `fahrerhaustyp` | string | Typ des Fahrerhauses | siehe unten |

**Erlaubte Werte für `fahrerhaustyp`:**
```
"GigaSpace ProCabin" | "GigaSpace" | "GigaSpace Edition" | 
"BigSpace" | "BigSpace Loader" | "StreamSpace" | "StreamSpace Volumer" |
"ClassicSpace" | "CompactSpace" | "M-Fahrerhaus" | 
"S-Fahrerhaus" | "S-Fahrerhaus kurz"
```

### 4.8 Verbrauch & Tank

| Key | Typ | Einheit | Beschreibung | Beispiel |
|-----|-----|---------|--------------|----------|
| `tankvolumen_l` | integer | l | Tankvolumen | 900 |
| `verbrauch_l_100km` | float | l/100km | Durchschnittsverbrauch | 24.5 |
| `hoechstgeschwindigkeit_kmh` | integer | km/h | Höchstgeschwindigkeit | 89 |

### 4.9 Gelände-Eigenschaften

| Key | Typ | Einheit | Beschreibung | Beispiel |
|-----|-----|---------|--------------|----------|
| `steigfaehigkeit_prozent` | integer | % | Max. Steigfähigkeit | 100 |
| `kriechgang` | string | - | Min. Kriechgeschwindigkeit | "0.04 km/h" |

### 4.10 Sonstiges

| Key | Typ | Beschreibung | Beispiel |
|-----|-----|--------------|----------|
| `nebenantrieb` | boolean | Nebenantrieb vorhanden | true |
| `bereifung` | string | Bereifungsart | "Singlebereifung hinten" |

---

## 5. Fahrzeugtyp-spezifische Felder

### Diesel-Fahrzeuge
Nutzen primär: `motor`, `hubraum_l`, `leistung_ps`, `leistung_kw`, `drehmoment_nm`, `abgasnorm`, `tankvolumen_l`, `verbrauch_l_100km`

### Elektro-Fahrzeuge  
Nutzen primär: `antrieb`, `dauerleistung_kw`, `spitzenleistung_kw`, `batteriekapazitaet_kwh`, `batterietyp`, `reichweite_km`, `ladeleistung_max_kw`, `bordspannung_v`

### Gas-Fahrzeuge (CNG/LNG)
Nutzen zusätzlich: `kraftstoff`, `tankvolumen_kg`, `reichweite_km`

### Geländefahrzeuge
Nutzen zusätzlich: `bodenfreiheit_mm`, `wattiefe_mm`, `steigfaehigkeit_prozent`, `kriechgang`

### Low-Entry (Econic)
Nutzen zusätzlich: `einstiegshoehe_mm`

### Sattelzugmaschinen
Nutzen zusätzlich: `sattelhoehe_mm`, `zuggesamtgewicht_t`

---

## 6. Beispiel-Struktur

```json
{
  "katalog": {
    "titel": "Daimler Truck Produktkatalog 2025",
    "unternehmen": "Daimler Truck AG",
    "version": "1.0",
    "erstellungsdatum": "2025-01-15",
    "sprache": "de-DE",
    "trucks": [
      {
        "id": 1,
        "modell": "Mercedes-Benz Actros L 1853 LS ProCabin",
        "baureihe": "Actros L",
        "kategorie": "Fernverkehr",
        "bild_url": "https://images.unsplash.com/photo-xxx",
        "kurzbeschreibung": "Das Flaggschiff im Fernverkehr...",
        "technische_daten": {
          "motor": "OM 471 Euro VI-E",
          "hubraum_l": 12.8,
          "leistung_ps": 530,
          "leistung_kw": 390,
          "drehmoment_nm": 2600,
          "antriebsformel": "4x2",
          "getriebe": "Mercedes PowerShift 3 (12-Gang)",
          "gesamtgewicht_t": 40,
          "fahrerhaustyp": "GigaSpace ProCabin",
          "tankvolumen_l": 900,
          "verbrauch_l_100km": 24.5
        },
        "ausstattung_highlights": [
          "Multimedia Cockpit Interactive 2",
          "Active Brake Assist 6",
          "Predictive Powertrain Control"
        ],
        "marketing_text_lang": "Der Mercedes-Benz Actros L 1853...",
        "marketing_text_mittel": "Der Actros L 1853 ist...",
        "preis_ab_eur": 189500
      }
    ]
  },
  "metadaten": {
    "anzahl_fahrzeuge": 30,
    "kategorien": ["Fernverkehr", "Baustellenverkehr"],
    "baureihen": ["Actros L", "Arocs"],
    "copyright": "Daimler Truck AG"
  }
}
```

---

## 7. Validierung

Das JSON-Schema kann zur Validierung verwendet werden:

```bash
# Mit Python jsonschema
pip install jsonschema
python -c "
import json
from jsonschema import validate

with open('daimler_truck_katalog_schema.json') as f:
    schema = json.load(f)
with open('daimler_truck_katalog_final.json') as f:
    data = json.load(f)

validate(instance=data, schema=schema)
print('✓ JSON ist valide!')
"
```

---

## Legende

| Symbol | Bedeutung |
|--------|-----------|
| ✓ | Pflichtfeld |
| ○ | Optionales Feld |
| string | Textfeld |
| integer | Ganzzahl |
| float | Dezimalzahl |
| boolean | Wahrheitswert (true/false) |
| array | Liste/Array |

---

*Dokumentation erstellt am: 2025-01-15*  
*Schema Version: 1.0*
