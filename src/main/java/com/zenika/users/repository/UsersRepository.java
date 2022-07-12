package com.zenika.users.repository;

import com.zenika.users.entity.Users;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends PagingAndSortingRepository<Users, String> {
}
