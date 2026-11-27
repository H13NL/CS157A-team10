<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    String error = request.getParameter("error");
%>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Sign Up | WhatsOnTheMenu</title>

    <link rel="stylesheet"
          href="<%= request.getContextPath() %>/assets/css/app.css">
</head>

<body class="auth-page">

<header class="topbar auth-topbar">

    <a class="brand"
       href="<%= request.getContextPath() %>/index.jsp">

        <span class="brand-mark">W</span>
        <span>WhatsOnTheMenu</span>
    </a>

    <a class="back-link"
       href="<%= request.getContextPath() %>/login.jsp">

        Already have an account? Log in
    </a>

</header>

<main class="auth-layout">

    <section class="auth-intro">

        <p class="eyebrow">JOIN WHATSONTHEMENU</p>

        <h1>
            Build your pantry and plan your next meal.
        </h1>

        <p>
            Create a consumer account to track ingredients,
            explore recipes, and place simulated grocery orders.
        </p>

    </section>

    <section class="auth-card">

        <h2>Create your account</h2>

        <p class="form-help">
            Sign up as a consumer to get started.
        </p>

        <% if ("missing".equals(error)) { %>
            <p class="demo-note">
                Please complete every field.
            </p>
        <% } else if ("mismatch".equals(error)) { %>
            <p class="demo-note">
                Passwords do not match.
            </p>
        <% } else if ("duplicate".equals(error)) { %>
            <p class="demo-note">
                An account with that email already exists.
            </p>
        <% } %>

        <form action="<%= request.getContextPath() %>/signup"
              method="post">

            <label for="name">Full name</label>

            <input id="name"
                   name="name"
                   type="text"
                   placeholder="Your name"
                   required>

            <label for="email">Email address</label>

            <input id="email"
                   name="email"
                   type="email"
                   placeholder="you@example.com"
                   required>

            <label for="password">Password</label>

            <input id="password"
                   name="password"
                   type="password"
                   placeholder="Create a password"
                   required>

            <label for="confirmPassword">
                Confirm password
            </label>

            <input id="confirmPassword"
                   name="confirmPassword"
                   type="password"
                   placeholder="Re-enter your password"
                   required>

            <button class="button primary full-width"
                    type="submit">

                Create account
                <span>→</span>
            </button>

        </form>

    </section>

</main>

</body>
</html>