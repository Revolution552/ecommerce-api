package com.backend.ecommerce.users.service;

import com.backend.ecommerce.users.api.model.LoginBody;
import com.backend.ecommerce.users.api.model.PasswordResetBody;
import com.backend.ecommerce.users.api.model.RegistrationBody;
import com.backend.ecommerce.users.exception.EmailFailureException;
import com.backend.ecommerce.users.exception.EmailNotFoundException;
import com.backend.ecommerce.users.exception.UserAlreadyExistsException;
import com.backend.ecommerce.users.exception.UserNotVerifiedException;
import com.backend.ecommerce.users.model.LocalUser;
import com.backend.ecommerce.users.model.VerificationToken;
import com.backend.ecommerce.users.model.dao.LocalUserDAO;
import com.backend.ecommerce.users.model.dao.VerificationTokenDAO;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final LocalUserDAO localUserDAO;
    private final EncryptionService encryptionService;
    private final JWTService jwtService;
    private final EmailService emailService;
    private final VerificationTokenDAO verificationTokenDAO;

    public UserService(LocalUserDAO localUserDAO, EncryptionService encryptionService, JWTService jwtService,
                       EmailService emailService, VerificationTokenDAO verificationTokenDAO) {
        this.localUserDAO = localUserDAO;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.verificationTokenDAO = verificationTokenDAO;
    }

    public LocalUser registerUser(RegistrationBody registrationBody) throws UserAlreadyExistsException, EmailFailureException {
        if (localUserDAO.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent()
                || localUserDAO.findByUsernameIgnoreCase(registrationBody.getUsername()).isPresent()) {
            logger.warn("User registration attempt failed - user already exists: {}", registrationBody.getEmail());
            throw new UserAlreadyExistsException();
        }

        LocalUser user = new LocalUser();
        user.setUsername(registrationBody.getUsername());
        user.setFirstName(registrationBody.getFirstName());
        user.setLastName(registrationBody.getLastName());
        user.setEmail(registrationBody.getEmail());
        user.setPhoneNumber(registrationBody.getPhoneNumber());
        user.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));
        user.setConfirmPassword(encryptionService.encryptPassword(registrationBody.getConfirmPassword()));
        VerificationToken verificationToken = createVerificationToken(user);

        try {
            emailService.sendVerificationEmail(verificationToken);
            logger.info("Verification email sent for user: {}", registrationBody.getEmail());
        } catch (EmailFailureException e) {
            logger.error("Failed to send verification email for user: {}", registrationBody.getEmail(), e);
            throw e;
        }

        return localUserDAO.save(user);
    }

    private VerificationToken createVerificationToken(LocalUser user) {
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(jwtService.generateVerificationJWT(user));
        verificationToken.setCreatedTimestamp(new Timestamp(System.currentTimeMillis()));
        verificationToken.setUser(user);
        user.getVerificationTokens().add(verificationToken);
        return verificationToken;
    }

    public String loginUser(LoginBody loginBody) throws UserNotVerifiedException, EmailFailureException {
        Optional<LocalUser> opUser = localUserDAO.findByUsernameIgnoreCase(loginBody.getUsername());
        if (opUser.isPresent()) {
            LocalUser user = opUser.get();
            if (encryptionService.verifyPassword(loginBody.getPassword(), user.getPassword())) {
                if (user.isEmailVerified()) {
                    String jwt = jwtService.generateJWT(user);
                    logger.info("User logged in successfully: {}", loginBody.getUsername());
                    return jwt;
                } else {
                    List<VerificationToken> verificationTokens = user.getVerificationTokens();
                    boolean resend = verificationTokens.size() == 0 ||
                            verificationTokens.get(0).getCreatedTimestamp().before(new Timestamp(System.currentTimeMillis() - (60 * 60 * 1000)));
                    if (resend) {
                        VerificationToken verificationToken = createVerificationToken(user);
                        verificationTokenDAO.save(verificationToken);
                        emailService.sendVerificationEmail(verificationToken);
                        logger.info("Verification email resent for user: {}", loginBody.getUsername());
                    }
                    throw new UserNotVerifiedException(resend);
                }
            }
        }
        logger.warn("Invalid login attempt for user: {}", loginBody.getUsername());
        return null;
    }

    @Transactional
    public boolean verifyUser(String token) {
        Optional<VerificationToken> opToken = verificationTokenDAO.findByToken(token);
        if (opToken.isPresent()) {
            VerificationToken verificationToken = opToken.get();
            LocalUser user = verificationToken.getUser();
            if (!user.isEmailVerified()) {
                user.setEmailVerified(true);
                localUserDAO.save(user);
                verificationTokenDAO.deleteByUser(user);
                logger.info("User verified successfully: {}", user.getEmail());
                return true;
            }
        }
        logger.warn("Verification failed for token: {}", token);
        return false;
    }

    public void forgotPassword(String email) throws EmailNotFoundException, EmailFailureException {
        Optional<LocalUser> opUser = localUserDAO.findByEmailIgnoreCase(email);
        if (opUser.isPresent()) {
            LocalUser user = opUser.get();
            String token = jwtService.generatePasswordResetJWT(user);
            try {
                emailService.sendPasswordResetEmail(user, token);
                logger.info("Password reset email sent successfully to: {}", email);
            } catch (EmailFailureException e) {
                logger.error("Failed to send password reset email to: {}", email, e);
                throw e;
            }
        } else {
            logger.warn("Email not found for password reset: {}", email);
            throw new EmailNotFoundException();
        }
    }

    public boolean resetPassword(PasswordResetBody body) {
        if (!body.isPasswordConfirmed()) {
            logger.warn("Password reset failed - passwords do not match for token: {}", body.getToken());
            throw new IllegalArgumentException("New password and confirmation password do not match.");
        }

        String email = jwtService.getResetPasswordEmail(body.getToken());
        Optional<LocalUser> opUser = localUserDAO.findByEmailIgnoreCase(email);

        if (opUser.isPresent()) {
            LocalUser user = opUser.get();
            user.setPassword(encryptionService.encryptPassword(body.getNewPassword()));
            localUserDAO.save(user);
            logger.info("Password reset successfully for token: {}", body.getToken());
            return true;
        }

        logger.warn("Password reset failed - email not found for token: {}", body.getToken());
        return false;
    }

}
