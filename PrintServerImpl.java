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


    private void checkSession(String username) throws RemoteException {
        if (!isSessionValid(username)) {
            throw new RemoteException("Session expired. Please reauthenticate.");
        }
    }

    @Override
    public void print(String filename, String printer, String username) throws RemoteException {
        checkSession(username); // Validate the session before processing
        logAction("print", "User " + username + " printing file " + filename + " on printer " + printer);
    }

    @Override
    public String queue(String printer, String username) throws RemoteException {
        checkSession(username); // Validate the session before processing
        logAction("queue", "User " + username + " fetching queue for printer " + printer);
        return "Queue for printer " + printer + ": [job1, job2]";
    }

    @Override
    public void topQueue(String printer, int job, String username) throws RemoteException {
        checkSession(username); // Validate the session before processing
        logAction("topQueue", "User " + username + " moving job " + job + " to top of queue for printer " + printer);
    }

    @Override
    public void start(String username) throws RemoteException {
        checkSession(username); // Validate the session before processing
        logAction("start", "User " + username + " starting the print server.");
    }

    @Override
    public void stop(String username) throws RemoteException {
        checkSession(username); // Validate the session before processing
        logAction("stop", "User " + username + " stopping the print server.");
    }

    @Override
    public void restart(String username) throws RemoteException {
        checkSession(username); // Validate the session before processing
        logAction("restart", "User " + username + " restarting the print server.");
    }

    @Override
    public String status(String printer, String username) throws RemoteException {
        checkSession(username); // Validate the session before processing
        logAction("status", "User " + username + " checking status of printer " + printer);
        return "Status of printer " + printer + ": OK";
    }

    @Override
    public String readConfig(String parameter, String username) throws RemoteException {
        checkSession(username); // Validate the session before processing
        logAction("readConfig", "User " + username + " reading config for parameter " + parameter);
        return "Value of " + parameter + ": 42";
    }

    @Override
    public void setConfig(String parameter, String value, String username) throws RemoteException {
        checkSession(username); // Validate the session before processing
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
