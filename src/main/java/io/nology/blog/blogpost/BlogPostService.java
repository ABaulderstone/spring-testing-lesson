package io.nology.blog.blogpost;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.nology.blog.category.Category;
import io.nology.blog.category.CategoryService;
import io.nology.blog.exceptions.ServiceValidationException;
import io.nology.blog.exceptions.ValidationErrors;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class BlogPostService {
    @Autowired
    private ModelMapper mapper;

    @Autowired
    private BlogPostRepository repo;

    @Autowired
    private CategoryService categoryService;

    public BlogPost createPost(CreateBlogPostDTO data) throws ServiceValidationException {
        // BlogPost newPost = new BlogPost();
        // newPost.setTitle(data.getTitle().trim());
        // newPost.setContent(data.getContent().trim());
        // newPost.setCategory(data.getCategory().trim().toLowerCase());
        BlogPost newPost = mapper.map(data, BlogPost.class);
        ValidationErrors errors = new ValidationErrors();
        Long categoryId = data.getCategoryId();
        Optional<Category> maybeCategory = this.categoryService.findById(categoryId);
        if (maybeCategory.isEmpty()) {
            errors.addError("category", String.format("Category with id %s does not exist", categoryId));
        } else {
            newPost.setCategory(maybeCategory.get());
        }
        if (errors.hasErrors()) {
            throw new ServiceValidationException(errors);
        }
        return this.repo.save(newPost);
    }

    public List<BlogPost> findAllPosts() {
        return this.repo.findAll();
    }

    public Optional<BlogPost> findById(Long id) {
        return this.repo.findById(id);
    }

    public boolean deleteById(Long id) {
        Optional<BlogPost> maybePost = this.findById(id);
        if (maybePost.isEmpty()) {
            return false;
        }
        this.repo.delete(maybePost.get());
        return true;
    }

    public Optional<BlogPost> updateById(Long id, UpdateBlogPostDTO data) {
        Optional<BlogPost> maybePost = this.findById(id);
        if (maybePost.isEmpty()) {
            return maybePost;
        }

        BlogPost foundPost = maybePost.get();
        // String newTitle = data.getTitle();

        // if(newTitle != null) {
        // foundPost.setTitle(newTitle.trim());
        // }
        // if(data.getCategory() != null) {
        // foundPost.setCategory(data.getCategory().trim().toLowerCase());
        // }
        // if(data.getContent() != null) {
        // foundPost.setContent(data.getContent().trim());
        // }
        mapper.map(data, foundPost);
        BlogPost updatedPost = this.repo.save(foundPost);
        return Optional.of(updatedPost);
    }

}
