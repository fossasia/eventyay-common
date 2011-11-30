package org.splitbrain.simpleical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
	for (String date : dates) {
	    try{
		SimpleIcalEvent.parseDate(date);
	    }catch (Exception e){
		fails += e.getMessage()+"\n";
	    }
	}
	if(fails.length() > 0) fail(fails);

    }


    @Test
    public void testParseDuration1() {
	assertEquals(1000* (15*24*60*60 + 5*60*60 + 20), SimpleIcalEvent.parseDuration("P15DT5H0M20S"));
    }

    @Test
    public void testParseDuration2() {
	assertEquals(1000* (60*60), SimpleIcalEvent.parseDuration("PT1H00M"));
    }
}
