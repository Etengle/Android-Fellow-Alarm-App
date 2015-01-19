package netdb.course.softwarestudio.service.rest.model;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

public class Image extends File {
    private static final String TAG = Image.class.getSimpleName();

    private Bitmap bitmap;

    public Image(String id, Bitmap bitmap) {
        this.id = id;
        this.bitmap = bitmap;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, bos);
        this.bytes = bos.toByteArray();
        mimeType = "image/jpeg";
    }

    public static String getCollectionName() {
        return "images";
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
