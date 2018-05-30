package io.datawire.conntest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import io.javalin.Javalin
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("io.datawire.conntest.ConnTest")

val mapper: ObjectMapper = ObjectMapper().registerModules(
    Jdk8Module(),
    JavaTimeModule(),
    KotlinModule(),
    ParameterNamesModule())

data class HttpRequestInfo(
    val method: String,
    val path: String,
    val queryParams: Map<String, Array<String>>,
    val headers: Map<String, String>,
    val remoteAddress: String,
    val remotePort: Int,
    val remoteHost: String?,
    val remoteUser: String?
)

data class HttpInfo(
    val host: String,
    val request: HttpRequestInfo
)

data class WebSocketSessionInfo(
    val id: String,
    val queryParams: Map<String, Array<String>>
)

data class WebSocketInfo(
    val host: String,
    val session: WebSocketSessionInfo
)

fun main(args: Array<String>) {
  val hostname = System.getenv("HOSTNAME")

  val api = Javalin
      .create()
      .port(System.getenv("SRV_PORT")?.toIntOrNull() ?: 7000)

  api.get("/") { ctx ->
    logger.info("request received: {}", ctx.ip())
    val requestInfo = HttpRequestInfo(
        method        = ctx.method(),
        path          = ctx.path(),
        queryParams   = ctx.queryParamMap(),
        headers       = ctx.headerMap(),
        remoteAddress = ctx.request().remoteAddr,
        remotePort    = ctx.request().remotePort,
        remoteHost    = ctx.request().remoteHost,
        remoteUser    = ctx.request().remoteUser
    )

    val info = HttpInfo(
        host = hostname,
        request = requestInfo
    )

    ctx.status(200).contentType("application/json").result(jsonify(info))
  }

  api.ws("/ws") { ws ->
    ws.onConnect { session ->
      logger.info("session started: {}", session.id)

      val sessionInfo = WebSocketSessionInfo(
          id = session.id,
          queryParams = if (session.queryString() != null) session.queryParamMap() else emptyMap()
      )

      val info = WebSocketInfo(
          host = hostname,
          session = sessionInfo
      )

      session.send(jsonify(sessionInfo))
    }

    ws.onMessage { session, msg ->
      logger.info("session activity: {}", session.id)
      session.send(msg)
    }

    ws.onError { session, throwable ->
      logger.error("session error: {}", session.id, throwable)
    }

    ws.onClose { session, code, reason ->
      logger.info("session ended (code={} reason={}): {}", code, reason ?: code, session.id)
    }
  }

  api.start()
}

fun jsonify(model: Any): String {
  return mapper.writer().withDefaultPrettyPrinter().writeValueAsString(model)
}

inline fun <reified T: Any> unjsonify(payload: String): T {
  return mapper.readValue(payload)
}
