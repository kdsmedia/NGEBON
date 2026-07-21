package com.example.ui.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdHelper {
    private const val TAG = "AdHelper"

    const val BANNER_ID = "ca-app-pub-6881903056221433/5334452222"
    const val INTERSTITIAL_ID = "ca-app-pub-6881903056221433/8862065477"
    const val REWARDED_ID = "ca-app-pub-6881903056221433/7548983804"
    const val NATIVE_ID = "ca-app-pub-6881903056221433/1008314543"
    const val APP_OPEN_ID = "ca-app-pub-6881903056221433/6235902130"

    private var isInitialized = false

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    private var appOpenAd: AppOpenAd? = null
    private var isAppOpenLoading = false
    private var loadTime: Long = 0

    fun init(context: Context) {
        if (isInitialized) return
        MobileAds.initialize(context.applicationContext) {
            isInitialized = true
            Log.d(TAG, "Mobile Ads SDK Initialized")
            // Preload ads
            loadInterstitial(context.applicationContext)
            loadRewardedAd(context.applicationContext)
            loadAppOpenAd(context.applicationContext)
        }
    }

    // --- INTERSTITIAL ADS ---
    fun loadInterstitial(context: Context) {
        if (isInterstitialLoading || interstitialAd != null) return
        isInterstitialLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "Interstitial Ad Loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.e(TAG, "Failed to load Interstitial Ad: ${error.message}")
                }
            }
        )
    }

    fun showInterstitial(activity: Activity, onDismiss: () -> Unit) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    onDismiss()
                    loadInterstitial(activity.applicationContext)
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    onDismiss()
                    loadInterstitial(activity.applicationContext)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial Ad Showed")
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "Interstitial Ad not ready yet, loading it now")
            onDismiss()
            loadInterstitial(activity.applicationContext)
        }
    }

    // --- REWARDED ADS ---
    fun loadRewardedAd(context: Context) {
        if (isRewardedLoading || rewardedAd != null) return
        isRewardedLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    Log.d(TAG, "Rewarded Ad Loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    Log.e(TAG, "Failed to load Rewarded Ad: ${error.message}")
                }
            }
        )
    }

    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onDismiss: () -> Unit) {
        val ad = rewardedAd
        if (ad != null) {
            var earnedReward = false
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    if (earnedReward) {
                        onRewardEarned()
                    } else {
                        onDismiss()
                    }
                    loadRewardedAd(activity.applicationContext)
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    rewardedAd = null
                    onDismiss()
                    loadRewardedAd(activity.applicationContext)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded Ad Showed")
                }
            }
            ad.show(activity) { rewardItem ->
                earnedReward = true
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            }
        } else {
            Log.d(TAG, "Rewarded Ad not ready yet, loading it now")
            onDismiss()
            loadRewardedAd(activity.applicationContext)
        }
    }

    // --- APP OPEN ADS ---
    fun loadAppOpenAd(context: Context) {
        if (isAppOpenLoading || isAppOpenAdAvailable()) return
        isAppOpenLoading = true

        val adRequest = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            APP_OPEN_ID,
            adRequest,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isAppOpenLoading = false
                    loadTime = System.currentTimeMillis()
                    Log.d(TAG, "App Open Ad Loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd = null
                    isAppOpenLoading = false
                    Log.e(TAG, "Failed to load App Open Ad: ${error.message}")
                }
            }
        )
    }

    private fun isAppOpenAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = System.currentTimeMillis() - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numHours * numMilliSecondsPerHour
    }

    fun showAppOpenAd(activity: Activity, onDismiss: () -> Unit) {
        val ad = appOpenAd
        if (ad != null && isAppOpenAdAvailable()) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    onDismiss()
                    loadAppOpenAd(activity.applicationContext)
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    appOpenAd = null
                    onDismiss()
                    loadAppOpenAd(activity.applicationContext)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "App Open Ad Showed")
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "App Open Ad not ready, loading now")
            onDismiss()
            loadAppOpenAd(activity.applicationContext)
        }
    }
}

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .border(
                border = BorderStroke(
                    2.dp,
                    Brush.horizontalGradient(
                        listOf(Color(0xFF8B5A2B), Color(0xFFFFD54F), Color(0xFF8B5A2B))
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1A0E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bulletin Board Header Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📌", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "📢 PAPAN PENGUMUMAN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD54F)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "INFO & SPONSOR",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("📌", fontSize = 12.sp)
                }
            }

            // Inner Frame for Ad Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF170F0A))
                    .border(1.dp, Color(0xFF5D4037), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { context ->
                        AdView(context).apply {
                            setAdSize(AdSize.BANNER)
                            adUnitId = AdHelper.BANNER_ID
                            loadAd(AdRequest.Builder().build())
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AdNative(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val adLoader = AdLoader.Builder(context, AdHelper.NATIVE_ID)
            .forNativeAd { ad ->
                nativeAd = ad
                isLoading = false
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                    Log.e("AdHelper", "Native Ad failed to load: ${error.message}")
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())

        onDispose {
            nativeAd?.destroy()
        }
    }

    if (isLoading) {
        // Placeholder skeleton with M3 color scheme
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(8.dp)))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Box(modifier = Modifier.size(width = 120.dp, height = 16.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(4.dp)))
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(modifier = Modifier.size(width = 80.dp, height = 12.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(4.dp)))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().height(14.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(4.dp)))
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.align(Alignment.End).size(width = 100.dp, height = 36.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(8.dp)))
            }
        }
    } else {
        nativeAd?.let { ad ->
            val onSurface = MaterialTheme.colorScheme.onSurface
            val surface = MaterialTheme.colorScheme.surface
            val primary = MaterialTheme.colorScheme.primary
            val onPrimary = MaterialTheme.colorScheme.onPrimary
            val outlineColor = MaterialTheme.colorScheme.outlineVariant

            AndroidView(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                factory = { ctx ->
                    val nativeAdView = NativeAdView(ctx)
                    val container = android.widget.LinearLayout(ctx).apply {
                        orientation = android.widget.LinearLayout.VERTICAL
                        setPadding(32, 32, 32, 32)
                    }

                    val topRow = android.widget.LinearLayout(ctx).apply {
                        orientation = android.widget.LinearLayout.HORIZONTAL
                        gravity = android.view.Gravity.CENTER_VERTICAL
                    }

                    val iconView = ImageView(ctx).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(100, 100).apply {
                            rightMargin = 16
                        }
                    }
                    topRow.addView(iconView)
                    nativeAdView.iconView = iconView

                    val titleCol = android.widget.LinearLayout(ctx).apply {
                        orientation = android.widget.LinearLayout.VERTICAL
                    }
                    val headlineView = TextView(ctx).apply {
                        textSize = 15f
                        setTypeface(null, android.graphics.Typeface.BOLD)
                    }
                    titleCol.addView(headlineView)
                    nativeAdView.headlineView = headlineView

                    val advertiserView = TextView(ctx).apply {
                        textSize = 11f
                    }
                    titleCol.addView(advertiserView)
                    nativeAdView.advertiserView = advertiserView

                    topRow.addView(titleCol)
                    container.addView(topRow)

                    val bodyView = TextView(ctx).apply {
                        textSize = 13f
                        setPadding(0, 12, 0, 12)
                    }
                    container.addView(bodyView)
                    nativeAdView.bodyView = bodyView

                    val ctaButton = Button(ctx).apply {
                        layoutParams = android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        isAllCaps = false
                        textSize = 13f
                    }
                    container.addView(ctaButton)
                    nativeAdView.callToActionView = ctaButton

                    nativeAdView.addView(container)
                    nativeAdView
                },
                update = { nativeAdView ->
                    val container = nativeAdView.getChildAt(0) as android.widget.LinearLayout
                    container.background = android.graphics.drawable.GradientDrawable().apply {
                        setColor(surface.toArgb())
                        setStroke(2, outlineColor.toArgb())
                        cornerRadius = 24f
                    }

                    val topRow = container.getChildAt(0) as android.widget.LinearLayout
                    val iconView = topRow.getChildAt(0) as ImageView
                    val titleCol = topRow.getChildAt(1) as android.widget.LinearLayout
                    val headlineView = titleCol.getChildAt(0) as TextView
                    val advertiserView = titleCol.getChildAt(1) as TextView
                    val bodyView = container.getChildAt(1) as TextView
                    val ctaButton = container.getChildAt(2) as Button

                    headlineView.setTextColor(onSurface.toArgb())
                    advertiserView.setTextColor(onSurface.copy(alpha = 0.6f).toArgb())
                    bodyView.setTextColor(onSurface.copy(alpha = 0.8f).toArgb())
                    
                    ctaButton.setBackgroundColor(primary.toArgb())
                    ctaButton.setTextColor(onPrimary.toArgb())

                    headlineView.text = ad.headline
                    bodyView.text = ad.body

                    if (ad.icon != null) {
                        iconView.setImageDrawable(ad.icon?.drawable)
                        iconView.visibility = View.VISIBLE
                    } else {
                        iconView.visibility = View.GONE
                    }

                    if (ad.advertiser != null) {
                        advertiserView.text = ad.advertiser
                        advertiserView.visibility = View.VISIBLE
                    } else {
                        advertiserView.visibility = View.GONE
                    }

                    if (ad.callToAction != null) {
                        ctaButton.text = ad.callToAction
                        ctaButton.visibility = View.VISIBLE
                    } else {
                        ctaButton.visibility = View.GONE
                    }

                    nativeAdView.setNativeAd(ad)
                }
            )
        }
    }
}
