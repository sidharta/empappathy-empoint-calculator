package com.empappathy.empoint.calculator;

import com.empappathy.empoint.calculator.model.Comment;
import com.empappathy.empoint.calculator.model.FaceType;
import com.empappathy.empoint.calculator.model.Image;

import java.util.*;

/**
 * Created by snoleto on 05/03/17.
 */
public class EmpointCalculatorService {

    private static String[] GOOD_LABELS = { "smile", "love", "kiss", "play", "kid", "baby" };

    private static final EmpointCalculatorService INSTANCE = new EmpointCalculatorService();

    private EmpointCalculatorService() {
    }

    public static EmpointCalculatorService getInstance() {
        return INSTANCE;
    }

    public Long calculateImageEmpoints(Image image) {
        Double points = 0d;

        // for each face detected
        if (image.facesTypes != null) {
            for (FaceType f : image.facesTypes) {
                // earns double points if joy or surprise
                points += (f.joy ? f.confidence * 2 : 0);
                points += (f.surprise ? f.confidence * 2 : 0);

                // earns points if your wearing somithing in your head
                points += (f.headwear ? f.confidence : 0);

                // loses half points if your image is blurred or underexposed
                points -= (f.blurred ? f.confidence / 2 : 0);
                points -= (f.underExposed ? f.confidence / 2 : 0);

                // loses points if the image contains anger or sorrow
                points -= (f.anger ? f.confidence : 0);
                points -= (f.sorrow ? f.confidence : 0);
            }
        }

        // for specific labels
        if (image.labels != null) {
            for (Object l : image.labels) {
                points += (Arrays.asList(GOOD_LABELS).contains(l.toString()) ? 100 : 0);
            }
        }

        return points.longValue();
    }

    public String[] processImageTags(Image image) {
        Set<String> tags = new HashSet<>();

        // for each face detected
        if (image.facesTypes != null) {
            for (FaceType f : image.facesTypes) {
                if (f.joy)
                    tags.add("joy");
                if (f.surprise)
                    tags.add("surprise");
                if (f.headwear)
                    tags.add("headwear");
            }
        }

        // for specific labels
        if (image.labels != null) {
            for (Object l : image.labels) {
                if (l.toString().equals("baby"))
                    tags.add("baby");
                if (l.toString().equals("kid"))
                    tags.add("kid");
                if (l.toString().equals("kiss"))
                    tags.add("kiss");
                if (l.toString().equals("smile"))
                    tags.add("smile");
            }
        }

        return tags.toArray(new String[] {});
    }

    public Long calculateCommentEmpoints(Comment comm) {
        Double empoints = 0d;

        if (comm.score != null) {
            // really bad comment or really good comment
            if (comm.score < -50 || comm.score > 50) {
                empoints += comm.score * 1.5;
            } else { // normal or bad comment
                empoints += comm.score;
            }
        }

        return empoints.longValue();
    }
}
