package entities;

import java.util.Map;

/**
 * Created by pegah on 6/26/17.
 */
public class Event {
    private String[] locations;
    private String[] times;
    private String lemmatizedVerb;
    private String subject;
    private String object;

    public String getLemmatizedVerb() {
        return lemmatizedVerb;
    }

    public void setLemmatizedVerb(String lemmatizedVerb) {
        this.lemmatizedVerb = lemmatizedVerb;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String[] getLocations() {
        return locations;
    }

    public void setLocations(String[] locations) {
        this.locations = locations;
    }

    public String[] getTimes() {
        return times;
    }

    public void setTimes(String[] times) {
        this.times = times;
    }
}
