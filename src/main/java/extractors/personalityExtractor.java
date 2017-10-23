package extractors;

import entities.Personality;
import entities.Profile;
import recognizer.PersonalityRecognizer;

import java.util.List;
import java.util.Map;

public class personalityExtractor {

    List<Profile> profiles;
    boolean profileContainsCheck;
    boolean useNLPForQuoteExtraction;
    public personalityExtractor(List<Profile> profiles, boolean profileContainsCheck, boolean useNLPForQuoteExtraction){
        this.profiles = profiles;
        this.profileContainsCheck = profileContainsCheck;
        this.useNLPForQuoteExtraction = useNLPForQuoteExtraction;
    }

    public List<Profile> extract(String content, String mode) {

        QuoteExtractor quoteExtractor = new QuoteExtractor(profiles);
        List<Profile> profiles = quoteExtractor.QuoteExtractor(content, mode, profileContainsCheck, useNLPForQuoteExtraction);
        quoteExtractor = null;
        //Models are trained to output scores on a scale from 1 (low) to 7 (high),
        //we have two type of models. First one is based on data-set which is score by observers and the
        //other one is trained by the data-set which is scored by observers. the second one
        // is more accurate. so selfReport is false

        //the most accurate algorithm is SVM, model number 4

        for (Profile profile:profiles) {
            String quote = profile.getQuote();
            if(quote != null && quote.length() > 100) {
                quote = quote.replace("'|\"|´|`","");
                double[] scores = PersonalityRecognizer.extractPersonality(4, false, quote);
                Personality personality = new Personality();
                personality.setAgreeableness(scores[0]);
                personality.setConscientiousness(scores[1]);
                //Emotional stability
                personality.setNeuroticism(scores[2]);
                personality.setExtroversion(scores[3]);
                personality.setOpenness(scores[4]);
                profile.setPersonality(personality);
            }
        }
        return profiles;
    }

}
