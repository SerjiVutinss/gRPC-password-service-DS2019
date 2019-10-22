package ie.gmit.serji;

import ie.gmit.serji.password.Passwords;

public class TestRunner {

    public static void main(String[] args) {

        TestUser u = new TestUser();
        u.userId = 1;
        u.password = Passwords.generateRandomPassword(64);
        u.salt = Passwords.getNextSalt();
        u.hashedPassword = Passwords.hash(u.password.toCharArray(), u.salt);

        System.out.println(u.toString());

        String badPassword = "HelloWorld!";

        boolean isExpectedPassword = Passwords.isExpectedPassword(
                u.password.toCharArray(), u.salt, u.hashedPassword
        );
        System.out.println("IS EXPECTED PASSWORD: " + isExpectedPassword);
    }
}
