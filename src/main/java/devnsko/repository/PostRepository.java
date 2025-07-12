package devnsko.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import devnsko.model.PostModel;

@Repository
public interface PostRepository extends JpaRepository<PostModel, Integer> {
    
}
