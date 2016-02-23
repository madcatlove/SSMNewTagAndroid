package kr.dude.newtag;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.ArcProgress;

import org.w3c.dom.Text;

/**
 * Created by madcat on 2016. 2. 18..
 */
public class ArcProgressDialog extends Dialog {

    private TextView messageView;
    private ArcProgress arcProgress;
    private TextView titleView;

    public ArcProgressDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.customdialog);

        messageView = (TextView) findViewById(R.id.progress_message);
        arcProgress = (ArcProgress) findViewById(R.id.arcprogress);
        titleView = (TextView) findViewById(R.id.progress_title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    }

    public void setMessage(String message) {
        messageView.setText(message);
    }

    public void setPercent(Integer percent) {
        arcProgress.setProgress(percent);
    }

    public void setTitleView(String title) {
        titleView.setText(title);
    }


}
