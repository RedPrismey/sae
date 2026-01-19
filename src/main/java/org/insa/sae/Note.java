package org.insa.sae;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Note {
    private int id;
    private int note; // value
    private int studentId; // references users(id)
    private int moduleId; // references module(id)

    public Note() {}

    public Note(int id, int note, int studentId, int moduleId) {
        this.id = id;
        this.note = note;
        this.studentId = studentId;
        this.moduleId = moduleId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public int getModuleId() { return moduleId; }
    public void setModuleId(int moduleId) { this.moduleId = moduleId; }

    public static Note findById(int id) throws SQLException {
        String sql = "SELECT id, note, id_student, id_module FROM notes WHERE id = ?";
        Connection c = DBConnection.getConnection();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return fromResultSet(rs);
            }
        }
        return null;
    }

    public static List<Note> findAll() throws SQLException {
        String sql = "SELECT id, note, id_student, id_module FROM notes ORDER BY id";
        List<Note> list = new ArrayList<>();
        Connection c = DBConnection.getConnection();
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(fromResultSet(rs));
        }
        return list;
    }

    private static Note fromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int value = rs.getInt("note");
        int studentId = rs.getInt("id_student");
        int moduleId = rs.getInt("id_module");
        return new Note(id, value, studentId, moduleId);
    }

    public void save() throws SQLException {
        if (this.id == 0) {
            String sql = "INSERT INTO notes (note, id_student, id_module) VALUES (?, ?, ?)";
            Connection c = DBConnection.getConnection();
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, this.note);
                ps.setInt(2, this.studentId);
                ps.setInt(3, this.moduleId);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) this.id = keys.getInt(1);
                }
            }
        } else {
            String sql = "UPDATE notes SET note = ?, id_student = ?, id_module = ? WHERE id = ?";
            Connection c = DBConnection.getConnection();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, this.note);
                ps.setInt(2, this.studentId);
                ps.setInt(3, this.moduleId);
                ps.setInt(4, this.id);
                ps.executeUpdate();
            }
        }
    }

    public void delete() throws SQLException {
        if (this.id == 0) return;
        String sql = "DELETE FROM notes WHERE id = ?";
        Connection c = DBConnection.getConnection();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, this.id);
            ps.executeUpdate();
        }
        this.id = 0;
    }
}
