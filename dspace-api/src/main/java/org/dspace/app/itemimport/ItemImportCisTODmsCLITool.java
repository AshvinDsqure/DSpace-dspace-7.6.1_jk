/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.itemimport;

import org.apache.commons.cli.*;
import org.dspace.app.itemimport.factory.ItemImportServiceFactory;
import org.dspace.app.itemimport.model.ItemCsv;
import org.dspace.app.itemimport.service.ItemCSVImportService;
import org.dspace.content.cis.Civilt;
import org.dspace.content.cis.service.CiviltService;
import org.dspace.content.cis.service.OrgNametService;
import org.dspace.content.cis.util.ConverterUtil;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.core.Context;
import org.dspace.core.ContextCIS;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.handle.service.HandleService;
import org.dspace.scripts.DSpaceRunnable;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
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
public class ItemImportCisTODmsCLITool extends DSpaceRunnable<ItemImportScriptConfiguration> {

    private static boolean template = false;

    private static final CollectionService collectionService = ContentServiceFactory.getInstance().getCollectionService();
    private static final EPersonService epersonService = EPersonServiceFactory.getInstance().getEPersonService();
    private static final HandleService handleService = HandleServiceFactory.getInstance().getHandleService();
    private static final CiviltService civiltService = EPersonServiceFactory.getInstance().getCiviltService();
    private static final OrgNametService orgNametService = EPersonServiceFactory.getInstance().getOrgNametService();
    private static final CommunityService communityService = ContentServiceFactory.getInstance().getCommunityService();


    /**
     * Default constructor
     */
    private ItemImportCisTODmsCLITool() {
    }

    public static void main(String[] argv) throws Exception {
        Instant start = Instant.now();
        System.out.println(":::::::::::::startTime ::::::::::" + start);
        int status = 0;
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("e", "eperson", true, "email of eperson doing importing");
        options.addOption("n", "ThreadNumber", true, "Number of tread ");
        options.addOption("l", "Limit of record", true, "Limit of record");
        options.addOption("c", "Migrate Connected", true, "Limit of record");
        options.addOption("u", "update Migrated Data", true, "Limit of record");
        CommandLine line = parser.parse(options, argv);
        String eperson = "";
        int threadNumber = 7;
        int limitofrecord = -1;
        boolean isConnected = false;
        boolean isupdate = false;

        try {

            if (!line.hasOption('e')) {
                System.out.println("Error  with, eperson  must add");
                System.exit(1);
            }
            if (!line.hasOption('n')) {
                System.out.println("Error  with, Thread Count  must add");
                System.exit(1);
            }
            if (line.hasOption('e')) { // eperson
                eperson = line.getOptionValue('e');
            }
            if (line.hasOption('n')) { // eperson
                threadNumber = Integer.valueOf(line.getOptionValue('n'));
            }
            if (line.hasOption('l')) { // eperson
                limitofrecord = Integer.valueOf(line.getOptionValue('l'));
            }
            if (line.hasOption('c')) { // eperson
                isConnected = Boolean.valueOf(line.getOptionValue('c'));
            }
            if (line.hasOption('u')) { // eperson
                isupdate = Boolean.valueOf(line.getOptionValue('u'));
            }
            System.out.println("threadNumber:::" + threadNumber);
            Context cs = new Context();
            cs.setMode(Context.Mode.BATCH_EDIT);
            ContextCIS c = new ContextCIS();
            c.setMode(ContextCIS.Mode.BATCH_EDIT);
            EPerson ePerson = epersonService.findByEmail(cs, eperson);
            c.setCurrentUser(ePerson);
            cs.setCurrentUser(ePerson);
            List<Civilt> civilts = new ArrayList<>();
            if (isConnected) {
                civilts = civiltService.findAllConnectedFromDspaceMapping(c, limitofrecord);
            } else {
                civilts = civiltService.findAllnotMigratedToDMS(c, limitofrecord);
            }
            System.out.println("eperson:::" + civilts.size());
            System.out.println("count iteam:::" + ePerson.getEmail());
            //c.commit();
            ItemCSVImportService myloader = ItemImportServiceFactory.getInstance().getItemCSVImportService();

            //List<List<Civilt>> splitedList = myloader.splitList(civilts, threadNumber);
            //System.out.println("splitedList size::"+splitedList.size());
            LinkedBlockingQueue<Civilt> queue = new LinkedBlockingQueue<>(civilts);
            System.out.println("count iteam:::" + queue.size());
            System.out.println(" is  isConnected:::" + isConnected);


            if (isConnected) {

            }
            List<CompletableFuture<ItemCsv>> completableFutures = myloader.ConvertCiviltTOCiviltDTOService(ePerson, threadNumber, queue,isupdate);
            CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
            List<ItemCsv> newList = completableFutures.stream().map(d -> {
                try {
                   ItemCsv itemCsv= d.get();
                  /* Context context= itemCsv.getConverterUtil().getContext();
                    ContextCIS contextCIS= itemCsv.getConverterUtil().getContextCIS();
                    context.setFromtool(false);
                    context.commit();
                    contextCIS.commit();*/
                    return itemCsv;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }).peek(d -> System.out.print("done.....")).collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Instant end = Instant.now();
            Duration totalTime = Duration.between(start, end);
            System.out.println("Total time taken: " + totalTime.toMillis() + " milliseconds");
        }
    }

    @Override
    public ItemImportScriptConfiguration getScriptConfiguration() {
        return null;
    }

    @Override
    public void setup() throws ParseException {

    }

    @Override
    public void internalRun() throws Exception {

    }
}