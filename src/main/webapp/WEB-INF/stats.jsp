<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.insa.sae.Specialty" %>

<!DOCTYPE html>
<html>
<head>
    <title>Statistiques - Admin</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        body { font-family: 'Segoe UI', sans-serif; margin: 0; display: flex; background: #f4f4f9; }
        .sidebar { width: 250px; background: #2c3e50; color: white; min-height: 100vh; padding: 20px; }
        .sidebar a { display: block; color: white; padding: 10px; text-decoration: none; margin-bottom: 5px; border-radius: 4px; }
        .sidebar a:hover { background: #34495e; }
        .content { flex: 1; padding: 30px; }
        .card { background: white; padding: 25px; margin-bottom: 25px; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }
        h1, h2 { color: #2c3e50; margin-top: 0; }
        
        table { width: 100%; border-collapse: collapse; margin-top: 15px; }
        th, td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #f8f9fa; color: #495057; font-weight: 600; }
        tr:hover { background-color: #f1f1f1; }
        
        .badge { padding: 5px 10px; border-radius: 15px; font-size: 0.85em; font-weight: bold; color: white;}
        .badge-success { background-color: #28a745; }
        .badge-warning { background-color: #ffc107; color: #333; }
        .badge-danger { background-color: #dc3545; }
        
        .medal { font-size: 1.5em; }
        
        .nav-link.active { background: #0056b3; color: white; }
        

        select { padding: 8px; border-radius: 4px; border: 1px solid #ccc; font-size: 14px; min-width: 200px; }
    </style>
</head>
<body>

<div class="sidebar">
    <h3>Administration</h3>
    <a href="admin">← Retour Dashboard</a>
    <hr>
    <a href="StatsServlet?type=classement" class="<%= "classement".equals(request.getAttribute("viewType")) ? "active" : "" %>">🏆 Classement Promo</a>
    <a href="StatsServlet?type=evaluations" class="<%= "evaluations".equals(request.getAttribute("viewType")) ? "active" : "" %>">📊 Évaluations & Réussite</a>
</div>

<div class="content">
    <% String viewType = (String) request.getAttribute("viewType"); %>

    <% if ("classement".equals(viewType)) { %>
        <div class="card">
            <div style="display:flex; justify-content:space-between; align-items:center;">
                <h1>🏆 Classement de la Promotion</h1>
                
                <form action="StatsServlet" method="get">
                    <input type="hidden" name="type" value="classement">
                    <label style="font-weight:bold; margin-right:10px;">Filtrer par Spécialité :</label>
                    <select name="specId" onchange="this.form.submit()">
                        <option value="">-- Toute les promos --</option>
                        <% 
                        List<Specialty> specs = (List<Specialty>) request.getAttribute("listeSpecs");
                        String selectedId = (String) request.getAttribute("selectedSpecId");
                        if (specs != null) {
                            for (Specialty s : specs) {
                                String isSel = (String.valueOf(s.getId()).equals(selectedId)) ? "selected" : "";
                        %>
                            <option value="<%= s.getId() %>" <%= isSel %>><%= s.getName() %></option>
                        <% 
                            }
                        }
                        %>
                    </select>
                </form>
            </div>
            
            <p>Moyenne générale pondérée de tous les étudiants.</p>
            
            <table>
                <thead>
                    <tr>
                        <th width="50">Rang</th>
                        <th>Étudiant</th>
                        <th>Spécialité</th>
                        <th>Moyenne Générale</th>
                        <th>Mention</th>
                    </tr>
                </thead>
                <tbody>
                    <% 
                    List<Map<String, Object>> list = (List<Map<String, Object>>) request.getAttribute("classementList");
                    if (list != null && !list.isEmpty()) {
                        int rank = 1;
                        for (Map<String, Object> u : list) {
                            double moy = (double) u.get("moyenne");
                            String mention = "Insuffisant";
                            String badgeClass = "badge-danger";
                            
                            if (moy >= 16) { mention = "Très Bien"; badgeClass = "badge-success"; }
                            else if (moy >= 14) { mention = "Bien"; badgeClass = "badge-success"; }
                            else if (moy >= 12) { mention = "Assez Bien"; badgeClass = "badge-warning"; }
                            else if (moy >= 10) { mention = "Passable"; badgeClass = "badge-warning"; }
                    %>
                    <tr>
                        <td>
                            <% if(rank==1) out.print("<span class='medal'>🥇</span>"); 
                               else if(rank==2) out.print("<span class='medal'>🥈</span>"); 
                               else if(rank==3) out.print("<span class='medal'>🥉</span>"); 
                               else out.print("<strong>"+rank+"</strong>"); %>
                        </td>
                        <td>
                            <strong><%= u.get("nom") %></strong> <%= u.get("prenom") %>
                            <a href="DetailEtudiantServlet?id=<%= u.get("id") %>" style="text-decoration:none; font-size:0.8em; margin-left:5px;">🔍</a>
                        </td>
                        <td><%= u.get("specialite") %></td>
                        <td style="font-size:1.1em; font-weight:bold;"><%= String.format("%.2f", moy) %>/20</td>
                        <td><span class="badge <%= badgeClass %>"><%= mention %></span></td>
                    </tr>
                    <%      rank++; 
                        } 
                    } else { %>
                        <tr><td colspan="5" style="text-align:center;">Aucun étudiant trouvé pour cette sélection.</td></tr>
                    <% } %>
                </tbody>
            </table>
        </div>

    <% } else if ("evaluations".equals(viewType)) { %>
        
        <div class="card">
            <h1>📊 Performance des Modules</h1>
            <p>Comparaison entre la satisfaction des étudiants (Évaluations) et leurs résultats réels (Notes).</p>
            <div style="height: 300px; width: 100%;">
                <canvas id="moduleChart"></canvas>
            </div>
        </div>

        <div class="card">
            <h2>Détails par Module</h2>
            <table>
                <thead>
                    <tr>
                        <th>Module</th>
                        <th>Satisfaction Étudiants</th>
                        <th>Moyenne de Classe</th>
                        <th>Taux de Réussite</th>
                    </tr>
                </thead>
                <tbody>
                    <% 
                    List<Map<String, Object>> stats = (List<Map<String, Object>>) request.getAttribute("statsModules");
                    
                    StringBuilder labels = new StringBuilder("[");
                    StringBuilder dataRating = new StringBuilder("[");
                    StringBuilder dataPassRate = new StringBuilder("[");
                    
                    if (stats != null) {
                        for (Map<String, Object> m : stats) {
                            double rating = m.get("avgRating") != null ? (double) m.get("avgRating") : 0.0;
                            double passRate = (double) m.get("tauxReussite");
                            double classAvg = (double) m.get("moyenneModule");
                            int nbAvis = (int) m.get("nbAvis");
                            
                            labels.append("'").append(m.get("moduleName")).append("',");
                            dataRating.append(rating * 20).append(","); 
                            dataPassRate.append(passRate).append(",");
                    %>
                    <tr>
                        <td><strong><%= m.get("moduleName") %></strong></td>
                        <td>
                            <div style="display:flex; align-items:center;">
                                <span style="color:#f39c12; font-size:1.2em; margin-right:5px;">★</span>
                                <strong><%= String.format("%.1f", rating) %>/5</strong>
                                <span style="color:#777; font-size:0.8em; margin-left:5px;">(<%= nbAvis %> avis)</span>
                            </div>
                        </td>
                        <td><strong><%= String.format("%.2f", classAvg) %>/20</strong></td>
                        <td>
                            <div style="width: 100%; background-color: #eee; border-radius: 5px; height: 10px; width: 100px; display: inline-block; margin-right: 5px;">
                                <div style="width: <%= passRate %>%; background-color: <%= passRate < 50 ? "#dc3545" : "#28a745" %>; height: 100%; border-radius: 5px;"></div>
                            </div>
                            <strong><%= String.format("%.0f", passRate) %>%</strong>
                        </td>
                    </tr>
                    <% 
                        } 
                    } 
                    labels.append("]");
                    dataRating.append("]");
                    dataPassRate.append("]");
                    %>
                </tbody>
            </table>
        </div>

        <script>
            const ctx = document.getElementById('moduleChart').getContext('2d');
            new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: <%= labels.toString() %>,
                    datasets: [
                        {
                            label: 'Taux de Réussite (%)',
                            data: <%= dataPassRate.toString() %>,
                            backgroundColor: 'rgba(40, 167, 69, 0.6)',
                            borderColor: 'rgba(40, 167, 69, 1)',
                            borderWidth: 1,
                            yAxisID: 'y'
                        },
                        {
                            label: 'Satisfaction (Convertie en %)',
                            data: <%= dataRating.toString() %>,
                            type: 'line',
                            borderColor: '#f39c12',
                            backgroundColor: '#f39c12',
                            borderWidth: 3,
                            pointRadius: 5,
                            yAxisID: 'y'
                        }
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        y: {
                            beginAtZero: true,
                            max: 100,
                            title: { display: true, text: 'Pourcentage' }
                        }
                    },
                    plugins: {
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    let label = context.dataset.label || '';
                                    if (label) {
                                        label += ': ';
                                    }
                                    if (context.raw !== null) {
                                        label += Math.round(context.raw) + '%';
                                    }
                                    return label;
                                }
                            }
                        }
                    }
                }
            });
        </script>
    <% } %>
</div>

</body>
</html>