package ie.ucc.salgadoe.pinbsapp;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import Utilities.CustomMarkerSonify;
import Utilities.DataHelper;
import Utilities.MyCustomFillFormatter;
import ie.ucc.salgadoe.pinbsapp.audio.VocoderPlayer;


public class FastCheckDialog extends DialogFragment {

    Bundle mArgs;

    private DataHelper data;
    private DataHelper vocoderData;
    private TextView time_selected;
    private Button play,pause;
    private DateFormat datef = new SimpleDateFormat("HH:mm:ss");
    private float xvalue = 0f;
    private int num_seizures_detected;
    private int position; //Center of visualization

    float seizure_limits[];
    ArrayList<Double> input  = new ArrayList<>();
    ArrayList<Double> vocoder = new ArrayList<>();
    List<Entry> eeg = new ArrayList<>();
    List<Entry> seizure1 = new ArrayList<>();
    List<Entry> seizure2 = new ArrayList<>();
    List<Entry> seizure3 = new ArrayList<>();
    List<Entry> seizure4 = new ArrayList<>();
    List<Entry> seizure5 = new ArrayList<>();
    VocoderPlayer vocoderPlayer = new VocoderPlayer();



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        num_seizures_detected = 0;
        position = 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.eeg_visualization_mp, null);
        time_selected =  mainView.findViewById(R.id.textViewhour);
        play = mainView.findViewById(R.id.buttonplay);
        pause =  mainView.findViewById(R.id.buttonpause);
        final Animation animScale = AnimationUtils.loadAnimation(getContext(),R.anim.anim_scale);

        //Clear arrays if is the second time user clicks the box
        eeg.clear();
        seizure1.clear();
        seizure2.clear();
        seizure3.clear();
        seizure4.clear();
        seizure5.clear();



        mArgs = getArguments();
        data = (DataHelper) mArgs.getSerializable("input");
        input = data.getList();
        vocoderData = (DataHelper) mArgs.getSerializable("vocoder");
        vocoder = vocoderData.getList();

        seizure_limits = mArgs.getFloatArray("limits");
        num_seizures_detected = mArgs.getInt("Seizures detected");
        position = mArgs.getInt("Center");

        builder.setView(mainView);
        builder.setTitle("EEG Visualization tool");
        vocoderPlayer.setSamples(vocoder);

        float seizure_start_end_points[][] = new float[num_seizures_detected][2];
        for(int i = 0; i<num_seizures_detected;i++){
            seizure_start_end_points[i][0] = seizure_limits[i*2];
            seizure_start_end_points[i][1] = seizure_limits[(i*2)+1];
        }
        int len = input.size();
        //MP Chart

        final LineChart lineChart1 = (LineChart) mainView.findViewById(R.id.chart);

        lineChart1.invalidate();
        lineChart1.clear();

        Legend legend = lineChart1.getLegend();
        legend.setEnabled(false);


        XAxis xAxis = lineChart1.getXAxis();
        YAxis yAxis = lineChart1.getAxisLeft();
        YAxis rightAxis = lineChart1.getAxisRight();

        rightAxis.setEnabled(true);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawLabels(false);

        //TODO Hide axis labels

        xAxis.setAxisMaximum(len);
        xAxis.setTextColor(Color.WHITE);
        yAxis.setTextColor(Color.WHITE);
        xAxis.setAxisMinimum(0 );
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(11,true);
        rightAxis.setAxisMaximum(1000);
        rightAxis.setAxisMinimum(-1000);
        lineChart1.setDrawBorders(true);
        lineChart1.setBorderColor(xAxis.getGridColor());


        float x = 0f;

        for(int i = 0; i<len; i++) {
            eeg.add(new Entry(x,input.get(i).floatValue()));
            //eeg.appendData(new DataPoint(x++, input[i]), false, len);
            for(int j = 0; j<num_seizures_detected;j++){
                if(j==0) {
                    if (seizure_start_end_points[j][0] < i && i < seizure_start_end_points[j][1]) {
                        //TODO no funciona cuando hay dos seizures, se unen las dos linias. Probar lo de abajo...
                        //Make 5 ifs for 5 "j" at least and multiple lineDataSets, the try to adapt the j for the number of entries (seizures)
                        seizure1.add(new Entry(x, 100f));
                    }
                }
                if(j==1){
                    if (seizure_start_end_points[j][0] < i && i < seizure_start_end_points[j][1]) {
                        //TODO no funciona cuando hay dos seizures, se unen las dos linias. Probar lo de abajo...
                        //Make 5 ifs for 5 "j" at least and multiple lineDataSets, the try to adapt the j for the number of entries (seizures)
                        seizure2.add(new Entry(x, 100f));
                    }
                }
                if(j==2){
                    if (seizure_start_end_points[j][0] < i && i < seizure_start_end_points[j][1]) {
                        //TODO no funciona cuando hay dos seizures, se unen las dos linias. Probar lo de abajo...
                        //Make 5 ifs for 5 "j" at least and multiple lineDataSets, the try to adapt the j for the number of entries (seizures)
                        seizure3.add(new Entry(x, 100f));
                    }
                }
            }

            x++;
            //x =+ 0.03125; //Time x label
        }
        LineDataSet dataEeg = new LineDataSet(eeg,"EEG");
        //Change color lines for marker
        dataEeg.setHighLightColor(Color.argb(150,2,163,220));
        LineDataSet dataSeizure1 = new LineDataSet(seizure1, "Seizure1");
        dataSeizure1.setHighlightEnabled(false);
        LineDataSet dataSeizure2 = new LineDataSet(seizure2, "Seizure2");
        dataSeizure2.setHighlightEnabled(false);
        LineDataSet dataSeizure3 = new LineDataSet(seizure3, "Seizure3");
        dataSeizure3.setHighlightEnabled(false);
        LineDataSet dataSeizure4 = new LineDataSet(seizure4, "Seizure4");
        dataSeizure4.setHighlightEnabled(false);
        LineDataSet dataSeizure5 = new LineDataSet(seizure5, "Seizure5");
        dataSeizure5.setHighlightEnabled(false);


        dataEeg.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSeizure5.setAxisDependency(YAxis.AxisDependency.RIGHT);

        dataEeg.setDrawCircles(false);
        dataEeg.setColor(Color.WHITE);


        dataSeizure1.setDrawCircles(false);
        dataSeizure1.setColor(Color.RED,0);
        dataSeizure1.setLineWidth(0.5f);
        dataSeizure1.setFillAlpha(40);
        dataSeizure1.setDrawFilled(true);
        dataSeizure1.setFillColor(Color.RED);
        dataSeizure1.setFillFormatter(new MyCustomFillFormatter());


        dataSeizure2.setDrawCircles(false);
        dataSeizure2.setColor(Color.RED,0);
        dataSeizure2.setLineWidth(0.5f);
        dataSeizure2.setFillAlpha(40);
        dataSeizure2.setDrawFilled(true);
        dataSeizure2.setFillColor(Color.RED);
        dataSeizure2.setFillFormatter(new MyCustomFillFormatter());


        dataSeizure3.setDrawCircles(false);
        dataSeizure3.setColor(Color.RED,0);
        dataSeizure3.setLineWidth(0.5f);
        dataSeizure3.setFillAlpha(40);
        dataSeizure3.setDrawFilled(true);
        dataSeizure3.setFillColor(Color.RED);
        dataSeizure3.setFillFormatter(new MyCustomFillFormatter());


        dataSeizure4.setDrawCircles(false);
        dataSeizure4.setColor(Color.RED,0);
        dataSeizure4.setLineWidth(0.5f);
        dataSeizure4.setFillAlpha(40);
        dataSeizure4.setDrawFilled(true);
        dataSeizure4.setFillColor(Color.RED);
        dataSeizure4.setFillFormatter(new MyCustomFillFormatter());

        dataSeizure5.setDrawCircles(false);
        dataSeizure5.setColor(Color.RED,0);
        dataSeizure5.setLineWidth(0.5f);
        dataSeizure5.setFillAlpha(40);
        dataSeizure5.setDrawFilled(true);
        dataSeizure5.setFillColor(Color.RED);
        dataSeizure5.setFillFormatter(new MyCustomFillFormatter());

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataEeg);
        dataSets.add(dataSeizure1);
        dataSets.add(dataSeizure2);
        dataSets.add(dataSeizure3);
        dataSets.add(dataSeizure4);
        dataSets.add(dataSeizure5);


        final LineData data = new LineData(dataSets);
        lineChart1.getDescription().setEnabled(false);
        lineChart1.setData(data);
        //Compute range y axis
        float yMin = lineChart1.getYMin();
        float yMax = lineChart1.getYMax();
        if(-yMin > 1000 || yMax > 1000){
            yAxis.setAxisMaximum(1000);
            yAxis.setAxisMinimum(-1000);
        }else {
            if (-yMin > yMax) {
                yAxis.setAxisMaximum(-yMin + 10);
                yAxis.setAxisMinimum(yMin - 10);
            } else {
                yAxis.setAxisMaximum(yMax + 10);
                yAxis.setAxisMinimum(-yMax - 10);
            }
        }

        lineChart1.setVisibleXRangeMaximum(13440); //20s by 21 times maximum visible range. 420 s = 7 min.
        lineChart1.setVisibleXRangeMinimum(640);
        lineChart1.setVisibleYRangeMinimum(120, YAxis.AxisDependency.LEFT);
        lineChart1.zoom(21f,1.5f,seizure_start_end_points[position][0]+320,0, YAxis.AxisDependency.LEFT);
        lineChart1.invalidate();

        //Draw marker

        lineChart1.setDrawMarkers(true);
        CustomMarkerSonify marker = new CustomMarkerSonify(getContext(),R.layout.sonify_marker);
        lineChart1.setMarker(marker);
        vocoderPlayer.configurePlayer(16000);

        lineChart1.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Calendar actual_time = new GregorianCalendar();
                actual_time.set(Calendar.HOUR,0);
                actual_time.set(Calendar.MINUTE,0);
                actual_time.set(Calendar.SECOND,0);
                actual_time.set(Calendar.HOUR_OF_DAY,0);
                int time = (int)((e.getX()*8)/256);
                actual_time.add(Calendar.SECOND,time);
                time_selected.setText(datef.format(actual_time.getTime()));
                xvalue = e.getX();
            }

            @Override
            public void onNothingSelected() {

            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(animScale);
                if(xvalue != 0){
                    //Todo nose si rula o no, si rula!
                    //Creo que moviendo el highlight dentro de un for puede funcionar, sobretodo cuando la pantalla esta zoom out al maximo donde no hay rango para hacer un moveview to con animacion.
                    lineChart1.moveViewToAnimated(xvalue+256,0f, YAxis.AxisDependency.LEFT,8000);
                }

                vocoderPlayer.runTest();
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(animScale);
            }
        });

        builder.setPositiveButton("OK",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){

                //Don't do anything here if you just want it dismissed when clicked
            }
        });

        //Create AlertDialog object and return it
        return builder.create();

        //Call it in your activity as:
        //DialogFragment dialog = new MyEEGFragment();
        //dialog.show(getSupportFragmentManager(),"MyDialogFragmentTag");


    }

}




