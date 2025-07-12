package devnsko.dto;

import java.util.Map;

import devnsko.type.PostBlockType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class PostBlockRequest {
    private PostBlockType type;
    private int orderIndex;
    private Map<String, Object> data;
}