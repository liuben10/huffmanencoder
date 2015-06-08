package org.huffman;

import java.util.Map;

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
    	if (this.key != null && this.key.equals(key)) {
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