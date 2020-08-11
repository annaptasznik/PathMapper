# PathMapper

## Main Functionality
PathMapper is an app designed for athletes who want to track runs, bicycle rides, hikes, or other activities that occur over large distances.
Most simply, the user can (1) record a path, (2) save distance information about their path, and (3) view all information from previous paths in a single interface.

## App Structure
PathMapper is designed with two major windows—the home screen and the map view. In the home screen, users can view all saved paths and view a chart summarizing them. In the map view, users can interact with a live map and record & save a new path.

![Home Screen and Map View Image](https://github.com/annaptasznik/PathMapper/tree/master/project_images/home_and_main.png)

When a user stops recording, they have an option to Save or Cancel. When saving the route, the user inputs a filename and category for their event. They may also dynamically add new event categories.


## Sensors and Data Management
The phone’s GPS is the primary sensor being used in the PathMapper app. When the user chooses to begin a recording, a LocationRequest object and callback is initiated. The callback reports the user’s latitude, longitude, and the time at a constant interval.

![Path Recording Image](https://github.com/annaptasznik/PathMapper/tree/master/project_images/path_progress.png)

All paths and associated data are stored in a RouteDataObject and pushed to a local SQLite database. These items are displayed in a list on the home page and aggregated in a chart showing distance events by date.

![DB of Paths Image](https://github.com/annaptasznik/PathMapper/tree/master/project_images/path_db.png)