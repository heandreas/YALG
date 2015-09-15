package nodomain.yalg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class UWonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uwon);

        Bundle extras = getIntent().getExtras();
        final int lastLevelID = extras.getInt("level");

        //calculate index of next level
        int nextLevel = lastLevelID + 1;
        if (nextLevel >= YALG.m_Levels.length)
            nextLevel = 0;

        final int nextLevelForSure = nextLevel;

        //write current level to stash
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.level_file_key), Context.MODE_PRIVATE) ;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("level", nextLevelForSure);
        editor.commit();

        final Button startGameButton = (Button)findViewById(R.id.next_level_button);
        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(UWonActivity.this, GameActivity.class);

                //this will hold the level to load

                myIntent.putExtra("level", nextLevelForSure);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                //start the actual game screen
                UWonActivity.this.startActivity(myIntent);
            }
        });
    }
}
