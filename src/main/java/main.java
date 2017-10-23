import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pegah on 8/26/17.
 */
public class main {

    public static void main(String[] args)
    {
        boolean profileContainsCheck = true;
        boolean removeProfilesWitoutInfo = true;
        boolean removeProfilesWithFreq1 = true;
        boolean removeEventsWithoutLocationAndObejct = true;//time does not matter
        boolean removeEventsWithNoCharacterObject = false;
        boolean coreferenceWithCoreNLP = false;
        boolean extractPersonalityAndOrg = false;
        boolean useNLPForQuoteExtraction = false;
        boolean useQuote = true;
        if (args.length < 3) {
            System.out.print("you should specify input file, output folder and mode.");
            return;
        }

        if(args[2].equals("eval")){
            String taggedProfileFile = "";
            String taggedRelationFile = "";
            String generatedProfileFile = "";
            String generatedRelationFile = "";
            Evaluator eval = new Evaluator(true, taggedRelationFile, taggedProfileFile,generatedProfileFile,generatedRelationFile);
            eval.strongCompareResults();
        }else{
            List<String> pipeLine = new ArrayList<>();
            pipeLine.add("profile");
            pipeLine.add("event");
            pipeLine.add("relation");
            pipeLine.add("temporal");
            ReadStory readStory = new ReadStory(profileContainsCheck, removeProfilesWitoutInfo , removeProfilesWithFreq1, removeEventsWithoutLocationAndObejct, removeEventsWithNoCharacterObject,extractPersonalityAndOrg, coreferenceWithCoreNLP, useQuote, useNLPForQuoteExtraction);
            readStory.returnJson(args[0], args[1], args[2], pipeLine);
        }
    }
}
