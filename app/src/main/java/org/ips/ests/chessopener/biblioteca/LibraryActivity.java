package org.ips.ests.chessopener.biblioteca;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;
import org.ips.ests.chessopener.R;
import org.ips.ests.chessopener.Start;
import org.ips.ests.chessopener.model.Opening;
import org.ips.ests.chessopener.ui.SlidingTabLayout;
import org.ips.ests.chessopener.ui.ViewPagerAdapter;
import org.ips.ests.chessopener.utils.OpeningUtils;

public class LibraryActivity extends AppCompatActivity implements NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Toolbar mToolbar;

    /**
     * Flag that indicates whether we're in Tablet or Phone mode
     */
    public static boolean isDrawerLocked = false;

    private ViewPager pager;
    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);

        /* The titles of the Tabs */
        CharSequence[] titles = new CharSequence[]{
                getString(R.string.tab_description),
                getString(R.string.tab_history),
                getString(R.string.tab_video)
        };

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.fragment_drawer);


        // Set up the drawer.
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), mToolbar);
        // populate the navigation drawer
        mNavigationDrawerFragment.setUserData("Chess Opener", "GNU GPL Licence v2",
                BitmapFactory.decodeResource(getResources(), R.drawable.avatar));

        // If we're on Tablet mode, disable the DrawerIndicator and locks NavDrawer
        LinearLayout layoutContent = (LinearLayout) findViewById(R.id.landscape_layout);
        if (layoutContent != null) {
            mNavigationDrawerFragment.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
            mNavigationDrawerFragment.getDrawerLayout().setScrimColor(Color.TRANSPARENT);
            isDrawerLocked = true;
            mNavigationDrawerFragment.openDrawer();
            mNavigationDrawerFragment.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
        }

        // If we have an Opening object coming from the bundle instantiate it, if not, use the
        // first opening from the array.
        Opening opening = Start.openings.get(0);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            try {
                opening = (Opening) bundle.getSerializable(Opening.OPENING_BUNDLE_KEY);
            } catch (Exception ignored) {
            }
        }

        // Changes the Toolbar's name to the Opening name
        getSupportActionBar().setTitle(opening.getName());

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles for the Tabs and Number Of Tabs.
        // Instantiates the adapter with the Opening object if not null, or the first Opening from the model
        adapter =  new ViewPagerAdapter(getSupportFragmentManager(), titles, titles.length, opening);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assigning the Sliding Tab Layout View
        /* The tabs of the ViewPager */
        SlidingTabLayout tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(position -> getResources().getColor(R.color.tabsScrollColor));

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);

        // Hack to make the NavDrawer select the opening coming from the Intent
        mNavigationDrawerFragment.onNavigationDrawerItemSelected(OpeningUtils.findPositionFromString(opening.getName(), Start.openings));

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        //Toast.makeText(this, "Menu item selected -> " + position, Toast.LENGTH_SHORT).show();

        Opening opening = Start.openings.get(position);

        if (adapter != null) {
            pager.setAdapter(adapter);
            adapter.update(opening);
        }
        if (mToolbar != null && opening != null) {
            mToolbar.setTitle(opening.getName());
        }
    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen() && !isDrawerLocked)
            mNavigationDrawerFragment.closeDrawer();
        else
            super.onBackPressed();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.biblioteca, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this, getString(R.string.not_implemented), Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}