import random
import pika
import bisect
import json
from datetime import datetime
import numpy as np

url = 'amqps://zknqdylg:TWrQvZmf4ZWjp2LU3ZL21jkiDqpDp9tW@cow.rmq2.cloudamqp.com/zknqdylg'

params = pika.URLParameters(url)
connection = pika.BlockingConnection(params)
channel = connection.channel() 
channel.queue_declare(queue='monitoring_queue') 

# (Index, Consumption_kW)
KEY_POINTS = [
    (0, 0.3),    # 00:00 - Low base load (fridges, standby)
    (42, 1.5),   # 07:00 - Morning peak (showers, kettles, lights)
    (54, 1.2),   # 09:00 - Morning drop (people leave for work/school)
    (72, 0.8),   # 12:00 - Daytime lull (some home activity)
    (102, 1.0),  # 17:00 - People start returning home
    (114, 2.2),  # 19:00 - **Evening peak** (cooking, HVAC, entertainment)
    (126, 1.8),  # 21:00 - Peak starts to drop
    (138, 0.5),  # 23:00 - Winding down for bed
    (144, 0.3)   # 24:00 - Back to base load (must match 00:00 for a smooth loop)
]

breakpoints = [42, 54, 72, 102, 114, 126, 138]

def get_current_consumption(index):
    
    base = KEY_POINTS[bisect.bisect_right(breakpoints, index)][1]
    
    noise = random.uniform(-0.15, 0.15)
    
    noise_factor = base * noise
    
    value = base + noise_factor
    rounded_value = round(value, 2)
    
    return max(0, rounded_value)

given_date = input("Insert the date(DD-MM-YYYY): ")
device_id = input("Insert Device ID: ")
try:
    parsed_date = datetime.strptime(given_date.strip(), "%d-%m-%Y")
except ValueError:
    raise SystemExit("Invalid date format, expected DD-MM-YYYY")

for index in range(143):
    total_minutes = index * 10
    hour = total_minutes // 60
    minute = total_minutes % 60
    timestamp = parsed_date.replace(hour=hour, minute=minute, second=0, microsecond=0)
    consumption = get_current_consumption(index)
    
    data = {
        "timestamp": timestamp.isoformat(),
        "device":{
            "id": device_id
        },
        "consumption": consumption
    }
    
    message = json.dumps(data)
    
    #print(message)
    
    channel.basic_publish(exchange='',
                      routing_key='monitoring_queue',
                      body=message)

connection.close()