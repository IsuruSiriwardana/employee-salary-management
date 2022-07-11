package com.zenika.users.service;

import com.zenika.users.dto.SimpleResponseDto;

import java.io.InputStream;

public interface UserService {

  SimpleResponseDto uploadUsers(InputStream inputStreamCsvData);
}
