package com.zenika.users.controller;

import com.zenika.users.constants.ResponseMessageConstant;
import com.zenika.users.dto.SimpleResponseDto;
import com.zenika.users.mapper.UsersCsvDtoToUsersMapper;
import com.zenika.users.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/v1/users")
@Slf4j
@AllArgsConstructor
public class UsersControllerV1 {

  private UserService userService;
  private UsersCsvDtoToUsersMapper usersMapper;

  @GetMapping
  public ResponseEntity<String> getVersion() {
    return new ResponseEntity<>("v1", HttpStatus.OK);
  }

  @PostMapping("/upload")
  public ResponseEntity<?> uploadUsers(@RequestParam("file") MultipartFile file) {
    log.info("User file upload request received");
    try {
      SimpleResponseDto simpleResponseDto = userService.uploadUsers(file.getInputStream());
      return new ResponseEntity<>(simpleResponseDto, HttpStatus.OK);
    } catch (IOException ex) {
      log.error("Error occurred during reading the incoming file", ex);
      return new ResponseEntity<>(
          new SimpleResponseDto(ResponseMessageConstant.FILE_READ_ERROR),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
