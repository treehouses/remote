package io.treehouses.remote.bases;

import android.content.Context;

import androidx.fragment.app.Fragment;
import io.treehouses.remote.callback.HomeInteractListener;

public class BaseFragment  extends Fragment {
  public   HomeInteractListener listener;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof HomeInteractListener)
            listener = (HomeInteractListener) context;
        else
            throw new RuntimeException("Implement interface first");
    }
}
