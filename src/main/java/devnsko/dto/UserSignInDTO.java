package devnsko.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserSignInDTO {

  @Schema(description = "Username")
  private String username;
  @Schema(description = "Password")
  private String password;
}
