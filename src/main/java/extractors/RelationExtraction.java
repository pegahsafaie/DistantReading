package extractors;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import entities.ResourceClass;
import entities.Profile;
import entities.Relation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


public class RelationExtraction {

    List<Profile> profiles;
    ResourceClass pattern = new ResourceClass();
    public RelationExtraction(List<Profile> profiles) {
        this.profiles = profiles;
        String patterns = getFile("RelationshipPatterns");
        pattern = convertFromJson(patterns);
    }

    public List<Relation> extract(String content) {
        List<Relation> relations = new ArrayList<>();
        try {
//            Properties props = new Properties();
//            props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
//            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
//            Annotation annotation = pipeline.process(content);
//            List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
            CRFClassifier<CoreLabel> classifier = CRFClassifier.getDefaultClassifier();

            List<List<CoreLabel>> classify = classifier.classify(content);
            for (List<CoreLabel> coreLabels : classify) {

                StringBuilder sb = new StringBuilder();
                for (CoreLabel label : coreLabels) {
                    sb.append(label.word());
                    sb.append(" ");
                }
                addPossibleRelations(relations, coreLabels, sb.toString());
            }
//            for (CoreMap sentence : sentences) {
//                addPossibleRelations(relations, sentence);
//            }
            //check the names with current profiles
            for (Relation relation : relations) {
                boolean isThere = false;
                for (String name : relation.getProfileNames()) {
                    isThere = false;
                    for (Profile profile : profiles) {
                        if (name.trim().toLowerCase().contains(profile.getName().trim().toLowerCase())
                                || profile.getName().trim().toLowerCase().contains(name.trim().toLowerCase())) {
                            isThere = true;
                        }
                    }
                }
                if (!isThere)
                    relations.remove(relation);
            }


        } catch (Exception ex) {
            System.out.print("Error in relation extraction: " + ex.getMessage());
        }
        return relations;
    }

    private void addPossibleRelations(List<Relation> relations, List<CoreLabel> coreLabels, String sentence) {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < coreLabels.size(); i++) {
            CoreLabel token = coreLabels.get(i);
            String ne = token.get(CoreAnnotations.AnswerAnnotation.class);
            String namedEntity = "";
            while (ne.equals("PERSON") && !names.contains(token.value())) {
                namedEntity += " " + token.value();
                token = coreLabels.get(++i);
                ne = token.get(CoreAnnotations.AnswerAnnotation.class);
            }
            if(namedEntity != "")
                names.add(namedEntity.trim());
        }


        String relationType = "";
        String relationName = "";
        if (names.size() > 1) {
            for (String enemyRel : pattern.getEnemyRelations()) {
                if (sentence.contains("not " + enemyRel.toLowerCase().trim())) {
                    relationType = "friendship";
                    relationName = "not " + enemyRel;
                }else if(sentence.contains(enemyRel.toLowerCase().trim())){
                    relationType = "enemy";
                    relationName = enemyRel;
                }
            }
            for (String familyRel : pattern.getFamilyRelations()) {
                if (sentence.contains(familyRel.toLowerCase().trim())) {
                    relationType = "family";
                    relationName = familyRel;
                }
            }
            for (String romanticRel : pattern.getRomanticRelationShip()) {
                if (sentence.contains(romanticRel.toLowerCase().trim())) {
                    relationType = "romantic";
                    relationName = romanticRel;
                }
            }
            for (String friendRel : pattern.getFriendRelations()) {
                if (sentence.contains("not " + friendRel.toLowerCase().trim())) {
                    relationType = "enemy";
                    relationName = "not " + friendRel;
                }else if (sentence.contains(friendRel.toLowerCase().trim())) {
                    relationType = "friendship";
                    relationName = friendRel;
                }
            }
        }

        if (!relationName.equals("") && !relationType.equals("")) {
            String[] profileNames = names.toArray(new String[names.size()]);
            Arrays.sort(profileNames);
            boolean isDefined = false;
            for (Relation relation : relations) {
                if (Arrays.equals(relation.getProfileNames(), profileNames)) {
                    relation.addFrequency();
                    relation.addTypeName(relationType, relationName);
                    isDefined = true;
                }
            }
            if (!isDefined) {
                Relation newRelation = new Relation();
                newRelation.addTypeName(relationType, relationName);
                newRelation.setProfileNames(profileNames);
                relations.add(newRelation);
            }
        }
    }

    private String getFile(String fileName) {

        StringBuilder result = new StringBuilder("");

        //Get file from resources folder
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());

        try (Scanner scanner = new Scanner(file)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }

            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();

    }

    private ResourceClass convertFromJson(String jsonContent) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonContent, ResourceClass.class);
        } catch (Exception ex) {
            System.out.print("ERROR IN convertFromJson: " + ex.getMessage());
            return null;
        }
    }
}
