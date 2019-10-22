package ie.gmit.serji;

public class TestUser {

    public int userId;
    public String password;
    public byte[] salt;
    public byte[] hashedPassword;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("UserID: " + userId);
        sb.append("\nPassword: " + password);
        sb.append("\nSalt: " + salt);
        sb.append("\nHash: " + hashedPassword);
        return sb.toString();
    }
}
