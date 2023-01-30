package org.ips.ests.chessopener.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.ips.ests.chessopener.R;
import org.ips.ests.chessopener.biblioteca.LibraryActivity;
import org.ips.ests.chessopener.model.Opening;

/**
 * A fragment representing the Introduction of a Opening.
 * It is comprised of an image with the general Opening and a brief description.
 * <p/>
 * It is either contained in a {@link LibraryActivity}
 * in two-pane mode (on tablets) or a {@link LibraryActivity}
 * on handsets.
 */
public class Tab1 extends BaseTab {

    TextView tvDescription;
    ImageView ivDescription;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.tab_1, container, false);
        tvDescription = (TextView) view.findViewById(R.id.tv_description);
        ivDescription = (ImageView) view.findViewById(R.id.iv_description);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public static Fragment newInstance(Opening opening) {
        Bundle args = new Bundle();
        args.putSerializable(Opening.OPENING_BUNDLE_KEY, opening);
        Fragment tab1 = new Tab1();
        tab1.setArguments(args);

        return tab1;
    }

    @Override
    public void update(Opening opening) {
        tvDescription.setText(Html.fromHtml(opening.getDescription()));
        tvDescription.setMovementMethod(LinkMovementMethod.getInstance());

        Glide.with(this.requireContext())
                .load(opening.getImageUrl())
                .fitCenter()
                .placeholder(R.drawable.failed_to_load)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(ivDescription);
    }
}
