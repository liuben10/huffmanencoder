package org.instructures;

import java.io.File;
import java.util.List;

public class Cut {
	  public static final String
	    SUMMARY = "Print selected parts of lines from each FILE to standard output",
	    VERSION = "cut (Facade) 8.21",
	    MODE_REQUIRED = "you must specify a list of bytes, characters, or fields",
	    BYTES_DESC = "select only these bytes",
	    CHAR_DESC = "select only these characters",
	    LIST_DESC = "select only these fields",
	    SPLIT_DESC = "with -b: don't split multibyte characters",
	    SKIP_DESC = "do not print lines not containing delimiters",
	    DELIM_DESC = "use DELIM instead of TAB for field delimiter";

	  static final ArgsParser parser;
	  static final Operand<String> LIST = Operand.create(String.class, "LIST");
	  static final Operand<String> DELIM = Operand.create(String.class, "DELIM");
	  static final Operand<File> FILES = Operand.create(File.class, "FILE");
	  static final Option AS_BYTES, AS_CHARACTERS, AS_FIELDS, NO_SPLIT, SKIP;

	  static {
	    parser = ArgsParser.create("cut").summary(SUMMARY)
	      .helpFlags("--help")
	      .versionNameAndFlags(VERSION, "--version");

	    AS_BYTES = Option.create("-b,--bytes", LIST).summary(BYTES_DESC);
	    AS_CHARACTERS = Option.create("-c,--characters", LIST).summary(CHAR_DESC);
	    AS_FIELDS = Option.create("-f,--fields", LIST).summary(LIST_DESC);
	    NO_SPLIT = Option.create("-n").summary(SPLIT_DESC).associatedWith(AS_BYTES);
	    SKIP = Option.create("-s,--only-delimited").summary(SKIP_DESC);

	    parser.requireOneOf(MODE_REQUIRED, AS_BYTES, AS_CHARACTERS, AS_FIELDS);
	    parser.optional(NO_SPLIT);
	    parser.optional(SKIP);
	    parser.optional(Option.create("-d,--delimiter", DELIM).summary(DELIM_DESC));
	    parser.zeroOrMoreOperands(FILES);
	  }

	  public static void main(String[] args) {
	    ArgsParser.Bindings setup = parser.parse(args);
	    List<File> files = setup.getOperands(FILES);
	    if (setup.hasOption(AS_BYTES)) {
	    	
	      // do bytes stuff...
	    } else if (setup.hasOption(AS_CHARACTERS)) {
	      // do characters stuff...
	    } else if (setup.hasOption(AS_FIELDS)) {
	      // do fields stuff...
	    }
	    // blah, blah, blah
	  }
	}