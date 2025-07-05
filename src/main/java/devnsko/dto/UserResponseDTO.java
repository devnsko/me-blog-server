package devnsko.dto;

import java.util.List;

import devnsko.model.AppUserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserResponseDTO {

  @Schema(description = "user id")
  private Integer id;
  @Schema(description = "user name")
  private String username;
  @Schema(description = "user email")
  private String email;
  @Schema(description = "user roles")
  List<AppUserRole> appUserRoles;

}
