package com.oneassist.ccf.contract;

public class ProductCategoryDTO {

    private Long id;
    private String categoryName;
    private Object configuration;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Object getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Object configuration) {
        this.configuration = configuration;
    }

    public ProductCategoryDTO() {
    }
}
