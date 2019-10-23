package org.ilri.eweigh.cattle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.marcinorlowski.fonty.Fonty;

import org.ilri.eweigh.R;
import org.ilri.eweigh.ui.SectionsPagerAdapter;
import org.ilri.eweigh.utils.Utils;

public class CattleViewActivity extends AppCompatActivity {
    public static final String TAG = CattleViewActivity.class.getSimpleName();

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    TabLayout tabLayout;

    AlertDialog alertDialog;

    Cattle cattle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_cattle);

        Bundle bundle = getIntent().getExtras();

        if(bundle != null){
            cattle = (Cattle) bundle.get(Cattle.CATTLE);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(String.format("View %s", cattle.getTag()));

        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        alertDialog = Utils.getSimpleDialog(this, "");

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = findViewById(R.id.view_pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = findViewById(R.id.tabs_cattle);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        DetailsFragment f1 = new DetailsFragment(this);
        mSectionsPagerAdapter.addFragment(f1, "Details");

        DosageFragment f2 = new DosageFragment(this);
        mSectionsPagerAdapter.addFragment(f2, "Dosage");

        MatingGuideFragment f3 = new MatingGuideFragment(this);
        mSectionsPagerAdapter.addFragment(f3, "Mating Guide");

        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    /**
     *
     * DETAILS
     *
     * */
    public static class DetailsFragment extends Fragment {

        ProgressBar progressBar;
        TextView blankState;

        private Context context;

        DetailsFragment(Context context) {
            this.context = context;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_cattle_details, container, false);

            progressBar = view.findViewById(R.id.progress_bar);

            blankState = view.findViewById(R.id.txt_blank_state);
            blankState.setText("No reports added");

            fetchInfo();

            Fonty.setFonts(container);

            return view;
        }

        private void fetchInfo(){

        }
    }

    /**
     *
     * DOSAGE
     *
     * */
    public static class DosageFragment extends Fragment {

        ProgressBar progressBar;
        TextView blankState;

        private Context context;

        DosageFragment(Context context) {
            this.context = context;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_dosage, container, false);

            progressBar = view.findViewById(R.id.progress_bar);

            blankState = view.findViewById(R.id.txt_blank_state);
            blankState.setText("No dosages added");

            fetchDosage();

            Fonty.setFonts(container);

            return view;
        }

        private void fetchDosage(){

        }
    }

    /**
     *
     * MATING GUIDE
     *
     * */
    public static class MatingGuideFragment extends Fragment {

        ProgressBar progressBar;
        TextView blankState;

        private Context context;

        MatingGuideFragment(Context context) {
            this.context = context;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_mating_guide, container, false);

            progressBar = view.findViewById(R.id.progress_bar);

            blankState = view.findViewById(R.id.txt_blank_state);
            blankState.setText("No mating guide available");

            fetchInfo();

            Fonty.setFonts(container);

            return view;
        }

        private void fetchInfo(){

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
