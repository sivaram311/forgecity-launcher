# ForgeCity Launcher

**Latest:** [`v0.12.0-patrol-openroof-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.12.0-patrol-openroof-dev) · vc **30**

## Download
```powershell
curl.exe -L -o forgecity-0.12.0-patrol-openroof-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.12.0-patrol-openroof-dev/forgecity-0.12.0-patrol-openroof-dev-debug.apk
Get-FileHash .\forgecity-0.12.0-patrol-openroof-dev-debug.apk -Algorithm SHA256
# expect 4DDFFF4F12CDF0FE30948A4A861392612290E06F9E7C86B5EED8063FF4AD0E54
```

## 0.12.0 (open roof + patrols)
- **Fix:** sealed ceilings blocked orbit view → open-roof perimeter coves + light trays only
- Higher dollhouse camera home
- Mayor / workshop **patrol** loops; assistant / kitchen **sit**; talk when speaking
- Gap #7 LANDED; fresnel/HDR still deferred

## 0.11.2 (set dressing — gap backlog 4–6)
- Droop cable chains; hero props; sphere dust; pictures + corner AO
- Gap plan: `docs/design/GAP-VS-PRODUCTION-HOUSE.md`

## 0.11.0 (PH-like humanoids + day-cycle)
- Jointed capsule humanoids (idle / talk / walk)
- Day-cycle lights + window/rim finishing
