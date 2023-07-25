import java.net.HttpURLConnection
import java.net.URL
import org.json.XML
import java.nio.charset.Charset

fun main() {
    var input = readLine()
    var args: Array<String> = arrayOf()
    if (input != null) {
        args = input.split(" ").toTypedArray()
    } else {
        println("Ошибка чтения ввода!")
    }

    val currencyCode = args[1].substringAfter("=")
    var date = args[2].substringAfter("=")
    val formatterDate = formatDate(date)
    val apiUrl = "https://www.cbr.ru/scripts/XML_daily.asp?date_req=$formatterDate"

    try {
        val url = URL(apiUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val data = connection.inputStream.bufferedReader(Charset.forName("windows-1251")).readText()
            val rate = parseXml(data, currencyCode)
            if (rate != null) {
                val (name, value) = rate
                println("$currencyCode ($name): $value")
            } else {
                println("Курс для валюты с кодом $currencyCode на дату $formatterDate не найден.")
            }
        } else {
            println("Ошибка при выполнении запроса. Код ответа: ${connection.responseCode}")
        }

        connection.disconnect()
    } catch (e: Exception) {
        println("Ошибка при выполнении запроса: ${e.message}")
    }
}

fun formatDate(date: String): String {
    val parts = date.split("-")
    if (parts.size == 3) {
        val day = parts[2]
        val month = parts[1]
        val year = parts[0]
        return "$month/$day/$year"
    }
    return date
}
fun parseXml(xmlData: String, currencyCode: String): Pair<String, String>? {
    val jsonObj = XML.toJSONObject(xmlData)
    val valutes = jsonObj.getJSONObject("ValCurs").getJSONArray("Valute")

    for (i in 0 until valutes.length()) {
        val valute = valutes.getJSONObject(i)
        if (valute.getString("CharCode") == currencyCode) {
            val name = valute.getString("Name")
            val value = valute.getString("Value")
            return Pair(name, value)
        }
    }
    return null
}
