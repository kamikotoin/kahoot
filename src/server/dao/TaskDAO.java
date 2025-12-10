package server.dao;

import common.Task;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    public static List<Task> getTasksByUser(int userId) throws SQLException {
        String sql = "SELECT id, text, done, priority, category FROM tasks WHERE user_id = ?";
        var list = new ArrayList<Task>();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Task(
                        rs.getInt("id"),
                        userId,
                        rs.getString("text"),
                        rs.getInt("done") != 0,
                        rs.getString("priority"),
                        rs.getString("category")
                    ));
                }
            }
        }
        return list;
    }

    public static int addTask(int userId, String text, String priority, String category) throws SQLException {
        String sql = "INSERT INTO tasks(user_id, text, priority, category, created_at, updated_at) VALUES(?, ?, ?, ?, ?, ?)";
        String now = LocalDateTime.now().toString();
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, text);
            ps.setString(3, priority);
            ps.setString(4, category);
            ps.setString(5, now);
            ps.setString(6, now);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) return k.getInt(1);
            }
        }
        return -1;
    }

    public static boolean deleteTask(int id) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public static boolean markDone(int id, boolean done) throws SQLException {
        String sql = "UPDATE tasks SET done = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, done ? 1 : 0);
            ps.setString(2, LocalDateTime.now().toString());
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }
}
