# Quick Search — Feature Suggestions

This document captures potential features, enhancements, and improvements for the Quick Search app, based on analysis of the current codebase (v4.4). Ideas are grouped by area and include rough implementation notes.

---

## 🔍 Search & Discovery

### 1. **Smart Search History & Suggestions**
- **Query autocomplete** — Show recent/popular queries as user types (debounced, local-only)
- **Time-aware suggestions** — Morning: calendar/weather; Evening: media/apps; Night: notes/tools
- **Cross-device sync (opt-in)** — Encrypted export/import of search history via file or cloud (Drive, Nextcloud)
- **Search analytics dashboard** — Local-only stats: top queries, most-used sections, tool usage frequency

### 2. **Enhanced Section Ranking**
- **Per-section result caps** — Configurable max results per section (e.g., 3 contacts, 5 files, 8 apps)
- **Dynamic section ordering** — Auto-reorder sections based on query intent (e.g., "calc" → Calculator first)
- **Section-specific fuzzy thresholds** — Tighter matching for contacts/files, looser for apps
- **Pinned items per section** — Already exists globally; add per-section pinning UI

### 3. **New Search Sections**
| Section | Data Source | Use Case |
|---------|-------------|----------|
| **Messages** | NotificationListenerService (already have) | Search SMS/Telegram/Signal/WhatsApp notifications |
| **Music** | MediaSession / Music apps | Play/pause, search tracks, control playback |
| **Clipboard** | ClipboardManager (API 28+) | Paste history, search snippets, pin favorites |
| **Screenshots** | MediaStore | Quick access to recent screenshots |
| **Downloads** | DownloadsProvider | Find recent downloads |
| **Bluetooth/Wi-Fi** | DeviceSettings (extend) | Toggle, connect, forget networks |
| **App Info** | PackageManager | Version, permissions, storage, data usage |

### 4. **Search Modes & Filters**
- **"Open with..." picker** — Long-press result → choose app (browser, editor, etc.)
- **Regex search toggle** — Power-user mode for files/notes/contacts
- **Date range filter** — For calendar, notes, files ("notes from last week")
- **Type filter chips** — Horizontal scroll: Apps / Contacts / Files / Notes / Settings / Web

---

## 🤖 AI & Tools

### 5. **AI Search Enhancements**
- **Conversation history** — Persist AI chat sessions (per provider), resume later
- **Multi-turn follow-ups** — "Refine: make it shorter" / "Translate to Spanish" / "Explain like I'm 5"
- **Provider fallback chain** — Auto-retry on rate limit/error (Gemini → Groq → OpenAI → Custom)
- **Local LLM support** — Integrate `llama.cpp` / `MLC-LLM` / `Termux + ollama` for offline AI
- **RAG with local data** — Index notes, calendar, files for context-aware answers (on-device embeddings)
- **Prompt templates library** — Built-in: "Summarize", "Code review", "Email draft", "Regex builder"
- **Streaming token-by-token UI** — Already partially there; polish with typewriter effect

### 6. **New Built-in Tools**
| Tool | Description | Alias Ideas |
|------|-------------|-------------|
| **Base64 Encoder/Decoder** | Encode/decode text, show preview | `b64`, `base64` |
| **Hash Generator** | MD5, SHA-1, SHA-256, SHA-512 | `hash`, `sha256` |
| **JWT Decoder** | Decode header/payload, verify exp | `jwt`, `decode jwt` |
| **Regex Tester** | Live match highlighting, capture groups | `regex`, `re` |
| **Cron Expression Parser** | Human-readable next run times | `cron`, `schedule` |
| **SQL Formatter** | Format/minify SQL queries | `sql`, `format sql` |
| **JSON Formatter** | Pretty-print, validate, query (jq-like) | `json`, `jq` |
| **URL Encoder/Decoder** | Percent-encode, decode, parse query params | `url`, `encode` |
| **Timestamp Converter** | Unix ↔ ISO ↔ Human, timezone support | `ts`, `epoch`, `time` |
| **QR Code Generator** | Text → QR bitmap, save/share | `qr`, `qrcode` |
| **Lorem Ipsum** | Generate placeholder text/paragraphs | `lorem`, `ipsum` |
| **Password Generator** | Configurable length, chars, entropy | `pass`, `genpass` |
| **Unit Converter (Extended)** | Data (bits/bytes), fuel, torque, pressure | Extend existing |
| **Tip Calculator** | Bill split, tip %, per-person | `tip`, `split` |

### 7. **Custom Tools Enhancements**
- **Tool sharing** — Export/import custom tools as JSON (share with community)
- **Tool marketplace** — Curated repo of community tools (opt-in fetch)
- **Tool versioning** — Track changes, rollback
- **Tool folders/categories** — Organize 20+ custom tools
- **Parameterized tools** — `toolName arg1 arg2` → prompt template with `{{arg1}}`

### 8. **Termux Integration**
- **Output streaming** — Show stdout/stderr incrementally (long-running commands)
- **Command templates** — Pre-defined: `git status`, `ls -la`, `pkg update`, custom
- **Interactive session support** — `tmux`/`screen` attach, REPL-style
- **Environment variable management** — Set `PATH`, `HOME`, custom vars per command
- **Result actions** — Copy, share, save as note, open file path in app

---

## 📱 Launcher & Home Screen

### 9. **Home Screen Features**
- **Smart folders** — Auto-categorize (Social, Productivity, Games) with ML/heuristics
- **App drawers** — Vertical/horizontal, alphabetical, categorical, searchable
- **Gesture system expansion** —
  - Swipe up/down/left/right on home screen (not just icons)
  - Two-finger gestures
  - Edge gestures (left/right edge swipe)
- **Dynamic icon theming** — Material You / Monet color extraction from wallpaper (API 31+)
- **Icon pack preview** — Live preview in settings before applying
- **Backup/restore layout** — JSON export of icon positions, folders, widgets
- **Hide apps from drawer** — Keep searchable but invisible in launcher
- **App categories/tags** — User-defined tags for filtering in drawer/search

### 10. **Widgets**
- **Notes widget** — Show pinned note, quick capture
- **Calendar widget** — Today's events, month view, tap to open
- **Tool widget** — Calculator/Unit converter/Weather direct on home
- **Search history widget** — Recent queries, tap to re-search
- **Custom button grid widget** — Already exists; add: folders, dynamic icons, Tasker actions
- **Stack widget** — Swipe through multiple widgets in one space

---

## ⚙️ Settings & Customization

### 11. **Settings UX**
- **Searchable settings** — Already have search; add: fuzzy, synonyms ("dark mode" → "theme")
- **Settings profiles** — "Work", "Personal", "Minimal" — switch full config sets
- **Per-app search exclusions** — Already have global; add per-section (hide from contacts only)
- **Import/Export v2** — Versioned format, selective restore (only theme, only shortcuts, etc.)
- **Settings sync** — Encrypted backup to Drive/Nextcloud, auto-restore on new device

### 12. **Accessibility & Inclusivity**
- **Screen reader optimizations** — Content descriptions, live regions for search results
- **High contrast theme** — WCAG AAA compliant color variants
- **Large text scaling** — Up to 200%+ without layout breakage
- **Reduced motion** — Disable animations globally
- **One-handed mode enhancements** — Lower search bar, compact results, edge-triggered
- **Voice control** — "Hey Quick Search" hotword (optional, on-device)

---

## 🔧 Technical & Architecture

### 13. **Performance**
- **Baseline profiles** — Already generating; expand to cover overlay, widget, settings
- **Lazy loading** — Defer heavy sections (files, calendar) until scrolled into view
- **Result pagination** — Load 20, fetch more on scroll (especially for files/contacts)
- **Image loading** — Coil/Glide for app icons, contact photos, file thumbnails
- **Database indexing** — Room indices on frequently queried columns (notes, calendar)

### 14. **Testing & Quality**
- **Screenshot tests** — Add more Compose preview/snapshot tests for RTL, themes, density
- **Mutation testing** — Pitest for critical logic (ranking, fuzzy, alias resolution)
- **Contract tests** — For SearchSectionRegistry, AliasHandler, ToolSettingsRegistry
- **Performance benchmarks** — Startup, search latency (p50/p95/p99), memory

### 15. **Modularization**
- **Dynamic feature modules** — Split AI providers, Termux, Widgets into on-demand modules
- **KMP core** — Extract search logic, models, tools to Kotlin Multiplatform (iOS/Desktop later)
- **Plugin architecture** — Community search sections/tools via defined interfaces

---

## 🌐 Distribution & Ecosystem

### 16. **F-Droid Enhancements**
- **Reproducible builds** — Enable and verify
- **FDroid-specific defaults** — Already done; document more
- **Anti-feature documentation** — Clear list of optional proprietary integrations

### 17. **Integrations**
- **KDE Connect / GSConnect** — Receive notifications, send SMS, clipboard sync
- **Home Assistant** — Entity search, trigger automations, state display
- **Tasker** — Already have intents; add: variable read/write, task execution
- **Automate / MacroDroid** — Similar to Tasker
- **Termux:API** — Sensors, GPS, camera, contacts via Termux
- **ADB over network** — Remote control from desktop (dev feature)

---

## 🎨 UI/UX Polish

### 18. **Visual Refinements**
- **Motion design system** — Consistent easing, duration, stagger across app
- **Loading skeletons** — For AI results, file thumbnails, contact photos
- **Empty states** — Illustrations + actionable tips for each section
- **Dark mode refinements** — Per-section tint, AMOLED true black option
- **Animation polish** — Search bar expand/collapse, section appear/disappear, widget transitions

### 19. **Onboarding & Discovery**
- **Interactive tutorial** — First-run: search, pin, alias, tool, widget
- **Feature spotlight** — "Did you know?" banner (dismissible, non-intrusive)
- **Shortcut cheat sheet** — Accessible from search bar long-press or settings
- **Video tutorials** — Links to YouTube/website for complex features

---

## 📦 Packaging & Release

### 20. **Release Automation**
- **Changelog generation** — Conventional commits → structured changelog
- **Automated screenshots** — Fastlane screengrab for Play Store / F-Droid
- **A/B testing framework** — Remote config for feature flags (Firebase Remote Config alternative)
- **Crash reporting** — Opt-in, privacy-respecting (Sentry self-hosted / ACRA / custom)

---

## 🗂️ Implementation Priority Matrix

| Priority | Effort | Impact | Suggestions |
|----------|--------|--------|-------------|
| **High** | Low | High | Searchable settings, Per-section result caps, Tool sharing, Smart folders (basic) |
| **High** | Medium | High | AI conversation history, Local LLM support, Clipboard section, Widget improvements |
| **Medium** | Medium | High | Regex search, Date range filter, Settings profiles, Backup/restore layout |
| **Medium** | High | High | Dynamic feature modules, KMP core, Plugin architecture |
| **Low** | Low | Medium | Base64/Hash/JWT tools, QR generator, Password generator |
| **Low** | Medium | Medium | Home Assistant, KDE Connect, ADB integration |
| **Low** | High | Medium | Full gesture system, Screen reader audit, Reproducible builds |

---

## 📝 Notes for Contributors

1. **Follow existing patterns** — Check `new-search-type.md`, `new-tool.md`, `new-setting.md` in `app/src/main/java/com/tk/quicksearch/` before adding features
2. **Respect flavor differences** — Standard (Play) vs FDroid implementations in `app/src/standard/` and `app/src/fdroid/`
3. **Permission-aware** — Degrade gracefully; never crash on missing permission
4. **Local-first** — No analytics, tracking, or required network calls
5. **Test on multiple surfaces** — Standard launch, overlay, home screen, widgets, foldables/tablets
6. **Update all 12 string locales** — `values-*` directories when adding user-facing text

---

*Last updated: 2026-09-17 | Based on codebase audit of v4.4 (versionCode 83)*