package co.favorie.at.customview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import co.favorie.at.MainActivity;

/**
 * Created by bmac on 2015-08-11.
 */
public class AnimatingSeekBar extends SeekBar{

    private Context mContext;
    private ValueAnimator animator;
    private Bitmap labelBackground;
    private Drawable progressDrawable, thumbDrawable;
    private Rect barBounds, labelTextRect;
    private Paint labelTextPaint, labelBackgroundPaint;
    private Point labelPos;
    private ImageButton btnThumb;
    private TextView txtLabel;
    int viewWidth, barHeight, labelOffset;
    float progressPosX;
    String thumbText;

    public AnimatingSeekBar(Context context) {
        super(context);
        mContext = context;
    }

    public AnimatingSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public AnimatingSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // Log.i("Seekbar", "onMeasure");
        if (labelBackground != null) {

            viewWidth = getMeasuredWidth();
            barHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
            setMeasuredDimension(viewWidth + labelBackground.getWidth(), barHeight + labelBackground.getHeight() / 2);
        }
    }
    /*Seekbar가 disabled 상태일 때 투명도를 없애주는 함수*/
    @Override
    protected void drawableStateChanged(){
        super.drawableStateChanged();

        Drawable progressDrawable = getProgressDrawable();
        if(isEnabled() == false){
            progressDrawable.setAlpha(255);
        }
    }
//////////////////////////////////////////////////////////////////

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        if(labelBackground != null) {
            barBounds.left = getPaddingLeft();
            barBounds.top = labelBackground.getHeight() + getPaddingTop();
            barBounds.right = barBounds.left + viewWidth - getPaddingRight() - getPaddingLeft();
            barBounds.bottom = barBounds.top + barHeight - getPaddingBottom() - getPaddingTop();

            progressPosX = barBounds.left + ((float) this.getProgress() / (float) this.getMax()) * barBounds.width();

            labelOffset = labelBackground.getWidth() / 2;
            labelPos.x = (int) progressPosX - labelOffset;
            labelPos.y = getPaddingTop();

            progressDrawable = getProgressDrawable();
            progressDrawable.setBounds(barBounds.left, barBounds.top, barBounds.right, barBounds.bottom);
            progressDrawable.draw(canvas);

//            labelTextPaint.getTextBounds(thumbText, 0, thumbText.length(), labelTextRect);

            float ratio = labelBackground.getDensity() / 480.0f;
            int offset = (int)(20 * ratio);

            btnThumb.setImageBitmap(labelBackground);
            btnThumb.setX(progressPosX - (btnThumb.getWidth()/2) + 2*offset + (offset/3) - (offset/7));
            btnThumb.setY(labelPos.y + 6*offset);
            btnThumb.bringToFront();
            labelTextPaint.getTextBounds(thumbText, 0, thumbText.length(), labelTextRect);

            txtLabel.setText(thumbText);
            txtLabel.setX(progressPosX - (2*offset) + (offset/3));
            txtLabel.setY(labelPos.y + (7*offset) - (offset/2) + (offset/4));
            txtLabel.bringToFront();

//            canvas.drawBitmap(labelBackground, labelPos.x, labelPos.y + offset, labelBackgroundPaint);
//            canvas.drawText(thumbText, labelPos.x + labelBackground.getWidth() / 2 - labelTextRect.width() / 2 - 2, labelPos.y + labelBackground.getHeight() / 14 * 5 + labelTextRect.height() / 2, labelTextPaint);

            /*
            int thumbX = (int) progressPosX - getThumbOffset();
            int bottom = barBounds.bottom - (barBounds.height() / 2);
            thumbDrawable.setBounds(thumbX, bottom - thumbDrawable.getIntrinsicHeight(), thumbX + thumbDrawable.getIntrinsicWidth(), bottom);
            //thumbDrawable.draw(canvas);
            barBounds = new Rect();
            barBounds.left = getPaddingLeft();
            barBounds.top = (int) (labelBackground.getHeight() / 2f);
            barBounds.right = barBounds.left + viewWidth - getPaddingRight() - getPaddingLeft();
            barBounds.bottom = barBounds.top + barHeight - getPaddingBottom() - getPaddingTop();

            progressPosX = barBounds.top + ((float) this.getProgress() / (float) this.getMax()) * barBounds.height() + getTopPaddingOffset();
            labelPos.y = getBottom() - (int) progressPosX - labelOffset ;//+ (int) (getProgress() * 0.1f);
            labelPos.x = getPaddingLeft();

            progressDrawable = getProgressDrawable();
            progressDrawable.setBounds(barBounds.left, barBounds.top, barBounds.right, getBottom());
            progressDrawable.draw(canvas);

            labelTextPaint.getTextBounds(thumbText, 0, thumbText.length(), labelTextRect);
            canvas.drawBitmap(labelBackground, labelPos.x, labelPos.y, labelBackgroundPaint);
            int drawPosX = labelPos.x + labelBackground.getWidth() / 2 - labelTextRect.width() / 2;
            int drawPosY = labelPos.y + labelBackground.getHeight() / 2 + labelTextRect.height() / 2;
            canvas.drawText(thumbText,  drawPosX, drawPosY, labelTextPaint);*/
        } else {
            super.onDraw(canvas);
        }
    }

    public void startProgressAnimation(int percentage){
        animator = ValueAnimator.ofInt(0, percentage);//getMax());
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animProgress = (Integer) animation.getAnimatedValue();
                setProgress(animProgress);
            }
        });
        animator.start();
    }

    public void setThumbButton(ImageButton btn_Thumb){

        btnThumb=btn_Thumb;
        float ratio = labelBackground.getDensity() / 480.0f;
        int offset = (int) (20 * ratio);
        btnThumb.setImageBitmap(labelBackground);
        btnThumb.setX(labelPos.x);
        btnThumb.setY(labelPos.y + offset);
    }

    public void setThumbLabel(TextView txt_Label){
        txtLabel = txt_Label;
        float ratio = labelBackground.getDensity() / 480.0f;
        int offset = (int) (20 * ratio);
        txtLabel.setText(thumbText);
        txtLabel.setX(progressPosX - (2*offset) + (offset/3));
        txtLabel.setY(labelPos.y + (7*offset) - (offset/2) + (offset/4));
    }

    public void setThumbDrawableWithString(int drawableId, String text) {
        if(text == null)
            text = "";
        barBounds = new Rect();
        labelPos = new Point();
        progressDrawable = getProgressDrawable();
        thumbText = text;
        labelTextPaint = new Paint();
        labelTextPaint.setColor(Color.parseColor("#5a5a5a"));
        labelTextPaint.setTypeface(MainActivity.BOLD_NOTO);
        labelTextPaint.setAntiAlias(true);
        labelTextPaint.setDither(true);
        final float scale = getResources().getDisplayMetrics().density;
        labelTextPaint.setTextSize(10f*scale); //// TODO: 2015-09-11 main activity lable text size
        labelBackgroundPaint = new Paint();
        labelTextRect = new Rect();


        Resources r = mContext.getResources();
        thumbDrawable = getResources().getDrawable(drawableId);
        labelBackground = BitmapFactory.decodeResource(r, drawableId).copy(Bitmap.Config.ARGB_8888, true);

/*        float scale = r.getDisplayMetrics().density;
        Canvas canvas = new Canvas(labelBackground);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK); //Change this if you want other color of text
        paint.setTextSize((int) (30 * scale)); //Change this if you want bigger/smaller font

        canvas.drawBitmap(labelBackground, 0, 0, null);
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        canvas.drawText(text, 0, labelBackground.getHeight() / 2, paint); //Change the position of the text here

        setThumb(new BitmapDrawable(labelBackground));*/
    }
/*
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                final int width = getWidth();0
                final int available = width - getPaddingLeft() - getPaddingRight();
                int x = (int) event.getX();
                float scale;
                float progress = 0;
                if (x < getPaddingLeft()) {
                    scale = 0.0f;
                } else if (x > width - getPaddingRight()) {
                    scale = 1.0f;
                } else {
                    scale = (float) (x - getPaddingLeft()) / (float) available;
                }
                final int max = getMax();
                progress += scale * max;
                if (progress < 0) {
                    progress = 0;
                } else if (progress > max) {
                    progress = max;
                }

                if (Math.abs(progress - getProgress()) < 10)
                    return super.onTouchEvent(event);
                else
                    return false;
            default:
                return super.onTouchEvent(event);
        }
    }
*/
}
