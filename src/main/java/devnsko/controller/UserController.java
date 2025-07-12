package devnsko.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import devnsko.dto.UserDataDTO;
import devnsko.dto.UserResponseDTO;
import devnsko.dto.UserSignInDTO;
import devnsko.model.AppUser;
import devnsko.response.ServerResponse;
import devnsko.service.UserService;

@RestController
@RequestMapping("/users")
@Tag(name = "users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final ModelMapper modelMapper;

  @PostMapping("/signin")
  @Operation(summary = "${UserController.signin}")
  @ApiResponses(value = {
         @ApiResponse(responseCode = "400", description = "Something went wrong"),
         @ApiResponse(responseCode = "422", description = "Invalid username/password supplied")
  })
  public ResponseEntity<ServerResponse> login(@RequestBody UserSignInDTO user) {
    try {
      String token = userService.signin(user.getUsername(), user.getPassword());
      if (token != null && token.length() > 0){
        return ResponseEntity.ok(ServerResponse.ok(token));
      } else 
        return ResponseEntity.badRequest().body(ServerResponse.error("Something went wrong"));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(ServerResponse.error("Something went wrong"));
    }
  }

  @PostMapping("/signup")
  @Operation(summary = "${UserController.signup}")
  @ApiResponses(value = {
         @ApiResponse(responseCode = "400", description = "Something went wrong"),
         @ApiResponse(responseCode = "403", description = "Access denied"),
         @ApiResponse(responseCode = "422", description = "Username is already in use")
  })
  public ResponseEntity<ServerResponse> signup(@Parameter(description = "Signup User") @RequestBody UserDataDTO user) {
    try {
      String token = userService.signup(modelMapper.map(user, AppUser.class));
      if (token != null && token.length() > 0){
        return ResponseEntity.ok(ServerResponse.ok(token));
      } else
        return ResponseEntity.badRequest().body(ServerResponse.error("Something went wrong"));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(ServerResponse.error(String.format("Something went wrong: %s", e)));
    }
  }

  @DeleteMapping(value = "/{username}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @Operation(summary = "${UserController.delete}", security = @SecurityRequirement(name = "apiKey"))
  @ApiResponses(value = {
         @ApiResponse(responseCode = "400", description = "Something went wrong"),
         @ApiResponse(responseCode = "403", description = "Access denied"),
         @ApiResponse(responseCode = "404", description = "The user doesn't exist"),
         @ApiResponse(responseCode = "500", description = "Expired or invalid JWT token")
  })
  public ResponseEntity<ServerResponse> delete(@Parameter(description = "Username") @PathVariable String username) {
    try {
      userService.delete(username);
      return ResponseEntity.ok(ServerResponse.ok(username));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(ServerResponse.error(String.format("Something went wrong: %s", e)));
    }
  }

  @GetMapping(value = "/{username}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @Operation(summary = "${UserController.search}", responses = {
         @ApiResponse(responseCode = "400", description = "Something went wrong"),
         @ApiResponse(responseCode = "403", description = "Access denied"),
         @ApiResponse(responseCode = "404", description = "The user doesn't exist"),
         @ApiResponse(responseCode = "500", description = "Expired or invalid JWT token")
  }, security = @SecurityRequirement(name = "apiKey"))
  public ResponseEntity<ServerResponse> search(@Parameter(description = "Username") @PathVariable String username) {
    try {
      UserResponseDTO userResponse = modelMapper.map(userService.search(username), UserResponseDTO.class);
      return ResponseEntity.ok(ServerResponse.ok(userResponse));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(ServerResponse.error(String.format("Something went wrong: %s", e)));
    } 
  }

  @GetMapping(value = "/me")
  @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
  @Operation(summary = "${UserController.me}", responses = {
         @ApiResponse(responseCode = "400", description = "Something went wrong"),
         @ApiResponse(responseCode = "403", description = "Access denied"),
         @ApiResponse(responseCode = "500", description = "Expired or invalid JWT token")
  }, security = @SecurityRequirement(name = "apiKey"))
  public ResponseEntity<ServerResponse> whoami(HttpServletRequest req) {
    
    try {
      UserResponseDTO userResponse = modelMapper.map(userService.whoami(req), UserResponseDTO.class);
      return ResponseEntity.ok(ServerResponse.ok(userResponse));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(ServerResponse.error(String.format("Something went wrong: %s", e)));
    } 
  }

  @GetMapping("/refresh")
  @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
  public ResponseEntity<ServerResponse> refresh(HttpServletRequest req) {
    try {
      String token = userService.refresh(req.getRemoteUser());
      return ResponseEntity.ok(ServerResponse.ok(token));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(ServerResponse.error(String.format("Something went wrong: %s", e)));
    } 
  }

}
