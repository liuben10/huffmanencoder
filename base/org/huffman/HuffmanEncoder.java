package org.huffman;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;


public class HuffmanEncoder {
	
	private HuffmanTree<Character, Integer> huffmanTree;
	
	private Map<Character, String> replacements;
	
	private Map<Character, Integer> frequencyMap;
	
	public static void main(String...args) {
		String testString = "sadfjaklfjafaksjflasjdflkasjfasljfasdlkfjhasdkfhsadfjhadslfkhadsjkfhashlfdajkfhfsahdjkfhsafkahfhjdsfhsajdfhals";
		HuffmanEncoder encoder = new HuffmanEncoder();
		Map<Character, Integer> frequencyMap = encoder.sortedFrequencyMap(testString);
		encoder.setFrequencyMap(frequencyMap);
		System.out.println(frequencyMap);
		PriorityQueue<HuffmanTree<Character, Integer>> q = encoder.sortedFrequency(testString);
		HuffmanTree<Character, Integer> resultTree = encoder.buildHuffmanTree(q);
		encoder.setHuffmanTree(resultTree);
		System.out.println(resultTree.treeString());
		Map<Character, String> replacementMap = encoder.generateReplacementMap();
		encoder.setReplacements(replacementMap);
		System.out.println(replacementMap);
		String encoded = encoder.encode(testString);
		System.out.println(encoded);
		String decoded = encoder.decode(encoded);
		System.out.println(decoded);
		System.out.println(testString.equals(decoded));
	}
	
	
	public String encode(String raw) {
		String resultString = "";
		for(char c : raw.toCharArray()) {
			if (replacements.get(c) == null) {
				System.out.println(c + " is null?");
			}
			resultString += replacements.get(c);
		}
		return resultString;
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
		HuffmanTree<Character, Integer> combined = new HuffmanTree(null, e1.getValue() + e2.getValue());
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
			int value = ((Integer) o1.getValue()) - ((Integer) o2.getValue()) ;
			if (value == 0) {
				return o1.getKey().compareTo(o2.getKey());
			} else {
				return value;
			}
		}
		
	}
}
