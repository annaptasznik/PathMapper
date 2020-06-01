package edu.uw.eep523.mapslocation.module.employee

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import edu.uw.eep523.mapslocation.Constants
import edu.uw.eep523.mapslocation.R
import edu.uw.eep523.mapslocation.EmployeeDatabaseAdapter
import edu.uw.eep523.mapslocation.Message
import kotlinx.android.synthetic.main.activity_add_data.*
import kotlinx.android.synthetic.main.toolbar.*

class AddDataActivity : AppCompatActivity(),View.OnClickListener{

    private var employeeData: EmployeeBean? = null
    private var actionScreen: String? = ""
    private lateinit var databaseHelper: EmployeeDatabaseAdapter

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
        databaseHelper = EmployeeDatabaseAdapter(this)
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
                employeeData =  intent.getSerializableExtra(Constants.EMPLOYEE_DATA) as? EmployeeBean
                setData(employeeData!!)
            }
        }
    }

    /**
     * This method is responsible to set data in ui
     */
    private fun setData(employeeData: EmployeeBean) {
        routeFilenameEt.setText(employeeData.routeFilename)
        routeDateEt.setText(employeeData.routeDate)
        routeCategoryEt.setText(employeeData.routeCategory)
        routeDistanceEt.setText(employeeData.routeDistance.toString())
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
        val id =  databaseHelper.updateData(name,routeDate,routeCategory,employeeData!!.id, routeDistance)
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
