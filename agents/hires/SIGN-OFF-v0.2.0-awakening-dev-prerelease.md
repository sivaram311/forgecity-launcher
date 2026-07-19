# SIGN-OFF — forgecity-launcher tag v0.2.0-awakening-dev (prerelease/debug)

| Field | Value |
|-------|-------|
| Session | forgecity-mvp-2026-07-19 |
| Reviewer agent id | CONSCIOUS #17 readonly Reviewer (generalPurpose) |
| Provider | cursor |
| Tip SHA | 26b9f82bff2b8dfee6d7a8533e8843142104b1cd |
| Branch / tag | feature/phase-2-awakening → tag v0.2.0-awakening-dev |
| When (UTC+5:30) | 2026-07-19 15:25 |

## Checklist

- [x] Docs updated same turn (CONSCIOUS #12) or N/A with reason — working tree clean; Lead will update VERIFICATION/README/OPS after release (accepted for debug prerelease, must honestly state #16 E2E PENDING)
- [x] No secrets in commit — tracked-file secret scan clean (only `.gitignore` rule `*.keystore`); no keys/passwords tracked
- [x] Fleet splits OK (classic vs css-next / AV upgrade, etc.) — N/A (Android launcher)
- [~] DEV E2E green if this push includes a release tag (#16) — **PENDING**; ACCEPTED for **debug prerelease** per prior v0.1.0-mvp boundary. NOT acceptable for production v0.2.0.
- [x] Login E2E used DEV public domain when host exists (#18) or waive documented — N/A
- [x] Tag ≠ live understood (matrix not falsely bumped) — this is `-dev` prerelease, not production

## Audit detail

1. **Tip SHA** — `git rev-parse HEAD` = `26b9f82bff2b8dfee6d7a8533e8843142104b1cd`. `git status --porcelain` clean (no uncommitted doc updates present). Branch `feature/phase-2-awakening`. Tag should point at this SHA. ✓
2. **versionName** — `app/build.gradle.kts:19` `versionName = "0.2.0-awakening-dev"` (versionCode = 2). Matches tag. ✓
3. **APK / hash / gitignore** —
   - `dist/forgecity-0.2.0-awakening-dev-debug.apk` exists, size **11000540** bytes (matches). ✓
   - `Get-FileHash` SHA-256 = `885182F9A12671BE1E68E3DF6819518FE20A308A3D6748FCAF2C440345E89B11` = provided hash = `.sha256` companion line. ✓
   - `.gitignore` has `*.apk` (line 10) and `dist/` (line 13). `git ls-files` shows **no** `.apk` and **no** `dist/` entries. `git check-ignore` confirms APK is ignored. ✓ (NOT committed)
4. **Prerelease/debug boundary** — tag is `-dev`, filename `-debug`, versionName `-dev`; mirrors prior `v0.1.0-mvp` debug prerelease (existing tag list: only `v0.1.0-mvp`). #16 device E2E PENDING is acceptable for debug prerelease ONLY. ✓ (see conditions)
5. **Secrets / permissions** — no secrets in assets/tracked files. Source `AndroidManifest.xml` uses scoped `<queries>` (LAUNCHER/DIAL/SENDTO/IMAGE_CAPTURE/VIEW-https) — **no `QUERY_ALL_PACKAGES`**; merged debug manifest also contains no `QUERY_ALL_PACKAGES`. Permissions: `PACKAGE_USAGE_STATS`, `RECEIVE_BOOT_COMPLETED` (expected for a launcher). ✓

## Verdict

**GO** — for creating **prerelease** tag `v0.2.0-awakening-dev` at `26b9f82` with the debug APK asset.

### Conditions (must hold — reviewer would flip to NO-GO otherwise)
1. GitHub release MUST be marked **prerelease = true**. Do NOT publish as a non-prerelease / production release, and do NOT create/reuse a `v0.2.0` (production) tag.
2. VERIFICATION/README/OPS must honestly state **#16 device E2E is PENDING** (Lead updating after release is acceptable).
3. Attach the APK + `.sha256` from `dist/` as release assets only; do NOT commit them.

### Findings
- No blocking findings. All 6 audit items pass.
- Advisory: PR #1 (feature/phase-2-awakening → main) remains open — merge is a separate action; this SIGN-OFF authorizes the **prerelease tag** only.
