# 📘 HƯỚNG DẪN QUY TRÌNH LÀM VIỆC VỚI GIT & GITHUB (DỰ ÁN DA_LTW)

Tài liệu này hướng dẫn chi tiết quy trình làm việc chuẩn cho toàn bộ thành viên trong nhóm phát triển dự án **Bookstore Chain Management System**.

---

## ⚠️ 3 NGUYÊN TẮC VÀNG (BẮT BUỘC TUÂN THỦ)

1. ❌ **TUYỆT ĐỐI KHÔNG GÕ LỆNH:** `git push --force` (hoặc `git push -f`). Lệnh này sẽ xóa sạch công sức và commit của các thành viên khác trên GitHub!
2. ❌ **KHÔNG CODE TRỰC TIẾP TRÊN NHÁNH `main`:** Luôn tạo nhánh riêng cho từng chức năng mình làm.
3. 🔄 **LUÔN KÉO CODE MỚI NHẤT VỀ TRƯỚC KHI CODE:** Trước khi bắt đầu làm bất cứ chức năng nào, hãy cập nhật code từ nhánh `main`.

---

## 🚀 QUY TRÌNH 5 BƯỚC ĐẨY CODE LÊN GITHUB

### Bước 1: Cập nhật code mới nhất từ `main` về máy
Mở Terminal / Git Bash trong thư mục dự án và gõ:
```bash
git checkout main
git pull origin main
```
> *(Mẹo: Đảm bảo máy tính của bạn đang có code mới nhất mà các bạn khác vừa gộp vào).*

---

### Bước 2: Tự tạo và chuyển sang nhánh riêng
Tạo một nhánh mới tương ứng với chức năng bạn chuẩn bị làm:
```bash
git checkout -b <tên-nhánh>
```

**Quy chuẩn đặt tên nhánh cho nhóm:**
* Cấu trúc: `feature/<tên-chức-năng>` hoặc `<tên-bạn>/<tên-chức-năng>`
* *Ví dụ:*
  * `git checkout -b feature/cart-checkout` (Làm giỏ hàng & thanh toán)
  * `git checkout -b feature/trade-in-survey` (Làm form khảo sát thu mua sách)
  * `git checkout -b nam/fix-navbar-ui` (Sửa giao diện thanh menu)

---

### Bước 3: Viết code, kiểm tra và Commit
Sau khi bạn đã code xong chức năng hoặc làm xong một phần việc:
1. Kiểm tra các file bạn đã thay đổi:
   ```bash
   git status
   ```
2. Thêm toàn bộ các file đã sửa vào danh sách chuẩn bị lưu:
   ```bash
   git add .
   ```
3. Lưu lại với thông điệp rõ ràng, mô tả những gì bạn vừa làm:
   ```bash
   git commit -m "Mô tả ngắn gọn chức năng bạn vừa làm"
   ```
   * *Ví dụ:* `git commit -m "Hoàn thiện giao diện trang giỏ hàng"`

---

### Bước 4: Đẩy nhánh riêng của bạn lên GitHub
Gõ lệnh sau để đưa nhánh bạn vừa tạo lên GitHub:
```bash
git push -u origin <tên-nhánh>
```
* *Ví dụ:*
  ```bash
  git push -u origin feature/cart-checkout
  ```
> *(Từ lần push thứ 2 trở đi trên cùng nhánh này, bạn chỉ cần gõ ngắn gọn `git push` là được).*

---

### Bước 5: Tạo Pull Request (PR) trên GitHub để gộp code
1. Truy cập vào kho chứa của nhóm: [https://github.com/PhuSyTT/DA_LTW](https://github.com/PhuSyTT/DA_LTW)
2. Bạn sẽ thấy một thanh thông báo màu vàng xuất hiện trên đầu trang kèm nút màu xanh:
   👉 **`Compare & pull request`**
3. Bấm vào nút đó:
   * **Tiêu đề (Title):** Viết tên chức năng bạn vừa làm.
   * **Mô tả (Description):** Ghi chú ngắn về các file bạn đã thêm hoặc sửa.
4. Bấm nút **`Create pull request`**.
5. Nhắn tin báo cho **Trưởng nhóm (Người giữ repo)** vào xem và bấm **Merge** để gộp code của bạn vào nhánh `main`.

---

## 🛠️ CÁC TÌNH HUỐNG THƯỜNG GẶP & CÁCH XỬ LÝ

### 1. Muốn xem mình đang đứng ở nhánh nào?
```bash
git branch
```
*(Nhánh có dấu sao `*` màu xanh là nhánh bạn đang làm việc).*

---

### 2. Sau khi chức năng đã được duyệt vào `main`, muốn làm chức năng mới thì làm sao?
Bạn quay lại Bước 1:
```bash
git checkout main
git pull origin main
git checkout -b feature/<chức-năng-mới>
```

---

### 3. Nhỡ commit nhầm và muốn hủy commit gần nhất (giữ lại code)?
```bash
git reset --soft HEAD~1
```

---

### 4. Lưu ý về tên Remote trên máy:
Kiểm tra tên remote bằng lệnh: `git remote -v`.
* Nếu hiện chữ `origin` $\rightarrow$ gõ lệnh theo `origin` (chuẩn chung).
* Nếu máy bạn nào trước đó lỡ đặt là `orgin` (thiếu chữ `i`) $\rightarrow$ hãy đổi lại cho đúng chuẩn bằng lệnh:
  ```bash
  git remote rename orgin origin
  ```

---
*Chúc nhóm phối hợp làm việc hiệu quả và đạt kết quả cao trong đồ án!*
