/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.cis.dao;

import org.dspace.content.cis.Civilt;
import org.dspace.content.cis.DspaceToCisMapping;
import org.dspace.core.ContextCIS;

import java.sql.SQLException;
import java.util.List;

/**
 * Database Access Object interface class for the Bitstream object.
 * The implementation of this class is responsible for all database calls for the Bitstream object and is autowired
 * by spring
 * This class should only be accessed from a single service and should never be exposed outside of the API
 *
 * @author kevinvandevelde at atmire.com
 */
public interface CiviltDAO{
    public List<Civilt> findAll(ContextCIS context) throws SQLException;
    public List<Civilt> findAllnotMigratedToDMS(ContextCIS context,int maxResults) throws SQLException;
    public List<Civilt> findAllConnectedFromDspaceMapping(ContextCIS context, int maxResults) throws SQLException;
}
