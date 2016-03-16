package co.favorie.at.character;

/**
 * Created by quki on 2015-11-29.
 */
public class DetectATCharacter {

    public String reSettingProductId(String tempProductId) {

        if (tempProductId.equals("military") || tempProductId.equals("gomsin")) {
            return "packagemilitary";
        } else if (tempProductId.equals("beach") || tempProductId.equals("marinegirl")) {
            return "packagesummer";
        } else if (tempProductId.equals("thief") || tempProductId.equals("police")) {
            return "packagepolice";
        } else if (tempProductId.equals("watson") || tempProductId.equals("sherlock")) {
            return "packagesherlock";
        } else if (tempProductId.equals("santa") || tempProductId.equals("rudolph")) {
            return "packagechristmas";
        }
        return tempProductId;
    }

    public int reSettingIndexForPackage(int tempIndex) {

        int resettingIndex = tempIndex;

        if (tempIndex == 7) {
            resettingIndex = 8;
        } else if (tempIndex == 8) {
            resettingIndex = 7;
        } else if (tempIndex == 14) {
            resettingIndex = 15;
        } else if (tempIndex == 15) {
            resettingIndex = 14;
        } else if (tempIndex == 17) {
            resettingIndex = 18;
        } else if (tempIndex == 18) {
            resettingIndex = 17;
        } else if (tempIndex == 20) {
            resettingIndex = 21;
        } else if (tempIndex == 21) {
            resettingIndex = 20;
        } else if (tempIndex == 26) {
            resettingIndex = 27;
        } else if (tempIndex == 27) {
            resettingIndex = 26;
        }
        return resettingIndex;
    }
}
