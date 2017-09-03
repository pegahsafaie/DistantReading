package entities;

/**
 * Created by pegah on 7/10/17.
 */
public class Context {
    private String classification;
    private String text;
    private String[] eventDateTime;//optional for Temporal context

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String[] getEventDateTime() {
        return eventDateTime;
    }

    public void setEventDateTime(String[] eventDateTime) {
        this.eventDateTime = eventDateTime;
    }
}
