package entities;

import java.util.Map;

/**
 * Created by pegah on 6/26/17.
 */
public class Event {
    private String sentence;
    private String verb;
    private String lemmatizedVerb;
    private String subject;
    private String predicate;
    private String object;
    private Context[] vContexts;
    private int sentiment = 0;

    private String verbNetId;
    private String proBankId;
    private Map<String,String> probArguments;
    private Map<String,String> verbNetArguments;
    private String verbTense;

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

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

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public Context[] getvContexts() {
        return vContexts;
    }

    public void setvContexts(Context[] vContexts) {
        this.vContexts = vContexts;
    }

    public String getVerbNetId() {
        return verbNetId;
    }

    public void setVerbNetId(String verbNetId) {
        this.verbNetId = verbNetId;
    }

    public String getProBankId() {
        return proBankId;
    }

    public void setProBankId(String proBankId) {
        this.proBankId = proBankId;
    }

    public String getVerbTense() {
        return verbTense;
    }

    public void setVerbTense(String verbTense) {
        this.verbTense = verbTense;
    }

        public Map<String, String> getProbArguments() {
        return probArguments;
    }

    public void setProbArguments(Map<String, String> probArguments) {
        this.probArguments = probArguments;
    }

    public Map<String, String> getVerbNetArguments() {
        return verbNetArguments;
    }

    public void setVerbNetArguments(Map<String, String> verbNetArguments) {
        this.verbNetArguments = verbNetArguments;
    }

    public int getSentiment() {
        return sentiment;
    }

    public void setSentiment(int sentiment) {
        this.sentiment = sentiment;
    }

    public void addToSentiment(int sentiment){
        this.sentiment += sentiment;
    }
}
