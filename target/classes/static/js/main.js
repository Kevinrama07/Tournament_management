/* ============================================================
   TROPHÉE · Animations fluides
   Vanilla JS — works with Thymeleaf server-rendered pages.
   Progressive enhancement: collapses gracefully when the AOS
   CDN is unreachable (offline).
   ============================================================ */

(function () {
  'use strict';

  var prefersReducedMotion = window.matchMedia &&
    window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  document.documentElement.classList.add('js');

  /* ------------------------------------------------------------
     1. AOS (scroll reveal) — init only if library loaded
  ------------------------------------------------------------ */
  if (typeof window.AOS !== 'undefined') {
    document.documentElement.classList.add('aos-active');
    if (prefersReducedMotion) {
      // Skip reveals for motion-sensitive users: show everything now.
      document.querySelectorAll('[data-aos]').forEach(function (el) {
        el.classList.add('aos-animate');
      });
    } else {
      window.AOS.init({
        duration: 700,
        easing: 'ease-out-cubic',
        once: true,
        offset: 70,
        delay: 40,
        disableMutationObserver: false,
        startEvent: 'DOMContentLoaded'
      });
    }
  }

  /* ------------------------------------------------------------
     2. Count-up animation for dashboard stat tiles
        <span class="count-up" data-count="145">0</span>
  ------------------------------------------------------------ */
  function animateCount(el) {
    var target = parseFloat(el.getAttribute('data-count')) || 0;
    var hasFraction = /\./.test(el.getAttribute('data-count'));
    var duration = 1400;
    var start = null;

    function step(ts) {
      if (!start) start = ts;
      var progress = Math.min((ts - start) / duration, 1);
      // easeOutExpo for a smooth, punchy landing
      var eased = progress === 1 ? 1 : 1 - Math.pow(2, -10 * progress);
      var value = target * eased;
      el.textContent = hasFraction ? value.toFixed(1) : Math.round(value).toLocaleString('fr-FR');
      if (progress < 1) {
        requestAnimationFrame(step);
      }
    }

    requestAnimationFrame(step);
  }

  function runCountUps() {
    var els = document.querySelectorAll('.count-up[data-count]');
    if (!('IntersectionObserver' in window)) {
      els.forEach(animateCount);
      return;
    }
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) {
          animateCount(entry.target);
          io.unobserve(entry.target);
        }
      });
    }, { threshold: 0.3 });
    els.forEach(function (el) { io.observe(el); });
  }

  /* ------------------------------------------------------------
     3. Progress bars animate width on scroll into view
  ------------------------------------------------------------ */
  function runProgressBars() {
    var bars = document.querySelectorAll('.progress-fill[data-target]');
    if (!bars.length) return;

    function fill(el, animate) {
      var target = parseFloat(el.getAttribute('data-target')) || 0;
      if (animate) {
        el.style.width = target + '%';
      } else {
        el.style.transition = 'none';
        el.style.width = target + '%';
        // force reflow before restoring transition-less state is fine
      }
    }

    if (prefersReducedMotion) {
      bars.forEach(function (b) { fill(b, false); });
      return;
    }
    if (!('IntersectionObserver' in window)) {
      bars.forEach(function (b) { fill(b, true); });
      return;
    }
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) {
          fill(entry.target, true);
          io.unobserve(entry.target);
        }
      });
    }, { threshold: 0.2 });
    bars.forEach(function (b) { io.observe(b); });
  }

  /* ------------------------------------------------------------
     4. 3D tilt micro-interaction on cards
        <div class="tilt">
  ------------------------------------------------------------ */
  function setupTilt() {
    if (prefersReducedMotion || !window.matchMedia('(hover: hover)').matches) return;

    var cards = document.querySelectorAll('.tilt');
    cards.forEach(function (card) {
      card.addEventListener('mousemove', function (e) {
        var rect = card.getBoundingClientRect();
        var px = (e.clientX - rect.left) / rect.width - 0.5;
        var py = (e.clientY - rect.top) / rect.height - 0.5;
        card.style.transform =
          'perspective(900px) rotateX(' + (-py * 6).toFixed(2) + 'deg) rotateY(' + (px * 6).toFixed(2) + 'deg) translateY(-2px)';
      });
      card.addEventListener('mouseleave', function () {
        card.style.transform = '';
      });
    });
  }

  /* ------------------------------------------------------------
     5. Row reveal for table rows (cascade as you scroll)
  ------------------------------------------------------------ */
  function setupRows() {
    var rows = Array.prototype.filter.call(
      document.querySelectorAll('table.sheet tbody tr'),
      function (tr) { return !tr.querySelector('.empty'); }
    );
    if (!rows.length) return;

    rows.forEach(function (tr) { tr.classList.add('row-anim'); });

    if (prefersReducedMotion) {
      rows.forEach(function (tr) { tr.classList.add('is-in'); });
      return;
    }
    if (!('IntersectionObserver' in window)) {
      rows.forEach(function (tr, i) {
        setTimeout(function () { tr.classList.add('is-in'); }, i * 50);
      });
      return;
    }
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-in');
          io.unobserve(entry.target);
        }
      });
    }, { threshold: 0.1 });
    rows.forEach(function (tr) { io.observe(tr); });
  }

  /* ------------------------------------------------------------
     6. Stagger reveal helper for repeatedly drawn lists
        (mark rows / cards with .reveal)
  ------------------------------------------------------------ */
  function runStaggers() {
    if (prefersReducedMotion) return;
    var groups = document.querySelectorAll('.stagger');
    groups.forEach(function (group) {
      if (!('IntersectionObserver' in window)) {
        group.querySelectorAll(':scope > *').forEach(function (c, i) {
          setTimeout(function () { c.classList.add('is-in'); }, i * 60);
        });
        return;
      }
      var io = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
          if (entry.isIntersecting) {
            Array.prototype.forEach.call(group.children, function (c, i) {
              setTimeout(function () { c.classList.add('is-in'); }, i * 55);
            });
            io.disconnect();
          }
        });
      }, { threshold: 0.12 });
      io.observe(group);
    });
  }

  /* ------------------------------------------------------------
     7. Buttons — magnetic hover + ripple for a tactile feel
  ------------------------------------------------------------ */
  function setupButtons() {
    document.querySelectorAll('.btn').forEach(function (btn) {
      btn.classList.add('btn-anim');
      btn.addEventListener('click', function (e) {
        if (prefersReducedMotion) return;
        var ripple = document.createElement('span');
        ripple.className = 'ripple';
        var rect = btn.getBoundingClientRect();
        var size = Math.max(rect.width, rect.height);
        ripple.style.width = ripple.style.height = size + 'px';
        ripple.style.left = (e.clientX - rect.left - size / 2) + 'px';
        ripple.style.top = (e.clientY - rect.top - size / 2) + 'px';
        btn.appendChild(ripple);
        setTimeout(function () { ripple.remove(); }, 600);
      });
    });
  }

  /* ------------------------------------------------------------
     Boot
  ------------------------------------------------------------ */
  function boot() {
    runCountUps();
    runProgressBars();
    setupTilt();
    setupRows();
    runStaggers();
    setupButtons();

    // Login page: choreographed entrance.
    var loginWrap = document.querySelector('.login-wrap');
    if (loginWrap && !prefersReducedMotion) {
      setTimeout(function () { loginWrap.classList.add('ready'); }, 60);
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', boot);
  } else {
    boot();
  }

  // Expose refresh for use after any client-side DOM updates.
  window.TropheeAnim = {
    refresh: function () {
      if (typeof window.AOS !== 'undefined' && !prefersReducedMotion) {
        window.AOS.refresh();
      }
      runCountUps();
      runProgressBars();
      runStaggers();
    }
  };
})();
