package ClientServer;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.github.redouane59.twitter.dto.user.User;
import pheme.PhemeService;
import security.AESCipher;
import timedelayqueue.PubSubMessage;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PhemeServer {
	/** Default port number where the server listens for connections. */
	public static final int PHEME_PORT = 4949;
	private final ServerSocket serverSocket;
	private final PhemeService service;
	private final String key = "Many years later, as he faced the firing squad, Colonel Aureliano Buendía was to remember that distant afternoon when his father took him to discover ice.";

	// Rep invariant: serverSocket != null
	//
	// Thread safety argument:
	//

	/**
	 * Make a FibonacciServerMulti that listens for connections on port.
	 * 
	 * @param port port number, requires 0 <= port <= 65535
	 */
	public PhemeServer(int port, File credentials) throws IOException {
		this.serverSocket = new ServerSocket(port);
		this.service = new PhemeService(credentials);
	}

	/**
	 * Run the server, listening for connections and handling them.
	 * 
	 * @throws IOException
	 *             if the main server socket is broken
	 */
	public void serve() throws IOException {
		while (true) {
			// block until a client connects
			final Socket socket = serverSocket.accept();
			// create a new thread to handle that client
			Thread handler = new Thread(() -> {
				try {
					try {
						handle(socket);
					} finally {
						socket.close();
					}
				} catch (IOException ioe) {
					// this exception wouldn't terminate serve(),
					// since we're now on a different thread, but
					// we still need to handle it
					ioe.printStackTrace();
				}
			});
			// start the thread
			handler.start();
		}
	}

	/**
	 * Handle one client connection. Returns when client disconnects.
	 * 
	 * @param socket
	 *            socket where client is connected
	 * @throws IOException
	 *             if connection encounters an error
	 */
	private void handle(Socket socket) throws IOException {
		System.out.println("Client Connected: " + socket);
		while (socket.isConnected()) {
			try {
				// Create a buffered reader to read the incoming JSON data from the client
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				//wait for request
				while (!in.ready()) { }
				// Read the incoming JSON data from the client
				String encryptedJson = in.readLine();
				AESCipher cipher = new AESCipher(key);
				//decrypt the encrypted message
				String json = cipher.decrypt(encryptedJson);

				if (json.equals("terminate")) {
					socket.close();
					break;
				}
				System.out.println("Received JSON data from client: " + json);

				// Parse the JSON data into a JsonObject
				Gson gson = new Gson();
				JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

				//Save the current request ID and operation
				String currReqID = jsonObject.get("requestId").getAsString();
				String operation = jsonObject.get("operation").getAsString();
				//create json object to be returned.
				JsonObject returnJson = new JsonObject();
				returnJson.addProperty("requestID", currReqID);
				switch (operation) {
					case "addUser" -> {
						List<JsonElement> parameters = jsonObject.get("parameters").getAsJsonArray().asList();
						//get parameters
						UUID userID = gson.fromJson(parameters.get(0), UUID.class);
						String userName = parameters.get(1).getAsString();
						String password = parameters.get(2).getAsString();
						//compute operation
						Boolean success = service.addUser(userID, userName, password);
						//encode result
						returnJson.addProperty("success", success);
						returnJson.addProperty("response", success);
					}
					case "removeUser" -> {
						List<JsonElement> parameters = jsonObject.get("parameters").getAsJsonArray().asList();
						//get parameters
						String userName = parameters.get(0).getAsString();
						String password = parameters.get(1).getAsString();
						//compute operation
						Boolean success = service.removeUser(userName, password);
						//encode result
						returnJson.addProperty("success", success);
						returnJson.addProperty("response", success);
					}
					case "addSubscription" -> {
						List<JsonElement> parameters = jsonObject.get("parameters").getAsJsonArray().asList();
						//get username, password, and handle
						String userName = parameters.get(0).getAsString();
						String password = parameters.get(1).getAsString();
						String handle = parameters.get(2).getAsString();
						boolean success;
						if (parameters.size() == 4) {
							String pattern = parameters.get(3).getAsString();
							success = service.addSubscription(userName, password, handle, pattern);
						} else {
							success = service.addSubscription(userName, password, handle);
						}
						returnJson.addProperty("success", success);
						returnJson.addProperty("response", success);
					}
					case "removeSubscription" -> {
						List<JsonElement> parameters = jsonObject.get("parameters").getAsJsonArray().asList();
						//get username, password, and handle
						String userName = parameters.get(0).getAsString();
						String password = parameters.get(1).getAsString();
						String handle = parameters.get(2).getAsString();
						boolean success;
						if (parameters.size() == 4) {
							String pattern = parameters.get(3).getAsString();
							success = service.cancelSubscription(userName, password, handle, pattern);
						} else {
							success = service.cancelSubscription(userName, password, handle);
						}
						returnJson.addProperty("success", success);
						returnJson.addProperty("response", success);
					}
					case "sendMessage" -> {
						List<JsonElement> parameters = jsonObject.get("parameters").getAsJsonArray().asList();
						//get username, password, and handle
						String userName = parameters.get(0).getAsString();
						String password = parameters.get(1).getAsString();
						PubSubMessage msg = gson.fromJson(parameters.get(2), PubSubMessage.class);
						boolean success = service.sendMessage(userName, password, msg);
						returnJson.addProperty("success", success);
						returnJson.addProperty("response", success);
					}
					case "isDelivered" -> {
						List<JsonElement> parameters = jsonObject.get("parameters").getAsJsonArray().asList();
						UUID msgID = gson.fromJson(parameters.get(0), UUID.class);
						Type listType = new TypeToken<List<UUID>>() {
						}.getType();
						List<UUID> userList = gson.fromJson(parameters.get(1), listType);
						List<Boolean> returnList = service.isDelivered(msgID, userList);
						returnJson.addProperty("success", returnList.stream().allMatch(x -> true));
						returnJson.addProperty("response", gson.toJson(returnList));
					}
					case "getNext" -> {
						List<JsonElement> parameters = jsonObject.get("parameters").getAsJsonArray().asList();
						//get username, password, and handle
						String userName = parameters.get(0).getAsString();
						String password = parameters.get(1).getAsString();
						PubSubMessage msg = service.getNext(userName, password);
						returnJson.addProperty("success", !msg.equals(PubSubMessage.NO_MSG));
						returnJson.addProperty("response", gson.toJson(msg));
					}
					case "getAllRecent" -> {
						List<JsonElement> parameters = jsonObject.get("parameters").getAsJsonArray().asList();
						//get username, password, and handle
						String userName = parameters.get(0).getAsString();
						String password = parameters.get(1).getAsString();
						List<PubSubMessage> msgs = service.getAllRecent(userName, password);
						returnJson.addProperty("success", msgs.size() > 0);
						returnJson.addProperty("response", gson.toJson(msgs));
					}
					default -> {
						returnJson.addProperty("success", false);
						returnJson.addProperty("response", "Error: unrecognized operation");
					}
				}
				// Convert the modified JSON data back into a string and encrypt it
				String unencryptedJson = returnJson.toString();
				json = cipher.encrypt(unencryptedJson);

				// Create a print writer to send the JSON data back to the client
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

				// Send the modified JSON data back to the client
				out.println(json);
				out.flush();
				System.out.println("Sent JSON data to client: " + json);
			} catch (IOException e) {
				// Print the stack trace if an I/O error occurs
				e.printStackTrace();
			}
		}
		System.out.println("Client Disconnected: " + socket);
	}

	public static void main(String[] args) {
		try {
			PhemeServer server = new PhemeServer(
					PHEME_PORT, new File("secret/credentials.json"));
			server.serve();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
