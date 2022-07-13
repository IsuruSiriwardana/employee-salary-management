package com.zenika.users.service;

import com.zenika.users.dto.SimpleResponseDto;
import com.zenika.users.dto.UsersListDto;

import java.io.InputStream;

public interface UserService {

  SimpleResponseDto uploadUsers(InputStream inputStreamCsvData);

  UsersListDto getUsers(double minSalary, double maxSalary, int offset, int limit, String[] sortBy);
}
