/**
 * OldBooks Landing Page Interaction Scripts
 */

document.addEventListener('DOMContentLoaded', () => {
  // 1. Sticky Header Blur Effect on Scroll
  const header = document.querySelector('.site-header');
  window.addEventListener('scroll', () => {
    if (window.scrollY > 30) {
      header?.classList.add('scrolled');
    } else {
      header?.classList.remove('scrolled');
    }
  });

  // 2. Mobile Menu Toggle
  const mobileToggle = document.querySelector('.mobile-toggle');
  const navMenu = document.querySelector('.nav-menu');
  if (mobileToggle && navMenu) {
    mobileToggle.addEventListener('click', () => {
      navMenu.classList.toggle('active');
      const icon = mobileToggle.querySelector('i');
      if (icon) {
        icon.classList.toggle('fa-bars');
        icon.classList.toggle('fa-xmark');
      }
    });
  }

  // 3. Wishlist Toggle & Counter
  const wishlistButtons = document.querySelectorAll('.book-wishlist-btn');
  const wishlistCountBadge = document.querySelector('.header-actions .badge-count');
  let wishlistCount = parseInt(wishlistCountBadge?.textContent || '0', 10);

  wishlistButtons.forEach((btn) => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      e.stopPropagation();
      const heartIcon = btn.querySelector('i');
      const isLiked = btn.classList.toggle('active');

      if (heartIcon) {
        if (isLiked) {
          heartIcon.classList.remove('fa-regular');
          heartIcon.classList.add('fa-solid');
          wishlistCount++;
          showToast('Đã thêm vào danh sách yêu thích!');
        } else {
          heartIcon.classList.remove('fa-solid');
          heartIcon.classList.add('fa-regular');
          wishlistCount = Math.max(0, wishlistCount - 1);
          showToast('Đã bỏ khỏi danh sách yêu thích!');
        }
      }

      if (wishlistCountBadge) {
        wishlistCountBadge.textContent = wishlistCount.toString();
        wishlistCountBadge.style.transform = 'scale(1.3)';
        setTimeout(() => {
          wishlistCountBadge.style.transform = 'scale(1)';
        }, 200);
      }
    });
  });

  // 4. Category Card Click Filter Simulation
  const categoryCards = document.querySelectorAll('.category-card');
  categoryCards.forEach((card) => {
    card.addEventListener('click', () => {
      const catName = card.querySelector('.category-name')?.textContent.trim();
      showToast(`Đang lọc sách thể loại: ${catName}`);
    });
  });

  // 5. Search Bar Input Action
  const searchInput = document.querySelector('.search-input');
  if (searchInput) {
    searchInput.addEventListener('keypress', (e) => {
      if (e.key === 'Enter') {
        const query = searchInput.value.trim();
        if (query) {
          showToast(`Tìm kiếm: "${query}"`);
        }
      }
    });
  }

  // 6. Community Subscription Form
  const communityForm = document.querySelector('.community-form');
  if (communityForm) {
    communityForm.addEventListener('submit', (e) => {
      e.preventDefault();
      const emailInput = communityForm.querySelector('input[type="email"]');
      if (emailInput && emailInput.value) {
        showToast('Cảm ơn bạn đã đăng ký nhận tin sách quý!');
        emailInput.value = '';
      }
    });
  }

  // 7. Dynamic Nav Active State & Silky Smooth ScrollSpy
  const navMenu = document.querySelector('.nav-menu');
  const navLinks = document.querySelectorAll('.nav-menu .nav-link');

  // Click handler: Silky smooth scroll and switch active tab immediately
  navLinks.forEach((link) => {
    link.addEventListener('click', function(e) {
      const href = this.getAttribute('href');
      if (href && (href.startsWith('#') || href.startsWith('/#') || href === '/')) {
        const targetId = href.replace('/#', '').replace('#', '');
        
        if (targetId && targetId !== '/') {
          const targetEl = document.getElementById(targetId);
          if (targetEl) {
            e.preventDefault();
            const headerOffset = 80;
            const elementPosition = targetEl.getBoundingClientRect().top;
            const offsetPosition = elementPosition + window.pageYOffset - headerOffset;

            window.scrollTo({
              top: offsetPosition,
              behavior: 'smooth'
            });

            if (history.pushState) {
              history.pushState(null, null, '#' + targetId);
            }
          }
        } else if (href === '/' || targetId === 'hero') {
          if (window.location.pathname === '/' || window.location.pathname === '') {
            e.preventDefault();
            window.scrollTo({
              top: 0,
              behavior: 'smooth'
            });
            if (history.pushState) {
              history.pushState(null, null, window.location.pathname);
            }
          }
        }

        navLinks.forEach(l => l.classList.remove('active'));
        this.classList.add('active');
      }
    });
  });

  // ScrollSpy Section Mapping
  const sections = [
    { id: 'hero', links: ['/', '#hero', 'index.html'] },
    { id: 'categories', links: ['#categories', '/#categories'] },
    { id: 'featured-books', links: ['#featured-books', '/books', '/#featured-books'] },
    { id: 'value-prop', links: ['#value-prop', '/#value-prop'] },
    { id: 'community', links: ['#community', '/#community'] },
    { id: 'contact', links: ['#contact', '/#contact'] }
  ];

  let scrollTicking = false;

  function onScrollHandler() {
    if (!scrollTicking) {
      window.requestAnimationFrame(() => {
        updateScrollSpy();
        scrollTicking = false;
      });
      scrollTicking = true;
    }
  }

  function updateScrollSpy() {
    const currentPath = window.location.pathname;
    if (currentPath !== '/' && currentPath !== '/home' && !currentPath.endsWith('index.html')) {
      return;
    }

    const scrollY = window.scrollY;
    const windowHeight = window.innerHeight;
    const documentHeight = document.documentElement.scrollHeight;

    // Check bottom of page -> highlight "Liên hệ"
    if (scrollY + windowHeight >= documentHeight - 60) {
      setActiveNavLink(['#contact', '/#contact']);
      return;
    }

    // Check top of page -> highlight "Home"
    if (scrollY < 180) {
      setActiveNavLink(['/', '#hero', 'index.html']);
      return;
    }

    // Check sections in reverse order
    for (let i = sections.length - 1; i >= 0; i--) {
      const sec = sections[i];
      const el = document.getElementById(sec.id);
      if (el) {
        const top = el.offsetTop - 120;
        if (scrollY >= top) {
          setActiveNavLink(sec.links);
          break;
        }
      }
    }
  }

  function setActiveNavLink(targetHrefs) {
    navLinks.forEach((link) => {
      const href = link.getAttribute('href');
      if (targetHrefs.includes(href)) {
        link.classList.add('active');
      } else {
        link.classList.remove('active');
      }
    });
  }

  window.addEventListener('scroll', onScrollHandler, { passive: true });

  // Handle hash changes
  window.addEventListener('hashchange', () => {
    const hash = window.location.hash;
    if (hash) {
      navLinks.forEach(l => {
        if (l.getAttribute('href') === hash || l.getAttribute('href')?.endsWith(hash)) {
          navLinks.forEach(item => item.classList.remove('active'));
          l.classList.add('active');
        }
      });
    }
  });
});

/**
 * Lightweight Toast Notification
 */
function showToast(message) {
  let toastContainer = document.getElementById('toast-container');
  if (!toastContainer) {
    toastContainer = document.createElement('div');
    toastContainer.id = 'toast-container';
    toastContainer.style.position = 'fixed';
    toastContainer.style.bottom = '24px';
    toastContainer.style.right = '24px';
    toastContainer.style.zIndex = '9999';
    toastContainer.style.display = 'flex';
    toastContainer.style.flexDirection = 'column';
    toastContainer.style.gap = '10px';
    document.body.appendChild(toastContainer);
  }

  const toast = document.createElement('div');
  toast.style.background = '#1E1A16';
  toast.style.color = '#FFFFFF';
  toast.style.padding = '12px 20px';
  toast.style.borderRadius = '10px';
  toast.style.borderLeft = '4px solid #B8860B';
  toast.style.boxShadow = '0 8px 24px rgba(0,0,0,0.2)';
  toast.style.fontSize = '0.9rem';
  toast.style.fontFamily = "'Plus Jakarta Sans', sans-serif";
  toast.style.display = 'flex';
  toast.style.alignItems = 'center';
  toast.style.gap = '10px';
  toast.style.opacity = '0';
  toast.style.transform = 'translateY(15px)';
  toast.style.transition = 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)';
  toast.innerHTML = `<i class="fa-solid fa-circle-check" style="color: #CD853F;"></i><span>${message}</span>`;

  toastContainer.appendChild(toast);

  // Trigger animation
  requestAnimationFrame(() => {
    toast.style.opacity = '1';
    toast.style.transform = 'translateY(0)';
  });

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(15px)';
    setTimeout(() => {
      toast.remove();
    }, 300);
  }, 2800);
}
