package netdb.course.softwarestudio.service.rest.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;

/**
 * A hack of {@link com.android.volley.toolbox.NetworkImageView} that accepts no default and error
 * drawables. Instead, it fires {@link ImageLoadListener} events to allow the controlling of other
 * views, for example, removing the {@link android.widget.ProgressBar} after the image specified by
 * {@link #setImageUrl(String, com.android.volley.toolbox.ImageLoader, ImageLoadListener)} is loaded.
 * <p/>
 * <p>Handles fetching an image from a URL as well as the life-cycle of the associated request.</p>
 */
public class RestImageView extends ImageView {
	private static final String TAG = RestImageView.class.getSimpleName();
	private ImageLoadListener mLoadListener;
	/**
	 * The URL of the network image to load
	 */
	private String mUrl;
	/**
	 * Local copy of the ImageLoader.
	 */
	private ImageLoader mImageLoader;

	public RestImageView(Context context) {
		this(context, null);
	}

	public RestImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RestImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * Sets URL of the image that should be loaded into this view. Note that calling this will
	 * immediately either set the cached image (if available) or the default image specified by
	 * {@link com.android.volley.toolbox.NetworkImageView#setDefaultImageResId(int)} on the view.
	 * <p/>
	 * NOTE: If applicable, {@link com.android.volley.toolbox.NetworkImageView#setDefaultImageResId(int)} and {@link
	 * com.android.volley.toolbox.NetworkImageView#setErrorImageResId(int)} should be called prior to calling this function.
	 *
	 * @param url         The URL that should be loaded into this ImageView.
	 * @param imageLoader ImageLoader that will be used to make the request.
	 * @param listener
	 */
	public void setImageUrl(String url, ImageLoader imageLoader, ImageLoadListener listener) {
		mUrl = url;
		mImageLoader = imageLoader;
		mLoadListener = listener;
		// The URL has potentially changed. See if we need to load it.
		loadImageIfNecessary();
	}

	/**
	 * Loads the image for the view if it isn't already loaded.
	 */
	private void loadImageIfNecessary() {
		int width = getWidth();
		int height = getHeight();
		// if the view's bounds aren't known yet, hold off on loading the image.
		if (width == 0 && height == 0) {
			return;
		}
		// if the URL to be loaded in this view is empty, cancel any old requests and clear the
		// currently loaded image.
		if (TextUtils.isEmpty(mUrl)) {
			ImageContainer oldContainer = (ImageContainer) getTag();
			if (oldContainer != null) {
				oldContainer.cancelRequest();
				setImageBitmap(null);
			}
			return;
		}
		ImageContainer oldContainer = (ImageContainer) getTag();
		// if there was an old request in this view, check if it needs to be canceled.
		if (oldContainer != null && oldContainer.getRequestUrl() != null) {
			if (oldContainer.getRequestUrl().equals(mUrl)) {
				// if the request is from the same URL, restore bitmap locally and return.
				if (oldContainer.getBitmap() != null) setImageBitmap(oldContainer.getBitmap());
				return;
			} else {
				// if there is a pre-existing request, cancel it if it's fetching a different URL.
				oldContainer.cancelRequest();
				setImageBitmap(null);
			}
		}
		// The pre-existing content of this view didn't match the current URL. Load the new image
		// from the network.
		ImageContainer newContainer = mImageLoader.get(mUrl, new ImageLoader.ImageListener() {
			@Override
			public void onErrorResponse(VolleyError error) {

				int code = error.networkResponse.statusCode;

				if (code >= 300 && code < 400) {
					String url = error.networkResponse.headers.get("Location");
					Log.d(TAG, "Redirect to: " + url);
					setImageUrl(url, mImageLoader, mLoadListener);
					
				} else {

					Log.d(TAG, "Error: " + (error.networkResponse == null ? error.getMessage() :
							error.networkResponse.statusCode), error.getCause());
					if (mLoadListener != null) {
						mLoadListener.onError();
					}
				}
			}

			@Override
			public void onResponse(ImageContainer response, boolean isImmediate) {
				if (response.getBitmap() != null) {
					Log.d(TAG, "Response from: " + mUrl);
					setImageBitmap(response.getBitmap());
					if (mLoadListener != null) {
						mLoadListener.onLoad();
					}
				}
			}
		});
		// update the tag to be the new bitmap container.
		setTag(newContainer);
		// look at the contents of the new container. if there is a bitmap, load it.
		final Bitmap bitmap = newContainer.getBitmap();
		if (bitmap != null) {
			setImageBitmap(bitmap);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		loadImageIfNecessary();
	}

	@Override
	protected void onDetachedFromWindow() {
		ImageContainer oldContainer = (ImageContainer) getTag();
		if (oldContainer != null) {
			// If the view was bound to an image request, cancel it and clear
			// out the image from the view.
			oldContainer.cancelRequest();
			setImageBitmap(null);
			// also clear out the tag so we can reload the image if necessary.
			setTag(null);
		}
		super.onDetachedFromWindow();
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		invalidate();
	}

	public interface ImageLoadListener {
		void onLoad();

		void onError();
	}
}
