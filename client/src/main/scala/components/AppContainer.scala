package components

import auth.dto.User
import japgolly.scalajs.react.{CtorType, _}
import japgolly.scalajs.react.component.Scala.Component
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router.RouterCtl
import monocle.macros._
import japgolly.scalajs.react.MonocleReact._

object AppContainer {

  @Lenses
  case class State(testState: TestPage.State)

  case class Props(user: User, rctl: RouterCtl[AppLoc], currentLoc: AppLoc, signOut: Callback) {
    @inline def render: VdomElement = Component(this)
  }

  val Component = ScalaComponent
    .builder[Props]("RootComponent")
    .initialState(State(TestPage.emptyState))
    .renderP { ($, p) =>
      val app = p.currentLoc match {
        case TestLoc => TestPage.Props(p.user, StateSnapshot.zoomL(State.testState).of($)).render
        case HomeLoc => HomePage.Props(p.rctl).render
      }
      <.div(
        <.button("sign-out", ^.onClick --> p.signOut),
        app
      )
    }
    .build
}
