<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.insa.sae.Specialty" %>
<%@ page import="org.insa.sae.*" %>
<!DOCTYPE html>
<html>
<head>
    <script>
        function confirmerSuppression(type, id) {
            if(confirm("Êtes-vous sûr de vouloir supprimer cet élément ? Cette action est irréversible.")) {
               
                var form = document.createElement("form");
                form.method = "POST";
                form.action = "AdminActionServlet";
                
                var inputAction = document.createElement("input");
                inputAction.type = "hidden";
                inputAction.name = "action";
                inputAction.value = "delete";
                form.appendChild(inputAction);
    
                var inputType = document.createElement("input");
                inputType.type = "hidden";
                inputType.name = "type";
                inputType.value = type;
                form.appendChild(inputType);
    
                var inputId = document.createElement("input");
                inputId.type = "hidden";
                inputId.name = "id";
                inputId.value = id;
                form.appendChild(inputId);
    
                document.body.appendChild(form);
                form.submit();
            }
        }
    </script>
    <title>Dashboard Admin</title>
    <style>
       
        * { box-sizing: border-box; } 
        
        html { scroll-behavior: smooth; } 
        body { 
            font-family: sans-serif; 
            margin: 0; 
            background-color: #f4f4f9;
            
        }

        .sidebar { 
            width: 250px; 
            background: #2c3e50; 
            color: white; 
            height: 100vh; 
            padding: 20px; 
            position: fixed; 
            top: 0;
            left: 0;
            overflow-y: auto; 
            z-index: 1000;
        }

        .content { 
           
            margin-left: 250px; 
            padding: 20px; 
            min-height: 100vh;
        }
       

        .sidebar a { display: block; color: white; padding: 10px; text-decoration: none; margin-bottom: 5px; border-radius: 4px; }
        .sidebar a:hover { background: #34495e; }
        
        .card { background: white; padding: 20px; margin-bottom: 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
       
        .card { scroll-margin-top: 20px; }

        h2 { border-bottom: 2px solid #0056b3; padding-bottom: 10px; }
        .form-row { display: flex; gap: 10px; margin-bottom: 10px; align-items: flex-end;}
        input, select { padding: 8px; border: 1px solid #ccc; border-radius: 4px; flex: 1; }
        button { padding: 10px 20px; background: #0056b3; color: white; border: none; border-radius: 4px; cursor: pointer; }
        table { width: 100%; border-collapse: collapse; margin-top: 15px; }
        th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }
        th { background-color: #eee; }
        .alert { color: green; font-weight: bold; margin-bottom: 15px; padding: 10px; background: #d4edda; border: 1px solid #c3e6cb; border-radius: 4px;}
    </style>
</head>
<body>

<div class="sidebar">
    <h3>Administration</h3>
    <p>Bienvenue Admin</p>
    <hr>
    <a href="#specialties">Gestion Spécialités</a>
    <a href="#modules">Gérer les modules</a>
    <a href="#inscription">Inscrire un étudiant</a>
    <a href="#saisie_notes">Saisir des notes</a>
    <a href="#suppression">Suppression d'User</a>
    <a href="#consultation_notes">Consultation Note</a>
    <a href="#consultation">Liste étudiants</a>
    <a href="#evaluations">Évaluations & Qualité</a>
    <hr>
    <a href="LogoutServlet">Déconnexion</a>
</div>

<div class="content">

    <% if (request.getAttribute("message") != null) { %>
        <div class="alert"><%= request.getAttribute("message") %></div>
    <% } %>

    <div id="specialties" class="card">
        <h2>Gestion des Spécialités</h2>
        <form action="AdminActionServlet" method="post">
            <input type="hidden" name="action" value="createSpecialty">
            <div class="form-row">
                <input type="text" name="nomSpecialite" placeholder="Nom de la nouvelle spécialité (ex: Big Data)" required>
                <button type="submit" style="flex:0;">Ajouter</button>
            </div>
        </form>
    </div>

    <div id="modules" class="card">
        <h2>Ajouter un Module</h2>
        <form action="AdminActionServlet" method="post">
            <input type="hidden" name="action" value="createModule">
            
            <div class="form-row">
                <input type="text" name="moduleName" placeholder="Nom du module (ex: Algèbre)" required>
                
                <select name="teacherId" required>
                    <option value="" disabled selected>-- Enseignant Responsable --</option>
                    <% 
                    List<User> profs = (List<User>) request.getAttribute("listeProfs");
                    if (profs != null) {
                        for (User p : profs) { 
                    %>
                        <option value="<%= p.getId() %>">
                            <%= p.getName() %> <%= p.getSurname() %>
                        </option>
                    <% 
                        } 
                    } 
                    %>
                </select>
            </div>

            <label style="font-weight:bold; display:block; margin-top:10px; margin-bottom:5px;">Associer aux spécialités :</label>
            <div style="display: flex; flex-wrap: wrap; gap: 15px; background: #f9f9f9; padding: 10px; border: 1px solid #ddd; border-radius: 4px;">
                <% 
                List<Specialty> listeSpecsPourModule = (List<Specialty>) request.getAttribute("listeSpecialites");
                if (listeSpecsPourModule != null) {
                    for (Specialty s : listeSpecsPourModule) { 
                %>
                    <div style="display: flex; align-items: center; gap: 5px;">
                        <input type="checkbox" id="spec_<%= s.getId() %>" name="specialtyIds" value="<%= s.getId() %>">
                        <label for="spec_<%= s.getId() %>"><%= s.getName() %></label>
                    </div>
                <% 
                    } 
                } 
                %>
            </div>
            
            <button type="submit" style="margin-top:15px;">Créer le Module</button>
        </form>
    </div>

    <div id="inscription" class="card">
        <h2>Valider l'inscription administrative</h2>
        <p style="font-size:0.9em; color:#666;">Seuls les étudiants sans spécialité apparaissent ici.</p>
        <form action="AdminActionServlet" method="post">
            <input type="hidden" name="action" value="createEtudiant">
            
            <div class="form-row">
                <select name="usernameEtudiant" required>
                    <option value="" disabled selected>-- Sélectionner un étudiant non-inscrit --</option>
                    <% 
                    List<User> etudiantsNonInscrits = (List<User>) request.getAttribute("etudiantsNonInscrits");
                    if (etudiantsNonInscrits != null && !etudiantsNonInscrits.isEmpty()) {
                        for (User u : etudiantsNonInscrits) { 
                    %>
                        <option value="<%= u.getUsername() %>">
                            <%= u.getName() %> <%= u.getSurname() %> (<%= u.getUsername() %>)
                        </option>
                    <% 
                        } 
                    } else {
                    %>
                        <option disabled>Tous les étudiants ont une spécialité</option>
                    <% } %>
                </select>
            </div>
            
            <div class="form-row">
                <input type="text" name="ine" placeholder="Numéro INE à attribuer" required>
                
                <select name="specialtyId" required>
                    <option value="" disabled selected>-- Spécialité --</option>
                    <% 
                    List<Specialty> specs = (List<Specialty>) request.getAttribute("listeSpecialites");
                    if (specs != null) {
                        for (Specialty s : specs) { 
                    %>
                        <option value="<%= s.getId() %>"><%= s.getName() %></option>
                    <% 
                        } 
                    } 
                    %>
                </select>
            </div>
            
            <button type="submit">Valider l'inscription</button>
        </form>
    </div>

    <div id="saisie_notes" class="card">
        <h2>Saisir une note</h2>
        <form action="AdminActionServlet" method="post">
            <input type="hidden" name="action" value="addNote">
            
            <div class="form-row">
                <select name="idEtudiant" required>
                    <option value="">-- Sélectionner un étudiant --</option>
                    <% 
                    List<User> etudiantsInscrits = (List<User>) request.getAttribute("etudiantsInscrits");
                    if(etudiantsInscrits != null) {
                        for(User u : etudiantsInscrits) { 
                    %>
                        <option value="<%= u.getId() %>">
                            <%= u.getName() %> <%= u.getSurname() %>
                        </option>
                    <% 
                        } 
                    } 
                    %>
                </select>

                <select name="idModule" required>
                    <option value="">-- Sélectionner un module --</option>
                    <% 
                    List<ModuleEntity> mods = (List<ModuleEntity>) request.getAttribute("listeModules");
                    if(mods != null) {
                        for(ModuleEntity m : mods) {
                    %>
                        <option value="<%= m.getId() %>"><%= m.getName() %></option>
                    <% 
                        } 
                    } 
                    %>
                </select>
            </div>

            <div class="form-row">
                <input type="number" step="0.5" name="valeur" placeholder="Note /20" required>
                <select name="typeNote">
                    <option value="Exam">Examen Final</option>
                    <option value="CC">Contrôle Continu</option>
                    <option value="TP">TP</option>
                </select>
                <input type="number" step="0.1" name="coef" placeholder="Coefficient" value="1.0">
            </div>
            <button type="submit">Enregistrer la note</button>
        </form>
    </div>

    <div class="card" id="suppression">
        <h2>Suppression Rapide (Maintenance)</h2>
        <div style="display:flex; gap:20px;">
            
            <div style="flex:1;">
                <h3>Professeurs</h3>
                <ul style="max-height:150px; overflow-y:auto; list-style:none; padding:0;">
                    <% 
                    List<User> tProfs = (List<User>) request.getAttribute("listeProfs");
                    if(tProfs != null) for(User p : tProfs) { %>
                    <li style="border-bottom:1px solid #eee; padding:5px; display:flex; justify-content:space-between;">
                        <%= p.getName() %> <%= p.getSurname() %>
                        <a href="#" onclick="confirmerSuppression('user', <%= p.getId() %>)">❌</a>
                    </li>
                    <% } %>
                </ul>
            </div>
    
            <div style="flex:1;">
                <h3>Modules</h3>
                <ul style="max-height:150px; overflow-y:auto; list-style:none; padding:0;">
                    <% 
                    List<ModuleEntity> tMods = (List<ModuleEntity>) request.getAttribute("listeModules");
                    if(tMods != null) for(ModuleEntity m : tMods) { %>
                    <li style="border-bottom:1px solid #eee; padding:5px; display:flex; justify-content:space-between;">
                        <%= m.getName() %>
                        <a href="#" onclick="confirmerSuppression('module', <%= m.getId() %>)">❌</a>
                    </li>
                    <% } %>
                </ul>
            </div>
        </div>
    </div>

    <div class="note" id="consultation_notes">
        <h2>Consultation des Notes par Spécialité & Modules</h2>
        
        <form action="admin" method="get">
            
            <div style="display: flex; gap: 20px; flex-wrap: wrap;">
                
                <div style="flex: 1; min-width: 250px;">
                    <label style="font-weight: bold;">1. Choisir la Spécialité :</label>
                    <select name="filterSpecId" required onchange="this.form.submit()">
                        <option value="">-- Sélectionner --</option>
                        <% 
                        String selSpecId = (String) request.getAttribute("selectedSpecId");
                        if (specs != null) {
                            for (Specialty s : specs) { 
                                String isSel = (String.valueOf(s.getId()).equals(selSpecId)) ? "selected" : "";
                        %>
                            <option value="<%= s.getId() %>" <%= isSel %>><%= s.getName() %></option>
                        <% 
                            } 
                        } 
                        %>
                    </select>
                </div>

                <div style="flex: 1; min-width: 250px;">
                    <label style="font-weight: bold;">3. Trier par :</label>
                    <% String sort = (String) request.getAttribute("selectedSort"); %>
                    <select name="sortType">
                        <option value="name_asc" <%= "name_asc".equals(sort) ? "selected" : "" %>>Nom (A-Z)</option>
                        <option value="name_desc" <%= "name_desc".equals(sort) ? "selected" : "" %>>Nom (Z-A)</option>
                        <option value="note_desc" <%= "note_desc".equals(sort) ? "selected" : "" %>>Meilleures notes d'abord</option>
                        <option value="note_asc" <%= "note_asc".equals(sort) ? "selected" : "" %>>Moins bonnes notes d'abord</option>
                    </select>
                </div>
            </div>

            <div style="margin-top: 15px; border: 1px solid #ddd; padding: 10px; border-radius: 4px; background: #f9f9f9;">
                <label style="font-weight: bold; display:block; margin-bottom:5px;">2. Filtrer par Modules (Cocher pour afficher) :</label>
                <div style="display: flex; flex-wrap: wrap; gap: 15px; max-height: 100px; overflow-y: auto;">
                    <% 
                    List<ModuleEntity> filterMods = (List<ModuleEntity>) request.getAttribute("listeModules");
                    List<Integer> selMods = (List<Integer>) request.getAttribute("selectedModules");
                    
                    if (filterMods != null) {
                        for (ModuleEntity m : filterMods) {
                            String checked = (selMods != null && selMods.contains(m.getId())) ? "checked" : "";
                    %>
                        <div style="display: flex; align-items: center; gap: 5px;">
                            <input type="checkbox" id="mod_f_<%= m.getId() %>" name="filterModuleIds" value="<%= m.getId() %>" <%= checked %>>
                            <label for="mod_f_<%= m.getId() %>"><%= m.getName() %></label>
                        </div>
                    <% 
                        } 
                    } 
                    %>
                </div>
                <p style="font-size: 0.8em; color: #666; margin-top: 5px;">* Si aucun module n'est coché, aucun résultat ne s'affichera.</p>
            </div>

            <button type="submit" style="margin-top: 15px; width: 100%;">Afficher les notes filtrées</button>
        </form>

        <% 
        List<Map<String, Object>> resultats = (List<Map<String, Object>>) request.getAttribute("resultatsNotes");
        if (resultats != null && !resultats.isEmpty()) { 
        %>
            <table style="margin-top: 20px;">
                <thead>
                    <tr>
                        <th>Nom</th>
                        <th>Prénom</th>
                        <th>Module</th>
                        <th>Note</th>
                        <th>Coef</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Map<String, Object> row : resultats) { 
                        float nVal = (float) row.get("note");
                        String color = nVal < 10 ? "red" : "green";
                    %>
                    <tr>
                        <td><strong><%= row.get("nom") %></strong></td>
                        <td><%= row.get("prenom") %></td>
                        <td><%= row.get("module") %></td>
                        <td style="color:<%= color %>; font-weight:bold;"><%= nVal %>/20</td>
                        <td><%= row.get("coef") %></td>
                        <td style="text-align: center;">
                            <a href="#" onclick="confirmerSuppression('note', <%= row.get("id") %>)" style="color:red; text-decoration:none; font-size: 1.2em;">❌</a>
                        </td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        <% } else if (selSpecId != null && !selSpecId.isEmpty()) { %>
            <p style="text-align: center; margin-top: 20px; color: #666;">Aucune note trouvée pour cette sélection. (Avez-vous coché des modules ?)</p>
        <% } %>
    </div>

    <div id="consultation" class="card">
        <h2>Liste étudiants</h2>
        
        <form action="admin" method="get" style="margin-bottom: 20px;">
            <label>Afficher les étudiants par spécialité :</label>
            <div class="form-row">
                <select name="filtreSpecialite">
                    <option value="all">-- Toutes les spécialités --</option>
                    <% 
                    if (specs != null) {
                        String currentFilter = (String) request.getAttribute("filtreActif");
                        for (Specialty s : specs) { 
                            String selected = (s.getName().equals(currentFilter)) ? "selected" : "";
                    %>
                        <option value="<%= s.getName() %>" <%= selected %>><%= s.getName() %></option>
                    <% 
                        } 
                    } 
                    %>
                </select>
                <button type="submit" style="flex:0;">Filtrer</button>
            </div>
        </form>

        <table>
            <thead>
                <tr>
                    <th>INE</th>
                    <th>Nom</th>
                    <th>Prénom</th>
                    <th>Spécialité</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <% 
                Map<Integer, String> mapIne = (Map<Integer, String>) request.getAttribute("mapIne");
                Map<Integer, String> mapSpe = (Map<Integer, String>) request.getAttribute("mapSpecialiteName");

                if (etudiantsInscrits != null && !etudiantsInscrits.isEmpty()) {
                    for (User u : etudiantsInscrits) {
                        String ineDisplay = mapIne.getOrDefault(u.getId(), "-");
                        String speDisplay = mapSpe.getOrDefault(u.getId(), "-");
                %>
                <tr>
                    <td><%= ineDisplay %></td>
                    <td><%= u.getSurname() %></td>
                    <td><%= u.getName() %></td>
                    <td><%= speDisplay %></td>
                    <td>
                        <a href="GenererBulletinServlet?id=<%= u.getId() %>" target="_blank">📄 Bulletin</a> |
                        <a href="DetailEtudiantServlet?id=<%= u.getId() %>">🔍 Détails</a>
                        <a href="#" onclick="confirmerSuppression('user', <%= u.getId() %>)" style="color:red; text-decoration:none;">❌ Supprimer</a>
                    </td>
                </tr>
                <% 
                    }
                } else {
                %>
                <tr>
                    <td colspan="5" style="text-align:center;">Aucun étudiant inscrit trouvé.</td>
                </tr>
                <% } %>
            </tbody>
        </table>
    </div>

    <div id="evaluations" class="card">
        <h2>Suivi Qualité & Statistiques</h2>
        <div class="form-row">
            <a href="StatsServlet?type=evaluations"><button type="button">📊 Voir résultats évaluations modules</button></a>
            <a href="StatsServlet?type=classement"><button type="button" style="background:#e67e22;"> Voir Classement Promo</button></a>
            <a href="LogoutServlet" class="btn-logout"><button type="button" style="background:#c0392b;">Se déconnecter</button></a>
        </div>
    </div>

</div>

</body>
</html>