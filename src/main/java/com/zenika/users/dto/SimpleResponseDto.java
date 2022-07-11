package com.zenika.users.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@ToString
@Getter
@Setter
public class SimpleResponseDto {
  String message;
  String timestamp;

  public SimpleResponseDto(String message) {
    this.message = message;
    this.timestamp = new Date().toString();
  }
}
