(() => {
  'use strict';

  const $ = (selector, root = document) => root.querySelector(selector);
  const $$ = (selector, root = document) => Array.from(root.querySelectorAll(selector));

  const setAll = (selector, text) => {
    $$(selector).forEach((el) => {
      el.textContent = text;
    });
  };

  const formatBytes = (bytes) => {
    const value = Number(bytes);
    if (!Number.isFinite(value) || value <= 0) return '';
    const mb = value / 1_000_000;
    return `${new Intl.NumberFormat('en-GB', { maximumFractionDigits: 1 }).format(mb)} MB`;
  };

  const formatDate = (value) => {
    if (!value) return '';
    const date = new Date(value.length === 10 ? `${value}T00:00:00` : value);
    if (Number.isNaN(date.getTime())) return value;
    return new Intl.DateTimeFormat('en-GB', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
    }).format(date);
  };

  const shortHash = (hash) => {
    if (!hash || hash.length < 16) return hash || '';
    return `${hash.slice(0, 8)}…${hash.slice(-4)}`;
  };

  const formatCount = (value) => {
    const count = Number(value);
    if (!Number.isFinite(count) || count < 0) return '0';
    return new Intl.NumberFormat('en-GB').format(count);
  };

  const applyDownloadCounts = (counts) => {
    if (!counts || typeof counts !== 'object') return;
    Object.entries(counts).forEach(([version, value]) => {
      const count = Number(value);
      if (!Number.isFinite(count) || count < 0) return;
      setAll(`[data-download-count="${version}"]`, formatCount(count));
      setAll(`[data-download-label="${version}"]`, count === 1 ? 'download' : 'downloads');
    });
  };

  const applyRelease = (data) => {
    if (!data || typeof data !== 'object') return;

    if (data.version) setAll('[data-release-version]', `v${data.version}`);
    if (data.sizeBytes) setAll('[data-release-size]', formatBytes(data.sizeBytes));
    if (data.installedSizeLabel) setAll('[data-installed-size]', data.installedSizeLabel);
    if (data.releasedAt) setAll('[data-release-date]', formatDate(data.releasedAt));

    if (data.sha256) {
      setAll('[data-release-sha-short]', shortHash(data.sha256));
      setAll('[data-release-sha]', data.sha256);
      $$('[data-copy-sha]').forEach((button) => {
        button.dataset.fullSha = data.sha256;
      });
    }

    if (data.certSha1) setAll('[data-release-cert]', data.certSha1);

    if (data.apkUrl) {
      $$('[data-download-link]').forEach((link) => {
        link.setAttribute('href', data.apkUrl);
      });
    }

    if (data.notesUrl) {
      $$('[data-notes-link]').forEach((link) => {
        link.setAttribute('href', data.notesUrl);
      });
    }

    if (data.downloads) applyDownloadCounts(data.downloads);
  };

  const applyWindowsRelease = (data) => {
    if (!data || typeof data !== 'object') return;

    if (data.version) setAll('[data-win-version]', `v${data.version}`);
    if (data.sizeBytes) setAll('[data-win-size]', formatBytes(data.sizeBytes));
    if (data.releasedAt) setAll('[data-win-date]', formatDate(data.releasedAt));

    if (data.sha256) {
      setAll('[data-win-sha-short]', shortHash(data.sha256));
      setAll('[data-win-sha]', data.sha256);
      $$('[data-copy-win-sha]').forEach((button) => {
        button.dataset.fullSha = data.sha256;
      });
    }

    if (data.installerUrl) {
      $$('[data-win-download]').forEach((link) => {
        link.setAttribute('href', data.installerUrl);
      });
    }

    if (data.notesUrl) {
      $$('[data-win-notes]').forEach((link) => {
        link.setAttribute('href', data.notesUrl);
      });
    }
  };

  const refreshDownloadCounts = async () => {
    try {
      const response = await fetch('https://api.github.com/repos/isygold1/nimelssa-vault/releases?per_page=10', {
        cache: 'no-store',
        headers: { Accept: 'application/vnd.github+json' },
      });
      if (!response.ok) return;
      const releases = await response.json();
      const counts = {};
      releases.forEach((release) => {
        const tag = String(release.tag_name || '');

        if (tag.startsWith('desktop-v')) {
          const desktopVersion = tag.replace(/^desktop-v/, '');
          const desktopAsset = (release.assets || []).find(
            (item) => item.name === `NIMELSSA-Vault-${desktopVersion}.exe`
          );
          if (desktopAsset && Number.isFinite(desktopAsset.download_count)) {
            counts[`desktop-${desktopVersion}`] = desktopAsset.download_count;
          }
          return;
        }

        const version = tag.replace(/^v/, '');
        const asset = (release.assets || []).find((item) => item.name === `NIMELSSA-Vault-v${version}.apk`);
        if (asset && Number.isFinite(asset.download_count)) counts[version] = asset.download_count;
      });
      if (Object.keys(counts).length) applyDownloadCounts(counts);
    } catch {
      // Keep the cached counts from release.json.
    }
  };

  if (!document.documentElement.dataset.archive) {
    fetch('release.json', { cache: 'no-store' })
      .then((response) => {
        if (!response.ok) throw new Error('release manifest unavailable');
        return response.json();
      })
      .then(applyRelease)
      .catch(() => {
        // The page keeps the last-known values embedded in the HTML.
      });

    fetch('windows-release.json', { cache: 'no-store' })
      .then((response) => {
        if (!response.ok) throw new Error('Windows release manifest unavailable');
        return response.json();
      })
      .then(applyWindowsRelease)
      .catch(() => {
        // The Windows card keeps the last-known values embedded in the HTML.
      });
  }

  refreshDownloadCounts();

  const platformTabs = $$('[data-platform-tab]');
  const platformPanels = $$('[data-platform-panel]');
  if (platformTabs.length && platformPanels.length) {
    const selectPlatform = (platform, focus = false) => {
      platformTabs.forEach((tab) => {
        const active = tab.dataset.platformTab === platform;
        tab.classList.toggle('is-active', active);
        tab.setAttribute('aria-selected', active ? 'true' : 'false');
        tab.tabIndex = active ? 0 : -1;
        if (active && focus) tab.focus();
      });
      platformPanels.forEach((panel) => {
        panel.hidden = panel.dataset.platformPanel !== platform;
      });
      $$('[data-platform-show]').forEach((section) => {
        section.hidden = section.dataset.platformShow !== platform;
      });
    };

    platformTabs.forEach((tab) => {
      tab.addEventListener('click', () => selectPlatform(tab.dataset.platformTab));
      tab.addEventListener('keydown', (event) => {
        if (event.key !== 'ArrowLeft' && event.key !== 'ArrowRight') return;
        event.preventDefault();
        const current = platformTabs.findIndex((item) => item.classList.contains('is-active'));
        const step = event.key === 'ArrowRight' ? 1 : -1;
        const next = (current + step + platformTabs.length) % platformTabs.length;
        selectPlatform(platformTabs[next].dataset.platformTab, true);
      });
    });

    $$('[data-select-platform]').forEach((link) => {
      link.addEventListener('click', () => selectPlatform(link.dataset.selectPlatform));
    });

    const requestedPlatform = new URLSearchParams(window.location.search).get('platform');
    if (requestedPlatform === 'android' || requestedPlatform === 'windows') {
      selectPlatform(requestedPlatform);
    }
  }

  const header = $('#site-header');
  if (header) {
    const syncHeader = () => header.classList.toggle('is-scrolled', window.scrollY > 24);
    syncHeader();
    window.addEventListener('scroll', syncHeader, { passive: true });
  }

  const menu = $('#mobile-menu');
  const menuToggle = $('.menu-toggle');
  if (menu && menuToggle && typeof menu.showModal === 'function') {
    menuToggle.addEventListener('click', () => {
      if (menu.open) return;
      menu.showModal();
      menuToggle.setAttribute('aria-expanded', 'true');
    });

    $$('[data-close-menu], .mobile-nav a', menu).forEach((el) => {
      el.addEventListener('click', () => menu.close());
    });

    menu.addEventListener('close', () => {
      menuToggle.setAttribute('aria-expanded', 'false');
      menuToggle.focus();
    });
  }

  const activeLinks = $$('.site-nav a[href^="#"]');
  if (activeLinks.length && 'IntersectionObserver' in window) {
    const sections = activeLinks
      .map((link) => document.querySelector(link.getAttribute('href')))
      .filter(Boolean);

    if (sections.length) {
      const sectionObserver = new IntersectionObserver(
        (entries) => {
          entries.forEach((entry) => {
            if (!entry.isIntersecting) return;
            activeLinks.forEach((link) => {
              link.classList.toggle(
                'is-active',
                link.getAttribute('href') === `#${entry.target.id}`
              );
            });
          });
        },
        { rootMargin: '-35% 0px -55% 0px', threshold: 0 }
      );

      sections.forEach((section) => sectionObserver.observe(section));
    }
  }

  const faqItems = $$('.faq-item');
  faqItems.forEach((item) => {
    item.addEventListener('toggle', () => {
      if (!item.open) return;
      faqItems.forEach((other) => {
        if (other !== item) other.open = false;
      });
    });
  });

  const copyText = async (text) => {
    try {
      if (navigator.clipboard && window.isSecureContext) {
        await navigator.clipboard.writeText(text);
        return true;
      }
    } catch (error) {
      // Fall through to the legacy path.
    }

    try {
      const field = document.createElement('textarea');
      field.value = text;
      field.setAttribute('readonly', '');
      field.style.position = 'fixed';
      field.style.opacity = '0';
      document.body.appendChild(field);
      field.select();
      const copied = document.execCommand('copy');
      field.remove();
      return copied;
    } catch (error) {
      return false;
    }
  };

  $$('[data-copy-sha], [data-copy-win-sha]').forEach((button) => {
    const isWindows = button.hasAttribute('data-copy-win-sha');
    const copyLabel = isWindows ? 'Copy Windows SHA-256' : 'Copy SHA-256';
    const copiedLabel = isWindows ? 'Windows SHA-256 copied' : 'SHA-256 copied';

    button.addEventListener('click', async () => {
      const value = button.dataset.fullSha || '';
      if (!value) return;
      const copied = await copyText(value);
      if (!copied) return;

      button.dataset.copied = 'true';
      button.setAttribute('aria-label', copiedLabel);

      window.clearTimeout(button.copyTimer);
      button.copyTimer = window.setTimeout(() => {
        button.dataset.copied = 'false';
        button.setAttribute('aria-label', copyLabel);
      }, 2000);
    });
  });

  const lightbox = $('#lightbox');
  const lightboxImage = $('#lightbox-img');
  const lightboxCaption = $('#lightbox-caption');
  const shots = $$('[data-shot]');

  if (lightbox && lightboxImage && lightboxCaption && shots.length) {
    let index = 0;
    let trigger = null;

    const syncLightbox = () => {
      const shot = shots[index];
      if (!shot) return;
      lightboxImage.src = shot.dataset.full || $('img', shot)?.src || '';
      lightboxImage.alt = shot.dataset.alt || '';
      lightboxCaption.textContent = shot.dataset.caption || '';
    };

    const openLightbox = (nextIndex, nextTrigger) => {
      index = nextIndex;
      trigger = nextTrigger || null;
      syncLightbox();
      if (typeof lightbox.showModal === 'function' && !lightbox.open) {
        lightbox.showModal();
      }
      $('[data-lightbox-close]', lightbox)?.focus();
    };

    const closeLightbox = () => {
      if (lightbox.open) lightbox.close();
    };

    const stepLightbox = (direction) => {
      index = (index + direction + shots.length) % shots.length;
      syncLightbox();
    };

    shots.forEach((shot, shotIndex) => {
      shot.addEventListener('click', () => openLightbox(shotIndex, shot));
    });

    $('[data-lightbox-close]', lightbox)?.addEventListener('click', closeLightbox);
    $('[data-lightbox-prev]', lightbox)?.addEventListener('click', () => stepLightbox(-1));
    $('[data-lightbox-next]', lightbox)?.addEventListener('click', () => stepLightbox(1));

    lightbox.addEventListener('click', (event) => {
      if (event.target === lightbox) closeLightbox();
    });

    lightbox.addEventListener('keydown', (event) => {
      if (event.key === 'ArrowLeft') {
        event.preventDefault();
        stepLightbox(-1);
      }
      if (event.key === 'ArrowRight') {
        event.preventDefault();
        stepLightbox(1);
      }
    });

    lightbox.addEventListener('close', () => {
      if (trigger) trigger.focus();
      trigger = null;
    });
  }
})();
