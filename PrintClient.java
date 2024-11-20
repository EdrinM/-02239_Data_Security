import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class PrintClient {
    public static void main(String[] args) {
        try {
            // Connect to the remote server
            Registry registry = LocateRegistry.getRegistry("localhost", 2099); // Match server port
            PrintServer server = (PrintServer) registry.lookup("PrintServer");

            Scanner scanner = new Scanner(System.in);

            String username = null;
            String password = null;

            boolean isRunning = true;

            while (isRunning) {
                if (username == null || password == null) {
                    System.out.print("Enter username: ");
                    username = scanner.nextLine();

                    System.out.print("Enter password: ");
                    password = scanner.nextLine();
                }

                // Present user with options
                System.out.println("\nAvailable commands: print, queue, logout");
                System.out.print("Enter command: ");
                String command = scanner.nextLine();

                try {
                    switch (command.toLowerCase()) {
                        case "print":
                            System.out.print("Enter filename: ");
                            String filename = scanner.nextLine();
                            System.out.print("Enter printer: ");
                            String printer = scanner.nextLine();
                            server.print(filename, printer, username, password);
                            System.out.println("Print request sent.");
                            break;

                        case "queue":
                            System.out.print("Enter printer: ");
                            printer = scanner.nextLine();
                            System.out.println("Queue: " + server.queue(printer, username, password));
                            break;

                        case "logout":
                            server.logout(username, password);
                            System.out.println("Logged out successfully.");
                            isRunning = false;
                            break;

                        default:
                            System.out.println("Unknown command. Please try again.");
                    }
                } catch (Exception e) {
                    // Check if the session expired and prompt for reauthentication
                    if (e.getMessage().contains("Session expired")) {
                        System.out.println("Session expired. Please reauthenticate.");
                        username = null; // Reset credentials
                        password = null;
                    } else {
                        System.out.println("Server response: " + e.getMessage());
                    }
                }
            }

            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
