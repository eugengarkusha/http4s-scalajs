package components.auth

import auth.dto.SignInUpData
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import _root_.components.{SignUpLoc, AuthLoc}

object SignInPage {

  case class Props(signIn: SignInUpData => Callback, sh: StateSnapshot[State], rctl: RouterCtl[AuthLoc]) {
    @inline def render: VdomElement = Component(this)
  }

  case class State(email: String, pass: String)
  val emptyState = State("", "")

  final class Backend($ : BackendScope[Props, Unit]) {
    def render(p: Props): VdomElement = {
      val state = p.sh.value
      import p.sh.setState

      <.div(
        <.div("Sign in"),
        <.div("Dummy user login : a, pass: b"),
        <.input.text(^.value := state.email,
                     ^.onChange ==> ((e: ReactEventFromInput) => setState(state.copy(email = e.target.value)))),
        <.input.text(^.value := state.pass,
                     ^.onChange ==> ((e: ReactEventFromInput) => setState(state.copy(pass = e.target.value)))),
        <.button("go", ^.onClick --> p.signIn(SignInUpData(state.email, state.pass)).>>(setState(emptyState))),
        <.button("sign-up", ^.onClick --> p.rctl.set(SignUpLoc).>>(setState(emptyState)))
      )
    }
  }

  val Component = ScalaComponent
    .builder[Props]("SignInPage")
    .renderBackend[Backend]
    .build
}
