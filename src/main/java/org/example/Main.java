package org.example;

import java.io.IOException;
import java.util.Scanner;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class Main {
    public static void main(String[] args) {

        System.setProperty("server.port", "8082");

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        scanner.close();

        if (!authenticate(username, password)) {
            System.out.println("Authentication failed. Exiting...");
            return;
        }

        readDataFromMongoDB();
    }

    private static boolean authenticate(String username, String password) {
        String authUrl = "http://auth-service:8081/auth";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(authUrl);
            httpPost.setHeader("Content-Type", "application/json");

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode json = mapper.createObjectNode();
            json.put("username", username);
            json.put("password", password);
            String jsonString = mapper.writeValueAsString(json);

            httpPost.setEntity(new StringEntity(jsonString));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                return statusCode == 200;
            }
        } catch (IOException e) {
            System.err.println("Authentication error: " + e.getMessage());
            return false;
        }
    }

    private static void readDataFromMongoDB() {
        String connectionString = "mongodb://localhost:27017";
        String databaseName = "test";
        String collectionName = "data";

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection(collectionName);

            try (MongoCursor<Document> cursor = collection.find().iterator()) {
                System.out.println("Data from MongoDB:");
                while (cursor.hasNext()) {
                    Document doc = cursor.next();
                    System.out.println(doc.toJson());
                }
            }
        } catch (Exception e) {
            System.err.println("MongoDB error: " + e.getMessage());
        }
    }
}