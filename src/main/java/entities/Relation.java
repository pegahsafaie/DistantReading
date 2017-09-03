package entities;

import java.util.HashMap;
import java.util.Map;

public class Relation {

    private String[] profileNames;

//    private int positiveSentimentCount = 0;
//    private int negativeSentimentCount = 0;
//    private int veryPositiveSentimentCount = 0;
//    private int veryNegativeSentimentCount = 0;
    private int frequency;
    private Map<String, String> typeName;


    public Relation(){
        this.frequency = 1;
        typeName = new HashMap<>();
    }

    public void setProfileNames(String[] profileNames) {
        this.profileNames = profileNames;
    }

    public String[] getProfileNames() {
        return profileNames;
    }

//    public int getPositiveSentimentCount() {
//        return positiveSentimentCount;
//    }
//
//    public void setPositiveSentimentCount(int positiveSentimentCount) {
//        this.positiveSentimentCount = positiveSentimentCount;
//    }
//
//    public int getNegativeSentimentCount() {
//        return negativeSentimentCount;
//    }
//
//    public void setNegativeSentimentCount(int negativeSentimentCount) {
//        this.negativeSentimentCount = negativeSentimentCount;
//    }
//
//    public int getVeryPositiveSentimentCount() {
//        return veryPositiveSentimentCount;
//    }
//
//    public void setVeryPositiveSentimentCount(int veryPositiveSentimentCount) {
//        this.veryPositiveSentimentCount = veryPositiveSentimentCount;
//    }
//
//    public int getVeryNegativeSentimentCount() {
//        return veryNegativeSentimentCount;
//    }
//
//    public void setVeryNegativeSentimentCount(int veryNegativeSentimentCount) {
//        this.veryNegativeSentimentCount = veryNegativeSentimentCount;
//    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

//    public void addToSentiment(String sentiment) {
//        if (sentiment.equals("Positive")) {
//            this.positiveSentimentCount++;
//        } else if (sentiment.equals("Very positive")) {
//            this.veryPositiveSentimentCount++;
//        } else if (sentiment.equals("Negative")) {
//            this.negativeSentimentCount++;
//        } else if (sentiment.equals("Very negative")) {
//            this.veryNegativeSentimentCount++;
//        }
//    }

    public void addFrequency() {
        this.frequency++;
    }

    public Map<String, String> getTypeName() {
        return typeName;
    }

    public void setTypeName(Map<String, String> typeName) {
        this.typeName = typeName;
    }

    public void addTypeName(String type, String name){
        this.typeName.put(type, name);
    }
}
