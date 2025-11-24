package za.co.tt.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${jwt.expiration.hours:24}")
    private long expirationHours;
    

    @Value("${jwt.secret:tymeless-tyre-super-secret-key-2025-very-long-and-secure}")
    private String secretKey;
    

    public long getExpirationTimeInMillis() {
        return expirationHours * 60 * 60 * 1000;
    }
    

    public long getExpirationHours() {
        return expirationHours;
    }
    

    public String getSecretKey() {
        return secretKey;
    }
    

    public void setExpirationHours(long hours) {
        this.expirationHours = hours;
    }
    

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}