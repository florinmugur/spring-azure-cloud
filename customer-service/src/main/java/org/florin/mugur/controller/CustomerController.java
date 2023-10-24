package org.florin.mugur.controller;

import lombok.RequiredArgsConstructor;
import org.florin.mugur.model.entity.Customer;
import org.florin.mugur.service.CustomerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/customers")
    public List<Customer> getCustomers() {
        return customerService.findAllCustomers();
    }

    @GetMapping("/customer/{id}")
    public Customer getCustomer(@PathVariable("id") Long id) {
        return customerService.findCustomer(id);
    }

    @GetMapping("/customer")
    public Customer getCustomerByName(@RequestParam("firstName") String firstName,
                                      @RequestParam(value = "lastName", required = false) String lastName) {
        return customerService.findCustomerByName(firstName, lastName);
    }

    @PostMapping("/customer")
    public void createCustomer(@RequestBody Customer customer) {
        customerService.createCustomer(customer);
    }

    @PutMapping("/customer")
    public void updateCustomer(@RequestBody Customer customer) {
        customerService.updateCustomerAddress(customer);
    }
}
