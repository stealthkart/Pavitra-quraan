package com.scalosphere.labs.kquran.common;

import java.io.Serializable;

public class QuranNotes implements Serializable {

   private static final long serialVersionUID = 3L;

   private int mSoorah = 0;
   private int mAayah = 0;

   // notes text
   private String mText = null;

    @Override
    public String toString() {
        return mText ;
    }

    public QuranNotes(int soorah, int aayah){
      mSoorah = soorah;
       mAayah = aayah;
   }

   public int getSoorah(){ return mSoorah; }
   public int getAayah(){ return mAayah; }
   public String getText(){ return mText; }
   public void setText(String text){ mText = text; }

}
