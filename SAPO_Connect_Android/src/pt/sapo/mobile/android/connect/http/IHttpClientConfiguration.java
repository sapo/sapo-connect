package pt.sapo.mobile.android.connect.http;

/**
 * Interface for the HttpClientConfiguration in every application.
 * 
 * @author Rui Roque
 */
public interface IHttpClientConfiguration {
	
	public void setConnectionTimeout(int timeout);
	
	public int getConnectionTimeout();

	public void setSocketTimeout(int timeout);
	
	public int getSocketTimeout();
	
}
