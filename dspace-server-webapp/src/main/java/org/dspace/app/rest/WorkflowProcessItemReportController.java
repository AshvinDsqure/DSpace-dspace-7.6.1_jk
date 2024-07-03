/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.layout.font.FontProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PageMode;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.dspace.app.rest.model.ExcelDTO;
import org.dspace.app.rest.model.ItemRest;
import org.dspace.app.rest.model.PDFDataDTO;
import org.dspace.app.rest.repository.BundleRestRepository;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.app.rest.utils.DigitalSingPDF;
import org.dspace.app.rest.utils.ExcelHelper;
import org.dspace.app.rest.utils.Utils;
import org.dspace.content.Bitstream;
import org.dspace.content.DocumentTypeTree;
import org.dspace.content.Item;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.DocumentTypeTreeService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Path;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Controller to upload bitstreams to a certain bundle, indicated by a uuid in the request
 * Usage: POST /api/core/bundles/{uuid}/bitstreams (with file and properties of file in request)
 * Example:
 * <pre>
 * {@code
 * curl https://<dspace.server.url>/api/core/bundles/d3599177-0408-403b-9f8d-d300edd79edb/bitstreams
 *  -XPOST -H 'Content-Type: multipart/form-data' \
 *  -H 'Authorization: Bearer eyJhbGciOiJI...' \
 *  -F "file=@Downloads/test.html" \
 *  -F 'properties={ "name": "test.html", "metadata": { "dc.description": [ { "value": "example file", "language": null,
 *          "authority": null, "confidence": -1, "place": 0 } ]}, "bundleName": "ORIGINAL" };type=application/json'
 * }
 * </pre>
 */
@RestController
@RequestMapping("/api/" + ItemRest.CATEGORY + "/" + ItemRest.PLURAL_NAME
        + "/report")
public class WorkflowProcessItemReportController {

    private static final Logger log = LogManager.getLogger();

    @Autowired
    protected Utils utils;
    @Autowired
    protected ItemService itemService;
    @Autowired
    ConfigurationService configurationService;
    @Autowired
    BitstreamService bitstreamService;

    @Autowired
    DocumentTypeTreeService documentTypeTreeService;

    @Autowired
    private BundleRestRepository bundleRestRepository;


    /**
     * Method to upload a Bitstream to a Bundle with the given UUID in the URL. This will create a Bitstream with the
     * file provided in the request and attach this to the Item that matches the UUID in the URL.
     * This will only work for uploading one file, any extra files will silently be ignored
     *
     * @return The created BitstreamResource
     */
    @RequestMapping(method = RequestMethod.GET, value = "/downloadItemReport")
    public ResponseEntity<Resource> downloadItem(HttpServletRequest request,
                                                 @Parameter(value = "startdate", required = true) String startdate,
                                                 @Parameter(value = "enddate", required = true) String enddate) {
        try {
            Context context = ContextUtil.obtainContext(request);
            String filename = "ProductivityReport.xlsx";
            List<Item> list = itemService.getDataTwoDateRangeDownload(context, startdate, enddate);
            System.out.println("size" + list.size());
            List<ExcelDTO> listDTo = list.stream().map(i -> {
                String title = itemService.getMetadataFirstValue(i, "dc", "title", null, null);
                String type = itemService.getMetadataFirstValue(i, "casefile", "case", "typename", null);
                String bundlename = itemService.getMetadataFirstValue(i, "casefile", "Bundle", "name", null);
                String nature = itemService.getMetadataFirstValue(i, "casefile", "case", "nature", null);
                String issued = itemService.getMetadataFirstValue(i, "casefile", "case", "registrationyear", null);
                String NoOfPages = itemService.getMetadataFirstValue(i, "casefile", "digitization", "NoOfPages", null);
                type = (type != null) ? type : "-";
                title = (title != null) ? title : "-";
                issued = (issued != null) ? issued : "-";
                nature = (nature != null) ? nature : "-";
                NoOfPages = (NoOfPages != null) ? NoOfPages : "-";
                String caseDetail = nature + "/" + title + "/" + issued;
                String uploaddate = itemService.getMetadataFirstValue(i, "dc", "date", "accessioned", null);
                uploaddate = (uploaddate != null) ? uploaddate : "-";
                String uploadedby = i.getSubmitter().getEmail();
                String email = (context.getCurrentUser() != null) ? context.getCurrentUser().getEmail() : "-";
                return new ExcelDTO(title, type, issued, caseDetail, uploaddate, uploadedby, NoOfPages, email, bundlename, type);
            }).collect(Collectors.toList());
            ByteArrayInputStream in = ExcelHelper.tutorialsToExcel(listDTo);
            InputStreamResource file = new InputStreamResource(in);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    @RequestMapping(method = RequestMethod.GET, value = "/downloadPDFMrged")
    public ResponseEntity<InputStreamResource> downloadPDFMrged(HttpServletRequest request,
                                                                @Parameter(value = "bitstreamuuids", required = true) String bitstreamuuids) {
        System.out.println(":::::::::::::downloadPDFMrged::::::::::::IN with id is :"+bitstreamuuids);
        try {
            Context context = ContextUtil.obtainContext(request);
            List<InputStream> Notes = new ArrayList<>();
            InputStream note = null;
            FileInputStream pdfFristPAge = null;
            Item item=null;
           Bitstream bitstream2certificate=null;
            ByteArrayInputStream byteArrayInputStream = null;
            PDFDataDTO pdfDataDTO=new PDFDataDTO();
            pdfDataDTO.setTodate(DateFormateDDMMYYYY(new Date()));
            pdfDataDTO.setDownlodedby(context.getCurrentUser().getFullName());
            final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");
            File output = new File(TEMP_DIRECTORY, "downloadPDFMrged.pdf");
            if (!output.exists()) {
                try {
                    output.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            try {
                if(bitstreamuuids.contains(",")){
                String[] bitstreamuuidsarray = bitstreamuuids.split(",");
                System.out.println("total Download pdf "+bitstreamuuidsarray.length);
                int i=0;
                for (String id:bitstreamuuidsarray) {
                   Bitstream bitstream=bitstreamService.find(context,UUID.fromString(id));
                   if(bitstream!=null){
                   DocumentTypeTree documentTypeTree= documentTypeTreeService.getAllDocumentTypeByBitstreamID(context,bitstream);
                       System.out.println("in bitstream for");
                       note = bitstreamService.retrieve(context, bitstream);
                       Notes.add(note);
                   if(documentTypeTree!=null){
                       item=documentTypeTree.getItem();
                       System.out.println("in documentTypeTree for");
                       if(documentTypeTree.getItem()!=null&&documentTypeTree.getItem().getSubmitter()!=null&&documentTypeTree.getItem().getSubmitter().getFullName()!=null) {
                           pdfDataDTO.setUplodatedBY(documentTypeTree.getItem().getSubmitter().getFullName());
                       }else{
                           pdfDataDTO.setUplodatedBY("-");
                       }
                       if(documentTypeTree.getItem()!=null&&documentTypeTree.getItem().getLastModified()!=null){
                           System.out.println("from date"+documentTypeTree.getItem().getLastModified());
                           pdfDataDTO.setFromDate(DateFormateDDMMYYYY(documentTypeTree.getItem().getLastModified()));
                       }
                        if(documentTypeTree.getDesc()!=null){
                            System.out.println("::::::::::::::::documentTypeTree.getDescription();:::"+documentTypeTree.getDesc());
                        pdfDataDTO.getUplodateDocname().add(documentTypeTree.getDesc());}
                   }else {
                       System.out.println(":::::::::for loop:::documentTypeTree not found ::::::::::");
                   }
                   }else {
                       System.out.println(":::for loop:::::::: bitstream Not found::::::::::::");
                   }
                i++;
                }
                }else{
                    Bitstream bitstream=bitstreamService.find(context,UUID.fromString(bitstreamuuids));
                    if(bitstream!=null){
                        note = bitstreamService.retrieve(context, bitstream);
                        Notes.add(note);
                        DocumentTypeTree documentTypeTree= documentTypeTreeService.getAllDocumentTypeByBitstreamID(context,bitstream);
                        if(documentTypeTree!=null){
                            item=documentTypeTree.getItem();
                           System.out.println("in documentTypeTree single");
                            if(documentTypeTree.getItem()!=null&&documentTypeTree.getItem().getSubmitter()!=null&&documentTypeTree.getItem().getSubmitter().getFullName()!=null) {
                                pdfDataDTO.setUplodatedBY(documentTypeTree.getItem().getSubmitter().getFullName());
                            }else{
                                pdfDataDTO.setUplodatedBY("-");
                            }
                            if(documentTypeTree.getItem()!=null&&documentTypeTree.getItem().getLastModified()!=null){
                                System.out.println("from date ss"+documentTypeTree.getItem().getLastModified());
                                pdfDataDTO.setFromDate(DateFormateDDMMYYYY(documentTypeTree.getItem().getLastModified()));
                            }
                            if(documentTypeTree.getParent()!=null&&documentTypeTree.getParent().getDocumentType()!=null&&documentTypeTree.getParent().getDocumentType().getDocumenttypename()!=null){
                                System.out.println("::::::::::::::::getDocumenttypename:::"+documentTypeTree.getParent().getDocumentType().getDocumenttypename());
                                pdfDataDTO.getUplodateDocname().add(documentTypeTree.getParent().getDocumentType().getDocumenttypename());}
                        }else {
                            System.out.println("::::in single::::::::::::getDocumenttype::not found");
                        }
                    }else {
                        System.out.println(":::in single:::in bitstream::::::::::::getDocumenttype::not found");
                    }
                }
                if (item!=null){
                    System.out.println("item find");
                    DocumentTypeTree perent=documentTypeTreeService.find(context,UUID.fromString("034788d5-1378-4a5e-a508-549febd0aaae"));
                    // local DocumentTypeTree perent=documentTypeTreeService.find(context,UUID.fromString("41f61697-baec-4567-a663-78507b0fa1eb"));
                    if(perent!=null){
                        System.out.println("perent find");
                        DocumentTypeTree documentTypecertificate= documentTypeTreeService.getByPerentidanditemidID(context,item,perent);
                        if(documentTypecertificate!=null){
                            System.out.println("certificate perent find");
                            bitstream2certificate=documentTypecertificate.getBitstream();
                        }
                    }
                }
                File certificatenew = new File(TEMP_DIRECTORY, "certificatenew.pdf");
                if (!certificatenew.exists()) {
                    try {
                        certificatenew.createNewFile();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                File tabledata = new File(TEMP_DIRECTORY, "tabledata.pdf");
                if (!tabledata.exists()) {
                    try {
                        tabledata.createNewFile();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                File tabledatasing = new File(TEMP_DIRECTORY, "tabledatasing.pdf");
                if (!tabledatasing.exists()) {
                    try {
                        tabledatasing.createNewFile();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                File tabledatasing2 = new File(TEMP_DIRECTORY, "tabledatasing2.pdf");
                if (!tabledatasing2.exists()) {
                    try {
                        tabledatasing2.createNewFile();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                //Add new Ganarate  certificate new
                InputStream inputStream=  ganratecertificatenew(context,certificatenew,pdfDataDTO);
                if(inputStream!=null)
                {
                    String p12File = configurationService.getProperty("certificate.digital.sign.p12File");
                    String p12Filepass = configurationService.getProperty("certificate.digital.sign.password");
                    DigitalSingPDF.AddDigitalSignClient(certificatenew.getAbsolutePath(),tabledatasing2.getAbsolutePath(),pdfDataDTO.getDownlodedby(),"Patna.",p12File,p12Filepass,2);
                    FileInputStream inputStream2 = new FileInputStream(new File(tabledatasing2.getAbsolutePath()));
                    if (inputStream2 != null) {
                        Notes.add(inputStream2);
                    }
                }
                //ADD existing certificate
                if(bitstream2certificate!=null){
                  InputStream certificate= bitstreamService.retrieve(context, bitstream2certificate);
                    if (certificate != null) {
                        Notes.add(certificate);
                    }
                }
                //generateTableData
                InputStream inputStream1=generateTableData(context,tabledata,pdfDataDTO);
                if(inputStream1!=null){
                    String p12File = configurationService.getProperty("certificate.digital.sign.p12File");
                    String p12Filepass = configurationService.getProperty("certificate.digital.sign.password");
                    DigitalSingPDF.AddDigitalSignClient(tabledata.getAbsolutePath(),tabledatasing.getAbsolutePath(),pdfDataDTO.getUplodatedBY(),"Patna.",p12File,p12Filepass,1);
                   // DigitalSingPDF.AddDigitalSignClient(tabledatasing.getAbsolutePath(),tabledatasing2.getAbsolutePath(),pdfDataDTO.getDownlodedby(),"Patna.",p12File,p12Filepass,2);
                    FileInputStream inputStream2 = new FileInputStream(new File(tabledatasing.getAbsolutePath()));
                    if (inputStream2 != null) {
                        Notes.add(inputStream2);
                    }
                }
                System.out.println("tabledatasing.getAbsolutePath():::"+tabledatasing2.getAbsolutePath());
                OutputStream out = new FileOutputStream(new File(output.getAbsolutePath()));
                mergePdfFiles(Notes, out);
                FileInputStream outputfile = new FileInputStream(new File(output.getAbsolutePath()));
                System.out.println("Final Mrged PDF :"+output.getAbsolutePath());


                File downloadPDFMrgeddoc = new File(TEMP_DIRECTORY, "downloadPDFMrgeddoc.pdf");
                if (!downloadPDFMrgeddoc.exists()) {
                    try {
                        downloadPDFMrgeddoc.createNewFile();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                saveBookmarkOntemp(outputfile,downloadPDFMrgeddoc.getAbsolutePath());
                FileInputStream watermark = new FileInputStream(new File(downloadPDFMrgeddoc.getAbsolutePath()));
                byte[] data = readStream(watermark);
                ByteArrayResource resource = new ByteArrayResource(data);
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=downloadPDFMrged.pdf");
                return ResponseEntity.ok()
                        .headers(headers)
                        .contentLength(data.length)
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(new InputStreamResource(resource.getInputStream()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public InputStream generateTableData(Context context, File tempFile1html,PDFDataDTO pdfDataDTO) throws
            Exception {

        boolean isTextEditorFlow = false;
        StringBuffer sb = new StringBuffer("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "<title>Single Border Table</title>\n" +
                "<style>\n" +
                "    table {\n" +
                "        border-collapse: collapse;\n" +
                "    }\n" +
                "\n" +
                "    td {\n" +
                "        border: 1px solid black;\n" +
                "        padding: 2px;\n" +
                "\t\twidth:50%;\n" +
                "\t\t    padding-right: 150px;\n" +
                "    }\n" +
                ".a{\n" +
                "\t  width:100%;\n" +
                "\t}\n" +
                "\t.b{\n" +
                "\t  width:30%;\n" +
                "\t  float:left;\n" +
                "\t  text-align:left;\n" +
                "\t}\n" +
                "\t.c{\n" +
                "\t  width:30%;\n" +
                "\t  float:right;\n" +
                "\t    text-align:left;\n" +
                "\t}"+
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<center>\n" +
                "<table border=\"1\">");
        //Form Date
        sb.append("<tr>");
        sb.append("<td>Date of Upload in DMS </td>");
        sb.append("<td>"+pdfDataDTO.getFromDate()+"</td>");
        sb.append("</tr>");
        //To Date
        sb.append("<tr>");
        sb.append("<td>Upload by</td>");
        sb.append("<td>"+pdfDataDTO.getUplodatedBY()+"</td>");
        sb.append("</tr>");
     //uploded by
        sb.append("<tr>");
        sb.append("<td>Download Date</td>");
        sb.append("<td>"+pdfDataDTO.getTodate()+"</td>");
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("</br>\n" +
                "</br>\n" +
                "</br>\n" +
                "</br>\n" +
                "</br>\n" +
                "</br>\n" +
                "</br>\n" +
                "</br>\n" +
                "<div class=\"b\">\n" +
                "<h3>Upload User Signature:</h3>\n" +
                "</br>\n" +
                "</br>\n" +
                "</br>\n" +
              //  "<h3>Download User Signature:</h3>\n" +
                "</div>\n" +
                "</center>");

       /* sb.append("<div class=\"a\">\n" +
                "\t\t\t<div class=\"b\">\n" +
                "\t\t\t<b>\n" +
                "\t\t\t<h3>Upload User Signature:</h3>\n" +
                "\t\t\t<p>Digitally Signed by:High Court of Patna.</p>\n" +
                "\t\t\t<p>Date:"+pdfDataDTO.getFromDate()+" </p>\n" +
                "\t\t\t<p>Reason: Digital Copy.</p>\n" +
                "\t\t\t</b>\n" +
                "\t\t\t</div>\n" +
                "\t\t\t<div class=\"c\">\n" +
                "\t\t\t<b>\n" +
                "\t\t\t<h3>Download User Signature:</h3>\n" +
                "\t\t\t<p>Digitally Signed by:"+pdfDataDTO.getUplodatedBY()+"</p>\n" +
                "\t\t\t<p>Date:"+pdfDataDTO.getTodate()+" </p>\n" +
                "\t\t\t<p>Reason: Digital Copy.</p>\n" +
                "\t\t\t</b></div>\n" +
                "\t\t\t</div>");*/
        sb.append("</body> </html>");

        System.out.println("start.....generateTableData.." + tempFile1html.getAbsolutePath());
        //Items
        isTextEditorFlow = true;
        if (isTextEditorFlow) {
            FileOutputStream files = new FileOutputStream(new File(tempFile1html.getAbsolutePath()));
           //System.out.println("HTML:::" + sb.toString());
            int result = HtmlconvertToPdf(sb.toString(), files);
            System.out.println("HTML CONVERT DONE::::::::::::::: :" + tempFile1html.getAbsolutePath());
            InputStream outputfile = new FileInputStream(new File(tempFile1html.getAbsolutePath()));
            return outputfile;
        }
        return null;
    }
    public InputStream  ganratecertificatenew(Context context, File tempFile1html,PDFDataDTO pdfDataDTO) throws
            Exception {


        boolean isTextEditorFlow = false;
        StringBuffer sb = new StringBuffer("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "<title>A4 Size Page with Letter Content Demo</title>\n" +
                "<style>\n" +
                "    /* Define A4 size */\n" +
                "    @page {\n" +
                "        size: A4;\n" +
                "        margin: 0;\n" +
                "    }\n" +
                "\n" +
                "    /* Set page margins */\n" +
                "    body {\n" +
                "        margin: 40px;\n" +
                "        font-family: Arial, sans-serif;\n" +
                "    }\n" +
                "\n" +
                "    /* Letter content demo */\n" +
                "    .letter {\n" +
                "        text-align: center;\n" +
                "    }\n" +
                "\n" +
                "    .sender-info {\n" +
                "        margin-bottom: 20px;\n" +
                "                float:left;\n" +
                "                text-align:left;\n" +
                "    }\n" +
                "\n" +
                "    .sender-info p {\n" +
                "        margin: 5px 0;\n" +
                "    }\n" +
                "\n" +
                "    .recipient-info {\n" +
                "        margin-top: 40px;\n" +
                "    }\n" +
                "\n" +
                "    .recipient-info p {\n" +
                "        margin: 5px 0;\n" +
                "    }\n" +
                "\n" +
                "    .content {\n" +
                "        margin-top: 40px;\n" +
                "        text-align: justify;\n" +
                "    }\n" +
                "\n" +
                "    .content p {\n" +
                "        margin-bottom: 20px;\n" +
                "    }\n" +
                "        .a{\n" +
                "          width:100%;\n" +
                "        }\n" +
                "        .b{\n" +
                "          width:50%;\n" +
                "          float:left;\n" +
                "        }\n" +
                "        .c{\n" +
                "          width:50%;\n" +
                "          float:right;\n" +
                "        }\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "    <div class=\"letter\">\n" +
                "        <div class=\"sender-info\">\n"
        );
        String logopath = configurationService.getProperty("certificate.logo.path");
        sb.append(
                "\t\t<center><img src=\""+logopath+"\"/></center>\n" +
                "\t\t<center><h3>CERTIFICATE</h3></center>\n" +
                "\t\t<p>The list of digital records enclosed herewith are preserved in the cloud-based Judicial\n" +
                "Trustworthy Digital Repository under the lawful control of the High Court From Date <b>"+pdfDataDTO.getFromDate()+"</b> To <b>"+pdfDataDTO.getTodate()+"</b></p>\n" +
                "                   </br>\n" +
                "                   </br>            <h4>List of record</h4>\n");
        for (String s:pdfDataDTO.getUplodateDocname()
        ) {
            sb.append("<p> "+s+"</p>");
        }
        sb.append(""+





                "        </div>\n" +
                "                </br>\n" +
                "           </br>\n" +
                "           </br>\n" +
                "           </br>\n" +
                "           </br>\n" +
                "           </br>\n" +
                "           </br>\n" +
                "           </br>\n" +
                "           </br>\n" +
                "           </br>\n" +
                "           </br>\n" +
                "           </br>\n" +
                "           </br>\n" +
                "        <div class=\"content\">\n" +
                "                <p>A) During the said period,the judical Digital Repository was oprating properly.</p>\n" +
                "                        <br/>\n" +
                "                        <p>I have check and verify the digitized record retrieved from the judical Trustwothy\n" +
                "                        Digital Repository to ensure its integrity.The Cuputer-generated report is enclosed</p>\n" +
                "                        <p>herewith.</p></br>");

        /*sb.append("</br>\n" +
                "\t\t\t</br>\n" +
                "\t\t\t<div class=\"a\">\n" +
                "\t\t\t<div class=\"b\">\n" +
                "\t\t\t<h4>Signature:</h4>\n" +
                "\t\t\t<h5>Date: "+DateFormateDDMMYYYY(new Date())+"</h5>\n" +
                "\t\t\t<h5>Name of Repository Manager: Support Team.</h5>\n" +
                "\t\t\t<h5>Name of High Court: Patna High Court.</h5>\n" +
                "\t\t\t<h5>Address: Patna.</h5>\n" +
                "\t\t\t</div>\n" +
                "\t\t\t<div class=\"c\">\n" +
                "\t\t\t<h4>Seal:</h4>\n" +
                "\t\t\t</div>\n" +
                "\t\t\t</div>");*/

sb.append("</div>  </div></body> </html>");

        System.out.println("start.....ganratecertificatenew.." + tempFile1html.getAbsolutePath());
        //Items
        isTextEditorFlow = true;
        if (isTextEditorFlow) {
            FileOutputStream files = new FileOutputStream(new File(tempFile1html.getAbsolutePath()));
            //System.out.println("HTML:::" + sb.toString());
            int result = HtmlconvertToPdf(sb.toString(), files);
            System.out.println("HTML CONVERT DONE::::::::::::::: :" + tempFile1html.getAbsolutePath());
            InputStream outputfile = new FileInputStream(new File(tempFile1html.getAbsolutePath()));
            return outputfile;
        }
        return null;
    }


    public static int HtmlconvertToPdf(String htmtext, FileOutputStream out) {
        try {
            System.out.println("::::::::::in HtmlconvertToPdf:::::::::::::");
            ConverterProperties converterProperties = new ConverterProperties();
            //  final FontSet set = new FontSet();
            FontProvider provider = new FontProvider();
            provider.addStandardPdfFonts();
            provider.addSystemFonts();
            converterProperties.setFontProvider(provider);
            HtmlConverter.convertToPdf(htmtext, out, converterProperties);
            System.out.println("::::::::::in HtmlconvertToPdf::::::done!:::::::");
            return 1;
        } catch (Exception e) {
            System.out.println("::::::::::error in  HtmlconvertToPdf:::::::::::" + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    private static byte[] readStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    public static void mergePdfFiles(List<InputStream> inputStreams, OutputStream outputStream) throws IOException {
        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        for (InputStream inputStream : inputStreams) {
            pdfMerger.addSource(inputStream);
        }
        pdfMerger.setDestinationStream(outputStream);
        pdfMerger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
    }

    public static  void bookmark(List<InputStream> inputStreams, OutputStream outputStream){
        try {

            PDDocument document1 = PDDocument.load(Path.of("D://n1.pdf").toFile());
            PDDocument document2 = PDDocument.load(Path.of("D://a1.pdf").toFile());
            PDDocumentOutline outline =  new PDDocumentOutline();
            document1.getDocumentCatalog().setDocumentOutline( outline );
            PDOutlineItem pagesOutline = new PDOutlineItem();
            pagesOutline.setTitle( "All Pages" );
            outline.addLast( pagesOutline );

            getStreamFromIterator(document1.getPages().iterator()).limit(10).forEach(d->{
                PDPageFitWidthDestination dest = new PDPageFitWidthDestination();
                dest.setPage( d );
                PDOutlineItem bookmark = new PDOutlineItem();
                bookmark.setDestination( dest );
                bookmark.setTitle( "Page1 " );
                pagesOutline.addLast( bookmark );
            });
            getStreamFromIterator(document2.getPages().iterator()).limit(10).forEach(dt->{
                document1.addPage(dt);
                PDPageFitWidthDestination dest = new PDPageFitWidthDestination();
                dest.setPage( dt );
                PDOutlineItem bookmark = new PDOutlineItem();
                bookmark.setDestination( dest );
                bookmark.setTitle( "Page2 " );
                pagesOutline.addLast( bookmark );
            });


            pagesOutline.openNode();
            outline.openNode();
            document1.save(Path.of("D://demo_book.pdf").toFile());
            System.out.println(document1.getDocumentId());
        } catch (Exception e) {

        }
    }

    public static <T> Stream<T> getStreamFromIterator(Iterator<T> iterator) {
        // Convert the iterator to Spliterator
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, 0);
        // Get a Sequential Stream from spliterator
        return StreamSupport.stream(spliterator, false);
    }

    public static String DateToSTRDDMMYYYHHMMSS(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        return formatter.format(date);
    }
    public static String DateFormateDDMMYYYY(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }
    public static String strDateToString(String date) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return DateFormateDDMMYYYY(dateFormat.parse(date));
    }


    public void saveBookmarkOntemp(FileInputStream inputStream, String tempFile) throws Exception {
        System.out.println("in Watermark save");
        PDDocument localpodocument = new PDDocument();
        try (InputStream pdfInputStream = inputStream;
             PDDocument p = PDDocument.load(pdfInputStream)) {
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


            PDDocumentOutline outline = new PDDocumentOutline();
            localpodocument.getDocumentCatalog().setDocumentOutline(outline);
            PDOutlineItem pagesOutline = new PDOutlineItem();
            //pagesOutline.setTitle("Smart View");
            outline.addLast(pagesOutline);

            pagesOutline.openNode();
            outline.openNode();
            localpodocument.getDocumentCatalog().setPageMode(PageMode.USE_OUTLINES);
            localpodocument.save(tempFile);
            pagesOutline.closeNode();
            outline.closeNode();
            localpodocument.close();
            System.out.println("mearge done!!!!"+tempFile);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("");
        } finally {
            localpodocument.close();
        }

    }
}