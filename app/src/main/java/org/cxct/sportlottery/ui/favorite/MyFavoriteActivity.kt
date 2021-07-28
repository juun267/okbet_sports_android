package org.cxct.sportlottery.ui.favorite

import android.os.Bundle
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_my_favorite.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.base.BaseFavoriteActivity

class MyFavoriteActivity : BaseFavoriteActivity<MyFavoriteViewModel>(MyFavoriteViewModel::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_favorite)

        setupToolbar()
    }

    private fun setupToolbar() {
        setSupportActionBar(favorite_toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }
}