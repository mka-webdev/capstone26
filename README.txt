OAGP_FIRST - NetBeans ready Spring Boot project

What this version does:
- Reads axe-core results from results.json in the project root folder
- Stores every startup import as a NEW scan record in SQLite
- Keeps all previous scans in the database
- Shows only the most recently imported scan at http://localhost:8080/

Important behavior:
- Each time you run the app, it imports the current results.json as a new scan
- The page only displays the latest scan
- The database keeps the full history of all earlier scans

How to use:
1. Open this project in NetBeans
2. Replace results.json with your latest axe-core file when needed
3. Run the project
4. Open http://localhost:8080/

Database file:
- oagp_first.db

If you want a completely fresh database:
- Stop the app
- Delete oagp_first.db
- Run again
