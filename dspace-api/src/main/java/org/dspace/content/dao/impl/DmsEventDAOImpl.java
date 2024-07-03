/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import org.apache.logging.log4j.Logger;
import org.dspace.content.DmsEvent;
import org.dspace.content.DocumentType;
import org.dspace.content.dao.DmsEventDAO;
import org.dspace.content.dao.DocumentTypeDAO;
import org.dspace.core.AbstractHibernateDSODAO;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Hibernate implementation of the Database Access Object interface class for the Item object.
 * This class is responsible for all database calls for the Item object and is autowired by spring
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class DmsEventDAOImpl extends AbstractHibernateDSODAO<DmsEvent> implements DmsEventDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(DmsEventDAOImpl.class);

    protected DmsEventDAOImpl() {
        super();
    }

    @Override
    public List<DmsEvent> findAll(Context context) throws SQLException {
        Query query = createQuery(context, "FROM DmsEvent ORDER BY actionDate");
        return query.getResultList();
    }

    @Override
    public List<DmsEvent> findAll(Context context, Integer limit, Integer offset) throws SQLException {
        Query query = createQuery(context, "FROM DmsEvent ORDER BY actionDate");
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public List<DmsEvent> findAllByCurrentDate(Context context, Integer limit, Integer offset, Date startDate, Date endDate, EPerson user,Integer action,String caseType,String CaseYear,String CaseNumber) throws SQLException{
        StringBuilder quertStringBuilder = new StringBuilder("FROM DmsEvent");// joing ePerson   as e where   actionDate BETWEEN :stDate AND :edDate ");
        if(caseType != null && CaseYear != null && CaseNumber != null){
            quertStringBuilder.append(" join item as i ");
            quertStringBuilder.append(" join metadatavalue as m  and m.metadataField =:m_metadataField and m.value=:mvalue ");
            quertStringBuilder.append(" join metadatavalue as mtype and mtype.metadataField =:mtype_metadataField and mtype.value=:mtypevalue");
            quertStringBuilder.append(" join metadatavalue as mnumber and mnumber.metadataField =:mnumber_metadataField and mnumber.value=:mnumbervalue");

        }
        if (startDate != null && endDate != null) {
            quertStringBuilder.append(" where   actionDate BETWEEN :stDate AND :edDate ");
        }
        if (user != null) {
            quertStringBuilder.append(" and ePerson =:epersonid ");
        }
        if(action != null){
            quertStringBuilder.append(" and action = :action ");
        }
        quertStringBuilder.append("  ORDER BY actionDate desc");
        System.out.println("Query::::" + quertStringBuilder.toString());
        Query query = createQuery(context, quertStringBuilder.toString());
        if (startDate != null && endDate != null) {
            query.setParameter("stDate", startDate);
            query.setParameter("edDate", endDate);
        }
        if (user != null) {
            query.setParameter("epersonid", user);
        }
        if (action != null) {
            query.setParameter("action", action);
        }
        if(caseType != null && CaseYear != null && CaseNumber != null) {
            query.setParameter("m_metadataField", 295);
            query.setParameter("mvalue", CaseYear);

            query.setParameter("mtype_metadataField", 295);
            query.setParameter("mtypevalue", caseType);

            query.setParameter("mnumber_metadataField", 295);
            query.setParameter("mnumbervalue", CaseNumber);
        }
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }
    @Override
    public int countfindAllByCurrentDate(Context context, Integer limit, Integer offset, Date startDate, Date endDate, EPerson user,Integer action,String caseType,String CaseYear,String CaseNumber) throws SQLException {
        StringBuilder quertStringBuilder = new StringBuilder("SELECT count(*)  FROM DmsEvent");// joing ePerson   as e where   actionDate BETWEEN :stDate AND :edDate ");
        System.out.println("user::::::"+user);
        if(caseType != null && CaseYear != null && CaseNumber != null){
            quertStringBuilder.append(" join item as i ");
            quertStringBuilder.append(" join metadatavalue as m  and m.metadataField =:m_metadataField and m.value=:mvalue ");
            quertStringBuilder.append(" join metadatavalue as mtype and mtype.metadataField =:mtype_metadataField and mtype.value=:mtypevalue");
            quertStringBuilder.append(" join metadatavalue as mnumber and mnumber.metadataField =:mnumber_metadataField and mnumber.value=:mnumbervalue");

        }
        System.out.println("startDate::::::"+startDate);
        System.out.println("endDate::::::"+endDate);
        if (startDate != null && endDate != null) {
            quertStringBuilder.append(" where   actionDate BETWEEN :stDate AND :edDate ");
        }
        if (user != null) {
            quertStringBuilder.append(" and ePerson =:epersonid ");
        }
        if(action != null){
            quertStringBuilder.append(" and action = :action ");
        }
        //if(case)
        //quertStringBuilder.append("  ORDER BY actionDate");
        System.out.println("Query::::" + quertStringBuilder.toString());
        Query query = createQuery(context, quertStringBuilder.toString());
        if (startDate != null && endDate != null) {
            query.setParameter("stDate", startDate);
            query.setParameter("edDate", endDate);
        }
        if (user != null) {
            query.setParameter("epersonid", user);
        }
        if (action != null) {
            query.setParameter("action", action);
        }
        if(caseType != null && CaseYear != null && CaseNumber != null) {
            query.setParameter("m_metadataField", 295);
            query.setParameter("mvalue", CaseYear);

            query.setParameter("mtype_metadataField", 295);
            query.setParameter("mtypevalue", caseType);

            query.setParameter("mnumber_metadataField", 295);
            query.setParameter("mnumbervalue", CaseNumber);
        }
        return count(query);
    }
}
