package util;

import mpicbg.imglib.util.Util;
import mpicbg.spim.registration.detection.DetectionSegmentation;

public class ScrollbarUtils {

	
	public static float computeValueFromScrollbarPosition(final int scrollbarPosition, final float min,
			final float max, final int scrollbarSize) {
		return min + (scrollbarPosition / (float) scrollbarSize) * (max - min);
	}

	public static float computeIntValueFromScrollbarPosition(final int scrollbarPosition, final float min,
			final float max, final int scrollbarSize) {
		return min + (scrollbarPosition / (max)) * (max - min);
	}

	public static int computeScrollbarPositionFromValue(final float sigma, final float min, final float max,
			final int scrollbarSize) {
		return Util.round(((sigma - min) / (max - min)) * scrollbarSize);
	}

	public static int computeIntScrollbarPositionFromValue(final float thirdDimensionslider, final float min,
			final float max, final int scrollbarSize) {
		return Util.round(((thirdDimensionslider - min) / (max - min)) * max);
	}
	
	public static float computeSigma2(final float sigma1, final int sensitivity) {
		final float k = (float) DetectionSegmentation.computeK(sensitivity);
		final float[] sigma = DetectionSegmentation.computeSigma(k, sigma1);

		return sigma[1];
	}
	
}
