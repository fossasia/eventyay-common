package org.splitbrain.simpleical;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A simple, optimistic approach to parsing iCal data
 * <p/>
 * This parser doesn't handle most of the specialities of RFC2445 but is able
 * to find at least simpler values . If you need completeness use ical4j, if
 * you need something simple with a very low footprint this might be the right
 * thing for you.
 *
 * @author Andreas Gohr <andi@splitbrain.org>
 */
public class SimpleIcalParser {

    private String line = null;
    private String nextline = null;
    private BufferedReader reader = null;

    /**
     * Initializes the parser on the given input stream
     *
     * @param is Stream to the iCal formatted data to parse
     */
    public SimpleIcalParser(InputStream is) {
        reader = new BufferedReader(new InputStreamReader(is));
    }

    /**
     * Read the next VEvent from the stream
     *
     * @return The Event or null if no more events can be found.
     * @throws IOException when reading from the stream fails
     */
    public SimpleIcalEvent nextEvent() throws IOException {
        SimpleIcalEvent event = null;

        String lineup = null;
        if (line == null) line = reader.readLine();
        while (line != null) {
            nextline = reader.readLine();
            lineup = line.toUpperCase();

            // look into the future for unfolding
            if ((nextline != null) && nextline.matches("^\\s.*")) {
                line = line + nextline.replaceAll("^\\s", "");
                continue;
            }

            if (lineup.startsWith("BEGIN:VEVENT")) {
                event = new SimpleIcalEvent();
            } else if (event == null) {
                // we're not in a event yet. do nothing
            } else if (lineup.startsWith("END:VEVENT")) {
                // we're done
                line = nextline;
                return event;
            } else {
                String frst = ""; // remembers first encountered parameter as possible value for convenience
                String key = "";
                String val = "";
                String[] params = null;
                String[] opts = null;
                String[] alt = null;

                int col = line.indexOf(":");
                if (col != -1) {
                    // split into key/value pair
                    key = line.substring(0, col);
                    val = line.substring(col + 1);

                    // key might contain parameters
                    params = key.split("(?<!\\\\);");
                    key = params[0]; // first part is the real key name
                    for (int i = 1; i < params.length; i++) {
                        opts = params[i].split("(?<!\\\\)=", 2);
                        // add all parameters as extra
                        if (opts.length == 2) {
                            event.set(key, unslash(opts[0]), unslash(opts[1]));
                            if (opts[0].equals("CN")) {
                                val = opts[1] + ';' + val; // always use CN as value if not yet set
                            } else if (frst.length() == 0) {
                                frst = opts[0]; // copy 1st one to value if not yet set
                            }
                        } else {
                            event.set(key, unslash(opts[0]), ""); // add as empty parameter
                            if (frst.length() == 0)
                                frst = opts[0]; // copy 1st one to value if not yet set
                        }
                    }

                    // value might contain parameters
                    params = val.split("(?<!\\\\)[;]");
                    val = "";
                    for (String param : params) {
                        opts = param.split("(?<!\\\\)[=]", 2);
                        if (opts.length == 2) {
                            // add parameter as extra
                            event.set(key, unslash(opts[0]), unslash(opts[1]));
                        } else {
                            // seems not to be a parameter, set as value if not yet set
                            if (val.length() == 0) {
                                val = opts[0];
                            } else {
                                // we have a value already, maybe it's an inline parameter like MAILTO:
                                alt = opts[0].split("(?<!\\\\)[=]", 2);
                                if (alt.length == 2) {
                                    event.set(key, unslash(alt[0]), unslash(alt[1]));
                                } else {
                                    event.set(key, unslash(opts[0]), ""); // add as empty parameter
                                }
                            }
                        }
                    }

                    // set value to event
                    if (val.length() == 0) val = frst;
                    event.set(key, unslash(val));
                }
            }

            line = nextline;
        }
        return null; //Shouldn't be reached except for EOF
    }

    private String unslash(String str) {
        str = str.replaceAll("\\\\n", "\n");
        str = str.replaceAll("\\\\(.)", "$1");
        return str.trim();
    }
}
