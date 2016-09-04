package org.splitbrain.giraffe;

public class EventRecord {
    public String id;
    public String title;
    public String location;
    public String speaker;
    public String description;
    public long starts;
    public long ends;
    public boolean favorite;
    public String url;

    public EventRecord() {
        id = "";
        title = "";
        location = "";
        speaker = "";
        description = "";
        starts = 0;
        ends = 0;
        favorite = false;
        url = "";
    }

}
