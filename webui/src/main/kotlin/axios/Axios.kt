package axios

import jsext.Dictionary
import kotlin.js.Promise

external interface AxiosClient {
    fun <T> get(url: String, config: BaseRequestConfig = definedExternally): Promise<Response<T>>
}

@JsModule("axios")
external object Axios : AxiosClient {
    override fun <T> get(url: String, config: BaseRequestConfig): Promise<Response<T>>
    fun <T> get(url: String): Promise<Response<T>>

    fun isCancel(thrown: Throwable): Boolean

    @JsName("CancelToken")
    object cancel {
        fun source(): CancelTokenSource
    }

    fun create(config: BaseRequestConfig = definedExternally): AxiosClient
}

external interface BaseRequestConfig {
    var withCredentials: Boolean // default: false
    var baseURL: String? // default: none
    var headers: dynamic // e.g. (not default): { "X-Requested-With": "XMLHttpRequest" }
    @JsName("timeout")
    var timeoutMillis: Int // default: ?
    var responseType: String // default: json
    var responseEncoding: String // default: utf8
    var xsrfCookieName: String // default "XSRF-TOKEN"
    var xsrfHeaderName: String // default "X-XSRF-TOKEN"
    var onUploadProgress: (ProgressEvent) -> Unit // default no-op
    var onDownloadProgress: (ProgressEvent) -> Unit // default no-op
    var maxContentLength: Int // default: ?
    var maxRedirects: Int // default: 5
    var socketPath: String? // default: null
    var proxy: ProxyConfig?
    var cancelToken: CancelTokenHandle? // default: none
}

external interface CancelTokenSource {
    val token: CancelTokenHandle
    fun cancel(message: String = definedExternally)
}

external interface CancelTokenHandle

external interface ProgressEvent

external interface ProxyConfig {
    var host: String
    var port: Int
    var auth: ProxyAuthConfig
}

external interface ProxyAuthConfig {
    var username: String
    var password: String
}

external interface Response<T> {
    val data: T
    val status: Int
    val statusText: String
    val headers: Dictionary<String>
    val config: BaseRequestConfig
    val request: dynamic
}
