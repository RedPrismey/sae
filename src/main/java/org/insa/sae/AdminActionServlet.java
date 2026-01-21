package org.insa.sae;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/AdminActionServlet")
public class AdminActionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
        String action = request.getParameter("action");

        try {
            if ("createEtudiant".equals(action)) {
               
                String usernameCible = request.getParameter("usernameEtudiant");
                String ine = request.getParameter("ine");
                
               
                int specialtyId = Integer.parseInt(request.getParameter("specialtyId"));

                
                User etudiant = User.findByUsername(usernameCible);
                if (etudiant == null) {
                    throw new Exception("Erreur : Utilisateur introuvable.");
                }

                
                Inscription inscription = new Inscription(0, etudiant.getId(), ine, specialtyId);
                inscription.save();
                
                request.setAttribute("message", "Inscription validée pour " + etudiant.getName() + " " + etudiant.getSurname());

            } else if ("addNote".equals(action)) {
               
                int idEtudiant = Integer.parseInt(request.getParameter("idEtudiant"));
                int idModule = Integer.parseInt(request.getParameter("idModule"));
                float valeur = Float.parseFloat(request.getParameter("valeur"));

               
                Note note = new Note(0, (int) valeur, idEtudiant, idModule);
                note.save();

                request.setAttribute("message", "Note enregistrée avec succès !");
            }

        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = e.getMessage();
          
            if (errorMsg != null && errorMsg.contains("duplicate key")) {
                errorMsg = "Cet étudiant ou ce numéro INE est déjà inscrit.";
            }
            request.setAttribute("message", "Erreur : " + errorMsg);
        }

       
        request.getRequestDispatcher("/admin").forward(request, response);
    }
}