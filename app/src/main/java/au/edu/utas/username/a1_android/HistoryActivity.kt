package au.edu.utas.username.a1_android

import android.content.Intent
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import au.edu.utas.username.a1_android.databinding.ActivityHistoryBinding
import au.edu.utas.username.a1_android.ui.main.SectionsPagerAdapter
import au.edu.utas.username.a1_android.databinding.ActivityMainBinding

class HistoryActivity : AppCompatActivity() {

    private lateinit var ui: ActivityHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ui = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(ui.root)

        ui.back.setOnClickListener(){
            var main = Intent(this, MainActivity::class.java)
            startActivity(main)
        }

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = ui.viewPager
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = ui.tabs
        tabs.setupWithViewPager(viewPager)
    }
}