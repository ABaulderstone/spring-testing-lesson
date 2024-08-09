package io.nology.blog.blogpost;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.nology.blog.exceptions.NotFoundException;
import io.nology.blog.exceptions.BadRequestException;
import io.nology.blog.exceptions.ServiceValidationException;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;





@RestController
@RequestMapping("/posts")
public class BlogPostController {
    @Autowired
    private BlogPostService blogPostService;

    @PostMapping()
    public ResponseEntity<BlogPost> createPost(@Valid @RequestBody CreateBlogPostDTO data) throws BadRequestException {
        BlogPost createdPost;
        try {
            createdPost = this.blogPostService.createPost(data);
            return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
        } catch (ServiceValidationException e) {
            e.printStackTrace();
            throw new BadRequestException(e.generateMessage());
        }
        
    }

    @GetMapping()
    public ResponseEntity<List<BlogPost>> findAllPosts() {
        List<BlogPost> allPosts =  this.blogPostService.findAllPosts();
        return new ResponseEntity<>(allPosts, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BlogPost> findPostById(@PathVariable Long id) throws NotFoundException {
        Optional<BlogPost> maybePost = this.blogPostService.findById(id);
        BlogPost foundPost = maybePost.orElseThrow(() -> new NotFoundException(BlogPost.class, id));
        return new ResponseEntity<>(foundPost, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BlogPost> updatePostById(@PathVariable Long id,  @Valid @RequestBody UpdateBlogPostDTO data) throws NotFoundException {
        Optional<BlogPost> maybePost = this.blogPostService.updateById(id, data);
        BlogPost updatedPost = maybePost.orElseThrow(() -> new NotFoundException(BlogPost.class, id));
        return new ResponseEntity<>(updatedPost, HttpStatus.OK);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePostById(@PathVariable Long id) throws NotFoundException {
        boolean isDeleted = this.blogPostService.deleteById(id);
        if(!isDeleted) {
            throw new NotFoundException(BlogPost.class, id);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
}
