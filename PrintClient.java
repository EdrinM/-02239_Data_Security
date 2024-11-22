import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.util.Scanner;

public class PrintClient {
    public static void main(String[] args) {
        try {
            // Connect to the remote server
            Registry registry = LocateRegistry.getRegistry("localhost", 2099);
            PrintServer server = (PrintServer) registry.lookup("PrintServer");

            Scanner scanner = new Scanner(System.in);

            boolean isRunning = true;

            while (isRunning) {
                String username = null;
                String password = null;

                // Authenticate user
                while (username == null || password == null) {
                    System.out.print("Enter username: ");
                    username = scanner.nextLine();

                    System.out.print("Enter password: ");
                    password = scanner.nextLine();

                    if (username.isBlank() || password.isBlank()) {
                        System.out.println("Invalid input. Please try again.");
                        username = null;
                        password = null;
                    } else {
                        try {
                            // Hash the password
                            String hashedPassword = hashPassword(password);

                            System.out.println("Attempting login with username=" + username);
                            server.login(username, hashedPassword);

                            System.out.println("Logged in successfully.");

                            try {
                                // Fetch available commands based on role
                                String availableCommands = server.getAvailableCommandsForRole(username);
                                System.out.println("Available commands: " + availableCommands);
                            } catch (Exception e) {
                                System.out.println("Error fetching available commands: " + e.getMessage());
                            }

                            break;
                        } catch (Exception e) {
                            System.out.println("Login failed: " + e.getMessage());
                            username = null;
                            password = null;
                        }
                    }
                }

                boolean authenticatedSession = true;

                // Main command loop
                while (authenticatedSession) {
                    System.out.print("Enter command: ");
                    String command = scanner.nextLine();

                    try {
                        switch (command.toLowerCase()) {
                            case "print":
                                System.out.print("Enter filename: ");
                                String filename = scanner.nextLine();
                                System.out.print("Enter printer: ");
                                String printer = scanner.nextLine();
                                server.print(filename, printer, username);
                                System.out.println("Print request sent.");
                                break;

                            case "queue":
                                System.out.print("Enter printer: ");
                                printer = scanner.nextLine();
                                System.out.println("Queue: " + server.queue(printer, username));
                                break;

                            case "topqueue":
                                System.out.print("Enter printer: ");
                                printer = scanner.nextLine();
                                System.out.print("Enter job number: ");
                                int job = Integer.parseInt(scanner.nextLine());
                                server.topQueue(printer, job, username);
                                System.out.println("Job moved to top of queue.");
                                break;

                            case "start":
                                server.start(username);
                                System.out.println("Print server started.");
                                break;

                            case "stop":
                                server.stop(username);
                                System.out.println("Print server stopped.");
                                break;

                            case "restart":
                                server.restart(username);
                                System.out.println("Print server restarted.");
                                break;

                            case "status":
                                System.out.print("Enter printer: ");
                                printer = scanner.nextLine();
                                System.out.println("Status: " + server.status(printer, username));
                                break;

                            case "readconfig":
                                System.out.print("Enter parameter: ");
                                String parameter = scanner.nextLine();
                                System.out.println("Config value: " + server.readConfig(parameter, username));
                                break;

                            case "setconfig":
                                System.out.print("Enter parameter: ");
                                parameter = scanner.nextLine();
                                System.out.print("Enter value: ");
                                String value = scanner.nextLine();
                                server.setConfig(parameter, value, username);
                                System.out.println("Config updated.");
                                break;

                            case "logout":
                                server.logout(username);
                                System.out.println("Logged out successfully.");
                                authenticatedSession = false;
                                break;

                            default:
                                System.out.println("Unknown command. Please try again.");
                        }
                    } catch (Exception e) {
                        if (e.getMessage().contains("Session expired")) {
                            System.out.println("Session expired. Please reauthenticate.");
                            authenticatedSession = false;
                        } else {
                            System.out.println("Server response: " + e.getMessage());
                        }
                    }
                }
            }

            scanner.close();
        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String hashPassword(String password) {
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
}
