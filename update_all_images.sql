USE BookstoreChainDB;
GO

-- 1. Thêm cột avatar_url vào bảng authors nếu chưa có
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'authors' AND COLUMN_NAME = 'avatar_url')
BEGIN
    ALTER TABLE authors ADD avatar_url VARCHAR(500) NULL;
END
GO

-- 2. Cập nhật ảnh chân dung chính xác cho toàn bộ 35 tác giả
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/6/67/Dale_Carnegie.jpg/440px-Dale_Carnegie.jpg' WHERE id = 1; -- Dale Carnegie
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/f/f6/Nguyen_Nhat_Anh_2015.jpg/440px-Nguyen_Nhat_Anh_2015.jpg' WHERE id = 2; -- Nguyễn Nhật Ánh
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/e/ee/Robert_Cecil_Martin.png/440px-Robert_Cecil_Martin.png' WHERE id = 3; -- Robert C. Martin
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/0b/Paulo_Coelho_14112007.jpg/440px-Paulo_Coelho_14112007.jpg' WHERE id = 4; -- Paulo Coelho
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/vi/thumb/9/90/Nam_Cao.jpg/440px-Nam_Cao.jpg' WHERE id = 5; -- Nam Cao
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/vi/thumb/7/77/Vutrongphung.jpg/440px-Vutrongphung.jpg' WHERE id = 6; -- Vũ Trọng Phụng
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/vi/thumb/0/0f/To_Hoai.jpg/440px-To_Hoai.jpg' WHERE id = 7; -- Tô Hoài
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/vi/thumb/6/6f/Thach_Lam.jpg/440px-Thach_Lam.jpg' WHERE id = 8; -- Thạch Lam
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/b/ba/Cha_Leung-yung_at_Hong_Kong_Book_Fair_20070720.jpg/440px-Cha_Leung-yung_at_Hong_Kong_Book_Fair_20070720.jpg' WHERE id = 9; -- Kim Dung
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/HarukiMurakami.png/440px-HarukiMurakami.png' WHERE id = 10; -- Haruki Murakami
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/2/2f/Keigo_Higashino_2010.jpg/440px-Keigo_Higashino_2010.jpg' WHERE id = 11; -- Higashino Keigo
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/b/b2/Arthur_Conan_Doyle_by_Herbert_Rose_Barraud_1893.jpg/440px-Arthur_Conan_Doyle_by_Herbert_Rose_Barraud_1893.jpg' WHERE id = 12; -- Arthur Conan Doyle
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Agatha_Christie.png/440px-Agatha_Christie.png' WHERE id = 13; -- Agatha Christie
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/7/7e/George_Orwell_press_photo.jpg/440px-George_Orwell_press_photo.jpg' WHERE id = 14; -- George Orwell
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/e/e6/Victor_Hugo_by_%C3%89tienne_Carjat_1876_-_full.jpg/440px-Victor_Hugo_by_%C3%89tienne_Carjat_1876_-_full.jpg' WHERE id = 15; -- Victor Hugo
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/2/28/ErnestHemingway.jpg/440px-ErnestHemingway.jpg' WHERE id = 16; -- Ernest Hemingway
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/4c/Kafka1906_cropped.jpg/440px-Kafka1906_cropped.jpg' WHERE id = 17; -- Franz Kafka
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/0f/Gabriel_Garcia_Marquez.jpg/440px-Gabriel_Garcia_Marquez.jpg' WHERE id = 18; -- Gabriel García Márquez
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/52/Yuval_Noah_Harari_2017.jpg/440px-Yuval_Noah_Harari_2017.jpg' WHERE id = 19; -- Yuval Noah Harari
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/91/Daniel_Kahneman_%283431628277%29_%28cropped%29.jpg/440px-Daniel_Kahneman_%283431628277%29_%28cropped%29.jpg' WHERE id = 20; -- Daniel Kahneman
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/6/6f/James_Clear.jpg/440px-James_Clear.jpg' WHERE id = 21; -- James Clear
UPDATE authors SET avatar_url = 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=440&q=80' WHERE id = 22; -- Morgan Housel
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/43/Robert_Kiyosaki_by_Gage_Skidmore.jpg/440px-Robert_Kiyosaki_by_Gage_Skidmore.jpg' WHERE id = 23; -- Robert Kiyosaki
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/b/ba/Napoleon_Hill_headshot.jpg/440px-Napoleon_Hill_headshot.jpg' WHERE id = 24; -- Napoleon Hill
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/5/57/Stephen_Covey%2C_World_Economic_Forum_2004.jpg/440px-Stephen_Covey%2C_World_Economic_Forum_2004.jpg' WHERE id = 25; -- Stephen R. Covey
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/1a/Brian_Tracy_2011.jpg/440px-Brian_Tracy_2011.jpg' WHERE id = 26; -- Brian Tracy
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/2/22/Walter_Isaacson_2012.jpg/440px-Walter_Isaacson_2012.jpg' WHERE id = 27; -- Walter Isaacson
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/2/29/Martin_Fowler_2008.jpg/440px-Martin_Fowler_2008.jpg' WHERE id = 28; -- Martin Fowler
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Joshua_Bloch.jpg/440px-Joshua_Bloch.jpg' WHERE id = 29; -- Joshua Bloch
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/7/7b/Erich_Gamma_2008.jpg/440px-Erich_Gamma_2008.jpg' WHERE id = 30; -- Erich Gamma
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/4/4f/KnuthAtOpenContentAlliance.jpg/440px-KnuthAtOpenContentAlliance.jpg' WHERE id = 31; -- Donald Knuth
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/8/86/Andrew_Tanenbaum.jpg/440px-Andrew_Tanenbaum.jpg' WHERE id = 32; -- Andrew S. Tanenbaum
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/9/91/Thomas_Cormen_headshot.jpg/440px-Thomas_Cormen_headshot.jpg' WHERE id = 33; -- Thomas H. Cormen
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/e/e9/Eric_Ries_TechCrunch_Disrupt_San_Francisco_2019.jpg/440px-Eric_Ries_TechCrunch_Disrupt_San_Francisco_2019.jpg' WHERE id = 34; -- Eric Ries
UPDATE authors SET avatar_url = 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/19/Cal_Newport_headshot.jpg/440px-Cal_Newport_headshot.jpg' WHERE id = 35; -- Cal Newport
GO

-- 3. Cập nhật ảnh bìa chính xác cho toàn bộ 110 đầu sách
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/id/8231996-L.jpg' WHERE id = 1; -- Đắc Nhân Tâm
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/id/10523450-L.jpg' WHERE id = 2; -- Quẳng Gánh Lo Đi Và Vui Sống
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/id/8231991-L.jpg' WHERE id = 3; -- Nghệ Thuật Nói Trước Công Chúng
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/id/7222246-L.jpg' WHERE id = 4; -- Thu Phục Lòng Người Trong Kỷ Nguyên Số
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=600&q=80' WHERE id = 5; -- Tôi Thấy Hoa Vàng Trên Cỏ Xanh
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1497633762265-9d179a990aa6?auto=format&fit=crop&w=600&q=80' WHERE id = 6; -- Mắt Biếc
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1532012164546-f432f2e3777a?auto=format&fit=crop&w=600&q=80' WHERE id = 7; -- Cho Tôi Xin Một Vé Đi Tuổi Thơ
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1476275466078-4007374efbbe?auto=format&fit=crop&w=600&q=80' WHERE id = 8; -- Cô Gái Đến Từ Hôm Qua
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=600&q=80' WHERE id = 9; -- Có Hai Con Mèo Ngồi Bên Cửa Sổ
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?auto=format&fit=crop&w=600&q=80' WHERE id = 10; -- Chúc Một Ngày Tốt Lành
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=600&q=80' WHERE id = 11; -- Ngồi Khóc Trên Cây
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1490750967868-88aa4486c946?auto=format&fit=crop&w=600&q=80' WHERE id = 12; -- Đi Qua Hoa Cúc
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780132350884-L.jpg' WHERE id = 13; -- Clean Code
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780134494166-L.jpg' WHERE id = 14; -- Clean Architecture
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780137081073-L.jpg' WHERE id = 15; -- The Clean Coder
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780136915713-L.jpg' WHERE id = 16; -- Clean Craftsmanship
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780135974445-L.jpg' WHERE id = 17; -- Agile Software Development
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780061122415-L.jpg' WHERE id = 18; -- Nhà Giả Kim
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780061338601-L.jpg' WHERE id = 19; -- Phù Thủy Phố Portobello
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780060928223-L.jpg' WHERE id = 20; -- Bên Sông Piedra
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780060505295-L.jpg' WHERE id = 21; -- Quỷ Dữ Và Nàng Prym
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1544716278-e513176f20b5?auto=format&fit=crop&w=600&q=80' WHERE id = 22; -- Chí Phèo & Lão Hạc
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1457369804613-52c61a468e7d?auto=format&fit=crop&w=600&q=80' WHERE id = 23; -- Sống Mòn
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1495640388908-05fa85288e61?auto=format&fit=crop&w=600&q=80' WHERE id = 24; -- Đôi Mắt
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1543002588-bfa74002ed7e?auto=format&fit=crop&w=600&q=80' WHERE id = 25; -- Số Đỏ
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1516979187457-637abb4f9353?auto=format&fit=crop&w=600&q=80' WHERE id = 26; -- Giông Tố
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1463320726281-696a485928c7?auto=format&fit=crop&w=600&q=80' WHERE id = 27; -- Kỹ Nghệ Lấy Tây
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1509021436665-8f07dbf5bf1d?auto=format&fit=crop&w=600&q=80' WHERE id = 28; -- Dế Mèn Phiêu Lưu Ký
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1519681393784-d120267933ba?auto=format&fit=crop&w=600&q=80' WHERE id = 29; -- Vợ Chồng A Phủ
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1507842229450-76905959e3e5?auto=format&fit=crop&w=600&q=80' WHERE id = 30; -- Cát Bụi Chân Ai
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=600&q=80' WHERE id = 31; -- Gió Đầu Mùa
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=600&q=80' WHERE id = 32; -- Hà Nội 36 Phố Phường
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1473496169904-658ba7c44d8a?auto=format&fit=crop&w=600&q=80' WHERE id = 33; -- Nắng Trong Vườn & Sợi Tóc
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=600&q=80' WHERE id = 34; -- Anh Hùng Xạ Điêu
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=600&q=80' WHERE id = 35; -- Thần Điêu Đại Hiệp
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1508739773434-c26b3d09e071?auto=format&fit=crop&w=600&q=80' WHERE id = 36; -- Ỷ Thiên Đồ Long Ký
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1514539079130-25950c84af65?auto=format&fit=crop&w=600&q=80' WHERE id = 37; -- Tiếu Ngạo Giang Hồ
UPDATE books SET cover_image_url = 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=600&q=80' WHERE id = 38; -- Thiên Long Bát Bộ
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780375704024-L.jpg' WHERE id = 39; -- Rừng Na Uy
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9781400079278-L.jpg' WHERE id = 40; -- Kafka Bên Bờ Biển
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780307593313-L.jpg' WHERE id = 41; -- 1Q84
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780679775430-L.jpg' WHERE id = 42; -- Biên Niên Sử Chim Vặn Dây Cót
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780307389831-L.jpg' WHERE id = 43; -- Tôi Nói Gì Khi Nói Về Chạy Bộ
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780312674397-L.jpg' WHERE id = 44; -- Phía Sau Nghi Can X
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9781975380250-L.jpg' WHERE id = 45; -- Tiệm Tạp Hóa Namiya
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780349141091-L.jpg' WHERE id = 46; -- Bạch Dạ Hành
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9781932234077-L.jpg' WHERE id = 47; -- Bí Mật Của Naoko
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9781250035639-L.jpg' WHERE id = 48; -- Ác Ý
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780140437713-L.jpg' WHERE id = 49; -- Sherlock Holmes Cuộc Phiêu Lưu
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780192840653-L.jpg' WHERE id = 50; -- Sherlock Holmes Chiếc Nhẫn Tình Cờ
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780140437867-L.jpg' WHERE id = 51; -- Con Chó Của Dòng Họ Baskerville
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780192840677-L.jpg' WHERE id = 52; -- Sherlock Holmes Thung Lũng Khủng Khiếp
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780062073488-L.jpg' WHERE id = 53; -- Mười Người Da Đen Nhỏ
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780062693662-L.jpg' WHERE id = 54; -- Án Mạng Tốc Hành Phương Đông
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780062073556-L.jpg' WHERE id = 55; -- Án Mạng Trên Sông Nile
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780008196516-L.jpg' WHERE id = 56; -- Vụ Án Mạng Bí Ẩn Ở Styles
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780451524935-L.jpg' WHERE id = 57; -- 1984
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780451526342-L.jpg' WHERE id = 58; -- Chuyện Ở Nông Trại
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780156767507-L.jpg' WHERE id = 59; -- Đường Về Wigan Pier
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780451419439-L.jpg' WHERE id = 60; -- Những Người Khốn Khổ
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780140443530-L.jpg' WHERE id = 61; -- Nhà Thờ Đức Bà Paris
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9781500494544-L.jpg' WHERE id = 62; -- Chín Mươi Ba
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780684801223-L.jpg' WHERE id = 63; -- Ông Già Và Biển Cả
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780684803357-L.jpg' WHERE id = 64; -- Chuông Nguyện Hồn Ai
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780684801469-L.jpg' WHERE id = 65; -- Giã Từ Vũ Khí
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780553213690-L.jpg' WHERE id = 66; -- Hóa Thân (The Metamorphosis)
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780805209990-L.jpg' WHERE id = 67; -- Vụ Án (The Trial)
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780805211061-L.jpg' WHERE id = 68; -- Lâu Đài (The Castle)
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780060883287-L.jpg' WHERE id = 69; -- Trăm Năm Cô Đơn
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780307389732-L.jpg' WHERE id = 70; -- Tình Yêu Thời Thổ Tả
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9781400034710-L.jpg' WHERE id = 71; -- Ký Sự Về Một Cái Chết Được Báo Trước
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780062316097-L.jpg' WHERE id = 72; -- Sapiens: Lược Sử Loài Người
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780062464316-L.jpg' WHERE id = 73; -- Homo Deus: Lược Sử Tương Lai
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780525512172-L.jpg' WHERE id = 74; -- 21 Bài Học Cho Thế Kỷ 21
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780374533557-L.jpg' WHERE id = 75; -- Tư Duy Nhanh Và Chậm
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780316451406-L.jpg' WHERE id = 76; -- Nhiễu (Noise)
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780735211292-L.jpg' WHERE id = 77; -- Thói Quen Nguyên Tử (Atomic Habits)
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780857197689-L.jpg' WHERE id = 78; -- Tâm Lý Học Về Tiền
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780593332702-L.jpg' WHERE id = 79; -- Như Chưa Từng Có Cuộc Đổi Thay
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9781612680194-L.jpg' WHERE id = 80; -- Dạy Con Làm Giàu Tập 1
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9781612680057-L.jpg' WHERE id = 81; -- Dạy Con Làm Giàu Tập 2
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9781612680217-L.jpg' WHERE id = 82; -- Dạy Con Làm Giàu Tập 3
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9781612680514-L.jpg' WHERE id = 83; -- Doanh Nghiệp Của Thế Kỷ 21
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9781585424337-L.jpg' WHERE id = 84; -- Nghĩ Giàu Và Làm Giàu
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9781402784750-L.jpg' WHERE id = 85; -- Chiến Thắng Con Quỷ Trong Bạn
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9781982137274-L.jpg' WHERE id = 86; -- 7 Thói Quen Của Người Thành Đạt
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780743272452-L.jpg' WHERE id = 87; -- Thói Quen Thứ 8
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9781416549000-L.jpg' WHERE id = 88; -- Tốc Độ Của Niềm Tin
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9781626569416-L.jpg' WHERE id = 89; -- Hãy Ăn Con Ếch Đó!
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780814433430-L.jpg' WHERE id = 90; -- Thuật Quản Trị Thời Gian
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780785288060-L.jpg' WHERE id = 91; -- Tâm Lý Học Bán Hàng
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9781451648539-L.jpg' WHERE id = 92; -- Tiểu Sử Steve Jobs
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9781982181284-L.jpg' WHERE id = 93; -- Tiểu Sử Elon Musk
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9781501139154-L.jpg' WHERE id = 94; -- Leonardo da Vinci
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780743264730-L.jpg' WHERE id = 95; -- Tiểu Sử Einstein
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780134757599-L.jpg' WHERE id = 96; -- Refactoring
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780321127426-L.jpg' WHERE id = 97; -- Patterns of Enterprise Application Architecture
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780134685991-L.jpg' WHERE id = 98; -- Effective Java
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780321336781-L.jpg' WHERE id = 99; -- Java Puzzlers
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780201633610-L.jpg' WHERE id = 100; -- Design Patterns (GoF)
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780201896831-L.jpg' WHERE id = 101; -- The Art of Computer Programming Vol 1
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780201896855-L.jpg' WHERE id = 102; -- The Art of Computer Programming Vol 3
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780132126953-L.jpg' WHERE id = 103; -- Computer Networks
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780133591620-L.jpg' WHERE id = 104; -- Modern Operating Systems
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780262046305-L.jpg' WHERE id = 105; -- Introduction to Algorithms (CLRS)
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780307887894-L.jpg' WHERE id = 106; -- The Lean Startup
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9781101903209-L.jpg' WHERE id = 107; -- The Startup Way
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9781455586691-L.jpg' WHERE id = 108; -- Deep Work
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9780525536512-L.jpg' WHERE id = 109; -- Digital Minimalism
UPDATE books SET cover_image_url = 'https://covers.openlibrary.org/b/isbn/9781455509126-L.jpg' WHERE id = 110; -- So Good They Can''t Ignore You
GO

SELECT 'Update completed successfully. Authors with avatar: ' + CAST(COUNT(avatar_url) AS VARCHAR) FROM authors;
SELECT 'Books with cover image: ' + CAST(COUNT(cover_image_url) AS VARCHAR) FROM books;
GO
