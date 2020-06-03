package edu.uw.eep523.mapslocation.module.routeobject

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.uw.eep523.mapslocation.OnItemClickListener
import edu.uw.eep523.mapslocation.R
import kotlinx.android.synthetic.main.route_row.view.*

class RouteDataObjectAdapter(data:ArrayList<RouteDataObject>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var context: Context
    private var routeList = data
    private lateinit var clickListener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.route_row,parent,false)
        return  RouteViewHolder(view)
    }

    override fun getItemCount(): Int {
       return routeList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is RouteViewHolder){
            val item= routeList[position]
            holder.routeFilenameTv.setText(item.routeFilename)
            holder.routeCategoryTv.setText(item.routeCategory)
            holder.routeDate.setText(item.routeDate)
            holder.routeDistance.setText(item.routeDistance)
        }
    }

    inner class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),View.OnClickListener{
        val routeFilenameTv = itemView.routeFilenameTv
        val  routeCategoryTv = itemView.routeCategoryTv
        val routeDate = itemView.routeDateTv
        val routeDistance = itemView.routeDistanceTv
        val container = itemView.rowContainer.setOnClickListener(this)
        override fun onClick(view: View?) {
            clickListener.itemClickListener(view!!,position)
        }
    }

    fun setOnClickListener(listener : OnItemClickListener){
        clickListener = listener
    }
}