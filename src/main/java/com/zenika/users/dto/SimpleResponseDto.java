package com.zenika.users.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@ToString
@Getter
@Setter
public class SimpleResponseDto {
  ResponseMessage message;
  String timestamp;

  @JsonInclude(Include.NON_NULL)
  String errorDetails;

  public SimpleResponseDto(ResponseMessage message) {
    this.message = message;
    this.timestamp = new Date().toString();
  }

  public SimpleResponseDto(ResponseMessage message, String errorDetails) {
    this.message = message;
    this.errorDetails = errorDetails;
    this.timestamp = new Date().toString();
  }
}
