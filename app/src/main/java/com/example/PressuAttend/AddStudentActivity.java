package com.example.PressuAttend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AddStudentActivity extends AppCompatActivity {
    private EditText et_user_id, et_name, et_roll_no, et_dept, et_phone, et_email, et_password;
    ProgressDialog pd;
    Button b5;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    CheckBox cb1, cb2, cb3, cb4, cb5, cb6, cb7, cb8;

    private static final String TAG = "AddStudentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        et_user_id = findViewById(R.id.editText5);
        et_name = findViewById(R.id.editText6);
        et_roll_no = findViewById(R.id.editText7);
        et_dept = findViewById(R.id.editText8);
        et_phone = findViewById(R.id.editText9);
        et_email = findViewById(R.id.editText10);
        et_password = findViewById(R.id.editText11);
        b5 = findViewById(R.id.button5);

        firebaseAuth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(this);

        if (firebaseAuth.getCurrentUser() != null) {
            Toast.makeText(AddStudentActivity.this, "Already signIn - signing out for admin flow", Toast.LENGTH_SHORT).show();
            firebaseAuth.signOut();
        }

        cb1 = findViewById(R.id.checkBox1);
        cb2 = findViewById(R.id.checkBox2);
        cb3 = findViewById(R.id.checkBox3);
        cb4 = findViewById(R.id.checkBox4);
//        cb5 = findViewById(R.id.checkBox5);
//        cb6 = findViewById(R.id.checkBox6);
//        cb7 = findViewById(R.id.checkBox7);
//        cb8 = findViewById(R.id.checkBox8);
    }

    public void fnRegister(View view) {
        Log.e(TAG, "Entering fnRegister");

        final String userid = et_user_id.getText().toString().trim();
        final String name = et_name.getText().toString().trim();
        final String rollno = et_roll_no.getText().toString().trim();
        final String dept = et_dept.getText().toString().trim();
        final String phone = et_phone.getText().toString().trim();
        final String email = et_email.getText().toString().trim();
        final String password = et_password.getText().toString().trim();

        // quick local validations
        if (TextUtils.isEmpty(userid) || TextUtils.isEmpty(name) || TextUtils.isEmpty(rollno) ||
                TextUtils.isEmpty(dept) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password)) {
            Toast.makeText(AddStudentActivity.this, "Block cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(AddStudentActivity.this, "Password should contain min. 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // ensure user id and rollno are not identical
        if (userid.equals(rollno)) {
            Toast.makeText(this, "User ID and Roll No. cannot be the same", Toast.LENGTH_LONG).show();
            return;
        }

        // build subjects list correctly (size 8, -1 indicates not selected)
        final ArrayList<Integer> subject = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) subject.add(0);
        if (!cb1.isChecked()) subject.set(0, -1);
        if (!cb2.isChecked()) subject.set(1, -1);
        if (!cb3.isChecked()) subject.set(2, -1);
        if (!cb4.isChecked()) subject.set(3, -1);
//        if (!cb5.isChecked()) subject.set(4, -1);
//        if (!cb6.isChecked()) subject.set(5, -1);
//        if (!cb7.isChecked()) subject.set(6, -1);
//        if (!cb8.isChecked()) subject.set(7, -1);

        int sum = 0;
        for (int i = 0; i < 4; i++) sum += subject.get(i);
        if (sum == -4) {
            Toast.makeText(AddStudentActivity.this, "Please select at least one subject", Toast.LENGTH_SHORT).show();
            return;
        }

        // show progress
        pd.setMessage("Checking availability...");
        pd.setCancelable(false);
        pd.show();

        // Use root reference (your users are placed under root UIDs in DB). If you later move to a 'users' node, change this.
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // 1) Check if userId already exists (child key)
        databaseReference.child(userid).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                pd.dismiss();
                Log.e(TAG, "Error checking userId", task.getException());
                Toast.makeText(AddStudentActivity.this, "Error checking user ID: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_LONG).show();
                return;
            }

            DataSnapshot snap = task.getResult();
            if (snap.exists()) {
                pd.dismiss();
                Toast.makeText(AddStudentActivity.this, "User ID already exists. Choose another user id.", Toast.LENGTH_LONG).show();
                return;
            }

            // 2) Check rollno uniqueness by scanning first-level children and comparing rollno field
            databaseReference.get().addOnCompleteListener(task2 -> {
                if (!task2.isSuccessful()) {
                    pd.dismiss();
                    Log.e(TAG, "Error reading DB for rollno check", task2.getException());
                    Toast.makeText(AddStudentActivity.this, "Error checking roll number: " + (task2.getException() != null ? task2.getException().getMessage() : ""), Toast.LENGTH_LONG).show();
                    return;
                }

                boolean rollExists = false;
                DataSnapshot rootSnap = task2.getResult();
                if (rootSnap.exists()) {
                    for (DataSnapshot child : rootSnap.getChildren()) {
                        if (child.child("rollno").exists()) {
                            String existingRoll = child.child("rollno").getValue(String.class);
                            if (existingRoll != null && existingRoll.equals(rollno)) {
                                rollExists = true;
                                break;
                            }
                        }
                    }
                }

                if (rollExists) {
                    pd.dismiss();
                    Toast.makeText(AddStudentActivity.this, "Roll No already used by another student", Toast.LENGTH_LONG).show();
                    return;
                }

                // All checks passed -> proceed to register user with FirebaseAuth
                pd.setMessage("Registering user...");
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(AddStudentActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> authTask) {
                                if (authTask.isSuccessful()) {
                                    // prepare student object and save under the chosen userid
                                    StudentInformation stdinfo = new StudentInformation(name, rollno, dept, phone, email, password, subject);

                                    // Save to DB under the selected userid (not the auto-generated uid)
                                    databaseReference.child(userid).setValue(stdinfo).addOnCompleteListener(saveTask -> {
                                        pd.dismiss();
                                        if (saveTask.isSuccessful()) {
                                            Toast.makeText(AddStudentActivity.this, "User Registered Successfully", Toast.LENGTH_LONG).show();
                                            // sign out to keep admin flow clean
                                            firebaseAuth.signOut();
                                        } else {
                                            Log.e(TAG, "Failed to save student info", saveTask.getException());
                                            Toast.makeText(AddStudentActivity.this, "Failed to save information: " + (saveTask.getException() != null ? saveTask.getException().getMessage() : ""), Toast.LENGTH_LONG).show();
                                            // even if save fails, sign out auth to avoid leaving a logged-in user
                                            firebaseAuth.signOut();
                                        }
                                    });

                                } else {
                                    pd.dismiss();
                                    Log.e(TAG, "Unable to register", authTask.getException());
                                    Toast.makeText(AddStudentActivity.this, "Unable to register: " + (authTask.getException() != null ? authTask.getException().getMessage() : ""), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            });
        });
    }
}
