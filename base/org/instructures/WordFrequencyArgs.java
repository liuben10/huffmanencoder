package org.instructures;
////////////////////////////////////////////////////////////////////////////////
// WordFrequencyArgs.java
////////////////////////////////////////////////////////////////////////////////

import java.io.*;
import java.util.*;

// Not the actual WordFrequency program, just a demo to show how the
// arguments to it can be expressed and parsed.
public class WordFrequencyArgs
{
  private static final ArgsParser parser;
  private static final Option BY_WORDS, BY_COUNT, ORIGINAL_ORDER;
  private static final Operand<Integer> THRESHOLD;
  private static final Operand<File> FILE;

  static {
    parser = ArgsParser.create("java WordFrequency")
      .summary("Utility to count the occurences of each word in a text file")
      .versionNameAndFlags("v1.0", "-v,--version")
      .helpFlags("-h,--help");
    
    BY_WORDS = Option.create("-w,--sort-by-words")
      .summary("sort by words (alphabetically)");
    BY_COUNT = Option.create("-c,--sort-by-count")
      .summary("sort by counts (from highest to lowest)");
    ORIGINAL_ORDER = Option.create("-f,--file-order")
      .summary("show words in order of first appearance");
    parser.requireOneOf("ordering option", BY_WORDS, BY_COUNT, ORIGINAL_ORDER);
    
    THRESHOLD = Operand.create(Integer.class, "NUM").setDefaultValue(0);
    Option thresholdOption = Option.create("-T,--threshold", THRESHOLD)
      .summary("only show words with frequencies > " + THRESHOLD.getDocName());
    parser.optional(thresholdOption);

    FILE = Operand.create(File.class, "FILENAME");
    parser.oneOrMoreOperands(FILE);
  }

  public static void main(String[] args) {
    final Map<Option, String> modeNames = new HashMap<>();
    modeNames.put(BY_WORDS, "Alphabetical order");
    modeNames.put(BY_COUNT, "Most frequent to least");
    modeNames.put(ORIGINAL_ORDER, "Original order");
    
    try {
      ArgsParser.Bindings setup = parser.parse(args);
      List<File> fileIn = setup.getOperands(FILE);
      int threshold = setup.getOperand(THRESHOLD);

      String mode = "INVALID MODE";
      for (Option mutexOption: modeNames.keySet()) {
        if (setup.hasOption(mutexOption)) {
          mode = modeNames.get(mutexOption);
          break;
        }
      }

      System.out.printf("Settings: File: %s, Threshold: %d, Mode: %s%n",
                        fileIn, threshold, mode);
    }
    catch (Exception ex) {
    	ex.printStackTrace();
      System.err.printf("Error: %s%n", ex.getMessage());
      System.exit(1);
    }
  }
}