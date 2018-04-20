package components

import auth.dto.{SignInData, User}
import http.httpClient.HttpError
import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router.RouterCtl
import monocle.macros._
import japgolly.scalajs.react.MonocleReact._
import japgolly.scalajs.react._

object AuthContainer {

  // Conteiners depend on functions, not DALs.
  case class Props(signIn: SignInData => (HttpError => Callback) => Callback, currentLoc: AuthLoc) {
    @inline def render: VdomElement = Component(this)
  }

  @Lenses
  case class State(signInState: SignInPage.State)

  def showAlert(err: HttpError): Callback = {
    //update auth container alerts state
    Callback(println("FAIL:" + err))
  }
  val Component = ScalaComponent
    .builder[Props]("AuthComponent")
    .initialState(State(SignInPage.emptyState))
    .renderP(($, p) =>
      p.currentLoc match {
        case SignInLoc => SignInPage.Props(p.signIn(_)(showAlert), StateSnapshot.zoomL(State.signInState).of($)).render
    })
    .build
}
