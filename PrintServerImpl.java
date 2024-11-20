import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class PrintServerImpl extends UnicastRemoteObject implements PrintServer {
    private Map<String, String> users = new HashMap<>(); // Username to hashed password
    private Map<String, Long> sessions = new HashMap<>(); // Username to session start time
    private final long SESSION_TIMEOUT = 5000; // 5 seconds for quick testing

    public PrintServerImpl() throws RemoteException {
        super();
        loadUsersFromFile("users.txt");
    }

    private void loadUsersFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    users.put(parts[0].trim(), parts[1].trim()); // Username, hashed password
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not found", e);
        }
    }

    private void authenticate(String username, String password) throws RemoteException {
        String hashedPassword = hashPassword(password);
        if (!users.containsKey(username) || !users.get(username).equals(hashedPassword)) {
            throw new RemoteException("Authentication failed. Invalid username or password.");
        }

        // Only set session if it doesn't already exist
        if (!sessions.containsKey(username)) {
            sessions.put(username, System.currentTimeMillis());
            System.out.println("Session created for " + username + " at " + System.currentTimeMillis());
        } else {
            System.out.println("Session already active for " + username);
        }
    }

    private void checkSession(String username) throws RemoteException {
        Long lastActive = sessions.get(username);
        if (lastActive == null) {
            System.out.println("No active session found for " + username);
        } else {
            long timeElapsed = System.currentTimeMillis() - lastActive;
            System.out.println("Time elapsed for session of " + username + ": " + timeElapsed + "ms");
        }

        if (lastActive == null || (System.currentTimeMillis() - lastActive) > SESSION_TIMEOUT) {
            System.out.println("Session expired for " + username);
            sessions.remove(username);
            throw new RemoteException("Session expired. Please reauthenticate.");
        }
    }

    @Override
    public void print(String filename, String printer, String username, String password) throws RemoteException {
        authenticate(username, password); // Authenticate user
        checkSession(username); // Validate session
        logAction("print", "User " + username + " printing file " + filename + " on printer " + printer);
    }

    @Override
    public String queue(String printer, String username, String password) throws RemoteException {
        authenticate(username, password); // Authenticate the user
        checkSession(username); // Validate the session
        logAction("queue", "User " + username + " fetching queue for printer " + printer);
        return "Queue for printer " + printer + ": [job1, job2]";
    }

    @Override
    public void topQueue(String printer, int job, String username, String password) throws RemoteException {
        authenticate(username, password); // Authenticate user
        checkSession(username); // Validate session
        logAction("topQueue", "User " + username + " moving job " + job + " to top of queue for printer " + printer);
    }

    @Override
    public void start(String username, String password) throws RemoteException {
        authenticate(username, password); // Authenticate user
        checkSession(username); // Validate session
        logAction("start", "User " + username + " starting the print server.");
    }

    @Override
    public void stop(String username, String password) throws RemoteException {
        authenticate(username, password); // Authenticate user
        checkSession(username); // Validate session
        logAction("stop", "User " + username + " stopping the print server.");
    }

    @Override
    public void restart(String username, String password) throws RemoteException {
        authenticate(username, password); // Authenticate user
        checkSession(username); // Validate session
        logAction("restart", "User " + username + " restarting the print server.");
    }

    @Override
    public String status(String printer, String username, String password) throws RemoteException {
        authenticate(username, password); // Authenticate user
        checkSession(username); // Validate session
        logAction("status", "User " + username + " checking status of printer " + printer);
        return "Status of printer " + printer + ": OK";
    }

    @Override
    public String readConfig(String parameter, String username, String password) throws RemoteException {
        authenticate(username, password); // Authenticate user
        checkSession(username); // Validate session
        logAction("readConfig", "User " + username + " reading config for parameter " + parameter);
        return "Value of " + parameter + ": 42";
    }

    @Override
    public void setConfig(String parameter, String value, String username, String password) throws RemoteException {
        authenticate(username, password); // Authenticate user
        checkSession(username); // Validate session
        logAction("setConfig", "User " + username + " setting parameter " + parameter + " to value " + value);
    }

    private void logAction(String action, String details) {
        try (java.io.FileWriter writer = new java.io.FileWriter("server.log", true)) {
            writer.write(action + ": " + details + "\n");
        } catch (IOException e) {
            System.err.println("Error writing to log: " + e.getMessage());
        }
    }
}
