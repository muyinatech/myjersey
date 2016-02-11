package muyinatech.myjersey.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class DbConnection {
    private static MongoClient mongoClient;
    private static MongoDatabase mongoDatabase;

    public static void init() {

        mongoClient = new MongoClient();
        mongoDatabase = mongoClient.getDatabase("orderEntry");
    }

    public static MongoCollection<Document> getCustomersCollection() {
        return mongoDatabase.getCollection("customers");
    }

    public static void close() {
        mongoClient.close();
    }
}
