package co.favorie.at.customview;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import co.favorie.at.MainActivity;
import co.favorie.at.R;

/**
 * Created by bmac on 2015-08-26.
 */
public class ATCustomFragmentActivity extends FragmentActivity {

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setStatusBarColor();
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        setGlobalFont(root);
    }

    // STATUS BAR COLOR Change

    private void setStatusBarColor() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getStatusBarColor(getIntent().getIntExtra("colorCode", 0)));
        }
    }

    private int getStatusBarColor(int colorCode) {


        if (colorCode == Color.parseColor("#ABD8CC")) {
            return Color.parseColor("#80BCAA");
        } else if (colorCode == Color.parseColor("#C8DCCF")) {
            return Color.parseColor("#9FC99F");
        } else if (colorCode == Color.parseColor("#EBEBBE")) {
            return Color.parseColor("#CECC93");
        } else if (colorCode == Color.parseColor("#FEE1A1")) {
            return Color.parseColor("#F2C977");
        } else if (colorCode == Color.parseColor("#F7D38B")) {
            return Color.parseColor("#E2AF5D");
        } else if (colorCode == Color.parseColor("#F4B98C")) {
            return Color.parseColor("#ED9B64");
        } else if (colorCode == Color.parseColor("#F29F83")) {
            return Color.parseColor("#E57D61");
        } else if (colorCode == Color.parseColor("#ED8B7E")) {
            return Color.parseColor("#E06C63");
        }

        return getResources().getColor(R.color.status_red_dark);
    }

    void setGlobalFont(ViewGroup root) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof TextView) {
                try {
                    TextView c = (TextView) child;
                    int style = c.getTypeface().getStyle();
                    if (style == Typeface.BOLD)
                        ((TextView) child).setTypeface(MainActivity.BLACK_NOTO);
                    else
                        ((TextView) child).setTypeface(MainActivity.BOLD_NOTO);
                } catch (Exception e) {

                }
            } else if (child instanceof ViewGroup)
                setGlobalFont((ViewGroup) child);
        }
    }
}
