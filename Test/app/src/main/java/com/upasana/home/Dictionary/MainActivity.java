package com.upasana.home.Dictionary;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Locale;

import static android.R.id.edit;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, EditText.OnEditorActionListener {

    private final int SPEECH_RECOGNITION_CODE = 1;
    public static final int progress_bar_type = 0;
    private ProgressDialog pDialog;
    EditText word;
    TextView tv;
    FloatingActionButton fab_go, fab_speak1, fab_speak2, fab, talk, del, anym;
    TextToSpeech t1;
    int n;
    LinearLayout l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#010d6b"));
        }
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        l = (LinearLayout) findViewById(R.id.l);
        word = (EditText) findViewById(R.id.edittext);
        tv = (TextView) findViewById(R.id.tv);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab_go = (FloatingActionButton) findViewById(R.id.fab_go);
        fab_speak1 = (FloatingActionButton) findViewById(R.id.speak1);
        fab_speak2 = (FloatingActionButton) findViewById(R.id.speak2);
        talk = (FloatingActionButton) findViewById(R.id.talk);
        del = (FloatingActionButton) findViewById(R.id.del);
        anym = (FloatingActionButton) findViewById(R.id.antonym);

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

        l.setOnClickListener(this);
        anym.setOnClickListener(this);
        del.setOnClickListener(this);
        fab.setOnClickListener(this);
        fab_go.setOnClickListener(this);
        fab_speak1.setOnClickListener(this);
        fab_speak2.setOnClickListener(this);
        talk.setOnClickListener(this);
        word.setOnEditorActionListener(this);



        n = 0;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void onClick(View v) {

        String validate[]=word.getText().toString().trim().split(" ");
        if (validate.length>1)
            Toast.makeText(this,"Please enter one word .. ",Toast.LENGTH_SHORT).show();
        else{
        if (v.equals(l))
            hide();
        if (v.equals(fab_go)) {
            n = 1;
            submit(v);
            hide();
        }

        if (v.equals(fab)) {
            n = 2;
            submit(v);
            hide();
        }
        if (v.equals(anym)) {
            hide();
            n = 3;
            submit(v);
        }

        if (v.equals(fab_speak2) && word.getText() != null)
            t1.speak(word.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);

        if (v.equals(fab_speak1) && tv.getText() != null)
            t1.speak(tv.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
        if (v.equals(talk))
            startSpeechToText();
        if (v.equals(del)) {
            tv.setText("");
            word.setText("");
        }
        }
    }

    public void submit(View v) {
        if (!isNetworkConnected())
            Toast.makeText(this, "Check your Internet Connection...", Toast.LENGTH_SHORT).show();
        else {

            String w = word.getText().toString().trim().toLowerCase();
            if (n == 1)
                new LongOperation().execute("http://www.dictionary.com/browse/"+w+"?s=t" );
            if (n == 2 || n == 3)
                new LongOperation().execute("http://www.thesaurus.com/browse/" + w);

        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class LongOperation extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }

        @Override
        protected String doInBackground(String... address) {


            String s = "";

            try {
                URL url = new URL(address[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Opera/9.80 (X11; Linux x86_64) Presto/2.12.388 Version/12.11");
                InputStream iS = conn.getInputStream();

                BufferedInputStream in = new BufferedInputStream(iS);

                byte[] data = new byte[1024];
                int x = 0;
                while ((x = in.read(data, 0, 1024)) >= 0) {
                    s += new String(data);
                }

                in.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return s;
        }

        @Override
        protected void onPostExecute(String result) {
            String input = word.getText().toString().trim().toLowerCase();
            dismissDialog(progress_bar_type);

            getMeaning(result, input);


        }

        private void getMeaning(String result, String input) {
            String meaning = "Sorry, Could not understand!";
            stop_speech();
            try {
                //<meta name="description" content="Hi definition,
                if (n == 1) {
                    //result=result.toLowerCase();
                    String spl="<meta name=\"description\" content=\"";
                    String[] temp = result.split(spl);
                    String [] t=temp[1].split("definition, ");
                    t[1] = t[1].replace("(", "");
                    String[] temp2 = t[1].split("See more");
                    temp2[0] = temp2[0].replace(")", "");
                    meaning = temp2[0];
                    n = 0;

                } else {

                    String[] temp = result.split("<!-- google_ad_section");
                    String[] temp1 = temp[1].split("<section class=\"container-info antonyms\" >");
                    if (n == 2) {
                        if (temp1[0] == null) {
                            temp1[0] = temp[1];
                        }
                        meaning = "Synonym" + System.getProperty("line.separator") + System.getProperty("line.separator");
                        String[] temp2 = temp1[0].split("<a href=\"http://www.thesaurus.com/browse/");
                        String sample = " ";
                        for (int j = 1; j < temp2.length; j++) {
                            sample = " ";
                            for (int i = 0; i < 40; i++) {
                                if (temp2[j].charAt(i) != '"')
                                    sample += temp2[j].charAt(i);
                                else {
                                    meaning = meaning.replace(sample + " , ", "");
                                    meaning += sample + " , ";
                                    break;
                                }
                            }

                        }
                    }
                    if (n == 3) {
                        meaning = "Antonym" + System.getProperty("line.separator") + System.getProperty("line.separator");

                        if (temp1[0] == null) {
                            meaning += "Sorry, Could not find required terms..";
                        } else {
                            String[] temp2 = temp1[1].split("<a href=\"http://www.thesaurus.com/browse/");
                            String sample = " ";
                            for (int j = 1; j < temp2.length; j++) {
                                sample = " ";
                                for (int i = 0; i < 40; i++) {
                                    if (temp2[j].charAt(i) != '"')
                                        sample += temp2[j].charAt(i);
                                    else {
                                        meaning = meaning.replace(sample + " , ", "");
                                        meaning += sample + " , ";
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    meaning= URLDecoder.decode(meaning, "UTF-8");


                }
            } catch (Exception e) {
                Toast.makeText(MainActivity.this,e.toString(),Toast.LENGTH_SHORT).show();
            }
            ;
            tv.setText(meaning);
            n = 0;
        }

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void startSpeechToText() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Speak something...");
        try {
            startActivityForResult(intent, SPEECH_RECOGNITION_CODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Speech recognition is not supported in this device.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Callback for speech recognition activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SPEECH_RECOGNITION_CODE: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String text = result.get(0);
                    word.setText(text);
                }
                break;
            }
        }
    }

    public void hide() {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

    }


    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            hide();
            n = 1;
            submit(v);
        }
        return false;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    /**
     * Showing Dialog
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type:
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file. Please wait...");
                pDialog.setIndeterminate(true);
                pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        stop_speech();
    }

    public void stop_speech()
    {
        if(t1.isSpeaking())
            t1.stop();
    }
}
