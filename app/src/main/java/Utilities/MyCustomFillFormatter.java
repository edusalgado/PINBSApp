package Utilities;

import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

/**
 * Created by SalgadoE on 05/03/2018.
 */

public class MyCustomFillFormatter implements IFillFormatter {
    private ILineDataSet boundaryDataSet;

    public MyCustomFillFormatter() {
        this(null);
    }
    //Pass the dataset of other line in the Constructor
    public MyCustomFillFormatter(ILineDataSet boundaryDataSet) {
        this.boundaryDataSet = boundaryDataSet;
    }

    @Override
    public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
        return -100f;
    }

   }