package io.nology.blog.blogpost;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class UpdateBlogPostDTO {
   
    @Pattern(regexp = ".*\\S.*", message = "Content cannot be empty")
    private String content;
    @Pattern(regexp = ".*\\S.*", message = "Title cannot be empty")
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
