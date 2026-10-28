import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility to generate BCrypt password hashes for dummy data.
 * 
 * Run this to generate proper password hashes for the dummy data scripts.
 * 
 * Usage:
 * 1. Compile: javac -cp spring-security-crypto.jar generate_password_hash.java
 * 2. Run: java -cp .:spring-security-crypto.jar generate_password_hash
 * 
 * Or use the online BCrypt generator: https://bcrypt-generator.com/
 */
public class generate_password_hash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String[] passwords = {
            "Password123",
            "SecurePass123",
            "AdminPass123"
        };
        
        System.out.println("BCrypt Password Hashes:");
        System.out.println("========================");
        for (String password : passwords) {
            String hash = encoder.encode(password);
            System.out.println(password + " -> " + hash);
        }
    }
}
