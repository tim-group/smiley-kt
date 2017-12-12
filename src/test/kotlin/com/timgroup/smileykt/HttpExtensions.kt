package com.timgroup.smileykt

import org.apache.http.HttpEntity
import org.apache.http.entity.ContentType
import org.apache.http.util.EntityUtils

val HttpEntity.parsedContentType: ContentType?
    get() = ContentType.get(this)

val HttpEntity.mimeType: String?
    get() = parsedContentType?.mimeType?.toLowerCase()

fun HttpEntity.readText(): String? = EntityUtils.toString(this)
