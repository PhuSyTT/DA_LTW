package com.bookstore.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PasswordStrengthDto implements Serializable {

    private int score; // 0 to 4
    private String level; // VERY_WEAK, WEAK, MEDIUM, STRONG, VERY_STRONG
    private String label; // Rất yếu, Yếu, Trung bình, Khá mạnh, Rất an toàn
    private String color; // #DC3545 (Đỏ), #FD7E14 (Cam), #FFC107 (Vàng), #20C997 (Xanh mòng két), #198754 (Xanh lá)
    private int percentage; // 0%, 25%, 50%, 75%, 100%
    private boolean hasMinLength; // >= 8 ký tự
    private boolean hasLowerCase; // có a-z
    private boolean hasUpperCase; // có A-Z
    private boolean hasDigit; // có 0-9
    private boolean hasSpecialChar; // có ký tự đặc biệt
    private boolean isAcceptable; // true nếu thỏa mãn tối thiểu để đăng ký an toàn
    private List<String> suggestions = new ArrayList<>();

    public PasswordStrengthDto() {
    }

    public static PasswordStrengthDto evaluate(String password) {
        PasswordStrengthDto dto = new PasswordStrengthDto();

        if (password == null || password.isEmpty()) {
            dto.setScore(0);
            dto.setLevel("VERY_WEAK");
            dto.setLabel("Chưa nhập mật khẩu");
            dto.setColor("#6C757D");
            dto.setPercentage(0);
            dto.setHasMinLength(false);
            dto.setHasLowerCase(false);
            dto.setHasUpperCase(false);
            dto.setHasDigit(false);
            dto.setHasSpecialChar(false);
            dto.setAcceptable(false);
            dto.getSuggestions().add("Vui lòng nhập mật khẩu tối thiểu 8 ký tự");
            return dto;
        }

        int length = password.length();
        boolean hasMinLength = length >= 8;
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        dto.setHasMinLength(hasMinLength);
        dto.setHasLowerCase(hasLower);
        dto.setHasUpperCase(hasUpper);
        dto.setHasDigit(hasDigit);
        dto.setHasSpecialChar(hasSpecial);

        // Tính điểm độ mạnh từ 0 đến 4
        int criteriaMet = 0;
        if (hasLower) criteriaMet++;
        if (hasUpper) criteriaMet++;
        if (hasDigit) criteriaMet++;
        if (hasSpecial) criteriaMet++;

        int calculatedScore;
        if (length < 6) {
            calculatedScore = 0; // Quá ngắn
        } else if (length < 8) {
            calculatedScore = (criteriaMet >= 2) ? 1 : 0;
        } else {
            // length >= 8
            if (criteriaMet <= 1) {
                calculatedScore = 1;
            } else if (criteriaMet == 2) {
                calculatedScore = 2;
            } else if (criteriaMet == 3) {
                calculatedScore = 3;
            } else {
                calculatedScore = 4;
            }
        }

        dto.setScore(calculatedScore);

        switch (calculatedScore) {
            case 0:
                dto.setLevel("VERY_WEAK");
                dto.setLabel("Rất yếu");
                dto.setColor("#DC3545"); // Đỏ
                dto.setPercentage(15);
                dto.setAcceptable(false);
                break;
            case 1:
                dto.setLevel("WEAK");
                dto.setLabel("Yếu");
                dto.setColor("#FD7E14"); // Cam
                dto.setPercentage(35);
                dto.setAcceptable(length >= 6 && (hasLower || hasUpper) && hasDigit);
                break;
            case 2:
                dto.setLevel("MEDIUM");
                dto.setLabel("Trung bình");
                dto.setColor("#FFC107"); // Vàng cam
                dto.setPercentage(60);
                dto.setAcceptable(true);
                break;
            case 3:
                dto.setLevel("STRONG");
                dto.setLabel("Khá mạnh");
                dto.setColor("#0D6EFD"); // Xanh dương
                dto.setPercentage(85);
                dto.setAcceptable(true);
                break;
            case 4:
            default:
                dto.setLevel("VERY_STRONG");
                dto.setLabel("Rất an toàn");
                dto.setColor("#198754"); // Xanh lá
                dto.setPercentage(100);
                dto.setAcceptable(true);
                break;
        }

        // Tạo danh sách gợi ý cải thiện
        if (!hasMinLength) {
            dto.getSuggestions().add("Nên có độ dài từ 8 ký tự trở lên");
        }
        if (!hasLower || !hasUpper) {
            dto.getSuggestions().add("Nên kết hợp cả chữ thường (a-z) và chữ in hoa (A-Z)");
        }
        if (!hasDigit) {
            dto.getSuggestions().add("Nên bổ sung thêm ít nhất 1 chữ số (0-9)");
        }
        if (!hasSpecial) {
            dto.getSuggestions().add("Nên thêm ký tự đặc biệt như @, #, $, %, ! để đạt độ an toàn tối đa");
        }

        return dto;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public boolean isHasMinLength() {
        return hasMinLength;
    }

    public void setHasMinLength(boolean hasMinLength) {
        this.hasMinLength = hasMinLength;
    }

    public boolean isHasLowerCase() {
        return hasLowerCase;
    }

    public void setHasLowerCase(boolean hasLowerCase) {
        this.hasLowerCase = hasLowerCase;
    }

    public boolean isHasUpperCase() {
        return hasUpperCase;
    }

    public void setHasUpperCase(boolean hasUpperCase) {
        this.hasUpperCase = hasUpperCase;
    }

    public boolean isHasDigit() {
        return hasDigit;
    }

    public void setHasDigit(boolean hasDigit) {
        this.hasDigit = hasDigit;
    }

    public boolean isHasSpecialChar() {
        return hasSpecialChar;
    }

    public void setHasSpecialChar(boolean hasSpecialChar) {
        this.hasSpecialChar = hasSpecialChar;
    }

    public boolean isAcceptable() {
        return isAcceptable;
    }

    public void setAcceptable(boolean acceptable) {
        isAcceptable = acceptable;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
}
