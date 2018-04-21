package Utilities;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by SalgadoE on 05/03/2018.
 */

public class DataHelper implements Serializable {

    //Data Helper for sending arraylists between activities

    private ArrayList<Double> data;

    public DataHelper(ArrayList<Double> data){
        this.data = data;
    }
    public ArrayList<Double> getList(){
        return this.data;
    }
}

