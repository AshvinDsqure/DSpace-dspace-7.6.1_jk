/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import org.apache.logging.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.*;
import org.dspace.content.dao.DocumentTypeDAO;
import org.dspace.content.dao.ItemDAO;
import org.dspace.core.AbstractHibernateDSODAO;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.hibernate.Criteria;
import org.hibernate.criterion.*;
import org.hibernate.type.StandardBasicTypes;

import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.sql.SQLException;
import java.util.*;

/**
 * Hibernate implementation of the Database Access Object interface class for the Item object.
 * This class is responsible for all database calls for the Item object and is autowired by spring
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class DocumentTypeDAOImpl extends AbstractHibernateDSODAO<DocumentType> implements DocumentTypeDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(DocumentTypeDAOImpl.class);
    protected DocumentTypeDAOImpl() {
        super();
    }



    @Override
    public List<DocumentType> findAll(Context context) throws SQLException {
        Query query = createQuery(context, "FROM DocumentType ORDER BY documenttypename");
        return query.getResultList();
    }
    @Override
    public List<DocumentType> findAll(Context context, Integer limit, Integer offset) throws SQLException {
        Query query = createQuery(context, "FROM DocumentType ORDER BY documenttypename");
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }
    @Override
    public int countRows(Context context) throws SQLException{
        return count(createQuery(context, "SELECT count(*) FROM DocumentType"));
    }
}
