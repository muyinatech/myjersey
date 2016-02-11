package muyinatech.myjersey.mongodb;

import com.mongodb.client.MongoDatabase;
import org.junit.Test;

import static org.junit.Assert.*;

public class DbConnectionTest {

    @Test
    public void main() {
        DbConnection.init();
        DbConnection.getCustomersCollection();
        DbConnection.close();
    }
}