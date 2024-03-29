package org.ilri.eweigh.cattle;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.marcinorlowski.fonty.Fonty;

import org.ilri.eweigh.R;
import org.ilri.eweigh.accounts.AccountUtils;
import org.ilri.eweigh.accounts.models.User;
import org.ilri.eweigh.cattle.models.Breed;
import org.ilri.eweigh.cattle.models.Cattle;
import org.ilri.eweigh.database.viewmodel.BreedsViewModel;
import org.ilri.eweigh.database.viewmodel.CattleViewModel;
import org.ilri.eweigh.network.APIService;
import org.ilri.eweigh.network.RequestParams;
import org.ilri.eweigh.utils.URL;
import org.ilri.eweigh.utils.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CattleActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    public static final String TAG = CattleActivity.class.getSimpleName();

    public static final int RC_CATTLE_LIST = 50;

    CattleAdapter adapter;
    ListView listView;
    List<Cattle> cattleList = new ArrayList<>();

    ProgressBar progressBar;
    TextView blankState;

    APIService apiService;
    User user;

    CattleViewModel cvm;

    String gender = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cattle);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Cattle");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        apiService = new APIService(this);
        user = new AccountUtils(this).getUserDetails();

        cvm = new ViewModelProvider(this).get(CattleViewModel.class);

        progressBar = findViewById(R.id.progress_bar);
        blankState = findViewById(R.id.txt_blank_state);

        listView = findViewById(R.id.list_view_cattle);

        FloatingActionButton fab = findViewById(R.id.fab_cattle);
        fab.setOnClickListener(v -> showRegisterCattleModal());

        cvm.getAll().observe(this, this::renderList);

        getCattle();

        Fonty.setFonts(this);
    }

    private void renderList(List<Cattle> cattle){
        adapter = new CattleAdapter(this, cattle);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        adapter.notifyDataSetChanged();
    }

    private void getCattle(){
        progressBar.setVisibility(View.VISIBLE);

        RequestParams params = new RequestParams();
        params.put(User.ID, String.valueOf(user.getUserId()));

        final int recordCount = cvm.getCount();

        apiService.post(URL.Cattle, params, response -> {
            Log.d(TAG, "Response: " + response);
            progressBar.setVisibility(View.GONE);

            try {
                JSONArray arr = new JSONArray(response);

                cattleList = new ArrayList<>();

                if(arr.length() > 0){
                    blankState.setVisibility(View.GONE);

                    cvm.deleteAll();

                    for(int i=0; i<arr.length(); i++){
                        Cattle c = new Cattle(arr.getJSONObject(i));
                        cattleList.add(c);

                        // Store cattleList locally
                        cvm.insert(c);
                    }
                }
                else if(arr.length() == 0 && recordCount == 0){
                    blankState.setVisibility(View.VISIBLE);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Log.d(TAG, "Error: " + error.getMessage());
            progressBar.setVisibility(View.GONE);

            if(recordCount == 0){
                blankState.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showRegisterCattleModal(){
        BreedsViewModel bvm = new ViewModelProvider(this).get(BreedsViewModel.class);

        View view = LayoutInflater
                .from(this)
                .inflate(R.layout.fragment_add_cattle, null);

        final EditText inputTag = view.findViewById(R.id.input_tag);
        final Spinner spinnerBreeds = view.findViewById(R.id.spinner_breeds);
        final RadioButton radioGenderFemale = view.findViewById(R.id.radio_gender_female);

        // Tags should be in capital
        inputTag.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

        // Populate breeds
        bvm.getAll().observe(this, breeds -> {
            ArrayList<Breed> breedsList = new ArrayList<>();

            breedsList.add(new Breed(0, "Select Breed"));
            breedsList.addAll(breeds);

            ArrayAdapter<Breed> adapter = new ArrayAdapter<>(CattleActivity.this,
                    android.R.layout.simple_spinner_dropdown_item, breedsList);
            spinnerBreeds.setAdapter(adapter);
        });

        final ProgressDialog progressDialog =
                Utils.getProgressDialog(this, "Submitting...", false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Register Cattle")
                .setView(view)
                .setCancelable(true);

        final AlertDialog dialog = builder.create();
        dialog.show();

        Button btnAddCattle = view.findViewById(R.id.btn_add_cattle);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        btnAddCattle.setOnClickListener(v -> {
            String tag = inputTag.getText().toString();

            Breed b = (Breed) spinnerBreeds.getSelectedItem();
            int breed = b.getId();

            if(TextUtils.isEmpty(tag)){
                inputTag.setError(getString(R.string.required));
            }
            else if(breed == 0){
                ((TextView) spinnerBreeds.getSelectedView())
                        .setError(getString(R.string.select_breed));
            }
            else if(TextUtils.isEmpty(gender)){
                radioGenderFemale.setError(getString(R.string.select_gender));
            }
            else{
                progressDialog.show();

                RequestParams params = new RequestParams();
                params.put(User.ID, String.valueOf(user.getUserId()));
                params.put(Cattle.TAG, tag);
                params.put(Cattle.GENDER, gender);
                params.put(Cattle.BREED, String.valueOf(breed));

                apiService.post(
                        URL.RegisterCattle,
                        params,
                        response -> {
                            Log.d(TAG, "Response: " + response);
                            progressDialog.dismiss();

                            try {
                                JSONObject obj = new JSONObject(response);

                                if(obj.has(Cattle.CATTLE)){
                                    dialog.dismiss();
                                    blankState.setVisibility(View.GONE);

                                    inputTag.setText("");
                                    spinnerBreeds.setSelection(0);

                                    Cattle c = new Cattle(obj.getJSONObject(Cattle.CATTLE));
                                    cattleList.add(0, c);

                                    cvm.insert(c);

                                    adapter.notifyDataSetChanged();
                                }

                                if(obj.has("message")){
                                    Toast.makeText(
                                            CattleActivity.this,
                                            obj.getString("message"),
                                            Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }, error -> {
                            Log.d(TAG, "Error: " + error.getMessage());
                            progressDialog.dismiss();

                            Toast.makeText(CattleActivity.this,
                                    "Could not add cattleList", Toast.LENGTH_LONG).show();
                        });
            }
        });

        btnCancel.setOnClickListener(view1 -> dialog.dismiss());
    }

    public void onGenderRadioChanged(View v) {
        switch (v.getId()) {

            case R.id.radio_gender_male:
                gender = Cattle.GENDER_MALE;
                break;

            case R.id.radio_gender_female:
                gender = Cattle.GENDER_FEMALE;
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){

            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cattle cattle = (Cattle) parent.getItemAtPosition(position);

        Intent intent = new Intent(this, CattleViewActivity.class);
        intent.putExtra(Cattle.CATTLE, cattle);

        startActivityForResult(intent, RC_CATTLE_LIST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_CATTLE_LIST) {

            if (resultCode == Activity.RESULT_OK &&
                    data.getBooleanExtra("deleted", false)) {
                getCattle();
            }
        }
    }
}
