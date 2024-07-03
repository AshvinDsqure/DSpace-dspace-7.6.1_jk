/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.itemimport;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.dspace.app.itemimport.factory.ItemImportServiceFactory;
import org.dspace.app.itemimport.model.ItemCsv;
import org.dspace.app.itemimport.service.ItemCSVImportService;
import org.dspace.content.cis.service.CiviltService;
import org.dspace.content.cis.service.OrgNametService;
import org.dspace.content.cis.util.ConverterUtil;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.dspace.discovery.IndexingService;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.handle.service.HandleService;
import org.dspace.services.factory.DSpaceServicesFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Import items into DSpace. The conventional use is upload files by copying
 * them. DSpace writes the item's bitstreams into its assetstore. Metadata is
 * also loaded to the DSpace database.
 * <p>
 * A second use assumes the bitstream files already exist in a storage
 * resource accessible to DSpace. In this case the bitstreams are 'registered'.
 * That is, the metadata is loaded to the DSpace database and DSpace is given
 * the location of the file which is subsumed into DSpace.
 * <p>
 * The distinction is controlled by the format of lines in the 'contents' file.
 * See comments in processContentsFile() below.
 * <p>
 * Modified by David Little, UCSD Libraries 12/21/04 to
 * allow the registration of files (bitstreams) into DSpace.
 */
public class ItemImportCisServiceTODmsCLITool {

    private static boolean template = false;

    private static final CollectionService collectionService = ContentServiceFactory.getInstance().getCollectionService();
    private static final EPersonService epersonService = EPersonServiceFactory.getInstance().getEPersonService();
    private static final HandleService handleService = HandleServiceFactory.getInstance().getHandleService();
    private static final CiviltService civiltService = EPersonServiceFactory.getInstance().getCiviltService();
    private static final OrgNametService orgNametService = EPersonServiceFactory.getInstance().getOrgNametService();
    private static final CommunityService communityService = ContentServiceFactory.getInstance().getCommunityService();
    private  static  final WorkspaceItemService workspaceItemService=ContentServiceFactory.getInstance().getWorkspaceItemService();
    private  static  final ItemService itemService=ContentServiceFactory.getInstance().getItemService();
    private  static  final RelationshipService relationshipService=ContentServiceFactory.getInstance().getRelationshipService();
    private  static  final RelationshipTypeService relationshipTypeService=ContentServiceFactory.getInstance().getRelationshipTypeService();
    private  static  final InstallItemService installItemService=ContentServiceFactory.getInstance().getInstallItemService();
    private static  final IndexingService indexer = DSpaceServicesFactory.getInstance().getServiceManager()
            .getServiceByName(IndexingService.class.getName(),
                    IndexingService.class);

    /**
     * Default constructor
     */
    private ItemImportCisServiceTODmsCLITool() {
    }

    public static void main(String[] argv) throws Exception {
        Date startTime = new Date();
        int status = 0;
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("e", "eperson", true, "email of eperson doing importing");
        options.addOption("t", "Type", true, "Type Dispose or Panding");
        options.addOption("d", "Type", true, "Cause List Date");
        options.addOption("c", "Cino", true, "cino");
        CommandLine line = parser.parse(options, argv);
        String eperson = "";
        String cisurl="";
        String causelistDate=null;
        String cino=null;
        try {
            if (!line.hasOption('e')) {
                System.out.println("Error  with, eperson  must add");
                System.exit(1);
            }
            if (!line.hasOption('t')) {
                System.out.println("Error  with, Case Status it will be d or p ");
                System.exit(1);
            }
            if (line.hasOption('e')) { // eperson
                eperson = line.getOptionValue('e');
            }
            if (line.hasOption('d')) { // eperson
                causelistDate = line.getOptionValue('d');
            }
            ConverterUtil converterUtil=new ConverterUtil(null,orgNametService,null);
            if (line.hasOption('t')) { // eperson
                String caseType = line.getOptionValue('t');
                if(caseType.equals("P")){
                    System.out.println("Migration Pending Case Pending");
                    converterUtil.setCisUri("http://10.129.122.111/dms_api/getPendingCase.php");
                }else if(caseType.equals("D")){
                    System.out.println("Migration Despose Case Pending");
                    converterUtil.setCisUri("http://10.129.122.111/dms_api/getDisposedCase.php");
                }else{
                    if (!line.hasOption('c')) {
                        System.out.println("Please enter CINO");
                        System.exit(1);
                    }
                    cino=line.getOptionValue('c');
                    System.out.println("Migration  Case using Cino");
                    converterUtil.setCino(cino);
                    converterUtil.setCisUri("http://10.129.122.111/dms_api/getCaseCino.php");
                }
            }
            Context c = new Context();
            EPerson ePerson = epersonService.findByEmail(c, eperson);
            c.setCurrentUser(ePerson);
            System.out.println("URL::::::" + converterUtil.getCisUri());
            //c.commit();
            ItemCSVImportService myloader = ItemImportServiceFactory.getInstance().getItemCSVImportService();

            converterUtil.setContext(c);
            converterUtil.setCommunityService(communityService);
            converterUtil.setCollectionService(collectionService);
            converterUtil.setWorkspaceItemService(workspaceItemService);
            converterUtil.setItemService(itemService);
            converterUtil.setInstallItemService(installItemService);
            converterUtil.setIndexingService(indexer);
            converterUtil.setRelationshipService(relationshipService);
            converterUtil.setRelationshipTypeService(relationshipTypeService);
            //converterUtil.setCisUri(cisurl);
            if(causelistDate == null) {
                LocalDate currentDate = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                // Format the current date using the formatter
                causelistDate = currentDate.format(formatter);
            }
            converterUtil.setCauselistDate(causelistDate);
            myloader.pushToDMSFromRest(converterUtil);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}