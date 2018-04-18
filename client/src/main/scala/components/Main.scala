package components

import japgolly.scalajs.react.extra.router.{BaseUrl, Redirect, Router, RouterConfig, RouterConfigDsl}
import org.scalajs.dom

object Main extends  {


  private val routerConfig: RouterConfig[Loc] = RouterConfigDsl[Loc].buildConfig { dsl =>
    import dsl._
    (
      staticRoute("#test", TestLoc) ~> renderR(_ =>TestPage.Props().render) |
      staticRoute("#sign-in", SignInLoc) ~> renderR(c =>SignInPage.Props(c).render) |
        staticRoute(root, HomeLoc) ~> renderR(HomePage.Props(_).render)
      ).notFound(redirectToPage(HomeLoc)(Redirect.Replace))
  }

  def main(args: Array[String]): Unit = {
    Router(BaseUrl.until_#, routerConfig)()
      .renderIntoDOM(dom.document.getElementById("bootstrap"))
  }
}
