package com.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class CategoryDto {

    private Long id;

    @NotBlank(message = "Tên thể loại không được để trống")
    @Size(max = 100, message = "Tên thể loại tối đa 100 ký tự")
    private String name;

    private Long parentId;

    private String parentName;

    private Long bookCount;

    private List<CategoryDto> subCategories = new ArrayList<>();

    public CategoryDto() {
    }

    public CategoryDto(Long id, String name, Long parentId, String parentName, Long bookCount, List<CategoryDto> subCategories) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.parentName = parentName;
        this.bookCount = bookCount;
        this.subCategories = (subCategories != null) ? subCategories : new ArrayList<>();
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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public Long getBookCount() {
        return bookCount;
    }

    public void setBookCount(Long bookCount) {
        this.bookCount = bookCount;
    }

    public List<CategoryDto> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<CategoryDto> subCategories) {
        this.subCategories = subCategories;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String name;
        private Long parentId;
        private String parentName;
        private Long bookCount;
        private List<CategoryDto> subCategories = new ArrayList<>();

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder parentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder parentName(String parentName) {
            this.parentName = parentName;
            return this;
        }

        public Builder bookCount(Long bookCount) {
            this.bookCount = bookCount;
            return this;
        }

        public Builder subCategories(List<CategoryDto> subCategories) {
            this.subCategories = subCategories;
            return this;
        }

        public CategoryDto build() {
            return new CategoryDto(id, name, parentId, parentName, bookCount, subCategories);
        }
    }
}
