package com.scalosphere.labs.kquran.ui.fragment;

import android.app.Activity;
import android.support.v4.util.LongSparseArray;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.scalosphere.labs.kquran.R;
import com.scalosphere.labs.kquran.data.Constants;
import com.scalosphere.labs.kquran.data.QuranInfo;
import com.scalosphere.labs.kquran.database.BookmarksDBAdapter;
import com.scalosphere.labs.kquran.ui.QuranActivity;
import com.scalosphere.labs.kquran.ui.helpers.BookmarkHandler;
import com.scalosphere.labs.kquran.ui.helpers.QuranRow;

import java.util.ArrayList;
import java.util.List;

public class TagsFragment extends AbsMarkersFragment {
   
   private static final int[] VALID_SORT_OPTIONS = {R.id.sort_alphabetical, R.id.sort_date};

   public static TagsFragment newInstance(){
      return new TagsFragment();
   }
   
   @Override
   protected int getContextualMenuId() {
      return R.menu.tag_menu;
   }
   
   @Override
   protected int getEmptyListStringId() {
      return R.string.tags_list_empty;
   }
   
   @Override
   protected int[] getValidSortOptions() {
      return VALID_SORT_OPTIONS;
   }
   
   @Override
   protected String getSortPref() {
      return Constants.PREF_SORT_TAGS;
   }
   
   @Override
   protected boolean isValidSelection(QuranRow selected) {
      return selected.isBookmark() || (selected.isBookmarkHeader() && selected.tagId >= 0);
   }
   
   @Override
   protected boolean prepareActionMode(ActionMode mode, Menu menu, QuranRow[] selected) {
      MenuItem editItem = menu.findItem(R.id.cab_edit_tag);
      MenuItem removeItem = menu.findItem(R.id.cab_delete_tag);
      MenuItem tagItem = menu.findItem(R.id.cab_tag_bookmark);
      
      int headers = 0;
      int bookmarks = 0;

      for (QuranRow row : selected) {
         if (row.isBookmarkHeader()) {
            headers++;
         } else if (row.isBookmark()) {
            bookmarks++;
         }
      }

      boolean canEdit = headers == 1 && bookmarks == 0;
      boolean canRemove = (headers + bookmarks) > 0;
      boolean canTag = headers == 0 && bookmarks > 0;
      editItem.setVisible(canEdit);
      removeItem.setVisible(canRemove);
      tagItem.setVisible(canTag);
      return true;
   }
   
   @Override
   protected boolean actionItemClicked(ActionMode mode, int menuItemId,
         QuranActivity activity, QuranRow[] selected) {
      switch (menuItemId) {
      case R.id.cab_delete_tag:
         new RemoveBookmarkTask(true).execute(selected);
         return true;
      case R.id.cab_new_tag:
         activity.addTag();
         return true;
      case R.id.cab_edit_tag:
         if (selected.length == 1) {
            activity.editTag(selected[0].tagId, selected[0].text);
         }
         return true;
      case R.id.cab_tag_bookmark:
         long[] ids = new long[selected.length];
         for (int i = 0; i < selected.length; i++) {
            ids[i] = selected[i].bookmarkId;
         }
         activity.tagBookmarks(ids);
         return true;
      default:
         return false;
      }
   }
   
   @Override
   protected QuranRow[] getItems(){
      return getTags();
   }
   
   private QuranRow[] getTags(){
      BookmarksDBAdapter adapter = null;
      Activity activity = getActivity();
      if (activity != null && activity instanceof BookmarkHandler){
         adapter = ((BookmarkHandler) activity).getBookmarksAdapter();
      }

      if (adapter == null){ return null; }

      List<BookmarksDBAdapter.Tag> tags;
      switch (mCurrentSortCriteria) {
      case R.id.sort_date:
         tags = adapter.getTags(BookmarksDBAdapter.SORT_DATE_ADDED);
         break;
      case R.id.sort_alphabetical:
      default:
         tags = adapter.getTags(BookmarksDBAdapter.SORT_ALPHABETICAL);
         break;
      }
      List<BookmarksDBAdapter.Bookmark> bookmarks = adapter.getBookmarks(true);

      List<QuranRow> rows = new ArrayList<QuranRow>();
      
      List<BookmarksDBAdapter.Bookmark> unTagged = new ArrayList<BookmarksDBAdapter.Bookmark>();
      LongSparseArray<List<BookmarksDBAdapter.Bookmark>> tagMap =
          new LongSparseArray<List<BookmarksDBAdapter.Bookmark>>();
      
      for (BookmarksDBAdapter.Bookmark bookmark : bookmarks){
         List<BookmarksDBAdapter.Tag> bookmarkTags = bookmark.mTags;
         if (bookmarkTags == null) {
            unTagged.add(bookmark);
         } else {
            for (BookmarksDBAdapter.Tag tag : bookmarkTags) {
               List <BookmarksDBAdapter.Bookmark> tagBookmarkList = tagMap.get(tag.mId);
               if (tagBookmarkList == null) {
                  List<BookmarksDBAdapter.Bookmark> newList = new ArrayList<BookmarksDBAdapter.Bookmark>();
                  newList.add(bookmark);
                  tagMap.put(tag.mId, newList);
               } else {
                  tagBookmarkList.add(bookmark);
               }
            }
         }
      }
      
      for (BookmarksDBAdapter.Tag tag : tags) {
         List<BookmarksDBAdapter.Bookmark> tagBookmarkList = tagMap.get(tag.mId);

         // add the tag header
         QuranRow bookmarkHeader = new QuranRow(
                 tag.mName, null, QuranRow.BOOKMARK_HEADER, 0, 0, null);
         bookmarkHeader.tagId = tag.mId;
         
         rows.add(bookmarkHeader);

         // no bookmarks in this tag, so move on
         if (tagBookmarkList == null || tagBookmarkList.isEmpty()){ continue; }

         // and now the bookmarks
         for (BookmarksDBAdapter.Bookmark bookmark : tagBookmarkList) {
            QuranRow row = createRow(activity, tag.mId, bookmark);
            rows.add(row);
         }
      }
      
      if (unTagged.size() > 0) {
         QuranRow header = new QuranRow(
                 activity.getString(R.string.not_tagged), "",
                 QuranRow.BOOKMARK_HEADER, 0, 0, null);
         header.tagId = -1;
         
         rows.add(header);

         for (BookmarksDBAdapter.Bookmark bookmark : unTagged) {
            QuranRow row = createRow(activity, -1, bookmark);
            rows.add(row);
         }
      }
      
      return rows.toArray(new QuranRow[rows.size()]);
   }

   private QuranRow createRow(Activity activity,
                              long tagId, BookmarksDBAdapter.Bookmark bookmark) {
      QuranRow row;
      if (bookmark.mSura == null) {
         int sura = QuranInfo.getSuraNumberFromPage(bookmark.mPage);
         row = new QuranRow(
               QuranInfo.getSuraNameString(activity, bookmark.mPage),
               QuranInfo.getPageSubtitle(activity, bookmark.mPage),
               QuranRow.PAGE_BOOKMARK, sura, bookmark.mPage,
               R.drawable.bookmark_page);
      } else {
         row = new QuranRow(
               QuranInfo.getAyahString(bookmark.mSura,
                       bookmark.mAyah, getActivity()),
               QuranInfo.getPageSubtitle(activity, bookmark.mPage),
               QuranRow.AYAH_BOOKMARK, bookmark.mSura,
                 bookmark.mAyah, bookmark.mPage,
               R.drawable.bookmark_ayah);
      }
      row.tagId = tagId;
      row.bookmarkId = bookmark.mId;
      return row;
   }
}
