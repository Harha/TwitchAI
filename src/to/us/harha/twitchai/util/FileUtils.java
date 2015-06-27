package to.us.harha.twitchai.util;

import static to.us.harha.twitchai.util.GenUtils.*;
import static to.us.harha.twitchai.util.LogUtils.logMsg;
import static to.us.harha.twitchai.util.LogUtils.logErr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class FileUtils
{

    public static ArrayList<String> readTextFile(String fileName)
    {
        ArrayList<String> result = new ArrayList<String>();

        File f = new File(fileName);
        if (!f.exists())
        {
            return result;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(fileName)))
        {
            String line = br.readLine();

            while (line != null)
            {
                result.add(line);
                line = br.readLine();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            exit(1);
        }

        return result;
    }

    public static void writeToTextFile(String directory, String fileName, String text)
    {
        if (!directoryExists(directory))
        {
            logErr("Cannot write to file " + fileName + " in directory " + directory + "! The directory doesn't exist.");
            return;
        }

        try
        {
            PrintWriter out = new PrintWriter(new FileWriter(directory + fileName, true));
            out.println(text);
            out.close();
        } catch (IOException e)
        {
            e.printStackTrace();
            exit(1);
        }
    }

    public static void removeFromTextFile(String directory, String fileName, String line)
    {
        ArrayList<String> lines = readTextFile(directory + fileName);

        if (lines.isEmpty())
        {
            logErr("File is empty or does not exist!");
            return;
        }

        ArrayList<String> lines_to_be_removed = new ArrayList<String>();
        for (String s : lines)
        {
            if (s.equals(line))
            {
                logMsg("Removing line " + s + " from " + fileName + "!");
                lines_to_be_removed.add(s);
            }
        }

        lines.removeAll(lines_to_be_removed);
        File file = new File(directory + fileName);
        file.delete();

        for (String s : lines)
        {
            writeToTextFile(directory, fileName, s);
        }
    }

    public static boolean directoryExists(String directory)
    {
        // Check if the directory exists, if not, create it
        boolean result = true;
        File dirTest = new File(directory);
        if (!dirTest.exists())
        {
            try
            {
                dirTest.mkdir();
                result = true;
            } catch (SecurityException e)
            {
                e.printStackTrace();
                exit(1);
            }

            // Failed to create directory!
            if (!result)
            {
                logErr("Failed to create directory! " + directory);
            }
        }

        return result;
    }
}
