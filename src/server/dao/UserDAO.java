package server.dao;

import common.User;
import java.sql.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class UserDAO {

    private static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public static User login(String username, String password) throws SQLException {
        String sql = "SELECT id, username, role, password_hash FROM users WHERE username = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String stored = rs.getString("password_hash");
                if (!stored.equals(sha256(password))) return null;
                return new User(rs.getInt("id"), rs.getString("username"), rs.getString("role"));
            }
        }
    }

    public static User register(String username, String password) throws SQLException {
        String hash = sha256(password);
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, hash);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new User(keys.getInt(1), username, "user");
                }
            }
        }
        return null;
    }
}
