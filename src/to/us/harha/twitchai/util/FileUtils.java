package to.us.harha.twitchai.util;

import static to.us.harha.twitchai.util.GenUtils.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class FileUtils
{

    public static void writeToTextFile(String directory, String fileName, String text)
    {
        if (!directoryExists(directory))
        {
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

    public static boolean removeLineFromTextFile(String fileName, String line)
    {
        File inputFile = new File(fileName);
        File tempFile = new File(fileName + ".temp");
        BufferedReader reader;
        BufferedWriter writer;
        boolean successful = false;

        File f = new File(fileName);
        if (!f.exists())
        {
            return successful;
        }

        try
        {
            reader = new BufferedReader(new FileReader(inputFile));
            writer = new BufferedWriter(new FileWriter(tempFile));

            String currentLine;

            while ((currentLine = reader.readLine()) != null)
            {
                // Trim newline when comparing with lineToRemove
                String trimmedLine = currentLine.trim();
                if (trimmedLine.equals(line))
                    continue;
                writer.write(currentLine + System.getProperty("line.separator"));
            }
            writer.close();
            reader.close();
        } catch (IOException e)
        {
            e.printStackTrace();
            exit(1);
        } finally
        {
            inputFile.delete();
            successful = tempFile.renameTo(inputFile);
        }

        return successful;
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

            // Failed to create directory, don't continue, return immediately!
            if (!result)
            {
                System.err.println("Failed to create directory! " + directory);
            }
        }

        return result;
    }
}
