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

    private lateinit var databaseHelper: RouteDatabaseAdapter


    companion object{


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_data)
        initView()
    }

    /**
     * This method is used to init the views and add clicklisteners
     */
    private fun initView() {

        saveBtn.setOnClickListener(this)
        databaseHelper = RouteDatabaseAdapter(this)
        titleTv.setText(getString(R.string.add_data_title))
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.saveBtn ->{
                        saveDataInDb()
            }
        }
    }

    /**
     * This method is used to save data in db
     */
    private fun saveDataInDb() {
        val name  =    "DIDWEDOIT"
        val routeDate  = "DIDWEDOIT"
        val routeCategory  = "DIDWEDOIT"
        val routeDistance  = "DIDWEDOIT"


        if(name.isNullOrEmpty() || routeDate.isNullOrEmpty() ||  routeDistance.isNullOrEmpty() ||routeCategory.isNullOrEmpty()){
            Message.message(this,"Please fill all the fields")
        }else{
            val id = databaseHelper.insertData(name,routeDate,routeCategory, routeDistance)
            if(id>0){
                Message.message(this,"Successfully inserted a row")
                finish()
            }else{
                Message.message(this,"Unsuccessful")
            }
        }

    }
}
