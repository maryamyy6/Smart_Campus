/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.resource;

/**
 *
 * @author maryam
 */

import com.example.model.Room;
import com.example.exception.RoomNotEmptyException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;


@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {
    
    //  Public static storage
    public static ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, List<String>> roomSensors = new ConcurrentHashMap<>();

    @GET
    public Response getAllRooms() {
        List<Room> roomList = new ArrayList<>(rooms.values());
        return Response.ok(roomList).build();
    }

    @POST
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Room ID is required").build();
        }
        rooms.put(room.getId(), room);
        roomSensors.putIfAbsent(room.getId(), new ArrayList<>());
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity("Room not found").build();
        }
        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        
        List<String> sensors = roomSensors.get(roomId);
        if (sensors != null && !sensors.isEmpty()) {
            throw new RoomNotEmptyException("Cannot delete room with " + sensors.size() + " active sensor(s)");
        }
        
        rooms.remove(roomId);
        roomSensors.remove(roomId);
        return Response.noContent().build();
    }
    
    
    public static boolean roomExists(String roomId) {
        return rooms.containsKey(roomId);
    }
    
    public static void addSensorToRoom(String roomId, String sensorId) {
        roomSensors.computeIfAbsent(roomId, k -> new ArrayList<>()).add(sensorId);
    }
    
    public static void removeSensorFromRoom(String roomId, String sensorId) {
        List<String> sensors = roomSensors.get(roomId);
        if (sensors != null) {
            sensors.remove(sensorId);
        }
    }
}