package pw.swordfish.util;

import java.util.HashMap;
import java.util.Set;

public class HashMultiSet<T> {

	private HashMap<T, Integer> hashMultiSet;
	private int totalCount;

	/**
	 * Construct a new HashMultiSet
	 */
    public HashMultiSet() {
		hashMultiSet = new HashMap<T, Integer>();
		totalCount = 0;
	}

	/**
	 * Adds a single occurrence of the specified element to this multiset
	 * @param element the element to add
	 */
	public void add(T element) {
		/*
		 * now we want to increment our total count and check to see if the element already exists,
		 * if it does, then we remove it and add it with an incremented counter variable
		 */
		totalCount++;
		if (hashMultiSet.containsKey(element)) {
			int elementCount = count(element) + 1;
			hashMultiSet.remove(element);
			hashMultiSet.put(element, elementCount);
		} else
			hashMultiSet.put(element, 1);
	}

	/**
	 * Adds hashMultiSet to this hashMultiSet
	 * @param hashMultiSet the hashMultiSet to add
	 */
	public void add(HashMultiSet<T> hashMultiSet) { 
		this.hashMultiSet.putAll(hashMultiSet.toHashMap());
	}

	/**
	 * Get the hashMap representing this HashMultiSet
	 * @return the hashMap representing this HashMultiSet
	 */
	public HashMap<T, Integer> toHashMap() {
		return hashMultiSet; 
	}

    /**
	 * Determines whether this multiset contains the specified element
	 * @param element the element to compare to
	 * @return true if the element is within this multiset, false otherwise
	 */
	public boolean contains(T element) {
		return hashMultiSet.containsKey(element);
	}

	/**
	 * Returns the number of occurrences of an element in this multiset (the count of the element)
	 * @param element the element to count the occurrences of
	 * @return the number of occurrences of that element
	 */
	public int count(T element) {
		try {
			return hashMultiSet.get(element);
		} catch (NullPointerException ex) {
            return 0;
		}
	}

	/**
	 * Returns the total count of all elements in this multiset
	 * @return the total number of all elements in this multiset
	 */
	public int countAll() {
		return totalCount;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (T element : hashMultiSet.keySet())
			result.append(element);
		return result.toString();
	}

	/**
	 * Get the KeySet
	 * @return the key set
	 */
	public Set<T> getKeySet() {
		return hashMultiSet.keySet();
	}
}
