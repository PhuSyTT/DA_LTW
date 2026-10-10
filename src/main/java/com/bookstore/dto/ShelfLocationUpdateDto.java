package com.bookstore.dto;

import jakarta.validation.constraints.NotBlank;

public class ShelfLocationUpdateDto {

    @NotBlank(message = "Vị trí kệ sách không được để trống")
    private String shelfLocation;

    public ShelfLocationUpdateDto() {
    }

    public ShelfLocationUpdateDto(String shelfLocation) {
        this.shelfLocation = shelfLocation;
    }

    public String getShelfLocation() {
        return shelfLocation;
    }

    public void setShelfLocation(String shelfLocation) {
        this.shelfLocation = shelfLocation;
    }
}
