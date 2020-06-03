package edu.uw.eep523.mapslocation.module.routeobject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import edu.uw.eep523.mapslocation.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.toolbar.*

class HomeActivity : AppCompatActivity(),View.OnClickListener, OnItemClickListener {
    private lateinit var databaseHelper: RouteDatabaseAdapter
    private var routeList= ArrayList<RouteDataObject>()
    private lateinit var routeDataObjectAdapter : RouteDataObjectAdapter
    private var Groutefilename = ""
    private var GrouteDate= ""
    private var GrouteCategory= ""
    private var GrouteDistance= ""


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
            RDO.routeDistance = routeDistance.toDouble()
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
        backIv.setImageResource(R.drawable.menu_icon)
        titleTv.setText(getString(R.string.route_title))
        setClickListener()
        databaseHelper = RouteDatabaseAdapter(this)
        routeDataObjectAdapter = RouteDataObjectAdapter(routeList)
        routeDataObjectAdapter.setOnClickListener(this)
        routeRecylerview.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        routeRecylerview.adapter = routeDataObjectAdapter

        ///
        if(Groutefilename != ""){
            val id =databaseHelper.insertData(Groutefilename,GrouteDate, GrouteCategory, GrouteDistance)
            goToAddDataActivity(-1)
        }
        ///

    }

    fun startLayers(v: View){
        val intent = Intent(this, MapsLayersActivity::class.java).apply {}
        startActivity(intent)
    }

    private fun setClickListener() {
        //backIv.setOnClickListener(this)
        SaveToDatabase.setOnClickListener(this)
    }

    /**
     * This method is used to get data from db
     */
    private fun getDataFromDb() {
        routeList.clear()
        routeList.addAll( databaseHelper.getAllData())
        showData()
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
        when(view?.id){
            R.id.SaveToDatabase ->{
                goToAddDataActivity(-1)
            }

        }

    }



    /**
     * This method is used to navigate to Add Data activity
     */
    private fun goToAddDataActivity(position: Int) {
        val intent = Intent(this,AddDataActivity::class.java)
        if(position == -1){
            intent.putExtra(Constants.ACTION,Constants.ADD_SCREEN)
        }else{
            intent.putExtra(Constants.ACTION,Constants.UPDATE_SCREEN)
            intent.putExtra(Constants.ROUTE_DATA,routeList.get(position))
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        getDataFromDb()
    }

    override fun itemClickListener(view: View, position: Int) {
       goToAddDataActivity(position)
    }




}
