package com.timgroup.smileykt

import java.time.Instant
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.ws.rs.core.HttpHeaders

@Path("oidc_user")
class ProxiedOpenIdAuthResources {
    @GET
    @Produces("application/json")
    fun showDetails(@Context headers: HttpHeaders): Map<String, Any> {
        val userId: String = headers.getHeaderString("X-OIDC-User") ?: return emptyMap()

        val claims = mutableMapOf<String, Any>()
        headers.requestHeaders.forEach { name, value ->
            if (name.startsWith("OIDC_CLAIM_")) {
                val claimKey = name.substring("OIDC_CLAIM_".length).toLowerCase()
                val claimString: String = value[0]
                claims[claimKey] = when (claimKey) {
                    "iat" -> Instant.ofEpochSecond(claimString.toLong())
                    "exp" -> Instant.ofEpochSecond(claimString.toLong())
                    "email_verified" -> claimString.toInt() != 0
                    else -> claimString
                }
            }
        }

        return mapOf("userId" to userId, "claims" to claims)
    }
}
