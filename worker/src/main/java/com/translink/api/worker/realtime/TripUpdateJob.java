package com.translink.api.worker.realtime;

import com.translink.api.worker.config.format.model.SpecializedTime;
import com.translink.api.worker.realtime.proto.GtfsRealtime;
import com.translink.api.worker.repository.BulkBatchProcessor;
import com.translink.api.worker.repository.model.embed.TripUpdate;
import com.translink.api.worker.repository.StopTimeRepository;
import com.translink.api.worker.repository.model.StopTime;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TripUpdateJob implements Job {
    @Value("${service.gtfs.url.trip-update}")
    private String tripUpdateUrl;

    private StopTimeRepository stopTimeRepository;
    private BulkBatchProcessor bulkBatchProcessor;

    @Autowired
    public void setBulkBatchProcessor(BulkBatchProcessor bulkBatchProcessor) {
        this.bulkBatchProcessor = bulkBatchProcessor;
    }

    @Autowired
    public void setStopTimeRepository(StopTimeRepository stopTimeRepository) {
        this.stopTimeRepository = stopTimeRepository;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        URL url;
        try {
            url = new URL(tripUpdateUrl);
        } catch (MalformedURLException e) {
            log.error("Malformed URL is set for Trip Update!");

            throw new JobExecutionException(e);
        }

        log.info("Executing TripUpdate job.");
        long timelapse = Instant.now().toEpochMilli();
        try(InputStream inputStream = url.openStream()) {
            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(inputStream);
            List<StopTime> stopTimes = feed.getEntityList().parallelStream()
                    .filter(GtfsRealtime.FeedEntity::hasTripUpdate)
                    .map(GtfsRealtime.FeedEntity::getTripUpdate)
                    .filter(tripUpdate -> tripUpdate.getTrip().hasTripId())
                    .filter(tripUpdate -> stopTimeRepository.existsByTripId(tripUpdate.getTrip().getTripId()))
                    .flatMap(tripUpdate -> tripUpdate.getStopTimeUpdateList().parallelStream()
                            .map(stopTimeUpdate -> {
                                StopTime stopTime = stopTimeRepository.findByTripIdAndSequence(tripUpdate.getTrip().getTripId(), stopTimeUpdate.getStopSequence());
                                if(stopTime == null) {
//                                    Best effort matching for the realtime data
//                                    if stopTime == null, it must mean realtime data is out of sync
                                    return null;
                                }

                                TripUpdate update = convert(stopTimeUpdate);
                                update.setTripRelationship(TripUpdate.TripRelationship.values()[tripUpdate.getTrip().getScheduleRelationship().getNumber()]);
                                update.setStopTimeId(stopTime.getId());

                                stopTime.setUpdate(update);

                                return stopTime;
                            })
                            .filter(Objects::nonNull)
                    )
                    .collect(Collectors.toList());

            if(!stopTimes.isEmpty()) {
                bulkBatchProcessor.bulkUpdate(stopTimes);
            }
        } catch (IOException e) {
            log.error("Trip Update Job failed!");
            log.error(e.getMessage(), e);

            throw new JobExecutionException(e, true);
        } finally {
            log.info("Finished TripUpdate Job for {}ms, next one at {}", Instant.now().toEpochMilli() - timelapse, context.getNextFireTime());
        }
    }

    private TripUpdate convert(GtfsRealtime.TripUpdate.StopTimeUpdate update) {
        TripUpdate tripUpdate = new TripUpdate();
        if(update.hasScheduleRelationship()) {
            tripUpdate.setScheduleRelationship(
                    TripUpdate.ScheduleRelationship.values()[update.getScheduleRelationship().getNumber()]
            );
        }

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        if(update.hasArrival()) {
            GtfsRealtime.TripUpdate.StopTimeEvent arrival = update.getArrival();
            String time = Instant.ofEpochSecond(arrival.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalTime()
                    .format(timeFormatter);

            TripUpdate.ExpectedTime expectedArrival = TripUpdate.ExpectedTime.builder()
                    .time(SpecializedTime.parse(time, timeFormatter))
                    .delay(arrival.getDelay())
                    .uncertainty(arrival.getUncertainty())
                    .build();

            tripUpdate.setArrival(expectedArrival);
        }

        if(update.hasDeparture()) {
            GtfsRealtime.TripUpdate.StopTimeEvent departure = update.getDeparture();
            String time = Instant.ofEpochSecond(departure.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalTime()
                    .format(timeFormatter);

            TripUpdate.ExpectedTime expectedDeparture = TripUpdate.ExpectedTime.builder()
                    .time(SpecializedTime.parse(time, timeFormatter))
                    .delay(departure.getDelay())
                    .uncertainty(departure.getUncertainty())
                    .build();

            tripUpdate.setDeparture(expectedDeparture);
        }

        return tripUpdate;
    }
}
