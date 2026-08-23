package competitivecounting.vaults.trophyvault;

public class WikiArticelObject {
    private final String title;
    private final SectionObject[] sectionObjects;

    public WikiArticelObject(String title, SectionObject[] sectionObjects) {
        this.title = title;
        this.sectionObjects = sectionObjects;
    }

    public String getTitle() {
        return title;
    }

    public SectionObject[] getSectionObjects() {
        return sectionObjects;
    }
}
