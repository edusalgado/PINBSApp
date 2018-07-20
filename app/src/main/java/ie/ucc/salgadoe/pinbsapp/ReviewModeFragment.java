package ie.ucc.salgadoe.pinbsapp;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import Utilities.DataHelper;
import Utilities.MyCustomFillFormatter;


public class ReviewModeFragment extends Fragment {

    Context ctx;
    ArrayList<Double> input = new ArrayList<>();
    ArrayList<Double> probabilities = new ArrayList<>();
    List<Entry> entries = new ArrayList<>();
    List<Integer> colors = new ArrayList<>();
    int shift_value;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_review_mode, container, false);
        LineChart chart =  view.findViewById(R.id.chart_review);

        chart.invalidate();
        chart.clear();

        load(); //Load the files with probabilities
        int len_input = input.size();
        int len_probabilities = probabilities.size();
        shift_value = probabilities.get(0).intValue();

        XAxis xAxis = chart.getXAxis();
        YAxis yAxis = chart.getAxisLeft();
        YAxis rightAxis = chart.getAxisRight();

        rightAxis.setEnabled(false);


        xAxis.setAxisMaximum(len_input);
        xAxis.setTextColor(Color.WHITE);
        yAxis.setTextColor(Color.WHITE);
        xAxis.setAxisMinimum(0);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(11,true);
        chart.setDrawBorders(true);
        chart.setBorderColor(xAxis.getGridColor());

        float x =0;
        float value;
        for(int i = 0; i<len_input; i++){
            value = input.get(i).floatValue();
            entries.add(new Entry(x,value));

            if(0.5 > probabilities.get(i/(32*shift_value))){
                colors.add(Color.RED); // Seizure
            }
            else{
                colors.add(Color.WHITE); //Non-seizure
            }
        }

        LineDataSet dataEeg = new LineDataSet(entries,"EEG");
        dataEeg.setHighLightColor(Color.argb(150,2,163,220));
        dataEeg.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataEeg.setDrawCircles(false);
        dataEeg.setColors(colors);

        final LineData data = new LineData(dataEeg);
        chart.setData(data);
        return view;

    }

public void load(){
    FileInputStream fis = null;
    BufferedReader br = null;
    //String text;
    InputStreamReader InputRead =null;

        try{
            fis = getActivity().openFileInput("test.txt");
            InputRead = new InputStreamReader(fis);
            br = new BufferedReader(InputRead);

            String line;
            StringBuilder builder = new StringBuilder();

            while((line = br.readLine()) != null){
                builder.append(line);
            }

            String[] in_prob = builder.toString().split(System.getProperty("line.separator"));

            int len = in_prob.length;

            int end_input = 0;

            for(int i = 0; i<len-1; i++){
                if(in_prob[i].equals("END INPUT")){
                    end_input = 1;
                }
                if(end_input == 0){
                    input.add(i,Double.parseDouble(in_prob[i]));
                }
                if(end_input == 1){
                    probabilities.add(i,Double.parseDouble(in_prob[i]));
                }
            }
            Toast.makeText(getActivity(),"File opened succesfully!",Toast.LENGTH_LONG).show();

            //Toast.makeText(getActivity(),sb.toString(),Toast.LENGTH_LONG).show();

        }catch (FileNotFoundException e){
            e.printStackTrace();
            Toast.makeText(getActivity(),"No data to plot!",Toast.LENGTH_LONG).show();
        }catch (IOException e){
            e.printStackTrace();
        } finally {
            if(fis != null){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

}




}
