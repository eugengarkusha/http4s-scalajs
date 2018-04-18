package components

import auth.dto.{SignInData, User}
import dal.AuthDal
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router.RouterCtl
import utils.CBT._

object SignInPage {

  case class Props(onSignIn: User => Callback, rctl: RouterCtl[HomeLoc.type], sh: StateSnapshot[State]) {
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
        <.button(
          "go",
          ^.onClick --> AuthDal
            .signIn(SignInData(state.email, state.pass))
            .map { e =>
              e.fold(
                err => Callback(println("FAIL:" + err)),
                user => p.onSignIn(user) >> p.rctl.set(HomeLoc)
              )
            }
            .callback
        )
      )
    }
  }

  val Component = ScalaComponent
    .builder[Props]("LogInForm")
    .renderBackend[Backend]
    .build
}
