<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    String error = request.getParameter("error");
    String success = request.getParameter("success");
%>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Change Password | WhatsOnTheMenu</title>

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

        Log out
    </a>

</header>

<main class="auth-layout">

    <section class="auth-intro">

        <p class="eyebrow">ACCOUNT SETTINGS</p>

        <h1>Update your password.</h1>

        <p>
            Keep your WhatsOnTheMenu account secure by changing
            your password regularly.
        </p>

    </section>

    <section class="auth-card">

        <h2>Change password</h2>

        <% if ("missing".equals(error)) { %>
            <p class="demo-note">
                Please complete every field.
            </p>
        <% } else if ("mismatch".equals(error)) { %>
            <p class="demo-note">
                The new passwords do not match.
            </p>
        <% } else if ("short".equals(error)) { %>
            <p class="demo-note">
                The new password must contain at least 6 characters.
            </p>
        <% } else if ("incorrect".equals(error)) { %>
            <p class="demo-note">
                Your current password is incorrect.
            </p>
        <% } else if ("1".equals(success)) { %>
            <p class="demo-note">
                Your password was changed successfully.
            </p>
        <% } %>

        <form action="<%= request.getContextPath() %>/change-password"
              method="post">

            <label for="currentPassword">
                Current password
            </label>

            <input id="currentPassword"
                   name="currentPassword"
                   type="password"
                   required>

            <label for="newPassword">
                New password
            </label>

            <input id="newPassword"
                   name="newPassword"
                   type="password"
                   minlength="6"
                   required>

            <label for="confirmPassword">
                Confirm new password
            </label>

            <input id="confirmPassword"
                   name="confirmPassword"
                   type="password"
                   minlength="6"
                   required>

            <button class="button primary full-width"
                    type="submit">

                Change password
                <span>→</span>
            </button>

        </form>

    </section>

</main>

</body>
</html>