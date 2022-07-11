package com.zenika.users.service;

import com.zenika.users.constants.ResponseMessageConstant;
import com.zenika.users.dto.SimpleResponseDto;
import com.zenika.users.dto.UsersCsvDto;
import com.zenika.users.entity.Users;
import com.zenika.users.exception.InvalidUserDataException;
import com.zenika.users.mapper.UsersCsvDtoToUsersMapper;
import com.zenika.users.repository.UsersRepository;
import com.zenika.users.utils.CsvToBeanConverter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

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
    saveUsers(usersList);
    return new SimpleResponseDto(ResponseMessageConstant.UPLOAD_SUCCESSFUL);
  }

  private void saveUsers(List<Users> users) {
    try {
      usersRepository.saveAll(users);
    } catch (ConstraintViolationException | DataIntegrityViolationException ex) {
      log.error("Error occurred while saving users to DB", ex);
      throw new InvalidUserDataException("Error caused by invalid input data: " + ex.getMessage());
    } catch (Exception ex) {
      log.error("Unhandled exception during data save to DB: ", ex);
      throw new RuntimeException(ex);
    }
  }
}
