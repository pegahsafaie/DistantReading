package entities;

public class ResourceClass {
    private String[] familyRelations;
    private String[] friendRelations;
    private String[] romanticRelationShip;
    private String[] enemyRelations;

    public String[] getFamilyRelations() {
        return familyRelations;
    }

    public void setFamilyRelations(String[] familyRelations) {
        this.familyRelations = familyRelations;
    }

    public String[] getFriendRelations() {
        return friendRelations;
    }

    public void setFriendRelations(String[] friendRelations) {
        this.friendRelations = friendRelations;
    }

    public String[] getRomanticRelationShip() {
        return romanticRelationShip;
    }

    public void setRomanticRelationShip(String[] romanticRelationShip) {
        this.romanticRelationShip = romanticRelationShip;
    }

    public String[] getEnemyRelations() {
        return enemyRelations;
    }

    public void setEnemyRelations(String[] enemyRelations) {
        this.enemyRelations = enemyRelations;
    }
}
