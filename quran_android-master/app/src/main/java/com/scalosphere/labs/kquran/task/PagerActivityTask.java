package com.scalosphere.labs.kquran.task;

import com.scalosphere.labs.kquran.ui.PagerActivity;

import java.lang.ref.WeakReference;

public abstract class PagerActivityTask<Params, Progress, Result>
    extends AsyncTask<Params, Progress, Result> {

  protected WeakReference<PagerActivity> mActivity;

  public PagerActivityTask(PagerActivity activity) {
    mActivity = new WeakReference<PagerActivity>(activity);
  }

  public PagerActivity getActivity() {
    return mActivity == null ? null : mActivity.get();
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    PagerActivity activity = getActivity();
    if (activity != null) activity.registerTask(this);
  }

  @Override
  protected void onPostExecute(Result result) {
    super.onPostExecute(result);
    PagerActivity activity = getActivity();
    if (activity != null) activity.unregisterTask(this);
  }

  @Override
  protected void onCancelled() {
    super.onCancelled();
    PagerActivity activity = getActivity();
    if (activity != null) activity.unregisterTask(this);
  }

}
