package com.backend.ecommerce.users.api.controller.user;

import com.backend.ecommerce.users.api.controller.auth.AuthenticationController;
import com.backend.ecommerce.users.model.Address;
import com.backend.ecommerce.users.model.LocalUser;
import com.backend.ecommerce.users.payload.AddressDTO;
import com.backend.ecommerce.users.payload.UserProfileDTO;
import com.backend.ecommerce.users.service.AddressService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Transactional
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    private final AddressService addressService;

    public UserController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/me")
    @Transactional
    public ResponseEntity<?> getLoggedInUserProfile(@AuthenticationPrincipal LocalUser user) {
        if (user == null) {
            logger.warn("Unauthorized access attempt: No authenticated user found.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated"));
        }

        UserProfileDTO userProfileDTO = new UserProfileDTO(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getAddresses()  // Addresses will be initialized within the session
        );

        // Log the user's profile access
        logger.info("User profile accessed: {}", user.getUsername());

        return ResponseEntity.ok(userProfileDTO);
    }

    @GetMapping("/address")
    public ResponseEntity<?> getAddresses(@AuthenticationPrincipal LocalUser user) {
        if (user == null) {
            logger.warn("Unauthorized access attempt to address API: No authenticated user found.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        List<Address> addresses = addressService.getAddressesByUserId(user.getId());

        logger.info("User '{}' accessed their addresses. Total addresses found: {}", user.getUsername(), addresses.size());

        return ResponseEntity.ok(Map.of("addresses", addresses));
    }

    @PostMapping("/address")
    public ResponseEntity<?> createAddress(@AuthenticationPrincipal LocalUser user,
                                           @Valid @RequestBody AddressDTO addressDTO) {
        if (user == null) {
            logger.warn("Unauthorized access attempt to create an address: No authenticated user found.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        Address address = new Address();
        address.setStreet(addressDTO.getStreet());
        address.setCity(addressDTO.getCity());
        address.setCountry(addressDTO.getCountry());
        address.setPostalCode(addressDTO.getPostalCode());
        address.setUser(user);

        Address savedAddress = addressService.createAddress(address);

        // Log successful address creation
        logger.info("User '{}' created a new address at '{}', '{}', '{}'",
                user.getUsername(), address.getStreet(), address.getCity(), address.getCountry());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Address created successfully", "address", savedAddress));
    }

    @PutMapping("/address/{addressId}")
    public ResponseEntity<?> updateAddress(@AuthenticationPrincipal LocalUser user,
                                           @PathVariable Long addressId,
                                           @Valid @RequestBody AddressDTO addressDTO) {
        if (user == null) {
            logger.warn("Unauthorized access attempt to update address with ID '{}'. No authenticated user found.", addressId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        Address updatedAddress = new Address();
        updatedAddress.setStreet(addressDTO.getStreet());
        updatedAddress.setCity(addressDTO.getCity());
        updatedAddress.setCountry(addressDTO.getCountry());
        updatedAddress.setPostalCode(addressDTO.getPostalCode());

        Address address = addressService.updateAddress(addressId, updatedAddress, user);
        if (address != null) {
            logger.info("User '{}' successfully updated address with ID '{}'. New address: '{}', '{}', '{}'",
                    user.getUsername(), addressId, address.getStreet(), address.getCity(), address.getCountry());
            return ResponseEntity.ok(Map.of("message", "Address updated successfully", "address", address));
        } else {
            logger.warn("User '{}' attempted to update address with ID '{}', but access was denied or the address was not found.",
                    user.getUsername(), addressId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied or address not found"));
        }
    }

    @DeleteMapping("/address/{addressId}")
    public ResponseEntity<?> deleteAddress(@AuthenticationPrincipal LocalUser user,
                                           @PathVariable Long addressId) {
        if (user == null) {
            logger.warn("Unauthorized access attempt to delete address with ID '{}'. No authenticated user found.", addressId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        boolean isDeleted = addressService.deleteAddress(addressId, user);
        if (isDeleted) {
            logger.info("User '{}' successfully deleted address with ID '{}'.", user.getUsername(), addressId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(Map.of("message", "Address deleted successfully"));
        } else {
            logger.warn("User '{}' attempted to delete address with ID '{}', but access was denied or the address was not found.",
                    user.getUsername(), addressId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied or address not found"));
        }
    }
}
