package com.irisa.ludecol.domain.subdomain;

/**
 * Created by dorian on 12/06/15.
 */
public class Pair<T,U> {

    private T x;
    private U y;

    public Pair(T x, U y) {
        this.x = x;
        this.y = y;
    }

    public T getX() {
        return x;
    }

    public void setX(T x) {
        this.x = x;
    }

    public U getY() {
        return y;
    }

    public void setY(U y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "Pair{" +
            "x=" + x +
            ", y=" + y +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;

        if (x != null ? !x.equals(pair.x) : pair.x != null) return false;
        return !(y != null ? !y.equals(pair.y) : pair.y != null);

    }

    @Override
    public int hashCode() {
        int result = x != null ? x.hashCode() : 0;
        result = 31 * result + (y != null ? y.hashCode() : 0);
        return result;
    }
}
