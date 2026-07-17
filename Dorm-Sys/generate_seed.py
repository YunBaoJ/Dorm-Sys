import sys

buildings = ['弘毅楼', '笃学楼', '格致楼']
floors = 6
rooms_per_floor = 20
beds_per_room = 4

sql = []
sql.append("USE dormitory;")
sql.append("SET FOREIGN_KEY_CHECKS = 0;")

b_id = 100
r_id = 10000
bd_id = 100000

for b_name in buildings:
    sql.append(f"INSERT INTO building (id, name, location, admin_id) VALUES ({b_id}, '{b_name}', '主校区', 1);")
    
    for f in range(1, floors + 1):
        for r in range(1, rooms_per_floor + 1):
            room_number = f"{f}{r:02d}"  # e.g., 101, 102, ..., 120
            sql.append(f"INSERT INTO room (id, building_id, room_number, capacity, type) VALUES ({r_id}, {b_id}, '{room_number}', {beds_per_room}, '4人间');")
            
            for b in range(1, beds_per_room + 1):
                bed_number = f"床位{b}"
                sql.append(f"INSERT INTO bed (id, room_id, bed_number, status) VALUES ({bd_id}, {r_id}, '{bed_number}', 'EMPTY');")
                bd_id += 1
                
            r_id += 1
            
    b_id += 1

sql.append("SET FOREIGN_KEY_CHECKS = 1;")

with open("seed_buildings.sql", "w", encoding="utf-8") as f:
    f.write("\n".join(sql))
print("SQL file generated successfully.")
