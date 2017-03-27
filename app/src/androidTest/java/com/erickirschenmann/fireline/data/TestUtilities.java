package com.erickirschenmann.fireline.data;

import static com.erickirschenmann.fireline.data.TestIncidentDatabase.REFLECTED_COLUMN_ADDRESS;
import static com.erickirschenmann.fireline.data.TestIncidentDatabase.REFLECTED_COLUMN_BLOCK;
import static com.erickirschenmann.fireline.data.TestIncidentDatabase.REFLECTED_COLUMN_CITY;
import static com.erickirschenmann.fireline.data.TestIncidentDatabase.REFLECTED_COLUMN_COMMENT;
import static com.erickirschenmann.fireline.data.TestIncidentDatabase.REFLECTED_COLUMN_DATE;
import static com.erickirschenmann.fireline.data.TestIncidentDatabase.REFLECTED_COLUMN_INCIDENT_ID;
import static com.erickirschenmann.fireline.data.TestIncidentDatabase.REFLECTED_COLUMN_INCIDENT_NUMBER;
import static com.erickirschenmann.fireline.data.TestIncidentDatabase.REFLECTED_COLUMN_INCIDENT_TYPE;
import static com.erickirschenmann.fireline.data.TestIncidentDatabase.REFLECTED_COLUMN_LATITUDE;
import static com.erickirschenmann.fireline.data.TestIncidentDatabase.REFLECTED_COLUMN_LONGITUDE;
import static com.erickirschenmann.fireline.data.TestIncidentDatabase.REFLECTED_COLUMN_RESPONSE_DATE;
import static com.erickirschenmann.fireline.data.TestIncidentDatabase.REFLECTED_COLUMN_STATUS;
import static com.erickirschenmann.fireline.data.TestIncidentDatabase.REFLECTED_COLUMN_UNITS;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import com.erickirschenmann.fireline.utilities.IncidentDateUtils;
import com.erickirschenmann.fireline.utils.PollingCheck;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestUtilities {
  /* October 1st, 2016 at midnight, GMT time */
  static final long DATE_NORMALIZED = 1475280000000L;

  static final int BULK_INSERT_RECORDS_TO_INSERT = 10;

  /**
   * Ensures there is a non empty cursor and validates the cursor's data by checking it against a
   * set of expected values. This method will then close the cursor.
   *
   * @param error Message when an error occurs
   * @param valueCursor The Cursor containing the actual values received from an arbitrary query
   * @param expectedValues The values we expect to receive in valueCursor
   */
  static void validateThenCloseCursor(
      String error, Cursor valueCursor, ContentValues expectedValues) {
    assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
    validateCurrentRecord(error, valueCursor, expectedValues);
    valueCursor.close();
  }

  /**
   * This method iterates through a set of expected values and makes various assertions that will
   * pass if our app is functioning properly.
   *
   * @param error Message when an error occurs
   * @param valueCursor The Cursor containing the actual values received from an arbitrary query
   * @param expectedValues The values we expect to receive in valueCursor
   */
  static void validateCurrentRecord(
      String error, Cursor valueCursor, ContentValues expectedValues) {
    Set<Entry<String, Object>> valueSet = expectedValues.valueSet();

    for (Map.Entry<String, Object> entry : valueSet) {
      String columnName = entry.getKey();
      int index = valueCursor.getColumnIndex(columnName);

      /* Test to see if the column is contained within the cursor */
      String columnNotFoundError = "Column '" + columnName + "' not found. " + error;
      assertFalse(columnNotFoundError, index == -1);

      /* Test to see if the expected value equals the actual value (from the Cursor) */
      String expectedValue = entry.getValue().toString();
      String actualValue = valueCursor.getString(index);

      String valuesDontMatchError =
          "Actual value '"
              + actualValue
              + "' did not match the expected value '"
              + expectedValue
              + "'. "
              + error;

      assertEquals(valuesDontMatchError, expectedValue, actualValue);
    }
  }

  /**
   * Used as a convenience method to return a singleton instance of ContentValues to populate our
   * database or insert using our ContentProvider.
   *
   * @return ContentValues that can be inserted into our ContentProvider or incident.db
   */
  static ContentValues createTestIncidentContentValues() {

    ContentValues testIncidentValues = new ContentValues();

    testIncidentValues.put(REFLECTED_COLUMN_DATE, DATE_NORMALIZED);
    testIncidentValues.put(REFLECTED_COLUMN_INCIDENT_ID, 1);
    testIncidentValues.put(REFLECTED_COLUMN_ADDRESS, "Martha Dr.");
    testIncidentValues.put(REFLECTED_COLUMN_BLOCK, "600");
    testIncidentValues.put(REFLECTED_COLUMN_CITY, "Newbury Park");
    testIncidentValues.put(REFLECTED_COLUMN_COMMENT, "comment");
    testIncidentValues.put(REFLECTED_COLUMN_INCIDENT_NUMBER, "1");
    testIncidentValues.put(REFLECTED_COLUMN_INCIDENT_TYPE, "Medical");
    testIncidentValues.put(REFLECTED_COLUMN_LATITUDE, 34.5);
    testIncidentValues.put(REFLECTED_COLUMN_LONGITUDE, -118.2);
    testIncidentValues.put(REFLECTED_COLUMN_RESPONSE_DATE, "3/1/2017 1:00");
    testIncidentValues.put(REFLECTED_COLUMN_STATUS, "On Scene");
    testIncidentValues.put(REFLECTED_COLUMN_UNITS, "MED443");

    return testIncidentValues;
  }

  /**
   * Used as a convenience method to return a singleton instance of an array of ContentValues to
   * populate our database or insert using our ContentProvider's bulk insert method.
   *
   * <p>It is handy to have utility methods that produce test values because it makes it easy to
   * compare results from ContentProviders and databases to the values you expect to receive. See
   * {@link #validateCurrentRecord(String, Cursor, ContentValues)} and {@link
   * #validateThenCloseCursor(String, Cursor, ContentValues)} for more information on how this
   * verification is performed.
   *
   * @return Array of ContentValues that can be inserted into our ContentProvider or incident.db
   */
  static ContentValues[] createBulkInsertTestIncidentValues() {

    ContentValues[] bulkTestIncidentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

    long testDate = TestUtilities.DATE_NORMALIZED;
    long normalizedTestDate = IncidentDateUtils.normalizeDate(testDate);

    for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++) {

      normalizedTestDate += IncidentDateUtils.DAY_IN_MILLIS;

      ContentValues testIncidentValues = new ContentValues();

      testIncidentValues.put(REFLECTED_COLUMN_DATE, DATE_NORMALIZED);
      testIncidentValues.put(REFLECTED_COLUMN_INCIDENT_ID, 1);
      testIncidentValues.put(REFLECTED_COLUMN_ADDRESS, "Martha Dr.");
      testIncidentValues.put(REFLECTED_COLUMN_BLOCK, "600");
      testIncidentValues.put(REFLECTED_COLUMN_CITY, "Newbury Park");
      testIncidentValues.put(REFLECTED_COLUMN_COMMENT, "comment");
      testIncidentValues.put(REFLECTED_COLUMN_INCIDENT_NUMBER, "1");
      testIncidentValues.put(REFLECTED_COLUMN_INCIDENT_TYPE, "Medical");
      testIncidentValues.put(REFLECTED_COLUMN_LATITUDE, 34.5);
      testIncidentValues.put(REFLECTED_COLUMN_LONGITUDE, -118.2);
      testIncidentValues.put(REFLECTED_COLUMN_RESPONSE_DATE, "3/1/2017 1:00");
      testIncidentValues.put(REFLECTED_COLUMN_STATUS, "On Scene");
      testIncidentValues.put(REFLECTED_COLUMN_UNITS, "MED443");

      bulkTestIncidentValues[i] = testIncidentValues;

      //
      //      ContentValues incidentValues = new ContentValues();
      //
      //      incidentValues.put(REFLECTED_COLUMN_DATE, normalizedTestDate);
      //      incidentValues.put(REFLECTED_COLUMN_WIND_DIR, 1.1);
      //      incidentValues.put(REFLECTED_COLUMN_HUMIDITY, 1.2 + 0.01 * (float) i);
      //      incidentValues.put(REFLECTED_COLUMN_PRESSURE, 1.3 - 0.01 * (float) i);
      //      incidentValues.put(REFLECTED_COLUMN_MAX, 75 + i);
      //      incidentValues.put(REFLECTED_COLUMN_MIN, 65 - i);
      //      incidentValues.put(REFLECTED_COLUMN_WIND_SPEED, 5.5 + 0.2 * (float) i);
      //      incidentValues.put(REFLECTED_COLUMN_WEATHER_ID, 321);

      //      bulkTestIncidentValues[i] = incidentValues;
    }

    return bulkTestIncidentValues;
  }

  static TestContentObserver getTestContentObserver() {
    return TestContentObserver.getTestContentObserver();
  }

  static String getConstantNameByStringValue(Class klass, String value) {
    for (Field f : klass.getDeclaredFields()) {
      int modifiers = f.getModifiers();
      Class<?> type = f.getType();
      boolean isPublicStaticFinalString =
          Modifier.isStatic(modifiers)
              && Modifier.isFinal(modifiers)
              && Modifier.isPublic(modifiers)
              && type.isAssignableFrom(String.class);

      if (isPublicStaticFinalString) {
        String fieldName = f.getName();
        try {
          String fieldValue = (String) klass.getDeclaredField(fieldName).get(null);
          if (fieldValue.equals(value)) return fieldName;
        } catch (IllegalAccessException e) {
          return null;
        } catch (NoSuchFieldException e) {
          return null;
        }
      }
    }

    return null;
  }

  static String getStaticStringField(Class clazz, String variableName)
      throws NoSuchFieldException, IllegalAccessException {
    Field stringField = clazz.getDeclaredField(variableName);
    stringField.setAccessible(true);
    String value = (String) stringField.get(null);
    return value;
  }

  static Integer getStaticIntegerField(Class clazz, String variableName)
      throws NoSuchFieldException, IllegalAccessException {
    Field intField = clazz.getDeclaredField(variableName);
    intField.setAccessible(true);
    Integer value = (Integer) intField.get(null);
    return value;
  }

  static String studentReadableClassNotFound(ClassNotFoundException e) {
    String message = e.getMessage();
    int indexBeforeSimpleClassName = message.lastIndexOf('.');
    String simpleClassNameThatIsMissing = message.substring(indexBeforeSimpleClassName + 1);
    simpleClassNameThatIsMissing = simpleClassNameThatIsMissing.replaceAll("\\$", ".");
    String fullClassNotFoundReadableMessage =
        "Couldn't find the class "
            + simpleClassNameThatIsMissing
            + ".\nPlease make sure you've created that class and followed the TODOs.";
    return fullClassNotFoundReadableMessage;
  }

  static String studentReadableNoSuchField(NoSuchFieldException e) {
    String message = e.getMessage();

    Pattern p = Pattern.compile("No field (\\w*) in class L.*/(\\w*\\$?\\w*);");

    Matcher m = p.matcher(message);

    if (m.find()) {
      String missingFieldName = m.group(1);
      String classForField = m.group(2).replaceAll("\\$", ".");
      String fieldNotFoundReadableMessage =
          "Couldn't find "
              + missingFieldName
              + " in class "
              + classForField
              + "."
              + "\nPlease make sure you've declared that field and followed the TODOs.";
      return fieldNotFoundReadableMessage;
    } else {
      return e.getMessage();
    }
  }

  /**
   * Students: The functions we provide inside of TestIncidentProvider use TestContentObserver to
   * test the ContentObserver callbacks using the PollingCheck class from the Android Compatibility
   * Test Suite tests.
   *
   * <p>NOTE: This only tests that the onChange function is called; it DOES NOT test that the
   * correct Uri is returned.
   */
  static class TestContentObserver extends ContentObserver {
    final HandlerThread mHT;
    boolean mContentChanged;

    private TestContentObserver(HandlerThread ht) {
      super(new Handler(ht.getLooper()));
      mHT = ht;
    }

    static TestContentObserver getTestContentObserver() {
      HandlerThread ht = new HandlerThread("ContentObserverThread");
      ht.start();
      return new TestContentObserver(ht);
    }

    /**
     * Called when a content change occurs.
     *
     * <p>To ensure correct operation on older versions of the framework that did not provide a Uri
     * argument, applications should also implement this method whenever they implement the {@link
     * #onChange(boolean, Uri)} overload.
     *
     * @param selfChange True if this is a self-change notification.
     */
    @Override
    public void onChange(boolean selfChange) {
      onChange(selfChange, null);
    }

    /**
     * Called when a content change occurs. Includes the changed content Uri when available.
     *
     * @param selfChange True if this is a self-change notification.
     * @param uri The Uri of the changed content, or null if unknown.
     */
    @Override
    public void onChange(boolean selfChange, Uri uri) {
      mContentChanged = true;
    }

    /**
     * Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite). It's
     * useful to look at the Android CTS source for ideas on how to test your Android applications.
     * The reason that PollingCheck works is that, by default, the JUnit testing framework is not
     * running on the main Android application thread.
     */
    void waitForNotificationOrFail() {

      new PollingCheck(5000) {
        @Override
        protected boolean check() {
          return mContentChanged;
        }
      }.run();
      mHT.quit();
    }
  }
}
