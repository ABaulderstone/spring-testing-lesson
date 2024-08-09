package io.nology.blog.blogpost;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.nology.blog.category.Category;
import io.nology.blog.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "blog_posts")
public class BlogPost extends BaseEntity {

    
    @Column(columnDefinition = "LONGTEXT")
    private String content;
    @Column
    private String title;

   @ManyToOne(cascade = CascadeType.ALL)
   @JoinColumn(name = "category_id")
   @JsonIgnoreProperties("posts")
   private Category category;

    BlogPost() {}

    public void setContent(String content) {
        this.content = content;
    }

    public void setTitle(String title) {
        this.title = title;
    }

  

    public void setCategory(Category category) {
        this.category = category;
    }

    

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

 

    public Category getCategory() {
        return category;
    }

    

}
