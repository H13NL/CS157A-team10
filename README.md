# WhatsOnTheMenu (CS157A Team 10)

## How to run

**Need:** Java 8+, MySQL Server + Workbench, Eclipse IDE for Enterprise Java (Tomcat 9 is included in `apache-tomcat-9.0.100/`).

1. **Get the branch:** `git clone -b sparsh https://github.com/H13NL/CS157A-team10.git`
2. **Create the database:** in Workbench, connect to your local server (port 3306), then File → Open SQL Script → run `db/schema.sql`, then `db/seed.sql`. This creates the `testproject` schema with sample data. Re-running `schema.sql` wipes all tables.
3. **Set your MySQL root password** as the environment variable `WHATS_MENU_DB_PASSWORD`. In Eclipse: Servers → double-click Tomcat → *Open launch configuration* → Environment → add it.
4. **Run:** in Eclipse, File → Import → *Existing Projects into Workspace* → pick the folder. If needed, add the Tomcat server (Window → Preferences → Server → Runtime Environments → Apache Tomcat v9.0 → point to `apache-tomcat-9.0.100`). Then right-click the project → Run As → *Run on Server*.
5. **Log in** at `http://localhost:8080/<project-name>/login.jsp` with a sample account (password `pass123`):

| Email | Restaurant |
|---|---|
| `chef@test.com` | Team 10 Kitchen |
| `chef2@test.com` | Team 10 Kitchen (shared stock) |
| `bistro@test.com` | Spartan Bistro |

**Troubleshooting:** *Login always fails* → check MySQL is running, step 2 was done, and the password variable is set in the Tomcat launch configuration. *Eclipse shows a missing `mysql-connector` library* → remove the old `C:/Users/...` entry under Build Path; the driver is already in `WebContent/WEB-INF/lib/`.
