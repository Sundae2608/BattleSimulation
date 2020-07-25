package model.utils;

/**
 * Triplet class storing 3 elements of any hashable types. This implementation is copied from:
 * https://www.techiedelight.com/implement-3-tuple-triplet-java/ and is used to implement the
 * hexagonal indexing system.
 * @param <U>
 * @param <V>
 * @param <T>
 */
class Triplet<U, V, T>
{
    public final U first;
    public final V second;
    public final T third;

    // Constructs a new Triplet with the given values
    Triplet(U first, V second, T third)
    {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @Override
    public boolean equals(Object o)
    {
        // Checks specified object is "equal to" current object or not
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Triplet triplet = (Triplet) o;

        // Call equals() method of the underlying objects
        if (!first.equals(triplet.first) ||
                !second.equals(triplet.second) ||
                !third.equals(triplet.third))
            return false;
        return true;
    }

    @Override
    public int hashCode()
    {
		/* Computes hash code for an object by using hash codes of
		the underlying objects */

        int result = first.hashCode();
        result = 31 * result + second.hashCode();
        result = 31 * result + third.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "(" + first + ", " + second + ", " + third + ")";
    }

    public static <U, V, T> Triplet <U, V, T> of(U a, V b, T c)
    {
        return new Triplet <>(a, b, c);
    }
}