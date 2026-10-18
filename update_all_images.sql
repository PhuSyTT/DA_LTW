USE BookstoreChainDB;
GO

-- ============================================================================
-- SCRIPT CẬP NHẬT HÌNH ẢNH NỘI BỘ CHÍNH XÁC (OFFLINE & TỐC ĐỘ CAO)
-- Toàn bộ 35 tác giả & 110 đầu sách được lưu trữ trực tiếp trong static resource
-- ============================================================================

-- 1. Đảm bảo cột avatar_url tồn tại trong bảng authors
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'authors' AND COLUMN_NAME = 'avatar_url')
BEGIN
    ALTER TABLE authors ADD avatar_url VARCHAR(500) NULL;
END
GO

-- 2. Cập nhật ảnh chân dung tác giả nội bộ (/images/authors/author_{id}.jpg)
UPDATE authors SET avatar_url = '/images/authors/author_' + CAST(id AS VARCHAR(10)) + '.jpg'
WHERE id BETWEEN 1 AND 35;
GO

-- 3. Cập nhật ảnh bìa sách nội bộ (/images/books/book_{id}.jpg)
UPDATE books SET cover_image_url = '/images/books/book_' + CAST(id AS VARCHAR(10)) + '.jpg'
WHERE id BETWEEN 1 AND 110;
GO

PRINT N'Cập nhật thành công toàn bộ ảnh tác giả và bìa sách!';
GO
