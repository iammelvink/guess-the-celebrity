package com.iammelvink.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    /*Class fields/variables*/
    private ImageView celebImg;
    private Button btn0;
    private Button btn1;
    private Button btn2;
    private Button btn3;
    /*
     * Generate random numbers*/
    private SecureRandom rand = new SecureRandom();
    private final int SIZE = 4;
    private String[] answers = new String[SIZE];
    private int locationOfCorrectAnswer = 0;
    private ArrayList<String> celebURLs = new ArrayList<String>();
    private ArrayList<String> celebNames = new ArrayList<String>();
    private int chosenCeleb = 0;

    /*Class download text/html on a background thread*/
    public class DownloadTask extends AsyncTask<String, Void, String> {

        /*array thingie
         * String... strings*/
        @Override
        protected String doInBackground(String... urls) {
            /*Return when task is complete*/
            String result = "";

            /*Converts String to URL*/
            URL url;
            /*Creates connection*/
            HttpURLConnection urlConnection = null;

            /*Converting String to URL*/
            try {
                url = new URL(urls[0]);
                /*Creating connection
                 * CAST URL AS HttpURLConnection*/
                urlConnection = (HttpURLConnection) url.openConnection();

                /*Gather data as in comes in*/
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                /*Each character is saved as an int*/
                int data = reader.read();

                /*
                Runs while there is something to download
                -1 means nothing*/
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                /*Downloaded text/html*/
                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                return "Failed";
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                return "Failed";
            }
        }
    }

    /*Class download images on a background thread*/
    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream in = connection.getInputStream();
                /*Downloads image in background*/
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;

            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                return null;
            }


        }
    }

    /*Checks if correct celeb name was chosen*/
    public void celebChosen(final View view) {
        if (view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))) {
            Toast.makeText(getApplicationContext(), "Correct! :)", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Wrong! :( It was " + celebNames.get(chosenCeleb), Toast.LENGTH_SHORT).show();
        }

        /*Generate new question after answering*/
        newQuestion();
    }

    public void newQuestion() {
        /*Choose celeb at random*/
        try {
            chosenCeleb = rand.nextInt(celebURLs.size());

            /*Get image*/
            ImageDownloader imageTask = new ImageDownloader();
            Bitmap celebImage = imageTask.execute(celebURLs.get(chosenCeleb)).get();

            /*Choose location of correct answer*/
            locationOfCorrectAnswer = rand.nextInt(SIZE);
            int wrongAnswer;

            for (int i = 0; i < SIZE; i++) {
                if (i == locationOfCorrectAnswer) {
                    answers[i] = celebNames.get(chosenCeleb);
                } else {
                    wrongAnswer = rand.nextInt(celebURLs.size());
                    /*Checks if correct answer appears more than once*/
                    while (wrongAnswer == chosenCeleb) {
                        wrongAnswer = rand.nextInt(celebURLs.size());
                    }
                    answers[i] = celebNames.get(wrongAnswer);
                }
            }
            celebImg.setImageBitmap(celebImage);
        } catch (ExecutionException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
//        /*Put answers in buttons*/
        btn0.setText(answers[0]);
        btn1.setText(answers[1]);
        btn2.setText(answers[2]);
        btn3.setText(answers[3]);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Connecting objects(Buttons & ImageView) to code*/
        celebImg = (ImageView) findViewById(R.id.imageView1);
        btn0 = (Button) findViewById(R.id.btn0);
        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        btn3 = (Button) findViewById(R.id.btn3);

        /*Downloading text/html from this link*/
        DownloadTask task = new DownloadTask();
        String result = null;
        try {
            result = task.execute("http://www.posh24.se/kandisar").get();

            /*Exclude images in
             * Articles*/
            String[] splitResult = result.split("<div class=\"listedArticles\">");

            /*Get link to image*/
            Pattern p = Pattern.compile("img src=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[0]);

            while (m.find()) {
                celebURLs.add(m.group(1));
            }

            /*Get link to name*/
            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitResult[0]);

            while (m.find()) {
                celebNames.add(m.group(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
        Log.i("Result", result);

        /*Generate a question*/
        newQuestion();
    }
}
