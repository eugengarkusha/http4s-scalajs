package com.example.httpo4sscalajs

import japgolly.scalajs.react.extra.router.{BaseUrl, Redirect, Router, RouterConfig, RouterConfigDsl}
import org.scalajs.dom

object Main extends  {

  sealed trait Loc
  case object LoginLoc extends Loc
  case object HomeLoc extends Loc
  private val routerConfig: RouterConfig[Loc] = RouterConfigDsl[Loc].buildConfig { dsl =>
    import dsl._
    (
      staticRoute("#login", LoginLoc) ~> renderR(ctrl => SignInPage.Props().render) |
        staticRoute(root, HomeLoc) ~> renderR(ctrl =>  HomePage.Props().render)
      ).notFound(redirectToPage(HomeLoc)(Redirect.Replace))
  }

  def main(args: Array[String]): Unit = {
    Router(BaseUrl.until_#, routerConfig)()
      .renderIntoDOM(dom.document.getElementById("scalajsShoutOut"))
  }
}
