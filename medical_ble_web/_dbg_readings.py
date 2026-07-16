import sqlite3

c = sqlite3.connect(r"data/poc.db")
c.row_factory = sqlite3.Row

print("=== newest 15 by measured_at ===")
for r in c.execute(
    """
    select r.id, r.brand, r.reading_type, r.measured_at, r.created_at,
           r.systolic, r.spo2, r.temperature, d.mac
    from readings r
    left join devices d on d.id = r.device_id
    order by coalesce(r.measured_at, r.created_at) desc, r.id desc
    limit 15
    """
):
    print(dict(r))

print("session 31 count", c.execute("select count(*) from readings where session_id=31").fetchone()[0])
print("session 30 count", c.execute("select count(*) from readings where session_id=30").fetchone()[0])

print("=== last omron by id ===")
for r in c.execute(
    "select id, measured_at, created_at, systolic, session_id from readings where brand='omron' order by id desc limit 5"
):
    print(dict(r))

print("=== last masimo by id ===")
for r in c.execute(
    "select id, measured_at, created_at, spo2, session_id from readings where brand='masimo' order by id desc limit 3"
):
    print(dict(r))
