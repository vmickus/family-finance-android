package com.financasdacasa.app.util

import com.financasdacasa.app.BuildConfig

/**
 * Resolves a relative path (e.g. "/uploads/avatars/xxx.jpg") to an absolute URL
 * using the server base URL derived from API_BASE_URL.
 */
fun resolveServerUrl(path: String?): String? {
    if (path == null) return null
    if (path.startsWith("http://") || path.startsWith("https://")) return path
    // API_BASE_URL is like "http://host:8080/api" — strip the /api suffix to get server root
    val baseUrl = BuildConfig.API_BASE_URL.removeSuffix("/api").removeSuffix("/")
    return "$baseUrl$path"
}
