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
        editor.

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_uwon, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}