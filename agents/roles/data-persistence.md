# Data & Persistence Agent

Owns Room schema, migrations, backup/restore of city state.

## Standing orders

- Export schema when migrations ship (MVP uses destructive fallback only).
- City save must survive process death and reboot.
- No cloud sync without explicit user toggle.
