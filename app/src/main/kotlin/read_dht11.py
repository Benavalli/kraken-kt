import Adafruit_DHT
import sys

sensor = Adafruit_DHT.DHT11
pin = int(sys.argv[1])  # GPIO passado como argumento

humidity, temperature = Adafruit_DHT.read_retry(sensor, pin)

if humidity is not None and temperature is not None:
    print(f"{temperature},{humidity}")  # ✅ Retorna apenas números separados por vírgula
else:
    print("ERROR")  # ✅ Se der erro, retorna "ERROR"
