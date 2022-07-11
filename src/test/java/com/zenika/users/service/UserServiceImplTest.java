package com.zenika.users.service;

import com.zenika.users.constants.ResponseMessageConstant;
import com.zenika.users.dto.SimpleResponseDto;
import com.zenika.users.exception.InvalidUserDataException;
import com.zenika.users.mapper.UsersCsvDtoToUsersMapper;
import com.zenika.users.repository.UsersRepository;
import com.zenika.users.testutils.TestFileReader;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.JDBCConnectionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mapstruct.factory.Mappers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {

  private static final String VALID_CSV_DATA_SOURCE = "src/test/resources/sample_upload_file.csv";

  private UsersRepository usersRepository;
  private UsersCsvDtoToUsersMapper usersMapper;
  private UserService userService;

  @BeforeEach
  void setup() {
    usersRepository = mock(UsersRepository.class);
    usersMapper = Mappers.getMapper(UsersCsvDtoToUsersMapper.class);
    userService = new UserServiceImpl(usersRepository, usersMapper);
  }

  @Test
  @DisplayName("Calling upload users should persist data given valid scv input stream")
  void uploadUsersShouldPersistData() throws URISyntaxException, IOException {
    InputStream inputStream = givenParsableCsvInputStream();
    givenUsersRepositorySaveAllSuccess();
    SimpleResponseDto simpleResponseDto = userService.uploadUsers(inputStream);
    assertEquals(ResponseMessageConstant.UPLOAD_SUCCESSFUL, simpleResponseDto.getMessage());
  }

  @Test
  @DisplayName("Calling upload users with invalid input stream should result RuntimeException")
  void uploadDataWithInvalidInputStream() {
    InputStream inputStream = mock(InputStream.class);
    Executable executable = () -> userService.uploadUsers(inputStream);
    assertThrows(RuntimeException.class, executable);
  }

  @Test
  @DisplayName("Calling upload users should return InvalidUserDataException "
      + "given repository throws InvalidUserDataException")
  void uploadDataWithNonUniqueData() throws URISyntaxException, IOException {
    InputStream inputStream = givenParsableCsvInputStream();
    givenUsersRepositoryThrowsException(new ConstraintViolationException("exception", null, null));
    Executable executable = () -> userService.uploadUsers(inputStream);
    assertThrows(InvalidUserDataException.class, executable);
  }

  @Test
  @DisplayName("Calling upload users should return RuntimeException "
      + "given repository throws JDBCConnectionException")
  void uploadDataWhileDBConnectionHavingIssues() throws URISyntaxException, IOException {
    InputStream inputStream = givenParsableCsvInputStream();
    givenUsersRepositoryThrowsException(new JDBCConnectionException("exception", null));
    Executable executable = () -> userService.uploadUsers(inputStream);
    assertThrows(RuntimeException.class, executable);
  }

  private void givenUsersRepositoryThrowsException(Exception exception) {
    when(usersRepository.saveAll(any())).thenThrow(exception);
  }


  private void givenUsersRepositorySaveAllSuccess() {
    when(usersRepository.saveAll(any())).thenReturn(Collections.emptyList());
  }

  private InputStream givenParsableCsvInputStream() throws URISyntaxException, IOException {
    return TestFileReader.readFile(VALID_CSV_DATA_SOURCE);
  }
}
