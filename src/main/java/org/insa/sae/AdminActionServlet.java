package org.insa.sae;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

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
                String type = request.getParameter("typeNote");
                Double coef = (double) Float.parseFloat(request.getParameter("coef"));


               
                Note note = new Note(0, valeur,type,coef ,idEtudiant, idModule);
                note.save();

                request.setAttribute("message", "Note enregistrée avec succès !");
            }
                else if ("createSpecialty".equals(action)) {
        
                String nomSpecialite = request.getParameter("nomSpecialite");
                
                if (nomSpecialite != null && !nomSpecialite.trim().isEmpty()) {
                    Specialty spe = new Specialty(0, nomSpecialite.trim());
                    spe.save(); 
                    request.setAttribute("message", "Spécialité '" + nomSpecialite + "' ajoutée avec succès.");
                } else {
                    throw new Exception("Le nom de la spécialité ne peut pas être vide.");
                }
        }
         else if ("createModule".equals(action)) {
        
        String moduleName = request.getParameter("moduleName");
        int teacherId = Integer.parseInt(request.getParameter("teacherId"));
        
        
        ModuleEntity nouveauModule = new ModuleEntity(0, moduleName, teacherId);
        nouveauModule.save(); 
    
        
        String[] specialtyIds = request.getParameterValues("specialtyIds");
        
        if (specialtyIds != null) {
            
            int newModuleId = nouveauModule.getId(); 
            
            Connection c = DBConnection.getConnection();
                String sql = "INSERT INTO specialty_modules (id_module, id_specialty) VALUES (?, ?)";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    for(String s : specialtyIds){
                    ps.setInt(1, newModuleId);
                    ps.setInt(2, Integer.parseInt(s));
                    ps.executeUpdate();
                }}
            c.close();
            }
        
    
        request.setAttribute("message", "Module '" + moduleName + "' créé et associé aux spécialités !");
    }else if ("delete".equals(action)) {
        String type = request.getParameter("type");
        int id = Integer.parseInt(request.getParameter("id"));
    
        if ("user".equals(type)) { 
            User u = User.findById(id);
            if (u != null) {
                u.delete(); 
                request.setAttribute("message", "Utilisateur (et ses données associées) supprimé avec succès.");
            }
        } 
        else if ("module".equals(type)) {
            ModuleEntity m = ModuleEntity.findById(id);
            if (m != null) {
                m.delete();
                request.setAttribute("message", "Module supprimé.");
            }
        }
        else if ("note".equals(type)) {
            Note n = Note.findById(id);
            if (n != null) {
                n.delete(); 
                request.setAttribute("message", "Note supprimée.");
            }
        }
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