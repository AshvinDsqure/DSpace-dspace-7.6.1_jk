/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import org.apache.logging.log4j.Logger;
import org.dspace.content.Bitstream;
import org.dspace.content.Item;
import org.dspace.content.PdfAnnotation;
import org.dspace.content.dao.PdfAnnotationDAO;
import org.dspace.core.AbstractHibernateDSODAO;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.List;

/**
 * Hibernate implementation of the Database Access Object interface class for the Item object.
 * This class is responsible for all database calls for the Item object and is autowired by spring
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class PdfAnnotationDAOImpl extends AbstractHibernateDSODAO<PdfAnnotation> implements PdfAnnotationDAO {
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(PdfAnnotationDAOImpl.class);
    protected PdfAnnotationDAOImpl() {
        super();
    }

    @Override
    public List<PdfAnnotation> findAll(Context context) throws SQLException {
        Query query = createQuery(context, "FROM PdfAnnotation ORDER BY created_date");
        return query.getResultList();
    }
    @Override
    public List<PdfAnnotation> findAll(Context context, Integer limit, Integer offset) throws SQLException {
        Query query = createQuery(context, "FROM PdfAnnotation ORDER BY created_date");
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }
    @Override
    public PdfAnnotation getAnnotationByBitstreamID(Context context, Bitstream  bitstream, EPerson ePerson) throws SQLException {
        Query query = createQuery(context, "FROM PdfAnnotation as p where p.bitstream=:bitstream and p.ePerson =:ePerson  ORDER BY created_date");
        query.setParameter("bitstream", bitstream);
        query.setParameter("ePerson", ePerson);
        return singleResult(query);
    }
    @Override
    public int countRows(Context context) throws SQLException{
        return count(createQuery(context, "SELECT count(*) FROM DocumentType"));
    }
    @Override
    public PdfAnnotation getNoteByItemID(Context context, Item item, EPerson ePerson) throws SQLException {
        Query query = createQuery(context, "FROM PdfAnnotation as p where p.item=:item and p.ePerson =:ePerson  ORDER BY created_date");
        query.setParameter("item", item);
        query.setParameter("ePerson", ePerson);
        return singleResult(query);
    }
}
