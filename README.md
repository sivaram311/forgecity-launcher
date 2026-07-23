# ForgeCity Launcher

**Latest:** [`v0.10.4-white-screen-fix-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.10.4-white-screen-fix-dev) · vc **26**

## Download
```powershell
curl.exe -L -o forgecity-0.10.4-white-screen-fix-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.10.4-white-screen-fix-dev/forgecity-0.10.4-white-screen-fix-dev-debug.apk
Get-FileHash .\forgecity-0.10.4-white-screen-fix-dev-debug.apk -Algorithm SHA256
# expect AF48EEA7D44FD3838724D45C68D65FC8ECBC719D7411577C36B73F87F55E7224
```

## 0.10.4 fix
White screen + orbit worked = over-exposure. `setExposure(1.15)` was EV100 (near daylight blowout). Now uses photographic f-stop/shutter/ISO; lowered sun/IBL; bloom off by day.

Grok: `docs/design/GROK-WHITE-SCREEN.md`
