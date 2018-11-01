package com.example.tmz.chauffage

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL
import java.util.zip.Inflater

class Room(val id : Int, val name : String){
    var target : Float = 0.0f
    var meas : Float = 0.0f
    var temp_start : Float = 0.0f
    var temp_stop : Float = 0.0f
    var cpt : Int = 0
    var valveIsOpen : String = ""
}

class Sensor(val id : Int, val name : String){
    var temp : Float = 0.0f
    var hum : Float = 0.0f
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

        tv_name.text = (getItem(position) as Sensor).name
        tv_temp.text = (getItem(position) as Sensor).temp.toString() + " °C"
        tv_hum.text = (getItem(position) as Sensor).hum.toString() + " %"

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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflater.inflate(R.layout.item_room, parent, false)
        val tv_name = view.findViewById(R.id.tv_name) as TextView
        val tv_temp = view.findViewById(R.id.tv_temp) as TextView
        //val tv_hum = view.findViewById(R.id.tv_hum) as TextView
        val tv_temp_start = view.findViewById(R.id.tv_temp_start) as TextView
        val tv_temp_end = view.findViewById(R.id.tv_temp_end) as TextView
        val tv_cpt = view.findViewById(R.id.tv_cpt) as TextView
        val tv_valve_open = view.findViewById(R.id.tv_valve_open) as TextView

        tv_name.text = (getItem(position) as Room).name
        tv_temp.text = (getItem(position) as Room).meas.toString() + " °C"
        //tv_hum.text = (getItem(position) as Room).
        tv_temp_start.text = (getItem(position) as Room).temp_start.toString()
        tv_temp_end.text = (getItem(position) as Room).temp_stop.toString()
        tv_cpt.text = (getItem(position) as Room).cpt.toString()
        tv_valve_open.text = (getItem(position) as Room).valveIsOpen

        return view
    }
}

class MainActivity : AppCompatActivity() {

    var rooms = ArrayList<Room>()
    var sensors = ArrayList<Sensor>()

    var r0 = Room(0,"Justine")
    var r1 = Room(1,"Garçon")
    var r2 = Room(3,"Parent lit")
    var r3 = Room(4,"Parent bureau")

    var s1 = Sensor(1, "Justine")
    var s2 = Sensor(2, "Garçon")
    var s4 = Sensor(4, "Parent lit")
    var s5 = Sensor(5, "Parent bureau")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rooms.add(r0)
        rooms.add(r1)
        rooms.add(r2)
        rooms.add(r3)

        sensors.add(s1)
        sensors.add(s2)
        sensors.add(s4)
        sensors.add(s5)

        val room_adapter = RoomAdapter(this, rooms)
        lv_rooms.adapter = room_adapter

        val sensor_adapter = SensorAdapter(this, sensors)
        lv_sensors.adapter = sensor_adapter

        btn_read.setOnClickListener {
            readMeasure()
        }
    }

    fun readMeasure() {
        val res = URL("http://192.168.43.20").readText()
        var txt = res.split('\r')
        txt.forEach {
            if( it.contains("Capt") ){
                val capt_str = it.split(',')
                val id = capt_str[1].toInt()
            }
            else if( it.contains("Valve")){
                val v_str = it.split(',')

            }
        }
    }

}
