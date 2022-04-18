package com.careerdevs.gorestmysql.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiErrorHandling {

    public static ResponseEntity<?> genericApiError(Exception e){
        System.out.println(e.getMessage());
        System.out.println(e.getClass());

        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    public static ResponseEntity<?> customApiError (String message,HttpStatus status){
        return new ResponseEntity<>(message,status);
    }


    public static boolean isStrNaN(String strNum){
        if(strNum == null){//if string is not a number
            return true;
        }
        try{
            Integer.parseInt(strNum);
        }catch(NumberFormatException e){
            return true;
        }
        return false;//string is a number
    }

}
