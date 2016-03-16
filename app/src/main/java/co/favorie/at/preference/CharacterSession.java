package co.favorie.at.preference;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * Created by quki on 2015-11-22.
 */
public class CharacterSession {

    private Context context;
    private static final int PRIVATE_MODE = 0;
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;

    //Initialize
    public CharacterSession(Context context){
        this.context = context;
        mPref = context.getSharedPreferences("ATcharacter",PRIVATE_MODE);
        mEditor = mPref.edit();

        if(mPref.getAll().size()==0){
            mEditor.putBoolean("man", true);
            mEditor.putBoolean("woman", true);
            mEditor.putBoolean("chosuk", true);
            mEditor.putBoolean("mcdonald", true);
            mEditor.putBoolean("buzz", true);
            mEditor.putBoolean("hulk", true);
            mEditor.putBoolean("superman", true);
            mEditor.apply();
        }

    }

    // Set Owned
    public void setOwned(String ownedCharacter){
        mEditor.putBoolean(ownedCharacter, true);
        mEditor.commit();
    }

    // Get Status Owned
    public boolean isOwned(String ownedCharacter){
        return mPref.getBoolean(ownedCharacter, false);
    }

    public void getMyCharacterDetails(){
        Map<String,?> map = mPref.getAll();
        for(int i =0; i<map.size(); i++){
        }
    }
}
