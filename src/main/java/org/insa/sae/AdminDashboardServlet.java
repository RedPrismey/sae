package org.insa.sae;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        doGet(request, response);
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        
        HttpSession session = request.getSession();
        Object userObj = session.getAttribute("user");
        User loggedUser = null;

        try {
            if (userObj instanceof User) {
                loggedUser = (User) userObj;
            } else if (userObj instanceof String) {
                loggedUser = User.findByUsername((String) userObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (loggedUser == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        
        try {
            
            List<User> allUsers = User.findAll();
            List<ModuleEntity> listeModules = ModuleEntity.findAll();
            List<Specialty> listeSpecialites = Specialty.findAll(); 
            List<Inscription> inscriptions = Inscription.findAll();
            List<User> listeProfs = allUsers.stream()
            .filter(u -> u.getRole() == Role.teacher)
            .collect(Collectors.toList());


            List<User> tousLesEtudiants = allUsers.stream()
                .filter(u -> u.getRole() == Role.student)
                .collect(Collectors.toList());

            List<Integer> idsEtudiantsInscrits = inscriptions.stream()
                .map(Inscription::getStudentId)
                .collect(Collectors.toList());

            List<User> etudiantsNonInscrits = tousLesEtudiants.stream()
                .filter(u -> !idsEtudiantsInscrits.contains(u.getId()))
                .collect(Collectors.toList());
            
            List<User> etudiantsInscrits = tousLesEtudiants.stream()
                .filter(u -> idsEtudiantsInscrits.contains(u.getId()))
                .collect(Collectors.toList());
            
            String filterSpecIdStr = request.getParameter("filterSpecId");
            String[] filterModuleIdsStr = request.getParameterValues("filterModuleIds");
            String sortType = request.getParameter("sortType");


            List<Map<String, Object>> resultatsNotes = new ArrayList<>();

           
            if (filterSpecIdStr != null && !filterSpecIdStr.isEmpty()) {
                int specId = Integer.parseInt(filterSpecIdStr);
                
               
                List<Integer> studentIdsInSpec = inscriptions.stream()
                    .filter(i -> i.getSpecialtyId() == specId)
                    .map(Inscription::getStudentId)
                    .collect(Collectors.toList());

              
                List<Note> allNotes = Note.findAll();

               
                for (Note n : allNotes) {
                   
                    if (studentIdsInSpec.contains(n.getStudentId())) {
                        
                       
                        boolean moduleMatch = true;
                        if (filterModuleIdsStr != null && filterModuleIdsStr.length > 0) {
                            moduleMatch = false;
                            for (String mId : filterModuleIdsStr) {
                                if (Integer.parseInt(mId) == n.getModuleId()) {
                                    moduleMatch = true;
                                    break;
                                }
                            }
                        }

                        if (moduleMatch) {
                           
                            Map<String, Object> row = new HashMap<>();
                            
                            User etu = User.findById(n.getStudentId());
                            ModuleEntity mod = ModuleEntity.findById(n.getModuleId());
                            
                            if (etu != null && mod != null) {
                                row.put("id", n.getId());
                                row.put("nom", etu.getSurname());
                                row.put("prenom", etu.getName());
                                row.put("module", mod.getName());
                                row.put("note", n.getNote());
                                row.put("coef", n.getCoef());
                                resultatsNotes.add(row);
                            }
                        }
                    }
                }
            }

            
            if (sortType != null) {
                switch (sortType) {
                    case "name_asc":
                        resultatsNotes.sort((m1, m2) -> ((String)m1.get("nom")).compareToIgnoreCase((String)m2.get("nom")));
                        break;
                    case "name_desc":
                        resultatsNotes.sort((m1, m2) -> ((String)m2.get("nom")).compareToIgnoreCase((String)m1.get("nom")));
                        break;
                    case "note_asc":
                        resultatsNotes.sort((m1, m2) -> Float.compare((float)m1.get("note"), (float)m2.get("note")));
                        break;
                    case "note_desc":
                        resultatsNotes.sort((m1, m2) -> Float.compare((float)m2.get("note"), (float)m1.get("note")));
                        break;
                }
            }

           
            request.setAttribute("resultatsNotes", resultatsNotes);
            
         
            request.setAttribute("selectedSpecId", filterSpecIdStr);
            request.setAttribute("selectedSort", sortType);
           
            List<Integer> selectedModules = new ArrayList<>();
            if (filterModuleIdsStr != null) {
                for(String s : filterModuleIdsStr) selectedModules.add(Integer.parseInt(s));
            }
            request.setAttribute("selectedModules", selectedModules);


            Map<Integer, String> mapIne = new HashMap<>();
            Map<Integer, String> mapSpecialiteName = new HashMap<>();
            
            
            for (Inscription i : inscriptions) {
                mapIne.put(i.getStudentId(), i.getIne());
                
                
                String nomSpe = listeSpecialites.stream()
                    .filter(s -> s.getId() == i.getSpecialtyId())
                    .map(Specialty::getName)
                    .findFirst()
                    .orElse("Inconnu");
                mapSpecialiteName.put(i.getStudentId(), nomSpe);
            }
            String filtreSpecialite = request.getParameter("filtreSpecialite");
            if (filtreSpecialite != null && !filtreSpecialite.isEmpty() && !filtreSpecialite.equals("all")) {
                etudiantsInscrits = etudiantsInscrits.stream()
                    .filter(u -> {
                        String userSpecName = mapSpecialiteName.get(u.getId());
                        return userSpecName != null && userSpecName.equalsIgnoreCase(filtreSpecialite);
                    })
                    .collect(Collectors.toList());
                request.setAttribute("filtreActif", filtreSpecialite);
            }

            
            request.setAttribute("listeModules", listeModules);
            request.setAttribute("listeSpecialites", listeSpecialites);
            request.setAttribute("etudiantsNonInscrits", etudiantsNonInscrits);
            request.setAttribute("etudiantsInscrits", etudiantsInscrits);
            request.setAttribute("mapIne", mapIne);
            request.setAttribute("mapSpecialiteName", mapSpecialiteName);
            request.setAttribute("listeProfs", listeProfs);

            for (Inscription i : inscriptions) {
                mapIne.put(i.getStudentId(), i.getIne());
                String nomSpe = listeSpecialites.stream()
                    .filter(s -> s.getId() == i.getSpecialtyId())
                    .map(Specialty::getName)
                    .findFirst().orElse("Inconnu");
                mapSpecialiteName.put(i.getStudentId(), nomSpe);
            }
            request.setAttribute("mapIne", mapIne);
            request.setAttribute("mapSpecialiteName", mapSpecialiteName);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("message", "Erreur lors du chargement des données : " + e.getMessage());
        }

        request.getRequestDispatcher("WEB-INF/admin_dashboard.jsp").forward(request, response);
    }
}