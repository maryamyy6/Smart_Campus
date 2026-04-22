package com.example.model;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author maryam
 */


public class ErrorMessage {
    private String errorMessage;
    private int errorCode;
    private String documentation;
    
    public ErrorMessage() {}
    
    public ErrorMessage(String errorMessage, int errorCode, String documentation) {
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.documentation = documentation;
    }
    
    // Getters
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
    
    public String getDocumentation() {
        return documentation;
    }
    
    // Setters
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
    
    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }
} 