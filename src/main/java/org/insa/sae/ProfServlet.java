package org.insa.sae;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

@WebServlet("/prof")
public class ProfServlet extends HttpServlet {
    @Override
protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String action = request.getParameter("action");
    
    if ("deleteComment".equals(action)) {
        try {
            int evalId = Integer.parseInt(request.getParameter("id"));
            String sql = "DELETE FROM evaluations WHERE id = ?";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, evalId);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   
    doGet(request, response);
}
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        
       
        Object userObj = session.getAttribute("user");
        User prof = null;

        try {
            if (userObj instanceof User) {
                
                prof = (User) userObj;
            } else if (userObj instanceof String) {
               
                prof = User.findByUsername((String) userObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

       
        if (prof == null || prof.getRole() != Role.teacher) {
            response.sendRedirect("login.jsp");
            return;
        }
       

        try {
            
            List<ModuleEntity> modules = ModuleEntity.findByTeacher(prof.getId());
            request.setAttribute("modulesList", modules);

           
            String moduleIdStr = request.getParameter("module");
            
            if (moduleIdStr != null && !moduleIdStr.isEmpty()) {
                int moduleId = Integer.parseInt(moduleIdStr);
                
                ModuleEntity currentModule = ModuleEntity.findById(moduleId);
                
                if(currentModule != null && currentModule.getTeacherId() == prof.getId()) {
                     request.setAttribute("selectedModuleId", moduleId);
                     
                   
                     List<String> notesList = new ArrayList<>();
                     String sqlNotes = "SELECT u.name, u.surname, n.note " +
                                       "FROM notes n " +
                                       "JOIN users u ON n.id_student = u.id " +
                                       "WHERE n.id_module = ?";
                     
                     try (Connection c = DBConnection.getConnection();
                          PreparedStatement ps = c.prepareStatement(sqlNotes)) {
                         ps.setInt(1, moduleId);
                         try (ResultSet rs = ps.executeQuery()) {
                             while (rs.next()) {
                                
                                 String fullName = rs.getString("surname") + " " + rs.getString("name");
                                 float note = rs.getFloat("note");
                                 notesList.add(fullName + ":" + note);
                             }
                         }
                     }
                     request.setAttribute("notesList", notesList);

                  
                     Map<String, Object> evalData = new HashMap<>();
                     int[] stars = new int[6]; 
                     List<Map<String, String>> comments = new ArrayList<>();
                     
                     String sqlEval = "SELECT e.id,e.rating, e.comment, e.created_at, u.name, u.surname " +
                                      "FROM evaluations e " +
                                      "JOIN users u ON e.id_student = u.id " +
                                      "WHERE e.id_module = ? ORDER BY e.created_at DESC";

                     try (Connection c = DBConnection.getConnection();
                          PreparedStatement ps = c.prepareStatement(sqlEval)) {
                         ps.setInt(1, moduleId);
                         try (ResultSet rs = ps.executeQuery()) {
                             while (rs.next()) {
                                 int rating = rs.getInt("rating");
                                 if (rating >= 1 && rating <= 5) {
                                     stars[rating]++;
                                 }
                                 
                                 String commentText = rs.getString("comment");
                                 if (commentText != null && !commentText.isEmpty()) {
                                     Map<String, String> comMap = new HashMap<>();
                                     comMap.put("id", String.valueOf(rs.getInt("id")));
                                     comMap.put("studentName", rs.getString("surname") + " " + rs.getString("name"));
                                     comMap.put("comment", commentText);
                                     comMap.put("rating", String.valueOf(rating));
                                     comments.add(comMap);
                                 }
                             }
                         }
                     }
                     
                     int total = stars[1] + stars[2] + stars[3] + stars[4] + stars[5];
                     double avg = 0.0;
                     if (total > 0) {
                         double sum = (1*stars[1] + 2*stars[2] + 3*stars[3] + 4*stars[4] + 5*stars[5]);
                         avg = sum / total;
                     }

                     evalData.put("statsArray", stars);
                     evalData.put("totalEvaluations", total);
                     evalData.put("averageRating", String.format("%.2f", avg));
                     evalData.put("comments", comments);
                     
                     request.setAttribute("evaluationData", evalData);
                     
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", e.getMessage());
        }
        request.setAttribute("prof", prof);
        request.getRequestDispatcher("WEB-INF/prof_dashboard.jsp").forward(request, response);
    }
}