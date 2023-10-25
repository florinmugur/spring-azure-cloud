package org.florin.mugur;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.florin.mugur.configuration.SecurityConfiguration;
import org.florin.mugur.model.entity.Address;
import org.florin.mugur.model.entity.Customer;
import org.florin.mugur.model.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.List;

import static org.florin.mugur.configuration.SecurityConfiguration.AUTH_TOKEN_HEADER_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = {CustomerServiceApplication.class, SecurityConfiguration.class},
        properties = {"API_KEY=integration_test"})
@TestPropertySource(
        locations = "classpath:application-integration.yml")
class CustomerServiceIntegrationTests {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MockMvc mvc;

    @Test
    void testNotAuthenticatedRequest() throws Exception {
        try {
            mvc.perform(get("/customers"));

            throw new RuntimeException("Request should not work without security header!");
        } catch (ServletException ex) {
            assertEquals("Access Denied", ex.getCause().getMessage());
        }
    }

    @Test
    void testGetCustomers() throws Exception {
        assertEquals(0, customerRepository.findAll().size());

        Address homeAddress = createAddress("Integration", 1, 123456, "Test");
        createCustomer("Get", "Customers", 23, homeAddress);

        mvc.perform(get("/customers")
                        .header(AUTH_TOKEN_HEADER_NAME, "integration_test"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$[0].firstName", is("Get")))
                .andExpect(jsonPath("$[0].lastName", is("Customers")))
                .andExpect(jsonPath("$[0].age", is(23)))
                .andExpect(jsonPath("$[0].homeAddress.streetName", is("Integration")))
                .andExpect(jsonPath("$[0].homeAddress.houseNumber", is(1)))
                .andExpect(jsonPath("$[0].homeAddress.postalCode", is(123456)))
                .andExpect(jsonPath("$[0].homeAddress.country", is("Test")));
    }

    @Test
    void testCustomerNotFound() throws Exception {
        assertEquals(0, customerRepository.findAll().size());

        int customerId = 10;

        mvc.perform(get("/customer/" + customerId)
                        .header(AUTH_TOKEN_HEADER_NAME, "integration_test"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Customer with id " + customerId + " was not found!"));
    }

    @Test
    void testGetCustomer() throws Exception {
        assertEquals(0, customerRepository.findAll().size());

        Address homeAddress = createAddress("Integration", 2, 654321, "Test");
        Customer customer = createCustomer("Get", "Customer", 2, homeAddress);

        mvc.perform(get("/customer/" + customer.getId())
                        .header(AUTH_TOKEN_HEADER_NAME, "integration_test"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("firstName", is("Get")))
                .andExpect(jsonPath("lastName", is("Customer")))
                .andExpect(jsonPath("age", is(2)))
                .andExpect(jsonPath("homeAddress.streetName", is("Integration")))
                .andExpect(jsonPath("homeAddress.houseNumber", is(2)))
                .andExpect(jsonPath("homeAddress.postalCode", is(654321)))
                .andExpect(jsonPath("homeAddress.country", is("Test")));
    }

    @Test
    void testFindCustomerByBothNames() throws Exception {
        assertEquals(0, customerRepository.findAll().size());

        Address homeAddress = createAddress("Integration", 3, 3333, "Test");
        createCustomer("Find", "Both", 3, homeAddress);

        mvc.perform(get("/customer")
                        .header(AUTH_TOKEN_HEADER_NAME, "integration_test")
                        .param("firstName", "Find")
                        .param("lastName", "Both"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("firstName", is("Find")))
                .andExpect(jsonPath("lastName", is("Both")))
                .andExpect(jsonPath("age", is(3)))
                .andExpect(jsonPath("homeAddress.streetName", is("Integration")))
                .andExpect(jsonPath("homeAddress.houseNumber", is(3)))
                .andExpect(jsonPath("homeAddress.postalCode", is(3333)))
                .andExpect(jsonPath("homeAddress.country", is("Test")));
    }

    @Test
    void testFindCustomerByFirstName() throws Exception {
        assertEquals(0, customerRepository.findAll().size());

        Address homeAddress = createAddress("Integration", 4, 4444, "Test");
        createCustomer("Find", "First", 4, homeAddress);

        mvc.perform(get("/customer")
                        .header(AUTH_TOKEN_HEADER_NAME, "integration_test")
                        .param("firstName", "First"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("firstName", is("Find")))
                .andExpect(jsonPath("lastName", is("First")))
                .andExpect(jsonPath("age", is(4)))
                .andExpect(jsonPath("homeAddress.streetName", is("Integration")))
                .andExpect(jsonPath("homeAddress.houseNumber", is(4)))
                .andExpect(jsonPath("homeAddress.postalCode", is(4444)))
                .andExpect(jsonPath("homeAddress.country", is("Test")));
    }

    @Test
    void testCreateCustomer() throws Exception {
        assertEquals(0, customerRepository.findAll().size());

        Address homeAddress = createAddress("Integration", 5, 5555, "Test");
        Customer customer = createCustomer("Create", "Customer", 5, homeAddress, false);

        mvc.perform(post("/customer")
                        .header(AUTH_TOKEN_HEADER_NAME, "integration_test")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(customer)))
                .andExpect(status().isOk());

        List<Customer> customers = customerRepository.findAll();
        assertEquals(1, customers.size());

        Customer dbCustomer = customers.get(0);

        assertNotNull(dbCustomer);
        assertEquals("Create", dbCustomer.getFirstName());
        assertEquals("Customer", dbCustomer.getLastName());
        assertEquals(5, dbCustomer.getAge());

        Address dbAddress = dbCustomer.getHomeAddress();
        assertEquals("Integration", dbAddress.getStreetName());
        assertEquals(5, dbAddress.getHouseNumber());
        assertEquals(5555, dbAddress.getPostalCode());
        assertEquals("Test", dbAddress.getCountry());
    }

    @Test
    void testCreateCustomerWithoutAddress() throws Exception {
        assertEquals(0, customerRepository.findAll().size());

        Customer customer = createCustomer("No", "Address", 8, null, false);

        mvc.perform(post("/customer")
                        .header(AUTH_TOKEN_HEADER_NAME, "integration_test")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(customer)))
                .andExpect(status().isOk());

        List<Customer> customers = customerRepository.findAll();
        assertEquals(1, customers.size());

        Customer dbCustomer = customers.get(0);

        assertNotNull(dbCustomer);
        assertEquals("No", dbCustomer.getFirstName());
        assertEquals("Address", dbCustomer.getLastName());
        assertEquals(8, dbCustomer.getAge());

        assertNull(dbCustomer.getHomeAddress());
    }

    @Test
    void testUpdateCustomerAddress() throws Exception {
        assertEquals(0, customerRepository.findAll().size());

        Address homeAddress = createAddress("Integration", 6, 6666, "Test");
        Customer customer = createCustomer("Update", "Customer", 6, homeAddress);

        assertEquals(1, customerRepository.findAll().size());

        Address newHomeAddress = createAddress("Test", 7, 7777, "Integration");
        Customer newCustomer = createCustomer("Update", "Customer", 6, newHomeAddress, false);
        newCustomer.setId(customer.getId());

        mvc.perform(put("/customer")
                        .header(AUTH_TOKEN_HEADER_NAME, "integration_test")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(newCustomer)))
                .andExpect(status().isOk());

        List<Customer> customers = customerRepository.findAll();
        assertEquals(1, customers.size());

        Customer dbCustomer = customers.get(0);

        assertNotNull(dbCustomer);
        assertEquals("Update", dbCustomer.getFirstName());
        assertEquals("Customer", dbCustomer.getLastName());
        assertEquals(6, dbCustomer.getAge());

        Address dbAddress = dbCustomer.getHomeAddress();
        assertEquals("Test", dbAddress.getStreetName());
        assertEquals(7, dbAddress.getHouseNumber());
        assertEquals(7777, dbAddress.getPostalCode());
        assertEquals("Integration", dbAddress.getCountry());
    }

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
    }

    byte[] toJson(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsBytes(object);
    }

    private Customer createCustomer(String firstName, String lastName, Integer age, Address homeAddress) {
        return createCustomer(firstName, lastName, age, homeAddress, true);
    }

    private Customer createCustomer(String firstName, String lastName, Integer age, Address homeAddress, boolean persist) {
        Customer customer = new Customer();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setAge(age);

        customer.setHomeAddress(homeAddress);

        if (persist) {
            customerRepository.saveAndFlush(customer);
        }

        return customer;
    }

    private Address createAddress(String streetName, Integer houseNumber, Integer postalCode, String country) {
        Address homeAddress = new Address();
        homeAddress.setStreetName(streetName);
        homeAddress.setCountry(country);
        homeAddress.setHouseNumber(houseNumber);
        homeAddress.setPostalCode(postalCode);

        return homeAddress;
    }
}
