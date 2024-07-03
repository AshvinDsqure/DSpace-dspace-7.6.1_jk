/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.cis.service;

import org.dspace.content.cis.Civilt;
import org.dspace.content.cis.Civilta;
import org.dspace.core.ContextCIS;

import java.sql.SQLException;
import java.util.List;

/**
 * Service interface class for the Bitstream object.
 * The implementation of this class is responsible for all business logic calls for the Bitstream object and is
 * autowired by spring
 *
 * @author kevinvandevelde at atmire.com
 */
public interface CiviltService {


    public List<Civilt> findAll(ContextCIS context) throws SQLException;
   public List<Civilt> findAllnotMigratedToDMS(ContextCIS context,int maxResults) throws SQLException;
    public List<Civilt> findAllConnectedFromDspaceMapping(ContextCIS context, int maxResults) throws SQLException;

}
