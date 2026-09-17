package com.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthorDto {

    private Long id;

    @NotBlank(message = "Tên tác giả không được để trống")
    @Size(max = 150, message = "Tên tác giả tối đa 150 ký tự")
    private String name;

    private String biography;

    private Long bookCount;

    public AuthorDto() {
    }

    public AuthorDto(Long id, String name, String biography, Long bookCount) {
        this.id = id;
        this.name = name;
        this.biography = biography;
        this.bookCount = bookCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public Long getBookCount() {
        return bookCount;
    }

    public void setBookCount(Long bookCount) {
        this.bookCount = bookCount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String name;
        private String biography;
        private Long bookCount;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder biography(String biography) {
            this.biography = biography;
            return this;
        }

        public Builder bookCount(Long bookCount) {
            this.bookCount = bookCount;
            return this;
        }

        public AuthorDto build() {
            return new AuthorDto(id, name, biography, bookCount);
        }
    }
}
