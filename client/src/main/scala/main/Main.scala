package main

import auth.dto.{SignInData, User}
import components._
import dal.AuthDal
import http.httpClient.HttpError
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
import misc.SharedVariables._

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
    def removeCookie() = dom.document.cookie = s"$cookieName=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;"
    def signIn(rctrl: RouterCtl[Loc]): SignInData => (HttpError => Callback) => Callback =
      sid => onError => AuthDal.signIn(sid)(_.fold(onError, u => Callback(setUser(u)) >> rctrl.set(HomeLoc)))

    def signOut(rctrl: RouterCtl[Loc]) =
      AuthDal.signOut(r =>
        Callback {
          r.left.map(e => println(s"Trying to sign-out. Server responded with $e. Removing cookie and user data."))
          removeCookie
          clearUser
        } >> rctrl.set(SignInLoc))

    def staticApp(route: Route[Unit], loc: AppLoc, user: => User) = {
      staticRoute(route, loc) ~>
        renderR(rctl => AppContainer.Props(user, rctl.narrow[AppLoc], loc, signOut(rctl)).render)
    }

    def staticAuth(route: Route[Unit], loc: AuthLoc) = {
      staticRoute(route, loc) ~> renderR(c => AuthContainer.Props(signIn(c), loc).render)
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
    Router(BaseUrl.until_#, routerConfig)().renderIntoDOM(dom.document.getElementById(bootstrapId))
  }
}
