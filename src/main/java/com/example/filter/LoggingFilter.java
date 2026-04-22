/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * @author maryam
 */


@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    
    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info("Incoming Request");
        LOGGER.info("   Method: " + requestContext.getMethod());
        LOGGER.info("   URI: " + requestContext.getUriInfo().getAbsolutePath());
        if (requestContext.getUriInfo().getQueryParameters().size() > 0) {
            LOGGER.info("   Query Params: " + requestContext.getUriInfo().getQueryParameters());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, 
                       ContainerResponseContext responseContext) throws IOException {
        LOGGER.info("Outgoing Response");
        LOGGER.info("   Status: " + responseContext.getStatus());
        LOGGER.info("   Method: " + requestContext.getMethod());
        LOGGER.info("   URI: " + requestContext.getUriInfo().getAbsolutePath());
       
    }
}