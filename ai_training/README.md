# 🌿 Hướng Dẫn Huấn Luyện (Fine-Tuning) AI Cho Book4Life

Thư mục này chứa đầy đủ tài nguyên cần thiết để bạn huấn luyện một mô hình AI riêng biệt dành cho hệ thống **Book4Life**:
* [`book4life_cskh_dataset.jsonl`](file:///d:/DA_LTW/ai_training/book4life_cskh_dataset.jsonl): Tập dữ liệu mẫu chuẩn chất lượng cao về nghiệp vụ nhà sách (đổi sách, tra cứu đơn hàng, chi nhánh, phân loại sách, tác giả văn học).
* [`train_unsloth_book4life.ipynb`](file:///d:/DA_LTW/ai_training/train_unsloth_book4life.ipynb): File Google Colab Notebook sẵn sàng chạy huấn luyện mô hình mở (Qwen 2.5 / Llama 3.2).

---

## Cách 1: Fine-Tuning Bằng Google AI Studio (Nhanh nhất - 3 phút - Không cần code)

Vì bạn đã có tài khoản và API Key tại Google AI Studio, đây là cách nhanh và mạnh mẽ nhất:

1. Truy cập: [Google AI Studio - Model Tuning](https://aistudio.google.com/app/tuned_models)
2. Nhấn nút **"Create Tuned Model"** (Tạo mô hình tinh chỉnh).
3. Tại ô **Select base model**, chọn: `gemini-1.5-flash` hoặc `gemini-2.5-flash`.
4. Tại ô **Training data**, nhấn **"Upload data"** và chọn file:
   `d:\DA_LTW\ai_training\book4life_cskh_dataset.jsonl`
5. Đặt tên cho mô hình (ví dụ: `book4life-cskh-v1`).
6. Nhấn nút **"Tune Model"**. Google sẽ tự động chạy huấn luyện trên siêu máy tính của họ trong khoảng 3 - 5 phút (hoàn toàn miễn phí).
7. Sau khi hoàn thành, bạn copy **Model ID** (dạng `tunedModels/book4life-cskh-v1-xxxxxx`).
8. Mở file `.env` của dự án và cập nhật:
   ```properties
   AI_MODE=api
   GEMINI_MODEL=tunedModels/book4life-cskh-v1-xxxxxx
   ```
👉 Toàn bộ website của bạn sẽ ngay lập tức được vận hành bởi chính con AI do bạn tự huấn luyện!

---

## Cách 2: Huấn Luyện Bằng Google Colab & Xuất File GGUF Chạy Offline (Ollama)

Nếu bạn muốn sở hữu 100% file mô hình để chạy offline trên máy tính không cần internet:

1. Mở [Google Colab](https://colab.research.google.com/).
2. Nhấn **File** -> **Upload notebook** -> Chọn file:
   `d:\DA_LTW\ai_training\train_unsloth_book4life.ipynb`
3. Trong Colab, chọn menu **Runtime** -> **Change runtime type** -> Chọn **T4 GPU** (miễn phí).
4. Nhấn biểu tượng thư mục ở thanh bên trái và tải file `book4life_cskh_dataset.jsonl` lên.
5. Chạy tuần tự các ô lệnh trong Notebook.
6. Sau khoảng 10 phút, tải file `book4life_model-unsloth.Q4_K_M.gguf` về máy tính.
7. Chạy offline trên máy tính thông qua công cụ [Ollama](https://ollama.com/):
   ```bash
   ollama create book4life -f Modelfile
   ollama run book4life
   ```

