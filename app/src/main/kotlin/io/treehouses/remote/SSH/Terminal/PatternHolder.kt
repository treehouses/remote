package io.treehouses.remote.SSH.Terminal

import java.util.regex.Pattern

object PatternHolder {
    var urlPattern: Pattern? = null

    init {
        // based on http://www.ietf.org/rfc/rfc2396.txt
        val scheme = "[A-Za-z][-+.0-9A-Za-z]*"
        val unreserved = "[-._~0-9A-Za-z]"
        val pctEncoded = "%[0-9A-Fa-f]{2}"
        val subDelims = "[!$&'()*+,;:=]"
        val userinfo = "(?:$unreserved|$pctEncoded|$subDelims|:)*"
        val h16 = "[0-9A-Fa-f]{1,4}"
        val decOctet = "(?:[0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])"
        val ipv4address: String = "$decOctet\\.$decOctet\\.$decOctet\\.$decOctet"
        val ls32 = "(?:$h16:$h16|$ipv4address)"
        val ipv6address = "(?:(?:$h16){6}$ls32)"
        val ipvfuture = "v[0-9A-Fa-f]+.(?:$unreserved|$subDelims|:)+"
        val ipLiteral = "\\[(?:$ipv6address|$ipvfuture)\\]"
        val regName = "(?:$unreserved|$pctEncoded|$subDelims)*"
        val host = "(?:$ipLiteral|$ipv4address|$regName)"
        val port = "[0-9]*"
        val authority = "(?:$userinfo@)?$host(?::$port)?"
        val pchar = "(?:$unreserved|$pctEncoded|$subDelims|@)"
        val segment: String = "$pchar*"
        val pathAbempty = "(?:/$segment)*"
        val segmentNz: String = "$pchar+"
        val pathAbsolute = "/(?:$segmentNz(?:/$segment)*)?"
        val pathRootless: String = "$segmentNz(?:/$segment)*"
        val hierPart = "(?://$authority$pathAbempty|$pathAbsolute|$pathRootless)"
        val query = "(?:$pchar|/|\\?)*"
        val fragment = "(?:$pchar|/|\\?)*"
        val uriRegex: String = "$scheme:$hierPart(?:$query)?(?:#$fragment)?"
        urlPattern = Pattern.compile(uriRegex)
    }
}