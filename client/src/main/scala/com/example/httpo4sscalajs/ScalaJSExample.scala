package com.example.httpo4sscalajs

import com.example.httpo4sscalajs.shared.SharedMessages
import org.scalajs.dom

object ScalaJSExample extends  {

  def main(args: Array[String]): Unit = {
    dom.document.getElementById("scalajsShoutOut").textContent =  SharedMessages.itWorks
  }
}
