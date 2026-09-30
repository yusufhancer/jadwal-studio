import os
from pathlib import Path
from server import Application, Store

# Path file database SQLite (dapat diatur lewat environment variable DATABASE_PATH)
BASE_DIR = Path(__file__).parent.resolve()
DEFAULT_DB_PATH = str(BASE_DIR / "data" / "jadwalstudio.sqlite3")
DB_PATH = os.environ.get("DATABASE_PATH", DEFAULT_DB_PATH)

# Pastikan direktori tempat database berada sudah ada
Path(DB_PATH).parent.mkdir(parents=True, exist_ok=True)

# WSGI Application instance
app = Application(Store(DB_PATH))
application = app  # Alias standar untuk WSGI runner (Gunicorn, uWSGI, PythonAnywhere)
