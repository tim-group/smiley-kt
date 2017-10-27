package com.timgroup.smileykt

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class RecordHappinessServlet : HttpServlet() {

    val happinesses = mutableMapOf<String, String>()

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val email: String = req.getParameter("email") ?: return resp.sendError(400, "Email must be supplied")
        val happiness: String = req.getParameter("happiness") ?: return resp.sendError(400, "Happiness must be supplied")
        happinesses[email] = happiness
        println(happinesses)
        resp.status = 200
    }

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.contentType = "text/plain"
        resp.writer.use { writer ->
            happinesses.forEach { email, happiness ->
                writer.println("$email $happiness")
            }
        }
    }
}