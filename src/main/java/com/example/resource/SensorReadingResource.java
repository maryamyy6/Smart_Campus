package com.example.resource;

import com.example.model.Sensor;
import com.example.model.SensorReading;
import com.example.exception.SensorUnavailableException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {
    
    private String sensorId;
    
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        // Direct access to SensorResource's static map
        Sensor sensor = SensorResource.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"Sensor not found\"}")
                .build();
        }
        
        List<SensorReading> readings = SensorResource.sensorReadings.get(sensorId);
        if (readings == null) {
            readings = new ArrayList<>();
        }
        return Response.ok(readings).build();
    }

    @POST
    public Response addReading(SensorReading reading) {
        // Direct access to SensorResource's static maps
        Sensor sensor = SensorResource.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\":\"Sensor not found\"}")
                .build();
        }
        
        // Check if sensor is in maintenance
        if ("MAINTENANCE".equals(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor is in MAINTENANCE mode - cannot accept new readings");
        }
        
        // If needed generate ID
        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        
        // If needed set timestamp
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }
        
        // Store reading
        List<SensorReading> readings = SensorResource.sensorReadings.get(sensorId);
        if (readings == null) {
            readings = new ArrayList<>();
            SensorResource.sensorReadings.put(sensorId, readings);
        }
        readings.add(reading);
        
        // Update sensor's current value
        sensor.setCurrentValue(reading.getValue());
        SensorResource.sensors.put(sensorId, sensor);
        
        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}