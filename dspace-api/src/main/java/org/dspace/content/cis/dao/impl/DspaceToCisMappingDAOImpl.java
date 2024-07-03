/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.content.cis.dao.impl;

import org.dspace.content.cis.DspaceToCisMapping;
import org.dspace.content.cis.SubnatureOnet;
import org.dspace.content.cis.dao.DspaceToCisMappingDAO;
import org.dspace.core.AbstractHibernateDSOCISDAO;
import org.dspace.core.ContextCIS;

import javax.persistence.Query;
import java.sql.SQLException;

/**
 * Hibernate implementation of the Database Access Object interface class for the Bitstream object.
 * This class is responsible for all database calls for the Bitstream object and is autowired by spring
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class DspaceToCisMappingDAOImpl extends AbstractHibernateDSOCISDAO<DspaceToCisMapping> implements DspaceToCisMappingDAO {

    protected DspaceToCisMappingDAOImpl() {
        super();
    }

    @Override
    public DspaceToCisMapping saveObject(ContextCIS context, DspaceToCisMapping dspaceToCisMapping) throws SQLException {
        return create(context,dspaceToCisMapping);
    }

    @Override
    public DspaceToCisMapping update(ContextCIS context, DspaceToCisMapping dspaceToCisMapping) throws SQLException {
        return create(context,dspaceToCisMapping);
    }

    @Override
    public DspaceToCisMapping findByCino(ContextCIS context, String cino) throws SQLException {
        Query query = createQuery(context, "FROM DspaceToCisMapping WHERE cino=:cino");
        query.setParameter("cino", cino);
        return (DspaceToCisMapping)query.getSingleResult();
    }
}
