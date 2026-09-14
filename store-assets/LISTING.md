# Google Play Store Listing — OdoTrack

Everything below is ready to paste into the Play Console. Placeholders in **[brackets]**
are the only things you need to fill in (contact email, privacy-policy URL).

Package name: `com.spritzlive.speedometer`  *(permanent — set at first upload; unrelated to the display name)*

---

## Store listing

### App name  *(max 30 characters)*
```
OdoTrack: GPS Speedometer
```

### Short description  *(max 80 characters)*
```
A clean GPS speedometer and trip odometer. No ads, no tracking, fully free.
```

### Full description  *(max 4000 characters)*
```
OdoTrack turns your phone into a clean, accurate GPS speedometer and trip
odometer — with absolutely no ads, no tracking, and no cost.

Whether you're driving, cycling, running, boating, or on a train, just open the
app and your speed appears in a large, easy-to-read dial you can glance at in a
second — with your trip distance tracked right alongside it.

WHY YOU'LL LIKE IT

• Big, clear readout — a large digital number inside a smooth analog gauge.
• Trip odometer — see how far you've travelled, in miles or kilometres, and
  reset it whenever you start a new journey.
• Max & average speed — know your top speed and your trip average at a glance.
• Speed accuracy indicator — shows the GPS confidence (e.g. "±2 mph") so you
  know how trustworthy the reading is.
• Portrait and landscape — mount it any way you like; the layout adapts.
• mph or km/h — switch units with one tap; your choice is remembered.
• Stays awake — the screen won't dim while you're watching your speed.
• Dark dashboard design — easy on the eyes, day or night.

BUILT FOR PRIVACY

OdoTrack has NO internet permission at all. Your location never leaves your
phone — it is read from the GPS purely to calculate your speed and trip distance
on screen. There are no ads, no analytics, no accounts, and nothing to buy. Ever.

• No ads
• No tracking or analytics
• No sign-up
• No in-app purchases
• No internet access

Your trip distance and unit choice are saved only on your own device, so they're
there when you come back — and they never go anywhere else.

HOW IT WORKS

Speed and distance come from your phone's GPS, so accuracy is best outdoors with
a clear view of the sky. Indoors or in tunnels the signal may be weak and the
reading may show 0 — that's normal for any GPS speedometer.

Please always follow local laws and keep your eyes on the road. This app is a
convenience tool and is not a certified measuring instrument.

Completely free, forever. Enjoy!
```

---

## Release notes  *(Play Console → track → release; max 500 chars per language, wrap in language tag)*

Closed-test build (v1.4 / versionCode 5):
```
<en-US>
First test build of OdoTrack: GPS Speedometer.

• Large, easy-to-read speed dial (mph or km/h)
• Trip odometer with max & average speed
• GPS accuracy indicator
• Portrait and landscape layouts
• No ads, no tracking, no internet access

Thanks for testing! Please report anything that looks off.
</en-US>
```

When promoting to Production later, swap in user-facing notes (drop the tester ask) — for the first public release something like `Initial release.` is fine.

---

## Categorization

- **App category:** Maps & Navigation  *(alternative: Tools)*
- **Tags:** speedometer, GPS, speed, odometer, trip, mph, km/h, HUD
- **Contact email:** jcook91@gmail.com
- **Website (optional):** https://spritzlive.net/odotrack/
- **Privacy policy URL (required):** https://spritzlive.net/odotrack/privacy.html
  - Hosted (uploaded to the `/odotrack/` path on spritzlive.net).
  - ⚠️ Cloudflare bot-blocking: verify Google's crawler can actually fetch this
    URL (see note below) — automated fetches to spritzlive.net currently return 403.

---

## Graphic assets (in this folder)

| Asset | File | Required size | Status |
|---|---|---|---|
| App icon (hi-res) | `play-icon-512.png` | 512 × 512 PNG (32-bit) | ✅ OdoTrack gauge mark |
| Feature graphic | `feature-graphic.png` | 1024 × 500 | ✅ OdoTrack branding |
| Phone screenshot 1 (portrait) | `phone-screenshot-1-portrait.png` | 1080 × 1920 (9:16) | ✅ upload this — Play-compliant |
| Phone screenshot 2 (landscape) | `phone-screenshot-2-landscape.png` | 1920 × 1080 (16:9) | ✅ upload this — Play-compliant |

Play requires **2–8 phone screenshots**, each **16:9 or 9:16**, sides 320–3,840 px, PNG/JPEG ≤8 MB.
The two files above meet the spec (letterboxed onto the app's dark bg to hit exact ratios). The
raw source captures `screenshot-portrait.png` (1080×2136) and `screenshot-landscape.png` (2161×975)
are **not** compliant ratios — kept only as source. Add more if you capture a "speed showing" shot
on a drive (recommended).

---

## Data safety form  (Play Console → App content → Data safety)

Answer exactly as follows — this app genuinely collects nothing:

- **Does your app collect or share any of the required user data types?** → **No**
- **Is all of the user data encrypted in transit?** → Not applicable (no data leaves the device)
- **Do you provide a way for users to request that their data is deleted?** → Not applicable

Note on location: location is *accessed* on-device to compute speed and trip
distance but is **not collected, stored off-device, or transmitted**, so it is
not declared as "collected" data. The trip distance and unit preference are saved
only in local app storage on the device, which Google's Data safety definition
does **not** count as "collected" (that term means transmitted off the device).

---

## App content declarations

- **Privacy policy:** required — host `privacy.html` and paste the URL.
- **Ads:** **No**, this app contains no ads.
- **Content rating questionnaire:** answer No to all violence/sexual/etc. →
  expected rating **Everyone / PEGI 3**.
- **Target audience:** 13+ (not designed for children; avoids extra
  Families-policy requirements).
- **Permissions declaration:** uses `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`
  in the **foreground only** (no background location, no `INTERNET`), so no
  sensitive-permissions video/justification is required.

---

## Release checklist

1. Create app in Play Console → set default language, app name "OdoTrack: GPS Speedometer".
2. Paste short + full description; upload the graphics above.
3. Complete Data safety, Content rating, Target audience, Privacy policy URL.
4. Testing track → upload `app/build/outputs/bundle/release/app-release.aab`.
5. Keep **Play App Signing** enabled (default).
6. New personal accounts (created after 13 Nov 2023): run **closed testing with
   12+ testers for 14 days**, then apply for production access.
```
