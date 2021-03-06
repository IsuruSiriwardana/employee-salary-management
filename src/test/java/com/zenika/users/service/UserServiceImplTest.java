package com.zenika.users.service;

import com.zenika.users.dto.ResponseMessage;
import com.zenika.users.dto.SimpleResponseDto;
import com.zenika.users.dto.UsersListDto;
import com.zenika.users.entity.Users;
import com.zenika.users.exception.InvalidUserDataException;
import com.zenika.users.mapper.UsersCsvDtoToUsersMapper;
import com.zenika.users.mapper.UsersToUsersListDtoMapper;
import com.zenika.users.repository.UsersRepository;
import com.zenika.users.testutils.TestFileReader;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.JDBCConnectionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.zenika.users.testutils.TestFileReader.DUPLICATE_ID_CSV_DATA_SOURCE;
import static com.zenika.users.testutils.TestFileReader.VALID_CSV_DATA_SOURCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {

  private UsersRepository usersRepository;
  private UserService userService;

  @BeforeEach
  void setup() {
    usersRepository = mock(UsersRepository.class);
    UsersCsvDtoToUsersMapper usersCsvDtoToUsersMapper =
        Mappers.getMapper(UsersCsvDtoToUsersMapper.class);
    UsersToUsersListDtoMapper usersToUsersDtoMapper =
        Mappers.getMapper(UsersToUsersListDtoMapper.class);
    userService =
        new UserServiceImpl(usersRepository, usersCsvDtoToUsersMapper, usersToUsersDtoMapper);
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

  @Test
  @DisplayName("getUsers should throw InvalidUserDataException when sortBy has wrong format")
  void getUsersGivenWrongSortByFormat() {
    Executable executable = () -> userService.getUsers(0, 1000, 0, 0, new String[] {"id"});
    assertThrows(InvalidUserDataException.class, executable);
  }

  @Test
  @DisplayName("getUsers should throw InvalidUserDataException when sortBy has invalid field")
  void getUsersGivenWrongSortByField() {
    Executable executable =
        () -> userService.getUsers(0, 1000, 0, 0, new String[] {"startDate,asc"});
    assertThrows(InvalidUserDataException.class, executable);
  }

  @Test
  @DisplayName("getUsers should throw InvalidUserDataException when sortBy has invalid direction")
  void getUsersGivenWrongSortByDirection() {
    Executable executable =
        () -> userService.getUsers(0, 1000, 0, 0, new String[] {"id,ascending"});
    assertThrows(InvalidUserDataException.class, executable);
  }

  @Test
  @DisplayName("getUsers should return filtered result when sortBy has the default passed value")
  void getUsersGivenDefaultSortBy() {
    givenRepositoryReturnsSortedUserDetails();
    UsersListDto users = userService.getUsers(500, 5000, 1, 3, new String[]{"name", "desc"});
    assertEquals(3, users.getResults().size());
    assertEquals("e0003", users.getResults().get(2).getId());
  }

  @Test
  @DisplayName("getUsers should return filtered result when all parameters are correct")
  void getUsersGivenCorrectInputs() {
    ArgumentCaptor<Sort> sortArgumentCaptor = ArgumentCaptor.forClass(Sort.class);
    givenRepositoryReturnsSortedUserDetails();
    UsersListDto users = userService.getUsers(500, 5000, 1, 3,
        new String[]{"salary,asc", "name,desc"});
    assertEquals(3, users.getResults().size());
    assertEquals("e0003", users.getResults().get(2).getId());
    verify(usersRepository).
        findBySalaryGreaterThanEqualAndSalaryLessThan(anyDouble(), anyDouble(), sortArgumentCaptor.capture());
    Sort sort = sortArgumentCaptor.getValue();
    List<Sort.Order> orders = sort.toList();
    assertEquals("salary", orders.get(0).getProperty());
    assertEquals(Sort.Direction.ASC, orders.get(0).getDirection());
    assertEquals("name", orders.get(1).getProperty());
    assertEquals(Sort.Direction.DESC, orders.get(1).getDirection());
  }

  private void givenRepositoryReturnsSortedUserDetails() {
    when(usersRepository
        .findBySalaryGreaterThanEqualAndSalaryLessThan(anyDouble(), anyDouble(), any()))
        .thenReturn(getSortedUsers());
  }

  private void givenUsersRepositoryThrowsException(Exception exception) {
    when(usersRepository.saveAll(any())).thenThrow(exception);
  }

  private void givenUsersRepositorySaveAllSuccess() {
    when(usersRepository.saveAll(any())).thenReturn(generateUsers());
  }

  private InputStream givenValidUsersCsvInputStream() throws IOException {
    return TestFileReader.readFile(VALID_CSV_DATA_SOURCE);
  }

  private InputStream givenCsvInputStreamWithDuplicateIds() throws IOException {
    return TestFileReader.readFile(DUPLICATE_ID_CSV_DATA_SOURCE);
  }

  private void givenRepositoryHasAllUsersAlready() {
    when(usersRepository.findAllById(any())).thenReturn(generateUsers());
  }

  private void givenRepositoryHasOnlySomeUsersAlready() {
    List<Users> users = generateUsers();
    List<Users> subList = users.stream().skip(users.size() / 2).collect(Collectors.toList());
    when(usersRepository.findAllById(any())).thenReturn(subList);
  }

  private List<Users> generateUsers() {
    return List.of(
        new Users("e0001", "hpotter", "Harry Potter", 1234.56, new Date()),
        new Users("e0002", "rwesley", "Ron Weasley", 19234.50, new Date()),
        new Users("e0003", "???????????????", "Severus Snape", 4000.0, new Date()),
        new Users("e0004", "rhagrid", "Rubeus Hagrid", 3999.999, new Date()),
        new Users("e0005", "voldemort", "Lord Voldemort", 523.4, new Date()),
        new Users("e0006", "gwesley", "Ginny Weasley", 4000.00, new Date()),
        new Users("e0007", "hgranger", "Hermione Granger", 0.0, new Date()),
        new Users("e0008", "adumbledore", "Albus Dumbledore", 341.23, new Date()));
  }
  private List<Users> getSortedUsers() {
    return List.of(
        new Users("e0005", "voldemort", "Lord Voldemort", 523.4, new Date()),
        new Users("e0001", "hpotter", "Harry Potter", 1234.56, new Date()),
        new Users("e0004", "rhagrid", "Rubeus Hagrid", 3999.999, new Date()),
        new Users("e0003", "???????????????", "Severus Snape", 4000.0, new Date()),
        new Users("e0006", "gwesley", "Ginny Weasley", 4000.00, new Date()));
  }
}
