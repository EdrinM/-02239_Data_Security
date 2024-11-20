import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class PrintClient {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 2099); // Match server port
            PrintServer server = (PrintServer) registry.lookup("PrintServer");

            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter username: ");
            String username = scanner.nextLine();

            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            // Perform some server operations
            try {
                System.out.println("Sending print request...");
                server.print("file.txt", "Printer1", username, password);

                System.out.println("Fetching queue...");
                System.out.println(server.queue("Printer1", username, password));

                // Wait for session expiration
                System.out.println("Waiting for session to expire...");
                Thread.sleep(7000); // Wait longer than SESSION_TIMEOUT

                // Attempt to fetch the queue again after session expiration
                System.out.println("Fetching queue after session expiration...");
                System.out.println(server.queue("Printer1", username, password));
            } catch (Exception e) {
                System.out.println("Server response: " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
