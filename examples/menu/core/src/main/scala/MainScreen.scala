package com.regblanc.sgl.menu
package core

import sgl._
import geometry._
import scene._
import scene.ui._
import util._

trait ScreensComponent {
  this: GraphicsProvider with InputProvider with SystemProvider with WindowProvider 
  with GameStateComponent with GameLoopStatisticsComponent with LoggingProvider
  with GraphicsHelpersComponent with ViewportComponent with SceneComponent with PopupsComponent =>

  private implicit val LogTag = Logger.Tag("main-screen")

  class LevelsScreen extends GameScreen {

    override def name: String = "LevelsScreen"

    val viewport = new Viewport(Window.width, Window.height)

    val scene = new SceneGraph(Window.width, Window.height, viewport)

    val levelsPane = new ScrollPane(0, 0, Window.width, Window.height, Window.width, 3*Window.height)
    scene.addNode(levelsPane)

    class Button(i: Int, _x: Int, _y: Int) extends SceneNode(_x, _y, 100, 30) {
      private var pressed = false 
      override def notifyDown(x: Int, y: Int): Boolean = {
        println(s"button $i down at ($x, $y)")
        pressed = true
        true
      }

      override def notifyPointerLeave(): Unit = {
        pressed = false
      }

      override def notifyUp(x: Int, y: Int): Boolean = {
        println(s"button $i up at ($x, $y)")
        pressed = false
        true
      }

      override def notifyClick(x: Int, y: Int): Boolean = {
        println(s"button $i clicked at ($x, $y)")
        true
      }

      override def update(dt: Long): Unit = {}
      override def render(canvas: Graphics.Canvas): Unit = {
        val color = Graphics.defaultPaint.withColor(if(pressed) Graphics.Color.Red else Graphics.Color.Green)
        canvas.drawRect(x.toInt, y.toInt, width.toInt, height.toInt, color)
      }
    }
    for(i <- 1 to 100) {
      val button = new Button(i, 20, i*50)
      levelsPane.addNode(button)
    }

    def processInputs(): Unit = {
      Input.pollEvent() match {
        case Some(event) =>
          event match {
            case Input.KeyDownEvent(Input.Keys.P) =>
              println("clicked p")
              scene.addNode(
                new DialogPopup(
                  Window.width, Window.height,
                  new Dialog(Window.dp2px(400),
                             "Hey there, do you like the weather ok?",
                             List(("Yes", () => { println("yes") }),
                                  ("Nope", () => { println("nope") }),
                                  ("Meh", () => { println("meh") })),
                             Window.dp2px(36), Graphics.Color.White)
                ) {
                  override val backgroundColor = Graphics.Color.rgba(0,0,0,150)
                }
              )
            case _ => scene.processInput(event)
          }
          processInputs()
        case None => ()
      }
    }

    override def update(dt: Long): Unit = {
      processInputs()
      scene.update(dt)
    }

    override def render(canvas: Graphics.Canvas): Unit = {
      scene.render(canvas)
    }

  }

}
