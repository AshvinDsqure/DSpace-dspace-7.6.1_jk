/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.security;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class RequestResponseInterceptor implements HandlerInterceptor {

//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
//        System.out.println("Request URL: " + request.getRequestURL());
//        System.out.println("Request Method: " + request.getMethod());
//        System.out.println("Request URI: " + request.getRequestURI());
//        System.out.println("Request Protocol: " + request.getProtocol());
//        System.out.println("Request Remote Address: " + request.getRemoteAddr());
//
//
//        System.out.println("Request Headers:");
//        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
//            System.out.println(headerName + " : " + request.getHeader(headerName));
//        });
//
//        StringBuilder requestData = new StringBuilder();
//
//        // Print request method and URI
//        requestData.append("Method: ").append(request.getMethod()).append("\n");
//        requestData.append("URI: ").append(request.getRequestURI()).append("\n");
//
//        // Print request headers
//        requestData.append("Headers:\n");
//        request.getHeaderNames().asIterator()
//                .forEachRemaining(headerName ->
//                        requestData.append(headerName).append(": ")
//                                .append(request.getHeader(headerName)).append("\n"));
//
//        // Print request parameters
//        requestData.append("Parameters:\n");
//        request.getParameterMap().forEach((param, values) -> {
//            requestData.append(param).append(": ");
//            for (String value : values) {
//                requestData.append(value).append(", ");
//            }
//            requestData.append("\n");
//        });
//
//        System.out.println("requestData.toString(); :\t"+requestData.toString());
//
//        //response.setHeader("DSPACE-XSRF-TOKEN", "sdss"+new Date().toString());
//        return true; // Continue with the request
//    }

//    @Override
//    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
//                           ModelAndView modelAndView) {
//        // Capture and save response data
//        int status = response.getStatus();
//        System.out.println("Response Status: " + status);
//        // You can save the status to a database or log file
//    }
}