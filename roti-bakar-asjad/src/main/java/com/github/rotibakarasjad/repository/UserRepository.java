package com.github.rotibakarasjad.repository;

import com.github.rotibakarasjad.entity.User;
import com.github.rotibakarasjad.entity.enums.Role;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public class UserRepository {

    private static final String SELECT_COLUMNS =
            "SELECT id, username, password_hash, full_name, role, created_at FROM users ";

    public Optional<User> findByUsername(String username) throws SQLException {
        return querySingle(SELECT_COLUMNS + "WHERE username = ?", username);
    }

    public Optional<User> findById(int id) throws SQLException {
        return querySingle(SELECT_COLUMNS + "WHERE id = ?", id);
    }

    private Optional<User> querySingle(String sql, Object param) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(Role.valueOf(rs.getString("role")));
        Timestamp created = rs.getTimestamp("created_at");
        user.setCreatedAt(created == null ? null : created.toLocalDateTime());
        return user;
    }
}