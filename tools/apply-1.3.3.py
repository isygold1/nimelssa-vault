#!/usr/bin/env python3
"""Rewrite the NIMELSSA Vault site for the v1.3.3 release.

Run from the repository root. Every replacement asserts that its anchor exists
exactly as expected, so a drifted page fails loudly instead of being silently
half-updated.
"""

import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent

VERSION = "1.3.3"
VERSION_CODE = 8
APK_NAME = f"NIMELSSA-Vault-v{VERSION}.apk"
APK_URL = f"https://github.com/isygold1/nimelssa-vault/releases/download/v{VERSION}/{APK_NAME}"
SHA256 = "77a8f3240664149b704cabfeb20f77a00f051435932d7f3e78fd1f7748b216ec"
SHA_SHORT = "77a8f324…16ec"
OLD_SHA256 = "c5b4a41869e7f5baa90cea2926f58a9b202342a141bb9277799a8cc339012a93"
OLD_SHA_SHORT = "c5b4a418…2a93"
SIZE_BYTES = 4660949
RELEASED_ISO = "2026-11-09"
RELEASED_HUMAN = "9 Nov 2026"
ARROW = (
    '<svg class="icon" viewBox="0 0 24 24" aria-hidden="true">'
    '<use href="#icon-arrow-right"/></svg>'
)


def read(name):
    return (ROOT / name).read_text(encoding="utf-8")


def write(name, text):
    (ROOT / name).write_text(text, encoding="utf-8")


def sub_once(text, pattern, replacement, label):
    new, count = re.subn(pattern, replacement, text, count=1, flags=re.S)
    if count != 1:
        sys.exit(f"anchor missing: {label}")
    return new


def replace_all(text, old, new, label):
    if old not in text:
        sys.exit(f"anchor missing: {label}")
    return text.replace(old, new)


# ---------------------------------------------------------------- release.json

release = {
    "version": VERSION,
    "versionCode": VERSION_CODE,
    "apkUrl": APK_URL,
    "sizeBytes": SIZE_BYTES,
    "installedSizeLabel": "~18 MB",
    "sha256": SHA256,
    "certSha1": "c6a59025ddc8ad47458d99b394607db97d2a887c",
    "releasedAt": RELEASED_ISO,
    "minAndroid": "8.0",
    "minSdk": 26,
    "notesUrl": "release-notes.html",
    "forceAfterCancels": 3,
    "highlights": [
        "Pinned files survive clearing Recently opened",
        "Only unpinned history is cleared",
        "Eight-entry cap unchanged",
        "In-app updates and verified installs carry over",
    ],
    "downloads": {
        "1.3.3": 0,
        "1.3.2": 0,
        "1.3.1": 0,
        "1.3.0": 1,
        "1.2.0": 1,
    },
}
write("release.json", json.dumps(release, indent=2, ensure_ascii=False) + "\n")


# ------------------------------------------------------------------ index.html

html = read("index.html")

card_match = re.search(
    r'<article class="release-history-card">(?:(?!</article>).)*?v1\.3\.2'
    r'(?:(?!</article>).)*?</article>',
    html,
    re.S,
)
if not card_match:
    sys.exit("anchor missing: v1.3.2 release-history card")
old_card = card_match.group(0)
html = html.replace(old_card, "@@CARD@@", 1)

# Everything outside the archived card refers to the current release.
html = html.replace("1.3.2", VERSION)
html = html.replace(OLD_SHA256, SHA256).replace(OLD_SHA_SHORT, SHA_SHORT)
html = html.replace(
    "data-release-date>7 Nov 2026<", f"data-release-date>{RELEASED_HUMAN}<"
)
# The updater arrived in v1.3.2; that sentence stays a historical reference.
html = html.replace("From v1.3.3 onward", "From v1.3.2 onward")
html = html.replace('"datePublished": "2026-11-07"', f'"datePublished": "{RELEASED_ISO}"')
html = html.replace(f'"fileSize": "4660997"', f'"fileSize": "{SIZE_BYTES}"')

html = sub_once(
    html,
    r'<h3>What\'s new in <span data-release-version>v1\.3\.3</span></h3>\s*<ul>.*?</ul>',
    (
        "<h3>What's new in <span data-release-version>v1.3.3</span></h3>\n"
        "          <ul>\n"
        "            <li>Pinned files stay put when you clear the Recently opened strip.</li>\n"
        "            <li>The X now clears only unpinned history, still capped at eight entries.</li>\n"
        "            <li>Everything from v1.3.2 carries over: in-app update checks and verified installs.</li>\n"
        "            <li>v1.3.1 and later update in place; v1.1.1 still needs a one-time uninstall.</li>\n"
        "          </ul>"
    ),
    "whats-new list",
)

archived_card = old_card.replace(
    '<p class="release-label"><span class="status-dot" aria-hidden="true"></span> Latest release</p>',
    '<p class="release-label">Previous release</p>',
)
archived_card = archived_card.replace(
    'class="btn btn-primary" href=', 'class="btn btn-ghost" href='
)
archived_card = replace_all(
    archived_card,
    'href="release-notes.html"',
    'href="release-notes-v1.3.2.html"',
    "archived card notes link",
)

new_card = f"""<article class="release-history-card">
            <div class="release-history-main">
              <p class="release-label"><span class="status-dot" aria-hidden="true"></span> Latest release</p>
              <h3>v{VERSION} <span class="badge">Bug fix</span></h3>
              <p class="release-date">Released <span data-release-date>{RELEASED_HUMAN}</span> · <span data-download-count="{VERSION}">0</span> <span data-download-label="{VERSION}">downloads</span></p>
              <p>Pinned files now survive clearing the Recently opened strip: the X removes only unpinned history. No other behaviour changes.</p>
            </div>
            <div class="release-history-actions">
              <a class="btn btn-primary" href="{APK_URL}" download>Download v{VERSION}</a>
              <a class="text-link" href="release-notes.html">Release notes {ARROW}</a>
            </div>
          </article>"""

html = html.replace("@@CARD@@", new_card + "\n\n          " + archived_card, 1)

if "@@CARD@@" in html:
    sys.exit("placeholder left behind in index.html")

write("index.html", html)


# -------------------------------------------------------------- release notes

notes = read("release-notes.html")

# The page for the previous release becomes the archived copy, untouched.
write("release-notes-v1.3.2.html", notes)

# Mechanical version/checksum/date swap first, so the authored sections below can
# still mention v1.3.2 where the history genuinely belongs.
notes = notes.replace("1.3.2", VERSION)
notes = notes.replace(OLD_SHA256, SHA256).replace(OLD_SHA_SHORT, SHA_SHORT)
notes = notes.replace("7 Nov 2026", RELEASED_HUMAN)

notes = sub_once(
    notes,
    r'<p class="notes-summary">.*?</p>',
    (
        '<p class="notes-summary">v1.3.3 is a bug-fix release: pinned files now survive '
        "clearing the Recently opened strip. The X clears unpinned history only, pinned files stay "
        "put, and the eight-entry cap is unchanged.</p>"
    ),
    "notes summary line",
)

notes = notes.replace(
    "Release notes for NIMELSSA Vault v1.3.3: in-app updater, three-lifeline update policy, and verified downloads.",
    "Release notes for NIMELSSA Vault v1.3.3: pinned files now survive clearing the Recently opened strip.",
)
notes = notes.replace(
    'content="In-app updater, three-lifeline update policy, and verified downloads."',
    'content="Pinned files now survive clearing the Recently opened strip."',
)

notes = sub_once(
    notes,
    r'<div class="callout">.*?</div>\s*</div>',
    (
        '<div class="callout">\n'
        '            <svg class="icon callout-icon" viewBox="0 0 24 24" aria-hidden="true">'
        '<use href="#icon-check"/></svg>\n'
        "            <div><strong>Bug fix release.</strong> v1.3.3 keeps the install path from "
        "v1.3.2: install it once from this page, and later releases arrive through "
        "Settings → APP UPDATE. v1.1.1 was debug-signed and still needs a one-time uninstall before a "
        "release-signed build can install.</div>\n"
        "          </div>"
    ),
    "notes callout",
)

notes = sub_once(
    notes,
    r'<section id="summary">.*?</section>',
    """<section id="summary">
            <h2>Summary</h2>
            <p>v1.3.3 is a bug-fix release. Clearing the Recently opened strip on the Workspace screen used to wipe pinned files along with the normal history. Pinning a file is meant to survive that button, and now it does: the X removes unpinned entries and leaves every pinned file exactly where it was.</p>
            <p>Unpinned history is still trimmed to eight entries, so the strip keeps its size cap and newly opened files continue to push out the oldest unpinned ones. Pinned files are never trimmed and never count against that cap.</p>
            <p>Nothing else changes. The in-app updater, the three-lifeline policy, and the SHA-256, package name, <code>versionCode</code>, and signing-certificate checks before install all behave exactly as they did in v1.3.2.</p>
          </section>""",
    "notes summary",
)

notes = sub_once(
    notes,
    r'<section id="features">.*?</section>',
    """<section id="features">
            <h2>Key Features</h2>
            <ul>
              <li><strong>Pinned files survive:</strong> the X on the Recently opened strip clears only unpinned history; pinned files stay pinned and stay visible.</li>
              <li><strong>Same eight-entry cap:</strong> unpinned history is still trimmed to eight entries, and pinned files do not count towards it.</li>
              <li><strong>Unchanged updater:</strong> automatic check at launch, manual check from Settings → APP UPDATE, and three counted Later skips per release.</li>
              <li><strong>Verified install:</strong> SHA-256, package name, <code>versionCode</code>, and signing certificate are checked before the installer is invoked.</li>
              <li><strong>In-place update:</strong> v1.3.1 and later use the same release signing key, so no uninstall is needed.</li>
            </ul>
          </section>""",
    "notes features",
)

notes = sub_once(
    notes,
    r'<section id="installation">.*?</section>',
    """<section id="installation">
            <h2>Installation</h2>
            <ol>
              <li>If you are on v1.1.1 (debug-signed), uninstall it once. Back up anything you need first.</li>
              <li>Install <code>NIMELSSA-Vault-v1.3.3.apk</code> from the download card on this page, or update from Settings → APP UPDATE inside the app.</li>
              <li>Android may ask you to allow installs from NIMELSSA Vault the first time. Grant it once; the permission is scoped to this app.</li>
              <li>Open the app and sign in. Verify your email and complete the access gate if prompted.</li>
              <li>Pinned files, saved resources, and a chosen Download location carry over; updating from v1.3.1 or later does not clear app data.</li>
            </ol>
          </section>""",
    "notes installation",
)

notes = sub_once(
    notes,
    r'<section id="configuration">.*?</section>',
    """<section id="configuration">
            <h2>Configuration</h2>
            <ul>
              <li>No new required keys and no <code>google-services.json</code> changes.</li>
              <li>The update feed is the public <code>release.json</code> on this site; no GitHub account is required to check, download, or read the notes.</li>
              <li>Android's install-unknown-apps permission is requested the first time the app hands an APK to the installer. It is app-scoped and can be revoked in Android settings.</li>
              <li>Firestore rules, the access gate, and the Drive API key are unchanged from v1.3.2.</li>
              <li>The Windows desktop client is a separate read-only build with its own Drive key and is not affected by this release.</li>
              <li>The download folder remains a user-selected Android SAF folder and is not a Firebase setting.</li>
            </ul>
          </section>""",
    "notes configuration",
)

notes = sub_once(
    notes,
    r'<section id="changelog">.*?</section>',
    """<section id="changelog">
            <h2>Changelog</h2>

            <h3>Added</h3>
            <ul>
              <li>None.</li>
            </ul>

            <h3>Changed</h3>
            <ul>
              <li>Version bumped to <code>versionCode 8</code> / <code>versionName 1.3.3</code>.</li>
              <li>Clearing the Recently opened strip now filters to unpinned entries before trimming to the eight-entry cap.</li>
            </ul>

            <h3>Fixed</h3>
            <ul>
              <li>The X on the Recently opened strip no longer removes pinned files; only unpinned history is cleared.</li>
            </ul>

            <h3>Removed</h3>
            <ul>
              <li>None.</li>
            </ul>
          </section>""",
    "notes changelog",
)

notes = sub_once(
    notes,
    r'(<div class="faq notes-faq">)\s*',
    """<div class="faq notes-faq">
              <details class="faq-item" name="release-faq">
                <summary><span>Does clearing Recently opened still remove pinned files?</span><span class="faq-icon" aria-hidden="true"></span></summary>
                <div class="faq-answer">
                  <p>No. The X clears only unpinned history. Pinned files stay pinned, stay in the strip, and are not counted towards the eight-entry cap.</p>
                </div>
              </details>

              """,
    "notes faq",
)

write("release-notes.html", notes)

print("site updated for v1.3.3")
