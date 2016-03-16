package co.favorie.at.preference;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by quki on 2015-11-28.
 */
public class AlarmDialogSession {

    private static final int PRIVATE_MODE = 0;
    // 캐릭터 리스트 사이즈 크기를 이전 것과 비교해서 크면 새로운 캐릭터가 추가된 것이므로 alarm dialog를 띄워준다
    private static final String CHARACTER_LIST_SIZE= "charlistsize";
    private Context context;
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;

    public AlarmDialogSession(Context context){
        this.context = context;
        mPref =  context.getSharedPreferences("AlarmDialog",PRIVATE_MODE);
        mEditor =  mPref.edit();

    }

    public boolean isAddedNewCharacter(int size){

        if(mPref.getInt(CHARACTER_LIST_SIZE,26) == size){
            return false;
        }else{
            mEditor.putInt(CHARACTER_LIST_SIZE, size);
            mEditor.commit();
            return true;
        }
    }



}
