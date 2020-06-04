package edu.uw.eep523.mapslocation

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import edu.uw.eep523.mapslocation.module.routeobject.RouteDataObject

class RouteDatabaseAdapter(context: Context){
    companion object{
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "RouteDatabase"
        private val TABLE_NAME = "RouteTable"
        private val KEY_ID = "id"
        private val KEY_ROUTENAME = "routeFilename"
        private val KEY_ROUTEDATE = "routeDate"
        private  val KEY_ROUTECATEGORY = "routeCategory"
        private val KEY_ROUTEDISTANCE = "routeDistance"
    }
    private val mContext = context
    private val databaseHelper = DatabaseHelper(mContext)
    private val routeList= ArrayList<RouteDataObject>()

    fun insertData(routeFilename:String, routeDate: String, routeCategory: String, routeDistance: String): Long {
        val db = databaseHelper.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_ROUTENAME, routeFilename)
        contentValues.put(KEY_ROUTEDATE,routeDate)
        contentValues.put(KEY_ROUTECATEGORY, routeCategory)
        contentValues.put(KEY_ROUTEDISTANCE, routeDistance)
        val id = db.insert(TABLE_NAME,null,contentValues)
        return id
    }

    fun updateData(routeFilename: String,routeDate: String,routeCategory: String,id: Int, routeDistance: Double): Int {
        val db = databaseHelper.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_ROUTENAME, routeFilename)
        contentValues.put(KEY_ROUTEDATE,routeDate)
        contentValues.put(KEY_ROUTECATEGORY, routeCategory)
        contentValues.put(KEY_ROUTEDISTANCE, routeDistance)
        val WhereArgs = arrayOf<String>(id.toString())
        val updatedRows = db.update(TABLE_NAME,contentValues, KEY_ID+" =?" ,WhereArgs)
        return updatedRows
    }

    fun getAllData(): ArrayList<RouteDataObject> {
        routeList.clear()
        val db =  databaseHelper.writableDatabase
        val columns = arrayOf<String>(KEY_ID,KEY_ROUTENAME, KEY_ROUTEDATE, KEY_ROUTECATEGORY, KEY_ROUTEDISTANCE)
        val cursor: Cursor = db.query(TABLE_NAME,columns,null,null,null,null,null)
        while (cursor.moveToNext()){
            val routeDataObject = RouteDataObject()
            val index1 =  cursor.getColumnIndex(KEY_ROUTENAME)
            val index2 =  cursor.getColumnIndex(KEY_ROUTEDATE)
            val index3 =  cursor.getColumnIndex(KEY_ROUTECATEGORY)
            val index4 =  cursor.getColumnIndex(KEY_ID)
            val index5 =  cursor.getColumnIndex(KEY_ROUTEDISTANCE)
            routeDataObject.routeFilename = cursor.getString(index1)
            routeDataObject.routeDate  = cursor.getString(index2)
            routeDataObject.routeCategory = cursor.getString(index3)
            routeDataObject.id = cursor.getInt(index4)
            routeDataObject.routeDistance = cursor.getString(index5)
            routeList.add(routeDataObject)
        }
        return routeList
    }

    class DatabaseHelper(context: Context): SQLiteOpenHelper(context,DATABASE_NAME,null,DATABASE_VERSION) {

        private  val mContext = context
        override fun onCreate(db: SQLiteDatabase?) {
            val querry = "CREATE TABLE "+TABLE_NAME+" ("+ KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+KEY_ROUTENAME+" VARCHAR(255), "+ KEY_ROUTEDATE+" TEXT, " + KEY_ROUTECATEGORY+" TEXT," + KEY_ROUTEDISTANCE+" DOUBLE);"
            db?.execSQL(querry)
            if(db!=null){
               // Message.message(mContext,"oncreate db success" )
            }
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            db!!.execSQL("DROP TABLE IF EXISTS" + TABLE_NAME)
            onCreate(db)
        }

    }

}