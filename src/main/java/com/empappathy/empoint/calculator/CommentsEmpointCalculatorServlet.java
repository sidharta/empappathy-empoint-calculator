package com.empappathy.empoint.calculator;

import com.empappathy.empoint.calculator.model.Comment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by snoleto on 05/03/17.
 */
public class CommentsEmpointCalculatorServlet extends HttpServlet {

    public java.util.logging.Logger log = Logger.getLogger(CommentsEmpointCalculatorServlet.class
            .getSimpleName());

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        initFirebase();

        // find comments with empoints null and valid score
        FirebaseDatabase
            .getInstance()
            .getReference("comment-score")
            .addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterator<DataSnapshot> children = dataSnapshot.getChildren().iterator();

                    while (children.hasNext()) {
                        final DataSnapshot childSnapshot = children.next();
                        Comment comm = childSnapshot.getValue(Comment.class);

                        final Long points = EmpointCalculatorService.getInstance().calculateCommentEmpoints(comm);

                        log.info("Processing comment: " + childSnapshot.getKey());

                        DatabaseReference postRef = FirebaseDatabase
                                .getInstance()
                                .getReference("posts/" + comm.postKey + "/empoints");

                        if (points != 0) { // if zero we don't need to update
                            log.info("Processing post: " + comm.postKey);

                            postRef.runTransaction(new Transaction.Handler() {
                                @Override public Transaction.Result doTransaction(MutableData mutableData) {
                                    Integer currentValue = mutableData.getValue(Integer.class);
                                    if (currentValue == null) {
                                        mutableData.setValue(points);
                                    } else {
                                        mutableData.setValue(currentValue + points);
                                    }
                                    return Transaction.success(mutableData);
                                }

                                @Override
                                public void onComplete(DatabaseError databaseError, boolean b,
                                        DataSnapshot dataSnapshot) {
                                    if (databaseError != null) {
                                        log.severe("Error: " + databaseError);
                                    } else {
                                        Map<String, Object> upd = new HashMap<>();
                                        upd.put("empoints", points);

                                        FirebaseDatabase.getInstance().getReference("user-comments/" + childSnapshot.getKey()).getRef().updateChildren(upd);

                                        childSnapshot.getRef().removeValue();
                                    }
                                }
                            });
                        }
                    }

                }

                @Override public void onCancelled(DatabaseError databaseError) {
                    log.severe("Error: " + databaseError);
                }

            });
    }

    private void initFirebase() {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setServiceAccount(getServletContext().getResourceAsStream("/WEB-INF/firebase-credentials.json"))
                .setDatabaseUrl("https://empappathy.firebaseio.com/")
                .build();

        try {
            FirebaseApp.getInstance();
        } catch (Exception error) {
        }

        try {
            FirebaseApp.initializeApp(options);
        } catch (Exception error) {
        }
    }

}
