/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.Enum;

/**
 *
 * @author root
 */
public enum DocumentTypeEnum {    
    
    
    Textbook(0,"Textbook","#23334D"),
    Book(1,"Curriculum","#278036"),
    Other_Educational_Book(2,"Other Educational Book","#41a950"),
    Curriculum(3,"Book","#87d69a"),
    Periodical(4,"Periodical","#1773d1"),
    Monograph(5,"Monograph","#a7a5a8"),
    Catalogue(6,"Catalogue","#577657"),
    Book_Part(7,"Book Part","#000183"),    
    Book_Review(8,"Book Review","#000183"),
    Reference_Book(9,"Reference Book","#0539b6"),
    Research(10,"Research","#C7898B"),
    Conference_Object(11,"Conference Object","#B1626D"),
    Conference_Paper(12,"Conference Paper","#2B3B51"),
    Conference_Poster(13,"Conference Poster","#644E3D"),
    Conference_Proceedings(14,"Conference_Proceedings","#FDFD96"),
    Dataset(15,"Dataset","#C8F902"),
    Exhibition(16,"Exhibition","#82D305"),
    Interactive_Resource(18,"Interactive Resource","#68C3A0"),
    Journal_Article(19,"Journal Article","#82D305"),
    Lecture(20,"Lecture","#000183"),
    Patent(21,"Patent","#CC81A1"),
    Policy_Report(22,"Policy Report","#CC81A1"),
    Research_Report(22,"Research Report","#82D305"),
    Technical_Report(22,"Technical Report","#CC81A1"),
    Other_Report(22,"Other_Report","#000183"), 
    Software(22,"Software","#2B3B51"),
    Thesis_Doctoral(22,"Thesis Doctoral","#B1626D"),
    Thesis_Masters(22,"Thesis Masters","#0539b6"),
    Manual(22,"Manual","#CC81A1"),
    Question_Paper(22,"Question Paper","#000183"),
    Nai_Talim(22,"Nai Talim","#C7898B"),
    Conversations_on_Schoolbooks(22,"Conversations on Schoolbooks","#000183"),
    Other(17,"Other","#CC8FAB");
    
    
    private int id;
    private String name;
    private String colour;
    

    private DocumentTypeEnum(int id,String name,String colour) {
        this.id = id;
        this.name=name;
        this.colour=colour;
    }
    public static DocumentTypeEnum getBykey(int key) {
        for (DocumentTypeEnum t : values()) {
            if (t.getId()==key) {
                return t;
            }
        }
        return null;
    }

    public static DocumentTypeEnum getDeploymentClass(String name) {
        for (DocumentTypeEnum t : values()) {
            if (t.getName().equalsIgnoreCase(name)) {
                return t;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }
    

    
}
