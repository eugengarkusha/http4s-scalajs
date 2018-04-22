package components.app

import auth.dto.UserInfo
import dal.TestDal
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.vdom.html_<^._

object TestPage {

  case class Props(user: UserInfo, sh: StateSnapshot[State]) {
    @inline def render: VdomElement = Component(this)
  }

  case class State(response: String)

  val emptyState = State("")

  final class Backend($ : BackendScope[Props, State]) {
    def render(p: Props, s: State): VdomElement =
      <.div(
        <.div(s"Welcome, ${p.user.displayName} !"),
        <.div(s"expect to return Unauthorized when authenticator expires"),
        <.button("click me",
                 ^.onClick -->
                   //usig dal directly(its a test page, dont care)
                   TestDal.test(v => $.modState(_.copy(response = v.fold(_.toString, identity))))),
        <.span(s"server response:  ${s.response}")
      )
  }

  val Component = ScalaComponent
    .builder[Props]("TestPage")
    .initialState(State(""))
    .renderBackend[Backend]
    .build
}
