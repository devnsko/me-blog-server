package devnsko.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import devnsko.dto.PostRequst;
import devnsko.model.PostModel;
import devnsko.response.ServerResponse;
import devnsko.service.PostService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/posts")
@Tag(name = "posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<ServerResponse> getAllPosts() {
        List<PostModel> posts = postService.getAll();
        return ResponseEntity.ok(ServerResponse.ok(Map.of("posts", posts)));
    }
    

    @GetMapping("/{id}")
    public ResponseEntity<ServerResponse> getPostById(@PathVariable Integer id) {
        PostModel post = postService.getById(id);
        return ResponseEntity.ok(ServerResponse.ok(Map.of("post", post)));
    }
    
    @PostMapping
    public ResponseEntity<ServerResponse> create(@RequestBody PostRequst req) {
        System.out.println(req);
        System.out.println(req.getBlocks());
        // Convert PostRequst to PostModel using a constructor or mapping method
        Integer newPostId = postService.create(req);
        return ResponseEntity.ok(ServerResponse.ok(Map.of("id", newPostId)));
    }

    
}
