package com.translink.api.datainitializer.repository.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.translink.api.datainitializer.repository.model.embed.RouteType;
import com.translink.api.datainitializer.config.format.DepthSerializable;
import lombok.*;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Route implements DepthSerializable {
    @Id
    private String id;

    @NotBlank
    private String shortName;

    @NotBlank
    private String longName;

    @ToString.Exclude
    private String description;

    @NotNull
    private RouteType routeType;

    @NotBlank
    @URL
    private String routeUrl;

    @NotNull
    private String routeColor;

    @NotNull
    private String textColor;

    @DocumentReference(lazy = true)
    @ToString.Exclude
    @JsonIgnore
    private List<Trip> trips;

    @Override
    public ObjectNode toJson(int depth, ObjectMapper mapper, Class<?> originalClass) {
        ObjectNode node = mapper.convertValue(this, ObjectNode.class);

        if(depth > 1) {
            if(Route.class.equals(originalClass)) {
                ArrayNode tripsNode = mapper.createArrayNode();
                trips.stream()
                        .map(trip -> trip.toJson(depth-1, mapper, originalClass))
                        .forEach(tripsNode::add);

                node.set("trips", tripsNode);
            }
        }

        return node;
    }
}
