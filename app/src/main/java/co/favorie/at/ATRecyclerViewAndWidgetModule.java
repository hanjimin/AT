package co.favorie.at;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import co.favorie.at.character.ATCharacterAdapter;
import co.favorie.at.notification.ATNotification;
import co.favorie.at.widget.ATWidget;

/**
 * Created by bmac on 2015-08-21.
 */
public class ATRecyclerViewAndWidgetModule extends LinearLayout{

    private RecyclerView charsListRecyclerview;
    private ATCharacterAdapter charsAdapter;
    private ATWidget mWidget;
    private ATNotification mNotification;
    private Context context;
    private ModuleCallbacks callback;


    public interface ModuleCallbacks {

         void charSelected(int index);
         void widgetChanged(boolean status);
         void notiChanged(boolean status);
    }
    public void setOnCharsViewCallbacks(ModuleCallbacks callback) {
        this.callback = callback;
    }

    public ATRecyclerViewAndWidgetModule(Context context) {
        super(context);
        this.context = context;
    }
    public ATRecyclerViewAndWidgetModule(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ATRecyclerViewAndWidgetModule(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initModule(int selected_char_position) {


        charsListRecyclerview = (RecyclerView) findViewById(R.id.at_recyclerview_and_widget_module_recyclerview);
        mWidget = (ATWidget) findViewById(R.id.at_recyclerview_and_widget_module_widget_button);
        mNotification = (ATNotification) findViewById(R.id.at_recyclerview_and_notification_module_notification_button);


        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        charsListRecyclerview.setLayoutManager(llm);
        charsListRecyclerview.scrollToPosition(selected_char_position);
        charsAdapter = new ATCharacterAdapter();

        charsAdapter.setOnItemClickListener(new ATCharacterAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {

                charsAdapter.setIndicatorOnByIndex(position);
                ImageView indicator = (ImageView)view.findViewById(R.id.at_cardview_character_indicator);
                indicator.setVisibility(VISIBLE);
                callback.charSelected(position);

            }
        });

        charsListRecyclerview.setAdapter(charsAdapter);

        mWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWidget.changeStatus();
                callback.widgetChanged(mWidget.getStatus());
            }
        });

        mNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNotification.changeStatus();
                callback.notiChanged(mNotification.getStatus());
            }
        });
    }

    public void notifyAdapter(){
        charsAdapter.notifyDataSetChanged();
    }

    public void setWidget(boolean status) {
        mWidget.setWidget(status);
    }
    public void setNotification(boolean status) {
        mNotification.setNotification(status);
    }

    /**
     * 캐릭터 선택이 완료된 index
     *
     * @param index
     */
    public void setSelectedCharIndex(int index) {
        charsAdapter.setIndicatorOnByIndex(index);
    }

}
