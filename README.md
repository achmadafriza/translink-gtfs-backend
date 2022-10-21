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
 
Note: data-initializer uses some clever trick to tell docker compose when it is done initializing the data using healthcheck. You need to wait for a couple of minutes.

You can see the list of running services by going to `http://localhost:8761`.