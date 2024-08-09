package io.nology.blog.blogpost;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateBlogPostDTO {
    @NotBlank
    private String content;
    @NotBlank
    private String title;
    
    @NotNull
    @Min(1)
    private Long categoryId;
    
    public String getContent() {
        return content;
    }
    public String getTitle() {
        return title;
    }
    public Long getCategoryId() {
        return categoryId;
    }
 
    
}
