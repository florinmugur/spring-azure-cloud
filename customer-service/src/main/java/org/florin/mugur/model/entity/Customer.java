package org.florin.mugur.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import static jakarta.persistence.CascadeType.ALL;

@Data
@Entity
public class Customer {

    @Id
    @GeneratedValue
    private Long id;

    private String firstName;

    private String lastName;

    private Integer age;

    @OneToOne(cascade = ALL, orphanRemoval = true)
    private Address homeAddress;
}
