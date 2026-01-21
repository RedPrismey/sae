package org.insa.sae;
import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


@WebServlet("/Login_Servlet")
public class Login_Servlet extends HttpServlet {

    @Override
protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String u = request.getParameter("username");
    String p = request.getParameter("mdp");

    try {
        
        User user = User.findByUsername(u);

       
        if (user != null && user.getPassword().equals(p)) {
            
            
            HttpSession session = request.getSession();
            session.setAttribute("user", user.getUsername());
            
            
            String roleStr = user.getRole().name(); 
            
           
            String roleSession = switch(user.getRole()) {
                case admin -> "Admin";
                case student -> "Etudiant";
                case teacher -> "Prof";
            };
            session.setAttribute("role", roleSession);

           
            switch (user.getRole()) {
                case admin -> response.sendRedirect("admin"); // Vers AdminDashboardServlet
                case student -> response.sendRedirect("etudiant"); // Vers EtudiantServlet
                case teacher -> response.sendRedirect("prof"); // Vers ProfServlet
            }

        } else {
           
            request.setAttribute("errorMessage", "Identifiant ou mot de passe incorrect.");
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }

    } catch (Exception e) {
        e.printStackTrace(); 
        request.setAttribute("errorMessage", "Erreur technique lors de la connexion.");
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }
}
}