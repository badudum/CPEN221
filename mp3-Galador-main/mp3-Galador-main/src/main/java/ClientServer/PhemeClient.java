package ClientServer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import security.AESCipher;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * FibonacciClient is a client that sends requests to the FibonacciServer
 * and interprets its replies.
 * A new FibonacciClient is "open" until the close() method is called,
 * at which point it is "closed" and may not be used further.
 */
public class PhemeClient {
    private static final List<JsonObject> requests = new ArrayList<>();
    private final AESCipher cipher;
    private final String key = "Many years later, as he faced the firing squad, Colonel Aureliano Buendía was to remember that distant afternoon when his father took him to discover ice.";
    private Socket socket;
    private BufferedReader in;
    // Rep invariant: socket, in, out != null
    private PrintWriter out;

    /**
     * Make a FibonacciClient and connect it to a server running on
     * hostname at the specified port.
     *
     * @throws IOException if can't connect
     */
    public PhemeClient(String hostname, int port) throws IOException {
        this.socket = new Socket(hostname, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.cipher = new AESCipher(key);
    }

    /**
     * All the requests to be completed
     */
    static {
        Gson gson = new Gson();
        //req 1 : add user1
        JsonObject req1 = new JsonObject();
        req1.addProperty("requestId", "1");
        req1.addProperty("operation", "addUser");
        JsonArray parameters1 = new JsonArray();
        parameters1.add(UUID.randomUUID().toString());
        parameters1.add("User1");
        parameters1.add("password1");
        req1.add("parameters", parameters1);
        requests.add(req1);
        //req 2 : add sub to "UBC" to user 1
        JsonObject req2 = new JsonObject();
        req2.addProperty("requestId", "2");
        req2.addProperty("operation", "addSubscription");
        JsonArray parameters2 = new JsonArray();
        parameters2.add("User1");
        parameters2.add("password1");
        parameters2.add("UBC");
        req2.add("parameters", parameters2);
        requests.add(req2);
        //req 3 : get next tweet
        JsonObject req3 = new JsonObject();
        req3.addProperty("requestId", "3");
        req3.addProperty("operation", "getNext");
        JsonArray parameters3 = new JsonArray();
        parameters3.add("User1");
        parameters3.add("password1");
        req3.add("parameters", parameters3);
        requests.add(req3);
    }

    /**
     * Use a PhemeServer to complete the requests
     */
    public static void main(String[] args) {
        try {
            PhemeClient client = new PhemeClient("127.0.0.1",
                    PhemeServer.PHEME_PORT);

            // send the requests to find the first N Fibonacci numbers
            for (JsonObject req: requests) {
                client.sendRequest(req.toString());
                System.out.println(req);
                String reply = client.getReply();
                System.out.println(reply);
            }

            client.close();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Send a request to the server. Requires this is "open".
     *
     * @param req the String of the request in Json Format
     * @throws IOException if network or server failure
     */
    public void sendRequest(String req) throws IOException {
        out.println(cipher.encrypt(req));
        out.flush();
    }

    /**
     * Get a reply from the next request that was submitted.
     * Requires this is "open".
     *
     * @return the requested Fibonacci number
     * @throws IOException if network or server failure
     */
    public String getReply() throws IOException {
        while(!in.ready()) {}
        String reply = in.readLine();
        if (reply == null) {
            throw new IOException("connection terminated unexpectedly");
        }

        try {
            return cipher.decrypt(reply);
            //return reply;
        }
        catch (NumberFormatException nfe) {
            throw new IOException("misformatted reply: " + reply);
        }
    }

    /**
     * Closes the client's connection to the server.
     * This client is now "closed". Requires this is "open".
     *
     * @throws IOException if close fails
     */
    public void close() throws IOException {
        //terminate PhemeServer Process
        out.println(cipher.encrypt("terminate"));
        out.flush();
        //close ports
        in.close();
        out.close();
        socket.close();
    }
}
