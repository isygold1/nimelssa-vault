# NIMELSSA Vault

Static download site for NIMELSSA Vault, the academic resource app for Medical Laboratory Science students at the University of Ilorin. It serves the signed v1.3.5 APK, checksums, install guidance, release history, and release notes from a public GitHub Pages site with no backend, login, or analytics.

## Key Features

- **Direct APK download:** the signed `NIMELSSA-Vault-v1.3.5.apk` is attached to the public release in `isygold1/nimelssa-vault`; a same-origin copy in `downloads/` remains the fallback when the release asset is unavailable.
- **Release integrity:** the page publishes the APK SHA-256 and signing-certificate SHA-1, with copy-to-clipboard and `sha256sum` / `apksigner` verification commands.
- **On-site release notes:** `release-notes.html` keeps the full v1.3.5 notes public even though the app source repository is private; `release-notes-v1.3.4.html`, `release-notes-v1.3.3.html`, `release-notes-v1.3.2.html`, `release-notes-v1.3.1.html`, `release-notes-v1.3.0.html`, and `release-notes-v1.2.0.html` preserve previous releases.
- **Release history:** the landing page has a Releases section after the FAQ with the current and previous signed builds.
- **Search-suppressed public site:** all HTML pages carry `noindex, nofollow, noarchive`; `robots.txt` disallows `/downloads/`; no sitemap is deployed. This is best-effort Google suppression, not access control.
- **Clear install guidance:** three install steps, the v1.1.1 debug-signed one-time uninstall caveat, and an FAQ covering the verified-email access gate.
- **Honest sizing:** the site shows the ≈4.7 MB download and the ≈18 MB on-disk footprint before saved files; on-disk use grows mostly with downloaded resources.
- **Responsive dark UI:** the app's slate/teal palette, crest, phone previews, FAQ accordion, and screenshot lightbox work from small phones to desktop.
- **No tracking:** no analytics, cookies, login, or third-party scripts. `release.json` is fetched same-origin.

## Installation

1. Work from this directory; it is separate from the app repository:
   ```bash
   cd ~/projects/nimelssa-vault-site
   ```
2. Build a clean local preview directory. There is no compile step, but the public directory must exclude `README.md`, `firebase.json`, `_headers`, and any Cloudflare upload zip:
   ```bash
   rm -rf dist
   mkdir dist
   cp index.html release-notes*.html release.json robots.txt site.webmanifest dist/
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
   cp -R ~/projects/nimelssa-vault-site/dist/. .
   cp ~/projects/nimelssa-vault-site/README.md .
   git add .
   git commit -m "site: add NIMELSSA Vault download site"
   git push -u origin main
   ```
   Enable Pages once in the repository settings: **Settings → Pages → Build and deployment → Deploy from a branch → `main` / (root)**. The site is then served at `https://isygold1.github.io/nimelssa-vault/`.
   No `CNAME` file is deployed; the site uses the default GitHub Pages project URL. To add a custom domain later, register the domain, add a `CNAME` file, and configure DNS and HTTPS in Pages settings.
4. Verify the deployment:
   ```bash
   curl -sSI https://isygold1.github.io/nimelssa-vault/
   curl -sSI https://isygold1.github.io/nimelssa-vault/downloads/NIMELSSA-Vault-v1.3.5.apk
   curl -sSL https://isygold1.github.io/nimelssa-vault/downloads/NIMELSSA-Vault-v1.3.5.apk | sha256sum
   ```
   Compare the checksum with `release.json`. The APK links use the HTML `download` attribute; GitHub Pages cannot set a `Content-Disposition` header. Verify the MIME type after publishing.
5. Keep the fallback hosts separate: Cloudflare uses `_headers`; Firebase Hosting uses `firebase.json`. Neither file is copied to GitHub Pages. Both fallbacks inherit the same noindex pages and robots policy.

## Configuration

- **`release.json` is the release source of truth.** It carries `version`, `versionCode`, `apkUrl`, `sizeBytes`, `installedSizeLabel`, `sha256`, `certSha1`, `releasedAt`, `minAndroid`, `minSdk`, `notesUrl`, `forceAfterCancels`, `highlights`, and cached `downloads` counts. The page JS fetches it and updates the version, sizes, date, checksum, download links, and counts.
- **Static fallback values matter.** `index.html` and `release-notes.html` contain last-known values so the page still works if the fetch fails or JavaScript is unavailable. Update those fallbacks whenever you update `release.json`.
- **APK placement and filename:** keep the signed APK at `downloads/NIMELSSA-Vault-v1.3.5.apk`. The public release asset in `isygold1/nimelssa-vault` is the download link used by the site; the `downloads/` copy remains the fallback and checksum source. If the filename changes, update every `data-download-link` fallback href, `release.json`, the public release asset, and the Cloudflare `_headers` / Firebase `firebase.json` filenames if those fallbacks are used.
- **Download counts:** public releases live in `isygold1/nimelssa-vault`, not the private app repo. Each release carries the APK asset. `main.js` reads cached counts from `release.json`, then refreshes from the public GitHub Releases API. Public release assets can be downloaded without a GitHub account.
- **GitHub Pages header limitation:** GitHub Pages does not support custom `Content-Type`, `Content-Disposition`, `X-Robots-Tag`, or cache-control headers. The APK links already have `download`, which makes browsers download the same-origin file; the server header requirement is only met by the Cloudflare or Firebase fallbacks.
- **Noindex is best effort:** all HTML pages have `noindex, nofollow, noarchive`; `robots.txt` disallows `/downloads/`; no sitemap is deployed. GitHub Pages cannot set `X-Robots-Tag`. The site remains publicly accessible to anyone with the URL. This is not access control.
- **Default Pages URL:** `https://isygold1.github.io/nimelssa-vault/` is used in canonical links, Open Graph tags, and JSON-LD. Download links point to the public release assets in `isygold1/nimelssa-vault`. No `CNAME` file is deployed. To add a custom domain later, register it, add a `CNAME` file, and update every absolute URL.
- **Dedicated site repository:** the site lives in `isygold1/nimelssa-vault` on `main`. The app repository's production branch remains `test3`. The separate public `isygold1/nimelssa-backup` repository is only the Firestore dump tool; do not add site files there, and do not reuse its old Pages URL.
- **Screenshots:** the four previews are SVGs in `assets/img/` (`720×1560`). They can be replaced with real device captures at the same aspect ratio; update both the `src` and the lightbox `data-full` paths if the filenames change.
- **Manifest and icons:** `site.webmanifest` uses `start_url: "./"` and `scope: "./"` for the GitHub Pages project path. It uses the 512×512 crest at `assets/img/crest.png` for both `any` and `maskable` purposes.
- **Secrets:** never add `google-services.json`, a keystore, `keystore.properties`, a service-account JSON key, `.env` files, PATs, signing passwords, or Cloudflare API tokens to this directory or the public site repository. The site needs only the signed public APK and public checksums. If an app credential appears, stop and remove it before deploying.
- **No sitemap:** do not add `sitemap.xml` while the site is intended to stay out of search results. `robots.txt` intentionally has no `Sitemap:` line.

## FAQ

**How large is the APK, and how much space does it use?**
The signed APK is 4,660,865 bytes (≈4.7 MB). A fresh install takes about 18 MB on disk before any saved resources; that footprint grows mostly with the course files downloaded. The landing page, release card, and FAQ show both numbers so users do not confuse the download size with installed size.

**How do I ship a new app version?**
Bump `versionCode` and `versionName` in the private app repo, commit to `test3`, and push an annotated `vX.Y.Z` tag. The tag workflow builds and signs the APK, creates the private draft release, publishes the public release in `isygold1/nimelssa-vault` with the APK and `SHA256SUMS.txt`, and commits the site update: `release.json`, `index.html`, `release-notes.html`, the previous notes archive, and `downloads/NIMELSSA-Vault-vX.Y.Z.apk`. The workflow needs a repository secret named `SITE_REPO_TOKEN` — a fine-grained PAT with `Contents: Read and write` on `isygold1/nimelssa-vault`. If it is missing, the tag build fails at the website step. The workflow file at the tagged commit is what runs, so commit the workflow change before creating the next tag. The Cloudflare and Firebase fallbacks still need their APK filename updated manually when those hosts are deployed.

**Will the site appear in Google search results?**
The two HTML pages carry `noindex, nofollow, noarchive`, and the APK/download folder is disallowed in `robots.txt`. That is best-effort suppression, not a guarantee. The Pages URL is public, the site repository is public, and anyone with the link can open the site and download the APK. For real privacy, use an access-controlled host instead of GitHub Pages.

**Why a separate site repository?**
The app repository is private. GitHub Pages from a private repository requires a paid plan, while a dedicated public repository named `nimelssa-vault` gives a clean Pages URL and keeps the site files out of the backup repo. The public site repo also hosts the APK release assets used for download counting, so the app repo stays private. The `nimelssa-backup` repository remains the separate Firestore dump tool.

**Do I need to copy the app's `google-services.json` here?**
No. The website has no Firebase SDK, no app initialization, and no login. It only serves static files and a public release manifest. Do not add app configuration or credentials to this directory or the site repository.

**Is the site tracking visitors?**
No analytics, cookies, login, or backend are included. `release.json` is fetched from the same origin, and the public GitHub Releases API is used to read download counts. GitHub may keep its own request logs; review GitHub's policy if you need to disclose it.

**Does the site verify the APK for me?**
It publishes the expected SHA-256 and certificate SHA-1 and shows the commands to compare them. The browser download itself is not an automatic verification step; users who want assurance should run `sha256sum` and `apksigner verify --print-certs`.

**What am I allowed to do with the app?**
The app is proprietary, all rights reserved, and intended for personal use. Do not redistribute or republish the APK without the author's permission. The site is the official download point; share the page rather than mirroring the file.

## Credits

**AGBOOLA ISRAEL OLUWAGBOGO**  
<israelagboola53@gmail.com>

> Independent student project. Not affiliated with or endorsed by NIMELSSA or the University of Ilorin. The NIMELSSA emblem is used with permission of the NIMELSSA President. AI assistance tools were used in the creation and debugging of this project.
