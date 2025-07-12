package devnsko.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import devnsko.dto.PostBlockRequest;
import devnsko.dto.PostRequst;
import devnsko.model.PostBlockModel;
import devnsko.model.PostModel;
import devnsko.repository.PostRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    
    public Integer create(PostRequst post) {
        // Map PostRequst to PostModel
        PostModel postModel = new PostModel();
        // Set fields from post to postModel as appropriate
        // Example:
        postModel.setTitle(post.getTitle());
        List<PostBlockRequest> blocksRequest = post.getBlocks();
        List<PostBlockModel> blocks = new ArrayList<>();
        blocksRequest.forEach(blockRequest -> {
            PostBlockModel blockModel = new PostBlockModel();
            // Map fields from blockRequest to blockModel as needed
            try {
                blockModel.setData(
                    new ObjectMapper().writeValueAsString(blockRequest.getData())
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize block data", e);
            }
            blockModel.setType(blockRequest.getType());
            blockModel.setOrderIndex(blockRequest.getOrderIndex());
            blockModel.setPost(postModel);
            blocks.add(blockModel);
        });

        postModel.setBlocks(blocks);

        PostModel createdPost = postRepository.save(postModel);
        if (createdPost != null) {
            System.out.println(createdPost);
            return createdPost.getId();
        } else {
            return null;
        }
    }

    public PostModel getById(Integer id) {
        PostModel post = postRepository.findById(id).orElse(null);
        List<PostBlockModel> blocks = post.getBlocks();
        System.out.println(blocks.get(0).getData());
        return post;
    }

    public List<PostModel> getAll() {
        return postRepository.findAll();
    }
}
