package com.zenika.users.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ResponseMessage {

  USERS_CREATED("Users successfully created", HttpStatus.CREATED),
  USERS_UPDATED("Users successfully updated", HttpStatus.OK),
  ERROR_OCCURRED_BAD_INPUT("Error occurred due to bad input", HttpStatus.BAD_REQUEST),
  FILE_READ_ERROR("Error occurred during reading the incoming file", HttpStatus.BAD_REQUEST);

  @Getter(onMethod_ = @JsonValue)
  private String message;
  private HttpStatus responseStatus;
}
