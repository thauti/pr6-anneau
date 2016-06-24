package exceptions;

public class WrongFormatException extends Exception {
	
	public WrongFormatException(String m){
		super("L'en-tete du message '"+m+"' n'est pas valide.");
	}

}
