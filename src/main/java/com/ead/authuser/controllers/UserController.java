package com.ead.authuser.controllers;

import com.ead.authuser.dtos.UserDto;
import com.ead.authuser.dtos.UserView;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.ead.authuser.specifications.SpecificationTemplate;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/users")
public class UserController {

  @Autowired
  UserService userService;

  @GetMapping
  public ResponseEntity<Page<UserModel>> getAllUsers(SpecificationTemplate.UserSpec spec,
          @PageableDefault(page = 0, size = 10, sort = "userId", direction = Sort.Direction.ASC) Pageable pageable) {
    Page<UserModel> userModelPage = userService.findAll(spec, pageable);
    if (!userModelPage.isEmpty()) {\
      for (UserModel user : userModelPage.toList()) {
        user.add(linkTo(methodOn(UserController.class).getOneUSer(user.getUserId())).withSelfRel());
      }
    }
    return ResponseEntity.status(HttpStatus.OK).body(userModelPage);
  }

  @GetMapping("/{userId}")
  public ResponseEntity<Object> getOneUSer(@PathVariable(value = "userId") UUID userId) {
    Optional<UserModel> userModelOptional = userService.findById(userId);
    if(!userModelOptional.isPresent()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }
    return ResponseEntity.status(HttpStatus.OK).body(userModelOptional.get());
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<Object> deleteUser(@PathVariable(value = "userId") UUID userId) {
    Optional<UserModel> userModelOptional = userService.findById(userId);
    if(!userModelOptional.isPresent()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }
    userService.delete(userModelOptional.get());
    return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully");
  }

  @PutMapping("/{userId}")
  public ResponseEntity<Object> updateUser(@PathVariable(value = "userId") UUID userId,
                                           @RequestBody @Validated(UserView.UserPut.class)
                                           @JsonView(UserView.UserPut.class) UserDto userDto) {
    Optional<UserModel> userModelOptional = userService.findById(userId);
    if(!userModelOptional.isPresent()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }
    UserModel userModel = userModelOptional.get();
    userModel.setFullName(userDto.fullName());
    userModel.setPhoneNumber(userDto.phoneNumber());
    userModel.setCpf(userDto.cpf());
    userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
    userService.save(userModel);
    return ResponseEntity.status(HttpStatus.OK).body(userModel);
  }

  @PutMapping("/{userId}/password")
  public ResponseEntity<Object> updatePassword(@PathVariable(value = "userId") UUID userId,
                                           @RequestBody @Validated(UserView.PasswordPut.class)
                                           @JsonView(UserView.PasswordPut.class) UserDto userDto) {
    Optional<UserModel> userModelOptional = userService.findById(userId);
    if(!userModelOptional.isPresent()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }
    if (!userModelOptional.get().getPassword().equals(userDto.oldPassword())) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Mismatched old password!");
    }
    UserModel userModel = userModelOptional.get();
    userModel.setPassword(userDto.password());
    userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
    userService.save(userModel);
    return ResponseEntity.status(HttpStatus.OK).body("Password updated successfully");
  }

  @PutMapping("/{userId}/image")
  public ResponseEntity<Object> updateImage(@PathVariable(value = "userId") UUID userId,
                                               @RequestBody @Validated(UserView.ImagePut.class)
                                               @JsonView(UserView.ImagePut.class) UserDto userDto) {
    Optional<UserModel> userModelOptional = userService.findById(userId);
    if(!userModelOptional.isPresent()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }
    UserModel userModel = userModelOptional.get();
    userModel.setImageUrl(userDto.imageUrl());
    userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
    userService.save(userModel);
    return ResponseEntity.status(HttpStatus.OK).body(userModel);
  }

}