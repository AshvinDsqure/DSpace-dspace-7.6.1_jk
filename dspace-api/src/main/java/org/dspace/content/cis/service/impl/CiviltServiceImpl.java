/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.cis.service.impl;

import org.dspace.content.cis.Civilt;
import org.dspace.content.cis.Civilta;
import org.dspace.content.cis.dao.CiviltDAO;
import org.dspace.content.cis.service.CiviltService;
import org.dspace.core.ContextCIS;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.List;

/**
 * Service interface class for the Bitstream object.
 * The implementation of this class is responsible for all business logic calls for the Bitstream object and is
 * autowired by spring
 *
 * @author kevinvandevelde at atmire.com
 */
public class CiviltServiceImpl implements CiviltService {
    @Autowired
    CiviltDAO civiltDAO;

    @Override
    public List<Civilt> findAll(ContextCIS context) throws SQLException {
        return civiltDAO.findAll(context);
    }

    @Override
    public List<Civilt> findAllnotMigratedToDMS(ContextCIS context, int maxResults) throws SQLException{
        return civiltDAO.findAllnotMigratedToDMS(context,maxResults);
    }
    @Override
    public List<Civilt> findAllConnectedFromDspaceMapping(ContextCIS context, int maxResults) throws SQLException {
        return civiltDAO.findAllConnectedFromDspaceMapping(context,maxResults);
    }
}
