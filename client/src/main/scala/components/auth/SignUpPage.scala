package components.auth

import auth.dto.SignInUpData
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import _root_.components.{SignInLoc, AuthLoc}
import japgolly.scalajs.react.Callback.alert

object SignUpPage {

  case class Props(signUp: SignInUpData => Callback, sh: StateSnapshot[State], rctl: RouterCtl[AuthLoc]) {
    @inline def render: VdomElement = Component(this)
  }

  case class State(email: String, pass: String)
  val emptyState = State("", "")

  final class Backend($ : BackendScope[Props, Unit]) {
    def render(p: Props): VdomElement = {
      val state = p.sh.value
      import p.sh.setState

      <.div(
        <.div("Sign up"),
        <.input.text(^.value := state.email,
                     ^.onChange ==> ((e: ReactEventFromInput) => setState(state.copy(email = e.target.value)))),
        <.input.text(^.value := state.pass,
                     ^.onChange ==> ((e: ReactEventFromInput) => setState(state.copy(pass = e.target.value)))),
        <.button("go", ^.onClick --> p.signUp(SignInUpData(state.email, state.pass)).>>(setState(emptyState))),
        <.button("sign-in", ^.onClick --> p.rctl.set(SignInLoc(None)).>>(setState(emptyState)))
      )
    }
  }

  val Component = ScalaComponent
    .builder[Props]("SignUpPage")
    .renderBackend[Backend]
    .build
}
