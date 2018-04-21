package ie.ucc.salgadoe.pinbsapp;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SalgadoE on 19/04/2018.
 */

public class CustomAdapter extends ArrayAdapter<DataPatients> implements Filterable {

    Context context;
    int layaoutResourceId;
    List<DataPatients> data = null;
    List<DataPatients> searched_data= null;
    PatientFilter patientFilter;

    public CustomAdapter(@NonNull Context context, int resource, @NonNull List<DataPatients> objects) {
        super(context, resource, objects);

        this.layaoutResourceId = resource;
        this.context = context;
        this.data = objects;
        searched_data = data;
    }

    static  class DataHolder{
        TextView tvId;
        TextView tvBirth;

    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    DataHolder holder = null;
    getCount();
    if(convertView==null){

        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        convertView = inflater.inflate(layaoutResourceId,parent,false);

        holder = new DataHolder();
        holder.tvId = (TextView)convertView.findViewById(R.id.textID);
        holder.tvBirth = (TextView)convertView.findViewById(R.id.textBirthDate);

        convertView.setTag(holder);

    }
    else{
        holder = (DataHolder)convertView.getTag();
    }

    DataPatients dataPatients = searched_data.get(position);
    holder.tvBirth.setText(dataPatients.birthDate);
    holder.tvId.setText(dataPatients.idName);
    return convertView;

    }
 private class PatientFilter extends Filter{

     @Override
     protected FilterResults performFiltering(CharSequence constraint) {
         FilterResults results = new FilterResults();
         //Filter logic
         if(constraint == null || constraint.length()==0){
             results.values = data;
             results.count = data.size();

         }
         else{
             List<DataPatients> nPatientsList = new ArrayList<DataPatients>();
             for(int i =0; i<data.size();i++){
                 if(searched_data.get(i).idName.toUpperCase().startsWith(constraint.toString().toUpperCase())){
                     nPatientsList.add(searched_data.get(i));
                 }
             }
             results.values = nPatientsList;
             results.count = nPatientsList.size();
         }
         return  results;
     }
     @Override
     protected void publishResults(CharSequence constraint,FilterResults results){
         //inform the adapter about the new list filtered
         if(results.count==0){
             notifyDataSetInvalidated();
         }
         else{
             searched_data = (List<DataPatients>) results.values;
             notifyDataSetChanged();
         }

     }

 }
    @Override
    public Filter getFilter(){
        if(patientFilter == null){
            patientFilter = new PatientFilter();
        }
        return patientFilter;

    }
    @Override
    public int getCount(){
        return searched_data.size();
    }
    public List<DataPatients> getListUpdated(){
        return searched_data;
    }
}
