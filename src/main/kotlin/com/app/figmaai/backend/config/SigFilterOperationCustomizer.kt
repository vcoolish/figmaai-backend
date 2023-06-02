package com.app.figmaai.backend.config

import com.app.figmaai.backend.config.properties.WebSecurityProperties
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.SpecVersion
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.HeaderParameter
import io.swagger.v3.oas.models.security.SecurityRequirement
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.http.HttpMethod
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.method.HandlerMethod
import javax.servlet.http.HttpServletRequest

@Component
class SigFilterOperationCustomizer(
  webSecurityProperties: WebSecurityProperties
) : OperationCustomizer {

  val requestMatchers = constructAntMatchers(webSecurityProperties)

  override fun customize(operation: Operation, handlerMethod: HandlerMethod): Operation {
    val controllerRequestAnnotation =
      AnnotatedElementUtils.findMergedAnnotation(handlerMethod.beanType, RequestMapping::class.java)
    val endpointRequestAnnotation =
      AnnotatedElementUtils.findMergedAnnotation(handlerMethod.method, RequestMapping::class.java)

    if (controllerRequestAnnotation != null && endpointRequestAnnotation != null) {
      val request: HttpServletRequest = DummyRequest(
        endpointRequestAnnotation.method[0],
        (
            (controllerRequestAnnotation.path.firstOrNull() ?: "")
                + (endpointRequestAnnotation.path.firstOrNull() ?: "")
            ).takeUnless(String::isEmpty) ?: "/"
      )
      for (matcher in requestMatchers) {
        if (matcher.matches(request)) {
          operation.addSecurityItem(SecurityRequirement().addList("token"))

          addIfAbsent(operation, "figma")
          addIfAbsent(operation, "token")
          break
        }
      }
    }

    return operation
  }

  private fun addIfAbsent(operation: Operation, headerName: String) {
    if (operation.parameters.firstOrNull { it.name == headerName } == null) {
      operation.addParametersItem(
        HeaderParameter()
          .name(headerName)
          .required(true)
          .allowEmptyValue(false)
          .schema(Schema<String>(SpecVersion.V30))
      )
    }
  }

  companion object {
    fun constructAntMatchers(webSecurityProperties: WebSecurityProperties): List<RequestMatcher> =
      webSecurityProperties.roleAccessRestrictionPaths?.flatMap { restrictionPath ->
        restrictionPath.methods.takeUnless(Array<HttpMethod>::isEmpty)?.flatMap { method ->
          restrictionPath.paths.map { path: String ->
            AntPathRequestMatcher(path, method.toString())
          }
        } ?: restrictionPath.paths.map { path: String -> AntPathRequestMatcher(path) }
      } ?: emptyList()
  }

}
