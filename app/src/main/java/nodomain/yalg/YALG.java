package nodomain.yalg;

import nodomain.yalg.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class YALG extends Activity {

    public static final int[] m_Levels = {R.raw.level1, R.raw.level20, R.raw.level10};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("Starting YALG.");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_yalg);

        final Button resumeGameButton = (Button)findViewById(R.id.resume_game_button);
        resumeGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Starting Game.");
                Intent myIntent = new Intent(YALG.this, GameActivity.class);

                //this will hold the level to load
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.level_file_key), Context.MODE_PRIVATE) ;
                int iLevel = sharedPref.getInt("level", 0);

                myIntent.putExtra("level", iLevel);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                //start the actual game screen
                YALG.this.startActivity(myIntent);
            }
        });

        final Button newGameButton = (Button)findViewById(R.id.new_game_button);
        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Starting Game.");
                Intent myIntent = new Intent(YALG.this, GameActivity.class);

                myIntent.putExtra("level", 0);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                //start the actual game screen
                YALG.this.startActivity(myIntent);
            }
        });
    }
}
