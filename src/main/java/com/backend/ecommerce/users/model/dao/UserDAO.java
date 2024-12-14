package com.backend.ecommerce.users.model.dao;

import com.backend.ecommerce.users.model.User;
import org.springframework.data.repository.ListCrudRepository;
import java.util.Optional;

public interface LocalUserDAO extends ListCrudRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);
}
