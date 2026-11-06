# NIMELSSA Vault

Static download site for NIMELSSA Vault, the academic resource app for Medical Laboratory Science students at the University of Ilorin. It serves the signed v1.3.0 APK, checksums, install guidance, release history, and release notes from a public GitHub Pages site with no backend, login, or analytics.

## Key Features

- **Direct APK download:** the signed `NIMELSSA-Vault-v1.3.0.apk` is served from `downloads/` on the same origin, with no GitHub release or Drive dependency.
- **Release integrity:** the page publishes the APK SHA-256 and signing-certificate SHA-1, with copy-to-clipboard and `sha256sum` / `apksigner` verification commands.
- **On-site release notes:** `release-notes.html` keeps the full v1.3.0 notes public even though the app source repository is private; `release-notes-v1.2.0.html` preserves the previous release.
- **Release history:** the landing page has a Releases section after the FAQ with the current and previous signed builds.
- **Search-suppressed public site:** both HTML pages carry `noindex, nofollow, noarchive`; `robots.txt` disallows `/downloads/`; no sitemap is deployed. This is best-effort Google suppression, not access control.
- **Clear install guidance:** three install steps, the v1.1.1 debug-signed one-time uninstall caveat, and an FAQ covering the verified-email access gate.
- **Honest sizing:** the site shows the ≈4.6 MB download and the ≈18 MB on-disk footprint before saved files; on-disk use grows mostly with downloaded resources.
- **Responsive dark UI:** the app's slate/teal palette, crest, phone previews, FAQ accordion, and screenshot lightbox work from small phones to desktop.
- **No tracking:** no analytics, cookies, login, or third-party scripts. `release.json` is fetched same-origin.

## Installation

1. Work from this directory; it is separate from the app repository:
   ```bash
   cd ~/projects/nimelssa-vault-website
   ```
2. Build a clean local preview directory. There is no compile step, but the public directory must exclude `README.md`, `firebase.json`, `_headers`, and any Cloudflare upload zip:
   ```bash
   rm -rf dist
   mkdir dist
   cp index.html release-notes.html release.json robots.txt site.webmanifest dist/
   cp -R assets downloads dist/
   touch dist/.nojekyll
   python3 -m http.server 8080 --directory dist
   ```
   Open `http://localhost:8080/`. Python's server does not apply production headers; GitHub Pages does not apply custom headers either.
3. Deploy to the dedicated public site repository:
   ```bash
   # One-time: create the empty public repository.
   gh repo create isygold1/nimelssa-vault --public \
     --description "NIMELSSA Vault static download site"

   gh repo clone isygold1/nimelssa-vault ~/projects/nimelssa-vault-site
   cd ~/projects/nimelssa-vault-site
   git switch -c main
   cp -R ~/projects/nimelssa-vault-website/dist/. .
   cp ~/projects/nimelssa-vault-website/README.md .
   git add .
   git commit -m "site: add NIMELSSA Vault v1.3.0 download site"
   git push -u origin main
   ```
   Enable Pages once in the repository settings: **Settings → Pages → Build and deployment → Deploy from a branch → `main` / (root)**. The site is then served at `https://vault.nimelssaunilorin.com/`.
   The repository includes a `CNAME` file for `vault.nimelssaunilorin.com`. At the DNS provider, add a `CNAME` record for `vault` pointing to `isygold1.github.io`, then enable **Enforce HTTPS** in Pages settings after GitHub verifies the domain. GitHub Pages redirects the `github.io` URL to the custom domain, so the site is unreachable until that DNS record resolves.
4. Verify the deployment:
   ```bash
   curl -sSI https://vault.nimelssaunilorin.com/
   curl -sSI https://vault.nimelssaunilorin.com/downloads/NIMELSSA-Vault-v1.3.0.apk
   curl -sSL https://vault.nimelssaunilorin.com/downloads/NIMELSSA-Vault-v1.3.0.apk | sha256sum
   ```
   Compare the checksum with `release.json`. The APK links use the HTML `download` attribute; GitHub Pages cannot set a `Content-Disposition` header. Verify the MIME type after publishing.
5. Keep the fallback hosts separate: Cloudflare uses `_headers`; Firebase Hosting uses `firebase.json`. Neither file is copied to GitHub Pages. Both fallbacks inherit the same noindex pages and robots policy.

## Configuration

- **`release.json` is the release source of truth.** It carries `version`, `versionCode`, `apkUrl`, `sizeBytes`, `installedSizeLabel`, `sha256`, `certSha1`, `releasedAt`, `minAndroid`, `minSdk`, and `notesUrl`. The page JS fetches it and updates the version, sizes, date, checksum, and download links.
- **Static fallback values matter.** `index.html` and `release-notes.html` contain last-known values so the page still works if the fetch fails or JavaScript is unavailable. Update those fallbacks whenever you update `release.json`.
- **APK placement and filename:** keep the signed APK at `downloads/NIMELSSA-Vault-v1.3.0.apk`. If the filename changes, update every `data-download-link` fallback href, `release.json`, and the Cloudflare `_headers` / Firebase `firebase.json` filenames if those fallbacks are used.
- **GitHub Pages header limitation:** GitHub Pages does not support custom `Content-Type`, `Content-Disposition`, `X-Robots-Tag`, or cache-control headers. The APK links already have `download`, which makes browsers download the same-origin file; the server header requirement is only met by the Cloudflare or Firebase fallbacks.
- **Noindex is best effort:** both pages have `noindex, nofollow, noarchive`; `robots.txt` disallows `/downloads/`; no sitemap is deployed. GitHub Pages cannot set `X-Robots-Tag`. The site remains publicly accessible to anyone with the URL. This is not access control.
- **Custom domain:** `https://vault.nimelssaunilorin.com/` is used in canonical links, Open Graph tags, and JSON-LD. The repository root and `dist/` contain a `CNAME` file with `vault.nimelssaunilorin.com`; the DNS provider needs `vault` → `isygold1.github.io`. Update every absolute URL and the `CNAME` file if the domain changes.
- **Dedicated site repository:** the site lives in `isygold1/nimelssa-vault` on `main`. The app repository's production branch remains `test3`. The separate public `isygold1/nimelssa-backup` repository is only the Firestore dump tool; do not add site files there, and do not reuse its old Pages URL.
- **Screenshots:** the four previews are SVGs in `assets/img/` (`720×1560`). They can be replaced with real device captures at the same aspect ratio; update both the `src` and the lightbox `data-full` paths if the filenames change.
- **Manifest and icons:** `site.webmanifest` uses `start_url: "./"` and `scope: "./"` for the GitHub Pages project path. It uses the 512×512 crest at `assets/img/crest.png` for both `any` and `maskable` purposes.
- **Secrets:** never add `google-services.json`, a keystore, `keystore.properties`, a service-account JSON key, `.env` files, PATs, signing passwords, or Cloudflare API tokens to this directory or the public site repository. The site needs only the signed public APK and public checksums. If an app credential appears, stop and remove it before deploying.
- **No sitemap:** do not add `sitemap.xml` while the site is intended to stay out of search results. `robots.txt` intentionally has no `Sitemap:` line.

## FAQ

**How large is the APK, and how much space does it use?**
The signed APK is 4,641,661 bytes (≈4.6 MB). A fresh install takes about 18 MB on disk before any saved resources; that footprint grows mostly with the course files downloaded. The landing page, release card, and FAQ show both numbers so users do not confuse the download size with installed size.

**How do I ship a new app version?**
Replace the APK in `downloads/`, update `release.json` and the fallback values in both HTML pages, recompute the SHA-256 and certificate fingerprint, update `release-notes.html` and the Releases section, and rebuild `dist/`. Copy the public files to `isygold1/nimelssa-vault`, commit, and push. Do not add a sitemap. If the Cloudflare or Firebase fallback is used, update its APK filename as well.

**Will the site appear in Google search results?**
The two HTML pages carry `noindex, nofollow, noarchive`, and the APK/download folder is disallowed in `robots.txt`. That is best-effort suppression, not a guarantee. The Pages URL is public, the site repository is public, and anyone with the link can open the site and download the APK. For real privacy, use an access-controlled host instead of GitHub Pages.

**Why a separate site repository?**
The app repository is private. GitHub Pages from a private repository requires a paid plan, while a dedicated public repository named `nimelssa-vault` gives a clean Pages URL and keeps the site files out of the backup repo. The `nimelssa-backup` repository remains the separate Firestore dump tool.

**Do I need to copy the app's `google-services.json` here?**
No. The website has no Firebase SDK, no app initialization, and no login. It only serves static files and a public release manifest. Do not add app configuration or credentials to this directory or the site repository.

**Is the site tracking visitors?**
No analytics, cookies, login, or backend are included. `release.json` is fetched from the same origin. GitHub may keep its own request logs; review GitHub's policy if you need to disclose it.

**Does the site verify the APK for me?**
It publishes the expected SHA-256 and certificate SHA-1 and shows the commands to compare them. The browser download itself is not an automatic verification step; users who want assurance should run `sha256sum` and `apksigner verify --print-certs`.

**What am I allowed to do with the app?**
The app is proprietary, all rights reserved, and intended for personal use. Do not redistribute or republish the APK without the author's permission. The site is the official download point; share the page rather than mirroring the file.

## Credits

**AGBOOLA ISRAEL OLUWAGBOGO**  
<israelagboola53@gmail.com>

> Independent student project. Not affiliated with or endorsed by NIMELSSA or the University of Ilorin. The NIMELSSA emblem is used with permission of the NIMELSSA President. AI assistance tools were used in the creation and debugging of this project.
