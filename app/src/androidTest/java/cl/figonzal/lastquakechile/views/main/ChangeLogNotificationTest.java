package cl.figonzal.lastquakechile.views.main;

/*
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
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ChangeLogNotification changeLogNotification = new ChangeLogNotification();
        changeLogNotification.trySendNotificationChangeLog(true, context);
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
*/