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
    private final long SESSION_TIMEOUT = 60000; // 60 000 sec timeout for testing
    private Map<String, List<String>> printQueues = new HashMap<>(); // Printer to job queue
    private Map<String, String> config = new HashMap<>(); // Printer configuration
    private Map<String, Set<String>> roles = new HashMap<>(); // Role to permissions
    private Map<String, String> userRoles = new HashMap<>(); // Username to role

    public PrintServerImpl() throws RemoteException {
        super();
        loadUsersFromFile("./-02239_Data_Security/users.txt");
        loadRolesFromFile("./-02239_Data_Security/roles.txt");
        loadUserRolesFromFile("./-02239_Data_Security/user_roles.txt");
        initializePrinters();
    }

    private void initializePrinters() {
        printQueues.put("Printer1", new ArrayList<>());
        printQueues.put("Printer2", new ArrayList<>());
        config.put("defaultPrinter", "Printer1");
    }

    private void loadUsersFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    users.put(parts[0].trim(), parts[1].trim()); // Load username and hashed password
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    private void loadRolesFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String role = parts[0].trim();
                    Set<String> permissions = new HashSet<>();
                    for (String perm : parts[1].split(",")) {
                        permissions.add(perm.trim());
                    }
                    roles.put(role, permissions);
                    System.out.println("Role loaded: " + role + " with permissions: " + permissions);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading roles: " + e.getMessage());
        }
    }

    private void loadUserRolesFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String user = parts[0].trim();
                    String role = parts[1].trim();
                    userRoles.put(user, role);
                    System.out.println("User loaded: " + user + " assigned to role: " + role);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading user roles: " + e.getMessage());
        }
    }

    private void enforceAccess(String username, String operation) throws RemoteException {
        String role = userRoles.get(username);
        System.out.println("Checking access for user: " + username + ", role: " + role + ", operation: " + operation);

        if (role == null) {
            System.err.println("Error: Role not assigned to user: " + username);
            throw new RemoteException("Access denied for operation: " + operation + " (Role not assigned)");
        }

        Set<String> permissions = roles.get(role);
        if (permissions == null) {
            System.err.println("Error: Permissions not defined for role: " + role);
            throw new RemoteException("Access denied for operation: " + operation + " (Role permissions missing)");
        }

        if (!permissions.contains(operation)) {
            System.err.println("Error: Operation " + operation + " not allowed for role: " + role);
            throw new RemoteException("Access denied for operation: " + operation);
        }
    }

    private void checkSession(String username) throws RemoteException {
        Long lastActive = sessions.get(username);
        if (lastActive == null || (System.currentTimeMillis() - lastActive) > SESSION_TIMEOUT) {
            sessions.remove(username);
            throw new RemoteException("Session expired. Please reauthenticate.");
        }
        sessions.put(username, System.currentTimeMillis()); // Renew session
    }

    @Override
    public void login(String username, String hashedPassword) throws RemoteException {
        if (!users.containsKey(username) || !users.get(username).equals(hashedPassword)) {
            throw new RemoteException("Authentication failed. Invalid username or password.");
        }
        sessions.put(username, System.currentTimeMillis());
        System.out.println("User " + username + " logged in successfully.");
    }

    @Override
    public String getAvailableCommandsForRole(String username) throws RemoteException {
        String role = userRoles.get(username);
        if (role == null || !roles.containsKey(role)) {
            throw new RemoteException("Role not found for user: " + username);
        }

        Set<String> commands = roles.get(role);
        return String.join(", ", commands) + ", logout";
    }

    @Override
    public void print(String filename, String printer, String username) throws RemoteException {
        checkSession(username);
        enforceAccess(username, "print");
        if (!printQueues.containsKey(printer)) {
            throw new RemoteException("Printer not found: " + printer);
        }
        printQueues.get(printer).add(filename);
        System.out.println("Added job to " + printer + ": " + filename);
    }

    @Override
    public String queue(String printer, String username) throws RemoteException {
        checkSession(username);
        enforceAccess(username, "queue");
        if (!printQueues.containsKey(printer)) {
            throw new RemoteException("Printer not found: " + printer);
        }
        List<String> jobs = printQueues.get(printer);
        StringBuilder result = new StringBuilder("Queue for " + printer + ":\n");
        for (int i = 0; i < jobs.size(); i++) {
            result.append((i + 1)).append(": ").append(jobs.get(i)).append("\n");
        }
        return result.toString();
    }

    @Override
    public void topQueue(String printer, int job, String username) throws RemoteException {
        checkSession(username);
        enforceAccess(username, "topqueue");
        if (!printQueues.containsKey(printer)) {
            throw new RemoteException("Printer not found: " + printer);
        }
        List<String> jobs = printQueues.get(printer);
        if (job < 1 || job > jobs.size()) {
            throw new RemoteException("Invalid job number.");
        }
        String topJob = jobs.remove(job - 1);
        jobs.add(0, topJob);
    }

    @Override
    public void start(String username) throws RemoteException {
        checkSession(username);
        enforceAccess(username, "start");
        System.out.println("Print server started.");
    }

    @Override
    public void stop(String username) throws RemoteException {
        checkSession(username);
        enforceAccess(username, "stop");
        System.out.println("Print server stopped.");
    }

    @Override
    public void restart(String username) throws RemoteException {
        checkSession(username);
        enforceAccess(username, "restart");
        for (String printer : printQueues.keySet()) {
            printQueues.get(printer).clear();
        }
        System.out.println("Print server restarted and queues cleared.");
    }

    @Override
    public String status(String printer, String username) throws RemoteException {
        checkSession(username);
        enforceAccess(username, "status");
        return "Status of " + printer + ": OK";
    }

    @Override
    public String readConfig(String parameter, String username) throws RemoteException {
        checkSession(username);
        enforceAccess(username, "readconfig");
        return config.getOrDefault(parameter, "Parameter not found");
    }

    @Override
    public void setConfig(String parameter, String value, String username) throws RemoteException {
        checkSession(username);
        enforceAccess(username, "setconfig");
        config.put(parameter, value);
        System.out.println("Config set: " + parameter + " = " + value);
    }

    @Override
    public void logout(String username) throws RemoteException {
        sessions.remove(username);
        System.out.println("User " + username + " logged out successfully.");
    }
}
