import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class PrintServerImpl extends UnicastRemoteObject implements PrintServer {
    private Map<String, String> users = new HashMap<>();
    private Map<String, Long> sessions = new HashMap<>();
    private final long SESSION_TIMEOUT = 300000; // 5 minutes in milliseconds

    public PrintServerImpl() throws RemoteException {
        super();
        users.put("user1", "password1"); // Predefined users for simplicity
        users.put("admin", "adminpass");
    }

    @Override
    public boolean authenticate(String username, String password) throws RemoteException {
        if (users.containsKey(username) && users.get(username).equals(password)) {
            sessions.put(username, System.currentTimeMillis());
            System.out.println("User " + username + " authenticated.");
            return true;
        }
        System.out.println("Authentication failed for user: " + username);
        return false;
    }

    private boolean isSessionValid(String username) {
        Long lastActive = sessions.get(username);
        return lastActive != null && (System.currentTimeMillis() - lastActive) <= SESSION_TIMEOUT;
    }

    @Override
    public void print(String filename, String printer) throws RemoteException {
        System.out.println("Printing file " + filename + " on printer " + printer);
    }

    @Override
    public String queue(String printer) throws RemoteException {
        return "Queue for printer " + printer + ": [job1, job2]";
    }

    @Override
    public void topQueue(String printer, int job) throws RemoteException {
        System.out.println("Moving job " + job + " to top of the queue for printer " + printer);
    }

    @Override
    public void start() throws RemoteException {
        System.out.println("Print server started.");
    }

    @Override
    public void stop() throws RemoteException {
        System.out.println("Print server stopped.");
    }

    @Override
    public void restart() throws RemoteException {
        System.out.println("Print server restarted.");
    }

    @Override
    public String status(String printer) throws RemoteException {
        return "Status of printer " + printer + ": OK";
    }

    @Override
    public String readConfig(String parameter) throws RemoteException {
        return "Value of " + parameter + ": 42";
    }

    @Override
    public void setConfig(String parameter, String value) throws RemoteException {
        System.out.println("Set parameter " + parameter + " to " + value);
    }
}
