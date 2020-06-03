package edu.uw.eep523.mapslocation.module.routeobject

import java.io.Serializable

class RouteDataObject : Serializable {
    var routeFilename = ""
    var routeCategory = ""
    var routeDate  = ""
    var id = -1
    var routeDistance  = 0.0
}