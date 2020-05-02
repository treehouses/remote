package io.treehouses.remote.bases;

import android.content.Context;
import android.view.WindowManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import io.treehouses.remote.callback.HomeInteractListener;

public class BaseBottomSheetDialog extends BottomSheetDialogFragment {
    protected Context context;
    protected HomeInteractListener listener;

    @Override
    public void onAttach(Context c) {
        super.onAttach(c);
        try {
            listener = (HomeInteractListener) c;
            context = c;
        } catch (ClassCastException e) {
            throw new ClassCastException(c.toString() + " must implement HomeInteractListener");
        }
    }
}
