package org.ips.ests.chessopener.ui;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.ips.ests.chessopener.model.Opening;

/**
 * Created by Edwin on 15/02/2015.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private Opening opening;
    CharSequence[] Titles; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created


    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ViewPagerAdapter(FragmentManager fm, CharSequence[] mTitles, int mNumbOfTabsumb, Opening opening) {
        super(fm);

        this.opening = opening;
        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;
    }

    //call this method to update fragments in ViewPager dynamically
    public void update(Opening opening) {
        this.opening = opening;
        notifyDataSetChanged();
    }

    //This method return the fragment for the every position in the View Pager
    @NonNull
    @Override
    public Fragment getItem(int position) {

        Fragment tab;
        switch (position) {

            default:
            case 0:
                tab = Tab1.newInstance(opening);
                break;
            case 1:
                tab = Tab2.newInstance(opening);
                break;
            case 2:
                tab = Tab3.newInstance(opening);
                break;
        }

        return tab;
    }

    @Override
    public int getItemPosition( @NonNull Object object) {
        if (object instanceof IUpdateableFragment) {
            ((IUpdateableFragment) object).update(opening);
        }
        //don't return POSITION_NONE, avoid fragment recreation.
        return super.getItemPosition(object);
    }

    // This method return the titles for the Tabs in the Tab Strip

    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }

    // This method return the Number of tabs for the tabs Strip

    @Override
    public int getCount() {
        return NumbOfTabs;
    }
}