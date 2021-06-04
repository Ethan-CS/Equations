package io.github.ethankelly.graph;

public class Vertex {

	public int getLocation() {
		return location;
	}

	private final int location;

	public Vertex(int location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return String.valueOf(location);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Vertex vertex = (Vertex) o;

		return this.location == vertex.location;
	}

	@Override
	public Vertex clone() {
		try {
			return (Vertex) super.clone();
		} catch (CloneNotSupportedException e) {
			return new Vertex(this.getLocation());
		}
	}

	@Override
	public int hashCode() {
		return location;
	}
}
