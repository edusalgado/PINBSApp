package ie.ucc.salgadoe.pinbsapp;


import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.ListViewAutoScrollHelper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FirstPatientFragment extends Fragment {

    List<DataPatients> lstData;
    CustomAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_first_patient, container, false);
        setHasOptionsMenu(true);

        lstData = new ArrayList<>();

        lstData.add(new DataPatients("Andriy","10/7/1990"));
        lstData.add(new DataPatients("Emmanuel","10/7/1990"));
        lstData.add(new DataPatients("Alison","10/7/1990"));
        lstData.add(new DataPatients("Oksana","10/7/1990"));
        lstData.add(new DataPatients("Mark","10/7/1990"));
        lstData.add(new DataPatients("Sergi","10/7/1990"));
        lstData.add(new DataPatients("Kevin","10/7/1990"));
        lstData.add(new DataPatients("Rat","10/7/1990"));
        lstData.add(new DataPatients("Paola","10/7/1990"));
        lstData.add(new DataPatients("Valentina","10/7/1990"));
        lstData.add(new DataPatients("1JK3W","10/7/1990"));
        lstData.add(new DataPatients("F5HB8","10/7/1990"));
        lstData.add(new DataPatients("ASDF5","10/7/1990"));
        lstData.add(new DataPatients("FJRT","10/7/1990"));
        lstData.add(new DataPatients("ER76","10/7/1990"));
        lstData.add(new DataPatients("TSHY","10/7/1990"));
        lstData.add(new DataPatients("43HHG","10/7/1990"));
        lstData.add(new DataPatients("ASDGQWE","10/7/1990"));
        lstData.add(new DataPatients("RJUyter","10/7/1990"));
        lstData.add(new DataPatients("RTJTR","10/7/1990"));
        lstData.add(new DataPatients("XCVYT","10/7/1990"));


        ListView lv = (ListView)view.findViewById(R.id.listViewPatients);

        adapter = new CustomAdapter(getContext(),R.layout.itemrow,lstData);

        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                SecondPatientFragment txt = (SecondPatientFragment)getFragmentManager().findFragmentById(R.id.secondLayout);
                txt.change(adapter.getListUpdated().get(position).idName);

                //more intents, more values.
                //Call transaction.

            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search,menu);
        MenuItem item = menu.findItem(R.id.menuSearch);
        SearchView searchView = (SearchView)item.getActionView();
        searchView.setLayoutParams(new ActionBar.LayoutParams(Gravity.CENTER));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);

                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }
}
