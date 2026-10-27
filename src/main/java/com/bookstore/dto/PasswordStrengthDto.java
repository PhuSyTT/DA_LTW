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
        boolean hasMinLength = length > 6; // Yêu cầu: Mật khẩu > 6 ký tự (từ 7 ký tự trở lên)
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLetter = hasLower || hasUpper; // có ký tự chữ
        boolean hasDigit = password.matches(".*[0-9].*"); // có chữ số
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*"); // có ký tự đặc biệt

        dto.setHasMinLength(hasMinLength);
        dto.setHasLowerCase(hasLower);
        dto.setHasUpperCase(hasUpper);
        dto.setHasDigit(hasDigit);
        dto.setHasSpecialChar(hasSpecial);

        // Đạt chuẩn nếu thỏa mãn đồng thời: độ dài > 6, có chữ cái, có số, và có ký tự đặc biệt
        boolean meetsAllRequired = hasMinLength && hasLetter && hasDigit && hasSpecial;
        dto.setAcceptable(meetsAllRequired);

        int criteriaCount = (hasLetter ? 1 : 0) + (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);

        int calculatedScore;
        if (length <= 6) {
            calculatedScore = 0; // Quá ngắn (<= 6 ký tự)
        } else if (!meetsAllRequired) {
            calculatedScore = (criteriaCount >= 2) ? 2 : 1;
        } else {
            // Đã đủ > 6 ký tự, có chữ, có số, có ký tự đặc biệt
            if (length >= 8 && hasLower && hasUpper) {
                calculatedScore = 4; // Rất an toàn (>= 8 ký tự, có đủ chữ thường, hoa, số, đặc biệt)
            } else {
                calculatedScore = 3; // Khá mạnh (thỏa mãn trọn vẹn yêu cầu > 6 ký tự, có chữ, số, ký tự đặc biệt)
            }
        }

        dto.setScore(calculatedScore);

        switch (calculatedScore) {
            case 0:
                dto.setLevel("VERY_WEAK");
                dto.setLabel("Rất yếu (Phải > 6 ký tự)");
                dto.setColor("#DC3545"); // Đỏ
                dto.setPercentage(15);
                break;
            case 1:
                dto.setLevel("WEAK");
                dto.setLabel("Yếu (Thiếu số hoặc ký tự đặc biệt)");
                dto.setColor("#FD7E14"); // Cam
                dto.setPercentage(35);
                break;
            case 2:
                dto.setLevel("MEDIUM");
                dto.setLabel("Trung bình (Cần thêm ký tự đặc biệt)");
                dto.setColor("#FFC107"); // Vàng cam
                dto.setPercentage(65);
                break;
            case 3:
                dto.setLevel("STRONG");
                dto.setLabel("Khá mạnh (Đã đạt chuẩn an toàn)");
                dto.setColor("#0D6EFD"); // Xanh dương
                dto.setPercentage(85);
                break;
            case 4:
            default:
                dto.setLevel("VERY_STRONG");
                dto.setLabel("Rất an toàn (Đạt chuẩn tối đa)");
                dto.setColor("#198754"); // Xanh lá
                dto.setPercentage(100);
                break;
        }

        // Tạo danh sách gợi ý cải thiện theo đúng yêu cầu
        if (!hasMinLength) {
            dto.getSuggestions().add("Độ dài mật khẩu phải lớn hơn 6 ký tự (từ 7 ký tự trở lên)");
        }
        if (!hasLetter) {
            dto.getSuggestions().add("Mật khẩu phải chứa ít nhất 1 chữ cái (a-z, A-Z)");
        }
        if (!hasDigit) {
            dto.getSuggestions().add("Mật khẩu phải chứa ít nhất 1 chữ số (0-9)");
        }
        if (!hasSpecial) {
            dto.getSuggestions().add("Mật khẩu phải chứa ít nhất 1 ký tự đặc biệt (!@#$%...)");
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

