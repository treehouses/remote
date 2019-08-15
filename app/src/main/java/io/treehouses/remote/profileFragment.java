package io.treehouses.remote;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.treehouses.remote.adapter.NetworkListAdapter;
import io.treehouses.remote.bases.BaseFragment;
import io.treehouses.remote.pojo.NetworkListItem;

public class profileFragment extends BaseFragment {

    private ExpandableListView expandableListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        expandableListView = view.findViewById(R.id.expandableListView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NetworkListAdapter adapter = new NetworkListAdapter(getContext(), NetworkListItem.getProfileList(), mChatService);
        adapter.setListener(listener);
        expandableListView.setAdapter(adapter);
        expandableListView.setGroupIndicator(null);
    }
}
