package extractors;

import entities.Profile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuoteExtractor {

    List<Profile> profiles;
    public QuoteExtractor(List<Profile> profiles){
        this.profiles = profiles;
    }
    public String[] DIALOG_VERBS = {"acknowledged", "admitted", "agreed", "answered", "argued", "asked", "barked", "begged", "bellowed", "blustered", "bragged", "complained", "confessed", "cried", "demanded", "denied", "giggled", "hinted", "hissed", "howled", "inquired", "interrupted", "laughed", "lied", "mumbled", "muttered", "nagged", "pleaded", "promised", "questioned", "remembered", "replied", "requested", "retorted", "roared", "sang", "screamed", "screeched", "shouted", "sighed", "snarled", "sobbed", "threatened", "wailed", "warned", "whined", "whispered", "wondered", "yelled", "responded", "stammered", "said", "told", "wrote", "saying"};
    Map<String, String> quotesPerNer = new HashMap<String, String>();

    public List<Profile> QuoteExtractor(String content, String mode) {
        try {
            System.out.print("Start Quote Extraction");
            if (mode.equals("interview"))//in this case we dont use coreference
                auoteExtractorLine_pattern3(content);
            else{
                auoteExtractorLine_pattern1(content);
                auoteExtractorLine_pattern2(content);
            }
            mapDicToProfiles();
            System.out.print("End Quote Extraction");

        } catch (Exception ex) {
            System.out.print(ex.getMessage());
        }

        return profiles;
    }

    private void auoteExtractorLine_pattern3(String content) {

        try {
            System.out.println("Start Finding interview Quotes...");
            String lines[] = content.split("\\r?\\n");

            for (String strLine : lines) {
                String pattern2 = "((\\w*\\W*){1,3}):(.*?)";
                Pattern r2 = Pattern.compile(pattern2);

                // Now create matcher object.
                Matcher m2 = r2.matcher(strLine);
                while (m2.find()) {
                    System.out.println("Finding interview Quotes...");
                    System.out.println(strLine);
                    String speaker = m2.group(1);
                    String quote = strLine.split(":")[1];
                    for (Profile profile : profiles) {
                        String NER = profile.getName();
                        if (speaker.trim().toLowerCase().contains(NER.trim().toLowerCase())) {
                            quote += (quotesPerNer.get(NER) == null) ? "" : quotesPerNer.get(NER);
                            quotesPerNer.put(NER, quote);
                        }
                    }
                }
                pattern2 = "((\\w*\\W*){1,3})-(.*?)";
                r2 = Pattern.compile(pattern2);

                // Now create matcher object.
                m2 = r2.matcher(strLine);
                while (m2.find()) {
                    System.out.println("Finding interview Quotes...");
                    System.out.println(strLine);
                    String speaker = m2.group(1);
                    String quote = strLine.split(":")[1];
                    for (Profile profile : profiles) {
                        String NER = profile.getName();
                        if (speaker.trim().toLowerCase().contains(NER.trim().toLowerCase())) {
                            quote += (quotesPerNer.get(NER) == null) ? "" : quotesPerNer.get(NER);
                            quotesPerNer.put(NER, quote);
                        }
                    }
                }
            }
            System.out.print("End Finding interview Quotes");

        } catch (Exception ex) {
            System.out.print("ERROR IN INTERVIEW REGEX PATTERN " + ex.getMessage());
        }

    }

    private void auoteExtractorLine_pattern2(String content) {

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
                        String NER = profile.getName();
                        if (speaker.trim().toLowerCase().contains(NER.trim().toLowerCase())) {
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

    private void auoteExtractorLine_pattern1(String content) {

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
                        for (Profile profile : profiles) {
                            String NER = profile.getName();
                            if (speaker.trim().toLowerCase().contains(NER.trim().toLowerCase())) {
                                quote += (quotesPerNer.get(NER) == null) ? "" : quotesPerNer.get(NER);
                                quotesPerNer.put(NER, quote);
                            }
                        }
                    } else {
                        System.out.print("problem in speaker group finding");
                    }
                }
            }
        } catch (Exception ex) {
            System.out.print("ERROR IN STORY REGEX PATTERN 1" + ex.getMessage());
        }
    }

    private void mapDicToProfiles() {
        for (Map.Entry<String, String> entry : quotesPerNer.entrySet()) {
            String NER = entry.getKey();
            String quote = entry.getValue();
            for (Profile profile : profiles) {
                if (profile.getName().equals(NER))
                    profile.setQuote(quote);
            }
        }
    }
}