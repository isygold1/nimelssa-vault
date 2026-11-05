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
  };

  fetch('release.json', { cache: 'no-store' })
    .then((response) => {
      if (!response.ok) throw new Error('release manifest unavailable');
      return response.json();
    })
    .then(applyRelease)
    .catch(() => {
      // The page keeps the last-known values embedded in the HTML.
    });

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

  $$('[data-copy-sha]').forEach((button) => {
    button.addEventListener('click', async () => {
      const value = button.dataset.fullSha || '';
      if (!value) return;
      const copied = await copyText(value);
      if (!copied) return;

      button.dataset.copied = 'true';
      button.setAttribute('aria-label', 'SHA-256 copied');

      window.clearTimeout(button.copyTimer);
      button.copyTimer = window.setTimeout(() => {
        button.dataset.copied = 'false';
        button.setAttribute('aria-label', 'Copy SHA-256');
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
