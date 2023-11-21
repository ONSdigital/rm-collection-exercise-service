package uk.gov.ons.ctp.lib.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/** Loads JSON representation of test DTOS for unit tests */
@Slf4j
public class FixtureHelper {

  /**
   * Overloaded version
   *
   * @param <T> the type of object we expect to load and return a List of
   * @param clazz the type
   * @return the list
   * @throws Exception failed to load - does the json file exist alongside the calling class in the
   *     classpath?
   */
  public static <T> List<T> loadMethodFixtures(final Class<T[]> clazz) throws Exception {
    String callerClassName = new Exception().getStackTrace()[1].getClassName();
    return actuallyLoadFixtures(clazz, callerClassName, null, null);
  }

  /**
   * Find, deserialize and return List of dummy test objects from a json file This method derives
   * the path and file name of the json file by looking at the class and method that called it, as
   * well as the name of the type you asked it to return.
   *
   * @param <T> the type of object we expect to load and return a List of
   * @param clazz the type
   * @param qualifier added to file name to allow a class to have multiple forms of same type
   * @return the list
   * @throws Exception failed to load - does the json file exist alongside the calling class in the
   *     classpath?
   */
  public static <T> List<T> loadMethodFixtures(final Class<T[]> clazz, final String qualifier)
      throws Exception {
    String callerClassName = new Exception().getStackTrace()[1].getClassName();
    String callerMethodName = new Exception().getStackTrace()[1].getMethodName();
    return actuallyLoadFixtures(clazz, callerClassName, callerMethodName, qualifier);
  }

  /**
   * Overloaded version
   *
   * @param <T> the type of object we expect to load and return a List of
   * @param clazz the type
   * @return the list
   * @throws Exception failed to load - does the json file exist alongside the calling class in the
   *     classpath?
   */
  public static <T> List<T> loadClassFixtures(final Class<T[]> clazz) throws Exception {
    String callerClassName = new Exception().getStackTrace()[1].getClassName();
    return actuallyLoadFixtures(clazz, callerClassName, null, null);
  }

  /**
   * Find, deserialize and return List of dummy test objects from a json file This method derives
   * the path and file name of the json file by looking at the class and method that called it, as
   * well as the name of the type you asked it to return.
   *
   * @param <T> the type of object we expect to load and return a List of
   * @param clazz the type
   * @param qualifier added to file name to allow a class to have multiple forms of same type
   * @return the list
   * @throws Exception failed to load - does the json file exist alongside the calling class in the
   *     classpath?
   */
  public static <T> List<T> loadClassFixtures(final Class<T[]> clazz, final String qualifier)
      throws Exception {
    String callerClassName = new Exception().getStackTrace()[1].getClassName();
    return actuallyLoadFixtures(clazz, callerClassName, null, qualifier);
  }

  /**
   * Actually does the dummy loading!
   *
   * @param clazz the type
   * @param <T> the type of object we expect to load and return a List of
   * @param callerClassName name of the class that made the initial call
   * @param callerMethodName name of the method that made the initial call
   * @param qualifier added to file name to allow a class to have multiple forms of same type
   * @return the loaded dummies of the the type T in a List
   * @throws Exception summats went wrong
   */
  private static <T> List<T> actuallyLoadFixtures(
      final Class<T[]> clazz,
      final String callerClassName,
      final String callerMethodName,
      final String qualifier)
      throws Exception {
    List<T> dummies = null;
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    String clazzName = clazz.getSimpleName().replaceAll("[\\[\\]]", "");
    String path = generatePath(callerClassName, clazzName, callerMethodName, qualifier);
    try {
      File file = new File(ClassLoader.getSystemResource(path).getFile());
      dummies = Arrays.asList(mapper.readValue(file, clazz));
    } catch (Throwable t) {
      log.warn("Problem loading fixture {} reason {}", path, t.getMessage());
      throw t;
    }
    return dummies;
  }

  /**
   * Format the path name to the json file, using optional params ie
   * "uk/gov/ons/ctp/response/action/thing/ThingTest.testThingOK.blueThings.json"
   *
   * @param callerClassName the name of the class that made the initial call
   * @param clazzName the type of object to deserialize and return in a List
   * @param methodName the name of the method in the callerClass that made the initial call
   * @param qualifier further quaification is a single method may need to have two collections of
   *     the same type, qualified
   * @return the constructed path string
   */
  private static String generatePath(
      final String callerClassName,
      final String clazzName,
      final String methodName,
      final String qualifier) {
    return callerClassName.replaceAll("\\.", "/")
        + "."
        + ((methodName != null) ? (methodName + ".") : "")
        + clazzName
        + "."
        + ((qualifier != null) ? (qualifier + ".") : "")
        + "json";
  }
}
