import org.http4s.server._
import scalaz._
import Scalaz._
import scalaz.concurrent.Task
import org.http4s.server.blaze._
import org.http4s._
import org.http4s.dsl._

object Main extends ServerApp {
  type Data = String
  case class UserData(name: String, authData: Task[String\/Data])


  val helloWorldService = HttpService {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name.")
  }
  override def server(args: List[String]): Task[Server] = {

    def theResponse(s: String) = {
      Ok("I emulate functions: " + s)
    }
    //def retrieveUser: Service[Long, User] = Kleisli(id => Task.delay(???))

    val authUser: Service[Request, String\/UserData] = Kleisli(req =>
      Task.delay{
        val userHeader = req.headers.toList.find(_.name.value == "user") \/> "No user provided"
        println(req.headers)
        println(userHeader)

        val usernameOrError = userHeader.map(_.value).flatMap(u =>
          if (u == "bela")
            \/-(u)
          else
            -\/("No such user")
        )

        val data: Task[String\/Data] = req.as[String].map{
          s =>
            if (s contains "palacsinta") \/-(s)
            else -\/("Nem palacsinta")
        }

        usernameOrError.map {
          un => UserData(un, data)
        }
      }
    )

    val onFailure: AuthedService[String] = Kleisli(req => Forbidden(req.authInfo))

    val middleware = AuthMiddleware(authUser, onFailure)

    val authedService: AuthedService[UserData] =
      AuthedService {
        case GET -> Root / "welcome" as user => Ok(s"Welcome, ${user.name}")

        case req @ GET -> Root / "payload" as userData =>
          RespondWith( s => theResponse(s) ) using (userData, req.req)

      }

    val service: HttpService = middleware(authedService)

    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(service, "/api")
      .start
  }

  object RespondWith {
    def apply(fn: (String) => Task[Response]) = {
      new RespondWith(fn)
    }
  }

  class RespondWith(fn: (String) => Task[Response]) {
    def using(userData: UserData) = {
      userData.authData.flatMap(_.fold(Forbidden(_),s => fn(s)))
    }

    def using(userData: UserData, request: Request) = {
      userData.authData.flatMap(_.fold(Forbidden(_),s => fn(s)))
    }
  }
}


