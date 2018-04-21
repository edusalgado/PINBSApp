package Utilities;

import android.content.Context;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

/**
 * Created by SalgadoE on 15/03/2018.
 */

public class CustomMarkerSonify extends MarkerView {

    public CustomMarkerSonify (Context context, int layoutResource) {
        super(context, layoutResource);
        // this markerview only displays a textview
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
// content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        super.refreshContent(e,highlight);

    }

    private MPPointF mOffset;

    public MPPointF getOffset() {
        if(mOffset == null){
            //center the marker horizontally and vertically

            mOffset = new MPPointF(-(getWidth()/2),-getHeight()-100);
        }

        return mOffset;
    }
}

