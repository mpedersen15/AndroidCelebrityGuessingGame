package com.example.matt3865.celebrityguessinggame;

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
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> names;
    ArrayList<String> imageUrls;
    ImageDownloader imageDownloader;
    Button button1;
    Button button2;
    Button button3;
    Button button4;
    Question currentQuestion;

    // Views
    ImageView celebImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        celebImageView = findViewById(R.id.celebImageView);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);

        fetchCelebrities();

        /* TODO:
            - Generate a new Question and render
            - Add in click handlers to buttons
            - Check for correct answer (and send appropriate toast)
            - Generate another question
        */


    }


    private void fetchCelebrities() {
        HtmlDownloader downloader = new HtmlDownloader();

        String result = null;
        try {
            result = downloader.execute("http://www.posh24.se/kandisar").get();

            Pattern p = Pattern.compile("<img src=\"(.*?)\"");
            Matcher m = p.matcher(result);

            imageUrls = new ArrayList<>();

            while (m.find()) {
                imageUrls.add(m.group(1));
            }

            Pattern p2 = Pattern.compile("<div class=\"name\">(.*?)<", Pattern.DOTALL);
            Matcher m2 = p2.matcher(result);

            names = new ArrayList<>();

            while (m2.find()) {
                names.add(m2.group(1).trim());
            }

            for (int i = imageUrls.size() - 1; i >= names.size(); i--) {
                imageUrls.remove(i);
            }

            Log.i("Names", names.get(names.size() - 1));
            Log.i("URLs", imageUrls.get(imageUrls.size() - 1));


            currentQuestion = new Question();
            renderQuestion(currentQuestion);



        } catch (Exception e) {
            Log.e("Error", "Error using downloader");
            e.printStackTrace();
        }

    }

    public void selectAnswer(View view) {
        Log.i("Button Selected", view.getTag().toString());

        int guess = Integer.parseInt(view.getTag().toString());

        if (currentQuestion.checkAnswer(guess)) {
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Wrong!", Toast.LENGTH_SHORT).show();
        }

        currentQuestion = new Question();

        renderQuestion(currentQuestion);
    }

    private void renderQuestion(Question q) {
        celebImageView.setImageBitmap(q.image);

        button1.setText(q.answers[0]);
        button2.setText(q.answers[1]);
        button3.setText(q.answers[2]);
        button4.setText(q.answers[3]);

    }

    private boolean contains(String[] arr, String item) {
        for (String i : arr) {
            if(item == i) {
                return true;
            }
        }
        return false;
    }

    public class Question {
        Bitmap image;
        String name;
        int indexOfCorrectAnswer;
        String[] answers = new String[4];

        Question() {
            Random r = new Random();
            imageDownloader = new ImageDownloader();

            int index = r.nextInt(names.size());
            try {
                image = imageDownloader.execute(imageUrls.get(index)).get();
            } catch(Exception e) {
                System.out.println("Failed to get Image");
                e.printStackTrace();
            }
            this.name = names.get(index);

            indexOfCorrectAnswer = r.nextInt(4);

            answers[indexOfCorrectAnswer] = name;

            for (int i = 0; i < answers.length; i++) {
                if (answers[i] == null) {
                    int randomIndex = r.nextInt(names.size());

                    String randomName = names.get(randomIndex);

                    while(contains(answers, randomName)) {
                         randomIndex = r.nextInt(names.size());

                         randomName = names.get(randomIndex);
                    }

                    answers[i] = randomName;
                }
            }

        }

        public boolean checkAnswer(int i) {
            return i == indexOfCorrectAnswer;
        }
    }

    public class ImageDownloader extends  AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.connect();

                InputStream in = urlConnection.getInputStream();

                Bitmap bitmap = BitmapFactory.decodeStream(in);

                return bitmap;
            } catch (Exception e) {
                System.out.println("Failed in image downloader...");
                e.printStackTrace();
                return null;
            }
        }
    }

    public class HtmlDownloader extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;

                    data = reader.read();
                }

                return result;
            } catch (MalformedURLException e) {
                Log.e("Malformed URL", "User needs to enter a valid URL");
                e.printStackTrace();

                return "Failed";
            } catch (Exception e) {
                Log.e("Error occurred", "See stacktrace below: ");
                e.printStackTrace();
                return "Failed";
            }


        }
    }
}
