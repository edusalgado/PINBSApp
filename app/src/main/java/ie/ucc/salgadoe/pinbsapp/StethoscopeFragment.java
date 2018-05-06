package ie.ucc.salgadoe.pinbsapp;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import Utilities.Buffer;
import Utilities.CNN;
import Utilities.CustomMarkerCNN;
import Utilities.DataHelper;
import Utilities.ProgressBarAnimation;
import ie.ucc.salgadoe.pinbsapp.audio.PhaseVocoderRunnable;
import ie.ucc.salgadoe.pinbsapp.audio.VocoderPlayer;

/**
 * A placeholder fragment containing a simple view.
 */
public class StethoscopeFragment extends Fragment {

    int shift_value = 0;
    int shift_set = 0;
    int baby_selected;
    int baby_set;
    int stop = 1;
    int speed = 0;
    int inputlen;
    int progressStatus = 0;
    int chunkLength;
    Spinner options, baby, sp_speed;
    ListView list;
    Button b_next, b_stop,b_reset,b_soni;
    TextView tv_output;
    TextView tv_output2, message_box, time;
    SeekBar sb;
    RadioGroup radioGroup;
    ProgressBar progressBar, verticalBar;
    Buffer buffer;
    CNN resp = new CNN();

    int counter = 0;
    int counter_sonification = 0;
    int position_flag;

    Bundle args = new Bundle();

    FastCheckDialog dialog = new FastCheckDialog();
    ArrayList<Double> resp_adaptation = new ArrayList<>();
    ArrayList<Double> input_buffer = new ArrayList<>();

    //Respiration Adaptation variables
    ArrayList<Integer> idxtmp = new ArrayList<>();
    int kk = 0;
    int i_aux =0;

    //Threshold bar

    double th_value=0.5;

    int layers_6_11 =-1;

    //Chart

    private LineChart mChart, mChart2;
    LineData dataEeg = new LineData();
    LineData dataProb = new LineData();


    private double[][] fweights;
    private double[][] fweights2;
    private double[][] fweights3;
    private double[][] fweights4;
    private double[][] fweights5;
    private double[][] fweights6;
    private double[][] fweights7;
    private double[][] fweights8;
    private double[][] fweights9;
    private double[][] fweights10;
    private double[][] fweights11;
    private double[][] fweightsbn;
    private double[][] fweightsbn2;
    private double[][] fweightsbn3;
    private double[][] finput;
    private double[][] dbias;
    private double[] fbias6;
    private double[] fbias_short;
    private double[] flabel;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;

    int count=0;
    String minute = "";
    String aux_time="";

    int buffer_aux = 0;


    private int position = 0; // Seizure at 18688
    private int annotation = 24;  // Seizure at 584 //24 to start after buffer
    private double x1 = 0.0;
    private int list_position = 0;
    private int[][] array4seizures = new int[20][2];
    private int num_seizures_detected = 0;

    private Runnable mTimer ;
    private Runnable BufferRun;
    private Runnable feedEeg;

    private Handler handler_progress = new Handler();

    /*Sonification cosis */

    private PhaseVocoderRunnable vocoder = new PhaseVocoderRunnable();
    private VocoderPlayer vocoderPlayer = new VocoderPlayer();


    public void readFiles11() {

        StringBuilder[] sb = new StringBuilder[16];

        try {
            int size;
            byte buffer[];
            InputStream is;
            if(baby_selected ==1) {
                is = getActivity().getAssets().open("input_long_15_02_2018.txt");
                size = is.available();
                buffer = new byte[size];
                is.read(buffer);
                is.close();
                sb[0] = new StringBuilder(new String(buffer));

                is = getActivity().getAssets().open("annotations_long_12_02_2018.txt");
                size = is.available();
                buffer = new byte[size];
                is.read(buffer);
                is.close();
                sb[15] = new StringBuilder(new String(buffer));
            }
            if(baby_selected == 0){
                is = getActivity().getAssets().open("input_long_19_02_2018.txt");
                size = is.available();
                buffer = new byte[size];
                is.read(buffer);
                is.close();
                sb[0] = new StringBuilder(new String(buffer));

                is = getActivity().getAssets().open("annotations_long_19_02_2018.txt");
                size = is.available();
                buffer = new byte[size];
                is.read(buffer);
                is.close();
                sb[15] = new StringBuilder(new String(buffer));

            }
            if(baby_selected == 2){
                is = getActivity().getAssets().open("one_minute_input.txt");
                size = is.available();
                buffer = new byte[size];
                is.read(buffer);
                is.close();
                sb[0] = new StringBuilder(new String(buffer));

                is = getActivity().getAssets().open("one_minute_annotations.txt");
                size = is.available();
                buffer = new byte[size];
                is.read(buffer);
                is.close();
                sb[15] = new StringBuilder(new String(buffer));

            }



            is = getActivity().getAssets().open("weightsconv1d_12_nospace.txt");
            size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            sb[1] = new StringBuilder(new String(buffer));

            is = getActivity().getAssets().open("weightsconv1d_13_nospace.txt");
            size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            sb[2] = new StringBuilder(new String(buffer));

            is = getActivity().getAssets().open("weightsconv1d_14_nospace.txt");
            size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            sb[3] = new StringBuilder(new String(buffer));

            is = getActivity().getAssets().open("weightsconv1d_15_nospace.txt");
            size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            sb[4] = new StringBuilder(new String(buffer));

            is = getActivity().getAssets().open("weightsconv1d_16_nospace.txt");
            size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            sb[5] = new StringBuilder(new String(buffer));

            is = getActivity().getAssets().open("weightsconv1d_17_nospace.txt");
            size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            sb[6] = new StringBuilder(new String(buffer));

            is = getActivity().getAssets().open("weightsconv1d_18_nospace.txt");
            size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            sb[7] = new StringBuilder(new String(buffer));
            is = getActivity().getAssets().open("weightsconv1d_19_nospace.txt");
            size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            sb[8] = new StringBuilder(new String(buffer));

            is = getActivity().getAssets().open("weightsconv1d_20_nospace.txt");
            size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            sb[9] = new StringBuilder(new String(buffer));

            is = getActivity().getAssets().open("weightsconv1d_21_nospace.txt");
            size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            sb[10] = new StringBuilder(new String(buffer));

            is = getActivity().getAssets().open("weightsconv1d_22_nospace.txt");
            size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            sb[11] = new StringBuilder(new String(buffer));

            is = getActivity().getAssets().open("weightsbatch_normalization_4_nospace.txt");
            size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            sb[12] = new StringBuilder(new String(buffer));

            is = getActivity().getAssets().open("weightsbatch_normalization_5_nospace.txt");
            size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            sb[13] = new StringBuilder(new String(buffer));

            is = getActivity().getAssets().open("weightsbatch_normalization_6_nospace.txt");
            size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            sb[14] = new StringBuilder(new String(buffer));


        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Split String into list of strings

        String[] linesin = sb[0].toString().split(System.getProperty("line.separator"));
        String[] linesw1 = sb[1].toString().split(System.getProperty("line.separator"));
        String[] linesw2 = sb[2].toString().split(System.getProperty("line.separator"));
        String[] linesw3 = sb[3].toString().split(System.getProperty("line.separator"));
        String[] linesw4 = sb[4].toString().split(System.getProperty("line.separator"));
        String[] linesw5 = sb[5].toString().split(System.getProperty("line.separator"));
        String[] linesw6 = sb[6].toString().split(System.getProperty("line.separator"));
        String[] linesw7 = sb[7].toString().split(System.getProperty("line.separator"));
        String[] linesw8 = sb[8].toString().split(System.getProperty("line.separator"));
        String[] linesw9 = sb[9].toString().split(System.getProperty("line.separator"));
        String[] linesw10 = sb[10].toString().split(System.getProperty("line.separator"));
        String[] linesw11= sb[11].toString().split(System.getProperty("line.separator"));
        String[] linesbn = sb[12].toString().split(System.getProperty("line.separator"));
        String[] linesbn2 = sb[13].toString().split(System.getProperty("line.separator"));
        String[] linesbn3 = sb[14].toString().split(System.getProperty("line.separator"));
        String[] linesin_an = sb[15].toString().split(System.getProperty("line.separator"));

        inputlen = linesin.length;
        int anlen = linesin_an.length;
        int weightslen = linesw2.length;
        int w32 = weightslen - 32;

        String[][] m_in = new String[inputlen][1];
        String[][] m_in_an = new String[anlen][1];
        String[][] w_in = new String[32][3];
        String[][] w2_in = new String[1024][3];
        String[][] w3_in = new String[1024][3];
        String[][] w4_in = new String[1024][3];
        String[][] w5_in = new String[1024][3];
        String[][] w6_in = new String[1024][4];
        String[][] w7_in = new String[1024][3];
        String[][] w8_in = new String[1024][3];
        String[][] w9_in = new String[1024][3];
        String[][] w10_in = new String[1024][3];
        String[][] w11_in = new String[64][3];
        String[][] bn_in = new String[4][32];
        String[][] bn2_in = new String[4][32];
        String[][] bn3_in = new String[4][32];
        String[] bias_short = new String[2];



        fweights = new double[linesw1.length - 32][3];
        fweights2 = new double[w32][3];
        fweights3 = new double[w32][3];
        fweights4 = new double[w32][3];
        fweights5 = new double[w32][3];
        fweights6 = new double[w32][3];
        fweights7 = new double[w32][3];
        fweights8 = new double[w32][3];
        fweights9 = new double[w32][3];
        fweights10 = new double[w32][3];
        fweights11 = new double[linesw11.length-2][3];
        fweightsbn = new double[linesbn.length][32];
        fweightsbn2 = new double[linesbn2.length][32];
        fweightsbn3 = new double[linesbn3.length][32];

        flabel = new double[anlen];
        finput = new double[inputlen][1];
        dbias = new double[10][32];
        fbias_short = new double[2];

        String[] row;
        for (int i = 0; i < inputlen; i++) {
            row = linesin[i].split(" ");
            m_in[i] = row;
            finput[i][0] = Double.parseDouble(m_in[i][0]);
        }

        for(int i = 0; i < anlen; i++){
            row = linesin_an[i].split(" ");
            m_in_an[i] = row;
        }

        for (int i = 32, len = linesw1.length; i < len; i++) {
            linesw1[i] = linesw1[i].replace("\r", "");
            row = linesw1[i].split(" ");
            w_in[i - 32] = row;
        }
        for (int i = 32; i < weightslen; i++) {
            linesw2[i] = linesw2[i].replace("\r", "");
            row = linesw2[i].split(" ");
            w2_in[i - 32] = row;
            linesw3[i] = linesw3[i].replace("\r", "");
            row = linesw3[i].split(" ");
            w3_in[i - 32] = row;
            linesw4[i] = linesw4[i].replace("\r", "");
            row = linesw4[i].split(" ");
            w4_in[i - 32] = row;
            linesw5[i] = linesw5[i].replace("\r", "");
            row = linesw5[i].split(" ");
            w5_in[i - 32] = row;
            linesw6[i] = linesw6[i].replace("\r", "");
            row = linesw6[i].split(" ");
            w6_in[i - 32] = row;
            linesw7[i] = linesw7[i].replace("\r", "");
            row = linesw7[i].split(" ");
            w7_in[i - 32] = row;
            linesw8[i] = linesw8[i].replace("\r", "");
            row = linesw8[i].split(" ");
            w8_in[i - 32] = row;
            linesw9[i] = linesw9[i].replace("\r", "");
            row = linesw9[i].split(" ");
            w9_in[i - 32] = row;
            linesw10[i] = linesw10[i].replace("\r", "");
            row = linesw10[i].split(" ");
            w10_in[i - 32] = row;
        }
        for (int i = 2, len = linesw11.length; i < len; i++) {
            linesw11[i] = linesw11[i].replace("\r", "");
            row = linesw11[i].split(" ");
            w11_in[i - 2] = row;
        }
        for (int i = 0, len = linesbn.length; i < len; i++) {
            linesbn[i] = linesbn[i].replace("\r", "");
            row = linesbn[i].split(" ");
            bn_in[i] = row;
            linesbn2[i] = linesbn2[i].replace("\r", "");
            row = linesbn2[i].split(" ");
            bn2_in[i] = row;
            linesbn3[i] = linesbn3[i].replace("\r", "");
            row = linesbn3[i].split(" ");
            bn3_in[i] = row;
        }
        for (int i = 0; i < 32; i++) {
            linesw1[i] = linesw1[i].replace("\r", "");
            dbias[0][i] = Double.parseDouble(linesw1[i]);
            linesw2[i] = linesw2[i].replace("\r", "");
            dbias[1][i] = Double.parseDouble(linesw2[i]);
            linesw3[i] = linesw3[i].replace("\r", "");
            dbias[2][i] = Double.parseDouble(linesw3[i]);
            linesw4[i] = linesw4[i].replace("\r", "");
            dbias[3][i] = Double.parseDouble(linesw4[i]);
            linesw5[i] = linesw5[i].replace("\r", "");
            dbias[4][i] = Double.parseDouble(linesw5[i]);
            linesw6[i] = linesw6[i].replace("\r", "");
            dbias[5][i] = Double.parseDouble(linesw6[i]);
            linesw7[i] = linesw7[i].replace("\r", "");
            dbias[6][i] = Double.parseDouble(linesw7[i]);
            linesw8[i] = linesw8[i].replace("\r", "");
            dbias[7][i] = Double.parseDouble(linesw8[i]);
            linesw9[i] = linesw9[i].replace("\r", "");
            dbias[8][i] = Double.parseDouble(linesw9[i]);
            linesw10[i] = linesw10[i].replace("\r", "");
            dbias[9][i] = Double.parseDouble(linesw10[i]);
        }
        for (int i = 0; i < 2; i++) {
            linesw11[i] = linesw11[i].replace("\r", "");
            bias_short[i] = linesw11[i];
        }

        for (int i = 0, len = linesw1.length - 32; i < len; i++) {
            for (int j = 0; j < 3; j++) {
                fweights[i][j] = Double.parseDouble(w_in[i][j]);
            }
        }
        for (int i = 0; i < w32; i++) {
            for (int j = 0; j < 3; j++) {
                fweights2[i][j] = Double.parseDouble(w2_in[i][j]);
                fweights3[i][j] = Double.parseDouble(w3_in[i][j]);
                fweights4[i][j] = Double.parseDouble(w4_in[i][j]);
                fweights5[i][j] = Double.parseDouble(w5_in[i][j]);
                fweights6[i][j] = Double.parseDouble(w6_in[i][j]);
                fweights7[i][j] = Double.parseDouble(w7_in[i][j]);
                fweights8[i][j] = Double.parseDouble(w8_in[i][j]);
                fweights9[i][j] = Double.parseDouble(w9_in[i][j]);
                fweights10[i][j] = Double.parseDouble(w10_in[i][j]);
            }
        }
        for (int i = 0, len = linesw11.length - 2; i < len; i++) {
            for (int j = 0; j < 3; j++) {
                fweights11[i][j] = Double.parseDouble(w11_in[i][j]);
            }
        }
        for (int i = 0; i < 2; i++) {
            fbias_short[i] = Double.parseDouble(bias_short[i]);
        }
        for (int i = 0, len = linesbn.length; i < len; i++) {
            for (int j = 0; j < 32; j++) {
                fweightsbn[i][j] = Double.parseDouble(bn_in[i][j]);
                fweightsbn2[i][j] = Double.parseDouble(bn2_in[i][j]);
                fweightsbn3[i][j] = Double.parseDouble(bn3_in[i][j]);

            }
        }
        for(int i = 0; i<anlen;i++){
            flabel[i] = Double.parseDouble(m_in_an[i][0]);
        }
        BufferRun.run();

    }
    public void readFiles6(){
        StringBuilder[] sb = new StringBuilder[9];

        try {
            int size;
            byte buffer[];
            InputStream is;

            if(baby_selected==1) {
                is = getActivity().getAssets().open("input_long_15_02_2018.txt");
                size = is.available();
                buffer = new byte[size];
                is.read(buffer);
                is.close();
                sb[0] = new StringBuilder(new String(buffer));

                is = getActivity().getAssets().open("annotations_long_12_02_2018.txt");
                size = is.available();
                buffer = new byte[size];
                is.read(buffer);
                is.close();
                sb[1] = new StringBuilder(new String(buffer));
            }
            if(baby_selected==0) {
                is = getActivity().getAssets().open("input_long_19_02_2018.txt");
                size = is.available();
                buffer = new byte[size];
                is.read(buffer);
                is.close();
                sb[0] = new StringBuilder(new String(buffer));

                is = getActivity().getAssets().open("annotations_long_19_02_2018.txt");
                size = is.available();
                buffer = new byte[size];
                is.read(buffer);
                is.close();
                sb[1] = new StringBuilder(new String(buffer));

            }
            if(baby_selected==2) {
                is = getActivity().getAssets().open("one_minute_input.txt");
                size = is.available();
                buffer = new byte[size];
                is.read(buffer);
                is.close();
                sb[0] = new StringBuilder(new String(buffer));

                is = getActivity().getAssets().open("one_minute_annotations.txt");
                size = is.available();
                buffer = new byte[size];
                is.read(buffer);
                is.close();
                sb[1] = new StringBuilder(new String(buffer));

            }
            is = getActivity().getAssets().open("weightsconv1d_1.txt");
            size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            sb[2] = new StringBuilder(new String(buffer));

            is = getActivity().getAssets().open("weightsconv1d_2.txt");
            size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            sb[3] = new StringBuilder(new String(buffer));

            is = getActivity().getAssets().open("weightsconv1d_3.txt");
            size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            sb[4] = new StringBuilder(new String(buffer));

            is = getActivity().getAssets().open("weightsconv1d_4.txt");
            size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            sb[5] = new StringBuilder(new String(buffer));

            is = getActivity().getAssets().open("weightsconv1d_5.txt");
            size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            sb[6] = new StringBuilder(new String(buffer));

            is = getActivity().getAssets().open("weightsconv1d_6.txt");
            size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            sb[7] = new StringBuilder(new String(buffer));

            is = getActivity().getAssets().open("weightsbatch_normalization_2.txt");
            size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
            sb[8] = new StringBuilder(new String(buffer));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Split String into list of strings

        String[] linesin = sb[0].toString().split(System.getProperty("line.separator"));
        String[] linesin_an = sb[1].toString().split(System.getProperty("line.separator"));
        String[] linesw1 = sb[2].toString().split(System.getProperty("line.separator"));
        String[] linesw2 = sb[3].toString().split(System.getProperty("line.separator"));
        String[] linesw3 = sb[4].toString().split(System.getProperty("line.separator"));
        String[] linesw4 = sb[5].toString().split(System.getProperty("line.separator"));
        String[] linesw5 = sb[6].toString().split(System.getProperty("line.separator"));
        String[] linesw6 = sb[7].toString().split(System.getProperty("line.separator"));
        String[] linesbn = sb[8].toString().split(System.getProperty("line.separator"));

        inputlen = linesin.length;
        int anlen = linesin_an.length;
        int weightslen = linesw2.length;
        int w32 = weightslen - 32;

        String[][] m_in = new String[inputlen][1];
        String[][] m_in_an = new String[anlen][1];
        String[][] w_in = new String[32][4];
        String[][] w2_in = new String[1024][4];
        String[][] w3_in = new String[1024][4];
        String[][] w4_in = new String[1024][4];
        String[][] w5_in = new String[1024][4];
        String[][] w6_in = new String[64][4];
        String[][] bn_in = new String[4][32];
        String[] bias6 = new String[2];



        fweights = new double[linesw1.length - 32][4];
        fweights2 = new double[w32][4];
        fweights3 = new double[w32][4];
        fweights4 = new double[w32][4];
        fweights5 = new double[w32][4];
        fweights6 = new double[linesw6.length - 2][4];
        fweightsbn = new double[linesbn.length][32];

        finput = new double[inputlen][1];
        flabel = new double[anlen];//// 1 input
        dbias = new double[5][32];
        fbias6 = new double[2];


        String[] row;

        for (int i = 0; i < inputlen; i++) {
            row = linesin[i].split(" ");
            m_in[i] = row;
            finput[i][0] = Double.parseDouble(m_in[i][0]);
        }
        for (int i = 0; i < anlen; i++) {
            row = linesin_an[i].split(" ");
            m_in_an[i] = row;
        }
        for (int i = 32, len = linesw1.length; i < len; i++) {
            linesw1[i] = linesw1[i].replace("\r", "");
            row = linesw1[i].split(" ");
            w_in[i - 32] = row;
        }
        for (int i = 32; i < weightslen; i++) {
            linesw2[i] = linesw2[i].replace("\r", "");
            row = linesw2[i].split(" ");
            w2_in[i - 32] = row;
            linesw3[i] = linesw3[i].replace("\r", "");
            row = linesw3[i].split(" ");
            w3_in[i - 32] = row;
            linesw4[i] = linesw4[i].replace("\r", "");
            row = linesw4[i].split(" ");
            w4_in[i - 32] = row;
            linesw5[i] = linesw5[i].replace("\r", "");
            row = linesw5[i].split(" ");
            w5_in[i - 32] = row;
        }
        for (int i = 2, len = linesw6.length; i < len; i++) {
            linesw6[i] = linesw6[i].replace("\r", "");
            row = linesw6[i].split(" ");
            w6_in[i - 2] = row;
        }
        for (int i = 0, len = linesbn.length; i < len; i++) {
            linesbn[i] = linesbn[i].replace("\r", "");
            row = linesbn[i].split(" ");
            bn_in[i] = row;
        }
        for (int i = 0; i < 32; i++) {
            linesw1[i] = linesw1[i].replace("\r", "");
            dbias[0][i] = Double.parseDouble(linesw1[i]);
            linesw2[i] = linesw2[i].replace("\r", "");
            dbias[1][i] = Double.parseDouble(linesw2[i]);
            linesw3[i] = linesw3[i].replace("\r", "");
            dbias[2][i] = Double.parseDouble(linesw3[i]);
            linesw4[i] = linesw4[i].replace("\r", "");
            dbias[3][i] = Double.parseDouble(linesw4[i]);
            linesw5[i] = linesw5[i].replace("\r", "");
            dbias[4][i] = Double.parseDouble(linesw5[i]);
        }
        for (int i = 0; i < 2; i++) {
            linesw6[i] = linesw6[i].replace("\r", "");
            bias6[i] = linesw6[i];
        }
        for (int i = 0; i < anlen; i++) {
            flabel[i] = Double.parseDouble(m_in_an[i][0]);
        }
        for (int i = 0, len = linesw1.length - 32; i < len; i++) {
            for (int j = 0; j < 4; j++) {
                fweights[i][j] = Double.parseDouble(w_in[i][j]);
            }
        }
        for (int i = 0; i < w32; i++) {
            for (int j = 0; j < 4; j++) {
                fweights2[i][j] = Double.parseDouble(w2_in[i][j]);
                fweights3[i][j] = Double.parseDouble(w3_in[i][j]);
                fweights4[i][j] = Double.parseDouble(w4_in[i][j]);
                fweights5[i][j] = Double.parseDouble(w5_in[i][j]);
            }
        }
        for (int i = 0, len = linesw6.length - 2; i < len; i++) {
            for (int j = 0; j < 4; j++) {
                fweights6[i][j] = Double.parseDouble(w6_in[i][j]);
            }
        }
        for (int i = 0; i < 2; i++) {
            fbias6[i] = Double.parseDouble(bias6[i]);
        }
        for (int i = 0, len = linesbn.length; i < len; i++) {
            for (int j = 0; j < 32; j++) {
                fweightsbn[i][j] = Double.parseDouble(bn_in[i][j]);
            }
        }
        BufferRun.run();


    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.stetoscope_fragment, container, false);

        //CHART 1
        mChart = (LineChart) view.findViewById(R.id.chart);
        mChart.getDescription().setEnabled(false);

        mChart.setData(dataEeg);
        Legend legendEeg = mChart.getLegend();
        legendEeg.setEnabled(false);
        XAxis xchart = mChart.getXAxis();
        xchart.setLabelCount(11,true);
        xchart.setAvoidFirstLastClipping(true);
        xchart.setAxisMinimum(0);
        xchart.setAxisMaximum(640);
        xchart.setEnabled(true);
        xchart.setTextColor(Color.WHITE);
        xchart.setPosition(XAxis.XAxisPosition.BOTTOM);

        //Ralentiza de locos. Pero queda de putis :)
        //mChart.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
        //mChart.getRenderer().getPaintRender().setShadowLayer(3,5,3,Color.GRAY);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setLabelCount(7,true);
        leftAxis.setAxisMaximum(500);
        leftAxis.setAxisMinimum(-500);
        mChart.setVisibleYRangeMinimum(100, YAxis.AxisDependency.LEFT);
        mChart.zoom(1f,3f,0,0, YAxis.AxisDependency.LEFT);
        mChart.setDrawBorders(true);
        mChart.setBorderColor(xchart.getGridColor());
        mChart.setHighlightPerTapEnabled(false);

        mChart.setData(dataEeg);
        mChart.invalidate();


        //Chart 2
        mChart2 = (LineChart) view.findViewById(R.id.chart2);
        mChart2.getDescription().setEnabled(false);
        //Todo controlar el zoom de la grafica(mas range). Fijarse en shift=1

        mChart2.setDrawMarkers(true);

        Legend legenProb = mChart2.getLegend();
        legenProb.setEnabled(false);



        YAxis rightChart2 = mChart2.getAxisRight();
        rightChart2.setEnabled(false);
        YAxis leftChart2 = mChart2.getAxisLeft();
        leftChart2.setTextColor(Color.WHITE);
        leftChart2.setAxisMaximum(1);
        leftChart2.setAxisMinimum(0);
        leftChart2.setLabelCount(6,true);

        XAxis xChart2 =mChart2.getXAxis();
        xChart2.setEnabled(true);
        xChart2.setAxisMaximum(60);
        xChart2.setAxisMinimum(0);
        xChart2.setTextColor(Color.WHITE);
        xChart2.setPosition(XAxis.XAxisPosition.BOTTOM);
        xChart2.setAvoidFirstLastClipping(true);

        final LimitLine threshold = new LimitLine(0.5f);
        threshold.enableDashedLine(4f,4f,0f);
        threshold.setLineColor(Color.parseColor("#02a3dc"));
        threshold.setLineWidth(2f);
        mChart2.getAxis(YAxis.AxisDependency.LEFT).addLimitLine(threshold);
        mChart2.setData(dataProb);
        mChart2.invalidate();


        final Calendar timer = new GregorianCalendar();
        final DateFormat datef = new SimpleDateFormat("HH:mm:ss");

        //SEEKBAR CODE

        sb = (SeekBar) view.findViewById(R.id.mySeekBar);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue =progress;
                th_value = ((double)progressChangedValue)/20;
                mChart2.getAxis(YAxis.AxisDependency.LEFT).removeAllLimitLines();
                LimitLine new_th = new LimitLine((float)th_value);
                new_th.setLineColor(Color.parseColor("#02a3dc"));
                new_th.enableDashedLine(4f,4f,0f);
                new_th.setLineWidth(2f);
                mChart2.getAxis(YAxis.AxisDependency.LEFT).addLimitLine(new_th);
                mChart2.invalidate();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getActivity(),"New threshold value: " + th_value, Toast.LENGTH_SHORT).show();
            }
        });

        //SPINNER CODE
        options = (Spinner) view.findViewById(R.id.sp01);
        final String[] values = new String[]{
                "none"," 8_1"," 8_4"," 8_8"
        };

        ArrayAdapter<String> adapter2 = new ArrayAdapter(getActivity(),R.layout.spinner_item, values){
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0) {
                    // Set the item text color
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };


        adapter2.setDropDownViewResource(R.layout.spinner_item);
        options.setAdapter(adapter2);
        options.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    shift_value =0;
                    shift_set = 0;
                }
                else if(position==1){
                    shift_value=1;
                    shift_set = 1;
                }
                else if(position ==2){
                    shift_value=4;
                    shift_set = 1;
                }
                else if(position ==3){
                    shift_value=8;
                    shift_set = 1;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //SPINNER BABY
         baby = (Spinner) view.findViewById(R.id.sp02);
         final String[] babies = new String[]{
                 "none"," 01"," 02"," 03"
         };
        ArrayAdapter<String> adapter3 = new ArrayAdapter(getActivity(),R.layout.spinner_item, babies){
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0) {
                    // Set the item text color
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        adapter3.setDropDownViewResource(R.layout.spinner_item);
        baby.setAdapter(adapter3);
        baby.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    baby_set = 0;
                    baby_selected=-1;
                }
                else if(position==1){
                    baby_set=1;
                    baby_selected=0;
                }
                else if(position ==2) {
                    baby_set=1;
                    baby_selected=1;
                }
                else if(position==3){
                    baby_set=1;
                    baby_selected=2;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //SPEED

        sp_speed = (Spinner) view.findViewById(R.id.sp03);
        final String[] speeds = new String[]{
                "Test mode","Real time"
        };
        ArrayAdapter<String> adapter4 = new ArrayAdapter(getActivity(),R.layout.spinner_item, speeds)
        {@Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            View view = super.getDropDownView(position, convertView, parent);
            TextView tv = (TextView) view;
                tv.setTextColor(Color.BLACK);


            return view;
        }
    };

        adapter4.setDropDownViewResource(R.layout.spinner_item);
        sp_speed.setAdapter(adapter4);
        sp_speed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    speed = 0;
                }
                else if(position==1){
                    speed = 1;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        //List

        View empty = view.findViewById(R.id.empty);
        list = (ListView) view.findViewById(R.id.seizurelist);
        list.bringToFront();
        arrayList = new ArrayList<>();
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, arrayList);
        list.setAdapter(adapter);
        list.setEmptyView(empty);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Setting the vocoder vector to the player
                //TODO add progress bar "loading" to avoid double click while processing
                float seizure_limits [] = new float[2*num_seizures_detected];
                for(int i = 0; i<num_seizures_detected;i++) {
                    seizure_limits[i*2] = array4seizures[i][0];
                    seizure_limits[(i*2)+1] = array4seizures[i][1];
                }
                //TODO use parcelable instead of serializable, better performance in android
                args.putSerializable("input",new DataHelper(input_buffer));
                args.putFloatArray("limits",seizure_limits);
                args.putInt("Seizures detected",num_seizures_detected);
                args.putInt("Center", position);
                args.putSerializable("vocoder", new DataHelper(vocoder.getVocoderSamples()));

                /*ReviewModeFragment ReviewMode = new ReviewModeFragment();
                ReviewMode.setArguments(args);
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frameLayout, ReviewMode);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();*/
                dialog.setArguments(args);
                dialog.show(getFragmentManager(),"MyDialogFragmentTag");
            }
        });




        b_next = (Button) view.findViewById(R.id.b_next);
        b_stop = (Button) view.findViewById(R.id.b_stop);
        b_reset = (Button) view.findViewById(R.id.b_reset);
        b_soni = view.findViewById(R.id.btn_soni);

        tv_output2 = (TextView) view.findViewById(R.id.tv_output2);
        time= (TextView) view.findViewById(R.id.time);
        tv_output = (TextView) view.findViewById(R.id.tv_output);
        message_box = (TextView) view.findViewById(R.id.message_box);
        final ImageView traffic = (ImageView) view.findViewById(R.id.imageView_traffic_light);
        final Handler handler_buffer = new Handler();
        final Handler handler_time = new Handler();

        vocoderPlayer.configurePlayer(16000);


        //RADIO GROUP

        radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
        //TODO: add option to avoid layer changing in the middle of the acquisition
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.radio6:
                        layers_6_11 = 0;
                        Toast.makeText(getActivity(),"6 layers selected", Toast.LENGTH_LONG).show();

                        message_box.setText("6 layers Network ready!");
                        break;
                    case R.id.radio11:
                        layers_6_11 = 1;
                        Toast.makeText(getActivity(),"11 layers selected", Toast.LENGTH_LONG).show();

                        message_box.setText("11 layers Network ready!");
                        break;
                }
            }
        });

        //PROGRESSBAR

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        //Vertical Bar
        verticalBar = (ProgressBar) view.findViewById(R.id.vertical_progressbar);
        verticalBar.setProgress(0);

        message_box.setText("CHOOSE A NETWORK TO START!");


        b_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                if (stop == 0) {
                    Toast.makeText(getActivity(), "Acquisition is running. To start a new acquisition press STOP button!", Toast.LENGTH_LONG).show();
                } else {

                    final double[] PVInput = new double[chunkLength];


                    //THREAD EEG TIME DOMAIN
                    //TODO add noise to generate fake audio signal ( - player????? mp3 WAV??) sampling rate
                    feedEeg = new Runnable() {
                        @Override
                        public void run() {
                            //double[][] input = new double[256][1];
                            //for (int i = position; i < (256 + position); i++) {
                                //input[tiempo_aux - position][0] = finput[tiempo_aux][0];
                                //input_buffer.add(finput[tiempo_aux][0]);

                            //TODO this thread can work as the main if we fill a buffer to send to scan thread with a counter and if sentence taking into account shift value!!
                                addEntryEeg((float)finput[position][0]);
                                input_buffer.add(position,finput[position][0]);
                                PVInput[counter_sonification] = finput[position][0];

                                if(chunkLength == counter_sonification + 1){
                                    counter_sonification = 0;
                                    /*vocoder.readNextChunk(PVInput);
                                    vocoder.run();
                                    */
                                    new executeVocoder().execute(PVInput);
                                }

                                //Todo entender porq no funciona si cambio de shift, el problema esta aqui creo que tiene que ver con la arquitectura de condiciones. (Si se cambia al shift 1 cuando counter es mayor a 32 por ejemplo)
                                if(shift_value == 1 && counter == 32){
                                    position_flag = position - 256;
                                    mTimer.run();
                                    counter = 0;
                                }
                                if(shift_value == 4 && counter == 128){
                                    position_flag = position -256;
                                    mTimer.run();
                                    counter = 0;
                                }
                                if(shift_value == 8 && counter == 256){
                                    position_flag = position - 256;
                                    mTimer.run();
                                    counter =0;
                                } else counter ++;
                                counter_sonification++;
                                position ++;
                            //}
                            //input4network = input;
                            handler_time.postDelayed(feedEeg,1);
                            if(position > inputlen-256){
                                handler_time.removeCallbacks(feedEeg);
                            }

                        }
                    };

                    //THREAD BUFFER

                    //Implement a buffer to get the first 48s for the moving average filter and smooth the output
                    //48s equal to the first 1536 samples

                    BufferRun = new Runnable() {
                        @Override
                        public void run() {


                            timer.set(Calendar.HOUR, 0);
                            timer.set(Calendar.MINUTE, 0);
                            timer.set(Calendar.SECOND, 0);
                            timer.set(Calendar.HOUR_OF_DAY, 0);


                            buffer = new Buffer(48 + 1); //depends on the shift value. 48s MAF Is not depending on the shift value anymore, always 49!

                            double[][] input = new double[256][1]; //256 samples each epoch
                            int len_buffer = 48 ; //We don't want the last value of the buffer. Will be computed in next! Always like shift by 1

                            for (int b = 0; b < len_buffer/shift_value; b++) {
                                for (int i = position; i < position + 256; i++) {
                                    input[i - position][0] = finput[i][0];
                                }
                                //TODO put buffer inside feedEEG
                                double[][] output1;
                                double[] output;

                                //11LAYERS
                                if (layers_6_11 == 1) {
                                    output1 = CNN.Conv1D(input, fweights, true, true, dbias[0]);
                                    output1 = CNN.Conv1D(output1, fweights2, true, true, dbias[1]);
                                    output1 = CNN.Conv1D(output1, fweights3, true, true, dbias[2]);
                                    output1 = CNN.BatchNormalization(output1, fweightsbn);
                                    output1 = CNN.AveragePooling1D(output1, 8, 3);

                                    output1 = CNN.Conv1D(output1, fweights4, true, true, dbias[3]);
                                    output1 = CNN.Conv1D(output1, fweights5, true, true, dbias[4]);
                                    output1 = CNN.Conv1D(output1, fweights6, true, true, dbias[5]);
                                    output1 = CNN.BatchNormalization(output1, fweightsbn2);
                                    output1 = CNN.AveragePooling1D(output1, 4, 3);

                                    output1 = CNN.Conv1D(output1, fweights7, true, true, dbias[6]);
                                    output1 = CNN.Conv1D(output1, fweights8, true, true, dbias[7]);
                                    output1 = CNN.Conv1D(output1, fweights9, true, true, dbias[8]);
                                    output1 = CNN.BatchNormalization(output1, fweightsbn3);
                                    output1 = CNN.AveragePooling1D(output1, 2, 3);

                                    output1 = CNN.Conv1D(output1, fweights10, true, true, dbias[9]);
                                    output1 = CNN.Conv1D(output1, fweights11, true, true, fbias_short);
                                    output = CNN.GlobalAveragePooling1D(output1);
                                    output = CNN.SoftmaxActivation(output);

                                    //This ifs conditions helps system to change shift in real time
                                    if(shift_value == 1){
                                        buffer.addValue(output[1]);
                                    }
                                    if(shift_value==4){
                                        for (int i=0; i <shift_value;i++){
                                            buffer.addValue(output[1]);
                                        }
                                    }
                                    if(shift_value == 8){
                                        for(int i=0; i < shift_value; i++){
                                            buffer.addValue(output[1]);
                                        }
                                    }

                                }

                                //6LAYERS
                                if (layers_6_11 == 0) {

                                    output1 = CNN.Conv1D(input, fweights, true, true, dbias[0]);
                                    output1 = CNN.Conv1D(output1, fweights2, true, true, dbias[1]);
                                    output1 = CNN.Conv1D(output1, fweights3, true, true, dbias[2]);

                                    output1 = CNN.BatchNormalization(output1, fweightsbn);
                                    output1 = CNN.AveragePooling1D(output1, 8, 2);

                                    output1 = CNN.Conv1D(output1, fweights4, true, true, dbias[3]);
                                    output1 = CNN.Conv1D(output1, fweights5, true, true, dbias[4]);
                                    output1 = CNN.AveragePooling1D(output1, 4, 2);

                                    output1 = CNN.Conv1D(output1, fweights6, true, true, fbias6);
                                    output = CNN.GlobalAveragePooling1D(output1);
                                    output = CNN.SoftmaxActivation(output);

                                    //This ifs conditions helps system to change shift in real time
                                    if(shift_value == 1){
                                    buffer.addValue(output[1]);
                                    }
                                    if(shift_value==4){
                                        for (int i=0; i <shift_value;i++){
                                            buffer.addValue(output[1]);
                                        }
                                    }
                                    if(shift_value == 8){
                                        for(int i=0; i < shift_value; i++){
                                            buffer.addValue(output[1]);
                                        }
                                    }
                                }
                                position = position + (32 * shift_value);



                            }

                            //TODO Chapuza mientras buffer no esta dentro de feedEEG
                            for(int i =0; i<position;i++){
                                addEntryEeg((float)finput[i][0]);
                                input_buffer.add(i,finput[i][0]);
                            }
                            timer.add(Calendar.SECOND, 24);


                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    time.append(datef.format(timer.getTime()));
                                    b_next.setText("Start");
                                    message_box.setText("Buffer filled successfully. \n Press start button! ");
                                    progressBar.setVisibility(View.INVISIBLE);

                                }
                            });

                            /* Configurando phasevocoder */

                            vocoder.configure();

                            /*inicializando chunk y vocoder */
                            chunkLength = vocoder.getChunkLength();

                            double[] chunk = new double[chunkLength];
                            for(int i =0; i < vocoder.getHop(); i++){
                                chunk[i] = 0;
                            }
                            for (int i = vocoder.getHop(); i < chunkLength; i++){
                                chunk[i] = input_buffer.get((position-chunkLength-vocoder.getHop())+i);
                            }
                            vocoder.initialiseVocoder(chunk);



                            buffer_aux = 1;
                            handler_buffer.postDelayed(this,10);

                            handler_buffer.removeCallbacks(BufferRun);


                        }

                    };

                    //THREAD SCAN

                    mTimer = new Runnable() {
                        @Override
                        public void run() {

                            CustomMarkerCNN marker = new CustomMarkerCNN(getContext(),R.layout.cnn_marker,shift_value);
                            mChart2.setMarker(marker);

                            tv_output.setText(null);
                            tv_output2.setText(null);

                            b_next.setText("Scanning... ");



                            double[][] input = new double[256][1];
                            for (int i = position_flag; i < (256 + position_flag); i++) {
                                input[i - position_flag][0] = input_buffer.get(i);
                                //input_buffer.add(finput[i][0]);
                            }

                            float value = 0.0f;
                            for (int i = annotation; i < 8 + annotation; i++) {
                                if (flabel[i] == 1) {
                                    value = 1;
                                }
                            }
                            //TODO add new line for annotations
                            //series2.appendData(new DataPoint(x1, value), true, 11000);
                            //th_seizure.appendData(new DataPoint(x1, th_value), true, 11000);

                            double[][] output1;
                            double[] output;
                            double prob = 0;
                            //11LAYERS
                            if (layers_6_11 == 1) {
                                output1 = CNN.Conv1D(input, fweights, true, true, dbias[0]);
                                output1 = CNN.Conv1D(output1, fweights2, true, true, dbias[1]);
                                output1 = CNN.Conv1D(output1, fweights3, true, true, dbias[2]);
                                output1 = CNN.BatchNormalization(output1, fweightsbn);
                                output1 = CNN.AveragePooling1D(output1, 8, 3);

                                output1 = CNN.Conv1D(output1, fweights4, true, true, dbias[3]);
                                output1 = CNN.Conv1D(output1, fweights5, true, true, dbias[4]);
                                output1 = CNN.Conv1D(output1, fweights6, true, true, dbias[5]);
                                output1 = CNN.BatchNormalization(output1, fweightsbn2);
                                output1 = CNN.AveragePooling1D(output1, 4, 3);

                                output1 = CNN.Conv1D(output1, fweights7, true, true, dbias[6]);
                                output1 = CNN.Conv1D(output1, fweights8, true, true, dbias[7]);
                                output1 = CNN.Conv1D(output1, fweights9, true, true, dbias[8]);
                                output1 = CNN.BatchNormalization(output1, fweightsbn3);
                                output1 = CNN.AveragePooling1D(output1, 2, 3);

                                output1 = CNN.Conv1D(output1, fweights10, true, true, dbias[9]);
                                output1 = CNN.Conv1D(output1, fweights11, true, true, fbias_short);
                                output = CNN.GlobalAveragePooling1D(output1);
                                output = CNN.SoftmaxActivation(output);

                                if(shift_value == 1){
                                    buffer.addValue(output[1]);
                                }
                                if(shift_value==4){
                                    for (int i=0; i <shift_value;i++){
                                        buffer.addValue(output[1]);
                                    }
                                }
                                if(shift_value == 8){
                                    for(int i=0; i < shift_value; i++){
                                        buffer.addValue(output[1]);
                                    }
                                }
                                prob = CNN.MAF(buffer);
                            }

                            //6LAYERS
                            if (layers_6_11 == 0) {
                                output1 = CNN.Conv1D(input, fweights, true, true, dbias[0]);
                                output1 = CNN.Conv1D(output1, fweights2, true, true, dbias[1]);
                                output1 = CNN.Conv1D(output1, fweights3, true, true, dbias[2]);
                                output1 = CNN.BatchNormalization(output1, fweightsbn);
                                output1 = CNN.AveragePooling1D(output1, 8, 2);

                                output1 = CNN.Conv1D(output1, fweights4, true, true, dbias[3]);
                                output1 = CNN.Conv1D(output1, fweights5, true, true, dbias[4]);
                                output1 = CNN.AveragePooling1D(output1, 4, 2);

                                output1 = CNN.Conv1D(output1, fweights6, true, true, fbias6);
                                output = CNN.GlobalAveragePooling1D(output1);
                                output = CNN.SoftmaxActivation(output);

                                if(shift_value == 1){
                                    buffer.addValue(output[1]);
                                }
                                if(shift_value==4){
                                    for (int i=0; i <shift_value;i++){
                                        buffer.addValue(output[1]);
                                    }
                                }
                                if(shift_value == 8){
                                    for(int i=0; i < shift_value; i++){
                                        buffer.addValue(output[1]);
                                    }
                                }
                                prob = CNN.MAF(buffer);
                            }

                            resp_adaptation.add(prob);//Save prob for respiration adaptation

                            double buf[] = buffer.getValue();

                            timer.add(Calendar.SECOND, shift_value);


                            DecimalFormat df = new DecimalFormat("#.00000000");// Para que no redondee
                            time.setText(datef.format(timer.getTime()));
                            tv_output2.append(df.format(prob));
                            tv_output.append(df.format(buf[24])); //The same position of the buffer always cause' we want to change shift in real time
                            message_box.setText("Scanning...");


                            //smooth.appendData(new DataPoint(x1, prob), true, 11000);
                            //green.appendData(new DataPoint(x1, buf[24]), true, 11000);//The same position of the buffer always cause' we want to change shift in real time

                            // FILL METER
                            fillMeter(prob);

                            //RED
                            if (prob >= th_value) {
                                if (count == 0) {
                                    Calendar maf_time; //Auxiliar variable to plus the collar time and not changing the real time value.
                                    maf_time = (Calendar) timer.clone();
                                    maf_time.add(Calendar.SECOND, -24);
                                    minute = datef.format(maf_time.getTime());
                                    maf_time.add(Calendar.SECOND,48); //In case seizure in only one epoch!
                                    aux_time = datef.format(maf_time.getTime());
                                    array4seizures[list_position][0] = position_flag - 768 -768; // - samples in 24s MAF, - position updated to the center maf point.
                                    array4seizures[list_position][1] = position_flag;//In case only one epoch of seizure + 768 24s MAF
                                }
                                if (count > 0) {
                                    Calendar maf_time; //Auxiliar variable to plus the collar time and not changing the real time value.
                                    maf_time = (Calendar) timer.clone();
                                    maf_time.add(Calendar.SECOND, 24);
                                    aux_time = datef.format(maf_time.getTime());
                                    array4seizures[list_position][1] = position_flag; //  +768 - 768!!!!
                                }
                                count++;

                                traffic.setBackgroundResource(R.drawable.traffic_light_red_horizontal);

                                addEntryProb((float)prob,true);
                                //red.appendData(new DataPoint(x1, prob), true, 2000);

                                //RESPIRATION ADAPTATION
                                idxtmp.add(kk, i_aux);
                                double y = resp.RespirationAdaptation(kk, idxtmp, resp_adaptation);
                                kk++;
                                //respiration.appendData(new DataPoint(x1, y), true, 11000);

                                //YELLOW
                            } else if (th_value > prob && prob > (th_value - 0.1)) {
                                if (count > 0) {
                                    minute = minute.concat(" - ").concat(aux_time);
                                    arrayList.add(minute);
                                    count = 0;
                                    list_position++;
                                    num_seizures_detected++;
                                    adapter.notifyDataSetChanged();

                                }

                                traffic.setBackgroundResource(R.drawable.traffic_light_yellow_horizontal);

                                addEntryProb((float)prob,false);
                                //yellow.appendData(new DataPoint(x1, prob), true, 2000);
                                //respiration.appendData(new DataPoint(x1, prob), true, 11000);

                                //GREEN
                            } else {

                                traffic.setBackgroundResource(R.drawable.traffic_light_green_horizontal);


                                if (count > 0) {
                                    minute = minute.concat(" - ").concat(aux_time);
                                    arrayList.add(minute);
                                    list_position++;
                                    count = 0;
                                    adapter.notifyDataSetChanged();

                                }
                                addEntryProb((float)prob,false);
                                //respiration.appendData(new DataPoint(x1, prob), true, 11000);


                            }
                            x1 += 1;
                            //position = position + 32 * shift_value;
                            annotation = annotation + shift_value;
                            i_aux++;

                            //handler.postDelayed(this, speed * 800 * shift_value + 1);

                            //if(position_flag> inputlen -256){
                            //    handler.removeCallbacks(mTimer);
                            //    stop = 1;
                            //}

                        }


                    };


                    if (layers_6_11 == -1) {
                        Toast.makeText(getActivity(), "None of the layers have been choosen", Toast.LENGTH_LONG).show();
                    } else if (shift_set == 0) {
                        Toast.makeText(getActivity(), "Please choose shift!", Toast.LENGTH_LONG).show();

                    } else if (baby_set == 0) {
                        Toast.makeText(getActivity(), "Select a baby before buffer!", Toast.LENGTH_LONG).show();

                    } else if (buffer_aux == 0) {
                        progressBar.setVisibility(View.VISIBLE);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                while(progressStatus < 100){
                                    progressStatus ++;
                                    handler_progress.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setProgress(progressStatus);
                                        }
                                    });
                                    if(layers_6_11==0){
                                        try{
                                            Thread.sleep(39);
                                        }catch (InterruptedException e){
                                            e.printStackTrace();
                                        }
                                    }else if(layers_6_11==1){
                                        try{

                                            Thread.sleep(50);
                                        }catch (InterruptedException e){
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }).start();

                        if (layers_6_11 == 0) { //Read weights from 6
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    readFiles6();
                                }
                            }).start();
                        } else if (layers_6_11 == 1) {//Read weights from 11
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    readFiles11();
                                }

                            }).start();

                        }
                        b_next.setText("Buffering...");
                        message_box.setText("Buffering please wait!");

                        stop = 1;
                    } else {
                        if (stop == 1) {
                            //mTimer.run();
                            feedEeg.run();
                            stop = 0;
                        }
                    }

                }
            }

        });
        b_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                message_box.setText("Stopped!");
                //handler.removeCallbacks(mTimer);
                handler_time.removeCallbacks(feedEeg);
                b_next.setText("Resume");
                stop = 1;
                //TODO add message when the acquisition is finished and you want to resume!!




            }
        });

        b_soni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vocoderPlayer.setSamples(vocoder.getVocoderSamples());
                vocoderPlayer.runTest();
            }
        });


        b_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(stop == 1) {

                    timer.set(Calendar.HOUR, 0);
                    timer.set(Calendar.MINUTE, 0);
                    timer.set(Calendar.SECOND, 0);
                    timer.set(Calendar.HOUR_OF_DAY, 0);
                    time.setText(null);

                    buffer_aux = 0;

                    input_buffer.clear();

                    options.setSelection(0);



                    th_value =0.5;
                    sb.setProgress(10);
                    kk = 0;
                    position = 0;
                    annotation = 24;
                    x1 = 0.0;
                    list_position = 0;
                    array4seizures = new int[20][2];
                    message_box.setText("");
                    shift_value = 0;
                    shift_set = 0;
                    adapter.clear();

                    tv_output.setText(null);
                    tv_output2.setText(null);

                    b_next.setText("Start");

                    progressBar.setProgress(0);
                    progressStatus = 0;
                    verticalBar.setProgress(0);

                    dataEeg.clearValues();
                    dataProb.clearValues();
                    mChart2.getAxis(YAxis.AxisDependency.LEFT).removeAllLimitLines();
                    LimitLine new_th = new LimitLine((float)th_value);
                    new_th.setLineColor(Color.parseColor("#02a3dc"));
                    new_th.enableDashedLine(4f,4f,0f);
                    new_th.setLineWidth(2f);
                    mChart2.getAxis(YAxis.AxisDependency.LEFT).addLimitLine(new_th);
                    mChart.getXAxis().setAxisMaximum(640);
                    mChart.getXAxis().setAxisMinimum(0);
                    mChart2.invalidate();
                    mChart.invalidate();


                }
                else{
                    Toast.makeText(getActivity(),"Acquisition running, press stop first!", Toast.LENGTH_LONG).show();

                }


            }
        });


        return view;
    }
    private void addEntryEeg(float inputEeg){
        LineData data = mChart.getData();

        if(data != null){
            ILineDataSet set = data.getDataSetByIndex(0);

            if(set == null){
                set = createSetEeg();
                data.addDataSet(set);
            }
          //TODO add updating points, not sure if it's how we are supposed to plot signal
            data.addEntry(new Entry(set.getEntryCount(),inputEeg),0);
            data.notifyDataChanged();
            if(set.getEntryCount() == 641){
                XAxis xaxis = mChart.getXAxis();
                xaxis.resetAxisMaximum();
                xaxis.resetAxisMinimum();
            }

            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(640);
            mChart.setVisibleXRangeMinimum(640);
            mChart.moveViewTo(data.getEntryCount(),0f, YAxis.AxisDependency.LEFT);

        }
    }
    private void addEntryProb(float prob, boolean highlight){
        LineData data = mChart2.getData();

        if(data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {
                set = createSetProb();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), prob), 0);
            data.notifyDataChanged();

            if(set.getEntryCount() == 61){
                XAxis xaxis = mChart2.getXAxis();
                xaxis.resetAxisMinimum();
                xaxis.resetAxisMaximum();
            }

            //GRADIENT PROBS
            Paint paint = mChart2.getRenderer().getPaintRender();
            int height = mChart2.getHeight();

            //no he sido capaz de controlar el centro del gradiente
            LinearGradient linGrad = new LinearGradient(0,0,0,height, Color.RED, Color.GREEN, Shader.TileMode.REPEAT);
            paint.setShader(linGrad);


            mChart2.notifyDataSetChanged();
            mChart2.setVisibleXRangeMinimum(0);
            mChart2.setVisibleXRangeMaximum(60);
            mChart2.moveViewToX(data.getEntryCount());
        }
    }
    private LineDataSet createSetProb(){
        LineDataSet set = new LineDataSet(null,"Prob data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setHighLightColor(Color.argb(150,2,163,220));
        set.setDrawCircles(true);
        set.setLineWidth(4);
        set.setCircleColorHole(Color.WHITE);
        set.setCircleRadius(3);
        set.setDrawValues(false);


        return set;
    }
    private LineDataSet createSetEeg(){

        LineDataSet set = new LineDataSet(null,"EEG Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setDrawValues(false);
        set.setColor(Color.WHITE);
        set.setDrawCircles(false);

        return set;

    }
    private void fillMeter(double prob){
        //TODO try to change cneterY of gradient instead of adding th_value
        ProgressBarAnimation anim = new ProgressBarAnimation(verticalBar, verticalBar.getProgress(),(float) (prob + 0.5 - th_value)*1000);
        anim.setDuration(1000*(shift_value));
        verticalBar.startAnimation(anim);
        //verticalBar.setProgress((int)(prob*1000));
    }
    private class executeVocoder extends AsyncTask<double[],Void,Void>{
        @Override
        protected Void doInBackground(double[]... params) {
            for (int i = 0; i < 5; i++) {
                vocoder.readNextChunk(params[0]);
                vocoder.run();
            }
            return  null;

        }

        @Override
        protected void onPostExecute(Void v) {
        }

        @Override
        protected void onPreExecute() {
            message_box.setText("Sonifying");
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private class playSonification extends  AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            vocoderPlayer.setSamples(vocoder.getVocoderSamples());
            vocoderPlayer.runTest();

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            message_box.setText("Preparing audio");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            message_box.setText("Playing...");
        }
    }
}


