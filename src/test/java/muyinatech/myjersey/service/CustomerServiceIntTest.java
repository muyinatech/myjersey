package muyinatech.myjersey.service;

import muyinatech.myjersey.Main;
import muyinatech.myjersey.domain.Customer;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class CustomerServiceIntTest {
    private HttpServer server;
    private WebTarget target;

    @Before
    public void setUp() throws Exception {
        // start the server
        server = Main.startServer();
        // create the client
        Client c = ClientBuilder.newClient();

        target = c.target(Main.BASE_URI);
    }

    @After
    public void tearDown() throws Exception {
        server.shutdownNow();
    }

    @Test
    public void shouldCreateAndRetrieveCustomer() {

        String firstName = "Fred";
        String lastName = "Wilson";

        Customer customer = new Customer();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);

        Entity<Customer> customerEntity = Entity.entity(customer, MediaType.APPLICATION_JSON_TYPE);
        Response response = target.path("customers").request().post(customerEntity);

        assertThat(response.getStatus(), is(Response.Status.CREATED.getStatusCode()));
        assertThat(response.getLocation().toString(), is("http://localhost:8080/myapp/customers/1"));
        assertThat(response.getEntity(), is(notNullValue()));


        Customer result = target.path("customers/1").request().get(Customer.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(1));
        assertThat(result.getFirstName(), is(firstName));
        assertThat(result.getLastName(), is(lastName));
    }

    @Test
    public void shouldReturn404IfCustomerNotFound() {
        Response response = target.path("customers/100").request().get();

        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }
}
