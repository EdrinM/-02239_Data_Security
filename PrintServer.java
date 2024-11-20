import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PrintServer extends Remote {
    void print(String filename, String printer, String username, String password) throws RemoteException;
    String queue(String printer, String username, String password) throws RemoteException;
    void topQueue(String printer, int job, String username, String password) throws RemoteException;
    void start(String username, String password) throws RemoteException;
    void stop(String username, String password) throws RemoteException;
    void restart(String username, String password) throws RemoteException;
    String status(String printer, String username, String password) throws RemoteException;
    String readConfig(String parameter, String username, String password) throws RemoteException;
    void setConfig(String parameter, String value, String username, String password) throws RemoteException;
    void logout(String username, String password) throws RemoteException;

}
