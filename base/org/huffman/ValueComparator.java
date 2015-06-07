package org.huffman;

import java.util.Comparator;
import java.util.Map;

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
