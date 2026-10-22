/**
 * Book4Life - Literary Page Transition with Video & Progress Bar
 * Chuyển cảnh êm ái, mượt mà chuẩn 60fps (Khắc phục triệt để hiện tượng chớp tắt)
 */

(function () {
    'use strict';

    let currentProgress = 0;
    let trickleTimer = null;
    let finishTimer = null;
    let safetyTimer = null;
    let isTransitioning = false;
    let hasFinished = false;

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
        hasFinished = false;

        const overlay = getOverlay();
        if (!overlay) return;

        isTransitioning = true;
        overlay.classList.remove('is-leaving');
        overlay.style.display = 'flex';
        overlay.setAttribute('aria-hidden', 'false');

        // Bắt đầu phát video nếu có
        const video = getVideo();
        if (video) {
            try {
                video.currentTime = 0;
                video.play().catch(function () {});
            } catch (e) {}
        }

        // Kích hoạt hiển thị mượt mà bằng requestAnimationFrame
        requestAnimationFrame(function () {
            overlay.classList.add('is-loading');
        });

        setProgress(startPercent !== undefined ? startPercent : 30);
        startTrickle();

        // An toàn: Tự động tắt sau 4s nếu trang bị nghẽn mạng
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

    // Kết thúc chuyển cảnh êm ái: Không chớp tắt, tan mờ nhẹ nhàng
    function finishTransition() {
        if (hasFinished) return;
        hasFinished = true;

        stopTrickle();
        clearTimeout(safetyTimer);
        setProgress(100);

        finishTimer = setTimeout(function () {
            const overlay = getOverlay();
            if (overlay && isTransitioning) {
                // Thêm class tan mờ nhẹ nhàng (is-leaving)
                overlay.classList.add('is-leaving');
                overlay.classList.remove('is-loading');

                setTimeout(function () {
                    overlay.style.display = 'none';
                    overlay.classList.remove('is-leaving');
                    overlay.setAttribute('aria-hidden', 'true');
                    isTransitioning = false;
                    setProgress(0);
                }, 350); // Đợi 350ms đúng bằng thời gian transition CSS kết thúc
            } else {
                isTransitioning = false;
            }
        }, 150); // Dừng lại 150ms để mắt kịp cảm nhận thanh tiến trình chạm 100%
    }

    function forceHide() {
        hasFinished = true;
        stopTrickle();
        clearTimeout(finishTimer);
        clearTimeout(safetyTimer);
        const overlay = getOverlay();
        if (overlay) {
            overlay.classList.remove('is-loading');
            overlay.classList.remove('is-leaving');
            overlay.style.display = 'none';
            overlay.setAttribute('aria-hidden', 'true');
        }
        isTransitioning = false;
        setProgress(0);
        try {
            sessionStorage.removeItem('b4l_navigating');
        } catch (e) {}
    }

    function handlePageLifecycle() {
        // Kiểm tra xem trang có được nạp từ một cú nhấp chuyển trang nội bộ không
        let wasNavigating = false;
        try {
            wasNavigating = (sessionStorage.getItem('b4l_navigating') === '1');
            sessionStorage.removeItem('b4l_navigating');
        } catch (e) {}

        if (wasNavigating) {
            // Hiển thị tiếp overlay mượt mà từ 75% lên 100%
            showOverlay(75);
            if (document.readyState === 'complete') {
                finishTransition();
            } else {
                window.addEventListener('load', finishTransition, { once: true });
                document.addEventListener('DOMContentLoaded', finishTransition, { once: true });
            }
        } else {
            // Nếu người dùng chỉ F5 tải lại hoặc mở trực tiếp tab mới:
            // Không chớp màn hình overlay, đảm bảo giao diện hiển thị tức thì
            forceHide();
        }

        // Hỗ trợ bfcache (khi bấm Back / Forward trên trình duyệt)
        window.addEventListener('pageshow', function (event) {
            forceHide();
        });

        // Cho phép nhấp ra ngoài để đóng nếu cần
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

            // Bỏ qua nếu có phím bổ trợ (Ctrl/Cmd click mở tab mới)
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

            // Đánh dấu chuyển trang trong session để trang kế tiếp đón nhận mượt mà
            try {
                sessionStorage.setItem('b4l_navigating', '1');
            } catch (err) {}

            showOverlay(30);
        });

        document.addEventListener('submit', function (e) {
            const form = e.target;
            if (!form || form.target === '_blank') return;
            try {
                sessionStorage.setItem('b4l_navigating', '1');
            } catch (err) {}
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
