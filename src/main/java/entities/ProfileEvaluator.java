package entities;

/**
 * Created by pegah on 9/24/17.
 */
public class ProfileEvaluator {

    private String name;
    private String[] adj;
    private String[] verb;
    private String[] location;
    private String personality;
    private String sentiment;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getAdj() {
        return adj;
    }

    public void setAdj(String[] adj) {
        this.adj = adj;
    }

    public String[] getVerb() {
        return verb;
    }

    public void setVerb(String[] verb) {
        this.verb = verb;
    }

    public String[] getLocation() {
        return location;
    }

    public void setLocation(String[] location) {
        this.location = location;
    }

    public String getPersonality() {
        return personality;
    }

    public void setPersonality(String personality) {
        this.personality = personality;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }
}
