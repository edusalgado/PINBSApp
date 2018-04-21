package Utilities;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ie.ucc.salgadoe.pinbsapp.R;

/**
 * Created by SalgadoE on 15/03/2018.
 */

public class CustomMarkerCNN extends MarkerView {

    private TextView tvProb;
    private TextView tvTime;
    private int shift_value;
    private DateFormat datef = new SimpleDateFormat("HH:mm:ss");

    public CustomMarkerCNN(Context context, int layoutResource, int shift_value) {
        super(context, layoutResource);
        // this markerview only displays 2 textviews
        tvProb = (TextView) findViewById(R.id.tvprob);
        tvTime = (TextView) findViewById(R.id.tvtime);
        this.shift_value = shift_value;
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
// content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        Calendar actual_time = new GregorianCalendar();
        actual_time.set(Calendar.HOUR,0);
        actual_time.set(Calendar.MINUTE,0);
        actual_time.set(Calendar.SECOND,0);
        actual_time.set(Calendar.HOUR_OF_DAY,0);
        int time = (int)(e.getX()*shift_value)+32;
        float prob = e.getY();

        actual_time.add(Calendar.SECOND,time);
        DecimalFormat df = new DecimalFormat("##.#####");
        df.setRoundingMode(RoundingMode.DOWN);
        tvProb.setText(df.format(prob));
        tvTime.setText(datef.format(actual_time.getTime()));
        super.refreshContent(e,highlight);


    }

    private MPPointF mOffset;

    public MPPointF getOffset() {
        if(mOffset == null){
            //center the marker horizontally and vertically

            mOffset = new MPPointF(-(getWidth()/2),-getHeight());
        }

        return mOffset;
    }
}

