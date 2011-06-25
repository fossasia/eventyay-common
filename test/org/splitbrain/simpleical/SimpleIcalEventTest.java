package org.splitbrain.simpleical;

import static org.junit.Assert.*;

import org.junit.Test;

public class SimpleIcalEventTest {

    @Test
    public void testParseDate() {
	String[] dates = {
		"20110413T170000",
		"20110413T170000Z",
		"20110413T170000+03:12",
		"20110413T170000+0312"
	};
	
	String fails = "";
	for(int i=0; i < dates.length; i++){
	    try{
		SimpleIcalEvent.parseDate(dates[i]);
	    }catch (Exception e){
		fails += e.getMessage()+"\n";
	    }
	}
	if(fails.length() > 0) fail(fails);

    }

}
