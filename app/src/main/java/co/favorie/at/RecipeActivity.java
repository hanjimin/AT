package co.favorie.at;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

public class RecipeActivity extends Activity {

    int i=0;
    Intent[] intent = new Intent[10];
    final static int ACTIVITY_FOR_NEW_RECIPE = 100;
    int screen_height, grid_height;
    int screen_width, grid_width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "notosans_bold.otf");
        View customActionBar = (View) findViewById(R.id.recipe_activity_action_bar);
        GridView grid_recipe = (GridView) findViewById(R.id.grid_view);

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(this.getResources().getDisplayMetrics().widthPixels, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        customActionBar.measure(widthMeasureSpec, heightMeasureSpec);

        float scale = this.getResources().getDisplayMetrics().density;

        screen_width = (int) (this.getResources().getDisplayMetrics().widthPixels);
        grid_width = (screen_width - 4*grid_recipe.getPaddingLeft())/3;

        screen_height = (int) (this.getResources().getDisplayMetrics().heightPixels);
        grid_height = (screen_height - customActionBar.getMeasuredHeight() - customActionBar.getLayoutParams().height + 50)/3;

        Button leftButton = (Button) customActionBar.findViewById(R.id.at_action_bar_button_left);
        leftButton.setVisibility(View.VISIBLE);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        for(i = 0; i < 4; i++){
            if(i == 0){
                intent[i] = new Intent(RecipeActivity.this, DateDetailActivity.class);
                intent[i].putExtra("update_or_insert",1);
                intent[i].putExtra("recipe_type","exam");
            } else if(i == 1){
                intent[i] = new Intent(RecipeActivity.this, DateDetailActivity.class);
                intent[i].putExtra("update_or_insert",1);
                intent[i].putExtra("recipe_type","love");
            } else if(i == 2){
                intent[i] = new Intent(RecipeActivity.this, TimeDetailActivity.class);
                intent[i].putExtra("update_or_insert",1);
                intent[i].putExtra("recipe_type","exercise");
            } else if(i == 3){
                intent[i] = new Intent(RecipeActivity.this, CustomDetailActivity.class);
                intent[i].putExtra("update_or_insert",1);
                intent[i].putExtra("recipe_type","diet");
            }
        }
        for (i = 4; i < 9; i++) {
            intent[i] = new Intent(RecipeActivity.this, UpdateDefaultATActivity.class);
            intent[i].putExtra("selected_recipe", i - 4);
            intent[i].putExtra("update_or_insert", 1);   //1 = insert
            if (i == 5)
                intent[i].putExtra("isWeekly", true);
            if (i == 8)
                intent[i].putExtra("isLifeTime", true);
        }

        grid_recipe.setAdapter(new RecipeAdapter(this));

        grid_recipe.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivityForResult(intent[position], ACTIVITY_FOR_NEW_RECIPE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if(requestCode == ACTIVITY_FOR_NEW_RECIPE) {
                Intent i = getIntent();
                setResult(RESULT_OK, i);
                finish();
            }
        }
    }

    public class RecipeAdapter extends BaseAdapter {
        private Context mContext;

        // Keep all Images in array
        public Integer[] txt_id = {
                R.string.default_exam, R.string.default_anniv,
                R.string.default_exercise, R.string.default_diet,
                R.string.default_daily, R.string.default_weekly,
                R.string.default_monthly, R.string.default_yearly,
                R.string.default_life_time
        };

        public Integer[] color_id = {
                R.color.recipe1, R.color.recipe2,
                R.color.recipe3, R.color.recipe4,
                R.color.recipe5, R.color.recipe6,
                R.color.recipe7, R.color.recipe8,
                R.color.recipe9
        };

        // Constructor
        public RecipeAdapter(Context c){
            mContext = c;
        }

        @Override
        public int getCount() {
            return txt_id.length;
        }

        @Override
        public Object getItem(int position) {
            return txt_id[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Typeface typeface_view = Typeface.createFromAsset(getAssets(), "notosans_bold.otf");
            TextView txt_recipe = new TextView(mContext);
            txt_recipe.setLayoutParams(new GridView.LayoutParams(grid_width, grid_height));
            txt_recipe.setTextSize(12);
            txt_recipe.setGravity(Gravity.CENTER);
            txt_recipe.setTextColor(getResources().getColor(R.color.white));
            txt_recipe.setTypeface(typeface_view);
            txt_recipe.setBackgroundColor(getResources().getColor(color_id[position]));
            txt_recipe.setText(getResources().getString(txt_id[position]));
            return txt_recipe;
        }

    }
}
