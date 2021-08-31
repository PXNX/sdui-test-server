package nyx69.plugins

import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable
import java.io.File
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import java.util.concurrent.TimeUnit

fun Application.configureSecurity() {

    /*  authentication {
          jwt("auth-jwt") {


              val jwtAudience = "https://rw-ktor-server.herokuapp.com/hello"
              realm = "jwt.realm"
     /*       val  issuer = "http://0.0.0.0:8080/"
          val    audience = "http://0.0.0.0:8080/hello"


              val jwkProvider = JwkProviderBuilder(issuer)
                  .cached(10, 24, TimeUnit.HOURS)
                  .rateLimited(10, 1, TimeUnit.MINUTES)
                  .build() */

              verifier(
                  JWT
                      .require(Algorithm.HMAC256("secret"))
                      .withAudience(jwtAudience)
                      .withIssuer("jwt.domain")
                      .build()
              )
              validate { credential ->
               //   if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null

                      if (credential.payload.getClaim("username").asString() != "") {
                          JWTPrincipal(credential.payload)
                      } else {
                          null
                      }

              }






          }
      }


      /////
      data class MySession(val count: Int = 0)
      install(Sessions) {
          cookie<MySession>("MY_SESSION") {
              cookie.extensions["SameSite"] = "lax"
          }
      }

      routing {
          get("/session/increment") {
              val session = call.sessions.get<MySession>() ?: MySession()
              call.sessions.set(session.copy(count = session.count + 1))
              call.respondText("Counter is ${session.count}. Refresh to increment.")
          }

          post("/login") {
              val user = call.receive<User>()

              val publicKey = jwkProvider.get("6f8856ed-9189-488f-9011-0ff4b6c08edc").publicKey
              val keySpecPKCS8 = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyString))
              val privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpecPKCS8)
              val token = JWT.create()
                  .withAudience(audience)
                  .withIssuer(issuer)
                  .withClaim("username", user.username)
                  .withExpiresAt(Date(System.currentTimeMillis() + 60000))
                  .sign(Algorithm.RSA256(publicKey as RSAPublicKey, privateKey as RSAPrivateKey))
              call.respond(hashMapOf("token" to token))
          }



          authenticate("auth-jwt") {
              get("/hello") {
                  val principal = call.principal<JWTPrincipal>()
                  val username = principal!!.payload.getClaim("username").asString()
                  val expiresAt = principal.expiresAt?.time?.minus(System.currentTimeMillis())
                  call.respondText("Hello, $username! Token is expired at $expiresAt ms.")
              }
          }


      }

     */

    val privateKeyString =
        "MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAtfJaLrzXILUg1U3N1KV8yJr92GHn5OtYZR7qWk1Mc4cy4JGjklYup7weMjBD9f3bBVoIsiUVX6xNcYIr0Ie0AQIDAQABAkEAg+FBquToDeYcAWBe1EaLVyC45HG60zwfG1S4S3IB+y4INz1FHuZppDjBh09jptQNd+kSMlG1LkAc/3znKTPJ7QIhANpyB0OfTK44lpH4ScJmCxjZV52mIrQcmnS3QzkxWQCDAiEA1Tn7qyoh+0rOO/9vJHP8U/beo51SiQMw0880a1UaiisCIQDNwY46EbhGeiLJR1cidr+JHl86rRwPDsolmeEF5AdzRQIgK3KXL3d0WSoS//K6iOkBX3KMRzaFXNnDl0U/XyeGMuUCIHaXv+n+Brz5BDnRbWS+2vkgIe9bUNlkiArpjWvX+2we"
    val issuer = "https://rw-ktor-server.herokuapp.com"
    val audience = "https://rw-ktor-server.herokuapp.com/hello"
    val myRealm = "Access to 'hello'"

    val jwkProvider = JwkProviderBuilder(issuer)
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = myRealm
            verifier(jwkProvider, issuer) {
                acceptLeeway(3)
            }
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
    routing {
        post("/login") {
            val user = call.receive<User>()
            // TODO Check username and password
            val publicKey = jwkProvider.get("6f8856ed-9189-488f-9011-0ff4b6c08edc").publicKey
            val keySpecPKCS8 = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyString))
            val privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpecPKCS8)
            val token = JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim("username", user.username)
                .withExpiresAt(Date(System.currentTimeMillis() + 60000))
                .sign(Algorithm.RSA256(publicKey as RSAPublicKey, privateKey as RSAPrivateKey))
            call.respond(hashMapOf("token" to token))
        }

        authenticate("auth-jwt") {
            get("/hello") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("username").asString()
                val expiresAt = principal.expiresAt?.time?.minus(System.currentTimeMillis())
                call.respondText("Hello, $username! Token will expire in $expiresAt ms.")
            }
        }
        static(".well-known") {
            staticRootFolder = File("certs")
            file("jwks.json")
        }
    }


}

@Serializable
data class User(val username: String, val password: String)