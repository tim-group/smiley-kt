package com.timgroup.smileykt

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class RecordHappinessServlet : HttpServlet() {

    val happinesses = mutableMapOf<String, String>()

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        if (req.contentType.toMimeType() == "application/x-www-form-urlencoded") {
            val email: String = req.getParameter("email") ?: return resp.sendError(400, "Email must be supplied")
            val happiness: String = req.getParameter("happiness") ?: return resp.sendError(400, "Happiness must be supplied")
            happinesses[email] = happiness
            println(happinesses)
            resp.status = HttpServletResponse.SC_OK
        }
        else {
            resp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE)
        }
    }

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.contentType = "text/plain"
        resp.writer.use { writer ->
            happinesses.forEach { email, happiness ->
                writer.println("$email $happiness")
            }
        }
    }

    private fun String.toMimeType(): String {
        val sepOffset = indexOf(';')
        if (sepOffset < 0)
            return toLowerCase()
        else
            return substring(0, sepOffset).toLowerCase()
    }
}
