package ie.ucc.salgadoe.pinbsapp;

/**
 * Created by SalgadoE on 19/04/2018.
 */

public class DataPatients {

    String idName;
    String birthDate;

    public DataPatients( String idName, String birthDate) {
        this.idName = idName;
        this.birthDate = birthDate;
    }
    public String getPatientName(){
        return idName;
    }
}
