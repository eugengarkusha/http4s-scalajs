package main

import auth.dto.{SignInUpData, UserInfo}
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
  private var user: Option[UserInfo] = None

  //This is shit: do compile time separation!
  def getUserUnsafe(): UserInfo =
    user.getOrElse(throw new IllegalStateException("Trying to access user but no user is defined"))
  def isAuthenticated: Boolean = user.isDefined
  def setUser(u: UserInfo): Unit = user = Some(u)
  def clearUser: Unit = user = None

}
object Main extends {
  import UserHolder._

  private val routerConfig: RouterConfig[Loc] = RouterConfigDsl[Loc].buildConfig { dsl =>
    import dsl._

    ///////////////////////////////Tooling////////////////////////////////////////////////////////////////////////////////////
    def removeCookie() = dom.document.cookie = s"$cookieName=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;"
    def signIn(rctrl: RouterCtl[Loc]): SignInUpData => (HttpError => Callback) => Callback =
      sid => onError => AuthDal.signIn(sid)(_.fold(onError, u => Callback(setUser(u)) >> rctrl.set(HomeLoc)))

    def signUp(rctrl: RouterCtl[Loc]): SignInUpData => (HttpError => Callback) => Callback =
      sd => onError => AuthDal.signUp(sd)(_.fold(onError, _ => Callback.empty) >> rctrl.set(SignInLoc()))

    def signOut(rctrl: RouterCtl[Loc]): Callback =
      AuthDal.signOut(
        r =>
          Callback(
            r.left.foreach(e =>
              println(s"Trying to sign-out. Server responded with $e. Removing cookie and user data."))
          ) >>
            Callback(clearUser) >>
            rctrl.set(SignInLoc()) >>
            Callback(removeCookie))

    def staticApp(route: Route[Unit], loc: AppLoc, user: => UserInfo) =
      staticRoute(route, loc) ~>
        renderR(rctl => AppContainer.Props(user, rctl.narrow[AppLoc], loc, signOut(rctl)).render)

    def staticAuth(route: Route[Unit], loc: AuthLoc) =
      staticRoute(route, loc) ~>
        renderR(rctl => AuthContainer.Props(signIn(rctl), signUp(rctl), loc, None, rctl.narrow[AuthLoc]).render)

    def authrorized(rule: StaticDsl.Rule[Loc]): StaticDsl.Rule[Loc] =
      rule.addCondition(CallbackTo(isAuthenticated))(_ => Some(dsl.redirectToPage(SignInLoc())(Redirect.Replace)))

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////Routes//////////////////////////////////////////////////////////////////////////

    val authorizedRoutes = authrorized(
      staticApp("#test", TestLoc, getUserUnsafe()) |
        staticApp(root, HomeLoc, getUserUnsafe())
    )

    val unAuthorizedRoutes = {
      val signInAndActivation = dynamicRouteCT[SignInLoc]("#sign-in" / uuid.option.caseClass[SignInLoc]) ~> dynRenderR(
        (loc, rctl) =>
          AuthContainer
            .Props(signIn(rctl), signUp(rctl), loc, loc.activationId.map(AuthDal.activate(_)), rctl.narrow[AuthLoc])
            .render)

      signInAndActivation | staticAuth("#sign-up", SignUpLoc)
    }

    (authorizedRoutes | unAuthorizedRoutes).notFound(redirectToPage(HomeLoc)(Redirect.Replace))

  }

  def main(args: Array[String]): Unit = {
    Router(BaseUrl.until_#, routerConfig)().renderIntoDOM(dom.document.getElementById(bootstrapId))
  }
}
