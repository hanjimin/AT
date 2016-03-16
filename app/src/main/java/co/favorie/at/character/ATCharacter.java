package co.favorie.at.character;

/**
 * Created by bmac on 2015-07-29.
 * Character들의 속성 정의
 * haveToBuy = 결제해야 되는 캐릭터
 * isPackage = 결제해야 되는 캐릭터에 패키지로 묶여있는 캐릭터
 * isFreeWithBand = 무료 캐릭터
 */
public class ATCharacter {
    public int listviewResourceId, detailviewResourceId, widgetResourceId;
    public boolean haveTobuy,isPackage, isFreeWithBand;
    public String name;

    public ATCharacter(String charName) {
        name = charName;
    }

    public void setPackage(){
        isPackage = true;
        haveTobuy = true;
    }

    public void setFree() {
        isPackage = false;
        haveTobuy = false;
    }
    public void setFreeWithBand() {
        isFreeWithBand = true;
        isPackage = false;
        haveTobuy = false;
    }
    public void setHaveToBuy() {
        isPackage = false;
        haveTobuy = true;
    }
}
