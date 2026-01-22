<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.insa.sae.*" %> 

<!DOCTYPE html>
<html>
<head>
    <link rel="icon" type="image/svg+xml" href="assets/logo.svg">
    <title>Espace Étudiant</title>
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

        .sidebar a { 
            display: block; 
            color: white; 
            padding: 10px; 
            text-decoration: none; 
            margin-bottom: 5px; 
            border-radius: 4px;
        }
        
        .sidebar a:hover { background: #34495e; }

        .content { 
            margin-left: 250px; 
            padding: 20px; 
            min-height: 100vh;
        }

        .card { 
            background: white; 
            padding: 20px; 
            margin-bottom: 20px; 
            border-radius: 8px; 
            box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1); 
            scroll-margin-top: 20px;
        }

        h2 { border-bottom: 2px solid #0056b3; padding-bottom: 10px; }
        .form-row { display: flex; gap: 10px; margin-bottom: 10px; }
        select, input, textarea { padding: 8px; width: 100%; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box;}
        button { padding: 10px; background: #0056b3; color: white; border: none; border-radius: 4px; cursor: pointer; }
        table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }
        th { background-color: #eee; }
        .alert { color: #155724; background-color: #d4edda; border-color: #c3e6cb; padding: 10px; margin-bottom: 20px; border-radius: 4px;}
        .note-bad { color: #dc3545; font-weight: bold; }
        .note-good { color: #28a745; font-weight: bold; }
    </style>
</head>

<body>

    <div class="sidebar">
        <div style="text-align: center; margin-bottom: 20px;">
            <img src="assets/logo.svg" alt="Logo Pronte" width="80" height="80">
        </div>
        <h3>Espace Étudiant</h3>
        <p>
            Bienvenue, 
            <%= ((User)request.getAttribute("etudiant")).getName() %> 
            <%= ((User)request.getAttribute("etudiant")).getSurname() %>
        </p>
        <hr>
        <a href="#mes_notes">📘 Mes notes</a>
        
        <a href="DetailEtudiantServlet?id=<%= ((User)request.getAttribute("etudiant")).getId() %>">
            🔍 Mon Dossier Détaillé
        </a>
        
        <a href="#evaluer_module">📝 Évaluer un module</a>
        <hr>
        <a href="LogoutServlet">Déconnexion</a>
    </div>

    <div class="content">

        <% if (request.getAttribute("message") != null) { %>
            <div class="alert">
                <%= request.getAttribute("message") %>
            </div>
        <% } %>

        <div id="mes_notes" class="card">
            <h2>Aperçu de mes notes</h2>
            
            <% 
            List<Map<String, Object>> mesNotes = (List<Map<String, Object>>) request.getAttribute("mesNotes");
            if (mesNotes != null && !mesNotes.isEmpty()) { 
            %>
                <table>
                    <thead>
                        <tr>
                            <th>Module</th>
                            <th>Type</th>
                            <th>Note</th>
                            <th>Coef</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Map<String, Object> note : mesNotes) { 
                             float val = (float) note.get("note");
                             String cssClass = (val < 10) ? "note-bad" : "note-good";
                        %>
                            <tr>
                                <td><%= note.get("module") %></td>
                                <td><%= note.get("type") %></td>
                                <td class="<%= cssClass %>"><%= val %> / 20</td>
                                <td><%= note.get("coef") %></td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            <% } else { %>
                <p>Aucune note enregistrée pour le moment.</p>
            <% } %>
        </div>

        <div id="evaluer_module" class="card">
            <h2>Évaluer un module</h2>

            <form method="post" action="etudiant">
                <input type="hidden" name="action" value="evaluerModule">

                <div class="form-row">
                    <select name="module" required>
                        <option value="">-- Sélectionner un module --</option>
                        <% 
                        List<ModuleEntity> listeModules = (List<ModuleEntity>) request.getAttribute("listeModules");
                        if (listeModules != null) {
                            for(ModuleEntity m : listeModules) {
                        %>
                            <option value="<%= m.getId() %>"><%= m.getName() %></option>
                        <% 
                            }
                        } 
                        %>
                    </select>
                </div>

                <div class="form-row">
                    <input type="number" name="supports" min="1" max="5" placeholder="Qualité des supports (1-5)" required>
                    <input type="number" name="equipe" min="1" max="5" placeholder="Équipe pédagogique (1-5)" required>
                    <input type="number" name="charge" min="1" max="5" placeholder="Charge de travail (1-5)" required>
                </div>

                <div class="form-row">
                    <textarea name="commentaire" placeholder="Votre avis (optionnel)" rows="3"></textarea>
                </div>

                <button type="submit">Envoyer l’évaluation</button>
            </form>
        </div>

    </div>

</body>
</html>