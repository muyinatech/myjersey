package muyinatech.myjersey.service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import muyinatech.myjersey.Main;
import muyinatech.myjersey.domain.Customer;
import muyinatech.myjersey.mongodb.DbConnection;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;

import static com.mongodb.client.model.Filters.eq;


@Path("/customers")
public class CustomerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);
    public static final String ID = "_id";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCustomer(Customer customer) throws IOException {

        MongoDatabase mongoDatabase = DbConnection.getMongoDatabase();
        Document document = getDocument(customer);
        mongoDatabase.getCollection("customers").insertOne(document);
        ObjectId id = (ObjectId) document.get(ID);

        LOGGER.info("Created customer - " + id);
        return Response.created(URI.create("/" + Main.PATH + "/customers/" + id)).entity(customer).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Customer getCustomer(@PathParam("id") String id) {
        try {
            BasicDBObject objectId = new BasicDBObject("_id", new ObjectId(id));
            Document document = DbConnection.getMongoDatabase()
                    .getCollection("customers")
                    .find(objectId)
                    .first();

            if (document == null) {
                LOGGER.error("Customer {id=" + id + " } does not exist.");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            return getCustomer(document);
        } catch(IllegalArgumentException e) {
            LOGGER.error("Invalid id format in request.",  e);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateCustomer(@PathParam("id") int id, Customer customer) {

        Document document = getDocument(customer);

        UpdateResult updateResult = DbConnection.getMongoDatabase()
                .getCollection("customers")
                .updateOne(eq("_id", id), document);
        if (updateResult.getModifiedCount() == 0) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
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

}
