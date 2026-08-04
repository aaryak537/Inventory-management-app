package com.example.inventory;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotifyHelper {

    public static void addNotification(String title,
                                       String message) {

        DatabaseReference ref =
                FirebaseDatabase.getInstance()
                        .getReference("Notifications");

        String id = ref.push().getKey();

        String time = new SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault())
                .format(new Date());

        NotifyModel model =
                new NotifyModel(
                        title,
                        message,
                        time,
                        false);

        ref.child(id).setValue(model);
    }
}