package com.backend.ecommerce.users.model.dao;

import com.backend.ecommerce.users.model.LocalUser;
import com.backend.ecommerce.users.model.VerificationToken;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface VerificationTokenDAO extends ListCrudRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    void deleteByUser(LocalUser user);
}
