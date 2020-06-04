package edu.uw.eep523.mapslocation.module.routeobject

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import edu.uw.eep523.mapslocation.MapsLayersActivity
import edu.uw.eep523.mapslocation.OnItemClickListener
import edu.uw.eep523.mapslocation.R
import edu.uw.eep523.mapslocation.RouteDatabaseAdapter
import kotlinx.android.synthetic.main.activity_home.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class HomeActivity : AppCompatActivity(),View.OnClickListener, OnItemClickListener {
    private lateinit var databaseHelper: RouteDatabaseAdapter
    private var routeList= ArrayList<RouteDataObject>()
    private lateinit var routeDataObjectAdapter : RouteDataObjectAdapter
    private var Groutefilename = ""
    private var GrouteDate= ""
    private var GrouteCategory= ""
    private var GrouteDistance= ""

    val seriesTitles = mutableListOf<String>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        ///
        val routefilename = intent.getStringExtra("routeFilename")
        val routeDistance = intent.getStringExtra("routeDistance")
        val routeCategory = intent.getStringExtra("routeCategory")
        val routeDate = intent.getStringExtra("routeDate")
        val id = intent.getStringExtra("id")



        if(routeCategory != null) {
            Log.e("routeDistance", routeDistance.toString())
            Log.e("routefilename", routefilename)


            var RDO: RouteDataObject = RouteDataObject()
            RDO.routeFilename = routefilename
            RDO.routeDistance = routeDistance
            RDO.routeCategory = routeCategory
            RDO.routeDate = routeDate
            RDO.id = id.toInt()

            // add the data to the db
            routeList.add(RDO)

            Log.e("routeList", routeList.size.toString())

        }
        else{
            Log.e("routeCategory", "null")

        }
        ///



        initView()
    }



    /**
     * This method is used to set views and clicklistener
     */
    private fun initView() {

        setClickListener()

        databaseHelper = RouteDatabaseAdapter(this)
        routeDataObjectAdapter = RouteDataObjectAdapter(routeList)
        routeDataObjectAdapter.setOnClickListener(this)
        routeRecylerview.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        routeRecylerview.adapter = routeDataObjectAdapter

    }

    fun startLayers(v: View){
        val intent = Intent(this, MapsLayersActivity::class.java).apply {}
        startActivity(intent)

    }

    private fun setClickListener() {
    }

    /**
     * This method is used to get data from db
     */
    private fun getDataFromDb() {
        routeList.clear()
        routeList.addAll( databaseHelper.getAllData())

        for(item in routeList){
            Log.e("item", item.routeFilename.toString())

        }


        getSeriesTitles()


        if(seriesTitles.size > 0){
            launchGraph()
            constructSeries()
        }

        showData()
    }

    private fun getSeriesTitles(){
        for(item in routeList){
            val cur_string = item.routeCategory.toString()
            if(cur_string !in seriesTitles)
            {
                seriesTitles.add(cur_string)
            }
        }
        Log.e("itemsize", seriesTitles.size.toString())
    }

    private fun constructSeries(){

        val colors = arrayOf<Int>(Color.RED, Color.BLUE, Color.GREEN,  Color.YELLOW, Color.CYAN,Color.LTGRAY, Color.DKGRAY)

        var seriesIndex = 0

        for(series in seriesTitles){

            Log.e("series", series)

            var holdingSeries: LineGraphSeries<DataPoint> = LineGraphSeries<DataPoint>(arrayOf<DataPoint>())
            val dataPointArray = mutableListOf<DataPoint>()

            val color = colors[seriesIndex]
            seriesIndex++

            val seriesparts = series.split("Route Type: ")
            val seriesName = seriesparts[1]
            var itemIndex = 0


            for(item in routeList){
                if(item.routeCategory == series){

                    val index = dataPointArray.size

                    val itemDate = dateToString(item.routeDate)
                    val parts = item.routeDistance.split("Distance (km):")


                    //dataPointArray.set(0, DataPoint(0.0, 0.0))
                    dataPointArray.add(DataPoint(itemIndex.toDouble(), parts[1].toDouble()))
                    //dataPointArray.add(DataPoint(itemDate, parts[1].toDouble()))
                    itemIndex++
                }
            }
            holdingSeries  = LineGraphSeries<DataPoint>(dataPointArray.toTypedArray())

            addToGraph(seriesName, holdingSeries, color)
        }
    }

    private fun addToGraph(seriesName: String, holdingSeries: LineGraphSeries<DataPoint>, color: Int){
        graph.addSeries(holdingSeries)
        holdingSeries.setTitle(seriesName)
        holdingSeries.color = color
    }


    private fun dateToString(dateString: String): Date {

        val parts = dateString.split("-")
        Log.e("parts", parts.toString())

        val date:Date = Date(parts[2].toInt(), parts[0].toInt(), parts[1].toInt())//Date(2020,3,1)

        return  date
    }

    private fun launchGraph(){



        val graph = findViewById(R.id.graph) as GraphView


        // styling legend
        graph.legendRenderer.isVisible = true
        graph.legendRenderer.textSize = 25f
        graph.legendRenderer.backgroundColor = Color.argb(30, 50, 0, 0)
        graph.legendRenderer.textColor = Color.WHITE
        graph.legendRenderer.setFixedPosition(0,0)
        //graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        //graph.getLegendRenderer().setMargin(30);

        // titles
        graph.title = "Distance Over Time"
        graph.gridLabelRenderer.verticalAxisTitle = "km"
        graph.gridLabelRenderer.horizontalAxisTitle = "Time"
        graph.gridLabelRenderer.isHorizontalLabelsVisible = false

        // axes
        /*
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(130);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinY(-115);
        graph.getViewport().setMaxY(0);
        graph.getViewport().setYAxisBoundsManual(true);
         */


    }

    /**
     * This method will show data when list is not empty otherwise no data found ui will be shown
     */
    private fun showData() {
        if(routeList.size == 0){
            noDataFoundTv.visibility=View.VISIBLE
        }else{
            routeDataObjectAdapter.notifyDataSetChanged()
            noDataFoundTv.visibility=View.GONE
        }
    }


    // ***
    override fun onClick(view: View?) {
    }



    override fun onResume() {
        super.onResume()
        graph.removeAllSeries()
        getDataFromDb()
    }

    override fun itemClickListener(view: View, position: Int) {

    }




}
