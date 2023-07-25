import com.fasterxml.jackson.databind.ObjectMapper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.json.XML
import org.simpleframework.xml.core.Persister

fun main() {
    var input = readLine()
    var args: Array<String> = arrayOf()
    if (input != null) {
        args = input.split(" ").toTypedArray()
    } else {
        println("Ошибка чтения ввода!")
    }

    if (args.size != 3 || args[0] != "currency_rates") {
        println("Неправильное использование утилиты.")
        println("Используйте: currency_rates -code=USD -date=2022-10-08")
        return
    }

    val currencyCode = args[1].substringAfter("=")
    var date = args[2].substringAfter("=") // это дата с -
    val formatterDate = formatDate(date)// это с /
    val datePoint = date.replace("-", ".")
    println(formatterDate)
    val apiUrl = "https://www.cbr.ru/scripts/XML_daily.asp?date_req=$formatterDate"

    try {
        val url = URL(apiUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val data = connection.inputStream.bufferedReader().readText()
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
    val xml = jsonObj.toString(4)
//    val xml = JSONObject(org.json.XML.toJSONObject(xmlData))
    val valutes = jsonObj.getJSONObject("ValCurs").getJSONArray("Valute")

    for (i in 0 until valutes.length()) {
        val valute = valutes.getJSONObject(i)
        if (valute.getString("CharCode") == currencyCode) {
            val name = valute.getString("Name").split(" ")[0]
            val value = valute.getString("Value")
            return Pair(name, value)
        }
    }
    return null
}
