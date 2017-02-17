package ch.idsia.blip.core.utils.data;

import ch.idsia.blip.core.utils.RandomStuff;

import java.util.ArrayList;
import java.util.HashMap;

public class FastList<K> {

    HashMap<K, Integer> hm;
    public ArrayList<K> ar;

    public FastList() {
        reset();
    }

    public void add(K obj) {
        ar.add(obj);
        hm.put(obj, ar.size() - 1);
    }

    public K rand() {
        int ix = RandomStuff.randInt(0, ar.size()-1);
        return  ar.get(ix);
    }

    public void delete(K obj) {
        int i = hm.get(obj);
        swap(ar, i, ar.size()-1);
        hm.remove(obj);
        hm.put(ar.get(i), i);
        ar.remove(ar.size()-1);
    }

    public void swap(ArrayList<K> v, int i, int j) {
        K t = v.get(i);
        v.set(i, v.get(j));
        v.set(j, t);
    }

    public int size() {
        return ar.size();
    }

    public void reset() {
        hm = new HashMap<K, Integer>();
        ar = new ArrayList<K>();
    }

    public boolean contains(K p) {
        return false;
    }
}
