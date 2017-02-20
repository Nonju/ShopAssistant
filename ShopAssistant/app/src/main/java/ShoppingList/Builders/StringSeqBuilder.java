package ShoppingList.Builders;

/**
 * Created by Hannes on 2016-12-13.
 *
 * Generates string-sequences
 */

public class StringSeqBuilder {

    public static String genString(int amount) { return genString(amount, '-');}
    public static String genString(int amount, char c) {
        return new String(new char[amount]).replace('\0', c);
    }
}
