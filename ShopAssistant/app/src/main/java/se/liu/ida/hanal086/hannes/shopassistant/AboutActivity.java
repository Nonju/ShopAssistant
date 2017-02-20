package se.liu.ida.hanal086.hannes.shopassistant;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Button backToMainMenuButton = (Button) findViewById(R.id.about_backToMainMenuButton);
        backToMainMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { backToMainMenuButtonClicked(); }
        });
    }

    // Returns to main menu when done reading
    private void backToMainMenuButtonClicked() { finish(); }
}
