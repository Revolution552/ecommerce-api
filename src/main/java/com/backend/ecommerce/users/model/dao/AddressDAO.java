package com.backend.ecommerce.users.model.dao;

import com.backend.ecommerce.users.model.Address;
import com.backend.ecommerce.users.model.LocalUser;
import com.backend.ecommerce.users.service.AddressService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AddressDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Address save(Address address) {
        if (address.getId() == null) {
            entityManager.persist(address);
            return address;
        } else {
            return entityManager.merge(address);
        }
    }

    public Optional<Address> findById(Long id) {
        Address address = entityManager.find(Address.class, id);
        return Optional.ofNullable(address);
    }

    @Transactional
    public void deleteById(Long id) {
        Address address = entityManager.find(Address.class, id);
        if (address != null) {
            entityManager.remove(address);
        }
    }

    public List<Address> findByUserId(Long userId) {
        return entityManager.createQuery("SELECT a FROM Address a WHERE a.user.id = :userId", Address.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}
