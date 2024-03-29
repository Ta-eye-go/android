package com.code_23.ta_eye_go.ui.settings

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.code_23.ta_eye_go.DB.User
import com.code_23.ta_eye_go.DB.UserDB
import com.code_23.ta_eye_go.R
import com.code_23.ta_eye_go.ui.login.LoginMain
import com.code_23.ta_eye_go.ui.main.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.user.UserApiClient
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_settings.menu
import kotlinx.android.synthetic.main.menu_bar.*
import kotlinx.android.synthetic.main.menu_bar.view.*

class Settings : AppCompatActivity(){

    // 로그아웃 구현을 위한 변수
    var auth : FirebaseAuth ?= null
    var googleSignInClient : GoogleSignInClient?= null
    // UserDB
    private var userDB : UserDB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        menu.menu_text.text = "설정"

        // Room DB
        userDB = UserDB.getInstance(this)

        // 구글 로그아웃을 위해 로그인 세션 가져오기
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // firebaseauth를 사용하기 위한 인스턴스 get
        auth = FirebaseAuth.getInstance()

        text_logout.setOnClickListener {

            UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
                if (error != null) {
                    userDB?.userDao()?.deleteAll()    // 유저 DB 초기화
                    // 구글 로그아웃
                    FirebaseAuth.getInstance().signOut()
                    googleSignInClient?.signOut()

                    val logoutIntent = Intent(this, LoginMain::class.java)
                    logoutIntent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(logoutIntent)
                    finish()
                }
                else if (tokenInfo != null) {
                    // 카카오 로그아웃, 로그아웃 눌렀을때 로그아웃+팝업, 로그인 화면으로 전환
                    UserApiClient.instance.logout { error ->
                        if (error != null) {
                            Log.e(TAG, "로그아웃 실패. SDK에서 토큰 삭제됨", error)
                            //Toast.makeText(this, "로그아웃 실패 $error", Toast.LENGTH_SHORT).show()
                        }else {
                            userDB?.userDao()?.deleteAll()    // 유저 DB 초기화
                            Log.i(TAG, "로그아웃 성공. SDK에서 토큰 삭제됨")
                            //Toast.makeText(this, "로그아웃 성공", Toast.LENGTH_SHORT).show()
                        }
                        val intent = Intent(this, LoginMain::class.java)
                        startActivity(intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP))
                        finish()
                    }
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        back_btn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        //간단 사용 설명서
        text_guide.setOnClickListener {
            val intent = Intent(this, Guide1::class.java)
            startActivity(intent)
            finish()
        }

//        //카카오톡 로그인 정보
//        val nickname = findViewById<TextView>(R.id.nickname_txt)
//        val email = findViewById<TextView>(R.id.email_txt)
//
//        UserApiClient.instance.me { user, error ->
//            nickname.text = "닉네임: ${user?.kakaoAccount?.profile?.nickname}"
//            email.text = "이메일 : ${user?.kakaoAccount?.email}"
//        }

        btn_yellow.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        btn_blue.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        // 안내견 유무 스위치
        if (userDB?.userDao()?.userdata(Firebase.auth.currentUser?.email.toString()) == true) {
            switch_dog.isChecked = true
        }
        switch_dog.setOnCheckedChangeListener{CompoundButton, onSwitch ->
            val r = kotlinx.coroutines.Runnable {
                try {
                    // 안내견 on
                    if (onSwitch){
                        val email = Firebase.auth.currentUser?.email.toString()
                        val users = User(email, true)
                        userDB?.userDao()?.updateUser(users)
                        Toast.makeText(this, "안내견 on", Toast.LENGTH_SHORT).show()
                    }
                    // 안내견견 off
                    else{
                        val email = Firebase.auth.currentUser?.email.toString()
                        val users = User(email, false)
                        userDB?.userDao()?.updateUser(users)
                        Toast.makeText(this, "안내견 off", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.d("tag", "Error - $e")
                }
            }
            val thread = Thread(r)
            thread.start()
        }
    }
    override fun onDestroy() {
        UserDB.destroyInstance()
        userDB = null
        super.onDestroy()
    }
    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
