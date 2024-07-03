/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao;

import org.dspace.content.Bitstream;
import org.dspace.content.DmsEvent;
import org.dspace.content.DocumentTypeTree;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Database Access Object interface class for the Item object.
 * The implementation of this class is responsible for all database calls for the Item object and is autowired by spring
 * This class should only be accessed from a single service and should never be exposed outside of the API
 *
 * @author kevinvandevelde at atmire.com
 */
public interface DmsEventDAO extends DSpaceObjectLegacySupportDAO<DmsEvent> {
    public DmsEvent create(Context context, DmsEvent dmsEvent) throws SQLException;
    public List<DmsEvent> findAll(Context context) throws SQLException;
    public List<DmsEvent> findAll(Context context, Integer limit, Integer offset) throws SQLException;
    public List<DmsEvent> findAllByCurrentDate(Context context, Integer limit, Integer offset, Date startDate, Date endDate, EPerson user,Integer action,String caseType,String CaseYear,String CaseNumber) throws SQLException;
    public int countfindAllByCurrentDate(Context context, Integer limit, Integer offset, Date startDate, Date endDate, EPerson user,Integer action,String caseType,String CaseYear,String CaseNumber) throws SQLException;
}
