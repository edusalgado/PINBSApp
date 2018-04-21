package ie.ucc.salgadoe.pinbsapp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class SecondPatientFragment extends Fragment {

    PieChart pieChart;
    private float[] yData = {10,20,70};
    private String[] xData = {"Seizure","Not sure","Health"};
    TextView textView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_second_patient, container, false);

        textView = (TextView)view.findViewById(R.id.textView7);


        pieChart = view.findViewById(R.id.patientPieChart);
        pieChart.setRotationEnabled(true);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setCenterText("Health Status");
        pieChart.setCenterTextColor(Color.parseColor("#02a3dc"));
        pieChart.setTransparentCircleAlpha(50);
        pieChart.setTransparentCircleColor(Color.parseColor("#3e3c41"));
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setCenterTextSize(15);
        //pieChart.setBackgroundColor(Color.TRANSPARENT);

        addDataSet();



    return view;

    }
    public void change(String txt){
        textView.setText(txt);
    }
    private void addDataSet(){
        ArrayList<PieEntry> yEntrys = new ArrayList<>();
        ArrayList<String> xEntrys = new ArrayList<>();

        for(int i = 0; i < yData.length; i++){
            yEntrys.add(new PieEntry(yData[i] , i));
        }
        for(int i = 1; i < xData.length;i++){
            xEntrys.add(xData[i]);
        }
        //Create the data set
        PieDataSet pieDataSet = new PieDataSet(yEntrys,"Detections");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(12);

        //Add colors to dataset

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#ef2d56")); //RED
        colors.add(Color.parseColor("#ffc857"));//YELLOW
        colors.add(Color.parseColor("#417b5a"));//GREEN

        pieDataSet.setColors(colors);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);

        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

}
