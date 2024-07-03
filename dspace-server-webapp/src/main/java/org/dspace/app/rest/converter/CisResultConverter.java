/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.model.CisResultsRest;
import org.springframework.stereotype.Component;

/**
 * This class' purpose is to create a SearchResultsRest object from the given parameters
 */
@Component
public class CisResultConverter {

    private static final Logger log = LogManager.getLogger();
    public CisResultsRest convert(String str) {
        CisResultsRest cisResultsRest = new CisResultsRest();
        cisResultsRest.setJsonStr(str);
        return cisResultsRest;
    }
}
