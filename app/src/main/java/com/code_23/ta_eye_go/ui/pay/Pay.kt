package com.code_23.ta_eye_go.ui.pay

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.code_23.ta_eye_go.ui.main.MainActivity
import kr.co.bootpay.Bootpay
import kr.co.bootpay.BootpayAnalytics
import kr.co.bootpay.enums.UX
import kr.co.bootpay.model.BootExtra
import kr.co.bootpay.model.BootUser

class Pay : AppCompatActivity() {

    private val application_id ="624ada9f2701800023f68ea1"
    private val stuck = 10
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BootpayAnalytics.init(this, application_id)
        Toast.makeText(this,"하차 완료, 결제 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
        goBootpayRequest()
    }
    fun goBootpayRequest() {
        val bootUser = BootUser().setPhone("010-1234-5678")
        val bootExtra = BootExtra().setQuotas(intArrayOf(0, 2, 3))

        val stuck = 1 //재고 있음

        Bootpay.init(this)
            .setApplicationId(application_id) // 해당 프로젝트(안드로이드)의 application id 값
            .setContext(this)
            .setBootUser(bootUser)
            .setBootExtra(bootExtra)
            .setUX(UX.PG_DIALOG)
//                .setUserPhone("010-1234-5678") // 구매자 전화번호
            .setName("버스 이용 결제") // 결제할 상품명
            .setOrderId("1234") // 결제 고유번호expire_month
            .setPrice(1250) // 결제할 금액
//            .addItem("마우's 스", 1, "ITEM_CODE_MOUSE", 100) // 주문정보에 담길 상품정보, 통계를 위해 사용
//            .addItem("키보드", 1, "ITEM_CODE_KEYBOARD", 200, "패션", "여성상의", "블라우스") // 주문정보에 담길 상품정보, 통계를 위해 사용
            .onConfirm { message ->
                if (0 < stuck) Bootpay.confirm(message) // 재고가 있을 경우.
                else Bootpay.removePaymentWindow() // 재고가 없어 중간에 결제창을 닫고 싶을 경우
                Log.d("confirm", message)
            }
            .onDone { message -> // 결제완료시 호출, 아이템 지급 등 데이터 동기화 로직을 수행합니다
                Log.d("done", message)
            }
            .onReady { message -> // 가상계좌 입금 계좌번호가 발급되면 호출되는 함수입니다.
                Log.d("ready", message)
            }
            .onCancel { message ->  // 결제 취소시 호출
                Log.d("cancel", message)
            }
            .onError{ message ->    // 에러가 났을때 호출되는 부분
                Log.d("error", message)
            }
            .onClose {     //결제창이 닫힐때 실행되는 부분
                Log.d("close", "close")
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .request()

    }
}