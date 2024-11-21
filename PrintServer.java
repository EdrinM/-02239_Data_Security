import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PrintServer extends Remote {
    public void login(String username, String password) throws RemoteException;
    void print(String filename, String printer, String username) throws RemoteException;
    String queue(String printer, String username) throws RemoteException;
    void topQueue(String printer, int job, String username) throws RemoteException;
    void start(String username) throws RemoteException;
    void stop(String username) throws RemoteException;
    void restart(String username) throws RemoteException;
    String status(String printer, String username) throws RemoteException;
    String readConfig(String parameter, String username) throws RemoteException;
    void setConfig(String parameter, String value, String username) throws RemoteException;
    void logout(String username) throws RemoteException;

}
