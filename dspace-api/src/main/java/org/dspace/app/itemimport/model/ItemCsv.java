/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.itemimport.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.dspace.content.cis.util.ConverterUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@JsonPropertyOrder({"referenceID", "stateCode", "districtCode", "Date", "registrationCode","LegacyData","LegacyID","docType","Link"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemCsv {
    @JsonProperty("referenceID")
    private String referenceID;
    @JsonProperty("stateCode")
    private String stateCode;
    @JsonProperty("districtCode")
    private String districtCode;
    @JsonProperty("Date")
    private String Date;
    @JsonProperty("registrationCode")
    private String registrationCode;
    @JsonProperty("LegacyData")
    private String LegacyData;
    @JsonProperty("LegacyID")
    private String LegacyID;
    @JsonProperty("docType")
    private String docType;
    @JsonProperty("Link")
    private String Link;
    private String collection;
    private String community;
    private  Boolean status;
    private  String errorMsg;
    private List<String> DspaceObjectHierarchy;
    private ConverterUtil converterUtil;

    public String getReferenceID() {
        return referenceID;
    }

    public void setReferenceID(String referenceID) {
        this.referenceID = referenceID;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getRegistrationCode() {
        return registrationCode;
    }

    public void setRegistrationCode(String registrationCode) {
        this.registrationCode = registrationCode;
    }

    public String getLegacyData() {
        return LegacyData;
    }

    public void setLegacyData(String legacyData) {
        LegacyData = legacyData;
    }

    public String getLegacyID() {
        return LegacyID;
    }

    public void setLegacyID(String legacyID) {
        LegacyID = legacyID;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getLink() {
        return Link;
    }

    public void setLink(String link) {
        Link = link;
    }
    public List<Document> convertDocument(){
      List<String> docuemntStr=Arrays.asList(this.getLink().split("\\|\\|"));
      return docuemntStr.stream().map(d->{
         String [] documentArray=d.split(";");
         if(documentArray.length ==2) {
             return new Document(documentArray[0], documentArray[1]);
         }else {
          return   null;
         }
      }).collect(Collectors.toList());
    }
    public void setCollectionTOObject(String collection){
        this.collection=collection;
    }
    public String returnCollectrion(){
        return  collection;
    }
    public void pushStatus(Boolean status){
        this.status=status;
    }
    public Boolean returnStatus(){
        return this.status;
    }
    public  void pusherrorMsg(String errorMsg){
        this.errorMsg=errorMsg;
    }
    public  String returnerrorMsg(){
        return this.errorMsg;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    @Override
    public String toString(){
        return referenceID +" :: "+  stateCode +" :: "+  districtCode +" :: "+ getDate() +" :: "+ registrationCode +" :: "+ getLegacyData() +" :: "+ getLegacyID() +" :: "+ docType ;
    }

    public ConverterUtil getConverterUtil() {
        return converterUtil;
    }

    public void setConverterUtil(ConverterUtil converterUtil) {
        this.converterUtil = converterUtil;
    }
}
