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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.multipdf.Overlay;

import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.dspace.app.rest.Enum.DmsAction;
import org.dspace.app.rest.Enum.DmsObject;
import org.dspace.app.rest.Parameter;
import org.dspace.app.rest.SearchRestMethod;
import org.dspace.app.rest.converter.BitstreamConverter;
import org.dspace.app.rest.converter.DocumentTypeTreeConverter;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.BitstreamRest;
import org.dspace.app.rest.model.DocumentTypeRest;
import org.dspace.app.rest.model.DocumentTypeTreeRest;
import org.dspace.app.rest.projection.Projection;
import org.dspace.app.rest.utils.DigitalSignatureInterface;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.dspace.eperson.service.RegistrationDataService;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.xml.crypto.Data;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * This is the repository responsible to manage Item Rest object
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 */

@Component(DocumentTypeTreeRest.CATEGORY + "." + DocumentTypeTreeRest.NAME)
public class DocumentTypeTreeRepository extends DSpaceObjectRestRepository<DocumentTypeTree, DocumentTypeTreeRest> implements InitializingBean {
    @Autowired
    DocumentTypeTreeService documentTypeTreeService;
    @Autowired
    DocumentTypeService documentTypeService;
    @Autowired
    ItemService itemService;
    @Autowired
    DocumentTypeTreeConverter documentTypeTreeConverter;
    @Autowired
    BitstreamConverter bitstreamConverter;
    @Autowired
    BitstreamService bitstreamService;
    @Autowired
    private RegistrationDataService registrationDataService;
    @Autowired(required = true)
    protected ConfigurationService configurationService;
    @Autowired
    protected BundleService bundleService;
    @Autowired
    private BitstreamFormatService bitstreamFormatService;

    public DocumentTypeTreeRepository(DocumentTypeTreeService documentTypeTreeService) {
        super(documentTypeTreeService);
    }
    static {
        // Add Bouncy Castle as a security provider

    }

    @Override
    public DocumentTypeTreeRest findOne(Context context, UUID uuid) {
        DocumentTypeTree documentTypeTree = null;
        try {
            documentTypeTree = documentTypeTreeService.find(context, uuid);
            System.out.println("Data:::::::::::" + documentTypeTree.getDoc_date());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        if (documentTypeTree == null) {
            return null;
        }
        return converter.toRest(documentTypeTree, utils.obtainProjection());
    }

    @Override
    public Page<DocumentTypeTreeRest> findAll(Context context, Pageable pageable) {
        try {
            // long total = //documentTypeService.(context);
            long total = 0;
            List<DocumentTypeTree> documentTypeTrees = documentTypeTreeService.findAll(context, pageable.getPageSize(), Math.toIntExact(pageable.getOffset()));
            return converter.toRestPage(documentTypeTrees, pageable, total, utils.obtainProjection());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Class<DocumentTypeTreeRest> getDomainClass() {
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
     *
     * @param context The DSpace context
     * @throws AuthorizeException If something goes wrong
     * @throws SQLException       If something goes wrong
     */
    @Override
    @PreAuthorize("hasPermission(#id, 'ITEM', 'ADD')")
    protected DocumentTypeTreeRest createAndReturn(Context context) throws AuthorizeException, SQLException {
        // this need to be revisited we should receive an EPersonRest as input
        HttpServletRequest req = getRequestService().getCurrentRequest().getHttpServletRequest();
        ObjectMapper mapper = new ObjectMapper();
        DocumentTypeTreeRest documentTypeTreeRest = null;
        DocumentTypeTree documentTypeTree = null;
        try {
            documentTypeTreeRest = mapper.readValue(req.getInputStream(), DocumentTypeTreeRest.class);
            if (documentTypeTreeRest.getIsTemplet()) {
                documentTypeTree = createDocumentTypeFromRestObject(context, documentTypeTreeRest);
            } else {

                DocumentTypeTree documentypetemlet = documentTypeTreeService.find(context, UUID.fromString(documentTypeTreeRest.getTempletTree().getId()));
                documentTypeTree = createDocumentTypeFromOrigalObject(context, documentypetemlet, documentTypeTreeRest);
            }

        } catch (IOException e1) {
            e1.printStackTrace();
            throw new UnprocessableEntityException("error parsing the body... maybe this is not the right error code");
        }
        return converter.toRest(documentTypeTree, utils.obtainProjection());
    }

    private DocumentTypeTree createDocumentTypeFromRestObject(Context context, DocumentTypeTreeRest documentTypeTreeRest) throws AuthorizeException {
        DocumentTypeTree documentTypeTree = null;
        try {
            documentTypeTree = documentTypeTreeService.create(context);
            documentTypeTree.setDesc(documentTypeTreeRest.getDesc());
            documentTypeTree.setDoc_date(documentTypeTreeRest.getDoc_date());
            documentTypeTree.setTempletName(documentTypeTreeRest.getTempletName());
            documentTypeTree.setIstemplet(true);
            documentTypeTree.setIsDate(documentTypeTreeRest.getIsDate());
            documentTypeTree.setIsRemark(documentTypeTreeRest.getIsRemark());
            documentTypeTree.setDescription(documentTypeTreeRest.getIsDescription());
            documentTypeTree.setRootOfmaster(documentTypeTreeRest.getRootOfmaster());
            documentTypeTree.setSubchild(documentTypeTreeRest.getIsSubchild());
            documentTypeTree.setHasSubChild(documentTypeTreeRest.getHasSubChild());
            documentTypeTree.setNonrepetitive(documentTypeTreeRest.getNonrepetitive());
            if (documentTypeTreeRest.getDocumentType() != null) {
                DocumentType documentType = documentTypeService.find(context, UUID.fromString(documentTypeTreeRest.getDocumentType().getId()));
                documentTypeTree.setDocumentType(documentType);
            }
            if (documentTypeTreeRest.getParent() != null) {
                DocumentTypeTree documentTypeParentTree = documentTypeTreeService.find(context, UUID.fromString(documentTypeTreeRest.getParent().getId()));
                int indexDocumentTypeTree = documentTypeTreeService.getLastnodeIndexByParent(context, documentTypeParentTree);
                System.out.println("indexDocumentTypeTree in parent:::" + indexDocumentTypeTree);
                documentTypeTree.setIndex(indexDocumentTypeTree <= -1 ? 0 : indexDocumentTypeTree + 1);
                documentTypeTree.setParent(documentTypeParentTree);
            } else {
                int indexDocumentTypeTree = documentTypeTreeService.getLastnodeIndexByParent(context, null);
                System.out.println("indexDocumentTypeTree in root:::" + indexDocumentTypeTree);
                documentTypeTree.setIndex(indexDocumentTypeTree <= -1 ? 0 : indexDocumentTypeTree + 1);
            }
            documentTypeTreeService.update(context, documentTypeTree);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
        return documentTypeTree;
    }

    private DocumentTypeTree createDocumentTypeFromOrigalObject(Context context, DocumentTypeTree documentTypeTreeRestsource, DocumentTypeTreeRest documentTypeTreeRest) throws AuthorizeException {
        DocumentTypeTree documentTypeTree = null;
        try {
            System.out.println("doc not create....");
            documentTypeTree = documentTypeTreeService.create(context);
            documentTypeTree.setDescription(documentTypeTreeRestsource.getDescription());
            documentTypeTree.setDoc_date(documentTypeTreeRestsource.getDoc_date());
            documentTypeTree.setTempletName(documentTypeTreeRestsource.getTempletName());
            documentTypeTree.setIsDate(documentTypeTreeRestsource.getDate());
            documentTypeTree.setIsRemark(documentTypeTreeRestsource.getIsRemark());
            documentTypeTree.setRootOfmaster(false);
            documentTypeTree.setIstemplet(documentTypeTreeRest.getIsTemplet());
            documentTypeTree.setDesc(documentTypeTreeRest.getDesc());
            documentTypeTree.setIndex(documentTypeTreeRest.getIndex());
            documentTypeTree.setDoc_date(documentTypeTreeRest.getDoc_date());
            documentTypeTree.setRemark(documentTypeTreeRest.getRemarkdesc());
            documentTypeTree.setSubchild(documentTypeTreeRest.getIsSubchild());
            documentTypeTree.setHasSubChild(documentTypeTreeRest.getHasSubChild());
            documentTypeTree.setNonrepetitive(documentTypeTreeRest.getNonrepetitive());
            if (documentTypeTreeRestsource.getDocumentType() != null) {
                if (documentTypeTreeRest.getIsSubchild() && documentTypeTreeRest.getBitstream() != null) {
                    DocumentType documentType = documentTypeService.find(context, UUID.fromString(documentTypeTreeRest.getDocumentType().getId()));
                    documentTypeTree.setDocumentType(documentType);
                } else {
                    documentTypeTree.setDocumentType(documentTypeTreeRestsource.getDocumentType());
                }
            }
            if (documentTypeTreeRest.getTempletTree() != null) {
                documentTypeTree.setParent(documentTypeTreeRestsource);
                documentTypeTree.setTemplet(documentTypeTreeRestsource);
            }
            if (documentTypeTreeRest.getBitstream() != null) {
                Bitstream bitsetream = bitstreamService.find(context, UUID.fromString(documentTypeTreeRest.getBitstream().getId()));
                documentTypeTree.setBitstream(bitsetream);
            }
            Item item = itemService.find(context, UUID.fromString(documentTypeTreeRest.getItem().getId()));
            if (item != null) {
                documentTypeTree.setItem(item);
            }
            documentTypeTreeService.update(context, documentTypeTree);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
        return documentTypeTree;
    }

    @Override
    @PreAuthorize("hasPermission(#id, 'ITEM', 'ADD')")
    protected DocumentTypeTreeRest put(Context context, HttpServletRequest request, String apiCategory, String model, UUID id, JsonNode jsonNode) throws SQLException, AuthorizeException {

        DocumentTypeTreeRest documentTypeTreeRest = new Gson().fromJson(jsonNode.toString(), DocumentTypeTreeRest.class);
        if (documentTypeTreeRest.getDocumentType() == null) {
            throw new UnprocessableEntityException("Documenttypename element (in request body) cannot be blank");
        }
        DocumentTypeTree documentTypeTree = documentTypeTreeService.find(context, id);
        if (documentTypeTree == null) {
            throw new ResourceNotFoundException("metadata field with id: " + id + " not found");
        }
        DocumentType documentType = documentTypeService.find(context, UUID.fromString(documentTypeTreeRest.getDocumentType().getId()));
        if (documentType == null) {
            throw new ResourceNotFoundException("documentType field with id: " + id + " not found");
        }
        if (documentTypeTree.getIstemplet()) {
            documentTypeTree.setDocumentType(documentType);
            documentTypeTree.setIsDate(documentTypeTreeRest.getIsDate());
            documentTypeTree.setDescription(documentTypeTreeRest.getIsDescription());
            documentTypeTree.setIsRemark(documentTypeTreeRest.getIsRemark());
            documentTypeTree.setIndex(documentTypeTreeRest.getIndex());
            documentTypeTree.setSubchild(documentTypeTreeRest.getIsSubchild());
            documentTypeTree.setHasSubChild(documentTypeTreeRest.getHasSubChild());
            documentTypeTree.setNonrepetitive(documentTypeTreeRest.getNonrepetitive());
        } else {
            DocumentTypeTree documentTypeTreeTemplet = documentTypeTreeService.find(context, UUID.fromString(documentTypeTreeRest.getTempletTree().getId()));
            if (documentTypeTreeTemplet != null) {
                documentTypeTree.setTemplet(documentTypeTreeTemplet);
                documentTypeTree.setParent(documentTypeTreeTemplet);
            }
            documentTypeTree.setRemark(documentTypeTreeRest.getRemarkdesc());
            documentTypeTree.setDesc(documentTypeTreeRest.getDesc());
            documentTypeTree.setDoc_date(documentTypeTreeRest.getDoc_date());
            documentTypeTree.setSubchild(documentTypeTreeRest.getIsSubchild());
            documentTypeTree.setHasSubChild(documentTypeTreeRest.getHasSubChild());
            documentTypeTree.setNonrepetitive(documentTypeTreeRest.getNonrepetitive());
        }
        DmsAction dmsAction = DmsAction.EDIT;
        dmsAction.setePerson(context.getCurrentUser());
        dmsAction.setDocumentTypeTree(documentTypeTree);
        dmsAction.setDsDmsObject(DmsObject.DOCEUMNTTYPE);
        Bitstream bitstream = documentTypeTree.getBitstream();
        if (bitstream != null) {
            Optional<Bundle> bundleOptional = bitstream.getBundles().stream().findFirst();
            if (bundleOptional.isPresent()) {
                Optional<Item> itemOptional = bundleOptional.get().getItems().stream().findFirst();
                if (itemOptional.isPresent())
                    dmsAction.setItem(bitstream.getBundles().get(0).getItems().get(0));
            }
        }
        System.out.println("capture event...");
        try {
            if (documentTypeTree.getItem() != null) {
                dmsAction.setItem(documentTypeTree.getItem());
            }
            String harichicalString = this.getHarichicalFromNode(documentTypeTree);
            dmsAction.setTitle(harichicalString);
            dmsAction.StoreDmsAction(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        documentTypeService.update(context, documentType);
        System.out.println("commit........");
        context.commit();
        return documentTypeTreeConverter.convertTempletForScanning(documentTypeTree, utils.obtainProjection());
    }

    public String getHarichicalFromNode(DocumentTypeTree documentTypeTree) {
        String documentTypeTreeHarichical = null;
        try {
            LinkedHashSet<String> documentRootHarichical = new LinkedHashSet();
            documentRootHarichical = getRootparent(documentTypeTree, documentRootHarichical, false);
            List<String> listOF = new ArrayList<String>(documentRootHarichical);
            Collections.reverse(listOF);
            documentTypeTreeHarichical = String.join(" > ", listOF);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return documentTypeTreeHarichical;
    }

    @PreAuthorize("hasPermission(#id, 'ITEM', 'ADD')")
    @SearchRestMethod(name = "getAllDocumentTypeByItemID")
    public Page<DocumentTypeTreeRest> getAllDocumentTypeByItemID(Pageable pageable, @Parameter(value = "itemID") UUID itemID) throws AuthorizeException {
        List<DocumentTypeTree> documentTypeTrees = null;
        List<DocumentTypeTree> parentnodes = new ArrayList<>();
        List<DocumentTypeTreeRest> documentTypeTreeRests = new ArrayList<>();
        try {
            Context context = obtainContext();
            System.out.println("getChildByNodeID" + itemID);
            Item item = itemService.find(context, itemID);
            if (item == null) {
                throw new ResourceNotFoundException("no bitstream found");
            }

            documentTypeTrees = documentTypeTreeService.getAllDocumentTypeByItemID(context, item);
            documentTypeTrees.forEach(documentTypeTree -> {
                List<DocumentTypeTree> documentTypeTreesList = new ArrayList<>();
                documentTypeTreesList = getRootParentDocumentTypeTree(documentTypeTree, documentTypeTreesList);
                DocumentTypeTree rootDocumentType = documentTypeTreesList.get(0);
                System.out.println("Final node parent----" + rootDocumentType.getDocumentType().getDocumenttypename());
                if (!parentnodes.stream().anyMatch(ti -> ti.getID() == rootDocumentType.getID())) {
                    parentnodes.add(documentTypeTreesList.get(0));
                }
            });

            parentnodes.forEach(documentTypeTree -> {
                DocumentTypeTreeRest documentTypeTreeRest = documentTypeTreeConverter.convert(documentTypeTree, utils.obtainProjection());
                if (documentTypeTree.getNonTempletChildren() != null && documentTypeTree.getNonTempletChildren().size() != 0) {
                    documentTypeTreeRest.setHasChildren(true);
                }
                documentTypeTreeRests.add(documentTypeTreeRest);
            });
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return new PageImpl<DocumentTypeTreeRest>(documentTypeTreeRests, pageable, documentTypeTreeRests.size());

    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @SearchRestMethod(name = "getAllRootTree")
    public Page<DocumentTypeTreeRest> getAllRootTree(Pageable pageable) {
        try {
            Context context = obtainContext();
            List<DocumentTypeTree> documentTypeTrees = documentTypeTreeService.getAllRootTree(context);
            return converter.toRestPage(documentTypeTrees, pageable, 0, utils.obtainProjection());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    //    @PreAuthorize("hasAuthority('ADMIN')")
    @SearchRestMethod(name = "updateDocTypeIndex")
    public DocumentTypeTreeRest updateDocTypeIndex(@Parameter(value = "parentID") UUID parentID) {
        try {
            Context context = obtainContext();
            Item item = null;
            if (parentID != null) {
                item = itemService.find(context, parentID);
                System.out.println("item" + item.getName());
            }

            List<DocumentTypeTree> documentTypeTrees = documentTypeTreeService.updateDocTypeIndex(context, item);
            documentTypeTrees.forEach(d -> {
                   /* System.out.println("==============================================");
                    System.out.println("Doctype::" + d.getDocumentType().getDocumenttypename());
                    System.out.println("description::" + d.getDesc());
                    System.out.println("subchild::" + d.getSubchild());
                    System.out.println("parent subchild::" + d.getParent().getSubchild());
                    System.out.println("parent of parent::" + d.getParent().getParent().getDocumentType().getDocumenttypename());
                    System.out.println("parent of parent templet::" + d.getParent().getParent().getIstemplet());
                    System.out.println("==============================================");
                    try {
                    documentTypeTreeService.update(context,d);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (AuthorizeException e) {
                    throw new RuntimeException(e);
                }*/

                DocumentTypeTree foundDocType = d.getParent().getParent().getChildren().stream().filter(c -> c.getDocumentType().getDocumenttypename().equals(d.getDesc())).findFirst().orElse(null);
                if (foundDocType == null) {
                    System.out.println("not found.........");
                } else {
                    d.setSubchild(true);
                    System.out.println("befor date" + d.getDoc_date());
                    d.setIndex(foundDocType.getIndex());
                    d.setDocumentType(foundDocType.getDocumentType());
                    try {
                        documentTypeTreeService.update(context, d);
                        System.out.println("after date" + d.getDoc_date());
                        System.out.println("uuid date" + d.getID());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } catch (AuthorizeException e) {
                        throw new RuntimeException(e);
                    }
                }


            });
            context.complete();
            System.out.println("documentTypeTrees::" + documentTypeTrees.size());
            return new DocumentTypeTreeRest();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @PreAuthorize("hasPermission(#id, 'ITEM', 'ADD')")
    @SearchRestMethod(name = "getChildNodeByparentID")
    public Page<DocumentTypeTreeRest> getChildNodeByparentID(Pageable pageable, @Parameter(value = "parentID") UUID parentID, @Parameter(value = "isTemplet") Boolean isTemplet) {
        try {
            Context context = obtainContext();
            DocumentTypeTree documentTypeTree = documentTypeTreeService.find(context, parentID);
            List<DocumentTypeTree> documentTypeTrees = new ArrayList<DocumentTypeTree>();
            List<DocumentTypeTreeRest> removedParentdocumentTypeTrees = new ArrayList<DocumentTypeTreeRest>();
            documentTypeTrees = documentTypeTreeService.getChildByNodeID(context, documentTypeTree, isTemplet);

            removedParentdocumentTypeTrees = documentTypeTrees.stream().map(d -> {
                d.setAllLoaded(false);
                d.setTemplet(null);
                return documentTypeTreeConverter.convertTODocumentType(d, utils.obtainProjection());
            }).collect(toList());
            return new PageImpl(removedParentdocumentTypeTrees, pageable, removedParentdocumentTypeTrees.size());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @PreAuthorize("hasPermission(#id, 'ITEM', 'ADD')")
    @SearchRestMethod(name = "getChildNodeByparentIDForScanning")
    public Page<DocumentTypeTreeRest> getChildNodeByparentIDForScanning(Pageable pageable, @Parameter(value = "parentID") UUID parentID, @Parameter(value = "isTemplet") Boolean isTemplet) {
        try {
            Context context = obtainContext();
            DocumentTypeTree documentTypeTree = documentTypeTreeService.find(context, parentID);
            List<DocumentTypeTree> documentTypeTrees = new ArrayList<DocumentTypeTree>();
            List<DocumentTypeTree> removedParentdocumentTypeTrees = new ArrayList<DocumentTypeTree>();
            documentTypeTrees = documentTypeTreeService.getChildByNodeID(context, documentTypeTree, isTemplet);
            List<DocumentTypeTreeRest> documentTypeTreesRest = new ArrayList<>();
            documentTypeTrees.stream().forEach(d -> {
                DocumentTypeTreeRest documentTypeTreeRest = documentTypeTreeConverter.convertSubchild(d, utils.obtainProjection());
                documentTypeTreesRest.add(documentTypeTreeRest);
            });
            return new PageImpl<DocumentTypeTreeRest>(documentTypeTreesRest, pageable, documentTypeTreesRest.size());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @PreAuthorize("hasPermission(#id, 'ITEM', 'ADD')")
    @SearchRestMethod(name = "getAllnodehierarchyforScanning")
    public Page<DocumentTypeTreeRest> getAllnodehierarchyforScanning(Pageable pageable, @Parameter(value = "parentID") UUID parentID, @Parameter(value = "isTemplet") Boolean isTemplet) {
        try {
            Context context = obtainContext();
            List<DocumentTypeTree> documentTypeTrees = new ArrayList<DocumentTypeTree>();
            documentTypeTrees = documentTypeTreeService.getAllRootTree(context);
            List<DocumentTypeTreeRest> documentTypeTreesRest = new ArrayList<>();
            documentTypeTrees.stream().forEach(d -> {
                DocumentTypeTreeRest documentTypeTreeRest = documentTypeTreeConverter.convertTempletForScanning(d, utils.obtainProjection());
                documentTypeTreesRest.add(documentTypeTreeRest);
            });
            return new PageImpl<DocumentTypeTreeRest>(documentTypeTreesRest, pageable, documentTypeTreesRest.size());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @PreAuthorize("hasPermission(#id, 'ITEM', 'READ')")
    @SearchRestMethod(name = "mergeBookMark")
    public DocumentTypeTreeRest mergeBookMark(Pageable pageable, @Parameter(value = "itemID") UUID itemID) {
        try {
            Context context = obtainContext();
            Item item = itemService.find(context, itemID);
            DocumentTypeTreeRest documentTypeTreeRest = null;
            if (item == null) {
                throw new ResourceNotFoundException("no bitstream found");
            }
            String tempWorkDir = configurationService.getProperty("org.dspace.app.batchitemimport.work.dir");
            List<Bundle> bundles = itemService.getBundles(item, "MergeDoc");
            Bundle targetBundle = null;
            if (bundles.size() < 1) {
                // not found, create a new one
                targetBundle = bundleService.create(context, item, "MergeDoc");
            } else {
                // put bitstreams into first bundle
                targetBundle = bundles.iterator().next();
            }
            Path tempPath = Path.of(tempWorkDir + "/" + targetBundle.getID() + Instant.now().toEpochMilli() + ".pdf");
            List<PDOutlineItem> rootBookMarks = new ArrayList<>();
            List<PDDocument> documents = new ArrayList<>();
            List<DocumentTypeTree> transformDocumentTypes = documentTypeTreeService.transformDocumentTypesFromChildToparent(context, item);
            transformDocumentTypes.stream().forEach(documenttypeObject -> {
                documenttypeObject.setAllLoaded(true);
                DocumentTypeTreeRest root = documentTypeTreeConverter.convertHasDisplay(context, documenttypeObject, utils.obtainProjection(), true, documents);
                rootBookMarks.add(root.retuenChildbookmark());

            });
            try {
                targetBundle.getBitstreams().forEach(bit -> {
                    try {
                        bitstreamService.delete(context, bit);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } catch (AuthorizeException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                saveBookmarkOntemp(rootBookMarks, tempPath, documents);
                Bitstream bitstream = processBitstreamCreation(context, targetBundle, new FileInputStream(tempPath.toFile()), "", tempPath.getFileName().toString());
                bundleService.update(context, targetBundle);
                Files.deleteIfExists(tempPath);
                BitstreamRest bitstreamRest = bitstreamConverter.convert(bitstream, utils.obtainProjection());
                DmsAction dmsAction = DmsAction.MERGEPDF;
                dmsAction.setePerson(context.getCurrentUser());
                dmsAction.setDsDmsObject(DmsObject.DOCEUMNTTYPE);
                try {
                    dmsAction.setItem(item);
                    dmsAction.StoreDmsAction(context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                context.commit();
                DocumentTypeTreeRest dummyDocType = new DocumentTypeTreeRest();
                dummyDocType.setBitstream(bitstreamRest);
                return dummyDocType;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void saveBookmarkOntemp(List<PDOutlineItem> rootBookMark, Path tempFile, List<PDDocument> podocument) throws Exception {
        PDDocument localpodocument = new PDDocument();

        try {
            boolean isWatermask = configurationService.getBooleanProperty("pdf.watermark.enable");
            int x = configurationService.getIntProperty("pdf.watermark.x");
            ; // Adjust the X-coordinate
            int y = configurationService.getIntProperty("pdf.watermark.y");
            int width = configurationService.getIntProperty("pdf.watermark.width");
            int height = configurationService.getIntProperty("pdf.watermark.height");

            // Set the opacity (0.0 fully transparent, 1.0 fully opaque)
            float opacity = 0.2f; // Adjust the opacity value
            PDExtendedGraphicsState extendedGraphicsState = new PDExtendedGraphicsState();
            extendedGraphicsState.setNonStrokingAlphaConstant(opacity);

            for (PDDocument p : podocument) {
                PDImageXObject image = null;

                if (isWatermask)
                    image = PDImageXObject.createFromFile(configurationService.getProperty("pdf.watermark.filepath"), p);
                for (PDPage pd : p.getPages()) {
                    if (isWatermask) {
                        try (PDPageContentStream contentStream = new PDPageContentStream(p, pd, PDPageContentStream.AppendMode.APPEND, true, true)) {
                            // Add the PNG image to the content stream
                            System.out.println("image write................");
                            contentStream.drawImage(image, x, y, width, height);
                            contentStream.setGraphicsStateParameters(extendedGraphicsState);
                            //contentStream.drawImage(image, x, y, width, height);
                        }
                    }
                    localpodocument.addPage(pd);
                }

            }
            podocument.clear();
            PDDocumentOutline outline = new PDDocumentOutline();
            localpodocument.getDocumentCatalog().setDocumentOutline(outline);
            PDOutlineItem pagesOutline = new PDOutlineItem();
            pagesOutline.setTitle("Smart View");
            outline.addLast(pagesOutline);
            for (PDOutlineItem pdi : rootBookMark) {
                pagesOutline.addLast(pdi);
            }
            pagesOutline.openNode();
            outline.openNode();
            localpodocument.getDocumentCatalog().setPageMode(PageMode.USE_OUTLINES);
            localpodocument.save(tempFile.toFile());
            pagesOutline.closeNode();
            outline.closeNode();
            localpodocument.close();
            System.out.println("mearge done");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("");
        } finally {
            localpodocument.close();
        }

    }
    public void saveWaterMark(List<PDOutlineItem> rootBookMark, Path tempFile, List<PDDocument> podocument) throws Exception {
        PDDocument localpodocument = new PDDocument();

        try {
            boolean isWatermask = configurationService.getBooleanProperty("pdf.watermark.enable");
            int x = configurationService.getIntProperty("pdf.watermark.x");
            ; // Adjust the X-coordinate
            int y = configurationService.getIntProperty("pdf.watermark.y");
            int width = configurationService.getIntProperty("pdf.watermark.width");
            int height = configurationService.getIntProperty("pdf.watermark.height");

            // Set the opacity (0.0 fully transparent, 1.0 fully opaque)
            float opacity = 0.2f; // Adjust the opacity value
            PDExtendedGraphicsState extendedGraphicsState = new PDExtendedGraphicsState();
            extendedGraphicsState.setNonStrokingAlphaConstant(opacity);

            for (PDDocument p : podocument) {
                PDImageXObject image = null;

                if (isWatermask)
                    image = PDImageXObject.createFromFile(configurationService.getProperty("pdf.watermark.filepath"), p);
                for (PDPage pd : p.getPages()) {
                    if (isWatermask) {
                        try (PDPageContentStream contentStream = new PDPageContentStream(p, pd, PDPageContentStream.AppendMode.APPEND, true, true)) {
                            // Add the PNG image to the content stream
                            System.out.println("image write................");
                            contentStream.drawImage(image, x, y, width, height);
                            contentStream.setGraphicsStateParameters(extendedGraphicsState);
                            //contentStream.drawImage(image, x, y, width, height);
                        }
                    }
                    localpodocument.addPage(pd);
                }

            }
            podocument.clear();
            PDDocumentOutline outline = new PDDocumentOutline();
            localpodocument.getDocumentCatalog().setDocumentOutline(outline);
            PDOutlineItem pagesOutline = new PDOutlineItem();
            pagesOutline.setTitle("Smart View");
            outline.addLast(pagesOutline);
            for (PDOutlineItem pdi : rootBookMark) {
                pagesOutline.addLast(pdi);
            }
            pagesOutline.openNode();
            outline.openNode();
            localpodocument.getDocumentCatalog().setPageMode(PageMode.USE_OUTLINES);
            localpodocument.save(tempFile.toFile());
            pagesOutline.closeNode();
            outline.closeNode();
            localpodocument.close();
            System.out.println("mearge done");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("");
        } finally {
            localpodocument.close();
        }

    }


    private Bitstream processBitstreamCreation(Context context, Bundle bundle, InputStream fileInputStream,
                                               String properties, String originalFilename)
            throws AuthorizeException, IOException, SQLException {

        Bitstream bitstream = null;
        if (StringUtils.isNotBlank(properties)) {
            ObjectMapper mapper = new ObjectMapper();
            BitstreamRest bitstreamRest = null;
            try {
                bitstreamRest = mapper.readValue(properties, BitstreamRest.class);
            } catch (Exception e) {
                throw new UnprocessableEntityException("The properties parameter was incorrect: " + properties);
            }
            bitstream = bitstreamService.create(context, bundle, fileInputStream);
            if (bitstreamRest.getMetadata() != null) {
                metadataConverter.setMetadata(context, bitstream, bitstreamRest.getMetadata());
            }
            String name = bitstreamRest.getName();
            if (StringUtils.isNotBlank(name)) {
                bitstream.setName(context, name);
            } else {
                bitstream.setName(context, originalFilename);
            }

        } else {
            bitstream = bitstreamService.create(context, bundle, fileInputStream);
            bitstream.setName(context, originalFilename);

        }
        BitstreamFormat bitstreamFormat = bitstreamFormatService.guessFormat(context, bitstream);
        bitstreamService.setFormat(context, bitstream, bitstreamFormat);
        bitstreamService.update(context, bitstream);

        return bitstream;
    }

    @PreAuthorize("hasPermission(#id, 'ITEM', 'ADD')")
    @SearchRestMethod(name = "getIssubchildNodeByparentIDandItemID")
    public Page<DocumentTypeTreeRest> getIssubchildNodeByparentIDandItemID(Pageable pageable, @Parameter(value = "parentID") UUID parentID, @Parameter(value = "itemUUID") UUID itemUUID) {
        try {
            Context context = obtainContext();
            DocumentTypeTree parentdocumentTypeTree = documentTypeTreeService.find(context, parentID);
            Item item = itemService.find(context, itemUUID);
            List<DocumentTypeTree> documentTypeTrees = documentTypeTreeService.getAllIsSubchildDocumentTypeByItemID(context, item, parentdocumentTypeTree);
            return converter.toRestPage(documentTypeTrees, pageable, 0, utils.obtainProjection());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @PreAuthorize("hasPermission(#id, 'ITEM', 'DELETE')")
    @SearchRestMethod(name = "getChildByNodeIDForTempletForSubmiter")
    public Page<DocumentTypeTreeRest> getChildByNodeIDForTempletForSubmiter(Pageable pageable, @Parameter(value = "rootid") String rootid, @Parameter(value = "istemplet") boolean istemplet, @Parameter(value = "itemid") String itemid) {
        try {
            Context context = obtainContext();
            System.out.println("getChildByNodeID" + rootid);
            DocumentTypeTree ParentNode = documentTypeTreeService.find(context, UUID.fromString(rootid));
            List<DocumentTypeTreeRest> documentTypeTreesRest = new ArrayList<>();
            if (ParentNode != null) {
                Projection projection = utils.obtainProjection();
                ParentNode.getNonTempletChildren().forEach(documenttype -> {
                    DocumentTypeTreeRest documentTypeTreeRest = documentTypeTreeConverter.convert(documenttype, utils.obtainProjection());
                    if (documenttype.getNonTempletChildren() != null && documenttype.getNonTempletChildren().size() != 0) {
                        documentTypeTreeRest.setHasChildren(true);
                    }
                    if (!documentTypeTreeRest.getIsTemplet()) {
                        if (documentTypeTreeRest.getItem().getId().equals(itemid)) {
                            documentTypeTreesRest.add(documentTypeTreeRest);
                        }
                    } else {
                        documentTypeTreesRest.add(documentTypeTreeRest);
                    }
                    // projection.transformModel(documentTypeTreeRest);


                });
            }
            return new PageImpl<DocumentTypeTreeRest>(documentTypeTreesRest, pageable, documentTypeTreesRest.size());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @SearchRestMethod(name = "getChildByNodeIDForTempletForAdmin")
    public Page<DocumentTypeTreeRest> getChildByNodeIDForTempletForAdmin(Pageable pageable, @Parameter(value = "rootid") String rootid, @Parameter(value = "istemplet") boolean istemplet) {
        try {
            Context context = obtainContext();
            System.out.println("getChildByNodeID" + rootid);
            DocumentTypeTree ParentNode = documentTypeTreeService.find(context, UUID.fromString(rootid));
            List<DocumentTypeTreeRest> documentTypeTreesRest = new ArrayList<>();
            if (ParentNode != null) {
                Projection projection = utils.obtainProjection();
                ParentNode.getChildren().forEach(documenttype -> {
                    DocumentTypeTreeRest documentTypeTreeRest = documentTypeTreeConverter.convert(documenttype, utils.obtainProjection());
                    documentTypeTreesRest.add(documentTypeTreeRest);

                });
            }
            return new PageImpl<DocumentTypeTreeRest>(documentTypeTreesRest, pageable, documentTypeTreesRest.size());
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    @PreAuthorize("hasAuthority('ADMIN')")
    @SearchRestMethod(name = "getDocTypeByBitstreamID")
    public DocumentTypeTreeRest getDocTypeByBitstreamID(@Parameter(value = "bitstreamId") UUID bitstreamId) {
        try {
            Context context = obtainContext();
            System.out.println("getChildByNodeID" + bitstreamId);
            Bitstream bitstream = bitstreamService.find(context, bitstreamId);
            System.out.println("bitstream:::" + bitstream);
            if (bitstream == null) {
                throw new ResourceNotFoundException("no bitstream found");
            }
            DocumentTypeTree documentTypeTree = documentTypeTreeService.getAllDocumentTypeByBitstreamID(context, bitstream);
            System.out.println("documentTypeTree:-----" + documentTypeTree);
            if (documentTypeTree == null) {
                throw new ResourceNotFoundException("no Templet found");
            }
            return converter.toRest(documentTypeTree, utils.obtainProjection());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    @Override
    @PreAuthorize("hasPermission(#id, 'ITEM', 'DELETE')")
    protected void delete(Context context, UUID id) throws AuthorizeException {

        DocumentTypeTree documentTypeTree = null;
        try {
            documentTypeTree = documentTypeTreeService.find(context, id);

            if (documentTypeTree == null) {
                throw new ResourceNotFoundException(DocumentTypeRest.CATEGORY + "." + DocumentTypeRest.NAME + " with id: " + id + " not found");
            }
            if (documentTypeTree.getChildren().size() != 0) {
                throw new ResourceNotFoundException("child can not delete");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {

            DmsAction dmsAction = DmsAction.DELETE;
            dmsAction.setePerson(context.getCurrentUser());
            dmsAction.setDsDmsObject(DmsObject.DOCEUMNTTYPE);
            dmsAction.setDocumentTypeTree(documentTypeTree);
            Bitstream bitstream = documentTypeTree.getBitstream();
            if (bitstream != null) {
                Optional<Bundle> bundleOptional = bitstream.getBundles().stream().findFirst();
                if (bundleOptional.isPresent()) {
                    Optional<Item> itemOptional = bundleOptional.get().getItems().stream().findFirst();
                    if (itemOptional.isPresent())
                        dmsAction.setItem(bitstream.getBundles().get(0).getItems().get(0));
                }
                dmsAction.setDescription(documentTypeTree.getDesc() + ">" + documentTypeTree.getParent().getDocumentType().getDocumenttypename());
            }
            System.out.println("capture event...");
            try {
                String harichicalString = this.getHarichicalFromNode(documentTypeTree);
                if (documentTypeTree.getItem() != null) {
                    dmsAction.setItem(documentTypeTree.getItem());
                }
                dmsAction.setTitle(harichicalString);
                dmsAction.StoreDmsAction(context);
            } catch (Exception e) {
                e.printStackTrace();
            }
            documentTypeTreeService.delete(context, documentTypeTree);
            context.commit();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEM', 'WRITE')")
    @SearchRestMethod(name = "searchDocumentTreeByDocumentTypeName")
    public Page<DocumentTypeTreeRest> searchDocumentTreeByDocumentTypeName(Pageable pageable, @Parameter(value = "query", required = false) String query) throws AuthorizeException {
        List<DocumentTypeTree> documentTypeTrees = new ArrayList<>();
        List<DocumentTypeTreeRest> documentTypeTreeRests = new ArrayList<>();
        try {
            Context context = obtainContext();
            documentTypeTrees = documentTypeTreeService.SearchDocumentTreeByDocumentTypeNamescanning(context, query);
            if (documentTypeTrees == null) {
                throw new ResourceNotFoundException(DocumentTypeRest.CATEGORY + "." + DocumentTypeRest.NAME + "");
            }
            documentTypeTrees = documentTypeTrees.stream().map(documentTypeTree -> {
                LinkedHashSet<String> documentRootHarichical = new LinkedHashSet();
                documentRootHarichical.add(documentTypeTree.getDocumentType().getDocumenttypename());
                documentRootHarichical = getRootparent(documentTypeTree, documentRootHarichical, false);
                List<String> listOF = new ArrayList<String>(documentRootHarichical);
                Collections.reverse(listOF);
                documentTypeTree.setTempletName(String.join(" > ", listOF));
                documentTypeTree.setAllLoaded(false);
                return documentTypeTree;
            }).collect(toList());
            System.out.println("find and get the detail....");
            documentTypeTreeRests = documentTypeTrees.stream().map(d -> {
                return documentTypeTreeConverter.convertsearch(d, utils.obtainProjection());
            }).collect(toList());
            System.out.println("i am done and getting result");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
        return new PageImpl<DocumentTypeTreeRest>(documentTypeTreeRests, pageable, documentTypeTreeRests.size());
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEM', 'WRITE')")
    @SearchRestMethod(name = "searchDocumentTreeByDocumentTypeNameFromDMS")
    public Page<DocumentTypeTreeRest> searchDocumentTreeByDocumentTypeNameFromDMS(Pageable pageable, @Parameter(value = "query", required = false) String query) throws AuthorizeException {
        List<DocumentTypeTree> documentTypeTrees = new ArrayList<>();
        List<DocumentTypeTreeRest> documentTypeTreeRests = new ArrayList<>();
        try {
            Context context = obtainContext();
            documentTypeTrees = documentTypeTreeService.SearchDocumentTreeByDocumentTypeName(context, query);
            if (documentTypeTrees == null) {
                throw new ResourceNotFoundException(DocumentTypeRest.CATEGORY + "." + DocumentTypeRest.NAME + "");
            }
            documentTypeTrees = documentTypeTrees.stream().map(documentTypeTree -> {
                LinkedHashSet<String> documentRootHarichical = new LinkedHashSet();
                documentRootHarichical.add(documentTypeTree.getDocumentType().getDocumenttypename());
                documentRootHarichical = getRootparent(documentTypeTree, documentRootHarichical, false);
                List<String> listOF = new ArrayList<String>(documentRootHarichical);
                Collections.reverse(listOF);
                documentTypeTree.setTempletName(String.join(" > ", listOF));
                documentTypeTree.setAllLoaded(false);
                return documentTypeTree;
            }).collect(toList());
            System.out.println("find and get the detail....");
            documentTypeTreeRests = documentTypeTrees.stream().map(d -> {
                return documentTypeTreeConverter.convertsearch(d, utils.obtainProjection());
            }).collect(toList());
            System.out.println("i am done and getting result");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
        return new PageImpl<DocumentTypeTreeRest>(documentTypeTreeRests, pageable, documentTypeTreeRests.size());
    }

    @PreAuthorize("hasPermission(#uuid, 'ITEM', 'WRITE')")
    @SearchRestMethod(name = "searchDocumentLastTreeDocumentType")
    public DocumentTypeTreeRest searchDocumentLastTreeDocumentType(Pageable pageable, @Parameter(value = "query", required = false) List<String> query) throws AuthorizeException {

        try {
            Context context = obtainContext();
            String root = query.stream().findFirst().get();
            System.out.println("Query::" + query.toString() + "root");
            AtomicReference<Optional<DocumentTypeTree>> documentTypeTree = new AtomicReference<>(Optional.ofNullable(documentTypeTreeService.getRootTreeByName(context, root)));
            if (documentTypeTree.get().isPresent()) {
                java.util.function.Consumer<Map<String, DocumentTypeTree>> documentTypeTreeConsumer = (Map<String, DocumentTypeTree> t) -> {
                    String searchingKey = t.entrySet().stream().findFirst().get().getKey();
                    Optional<DocumentTypeTree> itrationRootDoc = t.entrySet().stream().findFirst().get().getValue().getChildren().stream().filter(x -> x.getDocumentType().getDocumenttypename().equals(searchingKey)).findFirst();
                    if (itrationRootDoc.isPresent()) {
                        documentTypeTree.set(itrationRootDoc);
                    } else {
                        documentTypeTree.set(null);
                    }
                };
                query.stream().skip(1).forEach(x -> {
                    Map<String, DocumentTypeTree> mapforConsumer = new HashMap<>();
                    if (documentTypeTree.get() != null) {
                        System.out.println("key:::" + x + "parent" + documentTypeTree.get().get().getID());
                        mapforConsumer.put(x, documentTypeTree.get().get());
                        documentTypeTreeConsumer.accept(mapforConsumer);
                    }
                });

            }
            if (documentTypeTree.get() != null) {
                return converter.toRest(documentTypeTree.get().get(), utils.obtainProjection());
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    @PreAuthorize("hasPermission(#uuid, 'ITEM', 'WRITE')")
    @SearchRestMethod(name = "getDocumentTypeTreeIDWithPopulateNodeandChild")
    public DocumentTypeTreeRest getDocumentTypeTreeIDWithPopulateNodeandChild(@Parameter(value = "ID", required = false) UUID ID) throws AuthorizeException {
        DocumentTypeTree documentTypeTree = null;
        try {
            Context context = obtainContext();
            documentTypeTree = documentTypeTreeService.find(context, ID);
            System.out.println("documentTypeTrees::::" + documentTypeTree);
            if (documentTypeTree == null) {
                throw new ResourceNotFoundException(DocumentTypeRest.CATEGORY + "." + DocumentTypeRest.NAME + "");
            }
            LinkedHashSet<String> documentRootHarichical = new LinkedHashSet();
            documentRootHarichical.add(documentTypeTree.getID().toString() + "||" + documentTypeTree.getDocumentType().getDocumenttypename());
            documentRootHarichical = getRootparent(documentTypeTree, documentRootHarichical, true);
            List<String> listOF = new ArrayList<String>(documentRootHarichical);
            Collections.reverse(listOF);
            documentTypeTree.setTempletName(String.join(" > ", listOF));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
        System.out.println("documentTypeTreeRest:::::=>");
        return converter.toRest(documentTypeTree, utils.obtainProjection());
    }

    public LinkedHashSet<String> getRootparent(DocumentTypeTree documentTypeTree, LinkedHashSet<String> parentList, boolean isUUID) {
        try {
            if (documentTypeTree.getParent() != null) {
                if (isUUID) {
                    parentList.add(documentTypeTree.getID().toString() + "||" + documentTypeTree.getParent().getDocumentType().getDocumenttypename());
                } else {
                    parentList.add(documentTypeTree.getParent().getDocumentType().getDocumenttypename());
                }
                getRootparent(documentTypeTree.getParent(), parentList, isUUID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parentList;
    }

    public List<DocumentTypeTree> getRootParentDocumentTypeTree(DocumentTypeTree documentTypeTree, List<DocumentTypeTree> documentTypeTreesList) {
        try {
            // System.out.println("documentTypeTree::::" + documentTypeTree.getDocumentType().getDocumenttypename());

            if (documentTypeTree.getParent() != null) {
                documentTypeTree = documentTypeTree.getParent();
                documentTypeTree.getChildren().clear();
                documentTypeTree.getChildren().add(documentTypeTree);
                getRootParentDocumentTypeTree(documentTypeTree, documentTypeTreesList);
            } else {
                documentTypeTreesList.add(documentTypeTree);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return documentTypeTreesList;
    }

    public List<DocumentTypeTree> getAllChild(DocumentTypeTree documentTypeTree, List<DocumentTypeTree> documentTypeTreesList) {
        try {
            System.out.println("documentTypeTree.getParent()::::" + documentTypeTree.getParent());

            if (documentTypeTree.getParent() != null) {
                System.out.println("get parent:::" + documentTypeTree.getParent().getDocumentType().getDocumenttypename());
                DocumentTypeTree documentTypeTreeParent = documentTypeTree.getParent();
                documentTypeTreeParent.setChildren(new HashSet<>());
                //documentTypeTreeParent.getChildren().add(documentTypeTree);
                getRootParentDocumentTypeTree(documentTypeTreeParent, documentTypeTreesList);
            } else {
                documentTypeTreesList.add(documentTypeTree);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return documentTypeTreesList;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    //@PreAuthorize("hasAuthority('ADMIN')")
    @SearchRestMethod(name = "getAllDocumentTypeandChildByItemID")
    public Page<DocumentTypeTreeRest> getAllDocumentTypeandChildByItemID(Pageable pageable, @Parameter(value = "itemID") UUID itemID) throws AuthorizeException {
        List<DocumentTypeTreeRest> documentTypeTreesrest = new ArrayList();
        try {
            Context context = obtainContext();
            Item item = itemService.find(context, itemID);
            if (item == null) {
                throw new ResourceNotFoundException("no bitstream found");
            }
            long start = System.currentTimeMillis();
            List<DocumentTypeTree> transformDocumentTypes = documentTypeTreeService.transformDocumentTypesFromChildToparent(context, item);
            System.out.println("convert...");
            List<DocumentTypeTreeRest> documentTypeTreesItemRest = transformDocumentTypes.stream().map(documenttypeObject -> {
                documenttypeObject.setAllLoaded(true);
                DocumentTypeTreeRest documentTypeTreeRest = documentTypeTreeConverter.convertIteamDisplayPage(context, documenttypeObject, utils.obtainProjection(), false, null);
                return documentTypeTreeRest;
            }).collect(toList());
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            System.out.println("send to responce convert..." + timeElapsed);
            return new PageImpl<DocumentTypeTreeRest>(documentTypeTreesItemRest, pageable, transformDocumentTypes.size());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }


}