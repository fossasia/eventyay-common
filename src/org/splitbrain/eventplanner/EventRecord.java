package org.splitbrain.eventplanner;

public class EventRecord {
    public String  id;
    public String  event;
    public String  title;
    public String  location;
    public String  speaker;
    public String  description;
    public long    starts;
    public long    ends;
    public boolean favorite;

    public EventRecord(){
        id          = "";
        event       = "only-one-for-now";
        title       = "";
        location    = "";
        speaker     = "";
        description = "";
        starts      = 0;
        ends        = 0;
        favorite    = false;
    }

}
