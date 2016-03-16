package co.favorie.at.character;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import co.favorie.at.ATCharacterManager;
import co.favorie.at.R;
import co.favorie.at.preference.CharacterSession;

/**
 * Created by bmac on 2015-07-30.
 */

public class ATCharacterAdapter extends RecyclerView.Adapter<ATCharacterAdapter.CharacterViewHolder> {


    private List<ATCharacter> itemsFromCharManager;
    private List<CharacterViewHolder> views = new ArrayList<CharacterViewHolder>();
    private OnItemClickListener mItemClickListener;
    private boolean hasToUpdate;
    private int updateIndex;
    private  CharacterSession mCharacterSession;

    public class CharacterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        CardView cv;
        ImageView indicator, character,band;

        public CharacterViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.at_cardview_character_cardview);
            indicator = (ImageView) cv.findViewById(R.id.at_cardview_character_indicator);
            character = (ImageView) cv.findViewById(R.id.at_cardview_character_imageview);
            band = (ImageView) cv.findViewById(R.id.at_cardview_character_band);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mItemClickListener.onItemClick(v, getPosition());
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }
    public void setOnItemClickListener(final OnItemClickListener pItemClickListener) {
        this.mItemClickListener = pItemClickListener;
    }
    public void clearIndicator() {
        for(int i=0; i<views.size(); i++){
            views.get(i).indicator.setVisibility(View.INVISIBLE);
        }
    }
    public void setIndicatorOnByIndex(int index) {
        clearIndicator();
        hasToUpdate = true;
        updateIndex = index;
    }
    public ATCharacterAdapter() {
        ATCharacterManager characterManager = ATCharacterManager.getInstance();
        itemsFromCharManager = characterManager.getCharacterList();
    }



    @Override
    public CharacterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.at_cardview_character, parent, false);
        mCharacterSession = new CharacterSession(parent.getContext().getApplicationContext());
        CharacterViewHolder vh = new CharacterViewHolder(v);
        views.add(vh);
        if(hasToUpdate && updateIndex < views.size()) {
                views.get(updateIndex).indicator.setVisibility(View.VISIBLE);
                hasToUpdate = false;
        }
        vh.character.setBackgroundColor(Color.parseColor("#00ffffff"));



        return vh;
    }

    @Override
    public void onBindViewHolder(CharacterViewHolder holder, int position) { // to specify the contents of each item of the RecyclerView

        // 실시간으로 View가 갱신되는 코드... notifyAdapter에 의해 갱신됨

        if(position != updateIndex) {
            holder.indicator.setVisibility(View.INVISIBLE);

        } else {
            if(mCharacterSession.isOwned(itemsFromCharManager.get(position).name)){
                holder.indicator.setVisibility(View.VISIBLE);
            }
        }
        holder.character.setImageResource(itemsFromCharManager.get(position).detailviewResourceId);

        // 소유하고 있지 않은 경우
        if(!mCharacterSession.isOwned(itemsFromCharManager.get(position).name)){

            // package 상품인가
            if(itemsFromCharManager.get(position).isPackage){
                holder.band.setVisibility(View.VISIBLE);
                holder.band.setImageResource(R.drawable.band_buyget);
            }else{
                // 유료
                if(itemsFromCharManager.get(position).haveTobuy){
                    holder.band.setVisibility(View.VISIBLE);
                    holder.band.setImageResource(R.drawable.band_havetobuy);
                // 무료
                }else{
                    holder.band.setVisibility(View.VISIBLE);
                    holder.band.setImageResource(R.drawable.band_free);
                }
            }

        }else{
            // 소유하고 있는 경우
            holder.band.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return itemsFromCharManager.size();
    }

}
