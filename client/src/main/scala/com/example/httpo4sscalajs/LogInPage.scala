package com.example.httpo4sscalajs

import com.example.httpo4sscalajs.shared.SharedMessages
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra._

object LogInPage {

  final case class Props() {
    @inline def render: VdomElement = Component(this)
  }

  final case class State(login: String, pass: String)


  final class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State): VdomElement =
      <.div(
        <.input.text(^.onChange ==> ((e: ReactEventFromInput) =>  $.setState(s.copy(login = e.target.value)))),
        <.input.text(^.onChange ==> ((e: ReactEventFromInput) =>  $.setState(s.copy(pass = e.target.value)))),
        <.button(^.onClick --> Callback.empty )
      )
  }

  val Component = ScalaComponent.builder[Props]("LogInForm")
    .renderBackend[Backend]
    .build
}