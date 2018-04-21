package ie.ucc.salgadoe.pinbsapp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

import Utilities.DataHelper;
import Utilities.MyCustomFillFormatter;


public class ReviewModeFragment extends Fragment {

    Bundle mArgs;

    private DataHelper data;

    float seizure_limits[] = new float[2];
    ArrayList<Double> input  = new ArrayList<>();
    List<Entry> eeg = new ArrayList<>();
    List<Entry> seizures = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_review_mode, container, false);
        LineChart chart = (LineChart) view.findViewById(R.id.chart);
        chart.invalidate();
        chart.clear();
        int start_seizure;
        int finish_seizure;

        //Clear arrays if is the second time user clicks the box
        eeg.clear();
        seizures.clear();

        mArgs = getArguments();
        data = (DataHelper) mArgs.getSerializable("input");
        input = data.getList();
        seizure_limits = mArgs.getFloatArray("limits");

        int len = input.size();
        start_seizure = (int) seizure_limits[0];
        finish_seizure =(int) seizure_limits[1];

        Legend legend = chart.getLegend();


        XAxis xAxis = chart.getXAxis();
        YAxis yAxis = chart.getAxisLeft();
        YAxis rightAxis = chart.getAxisRight();

        rightAxis.setEnabled(true);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawLabels(false);
        //TODO Hide axis labels

        xAxis.setAxisMaximum(len);
        xAxis.setAxisMinimum(0 );
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(11,true);
        yAxis.setAxisMaximum(400);
        yAxis.setAxisMinimum(-400);
        rightAxis.setAxisMaximum(50);
        rightAxis.setAxisMinimum(-50);

        float x = 0f;

        for(int i = 0; i<len-2; i++) {
            eeg.add(new Entry(x,input.get(i).floatValue()));
            //eeg.appendData(new DataPoint(x++, input[i]), false, len);

            if(start_seizure<i && i <finish_seizure){
                seizures.add(new Entry(x,20f));

                //seizure.appendData(new DataPoint(x,50),false,len);
            }
            x++;
            //x =+ 0.03125; //Time x label
        }

        LineDataSet dataEeg = new LineDataSet(eeg,"EEG");
        LineDataSet dataSeizure = new LineDataSet(seizures, "Seizure detected");


        dataEeg.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSeizure.setAxisDependency(YAxis.AxisDependency.RIGHT);

        dataEeg.setDrawCircles(false);
        dataEeg.setColor(Color.BLUE);

        List<LegendEntry> legendEntries = new ArrayList<>(2);
        legendEntries.add(new LegendEntry("EEG",dataEeg.getForm(),dataEeg.getFormSize(),dataEeg.getFormLineWidth(),dataEeg.getFormLineDashEffect(), Color.BLUE));
        legendEntries.add(new LegendEntry("Seizure detected",dataSeizure.getForm(),dataSeizure.getFormSize(),dataSeizure.getFormLineWidth(),dataSeizure.getFormLineDashEffect(), Color.RED));

        legend.setCustom(legendEntries);

        dataSeizure.setDrawCircles(false);
        dataSeizure.setColor(Color.RED,0);
        dataSeizure.setLineWidth(1);
        dataSeizure.setFillAlpha(40);
        dataSeizure.setDrawFilled(true);
        dataSeizure.setFillColor(Color.RED);
        dataSeizure.setFillFormatter(new MyCustomFillFormatter());

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataEeg);
        dataSets.add(dataSeizure);

        LineData data = new LineData(dataSets);
        chart.getDescription().setText("20s of EEG");
        chart.setVisibleXRangeMaximum(640);
        chart.moveViewToX(start_seizure);//refresh
        chart.setData(data);
        chart.invalidate();

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event




}
