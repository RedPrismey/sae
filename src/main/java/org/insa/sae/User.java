package org.insa.sae;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String username;
    private String password;
    private Role role;
    private String name;
    private String surname;

    public User() {}

    public User(int id, String username, String password, Role role, String name, String surname) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
        this.surname = surname;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public static User findById(int id) throws SQLException {
        String sql = "SELECT id, username, password, role, name, surname FROM users WHERE id = ?";
        Connection c = DBConnection.getConnection();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return fromResultSet(rs);
            }
        }
        return null;
    }

    public static User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password, role, name, surname FROM users WHERE username = ?";
        Connection c = DBConnection.getConnection();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return fromResultSet(rs);
            }
        }
        return null;
    }

    public static List<User> findAll() throws SQLException {
        String sql = "SELECT id, username, password, role, name, surname FROM users ORDER BY id";
        List<User> list = new ArrayList<>();
        Connection c = DBConnection.getConnection();
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(fromResultSet(rs));
        }
        return list;
    }

    private static User fromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String roleStr = rs.getString("role");
        Role role = Role.valueOf(roleStr);
        String name = rs.getString("name");
        String surname = rs.getString("surname");
        return new User(id, username, password, role, name, surname);
    }

    public void save() throws SQLException {
        if (this.id == 0) {
            String sql = "INSERT INTO users (username, password, role, name, surname) VALUES (?, ?, ?::role, ?, ?)";
            Connection c = DBConnection.getConnection();
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, this.username);
                ps.setString(2, this.password);
                ps.setString(3, this.role.name());
                ps.setString(4, this.name);
                ps.setString(5, this.surname);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) this.id = keys.getInt(1);
                }
            }
        } else {
            String sql = "UPDATE users SET username = ?, password = ?, role = ?::role, name = ?, surname = ? WHERE id = ?";
            Connection c = DBConnection.getConnection();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, this.username);
                ps.setString(2, this.password);
                ps.setString(3, this.role.name());
                ps.setString(4, this.name);
                ps.setString(5, this.surname);
                ps.setInt(6, this.id);
                ps.executeUpdate();
            }
        }
    }

    public void delete() throws SQLException {
        if (this.id == 0) return;
        String sql = "DELETE FROM users WHERE id = ?";
        Connection c = DBConnection.getConnection();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, this.id);
            ps.executeUpdate();
        }
        this.id = 0;
    }
}
