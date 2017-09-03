package entities;

import java.util.*;


public class Chapter {
    private int chapterIndex;
    private HashMap<String, Map<String, Integer>> profiles;

    public Chapter(){
        profiles = new HashMap<String, Map<String, Integer>>() {
        };
    }

    public int getChapterIndex() {
        return chapterIndex;
    }

    public void setChapterIndex(int chapterIndex) {
        this.chapterIndex = chapterIndex;
    }

    public HashMap<String, Map<String, Integer>> getProfiles() {
        return profiles;
    }

    public void setProfiles(HashMap<String, Map<String, Integer>> profiles) {
        this.profiles = profiles;
    }
}
