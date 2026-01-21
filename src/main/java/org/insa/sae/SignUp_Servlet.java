package org.insa.sae;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/SignUp_Servlet")
public class SignUp_Servlet extends HttpServlet {
    
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/pronte";
    private static final String DB_USER = "root";
    private static final String DB_PWD = ""; 

    @Override
protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    
    try {
        // 1. Récupération des données
        String nom = request.getParameter("nom");
        String prenom = request.getParameter("prenom");
        String roleStr = request.getParameter("role"); // "Etudiant", "Enseignant", "Admin"
        String username = request.getParameter("username");
        String mdp = request.getParameter("mdp");

        
        Role role = switch(roleStr) {
            case "Etudiant" -> Role.student;
            case "Enseignant" -> Role.teacher;
            case "Admin" -> Role.admin;
            default -> Role.student;
        };

        
        User user = new User(0, username, mdp, role, nom, prenom);
        user.save(); // 

      
        response.sendRedirect("login.jsp?success=1");

    } catch (Exception e) {
        e.printStackTrace();
        request.setAttribute("errorMessage", "Erreur : " + e.getMessage());
        request.getRequestDispatcher("sign_up.jsp").forward(request, response);
    }
}
}