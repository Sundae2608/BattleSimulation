package model.utils;

/**
 * Triplet class storing 3 elements of any hashable types. This implementation is copied from:
 * https://www.techiedelight.com/implement-3-tuple-triplet-java/ and is used to implement the
 * hexagonal indexing system.
 * @param <U>
 * @param <V>
 * @param <T>
 */
public class Triplet<U, V, T>
{
    public final U x;
    public final V y;
    public final T z;

    // Constructs a new Triplet with the given values
    Triplet(U x, V y, T z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
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
        if (!x.equals(triplet.x) ||
                !y.equals(triplet.y) ||
                !z.equals(triplet.z))
            return false;
        return true;
    }

    @Override
    public int hashCode()
    {
		/* Computes hash code for an object by using hash codes of
		the underlying objects */

        int result = x.hashCode();
        result = 31 * result + y.hashCode();
        result = 31 * result + z.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    public static <U, V, T> Triplet <U, V, T> of(U a, V b, T c)
    {
        return new Triplet <>(a, b, c);
    }
}