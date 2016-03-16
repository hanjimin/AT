package co.favorie.at;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.favorie.at.character.ATCharacter;

/**
 * Created by bmac on 2015-08-19.
 * 캐릭터들의 속성이나 구매 내역 관리
 */
public class ATCharacterManager {
    private static ATCharacterManager managerInstance = new ATCharacterManager();
    List<ATCharacter> characterList;

    public ATCharacterManager() {
    }
    public static ATCharacterManager getInstance() {
        return managerInstance;
    }

    /*현재 속성을 반영한 캐릭터들의 리스트를 반환*/
    public List<ATCharacter> getCharacterList() {
        return characterList;
    }

    /*캐릭터들의 특성 초기화*/
    public void initiate(Context context) {
        //characterList = ATCharacter.listAll(ATCharacter.class);
        if(characterList == null) {
            characterList = new ArrayList<>();

            /*캐릭터들 이름*/
            String tempRelease[] = {"man", "woman", "chosuk", "mcdonald", "buzz", "hulk", "superman","military","gomsin","boarding","health"
                    ,"children", "hiphop","marine", "beach", "marinegirl", "harry", "police", "thief", "guard","sherlock", "watson",  "cowboy", "chef",
                    "doctor", "baseball","santa","rudolph"};

            //////////////Using List////////////////////
            List<String> charName = Arrays.asList(tempRelease);

            for (int i = 0; i < charName.size(); i++) {


                ATCharacter tempATChar = new ATCharacter(charName.get(i));


                switch (tempATChar.name){
                    case "man":
                        tempATChar.setFree();
                        break;

                    case "woman":
                        tempATChar.setFree();
                        break;

                    case "chosuk":
                        tempATChar.setFree();
                        break;
                    case "mcdonald":
                        tempATChar.setFree();
                        break;

                    case "buzz":
                        tempATChar.setFree();
                        break;

                    case "hulk":
                        tempATChar.setFree();
                        break;
                    case "superman":
                        tempATChar.setFree();
                        break;
                    case "military":
                        tempATChar.setPackage();
                        break;
                    case "gomsin":
                        tempATChar.setPackage();
                        break;
                    case "boarding":
                        tempATChar.setFreeWithBand();
                        break;
                    case "health":
                        tempATChar.setFreeWithBand();
                        break;

                    case "children":
                        tempATChar.setFreeWithBand();
                        break;

                    case "marine":
                        tempATChar.setFreeWithBand();
                        break;
                    case "beach":
                        tempATChar.setPackage();
                        break;

                    case "marinegirl":
                        tempATChar.setPackage();
                        break;

                    case "police":
                        tempATChar.setPackage();
                        break;
                    case "thief":
                        tempATChar.setPackage();
                        break;
                    case "sherlock":
                        tempATChar.setPackage();
                        break;

                    case "watson":
                        tempATChar.setPackage();
                        break;

                    case "harry":
                        tempATChar.setHaveToBuy();
                        break;
                    case "guard":
                        tempATChar.setHaveToBuy();
                        break;

                    case "cowboy":
                        tempATChar.setHaveToBuy();
                        break;

                    case "chef":
                        tempATChar.setHaveToBuy();
                        break;
                    case "doctor":
                        tempATChar.setFreeWithBand();
                        break;

                    case "baseball":
                        tempATChar.setFreeWithBand();
                        break;

                    case "hiphop":
                        tempATChar.setHaveToBuy();
                        break;
                    case "santa":
                        tempATChar.setPackage();
                        break;
                    case "rudolph":
                        tempATChar.setPackage();
                        break;

                }

                ///////////////////////////////////////TEMPORARY WORK////////////////////////////////////////////
                tempATChar.listviewResourceId = context.getResources().getIdentifier("caption" + charName.get(i), "drawable", context.getPackageName());
                tempATChar.detailviewResourceId = context.getResources().getIdentifier("character" + charName.get(i), "drawable", context.getPackageName());
                tempATChar.widgetResourceId = context.getResources().getIdentifier("widget" + charName.get(i), "drawable", context.getPackageName());
                characterList.add(tempATChar);

            }
        }
    }
}
