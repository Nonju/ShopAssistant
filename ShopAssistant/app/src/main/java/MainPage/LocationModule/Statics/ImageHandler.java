package MainPage.LocationModule.Statics;

import android.graphics.Matrix;
import android.widget.ImageView;

/**
 * Created by Hannes on 2016-12-15.
 *
 * Handles functions related to imageViews.
 */

public class ImageHandler {

    public static void rotateImage(ImageView imageView, double angle) {
        Matrix matrix = new Matrix();
        imageView.setScaleType(ImageView.ScaleType.MATRIX); // required
        matrix.postRotate((float) angle, imageView.getDrawable().getBounds()
                .width() / 2, imageView.getDrawable().getBounds().height() / 2);
        imageView.setImageMatrix(matrix);
    }
}
