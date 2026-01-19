package org.insa.sae;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Inscription {
    private int id;
    private int studentId; // references users(id)
    private String ine; // unique
    private int specialtyId; // references specialties(id)

    public Inscription() {}

    public Inscription(int id, int studentId, String ine, int specialtyId) {
        this.id = id;
        this.studentId = studentId;
        this.ine = ine;
        this.specialtyId = specialtyId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getIne() { return ine; }
    public void setIne(String ine) { this.ine = ine; }
    public int getSpecialtyId() { return specialtyId; }
    public void setSpecialtyId(int specialtyId) { this.specialtyId = specialtyId; }

    public static Inscription findById(int id) throws SQLException {
        String sql = "SELECT id, id_student, INE, id_specialty FROM inscriptions WHERE id = ?";
        Connection c = DBConnection.getConnection();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return fromResultSet(rs);
            }
        }
        return null;
    }

    public static List<Inscription> findAll() throws SQLException {
        String sql = "SELECT id, id_student, INE, id_specialty FROM inscriptions ORDER BY id";
        List<Inscription> list = new ArrayList<>();
        Connection c = DBConnection.getConnection();
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(fromResultSet(rs));
        }
        return list;
    }

    private static Inscription fromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int studentId = rs.getInt("id_student");
        String ine = rs.getString("INE");
        int specialtyId = rs.getInt("id_specialty");
        return new Inscription(id, studentId, ine, specialtyId);
    }

    public void save() throws SQLException {
        if (this.id == 0) {
            String sql = "INSERT INTO inscriptions (id_student, INE, id_specialty) VALUES (?, ?, ?)";
            Connection c = DBConnection.getConnection();
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, this.studentId);
                ps.setString(2, this.ine);
                ps.setInt(3, this.specialtyId);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) this.id = keys.getInt(1);
                }
            }
        } else {
            String sql = "UPDATE inscriptions SET id_student = ?, INE = ?, id_specialty = ? WHERE id = ?";
            Connection c = DBConnection.getConnection();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, this.studentId);
                ps.setString(2, this.ine);
                ps.setInt(3, this.specialtyId);
                ps.setInt(4, this.id);
                ps.executeUpdate();
            }
        }
    }

    public void delete() throws SQLException {
        if (this.id == 0) return;
        String sql = "DELETE FROM inscriptions WHERE id = ?";
        Connection c = DBConnection.getConnection();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, this.id);
            ps.executeUpdate();
        }
        this.id = 0;
    }
}
