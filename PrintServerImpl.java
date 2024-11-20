import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.util.*;

public class PrintServerImpl extends UnicastRemoteObject implements PrintServer {

    private Map<String, String> users = new HashMap<>(); // Username to hashed password
    private Map<String, Long> sessions = new HashMap<>(); // Username to session timestamps
    private final long SESSION_TIMEOUT = 15000; // 5 seconds for testing
    private Map<String, List<String>> printQueues = new HashMap<>(); // Printer to job queue
    private Map<String, String> config = new HashMap<>(); // Printer configuration

    public PrintServerImpl() throws RemoteException {
        super();
        // Initialize printers
        printQueues.put("Printer1", new ArrayList<>());
        printQueues.put("Printer2", new ArrayList<>());
        config.put("defaultPrinter", "Printer1");

        // Load users from file
        loadUsersFromFile("./-02239_Data_Security/users.txt");
    }

    private void loadUsersFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    users.put(parts[0].trim(), parts[1].trim()); // Username, hashed password
                    System.out.println("Loaded user: " + parts[0].trim() + " with hash " + parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
        System.out.println("Users loaded: " + users.keySet());
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
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private void authenticate(String username, String password) throws RemoteException {
        if (!users.containsKey(username)) {
            System.out.println("Authentication failed: Username not found - " + username);
            throw new RemoteException("Authentication failed. Invalid username or password.");
        }
        String hashedPassword = hashPassword(password); // Hash the input password
        System.out.println("Stored hash: " + users.get(username)); // Debug stored hash
        System.out.println("Input hash: " + hashedPassword); // Debug computed hash
        if (!users.get(username).equals(hashedPassword)) {
            System.out.println("Authentication failed: Incorrect password for - " + username);
            throw new RemoteException("Authentication failed. Invalid username or password.");
        }
        System.out.println("Authentication successful for user: " + username);
        if (!sessions.containsKey(username)) {
            sessions.put(username, System.currentTimeMillis());
        }
    }

    private void checkSession(String username) throws RemoteException {
        Long lastActive = sessions.get(username);
        if (lastActive == null || (System.currentTimeMillis() - lastActive) > SESSION_TIMEOUT) {
            sessions.remove(username);
            throw new RemoteException("Session expired. Please reauthenticate.");
        }
        sessions.put(username, System.currentTimeMillis());
    }

    @Override
    public void print(String filename, String printer, String username, String password) throws RemoteException {
        authenticate(username, password);
        checkSession(username);
        if (!printQueues.containsKey(printer)) {
            throw new RemoteException("Printer not found: " + printer);
        }
        printQueues.get(printer).add(filename);
        System.out.println("Added job to " + printer + ": " + filename);
    }

    @Override
    public String queue(String printer, String username, String password) throws RemoteException {
        authenticate(username, password);
        checkSession(username);
        if (!printQueues.containsKey(printer)) {
            throw new RemoteException("Printer not found: " + printer);
        }
        List<String> jobs = printQueues.get(printer);
        StringBuilder result = new StringBuilder("Queue for printer " + printer + ":\n");
        for (int i = 0; i < jobs.size(); i++) {
            result.append(i + 1).append(" ").append(jobs.get(i)).append("\n");
        }
        return result.toString();
    }

    @Override
    public void topQueue(String printer, int job, String username, String password) throws RemoteException {
        authenticate(username, password);
        checkSession(username);
        if (!printQueues.containsKey(printer)) {
            throw new RemoteException("Printer not found: " + printer);
        }
        List<String> jobs = printQueues.get(printer);
        if (job < 1 || job > jobs.size()) {
            throw new RemoteException("Invalid job number.");
        }
        String topJob = jobs.remove(job - 1);
        jobs.add(0, topJob);
        System.out.println("Moved job to top: " + topJob);
    }

    @Override
    public void start(String username, String password) throws RemoteException {
        authenticate(username, password);
        checkSession(username);
        System.out.println("Print server started.");
    }

    @Override
    public void stop(String username, String password) throws RemoteException {
        authenticate(username, password);
        checkSession(username);
        System.out.println("Print server stopped.");
    }

    @Override
    public void restart(String username, String password) throws RemoteException {
        authenticate(username, password);
        checkSession(username);
        for (String printer : printQueues.keySet()) {
            printQueues.get(printer).clear();
        }
        System.out.println("Print server restarted and queues cleared.");
    }

    @Override
    public String status(String printer, String username, String password) throws RemoteException {
        authenticate(username, password);
        checkSession(username);
        if (!printQueues.containsKey(printer)) {
            throw new RemoteException("Printer not found: " + printer);
        }
        return "Status of printer " + printer + ": OK";
    }

    @Override
    public String readConfig(String parameter, String username, String password) throws RemoteException {
        authenticate(username, password);
        checkSession(username);
        return config.getOrDefault(parameter, "Parameter not found: " + parameter);
    }

    @Override
    public void setConfig(String parameter, String value, String username, String password) throws RemoteException {
        authenticate(username, password);
        checkSession(username);
        config.put(parameter, value);
        System.out.println("Config set: " + parameter + " = " + value);
    }

    @Override
    public void logout(String username, String password) throws RemoteException {
        authenticate(username, password);
        sessions.remove(username);
        System.out.println("User " + username + " logged out successfully.");
    }
}
