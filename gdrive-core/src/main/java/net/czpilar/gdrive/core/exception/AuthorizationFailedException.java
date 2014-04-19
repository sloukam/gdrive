package net.czpilar.gdrive.core.exception;

/**
 * Exception used when authorization fails.
 *
 * @author David Pilar (david@czpilar.net)
 */
public class AuthorizationFailedException extends GDriveException {

	public AuthorizationFailedException(String message) {
		super(message);
	}

	public AuthorizationFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public AuthorizationFailedException(Throwable cause) {
		super(cause);
	}

}
