/**
 * Book4Life - Literary Warmth Page Transition System
 * Sử dụng điều hướng chuẩn (full page load) với hiệu ứng overlay để tránh lỗi icon/CSS
 */

(function () {
    'use strict';

    const MIN_DISPLAY_TIME = 80;   // ms hiển thị tối thiểu
    const FADE_OUT_DURATION = 150; // ms mờ dần
    const SAFETY_TIMEOUT = 2000;   // ms tự động ẩn nếu trang tải quá lâu

    let startTime = Date.now();
    let currentProgress = 0;
    let trickleInterval = null;
    let safetyTimer = null;
    let isTransitioning = false;

    function getOverlay() {
        return document.getElementById('page-transition-overlay');
    }

    function getProgressFill() {
        var overlay = getOverlay();
        return overlay ? overlay.querySelector('.b4l-progress-fill') : null;
    }

    function setProgress(percent) {
        currentProgress = Math.min(100, Math.max(0, percent));
        var fill = getProgressFill();
        if (fill) {
            fill.style.width = currentProgress + '%';
        }
    }

    function startTrickle(startFrom, maxTarget, intervalMs) {
        stopTrickle();
        setProgress(startFrom);
        trickleInterval = setInterval(function () {
            if (currentProgress < maxTarget) {
                var remaining = maxTarget - currentProgress;
                var increment = Math.max(0.5, remaining * 0.2);
                setProgress(currentProgress + increment);
            } else {
                stopTrickle();
            }
        }, intervalMs || 60);
    }

    function stopTrickle() {
        if (trickleInterval) {
            clearInterval(trickleInterval);
            trickleInterval = null;
        }
    }

    // Hiển thị overlay
    function showOverlay(initialPercent) {
        var overlay = getOverlay();
        if (!overlay) return;

        isTransitioning = true;
        startTime = Date.now();
        overlay.style.display = 'flex';
        overlay.setAttribute('aria-hidden', 'false');
        overlay.classList.remove('fade-out');
        setProgress(initialPercent || 30);
    }

    // Ẩn overlay mượt mà
    function finishTransition(callback) {
        stopTrickle();
        setProgress(100);

        var elapsed = Date.now() - startTime;
        var remainingDelay = Math.max(0, MIN_DISPLAY_TIME - elapsed);

        setTimeout(function () {
            var overlay = getOverlay();
            if (overlay) {
                overlay.classList.add('fade-out');
                setTimeout(function () {
                    overlay.style.display = 'none';
                    overlay.setAttribute('aria-hidden', 'true');
                    isTransitioning = false;
                    if (typeof callback === 'function') callback();
                }, FADE_OUT_DURATION);
            } else {
                isTransitioning = false;
                if (typeof callback === 'function') callback();
            }
        }, remainingDelay);
    }

    // Ẩn cưỡng bức (bfcache / timeout an toàn)
    function forceDismiss() {
        stopTrickle();
        clearTimeout(safetyTimer);
        var overlay = getOverlay();
        if (overlay) {
            overlay.classList.add('fade-out');
            overlay.style.display = 'none';
            overlay.setAttribute('aria-hidden', 'true');
        }
        isTransitioning = false;
    }

    // Điều hướng chuẩn có overlay (LUÔN dùng full page load để tránh lỗi CSS/icon/script)
    function standardNavigate(targetUrl) {
        if (isTransitioning) return;
        showOverlay(30);
        startTrickle(30, 88, 60);
        setTimeout(function () {
            window.location.href = targetUrl;
        }, 100);
    }

    // Xử lý trang tải lần đầu (Initial Page Load)
    function handleInitialLoad() {
        showOverlay(35);
        startTrickle(35, 92, 40);

        function onReady() {
            finishTransition();
        }

        if (document.readyState === 'interactive' || document.readyState === 'complete') {
            onReady();
        } else {
            document.addEventListener('DOMContentLoaded', onReady);
            window.addEventListener('load', onReady);
        }

        // Safety fallback: Tự động đóng overlay sau SAFETY_TIMEOUT
        safetyTimer = setTimeout(forceDismiss, SAFETY_TIMEOUT);

        // Cho phép click vào overlay để đóng
        var overlay = getOverlay();
        if (overlay) {
            overlay.addEventListener('click', forceDismiss);
        }
    }

    // Thiết lập bắt các liên kết chuyển trang
    function setupNavigationListeners() {
        document.addEventListener('click', function (e) {
            var link = e.target.closest('a');
            if (!link) return;

            // Bỏ qua nếu có phím bổ trợ
            if (e.defaultPrevented || e.button !== 0 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) {
                return;
            }

            // Bỏ qua link mở tab mới hoặc tải file
            if (link.target === '_blank' || link.hasAttribute('download')) {
                return;
            }

            var rawHref = link.getAttribute('href');
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

            // Bỏ qua nếu chỉ là anchor nhảy vị trí trong cùng trang
            if (link.pathname === window.location.pathname &&
                link.search === window.location.search &&
                link.hash) {
                return;
            }

            // Bỏ qua nếu đang ở cùng trang
            if (link.href === window.location.href) {
                return;
            }

            e.preventDefault();
            standardNavigate(link.href);
        });

        // Hỗ trợ bfcache (Back/Forward cache)
        window.addEventListener('pageshow', function (event) {
            if (event.persisted) {
                forceDismiss();
            }
        });

        // Bắt form submit tìm kiếm hoặc bộ lọc
        document.addEventListener('submit', function (e) {
            var form = e.target;
            if (!form || form.target === '_blank') return;
            if (isTransitioning) return;
            showOverlay(35);
            startTrickle(35, 88, 60);
        });
    }

    // Khởi chạy
    handleInitialLoad();
    setupNavigationListeners();

})();
