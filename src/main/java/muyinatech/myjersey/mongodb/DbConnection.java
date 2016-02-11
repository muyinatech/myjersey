package muyinatech.myjersey.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class DbConnection {
    private static MongoClient mongoClient;
    private static MongoDatabase mongoDatabase;

    public static void init() {

        mongoClient = new MongoClient();
        mongoDatabase = mongoClient.getDatabase("orderEntry");
    }

    public static MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    public static void close() {
        mongoClient.close();
    }
}
