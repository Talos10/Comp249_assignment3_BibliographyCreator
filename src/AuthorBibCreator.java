import java.io.*;
import java.util.Scanner;

/**
 * A class which uses its static method to search through some files containing articles and to create three output files based on the three different reference styles.
 *
 * @author Razvan Ivan
 * @since 2019/03/21
 */
public class AuthorBibCreator {


    /**
     * Main method which creates the files that represent the input files and the output files with the help of the static method processBibFiles.
     *
     * @see FileExistsException
     * @author Razvan Ivan
     * @since 2019/03/21
     */
    public static void main(String[] args) {
        int nbrOfInputFiles;//int variable which contains the number of input files that the user wants to the program to read
        String searchName;//a string which contains the name of the author that the user wants to search with
        Scanner kb = new Scanner(System.in);

        //Prints out a welcome message to the users.
        System.out.println("\n*********************************************************************************");
        System.out.println("\nWelcome to the best bibliography creating software out there: the BIBCREATOR9000!");
        System.out.println("\n*********************************************************************************");

        //Asks the user how many input articles there are
        System.out.println("\nPlease enter the number of input articles (make sure that they are named LatexNBR.bib where NBR is the number of the file, starting from 1): ");
        nbrOfInputFiles = kb.nextInt();

        File[] inputFiles = new File[nbrOfInputFiles];//creating an array of file objects for the input files
        Scanner[] sc = new Scanner[inputFiles.length];//creating an array of scanner objects to read from the input files
        File[] originals = new File[3];//creating an array of file objects for the output files
        String[] originalExtensions = {"-IEEE.json", "-ACM.json", "-NJ.json"};//creating a string array for the extensions of the output files
        File[] backups = new File[3];//creating an array of file objects for the backup output files
        String[] backupExtensions = {"-IEEE-BU.json", "-ACM-BU.json", "-NJ-BU.json"};//creating a string array for the extensions of the backup output files
        PrintWriter[] pw = new PrintWriter[originals.length];//creating an array of printwriter objects to write to the output files
        boolean authorNotFound = false;//boolean variable used to print a message to the user informing them that nothing was found with the author given
        String allCapsSearchName, smallCapsSearchName, startCaseSearchName;//strings where each contains a different format of the name of the author (all caps, all lower, and first cap and rest small)
        int nbrOfRecordsFound;//int variable which holds the amount of unique article references that were found by the author the user has given

        kb.nextLine();//removing the enter character from before when the program took the int for the nbrOfInputFiles

        //Asks the user to input an author name.
        System.out.println("\nPlease enter the author name that you want to search for (case insensitive: ");
        searchName = kb.next();

        //Changes the case of the given author's name.
        allCapsSearchName = searchName.toUpperCase();
        smallCapsSearchName = searchName.toLowerCase();
        startCaseSearchName = allCapsSearchName.substring(0, 1) + smallCapsSearchName.substring(1, smallCapsSearchName.length());

        //Checking if all input files exist. If not, exit program.
        for (int i = 0; i < inputFiles.length; i++) {
            inputFiles[i] = new File("Latex" + (i + 1) + ".bib");
            if (!inputFiles[i].exists()) {
                System.out.println("\nCould not open input file #" + (i + 1) + " for reading. Please check if file exists! Program will terminate after closing any opened input files.");
                System.exit(-1);
            }
        }

        //Opening all the input files.
        for (int i = 0; i < inputFiles.length; i++) {
            try {
                sc[i] = new Scanner(new FileInputStream(inputFiles[i]));
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage());
            }
        }

        //Checking if any originals searches or any backups searches already exist with the author's name.
        //Any existing backups are deleted and any existing originals are made into backups.
        //Once this for loop is completely executed, there will be no originals left (only backups of the originals).
        for (int i = 0; i < originals.length; i++) {

            originals[i] = new File(startCaseSearchName + originalExtensions[i]);//naming the output files
            backups[i] = new File(startCaseSearchName + backupExtensions[i]);//naming the backups of the output files

            try {
                if (backups[i].exists())
                    throw new FileExistsException();
            } catch (FileExistsException e) {
                backups[i].delete();//deleting backups
            }

            try {
                if (originals[i].exists()) {
                    System.out.println("\nA file already exists with the name " + originals[i].getName() + ".");
                    throw new FileExistsException();
                }

            } catch (FileExistsException e) {//Renaming the old output files and making them backup files.
                originals[i].renameTo(backups[i] = new File(startCaseSearchName + backupExtensions[i]));
                originals[i] = new File(startCaseSearchName + originalExtensions[i]);//Creating new output files to use for the new search.
                System.out.println("\nThe file has been renamed " + backups[i].getName() + " and any old BUs have been deleted.");
            }
        }

        //Creating three output files. If an output file cannot be created, everything is closed and all output files that were created
        //before the problem happened are deleted.
        for (int i = 0; i < pw.length; i++) {
            try {
                pw[i] = new PrintWriter(new FileOutputStream(originals[i]));
            } catch (FileNotFoundException e) {
                System.out.println("\nFile number " + (i + 1) + " could not be opened/created. Deleting all created output files, closing all input files, and exiting program.");

                for (int j = 0; j <= i; j++)
                    originals[j].delete();

                for (int j = 0; j <= i; j++)
                    pw[j].close();

                for (int j = 0; j < 10; j++)
                    sc[j].close();

                System.exit(-2);
            }
        }

        nbrOfRecordsFound = processBibFiles(sc, pw, allCapsSearchName, smallCapsSearchName, startCaseSearchName);//Storing the number of unique article references that were made which is given by calling the method processBibFiles

        //If the number of records found is bigger than 1, the program tells the user how many were found, the name of the author used for
        //the search, and the name of the output files that contain the article references. The number of records has to be bigger than 1
        //since the method returns the value of plus 1 but the value starts at 0.
        if (nbrOfRecordsFound > 1){
            System.out.println("\nA total number of " + nbrOfRecordsFound + " have been found for the author with the name: " + startCaseSearchName);
            System.out.println("Files " + originals[0].getName() + ", " + originals[1].getName() + ", and " + originals[2].getName() + " have been created.");
        }

        //Closing all input files.
        for (int i = 0; i < 10; i++)
            sc[i].close();

        //Closing all output files. If one of the output files has a size of 0,
        //then it means that no articles were found by the author given and
        //thus no article references were made and so all output files are deleted.
        //If that is the case, then the boolean authorNotFound is set to true.
        for (int i = 0; i < 3; i++) {
            pw[i].close();
            if (originals[i].length() == 0) {
                authorNotFound = true;
                originals[i].delete();
            }
        }

        //Telling the user the author was not found if the author given did not
        //show up in the search.
        if (authorNotFound)
            System.out.println("\nThe author you searched for has not been found.");

        //Prints out a closing message.
        System.out.println("\n\n\n*******************************************************************************************");

        System.out.println("\nThank you for using the BIBCREATOR9000!");

        System.out.println("Tell your friends about us!");

        System.out.println("\n*******************************************************************************************");

        System.out.println("\n\n\n*********************************************************************");

        System.out.println("\nThis program was written by Razvan Ivan on the 21st of March 2019.");

        System.out.println("\n*************************END OF THE PROGRAM.*************************");
    }

    /**
     * A static method which searches through the input files that it is given and writes to the given output files with the different formats of the author's name.
     *
     * @param sc the array of scanner objects which represent the different input files
     * @param pw the array of printwriter objects which represent the different output files
     * @param allCapsSearchName the name of the author needed for the search but in an all caps format
     * @param smallCapsSearchName the name of the author needed for the search but in small caps format
     * @param startCaseSearchName the name of the author needed for the search but with the first letter in big cap and the rest in small caps
     * @return the number of article references that were created
     */
    public static int processBibFiles(Scanner[] sc, PrintWriter[] pw, String allCapsSearchName, String smallCapsSearchName, String startCaseSearchName) {

        String line, author, authorIEEE = "", authorACM = "", authorNJ = "", journal = "", title = "", year = "", volume = "", number = "", pages = "", doi = "", month = "";//creates string variables
        int refNbr = -1;//int variable which holds the amount of unique article references that were created. The reason it is initialized at -1 will be discussed later.
        boolean articleFromAuthor = false;//boolean variable which states if the article that is currently being read is from the targeted author or not.
        boolean authorFound = false;//boolean variable which states if the article that is currently being read is from the targeted author or not.

        //Outer for loop which contains the whole search.
        //Each iteration of the for loop makes the program search through
        //a new input file.
        for (int j = 0; j < sc.length; j++) {

            //A while loop which allows the program to read from a specific file until it has no more lines.
            //This while loop works in the way that it makes the program read the file line by line where
            //one single line is used and processed in every iteration of the while loop. To know when to stop and start
            //processing each line, the while loop uses if statements and does the following: if it detects that the
            //line contains the @ARTICLE string then it knows it just reached a new article and thus its job has began.
            //The while loop is divided into three sections: an if statement which takes the current line and formats it to the program's needs in
            //order to extract the elements needed to create the reference of the article the while loop is currently in. The two other parts
            //of the while loops are two if else statements. One of them was created specifically for when the while loop reaches the first
            //article of the first file. The other was created for the case when the while loop reaches an @ARTICLE string which means
            //it is time to print to the output files the created reference of the previous article before entering the new article.
            //The reason the first else if statement exists is linked to the reason that the variable refNbr is initialized to -1. When
            //the program reaches the first article of the first file, the program should not output to the output files since it
            //has not yet created any reference that needs to be outputted. Therefore, in order for the program to not enter the second
            //else if which allows the printing to the output files, we make it enter the first else if which simply increments
            //the variable refNum from -1 to 0. Afterwards, each time the while loop reaches a new article (i.e. the line contains
            //the string @ARTICLE), the program will be allowed to go into the second else if and print the reference of the
            //previous article. One feature that was implemented in order to speed up the program and to cut unnecessary string
            //manipulations was implemented into the first else if of the program. Before entering that first else if, the program
            //has already declared that the current line contains the string "author". And so, if that line, then the else if
            //will check if that line actually contains the name of the targeted author in the three different formats. If the line
            //does, then the else if will allow the program to manipulate the line in order to extract the needed elements. However, if the line
            //does not actually contain the name of the author we are looking for, then the program will make the
            //articleFromAuthor boolean be false which means that the current article that the while loop is looking at is not from the
            //targeted author. This will make it so that every line that the while loop registers until the next @ARTICLE string
            //will not be able to enter into the first if that manipulates the current line to extract the elements needed. This
            //effectively skips all the lines of the articles that do not come from the targeted authors as soon as the program reaches
            //the line that talks about the author. This will reduce the unwanted string manipulation to the minimum.
            //One thing to look out for with the while loop in this method is that
            //because the program is going from @ARTICLE string to @ARTICLE string and is making decisions based on that and because
            //the program is not paying attention to any article that is not written by the targeted author (as soon as it
            //detects it), then there comes a problem when the program tries to output the last reference to the files.
            //Because it is the last reference to be outputted, the program will never find another article that is written
            //by the targeted author (which is what the program needs to write to the output files). Thus, to fix this,
            //an if statement has been written at the end of the for loop which checks to see if the program is reading
            //the last file and if the author was found. If that is the case then the program outputs the last reference to the output files.
            while (sc[j].hasNextLine()) {

                //Reading a line and storing it.
                line = sc[j].nextLine();

                //If statement for manipulating and extracting elements from the current line.
                if (!line.contains("@ARTICLE") || articleFromAuthor) {
                    //Make it loop in here in the if until we hit another @ARTICLE so that we can skip
                    //the current article since it does not have the author we want.
                    if (line.contains("author") || articleFromAuthor) {
                        if (line.contains("@ARTICLE"))
                            articleFromAuthor = false;

                        else if (!line.contains(allCapsSearchName) && !line.contains(smallCapsSearchName) && !line.contains(startCaseSearchName))
                            articleFromAuthor = true;

                        else {
                            author = line.replace("author={", "");
                            author = author.replace("}, ", "");
                            authorIEEE = author.replace(" and", ",");
                            authorACM = author.split("\\s*(and)\\s*", 2)[0];
                            authorNJ = author.replace("and", "&");
                            authorFound = true;
                        }
                    } else if (line.contains("journal")) {
                        line = line.replace("journal={", "");
                        journal = line.replace("}, ", "");
                    } else if (line.contains("title")) {
                        line = line.replace("title={", "");
                        title = line.replace("}, ", "");
                    } else if (line.contains("year")) {
                        line = line.replace("year={", "");
                        year = line.replace("}, ", "");
                    } else if (line.contains("volume")) {
                        line = line.replace("volume={", "");
                        volume = line.replace("}, ", "");
                    } else if (line.contains("number")) {
                        line = line.replace("number={", "");
                        number = line.replace("}, ", "");
                    } else if (line.contains("pages")) {
                        line = line.replace("pages={", "");
                        pages = line.replace("}, ", "");
                    } else if (line.contains("doi")) {
                        line = line.replace("doi={", "");
                        doi = line.replace("}, ", "");
                    } else if (line.contains("month")) {
                        line = line.replace("month={", "");
                        month = line.replace("},", "");
                    }

                //Else if to stop the program from printing when it reaches the first article of the first file.
                } else if (refNbr == -1)
                    refNbr++;

                //Else if which allows the program to print the reference of the last article once it reaches a new article.
                else if (refNbr >= 0 && line.contains("@ARTICLE")) {
                    pw[0].println(authorIEEE + ". \"" + title + "\", " + journal + ", vol. " + volume + ", no. " + number + ", p. " + pages + ", " + month + " " + year + ".\n");
                    pw[1].println("[" + (refNbr + 1) + "]\t\t" + authorACM + " et al. " + year + ". " + title + ". " + journal + ". " + volume + ", " + number + " (" + year + "), " + pages + ". DOI:https://doi.org/" + doi + ".\n");
                    pw[2].println(authorNJ + ". " + title + ". " + journal + ". " + volume + ", " + pages + " (" + year + ").\n");

                    refNbr++;
                }

            }

            //Prints the last reference that was created from the article that the targeted author wrote.
            if (j == (sc.length - 1) && authorFound) {
                pw[0].print(authorIEEE + ". \"" + title + "\", " + journal + ", vol. " + volume + ", no. " + number + ", p. " + pages + ", " + month + " " + year + ".");
                pw[1].print("[" + (refNbr + 1) + "]\t\t" + authorACM + " et al. " + year + ". " + title + ". " + journal + ". " + volume + ", " + number + " (" + year + "), " + pages + ". DOI:https://doi.org/" + doi + ".");
                pw[2].print(authorNJ + ". " + title + ". " + journal + ". " + volume + ", " + pages + " (" + year + ").");
            }


        }

        //Returns the number of unique article references that were written.
        return (refNbr + 1);

    }
}