/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.itemimport.service;

import org.dspace.app.itemimport.model.ItemCsv;
import org.dspace.content.cis.Civilt;
import org.dspace.content.cis.util.ConverterUtil;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Import items into DSpace. The conventional use is upload files by copying
 * them. DSpace writes the item's bitstreams into its assetstore. Metadata is
 * also loaded to the DSpace database.
 * <P>
 * A second use assumes the bitstream files already exist in a storage
 * resource accessible to DSpace. In this case the bitstreams are 'registered'.
 * That is, the metadata is loaded to the DSpace database and DSpace is given
 * the location of the file which is subsumed into DSpace.
 * <P>
 * The distinction is controlled by the format of lines in the 'contents' file.
 * See comments in processContentsFile() below.
 * <P>
 * Modified by David Little, UCSD Libraries 12/21/04 to
 * allow the registration of files (bitstreams) into DSpace.
 */
public interface ItemCSVImportService {
    public  <T> List<List<T>> splitList(List<T> originalList, int partitionSize);
    public List<CompletableFuture<List<ItemCsv>>> getCompletableFutureForItem(List<List<ItemCsv>> itemCsvs,EPerson ePerson,String colUuid,String CommunityName) throws SQLException ;

    public void pushCisToDMS(Civilt Civilt, Context context) throws Exception;
    public List<CompletableFuture<List<ItemCsv>>> getCompletableFutureForpushIteamCisToDms(List<List<Civilt>> civilts, EPerson ePerson,int theardCount,Context context) throws SQLException;

    void updateItemMataData(ConverterUtil converterUtil, int numberOfThread);

    public List<CompletableFuture<ItemCsv>> ConvertCiviltTOCiviltDTOService(EPerson ePerson, int numberOfThread, LinkedBlockingQueue<Civilt> queue,Boolean isupdated) throws SQLException ;
    public List<CompletableFuture<ItemCsv>> connectedCaseRelationShip(ConverterUtil converterUtil, int numberOfThread) throws SQLException ;
    public List<CompletableFuture<ItemCsv>> pusCiviltTODMS(ConverterUtil converterUtil, int numberOfThread) throws SQLException ;
    public void pushToDMSFromRest(ConverterUtil converterUtil) throws Exception;


}
