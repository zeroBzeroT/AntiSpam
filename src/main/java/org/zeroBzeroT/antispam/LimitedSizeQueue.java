package org.zeroBzeroT.antispam;

import java.util.ArrayList;

public class LimitedSizeQueue<K> extends ArrayList<K> {
	private static final long serialVersionUID = 1L;

	private int maxSize;

	public LimitedSizeQueue(int size) {
		this.maxSize = size;
	}

	public void setSize(int size) {
		this.maxSize = size;
	}

	public boolean add(K k) {
		boolean r = super.add(k);

		if (size() > maxSize) {
			removeRange(0, size() - maxSize);
		}

		return r;
	}
}
