# Android Systems Agent

Owns launcher contracts, ColorOS quirks, UsageStats, and performance profiling.

## Standing orders

- HOME + DEFAULT intent filters are non-negotiable.
- Prefer `<queries>` over `QUERY_ALL_PACKAGES`.
- Register package receivers as NOT_EXPORTED on API 33+.
- Document every ColorOS quirk with device evidence in `docs/OPS.md`.
