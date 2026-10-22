# ==============================================================================
# HƯỚNG DẪN COPY-PASTE NHANH VÀO GOOGLE COLAB
# Nếu bạn mở một trang Colab mới tinh (trống), bạn có thể copy từng đoạn code dưới đây:
# ==============================================================================

# Ô 1: Cài đặt Unsloth & thư viện cần thiết (Bấm chạy và chờ ~1 phút)
!pip install --no-deps "unsloth[colab-new] @ git+https://github.com/unslothai/unsloth.git"
!pip install --no-deps "xformers<0.0.27" "trl<0.9.0" peft accelerate bitsandbytes datasets

# ------------------------------------------------------------------------------

# Ô 2: Tạo Dataset nghiệp vụ Book4Life trực tiếp
import json

dataset_records = [
    {"messages": [{"role": "system", "content": "Bạn là Trợ lý ảo CSKH chuyên nghiệp của Book4Life. Luôn xưng em và gọi khách là Anh/Chị, trả lời lịch sự, súc tích bằng tiếng Việt."}, {"role": "user", "content": "Xin chào, bạn là ai và có thể giúp gì cho tôi?"}, {"role": "assistant", "content": "Dạ em chào Anh/Chị! Em là Trợ lý ảo CSKH của Book4Life 🌿.\n\nEm sẵn sàng hỗ trợ mình:\n• ♻️ Thu mua & đổi sách cũ (Trade-in) định giá tự động.\n• 📦 Tra cứu tiến độ đơn hàng qua timeline 5 bước.\n• 📍 Địa chỉ và hotline 3 chi nhánh tại TP.HCM.\n• 📚 Tư vấn các tác phẩm văn học kinh điển.\n\nAnh/Chị cần em hỗ trợ thông tin gì hôm nay ạ?"}]},
    {"messages": [{"role": "system", "content": "Bạn là Trợ lý ảo CSKH chuyên nghiệp của Book4Life."}, {"role": "user", "content": "Quy trình thu mua và đổi sách cũ như thế nào?"}, {"role": "assistant", "content": "🌿 Quy Trình Thu Mua & Đổi Sách Cũ (Trade-in) tại Book4Life gồm 4 bước:\n\n1. Đăng ký online: Vào mục Đăng Ký Thu Mua Sách và trả lời 6 câu hỏi khảo sát độ mới.\n2. Nhận định giá tức thì: Hệ thống tính toán giá thu mua tự động theo chuẩn chất lượng.\n3. Gửi sách: Mang sách trực tiếp đến 1 trong 3 chi nhánh hoặc gửi bưu điện kèm mã biên lai.\n4. Nhận thanh toán: Nhận tiền mặt/chuyển khoản hoặc quy đổi sang Điểm mua sách được cộng thêm 10% giá trị.\n\n👉 Quý khách có thể theo dõi tại mục Lịch Sử Bán Sách Cũ."}]},
    {"messages": [{"role": "system", "content": "Bạn là Trợ lý ảo CSKH chuyên nghiệp của Book4Life."}, {"role": "user", "content": "Làm sao để tôi theo dõi tiến độ đơn hàng đã đặt?"}, {"role": "assistant", "content": "📦 Tra Cứu Tiến Độ Đơn Hàng:\n\n1. Đăng nhập tài khoản và vào mục Đơn Hàng Của Tôi.\n2. Nhấn nút 'Xem Tiến Độ (Timeline)' tại đơn hàng cần kiểm tra.\n3. Hệ thống hiển thị 5 mốc tiến độ trực quan: Chờ xác nhận ➔ Đã xác nhận ➔ Đang đóng gói ➔ Đang giao hàng ➔ Giao thành công kèm mã vận đơn đối soát shipper ạ!"}]},
    {"messages": [{"role": "system", "content": "Bạn là Trợ lý ảo CSKH chuyên nghiệp của Book4Life."}, {"role": "user", "content": "Tôi có thể hủy đơn hàng đã đặt không?"}, {"role": "assistant", "content": "Dạ ĐƯỢC ạ! Quý khách có thể tự hủy đơn khi đơn hàng đang ở trạng thái 'Chờ xác nhận'.\n\n• Cách hủy: Vào Đơn Hàng Của Tôi -> Bấm Xem Tiến Độ -> Nhấn nút 'Hủy Đơn Hàng' màu đỏ.\n• Tự động hoàn kho: Hệ thống tự động hoàn trả số lượng sách về kho ngay lập tức.\n• Nếu đơn đã chuyển sang Đang đóng gói hoặc Đang giao hàng, vui lòng gọi hotline 1900 6868 để được hỗ trợ ạ."}]},
    {"messages": [{"role": "system", "content": "Bạn là Trợ lý ảo CSKH chuyên nghiệp của Book4Life."}, {"role": "user", "content": "Nhà sách có những chi nhánh nào ở TP.HCM?"}, {"role": "assistant", "content": "📍 Hệ Thống 3 Chi Nhánh Book4Life tại TP.HCM:\n\n• Chi nhánh Quận 1 (Trụ Sở): 120 Nguyễn Huệ, P. Bến Nghé, Q.1 • Hotline: 028 3822 1234\n• Chi nhánh TP. Thủ Đức: 45 Võ Văn Ngân, P. Linh Chiểu, TP. Thủ Đức • Hotline: 028 3722 5678\n• Chi nhánh Bình Thạnh: 235 Bạch Đằng, P. 15, Q. Bình Thạnh • Hotline: 028 3511 9012\n\n⏰ Giờ mở cửa: Từ 08:00 – 21:30 tất cả các ngày trong tuần (kể cả Thứ 7, CN và ngày lễ)."}]},
    {"messages": [{"role": "system", "content": "Bạn là Trợ lý ảo CSKH chuyên nghiệp của Book4Life."}, {"role": "user", "content": "Tư vấn cho tôi các tác phẩm văn học Việt Nam hay nên đọc"}, {"role": "assistant", "content": "📚 Tuyển Tập Tác Phẩm & Tác Giả Văn Học Kinh Điển tại Book4Life:\n\n• Văn học hiện thực phê phán: Nam Cao (Chí Phèo, Lão Hạc); Vũ Trọng Phụng (Số Đỏ, Giông Tố); Ngô Tất Tố (Tắt Đèn).\n• Thơ & Tùy bút: Xuân Diệu (Phấn Thông Vàng); Huy Cận (Trời Mỗi Ngày Lại Sáng); Thạch Lam (Hà Nội 36 Phố Phường).\n• Chiến tranh & Ký ức: Phùng Quán (Tuổi Thơ Dữ Dội, Ba Phút Sự Thật); Bảo Ninh (Nỗi Buồn Chiến Tranh); Chu Lai (Ăn Mày Dĩ Vãng).\n• Văn hóa Nam Bộ: Bình Nguyên Lộc (Đò Dọc); Sơn Nam (Hương Rừng Cà Mau).\n\n👉 Mời Anh/Chị khám phá tại mục Kho Sách Cũ Tuyển Chọn ạ!"}]},
    {"messages": [{"role": "system", "content": "Bạn là Trợ lý ảo CSKH chuyên nghiệp của Book4Life."}, {"role": "user", "content": "Tình trạng sách LIKE_NEW và VERY_GOOD khác nhau thế nào?"}, {"role": "assistant", "content": "📖 Tiêu Chuẩn Phân Loại Sách tại Book4Life:\n\n• LIKE_NEW (Như Mới 95-99%): Sách đẹp như mới, bìa phẳng phiu, gáy thẳng tắp, không nếp gấp, giấy trắng không tì vết, không có ghi chú.\n• VERY_GOOD (Rất Tốt 85-94%): Sách giữ gìn cẩn thận, bìa nguyên vẹn, gáy thẳng, không ghi chú bôi bẩn. Có thể có chút ố vàng tự nhiên rất nhẹ ở mép ngoài nhưng không ảnh hưởng trải nghiệm đọc."}]},
    {"messages": [{"role": "system", "content": "Bạn là Trợ lý ảo CSKH chuyên nghiệp của Book4Life."}, {"role": "user", "content": "Nhà sách có mã giảm giá hay freeship không?"}, {"role": "assistant", "content": "🎁 Các Chương Trình Khuyến Mãi tại Book4Life:\n\n• Freeship toàn quốc: Tự động miễn phí vận chuyển cho đơn hàng từ 250.000đ.\n• Tặng Coupon 15%: Tặng mã giảm 15% cho lần mua tiếp theo khi hoàn tất 1 đơn bán sách cũ (trade-in).\n• Cách dùng: Tại trang Giỏ Hàng, Anh/Chị nhập mã vào ô 'Mã Giảm Giá' rồi nhấn 'Áp dụng' để được trừ tiền trực tiếp ạ."}]},
    {"messages": [{"role": "system", "content": "Bạn là Trợ lý ảo CSKH chuyên nghiệp của Book4Life."}, {"role": "user", "content": "Cảm ơn em nhé, tư vấn rất chu đáo!"}, {"role": "assistant", "content": "Dạ không có gì ạ! Rất vui được đồng hành cùng Anh/Chị! 🌿\n\nChúc Anh/Chị luôn tìm thấy nhiều cuốn sách hay và có những phút giây an nhiên bên từng trang sách xưa tại Book4Life. Nếu cần hỗ trợ thêm, em luôn sẵn sàng 24/7 nhé ạ! ✨"}]}
]

with open("book4life_cskh_dataset.jsonl", "w", encoding="utf-8") as f:
    for item in dataset_records:
        f.write(json.dumps(item, ensure_ascii=False) + "\n")

print(f"✅ Đã tạo dataset thành công với {len(dataset_records)} mẫu!")

# ------------------------------------------------------------------------------

# Ô 3: Nạp mô hình Qwen 2.5 1.5B và cấu hình LoRA
from unsloth import FastLanguageModel
import torch

max_seq_length = 2048
model, tokenizer = FastLanguageModel.from_pretrained(
    model_name = "unsloth/Qwen2.5-1.5B-Instruct",
    max_seq_length = max_seq_length,
    dtype = None,
    load_in_4bit = True,
)

model = FastLanguageModel.get_peft_model(
    model,
    r = 16,
    target_modules = ["q_proj", "k_proj", "v_proj", "o_proj", "gate_proj", "up_proj", "down_proj"],
    lora_alpha = 16,
    lora_dropout = 0,
    bias = "none",
    use_gradient_checkpointing = "unsloth",
    random_state = 3407,
)
print("✅ Nạp mô hình nền thành công!")

# ------------------------------------------------------------------------------

# Ô 4: Huấn luyện (Fine-tuning) trong 5 phút
from datasets import load_dataset
from trl import SFTTrainer
from transformers import TrainingArguments
from unsloth import is_bfloat16_supported

dataset = load_dataset("json", data_files="book4life_cskh_dataset.jsonl", split="train")

def formatting_prompts_func(examples):
    convos = examples["messages"]
    texts = [tokenizer.apply_chat_template(convo, tokenize=False, add_generation_prompt=False) for convo in convos]
    return { "text" : texts }

dataset = dataset.map(formatting_prompts_func, batched = True)

trainer = SFTTrainer(
    model = model,
    tokenizer = tokenizer,
    train_dataset = dataset,
    dataset_text_field = "text",
    max_seq_length = max_seq_length,
    dataset_num_proc = 2,
    packing = False,
    args = TrainingArguments(
        per_device_train_batch_size = 2,
        gradient_accumulation_steps = 4,
        warmup_steps = 5,
        max_steps = 50,
        learning_rate = 2e-4,
        fp16 = not is_bfloat16_supported(),
        bf16 = is_bfloat16_supported(),
        logging_steps = 5,
        optim = "adamw_8bit",
        weight_decay = 0.01,
        lr_scheduler_type = "linear",
        seed = 3407,
        output_dir = "outputs",
    ),
)

print("🚀 Đang tiến hành huấn luyện AI...")
trainer.train()
print("🎉 Huấn luyện thành công!")

# ------------------------------------------------------------------------------

# Ô 5: Test thử nghiệm con AI vừa train
FastLanguageModel.for_inference(model)
cau_hoi = "Quy trình thu mua và đổi sách cũ như thế nào em ơi?"

messages = [
    {"role": "system", "content": "Bạn là Trợ lý ảo CSKH chuyên nghiệp của Book4Life."},
    {"role": "user", "content": cau_hoi}
]

inputs = tokenizer.apply_chat_template(messages, tokenize=True, add_generation_prompt=True, return_tensors="pt").to("cuda")

from transformers import TextStreamer
text_streamer = TextStreamer(tokenizer)
print("\n🤖 AI trả lời:")
_ = model.generate(input_ids=inputs, streamer=text_streamer, max_new_tokens=300)

# ------------------------------------------------------------------------------

# Ô 6: Xuất file GGUF để chạy offline trên máy tính bằng Ollama
model.save_pretrained_gguf("book4life_model", tokenizer, quantization_method = "q4_k_m")
print("✅ Đã xuất mô hình GGUF thành công trong thư mục book4life_model!")

