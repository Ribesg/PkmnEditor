package fr.ribesg.pokemon.editor.util;

import java.util.Objects;

/**
 * @author Ribesg
 */
public final class Pair<L, R> {

    private L left;
    private R right;

    public Pair(final L left, final R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return this.left;
    }

    public void setLeft(final L left) {
        this.left = left;
    }

    public R getRight() {
        return this.right;
    }

    public void setRight(final R right) {
        this.right = right;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(this.left, pair.left) &&
               Objects.equals(this.right, pair.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.left, this.right);
    }

    @Override
    public String toString() {
        return "Pair{" +
               "left=" + this.left +
               ", right=" + this.right +
               '}';
    }
}
