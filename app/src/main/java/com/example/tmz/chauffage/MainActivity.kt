package com.example.tmz.chauffage

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL

class Room(val id : Int, val name : String){
    var target : Float = 0.0f
    var meas : Float = 0.0f
}

class Sensor(val id : Int, val name : String){
    var temp : Float = 0.0f
    var hum : Float = 0.0f
}

class MainActivity : AppCompatActivity() {

    var rooms = mutableListOf<Room>()
    var sensors = mutableListOf<Sensor>()

    var r0 = Room(0,"Justine")
    var r1 = Room(1,"Garçon")
    var r2 = Room(3,"Parent lit")
    var r3 = Room(4,"Parent bureau")

    var s0 = Sensor(0, "Justine")
    var s1 = Sensor(0, "Garçon")
    var s2 = Sensor(0, "Parent")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rooms.add(r0)
        rooms.add(r1)
        rooms.add(r2)
        rooms.add(r3)

        sensors.add(s0)
        sensors.add(s1)
        sensors.add(s2)


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
