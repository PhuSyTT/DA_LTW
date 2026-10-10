// Admin Master Data Scripts

document.addEventListener('DOMContentLoaded', function () {
    // 1. Live ISBN Validation with Debounce
    const isbnInput = document.getElementById('isbnInput');
    const isbnFeedback = document.getElementById('isbnFeedback');
    const bookIdInput = document.getElementById('bookIdInput');

    if (isbnInput && isbnFeedback) {
        let debounceTimer;

        isbnInput.addEventListener('input', function () {
            clearTimeout(debounceTimer);
            const isbn = isbnInput.value.trim();
            const excludeId = bookIdInput ? bookIdInput.value : '';

            if (isbn.length < 10) {
                isbnFeedback.innerHTML = '<span class="text-muted"><i class="fas fa-info-circle me-1"></i>Mã ISBN phải có ít nhất 10 ký tự.</span>';
                isbnInput.classList.remove('is-valid', 'is-invalid');
                return;
            }

            isbnFeedback.innerHTML = '<span class="text-primary"><i class="fas fa-spinner fa-spin me-1"></i>Đang kiểm tra ISBN...</span>';

            debounceTimer = setTimeout(() => {
                fetch(`/api/master/books/check-isbn?isbn=${encodeURIComponent(isbn)}&excludeId=${encodeURIComponent(excludeId)}`)
                    .then(response => response.json())
                    .then(res => {
                        if (res.success && res.data) {
                            if (res.data.available) {
                                isbnInput.classList.remove('is-invalid');
                                isbnInput.classList.add('is-valid');
                                isbnFeedback.innerHTML = `<span class="text-success"><i class="fas fa-check-circle me-1"></i>${res.data.message}</span>`;
                            } else {
                                isbnInput.classList.remove('is-valid');
                                isbnInput.classList.add('is-invalid');
                                isbnFeedback.innerHTML = `<span class="text-danger"><i class="fas fa-exclamation-triangle me-1"></i>${res.data.message}</span>`;
                            }
                        }
                    })
                    .catch(err => {
                        console.error('Lỗi khi kiểm tra ISBN:', err);
                    });
            }, 400);
        });
    }

    // 2. Cover Image Preview Handler
    const coverUrlInput = document.getElementById('coverImageUrlInput');
    const coverPreviewImg = document.getElementById('coverPreviewImg');

    if (coverUrlInput && coverPreviewImg) {
        coverUrlInput.addEventListener('input', function () {
            const url = coverUrlInput.value.trim();
            if (url) {
                coverPreviewImg.src = url;
            } else {
                coverPreviewImg.src = 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=300&q=80';
            }
        });
    }

    // 3. Auto dismiss toasts/alerts
    const alerts = document.querySelectorAll('.alert-auto-dismiss');
    alerts.forEach(alert => {
        setTimeout(() => {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });
});
