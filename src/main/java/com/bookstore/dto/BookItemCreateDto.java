package com.bookstore.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BookItemCreateDto {

    private Long id;

    @NotNull(message = "Vui lòng chọn đầu sách gốc (Master Book)")
    private Long bookId;

    @NotNull(message = "Vui lòng chọn chi nhánh lưu kho")
    private Long branchId;

    @NotBlank(message = "Mã SKU / Barcode không được để trống")
    private String skuBarcode;

    @NotBlank(message = "Vui lòng chọn tình trạng sách (Grading)")
    private String conditionGrade;

    @NotNull(message = "Giá bán không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá bán phải lớn hơn 0")
    private BigDecimal sellingPrice;

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 1, message = "Số lượng tồn kho tối thiểu là 1")
    private Integer stockQuantity = 1;

    private String shelfLocation;

    private String conditionNote;

    private String status = "AVAILABLE";

    private List<MultipartFile> imageFiles = new ArrayList<>();

    private Integer primaryImageIndex = 0;

    public BookItemCreateDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public String getSkuBarcode() {
        return skuBarcode;
    }

    public void setSkuBarcode(String skuBarcode) {
        this.skuBarcode = skuBarcode;
    }

    public String getConditionGrade() {
        return conditionGrade;
    }

    public void setConditionGrade(String conditionGrade) {
        this.conditionGrade = conditionGrade;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getShelfLocation() {
        return shelfLocation;
    }

    public void setShelfLocation(String shelfLocation) {
        this.shelfLocation = shelfLocation;
    }

    public String getConditionNote() {
        return conditionNote;
    }

    public void setConditionNote(String conditionNote) {
        this.conditionNote = conditionNote;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<MultipartFile> getImageFiles() {
        return imageFiles;
    }

    public void setImageFiles(List<MultipartFile> imageFiles) {
        this.imageFiles = imageFiles;
    }

    public Integer getPrimaryImageIndex() {
        return primaryImageIndex;
    }

    public void setPrimaryImageIndex(Integer primaryImageIndex) {
        this.primaryImageIndex = primaryImageIndex;
    }
}
