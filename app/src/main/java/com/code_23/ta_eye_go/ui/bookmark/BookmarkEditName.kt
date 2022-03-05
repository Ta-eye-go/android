package com.code_23.ta_eye_go.ui.bookmark

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.code_23.ta_eye_go.R
import kotlinx.android.synthetic.main.activity_bookmark_edit_name.*
import kotlinx.android.synthetic.main.menu_bar.view.*

class BookmarkEditName : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark_edit_name)
        nickname_menu.menu_text.text = "별칭 수정"

        btn_cancel.setOnClickListener{
            startActivity(Intent(this, BookmarkMain::class.java))
            finish()
        }

        btn_complete.setOnClickListener {
            //bookmark textview가 변경되도록
            val text = edt_name.text.toString()
            val intent = Intent(this, BookmarkMain::class.java)
            intent.putExtra("Data", text)
            startActivity(intent)
        }
    }
    //뒤로가기 버튼 비활성화
    override fun onBackPressed() {
        //super.onBackPressed()
    }
}