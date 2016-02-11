package muyinatech.myjersey;

import muyinatech.myjersey.mongodb.DbConnection;
import muyinatech.myjersey.service.CustomerService;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

/**
 * Main class.
 *
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI;
    public static final String PROTOCOL;
    public static final Optional<String> HOST;
    public static final String PATH;
    public static final Optional<String> PORT;

    static{
        PROTOCOL = "http://";
        HOST = Optional.ofNullable(System.getenv("HOSTNAME"));
        PORT = Optional.ofNullable(System.getenv("PORT"));
        PATH = "myapp";
        BASE_URI = PROTOCOL + HOST.orElse("localhost") + ":" + PORT.orElse("8080") + "/" + PATH + "/";
    }

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in com.example.rest package
        final ResourceConfig rc = new ResourceConfig().registerInstances(new CustomerService());//packages("muyinatech.myjersey"); // singleton for now until db is created

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    /**
     * Main method.
     * @throws IOException
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        DbConnection.init();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
        System.in.read();
        DbConnection.close();
        server.shutdownNow();
    }
}

