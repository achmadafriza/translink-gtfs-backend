# QLD Bus Explorer

## Running the App

### Frontend

1. **Install** the dependencies to run Flutter on your machine [https://docs.flutter.dev/get-started/install](https://docs.flutter.dev/get-started/install)

2. Clone the **main** branch of the frontend repo at [https://github.com/Maccas-Sticky-Hot-BBQ-Sauce/maccas-hot-bbq-sauce-frontend.git](https://github.com/Maccas-Sticky-Hot-BBQ-Sauce/maccas-hot-bbq-sauce-frontend.git)

3. Run the Flutter app through your IDE or run `flutter run` or `flutter run -d web-server` on your terminal and choose **web** as the platform choice.

4. You may adjust the `stopId` url query of the website to switch between stops.

   ```http://localhost:####?stopId=94```

5. Currently the list of bus stops with landmarks available to be explored are: 10780, 10781, 10785, 10786, 10787, 10788, 10789, 10790, 10791, 19052, 19064, and 94.

### Backend

This project uses:
- `gtfs-realtime.proto` from [Google GTFS Specification](https://developers.google.com/transit/gtfs-realtime/) which is compiled into `GtfsRealtime` class.
- Static SEQ GTFS dataset on `./src/resources/static_gtfs/` from [Translink Open API](https://www.data.qld.gov.au/dataset/general-transit-feed-specification-gtfs-seq).

The live implementation can be seen via [Postman](https://www.postman.com/maccas-bbq-sauce/workspace/deco3801-maccas-sticky-hot-bbq-sauce).

For running the application you need to have this list of dependencies:
1. Docker
3. Google Maps Places API
4. Translink GTFS Realtime API

This application uses this list of environment variables to configure it's runtime:
- `MAPS_KEY` to specify Google Maps API key.

To run the application locally:
1. Build the application using `docker compose build`
2. Run the application using `docker compose up -d`.

To run the application on a swarm:
1. `docker swarm init`
2. `docker stack deploy -c docker-compose translink-gtfs`
 
Note: data-initializer uses some clever trick to tell docker compose when it is done initializing the data using healthcheck. You need to wait for a couple of minutes.

You can see the list of running services by going to `http://localhost:8761`.