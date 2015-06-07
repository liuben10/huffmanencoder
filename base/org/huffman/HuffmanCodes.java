package org.huffman;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;

import org.instructures.ArgsParser;
import org.instructures.Operand;
import org.instructures.Option;
import org.streams.BitInputStream;
import org.streams.BitOutputStream;


public class HuffmanCodes {
	
	private HuffmanTree<Character, Integer> huffmanTree;
	
	private static final Option ENCODE, DECODE, SHOW_FREQUENCY, SHOW_CODES, SHOW_BINARY;
	
	private static final Operand<File> FILES;
	
	private Map<Character, String> replacements;
	
	private Map<Character, Integer> frequencyMap;
	
	private static ArgsParser parser;
	
	public File inputFile;
	
	public File outputFile;
	
	  static {
		 String helpMsg = "Usage: java HuffmanCodes OPTIONS IN OUT\r\n" + 
					"Encodes and decodes files using Huffman's technique\r\n" + 
					"\r\n" + 
					"  -e, --encode               encodes IN to OUT\r\n" + 
					"  -d, --decode               decodes IN to OUT\r\n" + 
					"      --show-frequency       show the frequencies of each byte \r\n" + 
					"      --show-codes           show the codes for each byte\r\n" + 
					"      --show-binary          show the encoded sequence in binary\r\n" + 
					"  -h, --help                 display this help and exit";
		    parser = ArgsParser.create("java WordFrequency")
		      .summary(helpMsg)




  
  
		      .helpFlags("-h,--help");
		    
		    ENCODE = Option.create("-e,--encode")
		    		.summary("encodes IN to OUT");
		    DECODE = Option.create("-d,--decode")
		    		.summary("decodes IN to OUT");
		    
		    SHOW_FREQUENCY = Option.create("--show-frequency")
		    		.summary("show the frequencies of each byte");
		    SHOW_CODES = Option.create("--show-codes")
		    		.summary("show the codes for each byte");
		    SHOW_BINARY = Option.create("--show-binary")
		    		.summary("show the encoded sequence in binary");
		    parser.requireOneOf("encode/decode option", ENCODE, DECODE);
		    parser.optional(SHOW_FREQUENCY);
		    parser.optional(SHOW_CODES);
		    parser.optional(SHOW_BINARY);
		    
		    FILES = Operand.create(File.class, "IN_OUT_FILES");
		    parser.oneOrMoreOperands(FILES);
	  
	  }
	  
	
	  public HuffmanCodes(String raw) {
		  init(raw);
	  }
	
	public HuffmanCodes(ArgsParser.Bindings bindings) throws FileNotFoundException, IOException {
		List<File> files = bindings.getOperands(FILES);
		if (files.size() != 2)
			throw new IllegalArgumentException("You need to provide exactly 2 file names.");
		
		inputFile = files.get(0);
		outputFile = files.get(1);

		if (bindings.hasOption(ENCODE)) {
			String inputStr = readFile(inputFile.getAbsolutePath());
			init(inputStr);
		} else {
			BitInputStream inputStream = new BitInputStream(inputFile);
		}
	}
	
	public static void main(String...args) {
		ArgsParser.Bindings bindings = parser.parse(args);
		String testString = "sadfjaklfjafaksjflasjdflkasjfasljfasdlkfjhasdkfhsadfjhadslfkhadsjkfhashlfdajkfhfsahdjkfhsafkahfhjdsfhsajdfhals";
		HuffmanCodes encoder = new HuffmanCodes(testString);
		String encoded = encoder.encode(testString);
		System.out.println(encoded);
		String decoded = encoder.decode(encoded);
		System.out.println(decoded);
		System.out.println(testString.equals(decoded));
	}
	
	public void init(String raw) {
		Map<Character, Integer> frequencyMap = sortedFrequencyMap(raw);
		setFrequencyMap(frequencyMap);
		System.out.println(frequencyMap);
		PriorityQueue<HuffmanTree<Character, Integer>> q = sortedFrequency(raw);
		HuffmanTree<Character, Integer> resultTree = buildHuffmanTree(q);
		setHuffmanTree(resultTree);
		System.out.println(resultTree.treeString());
		Map<Character, String> replacementMap = generateReplacementMap();
		setReplacements(replacementMap);
		System.out.println(replacementMap);
	}
	
	public String encode(String raw) {
		String resultStream = "";
		for(char c : raw.toCharArray()) {
			if (replacements.get(c) == null) {
				System.out.println(c + " is null?");
			}
			resultStream += replacements.get(c);
		}
		return resultStream;
	}
	
	public String decode(String encoded) {
		String resultString = "";
		HuffmanTree<Character, Integer> iterator = huffmanTree;
		for(char c : encoded.toCharArray()) {
			if (c == '0') {
				iterator = iterator.getLeftChild();
			} else {
				iterator = iterator.getRightChild();
			}
			if (iterator.isLeaf()) {
				resultString += iterator.getKey();
				iterator = huffmanTree;
			}
		}
		return resultString;
	}
	
	public Map<Character, String> generateReplacementMap() {
		Map<Character, String> replacements = new HashMap<Character, String>();
		Set<Character> keyset = frequencyMap.keySet();
		Character[] keys = keyset.toArray(new Character[keyset.size()]);
		for(Character c : keys) {
			replacements.put(c, buildString(c));
		}
		return replacements;
	}
	
	
	public String buildString(Character c) {
		return huffmanTree.pathString(c, "");
	}


	public HuffmanTree<Character, Integer> buildHuffmanTree(PriorityQueue<HuffmanTree<Character, Integer>> q) {
		while (q.size() >= 2) {
			combineAndSort(q);
		}
		return q.peek();
	}
	
	public void combineAndSort(PriorityQueue<HuffmanTree<Character, Integer>> q) {
		HuffmanTree<Character, Integer> e1 = q.remove();
		HuffmanTree<Character, Integer> e2 = q.remove();
		HuffmanTree<Character, Integer> combined = combine(e1, e2);
		q.add(combined);
	}
	
	private HuffmanTree<Character, Integer> combine(HuffmanTree<Character, Integer> e1,
			HuffmanTree<Character, Integer> e2) {
		HuffmanTree<Character, Integer> combined = new HuffmanTree<Character, Integer>(null, e1.getValue() + e2.getValue());
		combined.setLeftChild(e1);
		combined.setRightChild(e2);
		return combined;
	}


	public PriorityQueue<HuffmanTree<Character, Integer>> sortedFrequency(String raw) {
		Map<Character, Integer> sortedMap = sortedFrequencyMap(raw);
		List<HuffmanTree<Character, Integer>> entryList = convertEntrySet(sortedMap);
		PriorityQueue<HuffmanTree<Character, Integer>> entryQ = new PriorityQueue<HuffmanTree<Character, Integer>>(new EntryComparator());
		for(HuffmanTree<Character, Integer> entry : entryList) {
			entryQ.add(entry);
		}
		return entryQ;
	}
	
	private List<HuffmanTree<Character, Integer>> convertEntrySet(Map<Character, Integer> map) {
		ArrayList<HuffmanTree<Character, Integer>> result = new ArrayList<HuffmanTree<Character, Integer>>();
		for(Entry<Character, Integer> entry : map.entrySet()) {
			result.add(new HuffmanTree<Character, Integer>(entry.getKey(), entry.getValue()));
		}
		return result;
	}
	
	public Map<Character, Integer> sortedFrequencyMap(String raw) {
		Map<Character, Integer> frequencyMap = new HashMap<Character, Integer>();
		for(char curchar : raw.toCharArray()) {
			if (frequencyMap.containsKey(curchar)) {
				frequencyMap.put(curchar, frequencyMap.get(curchar) + 1);
			} else {
				frequencyMap.put(curchar, 1);
			}
		}
		ValueComparator comp = new ValueComparator(frequencyMap);
		Map<Character, Integer> treeMap = new TreeMap<Character, Integer>(comp);
		treeMap.putAll(frequencyMap);
		System.out.println(treeMap);
		return treeMap;
		
	}
	
	public HuffmanTree<Character, Integer> getHuffmanTree() {
		return huffmanTree;
	}


	public void setHuffmanTree(HuffmanTree<Character, Integer> huffmanTree) {
		this.huffmanTree = huffmanTree;
	}

	public Map<Character, Integer> getFrequencyMap() {
		return frequencyMap;
	}


	public void setFrequencyMap(Map<Character, Integer> frequencyMap) {
		this.frequencyMap = frequencyMap;
	}

	public Map<Character, String> getReplacements() {
		return replacements;
	}


	public void setReplacements(Map<Character, String> replacements) {
		this.replacements = replacements;
	}

	class EntryComparator implements Comparator<HuffmanTree<Character, Integer>> {

		@Override
		public int compare(HuffmanTree<Character, Integer> o1, HuffmanTree<Character, Integer> o2) {
			int value = ((Integer)o1.getValue()) - ((Integer)o2.getValue()) ;
			if (value == 0) {
				return o1.getKey().compareTo(o2.getKey());
			} else {
				return value;
			}
		}
		
	}
	
	public String readFile(String filename){
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(filename));
			while ((sCurrentLine = br.readLine()) != null) {
				sb.append(sCurrentLine + "\n");
			}
	
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			if (br != null) br.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return sb.toString();
	}	
	
}
