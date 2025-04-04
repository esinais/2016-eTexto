package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

/**
 *
 * @author Pablo
 */
public class Util {

    public enum Format {

        format1, format2;
    }
    private static ArrayList<String> defaultSection = new ArrayList<String>(); // Abstract, Discussion, Results, Results and Discussion

    // return all files from directory e.g., paper1.arff
    public static ArrayList<String> getFiles(String directory, String[] extList) {

        ArrayList<String> files = new ArrayList<String>();
        File dir = new File(directory);
        String arq[] = dir.list();
        if (arq == null) {
            JOptionPane.showMessageDialog(null, "The directory is incorrect.\n\n" + directory, "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            for (int i = 0; i < arq.length; i++) {
                for (int j = 0; j < extList.length; j++) {
                    String string = arq[i].toLowerCase() + "final";
                    String split[] = string.split("." + extList[j]);  // .xml, .html, .pdf
                    // Just get files with extension from extList[]
                    if (split.length > 1) {
                        files.add(arq[i].toLowerCase());
                        break;
                    }
                }
            }
        }
        return files;

    }

    public static String getFile(String modelDir, String name, String ext) {

        String regEx = "(^|.)" + name + "(.|$)"; // Regular expression
        File dir = Util.createDirectory(modelDir);
        String arq[] = dir.list();
        for (int i = 0; i < arq.length; i++) {
            Pattern pName = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
            Matcher mName = pName.matcher(arq[i]);
            if (mName.find()) { // Search word in the string
                if (arq[i].endsWith(ext)) {
                    return modelDir + "/" + arq[i];
                }
            }
        }
        return "";

    }

    public static String removeExtesion(String name, String ext) {

        String filename = name + "something";
        if (filename.split(ext).length > 1) {
            return filename.split(ext)[0];
        } else {
            return name;
        }

    }

    // Check if the file exist
    public static boolean existFile(String fileDir) {
        File dir = new File(fileDir);
        if (!dir.exists()) {
            return false;
        }
        return true;
    }

    public static String quote(String include) {
        return ("\"" + include + "\"");
    }

    public static String simpleQuote(String include) {
        return ("'" + include + "'");
    }

    // nameclass: 1_Complication_44, 2_Benefit_36, 5_Other_55. return false if is other
    public static boolean existWord(String word, String text, String regEx) {

        regEx = regEx.replace("%s", word);
        Pattern pName = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        Matcher mName = pName.matcher(text);
        if (mName.find()) { // Search word in the string
            return true;
        } else {
            return false;
        }

    }

    // Check if a term exist in the list
    public static boolean existTerm(ArrayList<String> list, String term) {

        for (int i = 0; i < list.size(); i++) {
            String word = list.get(i);
            if (word.equals(term)) {
                return true;
            }
        }
        return false;

    }

    public static String removeAnyNumberDot(String sentence) {

        String temp = Util.validateSentence2Classification(sentence);
        temp = temp.replaceAll("[0-9]", ""); // Cut out number
        temp = temp.replaceAll("[.]", "");   // Cut out dot .
        temp = temp.trim().toLowerCase();
        return temp;

    }

    public static String removeNumberDot(String filter) {

        String temp = filter.replaceAll("^[0-9].", ""); // Cut out number following dot in the beginning of the word
        temp = temp.trim();
        return temp;

    }

    public static String removeInitialNumber(String sentence) {

        String temp = sentence.replaceAll("^[0-9]*", ""); // Cut out number in the beginning of the sentence
        temp = temp.trim();
        return temp;

    }

    public static String validateSentence2Classification(String sentence) {

        sentence = validateSentence2Extraction(sentence);
        sentence = sentence.replaceAll("[(|)]", "");        // Cut out parenthesis "(" and ")"
        // It is used in the PutInFileTXT program, i.e., the training instances are
        sentence = sentence.replaceAll("[\\;\\:\\,\\(\\)\\[\\]\\\"\\'\\`\\´\\\ufffd]*", ""); // Remove ; : , ( ) [ } " ' `´ \ufffd (indefinite character)
        return sentence;

    }

    public static String validateSentence2Extraction(String sentence) {

        //sentence = sentence.trim().toLowerCase();           // POS Tagger classifies the word doppler as noun(NN) and Doppler as proper noun (NNP)
        sentence = sentence.replaceAll("[\n]", "");         // Cut out character \n (enter)
        sentence = sentence.replaceAll("[\t]", "");         // Cut out character \t (tab)
        sentence = sentence.replaceAll("[\\s]{2,4}", " ");  // Cut out 2 until 4 white space
        sentence = sentence.replaceAll("[.]$", "");         // Cut out the final dot (.)
        // It is used in the PutInFileTXT program, i.e., the training instances are        
        sentence = sentence.replaceAll("\\s{2,}", " ");     // Remove the excess of the whitespaces
        return sentence;

    }

    // Split sentence by dot (.), exclamation (!) and question (?)
    public static ArrayList<String> splitSentence(String line) {

        ArrayList<String> sentences = new ArrayList<String>();
        String lines1[] = line.split("[.]");
        for (int i = 0; i < lines1.length; i++) {
            String lines2[] = lines1[i].split("[!|?]");
            // if find ! or ?
            if (lines2.length > 1) {
                for (int j = 0; j < lines2.length; j++) {
                    sentences.add(lines2[j]);
                }
            } else {
                sentences.add(lines1[i]);
            }
        }
        return sentences;

    }

    public static String standardDir(String dir) {
        return dir.replace("//", "\\");
    }

    public static String backSlash(String dir) {
        return dir.replace("/", "\\");
    }

    // Sort array by name
    public static void sortArrayByName(ArrayList<String> list) {

        Collections.sort(list, new Comparator() {

            public int compare(Object o1, Object o2) {
                String p1 = (String) o1;
                String p2 = (String) o2;
                return p1.compareTo(p2);
            }
        });

    }

    public static String getDefaultSectionShort(String section) {

        if (section.equals("abstract")) {
            return "Abstract";
        } else if (section.equals("results")) {
            return "Results";
        } else if (section.equals("discussion")) {
            return "Discussion";
        } else if (section.equals("results and discussion")) {
            return "R and D";
        } else {
            return section;
        }

    }

    private static String getSection(String sentence) {

        //sentence = sentence.toLowerCase();
        for (int i = 0; i < getDefaultSection().size(); i++) {
            String regEX = "[\\.]*[\\s]*\\(" + getDefaultSection().get(i) + "\\)[\\s]*";
            String tmp = sentence.replaceAll(regEX, "");
            if (!tmp.equals(sentence)) {
                return getDefaultSection().get(i); // section was identified in sentence
            }
        }
        return "section not identified";

    }

    private static String removeSection(String section, String sentence) {

        //sentence = sentence.toLowerCase();
        String regEX = "[\\.]*[\\s]*\\(" + section + "\\)[\\s]*";
        String tmp = sentence.replaceAll(regEX, "");
        if (!tmp.equals(sentence)) {
            return tmp;  // return sentence without section
        }
        return sentence; // return sentence with section

    }

    //Sections processed: abstract, results, discussion, results and discussion
    // If defaultSectionList is null, the processing is made in all sections
    public static ArrayList<String> getDefaultSection() {

        if (defaultSection.isEmpty()) {
            defaultSection.add("Abstract");
            defaultSection.add("Results");
            defaultSection.add("Discussion");
            defaultSection.add("Results and Discussion");
        }
        return defaultSection;

    }

    public static String getSectionAbstract() {
        return defaultSection.get(0);
    }

    public static String getSectionResults() {
        return defaultSection.get(1);
    }

    public static String getSectionDiscussion() {
        return defaultSection.get(2);
    }

    public static String getSectionResAndDisc() {
        return defaultSection.get(3);
    }

    public static String getSectionResAndDiscShort() {
        return "R and D";
    }

    // Return the index of the section. It's used to sort the sectionSentence by the index
    public static int getSectionIndex(String section) {

        for (int i = 0; i < defaultSection.size(); i++) {
            if (defaultSection.get(i).equals(section)) {
                return i;
            }
        }
        return -1;

    }

    public static File createDirectory(String directory) {

        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir;

    }

    private static void createFile(String directory) {

        File dir = new File(directory);
        if (!dir.exists()) {
            try {
                dir.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public static String readFiles(String dir) throws FileNotFoundException, IOException {

        createFile(dir);
        BufferedReader in = new BufferedReader(new FileReader(dir));
        String line = "";
        String content = "";
        while ((line = in.readLine()) != null) {
            content += line + "\n"; // select by line
        }
        in.close();
        return content;

    }

    public static ArrayList<String> getInfoTxt(String dir) {

        ArrayList<String> list = new ArrayList<String>();
        BufferedReader in = null;
        try {
            createFile(dir);
            in = new BufferedReader(new FileReader(dir));
            String line = "";
            while ((line = in.readLine()) != null) {
                list.add(line);
            }
            in.close();
            return list;
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
        return null; // error

    }

    public static String getDate(Format formatType) {
        if (formatType == Format.format1) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
            Date now = new Date();
            return "Data (yyyy/mm/dd): " + format.format(now) + "\t\tTime: " + now.getHours() + "h"
                    + now.getMinutes() + "m" + now.getSeconds() + "s";
        } else {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date now = new Date();
            return format.format(now) + "_" + now.getHours() + "h"
                    + now.getMinutes() + "m" + now.getSeconds() + "s";
        }
    }

    // remove inverted bar and POS pattern, and replace plural form (e.g., syndromes) by singular one (e.g., syndrome)
    public static String processTaggedSentence(String taggedSentence, boolean isAddFinalDot) {

        taggedSentence = taggedSentence.replace("\\", ""); // remove the inverted bar        
        taggedSentence = taggedSentence.replaceAll("([\\w-/]*_JJ.?\\s)?[\\w-/]*_NN[PS]?\\sof_IN\\s", ""); // Remove prefix (JJ)?_NN_(IN_of) in the sentence
        taggedSentence = taggedSentence.replaceAll("[A-Za-z]{1,3}/[A-Za-z]{1,3}_NN[PS]?\\s", ""); // Remove pattern: g/dL_NNP e cm/sec_NN
        taggedSentence = taggedSentence.replace("syndromes", "syndrome"); // replace plural form by singular one
        taggedSentence = taggedSentence.trim();
        if (isAddFinalDot) {
            taggedSentence += " ._."; // add the final dot in the specific part
        }
        return taggedSentence;

    }

    public static String getString(ArrayList<String> list) {
        String string = "";
        for (int i = 0; i < list.size(); i++) {
            string += list.get(i) + "; ";
        }
        return string;
    }

    public static int getPaperNumber(String line) {
        String regEx = "^(\\d{1,})";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(line);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return -1; // error
    }

}