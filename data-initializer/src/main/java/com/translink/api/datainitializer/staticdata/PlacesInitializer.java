package com.translink.api.datainitializer.staticdata;

import com.google.gson.Gson;
import com.google.maps.model.LatLng;
import com.translink.api.datainitializer.staticdata.service.PlacesService;
import com.translink.api.datainitializer.repository.LandmarkRepository;
import com.translink.api.datainitializer.repository.StopRepository;
import com.translink.api.datainitializer.repository.model.Landmark;
import com.translink.api.datainitializer.repository.model.Stop;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PlacesInitializer {
    @Value(value = "${refresh-data.places}")
    private boolean refreshData;

    @Value("classpath:places/stops.json")
    private Resource stops;

    private StopRepository stopRepository;
    private Gson gson;
    private PlacesService placesService;
    private LandmarkRepository landmarkRepository;

    @Autowired
    public void setLandmarkRepository(LandmarkRepository landmarkRepository) {
        this.landmarkRepository = landmarkRepository;
    }

    @Autowired
    public void setPlacesService(PlacesService placesService) {
        this.placesService = placesService;
    }

    @Autowired
    public void setGson(Gson gson) {
        this.gson = gson;
    }

    @Autowired
    public void setStopRepository(StopRepository stopRepository) {
        this.stopRepository = stopRepository;
    }

    @EventListener(ContextRefreshedEvent.class)
    @Order(2)
    public void populatePlacesAfterDatabase() {
        if(!refreshData) {
            log.info("Skip populating Places");

            return;
        }

        landmarkRepository.deleteAll();

        Map stopMap;
        try(Reader reader = new InputStreamReader(stops.getInputStream())) {
            stopMap = gson.fromJson(reader, Map.class);
        } catch(IOException e) {
            log.error("File cannot be read", e);
            return;
        }

        List<String> stopIds = (List<String>) stopMap.get("stops");
        stopIds.parallelStream()
                .forEach(stopId -> {
                    Stop stop = stopRepository.findById(stopId).get();

                    try {
                        List<Landmark> list = placesService.findNearbyPlaces(new LatLng(stop.getLatitude(), stop.getLongitude()))
                                .parallelStream()
                                .flatMap(response -> Arrays.stream(response.results)
                                        .parallel()
                                        .map(result -> {
                                            try {
                                                Landmark landmark = Landmark.builder()
                                                        .stopId(stop.getId())
                                                        .latitude(result.geometry.location.lat)
                                                        .longitude(result.geometry.location.lng)
                                                        .name(result.name)
                                                        .image(result.photos != null && result.photos.length > 0 ? placesService.getPhoto(result.photos[0].photoReference, result.photos[0].height, result.photos[0].width) : null)
                                                        .icon(String.valueOf(result.icon))
                                                        .url(placesService.getUrl(result.placeId))
                                                        .description(null)
                                                        .rating(result.rating)
                                                        .build();

                                                landmark.setDistance(
                                                        placesService.distance(
                                                                stop.getLatitude(), landmark.getLatitude(),
                                                                stop.getLongitude(), landmark.getLongitude()
                                                        )
                                                );

                                                return landmark;
                                            } catch (Exception e) {
                                                log.error(e.getMessage(), e);
                                                return null;
                                            }
                                        })
                                        .filter(Objects::nonNull))
                                .collect(Collectors.toList());

                        landmarkRepository.saveAll(list);
                        log.info("Populated stop {} with {} landmarks", stop.getId(), list.size());

                        if(stop.getChildStops() != null && !stop.getChildStops().isEmpty()) {
                            stop.getChildStops().parallelStream()
                                    .forEach(childStop -> {
                                        List<Landmark> childLandmarks = list.parallelStream()
                                                .map(Landmark::copy)
                                                .map(landmark -> {
                                                    landmark.setStopId(childStop.getId());

                                                    return landmark;
                                                })
                                                .collect(Collectors.toList());

                                        landmarkRepository.saveAll(childLandmarks);

                                        log.info("Populated stop {} with {} landmarks", childStop.getId(), childLandmarks.size());
                                    });
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                });
    }
}
