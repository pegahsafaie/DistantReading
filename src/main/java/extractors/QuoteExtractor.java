package extractors;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.QuoteAnnotator;
import edu.stanford.nlp.pipeline.QuoteAttributionAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import entities.Profile;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuoteExtractor {

    List<Profile> profiles;
    Properties props = new Properties();
    StanfordCoreNLP pipeline;

    public QuoteExtractor(List<Profile> profiles) {
        this.profiles = profiles;
    }

    public String[] DIALOG_VERBS = {"acknowledged", "admitted", "agreed", "answered", "argued", "asked", "barked", "begged", "bellowed", "blustered", "bragged", "complained", "confessed", "cried", "demanded", "denied", "giggled", "hinted", "hissed", "howled", "inquired", "interrupted", "laughed", "lied", "mumbled", "muttered", "nagged", "pleaded", "promised", "questioned", "remembered", "replied", "requested", "retorted", "roared", "sang", "screamed", "screeched", "shouted", "sighed", "snarled", "sobbed", "threatened", "wailed", "warned", "whined", "whispered", "wondered", "yelled", "responded", "stammered", "said", "told", "wrote", "saying"};
    Map<String, String> quotesPerNer = new HashMap<String, String>();

    public List<Profile> QuoteExtractor(String content, String mode, boolean profileContainsCheck, boolean useNLPForQuoteExtraction) {
        try {
            System.out.println("Start Quote Extraction " + getTime());
            if (useNLPForQuoteExtraction) {
                props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,entitymentions,quote,quoteattribution");
                pipeline = new StanfordCoreNLP(props);
                String[] chapters = content.split("\n\n\n\n");
                for (String chapter : chapters) {
                    quoteExtraction_NLP(chapter, profileContainsCheck);
                }
            } else {
                //TODO: I think we should not do it just for interview. what about a theater?
                if (mode.equals("interview"))//in this case we dont use coreference
                    auoteExtractorLine_pattern3(content, profileContainsCheck);
                else {
                    auoteExtractorLine_pattern1(content, profileContainsCheck);
                    auoteExtractorLine_pattern2(content, profileContainsCheck);
                }
            }
            mapDicToProfiles();
            System.out.println("End Quote Extraction " + getTime());

        } catch (Exception ex) {
            System.out.println("Error in Quote extraction: " + ex.getMessage());
        }

        return profiles;
    }

    private void quoteExtraction_NLP(String content, boolean profileContainsCheck) {

        Annotation document = new Annotation(content);
        List<CoreMap> quotes = new ArrayList<>();
        try {
            pipeline.annotate(document);
            quotes = document.get(CoreAnnotations.QuotationsAnnotation.class);
            if (quotes != null && quotes.size() > 0)
                for (CoreMap quote : quotes) {
                    try {
                        String speaker = quote.get(QuoteAttributionAnnotator.MentionAnnotation.class);
                        System.out.println(speaker);
                        if (speaker != null)
                            for (Profile profile : profiles) {
                                String NER = profile.getName().trim().toLowerCase();
                                speaker = speaker.trim().toLowerCase();
                                if ((profileContainsCheck && (speaker.contains(NER) || NER.contains(speaker))) || (!profileContainsCheck && speaker.equals(NER))) {
                                    quotesPerNer.put(NER, quote.toString());
                                }
                            }
                    } catch (Exception ex) {
                        System.out.println("Error in Quote extraction by NLP: " + ex.getMessage());
                    }
                }
        }
        catch(Exception ex){
            System.out.println("Error in Quote extraction by NLP: " + ex.getMessage());
        }

    }

    private void auoteExtractorLine_pattern3(String content, boolean profileContainsCheck) {

        try {
            System.out.println("Start Finding interview Quotes...");
            String lines[] = content.split("\\r?\\n");

            for (String strLine : lines) {
                try {
                    String pattern2 = "((\\w*\\W*){1,3}):(.*?)";
                    Pattern r2 = Pattern.compile(pattern2);

                    // Now create matcher object.
                    Matcher m2 = r2.matcher(strLine);
                    while (m2.find()) {

                        String speaker = m2.group(1);
                        String quote = strLine.split(":")[1];
                        if (speaker != null) {
                            speaker = speaker.trim().toLowerCase();
                            for (Profile profile : profiles) {
                                String NER = profile.getName().toLowerCase().trim();

                                if ((profileContainsCheck && (speaker.contains(NER) || NER.contains(speaker)))
                                        || (!profileContainsCheck && speaker.equals(NER))) {
                                    quote += (quotesPerNer.get(NER) == null) ? "" : quotesPerNer.get(NER);
                                    quotesPerNer.put(NER, quote);
                                }
                            }
                        }
                    }
                    pattern2 = "((\\w*\\W*){1,3})-(.*?)";
                    r2 = Pattern.compile(pattern2);

                    // Now create matcher object.
                    m2 = r2.matcher(strLine);
                    while (m2.find()) {

                        String speaker = m2.group(1);
                        String quote = strLine.split(":")[1];
                        if (speaker != null) {
                            speaker = speaker.trim().toLowerCase();
                            for (Profile profile : profiles) {
                                String NER = profile.getName().trim().toLowerCase();
                                if ((profileContainsCheck && (speaker.equals(NER) || NER.contains(speaker)))
                                        || (!profileContainsCheck && NER.equals(speaker))) {
                                    quote += (quotesPerNer.get(NER) == null) ? "" : quotesPerNer.get(NER);
                                    quotesPerNer.put(NER, quote);
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("ERROR IN INTERVIEW REGEX PATTERN " + ex.getMessage());
                }
            }
            System.out.println("End Finding interview Quotes");

        } catch (Exception ex) {
            System.out.println("ERROR IN INTERVIEW REGEX PATTERN " + ex.getMessage());
        }

    }

    private void auoteExtractorLine_pattern2(String content, boolean profileContainsCheck) {

        //search for the quotes, those after them, there are between 1 to 5 word and then one special verb.
        //like "\"I am tired.\" my mother told."
        String allPossibleVerbs = "";
        for (String dialogVerb : DIALOG_VERBS) {
            allPossibleVerbs += "|" + dialogVerb;
        }
        allPossibleVerbs = "none" + allPossibleVerbs;
        String pattern = "(.*?)(\"(.*?)\"|\'(.*?)\'|(``(.*)'')|(''(.*)'')|(``(.*)``)|(`(.*)`)|(`(.*)')|('(.*)`))(\\s)((\\w*\\W*){1,3}(" + allPossibleVerbs + ")(.*))";
        Pattern r = Pattern.compile(pattern);

        String[] sentences = content.split("([a-z]*)\\.\\s*");
        for (String sentence : sentences) {
            Matcher m = r.matcher(sentence);
            while (m.find()) {
                String quote = m.group(2);
                String speaker = m.group(18);
                if (speaker != null) {
                    for (Profile profile : profiles) {
                        String NER = profile.getName().trim().toLowerCase();
                        speaker = speaker.trim().toLowerCase();
                        if ((profileContainsCheck && (speaker.contains(NER) || NER.contains(speaker))) || (!profileContainsCheck && speaker.equals(NER))) {
                            quote += (quotesPerNer.get(NER) == null) ? "" : quotesPerNer.get(NER);
                            quotesPerNer.put(NER, quote);
                        }
                    }
                } else {
                    System.out.print("problem in speaker group finding");
                }
            }
        }
    }

    private void auoteExtractorLine_pattern1(String content, boolean profileContainsCheck) {

        try {

            String allPossibleVerbs = "";
            for (String dialogVerb : DIALOG_VERBS) {
                allPossibleVerbs += "|" + dialogVerb;
            }
            allPossibleVerbs = "none" + allPossibleVerbs;
            String pattern2 = "(.*)(" + allPossibleVerbs + ")( *),( *)((\"(.*)\")|(\'(.*)\')|(``(.*)``)|(``(.*)''))(.*)";
            Pattern r2 = Pattern.compile(pattern2);


            String[] sentences = content.split("([a-z]*)\\.\\s*");
            for (String sentence : sentences) {

                Matcher m2 = r2.matcher(sentence);
                while (m2.find()) {
                    String quote = m2.group(5);
                    String speaker = m2.group(1);
                    if (speaker != null) {
                        speaker = speaker.trim().toLowerCase();
                        for (Profile profile : profiles) {
                            String NER = profile.getName().trim().toLowerCase();
                            if ((profileContainsCheck && (speaker.contains(NER) || NER.contains(speaker))) ||
                                    (!profileContainsCheck && speaker.equals(NER))) {
                                quote += (quotesPerNer.get(NER) == null) ? "" : quotesPerNer.get(NER);
                                quotesPerNer.put(NER, quote);
                            }
                        }
                    } else {
                        System.out.println("problem in speaker group finding");
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("ERROR IN STORY REGEX PATTERN 1" + ex.getMessage());
        }
    }

    private void mapDicToProfiles() {
        for (Map.Entry<String, String> entry : quotesPerNer.entrySet()) {
            String NER = entry.getKey();
            String quote = entry.getValue();
            for (Profile profile : profiles) {
                if (profile.getName().toLowerCase().equals(NER))
                    profile.setQuote(quote);
            }
        }
    }

    private String getTime(){
        DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        Date dateobj = new Date();
        return df.format(dateobj);
    }

}