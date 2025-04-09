package com.example.quiz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class QuizQuestions extends AppCompatActivity {

    private String userName;
    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int score = 0;

    private TextView questionTextView, questionProgressTextView;
    private Button option1Button, option2Button, option3Button, option4Button, submitButton;
    private ProgressBar progressBar;

    private Button selectedButton = null;
    private boolean isAnswerSubmitted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_questions);

        progressBar = findViewById(R.id.progressBar);
        questionProgressTextView = findViewById(R.id.questionProgressTextView);
        questionTextView = findViewById(R.id.questionTextView);
        option1Button = findViewById(R.id.option1Button);
        option2Button = findViewById(R.id.option2Button);
        option3Button = findViewById(R.id.option3Button);
        option4Button = findViewById(R.id.option4Button);
        submitButton = findViewById(R.id.submitButton);

        SharedPreferences sharedPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        userName = sharedPreferences.getString("USERNAME", "");

        loadQuestionsFromJson();
        setQuestion(questionList.get(currentQuestionIndex));
        updateProgress(currentQuestionIndex);

        option1Button.setOnClickListener(optionClickListener);
        option2Button.setOnClickListener(optionClickListener);
        option3Button.setOnClickListener(optionClickListener);
        option4Button.setOnClickListener(optionClickListener);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAnswerSubmitted) {
                    submitAnswer();
                } else {
                    moveToNextQuestion();
                }
            }
        });
    }

    private View.OnClickListener optionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            selectOption((Button) v);
        }
    };

    private void loadQuestionsFromJson() {
        questionList = new ArrayList<>();
        try {
            InputStream is = getAssets().open("questions.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            JSONObject rootObject = new JSONObject(json);
            JSONArray jsonArray = rootObject.getJSONArray("questions");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String questionText = jsonObject.getString("question");
                String correctAnswer = jsonObject.getString("correct_answer");
                JSONArray optionsArray = jsonObject.getJSONArray("options");
                String[] options = new String[optionsArray.length()];
                for (int j = 0; j < optionsArray.length(); j++) {
                    options[j] = optionsArray.getString(j);
                }
                Question question = new Question(questionText, options, correctAnswer);
                questionList.add(question);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateProgress(int index) {
        int totalQuestions = questionList.size();
        questionProgressTextView.setText((index + 1) + "/" + totalQuestions);
        progressBar.setProgress((index + 1) * 100 / totalQuestions);
    }

    private void setQuestion(Question question) {
        questionTextView.setText(question.getQuestionText());
        String[] options = question.getOptions();
        option1Button.setText(options[0]);
        option2Button.setText(options[1]);
        option3Button.setText(options[2]);
        option4Button.setText(options[3]);
    }

    private void selectOption(Button button) {
        clearOptionButtons();
        button.setBackgroundColor(getResources().getColor(R.color.selectedOption));
        selectedButton = button;
    }

    private void clearOptionButtons() {
        option1Button.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        option2Button.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        option3Button.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        option4Button.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        selectedButton = null;
    }

    private void submitAnswer() {
        String selectedAnswer = getSelectedAnswer();
        if (TextUtils.isEmpty(selectedAnswer)) {
            Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show();
            return;
        }

        isAnswerSubmitted = true;
        String correctAnswer = questionList.get(currentQuestionIndex).getCorrectAnswer();

        Button[] allButtons = {option1Button, option2Button, option3Button, option4Button};

        for (Button btn : allButtons) {
            String btnText = btn.getText().toString();

            if (btnText.equals(correctAnswer)) {
                btn.setBackgroundColor(getResources().getColor(R.color.correctAnswer)); // green
            } else if (btnText.equals(selectedAnswer)) {
                btn.setBackgroundColor(getResources().getColor(R.color.wrongAnswer)); // red
            } else {
                // 👇 Reset other buttons to default background (light gray)
                btn.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            }

            btn.setEnabled(false); // disable all buttons after submission
        }

        if (selectedAnswer.equals(correctAnswer)) {
            score++;
        }

        submitButton.setText("Next");
    }


    private String getSelectedAnswer() {
        if (selectedButton != null) {
            return selectedButton.getText().toString();
        }
        return "";
    }

    private void moveToNextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < questionList.size()) {
            setQuestion(questionList.get(currentQuestionIndex));
            updateProgress(currentQuestionIndex);
            clearOptionButtons();
            option1Button.setEnabled(true);
            option2Button.setEnabled(true);
            option3Button.setEnabled(true);
            option4Button.setEnabled(true);
            submitButton.setText("Submit");
            isAnswerSubmitted = false;
        } else {
            Intent intent = new Intent(QuizQuestions.this, EndQuiz.class);
            intent.putExtra("totalQuestions", questionList.size());
            intent.putExtra("score", score);
            startActivity(intent);
            finish();
        }
    }
}