<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WhatsOnTheMenu | Smart kitchen planning</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/home.css">
</head>
<body>
<header class="topbar">
    <a class="brand" href="<%= request.getContextPath() %>/home">
        <span class="brand-mark">W</span><span>WhatsOnTheMenu</span>
    </a>
    <nav>
        <a href="#how-it-works">How it works</a>
        <a href="#roles">For every role</a>
        <a class="nav-login" href="<%= request.getContextPath() %>/login.jsp">Log in</a>
    </nav>
</header>

<main>
    <section class="hero">
        <div class="hero-copy">
            <p class="eyebrow">SMART KITCHEN MANAGEMENT</p>
            <h1>Know what you can cook <em>before</em> you start.</h1>
            <p class="hero-text">Track your pantry, check recipe feasibility, and restock only what you need - all in one place.</p>
            <div class="hero-actions">
                <a class="button primary" href="<%= request.getContextPath() %>/login.jsp">Get started <span>→</span></a>
                <a class="button secondary" href="#how-it-works">Explore the platform</a>
            </div>
            <div class="status-line"><span class="status-dot"></span> Built for chefs, home cooks, and suppliers</div>
        </div>
        <div class="kitchen-card">
            <div class="card-top"><span>YOUR KITCHEN</span><span class="green-pill">LIVE INVENTORY</span></div>
            <h2>Tonight's possibilities</h2>
            <div class="dish-row"><span class="dish-icon orange">🍜</span><div><strong>Vegetable Ramen</strong><small>All ingredients available</small></div><span class="makeable">Makeable</span></div>
            <div class="dish-row"><span class="dish-icon yellow">🍳</span><div><strong>Breakfast Bowl</strong><small>1 ingredient is short</small></div><span class="almost">Almost</span></div>
            <div class="dish-row"><span class="dish-icon purple">🥗</span><div><strong>Garden Salad</strong><small>3 ingredients are short</small></div><span class="missing">Restock</span></div>
            <div class="card-footer"><span>Inventory updated just now</span><span>↗</span></div>
        </div>
    </section>

    <section class="stats" id="how-it-works">
        <div><strong>----</strong><span>public recipes</span></div>
        <div><strong>----</strong><span>ingredients tracked</span></div>
        <div><strong>----</strong><span>approved restockers</span></div>
        <div><strong>3</strong><span>clear status levels</span></div>
    </section>

    <section class="section-heading">
        <p class="eyebrow">ONE PLATFORM, FOUR EXPERIENCES</p>
        <h2>Everything your kitchen network needs.</h2>
        <p>WhatsOnTheMenu connects inventory, recipes, and restocking through one simple workflow.</p>
    </section>

    <section class="role-grid" id="roles">
        <article class="role-card chef"><span class="role-icon">♨</span><h3>Chef</h3><p>Manage restaurant inventory, save recipes, and send restock requests to approved suppliers.</p><a href="<%= request.getContextPath() %>/login.jsp">Manage a kitchen →</a></article>
        <article class="role-card consumer"><span class="role-icon">⌂</span><h3>Home cook</h3><p>Track your pantry, discover what you can make, and place simulated grocery orders.</p><a href="<%= request.getContextPath() %>/login.jsp">Plan a meal →</a></article>
        <article class="role-card restocker"><span class="role-icon">▣</span><h3>Restocker</h3><p>Manage your catalog and pricing, then accept or reject incoming kitchen requests.</p><a href="<%= request.getContextPath() %>/login.jsp">View requests →</a></article>
        <article class="role-card admin"><span class="role-icon">⚙</span><h3>Administrator</h3><p>Oversee users, global recipes, restocker approvals, and system-wide audit history.</p><a href="<%= request.getContextPath() %>/login.jsp">Open controls →</a></article>
    </section>

    <section class="login-banner" id="login">
        <div><p class="eyebrow">READY WHEN YOU ARE</p><h2>Turn your inventory into your next meal.</h2></div>
        <a class="button light" href="<%= request.getContextPath() %>/login.jsp">Log in to continue <span>→</span></a>
    </section>
</main>
<footer><span>© 2026 Team 10 · WhatsOnTheMenu</span><span>Three-tier architecture demo</span></footer>
</body>
</html>
