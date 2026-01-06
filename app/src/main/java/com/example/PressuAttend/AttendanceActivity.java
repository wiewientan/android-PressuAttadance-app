package com.example.PressuAttend;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AttendanceActivity extends AppCompatActivity {

    ListView lv;
    Button submit;
    int position;
    List<StudentInformation> stdinfo = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        Intent intent = getIntent();
        position = Integer.parseInt(intent.getStringExtra("position"));

        lv = findViewById(R.id.listView2);
        submit = findViewById(R.id.button6);

        FirebaseDatabase.getInstance()
                .getReference()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        stdinfo.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            StudentInformation std = child.getValue(StudentInformation.class);
                            if (std != null && std.getSubjects().get(position) != -1) {
                                std.setPresent(false); // default
                                stdinfo.add(std);
                            }
                        }
                        lv.setAdapter(new MyBaseAdapter(AttendanceActivity.this));
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    public void submitAttendance(View view) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for (DataSnapshot child : snapshot.getChildren()) {
                    StudentInformation dbStd = child.getValue(StudentInformation.class);
                    if (dbStd == null) continue;

                    for (StudentInformation localStd : stdinfo) {
                        if (dbStd.getRollno().equals(localStd.getRollno())) {

                            int total = localStd.getSubjects().get(position);

                            if (localStd.isPresent()) {
                                total += 1001;
                            } else {
                                total += 1000;
                            }

                            child.getRef()
                                    .child("subjects")
                                    .child(String.valueOf(position))
                                    .setValue(total);
                        }
                    }
                }

                Toast.makeText(AttendanceActivity.this,
                        "Attendance Complete", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    // ================= ADAPTER =================

    class MyBaseAdapter extends BaseAdapter {

        Context context;
        LayoutInflater inflater;

        MyBaseAdapter(Context context) {
            this.context = context;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return stdinfo.size();
        }

        @Override
        public Object getItem(int position) {
            return stdinfo.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.cust_list_attendance, parent, false);
                holder = new ViewHolder();
                holder.name = convertView.findViewById(R.id.textView12);
                holder.roll = convertView.findViewById(R.id.textView13);
                holder.cb = convertView.findViewById(R.id.checkBoxA);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            StudentInformation std = stdinfo.get(position);

            holder.name.setText(std.getName());
            holder.roll.setText(std.getRollno());

            holder.cb.setOnCheckedChangeListener(null);
            holder.cb.setChecked(std.isPresent());

            holder.cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                std.setPresent(isChecked);
            });

            return convertView;
        }
    }

    static class ViewHolder {
        TextView name, roll;
        CheckBox cb;
    }
}
