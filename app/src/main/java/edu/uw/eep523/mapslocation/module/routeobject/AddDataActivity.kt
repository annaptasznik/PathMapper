package edu.uw.eep523.mapslocation.module.routeobject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import edu.uw.eep523.mapslocation.Constants
import edu.uw.eep523.mapslocation.R
import edu.uw.eep523.mapslocation.RouteDatabaseAdapter
import edu.uw.eep523.mapslocation.Message
import kotlinx.android.synthetic.main.activity_add_data.*
import kotlinx.android.synthetic.main.toolbar.*

class AddDataActivity : AppCompatActivity(),View.OnClickListener{

    private var routeData: RouteDataObject? = null
    private var actionScreen: String? = ""
    private lateinit var databaseHelper: RouteDatabaseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_data)
        getDataFromIntent()
        initView()
    }

    /**
     * This method is used to init the views and add clicklisteners
     */
    private fun initView() {
        backIv.setOnClickListener(this)
        saveBtn.setOnClickListener(this)
        databaseHelper = RouteDatabaseAdapter(this)
        titleTv.setText(getString(R.string.add_data_title))
    }

    /**
     * This method is responsible to get data from intent
     */
    private fun getDataFromIntent() {
        if(intent.extras!!.containsKey(Constants.ACTION)){
            actionScreen =  intent.extras!!.getString(Constants.ACTION)
            Log.e("action_screen",actionScreen!!)
            if(actionScreen.equals(Constants.UPDATE_SCREEN)){
                routeData =  intent.getSerializableExtra(Constants.ROUTE_DATA) as? RouteDataObject
                setData(routeData!!)
            }
        }
    }

    /**
     * This method is responsible to set data in ui
     */
    private fun setData(routeData: RouteDataObject) {
        routeFilenameEt.setText(routeData.routeFilename)
        routeDateEt.setText(routeData.routeDate)
        routeCategoryEt.setText(routeData.routeCategory)
        routeDistanceEt.setText(routeData.routeDistance.toString())
        saveBtn.setText(getString(R.string.update))
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.saveBtn ->{
                if(actionScreen!!.equals(Constants.ADD_SCREEN)){
                    if(dataIsValid()){
                        saveDataInDb()
                    }
                }else{
                    if(dataIsValid())
                        updateInDb()
                }
            }
            R.id.backIv ->{
                finish()
            }
        }
    }

    private fun dataIsValid(): Boolean {
        val name  =    routeFilenameEt.text.toString()
        val routeDate  = routeDateEt.text.toString()
        val routeCategory  = routeCategoryEt.text.toString()
        val routeDistance  = routeDistanceEt.text.toString()
        if(name.isNullOrEmpty() || routeDate.isNullOrEmpty() ||  routeDistance.isNullOrEmpty() ||routeCategory.isNullOrEmpty()){
            Message.message(this,"Please fill all the fields")
            return false
        }else{
            return  true
        }
    }

    /**
     * This method is responsible to update data in db
     */
    private fun updateInDb() {
        val name  =    routeFilenameEt.text.toString()
        val routeDate  = routeDateEt.text.toString()
        val routeCategory  = routeCategoryEt.text.toString()
        val routeDistance = routeDistanceEt.text.toString().toDouble()
        val id =  databaseHelper.updateData(name,routeDate,routeCategory,routeData!!.id, routeDistance)
        if(id>0){
            Message.message(this,"Updated successfully")
            finish()
        }else{
            Message.message(this,"error while updating")
        }
    }

    /**
     * This method is used to save data in db
     */
    private fun saveDataInDb() {
        val name  =    routeFilenameEt.text.toString()
        val routeDate  = routeDateEt.text.toString()
        val routeCategory  = routeCategoryEt.text.toString()
        val routeDistance  = routeCategoryEt.text.toString()
        val id = databaseHelper.insertData(name,routeDate,routeCategory, routeDistance)
        if(id>0){
            Message.message(this,"successfully inserted a row")
            finish()
        }else{
            Message.message(this,"Unsuccessfull")
        }
    }
}
