# GitHub Cruise - Documentation

This folder contains all project documentation organized by type.

---

## Structure

```
docs/
├── README.md                           # This file
│
├── master/                             # Cross-platform master specs
│   ├── MASTER_FEATURE_SPECIFICATION.md # Feature inventory (all platforms)
│   ├── GITHUB_API_SPECIFICATION.md     # API reference (all platforms)
│   ├── BEST_PRACTICES.md              # Engineering standards (all platforms)
│   └── README.md                       # Master docs guide
│
├── product/                            # Product & business docs
│   ├── case-study.md
│   ├── product-development.md
│   ├── requirements-specification.md
│   └── roadmap.md
│
├── technical/                          # Technical documentation
│   ├── architecture.md
│   ├── code-coverage.md
│   ├── coverage-report.md
│   ├── design-system.md
│   ├── features.md
│   └── testing-types.md
│
└── testing/                            # Testing guides
    ├── ui-testing-guide.md            # Compose Testing setup
    └── ui-test-journeys.md            # 10 user journey tests
```

---

## Cross-Platform Master Documentation

### For All Three Projects (Android, iOS, Flutter)

These master specs define features, APIs, and standards for the entire portfolio:

- **[MASTER_FEATURE_SPECIFICATION.md](master/MASTER_FEATURE_SPECIFICATION.md)** - Complete feature inventory with Feature IDs
- **[GITHUB_API_SPECIFICATION.md](master/GITHUB_API_SPECIFICATION.md)** - API reference with API IDs
- **[BEST_PRACTICES.md](master/BEST_PRACTICES.md)** - Engineering standards and best practices
- **[Master Docs Guide](master/README.md)** - How to use and maintain these docs

**Other Projects Reference:**
- iOS: Links to these docs via GitHub URL
- Flutter: Links to these docs via GitHub URL

---

## Product Documentation

### Product thinking and business context:

- **[case-study.md](product/case-study.md)** - Executive summary
- **[product-development.md](product/product-development.md)** - Full product journey
- **[requirements-specification.md](product/requirements-specification.md)** - Business to tech translation
- **[roadmap.md](product/roadmap.md)** - 12-month strategic plan

---

## Technical Documentation

### Development and implementation details:

**Getting Started:**
1. Main [README.md](../README.md) - Setup and run the project
2. [features.md](technical/features.md) - Understand what the app does
3. [architecture.md](technical/architecture.md) - Code structure and patterns

**Development:**
- [design-system.md](technical/design-system.md) - UI guidelines and Material Design 3
- [GITHUB_API_SPECIFICATION.md](master/GITHUB_API_SPECIFICATION.md) - GitHub API endpoints (see master docs)
- [code-coverage.md](technical/code-coverage.md) - Coverage setup and best practices
- [coverage-report.md](technical/coverage-report.md) - Detailed coverage analysis

**Testing:**
- [testing-types.md](technical/testing-types.md) - All testing types overview
- [ui-testing-guide.md](testing/ui-testing-guide.md) - Compose UI testing setup
- [ui-test-journeys.md](testing/ui-test-journeys.md) - 10 user journey tests

---

## Quick Navigation

**I want to...**
- **Check cross-platform features** → [MASTER_FEATURE_SPECIFICATION.md](master/MASTER_FEATURE_SPECIFICATION.md)
- **See all available APIs** → [GITHUB_API_SPECIFICATION.md](master/GITHUB_API_SPECIFICATION.md)
- **Follow engineering standards** → [BEST_PRACTICES.md](master/BEST_PRACTICES.md)
- Understand the product → [case-study.md](product/case-study.md)
- See business thinking → [product-development.md](product/product-development.md)
- Setup the project → [../README.md](../README.md)
- Learn architecture → [architecture.md](technical/architecture.md)
- Use GitHub APIs → [GITHUB_API_SPECIFICATION.md](master/GITHUB_API_SPECIFICATION.md)
- Style components → [design-system.md](technical/design-system.md)
- Write tests → [testing-types.md](technical/testing-types.md)
- Write UI tests → [ui-testing-guide.md](testing/ui-testing-guide.md)
- Setup coverage → [code-coverage.md](technical/code-coverage.md)
- Check coverage report → [coverage-report.md](technical/coverage-report.md)

---

## Contributing

If you find issues or want to improve documentation:
1. Create an issue describing the problem
2. Submit a PR with the fix
3. Keep documentation synchronized with code
