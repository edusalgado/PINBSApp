package ie.ucc.salgadoe.pinbsapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;



public class PatientFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_patient, container, false);

        FirstPatientFragment firstFragment = new FirstPatientFragment();
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction().replace(R.id.firstLayout,firstFragment,firstFragment.getTag()).commit();


        SecondPatientFragment secondFragment = new SecondPatientFragment();
        manager.beginTransaction().replace(R.id.secondLayout,secondFragment,secondFragment.getTag()).commit();
        return view;
    }
}

