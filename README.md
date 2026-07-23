# ForgeCity Launcher

**Latest:** [`v0.11.0-humanoid-daycycle-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.11.0-humanoid-daycycle-dev) · vc **28**

## Download
```powershell
curl.exe -L -o forgecity-0.11.0-humanoid-daycycle-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.11.0-humanoid-daycycle-dev/forgecity-0.11.0-humanoid-daycycle-dev-debug.apk
Get-FileHash .\forgecity-0.11.0-humanoid-daycycle-dev-debug.apk -Algorithm SHA256
# expect E09408908541924FA99B0A1A2D1452795F41377E3DDDE67AFFBAB3D080FBE1A6
```

## 0.11.0 (PH-like humanoids + day-cycle)
- Jointed capsule humanoids (idle / talk / walk) — Production House `Humanoid.tsx` port
- ~120s Filament-safe day-cycle sun/fill + sky bounce
- Window unlit panes + corner rim strips; fill pulse retained
- Gap plan: `docs/design/GAP-VS-PRODUCTION-HOUSE.md` · Grok: `docs/design/GROK-0.11-HUMANOID.md`

## 0.10.5 (Grok wall + character plan)
- Wall bands (base/shadow/trim) + baseboard/chair/picture rails + window frames + door casings
- Per-role characters: mayor / assist / npc GLBs + idle torso bob
- Plan: `docs/design/GROK-WALL-CHARACTER-PLAN.md`
- Prior fix: 0.10.4 white-screen exposure
