package sgl
package scene
package ui

trait PopupsComponent {
  this: GraphicsProvider with WindowProvider with SceneComponent =>

  import Graphics._

  /** A Popup is a SceneNode that cover the entire area.
    *
    * The popup covers the width/height area and intercept all click events. It contains
    * an inner node which is the actual content of the popup. The coordinates of the inner
    * node are relative to the popup coordinates and not centered, which gives the option
    * to the inner node to implement a transition to show up on screen.
    *
    * The popup becomes visible as soon as it is added to the scene graph, the way to activate
    * it is to add it to the scene whenever we need the popup to show.
    */
  class Popup(_width: Int, _height: Int, inner: SceneNode) extends SceneNode(0, 0, _width, _height) {

    val backgroundColor = Color.Transparent

    // A popup always intecept clicks, so either the inner node processes the
    // click or otherwise the popup intercept the click (and does nothing).
    override def hit(x: Int, y: Int): Option[SceneNode] = {
      inner.hit(x, y).orElse(Some(this))
    }

    override def update(dt: Long): Unit = {
      inner.update(dt)
    }

    override def render(canvas: Canvas): Unit = {
      canvas.drawColor(backgroundColor)
      inner.render(canvas)
    }

  }

  class Dialog(_width: Int, label: String, options: List[(String, () => Unit)], fontSize: Int, fontColor: Color) extends SceneNode(0, 0, _width, 0) {

    val leftMargin = Window.dp2px(32)
    val topMargin = Window.dp2px(32)
    val rightMargin = Window.dp2px(32)
    val bottomMargin = Window.dp2px(32)

    val labelOptionsSpace = Window.dp2px(64)

    val fillColor = Color.rgba(0,0,0,200)
    val outlineColor = Color.White

    private var outlinePaint: Paint = _
    private var fillPaint: Paint = _

    private val paint = defaultPaint.withFont(Font.Default.withSize(fontSize)).withColor(fontColor)

    override def update(dt: Long): Unit = {}

    override def render(canvas: Canvas): Unit = {
      val labelText = canvas.renderText(label, _width - leftMargin - rightMargin, paint)
      val totalHeight = topMargin + labelText.height + labelOptionsSpace + fontSize + bottomMargin
      this.height = totalHeight

      if(fillPaint == null)
        fillPaint = defaultPaint.withColor(fillColor)
      if(outlinePaint == null)
        outlinePaint = defaultPaint.withColor(outlineColor)

      canvas.drawRect(x.toInt, y.toInt, _width, totalHeight, fillPaint)
      canvas.drawLine(x.toInt, y.toInt, x.toInt+_width, y.toInt, outlinePaint)
      canvas.drawLine(x.toInt+_width, y.toInt, x.toInt+_width, y.toInt+totalHeight, outlinePaint)
      canvas.drawLine(x.toInt+_width, y.toInt+totalHeight, x.toInt, y.toInt+totalHeight, outlinePaint)
      canvas.drawLine(x.toInt, y.toInt+totalHeight, x.toInt, y.toInt, outlinePaint)

      canvas.drawText(labelText, x.toInt + leftMargin, y.toInt + topMargin + fontSize)
      canvas.drawString(options(0)._1, x.toInt + leftMargin, totalHeight - bottomMargin, paint)
    }

  }

  class DialogPopup(_width: Int, _height: Int, dialog: Dialog) extends Popup(_width, _height, dialog)

}
