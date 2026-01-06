package com.example.PressuAttend;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton; // Import untuk ImageButton
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class StudentInformationActivity extends AppCompatActivity {
    StudentInformation std = new StudentInformation();

    String searchBy = "";
    String value = "";
    TextView tvname, tvroll, tvdept, tvcontact;
    ListView lv;
    Button btnBack;
    // ImageButton tidak perlu dideklarasikan di sini jika hanya menggunakan android:onClick di XML.

    // Variabel untuk menyimpan kunci siswa (Enrollment ID/User ID)
    String studentEnrollmentKey = "";

    private static final String TAG = "StudentInfoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_information);

        // default sentinel
        std.setName("AttapattuD");

        final ProgressDialog pd = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
        pd.setMessage("Fetching Information");
        pd.setCancelable(false);
        pd.show();

        Intent intent = getIntent();
        searchBy = intent.getStringExtra("SearchBy");
        if (searchBy == null) searchBy = intent.getStringExtra("Name");
        if (searchBy == null) searchBy = intent.getStringExtra("searchBy");
        if (searchBy == null) searchBy = "";

        Log.d(TAG, "searchBy: " + searchBy);

        if ("Email".equals(searchBy)) {
            String e = intent.getStringExtra("Email");
            value = (e != null) ? e.trim() : "";
        } else {
            String r = intent.getStringExtra("RollNo");
            value = (r != null) ? r.trim() : "";
        }
        Log.d(TAG, "search value: " + value);

        // --- Menghubungkan Tombol Back (Hanya Tombol Teks yang membutuhkan Listener) ---
        btnBack = findViewById(R.id.button_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goBack(v);
                }
            });
        }
        // Ikon Edit/Delete terhubung melalui android:onClick di XML
        // --- Akhir Menghubungkan Tombol ---

        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = database.getReference(); // root

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        Log.d(TAG, "root exists=" + dataSnapshot.exists() + " children=" + dataSnapshot.getChildrenCount());

                        if (!dataSnapshot.exists()) {
                            Log.w(TAG, "Snapshot root is empty");
                            pd.dismiss();
                            Toast.makeText(StudentInformationActivity.this, "Data not found on server", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        boolean found = false;

                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            Log.d(TAG, "child key=" + child.getKey() + " value=" + child.getValue());

                            StudentInformation std1 = null;
                            try {
                                std1 = child.getValue(StudentInformation.class);
                            } catch (Exception e) {
                                Log.w(TAG, "Auto-map exception for " + child.getKey() + ": " + e.getMessage());
                            }

                            if (std1 == null) {
                                // build manual object from snapshot
                                Log.w(TAG, "Auto-mapping null for key=" + child.getKey() + " — building manual object");

                                StudentInformation manual = new StudentInformation();
                                if (child.child("name").exists()) manual.setName(child.child("name").getValue(String.class));
                                if (child.child("rollno").exists()) manual.setRollno(child.child("rollno").getValue(String.class));
                                if (child.child("email").exists()) manual.setEmail(child.child("email").getValue(String.class));
                                if (child.child("phone").exists()) manual.setPhone(child.child("phone").getValue(String.class));
                                if (child.child("department").exists()) manual.setDepartment(child.child("department").getValue(String.class));
                                if (child.child("password").exists()) manual.setPassword(child.child("password").getValue(String.class));

                                // --- Handle subjects robustly ---
                                ArrayList<Integer> subjectsInt = new ArrayList<>();
                                if (child.child("subjects").exists()) {
                                    DataSnapshot subjSnap = child.child("subjects");
                                    for (DataSnapshot sChild : subjSnap.getChildren()) {
                                        Object raw = sChild.getValue();
                                        if (raw instanceof Long) subjectsInt.add(((Long) raw).intValue());
                                        else if (raw instanceof Integer) subjectsInt.add((Integer) raw);
                                        else {
                                            try { subjectsInt.add(Integer.parseInt(String.valueOf(raw))); } catch (Exception ignore) {}
                                        }
                                    }
                                }
                                manual.setSubjects(subjectsInt);
                                std1 = manual;
                            } else {
                                // mapped success — ensure subjects not null
                                if (std1.getSubjects() == null && child.child("subjects").exists()) {
                                    ArrayList<Integer> subjectsInt = new ArrayList<>();
                                    for (DataSnapshot sChild : child.child("subjects").getChildren()) {
                                        Object raw = sChild.getValue();
                                        if (raw instanceof Long) subjectsInt.add(((Long) raw).intValue());
                                        else if (raw instanceof Integer) subjectsInt.add((Integer) raw);
                                        else {
                                            try { subjectsInt.add(Integer.parseInt(String.valueOf(raw))); } catch (Exception ignore) {}
                                        }
                                    }
                                    std1.setSubjects(subjectsInt);
                                }
                            }

                            // Now we have a StudentInformation object (std1) if the node contained student fields
                            if (std1 != null) {
                                String stdEmail = std1.getEmail();
                                String stdRoll = std1.getRollno();

                                if ("Email".equalsIgnoreCase(searchBy) && stdEmail != null && stdEmail.equalsIgnoreCase(value)) {
                                    std = std1;
                                    studentEnrollmentKey = child.getKey(); // SIMPAN KUNCI ENROLLMENT
                                    found = true;
                                    break;
                                } else if ("RollNo".equalsIgnoreCase(searchBy) && stdRoll != null && stdRoll.equalsIgnoreCase(value)) {
                                    std = std1;
                                    studentEnrollmentKey = child.getKey(); // SIMPAN KUNCI ENROLLMENT
                                    found = true;
                                    break;
                                }
                            }
                        }

                        pd.dismiss();

                        if (!found) {
                            if ("RollNo".equalsIgnoreCase(searchBy)) {
                                Toast.makeText(getApplicationContext(), "Roll No. not found", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(StudentInformationActivity.this, SelectRollNo.class));
                                finish();
                                return;
                            } else {
                                Toast.makeText(StudentInformationActivity.this, "Student not found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        fn();

                    } catch (Exception e) {
                        pd.dismiss();
                        Log.e(TAG, "Exception onDataChange: ", e);
                        Toast.makeText(StudentInformationActivity.this, "Error processing data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    pd.dismiss();
                    Log.e(TAG, "Firebase Error: " + databaseError.getMessage());
                    Toast.makeText(StudentInformationActivity.this, "Gagal mengambil data: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            pd.dismiss();
            Log.e(TAG, "Exception is", e);
            Toast.makeText(this, "Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // --- FUNGSI UNTUK TOMBOL BACK ---
    public void goBack(View view) {
        finish();
    }
    // --- AKHIR FUNGSI TOMBOL BACK ---

    // --- FUNGSI BARU: EDIT STUDENT ---
    public void editStudent(View view) {
        if (studentEnrollmentKey.isEmpty()) {
            Toast.makeText(this, "Student data not fully loaded. Cannot edit.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Buka Activity baru (EditStudentActivity) dan kirim data siswa
        Intent intent = new Intent(StudentInformationActivity.this, EditStudentActivity.class);
        intent.putExtra("student_key", studentEnrollmentKey);
        intent.putExtra("student_name", std.getName());
        intent.putExtra("student_rollno", std.getRollno());
        intent.putExtra("student_dept", std.getDepartment());
        intent.putExtra("student_phone", std.getPhone());
        intent.putExtra("student_email", std.getEmail());
        intent.putExtra("student_password", std.getPassword());

        // Catatan: Anda perlu membuat kelas EditStudentActivity.java
        startActivity(intent);
    }

    // --- FUNGSI BARU: DELETE STUDENT ---
    public void deleteStudent(View view) {
        if (studentEnrollmentKey.isEmpty()) {
            Toast.makeText(this, "Student data not fully loaded. Cannot delete.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tampilkan dialog konfirmasi
        new AlertDialog.Builder(this)
                .setTitle("Delete Student")
                .setMessage("Are you sure you want to delete student data? " + safeStr(std.getName()) + " permanently? This action cannot be undone.")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        executeDelete(studentEnrollmentKey);
                    }
                })
                .setNegativeButton("Cancle", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void executeDelete(String key) {
        final ProgressDialog deletePd = new ProgressDialog(this);
        deletePd.setMessage("Deleting student data...");
        deletePd.setCancelable(false);
        deletePd.show();

        FirebaseDatabase.getInstance().getReference().child(key).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        deletePd.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(StudentInformationActivity.this, "Student deleted successfully", Toast.LENGTH_LONG).show();
                            // Setelah hapus, kembali ke layar pemilihan Roll No
                            startActivity(new Intent(StudentInformationActivity.this, SelectRollNo.class));
                            finish();
                        } else {
                            Log.e(TAG, "Failed to delete student: ", task.getException());
                            Toast.makeText(StudentInformationActivity.this, "Failed to delete student: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    void fn() {
        try {
            if (std == null) {
                Toast.makeText(this, "Student object null", Toast.LENGTH_SHORT).show();
                return;
            }
            if ("RollNo".equalsIgnoreCase(searchBy) && "AttapattuD".equals(std.getName())) {
                Toast.makeText(getApplicationContext(), "Roll No. not found", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(StudentInformationActivity.this, SelectRollNo.class));
                finish();
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in fn (pre-check): ", e);
        }

        tvname = findViewById(R.id.textView14);
        tvroll = findViewById(R.id.textView17);
        tvdept = findViewById(R.id.textView18);
        tvcontact = findViewById(R.id.textView20);
        lv = findViewById(R.id.listView3);

        tvname.setText(safeStr(std.getName()));
        tvroll.setText(safeStr(std.getRollno()));
        tvdept.setText(safeStr(std.getDepartment()));
        tvcontact.setText(safeStr(std.getPhone()));

        ArrayList<Integer> al = std.getSubjects();
        List<String> subjectList = new ArrayList<>();
        int count = 0;
        int countTot = 0;

        if (al == null) {
            Log.w(TAG, "subjects list is null");
            al = new ArrayList<>();
        }

        int subjectCount = Math.min(al.size(), 7);

        for (int i = 0; i < subjectCount; i++) {
            Integer valObj = al.get(i);
            if (valObj == null) continue;
            int val = valObj;
            if (val != -1) {
                int attended = val % 1000;
                int total = val / 1000;
                count += attended;
                countTot += total;
                double percent = 0.0;
                if (total != 0) percent = ((double) attended / total) * 100.0;
                String str = "IT0" + (i + 1) + "   ----   " + attended + " / " + total + " Percentage " + new DecimalFormat("##.##").format(percent) + "%";
                subjectList.add(str);
            }
        }

        if (countTot == 0) countTot = 1;
        int overallPercent = (count * 100) / countTot;
        Toast.makeText(StudentInformationActivity.this, "Your Total Attendance is--" + overallPercent + "%", Toast.LENGTH_LONG).show();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, subjectList);
        lv.setAdapter(arrayAdapter);
    }

    private String safeStr(String s) {
        return s == null ? "" : s;
    }
}