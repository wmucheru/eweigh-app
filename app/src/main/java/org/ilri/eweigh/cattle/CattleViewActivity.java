package org.ilri.eweigh.cattle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.tabs.TabLayout;
import com.marcinorlowski.fonty.Fonty;

import org.ilri.eweigh.R;
import org.ilri.eweigh.cattle.models.Breed;
import org.ilri.eweigh.cattle.models.Cattle;
import org.ilri.eweigh.cattle.models.ChemicalAgent;
import org.ilri.eweigh.cattle.models.Disease;
import org.ilri.eweigh.cattle.models.Dosage;
import org.ilri.eweigh.database.viewmodel.BreedsViewModel;
import org.ilri.eweigh.database.viewmodel.CattleViewModel;
import org.ilri.eweigh.database.viewmodel.DosagesViewModel;
import org.ilri.eweigh.database.viewmodel.SubmissionsViewModel;
import org.ilri.eweigh.feeds.Feed;
import org.ilri.eweigh.feeds.FeedsActivity;
import org.ilri.eweigh.hg_lw.LiveWeightActivity;
import org.ilri.eweigh.hg_lw.models.Submission;
import org.ilri.eweigh.network.APIService;
import org.ilri.eweigh.network.RequestParams;
import org.ilri.eweigh.ui.SectionsPagerAdapter;
import org.ilri.eweigh.utils.URL;
import org.ilri.eweigh.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CattleViewActivity extends AppCompatActivity {
    public static final String TAG = CattleViewActivity.class.getSimpleName();

    public static final int RC_LIVE_WEIGHT = 100;
    public static final int RC_FEED_RATION = 200;

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

        DetailsFragment f1 = new DetailsFragment(this, cattle);
        mSectionsPagerAdapter.addFragment(f1, "Details");

        FeedsFragment f2 = new FeedsFragment(this, cattle);
        mSectionsPagerAdapter.addFragment(f2, "Feeds");

        DosagesFragment f3 = new DosagesFragment(this, cattle);
        mSectionsPagerAdapter.addFragment(f3, "Dosages");

        MatingGuideFragment f4 = new MatingGuideFragment(this, cattle);
        mSectionsPagerAdapter.addFragment(f4, "Mating Guide");

        mViewPager.setAdapter(mSectionsPagerAdapter);

        int[] tabIcons = {
                R.drawable.ic_information_outline_white_24dp,
                R.drawable.ic_sprout_white_24dp,
                R.drawable.ic_pill_white_24dp,
                R.drawable.ic_gender_male_female_white_24dp
        };

        for(int i=0; i<tabLayout.getTabCount(); i++){
            if(tabLayout.getTabAt(i) != null){
                tabLayout.getTabAt(i).setIcon(tabIcons[i]);
            }
        }
    }

    /**
     *
     * DETAILS
     *
     * */
    public static class DetailsFragment extends Fragment {
        private Context context;

        LineChart lineChart;
        TextView txtLiveWeight;

        private Cattle cattle;

        CattleViewModel cvm;

        DetailsFragment(Context context, Cattle cattle) {
            this.context = context;
            this.cattle = cattle;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            cvm = new ViewModelProvider(this).get(CattleViewModel.class);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_details, container, false);

            TextView txtTag = view.findViewById(R.id.txt_tag);
            TextView txtBreed = view.findViewById(R.id.txt_breed);
            TextView txtGender = view.findViewById(R.id.txt_gender);
            TextView txtDateAdded = view.findViewById(R.id.txt_date_added);
            txtLiveWeight = view.findViewById(R.id.txt_live_weight);

            txtTag.setText(String.format("%s: %s", context.getString(R.string.tag), cattle.getTag()));
            txtBreed.setText(String.format("%s: %s", context.getString(R.string.breed),
                    cattle.getBreed().toUpperCase()));
            txtGender.setText(String.format("%s: %s", context.getString(R.string.gender), cattle.getGender().toUpperCase()));
            txtDateAdded.setText(String.format("%s: %s", context.getString(R.string.added_on),
                    Utils.formatDate(cattle.getCreatedOn())));

            txtLiveWeight.setText(Utils.formatNumber(cattle.getLiveWeight()));

            view.findViewById(R.id.btn_get_lw).setOnClickListener(v -> {

                /*
                 * Send a request to the LiveWeightActivity class to calculate the live weight:
                 *
                 * 1. Pass cattleList object
                 *
                 * This returns the final value to the onActivityResult function and we can
                 * update the UI and database values
                 *
                 * */
                Intent intent = new Intent(context, LiveWeightActivity.class);
                intent.putExtra(Cattle.CATTLE, cattle);

                startActivityForResult(intent, RC_LIVE_WEIGHT);
            });

            // Set up chart
            lineChart = view.findViewById(R.id.chart_lw_trend);
            lineChart.setBackgroundColor(Color.WHITE);

            lineChart.getDescription().setEnabled(false);
            lineChart.setTouchEnabled(true);

            // enable scaling and dragging
            lineChart.setDragEnabled(true);
            lineChart.setScaleEnabled(true);

            // if disabled, scaling can be done on x- and y-axis separately
            lineChart.setPinchZoom(false);

            lineChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);

            lineChart.setDrawGridBackground(false);
            lineChart.setMaxHighlightDistance(300);

            // Remove y-axis from the right of chart
            lineChart.getAxisRight().setEnabled(false);

            XAxis x = lineChart.getXAxis();
            x.setPosition(XAxis.XAxisPosition.BOTTOM);
            x.setGranularity(1);

            YAxis y = lineChart.getAxisLeft();
            y.setLabelCount(6, false);
            y.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
            y.setDrawGridLines(false);

            lineChart.animateXY(300, 300);
            lineChart.invalidate();

            SubmissionsViewModel svm = new ViewModelProvider(this).get(SubmissionsViewModel.class);

            svm.getCattleSubmissions(cattle.getId()).observe(getViewLifecycleOwner(),
                    submissions -> buildChart(submissions));

            Fonty.setFonts(container);

            return view;
        }

        private void buildChart(List<Submission> submissions){
            final ArrayList<Entry> values = new ArrayList<>();

            for (int i = 0; i < submissions.size(); i++) {
                Submission s = submissions.get(i);
                values.add(new Entry(i, (float) s.getLw()));
            }

            LineDataSet set1;

            if (lineChart.getData() != null && lineChart.getData().getDataSetCount() > 0) {
                set1 = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
                set1.setValues(values);

                lineChart.getData().notifyDataChanged();
                lineChart.notifyDataSetChanged();
            }
            else {

                set1 = new LineDataSet(values, "LiveWeight(KG) over time (days)");

                int chartColor = Color.rgb(104, 241, 175);

                set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                set1.setCubicIntensity(0.2f);
                set1.setDrawFilled(true);
                set1.setDrawCircles(false);
                set1.setLineWidth(1.8f);
                set1.setCircleRadius(4f);
                set1.setCircleColor(Color.WHITE);
                set1.setHighLightColor(chartColor);
                set1.setColor(chartColor);
                set1.setFillColor(chartColor);
                set1.setFillAlpha(100);
                set1.setDrawHorizontalHighlightIndicator(false);
                set1.setFillFormatter((dataSet,
                                       dataProvider) -> lineChart.getAxisLeft().getAxisMinimum());

                // create a data object with the data sets
                LineData data = new LineData(set1);

                data.setValueTextSize(9f);
                data.setDrawValues(false);

                // set data
                lineChart.setData(data);
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if(requestCode == RC_LIVE_WEIGHT){

                if(resultCode == Activity.RESULT_OK){
                    double liveWeight = data.getDoubleExtra(Cattle.LIVE_WEIGHT, 0);

                    if(liveWeight > 0){
                        Toast.makeText(context, "Live Weight is " + liveWeight, Toast.LENGTH_SHORT).show();

                        txtLiveWeight.setText(String.valueOf(liveWeight));

                        // Update cattleList object in database
                        cattle.setLiveWeight(liveWeight);

                        cvm.update(cattle);

                        // Update fragments
                        new FeedsFragment(context, cattle);
                        new DosagesFragment(context, cattle);
                        new MatingGuideFragment(context, cattle);
                    }
                    else{
                        Toast.makeText(context, "Could not fetch live weight", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(context, "Request cancelled", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     *
     * FEEDS
     *
     * */
    public static class FeedsFragment extends Fragment {
        private Context context;

        TextView txtRation;

        private Cattle cattle;

        FeedsFragment(Context context, Cattle cattle) {
            this.context = context;
            this.cattle = cattle;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_feeds, container, false);

            txtRation = view.findViewById(R.id.txt_feed_ration);

            view.findViewById(R.id.btn_get_feed_ration).setOnClickListener(v -> {

                /*
                 * Send a request to the FeedsActivity class to get :
                 *
                 * 1. Pass cattleList object
                 *
                 * This returns the final value to the onActivityResult function and we can
                 * update the UI and database values
                 *
                 * */
                Intent intent = new Intent(context, FeedsActivity.class);
                intent.putExtra(Cattle.CATTLE, cattle);

                startActivityForResult(intent, RC_FEED_RATION);
            });

            Fonty.setFonts(container);

            return view;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if(requestCode == RC_FEED_RATION){

                if(resultCode == Activity.RESULT_OK){
                    String ration = data.getStringExtra(Feed.RATION);

                    if(!ration.isEmpty()){
                        txtRation.setText(ration);
                        txtRation.setTextColor(Color.BLUE);
                    }
                    else{
                        Toast.makeText(context, "Could not get feed ration", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(context, "Request cancelled", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     *
     * DOSAGES
     *
     * */
    public static class DosagesFragment extends Fragment {

        private Context context;

        EditText editLiveWeight;
        Spinner spinnerDiseases, spinnerAgents;
        TextView txtResponse;

        DosagesViewModel dvm;

        Cattle cattle;

        DosagesFragment(Context context, Cattle cattle) {
            this.context = context;
            this.cattle = cattle;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            dvm = new ViewModelProvider(this).get(DosagesViewModel.class);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_dosages, container, false);

            txtResponse = view.findViewById(R.id.txt_response);

            editLiveWeight = view.findViewById(R.id.edit_live_weight);
            editLiveWeight.setText(Utils.formatNumber(cattle.getLiveWeight()));

            // Init disease spinner
            ArrayAdapter<Disease> adapter = new ArrayAdapter<>(context,
                    android.R.layout.simple_spinner_dropdown_item, dvm.getDiseases());

            spinnerDiseases = view.findViewById(R.id.spinner_diseases);
            spinnerDiseases.setAdapter(adapter);
            spinnerDiseases.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                    Disease disease = (Disease) parent.getItemAtPosition(position);

                    List<ChemicalAgent> agents = dvm.getChemicalAgents(disease.getId());

                    ArrayAdapter<ChemicalAgent> adapter = new ArrayAdapter<>(context,
                            android.R.layout.simple_spinner_dropdown_item, agents);
                    spinnerAgents.setAdapter(adapter);

                    spinnerAgents.setEnabled(true);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    spinnerAgents.setEnabled(false);
                }
            });

            spinnerAgents = view.findViewById(R.id.spinner_agents);
            spinnerAgents.setEnabled(false);

            view.findViewById(R.id.btn_get_dosage).setOnClickListener(v -> fetchDosage());

            Fonty.setFonts(container);

            return view;
        }

        private void fetchDosage(){
            AlertDialog alertDialog = Utils.getStandardDialog(context, "");

            Disease disease = (Disease) spinnerDiseases.getSelectedItem();
            ChemicalAgent agent = (ChemicalAgent) spinnerAgents.getSelectedItem();

            if(Double.parseDouble(editLiveWeight.getText().toString()) == 0){
                String s = "Get cattleList's live weight first";
                editLiveWeight.setError(s);

                alertDialog.setMessage(s);
                alertDialog.show();
            }
            else if(disease.getId() == 0){
                ((TextView) spinnerDiseases.getSelectedView()).setError("Select disease");
            }
            else if(agent.getId() == 0){
                ((TextView) spinnerAgents.getSelectedView()).setError("Select chemical agent");
            }
            else{
                final ProgressDialog progressDialog =
                        Utils.getProgressDialog(context, "Fetching dosage", true);

                progressDialog.show();
                txtResponse.setVisibility(View.GONE);

                RequestParams params = new RequestParams();

                params.put(Dosage.DISEASE, String.valueOf(disease.getId()));
                params.put(Dosage.CHEMICAL_AGENT, String.valueOf(agent.getId()));
                params.put(Cattle.LIVE_WEIGHT, String.valueOf(cattle.getLiveWeight()));

                new APIService(context).post(URL.GetDosage, params, response -> {
                    Log.d(TAG, response);
                    progressDialog.dismiss();

                    try {
                        JSONObject obj = new JSONObject(response);

                        if(obj.has(Dosage.DOSAGE)){
                            String dose = "" +
                                    "Disease: " + obj.getString(Dosage.DISEASE) + "\n" +
                                    "Agent: " + obj.getString(Dosage.CHEMICAL_AGENT) + "\n" +
                                    "Dosage: " + obj.getString(Dosage.DOSAGE) + "\n" +
                                    "Application Mode: " + obj.getString(Dosage.APPLICATION_MODE);

                            txtResponse.setText(dose);
                            txtResponse.setTextColor(Color.BLUE);
                            txtResponse.setVisibility(View.VISIBLE);

                            spinnerAgents.setSelection(0);
                            spinnerDiseases.setSelection(0);
                        }
                        else{
                            txtResponse.setVisibility(View.GONE);
                        }

                        if(obj.has("message")){
                            Toast.makeText(context, obj.getString("message"),
                                    Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    progressDialog.dismiss();
                    txtResponse.setVisibility(View.GONE);

                    Toast.makeText(context, "Error fetching dosage",
                            Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    /**
     *
     * MATING GUIDE
     *
     * */
    public static class MatingGuideFragment extends Fragment {
        TextView txtResponse;

        BreedsViewModel bvm;

        Cattle cattle;

        MatingGuideFragment(Context context, Cattle cattle) {
            this.cattle = cattle;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            bvm = new ViewModelProvider(this).get(BreedsViewModel.class);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_mating_guide, container, false);

            txtResponse = view.findViewById(R.id.txt_response);

            if(cattle.isFemale()){
                Breed breed = bvm.getBreed(cattle.getBreedId());

                /*
                 *
                 * A cow is to be mated at 65% of their mature weight
                 *
                 * */
                double referenceWeight = breed.getReferenceWeight();

                if(cattle.getLiveWeight() >= referenceWeight){
                    txtResponse.setText("Ready to mate");
                    txtResponse.setTextColor(Color.BLUE);
                }
                else{
                    String kiloGain = Utils.formatNumber(referenceWeight - cattle.getLiveWeight());

                    txtResponse.setText(Html.fromHtml(
                            String.format("Not ready to mate. Needs to gain <b>%sKG</b>", kiloGain)));
                    txtResponse.setTextColor(Color.RED);
                }
            }
            else{
                txtResponse.setText("No info for bulls");
                txtResponse.setTextColor(Color.RED);
            }

            Fonty.setFonts(container);

            return view;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cattle_view, menu);

        menu.findItem(R.id.action_delete).setOnMenuItemClickListener(
                menuItem -> {
                    showDeleteCattleDialog();
                    return true;
                });

        return true;
    }

    private void showDeleteCattleDialog(){
        AlertDialog alertDialog = Utils.getSimpleDialog(CattleViewActivity.this,
                String.format("Delete %s?",  cattle.getTag()));

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete", (dialogInterface, i) -> {

            RequestParams params = new RequestParams();
            params.put(Cattle.ID, String.valueOf(cattle.getId()));

            new APIService(CattleViewActivity.this).post(URL.DeleteCattle,
                    params, response -> {
                        Log.d(TAG, response);

                        try {
                            JSONObject obj = new JSONObject(response);

                            if(obj.has("success")){
                                Toast.makeText(CattleViewActivity.this,
                                        " " + obj.getString("message"),
                                        Toast.LENGTH_SHORT).show();

                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("deleted", true);
                                setResult(Activity.RESULT_OK, returnIntent);
                                finish();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, error -> Toast.makeText(CattleViewActivity.this,
                            "Could not process request", Toast.LENGTH_SHORT).show());
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                (dialogInterface, i) -> {
                    // Fail gracefully
                });
        alertDialog.show();
    }
}
