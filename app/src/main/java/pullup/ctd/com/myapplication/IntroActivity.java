package pullup.ctd.com.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import ctd.solutions.pullup.com.R;

public class IntroActivity extends Activity {

    private Button btnSkipToMainScreen;
    private Button btnLoginWithFacebook;
    private Button btnLoginWithEmail;



    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_intro);
        VideoView videoView = (VideoView) findViewById(R.id.myvideoview);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.dallasvideo1080));

        videoView.setOnPreparedListener (new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        videoView.start();

        btnSkipToMainScreen = (Button) findViewById(R.id.btnSkipToMainScreen);
        btnLoginWithFacebook = (Button) findViewById(R.id.button_facebook_login);
        btnLoginWithEmail = (Button) findViewById(R.id.button_email_login);

        btnSkipToMainScreen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(IntroActivity.this, MainMapActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Intent intent = new Intent(IntroActivity.this, MainMapActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        btnLoginWithEmail.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(IntroActivity.this, MainMapActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Intent intent = new Intent(IntroActivity.this, MainMapActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        btnLoginWithFacebook.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(IntroActivity.this, MainMapActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Intent intent = new Intent(IntroActivity.this, MainMapActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}
