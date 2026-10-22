/**
 * Book4Life - Modern Top Loading Bar & Instant Navigation
 * Thay thế màn hình chờ xoay bằng thanh tiến trình lướt trên cùng (YouTube/GitHub/NProgress style)
 * Hoàn toàn không chặn màn hình, thao tác ngay lập tức, siêu mượt mà.
 */

(function () {
    'use strict';

    let currentProgress = 0;
    let trickleTimer = null;
    let finishTimer = null;
    let safetyTimer = null;

    function getOverlay() {
        return document.getElementById('page-transition-overlay') || document.querySelector('.b4l-top-loader');
    }

    function getProgressBar() {
        const overlay = getOverlay();
        if (!overlay) return null;
        return overlay.querySelector('.b4l-top-loader-bar') || overlay.querySelector('.b4l-progress-fill');
    }

    function setProgress(percent) {
        currentProgress = Math.min(100, Math.max(0, percent));
        const bar = getProgressBar();
        if (bar) {
            bar.style.width = currentProgress + '%';
        }
    }

    function showLoader(startPercent) {
        clearTimeout(finishTimer);
        clearTimeout(safetyTimer);
        stopTrickle();

        const overlay = getOverlay();
        if (overlay) {
            overlay.classList.add('is-loading');
            overlay.style.opacity = '1';
            overlay.style.visibility = 'visible';
            overlay.setAttribute('aria-hidden', 'false');
        }

        setProgress(startPercent !== undefined ? startPercent : 20);

        // Bắt đầu chạy tiến trình nhỏ giọt tự nhiên trong lúc đợi mạng
        startTrickle();

        // Tự động tắt sau 4s nếu trang bị kẹt mạng
        safetyTimer = setTimeout(forceHide, 4000);
    }

    function startTrickle() {
        stopTrickle();
        trickleTimer = setInterval(function () {
            if (currentProgress < 85) {
                const diff = (85 - currentProgress) * 0.15;
                setProgress(currentProgress + Math.max(0.4, diff));
            } else {
                stopTrickle();
            }
        }, 80);
    }

    function stopTrickle() {
        if (trickleTimer) {
            clearInterval(trickleTimer);
            trickleTimer = null;
        }
    }

    function finishLoader() {
        stopTrickle();
        clearTimeout(safetyTimer);
        setProgress(100);

        finishTimer = setTimeout(function () {
            const overlay = getOverlay();
            if (overlay) {
                overlay.style.opacity = '0';
                overlay.classList.remove('is-loading');
                setTimeout(function () {
                    overlay.style.visibility = 'hidden';
                    overlay.setAttribute('aria-hidden', 'true');
                    setProgress(0);
                }, 200);
            }
        }, 120);
    }

    function forceHide() {
        stopTrickle();
        clearTimeout(finishTimer);
        clearTimeout(safetyTimer);
        const overlay = getOverlay();
        if (overlay) {
            overlay.style.opacity = '0';
            overlay.classList.remove('is-loading');
            overlay.style.visibility = 'hidden';
            overlay.setAttribute('aria-hidden', 'true');
            setProgress(0);
        }
    }

    // Xử lý khi trang vừa bắt đầu tải và khi nạp xong
    function handlePageLifecycle() {
        // Khởi động nhẹ khi tài liệu đang nạp
        if (document.readyState === 'loading') {
            showLoader(30);
            document.addEventListener('DOMContentLoaded', finishLoader);
            window.addEventListener('load', finishLoader);
        } else {
            // Trang đã sẵn sàng
            finishLoader();
        }

        // Hỗ trợ bfcache (khi bấm nút Back / Forward trên trình duyệt)
        window.addEventListener('pageshow', function (event) {
            forceHide();
        });
    }

    // Bắt sự kiện nhấp link nội bộ để hiện thanh tải ngay lập tức
    function setupLinkListeners() {
        document.addEventListener('click', function (e) {
            const link = e.target.closest('a');
            if (!link) return;

            // Bỏ qua nếu có phím bổ trợ (Ctrl/Cmd click để mở tab mới)
            if (e.defaultPrevented || e.button !== 0 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) {
                return;
            }

            // Bỏ qua link mở tab mới hoặc tải file
            if (link.target === '_blank' || link.hasAttribute('download')) {
                return;
            }

            const rawHref = link.getAttribute('href');
            if (!rawHref) return;

            // Bỏ qua anchor nội bộ, javascript, mailto, tel
            if (rawHref.startsWith('#') ||
                rawHref.startsWith('javascript:') ||
                rawHref.startsWith('mailto:') ||
                rawHref.startsWith('tel:')) {
                return;
            }

            // Bỏ qua liên kết ra trang web ngoài
            if (link.origin && link.origin !== window.location.origin) {
                return;
            }

            // Bỏ qua nếu chỉ là anchor cùng trang
            if (link.pathname === window.location.pathname &&
                link.search === window.location.search &&
                link.hash) {
                return;
            }

            // Bỏ qua nếu nhấn lại chính link hiện tại
            if (link.href === window.location.href) {
                return;
            }

            // Hiển thị thanh tiến trình tức thì ngay khi nhấn
            showLoader(25);
            // Để trình duyệt tự nhiên chuyển trang (không dùng setTimeout delay)
        });

        // Bắt sự kiện submit form (tìm kiếm, lọc)
        document.addEventListener('submit', function (e) {
            const form = e.target;
            if (!form || form.target === '_blank') return;
            showLoader(30);
        });
    }

    // Khởi tạo
    handlePageLifecycle();
    setupLinkListeners();

    // Export API nếu các script khác cần gọi thủ công
    window.B4LLoader = {
        start: showLoader,
        finish: finishLoader,
        hide: forceHide
    };
})();
