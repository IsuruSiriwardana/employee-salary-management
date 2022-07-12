package com.zenika.users.service;

import com.zenika.users.dto.ResponseMessage;
import com.zenika.users.dto.SimpleResponseDto;
import com.zenika.users.entity.Users;
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
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

import static com.zenika.users.testutils.TestFileReader.DUPLICATE_ID_CSV_DATA_SOURCE;
import static com.zenika.users.testutils.TestFileReader.VALID_CSV_DATA_SOURCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserServiceImplTest {

  private static final String[] FULL_VALID_USERS_IDS =
      new String[] {"e0001", "e0002", "e0003", "e0004", "e0005", "e0006", "e0007", "e0008"};
  private static final String[] PARTIAL_VALID_USERS_IDS =
      new String[] {"e0005", "e0006", "e0007", "e0008"};

  private UsersRepository usersRepository;
  private UserService userService;

  @BeforeEach
  void setup() {
    usersRepository = mock(UsersRepository.class);
    UsersCsvDtoToUsersMapper usersMapper = Mappers.getMapper(UsersCsvDtoToUsersMapper.class);
    userService = new UserServiceImpl(usersRepository, usersMapper);
  }

  @Test
  @DisplayName(
      "Calling upload users should persist data and "
          + "return USERS_CREATED if at least one new user created")
  void uploadUsersWithSomeNewUsers() throws IOException {
    InputStream inputStream = givenValidUsersCsvInputStream();
    givenRepositoryHasOnlySomeUsersAlready();
    givenUsersRepositorySaveAllSuccess();
    SimpleResponseDto simpleResponseDto = userService.uploadUsers(inputStream);
    assertEquals(ResponseMessage.USERS_CREATED, simpleResponseDto.getMessage());
  }

  @Test
  @DisplayName(
      "Calling upload users should persist data and "
          + "return USERS_UPDATED if all users already exist")
  void uploadUsersWithAllExistingUsers() throws IOException {
    InputStream inputStream = givenValidUsersCsvInputStream();
    givenRepositoryHasAllUsersAlready();
    givenUsersRepositorySaveAllSuccess();
    SimpleResponseDto simpleResponseDto = userService.uploadUsers(inputStream);
    assertEquals(ResponseMessage.USERS_UPDATED, simpleResponseDto.getMessage());
  }

  @Test
  @DisplayName(
      "Calling upload users with invalid input stream should result InvalidUserDataException")
  void uploadDataWithInvalidInputStream() {
    InputStream inputStream = mock(InputStream.class);
    Executable executable = () -> userService.uploadUsers(inputStream);
    assertThrows(InvalidUserDataException.class, executable);
  }

  @Test
  @DisplayName("Calling upload users with duplicate IDs should result InvalidUserDataException")
  void uploadUsersWithDuplicateIds() throws IOException {
    InputStream inputStream = givenCsvInputStreamWithDuplicateIds();
    Executable executable = () -> userService.uploadUsers(inputStream);
    assertThrows(InvalidUserDataException.class, executable);
  }

  @Test
  @DisplayName(
      "Calling upload users should return InvalidUserDataException "
          + "given repository throws InvalidUserDataException")
  void uploadDataWithNonUniqueData() throws IOException {
    InputStream inputStream = givenValidUsersCsvInputStream();
    givenUsersRepositoryThrowsException(new ConstraintViolationException("exception", null, null));
    Executable executable = () -> userService.uploadUsers(inputStream);
    assertThrows(InvalidUserDataException.class, executable);
  }

  @Test
  @DisplayName(
      "Calling upload users should return RuntimeException "
          + "given repository throws JDBCConnectionException")
  void uploadDataWhileDBConnectionHavingIssues() throws IOException {
    InputStream inputStream = givenValidUsersCsvInputStream();
    givenUsersRepositoryThrowsException(new JDBCConnectionException("exception", null));
    Executable executable = () -> userService.uploadUsers(inputStream);
    assertThrows(RuntimeException.class, executable);
  }

  private void givenUsersRepositoryThrowsException(Exception exception) {
    when(usersRepository.saveAll(any())).thenThrow(exception);
  }

  private void givenUsersRepositorySaveAllSuccess() {
    when(usersRepository.saveAll(any())).thenReturn(generateUsers(FULL_VALID_USERS_IDS));
  }

  private InputStream givenValidUsersCsvInputStream() throws IOException {
    return TestFileReader.readFile(VALID_CSV_DATA_SOURCE);
  }

  private InputStream givenCsvInputStreamWithDuplicateIds() throws IOException {
    return TestFileReader.readFile(DUPLICATE_ID_CSV_DATA_SOURCE);
  }

  private void givenRepositoryHasAllUsersAlready() {
    when(usersRepository.findAllById(any())).thenReturn(generateUsers(FULL_VALID_USERS_IDS));
  }

  private void givenRepositoryHasOnlySomeUsersAlready() {
    when(usersRepository.findAllById(any())).thenReturn(generateUsers(PARTIAL_VALID_USERS_IDS));
  }

  private Iterable<Users> generateUsers(String... ids) {
    return Arrays.stream(ids)
        .map(id -> new Users(id, id, id, 100.0, new Date()))
        .collect(Collectors.toList());
  }
}
