package com.alexb.user

import scala.concurrent.{ExecutionContext, Future}
import spray.routing.{Directives, ExceptionHandler}
import spray.httpx.SprayJsonSupport
import spray.http.HttpResponse
import spray.http.StatusCodes._
import org.joda.time.Instant
import com.alexb.utils.{ErrorDescription, ErrorDescriptionMarshallers}
import com.alexb.main.context.{ActorSystemContext, MongoSupport}

class UserService(userRepository: UserRepository)(implicit ec: ExecutionContext)
  extends Directives with SprayJsonSupport with UserMarshallers with ErrorDescriptionMarshallers {

  implicit def exceptionHandler = ExceptionHandler.fromPF {
    case e: DuplicateUsernameException => ctx =>
      ctx.complete(BadRequest, ErrorDescription("Duplicate username"))
  }

  val userRoute =
    pathPrefix("users") {
      get {
        path("check-username-free") {
          parameter('username.as[String]) { username =>
            complete(Future { CheckResult(userRepository.checkUsernameFree(username)) })
          }
        } ~
          path("check-credentials") {
            parameters('username.as[String], 'password.as[String]) { (username, password) =>
              complete(Future { CheckResult(userRepository.checkCredentials(username, password)) })
            }
          }
      } ~
      post {
        path("register") {
          entity(as[RegisterUserCommand]) { cmd =>
            ctx =>
              val result = Future {
                userRepository.save(User(cmd.username, cmd.password, Instant.now))
              } map {
                _ => HttpResponse(Created)
              }
              ctx.complete(result)
          }
        }
      }
    }
}

trait UserServiceContext { this: ActorSystemContext with MongoSupport =>

  private lazy val userRepository = new MongoUserRepository(mongoDb)
  lazy val userService = new UserService(userRepository)(actorSystem.dispatcher)
}
