package net.retiolus.osm2gmaps.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import net.retiolus.osm2gmaps.R

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    protected fun setUpToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    openConfigActivity()
                    true
                }

                R.id.action_history -> {
                    openHistoryActivity()
                    true
                }

                R.id.action_about -> {
                    openAboutActivity()
                    true
                }

                R.id.action_custom_links -> {
                    openCustomLinksActivity()
                    true
                }

                else -> false
            }
        }
    }

    // temp function to set up footer in config and about activities
    protected fun setUpFooter() {
        val linkText = findViewById<TextView>(R.id.footer)
        linkText.movementMethod = LinkMovementMethod.getInstance()
        linkText.setLinkTextColor(Color.CYAN)
    }

    private fun openConfigActivity() {
        startActivity(Intent(this, ConfigActivity::class.java))
    }

    private fun openCustomLinksActivity() {
        startActivity(Intent(this, CustomLinkActivity::class.java))
    }

    private fun openAboutActivity() {
        startActivity(Intent(this, AboutActivity::class.java))
    }

    private fun openHistoryActivity() {
        startActivity(Intent(this, HistoryActivity::class.java))
    }
}
