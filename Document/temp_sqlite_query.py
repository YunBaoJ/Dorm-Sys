import sqlite3
from datetime import datetime

DB_PATH = r"C:\Users\Administrator\.local\share\mimocode\mimocode.db"

conn = sqlite3.connect(DB_PATH)
c = conn.cursor()

# Check the sibling project c2a8723b sessions
SIBLING_PROJECT = "c2a8723b-6d92-42b6-b287-9cc85703f41a"
print("=== SIBLING PROJECT (c2a8723b) SESSIONS ===")
c.execute("""
    SELECT id, title, time_created, directory
    FROM session 
    WHERE project_id = ?
    ORDER BY time_created ASC
""", (SIBLING_PROJECT,))
for row in c.fetchall():
    sid, title, tc, directory = row
    tc_str = datetime.fromtimestamp(tc/1000).strftime('%Y-%m-%d %H:%M') if tc else 'N/A'
    print(f"  {sid} | {tc_str} | {(title or 'N/A')[:100]}")

# Check other project 32512a8a sessions
OTHER_PROJECT = "32512a8a-a887-44c4-929f-8c77ae1f1d9a"
print(f"\n=== OTHER PROJECT (32512a8a) SESSIONS ===")
c.execute("""
    SELECT id, title, time_created, directory
    FROM session 
    WHERE project_id = ?
    ORDER BY time_created ASC
""", (OTHER_PROJECT,))
for row in c.fetchall():
    sid, title, tc, directory = row
    tc_str = datetime.fromtimestamp(tc/1000).strftime('%Y-%m-%d %H:%M') if tc else 'N/A'
    print(f"  {sid} | {tc_str} | {(title or 'N/A')[:100]}")

conn.close()
