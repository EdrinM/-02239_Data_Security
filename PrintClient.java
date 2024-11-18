import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class PrintClient {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 2099); // Use your port
            PrintServer server = (PrintServer) registry.lookup("PrintServer");

            if (server.authenticate("user1", "password1")) {
                System.out.println("User authenticated successfully!");

                System.out.println("Sending print request...");
                server.print("file.txt", "Printer1");

                System.out.println("Fetching queue...");
                System.out.println(server.queue("Printer1"));

                System.out.println("Checking printer status...");
                System.out.println(server.status("Printer1"));
            } else {
                System.out.println("Authentication failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
