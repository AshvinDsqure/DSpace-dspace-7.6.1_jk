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
import org.apache.commons.lang3.StringUtils;
import org.dspace.app.rest.exception.DSpaceBadRequestException;
import org.dspace.app.rest.exception.RESTAuthorizationException;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.CollectionRest;
import org.dspace.app.rest.model.DocumentTypeRest;
import org.dspace.app.rest.model.ItemRest;
import org.dspace.app.rest.model.MetadataFieldRest;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.model.patch.Patch;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DocumentType;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.NonUniqueMetadataException;
import org.dspace.content.service.DocumentTypeService;
import org.dspace.core.Context;
import org.dspace.eperson.service.RegistrationDataService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * This is the repository responsible to manage Item Rest object
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */

@Component(DocumentTypeRest.CATEGORY + "." + DocumentTypeRest.NAME)
public class DocumentTypeRestRepository extends DSpaceObjectRestRepository<DocumentType, DocumentTypeRest> implements InitializingBean {
    @Autowired
    DocumentTypeService documentTypeService;
    @Autowired
    private RegistrationDataService registrationDataService;

    public DocumentTypeRestRepository(DocumentTypeService documentTypeService) {
        super(documentTypeService);
    }

    @Override
    public DocumentTypeRest findOne(Context context, UUID uuid) {
        DocumentType documentType = null;
        try {

            documentType = documentTypeService.find(context, uuid);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        if (documentType == null) {
            return null;
        }
        return converter.toRest(documentType, utils.obtainProjection());
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<DocumentTypeRest> findAll(Context context, Pageable pageable) {
        try {
           // long total = //documentTypeService.(context);
            long total=documentTypeService.countRows(context);
            List<DocumentType> documentTypes= documentTypeService.findAll(context, pageable.getPageSize(),Math.toIntExact(pageable.getOffset()));
            return converter.toRestPage(documentTypes, pageable, total, utils.obtainProjection());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Class<DocumentTypeRest> getDomainClass() {
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
    @PreAuthorize("hasAuthority('ADMIN')")
    protected DocumentTypeRest createAndReturn(Context context)
            throws AuthorizeException, SQLException {
        // this need to be revisited we should receive an EPersonRest as input
        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ObjectMapper mapper = new ObjectMapper();
        DocumentTypeRest documentTypeRest = null;
        try {
            documentTypeRest = mapper.readValue(req.getInputStream(), DocumentTypeRest.class);
        } catch (IOException e1) {
            throw new UnprocessableEntityException("error parsing the body... maybe this is not the right error code");
        }

        // If no token is present, we simply do the admin execution
        DocumentType documentType = createDocumentTypeFromRestObject(context, documentTypeRest);

        return converter.toRest(documentType, utils.obtainProjection());
    }

    private DocumentType createDocumentTypeFromRestObject(Context context, DocumentTypeRest documentTypeRest) throws AuthorizeException {
        DocumentType documentType = null;
        try {
            documentType = documentTypeService.create(context);
            // this should be probably moved to the converter (a merge method?)
            documentType.setDocumenttypename(documentTypeRest.getDocumenttypename());
            documentTypeService.update(context, documentType);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
        return documentType;
    }
    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    protected DocumentTypeRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID id,
                                 JsonNode jsonNode) throws SQLException, AuthorizeException {
        DocumentTypeRest documentTypeRest = new Gson().fromJson(jsonNode.toString(), DocumentTypeRest.class);
       // System.out.println("documentTypeRest :::"+documentTypeRest.getDocumenttypename());
        //System.out.println("documentTypeRest id :::"+id.toString());
        //System.out.println("documentTypeRest id getbyyyy :::"+documentTypeRest.getId());
        if (isBlank(documentTypeRest.getDocumenttypename())) {
            throw new UnprocessableEntityException("Documenttypename element (in request body) cannot be blank");
        }



        DocumentType documentType = documentTypeService.find(context, id);
        //System.out.println("documentType name"+documentType.getDocumenttypename());
        if (documentType == null) {
            System.out.println("documentTypeRest id ::: is Null  document tye null");
            throw new ResourceNotFoundException("metadata field with id: " + id + " not found");
        }
        documentType.setDocumenttypename(documentTypeRest.getDocumenttypename());
       // System.out.println("documentType name"+documentType.getDocumenttypename());
        documentTypeService.update(context, documentType);
        return converter.toRest(documentType, utils.obtainProjection());
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

        DocumentType documentType = null;
        try {
            documentType = documentTypeService.find(context, id);
            if (documentType == null) {
                throw new ResourceNotFoundException(DocumentTypeRest.CATEGORY + "." + DocumentTypeRest.NAME +
                        " with id: " + id + " not found");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            documentTypeService.delete(context, documentType);
            context.commit();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
