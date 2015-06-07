package org.huffman;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.TreeMap;


public class HuffmanEncoder {
	
	public static void main(String...args) {
		String testString = "sadfjaklfjafaksjflasjdflkasjfasljfasdlkfjhasdkfhsadfjhadslfkhadsjkfhashlfdajkfhfsahdjkfhsafkahfhjdsfhsajdfhals";
		HuffmanEncoder encoder = new HuffmanEncoder();
		System.out.println(encoder.sortedFrequencyMap(testString));
		PriorityQueue<MyEntry<Character, Integer>> q = encoder.sortedFrequency(testString);
	}
	
	
	public void combineAndSortLowest(PriorityQueue<MyEntry<Character, Integer>> q) {
		if (q.size() >= 2) {
			combineAndSort(q);
		}
	}
	
	public void combineAndSort(PriorityQueue<MyEntry<Character, Integer>> q) {
		MyEntry<Character, Integer> e1 = q.remove();
		MyEntry<Character, Integer> e2 = q.remove();
		MyEntry<Character, Integer> combined = combine(e1, e2);
		q.add(combined);
	}
	
	private MyEntry<Character, Integer> combine(MyEntry<Character, Integer> e1,
			MyEntry<Character, Integer> e2) {
		return new MyEntry(null, e1.getValue() + e2.getValue());
	}


	public PriorityQueue<MyEntry<Character, Integer>> sortedFrequency(String raw) {
		Map<Character, Integer> sortedMap = sortedFrequencyMap(raw);
		List<MyEntry<Character, Integer>> entryList = convertEntrySet(sortedMap);
		PriorityQueue<MyEntry<Character, Integer>> entryQ = new PriorityQueue<MyEntry<Character, Integer>>(new EntryComparator());
		for(MyEntry<Character, Integer> entry : entryList) {
			entryQ.add(entry);
		}
		return entryQ;
	}
	
	private List<MyEntry<Character, Integer>> convertEntrySet(Map<Character, Integer> map) {
		ArrayList<MyEntry<Character, Integer>> result = new ArrayList<MyEntry<Character, Integer>>();
		for(Entry<Character, Integer> entry : map.entrySet()) {
			result.add(new MyEntry<Character, Integer>(entry.getKey(), entry.getValue()));
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
	
	final class MyEntry<K, V> implements Map.Entry<K, V> {
	    private final K key;
	    private V value;

	    public MyEntry(K key, V value) {
	        this.key = key;
	        this.value = value;
	    }

	    @Override
	    public K getKey() {
	        return key;
	    }
	    
	    @Override
	    public boolean equals(Object o) {
	    	return o != null && o instanceof MyEntry && ((MyEntry) o).getKey().equals(this.getKey());
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
	}
	


	
	class EntryComparator implements Comparator<MyEntry<Character, Integer>> {

		@Override
		public int compare(MyEntry<Character, Integer> o1, MyEntry<Character, Integer> o2) {
			return ((Integer) o1.getValue()) - ((Integer) o2.getValue());
		}
		
	}
}
