import java.util.ArrayList;
import java.util.List;

/**
 * Created by pegah on 8/26/17.
 */
public class main {

    public static void main(String[] args)
    {
        if (args.length < 3) {
            System.out.print("you should specify input file, output folder and mode.");
            return;
        }

        if(args[2].equals("eval")){
            Evaluator eval = new Evaluator(true);
            eval.strongCompareResults();
        }else{
            List<String> pipeLine = new ArrayList<>();
            pipeLine.add("profile");
//        pipeLine.add("event");
            pipeLine.add("relation");
            pipeLine.add("temporal");
            ReadStory readStory = new ReadStory();
            readStory.returnJson(args[0], args[1], args[2], pipeLine);
        }
    }
}
