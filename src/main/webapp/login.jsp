<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <link rel="icon" type="image/svg+xml" href="assets/logo.svg">
    <title>Connexion - Pronte</title>
    <style>
        body { font-family: Arial, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; background-color: #f4f4f9; }
        .login-container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); width: 300px; }
        .form-group { margin-bottom: 15px; }
        label { display: block; margin-bottom: 5px; font-weight: bold; }
        input[type="text"], input[type="password"] { width: 100%; padding: 8px; box-sizing: border-box; border: 1px solid #ccc; border-radius: 4px; }
        button { width: 100%; padding: 10px; background-color: #0056b3; color: white; border: none; border-radius: 4px; cursor: pointer; }
        button:hover { background-color: #004494; }
        .error { color: red; font-size: 0.9em; margin-bottom: 15px; text-align: center; }
        .signup-link { text-align: center; margin-top: 15px; font-size: 0.9em; }
    </style>
</head>
<body>
    
<div class="login-container">
    <div style="text-align: center; margin-bottom: 20px;">
        <img src="assets/logo.svg" alt="Logo Pronte" width="80" height="80">
    </div>
    <h2 style="text-align: center;">Connexion</h2>
    <%-- Gestion du message de déconnexion --%>
    <% if (request.getParameter("logout") != null) { %>
        <div style="color: green; background-color: #d4edda; border: 1px solid #c3e6cb; padding: 10px; margin-bottom: 15px; text-align: center; border-radius: 5px;">
            Vous avez été déconnecté avec succès.
        </div>
    <% } %>
    <% if (request.getAttribute("errorMessage") != null) { %>
        <div class="error"><%= request.getAttribute("errorMessage") %></div>
    <% } %>

    <form action="Login_Servlet" method="post">
        <div class="form-group">
            <label for="username">Nom d'utilisateur</label>
            <input type="text" id="username" name="username" required placeholder="Ex: jdupont">
        </div>

        <div class="form-group">
            <label for="mdp">Mot de passe</label>
            <input type="password" id="mdp" name="mdp" required>
        </div>

        <button type="submit">Se connecter</button>
    </form>

    <div class="signup-link">
        Pas encore de compte ? <a href="sign_up.jsp">S'inscrire</a>
    </div>
</div>

</body>
</html>