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
    private final long SESSION_TIMEOUT = 300000; // 5 minutes in milliseconds

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

    @Override
    public boolean authenticate(String username, String password) throws RemoteException {
        String hashedPassword = hashPassword(password);
        if (users.containsKey(username) && users.get(username).equals(hashedPassword)) {
            sessions.put(username, System.currentTimeMillis());
            System.out.println("User " + username + " authenticated.");
            return true;
        }
        System.out.println("Authentication failed for user: " + username);
        return false;
    }

    private boolean isSessionValid(String username) {
        Long lastActive = sessions.get(username);
        if (lastActive != null && (System.currentTimeMillis() - lastActive) <= SESSION_TIMEOUT) {
            return true;
        } else {
            sessions.remove(username); // Remove expired session
            return false;
        }
    }

    @Override
    public void print(String filename, String printer) throws RemoteException {
        logAction("print", "Printing file " + filename + " on printer " + printer);
    }

    @Override
    public String queue(String printer) throws RemoteException {
        logAction("queue", "Fetching queue for printer " + printer);
        return "Queue for printer " + printer + ": [job1, job2]";
    }

    @Override
    public void topQueue(String printer, int job) throws RemoteException {
        logAction("topQueue", "Moving job " + job + " to top of queue for printer " + printer);
    }

    @Override
    public void start() throws RemoteException {
        logAction("start", "Starting the print server.");
    }

    @Override
    public void stop() throws RemoteException {
        logAction("stop", "Stopping the print server.");
    }

    @Override
    public void restart() throws RemoteException {
        logAction("restart", "Restarting the print server.");
    }

    @Override
    public String status(String printer) throws RemoteException {
        logAction("status", "Checking status for printer " + printer);
        return "Status of printer " + printer + ": OK";
    }

    @Override
    public String readConfig(String parameter) throws RemoteException {
        logAction("readConfig", "Reading config for parameter " + parameter);
        return "Value of " + parameter + ": 42";
    }

    @Override
    public void setConfig(String parameter, String value) throws RemoteException {
        logAction("setConfig", "Setting parameter " + parameter + " to value " + value);
    }

    private void logAction(String action, String details) {
        try (java.io.FileWriter writer = new java.io.FileWriter("server.log", true)) {
            writer.write(action + ": " + details + "\n");
        } catch (IOException e) {
            System.err.println("Error writing to log: " + e.getMessage());
        }
    }
}
