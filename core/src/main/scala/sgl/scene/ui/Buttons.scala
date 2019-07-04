package sgl
package scene
package ui

trait ButtonsComponent {
  this: GraphicsProvider with SceneComponent =>

  import Graphics._

  class Button(_x: Int, _y: Int, _width: Int, _height: Int)
    extends SceneNode(_x, _y, _width, _height) {

    private var pressed: Boolean = false

    override def update(dt: Long): Unit = {}

    def renderRegular(canvas: Canvas): Unit
    def renderPressed(canvas: Canvas): Unit

    override def render(canvas: Canvas): Unit = {
      if(pressed)
        renderPressed(canvas)
      else
        renderRegular(canvas)
    }

    override def notifyDown(x: Int, y: Int): Boolean = {
      pressed = true
      true
    }
    override def notifyPointerLeave(): Unit = {
      pressed = false
    }
    override def notifyUp(x: Int, y: Int): Boolean = {
      pressed = false
      true
    }

  }

  class BitmapButton(_x: Int, _y: Int, regularBitmap: BitmapRegion, pressedBitmap: BitmapRegion)
    extends Button(_x, _y, regularBitmap.width, regularBitmap.height) {

    override def renderPressed(canvas: Canvas): Unit =
      canvas.drawBitmap(pressedBitmap, x.toInt, y.toInt)

    override def renderRegular(canvas: Canvas): Unit =
      canvas.drawBitmap(regularBitmap, x.toInt, y.toInt)
  }

  // Seems like we could use some general theme object, which provide the
  // look and feel of the interface. It would contain border color, border
  // width, corner (round vs square), fill color, margins, etc. But for now,
  // since we are just getting started, a simple button theme might be enough
  // for our needs.
  case class ButtonTheme(
    borderColor: Color,
    fillColor: Color,
    textColor: Color,
    textFont: Font
  ) {

    val borderPaint = defaultPaint.withColor(borderColor)
    val fillPaint = defaultPaint.withColor(fillColor)
    val textPaint = defaultPaint.withColor(textColor)

    def drawBox(canvas: Canvas, x: Int, y: Int, width: Int, height: Int): Unit = {
      canvas.drawRect(x, y, width, height, fillPaint)
      canvas.drawLine(x, y, x+width, y, borderPaint)
      canvas.drawLine(x+width, y, x+width, y.toInt+height, borderPaint)
      canvas.drawLine(x+width, y+height, x, y+height, borderPaint)
      canvas.drawLine(x, y+height, x, y, borderPaint)
    }

  }

  class TextButton(_x: Int, _y: Int, _width: Int, _height: Int, label: String, regularTheme: ButtonTheme, pressedTheme: ButtonTheme)
    extends Button(_x, _y, _width, _height) {

    val regularTextPaint = defaultPaint.withColor(regularTheme.textColor).withFont(regularTheme.textFont).withAlignment(Alignments.Center)
    val pressedTextPaint = defaultPaint.withColor(pressedTheme.textColor).withFont(pressedTheme.textFont).withAlignment(Alignments.Center)

    override def renderPressed(canvas: Canvas): Unit = {
      pressedTheme.drawBox(x.toInt, y.toInt, _width, _height)
      canvas.drawString(label, x.toInt + width.toInt/2, y.toInt + (height-pressedTheme.textFont.size)/2, pressedTextPaint)
    }

    override def renderRegular(canvas: Canvas): Unit = {
      regularTheme.drawBox(x.toInt, y.toInt, _width, _height)
      canvas.drawString(label, x.toInt + width.toInt/2, y.toInt + (height-regularTheme.textFont.size)/2, regularTextPaint)
    }

  }

}

