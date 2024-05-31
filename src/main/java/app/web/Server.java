package app.web;

import spark.Spark;

import java.util.HashMap;
import java.util.Map;

public class Server {

    private final String serverAddress = "localhost";
    private int serverPort;
    private int id = 1;
    private final Map<String, String> clientFiguresMap = new HashMap<>();

    public static void main(String[] args) {
        Server server = new Server(1235);
    }

    public Server(int port) {
        this.serverPort = port;
        initServer();
    }

    private void initServer() {
        Spark.ipAddress(serverAddress);
        Spark.port(serverPort);
        initRegMethod();
        initPostMethod();
        initGetMethod();
        initDeleteMethod();
        Spark.init();
    }

    private void initRegMethod() {
        Spark.post("/register", (request, response) -> {
            String clientId = "client" + id++;
            clientFiguresMap.put(clientId, "");
            response.status(201);
            response.type("text/plain");
            logMap("Register method");
            return clientId;
        });
    }

    private void initPostMethod() {
        Spark.post("/figures/:clientId", (request, response) -> {
            String clientId = request.params(":clientId");
            if (!clientFiguresMap.containsKey(clientId)) {
                response.status(404);
                return "Client " + clientId + " haven't been found";
            }
            String xml = request.body();
            clientFiguresMap.put(clientId, xml);
            logMap("Post method");
            response.status(201);
            return "Figures uploaded for client " + clientId;
        });
    }

    private void initGetMethod() {
        Spark.get("/figures/:clientId",  (request, response) -> {
            String clientId = request.params(":clientId");
            String xml = clientFiguresMap.get(clientId);
            if (xml != null) {
                return xml;
            } else {
                response.status(404);
                return "Client " + clientId + " haven't been found";
            }
        });
    }

    private void initDeleteMethod() {
        Spark.delete("/figures/:clientId", (request, response) -> {
            String clientId = request.params(":clientId");
            String xml = clientFiguresMap.remove(clientId);
            if (xml != null) {
                logMap("Delete method");
                return "Info for client " + clientId + " have been deleted";
            } else {
                response.status(404);
                return "Client " + clientId + " haven't been found";
            }
        });
    }

    private void logMap(String methodName) {
        System.out.println(methodName + ":");
        clientFiguresMap.forEach((key,value)->{
            String valueString = value.replaceAll("(\\n)", "");
            System.out.println("[\n  Key: " + key + ",\n  value: " + valueString + "\n]");
        });
        System.out.println("\n");
    }

}
