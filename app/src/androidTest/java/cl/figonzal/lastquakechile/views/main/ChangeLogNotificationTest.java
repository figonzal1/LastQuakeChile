package cl.figonzal.lastquakechile.views.main;


import android.content.Context;
import android.content.pm.PackageManager;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import cl.figonzal.lastquakechile.services.SharedPrefService;
import cl.figonzal.lastquakechile.services.notifications.ChangeLogNotification;
import cl.figonzal.lastquakechile.views.activities.MainActivity;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@LargeTest
public class ChangeLogNotificationTest {

    @Rule
    public final ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>(MainActivity.class);

    private UiDevice uiDevice;
    private Context context;

    @Before
    public void setUp() {
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        ChangeLogNotification changeLogNotification = new ChangeLogNotification(context, new SharedPrefService(context));
        changeLogNotification.configNotificationChangeLog();
    }

    @Test
    public void testNotificationChangeLog() throws PackageManager.NameNotFoundException {

        String esperado_title =
                "¡Novedades! v" + context.getPackageManager().getPackageInfo(context.getPackageName(),
                        0).versionName;

        uiDevice.pressHome();
        uiDevice.openNotification();
        uiDevice.wait(Until.hasObject(By.text(esperado_title)), 7000);

        UiObject2 uiObject = uiDevice.findObject(By.text(esperado_title));
        uiObject.click();

        String actual_title = uiDevice.findObject(By.text(esperado_title)).getText();
        assertEquals(esperado_title, actual_title);

        uiDevice.pressBack();
    }

}
