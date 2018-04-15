package com.example.httpo4sscalajs

import auth.dto.SignInData
import com.example.httpo4sscalajs.shared.SharedMessages
import dal.AuthDal
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra._
import utils.CBT._

object SignInPage {

  final case class Props() {
    @inline def render: VdomElement = Component(this)
  }

  final case class State(email: String, pass: String)


  final class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State): VdomElement =
      <.div(
        <.input.text(^.onChange ==> ((e: ReactEventFromInput) =>  $.setState(s.copy(email = e.target.value)))),
        <.input.text(^.onChange ==> ((e: ReactEventFromInput) =>  $.setState(s.copy(pass = e.target.value)))),
        <.button(^.onClick --> AuthDal.signIn(SignInData(s.email, s.pass)).map{e =>
          e.fold(
            err => println("FAIL:"+err),
            user => println("SUCC:"+user)
          )
        }.callback)
      )
  }

  val Component = ScalaComponent.builder[Props]("LogInForm")
    .renderBackend[Backend]
    .build
}