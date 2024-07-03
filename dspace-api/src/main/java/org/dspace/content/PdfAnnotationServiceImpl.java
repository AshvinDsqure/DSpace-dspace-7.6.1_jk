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
import org.dspace.content.dao.PdfAnnotationDAO;
import org.dspace.content.service.PdfAnnotationService;
import org.dspace.core.Context;
import org.dspace.core.LogHelper;
import org.dspace.eperson.EPerson;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Service implementation for the Item object.
 * This class is responsible for all business logic calls for the Item object and is autowired by spring.
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class PdfAnnotationServiceImpl extends DSpaceObjectServiceImpl<PdfAnnotation> implements PdfAnnotationService {

    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(PdfAnnotationServiceImpl.class);

    @Autowired(required = true)
    protected PdfAnnotationDAO pdfAnnotationDAO;
    @Autowired(required = true)
    protected AuthorizeService authorizeService;
    protected PdfAnnotationServiceImpl() {
        super();
    }

    @Override
    public PdfAnnotation findByIdOrLegacyId(Context context, String id) throws SQLException {
        return null;
    }

    @Override
    public PdfAnnotation findByLegacyId(Context context, int id) throws SQLException {
        return null;
    }

    @Override
    public PdfAnnotation find(Context context, UUID uuid) throws SQLException {
        return pdfAnnotationDAO.findByID(context,PdfAnnotation.class,uuid);
    }

    @Override
    public void updateLastModified(Context context, PdfAnnotation dso) throws SQLException, AuthorizeException {

    }

    @Override
    public void delete(Context context, PdfAnnotation dso) throws SQLException, AuthorizeException, IOException {
        pdfAnnotationDAO.delete(context,dso);
    }

    @Override
    public int getSupportsTypeConstant() {
        return 0;
    }

    @Override
    public PdfAnnotation create(Context context, PdfAnnotation dso) throws SQLException, AuthorizeException {
        return pdfAnnotationDAO.create(context,dso);
    }

    @Override
    public PdfAnnotation create(Context context) throws SQLException, AuthorizeException {
        PdfAnnotation e = (PdfAnnotation)this.pdfAnnotationDAO.create(context, new PdfAnnotation());
        return e;
    }

    @Override
    public List<PdfAnnotation> findAll(Context context) throws SQLException {
        return null;
    }

    @Override
    public List<PdfAnnotation> findAll(Context context, Integer limit, Integer offset) throws SQLException {
        return pdfAnnotationDAO.findAll(context,limit,offset);
    }

    public void update(Context context, PdfAnnotation pdfAnnotation) throws SQLException, AuthorizeException {
        log.info(LogHelper.getHeader(context, "update_bundle", "bundle_id=" + pdfAnnotation.getID()));
        super.update(context, pdfAnnotation);
        this.pdfAnnotationDAO.save(context, pdfAnnotation);
    }
    @Override
    public int countRows(Context context) throws SQLException{
        return this.pdfAnnotationDAO.countRows(context);
    }

    @Override
    public PdfAnnotation getAnnotationByBitstreamID(Context context, Bitstream  bitstream, EPerson ePerson) throws SQLException {
        return this.pdfAnnotationDAO.getAnnotationByBitstreamID(context,bitstream,ePerson);
    }
    @Override
    public PdfAnnotation getNoteByItemID(Context context, Item item, EPerson ePerson) throws SQLException {
        return this.pdfAnnotationDAO.getNoteByItemID(context,item,ePerson);

    }
}
