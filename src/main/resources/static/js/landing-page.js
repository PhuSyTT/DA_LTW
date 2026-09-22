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
