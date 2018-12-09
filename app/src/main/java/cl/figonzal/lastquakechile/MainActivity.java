package cl.figonzal.lastquakechile;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private VolleySingleton vl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Buscar toolbar en resources
        toolbar = findViewById(R.id.tool_bar);

        //Setear el toolbar sobre el main activity
        setSupportActionBar(toolbar);

        vl = VolleySingleton.getInstance(getApplicationContext());


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menuItem){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_menu,menuItem);
        return super.onCreateOptionsMenu(menuItem);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch (item.getItemId()) {
            case R.id.refresh:
                return true;

            case R.id.settings:
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }
}
