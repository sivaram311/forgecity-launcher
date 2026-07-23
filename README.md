# ForgeCity Launcher

**Latest:** [`v0.11.2-set-dressing-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.11.2-set-dressing-dev) · vc **29**

## Download
```powershell
curl.exe -L -o forgecity-0.11.2-set-dressing-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.11.2-set-dressing-dev/forgecity-0.11.2-set-dressing-dev-debug.apk
Get-FileHash .\forgecity-0.11.2-set-dressing-dev-debug.apk -Algorithm SHA256
# expect 79342FD2A70D9EB47BA85956E4465F61E0E554662D51D3CA226BABF20C88B9AF
```

## 0.11.2 (set dressing — gap backlog 4–6)
- Droop cable chains (parabolic mid-span) hallway→office→workshop
- Hero props: kettle, plant, laptop, slate, toolbox, vault lock bar
- Soft **sphere** dust (not cubes); ceilings + picture frames + corner AO
- Gap plan: `docs/design/GAP-VS-PRODUCTION-HOUSE.md`

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
