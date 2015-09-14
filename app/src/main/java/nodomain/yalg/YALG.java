package nodomain.yalg;

import nodomain.yalg.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
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

    public static final int[] m_Levels = {R.raw.level1, R.raw.level10};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("Starting YALG.");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_yalg);

        final Button startGameButton = (Button)findViewById(R.id.start_game_button);
        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Starting Game.");
                Intent myIntent = new Intent(YALG.this, GameActivity.class);

                //this will hold the level to load
                myIntent.putExtra("Level", 0);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                //start the actual game screen
                YALG.this.startActivity(myIntent);
            }
        });
    }
}
