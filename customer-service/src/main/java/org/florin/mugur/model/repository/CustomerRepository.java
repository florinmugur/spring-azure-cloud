package org.florin.mugur.model.repository;

import org.florin.mugur.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Customer findCustomerByFirstNameAndLastName(String firstName, String lastName);

    Customer findCustomerByFirstNameOrLastName(String firstName, String lastName);
}
