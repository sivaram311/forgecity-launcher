# ForgeCity Launcher

**Latest:** [`v0.15.1-face-front-dev`](https://github.com/sivaram311/forgecity-launcher/releases/tag/v0.15.1-face-front-dev) · vc **34**

## Download
```powershell
curl.exe -L -o forgecity-0.15.1-face-front-dev-debug.apk `
  https://github.com/sivaram311/forgecity-launcher/releases/download/v0.15.1-face-front-dev/forgecity-0.15.1-face-front-dev-debug.apk
Get-FileHash .\forgecity-0.15.1-face-front-dev-debug.apk -Algorithm SHA256
# expect 977CCF820F37CC74F77282764A2FCBB849D9CBA02161A08D89E0D7B5BD96AF0E
```

## 0.15.1 (face front fix)
- Face card moved to **front of head** (−Z + 180° yaw); was on the back in 0.15.0

## 0.15.0 (shared face card)
- All house humanoids show **`faces/siva.png`** (256×256) as a face card

## 0.14.0 (gap #8 — IBL + fresnel stand-ins)
- Soft **256×128 HDR IBL** · reflectance + rim light
