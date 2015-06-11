

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import org.instructures.ArgsParser;
import org.instructures.Operand;
import org.instructures.Option;


public class HuffmanCodes {
	
	private HuffmanTree<Character, Integer> huffmanTree;
	
	private static final Option ENCODE, DECODE, SHOW_FREQUENCY, SHOW_CODES, SHOW_BINARY;
	
	private static final Operand<File> FILES;
	
	private Map<Character, String> replacements;
	
	private Map<Character, Integer> frequencyMap;
	
	private static ArgsParser parser;
	
	private static final String BEGINNING_STRING = "00001000";
	
	public File inputFile;
	
	public File outputFile;
	
	private int startIndex;
	
	private String inputString;
	
	private String encodedString;

	private String binary;
	

	
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
			encodedString = buildEncodedString(inputStream);
		}
	}
	
	private String buildEncodedString(BitInputStream inputStream) {
		String result = "";
		while(true) {
			try {
				result += inputStream.readBit();
			} catch (Exception e) {
				break;
			}
		}
		return result;
	}

	public static void main(String...args) throws FileNotFoundException, IOException {
		ArgsParser.Bindings bindings = parser.parse(args);
		HuffmanCodes encoder = new HuffmanCodes(bindings);
		if (bindings.hasOption(ENCODE)) {
			encoder.encode();
		} else {
			encoder.decode();
		}
		
		if (bindings.hasOption(SHOW_CODES)) {
			encoder.printShowCodes();
		}
		
		if (bindings.hasOption(SHOW_BINARY)) {
			encoder.showBinary();
		}
	}
	
	public void printShowCodes() {
		Set<Character> keyset = replacements.keySet();
		Character[] keys = keyset.toArray(new Character[keyset.size()]);
		System.out.println("CODES");
		for(Character c : keys) {
			System.out.println("\""  + replacements.get(c) + "\" -> '" + c + "'");
		}
	}
	
	public void showBinary() {
		System.out.println("ENCODED SEQUENCE");
		System.out.println(this.binary);
	}

	public void init(String raw) {
		Map<Character, Integer> frequencyMap = sortedFrequencyMap(raw);
		setFrequencyMap(frequencyMap);
		this.inputString = raw;
		PriorityQueue<HuffmanTree<Character, Integer>> q = sortedFrequency(raw);
		HuffmanTree<Character, Integer> resultTree = buildHuffmanTree(q);
		setHuffmanTree(resultTree);
		
		Map<Character, String> replacementMap = generateReplacementMap();
		setReplacements(replacementMap);
		
	}
	
	public void encode() {
		encode(inputString);
	}
	private String encode(String raw) {
		System.out.println("encoded input = " + raw + " for file = " + this.inputFile);
		String resultStream = "";
		String header = encodeTree();
		
		for(char c : raw.toCharArray()) {
			resultStream += replacements.get(c);
		}
		this.binary = resultStream;
		try {
			writeBits(header, resultStream);
		} catch (Exception e) { 
			e.printStackTrace();
		}
		return header + resultStream;
	}
	
	private void writeBits(String header, String resultStream) throws IOException {
		BitOutputStream out = new BitOutputStream(outputFile);
		out.writeInt(header.length());
		System.out.println("headerlength = " + header.length());
		out.writeInt(resultStream.length());
		System.out.println("textlength = " + resultStream.length());
		for(char c : header.toCharArray()) {
			if (c == '1') {
				out.writeBit(1);
			} else {
				out.writeBit(0);
			}
		}
		for(char c : resultStream.toCharArray()) {
			if (c == '1') {
				out.writeBit(1);
			} else {
				out.writeBit(0);
			}
		}

		out.close();
		
	}
	
	public String encodeTree() {
		String encodedTree = huffmanTree.encode(8);
		return BEGINNING_STRING + encodedTree;
	}
	
	public void decode() {
		char[] encodedbits = encodedString.toCharArray();
		char[] headerLengthBits = subArray(encodedbits, 0, 32);
		char[] textLengthBits = subArray(encodedbits, 32, 32);
		int headerLength = fromBinaryString(new String(headerLengthBits));
		int textLength = fromBinaryString(new String(textLengthBits));
		System.out.println(encodedString);
		String encoded = encodedString.substring(64);
		String decodedString = decode(encoded, headerLength, textLength);
		System.out.println("Decoded output = " + decodedString);
		init(decodedString);
		writeFile(decodedString);
		
	}
	
	private void writeFile(String inputString) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(outputFile.getAbsolutePath());
			pw.print(inputString);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pw.close();
		}
	}
	
	public String decode(String encoded, int headerLength, int textLength) {
		System.out.println(encoded);
		String resultString = "";
		HuffmanTree<Character, Integer> huffmanTree = buildTree(encoded);
		System.out.println("new start index = " + startIndex);
		HuffmanTree<Character, Integer> iterator = huffmanTree;
		char[] encodedBinary = subArray(encoded.toCharArray(), startIndex, textLength );
		this.binary = new String(encodedBinary);
		for(char c : encodedBinary) {
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
	
	public HuffmanTree<Character, Integer> buildTree(String encoded) {
		char[] chars = encoded.toCharArray();
		startIndex = 8;
		return readTree(chars);
	}
	
	private HuffmanTree<Character, Integer> readTree(char[] chars) {
		if (chars[startIndex] == '1') {
			char[] asciiArray = subArray(chars, startIndex+1, 8);
			String binaryString = new String(asciiArray);
			char asciiChar = (char) fromBinaryString(binaryString);

			startIndex += 9;
			return new HuffmanTree<Character, Integer>(asciiChar, 0);
		} else {
			startIndex += 1;
			HuffmanTree<Character, Integer> treeToBuild = new HuffmanTree<Character, Integer>('\0', 0);
			treeToBuild.setLeftChild(readTree(chars));
			treeToBuild.setRightChild(readTree(chars));
			return treeToBuild;
		}
	}
	
	private char[] subArray(char[] chars, int startIndex, int length) {
		char[] subArray = new char[length];
		System.arraycopy(chars, startIndex, subArray, 0, length);
		return subArray;
	}
	
	private int fromBinaryString(String binaryString) {
		return Integer.parseInt(binaryString, 2);
	}

	public Map<Character, String> generateReplacementMap() {
		Map<Character, String> replacements = new HashMap<Character, String>();
		System.out.println(huffmanTree.treeString());
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
		HuffmanTree<Character, Integer> combined = new HuffmanTree<Character, Integer>('\0', e1.getValue() + e2.getValue());
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
		String result = "";
		try {
			byte[] data = Files.readAllBytes(Paths.get(filename));
			result = new String(data, "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}	
	
//	public String readFile(String filename){
//		BufferedReader br = null;
//		StringBuilder sb = new StringBuilder();
//		
//		try {
//			String sCurrentLine;
//			br = new BufferedReader(new FileReader(filename));
//			while ((sCurrentLine = br.readLine()) != null) {
//				sb.append(sCurrentLine + "\n");
//			}
//	
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			if (br != null) br.close();
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		}
//		return sb.toString();
//	}
//	

	final class HuffmanTree<K, V> implements Map.Entry<K, V> {
	    private final K key;
	    private V value;
	    
	    private HuffmanTree<K, V> leftChild;
	    
	    private HuffmanTree<K, V> rightChild;

	    public HuffmanTree(K key, V value) {
	        this.key = key;
	        this.value = value;
	    }
	    
	    public String treeString() {
	    	return treeStringHelper(0);
	    }
	    
	    public String pathString(K key, String pathSoFar) {
	    	if ((Character) this.key != '\0' && this.key.equals(key)) {
	    		return pathSoFar;
	    	} else {
	    		String leftPath = "";
	    		if (this.leftChild != null) {
	    			leftPath = this.leftChild.pathString(key, pathSoFar + "0");
	    		}
	    		String rightPath = "";
	    		if (leftPath.equals("")){
	        		if (this.rightChild != null) {
	        			rightPath = this.rightChild.pathString(key, pathSoFar + "1");
	        		}
	        		return rightPath;
	    		} else {
	    			return leftPath;
	    		}
	    	}
	    }
	    
	    public String treeStringHelper(int depth) {
	    	String result = "\n" + pad(depth) + this.toString();
	    	if (leftChild != null) {
	    		result += leftChild.treeStringHelper(depth + 1);
	    	}
	    	if (rightChild != null) {
	    		result += rightChild.treeStringHelper(depth + 1);
	    	}
	    	return result;
	    }
	    
	    private String pad(int depth) {
	    	String padding = "";
	    	for(int i = 0; i < depth; i++) {
	    		padding += " ";
	    	}
	    	return padding;
	    }
	    

	    @Override
	    public K getKey() {
	        return key;
	    }
	    
	    @Override
	    public boolean equals(Object o) {
	    	return o != null && o instanceof HuffmanTree && ((HuffmanTree) o).getKey().equals(this.getKey());
	    }
	    
	    @Override
	    public String toString() {
	    	return key + "=" + value;
	    }

	    @Override
	    public V getValue() {
	        return value;
	    }

	    @Override
	    public V setValue(V value) {
	        V old = this.value;
	        this.value = value;
	        return old;
	    }
	    
	    private String keyToBinaryString() {
	    	return Integer.toBinaryString((int)((Character) this.key).charValue());
	    }
	    
	    public String encode(int bits) {
	    	if (this.isLeaf()) {
	    		return "1" + String.format("%8s", keyToBinaryString()).replace(' ', '0');
	    	} else {
	    		return "0" + getLeftChild().encode(bits) + getRightChild().encode(bits);
	    	}
	    }

		public HuffmanTree<K, V> getLeftChild() {
			return leftChild;
		}

		public void setLeftChild(HuffmanTree<K, V> leftChild) {
			this.leftChild = leftChild;
		}

		public HuffmanTree<K, V> getRightChild() {
			return rightChild;
		}

		public void setRightChild(HuffmanTree<K, V> rightChild) {
			this.rightChild = rightChild;
		}

		public boolean isLeaf() {
			return leftChild == null && rightChild == null;
		}
	}
	
	public class ValueComparator implements Comparator<Character>{
		
		public Map<Character, Integer> frequencies;

		public ValueComparator(Map<Character, Integer> frequencies) {
			this.frequencies = frequencies;
		}

		@Override
		public int compare(Character o1, Character o2) {
			int value = frequencies.get(o1) - frequencies.get(o2);
			if (value == 0) {
				return o1.compareTo(o2);
			} else {
				return value;
			}
		}

	}
	
}
