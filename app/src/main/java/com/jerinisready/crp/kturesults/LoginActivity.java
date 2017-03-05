package com.jerinisready.crp.kturesults;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.content.ContentValues.TAG;


public class LoginActivity extends AppCompatActivity{

    private static final String CEK_LOGIN = "http://192.168.1.80/json/login.php";

    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mRegisterView;
    private EditText mPasswordView;
    private View mProgressView;
    private Spinner mCollegeName;
    private View mLoginFormView;
    private CheckBox tnc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mRegisterView = (EditText) findViewById(R.id.register_no);
        mPasswordView = (EditText) findViewById(R.id.login_password);
        mCollegeName = (Spinner) findViewById(R.id.spinner2);
        tnc = (CheckBox) findViewById(R.id.TnC);

        Button SignInButton = (Button) findViewById(R.id.login_button);
        SignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tnc.isChecked()!=true) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Terms AND Conditions Not Accepted!", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else if (mRegisterView.getText().toString() == null) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Registration Number Required", Toast.LENGTH_SHORT);
                    toast.show();
                    mRegisterView.setError("Registration Number Required");
                } else if (mPasswordView.getText().toString() == null) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Password From Institution Required", Toast.LENGTH_SHORT);
                    toast.show();
                    mPasswordView.setError("Password From Institution Required");
                }
                else if (mCollegeName.getSelectedItem().toString() == null ){
                    Toast toast = Toast.makeText(getApplicationContext(), "Select A College From The List", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo == null || !networkInfo.isConnected()) {
                        Toast toast = Toast.makeText(getApplicationContext(), "No Network Found! Please Connect To A Network!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    else{
                                // LOGIN // REGISTER MOBILE APP.
                        attemptLogin();
                        String s[]=get_password(mRegisterView.getText().toString()).split(":");
                        if(s[0] == mPasswordView.getText().toString() || s[1] == mPasswordView.getText().toString()){
                            set_database(true);
                        }
                    }

                }
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void attemptLogin() {

        // Reset errors.
        mRegisterView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String regrn_no = mRegisterView.getText().toString();
        String password = mPasswordView.getText().toString();


        // Check for a valid password, if the user entered one.
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask();
            mAuthTask.execute((Void) null);
        }
    private void set_database(boolean isloggedin){
        ___Database_handler __DB = new ___Database_handler(LoginActivity.this);
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast toast = Toast.makeText(getApplicationContext(), "No Network Found! Please Connect To A Network!", Toast.LENGTH_SHORT);
            toast.show();
        }
        else{
            // CONNECTION AVAILABLE
        }

    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)

    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    public String get_password(String regn_no) {
        URL url = null;
        String json="";
        try {
            url = new URL(CEK_LOGIN+"?R="+regn_no+"&a="+BuildConfig.APPLICATION_ID);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String data = "";
            while ((data = reader.readLine()) != null) {
                json += data;
            }
            data = "";
            Toast toast = Toast.makeText(getApplicationContext(), json, Toast.LENGTH_LONG);
            toast.show();
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject obj = null;
        try {
            obj = new JSONObject(json);
            if( obj.getBoolean("register_no_availability") ){
                String password = obj.getString("registered_password");
                String password2 = obj.getString("registered_parent_password");
                return password+":"+password2;
                }
            else {
               // DEVELOPMENT
                Log.d(TAG, "Register Number Not Avalable");
               Toast toast = Toast.makeText( LoginActivity.this, "Register No not available" , Toast.LENGTH_SHORT);
               toast.show();
            }
        } catch (JSONException e) {
                e.printStackTrace();
        }
        return null;
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: register the new account here.
            return true;
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            }
        }
        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }


    public class ___Database_handler extends SQLiteOpenHelper {

        // Database Info    //
        private static final String DATABASE_NAME = "crp_results";
        private static final int DATABASE_VERSION = 1;

        // Table Names  //
        private static final String TABLE_CONFIG = "crp_config";
        private static final String TABLE_ATTENDANCE = "crp_attendance";
        private static final String TABLE_SUBJECTS = "crp_subjects";
        private static final String TABLE_MARKS = "crp_marks";
        private static final String TABLE_NOTIF = "crp_notifications";

        // Config Table Columns //
        private static final String CONFIG_NAME = "name";
        private static final String CONFIG_VALUE = "address";

        // Attendance Table Columns //
        private static final String ATTEND_SEM = "sem";
        private static final String ATTEND_PERCENT = "percent";

        // Subject Table Columns    //
        private static final String SUB_ID = "id";
        private static final String SUB_SUB_ID = "sub_id";
        private static final String SUB_SUB = "sub_subject";
        private static final String SUB_SYLLABUS = "sub_syllabus";
        private static final String SUB_REFERANCE = "sub_referance";
        private static final String SUB_SCHEME = "sub_scheme";
        private static final String SUB_PREREQUEST = "sub_prerequest";
        private static final String SUB_OBJECTIVE = "sub_objective";
        private static final String SUB_TEXT = "sub_text_books";
        private static final String SUB_MOD_1 = "sub_module_1";
        private static final String SUB_MOD_2 = "sub_module_2";
        private static final String SUB_MOD_3 = "sub_module_3";
        private static final String SUB_MOD_4 = "sub_module_4";
        private static final String SUB_MOD_5 = "sub_module_5";
        private static final String SUB_MOD_6 = "sub_module_6";
        private static final String SUB_OUTCOME = "sub_outcome";

        // Marks Table Columns  //
        private static final String MARKS_ID = "id";
        private static final String MARKS_SEM = "sem";
        private static final String MARKS_SUB_CODE = "code";
        private static final String MARKS_MARK = "mark";
        private static final String MARKS_TYPE = "grade";

        // Notification Table Columns   //
        private static final String NOTIF_ID = "id";
        private static final String NOTIF_SUBJECT = "subject";
        private static final String NOTIF_BODY = "body";
        private static final String NOTIF_TIME = "timestamp";
        private static final String NOTIF_STATUS = "src";


        public ___Database_handler(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // CREATE TABLE CONFIG  //
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CONFIG +
                    " ( "
                    + CONFIG_NAME + "  varchar(64) NOT NULL,"
                    + CONFIG_VALUE + " varchar(256) DEFAULT NULL " +
                    "PRIMARY KEY (`" + CONFIG_NAME + "`)" +
                    ");"

            );

            // CREATE TABLE ATTENDANCE  //
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ATTENDANCE +
                    " ( "
                    + ATTEND_SEM + "  varchar(64) NOT NULL,"
                    + ATTEND_PERCENT + " int(3) NOT NULL" +
                    "PRIMARY KEY (`" + ATTEND_SEM + "`)" +
                    ");"
            );

            // CREATE TABLE SUBJECTS    //
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SUBJECTS +
                    " ( "
                    + SUB_ID + " int(11) NOT NULL AUTO_INCREMENT,"
                    + SUB_SUB_ID + " int(11) NOT NULL,"
                    + SUB_SUB + " varchar(255) NOT NULL,"
                    + SUB_SYLLABUS + " longtext NOT NULL,"
                    + SUB_REFERANCE + " longtext,"
                    + SUB_SCHEME + " longtext,"
                    + SUB_PREREQUEST + " longtext,"
                    + SUB_OBJECTIVE + " longtext,"
                    + SUB_TEXT + " longtext,"
                    + SUB_MOD_1 + " longtext NOT NULL,"
                    + SUB_MOD_2 + " longtext NOT NULL,"
                    + SUB_MOD_3 + " longtext NOT NULL,"
                    + SUB_MOD_4 + " longtext NOT NULL,"
                    + SUB_MOD_5 + " longtext,"
                    + SUB_MOD_6 + " longtext,"
                    + SUB_OUTCOME + " longtext,"
                    + "PRIMARY KEY (`" + SUB_ID + "`),"
                    + "UNIQUE KEY `" + SUB_SUB_ID + "` (`" + SUB_SUB_ID + "`)" +
                    ");"
            );

            // CREATE TABLE SUBJECTS
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_MARKS +
                    " ( "
                    + MARKS_ID + "  int(11) NOT NULL AUTO_INCREMENT,"
                    + MARKS_SEM + "  enum('S1','S2','S3','S4','S5','S6','S7','S8') NOT NULL,"
                    + MARKS_SUB_CODE + "  int(11) NOT NULL,"
                    + MARKS_MARK + "  varchar(4) DEFAULT 'F',"
                    + MARKS_TYPE + "  enum('int','ext') NOT NULL,"
                    + " PRIMARY KEY (`" + MARKS_ID + "`)" +
                    ");"
            );
            // CREATE TABLE NOTIF
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NOTIF +
                    " ( "
                    + NOTIF_ID + "`id` int(11) NOT NULL AUTO_INCREMENT,"
                    + NOTIF_SUBJECT + " varchar(128) NOT NULL,"
                    + NOTIF_BODY + " varchar(2048) NOT NULL,"
                    + NOTIF_TIME + " timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + NOTIF_STATUS + " tinyint(4) NOT NULL DEFAULT '0',"
                    + "PRIMARY KEY (`" + NOTIF_ID + "`)" +
                    ");"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion != newVersion) {
                // Simplest implementation is to drop all old tables and recreate them
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_MARKS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBJECTS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTENDANCE);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONFIG);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIF);
                onCreate(db);
            }
        }


        //  UPDATE CONFIGURATION TABLE  //
        public void updateConfig(String name, String val) {
            // Create and/or open the database for writing
            SQLiteDatabase db = getWritableDatabase();

            long userId = 0;
            db.beginTransaction();
            try {
                ContentValues values = new ContentValues();
                values.put(CONFIG_NAME, name);
                values.put(CONFIG_VALUE, val);

                // First try to update the user in case the user already exists in the database
                // This assumes userNames are unique
                int rows = db.update(TABLE_CONFIG, values, CONFIG_NAME + "= ?", new String[]{name});

                // Check if update succeeded
                if (rows == 1) {
                    db.setTransactionSuccessful();

                } else {
                    // user with this userName did not already exist, so insert new user
                    userId = db.insertOrThrow(TABLE_CONFIG, null, values);
                    db.setTransactionSuccessful();
                }
            } catch (Exception e) {
                Log.d(TAG, "Error while trying to add or update persornal details");
            } finally {
                db.endTransaction();
            }
        }


        //      ATTENDANCE       //
        public void updateAttendance(String sem, String percent) {          //  updateAttendance ( SEM / PERCENT )
            // Create and/or open the database for writing
            SQLiteDatabase db = getWritableDatabase();

            long userId = 0;
            db.beginTransaction();
            try {
                ContentValues values = new ContentValues();
                values.put(ATTEND_SEM, sem);
                values.put(ATTEND_PERCENT, percent);

                // First try to update the user in case the user already exists in the database
                // This assumes userNames are unique
                int rows = db.update(TABLE_ATTENDANCE, values, ATTEND_PERCENT + "= ?", new String[]{sem});

                // Check if update succeeded
                if (rows == 1) {
                    db.setTransactionSuccessful();

                } else {
                    // user with this userName did not already exist, so insert new user
                    userId = db.insertOrThrow(TABLE_ATTENDANCE, null, values);
                    db.setTransactionSuccessful();
                }
            } catch (Exception e) {
                Log.d(TAG, "Error while trying to add or update Attendance");
            } finally {
                db.endTransaction();
            }
        }

        //      MARKS       //
        public void updateMarks(String sem, String sub, String grade, String type) {       // updateMarks ( SEM / SUB / GRADE / TYPE )
            // Create and/or open the database for writing
            // type= int / ext
            SQLiteDatabase db = getWritableDatabase();

            long userId = 0;
            db.beginTransaction();
            try {
                ContentValues values = new ContentValues();
                //values.put(MARKS_SEM, sem);
                //values.put(MARKS_SUB_CODE, sub);
                values.put(MARKS_MARK, grade);
                //values.put(MARKS_TYPE, type);

                // First try to update the user in case the user already exists in the database
                // This assumes userNames are unique
                int rows = db.update(TABLE_MARKS, values, MARKS_SEM + "= ? " + MARKS_SUB_CODE + "= ? " + MARKS_TYPE + "= ?", new String[]{sem, sub, type});

                // Check if update succeeded
                if (rows == 1) {
                    db.setTransactionSuccessful();
                } else {
                    values.put(MARKS_SEM, sem);
                    values.put(MARKS_SUB_CODE, sub);
                    values.put(MARKS_MARK, grade);
                    values.put(MARKS_TYPE, type);

                    // user with this userName did not already exist, so insert new user
                    userId = db.insertOrThrow(TABLE_MARKS, null, values);
                    db.setTransactionSuccessful();
                }
            } catch (Exception e) {
                Log.d(TAG, "Error while trying to add or update Marks");
            } finally {
                db.endTransaction();
            }
        }


        //      SUBJECTS       //
        public void updateSubject(String name, String id, String syllabus, String reference, String scheme, String prerequest, String objective, String text, String cource_plan_1, String cource_plan_2, String cource_plan_3, String cource_plan_4, String cource_plan_5, String cource_plan_6, String outcome) {
            // Create and/or open the database for writing
            // type= int / ext
            SQLiteDatabase db = getWritableDatabase();

            long userId = 0;
            db.beginTransaction();
            try {
                ContentValues values = new ContentValues();
                values.put(SUB_SUB_ID, id);
                values.put(SUB_SUB, name);
                values.put(SUB_SYLLABUS, syllabus);
                values.put(SUB_REFERANCE, reference);
                values.put(SUB_SCHEME, scheme);
                values.put(SUB_PREREQUEST, prerequest);
                values.put(SUB_OBJECTIVE, objective);
                values.put(SUB_TEXT, text);
                values.put(SUB_MOD_1, cource_plan_1);
                values.put(SUB_MOD_2, cource_plan_2);
                values.put(SUB_MOD_3, cource_plan_3);
                values.put(SUB_MOD_4, cource_plan_4);
                values.put(SUB_MOD_5, cource_plan_5);
                values.put(SUB_MOD_6, cource_plan_6);
                values.put(SUB_OUTCOME, outcome);


                // First try to update the user in case the user already exists in the database
                // This assumes userNames are unique
                int rows = db.update(TABLE_SUBJECTS, values, SUB_SUB_ID + "= ? ", new String[]{id});

                // Check if update succeeded
                if (rows == 1) {
                    db.setTransactionSuccessful();
                } else {
                    // user with this userName did not already exist, so insert new user
                    userId = db.insertOrThrow(TABLE_MARKS, null, values);
                    db.setTransactionSuccessful();
                }
            } catch (Exception e) {
                Log.d(TAG, "Error while trying to add or update Subject");
            } finally {
                db.endTransaction();
            }
        }

        public Cursor getSubject(String id) {
            SQLiteDatabase db = getReadableDatabase();
            return db.query(TABLE_SUBJECTS, new String[]{SUB_SUB}, SUB_SUB_ID + "=?", new String[]{id}, null, null, null);
            // Get a subject from subject id
        }


        public Cursor getSyllabus(String id) {
            SQLiteDatabase db = getReadableDatabase();
            return db.query(TABLE_SUBJECTS, new String[]{SUB_SUB, SUB_SYLLABUS, SUB_MOD_1, SUB_MOD_2, SUB_MOD_3, SUB_MOD_4, SUB_MOD_5, SUB_MOD_6, SUB_OBJECTIVE, SUB_OUTCOME, SUB_TEXT, SUB_REFERANCE, SUB_SCHEME, SUB_PREREQUEST}, SUB_SUB_ID + "=?", new String[]{id}, null, null, null);
            // get the syllabus of a subject
        }


        public Cursor getAttendance() {
            SQLiteDatabase db = getReadableDatabase();
            return db.query(TABLE_ATTENDANCE, new String[]{ATTEND_SEM, ATTEND_PERCENT}, ATTEND_SEM + "=?", new String[]{ATTEND_PERCENT}, null, null, null);
            //Get the whole attendance
        }


        public Cursor getMarkOf(String sem, String subj, String type) {
            SQLiteDatabase db = getReadableDatabase();
            return db.query(TABLE_MARKS, new String[]{MARKS_MARK}, MARKS_SEM + "=?" + MARKS_SUB_CODE + "=?" + MARKS_TYPE + "=?", new String[]{sem, subj, type}, null, null, null);
            //returns Mark of a subejct at a sem.
        }


        public Cursor getConfig(String data) {
            SQLiteDatabase db = getReadableDatabase();
            return db.query(TABLE_CONFIG, new String[]{CONFIG_NAME, CONFIG_VALUE}, CONFIG_NAME + "=?", new String[]{data}, null, null, null);
            //returns config
        }

        public Cursor getConfig() {
            SQLiteDatabase db = getReadableDatabase();
            return db.query(TABLE_CONFIG, new String[]{CONFIG_NAME, CONFIG_VALUE}, CONFIG_NAME + "=?", new String[]{null}, null, null, null);
            //returns config
        }
        //  END OF CLASS    //
    }
}

