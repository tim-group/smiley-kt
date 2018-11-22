package com.timgroup.smileykt

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.ws.rs.core.HttpHeaders

@Path("gauth_user")
class ProxiedGoogleAuthResources {
    @GET
    @Produces("application/json")
    fun showDetails(@Context headers: HttpHeaders): Map<String, String> {
        val claims = mutableMapOf<String, String>()
        headers.requestHeaders.forEach { name, value ->
            if (name.startsWith("OIDC_CLAIM_")) {
                claims[name.substring("OIDC_CLAIM_".length)] = value[0]
            }
        }
        return claims
    }
}