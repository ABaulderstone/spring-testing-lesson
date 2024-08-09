package io.nology.blog.category;

import jakarta.validation.constraints.NotBlank;

public class CreateCategoryDTO {
    @NotBlank
    private String name;

    public CreateCategoryDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String string) {
        this.name = string;
    }

}
