package org.florin.mugur.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Address {

    @Id
    @GeneratedValue
    private Long id;

    private String streetName;

    private Integer houseNumber;

    private Integer postalCode;

    private String country;

}
