package com.app.figmaai.backend.config

import org.springframework.web.bind.annotation.RequestMethod
import java.io.BufferedReader
import java.security.Principal
import java.util.*
import javax.servlet.AsyncContext
import javax.servlet.DispatcherType
import javax.servlet.RequestDispatcher
import javax.servlet.ServletContext
import javax.servlet.ServletInputStream
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import javax.servlet.http.HttpUpgradeHandler
import javax.servlet.http.Part

class DummyRequest(private val method: RequestMethod, val path: String) : HttpServletRequest {

  override fun getAttribute(p0: String?): Any {
    throw NotImplementedError()
  }

  override fun getAttributeNames(): Enumeration<String> {
    throw NotImplementedError()
  }

  override fun getCharacterEncoding(): String {
    throw NotImplementedError()
  }

  override fun setCharacterEncoding(p0: String?) {
    throw NotImplementedError()
  }

  override fun getContentLength(): Int {
    throw NotImplementedError()
  }

  override fun getContentLengthLong(): Long {
    throw NotImplementedError()
  }

  override fun getContentType(): String {
    throw NotImplementedError()
  }

  override fun getInputStream(): ServletInputStream {
    throw NotImplementedError()
  }

  override fun getParameter(p0: String?): String {
    throw NotImplementedError()
  }

  override fun getParameterNames(): Enumeration<String> {
    throw NotImplementedError()
  }

  override fun getParameterValues(p0: String?): Array<String> {
    throw NotImplementedError()
  }

  override fun getParameterMap(): MutableMap<String, Array<String>> {
    throw NotImplementedError()
  }

  override fun getProtocol(): String {
    throw NotImplementedError()
  }

  override fun getScheme(): String {
    throw NotImplementedError()
  }

  override fun getServerName(): String {
    throw NotImplementedError()
  }

  override fun getServerPort(): Int {
    throw NotImplementedError()
  }

  override fun getReader(): BufferedReader {
    throw NotImplementedError()
  }

  override fun getRemoteAddr(): String {
    throw NotImplementedError()
  }

  override fun getRemoteHost(): String {
    throw NotImplementedError()
  }

  override fun setAttribute(p0: String?, p1: Any?) {
    throw NotImplementedError()
  }

  override fun removeAttribute(p0: String?) {
    throw NotImplementedError()
  }

  override fun getLocale(): Locale {
    throw NotImplementedError()
  }

  override fun getLocales(): Enumeration<Locale> {
    throw NotImplementedError()
  }

  override fun isSecure(): Boolean {
    throw NotImplementedError()
  }

  override fun getRequestDispatcher(p0: String?): RequestDispatcher {
    throw NotImplementedError()
  }

  @Deprecated("Deprecated in Java")
  override fun getRealPath(p0: String?): String {
    throw NotImplementedError()
  }

  override fun getRemotePort(): Int {
    throw NotImplementedError()
  }

  override fun getLocalName(): String {
    throw NotImplementedError()
  }

  override fun getLocalAddr(): String {
    throw NotImplementedError()
  }

  override fun getLocalPort(): Int {
    throw NotImplementedError()
  }

  override fun getServletContext(): ServletContext {
    throw NotImplementedError()
  }

  override fun startAsync(): AsyncContext {
    throw NotImplementedError()
  }

  override fun startAsync(p0: ServletRequest?, p1: ServletResponse?): AsyncContext {
    throw NotImplementedError()
  }

  override fun isAsyncStarted(): Boolean {
    throw NotImplementedError()
  }

  override fun isAsyncSupported(): Boolean {
    throw NotImplementedError()
  }

  override fun getAsyncContext(): AsyncContext {
    throw NotImplementedError()
  }

  override fun getDispatcherType(): DispatcherType {
    throw NotImplementedError()
  }

  override fun getAuthType(): String {
    throw NotImplementedError()
  }

  override fun getCookies(): Array<Cookie> {
    throw NotImplementedError()
  }

  override fun getDateHeader(p0: String?): Long {
    throw NotImplementedError()
  }

  override fun getHeader(p0: String?): String {
    throw NotImplementedError()
  }

  override fun getHeaders(p0: String?): Enumeration<String> {
    throw NotImplementedError()
  }

  override fun getHeaderNames(): Enumeration<String> {
    throw NotImplementedError()
  }

  override fun getIntHeader(p0: String?): Int {
    throw NotImplementedError()
  }

  override fun getMethod(): String = this.method.name

  override fun getPathInfo(): String = this.path

  override fun getPathTranslated(): String {
    throw NotImplementedError()
  }

  override fun getContextPath(): String {
    throw NotImplementedError()
  }

  override fun getQueryString(): String {
    throw NotImplementedError()
  }

  override fun getRemoteUser(): String {
    throw NotImplementedError()
  }

  override fun isUserInRole(p0: String?): Boolean {
    throw NotImplementedError()
  }

  override fun getUserPrincipal(): Principal {
    throw NotImplementedError()
  }

  override fun getRequestedSessionId(): String {
    throw NotImplementedError()
  }

  override fun getRequestURI(): String {
    return ""
  }

  override fun getRequestURL(): StringBuffer {
    throw NotImplementedError()
  }

  override fun getServletPath(): String {
    return ""
  }

  override fun getSession(p0: Boolean): HttpSession {
    throw NotImplementedError()
  }

  override fun getSession(): HttpSession {
    throw NotImplementedError()
  }

  override fun changeSessionId(): String {
    return ""
  }

  override fun isRequestedSessionIdValid(): Boolean {
    throw NotImplementedError()
  }

  override fun isRequestedSessionIdFromCookie(): Boolean {
    throw NotImplementedError()
  }

  override fun isRequestedSessionIdFromURL(): Boolean {
    throw NotImplementedError()
  }

  @Deprecated("Deprecated in Java")
  override fun isRequestedSessionIdFromUrl(): Boolean {
    throw NotImplementedError()
  }

  override fun authenticate(p0: HttpServletResponse?): Boolean {
    throw NotImplementedError()
  }

  override fun login(p0: String?, p1: String?) {
    throw NotImplementedError()
  }

  override fun logout() {
    throw NotImplementedError()
  }

  override fun getParts(): MutableCollection<Part> {
    throw NotImplementedError()
  }

  override fun getPart(p0: String?): Part {
    throw NotImplementedError()
  }

  override fun <T : HttpUpgradeHandler?> upgrade(p0: Class<T>?): T {
    throw NotImplementedError()
  }

}
