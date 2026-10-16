public class Person {
    private String id;
    private String nama;
    private String negara;
    private int tahunLahir;
    private String role;
    private String skillLevel;

    // konstruktor kosong
    public Person(){
    }

    // konstruktor
    public Person(String id, String nama, String negara, int tahunLahir, String role, String skillLevel){
        this.id = id;
        this.nama = nama;
        this.negara = negara;
        this.tahunLahir = tahunLahir;
        this.role = role;
        this.skillLevel = skillLevel;
    }

    // getter
    public String getId(){
        return id;
    }
    public String getNama(){
        return nama;
    }
    public String getNegara(){
        return negara;
    }
    public int getTahunLahir(){
        return tahunLahir;
    }
    public String getRole(){
        return role;
    }
    public String getSkillLevel(){
        return skillLevel;
    }

    // setter
    public void setId(String id){
        this.id = id;
    }
    public void setNama(String nama){
        this.nama = nama;
    }
    // BARU: setter negara, dibutuhkan oleh fitur Update
    public void setNegara(String negara){
        this.negara = negara;
    }
    public void setTahunLahir(int tahunLahir){
        this.tahunLahir = tahunLahir;
    }
    public void setRole(String role){
        this.role = role;
    }
    public void setSkillLevel(String skillLevel){
        this.skillLevel = skillLevel;
    }
}