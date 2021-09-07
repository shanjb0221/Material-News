package com.example.myapplication.fragment;

import static com.example.myapplication.R.id.menu_about;
import static com.example.myapplication.R.id.menu_editChannel;
import static com.example.myapplication.R.id.menu_history;
import static com.example.myapplication.R.id.menu_refresh;
import static com.example.myapplication.R.id.menu_search;
import static com.example.myapplication.R.id.menu_star;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.R;
import com.example.myapplication.adapter.DemoCollectionPagerAdapter;
import com.example.myapplication.databinding.FragmentEditChannelBinding;
import com.example.myapplication.databinding.FragmentMainBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainFragment extends Fragment {

    String TAG = "MainFragment";

    private FragmentMainBinding B;
    private FragmentEditChannelBinding eB;
    private NavController nav;
    private BottomSheetDialog bottomSheetDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (B == null) {
            B = FragmentMainBinding.inflate(inflater, container, false);
            nav = NavHostFragment.findNavController(this);
            eB = FragmentEditChannelBinding.inflate(inflater, container, false);
            setupTopAppBar();
            setupNavigationView();
            setupViewPager();
            bottomSheetDialog = new BottomSheetDialog(getContext());
            bottomSheetDialog.setContentView(eB.getRoot());
            bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                }
            });
        }
        return B.getRoot();
    }

    private final int MODE_BUTTON = 0;
    private final int MODE_NAVIGATION_VIEW = 1;
    private final int MODE_TOP_APP_BAR = 2;

    private void setupViewPager() {
        B.editChannelButton.setOnClickListener(view -> navigateTo(menu_editChannel, MODE_BUTTON));
        B.viewPager.setAdapter(new DemoCollectionPagerAdapter(this));
        new TabLayoutMediator(B.tabLayout, B.viewPager, (tab, position) -> tab.setText("item #" + position)).attach();
    }

    private void setupNavigationView() {
        B.navigationView.setNavigationItemSelectedListener(item -> navigateTo(item.getItemId(), MODE_NAVIGATION_VIEW));
    }

    private boolean navigateTo(int destination, int mode) {
        if (mode == MODE_NAVIGATION_VIEW) {
            B.drawerLayout.closeDrawer(GravityCompat.START);
        }
        if (destination == menu_search) {
            nav.navigate(MainFragmentDirections.actionMainFragmentToSearchFragment());
            return true;
        }
        if (destination == menu_editChannel) {
            bottomSheetDialog.show();
            return true;
        }
        if (destination == menu_star) {
            nav.navigate(MainFragmentDirections.actionMainFragmentToStarFragment());
            return true;
        }
        if (destination == menu_history) {
            nav.navigate(MainFragmentDirections.actionMainFragmentToHistoryFragment());
            return true;
        }
        if (destination == menu_about) {
            Snackbar.make(B.coordinatorLayout, R.string.style, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.link, view -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://material.io/"));
                        requireContext().startActivity(intent);
                    }).show();
            return true;
        }
        if (destination == menu_refresh) {
            NewsListFragment cur = (NewsListFragment) getChildFragmentManager().findFragmentByTag("f" + B.viewPager.getCurrentItem());
            cur.getBinding().swipeRefreshLayout.setRefreshing(true);
            cur.refresh();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        B = null;
        nav = null;
    }

    private void setupTopAppBar() {
        B.topAppBar.setNavigationOnClickListener(view -> B.drawerLayout.openDrawer(GravityCompat.START));

        B.topAppBar.setOnMenuItemClickListener(item -> navigateTo(item.getItemId(), MODE_TOP_APP_BAR));

        MenuItem search = B.topAppBar.getMenu().findItem(menu_search);
        SearchView searchView = (SearchView) search.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Snackbar.make(B.coordinatorLayout, "Searching " + query, BaseTransientBottomBar.LENGTH_LONG).show();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

}