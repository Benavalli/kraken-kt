import java.io.BufferedReader
import java.io.InputStreamReader

class DHT11Sensor(private val gpioPin: Int) {

    fun readData(): Pair<Float, Float>? {
        try {
            // ✅ Chama o script Python e passa o GPIO como argumento
            val process = ProcessBuilder("python3", "read_dht11.py", gpioPin.toString()).start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))

            // ✅ Captura a saída do script
            val output = reader.readLine()?.trim() ?: return null
            val error = errorReader.readLine()

            // Se houver erro, imprime e retorna null
            if (error != null || output == "ERROR") {
                println("Erro ao ler DHT11")
                return null
            }

            // ✅ Divide a saída "23.5,60.0" em temperatura e umidade
            val parts = output.split(",")
            if (parts.size != 2) return null

            val temperature = parts[0].toFloat()
            val humidity = parts[1].toFloat()

            return Pair(temperature, humidity)

        } catch (e: Exception) {
            println("Erro ao executar script Python: ${e.message}")
            return null
        }
    }
    //pip install Adafruit_DHT
    // python3 -c "import Adafruit_DHT; print(Adafruit_DHT.read_retry(11, 4))"
}

// ✅ Testando a leitura
fun main() {
    val sensor = DHT11Sensor(4) // Altere para o GPIO correto
    val result = sensor.readData()

    if (result != null) {
        println("Temperatura: ${result.first}°C, Umidade: ${result.second}%")
    } else {
        println("Falha ao obter dados do sensor!")
    }
}