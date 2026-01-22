package org.insa.sae;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/etudiant")
public class EtudiantServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 1. Vérification session
        HttpSession session = request.getSession();
        User etudiant = null;
        Object userObj = session.getAttribute("user");

        // Gestion de l'objet session (String vs User object)
        try {
            if (userObj instanceof User) {
                etudiant = (User) userObj;
            } else if (userObj instanceof String) {
                etudiant = User.findByUsername((String) userObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (etudiant == null || etudiant.getRole() != Role.student) {
            response.sendRedirect("login.jsp");
            return;
        }

        // 2. Récupération des données
        try {
            // A. Récupérer TOUS les modules pour la liste déroulante d'évaluation
            List<ModuleEntity> tousLesModules = ModuleEntity.findAll();
            request.setAttribute("listeModules", tousLesModules);

            // B. Récupérer les NOTES de l'étudiant
            List<Map<String, Object>> mesNotes = new ArrayList<>();
            
            String sql = "SELECT m.name, n.note, n.coef, n.type " +
                         "FROM notes n " +
                         "JOIN module m ON n.id_module = m.id " +
                         "WHERE n.id_student = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setInt(1, etudiant.getId());
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> ligne = new HashMap<>();
                        ligne.put("module", rs.getString("name"));
                        ligne.put("note", rs.getFloat("note"));
                        ligne.put("coef", rs.getDouble("coef"));
                        ligne.put("type", rs.getString("type"));
                        mesNotes.add(ligne);
                    }
                }
            }
            request.setAttribute("mesNotes", mesNotes);

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("message", "Erreur technique : " + e.getMessage());
        }
        request.setAttribute("etudiant", etudiant);
        request.getRequestDispatcher("WEB-INF/etudiant_dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        Object userObj = session.getAttribute("user");
        User etudiant = null;
        
        // Récupération sécurisée de l'étudiant
        try {
            if (userObj instanceof User) etudiant = (User) userObj;
            else if (userObj instanceof String) etudiant = User.findByUsername((String) userObj);
        } catch (Exception e) { e.printStackTrace(); }

        if (etudiant == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = request.getParameter("action");

        if ("evaluerModule".equals(action)) {
            try {
                int moduleId = Integer.parseInt(request.getParameter("module"));
                String comment = request.getParameter("commentaire");
                
                int supports = Integer.parseInt(request.getParameter("supports"));
                int equipe = Integer.parseInt(request.getParameter("equipe"));
                int charge = Integer.parseInt(request.getParameter("charge"));

                // Calcul moyenne arrondie
                int rating = Math.round((supports + equipe + charge) / 3.0f);

                // Insertion BDD
                try (Connection conn = DBConnection.getConnection()) {
                    String sql = "INSERT INTO evaluations (id_module, id_student, rating, comment, created_at) VALUES (?, ?, ?, ?, NOW())";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, moduleId);
                        ps.setInt(2, etudiant.getId());
                        ps.setInt(3, rating);
                        ps.setString(4, comment);
                        ps.executeUpdate();
                    }
                }
                request.setAttribute("message", "Évaluation enregistrée avec succès !");

            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("message", "Erreur lors de l'évaluation : " + e.getMessage());
            }
        }

        doGet(request, response); // Recharger la page
    }
}