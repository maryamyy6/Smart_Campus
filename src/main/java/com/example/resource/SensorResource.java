/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.resource;



import com.example.model.Sensor;
import com.example.model.SensorReading;
import com.example.exception.LinkedResourceNotFoundException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {
    
    // PUBLIC STATIC STORAGE - accessible everywhere
    public static ConcurrentHashMap<String, Sensor> sensors = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    @GET
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> sensorList = new ArrayList<>(sensors.values());
        
        if (type != null && !type.isEmpty()) {
            sensorList = sensorList.stream()
                .filter(s -> s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
        }
        return Response.ok(sensorList).build();
    }

    @POST
    public Response createSensor(Sensor sensor) {
        // Verify room exists
        if (!RoomResource.rooms.containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException("Room with ID '" + sensor.getRoomId() + "' does not exist");
        }
        
        sensors.put(sensor.getId(), sensor);
        RoomResource.addSensorToRoom(sensor.getRoomId(), sensor.getId());
        sensorReadings.putIfAbsent(sensor.getId(), new ArrayList<>());
        
        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("Sensor not found").build();
        }
        return Response.ok(sensor).build();
    }
    
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        RoomResource.removeSensorFromRoom(sensor.getRoomId(), sensorId);
        sensors.remove(sensorId);
        sensorReadings.remove(sensorId);
        
        return Response.noContent().build();
    }

    // SUB-RESOURCE LOCATOR
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}