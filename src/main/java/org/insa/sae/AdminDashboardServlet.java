package org.insa.sae;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/admin")
public class AdminDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        
        HttpSession session = request.getSession();
        String role = (String) session.getAttribute("role");
        
        if (role == null || !role.equals("Admin")) {
            response.sendRedirect("login.jsp");
            return;
        }
        
        try {
            
            List<User> allUsers = User.findAll();
            List<ModuleEntity> listeModules = ModuleEntity.findAll();
            List<Specialty> listeSpecialites = Specialty.findAll(); 

            
            List<User> listeEtudiants = allUsers.stream()
                .filter(u -> u.getRole() == Role.student)
                .collect(Collectors.toList());

            
            String filtreSpecialite = request.getParameter("filtreSpecialite");
            
            if (filtreSpecialite != null && !filtreSpecialite.isEmpty()) {
                
                int specId = -1;
                for (Specialty s : listeSpecialites) {
                    if (s.getName().equalsIgnoreCase(filtreSpecialite)) {
                        specId = s.getId();
                        break;
                    }
                }

               
                if (specId != -1) {
                    List<Inscription> inscriptions = Inscription.findAll();
                    
                    List<Integer> idsEtudiantsInscrits = new ArrayList<>();
                    for (Inscription i : inscriptions) {
                        if (i.getSpecialtyId() == specId) {
                            idsEtudiantsInscrits.add(i.getStudentId());
                        }
                    }

                    
                    final int finalSpecId = specId; 
                    listeEtudiants = listeEtudiants.stream()
                        .filter(u -> idsEtudiantsInscrits.contains(u.getId()))
                        .collect(Collectors.toList());
                    
                    request.setAttribute("filtreActif", filtreSpecialite);
                }
            }

            
            request.setAttribute("listeEtudiants", listeEtudiants);
            request.setAttribute("listeModules", listeModules);
            request.setAttribute("listeSpecialites", listeSpecialites);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("message", "Erreur lors du chargement des données : " + e.getMessage());
        }

        request.getRequestDispatcher("WEB-INF/admin_dashboard.jsp").forward(request, response);
    }
}