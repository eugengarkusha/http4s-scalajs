package com.example.httpo4sscalajs

import com.example.httpo4sscalajs.shared.SharedMessages
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object HomePage {

  final case class Props() {
    @inline def render: VdomElement = Component(this)
  }


  final class Backend($: BackendScope[Props, Unit]) {
    def render(p: Props): VdomElement =
      <.div(SharedMessages.itWorks)
  }

  val Component = ScalaComponent.builder[Props]("LogInForm")
    .renderBackend[Backend]
    .build
}