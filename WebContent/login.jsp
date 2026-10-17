<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Log in | WhatsOnTheMenu</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/app.css">
</head>
<body class="auth-page">
<header class="topbar auth-topbar">
    <a class="brand" href="<%= request.getContextPath() %>/home">
        <span class="brand-mark">W</span><span>WhatsOnTheMenu</span>
    </a>
    <a class="back-link" href="<%= request.getContextPath() %>/home">← Back to home</a>
</header>

<main class="auth-layout">
    <section class="auth-intro">
        <p class="eyebrow">WELCOME BACK</p>
        <h1>Make your kitchen work smarter.</h1>
        <p>Log in to manage your restaurant inventory, organize recipes, and see what you can make today.</p>
        <div class="auth-points">
            <div><span>✓</span><strong>Live inventory</strong><small>See what is available right now.</small></div>
            <div><span>✓</span><strong>Recipe feasibility</strong><small>Know what is makeable before cooking.</small></div>
            <div><span>✓</span><strong>One shared workspace</strong><small>Keep your restaurant information together.</small></div>
        </div>
    </section>

    <section class="auth-card">
        <p class="eyebrow">ACCOUNT LOGIN</p>
        <h2>Log in to your account</h2>
        <p class="form-help">Use the email and password associated with your account.</p>
        <form action="<%= request.getContextPath() %>/login" method="post">
            <label for="email">Email address</label>
            <input id="email" name="email" type="email" placeholder="you@example.com" required>

            <label for="password">Password</label>
            <input id="password" name="password" type="password" placeholder="Enter your password" required>
            <button class="button primary full-width" type="submit">Log in <span>→</span></button>
        </form>
    </section>
</main>
</body>
</html>
