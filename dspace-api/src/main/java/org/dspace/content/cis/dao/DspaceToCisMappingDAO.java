/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.cis.dao;

import org.dspace.content.cis.DspaceToCisMapping;
import org.dspace.core.ContextCIS;

import java.sql.SQLException;

/**
 * Database Access Object interface class for the Bitstream object.
 * The implementation of this class is responsible for all database calls for the Bitstream object and is autowired
 * by spring
 * This class should only be accessed from a single service and should never be exposed outside of the API
 *
 * @author kevinvandevelde at atmire.com
 */
public interface DspaceToCisMappingDAO {
    public DspaceToCisMapping saveObject(ContextCIS context, DspaceToCisMapping dspaceToCisMapping) throws SQLException;
    public DspaceToCisMapping update(ContextCIS context, DspaceToCisMapping dspaceToCisMapping) throws SQLException;
    public DspaceToCisMapping findByCino(ContextCIS context, String cino) throws SQLException;


}
