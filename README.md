# Κατανεμημένα Συστήματα

### Καρβελάς Ευθύμιος it21114

This project runs on Docker. There is one container for the database (postgres), one for nginx and one for the app.

To install-run the project:

```
cd ss_it21114
docker compose up -d --build
```

Now open http://localhost/ in a web browser.

Cleanup after done:

```
cd ss_it21114
docker compose down -v
```