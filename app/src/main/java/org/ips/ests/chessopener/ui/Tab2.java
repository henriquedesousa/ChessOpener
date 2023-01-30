package org.ips.ests.chessopener.ui;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.ips.ests.chessopener.R;
import org.ips.ests.chessopener.biblioteca.LibraryActivity;
import org.ips.ests.chessopener.model.Opening;

/**
 * A fragment representing (...)
 * This fragment is either contained in a {@link LibraryActivity}
 * in two-pane mode (on tablets) or a {@link LibraryActivity}
 * on handsets.
 */
public class Tab2 extends BaseTab {

    TextView tvHistory;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.tab_2, container, false);
        tvHistory = (TextView) view.findViewById(R.id.tv_history);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public static Fragment newInstance(Opening opening) {
        Bundle args = new Bundle();
        args.putSerializable(Opening.OPENING_BUNDLE_KEY, opening);
        Fragment tab2 = new Tab2();
        tab2.setArguments(args);

        return tab2;
    }

    @Override
    public void update(Opening opening) {
        tvHistory.setText(Html.fromHtml(opening.getHistory()));
        tvHistory.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
