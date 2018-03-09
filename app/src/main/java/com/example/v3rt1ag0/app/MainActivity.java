package com.example.v3rt1ag0.app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import android.Manifest;
import android.accounts.AccountManager;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks
{

    EditText name, contatct, email, address, companyORcollegeName, Myname;
    Spinner whatDoYouDoSpinner, interestLevelSpinner, paymentMethod, Amount;
    LinearLayout amountLL, modeOfPayemntLL;
    ImageView tick, edit;
    GoogleAccountCredential mCredential;
    Button submit;
    Boolean SendEmptyFieldsForAmountandPaymentMode = false;
    CircularProgressView progressView;
    String formattedDate;


    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    String MYNAME = "unknown";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final SharedPreferences sharedPref = this.getSharedPreferences(
                "com.userDetails", Context.MODE_PRIVATE);
        name = findViewById(R.id.name);
        contatct = findViewById(R.id.number);
        email = findViewById(R.id.email);
        address = findViewById(R.id.address);
        companyORcollegeName = findViewById(R.id.officeOrcollegeName);
        whatDoYouDoSpinner = findViewById(R.id.whatDoYouDoSpinner);
        interestLevelSpinner = findViewById(R.id.InterestLevel);
        Myname = findViewById(R.id.MYname);
        progressView = findViewById(R.id.progress_view);
        submit = findViewById(R.id.Submit);
        progressView.setVisibility(View.GONE);
        edit = findViewById(R.id.edit);
        tick = findViewById(R.id.tick);
        paymentMethod = findViewById(R.id.PaymentMode);
        Amount = findViewById(R.id.Amount);
        modeOfPayemntLL = findViewById(R.id.ModeOfPaymentLL);
        amountLL = findViewById(R.id.AmountLL);

        edit.setVisibility(View.VISIBLE);
        tick.setVisibility(View.GONE);
        Myname.setEnabled(false);

        edit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                edit.setVisibility(View.GONE);
                tick.setVisibility(View.VISIBLE);
                Myname.setEnabled(true);
            }
        });

        tick.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                edit.setVisibility(View.VISIBLE);
                tick.setVisibility(View.GONE);
                Myname.setEnabled(false);
                SharedPreferences.Editor editor = sharedPref.edit();
                MYNAME = Myname.getText().toString();
                editor.putString("MyName", MYNAME);
                editor.commit();

            }
        });

        try
        {
            getSupportActionBar().setTitle("IYF, Whiltefield");
        } catch (Exception e)
        {
            e.printStackTrace();
        }


        interestLevelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                if (i == 3)
                {
                    modeOfPayemntLL.setVisibility(View.GONE);
                    amountLL.setVisibility(View.GONE);
                    SendEmptyFieldsForAmountandPaymentMode = true;
                } else
                {
                    modeOfPayemntLL.setVisibility(View.VISIBLE);
                    amountLL.setVisibility(View.VISIBLE);
                    SendEmptyFieldsForAmountandPaymentMode = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {

            }
        });

        whatDoYouDoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                if (i == 0)
                {
                    companyORcollegeName.setHint("Company Name");
                } else
                {
                    companyORcollegeName.setHint("College name and year");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {

            }
        });

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        formattedDate = df.format(c);


        MYNAME = sharedPref.getString("MyName", null);
        Myname.setText(MYNAME);

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        // https://www.googleapis.com/auth/spreadsheets
    }

    private void getResultsFromApi()
    {
        if (!isGooglePlayServicesAvailable())
        {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null)
        {
            chooseAccount();
        } else if (!isDeviceOnline())
        {
            Toast.makeText(this, "No network connection", Toast.LENGTH_SHORT).show();
        } else
        {

            new MakeRequestTask(mCredential).execute();
            //putDataToSheet(mCredential);
        }

    }


    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount()
    {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS))
        {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null)
            {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else
            {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else
        {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK)
                {
                    Toast.makeText(MainActivity.this,
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.", Toast.LENGTH_LONG).show();
                } else
                {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null)
                {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null)
                    {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        Log.d("SelectedAccountName", accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK)
                {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list)
    {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list)
    {
        // Do nothing.
        Log.d("executed", "permission denied");
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline()
    {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable()
    {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices()
    {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode))
        {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode)
    {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    public void sendDataToGoogleForm(View view)
    {
//putDataToSheet(mCredential);

        try
        {
            if (FieldsAreEmpty())
            {
                Toast.makeText(this, "One or more fields are empty", Toast.LENGTH_SHORT).show();
            } else
            {
                if (contatct.getText().toString().length() == 10)
                    getResultsFromApi();
                else
                    Toast.makeText(this, "Invalid contact number", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e)
        {

        }
    }

    private boolean FieldsAreEmpty()
    {
        ViewGroup group = findViewById(R.id.LLayout);
        for (int i = 0, count = group.getChildCount(); i < count; ++i)
        {
            View view = group.getChildAt(i);
            if (view instanceof EditText)
            {
                if (((EditText) view).getText().toString().length() <= 0)
                {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * An asynchronous task that handles the Google Sheets API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>>
    {
        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential)
        {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build();



            /*mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                   )*/

        }


        @Override
        protected List<String> doInBackground(Void... params)
        {
            try
            {
                // return getDataFromApi() ;
                return putDataToSheet();
            } catch (Exception e)
            {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of names and majors of students in a sample spreadsheet:
         * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         *
         * @return List of names and majors
         * @throws IOException
         */

        private List<String> putDataToSheet()
        {
        /* com.google.api.services.sheets.v4.Sheets mService = null;
        Exception mLastError = null;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.sheets.v4.Sheets.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("Google Sheets API Android Quickstart")
                .build();*/


            List<Object> row1 = new ArrayList<>();
            row1.add(name.getText().toString());
            row1.add(contatct.getText().toString());
            row1.add(email.getText().toString());
            row1.add(address.getText().toString());
            row1.add(whatDoYouDoSpinner.getSelectedItem().toString());
            row1.add(companyORcollegeName.getText().toString());
            row1.add(interestLevelSpinner.getSelectedItem().toString());
            row1.add(MYNAME);
            row1.add(formattedDate);

            if (SendEmptyFieldsForAmountandPaymentMode)
            {
                row1.add("-");
                row1.add("-");
            } else
            {
                row1.add(Amount.getSelectedItem().toString());
                row1.add(paymentMethod.getSelectedItem().toString());
            }


            List<List<Object>> values = Arrays.asList(
                    row1
            );
            ValueRange body = new ValueRange()
                    .setValues(values);
            AppendValuesResponse result =
                    null;
            try
            {
                result = this.mService.spreadsheets().values().append("12x7-slizJR_l_4WG_UXKy7dOqAFZmZ-i6G5bkr1bnf4", "Sheet1", body)
                        .setValueInputOption("USER_ENTERED")
                        .execute();
            } catch (UserRecoverableAuthIOException e)
            {
                e.printStackTrace();
                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            System.out.printf("%d cells appended.", result.getUpdates().getUpdatedCells());

            return (new ArrayList<String>());
        }

        private List<String> getDataFromApi() throws IOException
        {
            String spreadsheetId = "1HiTAUcmuaSH2tZ0WgcN_9oZoyXYkkv71hOTDYJDKWBI";
            String range = "Sheet1";
            List<String> results = new ArrayList<String>();
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            List<List<Object>> values = response.getValues();
            if (values != null)
            {
                results.add("Name, Major");
                for (List row : values)
                {
                    results.add(row.get(0) + ", " + row.get(4));
                }
            }
            return results;
        }


        @Override
        protected void onPreExecute()
        {
            // mOutputText.setText("");
            // mProgress.show();
            submit.setVisibility(View.GONE);
            progressView.setVisibility(View.VISIBLE);

        }

        @Override
        protected void onPostExecute(List<String> output)
        {
            // mProgress.hide();
            ViewGroup group = findViewById(R.id.LLayout);
            for (int i = 0, count = group.getChildCount(); i < count; ++i)
            {
                View view = group.getChildAt(i);
                if (view instanceof EditText)
                {
                    ((EditText) view).setText("");
                }
            }
            Toast.makeText(MainActivity.this, "Data inserted", Toast.LENGTH_SHORT).show();
            progressView.setVisibility(View.GONE);
            submit.setVisibility(View.VISIBLE);

        }

        @Override
        protected void onCancelled()
        {
//            mProgress.hide();
            if (mLastError != null)
            {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException)
                {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException)
                {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else
                {
                    Log.d("Tag", mLastError.getMessage());
                }
            } else
            {
                Toast.makeText(MainActivity.this, "Request cancelled", Toast.LENGTH_SHORT).show();

            }
        }
    }


}

