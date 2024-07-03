/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.Enum.DmsAction;
import org.dspace.app.rest.cis.CISServerImpl;
import org.dspace.app.rest.cis.models.CISProcess;
import org.dspace.app.rest.converter.CisResultConverter;
import org.dspace.app.rest.converter.ConverterService;
import org.dspace.app.rest.model.CisResultsRest;
import org.dspace.app.rest.model.hateoas.CisResultsResource;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.app.rest.utils.HttpHeadersInitializer;
import org.dspace.app.rest.utils.Utils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.BitstreamFormat;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.disseminate.service.CitationDocumentService;
import org.dspace.eperson.EPerson;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The controller for the api/discover endpoint
 */
@RestController
@RequestMapping("/api/" + CisResultsRest.CATEGORY)
public class CisRestController implements InitializingBean {

    private static final Logger log = LogManager.getLogger();

    @Autowired
    protected Utils utils;

    @Autowired
    private DiscoverableEndpointsService discoverableEndpointsService;

    @Autowired
    public CISServerImpl jbpmServer;
    @Autowired
    private ConfigurationService configurationService;
    @Autowired
    private ConverterService converter;
    @Autowired
    CisResultConverter cisResultConverter;
    @Autowired
    ItemService itemService;
    @Autowired
    BitstreamService bitstreamService;
    @Autowired
    private CitationDocumentService citationDocumentService;
    private static final int BUFFER_SIZE = 4096 * 10;
    @Override
    public void afterPropertiesSet() throws Exception {
        discoverableEndpointsService
                .register(this, Arrays.asList(Link.of("/api/" + CisResultsRest.CATEGORY, CisResultsRest.CATEGORY)));
    }

    /*@RequestMapping(method = RequestMethod.GET)
    public CisResultsRest getSearchSupport(@RequestParam(name = "jocode", required = true) String jocode,
                                                  @RequestParam(name = "date", required = true) String
                                                          date) {
       String causeListJson=jbpmServer.startProcess(new CISProcess(jocode,date));
       return  cisResultConverter.convert(causeListJson);
    }*/
    @RequestMapping(method = RequestMethod.GET)
    public CisResultsResource getSearchSupport(@RequestParam(name = "jocode", required = true) String jocode,
                                               @RequestParam(name = "date", required = true) String
                                                           date) {
        try{
            System.out.println("jocode");
           // String auth=jbpmServer.logintocisResapi(new CISProcess(jocode,date));
            String causeListJson=jbpmServer.startProcess(new CISProcess(jocode,date));
            CisResultsRest cisResultsRest =new CisResultsRest();
            cisResultsRest.setJsonStr(causeListJson);
            return converter.toResource(cisResultsRest);
        }catch (Exception e){
            e.printStackTrace();
        }
    return  null;

    }
    @RequestMapping(method = RequestMethod.GET, value = "searchCaseFile")
    @PreAuthorize("hasAuthority('ADMIN')")
    public CisResultsResource searchCaseFile(@RequestParam(name = "caseType", required = true) String caseType,
                                             @RequestParam(name = "caseNumber", required = true) String caseNumber,
                                             @RequestParam(name = "caseYear", required = true) String  caseYear,
                                             @RequestParam(name = "defected", required = true) boolean  defected) {
        try{
            System.out.println("caseType"+caseType);
            System.out.println("caseNumber"+caseType);
            System.out.println("caseYear"+caseType);
            String causeListJson=jbpmServer.searchCase(new CISProcess(caseType,caseNumber,caseYear,defected));
            CisResultsRest cisResultsRest =new CisResultsRest();
            cisResultsRest.setJsonStr(causeListJson);
            return converter.toResource(cisResultsRest);
        }catch (Exception e){
            e.printStackTrace();
        }
        return  null;

    }
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value = "downloadBitstreamByCino")
    public ResponseEntity downloadBitstreamByCino(@RequestParam @NotNull String cino, HttpServletResponse response,
                                                  HttpServletRequest request) throws IOException, SQLException, AuthorizeException {

        Context context = ContextUtil.obtainContext(request);
        EPerson currentUser = context.getCurrentUser();
        Bitstream bit= null;
        try {
            Item item = itemService.findByCIno(context, cino);
            if (item != null) {
                List<Bundle> bundles = itemService.getBundles(item, "MergeDoc");
                System.out.println("bundles.size()::::"+bundles.size());
                if (bundles.size() < 1) {
                    // not found, create a new one
                    // throw new IllegalArgumentException("MergeDoc not found");
                } else {
                    Bundle targetBundle = bundles.iterator().next();
                    Optional<Bitstream> bitstreamOption = targetBundle.getBitstreams().stream().findFirst();
                    if (bitstreamOption.isPresent()) {
                        bit = bitstreamOption.get();


                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if(bit == null){
            throw  new RuntimeException("Bitstream not found");
        }
        if (bit == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        Long lastModified = bitstreamService.getLastModified(bit);
        BitstreamFormat format = bit.getFormat(context);
        String mimetype = format.getMIMEType();
        String name = getBitstreamName(bit, format);

        if (StringUtils.isBlank(request.getHeader("Range"))) {
            //We only log a download request when serving a request without Range header. This is because
            //a browser always sends a regular request first to check for Range support.

        }

        try {
            long filesize = bit.getSizeBytes();
            Boolean citationEnabledForBitstream = citationDocumentService.isCitationEnabledForBitstream(bit, context);

            HttpHeadersInitializer httpHeadersInitializer = new HttpHeadersInitializer()
                    .withBufferSize(BUFFER_SIZE)
                    .withFileName(name)
                    .withLength(filesize)
                    .withChecksum(bit.getChecksum())
                    .withMimetype(mimetype)
                    .with(request)
                    .with(response);

            if (lastModified != null) {
                httpHeadersInitializer.withLastModified(lastModified);
            }

            //Determine if we need to send the file as a download or if the browser can open it inline
            long dispositionThreshold = configurationService.getLongProperty("webui.content_disposition_threshold");
            if (dispositionThreshold >= 0 && filesize > dispositionThreshold) {
                httpHeadersInitializer.withDisposition(HttpHeadersInitializer.CONTENT_DISPOSITION_ATTACHMENT);
            }


            org.dspace.app.rest.utils.BitstreamResource bitstreamResource =
                    new org.dspace.app.rest.utils.BitstreamResource(name, bit.getID(),
                            currentUser != null ? currentUser.getID() : null,
                            context.getSpecialGroupUuids(), citationEnabledForBitstream);
            DmsAction dmsAction = DmsAction.DOWNLOAD;
            dmsAction.setePerson(currentUser);
            dmsAction.setDocumentTypeTree(bit.getDocumentTypeTree());
            Optional<Bundle> bundleOptional = bit.getBundles().stream().findFirst();
            if (bundleOptional.isPresent()) {
                Optional<Item> itemOptional=bundleOptional.get().getItems().stream().findFirst();
                if(itemOptional.isPresent())
                    dmsAction.setItem(bit.getBundles().get(0).getItems().get(0));
            }
            System.out.println("capture event...");
            try {
                dmsAction.StoreDmsAction(context);
            }catch (Exception e){
                e.printStackTrace();
            }
            //We have all the data we need, close the connection to the database so that it doesn't stay open during
            //download/streaming
            context.complete();
            //Send the data
            if (httpHeadersInitializer.isValid()) {
                HttpHeaders httpHeaders = httpHeadersInitializer.initialiseHeaders();
                httpHeaders.setContentType(MediaType.APPLICATION_PDF);
                // Here you have to set the actual filename of your pdf
                String filename = cino;
                httpHeaders.setContentDispositionFormData(filename, filename);
                return ResponseEntity.ok().headers(httpHeaders).body(bitstreamResource);
            }

        } catch (ClientAbortException ex) {
            log.debug("Client aborted the request before the download was completed. " +
                    "Client is probably switching to a Range request.", ex);
        } catch (Exception e) {
            throw e;
        }
        return null;
    }
    @RequestMapping(method = RequestMethod.GET, value = "migrateCaseFile")
    @PreAuthorize("hasAuthority('ADMIN')")
    public CisResultsResource migrateCaseFile(@RequestParam(name = "cino", required = true) String cino,@RequestParam(name = "type", required = true) int type) {
        try{
            System.out.println("cino"+cino);
            String causeListJson=jbpmServer.migrate(new CISProcess(cino,type));
            CisResultsRest cisResultsRest =new CisResultsRest();
            cisResultsRest.setJsonStr(causeListJson);
            return converter.toResource(cisResultsRest);
        }catch (Exception e){
            e.printStackTrace();
        }
        return  null;

    }
    private String getBitstreamName(Bitstream bit, BitstreamFormat format) {
        String name = bit.getName();
        if (name == null) {
            // give a default name to the file based on the UUID and the primary extension of the format
            name = bit.getID().toString();
            if (format.getExtensions() != null && format.getExtensions().size() > 0) {
                name += "." + format.getExtensions().get(0);
            }
        }
        return name;
    }
}
