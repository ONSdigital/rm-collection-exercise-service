package uk.gov.ons.ctp.lib.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/** Some individual methods for unit tests to reuse */
public class TestHelper {

  /**
   * Creates an instance of the target class, using its default constructor, and invokes the private
   * method, passing the provided params.
   *
   * @param target the Class owning the provate method
   * @param methodName the name of the private method we wish to invoke
   * @param params the params we wish to send to the private method
   * @return the object that came back from the method!
   * @throws Exception Something went wrong with reflection, Get over it.
   */
  public static Object callPrivateMethodOfDefaultConstructableClass(
      final Class<?> target, final String methodName, final Object... params) throws Exception {
    Constructor<?> constructor = target.getConstructor();
    Object instance = constructor.newInstance();
    Class<?>[] parameterTypes = new Class[params.length];
    for (int i = 0; i < params.length; i++) {
      parameterTypes[i] = params[i].getClass();
    }
    Method methodUnderTest = instance.getClass().getDeclaredMethod(methodName, parameterTypes);
    methodUnderTest.setAccessible(true);
    return methodUnderTest.invoke(instance, params);
  }

  /**
   * Creates and returns Test Date
   *
   * @param date date to parse
   * @return String test date as String
   */
  public static String createTestDate(String date) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    ZonedDateTime zdt = ZonedDateTime.parse(date, formatter);
    ZonedDateTime compareDate = zdt.withZoneSameInstant(ZoneOffset.systemDefault());
    return formatter.format(compareDate);
  }
}
