package components.auth

import auth.dto.SignInUpData
import cats.instances.option._
import cats.syntax.foldable._
import http.httpClient.{HttpError, OnComplete}
import japgolly.scalajs.react.MonocleReact._
import japgolly.scalajs.react.{Callback, _}
import japgolly.scalajs.react.component.builder.Lifecycle.StateRW
import japgolly.scalajs.react.extra._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import misc.SharedAliases.Email
import _root_.components._
import monocle.macros._
import utils.Instances.cbMonoid

object AuthContainer {

  type OnError = (HttpError => Callback) => Callback

  // Conteiners depend on functions, not DALs.
  case class Props(signIn: SignInUpData => OnError,
                   signUp: SignInUpData => OnError,
                   currentLoc: AuthLoc,
                   activation: Option[OnComplete[Email]],
                   rctl: RouterCtl[AuthLoc]) {
    @inline def render: VdomElement = Component(this)
  }

  @Lenses
  case class State(signInState: SignInPage.State, signUpState: SignUpPage.State)

  def addAllert(err: HttpError): Callback = {
    //update auth container alerts state
    Callback(println("FAIL:" + err))
  }
  // Calling activation api from within the app to be able to provide different notifications to the user depending in status
  // (alternative without notifications : activation url points to  server side api that does activation and redirects to sign in)
  //TODO: rewrite with backend and remove $ param
  def processActivation(runActivation: OnComplete[Email], $ : StateRW[Props, State, Unit]): Callback =
    runActivation(
      _.fold[Callback](
        err => addAllert(err),
        email => $.modStateL(State.signInState)(_.copy(email = email))
      )
    )

  val Component = ScalaComponent
    .builder[Props]("AuthComponent")
    .initialState(State(SignInPage.emptyState, SignUpPage.emptyState))
    .renderP(($, p) =>
      p.currentLoc match {
        case _: SignInLoc =>
          SignInPage.Props(p.signIn(_)(addAllert), StateSnapshot.zoomL(State.signInState).of($), p.rctl).render
        case SignUpLoc =>
          SignUpPage.Props(p.signUp(_)(addAllert), StateSnapshot.zoomL(State.signUpState).of($), p.rctl).render
    })
    .componentWillMount($ => $.props.activation.foldMap(processActivation(_, $)))
    .componentWillReceiveProps($ => $.nextProps.activation.foldMap(processActivation(_, $)))
    .build
}
