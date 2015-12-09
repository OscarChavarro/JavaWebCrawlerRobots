package databaseMongo.model;

/**
*/
public class HtmlExtraInformation {
    private int nh2;
    private int currentSection;
    public static final int SECTION_UNDEFINED = 0;
    public static final int SECTION_GENERAL = 1;
    public static final int SECTION_EXPERIENCE = 2;
    public static final int SECTION_EDUCATION = 3;
    public static final int SECTION_KNOWLEDGE_AND_SKILLS = 4;
    private boolean withGeneralSection;
    private String generalSectionTitle;

    public HtmlExtraInformation()
    {
        currentSection = SECTION_UNDEFINED;
        withGeneralSection = false;
        generalSectionTitle = null;
    }
    
    /**
     * @return the nh2
     */
    public int getNh2() {
        return nh2;
    }

    /**
     * @param nh2 the nh2 to set
     */
    public void setNh2(int nh2) {
        this.nh2 = nh2;
    }

    public void processH2(String name) {
        if ( name.equals("Experiencia Profesional") ) {
            currentSection = SECTION_EXPERIENCE;
        }
        else if ( name.equals("Formación") ) {
            currentSection = SECTION_EDUCATION;
        }
        else if ( name.equals("Habilidades y conocimientos") ) {
            currentSection = SECTION_KNOWLEDGE_AND_SKILLS;
        }
        else {
            if ( !withGeneralSection ) {
                withGeneralSection = true;
                currentSection = SECTION_GENERAL;
                setGeneralSectionTitle(name);
            }
            else {
                currentSection = SECTION_UNDEFINED;
                System.out.println("**** WARNING: Two unregistered sections!");
                System.out.println("  - " + name);
                System.out.println("  - " + getGeneralSectionTitle());
                //System.exit(9);
            }
        }
    }

    /**
    @return the generalSectionTitle
    */
    public String getGeneralSectionTitle() {
        return generalSectionTitle;
    }

    /**
     * @param generalSectionTitle the generalSectionTitle to set
     */
    public void setGeneralSectionTitle(String generalSectionTitle) {
        this.generalSectionTitle = generalSectionTitle;
    }
}
