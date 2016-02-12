package muyinatech.myjersey.service;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.result.UpdateResult;
import muyinatech.myjersey.Main;
import muyinatech.myjersey.domain.Customer;
import muyinatech.myjersey.mongodb.DbConnection;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;


@Path("/customers")
public class CustomerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);
    private static final String ID = "_id";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response createCustomer(Customer customer) throws IOException {

        Document document = getDocument(customer);
        DbConnection.getCustomersCollection().insertOne(document);
        ObjectId id = (ObjectId) document.get(ID);

        Customer newCustomer = getCustomer(document);

        LOGGER.info("Created customer - " + id);

        return Response.created(URI.create("/" + Main.PATH + "/customers/" + id))
                .entity(newCustomer)
                .build();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<Customer> getCustomers() {
        final List<Customer> customers = new ArrayList<>();
        FindIterable<Document> documents = DbConnection.getCustomersCollection().find();
        documents.forEach((Block<Document>) document -> customers.add(getCustomer(document)));
        return customers;
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCustomer(@PathParam("id") String id, @Context Request request) {
        try {
            BasicDBObject objectId = new BasicDBObject("_id", new ObjectId(id));
            Document document = DbConnection.getCustomersCollection()
                    .find(objectId)
                    .first();

            if (document == null) {
                LOGGER.error("Customer {id=" + id + " } does not exist.");
                throw new NotFoundException();
            }

            Customer customer = getCustomer(document);
            EntityTag etag = new EntityTag(Integer.toString(customer.hashCode())); // compute E-Tag

            Response.ResponseBuilder builder = request.evaluatePreconditions(etag);

            if (builder == null) { // cached resource did change -> serve updated content
                builder = Response.ok(customer);
                builder.tag(etag);
            }

            return builder.cacheControl(getCacheControl()).build();

        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid id format in request.", e);
            throw new NotFoundException();
        }

    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateCustomer(@PathParam("id") int id, Customer customer) {

        Document document = getDocument(customer);

        UpdateResult updateResult = DbConnection.getCustomersCollection()
                .updateOne(eq("_id", id), document);
        if (updateResult.getModifiedCount() == 0) {
            throw new NotFoundException();
        }
    }

    private Document getDocument(Customer customer) {
        Document customerDocument = new Document();
        customerDocument.append(FIRST_NAME, customer.getFirstName());
        customerDocument.append(LAST_NAME, customer.getLastName());
        return customerDocument;
    }

    private Customer getCustomer(Document document) {
        Customer customer = new Customer();
        customer.setId(document.get(ID).toString());
        customer.setFirstName(document.get(FIRST_NAME).toString());
        customer.setLastName(document.get(LAST_NAME).toString());
        return customer;
    }

    private CacheControl getCacheControl() {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setMaxAge(300); // response is valid for 5 mins
        cacheControl.setPrivate(true); // only the client can cache the response, no intermediary such as CDN
        cacheControl.setNoStore(true); // response should not be stored on disk
        return cacheControl;
    }

}
