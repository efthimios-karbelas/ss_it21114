# Κατανεμημένα Συστήματα

### Καρβελάς Ευθύμιος it21114

This project runs on Docker. There is one container for the database (postgres), one for nginx and one for the app.

To install-run the project:

```
cd ss_it21114
docker compose up -d --build
```

Now wait a minute and then open http://localhost/ in a web browser.

Cleanup after done:

```
cd ss_it21114
docker compose down -v
```

Users are autocreated with script.
The password is always:

```
it21114
```

Admin username:

```
admin1
```

Volunteers usernames

```
volunteer1
volunteer2
volunteer3
...
volunteer48
volunteer49
volunteer50
```

Volunteers usernames

```
organization1
organization2
organization3
...
organization18
organization19
organization20
```