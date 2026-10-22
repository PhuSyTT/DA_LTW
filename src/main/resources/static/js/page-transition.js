/**
 * Book4Life - Literary Page Transition with Video & Progress Bar
 * Phía trên: Logo shop
 * Ở giữa: Video lật mở những trang sách
 * Phía dưới: Thanh tiến trình đang "lật mở những trang sách"
 */

(function () {
    'use strict';

    let currentProgress = 0;
    let trickleTimer = null;
    let finishTimer = null;
    let safetyTimer = null;
    let isTransitioning = false;

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

    function showOverlay(startPercent) {
        clearTimeout(finishTimer);
        clearTimeout(safetyTimer);
        stopTrickle();

        const overlay = getOverlay();
        if (!overlay) return;

        isTransitioning = true;
        overlay.style.display = 'flex';
        overlay.setAttribute('aria-hidden', 'false');

        // Bắt đầu phát video nếu có
        const video = getVideo();
        if (video) {
            video.currentTime = 0;
            video.play().catch(function () {
                // Trình duyệt có thể chặn autoplay không tiếng hoặc video chưa sẵn sàng
            });
        }

        requestAnimationFrame(function () {
            overlay.classList.add('is-loading');
        });

        setProgress(startPercent !== undefined ? startPercent : 25);
        startTrickle();

        // An toàn: Tự động tắt sau 3.5 giây nếu trang bị nghẽn mạng
        safetyTimer = setTimeout(forceHide, 3500);
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
        }, 90);
    }

    function stopTrickle() {
        if (trickleTimer) {
            clearInterval(trickleTimer);
            trickleTimer = null;
        }
    }

    function finishTransition() {
        stopTrickle();
        clearTimeout(safetyTimer);
        setProgress(100);

        finishTimer = setTimeout(function () {
            const overlay = getOverlay();
            if (overlay) {
                overlay.classList.remove('is-loading');
                setTimeout(function () {
                    overlay.style.display = 'none';
                    overlay.setAttribute('aria-hidden', 'true');
                    isTransitioning = false;
                    setProgress(0);
                }, 220);
            } else {
                isTransitioning = false;
            }
        }, 120);
    }

    function forceHide() {
        stopTrickle();
        clearTimeout(finishTimer);
        clearTimeout(safetyTimer);
        const overlay = getOverlay();
        if (overlay) {
            overlay.classList.remove('is-loading');
            overlay.style.display = 'none';
            overlay.setAttribute('aria-hidden', 'true');
        }
        isTransitioning = false;
        setProgress(0);
    }

    function handlePageLifecycle() {
        if (document.readyState === 'loading') {
            showOverlay(35);
            document.addEventListener('DOMContentLoaded', finishTransition);
            window.addEventListener('load', finishTransition);
        } else {
            finishTransition();
        }

        // Hỗ trợ bfcache (khi bấm Back / Forward)
        window.addEventListener('pageshow', function (event) {
            forceHide();
        });

        // Cho phép người dùng nhấp để đóng overlay nếu không muốn chờ
        const overlay = getOverlay();
        if (overlay) {
            overlay.addEventListener('click', function (e) {
                if (e.target === overlay || e.target.classList.contains('b4l-transition-backdrop')) {
                    forceHide();
                }
            });
        }
    }

    function setupNavigationListeners() {
        document.addEventListener('click', function (e) {
            const link = e.target.closest('a');
            if (!link) return;

            if (e.defaultPrevented || e.button !== 0 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) {
                return;
            }

            if (link.target === '_blank' || link.hasAttribute('download')) {
                return;
            }

            const rawHref = link.getAttribute('href');
            if (!rawHref) return;

            if (rawHref.startsWith('#') ||
                rawHref.startsWith('javascript:') ||
                rawHref.startsWith('mailto:') ||
                rawHref.startsWith('tel:')) {
                return;
            }

            if (link.origin && link.origin !== window.location.origin) {
                return;
            }

            if (link.pathname === window.location.pathname &&
                link.search === window.location.search &&
                link.hash) {
                return;
            }

            if (link.href === window.location.href) {
                return;
            }

            showOverlay(30);
        });

        document.addEventListener('submit', function (e) {
            const form = e.target;
            if (!form || form.target === '_blank') return;
            showOverlay(30);
        });
    }

    handlePageLifecycle();
    setupNavigationListeners();

    window.B4LLoader = {
        show: showOverlay,
        finish: finishTransition,
        hide: forceHide
    };
})();
