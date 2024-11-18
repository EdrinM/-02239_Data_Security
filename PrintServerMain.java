import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class PrintServerMain {
    public static void main(String[] args) {
        try {
            PrintServerImpl server = new PrintServerImpl();
            Registry registry = LocateRegistry.createRegistry(2099);
            registry.rebind("PrintServer", server);
            System.out.println("Print server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}