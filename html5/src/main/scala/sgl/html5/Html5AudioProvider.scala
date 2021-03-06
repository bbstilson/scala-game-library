package sgl
package html5

import sgl.util.Loader

trait Html5AudioProvider extends AudioProvider {
  this: Html5SystemProvider =>

  object Html5Audio extends Audio {

    /** Not supported. */
    class Sound extends AbstractSound {

      type PlayedSound = Int

      override def play(volume: Float): Option[PlayedSound] = None
      override def withConfig(loop: Int, rate: Float): Sound = this
      override def dispose(): Unit = {}

      override def stop(id: PlayedSound): Unit = {}
      override def pause(id: PlayedSound): Unit = {}
      override def resume(id: PlayedSound): Unit = {}
      override def endLoop(id: PlayedSound): Unit = {}
    }

    /** Not supported. */
    override def loadSound(path: ResourcePath): Loader[Sound] = Loader.successful(new Sound)

    /** Not supported. */
    class Music extends AbstractMusic {
      override def play(): Unit = {}
      override def pause(): Unit = {}
      override def stop(): Unit = {}
      override def setVolume(volume: Float): Unit = {}
      override def setLooping(isLooping: Boolean): Unit = {}
      override def dispose(): Unit = {}
    }

    /** Not supported. */
    override def loadMusic(path: ResourcePath): Loader[Music] = Loader.successful(new Music)
  }
  override val Audio = Html5Audio
}
