/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import org.apache.logging.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.dao.DmsEventDAO;
import org.dspace.content.dao.DocumentTypeDAO;
import org.dspace.content.service.DmsEventService;
import org.dspace.content.service.DocumentTypeService;
import org.dspace.core.Context;
import org.dspace.core.LogHelper;
import org.dspace.eperson.EPerson;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service implementation for the Item object.
 * This class is responsible for all business logic calls for the Item object and is autowired by spring.
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class DmsEventServiceImpl extends DSpaceObjectServiceImpl<DmsEvent> implements DmsEventService {

    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(Item.class);

    @Autowired(required = true)
    protected DmsEventDAO dmsEventDAO;
    protected DmsEventServiceImpl() {
        super();
    }

    @Override
    public DmsEvent create(Context context, DmsEvent dmsEvent) throws SQLException {
        return dmsEventDAO.create(context,dmsEvent);
    }
    @Override
    public DmsEvent create(Context context) throws SQLException, AuthorizeException {
        return dmsEventDAO.create(context,new DmsEvent());
    }


    @Override
    public List<DmsEvent> findAll(Context context) throws SQLException {
        return dmsEventDAO.findAll(context);
    }

    @Override
    public List<DmsEvent> findAll(Context context, Integer limit, Integer offset) throws SQLException {
        return dmsEventDAO.findAll(context,limit,offset);
    }

    @Override
    public List<DmsEvent> findAllByCurrentDate(Context context, Integer limit, Integer offset, Date startDate, Date endDate, EPerson user,Integer action,String caseType,String caseYear,String caseNumber) throws SQLException{
        return dmsEventDAO.findAllByCurrentDate(context,limit,offset,startDate,endDate,user,action,caseType,caseYear,caseNumber);
    }

    @Override
    public int countfindAllByCurrentDate(Context context, Integer limit, Integer offset, Date startDate, Date endDate, EPerson user,Integer action,String caseType,String caseYear,String caseNumber) throws SQLException {
        return dmsEventDAO.countfindAllByCurrentDate(context,limit,offset,startDate,endDate,user,action,caseType,caseYear,caseNumber);
    }

    @Override
    public DmsEvent find(Context context, UUID uuid) throws SQLException {
        return dmsEventDAO.findByID(context,DmsEvent.class,uuid);
    }

    @Override
    public void updateLastModified(Context context, DmsEvent dso) throws SQLException, AuthorizeException {

    }

    @Override
    public void delete(Context context, DmsEvent dso) throws SQLException {

    }

    @Override
    public DmsEvent findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }

    @Override
    public DmsEvent findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }

    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }
}
