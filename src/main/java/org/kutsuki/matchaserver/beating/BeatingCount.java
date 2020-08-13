package org.kutsuki.matchaserver.beating;

public class BeatingCount implements Comparable<BeatingCount> {
    private String name;
    private int count;

    public BeatingCount(String name, int count) {
	this.name = name;
	this.count = count;
    }

    @Override
    public int compareTo(BeatingCount rhs) {
	int result = Integer.compare(getCount(), rhs.getCount());

	if (result == 0) {
	    result = getName().compareTo(rhs.getName());
	}

	return result;
    }

    public String getName() {
	return name;
    }

    public int getCount() {
	return count;
    }
}
