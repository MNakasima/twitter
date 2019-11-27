package com.mnakasima.twitter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_login.*

class Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        ivProfile.setOnClickListener( View.OnClickListener {
            //TODO later
        })
    }

    fun buLoginEvent(view: View){

    }
}
