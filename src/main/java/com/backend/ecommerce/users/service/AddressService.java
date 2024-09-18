package com.backend.ecommerce.users.service;

import com.backend.ecommerce.users.model.Address;
import com.backend.ecommerce.users.model.LocalUser;
import com.backend.ecommerce.users.model.dao.AddressDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("ALL")
@Service
public class AddressService {

    private final AddressDAO addressDAO;

    public AddressService(AddressDAO addressDAO) {
        this.addressDAO = addressDAO;
    }

    public List<Address> getAddressesByUserId(Long userId) {
        return addressDAO.findByUserId(userId);
    }

    public Address createAddress(Address address) {
        return addressDAO.save(address);
    }

    public Optional<Address> getAddressById(Long addressId) {
        return addressDAO.findById(addressId);
    }

    @Transactional
    public Address updateAddress(Long addressId, Address updatedAddress, LocalUser user) {
        Optional<Address> existingAddressOpt = addressDAO.findById(addressId);
        if (existingAddressOpt.isPresent()) {
            Address existingAddress = existingAddressOpt.get();
            if (existingAddress.getUser().getId().equals(user.getId())) {
                existingAddress.setStreet(updatedAddress.getStreet());
                existingAddress.setCity(updatedAddress.getCity());
                existingAddress.setCountry(updatedAddress.getCountry());
                existingAddress.setPostalCode(updatedAddress.getPostalCode());
                return addressDAO.save(existingAddress);
            }
        }
        return null;
    }

    @Transactional
    public boolean deleteAddress(Long addressId, LocalUser user) {
        Optional<Address> existingAddressOpt = addressDAO.findById(addressId);
        if (existingAddressOpt.isPresent() && existingAddressOpt.get().getUser().getId().equals(user.getId())) {
            addressDAO.deleteById(addressId);
            return true;
        }
        return false;
    }

    public Optional<Address> findById(Long addressId) {
        return addressDAO.findById(addressId);
    }

    public Address save(Address existingAddress) {
        return addressDAO.save(existingAddress);
    }
}

