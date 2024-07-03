/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.cis.models;
import java.io.Serializable;
public  class CISProcess implements Serializable  {
    private String  cino;
    private String  jocode;
    private String  date;
    private String  causelist_date;
    private String  secret_key;
    private Boolean  isreview=false;
    private String  name="Dsquare";
    private String caseType;
    private String caseNumber;
    private String caseYear;
    private  Integer type;
    private  boolean defected;
    private String username;
    private String password;
    public CISProcess(String JOCODE, String date){
       this.jocode=JOCODE;
       this.causelist_date=date;
       this.secret_key="UKHC@321";
    }
    public CISProcess(String cino,int type){
        this.cino=cino;
        this.type=type;
    }

    public CISProcess(String caseType, String caseNumber,String caseYear, boolean defected){
        this.caseYear=caseYear;
        this.caseNumber=caseNumber;
        this.caseType=caseType;
        this.defected=defected;
    }
    public String getJOCODE() {
        return jocode;
    }
    public void setJOCODE(String jocode) {
        this.jocode = jocode;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public Boolean getIsreview() {
        return isreview;
    }

    public void setIsreview(Boolean isreview) {
        this.isreview = isreview;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCaseType() {
        return caseType;
    }

    public void setCaseType(String caseType) {
        this.caseType = caseType;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public String getCaseYear() {
        return caseYear;
    }

    public void setCaseYear(String caseYear) {
        this.caseYear = caseYear;
    }

    public String getCino() {
        return cino;
    }

    public void setCino(String cino) {
        this.cino = cino;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public boolean isDefected() {
        return defected;
    }

    public void setDefected(boolean defected) {
        this.defected = defected;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCauselist_date() {
        return causelist_date;
    }

    public void setCauselist_date(String causelist_date) {
        this.causelist_date = causelist_date;
    }

    public String getSecret_key() {
        return secret_key;
    }

    public void setSecret_key(String secret_key) {
        this.secret_key = secret_key;
    }
}
