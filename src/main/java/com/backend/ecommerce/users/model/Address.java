package com.backend.ecommerce.users.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotEmpty(message = "Street must not be empty")
    @Size(max = 100, message = "Street cannot exceed 100 characters")
    @Column(name = "street", nullable = false)
    private String street;

    @NotEmpty(message = "City must not be empty")
    @Size(max = 50, message = "City cannot exceed 50 characters")
    @Column(name = "city", nullable = false)
    private String city;

    @NotEmpty(message = "Country must not be empty")
    @Size(max = 75, message = "Country cannot exceed 75 characters")
    @Column(name = "country", nullable = false)
    private String country;

    @NotEmpty(message = "Postal code must not be empty")
    @Size(max = 20, message = "Postal code cannot exceed 20 characters")
    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private LocalUser user;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return id != null && id.equals(address.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
