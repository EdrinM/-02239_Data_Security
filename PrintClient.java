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

            if (server.authenticate(username, password)) {
                System.out.println("User authenticated successfully!");
                server.print("file.txt", "Printer1");
                System.out.println(server.queue("Printer1"));
                System.out.println(server.status("Printer1"));
            } else {
                System.out.println("Authentication failed.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
