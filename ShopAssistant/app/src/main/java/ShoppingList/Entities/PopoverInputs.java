package ShoppingList.Entities;

import android.widget.EditText;

import java.util.HashMap;

/**
 * Created by Hannes on 2016-12-13.
 *
 * Container for transferring EditTexts to be used in a Popover
 */

public class PopoverInputs {

    private HashMap<String, EditText> inputs;

    public PopoverInputs() {
        this.inputs = new HashMap<>();
    }

    public void addInput(String label, EditText inputET) { inputs.put(label, inputET); }

    // Getter
    public HashMap<String, EditText> getInputs() { return inputs; }

    // Setter
    public void setInputs(HashMap<String, EditText> inputs) { this.inputs = inputs; }
}
