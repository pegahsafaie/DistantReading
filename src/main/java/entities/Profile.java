package entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pegah on 7/15/17.
 */
public class Profile {

    public Profile(){
        frequency = 1;
        verbs = new HashMap<String, Integer>();
        adjs = new HashMap<String, Integer>();
        locations = new ArrayList<>();
        temporals = new ArrayList<>();
    }
    private String name;
    private Map<String, Integer> verbs;
    private Map<String, Integer> adjs;
    private int positiveSentimentCount = 0;
    private int negativeSentimentCount = 0;
    private int veryPositiveSentimentCount = 0;
    private int veryNegativeSentimentCount= 0;
    private String quote;
    private Personality personality;
    private int frequency;
    private List<String> locations;
    private List<String> temporals;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public Map<String, Integer> getVerbs() {
        return verbs;
    }

    public void setVerbs(Map<String, Integer> verbs) {
        this.verbs = verbs;
    }

    public void addToVerbs(String verb){
        if(!verbs.keySet().contains(verb))
            this.verbs.put(verb,1);
        else{
            Integer frequency = this.verbs.get(verb);
            this.verbs.put(verb,++frequency);
        }

    }

    public Map<String, Integer> getAdjs() {
        return adjs;
    }

    public void setAdjs(Map<String, Integer> adjs) {
        this.adjs = adjs;
    }

    public void addToAdjs(String adj){
        if(!this.adjs.keySet().contains(adj))
            this.adjs.put(adj,1);
        else{
            Integer frequency = this.adjs.get(adj);
            this.adjs.put(adj, ++frequency);
        }
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Profile){
            Profile toCompareProfile = (Profile) o;
            return this.getName().equals(toCompareProfile.getName());
        }
        return false;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public Personality getPersonality() {
        return personality;
    }

    public void setPersonality(Personality personality) {
        this.personality = personality;
    }

    public int getPositiveSentimentCount() {
        return positiveSentimentCount;
    }

    public void setPositiveSentimentCount(int positiveSentimentCount) {
        this.positiveSentimentCount = positiveSentimentCount;
    }

    public int getNegativeSentimentCount() {
        return negativeSentimentCount;
    }

    public void setNegativeSentimentCount(int negativeSentimentCount) {
        this.negativeSentimentCount = negativeSentimentCount;
    }

    public int getVeryPositiveSentimentCount() {
        return veryPositiveSentimentCount;
    }

    public void setVeryPositiveSentimentCount(int veryPositiveSentimentCount) {
        this.veryPositiveSentimentCount = veryPositiveSentimentCount;
    }

    public int getVeryNegativeSentimentCount() {
        return veryNegativeSentimentCount;
    }

    public void setVeryNegativeSentimentCount(int veryNegativeSentimentCount) {
        this.veryNegativeSentimentCount = veryNegativeSentimentCount;
    }

    public void addToSentiment(String sentiment){
        if(sentiment.equals("Positive")){
            this.positiveSentimentCount++;
        }else if(sentiment.equals("Very positive")){
            this.veryPositiveSentimentCount++;
        }else if(sentiment.equals("Negative")){
            this.negativeSentimentCount++;
        }else if(sentiment.equals("Very negative")){
            this.veryNegativeSentimentCount++;
        }
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void addFrequency(){
        this.frequency++;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public List<String> getTemporals() {
        return temporals;
    }

    public void setTemporals(List<String> temporals) {
        this.temporals = temporals;
    }

    public void addLocation(String location){
        locations.add(location);
    }

    public void addTemporal(String temporal){
        temporals.add(temporal);
    }
}
