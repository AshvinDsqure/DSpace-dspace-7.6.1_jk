/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.Enum.DmsAction;
import org.dspace.app.rest.Enum.DmsObject;
import org.dspace.app.rest.converter.DocumentTypeTreeConverter;
import org.dspace.app.rest.model.DocumentTypeTreeRest;
import org.dspace.app.rest.repository.DocumentTypeTreeRepository;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.app.rest.utils.Utils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.DocumentTypeTree;
import org.dspace.content.Item;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.DocumentTypeTreeService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static org.dspace.app.rest.utils.RegexUtils.REGEX_REQUESTMAPPING_IDENTIFIER_AS_UUID;

@RestController
@RequestMapping("/api/" + DocumentTypeTreeRest.NAME + "/" + DocumentTypeTreeRest.PLURAL_NAME+"s"
        + REGEX_REQUESTMAPPING_IDENTIFIER_AS_UUID)
public class DocumentTypeTreeRestController {
    private static final Logger log = org.apache.logging.log4j.LogManager
            .getLogger(DocumentTypeTreeRestController.class);
    @Autowired
    private DocumentTypeTreeRepository documentTypeTreeRepository;
    @Autowired
    private DocumentTypeTreeService documentTypeTreeService;
    @Autowired
    private BitstreamService bitstreamService;
    @Autowired
    Utils utils;
    @Autowired
    private DocumentTypeTreeConverter documentTypeTreeConverter;
    @PreAuthorize("hasPermission(#uuid, 'BITSTREAM','WRITE')")
    @RequestMapping(method = RequestMethod.POST,
            consumes = {"application/json"},value = "/update")
    public DocumentTypeTreeRest updateDocumentTypeTree(HttpServletRequest request, @PathVariable UUID uuid, @RequestBody DocumentTypeTreeRest documentTypeTreeRest)
            throws SQLException, IOException, AuthorizeException {
        Context context = ContextUtil.obtainContext(request);
        DocumentTypeTree documentTypeTree = documentTypeTreeService.find(context, uuid);
        if (documentTypeTree == null) {
            throw new ResourceNotFoundException(
                    "The given uuid did not resolve to a community on the server: " + uuid);
        }
        if(documentTypeTreeRest.getDesc() != null && documentTypeTreeRest.getDesc().trim().length() != 0){
            documentTypeTree.setDesc(documentTypeTreeRest.getDesc());
        }
        if(documentTypeTreeRest.getDoc_date() != null && documentTypeTreeRest.getDoc_date().toString().trim().length() != 0){
            documentTypeTree.setDoc_date(documentTypeTreeRest.getDoc_date());
            documentTypeTree.setCreated_date(documentTypeTreeRest.getDoc_date());
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
        System.out.println("capture event edit doument.........");
        try {
            if (documentTypeTree.getItem() != null) {
                dmsAction.setItem(documentTypeTree.getItem());
            }
            String harichicalString= this.getHarichicalFromNode(documentTypeTree);
            dmsAction.setTitle(harichicalString);
            dmsAction.StoreDmsAction(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        documentTypeTreeService.update(context, documentTypeTree);
        documentTypeTree.setAllLoaded(false);
       documentTypeTreeRest=documentTypeTreeConverter.convert(documentTypeTree, utils.obtainProjection());
        context.commit();
        return documentTypeTreeRest;
    }
    public  String getHarichicalFromNode(DocumentTypeTree documentTypeTree){
        String documentTypeTreeHarichical=null;
        try{
            LinkedHashSet<String> documentRootHarichical = new LinkedHashSet();
            if(documentTypeTree.getDesc() != null){

            }

            documentRootHarichical.addAll(getRootparent(documentTypeTree, documentRootHarichical, false));
            List<String> listOF = new ArrayList<String>(documentRootHarichical);

            Collections.reverse(listOF);
            documentTypeTreeHarichical= String.join(" > ", listOF);
        }catch (Exception e){
            e.printStackTrace();
        }
        return documentTypeTreeHarichical;
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
    @PreAuthorize("hasPermission(#uuid, 'BITSTREAM','WRITE')")
    @RequestMapping(method = RequestMethod.DELETE,
            consumes = {"application/json"},value = "/delete")
    public void deleteDocumentTypeTree(HttpServletRequest request, @PathVariable UUID uuid)
            throws SQLException, IOException, AuthorizeException {
        DocumentTypeTree documentTypeTree =null;
        try {
            Context context = ContextUtil.obtainContext(request);
            documentTypeTree = documentTypeTreeService.find(context, uuid);
            if (documentTypeTree == null) {
                throw new ResourceNotFoundException(
                        "The given uuid did not resolve to a community on the server: " + uuid);
            }
            if (documentTypeTree.getChildren().size() != 0) {
                throw new ResourceNotFoundException(
                        "child document found can not delete a penrent");
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
                if (documentTypeTree.getBitstream() != null) {
                    bitstreamService.delete(context, documentTypeTree.getBitstream());
                }
                documentTypeTreeService.delete(context, documentTypeTree);
                context.commit();
            }catch (Exception e){

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
