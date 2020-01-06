package sgl
package android
package ads

import sgl.ads._

import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd

import com.google.android.gms.ads.initialization.{InitializationStatus, OnInitializationCompleteListener}

import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAdCallback

import _root_.android.app.Activity
import _root_.android.os.Bundle

trait AndroidAdMobProvider extends Activity with AdsProvider {
  self =>

  // TODO: We probably want to support more than one units of each type. This will
  // require refactoring the API.

  val InterstitialAdUnitId: Option[String] = None

  /** The special name for a test InterstitialAdUnitId.
    *
    * This value will be used if the InterstitialAdUnitId is not set.
    */
  val TestInterstitialAdUnitId = "ca-app-pub-3940256099942544/1033173712"

  val RewardedAdUnitId: Option[String] = None
  val TestRewardedAdUnitId = "ca-app-pub-3940256099942544/5224354917"

  private var interstitialAd: InterstitialAd = null
  private var _isInterstitialLoading = false
  private var _isInterstitialLoaded = false

  private var rewardedAd: RewardedAd = null
  private var _isRewardedLoading = false
  private var _isRewardedLoaded = false

  override def onCreate(bundle: Bundle): Unit = {
    super.onCreate(bundle)

    MobileAds.initialize(this, new OnInitializationCompleteListener() {
      override def onInitializationComplete(status: InitializationStatus): Unit = {
        interstitialAd = new InterstitialAd(self)
        interstitialAd.setAdUnitId(InterstitialAdUnitId.getOrElse(TestInterstitialAdUnitId))
        interstitialAd.setAdListener(new AdListener {
          override def onAdLoaded(): Unit = {
            _isInterstitialLoaded = true
            _isInterstitialLoading = false
          }
  
          override def onAdFailedToLoad(errorCode: Int): Unit = {
            // Code to be executed when an ad request fails.
          }
  
          override def onAdOpened(): Unit = {
            // Code to be executed when the ad is displayed.
          }
  
          override def onAdClicked(): Unit = {
            // Code to be executed when the user clicks on an ad.
          }
  
          override def onAdLeftApplication(): Unit = {
            // Code to be executed when the user has left the app.
          }
  
          override def onAdClosed(): Unit = {
            if(AlwaysPreload) {
              _isInterstitialLoading = true
              interstitialAd.loadAd(new AdRequest.Builder().build())
            }
          }
        })
  
        if(AlwaysPreload) {
          _isInterstitialLoading = true
          interstitialAd.loadAd(new AdRequest.Builder().build())
  
          rewardedAd = createRewardedAd()
          _isRewardedLoading = true
          loadRewardedAd()
        }
      }
    })
  }

  private def createRewardedAd(): RewardedAd = new RewardedAd(this, RewardedAdUnitId.getOrElse(TestRewardedAdUnitId))
  private def loadRewardedAd(): Unit = {
    val callback = new RewardedAdLoadCallback() {
      override def onRewardedAdLoaded(): Unit = {
        _isRewardedLoaded = true
        _isRewardedLoading = false
      }
      override def onRewardedAdFailedToLoad(errorCode: Int): Unit =  {
        _isRewardedLoading = false
      }
    }
    rewardedAd.loadAd(new AdRequest.Builder().build(), callback)
  }

  object AndroidAdMobAds extends Ads {

    override def loadInterstitial(): Unit = {
      // TODO: If the call happen before the initialization callback, we are
      // ignoring it.  we should instead schedule it to start loading after the
      // end of initialization.
      if(interstitialAd != null && !_isInterstitialLoading && !_isInterstitialLoaded) {
        _isInterstitialLoading = true
        runOnUiThread(new Runnable {
          override def run(): Unit = {
            interstitialAd.loadAd(new AdRequest.Builder().build())
          }
        })
      }
    }
    override def isInterstitialLoaded: Boolean = _isInterstitialLoaded
    override def showInterstitial(): Boolean = {
      if(_isInterstitialLoaded) {
        _isInterstitialLoaded = false
        runOnUiThread(new Runnable {
          override def run(): Unit = {
            // This call is asynchronous, not that it matters a lot because we are
            // also running it in a separate thread, but just for reference, the
            // show will return right away and the ad could be shown later.
            interstitialAd.show()
          }
        })
        true
      } else {
        false
      }
    }

    override def loadRewarded(): Unit = {
      // TODO: same initialization problem as loadInterstitial.
      if(rewardedAd != null && !_isRewardedLoading && !_isRewardedLoaded) {
        _isRewardedLoading = true
        runOnUiThread(new Runnable {
          override def run(): Unit = {
            loadRewardedAd()
          }
        })
      }
    }
    override def isRewardedLoaded: Boolean = _isRewardedLoaded
    override def showRewarded(onClosed: (Boolean) => Unit): Boolean = {
      if(_isRewardedLoaded) {
        _isRewardedLoaded = false
        runOnUiThread(new Runnable {
          override def run(): Unit = {
            if(rewardedAd.isLoaded()) {
              var earned = false
              val callback = new RewardedAdCallback() {
                override def onRewardedAdOpened(): Unit = {}
                override def onRewardedAdClosed(): Unit = {
                  if(AlwaysPreload) {
                    rewardedAd = createRewardedAd()
                    _isRewardedLoading = true
                    loadRewardedAd()
                  }
                  // TODO: the onClosed callback is done on the UI thread,
                  // maybe we want to document this behavior? Maybe we want to
                  // use a different thread for this callback?
                  onClosed(earned)
                }
                // Not clear from the official documentation, but I am going to assume
                // that the onUserEarnedReward is always called (if it is called) before the 
                // onRewardedAdClosed, because otherwise there's no way to know
                // how long after an onClose callback we should wait to see if
                // the reward was earned.
                override def onUserEarnedReward(reward: RewardItem): Unit = {
                  earned = true
                }
                override def onRewardedAdFailedToShow(errorCode: Int): Unit = {}
              }
              rewardedAd.show(self, callback)
            }
          }
        })
        true
      } else {
        false
      }
    }

  }
  override val Ads = AndroidAdMobAds

}
