package com.example.cvd_draft_1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ScriptDisplayActivity extends AppCompatActivity {

    private LinearLayout scriptContainer;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private ArrayList<String> questionsList = new ArrayList<>();
    private ArrayList<String> answersList = new ArrayList<>();
    private ArrayList<EditText> answerFields = new ArrayList<>();  // Store references to answer EditTexts
    ImageButton btnBack;  // Declare the back button


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_display);

        scriptContainer = findViewById(R.id.scriptContainer);
        TextView btnBack = findViewById(R.id.btnBack);
        Button btnNext = findViewById(R.id.btnNext);  // Button to handle "Next" action

        // Initialize Firebase Firestore and Auth
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // Initialize the ImageButton for the back button
        btnBack = findViewById(R.id.btnBack);

        // Set click listener for the back button to finish the current activity
        btnBack.setOnClickListener(v -> finish());



        // Handle Next Button Click
        btnNext.setOnClickListener(v -> {
            updateAnswersList();  // Update the answersList with the edited values
            saveScriptToFirestore();
        });

        // Retrieve the passed questions and answers from the Intent
        ArrayList<String> questions = getIntent().getStringArrayListExtra("QUESTIONS");
        ArrayList<String> answers = getIntent().getStringArrayListExtra("ANSWERS");

        // Display the script if questions and answers are valid
        if (questions != null && answers != null && questions.size() == answers.size()) {
            questionsList = questions;
            answersList = answers;
            for (int i = 0; i < questions.size(); i++) {
                String question = questions.get(i);
                String answer = answers.get(i);
                addScriptEditText(question, answer);
            }
        } else {
            Toast.makeText(this, "No script data available.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to add an EditText for each question and answer
    private void addScriptEditText(String question, String answer) {
        // Create a TextView for the question label
        TextView questionLabel = new TextView(this);
        questionLabel.setText(question);  // Display the question as a label
        questionLabel.setTextSize(16);
        questionLabel.setPadding(16, 16, 16, 8);

        // Create an EditText for the answer
        EditText editText = new EditText(this);
        editText.setText(answer);  // Set the answer as the initial text
        editText.setTextSize(16);
        editText.setPadding(16, 16, 16, 16);

        // Add the EditText reference to the list
        answerFields.add(editText);

        // Add the question label and the editable answer field to the layout
        scriptContainer.addView(questionLabel);
        scriptContainer.addView(editText);
    }

    // Method to update the answersList with edited values
    private void updateAnswersList() {
        for (int i = 0; i < answerFields.size(); i++) {
            EditText editText = answerFields.get(i);
            answersList.set(i, editText.getText().toString());  // Update the answersList with the current text
        }
    }

    // Method to save the script to Firestore
    private void saveScriptToFirestore() {
        if (currentUser == null) {
            Toast.makeText(ScriptDisplayActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare the script data
        Map<String, Object> scriptData = new HashMap<>();
        scriptData.put("questions", questionsList);  // Save questions as a list
        scriptData.put("answers", answersList);      // Save answers as a list
//        scriptData.put("createdAt", System.currentTimeMillis());

        // Convert the timestamp to a readable string format
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String createdAt = sdf.format(new Date(System.currentTimeMillis())); // Store as String
        scriptData.put("createdAt", createdAt);       // Save createdAt as String



        // Reference to the user's scripts collection
        DocumentReference userDocRef = db.collection("users").document(currentUser.getUid());

        // Add a new script document to the "scripts" sub-collection
        userDocRef.collection("scripts")
                .add(scriptData)  // Add the script document to the "scripts" sub-collection
                .addOnSuccessListener(documentReference -> {
                    // Navigate to another activity after saving
                    Intent intent = new Intent(ScriptDisplayActivity.this, ScriptReadyNotification.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ScriptDisplayActivity.this, "Failed to save script.", Toast.LENGTH_SHORT).show();
                });
    }
}


