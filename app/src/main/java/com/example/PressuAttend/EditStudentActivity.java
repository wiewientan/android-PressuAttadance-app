package com.example.PressuAttend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class EditStudentActivity extends AppCompatActivity {
    private EditText et_user_id, et_name, et_roll_no, et_dept, et_phone, et_email, et_password;
    private String studentKey; // Kunci Enrollment/UID siswa
    private ProgressDialog pd;

    private static final String TAG = "EditStudentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student);

        // 1. Inisialisasi UI
        et_user_id = findViewById(R.id.et_user_id);
        et_name = findViewById(R.id.et_name);
        et_roll_no = findViewById(R.id.et_roll_no);
        et_dept = findViewById(R.id.et_dept);
        et_phone = findViewById(R.id.et_phone);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        pd = new ProgressDialog(this);

        // 2. Ambil Data dari Intent (Data siswa lama)
        Intent intent = getIntent();
        studentKey = intent.getStringExtra("student_key");

        String name = intent.getStringExtra("student_name");
        String rollno = intent.getStringExtra("student_rollno");
        String dept = intent.getStringExtra("student_dept");
        String phone = intent.getStringExtra("student_phone");
        String email = intent.getStringExtra("student_email");
        String password = intent.getStringExtra("student_password"); // Hanya ditampilkan, tidak diedit

        if (studentKey == null || studentKey.isEmpty()) {
            Toast.makeText(this, "Error: Student key not found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 3. Isi Data ke EditText
        et_user_id.setText(studentKey);
        et_name.setText(name);
        et_roll_no.setText(rollno);
        et_dept.setText(dept);
        et_phone.setText(phone);
        et_email.setText(email);

        // Tampilkan Password (atau placeholder) untuk field yang dinonaktifkan
        et_password.setText(password != null ? "******" : "N/A");

        // Disable fields yang tidak boleh diubah (Sudah diatur di XML, ini hanya konfirmasi)
        et_user_id.setEnabled(false);
        et_roll_no.setEnabled(false);
        et_email.setEnabled(false);
        et_password.setEnabled(false);
    }

    // Fungsi untuk Tombol Cancel
    public void cancelEdit(View view) {
        finish(); // Cukup tutup Activity Edit
    }

    // Fungsi untuk Tombol Update
    public void updateStudentInfo(View view) {
        final String newName = et_name.getText().toString().trim();
        final String newDept = et_dept.getText().toString().trim();
        final String newPhone = et_phone.getText().toString().trim();

        // Validasi
        if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newDept) || TextUtils.isEmpty(newPhone)) {
            Toast.makeText(this, "Name, Department, and Phone cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        pd.setMessage("Updating student data...");
        pd.setCancelable(false);
        pd.show();

        // Siapkan data yang akan diupdate (Hanya field yang BISA diubah)
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("department", newDept);
        updates.put("phone", newPhone);

        // Ambil referensi database menggunakan kunci enrollment siswa
        DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference().child(studentKey);

        // Lakukan pembaruan (patch) ke Firebase
        studentRef.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(EditStudentActivity.this, "Student information updated successfully!", Toast.LENGTH_LONG).show();
                    // Setelah sukses, tutup Activity Edit dan kembali ke StudentInformationActivity
                    // (yang akan me-refresh data di onResume jika diimplementasikan).
                    finish();
                } else {
                    Log.e(TAG, "Update failed: ", task.getException());
                    Toast.makeText(EditStudentActivity.this, "Failed to update: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}