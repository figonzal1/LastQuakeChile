package cl.figonzal.lastquakechile.views.main;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.Direction;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import cl.figonzal.lastquakechile.views.MainActivity;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@LargeTest
public class ChangeLogNotificationTest {

    @Rule
    public final ActivityTestRule<MainActivity> testRule =
            new ActivityTestRule<>(MainActivity.class);

    private UiDevice uiDevice;
    private Context context;

    @Before
    public void setUp() {
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        testRule.getActivity().notificationChangeLog(true);
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void testNotificationChangeLog() throws PackageManager.NameNotFoundException {

        String esperado_title =
                "¡Novedades! v" + context.getPackageManager().getPackageInfo(context.getPackageName(),
                        0).versionName;

        uiDevice.pressHome();
        uiDevice.openNotification();
        UiObject2 uiObject = uiDevice.findObject(By.text(esperado_title));
        uiObject.swipe(Direction.DOWN, 0.05f);

        uiDevice.wait(Until.hasObject(By.text(esperado_title)), 7000);

        String actual_title = uiDevice.findObject(By.text(esperado_title)).getText();

        assertEquals(esperado_title, actual_title);
    }

}