package org.insa.sae;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/StatsServlet")
public class StatsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
       
        HttpSession session = request.getSession();
        String role = (String) session.getAttribute("role"); 
        
      
        if (role == null || !role.equals("Admin")) {
            response.sendRedirect("login.jsp");
            return;
        }

        String type = request.getParameter("type");

        try {
            List<Specialty> specs = Specialty.findAll();
            request.setAttribute("listeSpecs", specs);
            if ("classement".equals(type)) {
                handleClassement(request);
            } else if ("evaluations".equals(type)) {
                handleEvaluations(request);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", e.getMessage());
        }

        request.getRequestDispatcher("WEB-INF/stats.jsp").forward(request, response);
    }

  
    private void handleClassement(HttpServletRequest request) throws SQLException {
        String filterSpecIdStr = request.getParameter("specId");
        Integer filterSpecId = (filterSpecIdStr != null && !filterSpecIdStr.isEmpty()) ? Integer.parseInt(filterSpecIdStr) : null;
        
        request.setAttribute("selectedSpecId", filterSpecIdStr);
        
      
        List<User> allUsers = User.findAll();
        List<User> students = new ArrayList<>();
        List<Inscription> inscriptions = Inscription.findAll();

        for (User u : allUsers) {
            if (u.getRole() == Role.student) {
               
                if (filterSpecId != null) {
                    boolean isInSpec = false;
                    for (Inscription i : inscriptions) {
                        if (i.getStudentId() == u.getId() && i.getSpecialtyId() == filterSpecId) {
                            isInSpec = true;
                            break;
                        }
                    }
                    if (isInSpec) students.add(u);
                } else {
                    
                    students.add(u);
                }
            }
        }

        List<Map<String, Object>> classement = new ArrayList<>();

        for (User student : students) {
           
            String sql = "SELECT note, coef FROM notes WHERE id_student = ?";
            double sommePonderee = 0;
            double sommeCoef = 0;
            
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, student.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        sommePonderee += rs.getFloat("note") * rs.getDouble("coef");
                        sommeCoef += rs.getDouble("coef");
                    }
                }
            }

            double moyenne = (sommeCoef > 0) ? (sommePonderee / sommeCoef) : 0.0;

            if (sommeCoef > 0) { 
                Map<String, Object> entry = new HashMap<>();
                entry.put("id", student.getId());
                entry.put("nom", student.getSurname());
                entry.put("prenom", student.getName());
                entry.put("moyenne", moyenne);
                
                
                String sqlSpe = "SELECT s.name FROM specialties s JOIN inscriptions i ON s.id = i.id_specialty WHERE i.id_student = ?";
                try (Connection c = DBConnection.getConnection();
                     PreparedStatement ps = c.prepareStatement(sqlSpe)) {
                    ps.setInt(1, student.getId());
                    try(ResultSet rs = ps.executeQuery()){
                        if(rs.next()) entry.put("specialite", rs.getString("name"));
                        else entry.put("specialite", "-");
                    }
                }
                
                classement.add(entry);
            }
        }

        Collections.sort(classement, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                Double m1 = (Double) o1.get("moyenne");
                Double m2 = (Double) o2.get("moyenne");
                return m2.compareTo(m1);
            }
        });

        request.setAttribute("classementList", classement);
        request.setAttribute("viewType", "classement");
    }

    
    private void handleEvaluations(HttpServletRequest request) throws SQLException {
        List<Map<String, Object>> statsModules = new ArrayList<>();
        List<ModuleEntity> modules = ModuleEntity.findAll();

        for (ModuleEntity m : modules) {
            Map<String, Object> data = new HashMap<>();
            data.put("moduleName", m.getName());

            Connection c = DBConnection.getConnection();

           
            String sqlEval = "SELECT AVG(rating) as avg_rating, COUNT(*) as nb_avis FROM evaluations WHERE id_module = ?";
            try (PreparedStatement ps = c.prepareStatement(sqlEval)) {
                ps.setInt(1, m.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        data.put("avgRating", rs.getDouble("avg_rating")); 
                        data.put("nbAvis", rs.getInt("nb_avis"));
                    }
                }
            }

           
            String sqlNotes = "SELECT note FROM notes WHERE id_module = ?";
            int totalNotes = 0;
            int reussite = 0;
            double sommeNotes = 0;

            try (PreparedStatement ps = c.prepareStatement(sqlNotes)) {
                ps.setInt(1, m.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        float n = rs.getFloat("note");
                        totalNotes++;
                        sommeNotes += n;
                        if (n >= 10) reussite++;
                    }
                }
            }

            double tauxReussite = (totalNotes > 0) ? ((double) reussite / totalNotes) * 100 : 0.0;
            double moyenneModule = (totalNotes > 0) ? (sommeNotes / totalNotes) : 0.0;

            data.put("tauxReussite", tauxReussite);
            data.put("moyenneModule", moyenneModule); 
            data.put("nbNotes", totalNotes);
            
            if (totalNotes > 0 || (int)data.get("nbAvis") > 0) {
                statsModules.add(data);
            }
        }
        
        request.setAttribute("statsModules", statsModules);
        request.setAttribute("viewType", "evaluations");
    }
}