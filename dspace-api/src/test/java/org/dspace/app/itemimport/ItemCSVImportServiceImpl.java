/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.app.itemimport;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.dspace.app.itemimport.model.ItemCsv;
import org.dspace.app.itemimport.model.MataDataEnum;
import org.dspace.app.itemimport.service.ItemCSVImportService;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.authorize.service.ResourcePolicyService;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.WorkspaceItem;
import org.dspace.content.cis.Civilt;
import org.dspace.content.cis.dto.CiviltDTO;
import org.dspace.content.cis.dto.CiviltDTORest;
import org.dspace.content.cis.service.OrgNametService;
import org.dspace.content.cis.util.ConverterUtil;
import org.dspace.content.service.*;
import org.dspace.core.Context;
import org.dspace.core.ContextCIS;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.service.EPersonService;
import org.dspace.eperson.service.GroupService;
import org.dspace.handle.service.HandleService;
import org.dspace.services.ConfigurationService;
import org.dspace.workflow.WorkflowService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


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
public class ItemCSVImportServiceImpl implements ItemCSVImportService, InitializingBean {


    @Autowired(required = true)
    protected AuthorizeService authorizeService;
    @Autowired(required = true)
    protected BitstreamService bitstreamService;
    @Autowired(required = true)
    protected BitstreamFormatService bitstreamFormatService;
    @Autowired(required = true)
    protected BundleService bundleService;
    @Autowired(required = true)
    protected CollectionService collectionService;
    @Autowired(required = true)
    protected EPersonService ePersonService;
    @Autowired(required = true)
    protected HandleService handleService;
    @Autowired(required = true)
    protected ItemService itemService;
    @Autowired(required = true)
    protected InstallItemService installItemService;
    @Autowired(required = true)
    protected GroupService groupService;
    @Autowired(required = true)
    protected MetadataFieldService metadataFieldService;
    @Autowired(required = true)
    protected MetadataSchemaService metadataSchemaService;
    @Autowired(required = true)
    protected ResourcePolicyService resourcePolicyService;
    @Autowired(required = true)
    protected WorkspaceItemService workspaceItemService;
    @Autowired(required = true)
    protected OrgNametService orgNametService;
    @Autowired(required = true)
    protected WorkflowService workflowService;
    @Autowired(required = true)
    protected ConfigurationService configurationService;

    @Autowired(required = true)
    protected MetadataValueService metadataValueService;
    @Autowired(required = true)
    protected CommunityService communityService;


    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public <T> List<List<T>> splitList(List<T> originalList, int partitionSize) {
        return Lists.partition(originalList, originalList.size() / partitionSize);
    }

    @Override
    public List<CompletableFuture<List<ItemCsv>>> getCompletableFutureForItem(List<List<ItemCsv>> itemCsvs, EPerson ePerson, String colUuid, String CommunityName) throws SQLException {
        return itemCsvs.stream().map(d -> {
            Executor executor = Executors.newFixedThreadPool(5);
            return CompletableFuture.supplyAsync(() -> {
                System.out.println("itemCsvs::" + itemCsvs.size());
                return d.stream().map(i -> {

                    ItemCsv model = new ItemCsv();
                    try {
                        Context context = new Context(Context.Mode.BATCH_EDIT);
                        context.setCurrentUser(ePerson);

                        //i.setCollectionTOObject(colUuid);
                        //i.setCommunity(CommunityName);
                        // Item item=pushIteamTODspace(i,context);

                        model.pushStatus(true);
                        context.commit();
                    } catch (Exception e) {
                        model.pushStatus(false);
                        model.pusherrorMsg("something went wrong....");
                        e.printStackTrace();
                    }

                    return model;
                }).collect(Collectors.toList());
            }, executor).exceptionally(ex -> {
                System.out.println("error.........");
                return null;
            });
        }).collect(Collectors.toList());
    }

    @Override
    synchronized public void pushCisToDMS(Civilt Civilt, Context context) throws SQLException, AuthorizeException {
        CiviltDTO civiltDTO = null;
        /*try {
            civiltDTO = converterUtil.convertCiviltTOCiviltDTO(Civilt);
            if (civiltDTO.getCourtt() != null) {
                Community community = communityService.searchByTitle(converterUtil.getContext(), civiltDTO.getCourtt().getBench_desc());
                System.out.println("community.........." + community.getName());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/

        //System.out.println("cinooooooo...."+civiltDTO.getCino() +"length:::::"+CiviltDTO.class.getDeclaredFields().length);
        CiviltDTO finalCiviltDTO = civiltDTO;
        Arrays.asList(CiviltDTO.class.getDeclaredFields()).stream().forEach(dx -> {
            MataDataEnum mataDataEnum = null;
            try {
                mataDataEnum = MataDataEnum.valueOf(dx.getName());
            } catch (IllegalArgumentException e) {

            } catch (Exception ex) {

            }
            if (mataDataEnum != null) {
                mataDataEnum.setCiviltDTO(finalCiviltDTO);
                if (mataDataEnum.getvalue() != null) {
                    System.out.println("mataDataEnum.getvalue():::" + mataDataEnum.getvalue());
                }
            }
        });
        ;
       /* Collection collection = (Collection) handleService.resolveToObject(context, itemCsv.returnCollectrion());
        WorkspaceItem workspaceItem = workspaceItemService.create(context, collection, false);
        Item item = workspaceItem.getItem();
        item.setArchived(true);
        item.setOwningCollection(collection);
        item.setDiscoverable(true);
        item.setLastModified(new Date());
        itemService.clearMetadata(context,item,Item.ANY, Item.ANY, Item.ANY, Item.ANY);
        Class<?> cls = ItemCsv.class;
        JsonPropertyOrder annotation = ItemCsv.class.getAnnotation(JsonPropertyOrder.class);
        Arrays.asList(annotation.value()).stream().forEach(d->{
            MataDataEnum mataDataEnum=MataDataEnum.valueOf(d);
            mataDataEnum.setItemCsv(itemCsv);
            if(mataDataEnum.getvalue() != null && !mataDataEnum.getvalue().equals("Link")) {
                try {
                    System.out.println("convertToString::"+mataDataEnum.convertToString());
                    itemService.addMetadata(context, item, mataDataEnum.getSchema(), mataDataEnum.getElement(), mataDataEnum.getQualifier(),null,mataDataEnum.getvalue());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }else{
                System.out.println("Miss mata Data"+mataDataEnum.getvalue());
            }
        });
        System.out.println("item save.....");
        //return  null;
        return installItemService.installItem(context, workspaceItem);*/

    }


    public void pushCisToDMS_(CiviltDTO civiltDTO, Context context) throws Exception {
        Collection collection = null;
        WorkspaceItem workspaceItem = workspaceItemService.create(context, collection, false);
        Item item = workspaceItem.getItem();
        item.setArchived(true);
        item.setOwningCollection(collection);
        item.setDiscoverable(true);
        item.setLastModified(new Date());
        itemService.clearMetadata(context, item, Item.ANY, Item.ANY, Item.ANY, Item.ANY);
        Arrays.asList(CiviltDTO.class.getFields()).stream().forEach(d -> {
            MataDataEnum mataDataEnum = null;
            try {
                mataDataEnum = MataDataEnum.valueOf(d.getName());
            } catch (IllegalArgumentException e) {

            } catch (Exception ex) {

            }
            if (mataDataEnum != null) {
                mataDataEnum.setCiviltDTO(civiltDTO);
                if (mataDataEnum.getvalue() != null) {
                    System.out.println("mataDataEnum.getvalue():::" + mataDataEnum.getvalue());
                }
            } else {
                System.out.println("Miss mata Data" + mataDataEnum.getvalue());
            }
        });
        System.out.println("item save.....");
        installItemService.installItem(context, workspaceItem);
    }

    @Override
    public List<CompletableFuture<List<ItemCsv>>> getCompletableFutureForpushIteamCisToDms(List<List<Civilt>> civilts, EPerson ePerson, int theardCount, Context context) throws SQLException {
        ExecutorService executorService = Executors.newFixedThreadPool(theardCount, new ThreadFactory() {
            private AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("CustomThread-" + threadNumber.getAndIncrement());
                return thread;
            }
        });

        return civilts.stream().map(d -> {
            return CompletableFuture.supplyAsync(() -> {

                return d.stream().map(i -> {
                    ItemCsv model = new ItemCsv();
                    try {
                        //pushBitstreamTODspace(i,context);
                        pushCisToDMS(i, context);
                        model.pushStatus(true);
                        //context.commit();
                    } catch (Exception e) {
                        model.pushStatus(false);
                        model.pusherrorMsg("something went wrong....");
                        e.printStackTrace();
                    }
                    return model;
                }).collect(Collectors.toList());
            }, executorService).exceptionally(ex -> {
                System.out.println("error.........");
                return null;
            });
        }).collect(Collectors.toList());
    }

    @Override
    public void updateItemMataData(ConverterUtil converterUtil, int numberOfThread) {
        converterUtil.setWorkspaceItemService(workspaceItemService);
        converterUtil.setItemService(itemService);
        converterUtil.setInstallItemService(installItemService);
        converterUtil.setCollectionService(collectionService);
        converterUtil.setOrgNametService(orgNametService);
        while (!converterUtil.getQueue().isEmpty()) {
            try {
                System.out.println("Thread Name" + Thread.currentThread().getName());
                if (converterUtil.isMigrateConnectedCase()) {
                    CiviltDTO civiltDTO = converterUtil.getConnectedCiviltDTO();
                    converterUtil.pushCiviltDTOTODspace(civiltDTO);
                } else {
                    CiviltDTO civiltDTO = converterUtil.getTOCiviltDTOQueueforupdate(null);
                    converterUtil.updateMataDataBycino(civiltDTO);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public List<CompletableFuture<ItemCsv>> ConvertCiviltTOCiviltDTOService(EPerson ePerson, int numberOfThread,LinkedBlockingQueue<Civilt> queue ,Boolean isupdated) throws SQLException {

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThread, new ThreadFactory() {
            private AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("CustomThread-" + threadNumber.getAndIncrement());
                return thread;
            }
        });
        return IntStream.range(1, numberOfThread)
                .mapToObj(d -> {
                    return CompletableFuture.supplyAsync(() -> {
                        ItemCsv model = new ItemCsv();

                        System.out.println("converterUtil.getQueue():::" + queue.size());
                        synchronized (queue) {

                            ConverterUtil converterUtil=new ConverterUtil(null,orgNametService,queue);
                            converterUtil.setWorkspaceItemService(workspaceItemService);
                            converterUtil.setItemService(itemService);
                            converterUtil.setInstallItemService(installItemService);
                            converterUtil.setCollectionService(collectionService);
                            converterUtil.setOrgNametService(orgNametService);
                            converterUtil.setCommunityService(communityService);

                            model.setConverterUtil(converterUtil);
                            while (!queue.isEmpty()) {
                                try {

                                    System.out.println("Thread Name" + Thread.currentThread().getName());
                                    System.out.println("converterUtil.isMigrateConnectedCase():::::" + converterUtil.isMigrateConnectedCase());
                                    ContextCIS c = new ContextCIS();
                                    c.setMode(ContextCIS.Mode.BATCH_EDIT);
                                    Context cs = new Context();
                                    cs.setCurrentUser(ePerson);
                                    cs.setMode(Context.Mode.BATCH_EDIT);
                                    converterUtil.setContext(cs);
                                    converterUtil.setContextCIS(c);
                                    converterUtil.setePerson(ePerson);

                                    if (converterUtil.isMigrateConnectedCase()) {
                                        CiviltDTO civiltDTO = converterUtil.getConnectedCiviltDTO();
                                        converterUtil.pushConnectedCase(civiltDTO);
                                    } else if(isupdated){
                                        CiviltDTO civiltDTO = converterUtil.getTOCiviltDTOQueueforupdate(queue);
                                        // converterUtil.updateMataDataBycino(civiltDTO);
                                    }else {
                                        // CiviltDTO civiltDTO = converterUtil.getTOCiviltDTOQueueforupdate();
                                        // converterUtil.updateMataDataBycino(civiltDTO);

                                        CiviltDTO civiltDTO = converterUtil.pushTOCiviltDTOQueue(queue);
                                        converterUtil.pushCiviltDTOTODspace(civiltDTO);

                                    }
                                    //System.out.println("converterUtil.getContextCIS():: user"+converterUtil.getContextCIS().getCurrentUser().getEmail());

                                    model.pushStatus(true);

                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    model.pushStatus(false);
                                    throw new RuntimeException(e);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                        return model;
                    }, executorService).exceptionally(ex -> {
                        ex.printStackTrace();
                        System.out.println("error.........");
                        return null;
                    });
                }).collect(Collectors.toList());
    }

    @Override
    public List<CompletableFuture<ItemCsv>> connectedCaseRelationShip(ConverterUtil converterUtil, int numberOfThread) throws SQLException {
        return null;
    }

    public static HttpRequest.BodyPublisher ofForm(Map<Object, Object> data) {
        StringBuilder body = new StringBuilder();
        for (Object dataKey : data.keySet()) {
            if (body.length() > 0) {
                body.append("&");
            }
            body.append(encode(dataKey))
                    .append("=")
                    .append(encode(data.get(dataKey)));
        }
        return HttpRequest.BodyPublishers.ofString(body.toString());
    }

    private static String encode(Object obj) {
        return URLEncoder.encode(obj.toString(), StandardCharsets.UTF_8);
    }

    @Override
    public void pushToDMSFromRest(ConverterUtil converterUtil) throws Exception {
        //String jsonStr = this.readFileAsString("/home/vipul/Desktop/jhc/dispose.json");

        // Format the current date using the formatter
        HttpClient client = HttpClient.newHttpClient();
        Map<Object, Object> data = new HashMap<>();
        data.put("secret_key", "UKHC@321");
        data.put("date", converterUtil.getCauselistDate());
        if (converterUtil.getCino() != null) {
            data.put("cino", converterUtil.getCino());
        }
        System.out.println("url::" + converterUtil.getCisUri());
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(converterUtil.getCisUri()))
                .POST(ofForm(data))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String jsonStr = response.body();

        System.out.println("response.body()::::>>" + jsonStr);
        List<CiviltDTORest> civiltDTOList = new Gson().fromJson(jsonStr, new TypeToken<ArrayList<CiviltDTORest>>() {
        }.getType());
        civiltDTOList.forEach(d -> {
            System.out.println("cino::::" + d.getCino() + "Nature CD:::" + d.getNature_t());
            try {
                Item i = itemService.findByCIno(converterUtil.getContext(), d.getCino());
                /* if(i != null && d.getNature_t()  != null){
                    itemService.delete(converterUtil.getContext(),i);
                    System.out.println("item deleted :::::"+i.getID());
                    i= null;
                }*/
                if (i == null) {
                    converterUtil.pushCiviltDTORestTODspace(d);
                } else
                    System.out.println("Item Name:::::" + i.getName());
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        });
    }


    public String readFileAsString(String file) throws Exception {
        return new String(Files.readAllBytes(Paths.get(file)));
    }

    @Override
    public List<CompletableFuture<ItemCsv>> pusCiviltTODMS(ConverterUtil converterUtil, int numberOfThread) throws SQLException {
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThread, new ThreadFactory() {
            private AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("CustomThread-" + threadNumber.getAndIncrement());
                return thread;
            }
        });
        return IntStream.range(1, numberOfThread)
                .mapToObj(d -> {
                    return CompletableFuture.supplyAsync(() -> {
                        ItemCsv model = new ItemCsv();
                        while (!converterUtil.getQueueDTO().isEmpty()) {
                            try {
                                System.out.println("pushTOCiviltDTOQueueName:::" + Thread.currentThread().getName());
                                //converterUtil.pushCiviltDTOTODspace();
                                model.pushStatus(true);
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }

                        }
                        return model;
                    }, executorService).exceptionally(ex -> {
                        ex.printStackTrace();
                        System.out.println("error.........");
                        return null;
                    });
                }).collect(Collectors.toList());
    }


}