package muyinatech.myjersey.service;

import muyinatech.myjersey.Main;
import muyinatech.myjersey.domain.Customer;
import muyinatech.myjersey.mongodb.DbConnection;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
        DbConnection.init();
        // create the client
        Client c = ClientBuilder.newClient();

        target = c.target(Main.BASE_URI);
    }

    @After
    public void tearDown() throws Exception {
        DbConnection.close();
        server.shutdownNow();
    }

    @Test
    @Ignore
    public void shouldCreateAndRetrieveCustomer() {

        String firstName = "Fred";
        String lastName = "Wilson";

        Customer customer = new Customer();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);

        Entity<Customer> customerEntity = Entity.entity(customer, MediaType.APPLICATION_JSON_TYPE);
        Response response = target.path("customers").request().post(customerEntity);

        assertThat(response.getStatus(), is(Response.Status.CREATED.getStatusCode()));
        assertThat(response.getEntity(), is(notNullValue()));
        String customerId = response.readEntity(Customer.class).getId();


        Customer result = target.path("customers/" + customerId).request().get(Customer.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(customerId));
        assertThat(result.getFirstName(), is(firstName));
        assertThat(result.getLastName(), is(lastName));
    }

    @Test
    @Ignore
    public void shouldUpdateCustomer() {

        // Obtain customer
        Customer newCustomer = new Customer();
        newCustomer.setId("56bd028852a90329206364a3");
        newCustomer.setFirstName("Fred");
        newCustomer.setLastName("Wilson");
        newCustomer.setCity("London");

        // Update customer
        Entity<Customer> newCustomerEntity = Entity.entity(newCustomer, MediaType.APPLICATION_JSON_TYPE);
        Response result = target.path("customers/" +  "56bd028852a90329206364a3").request().put(newCustomerEntity);

        assertThat(result.getStatus(), is(Response.Status.NO_CONTENT.getStatusCode()));
    }

    @Test
    @Ignore
    public void shouldReturn404IfCustomerNotFoundForCreate() {
        Response response = target.path("customers/100").request().get();

        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    @Ignore
    public void shouldReturn404IfCustomerNotFoundForUpdate() {
        Customer customer = new Customer();
        customer.setFirstName("Some");
        customer.setLastName("Body");
        Entity<Customer> customerEntity = Entity.entity(customer, MediaType.APPLICATION_JSON_TYPE);

        Response response = target.path("customers/00bd028852a90329206364a3").request().put(customerEntity);

        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    @Ignore
    public void shouldReturn304IfCustomerNotModified() {

        Response response = target.path("customers/56bd028852a90329206364a3").request().get();

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(response.getHeaderString("Cache-Control"), is("private, no-store, no-transform, max-age=300"));

        String eTag = response.getEntityTag().toString();

        response = target.path("customers/56bd028852a90329206364a3").request().header("If-None-Match", eTag).get();
        assertThat(response.getStatus(), is(Response.Status.NOT_MODIFIED.getStatusCode()));
    }

}
