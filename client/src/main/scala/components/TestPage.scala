package components

import dal.TestDal
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra._
import utils.CBT._

object TestPage {

  case class Props() {
    @inline def render: VdomElement = Component(this)
  }

  case class State(response: String)

  final class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State): VdomElement =
      <.div(
        <.button(^.onClick --> TestDal.test.map(v => $.modState(_.copy(response = v.fold(_.toString, _.toString)))).callback),
        <.span("response:"+ s.response)
      )
  }

  val Component = ScalaComponent.builder[Props]("TestPage")
    .initialState(State(""))
    .renderBackend[Backend]
    .build
}