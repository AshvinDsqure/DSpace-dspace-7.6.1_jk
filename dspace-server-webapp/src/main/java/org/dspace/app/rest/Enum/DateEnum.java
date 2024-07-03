/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.Enum;

import java.text.SimpleDateFormat;
import java.util.*;

public enum DateEnum {
	 WEEK(0),
	    Month(1),
	    Year(2),
	    ALLTime(3),
	    DateBase(4);
	    private int id;
	    private Date StartDate;
	    private String StartDatestr;
	    private Date endDate;
	     private String endDatestr;
	    String queryString;
	    String  CollectionList;
	    String GroupBy;

	    private DateEnum(int id) {
	        this.id = id;
	    }

	    public static DateEnum getDeploymentClass(int id) {
	        for (DateEnum t : values()) {
	            if (t.getId() == id) {
	                return t;
	            }
	        }
	        return null;
	    }

	    public int getId() {
	        return id;
	    }

	    public void setId(int id) {
	        this.id = id;
	    }

	    public Date getStartDate() {
	        return new Date();
	    }

	    public void setStartDate(Date StartDate) {
	        this.StartDate = StartDate;
	    }
	    public String getqueryString(){
	        String query="";
	        try{
	        SimpleDateFormat DateFor = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX");        
	        if(CollectionList.trim().toString().length() !=0){
	             String CollectionListString="";
	             List<String> CollactionUUIDs = new ArrayList<String>();
	             CollactionUUIDs = Arrays.asList(this.CollectionList.trim().toString().split(","));
	             for(int i =0;i<CollactionUUIDs.size();i++){
	                 if(i == (CollactionUUIDs.size()-1)){
	                    CollectionListString=CollectionListString+"'"+CollactionUUIDs.get(i)+"'"; 
	                 }else{
	                    CollectionListString=CollectionListString+"'"+CollactionUUIDs.get(i)+"',";
	                 }                 
	             }
	             query="and e.parencollection in ("+CollectionListString+")";
	        }        
	        if (this.id == 0) {
	            query=query+" and   e.action_date <= '"+DateFor.format(new Date())+"' AND  e.action_date >= '"+DateFor.format(getDate(7))+"'";
	        } else if (this.id == 1) {
	             query=query+ " and e.action_date <= '"+DateFor.format(new Date())+"' AND  e.action_date >= '"+DateFor.format(getDate(30))+"'";
	        } else if (this.id == 2) {
	             query=query+ " and  e.action_date <= '"+DateFor.format(new Date())+"' AND  e.action_date >= '"+DateFor.format(getDate(365))+"'";
	        } else if(this.id==3){
	            query = query+"";
	        }else if(this.id==4){
	            query=query+ " and e.action_date >= '"+ DateFor.format(new SimpleDateFormat("dd-MM-yyyy").parse(this.StartDatestr))+"' AND  e.action_date <= '"+DateFor.format(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(this.endDatestr+" 23:59:59"))+"'";
	        }else {
	            return null;
	        }
	        }catch(Exception e){
	            e.printStackTrace();
	            return query;
	        }
	        return  query;
	    }
	    public Date getEndDate() {
	        if (this.id == 0) {
	            return getDate(7);
	        } else if (this.id == 1) {
	            return getDate(30);
	        } else if (this.id == 2) {
	            return getDate(365);
	        } else if (this.id == 3){
	            return getDate(365);
	        }else {
	            return getDate(365);
	        }
	    }

	    public void setEndDate(Date endDate) {
	        this.endDate = endDate;
	    }
	    public Date getDate(int days) {
	        Calendar cal = Calendar.getInstance();
	        cal.setTime(new Date());
	        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - days);
	        return  cal.getTime();
	    }

	    public String getQueryString() {
	        return queryString;
	    }

	    public void setQueryString(String queryString) {
	        this.queryString = queryString;
	    }

	    public String getCollectionList() {
	        return CollectionList;
	    }

	    public void setCollectionList(String CollectionList) {
	        this.CollectionList = CollectionList;
	    }

	    public String getGroupBy() {
	         if (this.id == 0) {
	            return "to_char(e.action_date,'dd-MM-YYYY')";
	        } else if (this.id == 1) {
	            return "to_char(e.action_date,'MM-YYYY')";
	        } else if (this.id == 2) {
	            return "to_char(e.action_date,'YYYY')";
	        } else if (this.id == 3) {
	            return "to_char(e.action_date,'YYYY')";
	        }else {
	            return "to_char(e.action_date,'dd-MM-YYYY')";
	        }
	    }

	    public void setGroupBy(String GroupBy) {
	        this.GroupBy = GroupBy;
	    }

	    public void setStartDatestr(String StartDatestr) {
	        this.StartDatestr = StartDatestr;
	    }

	    public void setEndDatestr(String endDatestr) {
	        this.endDatestr = endDatestr;
	    }

	    public String getStartDatestr() {
	        return StartDatestr;
	    }

	    public String getEndDatestr() {
	        return endDatestr;
	    }
	    
	    

}
