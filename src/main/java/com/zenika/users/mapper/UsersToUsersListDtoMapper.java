package com.zenika.users.mapper;

import com.zenika.users.dto.UsersListDto;
import com.zenika.users.entity.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsersToUsersListDtoMapper {

  default UsersListDto mapToUsersListDto(List<Users> users) {
    return mapToUsersListDto(0, users);
  }

  @Mapping(source = "users", target = "results")
  UsersListDto mapToUsersListDto(Integer dummy, List<Users> users);
}
