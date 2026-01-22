<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.insa.sae.*" %> 

<!DOCTYPE html>
<html>

<head>
    <link rel="icon" type="image/svg+xml" href="assets/logo.svg">
    <title>Dashboard Enseignant</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

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

        select { padding: 8px; border: 1px solid #ccc; border-radius: 4px; flex: 1; }

        table { width: 100%; border-collapse: collapse; margin-top: 15px; }

        th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }

        th { background-color: #eee; }
    </style>
</head>

<body>

    <div class="sidebar">
        <div style="text-align: center; margin-bottom: 20px;">
            <img src="assets/logo.svg" alt="Logo Pronte" width="80" height="80">
        </div>
        <h3>Enseignant</h3>
        <p>
            Bienvenue, 
            <%= ((User)request.getAttribute("prof")).getName() %> 
            <%= ((User)request.getAttribute("prof")).getSurname() %>
        </p>
        <hr>
        <a href="#mes_modules">Mes modules</a>
        <a href="#notes_module">Notes</a>
        <a href="#evaluations_etudiants">Évaluations</a>
        <hr>
        <a href="LogoutServlet">Déconnexion</a>
    </div>

    <div class="content">
        <% 
        // Récupération avec les bons types
        List<ModuleEntity> modules = (List<ModuleEntity>) request.getAttribute("modulesList");
        List<String> notes = (List<String>) request.getAttribute("notesList");
        Map<String,Object> evalData = (Map<String,Object>) request.getAttribute("evaluationData");
        Integer selectedModuleId = (Integer) request.getAttribute("selectedModuleId");
        %>
    
        <div id="mes_modules" class="card">
            <h2>Mes modules</h2>
            <form action="prof" method="get">
                <div class="form-row">
                    <select name="module" onchange="this.form.submit()" required>
                        <option value="">-- Sélectionner un module --</option>
                        <% if (modules != null) { 
                            for(ModuleEntity m : modules) { 
                                String selected = (selectedModuleId != null && m.getId() == selectedModuleId) ? "selected" : "";
                        %>
                            <option value="<%= m.getId() %>" <%= selected %>>
                                <%= m.getName() %>
                            </option>
                        <%  } 
                           } %>
                    </select>
                    <button type="submit">Consulter</button>
                </div>
            </form>
        </div>
    
        <div id="notes_module" class="card">
            <h2>Notes du module</h2>
            <% if (notes != null && !notes.isEmpty()) { %>
                <table>
                    <thead>
                        <tr>
                            <th>Étudiant</th>
                            <th>Note</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for(String n : notes) { 
                            // Format "Nom Prénom:Note"
                            String[] p = n.split(":");
                            if(p.length >= 2) {
                        %>
                        <tr>
                            <td><%= p[0] %></td>
                            <td>
                                 <% 
                                 float noteVal = Float.parseFloat(p[1]);
                                 String color = noteVal < 10 ? "#dc3545" : "#28a745";
                                 %>
                                <strong style="color:<%= color %>"><%= p[1] %>/20</strong>
                            </td>
                        </tr>
                        <%  } 
                           } %>
                    </tbody>
                </table>
            <% } else { %>
                <p>Veuillez sélectionner un module ou aucune note n'est saisie.</p>
            <% } %>
        </div>

        <div id="evaluations_etudiants" class="card">
            <h2>Évaluations des étudiants</h2>
            
            <% if (evalData != null && (int)evalData.get("totalEvaluations") > 0) { 
                int[] stars = (int[]) evalData.get("statsArray");
            %>
                <div style="margin-bottom:15px;">
                    <strong>Note Moyenne : <%= evalData.get("averageRating") %>/5</strong> 
                    (<%= evalData.get("totalEvaluations") %> avis)
                </div>

                <canvas id="evalChart" height="100"></canvas>
                <script>
                    new Chart(document.getElementById('evalChart'), {
                        type: 'bar',
                        data: {
                            labels: ['1★', '2★', '3★', '4★', '5★'],
                            datasets: [{
                                label: 'Nombre de votes',
                                data: [<%= stars[1] %>, <%= stars[2] %>, <%= stars[3] %>, <%= stars[4] %>, <%= stars[5] %>],
                                backgroundColor: ['#dc3545', '#ed97a1', '#ffc107', '#7abaff', '#28a745']
                            }]
                        },
                        options: {
                            responsive: true,
                            plugins: { legend: { display: false } },
                            scales: { 
                                y: { beginAtZero: true, ticks: { stepSize: 1 } } 
                            }
                        }
                    });
                </script>
            <% } else { %>
                <p>Aucune évaluation disponible pour ce module.</p>
            <% } %>
        </div>

        <div class="card">
            <h2>Commentaires</h2>
            <% 
            if (evalData != null) {
                List<Map<String,String>> comments = (List<Map<String,String>>) evalData.get("comments");
                if (comments != null && !comments.isEmpty()) {
                    for(Map<String,String> c : comments) {
            %>
                <div style="border-bottom:1px solid #eee; padding:10px 0; position:relative;">
                    <strong style="color:#2c3e50"><%= c.get("studentName") %></strong> 
                    <span style="color:#f39c12">
                        (<%= c.get("rating") %>★)
                    </span> :
                    <br>
                    <i style="color:#555">"<%= c.get("comment") %>"</i>

                    <form action="prof" method="post" style="position:absolute; top:10px; right:0;">
                        <input type="hidden" name="action" value="deleteComment">
                        <input type="hidden" name="id" value="<%= c.get("id") %>">
                        <input type="hidden" name="module" value="<%= selectedModuleId %>">
                        <button type="submit" style="background:none; border:none; color:red; cursor:pointer; font-size:1.2em;" onclick="return confirm('Supprimer ce commentaire ?')">
                            &times;
                        </button>
                    </form>
                </div>
            <% 
                    }
                } else { out.print("<p>Aucun commentaire écrit.</p>"); }
            }
            %>
        </div>
    </div>
</body>
</html>