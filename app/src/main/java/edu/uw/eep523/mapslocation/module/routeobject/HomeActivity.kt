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
import kotlin.collections.ArrayList


class HomeActivity : AppCompatActivity(),View.OnClickListener, OnItemClickListener {
    private lateinit var databaseHelper: RouteDatabaseAdapter
    private var routeList= ArrayList<RouteDataObject>()
    private lateinit var routeDataObjectAdapter : RouteDataObjectAdapter
    private var Groutefilename = ""
    private var GrouteDate= ""
    private var GrouteCategory= ""
    private var GrouteDistance= ""


    private var mNumLabels = 0

    fun Dates(numLabels: Int) {
        mNumLabels = numLabels
    }

    fun Dates() {
        mNumLabels = 3
    }



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

        launchGraph()
        showData()
    }


    private fun launchGraph(){

        val date:Date = Date(2020,3,1)
        val date2:Date = Date(2020,3,4)
        val date3:Date = Date(2020,3,7)

        val graph = findViewById(R.id.graph) as GraphView
        val series: LineGraphSeries<DataPoint> = LineGraphSeries<DataPoint>(
            arrayOf<DataPoint>(
                DataPoint(date, 1.0),
                DataPoint(date2, 5.0),
                DataPoint(date3, 3.0)
            )
        )

        val series2: LineGraphSeries<DataPoint> = LineGraphSeries<DataPoint>(
            arrayOf<DataPoint>(
                DataPoint(date, 9.0),
                DataPoint(date2, 6.0),
                DataPoint(date3, 7.0)
            )
        )
        graph.addSeries(series)

        graph.addSeries(series2)

        series.setTitle("Cycle")
        series2.setTitle("Run")
        series.color = Color.RED

        // styling legend
        graph.legendRenderer.isVisible = true
        graph.legendRenderer.textSize = 25f
        graph.legendRenderer.backgroundColor = Color.argb(150, 50, 0, 0)
        graph.legendRenderer.textColor = Color.WHITE
        graph.legendRenderer.setFixedPosition(500,195)
        //graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        //graph.getLegendRenderer().setMargin(30);

        // titles
        graph.title = "Distance by Day"
        graph.gridLabelRenderer.verticalAxisTitle = "km"
        graph.gridLabelRenderer.horizontalAxisTitle = "Date"
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
        getDataFromDb()
    }

    override fun itemClickListener(view: View, position: Int) {

    }




}
