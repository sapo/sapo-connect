package pt.sapo.mobile.android.connect.exception;

public class SapoException extends Throwable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * An optional error message for the Exception.
	 */
	private String message;
	
    /**
     * An optional Error Code for the Exception.
     */
    private Integer errorCode;
        
    /**
     * Available Error Codes.
     */
    public static final int ERROR_CODE_READ       = 0;
    public static final int ERROR_CODE_WRITE      = 1;
    public static final int ERROR_CODE_ZIP        = 2;
    public static final int ERROR_CODE_ENCODE     = 3;
    public static final int ERROR_CODE_REFLECTION = 4;
    
    
    // ------------------------------------------------------------------------------------------------------- //
    //                                              CONSTRUCTORS                                               //
    // ------------------------------------------------------------------------------------------------------- //
    
    /**
     * Constructs a SapoException with an already existing Throwable.
     * 
     * @param throwable A Java throwable. 
     */
    public SapoException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Constructs a SapoException with an already existing Throwable and an exception message.
     * 
     * @param message The exception message.
     * @param throwable A Java throwable.
     */
    public SapoException(String message, Throwable throwable) {
        super(message, throwable);
        setMessage(message);
    }

    /**
     * Constructs a SapoException with an exception message.
     * 
     * @param message The exception message.
     */
    public SapoException(String message) {
        super(message);
        setMessage(message);
    }
    
    /**
     * Constructs a SapoException with an exception message and an error code.
     * 
     * @param message The exception message.
     * @param errorCode The exception error code.
     */
    public SapoException(String message, Integer errorCode) {
        setMessage(message);
        setErrorCode(errorCode);
    }
    
    /**
     * Default constructor.
     */
    public SapoException() {
    	super();
    }
   
    
    // ------------------------------------------------------------------------------------------------------- //
    //                                           SETTERS & GETTERS                                             //
    // ------------------------------------------------------------------------------------------------------- //
        
    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public Integer getErrorCode() {
        return errorCode;
    }
    
}
