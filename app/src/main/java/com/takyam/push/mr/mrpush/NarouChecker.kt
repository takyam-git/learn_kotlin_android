package com.takyam.push.mr.mrpush

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Level

/**
 * Created by takyam on 2016/03/20.
 */
class NarouChecker {
    companion object {
        val url = "http://syosetu.com/favnovelmain/isnoticelist/"
        val loginUrl = "https://ssl.syosetu.com/login/login/"
        var sessionCookie = ""
        val userAgent = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.76 Mobile Safari/537.36"
        fun updateSession(id: String, pw: String) {
            Log.i("LOGIN", "ID: ${id}, PW: ${pw}")
            Fuel.post(loginUrl)
                    .body("id=${URLEncoder.encode(id, "UTF-8")}&pass=${URLEncoder.encode(pw, "UTF-8")}")
                    .header(Pair("Content-Type", "application/x-www-form-urlencoded"))
                    .header(Pair("User-Agent", userAgent))
                    .response { request, response, result ->
                        Log.i("LOGIN", response.httpStatusCode.toString())

                        if (response.httpStatusCode != 302) {
                            Log.i("LOGIN", "invalid http status code: ${response.httpStatusCode.toString()}")
                            return@response
                        }

                        response.httpResponseHeaders.forEach { entry ->
                            Log.i("ENTRY", entry.toString())
                            if (entry.key == "Set-Cookie") {
                                entry.value.first().split(";").forEach { cval ->
                                    Log.i("COOKIE PARSER", cval.toString())
                                    val co = cval.split("=")
                                    Log.i("co", co.toString())
                                    if (co.size == 2 && co[0] == "ses") {
                                        sessionCookie = "${cval.trim()};"
                                        Log.i("NEW COOKIE", sessionCookie)
                                        return@response
                                    }
                                }
                            }
                        }
                    }
        }
    }

    var isActive: Boolean = false
    var arrivals: ArrayList<Novel> = arrayListOf<Novel>()

    fun check(onFetched: (ArrayList<Novel>) -> Unit) {
        val newArrivals = arrayListOf<Novel>()
        if (sessionCookie == "") {
            Log.i("CHECK", "Session is empty")
            onFetched(newArrivals)
            return
        }
        isActive = true
        Fuel.get(url)
                .header(Pair("Cookie", sessionCookie))
                .header(Pair("User-Agent", userAgent))
                .responseString { request, response, result ->
                    Log.i("INFO", arrivals.size.toString())
                    result.fold({ body ->
                        val document = Jsoup.parse(body)
                        val novels = document.select("#contents table.favnovel");
                        novels.forEach { novel ->
                            val title = novel!!.select("a.title").text()
                            val number = novel.select("p.no a").last().text().replace(Regex("[^\\d]"), "").toInt()
                            val updateTimeString = novel.select("td.info p").first().text().replace("チェック中", "").replace("更新日：", "").trim()
                            val updateTime = try {
                                SimpleDateFormat("yyyy/MM/dd kk:mm").parse(updateTimeString)
                            } catch(e: ParseException) {
                                null
                            }
                            val novel = Novel(title, number, updateTime)
                            if (!arrivals.contains(novel)) {
                                newArrivals.add(novel)
                                arrivals.add(novel)
                                Log.i(Level.INFO.toString(), "GET NEW ARRIVAL!! ${novel.toString()}")
                            } else {
                                Log.i(Level.INFO.toString(), "EXISTS: ${novel.toString()}")
                            }
                        }

                        while (arrivals.size > 1000) {
                            arrivals.removeAt(0)
                        }
                        onFetched(newArrivals)
                    }, { err ->
                        Log.i(Level.INFO.toString(), err.toString())
                        onFetched(newArrivals)
                    })
                    isActive = false
                }
    }
}