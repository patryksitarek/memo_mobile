package com.example.notes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import static com.google.common.primitives.Ints.min;


public class Calendar extends AppCompatActivity {

    CompactCalendarView compactCalendar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

    String[] notesDate = {"1587938400000", "1587992400000", "1587938400000", "1587992400000", "1588204800000", "1588111200000", "1588024800000", "1587999600000"};
    String[] notesTitle = {"title1", "title2", "title3", "title4", "title5", "title6", "title7", "title8"};
    String[] notesContent = {"Content1", "Content2", "Content3", "Content4", "Content5", "Content6", "Content7", "Content8"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);

        compactCalendar = (CompactCalendarView) findViewById(R.id.compactcalendar_view);
        compactCalendar.setUseThreeLetterAbbreviation(true);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle(null);
        actionBar.setTitle(dateFormatMonth.format(compactCalendar.getFirstDayOfCurrentMonth()));

        markEvents();

        compactCalendar.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                Context context = getApplicationContext();

                ConstraintLayout noteView1 = findViewById(R.id.note1);
                ConstraintLayout noteView2 = findViewById(R.id.note2);

                TextView noteView1Title = findViewById(R.id.note1ViewTitle);
                TextView noteView1Content = findViewById(R.id.note1ViewContent);
                TextView noteView1Date = findViewById(R.id.note1ViewDate);
                TextView noteView2Title = findViewById(R.id.note2ViewTitle);
                TextView noteView2Content = findViewById(R.id.note2ViewContent);
                TextView noteView2Date = findViewById(R.id.note2ViewDate);

                boolean found = false;
                List<Integer> eventPosition = new ArrayList<Integer>();
                for (int i = 0; i < notesDate.length; i++) {
                    if (notesDate[i].substring(0,11).equals(dateClicked.toString().substring(0,11))) {
                        found = true;
                        eventPosition.add(i);
                    }
                }

                noteView1.setVisibility(View.GONE);
                noteView2.setVisibility(View.GONE);

                if (found) {

                    for (int i = 0; i < min(2, eventPosition.size()); i++)
                        if (0 == i) {
                            noteView1.setVisibility(View.VISIBLE);
                            noteView1Title.setText(notesTitle[eventPosition.get(0)]);
                            noteView1Content.setText(notesContent[eventPosition.get(0)]);
                            noteView1Date.setText(notesDate[eventPosition.get(0)]);
                        } else if (1 == i) {
                            noteView2.setVisibility(View.VISIBLE);
                            noteView2Title.setText(notesTitle[eventPosition.get(1)]);
                            noteView2Content.setText(notesContent[eventPosition.get(1)]);
                            noteView2Date.setText(notesDate[eventPosition.get(1)]);
                        }
                }
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                actionBar.setTitle(dateFormatMonth.format(firstDayOfNewMonth));
            }
        });
    }

    void markEvents() {

        db.collection("notes")
                .whereArrayContains("author", db.document("users/"+user.getUid()))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                System.out.println(document.getId() + " => " + document.getData());
                                Long data = document.getTimestamp("created").getSeconds() * 1000;
                                
                            }
                        } else {
                            System.out.println("Error getting documents."+ task.getException());
                        }
                    }
                });

        for (int i = 0; i < notesDate.length; i++) {
            Event ev = new Event(Color.parseColor("#41B883"), Long.parseLong(notesDate[i]), notesTitle[i]);
            compactCalendar.addEvent(ev);

            Date result = new Date(Long.parseLong(notesDate[i]));
            notesDate[i] = result.toString();
        }
    }
}