package components

import auth.dto.SignInData
import http.httpClient.HttpError
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra._
import cats.syntax.foldable._

object SignInPage {

  case class Props(signIn: SignInData => Callback, sh: StateSnapshot[State]) {
    @inline def render: VdomElement = Component(this)
  }

  case class State(email: String, pass: String)
  val emptyState = State("", "")

  final class Backend($ : BackendScope[Props, Unit]) {
    def render(p: Props): VdomElement = {
      val state = p.sh.value
      import p.sh.setState

      <.div(
        <.input.text(^.onChange ==> ((e: ReactEventFromInput) => setState(state.copy(email = e.target.value)))),
        <.input.text(^.onChange ==> ((e: ReactEventFromInput) => setState(state.copy(pass = e.target.value)))),
        <.button("go", ^.onClick --> p.signIn(SignInData(state.email, state.pass)))
      )
    }
  }

  val Component = ScalaComponent
    .builder[Props]("LogInForm")
    .renderBackend[Backend]
    .build
}
