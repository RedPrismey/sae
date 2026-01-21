package org.insa.sae;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import org.insa.sae.*;

@WebServlet("/DetailEtudiantServlet")
public class DetailEtudiantServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String studentIdStr = request.getParameter("id");
        System.out.println("DetailEtudiantServlet - ID étudiant: " + studentIdStr);
        
        if (studentIdStr == null || studentIdStr.isEmpty()) {
            response.sendError(400, "ID étudiant manquant");
            return;
        }
        
        try {
            int studentId = Integer.parseInt(studentIdStr);
            
            // 1. Récupérer l'étudiant (User)
            User etudiant = User.findById(studentId);
            if (etudiant == null || etudiant.getRole() != Role.student) {
                response.sendError(404, "Étudiant non trouvé");
                return;
            }
            
            // 2. Récupérer l'inscription (INE + spécialité)
            Inscription inscription = getInscriptionByStudentId(studentId);
            
            // 3. Récupérer la spécialité
            Specialty specialite = null;
            String nomSpecialite = "Non spécifiée";
            if (inscription != null) {
                specialite = Specialty.findById(inscription.getSpecialtyId());
                if (specialite != null) {
                    nomSpecialite = specialite.getName();
                }
            }
            
            // 4. Récupérer toutes les notes avec détails des modules
            List<Map<String, Object>> notesDetaillees = getNotesDetaillees(studentId);
            
            // 5. Calculer les statistiques
            Map<String, Object> stats = calculerStatsEtudiant(notesDetaillees);
            
            // 6. Récupérer la position dans le classement
            int positionClassement = getPositionClassement(studentId);
            
            // 7. Identifier les modules en difficulté (<10)
            List<Map<String, Object>> modulesDifficulte = getModulesEnDifficulte(notesDetaillees);
            
            // 8. Préparer les données pour la JSP
            request.setAttribute("etudiant", etudiant);
            request.setAttribute("inscription", inscription);
            request.setAttribute("specialite", specialite);
            request.setAttribute("nomSpecialite", nomSpecialite);
            request.setAttribute("notes", notesDetaillees);
            request.setAttribute("stats", stats);
            request.setAttribute("positionClassement", positionClassement);
            request.setAttribute("modulesDifficulte", modulesDifficulte);
            
            // 9. Afficher la page de détail
            request.getRequestDispatcher("/detailEtudiant.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(400, "ID invalide. Doit être un nombre.");
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("error", "Erreur base de données: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Erreur: " + e.getMessage());
        }
    }
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Récupère l'inscription d'un étudiant
     */
    private Inscription getInscriptionByStudentId(int studentId) throws SQLException {
        List<Inscription> inscriptions = Inscription.findAll();
        if (inscriptions != null) {
            for (Inscription ins : inscriptions) {
                if (ins.getStudentId() == studentId) {
                    return ins;
                }
            }
        }
        return null;
    }
    
    /**
     * Récupère les notes détaillées (avec infos modules)
     */
    private List<Map<String, Object>> getNotesDetaillees(int studentId) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // Récupérer toutes les notes
        List<Note> toutesNotes = Note.findAll();
        if (toutesNotes == null) return result;
        
        for (Note note : toutesNotes) {
            if (note.getStudentId() == studentId) {
                Map<String, Object> noteDetail = new HashMap<>();
                noteDetail.put("id", note.getId());
                noteDetail.put("valeur", note.getNote());
                
                // Récupérer les infos du module
                ModuleEntity module = ModuleEntity.findById(note.getModuleId());
                if (module != null) {
                    noteDetail.put("moduleId", module.getId());
                    noteDetail.put("moduleNom", module.getName());
                    
                    // Récupérer l'enseignant du module
                    User enseignant = User.findById(module.getTeacherId());
                    if (enseignant != null) {
                        noteDetail.put("enseignantNom", 
                            enseignant.getSurname() + " " + enseignant.getName());
                        noteDetail.put("enseignantId", enseignant.getId());
                    }
                    
                } else {
                    noteDetail.put("moduleNom", "Module inconnu");
                }
                
                // Déterminer si c'est une note problématique (<10)
                noteDetail.put("problematique", note.getNote() < 10);
                
                result.add(noteDetail);
            }
        }
        
        // Trier par module
        Collections.sort(result, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> a, Map<String, Object> b) {
                String moduleA = (String) a.get("moduleNom");
                String moduleB = (String) b.get("moduleNom");
                return moduleA.compareTo(moduleB);
            }
        });
        
        return result;
    }
    
    /**
     * Calcule les statistiques de l'étudiant
     */
    private Map<String, Object> calculerStatsEtudiant(List<Map<String, Object>> notes) {
        Map<String, Object> stats = new HashMap<>();
        
        if (notes == null || notes.isEmpty()) {
            stats.put("moyenneGenerale", 0.0);
            stats.put("nbNotes", 0);
            stats.put("meilleureNote", 0);
            stats.put("pireNote", 0);
            stats.put("nbNotesProblematiques", 0);
            stats.put("pourcentageReussite", 0.0);
            return stats;
        }
        
        double somme = 0;
        int meilleure = Integer.MIN_VALUE;
        int pire = Integer.MAX_VALUE;
        int nbProblematiques = 0;
        
        for (Map<String, Object> note : notes) {
            int valeur = (int) note.get("valeur");
            somme += valeur;
            
            if (valeur > meilleure) meilleure = valeur;
            if (valeur < pire) pire = valeur;
            
            if (valeur < 10) {
                nbProblematiques++;
            }
        }
        
        double moyenne = somme / notes.size();
        double pourcentageReussite = ((double)(notes.size() - nbProblematiques) / notes.size()) * 100;
        
        stats.put("moyenneGenerale", Math.round(moyenne * 100.0) / 100.0);
        stats.put("nbNotes", notes.size());
        stats.put("meilleureNote", meilleure);
        stats.put("pireNote", pire);
        stats.put("nbNotesProblematiques", nbProblematiques);
        stats.put("pourcentageReussite", Math.round(pourcentageReussite * 100.0) / 100.0);
        
        // Mention selon la moyenne
        stats.put("mention", getMention(moyenne));
        
        return stats;
    }
    
    /**
     * Calcule la position dans le classement général
     */
    private int getPositionClassement(int studentId) throws SQLException {
        // Récupérer tous les étudiants avec leurs moyennes
        List<Map<String, Object>> etudiants = new ArrayList<>();
        List<User> users = User.findAll();
        
        if (users != null) {
            for (User user : users) {
                if (user.getRole() == Role.student) {
                    Map<String, Object> etudiant = new HashMap<>();
                    etudiant.put("id", user.getId());
                    
                    // Calculer la moyenne
                    List<Note> notes = getNotesByStudent(user.getId());
                    double moyenne = calculerMoyenne(notes);
                    etudiant.put("moyenne", moyenne);
                    
                    etudiants.add(etudiant);
                }
            }
        }
        
        // Trier par moyenne décroissante
        Collections.sort(etudiants, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> a, Map<String, Object> b) {
                double moyenneA = (double) a.get("moyenne");
                double moyenneB = (double) b.get("moyenne");
                return Double.compare(moyenneB, moyenneA);
            }
        });
        
        // Trouver la position
        for (int i = 0; i < etudiants.size(); i++) {
            if ((int) etudiants.get(i).get("id") == studentId) {
                return i + 1; // Position (1-indexed)
            }
        }
        
        return 0; // Non classé
    }
    
    /**
     * Identifie les modules où l'étudiant a des difficultés (<10)
     */
    private List<Map<String, Object>> getModulesEnDifficulte(List<Map<String, Object>> notes) {
        List<Map<String, Object>> modulesDifficulte = new ArrayList<>();
        
        for (Map<String, Object> note : notes) {
            if ((boolean) note.get("problematique")) {
                Map<String, Object> module = new HashMap<>();
                module.put("moduleNom", note.get("moduleNom"));
                module.put("note", note.get("valeur"));
                module.put("enseignant", note.get("enseignantNom"));
                modulesDifficulte.add(module);
            }
        }
        
        return modulesDifficulte;
    }
    
    /**
     * Récupère les notes d'un étudiant (méthode utilitaire)
     */
    private List<Note> getNotesByStudent(int studentId) throws SQLException {
        List<Note> toutesNotes = Note.findAll();
        List<Note> notesEtudiant = new ArrayList<>();
        
        if (toutesNotes != null) {
            for (Note note : toutesNotes) {
                if (note.getStudentId() == studentId) {
                    notesEtudiant.add(note);
                }
            }
        }
        return notesEtudiant;
    }
    
    /**
     * Calcule la moyenne d'une liste de notes
     */
    private double calculerMoyenne(List<Note> notes) {
        if (notes == null || notes.isEmpty()) return 0.0;
        
        double somme = 0;
        for (Note note : notes) {
            somme += note.getNote();
        }
        return somme / notes.size();
    }
    
    /**
     * Détermine la mention selon la moyenne
     */
    private String getMention(double moyenne) {
        if (moyenne >= 16) return "Très Bien";
        if (moyenne >= 14) return "Bien";
        if (moyenne >= 12) return "Assez Bien";
        if (moyenne >= 10) return "Passable";
        return "Insuffisant";
    }
}
