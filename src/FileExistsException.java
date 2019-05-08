/**
 * An exception class which is used in the event that a file we are trying to create already exists.
 */
public class FileExistsException extends Exception {

    /**
     * Constructor which uses the default constructor of the father class in order to set the message of the exception to a default message.
     */
    public FileExistsException(){
        super("Exception: There is already an existing file for that author. File will\n" +
                "be renamed as BU, and older BU files will be deleted!");
    }

    /**
     * Constructor which uses the parametrized constructor of the father class in order to set the message of the exception to something we want.
     */
    public FileExistsException(String s){
        super(s);
    }

    /**
     * A method which returns the message of the exception.
     *
     * @return a string which contains the message of the exception
     */
    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
