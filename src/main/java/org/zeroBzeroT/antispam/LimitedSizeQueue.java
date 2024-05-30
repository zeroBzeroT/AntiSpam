package org.zeroBzeroT.antispam;

import java.io.Serial;
import java.util.ArrayList;

public class LimitedSizeQueue<K> extends ArrayList<K> {

	@Serial
    private static final long serialVersionUID = 1L;

	private int maxSize;

	public LimitedSizeQueue(final int size) {
		this.maxSize = size;
	}

	public void setSize(final int size) {
		this.maxSize = size;
	}

	public boolean add(final K k) {
		boolean r = super.add(k);

		if (size() > maxSize) {
			removeRange(0, size() - maxSize);
		}

		return r;
	}
}
