# Project Structure

```
catForge-backend/
├── src/main/java/com/catalogforge/
│   ├── agent/              # Agent framework (pipelines, steps, strategies)
│   │   ├── steps/          # Pipeline steps (ImageAnalysis, LayoutGeneration, etc.)
│   │   └── strategies/     # Pipeline strategies (Simple, Complex, MultiVariant)
│   ├── config/             # Spring configuration classes
│   │   └── properties/     # @ConfigurationProperties classes
│   ├── controller/         # REST controllers (Products, Layouts, Skills, PDF, Images)
│   ├── exception/          # Custom exceptions + GlobalExceptionHandler
│   ├── gemini/             # Gemini API client, request/response, vision analyzer
│   ├── logging/            # LLM interaction logging (JSONL format)
│   ├── model/              # Domain models (Product, Layout, Skill, etc.)
│   │   ├── request/        # Request DTOs
│   │   └── response/       # Response DTOs
│   ├── pdf/                # PDF generation (PuppeteerBridge, PrintPreset)
│   ├── service/            # Business logic services
│   ├── skill/              # Skills system (loader, assembler)
│   └── util/               # Utilities (CSS validation, HTML sanitization, etc.)
│
├── src/main/resources/
│   ├── css/                # CSS templates for layouts
│   │   ├── components/     # Reusable component styles
│   │   └── print/          # Print-specific CSS (bleed, crop marks)
│   ├── data/               # Product data (products.json)
│   ├── prompts/            # Example prompts
│   └── skills/             # Skill markdown files
│       ├── core/           # Layout principles, typography, color, grid, spacing
│       ├── formats/        # Page formats (A4, A5, DL, A6, Square)
│       └── styles/         # Visual styles (Modern, Technical, Premium, Eco, Dynamic)
│
├── src/test/
│   ├── java/               # Test classes (mirrors main structure)
│   └── resources/
│       └── fixtures/       # Test fixtures (Gemini response mocks)
│
├── scripts/                # Node.js scripts for PDF generation
│   └── pdf-generator.js    # Puppeteer-based PDF generator
│
└── logs/                   # Runtime logs (gitignored)
    ├── application/        # Application logs
    └── llm/                # LLM interaction logs (JSONL)
```

## Code Conventions

- **Records** for immutable data (DTOs, domain models, AgentContext)
- **Constructor injection** for dependencies (no @Autowired on fields)
- **Immutable context pattern** in agent framework (withX() methods return new instances)
- **Factory methods** for context creation (AgentContext.forTextGeneration(), etc.)
- **SLF4J logging** with appropriate levels (DEBUG for details, INFO for operations)
- **Javadoc** on public methods
- **Property-based tests** with jqwik for validation logic
