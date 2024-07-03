/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.dspace.app.rest.Parameter;
import org.dspace.app.rest.SearchRestMethod;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.PdfAnnotationRest;
import org.dspace.app.rest.model.patch.Patch;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.PdfAnnotation;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.PdfAnnotationService;
import org.dspace.core.Context;
import org.dspace.eperson.service.RegistrationDataService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * This is the repository responsible to manage Item Rest object
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */

@Component(PdfAnnotationRest.CATEGORY + "." + PdfAnnotationRest.NAME)
public class PdfAnnotationRestRepository extends DSpaceObjectRestRepository<PdfAnnotation, PdfAnnotationRest> implements InitializingBean {
    @Autowired
    PdfAnnotationService pdfAnnotationService;
    @Autowired
    private RegistrationDataService registrationDataService;
    @Autowired
    private BitstreamService bitstreamService;
    //@Autowired
    //PdfAnnotationConverter pdfAnnotationConverter;

    public PdfAnnotationRestRepository(PdfAnnotationService pdfAnnotationService) {
        super(pdfAnnotationService);
    }

    @Override
    public PdfAnnotationRest findOne(Context context, UUID uuid) {
        PdfAnnotation pdfAnnotation = null;
        try {
            pdfAnnotation = pdfAnnotationService.find(context, uuid);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        if (pdfAnnotation == null) {
            return null;
        }
        return converter.toRest(pdfAnnotation, utils.obtainProjection());
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<PdfAnnotationRest> findAll(Context context, Pageable pageable) {
        try {
            // long total = //documentTypeService.(context);
            long total=pdfAnnotationService.countRows(context);
            List<PdfAnnotation> pdfAnnotations= pdfAnnotationService.findAll(context, pageable.getPageSize(),Math.toIntExact(pageable.getOffset()));
            return converter.toRestPage(pdfAnnotations, pageable, total, utils.obtainProjection());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Class<PdfAnnotationRest> getDomainClass() {
        return null;
    }

    /**
     * This method will perform checks on whether or not the given Request was valid for the creation of an EPerson
     * with a token or not.
     * It'll check that the token exists, that the token doesn't yet resolve to an actual eperson already,
     * that the email in the given json is equal to the email for the token and that other properties are set to
     * what we expect in this creation.
     * It'll check if all of those constraints hold true and if we're allowed to register new accounts.
     * If this is the case, we'll create an EPerson without any authorization checks and delete the token
     * @param context       The DSpace context
     * @throws AuthorizeException   If something goes wrong
     * @throws SQLException         If something goes wrong
     */
    @Override
    protected PdfAnnotationRest createAndReturn(Context context)
            throws AuthorizeException, SQLException {
        // this need to be revisited we should receive an EPersonRest as input
        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ObjectMapper mapper = new ObjectMapper();
        System.out.println("createAndReturn::::::::::");
        PdfAnnotationRest pdfAnnotationRest = null;
        try {
            pdfAnnotationRest = mapper.readValue(req.getInputStream(), PdfAnnotationRest.class);
        } catch (IOException e1) {
            e1.printStackTrace();
            throw new UnprocessableEntityException("error parsing the body... maybe this is not the right error code");
        }
        PdfAnnotation pdfAnnotation= null;
        try {
            System.out.println("context.getCurrentUser():::"+context.getCurrentUser().getEmail());
            // If no token is present, we simply do the admin execution
            Bitstream bitstream = bitstreamService.find(context, UUID.fromString(pdfAnnotationRest.getBitstreamRest().getUuid()));
            pdfAnnotation = pdfAnnotationService.getAnnotationByBitstreamID(context,bitstream ,context.getCurrentUser());
            if(pdfAnnotation == null) {
                pdfAnnotation = createRestObject(context, pdfAnnotationRest);
            }else{
                if(pdfAnnotationRest.getPdfannotationStr()!=null){
                    pdfAnnotation.setAnnotationStr(pdfAnnotationRest.getPdfannotationStr());
                }
                if(pdfAnnotationRest.getNoteannotatiostr()!=null){
                    pdfAnnotation.setNoteannotatiostr(pdfAnnotationRest.getNoteannotatiostr());
                } pdfAnnotationService.update(context,pdfAnnotation);            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return converter.toRest(pdfAnnotation, utils.obtainProjection());
    }
    @SearchRestMethod(name = "getAnnotationByBitstreamID")
    public PdfAnnotationRest getAnnotationByBitstreamID(@Parameter(value = "bitstreamID", required = false) UUID bitstreamID ) throws AuthorizeException {
        PdfAnnotation pdfAnnotation=null;
        try {
            Context context = obtainContext();
            if(context.getCurrentUser() != null) {
                Bitstream bitstream = bitstreamService.find(context, bitstreamID);
                pdfAnnotation = pdfAnnotationService.getAnnotationByBitstreamID(context, bitstream,context.getCurrentUser());
                if (pdfAnnotation == null) {
                    throw new ResourceNotFoundException("authorization not found");
                }
            }else{
                System.out.println("authorization not found");
                throw new ResourceNotFoundException("authorization not found");
            }
        }catch (Exception e){
            System.out.println("something went wrong");
            throw new ResourceNotFoundException("something went wrong");
        }
        return converter.toRest(pdfAnnotation, utils.obtainProjection());
    }
    private PdfAnnotation createRestObject(Context context, PdfAnnotationRest pdfAnnotationRest) throws AuthorizeException {
        PdfAnnotation pdfAnnotation=null;
        try {
            pdfAnnotation = pdfAnnotationService.create(context);
            Bitstream bitstream=bitstreamService.find(context,UUID.fromString(pdfAnnotationRest.getBitstreamRest().getUuid()));
            pdfAnnotation.setBitstream(bitstream);
            pdfAnnotation.setCreated_date(new Date());
            pdfAnnotation.setAnnotationStr(pdfAnnotationRest.getPdfannotationStr());
            if(context.getCurrentUser() != null){
                pdfAnnotation.setePerson(context.getCurrentUser());
            }
            //add note annotation
            if(pdfAnnotationRest.getNoteannotatiostr()!=null){
                pdfAnnotation.setNoteannotatiostr(pdfAnnotationRest.getNoteannotatiostr());
            }
            pdfAnnotationService.update(context, pdfAnnotation);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
        return pdfAnnotation;
    }
    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    protected PdfAnnotationRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                                    JsonNode jsonNode) throws SQLException, AuthorizeException {
        PdfAnnotationRest pdfAnnotationRest = new Gson().fromJson(jsonNode.toString(), PdfAnnotationRest.class);
        if (isBlank(pdfAnnotationRest.getPdfannotationStr())) {
            throw new UnprocessableEntityException("PdfannotationStr element (in request body) cannot be blank");
        }
        PdfAnnotation pdfAnnotation = pdfAnnotationService.find(context, id);
        if (pdfAnnotation == null) {
            System.out.println("documentTypeRest id ::: is Null  document tye null");
            throw new ResourceNotFoundException("metadata field with id: " + id + " not found");
        }
        //add note annotation
        if(pdfAnnotationRest.getNoteannotatiostr()!=null){
            pdfAnnotation.setNoteannotatiostr(pdfAnnotationRest.getNoteannotatiostr());
        }
        pdfAnnotation.setAnnotationStr(pdfAnnotationRest.getPdfannotationStr());
        // System.out.println("documentType name"+documentType.getDocumenttypename());
        pdfAnnotationService.update(context, pdfAnnotation);
        return converter.toRest(pdfAnnotation, utils.obtainProjection());

    }
    @Override
    @PreAuthorize("hasPermission(#uuid, 'EPERSON', '#patch')")
    protected void patch(Context context, HttpServletRequest request, String apiCategory, String model, UUID uuid,
                         Patch patch) throws AuthorizeException, SQLException {

        patchDSpaceObject(apiCategory, model, uuid, patch);
    }
    @Override
    @PreAuthorize("hasPermission(#id, 'ITEM', 'DELETE')")
    protected void delete(Context context, UUID id) throws AuthorizeException {

    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
