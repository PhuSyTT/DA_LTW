/**
 * Book4Life - Literary Page Transition with Video & Progress Bar
 * Hiệu ứng CHỈ xuất hiện khi bạn bắt đầu chuyển trang.
 * Khi trang mới load xong sẽ hoàn toàn KHÔNG có hiệu ứng để tránh cảm giác bị giựt.
 */

(function () {
    'use strict';

    let currentProgress = 0;
    let trickleTimer = null;
    let safetyTimer = null;

    function getOverlay() {
        return document.getElementById('page-transition-overlay');
    }

    function getProgressBar() {
        const overlay = getOverlay();
        if (!overlay) return null;
        return overlay.querySelector('.b4l-progress-fill') || overlay.querySelector('.b4l-top-loader-bar');
    }

    function getVideo() {
        const overlay = getOverlay();
        if (!overlay) return null;
        return overlay.querySelector('#b4l-loading-video');
    }

    function setProgress(percent) {
        currentProgress = Math.min(100, Math.max(0, percent));
        const bar = getProgressBar();
        if (bar) {
            bar.style.width = currentProgress + '%';
        }
    }

    // 1. CHỈ KÍCH HOẠT KHI NGƯỜI DÙNG BẮT ĐẦU CHUYỂN TRANG (Click link / Submit form)
    function startLoadingEffect() {
        const overlay = getOverlay();
        if (!overlay) return;

        clearTimeout(safetyTimer);
        stopTrickle();

        overlay.style.display = 'flex';
        overlay.setAttribute('aria-hidden', 'false');

        const video = getVideo();
        if (video) {
            try {
                video.currentTime = 0;
                video.play().catch(function () {});
            } catch (e) {}
        }

        requestAnimationFrame(function () {
            overlay.classList.add('is-loading');
        });

        setProgress(30);
        startTrickle();

        // An toàn: Tự động tắt sau 6s nếu mạng bị gián đoạn
        safetyTimer = setTimeout(forceHide, 6000);
    }

    function startTrickle() {
        stopTrickle();
        trickleTimer = setInterval(function () {
            if (currentProgress < 90) {
                const diff = (90 - currentProgress) * 0.15;
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

    // 2. KHI TRANG MỚI LOAD XONG: ẨN TỨC THÌ, TUYỆT ĐỐI KHÔNG CHẠY HIỆU ỨNG THỪA ĐỂ TRÁNH GIỰT
    function forceHide() {
        stopTrickle();
        clearTimeout(safetyTimer);
        const overlay = getOverlay();
        if (overlay) {
            overlay.classList.remove('is-loading');
            overlay.style.display = 'none';
            overlay.setAttribute('aria-hidden', 'true');
        }
        setProgress(0);
    }

    function handlePageLoaded() {
        // Trang mới đã sẵn sàng -> Không hiện bất kỳ hiệu ứng nào
        forceHide();

        // Xử lý khi người dùng nhấn nút Back / Forward của trình duyệt (bfcache)
        window.addEventListener('pageshow', function (event) {
            forceHide();
        });

        // Cho phép nhấp ra ngoài để hủy nếu cần
        const overlay = getOverlay();
        if (overlay) {
            overlay.addEventListener('click', function (e) {
                if (e.target === overlay || e.target.classList.contains('b4l-transition-backdrop')) {
                    forceHide();
                }
            });
        }
    }

    // 3. LẮNG NGHE SỰ KIỆN ĐỂ BẮT ĐẦU LOAD TRANG
    function setupNavigationListeners() {
        document.addEventListener('click', function (e) {
            const link = e.target.closest('a');
            if (!link) return;

            // Bỏ qua nếu có phím bổ trợ (mở tab mới Ctrl/Cmd click)
            if (e.defaultPrevented || e.button !== 0 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) {
                return;
            }

            // Bỏ qua link target blank hoặc download
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

            // Bỏ qua liên kết ra ngoài website
            if (link.origin && link.origin !== window.location.origin) {
                return;
            }

            // Bỏ qua nếu chỉ là anchor cùng trang
            if (link.pathname === window.location.pathname &&
                link.search === window.location.search &&
                link.hash) {
                return;
            }

            // Bỏ qua nếu bấm chính trang hiện tại
            if (link.href === window.location.href) {
                return;
            }

            // Bắt đầu hiệu ứng trên trang hiện tại trong lúc đợi trang mới nạp
            startLoadingEffect();
        });

        // Bắt sự kiện submit form (tìm kiếm, chuyển trang)
        document.addEventListener('submit', function (e) {
            const form = e.target;
            if (!form || form.target === '_blank') return;
            startLoadingEffect();
        });
    }

    handlePageLoaded();
    setupNavigationListeners();

    window.B4LLoader = {
        start: startLoadingEffect,
        hide: forceHide
    };
})();
