/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.mapper;

import com.example.model.ErrorMessage;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


/**
 *
 * @author maryam
 */

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    
    @Override
    public Response toResponse(Throwable exception) {
        // Log the actual exception for debugging (server-side only)
        exception.printStackTrace();
        
        ErrorMessage errorMessage = new ErrorMessage(
            "An unexpected internal server error occurred",
            500,
            "https://smartcampus.university.edu/api/docs/errors#500"
        );
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(errorMessage)
            .build();
    }
}

