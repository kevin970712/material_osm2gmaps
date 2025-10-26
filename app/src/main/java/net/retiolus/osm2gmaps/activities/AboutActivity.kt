package net.retiolus.osm2gmaps.activities

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import net.retiolus.osm2gmaps.R

class AboutActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setUpToolbar()
        setUpFooter()

        val versionTextView = findViewById<TextView>(R.id.aboutOsm2gmapsVersion)
        val modelTextView = findViewById<TextView>(R.id.aboutYourDeviceModel)
        val androidVersionTextView = findViewById<TextView>(R.id.aboutYourDeviceAndroidVersion)
        val sdkVersionTextView = findViewById<TextView>(R.id.aboutYourDeviceSdkVersion)

        val bugFeatureTextView = findViewById<TextView>(R.id.aboutBugFeatureText)
        val mapuTextView = findViewById<TextView>(R.id.aboutMapuText)
        val osm2gampsLicenseTextView = findViewById<TextView>(R.id.aboutOsm2gmapsLicense)
        val openMojiLicenseTextView = findViewById<TextView>(R.id.aboutOpenMojiLicense)
        val footerTextView = findViewById<TextView>(R.id.footerLinks)

        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val versionName = packageInfo.versionName

        versionTextView.text = getString(R.string.about_osm2gmaps_version, versionName)
        modelTextView.text = getString(R.string.about_device_model, Build.MODEL)
        androidVersionTextView.text =
            getString(R.string.about_device_android_version, Build.VERSION.RELEASE)
        sdkVersionTextView.text =
            getString(R.string.about_device_sdk_version, Build.VERSION.SDK_INT.toString())

        val allLinkTextViews = listOf(
            bugFeatureTextView,
            mapuTextView,
            osm2gampsLicenseTextView,
            openMojiLicenseTextView,
            footerTextView
        )
        setUpLinks(allLinkTextViews)
    }

    private fun setUpLinks(textViews: List<TextView>) {
        textViews.forEach { textView ->
            textView?.let {
                it.movementMethod = LinkMovementMethod.getInstance()
                it.setLinkTextColor(Color.CYAN)
            }
        }
    }
}
