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
  public String login(@RequestBody UserSignInDTO user) {
    return userService.signin(user.getUsername(), user.getPassword());
  }

  @PostMapping("/signup")
  @Operation(summary = "${UserController.signup}")
  @ApiResponses(value = {
         @ApiResponse(responseCode = "400", description = "Something went wrong"),
         @ApiResponse(responseCode = "403", description = "Access denied"),
         @ApiResponse(responseCode = "422", description = "Username is already in use")
  })
  public String signup(@Parameter(description = "Signup User") @RequestBody UserDataDTO user) {
    return userService.signup(modelMapper.map(user, AppUser.class));
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
  public String delete(@Parameter(description = "Username") @PathVariable String username) {
    userService.delete(username);
    return username;
  }

  @GetMapping(value = "/{username}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @Operation(summary = "${UserController.search}", responses = {
         @ApiResponse(responseCode = "400", description = "Something went wrong"),
         @ApiResponse(responseCode = "403", description = "Access denied"),
         @ApiResponse(responseCode = "404", description = "The user doesn't exist"),
         @ApiResponse(responseCode = "500", description = "Expired or invalid JWT token")
  }, security = @SecurityRequirement(name = "apiKey"))
  public UserResponseDTO search(@Parameter(description = "Username") @PathVariable String username) {
    return modelMapper.map(userService.search(username), UserResponseDTO.class);
  }

  @GetMapping(value = "/me")
  @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
  @Operation(summary = "${UserController.me}", responses = {
         @ApiResponse(responseCode = "400", description = "Something went wrong"),
         @ApiResponse(responseCode = "403", description = "Access denied"),
         @ApiResponse(responseCode = "500", description = "Expired or invalid JWT token")
  }, security = @SecurityRequirement(name = "apiKey"))
  public UserResponseDTO whoami(HttpServletRequest req) {
    return modelMapper.map(userService.whoami(req), UserResponseDTO.class);
  }

  @GetMapping("/refresh")
  @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CLIENT')")
  public String refresh(HttpServletRequest req) {
    return userService.refresh(req.getRemoteUser());
  }

}
