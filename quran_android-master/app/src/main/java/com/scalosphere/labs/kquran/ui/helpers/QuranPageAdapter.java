package com.scalosphere.labs.kquran.ui.helpers;


import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.scalosphere.labs.kquran.data.Constants;
import com.scalosphere.labs.kquran.data.QuranInfo;
import com.scalosphere.labs.kquran.ui.fragment.QuranPageFragment;
import com.scalosphere.labs.kquran.ui.fragment.TabletFragment;
import com.scalosphere.labs.kquran.ui.fragment.TranslationFragment;

public class QuranPageAdapter extends FragmentStatePagerAdapter {
   private static final String TAG = "QuranPageAdapter";

   private boolean mIsShowingTranslation = false;
   //private boolean mKannadaOnlyView =false;
   private boolean mIsDualPages = false;

   public QuranPageAdapter(FragmentManager fm, boolean dualPages,
                           boolean isShowingTranslation){
      super(fm);
      mIsDualPages = dualPages;
      mIsShowingTranslation = isShowingTranslation;
   }

   public void setKannadaMode(){
        //mKannadaOnlyView = true;
        notifyDataSetChanged();
    }

   public void setTranslationMode(){
        if (!mIsShowingTranslation){
            mIsShowingTranslation = true;
            notifyDataSetChanged();
        }
    }

   public void setQuranMode(){
      if (mIsShowingTranslation ){
         mIsShowingTranslation = false;
         notifyDataSetChanged();
      }
   }

   @Override
   public int getItemPosition(Object object){
      /* when the ViewPager gets a notifyDataSetChanged (or invalidated),
       * it goes through its set of saved views and runs this method on
       * each one to figure out whether or not it should remove the view
       * or not.  the default implementation returns POSITION_UNCHANGED,
       * which means that "this page is as is."
       *
       * as noted in http://stackoverflow.com/questions/7263291 in one
       * of the answers, if you're just updating your view (changing a
       * field's value, etc), this is highly inefficient (because you
       * recreate the view for nothing).
       *
       * in our case, however, this is the right thing to do since we
       * change the fragment completely when we notifyDataSetChanged.
       */
      return POSITION_NONE;
   }

	@Override
	public int getCount() {
    return mIsDualPages ? Constants.PAGES_LAST_DUAL : Constants.PAGES_LAST;
  }

	@Override
	public Fragment getItem(int position){
    int page = QuranInfo.getPageFromPos(position, mIsDualPages);
	  //android.util.Log.i(TAG, "getting page: " + page);
      //Log.i(TAG, " mIsShowingTranslation "+ mIsShowingTranslation + " mIsDualPages "+mIsDualPages);
      if (mIsDualPages){
          //Log.i(TAG, "inside tablet dual page view");
         return TabletFragment.newInstance(page,
                mIsShowingTranslation? TabletFragment.Mode.TRANSLATION :
                        TabletFragment.Mode.ARABIC);

      } else if (mIsShowingTranslation){
          //Log.i(TAG, "inside mIsShowingTranslation");
         return TranslationFragment.newInstance(page);
      } else {
        return QuranPageFragment.newInstance(page);
      }
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object){
      Fragment f = (Fragment)object;
      if (f instanceof QuranPageFragment){
         ((QuranPageFragment)f).cleanup();
      }
      else if (f instanceof TabletFragment){
         ((TabletFragment)f).cleanup();
      }
	   super.destroyItem(container, position, object);
	}

  public AyahTracker getFragmentIfExistsForPage(int page){
    if (page < Constants.PAGES_FIRST || Constants.PAGES_LAST < page) {
      return null;
    }
    int position = QuranInfo.getPosFromPage(page, mIsDualPages);
    Fragment fragment = getFragmentIfExists(position);
    return fragment != null && fragment instanceof AyahTracker ?
        (AyahTracker) fragment : null;
  }

}
