package com.zenika.users.controller;

import com.zenika.users.dto.ResponseMessage;
import com.zenika.users.dto.SimpleResponseDto;
import com.zenika.users.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  @PostMapping("/upload")
  public ResponseEntity<?> uploadUsers(@RequestParam("file") MultipartFile file) {
    log.info("User file upload request received");
    try {
      SimpleResponseDto simpleResponseDto = userService.uploadUsers(file.getInputStream());
      return new ResponseEntity<>(
          simpleResponseDto, simpleResponseDto.getMessage().getResponseStatus());
    } catch (IOException ex) {
      log.error("Error occurred during reading the incoming file", ex);
      return new ResponseEntity<>(
          new SimpleResponseDto(ResponseMessage.FILE_READ_ERROR, ex.getMessage()),
          ResponseMessage.FILE_READ_ERROR.getResponseStatus());
    }
  }
}
