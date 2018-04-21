package components

import auth.dto.SignInUpData
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router.RouterCtl

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
        <.input.text(^.onChange ==> ((e: ReactEventFromInput) => setState(state.copy(email = e.target.value)))),
        <.input.text(^.onChange ==> ((e: ReactEventFromInput) => setState(state.copy(pass = e.target.value)))),
        <.button("go", ^.onClick --> p.signUp(SignInUpData(state.email, state.pass))),
        <.button("sign-in", ^.onClick --> p.rctl.set(SignInLoc(None)))
      )
    }
  }

  val Component = ScalaComponent
    .builder[Props]("SignUpPage")
    .renderBackend[Backend]
    .build
}
