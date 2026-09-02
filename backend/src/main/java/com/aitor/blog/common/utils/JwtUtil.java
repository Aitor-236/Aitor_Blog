package com.aitor.blog.common.utils;

import java.util.Date;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.JWT;

public class JwtUtil {
    // This class is intended to provide utility methods for handling JWT.
    // The implementation details for generating, validating, and parsing JWTs would be added here.

    // secret key
    private static final String SECRET_KEY = "aitor_blog_secret_key";
    // expiration time in milliseconds
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 1 day

    /**
     * Generates a JWT token for the given user ID and username.
     * @param userId The ID of the user for whom to generate the token.
     * @param username The username of the user for whom to generate the token.
     * @return The generated JWT token.
     */
    public static String generateToken(Long userId, String username) {
        Date date = new Date(System.currentTimeMillis() + EXPIRATION_TIME);
        return JWT.create()
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withExpiresAt(date)
                .sign(Algorithm.HMAC256(SECRET_KEY));
    }
    
    /**
     * Verifies the given JWT token.
     * @param token The JWT token to verify.
     * @return true if the token is valid, false otherwise.
     */
    public static boolean verifyToken(String token) {
        try {
            JWT.require(Algorithm.HMAC256(SECRET_KEY)).build().verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extracts the username from the given JWT token.
     * @param token The JWT token from which to extract the username.
     * @return The username extracted from the token, or null if the token is invalid or the username claim is not present.
     */
    public static String getUsername(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("username").asString();
        } catch (Exception e) {
            return null;
        }
    }
}
