package com.code_23.ta_eye_go.ui.bookbus

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.code_23.ta_eye_go.BuildConfig
import com.code_23.ta_eye_go.DB.DataModelDB
import com.code_23.ta_eye_go.DB.bordinglist
import com.code_23.ta_eye_go.DB.getofflist
import com.code_23.ta_eye_go.R
import com.code_23.ta_eye_go.ui.pay.Pay
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_in_bus.*
import kotlinx.android.synthetic.main.activity_in_bus.currentStation_text
import kotlinx.android.synthetic.main.alertdialog_item.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL


class InBus : AppCompatActivity() {

    // 이전 단계에서 받아와야 하는 변수들(3개)
    private var startSttnID : String? = "" // 출발 정류장 id
    private var endSttnID : String? = "" // 도착 정류장 id
    private var routeId : String? = "" // 탑승 버스의 노선 번호 id

    private val citycode : Int = 23 // 도시코드 (인천 : 23)
    private var vehicleNo : String? = null // 버스 번호
    private var nodeord : Int? = null // 현재 정류장의 순서 번호 -> 노선마다 정류장에 순서대로 번호를 붙임
    private var endNodeord : Int? = null // 도착 정류장의 순서 번호
    private var thisStation : String? = null // 현재 정류장 이름
    private var nextStation : String? = null // 도착 정류장 이름
    private var leftSttnCnt : Int = 0 // 남은 정류장

    private var startSttnNm : String? = "" // 출발(현재) 정류장 이름
    private var endSttnnNm : String? = "" // 도착 정류장 이름
    private var onBoardSttnNm : String? = ""

    // 도착 여부 확인용
    var arrived = false

    private val key = BuildConfig.TAGO_API_KEY
    private val addressMybusLc = "http://apis.data.go.kr/1613000/BusLcInfoInqireService/getRouteAcctoBusLcList?serviceKey=" //노선별버스위치 목록조회
    private val addressGetNodeord = "http://apis.data.go.kr/1613000/BusRouteInfoInqireService/getRouteAcctoThrghSttnList?serviceKey=" //노선별경유정류소목록 조회

    // Room DB
    private var datamodelDB : DataModelDB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_bus)

        datamodelDB = DataModelDB.getInstance(this)
        val a = datamodelDB?.datamodelDao()?.getAll()
        onBoardSttnNm = a?.get(0)?.startNodenm

        // 탑승 예정 데이터 서버로 전송
        val database = Firebase.database
        var driverdata = database.getReference("Driver").child("on board")
        val toDriver =  bordinglist(onBoardSttnNm)   // 탑승정류장
        driverdata.setValue(toDriver)

        refreshBtn.setOnClickListener {
            if (!arrived) {
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Main){
                        val thread = NetworkThread()
                        thread.start()
                        thread.join()
                    }
                    if (arrived) {
                        driverdata = database.getReference("Driver").child("get off")
                        val arrive =  getofflist(endSttnnNm)   // 도착정류장
                        driverdata.setValue(arrive)
                        datamodelDB?.datamodelDao()?.deleteAll()
                        moveToPay()
                    }
                    delay(1000)
                    if (arrived) {
                        driverdata = database.getReference("Driver").child("get off")
                        val arrive =  getofflist(endSttnnNm)   // 도착정류장
                        driverdata.setValue(arrive)
                        datamodelDB?.datamodelDao()?.deleteAll()
                        moveToPay()
                    }
                }
            }
        }

        getoffBtn.setOnClickListener {
            confirmDialog()
        }

        Toast.makeText(this,"탑승 완료, 탑승 후 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Main) {
                delay(1500)
                startSttnNm = a?.get(0)?.startNodenm
                endSttnnNm = a?.get(0)?.endNodenm
                startSttnID = a?.get(0)?.startNodeID
                endSttnID = a?.get(0)?.endNodeID
                routeId = a?.get(0)?.routeID
                val thread2 = NetworkThread2()
                thread2.start()
                thread2.join()

                delay(1000)
                val thread = NetworkThread()
                thread.start()
                thread.join()
            }
            // 버스 도착 정보와 버스 위치 정보의 싱크 오류 시 재 검색 (세 번까지 시도)
            if (nodeord == null) {
                delay(2000)
                val thread = NetworkThread()
                thread.start()
                thread.join()
            }
            if (nodeord == null) {
                delay(3000)
                val thread = NetworkThread()
                thread.start()
                thread.join()
            }
            if (nodeord == null) {
                delay(4000)
                val thread = NetworkThread()
                thread.start()
                thread.join()
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            withContext(Main) {
                delay(5000)
                val thread = NetworkThread()
                thread.start()
                thread.join()

                for (i in 0..100)  {
                    if (!arrived) {
                        CoroutineScope(Dispatchers.IO).launch {
                            withContext(Main) {
                                val thread = NetworkThread()
                                thread.start()
                                thread.join()
                            }
                        }
                    }
                    if (arrived) {
                        driverdata = database.getReference("Driver").child("get off")
                        val arrive =  getofflist(endSttnnNm)   // 도착정류장
                        driverdata.setValue(arrive)
                        datamodelDB?.datamodelDao()?.deleteAll()
                        break
                    }
                    delay(1000)
                    if (arrived) {
                        driverdata = database.getReference("Driver").child("get off")
                        val arrive =  getofflist(endSttnnNm)   // 도착정류장
                        driverdata.setValue(arrive)
                        datamodelDB?.datamodelDao()?.deleteAll()
                        break
                    }
                    delay(19000)
                }
                if (arrived) moveToPay()
            }
        }
    }

    override fun onBackPressed() {
    }

    private fun moveToPay() {
        // 예약 후 화면 이동
        val intent = Intent(this, Pay::class.java)
        startActivity(intent)
        finish()
    }

    private fun confirmDialog() {
        val layoutInflater = LayoutInflater.from(this)
        val view = layoutInflater.inflate(R.layout.alertdialog_item, null)

        val alertDialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        view.menu_name.text = ""
        view.menu_content.text = "하차하시겠습니까?"

        alertDialog.show()

        view.btn_yes.setOnClickListener {
            datamodelDB?.datamodelDao()?.deleteAll()
            val database = Firebase.database
            val driverdata = database.getReference("Driver").child("get off i")
            val getoff =  getofflist(endSttnnNm)   // 도착정류장
            driverdata.setValue(getoff)
            alertDialog.dismiss()
            val intent = Intent(this, Pay::class.java)
            startActivity(intent)
            finish()
        }
        view.btn_no.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun parsing1(urlAddress: String?) : StringBuffer {
        val url = URL(urlAddress)
        val conn = url.openConnection()
        val input = conn.getInputStream()
        val isr = InputStreamReader(input)
        val br = BufferedReader(isr)

        var str: String?
        val buf = StringBuffer()

        do {
            str = br.readLine()

            if (str != null) {
                buf.append(str)
            }
        } while (str != null)

        return buf
    }

    // 초기 화면
    inner class NetworkThread2 : Thread() {
        @SuppressLint("SetTextI18n")
        override fun run() {
            val urlAddress = "${addressGetNodeord}${key}&numOfRows=1000&cityCode=${citycode}&routeId=${routeId}&_type=json"
            Log.d("url", urlAddress)

            try {
                val buf = parsing1(urlAddress)
                val jsonObject = JSONObject(buf.toString())
                val response = jsonObject.getJSONObject("response").getJSONObject("body")
                    .getJSONObject("items")
                val item = response.getJSONArray("item")
                var sttnCnt = 0
                for (i in 0 until item.length()) {
                    val iObject = item.getJSONObject(i)
                    if (iObject.getString("nodeid") == startSttnID) {
                        currentStation_text.text = onBoardSttnNm
                        sttnCnt = iObject.getInt("nodeord")
                        nextStation_text.text = item.getJSONObject(i+1).getString("nodenm")
                        continue
                    }
                    if (iObject.getString("nodeid") == endSttnID) {
                        endNodeord = iObject.getInt("nodeord")
                        sttnCnt = iObject.getInt("nodeord") - sttnCnt
                        numOfStations_text.text = "$sttnCnt 정류장"
                        break
                    }
                }
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    inner class NetworkThread : Thread() {
        @SuppressLint("SetTextI18n")
        override fun run() {
            // 탑승한 차량의 위치 정보
            if (vehicleNo == null) { // 탑승한 버스 차량 번호를 모를 때 (처음 돌렸을 경우)
                val urlAddress = "${addressMybusLc}${key}&cityCode=${citycode}&routeId=${routeId}&numOfRows=1000&_type=json"
                Log.d("url", urlAddress)

                try {
                    val buf = parsing1(urlAddress)
                    val jsonObject = JSONObject(buf.toString())

                    // 결과가 하나 있을 때는 Array로 처리하면 오류가 나기 때문에 구분해주기
                    val totalCnt = jsonObject.getJSONObject("response").getJSONObject("body")
                        .getInt("totalCount")
                    // 결과가 하나일 경우
                    if (totalCnt == 1) {
                        val response = jsonObject.getJSONObject("response").getJSONObject("body")
                            .getJSONObject("items").getJSONObject("item")
                        vehicleNo = response.getString("vehicleno")
                        nodeord = response.getInt("nodeord")
                    }
                    else {
                        val response = jsonObject.getJSONObject("response").getJSONObject("body")
                            .getJSONObject("items")
                        val item = response.getJSONArray("item")
                        for (i in 0 until item.length()) {
                            val iObject = item.getJSONObject(i)
                            if (iObject.getString("nodeid") == startSttnID) {
                                vehicleNo = iObject.getString("vehicleno")
                                nodeord = iObject.getInt("nodeord")
                                break
                            }
                        }
                    }
                    if (nodeord == null) {
                        return
                    }
                } catch (e: MalformedURLException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            } else { // 이미 버스 차량번호를 알고 있을 때
                val urlAddress = "${addressMybusLc}${key}&cityCode=${citycode}&routeId=${routeId}&numOfRows=1000&_type=json"
                Log.d("url", urlAddress)
                try {
                    val buf = parsing1(urlAddress)

                    val jsonObject = JSONObject(buf.toString())
                    val totalCnt = jsonObject.getJSONObject("response").getJSONObject("body")
                        .getInt("totalCount")

                    // 결과가 하나일 경우
                    if (totalCnt == 1) {
                        val response = jsonObject.getJSONObject("response").getJSONObject("body")
                            .getJSONObject("items").getJSONObject("item")
                        nodeord = response.getInt("nodeord")
                    }
                    else {
                        val response = jsonObject.getJSONObject("response").getJSONObject("body")
                            .getJSONObject("items")
                        val item = response.getJSONArray("item")
                        for (i in 0 until item.length()) {
                            val iObject = item.getJSONObject(i)
                            if (iObject.getString("vehicleno") == "$vehicleNo") {
                                nodeord = iObject.getInt("nodeord")
                                break
                            }
                        }
                    }
                } catch (e: MalformedURLException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }

            // 현재 정류장의 다음 정류장 조회
            // nodeord는 현재 정류장, nodeord+1은 다음 정류장임을 이용한다.
            val urlAddress1 =
                "${addressGetNodeord}${key}&numOfRows=1&pageNo=${nodeord}&cityCode=${citycode}&routeId=${routeId}&_type=json"
            val urlAddress2 =
                "${addressGetNodeord}${key}&numOfRows=1&pageNo=${nodeord?.plus(1)}&cityCode=${citycode}&routeId=${routeId}&_type=json"

            Log.d("url", urlAddress1)
            Log.d("url", urlAddress2)

            try {
                val buf1 = parsing1(urlAddress1)
                val buf2 = parsing1(urlAddress2)

                val jsonObject1 = JSONObject(buf1.toString())
                val jsonObject2 = JSONObject(buf2.toString())

                val response1 = jsonObject1.getJSONObject("response").getJSONObject("body")
                    .getJSONObject("items").getJSONObject("item")
                val response2 = jsonObject2.getJSONObject("response").getJSONObject("body")
                    .getJSONObject("items").getJSONObject("item")

                thisStation = response1.getString("nodenm")
                nextStation = response2.getString("nodenm")

                currentStation_text.text = thisStation
                nextStation_text.text = nextStation

            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            // 남은 정류장 수 계산하기
            leftSttnCnt = endNodeord!! - nodeord!!
            if (leftSttnCnt <= 0) { // 남은 정류장이 0 이하가 되면
                arrived = true
            }
            numOfStations_text.text = "$leftSttnCnt 정류장"

            if (leftSttnCnt < 3) { // 남은 정류장이 1개일때 기사용 서버 알림
                val database = Firebase.database
                val driverdata = database.getReference("Driver").child("get off i")
                val toDriver =  getofflist(endSttnnNm)   // 도착정류장
                driverdata.setValue(toDriver)
            }
        }
    }
}