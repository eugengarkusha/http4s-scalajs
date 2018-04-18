package components

import auth.dto.SignInData
import dal.AuthDal
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router.RouterCtl
import utils.CBT._

object SignInPage {

  case class Props(rctl: RouterCtl[Loc]) {
    @inline def render: VdomElement = Component(this)
  }

  case class State(email: String, pass: String)


  final class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State): VdomElement =
      <.div(
        <.input.text(^.onChange ==> ((e: ReactEventFromInput) => $.setState(s.copy(email = e.target.value)))),
        <.input.text(^.onChange ==> ((e: ReactEventFromInput) => $.setState(s.copy(pass = e.target.value)))),
        <.button("go", ^.onClick --> AuthDal.signIn(SignInData(s.email, s.pass)).map { e =>
          e.fold(
            err =>
              Callback(println("FAIL:" + err)),
            user => {
              println("SUCC:" + user)
              p.rctl.set(TestLoc)
            }
          )
        }.callback)
      )
  }

  val Component = ScalaComponent.builder[Props]("LogInForm")
    .initialState(State("", ""))
    .renderBackend[Backend]
    .build
}