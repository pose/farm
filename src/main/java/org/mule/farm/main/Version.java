package org.mule.farm.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class Version implements Comparable<Version> {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((versionNumbers == null) ? 0 : versionNumbers.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Version other = (Version) obj;
		if (versionNumbers == null) {
			if (other.versionNumbers != null)
				return false;
		} else if (this.compareTo(other) != 0)
			return false;
		return true;
	}

	public List<Integer> getVersionNumbers() {
		return versionNumbers;
	}

	private List<Integer> versionNumbers;
	
	public static Version inferVersionFromFileName(String path) {
		for ( String s : path.split("-|/") ) {
			if ( StringUtils.startsWithAny(s, "0","1","2","3","4","5","6","7","8","9") ) {
				String [] numbers = s.split("\\.");
				
				String versionString = null;
				int i = numbers.length - 1;
				for ( ; i >= 0; i--) {
					if (StringUtils.startsWithAny(numbers[i],  "0","1","2","3","4","5","6","7","8","9")) {
						break;
					}
				}
				
				if (!StringUtils.startsWithAny(numbers[numbers.length - 1],  "0","1","2","3","4","5","6","7","8","9")) {
					versionString = StringUtils.join(Arrays.copyOfRange(numbers, 0, i + 1), ".");
					return fromString(versionString);
				} else {
					continue;
				}
			}
		}
		throw new IllegalArgumentException();
	}

	public static Version fromString(String versionString) {
		Validate.notNull(versionString);
		ArrayList<Integer> versionNumbers = new ArrayList<Integer>();
		for (String versionNumber : versionString.split("\\.")) {
			versionNumbers.add(Integer.parseInt(versionNumber));
		}
		return new Version(versionNumbers);
	}

	private Version(List<Integer> versionNumbers) {
		Validate.notNull(versionNumbers);
		Validate.notEmpty(versionNumbers);
		Validate.noNullElements(versionNumbers);
		this.versionNumbers = versionNumbers;
	}

	@Override
	public String toString() {
		return StringUtils.join(versionNumbers, ".");
	}
	
	private Integer getOrZero(int position) {
		if ( this.versionNumbers.size() <= position ) {
			return 0;
		}
		return this.versionNumbers.get(position);
	}

	@Override
	public int compareTo(Version o) {
		int max = Math.max(this.versionNumbers.size(), o.versionNumbers.size());
		for ( int i = 0; i < max; i++) {
			int comparisonResult = this.getOrZero(i).compareTo(o.getOrZero(i));
			
			if ( comparisonResult != 0 ) {
				return comparisonResult;
			}
		}
		return 0;
	}
}
