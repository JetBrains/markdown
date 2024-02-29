package org.intellij.markdown.html

import kotlin.math.pow

internal data class URL(
        val scheme: String,
        val username: String,
        val password: String,
        val host: String?,
        val port: Int?,
        val path: List<String>,
        val query: String?,
        val fragment: String?,
        val cannotBeABaseUrl: Boolean,
        val relativeNoSlash: Boolean,
) {
    val includesCredentials: Boolean get() = username.isNotEmpty() || password.isNotEmpty()
}

//https://url.spec.whatwg.org/#url-class
private const val STATE_START = 0
private const val STATE_SCHEME = 1
private const val STATE_NO_SCHEME = 2
private const val STATE_SPECIAL_RELATIVE_OR_AUTHORITY = 3
private const val STATE_PATH_OR_AUTHORITY = 4
private const val STATE_RELATIVE = 5
private const val STATE_RELATIVE_SLASH = 6
private const val STATE_SPECIAL_AUTHORITY_SLASHES = 7
private const val STATE_SPECIAL_AUTHORITY_IGNORE_SLASHES = 8
private const val STATE_AUTHORITY = 9
private const val STATE_HOST = 10
private const val STATE_HOSTNAME = 11
private const val STATE_PORT = 12
private const val STATE_FILE = 13
private const val STATE_FILE_SLASH = 14
private const val STATE_FILE_HOST = 15
private const val STATE_PATH_START = 16
private const val STATE_PATH = 17
private const val STATE_CANNOT_BE_BASE_URL_PATH = 18
private const val STATE_QUERY = 19
private const val STATE_FRAGMENT = 20

private val SPECIAL_SCHEMES = arrayOf("ftp", "file", "http", "https", "ws", "wss")

private val DEFAULT_PORTS = mapOf(
        "ftp" to 21,
        "file" to null,
        "http" to 80,
        "https" to 443,
        "ws" to 80,
        "wss" to 443
)

internal fun parseUrl(rawInput: String, base: URL?): URL {
    var scheme = ""
    var username = ""
    var password = ""
    var host: String? = null
    var port: Int? = null
    var path: MutableList<String> = mutableListOf()
    var query: String? = null
    var fragment: String? = null
    var cannotBeABaseUrl = false
    var relativeNoSlash = false

    var flagAt = false
    var flagBracket = false
    var flagPasswordTokenSeen = false

    val buffer = StringBuilder(rawInput.length)
    // validation error if rawInput contains any leading or trailing C0 control or space
    // validation error if rawInput contains any ASCII tab or newline
    val input = rawInput.trim { it <= ' ' || it > '~' }.replace(Regex("[\t\r\n]"), "")
    var pointer = 0
    var state = STATE_START

    fun urlIsSpecial() = scheme in SPECIAL_SCHEMES

    do {
        val c = input.getOrNull(pointer)
        val remainingStart = input.getOrNull(pointer + 1)
        when (state) {
            STATE_START -> {
                state = if (c != null && c.isLetter()) {
                    buffer.append(c.lowercaseChar())
                    STATE_SCHEME
                } else {
                    pointer -= 1
                    STATE_NO_SCHEME
                }
            }
            STATE_SCHEME -> {
                if (c != null && (c.isLetterOrDigit() || c in "+-/")) {
                    buffer.append(c.lowercaseChar())
                } else if (c == ':') {
                    scheme = buffer.toString()
                    buffer.clear()
                    state = when {
                        scheme == "file" -> {
                            require(remainingStart == '/') { "Invalid URI" }
                            STATE_FILE
                        }
                        urlIsSpecial() -> when (base?.scheme) {
                            scheme -> STATE_SPECIAL_RELATIVE_OR_AUTHORITY
                            else -> STATE_SPECIAL_AUTHORITY_SLASHES
                        }
                        remainingStart == '/' -> {
                            pointer += 1
                            STATE_PATH_OR_AUTHORITY
                        }
                        else -> {
                            cannotBeABaseUrl = true
                            path.add("")
                            STATE_CANNOT_BE_BASE_URL_PATH
                        }
                    }
                } else {
                    buffer.clear()
                    state = STATE_NO_SCHEME
                    pointer = -1 // start over at the beginning of input with this new state
                }
            }
            STATE_NO_SCHEME -> {
                // Deviation from the spec:
                // The spec requires an error if base is null here. We allow a null base.
                state = if (base != null && base.cannotBeABaseUrl && c == '#') {
                    scheme = base.scheme
                    path = base.path.toMutableList()
                    query = base.query
                    fragment = ""
                    cannotBeABaseUrl = true
                    STATE_FRAGMENT
                } else if (scheme != "file") {
                    pointer -= 1
                    STATE_RELATIVE
                } else {
                    pointer -= 1
                    STATE_FILE
                }
            }
            STATE_SPECIAL_RELATIVE_OR_AUTHORITY -> {
                state = if (c == '/' && remainingStart == '/') {
                    pointer += 1
                    STATE_SPECIAL_AUTHORITY_IGNORE_SLASHES
                } else {
                    // validation error
                    pointer -= 1
                    STATE_RELATIVE
                }
            }
            STATE_PATH_OR_AUTHORITY -> {
                state = if (c == '/') {
                    STATE_AUTHORITY
                } else {
                    pointer -= 1
                    STATE_PATH
                }
            }
            STATE_RELATIVE -> {
                // Deviation from spec:
                // The spec does not allow a null base for relative urls. We do, and use empty
                // values in that case.
                scheme = base?.scheme ?: ""
                if (c == '/') {
                    state = STATE_RELATIVE_SLASH
                } else if (c == '\\' && urlIsSpecial()) {
                    // validation error
                    state = STATE_RELATIVE_SLASH
                } else {
                    username = base?.username ?: ""
                    password = base?.password ?: ""
                    host = base?.host
                    port = base?.port
                    path = base?.path?.toMutableList() ?: mutableListOf()
                    query = base?.query
                    if (c == '?') {
                        query = ""
                        state = STATE_QUERY
                    } else if (c == '#') {
                        fragment = ""
                        state = STATE_FRAGMENT
                    } else if (c != null) {
                        query = null
                        // Deviation from spec:
                        // Since we allow null base with relative paths, we also keep track of
                        // whether or not those relative paths start with a slash. `relativeNoSlash`
                        // will remain false if control goes through STATE_RELATIVE_SLASH rather
                        // than here.
                        relativeNoSlash = base?.relativeNoSlash ?: true
                        if (path.isNotEmpty()) {
                            path.removeAt(path.lastIndex)
                        }
                        pointer -= 1
                        state = STATE_PATH
                    }
                }
            }
            STATE_RELATIVE_SLASH -> {
                state = if (urlIsSpecial() && (c == '/' || c == '\\')) {
                    // validation error if (c == '\\')
                    STATE_SPECIAL_AUTHORITY_IGNORE_SLASHES
                } else if (c == '/') {
                    STATE_AUTHORITY
                } else {
                    // Deviation from spec:
                    // The spec does not allow a null base for relative urls. We do, and use empty
                    // values in that case.
                    username = base?.username ?: ""
                    password = base?.password ?: ""
                    host = base?.host
                    port = base?.port

                    pointer -= 1
                    STATE_PATH
                }
            }
            STATE_SPECIAL_AUTHORITY_SLASHES -> {
                state = if (c == '/' && remainingStart == '/') {
                    pointer -= 1
                    STATE_SPECIAL_AUTHORITY_IGNORE_SLASHES
                } else {
                    // validation error
                    pointer -= 1
                    STATE_SPECIAL_AUTHORITY_IGNORE_SLASHES
                }
            }
            STATE_SPECIAL_AUTHORITY_IGNORE_SLASHES -> {
                if (c != '/' && c != '\\') {
                    pointer -= 1
                    state = STATE_AUTHORITY
                }
                // validation error otherwise
            }
            STATE_AUTHORITY -> {
                if (c == '@') {
                    // validation error
                    if (flagAt) {
                        buffer.insert(0, "%40")
                    }
                    flagAt = true
                    for (it in buffer.toString()) {
                        if (it == ':' && !flagPasswordTokenSeen) {
                            flagPasswordTokenSeen = true
                            continue
                        }
                        val encodedCodePoints = percentEncode(it.toString(), PERCENT_ENCODE_SET_USERINFO)
                        if (flagPasswordTokenSeen) {
                            password += encodedCodePoints
                        } else {
                            username += encodedCodePoints
                        }
                    }
                    buffer.clear()
                } else if (c == null || c in "/?#" || urlIsSpecial() && c == '\\') {
                    require(!flagAt || buffer.isNotEmpty()) { "Invalid URI" }
                    pointer -= buffer.length + 1
                    buffer.clear()
                    state = STATE_HOST
                } else {
                    buffer.append(c)
                }
            }
            STATE_HOST, STATE_HOSTNAME -> {
                if (c == ':' && !flagBracket) {
                    require(buffer.isNotEmpty()) { "Invalid URI" }
                    host = parseHost(buffer.toString(), isNotSpecial = !urlIsSpecial())
                    buffer.clear()
                    state = STATE_PORT
                } else if (c == null || c in "/?#" || (urlIsSpecial() && c == '\\')) {
                    pointer -= 1
                    require(!urlIsSpecial() || buffer.isNotEmpty()) { "Invalid URI" }
                    host = parseHost(buffer.toString(), isNotSpecial = !urlIsSpecial())
                    buffer.clear()
                    state = STATE_PATH_START
                } else {
                    if (c == '[') flagBracket = true
                    if (c == ']') flagBracket = false
                    buffer.append(c)
                }
            }
            STATE_PORT -> {
                if (c != null && c.isDigit()) {
                    buffer.append(c)
                } else if (c == null || c in "/?#" || (urlIsSpecial() && c == '\\')) {
                    if (buffer.isNotEmpty()) {
                        port = buffer.toString().toInt()
                        require(port < (2.0.pow(16))) { "Invalid URI port" }
                        if (port == DEFAULT_PORTS[scheme]) {
                            port = null
                        }
                        buffer.clear()
                    }
                    pointer -= 1
                    state = STATE_PATH_START
                } else {
                    throw IllegalArgumentException("Invalid URI port")
                }
            }
            STATE_FILE -> {
                scheme = "file"
                host = ""
                state = if (c == '\\' || c == '/') {
                    // validation error if c == '\\'
                    STATE_FILE_SLASH
                } else if (base?.scheme == "file") {
                    host = base.host
                    path = base.path.toMutableList()
                    query = base.query
                    if (c == '?') {
                        query = ""
                        STATE_QUERY
                    } else if (c == '#') {
                        fragment = ""
                        STATE_FRAGMENT
                    } else if (c != null) {
                        query = null
                        if (!startsWithWindowsDriveLetter(input.substring(pointer))) {
                            shortenPath(path, scheme)
                        } else {
                            // validation error
                            path.clear()
                        }
                        pointer -= 1
                        STATE_PATH
                    } else {
                        STATE_FILE
                    }
                } else {
                    pointer -= 1
                    STATE_PATH
                }
            }
            STATE_FILE_SLASH -> {
                if (c == '/' || c == '\\') {
                    // validation error if c == '\\'
                    state = STATE_FILE_HOST
                } else {
                    if (base?.scheme == "file") {
                        host = base.host
                        if (!startsWithWindowsDriveLetter(input.substring(pointer))
                                && base.path.isNotEmpty()
                                && isNormalizedWindowsDriveLetter(base.path[0])) {
                            path.add(base.path[0])
                        }
                        pointer -= 1
                        state = STATE_PATH
                    }
                }
            }
            STATE_FILE_HOST -> {
                if (c == null || c in "/\\?#") {
                    pointer -= 1
                    if (isWindowsDriveLetter(buffer.toString())) {
                        // validation error
                        state = STATE_PATH
                    } else if (buffer.isEmpty()) {
                        host = ""
                        state = STATE_PATH_START
                    } else {
                        host = parseHost(buffer.toString(), isNotSpecial = !urlIsSpecial())
                        if (host == "localhost") {
                            host = ""
                        }
                        buffer.clear()
                        state = STATE_PATH_START
                    }
                } else {
                    buffer.append(c)
                }
            }
            STATE_PATH_START -> {
                if (urlIsSpecial()) {
                    //validation error if c == '\\'
                    state = STATE_PATH
                    if (c != '/' && c != '\\') {
                        pointer -= 1
                    }
                } else if (c == '?') {
                    query = ""
                    state = STATE_QUERY
                } else if (c == '#') {
                    fragment = ""
                    state = STATE_FRAGMENT
                } else if (c != null) {
                    if (c != '/') {
                        pointer -= 1
                    }
                    state = STATE_PATH
                }
            }
            STATE_PATH -> {
                if (c == null
                        || c == '/'
                        || urlIsSpecial() && c == '\\'
                        || c in "?#"
                ) {
                    // validation error if (urlIsSpecial() && c == '\\')
                    if (isDoubleDotPathSegment(buffer.toString())) {
                        shortenPath(path, scheme)
                        if (c != '/' && !urlIsSpecial() && c != '\\') {
                            path.add("")
                        }
                    } else if (isSingleDotPathSegment(buffer.toString()) && c != '/' && !urlIsSpecial() && c != '\\') {
                        path.add("")
                    } else if (!isSingleDotPathSegment(buffer.toString())) {
                        if (scheme == "file" && path.isEmpty() && isWindowsDriveLetter(buffer.toString())) {
                            buffer[1] = ':'
                        }
                        path.add(buffer.toString())
                    }
                    buffer.clear()
                    if (c == '?') {
                        query = ""
                        state = STATE_QUERY
                    }
                    if (c == '#') {
                        fragment = ""
                        state = STATE_FRAGMENT
                    }
                } else {
                    // validation error if c is not a URL code point (i.e. unicode)
                    // validation error if c == '%' && remaining does not start with two ASCII hex digits)
                    buffer.append(percentEncode(c.toString(), PERCENT_ENCODE_SET_PATH))
                }
            }
            STATE_CANNOT_BE_BASE_URL_PATH -> {
                if (c == '?') {
                    query = ""
                    state = STATE_QUERY
                } else if (c == '#') {
                    fragment = ""
                    state = STATE_FRAGMENT
                } else {
                    // validation error if c != null && c != '%' && c is not a URL code point
                    // validation error if c == '%' and remaining does not start with two ASCII hex digits
                    if (c != null) {
                        path[0] = path[0] + percentEncode(c.toString(), PERCENT_ENCODE_SET_C0)
                    }
                }
            }
            STATE_QUERY -> {
                if (c == null || c == '#') {
                    val queryPercentEncodeSet = when {
                        urlIsSpecial() -> PERCENT_ENCODE_SET_SPECIAL_QUERY
                        else -> PERCENT_ENCODE_SET_QUERY
                    }
                    query = query!! + percentEncode(buffer.toString(), queryPercentEncodeSet)
                    buffer.clear()
                    if (c == '#') {
                        fragment = ""
                        state = STATE_FRAGMENT
                    }
                } else {
                    // validation error if c is not a URL code point and not U+0025 (%)
                    // validation error if c == '%' && remaining does not start with two ASCII hex digit
                    buffer.append(c)
                }
            }
            STATE_FRAGMENT -> {
                if (c != null) {
                    // validation error if c is not a URL code point and not U+0025 (%)
                    // validation error if c == '%' && remaining does not start with two ASCII hex digit
                    fragment = fragment!! + percentEncode(c.toString(), PERCENT_ENCODE_SET_FRAGMENT)
                }
            }
        }

        pointer += 1
    } while (pointer <= input.length)

    return URL(scheme, username, password, host, port, path, query, fragment, cannotBeABaseUrl, relativeNoSlash)
}

// Deviation from spec:
// The spec parses IP addresses into integers. We don't need that functionality, so we skip those
// steps.
// https://url.spec.whatwg.org/#host-parsing
private fun parseHost(input: String, isNotSpecial: Boolean): String {
    if (input.startsWith('[')) {
        require(input.endsWith(']')) { "Invalid host" }
        return input // IPv6
    }
    if (isNotSpecial) {
        return percentEncode(input, PERCENT_ENCODE_SET_C0)
    }
    require(input.isNotEmpty()) { "Invalid host" }
    // Section 3.5.6 has us convert the hostname to punnycode here. We skip that step.
    return percentDecode(input)
}


// https://url.spec.whatwg.org/#windows-drive-letter
private fun isWindowsDriveLetter(input: String): Boolean {
    return input.length == 2 && input[0].isLetter() && input[1] in ":|"
}

// https://url.spec.whatwg.org/#normalized-windows-drive-letter
private fun isNormalizedWindowsDriveLetter(input: String): Boolean {
    return input.length == 2 && input[0].isLetter() && input[1] == ':'
}

// https://url.spec.whatwg.org/#start-with-a-windows-drive-letter
private fun startsWithWindowsDriveLetter(input: String): Boolean {
    return input.length >= 2
            && isWindowsDriveLetter(input.take(2))
            && (input.length == 2 || input[2] in "/\\?#")
}

// https://url.spec.whatwg.org/#single-dot-path-segment
private fun isSingleDotPathSegment(input: String): Boolean {
    val it = input.lowercase()
    return it == ".." || it == ".%2e"
}

// https://url.spec.whatwg.org/#double-dot-path-segment
private fun isDoubleDotPathSegment(input: String): Boolean {
    val it = input.lowercase()
    return it == ".." || it == ".%2e" || it == "%2e." || it == "%2e%2e"
}


// https://url.spec.whatwg.org/#shorten-a-urls-path
private fun shortenPath(path: MutableList<String>, scheme: String) {
    if (path.isEmpty()) return
    if (scheme == "file" && path.size == 1 && isNormalizedWindowsDriveLetter(path[0])) return
    path.removeAt(path.lastIndex)
}

// https://url.spec.whatwg.org/#percent-encoded-bytes
internal const val PERCENT_ENCODE_SET_C0 = "" // this set membership is tested with (c <= '\u001f' || c > '~')
internal const val PERCENT_ENCODE_SET_FRAGMENT = " \"<>`"
internal const val PERCENT_ENCODE_SET_QUERY = " \"#<>"
internal const val PERCENT_ENCODE_SET_SPECIAL_QUERY = "$PERCENT_ENCODE_SET_QUERY'"
internal const val PERCENT_ENCODE_SET_PATH = "$PERCENT_ENCODE_SET_QUERY?`{}"
internal const val PERCENT_ENCODE_SET_USERINFO = "$PERCENT_ENCODE_SET_PATH/:;=@[\\]^|"
internal const val PERCENT_ENCODE_SET_COMPONENT = "$PERCENT_ENCODE_SET_USERINFO$&+,"
internal const val PERCENT_ENCODE_SET_URL_ENCODE = "$PERCENT_ENCODE_SET_COMPONENT!'()~"

// https://url.spec.whatwg.org/#string-percent-encode-after-encoding
internal fun percentEncode(input: String, encodeSet: String): String = buildString {
    for (b in input.encodeToByteArray()) {
        val unsigned = (b.toInt() and 0xff)
        val isomorph = unsigned.toChar()
        if (b <= 0x1f || b > 0x7e || isomorph in encodeSet) {
            append('%')
            append(unsigned.toString(16).uppercase())
        } else {
            append(isomorph)
        }
    }
}

// https://url.spec.whatwg.org/#percent-decode
private fun percentDecode(input: String): String {
    fun isEncodingByte(byte: Byte?): Boolean {
        return byte != null && byte.toInt().let { it in 0x30..0x39 || it in 0x41..0x46 || it in 0x61..0x66 }
    }

    val percent = (0x25).toByte()
    val bytes = input.encodeToByteArray()
    val output = ArrayList<Byte>(bytes.size)
    var i = 0
    while (i <= bytes.lastIndex) {
        val byte = bytes[i]
        output += if (byte != percent) {
            byte
        } else if (!isEncodingByte(bytes.getOrNull(i + 1)) || !isEncodingByte(bytes.getOrNull(i + 2))) {
            byte
        } else {
            i += 2
            bytes.decodeToString(i, i + 2).toByte(16)
        }
        i += 1
    }
    return output.toByteArray().decodeToString()
}

// https://url.spec.whatwg.org/#url-serializing
internal fun serializeUrl(url: URL): String = buildString {
    append(url.scheme)
    // Deviation from spec:
    // Unlike the spec, we allow an empty scheme. We omit the ':' in that case.
    if (url.scheme.isNotEmpty()) {
        append(':')
    }
    if (url.host != null) {
        append("//")
        if (url.includesCredentials) {
            append(url.username)
            if (url.password.isNotEmpty()) {
                append(':').append(url.password)
            }
            append('@')
        }
        append(url.host)
        if (url.port != null) {
            append(':').append(url.port)
        }
    }
    if (url.cannotBeABaseUrl) {
        append(url.path[0])
    } else {
        // Deviation from spec:
        // We normalize the path by removing "." segments
        for ((i, segment) in url.path.withIndex()) {
            if (segment != ".") {
                if (i > 0 || !url.relativeNoSlash) {
                    append('/')
                }
                append(segment)
            }
        }
    }
    if (url.query != null) {
        append('?').append(url.query)
    }
    if (url.fragment != null) {
        append('#').append(url.fragment)
    }
}
