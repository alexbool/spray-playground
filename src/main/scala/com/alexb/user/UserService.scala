package com.alexb.user

import akka.actor.ActorSystem
import akka.dispatch.Future
import spray.routing.{ HttpService, ExceptionHandler }
import spray.httpx.SprayJsonSupport
import spray.http.HttpResponse
import spray.http.StatusCodes._
import org.joda.time.Instant
import com.alexb.utils.{ ErrorDescription, ErrorDescriptionMarshallers }

trait UserService
	extends HttpService
	with SprayJsonSupport
	with UserMarshallers
	with ErrorDescriptionMarshallers {
	
	this: UserDao =>
	
	implicit def actorSystem: ActorSystem
	
	implicit def exceptionHandler = ExceptionHandler.fromPF { 
		case e: DuplicateUsernameException => log => ctx =>
		    ctx.complete(BadRequest, ErrorDescription("Duplicate username"))
	}
	
	val userRoute =
		pathPrefix("users") {
			get {
				path("check-username-free") {
					parameter('username.as[String]) { username =>
						complete(Future { CheckResult(checkUsernameFree(username)) })
					}
				} ~
				path("check-credentials") {
					parameters('username.as[String], 'password.as[String]) { (username, password) =>
						complete(Future { CheckResult(checkCredentials(username, password)) })
					}
				}
			} ~
			post {
				path("register") {
					entity(as[RegisterUserCommand]) { cmd => ctx =>
						val result = Future {
							save(User(cmd.username, cmd.password, Instant.now))
						} map {
							_ => HttpResponse(Created)
						}
						ctx.complete(result)
					}
				}
			}
		}
}

trait UserModule extends UserService with MongoUserDao

