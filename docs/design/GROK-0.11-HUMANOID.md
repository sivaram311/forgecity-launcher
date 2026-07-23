# ForgeCity - Grok 0.11 humanoid + day-cycle

**API:** grok-4.3 · **2026-07-23**

---

0.11.0-adreno710-filament4.15

**Joint offsets (m, PH proportions, Y-up, root@0,0,0):**
hipY:0.92
head:1.72 (neck@1.58)
shoulderL/R:Â±0.19,1.48,0
elbowL/R:Â±0.19,1.12,0
wristL/R:Â±0.19,0.78,0
hipL/R:Â±0.12,0.92,0
kneeL/R:Â±0.12,0.48,0
ankleL/R:Â±0.12,0.08,0

**onFrame rotations (rad, t=seconds):**
idle: armL/R=sin(t*1.2)*0.08, legL/R=sin(t*0.9)*0.05, head=sin(t*0.6)*0.04
talk: armL=sin(t*3.4)*0.22, head=sin(t*2.1)*0.11, torso=sin(t*1.8)*0.03
walk: armL=sin(t*4.2)*0.65, armR=-armL, legL=sin(t*4.2)*0.75, legR=-legL, head=sin(t*4.2)*0.07

**Day-cycle 5 stops (t 0..1, ~120s):**
0.00 #FFB366 5200 1800 0.55,0.72,0.95
0.25 #FFE4B5 5800 1200 0.48,0.65,0.92
0.50 #FFF8E7 6100 900 0.42,0.58,0.88
0.75 #FFCC99 5400 1500 0.50,0.68,0.93
1.00 #FF9966 4800 2100 0.58,0.75,0.96

**DO-NOT Adreno 710:**
no cascaded shadows, no 4K env, no per-pixel emissive >2, no dynamic bone count >18, no MSAA>2, no realtime GI probes.
