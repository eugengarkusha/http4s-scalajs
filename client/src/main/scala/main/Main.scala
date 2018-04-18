package main

import auth.dto.User
import components._
import japgolly.scalajs.react.{Callback, CallbackTo}
import japgolly.scalajs.react.extra.router.StaticDsl.Route
import japgolly.scalajs.react.extra.router.{
  BaseUrl,
  Redirect,
  Router,
  RouterConfig,
  RouterConfigDsl,
  RouterCtl,
  StaticDsl
}
import org.scalajs.dom

object UserHolder {
  private var user: Option[User] = None

  //This is shit: do compile time separation!
  def getUserUnsafe(): User =
    user.getOrElse(throw new IllegalStateException("Trying to access user but no user is defined"))
  def isAuthenticated: Boolean = user.isDefined
  def setUser(u: User): Unit = user = Some(u)
  def clearUser: Unit = user = None

}
object Main extends {
  import UserHolder._

  private val routerConfig: RouterConfig[Loc] = RouterConfigDsl[Loc].buildConfig { dsl =>
    import dsl._

    ///////////////////////////////Tooling////////////////////////////////////////////////////////////////////////////////////
    def onSignIn(rctrl: RouterCtl[Loc], user: User): Callback = {
      Callback(setUser(user)) >> rctrl.set(HomeLoc)
    }

    def staticApp(route: Route[Unit], loc: AppLoc, user: => User) = {
      staticRoute(route, loc) ~> renderR(c => AppContainer.Props(user, c.narrow[AppLoc], loc).render)
    }

    def staticAuth(route: Route[Unit], loc: AuthLoc) = {
      staticRoute(route, loc) ~> renderR(c => AuthContainer.Props(onSignIn(c, _), c.narrow[HomeLoc.type], loc).render)
    }

    def authrorized(rule: StaticDsl.Rule[Loc]): StaticDsl.Rule[Loc] =
      rule.addCondition(CallbackTo(isAuthenticated))(_ => Some(dsl.redirectToPage(SignInLoc)(Redirect.Replace)))

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////Routes//////////////////////////////////////////////////////////////////////////

    val authorizedRoutes = authrorized {
      staticApp("#test", TestLoc, getUserUnsafe()) |
        staticApp(root, HomeLoc, getUserUnsafe())
    }

    val unAuthorizedRoutes = {
      staticAuth("#sign-in", SignInLoc)
    }

    (authorizedRoutes | unAuthorizedRoutes).notFound(redirectToPage(HomeLoc)(Redirect.Replace))

  }

  def main(args: Array[String]): Unit = {
    Router(BaseUrl.until_#, routerConfig)().renderIntoDOM(dom.document.getElementById("bootstrap"))
  }
}
