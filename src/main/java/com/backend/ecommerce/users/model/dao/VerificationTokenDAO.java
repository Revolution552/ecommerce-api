package com.backend.ecommerce.users.model.dao;

import com.backend.ecommerce.users.model.LocalUser;
import com.backend.ecommerce.users.model.VerificationToken;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface VerificationTokenDAO extends ListCrudRepository<VerificationToken, Long> {

    // Find a VerificationToken by its token value
    Optional<VerificationToken> findByToken(String token);

    // Delete all tokens associated with a specific user
    void deleteByUser(LocalUser user);
}
