package org.splitbrain.simpleical;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Hold a single iCal VEvent
 * 
 * @author Andreas Gohr <andi@splitbrain.org>
 */
public class SimpleIcalEvent {
    private HashMap<String, IcalEventFacet> data;

    /**
     * Constructor
     */
    public SimpleIcalEvent(){
	data = new HashMap<String, IcalEventFacet>();
    }
    
    /**
     * Convenience method to get the DTSTART key as a Date
     * 
     * @return null if the field isn't set or can't be parsed
     */
    public Date getStartDate(){
	String date = get("DTSTART");
	if(date == null) return null;
	
	try{
	    return parseDate(date);
	}catch(ParseException e){
	    return null;
    	}
    }

    /**
     * Convenience method to get the DTEND key as a Date
     * 
     * @return null if the field isn't set or can't be parsed
     */
    public Date getEndDate(){
	String date = get("DTEND");
	if(date == null) return null;
	
	try{
	    return parseDate(date);
	}catch(ParseException e){
	    return null;
    	}
    }
    
    /**
     * Simplistic ical date parser
     * 
     * @param date A string in ical format
     * @return the parsed date
     * @throws java.text.ParseException if the parsing fails
     */
    public static Date parseDate(String date) throws ParseException {
	date = date.replaceAll("Z$", "-0000");
	date = date.replaceAll("([0-9][0-9]):([0-9][0-9])$", "$1$2");
	
	Date dt = null;
	try{
	    // with timezone first
	    SimpleDateFormat df = new SimpleDateFormat( "yyyyMMdd'T'HHmmssZ" );
	    dt = df.parse(date);
	}catch(ParseException e){
	    // no timezone
	    SimpleDateFormat df = new SimpleDateFormat( "yyyyMMdd'T'HHmmss" );
	    dt = df.parse(date);
	}
	return dt;
    }
    
    /**
     * Sets the value of the given key
     * 
     * @param key
     * @param val
     */
    public void set(String key, String val){
	key   = key.toUpperCase();
	IcalEventFacet facet = null;
	if(data.containsKey(key)){
	    facet = data.get(key);
	}else{
	    facet = new IcalEventFacet();
	    data.put(key,facet);
	}
	
	facet.set(val);
    }
    
    /**
     * Sets a parameter value of a given key
     * 
     * @param key
     * @param param
     * @param val
     */
    public void set(String key, String param, String val){
	key   = key.toUpperCase();
	param = param.toUpperCase();
	IcalEventFacet facet = null;
	if(data.containsKey(key)){
	    facet = data.get(key);
	}else{
	    facet = new IcalEventFacet();
	    data.put(key,facet);
	}
	
	facet.set(param,val);
    }
    
    /**
     * Returns the value of a given key
     * 
     * @param key
     * @return null if the key doesn't exist
     */
    public String get(String key){
	key = key.toUpperCase();
	if(data.containsKey(key)){
	    return data.get(key).get();
	}else{
	    return null;
	}
    }
    
    /**
     * Returns a parameter value of a given key
     *  
     * @param key
     * @param param
     * @return null if the key or parameter doesn't exist
     */
    public String get(String key, String param){
	key   = key.toUpperCase();
	param = param.toUpperCase();
	if(data.containsKey(key)){
	    return data.get(key).get(param);
	}else{
	    return null;
	}
    }
    
    private class IcalEventFacet {
	private String value;
	private HashMap<String, String> extra;
	
	public IcalEventFacet(){
	    extra = new HashMap<String, String>();
	}
	
	public void set(String val){
	    value = val;
	}
	
	public String get(){
	    return value;
	}
	
	public void set(String key, String val){
	    key   = key.toUpperCase();
	    extra.put(key, val);
	}
	
	public String get(String key){
	    key   = key.toUpperCase();
	    if(extra.containsKey(key)){
		return extra.get(key);
	    }else{
		return null;
	    }
	}
    }
}
