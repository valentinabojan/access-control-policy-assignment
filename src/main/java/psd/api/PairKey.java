package psd.api;

import java.io.Serializable;

public class PairKey implements Serializable{

    private String key1;
    private String key2;

    public PairKey() {
    }

    public PairKey(String key1, String key2) {
        this.key1 = key1;
        this.key2 = key2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PairKey that = (PairKey) o;

        if (!key1.equals(that.key1)) return false;
        if (!key2.equals(that.key2)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key1.hashCode();
        result = 31 * result + key2.hashCode();
        return result;
    }
}
