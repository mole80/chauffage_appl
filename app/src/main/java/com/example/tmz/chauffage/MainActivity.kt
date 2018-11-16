package com.example.tmz.chauffage

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.zip.Inflater
import kotlin.math.log

class Room(val id : Int, val name : String){
    var target : Float = 0.0f
    var meas : Float = 0.0f
    var temp_start : Float = 0.0f
    var temp_stop : Float = 0.0f
    var cpt : Int = 0
    var timeout : Int = 0
    var valveIsOpen : String = ""
}

class Sensor(val id : Int, val name : String){
    var temp : Float = 0.0f
    var hum : Float = 0.0f
    var cpt : Int = 0
}

class SensorAdapter(private val context: Context,
private val dataSource : ArrayList<Sensor>) : BaseAdapter() {

    private val inflater : LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflater.inflate(R.layout.item_sensor, parent, false)
        val tv_name = view.findViewById(R.id.tv_name) as TextView
        val tv_temp = view.findViewById(R.id.tv_temp) as TextView
        val tv_hum = view.findViewById(R.id.tv_hum) as TextView
        val tv_id = view.findViewById(R.id.tv_id) as TextView
        val tv_cpt = view.findViewById(R.id.tv_cpt) as TextView

        tv_name.text = (getItem(position) as Sensor).name
        tv_temp.text = (getItem(position) as Sensor).temp.toString() + " °C"
        tv_hum.text = (getItem(position) as Sensor).hum.toString() + " %"
        tv_id.text = (getItem(position) as Sensor).id.toString()
        tv_cpt.text = (getItem(position) as Sensor).cpt.toString()

        return view
    }
}

class RoomAdapter(private val context: Context,
                  private val dataSource : ArrayList<Room>) : BaseAdapter() {

    private val inflater : LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setTextViewTemp(tv : TextView, meas : Float, cons : Float){
        tv.text = "Meas : $meas °C"

        try {
            if (meas > cons) {
                tv.setTextColor(Color.BLUE)
            }
            else{
                tv.setTextColor(Color.RED)
            }
        }
        catch (e:Exception){
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflater.inflate(R.layout.item_room, parent, false)
        val tv_name = view.findViewById(R.id.tv_name) as TextView
        val tv_temp = view.findViewById(R.id.tv_temp) as TextView
        //val tv_hum = view.findViewById(R.id.tv_hum) as TextView
        val tv_temp_start = view.findViewById(R.id.tv_temp_start) as TextView
        val tv_temp_end = view.findViewById(R.id.tv_temp_end) as TextView
        val tv_cpt = view.findViewById(R.id.tv_cpt) as TextView
        val tv_valve_open = view.findViewById(R.id.tv_valve_open) as TextView
        val tv_cons = view.findViewById(R.id.tv_cons) as TextView
        val tv_timeout = view.findViewById(R.id.tv_timeout) as TextView

        val r = getItem(position) as Room

        tv_name.text = r.name

        setTextViewTemp(tv_temp, r.meas, r.target )

        //tv_hum.text = (getItem(position) as Room).
        tv_temp_start.text = "Départ ${r.temp_start} °C"
        tv_temp_end.text = "Retour ${r.temp_stop} °C"
        tv_cpt.text = "Cpt : ${r.cpt}"
        tv_valve_open.text = r.valveIsOpen
        tv_cons.text = "Cons : " + r.target + " °C"

        tv_timeout.text = "Timeout : ${r.timeout}"
        if(r.timeout > 300){
            tv_timeout.setTextColor(Color.RED)
        }
        else{
            tv_timeout.setTextColor(Color.DKGRAY)
        }

        return view
    }
}

class MainActivity : AppCompatActivity(), AsyncRequest.Listeners {

    val test = "\n" +
            "<h2>Maulaz's Home</h2>\n" +
            "<h4>\n" +
            "\n" +
            "Capt,0,0,0,0\n" +
            "</br>\n" +
            "\n" +
            "Capt,1,20.80,47.40,10344\n" +
            "</br>\n" +
            "\n" +
            "Capt,2,19.50,64.40,10350\n" +
            "</br>\n" +
            "\n" +
            "Capt,3,0,0,0\n" +
            "</br>\n" +
            "\n" +
            "Capt,4,19.40,59.60,10351\n" +
            "</br>\n" +
            "\n" +
            "Capt,5,19.50,57.10,10349\n" +
            "</br>\n" +
            "\n" +
            "Capt,6,0,0,0\n" +
            "</br>\n" +
            "\n" +
            "Capt,7,0,0,0\n" +
            "</br>\n" +
            "\n" +
            "Capt,8,0,0,0\n" +
            "</br>\n" +
            "\n" +
            "Capt,9,0,0,0\n" +
            "</br>\n" +
            "\n" +
            "Valve,0,17.80,20.80,20.49,20.62,C,7,0\n" +
            "</br>\n" +
            "\n" +
            "Valve,1,17.90,19.50,20.49,22.93,C,1,0\n" +
            "</br>\n" +
            "\n" +
            "Valve,2,21.20,0.00,20.49,64.10,O,12465,1065\n" +
            "</br>\n" +
            "\n" +
            "Valve,3,18.10,19.40,20.49,64.10,C,0,0\n" +
            "</br>\n" +
            "\n" +
            "Valve,4,20.20,19.50,20.49,64.10,O,2,0\n" +
            "</br>\n" +
            "\n" +
            "\n" +
            "<h4/>\n"

    var rooms = ArrayList<Room>()
    var sensors = ArrayList<Sensor>()

    var r0 = Room(0,"Justine")
    var r1 = Room(1,"Garçon")
    var r3 = Room(3,"Parent lit")
    var r4 = Room(4,"Parent bureau")

    var r10 = Room(10,"Salon TV")
    var r11 = Room(11,"Salon")
    var r13 = Room(13,"Cuisine")
    var r14 = Room(14,"Hall")

    var s1 = Sensor(1, "Justine")
    var s2 = Sensor(2, "Garçon")
    var s4 = Sensor(4, "Parent lit")
    var s5 = Sensor(5, "Parent bureau")
    var s6 = Sensor(6, "Placard amis")

    var s11 = Sensor(11, "Salon")
    var s12 = Sensor(12, "Cuisine")
    var s13 = Sensor(13, "Corridor")
    var s14 = Sensor(14, "Cave")
    var s15 = Sensor(15, "Terasse")

    var floor : Int = 1

    lateinit var room_adapter : RoomAdapter
    lateinit var sensor_adapter : SensorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rooms.add(r0)
        rooms.add(r1)
        rooms.add(r3)
        rooms.add(r4)

        rooms.add(r10)
        rooms.add(r11)
        rooms.add(r13)
        rooms.add(r14)

        sensors.add(s1)
        sensors.add(s2)
        sensors.add(s4)
        sensors.add(s5)
        sensors.add(s6)

        sensors.add(s11)
        sensors.add(s12)
        sensors.add(s13)
        sensors.add(s14)
        sensors.add(s15)

        room_adapter = RoomAdapter(this, rooms)
        lv_rooms.adapter = room_adapter

        sensor_adapter = SensorAdapter(this, sensors)
        lv_sensors.adapter = sensor_adapter

        btn_read.setOnClickListener {
            //onPostExecute(test)
            floor = -1
            startRead()
        }

        if( this.checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 123)
        }
    }

    // "Valve , 1 , 17.90 , 19.50 , 20.49 , 22.93 , C , 1 , 0 \n"
    fun SetRoom(value:String){
        try {
            val v_str = value.split(',')
            var id = v_str[1].toInt()

            if(floor == -1){
                id += 20
            }
            else if(floor == 0){
                id += 10
            }

            for (r in rooms) {
                if (r.id == id) {
                    r.target = v_str[2].toFloat()
                    r.meas = v_str[3].toFloat()
                    r.temp_start = v_str[4].toFloat()
                    r.temp_stop = v_str[5].toFloat()
                    r.valveIsOpen = v_str[6]
                    r.timeout = v_str[7].toInt()
                    r.cpt = v_str[8].toInt()
                }
            }
        }
        catch (e:Exception)
        {}
    }

    // "Capt , 2 , 19.50 , 64.40 , 10350\n"
    fun SetSensor(value:String){
        try {
            val capt_str = value.split(',')
            val id = capt_str[1].toInt()
            for (s in sensors) {
                if (s.id == id) {
                    s.temp = capt_str[2].toFloat()
                    s.hum = capt_str[3].toFloat()
                    s.cpt = capt_str[4].split('\r')[0].toInt()
                }
            }
        }
        catch (e:Exception)
        {}
    }


    fun startRead(){
        AsyncRequest(this)?.execute()
    }

    fun ReadResponse(result : String?){
        try {
            var txt = result?.split('\n')

            if(txt!!.count() < 30){
                txt = result?.split('\r')
            }

            txt?.forEach {
                if (it.contains("Capt")) {
                    SetSensor(it)
                } else if (it.contains("Valve")) {
                    SetRoom(it)
                }
            }
        }
        catch (e:Exception){}
    }

    override fun doInBackground() {

    }

    override fun onPostExecute(result: String?) {
        var res = result
        try {
            if( res != "" ) {
                ReadResponse(result)
                sensor_adapter.notifyDataSetChanged()
                room_adapter.notifyDataSetChanged()
            }
        }
        catch (e:Exception){
            res = ""
        }
        finally {
            if(res == ""){
                btn_read.text = "Read " + floor.toString() + " : Error"
            }
            else{
                btn_read.text = "Read " + floor.toString() + ": Ok"
            }

            Thread.sleep(300)

            if(floor < 1){
                floor++
                startRead()
            }
            else{
                floor = -1
            }
        }
    }

    override fun onPreExecute() :Int {
        return floor
    }
}

class AsyncRequest(cb : Listeners) : AsyncTask<Void, Void, String>(){

    var callback : WeakReference<Listeners>? = null

    var floor : Int? = 1

    init{
        callback = WeakReference(cb)
    }

    interface Listeners {
        fun onPreExecute() : Int
        fun doInBackground()
        fun onPostExecute(result: String?)
    }

    override fun doInBackground(vararg params: Void?): String {
        callback?.get()?.doInBackground()

        var res: String = ""

        //val u = URL("http://www.google.ch/")
        try {
            lateinit var u : URL
            if(floor == -1) {
                //u = URL("http://192.168.100.62/") // Correct address
                u = URL("http://192.168.100.60/")
            }
            else if(floor == 0){
                u = URL("http://192.168.100.60/")
            }
            else{
                u = URL("http://192.168.100.61/")
            }
            res = u.readText()
        }
        catch (e:Exception){}
        /*val urlConnection = u.openConnection() as HttpURLConnection

        try {
            urlConnection.connect()
            val repCode = urlConnection.responseCode
            if( repCode != 200 ){
                return ""
            }

            val st = BufferedInputStream( urlConnection.inputStream )

            val sReader = InputStreamReader(st, "UTF-8")
            val buffReader = BufferedReader(sReader)
            val strBuff = StringBuffer()
            var line : String?

            do {
                line = buffReader.readLine()
                strBuff.append(line + "\n")
            }while( line != null )

            res = strBuff.toString()

        }catch (e : Exception){
            Log.e("Exc", e.toString())
        }
        finally {
            urlConnection.disconnect()
        }*/

        return res
   }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        callback?.get()?.onPostExecute(result)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        floor = callback?.get()?.onPreExecute()
    }
}