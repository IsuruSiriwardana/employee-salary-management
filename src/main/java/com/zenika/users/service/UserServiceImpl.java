package com.zenika.users.service;

import com.zenika.users.dto.ResponseMessage;
import com.zenika.users.dto.SimpleResponseDto;
import com.zenika.users.dto.UsersCsvDto;
import com.zenika.users.entity.Users;
import com.zenika.users.exception.InvalidUserDataException;
import com.zenika.users.mapper.UsersCsvDtoToUsersMapper;
import com.zenika.users.repository.UsersRepository;
import com.zenika.users.utils.CsvToBeanConverter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {

  private UsersRepository usersRepository;
  private UsersCsvDtoToUsersMapper usersMapper;

  @Override
  public SimpleResponseDto uploadUsers(InputStream inputStreamCsvData) {
    List<UsersCsvDto> usersCsvDtoList =
        CsvToBeanConverter.convertCsvToBean(inputStreamCsvData, UsersCsvDto.class);
    List<Users> usersList = usersMapper.mapToUsers(usersCsvDtoList);
    validateUsersList(usersList);
    Iterable<Users> existingUsers = getExistingUsers(usersList);
    Iterable<Users> savedUsers = saveUsers(usersList);
    boolean newUsersCreated = IterableUtils.size(existingUsers) != IterableUtils.size(savedUsers);
    log.info(
        "Uploading completed with creating and updating {} users", IterableUtils.size(savedUsers));
    return new SimpleResponseDto(
        newUsersCreated ? ResponseMessage.USERS_CREATED : ResponseMessage.USERS_UPDATED);
  }

  private void validateUsersList(List<Users> usersList) {
    log.info("Validating users");
    validateForDuplicateIds(usersList);
  }

  private void validateForDuplicateIds(List<Users> usersList) {
    log.info("Validating for duplicate IDs");
    Set<String> uniqueIds = new HashSet<>();
    String duplicateIds =
        usersList.stream()
            .map(Users::getId)
            .filter(id -> !uniqueIds.add(id))
            .collect(Collectors.joining(", "));
    if (StringUtils.isNoneBlank(duplicateIds)) {
      log.info("Found duplicate IDs in the users list: {}", duplicateIds);
      throw new InvalidUserDataException("Found duplicate id values: " + duplicateIds);
    }
  }

  private Iterable<Users> getExistingUsers(List<Users> usersList) {
    List<String> userIdList = usersList.stream().map(Users::getId).collect(Collectors.toList());
    return usersRepository.findAllById(userIdList);
  }

  private Iterable<Users> saveUsers(List<Users> users) {
    log.info("Saving users in DB");
    try {
      return usersRepository.saveAll(users);
    } catch (ConstraintViolationException | DataIntegrityViolationException ex) {
      log.error("Error occurred while saving users to DB", ex);
      throw new InvalidUserDataException("Error caused by invalid input data: " + ex.getMessage());
    } catch (Exception ex) {
      log.error("Unhandled exception during data save to DB: ", ex);
      throw new RuntimeException(ex);
    }
  }
}
