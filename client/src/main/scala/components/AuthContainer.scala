package components

import auth.dto.User
import japgolly.scalajs.react.Callback
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router.RouterCtl
import monocle.macros._
import japgolly.scalajs.react.MonocleReact._
import japgolly.scalajs.react._

object AuthContainer {

  case class Props(onSignIn: User => Callback, rctl: RouterCtl[HomeLoc.type], currentLoc: AuthLoc) {
    @inline def render: VdomElement = Component(this)
  }

  @Lenses
  case class State(signInState: SignInPage.State)

  val Component = ScalaComponent
    .builder[Props]("AuthComponent")
    .initialState(State(SignInPage.emptyState))
    .renderP(($, p) =>
      p.currentLoc match {
        case SignInLoc => SignInPage.Props(p.onSignIn, p.rctl, StateSnapshot.zoomL(State.signInState).of($)).render
    })
    .build
}
