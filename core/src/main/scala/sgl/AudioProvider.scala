package sgl

import sgl.util._

/** Provides platform-specific Audio module.
  *
  * The Audio module defines the Sound and Music datatypes.  While both have
  * relatively similar interfaces, their usage intent, and implementation,
  * differ.  Sound are for short effect (click effect, level up, etc), while
  * Music is for long-running audio, such as background music.  More generally,
  * a Sound should generally be a small file and will be loaded in RAM
  * entirely, while Music can be much longer, will be streamed from the file,
  * and not entirely loaded in RAM. It also means that loading music can be
  * somewhat more expensive and thus it should not be used for sound effects.
  *
  * This provider is not mandatory. If your game does not require audio, you do
  * not need to mix-in an implementation for this provider. That being said, I
  * do not recommand a game without sound.
  */
trait AudioProvider {
  this: SystemProvider =>

  trait Audio {

    // TODO: It seems both AWT and Android has some sort of implementation
    // based on a pool of sounds with a maximum number in flights. Based on
    // that, it seems like we should make the max number of PlayedSound an
    // explicit parameter of the interface, to avoid surprise for the
    // clients.
    // Maybe:
    //   val MaxPlayingSounds: Int = 10 ?
    // With the option of overriding in the game. We should document the
    // drawbacks of increasing and what are reasonable values.
 
    /** Represents a short sound loaded and ready to play.
      *
      * A Sound class should be able to load a short sound
      * data, and generate a fresh, independent PlayedSound for
      * each call to play. The same sound can be played and
      * overlapped several times.
      *
      * PlayedSound resources are auto-freeing, typically they
      * are going to be cleaned up either on stop, or when fully
      * played.
      *
      * The Sound itself must be disposed when the game no longer
      * plans to play sounds from it. Typically, a game would load
      * a bunch of sounds specific to a level when loading the level,
      * then play them at appropriate time, and call dispose on each
      * of them when leaving the level.
      */
    abstract class AbstractSound {
  
      type PlayedSound
  
      /** Start playing an instance of the Sound.
        *
        * Return a PlayedSound object, which can be used to do further
        * manipulation on the sound currently being played. The call
        * can potentially fail, so it returns an Option.
        *
        * A Sound can be started many times, it will be overlaid. Each
        * time a different PlayedSound will be returned.
        *
        * @param volume volume value, from 0.0 to 1.0, act as a multiplier on
        *   the system current volume.
        */
      def play(volume: Float): Option[PlayedSound]
      def play(): Option[PlayedSound] = play(1f)

      /** Returns a cloned version of the Sound but in a different play configuration.
        *
        * This method gives us a way to combine a primitive Sound into a
        * looped effect. The idea is that some sound effect can be accomplished
        * by looping a basic sound N times, and it is more efficient to just
        * store one iteration of the loop (both in resources and then loaded in
        * memory) but then to use some looped behaviour logic when playing.
        *
        * Using a different rate can let you reuse the same sound resource in several
        * context.
        *
        * After calling withConfig, you end up with two independent Sound
        * instance in your system, and you must dispose both, or you can
        * dispose one of them and keep using the other one. Typically, you
        * might want to load the sound sample, call withConfig to get the play
        * configuration, and then dispose the original as you are only planning
        * to use the custom version from now on. Although it looks like we end
        * up with using double the resources with both Sound object, it's
        * likely that the underlying backend actually optimizes memory to only
        * load the primitve sound once, and just keep track of the settings and
        * disposed state.
        *
        * @param loop the number of times the sound should be looped (-1 for
        *   infinity, 0 for regular play, any other positive numbers for number
        *   of repeats)
        * @param rate from 0.5 to 2, the play speed (from half to twice as fast
        *   as the original).
        */
      def withConfig(loop: Int, rate: Float): Sound
  
      /** Returns a cloned version of the sound but in a looped state.
        *
        * Of course the effect could be accomplished by just wrapping some logic
        * around the Sound class (by playing 3 times in a row). But first that
        * would require to detect when the sound effect is completed, and then
        * it's also additional logic that needs to run, while most backends
        * actually support natively the concept of looping a sound, so it
        * probably makes sense to offer this feature in the Sound API directly.
        *
        * Note that the API only allows to clone a Sound to get a looped
        * version instead of actually setting the looped state. It is a
        * deliberate design, as usually immutability is better, and in the case
        * of sound effects, it's very likely that you want to build the proper
        * sound configuration from the sound file and then just play it as is,
        * instead of changing the state in each frame. This also give you the
        * ability to use the same sound file, load it only once, and combine it
        * in several different sound instances (you couldn't do that if you had
        * to set the state of the sound, unless you are willing to set the
        * state before every call to play).
        */
      def looped(n: Int): Sound = withConfig(n, 1f)
      def looped: Sound = looped(-1)

      // An interesting extension to the looped interface is how to combine several
      // sounds together. This has the same challenge to detect when the sound is over,
      // and the same argument that it is more efficient to store just a few independent
      // primitive sounds and combine them together. However, backends might not always
      // support such an API, so we won't expose it for now. It also seems like a less
      // common case than looping a sound.
  
      /** Free up resources associated with this Sound.
        *
        * A Sound is kept in memory until dispose() is called. A typical implementation in
        * the backend will be to load the Sound into some system data structure, which will
        * hold it in memory and ready to use. If a Sound is no longer necessary (for example
        * after completing a level), it should be disposed for recycling resources.
        */
      def dispose(): Unit
  
      /** Pause the PlayedSound instance.
        *
        * Can then resume it. Has no effect if already paused or stoped. If the
        * sound is stopped or finished, calling pause will not revive it.
        */
      def pause(id: PlayedSound): Unit

      /** Resume a paused PlayedSound instance. */
      def resume(id: PlayedSound): Unit
  
      /** Stop the PlayedSound instance.
        *
        * Once stopped, it can no longer be resumed.
        * The memory might be reused for future sounds,
        * so it is unsafe to keep using it.
        */
      def stop(id: PlayedSound): Unit

      /** Set the looping state of the specific PlayedSound instance within that sound.
        *
        * This is a way to set the looping state of a sound once it started to play.
        * It will not affect the Sound state itself. There are not many use cases
        * for such a method, but one is to smoothly terminate a looped sound
        * by completing the current loop instead of abruptly stopping in the middle
        * of the loop sequence (Say you use the Sound as a five seconds loop effect,
        * made of 5 times a 1 second effect, and a game event forces you to stop the
        * sound effect, you may want to call setLooping(id, false) instead of pause(id),
        * in order to have a smooth transition).
        *
        * It is not clear what is a reasonable use case for setLooping(id, true), but
        * this seems like a relatively cheap general interface to provide, and maybe
        * there are decent use cases.
        */
      def setLooping(id: PlayedSound, isLooping: Boolean): Unit
  
    }
    type Sound <: AbstractSound

    def loadSound(path: ResourcePath): Loader[Sound]
  
    /*
     * Music has a similar interface to sound, but is meant to load
     * long music, typically used as background music.
     * It is not necessarly entirely loaded in memory, for some device
     * with low RAM it might be streamed directly from disk.
     */
    abstract class AbstractMusic {
      def play(): Unit
      def pause(): Unit
      def stop(): Unit
  
      /** Set the volume of the music
        *
        * The volume is between 0 and 1, with
        * 0 meaning the lowest available value, and 1 the
        * highest.
        *
        * The progression from 0 to 1 should be linear. It seems
        * like some system use some sort of logarithmic scale, but
        * I don't really know why, so this function will take the
        * simpler approach to have a linear scale from 0 to 1, with
        * 0.5 being 50% of max volume.
        */
      def setVolume(volume: Float): Unit
  
      def setLooping(isLooping: Boolean): Unit
  
      def dispose(): Unit
    }
    type Music <: AbstractMusic

    def loadMusic(path: ResourcePath): Loader[Music]

  }
  val Audio: Audio

}
