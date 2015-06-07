////////////////////////////////////////////////////////////////////////////////
// ArgsParser.java
////////////////////////////////////////////////////////////////////////////////

package org.instructures;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// A general command-line argument parser following the Unix
// single-character option conventions (similar to getopt,
// http://en.wikipedia.org/wiki/Getopt) and also the GNU long-form
// option conventions (cf. getopt_long, ibid.).
//
// The API uses the fluent-interface style, as discussed in:
// http://www.martinfowler.com/bliki/FluentInterface.html.
public class ArgsParser
{
	
 private Logger logger = LoggerFactory.getLogger(ArgsParser.class);
	
 private HashMap<Option, String> optionalOpts = new HashMap<Option, String>();
 private HashMap<Option, String> mandatoryOpts = new HashMap<Option, String>();
 private HashMap<Operand, Integer> operands = new HashMap<Operand, Integer>();
 private HashMap<String, Operand> docNameToOperand = new HashMap<String, Operand>();


 
 private static final int
 	REQUIRED = 1,
 	OPTIONAL = 2,
 	AT_LEAST_ONE = 3,
 	AT_LEAST_ZERO = 4;
 	

  // Canned messages and formatting strings.
  private static final String
    DEFAULT_VERSION = "(unknown)",
    HELP_MESSAGE = "display this help and exit",
    VERSION_MESSAGE = "output version information and exit",
    GENERIC_OPTIONS = "OPTIONS",
    OPTION_SUMMARY_FMT = "%4s%s %-20s   %s%n";
  
  // Factory to make a new ArgsParser instance, to generate help
  // messages and to process and validate the arguments for a command
  // with the given `commandName`.
  public static ArgsParser create(String commandName) {
    return new ArgsParser(commandName);
  }

  // A queryable container to hold the parsed results.
  //
  // Options are added using on of the `optional`, `require`, and
  // `requireOneOf` methods. The presence of such Options in the
  // actual arguments processed can be queried via the `hasOption`
  // method.
  //
  // Operands can be associated with an Option or can stand
  // alone. Standalone Operands are added using the `requiredOperand`,
  // `optionalOperand`, `oneOrMoreOperands`, and `zeroOrMoreOperands`
  // methods. Operands associated with an Option are added when that
  // Option is added.
  public class Bindings {
    public boolean hasOption(Option optionToQuery) {
      return options.contains(optionToQuery);
    }

    // If an Operand is optional and has a default value, then this method
    // will return the default value when the Operand wasn't specified.
    public <T> T getOperand(Operand<T> operand) {
      if (operands.containsKey(operand)) {
        List<T> result = getOperands(operand);
        if (result.size() == 1) {
          return result.get(0);
        }
      }
      else if (operand.hasDefaultValue()) {
        return operand.getDefaultValue();
      }
      throw new RuntimeException(
        String.format("Expected one binding for operand %s", operand));
    }
    
    public <T> List<T> getOperands(Operand<T> operand) {
      List<T> result = new ArrayList<>();
      if (operands.containsKey(operand)) {
        List<String> uninterpretedStrings = operands.get(operand);
        for (String stringFormat: uninterpretedStrings) {
          result.add(operand.convertArgument(stringFormat));
        }
      }
      return result;
    }

    private void addOption(Option option) {
      options.add(option);
    }
    
    private void bindOperand(Operand<?> operand, String lexeme) {
      List<String> bindings;
      if (operands.containsKey(operand)) {
        bindings = operands.get(operand);
      }
      else {
        bindings = new ArrayList<>();
        operands.put(operand, bindings);
      }
      try {
        operand.convertArgument(lexeme);
      }
      catch (Exception e) {
        throw new RuntimeException(
          String.format("(invalid format) %s", e.getMessage()));
      }
      bindings.add(lexeme);
    }

    private final Set<Option> options = new HashSet<>();
    private final Map<Operand, List<String>> operands = new HashMap<>();
    
    private Bindings() {
      /* intentionally left blank */
    }
  }

  // Parses the given command-line argument values according to the
  // specifications set through calls to the `optional`, `require`,
  // `requireOneOf` and `operands` methods.
  //
  // When the given arguments don't match the options specified, an
  // error message is printed and the program exits.
  //
  // Options for displaying the help message and the version message
  // are supported by calls made to `help` and `version`,
  // respectively. A call to 'parse` will cause the program to exit if
  // the help or version options are present in the given `args`. If
  // both are specified, then both will be printed before exit.
  public ArgsParser.Bindings parse(String[] args) {
    Bindings bindings = new Bindings();
    logger.info("optional options: " + optionalOpts.toString());
    logger.info("mandatory options: " + mandatoryOpts.toString());
    boolean helpFlag = false;
    boolean versionFlag = false;
    int i = 0;
    while(i < args.length) {
    	String arg = args[i];
		int offset = 0;
    	if (isOption(arg)) {
    		offset = 1;
    		arg = removeDashes(arg);
    		Option specifiedOption = findOptionAmongOptions(arg);
    		if (specifiedOption == null) {
    			throw new IllegalArgumentException("Option not found!");
    		}
    		if (specifiedOption.equals(helpOption)) {
    			helpFlag = true;
    			break;
    		} else if (specifiedOption.equals(versionOption)) {
    			versionFlag = true;
    			break;
    		}
    		if (hasSpecifiedOption(bindings, specifiedOption)) {
    			throw new IllegalArgumentException("Option duplicated or an associated option is already bound");
    		}
    		bindings.addOption(specifiedOption);
    		while(i + offset < args.length && !isOption(args[i + offset]) && isBindable(specifiedOption, args[i + offset])) {
    			bindings.bindOperand(specifiedOption.getOperand(), args[i + offset]);
    			offset += 1;
    		}

    	} else {
    		Operand boundOperand = getOperand();
    		logger.info("operand to bind: " + boundOperand);
    		if (boundOperand != null) {
    			switch(operands.get(boundOperand)) {
    			case REQUIRED:
    				if (isOption(arg)) {
    					throw new IllegalArgumentException("Expected a mandatory operand");
    				}
    				bindings.bindOperand(boundOperand, arg);
    				offset += 1;
    				break;
    			case OPTIONAL:
    				if (!isOption(arg)) {
    					bindings.bindOperand(boundOperand, arg);
    				}
    				offset += 1;
    				break;
    			case AT_LEAST_ONE:
					if (isOption(arg)) {
						throw new IllegalArgumentException("Expected at least one operand");
					}
    				while(i + offset < args.length && !isOption(args[i + offset])) {
    					bindings.bindOperand(boundOperand, args[i + offset]);	
    					offset += 1;
    				}
    				break;
    			case AT_LEAST_ZERO:
    				while(i + offset < args.length && !isOption(args[i + offset])) {
    					bindings.bindOperand(boundOperand, args[i + offset]);
    					offset += 1;
    				}
    				break;
    			}
    		}
    	}
    	i += offset;
    }
    if (helpFlag) {
    	logger.info(helpOption.getSummary());
    	for (Entry<Option, String> option : optionalOpts.entrySet()) {
    		logger.info(option.getKey().toString() + " - " + option.getKey().getSummary());
    	}
    	for (Entry<Option, String> option : mandatoryOpts.entrySet()) {
    		logger.info(option.getKey().toString() + " - " + option.getKey().getSummary());
    	}
    	return bindings;
    }
    if (versionFlag) {
    	logger.info(this.versionString +" " +  this.versionOption.getSummary());
    	return bindings;
    }
    for(Entry<Option, String> optionEntry : mandatoryOpts.entrySet()) {
    	if (!hasSpecifiedOption(bindings, optionEntry.getKey())) {
    		throw new IllegalArgumentException(optionEntry.getValue() + " is missing");
    	}
    }
    return bindings;
  }
  
	public boolean hasSpecifiedOption(Bindings bindings, Option specifiedOption) {
		if  (bindings.hasOption(specifiedOption)) {
			return true;
		}
		for(Option option : specifiedOption.getDependencies()) {
			if (bindings.hasOption(option)) {
				return true;
			}
		}
		return false;

	}
  
  private boolean isBindable(Option specifiedOption, String string) {
	try {
		specifiedOption.getOperand().convertArgument(string);
		return true;
	} catch (Exception e) {
		return false;
	}
}

private boolean isOption(String arg) {
	  return arg.startsWith("-") || arg.startsWith("--");
  }
  
  private Operand getOperand() {
	  Operand[] operand = operands.keySet().toArray(new Operand[operands.entrySet().size()]);
	  return operand.length > 0 ? operand[0] : null;
  }


private String removeDashes(String input) {
	  while(input.charAt(0) == '-') {
		  input = input.substring(1);
	  }
	  return input;
  }

  private Option findOptionAmongOptions(String toFind) {
	  	Option toReturn = null;
	  	if ((toReturn = findMandatory(toFind)) != null) {
	  		return toReturn;
	  	}
	  	toReturn = null;
	  	if ((toReturn = findOptional(toFind)) != null) {
	  		return toReturn;
	  	}
		return null;
}


private boolean inFlags(String toFind, Option option) {
	ArrayList<String> longflags = new ArrayList<String>();
	ArrayList<String> shortflags = new ArrayList<String>();
	option.getFlags(longflags, shortflags);
	return longflags.contains(toFind) || shortflags.contains(toFind);
}


private Option findOptional(String toFind) {
	for(Entry<Option, String> entry : optionalOpts.entrySet()) {
		if (inFlags(toFind, entry.getKey())) {
			return entry.getKey();
		}
	}
	return null;
}

private Option findMandatory(String toFind) {
	for(Entry<Option, String> entry : mandatoryOpts.entrySet()) {
		if (inFlags(toFind, entry.getKey())) {
			return entry.getKey();
		}
	}
	return null;
}

// Uses the given `summaryString` when the help/usage message is printed.
  public ArgsParser summary(String summaryString) {
    this.summaryString = summaryString;
    return this;
  }

  // Enables the command to have an option to display the current
  // version, represented by the given `versionString`. The version
  // option is invoked whenever any of the given `flags` are used,
  // where `flags` is a comma-separated list of valid short- and
  // long-form flags.
  public ArgsParser versionNameAndFlags(String versionString, String flags) {
    this.versionString = versionString;
    this.versionOption = Option.create(flags).summary(VERSION_MESSAGE);
    return optional(versionOption);
  }

  // Enables an automated help message, generated from the options
  // specified.  The help message will be invoked whenever any of the
  // given `flags` are used.
  //
  // The `flags` parameter is a comma-separated list of valid short-
  // and long-form flags, including the leading `-` and `--` marks.
  public ArgsParser helpFlags(String flags) {
    this.helpOption = Option.create(flags).summary(HELP_MESSAGE);
    return optional(helpOption);
  }

  // Adds the given option to the parsing sequence as an optional
  // option. If the option takes an Operand, the value of the
  // associated operand can be accessed using a reference to that
  // specific Operand instance.
  //
  // Throws an IllegalArgumentException if the given option specifies
  // flags that have already been added.
  public ArgsParser optional(Option optionalOption) {
    optionalOpts.put(optionalOption, null);
    return this;
  }

  // Adds the given option to the parsing sequence as a required
  // option. If the option is not present during argument parsing, an
  // error message is generated using the given `errString`. If the
  // option takes an Operand, the value of the associated operand can
  // be accessed using a reference to that specific Operand instance.
  //
  // Throws an IllegalArgumentException if the given option specifies
  // flags that have already been added.
  public ArgsParser require(String errString, Option requiredOption) {
	mandatoryOpts.put(requiredOption, errString);
    return this;
  }

  // Adds the given set of mutually-exclusive options to the parsing
  // sequence. An error message is generated using the given
  // `errString` when multiple options that are mutually exclusive
  // appear, and when none appear. An example of such a group of
  // mutually- exclusive options is when the option specifies a
  // particular mode for the command where none of the modes are
  // considered as a default.
  //
  // Throws an IllegalArgumentException if any of the given options
  // specify flags that have already been added.
  public ArgsParser requireOneOf(String errString, Option... exclusiveOptions) {
    for(Option opt : exclusiveOptions) {
    	associateWithOthers(opt, exclusiveOptions);
        mandatoryOpts.put(opt, errString);
    }
    return this;
  }
  
  private void associateWithOthers(Option opt, Option... options) {
	  for(Option option : options) {
		  opt.associatedWith(option);
	  }
  }

  // Adds the given operand to the parsing sequence as a required
  // operand to appear exactly once. The matched argument's value is
  // retrievable from the `ArgsParser.Bindings` store by passing the
  // same `requiredOperand` instance to the `getOperand` method.
  public ArgsParser requiredOperand(Operand requiredOperand) {
	  this.docNameToOperand.put(requiredOperand.getDocName(), requiredOperand);
	  this.operands.put(requiredOperand, REQUIRED);
    return this;
  }

  // Adds the given operand to the parsing sequence as an optional
  // operand. The matched argument's value is retrievable from the
  // `ArgsParser.Bindings` store by passing the same `optionalOperand`
  // instance to the `getOperands` method, which will return either a
  // the empty list or a list with a single element.
  public ArgsParser optionalOperand(Operand optionalOperand) {
	  this.docNameToOperand.put(optionalOperand.getDocName(), optionalOperand);
	this.operands.put(optionalOperand, OPTIONAL);
    return this;
  }

  // Adds the given operand to the parsing sequence as a required
  // operand that must be specifed at least once and can be used
  // multiple times (the canonical example would be a list of one or
  // more input files).
  //
  // The values of the arguments matched is retrievable from the
  // `ArgsParser.Bindings` store by passing the same `operand`
  // instance to the `getOperands` method, which will return a list
  // with at least one element (should the arguments pass the
  // validation process).
  public ArgsParser oneOrMoreOperands(Operand operand) {
	  this.docNameToOperand.put(operand.getDocName(), operand);
    this.operands.put(operand, AT_LEAST_ONE);
    return this;
  }

  // Adds the given operand to the parsing sequence as an optional
  // operand that can be used zero or more times (the canonical
  // example would be a list of input files, where if none are given
  // then stardard input is assumed).
  //
  // The values of the arguments matched is retrievable from the
  // `ArgsParser.Bindings` store by passing the same `operand`
  // instance to the `getOperands` method, which will return a list of
  // all matches, potentially the empty list.
  public ArgsParser zeroOrMoreOperands(Operand operand) {
	  this.docNameToOperand.put(operand.getDocName(), operand);
    this.operands.put(operand, AT_LEAST_ZERO);
    return this;
  }

  private final String commandName;

  private String summaryString = null;
  private String versionString = DEFAULT_VERSION;
  private Option helpOption = null;
  private Option versionOption = null;
  
  private ArgsParser(String commandName) {
    this.commandName = commandName;
  }

}
