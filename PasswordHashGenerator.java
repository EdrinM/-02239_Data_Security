import java.security.MessageDigest;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        String[] passwords = {"password", "admin"};

        for (String password : passwords) {
            System.out.println("Plain: " + password);
            System.out.println("Hash: " + hashPassword(password));
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
            throw new RuntimeException(e);
        }
    }
}
