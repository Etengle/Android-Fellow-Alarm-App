package netdb.course.softwarestudio.service.rest.model;

public abstract class File extends PostResource {
	private static final String TAG = File.class.getSimpleName();

	protected byte[] bytes;
	protected String mimeType;

	public String getName() {
		return id.substring(id.lastIndexOf("/") + 1);
	}

	public byte[] getBytes() {
		return bytes;
	}

	public String getMimeType() {
		return mimeType;
	}
}
