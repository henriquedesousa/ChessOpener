package org.ips.ests.chessopener.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.ips.ests.chessopener.Start;
import org.ips.ests.chessopener.model.Opening;

/**
 * Created by hsousa on 20/07/15.
 */
public abstract class BaseTab extends Fragment implements IUpdateableFragment {

    View view;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        /*
         * Base method to be performed by all TabFragments.
         * On device rotation savedInstanceState != null, so we re instantiate the view.
         */
        if (savedInstanceState != null) {
            Bundle args = getArguments();
            Opening opening = (Opening) args.getSerializable(Opening.OPENING_BUNDLE_KEY);
            update(opening);
        } else if (getArguments() != null && getArguments().getSerializable(Opening.OPENING_BUNDLE_KEY) != null) {
            Opening opening = (Opening) getArguments().getSerializable(Opening.OPENING_BUNDLE_KEY);
            update(opening);
        } else {
            update(Start.openings.get(0));
        }
        return view;
    }

}
