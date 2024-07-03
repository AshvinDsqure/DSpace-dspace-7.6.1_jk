/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import static org.dspace.app.rest.utils.ContextUtil.obtainContext;
import static org.dspace.app.rest.utils.RegexUtils.REGEX_REQUESTMAPPING_IDENTIFIER_AS_UUID;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.io.*;
import java.nio.file.*;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
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
import org.dspace.app.rest.converter.ConverterService;
import org.dspace.app.rest.exception.DSpaceBadRequestException;
import org.dspace.app.rest.model.BitstreamRest;
import org.dspace.app.rest.model.DigitalSignRequet;
import org.dspace.app.rest.model.hateoas.BitstreamResource;
import org.dspace.app.rest.repository.BundleRestRepository;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.app.rest.utils.DigitalSign;
import org.dspace.app.rest.utils.HttpHeadersInitializer;
import org.dspace.app.rest.utils.Utils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.BitstreamFormat;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.content.service.BitstreamFormatService;
import org.dspace.content.service.BitstreamService;
import org.dspace.core.Context;
import org.dspace.disseminate.service.CitationDocumentService;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.eperson.service.GroupService;
import org.dspace.services.ConfigurationService;
import org.dspace.services.EventService;
import org.dspace.usage.UsageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * This is a specialized controller to provide access to the bitstream binary
 * content
 *
 * The mapping for requested endpoint try to resolve a valid UUID, for example
 * <pre>
 * {@code
 * https://<dspace.server.url>/api/core/bitstreams/26453b4d-e513-44e8-8d5b-395f62972eff/content
 * }
 * </pre>
 *
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 * @author Tom Desair (tom dot desair at atmire dot com)
 * @author Frederic Van Reet (frederic dot vanreet at atmire dot com)
 */
@RestController
@RequestMapping("/api/" + BitstreamRest.CATEGORY + "/" + BitstreamRest.PLURAL_NAME
    + REGEX_REQUESTMAPPING_IDENTIFIER_AS_UUID)
public class BitstreamRestController {

    private static final Logger log = org.apache.logging.log4j.LogManager
            .getLogger(BitstreamRestController.class);

    //Most file systems are configured to use block sizes of 4096 or 8192 and our buffer should be a multiple of that.
    private static final int BUFFER_SIZE = 4096 * 10;
    @Autowired
    public static BundleRestRepository bundleRestRepository;
    @Autowired
    private BitstreamService bitstreamService;

    @Autowired
    BitstreamFormatService bitstreamFormatService;

    @Autowired
    private EventService eventService;

    @Autowired
    private CitationDocumentService citationDocumentService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    ConverterService converter;
    @Autowired
    private GroupService groupService;
    @Autowired
    DigitalSign digitalSign;



    @Autowired
    Utils utils;

    @PreAuthorize("hasPermission(#uuid, 'BITSTREAM', 'READ')")
    @RequestMapping( method = {RequestMethod.GET, RequestMethod.HEAD}, value = "content")
    public ResponseEntity retrieve(@PathVariable UUID uuid, HttpServletResponse response,
                         HttpServletRequest request) throws IOException, SQLException, AuthorizeException {


        Context context = ContextUtil.obtainContext(request);

        Bitstream bit = bitstreamService.find(context, uuid);
        EPerson currentUser = context.getCurrentUser();

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
            eventService.fireEvent(
                new UsageEvent(
                    UsageEvent.Action.VIEW,
                    request,
                    context,
                    bit));
        }

        try {
            long filesize = bit.getSizeBytes();
            Boolean citationEnabledForBitstream = citationDocumentService.isCitationEnabledForBitstream(bit, context);

            HttpHeadersInitializer httpHeadersInitializer = new HttpHeadersInitializer()
                .withBufferSize(BUFFER_SIZE)
                .withFileName(name)
                .withChecksum(bit.getChecksum())
                .withMimetype(mimetype)
                .with(request)
                .with(response);

            if (lastModified != null) {
                httpHeadersInitializer.withLastModified(lastModified);
            }

            //Determine if we need to send the file as a download or if the browser can open it inline
            //The file will be downloaded if its size is larger than the configured threshold,
            //or if its mimetype/extension appears in the "webui.content_disposition_format" config
            long dispositionThreshold = configurationService.getLongProperty("webui.content_disposition_threshold");
            if ((dispositionThreshold >= 0 && filesize > dispositionThreshold)
                    || checkFormatForContentDisposition(format)) {
                httpHeadersInitializer.withDisposition(HttpHeadersInitializer.CONTENT_DISPOSITION_ATTACHMENT);
            }

            org.dspace.app.rest.utils.BitstreamResource bitstreamResource =
                new org.dspace.app.rest.utils.BitstreamResource(name, uuid,
                    currentUser != null ? currentUser.getID() : null,
                    context.getSpecialGroupUuids(), citationEnabledForBitstream);
            DmsAction dmsAction = DmsAction.VIEWDOC;

            dmsAction.setePerson(currentUser);
            dmsAction.setDsDmsObject(DmsObject.bitstream);
            dmsAction.setDocumentTypeTree(bit.getDocumentTypeTree());
            Optional<Bundle> bundleOptional = bit.getBundles().stream().findFirst();
            if (bundleOptional.isPresent()) {
                Optional<Item> itemOptional=bundleOptional.get().getItems().stream().findFirst();
                if(itemOptional.isPresent())
                    dmsAction.setItem(bit.getBundles().get(0).getItems().get(0));
            }
            System.out.println("capture event...");
            try {
                dmsAction.setTitle(bit.getName());
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

    private String getBitstreamName(Bitstream bit, BitstreamFormat format) {
        String name = bit.getName();
        if (name == null) {
            // give a default name to the file based on the UUID and the primary extension of the format
            name = bit.getID().toString();
            if (format != null && format.getExtensions() != null && format.getExtensions().size() > 0) {
                name += "." + format.getExtensions().get(0);
            }
        }
        return name;
    }

    private boolean isNotAnErrorResponse(HttpServletResponse response) {
        Response.Status.Family responseCode = Response.Status.Family.familyOf(response.getStatus());
        return responseCode.equals(Response.Status.Family.SUCCESSFUL)
            || responseCode.equals(Response.Status.Family.REDIRECTION);
    }

    private boolean checkFormatForContentDisposition(BitstreamFormat format) {
        // never automatically download undefined formats
        if (format == null) {
            return false;
        }
        List<String> formats = List.of((configurationService.getArrayProperty("webui.content_disposition_format")));
        boolean download = formats.contains(format.getMIMEType());
        if (!download) {
            for (String ext : format.getExtensions()) {
                if (formats.contains(ext)) {
                    download = true;
                    break;
                }
            }
        }
        return download;
    }

    /**
     * This method will update the bitstream format of the bitstream that corresponds to the provided bitstream uuid.
     *
     * @param uuid The UUID of the bitstream for which to update the bitstream format
     * @param request  The request object
     * @return The wrapped resource containing the bitstream which in turn contains the bitstream format
     * @throws SQLException       If something goes wrong in the database
     */
    @RequestMapping(method = PUT, consumes = {"text/uri-list"}, value = "format")
    @PreAuthorize("hasPermission(#uuid, 'BITSTREAM','WRITE')")
    @PostAuthorize("returnObject != null")
    public BitstreamResource updateBitstreamFormat(@PathVariable UUID uuid,
                                                   HttpServletRequest request) throws SQLException {

        Context context = obtainContext(request);

        List<BitstreamFormat> bitstreamFormats = utils.constructBitstreamFormatList(request, context);

        if (bitstreamFormats.size() > 1) {
            throw new DSpaceBadRequestException("Only one bitstream format is allowed");
        }

        BitstreamFormat bitstreamFormat = bitstreamFormats.stream().findFirst()
                .orElseThrow(() -> new DSpaceBadRequestException("No valid bitstream format was provided"));

        Bitstream bitstream = bitstreamService.find(context, uuid);

        if (bitstream == null) {
            throw new ResourceNotFoundException("Bitstream with id: " + uuid + " not found");
        }

        bitstream.setFormat(context, bitstreamFormat);

        context.commit();

        BitstreamRest bitstreamRest = converter.toRest(context.reloadEntity(bitstream), utils.obtainProjection());
        return converter.toResource(bitstreamRest);
    }
    PDDocument addSignature(PDDocument pdDocument, String p12File, String pwd) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        String password = "badssl.com";
        Boolean showSignature = true;
        String reason = "Digital Copy.";
        String location = "Bihar";
        String name = "High Court of Bihar. ";
        Integer pageNumber = 0;
        PrivateKey privateKey = null;
        X509Certificate cert = null;
        if (p12File != null) {
            System.out.println("2");
            KeyStore ks = KeyStore.getInstance("PKCS12");
            FileInputStream fileInputStream = new FileInputStream(p12File);
            byte[] byteArray = new byte[fileInputStream.available()];
            // Step 3: Read the file data into the byte array
            fileInputStream.read(byteArray);
            ks.load(new ByteArrayInputStream(byteArray), password.toCharArray());
            String alias = ks.aliases().nextElement();
            if (!ks.isKeyEntry(alias)) {
                throw new IllegalArgumentException("The provided PKCS12 file does not contain a private key.");
            }
            privateKey = (PrivateKey) ks.getKey(alias, password.toCharArray());
            cert = (X509Certificate) ks.getCertificate(alias);
        }
        PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE); // default filter
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_SHA1);
        signature.setName(name);
        signature.setLocation(location);
        signature.setReason(reason);
        signature.setSignDate(Calendar.getInstance());

        // Load the PDF
        try{
            SignatureOptions signatureOptions = new SignatureOptions();

            // If you want to show the signature

            // ATTEMPT 2
            if (showSignature != null && showSignature) {
                System.out.println("document.getNumberOfPages()::"+pdDocument.getNumberOfPages());
                PDPage page = pdDocument.getPage(pdDocument.getNumberOfPages() - 1);

                PDAcroForm acroForm = pdDocument.getDocumentCatalog().getAcroForm();
                if (acroForm == null) {
                    acroForm = new PDAcroForm(pdDocument);
                    pdDocument.getDocumentCatalog().setAcroForm(acroForm);
                }

                // Create a new signature field and widget

                PDSignatureField signatureField = new PDSignatureField(acroForm);
                PDAnnotationWidget widget = signatureField.getWidgets().get(0);
                PDRectangle rect = new PDRectangle(100, 100, 300, 100); // Define the rectangle size here
                widget.setRectangle(rect);
                page.getAnnotations().add(widget);

                // Set the appearance for the signature field
                PDAppearanceDictionary appearanceDict = new PDAppearanceDictionary();
                PDAppearanceStream appearanceStream = new PDAppearanceStream(pdDocument);
                appearanceStream.setResources(new PDResources());
                appearanceStream.setBBox(rect);
                appearanceDict.setNormalAppearance(appearanceStream);
                widget.setAppearance(appearanceDict);

                try (PDPageContentStream contentStream = new PDPageContentStream(pdDocument, appearanceStream)) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.newLineAtOffset(110, 130);
                    contentStream.showText("Digitally signed by: " + (name != null ? name : "Unknown"));
                    contentStream.newLineAtOffset(0, -15);
                    contentStream.showText("Date: " + new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z").format(new Date()));
                    contentStream.newLineAtOffset(0, -15);
                    if (reason != null && !reason.isEmpty()) {
                        contentStream.showText("Reason: " + reason);
                        contentStream.newLineAtOffset(0, -13);
                    }
                    if (location != null && !location.isEmpty()) {
                        contentStream.showText("Location: " + location);
                        contentStream.newLineAtOffset(0, -15);
                    }
                    contentStream.endText();
                }

                // Add the widget annotation to the page
                page.getAnnotations().add(widget);

                // Add the signature field to the acroform
                acroForm.getFields().add(signatureField);

                // Handle multiple signatures by ensuring a unique field name
                String baseFieldName = "Signature";
                String signatureFieldName = baseFieldName;
                int suffix = 1;
                while (acroForm.getField(signatureFieldName) != null) {
                    suffix++;
                    signatureFieldName = baseFieldName + suffix;
                }
                signatureField.setPartialName(signatureFieldName);
            }

            pdDocument.addSignature(signature, signatureOptions);
            // External signing
            ExternalSigningSupport externalSigning = pdDocument
                    .saveIncrementalForExternalSigning(new ByteArrayOutputStream());

            byte[] content = IOUtils.toByteArray(externalSigning.getContent());

            // Using BouncyCastle to sign
            CMSTypedData cmsData = new CMSProcessableByteArray(content);

            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            BouncyCastleProvider d=new BouncyCastleProvider();
            d.getProperty(BouncyCastleProvider.PROVIDER_NAME);
            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME).build(privateKey);

            gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                    new JcaDigestCalculatorProviderBuilder().setProvider(BouncyCastleProvider.PROVIDER_NAME).build())
                    .build(signer, cert));

            gen.addCertificates(new JcaCertStore(Collections.singletonList(cert)));
            CMSSignedData signedData = gen.generate(cmsData, false);

            byte[] cmsSignature = signedData.getEncoded();

            externalSigning.setSignature(cmsSignature);


            // After setting the signature, return the resultant PDF
            return pdDocument;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD}, value = "smartviewcontent")
    public ResponseEntity smartviewretrieve(@PathVariable UUID uuid, @RequestParam @NotNull String caseType, @RequestParam @NotNull String caseYear, @RequestParam @NotNull String caseNumber, HttpServletResponse response,
                                            HttpServletRequest request) throws Exception {


        Context context = ContextUtil.obtainContext(request);

        Bitstream bit = bitstreamService.find(context, uuid);
        EPerson currentUser = context.getCurrentUser();
        Group group = groupService.findByName(context, "Mergepdf download");
        Boolean isDirectMember = groupService.isDirectMember(group, currentUser);
        if (!isDirectMember) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        if (bit == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        try  {
            DigitalSignRequet digitalSignRequet = new DigitalSignRequet();
            InputStream pkcs12File = null;
            InputStream certFile = null;
            InputStream fileInputa = null;
            Bitstream bitstream = null;
            String certType = configurationService.getProperty("digital.sign.certtype");
            String password = configurationService.getProperty("digital.sign.password");
            String showSignature = configurationService.getProperty("digital.sign.showsignature");
            String reason = configurationService.getProperty("digital.sign.reason");
            String location = configurationService.getProperty("digital.sign.location");
            String name = configurationService.getProperty("digital.sign.name");
            String pageNumber = configurationService.getProperty("digital.sign.pagenumber");
            File p12 = new File(configurationService.getProperty("digital.sign.p12File"));
            File cert = new File(configurationService.getProperty("digital.sign.p12File"));
            pkcs12File = new FileInputStream(p12);
            certFile = new FileInputStream(cert);
            if (cert != null) {
                digitalSignRequet.setCertFileName(cert.getName());
            }
            if (p12 != null) {
                digitalSignRequet.setP12FileName(p12.getName());
            }
            if (bit != null) {
                digitalSignRequet.setFileInputName(bit.getName());
                fileInputa = bitstreamService.retrieve(context, bit);
                if (fileInputa != null) {
                    digitalSignRequet.setFileInput(fileInputa);
                }
            }
            System.out.println("bitstreamid :" + bit.getID());
            System.out.println("certFile :" + certFile);
            System.out.println("pdf :" + fileInputa);
            System.out.println("p12File :" + pkcs12File);
            System.out.println("password :" + password);
            System.out.println("certType :" + certType);
            System.out.println("location :" + location);
            System.out.println("pageNumber :" + pageNumber);
            System.out.println("reason :" + reason);
            System.out.println("name :" + name);
            System.out.println("p12.getName() :" + p12.getName());
            System.out.println("cert.getName() :" + cert.getName());
            if (!isNullOrEmptyOrBlank(certType)) {
                digitalSignRequet.setCertType(certType);
            }
            if (!isNullOrEmptyOrBlank(password)) {
                digitalSignRequet.setPassword(password);
            }
            if (!isNullOrEmptyOrBlank(showSignature)) {
                digitalSignRequet.setShowSignature(showSignature);
            }
            if (!isNullOrEmptyOrBlank(reason)) {
                digitalSignRequet.setReason(reason);
            }
            if (!isNullOrEmptyOrBlank(location)) {
                digitalSignRequet.setLocation(location);
            }
            if (!isNullOrEmptyOrBlank(name)) {
                digitalSignRequet.setName(name);
            }
            if (!isNullOrEmptyOrBlank(pageNumber)) {
                digitalSignRequet.setPageNumber(pageNumber);
            }
            if (pkcs12File != null) {
                digitalSignRequet.setP12File(pkcs12File);
            }
            if (certFile != null) {
                digitalSignRequet.setCertFile(certFile);
            }
            digitalSignData(context,digitalSignRequet,bit);
            //InputStream fileInputStream = Files.newInputStream(tempPath);
            //bitstreamService.createWithoutBundleEditBitstream(context, fileInputStream,bit);
            // fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Long lastModified = bitstreamService.getLastModified(bit);
        BitstreamFormat format = bit.getFormat(context);
        String mimetype = format.getMIMEType();
        String name = getBitstreamName(bit, format);

        if (StringUtils.isBlank(request.getHeader("Range"))) {
            //We only log a download request when serving a request without Range header. This is because
            //a browser always sends a regular request first to check for Range support.
            eventService.fireEvent(
                    new UsageEvent(
                            UsageEvent.Action.VIEW,
                            request,
                            context,
                            bit));
        }

        try {
            long filesize;
            filesize = bit.getSizeBytes();
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

            Boolean citationEnabledForBitstream = citationDocumentService.isCitationEnabledForBitstream(bit, context);
            org.dspace.app.rest.utils.BitstreamResource bitstreamResource =
                    new org.dspace.app.rest.utils.BitstreamResource(name, uuid,
                            currentUser != null ? currentUser.getID() : null,
                            context.getSpecialGroupUuids(), citationEnabledForBitstream);
            DmsAction dmsAction = DmsAction.DOWNLOAD;
            dmsAction.setePerson(currentUser);
            dmsAction.setDsDmsObject(DmsObject.bitstream);
            dmsAction.setDocumentTypeTree(bit.getDocumentTypeTree());
            Optional<Bundle> bundleOptional = bit.getBundles().stream().findFirst();
            if (bundleOptional.isPresent()) {
                Optional<Item> itemOptional=bundleOptional.get().getItems().stream().findFirst();
                if(itemOptional.isPresent())
                    dmsAction.setItem(bit.getBundles().get(0).getItems().get(0));
            }
            System.out.println("capture event...");
            try {
                dmsAction.setTitle(bit.getName());
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
                String filename = caseType + "_" + caseNumber + "_" + caseYear;
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
    public static boolean isNullOrEmptyOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    public  Map<String, String> digitalSignData(Context context, DigitalSignRequet requestModel, Bitstream bitstream) {
        final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
        File tempsingpdf = new File(TEMP_DIRECTORY, "sign" + ".pdf");
        if (!tempsingpdf.exists()) {
            try {
                tempsingpdf.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        HttpClient httpClient = HttpClients.createDefault();
        try {
            //String url = "http://localhost:8081/api/v1/security/cert-sign";
            String url = "http://localhost:8084/api/v1/security/cert-sign";
            HttpPost httpPost = new HttpPost(url);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            // Add parameters as form data
            builder.addTextBody("certType", requestModel.getCertType(), ContentType.TEXT_PLAIN);
            builder.addTextBody("showSignature", requestModel.getShowSignature(), ContentType.TEXT_PLAIN);
            builder.addTextBody("location", requestModel.getLocation(), ContentType.TEXT_PLAIN);
            builder.addTextBody("reason", requestModel.getReason(), ContentType.TEXT_PLAIN);
            builder.addTextBody("pageNumber", requestModel.getPageNumber(), ContentType.TEXT_PLAIN);
            builder.addTextBody("name", requestModel.getName(), ContentType.TEXT_PLAIN);
            builder.addTextBody("password", requestModel.getPassword(), ContentType.TEXT_PLAIN);
            // Add a binary file
            builder.addBinaryBody("fileInput", requestModel.getFileInput(), ContentType.APPLICATION_OCTET_STREAM, requestModel.getFileInputName());
            builder.addBinaryBody("p12File", requestModel.getP12File(), ContentType.APPLICATION_OCTET_STREAM, requestModel.getP12FileName());
            builder.addBinaryBody("certFile", requestModel.getCertFile(), ContentType.APPLICATION_OCTET_STREAM, requestModel.getCertFileName());
            // Build the multipart entity
            httpPost.setEntity(builder.build());
            // Execute the request
            try {
                // Execute the request and get the response
                HttpResponse response = httpClient.execute(httpPost);
                System.out.println("Response :::::::::::::" + response);
                // Check the response status code and content
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
                    HttpHeaders headers = new HttpHeaders();
                    for (org.apache.http.Header header : response.getAllHeaders()) {
                        headers.add(header.getName(), header.getValue());
                    }
                    byte[] responseBody = EntityUtils.toByteArray(entity);
                    byte[] s = responseBody;
                    try (FileOutputStream fos = new FileOutputStream(new File(tempsingpdf.getAbsolutePath()))) {
                        fos.write(responseBody);
                        fos.close();
                        fos.flush();
                    }
                    System.out.println("file path" + tempsingpdf.getAbsolutePath());
                    FileInputStream pdfFileInputStream = new FileInputStream(new File(tempsingpdf.getAbsolutePath()));
                    System.out.println("context"+context);
                    System.out.println("pdfFileInputStream"+pdfFileInputStream);
                    System.out.println("name"+bitstream.getName());
                    System.out.println("bundleRestRepository"+bundleRestRepository);
                    Bitstream bitstreampdfsing = bitstreamService.createWithoutBundleEditBitstream(context, pdfFileInputStream,bitstream);
                    //Bitstream bitstreampdfsing = bundleRestRepository.processBitstreamCreationWithoutBundleEditBitstream(context, pdfFileInputStream, "", bitstream.getName(), bitstream);
                    if (bitstreampdfsing != null) {
                        Map<String, String> map = new HashMap<>();
                        map.put("bitstreampid", bitstreampdfsing.getID().toString());
                        System.out.println("Sing Doc Paths::" + tempsingpdf.getAbsolutePath());
                        //context.commit();
                        return map;
                    }
                    // Process the response content here
                } else {
                    System.out.println("errot with " + statusCode);
                    HttpEntity entity = response.getEntity();
                    byte[] responseBody = EntityUtils.toByteArray(entity);
                    return null;

                }
            } catch (IOException e) {
                System.out.println("error" + e.getMessage());
                e.printStackTrace();

            }
        } catch (Exception e) {
            System.out.println("error" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

}
