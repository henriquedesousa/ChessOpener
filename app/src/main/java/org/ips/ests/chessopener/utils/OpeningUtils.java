package org.ips.ests.chessopener.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import org.ips.ests.chessopener.R;
import org.ips.ests.chessopener.model.Opening;

import java.util.ArrayList;

/**
 * Created by hsousa on 18/07/15.
 */
public class OpeningUtils {

    /**
     * Build our array of Openings from the XML files.
     *
     * @return an ArrayList of Opening object, containing our entire openings database.
     */
    public static ArrayList<Opening> populateFromArray(Context context) {

        ArrayList<Opening> openings = new ArrayList<>();

        String[] description = context.getResources().getStringArray(R.array.openings_description);
        String[] histories = context.getResources().getStringArray(R.array.openings_history);
        String[] names = context.getResources().getStringArray(R.array.openings_names);
        String[] youtubeIds = context.getResources().getStringArray(R.array.openings_youtube_id);
        String[] imageUrls = context.getResources().getStringArray(R.array.openings_image_url);

        if(description.length != histories.length
                && histories.length != names.length
                && names.length != youtubeIds.length
                && youtubeIds.length != imageUrls.length) {
            throw new RuntimeException("arrays lenght dont match");
        }


        for (int i = 0; i < histories.length; i++) {
            openings.add(new Opening(i, names[i], description[i], youtubeIds[i], histories[i], imageUrls[i]));
        }

        return openings;
    }

    /**
     *
     * @param str opening's name
     * @param openings list of openings
     * @return Opening
     */
    public static Opening findOpeningFromString(@NonNull String str, @NonNull ArrayList<Opening> openings) {

        for (Opening opening : openings) {
            if (str.toLowerCase().contains(opening.getName().toLowerCase())) {
                return opening;
            }
        }

        return null;
    }

    public static int findPositionFromString(@NonNull String str, @NonNull ArrayList<Opening> openings) {

        for (Opening opening : openings) {
            if (str.toLowerCase().contains(opening.getName().toLowerCase())) {
                return openings.indexOf(opening);
            }
        }

        return -1;
    }

}