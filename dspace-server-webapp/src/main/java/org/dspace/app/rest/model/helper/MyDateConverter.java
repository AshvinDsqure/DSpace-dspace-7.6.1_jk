package org.dspace.app.rest.model.helper;

import com.fasterxml.jackson.databind.util.StdConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyDateConverter extends StdConverter<String, Date> {
    @Override
    public Date convert(final String value) {
        System.out.println("receivedDate:::"+value);
        if (value == null || value.equals("Invalid date") || value.trim().length() == 0) {
            return null;
        }
        try {
            return new SimpleDateFormat("dd/MM/yyyy").parse(value);
        } catch (ParseException e) {
            throw new IllegalStateException("Unable to parse date", e);
        }
    }
}
