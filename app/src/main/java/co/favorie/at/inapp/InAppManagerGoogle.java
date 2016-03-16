package co.favorie.at.inapp;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONObject;

import java.util.ArrayList;

import co.favorie.at.ATCharacterManager;
import co.favorie.at.ATRecyclerViewAndWidgetModule;
import co.favorie.at.character.DetectATCharacter;
import co.favorie.at.preference.CharacterSession;

/**
 * Created by quki on 2016-01-25.
 */
public class InAppManagerGoogle {

    public Context context;
    public Typeface typeface;
    public IInAppBillingService mInAppService;
    public ATCharacterManager characterManager = ATCharacterManager.getInstance();
    public CharacterSession mCharacterPref;

    public InAppManagerGoogle(Context context,IInAppBillingService mInAppService,Typeface typeface){
        this.context = context;
        this.mInAppService = mInAppService;
        this.typeface = typeface;
        this.mCharacterPref = new CharacterSession(context);
    }

    /**
     * 내가 이전에 구매한 아이템인지 체크하고 확인이 되면 복구
     *
     * @param productId
     * @param index
     * @param module
     * @return selectedCharIndex == 1000 ? 캐릭터 선택X : 캐릭터 선택완료;
     */
    public int checkOwnedItemsAndRestore(String productId, int index, final ATRecyclerViewAndWidgetModule module) {

        int selectedCharIndex = 1000; // Flag 역할, 1000이면 캐릭터 선택이 확정된 것이 아니다.

        if (characterManager.getCharacterList().get(index).isPackage) {
            // 패키지 상품일 때 인앱을 위한 ID로 변환
            DetectATCharacter charHelper = new DetectATCharacter();
            productId = charHelper.reSettingProductId(characterManager.getCharacterList().get(index).name);
        }

        try {

            Bundle ownedItems = mInAppService.getPurchases(3, context.getPackageName(), "inapp", null);
            int response = ownedItems.getInt("RESPONSE_CODE");

            if (response == 0) {
                ArrayList<String> ownedSkus =
                        ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                if (ownedSkus != null) {
                    for (int i = 0; i < ownedSkus.size(); ++i) {
                        String ownedId = ownedSkus.get(i);

                        // 구매한 상품의 정보를 이용하여 처리
                        // 유저가 보유한 상품을 복구
                        if (productId.equals(ownedId)) {
                            selectedCharIndex = completeBuyItem(productId, module,index);
                            return selectedCharIndex;
                        }
                    }
                } else {
                    Log.e("==INAPP==","CANNOT FIND YOUR PURCHASE LIST" );
                }

            } else {

                // 결제가 막히는 예상치 못한 오류
                // ERROR Alert Dialog

                /*final TextView message = new TextView(context);
                final SpannableString stringWithLink = new SpannableString(context.getString(R.string.alert_fatal_error_message));
                Linkify.addLinks(stringWithLink, Linkify.WEB_URLS);
                message.setText(stringWithLink);
                message.setMovementMethod(LinkMovementMethod.getInstance());
                AlertDialog.Builder aBuilder = new AlertDialog.Builder(context);
                aBuilder.setTitle(context.getString(R.string.alert_fatal_error_title))
                        .setView(message)
                        .setCancelable(false)
                        .setPositiveButton(context.getString(R.string.alert_positive), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                module.setSelectedCharIndex(selectedCharIndex);
                                module.notifyAdapter();
                            }
                        });
                AlertDialog aDialog = aBuilder.create();
                aDialog.show();*/

                consumeAlreadyPurchaseItems(productId);
            }
        } catch (Exception e) {
            Log.e("==checkOwnedItems==", e.getMessage());
        }

        return selectedCharIndex;
    }

    /**
     * 결제 중,
     * 예상치 못한 오류가 발생했을 때, 이미 구매한 아이템인 경우에 소진을 시킨다.
     * 즉, 사용자가 결제는 했지만 아이템을 갖지 못하게 된 상황을 만든다.
     * 사용자의 피드백을 받고
     * Google wallet에서 일일이 환불을 해주자.
     *
     * @param productId
     */
    public void consumeAlreadyPurchaseItems(String productId) {
        try {
            Bundle ownedItems = mInAppService.getPurchases(3, context.getPackageName(), "inapp", null);
            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                String[] tokens = new String[purchaseDataList.size()];
                String tempToken = "";
                for (int i = 0; i < purchaseDataList.size(); ++i) {
                    String purchaseData = purchaseDataList.get(i);
                    JSONObject jo = new JSONObject(purchaseData);
                    tokens[i] = jo.getString("purchaseToken");

                    if (productId.equals(jo.getString("productId"))) {
                        tempToken = tokens[i];
                    }
                }
                final String thisToken = tempToken;
                //여기서 tokens를 모두 컨슘 해주기
                consume(thisToken);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 모두 소진 ( TEST 용으로 주로 사용함 )
     */
    public void consumeAll() {
        try {
            Bundle ownedItems = mInAppService.getPurchases(3, context.getPackageName(), "inapp", null);
            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                String[] tokens = new String[purchaseDataList.size()];
                for (int i = 0; i < purchaseDataList.size(); ++i) {
                    String purchaseData = purchaseDataList.get(i);
                    JSONObject jo = new JSONObject(purchaseData);
                    tokens[i] = jo.getString("purchaseToken");
                    consume(tokens[i]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 소진 ( 소진을 시키면 사용자가 결제이후에도 재구매를 해야한다. )
     *
     * @param token
     */
    public void consume(final String token) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int response = mInAppService.consumePurchase(3, context.getPackageName(), token);
                    //response가 0이면 성공.
                    if (response == 0) {
                        Log.v("==CONSUME==", "SUCCESS");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 구매 완료
     *
     * @param productId
     * @param module view 갱신을 위함
     * @param selectedCharIndex
     * @return selectedCharIndex 구매 완료 이후에 받아온 index 가 선택완료 index(selectedCharIndex) 로 초기화됨
     */
    public int completeBuyItem(String productId,final ATRecyclerViewAndWidgetModule module,int selectedCharIndex) {
        DetectATCharacter charHelper = new DetectATCharacter();
        // package라는 문자열이 들어있으면 묶음이므로, 묶음 캐릭터 둘다 열어줘야한다.
        if (productId.contains("package")) {
            mCharacterPref.setOwned(characterManager.getCharacterList().get(selectedCharIndex).name);
            mCharacterPref.setOwned(characterManager.getCharacterList().get(charHelper.reSettingIndexForPackage(selectedCharIndex)).name);
        } else {
            mCharacterPref.setOwned(characterManager.getCharacterList().get(selectedCharIndex).name);
        }

        // 구매 확정 이후 index 값을 전달... selecteCharIndex 를 초기화

        module.setSelectedCharIndex(selectedCharIndex);
        module.notifyAdapter();

        return selectedCharIndex;
    }
}
