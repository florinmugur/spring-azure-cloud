package org.florin.mugur.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.florin.mugur.exception.CustomerNotFoundException;
import org.florin.mugur.model.entity.Address;
import org.florin.mugur.model.entity.Customer;
import org.florin.mugur.model.repository.AddressRepository;
import org.florin.mugur.model.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasLength;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;

    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer findCustomer(Long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isEmpty()) {
            throw new CustomerNotFoundException(id);
        }

        return customer.get();
    }

    public void createCustomer(Customer customer) {
        Address homeAddress = customer.getHomeAddress();
        if (homeAddress != null) {
            addressRepository.save(homeAddress);
        }

        customerRepository.save(customer);
    }

    public Customer findCustomerByName(String firstName, String lastName) {
        if (hasLength(lastName)) {
            return customerRepository.findCustomerByFirstNameAndLastName(firstName, lastName);
        } else {
            return customerRepository.findCustomerByFirstNameOrLastName(firstName, firstName);
        }
    }

    public void updateCustomerAddress(Customer newCustomer) {
        Long customerId = newCustomer.getId();
        Customer customer = findCustomer(customerId);

        Address homeAddress = newCustomer.getHomeAddress();
        addressRepository.save(homeAddress);

        customer.setHomeAddress(homeAddress);
    }
}
