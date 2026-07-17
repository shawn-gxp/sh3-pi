import sqlite3
import pprint
conn = sqlite3.connect('data/poc.db')
conn.row_factory = sqlite3.Row
rows = conn.execute("SELECT id, reading_type, measured_at, created_at, temperature FROM readings WHERE brand='thermo' ORDER BY id DESC LIMIT 10").fetchall()
for row in rows:
    print(dict(row))
